package com.horarios.view;

import com.horarios.controller.DocenteController;
import com.horarios.controller.UsuarioController;
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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.JFrame;
import javax.swing.BoxLayout;

public class UsuarioPanel extends JPanel {

    private final UsuarioController controller;
    private final DocenteController docenteController;
    private final Usuario usuarioActual;
    private final UsuarioTableModel tableModel;
    private final JTable tabla;
    private JScrollPane scrollTabla;
    private TableRowSorter<UsuarioTableModel> sorter;
    private JTextField txtFiltro;
    private JComboBox<String> cboRolFiltro;
    private JComboBox<String> cboEstadoFiltro;

    public UsuarioPanel(UsuarioController controller, DocenteController docenteController) {
        this(controller, docenteController, null);
    }

    public UsuarioPanel(UsuarioController controller, DocenteController docenteController, Usuario usuarioActual) {
        this.controller = controller;
        this.docenteController = docenteController;
        this.usuarioActual = usuarioActual;
        this.tableModel = new UsuarioTableModel();
        this.tabla = new JTable(tableModel);
        construirUI();
        refrescarTabla();
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitulo = new JLabel("Módulo de Usuarios", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel lblSubtitulo = new JLabel(
                "Administración de cuentas de administrador y coordinador.",
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

        btnRegistrar.addActionListener(e -> registrarUsuarioDialog());
        btnModificar.addActionListener(e -> modificarUsuarioDialog());
        btnConsultar.addActionListener(e -> consultarUsuarioDialog());
        btnRefrescar.addActionListener(e -> refrescarTabla());
        btnEliminar.addActionListener(e -> eliminarUsuario());

        JPanel panelAcciones = CrudPanelUtil.crearBarraAcciones(
                btnRegistrar, btnModificar, btnConsultar, btnRefrescar, btnEliminar);
        PanelPermisosUtil.aplicarBotonesCrud(usuarioActual, PermisosRol.MOD_USUARIOS,
                btnRegistrar, btnModificar, btnEliminar);

        // Una sola línea: buscador comparte fila con Rol y Estado (el texto crece solo en el espacio sobrante)
        JPanel panelFiltro = new JPanel(new GridBagLayout());
        panelFiltro.setOpaque(false);
        panelFiltro.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        GridBagConstraints gf = new GridBagConstraints();
        gf.insets = new Insets(4, 0, 4, 8);
        gf.anchor = GridBagConstraints.WEST;
        gf.gridy = 0;
        gf.gridx = 0;
        panelFiltro.add(new JLabel("Buscar:"), gf);
        txtFiltro = new JTextField();
        txtFiltro.setPreferredSize(new Dimension(200, 32));
        txtFiltro.setMinimumSize(new Dimension(120, 32));
        gf.gridx = 1;
        gf.weightx = 1.0;
        gf.fill = GridBagConstraints.HORIZONTAL;
        panelFiltro.add(txtFiltro, gf);
        gf.weightx = 0;
        gf.fill = GridBagConstraints.NONE;
        gf.gridx = 2;
        panelFiltro.add(new JLabel("Rol:"), gf);
        gf.gridx = 3;
        cboRolFiltro = new JComboBox<>(new String[]{"TODOS", "ADMIN", "COORDINADOR", "DOCENTE"});
        cboRolFiltro.setPreferredSize(new Dimension(150, 32));
        panelFiltro.add(cboRolFiltro, gf);
        gf.gridx = 4;
        panelFiltro.add(new JLabel("Estado:"), gf);
        gf.gridx = 5;
        cboEstadoFiltro = new JComboBox<>(new String[]{"Todos", "Activos", "Inactivos"});
        cboEstadoFiltro.setPreferredSize(new Dimension(150, 32));
        panelFiltro.add(cboEstadoFiltro, gf);

        JPanel panelNorte = new JPanel();
        panelNorte.setOpaque(false);
        panelNorte.setLayout(new BoxLayout(panelNorte, BoxLayout.Y_AXIS));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelAcciones.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelFiltro.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelNorte.add(header);
        panelNorte.add(panelAcciones);
        panelNorte.add(panelFiltro);

        scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Usuarios registrados (activos e inactivos)"));
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(22);
        tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        sorter = new TableRowSorter<>(tableModel);
        tabla.setRowSorter(sorter);

        javax.swing.event.DocumentListener dl = new javax.swing.event.DocumentListener() {
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
        };
        txtFiltro.getDocument().addDocumentListener(dl);
        cboRolFiltro.addActionListener(e -> aplicarFiltro());
        cboEstadoFiltro.addActionListener(e -> aplicarFiltro());

        add(panelNorte, BorderLayout.NORTH);
        add(scrollTabla, BorderLayout.CENTER);
    }

    /** Recarga datos del controlador (p. ej. tras crear usuario desde Docentes). */
    public void refrescar() {
        refrescarTabla();
    }

    private void aplicarFiltro() {
        if (sorter == null) {
            return;
        }
        String texto = txtFiltro != null && txtFiltro.getText() != null ? txtFiltro.getText().trim().toLowerCase() : "";
        String rol = cboRolFiltro != null && cboRolFiltro.getSelectedItem() != null ? cboRolFiltro.getSelectedItem().toString() : "TODOS";
        String estadoF = cboEstadoFiltro != null && cboEstadoFiltro.getSelectedItem() != null ? cboEstadoFiltro.getSelectedItem().toString() : "Todos";

        sorter.setRowFilter(new RowFilter<UsuarioTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends UsuarioTableModel, ? extends Integer> entry) {
                boolean okTexto = texto.isEmpty();
                if (!okTexto) {
                    for (int i = 0; i < entry.getValueCount(); i++) {
                        String v = entry.getStringValue(i);
                        if (v != null && v.toLowerCase().contains(texto)) {
                            okTexto = true;
                            break;
                        }
                    }
                }
                String r = entry.getStringValue(4) != null ? entry.getStringValue(4).toUpperCase() : "";
                String est = entry.getStringValue(5) != null ? entry.getStringValue(5) : "";
                boolean okRol = "TODOS".equalsIgnoreCase(rol) || r.equalsIgnoreCase(rol);
                boolean okEstado = true;
                if ("Activos".equalsIgnoreCase(estadoF)) {
                    okEstado = "Activo".equalsIgnoreCase(est);
                } else if ("Inactivos".equalsIgnoreCase(estadoF)) {
                    okEstado = "Inactivo".equalsIgnoreCase(est);
                }
                return okTexto && okRol && okEstado;
            }
        });
    }

