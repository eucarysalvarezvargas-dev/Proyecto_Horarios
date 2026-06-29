package com.horarios.view;

import com.horarios.model.Aula;
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

public class AulaFormDialog extends JDialog {

    private final JTextField txtCodigo;
    private final JTextField txtDescripcion;
    private final JTextField txtCapacidad;

    private Aula resultado;

    public AulaFormDialog(JFrame owner, String titulo, Aula inicial, boolean soloLectura) {
        super(owner, titulo, true);
        setLayout(new BorderLayout(10, 10));

        txtCodigo = new JTextField(18);
        txtDescripcion = new JTextField(18);
        txtCapacidad = new JTextField(6);

        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos del aula"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelFormulario.add(new JLabel("Código:"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtCodigo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panelFormulario.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtDescripcion, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panelFormulario.add(new JLabel("Capacidad (1-200):"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtCapacidad, gbc);

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
            txtCodigo.setText(inicial.getCodigo());
            txtDescripcion.setText(inicial.getDescripcion());
            txtCapacidad.setText(String.valueOf(inicial.getCapacidad()));
        }

        if (soloLectura) {
            txtCodigo.setEditable(false);
            txtDescripcion.setEditable(false);
            txtCapacidad.setEditable(false);
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

        String codigo = txtCodigo.getText() != null ? txtCodigo.getText().trim() : "";
        String descripcion = txtDescripcion.getText() != null ? txtDescripcion.getText().trim() : "";
        String capTxt = txtCapacidad.getText() != null ? txtCapacidad.getText().trim() : "";

        if (codigo.isEmpty() || descripcion.isEmpty() || capTxt.isEmpty()) {
            Mensajes.error("Complete todos los campos obligatorios.");
            return;
        }

        int capacidad;
        try {
            capacidad = Integer.parseInt(capTxt);
        } catch (NumberFormatException ex) {
            Mensajes.error("Ingrese una capacidad entre 1 y 200 personas.");
            return;
        }
        if (capacidad < 1 || capacidad > 200) {
            Mensajes.error("Ingrese una capacidad entre 1 y 200 personas.");
            return;
        }

        Aula a = new Aula();
        a.setCodigo(codigo);
        a.setDescripcion(descripcion);
        a.setCapacidad(capacidad);
        a.setEstado(1);
        resultado = a;
        dispose();
    }

    private void onCancelar() {
        resultado = null;
        dispose();
    }

    public Aula getResultado() {
        return resultado;
    }
}

