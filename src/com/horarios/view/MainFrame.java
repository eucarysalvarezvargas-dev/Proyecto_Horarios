package com.horarios.view;

import com.horarios.controller.AuthController;
import com.horarios.controller.AsignaturaController;
import com.horarios.controller.AulaController;
import com.horarios.controller.BitacoraController;
import com.horarios.controller.DocenteController;
import com.horarios.controller.HorarioController;
import com.horarios.controller.UsuarioController;
import com.horarios.model.Usuario;
import com.horarios.util.Mensajes;
import com.horarios.util.PasswordUtil;
import com.horarios.util.PermisosRol;
import com.horarios.util.PersistenciaConfig;
import com.horarios.util.ReporteService;
import com.horarios.util.RecursosUi;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MainFrame extends JFrame {

    private final JPanel panelMenu;
    private final JPanel panelContenido;
    private final CardLayout cardLayout;
    private InicioPanel inicioPanel;
    private final DocentePanel docentePanel;
    private final AsignaturaPanel asignaturaPanel;
    private final AulaPanel aulaPanel;
    private final DocenteController docenteController;
    private final AsignaturaController asignaturaController;
    private final AulaController aulaController;
    private final UsuarioController usuarioController;
    private final HorarioController horarioController;
    private Usuario usuarioActual;
    private final UsuarioPanel usuarioPanel;
    private final HorarioPanel horarioPanel;
    private final BitacoraController bitacoraController;
    private final BitacoraPanel bitacoraPanel;
    private final PerfilPanel perfilPanel;
    private JLabel lblUsuarioEncabezado;

    private static final String CARD_INICIO = "INICIO";
    private static final String CARD_DOCENTES = "DOCENTES";
    private static final String CARD_ASIGNATURAS = "ASIGNATURAS";
    private static final String CARD_AULAS = "AULAS";
    private static final String CARD_HORARIOS = "HORARIOS";
    private static final String CARD_USUARIOS = "USUARIOS";
    private static final String CARD_BITACORA = "BITACORA";
    private static final String CARD_PERFIL = "PERFIL";

    public MainFrame(Usuario usuarioActual, UsuarioController usuarioController, BitacoraController bitacoraController) {
        this.usuarioActual = usuarioActual;
        this.bitacoraController = bitacoraController != null ? bitacoraController : new BitacoraController();
        setTitle("Sistema de Gestión y Control de Horarios Docentes - UNEFA");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int opcion = Mensajes.confirmar("¿Desea salir del sistema?");
                if (opcion == 0) {
                    dispose();
                }
            }
        });

        panelMenu = new JPanel(new BorderLayout());
        panelMenu.setPreferredSize(new Dimension(260, 0));
        panelMenu.setBackground(new Color(33, 47, 61));
        panelMenu.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel panelEncabezado = new JPanel();
        panelEncabezado.setLayout(new BoxLayout(panelEncabezado, BoxLayout.Y_AXIS));
        panelEncabezado.setOpaque(false);

        JLabel lblLogo = RecursosUi.crearEtiquetaLogoPanel();
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelEncabezado.add(lblLogo);

        if (usuarioActual != null) {
            panelEncabezado.add(Box.createVerticalStrut(10));
            lblUsuarioEncabezado = new JLabel(textoEncabezadoUsuario(), SwingConstants.CENTER);
            lblUsuarioEncabezado.setForeground(Color.WHITE);
            lblUsuarioEncabezado.setFont(new Font("SansSerif", Font.PLAIN, 11));
            lblUsuarioEncabezado.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelEncabezado.add(lblUsuarioEncabezado);
        }

        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new BoxLayout(panelBotones, BoxLayout.Y_AXIS));
        panelBotones.setOpaque(false);

        JButton btnInicio = crearBotonMenu("Inicio");
        JButton btnDocentes = crearBotonMenu("Docentes");
        JButton btnAsignaturas = crearBotonMenu("Asignaturas");
        JButton btnAulas = crearBotonMenu("Aulas");
        JButton btnHorarios = crearBotonMenu("Horarios");
        JButton btnUsuarios = crearBotonMenu("Usuarios");

        btnInicio.addActionListener(e -> {
            if (inicioPanel != null) {
                inicioPanel.refrescar();
            }
            mostrarCard(CARD_INICIO);
        });
        btnDocentes.addActionListener(e -> mostrarCard(CARD_DOCENTES));
        btnAsignaturas.addActionListener(e -> mostrarCard(CARD_ASIGNATURAS));
        btnAulas.addActionListener(e -> mostrarCard(CARD_AULAS));
        btnHorarios.addActionListener(e -> mostrarCard(CARD_HORARIOS));
        btnUsuarios.addActionListener(e -> mostrarCard(CARD_USUARIOS));

        agregarBotonMenuSiVisible(panelBotones, btnInicio, CARD_INICIO);
        agregarBotonMenuSiVisible(panelBotones, btnDocentes, CARD_DOCENTES);
        agregarBotonMenuSiVisible(panelBotones, btnAsignaturas, CARD_ASIGNATURAS);
        agregarBotonMenuSiVisible(panelBotones, btnAulas, CARD_AULAS);
        agregarBotonMenuSiVisible(panelBotones, btnHorarios, CARD_HORARIOS);
        agregarBotonMenuSiVisible(panelBotones, btnUsuarios, CARD_USUARIOS);
        JButton btnPerfil = crearBotonMenu("Mi perfil");
        btnPerfil.addActionListener(e -> mostrarCard(CARD_PERFIL));
        agregarBotonMenuSiVisible(panelBotones, btnPerfil, CARD_PERFIL);
        if (usuarioActual != null && PermisosRol.puedeExportarReportes(usuarioActual)) {
            JButton btnReportes = crearBotonMenu("Reportes PDF");
            btnReportes.setToolTipText("Exportar horarios y resumen a PDF o CSV");
            btnReportes.addActionListener(e -> abrirDialogoReportes());
            agregarBotonAlMenu(panelBotones, btnReportes);
        }
        if (usuarioActual != null && PermisosRol.puedeVerBitacora(usuarioActual)) {
            JButton btnBitacora = crearBotonMenu("Bitácora");
            btnBitacora.addActionListener(e -> mostrarCard(CARD_BITACORA));
            agregarBotonAlMenu(panelBotones, btnBitacora);
        }

        panelBotones.add(Box.createVerticalStrut(6));
        JButton btnCerrarSesion = crearBotonMenu("Cerrar sesión");
        btnCerrarSesion.addActionListener(e -> cerrarSesion());
        agregarBotonAlMenu(panelBotones, btnCerrarSesion);

        JScrollPane scrollMenu = new JScrollPane(panelBotones);
        scrollMenu.setBorder(null);
        scrollMenu.setOpaque(false);
        scrollMenu.getViewport().setOpaque(false);
        scrollMenu.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollMenu.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        panelMenu.add(panelEncabezado, BorderLayout.NORTH);
        panelMenu.add(scrollMenu, BorderLayout.CENTER);

        cardLayout = new CardLayout();
        panelContenido = new JPanel(cardLayout);
        panelContenido.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        this.usuarioController = usuarioController != null ? usuarioController : new UsuarioController();

        docenteController = new DocenteController(this.usuarioController);
        this.usuarioController.setDocenteController(docenteController);
        asignaturaController = new AsignaturaController();
        aulaController = new AulaController();
        horarioController = new HorarioController(docenteController, asignaturaController, aulaController);
        docenteController.setHorarioController(horarioController);
        docenteController.reconciliarVinculos();
        configurarAuditoriaControladores();

        panelContenido.add(this.docentePanel = new DocentePanel(docenteController, usuarioActual), CARD_DOCENTES);
        this.docentePanel.setOnDatosCambiados(this::refrescarTrasCambioDocentes);
        panelContenido.add(this.asignaturaPanel = new AsignaturaPanel(asignaturaController, usuarioActual), CARD_ASIGNATURAS);
        panelContenido.add(this.aulaPanel = new AulaPanel(aulaController, usuarioActual), CARD_AULAS);
        this.usuarioPanel = new UsuarioPanel(this.usuarioController, docenteController, usuarioActual);
        panelContenido.add(this.usuarioPanel, CARD_USUARIOS);
        this.horarioPanel = new HorarioPanel(horarioController, docenteController, asignaturaController, aulaController,
                usuarioActual, bitacoraController, this.usuarioController);
        panelContenido.add(this.horarioPanel, CARD_HORARIOS);
        this.bitacoraPanel = new BitacoraPanel(this.bitacoraController);
        panelContenido.add(this.bitacoraPanel, CARD_BITACORA);
        this.perfilPanel = new PerfilPanel(this.usuarioController, docenteController, usuarioActual, this::onPerfilActualizado);
        panelContenido.add(this.perfilPanel, CARD_PERFIL);

        inicioPanel = new InicioPanel(
                docenteController,
                asignaturaController,
                aulaController,
                this.usuarioController,
                horarioController,
                usuarioActual
        );
        inicioPanel.setOnIrDocentes(() -> mostrarCard(CARD_DOCENTES));
        inicioPanel.setOnIrAsignaturas(() -> mostrarCard(CARD_ASIGNATURAS));
        inicioPanel.setOnIrAulas(() -> mostrarCard(CARD_AULAS));
        inicioPanel.setOnIrHorarios(() -> mostrarCard(CARD_HORARIOS));
        inicioPanel.setOnIrUsuarios(() -> mostrarCard(CARD_USUARIOS));
        inicioPanel.setOnExportarReporteGeneral(() -> ReporteService.exportarResumenGeneralCsv(
                docenteController, asignaturaController, aulaController, horarioController,
                this.usuarioController, bitacoraController, usuarioActual));
        inicioPanel.setOnAbrirReportesPdf(this::abrirDialogoReportes);

        panelContenido.add(inicioPanel, CARD_INICIO);

        add(panelMenu, BorderLayout.WEST);
        add(panelContenido, BorderLayout.CENTER);

        mostrarCard(CARD_INICIO);
    }

    private JButton crearBotonMenu(String texto) {
        JButton btn = new JButton(texto);
        Dimension tam = new Dimension(220, 38);
        btn.setPreferredSize(tam);
        btn.setMinimumSize(tam);
        btn.setMaximumSize(tam);
        btn.setFocusPainted(false);
        return btn;
    }

    private void agregarBotonAlMenu(JPanel panel, JButton btn) {
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(btn);
        panel.add(Box.createVerticalStrut(8));
    }

    private void agregarBotonMenuSiVisible(JPanel panel, JButton btn, String card) {
        if (usuarioActual != null && PermisosRol.puedeVerModulo(usuarioActual, card)) {
            agregarBotonAlMenu(panel, btn);
        }
    }

    private void configurarAuditoriaControladores() {
        usuarioController.setContextoAuditoria(bitacoraController, usuarioActual);
        docenteController.setContextoAuditoria(bitacoraController, usuarioActual);
        asignaturaController.setContextoAuditoria(bitacoraController, usuarioActual);
        aulaController.setContextoAuditoria(bitacoraController, usuarioActual);
        horarioController.setContextoAuditoria(bitacoraController, usuarioActual);
    }

    private JPanel crearPanelModulo(String titulo, String subtitulo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        JLabel lblSub = new JLabel(subtitulo);
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        header.add(lbl, BorderLayout.NORTH);
        header.add(lblSub, BorderLayout.CENTER);

        panel.add(header, BorderLayout.NORTH);
        return panel;
    }

    private void abrirDialogoReportes() {
        horarioPanel.abrirDialogoReportesPdf();
    }

    private void mostrarCard(String nombreCard) {
        if (CARD_INICIO.equals(nombreCard) && inicioPanel != null) {
            inicioPanel.refrescar();
        }
        if (CARD_DOCENTES.equals(nombreCard)) {
            docentePanel.refrescar();
        }
        if (CARD_ASIGNATURAS.equals(nombreCard)) {
            asignaturaPanel.refrescar();
        }
        if (CARD_AULAS.equals(nombreCard)) {
            aulaPanel.refrescar();
        }
        if (CARD_USUARIOS.equals(nombreCard)) {
            usuarioPanel.refrescar();
        }
        if (CARD_HORARIOS.equals(nombreCard)) {
            horarioPanel.refrescar();
        }
        if (CARD_BITACORA.equals(nombreCard)) {
            bitacoraPanel.refrescarTabla();
        }
        if (CARD_PERFIL.equals(nombreCard)) {
            perfilPanel.refrescar();
        }
        cardLayout.show(panelContenido, nombreCard);
    }

    private void refrescarTrasCambioDocentes() {
        usuarioPanel.refrescar();
        if (inicioPanel != null) {
            inicioPanel.refrescar();
        }
        horarioPanel.refrescar();
    }

    private String textoEncabezadoUsuario() {
        if (usuarioActual == null) {
            return "";
        }
        return usuarioActual.getEtiquetaSesion();
    }

    private void onPerfilActualizado(Usuario actualizado) {
        this.usuarioActual = actualizado;
        if (lblUsuarioEncabezado != null) {
            lblUsuarioEncabezado.setText(textoEncabezadoUsuario());
        }
        configurarAuditoriaControladores();
        perfilPanel.setUsuarioSesion(actualizado);
        if (inicioPanel != null) {
            inicioPanel.setUsuarioSesion(actualizado);
        }
    }

    private void cerrarSesion() {
        int opcion = Mensajes.confirmar("¿Desea cerrar sesión y volver al inicio de sesión?");
        if (opcion != 0) {
            return;
        }
        bitacoraController.registrarLogout(usuarioActual);
        dispose();
        SwingUtilities.invokeLater(() -> iniciarSesionYMostrarPrincipal(usuarioController, bitacoraController));
    }

    // Muestra el login y, si es correcto, abre la ventana principal.
    public static void iniciarSesionYMostrarPrincipal(UsuarioController usuarioController, BitacoraController bitacoraController) {
        AuthController auth = new AuthController(usuarioController);
        BitacoraController bitacora = bitacoraController != null ? bitacoraController : new BitacoraController();
        Usuario usuarioLogueado = null;

        do {
            String[] cred = Mensajes.pedirCredencialesCancelable();
            if (cred == null) {
                int salir = Mensajes.confirmar("¿Desea cancelar el inicio de sesión y salir del sistema?");
                if (salir == 0) {
                    return;
                }
                continue;
            }

            String username = cred[0] != null ? cred[0].trim() : "";
            String password = cred[1] != null ? cred[1].trim() : "";

            if (username.isEmpty() || password.isEmpty()) {
                Mensajes.error("Debe ingresar usuario y contraseña.");
                continue;
            }

            usuarioLogueado = auth.autenticar(username, password);
            if (usuarioLogueado == null) {
                String errValidacion = auth.getUltimoError();
                if (errValidacion != null) {
                    Mensajes.error(errValidacion);
                    bitacora.registrarLoginFallido(username, "Validación de login rechazada.", null);
                    continue;
                }
                Usuario u = usuarioController.buscarPorUsername(username);
                if (u != null && u.getEstado() != 1
                        && u.getPassword() != null && PasswordUtil.verificar(password, u.getPassword())) {
                    Mensajes.error("Su cuenta está inactiva.\n\nNo puede iniciar sesión. Contacte al administrador del sistema.");
                    bitacora.registrarLoginFallido(username, "Intento de acceso con cuenta inactiva.", u);
                } else {
                    Mensajes.error("Usuario o contraseña incorrectos.\n\nVerifique sus datos e intente nuevamente.");
                    bitacora.registrarLoginFallido(username, "Credenciales incorrectas o usuario inexistente.", u);
                }
            }
        } while (usuarioLogueado == null);

        bitacora.registrarLoginExitoso(usuarioLogueado);
        new MainFrame(usuarioLogueado, usuarioController, bitacora).setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
        }

        SwingUtilities.invokeLater(() -> {
            if (!PersistenciaConfig.isJdbcDisponible()) {
                Mensajes.error("No se pudo conectar con la base de datos.\n\n"
                        + "Verifique que el servicio MySQL esté activo y que la base de datos "
                        + "esté instalada correctamente. Si el problema persiste, contacte al administrador del sistema.");
                return;
            }
            UsuarioController usuarioController = new UsuarioController();
            BitacoraController bitacoraController = new BitacoraController();
            iniciarSesionYMostrarPrincipal(usuarioController, bitacoraController);
        });
    }
}

