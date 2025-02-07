FROM eclipse-temurin:17-jre-alpine

COPY ./target/depocheck-0.0.1-SNAPSHOT.jar .

EXPOSE 8080

CMD ["sh","-c","java -XX:InitialRAMPercentage=50 -XX:MaxRAMPercentage=70  -XshowSettings $JAVA_OPTS -jar depocheck-0.0.1-SNAPSHOT.jar"]