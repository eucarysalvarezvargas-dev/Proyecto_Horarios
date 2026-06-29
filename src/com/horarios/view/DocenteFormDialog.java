package com.horarios.view;

import com.horarios.model.Docente;
import com.horarios.util.Mensajes;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DocenteFormDialog extends JDialog {

    private final JTextField txtCedula;
    private final JTextField txtNombres;
    private final JTextField txtApellidos;
    private final JTextField txtCorreo;
    private final JTextField txtTelefono;

    private Docente resultado;

    public DocenteFormDialog(JFrame owner, String titulo, Docente inicial, boolean soloLectura) {
        super(owner, titulo, true);
        setLayout(new BorderLayout(10, 10));

        txtCedula = new JTextField(18);
        Mensajes.aplicarCampoCedula(txtCedula);
        txtNombres = new JTextField(18);
        txtApellidos = new JTextField(18);
        txtCorreo = new JTextField(18);
        txtTelefono = new JTextField(18);

        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos del docente"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelFormulario.add(new JLabel("Cédula (8 dígitos):"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtCedula, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panelFormulario.add(new JLabel("Nombres:"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtNombres, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panelFormulario.add(new JLabel("Apellidos:"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtApellidos, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panelFormulario.add(new JLabel("Correo (.com):"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtCorreo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panelFormulario.add(new JLabel("Teléfono (11 dígitos):"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtTelefono, gbc);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnAceptar = new JButton("Aceptar");
        JButton btnCancelar = new JButton("Cancelar");

        btnAceptar.addActionListener(e -> onAceptar(soloLectura));
        btnCancelar.addActionListener(e -> onCancelar());

        panelBotones.add(btnAceptar);
        panelBotones.add(btnCancelar);

        add(panelFormulario, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        if (inicial != null) {
            txtCedula.setText(inicial.getCedula());
            txtNombres.setText(inicial.getNombres());
            txtApellidos.setText(inicial.getApellidos());
            txtCorreo.setText(inicial.getCorreo());
            txtTelefono.setText(inicial.getTelefono());
        }

        if (soloLectura) {
            txtCedula.setEditable(false);
            txtNombres.setEditable(false);
            txtApellidos.setEditable(false);
            txtCorreo.setEditable(false);
            txtTelefono.setEditable(false);
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private void onAceptar(boolean soloLectura) {
        if (soloLectura) {
            resultado = null;
            dispose();
            return;
        }

        String cedula = txtCedula.getText() != null ? txtCedula.getText().trim() : "";
        String nombres = txtNombres.getText() != null ? txtNombres.getText().trim() : "";
        String apellidos = txtApellidos.getText() != null ? txtApellidos.getText().trim() : "";
        String correo = txtCorreo.getText() != null ? txtCorreo.getText().trim() : "";
        String telefono = txtTelefono.getText() != null ? txtTelefono.getText().trim() : "";

        if (cedula.isEmpty() || nombres.isEmpty() || apellidos.isEmpty() || correo.isEmpty() || telefono.isEmpty()) {
            Mensajes.error("Complete todos los campos obligatorios.");
            return;
        }
        if (!Mensajes.esCedulaValida(cedula)) {
            Mensajes.error("La cédula debe tener " + Mensajes.CEDULA_DIGITOS + " dígitos.");
            return;
        }
        if (!nombres.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")) {
            Mensajes.error("Los nombres solo pueden contener letras.");
            return;
        }
        if (!apellidos.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")) {
            Mensajes.error("Los apellidos solo pueden contener letras.");
            return;
        }
        if (!correo.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.com$")) {
            Mensajes.error("Ingrese un correo válido con dominio .com.");
            return;
        }
        if (!telefono.matches("^\\d+$") || telefono.length() != 11) {
            Mensajes.error("El teléfono debe tener 11 dígitos numéricos.");
            return;
        }

        Docente d = new Docente();
        d.setCedula(cedula);
        d.setNombres(nombres);
        d.setApellidos(apellidos);
        d.setCorreo(correo);
        d.setTelefono(telefono);
        d.setEstado(1);
        resultado = d;
        dispose();
    }

    private void onCancelar() {
        resultado = null;
        dispose();
    }

    public Docente getResultado() {
        return resultado;
    }
}

