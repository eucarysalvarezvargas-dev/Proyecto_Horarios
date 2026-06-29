package com.horarios.util;

import com.horarios.controller.AsignaturaController;
import com.horarios.controller.AulaController;
import com.horarios.controller.DocenteController;
import com.horarios.controller.HorarioController;
import com.horarios.model.Asignatura;
import com.horarios.model.Aula;
import com.horarios.model.Docente;
import com.horarios.model.Horario;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Construye leyenda y cuadrícula del PDF alineadas con la vista de {@code HorarioPanel}.
 * No existe campo «sección» en el sistema; la columna equivalente es <b>AULA</b>.
 */
public final class ReporteHorarioLayoutBuilder {

    private static final int SLOT_MINUTOS = 30;
    private static final int MINUTOS_DEF_INI = 7 * 60;
    private static final int MINUTOS_DEF_FIN = 22 * 60;
    private static final String[] DIAS_LUN_A_DOM = {
        "LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO"
    };

    private ReporteHorarioLayoutBuilder() {
    }

    public static List<FilaLeyendaPdf> construirLeyenda(
            List<Horario> horarios,
            DocenteController docenteController,
            AsignaturaController asignaturaController,
            AulaController aulaController) {
        Map<String, FilaLeyendaPdf> map = new LinkedHashMap<>();
        for (Horario h : horarios) {
            String clave = h.getIdAsignatura() + "|" + h.getIdDocente() + "|" + h.getIdAula();
            if (map.containsKey(clave)) {
                continue;
            }
            Asignatura a = asignaturaController != null ? asignaturaController.buscarPorId(h.getIdAsignatura()) : null;
            Docente d = docenteController != null ? docenteController.buscarPorId(h.getIdDocente()) : null;
            Aula au = aulaController != null ? aulaController.buscarPorId(h.getIdAula()) : null;
            String cod = a != null && a.getCodigo() != null ? a.getCodigo().trim() : "?";
            String nom = a != null && a.getNombre() != null ? a.getNombre().trim().toUpperCase(Locale.ROOT) : "";
            String aula = au != null && au.getCodigo() != null ? au.getCodigo().trim().toUpperCase(Locale.ROOT) : "";
            String doc = formatearDocentePdf(d);
            map.put(clave, new FilaLeyendaPdf(0, "", cod, nom, aula, doc));
        }
        List<FilaLeyendaPdf> filas = new ArrayList<>(map.values());
        filas.sort(Comparator.comparing((FilaLeyendaPdf f) -> f.getCodAsig().toLowerCase(Locale.ROOT))
                .thenComparing(f -> f.getDocente().toLowerCase(Locale.ROOT)));
        for (int i = 0; i < filas.size(); i++) {
            FilaLeyendaPdf f = filas.get(i);
            filas.set(i, new FilaLeyendaPdf(i + 1, String.format("%02d", i + 1),
                    f.getCodAsig(), f.getNombreAsig(), f.getAula(), f.getDocente()));
        }
        return filas;
    }

    public static List<FilaGrillaPdf> construirGrilla(
            List<Horario> horarios,
            AsignaturaController asignaturaController) {
        List<FilaGrillaPdf> filas = new ArrayList<>();
        if (horarios == null || horarios.isEmpty()) {
            return filas;
        }
        int minM = Integer.MAX_VALUE;
        int maxM = Integer.MIN_VALUE;
        for (Horario h : horarios) {
            if (h.getHoraInicio() != null && h.getHoraFin() != null) {
                int a = minutosDia(h.getHoraInicio());
                int b = minutosDia(h.getHoraFin());
                minM = Math.min(minM, redondearAbajo(a, SLOT_MINUTOS));
                maxM = Math.max(maxM, redondearArriba(b, SLOT_MINUTOS));
            }
        }
        if (minM == Integer.MAX_VALUE || maxM == Integer.MIN_VALUE) {
            minM = MINUTOS_DEF_INI;
            maxM = MINUTOS_DEF_INI + SLOT_MINUTOS * 4;
        }
        for (int slotIni = minM; slotIni < maxM; slotIni += SLOT_MINUTOS) {
            int slotFin = slotIni + SLOT_MINUTOS;
            if (!franjaConContenido(horarios, slotIni, slotFin)) {
                continue;
            }
            filas.add(new FilaGrillaPdf(
                    formatoRango(slotIni, slotFin),
                    textoCelda(horarios, asignaturaController, 0, slotIni, slotFin),
                    textoCelda(horarios, asignaturaController, 1, slotIni, slotFin),
                    textoCelda(horarios, asignaturaController, 2, slotIni, slotFin),
                    textoCelda(horarios, asignaturaController, 3, slotIni, slotFin),
                    textoCelda(horarios, asignaturaController, 4, slotIni, slotFin),
                    textoCelda(horarios, asignaturaController, 5, slotIni, slotFin),
                    textoCelda(horarios, asignaturaController, 6, slotIni, slotFin)));
        }
        return filas;
    }

