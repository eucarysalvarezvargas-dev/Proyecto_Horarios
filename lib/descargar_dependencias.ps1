# Descarga JasperReports 6.21.0, dependencias y MySQL Connector/J a lib/
# Ejecutar en PowerShell:  Set-Location lib; .\descargar_dependencias.ps1

$ErrorActionPreference = "Stop"
$base = "https://repo1.maven.org/maven2"
$lib = $PSScriptRoot

function Descargar($rutaRelativa, $nombreArchivo) {
    $url = "$base/$rutaRelativa"
    $dest = Join-Path $lib $nombreArchivo
    if (Test-Path $dest) {
        Write-Host "OK (ya existe): $nombreArchivo"
        return
    }
    Write-Host "Descargando $nombreArchivo ..."
    Invoke-WebRequest -Uri $url -OutFile $dest -UseBasicParsing
}

# MySQL
Descargar "com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar" "mysql-connector-j-8.3.0.jar"

# Jasper core
Descargar "net/sf/jasperreports/jasperreports/6.21.0/jasperreports-6.21.0.jar" "jasperreports-6.21.0.jar"

# Dependencias habituales Jasper 6.x
Descargar "commons-logging/commons-logging/1.2/commons-logging-1.2.jar" "commons-logging-1.2.jar"
Descargar "org/apache/commons/commons-collections4/4.4/commons-collections4-4.4.jar" "commons-collections4-4.4.jar"
Descargar "commons-digester/commons-digester/2.1/commons-digester-2.1.jar" "commons-digester-2.1.jar"
Descargar "commons-beanutils/commons-beanutils/1.9.4/commons-beanutils-1.9.4.jar" "commons-beanutils-1.9.4.jar"
Descargar "org/jfree/jcommon/1.0.23/jcommon-1.0.23.jar" "jcommon-1.0.23.jar"
Descargar "org/jfree/jfreechart/1.0.19/jfreechart-1.0.19.jar" "jfreechart-1.0.19.jar"
# PDF (OpenPDF compatible con exportación Jasper 6.x)
Descargar "com/github/librepdf/openpdf/1.3.30/openpdf-1.3.30.jar" "openpdf-1.3.30.jar"
Descargar "org/eclipse/jdt/ecj/3.21.0/ecj-3.21.0.jar" "ecj-3.21.0.jar"

Write-Host ""
Write-Host "Listo. En NetBeans: Propiedades del proyecto > Libraries > Add JAR/Folder > seleccione todos los .jar de lib/"
