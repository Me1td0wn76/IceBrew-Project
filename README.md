#  IceBrew

[JAPANESE README](./README_ja.md)もご覧ください。  
**Spring Boot + Vite Integration Framework for Modern Web Development**

IceBrew is an open-source framework that seamlessly integrates Vite dev server with Spring Boot, providing an excellent developer experience with Hot Module Replacement (HMR) in development mode and efficient static file serving in production mode.

##  Features

-  **Hot Module Replacement (HMR)** - Instant feedback during development
-  **Auto-proxy** - Automatic proxying of static assets to Vite dev server
-  **Zero Configuration** - Works out of the box with sensible defaults
-  **Production Ready** - Automatic static file serving from build output
-  **CLI Tool** - Quick project scaffolding with multiple framework options
-  **Multi-framework Support** - React, Vue, Svelte, and vanilla JavaScript
-  **Vite Powered** - Lightning fast builds and development experience
-  **Spring Boot 3** - Built on the latest Spring Boot platform

##  Key Capabilities

### Development Mode
- Vite dev server is automatically started by the Java backend
- Static file requests are proxied to Vite dev server
- HMR works seamlessly
- WebSocket connections for hot updates are properly handled

### Production Mode
- Built frontend assets (`dist/`) are served by Spring Boot
- Efficient static resource handling
- Optimized for production deployment

##  Installation

### Maven

Add the IceBrew starter to your `pom.xml`:

```xml
<dependency>
    <groupId>io.icebrew</groupId>
    <artifactId>icebrew-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

##  Quick Start

### Using the CLI

The easiest way to create a new IceBrew project:

```bash
# Build the CLI
cd icebrew-cli
mvn clean package

# Create a new project
java -jar target/icebrew-cli-0.1.0-SNAPSHOT-jar-with-dependencies.jar create my-app

# Start development
cd my-app
cd frontend && npm install && cd ..
mvn spring-boot:run
```

Your application will be available at `http://localhost:8080`

### Manual Setup

1. **Create a Spring Boot project** with the IceBrew starter dependency

2. **Configure `application.properties`**:

```properties
# Server Configuration
server.port=8080

# IceBrew Vite Configuration
icebrew.vite.enabled=true
icebrew.vite.host=localhost
icebrew.vite.port=5173
icebrew.vite.frontend-dir=frontend
icebrew.vite.build-dir=dist
icebrew.vite.auto-start=true
```

3. **Create your frontend** in the `frontend/` directory:

```bash
npm create vite@latest frontend -- --template react-ts
cd frontend
npm install
```

4. **Run your application**:

```bash
mvn spring-boot:run
```

##  Configuration Options

| Property | Default | Description |
|----------|---------|-------------|
| `icebrew.vite.enabled` | `true` | Enable/disable Vite integration |
| `icebrew.vite.host` | `localhost` | Vite dev server host |
| `icebrew.vite.port` | `5173` | Vite dev server port |
| `icebrew.vite.frontend-dir` | `frontend` | Frontend source directory |
| `icebrew.vite.build-dir` | `dist` | Build output directory |
| `icebrew.vite.auto-start` | `true` | Auto-start Vite dev server |
| `icebrew.vite.base-path` | `""` | Base URL path for Vite |
| `icebrew.vite.startup-timeout` | `60` | Startup timeout in seconds |

##  Project Structure

```
my-app/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/demo/
│       │       ├── DemoApplication.java
│       │       └── ApiController.java
│       └── resources/
│           └── application.properties
├── frontend/
│   ├── src/
│   │   ├── App.tsx
│   │   ├── main.tsx
│   │   └── index.css
│   ├── index.html
│   ├── package.json
│   ├── tsconfig.json
│   └── vite.config.ts
└── pom.xml
```

##  Advanced Usage

### Custom API Endpoints

Create REST controllers in your Spring Boot application:

```java
@RestController
@RequestMapping("/api")
public class ApiController {
    
    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Hello from Spring Boot!");
    }
}
```

Access from frontend:

```typescript
fetch('/api/hello')
  .then(res => res.json())
  .then(data => console.log(data.message))
```

### Production Build

1. Build the frontend:
```bash
cd frontend
npm run build
```

2. Run Spring Boot in production mode:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

Or package as a JAR:
```bash
mvn clean package
java -jar target/my-app-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

##  Supported Frontend Frameworks

- **React** - React 18 with TypeScript
- **Vue** - Vue 3 with TypeScript
- **Svelte** - Svelte with TypeScript
- **Vanilla** - Plain JavaScript/TypeScript

More frameworks coming soon!

##  Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

**Note:** The `release.ps1` and `release.sh` scripts are for project maintainers only. If you've forked this project, please modify these scripts to match your repository before use.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

##  License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

##  Acknowledgments
- [Spring Boot](https://spring.io/projects/spring-boot) - The Spring team for their amazing framework
- [Vite](https://vitejs.dev/) - Evan You and the Vite team for the blazing fast build tool
- All contributors who help make this project better
---

