package com.horarios.model.dao;

import com.horarios.model.RegistroBitacora;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BitacoraDAO {

    public void insertar(RegistroBitacora r) throws SQLException {
        String sql = "INSERT INTO bitacora (fecha_hora, id_usuario, username, rol, accion, modulo, detalle, exito) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(r.getFechaHora()));
            if (r.getIdUsuario() != null) {
                ps.setInt(2, r.getIdUsuario());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setString(3, r.getUsername());
            ps.setString(4, r.getRol());
            ps.setString(5, r.getAccion());
            ps.setString(6, r.getModulo());
            ps.setString(7, r.getDetalle());
            ps.setInt(8, r.isExito() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    public List<RegistroBitacora> listarOrdenFechaDesc() throws SQLException {
        String sql = "SELECT * FROM bitacora ORDER BY fecha_hora DESC LIMIT 5000";
        List<RegistroBitacora> lista = new ArrayList<>();
        try (Connection cn = DaoUtil.conexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RegistroBitacora r = new RegistroBitacora();
                r.setIdEvento(rs.getInt("id_evento"));
                Timestamp ts = rs.getTimestamp("fecha_hora");
                r.setFechaHora(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
                int idU = rs.getInt("id_usuario");
                r.setIdUsuario(rs.wasNull() ? null : idU);
                r.setUsername(rs.getString("username"));
                r.setRol(rs.getString("rol"));
                r.setAccion(rs.getString("accion"));
                r.setModulo(rs.getString("modulo"));
                r.setDetalle(rs.getString("detalle"));
                r.setExito(rs.getInt("exito") == 1);
                lista.add(r);
            }
        }
        return lista;
    }
}
