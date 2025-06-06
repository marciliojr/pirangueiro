# Multi-stage build para imagem menor em produção
FROM openjdk:21-jdk-slim AS build

# Instalar Maven e ferramentas necessárias
RUN apt-get update && \
    apt-get install -y maven wget && \
    rm -rf /var/lib/apt/lists/*

# Definir diretório de trabalho
WORKDIR /app

# Copiar arquivos do Maven primeiro (para cache de dependências)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Tornar mvnw executável
RUN chmod +x mvnw

# Baixar dependências (isso será cacheado se o pom.xml não mudar)
RUN mvn dependency:go-offline -B

# Copiar o código fonte
COPY src ./src

# Compilar a aplicação
RUN mvn clean package -DskipTests

# Stage de produção - imagem menor
FROM openjdk:21-jre-slim

# Instalar wget para healthcheck
RUN apt-get update && \
    apt-get install -y wget && \
    rm -rf /var/lib/apt/lists/*

# Criar usuário não-root para segurança
RUN groupadd -r spring && useradd -r -g spring spring

# Definir diretório de trabalho
WORKDIR /app

# Copiar apenas o JAR da stage de build
COPY --from=build /app/target/pirangueiro-0.0.1-SNAPSHOT.jar app.jar

# Dar permissões ao usuário spring
RUN chown spring:spring app.jar

# Trocar para usuário não-root
USER spring

# Expor a porta da aplicação
EXPOSE 8080

# Adicionar opções da JVM para container
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Comando para executar a aplicação
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]