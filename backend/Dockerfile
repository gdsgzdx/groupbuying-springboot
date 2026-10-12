# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-8 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:8-jre
WORKDIR /app
ENV SERVER_PORT=5000 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
RUN useradd --system --uid 1001 appuser \
    && mkdir -p /app/uploads \
    && chown -R appuser:appuser /app
COPY --from=builder /build/target/*.jar /app/app.jar
USER appuser
EXPOSE 5000
VOLUME ["/app/uploads"]
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
