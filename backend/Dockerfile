# backend/Dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build


WORKDIR /app

# Copiar Maven wrapper y pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Dar permisos de ejecución al wrapper
RUN chmod +x mvnw
# Descargar dependencias (caché)
RUN ./mvnw dependency:go-offline -B



# Copiar código fuente
COPY src ./src

# Construir el JAR
RUN ./mvnw package -DskipTests

# Segunda etapa: imagen ligera para ejecutar
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar el JAR construido
COPY --from=build /app/target/*.jar app.jar

# Puerto de la aplicación
EXPOSE 8080

# Comando para ejecutar
ENTRYPOINT ["java", "-jar", "app.jar"]