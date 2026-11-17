-- ===========================================================================
--  Microservicio foro-moderacion
--  Elimina SUS objetos del esquema FORO para poder recrearlos desde cero.
--  El orden importa por las claves foraneas. No toca las tablas de los otros
--  microservicios.
-- ===========================================================================
DROP TABLE SANCION CASCADE CONSTRAINTS;
DROP TABLE REPORTE CASCADE CONSTRAINTS;
DROP TABLE NORMA   CASCADE CONSTRAINTS;

DROP SEQUENCE SANCION_SEQ;
DROP SEQUENCE REPORTE_SEQ;
DROP SEQUENCE NORMA_SEQ;
