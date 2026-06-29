package com.horarios.view;

import com.horarios.controller.DocenteController;
import com.horarios.model.Docente;
import com.horarios.model.Usuario;
import com.horarios.util.CrudPanelUtil;
import com.horarios.util.Mensajes;
import com.horarios.util.PanelPermisosUtil;
import com.horarios.util.PermisosRol;
import com.horarios.util.TablaUtil;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.JFrame;

public class DocentePanel extends JPanel {

    private final DocenteController controller;
    private final Usuario usuarioActual;
    private final DocenteTableModel tableModel;
    private final JTable tabla;
    private JScrollPane scrollTabla;
    private TableRowSorter<DocenteTableModel> sorter;
    private JTextField txtFiltro;

    public DocentePanel(DocenteController controller) {
        this(controller, null);
    }

    public DocentePanel(DocenteController controller, Usuario usuarioActual) {
        this.controller = controller;
        this.usuarioActual = usuarioActual;
        this.tableModel = new DocenteTableModel();
        this.tabla = new JTable(tableModel);

        construirUI();
        refrescarTabla();
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitulo = new JLabel("Módulo de Docentes", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel lblSubtitulo = new JLabel(
                "Registro y mantenimiento del catálogo de docentes.",
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

        btnRegistrar.addActionListener(e -> registrarDocenteDialog());
        btnModificar.addActionListener(e -> modificarDocenteDialog());
        btnConsultar.addActionListener(e -> consultarDocenteDialog());
        btnRefrescar.addActionListener(e -> refrescarTabla());
        btnEliminar.addActionListener(e -> eliminarDocente());

        JPanel panelAcciones = CrudPanelUtil.crearBarraAcciones(
                btnRegistrar, btnModificar, btnConsultar, btnRefrescar, btnEliminar);
        PanelPermisosUtil.aplicarBotonesCrud(usuarioActual, PermisosRol.MOD_DOCENTES,
                btnRegistrar, btnModificar, btnEliminar);

        JPanel panelNorte = new JPanel();
        panelNorte.setOpaque(false);
        panelNorte.setLayout(new javax.swing.BoxLayout(panelNorte, javax.swing.BoxLayout.Y_AXIS));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelAcciones.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelNorte.add(header);
        panelNorte.add(panelAcciones);

        JPanel panelFiltro = new JPanel(new GridBagLayout());
        panelFiltro.setOpaque(false);
        panelFiltro.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        GridBagConstraints gf = new GridBagConstraints();
        gf.insets = new Insets(4, 0, 4, 10);
        gf.anchor = GridBagConstraints.WEST;
        gf.gridy = 0;
        gf.gridx = 0;
        panelFiltro.add(new JLabel("Buscar:"), gf);
        txtFiltro = new JTextField();
        txtFiltro.setPreferredSize(new Dimension(400, 32));
        txtFiltro.setMinimumSize(new Dimension(120, 32));
        gf.gridx = 1;
        gf.weightx = 1.0;
        gf.fill = GridBagConstraints.HORIZONTAL;
        panelFiltro.add(txtFiltro, gf);
        panelFiltro.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelNorte.add(panelFiltro);

        scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Docentes registrados (activos)"));

        sorter = new TableRowSorter<>(tableModel);
        tabla.setRowSorter(sorter);
        txtFiltro.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                aplicarFiltro();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                aplicarFiltro();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                aplicarFiltro();
            }
        });

        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(22);
        tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        add(panelNorte, BorderLayout.NORTH);
        add(scrollTabla, BorderLayout.CENTER);
    }

    private void aplicarFiltro() {
        if (sorter == null) {
            return;
        }
        String texto = txtFiltro != null && txtFiltro.getText() != null ? txtFiltro.getText().trim().toLowerCase() : "";
        if (texto.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        sorter.setRowFilter(new RowFilter<DocenteTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DocenteTableModel, ? extends Integer> entry) {
                for (int i = 0; i < entry.getValueCount(); i++) {
                    String v = entry.getStringValue(i);
                    if (v != null && v.toLowerCase().contains(texto)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void registrarDocenteDialog() {
        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        DocenteFormDialog dlg = new DocenteFormDialog(owner, "Registrar Docente", null, false);
        dlg.setVisible(true);
        Docente d = dlg.getResultado();
        if (d == null) {
            return;
        }
        Docente creado = controller.registrar(d);
        if (creado == null) {
            String err = controller.getUltimoError();
            Mensajes.error(err != null ? err : "No se pudo registrar el docente.\n\nVerifique cédula duplicada o usuario asociado.");
            return;
        }
        Mensajes.info("Docente registrado correctamente.\n\nCédula: " + creado.getCedula()
                + "\n\nAcceso creado:\nUsername: " + creado.getCedula() + "\nClave inicial: " + creado.getCedula() + "\nRol: DOCENTE");
        refrescarTabla();
    }

    private Docente docenteSeleccionado() {
        int viewRow = tabla.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return tableModel.getAt(tabla.convertRowIndexToModel(viewRow));
    }

    private void modificarDocenteDialog() {
        Docente existente = docenteSeleccionado();
        if (existente == null) {
            String cedulaBuscar = Mensajes.pedirCedula9Cancelable("Seleccione una fila o ingrese la CÉDULA del docente a modificar:");
            if (cedulaBuscar == null) {
                return;
            }
            existente = controller.buscarPorCedula(cedulaBuscar);
            if (existente == null || existente.getEstado() != 1) {
                Mensajes.error("No se encontró un docente activo con la cédula indicada.");
                return;
            }
        }
        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        DocenteFormDialog dlg = new DocenteFormDialog(owner, "Modificar Docente", existente, false);
        dlg.setVisible(true);
        Docente actualizado = dlg.getResultado();
        if (actualizado == null) {
            return;
        }
        actualizado.setIdDocente(existente.getIdDocente());
        boolean ok = controller.actualizar(actualizado);
        if (ok) {
            Mensajes.info("Docente actualizado correctamente.");
            refrescarTabla();
        } else {
            Mensajes.error("No se pudo modificar el docente.\n\nVerifique que la cédula no esté duplicada.");
        }
    }

    private void consultarDocenteDialog() {
        Docente d = docenteSeleccionado();
        if (d == null) {
            String cedulaBuscar = Mensajes.pedirCedula9Cancelable("Seleccione una fila o ingrese la CÉDULA del docente a consultar:");
            if (cedulaBuscar == null) {
                return;
            }
            d = controller.buscarPorCedula(cedulaBuscar);
            if (d == null || d.getEstado() != 1) {
                Mensajes.error("No se encontró un docente activo con la cédula indicada.");
                return;
            }
        }
        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        DocenteFormDialog dlg = new DocenteFormDialog(owner, "Consultar Docente", d, true);
        dlg.setVisible(true);
    }

    private void eliminarDocente() {
        Docente existente = docenteSeleccionado();
        if (existente == null) {
            String cedulaBuscar = Mensajes.pedirCedula9Cancelable("Seleccione una fila o ingrese la CÉDULA del docente a eliminar:");
            if (cedulaBuscar == null) {
                return;
            }
            existente = controller.buscarPorCedula(cedulaBuscar);
            if (existente == null || existente.getEstado() != 1) {
                Mensajes.error("No se encontró un docente activo con la cédula indicada.");
                return;
            }
        }
        int opcion = Mensajes.confirmar("¿Está seguro de eliminar este docente?\n\n" + formatearDocente(existente));
        if (opcion == 0) {
            boolean ok = controller.eliminarLogico(existente.getIdDocente());
            if (ok) {
                Mensajes.info("Docente eliminado.");
                refrescarTabla();
            } else {
                Mensajes.error("No se pudo eliminar el docente.");
            }
        }
    }

    private void refrescarTabla() {
        tableModel.setDatos(controller.listarActivos());
        aplicarFiltro();
        TablaUtil.aplicarAnchosContenidoYRellenarPadre(scrollTabla, tabla, 14, 52, 560, new int[]{1, 2, 3});
    }

    private String formatearDocente(Docente d) {
        StringBuilder sb = new StringBuilder();
        sb.append("DOCENTE\n\n");
        sb.append("ID: ").append(d.getIdDocente()).append("\n");
        sb.append("Cédula: ").append(d.getCedula()).append("\n");
        sb.append("Nombres: ").append(d.getNombres()).append("\n");
        sb.append("Apellidos: ").append(d.getApellidos()).append("\n");
        sb.append("Correo: ").append(d.getCorreo()).append("\n");
        sb.append("Teléfono: ").append(d.getTelefono()).append("\n");
        return sb.toString();
    }

    private class DocenteTableModel extends AbstractTableModel {

        private final String[] columnas = new String[]{"Cédula", "Nombres", "Apellidos", "Correo", "Teléfono"};
        private List<Docente> datos;

        public DocenteTableModel() {
            this.datos = new java.util.ArrayList<>();
        }

        public void setDatos(List<Docente> datos) {
            this.datos = datos != null ? datos : new java.util.ArrayList<>();
            fireTableDataChanged();
        }

        public Docente getAt(int row) {
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
            Docente d = datos.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return d.getCedula();
                case 1:
                    return d.getNombres();
                case 2:
                    return d.getApellidos();
                case 3:
                    return d.getCorreo();
                case 4:
                    return d.getTelefono();
                default:
                    return "";
            }
        }
    }
}

