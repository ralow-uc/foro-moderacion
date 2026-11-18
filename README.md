# foro-moderacion

Microservicio de **gestión de publicaciones y comentarios por parte de los
moderadores** del caso *Sistema de Foros en Línea* (CuboClub, foro de
speedcubing): normas del foro, reportes de los usuarios y **sanciones que banean
temas o comentarios** que incumplen las normas.

Es el **tercer microservicio** del caso y el primero **generado con el
arquetipo propio** [`microservicio-arquetipo`](../microservicio-arquetipo).

| | |
|---|---|
| Puerto | `8083` |
| Responsabilidad | Normas, reportes y sanciones; aplica los baneos en `foro-temas` |
| Tablas propias | `NORMA`, `REPORTE`, `SANCION` (esquema `FORO`, Oracle Cloud) |
| Patrones de diseño | **Strategy + Factory** (moderación por tipo de contenido), **Adapter** (cliente HTTP de `foro-temas`) |
| Versión | `v1.0.1` |
| Arquetipo | `com.foro:microservicio-arquetipo:1.0.0` |

**Asignatura:** Desarrollo Full Stack III (DSY2205) · Experiencia 2, Semana 5
· Actividad sumativa *Desarrollando nuestro proyecto bajo lineamientos empresariales*.

---

## 1. Los tres microservicios

| Repositorio | Puerto | Responsabilidad | Requisito del caso |
|---|---|---|---|
| `foro-usuarios` | 8081 | Cuentas, roles, inicio de sesión. **Emite** el JWT. | Control de usuarios e inicio de sesión |
| `foro-temas` | 8082 | Categorías, búsqueda, publicación y comentarios. Aplica los baneos en sus tablas. | Búsqueda, visualización, publicación y comentarios |
| **`foro-moderacion`** (este) | 8083 | Normas, reportes y sanciones. | Gestión por un administrador: banear temas o comentarios |

Detalle y diagramas en [`docs/arquitectura.md`](docs/arquitectura.md).

## 2. Cómo se generó

```bash
# 1. Instalar el arquetipo (una vez)
cd ../microservicio-arquetipo && ./mvnw clean install

# 2. Generar el proyecto
mvn archetype:generate -DarchetypeCatalog=local \
  -DarchetypeGroupId=com.foro -DarchetypeArtifactId=microservicio-arquetipo -DarchetypeVersion=1.0.0 \
  -DgroupId=com.foro -DartifactId=foro-moderacion -Dpackage=com.foro.moderacion -Dversion=1.0.0 \
  -DnombreClase=ForoModeracion -Dentidad=Norma -Drecurso=normas -Dtabla=NORMA -Dpuerto=8083 \
  -Ddescripcion="Microservicio de moderacion del foro" -DinteractiveMode=false
```

El primer commit del repositorio es **exactamente** lo que produjo el arquetipo
(con 5 pruebas en verde). Sobre esa base se agregaron, en ramas `feature/*`:
el código y la gravedad de las normas, los reportes, las sanciones con sus
patrones, los datos iniciales y esta documentación (`git log --graph`).

## 3. Estructura

```
src/main/java/com/foro/moderacion/
├── controller/    NormaController, ReporteController, SancionController, EstadoController
├── service/       contratos + impl con las reglas de negocio
├── repository/    NormaRepository, ReporteRepository, SancionRepository (JPA)
├── model/         Norma, Reporte, Sancion + enums Gravedad, TipoContenido, EstadoReporte
├── dto/           entrada/salida, nunca se expone una entidad JPA
├── estrategia/    ★ Strategy + Factory: ModeracionDeTema, ModeracionDeComentario, Fabrica...
├── cliente/       ★ Adapter: ClienteForoTemas (RestClient hacia foro-temas)
├── security/      validación del JWT de foro-usuarios (del arquetipo)
├── exception/     excepciones propias + manejador global (ProblemDetail)
└── config/        datos de ejemplo del perfil local
scripts/           00 esquema · 01 tablas · 02 datos · 99 borrar · consultas_demo · sql.sh · run.sh
postman/           colección de los 3 microservicios + entornos local y Render
Dockerfile         imagen multi-etapa (del arquetipo)
```

## 4. Patrones de diseño

Explicados con diagramas en [`docs/patrones-de-diseno.md`](docs/patrones-de-diseno.md).

- **Strategy** — `EstrategiaDeModeracion` con una implementación por tipo de
  contenido. Cada una sabe cómo banear y restituir su tipo en `foro-temas`.
- **Factory** — `FabricaDeEstrategiasDeModeracion` recibe todas las estrategias
  e indexa por `TipoContenido`. El servicio pide `fabrica.para(tipo)` y no tiene
  ningún `if (tipo == TEMA)`. Agregar un tipo nuevo es agregar una clase.
- **Adapter** — `ClienteForoTemas` convierte la API HTTP de `foro-temas` en una
  llamada Java y sus códigos (`404`, `401/403`, `5xx`, sin red) en las
  excepciones propias de este servicio.
- Además, los heredados del arquetipo: arquitectura en capas, **Repository**
  (Spring Data), **DTO**, inyección de dependencias (beans **Singleton**) y la
  cadena de filtros de seguridad (**Chain of Responsibility**).

## 5. Base de datos (Oracle Cloud)

```bash
./scripts/sql.sh foro scripts/01_crear_tablas.sql      # NORMA, REPORTE, SANCION
./scripts/sql.sh foro scripts/02_datos_iniciales.sql   # 6 normas, 4 reportes, 3 sanciones
```

**Antes de desplegar `foro-temas` v2.0.0** hay que correr su migración
`foro-temas/scripts/03_moderacion_de_contenido.sql`, que agrega `BANEADO` y
`MOTIVO_BANEO` a `TEMA` y `COMENTARIO`.

