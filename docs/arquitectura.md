# Arquitectura del Sistema de Foros en Línea (S5)

## Visión general

```
                         ┌──────────────────────────────┐
                         │   foro-web  (Angular 22)     │  datos en arreglos/signals,
                         │   nginx :80                  │  independiente del backend
                         └──────────────────────────────┘    (lo pide la actividad)

  Postman ─────┬──────────────────────┬──────────────────────────┐
               ▼                      ▼                          ▼
  ┌────────────────────────┐ ┌────────────────────────┐ ┌──────────────────────────┐
  │ foro-usuarios  :8081   │ │ foro-temas  :8082      │ │ foro-moderacion  :8083   │
  │ cuentas, roles, login  │ │ categorías, temas,     │ │ normas, reportes,        │
  │ EMITE el JWT           │ │ comentarios            │ │ sanciones                │
  │                        │ │ valida el JWT          │ │ valida el JWT            │
  │                        │ │ /api/moderacion/* ◀────┼─┤ ClienteForoTemas (HTTP,  │
  │                        │ │ (BANEADO = 1/0)        │ │ reenvía el JWT)          │
  └───────────┬────────────┘ └───────────┬────────────┘ └────────────┬─────────────┘
              │      JWT HS256, secreto compartido (FORO_JWT_SECRETO)│
              ▼                          ▼                           ▼
  ┌──────────────────────────────────────────────────────────────────────────────┐
  │ Oracle Autonomous Database · FULLSTACK-3 · esquema FORO                      │
  │ ROL USUARIO USUARIO_ROL │ CATEGORIA TEMA COMENTARIO │ NORMA REPORTE SANCION  │
  └──────────────────────────────────────────────────────────────────────────────┘
```

| Repositorio | Versión | Tablas propias | Cómo se construyó |
|---|---|---|---|
| `foro-usuarios` | v2.0.0 | `ROL`, `USUARIO`, `USUARIO_ROL` | S2; en S5 se le agregan Docker, CI y CORS configurable |
| `foro-temas` | v2.0.0 | `CATEGORIA`, `TEMA`, `COMENTARIO` | S2; en S5, columnas y endpoints de moderación |
| `foro-moderacion` | v1.0.1 | `NORMA`, `REPORTE`, `SANCION` | **generado con `microservicio-arquetipo`** |
| `microservicio-arquetipo` | v1.0.0 | — | extraído de la estructura común de los dos primeros |
| `foro-web` | v1.0.0 | — | Angular 22 + Bootstrap 5.3 |

## Comunicación

- **JWT**: `foro-usuarios` lo firma; los otros dos lo validan solos con el mismo
  secreto. Ninguno consulta a `foro-usuarios` por petición.
- **Moderación → foro**: única llamada entre servicios. `foro-moderacion` pide
  el baneo a `foro-temas` (`PUT /api/moderacion/temas/{id}` o `/comentarios/{id}`)
  **reenviando el JWT del moderador**. `foro-temas` vuelve a verificar que tenga
  rol `MODERADOR` o `ADMIN`: no existe un usuario técnico con privilegios extra.
- **Propiedad de los datos**: `foro-moderacion` nunca escribe `TEMA` ni
  `COMENTARIO`. `foro-temas` es su único dueño; moderación guarda solo el
  registro de la decisión (`SANCION`).

### Por qué una llamada HTTP y no escribir directo en TEMA

Las dos bases son el mismo esquema `FORO`, así que técnicamente
`foro-moderacion` podría hacer `UPDATE TEMA SET BANEADO = 1`. No se hizo porque
ataría el modelo de datos de los dos servicios: un cambio en `TEMA` rompería la
moderación. Con la API, `foro-temas` decide qué significa "baneado" (ocultar de
búsquedas, `410` en el detalle, no contar comentarios) y moderación solo pide.

### Consistencia

1. Se valida todo lo local **antes** de llamar a `foro-temas`.
2. Si `foro-temas` falla, no se registra la sanción (`404` / `503` al cliente).
3. Si `foro-temas` baneó pero falla el guardado local, se **compensa**
   restituyendo el contenido.

## Moderación del contenido en foro-temas

| | Visible | Búsqueda / listados | Detalle `GET /api/temas/{id}` | Conteos |
|---|---|---|---|---|
| Tema normal | sí | sí | `200` | sí |
| Tema baneado | no | excluido | **`410 Gone`** + motivo | excluido |
| Comentario baneado | no | — | no aparece en `comentarios` | excluido |

El contenido baneado **no se borra** (`BANEADO = 1`, `MOTIVO_BANEO`): queda como
evidencia y se restituye si un `ADMIN` levanta la sanción.

## Roles

| Operación | Anónimo | `USUARIO` | `MODERADOR` | `ADMIN` |
|---|---|---|---|---|
| Leer foro y normas | sí | sí | sí | sí |
| Publicar, comentar, editar lo propio | no | sí | sí | sí |
| Reportar contenido | no | sí | sí | sí |
| Revisar reportes, banear contenido | no | no | **sí** | sí |
| Levantar sanciones, administrar normas | no | no | no | **sí** |
| Gestionar cuentas y roles, categorías | no | no | no | **sí** |

## Despliegue en la nube (Render)

Los cuatro componentes (tres microservicios y `foro-web`) corren como
contenedores Docker en **Render**, que construye cada imagen con el Dockerfile
de su repositorio de GitHub (Blueprint `render.yaml` en `foro-despliegue`).
`foro-moderacion` llama a `foro-temas` por su URL pública HTTPS. El Wallet de
Oracle **no va en ninguna imagen**: el driver JDBC lo recibe en memoria, en
base64, desde una variable secreta de Render. Ver
[`../foro-despliegue`](../../foro-despliegue/README.md).

## Ramas

Un repositorio por microservicio y **Trunk Based Development**: `main` siempre
desplegable, ramas `feature/*` de vida corta integradas con `--no-ff`, CI en
cada push y versiones como etiquetas. Ver `CONTRIBUTING.md`.
