package io.icebrew.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "icebrew", version = "IceBrew CLI 0.2.2", description = "IceBrew CLI - Spring Boot + Vite development tool", subcommands = {
    CommandLine.HelpCommand.class })
public class IceBrewCli implements Callable<Integer> {

  @Option(names = { "-h", "--help" }, usageHelp = true, description = "Show this help message")
  boolean helpRequested;

  @Option(names = { "-V", "--version" }, versionHelp = true, description = "Print version information")
  boolean versionRequested;

  @Command(name = "create", description = "Create a new IceBrew project")
  public Integer create(
      @Parameters(index = "0", description = "Project name") String projectName,
      @Option(names = { "-f",
          "--framework" }, description = "Frontend framework: react, vue, svelte, vanilla", defaultValue = "react") String framework,
      @Option(names = { "-t",
          "--typescript" }, description = "Use TypeScript", defaultValue = "true") boolean typescript,
      @Option(names = { "-d", "--dir" }, description = "Target directory", defaultValue = ".") String targetDir) {
    try {
      System.out.println(" Creating IceBrew project: " + projectName);
      System.out.println("   Framework: " + framework);
      System.out.println("   TypeScript: " + typescript);
      System.out.println();

      Path projectPath = Paths.get(targetDir, projectName);

      if (Files.exists(projectPath)) {
        System.err.println(" Error: Directory already exists: " + projectPath);
        return 1;
      }

      // Create project structure
      createProjectStructure(projectPath, projectName, framework, typescript);

      System.out.println(" Project created successfully!");
      System.out.println();
      System.out.println(" Next steps:");
      System.out.println("   cd " + projectName);
      System.out.println("   cd frontend && npm install && cd ..");
      System.out.println("   mvn spring-boot:run");
      System.out.println();

      return 0;

    } catch (Exception e) {
      System.err.println(" Error: " + e.getMessage());
      e.printStackTrace();
      return 1;
    }
  }

  @Command(name = "version", description = "Show version information")
  public Integer version() {
    System.out.println("IceBrew CLI 0.2.1");
    return 0;
  }

  @Command(name = "build", description = "Build IceBrew project in current directory")
  public Integer build(
      @Option(names = { "-c", "--clean" }, description = "Clean build") boolean clean,
      @Option(names = { "-s", "--skip-tests" }, description = "Skip tests") boolean skipTests) {
    try {
      System.out.println(" Building IceBrew project...");
      System.out.println();

      // Check if pom.xml exists
      Path pomXml = Paths.get("pom.xml");
      if (!Files.exists(pomXml)) {
        System.err.println(" Error: pom.xml not found. Are you in a Maven project directory?");
        return 1;
      }

      // Build Maven command
      StringBuilder command = new StringBuilder("mvn ");
      if (clean) {
        command.append("clean ");
      }
      command.append("package");
      if (skipTests) {
        command.append(" -DskipTests");
      }

      System.out.println("Executing: " + command);
      System.out.println();

      // Execute Maven command
      ProcessBuilder pb = new ProcessBuilder();
      if (System.getProperty("os.name").toLowerCase().contains("windows")) {
        pb.command("cmd.exe", "/c", command.toString());
      } else {
        pb.command("sh", "-c", command.toString());
      }
      pb.inheritIO();
      Process process = pb.start();
      int exitCode = process.waitFor();

      if (exitCode == 0) {
        System.out.println();
        System.out.println(" Build successful!");
      } else {
        System.err.println();
        System.err.println(" Build failed with exit code: " + exitCode);
      }

      return exitCode;

    } catch (Exception e) {
      System.err.println(" Error: " + e.getMessage());
      e.printStackTrace();
      return 1;
    }
  }

