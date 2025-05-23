# Instant Payment API

This application provides an API for instant payment processing.

## Prerequisites

*   [Docker](https://www.docker.com/get-started)
*   [Docker Compose](https://docs.docker.com/compose/install/) (usually included with Docker Desktop)
*   [Java Development Kit (JDK) 21](https://www.oracle.com/java/technologies/downloads/#java21) or later
*   [Apache Maven](https://maven.apache.org/download.cgi) (latest version recommended)

## Setup and Running the Application

1.  **Clone the Repository (if you haven't already):**
    ```shell
    git clone <repository-url>
    cd instant-payment-api
    ```

2.  **Build and Start with Docker Compose:**
    This is the recommended way to run the application along with its dependencies (PostgreSQL, Kafka, Redis).
    The Docker build process will compile the Java project using Maven.

    Open a terminal in the project's root directory (`c:\code\instant-payment-api`) and run:
    ```shell
    docker-compose up --build -d
    ```
    *   `--build`: This flag tells Docker Compose to build the application image (defined in `Dockerfile`) before starting the services. This includes the Maven build step.
    *   `-d`: This flag runs the containers in detached mode (in the background).

3.  **Verify the Application is Running:**

    *   **Check Docker Container Status:**
        List all running containers to ensure the `app`, `db`, `kafka`, and `redis` services are up:
        ```shell
        docker-compose ps
        ```
        You should see the `app` container (e.g., `instant-payment-api-app-1`) with a state of `Up` or `running`.

    *   **Check Application Logs:**
        To view the logs from the Spring Boot application:
        ```shell
        docker-compose logs -f app
        ```
        Look for messages indicating that the Spring application has started successfully, typically ending with a line like `Started InstantPaymentApiApplication in ... seconds`.

    *   **Access Swagger UI:**
        Once the application is running, you can access the API documentation (Swagger UI) in your web browser:
        [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Development

### Building the JAR locally (Optional)

If you want to build the JAR file locally without Docker, you can run:
```shell
mvn clean package -DskipTests
```
This will create the executable JAR in the `target/` directory (e.g., `target/instant-payment-api-1.0.0.jar`).

### Running Tests Locally
To run the unit and integration tests:
```shell
mvn test
```

## Stopping the Application

To stop all running services started by Docker Compose:
```shell
docker-compose down
```
If you also want to remove the volumes (which store data for PostgreSQL, etc.), use:
```shell
docker-compose down -v
```

## Configuration

The main application configuration is in `src/main/resources/application.yml`.
When running with Docker Compose, the application connects to other services (PostgreSQL, Kafka, Redis) using their service names as hostnames (e.g., `db`, `kafka`, `redis`).
