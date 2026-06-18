# Usa la imagen oficial de Eclipse Temurin con OpenJDK 17
FROM eclipse-temurin:17-jdk-alpine

# Define /app como el directorio de trabajo interno
WORKDIR /app

# Copia los archivos de configuración de Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Da permisos de ejecución al envoltorio de Maven
RUN chmod +x mvnw

# Descarga las dependencias del proyecto de forma aislada
RUN ./mvnw dependency:go-offline

# Copia la carpeta de código fuente src/
COPY src ./src

# --- 🌟 AQUÍ ESTÁ EL TRUCO MODIFICADO ---
# Usamos "-Dmaven.test.skip=true" para obligar a Maven a ignorar por completo las conexiones de BD en el build
RUN ./mvnw clean package -Dmaven.test.skip=true

# Busca de manera automática cualquier archivo .jar que se cree en target/ y córrelo
# Así no dependemos de adivinar el nombre original si cambia la versión
CMD ["sh", "-c", "java -jar target/*.jar"]