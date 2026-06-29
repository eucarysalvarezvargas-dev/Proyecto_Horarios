package com.horarios.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hash de contraseñas (SHA-256 + salt) sin librerías externas.
 * Formato almacenado: {@code base64(salt)}:{@code base64(hash)}.
 */
public final class PasswordUtil {

    private static final int SALT_BYTES = 16;

    private PasswordUtil() {
    }

    public static String hashPassword(String passwordPlano) {
        if (passwordPlano == null) {
            return null;
        }
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] hash = sha256(concat(salt, passwordPlano.getBytes(StandardCharsets.UTF_8)));
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean esAlmacenadoHasheado(String almacenado) {
        if (almacenado == null || !almacenado.contains(":")) {
            return false;
        }
        String[] partes = almacenado.split(":", 2);
        return partes.length == 2 && partes[0].length() >= 16 && partes[1].length() >= 16;
    }

    public static boolean verificar(String passwordPlano, String almacenado) {
        if (passwordPlano == null || almacenado == null) {
            return false;
        }
        if (!esAlmacenadoHasheado(almacenado)) {
            return false;
        }
        String[] partes = almacenado.split(":", 2);
        byte[] salt = Base64.getDecoder().decode(partes[0]);
        byte[] esperado = Base64.getDecoder().decode(partes[1]);
        byte[] calculado = sha256(concat(salt, passwordPlano.getBytes(StandardCharsets.UTF_8)));
        return MessageDigest.isEqual(esperado, calculado);
    }

    /** Hashea texto plano; si ya está hasheado, lo devuelve sin cambios. */
    public static String normalizarParaAlmacenar(String password) {
        if (password == null || password.isEmpty()) {
            return password;
        }
        if (esAlmacenadoHasheado(password)) {
            return password;
        }
        return hashPassword(password);
    }

    private static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] r = new byte[a.length + b.length];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }
}
