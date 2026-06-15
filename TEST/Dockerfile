FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built JAR into the container
COPY target/TEST-1.0-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Using ENTRYPOINT + CMD allows easier argument overriding if needed
ENTRYPOINT ["java", "-cp", "app.jar"]
CMD ["com.example.app.HelloApp"]
