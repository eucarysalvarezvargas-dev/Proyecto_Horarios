package com.horarios.view;

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
import com.horarios.util.Mensajes;
import com.horarios.util.PermisosRol;
import com.horarios.util.ReporteService;
import com.horarios.util.TablaUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

public class HorarioPanel extends JPanel {

    private static final String TODOS = "Todos";
    private static final String[] DIAS = {
        TODOS, "LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO"
    };
    private static final String[] DIAS_CORTOS = {"LUN", "MAR", "MIE", "JUE", "VIE", "SAB", "DOM"};
    private static final String[] DIAS_LUN_A_DOM = {
        "LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO"
    };
    private static final int SLOT_MINUTOS = 30;
    private static final int MINUTOS_DEF_INI = 7 * 60;
    private static final int MINUTOS_DEF_FIN = 22 * 60;
    private static final Color COLOR_CABECERA_GRILLA = new Color(236, 239, 241);
    private static final Color COLOR_LINEA_GRILLA = new Color(190, 193, 198);
    private static final Color COLOR_CELDA_VACIA_TEXTO = new Color(150, 150, 150);
    private static final String PROP_BORDE_BASE_GRILLA = "bordeBaseGrilla";
    /** Pista de búsqueda (tooltip y fila de ayuda); el campo usa etiqueta corta «Buscar:» para alinear la rejilla. */
    private static final String PISTA_TEXTO_BUSQUEDA_HORARIO
            = "Incluye código o nombre de asignatura, docente, aula, día, franja horaria e ID de horario.";
    /** Paleta fija: un color estable por id de asignatura (sin aleatoriedad). */
    private static final Color[] PALETA_BORDE_ASIGNATURA = {
        new Color(30, 136, 229),
        new Color(211, 47, 47),
        new Color(56, 142, 60),
        new Color(123, 31, 162),
        new Color(255, 143, 0),
        new Color(0, 137, 123),
        new Color(194, 24, 91),
        new Color(63, 81, 181),
        new Color(175, 180, 43),
        new Color(0, 151, 167),
        new Color(109, 76, 65),
        new Color(94, 53, 177),
    };

    private final HorarioController controller;
    private final DocenteController docenteController;
    private final AsignaturaController asignaturaController;
    private final AulaController aulaController;
    private final Usuario usuarioActual;
    private final BitacoraController bitacoraController;
    private final UsuarioController usuarioController;

    private final LeyendaTableModel leyendaModel = new LeyendaTableModel();
    private final JTable tablaLeyenda = new JTable(leyendaModel);
    private JScrollPane scrollLeyenda;
    private JPanel panelGrilla;
    private JScrollPane scrollGrilla;
    /** Solo la cuadrícula (+ título); se muestra en el diálogo modal. */
    private JPanel panelVistaHorario;

    private JTextField txtFiltro;
    private JComboBox<String> cboDia;
    private JComboBox<FiltroDocente> cboDocenteFiltro;
    private JComboBox<FiltroAsig> cboAsigFiltro;
    private JComboBox<FiltroAula> cboAulaFiltro;

    private JTextField txtFiltroModal;
    private JComboBox<String> cboDiaModal;
    private JComboBox<FiltroDocente> cboDocenteModal;
    private JComboBox<FiltroAsig> cboAsigModal;
    private JComboBox<FiltroAula> cboAulaModal;
    private JDialog dialogoHorarioActivo;

    /** Horario de la celda pulsada (para Modificar / Consultar / Eliminar). */
    private Horario horarioSeleccionGrilla;
    private JButton celdaSeleccionada;

    private List<Horario> datosVista = new ArrayList<>();

    private static final class FiltroDocente {

        final int id;
        final String etiqueta;

        FiltroDocente(int id, String etiqueta) {
            this.id = id;
            this.etiqueta = etiqueta;
        }

        @Override
        public String toString() {
            return etiqueta;
        }
    }

    private static final class FiltroAsig {

        final int id;
        final String etiqueta;

        FiltroAsig(int id, String etiqueta) {
            this.id = id;
            this.etiqueta = etiqueta;
        }

        @Override
        public String toString() {
            return etiqueta;
        }
    }

    private static final class FiltroAula {

        final int id;
        final String etiqueta;

        FiltroAula(int id, String etiqueta) {
            this.id = id;
            this.etiqueta = etiqueta;
        }

        @Override
        public String toString() {
            return etiqueta;
        }
    }

    private static final class FilaLeyenda {

        final int indice;
        final String codigo;
        final String asignatura;
        final String docente;
        final String aula;

        FilaLeyenda(int indice, String codigo, String asignatura, String docente, String aula) {
            this.indice = indice;
            this.codigo = codigo;
            this.asignatura = asignatura;
            this.docente = docente;
            this.aula = aula;
        }
    }

    public HorarioPanel(HorarioController controller,
            DocenteController docenteController,
            AsignaturaController asignaturaController,
            AulaController aulaController,
            Usuario usuarioActual) {
        this(controller, docenteController, asignaturaController, aulaController, usuarioActual, null, null);
    }

    public HorarioPanel(HorarioController controller,
            DocenteController docenteController,
            AsignaturaController asignaturaController,
            AulaController aulaController,
            Usuario usuarioActual,
            BitacoraController bitacoraController) {
        this(controller, docenteController, asignaturaController, aulaController, usuarioActual, bitacoraController, null);
    }

    public HorarioPanel(HorarioController controller,
            DocenteController docenteController,
            AsignaturaController asignaturaController,
            AulaController aulaController,
            Usuario usuarioActual,
            BitacoraController bitacoraController,
            UsuarioController usuarioController) {
        this.controller = controller;
        this.docenteController = docenteController;
        this.asignaturaController = asignaturaController;
        this.aulaController = aulaController;
        this.usuarioActual = usuarioActual;
        this.bitacoraController = bitacoraController;
        this.usuarioController = usuarioController;
        construirUI();
        aplicarRestriccionesRolDocente();
        refrescarTabla();
    }

    /** Abre el diálogo de reportes PDF (también desde menú lateral / Inicio). */
    public void abrirDialogoReportesPdf() {
        if (usuarioActual == null || !PermisosRol.puedeExportarReportes(usuarioActual)) {
            Mensajes.error("Solo administradores y coordinadores pueden exportar reportes.");
            return;
        }
        java.awt.Frame owner = (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this);
        ReporteDialog dlg = new ReporteDialog(owner, controller, docenteController, asignaturaController,
                aulaController, usuarioController, bitacoraController, usuarioActual);
        dlg.setVisible(true);
    }

