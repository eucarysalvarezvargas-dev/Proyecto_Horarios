package com.horarios.model;

import java.sql.Time;
import java.sql.Timestamp;

public class Horario {

    private int idHorario;
    private int idDocente;
    private int idAsignatura;
    private int idAula;
    private String diaSemana;
    private Time horaInicio;
    private Time horaFin;
    private int usuarioCreador;
    private Timestamp fechaRegistro;
    private int estado;

    public Horario() {
    }

    public Horario(int idHorario, int idDocente, int idAsignatura, int idAula, String diaSemana, Time horaInicio, Time horaFin, int usuarioCreador, Timestamp fechaRegistro, int estado) {
        this.idHorario = idHorario;
        this.idDocente = idDocente;
        this.idAsignatura = idAsignatura;
        this.idAula = idAula;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.usuarioCreador = usuarioCreador;
        this.fechaRegistro = fechaRegistro;
        this.estado = estado;
    }

    public int getIdHorario() {
        return idHorario;
    }

    public void setIdHorario(int idHorario) {
        this.idHorario = idHorario;
    }

    public int getIdDocente() {
        return idDocente;
    }

    public void setIdDocente(int idDocente) {
        this.idDocente = idDocente;
    }

    public int getIdAsignatura() {
        return idAsignatura;
    }

    public void setIdAsignatura(int idAsignatura) {
        this.idAsignatura = idAsignatura;
    }

    public int getIdAula() {
        return idAula;
    }

    public void setIdAula(int idAula) {
        this.idAula = idAula;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public Time getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(Time horaInicio) {
        this.horaInicio = horaInicio;
    }

    public Time getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(Time horaFin) {
        this.horaFin = horaFin;
    }

    public int getUsuarioCreador() {
        return usuarioCreador;
    }

    public void setUsuarioCreador(int usuarioCreador) {
        this.usuarioCreador = usuarioCreador;
    }

    public Timestamp getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Timestamp fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }
}

