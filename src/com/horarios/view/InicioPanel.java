package com.horarios.view;

import com.horarios.controller.AsignaturaController;
import com.horarios.controller.AulaController;
import com.horarios.controller.DocenteController;
import com.horarios.controller.HorarioController;
import com.horarios.controller.UsuarioController;
import com.horarios.model.Usuario;
import com.horarios.util.PermisosRol;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.Timer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.GridLayout;
import java.awt.FlowLayout;

public class InicioPanel extends JPanel {

    private static final DateTimeFormatter FMT_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final DocenteController docenteController;
    private final AsignaturaController asignaturaController;
    private final AulaController aulaController;
    private final UsuarioController usuarioController;
    private final HorarioController horarioController;
    private final Usuario usuarioActual;

    private final JLabel lblFechaHora;
    private final JLabel lblDocentesValor;
    private final JLabel lblAsignaturasValor;
    private final JLabel lblAulasValor;
    private final JLabel lblHorariosValor;
    private final JLabel lblUsuariosValor;

    private Runnable onIrDocentes;
    private Runnable onIrAsignaturas;
    private Runnable onIrAulas;
    private Runnable onIrHorarios;
    private Runnable onIrUsuarios;
    private Runnable onExportarReporteGeneral;
    private Runnable onAbrirReportesPdf;

    /** Actualiza la hora en vivo mientras el panel existe (cada segundo, EDT). */
    private Timer timerReloj;

    public InicioPanel(
            DocenteController docenteController,
            AsignaturaController asignaturaController,
            AulaController aulaController,
            UsuarioController usuarioController,
            HorarioController horarioController,
            Usuario usuarioActual
    ) {
        this.docenteController = docenteController;
        this.asignaturaController = asignaturaController;
        this.aulaController = aulaController;
        this.usuarioController = usuarioController;
        this.horarioController = horarioController;
        this.usuarioActual = usuarioActual;

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel header = new JPanel(new BorderLayout(10, 10));
        header.setOpaque(false);

        JLabel lblTitulo = new JLabel("Dashboard Principal", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 26));

        String usuarioTxt = "Usuario: (no definido)";
        if (usuarioActual != null) {
            usuarioTxt = "Usuario: " + usuarioActual.getUsername() + " (" + usuarioActual.getRol() + ")";
        }
        JLabel lblUsuario = new JLabel(usuarioTxt, SwingConstants.LEFT);
        lblUsuario.setFont(new Font("SansSerif", Font.PLAIN, 13));

        lblFechaHora = new JLabel("", SwingConstants.RIGHT);
        lblFechaHora.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel headerTop = new JPanel(new BorderLayout());
        headerTop.setOpaque(false);
        headerTop.add(lblTitulo, BorderLayout.WEST);
        headerTop.add(lblFechaHora, BorderLayout.EAST);

        header.add(headerTop, BorderLayout.NORTH);
        header.add(lblUsuario, BorderLayout.SOUTH);

        JPanel panelMetricas = new JPanel(new GridLayout(2, 3, 12, 12));
        panelMetricas.setOpaque(false);

        lblDocentesValor = new JLabel("0", SwingConstants.CENTER);
        lblAsignaturasValor = new JLabel("0", SwingConstants.CENTER);
        lblAulasValor = new JLabel("0", SwingConstants.CENTER);
        lblHorariosValor = new JLabel("0", SwingConstants.CENTER);
        lblUsuariosValor = new JLabel("0", SwingConstants.CENTER);

        panelMetricas.add(crearTarjeta("Docentes activos", lblDocentesValor, new Color(52, 152, 219)));
        panelMetricas.add(crearTarjeta("Asignaturas activas", lblAsignaturasValor, new Color(46, 204, 113)));
        panelMetricas.add(crearTarjeta("Aulas activas", lblAulasValor, new Color(155, 89, 182)));
        panelMetricas.add(crearTarjeta("Horarios activos", lblHorariosValor, new Color(241, 196, 15)));
        panelMetricas.add(crearTarjeta("Usuarios activos", lblUsuariosValor, new Color(231, 76, 60)));
        panelMetricas.add(crearPanelAccesosRapidos());

        add(header, BorderLayout.NORTH);
        add(panelMetricas, BorderLayout.CENTER);

