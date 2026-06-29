package com.horarios.util;

/**
 * Fila de la tabla superior del PDF (referencia código → asignatura, aula, docente).
 */
public class FilaLeyendaPdf {

    private Integer numero;
    private String ref;
    private String codAsig;
    private String nombreAsig;
    private String aula;
    private String docente;

    public FilaLeyendaPdf() {
    }

    public FilaLeyendaPdf(Integer numero, String ref, String codAsig, String nombreAsig, String aula, String docente) {
        this.numero = numero;
        this.ref = ref;
        this.codAsig = codAsig;
        this.nombreAsig = nombreAsig;
        this.aula = aula;
        this.docente = docente;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getCodAsig() {
        return codAsig;
    }

    public void setCodAsig(String codAsig) {
        this.codAsig = codAsig;
    }

    public String getNombreAsig() {
        return nombreAsig;
    }

    public void setNombreAsig(String nombreAsig) {
        this.nombreAsig = nombreAsig;
    }

    public String getAula() {
        return aula;
    }

    public void setAula(String aula) {
        this.aula = aula;
    }

    public String getDocente() {
        return docente;
    }

    public void setDocente(String docente) {
        this.docente = docente;
    }
}
