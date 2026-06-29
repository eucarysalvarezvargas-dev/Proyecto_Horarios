package com.horarios.controller;

import com.horarios.model.Asignatura;
import com.horarios.model.dao.AsignaturaDAO;
import com.horarios.util.ControladorBase;
import com.horarios.util.PermisosRol;
import com.horarios.util.PersistenciaConfig;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AsignaturaController extends ControladorBase {

    private final List<Asignatura> asignaturas;
    private int secuenciaId;
    private final AsignaturaDAO asignaturaDAO;
    private final boolean jdbc;

    public AsignaturaController() {
        this.jdbc = PersistenciaConfig.isJdbcDisponible();
        this.asignaturaDAO = jdbc ? new AsignaturaDAO() : null;
        this.asignaturas = jdbc ? null : new ArrayList<>();
        this.secuenciaId = 1;
    }

    public Asignatura registrar(Asignatura asignatura) {
        limpiarError();
        try {
            if (jdbc) {
                if (asignaturaDAO.buscarPorCodigo(asignatura.getCodigo()) != null) {
                    setUltimoError("El código de asignatura ya existe.");
                    auditarCrud(PermisosRol.MOD_ASIGNATURAS, BitacoraController.ACCION_CREATE, "Código duplicado.", false);
                    return null;
                }
                Asignatura c = asignaturaDAO.insertar(asignatura);
                auditarCrud(PermisosRol.MOD_ASIGNATURAS, BitacoraController.ACCION_CREATE, "Asignatura " + c.getCodigo(), true);
                return c;
            }
        } catch (SQLException ex) {
            setUltimoError(ex.getMessage());
            auditarCrud(PermisosRol.MOD_ASIGNATURAS, BitacoraController.ACCION_CREATE, ex.getMessage(), false);
            return null;
        }
        if (buscarPorCodigo(asignatura.getCodigo()) != null) {
            setUltimoError("El código de asignatura ya existe.");
            return null;
        }
        asignatura.setIdAsignatura(secuenciaId++);
        asignatura.setEstado(1);
        asignaturas.add(asignatura);
        auditarCrud(PermisosRol.MOD_ASIGNATURAS, BitacoraController.ACCION_CREATE, "Asignatura " + asignatura.getCodigo(), true);
        return asignatura;
    }

    public boolean actualizar(Asignatura asignaturaActualizada) {
        limpiarError();
        try {
            if (jdbc) {
                Asignatura otro = asignaturaDAO.buscarPorCodigo(asignaturaActualizada.getCodigo());
                if (otro != null && otro.getIdAsignatura() != asignaturaActualizada.getIdAsignatura()) {
                    setUltimoError("El código ya pertenece a otra asignatura.");
                    return false;
                }
                boolean ok = asignaturaDAO.actualizar(asignaturaActualizada);
                auditarCrud(PermisosRol.MOD_ASIGNATURAS, BitacoraController.ACCION_UPDATE,
                        "Asignatura id=" + asignaturaActualizada.getIdAsignatura(), ok);
                return ok;
            }
        } catch (SQLException ex) {
            setUltimoError(ex.getMessage());
            return false;
        }
        Asignatura existente = buscarPorId(asignaturaActualizada.getIdAsignatura());
        if (existente == null || existente.getEstado() != 1) {
            setUltimoError("Asignatura no encontrada.");
            return false;
        }
        Asignatura otro = buscarPorCodigo(asignaturaActualizada.getCodigo());
        if (otro != null && otro.getIdAsignatura() != existente.getIdAsignatura()) {
            setUltimoError("El código ya pertenece a otra asignatura.");
            return false;
        }
        existente.setCodigo(asignaturaActualizada.getCodigo());
        existente.setNombre(asignaturaActualizada.getNombre());
        existente.setHorasSemanales(asignaturaActualizada.getHorasSemanales());
        auditarCrud(PermisosRol.MOD_ASIGNATURAS, BitacoraController.ACCION_UPDATE,
                "Asignatura id=" + existente.getIdAsignatura(), true);
        return true;
    }

    public Asignatura buscarPorId(int idAsignatura) {
        try {
            if (jdbc) {
                return asignaturaDAO.buscarPorId(idAsignatura);
            }
        } catch (SQLException ex) {
            setUltimoError(ex.getMessage());
            return null;
        }
        for (Asignatura a : asignaturas) {
            if (a.getIdAsignatura() == idAsignatura && a.getEstado() == 1) {
                return a;
            }
        }
        return null;
    }

    public Asignatura buscarPorCodigo(String codigo) {
        if (codigo == null) {
            return null;
        }
        try {
            if (jdbc) {
                return asignaturaDAO.buscarPorCodigo(codigo);
            }
        } catch (SQLException ex) {
            return null;
        }
        for (Asignatura a : asignaturas) {
            if (a.getCodigo() != null && a.getCodigo().equalsIgnoreCase(codigo) && a.getEstado() == 1) {
                return a;
            }
        }
        return null;
    }

    public boolean eliminarLogico(int idAsignatura) {
        limpiarError();
        try {
            if (jdbc) {
                boolean ok = asignaturaDAO.eliminarLogico(idAsignatura);
                auditarCrud(PermisosRol.MOD_ASIGNATURAS, BitacoraController.ACCION_DELETE_LOGIC,
                        "Asignatura id=" + idAsignatura, ok);
                return ok;
            }
        } catch (SQLException ex) {
            setUltimoError(ex.getMessage());
            return false;
        }
        Asignatura existente = buscarPorId(idAsignatura);
        if (existente == null) {
            setUltimoError("Asignatura no encontrada.");
            return false;
        }
        existente.setEstado(0);
        auditarCrud(PermisosRol.MOD_ASIGNATURAS, BitacoraController.ACCION_DELETE_LOGIC,
                "Asignatura id=" + idAsignatura, true);
        return true;
    }

    public List<Asignatura> listarActivas() {
        try {
            if (jdbc) {
                return asignaturaDAO.listarActivas();
            }
        } catch (SQLException ex) {
            return new ArrayList<>();
        }
        List<Asignatura> activas = new ArrayList<>();
        for (Asignatura a : asignaturas) {
            if (a.getEstado() == 1) {
                activas.add(a);
            }
        }
        return activas;
    }
}
