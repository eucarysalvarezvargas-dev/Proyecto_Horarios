package com.horarios.model;

public class Aula {

    private int idAula;
    private String codigo;
    private String descripcion;
    private int capacidad;
    private int estado;

    public Aula() {
    }

    public Aula(int idAula, String codigo, String descripcion, int capacidad, int estado) {
        this.idAula = idAula;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.capacidad = capacidad;
        this.estado = estado;
    }

    public int getIdAula() {
        return idAula;
    }

    public void setIdAula(int idAula) {
        this.idAula = idAula;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }
}

