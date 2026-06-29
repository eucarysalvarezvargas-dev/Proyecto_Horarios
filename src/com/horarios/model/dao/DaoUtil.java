package com.horarios.model.dao;

import com.horarios.util.Conexion;
import java.sql.Connection;
import java.sql.SQLException;

public final class DaoUtil {

    private DaoUtil() {
    }

    public static Connection conexion() throws SQLException {
        Connection c = Conexion.getInstancia().getConexion();
        if (c == null) {
            throw new SQLException("Sin conexión a MySQL. Verifique XAMPP y el script database/db_horarios_unefa.sql");
        }
        return c;
    }
}
