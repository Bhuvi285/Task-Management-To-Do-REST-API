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



==========================================


# Phase 2 — Database, JPA, Hibernate & Persistence

## 1. Understanding the Database Technology Stack

Before working with Spring Data JPA, understand the responsibility of each technology.

### 1.1 JDBC (Java Database Connectivity)

JDBC is a Java API that allows Java applications to communicate with relational databases.

Using JDBC, developers can:
- Establish database connections.
- Execute SQL queries.
- Insert, update and delete records.
- Retrieve query results.
- Manage database transactions.

Example:

```java
Connection connection = DriverManager.getConnection(
    "jdbc:mysql://localhost:3306/taskdb",
    "root",
    "password"
);

PreparedStatement statement = connection.prepareStatement(
    "SELECT * FROM users WHERE id = ?"
);

statement.setLong(1, 1L);

ResultSet resultSet = statement.executeQuery();
```

**Problem with raw JDBC:** Developers need to write SQL, manage connections, map result sets to Java objects, and handle repetitive database operations.

### 1.2 JPA (Jakarta Persistence API)

JPA is a specification that defines how Java objects should be mapped to relational database tables.

It provides concepts and APIs for:
- Entity mapping.
- Relationships between entities.
- Persistence operations.
- Entity lifecycle.
- Queries.
- Transaction-related persistence behavior.

Important: **JPA is a specification, not an implementation.**

It defines what an ORM provider should do, but another technology must implement it.

### 1.3 Hibernate

Hibernate is an ORM (Object-Relational Mapping) framework and a popular implementation of the JPA specification.

Its responsibilities include:
- Mapping Java objects to database tables.
- Generating SQL queries.
- Managing entity states.
- Maintaining the Persistence Context.
- Performing dirty checking.
- Managing lazy loading.
- Executing database operations through JDBC.

For example, instead of writing:

```sql
INSERT INTO users (name, email)
VALUES ('John', 'john@example.com');
```

We can work with a Java object:

```java
User user = new User("John", "john@example.com");

entityManager.persist(user);
```

Hibernate generates and executes the required SQL.

### 1.4 Spring Data JPA

Spring Data JPA is a Spring abstraction that simplifies database access.

It provides:
- Repository interfaces.
- Predefined CRUD operations.
- Derived query methods.
- Pagination and sorting support.
- Integration with JPA providers.

Example:

```java
public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
```

We do not need to manually implement this interface. Spring Data creates the repository implementation infrastructure and injects a repository proxy.

### 1.5 How All These Technologies Work Together

```text
Spring Boot Application
        |
        v
Spring Data JPA
        |
        v
Repository Proxy
        |
        v
JPA EntityManager
        |
        v
Hibernate ORM
        |
        v
JDBC
        |
        v
HikariCP Connection Pool
        |
        v
H2 / MySQL / PostgreSQL
```

| Technology | Main Responsibility |
|---|---|
| JDBC | Java-to-database connectivity |
| JPA | Persistence specification |
| Hibernate | ORM implementation |
| Spring Data JPA | Repository abstraction |
| HikariCP | Database connection pooling |
| H2 | Database engine used in the initial project phase |

**Interview question: Is Spring Data JPA the same as Hibernate?**

No. Spring Data JPA provides repository abstractions, while Hibernate is an ORM provider that implements JPA. Spring Data JPA commonly uses Hibernate underneath.

---

## 2. ORM — Object Relational Mapping

ORM stands for Object Relational Mapping.

It connects two different worlds:

- Object-oriented programming: Java classes and objects.
- Relational databases: Tables, rows, columns and foreign keys.

Example:

Java entity:

```java
public class User {

    private Long id;
    private String name;
    private String email;
}
```

Database table:

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255)
);
```

The ORM framework maps:

| Java | Database |
|---|---|
| Class | Table |
| Object | Row |
| Field | Column |
| Object ID | Primary key |
| Object relationship | Foreign key relationship |

### Why ORM?

Without ORM, developers repeatedly write SQL and manually convert database rows into Java objects.

With ORM, developers can work with Java objects while Hibernate manages the SQL operations.

**Important:** ORM does not eliminate SQL. It generates SQL behind the scenes, and developers should still understand SQL for debugging and performance optimization.

---

## 3. JPA Entity and Entity Mapping

An entity is a Java class whose objects can be persisted in a relational database.

An entity represents a database table.

### 3.1 @Entity

```java
@Entity
public class User {
}
```

`@Entity` tells the JPA provider that this class is a persistent entity.

### 3.2 @Table

```java
@Entity
@Table(name = "users")
public class User {
}
```

`@Table` specifies the database table name.

### 3.3 @Id

```java
@Id
private Long id;
```

Defines the primary key of the entity.

Every JPA entity must have an identifier.

### 3.4 @GeneratedValue

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

Defines how primary-key values are generated.

Common strategies:

| Strategy | Explanation |
|---|---|
| IDENTITY | Database-generated identity value |
| SEQUENCE | Database sequence generates IDs |
| TABLE | Uses a database table to manage ID generation |
| AUTO | Provider chooses a suitable strategy |

Our Phase 2 entities use `GenerationType.IDENTITY`.

It works with H2 and is commonly supported by relational databases. When migrating to PostgreSQL, the identifier strategy and generated DDL should be reviewed.

### 3.5 @Column

```java
@Column(nullable = false, unique = true)
private String email;
```

Common attributes:

- `nullable = false`: Column must not contain NULL.
- `unique = true`: Column values must be unique.
- `length`: Specifies the column's intended maximum length.
- `name`: Specifies the database column name.

Remember: `@Column(nullable = false)` is a database mapping constraint. It is not a replacement for validating incoming API data.

### 3.6 @Enumerated

```java
@Enumerated(EnumType.STRING)
private TaskStatus status;
```

Stores the enum name in the database.

For example:

```java
public enum TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}
```

`EnumType.STRING` stores values such as `PENDING`.

Why prefer STRING over ORDINAL?

- ORDINAL stores numeric positions such as 0, 1 and 2.
- Reordering enum constants can change the meaning of existing numeric values.
- STRING stores readable names and avoids that particular problem.

---

## 4. Entity Relationships

Relational databases use foreign keys to connect tables.

JPA provides annotations to represent those relationships in Java.

Four common relationship types:

| Relationship | Meaning |
|---|---|
| @OneToOne | One record relates to one record |
| @OneToMany | One record relates to multiple records |
| @ManyToOne | Multiple records relate to one record |
| @ManyToMany | Multiple records relate to multiple records |

### 4.1 Our User–Task Relationship

One user can create multiple tasks.

Each task belongs to one user.

```text
User
+------------+
| id         |
| name       |
| email      |
+------------+
      |
      | 1
      |
      | has many
      |
      | *
Task
+------------+
| id         |
| title      |
| status     |
| user_id FK |
+------------+
```

The relationship is implemented in `Task`:

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "user_id", nullable = false)
private User user;
```

Explanation:

- `@ManyToOne`: Many tasks can belong to one user.
- `fetch = FetchType.LAZY`: The associated User is not necessarily loaded immediately.
- `optional = false`: A Task must have a User association.
- `@JoinColumn`: Specifies the foreign-key column.
- `nullable = false`: The foreign-key column must not be NULL.

### 4.2 Owning Side of a Relationship

The owning side controls the foreign-key mapping.

In our project, `Task` is the owning side because it contains:

```java
@JoinColumn(name = "user_id")
```

The foreign key is physically stored in the `tasks` table.

### 4.3 Unidirectional vs Bidirectional Mapping

**Unidirectional:**

```java
// Task.java

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private User user;
```

Only Task knows about User.

**Bidirectional:**

```java
// User.java

@OneToMany(mappedBy = "user")
private List<Task> tasks;
```

```java
// Task.java

@ManyToOne
@JoinColumn(name = "user_id")
private User user;
```

Both entities can navigate the relationship.

Our current Phase 2 implementation uses a unidirectional relationship.

This is a valid design. We can retrieve a user's tasks through `TaskRepository.findByUserId()` without maintaining a collection inside User.

### 4.4 Cascade Types

Cascade determines whether persistence operations on one entity are propagated to associated entities.

Common cascade types:

| Cascade | Meaning |
|---|---|
| PERSIST | Propagate persist |
| MERGE | Propagate merge |
| REMOVE | Propagate remove |
| REFRESH | Propagate refresh |
| DETACH | Propagate detach |
| ALL | Apply all cascade operations |

Our Task–User relationship does not use `CascadeType.ALL`.

This is a conservative choice: deleting or modifying a Task should not automatically delete its User.

---

## 5. Entity Lifecycle

A JPA entity moves through different lifecycle states.

The four important states are:

### 5.1 Transient State

An ordinary Java object that has not been associated with a Persistence Context.

```java
User user = new User(
    "John",
    "john@example.com"
);
```

At this stage:
- It is an ordinary Java object.
- It is not managed by Hibernate.
- Hibernate does not track its changes.
- It has not been persisted.

### 5.2 Managed State

An entity becomes managed when it is associated with a Persistence Context.

```java
entityManager.persist(user);
```

Now:
- Hibernate tracks the entity.
- The Persistence Context maintains its identity.
- Changes can be detected through dirty checking.
- SQL synchronization happens during flush.

### 5.3 Detached State

An entity becomes detached when it is no longer associated with its Persistence Context.

```java
entityManager.detach(user);
```

Now:
- Hibernate does not track changes to that object.
- Modifying its fields does not automatically update the database.
- It still represents an object with its existing identifier.

### 5.4 Removed State

An entity becomes removed when it is scheduled for deletion.

```java
entityManager.remove(user);
```

Now:
- It is marked for deletion.
- Hibernate executes the DELETE during flush.
- The deletion is finalized in the database when the transaction commits.

### Lifecycle Diagram

```text
       Create Object
             |
             v
         TRANSIENT
             |
             | persist()
             v
          MANAGED
          /     \
         /       \
 detach()       remove()
       |           |
       v           v
   DETACHED     REMOVED
       |
       | merge()
       v
   MANAGED
```

Important: `merge()` returns a managed instance. The original detached object does not itself become managed again.

---

## 6. EntityManager

`EntityManager` is a central JPA interface used to interact with the Persistence Context.

Important operations:

| Method | Responsibility |
|---|---|
| persist() | Makes a new entity managed |
| find() | Finds an entity by its primary key |
| merge() | Copies detached entity state into a managed instance |
| remove() | Marks a managed entity for deletion |
| detach() | Detaches an entity |
| clear() | Detaches all managed entities |
| flush() | Synchronizes pending changes with the database |

Example:

```java
entityManager.persist(user);

entityManager.flush();

entityManager.clear();
```

`EntityManager` is also used internally by Hibernate and Spring Data JPA to perform persistence operations.

---

## 7. Persistence Context

The Persistence Context is an important concept in Hibernate and JPA.

It is an environment that manages entity instances during persistence operations.

Think of it as a managed workspace for entities.

It provides:

1. First-level cache.
2. Entity identity management.
3. Dirty checking.
4. Tracking of managed entity changes.
5. Coordination of SQL synchronization.

### Example: First-Level Cache

```java
User first = entityManager.find(User.class, 1L);

User second = entityManager.find(User.class, 1L);

System.out.println(first == second);
```

Within the same Persistence Context, these lookups return the same managed Java object for that entity identity.

Output:

```text
true
```

This is the identity guarantee provided by the Persistence Context.

### Important Distinction

The Persistence Context is not simply the database.

- Database stores persistent records.
- Persistence Context manages entity instances.
- Hibernate synchronizes changes between the managed entities and database.

A Persistence Context is commonly associated with a transaction in Spring applications, but its lifetime is not universally identical to a transaction in every possible JPA configuration.

---

## 8. Dirty Checking

Dirty checking is a Hibernate feature that detects changes made to managed entities.

It allows developers to modify an entity without explicitly calling `save()` again.

Example:

```java
@Transactional
public Task updateTask(Long taskId, String title) {

    Task task = taskRepository.findById(taskId)
        .orElseThrow();

    task.setTitle(title);

    return task;
}
```

Notice that there is no:

```java
taskRepository.save(task);
```

Why does the update still happen?

1. The service method starts a transaction.
2. The repository retrieves the Task.
3. Hibernate places the Task in the Persistence Context.
4. The Task is now managed.
5. We modify its title.
6. Hibernate detects the changed field.
7. During flush, Hibernate generates an UPDATE statement.
8. The transaction commits.

Conceptually:

```sql
UPDATE tasks
SET title = ?
WHERE id = ?;
```

**Important:** Dirty checking works for managed entities. Modifying a detached entity does not automatically trigger an UPDATE.

This is why our Phase 2 `updateTask()` implementation does not need to call `save()` after modifying the retrieved managed entity.

---

## 9. Flush vs Commit

These two concepts are related but are not the same.

### Flush

Flush synchronizes pending changes in the Persistence Context with the database.

```java
entityManager.flush();
```

It can execute:
- INSERT
- UPDATE
- DELETE

But flush does not commit the transaction.

### Commit

Commit finalizes the database transaction.

```text
Transaction
    |
    v
Changes made to entities
    |
    v
Flush
    |
    v
SQL executed
    |
    v
Database COMMIT
    |
    v
Transaction completed
```

### Comparison

| Flush | Commit |
|---|---|
| Synchronizes pending changes | Finalizes transaction |
| Executes SQL when necessary | Makes transaction durable |
| Does not end transaction | Ends transaction |
| Can happen multiple times | Normally completes once |

An important consequence: SQL can execute successfully during flush and still be rolled back later if the transaction fails.

---

## 10. Database Transactions

A transaction groups database operations into one logical unit of work.

Example:

A user creates a task.

```text
BEGIN TRANSACTION

1. Find User
2. Create Task
3. Save Task
4. Flush SQL

COMMIT
```

If an operation fails and the transaction is rolled back, the database does not retain the uncommitted changes.

### ACID Properties

| Property | Meaning |
|---|---|
| Atomicity | All operations succeed or the transaction is rolled back |
| Consistency | Database constraints and rules remain valid |
| Isolation | Concurrent transactions are isolated according to the configured isolation level |
| Durability | Committed changes are retained according to database guarantees |

### @Transactional

Spring provides `@Transactional` to manage transaction boundaries.

Example:

```java
@Transactional
public Task createTask(...) {
    // Database operations
}
```

Spring manages the transaction around the method invocation.

At a high level:

```text
Caller
  |
  v
Spring Transaction Proxy
  |
  v
Begin Transaction
  |
  v
Execute Service Method
  |
  v
Flush and Commit
  |
  v
Return Result
```

If an unchecked exception escapes the transactional method, Spring's default rollback rules generally trigger rollback.

Checked exceptions do not trigger rollback by default unless configured.

### Read-Only Transactions

```java
@Transactional(readOnly = true)
public List<Task> getAllTasks() {
    return taskRepository.findAll();
}
```

`readOnly = true` communicates that the transaction is intended for reading.

It can allow optimizations by Spring and the persistence provider.

It is not a universal guarantee that writes are impossible at the database level.

### Important: Spring Transaction Proxy

Spring commonly implements declarative transactions using proxies.

Consider:

```java
@Transactional
public Task updateTask(Long id) {
    Task task = getTaskById(id);
    return task;
}

@Transactional(readOnly = true)
public Task getTaskById(Long id) {
    // Find Task
}
```

When `updateTask()` calls `getTaskById()` internally using `this.getTaskById()` implicitly, the call does not pass through the Spring proxy again.

Therefore, the inner method's separate transactional annotation is not independently applied.

In our current implementation this is safe because the outer `updateTask()` already has a write transaction.

---

## 11. Lazy Loading and Eager Loading

JPA relationships can use different fetching strategies.

### Lazy Loading

```java
@ManyToOne(fetch = FetchType.LAZY)
private User user;
```

Lazy loading means the related entity does not necessarily need to be loaded immediately.

Hibernate can use a proxy representing the related entity.

When the application accesses the relationship, Hibernate may execute another SELECT to initialize it.

Example:

```java
Task task = taskRepository.findById(1L)
    .orElseThrow();

User user = task.getUser();

System.out.println(user.getName());
```

Accessing `getName()` may trigger the SQL needed to load the User.

### Eager Loading

```java
@ManyToOne(fetch = FetchType.EAGER)
private User user;
```

Eager loading requires the relationship to be fetched as part of loading the entity, although the provider is not required to use one particular SQL strategy.

### Comparison

| Lazy | Eager |
|---|---|
| Loads relationship when needed | Requires relationship to be fetched |
| Can reduce unnecessary loading | Can load unnecessary data |
| May need an active Persistence Context | Relationship is fetched as part of entity loading |
| Can cause N+1 problems | Can also create expensive queries |

Our Task entity uses LAZY loading for its User relationship.

### LazyInitializationException

Suppose the Persistence Context is closed and the application tries to access an uninitialized lazy relationship.

Hibernate may throw:

```text
LazyInitializationException
```

Why?

Because Hibernate no longer has an active session to load the relationship.

This is one reason why returning JPA entities directly from REST controllers can be risky.

---

## 12. N+1 Query Problem

N+1 is a database performance problem.

Suppose we retrieve 100 tasks.

```java
List<Task> tasks = taskRepository.findAll();
```

Then access the User for each task:

```java
for (Task task : tasks) {
    System.out.println(task.getUser().getName());
}
```

A possible SQL pattern is:

```text
1 query: Retrieve all tasks

100 queries: Retrieve each task's user
```

Total:

```text
1 + 100 = 101 queries
```

This is called the N+1 query problem.

### Common Solutions

- JPQL `JOIN FETCH`.
- Entity graphs.
- DTO projections.
- Batch fetching where appropriate.

Example JPQL:

```java
@Query("""
    SELECT t
    FROM Task t
    JOIN FETCH t.user
    WHERE t.id = :id
""")
Optional<Task> findTaskWithUserById(Long id);
```

This fetches the Task and its User through a fetch join.

The correct fetching strategy depends on the query and use case.

---

## 13. Spring Data JPA Repository

A repository is an abstraction responsible for data-access operations.

In our project:

```java
public interface TaskRepository
        extends JpaRepository<Task, Long> {
}
```

The generic parameters mean:

- `Task`: Entity type.
- `Long`: Primary-key type.

### Common JpaRepository Methods

| Method | Purpose |
|---|---|
| save() | Save an entity |
| findById() | Find by primary key |
| findAll() | Retrieve all entities |
| delete() | Delete an entity |
| deleteById() | Delete by ID |
| existsById() | Check whether ID exists |
| count() | Count records |

### How Does Spring Implement an Interface?

We only declare:

```java
public interface UserRepository
        extends JpaRepository<User, Long> {
}
```

Spring Data JPA creates repository infrastructure at runtime.

Conceptually:

```text
UserRepository Interface
        |
        v
Spring Data Repository Infrastructure
        |
        v
Repository Proxy
        |
        v
JPA Operations
        |
        v
Hibernate
```

The application injects the generated repository bean through constructor injection.

We do not manually write the basic CRUD implementation.

---

## 14. Derived Query Methods

Spring Data JPA can derive queries from repository method names.

Example:

```java
Optional<User> findByEmail(String email);
```

Spring understands:

```text
find By Email
```

Conceptually, it generates:

```sql
SELECT *
FROM users
WHERE email = ?;
```

### More Examples from Our Project

**Find tasks by status:**

```java
List<Task> findByStatus(TaskStatus status);
```

**Find tasks by user ID:**

```java
List<Task> findByUserId(Long userId);
```

Here `userId` refers to the `id` property of the associated `user` entity.

Conceptually:

```sql
SELECT *
FROM tasks
WHERE user_id = ?;
```

**Find tasks by title, ignoring case:**

```java
List<Task> findByTitleContainingIgnoreCase(String title);
```

This performs case-insensitive partial matching.

Conceptually:

```sql
SELECT *
FROM tasks
WHERE UPPER(title) LIKE UPPER(?);
```

### Why Use Derived Queries?

- Less boilerplate.
- No manual query implementation for simple operations.
- Easy integration with repository interfaces.

For complex queries, use `@Query`, JPQL, Criteria API, or other suitable approaches.

---

## 15. HikariCP Connection Pool

Creating a new database connection for every request is expensive.

Connection pooling solves this problem.

Spring Boot commonly uses HikariCP as its default JDBC connection pool.

### How It Works

```text
Spring Boot Application
        |
        v
HikariCP Connection Pool
   |       |       |
   v       v       v
 Conn 1  Conn 2  Conn 3
   |       |       |
   +-------+-------+
           |
           v
        Database
```

Instead of repeatedly creating and destroying database connections:

1. HikariCP maintains a pool of connections.
2. An application operation requests a connection.
3. HikariCP provides an available connection.
4. The operation executes database work.
5. The connection is returned to the pool.

**Important:** HikariCP is not an ORM and does not generate SQL. It manages JDBC database connections.

---

## 16. H2 Database

H2 is a lightweight relational database written in Java.

We use it during Phase 2 because:
- It is easy to configure.
- It supports SQL.
- It works well for development and tests.
- It can run in memory.
- It integrates with Spring Boot and Hibernate.

### In-Memory Database

A typical URL:

```properties
spring.datasource.url=jdbc:h2:mem:taskdb
```

This creates an in-memory database.

With an in-memory database, data generally does not survive application shutdown.

### H2 Console

The H2 Console provides a browser-based interface for inspecting the database.

It is useful during development, but should not be exposed as a production database administration interface.

### H2 vs PostgreSQL

| H2 | PostgreSQL |
|---|---|
| Lightweight | Full-featured relational database |
| Useful for local development and tests | Common production database |
| Can run in memory | Persistent database storage |
| Fast setup | Requires database deployment/configuration |
| May differ in SQL and behavior | Production target for our later phase |

Passing tests against H2 does not guarantee identical behavior on PostgreSQL. We should run integration tests against the production database engine before deployment.

---

## 17. Database Schema Generation

Hibernate can generate database tables based on entity mappings.

For example:

```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
}
```

Hibernate can create a corresponding table when schema generation is configured.

Common `ddl-auto` options:

| Value | Meaning |
|---|---|
| none | No automatic schema management |
| validate | Validate entity mappings against the schema |
| update | Attempt to update the schema |
| create | Create schema on startup |
| create-drop | Create schema and drop it when the application shuts down |

Our Phase 2 setup uses an H2 development database with schema creation configured for the development workflow.

**Production warning:** `create` and `create-drop` are not suitable for preserving production data. Production systems commonly use database migration tools such as Flyway or Liquibase.

---

## 18. Bean Validation vs Database Constraints

These are two different layers of protection.

### Bean Validation

Used to validate Java objects and incoming application data.

Examples:

```java
@NotBlank
private String title;

@Email
@NotBlank
private String email;

@Size(max = 500)
private String description;
```

### Database Constraints

Enforced by the database itself.

Examples:

```java
@Column(nullable = false, unique = true)
private String email;
```

### Comparison

| Bean Validation | Database Constraint |
|---|---|
| Validates application data | Protects database integrity |
| Usually runs before persistence | Enforced by the database |
| Provides useful validation messages | Prevents invalid database state |
| Examples: @NotBlank, @Email | Examples: NOT NULL, UNIQUE, FOREIGN KEY |

