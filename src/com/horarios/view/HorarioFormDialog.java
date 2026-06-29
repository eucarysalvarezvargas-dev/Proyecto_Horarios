package com.horarios.view;

import com.horarios.controller.AsignaturaController;
import com.horarios.controller.AulaController;
import com.horarios.controller.DocenteController;
import com.horarios.controller.HorarioController;
import com.horarios.model.Asignatura;
import com.horarios.model.Aula;
import com.horarios.model.Docente;
import com.horarios.model.Horario;
import com.horarios.util.Mensajes;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

public class HorarioFormDialog extends JDialog {

    private static final String[] DIAS = {
        "LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO"
    };

    private final JComboBox<EntidadCombo> cboDocente;
    private final JComboBox<EntidadCombo> cboAsignatura;
    private final JComboBox<EntidadCombo> cboAula;
    private final JComboBox<String> cboDia;
    private final JSpinner spInicio;
    private final JSpinner spFin;

    private final int idUsuarioCreador;
    private final Horario inicial;

    private Horario resultado;

    /** Prellena día y franja horaria al registrar desde la cuadrícula ({@code inicial == null}). */
    public static final class PrellenoFranja {

        public final String diaNormalizado;
        public final Time horaIni;
        public final Time horaFin;

        public PrellenoFranja(String diaNormalizado, Time horaIni, Time horaFin) {
            this.diaNormalizado = diaNormalizado;
            this.horaIni = horaIni;
            this.horaFin = horaFin;
        }
    }

    private static final class EntidadCombo {

        final int id;
        final String etiqueta;

        EntidadCombo(int id, String etiqueta) {
            this.id = id;
            this.etiqueta = etiqueta;
        }

        @Override
        public String toString() {
            return etiqueta;
        }
    }

    public HorarioFormDialog(JFrame owner, String titulo, Horario inicial, boolean soloLectura,
            DocenteController docenteController,
            AsignaturaController asignaturaController,
            AulaController aulaController,
            int idUsuarioCreador) {
        this(owner, titulo, inicial, soloLectura, docenteController, asignaturaController, aulaController,
                idUsuarioCreador, null);
    }

