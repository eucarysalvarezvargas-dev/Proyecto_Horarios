package com.horarios.util;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

/** Barra de acciones CRUD en dos filas para que «Eliminar» siempre sea visible. */
public final class CrudPanelUtil {

    private CrudPanelUtil() {
    }

    public static JPanel crearBarraAcciones(
            JButton btnRegistrar,
            JButton btnModificar,
            JButton btnConsultar,
            JButton btnRefrescar,
            JButton btnEliminar) {
        Dimension size = new Dimension(150, 34);
        aplicarTamano(btnRegistrar, size);
        aplicarTamano(btnModificar, size);
        aplicarTamano(btnConsultar, size);
        aplicarTamano(btnRefrescar, size);
        btnEliminar.setPreferredSize(new Dimension(150, 34));
        btnEliminar.setText("Eliminar");
        btnEliminar.setFont(new Font("SansSerif", Font.BOLD, 12));

        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fila1.setOpaque(false);
        fila1.add(btnRegistrar);
        fila1.add(btnModificar);
        fila1.add(btnConsultar);

        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fila2.setOpaque(false);
        fila2.add(btnRefrescar);
        fila2.add(btnEliminar);

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        fila1.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        fila2.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        panel.add(fila1);
        panel.add(fila2);
        return panel;
    }

    private static void aplicarTamano(JButton btn, Dimension size) {
        if (btn != null) {
            btn.setPreferredSize(size);
        }
    }
}
