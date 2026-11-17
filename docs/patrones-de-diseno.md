# Patrones de diseño en foro-moderacion

## Arquetipo ≠ patrón de diseño

- El **arquetipo** (`microservicio-arquetipo`) es la *plantilla del proyecto*:
  carpetas, dependencias, seguridad, perfiles, Dockerfile, CI. Decide **cómo
  empieza** cada microservicio.
- Un **patrón de diseño** es una *solución reutilizable a un problema concreto
  dentro del código*. Decide **cómo se resuelve** algo específico.

Este microservicio usa los dos: se generó con el arquetipo y resuelve su
problema central con **Strategy + Factory**, apoyados en un **Adapter**.

---

## El problema

Un moderador puede banear **temas** y **comentarios**. Cada tipo se aplica
distinto en `foro-temas` (otra ruta, otro efecto: un tema baneado desaparece
con sus comentarios; un comentario baneado se oculta pero el tema sigue). La
solución ingenua llena el servicio de condicionales:

```java
// lo que NO se hizo
if (tipo == TEMA) { ...PUT /api/moderacion/temas/{id}... }
else if (tipo == COMENTARIO) { ...PUT /api/moderacion/comentarios/{id}... }
// y lo mismo otra vez para restituir, y otra vez si mañana se moderan perfiles
```

## Strategy

```
                 «interface»
            EstrategiaDeModeracion
            ───────────────────────
            + tipo(): TipoContenido
            + banear(id, motivo, jwt)
            + restituir(id, jwt)
                    ▲          ▲
        ┌───────────┘          └────────────┐
 ModeracionDeTema               ModeracionDeComentario
 TEMA                           COMENTARIO
 /api/moderacion/temas/{id}     /api/moderacion/comentarios/{id}
```

Cada estrategia encapsula **cómo** se modera su tipo. Archivos:
`estrategia/EstrategiaDeModeracion.java`, `ModeracionDeTema.java`,
`ModeracionDeComentario.java`.

## Factory

```java
@Component
public class FabricaDeEstrategiasDeModeracion {
    public FabricaDeEstrategiasDeModeracion(List<EstrategiaDeModeracion> disponibles) { ... }
    public EstrategiaDeModeracion para(TipoContenido tipo) { ... }
}
```

- Spring inyecta **todas** las estrategias; la fábrica las indexa en un `EnumMap`.
- Valida al arrancar: si a un `TipoContenido` le falta su estrategia, o hay dos,
  la aplicación **no levanta**. El error se ve al desplegar, no cuando un
  moderador intenta banear.
- El servicio queda así, sin condicionales:

```java
EstrategiaDeModeracion estrategia = fabrica.para(solicitud.tipoContenido());
ContenidoModerado contenido = estrategia.banear(id, motivo, autorizacion);
```

**Abierto/cerrado:** para moderar un tipo nuevo (por ejemplo, perfiles) se
agrega el valor al enum y una clase `@Component` nueva. `SancionServiceImpl` no
se toca.

## Adapter

```
SancionServiceImpl ──▶ EstrategiaDeModeracion ──▶ ClienteForoTemas ──HTTP──▶ foro-temas
                                                   (Adapter)
                                         404        → RecursoNoEncontradoException  → 404
                                         401 / 403  → OperacionNoPermitidaException → 403
                                         5xx / red  → ServicioNoDisponibleException → 503
```

`ClienteForoTemas` adapta la interfaz HTTP de otro servicio a una llamada Java
con tipos propios (`OrdenDeModeracion`, `ContenidoModerado`). Las estrategias y
el servicio no saben que hay HTTP de por medio, y en las pruebas el adaptador se
reemplaza por un *mock* (`@MockitoBean`) o se prueba solo con
`MockRestServiceServer`.

## Otros patrones presentes

| Patrón | Dónde | Viene de |
|---|---|---|
| Arquitectura en capas (Controller → Service → Repository) | todo el proyecto | arquetipo |
| Repository | `repository/*` (Spring Data JPA) | arquetipo |
| DTO | `dto/*`: ninguna entidad JPA sale por la API | arquetipo |
| Singleton + Inyección de dependencias | todos los `@Service`, `@Component` (un bean por contexto) | arquetipo / Spring |
| Chain of Responsibility | `SecurityFilterChain` + `FiltroDeAutenticacionJwt` | arquetipo |
| Compensación | `SancionServiceImpl.sancionar`: si falla el guardado tras banear, se restituye el contenido | este servicio |

## Cómo se prueba cada uno

| Patrón | Prueba |
|---|---|
| Factory | `FabricaDeEstrategiasDeModeracionTest`: tipo → estrategia; falta o duplicado → no arranca |
| Strategy | `SancionControllerTest.comentarioUsaSuEstrategia`: un COMENTARIO llama a la ruta de comentarios |
| Adapter | `ClienteForoTemasTest`: petición armada y traducción de 404 / 403 / 500 |
