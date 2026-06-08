-- ============================================================
-- COLEGIO ADA BYRON - Sistema de Matrícula
-- Base de Datos PostgreSQL
-- ============================================================

CREATE DATABASE adabyron_db;
\c adabyron_db;

-- ============================================================
-- TABLAS MAESTRAS
-- ============================================================

CREATE TABLE nivel (
    idnivel     SERIAL PRIMARY KEY,
    nombre      VARCHAR(50) NOT NULL
);

CREATE TABLE cargo (
    idcargo     SERIAL PRIMARY KEY,
    nombre      VARCHAR(80) NOT NULL
);

CREATE TABLE concepto_pago (
    idconceptopago  SERIAL PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL
);

-- ============================================================
-- ACADÉMICO
-- ============================================================

CREATE TABLE grado_academico (
    idgrado     SERIAL PRIMARY KEY,
    nombre      VARCHAR(50) NOT NULL,
    idnivel     INTEGER NOT NULL REFERENCES nivel(idnivel)
);

CREATE TABLE seccion (
    idseccion   SERIAL PRIMARY KEY,
    nombre      VARCHAR(50) NOT NULL,
    capacidad   INTEGER NOT NULL DEFAULT 30,
    turno       VARCHAR(20) NOT NULL,
    aula        VARCHAR(20),
    idgrado     INTEGER NOT NULL REFERENCES grado_academico(idgrado)
);

-- ============================================================
-- PERSONAS
-- ============================================================

CREATE TABLE personal (
    idpersonal      SERIAL PRIMARY KEY,
    paterno         VARCHAR(80) NOT NULL,
    materno         VARCHAR(80) NOT NULL,
    nombre          VARCHAR(80) NOT NULL,
    docidentidad    VARCHAR(15) NOT NULL UNIQUE,
    direccion       VARCHAR(200),
    ubigeo          VARCHAR(50),
    fechaingreso    DATE,
    idcargo         INTEGER NOT NULL REFERENCES cargo(idcargo)
);

CREATE TABLE estudiante (
    idestudiante    SERIAL PRIMARY KEY,
    paterno         VARCHAR(80) NOT NULL,
    materno         VARCHAR(80) NOT NULL,
    nombre          VARCHAR(80) NOT NULL,
    fechanac        DATE NOT NULL,
    docidentidad    VARCHAR(15) UNIQUE,
    direccion       VARCHAR(200),
    ubigeo          VARCHAR(200),
    estado          VARCHAR(10) NOT NULL DEFAULT 'ACTIVO'
);

CREATE TABLE apoderado (
    idapoderado     SERIAL PRIMARY KEY,
    paterno         VARCHAR(80) NOT NULL,
    materno         VARCHAR(80) NOT NULL,
    nombre          VARCHAR(80) NOT NULL,
    fechanac        DATE,
    telefono        VARCHAR(15),
    celular         VARCHAR(15),
    correo          VARCHAR(100)
);

CREATE TABLE estudiante_apoderado (
    idestudianteapoderado   CHAR(18) PRIMARY KEY,
    idestudiante            INTEGER NOT NULL REFERENCES estudiante(idestudiante),
    idapoderado             INTEGER NOT NULL REFERENCES apoderado(idapoderado),
    codparentesco           VARCHAR(6) NOT NULL
);

-- ============================================================
-- USUARIOS DEL SISTEMA
-- ============================================================

CREATE TABLE usuarios (
    idusuario       SERIAL PRIMARY KEY,
    codusuario      VARCHAR(50) NOT NULL UNIQUE,
    clave           VARCHAR(200) NOT NULL,
    codestado       VARCHAR(8) NOT NULL DEFAULT 'ACTIVO',
    rol             VARCHAR(30) NOT NULL DEFAULT 'PADRE',
    idpersonal      INTEGER REFERENCES personal(idpersonal)
);

-- ============================================================
-- SOLICITUD DE MATRÍCULA
-- ============================================================

CREATE TABLE solicitud (
    idsolicitud     SERIAL PRIMARY KEY,
    fecharegisto    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    codestado       VARCHAR(6) NOT NULL DEFAULT 'PEND',
    idestudiante    INTEGER NOT NULL REFERENCES estudiante(idestudiante),
    idapoderado     INTEGER NOT NULL REFERENCES apoderado(idapoderado)
);

CREATE TABLE solicitud_documentos (
    idpostulacion_documento SERIAL PRIMARY KEY,
    idsolicitud             INTEGER NOT NULL REFERENCES solicitud(idsolicitud),
    codocumento             VARCHAR(10) NOT NULL,
    comentario              VARCHAR(200)
);

