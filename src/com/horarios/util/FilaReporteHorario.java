package com.horarios.util;

/**
 * Fila para plantillas Jasper (JRBeanCollectionDataSource).
 */
public class FilaReporteHorario {

    private String dia;
    private String franja;
    private String asignatura;
    private String docente;
    private String aula;
    private String codigoAsignatura;
    private String horasBloque;

    public FilaReporteHorario() {
    }

    public FilaReporteHorario(String dia, String franja, String asignatura, String docente, String aula,
            String codigoAsignatura, String horasBloque) {
        this.dia = dia;
        this.franja = franja;
        this.asignatura = asignatura;
        this.docente = docente;
        this.aula = aula;
        this.codigoAsignatura = codigoAsignatura;
        this.horasBloque = horasBloque;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public String getFranja() {
        return franja;
    }

    public void setFranja(String franja) {
        this.franja = franja;
    }

    public String getAsignatura() {
        return asignatura;
    }

    public void setAsignatura(String asignatura) {
        this.asignatura = asignatura;
    }

    public String getDocente() {
        return docente;
    }

    public void setDocente(String docente) {
        this.docente = docente;
    }

    public String getAula() {
        return aula;
    }

    public void setAula(String aula) {
        this.aula = aula;
    }

    public String getCodigoAsignatura() {
        return codigoAsignatura;
    }

    public void setCodigoAsignatura(String codigoAsignatura) {
        this.codigoAsignatura = codigoAsignatura;
    }

    public String getHorasBloque() {
        return horasBloque;
    }

    public void setHorasBloque(String horasBloque) {
        this.horasBloque = horasBloque;
    }
}
