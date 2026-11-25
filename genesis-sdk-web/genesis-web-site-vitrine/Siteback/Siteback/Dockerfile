# ─────────────────────────────────────────────────────────────
#  Siteback — API du contenu du site (Spring Boot 3, Java 21)
#  Build :  docker build -t genesis-siteback ./Siteback/Siteback
#  Lancé par deploy/docker-compose.yml (service « siteback »)
# ─────────────────────────────────────────────────────────────

# 1) Build Maven (les dépendances sont mises en cache tant que pom.xml ne change pas)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q -B dependency:go-offline || true
COPY src ./src
RUN mvn -q -B package -DskipTests \
 && cp target/*.jar /app.jar

# 2) Image finale
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app.jar app.jar
# Lu par ColumnVisibilityController : ${user.dir}/config/column-visibility.json
COPY config ./config

RUN useradd --system --uid 10001 siteback && chown -R siteback /app
USER siteback

# Base, mot de passe et niveau de logs viennent des variables d'environnement
# (SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD, LOGGING_LEVEL_ROOT…)
EXPOSE 9000
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
