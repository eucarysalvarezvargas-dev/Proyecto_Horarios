package com.horarios.util;

import com.horarios.model.Usuario;

/**
 * Permisos por rol para menú y operaciones CRUD (en memoria / UI).
 */
public final class PermisosRol {

    public static final String MOD_INICIO = "INICIO";
    public static final String MOD_DOCENTES = "DOCENTES";
    public static final String MOD_ASIGNATURAS = "ASIGNATURAS";
    public static final String MOD_AULAS = "AULAS";
    public static final String MOD_HORARIOS = "HORARIOS";
    public static final String MOD_USUARIOS = "USUARIOS";
    public static final String MOD_BITACORA = "BITACORA";
    public static final String MOD_PERFIL = "PERFIL";

    private PermisosRol() {
    }

    public static boolean puedeVerModulo(Usuario u, String modulo) {
        if (u == null || u.getRol() == null || modulo == null) {
            return false;
        }
        if (MOD_PERFIL.equals(modulo)) {
            return true;
        }
        String rol = u.getRol().trim().toUpperCase();
        if ("ADMIN".equals(rol)) {
            return true;
        }
        if ("COORDINADOR".equals(rol)) {
            return !MOD_USUARIOS.equals(modulo);
        }
        if ("DOCENTE".equals(rol)) {
            return MOD_INICIO.equals(modulo) || MOD_HORARIOS.equals(modulo);
        }
        return false;
    }

    public static boolean puedeEditarModulo(Usuario u, String modulo) {
        if (u == null || u.getRol() == null || modulo == null) {
            return false;
        }
        String rol = u.getRol().trim().toUpperCase();
        if ("ADMIN".equals(rol)) {
            return true;
        }
        if ("COORDINADOR".equals(rol)) {
            return puedeVerModulo(u, modulo) && !MOD_BITACORA.equals(modulo);
        }
        return false;
    }

    public static boolean puedeVerBitacora(Usuario u) {
        return puedeVerModulo(u, MOD_BITACORA);
    }

    /** Exportación PDF/CSV de horarios y resumen (ADMIN y COORDINADOR). */
    public static boolean puedeExportarReportes(Usuario u) {
        return puedeEditarModulo(u, MOD_HORARIOS);
    }
}
