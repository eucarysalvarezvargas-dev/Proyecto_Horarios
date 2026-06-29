package com.horarios.model.dao;

import com.horarios.model.Aula;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AulaDAO {

    public Aula insertar(Aula a) throws SQLException {
        String sql = "INSERT INTO aulas (codigo, descripcion, capacidad, estado) VALUES (?,?,?,1)";
        try (Connection cn = DaoUtil.conexion();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getCodigo());
            ps.setString(2, a.getDescripcion());
            ps.setInt(3, a.getCapacidad());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    a.setIdAula(rs.getInt(1));
                }
            }
            a.setEstado(1);
            return a;
        }
    }

    public boolean actualizar(Aula a) throws SQLException {
        String sql = "UPDATE aulas SET codigo=?, descripcion=?, capacidad=? WHERE id_aula=? AND estado=1";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, a.getCodigo());
            ps.setString(2, a.getDescripcion());
            ps.setInt(3, a.getCapacidad());
            ps.setInt(4, a.getIdAula());
            return ps.executeUpdate() > 0;
        }
    }

    public Aula buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM aulas WHERE id_aula=? AND estado=1";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Aula buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT * FROM aulas WHERE LOWER(codigo)=LOWER(?) AND estado=1";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public boolean eliminarLogico(int id) throws SQLException {
        String sql = "UPDATE aulas SET estado=0 WHERE id_aula=?";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Aula> listarActivas() throws SQLException {
        String sql = "SELECT * FROM aulas WHERE estado=1 ORDER BY id_aula";
        List<Aula> lista = new ArrayList<>();
        try (Connection cn = DaoUtil.conexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private static Aula mapear(ResultSet rs) throws SQLException {
        Aula a = new Aula();
        a.setIdAula(rs.getInt("id_aula"));
        a.setCodigo(rs.getString("codigo"));
        a.setDescripcion(rs.getString("descripcion"));
        a.setCapacidad(rs.getInt("capacidad"));
        a.setEstado(rs.getInt("estado"));
        return a;
    }
}