    /** Solo filas de franja donde al menos un día tiene clase (evita huecos vacíos hasta las 22:00). */
    private static boolean franjaConContenido(List<Horario> horarios, int slotIni, int slotFin) {
        for (int d = 0; d < 7; d++) {
            if (!horariosEnSlot(horarios, d, slotIni, slotFin).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static String textoCelda(List<Horario> lista, AsignaturaController ac, int diaIndex, int slotIni, int slotFin) {
        List<Horario> enSlot = horariosEnSlot(lista, diaIndex, slotIni, slotFin);
        if (enSlot.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < enSlot.size(); k++) {
            if (k > 0) {
                sb.append(" / ");
            }
            sb.append(codigoAsignatura(enSlot.get(k), ac));
        }
        return sb.toString();
    }

    private static String codigoAsignatura(Horario h, AsignaturaController ac) {
        if (h == null) {
            return "";
        }
        Asignatura a = ac != null ? ac.buscarPorId(h.getIdAsignatura()) : null;
        return a != null && a.getCodigo() != null ? a.getCodigo().trim() : "";
    }

    private static List<Horario> horariosEnSlot(List<Horario> lista, int diaIndex, int slotIni, int slotFin) {
        List<Horario> r = new ArrayList<>();
        for (Horario h : lista) {
            String nd = HorarioController.normalizarDia(h.getDiaSemana());
            if (nd == null || indiceDia(nd) != diaIndex) {
                continue;
            }
            if (h.getHoraInicio() == null || h.getHoraFin() == null) {
                continue;
            }
            int a = minutosDia(h.getHoraInicio());
            int b = minutosDia(h.getHoraFin());
            if (a < slotFin && b > slotIni) {
                r.add(h);
            }
        }
        return r;
    }

    private static String formatearDocentePdf(Docente d) {
        if (d == null) {
            return "";
        }
        String ap = d.getApellidos() != null ? d.getApellidos().trim() : "";
        String no = d.getNombres() != null ? d.getNombres().trim() : "";
        String completo = (ap + " " + no).trim();
        return completo.toUpperCase(Locale.ROOT);
    }

    private static int indiceDia(String normalizado) {
        switch (normalizado) {
            case "LUNES":
                return 0;
            case "MARTES":
                return 1;
            case "MIERCOLES":
                return 2;
            case "JUEVES":
                return 3;
            case "VIERNES":
                return 4;
            case "SABADO":
                return 5;
            case "DOMINGO":
                return 6;
            default:
                return -1;
        }
    }

    @SuppressWarnings("deprecation")
    private static int minutosDia(Time t) {
        return t.getHours() * 60 + t.getMinutes();
    }

    private static int redondearAbajo(int min, int step) {
        return (min / step) * step;
    }

    private static int redondearArriba(int min, int step) {
        return ((min + step - 1) / step) * step;
    }

    private static String formatoRango(int iniMin, int finMin) {
        return String.format("%02d:%02d – %02d:%02d", iniMin / 60, iniMin % 60, finMin / 60, finMin % 60);
    }
}
