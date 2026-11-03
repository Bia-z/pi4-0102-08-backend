#compila o projeto
#começa de uma "caixinha" que já tem Maven e Java 17 instalados
FROM maven:3.9-eclipse-temurin-17 AS build

#define a pasta de trabalho dentro da caixinha
WORKDIR /app

#copia só o pom.xml primeiro e baixa as dependências
#(assim o Docker guarda isso em cache e as próximas builds ficam mais rápidas)
COPY pom.xml .
RUN mvn -q dependency:go-offline

#agora copia o seu código e compila, gerando o arquivo .jar
#-DskipTests = não roda os testes agora (o CI do H01.3 vai cuidar disso)
COPY src ./src
RUN mvn -q package -DskipTests

#rodar
#caixinha final, menor: só Java para rodar (sem Maven)
FROM eclipse-temurin:17-jre
WORKDIR /app

#pega o .jar pronto da etapa 1
COPY --from=build /app/target/*.jar app.jar

#avisa que o backend usa a porta 8080
EXPOSE 8080

#comando que roda quando a caixinha liga
ENTRYPOINT ["java", "-jar", "app.jar"]