# Spring Boot Learning Concepts
## Phase 1 — Project Setup, IoC, Dependency Injection & Spring MVC

**Project:** Task Management REST API  
**Java:** 21  
**Framework:** Spring Boot 3.x  
**Build Tool:** Maven  
**IDE:** IntelliJ IDEA

---

# 1. Understanding Java, Spring Framework and Spring Boot

### Java
Java is a programming language used to develop applications. It provides object-oriented programming, collections, multithreading, exception handling and other core programming capabilities.

### Spring Framework
Spring is a Java application development framework that simplifies enterprise application development.

Its important features include:
- IoC (Inversion of Control).
- Dependency Injection.
- Aspect-Oriented Programming (AOP).
- Transaction Management.
- Spring MVC.
- Integration with databases and security frameworks.

### Spring Boot
Spring Boot is built on top of the Spring Framework. It simplifies application configuration and development by providing:

- Auto-configuration.
- Starter dependencies.
- Embedded web servers.
- Externalized configuration.
- Production-ready features through Spring Boot Actuator.

**Example:** Instead of manually configuring a web server and Spring MVC infrastructure, Spring Boot can automatically configure much of it when we add `spring-boot-starter-web`.

### Difference

| Java | Spring Framework | Spring Boot |
|---|---|---|
| Programming language | Application framework | Built on Spring Framework |
| Provides language features | Provides IoC and DI | Simplifies Spring configuration |
| Creates Java objects | Manages application components | Automates application setup |

# 2. Maven and pom.xml

### What is Maven?

Maven is a build automation and dependency management tool used in Java projects.

It helps us:
- Download external libraries.
- Manage dependencies.
- Compile Java source code.
- Run tests.
- Package applications into JAR files.
- Manage project builds.

### What is pom.xml?

POM stands for Project Object Model.

`pom.xml` is the main configuration file of a Maven project.

It defines:
- Project information.
- Dependencies.
- Java version.
- Build plugins.
- Parent project configuration.

### Maven Dependency

Example:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

This adds Spring Boot's web starter to the project.

### What are Transitive Dependencies?

When we add one dependency, it may require other libraries to work.

Maven automatically resolves these additional dependencies.

For example:

`spring-boot-starter-web` brings in the required Spring MVC and embedded web-server dependencies.

### What is a Spring Boot Starter?

A starter is a convenient dependency that groups commonly required libraries for a particular functionality.

Examples:

- `spring-boot-starter-web` — Web and REST API development.
- `spring-boot-starter-data-jpa` — JPA and database access.
- `spring-boot-starter-security` — Application security.
- `spring-boot-starter-validation` — Input validation.
- `spring-boot-starter-test` — Testing support.

# 3. IoC — Inversion of Control

### Definition

IoC stands for Inversion of Control.

It is a design principle in which the responsibility of creating and managing application objects is transferred from the developer's code to an external container or framework.

In Spring Boot, the IoC container manages application objects.

### Without IoC

```java
public class TaskController {

    private TaskService taskService =
            new TaskService();

}
```

Here, the developer manually creates the dependency using `new`.

The controller controls object creation.

### With IoC

```java
@Service
public class TaskService {

}
```

Spring discovers and manages the TaskService object.

The developer no longer needs to manually create and manage this dependency.

### Why is IoC important?

1. Reduces tight coupling.
2. Centralizes object management.
3. Makes applications easier to maintain.
4. Improves testability.
5. Makes dependency replacement easier.

**Remember:** IoC is a principle. Spring's IoC container is the mechanism that manages application objects.

# 4. IoC Container

### Definition

The IoC container is responsible for creating, configuring, managing and maintaining the lifecycle of Spring Beans.

Spring provides two major container interfaces:

1. BeanFactory.
2. ApplicationContext.

### BeanFactory

BeanFactory is the fundamental Spring IoC container interface.

It provides basic bean management and dependency injection functionality.

### ApplicationContext

ApplicationContext extends BeanFactory and provides additional enterprise application features.

Examples:
- Event publishing.
- Internationalization.
- Resource loading.
- Integration with Spring's annotation-based configuration.
- Application lifecycle management.

Spring Boot applications generally use ApplicationContext.

### How does ApplicationContext work?

When Spring Boot starts:

1. Spring creates the ApplicationContext.
2. It discovers component classes.
3. It registers bean definitions.
4. It creates and configures required beans.
5. It resolves dependencies.
6. It makes the beans available to the application.

Example:

```java
ConfigurableApplicationContext context =
        SpringApplication.run(
            TaskManagementApplication.class,
            args
        );
```

Retrieving a bean:

