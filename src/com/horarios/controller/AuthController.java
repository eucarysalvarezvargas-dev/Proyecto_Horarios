package com.horarios.controller;

import com.horarios.model.Usuario;
import com.horarios.util.PasswordUtil;
import com.horarios.util.ValidacionSeguridad;

public class AuthController {

    private final UsuarioController usuarioController;
    private String ultimoError;

    public AuthController(UsuarioController usuarioController) {
        this.usuarioController = usuarioController;
    }

    public String getUltimoError() {
        return ultimoError;
    }

    public Usuario autenticar(String username, String password) {
        ultimoError = null;
        if (usuarioController == null) {
            ultimoError = "Servicio de usuarios no disponible.";
            return null;
        }
        String errUser = ValidacionSeguridad.validarUsernameLogin(username);
        if (errUser != null) {
            ultimoError = errUser;
            return null;
        }
        String errPwd = ValidacionSeguridad.validarPasswordLogin(password);
        if (errPwd != null) {
            ultimoError = errPwd;
            return null;
        }
        String user = username.trim();
        Usuario u = usuarioController.buscarPorUsername(user);
        if (u == null) {
            return null;
        }
        if (u.getEstado() != 1) {
            return null;
        }
        if (u.getPassword() == null) {
            return null;
        }
        return PasswordUtil.verificar(password.trim(), u.getPassword()) ? u : null;
    }
}