        refrescar();
        iniciarRelojEnVivo();
    }

    private void iniciarRelojEnVivo() {
        if (timerReloj != null) {
            timerReloj.stop();
        }
        timerReloj = new Timer(1000, e -> lblFechaHora.setText("Fecha/Hora: " + LocalDateTime.now().format(FMT_FECHA_HORA)));
        timerReloj.setInitialDelay(0);
        timerReloj.start();
    }

    @Override
    public void removeNotify() {
        if (timerReloj != null) {
            timerReloj.stop();
            timerReloj = null;
        }
        super.removeNotify();
    }

    private JPanel crearTarjeta(String titulo, JLabel lblValor, Color colorBarra) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        // Borde de color fino: 3px a la izquierda + contorno gris + relleno
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, colorBarra),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220)),
                        BorderFactory.createEmptyBorder(12, 12, 12, 12)
                )
        ));
        card.setPreferredSize(new Dimension(220, 110));

        JLabel lblTitulo = new JLabel(titulo, SwingConstants.LEFT);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 13));

        lblValor.setFont(new Font("SansSerif", Font.BOLD, 34));
        lblValor.setForeground(new Color(44, 62, 80));

        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.add(lblTitulo, BorderLayout.NORTH);
        centro.add(lblValor, BorderLayout.CENTER);

        card.add(centro, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearPanelAccesosRapidos() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblTitulo = new JLabel("Accesos rápidos", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        botones.setOpaque(false);

        JButton b1 = new JButton("Docentes");
        JButton b2 = new JButton("Asignaturas");
        JButton b3 = new JButton("Aulas");
        JButton b4 = new JButton("Horarios");
        JButton b5 = new JButton("Usuarios");

        b1.addActionListener(e -> { if (onIrDocentes != null) { onIrDocentes.run(); } });
        b2.addActionListener(e -> { if (onIrAsignaturas != null) { onIrAsignaturas.run(); } });
        b3.addActionListener(e -> { if (onIrAulas != null) { onIrAulas.run(); } });
        b4.addActionListener(e -> { if (onIrHorarios != null) { onIrHorarios.run(); } });
        b5.addActionListener(e -> { if (onIrUsuarios != null) { onIrUsuarios.run(); } });

        botones.add(b1);
        botones.add(b2);
        botones.add(b3);
        botones.add(b4);
        botones.add(b5);

        if (usuarioActual != null && PermisosRol.puedeVerModulo(usuarioActual, PermisosRol.MOD_USUARIOS)) {
            b5.setVisible(true);
        } else {
            b5.setVisible(false);
        }
        if (usuarioActual != null && !PermisosRol.puedeVerModulo(usuarioActual, PermisosRol.MOD_HORARIOS)) {
            b4.setVisible(false);
        }
        if (usuarioActual != null && !PermisosRol.puedeVerModulo(usuarioActual, PermisosRol.MOD_DOCENTES)) {
            b1.setVisible(false);
        }
        if (usuarioActual != null && !PermisosRol.puedeVerModulo(usuarioActual, PermisosRol.MOD_ASIGNATURAS)) {
            b2.setVisible(false);
        }
        if (usuarioActual != null && !PermisosRol.puedeVerModulo(usuarioActual, PermisosRol.MOD_AULAS)) {
            b3.setVisible(false);
        }
        if (usuarioActual != null && PermisosRol.puedeExportarReportes(usuarioActual)) {
            if (onAbrirReportesPdf != null) {
                JButton btnPdf = new JButton("Reportes PDF");
                btnPdf.setFont(new Font("SansSerif", Font.BOLD, 12));
                btnPdf.addActionListener(e -> onAbrirReportesPdf.run());
                botones.add(btnPdf);
            }
            if (onExportarReporteGeneral != null) {
                JButton btnReporte = new JButton("Reporte general CSV");
                btnReporte.addActionListener(e -> onExportarReporteGeneral.run());
                botones.add(btnReporte);
            }
        }

        card.add(lblTitulo, BorderLayout.NORTH);
        card.add(botones, BorderLayout.CENTER);
        return card;
    }

    public void setOnIrDocentes(Runnable onIrDocentes) {
        this.onIrDocentes = onIrDocentes;
    }

    public void setOnIrAsignaturas(Runnable onIrAsignaturas) {
        this.onIrAsignaturas = onIrAsignaturas;
    }

    public void setOnIrAulas(Runnable onIrAulas) {
        this.onIrAulas = onIrAulas;
    }

    public void setOnIrHorarios(Runnable onIrHorarios) {
        this.onIrHorarios = onIrHorarios;
    }

    public void setOnIrUsuarios(Runnable onIrUsuarios) {
        this.onIrUsuarios = onIrUsuarios;
    }

    public void setOnExportarReporteGeneral(Runnable onExportarReporteGeneral) {
        this.onExportarReporteGeneral = onExportarReporteGeneral;
    }

    public void setOnAbrirReportesPdf(Runnable onAbrirReportesPdf) {
        this.onAbrirReportesPdf = onAbrirReportesPdf;
    }

    public final void refrescar() {
        lblFechaHora.setText("Fecha/Hora: " + LocalDateTime.now().format(FMT_FECHA_HORA));

        int docentes = docenteController != null ? docenteController.listarActivos().size() : 0;
        int asignaturas = asignaturaController != null ? asignaturaController.listarActivas().size() : 0;
        int aulas = aulaController != null ? aulaController.listarActivas().size() : 0;
        int usuarios = usuarioController != null ? usuarioController.listarActivos().size() : 0;
        int horarios = horarioController != null ? horarioController.listarActivos().size() : 0;

        lblDocentesValor.setText(String.valueOf(docentes));
        lblAsignaturasValor.setText(String.valueOf(asignaturas));
        lblAulasValor.setText(String.valueOf(aulas));
        lblUsuariosValor.setText(String.valueOf(usuarios));
        lblHorariosValor.setText(String.valueOf(horarios));
    }
}

