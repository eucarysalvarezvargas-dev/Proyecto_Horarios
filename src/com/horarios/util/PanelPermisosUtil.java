package com.horarios.util;

import com.horarios.model.Usuario;
import javax.swing.JButton;

public final class PanelPermisosUtil {

    private PanelPermisosUtil() {
    }

    public static void aplicarBotonesCrud(Usuario usuario, String modulo,
            JButton btnRegistrar, JButton btnModificar, JButton btnEliminar) {
        boolean editar = PermisosRol.puedeEditarModulo(usuario, modulo);
        if (btnRegistrar != null) {
            btnRegistrar.setEnabled(editar);
        }
        if (btnModificar != null) {
            btnModificar.setEnabled(editar);
        }
        if (btnEliminar != null) {
            btnEliminar.setEnabled(editar);
        }
    }
}