    private void registrarUsuarioDialog() {
        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        UsuarioFormDialog dlg = new UsuarioFormDialog(owner, "Registrar Usuario", null, false);
        dlg.setVisible(true);
        Usuario u = dlg.getResultado();
        if (u == null) {
            return;
        }
        Usuario creado = controller.registrar(u);
        if (creado == null) {
            String err = controller.getUltimoError();
            Mensajes.error(err != null ? err : "Ya existe un usuario con el mismo nombre de usuario.");
            return;
        }
        Mensajes.info("Usuario registrado correctamente.\n\nUsuario: " + creado.getUsername());
        refrescarTabla();
    }

    private Usuario usuarioSeleccionado() {
        int viewRow = tabla.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return tableModel.getAt(tabla.convertRowIndexToModel(viewRow));
    }

    private void modificarUsuarioDialog() {
        Usuario existente = usuarioSeleccionado();
        if (existente == null) {
            String clave = Mensajes.pedirTextoNoVacioCancelable("Seleccione una fila o ingrese el username o la cédula del usuario a modificar:");
            if (clave == null || !validarClaveBusquedaUsuario(clave)) {
                return;
            }
            existente = buscarUsuarioPorUsernameOCedula(clave);
            if (existente == null) {
                Mensajes.error("No se encontró un usuario con el nombre de usuario o la cédula indicados.");
                return;
            }
        }
        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        UsuarioFormDialog dlg = new UsuarioFormDialog(owner, "Modificar Usuario", existente, false);
        dlg.setVisible(true);
        Usuario actualizado = dlg.getResultado();
        if (actualizado == null) {
            return;
        }
        actualizado.setIdUsuario(existente.getIdUsuario());
        boolean ok = controller.actualizar(actualizado);
        if (ok) {
            Mensajes.info("Usuario actualizado correctamente.");
            refrescarTabla();
        } else {
            String err = controller.getUltimoError();
            Mensajes.error(err != null ? err : "No se pudo modificar el usuario.\n\nVerifique que el nombre de usuario no esté duplicado.");
        }
    }

    private void consultarUsuarioDialog() {
        Usuario u = usuarioSeleccionado();
        if (u == null) {
            String clave = Mensajes.pedirTextoNoVacioCancelable("Seleccione una fila o ingrese el username o la cédula del usuario a consultar:");
            if (clave == null || !validarClaveBusquedaUsuario(clave)) {
                return;
            }
            u = buscarUsuarioPorUsernameOCedula(clave);
            if (u == null) {
                Mensajes.error("No se encontró un usuario con el nombre de usuario o la cédula indicados.");
                return;
            }
        }
        JFrame owner = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        UsuarioFormDialog dlg = new UsuarioFormDialog(owner, "Consultar Usuario", u, true);
        dlg.setVisible(true);
    }