CREATE TABLE evaluacion (
    idevaluacion        SERIAL PRIMARY KEY,
    idsolicitud         INTEGER NOT NULL REFERENCES solicitud(idsolicitud),
    idpersonal          INTEGER NOT NULL REFERENCES personal(idpersonal),
    fechaevaluacion     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    codestado           VARCHAR(10) NOT NULL,
    comentario          VARCHAR(200)
);

-- ============================================================
-- PAGO Y MATRÍCULA
-- ============================================================

CREATE TABLE pago (
    idpago          SERIAL PRIMARY KEY,
    idconceptopago  INTEGER NOT NULL REFERENCES concepto_pago(idconceptopago),
    idestudiante    INTEGER NOT NULL REFERENCES estudiante(idestudiante),
    fecharegisto    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    codestado       VARCHAR(8) NOT NULL DEFAULT 'PEND',
    importe         DECIMAL(10,2) NOT NULL,
    metodopago      VARCHAR(50),
    glosa           VARCHAR(200)
);

CREATE TABLE matricula (
    idmatricula     SERIAL PRIMARY KEY,
    aniolectivo     VARCHAR(5) NOT NULL,
    fecharegisto    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    idestudiante    INTEGER NOT NULL REFERENCES estudiante(idestudiante),
    idseccion       INTEGER NOT NULL REFERENCES seccion(idseccion),
    idpago          INTEGER REFERENCES pago(idpago),
    codestado       VARCHAR(10) NOT NULL DEFAULT 'ACTIVO'
);

-- ============================================================
-- DATOS INICIALES
-- ============================================================

INSERT INTO nivel (nombre) VALUES ('Inicial'), ('Primaria'), ('Secundaria');

INSERT INTO cargo (nombre) VALUES ('Director'), ('Secretaria'), ('Docente'), ('Administrador');

INSERT INTO concepto_pago (nombre) VALUES ('Matrícula'), ('Pensión'), ('Materiales');

INSERT INTO grado_academico (nombre, idnivel) VALUES
    ('1ro Inicial',1),('2ro Inicial',1),('3ro Inicial',1),
    ('1ro Primaria',2),('2do Primaria',2),('3ro Primaria',2),
    ('4to Primaria',2),('5to Primaria',2),('6to Primaria',2),
    ('1ro Secundaria',3),('2do Secundaria',3),('3ro Secundaria',3),
    ('4to Secundaria',3),('5to Secundaria',3);

INSERT INTO personal (paterno, materno, nombre, docidentidad, idcargo)
    VALUES ('Admin', 'Sistema', 'Administrador', '00000001', 4);

INSERT INTO usuarios (codusuario, clave, codestado, rol, idpersonal)
    VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Zs', 'ACTIVO', 'ADMIN', 1);
-- Contraseña: admin123

COMMIT;

-- ============================================================
-- PASARELA DE PAGO (mejora sobre Colegio2)
-- ============================================================
CREATE TABLE IF NOT EXISTS transaccion_pago (
    idtransaccion       SERIAL PRIMARY KEY,
    codigotransaccion   VARCHAR(30) NOT NULL UNIQUE,
    idestudiante        INTEGER NOT NULL REFERENCES estudiante(idestudiante),
    idpago              INTEGER REFERENCES pago(idpago),
    monto               DECIMAL(10,2) NOT NULL,
    tipotarjeta         VARCHAR(20),
    ultimosdigitos      VARCHAR(4),
    nombretitular       VARCHAR(100),
    estado              VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE',
    observaciones       VARCHAR(200),
    fechatransaccion    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS periodo_academico (
    idperiodo   SERIAL PRIMARY KEY,
    nombre      VARCHAR(20) NOT NULL,
    anio        VARCHAR(5)  NOT NULL,
    tipo        VARCHAR(20) NOT NULL DEFAULT 'BIMESTRE',
    activo      BOOLEAN     NOT NULL DEFAULT TRUE
);

INSERT INTO periodo_academico (nombre, anio, tipo) VALUES
  ('Bimestre 1', '2026', 'BIMESTRE'),
  ('Bimestre 2', '2026', 'BIMESTRE'),
  ('Bimestre 3', '2026', 'BIMESTRE'),
  ('Bimestre 4', '2026', 'BIMESTRE');

-- ============================================================
-- USUARIO ADMIN (contraseña: 1234)
-- ============================================================
INSERT INTO usuarios (codusuario, clave, codestado, rol)
VALUES (
  'admin',
  '$2a$10$MhGdQZgeJJUrTtZ/rQAcMO46057vOkeMnv.qOVx.y6qXCvDRu05U6',
  'ACTIVO',
  'ADMIN'
) ON CONFLICT (codusuario) DO NOTHING;
