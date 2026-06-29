package com.horarios.view;

import com.horarios.controller.BitacoraController;
import com.horarios.model.RegistroBitacora;
import com.horarios.util.TablaUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Consulta de bitácora de auditoría (MySQL o copia en memoria si falla el INSERT).
 */
public class BitacoraPanel extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final BitacoraController bitacoraController;
    private final BitacoraTableModel tableModel;
    private final JTable tabla;
    private JScrollPane scrollTabla;
    private TableRowSorter<BitacoraTableModel> sorter;
    private JTextField txtFiltro;

    public BitacoraPanel(BitacoraController bitacoraController) {
        this.bitacoraController = bitacoraController;
        this.tableModel = new BitacoraTableModel();
        this.tabla = new JTable(tableModel);
        construirUI();
        refrescarTabla();
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitulo = new JLabel("Bitácora de auditoría", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel lblSubtitulo = new JLabel(
                "Historial de acciones registradas en el sistema.",
                SwingConstants.LEFT);
        lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        header.add(lblTitulo, BorderLayout.NORTH);
        header.add(lblSubtitulo, BorderLayout.CENTER);

        JPanel panelNorte = new JPanel(new BorderLayout(8, 8));
        panelNorte.setOpaque(false);
        panelNorte.add(header, BorderLayout.NORTH);

        JPanel filaFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filaFiltro.setOpaque(false);
        filaFiltro.add(new JLabel("Filtrar:"));
        txtFiltro = new JTextField();
        txtFiltro.setPreferredSize(new Dimension(320, 28));
        txtFiltro.setToolTipText("Busque por usuario, acción, módulo o detalle del evento.");
        filaFiltro.add(txtFiltro);
        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.setPreferredSize(new Dimension(120, 30));
        btnRefrescar.addActionListener(e -> refrescarTabla());
        filaFiltro.add(btnRefrescar);
        panelNorte.add(filaFiltro, BorderLayout.SOUTH);

        tabla.setRowHeight(22);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        sorter = new TableRowSorter<>(tableModel);
        tabla.setRowSorter(sorter);

        txtFiltro.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aplicarFiltro();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aplicarFiltro();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aplicarFiltro();
            }
        });

        scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Eventos (más recientes primero)"));

        add(panelNorte, BorderLayout.NORTH);
        add(scrollTabla, BorderLayout.CENTER);
    }

    private void aplicarFiltro() {
        String t = txtFiltro.getText() != null ? txtFiltro.getText().trim().toLowerCase() : "";
        if (t.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        sorter.setRowFilter(new RowFilter<BitacoraTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends BitacoraTableModel, ? extends Integer> entry) {
                for (int i = 0; i < entry.getValueCount(); i++) {
                    Object v = entry.getValue(i);
                    if (v != null && v.toString().toLowerCase().contains(t)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void refrescarTabla() {
        tableModel.setFilas(bitacoraController.listarOrdenFechaDesc());
        TablaUtil.aplicarAnchosContenidoYRellenarPadre(scrollTabla, tabla, 8, 40, 400, new int[]{5, 6, 7});
        aplicarFiltro();
    }

    private static final class BitacoraTableModel extends AbstractTableModel {

        private final String[] columnas = {"Id", "Fecha y hora", "Id usuario", "Usuario", "Rol", "Acción", "Módulo", "Detalle", "Éxito"};
        private List<RegistroBitacora> filas = new ArrayList<>();

        void setFilas(List<RegistroBitacora> filas) {
            this.filas = filas != null ? new ArrayList<>(filas) : new ArrayList<>();
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return filas.size();
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
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Integer.class;
            }
            if (columnIndex == 8) {
                return Boolean.class;
            }
            return String.class;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            RegistroBitacora r = filas.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return r.getIdEvento();
                case 1:
                    return r.getFechaHora() != null ? r.getFechaHora().format(FMT) : "";
                case 2:
                    return r.getIdUsuario() != null ? String.valueOf(r.getIdUsuario()) : "";
                case 3:
                    return r.getUsername() != null ? r.getUsername() : "";
                case 4:
                    return r.getRol() != null ? r.getRol() : "";
                case 5:
                    return r.getAccion() != null ? r.getAccion() : "";
                case 6:
                    return r.getModulo() != null ? r.getModulo() : "";
                case 7:
                    return r.getDetalle() != null ? r.getDetalle() : "";
                case 8:
                    return r.isExito();
                default:
                    return "";
            }
        }
    }
}
