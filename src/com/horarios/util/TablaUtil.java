package com.horarios.util;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Ajusta anchos según contenido y reparte el ancho sobrante del {@link JViewport} entre columnas
 * elásticas para que la tabla use todo el ancho visible (sin bandas vacías a la derecha).
 */
public final class TablaUtil {

    private static final String K_BASE = "tablaUtil.baseWidths";
    private static final String K_ELASTIC = "tablaUtil.elasticCols";
    private static final String K_VP_HOOK = "tablaUtil.viewportResizeHook";

    private TablaUtil() {
    }

    /**
     * Mide contenido (y encabezado), guarda anchos base, instala escucha de redimensionado del viewport
     * una sola vez y reparte el espacio extra entre las columnas elásticas.
     *
     * @param columnasElasticas índices de columna (0-based) que absorben el ancho extra; si es null o
     *                          vacío, solo la última columna.
     */
    public static void aplicarAnchosContenidoYRellenarPadre(
            JScrollPane scroll,
            JTable tabla,
            int margenPx,
            int anchoMinimo,
            int anchoMaximo,
            int[] columnasElasticas
    ) {
        if (scroll == null || tabla == null) {
            return;
        }
        int cols = tabla.getColumnCount();
        if (cols <= 0) {
            return;
        }
        int[] elasticas = columnasElasticas;
        if (elasticas == null || elasticas.length == 0) {
            elasticas = new int[]{cols - 1};
        }
        int[] elCopy = Arrays.copyOf(elasticas, elasticas.length);
        for (int i = 0; i < elCopy.length; i++) {
            if (elCopy[i] < 0 || elCopy[i] >= cols) {
                elCopy[i] = cols - 1;
            }
        }
        tabla.putClientProperty(K_ELASTIC, elCopy);

        medirContenidoYGuardarBases(tabla, margenPx, anchoMinimo, anchoMaximo);
        asegurarEscuchaViewport(scroll, tabla);
        rellenarAnchoExtraDesdeBase(tabla);
        SwingUtilities.invokeLater(() -> rellenarAnchoExtraDesdeBase(tabla));
    }

    private static void asegurarEscuchaViewport(JScrollPane scroll, JTable tabla) {
        if (Boolean.TRUE.equals(scroll.getClientProperty(K_VP_HOOK))) {
            return;
        }
        scroll.putClientProperty(K_VP_HOOK, Boolean.TRUE);
        scroll.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                rellenarAnchoExtraDesdeBase(tabla);
            }
        });
    }

    private static void medirContenidoYGuardarBases(JTable tabla, int margenPx, int anchoMinimo, int anchoMaximo) {
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel colModel = tabla.getColumnModel();
        TableModel model = tabla.getModel();
        int cols = model.getColumnCount();
        int rows = model.getRowCount();

        for (int col = 0; col < cols; col++) {
            TableColumn column = colModel.getColumn(col);
            int ancho = anchoMinimo;

            if (tabla.getTableHeader() != null) {
                TableCellRenderer headerRenderer = column.getHeaderRenderer();
                if (headerRenderer == null) {
                    headerRenderer = tabla.getTableHeader().getDefaultRenderer();
                }
                Object headerValue = column.getHeaderValue();
                Component hc = headerRenderer.getTableCellRendererComponent(
                        tabla, headerValue, false, false, 0, col);
                ancho = Math.max(ancho, hc.getPreferredSize().width + margenPx);
            }

            TableCellRenderer baseRenderer = column.getCellRenderer();
            if (baseRenderer == null) {
                Class<?> cc = tabla.getColumnClass(col);
                baseRenderer = tabla.getDefaultRenderer(cc != null ? cc : Object.class);
            }

            for (int row = 0; row < rows; row++) {
                Object value = model.getValueAt(row, col);
                Component c = baseRenderer.getTableCellRendererComponent(
                        tabla, value, false, false, row, col);
                ancho = Math.max(ancho, c.getPreferredSize().width + margenPx);
            }

            ancho = Math.max(anchoMinimo, Math.min(anchoMaximo, ancho));
            column.setPreferredWidth(ancho);
            column.setMinWidth(anchoMinimo);
        }

        int[] bases = new int[cols];
        for (int i = 0; i < cols; i++) {
            bases[i] = colModel.getColumn(i).getPreferredWidth();
        }
        tabla.putClientProperty(K_BASE, bases);
    }

    private static void rellenarAnchoExtraDesdeBase(JTable tabla) {
        int[] bases = (int[]) tabla.getClientProperty(K_BASE);
        int[] elasticas = (int[]) tabla.getClientProperty(K_ELASTIC);
        if (bases == null || elasticas == null || bases.length == 0) {
            return;
        }
        Component parent = tabla.getParent();
        if (!(parent instanceof JViewport)) {
            return;
        }
        JViewport vp = (JViewport) parent;
        int avail = vp.getExtentSize().width;
        if (avail <= 1) {
            return;
        }

        TableColumnModel cm = tabla.getColumnModel();
        int sum = 0;
        for (int w : bases) {
            sum += w;
        }
        int extra = avail - sum;
        for (int i = 0; i < bases.length; i++) {
            cm.getColumn(i).setPreferredWidth(bases[i]);
        }
        if (extra <= 0) {
            return;
        }
        int nEl = elasticas.length;
        int share = extra / nEl;
        int rem = extra % nEl;
        for (int j = 0; j < nEl; j++) {
            int col = elasticas[j];
            if (col < 0 || col >= bases.length) {
                continue;
            }
            int add = share + (j < rem ? 1 : 0);
            TableColumn tc = cm.getColumn(col);
            tc.setPreferredWidth(tc.getPreferredWidth() + add);
        }
    }
}