A robust application often needs both.

In our Phase 2 project, the validation dependency is present, but the architecture audit identified that validation annotations have not yet been applied.

We will use validation more fully when implementing REST request DTOs.

---

## 19. TDD — Test-Driven Development

TDD is a development approach where tests guide implementation.

The typical cycle:

```text
RED
 |
 v
Write a failing test
 |
 v
GREEN
 |
 v
Implement minimum required code
 |
 v
REFACTOR
 |
 v
Improve design without breaking tests
```

### RED

Write a test for behavior that does not exist yet.

The test fails because the required implementation is missing.

### GREEN

Implement the minimum code required to make the test pass.

### REFACTOR

Improve the implementation while keeping tests green.

### Testing Tools Used in Phase 2

**JUnit 5**

A Java testing framework used to write and execute tests.

**Mockito**

A mocking framework used to isolate a class from its dependencies.

**@DataJpaTest**

A Spring Boot test annotation that configures a JPA-focused test slice, commonly using an embedded database when available.

### Unit Test vs Integration Test

| Unit Test | Integration Test |
|---|---|
| Tests one component in isolation | Tests multiple components working together |
| Often uses mocks | Uses real infrastructure such as a database |
| Fast | Usually slower |
| Example: TaskServiceTest | Example: TaskRepositoryTest |

### Mockito Example

```java
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;
}
```

- `@Mock` creates mock dependencies.
- `@InjectMocks` creates the service and injects its mocks.
- The test can verify method calls and returned behavior without starting a database.

### @DataJpaTest Example

```java
@DataJpaTest
class UserRepositoryTest {
}
```

This loads the JPA test slice and allows us to test real repository and persistence behavior.

### Phase 2 Test Results

The architecture audit records:

| Test Class | Tests |
|---|---:|
| PersistenceBehaviorTest | 4 |
| TaskRepositoryTest | 3 |
| UserRepositoryTest | 3 |
| TaskServiceTest | 8 |
| TestServiceTest | 1 |
| **Total** | **19** |

Recorded final Maven result:

- 19 tests.
- 0 failures.
- 0 errors.
- 0 skipped.
- Build success.

The service unit tests verify orchestration and interactions. Repository and persistence tests verify real JPA behavior. These test types complement each other; neither replaces the other.

---

## 20. DTO and Entity Separation — Why It Matters

An Entity represents the database persistence model.

A DTO (Data Transfer Object) represents the data exchanged between application layers or across an API boundary.

Example Entity:

```java
@Entity
public class Task {

    @Id
    private Long id;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}
```

A future API should not automatically return the entity directly.

Instead, define:

```java
public class TaskResponseDTO {

    private Long id;
    private String title;
    private String status;
}
```

Why?

1. Prevents exposing internal database structure.
2. Avoids accidental exposure of sensitive fields.
3. Gives the API a stable response structure.
4. Helps control lazy-loaded relationships.
5. Separates persistence changes from API contract changes.

Our current Phase 2 service returns entities because the REST Task API has not yet been implemented. We will introduce DTO boundaries in the API implementation phase.

---

## 21. Important Phase 2 Architecture Observations

The following are findings from the current project audit, not additional implemented features.

| Observation | Explanation |
|---|---|
| Validation starter is installed | Validation annotations are not yet applied |
| Task uses LAZY User loading | Accessing User may trigger another query |
| TaskService uses transactions | Database work is organized around service operations |
| updateTask does not call save | It modifies a managed entity, so dirty checking handles synchronization |
| getTasksByUserId performs two queries | It checks user existence and then retrieves tasks |
| No pagination yet | A future API should avoid loading unlimited records |
| H2 is in-memory | Data is not intended to survive application shutdown |
| Open EntityManager in View is enabled by default | It should be configured intentionally, especially before REST development |
| No DTO layer yet | DTOs will be introduced when REST endpoints are built |
| No explicit rollback integration test | Transaction rollback behavior deserves a dedicated test |

### Open EntityManager in View (OSIV)

OSIV can keep the EntityManager available through the web request, even after the service transaction finishes.

This can make lazy loading appear to work in controllers or views.

However, it can hide unclear transaction boundaries and cause database queries during response rendering.

For a REST application, a common approach is to disable OSIV and fetch the required data within the service transaction, returning DTOs.

---

## 22. PostgreSQL Migration Preparation

The project starts with H2, but the eventual target is PostgreSQL.

Before migration, consider:

1. Add the PostgreSQL JDBC driver.
2. Configure a PostgreSQL datasource.
3. Use separate development and production profiles.
4. Review entity ID generation strategy.
5. Review generated SQL and database constraints.
6. Introduce Flyway or Liquibase for schema migrations.
7. Test relationships, queries and transactions against PostgreSQL.
8. Avoid using H2-specific behavior in application logic.

A useful distinction:

- JPA entity mappings describe persistence structure.
- Hibernate translates those mappings into database operations.
- PostgreSQL executes the generated SQL.
- Flyway or Liquibase manages controlled schema changes.

---

## 23. Phase 2 — Interview Questions

**Q1. What is ORM?**

ORM maps Java objects to relational database tables, allowing developers to work with objects instead of manually mapping every SQL result.

**Q2. What is JPA?**

JPA is a Java persistence specification that defines APIs and rules for mapping and managing relational data.

**Q3. Is Hibernate an implementation of JPA?**

Yes. Hibernate is a popular ORM framework that implements the JPA specification.

**Q4. What is Spring Data JPA?**

It simplifies persistence through repository abstractions, CRUD operations and derived query methods.

**Q5. What is an Entity?**

An entity is a Java class mapped to a database table and managed by a JPA provider.

**Q6. What is the Persistence Context?**

It is the environment that manages entity instances, identity and changes during persistence operations.

**Q7. What is dirty checking?**

Hibernate detects modifications to managed entities and generates the required SQL during flush.

**Q8. What is the difference between persist() and merge()?**

`persist()` makes a new entity managed. `merge()` copies state into a managed instance and returns that instance.

**Q9. What is the difference between flush and commit?**

Flush synchronizes pending changes with the database. Commit finalizes the transaction.

**Q10. What is lazy loading?**

Lazy loading postpones loading an associated entity until it is accessed, when the persistence context can initialize it.

**Q11. What is LazyInitializationException?**

It can occur when an uninitialized lazy association is accessed after the Hibernate session or Persistence Context is no longer available.

**Q12. What is the N+1 problem?**

It is a performance problem where one query retrieves a collection and additional queries are executed for associated data.

**Q13. What is @Transactional?**

It declares a transaction boundary managed by Spring.

**Q14. What is @ManyToOne?**

It represents a relationship where multiple records can reference one associated record.

**Q15. What is the owning side of a JPA relationship?**

The owning side controls the foreign-key mapping. In our project, Task owns the User relationship through `user_id`.

**Q16. Why use EnumType.STRING?**

It stores readable enum names and avoids the fragility of numeric ordinal values when enum constants are reordered.

**Q17. What is HikariCP?**

It is a JDBC connection pool that manages reusable database connections.

**Q18. What is @DataJpaTest?**

It configures a JPA-focused Spring test slice for testing repositories and persistence behavior.

**Q19. Why should we use DTOs instead of exposing entities?**

DTOs separate the API contract from database structure and help prevent accidental exposure of internal fields and lazy-loading problems.

**Q20. Why is create-drop unsuitable for production?**

It can drop the database schema when the application shuts down, resulting in loss of stored data.

---

## 24. Phase 2 Completion Checklist

- [ ] Understand JDBC, JPA, Hibernate and Spring Data JPA.
- [ ] Understand ORM.
- [ ] Understand entity annotations and primary-key generation.
- [ ] Understand User–Task relationships.
- [ ] Understand owning and inverse sides.
- [ ] Understand cascade types.
- [ ] Understand transient, managed, detached and removed states.
- [ ] Understand EntityManager.
- [ ] Understand Persistence Context and first-level cache.
- [ ] Understand dirty checking.
- [ ] Understand flush vs commit.
- [ ] Understand Spring transactions and rollback rules.
- [ ] Understand transaction proxies and self-invocation.
- [ ] Understand lazy loading and eager loading.
- [ ] Understand N+1 queries and fetch joins.
- [ ] Understand repository proxies and derived queries.
- [ ] Understand HikariCP and H2.
- [ ] Understand Bean Validation vs database constraints.
- [ ] Understand JUnit, Mockito and @DataJpaTest.
- [ ] Understand why entities and DTOs should be separated.
- [ ] Understand the preparation required for PostgreSQL.

**Phase 2 learning objective:** Be able to explain how a Java object becomes a database record, how Hibernate tracks and updates that object, how Spring manages transactions, and how Spring Data JPA communicates with the database.

---

# End of Phase 2

Next phase: **Spring Security Fundamentals, Security Filter Chain, Authentication, Authorization and JWT.**

Do not move to Phase 3 until the database and persistence concepts above are understood and the Phase 2 implementation has been reviewed.

---

# Phase 3 — Spring Security & JWT

## Spring Security overview

Spring Security is the framework used to protect application resources. It processes incoming requests through a chain of security filters and applies authentication and authorization rules before a request reaches a controller. This milestone establishes those rules only; JWT authentication, user lookup, registration, and login are intentionally deferred.

## SecurityFilterChain

`SecurityFilterChain` defines which security filters and request rules apply to incoming HTTP requests. The `SecurityConfig` class in `com.taskmanager.config` supplies a `SecurityFilterChain` bean. It disables form login and HTTP Basic, uses stateless session management, disables CSRF for this stateless REST foundation, permits only `GET /api/v1/test`, and requires authentication for every other request.

## Authentication vs authorization

Authentication establishes who is making a request. Authorization decides whether that authenticated identity is allowed to perform the requested action. This milestone configures authorization boundaries but does not yet provide a REST authentication mechanism or user identity implementation.

## Stateless vs stateful sessions

A stateful application stores authentication state on the server, commonly in an HTTP session, and associates subsequent requests with that session. A stateless API does not keep authentication state in a server-side session; each request must carry the information needed for authentication. The filter chain sets `SessionCreationPolicy.STATELESS` as preparation for a later token-based design. JWTs themselves are not part of this milestone.

## CSRF and stateless APIs

Cross-Site Request Forgery (CSRF) occurs when a browser is induced to send an authenticated request using credentials that the browser attaches automatically, such as a session cookie. CSRF protection is important for browser applications that authenticate with automatically sent credentials. This API foundation disables CSRF because it is configured as stateless and does not use cookie-backed session authentication. If the authentication design later uses browser-managed cookies, CSRF protection must be reconsidered.

## Public vs protected endpoints

A public endpoint can be called without authentication. A protected endpoint requires an authenticated request. For this milestone, `GET /api/v1/test` remains public for Phase 1 verification; all other application requests require authentication by default. No registration or login routes have been added.

## Implemented configuration

The Maven project includes `spring-boot-starter-security`, with `spring-security-test` available to tests. `SecurityConfig` explicitly disables CSRF, form login, and HTTP Basic, sets stateless session management, and permits only the existing test GET endpoint. The MockMvc tests verify the public endpoint, rejection of an unauthenticated request to another endpoint, and successful application-context startup. An authentication provider, `UserDetailsService`, JWT support, OAuth2, and credentials are outside Phase 3.1.



-----------------------------------------

# Phase 3.1 – Spring Security Foundation

## 1. What is Spring Security?

Spring Security is a framework used to secure Java applications. It provides mechanisms for:

- **Authentication:** Verifying who the user is.
- **Authorization:** Determining what an authenticated user is allowed to access.
- **Protection against common attacks:** Such as CSRF and session-related security risks.
- **Security integration:** Integrating authentication and authorization into Spring applications.

In our Task Management REST API, Spring Security will protect endpoints so that users can access only the resources they are authorized to use.

### Why do we need Spring Security?

Without security, anyone who knows an API endpoint may be able to access it.

For example:

```http
GET /api/v1/tasks
```

Without authentication, an unauthorized client might access task data.

With Spring Security, we can require authentication before allowing access.

---

## 2. Authentication vs Authorization

These are two fundamental concepts in application security.

### Authentication

Authentication verifies the identity of a user.

Example:
- User enters an email and password.
- The application verifies the credentials.
- If valid, the user is authenticated.

**Question:** Who are you?

### Authorization

Authorization determines whether an authenticated user has permission to perform a particular operation.

Example:
- A logged-in user can view their own tasks.
- The same user cannot access another user's private tasks.

**Question:** What are you allowed to do?

| Authentication | Authorization |
|---|---|
| Verifies identity | Checks permissions |
| Happens before authorization | Follows authentication |
| Checks credentials | Checks roles or authorities |
| Example: Login | Example: Access admin endpoint |

---

## 3. Spring Security Architecture

Spring Security integrates with the Servlet filter mechanism.

When an HTTP request reaches a Spring Boot application, security filters can inspect the request before it reaches the controller.

Simplified request flow:

```text
Client / Browser
       |
       v
HTTP Request
       |
       v
Servlet Container
       |
       v
Spring Security Filter Chain
       |
       v
Authentication / Authorization Checks
       |
       v
DispatcherServlet
       |
       v
Controller
       |
       v
Service
       |
       v
Repository
```

If the request is not permitted, Spring Security can reject it before the controller is executed.

### What is a Filter?

A filter is a component that intercepts HTTP requests and responses.

Filters can:
- Inspect incoming requests.
- Validate credentials or tokens.
- Reject unauthorized requests.
- Add or modify response headers.
- Continue the filter chain.

Spring Security uses multiple filters to implement its security functionality.

### What is SecurityFilterChain?

`SecurityFilterChain` defines which security filters should be applied to incoming requests and how HTTP security should be configured.

In our project, we created a bean of type `SecurityFilterChain` inside `SecurityConfig`.

---

## 4. Understanding SecurityConfig.java

Our current configuration:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .formLogin(formLogin -> formLogin.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .sessionManagement(sessionManagement ->
                sessionManagement.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.GET, "/api/v1/test")
                .permitAll()
                .anyRequest()
                .authenticated()
            );

        return http.build();
    }
}
```

Let's understand each annotation and method.

### 4.1 `@Configuration`

Indicates that the class contains Spring configuration.

Spring processes this class to register configuration and bean definitions in the application context.

### 4.2 `@EnableWebSecurity`

Enables Spring Security's web security support.

It allows us to configure web application security through Spring Security.

### 4.3 `@Bean`

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http)
```

The method returns a `SecurityFilterChain` object.

Spring registers the returned object as a bean in the ApplicationContext.

Spring Security uses this bean to apply our HTTP security rules.

### 4.4 `HttpSecurity`

`HttpSecurity` is an object used to configure HTTP security.

Through it, we configure:
- CSRF protection.
- Login mechanisms.
- Session management.
- Request authorization.
- Other security-related behavior.

---

## 5. Understanding the Security Configuration Methods

### 5.1 CSRF Protection

```java
.csrf(csrf -> csrf.disable())
```

CSRF stands for **Cross-Site Request Forgery**.

It is an attack in which a malicious website tricks a user's browser into sending an unwanted request to another website where the user is already authenticated.

For example:

1. A user logs into a banking website.
2. The browser stores an authenticated session cookie.
3. The user visits a malicious website.
4. That website attempts to make an unwanted request to the banking website.
5. The browser may automatically attach the session cookie.

CSRF protection helps prevent such attacks.

**Why did we disable CSRF in our project?**

Our current application is being configured as a stateless REST API. We plan to authenticate API requests using bearer tokens rather than automatically submitted session cookies.

For this architecture, disabling CSRF is a common configuration choice.

Important: If we later store refresh tokens in cookies, we must reconsider CSRF protection because browsers automatically send cookies with matching requests.

### 5.2 Disable Form Login

```java
.formLogin(formLogin -> formLogin.disable())
```

Spring Security can provide a default HTML login page.

However, our application is a REST API, not a traditional server-rendered login application.

We will implement authentication through REST endpoints instead.

Therefore, we disable the default form login mechanism.

### 5.3 Disable HTTP Basic Authentication

```java
.httpBasic(httpBasic -> httpBasic.disable())
```

HTTP Basic Authentication sends credentials through the HTTP `Authorization` header using the Basic authentication scheme.

Example:

```http
Authorization: Basic dXNlcjpwYXNzd29yZA==
```

The value is Base64-encoded, not encrypted.

HTTPS is essential when using Basic Authentication because Base64 does not provide confidentiality.

We disable HTTP Basic because our planned authentication mechanism is JWT-based bearer authentication.

### 5.4 Session Management

```java
.sessionManagement(sessionManagement ->
    sessionManagement.sessionCreationPolicy(
        SessionCreationPolicy.STATELESS
    )
)
```

This configures how Spring Security manages authentication across requests.

Common session creation policies:

| Policy | Meaning |
|---|---|
| ALWAYS | Always create an HTTP session |
| IF_REQUIRED | Create a session when required |
| NEVER | Do not create a session, but use an existing one if available |
| STATELESS | Do not create or use an HTTP session to obtain the security context |

We use `STATELESS` because our REST API will use JWT bearer tokens.

In a stateless architecture, each protected request must carry its authentication information, typically through an HTTP Authorization header.

Example:

```http
Authorization: Bearer <access-token>
```

The server validates the token for each request rather than relying on a login session stored on the server.

### 5.5 Request Authorization

```java
.authorizeHttpRequests(authorize -> authorize
    .requestMatchers(HttpMethod.GET, "/api/v1/test")
    .permitAll()
    .anyRequest()
    .authenticated()
)
```

This defines which requests are publicly accessible and which require authentication.

**`requestMatchers()`**

Selects requests based on HTTP methods and URL patterns.

**`permitAll()`**

Allows everyone to access the matched endpoint, even without authentication.

**`anyRequest()`**

Matches all remaining requests.

**`authenticated()`**

Requires the request to have an authenticated principal.

Our current rules:

| Endpoint | Access |
|---|---|
| `GET /api/v1/test` | Public |
| Any other request | Authentication required |

Later, when registration is implemented, we will also make the registration endpoint public.

---

## 6. What Happens When an Unauthorized Request Arrives?

Suppose a client sends:

```http
GET /api/v1/tasks
```

But the request does not contain valid authentication.

The simplified flow is:

1. The request enters the Spring Security filter chain.
2. Security checks the authentication associated with the request.
3. No valid authenticated principal is available.
4. The authorization rules require authentication.
5. Spring Security rejects the request.
6. The controller is not executed.

For a REST API, we will later configure appropriate JSON error responses, such as HTTP `401 Unauthorized`, rather than relying on default browser-oriented behavior.

---

## 7. Important HTTP Status Codes in Security

| Status | Meaning | Example |
|---|---|---|
| 200 OK | Request succeeded | Successfully fetching tasks |
| 201 Created | Resource created | User registration |
| 400 Bad Request | Invalid request data | Invalid email |
| 401 Unauthorized | Authentication is missing or invalid | Missing or invalid JWT |
| 403 Forbidden | Request is understood, but access is denied | Authenticated user lacks permission |
| 404 Not Found | Resource does not exist | Task ID not found |

**401 vs 403**

- `401 Unauthorized`: The request does not have valid authentication.
- `403 Forbidden`: The user is authenticated but does not have sufficient permission.

---

## 8. How We Tested Spring Security

We added `spring-security-test` as a test dependency and created `SecurityConfigTest`.

The security tests verify:

1. The public test endpoint can be accessed without authentication.
2. A protected endpoint rejects an unauthenticated request.
3. The Spring application context loads with the security configuration.

We also ran:

```bash
mvn clean test
```

**Phase 3.1 test result:**

- Total tests: 22
- Failures: 0
- Errors: 0

This includes the existing Phase 1 and Phase 2 tests, along with the new security tests.

---

## 9. Important Security Dependencies

We added these dependencies to `pom.xml`:

### Spring Security

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

Provides Spring Security integration for the application.

### Spring Security Test

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

Provides testing utilities for security-related application behavior.

For example, it supports testing authenticated and unauthenticated requests.

---

## 10. Important Concepts to Remember

| Concept | Meaning |
|---|---|
| Spring Security | Framework for authentication and authorization |
| Authentication | Verifying user identity |
| Authorization | Checking access permissions |
| Filter | Intercepts HTTP requests and responses |
| SecurityFilterChain | Defines the security filters applied to requests |
| HttpSecurity | Configures HTTP security rules |
| CSRF | Cross-Site Request Forgery |
| HTTP Basic | Authentication scheme that sends Base64-encoded credentials |
| Form Login | Spring Security's default form-based login mechanism |
| Stateless | Authentication does not depend on an HTTP session |
| `permitAll()` | Allows access without authentication |
| `authenticated()` | Requires an authenticated user |
| 401 | Missing or invalid authentication |
| 403 | Insufficient permission |

---

## 11. Interview Questions and Answers

**Q1. What is Spring Security?**

Spring Security is a framework that provides authentication, authorization, and protection against common security attacks in Spring applications.

**Q2. What is the difference between authentication and authorization?**

Authentication verifies the user's identity, whereas authorization checks whether the authenticated user has permission to access a resource.

**Q3. What is a SecurityFilterChain?**

It defines the security filters that Spring Security applies to incoming HTTP requests.

**Q4. What is HttpSecurity?**

`HttpSecurity` is an object used to configure web security, including authentication mechanisms, session management, CSRF protection, and authorization rules.

**Q5. Why did we disable CSRF?**

Our current application is a stateless REST API using bearer-token authentication. CSRF protection is commonly disabled in this setup, but cookie-based authentication requires us to reconsider it.

**Q6. Why did we disable form login?**

Because we are developing a REST API and will implement authentication using API endpoints instead of Spring Security's default HTML login page.

**Q7. Why did we disable HTTP Basic?**

Because we plan to use JWT bearer-token authentication instead of sending Basic authentication credentials with every request.

**Q8. What does SessionCreationPolicy.STATELESS mean?**

It means Spring Security will not create or use an HTTP session to obtain the security context. Each request must carry its own authentication information.

**Q9. What is the difference between `permitAll()` and `authenticated()`?**

`permitAll()` allows unauthenticated access, whereas `authenticated()` requires an authenticated principal.

**Q10. What is the difference between 401 and 403?**

401 indicates missing or invalid authentication. 403 indicates that the authenticated user does not have sufficient permission.

**Q11. Does Spring Security execute before the controller?**

Yes. Spring Security's servlet filters process the request before it reaches the DispatcherServlet and controller.

**Q12. Why do we use `@EnableWebSecurity`?**

It enables Spring Security's web security support and allows us to configure the application's web security.

---

## 12. Phase 3.1 Implementation Summary

Files added:

```text
src/main/java/
└── com/example/taskmanagement/
    └── config/
        └── SecurityConfig.java

src/test/java/
└── com/example/taskmanagement/
    └── config/
        └── SecurityConfigTest.java
```

Dependencies added:

- `spring-boot-starter-security`
- `spring-security-test`

Configuration implemented:

