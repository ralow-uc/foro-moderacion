-- ===========================================================================
--  Microservicio foro-moderacion
--  Datos iniciales de SUS tablas.
--
--  Ejecutar con el usuario FORO, despues de scripts/01_crear_tablas.sql:
--    ./scripts/sql.sh foro scripts/02_datos_iniciales.sql
--
--  Quedan:  NORMA 6 | REPORTE 4 | SANCION 3
--
--  CONTENIDO_ID y los ID de usuario son literales: apuntan a los datos que
--  siembran foro-temas (temas 1-9, comentarios 1-13) y foro-usuarios
--  (1 admin | 2 moderadora | 3 feliks | 4 yiheng | 5 tymon). Este script tiene
--  que poder correr solo, sin leer tablas de otros microservicios.
--
--  Las tres sanciones de ejemplo estan LEVANTADAS (VIGENTE = 0): son historial.
--  Asi los datos de los dos servicios cuentan la misma historia y no queda
--  una sancion vigente sobre un contenido que foro-temas muestra como visible.
-- ===========================================================================


-- ---------------------------------------------------------------------------
-- 1. Normas del foro
-- ---------------------------------------------------------------------------
INSERT INTO NORMA (ID, CODIGO, NOMBRE, DESCRIPCION, GRAVEDAD) VALUES (NORMA_SEQ.NEXTVAL, 'N1',
 'Respeto entre cuberos', 'Trata a los demás con respeto. Nada de insultos, burlas por tiempos lentos ni ataques personales.', 'GRAVE');

INSERT INTO NORMA (ID, CODIGO, NOMBRE, DESCRIPCION, GRAVEDAD) VALUES (NORMA_SEQ.NEXTVAL, 'N2',
 'Sin spam ni publicidad', 'No se permite publicidad de tiendas, enlaces de afiliados ni mensajes repetidos.', 'GRAVE');

INSERT INTO NORMA (ID, CODIGO, NOMBRE, DESCRIPCION, GRAVEDAD) VALUES (NORMA_SEQ.NEXTVAL, 'N3',
 'Contenido en la categoría correcta', 'Publica en la categoría que corresponde y busca antes de repetir una pregunta.', 'LEVE');

INSERT INTO NORMA (ID, CODIGO, NOMBRE, DESCRIPCION, GRAVEDAD) VALUES (NORMA_SEQ.NEXTVAL, 'N4',
 'Sin información personal', 'No publiques teléfonos, direcciones ni datos personales tuyos o de otras personas.', 'GRAVE');

INSERT INTO NORMA (ID, CODIGO, NOMBRE, DESCRIPCION, GRAVEDAD) VALUES (NORMA_SEQ.NEXTVAL, 'N5',
 'Sin contenido ofensivo', 'Prohibido el contenido discriminatorio, violento o sexual.', 'CRITICA');

INSERT INTO NORMA (ID, CODIGO, NOMBRE, DESCRIPCION, GRAVEDAD) VALUES (NORMA_SEQ.NEXTVAL, 'N6',
 'Sin piratería ni contenido ilegal', 'No compartas copias ilegales de software de cronometraje, videos o material con derechos.', 'CRITICA');


-- ---------------------------------------------------------------------------
-- 2. Reportes: dos pendientes para la bandeja del moderador y dos descartados
-- ---------------------------------------------------------------------------
INSERT INTO REPORTE (ID, TIPO_CONTENIDO, CONTENIDO_ID, NORMA_ID, REPORTANTE_ID, REPORTANTE_NOMBRE, DETALLE, ESTADO)
VALUES (REPORTE_SEQ.NEXTVAL, 'TEMA', 2, (SELECT ID FROM NORMA WHERE CODIGO = 'N3'), 3, 'feliks',
 'Esta pregunta ya se hizo varias veces este mes.', 'PENDIENTE');

INSERT INTO REPORTE (ID, TIPO_CONTENIDO, CONTENIDO_ID, NORMA_ID, REPORTANTE_ID, REPORTANTE_NOMBRE, DETALLE, ESTADO)
VALUES (REPORTE_SEQ.NEXTVAL, 'COMENTARIO', 12, (SELECT ID FROM NORMA WHERE CODIGO = 'N1'), 3, 'feliks',
 'El comentario desacredita la reseña sin argumentos.', 'PENDIENTE');

