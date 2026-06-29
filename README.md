# Gestion Academica API

API REST en Spring Boot + PostgreSQL.

## Variables de entorno

| Variable | Obligatoria | Default | Descripcion |
| --- | --- | --- | --- |
| `DB_URL` | No | `jdbc:postgresql://localhost:5432/gestionbd` | JDBC URL de la BD |
| `DB_USERNAME` | **Si** | _(vacio)_ | Usuario de PostgreSQL |
| `DB_PASSWORD` | **Si** | _(vacio)_ | Contrasena del usuario |
| `JPA_DDL_AUTO` | No | `none` | Estrategia DDL de Hibernate |

## Configurar (Windows, permanente)

**CMD:**

```cmd
setx DB_URL "jdbc:postgresql://localhost:5432/gestionbd"
setx DB_USERNAME "tu_usuario_postgres"
setx DB_PASSWORD "tu_password"
setx JPA_DDL_AUTO "none"
```

**PowerShell:**

```powershell
[Environment]::SetEnvironmentVariable("DB_URL", "jdbc:postgresql://localhost:5432/gestionbd", "User")
[Environment]::SetEnvironmentVariable("DB_USERNAME", "tu_usuario_postgres", "User")
[Environment]::SetEnvironmentVariable("DB_PASSWORD", "tu_password", "User")
[Environment]::SetEnvironmentVariable("JPA_DDL_AUTO", "none", "User")
```

## Ejecutar

```bash
mvn spring-boot:run
```