    private void aplicarRestriccionesRolDocente() {
        if (usuarioActual == null || usuarioActual.getRol() == null
                || !"DOCENTE".equalsIgnoreCase(usuarioActual.getRol().trim())) {
            return;
        }
        String ced = usuarioActual.getCedula();
        if (ced == null || ced.trim().isEmpty() || docenteController == null || cboDocenteFiltro == null) {
            return;
        }
        Docente d = docenteController.buscarPorCedula(ced.trim());
        if (d == null) {
            return;
        }
        for (int i = 0; i < cboDocenteFiltro.getItemCount(); i++) {
            FiltroDocente fd = cboDocenteFiltro.getItemAt(i);
            if (fd != null && fd.id == d.getIdDocente()) {
                cboDocenteFiltro.setSelectedIndex(i);
                break;
            }
        }
        cboDocenteFiltro.setEnabled(false);
    }

    private void llenarComboDocentes(JComboBox<FiltroDocente> cbo) {
        cbo.removeAllItems();
        cbo.addItem(new FiltroDocente(-1, TODOS));
        if (docenteController != null) {
            for (Docente d : docenteController.listarActivosParaHorarios()) {
                String nom = (d.getNombres() != null ? d.getNombres() : "") + " " + (d.getApellidos() != null ? d.getApellidos() : "");
                cbo.addItem(new FiltroDocente(d.getIdDocente(),
                        d.getIdDocente() + " — " + d.getCedula() + " — " + nom.trim()));
            }
        }
    }

    private void llenarComboAsignaturas(JComboBox<FiltroAsig> cbo) {
        cbo.removeAllItems();
        cbo.addItem(new FiltroAsig(-1, TODOS));
        if (asignaturaController != null) {
            for (Asignatura a : asignaturaController.listarActivas()) {
                cbo.addItem(new FiltroAsig(a.getIdAsignatura(),
                        a.getIdAsignatura() + " — " + a.getCodigo()));
            }
        }
    }

