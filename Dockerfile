# Usar OpenJDK 21 como base
FROM openjdk:21-jdk-slim

# Instalar Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Definir diretório de trabalho
WORKDIR /app

# Copiar arquivos do Maven primeiro (para cache de dependências)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Baixar dependências (isso será cacheado se o pom.xml não mudar)
RUN mvn dependency:go-offline -B

# Copiar o código fonte
COPY src ./src

# Compilar a aplicação
RUN mvn clean package -DskipTests

# Expor a porta da aplicação
EXPOSE 8080

# Comando para executar a aplicação
CMD ["java", "-jar", "target/pirangueiro-0.0.1-SNAPSHOT.jar"] 