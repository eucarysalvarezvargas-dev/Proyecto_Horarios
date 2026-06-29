package com.horarios.view;

import com.horarios.model.Usuario;
import com.horarios.util.Mensajes;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class UsuarioFormDialog extends JDialog {

    private static final String MANTENER_DOCENTE = "— Mantener DOCENTE —";

    private enum ModoFormulario {
        REGISTRAR, MODIFICAR, CONSULTAR
    }

    private final JTextField txtUsername;
    private final JPasswordField txtPassword;
    private final JLabel lblClaveSegura;
    private final ModoFormulario modo;
    private final JComboBox<String> cboRol;
    private final JTextField txtCedula;
    private final JTextField txtNombres;
    private final JTextField txtApellidos;
    private final boolean usuarioDocenteExistente;
  /** Al modificar, conserva activo/inactivo; al registrar nuevo, queda activo (1). */
    private final int estadoAlPersistir;

    private Usuario resultado;

    public UsuarioFormDialog(JFrame owner, String titulo, Usuario inicial, boolean soloLectura) {
        super(owner, titulo, true);
        setLayout(new BorderLayout(10, 10));

        if (inicial != null) {
            this.estadoAlPersistir = inicial.getEstado();
            this.modo = soloLectura ? ModoFormulario.CONSULTAR : ModoFormulario.MODIFICAR;
            this.usuarioDocenteExistente = inicial.getRol() != null
                    && "DOCENTE".equalsIgnoreCase(inicial.getRol().trim());
        } else {
            this.estadoAlPersistir = 1;
            this.modo = ModoFormulario.REGISTRAR;
            this.usuarioDocenteExistente = false;
        }

        txtUsername = new JTextField(18);
        txtPassword = new JPasswordField(18);
        lblClaveSegura = new JLabel("Contraseña protegida (no se muestra por seguridad)");
        cboRol = new JComboBox<>();
        configurarComboRoles();
        txtCedula = new JTextField(18);
        Mensajes.aplicarCampoCedula(txtCedula);
        txtNombres = new JTextField(18);
        txtApellidos = new JTextField(18);

        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos del usuario"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        gbc.gridx = 0;
        gbc.gridy = row;
        panelFormulario.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtUsername, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        if (modo == ModoFormulario.CONSULTAR) {
            gbc.gridwidth = 2;
            panelFormulario.add(lblClaveSegura, gbc);
            gbc.gridwidth = 1;
        } else {
            String etiquetaClave = modo == ModoFormulario.REGISTRAR
                    ? "Clave inicial:"
                    : "Nueva clave (opcional):";
            panelFormulario.add(new JLabel(etiquetaClave), gbc);
            gbc.gridx = 1;
            panelFormulario.add(txtPassword, gbc);
        }

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        String etiquetaRol = usuarioDocenteExistente && modo == ModoFormulario.MODIFICAR
                ? "Rol / promover:"
                : "Rol:";
        panelFormulario.add(new JLabel(etiquetaRol), gbc);
        gbc.gridx = 1;
        panelFormulario.add(cboRol, gbc);

        if (modo == ModoFormulario.REGISTRAR) {
            row++;
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.gridwidth = 2;
            panelFormulario.add(new JLabel(
                    "<html><i>Las cuentas de docente se registran desde el módulo Docentes.</i></html>"), gbc);
            gbc.gridwidth = 1;
        }

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        panelFormulario.add(new JLabel("Cédula (8 dígitos):"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtCedula, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        panelFormulario.add(new JLabel("Nombres:"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtNombres, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        panelFormulario.add(new JLabel("Apellidos:"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtApellidos, gbc);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnAceptar = new JButton("Aceptar");
        JButton btnCancelar = new JButton("Cancelar");

        btnAceptar.addActionListener(e -> onAceptar());
        btnCancelar.addActionListener(e -> onCancelar());

        panelBotones.add(btnAceptar);
        panelBotones.add(btnCancelar);

        add(panelFormulario, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        if (inicial != null) {
            txtUsername.setText(inicial.getUsername());
            seleccionarRolInicial(inicial.getRol());
            txtCedula.setText(inicial.getCedula() != null ? inicial.getCedula() : "");
            txtNombres.setText(inicial.getNombres() != null ? inicial.getNombres() : "");
            txtApellidos.setText(inicial.getApellidos() != null ? inicial.getApellidos() : "");
        }

        if (modo == ModoFormulario.CONSULTAR) {
            txtUsername.setEditable(false);
            cboRol.setEnabled(false);
            txtCedula.setEditable(false);
            txtNombres.setEditable(false);
            txtApellidos.setEditable(false);
        }

        if (usuarioDocenteExistente && modo == ModoFormulario.MODIFICAR) {
            txtUsername.setEditable(false);
            txtUsername.setToolTipText("Los docentes inician sesión con la cédula (módulo Docentes).");
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private void configurarComboRoles() {
        cboRol.removeAllItems();
        if (modo == ModoFormulario.CONSULTAR) {
            cboRol.addItem("ADMIN");
            cboRol.addItem("COORDINADOR");
            cboRol.addItem("DOCENTE");
            return;
        }
        if (usuarioDocenteExistente && modo == ModoFormulario.MODIFICAR) {
            cboRol.addItem(MANTENER_DOCENTE);
            cboRol.addItem("ADMIN");
            cboRol.addItem("COORDINADOR");
            return;
        }
        cboRol.addItem("ADMIN");
        cboRol.addItem("COORDINADOR");
    }

    private void seleccionarRolInicial(String rol) {
        if (rol == null) {
            return;
        }
        if (usuarioDocenteExistente && modo == ModoFormulario.MODIFICAR) {
            cboRol.setSelectedItem(MANTENER_DOCENTE);
            return;
        }
        String r = rol.toUpperCase();
        for (int i = 0; i < cboRol.getItemCount(); i++) {
            if (r.equals(cboRol.getItemAt(i))) {
                cboRol.setSelectedIndex(i);
                return;
            }
        }
        if (cboRol.getItemCount() > 0) {
            cboRol.setSelectedIndex(0);
        }
    }

    private String resolverRolSeleccionado() {
        Object sel = cboRol.getSelectedItem();
        if (sel == null) {
            return "";
        }
        String rol = sel.toString();
        if (MANTENER_DOCENTE.equals(rol)) {
            return "DOCENTE";
        }
        return rol;
    }

    private void onAceptar() {
        if (modo == ModoFormulario.CONSULTAR) {
            resultado = null;
            dispose();
            return;
        }

        String username = txtUsername.getText() != null ? txtUsername.getText().trim() : "";
        String password = new String(txtPassword.getPassword()).trim();
        String rol = resolverRolSeleccionado();
        String cedula = txtCedula.getText() != null ? txtCedula.getText().trim() : "";
        String nombres = txtNombres.getText() != null ? txtNombres.getText().trim() : "";
        String apellidos = txtApellidos.getText() != null ? txtApellidos.getText().trim() : "";

        if (username.isEmpty() || rol.isEmpty()) {
            Mensajes.error("Ingrese el nombre de usuario y el rol.");
            return;
        }
        if (modo == ModoFormulario.REGISTRAR && password.isEmpty()) {
            Mensajes.error("Ingrese la contraseña inicial del usuario.");
            return;
        }
        if (username.contains(" ")) {
            Mensajes.error("El nombre de usuario no debe contener espacios.");
            return;
        }

        if (!cedula.isEmpty() && !Mensajes.esCedulaValida(cedula)) {
            Mensajes.error("La cédula debe tener " + Mensajes.CEDULA_DIGITOS + " dígitos.");
            return;
        }
        if (!nombres.isEmpty() && !nombres.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")) {
            Mensajes.error("Los nombres solo pueden contener letras, o puede dejar el campo vacío.");
            return;
        }
        if (!apellidos.isEmpty() && !apellidos.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")) {
            Mensajes.error("Los apellidos solo pueden contener letras, o puede dejar el campo vacío.");
            return;
        }

        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPassword(password);
        u.setRol(rol);
        u.setEstado(estadoAlPersistir);
        u.setCedula(cedula.isEmpty() ? null : cedula);
        u.setNombres(nombres.isEmpty() ? null : nombres);
        u.setApellidos(apellidos.isEmpty() ? null : apellidos);
        resultado = u;
        dispose();
    }

    private void onCancelar() {
        resultado = null;
        dispose();
    }

    public Usuario getResultado() {
        return resultado;
    }
}