INSERT INTO REPORTE (ID, TIPO_CONTENIDO, CONTENIDO_ID, NORMA_ID, REPORTANTE_ID, REPORTANTE_NOMBRE, DETALLE, ESTADO,
                     FECHA_RESOLUCION, RESUELTO_POR)
VALUES (REPORTE_SEQ.NEXTVAL, 'TEMA', 9, (SELECT ID FROM NORMA WHERE CODIGO = 'N2'), 5, 'tymon',
 'Parece que promociona una marca de lubricante.', 'DESCARTADO', SYSTIMESTAMP, 'moderadora');

INSERT INTO REPORTE (ID, TIPO_CONTENIDO, CONTENIDO_ID, NORMA_ID, REPORTANTE_ID, REPORTANTE_NOMBRE, DETALLE, ESTADO,
                     FECHA_RESOLUCION, RESUELTO_POR)
VALUES (REPORTE_SEQ.NEXTVAL, 'COMENTARIO', 9, (SELECT ID FROM NORMA WHERE CODIGO = 'N4'), 5, 'tymon',
 'Pide datos para contactarlo.', 'DESCARTADO', SYSTIMESTAMP, 'moderadora');


-- ---------------------------------------------------------------------------
-- 3. Sanciones historicas (levantadas)
-- ---------------------------------------------------------------------------
INSERT INTO SANCION (ID, TIPO_CONTENIDO, CONTENIDO_ID, TITULO_CONTENIDO, AUTOR_ID, AUTOR_NOMBRE, NORMA_ID, MOTIVO,
                     MODERADOR_ID, MODERADOR_NOMBRE, VIGENTE, FECHA_LEVANTAMIENTO, LEVANTADA_POR, MOTIVO_LEVANTAMIENTO)
VALUES (SANCION_SEQ.NEXTVAL, 'TEMA', 8, 'Reseña del GAN 14 con MagLev', 3, 'feliks',
 (SELECT ID FROM NORMA WHERE CODIGO = 'N2'), 'Parecía publicidad encubierta de una tienda.',
 2, 'moderadora', 0, SYSTIMESTAMP, 'admin', 'Apelación aceptada: es una reseña personal, no publicidad.');

INSERT INTO SANCION (ID, TIPO_CONTENIDO, CONTENIDO_ID, TITULO_CONTENIDO, AUTOR_ID, AUTOR_NOMBRE, NORMA_ID, MOTIVO,
                     MODERADOR_ID, MODERADOR_NOMBRE, VIGENTE, FECHA_LEVANTAMIENTO, LEVANTADA_POR, MOTIVO_LEVANTAMIENTO)
VALUES (SANCION_SEQ.NEXTVAL, 'COMENTARIO', 6, 'Lo de no mezclar simétricos el mismo día es el mejor consejo del post.', 4, 'yiheng',
 (SELECT ID FROM NORMA WHERE CODIGO = 'N3'), 'Comentario fuera del tema de la publicación.',
 2, 'moderadora', 0, SYSTIMESTAMP, 'admin', 'Revisado: el comentario sí aporta al tema.');

INSERT INTO SANCION (ID, TIPO_CONTENIDO, CONTENIDO_ID, TITULO_CONTENIDO, AUTOR_ID, AUTOR_NOMBRE, NORMA_ID, MOTIVO,
                     MODERADOR_ID, MODERADOR_NOMBRE, VIGENTE, FECHA_LEVANTAMIENTO, LEVANTADA_POR, MOTIVO_LEVANTAMIENTO)
VALUES (SANCION_SEQ.NEXTVAL, 'COMENTARIO', 13, 'Coincido. En las piezas casi no se nota después del primer día.', 3, 'feliks',
 (SELECT ID FROM NORMA WHERE CODIGO = 'N2'), 'Mensaje repetido en varios temas.',
 1, 'admin', 0, SYSTIMESTAMP, 'admin', 'Era un único mensaje; el duplicado fue un error de la aplicación.');

COMMIT;


-- ---------------------------------------------------------------------------
-- 4. Verificacion
-- ---------------------------------------------------------------------------
SELECT 'NORMA' AS TABLA, COUNT(*) AS REGISTROS FROM NORMA
UNION ALL SELECT 'REPORTE', COUNT(*) FROM REPORTE
UNION ALL SELECT 'SANCION', COUNT(*) FROM SANCION;
