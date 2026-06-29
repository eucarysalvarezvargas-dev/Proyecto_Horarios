package com.horarios.model;

import java.time.LocalDateTime;

/**
 * Línea de bitácora de auditoría (en memoria hasta conectar DAO/JDBC).
 */
public class RegistroBitacora {

    private int idEvento;
    private LocalDateTime fechaHora;
    private Integer idUsuario;
    private String username;
    private String rol;
    private String accion;
    private String modulo;
    private String detalle;
    private boolean exito;

    public RegistroBitacora() {
    }

    public RegistroBitacora(int idEvento, LocalDateTime fechaHora, Integer idUsuario, String username, String rol,
            String accion, String modulo, String detalle, boolean exito) {
        this.idEvento = idEvento;
        this.fechaHora = fechaHora;
        this.idUsuario = idUsuario;
        this.username = username;
        this.rol = rol;
        this.accion = accion;
        this.modulo = modulo;
        this.detalle = detalle;
        this.exito = exito;
    }

    public int getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(int idEvento) {
        this.idEvento = idEvento;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public boolean isExito() {
        return exito;
    }

    public void setExito(boolean exito) {
        this.exito = exito;
    }
}
