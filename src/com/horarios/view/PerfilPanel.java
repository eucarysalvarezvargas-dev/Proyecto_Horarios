package com.horarios.view;

import com.horarios.controller.DocenteController;
import com.horarios.controller.UsuarioController;
import com.horarios.model.Docente;
import com.horarios.model.Usuario;
import com.horarios.util.Mensajes;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Perfil del usuario en sesión: datos personales y cambio de clave/username.
 */
public class PerfilPanel extends JPanel {

    private final UsuarioController usuarioController;
    private final DocenteController docenteController;
    private Usuario usuarioSesion;
    private final Consumer<Usuario> onPerfilActualizado;

    private final JLabel lblRol;
    private final JLabel lblCedula;
    private final JTextField txtUsername;
    private final JTextField txtNombres;
    private final JTextField txtApellidos;
    private final JTextField txtCorreo;
    private final JTextField txtTelefono;
    private final JPasswordField txtClaveActual;
    private final JPasswordField txtClaveNueva;
    private final JPasswordField txtClaveConfirm;
    private final JPanel panelDocente;

    public PerfilPanel(UsuarioController usuarioController, DocenteController docenteController,
            Usuario usuarioSesion, Consumer<Usuario> onPerfilActualizado) {
        this.usuarioController = usuarioController;
        this.docenteController = docenteController;
        this.usuarioSesion = usuarioSesion;
        this.onPerfilActualizado = onPerfilActualizado;

        lblRol = new JLabel();
        lblCedula = new JLabel();
        txtUsername = new JTextField(22);
        txtNombres = new JTextField(22);
        txtApellidos = new JTextField(22);
        txtCorreo = new JTextField(22);
        txtTelefono = new JTextField(22);
        txtClaveActual = new JPasswordField(22);
        txtClaveNueva = new JPasswordField(22);
        txtClaveConfirm = new JPasswordField(22);
        panelDocente = new JPanel(new GridBagLayout());

        construirUI();
        cargarDesdeSesion();
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitulo = new JLabel("Mi perfil", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        JLabel lblSub = new JLabel(
                "Actualice sus datos personales y, si lo desea, cambie su contraseña.",
                SwingConstants.LEFT);
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(lblTitulo, BorderLayout.NORTH);
        header.add(lblSub, BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Datos de la cuenta"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        g.gridx = 0;
        g.gridy = row;
        form.add(new JLabel("Rol:"), g);
        g.gridx = 1;
        lblRol.setFont(new Font("SansSerif", Font.BOLD, 12));
        form.add(lblRol, g);

        row++;
        g.gridx = 0;
        g.gridy = row;
        form.add(new JLabel("Cédula:"), g);
        g.gridx = 1;
        form.add(lblCedula, g);

        row++;
        g.gridx = 0;
        g.gridy = row;
        form.add(new JLabel("Username:"), g);
        g.gridx = 1;
        form.add(txtUsername, g);

        row++;
        g.gridx = 0;
        g.gridy = row;
        form.add(new JLabel("Nombres:"), g);
        g.gridx = 1;
        form.add(txtNombres, g);

        row++;
        g.gridx = 0;
        g.gridy = row;
        form.add(new JLabel("Apellidos:"), g);
        g.gridx = 1;
        form.add(txtApellidos, g);

        panelDocente.setBorder(BorderFactory.createTitledBorder("Datos de docente"));
        GridBagConstraints gd = new GridBagConstraints();
        gd.insets = new Insets(6, 8, 6, 8);
        gd.anchor = GridBagConstraints.WEST;
        gd.fill = GridBagConstraints.HORIZONTAL;
        gd.gridx = 0;
        gd.gridy = 0;
        panelDocente.add(new JLabel("Correo (.com):"), gd);
        gd.gridx = 1;
        panelDocente.add(txtCorreo, gd);
        gd.gridx = 0;
        gd.gridy = 1;
        panelDocente.add(new JLabel("Teléfono (11 dígitos):"), gd);
        gd.gridx = 1;
        panelDocente.add(txtTelefono, gd);

        JPanel clavePanel = new JPanel(new GridBagLayout());
        clavePanel.setBorder(BorderFactory.createTitledBorder("Cambiar clave (opcional)"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 6, 8);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.gridy = 0;
        clavePanel.add(new JLabel("Clave actual:"), gc);
        gc.gridx = 1;
        clavePanel.add(txtClaveActual, gc);
        gc.gridx = 0;
        gc.gridy = 1;
        clavePanel.add(new JLabel("Clave nueva:"), gc);
        gc.gridx = 1;
        clavePanel.add(txtClaveNueva, gc);
        gc.gridx = 0;
        gc.gridy = 2;
        clavePanel.add(new JLabel("Confirmar clave nueva:"), gc);
        gc.gridx = 1;
        clavePanel.add(txtClaveConfirm, gc);

        JPanel centro = new JPanel();
        centro.setLayout(new javax.swing.BoxLayout(centro, javax.swing.BoxLayout.Y_AXIS));
        centro.add(form);
        centro.add(javax.swing.Box.createVerticalStrut(10));
        centro.add(panelDocente);
        centro.add(javax.swing.Box.createVerticalStrut(10));
        centro.add(clavePanel);

        JButton btnGuardar = new JButton("Guardar cambios");
        btnGuardar.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnGuardar.addActionListener(e -> guardar());

        JPanel sur = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        sur.setOpaque(false);
        sur.add(btnGuardar);

        add(header, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);
    }

    public void setUsuarioSesion(Usuario usuario) {
        this.usuarioSesion = usuario;
        cargarDesdeSesion();
    }

    public void refrescar() {
        if (usuarioSesion != null && usuarioController != null) {
            Usuario fresco = usuarioController.buscarPorId(usuarioSesion.getIdUsuario());
            if (fresco != null) {
                usuarioSesion = fresco;
            }
        }
        cargarDesdeSesion();
    }

    private void cargarDesdeSesion() {
        if (usuarioSesion == null) {
            return;
        }
        boolean esDocente = "DOCENTE".equalsIgnoreCase(usuarioSesion.getRol());
        lblRol.setText(usuarioSesion.getRol() != null ? usuarioSesion.getRol() : "—");
        String ced = usuarioSesion.getCedula();
        if (ced == null || ced.isEmpty()) {
            ced = esDocente ? usuarioSesion.getUsername() : "—";
        }
        lblCedula.setText(ced);
        txtUsername.setText(usuarioSesion.getUsername() != null ? usuarioSesion.getUsername() : "");
        txtNombres.setText(usuarioSesion.getNombres() != null ? usuarioSesion.getNombres() : "");
        txtApellidos.setText(usuarioSesion.getApellidos() != null ? usuarioSesion.getApellidos() : "");
        txtUsername.setEditable(!esDocente);
        if (esDocente) {
            txtUsername.setToolTipText("Para docentes el username es la cédula y no se puede cambiar desde aquí.");
        } else {
            txtUsername.setToolTipText(null);
        }
        panelDocente.setVisible(esDocente);
        txtCorreo.setText("");
        txtTelefono.setText("");
        if (esDocente && docenteController != null) {
            Docente d = docenteController.buscarPorCedula(ced);
            if (d != null) {
                txtCorreo.setText(d.getCorreo() != null ? d.getCorreo() : "");
                txtTelefono.setText(d.getTelefono() != null ? d.getTelefono() : "");
            }
        }
        txtClaveActual.setText("");
        txtClaveNueva.setText("");
        txtClaveConfirm.setText("");
    }

    private void guardar() {
        if (usuarioSesion == null || usuarioController == null) {
            return;
        }
        boolean esDocente = "DOCENTE".equalsIgnoreCase(usuarioSesion.getRol());
        String claveActual = new String(txtClaveActual.getPassword());
        String claveNueva = new String(txtClaveNueva.getPassword());
        String claveConfirm = new String(txtClaveConfirm.getPassword());

        Usuario actualizado = usuarioController.actualizarPerfil(
                usuarioSesion.getIdUsuario(),
                txtUsername.getText(),
                txtNombres.getText(),
                txtApellidos.getText(),
                claveActual,
                claveNueva,
                claveConfirm);
        if (actualizado == null) {
            String err = usuarioController.getUltimoError();
            Mensajes.error(err != null ? err : "No se pudo actualizar el perfil.");
            return;
        }

        if (esDocente && docenteController != null) {
            String ced = lblCedula.getText();
            Docente d = docenteController.buscarPorCedula(ced);
            if (d != null) {
                String correo = txtCorreo.getText() != null ? txtCorreo.getText().trim() : "";
                String telefono = txtTelefono.getText() != null ? txtTelefono.getText().trim() : "";
                if (!correo.isEmpty() && !correo.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.com$")) {
                    Mensajes.error("Ingrese un correo válido con dominio .com.");
                    return;
                }
                if (!telefono.isEmpty() && (!telefono.matches("^\\d+$") || telefono.length() != 11)) {
                    Mensajes.error("El teléfono debe tener 11 dígitos.");
                    return;
                }
                d.setNombres(actualizado.getNombres());
                d.setApellidos(actualizado.getApellidos());
                if (!correo.isEmpty()) {
                    d.setCorreo(correo);
                }
                if (!telefono.isEmpty()) {
                    d.setTelefono(telefono);
                }
                if (!docenteController.actualizar(d)) {
                    String err = docenteController.getUltimoError();
                    Mensajes.error(err != null ? err : "No se pudieron actualizar los datos de docente.");
                    return;
                }
            }
        }

        usuarioSesion = actualizado;
        if (onPerfilActualizado != null) {
            onPerfilActualizado.accept(actualizado);
        }
        cargarDesdeSesion();
        Mensajes.info("Perfil actualizado correctamente.");
    }
}
