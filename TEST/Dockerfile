FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/TEST-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java","-cp","app.jar","com.example.app.HelloApp"]
