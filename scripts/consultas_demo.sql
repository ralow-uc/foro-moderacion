-- ===========================================================================
--  Microservicio foro-moderacion
--  Consultas para demostrar en vivo que los datos estan en Oracle.
--
--  Ejecutar con el usuario FORO desde Database Actions o con:
--      ./scripts/sql.sh foro scripts/consultas_demo.sql
--
--  Pensadas para el video: se corren despues de reportar y banear un tema por
--  Postman, para evidenciar que la decision quedo en las dos bases.
-- ===========================================================================


-- 1. Registros por tabla de este microservicio
SELECT 'NORMA' AS TABLA, COUNT(*) AS REGISTROS FROM NORMA
UNION ALL SELECT 'REPORTE', COUNT(*) FROM REPORTE
UNION ALL SELECT 'SANCION', COUNT(*) FROM SANCION;


-- 2. Bandeja del moderador: reportes pendientes con la norma citada
SELECT r.ID, r.TIPO_CONTENIDO, r.CONTENIDO_ID, n.CODIGO AS NORMA, r.REPORTANTE_NOMBRE, r.DETALLE, r.FECHA_CREACION
  FROM REPORTE r JOIN NORMA n ON n.ID = r.NORMA_ID
 WHERE r.ESTADO = 'PENDIENTE'
 ORDER BY r.FECHA_CREACION;


-- 3. Ultimas sanciones, vigentes primero
SELECT s.ID, s.TIPO_CONTENIDO, s.CONTENIDO_ID, s.AUTOR_NOMBRE, n.CODIGO AS NORMA,
       s.MODERADOR_NOMBRE, s.VIGENTE, s.FECHA_CREACION, s.LEVANTADA_POR
  FROM SANCION s JOIN NORMA n ON n.ID = s.NORMA_ID
 ORDER BY s.VIGENTE DESC, s.FECHA_CREACION DESC
 FETCH FIRST 10 ROWS ONLY;


-- 4. El mismo baneo visto desde foro-temas: el tema quedo oculto con su motivo.
--    (TEMA pertenece a foro-temas; esta consulta solo lee, para la evidencia.)
SELECT t.ID, t.TITULO, t.AUTOR_NOMBRE, t.BANEADO, t.MOTIVO_BANEO
  FROM TEMA t
 WHERE t.BANEADO = 1;


-- 5. Reincidencia: autores con mas sanciones
SELECT AUTOR_NOMBRE, COUNT(*) AS SANCIONES, SUM(VIGENTE) AS VIGENTES
  FROM SANCION
 GROUP BY AUTOR_NOMBRE
 ORDER BY SANCIONES DESC;
