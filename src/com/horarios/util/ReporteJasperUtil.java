package com.horarios.util;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exportación PDF vía JasperReports sin dependencia de compilación (reflexión).
 */
public final class ReporteJasperUtil {

    private ReporteJasperUtil() {
    }

    public static boolean isDisponible() {
        try {
            Class.forName("net.sf.jasperreports.engine.JasperCompileManager");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    /**
     * PDF de horario estilo UNEFA: subreporte de leyenda + cuadrícula semanal.
     */
    public static void exportarHorarioDocentePdf(
            File jrxmlPrincipal,
            File jrxmlLeyenda,
            File destinoPdf,
            Map<String, Object> parametros,
            List<?> filasGrilla,
            List<?> filasLeyenda) throws Exception {
        if (!isDisponible()) {
            throw new ClassNotFoundException("JasperReports no está en lib/. Ver lib/LEEME_JASPER.txt");
        }
        Class<?> compileMgr = Class.forName("net.sf.jasperreports.engine.JasperCompileManager");
        Method compile = compileMgr.getMethod("compileReport", String.class);
        Object subReport = compilarReporte(compile, jrxmlLeyenda);
        Object jasperReport = compilarReporte(compile, jrxmlPrincipal);

        Map<String, Object> params = parametros != null ? new HashMap<>(parametros) : new HashMap<>();
        params.put("SUBREPORT_LEYENDA", subReport);
        params.put("LISTA_LEYENDA", filasLeyenda != null ? filasLeyenda : new java.util.ArrayList<>());

        Class<?> dsClass = Class.forName("net.sf.jasperreports.engine.data.JRBeanCollectionDataSource");
        Constructor<?> dsCtor = dsClass.getConstructor(java.util.Collection.class);
        Object dataSource = dsCtor.newInstance(filasGrilla);

        Class<?> fillMgr = Class.forName("net.sf.jasperreports.engine.JasperFillManager");
        Method fill = fillMgr.getMethod("fillReport",
                Class.forName("net.sf.jasperreports.engine.JasperReport"),
                Map.class,
                Class.forName("net.sf.jasperreports.engine.JRDataSource"));
        Object jasperPrint = fill.invoke(null, jasperReport, params, dataSource);

        Class<?> exportMgr = Class.forName("net.sf.jasperreports.engine.JasperExportManager");
        Method export = exportMgr.getMethod("exportReportToPdfFile",
                Class.forName("net.sf.jasperreports.engine.JasperPrint"),
                String.class);
        export.invoke(null, jasperPrint, destinoPdf.getAbsolutePath());
    }

    public static void exportarBeanCollectionPdf(File archivoJrxml, File destinoPdf,
            Map<String, Object> parametros, List<?> filas) throws Exception {
        if (!isDisponible()) {
            throw new ClassNotFoundException("JasperReports no está en lib/. Ver lib/LEEME_JASPER.txt");
        }
        if (archivoJrxml == null || !archivoJrxml.isFile()) {
            throw new IllegalArgumentException("Plantilla no encontrada: " + archivoJrxml);
        }
        Class<?> compileMgr = Class.forName("net.sf.jasperreports.engine.JasperCompileManager");
        Method compile = compileMgr.getMethod("compileReport", String.class);
        Object jasperReport = compile.invoke(null, archivoJrxml.getAbsolutePath());

        Class<?> dsClass = Class.forName("net.sf.jasperreports.engine.data.JRBeanCollectionDataSource");
        Constructor<?> dsCtor = dsClass.getConstructor(java.util.Collection.class);
        Object dataSource = dsCtor.newInstance(filas);

        Map<String, Object> params = parametros != null ? parametros : new HashMap<>();
        Class<?> fillMgr = Class.forName("net.sf.jasperreports.engine.JasperFillManager");
        Method fill = fillMgr.getMethod("fillReport",
                Class.forName("net.sf.jasperreports.engine.JasperReport"),
                Map.class,
                Class.forName("net.sf.jasperreports.engine.JRDataSource"));
        Object jasperPrint = fill.invoke(null, jasperReport, params, dataSource);

        Class<?> exportMgr = Class.forName("net.sf.jasperreports.engine.JasperExportManager");
        Method export = exportMgr.getMethod("exportReportToPdfFile",
                Class.forName("net.sf.jasperreports.engine.JasperPrint"),
                String.class);
        export.invoke(null, jasperPrint, destinoPdf.getAbsolutePath());
    }

    private static Object compilarReporte(Method compile, File jrxml) throws Exception {
        if (jrxml == null || !jrxml.isFile()) {
            throw new IllegalArgumentException("Plantilla no encontrada: " + jrxml);
        }
        try {
            return compile.invoke(null, jrxml.getAbsolutePath());
        } catch (java.lang.reflect.InvocationTargetException ite) {
            Throwable cause = ite.getCause() != null ? ite.getCause() : ite;
            throw new Exception("Error en " + jrxml.getName() + ": " + cause.getMessage(), cause);
        }
    }

    public static File resolverPlantilla(String nombreArchivo) {
        File directo = new File("reports", nombreArchivo);
        if (directo.isFile()) {
            return directo;
        }
        File cwd = new File(System.getProperty("user.dir", "."), "reports/" + nombreArchivo);
        if (cwd.isFile()) {
            return cwd;
        }
        try (InputStream in = ReporteJasperUtil.class.getResourceAsStream("/reports/" + nombreArchivo)) {
            if (in != null) {
                File tmp = Files.createTempFile("jr_", "_" + nombreArchivo).toFile();
                tmp.deleteOnExit();
                Files.copy(in, tmp.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                return tmp;
            }
        } catch (Exception ignored) {
        }
        return directo;
    }
}
