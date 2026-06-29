package com.horarios.model.dao;

import com.horarios.model.Horario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class HorarioDAO {

    public Horario insertar(Horario h) throws SQLException {
        String sql = "INSERT INTO horarios (id_docente, id_asignatura, id_aula, dia_semana, hora_inicio, hora_fin, usuario_creador, fecha_registro, estado) VALUES (?,?,?,?,?,?,?,?,1)";
        try (Connection cn = DaoUtil.conexion();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, h.getIdDocente());
            ps.setInt(2, h.getIdAsignatura());
            ps.setInt(3, h.getIdAula());
            ps.setString(4, h.getDiaSemana());
            ps.setTime(5, h.getHoraInicio());
            ps.setTime(6, h.getHoraFin());
            ps.setInt(7, h.getUsuarioCreador());
            ps.setTimestamp(8, h.getFechaRegistro() != null ? h.getFechaRegistro() : new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    h.setIdHorario(rs.getInt(1));
                }
            }
            h.setEstado(1);
            return h;
        }
    }

    public boolean actualizar(Horario h) throws SQLException {
        String sql = "UPDATE horarios SET id_docente=?, id_asignatura=?, id_aula=?, dia_semana=?, hora_inicio=?, hora_fin=? WHERE id_horario=? AND estado=1";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, h.getIdDocente());
            ps.setInt(2, h.getIdAsignatura());
            ps.setInt(3, h.getIdAula());
            ps.setString(4, h.getDiaSemana());
            ps.setTime(5, h.getHoraInicio());
            ps.setTime(6, h.getHoraFin());
            ps.setInt(7, h.getIdHorario());
            return ps.executeUpdate() > 0;
        }
    }

    public Horario buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM horarios WHERE id_horario=? AND estado=1";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public boolean eliminarLogico(int id) throws SQLException {
        String sql = "UPDATE horarios SET estado=0 WHERE id_horario=?";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Horario> listarActivos() throws SQLException {
        return listarConFiltro("SELECT * FROM horarios WHERE estado=1 ORDER BY id_horario");
    }

    public List<Horario> listarActivosPorDocente(int idDocente) throws SQLException {
        return listarConFiltro("SELECT * FROM horarios WHERE estado=1 AND id_docente=" + idDocente);
    }

    public int contarActivosPorDocente(int idDocente) throws SQLException {
        String sql = "SELECT COUNT(*) FROM horarios WHERE estado=1 AND id_docente=?";
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idDocente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public List<Horario> listarActivosPorAsignatura(int idAsignatura) throws SQLException {
        return listarConFiltro("SELECT * FROM horarios WHERE estado=1 AND id_asignatura=" + idAsignatura);
    }

    public List<Horario> listarActivosPorAula(int idAula) throws SQLException {
        return listarConFiltro("SELECT * FROM horarios WHERE estado=1 AND id_aula=" + idAula);
    }

    public List<Horario> listarActivosPorDia(String dia) throws SQLException {
        String sql = "SELECT * FROM horarios WHERE estado=1 AND UPPER(dia_semana)=UPPER(?)";
        List<Horario> lista = new ArrayList<>();
        try (Connection cn = DaoUtil.conexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, dia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private List<Horario> listarConFiltro(String sql) throws SQLException {
        List<Horario> lista = new ArrayList<>();
        try (Connection cn = DaoUtil.conexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private static Horario mapear(ResultSet rs) throws SQLException {
        Horario h = new Horario();
        h.setIdHorario(rs.getInt("id_horario"));
        h.setIdDocente(rs.getInt("id_docente"));
        h.setIdAsignatura(rs.getInt("id_asignatura"));
        h.setIdAula(rs.getInt("id_aula"));
        h.setDiaSemana(rs.getString("dia_semana"));
        h.setHoraInicio(rs.getTime("hora_inicio"));
        h.setHoraFin(rs.getTime("hora_fin"));
        h.setUsuarioCreador(rs.getInt("usuario_creador"));
        h.setFechaRegistro(rs.getTimestamp("fecha_registro"));
        h.setEstado(rs.getInt("estado"));
        return h;
    }
}
