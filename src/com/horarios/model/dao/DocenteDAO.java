package com.horarios.model.dao;

import com.horarios.model.Docente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DocenteDAO {

    public Docente insertar(Docente d) throws SQLException {
        String sql = "INSERT INTO docentes (cedula, nombres, apellidos, correo, telefono, estado) VALUES (?,?,?,?,?,1)";
        try (Connection cn = DaoUtil.conexion();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getCedula());
            ps.setString(2, d.getNombres());
            ps.setString(3, d.getApellidos());
            ps.setString(4, d.getCorreo());
            ps.setString(5, d.getTelefono());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    d.setIdDocente(rs.getInt(1));
                }
            }
            d.setEstado(1);
            return d;
        }
    }

    public boolean actualizar(Docente d) throws SQLException {
        String sql = "UPDATE docentes SET cedula=?, nombres=?, apellidos=?, correo=?, telefono=? WHERE id_docente=? AND estado=1";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, d.getCedula());
            ps.setString(2, d.getNombres());
            ps.setString(3, d.getApellidos());
            ps.setString(4, d.getCorreo());
            ps.setString(5, d.getTelefono());
            ps.setInt(6, d.getIdDocente());
            return ps.executeUpdate() > 0;
        }
    }

    public Docente buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM docentes WHERE id_docente=? AND estado=1";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Docente buscarPorCedula(String cedula) throws SQLException {
        String sql = "SELECT * FROM docentes WHERE cedula=? AND estado=1";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, cedula);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Docente buscarPorCedulaInclInactivo(String cedula) throws SQLException {
        String sql = "SELECT * FROM docentes WHERE cedula=?";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, cedula);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Docente buscarPorIdInclInactivo(int id) throws SQLException {
        String sql = "SELECT * FROM docentes WHERE id_docente=?";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public boolean reactivar(int id) throws SQLException {
        String sql = "UPDATE docentes SET estado=1 WHERE id_docente=?";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminarLogico(int id) throws SQLException {
        String sql = "UPDATE docentes SET estado=0 WHERE id_docente=?";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Docente> listarActivos() throws SQLException {
        String sql = "SELECT * FROM docentes WHERE estado=1 ORDER BY id_docente";
        List<Docente> lista = new ArrayList<>();
        try (Connection cn = DaoUtil.conexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private static Docente mapear(ResultSet rs) throws SQLException {
        Docente d = new Docente();
        d.setIdDocente(rs.getInt("id_docente"));
        d.setCedula(rs.getString("cedula"));
        d.setNombres(rs.getString("nombres"));
        d.setApellidos(rs.getString("apellidos"));
        d.setCorreo(rs.getString("correo"));
        d.setTelefono(rs.getString("telefono"));
        d.setEstado(rs.getInt("estado"));
        return d;
    }
}
