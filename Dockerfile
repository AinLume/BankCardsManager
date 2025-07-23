FROM eclipse-temurin:17-jdk-alpine as builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -Dspring.profiles.active=docker -Dlogging.level.com.example.tasks_service=DEBUG -Dlogging.level.liquibase=INFO -Dlogging.level.org.springframework=INFO"
ENV TZ=Europe/Moscow
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS

RUN addgroup --system spring && \
    adduser --system --ingroup spring spring && \
    chown -R spring:spring /app && \
    chmod -R 755 /app

COPY --from=builder --chown=spring:spring /app/src/main/resources /app/resources

USER spring:spring
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Dloader.path=/app/resources -jar /app/app.jar --logging.file.name=/app/logs/application.log --logging.file.path=/app/logs"]