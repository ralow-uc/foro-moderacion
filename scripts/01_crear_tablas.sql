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
    CODIGO          VARCHAR2(10)    NOT NULL,
    NOMBRE          VARCHAR2(100)   NOT NULL,
    DESCRIPCION     VARCHAR2(400),
    GRAVEDAD        VARCHAR2(10)    DEFAULT 'LEVE' NOT NULL,
    ACTIVO          NUMBER(1)       DEFAULT 1 NOT NULL,
    FECHA_CREACION  TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT PK_NORMA         PRIMARY KEY (ID),
    CONSTRAINT UK_NORMA_CODIGO  UNIQUE (CODIGO),
    CONSTRAINT UK_NORMA_NOMBRE  UNIQUE (NOMBRE),
    CONSTRAINT CK_NORMA_GRAVEDAD CHECK (GRAVEDAD IN ('LEVE', 'GRAVE', 'CRITICA')),
    CONSTRAINT CK_NORMA_ACTIVO  CHECK (ACTIVO IN (0, 1))
);

COMMENT ON TABLE NORMA IS 'Normas de convivencia del foro que citan los reportes y las sanciones';

CREATE SEQUENCE NORMA_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;


-- ---------------------------------------------------------------------------
-- REPORTE: un usuario denuncia un tema o un comentario citando una norma.
-- CONTENIDO_ID apunta a TEMA o COMENTARIO de foro-temas y NO lleva clave
-- foranea: cada microservicio es dueno de sus tablas. REPORTANTE_ID y
-- REPORTANTE_NOMBRE se copian del JWT.
-- ---------------------------------------------------------------------------
CREATE TABLE REPORTE (
    ID                 NUMBER(19)      NOT NULL,
    TIPO_CONTENIDO     VARCHAR2(20)    NOT NULL,
    CONTENIDO_ID       NUMBER(19)      NOT NULL,
    NORMA_ID           NUMBER(19)      NOT NULL,
    REPORTANTE_ID      NUMBER(19)      NOT NULL,
    REPORTANTE_NOMBRE  VARCHAR2(50)    NOT NULL,
    DETALLE            VARCHAR2(500),
    ESTADO             VARCHAR2(20)    DEFAULT 'PENDIENTE' NOT NULL,
    FECHA_CREACION     TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    FECHA_RESOLUCION   TIMESTAMP,
    RESUELTO_POR       VARCHAR2(50),
    CONSTRAINT PK_REPORTE          PRIMARY KEY (ID),
    CONSTRAINT FK_REPORTE_NORMA    FOREIGN KEY (NORMA_ID) REFERENCES NORMA (ID),
    CONSTRAINT CK_REPORTE_TIPO     CHECK (TIPO_CONTENIDO IN ('TEMA', 'COMENTARIO')),
    CONSTRAINT CK_REPORTE_ESTADO   CHECK (ESTADO IN ('PENDIENTE', 'ACEPTADO', 'DESCARTADO'))
);

COMMENT ON TABLE  REPORTE                IS 'Reportes de los usuarios sobre temas o comentarios que incumplen una norma';
COMMENT ON COLUMN REPORTE.CONTENIDO_ID   IS 'ID del TEMA o COMENTARIO en foro-temas (sin clave foranea entre servicios)';
COMMENT ON COLUMN REPORTE.ESTADO         IS 'PENDIENTE, ACEPTADO (se sanciono) o DESCARTADO (no incumple)';

CREATE SEQUENCE REPORTE_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE INDEX IX_REPORTE_ESTADO    ON REPORTE (ESTADO);
CREATE INDEX IX_REPORTE_CONTENIDO ON REPORTE (TIPO_CONTENIDO, CONTENIDO_ID);


-- ---------------------------------------------------------------------------
-- SANCION: un moderador banea un tema o un comentario citando una norma.
-- El efecto (ocultar el contenido) lo aplica foro-temas en sus columnas
-- BANEADO / MOTIVO_BANEO; aqui queda el registro historico de la decision.
-- Al levantar la sancion no se borra: VIGENTE pasa a 0.
-- ---------------------------------------------------------------------------
CREATE TABLE SANCION (
    ID                    NUMBER(19)      NOT NULL,
    TIPO_CONTENIDO        VARCHAR2(20)    NOT NULL,
    CONTENIDO_ID          NUMBER(19)      NOT NULL,
    TITULO_CONTENIDO      VARCHAR2(200),
    AUTOR_ID              NUMBER(19),
    AUTOR_NOMBRE          VARCHAR2(50),
    NORMA_ID              NUMBER(19)      NOT NULL,
    REPORTE_ID            NUMBER(19),
    MOTIVO                VARCHAR2(500)   NOT NULL,
    MODERADOR_ID          NUMBER(19)      NOT NULL,
    MODERADOR_NOMBRE      VARCHAR2(50)    NOT NULL,
    VIGENTE               NUMBER(1)       DEFAULT 1 NOT NULL,
    FECHA_CREACION        TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    FECHA_LEVANTAMIENTO   TIMESTAMP,
    LEVANTADA_POR         VARCHAR2(50),
    MOTIVO_LEVANTAMIENTO  VARCHAR2(500),
    CONSTRAINT PK_SANCION          PRIMARY KEY (ID),
    CONSTRAINT FK_SANCION_NORMA    FOREIGN KEY (NORMA_ID)   REFERENCES NORMA (ID),
    CONSTRAINT FK_SANCION_REPORTE  FOREIGN KEY (REPORTE_ID) REFERENCES REPORTE (ID),
    CONSTRAINT CK_SANCION_TIPO     CHECK (TIPO_CONTENIDO IN ('TEMA', 'COMENTARIO')),
    CONSTRAINT CK_SANCION_VIGENTE  CHECK (VIGENTE IN (0, 1))
);

COMMENT ON TABLE  SANCION                IS 'Sanciones de moderacion: temas o comentarios baneados por incumplir una norma';
COMMENT ON COLUMN SANCION.CONTENIDO_ID   IS 'ID del TEMA o COMENTARIO en foro-temas (sin clave foranea entre servicios)';
COMMENT ON COLUMN SANCION.AUTOR_ID       IS 'Autor del contenido sancionado, informado por foro-temas';
COMMENT ON COLUMN SANCION.VIGENTE        IS '1 mientras el contenido sigue baneado, 0 si la sancion se levanto';

CREATE SEQUENCE SANCION_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE INDEX IX_SANCION_CONTENIDO ON SANCION (TIPO_CONTENIDO, CONTENIDO_ID);
CREATE INDEX IX_SANCION_AUTOR     ON SANCION (AUTOR_ID);
