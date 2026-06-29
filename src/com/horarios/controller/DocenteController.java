package com.horarios.controller;

import com.horarios.model.Docente;
import com.horarios.model.Usuario;
import com.horarios.model.dao.DocenteDAO;
import com.horarios.util.ControladorBase;
import com.horarios.util.PasswordUtil;
import com.horarios.util.PermisosRol;
import com.horarios.util.PersistenciaConfig;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DocenteController extends ControladorBase {

    private final List<Docente> docentes;
    private int secuenciaId;
    private final UsuarioController usuarioController;
    private final DocenteDAO docenteDAO;
    private final boolean jdbc;
    private HorarioController horarioController;

    public DocenteController() {
        this(null);
    }

    public DocenteController(UsuarioController usuarioController) {
        this.jdbc = PersistenciaConfig.isJdbcDisponible();
        this.docenteDAO = jdbc ? new DocenteDAO() : null;
        this.docentes = jdbc ? null : new ArrayList<>();
        this.secuenciaId = 1;
        this.usuarioController = usuarioController;
    }

    public void setHorarioController(HorarioController horarioController) {
        this.horarioController = horarioController;
    }

    /**
     * Corrige datos huérfanos: docente activo sin usuario DOCENTE activo vinculado.
     */
    public void reconciliarVinculos() {
        if (usuarioController == null) {
            return;
        }
        for (Docente d : listarActivos()) {
            if (!esDocenteVinculadoActivo(d)) {
                bajaLogicaCatalogo(d.getIdDocente());
            }
        }
    }

    public Docente registrar(Docente docente) {
        limpiarError();
        try {
            if (jdbc) {
                if (docenteDAO.buscarPorCedula(docente.getCedula()) != null) {
                    setUltimoError("La cédula ya está registrada.");
                    auditarCrud(PermisosRol.MOD_DOCENTES, BitacoraController.ACCION_CREATE, "Cédula duplicada.", false);
                    return null;
                }
                Docente creado = docenteDAO.insertar(docente);
                if (!vincularUsuarioDocente(creado)) {
                    setUltimoError(usuarioController != null ? usuarioController.getUltimoError()
                            : "No se pudo vincular el usuario DOCENTE.");
                    return null;
                }
                auditarCrud(PermisosRol.MOD_DOCENTES, BitacoraController.ACCION_CREATE,
                        "Docente cédula=" + creado.getCedula(), true);
                return creado;
            }
        } catch (SQLException ex) {
            setUltimoError(ex.getMessage());
            auditarCrud(PermisosRol.MOD_DOCENTES, BitacoraController.ACCION_CREATE, ex.getMessage(), false);
            return null;
        }
        if (buscarPorCedula(docente.getCedula()) != null) {
            setUltimoError("La cédula ya está registrada.");
            return null;
        }
        docente.setIdDocente(secuenciaId++);
        docente.setEstado(1);
        docentes.add(docente);
        if (!vincularUsuarioDocente(docente)) {
            docentes.remove(docente);
            return null;
        }
        auditarCrud(PermisosRol.MOD_DOCENTES, BitacoraController.ACCION_CREATE,
                "Docente cédula=" + docente.getCedula(), true);
        return docente;
    }

    private boolean vincularUsuarioDocente(Docente docente) {
        if (usuarioController == null || docente == null) {
            return true;
        }
        String cedula = docente.getCedula();
        Usuario activo = usuarioController.buscarPorUsername(cedula);
        if (activo != null) {
            Usuario act = copiarUsuario(activo);
            act.setRol("DOCENTE");
            act.setCedula(cedula);
            act.setNombres(docente.getNombres());
            act.setApellidos(docente.getApellidos());
            return usuarioController.actualizarSinVinculo(act);
        }
        Usuario inactivo = usuarioController.buscarPorUsernameInclInactivo(cedula);
        if (inactivo != null) {
            return usuarioController.reactivarComoDocente(inactivo, docente);
        }
        Usuario u = new Usuario();
        u.setUsername(cedula);
        u.setPassword(PasswordUtil.hashPassword(cedula));
        u.setRol("DOCENTE");
        u.setEstado(1);
        u.setCedula(cedula);
        u.setNombres(docente.getNombres());
        u.setApellidos(docente.getApellidos());
        return usuarioController.registrar(u) != null;
    }

    private static Usuario copiarUsuario(Usuario u) {
        Usuario c = new Usuario();
        c.setIdUsuario(u.getIdUsuario());
        c.setUsername(u.getUsername());
        c.setPassword(u.getPassword());
        c.setRol(u.getRol());
        c.setCedula(u.getCedula());
        c.setNombres(u.getNombres());
        c.setApellidos(u.getApellidos());
        c.setEstado(u.getEstado());
        return c;
    }

    public boolean actualizar(Docente docenteActualizado) {
        limpiarError();
        Docente existente = buscarPorId(docenteActualizado.getIdDocente());
        if (existente == null || existente.getEstado() != 1) {
            setUltimoError("Docente no encontrado o inactivo.");
            return false;
        }
        if (docenteActualizado.getCedula() != null && existente.getCedula() != null
                && !existente.getCedula().equals(docenteActualizado.getCedula().trim())) {
            setUltimoError("No se puede cambiar la cédula del docente.\n\nRegistre uno nuevo y elimine este si corresponde.");
            return false;
        }
        try {
            if (jdbc) {
                boolean ok = docenteDAO.actualizar(docenteActualizado);
                if (ok) {
                    sincronizarUsuarioDesdeDocente(docenteActualizado);
                }
                auditarCrud(PermisosRol.MOD_DOCENTES, BitacoraController.ACCION_UPDATE,
                        "Docente id=" + docenteActualizado.getIdDocente(), ok);
                return ok;
            }
        } catch (SQLException ex) {
            setUltimoError(ex.getMessage());
            auditarCrud(PermisosRol.MOD_DOCENTES, BitacoraController.ACCION_UPDATE, ex.getMessage(), false);
            return false;
        }
        existente.setNombres(docenteActualizado.getNombres());
        existente.setApellidos(docenteActualizado.getApellidos());
        existente.setCorreo(docenteActualizado.getCorreo());
        existente.setTelefono(docenteActualizado.getTelefono());
        sincronizarUsuarioDesdeDocente(existente);
        auditarCrud(PermisosRol.MOD_DOCENTES, BitacoraController.ACCION_UPDATE,
                "Docente id=" + existente.getIdDocente(), true);
        return true;
    }

    private void sincronizarUsuarioDesdeDocente(Docente docente) {
        if (usuarioController == null || docente == null || docente.getCedula() == null) {
            return;
        }
        Usuario u = usuarioController.buscarPorUsername(docente.getCedula());
        if (u == null || !"DOCENTE".equalsIgnoreCase(u.getRol())) {
            return;
        }
        Usuario act = copiarUsuario(u);
        act.setNombres(docente.getNombres());
        act.setApellidos(docente.getApellidos());
        act.setCedula(docente.getCedula());
        usuarioController.actualizarSinVinculo(act);
    }

    public Docente buscarPorId(int idDocente) {
        try {
            if (jdbc) {
                return docenteDAO.buscarPorId(idDocente);
            }
        } catch (SQLException ex) {
            setUltimoError(ex.getMessage());
            return null;
        }
        for (Docente d : docentes) {
            if (d.getIdDocente() == idDocente && d.getEstado() == 1) {
                return d;
            }
        }
        return null;
    }

    public Docente buscarPorCedula(String cedula) {
        if (cedula == null) {
            return null;
        }
        try {
            if (jdbc) {
                return docenteDAO.buscarPorCedula(cedula);
            }
        } catch (SQLException ex) {
            setUltimoError(ex.getMessage());
            return null;
        }
        for (Docente d : docentes) {
            if (d.getCedula() != null && d.getCedula().equals(cedula) && d.getEstado() == 1) {
                return d;
            }
        }
        return null;
    }

    public Docente buscarPorCedulaInclInactivo(String cedula) {
        if (cedula == null) {
            return null;
        }
        try {
            if (jdbc) {
                return docenteDAO.buscarPorCedulaInclInactivo(cedula);
            }
        } catch (SQLException ex) {
            return null;
        }
        for (Docente d : docentes) {
            if (d.getCedula() != null && d.getCedula().equals(cedula)) {
                return d;
            }
        }
        return null;
    }

    /** Docentes activos cuyo usuario vinculado sigue con rol DOCENTE (para horarios). */
    public List<Docente> listarActivosParaHorarios() {
        List<Docente> activos = listarActivos();
        if (usuarioController == null) {
            return activos;
        }
        List<Docente> filtrados = new ArrayList<>();
        for (Docente d : activos) {
            if (esDocenteVinculadoActivo(d)) {
                filtrados.add(d);
            }
        }
        return filtrados;
    }

    private boolean esDocenteVinculadoActivo(Docente d) {
        if (d == null || d.getCedula() == null || usuarioController == null) {
            return false;
        }
        Usuario u = usuarioController.buscarPorUsername(d.getCedula());
        return u != null && u.getEstado() == 1 && "DOCENTE".equalsIgnoreCase(u.getRol());
    }

    /**
     * Baja lógica solo en catálogo docentes (sin tocar usuario). Usado al cambiar rol en Usuarios.
     */
    public boolean bajaLogicaCatalogo(int idDocente) {
        limpiarError();
        try {
            if (jdbc) {
                return docenteDAO.eliminarLogico(idDocente);
            }
        } catch (SQLException ex) {
            setUltimoError(ex.getMessage());
            return false;
        }
        for (Docente d : docentes) {
            if (d.getIdDocente() == idDocente) {
                d.setEstado(0);
                return true;
            }
        }
        return false;
    }

    public boolean bajaLogicaCatalogoPorCedula(String cedula) {
        Docente d = buscarPorCedula(cedula);
        if (d == null) {
            return true;
        }
        return bajaLogicaCatalogo(d.getIdDocente());
    }

    public boolean tieneHorariosActivos(int idDocente) {
        if (horarioController == null || idDocente <= 0) {
            return false;
        }
        return horarioController.contarHorariosActivosPorDocente(idDocente) > 0;
    }

    public boolean eliminarLogico(int idDocente) {
        limpiarError();
        try {
            if (jdbc) {
                Docente d = docenteDAO.buscarPorId(idDocente);
                boolean ok = docenteDAO.eliminarLogico(idDocente);
                if (ok && d != null && usuarioController != null) {
                    Usuario u = usuarioController.buscarPorUsername(d.getCedula());
                    if (u != null) {
                        usuarioController.eliminarLogicoSinVinculo(u.getIdUsuario());
                    }
                }
                auditarCrud(PermisosRol.MOD_DOCENTES, BitacoraController.ACCION_DELETE_LOGIC,
                        "Docente id=" + idDocente, ok);
                return ok;
            }
        } catch (SQLException ex) {
            setUltimoError(ex.getMessage());
            auditarCrud(PermisosRol.MOD_DOCENTES, BitacoraController.ACCION_DELETE_LOGIC, ex.getMessage(), false);
            return false;
        }
        Docente existente = buscarPorId(idDocente);
        if (existente == null) {
            setUltimoError("Docente no encontrado.");
            return false;
        }
        existente.setEstado(0);
        if (usuarioController != null && existente.getCedula() != null) {
            Usuario u = usuarioController.buscarPorUsername(existente.getCedula());
            if (u != null) {
                usuarioController.eliminarLogicoSinVinculo(u.getIdUsuario());
            }
        }
        auditarCrud(PermisosRol.MOD_DOCENTES, BitacoraController.ACCION_DELETE_LOGIC,
                "Docente id=" + idDocente, true);
        return true;
    }

    public List<Docente> listarActivos() {
        try {
            if (jdbc) {
                return docenteDAO.listarActivos();
            }
        } catch (SQLException ex) {
            setUltimoError(ex.getMessage());
            return new ArrayList<>();
        }
        List<Docente> activos = new ArrayList<>();
        for (Docente d : docentes) {
            if (d.getEstado() == 1) {
                activos.add(d);
            }
        }
        return activos;
    }
}
