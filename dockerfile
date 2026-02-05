FROM eclipse-temurin:17-jdk-alpine

# Install bash and curl for test script execution
RUN apk add --no-cache bash curl

VOLUME /tmp

# Copy the test script
COPY src/main/financial_manager_tests.sh /src/main/financial_manager_tests.sh
RUN chmod +x /src/main/financial_manager_tests.sh

# Copy the JAR file
COPY target/*.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
