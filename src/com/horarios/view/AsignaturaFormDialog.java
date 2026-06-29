package com.horarios.view;

import com.horarios.model.Asignatura;
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

public class AsignaturaFormDialog extends JDialog {

    private final JTextField txtCodigo;
    private final JTextField txtNombre;
    private final JTextField txtHoras;

    private Asignatura resultado;

    public AsignaturaFormDialog(JFrame owner, String titulo, Asignatura inicial, boolean soloLectura) {
        super(owner, titulo, true);
        setLayout(new BorderLayout(10, 10));

        txtCodigo = new JTextField(18);
        txtNombre = new JTextField(18);
        txtHoras = new JTextField(6);

        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos de la asignatura"));

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
        panelFormulario.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtNombre, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panelFormulario.add(new JLabel("Horas semanales (1-20):"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtHoras, gbc);

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
            txtNombre.setText(inicial.getNombre());
            txtHoras.setText(String.valueOf(inicial.getHorasSemanales()));
        }

        if (soloLectura) {
            txtCodigo.setEditable(false);
            txtNombre.setEditable(false);
            txtHoras.setEditable(false);
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
        String nombre = txtNombre.getText() != null ? txtNombre.getText().trim() : "";
        String horasTxt = txtHoras.getText() != null ? txtHoras.getText().trim() : "";

        if (codigo.isEmpty() || nombre.isEmpty() || horasTxt.isEmpty()) {
            Mensajes.error("Complete todos los campos obligatorios.");
            return;
        }

        int horas;
        try {
            horas = Integer.parseInt(horasTxt);
        } catch (NumberFormatException ex) {
            Mensajes.error("Ingrese un valor entre 1 y 20 horas semanales.");
            return;
        }
        if (horas < 1 || horas > 20) {
            Mensajes.error("Ingrese un valor entre 1 y 20 horas semanales.");
            return;
        }

        Asignatura a = new Asignatura();
        a.setCodigo(codigo);
        a.setNombre(nombre);
        a.setHorasSemanales(horas);
        a.setEstado(1);
        resultado = a;
        dispose();
    }

    private void onCancelar() {
        resultado = null;
        dispose();
    }

    public Asignatura getResultado() {
        return resultado;
    }
}

