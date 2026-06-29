package com.horarios.util;

/**
 * Una fila de la cuadrícula semanal del PDF (franja horaria × días).
 */
public class FilaGrillaPdf {

    private String entSal;
    private String lun;
    private String mar;
    private String mie;
    private String jue;
    private String vie;
    private String sab;
    private String dom;

    public FilaGrillaPdf() {
    }

    public FilaGrillaPdf(String entSal, String lun, String mar, String mie, String jue, String vie, String sab, String dom) {
        this.entSal = entSal;
        this.lun = lun;
        this.mar = mar;
        this.mie = mie;
        this.jue = jue;
        this.vie = vie;
        this.sab = sab;
        this.dom = dom;
    }

    public String getEntSal() {
        return entSal;
    }

    public void setEntSal(String entSal) {
        this.entSal = entSal;
    }

    public String getLun() {
        return lun;
    }

    public void setLun(String lun) {
        this.lun = lun;
    }

    public String getMar() {
        return mar;
    }

    public void setMar(String mar) {
        this.mar = mar;
    }

    public String getMie() {
        return mie;
    }

    public void setMie(String mie) {
        this.mie = mie;
    }

    public String getJue() {
        return jue;
    }

    public void setJue(String jue) {
        this.jue = jue;
    }

    public String getVie() {
        return vie;
    }

    public void setVie(String vie) {
        this.vie = vie;
    }

    public String getSab() {
        return sab;
    }

    public void setSab(String sab) {
        this.sab = sab;
    }

    public String getDom() {
        return dom;
    }

    public void setDom(String dom) {
        this.dom = dom;
    }
}