- CSRF disabled for the current stateless bearer-token API foundation.
- Form login disabled.
- HTTP Basic disabled.
- Session management set to STATELESS.
- `GET /api/v1/test` made public.
- All remaining requests require authentication.

**Phase 3.1 is complete.** We have established the security foundation. User registration, password hashing, JWT authentication, refresh tokens, and OAuth2 will be implemented in subsequent milestones.

## Phase 3.2 — User registration and password hashing

### Authentication vs authorization

Authentication verifies a user's identity, usually from credentials. Authorization determines what that verified user may access. User registration creates a credential record; it does not sign the user in. This milestone adds registration only and does not add a login flow.

### Password hashing, BCrypt, and PasswordEncoder

Encryption is designed to be reversible with a key, while password hashing is one-way. Passwords should be hashed rather than encrypted so the original password cannot be recovered from a database leak. The `User` entity stores only the BCrypt output in its non-null `password_hash` column; the original request password is never persisted.

`PasswordEncoder` is Spring Security's abstraction for encoding and verifying passwords. `PasswordConfig` provides a `BCryptPasswordEncoder`, which uses a per-password random salt and a deliberately expensive hash operation. At login time a future authentication flow can call `matches(rawPassword, storedHash)` without decrypting the hash. Registration requires 8–72 characters and also validates BCrypt's 72-byte UTF-8 input limit so multi-byte characters cannot exceed the encoder's limit.

### Registration DTOs and Bean Validation

`RegistrationRequest` is the input DTO and validates a nonblank name, a valid email address, and a password between 8 and 72 characters. `RegistrationResponse` contains only the generated ID, name, email, and creation time. The API never serializes the `User` entity and never returns either a raw password or password hash.

Bean Validation runs on the controller's `@Valid` request before service logic. Invalid fields produce a consistent `400 Bad Request` error response with field-level messages. Duplicate email produces `409 Conflict`; unexpected registration failures return a generic `500` message without internal exception details.

### Registration request flow

`POST /api/v1/auth/register` is public. The `AuthController` accepts and validates the JSON request, `AuthService` checks for an existing email, encodes the password using `PasswordEncoder`, creates the entity, and persists it through `UserRepository`. The service returns a safe response DTO, and the controller responds with `201 Created`. The registration API does not accept a role, and no role field is added until role-based authorization is required.

### Database uniqueness and duplicate registration

The service's email lookup provides a clear early duplicate check, but two concurrent requests can both pass it. The named database unique constraint `uk_users_email` is the final protection. The service flushes the insert inside its transaction and converts a violation of that specific constraint into the same duplicate-email response; unrelated database integrity failures are not mislabeled as duplicate emails.

## Phase 3.3 — JWT-Based Authentication

### Authentication flow

This phase adds email/password login and bearer-token authentication. Password verification remains Spring Security's responsibility, with the existing BCrypt `PasswordEncoder`.

```text
POST /api/v1/auth/register
Controller -> AuthService -> PasswordEncoder -> UserRepository -> H2

POST /api/v1/auth/login
Controller -> AuthService -> AuthenticationManager
         -> DaoAuthenticationProvider -> UserDetailsService
         -> BCrypt PasswordEncoder -> JwtService -> access token

Protected request
Authorization: Bearer <access-token>
JwtAuthenticationFilter -> JwtService -> UserDetailsService
                         -> SecurityContextHolder -> Controller
```

Registration still uses `201 Created`. Login uses `200 OK` and returns a 900-second token when the default 15-minute lifetime is configured.

### JWT structure

A JWT has three dot-separated parts:

```text
header.payload.signature
```

- **Header:** identifies the signing algorithm, such as `HS256`.
- **Payload:** contains claims, including the subject (`sub`) and expiration (`exp`).
- **Signature:** proves that the token was created with the configured signing key and has not been modified.

The project uses the email address as the subject. It adds an issuer and standard issued-at/expiration claims. It does not include passwords, password hashes, names, or other unnecessary personal data.

### Signing and verification

JWT generation and parsing are isolated in `JwtService`. It uses JJWT 0.12.6 and HMAC SHA-256 (`HS256`). JJWT verifies the signature and issuer, validates expiration, and requires a subject and expiration in this project.

The signing key is symmetric: the application uses the same secret to sign and verify tokens. The secret is read from `app.jwt.secret`, supplied by the environment variable `JWT_SECRET`. It must be Base64-encoded and decode to at least 32 bytes. Missing, malformed, or weak secrets fail application startup.

Generate a local development key only for local or test use:

```powershell
$bytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToBase64String($bytes)
$env:JWT_SECRET = "<generated-base64-key>"
mvn spring-boot:run
```

Do not commit the environment value or log the signing secret or complete tokens. Tests provide a test-specific key through `src/test/resources/application.properties`.

### Access tokens and expiration

An access token is an authorization credential for a short-lived request session. This project configures `app.jwt.access-token-expiration=PT15M`, and the login response returns `expiresIn: 900`. The value is calculated from the actual configured duration rather than duplicated in the controller.

There is no refresh token in this milestone.

### Bearer authentication

Protected requests send:

```http
Authorization: Bearer <access-token>
```

`JwtAuthenticationFilter` is a `OncePerRequestFilter` registered before `UsernamePasswordAuthenticationFilter`. It skips requests that already have a non-anonymous authenticated context, reads the Bearer header, parses and validates the token, loads the current user details, creates a `UsernamePasswordAuthenticationToken`, and stores it in `SecurityContextHolder`.

Invalid, malformed, expired, wrongly signed, or unsupported-algorithm tokens do not establish authentication. If the user no longer exists, loading user details fails and the request is rejected. Missing tokens are handled by Spring Security's REST authentication entry point.

### Public and protected endpoints

The security filter chain keeps these endpoints public:

- `GET /api/v1/test`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`

Every other endpoint requires authentication. CSRF remains disabled because this API uses bearer tokens and is stateless. Form login and HTTP Basic remain disabled.

### UserDetails and authentication infrastructure

`TaskManagerUserDetailsService` implements `UserDetailsService`. It loads a `User` by email and adapts it into Spring Security's `UserDetails` representation. The original `User` entity remains independent of Spring Security interfaces.

`AuthenticationManager` coordinates authentication. `DaoAuthenticationProvider` uses the `UserDetailsService` and the existing BCrypt `PasswordEncoder`. Login creates a `UsernamePasswordAuthenticationToken` containing the submitted email and password and passes it to the authentication manager; the controller never compares passwords directly.

### Security context and sessions

`SecurityContextHolder` holds the authenticated principal for the duration of the request. This is different from a server-side HTTP session. With `SessionCreationPolicy.STATELESS`, Spring Security does not create or rely on a login session for bearer-token authentication.

### Error behavior

Login with an unknown email or incorrect password returns the same safe `401 Unauthorized` response. Bean Validation failures return `400 Bad Request` field details. Protected requests without a valid bearer token return a safe `401` JSON response. Authenticated-but-disallowed requests would use the REST `403 Forbidden` handler. Internally, user lookup, JWT parsing, and authentication failures do not expose stack traces or complete tokens.

### JWT security limitations

JWT access tokens are bearer credentials: anyone holding a valid token can present it until expiration. Tokens should therefore be short-lived and sent only over HTTPS. Logout and revocation require additional state or infrastructure and are not implemented here. JWT payloads are signed but not encrypted, so they must not contain secrets. A compromised signing secret can allow forged tokens. Refresh tokens, rotating keys, and OAuth2/OIDC are outside this milestone.

### Interview questions and answers

**Q1. What is the difference between authentication and authorization?**

Authentication determines who the caller is. Authorization determines whether that identity can access a resource.

**Q2. What are the three JWT parts?**

Header, payload, and signature. The header identifies signing metadata, the payload carries claims, and the signature detects unauthorized modification.

**Q3. Why use the email as the subject?**

It is a stable application identity already used to load and authenticate the registered user. The database remains the final source for whether that user still exists.

**Q4. Why verify `exp` and the signature?**

Signature verification proves integrity and trust; expiration verification prevents a validly issued token from being accepted after its lifetime.

**Q5. What is `HS256`?**

It is HMAC with SHA-256. It uses a shared symmetric signing secret for signing and verification.

**Q6. How does `OncePerRequestFilter` differ from a normal servlet filter?**

It guarantees one filter execution per request dispatch, avoiding duplicate bearer-processing logic across forwarded or error dispatches.

**Q7. What is `SecurityContextHolder`?**

It stores the `SecurityContext` for the current execution context, allowing Spring Security and application code to access the authenticated principal.

**Q8. Why does JWT authentication need no HTTP session?**

The token carries signed identity and expiration claims. Each request presents the token and the server validates it rather than looking up server-side session state.

**Q9. What is the difference between 401 and 403?**

`401` means the request lacks valid authentication. `403` means authentication succeeded but the authenticated principal lacks permission.

**Q10. What happens if a validly issued token refers to a deleted user?**

The filter loads the subject from the database through `UserDetailsService`. If no user exists, authentication is rejected.

------------------------------------------------

# Phase 3.2 – User Registration and Password Encryption

## 1. Introduction to Authentication

Authentication is the process of verifying the identity of a user.

In our Task Management REST API, users must register before they can access protected application features.

We implemented user registration using:

- Spring Boot REST API.
- Spring Security.
- BCrypt password hashing.
- Spring Data JPA.
- DTOs.
- Bean Validation.
- Global exception handling.

The registration flow:

```text
Client
  |
  | POST /api/v1/auth/register
  v
AuthController
  |
  v
AuthService
  |
  |-- Check whether email already exists
  |
  |-- Hash password using BCrypt
  |
  v
UserRepository
  |
  v
Database
  |
  |-- Save user details
  |-- Store password hash
  v
Registration Response DTO
  |
  v
Client
```

The important security principle is:

**Never store a user's original password in the database.**

---

## 2. Authentication vs Authorization

These two concepts are related but different.

### Authentication

Authentication verifies the identity of a user.

Example:

A user enters their registered email and password to log in.

### Authorization

Authorization determines what an authenticated user is allowed to access.

Example:

A user can access their own tasks but cannot access another user's private tasks.

| Authentication | Authorization |
|---|---|
| Verifies identity | Checks permissions |
| Who are you? | What can you access? |
| Login | Access control |
| Credentials | Roles and authorities |

Our registration implementation prepares the foundation for authentication. Authorization rules for individual user resources will be developed later.

---

## 3. Password Hashing vs Encryption

This is one of the most important concepts in backend security.

### Encryption

Encryption transforms readable information into an unreadable format using a key.

The original data can be recovered through decryption using the appropriate key.

Example:

```text
Original: Hello123
    |
    v
Encryption
    |
    v
Encrypted data
    |
    v
Decryption
    |
    v
Hello123
```

Encryption is useful when data needs to be recovered later.

### Hashing

Hashing transforms input data into a fixed-format digest.

It is designed to be one-way: the original password is not recovered by decrypting the hash.

Example:

```text
Password: StrongPassword123
        |
        v
     BCrypt
        |
        v
Stored password hash
```

During login, the application checks whether the supplied password matches the stored hash.

**Why use hashing for passwords?**

If a database is compromised, attackers should not immediately obtain users' original passwords.

---

## 4. What is BCrypt?

BCrypt is a password-hashing algorithm designed to make password guessing computationally expensive.

It provides:

- A salt for each password hash.
- A configurable computational cost.
- Resistance to efficient large-scale password guessing.

### What is a Salt?

A salt is random data added during password hashing.

Even if two users choose the same password, BCrypt normally generates different hashes because each password receives a different salt.

Example:

```text
User A password: hello123
User B password: hello123

User A hash: $2a$...different...
User B hash: $2a$...different...
```

The hashes differ even though the original passwords are identical.

### Why is BCrypt useful?

BCrypt makes password verification deliberately expensive, making brute-force password guessing more difficult.

It is preferable to fast general-purpose hashes such as plain SHA-256 for password storage.

---

## 5. PasswordEncoder in Spring Security

Spring Security provides the `PasswordEncoder` interface for password encoding and verification.

Example:

```java
passwordEncoder.encode(rawPassword);
```

This generates a password hash.

To verify a password:

```java
passwordEncoder.matches(rawPassword, storedHash);
```

### Important difference

| Method | Purpose |
|---|---|
| `encode()` | Generates a password hash |
| `matches()` | Checks whether a raw password matches a stored hash |

Example:

```java
String rawPassword = "StrongPassword123";

String hashedPassword =
        passwordEncoder.encode(rawPassword);

boolean result =
        passwordEncoder.matches(
            rawPassword,
            hashedPassword
        );
```

The result is `true`.

We do not decrypt the hash or compare the raw password directly with the stored hash.

---

## 6. BCrypt's 72-Byte Password Limit

BCrypt processes a maximum of 72 bytes of password input.

This is a byte limit, not necessarily a character limit.

For ordinary ASCII characters, one character generally uses one byte in UTF-8.

However, multibyte UTF-8 characters can use more than one byte.

For example:

```text
ASCII character: A
UTF-8 size: 1 byte

Some Unicode characters:
UTF-8 size: 2, 3 or 4 bytes
```

Therefore, checking only the Java string's character length is insufficient.

Our Phase 3.2 implementation added validation for the BCrypt 72-byte boundary, including multibyte UTF-8 passwords.

This prevents passwords that exceed BCrypt's effective input limit from being silently treated as equivalent after truncation.

---

## 7. Updating the User Entity

Previously, our User entity contained basic user information.

For registration, we added a password hash field.

Conceptually, the entity now contains:

```text
User
 |
 |-- id
 |-- name
 |-- email
 |-- passwordHash
 |-- createdAt
 |
 v
Database table: users
```

### Important database design decisions

1. The password hash is stored, not the original password.
2. The password hash column is non-null.
3. Email has a database uniqueness constraint.
4. The named email constraint makes duplicate-email violations easier to identify.
5. The existing two-argument constructor was preserved for compatibility with existing code.

The database uniqueness constraint is important even when the application checks for an existing email.

Why?

Because two registration requests can arrive simultaneously.

Both may check the database before either one has inserted the new user.

The database must remain the final authority for uniqueness.

---

## 8. What is a DTO?

DTO stands for **Data Transfer Object**.

A DTO is an object used to transfer data between different application layers or between the client and server.

We use DTOs to control which information enters and leaves our API.

### Why not return the User entity directly?

Our User entity contains sensitive information such as the password hash.

If we return the entity directly, we risk exposing internal database fields.

DTOs prevent this problem.

### Registration Request DTO

The client sends:

```json
{
  "name": "Rahul",
  "email": "rahul@example.com",
  "password": "StrongPassword123"
}
```

The request DTO receives this information.

### Registration Response DTO

The API returns safe information:

```json
{
  "id": 1,
  "name": "Rahul",
  "email": "rahul@example.com"
}
```

Notice that neither the raw password nor the password hash is returned.

### DTO flow

```text
Client Request JSON
        |
        v
Registration Request DTO
        |
        v
AuthController
        |
        v
AuthService
        |
        v
User Entity
        |
        v
Database
        |
        v
Saved User Entity
        |
        v
Registration Response DTO
        |
        v
Client Response JSON
```

---

## 9. Bean Validation

Bean Validation is used to validate incoming data before processing it.

We use validation annotations on DTO fields.

Common annotations:

| Annotation | Purpose |
|---|---|
| `@NotNull` | Value must not be null |
| `@NotBlank` | String must not be null, empty or whitespace-only |
| `@NotEmpty` | Value must not be null or empty |
| `@Email` | String should have a valid email format |
| `@Size` | Validates minimum and maximum length |
| `@Min` | Minimum numeric value |
| `@Max` | Maximum numeric value |
| `@Pattern` | Validates a regular-expression pattern |

Example:

```java
public class RegisterRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 72)
    private String password;
}
```

The actual project validation must also account for BCrypt's byte limit rather than relying only on `@Size`.

### What is `@Valid`?

When used on a controller request parameter, `@Valid` tells Spring to validate the incoming object against its validation constraints.

Conceptually:

```java
public ResponseEntity<?> register(
        @Valid @RequestBody RegisterRequest request) {
    // Registration logic
}
```

If validation fails, the request is rejected before the normal registration service flow proceeds.

---

## 10. Understanding AuthController

The controller handles incoming HTTP requests.

Our registration endpoint:

```http
POST /api/v1/auth/register
```

Its responsibilities include:

- Receiving registration JSON.
- Binding JSON to the request DTO.
- Triggering validation.
- Calling the authentication service.
- Returning the appropriate HTTP response.

The controller should not contain password hashing or database persistence logic.

Those responsibilities belong in the service layer.

---

## 11. Understanding AuthService

The service contains the business logic for registration.

The registration operation follows this sequence:

1. Receive validated registration data.
2. Check whether the email already exists.
3. Encode the raw password using BCrypt.
4. Create a User entity.
5. Store the encoded password hash in the entity.
6. Save the user through UserRepository.
7. Convert the saved entity into a safe response DTO.
8. Return the response.

### Why use a service layer?

It separates business logic from HTTP request handling.

This makes the application easier to:

- Test.
- Maintain.
- Extend.
- Reuse.
- Refactor.

### Why use constructor injection?

Constructor injection makes dependencies explicit and allows tests to supply mocked dependencies.

It also ensures that required dependencies are available when the service is created.

---

## 12. Database Unique Constraint and Race Conditions

We implemented both an application-level duplicate-email check and a database-level unique constraint.

### Application-level check

The service checks whether an email already exists before attempting insertion.

This provides a clean response for normal duplicate registration requests.

### Database-level protection

The database enforces the unique constraint.

Consider this situation:

```text
Request A                 Request B
    |                         |
    v                         v
Check email               Check email
    |                         |
    v                         v
Email available           Email available
    |                         |
    v                         v
Insert user               Insert user
    |                         |
    v                         v
Success                   Unique constraint violation
```

Both requests can pass the initial check.

Only one insertion should succeed.

The database prevents duplicate records.

### Why handle database exceptions?

The application should translate a database uniqueness violation into a safe application-level response instead of exposing SQL or database details.

Our Phase 3.2 implementation includes named email uniqueness and duplicate-email race-condition handling.

---

## 13. Transaction Management

Registration involves writing user data to the database.

The operation is transactional.

A transaction ensures that database changes follow defined commit and rollback behavior.

Common Spring annotation:

```java
@Transactional
```

If an operation fails with an exception that triggers rollback, the transaction can be rolled back rather than leaving partially completed database work.

For database uniqueness violations, the service or exception-handling layer should translate the failure into an appropriate safe response.

---

## 14. Global Exception Handling

A REST API should return consistent error responses.

Examples of registration errors:

| Situation | HTTP Status |
|---|---|
| Invalid email format | 400 Bad Request |
| Blank name | 400 Bad Request |
| Invalid password length | 400 Bad Request |
| Duplicate email | 409 Conflict |
| Successful registration | 201 Created |

A global exception handler can translate application exceptions and validation failures into consistent JSON responses.

Example:

```json
{
  "status": 409,
  "message": "Email already registered"
}
```

The exact error response structure should follow the project's implemented error DTO.

Never expose:

- SQL statements.
- Database stack traces.
- Password hashes.
- Internal implementation details.

---

## 15. Why We Did Not Add a Role Yet

Roles are commonly used for authorization.

Examples:

```text
ROLE_USER
ROLE_ADMIN
```

A user role might allow normal application operations, while an admin role could allow administrative operations.

However, our current registration milestone does not require role-based access control.

We therefore did not introduce an unnecessary role field or role enum.

When roles become necessary, public registration must never allow users to assign themselves administrative privileges.

---

## 16. Testing Registration

We added automated tests to verify registration behavior.

The important test cases include:

1. Successful user registration.
2. Password is stored as a hash.
3. BCrypt `matches()` verifies the original password against the stored hash.
4. Duplicate email registration is rejected.
5. Invalid email is rejected.
6. Blank name is rejected.
7. Invalid password length is rejected.
8. Response does not expose the raw password or password hash.
9. Registration endpoint is publicly accessible.
10. Duplicate-email race handling.
11. BCrypt UTF-8 byte-boundary validation.

### Why test password hashing?

A successful registration response does not prove that the password was stored securely.

We must verify that:

```text
Stored password hash != Original password
```

And that:

```java
passwordEncoder.matches(
    originalPassword,
    storedPasswordHash
)
```

returns `true`.

---

## 17. Phase 3.2 Test Results

After implementing registration and password hashing, we executed:

```bash
mvn clean test
```

Test environment:

- Java 21
- Spring Boot 3.5.7
- Maven
- H2 database

Final reported result:

| Test result | Count |
|---|---:|
| Total tests | 32 |
| Failures | 0 |
| Errors | 0 |

All previous Phase 1, Phase 2 and Phase 3.1 tests also passed.

No additional Maven dependencies were required for Phase 3.2.

---

## 18. Complete Registration Flow – Interview Explanation

If an interviewer asks:

**"Explain how you implemented user registration in your Spring Boot project."**

You can answer:

"I implemented registration using a layered architecture. The client sends name, email and password to the registration REST endpoint. The controller receives the request through a DTO and applies Bean Validation. The service checks whether the email already exists, encodes the password using Spring Security's BCrypt PasswordEncoder, creates the User entity and saves it through the UserRepository. Finally, the saved entity is converted into a response DTO so that sensitive fields such as the password hash are never exposed. I also implemented database-level email uniqueness and exception handling for duplicate registration requests."

---

## 19. Interview Questions and Answers

**Q1. What is password hashing?**

Password hashing converts a password into a one-way digest. The application verifies passwords by comparing the supplied password against the stored hash.

**Q2. Why should passwords never be stored in plain text?**

A database leak could expose every user's original password. Password hashing reduces the risk of direct password disclosure.

**Q3. What is BCrypt?**

BCrypt is a password-hashing algorithm that uses a salt and configurable computational cost to make password guessing more expensive.

**Q4. What is a salt?**

A salt is random data incorporated into password hashing so that identical passwords can produce different stored hashes.

**Q5. What is PasswordEncoder?**

It is a Spring Security interface used to encode passwords and verify raw passwords against stored password hashes.

**Q6. What is the difference between `encode()` and `matches()`?**

`encode()` creates a password hash, while `matches()` verifies whether a raw password matches an existing hash.

**Q7. Why do we use DTOs?**

DTOs control the data transferred through API boundaries and prevent sensitive entity fields from being exposed.

**Q8. Why not return the User entity directly?**

The User entity contains internal database fields, including the password hash. Returning it directly could expose sensitive information.

**Q9. What is Bean Validation?**

Bean Validation checks incoming data against constraints such as `@NotBlank`, `@Email` and `@Size`.

**Q10. What is the purpose of `@Valid`?**

It triggers validation of a request object against its configured validation constraints.

**Q11. Why do we check duplicate emails in both the service and database?**

The service provides a user-friendly duplicate check, while the database unique constraint protects against concurrent registration requests.

**Q12. What is a race condition in registration?**

It occurs when concurrent requests check the same email before either request inserts it. Both may pass the application check, so the database must enforce uniqueness.

**Q13. Why do we use transactions in registration?**

Transactions provide controlled commit and rollback behavior for database operations.

**Q14. What is the difference between encryption and hashing?**

Encryption is reversible with the appropriate key, while password hashing is designed to be one-way.

**Q15. Why is BCrypt's 72-byte limit important?**

BCrypt has a 72-byte input limit. UTF-8 characters may use multiple bytes, so character count alone cannot reliably enforce this boundary.

---

## 20. Phase 3.2 Implementation Summary

Implemented functionality:

```text
POST /api/v1/auth/register
```

Registration request:

```json
{
  "name": "Rahul",
  "email": "rahul@example.com",
  "password": "StrongPassword123"
}
```

Registration flow:

```text
Client
  |
  v
