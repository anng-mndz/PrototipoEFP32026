-- ============================================================
--  Script SQL – PrototipoEFP32026
--  Universidad Mariano Gálvez de Guatemala
--  Programación III – Examen Final Serie IV
--  Tabla principal: carreras  |
-- ============================================================
USE sig;

DROP TABLE IF EXISTS carreras;

CREATE TABLE carreras (
    codigo_carrera   VARCHAR(5)   NOT NULL,
    nombre_carrera   VARCHAR(45)  NOT NULL,
    codigo_facultad  VARCHAR(5)   NOT NULL,
    estatus_carrera  VARCHAR(1)   NOT NULL DEFAULT 'A',
    CONSTRAINT pk_carreras       PRIMARY KEY (codigo_carrera),
    CONSTRAINT ck_estatus_carrera CHECK (estatus_carrera IN ('A','I'))
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COMMENT='Catálogo de carreras de la universidad';