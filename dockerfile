# Use an official Ubuntu runtime as a base image
FROM ubuntu:latest

# Set the working directory inside the container
WORKDIR /app-chat

# Update package lists and install necessary dependencies
RUN apt-get update && apt-get install -y \
    openjdk-21-jdk \
    maven

# Copy the application source code into the container
COPY . .

# Build the application
RUN mvn clean install

# Expose the port your application will run on
EXPOSE 8080

# Command to run your application
CMD ["java", "-jar", "target/appChat-1.0-SNAPSHOT.jar"]