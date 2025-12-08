# IceBrew Sample - React

This is a sample project demonstrating IceBrew framework integration with React and TypeScript.

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Node.js 18 or higher
- npm or yarn

### Installation

1. Install frontend dependencies:
```bash
cd frontend
npm install
```

2. Return to the project root:
```bash
cd ..
```

### Running the Application

#### Development Mode

Simply run:
```bash
mvn spring-boot:run
```

This will:
- Start the Spring Boot application on port 8080
- Automatically start the Vite dev server on port 5173
- Proxy all frontend requests to Vite
- Enable Hot Module Replacement (HMR)

Open your browser and navigate to `http://localhost:8080`

#### Production Mode

1. Build the frontend:
```bash
cd frontend
npm run build
cd ..
```

2. Run Spring Boot with production profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Features Demonstrated

- ✅ Vite Dev Server integration with automatic startup
- ✅ Hot Module Replacement (HMR) for instant updates
- ✅ API proxy from frontend to Spring Boot backend
- ✅ React 18 with TypeScript
- ✅ Production build serving from Spring Boot

### Project Structure

```
sample-react/
├── src/main/java/         # Spring Boot application
│   └── io/icebrew/samples/react/
│       ├── SampleReactApplication.java
│       └── controller/
│           └── ApiController.java
├── src/main/resources/
│   └── application.properties
└── frontend/              # React + Vite frontend
    ├── src/
    │   ├── App.tsx
    │   ├── main.tsx
    │   └── *.css
    ├── index.html
    ├── package.json
    ├── tsconfig.json
    └── vite.config.ts
```

### API Endpoints

- `GET /api/hello` - Returns a greeting message with timestamp
- `GET /api/status` - Returns application status

### Customization

You can customize the configuration in `application.properties`:

```properties
# Change server port
server.port=8080

# Change Vite dev server port
icebrew.vite.port=5173

# Disable auto-start (manual Vite startup)
icebrew.vite.auto-start=false
```

## Learn More

- [IceBrew Documentation](../../README.md)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Vite Documentation](https://vitejs.dev/)
- [React Documentation](https://react.dev/)
