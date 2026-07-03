# Gestion Academica API

API REST en Spring Boot + PostgreSQL.

## Stack

- Spring Boot 4.1.0 + Java 26
- PostgreSQL 18
- Maven (wrapper incluido)
- Docker + Docker Compose

---

## Opcion 1: Ejecutar con Docker (recomendada para dev)

Levanta PostgreSQL + backend con un solo comando. Las tablas las crea Hibernate al arrancar y los datos de prueba se siembran automaticamente la primera vez.

### Requisitos

- Docker Desktop instalado y corriendo
- Puerto 5432 (PostgreSQL) y 8080 (backend) libres

### Comandos

```bash
# Construir imagen y levantar servicios (primera vez, ~3-5 min por la build)
docker compose up -d --build

# Ver logs en tiempo real
docker compose logs -f backend

# Verificar estado
docker compose ps

# Probar que respondio
curl http://localhost:8080/api/swagger-ui/index.html

# Detener (conservando datos en volumen)
docker compose down

# Detener Y borrar datos (reset completo)
docker compose down -v
```

### Verificacion rapida (smoke test)

Despues de `docker compose up -d --build`, esperar ~15 s y correr:

```bash
# 1. Ambos contenedores deben estar "healthy"
docker ps --filter "name=gestion-"
# gestion-backend    Up X minutes (healthy)
# gestion-postgres   Up X minutes (healthy)

# 2. Logs del backend deben terminar con "Started GestionAcademica..."
docker compose logs backend | tail -5

# 3. Swagger UI responde 200
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/swagger-ui/index.html
# Esperado: 200

# 4. OpenAPI docs responde 200
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/v3/api-docs
# Esperado: 200

# 5. Generar token dev (sin auth previa)
curl -X POST http://localhost:8080/api/dev/token \
  -H "Content-Type: application/json" \
  -d '{"identificadorCorporativo":"u202010101","tipoUsuario":"ESTUDIANTE","nombre":"Ana","apellidos":"Perez"}'
# Esperado: JSON con campo "token" (JWT firmado)

# 6. Verificar datos sembrados (solo primera vez, cuando el volumen esta vacio)
docker exec gestion-postgres psql -U postgres -d gestionbd \
  -c "SELECT count(*) AS usuarios FROM usuario UNION ALL SELECT count(*), 'materias' FROM materia;"
# Esperado: 7 usuarios, 3 materias
```

Si todos los pasos dan el valor esperado, el sistema esta listo para usar con Postman.

### Variables de entorno para Docker (opcionales)

Todas tienen defaults razonables para desarrollo local. Para uso real **definir valores propios** y no commitearlos. Crear un archivo `.env` en la raiz del proyecto:

```bash
# .env (no commitear, agregar a .gitignore)
# Definir valores propios. NO usar los defaults de docker-compose en ningun
# entorno expuesto a internet.
POSTGRES_DB=
POSTGRES_USER=
POSTGRES_PASSWORD=
POSTGRES_PORT=5432
BACKEND_PORT=8080
JPA_DDL_AUTO=update
```

> [!warning] Contraseñas
> Aunque el `docker-compose.yml` define defaults para que `docker compose up` funcione sin configurar nada, **no usar esos defaults** en staging ni produccion. Definir contrasenas fuertes en el `.env` o via variables de entorno del orquestador (Kubernetes secrets, AWS SSM, etc.). Los defaults exactos de desarrollo (solo para `docker compose up` sin `.env`) viven en `docker-compose.yml` y `application-docker.yml` del repositorio.

> [!note] Sobre JPA_DDL_AUTO=update
> `update` crea las tablas si no existen y agrega columnas nuevas sin borrar datos. **Solo para desarrollo**. Para produccion se debe usar Flyway (ver gaps doc, Bloque 6.1).

### Volumenes persistentes

| Volumen | Contenido | Persistencia |
|---|---|---|
| `gestion-postgres-data` | Datos de PostgreSQL | Persiste entre `docker compose down` |
| `gestion-backend-uploads` | Archivos subidos (futuro Bloque 12) | Persiste entre `docker compose down` |

Para inspeccionar los datos sembrados:

```bash
docker exec -it gestion-postgres psql -U postgres -d gestionbd -c "SELECT * FROM usuario;"
```

### Re-seedear despues de cambios en seed.sql

```bash
docker compose down -v   # Borra volumen de postgres
docker compose up -d     # Re-crea y ejecuta seed.sql automaticamente
```

### Troubleshooting

