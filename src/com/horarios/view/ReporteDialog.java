package com.horarios.view;



import com.horarios.controller.AsignaturaController;

import com.horarios.controller.AulaController;

import com.horarios.controller.BitacoraController;

import com.horarios.controller.DocenteController;

import com.horarios.controller.HorarioController;

import com.horarios.controller.UsuarioController;

import com.horarios.model.Docente;

import com.horarios.model.Horario;

import com.horarios.model.Usuario;

import com.horarios.util.PermisosRol;

import com.horarios.util.ReporteService;

import com.horarios.util.TipoReporte;

import java.awt.BorderLayout;

import java.awt.FlowLayout;

import java.awt.GridBagConstraints;

import java.awt.GridBagLayout;

import java.awt.Insets;

import java.io.File;

import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;

import java.util.List;

import javax.swing.BorderFactory;

import javax.swing.JButton;

import javax.swing.JComboBox;

import javax.swing.JDialog;

import javax.swing.JFileChooser;

import javax.swing.JLabel;

import javax.swing.JPanel;

import javax.swing.filechooser.FileNameExtensionFilter;



/**

 * Diálogo para reportes PDF (General, Semanal, Quincenal, Mensual) y resumen.

 */

public class ReporteDialog extends JDialog {



    private static final String TODOS = "— Todos los docentes —";

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");



    private final HorarioController horarioController;

    private final DocenteController docenteController;

    private final AsignaturaController asignaturaController;

    private final AulaController aulaController;

    private final UsuarioController usuarioController;

    private final BitacoraController bitacora;

    private final Usuario usuario;



    private final JComboBox<TipoReporte> cboTipo = new JComboBox<>(TipoReporte.values());

    private final JComboBox<OpcionDocente> cboDocente = new JComboBox<>();

    private final JLabel lblAyuda = new JLabel();



    public ReporteDialog(

            java.awt.Frame owner,

            HorarioController horarioController,

            DocenteController docenteController,

            AsignaturaController asignaturaController,

            AulaController aulaController,

            UsuarioController usuarioController,

            BitacoraController bitacora,

            Usuario usuario) {

        super(owner, "Reportes PDF — UNEFA", true);

        this.horarioController = horarioController;

        this.docenteController = docenteController;

        this.asignaturaController = asignaturaController;

        this.aulaController = aulaController;

        this.usuarioController = usuarioController;

        this.bitacora = bitacora;

        this.usuario = usuario;

        construir();

        actualizarVistaPrevia();

        setSize(500, 240);

        setLocationRelativeTo(owner);

    }



    private void construir() {

        JPanel root = new JPanel(new BorderLayout(12, 12));

        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));



        JPanel form = new JPanel(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();

        g.insets = new Insets(6, 4, 6, 8);

        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0;

        g.gridy = 0;

        form.add(new JLabel("Tipo de reporte:"), g);

        g.gridx = 1;

        g.fill = GridBagConstraints.HORIZONTAL;

        g.weightx = 1;

        form.add(cboTipo, g);

        cboTipo.addActionListener(e -> actualizarVistaPrevia());



        g.gridy = 1;

        g.gridx = 0;

        g.weightx = 0;

        g.fill = GridBagConstraints.NONE;

        form.add(new JLabel("Docente (opcional):"), g);

        g.gridx = 1;

        g.fill = GridBagConstraints.HORIZONTAL;

        g.weightx = 1;

        llenarDocentes();

        form.add(cboDocente, g);

        cboDocente.addActionListener(e -> actualizarVistaPrevia());



        lblAyuda.setText("<html><small>Cargando vista previa…</small></html>");

        g.gridy = 2;

        g.gridx = 0;

        g.gridwidth = 2;

        form.add(lblAyuda, g);



        root.add(form, BorderLayout.CENTER);



        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        JButton btnPdf = new JButton("Generar PDF horarios");

        JButton btnResumen = new JButton("PDF resumen catálogos");

        JButton btnCerrar = new JButton("Cerrar");



        btnPdf.addActionListener(e -> generarPdfHorarios());

        btnResumen.addActionListener(e -> generarPdfResumen());

        btnCerrar.addActionListener(e -> dispose());



        if (!PermisosRol.puedeExportarReportes(usuario)) {

            btnPdf.setEnabled(false);

            btnResumen.setEnabled(false);

        }



        botones.add(btnResumen);

        botones.add(btnPdf);

        botones.add(btnCerrar);

        root.add(botones, BorderLayout.SOUTH);

        setContentPane(root);

    }



    private void actualizarVistaPrevia() {

        TipoReporte tipo = (TipoReporte) cboTipo.getSelectedItem();

        if (tipo == null) {

            return;

        }

        List<Horario> filtrados = ReporteService.filtrarParaReporte(

                horarioController.listarActivos(), tipo, idDocenteSeleccionado());

        String color = filtrados.isEmpty() ? "#a00" : "#060";

        lblAyuda.setText("<html><small>" + ReporteService.textoAyudaTipo(tipo)

                + "<br/><span style='color:" + color + ";'><b>Incluirá " + filtrados.size()

                + " horario(s)</b> en el PDF (misma plantilla, datos según el tipo).</span></small></html>");

    }



    private void llenarDocentes() {

        cboDocente.removeAllItems();

        cboDocente.addItem(new OpcionDocente(-1, TODOS));

        if (docenteController != null) {

            for (Docente d : docenteController.listarActivosParaHorarios()) {

                cboDocente.addItem(new OpcionDocente(d.getIdDocente(),

                        d.getCedula() + " — " + d.getNombres()));

            }

        }

    }



    private int idDocenteSeleccionado() {

        OpcionDocente o = (OpcionDocente) cboDocente.getSelectedItem();

        return o != null ? o.id : -1;

    }



    private void generarPdfHorarios() {

        TipoReporte tipo = (TipoReporte) cboTipo.getSelectedItem();

        if (tipo == null) {

            return;

        }

        ReporteService.elegirYExportarPdfHorarios(tipo, idDocenteSeleccionado(),

                horarioController, docenteController, asignaturaController, aulaController,

                bitacora, usuario);

    }



    private void generarPdfResumen() {

        JFileChooser fc = new JFileChooser();

        fc.setDialogTitle("Guardar resumen PDF");

        fc.setSelectedFile(new File("resumen_" + LocalDateTime.now().format(FMT) + ".pdf"));

        fc.setFileFilter(new FileNameExtensionFilter("PDF (*.pdf)", "pdf"));

        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {

            return;

        }

        File dest = fc.getSelectedFile();

        if (!dest.getName().toLowerCase().endsWith(".pdf")) {

            dest = new File(dest.getAbsolutePath() + ".pdf");

        }

        ReporteService.exportarPdfResumenJasper(docenteController, asignaturaController, aulaController,

                horarioController, usuarioController, bitacora, usuario, dest);

    }



    private static final class OpcionDocente {

        final int id;

        final String etiqueta;



        OpcionDocente(int id, String etiqueta) {

            this.id = id;

            this.etiqueta = etiqueta;

        }



        @Override

        public String toString() {

            return etiqueta;

        }

    }

}