  @Command(name = "run", description = "Run IceBrew application in current directory")
  public Integer run(
      @Option(names = { "-p", "--port" }, description = "Server port", defaultValue = "8080") int port,
      @Option(names = { "-d", "--dev" }, description = "Development mode") boolean dev) {
    try {
      System.out.println(" Starting IceBrew application...");
      System.out.println("   Port: " + port);
      System.out.println("   Mode: " + (dev ? "Development" : "Production"));
      System.out.println();

      // Check if project is built
      Path targetDir = Paths.get("target");
      if (!Files.exists(targetDir)) {
        System.err.println(" Error: target directory not found.");
        System.err.println("   Please build the project first: icebrew build");
        return 1;
      }

      // Find JAR file
      Path jarFile = Files.list(targetDir)
          .filter(p -> p.toString().endsWith(".jar") && !p.toString().endsWith(".original"))
          .findFirst()
          .orElse(null);

      if (jarFile == null) {
        System.err.println(" Error: No JAR file found in target directory.");
        System.err.println("   Please build the project first: icebrew build");
        return 1;
      }

      System.out.println("Starting: " + jarFile.getFileName());
      System.out.println("Open browser at: http://localhost:" + port);
      System.out.println();
      System.out.println("Press Ctrl+C to stop");
      System.out.println("=".repeat(70));
      System.out.println();

      // Run Spring Boot application
      ProcessBuilder pb = new ProcessBuilder();
      pb.command("java", "-jar", jarFile.toString());
      pb.environment().put("SERVER_PORT", String.valueOf(port));
      if (dev) {
        pb.environment().put("SPRING_PROFILES_ACTIVE", "dev");
      }
      pb.inheritIO();
      Process process = pb.start();
      int exitCode = process.waitFor();

      return exitCode;

    } catch (Exception e) {
      System.err.println(" Error: " + e.getMessage());
      e.printStackTrace();
      return 1;
    }
  }

  @Override
  public Integer call() {
    CommandLine.usage(this, System.out);
    return 0;
  }

  private void createProjectStructure(Path projectPath, String projectName, String framework, boolean typescript)
      throws IOException {
    // Create directories
    Files.createDirectories(projectPath);
    Files.createDirectories(projectPath.resolve("src/main/java/com/example/demo"));
    Files.createDirectories(projectPath.resolve("src/main/resources"));
    Files.createDirectories(projectPath.resolve("frontend"));

    // Create pom.xml
    createPomXml(projectPath, projectName);

    // Create application.properties
    createApplicationProperties(projectPath);

    // Create main application class
    createMainClass(projectPath);

    // Create controller
    createController(projectPath);

    // Create frontend
    createFrontend(projectPath, framework, typescript);
  }

  private void createPomXml(Path projectPath, String projectName) throws IOException {
    String content = """
        <?xml version="1.0" encoding="UTF-8"?>
        <project xmlns="http://maven.apache.org/POM/4.0.0"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
            <modelVersion>4.0.0</modelVersion>

            <parent>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-parent</artifactId>
                <version>3.2.0</version>
                <relativePath/>
            </parent>

            <groupId>com.example</groupId>
            <artifactId>%s</artifactId>
            <version>0.0.1-SNAPSHOT</version>
            <name>%s</name>
            <description>IceBrew project with Spring Boot and Vite</description>

            <properties>
                <java.version>17</java.version>
            </properties>

            <dependencies>
                <dependency>
                    <groupId>io.icebrew</groupId>
                    <artifactId>icebrew-starter</artifactId>
                    <version>0.2.2</version>
                </dependency>
                <dependency>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-web</artifactId>
                </dependency>
                <dependency>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-test</artifactId>
                    <scope>test</scope>
                </dependency>
            </dependencies>

            <build>
                <plugins>
                    <plugin>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-maven-plugin</artifactId>
                    </plugin>
                </plugins>
            </build>
        </project>
        """.formatted(projectName, projectName);

    Files.writeString(projectPath.resolve("pom.xml"), content);
  }

  private void createApplicationProperties(Path projectPath) throws IOException {
    String content = """
        # Server Configuration
        server.port=8080

        # IceBrew Vite Configuration
        icebrew.vite.enabled=true
        icebrew.vite.host=localhost
        icebrew.vite.port=5173
        icebrew.vite.frontend-dir=frontend
        icebrew.vite.build-dir=dist
        icebrew.vite.auto-start=true

        # Logging
        logging.level.io.icebrew=DEBUG
        """;

    Files.writeString(projectPath.resolve("src/main/resources/application.properties"), content);
  }

  private void createMainClass(Path projectPath) throws IOException {
    String content = """
        package com.example.demo;

        import org.springframework.boot.SpringApplication;
        import org.springframework.boot.autoconfigure.SpringBootApplication;

        @SpringBootApplication
        public class DemoApplication {

            public static void main(String[] args) {
                SpringApplication.run(DemoApplication.class, args);
            }
        }
        """;

    Files.writeString(projectPath.resolve("src/main/java/com/example/demo/DemoApplication.java"), content);
  }

  private void createController(Path projectPath) throws IOException {
    String content = """
        package com.example.demo;

        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RestController;

        import java.util.Map;

        @RestController
        @RequestMapping("/api")
        public class ApiController {

            @GetMapping("/hello")
            public Map<String, String> hello() {
                return Map.of("message", "Hello from Spring Boot with IceBrew!");
            }
        }
        """;

    Files.writeString(projectPath.resolve("src/main/java/com/example/demo/ApiController.java"), content);
  }