AuthController
  |
  v
Registration DTO + Bean Validation
  |
  v
AuthService
  |
  |-- Check duplicate email
  |-- BCrypt password hashing
  |-- Create User entity
  |
  v
UserRepository
  |
  v
H2 Database
  |
  v
Safe Registration Response DTO
```

Key implementation decisions:

- BCrypt password hashing.
- No plain-text password storage.
- No password hash in API responses.
- Request and response DTOs.
- Bean Validation.
- Constructor injection.
- Transactional registration.
- Database email uniqueness.
- Duplicate-email race handling.
- UTF-8 BCrypt byte-boundary validation.
- No role field added.
- No JWT or login implementation in this milestone.

**Phase 3.2 is complete.** Registration and password storage are ready to be used by the next milestone: JWT-based login and authentication.



---------------------------------------------------------------

# Phase 3.3 – JWT-Based Authentication

## 1. Introduction to JWT

JWT stands for **JSON Web Token**.

It is a compact, URL-safe token format used to securely transfer information between parties.

In our Task Management REST API, JWT is used to authenticate users without maintaining traditional server-side login sessions.

After a user successfully logs in, the backend generates a signed JWT access token and returns it to the client.

The client sends this token with subsequent protected API requests.

Example:

```http
Authorization: Bearer <access-token>
```

Spring Security validates the token and authenticates the user before allowing access to protected endpoints.

### Why JWT?

Traditional session-based authentication generally stores session state on the server.

JWT-based authentication allows the server to validate a signed token on each request without relying on a server-side HTTP login session.

Advantages:

- Stateless authentication.
- Suitable for REST APIs.
- Easy integration with frontend applications.
- Can carry identity and other claims.
- Supports distributed backend architectures.

Important: JWT does not automatically make an application secure. Token validation, expiration, signing-key protection and authorization must be implemented correctly.

---

## 2. JWT Authentication Flow

Our implemented authentication flow:

```text
              CLIENT
                |
                |
         POST /auth/login
         Email + Password
                |
                v
          AuthController
                |
                v
           AuthService
                |
                v
        AuthenticationManager
                |
                v
        AuthenticationProvider
                |
                v
          UserDetailsService
                |
                v
           UserRepository
                |
                v
           H2 Database
                |
                v
       BCrypt Password Verification
                |
          Credentials Valid?
             /       \
           No         Yes
           |           |
           v           v
       401 Error    JwtService
                       |
                       v
                Generate JWT
                       |
                       v
                Login Response
                       |
                       v
                     CLIENT
                       |
                       |
              Stores Access Token
                       |
                       |
          GET /api/v1/protected-endpoint
                       |
                       v
              Bearer JWT Header
                       |
                       v
           JwtAuthenticationFilter
                       |
                       v
               Validate JWT
                       |
                       v
              SecurityContext
                       |
                       v
             Authorization Check
                       |
                       v
                  Controller
```

---

## 3. Structure of a JWT

A JWT consists of three parts:

```text
HEADER.PAYLOAD.SIGNATURE
```

Example structure:

```text
xxxxx.yyyyy.zzzzz
```

### 3.1 Header

The header contains metadata about the token.

Example:

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

- `alg`: Signing algorithm.
- `typ`: Token type.

Our implementation uses HS256.

### 3.2 Payload

The payload contains claims, which are statements about the token's subject and other metadata.

Example:

```json
{
  "sub": "user@example.com",
  "iss": "task-management-api",
  "iat": 1795348800,
  "exp": 1795349700
}
```

Common JWT claims:

| Claim | Meaning |
|---|---|
| `sub` | Subject, usually the user identity |
| `iss` | Issuer of the token |
| `iat` | Issued-at timestamp |
| `exp` | Expiration timestamp |
| `aud` | Intended audience |
| `jti` | Unique token identifier |

Our implementation uses the user's email as the JWT subject and includes relevant token metadata.

### 3.3 Signature

The signature ensures that the token has not been modified and was signed using the expected signing key.

For HS256, the conceptual signing operation is:

```text
HMAC-SHA256(
    Base64Url(Header) + "." + Base64Url(Payload),
    SecretKey
)
```

If someone modifies the payload, the signature verification fails.

**Important:** A normal signed JWT is not encrypted. Anyone possessing the token can decode its header and payload. Never store passwords, password hashes or other sensitive secrets inside JWT claims.

---

## 4. What is HS256?

HS256 means:

**HMAC using SHA-256**

It is a symmetric signing algorithm.

Symmetric signing means the same secret key is used for signing and verifying the token.

```text
              SECRET KEY
                  |
          -----------------
          |               |
          v               v
       Signing         Verification
          |               |
          v               v
       Generate       Validate JWT
       JWT
```

Advantages:

- Efficient signing and verification.
- Straightforward configuration.
- Suitable for applications where trusted services share a signing secret.

Security requirements:

- Use a sufficiently strong random secret.
- Store the production secret outside source code.
- Never expose the secret to the frontend.
- Never log the secret.
- Do not commit production secrets to GitHub.

Our implementation reads JWT configuration from environment-backed application properties.

---

## 5. JWT Configuration

We configured JWT properties such as:

- Signing secret.
- Access-token expiration.
- Issuer.

### Why externalize configuration?

Hardcoding secrets in Java source code is dangerous.

If the code is pushed to a public repository, the secret could be exposed.

Instead, use environment variables and application configuration.

Conceptual example:

```properties
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration=${JWT_EXPIRATION:900000}
app.jwt.issuer=task-management-api
```

The exact property names and expiration units must match the actual project configuration.

For a production environment:

- Supply a strong random signing secret.
- Keep it in a secure environment or secret manager.
- Rotate it according to the application's security requirements.
- Do not commit real secrets to version control.

The application should fail fast when required JWT configuration is missing or invalid.

---

## 6. JWT Expiration

Every access token should have an expiration time.

The `exp` claim identifies when the token expires.

Example:

```json
{
  "sub": "user@example.com",
  "exp": 1795349700
}
```

Suppose an access token is configured to expire after 15 minutes.

After that time, the token should no longer be accepted.

Why expiration matters:

- Limits the useful lifetime of stolen tokens.
- Reduces the impact of token leakage.
- Encourages clients to obtain fresh credentials when necessary.

Our implementation returns the token's expiration duration through the login response's `expiresIn` field.

---

## 7. What is a Bearer Token?

A bearer token is an access token that grants access to protected resources to whoever possesses it.

The client sends it through the HTTP Authorization header.

Example:

```http
GET /api/v1/tasks
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

The word `Bearer` identifies the authentication scheme.

The backend extracts the token and validates it.

Important: A bearer token must be protected because possession of a valid token can allow someone to act as its associated user.

Use HTTPS and avoid exposing tokens in URLs, logs or error messages.

---

## 8. AuthenticationManager

`AuthenticationManager` is a Spring Security interface responsible for processing authentication requests.

It delegates authentication to an appropriate AuthenticationProvider.

Conceptually:

```text
AuthService
    |
    v
AuthenticationManager
    |
    v
AuthenticationProvider
    |
    v
Credential Verification
    |
    v
Authentication Result
```

In our project, the login service uses AuthenticationManager rather than manually comparing passwords.

This keeps authentication responsibilities inside Spring Security.

---

## 9. AuthenticationProvider

An `AuthenticationProvider` performs a specific type of authentication.

Our implementation uses `DaoAuthenticationProvider`.

It works with:

- `UserDetailsService`
- `PasswordEncoder`

Simplified flow:

1. Receive authentication credentials.
2. Load user details.
3. Obtain the stored password hash.
4. Verify the submitted password using PasswordEncoder.
5. Return an authenticated result if credentials are valid.
6. Reject invalid credentials.

This reuses the BCrypt password hashing foundation from Phase 3.2.

---

## 10. UserDetails and UserDetailsService

### UserDetails

`UserDetails` is a Spring Security interface representing user information required during authentication and authorization.

It provides information such as:

- Username.
- Password hash.
- Authorities.
- Account expiration.
- Account locking.
- Credential expiration.
- Account enabled status.

We use a separate Spring Security user representation rather than tightly coupling our JPA User entity to Spring Security.

### UserDetailsService

`UserDetailsService` loads a user's security details using a username.

In our application, the email address is used as the username.

Simplified implementation:

```java
@Override
public UserDetails loadUserByUsername(String email) {
    // Find user by email.
    // Convert user into Spring Security UserDetails.
}
```

Spring Security uses this service during credential authentication.

---

## 11. Authentication Object and UsernamePasswordAuthenticationToken

Spring Security uses Authentication objects to represent authentication information.

`UsernamePasswordAuthenticationToken` is an implementation of the Authentication interface.

It can represent:

- An authentication request containing username and password.
- An authenticated principal after successful verification.

Conceptually:

```text
Before Authentication
        |
        v
Username + Password
        |
        v
AuthenticationManager
        |
        v
Credential Verification
        |
        v
Authenticated Result
```

For JWT authentication, the filter creates an authenticated Authentication object after successful token validation.

---

## 12. What is OncePerRequestFilter?

`OncePerRequestFilter` is a Spring web filter base class designed to execute its filtering logic once per request within a request dispatch.

We created a custom:

```text
JwtAuthenticationFilter
```

Its job is to inspect incoming requests for bearer tokens.

### JWT filter flow

```text
Incoming HTTP Request
        |
        v
Read Authorization Header
        |
        v
Does it contain Bearer token?
        |
        v
Extract JWT
        |
        v
Validate Signature and Expiration
        |
        v
Extract Subject
        |
        v
Load User Details
        |
        v
Create Authentication Object
        |
        v
Set SecurityContext
        |
        v
Continue Filter Chain
```

If the token is malformed, expired or invalid, it must not establish an authenticated identity.

A missing bearer token is not itself a parsing error. The request continues through the security chain and protected endpoints are rejected by authorization rules if authentication is required.

---

## 13. SecurityContext and SecurityContextHolder

### SecurityContext

`SecurityContext` stores security-related information for the current execution context, especially the currently authenticated principal.

### SecurityContextHolder

`SecurityContextHolder` provides access to the current SecurityContext.

Conceptually:

```java
SecurityContextHolder.getContext()
        .setAuthentication(authentication);
```

Our JWT filter sets the authenticated user in the SecurityContext after successfully validating the JWT and loading the user details.

Later security components can access this authentication information.

### Why is this important?

Spring Security does not need every controller to manually validate JWT tokens.

The filter establishes the authenticated identity, and the rest of the security infrastructure can use it.

---

## 14. Spring Security Filter Chain Ordering

The JWT filter must execute at an appropriate position in the security filter chain.

Our implementation adds it before:

```java
UsernamePasswordAuthenticationFilter.class
```

Conceptually:

```text
HTTP Request
     |
     v
Spring Security Filters
     |
     v
JwtAuthenticationFilter
     |
     v
UsernamePasswordAuthenticationFilter Position
     |
     v
Authorization Filters
     |
     v
DispatcherServlet
     |
     v
Controller
```

The correct filter position matters because the JWT must be processed before Spring Security makes the authorization decision for protected requests.

We also ensure the filter is not accidentally registered twice as both a servlet filter and a security-chain filter.

---

## 15. Stateless Authentication

Our security configuration uses:

```java
SessionCreationPolicy.STATELESS
```

This means Spring Security does not create or use an HTTP session to obtain the security context.

Every protected request carries its own authentication information.

Example:

```http
GET /api/v1/tasks
Authorization: Bearer <access-token>
```

The server validates the token on each request.

### Session vs JWT

| Session-based authentication | JWT-based authentication |
|---|---|
| Server maintains session state | Token carries signed claims |
| Usually uses session ID cookie | Usually uses Authorization header |
| Server-side session lookup | Token signature verification |
| Session expiration managed server-side | Token expiration claim |
| Requires session infrastructure | Does not require login session state |

JWT authentication is not completely stateless in every possible implementation. For example, applications may maintain revocation lists or other token-related state.

Our current access-token design does not implement such a revocation store.

---

## 16. JwtService

We implemented a dedicated JWT service to keep token-related operations separate from controllers and business logic.

Its responsibilities include:

- Generating signed access tokens.
- Extracting the token subject.
- Reading expiration information.
- Validating token signatures.
- Validating expiration.
- Rejecting malformed tokens.
- Rejecting tokens signed with unexpected secrets or algorithms.

Why use a separate service?

It follows the Single Responsibility Principle and allows token generation and validation to be tested independently.

The controller should not contain JWT signing or parsing logic.

---

## 17. Login DTOs

We introduced DTOs for login.

### Login Request

Example:

```json
{
  "email": "user@example.com",
  "password": "StrongPassword123"
}
```

The request DTO validates that:

- Email is present and valid.
- Password is not blank.

### Login Response

Example:

```json
{
  "accessToken": "<signed-jwt>",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

The response contains:

- Access token.
- Token type.
- Expiration duration in seconds.

It does not contain the user's password or password hash.

---

## 18. REST Authentication and Authorization Error Handling

A REST API should provide consistent responses when authentication or authorization fails.

### 401 Unauthorized

Returned when authentication is missing or invalid.

Examples:

- Missing token on a protected endpoint.
- Invalid token.
- Expired token.
- Invalid login credentials.

### 403 Forbidden

Returned when the user is authenticated but is not allowed to perform an operation.

Example:

An authenticated user attempts an operation for which they lack the required permission.

Our implementation includes REST-oriented handlers for authentication and authorization failures.

| Status | Meaning |
|---|---|
| 401 | Authentication required or invalid |
| 403 | Insufficient permission |

---

## 19. Complete Security Configuration Flow

Our public endpoints:

```text
GET  /api/v1/test
POST /api/v1/auth/register
POST /api/v1/auth/login
```

All remaining endpoints require authentication.

Security configuration retains:

- Stateless session management.
- Form login disabled.
- HTTP Basic disabled.
- Existing CSRF decision for the bearer-token API.
- JWT filter registered in the security chain.
- REST authentication and authorization handling.

This means registration and login can be accessed without a JWT, while protected API operations require a valid access token.

---

## 20. Testing JWT Authentication

We added tests for login, token generation, validation and protected endpoint access.

### Login tests

1. Successful login with valid credentials.
2. Invalid password rejection.
3. Unknown email rejection.
4. Invalid email rejection.
5. Blank password rejection.
6. Token included in successful response.
7. No password or password hash in response.

### JWT tests

1. Generate a valid token.
2. Validate a valid token.
3. Reject expired tokens.
4. Reject malformed tokens.
5. Reject tokens signed with a different secret.
6. Verify the expected subject.

### Security integration tests

1. Registration remains public.
2. Login remains public.
3. Test endpoint remains public.
4. Protected endpoint rejects missing tokens.
5. Protected endpoint rejects invalid tokens.
6. Protected endpoint accepts valid bearer tokens.
7. SecurityContext contains the expected authenticated principal.

### Test command

```bash
mvn clean test
```

### Final Phase 3.3 test result

| Metric | Result |
|---|---:|
| Total tests | 44 |
| Failures | 0 |
| Errors | 0 |

All previous phase tests also passed.

---

## 21. Important Security Limitations

JWT is a signed token format, not a complete authentication system by itself.

Important considerations:

1. A signed JWT is not encrypted.
2. Tokens must be protected from theft.
3. Access tokens should have limited lifetimes.
4. Signing secrets must remain confidential.
5. The server must validate signatures and expiration.
6. The server must reject unexpected algorithms.
7. Token revocation is not automatically provided.
8. A user's account status should be considered when loading user details.
9. HTTPS should be used in production.
10. Refresh-token management is a separate security concern.

Our current implementation provides access-token authentication only.

It does not yet implement refresh tokens, token revocation, OAuth2 or social login.

---

## 22. Interview Questions and Answers

**Q1. What is JWT?**

JWT stands for JSON Web Token. It is a compact, URL-safe format used to transfer signed claims between parties.

**Q2. What are the three parts of JWT?**

Header, Payload and Signature.

**Q3. Is JWT encrypted?**

A normal signed JWT is not encrypted. Its payload can be decoded, but its signature protects integrity and authenticity.

**Q4. What is the purpose of the JWT signature?**

It allows the server to verify that the token was signed using the expected key and has not been modified.

**Q5. What is HS256?**

HS256 is an HMAC-based JWT signing algorithm using SHA-256 and a shared secret key.

**Q6. What is a JWT claim?**

A claim is a statement about the token subject or metadata, such as subject, issuer, issued-at time or expiration.

**Q7. What is the `sub` claim?**

It identifies the subject of the token. In our project, it contains the user's email.

**Q8. What is the `exp` claim?**

It specifies the expiration timestamp after which the token must not be accepted.

**Q9. What is a Bearer token?**

It is an access token sent in the HTTP Authorization header using the Bearer authentication scheme.

**Q10. What is AuthenticationManager?**

It is a Spring Security interface responsible for processing authentication requests by delegating to an AuthenticationProvider.

**Q11. What is DaoAuthenticationProvider?**

It is an AuthenticationProvider that uses UserDetailsService and PasswordEncoder to authenticate users against stored user details.

**Q12. What is UserDetailsService?**

It is a Spring Security interface used to load user-specific security details, typically using a username.

**Q13. What is UserDetails?**

It represents the user information needed by Spring Security for authentication and authorization.

**Q14. What is OncePerRequestFilter?**

It is a Spring filter base class used to implement filtering logic that executes once per request dispatch.

**Q15. Why do we create a custom JWT filter?**

To extract bearer tokens from incoming requests, validate them and establish authentication in Spring Security.

**Q16. What is SecurityContext?**

It holds security-related information, including the current authenticated principal.

**Q17. What is SecurityContextHolder?**

It provides access to the current SecurityContext.

**Q18. Why do we use SessionCreationPolicy.STATELESS?**

Because our JWT-based API does not depend on server-side HTTP sessions for authentication.

**Q19. What happens when a JWT expires?**

The token should be rejected, and the user must authenticate again or use a refresh-token mechanism if one has been implemented.

**Q20. What is the difference between 401 and 403?**

401 indicates missing or invalid authentication. 403 indicates insufficient permission for an authenticated user.

**Q21. Can JWT be modified by a client?**

A client can modify the token contents, but signature verification should detect the modification and reject the token.

**Q22. Does JWT automatically support logout?**

No. A stateless access token generally remains valid until expiration unless the server implements revocation or another invalidation mechanism.

**Q23. Why should we not store passwords inside JWT payloads?**

JWT payloads are normally readable by anyone who possesses the token. Passwords and password hashes must never be included.

**Q24. Why should JWT secrets be stored in environment variables?**

To prevent sensitive signing keys from being exposed in source code or version control.

---

## 23. Phase 3.3 Implementation Summary

Implemented components:

```text
JWT Configuration
       |
       v
JwtService
       |
       v
Token Generation and Validation

AuthController
       |
       v
AuthService
       |
       v
AuthenticationManager
       |
       v
DaoAuthenticationProvider
       |
       v
UserDetailsService
       |
       v
UserRepository

JwtAuthenticationFilter
       |
       v
SecurityContextHolder
       |
       v
