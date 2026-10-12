# One image: React build is bundled into the Spring Boot jar.
FROM node:22-alpine AS web
WORKDIR /web
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-21 AS api
WORKDIR /app
COPY backend/pom.xml .
RUN mvn -B -q dependency:go-offline
COPY backend/src ./src
COPY --from=web /web/dist ./src/main/resources/static
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --create-home stockyard && mkdir /app/uploads && chown stockyard /app/uploads
USER stockyard
COPY --from=api /app/target/stockyard-api-*.jar app.jar
ENV SERVER_PORT=8080
EXPOSE 8080
# Honors the PORT variable that hosts like Render inject.
ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT:-8080} -jar app.jar"]
