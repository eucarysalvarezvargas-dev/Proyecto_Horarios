package com.horarios.util;

public class FilaResumenReporte {

    private String modulo;
    private Integer cantidad;

    public FilaResumenReporte() {
    }

    public FilaResumenReporte(String modulo, Integer cantidad) {
        this.modulo = modulo;
        this.cantidad = cantidad;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
