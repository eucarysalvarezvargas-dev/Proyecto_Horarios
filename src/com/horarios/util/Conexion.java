package com.horarios.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static Conexion instancia;
    private Connection conexion;

    private final String url;
    private final String usuario;
    private final String clave;

    private Conexion() {
        this.url = "jdbc:mysql://localhost:3306/db_horarios_unefa?useSSL=false&serverTimezone=UTC";
        this.usuario = "root";
        this.clave = "";
    }

    public static Conexion getInstancia() {
        if (instancia == null) {
            instancia = new Conexion();
        }
        return instancia;
    }

    /** Prueba de conexión al arrancar (sin diálogos si falla el driver o MySQL). */
    public Connection getConexionSilenciosa() {
        return obtenerConexion(false);
    }

    /** Conexión para operaciones JDBC; muestra error si no hay driver o BD. */
    public Connection getConexion() {
        return obtenerConexion(true);
    }

    private Connection obtenerConexion(boolean mostrarDialogo) {
        try {
            if (conexion == null || conexion.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conexion = DriverManager.getConnection(url, usuario, clave);
            }
        } catch (ClassNotFoundException ex) {
            conexion = null;
            if (mostrarDialogo) {
                Mensajes.error("No se encontró el Driver JDBC de MySQL.\n\n"
                        + "Agregue mysql-connector-j.jar en la carpeta lib/ del proyecto y en Libraries de NetBeans.\n"
                        + "Ver README.md sección «Conector MySQL».\n\nDetalle: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            conexion = null;
            if (mostrarDialogo) {
                Mensajes.error("No se pudo conectar a la base de datos.\n"
                        + "Verifique XAMPP/MySQL y que exista la BD db_horarios_unefa (ejecute database/db_horarios_unefa.sql).\n\n"
                        + "Detalle: " + ex.getMessage());
            }
        }
        return conexion;
    }

    public void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException ex) {
            Mensajes.error("No se pudo cerrar la conexión.\n\nDetalle: " + ex.getMessage());
        }
    }
}

