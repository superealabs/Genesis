# ─────────────────────────────────────────────────────────────
#  genesis-api à partir du .jar déjà compilé (pas de Gradle dans Docker)
#
#  1. Compiler sur ton PC :   .\gradlew.bat :genesis-api:bootJar
#  2. Copier le jar ICI sous le nom genesis-api.jar
#  3. docker compose up -d --build
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY genesis-api.jar app.jar

# Même utilisateur (uid 1000) que le conteneur du site : les deux écrivent dans le volume /work
RUN mkdir -p /work && chown -R 1000:1000 /app /work
USER 1000:1000

# application.yml écoute sur 127.0.0.1 : dans Docker il faut 0.0.0.0 (le port n'est PAS publié)
ENV SERVER_ADDRESS=0.0.0.0 \
    GENESIS_SQL_EXECUTION_ENABLED=false
EXPOSE 8081
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