| Tabla | Registros iniciales | Relación |
|---|---|---|
| `NORMA` | 6 (N1–N6) | — |
| `REPORTE` | 4 (2 pendientes, 2 descartados) | `NORMA_ID → NORMA` |
| `SANCION` | 3 (históricas, levantadas) | `NORMA_ID → NORMA`, `REPORTE_ID → REPORTE` |

`CONTENIDO_ID` apunta a `TEMA` o `COMENTARIO` de `foro-temas` **sin clave
foránea**: cada microservicio es dueño de sus tablas.

## 6. Ejecución

```bash
./mvnw spring-boot:run          # perfil local (H2, siembra datos), puerto 8083
./scripts/run.sh                # perfil oracle, credenciales del Llavero
./scripts/test.sh               # 29 pruebas
docker build -t foro-moderacion . && docker run -p 8083:8083 -e SPRING_PROFILES_ACTIVE=local foro-moderacion
```

Para banear necesita a `foro-temas` arriba (`FORO_TEMAS_URL`, por omisión
`http://localhost:8082`). Para obtener tokens, a `foro-usuarios`. El despliegue
de los tres con Docker Compose está en [`../foro-despliegue`](../foro-despliegue).

## 7. API REST

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| `GET` | `/api/normas` | Normas del foro (`?soloActivos=true`) | público |
| `GET` | `/api/normas/{id}` | Detalle | público |
| `POST` `PUT` `DELETE` | `/api/normas[/{id}]` | CRUD de normas. Una norma citada no se borra (`409`) | `ADMIN` |
| `POST` | `/api/reportes` | Reportar un tema o comentario | con token |
| `GET` | `/api/reportes/mios` | Mis reportes | con token |
| `GET` | `/api/reportes?estado=` | Bandeja de moderación | `MODERADOR` · `ADMIN` |
| `GET` | `/api/reportes/{id}` | Detalle | `MODERADOR` · `ADMIN` |
| `PUT` | `/api/reportes/{id}/descartar` | Cerrar sin sanción | `MODERADOR` · `ADMIN` |
| `POST` | `/api/sanciones` | **Banear** un tema o comentario | `MODERADOR` · `ADMIN` |
| `GET` | `/api/sanciones?vigente=&tipo=` | Sanciones con filtros | `MODERADOR` · `ADMIN` |
| `GET` | `/api/sanciones/{id}` | Detalle | `MODERADOR` · `ADMIN` |
| `GET` | `/api/sanciones/autor/{autorId}` | Historial de un autor | `MODERADOR` · `ADMIN` |
| `PUT` | `/api/sanciones/{id}/levantar` | Levantar la sanción y restituir el contenido | **solo `ADMIN`** |
| `GET` | `/api/estado` | Estado y motor de base de datos | público |

Códigos: `201` creado · `400` datos inválidos (detalle por campo) · `401` sin
token · `403` sin privilegio · `404` no existe (también si `foro-temas` no
encuentra el contenido) · `409` estado inválido o duplicado · `503`
`foro-temas` no disponible.

### Qué pasa al banear

```
moderadora ──POST /api/sanciones──▶ foro-moderacion
                                     1. valida norma, reporte y que no haya sanción vigente
                                     2. fabrica.para(TEMA) → ModeracionDeTema
                                     3. ClienteForoTemas ──PUT /api/moderacion/temas/{id}──▶ foro-temas
                                                           (reenvía el JWT de la moderadora)    BANEADO = 1
                                     4. guarda SANCION con el autor que devolvió foro-temas
                                     5. marca ACEPTADOS los reportes pendientes de ese tema
◀──201 + sanción────────────────────
GET /api/temas/{id} en foro-temas ──▶ 410 Gone "Contenido moderado"
```

## 8. Roles

| Operación | `USUARIO` | `MODERADOR` | `ADMIN` |
|---|---|---|---|
| Ver normas | sí | sí | sí |
| Reportar contenido, ver mis reportes | sí | sí | sí |
| Bandeja de reportes, descartar | no | **sí** | sí |
| Banear temas y comentarios | no | **sí** | sí |
| Levantar sanciones | no | no | **sí** |
| Crear, editar o borrar normas | no | no | **sí** |

## 9. Pruebas

```bash
./scripts/test.sh     # 29 pruebas, H2 en modo Oracle, sin red
```

| Clase | Pruebas | Qué cubre |
|---|---|---|
| `NormaControllerTest` | 5 | CRUD, 401/403, validaciones, código duplicado (generada por el arquetipo y adaptada) |
| `ReporteControllerTest` | 6 | Reportar, duplicados, bandeja por rol, descartar, enums inválidos, norma citada |
| `SancionControllerTest` | 8 | Banear tema y comentario (estrategia correcta), JWT reenviado, reportes aceptados, duplicado, 404 de foro-temas, levantar solo ADMIN, filtros |
| `ClienteForoTemasTest` | 6 | Adapter: petición armada; traducción de 404 / 403 / 500, host inalcanzable y respuesta sin cuerpo |
| `FabricaDeEstrategiasDeModeracionTest` | 3 | Factory: estrategia por tipo, falta o duplicado de estrategia |
| `ForoModeracionApplicationTests` | 1 | Arranque del contexto |

## 10. Postman

`postman/foro-online.postman_collection.json` recorre los **tres** servicios
(peticiones 00–59, 108 aserciones). Las 25–59 son de este microservicio. Para la
nube, importa `postman/foro-render.postman_environment.json`, que ya trae las
URL públicas de Render (`https://foro-moderacion.onrender.com`, etc.).
Evidencia: `docs/registros/`.

## 11. Ramas

Trunk Based Development, ver [CONTRIBUTING.md](CONTRIBUTING.md).
