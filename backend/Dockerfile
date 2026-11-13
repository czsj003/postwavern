FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline
COPY src src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S postwave && adduser -S postwave -G postwave \
    && mkdir -p /app/uploads && chown -R postwave:postwave /app
COPY --from=build --chown=postwave:postwave /workspace/target/*.jar app.jar
USER postwave
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
