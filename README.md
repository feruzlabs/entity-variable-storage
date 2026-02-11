# Entity Variable Storage (EVS)

Universal, high-performance entity-variable storage system with PostgreSQL sub-partitioning.

## Features

- **3-layer architecture** (Entity → Instance → Variables)
- **PostgreSQL sub-partitioning** (entity-level + monthly)
- **Type-safe variable storage** (String, Integer, Float, Boolean, JSON, etc.)
- **Reusable library** – works with any Java project
- **Spring Boot auto-configuration**
- **Java 21** (Records, Virtual Threads, Pattern Matching)

## Installation

### Gradle (Kotlin DSL)

Add GitHub Package Registry to your **settings.gradle.kts**:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/feruzlabsentity-variable-storage")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

Add dependency in **build.gradle.kts**:

```kotlin
dependencies {
    implementation("com.evs:entity-variable-storage-core:1.0.0")

    // Optional: Spring Boot starter
    implementation("com.evs:entity-variable-storage-spring-boot-starter:1.0.0")
}
```

### Gradle (Groovy DSL)

Add to **build.gradle**:

```gradle
repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/feruzlabs/entity-variable-storage")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation 'com.evs:entity-variable-storage-core:1.0.0'

    // Optional: Spring Boot starter
    implementation 'com.evs:entity-variable-storage-spring-boot-starter:1.0.0'
}
```

### Maven

Add to your **pom.xml**:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/feruzlabs/entity-variable-storage</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.evs</groupId>
        <artifactId>entity-variable-storage-core</artifactId>
        <version>1.0.0</version>
    </dependency>

    <!-- Optional: Spring Boot starter -->
    <dependency>
        <groupId>com.evs</groupId>
        <artifactId>entity-variable-storage-spring-boot-starter</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

Add **~/.m2/settings.xml** for authentication:

```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>feruzlabs</username>
            <password>YOUR_GITHUB_TOKEN</password>
        </server>
    </servers>
</settings>
```

### Authentication Setup

#### For Development

Create **~/.gradle/gradle.properties**:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.token=YOUR_GITHUB_PERSONAL_ACCESS_TOKEN
```

#### For CI/CD

GitHub Actions automatically provides `GITHUB_TOKEN` and `GITHUB_ACTOR`.

#### Generate GitHub Personal Access Token

1. Go to **GitHub Settings → Developer settings → Personal access tokens → Tokens (classic)**
2. Create a token with scopes: `read:packages`, `write:packages`
3. Use the token in `gradle.properties` or `settings.xml`

> **Note:** Replace `YOUR_GITHUB_USERNAME` with your GitHub username in all URLs and configuration.

## Quick Start

### Standalone Java

```java
import com.evs.config.*;
import com.evs.model.*;
import com.evs.service.*;

public class Example {
    public static void main(String[] args) {
        EVSConfig config = new EVSConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/evs_db");
        config.setUsername("postgres");
        config.setPassword("postgres");

        EVSFactory factory = new EVSFactory(config);
        EntityService entityService = factory.entityService();
        EntityInstanceService instanceService = factory.entityInstanceService();
        VariableService variableService = factory.variableService();

        Entity user = entityService.createEntity(
            Entity.builder().name("User").displayName("User Entity").build()
        );

        EntityInstance instance = instanceService.createInstance(
            EntityInstance.builder()
                .entityId(user.id())
                .uuid(UUID.randomUUID())
                .build()
        );

        variableService.createVariable(
            Variable.builder()
                .entityId(user.id())
                .entityInstanceId(instance.id())
                .variableName("email")
                .variableType(VariableType.STRING)
                .value("john@example.com")
                .registeredAt(instance.registeredAt())
                .build()
        );
    }
}
```

### Spring Boot

Add to **application.yml**:

```yaml
evs:
  jdbc-url: jdbc:postgresql://localhost:5432/evs_db
  username: postgres
  password: postgres
  maximum-pool-size: 25
  auto-migrate: true
```

Inject services:

```java
@RestController
@RequiredArgsConstructor
public class UserController {
    private final EntityService entityService;
    private final VariableService variableService;
}
```

## Architecture

```
entities (Class/Schema)
    ↓ 1:N
entity_instances (Objects with UUID)
    ↓ 1:N
variables (Properties - PARTITIONED)

variables table
  ↓ LIST partition by entity_id
  ├─ variables_entity_user
  ├─ variables_entity_order
  └─ variables_entity_invoice
      ↓ RANGE sub-partition by registered_at (monthly)
      ├─ variables_entity_user_y2025m01
      ├─ variables_entity_user_y2025m02
      └─ ...
```

## Building

```bash
# Generate Gradle wrapper (first time)
gradle wrapper --gradle-version 8.5

# Build
./gradlew build   # Unix/Mac
gradlew.bat build # Windows
```

## Testing

Tests use Testcontainers with PostgreSQL 16 (Docker required):

```bash
./gradlew test
```

## Examples

- `examples/standalone-example` – plain Java usage
- `examples/spring-boot-example` – Spring Boot REST API

### Docker

Start PostgreSQL for development:

```bash
docker compose up -d
```

Full stack (PostgreSQL + Spring Boot app):

```bash
cd examples/spring-boot-example
docker compose up -d
# API: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

## Publishing

See [PUBLISHING.md](PUBLISHING.md) for release and publishing instructions.

## License

Apache License 2.0
