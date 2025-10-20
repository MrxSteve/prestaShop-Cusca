# Usamos imagen base con JDK 17
FROM eclipse-temurin:17-jdk-alpine

# Instalamos Maven directamente en la imagen
RUN apk add --no-cache maven

# Establecemos el directorio de trabajo
WORKDIR /app

# Copiamos el pom y descargamos dependencias primero (mejor cache)
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copiamos el resto del código y lo construimos
COPY . .
RUN mvn clean package -DskipTests -B

# Ejecutamos el JAR generado
CMD ["java", "-jar", "target/shopmoney-pg-0.0.1-SNAPSHOT.jar"]
