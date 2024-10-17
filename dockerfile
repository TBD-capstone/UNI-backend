# Step 1: Base image
FROM openjdk:21

# Step 2: Set the working directory
WORKDIR /app

# Step 3: Copy the built JAR file into the container
COPY build/libs/backend-0.0.1-SNAPSHOT.jar app.jar

# Step 4: Expose the port your application uses
EXPOSE 8080

# Step 5: Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