Spring Security Authorization
```

Dependencies added:

- JJWT API 0.12.6
- JJWT implementation 0.12.6
- JJWT Jackson integration 0.12.6

Features completed:

- Email/password login.
- BCrypt credential verification.
- JWT access-token generation.
- HS256 signing.
- Environment-backed signing configuration.
- Token subject and expiration validation.
- Custom JWT authentication filter.
- UserDetailsService.
- AuthenticationManager and DaoAuthenticationProvider.
- SecurityContext population.
- Stateless bearer-token authentication.
- REST-oriented 401 and 403 handling.
- Automated JWT and security integration tests.

**Final verification:** 44 tests passed, with zero failures and zero errors.

Phase 3.3 is complete. The application now supports registration and JWT-based access-token authentication.

Refresh tokens, OAuth2 social login and task CRUD remain future milestones.

# Phase 3.4 – Refresh Token Authentication

## Access tokens and refresh tokens

An access token is a short-lived JWT used as a bearer credential on protected API requests. The configured access-token lifetime remains 15 minutes (`app.jwt.access-token-expiration=PT15M`). A refresh token is a longer-lived, opaque random value used only to obtain a replacement access/refresh-token pair; its default server-side lifetime is 30 days (`app.jwt.refresh-token-expiration=P30D`). The two lifetimes are configured independently.

Unlike a JWT, an opaque refresh token contains no claims or user data that a client can decode. The server recognizes it by hashing the presented value and looking up the stored hash. Refresh-token expiry and revocation are checked against the database on every refresh.

## Random token generation and storage

`RefreshTokenGenerator` uses `SecureRandom` to create 32 random bytes (256 bits) and encodes them as unpadded URL-safe Base64. It hashes the raw value with SHA-256 and stores only that 43-character hash in `refresh_tokens.token_hash`.

SHA-256 is appropriate here because the refresh token is uniformly random and has high entropy; there is no practical password-style dictionary to slow down with an intentionally expensive function. Human-created passwords have low, guessable entropy, so the project continues to use BCrypt through Spring Security's `PasswordEncoder` for passwords. A refresh token is never stored, logged, or returned again after its one-time response.

`RefreshToken` has a required lazy many-to-one relationship to `User`, a unique token-hash constraint, indexes for hash/family/user lookup, creation and expiration timestamps, nullable revocation time, and an optional replacement ID. Each login creates a new family, so one user's independent device sessions can be revoked separately.

## Login and refresh endpoints

`POST /api/v1/auth/login` authenticates email/password using `AuthenticationManager` and the existing `DaoAuthenticationProvider`, `UserDetailsService`, and BCrypt encoder. It returns:

```json
{
  "accessToken": "<signed-access-jwt>",
  "refreshToken": "<one-time-opaque-random-token>",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

The raw refresh token is returned to the client once; only its SHA-256 hash and session metadata are persisted.

`POST /api/v1/auth/refresh` accepts:

```json
{ "refreshToken": "<opaque-refresh-token>" }
```

The server finds the record by hashing the submitted value, validates that the record exists, is unexpired, and is not revoked, then revokes it and persists a replacement hash in the same family. The response has the same token-pair shape as login. Refresh failures use a generic `401 Unauthorized` response for unknown, expired, revoked, or reused tokens.

## Rotation, token families, and reuse detection

Every successful refresh is single-use: it revokes the presented token and records the replacement token ID. If an already-revoked token is presented to refresh again, the server treats it as possible theft/reuse and revokes every still-active token in that token family. Other device families belonging to that user are unaffected.

The refresh operation locks the matching token row with a JPA pessimistic write lock (`SELECT ... FOR UPDATE`) for the duration of a database transaction. Concurrent requests using the same token serialize: one rotates it; the other observes the revoked row, triggers family revocation, and fails. An entity `@Version` field provides additional optimistic-version protection for entity updates. The concurrent integration test verifies that two simultaneous calls cannot both produce successful rotations.

The rotation service returns a failure result for revoked-token reuse so the transaction can commit family revocation before `AuthService` converts that result into the safe HTTP error. Throwing inside the rotation transaction would roll back the family revocation.

If a client submits the same refresh token concurrently, the first response may contain a replacement that is subsequently revoked when the second request is classified as reuse. Clients should serialize refresh requests and retain only the latest token pair.

## Logout and endpoint security

`POST /api/v1/auth/logout` accepts the same refresh-token request body. It revokes that token's session family and returns `204 No Content`. Logout is idempotent: unknown and already-revoked token submissions also return `204`, and other device families remain active.

Registration and login remain public. Refresh and logout are explicitly public at the HTTP bearer-filter level and rely instead on possession of the opaque refresh token plus database expiry/revocation checks. The JWT access-token filter deliberately skips these two routes. `GET /api/v1/test` stays public; all other endpoints require a valid access token. The API remains stateless, with form login and HTTP Basic disabled and the existing CSRF decision unchanged. If a browser client later sends refresh tokens automatically in cookies, CSRF protection must be reconsidered.

## Refresh-token configuration for local development

The refresh lifetime can be set with an ISO-8601 duration using `JWT_REFRESH_TOKEN_EXPIRATION`, for example `P30D` or `P7D`. The access-token lifetime remains configurable separately through `app.jwt.access-token-expiration`. The JWT signing key remains environment-provided as `JWT_SECRET`; generate a fresh local key instead of committing one:

```powershell
$bytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToBase64String($bytes)
$env:JWT_SECRET = "<generated-base64-key>"
$env:JWT_REFRESH_TOKEN_EXPIRATION = "P30D"
mvn spring-boot:run
```

Production signing keys must be provisioned through a secret manager or deployment environment, not source control. The local and test values are not production credentials.

## Authentication request flow

```text
Login:
POST /api/v1/auth/login
  -> AuthController -> AuthService -> AuthenticationManager
  -> DaoAuthenticationProvider -> UserDetailsService + BCrypt
  -> RefreshTokenLifecycleService
  -> persist SHA-256 refresh-token hash -> return token pair

Protected API:
Authorization: Bearer <access-token>
  -> JwtAuthenticationFilter -> JwtService verifies signature/expiry
  -> UserDetailsService -> SecurityContextHolder -> controller

Refresh:
POST /api/v1/auth/refresh { refreshToken }
  -> hash opaque token -> lock matching database row
  -> check expiry/revocation -> revoke old token
  -> save replacement hash in same family -> return new token pair

Logout:
POST /api/v1/auth/logout { refreshToken }
  -> hash opaque token -> lock token row
  -> revoke its active family members -> 204 No Content
```

## Security considerations and limitations

Refresh tokens are bearer credentials: anyone who obtains one can use it until it expires or the family is revoked. Clients must protect them and use HTTPS. Hash-at-rest reduces the impact of a database read compromise but does not protect a token intercepted before storage. Reuse detection revokes the affected family, but already-issued access JWTs remain valid until their short expiration because access-token revocation is not implemented. Refresh-token cleanup/retention jobs, per-user account disablement, distributed revocation infrastructure, and browser-cookie flows are not implemented. `User` currently has no enabled/disabled status field to validate.

## Interview questions and answers

**Q1. Why use a refresh token in addition to an access token?**

The short-lived access token limits the time an exposed access credential can be used, while a refresh token allows the client to obtain a new pair without asking the user to enter credentials again.

**Q2. Why is the refresh token opaque instead of another JWT?**

The server needs to track refresh-token rotation and revocation. An opaque random identifier maps directly to a server-side record and carries no unnecessary claims.

**Q3. Why hash refresh tokens with SHA-256 rather than BCrypt?**

Refresh tokens contain 256 bits of cryptographically random entropy, making offline guessing infeasible. SHA-256 is fast and suitable for indexing and lookup; human passwords remain low-entropy and require BCrypt's deliberately expensive hashing.

**Q4. What is refresh-token rotation?**

Each successful refresh consumes the presented token, revokes it, and returns a newly generated token linked to the same session family.

**Q5. How does token-family reuse detection work?**

If a token already revoked by rotation is presented again, all active tokens in its family are revoked, invalidating that device session without logging out other devices.

**Q6. Why use a pessimistic lock for refresh?**

Two concurrent refresh requests must not both observe the same token as active. A database write lock serializes access to that row until the transaction commits.

**Q7. Why is a database transaction necessary for rotation?**

Revoking the old token and persisting its replacement must be atomic. If either operation fails, the transaction rolls back so the session is not left partially rotated.

**Q8. Is logout authenticated with the access JWT?**

No. In this API design, refresh and logout are explicitly possession-based operations. The body refresh token is hashed and checked against server-side session state; the JWT access filter skips both endpoints.

**Q9. Does logout immediately invalidate access JWTs?**

No. It revokes the refresh-token family, preventing further access-token renewal. Existing access tokens remain usable until their configured expiration because a JWT denylist is not part of this phase.

-----------------------------------------------------------------------


# Phase 3.4 – Refresh Token Authentication

## 1. Introduction to Refresh Tokens

In Phase 3.3, we implemented JWT access-token authentication.

After successful login, the backend generates an access token. The client sends this token with every protected API request.

However, access tokens should have a short lifetime for security reasons.

Consider this situation:

1. A user logs in successfully.
2. The backend generates an access token valid for 15 minutes.
3. The user works with the application for 20 minutes.
4. The access token expires.
5. The user tries to access a protected endpoint.
6. The backend rejects the expired token with HTTP 401.

Should the user enter their email and password again?

Not necessarily.

This is where refresh tokens are useful.

A refresh token allows the client to obtain a new access token without asking the user to enter their credentials again.

### Access Token vs Refresh Token

| Access Token | Refresh Token |
|---|---|
| Short-lived | Longer-lived |
| Used to access protected APIs | Used to obtain new access tokens |
| JWT in our implementation | Opaque random token |
| Validated by JWT signature and expiration | Validated against database hash and session state |
| Not stored as a session record in our design | Stored as a hash in database |
| Expires quickly | Expires after a longer configured period |

Our implementation uses:
- Short-lived JWT access tokens.
- 30-day server-side refresh-token expiration.
- Opaque refresh tokens.
- SHA-256 hashing for refresh-token storage.

---

## 2. Why Do We Need Refresh Tokens?

Without refresh tokens:

```text
User Login
    |
    v
Access Token Generated
    |
    v
User Uses Application
    |
    v
Access Token Expires
    |
    v
User Must Login Again
```

With refresh tokens:

```text
User Login
    |
    v
Access Token + Refresh Token
    |
    v
User Uses Application
    |
    v
Access Token Expires
    |
    v
Client Sends Refresh Token
    |
    v
Backend Validates Refresh Token
    |
    v
New Access Token + Refresh Token
    |
    v
User Continues Without Logging In Again
```

The refresh token improves the user experience while allowing access tokens to remain short-lived.

---

## 3. What Is an Opaque Refresh Token?

An opaque token is a random token whose contents do not represent readable application claims.

Unlike JWT, it does not contain a header, payload and signature.

Our refresh token is generated using cryptographically secure random data.

Conceptually:

```text
SecureRandom
     |
     v
Generate 256 Random Bits
     |
     v
URL-Safe Encoding
     |
     v
Opaque Refresh Token
```

Example:

```text
refreshToken:
a8f9c3d2e7b1...random-token-value...
```

The token does not expose:
- User email.
- User ID.
- Expiration timestamp.
- Roles.
- Other user information.

The backend identifies the associated session through its stored hash.

### Why not use JWT as the refresh token?

A JWT refresh token can be used, but an opaque random token is convenient for this architecture because:

- Its validity can be controlled through database records.
- It can be revoked immediately.
- Token rotation can be tracked.
- Reuse detection can be implemented.
- Session families can be managed.

---

## 4. SecureRandom

Java provides `SecureRandom` for generating cryptographically strong random values.

We use it to generate refresh tokens.

Conceptual example:

```java
SecureRandom secureRandom = new SecureRandom();

byte[] randomBytes = new byte[32];

secureRandom.nextBytes(randomBytes);
```

Here:

- 32 bytes are generated.
- 32 bytes contain 256 bits.
- The output has high randomness.
- The bytes can be encoded using a URL-safe Base64 encoder.

### Why SecureRandom instead of Random?

`Random` is designed for general-purpose pseudorandom number generation.

It is not appropriate for generating security-sensitive authentication tokens.

`SecureRandom` is designed for cryptographic applications.

A refresh token must be extremely difficult to guess.

---

## 5. SHA-256 Hashing for Refresh Tokens

Our implementation stores only the SHA-256 hash of the refresh token.

Suppose the generated token is:

```text
Raw Token = abc123xyz...
```

The backend calculates:

```text
SHA-256(Raw Token)
```

The database stores the resulting hash rather than the original token.

Conceptual flow:

```text
Generated Refresh Token
          |
          v
       SHA-256
          |
          v
      Token Hash
          |
          v
       Database
```

### Why SHA-256 instead of BCrypt?

We use different hashing strategies for passwords and refresh tokens.

| BCrypt | SHA-256 |
|---|---|
| Used for human-created passwords | Used for high-entropy random refresh tokens |
| Computationally expensive | Computationally fast |
| Includes a salt | Deterministic for the same input |
| Designed to slow password guessing | Suitable for efficiently looking up random tokens by hash |

Passwords are often predictable because humans choose them.

Refresh tokens are generated with high entropy using SecureRandom.

Therefore, SHA-256 is appropriate for hashing these random tokens, provided the token generation is sufficiently unpredictable.

### Important property

The same raw token produces the same SHA-256 hash.

```text
SHA256("abc123") = X

SHA256("abc123") = X
```

This makes database lookup possible.

The backend hashes the supplied refresh token and searches for the corresponding stored hash.

---

## 6. RefreshToken Entity

We introduced a separate RefreshToken entity.

Its purpose is to represent a user's refresh-token session.

Conceptual structure:

```text
RefreshToken
     |
     |-- id
     |-- user
     |-- tokenHash
     |-- tokenFamilyId
     |-- createdAt
     |-- expiresAt
     |-- revokedAt
     |-- replacedByTokenId
```

The exact fields should follow the implemented entity.

### Important fields

**id**

Unique identifier for the refresh-token record.

**user**

Reference to the user associated with the refresh-token session.

**tokenHash**

Stores the SHA-256 hash of the raw refresh token.

**tokenFamilyId**

Identifies the group of tokens belonging to the same authentication session.

**createdAt**

Records when the token was created.

**expiresAt**

Records when the token expires.

**revokedAt**

Indicates when the token was revoked.

**replacedByTokenId**

Can identify the replacement token when rotation occurs.

### Why a separate entity?

Access tokens and refresh tokens have different responsibilities.

The User entity represents application users.

The RefreshToken entity represents authentication sessions.

Keeping them separate makes token lifecycle management easier.

---

## 7. One User Can Have Multiple Refresh Sessions

A user may log in from multiple devices.

For example:

```text
User: rahul@example.com

        User
          |
     -------------
     |     |     |
     v     v     v
   Laptop Phone Tablet
     |     |     |
     v     v     v
  Session Session Session
     A      B      C
```

Each device can have its own refresh-token session.

This means logging out from the laptop should not automatically log out the phone.

Our implementation uses separate refresh-token families to support independent sessions.

### Why is this important?

It provides session-level control instead of treating every login as one global user session.

---

## 8. What Is a Token Family?

A token family groups refresh tokens that belong to the same login session.

When a refresh token is rotated, the replacement belongs to the same family.

Example:

```text
Token Family A

Refresh Token 1
      |
      | Rotation
      v
Refresh Token 2
      |
      | Rotation
      v
Refresh Token 3
      |
      | Rotation
      v
Refresh Token 4
```

All four tokens belong to the same token family.

The family allows the backend to identify the complete session history.

### Why do we need token families?

They help us:

- Track token rotation.
- Detect reuse of old tokens.
- Revoke an entire suspicious session.
- Avoid affecting unrelated device sessions.

---

## 9. What Is Refresh Token Rotation?

Refresh-token rotation means replacing the old refresh token with a new one after every successful refresh operation.

Example:

```text
Client sends Refresh Token A
             |
             v
Backend validates Token A
             |
             v
Token A is revoked
             |
             v
New Token B is generated
             |
             v
Token B is stored as a hash
             |
             v
New Access Token + Token B returned
```

The old token must no longer be accepted.

### Why rotate refresh tokens?

If an attacker steals a refresh token, rotation reduces the period during which that stolen token can be used.

After legitimate use, the old token becomes invalid.

The client must replace its stored refresh token with the newly issued one.

### Important rule

A successful refresh must not leave the old refresh token active.

---

## 10. Refresh Token Reuse Detection

This is one of the most important security features of Phase 3.4.

Consider this situation:

1. The client sends Refresh Token A.
2. The backend validates it.
3. The backend revokes A and issues B.
4. The client stores B.
5. An attacker later tries to use stolen Token A.

The backend discovers that Token A was already rotated.

This is called refresh-token reuse.

### What should happen?

The backend should treat this as a potential security incident.

Our implementation revokes the affected token family.

Example:

```text
Token Family A

Token A ---> Token B ---> Token C

Attacker reuses Token A
          |
          v
Reuse Detected
          |
          v
Revoke Family A
          |
          v
Token B and Token C become unusable
```

The user must authenticate again.

### Why revoke the entire family?

The backend cannot safely determine whether the legitimate client or an attacker possesses the latest token.

Revoking the affected family invalidates the suspicious session.

Other device sessions remain unaffected.

### Important distinction

An expired or unknown token is not necessarily proof of token reuse.

Reuse detection specifically concerns a previously rotated or revoked token whose use violates the expected rotation sequence.

---

## 11. What Is Token Revocation?

Revocation means invalidating a token before its natural expiration.

A token may be revoked because:

- The user logs out.
- Refresh-token reuse is detected.
- A suspicious session is identified.
- An administrative security action invalidates a session.

Our implementation tracks revocation using database state.

A token with a recorded revocation timestamp must not be accepted for refreshing.

This gives the backend immediate control over refresh-token validity.

---

## 12. Login Flow After Phase 3.4

Previously, login returned only an access token.

Now, successful login generates two tokens.

Endpoint:

```http
POST /api/v1/auth/login
```

Request:

```json
{
  "email": "user@example.com",
  "password": "StrongPassword123"
}
```

Response:

```json
{
  "accessToken": "<jwt-access-token>",
  "refreshToken": "<opaque-refresh-token>",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

### Backend operations

1. Authenticate email and password through Spring Security.
2. Generate JWT access token.
3. Generate random refresh token.
4. Hash refresh token using SHA-256.
5. Store its hash in the database.
6. Set the refresh-token expiration.
7. Return the raw refresh token to the client.

The raw refresh token is returned to the client once and is not stored in the database.

---

## 13. Refresh Endpoint

We implemented:

```http
POST /api/v1/auth/refresh
```

Request:

```json
{
  "refreshToken": "<opaque-refresh-token>"
}
```

Successful response:

```json
{
  "accessToken": "<new-jwt-access-token>",
  "refreshToken": "<new-opaque-refresh-token>",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

### Refresh request processing

```text
Client
  |
  v
POST /api/v1/auth/refresh
  |
  v
Receive Raw Refresh Token
  |
  v
Calculate SHA-256 Hash
  |
  v
Find Token Record
  |
  v
Check Expiration and Revocation
  |
  v
Check Reuse
  |
  v
Revoke Old Token
  |
  v
Generate New Refresh Token
  |
  v
Save New Token Hash
  |
  v
Generate New Access Token
  |
  v
Return Token Pair
```

The refresh operation is transactional.

---

## 14. Transaction Management in Token Rotation

Token rotation contains multiple database operations.

For example:

1. Find the old token.
2. Revoke the old token.
3. Generate a replacement token.
4. Store the replacement token hash.

These operations should be treated as one transactional operation.

Our implementation uses transaction management to keep the rotation consistent.

If the transaction fails, the database should not be left with an incorrectly completed rotation.

### Why is this important?

Imagine the old token is revoked successfully but saving the new token fails.

Without proper transaction handling, the user could lose their session unexpectedly.

Transactions help maintain consistency across the rotation operation.

---

## 15. What Is a Race Condition?

A race condition occurs when multiple operations access or modify shared data concurrently and the outcome depends on their execution timing.

Consider two refresh requests arriving almost simultaneously with the same token.

```text
Request A                  Request B
    |                          |
    v                          v
Find Token                Find Token
    |                          |
    v                          v
Token Active              Token Active
    |                          |
    v                          v
Revoke Token              Revoke Token
    |                          |
    v                          v
Create Token B            Create Token C
```

If the implementation does not protect the operation, both requests might succeed.

That is undesirable.

### Expected behavior

Only one refresh operation should successfully rotate the token.

The other request must be rejected.

Our implementation tests this behavior using two concurrent refresh requests.

---

## 16. Pessimistic Locking

We use pessimistic locking to protect refresh-token rotation.

Pessimistic locking obtains a database lock on the selected record so conflicting transactions cannot modify it concurrently.

Conceptual example:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<RefreshToken> findByTokenHash(String tokenHash);
```

The exact repository method signature should match the implemented code.

### How it works

```text
Request A
    |
    v
Acquire Database Lock
    |
    v
Validate and Rotate Token
    |
    v
Commit Transaction
    |
    v
Release Lock

Request B
    |
    v
Wait for Lock
    |
    v
Read Updated Token State
    |
    v
Reject Old Token
```

### Why use pessimistic locking?

It prevents two transactions from simultaneously rotating the same refresh-token record.

It is useful when concurrent updates must be serialized.

### Pessimistic vs Optimistic Locking

| Pessimistic locking | Optimistic locking |
|---|---|
| Acquires a database lock | Detects conflicting updates |
| Other transactions may wait | Conflicting transaction may fail |
| Useful for serialized updates | Useful when conflicts are uncommon |
| Can increase lock contention | Usually avoids long-held locks |

Our Phase 3.4 implementation uses pessimistic locking.

---

## 17. Logout and Refresh-Token Revocation

We implemented:

```http
POST /api/v1/auth/logout
```

Request:

```json
{
  "refreshToken": "<opaque-refresh-token>"
}
```

### Logout processing

1. Receive refresh token.
2. Calculate its SHA-256 hash.
3. Find the corresponding token record.
4. Identify its token family.
5. Revoke the appropriate session family.
6. Return a safe response.

### Why family-scoped logout?

Suppose a user has:

- Laptop session.
- Mobile session.
- Tablet session.

If the user logs out from the laptop, only the laptop's session family should be revoked.

The other sessions remain active.

Our logout behavior is idempotent: repeating a logout operation should not cause an additional security problem.

### Important

Our current logout endpoint is possession-based. It uses the refresh token to identify the session to revoke rather than requiring the short-lived access token.

The endpoint must therefore protect the refresh token as a sensitive credential.

---

## 18. Expiration of Refresh Tokens

Our implementation uses a configurable 30-day refresh-token lifetime.

The expiration is stored server-side in the database.

Example:

```text
Created At:  2026-11-22
Expires At:  2026-12-22
```

When the expiration time is reached, the token must not be accepted.

### Access token vs refresh-token expiration

```text
Access Token
    |
    | Short lifetime
    v
Expires quickly

Refresh Token
    |
    | Longer lifetime
    v
Expires after configured duration
```

Both expiration periods serve different purposes.

A refresh token must not be accepted merely because its hash exists in the database.

The backend must check its expiration and revocation state.

---

## 19. Database Design and Indexing

The RefreshToken table stores token-session metadata.

Conceptually:

```text
refresh_tokens
--------------------------------
id
user_id
token_hash
token_family_id
created_at
expires_at
revoked_at
replaced_by_token_id
--------------------------------
```

### Why add an index on token_hash?

Every refresh operation needs to find the stored token hash.

Without an index, the database may need to scan many rows.

An index helps make lookups more efficient.

### Why make token_hash unique?

A cryptographically generated refresh token should identify a single token record.

A unique constraint protects the database from duplicate stored hashes.

### Why use a foreign key to users?

It connects each refresh-token session to its owner.

This helps the application manage user sessions and their lifecycle.

---

## 20. Security Decisions in Phase 3.4

Our implementation follows these security decisions:

| Area | Decision |
|---|---|
| Access token | JWT |
| Access-token algorithm | HS256 |
| Refresh token | Opaque random token |
| Randomness | SecureRandom |
| Random token size | 256 bits |
| Refresh-token storage | SHA-256 hash |
| Refresh expiration | Configurable 30 days |
| Rotation | Enabled |
| Reuse detection | Enabled |
| Reuse response | Revoke affected token family |
| Concurrent refresh | Pessimistic locking |
| Logout | Family-scoped and idempotent |
| Multiple devices | Independent sessions |
| Refresh/logout authentication | Refresh-token possession |

These decisions provide a manageable token lifecycle without requiring long-lived JWT refresh tokens.

---

## 21. API Endpoints After Phase 3.4

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/v1/auth/register` | Register user |
| POST | `/api/v1/auth/login` | Login and receive both tokens |
| POST | `/api/v1/auth/refresh` | Rotate refresh token and renew access token |
| POST | `/api/v1/auth/logout` | Revoke the associated refresh-token family |
| GET | `/api/v1/test` | Public test endpoint |

Protected task endpoints will be implemented in a later milestone.

---

## 22. Testing Phase 3.4

We added automated tests for refresh-token lifecycle management.

### Token generation tests

- Refresh tokens are generated using secure randomness.
- Different generated tokens are unique.
- SHA-256 hashing is deterministic for the same token.
- The database stores the hash rather than the raw token.

### Login tests

- Login returns an access token.
- Login returns a refresh token.
- Refresh-token hash is persisted.
- Existing BCrypt authentication still works.

### Refresh tests

- Valid token generates a new token pair.
- Old token becomes revoked.
- New token hash is persisted.
- New token belongs to the same family.
- Expired token is rejected.
- Unknown token is rejected.
- Revoked token is rejected.
- Reuse of an old token revokes the affected family.
- Concurrent refresh attempts cannot both succeed.

### Logout tests

- Valid refresh token is revoked.
- Other device sessions remain active.
- Repeated logout is handled idempotently.
- Unknown-token behavior is handled safely.

### Regression tests

All earlier registration, login, JWT and Spring Security functionality continues to pass.

### Test command

```bash
mvn clean test
```

### Final Phase 3.4 result

| Metric | Result |
|---|---:|
| Total tests | 54 |
| Failures | 0 |
| Errors | 0 |

The full Java 21 Maven test suite passed.

---

## 23. Interview Questions and Answers

**Q1. What is a refresh token?**

A refresh token is a longer-lived credential used to obtain a new access token without asking the user to log in again.

**Q2. What is the difference between access and refresh tokens?**

An access token is used to access protected APIs and has a short lifetime. A refresh token is used to obtain new access tokens and has a longer lifetime.

**Q3. Why do we use opaque refresh tokens instead of JWT refresh tokens?**

Opaque tokens are easier to revoke and manage through database records. They also support rotation and reuse detection through server-side state.

**Q4. What is SecureRandom?**

SecureRandom is a Java class used to generate cryptographically strong random values.

**Q5. Why do we generate 256-bit refresh tokens?**

A large random token space makes guessing a valid token computationally impractical.

**Q6. Why do we store the refresh-token hash instead of the raw token?**

If the database is compromised, attackers cannot immediately use the stored hashes as refresh tokens.

**Q7. Why use SHA-256 for refresh tokens but BCrypt for passwords?**

Refresh tokens are high-entropy random values, making SHA-256 suitable for lookup. Human-created passwords have lower entropy, so BCrypt's deliberately expensive hashing is more appropriate.

**Q8. What is refresh-token rotation?**

Rotation means invalidating the old refresh token and issuing a new one whenever the client successfully refreshes its authentication.

**Q9. What is refresh-token reuse detection?**

It detects when a previously rotated or revoked refresh token is presented again. This may indicate token theft or replay.

**Q10. What is a token family?**

A token family groups refresh tokens that belong to the same login session across successive rotations.

**Q11. Why revoke an entire token family after detecting reuse?**

Because the backend cannot safely determine which party possesses the legitimate latest token. Revoking the affected family invalidates that session.

**Q12. What is a race condition?**

A race condition occurs when concurrent operations access or modify shared data and their execution timing affects the result.

**Q13. Why did we use pessimistic locking?**

To prevent concurrent requests from rotating the same refresh-token record simultaneously.

**Q14. What is the difference between pessimistic and optimistic locking?**

Pessimistic locking acquires a database lock before modification. Optimistic locking detects conflicting updates and rejects one of the transactions.

**Q15. What is token revocation?**

Token revocation means invalidating a token before its natural expiration.

**Q16. Why should logout revoke a refresh-token session?**

Because invalidating the session's refresh tokens prevents future access-token renewal through that session.

**Q17. Why support multiple refresh-token sessions per user?**

Users commonly log in from multiple devices. Separate sessions allow one device to log out without affecting other devices.

**Q18. What is idempotent logout?**

An idempotent logout operation can be repeated without causing additional unwanted effects.

**Q19. Why is refresh-token expiration stored in the database?**

It allows the backend to check the actual server-side expiration of the session rather than trusting client-provided information.

**Q20. Does a refresh token automatically invalidate an existing access token?**

No. Revoking a refresh token prevents future renewal, but an already-issued JWT access token normally remains valid until expiration unless access-token revocation is separately implemented.

---

## 24. Phase 3.4 Implementation Summary

### Components implemented

```text
AuthController
     |
     v
AuthService
     |
     |-------------------------
     |            |           |
     v            v           v
 JwtService  RefreshToken  UserRepository
                Service
                   |
                   v
            RefreshTokenRepository
                   |
                   v
              H2 Database
```

### Completed features

- Opaque refresh-token generation.
- SecureRandom with 256-bit random material.
- SHA-256 hash-at-rest.
- RefreshToken entity and repository.
- Indexed and unique token hash.
- Configurable 30-day expiration.
- Login returns access and refresh tokens.
- Refresh endpoint.
- Refresh-token rotation.
- Token-family management.
- Reuse detection.
- Family-scoped revocation.
- Pessimistic locking.
- Concurrency-safe refresh operation.
- Idempotent logout.
- Multiple independent device sessions.
- Safe authentication errors.
- Automated lifecycle and concurrency tests.

### Final verification

```bash
mvn clean test
```

**Result: 54 tests passed, 0 failures, 0 errors.**

Phase 3.4 is complete.

The application now supports registration, login, JWT access-token authentication, refresh-token rotation, session revocation and logout.

OAuth2 social login and task CRUD remain future milestones.

# Phase 3.5 – Authentication Security Hardening and Production Readiness

## 1. Audit summary

The existing JWT parser verifies signatures and expiration, refresh tokens are opaque and stored as SHA-256 hashes, and rotation/reuse handling uses token-family locking. The JWT filter is added to the Spring Security chain rather than registered as a second servlet filter. Login errors already use a generic response.

The audit identified two improvements:

- **Medium — login brute-force exposure:** login had no attempt throttling. A configurable, in-memory limiter now tracks both client IP and normalized account identifier.
- **Medium — registration account enumeration:** registration returned distinct success and duplicate-email responses. Both now return the same generic `202 Accepted` response.

No critical or high-severity issue was verified in the reviewed authentication implementation. Security controls already in place remain active.

## 2. Hardened authentication architecture

```text
Login request
    |
    v
IP + account rate limiter ---> 429 when limited
    |
    v
AuthenticationManager ---> enabled-account check + BCrypt password verification
    |
    +-- JWT access token (signed, short-lived, stateless)
    |
    +-- opaque refresh token (raw value returned once;
         SHA-256 hash stored; rotated under a database lock)

Password reset request
    |
    v
Generic response ---> delivery port ---> single-use, short-lived hashed token
                                          |
                                          v
                              change password + revoke refresh sessions
```

### JWT validation and claims

The HMAC signing secret is externalized as `JWT_SECRET`, Base64-decoded, and validated to contain at least 256 bits. The application issues HS256 tokens and parses them with the configured signing key; parsing verifies the signature and expiration before the subject or claims are used. Missing/blank subjects, missing `iat`, or a future `iat` are rejected. The current token contains the subject, issue time, expiration, and configured issuer; no audience is configured or treated as a required claim.

Do not log bearer tokens. Invalid, malformed, expired, wrong-signature, or unsupported-algorithm tokens do not authenticate the request. The filter loads the current user and rejects disabled accounts before setting the `SecurityContext`.

### Account status

`User.enabled` defaults to `true`, preserving enabled status for new registrations. Spring Security checks this property during password authentication; access-token authentication and refresh rotation also reject disabled users. Existing data stores that predate this column need a schema migration/backfill before deployment with the production `ddl-auto=validate` setting.

## 3. CORS and HTTP response headers

CORS is configured in Spring Security so valid preflight requests are handled before authentication. The API permits only configured explicit origins, `GET`, `POST`, and `OPTIONS`, and the `Authorization` and `Content-Type` headers. Credentialed CORS is disabled. `OPTIONS` is permitted by authorization rules.

Local development uses:

```properties
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

Production must set a comma-separated allowlist of actual frontend origins; no production URL is embedded in the code:

```text
CORS_ALLOWED_ORIGINS=https://frontend.example
```

The security chain sets `X-Content-Type-Options: nosniff`, denies framing, applies `strict-origin-when-cross-origin` referrer policy, and configures one-year HSTS with subdomains (emitted for secure requests). Authentication responses, including security errors, use `Cache-Control: no-store`. A Content Security Policy is not imposed because this API does not serve browser-rendered application pages.

## 4. Login rate limiting

`LoginRateLimiter` uses synchronized in-memory fixed windows and independent IP and normalized-account buckets. Limits, window duration, and maximum tracked keys are configurable:

```properties
LOGIN_RATE_LIMIT=10
LOGIN_RATE_WINDOW=PT1M
LOGIN_RATE_LIMIT_MAX_KEYS=10000
```

It uses the servlet peer address, not client-controlled forwarded headers. Excess requests return `429 Too Many Requests` with the same safe error format used by the API. This is intentionally a **single-process development control**, not a distributed production limiter: multiple application instances do not share counters, and production deployments requiring reliable abuse controls need a shared store such as Redis and trusted-proxy configuration.

## 5. Password change and reset

`POST /api/v1/auth/change-password` requires a valid access token and identifies the account from the authenticated principal, not a caller-supplied user ID. It verifies the current BCrypt password, applies the existing password validation, encodes the replacement password, and revokes all refresh-token sessions in a transaction.

`POST /api/v1/auth/forgot-password` returns the same generic `202 Accepted` response for known and unknown accounts. `POST /api/v1/auth/reset-password` accepts a one-time reset token and a validated new password. Reset tokens are generated using `SecureRandom`, have configurable short expiration (`PASSWORD_RESET_TOKEN_EXPIRATION`, default 15 minutes), are stored only as SHA-256 hashes, and are redeemed under a database lock. Successful password reset invalidates the token and all refresh sessions. It also invalidates any other outstanding unused reset tokens for that account.

Email delivery is not implemented. `PasswordResetTokenDelivery` is the integration port; without a configured provider, the service does not create or expose reset tokens. Tests use an isolated fake delivery implementation. A real production deployment must provide a secure delivery implementation before forgot-password can issue usable tokens.

## 6. Revocation and session limitations

Password changes and resets revoke every persisted refresh-token session. Logout revokes the relevant refresh-token family. The application does not maintain a JWT denylist or token version and does not query the database for every access-token request. Consequently, already-issued access JWTs remain valid until their short expiration even after logout, password change/reset, or refresh-session revocation. Immediate JWT revocation would require shared revocation state or per-request account/token-version validation and its associated availability and latency trade-offs.

## 7. Configuration and deployment

Common settings are in `application.properties`; `application-dev.properties` and `application-prod.properties` provide profile-specific defaults. Production requires externally supplied database and allowed-origin configuration, disables the H2 console and SQL display, and uses Hibernate schema validation rather than automatic schema creation. Activate the intended profile explicitly (for example, `SPRING_PROFILES_ACTIVE=dev` or `SPRING_PROFILES_ACTIVE=prod`). The sample production profile externalizes connection properties but does not add a production database driver or migration framework; those must be selected and deployed for the chosen production database. The `enabled` column and reset-token table also require a production schema migration before validation can succeed.

Key environment variables:

| Variable | Purpose |
|---|---|
| `JWT_SECRET` | Base64-encoded random HS256 key, minimum 32 bytes |
| `JWT_ACCESS_TOKEN_EXPIRATION` | Access-token lifetime, default 15 minutes |
| `JWT_REFRESH_TOKEN_EXPIRATION` | Refresh-token lifetime |
| `CORS_ALLOWED_ORIGINS` | Explicit frontend-origin allowlist |
| `LOGIN_RATE_LIMIT` | Requests per IP/account per window |
| `LOGIN_RATE_WINDOW` | Login rate-limit window, ISO-8601 duration |
| `LOGIN_RATE_LIMIT_MAX_KEYS` | Bound on in-memory tracking entries |
| `PASSWORD_RESET_TOKEN_EXPIRATION` | Password-reset token lifetime |
| `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` | Production database connection |

Never commit production secrets. The H2 in-memory database, local origin, and in-memory limiter are development conveniences, not production persistence or distributed security infrastructure.

## 8. Error handling, logging, and tests

Auth responses are not cacheable. Invalid credentials do not distinguish unknown users, wrong passwords, or disabled users. Invalid or expired refresh/reset tokens use generic errors; rate limiting returns HTTP 429. Validation errors remain structured, and unexpected errors do not return exception details to clients. Authentication logs record outcomes without passwords, hashes, bearer tokens, refresh tokens, or reset tokens; identifiers are omitted from the event messages.

Hardening tests cover allowed and disallowed CORS preflights, security headers, disabled-account login/access/refresh rejection, deterministic throttling behavior, password-change principal handling and session revocation, generic reset responses, token hashing, reset expiry/single-use behavior, and existing authentication regression scenarios.

```text
Enabled account + valid password -> authentication succeeds
Disabled account                  -> authentication rejected
Password change/reset             -> refresh sessions revoked
Existing access JWT               -> remains valid until expiry
```

This phase does not add OAuth2, application-level task authorization, a production email provider, a distributed rate limiter, or immediate JWT revocation.

# Phase 3.5.1 — Authentication Security Corrections

## 1. Password-reset invalidation after password change

**Problem:** a password change updated the user's BCrypt hash and revoked refresh sessions, but an already issued password-reset token could remain unused.

**Why it matters:** possession of an old reset link could allow a second password reset after a successful authenticated password change.

**Implementation:** `AuthService.changePassword()` now calls `PasswordResetService.invalidateAllForUserId()` in the same `@Transactional` password-change operation. The service performs a bulk update through `PasswordResetTokenRepository.invalidateUnusedByUserId()`, marking unused reset tokens as used at the current transaction time. Refresh sessions are revoked in the same transaction. Password reset redemption still uses a pessimistic row lock and marks the selected token as used.

**Practical example:**

```text
Reset token requested
Authenticated password changed
All unused reset tokens marked used
Old reset token -> 400 password reset request is invalid or expired
New reset token -> accepted once
```

**Limitation:** concurrency between a password change and reset redemption is database-lock dependent. The transaction and invalidation bulk update are consistent, and the reset redemption lookup uses a write lock, but a production deployment should use the same database and transaction isolation settings and migrations for the reset-token table.

**Interview question:** Why invalidate reset tokens inside the password-change transaction?

**Answer:** Password, refresh-session revocation, and reset-token invalidation should commit or roll back together. Otherwise a successful password change could still leave reset credentials usable.

## 2. Bounded login rate-limiter capacity

**Problem:** when the login limiter's key map was full, a request for a new key could raise `429` for that key, effectively making capacity exhaustion affect unrelated clients.

**Why it matters:** key exhaustion can become an availability attack.

**Phase 3.5.1 implementation (superseded in Phase 3.5.2):** the initial correction used a bounded access-ordered `LinkedHashMap` and LRU eviction. Phase 3.5.2 replaced it with fixed HMAC-indexed buckets to avoid evicting active blocked identities. See the later correction section for the current design and its collision tradeoffs.

**Security tradeoff:** least-recent/low-request eviction helps prevent one burst of distinct keys from blocking unrelated traffic, but it can evict a partially active attacker bucket and allow some additional attempts. Blocked buckets are therefore protected first. Eviction is not an identity or distributed security guarantee.

**Practical example:**

```text
Map reaches capacity
Remove expired buckets
Evict oldest low-request bucket
Add the unrelated key
Preserve keys that already reached the login limit
```

**Interview question:** Why not simply throw `429` whenever the map is full?

**Answer:** that turns limiter memory exhaustion into a global denial-of-service mechanism. Bounded eviction maintains availability, while the tradeoff is that the eviction policy must avoid an obvious reset of active attack counts.

## 3. Separate password-reset rate limiting

**Problem:** `/api/v1/auth/forgot-password` had no dedicated public-endpoint throttling or resend cooldown.

**Why it matters:** a real mail provider could be abused to send repeated reset emails and increase account or provider cost.

**Implementation:** `PasswordResetRateLimiter` maintains independent synchronized IP and normalized-account buckets. It has separate configurable properties:

```properties
app.security.password-reset-limit=3
app.security.password-reset-window=PT1H
app.security.password-reset-rate-limit-max-keys=10000
app.security.password-reset-cooldown=PT1M
```

The forgot-password controller passes the servlet peer address to `AuthService.requestPasswordReset()`. Excess requests throw `RateLimitExceededException`, which the existing REST advice converts to `429 Too Many Requests`. Login and reset policies are independent.

The account bucket enforces a resend cooldown, and IP/account windows enforce repeated-request limits. `PasswordResetService` still returns only generic responses and never returns raw reset tokens.

**Practical example:**

```text
Known or unknown email:
  202 + generic response

Same account before cooldown:
  429 Too Many Requests

Excess requests from one IP:
  429 Too Many Requests
```

**Limitation:** this remains in-memory and single-instance. A load-balanced deployment needs a shared distributed limiter and trusted-proxy configuration.

**Interview question:** Why have separate login and reset limits?

**Answer:** their abuse patterns and availability cost differ. Login is credential guessing; reset is account spam and email-provider cost. Separate policies allow independent tuning.

## 4. Registration timing mitigation

**Problem:** registration checked `existsByEmail()` before BCrypt encoding, so a duplicate-account request could avoid the expensive password-hash operation.

**Why it matters:** a measurable timing difference can help infer whether an account is registered.

**Implementation:** `AuthService.register()` now performs BCrypt password encoding before the duplicate-email lookup. New and duplicate account flows therefore perform the expensive operation before account existence is known. The duplicate check still returns the generic registration response, and database unique constraints remain the final protection for concurrent registration races.

**Regression tests verify:** new registration succeeds, duplicate registration is generic, password hashes differ from raw passwords, validation is unchanged, and concurrent duplicate requests both receive `202 Accepted`.

**Limitations:** one code-path comparison does not eliminate timing side channels. Database and framework overhead, duplicate inserts, caching, network jitter, and concurrent load can remain observable. The mitigation reduces a known BCrypt timing asymmetry but does not claim equal timing.

**Interview question:** Why encode before checking duplicate email?

**Answer:** it reduces the difference caused by intentionally expensive password hashing, while retaining the database uniqueness constraint as the authoritative concurrent-registration guard.

## 5. Forgot-password timing mitigation

**Problem:** existing accounts require lookup, token persistence, and token delivery work, while nonexistent accounts returned earlier. The same synchronous behavior could remain observable.

**Why it matters:** response timing or work duration can leak account eligibility.

**Implementation:** delivery is now scheduled through Spring `TaskExecutor` after successful transaction commit instead of being invoked directly from the account lookup path. The API response remains generic for known and unknown accounts. Reset-token generation, SHA-256 storage, and persistence remain transactional and secure. The existing `PasswordResetTokenDelivery` port remains ready for a future email provider.

**The system still does not claim email delivery.** Without a configured provider, no token is delivered. Tests use an isolated fake delivery implementation.

**Limitations:** the account lookup and transactional reset-token persistence are still synchronous, so this is not a guarantee of equal response timing. Full equalization requires a carefully designed asynchronous delivery/job system and, for email, an actual provider and queue infrastructure. That infrastructure is intentionally not implemented in this milestone.

**Interview question:** Why is async delivery useful here?

**Answer:** it removes provider I/O from the HTTP response path and schedules delivery only after the reset-token transaction commits. It does not itself prove equal timing or prove that every account exists.

## 6. Security logging and 429 responses

Authentication and reset limits use structured `429` handling with `Cache-Control: no-store`. Reset-limit messages are separate from login-limit messages. Raw reset tokens, raw refresh tokens, passwords, password hashes, and JWT signing secrets are not logged.

## 7. Security regression testing

Phase 3.5.1 tests cover:

- reset-token invalidation after password change;
- old reset-token rejection and new reset-token redemption;
- refresh-session revocation after password change;
- bounded login limiter capacity and concurrency;
- bounded password-reset limiter capacity and concurrency;
- independent login/reset limits;
- IP/account reset cooldown and generic responses;
- registration encoding before duplicate lookup;
- concurrent duplicate registration safety;
- async test-provider reset delivery;
- existing JWT, refresh rotation, logout, registration, login, CORS, header, and password-reset behavior.

The in-memory limiters are development/single-instance controls. Distributed production throttling and real email delivery remain future infrastructure work.


------------------------------------------------

# Phase 3.5 – Authentication Security Hardening and Production Readiness

## 1. Introduction to Authentication Hardening

In the previous phases, we implemented:

- User registration.
- BCrypt password hashing.
- JWT access-token authentication.
- Refresh-token generation and rotation.
- Refresh-token reuse detection.
- Logout and session revocation.

However, implementing authentication does not automatically make an application production-ready.

We must also consider:

- Unauthorized access.
- Brute-force attacks.
- Token leakage.
- Weak configuration.
- Cross-Origin Resource Sharing.
- Security headers.
- Password recovery.
- Account status.
- Sensitive logging.
- Production secrets.

Authentication hardening means reviewing and improving the existing security implementation to reduce vulnerabilities.

### Authentication development journey

```text
User Registration
       |
       v
Password Hashing
       |
       v
User Login
       |
       v
JWT Access Token
       |
       v
Refresh Token Rotation
       |
       v
Session Revocation
       |
       v
Authentication Hardening
       |
       v
Production Readiness
```

---

## 2. Security Audit

A security audit is a systematic review of an application's security implementation.

Before making changes, we inspect the existing application and identify weaknesses.

### What should we audit?

| Component | What to verify |
|---|---|
| Registration | Password hashing and validation |
| Login | Credential verification and rate limiting |
| JWT | Signature and expiration validation |
| JWT Filter | Correct authentication behavior |
| Refresh Token | Rotation and reuse detection |
| Logout | Correct session revocation |
| SecurityConfig | Public and protected endpoints |
| CORS | Allowed frontend origins |
| Exception Handling | Safe error responses |
| Logging | No sensitive information exposed |
| Configuration | No hardcoded production secrets |

### Security issue severity

Security findings can be categorized as:

- Critical: A severe vulnerability that could compromise the application.
- High: A serious vulnerability with significant security impact.
- Medium: A weakness that should be addressed.
- Low: A minor issue or improvement.

The goal of an audit is to identify actual problems rather than inventing vulnerabilities.

---

## 3. JWT Claim Validation

In Phase 3.3, we implemented JWT generation and validation.

JWT validation should verify more than just whether a token can be parsed.

Important validation checks include:

1. Signature verification.
2. Expiration validation.
3. Expected signing algorithm.
4. Expected issuer, when configured.
5. Expected audience, when configured.
6. Required claims.
7. Token structure.

### Why is signature validation important?

Suppose an attacker modifies a JWT payload:

```json
{
  "sub": "user@example.com",
  "role": "ADMIN"
}
```

If the application trusts the payload without validating its signature, the attacker might manipulate token information.

A correctly implemented JWT validator verifies the signature before trusting claims.

### Why validate the issuer?

The issuer (`iss`) identifies the system that issued the token.

Example:

```json
{
  "iss": "task-management-api"
}
```

The application can reject tokens issued by an unexpected source.

### Why validate the audience?

The audience (`aud`) identifies the intended recipient of the token.

It helps prevent a token issued for one application or service from being accepted by another unintended service.

### Important rule

Never trust JWT payload data before verifying the token's signature.

---

## 4. Signing Secret Management

Our JWT implementation uses HS256.

HS256 requires a shared secret for token signing and verification.

Hardcoding a signing secret in Java source code is dangerous.

Bad approach:

```java
private static final String SECRET = "my-secret-key";
```

Better approach:

```properties
app.jwt.secret=${JWT_SECRET}
```

The actual secret is supplied through an environment variable.

### Why use environment variables?

- Keeps production secrets outside source code.
- Reduces accidental exposure through GitHub.
- Allows different secrets in different environments.
- Simplifies deployment configuration.

### Secret management best practices

1. Use a sufficiently strong random secret.
2. Never commit production secrets.
3. Never print secrets in logs.
4. Never expose secrets to frontend applications.
5. Rotate secrets according to a planned security policy.
6. Use a secret manager in production where appropriate.

---

## 5. User Account Status

A user may exist in the database but should not necessarily be allowed to authenticate.

For example:

- An administrator disables an account.
- A suspicious account is temporarily suspended.
- An account is deactivated.

This is why applications often maintain an account status.

Example:

```text
User
 |
 |-- id
 |-- name
 |-- email
 |-- passwordHash
 |-- enabled
 |
 v
Database
```

### Enabled vs Disabled

| Account status | Authentication |
|---|---|
| Enabled | May authenticate if credentials are valid |
| Disabled | Authentication must be rejected |

Spring Security's UserDetails representation supports account status checks.

A disabled account should not be able to obtain new authentication tokens.

Existing refresh-token sessions should also respect the account's current status.

---

## 6. What is CORS?

CORS stands for **Cross-Origin Resource Sharing**.

It is a browser security mechanism that controls whether a frontend application can make requests to a backend hosted on a different origin.

### What is an Origin?

An origin consists of:

- Protocol.
- Domain or hostname.
- Port.

Example:

```text
Frontend:
http://localhost:3000

Backend:
http://localhost:8080
```

These are different origins because their ports differ.

A browser may restrict frontend JavaScript from reading backend responses unless the backend allows the frontend origin through CORS.

### Example

```text
React Frontend
http://localhost:3000
       |
       | HTTP Request
       v
Spring Boot API
http://localhost:8080
       |
       v
CORS Validation
       |
       v
Allowed or Rejected
```

### Why do we need CORS?

When our React frontend communicates with our Spring Boot backend, the browser may enforce cross-origin restrictions.

CORS allows the backend to specify which origins are permitted.

---

## 7. CORS Configuration

A CORS policy can define:

- Allowed origins.
- Allowed HTTP methods.
- Allowed headers.
- Whether credentials are supported.
- Preflight request handling.

Example conceptual configuration:

```java
CorsConfiguration configuration =
        new CorsConfiguration();

configuration.setAllowedOrigins(
        List.of("http://localhost:3000")
);

configuration.setAllowedMethods(
        List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
);

configuration.setAllowedHeaders(
        List.of("Authorization", "Content-Type")
);
```

The actual production origin should come from external configuration.

### What is a Preflight Request?

Before some cross-origin requests, browsers send an HTTP OPTIONS request to check whether the actual request is permitted.

Example:

```http
OPTIONS /api/v1/auth/login
Origin: http://localhost:3000
Access-Control-Request-Method: POST
Access-Control-Request-Headers: content-type
```

The backend responds with the applicable CORS permissions.

### Important CORS security rules

- Do not allow every origin in production without justification.
- Allow only necessary HTTP methods.
- Allow only required headers.
- Do not combine wildcard origins with credentialed requests.
- Configure CORS correctly within Spring Security.

**Important:** CORS is a browser-enforced access policy, not an authentication mechanism. It does not replace JWT validation or API authorization.

---

## 8. HTTP Security Headers

HTTP security headers provide additional protection through server responses.

They tell browsers how to handle application content and certain security-sensitive behaviors.

Important headers:

| Header | Purpose |
|---|---|
| X-Content-Type-Options | Prevents MIME-type sniffing |
| Strict-Transport-Security | Encourages HTTPS-only access |
| Content-Security-Policy | Restricts permitted content sources |
| Referrer-Policy | Controls referrer information |
| Cache-Control | Controls response caching |
| X-Frame-Options | Controls framing behavior |

### 8.1 X-Content-Type-Options

Example:

```http
X-Content-Type-Options: nosniff
```

It prevents browsers from trying to interpret content using an unexpected MIME type.

### 8.2 Strict-Transport-Security

Example:

```http
Strict-Transport-Security: max-age=31536000
```

It instructs browsers to use HTTPS for future connections to the applicable host.

This should only be enabled appropriately for HTTPS deployments.

### 8.3 Content-Security-Policy

CSP controls which sources the browser is allowed to load content from.

It is particularly useful for browser-rendered applications.

For a REST API, configure it according to the actual deployment rather than blindly copying policies intended for HTML applications.

### 8.4 Cache-Control

Sensitive authentication responses should not be cached unnecessarily.

Example:

```http
Cache-Control: no-store
```

This is useful for responses containing authentication tokens.

---

## 9. Brute-Force Attacks

A brute-force attack attempts many password combinations to gain unauthorized access.

Example:

```text
Login Attempt 1: password123
Login Attempt 2: admin123
Login Attempt 3: qwerty123
Login Attempt 4: welcome123
...
```

An attacker may automate thousands of attempts.

### How can we prevent brute-force attacks?

- Rate limiting.
- Monitoring repeated failures.
- Strong password policies.
- CAPTCHA where appropriate.
- Suspicious activity detection.
- Appropriate account security policies.

---

## 10. What is Rate Limiting?

Rate limiting restricts how many requests a client can make within a defined period.

Example:

```text
Login endpoint:

Maximum 5 attempts
Within 1 minute
```

If the limit is exceeded, the server can return:

```http
HTTP/1.1 429 Too Many Requests
```

### Why use rate limiting?

It helps protect against:

- Brute-force login attempts.
- Automated password guessing.
- Excessive API requests.
- Certain forms of abuse.

### Rate limiting architecture

```text
Client
  |
  v
Login Request
  |
  v
Rate Limiter
  |
  |-- Within Limit
  |       |
  |       v
  |    AuthService
  |
  |-- Limit Exceeded
          |
          v
      HTTP 429
```

### In-memory vs Distributed Rate Limiting

| In-memory limiter | Distributed limiter |
|---|---|
| Stores counters in application memory | Stores shared counters |
| Simple to implement | Suitable for multiple instances |
| Resets on application restart | Can persist across instances |
| Not shared across servers | Shared through infrastructure such as Redis |

An in-memory limiter is useful for development and single-instance applications.

For a horizontally scaled production system, a distributed rate limiter is generally more appropriate.

---

## 11. Password Change

A password-change feature allows an authenticated user to update their password.

Suggested endpoint:

```http
POST /api/v1/auth/change-password
```

Example request:

```json
{
  "currentPassword": "OldPassword123",
  "newPassword": "NewStrongPassword456"
}
```

### Password change flow

```text
Authenticated User
       |
       v
Change Password Request
       |
       v
Verify Current Password
       |
       v
Validate New Password
       |
       v
Hash New Password using BCrypt
       |
       v
Update User Record
       |
       v
Revoke Refresh Sessions
       |
       v
Return Success
```

### Important security rules

1. Require authentication.
2. Verify the current password.
3. Validate the new password.
4. Hash the new password using BCrypt.
5. Never store the raw password.
6. Never return the password hash.
7. Identify the user through the authenticated principal.
8. Revoke existing refresh-token sessions after a successful password change.

### Why revoke refresh tokens?

If an attacker previously obtained a refresh token, changing the password should prevent that old session from continuing to obtain new access tokens.

Existing access JWTs may still remain valid until expiration unless a separate access-token revocation mechanism exists.

---

## 12. Forgot Password and Password Reset

Users sometimes forget their passwords.

A password-reset workflow allows them to recover access without knowing their current password.

Suggested endpoints:

```http
POST /api/v1/auth/forgot-password
POST /api/v1/auth/reset-password
```

### Forgot-password flow

```text
User
 |
 v
Submit Email
 |
 v
Backend
 |
 v
Generate Random Reset Token
 |
 v
Hash Reset Token
 |
 v
Store Hash and Expiration
 |
 v
Send Reset Link through Email Provider
```

### Reset-password flow

```text
User Opens Reset Link
        |
        v
Submit Reset Token + New Password
        |
        v
Backend Hashes Supplied Token
        |
        v
Find Matching Reset Record
        |
        v
Validate Token and Expiration
        |
        v
Update Password using BCrypt
        |
        v
Mark Reset Token as Used
        |
        v
Revoke Refresh Sessions
        |
        v
Return Success
```

### Security requirements

- Generate a cryptographically secure random token.
- Store only its hash.
- Set a short expiration.
- Make the token single-use.
- Reject expired tokens.
- Never expose the raw reset token through production API responses.
- Never reveal whether an email address exists.

### Why use a single-use reset token?

If a reset token could be used repeatedly, an attacker might reuse a stolen token to change the password multiple times.

Making it single-use reduces this risk.

### Email delivery dependency

A real password-reset workflow requires an email delivery provider.

A development mock or abstraction is not equivalent to actual email delivery.

---

## 13. Access Token Revocation

JWT access tokens are normally validated through their signature and expiration.

In our current architecture, access tokens are stateless.

This creates an important limitation.

Suppose:

1. User logs in.
2. Backend issues an access token valid for 15 minutes.
3. User logs out.
4. Backend revokes the refresh-token family.

The existing access token may still be valid until its expiration.

### Why?

Revoking a refresh token prevents future access-token renewal.

It does not automatically invalidate an already-issued JWT.

### Possible approaches

**Approach 1: Short access-token lifetime**

Use a short expiration to limit the impact of stolen access tokens.

**Approach 2: Token denylist**

Maintain revoked JWT identifiers in a database or cache.

**Approach 3: Token versioning**

Maintain a version or security timestamp associated with the user and compare it against token claims.

Each approach has performance and architectural trade-offs.

Our current implementation does not automatically provide immediate access-token revocation.

---

## 14. Security Logging

Security logging records important authentication-related events.

Examples:

- Failed login attempts.
- Successful login.
- Refresh-token reuse detection.
- Session revocation.
- Password change.
- Password reset.
- Rate-limit violations.

### What should never be logged?

Never log:

- Plain-text passwords.
- Password hashes.
- Raw JWTs.
- Raw refresh tokens.
- Raw password-reset tokens.
- JWT signing secrets.

### Why is logging important?

It helps developers and security teams:

- Investigate suspicious activity.
- Understand authentication failures.
- Detect repeated attacks.
- Monitor application security.
- Troubleshoot production issues.

Logs must be useful without exposing credentials or unnecessary personal information.

---

## 15. Development, Test and Production Profiles

Spring Boot supports different configuration profiles.

Common profiles:

| Profile | Purpose |
|---|---|
| dev | Local development |
| test | Automated testing |
| prod | Production deployment |

Example:

```text
application.properties
application-dev.properties
application-test.properties
application-prod.properties
```

### Why use profiles?

Different environments require different configurations.

For example:

**Development**
- H2 database.
- H2 Console if needed.
- Local frontend origin.
- Development-only test configuration.

**Testing**
- Test database configuration.
- Test JWT secret.
- Deterministic test behavior.
- No real external email delivery.

**Production**
- Production database.
- External signing secrets.
- Restricted CORS origins.
- HTTPS.
- No H2 Console.
- Appropriate logging configuration.

### Important security principle

Never commit production secrets into the repository.

Environment variables or secret-management systems should supply sensitive configuration.

---

## 16. Authentication Error Handling

A secure API should return appropriate HTTP status codes without exposing internal information.

| Status | Meaning | Example |
|---|---|---|
| 400 | Bad Request | Invalid request DTO |
| 401 | Unauthorized | Missing or invalid JWT |
| 403 | Forbidden | Insufficient permission |
| 409 | Conflict | Duplicate email |
| 429 | Too Many Requests | Login rate limit exceeded |
| 500 | Internal Server Error | Unexpected server failure |

### Why should errors be generic?

Consider login with an unknown email.

Returning:

"Email does not exist"

could help an attacker discover registered accounts.

A safer response is:

"Invalid email or password."

Similarly, forgot-password endpoints should not reveal whether an account exists.

---

## 17. Authentication Security Architecture

After hardening, the conceptual architecture looks like this:

```text
                 CLIENT
                    |
                    v
               AuthController
                    |
                    v
                AuthService
                    |
        -------------------------
        |           |           |
        v           v           v
    JwtService  RefreshToken  PasswordReset
                   Service       Service
        |           |           |
        -------------------------
                    |
                    v
              Repositories
                    |
                    v
                 Database

Incoming Protected Requests
             |
             v
      SecurityFilterChain
             |
             v
    JwtAuthenticationFilter
             |
             v
     JWT Signature Validation
             |
             v
       SecurityContext
             |
             v
       Authorization Rules
             |
             v
          Controller
```

Additional security controls:

- CORS.
- HTTP security headers.
- Rate limiting.
- Security logging.
- Environment-based configuration.

---

## 18. Testing Authentication Security

Testing should verify not only successful authentication but also security failure scenarios.

Important test categories:

### JWT tests

- Valid token accepted.
- Expired token rejected.
- Invalid signature rejected.
- Unexpected issuer rejected when configured.
- Malformed token rejected.

### Account tests

- Enabled user can authenticate.
- Disabled user cannot authenticate.
- Disabled user cannot refresh tokens.

### CORS tests

- Allowed origin accepted.
- Disallowed origin rejected.
- OPTIONS preflight handled.
- Authorization header permitted.

### Rate-limiting tests

- Requests within the limit succeed.
- Excessive requests return 429.
- Counters behave correctly.

### Password management tests

- Valid current password accepted.
- Invalid current password rejected.
- New password hashed.
- Reset token expires.
- Reset token cannot be reused.
- Refresh sessions revoked after password changes.

### Regression tests

All previous registration, login, JWT, refresh, rotation and logout behavior should continue working.

---

## 19. Interview Questions and Answers

**Q1. What is authentication hardening?**

Authentication hardening is the process of improving an application's authentication security by addressing vulnerabilities, strengthening configuration and adding appropriate security controls.

**Q2. What is a security audit?**

A security audit is a systematic examination of an application's security implementation to identify weaknesses and verify security controls.

**Q3. Why validate JWT claims?**

JWT claim validation ensures the token has the expected identity, issuer, audience and expiration information.

**Q4. What is CORS?**

Cross-Origin Resource Sharing is a browser security mechanism that controls which origins can access resources from another origin.

**Q5. Is CORS an authentication mechanism?**

No. CORS controls browser cross-origin access and does not replace authentication or authorization.

**Q6. What is a preflight request?**

It is an HTTP OPTIONS request sent by a browser to check whether a cross-origin request is permitted.

**Q7. What is an HTTP security header?**

It is an HTTP response header that instructs browsers to apply certain security-related behaviors.

**Q8. What is HSTS?**

HTTP Strict Transport Security tells browsers to use HTTPS for future connections to the applicable host.

**Q9. What is rate limiting?**

Rate limiting restricts how many requests a client can make during a specified time period.

**Q10. What is a brute-force attack?**

It is an attack that attempts many possible passwords or credentials to gain unauthorized access.

**Q11. What is HTTP 429?**

It indicates that the client has sent too many requests within a specified period.

**Q12. What is password change?**

It is the process by which an authenticated user changes their existing password after verifying their current credentials.

**Q13. What is forgot-password functionality?**

It allows users who cannot remember their password to request a secure password-reset process.

**Q14. Why should password-reset tokens be single-use?**

Single-use tokens prevent an already-consumed recovery credential from being reused to reset a password again.

**Q15. Why store reset-token hashes?**

Storing only hashes reduces the risk of an attacker immediately using reset tokens if the database is compromised.

**Q16. What is access-token revocation?**

It is the process of invalidating an access token before its natural expiration.

**Q17. Does logout automatically invalidate a JWT?**

No. In a stateless JWT design, an issued access token may remain valid until expiration unless additional revocation mechanisms exist.

**Q18. Why use environment profiles?**

Profiles allow the application to use different configurations for development, testing and production.

**Q19. Why should sensitive information not be logged?**

Logs may be accessible to developers, monitoring systems and third parties. Exposing credentials or tokens through logs can lead to account compromise.

**Q20. Why use HTTPS in production?**

HTTPS protects data in transit and reduces the risk of credentials and authentication tokens being intercepted.

---

## 20. Phase 3.5 Implementation Summary

The objective of Phase 3.5 is to strengthen the existing authentication module.

Areas covered:

- Authentication security audit.
- JWT claim validation.
- Signing-secret management.
- Account status validation.
- CORS configuration.
- HTTP security headers.
- Login rate limiting.
- Brute-force protection.
- Password change.
- Forgot-password and reset-password.
- Refresh-session invalidation.
- Access-token revocation assessment.
- Security logging.
- Development and production profiles.
- Automated security testing.

### Important limitations to document

Do not claim that a feature is implemented unless it exists in the actual source code.

For example:

- CORS configuration does not replace authentication.
- In-memory rate limiting is not shared across multiple application instances.
- Password-reset email delivery requires an actual provider.
- Refresh-token revocation does not automatically revoke existing access JWTs.
- JWT signature validation does not encrypt the payload.

**Phase 3.5 is the authentication hardening milestone.** After its implementation and verification, the authentication module will be ready for the next stage of application development, subject to any remaining production dependencies and security findings.

# Phase 3.5.2 — Final Authentication Security Corrections

## 1. External JWT signing secret

**Problem:** a signing key included in shared application properties can be copied into deployed environments or source history.

**Implementation:** shared, development, and production configuration now require `JWT_SECRET` through `${JWT_SECRET}` with no fallback value. `JwtService` rejects a missing value, malformed Base64, or a decoded key shorter than 32 bytes during bean construction, so startup fails rather than falling back to a known secret. The only configured static secret is a clearly test-only value in `src/test/resources/application.properties`; JWT unit tests read it from that file. Do not copy it to a deployment.

For development, supply a fresh random Base64-encoded key using the local environment or secret manager:

```powershell
$env:JWT_SECRET = "<base64-encoded-random-key-of-at-least-32-bytes>"
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

Production must supply a unique secret using its deployment secret manager. There is no committed production value and no default inherited from the test configuration.

**Practical example:** starting the application with an unset `JWT_SECRET` fails configuration/bean initialization; a too-short decoded key fails the HS256 strength check.

**Interview question:** Why should a JWT secret be externalized even if the repository is private?

**Answer:** source access expands over time, secrets are difficult to rotate once committed, and the same artifact should be deployable with environment-specific credentials without rebuilding.

## 2. Account enumeration through password-reset throttling

**Problem:** an account-specific HTTP 429 reveals that an account bucket has been populated, allowing a caller to test emails from many IPs.

**Implementation:** the reset limiter now distinguishes its internal decisions. Exceeding the IP limit still returns the normal `429 Too Many Requests`. Exceeding the normalized-account limit returns an internal suppression decision; `AuthService` does not invoke reset-token generation, persistence, or delivery for that request, while the controller returns the same generic `202 Accepted` response. Unknown accounts receive the same generic response. Raw tokens are never returned.

IP and account thresholds are independently configurable:

```properties
PASSWORD_RESET_IP_LIMIT=10
PASSWORD_RESET_ACCOUNT_LIMIT=3
PASSWORD_RESET_RATE_WINDOW=PT1H
PASSWORD_RESET_COOLDOWN=PT1M
```

Account throttling applies to submitted identifiers without consulting account existence, so an unknown address and an existing throttled address follow the same response path at the HTTP layer. IP abuse remains independently observable through `429`.

**Practical example:** first eligible request for an address may enqueue delivery; subsequent requests for the normalized address from other IPs still return the generic 202 but do not create another token or invoke delivery. An IP exceeding its separate request threshold receives 429 regardless of account status.

**Interview question:** Why suppress account-level throttling rather than returning 429?

**Answer:** account-specific status codes reveal that the submitted identifier has prior state. Silent suppression retains account-level abuse control without making that state an HTTP oracle.

## 3. Bounded limiter memory and identity churn

**Problem:** an LRU map can evict a blocked identity at capacity, while refusing all new identities at capacity harms availability. A bounded process cannot retain exact counters for an unlimited number of identities.

**Implementation:** both in-memory limiters now use separate fixed-size arrays for IP and account time-window buckets. Each identity is mapped using a process-random HMAC-SHA-256 key; no identity strings are retained in the bucket store. `*_RATE_LIMIT_MAX_KEYS` controls the slot count for each dimension, so combined storage is at most twice that setting per limiter. Expired buckets are cleaned on each check (subject to the account resend cooldown). There is no LRU policy that explicitly deletes a blocked identity to make room. The reset limiter uses separate configurable IP and account thresholds.

**Tradeoff and limitation:** hashing multiple identities into a finite number of slots can cause collisions and false positives: unrelated login identities can share a counter (and may receive a 429), while reset-account collisions are silently suppressed. This avoids global refusal merely because a map filled and prevents identity churn from explicitly evicting an active blocked bucket. It does **not** provide exact per-identity enforcement or guarantee that arbitrary client keys will never influence one another. A process restart creates a new HMAC key and resets volatile state, and separate instances do not share counters. Strong distributed enforcement requires shared infrastructure, intentionally not added here.

**Practical example:** if the configured slot array is full, a new identity still maps to a slot and can proceed while that slot's counter is below the relevant threshold; if it collides with a blocked slot, it may be throttled as a false positive. Existing blocked slot state is not cleared simply to admit the new key.

**Interview question:** Can an in-memory limiter be both strictly exact for arbitrary identities and strictly bounded in memory?

**Answer:** not for an unbounded identity space. Exact state needs storage proportional to distinct identities. A fixed-size hashed counter trades exactness for bounded memory and possible collision-based false positives.

## 4. Forgot-password persistence and timing

**Problem:** eligible-account reset requests synchronously look up the user, invalidate/insert database token rows, and register delivery, while unknown accounts return before those database writes. Deferring email delivery alone does not make these request paths equal in time.

**Implementation:** reset tokens remain generated securely, persisted only as hashes, expire, are consumed under a pessimistic lock, and revoke refresh sessions on successful reset. Delivery remains behind `PasswordResetTokenDelivery` and is scheduled after the token transaction commits. The HTTP response remains generic, and no provider is fabricated or claimed.

**Asynchronous is not durable:** `TaskExecutor` schedules delivery after commit but does not itself persist a job. A process crash, executor rejection, or shutdown can lose a scheduled delivery after the token row commits. A durable queue/outbox and real email provider are needed for delivery guarantees; they are outside this milestone.

**Timing limitation:** token persistence still occurs synchronously only for eligible accounts, so response latency may differ between eligible and unknown addresses. The code does not claim complete timing equalization. A durable asynchronous workflow can reduce request-path variation, but it must be designed to avoid account-dependent acceptance responses and still requires generic responses.

**Practical example:** both known and unknown addresses receive `202` with identical text; only an eligible account with a configured delivery port causes a hashed reset row and a post-commit delivery task.

**Interview question:** Does post-commit asynchronous email delivery eliminate forgot-password timing enumeration?

**Answer:** no. It removes provider I/O from the request path, but synchronous account lookup and database writes can still differ. The implementation documents that limitation rather than claiming equal timing.

## 5. Verification

Regression tests cover missing, invalid, and short JWT secrets; account-limit suppression with generic responses; no extra token row or delivery after suppression; independent IP 429 behavior; fixed limiter capacity, expiry, blocked-state retention, churn, new-client handling, and concurrent calls; and the existing authentication, refresh-token, logout, and reset-token flows.

These controls improve the existing single-instance application but do not make it production-ready by themselves. Production still needs secret-manager operations and rotation, schema migrations, a durable email delivery mechanism, and shared rate-limit state where multiple instances are deployed.


---------------------------------------------------------------------

# Phase 4.1 — OAuth2 Foundation and Architecture

## 1. Introduction to OAuth2

OAuth2 (Open Authorization 2.0) is an authorization framework that allows an application to access resources or establish a login flow through an external identity provider without directly collecting the user's provider password.

In our Task Management REST API, we want users to authenticate using:

- Google
- GitHub
- Existing email and password authentication

Instead of implementing authentication independently for each provider, we will integrate Google and GitHub through Spring Security OAuth2 Client.

### Why OAuth2?

Consider traditional authentication:

1. User enters email and password.
2. Backend validates the credentials.
3. Backend generates JWT access and refresh tokens.

With social authentication:

1. User selects Login with Google or GitHub.
2. The backend redirects the user to the provider.
3. The provider authenticates the user.
4. The provider sends the user back to our application.
5. Our backend validates the provider's response.
6. Our backend identifies or creates a local user.
7. Our backend generates its own JWT access and refresh tokens.

**Important:** Google and GitHub authenticate the user, but our application remains responsible for its own authorization and API authentication.

---

## 2. OAuth2 vs OpenID Connect (OIDC)

These two concepts are related but not identical.

### OAuth2

OAuth2 primarily handles delegated authorization.

It allows an application to obtain permission to access resources on behalf of a user.

Example:

A user authorizes an application to access their GitHub profile.

### OpenID Connect

OIDC is an identity layer built on top of OAuth2.

It provides standardized information about the authenticated user.

For example:

- User identifier
- Email
- Email verification status
- Name
- Profile information

Google supports OpenID Connect.

GitHub login through Spring Security generally uses OAuth2 user information rather than an OIDC ID token.

| Feature | OAuth2 | OpenID Connect |
|---|---|---|
| Primary purpose | Authorization | Authentication and identity |
| Access token | Yes | Yes |
| ID token | Not inherently | Yes |
| User identity standard | Not inherently | Yes |
| Example | GitHub OAuth login | Google OIDC login |

**Remember:** An OAuth2 access token should not automatically be treated as proof of user identity. Identity must be established using the provider's appropriate authentication mechanism.

---

## 3. OAuth2 Authorization Code Flow

We plan to use the Authorization Code Flow.

This is the standard browser-based authorization flow for our application.

### Complete flow

```text
User Browser
    |
    | 1. Click Login with Google
    v
Our Spring Boot Application
    |
    | 2. Generate authorization request
    |    Generate random state
    v
Google Authorization Server
    |
    | 3. User authenticates
    | 4. User grants requested permissions
    v
Google redirects browser to callback
    |
    | 5. Authorization code + state
    v
Our Spring Boot Application
    |
    | 6. Validate state
    | 7. Exchange authorization code
    v
Google Token Endpoint
    |
    | 8. Access token / ID token
    v
Our Application
    |
    | 9. Validate identity and load profile
    | 10. Resolve local user
    v
Local Database
    |
    | 11. Generate application JWT
    | 12. Generate refresh token
    v
Browser receives authentication response
```

### Step-by-step explanation

**Step 1: Authorization request**

The browser starts the OAuth2 login process.

For example:

```text
/oauth2/authorization/google
```

Spring Security identifies the Google registration and constructs an authorization request.

**Step 2: State generation**

A random state value is generated.

It helps protect against Cross-Site Request Forgery (CSRF) attacks during the OAuth2 authorization flow.

**Step 3: Provider authentication**

The browser is redirected to Google's authentication page.

The user signs in and approves the requested permissions.

**Step 4: Authorization callback**

Google redirects the browser to:

```text
/login/oauth2/code/google
```

The callback contains an authorization code and state.

**Step 5: State validation**

Spring Security compares the returned state with the original authorization request.

If the state does not match, the authentication process must fail.

**Step 6: Authorization code exchange**

The backend exchanges the authorization code for tokens through the provider's token endpoint.

**Step 7: User information retrieval**

The backend obtains the authenticated user's identity information.

**Step 8: Local authentication**

Our application resolves the provider identity to a local user and issues its own tokens.

---

## 4. OAuth2 Client in Spring Security

Spring Security provides OAuth2 Client support through:

```xml
spring-boot-starter-oauth2-client
```

This starter provides the infrastructure required for OAuth2 login.

Important Spring Security components:

| Component | Responsibility |
|---|---|
| ClientRegistration | Stores provider configuration |
| ClientRegistrationRepository | Provides configured client registrations |
| OAuth2AuthorizationRequest | Represents an authorization request |
| OAuth2UserService | Loads OAuth2 user information |
| OidcUserService | Loads OIDC user information |
| OAuth2AuthenticationToken | Represents OAuth2 authentication |
| OAuth2LoginAuthenticationFilter | Processes OAuth2 login callbacks |
| AuthenticationSuccessHandler | Handles successful authentication |
| AuthenticationFailureHandler | Handles failed authentication |

### ClientRegistration

A ClientRegistration represents one OAuth2 provider configuration.

For example:

- Google registration
- GitHub registration

It includes:

- Client ID
- Client secret
- Authorization URI
- Token URI
- User information endpoint
- Redirect URI
- Requested scopes
- Client authentication method

### Registration ID

We will use:

```text
google
github
```

Spring Security uses these registration IDs in authorization and callback URLs.

---

## 5. OAuth2 Configuration and Environment Variables

OAuth2 providers require application credentials.

These credentials are created when registering our application with Google and GitHub.

They include:

- Client ID
- Client Secret

These are different from our JWT signing secret.

| Credential | Purpose |
|---|---|
| Google Client ID | Identifies our application to Google |
| Google Client Secret | Authenticates our application to Google |
| GitHub Client ID | Identifies our application to GitHub |
| GitHub Client Secret | Authenticates our application to GitHub |
| JWT Secret | Signs our application's JWT access tokens |

Example environment variables:

```properties
GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID}
GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET}

GITHUB_CLIENT_ID=${GITHUB_CLIENT_ID}
GITHUB_CLIENT_SECRET=${GITHUB_CLIENT_SECRET}
```

The actual provider configuration will be externalized into Spring configuration.

**Security rules:**

- Never commit actual client secrets.
- Never print secrets in application logs.
- Use different credentials for development and production.
- Use HTTPS redirect URIs in production.
- Do not use production credentials in automated tests.

Provider enablement should be configurable so that local email/password authentication can continue working when social login is not configured.

---

## 6. Google OAuth2 and OIDC

Google supports OpenID Connect.

We can request scopes such as:

```text
openid
email
profile
```

These provide access to identity information.

Important Google identity claims:

| Claim | Meaning |
|---|---|
| sub | Stable Google account identifier |
| email | User email address |
| email_verified | Whether Google verifies the email |
| name | User's display name |
| picture | Profile picture URL |

### Why is `sub` important?

Suppose a Google user has:

```json
{
  "sub": "109876543210987654321",
  "email": "user@example.com",
  "email_verified": true,
  "name": "Example User"
}
```

The `sub` value is the provider's stable account identifier.

We should identify the social account using:

```text
provider = google
providerUserId = sub
```

Do not use email as the primary social identity key.

Email addresses can change. The provider's stable account identifier is a better identity reference.

### Email verification

Only accept an email as verified when the provider's trusted identity response indicates:

```text
email_verified = true
```

Do not assume every email returned by an OAuth2 provider is verified.

---

## 7. GitHub OAuth2

GitHub uses OAuth2 user information.

We can request scopes such as:

```text
read:user
user:email
```

These permissions allow the application to retrieve profile information and email information.

Important GitHub fields:

| Field | Meaning |
|---|---|
| id | Stable numeric GitHub account identifier |
| login | GitHub username |
| name | Display name |
| email | Public profile email, possibly null |
| avatar_url | Profile picture |

### Why can GitHub email be null?

A GitHub user can keep their email address private.

Therefore, the normal profile endpoint may return:

```json
{
  "id": 12345678,
  "login": "example-user",
  "email": null
}
```

The application may need to use GitHub's email endpoint with the appropriate scope.

The verified primary email should be selected from the returned email records.

Example:

```json
[
  {
    "email": "user@example.com",
    "primary": true,
    "verified": true
  }
]
```

We should not use an unverified email for account provisioning.

### GitHub identity

Use:

```text
provider = github
providerUserId = GitHub numeric id
```

Do not use the GitHub username as the permanent identity key.

---

## 8. OAuthAccount Entity

Our existing application has a `User` entity.

However, one local user may eventually authenticate through multiple identity providers.

For example:

```text
Local User
   |
   |---- Google Account
   |
   |---- GitHub Account
```

We should not put all provider-specific fields directly inside the User entity.

Instead, we propose a separate OAuthAccount entity.

### Proposed relationship

```text
             USER
              |
              | 1
              |
              | Many
          OAUTH_ACCOUNT
              |
              |-------------------
              |                  |
          Google Account    GitHub Account
```

### Proposed fields

| Field | Purpose |
|---|---|
| id | Primary key |
| user_id | Reference to local User |
| provider | GOOGLE or GITHUB |
| provider_user_id | Stable provider account ID |
| created_at | Account linking/creation time |

### Unique constraint

The database should enforce:

```text
UNIQUE(provider, provider_user_id)
```

This ensures that one provider identity cannot be associated with multiple local users.

An additional index on `user_id` supports retrieving all linked provider accounts for a local user.

### Why separate OAuthAccount from User?

Because the local user represents the application account, while OAuthAccount represents an external identity associated with that account.

This design also supports adding more providers in the future without continually changing the User entity.

---

## 9. Social Account Provisioning

When a user authenticates through Google or GitHub for the first time, our application needs to determine whether the provider identity already exists.

### Existing provider identity

```text
Google identity
      |
      v
Search OAuthAccount by provider + providerUserId
      |
      v
Identity found
      |
      v
Load associated local User
      |
      v
Check account enabled status
      |
      v
Issue application JWT and refresh token
```

### New provider identity

```text
Google identity
      |
      v
Search OAuthAccount
      |
      v
Identity not found
      |
      v
Validate provider email
      |
      v
Check local account conflicts
      |
      v
Create local User
      |
      v
Create OAuthAccount
      |
      v
Issue application JWT and refresh token
```

### Important account-linking rule

Never silently link an OAuth identity to an existing local account simply because the email addresses match.

A secure account-linking workflow should require:

- An already authenticated local user.
- Explicit user consent.
- Reauthentication where appropriate.
- Validation of the new provider identity.
- Protection against CSRF and identity substitution.

Account linking is different from first-time social account provisioning.

---

## 10. Handling OAuth-only users

Our existing User entity requires a non-null password hash.

An OAuth-only account does not have a password supplied to our application.

There are two possible designs:

**Option A: Unusable placeholder password hash**

Generate a high-entropy random value and store only its BCrypt hash.

The plaintext must never be exposed or logged.

This allows us to preserve the existing non-null database constraint, but we must ensure the account cannot use an assumed password.

**Option B: Nullable password hash**

Allow password_hash to be null for OAuth-only users.

This requires schema and validation changes.

Local password authentication must explicitly reject accounts without a password hash.

For the initial implementation, Option A is the less disruptive option, provided it is implemented carefully.

---

## 11. Refresh Token and JWT Integration

This is the most important architectural decision in Phase 4.

Our application already implements:

- JwtService
- RefreshTokenGenerator
- RefreshTokenLifecycleService
- LoginResponse

We should reuse these components.

We must not generate provider-specific JWTs.

### Local authentication

```text
Email + Password
      |
      v
AuthenticationManager
      |
      v
AuthService
      |
      v
RefreshTokenLifecycleService
      |
      v
JWT + Refresh Token
```

### Social authentication

```text
Google / GitHub
      |
      v
Spring Security OAuth2
      |
      v
OAuth Identity Service
      |
      v
Local User
      |
      v
RefreshTokenLifecycleService
      |
      v
JWT + Refresh Token
```

Both flows should eventually produce the same response.

Example:

```json
{
  "accessToken": "application-jwt",
  "refreshToken": "opaque-refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

The example values are illustrative, not real credentials.

### Why is this architecture better?

Because our APIs do not need to know whether the user logged in through:

- Email/password
- Google
- GitHub

They only need to validate our application's JWT.

This keeps authentication provider details separate from API authorization.

---

## 12. OAuth2 Success Handler

Spring Security provides an authentication success handler that runs after successful OAuth2 login.

We propose a custom handler.

Its responsibility will be:

1. Obtain the authenticated OAuth2 identity.
2. Resolve the corresponding local user.
3. Verify the local account is enabled.
4. Call the existing RefreshTokenLifecycleService.
5. Return the existing LoginResponse structure.
6. Set `Cache-Control: no-store`.
7. Avoid putting tokens into URLs.

Conceptually:

```text
OAuth2AuthenticationSuccessHandler
          |
          v
OAuth Identity Service
          |
          v
Local User
          |
          v
RefreshTokenLifecycleService
          |
          v
LoginResponse
```

### Authentication failure handler

A custom failure handler should:

- Return a generic failure response.
- Avoid exposing provider access tokens.
- Avoid returning raw provider attributes.
- Avoid revealing whether an account exists.
- Avoid logging secrets or authorization codes.

---

## 13. Stateless Authentication vs OAuth2 Temporary State

Our current API uses:

```java
SessionCreationPolicy.STATELESS
```

This means normal API authentication does not depend on a server-side HTTP session.

However, OAuth2 Authorization Code Flow requires temporary state between the authorization request and callback.

This is an important distinction.

### Normal API requests

```text
HTTP Request
     |
     v
Bearer JWT
     |
     v
JwtAuthenticationFilter
     |
     v
SecurityContext
```

The API uses JWT authentication.

### OAuth2 browser handshake

```text
Browser
   |
   v
OAuth2 Authorization Request
   |
   v
Temporary authorization request storage
   |
   v
Provider redirect
   |
   v
OAuth2 callback
   |
   v
Validate state
```

Spring Security can use:

```java
HttpSessionOAuth2AuthorizationRequestRepository
```

to preserve the temporary authorization request.

The temporary OAuth2 state is not the same thing as maintaining a permanent authenticated application session.

### Important cookie settings

For the temporary handshake cookie/session:

- HttpOnly should be enabled.
- Secure should be enabled under HTTPS.
- SameSite should be configured appropriately, commonly Lax for a top-level GET callback.
- Expiry should be limited.
- Temporary state should be cleaned up after the handshake.

The exact implementation must be tested with the actual browser redirect flow.

---

## 14. OAuth2 Security Considerations

Important security requirements:

### State validation

The returned state must match the original authorization request.

This protects the OAuth2 callback against CSRF.

### OIDC nonce

For Google OIDC, nonce validation helps bind the ID token to the original authentication request and prevents replay.

### Redirect URI validation

The redirect URI must match the registered provider configuration.

Use HTTPS in production.

### Client secret protection

Never expose provider client secrets to a browser or frontend.

### Access-token protection

Do not log provider access tokens.

Do not return provider tokens to the frontend unless there is a separately justified requirement.

### Local account enabled status

An existing disabled local account must not receive application tokens through OAuth2.

### Account linking

Never automatically attach an external identity using email alone.

### Token delivery

Never place application access or refresh tokens in:

- URL query parameters
- URL fragments
- Browser history
- Logs

Use an appropriately secured token-response or browser handoff design.

---

## 15. Testing Strategy

OAuth2 integration must be testable without depending on real Google or GitHub credentials.

### Provider identity tests

Google:

- Valid subject and verified email.
- Missing subject.
- Unverified email.
- Missing email.

GitHub:

- Stable numeric ID.
- Null public email.
- Verified primary email available.
- No verified primary email.

### Social account tests

- Existing Google identity.
- Existing GitHub identity.
- New social identity.
- Existing email conflict.
- Disabled local account.
- Duplicate provider identity.
- Concurrent first-time login.

### OAuth2 security tests

- Authorization initiation.
- Valid state.
- Invalid state.
- Missing state.
- Failed token exchange.
- Generic authentication failure.

### JWT integration tests

- Successful Google login produces application JWT.
- Successful GitHub login produces application JWT.
- Refresh tokens are stored hashed.
- Existing refresh-token rotation works.
- Disabled accounts cannot receive tokens.
- Both providers return the same LoginResponse structure.

---

## 16. Proposed Phase 4 Implementation Structure

The proposed structure is:

```text
com.taskmanager
|
|-- config
|   |-- SecurityConfig.java
|   |-- OAuth2ClientConfig.java
|   |-- OAuth2Properties.java
|
|-- controller
|   |-- AuthController.java
|
|-- entity
|   |-- User.java
|   |-- OAuthAccount.java
|
|-- repository
|   |-- UserRepository.java
|   |-- OAuthAccountRepository.java
|
|-- security
|   |-- JwtAuthenticationFilter.java
|   |-- OAuth2AuthenticationSuccessHandler.java
|   |-- OAuth2AuthenticationFailureHandler.java
|
|-- service
|   |-- AuthService.java
|   |-- JwtService.java
|   |-- RefreshTokenLifecycleService.java
|   |-- OAuth2UserProvisioningService.java
|
|-- dto
|   |-- auth
|       |-- LoginResponse.java
|
```

This is a proposed structure. Actual package names may be adjusted to match the existing codebase.

---

## 17. Important Interview Questions

**Q1. What is OAuth2?**

OAuth2 is an authorization framework that allows applications to access protected resources through delegated authorization.

**Q2. What is OpenID Connect?**

OIDC is an identity layer built on OAuth2 that provides standardized user authentication and identity claims.

**Q3. What is Authorization Code Flow?**

It is a flow where the application receives a short-lived authorization code and exchanges it for tokens through the provider's token endpoint.

**Q4. What is the state parameter?**

It is a random value used to correlate the authorization request and callback and protect against CSRF.

**Q5. Why should we not use email as the social account's primary identifier?**

Email addresses can change. Stable provider-specific identifiers such as Google's `sub` and GitHub's numeric ID are better identity keys.

**Q6. What is an OAuth2 Success Handler?**

It is a Spring Security component that handles the application-specific behavior after successful OAuth2 authentication.

**Q7. Why reuse our existing JwtService?**

It ensures that all authentication methods produce tokens accepted by the same application security infrastructure.

**Q8. Is JWT authentication stateless while OAuth2 login uses a session?**

They serve different purposes. JWT handles normal API authentication without a persistent server-side session, while OAuth2 may require temporary authorization-request storage during the browser handshake.

**Q9. What is the difference between access token and refresh token?**

An access token authenticates API requests. A refresh token is used to obtain a new access-token pair and is typically stored and rotated by the backend.

**Q10. Why must account linking be explicit?**

Automatically linking accounts based only on email can attach an external identity to an account without sufficient proof of ownership or user consent.

---

## 18. Phase 4.1 Learning Summary

At the end of the architecture-inspection milestone, the key concepts to understand are:

- OAuth2 fundamentals.
- OAuth2 vs OIDC.
- Authorization Code Flow.
- Authorization request and callback.
- State validation.
- OIDC nonce.
- Spring Security OAuth2 Client.
- ClientRegistration.
- OAuth2UserService and OidcUserService.
- Google OIDC identity.
- GitHub OAuth2 identity.
- Provider-specific stable user IDs.
- Verified email handling.
- OAuthAccount entity and relationship.
- Social user provisioning.
- Account linking.
- OAuth2 success and failure handlers.
- Stateless JWT authentication.
- Temporary OAuth2 authorization state.
- Refresh-token lifecycle reuse.
- OAuth2 testing with mocked providers.

**Implementation status:** These notes describe the proposed Phase 4 architecture and learning objectives. They do not mean Google/GitHub OAuth2 has already been implemented. Update this section as each implementation milestone is completed and verified.

---

**End of Phase 4.1 — OAuth2 Foundation and Architecture**

# Phase 4.2 — OAuth2 Dependencies and Provider Configuration

## Spring Security OAuth2 Client

The project now includes `spring-boot-starter-oauth2-client`, managed by the existing Spring Boot dependency management. Spring Security OAuth2 Client provides the authorization-code login filters and client registration model; it does not provision local users or issue this application's JWTs in this phase.

A `ClientRegistration` describes one OAuth client: its registration ID, client ID and secret, scopes, provider endpoints, and callback URI. `OAuth2ClientConfiguration` exposes a `ClientRegistrationRepository` containing only enabled registrations. If both providers are disabled, the repository is still available but contains no registrations, so OAuth endpoints cannot start a provider flow and ordinary local authentication remains available.

## Provider registrations and scopes

The Google registration uses Spring Security's built-in Google provider configuration with OIDC scopes `openid`, `profile`, and `email`. `openid` selects the OIDC flow and enables Spring's OIDC processing. The callback template defaults to `{baseUrl}/login/oauth2/code/google`.

The GitHub registration uses Spring Security's built-in GitHub endpoint configuration and requests `read:user` and `user:email`. Its callback template defaults to `{baseUrl}/login/oauth2/code/github`.

`{baseUrl}` is resolved by Spring for the request that starts authorization. Exact redirect URI values can be overridden with `GOOGLE_REDIRECT_URI` and `GITHUB_REDIRECT_URI`; wildcard redirect values are rejected. Development defaults to explicit localhost callback URLs, while production defaults to the request-derived `{baseUrl}` pattern, and either profile can override these settings.

## Client credentials and provider enablement

No client credential is committed. Development and production read:

```text
GOOGLE_OAUTH2_ENABLED
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
GITHUB_OAUTH2_ENABLED
GITHUB_CLIENT_ID
GITHUB_CLIENT_SECRET
```

Each provider is disabled by default. Missing credentials for a disabled provider do not prevent application startup. Enabling a provider without both a client ID and secret fails configuration clearly; the secret is not written to logs. Test properties explicitly disable providers, and OAuth tests use dummy credentials only in test code.

## Authorization request storage and state

Starting `/oauth2/authorization/google` or `/oauth2/authorization/github` creates an authorization request containing a random `state` value and redirects the browser to the configured provider. Spring stores that request in `HttpSessionOAuth2AuthorizationRequestRepository`. On callback to `/login/oauth2/code/{registrationId}`, Spring compares the returned state to the saved request before processing the authorization code. State validation remains enabled.

The API still configures `SessionCreationPolicy.STATELESS`: its JWT security context is not intended to become a persistent authenticated server-side session. OAuth authorization is a short browser round trip and needs temporary request state despite stateless API authentication. The existing global CSRF configuration remains unchanged; OAuth `state` validation is not disabled or replaced by CSRF configuration.

## Testing and milestone boundary

Configuration tests verify that disabled providers need no credentials, enabled Google and GitHub registrations have the expected scopes and callback endpoints, and enabled providers fail when credentials are missing. A MockMvc test starts Google authorization, checks the redirect and non-empty state, and verifies Spring saved the matching request in the temporary session. Tests do not call either provider or require real credentials; existing local-authentication integration tests remain in the suite.

This phase does **not** create OAuth-linked local users, an `OAuthAccount` table, account-linking behavior, GitHub verified-email retrieval, or an OAuth success handler that issues application access/refresh tokens. Spring's authorization infrastructure only is configured; the existing local login and JWT issuance path is unchanged. Those behaviors require later milestones.

**Interview question:** Why is a temporary HTTP session compatible with a stateless API?

**Answer:** the session stores only the short-lived authorization request needed to bind a callback to its initiating browser and validate `state`. It is distinct from persisting the API's authenticated security context, which continues to use JWTs.