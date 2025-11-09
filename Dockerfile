# Etapa 1: Compilar
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos solo lo necesario desde la subcarpeta
COPY chemiconsult/pom.xml .
COPY chemiconsult/src ./src

# Ejecutamos el build desde /app
RUN mvn clean package -DskipTests

# Etapa 2: Ejecutar la app
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
