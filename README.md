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
