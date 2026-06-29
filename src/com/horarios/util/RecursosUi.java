package com.horarios.util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Carga imágenes desde la carpeta {@code Public} en la raíz del proyecto.
 * <p>
 * Para cambiar el tamaño de los logos, edita las constantes {@link #LOGO_LOGIN_ANCHO_MAX},
 * {@link #LOGO_LOGIN_ALTO_MAX}, {@link #LOGO_PANEL_ANCHO_MAX} y {@link #LOGO_PANEL_ALTO_MAX}.
 */
public final class RecursosUi {

    public static final String CARPETA_PUBLIC = "Public";
    public static final String ARCHIVO_LOGO_LOGIN = "Logo 1 login.png";
    public static final String ARCHIVO_LOGO_PANEL = "Logo 2 Panel.png";

    /** Ancho máximo del logo en el diálogo de inicio de sesión (px). */
    public static final int LOGO_LOGIN_ANCHO_MAX = 400;
    /** Alto máximo del logo en el diálogo de inicio de sesión (px). */
    public static final int LOGO_LOGIN_ALTO_MAX = 200;

    /** Ancho máximo del logo en la barra lateral (px). */
    public static final int LOGO_PANEL_ANCHO_MAX = 200;
    /** Alto máximo del logo en la barra lateral (px). */
    public static final int LOGO_PANEL_ALTO_MAX = 160;

    private RecursosUi() {
    }

    public static JLabel crearEtiquetaLogoLogin() {
        return crearEtiquetaLogo(ARCHIVO_LOGO_LOGIN, LOGO_LOGIN_ANCHO_MAX, LOGO_LOGIN_ALTO_MAX);
    }

    public static JLabel crearEtiquetaLogoPanel() {
        return crearEtiquetaLogo(ARCHIVO_LOGO_PANEL, LOGO_PANEL_ANCHO_MAX, LOGO_PANEL_ALTO_MAX);
    }

    public static JLabel crearEtiquetaLogo(String nombreArchivo, int anchoMaxPx, int altoMaxPx) {
        JLabel lbl = new JLabel();
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        ImageIcon icono = cargarIconoEscalado(nombreArchivo, anchoMaxPx, altoMaxPx);
        if (icono != null) {
            lbl.setIcon(icono);
        }
        return lbl;
    }

    public static ImageIcon cargarIconoEscalado(String nombreArchivo, int anchoMaxPx, int altoMaxPx) {
        File archivo = resolverArchivoPublico(nombreArchivo);
        if (archivo == null || !archivo.isFile()) {
            return null;
        }
        BufferedImage original;
        try {
            original = ImageIO.read(archivo);
        } catch (IOException ex) {
            return null;
        }
        if (original == null) {
            return null;
        }

        int w = original.getWidth();
        int h = original.getHeight();
        if (w <= 0 || h <= 0) {
            return new ImageIcon(original);
        }

        double escala = 1.0;
        if (anchoMaxPx > 0) {
            escala = Math.min(escala, (double) anchoMaxPx / w);
        }
        if (altoMaxPx > 0) {
            escala = Math.min(escala, (double) altoMaxPx / h);
        }
        if (escala >= 1.0) {
            return new ImageIcon(original);
        }

        int nw = Math.max(1, (int) Math.round(w * escala));
        int nh = Math.max(1, (int) Math.round(h * escala));
        Image img = original.getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    private static File resolverArchivoPublico(String nombreArchivo) {
        String relativo = CARPETA_PUBLIC + File.separator + nombreArchivo;
        String userDir = System.getProperty("user.dir");
        if (userDir != null) {
            File desdeUserDir = buscarSubiendo(new File(userDir), relativo);
            if (desdeUserDir != null) {
                return desdeUserDir;
            }
        }
        File directo = new File(relativo);
        if (directo.isFile()) {
            return directo;
        }
        return null;
    }

    private static File buscarSubiendo(File inicio, String rutaRelativa) {
        File dir = inicio;
        for (int i = 0; i < 6 && dir != null; i++) {
            File candidato = new File(dir, rutaRelativa);
            if (candidato.isFile()) {
                return candidato;
            }
            dir = dir.getParentFile();
        }
        return null;
    }
}
