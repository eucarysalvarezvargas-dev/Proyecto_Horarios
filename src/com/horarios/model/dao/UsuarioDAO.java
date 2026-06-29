package com.horarios.model.dao;

import com.horarios.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public Usuario insertar(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuarios (username, password, rol, cedula, nombres, apellidos, estado) VALUES (?,?,?,?,?,?,1)";
        try (Connection cn = DaoUtil.conexion();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getRol());
            ps.setString(4, u.getCedula());
            ps.setString(5, u.getNombres());
            ps.setString(6, u.getApellidos());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    u.setIdUsuario(rs.getInt(1));
                }
            }
            u.setEstado(1);
            return u;
        }
    }

    public boolean actualizar(Usuario u) throws SQLException {
        String sql = "UPDATE usuarios SET username=?, password=?, rol=?, cedula=?, nombres=?, apellidos=? WHERE id_usuario=? AND estado=1";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getRol());
            ps.setString(4, u.getCedula());
            ps.setString(5, u.getNombres());
            ps.setString(6, u.getApellidos());
            ps.setInt(7, u.getIdUsuario());
            return ps.executeUpdate() > 0;
        }
    }

    public Usuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT id_usuario, username, password, rol, cedula, nombres, apellidos, estado FROM usuarios WHERE id_usuario=? AND estado=1";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Usuario buscarPorUsername(String username) throws SQLException {
        String sql = "SELECT id_usuario, username, password, rol, cedula, nombres, apellidos, estado FROM usuarios WHERE LOWER(username)=LOWER(?) AND estado=1";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Usuario buscarPorUsernameInclInactivo(String username) throws SQLException {
        String sql = "SELECT id_usuario, username, password, rol, cedula, nombres, apellidos, estado FROM usuarios WHERE LOWER(username)=LOWER(?)";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public boolean eliminarLogico(int id) throws SQLException {
        String sql = "UPDATE usuarios SET estado=0 WHERE id_usuario=?";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean reactivar(int id) throws SQLException {
        String sql = "UPDATE usuarios SET estado=1 WHERE id_usuario=?";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Usuario> listarActivos() throws SQLException {
        String sql = "SELECT id_usuario, username, password, rol, cedula, nombres, apellidos, estado FROM usuarios WHERE estado=1 ORDER BY id_usuario";
        List<Usuario> lista = new ArrayList<>();
        try (Connection cn = DaoUtil.conexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Usuario> listarTodos() throws SQLException {
        String sql = "SELECT id_usuario, username, password, rol, cedula, nombres, apellidos, estado FROM usuarios ORDER BY id_usuario";
        List<Usuario> lista = new ArrayList<>();
        try (Connection cn = DaoUtil.conexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public int contarActivos() throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE estado=1";
        try (Connection cn = DaoUtil.conexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setRol(rs.getString("rol"));
        u.setCedula(rs.getString("cedula"));
        u.setNombres(rs.getString("nombres"));
        u.setApellidos(rs.getString("apellidos"));
        u.setEstado(rs.getInt("estado"));
        return u;
    }
}