```java
TaskService service =
        context.getBean(TaskService.class);
```

**Remember:** ApplicationContext is the container; a Bean is an object managed by that container.

# 5. Spring Bean

### Definition

A Spring Bean is an ordinary Java object that is instantiated, configured and managed by the Spring IoC container.

Not every Java object is a Spring Bean.

### Example

```java
@Service
public class TaskService {

    public void createTask() {
        System.out.println("Task created");
    }
}
```

Here, Spring discovers and registers TaskService as a managed component.

### Important Spring annotations

| Annotation | Purpose |
|---|---|
| `@Component` | General-purpose Spring component |
| `@Service` | Service/business logic component |
| `@Repository` | Data access component |
| `@Controller` | Spring MVC controller |
| `@RestController` | REST controller |
| `@Configuration` | Configuration class |
| `@Bean` | Registers an object returned by a configuration method |

### @Component vs @Service vs @Repository

All three are component stereotypes and are discovered through component scanning.

- `@Component`: General-purpose component.
- `@Service`: Expresses service-layer responsibility.
- `@Repository`: Expresses persistence-layer responsibility and supports persistence exception translation.

They communicate the architectural role of a class.

# 6. Component Scanning

### Definition

Component scanning is the process through which Spring searches for classes annotated with component stereotype annotations and registers their bean definitions.

Example:

```java
@SpringBootApplication
public class TaskManagementApplication {

    public static void main(String[] args) {

        SpringApplication.run(
            TaskManagementApplication.class,
            args
        );

    }
}
```

Because the main class is in:

`com.taskmanager`

Spring scans that package and its subpackages by default.

For example:

```text
com.taskmanager
│
├── controller
├── service
├── repository
├── entity
└── config
```

This is why we keep all application packages under the main package.

# 7. @SpringBootApplication

### Definition

`@SpringBootApplication` is the main annotation used to configure a Spring Boot application.

It combines three important annotations:

### 1. @SpringBootConfiguration

Identifies the class as a source of Spring configuration.

It is a specialized form of `@Configuration`.

### 2. @EnableAutoConfiguration

Enables Spring Boot's auto-configuration mechanism.

Spring Boot examines the classpath, existing beans and configuration properties to determine which configurations are appropriate.

Example:

When Spring Web dependencies are present, Spring Boot can configure the web application infrastructure.

### 3. @ComponentScan

Enables component scanning to discover Spring-managed components.

**Remember:** `@SpringBootApplication` combines configuration, auto-configuration and component scanning.

# 8. Dependency Injection (DI)

### Definition

Dependency Injection is a design technique in which an object's required dependencies are supplied from outside instead of being created directly inside that object.

### Example: Constructor Injection

```java
@RestController
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {

        this.taskService = taskService;

    }
}
```

Here:

- TaskController depends on TaskService.
- Spring manages both objects.
- Spring supplies the TaskService bean through the constructor.

We do not need to write `new TaskService()`.

### Three types of Dependency Injection

**1. Constructor Injection**

Dependencies are supplied through the constructor.

```java
public TaskController(TaskService taskService) {
    this.taskService = taskService;
}
```

**2. Setter Injection**

Dependencies are supplied through setter methods.

```java
@Autowired
public void setTaskService(TaskService taskService) {
    this.taskService = taskService;
}
```

**3. Field Injection**

Dependencies are injected directly into fields.

```java
@Autowired
private TaskService taskService;
```

### Why prefer Constructor Injection?

- Dependencies are explicitly declared.
- Required dependencies cannot remain uninitialized after successful construction.
- Fields can be declared `final`.
- Classes are easier to unit test.
- Dependencies can be supplied manually in tests.
- It avoids reflection-based field injection.

Spring automatically uses the only constructor of a Spring-managed class, so `@Autowired` is unnecessary in that case.

# 9. IoC vs Dependency Injection

These concepts are related but not identical.

| IoC | Dependency Injection |
|---|---|
| Design principle | Design technique |
| Transfers control to a container/framework | Supplies required dependencies |
| Controls object management | Handles object relationships |
| Broader concept | One way to implement IoC |

**Example:**

IoC means Spring controls the creation and management of TaskService.

DI means Spring supplies TaskService to TaskController.

# 10. Spring Bean Lifecycle

### Definition

The Bean Lifecycle describes the stages through which a Spring-managed bean passes, from creation to destruction.

### Simplified lifecycle

