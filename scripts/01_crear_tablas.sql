-- ===========================================================================
--  Microservicio foro-moderacion
--  Creacion de SUS tablas en el esquema FORO de Oracle Autonomous Database.
--
--  Ejecutar con el usuario FORO:
--    ./scripts/sql.sh foro scripts/01_crear_tablas.sql
--
--  Generado por microservicio-arquetipo con la tabla de ejemplo NORMA.
--  Cada microservicio es dueno de sus tablas y no toca las de los demas.
-- ===========================================================================

CREATE TABLE NORMA (
    ID              NUMBER(19)      NOT NULL,
    NOMBRE          VARCHAR2(100)   NOT NULL,
    DESCRIPCION     VARCHAR2(400),
    ACTIVO          NUMBER(1)       DEFAULT 1 NOT NULL,
    FECHA_CREACION  TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT PK_NORMA         PRIMARY KEY (ID),
    CONSTRAINT UK_NORMA_NOMBRE  UNIQUE (NOMBRE),
    CONSTRAINT CK_NORMA_ACTIVO  CHECK (ACTIVO IN (0, 1))
);

COMMENT ON TABLE NORMA IS 'Microservicio de moderacion del foro: normas, reportes y sanciones sobre temas y comentarios';

CREATE SEQUENCE NORMA_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
