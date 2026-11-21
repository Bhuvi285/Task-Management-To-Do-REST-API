# Task Management REST API

Phase 1 is a minimal Spring Boot application for learning IoC, dependency injection, bean management, and basic Spring MVC request handling.

## Technology stack

- Java 21
- Spring Boot 3.5.x
- Spring Data JPA and Hibernate
- H2 in-memory database
- Maven
- Embedded Apache Tomcat
- JUnit 5

## Phase 1 objectives

- Understand `SpringApplication.run(...)`
- Understand `ApplicationContext` and bean creation
- Understand component scanning and stereotype annotations
- Use constructor-based dependency injection
- Understand the default singleton bean scope
- Understand the Spring MVC HTTP request lifecycle
- Understand JDBC, JPA, Hibernate, and Spring Data JPA
- Map entities and a Many-to-One User-Task relationship
- Demonstrate repositories, persistence context, and dirty checking

## Folder structure

```text
task-management-api/
├── src/main/java/com/taskmanager/
│   ├── TaskManagementApplication.java
│   ├── controller/TestController.java
│   └── service/TestService.java
├── src/main/resources/application.properties
├── src/test/java/com/taskmanager/
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

## Run the application

Requirements: Java 21 and Maven, or use the Maven wrapper after it is generated.

```powershell
cd task-management-api
.\mvnw.cmd spring-boot:run
```

The application starts with embedded Tomcat on port 8080.

## API endpoint

```text
GET http://localhost:8080/api/v1/test
```

Expected response:

```text
Spring Boot IoC and DI are working successfully!
```

Test it with PowerShell:

```powershell
Invoke-RestMethod http://localhost:8080/api/v1/test
```

## IoC and DI

IoC (Inversion of Control) means Spring, rather than application code, controls object creation and wiring. The IoC container creates Spring-managed objects and supplies their dependencies.

DI (Dependency Injection) is how the container gives those dependencies to an object. Constructor injection is preferred because required dependencies are explicit, immutable through `private final`, and easy to test.

## Spring-managed object versus ordinary object

`TestService` is Spring-managed because `@Service` makes it eligible for component scanning. Spring creates and stores a bean in the `ApplicationContext`.

An object created with `new TestService()` is an ordinary Java object and is not registered or managed by Spring.

## Singleton bean scope

The default bean scope is singleton. Within one `ApplicationContext`, repeated calls to `getBean(TestService.class)` return the same bean instance. Singleton here means one container-managed bean instance; it does not mean the Java class prevents multiple instances.

## Bean lifecycle, simplified

1. Component scanning discovers `@Service`, `@RestController`, and similar classes.
2. The container creates bean definitions.
3. Spring instantiates beans and resolves constructor dependencies.
4. Spring prepares the fully wired bean and registers it in the singleton cache.
5. The application uses the bean through injected references or `ApplicationContext`.

## Application startup

`SpringApplication.run(...)`:

1. Starts Spring Boot bootstrap processing.
2. Creates the `ApplicationContext`.
3. Reads auto-configuration metadata.
4. Performs component scanning for `com.taskmanager` and subpackages.
5. Creates beans and performs dependency injection.
6. Starts embedded Tomcat.

`@SpringBootApplication` combines:

- `@SpringBootConfiguration`: marks the class as the configuration source
- `@EnableAutoConfiguration`: enables Spring Boot auto-configuration
- `@ComponentScan`: discovers components from the base package and subpackages

## ApplicationContext versus BeanFactory

`BeanFactory` is Spring's basic bean container and bean lookup mechanism. `ApplicationContext` is a higher-level container built on it, adding features such as component scanning, internationalization, application events, environment access, and easier integration with Spring MVC.

## HTTP request lifecycle

```text
Browser/Client
    |
    | GET /api/v1/test
    v
Embedded Tomcat (Servlet container)
    |
    v
DispatcherServlet
    |
    v
HandlerMapping -> TestController#getMessage
    |
    v
HandlerAdapter invokes the controller method
    |
    v
TestController -> TestService#getMessage()
    |
    v
String result
    |
    v
HttpMessageConverter writes the response
    |
    v
HTTP 200 + response text
```