  private void createFrontend(Path projectPath, String framework, boolean typescript) throws IOException {
    Path frontendPath = projectPath.resolve("frontend");

    // Create package.json
    createPackageJson(frontendPath, framework, typescript);

    // Create vite.config
    createViteConfig(frontendPath, typescript);

    // Create index.html
    createIndexHtml(frontendPath);

    // Create src directory and files based on framework
    createFrontendSrc(frontendPath, framework, typescript);
  }

  private void createPackageJson(Path frontendPath, String framework, boolean typescript) throws IOException {
    String content = """
        {
          "name": "frontend",
          "private": true,
          "version": "0.0.0",
          "type": "module",
          "scripts": {
            "dev": "vite",
            "build": "vite build",
            "preview": "vite preview"
          },
          "dependencies": {
            "react": "^18.2.0",
            "react-dom": "^18.2.0"
          },
          "devDependencies": {
            "@types/react": "^18.2.43",
            "@types/react-dom": "^18.2.17",
            "@vitejs/plugin-react": "^4.2.1",
            "typescript": "^5.3.3",
            "vite": "^5.0.8"
          }
        }
        """;

    Files.writeString(frontendPath.resolve("package.json"), content);
  }

  private void createViteConfig(Path frontendPath, boolean typescript) throws IOException {
    String ext = typescript ? "ts" : "js";
    String content = """
        import { defineConfig } from 'vite'
        import react from '@vitejs/plugin-react'

        export default defineConfig({
          plugins: [react()],
          server: {
            host: 'localhost',
            port: 5173,
            strictPort: true,
            proxy: {
              '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
              }
            }
          },
          build: {
            outDir: 'dist',
            assetsDir: 'assets',
            emptyOutDir: true,
          }
        })
        """;

    Files.writeString(frontendPath.resolve("vite.config." + ext), content);
  }

  private void createIndexHtml(Path frontendPath) throws IOException {
    String content = """
        <!doctype html>
        <html lang="en">
          <head>
            <meta charset="UTF-8" />
            <link rel="icon" type="image/svg+xml" href="/vite.svg" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <title>IceBrew App</title>
          </head>
          <body>
            <div id="root"></div>
            <script type="module" src="/src/main.tsx"></script>
          </body>
        </html>
        """;

    Files.writeString(frontendPath.resolve("index.html"), content);
  }

  private void createFrontendSrc(Path frontendPath, String framework, boolean typescript) throws IOException {
    Path srcPath = frontendPath.resolve("src");
    Files.createDirectories(srcPath);

    String ext = typescript ? "tsx" : "jsx";

    // Create main file
    String mainContent = """
        import React from 'react'
        import ReactDOM from 'react-dom/client'
        import App from './App.%s'
        import './index.css'

        ReactDOM.createRoot(document.getElementById('root')!).render(
          <React.StrictMode>
            <App />
          </React.StrictMode>,
        )
        """.formatted(ext);

    Files.writeString(srcPath.resolve("main." + ext), mainContent);

    // Create App component
    String appContent = """
        import { useState, useEffect } from 'react'
        import './App.css'

        function App() {
          const [message, setMessage] = useState('')

          useEffect(() => {
            fetch('/api/hello')
              .then(res => res.json())
              .then(data => setMessage(data.message))
          }, [])

          return (
            <div className="App">
              <h1> IceBrew</h1>
              <p>Spring Boot + Vite + React</p>
              <div className="card">
                <p>{message || 'Loading...'}</p>
              </div>
            </div>
          )
        }

        export default App
        """;

    Files.writeString(srcPath.resolve("App." + ext), appContent);

    // Create CSS files
    String appCss = """
        .App {
          text-align: center;
          padding: 2rem;
        }

        .card {
          padding: 2rem;
          background-color: #f0f0f0;
          border-radius: 8px;
          margin-top: 2rem;
        }
        """;

    Files.writeString(srcPath.resolve("App.css"), appCss);

    String indexCss = """
        :root {
          font-family: Inter, system-ui, Avenir, Helvetica, Arial, sans-serif;
          line-height: 1.5;
          font-weight: 400;
        }

        body {
          margin: 0;
          display: flex;
          place-items: center;
          min-width: 320px;
          min-height: 100vh;
        }

        #root {
          max-width: 1280px;
          margin: 0 auto;
          padding: 2rem;
          text-align: center;
        }
        """;

    Files.writeString(srcPath.resolve("index.css"), indexCss);
  }

  public static void main(String[] args) {
    int exitCode = new CommandLine(new IceBrewCli()).execute(args);
    System.exit(exitCode);
  }
}
