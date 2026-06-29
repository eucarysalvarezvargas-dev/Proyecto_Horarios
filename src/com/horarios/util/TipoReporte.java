package com.horarios.util;

public enum TipoReporte {
    GENERAL("General", 0),
    SEMANAL("Semanal", 7),
    QUINCENAL("Quincenal", 15),
    MENSUAL("Mensual", 30);

    private final String etiqueta;
    private final int diasAtras;

    TipoReporte(String etiqueta, int diasAtras) {
        this.etiqueta = etiqueta;
        this.diasAtras = diasAtras;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public int getDiasAtras() {
        return diasAtras;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
