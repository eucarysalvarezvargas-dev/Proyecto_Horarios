package com.horarios.controller;

import com.horarios.model.RegistroBitacora;
import com.horarios.model.Usuario;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bitácora de auditoría en memoria. En la fase JDBC delegará en DAO; la vista no usa SQL.
 */
public class BitacoraController {

    /** Evita crecimiento ilimitado en sesiones largas de demostración. */
    private static final int MAX_REGISTROS_EN_MEMORIA = 5000;

    public BitacoraController() {
        this.jdbc = com.horarios.util.PersistenciaConfig.isJdbcDisponible();
        this.bitacoraDAO = jdbc ? new com.horarios.model.dao.BitacoraDAO() : null;
    }

    public static final String ACCION_LOGIN_OK = "LOGIN_OK";
    public static final String ACCION_LOGIN_FAIL = "LOGIN_FAIL";
    public static final String ACCION_LOGOUT = "LOGOUT";
    public static final String ACCION_CREATE = "CREATE";
    public static final String ACCION_UPDATE = "UPDATE";
    public static final String ACCION_DELETE_LOGIC = "DELETE_LOGIC";
    public static final String ACCION_EXPORT = "EXPORT";

    public static final String MODULO_SEGURIDAD = "SEGURIDAD";

    private final List<RegistroBitacora> registros = new ArrayList<>();
    private int siguienteId = 1;
    private final com.horarios.model.dao.BitacoraDAO bitacoraDAO;
    private final boolean jdbc;

    /**
     * Registra un evento (fecha/hora = ahora). Los más recientes quedan primero en {@link #listarOrdenFechaDesc()}.
     */
    public synchronized void registrar(Integer idUsuario, String username, String rol, String accion, String modulo,
            String detalle, boolean exito) {
        String u = username != null ? username : "";
        String r = rol != null ? rol : "";
        String d = detalle != null ? detalle : "";
        RegistroBitacora reg = new RegistroBitacora(siguienteId++, LocalDateTime.now(), idUsuario, u, r, accion, modulo, d, exito);
        registros.add(0, reg);
        while (registros.size() > MAX_REGISTROS_EN_MEMORIA) {
            registros.remove(registros.size() - 1);
        }
        if (jdbc && bitacoraDAO != null) {
            try {
                bitacoraDAO.insertar(reg);
            } catch (java.sql.SQLException ex) {
                // Mantiene copia en memoria aunque falle JDBC
            }
        }
    }

    public void registrarLoginExitoso(Usuario u) {
        if (u == null) {
            return;
        }
        registrar(u.getIdUsuario(), u.getUsername(), u.getRol(), ACCION_LOGIN_OK, MODULO_SEGURIDAD,
                "Inicio de sesión correcto.", true);
    }

    public void registrarLoginFallido(String usernameIntento, String detalle, Usuario usuarioEncontrado) {
        Integer id = usuarioEncontrado != null ? usuarioEncontrado.getIdUsuario() : null;
        String rol = usuarioEncontrado != null && usuarioEncontrado.getRol() != null ? usuarioEncontrado.getRol() : "";
        registrar(id, usernameIntento, rol, ACCION_LOGIN_FAIL, MODULO_SEGURIDAD, detalle, false);
    }

    public void registrarLogout(Usuario u) {
        if (u == null) {
            registrar(null, "(desconocido)", "", ACCION_LOGOUT, MODULO_SEGURIDAD, "Cierre de sesión.", true);
            return;
        }
        registrar(u.getIdUsuario(), u.getUsername(), u.getRol(), ACCION_LOGOUT, MODULO_SEGURIDAD,
                "Cierre de sesión.", true);
    }

    public synchronized List<RegistroBitacora> listarOrdenFechaDesc() {
        if (jdbc && bitacoraDAO != null) {
            try {
                List<RegistroBitacora> bd = bitacoraDAO.listarOrdenFechaDesc();
                if (!bd.isEmpty()) {
                    return Collections.unmodifiableList(bd);
                }
            } catch (java.sql.SQLException ex) {
                // fallback memoria
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(registros));
    }
}
