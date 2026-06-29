package com.horarios.controller;

import com.horarios.model.Asignatura;
import com.horarios.model.Aula;
import com.horarios.model.Docente;
import com.horarios.model.Horario;
import com.horarios.model.dao.HorarioDAO;
import com.horarios.util.ControladorBase;
import com.horarios.util.PermisosRol;
import com.horarios.util.PersistenciaConfig;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reglas de negocio y motor de detección de colisiones.
 * Antes de persistir se valida integridad referencial y choques docente/aula/límite semanal por asignatura.
 */
public class HorarioController extends ControladorBase {

    private final List<Horario> horarios;
    private int secuenciaId;
    private final DocenteController docenteController;
    private final AsignaturaController asignaturaController;
    private final AulaController aulaController;
    private final HorarioDAO horarioDAO;
    private final boolean jdbc;

    public HorarioController() {
        this(null, null, null);
    }

    public HorarioController(DocenteController docenteController,
            AsignaturaController asignaturaController,
            AulaController aulaController) {
        this.jdbc = PersistenciaConfig.isJdbcDisponible();
        this.horarioDAO = jdbc ? new HorarioDAO() : null;
        this.horarios = jdbc ? null : new ArrayList<>();
        this.secuenciaId = 1;
        this.docenteController = docenteController;
        this.asignaturaController = asignaturaController;
        this.aulaController = aulaController;
    }

    public String getUltimoErrorValidacion() {
        return ultimoError != null ? ultimoError : null;
    }

    private void setUltimoErrorValidacion(String mensaje) {
        this.ultimoError = mensaje;
    }

    /**
     * @param excluirIdHorario id a ignorar (actualización); null en altas.
     * @return null si OK; mensaje descriptivo si rechazado.
     */
    public String validarHorario(Horario candidato, Integer excluirIdHorario) {
        limpiarError();
        if (candidato == null) {
            return "Datos de horario inválidos.";
        }
        if (candidato.getHoraInicio() == null || candidato.getHoraFin() == null) {
            return "Debe indicar hora de inicio y fin.";
        }
        if (!candidato.getHoraInicio().before(candidato.getHoraFin())) {
            return "La hora de inicio debe ser menor que la hora fin.";
        }
        String dia = normalizarDia(candidato.getDiaSemana());
        if (dia == null) {
            return "Día de la semana inválido. Use LUNES..DOMINGO.";
        }

        if (docenteController != null) {
            Docente d = docenteController.buscarPorId(candidato.getIdDocente());
            if (d == null || d.getEstado() != 1) {
                return "El docente indicado no existe o está inactivo.";
            }
        }
        if (asignaturaController != null) {
            Asignatura a = asignaturaController.buscarPorId(candidato.getIdAsignatura());
            if (a == null || a.getEstado() != 1) {
                return "La asignatura indicada no existe o está inactiva.";
            }
        }
        if (aulaController != null) {
            Aula au = aulaController.buscarPorId(candidato.getIdAula());
            if (au == null || au.getEstado() != 1) {
                return "El aula indicada no existe o está inactiva.";
            }
        }

        for (Horario otro : listarActivos()) {
            if (otro.getEstado() != 1) {
                continue;
            }
            if (excluirIdHorario != null && otro.getIdHorario() == excluirIdHorario) {
                continue;
            }
            String otroDia = normalizarDia(otro.getDiaSemana());
            if (otroDia == null || !dia.equals(otroDia)) {
                continue;
            }
            if (otro.getIdDocente() == candidato.getIdDocente()
                    && solapan(candidato.getHoraInicio(), candidato.getHoraFin(), otro.getHoraInicio(), otro.getHoraFin())) {
                return "Conflicto de horario: el docente ya tiene otra clase solapada ese día.";
            }
            if (otro.getIdAula() == candidato.getIdAula()
                    && solapan(candidato.getHoraInicio(), candidato.getHoraFin(), otro.getHoraInicio(), otro.getHoraFin())) {
                return "Conflicto de horario: el aula ya está ocupada en ese intervalo.";
            }
        }

        if (asignaturaController != null) {
            Asignatura asig = asignaturaController.buscarPorId(candidato.getIdAsignatura());
            if (asig != null) {
                double horasActuales = sumarHorasSemanaAsignatura(candidato.getIdAsignatura(), excluirIdHorario);
                double horasCandidato = duracionHoras(candidato.getHoraInicio(), candidato.getHoraFin());
                if (horasActuales + horasCandidato > asig.getHorasSemanales() + 1e-6) {
                    return "Conflicto de asignatura: se supera el límite de horas semanales (" + asig.getHorasSemanales() + " h).";
                }
            }
        }

        return null;
    }

