-- ===========================================================================
--  Microservicio foro-moderacion
--  Elimina SUS objetos del esquema FORO para poder recrearlos desde cero.
-- ===========================================================================
DROP TABLE NORMA CASCADE CONSTRAINTS;
DROP SEQUENCE NORMA_SEQ;
