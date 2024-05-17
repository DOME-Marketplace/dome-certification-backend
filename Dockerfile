# Usa una imagen base de Maven para construir y ejecutar la aplicación
FROM maven:3.8.5-openjdk-17-slim AS build

# Establece el directorio de trabajo
WORKDIR /app

# Copia los archivos del proyecto al contenedor
COPY . .

# Construye el proyecto usando el perfil 'prod' y omitiendo las pruebas
RUN mvn clean package -P test -DskipTests

# Usa una imagen base de OpenJDK para ejecutar la aplicación
FROM openjdk:17-jdk-alpine

# Establece el directorio de trabajo
WORKDIR /app

# Copia el archivo WAR construido desde el directorio target al nuevo contenedor
COPY --from=build /app/target/*.war app.war

# Expon el puerto en el que la aplicación estará escuchando
EXPOSE 8080

# Define el comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app/app.war"]