1. Instantiation — Spring creates the bean object.
2. Dependency Injection — Spring injects required dependencies.
3. Aware callbacks — Applicable Aware interfaces receive container-related information.
4. Before initialization — BeanPostProcessor callbacks execute.
5. Initialization — Initialization callbacks such as `@PostConstruct` execute.
6. After initialization — BeanPostProcessor callbacks execute again.
7. Ready for use — Bean is available to the application.
8. Destruction — Applicable destruction callbacks execute when the context shuts down.

### Important lifecycle annotations

| Annotation | Purpose |
|---|---|
| `@PostConstruct` | Executes during bean initialization |
| `@PreDestroy` | Executes during bean destruction |

These annotations are provided through Jakarta annotations in modern Spring Boot.

### What is BeanPostProcessor?

BeanPostProcessor is a Spring extension point that allows processing of bean instances before and after initialization.

It is important for understanding how Spring infrastructure creates proxies and applies certain framework features.

**Remember:** This is a simplified lifecycle. Actual bean creation can involve additional steps, such as dependency resolution, initialization callbacks and proxy creation.

# 11. Spring Bean Scope

### Definition

Bean scope defines how many instances of a bean Spring creates and how long those instances are associated with a particular scope.

### Common Bean Scopes

| Scope | Description |
|---|---|
| Singleton | One shared instance per ApplicationContext |
| Prototype | A new instance each time the bean is requested from the container |
| Request | One instance per HTTP request |
| Session | One instance per HTTP session |
| Application | One instance per ServletContext |
| WebSocket | One instance per WebSocket session |

### Singleton Scope

Singleton is the default Spring Bean scope.

Example:

```java
TaskService service1 =
        context.getBean(TaskService.class);

TaskService service2 =
        context.getBean(TaskService.class);

System.out.println(service1 == service2);
```

Output:

```text
true
```

Both references point to the same bean instance.

### Important distinction

Spring Singleton is not the same as the Java Singleton Design Pattern.

- Java Singleton: A class-level design pattern that restricts object creation.
- Spring Singleton: One managed bean instance per ApplicationContext.

**Important:** Singleton beans are shared across requests. Avoid storing request-specific mutable data in singleton service fields.

# 12. Spring MVC

### Definition

Spring MVC is a web framework built on the Model-View-Controller architectural pattern.

It handles HTTP requests and coordinates controller execution.

In our REST API, controllers receive requests and return data, usually JSON.

### Important components

**1. DispatcherServlet**

The Front Controller of Spring MVC.

It coordinates request processing and delegates work to appropriate components.

**2. HandlerMapping**

Identifies which controller method should handle a particular request.

**3. HandlerAdapter**

Invokes the selected handler using the appropriate execution mechanism.

**4. Controller**

Processes the HTTP request at the application boundary.

**5. HttpMessageConverter**

Converts HTTP request and response bodies between formats such as JSON and Java objects.

**6. ViewResolver**

Resolves view names into views in traditional MVC applications. It is generally not needed for a REST endpoint that directly returns response data.

# 13. DispatcherServlet — HTTP Request Lifecycle

### Definition

DispatcherServlet is the central Front Controller in Spring MVC.

It receives incoming requests and coordinates their processing.

### Request Example

```http
GET http://localhost:8080/api/v1/test
```

### Internal execution flow

**Step 1: Client**

Postman or a browser sends an HTTP request.

**Step 2: Embedded Tomcat**

Tomcat receives the request and passes it through the Servlet infrastructure.

**Step 3: DispatcherServlet**

The request reaches Spring MVC's DispatcherServlet.

**Step 4: HandlerMapping**

Spring identifies the matching controller method using the request mapping information.

**Step 5: HandlerAdapter**

The appropriate HandlerAdapter invokes the controller method.

**Step 6: Controller**

TestController receives the request.

**Step 7: Service**

The controller calls TestService to execute the application logic.

**Step 8: Response Conversion**

The return value is processed by an appropriate HttpMessageConverter.

For our String response, Spring writes the returned string to the HTTP response.

**Step 9: Client Response**

The response is sent back to Postman or the browser.

### Request Flow Diagram

```text
Client (Postman / Browser)
          |
          v
    Embedded Tomcat
          |
          v
    DispatcherServlet
          |
          v
     HandlerMapping
          |
          v
     HandlerAdapter
          |
          v
      Controller
          |
          v
       Service
          |
          v
   Return Response
          |
          v
  HttpMessageConverter
          |
          v
    HTTP Response
```

# 14. Spring Boot Application Startup Lifecycle

When we execute:

```java
SpringApplication.run(
    TaskManagementApplication.class,
    args
);
```

The simplified startup process is:

