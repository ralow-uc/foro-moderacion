# foro-moderacion

Microservicio de moderacion del foro: normas, reportes y sanciones sobre temas y comentarios.

Generado con el arquetipo **`com.foro:microservicio-arquetipo:1.0.0`**.

| | |
|---|---|
| Puerto | `8083` |
| Paquete | `com.foro.moderacion` |
| Entidad de ejemplo | `Norma` → tabla `NORMA` → `/api/normas` |

 Estructura

```
src/main/java/.../
├── controller/   NormaController, EstadoController
├── service/      contrato + impl con la logica de negocio
├── repository/   NormaRepository (JPA)
├── model/        Norma
├── dto/          NormaRequest, NormaResponse
├── security/     validacion del JWT de foro-usuarios
└── exception/    excepciones propias + manejador global (ProblemDetail)
scripts/          SQL de la tabla, ejecucion contra Oracle y pruebas
Dockerfile        imagen multi-etapa, el Wallet se monta al ejecutar
.github/          integracion continua
```

 Ejecucion

```bash
chmod +x mvnw scripts/*.sh        # el arquetipo no conserva el bit de ejecucion
./mvnw spring-boot:run            # perfil local (H2), sin credenciales
./scripts/run.sh                  # perfil oracle, credenciales del Llavero
./scripts/test.sh                 # pruebas automatizadas
```

 Docker

```bash
docker build -t foro-moderacion .
docker run -p 8083:8083 -e SPRING_PROFILES_ACTIVE=local foro-moderacion
```

 API de ejemplo

| Metodo | Ruta | Acceso |
|---|---|---|
| `GET` | `/api/normas` | publico |
| `GET` | `/api/normas/{id}` | publico |
| `POST` | `/api/normas` | `ADMIN` |
| `PUT` | `/api/normas/{id}` | `ADMIN` |
| `DELETE` | `/api/normas/{id}` | `ADMIN` |
| `GET` | `/api/estado` | publico |

 Ramas

Trunk Based Development: ver [CONTRIBUTING.md](CONTRIBUTING.md).
