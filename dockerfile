# Step 1: Base image
#FROM openjdk:21

# Step 2: Set the working directory
#WORKDIR /app

# Step 3: Copy the built JAR file into the container
#COPY build/libs/backend-0.0.1-SNAPSHOT.jar app.jar

# Step 4: Expose the port your application uses
#EXPOSE 8080

# Step 5: Run the application
#ENTRYPOINT ["java", "-jar", "app.jar"]

# Step 1: Base image
FROM openjdk:21-jdk

# Step 2: Set the working directory
WORKDIR /app

# Step 3: Copy the built JAR file into the container
COPY build/libs/*.jar app.jar

# Step 4: Create resources directory for configuration
RUN mkdir -p /app/src/main/resources

# Step 5: Define the application YAML as an ARG and write it to application.yml
ARG APPLICATION_YAML
RUN echo "$APPLICATION_YAML" > /app/src/main/resources/application.yml

# Step 6: Expose the port your application uses
EXPOSE 8080

# ci와 local 구분
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=ci"]
#ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=local"]