    private void eliminarUsuario() {
        Usuario existente = usuarioSeleccionado();
        if (existente == null) {
            String clave = Mensajes.pedirTextoNoVacioCancelable("Seleccione una fila o ingrese el username o la cédula del usuario a eliminar:");
            if (clave == null || !validarClaveBusquedaUsuario(clave)) {
                return;
            }
            existente = buscarUsuarioPorUsernameOCedula(clave);
            if (existente == null) {
                Mensajes.error("No se encontró un usuario con el nombre de usuario o la cédula indicados.");
                return;
            }
        }
        if (existente.getEstado() != 1) {
            Mensajes.error("El usuario ya está inactivo.");
            return;
        }
        int opcion = Mensajes.confirmar("¿Está seguro de eliminar este usuario?\n\nUsuario: " + existente.getUsername()
                + "\nRol: " + existente.getRol());
        if (opcion == 0) {
            boolean ok = controller.eliminarLogico(existente.getIdUsuario());
            if (ok) {
                Mensajes.info("Usuario eliminado.");
                refrescarTabla();
            } else {
                Mensajes.error("No se pudo eliminar el usuario.");
            }
        }
    }

    private void refrescarTabla() {
        tableModel.setDatos(controller.listarTodos());
        aplicarFiltro();
        TablaUtil.aplicarAnchosContenidoYRellenarPadre(scrollTabla, tabla, 12, 52, 520, new int[]{0, 2, 3});
    }

    /** Si la clave es solo dígitos, se trata como cédula (8 dígitos). */
    private boolean validarClaveBusquedaUsuario(String clave) {
        if (clave != null && clave.trim().matches("^\\d+$") && !Mensajes.esCedulaValida(clave.trim())) {
            Mensajes.error("Para buscar por cédula, ingrese " + Mensajes.CEDULA_DIGITOS + " dígitos.");
            return false;
        }
        return true;
    }

    /** Docente cuya cédula coincide con el username (convención al crear usuario docente). */
    private Docente docenteVinculado(Usuario u) {
        if (docenteController == null || u == null || u.getUsername() == null) {
            return null;
        }
        return docenteController.buscarPorCedula(u.getUsername());
    }

    /**
     * Localiza un usuario por su {@code username} o por la {@code cédula} del docente vinculado
     * (cuando el acceso docente usa la cédula como username).
     */
    private Usuario buscarUsuarioPorUsernameOCedula(String clave) {
        if (clave == null) {
            return null;
        }
        String t = clave.trim();
        if (t.isEmpty()) {
            return null;
        }
        Usuario u = controller.buscarPorUsername(t);
        if (u != null) {
            return u;
        }
        if (docenteController != null) {
            Docente d = docenteController.buscarPorCedula(t);
            if (d != null && d.getCedula() != null) {
                return controller.buscarPorUsername(d.getCedula());
            }
        }
        return null;
    }

    private class UsuarioTableModel extends AbstractTableModel {

        private final String[] columnas = new String[]{"Username", "Cédula", "Nombres", "Apellidos", "Rol", "Estado"};
        private List<Usuario> datos;

        public UsuarioTableModel() {
            this.datos = new java.util.ArrayList<>();
        }

        public void setDatos(List<Usuario> datos) {
            this.datos = datos != null ? datos : new java.util.ArrayList<>();
            fireTableDataChanged();
        }

        public Usuario getAt(int row) {
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
            Usuario u = datos.get(rowIndex);
            Docente d = docenteVinculado(u);
            switch (columnIndex) {
                case 0:
                    return u.getUsername() != null ? u.getUsername() : "";
                case 1:
                    if (u.getCedula() != null && !u.getCedula().isEmpty()) {
                        return u.getCedula();
                    }
                    if (d != null && d.getCedula() != null) {
                        return d.getCedula();
                    }
                    if (u.getRol() != null && u.getRol().equalsIgnoreCase("DOCENTE") && u.getUsername() != null) {
                        return u.getUsername();
                    }
                    return "";
                case 2:
                    if (u.getNombres() != null && !u.getNombres().isEmpty()) {
                        return u.getNombres();
                    }
                    return d != null && d.getNombres() != null ? d.getNombres() : "";
                case 3:
                    if (u.getApellidos() != null && !u.getApellidos().isEmpty()) {
                        return u.getApellidos();
                    }
                    return d != null && d.getApellidos() != null ? d.getApellidos() : "";
                case 4:
                    return u.getRol() != null ? u.getRol() : "";
                case 5:
                    return u.getEstado() == 1 ? "Activo" : "Inactivo";
                default:
                    return "";
            }
        }
    }
}