| Sintoma | Causa probable | Solucion |
|---|---|---|
| `docker compose` no se reconoce | Docker Desktop no instalado o no corriendo | Instalar Docker Desktop, esperar a que el icono de la ballena deje de animarse |
| `Bind for 0.0.0.0:8080 failed: port is already allocated` | Otro proceso usa el puerto (ej: `java.exe` de una corrida nativa anterior) | Windows: `netstat -ano \| findstr :8080` y `taskkill /PID <pid> /F`. O cambiar `BACKEND_PORT=8090` en `.env` |
| `Bind for 0.0.0.0:5432 failed` | PostgreSQL local corriendo en el host | Detener el servicio de PostgreSQL local, o cambiar `POSTGRES_PORT=5433` en `.env` |
| `gestion-backend` queda en `Restarting` o `Exit 1` | Error de build o de conexion a Postgres | `docker compose logs backend` para ver el error especifico |
| `gestion-backend` queda en `(unhealthy)` | El healthcheck no responde 200 en Swagger UI | Verificar logs: probablemente tablas no creadas o `DevTokenController` no carga |
| `curl /api/dev/token` devuelve 404 | Falta el perfil `dev` (controller tiene `@Profile({"dev","default"})`) | Verificar `.env` tiene `SPRING_PROFILES_ACTIVE=dev,docker` y rebuildear |
| `curl /api/dev/token` devuelve 500 con `"relation X does not exist"` | Hibernate no creo las tablas: `JPA_DDL_AUTO=none` | Crear `.env` con `JPA_DDL_AUTO=update` y `docker compose up -d --build` |
| Cualquier endpoint protegido devuelve 401 | JWT no enviado o invalido | Agregar header `Authorization: Bearer <token>` (generar primero con `/api/dev/token`) |
| Cualquier endpoint publico (`/api/materias`) devuelve 500 | Falta `JPA_DDL_AUTO=update` (idem arriba) | Crear `.env` con `JPA_DDL_AUTO=update` y rebuild |
| Volumen postgres tiene datos viejos de otra rama | El volumen se reutiliza entre rebuilds | `docker compose down -v && docker compose up -d --build` (resetea todo) |
| `docker compose up` tarda 10+ min | Primera vez descargando imagenes base (`eclipse-temurin:26`, `postgres:18-alpine`) y dependencias Maven | Normal en la primera corrida. Las siguientes son ~30 s |

### Comandos utiles de referencia

```bash
# Ver logs solo del backend (ultimas 100 lineas)
docker compose logs --tail 100 backend

# Seguir logs en vivo filtrados por errores
docker compose logs -f backend | grep -iE "error|exception"

# Entrar al contenedor del backend (shell)
docker exec -it gestion-backend sh

# Entrar a PostgreSQL directamente
docker exec -it gestion-postgres psql -U postgres -d gestionbd

# Inspeccionar variables de entorno del backend
docker inspect gestion-backend --format '{{range .Config.Env}}{{println .}}{{end}}'

# Reconstruir solo la imagen del backend (sin tocar Postgres)
docker compose build backend

# Forzar recreacion del backend (baja y sube)
docker compose up -d --force-recreate backend

# Ver tamanio de imagenes/volumenes
docker images gestion-academica-api
docker volume ls | grep gestion
```

---

## Opcion 2: Ejecutar nativo (sin Docker)

Requiere Java 26 + Maven + PostgreSQL local ya configurado.

### Variables de entorno

| Variable | Obligatoria | Default | Descripcion |
| --- | --- | --- | --- |
| `DB_URL` | No | `jdbc:postgresql://localhost:5432/gestionbd` | JDBC URL de la BD |
| `DB_USERNAME` | **Si** | _(vacio)_ | Usuario de PostgreSQL |
| `DB_PASSWORD` | **Si** | _(vacio)_ | Contrasena del usuario |
| `JPA_DDL_AUTO` | No | `none` | Estrategia DDL de Hibernate |

### Configurar (Windows, permanente)

**CMD:**

```cmd
setx DB_URL "jdbc:postgresql://localhost:5432/gestionbd"
setx DB_USERNAME "tu_usuario_postgres"
setx DB_PASSWORD "tu_password"
setx JPA_DDL_AUTO "update"
```

**PowerShell:**

```powershell
[Environment]::SetEnvironmentVariable("DB_URL", "jdbc:postgresql://localhost:5432/gestionbd", "User")
[Environment]::SetEnvironmentVariable("DB_USERNAME", "tu_usuario_postgres", "User")
[Environment]::SetEnvironmentVariable("DB_PASSWORD", "tu_password", "User")
[Environment]::SetEnvironmentVariable("JPA_DDL_AUTO", "update", "User")
```

### Ejecutar

```bash
mvn spring-boot:run
```

Y luego correr manualmente el sembrado desde `scripts/seed.sql`:

```bash
psql -U postgres -d gestionbd -f scripts/seed.sql
```

---

## Swagger UI

Disponible en `http://localhost:8080/api/swagger-ui/index.html` una vez levantado el backend.

## Postman

Importar `gestion-academica-api.postman_collection.json` (ubicado en la raiz del monorepo, no en este subproyecto).
