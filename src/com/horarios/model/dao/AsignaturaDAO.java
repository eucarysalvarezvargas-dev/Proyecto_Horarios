package com.horarios.model.dao;

import com.horarios.model.Asignatura;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AsignaturaDAO {

    public Asignatura insertar(Asignatura a) throws SQLException {
        String sql = "INSERT INTO asignaturas (codigo, nombre, horas_semanales, estado) VALUES (?,?,?,1)";
        try (Connection cn = DaoUtil.conexion();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getCodigo());
            ps.setString(2, a.getNombre());
            ps.setInt(3, a.getHorasSemanales());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    a.setIdAsignatura(rs.getInt(1));
                }
            }
            a.setEstado(1);
            return a;
        }
    }

    public boolean actualizar(Asignatura a) throws SQLException {
        String sql = "UPDATE asignaturas SET codigo=?, nombre=?, horas_semanales=? WHERE id_asignatura=? AND estado=1";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, a.getCodigo());
            ps.setString(2, a.getNombre());
            ps.setInt(3, a.getHorasSemanales());
            ps.setInt(4, a.getIdAsignatura());
            return ps.executeUpdate() > 0;
        }
    }

    public Asignatura buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM asignaturas WHERE id_asignatura=? AND estado=1";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Asignatura buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT * FROM asignaturas WHERE LOWER(codigo)=LOWER(?) AND estado=1";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public boolean eliminarLogico(int id) throws SQLException {
        String sql = "UPDATE asignaturas SET estado=0 WHERE id_asignatura=?";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Asignatura> listarActivas() throws SQLException {
        String sql = "SELECT * FROM asignaturas WHERE estado=1 ORDER BY id_asignatura";
        List<Asignatura> lista = new ArrayList<>();
        try (Connection cn = DaoUtil.conexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private static Asignatura mapear(ResultSet rs) throws SQLException {
        Asignatura a = new Asignatura();
        a.setIdAsignatura(rs.getInt("id_asignatura"));
        a.setCodigo(rs.getString("codigo"));
        a.setNombre(rs.getString("nombre"));
        a.setHorasSemanales(rs.getInt("horas_semanales"));
        a.setEstado(rs.getInt("estado"));
        return a;
    }
}
