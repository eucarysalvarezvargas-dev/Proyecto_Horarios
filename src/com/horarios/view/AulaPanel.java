package com.horarios.view;

import com.horarios.controller.AulaController;
import com.horarios.model.Aula;
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

public class AulaPanel extends JPanel {

    private final AulaController controller;
    private final Usuario usuarioActual;
    private final AulaTableModel tableModel;
    private final JTable tabla;
    private JScrollPane scrollTabla;

    public AulaPanel(AulaController controller) {
        this(controller, null);
    }

    public AulaPanel(AulaController controller, Usuario usuarioActual) {
        this.controller = controller;
        this.usuarioActual = usuarioActual;
        this.tableModel = new AulaTableModel();
        this.tabla = new JTable(tableModel);
        construirUI();
        refrescarTabla();
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitulo = new JLabel("Módulo de Aulas", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel lblSubtitulo = new JLabel(
                "Registro y mantenimiento del catálogo de aulas.",
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

        btnRegistrar.addActionListener(e -> registrarAulaDialog());
        btnModificar.addActionListener(e -> modificarAulaDialog());
        btnConsultar.addActionListener(e -> consultarAulaDialog());
        btnRefrescar.addActionListener(e -> refrescarTabla());
        btnEliminar.addActionListener(e -> eliminarAula());

        JPanel panelAcciones = CrudPanelUtil.crearBarraAcciones(
                btnRegistrar, btnModificar, btnConsultar, btnRefrescar, btnEliminar);
        PanelPermisosUtil.aplicarBotonesCrud(usuarioActual, PermisosRol.MOD_AULAS,
                btnRegistrar, btnModificar, btnEliminar);

        JPanel panelNorte = new JPanel();
        panelNorte.setOpaque(false);
        panelNorte.setLayout(new BoxLayout(panelNorte, BoxLayout.Y_AXIS));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelAcciones.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelNorte.add(header);
        panelNorte.add(panelAcciones);

        scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Aulas registradas (activas)"));
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(22);
        tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        add(panelNorte, BorderLayout.NORTH);
        add(scrollTabla, BorderLayout.CENTER);
    }

    private Aula aulaSeleccionada() {
        int viewRow = tabla.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return tableModel.getAt(tabla.convertRowIndexToModel(viewRow));
    }

    private void registrarAulaDialog() {
        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        AulaFormDialog dlg = new AulaFormDialog(owner, "Registrar Aula", null, false);
        dlg.setVisible(true);
        Aula a = dlg.getResultado();
        if (a == null) {
            return;
        }
        Aula creada = controller.registrar(a);
        if (creada == null) {
            String err = controller.getUltimoError();
            Mensajes.error(err != null ? err : "Ya existe un aula registrada con el mismo código.");
            return;
        }
        Mensajes.info("Aula registrada correctamente.\n\nCódigo: " + creada.getCodigo());
        refrescarTabla();
    }

    private void modificarAulaDialog() {
        Aula existente = aulaSeleccionada();
        if (existente == null) {
            String codigo = Mensajes.pedirTextoNoVacioCancelable("Seleccione una fila o ingrese el CÓDIGO del aula a modificar:");
            if (codigo == null) {
                return;
            }
            existente = controller.buscarPorCodigo(codigo);
            if (existente == null || existente.getEstado() != 1) {
                Mensajes.error("No se encontró un aula activa con el código indicado.");
                return;
            }
        }

        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        AulaFormDialog dlg = new AulaFormDialog(owner, "Modificar Aula", existente, false);
        dlg.setVisible(true);
        Aula actualizado = dlg.getResultado();
        if (actualizado == null) {
            return;
        }
        actualizado.setIdAula(existente.getIdAula());
        boolean ok = controller.actualizar(actualizado);
        if (ok) {
            Mensajes.info("Aula actualizada correctamente.");
            refrescarTabla();
        } else {
            String err = controller.getUltimoError();
            Mensajes.error(err != null ? err : "No se pudo modificar el aula.\n\nVerifique que el código no esté duplicado.");
        }
    }

    private void consultarAulaDialog() {
        Aula a = aulaSeleccionada();
        if (a == null) {
            String codigo = Mensajes.pedirTextoNoVacioCancelable("Seleccione una fila o ingrese el CÓDIGO del aula a consultar:");
            if (codigo == null) {
                return;
            }
            a = controller.buscarPorCodigo(codigo);
            if (a == null || a.getEstado() != 1) {
                Mensajes.error("No se encontró un aula activa con el código indicado.");
                return;
            }
        }
        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        AulaFormDialog dlg = new AulaFormDialog(owner, "Consultar Aula", a, true);
        dlg.setVisible(true);
    }

    private void eliminarAula() {
        Aula existente = aulaSeleccionada();
        if (existente == null) {
            String codigo = Mensajes.pedirTextoNoVacioCancelable("Seleccione una fila o ingrese el CÓDIGO del aula a eliminar:");
            if (codigo == null) {
                return;
            }
            existente = controller.buscarPorCodigo(codigo);
            if (existente == null || existente.getEstado() != 1) {
                Mensajes.error("No se encontró un aula activa con el código indicado.");
                return;
            }
        }
        int opcion = Mensajes.confirmar("¿Está seguro de eliminar este aula?\n\nCódigo: " + existente.getCodigo()
                + "\nDescripción: " + existente.getDescripcion());
        if (opcion == 0) {
            boolean ok = controller.eliminarLogico(existente.getIdAula());
            if (ok) {
                Mensajes.info("Aula eliminada.");
                refrescarTabla();
            } else {
                Mensajes.error("No se pudo eliminar el aula.");
            }
        }
    }

    private void refrescarTabla() {
        tableModel.setDatos(controller.listarActivas());
        TablaUtil.aplicarAnchosContenidoYRellenarPadre(scrollTabla, tabla, 14, 52, 520, new int[]{1});
    }

    public void refrescar() {
        refrescarTabla();
    }

    private class AulaTableModel extends AbstractTableModel {

        private final String[] columnas = new String[]{"Código", "Descripción", "Capacidad"};
        private List<Aula> datos;

        public AulaTableModel() {
            this.datos = new java.util.ArrayList<>();
        }

        public void setDatos(List<Aula> datos) {
            this.datos = datos != null ? datos : new java.util.ArrayList<>();
            fireTableDataChanged();
        }

        public Aula getAt(int row) {
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
            Aula a = datos.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return a.getCodigo();
                case 1:
                    return a.getDescripcion();
                case 2:
                    return a.getCapacidad();
                default:
                    return "";
            }
        }
    }
}