    public HorarioFormDialog(JFrame owner, String titulo, Horario inicial, boolean soloLectura,
            DocenteController docenteController,
            AsignaturaController asignaturaController,
            AulaController aulaController,
            int idUsuarioCreador,
            PrellenoFranja prefranja) {
        super(owner, titulo, true);
        this.inicial = inicial;
        this.idUsuarioCreador = idUsuarioCreador;

        cboDocente = new JComboBox<>();
        cboAsignatura = new JComboBox<>();
        cboAula = new JComboBox<>();
        cboDia = new JComboBox<>(DIAS);

        if (docenteController != null) {
            for (Docente d : docenteController.listarActivosParaHorarios()) {
                String nom = (d.getNombres() != null ? d.getNombres() : "") + " " + (d.getApellidos() != null ? d.getApellidos() : "");
                cboDocente.addItem(new EntidadCombo(d.getIdDocente(),
                        d.getIdDocente() + " — " + d.getCedula() + " — " + nom.trim()));
            }
        }
        if (asignaturaController != null) {
            for (Asignatura a : asignaturaController.listarActivas()) {
                cboAsignatura.addItem(new EntidadCombo(a.getIdAsignatura(),
                        a.getIdAsignatura() + " — " + a.getCodigo() + " — " + (a.getNombre() != null ? a.getNombre() : "")));
            }
        }
        if (aulaController != null) {
            for (Aula au : aulaController.listarActivas()) {
                cboAula.addItem(new EntidadCombo(au.getIdAula(),
                        au.getIdAula() + " — " + au.getCodigo() + " — " + (au.getDescripcion() != null ? au.getDescripcion() : "")));
            }
        }

        Calendar calIni = Calendar.getInstance();
        calIni.set(Calendar.HOUR_OF_DAY, 8);
        calIni.set(Calendar.MINUTE, 0);
        calIni.set(Calendar.SECOND, 0);
        calIni.set(Calendar.MILLISECOND, 0);
        SpinnerDateModel smIni = new SpinnerDateModel(calIni.getTime(), null, null, Calendar.MINUTE);
        spInicio = new JSpinner(smIni);
        spInicio.setEditor(new JSpinner.DateEditor(spInicio, "HH:mm:ss"));

        Calendar calFin = Calendar.getInstance();
        calFin.set(Calendar.HOUR_OF_DAY, 9);
        calFin.set(Calendar.MINUTE, 0);
        calFin.set(Calendar.SECOND, 0);
        calFin.set(Calendar.MILLISECOND, 0);
        SpinnerDateModel smFin = new SpinnerDateModel(calFin.getTime(), null, null, Calendar.MINUTE);
        spFin = new JSpinner(smFin);
        spFin.setEditor(new JSpinner.DateEditor(spFin, "HH:mm:ss"));

        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos del horario"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelFormulario.add(new JLabel("Docente:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        panelFormulario.add(cboDocente, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panelFormulario.add(new JLabel("Asignatura:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        panelFormulario.add(cboAsignatura, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panelFormulario.add(new JLabel("Aula:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        panelFormulario.add(cboAula, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panelFormulario.add(new JLabel("Día:"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(cboDia, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panelFormulario.add(new JLabel("Hora inicio:"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(spInicio, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        panelFormulario.add(new JLabel("Hora fin:"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(spFin, gbc);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnAceptar = new JButton("Aceptar");
        JButton btnCancelar = new JButton("Cancelar");

        btnAceptar.addActionListener(e -> onAceptar(soloLectura));
        btnCancelar.addActionListener(e -> onCancelar());

        panelBotones.add(btnAceptar);
        panelBotones.add(btnCancelar);

        setLayout(new BorderLayout(10, 10));
        add(panelFormulario, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        if (inicial != null) {
            seleccionarPorId(cboDocente, inicial.getIdDocente());
            seleccionarPorId(cboAsignatura, inicial.getIdAsignatura());
            seleccionarPorId(cboAula, inicial.getIdAula());
            String nd = HorarioController.normalizarDia(inicial.getDiaSemana());
            if (nd != null) {
                cboDia.setSelectedItem(nd);
            }
            if (inicial.getHoraInicio() != null) {
                spInicio.setValue(new Date(inicial.getHoraInicio().getTime()));
            }
            if (inicial.getHoraFin() != null) {
                spFin.setValue(new Date(inicial.getHoraFin().getTime()));
            }
        } else if (prefranja != null && prefranja.diaNormalizado != null) {
            cboDia.setSelectedItem(prefranja.diaNormalizado);
            if (prefranja.horaIni != null) {
                spInicio.setValue(new Date(prefranja.horaIni.getTime()));
            }
            if (prefranja.horaFin != null) {
                spFin.setValue(new Date(prefranja.horaFin.getTime()));
            }
        }

        if (soloLectura) {
            cboDocente.setEnabled(false);
            cboAsignatura.setEnabled(false);
            cboAula.setEnabled(false);
            cboDia.setEnabled(false);
            spInicio.setEnabled(false);
            spFin.setEnabled(false);
        }

        pack();
        setLocationRelativeTo(owner);
    }

    private static void seleccionarPorId(JComboBox<EntidadCombo> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).id == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void onAceptar(boolean soloLectura) {
        if (soloLectura) {
            resultado = null;
            dispose();
            return;
        }

        if (cboDocente.getItemCount() == 0 || cboAsignatura.getItemCount() == 0 || cboAula.getItemCount() == 0) {
            Mensajes.error("Debe existir al menos un docente, una asignatura y un aula activos para registrar horarios.");
            return;
        }

        EntidadCombo doc = (EntidadCombo) cboDocente.getSelectedItem();
        EntidadCombo asig = (EntidadCombo) cboAsignatura.getSelectedItem();
        EntidadCombo aula = (EntidadCombo) cboAula.getSelectedItem();
        String dia = (String) cboDia.getSelectedItem();

        Time inicio = toSqlTime(spInicio.getValue());
        Time fin = toSqlTime(spFin.getValue());

        if (!inicio.before(fin)) {
            Mensajes.error("La hora de inicio debe ser anterior a la hora de fin.");
            return;
        }

        Horario h = new Horario();
        h.setIdDocente(doc.id);
        h.setIdAsignatura(asig.id);
        h.setIdAula(aula.id);
        h.setDiaSemana(dia);
        h.setHoraInicio(inicio);
        h.setHoraFin(fin);
        if (inicial != null) {
            h.setUsuarioCreador(inicial.getUsuarioCreador());
            h.setFechaRegistro(inicial.getFechaRegistro());
        } else {
            h.setUsuarioCreador(idUsuarioCreador);
            h.setFechaRegistro(new Timestamp(System.currentTimeMillis()));
        }
        h.setEstado(1);

        resultado = h;
        dispose();
    }

    private static Time toSqlTime(Object value) {
        Date d = (Date) value;
        Calendar c = Calendar.getInstance(Locale.getDefault());
        c.setTime(d);
        String s = String.format(Locale.ROOT, "%02d:%02d:%02d",
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
        return Time.valueOf(s);
    }

    private void onCancelar() {
        resultado = null;
        dispose();
    }

    public Horario getResultado() {
        return resultado;
    }
}