    /** Compatibilidad con vista existente: true = válido. */
    public boolean validarChoques(Horario horario) {
        String err = validarHorario(horario, null);
        if (err != null) {
            setUltimoErrorValidacion(err);
            return false;
        }
        return true;
    }

    public boolean validarChoquesActualizacion(Horario horario, int idHorarioExcluir) {
        String err = validarHorario(horario, idHorarioExcluir);
        if (err != null) {
            setUltimoErrorValidacion(err);
            return false;
        }
        return true;
    }

    public Horario registrar(Horario horario) {
        String err = validarHorario(horario, null);
        if (err != null) {
            setUltimoErrorValidacion(err);
            auditarCrud(PermisosRol.MOD_HORARIOS, BitacoraController.ACCION_CREATE, err, false);
            return null;
        }
        horario.setDiaSemana(normalizarDia(horario.getDiaSemana()));
        if (horario.getFechaRegistro() == null) {
            horario.setFechaRegistro(new Timestamp(System.currentTimeMillis()));
        }
        try {
            if (jdbc) {
                Horario c = horarioDAO.insertar(horario);
                limpiarError();
                auditarCrud(PermisosRol.MOD_HORARIOS, BitacoraController.ACCION_CREATE,
                        "Horario id=" + c.getIdHorario(), true);
                return c;
            }
        } catch (SQLException ex) {
            setUltimoErrorValidacion(ex.getMessage());
            auditarCrud(PermisosRol.MOD_HORARIOS, BitacoraController.ACCION_CREATE, ex.getMessage(), false);
            return null;
        }
        horario.setIdHorario(secuenciaId++);
        horario.setEstado(1);
        horarios.add(horario);
        limpiarError();
        auditarCrud(PermisosRol.MOD_HORARIOS, BitacoraController.ACCION_CREATE,
                "Horario id=" + horario.getIdHorario(), true);
        return horario;
    }

    public boolean actualizar(Horario horarioActualizado) {
        Horario existente = buscarPorId(horarioActualizado.getIdHorario());
        if (existente == null || existente.getEstado() != 1) {
            setUltimoErrorValidacion("Horario no encontrado o inactivo.");
            return false;
        }
        String err = validarHorario(horarioActualizado, horarioActualizado.getIdHorario());
        if (err != null) {
            setUltimoErrorValidacion(err);
            auditarCrud(PermisosRol.MOD_HORARIOS, BitacoraController.ACCION_UPDATE, err, false);
            return false;
        }
        horarioActualizado.setDiaSemana(normalizarDia(horarioActualizado.getDiaSemana()));
        try {
            if (jdbc) {
                boolean ok = horarioDAO.actualizar(horarioActualizado);
                auditarCrud(PermisosRol.MOD_HORARIOS, BitacoraController.ACCION_UPDATE,
                        "Horario id=" + horarioActualizado.getIdHorario(), ok);
                limpiarError();
                return ok;
            }
        } catch (SQLException ex) {
            setUltimoErrorValidacion(ex.getMessage());
            return false;
        }
        existente.setIdDocente(horarioActualizado.getIdDocente());
        existente.setIdAsignatura(horarioActualizado.getIdAsignatura());
        existente.setIdAula(horarioActualizado.getIdAula());
        existente.setDiaSemana(horarioActualizado.getDiaSemana());
        existente.setHoraInicio(horarioActualizado.getHoraInicio());
        existente.setHoraFin(horarioActualizado.getHoraFin());
        limpiarError();
        auditarCrud(PermisosRol.MOD_HORARIOS, BitacoraController.ACCION_UPDATE,
                "Horario id=" + existente.getIdHorario(), true);
        return true;
    }

    public Horario buscarPorId(int idHorario) {
        try {
            if (jdbc) {
                return horarioDAO.buscarPorId(idHorario);
            }
        } catch (SQLException ex) {
            setUltimoErrorValidacion(ex.getMessage());
            return null;
        }
        for (Horario h : horarios) {
            if (h.getIdHorario() == idHorario) {
                return h;
            }
        }
        return null;
    }

    public boolean eliminarLogico(int idHorario) {
        try {
            if (jdbc) {
                boolean ok = horarioDAO.eliminarLogico(idHorario);
                auditarCrud(PermisosRol.MOD_HORARIOS, BitacoraController.ACCION_DELETE_LOGIC,
                        "Horario id=" + idHorario, ok);
                return ok;
            }
        } catch (SQLException ex) {
            setUltimoErrorValidacion(ex.getMessage());
            return false;
        }
        Horario existente = buscarPorId(idHorario);
        if (existente == null) {
            return false;
        }
        existente.setEstado(0);
        auditarCrud(PermisosRol.MOD_HORARIOS, BitacoraController.ACCION_DELETE_LOGIC,
                "Horario id=" + idHorario, true);
        return true;
    }

