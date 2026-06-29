package com.horarios.util;

import java.util.regex.Pattern;

/**
 * Validación de entradas sensibles (login) para reducir intentos de inyección SQL y entradas malformadas.
 * La capa DAO usa PreparedStatement; esta clase es defensa adicional en la aplicación.
 */
public final class ValidacionSeguridad {

    private static final int USERNAME_MAX = 50;
    private static final int PASSWORD_MAX = 128;
    private static final Pattern USERNAME_LOGIN = Pattern.compile("^[A-Za-z0-9_]+$");

    private ValidacionSeguridad() {
    }

    /**
     * @return mensaje de error o {@code null} si el username es válido
     */
    public static String validarUsernameLogin(String username) {
        if (username == null) {
            return "Debe ingresar usuario.";
        }
        String t = username.trim();
        if (t.isEmpty()) {
            return "Debe ingresar usuario.";
        }
        if (t.length() > USERNAME_MAX) {
            return "Usuario demasiado largo (máx. " + USERNAME_MAX + " caracteres).";
        }
        if (!USERNAME_LOGIN.matcher(t).matches()) {
            return "Usuario inválido.\n\nSolo letras, números y guion bajo (_).";
        }
        if (contienePatronSqlSospechoso(t)) {
            return "Usuario inválido.\n\nCaracteres o secuencias no permitidas.";
        }
        return null;
    }

    /**
     * @return mensaje de error o {@code null} si la clave es aceptable para login
     */
    public static String validarPasswordLogin(String password) {
        if (password == null) {
            return "Debe ingresar clave.";
        }
        String t = password.trim();
        if (t.isEmpty()) {
            return "Debe ingresar clave.";
        }
        if (t.length() > PASSWORD_MAX) {
            return "Clave demasiado larga.";
        }
        if (t.indexOf('\0') >= 0) {
            return "Clave inválida.";
        }
        return null;
    }

    private static boolean contienePatronSqlSospechoso(String texto) {
        String lower = texto.toLowerCase();
        return lower.contains("'")
                || lower.contains("\"")
                || lower.contains(";")
                || lower.contains("--")
                || lower.contains("/*")
                || lower.contains("*/")
                || lower.contains(" or ")
                || lower.contains(" and ")
                || lower.contains(" union ")
                || lower.contains(" select ")
                || lower.contains(" drop ")
                || lower.contains(" insert ")
                || lower.contains(" update ")
                || lower.contains(" delete ")
                || lower.contains(" xp_");
    }
}