    private void llenarComboAulas(JComboBox<FiltroAula> cbo) {
        cbo.removeAllItems();
        cbo.addItem(new FiltroAula(-1, TODOS));
        if (aulaController != null) {
            for (Aula au : aulaController.listarActivas()) {
                cbo.addItem(new FiltroAula(au.getIdAula(), au.getIdAula() + " — " + au.getCodigo()));
            }
        }
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitulo = new JLabel("Módulo de Horarios", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel lblSubtitulo = new JLabel(
                "Consulte y administre los horarios docentes mediante filtros y la cuadrícula semanal.",
                SwingConstants.LEFT);
        lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(lblTitulo, BorderLayout.NORTH);
        header.add(lblSubtitulo, BorderLayout.CENTER);

        JPanel panelExportacion = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        panelExportacion.setOpaque(false);
        if (usuarioActual != null && PermisosRol.puedeExportarReportes(usuarioActual)) {
            JButton btnExportarCsv = new JButton("Exportar CSV");
            btnExportarCsv.setPreferredSize(new Dimension(180, 36));
            btnExportarCsv.setToolTipText("Guarda horarios filtrados en Excel (archivo .csv)");
            btnExportarCsv.addActionListener(e -> {
                FiltroDocente fd = (FiltroDocente) cboDocenteFiltro.getSelectedItem();
                int idDoc = fd != null ? fd.id : -1;
                ReporteService.exportarHorariosCsv(controller, docenteController, asignaturaController, aulaController,
                        bitacoraController, usuarioActual, idDoc);
            });
            JButton btnReportesPdf = new JButton("Reportes PDF");
            btnReportesPdf.setPreferredSize(new Dimension(180, 36));
            btnReportesPdf.setFont(new Font("SansSerif", Font.BOLD, 12));
            btnReportesPdf.setToolTipText("General, Semanal, Quincenal y Mensual — elige carpeta al generar");
            btnReportesPdf.addActionListener(e -> abrirDialogoReportesPdf());
            panelExportacion.add(btnExportarCsv);
            panelExportacion.add(btnReportesPdf);
        } else {
            JLabel lblSinExport = new JLabel(
                    "<html><i>La exportación de reportes está disponible para administradores y coordinadores.</i></html>");
            panelExportacion.add(lblSinExport);
        }

        JLabel lblAyudaVista = new JLabel(
                "<html><div style='width:560px;'>Use la vista ampliada para consultar el horario semanal con mayor detalle.</div></html>");
        JButton btnVerVistaHorario = new JButton("Ver horario");
        btnVerVistaHorario.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnVerVistaHorario.setPreferredSize(new Dimension(320, 40));
        btnVerVistaHorario.addActionListener(e -> abrirDialogoVistaHorario());
        JPanel filaBotonVerHorario = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filaBotonVerHorario.setOpaque(false);
        filaBotonVerHorario.add(btnVerVistaHorario);
        JPanel panelBloqueVerHorario = new JPanel();
        panelBloqueVerHorario.setOpaque(false);
        panelBloqueVerHorario.setLayout(new BoxLayout(panelBloqueVerHorario, BoxLayout.Y_AXIS));
        lblAyudaVista.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaBotonVerHorario.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelBloqueVerHorario.add(lblAyudaVista);
        panelBloqueVerHorario.add(Box.createVerticalStrut(6));
        panelBloqueVerHorario.add(filaBotonVerHorario);

        JPanel panelFiltro = new JPanel(new GridBagLayout());
        panelFiltro.setOpaque(false);
        panelFiltro.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        GridBagConstraints gf = new GridBagConstraints();
        gf.insets = new Insets(4, 0, 4, 8);
        gf.anchor = GridBagConstraints.WEST;

        JLabel lblPistaBuscar = new JLabel("<html><small><span style='color:#555;'>"
                + PISTA_TEXTO_BUSQUEDA_HORARIO
                + "</span></small></html>");
        gf.gridy = 0;
        gf.gridx = 0;
        gf.gridwidth = GridBagConstraints.REMAINDER;
        gf.weightx = 1.0;
        gf.fill = GridBagConstraints.HORIZONTAL;
        panelFiltro.add(lblPistaBuscar, gf);
        gf.gridwidth = 1;

        gf.gridy = 1;
        gf.gridx = 0;
        gf.weightx = 0;
        gf.fill = GridBagConstraints.NONE;
        panelFiltro.add(new JLabel("Buscar:"), gf);
        txtFiltro = new JTextField();
        txtFiltro.setToolTipText(PISTA_TEXTO_BUSQUEDA_HORARIO);
        gf.gridx = 1;
        gf.weightx = 1.0;
        gf.fill = GridBagConstraints.HORIZONTAL;
        gf.gridwidth = GridBagConstraints.REMAINDER;
        panelFiltro.add(txtFiltro, gf);
        gf.gridwidth = 1;

        gf.gridy = 2;
        gf.weightx = 0;
        gf.fill = GridBagConstraints.NONE;
        gf.gridx = 0;
        panelFiltro.add(new JLabel("Día:"), gf);
        gf.gridx = 1;
        gf.weightx = 1.0;
        gf.fill = GridBagConstraints.HORIZONTAL;
        cboDia = new JComboBox<>(DIAS);
        cboDia.setMinimumSize(new Dimension(72, 32));
        panelFiltro.add(cboDia, gf);

        gf.gridx = 2;
        gf.weightx = 0;
        gf.fill = GridBagConstraints.NONE;
        panelFiltro.add(new JLabel("Docente:"), gf);
        gf.gridx = 3;
        gf.weightx = 1.0;
        gf.fill = GridBagConstraints.HORIZONTAL;
        cboDocenteFiltro = new JComboBox<>();
        llenarComboDocentes(cboDocenteFiltro);
        cboDocenteFiltro.setMinimumSize(new Dimension(100, 32));
        panelFiltro.add(cboDocenteFiltro, gf);

        gf.gridx = 4;
        gf.weightx = 0;
        gf.fill = GridBagConstraints.NONE;
        panelFiltro.add(new JLabel("Asignatura:"), gf);
        gf.gridx = 5;
        gf.weightx = 1.0;
        gf.fill = GridBagConstraints.HORIZONTAL;
        cboAsigFiltro = new JComboBox<>();
        llenarComboAsignaturas(cboAsigFiltro);
        cboAsigFiltro.setMinimumSize(new Dimension(100, 32));
        panelFiltro.add(cboAsigFiltro, gf);

        gf.gridx = 6;
        gf.weightx = 0;
        gf.fill = GridBagConstraints.NONE;
        panelFiltro.add(new JLabel("Aula:"), gf);
        gf.gridx = 7;
        gf.weightx = 1.0;
        gf.fill = GridBagConstraints.HORIZONTAL;
        cboAulaFiltro = new JComboBox<>();
        llenarComboAulas(cboAulaFiltro);
        cboAulaFiltro.setMinimumSize(new Dimension(100, 32));
        panelFiltro.add(cboAulaFiltro, gf);

        JPanel panelNorte = new JPanel();
        panelNorte.setOpaque(false);
        panelNorte.setLayout(new BoxLayout(panelNorte, BoxLayout.Y_AXIS));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelBloqueVerHorario.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelFiltro.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel panelExportacionEnvuelto = new JPanel(new BorderLayout());
        panelExportacionEnvuelto.setOpaque(false);
        panelExportacionEnvuelto.setBorder(BorderFactory.createTitledBorder("Exportación de reportes"));
        panelExportacionEnvuelto.add(panelExportacion, BorderLayout.CENTER);
        panelExportacionEnvuelto.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelExportacionEnvuelto.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        panelNorte.add(header);
        panelNorte.add(panelExportacionEnvuelto);
        panelNorte.add(Box.createVerticalStrut(4));
        panelNorte.add(panelBloqueVerHorario);
        panelNorte.add(Box.createVerticalStrut(4));
        panelNorte.add(panelFiltro);

        tablaLeyenda.setRowHeight(22);
        tablaLeyenda.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tablaLeyenda.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        DefaultTableCellRenderer centrar = new DefaultTableCellRenderer();
        centrar.setHorizontalAlignment(SwingConstants.CENTER);
        tablaLeyenda.getColumnModel().getColumn(0).setCellRenderer(centrar);
        scrollLeyenda = new JScrollPane(tablaLeyenda);
        scrollLeyenda.setBorder(BorderFactory.createTitledBorder("Referencia (código → asignatura, docente y aula)"));
        scrollLeyenda.setPreferredSize(new Dimension(0, 200));

        JLabel lblHorarioTitulo = new JLabel("HORARIO DE CLASES", SwingConstants.CENTER);
        lblHorarioTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblHorarioTitulo.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        panelGrilla = new JPanel(new GridBagLayout());
        panelGrilla.setBackground(Color.WHITE);
        panelGrilla.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scrollGrilla = new JScrollPane(panelGrilla);
        scrollGrilla.setBorder(BorderFactory.createTitledBorder("Cuadrícula semanal (código de asignatura por franja)"));

        panelVistaHorario = new JPanel(new BorderLayout(0, 8));
        panelVistaHorario.setOpaque(true);
        panelVistaHorario.setBackground(new Color(250, 250, 250));
        panelVistaHorario.add(lblHorarioTitulo, BorderLayout.NORTH);
        panelVistaHorario.add(scrollGrilla, BorderLayout.CENTER);

        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));
        panelCentro.setOpaque(false);
        panelCentro.add(scrollLeyenda, BorderLayout.CENTER);

