package com.horarios.controller;

import com.horarios.model.Aula;
import com.horarios.model.dao.AulaDAO;
import com.horarios.util.ControladorBase;
import com.horarios.util.PermisosRol;
import com.horarios.util.PersistenciaConfig;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AulaController extends ControladorBase {

    private final List<Aula> aulas;
    private int secuenciaId;
    private final AulaDAO aulaDAO;
    private final boolean jdbc;

    public AulaController() {
        this.jdbc = PersistenciaConfig.isJdbcDisponible();
        this.aulaDAO = jdbc ? new AulaDAO() : null;
        this.aulas = jdbc ? null : new ArrayList<>();
        this.secuenciaId = 1;
    }

    public Aula registrar(Aula aula) {
        limpiarError();
        try {
            if (jdbc) {
                if (aulaDAO.buscarPorCodigo(aula.getCodigo()) != null) {
                    setUltimoError("El código de aula ya existe.");
                    auditarCrud(PermisosRol.MOD_AULAS, BitacoraController.ACCION_CREATE, "Código duplicado.", false);
                    return null;
                }
                Aula c = aulaDAO.insertar(aula);
                auditarCrud(PermisosRol.MOD_AULAS, BitacoraController.ACCION_CREATE, "Aula " + c.getCodigo(), true);
                return c;
            }
        } catch (SQLException ex) {
            setUltimoError(ex.getMessage());
            return null;
        }
        if (buscarPorCodigo(aula.getCodigo()) != null) {
            setUltimoError("El código de aula ya existe.");
            return null;
        }
        aula.setIdAula(secuenciaId++);
        aula.setEstado(1);
        aulas.add(aula);
        auditarCrud(PermisosRol.MOD_AULAS, BitacoraController.ACCION_CREATE, "Aula " + aula.getCodigo(), true);
        return aula;
    }

    public boolean actualizar(Aula aulaActualizada) {
        limpiarError();
        try {
            if (jdbc) {
                Aula otro = aulaDAO.buscarPorCodigo(aulaActualizada.getCodigo());
                if (otro != null && otro.getIdAula() != aulaActualizada.getIdAula()) {
                    setUltimoError("El código ya pertenece a otra aula.");
                    return false;
                }
                boolean ok = aulaDAO.actualizar(aulaActualizada);
                auditarCrud(PermisosRol.MOD_AULAS, BitacoraController.ACCION_UPDATE,
                        "Aula id=" + aulaActualizada.getIdAula(), ok);
                return ok;
            }
        } catch (SQLException ex) {
            setUltimoError(ex.getMessage());
            return false;
        }
        Aula existente = buscarPorId(aulaActualizada.getIdAula());
        if (existente == null || existente.getEstado() != 1) {
            setUltimoError("Aula no encontrada.");
            return false;
        }
        Aula otro = buscarPorCodigo(aulaActualizada.getCodigo());
        if (otro != null && otro.getIdAula() != existente.getIdAula()) {
            setUltimoError("El código ya pertenece a otra aula.");
            return false;
        }
        existente.setCodigo(aulaActualizada.getCodigo());
        existente.setDescripcion(aulaActualizada.getDescripcion());
        existente.setCapacidad(aulaActualizada.getCapacidad());
        auditarCrud(PermisosRol.MOD_AULAS, BitacoraController.ACCION_UPDATE, "Aula id=" + existente.getIdAula(), true);
        return true;
    }

    public Aula buscarPorId(int idAula) {
        try {
            if (jdbc) {
                return aulaDAO.buscarPorId(idAula);
            }
        } catch (SQLException ex) {
            return null;
        }
        for (Aula a : aulas) {
            if (a.getIdAula() == idAula && a.getEstado() == 1) {
                return a;
            }
        }
        return null;
    }

    public Aula buscarPorCodigo(String codigo) {
        if (codigo == null) {
            return null;
        }
        try {
            if (jdbc) {
                return aulaDAO.buscarPorCodigo(codigo);
            }
        } catch (SQLException ex) {
            return null;
        }
        for (Aula a : aulas) {
            if (a.getCodigo() != null && a.getCodigo().equalsIgnoreCase(codigo) && a.getEstado() == 1) {
                return a;
            }
        }
        return null;
    }

    public boolean eliminarLogico(int idAula) {
        limpiarError();
        try {
            if (jdbc) {
                boolean ok = aulaDAO.eliminarLogico(idAula);
                auditarCrud(PermisosRol.MOD_AULAS, BitacoraController.ACCION_DELETE_LOGIC, "Aula id=" + idAula, ok);
                return ok;
            }
        } catch (SQLException ex) {
            setUltimoError(ex.getMessage());
            return false;
        }
        Aula existente = buscarPorId(idAula);
        if (existente == null) {
            setUltimoError("Aula no encontrada.");
            return false;
        }
        existente.setEstado(0);
        auditarCrud(PermisosRol.MOD_AULAS, BitacoraController.ACCION_DELETE_LOGIC, "Aula id=" + idAula, true);
        return true;
    }

    public List<Aula> listarActivas() {
        try {
            if (jdbc) {
                return aulaDAO.listarActivas();
            }
        } catch (SQLException ex) {
            return new ArrayList<>();
        }
        List<Aula> activas = new ArrayList<>();
        for (Aula a : aulas) {
            if (a.getEstado() == 1) {
                activas.add(a);
            }
        }
        return activas;
    }
}
