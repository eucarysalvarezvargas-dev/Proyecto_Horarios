package com.horarios.view;

import com.horarios.controller.AsignaturaController;
import com.horarios.model.Asignatura;
import com.horarios.model.Usuario;
import com.horarios.util.CrudPanelUtil;
import com.horarios.util.Mensajes;
import com.horarios.util.PanelPermisosUtil;
import com.horarios.util.PermisosRol;
import com.horarios.util.TablaUtil;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.JFrame;

public class AsignaturaPanel extends JPanel {

    private final AsignaturaController controller;
    private final Usuario usuarioActual;
    private final AsignaturaTableModel tableModel;
    private final JTable tabla;
    private JScrollPane scrollTabla;

    public AsignaturaPanel(AsignaturaController controller) {
        this(controller, null);
    }

    public AsignaturaPanel(AsignaturaController controller, Usuario usuarioActual) {
        this.controller = controller;
        this.usuarioActual = usuarioActual;
        this.tableModel = new AsignaturaTableModel();
        this.tabla = new JTable(tableModel);
        construirUI();
        refrescarTabla();
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitulo = new JLabel("Módulo de Asignaturas", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel lblSubtitulo = new JLabel(
                "Registro y mantenimiento del catálogo de asignaturas.",
                SwingConstants.LEFT);
        lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(lblTitulo, BorderLayout.NORTH);
        header.add(lblSubtitulo, BorderLayout.CENTER);

        JButton btnRegistrar = new JButton("Registrar");
        JButton btnModificar = new JButton("Modificar");
        JButton btnConsultar = new JButton("Consultar");
        JButton btnRefrescar = new JButton("Refrescar");
        JButton btnEliminar = new JButton("Eliminar");

        btnRegistrar.addActionListener(e -> registrarAsignaturaDialog());
        btnModificar.addActionListener(e -> modificarAsignaturaDialog());
        btnConsultar.addActionListener(e -> consultarAsignaturaDialog());
        btnRefrescar.addActionListener(e -> refrescarTabla());
        btnEliminar.addActionListener(e -> eliminarAsignatura());

        JPanel panelAcciones = CrudPanelUtil.crearBarraAcciones(
                btnRegistrar, btnModificar, btnConsultar, btnRefrescar, btnEliminar);
        PanelPermisosUtil.aplicarBotonesCrud(usuarioActual, PermisosRol.MOD_ASIGNATURAS,
                btnRegistrar, btnModificar, btnEliminar);

        JPanel panelNorte = new JPanel();
        panelNorte.setOpaque(false);
        panelNorte.setLayout(new BoxLayout(panelNorte, BoxLayout.Y_AXIS));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelAcciones.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelNorte.add(header);
        panelNorte.add(panelAcciones);

        scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Asignaturas registradas (activas)"));
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(22);
        tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        add(panelNorte, BorderLayout.NORTH);
        add(scrollTabla, BorderLayout.CENTER);
    }

    private Asignatura asignaturaSeleccionada() {
        int viewRow = tabla.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return tableModel.getAt(tabla.convertRowIndexToModel(viewRow));
    }

    private void registrarAsignaturaDialog() {
        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        AsignaturaFormDialog dlg = new AsignaturaFormDialog(owner, "Registrar Asignatura", null, false);
        dlg.setVisible(true);
        Asignatura a = dlg.getResultado();
        if (a == null) {
            return;
        }
        Asignatura creada = controller.registrar(a);
        if (creada == null) {
            String err = controller.getUltimoError();
            Mensajes.error(err != null ? err : "Ya existe una asignatura registrada con el mismo código.");
            return;
        }
        Mensajes.info("Asignatura registrada correctamente.\n\nCódigo: " + creada.getCodigo());
        refrescarTabla();
    }

    private void modificarAsignaturaDialog() {
        Asignatura existente = asignaturaSeleccionada();
        if (existente == null) {
            String codigo = Mensajes.pedirTextoNoVacioCancelable("Seleccione una fila o ingrese el CÓDIGO de la asignatura a modificar:");
            if (codigo == null) {
                return;
            }
            existente = controller.buscarPorCodigo(codigo);
            if (existente == null || existente.getEstado() != 1) {
                Mensajes.error("No se encontró una asignatura activa con el código indicado.");
                return;
            }
        }

        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        AsignaturaFormDialog dlg = new AsignaturaFormDialog(owner, "Modificar Asignatura", existente, false);
        dlg.setVisible(true);
        Asignatura actualizado = dlg.getResultado();
        if (actualizado == null) {
            return;
        }
        actualizado.setIdAsignatura(existente.getIdAsignatura());
        boolean ok = controller.actualizar(actualizado);
        if (ok) {
            Mensajes.info("Asignatura actualizada correctamente.");
            refrescarTabla();
        } else {
            String err = controller.getUltimoError();
            Mensajes.error(err != null ? err : "No se pudo modificar la asignatura.\n\nVerifique que el código no esté duplicado.");
        }
    }

    private void consultarAsignaturaDialog() {
        Asignatura a = asignaturaSeleccionada();
        if (a == null) {
            String codigo = Mensajes.pedirTextoNoVacioCancelable("Seleccione una fila o ingrese el CÓDIGO de la asignatura a consultar:");
            if (codigo == null) {
                return;
            }
            a = controller.buscarPorCodigo(codigo);
            if (a == null || a.getEstado() != 1) {
                Mensajes.error("No se encontró una asignatura activa con el código indicado.");
                return;
            }
        }
        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        AsignaturaFormDialog dlg = new AsignaturaFormDialog(owner, "Consultar Asignatura", a, true);
        dlg.setVisible(true);
    }

    private void eliminarAsignatura() {
        Asignatura existente = asignaturaSeleccionada();
        if (existente == null) {
            String codigo = Mensajes.pedirTextoNoVacioCancelable("Seleccione una fila o ingrese el CÓDIGO de la asignatura a eliminar:");
            if (codigo == null) {
                return;
            }
            existente = controller.buscarPorCodigo(codigo);
            if (existente == null || existente.getEstado() != 1) {
                Mensajes.error("No se encontró una asignatura activa con el código indicado.");
                return;
            }
        }
        int opcion = Mensajes.confirmar("¿Está seguro de eliminar esta asignatura?\n\nCódigo: " + existente.getCodigo()
                + "\nNombre: " + existente.getNombre());
        if (opcion == 0) {
            boolean ok = controller.eliminarLogico(existente.getIdAsignatura());
            if (ok) {
                Mensajes.info("Asignatura eliminada.");
                refrescarTabla();
            } else {
                Mensajes.error("No se pudo eliminar la asignatura.");
            }
        }
    }

    private void refrescarTabla() {
        tableModel.setDatos(controller.listarActivas());
        TablaUtil.aplicarAnchosContenidoYRellenarPadre(scrollTabla, tabla, 14, 52, 520, new int[]{1});
    }

    private class AsignaturaTableModel extends AbstractTableModel {

        private final String[] columnas = new String[]{"Código", "Nombre", "Horas"};
        private List<Asignatura> datos;

        public AsignaturaTableModel() {
            this.datos = new java.util.ArrayList<>();
        }

        public void setDatos(List<Asignatura> datos) {
            this.datos = datos != null ? datos : new java.util.ArrayList<>();
            fireTableDataChanged();
        }

        public Asignatura getAt(int row) {
            if (row < 0 || row >= datos.size()) {
                return null;
            }
            return datos.get(row);
        }

        @Override
        public int getRowCount() {
            return datos.size();
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
            Asignatura a = datos.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return a.getCodigo();
                case 1:
                    return a.getNombre();
                case 2:
                    return a.getHorasSemanales();
                default:
                    return "";
            }
        }
    }
}
