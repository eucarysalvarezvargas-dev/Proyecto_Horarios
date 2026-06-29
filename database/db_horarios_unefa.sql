-- Sistema de Gestión de Horarios Docentes UNEFA
-- MySQL 8 / XAMPP
-- Ejecutar este único script para crear la base y el usuario administrador inicial.

CREATE DATABASE IF NOT EXISTS db_horarios_unefa
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE db_horarios_unefa;

CREATE TABLE IF NOT EXISTS usuarios (
  id_usuario INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  rol ENUM('ADMIN','COORDINADOR','DOCENTE') NOT NULL,
  cedula VARCHAR(8),
  nombres VARCHAR(80),
  apellidos VARCHAR(80),
  estado TINYINT NOT NULL DEFAULT 1,
  fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS docentes (
  id_docente INT AUTO_INCREMENT PRIMARY KEY,
  cedula VARCHAR(8) NOT NULL UNIQUE,
  nombres VARCHAR(80) NOT NULL,
  apellidos VARCHAR(80) NOT NULL,
  correo VARCHAR(120),
  telefono VARCHAR(30),
  estado TINYINT NOT NULL DEFAULT 1,
  fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS asignaturas (
  id_asignatura INT AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(20) NOT NULL UNIQUE,
  nombre VARCHAR(120) NOT NULL,
  horas_semanales INT NOT NULL,
  estado TINYINT NOT NULL DEFAULT 1,
  fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS aulas (
  id_aula INT AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(20) NOT NULL UNIQUE,
  descripcion VARCHAR(120),
  capacidad INT NOT NULL DEFAULT 30,
  estado TINYINT NOT NULL DEFAULT 1,
  fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS horarios (
  id_horario INT AUTO_INCREMENT PRIMARY KEY,
  id_docente INT NOT NULL,
  id_asignatura INT NOT NULL,
  id_aula INT NOT NULL,
  dia_semana VARCHAR(15) NOT NULL,
  hora_inicio TIME NOT NULL,
  hora_fin TIME NOT NULL,
  usuario_creador INT,
  fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  estado TINYINT NOT NULL DEFAULT 1,
  CONSTRAINT fk_hor_docente FOREIGN KEY (id_docente) REFERENCES docentes(id_docente),
  CONSTRAINT fk_hor_asig FOREIGN KEY (id_asignatura) REFERENCES asignaturas(id_asignatura),
  CONSTRAINT fk_hor_aula FOREIGN KEY (id_aula) REFERENCES aulas(id_aula),
  CONSTRAINT fk_hor_usuario FOREIGN KEY (usuario_creador) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bitacora (
  id_evento INT AUTO_INCREMENT PRIMARY KEY,
  fecha_hora DATETIME NOT NULL,
  id_usuario INT,
  username VARCHAR(50),
  rol VARCHAR(20),
  accion VARCHAR(30) NOT NULL,
  modulo VARCHAR(30) NOT NULL,
  detalle VARCHAR(500),
  exito TINYINT NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- Administrador inicial: usuario admin / clave 12345678 (hash SHA-256 + salt)
INSERT INTO usuarios (username, password, rol, estado)
SELECT 'admin', 'QUJDREVGR0hJSktMTU5PUA==:F1kXWYv3vKv8rle8uh7p1h0Qv6jDTd0T9wHWRrtVsvw=', 'ADMIN', 1
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE username = 'admin');

-- Migrar instalaciones previas con admin en texto plano
UPDATE usuarios
SET password = 'QUJDREVGR0hJSktMTU5PUA==:F1kXWYv3vKv8rle8uh7p1h0Qv6jDTd0T9wHWRrtVsvw='
WHERE username = 'admin' AND password = '12345678';