        txtFiltro.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refrescarTablaRespetandoModal();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refrescarTablaRespetandoModal();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refrescarTablaRespetandoModal();
            }
        });
        cboDia.addActionListener(e -> refrescarTablaRespetandoModal());
        cboDocenteFiltro.addActionListener(e -> refrescarTablaRespetandoModal());
        cboAsigFiltro.addActionListener(e -> refrescarTablaRespetandoModal());
        cboAulaFiltro.addActionListener(e -> refrescarTablaRespetandoModal());

        add(panelNorte, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);
    }

    private void abrirDialogoVistaHorario() {
        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        if (owner == null) {
            return;
        }
        refrescarTabla();
        JPanel panelFiltrosModal = crearPanelFiltrosModalHorario();
        copiarFiltrosPanelAModal();
        refrescarDesdeFiltrosModal();

        dialogoHorarioActivo = new JDialog(owner, "Horario de clases", true);
        JDialog dlg = dialogoHorarioActivo;
        JPanel dlgRoot = new JPanel(new BorderLayout(10, 10));
        dlgRoot.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        dlgRoot.add(panelFiltrosModal, BorderLayout.NORTH);
        dlgRoot.add(panelVistaHorario, BorderLayout.CENTER);
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        sur.setOpaque(false);
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dlg.dispose());
        sur.add(btnCerrar);
        dlgRoot.add(sur, BorderLayout.SOUTH);
        dlg.setContentPane(dlgRoot);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                copiarFiltrosModalAPanel();
                dialogoHorarioActivo = null;
                refrescarTabla();
            }
        });
        dlg.setMinimumSize(new Dimension(880, 520));
        dlg.setSize(Math.min(1100, owner.getWidth() - 40), Math.min(720, owner.getHeight() - 40));
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);
    }

    public void refrescar() {
        refrescarTablaRespetandoModal();
    }

    private List<Horario> filtrarPorCombos(List<Horario> todos, String diaSel, FiltroDocente fd, FiltroAsig fa, FiltroAula fu) {
        List<Horario> r = new ArrayList<>();
        for (Horario h : todos) {
            if (diaSel != null && !TODOS.equals(diaSel)) {
                String nd = HorarioController.normalizarDia(h.getDiaSemana());
                if (nd == null || !nd.equals(diaSel)) {
                    continue;
                }
            }
            if (fd != null && fd.id >= 0 && h.getIdDocente() != fd.id) {
                continue;
            }
            if (fa != null && fa.id >= 0 && h.getIdAsignatura() != fa.id) {
                continue;
            }
            if (fu != null && fu.id >= 0 && h.getIdAula() != fu.id) {
                continue;
            }
            r.add(h);
        }
        return r;
    }

    private List<Horario> filtrarPorTexto(List<Horario> lista, String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return new ArrayList<>(lista);
        }
        String t = texto.trim().toLowerCase();
        List<Horario> r = new ArrayList<>();
        for (Horario h : lista) {
            StringBuilder sb = new StringBuilder();
            sb.append(h.getIdHorario()).append(' ');
            sb.append(etiquetaDocente(h.getIdDocente())).append(' ');
            sb.append(etiquetaAsignatura(h.getIdAsignatura())).append(' ');
            sb.append(etiquetaAula(h.getIdAula())).append(' ');
            sb.append(h.getDiaSemana()).append(' ');
            if (h.getHoraInicio() != null) {
                sb.append(h.getHoraInicio()).append(' ');
            }
            if (h.getHoraFin() != null) {
                sb.append(h.getHoraFin()).append(' ');
            }
            if (sb.toString().toLowerCase().contains(t)) {
                r.add(h);
            }
        }
        return r;
    }

    private List<Horario> calcularDatosVista(String textoBusqueda, String diaSel, FiltroDocente fd, FiltroAsig fa, FiltroAula fu) {
        List<Horario> porCombo = filtrarPorCombos(controller.listarActivos(), diaSel, fd, fa, fu);
        String busq = textoBusqueda != null ? textoBusqueda : "";
        return filtrarPorTexto(porCombo, busq);
    }

    private void actualizarVistaConLista(List<Horario> lista) {
        horarioSeleccionGrilla = null;
        limpiarBordeSeleccion();
        datosVista = lista;
        leyendaModel.setFilas(construirLeyenda(datosVista));
        TablaUtil.aplicarAnchosContenidoYRellenarPadre(scrollLeyenda, tablaLeyenda, 10, 40, 400, new int[]{2, 3});
        reconstruirGrilla(datosVista);
        revalidate();
        repaint();
    }

    private void refrescarTabla() {
        String busq = txtFiltro != null && txtFiltro.getText() != null ? txtFiltro.getText() : "";
        String diaSel = (String) cboDia.getSelectedItem();
        FiltroDocente fd = (FiltroDocente) cboDocenteFiltro.getSelectedItem();
        FiltroAsig fa = (FiltroAsig) cboAsigFiltro.getSelectedItem();
        FiltroAula fu = (FiltroAula) cboAulaFiltro.getSelectedItem();
        actualizarVistaConLista(calcularDatosVista(busq, diaSel, fd, fa, fu));
    }

    private void refrescarTablaRespetandoModal() {
        if (dialogoHorarioActivo != null && dialogoHorarioActivo.isShowing()) {
            copiarFiltrosPanelAModal();
            refrescarDesdeFiltrosModal();
        } else {
            refrescarTabla();
        }
    }

    private void refrescarDesdeFiltrosModal() {
        if (txtFiltroModal == null) {
            refrescarTabla();
            return;
        }
        String busq = txtFiltroModal.getText() != null ? txtFiltroModal.getText() : "";
        String diaSel = (String) cboDiaModal.getSelectedItem();
        FiltroDocente fd = (FiltroDocente) cboDocenteModal.getSelectedItem();
        FiltroAsig fa = (FiltroAsig) cboAsigModal.getSelectedItem();
        FiltroAula fu = (FiltroAula) cboAulaModal.getSelectedItem();
        actualizarVistaConLista(calcularDatosVista(busq, diaSel, fd, fa, fu));
    }

    private void refrescarDespuesDeCambioDatos() {
        if (dialogoHorarioActivo != null && dialogoHorarioActivo.isShowing()) {
            refrescarDesdeFiltrosModal();
        } else {
            refrescarTabla();
        }
    }

    private void seleccionarFiltroDocente(JComboBox<FiltroDocente> dest, FiltroDocente ref) {
        if (ref == null) {
            return;
        }
        for (int i = 0; i < dest.getItemCount(); i++) {
            if (dest.getItemAt(i).id == ref.id) {
                dest.setSelectedIndex(i);
                return;
            }
        }
    }

    private void seleccionarFiltroAsig(JComboBox<FiltroAsig> dest, FiltroAsig ref) {
        if (ref == null) {
            return;
        }
        for (int i = 0; i < dest.getItemCount(); i++) {
            if (dest.getItemAt(i).id == ref.id) {
                dest.setSelectedIndex(i);
                return;
            }
        }
    }

    private void seleccionarFiltroAula(JComboBox<FiltroAula> dest, FiltroAula ref) {
        if (ref == null) {
            return;
        }
        for (int i = 0; i < dest.getItemCount(); i++) {
            if (dest.getItemAt(i).id == ref.id) {
                dest.setSelectedIndex(i);
                return;
            }
        }
    }

    private void copiarFiltrosPanelAModal() {
        if (txtFiltroModal == null) {
            return;
        }
        txtFiltroModal.setText(txtFiltro.getText() != null ? txtFiltro.getText() : "");
        cboDiaModal.setSelectedItem(cboDia.getSelectedItem());
        seleccionarFiltroDocente(cboDocenteModal, (FiltroDocente) cboDocenteFiltro.getSelectedItem());
        seleccionarFiltroAsig(cboAsigModal, (FiltroAsig) cboAsigFiltro.getSelectedItem());
        seleccionarFiltroAula(cboAulaModal, (FiltroAula) cboAulaFiltro.getSelectedItem());
    }

    private void copiarFiltrosModalAPanel() {
        if (txtFiltroModal == null) {
            return;
        }
        txtFiltro.setText(txtFiltroModal.getText() != null ? txtFiltroModal.getText() : "");
        cboDia.setSelectedItem(cboDiaModal.getSelectedItem());
        seleccionarFiltroDocente(cboDocenteFiltro, (FiltroDocente) cboDocenteModal.getSelectedItem());
        seleccionarFiltroAsig(cboAsigFiltro, (FiltroAsig) cboAsigModal.getSelectedItem());
        seleccionarFiltroAula(cboAulaFiltro, (FiltroAula) cboAulaModal.getSelectedItem());
    }

    private JPanel crearPanelFiltrosModalHorario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Filtros (mismo criterio que en el módulo; la cuadrícula y la leyenda siguen estos valores)"));
        GridBagConstraints gf = new GridBagConstraints();
        gf.insets = new Insets(4, 0, 4, 8);
        gf.anchor = GridBagConstraints.WEST;

        JLabel lblPistaModal = new JLabel("<html><small><span style='color:#555;'>"
                + PISTA_TEXTO_BUSQUEDA_HORARIO
                + "</span></small></html>");
        gf.gridy = 0;
        gf.gridx = 0;
        gf.gridwidth = GridBagConstraints.REMAINDER;
        gf.weightx = 1.0;
        gf.fill = GridBagConstraints.HORIZONTAL;
        panel.add(lblPistaModal, gf);
        gf.gridwidth = 1;

        gf.gridy = 1;
        gf.gridx = 0;
        gf.weightx = 0;
        gf.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Buscar:"), gf);
        txtFiltroModal = new JTextField();
        txtFiltroModal.setToolTipText(PISTA_TEXTO_BUSQUEDA_HORARIO);
        gf.gridx = 1;
        gf.weightx = 1.0;
        gf.fill = GridBagConstraints.HORIZONTAL;
        gf.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(txtFiltroModal, gf);
        gf.gridwidth = 1;

        gf.gridy = 2;
        gf.weightx = 0;
        gf.fill = GridBagConstraints.NONE;
        gf.gridx = 0;
        panel.add(new JLabel("Día:"), gf);
        gf.gridx = 1;
        gf.weightx = 1.0;
        gf.fill = GridBagConstraints.HORIZONTAL;
        cboDiaModal = new JComboBox<>(DIAS);
        cboDiaModal.setMinimumSize(new Dimension(72, 32));
        panel.add(cboDiaModal, gf);

        gf.gridx = 2;
        gf.weightx = 0;
        gf.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Docente:"), gf);
        gf.gridx = 3;
        gf.weightx = 1.0;
        gf.fill = GridBagConstraints.HORIZONTAL;
        cboDocenteModal = new JComboBox<>();
        llenarComboDocentes(cboDocenteModal);
        cboDocenteModal.setMinimumSize(new Dimension(100, 32));
        panel.add(cboDocenteModal, gf);

        gf.gridx = 4;
        gf.weightx = 0;
        gf.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Asignatura:"), gf);
        gf.gridx = 5;
        gf.weightx = 1.0;
        gf.fill = GridBagConstraints.HORIZONTAL;
        cboAsigModal = new JComboBox<>();
        llenarComboAsignaturas(cboAsigModal);
        cboAsigModal.setMinimumSize(new Dimension(100, 32));
        panel.add(cboAsigModal, gf);

        gf.gridx = 6;
        gf.weightx = 0;
        gf.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Aula:"), gf);
        gf.gridx = 7;
        gf.weightx = 1.0;
        gf.fill = GridBagConstraints.HORIZONTAL;
        cboAulaModal = new JComboBox<>();
        llenarComboAulas(cboAulaModal);
        cboAulaModal.setMinimumSize(new Dimension(100, 32));
        panel.add(cboAulaModal, gf);

        DocumentListener dlModal = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refrescarDesdeFiltrosModal();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refrescarDesdeFiltrosModal();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refrescarDesdeFiltrosModal();
            }
        };
        txtFiltroModal.getDocument().addDocumentListener(dlModal);
        cboDiaModal.addActionListener(e -> refrescarDesdeFiltrosModal());
        cboDocenteModal.addActionListener(e -> refrescarDesdeFiltrosModal());
        cboAsigModal.addActionListener(e -> refrescarDesdeFiltrosModal());
        cboAulaModal.addActionListener(e -> refrescarDesdeFiltrosModal());

        return panel;
    }

    private static JFrame marcoPrincipalDesde(Window w) {
        if (w == null) {
            return null;
        }
        if (w instanceof JFrame) {
            return (JFrame) w;
        }
        if (w instanceof JDialog) {
            Window ow = ((JDialog) w).getOwner();
            return ow instanceof JFrame ? (JFrame) ow : null;
        }
        return null;
    }

    private void limpiarBordeSeleccion() {
        if (celdaSeleccionada != null) {
            Object o = celdaSeleccionada.getClientProperty(PROP_BORDE_BASE_GRILLA);
            Border base = o instanceof Border ? (Border) o : bordeCeldaGrilla();
            celdaSeleccionada.setBorder(base);
            celdaSeleccionada = null;
        }
    }

    private List<FilaLeyenda> construirLeyenda(List<Horario> lista) {
        Map<String, FilaLeyenda> map = new LinkedHashMap<>();
        int n = 0;
        for (Horario h : lista) {
            String clave = h.getIdAsignatura() + "|" + h.getIdDocente() + "|" + h.getIdAula();
            if (map.containsKey(clave)) {
                continue;
            }
            n++;
            Asignatura a = asignaturaController != null ? asignaturaController.buscarPorId(h.getIdAsignatura()) : null;
            Docente d = docenteController != null ? docenteController.buscarPorId(h.getIdDocente()) : null;
            Aula au = aulaController != null ? aulaController.buscarPorId(h.getIdAula()) : null;
            String cod = a != null && a.getCodigo() != null ? a.getCodigo() : "?";
            String nomAs = a != null && a.getNombre() != null ? a.getNombre() : "";
            String doc = d != null
                    ? ((d.getApellidos() != null ? d.getApellidos() : "") + ", " + (d.getNombres() != null ? d.getNombres() : "")).trim()
                    : "";
            if (doc.startsWith(",")) {
                doc = doc.substring(1).trim();
            }
            String aulaTxt = au != null && au.getCodigo() != null ? au.getCodigo() : "";
            map.put(clave, new FilaLeyenda(n, cod, nomAs, doc, aulaTxt));
        }
        List<FilaLeyenda> filas = new ArrayList<>(map.values());
        filas.sort(Comparator.comparing((FilaLeyenda f) -> f.codigo.toLowerCase()).thenComparing(f -> f.docente.toLowerCase()));
        for (int i = 0; i < filas.size(); i++) {
            FilaLeyenda f = filas.get(i);
            filas.set(i, new FilaLeyenda(i + 1, f.codigo, f.asignatura, f.docente, f.aula));
        }
        return filas;
    }

    private static String diaDesdeColumna(int col0a6) {
        if (col0a6 < 0 || col0a6 >= DIAS_LUN_A_DOM.length) {
            return "LUNES";
        }
        return DIAS_LUN_A_DOM[col0a6];
    }

    private static Time tiempoDesdeMinutosDia(int minutos) {
        return Time.valueOf(String.format("%02d:%02d:00", minutos / 60, minutos % 60));
    }

    private boolean puedeEditarDesdeGrilla() {
        return PermisosRol.puedeEditarModulo(usuarioActual, PermisosRol.MOD_HORARIOS);
    }

    private javax.swing.border.Border bordeCeldaGrilla() {
        return BorderFactory.createMatteBorder(0, 0, 1, 1, COLOR_LINEA_GRILLA);
    }

    private javax.swing.border.Border bordeCeldaSeleccionadaGrilla() {
        return BorderFactory.createLineBorder(new Color(30, 144, 255), 2);
    }

    private static Color colorBordePorIdAsignatura(int idAsignatura) {
        if (idAsignatura < 0) {
            return COLOR_LINEA_GRILLA;
        }
        return PALETA_BORDE_ASIGNATURA[Math.floorMod(idAsignatura, PALETA_BORDE_ASIGNATURA.length)];
    }

    private static Color colorFondoSuavePorBorde(Color acento) {
        float[] hsb = Color.RGBtoHSB(acento.getRed(), acento.getGreen(), acento.getBlue(), null);
        return Color.getHSBColor(hsb[0], Math.min(0.14f, hsb[1] * 0.35f + 0.08f), 0.97f);
    }

    private static boolean horarioSolapaFranja(Horario h, int diaIndex, int slotIni, int slotFin) {
        if (h == null) {
            return false;
        }
        String nd = HorarioController.normalizarDia(h.getDiaSemana());
        if (nd == null || indiceDia(nd) != diaIndex) {
            return false;
        }
        if (h.getHoraInicio() == null || h.getHoraFin() == null) {
            return false;
        }
        int a = minutosDia(h.getHoraInicio());
        int b = minutosDia(h.getHoraFin());
        return a < slotFin && b > slotIni;
    }

    private String textoTooltipCeldaHorarioUnico(Horario h) {
        if (h == null) {
            return "";
        }
        String cod = codigoAsignatura(h);
        String hi = h.getHoraInicio() != null ? h.getHoraInicio().toString().substring(0, Math.min(5, h.getHoraInicio().toString().length())) : "?";
        String hf = h.getHoraFin() != null ? h.getHoraFin().toString().substring(0, Math.min(5, h.getHoraFin().toString().length())) : "?";
        String doc = etiquetaDocente(h.getIdDocente());
        String aula = etiquetaAula(h.getIdAula());
        return cod + " · " + hi + " – " + hf + "\nDocente: " + doc + "\nAula: " + aula + "\nID horario: " + h.getIdHorario();
    }

    private void reconstruirGrilla(List<Horario> lista) {
        panelGrilla.removeAll();
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 0, 0, 0);
        gc.fill = GridBagConstraints.BOTH;

        int minM = MINUTOS_DEF_INI;
        int maxM = MINUTOS_DEF_FIN;
        for (Horario h : lista) {
            if (h.getHoraInicio() != null && h.getHoraFin() != null) {
                int a = minutosDia(h.getHoraInicio());
                int b = minutosDia(h.getHoraFin());
                minM = Math.min(minM, redondearAbajo(a, SLOT_MINUTOS));
                maxM = Math.max(maxM, redondearArriba(b, SLOT_MINUTOS));
            }
        }
        if (minM >= maxM) {
            maxM = minM + SLOT_MINUTOS * 4;
        }

        Font fHeader = new Font("SansSerif", Font.BOLD, 11);
        Font fCelda = new Font("SansSerif", Font.PLAIN, 11);

        gc.gridy = 0;
        gc.weightx = 0;
        gc.weighty = 0;
        gc.gridx = 0;
        panelGrilla.add(celdaCabeceraGrilla("ENT / SAL", fHeader), gc);
        for (int d = 0; d < 7; d++) {
            gc.gridx = d + 1;
            gc.weightx = 1;
            panelGrilla.add(celdaCabeceraGrilla(DIAS_CORTOS[d], fHeader), gc);
        }

        int row = 1;
        for (int slotIni = minM; slotIni < maxM; slotIni += SLOT_MINUTOS) {
            int slotFin = slotIni + SLOT_MINUTOS;
            gc.gridy = row;
            gc.gridx = 0;
            gc.weightx = 0;
            gc.weighty = 0;
            JLabel lblH = celdaCabeceraGrilla(formatoRango(slotIni, slotFin), fHeader);
            lblH.setPreferredSize(new Dimension(100, 26));
            panelGrilla.add(lblH, gc);

            for (int d = 0; d < 7; d++) {
                gc.gridx = d + 1;
                gc.weightx = 1;
                List<Horario> enSlot = horariosEnSlot(lista, d, slotIni, slotFin);
                String texto;
                if (enSlot.isEmpty()) {
                    texto = "+";
                } else if (enSlot.size() == 1) {
                    texto = codigoAsignatura(enSlot.get(0));
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (int k = 0; k < enSlot.size(); k++) {
                        if (k > 0) {
                            sb.append(" / ");
                        }
                        sb.append(codigoAsignatura(enSlot.get(k)));
                    }
                    texto = sb.toString();
                }
                JButton celda = nuevaBotonCeldaGrilla(enSlot, texto, d, slotIni, slotFin, minM, maxM, fCelda);
                panelGrilla.add(celda, gc);
            }
            row++;
        }

        gc.gridy = row;
        gc.gridx = 0;
        gc.gridwidth = 8;
        gc.weightx = 1;
        gc.weighty = 1;
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        panelGrilla.add(filler, gc);

        panelGrilla.revalidate();
        panelGrilla.repaint();
    }

    private JLabel celdaCabeceraGrilla(String texto, Font f) {
        JLabel l = new JLabel(texto, SwingConstants.CENTER);
        l.setFont(f);
        l.setOpaque(true);
        l.setBackground(COLOR_CABECERA_GRILLA);
        l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(160, 165, 170)));
        return l;
    }

    private JButton nuevaBotonCeldaGrilla(List<Horario> enSlot, String texto, int diaColumna0a6,
            int slotIni, int slotFin, int minMFranja, int maxMFranja, Font fuente) {
        JButton b = new JButton(texto);
        b.setFocusPainted(false);
        b.setMargin(new Insets(2, 2, 2, 2));
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        javax.swing.border.Border bordeBase;
        if (enSlot.isEmpty()) {
            b.setFont(new Font("SansSerif", Font.BOLD, 16));
            b.setForeground(COLOR_CELDA_VACIA_TEXTO);
            b.setBackground(Color.WHITE);
            b.setToolTipText("Franja libre: pulse para registrar un horario en este bloque.");
            bordeBase = bordeCeldaGrilla();
        } else if (enSlot.size() > 1) {
            b.setFont(fuente);
            b.setForeground(Color.BLACK);
            b.setBackground(Color.WHITE);
            b.setToolTipText("Varios bloques en esta franja. Elija al pulsar.");
            bordeBase = bordeCeldaGrilla();
        } else {
            Horario h = enSlot.get(0);
            Color acento = colorBordePorIdAsignatura(h.getIdAsignatura());
            b.setFont(fuente);
            b.setForeground(Color.BLACK);
            b.setBackground(colorFondoSuavePorBorde(acento));
            b.setToolTipText(textoTooltipCeldaHorarioUnico(h));
            boolean contArr = slotIni > minMFranja
                    && horarioSolapaFranja(h, diaColumna0a6, slotIni - SLOT_MINUTOS, slotIni);
            boolean contAb = slotFin < maxMFranja
                    && horarioSolapaFranja(h, diaColumna0a6, slotFin, slotFin + SLOT_MINUTOS);
            bordeBase = new BordeBloqueCelda(acento, contArr, contAb);
        }

        b.setBorder(bordeBase);
        b.putClientProperty(PROP_BORDE_BASE_GRILLA, bordeBase);
        b.putClientProperty("horariosSlot", new ArrayList<>(enSlot));
        b.putClientProperty("diaColumna", diaColumna0a6);
        b.putClientProperty("slotIni", slotIni);
        b.putClientProperty("slotFin", slotFin);
        b.addActionListener(e -> onAccionCeldaGrilla(b));
        return b;
    }

    /**
     * Diálogo al pulsar celda con un horario.
     *
     * @return 0 consultar, 1 modificar, 2 eliminar, 3 cancelar, -1 cerrado
     */
    private int elegirAccionSobreHorarioEnCelda(Component parentVisual, Horario h) {
        String det = codigoAsignatura(h) + " · ID " + h.getIdHorario();
        String[] op = {"Consultar", "Modificar", "Eliminar", "Cancelar"};
        return JOptionPane.showOptionDialog(parentVisual,
                "¿Qué desea hacer con este horario?\n\n" + det,
                "Acción sobre el horario",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                op,
                op[0]);
    }

    private void abrirFormularioConsultarHorario(JFrame owner, Horario h) {
        HorarioFormDialog dlg = new HorarioFormDialog(owner, "Consultar Horario", h, true,
                docenteController, asignaturaController, aulaController, idUsuarioCreador());
        dlg.setVisible(true);
    }

    private void abrirFormularioModificarHorario(JFrame owner, Horario h) {
        HorarioFormDialog dlg = new HorarioFormDialog(owner, "Modificar Horario", h, false,
                docenteController, asignaturaController, aulaController, idUsuarioCreador());
        dlg.setVisible(true);
        procesarHorarioTrasFormulario(dlg.getResultado(), h);
    }

    @SuppressWarnings("unchecked")
    private void onAccionCeldaGrilla(JButton b) {
        List<Horario> list = (List<Horario>) b.getClientProperty("horariosSlot");
        int diaCol = (Integer) b.getClientProperty("diaColumna");
        int slotIni = (Integer) b.getClientProperty("slotIni");
        int slotFin = (Integer) b.getClientProperty("slotFin");

        limpiarBordeSeleccion();
        celdaSeleccionada = b;
        Object ob = b.getClientProperty(PROP_BORDE_BASE_GRILLA);
        Border base = ob instanceof Border ? (Border) ob : bordeCeldaGrilla();
        b.setBorder(BorderFactory.createCompoundBorder(bordeCeldaSeleccionadaGrilla(), base));
        if (list == null || list.isEmpty()) {
            horarioSeleccionGrilla = null;
        } else {
            horarioSeleccionGrilla = list.get(0);
        }

        Window wOrigen = javax.swing.SwingUtilities.getWindowAncestor(b);
        JFrame owner = marcoPrincipalDesde(wOrigen);
        if (owner == null) {
            owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        }
        if (owner == null) {
            return;
        }

        if (!puedeEditarDesdeGrilla()) {
            if (list == null || list.isEmpty()) {
                return;
            }
            if (list.size() == 1) {
                abrirFormularioConsultarHorario(owner, list.get(0));
                return;
            }
            String[] opcionesRo = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                Horario hx = list.get(i);
                opcionesRo[i] = "ID " + hx.getIdHorario() + " — " + codigoAsignatura(hx);
            }
            int idxRo = JOptionPane.showOptionDialog(b,
                    "Varios horarios en esta franja. Elija cuál desea consultar:",
                    "Seleccionar horario",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opcionesRo,
                    opcionesRo[0]);
            if (idxRo < 0) {
                return;
            }
            abrirFormularioConsultarHorario(owner, list.get(idxRo));
            return;
        }

        if (list == null || list.isEmpty()) {
            int op = Mensajes.confirmar("¿Desea registrar un horario en este bloque?");
            if (op != 0) {
                return;
            }
            String diaNom = diaDesdeColumna(diaCol);
            HorarioFormDialog.PrellenoFranja pre = new HorarioFormDialog.PrellenoFranja(
                    diaNom, tiempoDesdeMinutosDia(slotIni), tiempoDesdeMinutosDia(slotFin));
            HorarioFormDialog dlg = new HorarioFormDialog(owner, "Registrar Horario", null, false,
                    docenteController, asignaturaController, aulaController, idUsuarioCreador(), pre);
            dlg.setVisible(true);
            procesarHorarioTrasFormulario(dlg.getResultado(), null);
            return;
        }

        if (list.size() == 1) {
            Horario h0 = list.get(0);
            int acc = elegirAccionSobreHorarioEnCelda(b, h0);
            if (acc < 0 || acc == 3) {
                return;
            }
            if (acc == 0) {
                abrirFormularioConsultarHorario(owner, h0);
                return;
            }
            if (acc == 1) {
                abrirFormularioModificarHorario(owner, h0);
                return;
            }
            confirmarEliminarHorario(h0);
            return;
        }

        String[] opciones = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Horario hx = list.get(i);
            opciones[i] = "ID " + hx.getIdHorario() + " — " + codigoAsignatura(hx);
        }
        int idx = JOptionPane.showOptionDialog(b,
                "Varios horarios coinciden en esta franja. Elija primero cuál tratará:",
                "Seleccionar horario",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]);
        if (idx < 0) {
            return;
        }
        Horario elegido = list.get(idx);
        int acc = elegirAccionSobreHorarioEnCelda(b, elegido);
        if (acc < 0 || acc == 3) {
            return;
        }
        if (acc == 0) {
            abrirFormularioConsultarHorario(owner, elegido);
            return;
        }
        if (acc == 1) {
            abrirFormularioModificarHorario(owner, elegido);
            return;
        }
        confirmarEliminarHorario(elegido);
    }

    private void procesarHorarioTrasFormulario(Horario r, Horario existenteModificar) {
        if (r == null) {
            return;
        }
        if (existenteModificar == null) {
            Horario creado = controller.registrar(r);
            if (creado == null) {
                String err = controller.getUltimoErrorValidacion();
                Mensajes.error(err != null ? err : "No se pudo registrar el horario.");
                return;
            }
            Mensajes.info("Horario registrado correctamente.\n\nID: " + creado.getIdHorario());
        } else {
            r.setIdHorario(existenteModificar.getIdHorario());
            boolean ok = controller.actualizar(r);
            if (ok) {
                Mensajes.info("Horario actualizado correctamente.");
            } else {
                String err = controller.getUltimoErrorValidacion();
                Mensajes.error(err != null ? err : "No se pudo modificar el horario.");
                return;
            }
        }
        refrescarDespuesDeCambioDatos();
    }

    private String codigoAsignatura(Horario h) {
        if (h == null || asignaturaController == null) {
            return "·";
        }
        Asignatura a = asignaturaController.buscarPorId(h.getIdAsignatura());
        return a != null && a.getCodigo() != null ? a.getCodigo() : "·";
    }

    private List<Horario> horariosEnSlot(List<Horario> lista, int diaIndex, int slotIni, int slotFin) {
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

    private int idUsuarioCreador() {
        return usuarioActual != null ? usuarioActual.getIdUsuario() : 1;
    }

    private void confirmarEliminarHorario(Horario existente) {
        if (existente == null) {
            return;
        }
        int opcion = Mensajes.confirmar("¿Está seguro de eliminar este horario?\n\nID: " + existente.getIdHorario()
                + "\nDía: " + existente.getDiaSemana());
        if (opcion == 0) {
            boolean ok = controller.eliminarLogico(existente.getIdHorario());
            if (ok) {
                Mensajes.info("Horario eliminado.");
                refrescarDespuesDeCambioDatos();
            } else {
                Mensajes.error("No se pudo eliminar el horario.");
            }
        }
    }

    private String etiquetaDocente(int idDocente) {
        if (docenteController == null) {
            return String.valueOf(idDocente);
        }
        Docente d = docenteController.buscarPorId(idDocente);
        if (d == null) {
            return String.valueOf(idDocente);
        }
        return d.getIdDocente() + " — " + d.getCedula() + " — "
                + (d.getNombres() != null ? d.getNombres() : "") + " " + (d.getApellidos() != null ? d.getApellidos() : "");
    }

    private String etiquetaAsignatura(int idAsignatura) {
        if (asignaturaController == null) {
            return String.valueOf(idAsignatura);
        }
        Asignatura a = asignaturaController.buscarPorId(idAsignatura);
        if (a == null) {
            return String.valueOf(idAsignatura);
        }
        return a.getIdAsignatura() + " — " + (a.getCodigo() != null ? a.getCodigo() : "") + " — " + (a.getNombre() != null ? a.getNombre() : "");
    }

    private String etiquetaAula(int idAula) {
        if (aulaController == null) {
            return String.valueOf(idAula);
        }
        Aula au = aulaController.buscarPorId(idAula);
        if (au == null) {
            return String.valueOf(idAula);
        }
        return au.getIdAula() + " — " + (au.getCodigo() != null ? au.getCodigo() : "");
    }

    /**
     * Borde por celda de un solo horario: laterales en color de asignatura; tapa superior/inferior
     * gruesa solo en los extremos del bloque continuo en la grilla.
     */
    private static final class BordeBloqueCelda extends AbstractBorder {

        private static final int GRUESO = 3;
        private static final int FINO = 1;
        private final Color acento;
        private final boolean continuaArriba;
        private final boolean continuaAbajo;

        BordeBloqueCelda(Color acento, boolean continuaArriba, boolean continuaAbajo) {
            this.acento = acento;
            this.continuaArriba = continuaArriba;
            this.continuaAbajo = continuaAbajo;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            int t = continuaArriba ? FINO : GRUESO;
            int b = continuaAbajo ? FINO : GRUESO;
            return new Insets(t, GRUESO, b, GRUESO);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            int t = continuaArriba ? FINO : GRUESO;
            int b = continuaAbajo ? FINO : GRUESO;
            insets.set(t, GRUESO, b, GRUESO);
            return insets;
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int t = continuaArriba ? FINO : GRUESO;
                int b = continuaAbajo ? FINO : GRUESO;
                g2.setColor(continuaArriba ? COLOR_LINEA_GRILLA : acento);
                g2.fillRect(x, y, w, t);
                g2.setColor(continuaAbajo ? COLOR_LINEA_GRILLA : acento);
                g2.fillRect(x, y + h - b, w, b);
                g2.setColor(acento);
                g2.fillRect(x, y, GRUESO, h);
                g2.fillRect(x + w - GRUESO, y, GRUESO, h);
            } finally {
                g2.dispose();
            }
        }
    }

    private class LeyendaTableModel extends AbstractTableModel {

        private final String[] columnas = new String[]{"#", "Código", "Asignatura", "Docente", "Aula"};
        private List<FilaLeyenda> filas = new ArrayList<>();

        void setFilas(List<FilaLeyenda> filas) {
            this.filas = filas != null ? filas : new ArrayList<>();
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return filas.size();
        }

        @Override
        public int getColumnCount() {
            return columnas.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnas[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            FilaLeyenda f = filas.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return f.indice;
                case 1:
                    return f.codigo;
                case 2:
                    return f.asignatura;
                case 3:
                    return f.docente;
                case 4:
                    return f.aula;
                default:
                    return "";
            }
        }
    }
}
