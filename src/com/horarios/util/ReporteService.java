package com.horarios.util;

import com.horarios.controller.AsignaturaController;
import com.horarios.controller.AulaController;
import com.horarios.controller.BitacoraController;
import com.horarios.controller.DocenteController;
import com.horarios.controller.HorarioController;
import com.horarios.controller.UsuarioController;
import com.horarios.model.Asignatura;
import com.horarios.model.Aula;
import com.horarios.model.Docente;
import com.horarios.model.Horario;
import com.horarios.model.Usuario;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Exportación de reportes (CSV y PDF vía Jasper si las librerías están en lib/).
 */
public final class ReporteService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private ReporteService() {
    }

    public static List<FilaReporteHorario> armarFilas(
            List<Horario> horarios,
            DocenteController docenteController,
            AsignaturaController asignaturaController,
            AulaController aulaController) {
        List<FilaReporteHorario> filas = new ArrayList<>();
        if (horarios == null) {
            return filas;
        }
        for (Horario h : horarios) {
            Asignatura asig = asignaturaController != null ? asignaturaController.buscarPorId(h.getIdAsignatura()) : null;
            Docente doc = docenteController != null ? docenteController.buscarPorId(h.getIdDocente()) : null;
            Aula aula = aulaController != null ? aulaController.buscarPorId(h.getIdAula()) : null;
            String codigo = asig != null ? asig.getCodigo() : String.valueOf(h.getIdAsignatura());
            String nombreAsig = asig != null ? asig.getNombre() : codigo;
            String docente = doc != null
                    ? doc.getCedula() + " " + doc.getNombres() + " " + (doc.getApellidos() != null ? doc.getApellidos() : "")
                    : String.valueOf(h.getIdDocente());
            String aulaTxt = aula != null ? aula.getCodigo() : String.valueOf(h.getIdAula());
            long min = 0;
            if (h.getHoraInicio() != null && h.getHoraFin() != null) {
                min = (h.getHoraFin().getTime() - h.getHoraInicio().getTime()) / 60000L;
            }
            filas.add(new FilaReporteHorario(
                    h.getDiaSemana(),
                    formatoFranja(h.getHoraInicio(), h.getHoraFin()),
                    nombreAsig,
                    docente.trim(),
                    aulaTxt,
                    codigo,
                    min > 0 ? String.valueOf(min) + " min" : ""));
        }
        return filas;
    }

    public static List<Horario> filtrarPorPeriodo(List<Horario> activos, TipoReporte tipo) {
        if (activos == null) {
            return new ArrayList<>();
        }
        if (tipo == null || tipo == TipoReporte.GENERAL || tipo.getDiasAtras() <= 0) {
            return new ArrayList<>(activos);
        }
        LocalDateTime limite = LocalDateTime.now().minusDays(tipo.getDiasAtras());
        List<Horario> out = new ArrayList<>();
        for (Horario h : activos) {
            if (h.getFechaRegistro() == null) {
                continue;
            }
            if (!h.getFechaRegistro().toLocalDateTime().isBefore(limite)) {
                out.add(h);
            }
        }
        return out;
    }

    public static List<Horario> filtrarParaReporte(List<Horario> activos, TipoReporte tipo, int idDocente) {
        return filtrarPorDocente(filtrarPorPeriodo(activos, tipo), idDocente);
    }

    public static String textoAyudaTipo(TipoReporte tipo) {
        if (tipo == null || tipo == TipoReporte.GENERAL) {
            return "General: todos los horarios activos, sin límite de fecha.";
        }
        LocalDateTime desde = LocalDateTime.now().minusDays(tipo.getDiasAtras());
        return tipo.getEtiqueta() + ": solo horarios registrados desde "
                + desde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + " (últimos " + tipo.getDiasAtras() + " días).";
    }

    public static String armarSubtituloPdf(TipoReporte tipo, int cantidadHorarios, int idDocente,
            DocenteController docenteController) {
        StringBuilder sb = new StringBuilder();
        if (tipo == TipoReporte.GENERAL) {
            sb.append("Reporte general — todos los horarios activos");
        } else {
            LocalDateTime desde = LocalDateTime.now().minusDays(tipo.getDiasAtras());
            sb.append("Reporte ").append(tipo.getEtiqueta().toLowerCase())
                    .append(" — registrados desde ")
                    .append(desde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        sb.append(" · ").append(cantidadHorarios).append(" horario(s) en el PDF");
        if (idDocente > 0 && docenteController != null) {
            Docente docF = docenteController.buscarPorId(idDocente);
            if (docF != null) {
                sb.append(" · Docente: ").append(docF.getCedula()).append(" — ").append(docF.getNombres());
                if (docF.getApellidos() != null) {
                    sb.append(" ").append(docF.getApellidos());
                }
            }
        }
        return sb.toString();
    }

    public static List<Horario> filtrarPorDocente(List<Horario> lista, int idDocente) {
        if (idDocente <= 0 || lista == null) {
            return lista != null ? new ArrayList<>(lista) : new ArrayList<>();
        }
        List<Horario> out = new ArrayList<>();
        for (Horario h : lista) {
            if (h.getIdDocente() == idDocente) {
                out.add(h);
            }
        }
        return out;
    }

    public static void exportarPdfHorariosJasper(
            TipoReporte tipo,
            int idDocenteOpcional,
            HorarioController horarioController,
            DocenteController docenteController,
            AsignaturaController asignaturaController,
            AulaController aulaController,
            BitacoraController bitacora,
            Usuario usuario,
            File destino) {
        List<Horario> filtrados = filtrarParaReporte(horarioController.listarActivos(), tipo, idDocenteOpcional);
        if (filtrados.isEmpty()) {
            Mensajes.error("No hay horarios para «" + tipo.getEtiqueta() + "» con los filtros actuales.\n\n"
                    + "Pruebe «General» o otro docente.");
            return;
        }
        List<FilaLeyendaPdf> leyenda = ReporteHorarioLayoutBuilder.construirLeyenda(
                filtrados, docenteController, asignaturaController, aulaController);
        List<FilaGrillaPdf> grilla = ReporteHorarioLayoutBuilder.construirGrilla(filtrados, asignaturaController);
        try {
            File jrxml = ReporteJasperUtil.resolverPlantilla("horario_docente.jrxml");
            File jrxmlLeyenda = ReporteJasperUtil.resolverPlantilla("horario_leyenda_sub.jrxml");
            Map<String, Object> params = new HashMap<>();
            params.put("TITULO", "Horario de clases — " + tipo.getEtiqueta());
            params.put("SUBTITULO", armarSubtituloPdf(tipo, filtrados.size(), idDocenteOpcional, docenteController));
            params.put("FECHA_GENERACION", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            ReporteJasperUtil.exportarHorarioDocentePdf(jrxml, jrxmlLeyenda, destino, params, grilla, leyenda);
            registrarExportOk(bitacora, usuario, "PDF horarios " + tipo.getEtiqueta() + ": " + destino.getAbsolutePath());
            Mensajes.info("PDF guardado:\n" + destino.getAbsolutePath());
            abrirSiPosible(destino);
        } catch (ClassNotFoundException ex) {
            Mensajes.error("JasperReports no está en el classpath.\n\nAgregue los JAR en lib/ (ver lib/LEEME_JASPER.txt).\n\nPuede usar Exportar CSV como alternativa.");
            registrarExportFail(bitacora, usuario, ex.getMessage());
        } catch (Exception ex) {
            Mensajes.error("No se pudo generar el PDF.\n\n" + mensajeExcepcion(ex)
                    + "\n\nSi faltan dependencias, ejecute lib/descargar_dependencias.ps1");
            registrarExportFail(bitacora, usuario, mensajeExcepcion(ex));
            exportarHorariosCsvFallback(filtrados, docenteController, asignaturaController, aulaController, destino);
        }
    }

    private static String mensajeExcepcion(Throwable ex) {
        if (ex == null) {
            return "Error desconocido";
        }
        String msg = ex.getMessage();
        if (msg != null && !msg.trim().isEmpty()) {
            return msg;
        }
        Throwable cause = ex.getCause();
        if (cause != null && cause != ex) {
            String cm = mensajeExcepcion(cause);
            if (!cm.isEmpty()) {
                return ex.getClass().getSimpleName() + ": " + cm;
            }
        }
        return ex.getClass().getSimpleName() + " (revise la consola de NetBeans para el detalle)";
    }

    public static void exportarPdfResumenJasper(
            DocenteController docenteController,
            AsignaturaController asignaturaController,
            AulaController aulaController,
            HorarioController horarioController,
            UsuarioController usuarioController,
            BitacoraController bitacora,
            Usuario usuario,
            File destino) {
        List<FilaResumenReporte> filas = new ArrayList<>();
        filas.add(new FilaResumenReporte("Docentes", docenteController.listarActivos().size()));
        filas.add(new FilaResumenReporte("Asignaturas", asignaturaController.listarActivas().size()));
        filas.add(new FilaResumenReporte("Aulas", aulaController.listarActivas().size()));
        filas.add(new FilaResumenReporte("Horarios", horarioController.listarActivos().size()));
        filas.add(new FilaResumenReporte("Usuarios", usuarioController.contarActivos()));
        try {
            File jrxml = ReporteJasperUtil.resolverPlantilla("reporte_resumen.jrxml");
            Map<String, Object> params = new HashMap<>();
            params.put("TITULO", "Resumen general — UNEFA");
            params.put("FECHA_GENERACION", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            ReporteJasperUtil.exportarBeanCollectionPdf(jrxml, destino, params, filas);
            registrarExportOk(bitacora, usuario, "PDF resumen: " + destino.getAbsolutePath());
            Mensajes.info("PDF resumen guardado:\n" + destino.getAbsolutePath());
            abrirSiPosible(destino);
        } catch (ClassNotFoundException ex) {
            Mensajes.error("JasperReports no disponible. Use CSV desde Inicio o lib/LEEME_JASPER.txt.");
            registrarExportFail(bitacora, usuario, ex.getMessage());
        } catch (Exception ex) {
            Mensajes.error("No se pudo generar el PDF de resumen.\n\n" + mensajeExcepcion(ex));
            registrarExportFail(bitacora, usuario, mensajeExcepcion(ex));
        }
    }

    public static void elegirYExportarPdfHorarios(
            TipoReporte tipo,
            int idDocenteOpcional,
            HorarioController horarioController,
            DocenteController docenteController,
            AsignaturaController asignaturaController,
            AulaController aulaController,
            BitacoraController bitacora,
            Usuario usuario) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar reporte PDF — " + tipo.getEtiqueta());
        String suf = idDocenteOpcional > 0 ? "_doc" + idDocenteOpcional : "";
        fc.setSelectedFile(new File("horarios_" + tipo.name().toLowerCase() + suf + "_"
                + LocalDateTime.now().format(FMT) + ".pdf"));
        fc.setFileFilter(new FileNameExtensionFilter("PDF (*.pdf)", "pdf"));
        if (fc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File dest = fc.getSelectedFile();
        if (!dest.getName().toLowerCase().endsWith(".pdf")) {
            dest = new File(dest.getAbsolutePath() + ".pdf");
        }
        exportarPdfHorariosJasper(tipo, idDocenteOpcional, horarioController, docenteController,
                asignaturaController, aulaController, bitacora, usuario, dest);
    }

    public static void exportarHorariosCsv(
            HorarioController horarioController,
            DocenteController docenteController,
            AsignaturaController asignaturaController,
            AulaController aulaController,
            BitacoraController bitacora,
            Usuario usuario,
            int idDocenteFiltro) {
        List<Horario> lista = idDocenteFiltro > 0
                ? horarioController.listarActivosPorDocente(idDocenteFiltro)
                : horarioController.listarActivos();
        if (lista.isEmpty()) {
            Mensajes.error("No hay horarios activos para exportar.");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar reporte de horarios (CSV)");
        fc.setSelectedFile(new File("horarios_" + LocalDateTime.now().format(FMT) + ".csv"));
        fc.setFileFilter(new FileNameExtensionFilter("CSV (*.csv)", "csv"));
        if (fc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File dest = fc.getSelectedFile();
        if (!dest.getName().toLowerCase().endsWith(".csv")) {
            dest = new File(dest.getAbsolutePath() + ".csv");
        }
        exportarHorariosCsvFallback(lista, docenteController, asignaturaController, aulaController, dest);
        if (bitacora != null && usuario != null) {
            bitacora.registrar(usuario.getIdUsuario(), usuario.getUsername(), usuario.getRol(),
                    BitacoraController.ACCION_EXPORT, PermisosRol.MOD_HORARIOS,
                    "Exportación CSV: " + dest.getAbsolutePath(), true);
        }
        Mensajes.info("Reporte guardado:\n" + dest.getAbsolutePath());
        abrirSiPosible(dest);
    }

    public static void exportarResumenGeneralCsv(
            DocenteController docenteController,
            AsignaturaController asignaturaController,
            AulaController aulaController,
            HorarioController horarioController,
            UsuarioController usuarioController,
            BitacoraController bitacora,
            Usuario usuario) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar reporte general (CSV)");
        fc.setSelectedFile(new File("reporte_general_" + LocalDateTime.now().format(FMT) + ".csv"));
        fc.setFileFilter(new FileNameExtensionFilter("CSV (*.csv)", "csv"));
        if (fc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File dest = fc.getSelectedFile();
        if (!dest.getName().toLowerCase().endsWith(".csv")) {
            dest = new File(dest.getAbsolutePath() + ".csv");
        }
        try (OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(dest), StandardCharsets.UTF_8)) {
            w.write("modulo;cantidad_activos\n");
            w.write("docentes;" + docenteController.listarActivos().size() + "\n");
            w.write("asignaturas;" + asignaturaController.listarActivas().size() + "\n");
            w.write("aulas;" + aulaController.listarActivas().size() + "\n");
            w.write("horarios;" + horarioController.listarActivos().size() + "\n");
            w.write("usuarios;" + usuarioController.contarActivos() + "\n");
            w.flush();
            registrarExportOk(bitacora, usuario, "Reporte general CSV: " + dest.getAbsolutePath());
            Mensajes.info("Reporte general guardado:\n" + dest.getAbsolutePath());
        } catch (Exception ex) {
            Mensajes.error("No se pudo guardar el reporte general.\n\n" + ex.getMessage());
        }
    }

    public static void imprimirVistaHorarios(
            List<Horario> horarios,
            DocenteController docenteController,
            AsignaturaController asignaturaController,
            AulaController aulaController,
            String titulo) {
        List<String> lineas = filasHorarioTexto(horarios, docenteController, asignaturaController, aulaController);
        if (lineas.isEmpty()) {
            Mensajes.error("No hay filas para imprimir.");
            return;
        }
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName(titulo != null ? titulo : "Horarios UNEFA");
        job.setPrintable(new Printable() {
            private int idx;

            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0) {
                    return NO_SUCH_PAGE;
                }
                Graphics2D g2 = (Graphics2D) graphics;
                g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
                int y = 12;
                g2.drawString(titulo != null ? titulo : "Horarios", 0, y);
                y += 18;
                for (String ln : lineas) {
                    if (y > pageFormat.getImageableHeight() - 12) {
                        g2.drawString("... (más líneas en PDF/CSV)", 0, y);
                        break;
                    }
                    g2.drawString(ln, 0, y);
                    y += 14;
                }
                return PAGE_EXISTS;
            }
        });
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                Mensajes.error("Error al imprimir.\n\n" + ex.getMessage());
            }
        }
    }

    private static void exportarHorariosCsvFallback(
            List<Horario> lista,
            DocenteController docenteController,
            AsignaturaController asignaturaController,
            AulaController aulaController,
            File dest) {
        try (OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(dest), StandardCharsets.UTF_8)) {
            w.write("id_horario;docente;asignatura;aula;dia;hora_inicio;hora_fin\n");
            for (Horario h : lista) {
                w.write(h.getIdHorario() + ";");
                w.write(etiquetaDocente(docenteController, h.getIdDocente()) + ";");
                w.write(etiquetaAsignatura(asignaturaController, h.getIdAsignatura()) + ";");
                w.write(etiquetaAula(aulaController, h.getIdAula()) + ";");
                w.write(h.getDiaSemana() + ";");
                w.write(h.getHoraInicio() + ";" + h.getHoraFin() + "\n");
            }
        } catch (Exception ex) {
            Mensajes.error("Fallback CSV falló.\n\n" + ex.getMessage());
        }
    }

    private static String formatoFranja(Time ini, Time fin) {
        if (ini == null || fin == null) {
            return "";
        }
        return ini.toString().substring(0, 5) + " - " + fin.toString().substring(0, 5);
    }

    private static void registrarExportOk(BitacoraController bitacora, Usuario usuario, String detalle) {
        if (bitacora != null && usuario != null) {
            bitacora.registrar(usuario.getIdUsuario(), usuario.getUsername(), usuario.getRol(),
                    BitacoraController.ACCION_EXPORT, PermisosRol.MOD_HORARIOS, detalle, true);
        }
    }

    private static void registrarExportFail(BitacoraController bitacora, Usuario usuario, String detalle) {
        if (bitacora != null && usuario != null) {
            bitacora.registrar(usuario.getIdUsuario(), usuario.getUsername(), usuario.getRol(),
                    BitacoraController.ACCION_EXPORT, PermisosRol.MOD_HORARIOS,
                    "Error exportación PDF: " + detalle, false);
        }
    }

    private static void abrirSiPosible(File dest) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(dest);
            } catch (Exception ignored) {
            }
        }
    }

    private static String etiquetaDocente(DocenteController dc, int id) {
        Docente d = dc != null ? dc.buscarPorId(id) : null;
        return d != null ? d.getCedula() + " " + d.getNombres() : String.valueOf(id);
    }

    private static String etiquetaAsignatura(AsignaturaController ac, int id) {
        Asignatura a = ac != null ? ac.buscarPorId(id) : null;
        return a != null ? a.getCodigo() : String.valueOf(id);
    }

    private static String etiquetaAula(AulaController auc, int id) {
        Aula au = auc != null ? auc.buscarPorId(id) : null;
        return au != null ? au.getCodigo() : String.valueOf(id);
    }

    public static List<String> filasHorarioTexto(
            List<Horario> horarios,
            DocenteController docenteController,
            AsignaturaController asignaturaController,
            AulaController aulaController) {
        List<String> filas = new ArrayList<>();
        for (Horario h : horarios) {
            filas.add(h.getDiaSemana() + " " + h.getHoraInicio() + "-" + h.getHoraFin()
                    + " | " + etiquetaAsignatura(asignaturaController, h.getIdAsignatura())
                    + " | " + etiquetaDocente(docenteController, h.getIdDocente())
                    + " | Aula " + etiquetaAula(aulaController, h.getIdAula()));
        }
        return filas;
    }
}