1. Spring Boot prepares the application environment.
2. Determines the appropriate application context type.
3. Creates the ApplicationContext.
4. Loads and processes configuration classes.
5. Performs component scanning and auto-configuration.
6. Registers bean definitions.
7. Creates and initializes required singleton beans.
8. Resolves dependencies and performs dependency injection.
9. Starts the embedded web server for a servlet-based web application.
10. Publishes application-ready events.

After startup, the application can receive HTTP requests.

# 15. Our Phase 1 Practical Implementation

### Project Structure

```text
task-management-api/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/taskmanager/
│   │   │       ├── TaskManagementApplication.java
│   │   │       ├── controller/
│   │   │       │   └── TestController.java
│   │   │       └── service/
│   │   │           └── TestService.java
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│
├── pom.xml
└── README.md
```

### Main Application

```java
@SpringBootApplication
public class TaskManagementApplication {

    public static void main(String[] args) {

        SpringApplication.run(
            TaskManagementApplication.class,
            args
        );

    }
}
```

### Service

```java
@Service
public class TestService {

    public String getMessage() {

        return "Spring Boot IoC and DI are working successfully!";

    }
}
```

### Controller

```java
@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {

        this.testService = testService;

    }

    @GetMapping
    public String testApplication() {

        return testService.getMessage();

    }
}
```

### API Testing

HTTP Method: GET

Endpoint:

```text
http://localhost:8080/api/v1/test
```

Expected Response:

```text
Spring Boot IoC and DI are working successfully!
```

# 16. Important Interview Questions

| Question | Short Answer |
|---|---|
| What is Spring Boot? | A framework built on Spring that simplifies application setup and configuration. |
| What is IoC? | A principle where control of object creation and management is transferred to a container. |
| What is DI? | Supplying an object's dependencies from outside. |
| What is a Spring Bean? | An object managed by the Spring IoC container. |
| What is ApplicationContext? | Spring's feature-rich IoC container interface. |
| What is BeanFactory? | The fundamental Spring IoC container interface. |
| What is component scanning? | Discovering and registering eligible Spring components. |
| What is @SpringBootApplication? | A combination of configuration, auto-configuration and component scanning. |
| Why constructor injection? | It makes required dependencies explicit, supports immutability and improves testability. |
| What is singleton scope? | One shared bean instance per ApplicationContext. |
| What is DispatcherServlet? | The Front Controller of Spring MVC. |
| What is HandlerMapping? | Identifies the handler for an incoming request. |
| What is HandlerAdapter? | Invokes the selected handler. |
| What is HttpMessageConverter? | Converts HTTP bodies between Java objects and supported formats. |
| What is Maven? | A Java build automation and dependency management tool. |
| What are transitive dependencies? | Dependencies required by other dependencies. |

# 17. Phase 1 Final Revision — Most Important Concepts

Before moving to Phase 2, make sure you understand these relationships:

1. **Spring Boot → Spring Framework:** Spring Boot simplifies the configuration and development of Spring applications.

2. **IoC → ApplicationContext:** Spring's container manages application objects.

3. **Bean → Spring-managed object:** A bean is an object managed by Spring.

4. **Component Scanning → Bean Registration:** Spring discovers eligible annotated classes.

5. **DI → Dependency Management:** Spring supplies required dependencies to objects.

6. **Constructor Injection → Preferred DI:** Dependencies are supplied through constructors.

7. **Bean Lifecycle → Object Management:** Spring controls bean initialization and applicable destruction callbacks.

8. **Singleton Scope → Shared Instance:** The default bean scope provides one instance per ApplicationContext.

9. **Spring MVC → HTTP Handling:** Spring MVC processes incoming web requests.

10. **DispatcherServlet → Front Controller:** It coordinates the request lifecycle.

11. **Maven → Build and Dependencies:** It manages external libraries and application builds.

12. **SpringApplication.run() → Application Startup:** It bootstraps the Spring application and starts the web server when configured.

---

## Phase 1 Completion

**What we have built:**

A basic Spring Boot application that demonstrates:

- Spring IoC Container.
- ApplicationContext.
- Bean creation and management.
- Constructor-based Dependency Injection.
- Singleton Bean Scope.
- Spring MVC request processing.
- REST endpoint creation.

**What we have intentionally not implemented yet:**

- Database connectivity.
- JPA and Hibernate.
- Entity relationships.
- Authentication and Authorization.
- JWT.
- OAuth2.
- Task CRUD.

These will be introduced in their respective development phases.

**Learning Principle:**

Do not memorize annotations without understanding their internal responsibilities. Always understand what Spring does when an annotation is processed and what happens when a method is called.

The objective is to become capable of designing, debugging and explaining a Spring Boot backend independently.