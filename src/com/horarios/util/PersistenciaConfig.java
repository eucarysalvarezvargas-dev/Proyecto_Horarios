package com.horarios.util;

import java.sql.Connection;

/**
 * Activa JDBC cuando XAMPP/MySQL responde; si no, el sistema usa listas en memoria.
 */
public final class PersistenciaConfig {

    /** Debe ser true: la aplicación requiere MySQL para iniciar. */
    public static final boolean INTENTAR_JDBC = true;

    private static Boolean jdbcDisponible;

    private PersistenciaConfig() {
    }

    public static boolean isJdbcDisponible() {
        if (!INTENTAR_JDBC) {
            return false;
        }
        if (jdbcDisponible == null) {
            jdbcDisponible = probarConexion();
        }
        return jdbcDisponible;
    }

    public static boolean probarConexion() {
        Connection c = Conexion.getInstancia().getConexionSilenciosa();
        return c != null;
    }

    public static void reiniciarCache() {
        jdbcDisponible = null;
    }
}
