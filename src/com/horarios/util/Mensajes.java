package com.horarios.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.util.regex.Pattern;

public class Mensajes {

    private Mensajes() {
    }

    private static final Pattern SOLO_NUMEROS = Pattern.compile("^\\d+$");
    private static final Pattern CEDULA_VALIDA = Pattern.compile("^\\d{8}$");
    private static final Pattern SOLO_LETRAS_ESPACIOS = Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$");
    private static final Pattern CORREO_UNEFA = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.com$");

    public static void error(String mensaje) {
        JOptionPane.showMessageDialog(null,
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void info(String mensaje) {
        JOptionPane.showMessageDialog(null,
                mensaje,
                "Información",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static int confirmar(String mensaje) {
        return JOptionPane.showConfirmDialog(
                null,
                mensaje,
                "Confirmación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
    }

    public static String pedirTextoNoVacio(String prompt) {
        String data;
        boolean valido;
        do {
            valido = true;
            data = JOptionPane.showInputDialog(null, prompt, "Datos requeridos", JOptionPane.QUESTION_MESSAGE);
            if (data == null) {
                error("Operación cancelada.");
                valido = false;
                continue;
            }
            data = data.trim();
            if (data.isEmpty()) {
                error("Campo obligatorio.\n\nIngrese un valor válido.");
                valido = false;
            }
        } while (!valido);
        return data;
    }

    public static String pedirTextoNoVacioCancelable(String prompt) {
        String data;
        boolean valido;
        do {
            valido = true;
            data = JOptionPane.showInputDialog(null, prompt, "Datos requeridos", JOptionPane.QUESTION_MESSAGE);
            if (data == null) {
                return null;
            }
            data = data.trim();
            if (data.isEmpty()) {
                error("Campo obligatorio.\n\nIngrese un valor válido.");
                valido = false;
            }
        } while (!valido);
        return data;
    }

    public static String pedirPasswordNoVacio(String prompt) {
        String data = "";
        boolean valido;
        do {
            valido = true;
            JPasswordField pf = new JPasswordField();
            Object[] contenido = new Object[]{prompt, pf};

            int opcion = JOptionPane.showConfirmDialog(
                    null,
                    contenido,
                    "Datos requeridos",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (opcion != JOptionPane.OK_OPTION) {
                valido = false;
                continue;
            }

            data = new String(pf.getPassword()).trim();
            if (data.isEmpty()) {
                error("Campo obligatorio.\n\nIngrese un valor válido.");
                valido = false;
            }
        } while (!valido);
        return data;
    }

    public static String pedirPasswordNoVacioCancelable(String prompt) {
        String data = "";
        boolean valido;
        do {
            valido = true;
            JPasswordField pf = new JPasswordField();
            Object[] contenido = new Object[]{prompt, pf};

            int opcion = JOptionPane.showConfirmDialog(
                    null,
                    contenido,
                    "Datos requeridos",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (opcion != JOptionPane.OK_OPTION) {
                return null;
            }

            data = new String(pf.getPassword()).trim();
            if (data.isEmpty()) {
                error("Campo obligatorio.\n\nIngrese un valor válido.");
                valido = false;
            }
        } while (!valido);
        return data;
    }

    public static String[] pedirCredencialesCancelable() {
        JTextField tfUsuario = new JTextField();
        JPasswordField pfClave = new JPasswordField();
        JCheckBox chkMostrar = new JCheckBox("Mostrar contraseña");

        Font fontBase = UIManager.getFont("Label.font");
        Font fontSub = new Font("SansSerif", Font.PLAIN, 12);
        Font fontCampo = (fontBase != null) ? fontBase.deriveFont(Font.PLAIN, 13f) : new Font("SansSerif", Font.PLAIN, 13);

        tfUsuario.setFont(fontCampo);
        pfClave.setFont(fontCampo);

        Color borde = new Color(210, 210, 210);
        Border campoBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borde),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        );
        tfUsuario.setBorder(campoBorder);
        pfClave.setBorder(campoBorder);
        tfUsuario.setPreferredSize(new Dimension(260, 34));
        pfClave.setPreferredSize(new Dimension(260, 34));

        char echoOriginal = pfClave.getEchoChar();
        chkMostrar.setOpaque(false);
        chkMostrar.setFont(fontSub);
        chkMostrar.addActionListener((ActionEvent e) -> {
            pfClave.setEchoChar(chkMostrar.isSelected() ? (char) 0 : echoOriginal);
        });

        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setOpaque(false);
        JLabel lblLogo = RecursosUi.crearEtiquetaLogoLogin();
        JLabel lblSub = new JLabel("Ingrese sus credenciales para acceder al sistema.", SwingConstants.CENTER);
        lblSub.setFont(fontSub);
        lblSub.setForeground(new Color(90, 90, 90));
        header.add(lblLogo, BorderLayout.NORTH);
        header.add(lblSub, BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 0, 0, 0);

        JLabel lUsuario = new JLabel("Usuario");
        lUsuario.setFont(fontSub);
        JLabel lClave = new JLabel("Contraseña");
        lClave.setFont(fontSub);

        gc.gridy = 0;
        form.add(lUsuario, gc);
        gc.gridy = 1;
        form.add(tfUsuario, gc);
        gc.gridy = 2;
        form.add(lClave, gc);
        gc.gridy = 3;
        form.add(pfClave, gc);
        gc.gridy = 4;
        gc.insets = new Insets(6, 0, 0, 0);
        form.add(chkMostrar, gc);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));
        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);

        String[] opciones = new String[]{"Iniciar sesión", "Cancelar"};
        JOptionPane pane = new JOptionPane(
                root,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION,
                null,
                opciones,
                opciones[0]
        );

        JDialog dialog = pane.createDialog(null, "Inicio de sesión");
        dialog.setModal(true);
        dialog.setResizable(false);

        // Enter para confirmar, Esc para cancelar (sin romper el look)
        dialog.getRootPane().setDefaultButton(findButton(dialog, "Iniciar sesión"));
        dialog.getRootPane().registerKeyboardAction(e -> pane.setValue(opciones[1]),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        SwingUtilities.invokeLater(tfUsuario::requestFocusInWindow);
        dialog.setVisible(true);
        dialog.dispose();

        Object value = pane.getValue();
        if (value == null || value.equals(opciones[1])) {
            return null;
        }

        String usuario = tfUsuario.getText() != null ? tfUsuario.getText().trim() : "";
        String clave = new String(pfClave.getPassword()).trim();
        return new String[]{usuario, clave};
    }

    private static JButton findButton(Component parent, String text) {
        if (parent instanceof JButton) {
            JButton b = (JButton) parent;
            if (text.equals(b.getText())) {
                return b;
            }
        }
        if (parent instanceof java.awt.Container) {
            for (Component c : ((java.awt.Container) parent).getComponents()) {
                JButton found = findButton(c, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    public static int pedirEnteroPositivoEnRango(String prompt, int min, int max) {
        int valor = 0;
        boolean valido;
        do {
            valido = true;
            String data = JOptionPane.showInputDialog(null, prompt, "Datos requeridos", JOptionPane.QUESTION_MESSAGE);
            if (data == null) {
                error("Operación cancelada.");
                valido = false;
                continue;
            }
            data = data.trim();
            try {
                valor = Integer.parseInt(data);
                if (valor < min || valor > max) {
                    error("Ingrese un número entero entre " + min + " y " + max + ".");
                    valido = false;
                }
            } catch (NumberFormatException ex) {
                error("Valor incorrecto.\n\nDebe ingresar un valor numérico entero positivo entre [" + min + "-" + max + "].");
                valido = false;
            }
        } while (!valido);
        return valor;
    }

    public static Integer pedirEnteroPositivoEnRangoCancelable(String prompt, int min, int max) {
        Integer valor = null;
        boolean valido;
        do {
            valido = true;
            String data = JOptionPane.showInputDialog(null, prompt, "Datos requeridos", JOptionPane.QUESTION_MESSAGE);
            if (data == null) {
                return null;
            }
            data = data.trim();
            try {
                int v = Integer.parseInt(data);
                if (v < min || v > max) {
                    error("Ingrese un número entero entre " + min + " y " + max + ".");
                    valido = false;
                } else {
                    valor = v;
                }
            } catch (NumberFormatException ex) {
                error("Valor incorrecto.\n\nDebe ingresar un valor numérico entero positivo entre [" + min + "-" + max + "].");
                valido = false;
            }
        } while (!valido);
        return valor;
    }

    public static String pedirSoloNumerosCancelable(String prompt) {
        String data;
        boolean valido;
        do {
            valido = true;
            data = pedirTextoNoVacioCancelable(prompt);
            if (data == null) {
                return null;
            }
            if (!SOLO_NUMEROS.matcher(data).matches()) {
                error("Ingrese solo números.");
                valido = false;
            }
        } while (!valido);
        return data;
    }

    public static String pedirSoloLetrasCancelable(String prompt) {
        String data;
        boolean valido;
        do {
            valido = true;
            data = pedirTextoNoVacioCancelable(prompt);
            if (data == null) {
                return null;
            }
            if (!SOLO_LETRAS_ESPACIOS.matcher(data).matches()) {
                error("Ingrese solo letras.");
                valido = false;
            }
        } while (!valido);
        return data;
    }

    public static String pedirCorreoComCancelable(String prompt) {
        String data;
        boolean valido;
        do {
            valido = true;
            data = pedirTextoNoVacioCancelable(prompt);
            if (data == null) {
                return null;
            }
            if (!CORREO_UNEFA.matcher(data).matches()) {
                error("Ingrese un correo válido con dominio .com.\n\nEjemplo: usuario@dominio.com");
                valido = false;
            }
        } while (!valido);
        return data;
    }

    public static String pedirTelefono11Cancelable(String prompt) {
        String data;
        boolean valido;
        do {
            valido = true;
            data = pedirSoloNumerosCancelable(prompt);
            if (data == null) {
                return null;
            }
            if (data.length() != 11) {
                error("El teléfono debe tener 11 dígitos.");
                valido = false;
            }
        } while (!valido);
        return data;
    }

    public static final int CEDULA_DIGITOS = 8;

    public static boolean esCedulaValida(String cedula) {
        return cedula != null && CEDULA_VALIDA.matcher(cedula.trim()).matches();
    }

    /** Solo dígitos, máximo 8 (cédula venezolana). */
    public static void aplicarCampoCedula(JTextField campo) {
        if (campo == null) {
            return;
        }
        campo.setColumns(9);
        AbstractDocument doc = (AbstractDocument) campo.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string == null) {
                    return;
                }
                String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
                String nuevo = actual.substring(0, offset) + string + actual.substring(offset);
                if (nuevo.matches("\\d{0," + CEDULA_DIGITOS + "}")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                String reemplazo = text != null ? text : "";
                String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
                String nuevo = actual.substring(0, offset) + reemplazo + actual.substring(offset + length);
                if (nuevo.matches("\\d{0," + CEDULA_DIGITOS + "}")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }

    public static String pedirCedula9Cancelable(String prompt) {
        String data;
        boolean valido;
        do {
            valido = true;
            data = pedirSoloNumerosCancelable(prompt);
            if (data == null) {
                return null;
            }
            if (!esCedulaValida(data)) {
                error("La cédula debe tener " + CEDULA_DIGITOS + " dígitos.");
                valido = false;
            }
        } while (!valido);
        return data;
    }
}

