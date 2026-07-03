# =====================================================
# Stage 1: Build con Maven + JDK 26
# =====================================================
FROM eclipse-temurin:26-jdk-alpine AS build

WORKDIR /workspace

# Copiar solo pom.xml + wrapper para cachear dependencias en rebuilds
COPY pom.xml ./
COPY .mvn/ .mvn/
COPY mvnw ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Ahora copiar el codigo fuente y compilar
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# =====================================================
# Stage 2: Runtime con JRE 26
# =====================================================
FROM eclipse-temurin:26-jre-alpine AS runtime

# Instalar wget para healthcheck (no viene por defecto en alpine)
RUN apk add --no-cache wget

# Crear usuario no-root (seguridad)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copiar el jar compilado (renombrar para simplicidad)
COPY --from=build /workspace/target/gestion-academica-api-0.0.1-SNAPSHOT.jar app.jar

# Directorio para uploads futuros (Bloque 12 adjuntos)
RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app
USER appuser

# Puerto que expone Spring Boot
EXPOSE 8080

# Variables de entorno con defaults razonables (seran sobrescritas por compose)
ENV SPRING_PROFILES_ACTIVE=docker \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"

# Healthcheck: verifica que el puerto 8080 este escuchando.
# (El endpoint /actuator/health no existe aun en el backend, ver Bloque 10.7 del gaps doc)
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8080/api/swagger-ui/index.html || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]