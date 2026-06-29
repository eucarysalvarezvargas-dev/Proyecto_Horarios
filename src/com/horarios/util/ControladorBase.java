package com.horarios.util;

import com.horarios.controller.BitacoraController;
import com.horarios.model.Usuario;

/**
 * Contexto compartido: mensajes de validación y auditoría en memoria.
 */
public abstract class ControladorBase {

    protected String ultimoError;
    protected BitacoraController bitacora;
    protected Usuario usuarioSesion;

    public String getUltimoError() {
        return ultimoError;
    }

    protected void setUltimoError(String mensaje) {
        this.ultimoError = mensaje;
    }

    protected void limpiarError() {
        this.ultimoError = null;
    }

    public void setContextoAuditoria(BitacoraController bitacora, Usuario usuarioSesion) {
        this.bitacora = bitacora;
        this.usuarioSesion = usuarioSesion;
    }

    protected void auditarCrud(String modulo, String accion, String detalle, boolean exito) {
        if (bitacora == null) {
            return;
        }
        Integer id = usuarioSesion != null ? usuarioSesion.getIdUsuario() : null;
        String user = usuarioSesion != null ? usuarioSesion.getUsername() : "";
        String rol = usuarioSesion != null && usuarioSesion.getRol() != null ? usuarioSesion.getRol() : "";
        bitacora.registrar(id, user, rol, accion, modulo, detalle, exito);
    }
}
