# Contributing to IceBrew

Thank you for your interest in contributing to IceBrew! We welcome contributions from the community.

## Getting Started

1. Fork the repository
2. Clone your fork locally
3. Create a new branch for your feature or bugfix
4. Make your changes
5. Test your changes thoroughly
6. Submit a pull request

## Development Setup

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Node.js 18 or higher
- Git

### Building the Project

```bash
# Build all modules
mvn clean install

# Build specific module
cd icebrew-core
mvn clean install
```

### Running Tests

```bash
mvn test
```

### Running the Sample

```bash
cd icebrew-samples/sample-react
cd frontend && npm install && cd ..
mvn spring-boot:run
```

## Code Style

- Follow standard Java code conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public APIs
- Keep methods focused and concise

## Pull Request Process

1. Update the README.md with details of changes if applicable
2. Update the CHANGELOG.md with your changes
3. Ensure all tests pass
4. Request review from maintainers

## Reporting Issues

- Use GitHub Issues to report bugs
- Include detailed information about the issue
- Provide steps to reproduce
- Include error messages and stack traces

## Feature Requests

We welcome feature requests! Please:
- Check if the feature has already been requested
- Provide a clear description of the feature
- Explain the use case and benefits

## Questions?

Feel free to open a discussion on GitHub Discussions for any questions.

Thank you for contributing! 