    public List<Horario> listarActivos() {
        try {
            if (jdbc) {
                return horarioDAO.listarActivos();
            }
        } catch (SQLException ex) {
            return new ArrayList<>();
        }
        List<Horario> activos = new ArrayList<>();
        for (Horario h : horarios) {
            if (h.getEstado() == 1) {
                activos.add(h);
            }
        }
        return activos;
    }

    public List<Horario> listarActivosPorDocente(int idDocente) {
        try {
            if (jdbc) {
                return horarioDAO.listarActivosPorDocente(idDocente);
            }
        } catch (SQLException ex) {
            return new ArrayList<>();
        }
        List<Horario> r = new ArrayList<>();
        for (Horario h : horarios) {
            if (h.getEstado() == 1 && h.getIdDocente() == idDocente) {
                r.add(h);
            }
        }
        return r;
    }

    public int contarHorariosActivosPorDocente(int idDocente) {
        try {
            if (jdbc) {
                return horarioDAO.contarActivosPorDocente(idDocente);
            }
        } catch (SQLException ex) {
            return listarActivosPorDocente(idDocente).size();
        }
        int n = 0;
        for (Horario h : horarios) {
            if (h.getEstado() == 1 && h.getIdDocente() == idDocente) {
                n++;
            }
        }
        return n;
    }

    public List<Horario> listarActivosPorAsignatura(int idAsignatura) {
        try {
            if (jdbc) {
                return horarioDAO.listarActivosPorAsignatura(idAsignatura);
            }
        } catch (SQLException ex) {
            return new ArrayList<>();
        }
        List<Horario> r = new ArrayList<>();
        for (Horario h : horarios) {
            if (h.getEstado() == 1 && h.getIdAsignatura() == idAsignatura) {
                r.add(h);
            }
        }
        return r;
    }

    public List<Horario> listarActivosPorDia(String diaSemana) {
        String dia = normalizarDia(diaSemana);
        if (dia == null) {
            return new ArrayList<>();
        }
        try {
            if (jdbc) {
                return horarioDAO.listarActivosPorDia(dia);
            }
        } catch (SQLException ex) {
            return new ArrayList<>();
        }
        List<Horario> r = new ArrayList<>();
        for (Horario h : horarios) {
            if (h.getEstado() == 1 && dia.equals(normalizarDia(h.getDiaSemana()))) {
                r.add(h);
            }
        }
        return r;
    }

    public List<Horario> listarActivosPorAula(int idAula) {
        try {
            if (jdbc) {
                return horarioDAO.listarActivosPorAula(idAula);
            }
        } catch (SQLException ex) {
            return new ArrayList<>();
        }
        List<Horario> r = new ArrayList<>();
        for (Horario h : horarios) {
            if (h.getEstado() == 1 && h.getIdAula() == idAula) {
                r.add(h);
            }
        }
        return r;
    }

    private static boolean solapan(Time aIni, Time aFin, Time bIni, Time bFin) {
        // Semiabierto [inicio, fin): solapan si aIni < bFin && aFin > bIni
        return aIni.before(bFin) && aFin.after(bIni);
    }

    private static double duracionHoras(Time ini, Time fin) {
        long ms = fin.getTime() - ini.getTime();
        if (ms <= 0) {
            return 0;
        }
        return ms / (1000.0 * 60.0 * 60.0);
    }

    private double sumarHorasSemanaAsignatura(int idAsignatura, Integer excluirIdHorario) {
        double total = 0;
        for (Horario h : listarActivos()) {
            if (h.getIdAsignatura() != idAsignatura) {
                continue;
            }
            if (excluirIdHorario != null && h.getIdHorario() == excluirIdHorario) {
                continue;
            }
            if (h.getHoraInicio() != null && h.getHoraFin() != null) {
                total += duracionHoras(h.getHoraInicio(), h.getHoraFin());
            }
        }
        return total;
    }

    /** Normaliza a LUNES..DOMINGO o null si no reconocido. */
    public static String normalizarDia(String dia) {
        if (dia == null) {
            return null;
        }
        String d = Normalizer.normalize(dia.trim().toUpperCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        switch (d) {
            case "LUNES":
            case "MARTES":
            case "MIERCOLES":
            case "JUEVES":
            case "VIERNES":
            case "SABADO":
            case "DOMINGO":
                return d;
            default:
                return null;
        }
    }
}
