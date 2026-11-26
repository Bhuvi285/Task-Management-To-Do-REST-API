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


----------------------------------------------------------------------------

# Phase 4.2 — OAuth2 Dependencies and Provider Configuration

## 1. What Is OAuth2?

OAuth2 (Open Authorization 2.0) is an authorization framework that allows an application to access resources or establish a login flow through an external identity provider without directly handling the provider's password.

In this project, we are integrating:

- Google
- GitHub

Instead of implementing Google's or GitHub's authentication mechanism ourselves, we delegate the external authentication process to those providers.

### Why do we need OAuth2?

Imagine a user wants to log in to our Task Management application using Google.

Without OAuth2:
- We would need to build and maintain our own Google authentication integration.
- We would need to handle provider communication and token validation ourselves.

With OAuth2:
- Google handles the user's authentication.
- Google redirects the user back to our application.
- Spring Security manages the OAuth2 authorization flow.
- Our application will eventually identify or create its corresponding local user.

**Important:** OAuth2 login does not replace our application's own JWT authentication. OAuth2 establishes the external identity; our application will later issue its own JWT and refresh token.

---

## 2. Spring Security OAuth2 Client

Spring Security provides OAuth2 Client support to implement OAuth2 login and authorization flows.

We added the following dependency:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

### What does this dependency provide?

It provides the Spring Security components required for:

- OAuth2 authorization requests.
- Redirecting users to external providers.
- Handling OAuth2 callbacks.
- Managing authorization requests and state.
- Exchanging authorization codes for provider tokens.
- Loading authenticated OAuth2 user information.
- Supporting OpenID Connect providers such as Google.

### Important Spring Security components

| Component | Responsibility |
|---|---|
| OAuth2LoginAuthenticationFilter | Processes OAuth2 login callbacks |
| OAuth2AuthorizationRequest | Represents the outgoing authorization request |
| ClientRegistration | Stores provider registration details |
| ClientRegistrationRepository | Provides configured provider registrations |
| OAuth2UserService | Loads OAuth2 user information |
| OAuth2AuthenticationToken | Represents an authenticated OAuth2 user |
| OAuth2AuthorizationRequestRepository | Stores and retrieves authorization requests |

These components are provided or configured through Spring Security's OAuth2 Client support.

---

## 3. OAuth2 Client Registration

A client registration represents our application's configuration for communicating with an OAuth2 provider.

For example, our application has two registrations:

- `google`
- `github`

Each registration contains information such as:

- Client ID
- Client secret
- Authorization URI
- Token URI
- User information endpoint
- Scopes
- Redirect URI

### Google registration

Google uses OpenID Connect (OIDC), which is an identity layer built on top of OAuth2.

Our Google registration requests these scopes:

```properties
openid
profile
email
```

Meaning:

| Scope | Purpose |
|---|---|
| openid | Enables OpenID Connect authentication |
| profile | Requests basic profile information |
| email | Requests the user's email address |

Google provides a stable identity claim called `sub` (subject).

The `sub` value identifies the Google account and is preferable to using email as the permanent external identity key.

### GitHub registration

GitHub OAuth2 registration requests:

```properties
read:user
user:email
```

| Scope | Purpose |
|---|---|
| read:user | Allows access to basic user profile information |
| user:email | Allows access to the user's email information |

GitHub provides a stable numeric user ID.

A GitHub user's public profile email may be unavailable. Therefore, obtaining a verified email may require querying GitHub's email endpoint using the granted permissions.

**Important:** Phase 4.2 configures the provider registrations. Mapping these provider identities to our local users belongs to Phase 4.3.

---

## 4. Client ID and Client Secret

When registering an application with Google or GitHub, the provider generates credentials.

### Client ID

The Client ID identifies our registered application to the provider.

### Client Secret

The Client Secret is a confidential credential used by our backend during OAuth2 communication.

It must never be exposed in:
- Frontend JavaScript.
- Public GitHub repositories.
- Committed configuration files.
- Application logs.

### Environment variables

Our project uses environment-backed configuration:

```properties
GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID}
GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET}

GITHUB_CLIENT_ID=${GITHUB_CLIENT_ID}
GITHUB_CLIENT_SECRET=${GITHUB_CLIENT_SECRET}
```

The exact provider configuration also includes registration IDs, scopes and redirect URI settings.

### Why environment variables?

Environment variables allow different environments to use different credentials without modifying application source code.

For example:

| Environment | Credentials |
|---|---|
| Local development | Development OAuth credentials |
| Testing | Mock or test-only credentials |
| Production | Production OAuth credentials |

Never commit real client secrets to GitHub.

---

## 5. Disabled-by-Default Provider Configuration

Our implementation keeps Google and GitHub providers disabled by default.

This means the application should still start when OAuth2 credentials have not been configured, provided both providers are disabled.

When a provider is explicitly enabled, its required credentials must be present and valid.

### Why is this useful?

Consider a developer who only wants to work on local email/password authentication.

They should not be forced to configure Google and GitHub credentials just to start the application.

However, if Google login is enabled without its required credentials, the application should fail clearly rather than silently using placeholder credentials.

This approach provides:

- Safer configuration.
- Easier local development.
- Explicit feature activation.
- Clear startup failures for invalid configuration.

**Configuration principle:** Disabled feature = no provider credentials required. Enabled feature = valid credentials required.

---

## 6. OAuth2 Redirect URI

The redirect URI is the endpoint to which the external provider sends the user after authorization.

Our application uses these callback patterns:

```text
/login/oauth2/code/google
/login/oauth2/code/github
```

The redirect URI follows this general structure:

```text
{baseUrl}/login/oauth2/code/{registrationId}
```

For example, during local development:

```text
http://localhost:8080/login/oauth2/code/google
```

For production, the base URL should use the application's HTTPS domain.

### Why must the redirect URI match?

The provider checks whether the callback URI matches the URI registered for the OAuth2 application.

This prevents authorization responses from being redirected to arbitrary destinations.

A redirect URI mismatch can result in an OAuth2 authorization error.

**Important:** The callback URI must be registered with the corresponding Google or GitHub developer application.

---

## 7. OAuth2 Authorization Code Flow

The Authorization Code Flow is the OAuth2 flow used by our application.

It is appropriate for server-side applications because the client secret and token exchange remain on the backend.

### Flow

```text
User
 |
 | Clicks Login with Google
 v
Our Spring Boot Application
 |
 | Redirects to Google authorization endpoint
 v
Google Login Page
 |
 | User authenticates and grants permissions
 v
Google
 |
 | Redirects back with authorization code and state
 v
Our OAuth2 Callback Endpoint
 |
 | Validates state
 | Exchanges authorization code for provider tokens
 v
Spring Security
 |
 | Processes provider authentication
 v
OAuth2 Authentication
 |
 v
Future Phase: Local User Mapping + JWT Issuance
```

### Step-by-step explanation

1. The user clicks the Google login link.
2. Spring Security identifies the `google` client registration.
3. Spring creates an OAuth2 authorization request.
4. Spring generates a `state` value.
5. The application stores the authorization request temporarily.
6. The browser redirects the user to Google.
7. The user authenticates with Google.
8. Google redirects the browser to our callback endpoint.
9. Spring retrieves the stored authorization request.
10. Spring validates the returned `state`.
11. Spring exchanges the authorization code for provider tokens.
12. Spring processes the provider authentication response.

At this point, Spring can establish an OAuth2 authentication result.

Our local user provisioning and application JWT issuance are intentionally deferred to later milestones.

---

## 8. What Is the OAuth2 State Parameter?

The `state` parameter is a random value generated by the application before redirecting the user to the external provider.

It connects the authorization response to the original authorization request.

### Example

When login starts:

```text
Our Application
    |
    | state=abc123
    v
Google
```

After authentication:

```text
Google
    |
    | code=xyz789
    | state=abc123
    v
Our Application
```

Spring checks whether the returned state matches the state associated with the original request.

If the state does not match, the authorization response must be rejected.

### Why is state important?

It protects against Cross-Site Request Forgery (CSRF) attacks during the OAuth2 authorization flow.

An attacker should not be able to trick a user's browser into completing an authorization response that the user did not initiate.

**Important distinction:**

- JWT authentication uses a signed token to authenticate API requests.
- OAuth2 state validates the authorization response against the original login request.

They solve different security problems.

---

## 9. Why Do We Need a Session for OAuth2?

Our application uses:

```java
SessionCreationPolicy.STATELESS
```

This means the REST API does not rely on an HTTP session to maintain authenticated user state.

However, OAuth2 authorization requires temporary information to survive the redirect to an external provider.

For this, our implementation uses:

```java
HttpSessionOAuth2AuthorizationRequestRepository
```

This repository stores the outgoing OAuth2 authorization request in an HTTP session.

### Why is temporary session storage necessary?

Consider this sequence:

1. Our application sends the browser to Google.
2. The browser leaves our application.
3. Google authenticates the user.
4. Google redirects the browser back to our application.
5. Our application must retrieve the original authorization request to validate `state`.

The original request must survive this external redirect.

An HTTP session provides a way to retain that temporary information.

### Does this mean our API is no longer stateless?

No. We need to distinguish between two different things:

**Temporary OAuth2 session state**
- Stores authorization request information.
- Exists for the OAuth2 redirect/callback process.
- Is not intended to maintain the user's authenticated REST API session.

**JWT-based API authentication**
- Uses a signed access token.
- Authenticates subsequent API requests.
- Does not require a server-side HTTP session.

Our architecture keeps JWT as the authentication mechanism for protected REST endpoints.

### Important security consideration

The OAuth2 temporary session must not accidentally become a persistent authenticated session for REST APIs.

We must also ensure that temporary authorization requests are cleaned up after use and that session cookies have appropriate security settings, especially in production.

---

## 10. Integrating OAuth2 with the Existing SecurityFilterChain

Our application already has a `SecurityConfig` that configures:

- CSRF behavior.
- Form login.
- HTTP Basic authentication.
- Session creation policy.
- Public endpoints.
- Protected endpoints.
- JWT authentication filter.
- REST authentication and authorization error handlers.

Phase 4.2 integrates OAuth2 into this existing configuration rather than replacing the JWT architecture.

### Existing authentication architecture

```text
Incoming HTTP Request
        |
        v
SecurityFilterChain
        |
        v
JwtAuthenticationFilter
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

### OAuth2 login architecture

```text
Browser
   |
   v
OAuth2 Authorization Initiation
   |
   v
External Provider
   |
   v
OAuth2 Callback
   |
   v
Spring Security OAuth2 Filters
   |
   v
OAuth2 Authentication Result
```

These are two related but distinct authentication paths.

### Important implementation boundary

In Phase 4.2, we have configured OAuth2 login infrastructure and authorization-request state handling.

We have not yet implemented:

- OAuthAccount persistence.
- Local user creation.
- Google/GitHub identity mapping.
- Account linking.
- Application JWT issuance after OAuth login.

Those responsibilities belong to subsequent milestones.

---

## 11. OAuth2 Success Handler — Why We Deferred It

Spring Security supports an `AuthenticationSuccessHandler`.

It runs after successful authentication.

Eventually, we will implement a custom success handler that:

1. Reads the authenticated Google/GitHub identity.
2. Finds or creates the corresponding local user.
3. Checks whether the local account is enabled.
4. Issues our application's JWT access token.
5. Issues an opaque refresh token.
6. Returns the same `LoginResponse` structure used by local login.

However, we intentionally did not implement this in Phase 4.2.

Why?

Because we first need a reliable local identity model and provider-to-user mapping.

Implementing JWT issuance before that mapping exists would mix responsibilities and make the architecture harder to maintain.

---

## 12. Testing OAuth2 Configuration

OAuth2 configuration should be tested without requiring real Google or GitHub credentials.

Our Phase 4.2 implementation includes configuration and MockMvc state-validation tests.

### What should these tests verify?

| Test | Expected result |
|---|---|
| Both providers disabled | Application starts |
| Local authentication | Existing behavior remains functional |
| Google enabled with credentials | Google registration is available |
| GitHub enabled with credentials | GitHub registration is available |
| Enabled provider missing credentials | Clear startup failure |
| OAuth2 authorization request | Correct authorization flow starts |
| Valid state | Callback state validation succeeds |
| Invalid or mismatched state | Callback is rejected |

### Why avoid real provider calls in automated tests?

Tests should be:
- Repeatable.
- Independent of internet availability.
- Independent of external provider uptime.
- Independent of real credentials.

Mock registrations and mock provider responses make automated tests more reliable.

---

## 13. Phase 4.2 Implementation Summary

| Area | Implementation |
|---|---|
| OAuth2 dependency | Spring Boot OAuth2 Client starter |
| Google | OIDC registration |
| GitHub | OAuth2 registration |
| Provider credentials | Environment variables |
| Provider activation | Disabled by default |
| Redirect URI | Configurable exact callback pattern |
| OAuth2 state | Session-backed authorization request |
| Security integration | Existing SecurityFilterChain |
| JWT authentication | Preserved |
| OAuth2 user provisioning | Deferred |
| OAuth2 JWT issuance | Deferred |

### Test result

The reported final Java 21 clean test run:

```text
Tests run: 84
Failures: 0
Errors: 0
Skipped: 0
```

This is the reported project-wide result for Phase 4.2.

---

## 14. Important Interview Questions

**Q1. What is OAuth2?**

OAuth2 is an authorization framework that enables applications to obtain limited access to resources through an authorization server.

**Q2. What is OpenID Connect?**

OIDC is an identity layer built on OAuth2. It adds authentication and standardized identity information through ID tokens and claims.

**Q3. What is the difference between OAuth2 and JWT?**

OAuth2 is an authorization framework. JWT is a token format. OAuth2 can use JWTs, but they are not the same thing.

**Q4. What is an authorization code?**

It is a short-lived code returned by the authorization server after user authorization. The backend exchanges it for tokens.

**Q5. What is the purpose of the state parameter?**

It associates the callback with the original authorization request and helps protect against CSRF attacks.

**Q6. Why do we use Client ID and Client Secret?**

They identify and authenticate our registered application during communication with the provider. The client secret must remain confidential.

**Q7. What is ClientRegistration?**

It represents the configuration of an OAuth2 client, including its credentials, provider endpoints, scopes and redirect URI.

**Q8. Why do we use `spring-boot-starter-oauth2-client`?**

It provides Spring Security's OAuth2 Client and login integration, including authorization requests, callbacks and provider authentication processing.

**Q9. Why does a stateless REST API need an HTTP session during OAuth2 login?**

The session can temporarily store the authorization request and state across the external provider redirect. It does not mean REST API authentication must become session-based.

**Q10. What is the difference between an access token from Google and our application's JWT?**

The Google access token is issued by Google for access to Google resources. Our application's JWT is issued by our backend to authenticate requests to our own APIs.

**Q11. Why should we not use email as the only external identity identifier?**

Email can change and may not be verified. Stable provider-specific identifiers, such as Google's `sub` and GitHub's numeric user ID, are better identity keys.

**Q12. Why should OAuth2 credentials be stored in environment variables?**

It keeps secrets outside source control and allows different credentials to be used across development, testing and production.

---

## 15. What Comes Next?

Phase 4.2 established the OAuth2 client configuration and authorization flow infrastructure.

The next milestone is:

**Phase 4.3 — Social Identity Mapping and OAuth Account Provisioning**

We will focus on:

1. Creating an `OAuthAccount` entity.
2. Mapping Google and GitHub identities to local users.
3. Handling verified email information safely.
4. Creating local users for first-time social logins.
5. Preventing duplicate provider identities.
6. Handling concurrent first-time logins.
7. Keeping local password authentication compatible with OAuth-only accounts.

The goal is to ensure that every authentication method ultimately identifies a user in our own application's database.

# Phase 4.3 — Social Identity Mapping and OAuth Account Provisioning

Phase 4.3 maps verified Google and GitHub identities to local `User` records. The provider identity and the application user are separate concepts: Google or GitHub authenticates the person, while the local user remains the identity used by this application's services and authorization.

## OAuthAccount and stable identifiers

`OAuthAccount` stores a mandatory, lazy `ManyToOne` relationship to `User`, the provider enum (`GOOGLE` or `GITHUB`), the stable provider user ID, and its creation time. The database enforces uniqueness across `(provider, provider_user_id)` and indexes `user_id`. A local user may have identities from multiple providers, while one provider identity cannot map to multiple local users.

Email is not an external identity key because it can change and is not always verified. Google identities use the OIDC `sub` claim. GitHub identities use the numeric profile `id`.

## Provider-specific identity loading

`GoogleOidcUserService` delegates token and user-information processing to Spring Security's `OidcUserService`; it does not implement ID-token validation. It requires a non-empty subject, an email, and `email_verified == true` before provisioning. The resulting local-user principal preserves the provider's `OidcUser` claims, ID token, user info, and authorities.

`GithubOAuth2UserService` delegates profile loading to `DefaultOAuth2UserService`, validates the numeric GitHub ID, and uses the authorized access token to call GitHub's `/user/emails` endpoint. It selects a non-empty verified email, preferring the verified primary address. The public profile email is not treated as verified. Provider access tokens and email data are not logged.

Spring's `OAuth2UserService` handles GitHub's OAuth2 user-info path; `OidcUserService` handles Google's OIDC path. The wrappers associate each provider principal with the corresponding local user while preserving the original provider attributes and authorities. Disabled local users are rejected.

## Provisioning, collisions, and OAuth-only users

`SocialAccountService` first looks up the provider and stable ID. A match returns its enabled local user. For a new identity, it requires a verified email, normalizes it to lowercase, and rejects an email already owned by a local account. It never silently links accounts based only on matching email; explicit account linking is outside this milestone.

The existing schema requires a non-null 60-character BCrypt password hash. To keep local authentication and the schema unchanged, new OAuth-only users receive a BCrypt hash of a fresh cryptographically random value. The raw value is neither persisted nor returned. This avoids a predictable shared placeholder and does not overwrite local account passwords.

Provisioning runs in a transaction and flushes the local user and OAuth account together. Database uniqueness constraints remain the final protection against races; a data-integrity conflict fails authentication with a generic error, and the failed transaction rolls back so it cannot leave an orphan user. The integration test exercises simultaneous first-time requests and verifies only one provider identity and one associated user remain.

## Testing and deployment notes

Automated tests use H2 and mocked provider principals and HTTP responses; they do not call Google or GitHub. Coverage includes entity/repository uniqueness, provider identity validation, verified-email selection, account collisions, disabled users, provisioning, and concurrent requests. Existing local authentication and JWT/refresh-token flows remain part of the full project test suite.

This phase does not issue application JWTs or refresh tokens after OAuth login. The existing default success handling remains; JWT integration is deferred to Phase 4.4. No account linking or provider-token revocation is implemented.

The `oauth_accounts` table is created by the existing development/test schema-generation setup. Production uses schema validation, so an appropriate database migration must create this table and its constraints before deploying this change. No migration tool was introduced.

## Important interview questions

**Why use Google `sub` and GitHub's numeric ID rather than email?** These identifiers are provider-specific stable identity keys; an email can change and may not be verified.

**Why require a verified email?** Provisioning or associating an account with an unverified address could allow an attacker to claim another person's local identity.

**Why not automatically link accounts with the same email?** Email equality alone is insufficient proof that both authentication methods belong to the same person. Linking needs an explicit, authenticated flow.

**How are duplicate first-time logins handled?** Application checks improve normal flow, but a database uniqueness constraint is the authoritative race guard. Provisioning is transactional, so a failed insert rolls back the user created in that transaction.

**Why does an OAuth-only user still have a password hash?** The existing schema requires one. A cryptographically random value is hashed with BCrypt and discarded, preserving the existing local password model without introducing a known password.

**Why use both `OAuth2UserService` and `OidcUserService`?** GitHub's user profile is loaded through OAuth2 user-info, while Google is an OIDC provider with ID-token claims and a distinct principal type. Delegating to Spring's corresponding services preserves the framework's protocol handling.


---------------------------------------------------------------------------------------------------------------

# Phase 4.3 — Social Identity Mapping and OAuth Account Provisioning

## 1. What Did We Build in This Phase?

In Phase 4.2, we configured Google and GitHub OAuth2 clients and established the authorization-code flow.

However, a successful Google or GitHub authentication does not automatically mean that our application knows which local user is logging in.

Our application needs its own user identity.

For example:

- Google authenticates a person.
- GitHub authenticates a person.
- Our Task Management API needs to identify that person as a record in its own `users` table.

Phase 4.3 solves this problem.

We implemented:

- `OAuthAccount` entity.
- `OAuthAccountRepository`.
- Google OIDC identity extraction.
- GitHub OAuth2 identity extraction.
- Verified email handling.
- Local user provisioning.
- Existing social identity lookup.
- Duplicate identity protection.
- OAuth2 principal wrappers.
- Concurrent provisioning protection.

**Important:** This phase maps external identities to local users. Issuing our own JWT after social login is intentionally deferred to Phase 4.4.

---

## 2. External Identity vs Local Application User

These are two different concepts.

### External identity

An external identity belongs to an identity provider, such as Google or GitHub.

Examples:

- A Google account identified by Google's `sub` claim.
- A GitHub account identified by its numeric user ID.

### Local application user

A local user is a record in our own application's database.

Our `User` entity stores application-level information such as:

- User ID.
- Name.
- Email.
- Password hash.
- Account enabled status.
- Creation timestamp.

### Why do we need both?

Suppose a user logs in using Google today and GitHub tomorrow.

If both accounts are intentionally associated with the same local application user in the future, that user should still have one local identity and one set of tasks.

The external provider identifies the person during authentication. Our database identifies the person inside our application.

---

## 3. What Is an OAuthAccount Entity?

We created a separate entity named `OAuthAccount`.

It represents the relationship between an external provider identity and a local application user.

Conceptually:

```text
User
 |
 | One local user can have multiple external identities
 |
 +---- OAuthAccount (Google)
 |
 +---- OAuthAccount (GitHub)
```

### Main fields

| Field | Purpose |
|---|---|
| id | Primary key |
| user | Associated local User |
| provider | Google or GitHub |
| providerUserId | Stable identity assigned by provider |
| createdAt | When the identity was linked/provisioned |

### Why not store Google and GitHub IDs directly in User?

Because a separate entity is more extensible.

If we store provider-specific fields directly inside `User`, every new identity provider requires additional columns.

With `OAuthAccount`, we can support additional providers without continuously modifying the User entity.

For example, future providers could include Microsoft or LinkedIn.

---

## 4. OAuthProvider Enum

We use an enum to represent supported providers.

Conceptually:

```java
public enum OAuthProvider {
    GOOGLE,
    GITHUB
}
```

### Why use an enum?

An enum restricts the provider values to known options.

It avoids arbitrary strings such as:

```text
google
Google
GOOGLE
git-hub
GitHub
```

For database persistence, we use:

```java
@Enumerated(EnumType.STRING)
```

This stores the enum name rather than its numeric ordinal.

String persistence is safer when enum declaration order changes.

---

## 5. Database Relationship and Constraints

The `OAuthAccount` entity has a mandatory `ManyToOne` relationship with `User`.

Conceptually:

```text
users
----------------
id (PK)
name
email
password_hash
...

oauth_accounts
----------------
id (PK)
user_id (FK)
provider
provider_user_id
created_at
```

### Why ManyToOne?

One local user may eventually have multiple external identities.

However, each OAuthAccount belongs to exactly one local user.

### Unique constraint

We enforce uniqueness on:

```text
(provider, provider_user_id)
```

This means:

- A Google identity cannot be registered twice.
- A GitHub identity cannot be registered twice.
- The same provider identity cannot belong to two different local users.

### Why is a database constraint necessary?

An application-level check alone is insufficient.

Consider two requests:

1. Request A checks whether an identity exists.
2. Request B checks whether the same identity exists.
3. Both find no existing record.
4. Both try to insert the same identity.

Without a database constraint, duplicate records could be created.

The unique constraint makes the database the final authority for identity uniqueness.

We also added an index on `user_id` to support lookups of external identities associated with a local user.

---

## 6. Stable Provider Identifiers

We must distinguish a provider's stable account identifier from its email address.

An email address can change. A stable provider identifier is designed to identify the provider account.

### Google — `sub`

Google uses OpenID Connect.

Its `sub` claim represents the subject identifier of the authenticated user.

Example:

```json
{
  "sub": "google-account-unique-id",
  "email": "user@example.com",
  "email_verified": true,
  "name": "Example User"
}
```

We use `sub` as the Google `providerUserId`.

### GitHub — numeric `id`

GitHub exposes a numeric user ID in its user profile.

Example:

```json
{
  "id": 12345678,
  "login": "example-user",
  "name": "Example User",
  "email": null
}
```

We use the GitHub numeric ID as the provider identity.

### Why not use email as providerUserId?

Because:

- Users can change email addresses.
- Some provider profiles do not expose email.
- Email alone is not a reliable permanent identity key.
- Email verification status must also be considered.

The correct identity key is the provider plus its stable account identifier.

---

## 7. Google OIDC Identity Extraction

Google supports OpenID Connect.

Our implementation uses Spring Security's OIDC support rather than manually implementing ID-token signature validation.

The important Google claims are:

| Claim | Meaning |
|---|---|
| sub | Stable Google account identifier |
| email | User's email |
| email_verified | Whether Google considers the email verified |
| name | User's display name |
| given_name | First/given name |
| family_name | Last/family name |
| picture | Profile image URL |

### Why check `email_verified`?

A Google profile email should not automatically be trusted without checking its verification status.

Our provisioning process requires:

- A valid subject identifier.
- A non-empty email.
- A verified email.

If the email is not verified, the application must reject provisioning.

### Why use Spring's OIDC support?

Spring Security handles the OIDC protocol responsibilities, including processing and validating the provider's ID token.

We should not implement cryptographic ID-token validation ourselves when the framework already provides it.

---

## 8. GitHub OAuth2 Identity Extraction

GitHub uses OAuth2 for this integration.

The important profile fields include:

- `id`
- `login`
- `name`
- `email`

One important difference from Google is that GitHub's public profile email may be `null`.

For example:

```json
{
  "id": 12345678,
  "login": "example-user",
  "name": "Example User",
  "email": null
}
```

### How do we retrieve a verified GitHub email?

Our implementation includes:

- `GithubEmailClient`
- `GithubEmailClientImpl`
- `GithubOAuth2UserService`

When the public profile does not provide a suitable email, the backend uses the authenticated provider access token to retrieve email information from GitHub's email endpoint.

The application selects a verified email, preferably the verified primary email.

### Why is this necessary?

A user may keep their GitHub email private.

If we relied only on the public profile, social registration could fail even when the user has a verified email available through GitHub's API.

### Security requirements

- Do not trust an unverified email.
- Do not log the provider access token.
- Do not expose the provider access token to the frontend.
- Handle unavailable or missing verified emails safely.

---

## 9. OAuth2UserService and OidcUserService

Spring Security provides different user-loading mechanisms for OAuth2 and OpenID Connect.

### OAuth2UserService

`OAuth2UserService` loads user attributes from an OAuth2 provider.

GitHub uses this path in our implementation.

Our project includes:

```text
GithubOAuth2UserService
```

It loads the GitHub profile, obtains a verified email when necessary, and uses the social provisioning service to resolve the local user.

### OidcUserService

`OidcUserService` handles OpenID Connect user information.

Google uses this path in our implementation.

Our project includes:

```text
GoogleOidcUserService
```

It delegates the OIDC processing to Spring's standard implementation and uses the validated identity claims to resolve the local user.

### Difference

| OAuth2UserService | OidcUserService |
|---|---|
| Used for OAuth2 user loading | Used for OpenID Connect user loading |
| Loads OAuth2 user attributes | Processes OIDC identity information |
| Used by GitHub integration | Used by Google integration |

OIDC builds on OAuth2 and adds standardized authentication and identity information.

---

## 10. SocialAccountService — Central Provisioning Logic

We created a dedicated service named:

```text
SocialAccountService
```

It is responsible for mapping external identities to local users.

This keeps business logic out of controllers and Spring Security configuration.

### Existing identity flow

When a user logs in using Google or GitHub:

1. Spring Security authenticates the external identity.
2. The provider-specific service extracts the identity.
3. The `SocialAccountService` searches for an existing OAuthAccount.
4. If found, it retrieves the associated local user.
5. It verifies that the local user is enabled.
6. It returns the existing local user.

### First-time login flow

If the identity does not exist:

1. Validate the provider identity.
2. Validate the email.
3. Check for an existing local email.
4. Reject an unsafe email collision.
5. Create a new local user.
6. Create the associated OAuthAccount.
7. Persist both consistently.
8. Return the local user.

### Why a dedicated service?

It separates responsibilities:

- Provider-specific services extract identity information.
- `SocialAccountService` handles local provisioning.
- Repositories handle persistence.
- Security configuration connects these components.

This makes the design easier to test and maintain.

---

## 11. Email Collision and Automatic Account Linking

This is one of the most important security decisions in this phase.

Imagine:

1. A user registers using email/password.
2. Their local account already exists.
3. Later, someone attempts Google login with the same email.
4. No Google OAuthAccount exists for that Google identity.

Should the application automatically link Google to the existing account?

**No.**

Matching email addresses alone are not sufficient authorization to link accounts.

Our implementation rejects automatic linking and keeps the existing local account unchanged.

### Why is automatic linking dangerous?

If an application trusts an email without carefully verifying ownership and linking authorization, an attacker may gain access to an existing account through an incorrectly trusted external identity.

### Correct approach

Account linking should be a separate authenticated operation.

A future account-linking flow should require:

- An already authenticated local user.
- Appropriate reauthentication or confirmation.
- A protected linking operation.
- Verification that the external identity is not already linked elsewhere.

Explicit account linking is not implemented in Phase 4.3.

---

## 12. OAuth-Only Users and Password Hashing

Our existing `User` entity requires a non-null password hash.

But users who register through Google or GitHub do not provide a local application password.

We need a safe way to represent these users without breaking the existing database schema.

### Our implementation

For OAuth-only users:

1. Generate a cryptographically random password value.
2. Encode it using BCrypt.
3. Store only the resulting BCrypt hash.
4. Never expose or return the raw generated password.

### Why use SecureRandom?

`SecureRandom` generates cryptographically strong random values.

A predictable value such as:

```text
google123
defaultPassword
oauthUser
```

would be unsafe.

### Why BCrypt?

BCrypt is a password-hashing algorithm.

It is intentionally computationally expensive and uses a salt.

Our local authentication system already uses BCrypt, so the approach remains compatible with the existing password model.

### Important distinction

An OAuth-only account must not be able to log in using a known default password.

The generated value is never disclosed, and the user must use the external provider unless a separate, secure local-password setup process is introduced.

---

## 13. Transaction Management During Provisioning

Creating a local user and its OAuthAccount must be treated as one logical operation.

Consider:

```text
Create User
    |
    v
Create OAuthAccount
    |
    v
Save OAuthAccount
```

What happens if user creation succeeds but OAuthAccount creation fails?

We could end up with an orphaned local user.

### Why use `@Transactional`?

A transaction ensures that related database operations succeed or roll back together.

Conceptually:

```java
@Transactional
public User provisionSocialUser(...) {
    // Create local user
    // Create OAuthAccount
    // Persist both
}
```

If the operation fails and the transaction rolls back, the database should not retain only half of the provisioning operation.

### Important transaction principle

A transaction provides atomicity for the database work participating in that transaction.

It does not automatically make external HTTP calls transactional.

For example, retrieving a GitHub email is an external network operation, not part of the database transaction's atomic commit.

Keep external calls and database transaction boundaries thoughtfully separated.

---

## 14. Concurrent First-Time Login

Concurrency is a major consideration.

Imagine two requests arrive at almost the same time for the same Google account.

```text
Request A                 Request B
    |                         |
    v                         v
Check OAuthAccount        Check OAuthAccount
    |                         |
    v                         v
Not found                 Not found
    |                         |
    v                         v
Create User               Create User
    |                         |
    v                         v
Insert OAuthAccount       Insert OAuthAccount
```

Both requests may initially observe that no account exists.

This is called a race condition.

### How did we protect against it?

Our implementation uses:

- Database unique constraint on `(provider, provider_user_id)`.
- Transactional provisioning.
- Safe handling of persistence conflicts.
- Rollback of failed provisioning.

The database unique constraint is the final protection against duplicate provider identities.

### Why is an existence check insufficient?

Because two transactions can both pass the existence check before either inserts its record.

The database constraint prevents both inserts from succeeding.

### Important principle

Application-level validation improves normal behavior, but database constraints protect the final integrity of the data.

---

## 15. OAuth2 Principal Wrappers

Spring Security represents an authenticated OAuth2 user through security principal objects.

Our implementation introduces local-user-aware principal wrappers:

- `LocalUserOAuth2Principal`
- `LocalOAuth2User`
- `LocalOidcUser`

### Why do we need them?

After provider authentication, we want the authenticated principal to carry the resolved local application user.

Conceptually:

```text
Google / GitHub Authentication
             |
             v
Provider Principal
             |
             v
SocialAccountService
             |
             v
Local User
             |
             v
Local OAuth2 Principal Wrapper
```

This gives later security components access to the local application user rather than forcing every component to resolve the external identity again.

### Why separate OAuth2 and OIDC wrappers?

GitHub follows the OAuth2 user model, while Google follows the OIDC user model.

The wrappers preserve the appropriate Spring Security interfaces and their respective identity information.

---

## 16. Disabled Account Handling

Our application already supports enabled/disabled local users.

A user should not be able to bypass this rule by switching authentication methods.

For example:

- A local user is disabled.
- The user tries email/password login.
- The user should be rejected.
- The same user tries Google login.
- The user should still be rejected.

Our social provisioning flow checks the local account's enabled status before returning it as an authenticated application user.

**Security principle:** All authentication methods must respect the same local account status.

---

## 17. Testing Strategy

The Phase 4.3 implementation added tests for:

- OAuthAccount repository persistence and constraints.
- Social account provisioning.
- Google identity mapping.
- GitHub identity mapping.
- GitHub email endpoint behavior.
- Concurrent provisioning.

### Why mock GitHub's email endpoint?

Automated tests should not depend on real provider servers or credentials.

A mocked HTTP response allows us to test cases such as:

- A verified primary email exists.
- The public email is unavailable.
- No verified email is returned.
- The endpoint fails.

### Why test concurrent provisioning?

A normal unit test cannot prove that the database constraint works under concurrent requests.

Concurrency integration coverage helps verify that simultaneous first-time login attempts cannot create duplicate provider identities or leave orphaned users.

### Reported test result

```text
Java version: 21
Tests run: 98
Failures: 0
Errors: 0
Skipped: 0
```

This is the reported final clean test result for Phase 4.3.

---

## 18. Files Created and Modified

### Created production files

| File | Responsibility |
|---|---|
| OAuthProvider | Supported provider enum |
| OAuthAccount | External identity entity |
| OAuthAccountRepository | External identity persistence |
| SocialIdentity | Normalized external identity data |
| SocialAccountService | Local user provisioning |
| LocalUserOAuth2Principal | Local-user-aware principal contract |
| LocalOAuth2User | OAuth2 principal wrapper |
| LocalOidcUser | OIDC principal wrapper |
| GithubEmailClient | GitHub email client abstraction |
| GithubEmailClientImpl | GitHub email retrieval |
| GithubOAuth2UserService | GitHub identity loading |
| GoogleOidcUserService | Google identity loading |

### Modified files

- `UserRepository`
- `SecurityConfig`
- `learning_concepts.md`

The OAuth2 Client dependency was already added in Phase 4.2.

---

## 19. What We Have Not Implemented Yet

Phase 4.3 deliberately stops before issuing application tokens after social login.

Still pending:

- Custom OAuth2 authentication success handler.
- Issuing our own JWT access token after Google login.
- Issuing our own JWT access token after GitHub login.
- Refresh-token issuance after social login.
- Returning the same `LoginResponse` structure as local login.
- Social login integration testing for the complete token response.

These belong to Phase 4.4.

Account linking and unlinking are also deferred.

---

## 20. Interview Questions

**Q1. What is the difference between an OAuth identity and a local user?**

An OAuth identity belongs to an external provider. A local user is the application's own database record. Multiple external identities may eventually be associated with one local user.

**Q2. Why do we need an OAuthAccount entity?**

It stores the mapping between an external provider identity and a local application user without adding provider-specific columns to the User entity.

**Q3. Why use Google's `sub` claim?**

It is Google's stable subject identifier and is a better identity key than email.

**Q4. Why use GitHub's numeric ID?**

It uniquely identifies the GitHub account independently of its username or email address.

**Q5. Why is email verification important?**

It helps establish that the provider has verified ownership of the email address before we use it for local account provisioning.

**Q6. Why should we not automatically link accounts using email?**

Matching email alone does not establish that an external identity is authorized to access an existing local account.

**Q7. What is the purpose of a database unique constraint?**

It guarantees identity uniqueness even when concurrent requests bypass application-level existence checks.

**Q8. Why use `@Transactional` during social provisioning?**

It ensures that local user creation and OAuthAccount persistence succeed or roll back together.

**Q9. Why does GitHub require special email handling?**

GitHub's public profile email may be unavailable. We may need to retrieve verified email information through its authenticated email endpoint.

**Q10. Why use `SecureRandom` for OAuth-only password values?**

It generates unpredictable values, preventing OAuth-only accounts from receiving predictable placeholder passwords.

**Q11. Why do we need OAuth2UserService and OidcUserService?**

They are Spring Security extension points for loading authenticated OAuth2 and OIDC user information.

**Q12. Why do we need local-user-aware principal wrappers?**

They allow the authenticated Spring Security principal to carry the resolved local user, making later application authentication and authorization integration easier.

**Q13. Why is concurrency important during first-time social login?**

Two simultaneous requests may both observe that an external identity does not exist. Database uniqueness and transactional conflict handling prevent duplicate provisioning.

**Q14. Does successful Google authentication automatically issue our application's JWT?**

No. Provider authentication and application-token issuance are separate steps. Our custom JWT issuance is implemented in Phase 4.4.

---

## 21. Phase 4.3 Completion Summary

Phase 4.3 established the bridge between external provider identities and our application's local user model.

The architecture now supports:

```text
Google Login ─────┐
                  |
                  v
             Provider Identity
                  |
GitHub Login ─────┘
                  |
                  v
          Identity Extraction
                  |
                  v
          SocialAccountService
                  |
                  v
            Local User
                  |
                  v
        OAuth2/OIDC Principal
```

The next milestone connects this authenticated local user to our existing JWT and refresh-token authentication system.

**Next: Phase 4.4 — OAuth2 Success Handler and JWT Integration.**

# Phase 4.4 — OAuth2 Success Handler and JWT Integration

Phase 4.4 connects successful Google and GitHub authentication to the application's existing access-token and refresh-token contract. Provider authentication establishes who authenticated; the local `User` remains the identity used by the Task Management API.

## OAuth2 authentication success

Spring Security invokes `OAuth2AuthenticationSuccessHandler` only after the configured OAuth2/OIDC authentication flow succeeds. The handler extracts the local user through `LocalUserOAuth2Principal`, an abstraction implemented by both `LocalOAuth2User` and `LocalOidcUser`. It does not inspect provider-specific claim maps or use a provider token as the application's credential.

Google's OIDC and GitHub's OAuth2 paths therefore converge on the same handler and token lifecycle. The success handler rejects a missing, invalid, or disabled local user. `RefreshTokenLifecycleService.issueForUser` also checks the persisted account's enabled status before issuing tokens, protecting all callers against a disabled account and status changes between authentication and issuance.

## Reusing the application token lifecycle

The handler calls the existing `RefreshTokenLifecycleService.issueForUser(...)`. This service uses the existing `JwtService` to create the application's HS256 access token with its configured issuer, subject, and expiration, and uses `RefreshTokenGenerator` for a fresh opaque refresh token. Only the refresh-token hash is persisted; expiry and token-family behavior remain unchanged. Each successful login creates a distinct refresh session, while rotation and revocation continue to use the existing lifecycle.

Local email/password login already uses the same service, so both flows return the existing `LoginResponse` record with `accessToken`, `refreshToken`, `tokenType`, and `expiresIn`. Provider access tokens and ID tokens are never returned in that response.

## REST response and failure handling

On success, the callback returns HTTP 200 with `application/json`, `Cache-Control: no-store`, and `Pragma: no-cache`. Tokens are returned only in the response body; the success handler does not redirect with credentials in a URL or introduce cookies.

`OAuth2AuthenticationFailureHandler` returns a generic HTTP 401 JSON error for provider authentication and callback failures. It does not serialize provider exception details and adds the same cache-prevention headers. In particular, OAuth2 state validation remains enabled: the existing session-backed authorization-request repository checks the callback state, and an invalid state reaches the generic failure response without issuing tokens.

The API remains stateless for subsequent requests. The temporary session is used only to hold OAuth2 authorization state across the provider redirect; API requests continue to authenticate with `Authorization: Bearer <application-access-token>`.

## Automated testing

Handler unit tests cover both local principal wrappers, response shape and headers, provider-token exclusion, disabled users, and unsupported principals. Integration tests exercise the real JWT and refresh-token lifecycle with H2, including hashed refresh-token persistence, independent Google/GitHub sessions, rotation, revocation, and bearer-token acceptance by the security filter. Provider HTTP calls are not made by these tests. The authorization-flow test verifies a mismatched OAuth2 state fails with the generic response and does not redirect with tokens.

## Security considerations

- Only a principal implementing the local-user abstraction can receive application tokens.
- The stored account is rechecked as enabled at token issuance time.
- JWT signing configuration and refresh-token entropy, hashing, expiry, rotation, reuse detection, and revocation are reused rather than reimplemented.
- OAuth responses are non-cacheable and contain no provider credentials.
- Provider failures and invalid state fail closed with no token issuance.

## Important interview questions

**Why use an `AuthenticationSuccessHandler`?** It is Spring Security's extension point for behavior after authentication succeeds; it keeps token response logic out of controllers and configuration.

**Why extract the local user instead of reading provider claims again?** Phase 4.3 already validated and mapped provider identity to a local account. The principal abstraction carries that resolved identity consistently for both OIDC and OAuth2 providers.

**Why should a provider access token not be used as the API JWT?** It is issued for the provider's resources and has a different issuer, audience, and trust boundary from the application's API token.

**How do local and social authentication keep a consistent response?** Both invoke `RefreshTokenLifecycleService`, which produces the same `LoginResponse` and uses the existing JWT and refresh-token implementations.

**Why does a stateless API still use temporary session state during OAuth2 login?** The authorization request and `state` must survive the browser redirect and be validated on callback. That temporary flow state does not make subsequent API authentication session-based.

**How are OAuth2 failure details kept private?** A dedicated failure handler returns a fixed generic message and never serializes provider exceptions or tokens.

------------------------------------------------------------------------

# Phase 4.4 — OAuth2 Success Handler and JWT Integration

## 1. What Did We Build in This Phase?

In Phase 4.3, we implemented Google and GitHub identity mapping and local user provisioning.

However, after successful social authentication, our application still needed to issue its own authentication tokens.

Phase 4.4 completes this integration.

We implemented:

- `OAuth2AuthenticationSuccessHandler`
- Generic OAuth2 JSON failure handling
- JWT issuance after Google login
- JWT issuance after GitHub login
- Refresh-token issuance through the existing lifecycle service
- Consistent `LoginResponse`
- Non-cacheable JSON authentication responses
- Enabled-user validation in the refresh-token issuance path
- Integration tests for the complete social-login token flow

**Main objective:** Local login and social login must use the same application-token issuance architecture.

---

## 2. Authentication vs Token Issuance

Authentication and token issuance are related, but they are not the same operation.

### Authentication

Authentication establishes who the user is.

For local login:

- The user provides email and password.
- Spring Security validates the credentials.
- The application identifies the local user.

For social login:

- Google or GitHub authenticates the user.
- Spring Security processes the OAuth2/OIDC response.
- Our application maps the provider identity to a local user.

### Token issuance

After authentication, our application generates its own tokens.

These tokens are used to authenticate subsequent requests to our Task Management API.

### Important distinction

Google's access token is not our application's access token.

GitHub's access token is not our application's access token.

Our backend generates its own JWT after successfully resolving the local application user.

---

## 3. Why Do We Need an AuthenticationSuccessHandler?

Spring Security provides an extension point called:

```java
AuthenticationSuccessHandler
```

It is invoked after successful authentication.

It allows an application to define what should happen after authentication succeeds.

For a traditional web application, this may involve redirecting the user to a dashboard.

For our REST API, we need to return a JSON response containing our application's authentication tokens.

We therefore implemented:

```text
OAuth2AuthenticationSuccessHandler
```

### Responsibilities

1. Receive the successful OAuth2/OIDC authentication.
2. Extract the local user from the custom principal.
3. Validate the local user.
4. Issue an application JWT.
5. Issue an opaque refresh token.
6. Construct the existing `LoginResponse`.
7. Serialize the response as JSON.
8. Return HTTP 200 with appropriate security headers.

### Why use a separate handler?

It keeps OAuth2 post-authentication behavior separate from:

- Provider identity extraction.
- Local user provisioning.
- Security configuration.
- JWT implementation.
- Refresh-token persistence.

This separation makes the implementation easier to maintain and test.

---

## 4. Understanding the OAuth2 Authentication Lifecycle

Our social-login flow now looks like this:

```text
User
 |
 v
Google / GitHub Login
 |
 v
External Provider Authentication
 |
 v
Spring Security OAuth2/OIDC Filters
 |
 v
GoogleOidcUserService / GithubOAuth2UserService
 |
 v
SocialAccountService
 |
 v
Local Application User
 |
 v
Local OAuth2/OIDC Principal
 |
 v
OAuth2AuthenticationSuccessHandler
 |
 v
Application JWT + Refresh Token
 |
 v
JSON LoginResponse
```

### Explanation

1. The user starts Google or GitHub login.
2. The external provider authenticates the user.
3. Spring Security processes the OAuth2/OIDC callback.
4. The provider-specific user service loads the external identity.
5. `SocialAccountService` resolves the local user.
6. The local-user-aware principal is returned to Spring Security.
7. The authentication success handler receives the authenticated principal.
8. The handler extracts the local user.
9. The existing token issuance services generate our application's tokens.
10. The response is returned to the client.

The important part is that both providers eventually reach the same token-issuance implementation.

---

## 5. Local Application User vs Provider Principal

Spring Security represents an authenticated user through an `Authentication` object.

The `Authentication` object contains a principal.

For social login, the principal initially represents the external provider identity.

However, our application needs the corresponding local user.

In Phase 4.3, we created local-user-aware principal wrappers:

- `LocalUserOAuth2Principal`
- `LocalOAuth2User`
- `LocalOidcUser`

### Why are these wrappers useful?

They allow our authentication success handler to retrieve the local application user from the authenticated principal.

Conceptually:

```text
Authentication
      |
      v
Principal
      |
      v
Local OAuth2/OIDC Principal
      |
      v
Local User
```

This avoids repeating provider identity lookup inside the success handler.

### Important principle

The success handler must not trust arbitrary request parameters or unvalidated provider attributes.

It should use the principal produced by the successfully completed Spring Security authentication flow.

---

## 6. Reusing JwtService

Our project already has a dedicated `JwtService`.

It is responsible for generating and validating our application's JWTs.

We must reuse it for social login rather than implementing another JWT generator.

### Why?

Suppose local login and Google login generate tokens differently.

They might accidentally use different:

- Signing algorithms.
- Secret keys.
- Expiration values.
- Subject conventions.
- Claims.
- Validation rules.

That would make authentication inconsistent.

By reusing the same `JwtService`, both login methods follow the same token-generation rules.

### Conceptual flow

```text
Local Login
     |
     v
Authenticated Local User
     |
     v
JwtService
     |
     v
Application JWT
```

```text
Google / GitHub Login
     |
     v
Resolved Local User
     |
     v
JwtService
     |
     v
Application JWT
```

Both flows produce application JWTs through the same implementation.

---

## 7. Reusing RefreshTokenLifecycleService

Our application also has a dedicated:

```text
RefreshTokenLifecycleService
```

It manages refresh-token creation and lifecycle operations.

It handles important responsibilities such as:

- Generating secure opaque refresh tokens.
- Hashing tokens before database storage.
- Applying expiration.
- Managing refresh-token sessions.
- Rotating refresh tokens.
- Detecting token reuse.
- Revoking token families.

### Why must OAuth2 login reuse it?

We do not want one refresh-token implementation for local login and another for social login.

Otherwise, security behavior could differ between authentication methods.

The success handler should use the existing lifecycle service rather than manually creating or persisting refresh tokens.

### Shared architecture

```text
              Local Login
                  |
                  |
              Social Login
                  |
                  v
          Authenticated Local User
                  |
                  v
       Shared Token Issuance Logic
                  |
          +-------+-------+
          |               |
          v               v
       JwtService   RefreshTokenLifecycleService
          |               |
          v               v
      Access JWT     Opaque Refresh Token
          |               |
          +-------+-------+
                  |
                  v
             LoginResponse
```

---

## 8. Enabled-User Validation

Our application supports enabled and disabled local users.

A disabled user must not obtain new authentication tokens.

In Phase 4.4, we added an enabled-user guard to:

```text
RefreshTokenLifecycleService.issueForUser(...)
```

### Why put the guard in the lifecycle service?

Token issuance is a security-sensitive operation.

If the enabled-user check exists only inside the local login controller or OAuth2 success handler, another caller might accidentally issue tokens without performing the same check.

Placing the guard in the shared issuance path provides another layer of protection.

### Expected behavior

| User status | Token issuance |
|---|---|
| Enabled user | Allowed |
| Disabled user | Rejected |
| Missing user | Rejected |

This is a defense-in-depth measure.

It does not replace the account checks performed during authentication.

---

## 9. LoginResponse Consistency

Our local login already returns a response represented by:

```text
LoginResponse
```

Social login should return the same response structure.

### Why is this important?

Imagine a frontend application supports:

- Email/password login.
- Google login.
- GitHub login.

If each login method returns a different response structure, the frontend must implement different token-handling logic for each provider.

A consistent response avoids that complexity.

### Expected response contract

The exact fields depend on the existing `LoginResponse` class.

For example, it may contain:

```json
{
  "accessToken": "application-jwt",
  "refreshToken": "opaque-refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

The example is illustrative. The existing DTO defines the actual contract.

### Important security rule

The response must contain our application's tokens, not the provider's access token or ID token.

---

## 10. Returning JSON Instead of Redirecting

Spring Security's traditional OAuth2 login behavior may redirect the browser after successful authentication.

Our application is a REST API.

Therefore, the success handler returns JSON instead.

### HTTP response

The successful OAuth2 callback returns:

```text
HTTP 200 OK
Content-Type: application/json
```

It serializes the existing `LoginResponse`.

### Why not redirect with tokens in the URL?

Putting access or refresh tokens in URLs is dangerous because URLs can appear in:

- Browser history.
- Server access logs.
- Proxy logs.
- Analytics systems.
- Referrer information.

Tokens should not be passed through query parameters or URL fragments as part of this implementation.

Our success handler returns them in the JSON response body.

---

## 11. Cache-Control and Pragma Headers

Authentication responses contain sensitive information.

We do not want browsers or intermediary caches to store these responses.

Our implementation sets:

```http
Cache-Control: no-store
Pragma: no-cache
```

### Cache-Control: no-store

It instructs caches not to store the response.

This is particularly important for responses containing access and refresh tokens.

### Pragma: no-cache

This is a legacy HTTP cache-control header retained for compatibility with older HTTP/1.0 caching behavior.

### Why use both?

Together, they help prevent sensitive authentication responses from being cached.

These headers should be included on both success and failure responses where appropriate.

---

## 12. OAuth2 Authentication Failure Handler

Authentication can fail because of:

- Invalid provider response.
- Invalid OAuth2 state.
- Provider authentication failure.
- Missing or invalid local principal.
- Disabled local user.
- Provisioning failure.

Our implementation includes a generic JSON failure handler.

### Why generic errors?

Returning raw provider exceptions can expose internal information.

A failure response should not reveal:

- Internal stack traces.
- Provider access tokens.
- Client secrets.
- Database details.
- Sensitive account information.

### Current reported behavior

The OAuth2 failure handler returns a generic non-cacheable HTTP 401 response.

It does not issue access or refresh tokens.

---

## 13. Refresh Token Sessions After Social Login

Social login must use the same refresh-token lifecycle as local login.

When a user successfully logs in through Google or GitHub:

1. A new opaque refresh token is generated.
2. Its hash is stored in the database.
3. Its expiration is applied.
4. A separate refresh-token session is created.
5. The raw refresh token is returned through the existing login response.

### Why separate sessions?

A user might log in using:

- A laptop.
- A desktop.
- A mobile device.
- A different browser.

Each login should have its own refresh-token session.

This allows sessions to be managed and revoked independently according to the existing token-family rules.

### Important distinction

The provider's OAuth2 access token and our application's refresh token serve different purposes.

The provider token is used for provider-related API access.

Our opaque refresh token is used to obtain new application access tokens through our refresh endpoint.

We should not expose provider tokens to the frontend unless a separate, explicit requirement calls for it.

---

## 14. OAuth2 Session State vs Stateless REST API

Our application uses:

```java
SessionCreationPolicy.STATELESS
```

The OAuth2 authorization process still needs temporary state to validate the provider callback.

Phase 4.2 introduced a session-backed authorization-request repository.

Phase 4.4 preserves that design.

### Temporary OAuth2 session

Used for:

- Storing the authorization request.
- Preserving the state parameter across redirects.
- Validating the callback.

### JWT authentication

Used for:

- Authenticating protected REST API requests.
- Identifying the local application user.
- Authorizing API access.

### Subsequent API requests

After login, the client sends:

```http
Authorization: Bearer <application-access-token>
```

The JWT authentication filter processes the request.

The REST API does not depend on an authenticated HTTP session.

---

## 15. End-to-End Authentication Architecture

Our authentication architecture now supports three login paths:

```text
                  Authentication
                         |
          +--------------+--------------+
          |              |              |
          v              v              v
       Local           Google          GitHub
       Login            Login           Login
          |              |              |
          v              v              v
     Credentials      OIDC Flow      OAuth2 Flow
          |              |              |
          |              v              v
          |        SocialAccountService
          |              |
          |              v
          |          Local User
          |              |
          +--------------+
                         |
                         v
              Shared Token Issuance
                         |
              +----------+----------+
              |                     |
              v                     v
           JwtService      RefreshTokenLifecycleService
              |                     |
              v                     v
          Access JWT          Refresh Token
              |                     |
              +----------+----------+
                         |
                         v
                   LoginResponse
                         |
                         v
                 Client Application
                         |
                         v
               Protected REST APIs
```

The key architectural benefit is that all three login methods ultimately use one local-user and token-issuance model.

---

## 16. Testing Strategy

Phase 4.4 includes handler unit tests and H2-backed integration tests.

### Success handler tests

Test:

- Google authentication produces an application token response.
- GitHub authentication produces an application token response.
- Existing response fields remain consistent.
- Provider tokens are not returned.
- Disabled and missing users are rejected.
- JSON content type is correct.
- Cache-prevention headers are present.

### Refresh-token integration tests

Test:

- Refresh-token hashes are stored.
- Different login sessions remain distinct.
- Rotation works.
- Reuse detection works.
- Family revocation works.

### Security integration tests

Test:

- OAuth2 callback reaches the success handler.
- Invalid state does not issue tokens.
- Failure responses are generic.
- JWT bearer authentication still works.
- Local login remains functional.

### Reported test result

```text
Java version: 21
Tests run: 105
Failures: 0
Errors: 0
Skipped: 0
```

This is the reported final clean test result for Phase 4.4.

---

## 17. Files and Components Added

### Production component

| Component | Responsibility |
|---|---|
| OAuth2AuthenticationSuccessHandler | Issues application tokens after successful social login |
| Generic OAuth2 failure handler | Returns safe JSON authentication failures |

### Modified components

- `SecurityConfig`
- `RefreshTokenLifecycleService`
- `learning_concepts.md`

The existing JWT and refresh-token implementation was reused rather than duplicated.

---

## 18. Important Interview Questions

**Q1. What is AuthenticationSuccessHandler in Spring Security?**

It is an extension point that defines what happens after successful authentication.

**Q2. Why did we implement a custom OAuth2 success handler?**

Because our REST API needs to return application JWT and refresh tokens as JSON instead of using the default redirect behavior.

**Q3. Does Google issue our application's JWT?**

No. Google authenticates the external identity. Our backend issues its own JWT after resolving the local user.

**Q4. Why reuse JwtService?**

It ensures all login methods use the same JWT signing, claims, subject, and expiration rules.

**Q5. Why reuse RefreshTokenLifecycleService?**

It ensures all login methods follow the same secure refresh-token generation, hashing, expiration, rotation, and revocation rules.

**Q6. Why should disabled users be rejected during token issuance?**

Otherwise, an alternative authentication path could accidentally issue tokens to a disabled account.

**Q7. Why return the same LoginResponse for local and social login?**

It provides a consistent authentication contract for frontend clients.

**Q8. Why return JSON instead of redirecting with tokens?**

It avoids exposing tokens in URLs and provides a consistent REST API response.

**Q9. What does `Cache-Control: no-store` do?**

It instructs caches not to store the authentication response containing sensitive tokens.

**Q10. Why do we need an OAuth2 failure handler?**

It allows the application to return generic, safe JSON responses without exposing internal provider or implementation errors.

**Q11. Does SessionCreationPolicy.STATELESS prevent temporary OAuth2 sessions?**

It configures Spring Security not to use HTTP sessions as the persistent security context for API authentication. OAuth2 can still use temporary session-backed authorization-request state.

**Q12. Why should provider access tokens not be returned to our frontend?**

They belong to the external provider's authorization context. Our API should issue its own application tokens and expose only what the client needs.

**Q13. How does a refresh token differ from an access token?**

An access token authenticates API requests. A refresh token is used to obtain a new access token when the current one expires.

**Q14. Why should refresh tokens be stored as hashes?**

If the database is compromised, an attacker should not immediately obtain usable refresh tokens from stored records.

---

## 19. Phase 4.4 Completion Summary

Phase 4.4 completes the social-login token integration.

Our application now has a shared authentication architecture:

- Email/password login.
- Google login.
- GitHub login.

All three resolve a local user and use the existing token-generation and refresh-token lifecycle implementation.

The next milestone is a final authentication integration and hardening review.

**Next: Phase 4.5 — Authentication Integration Review and Security Hardening.**

---

# Phase 4.5 — Authentication Integration Review and Security Hardening

## 1. Review Scope and Outcome

This phase reviewed the existing local, JWT, refresh-token, password-reset, Google OIDC, GitHub OAuth2, session-state, CORS, rate-limiting, configuration, and test implementations. It did not add Task CRUD, account linking, new providers, or a new token architecture.

The review confirmed a shared local-user and application-token model for local and social sign-in. It also identified consistency and deployment-hardening work:

- Local email-based operations now normalize email input and use case-insensitive lookup, consistent with social identity mapping. This prevents case variants from being treated as unrelated identities by authentication and reset flows.
- Login input validation is bounded, including the BCrypt-compatible password byte-length constraint.
- OAuth2's temporary session cookie has explicit HttpOnly, SameSite, timeout, and Secure settings. Secure cookies are enabled in the production profile; local development can use HTTP.
- Tests cover case-insensitive identity behavior, cookie configuration, OAuth state rejection, and the separation between temporary OAuth state and REST bearer authentication.

These are targeted consistency and hardening changes, not a claim that the service is production-secure without deployment-specific controls.

## 2. Shared Authentication Architecture

The reviewed flow is:

```text
Local email/password ─┐
Google OIDC ──────────┼─> Local User ─> JwtService + RefreshTokenLifecycleService
GitHub OAuth2 ────────┘                    └─> LoginResponse
```

Local login authenticates the local account. Google and GitHub authenticate an external identity, which is mapped to a local user through `SocialAccountService`. Successful paths use the application's existing JWT and refresh-token issuance; provider credentials are not the application's API tokens. The OAuth2 success handler returns the existing JSON response rather than placing tokens in a URL.

The source and tests were reviewed for disabled-user handling, token issuance consistency, generic OAuth failures, and non-cacheable authentication responses. OAuth2 tests use mocked provider data rather than contacting Google or GitHub.

## 3. JWT and Access-Token Findings

The JWT service explicitly signs with HS256 and validates the signature, configured issuer, expiration, issue time, and subject. The configured secret is Base64-decoded, must contain at least 32 bytes, and has no production fallback secret. The bearer filter resolves the local user and rejects missing or disabled users; malformed and invalid tokens do not authenticate.

Access tokens are stateless. Logout and password changes revoke refresh sessions but cannot immediately invalidate an already-issued access token. The configured access-token lifetime bounds this exposure; account disablement is checked when protected requests resolve the user. No token blacklist was introduced.

## 4. Refresh Tokens and Password Authentication

Refresh tokens are generated with secure randomness. Only SHA-256 hashes are persisted; refresh operations hash the presented token for lookup, check expiry and user status, and lock the persisted token while rotating it. Reuse detection revokes the relevant token family. Logout is session/family-scoped, and password changes and resets revoke refresh sessions.

Passwords use BCrypt, are not returned in response DTOs, and OAuth-only users do not authenticate using a predictable placeholder password. Reset tokens are hashed at rest, expire, and are processed as single-use values under a database lock. The user-facing reset response is generic.

### Password-reset delivery limitation

No production `PasswordResetTokenDelivery` adapter is configured. Without an adapter, a reset request can return its generic response without delivering a usable reset link. Production deployment must configure a delivery channel before enabling password reset for users.

## 5. OAuth2 State, Sessions, CSRF, and CORS

The API uses stateless security-context handling and bearer tokens for protected REST requests. OAuth authorization requests are the exception: a temporary session-backed repository retains the authorization request and `state` across the provider redirect. State is validated on callback; a missing or mismatched state fails before the success handler can issue application tokens. The session is not the REST authentication credential.

The OAuth session timeout is 10 minutes. Its cookie is HttpOnly and SameSite=Lax; Secure is configurable for local development and enabled in production. Deployments behind TLS-terminating proxies should correctly forward secure-request information.

CSRF is disabled for the REST API because the API's access and refresh credentials are sent in authorization headers or request bodies, not ambient authentication cookies. OAuth2 callback state remains enabled as the redirect-flow CSRF defense. Revisit this decision if authentication credentials are moved to cookies.

CORS uses explicit configured origins, allows the required API methods and `Authorization`/`Content-Type` headers, and does not enable credentialed cross-origin requests. Existing headers include content-type sniffing protection, frame denial, a strict referrer policy, and HSTS. A CSP was not added to this JSON API; HSTS deployment behavior depends on correct HTTPS and proxy configuration.

## 6. Provider Identity and Provisioning

Google identity mapping uses the OIDC subject and requires a verified email. GitHub mapping uses the stable provider ID and obtains an appropriately verified email rather than trusting an arbitrary public profile email. Provider identity uniqueness, email-collision protection, and concurrent provisioning controls are part of the existing mapping implementation. Account linking was not added.

The success path issues only application tokens. OAuth2 failures use a generic JSON response and do not expose provider exceptions or issue tokens. No real provider credentials or network calls are required by the test suite.

## 7. Rate Limiting

Login and reset-request rate limiting is bounded and synchronized in-process. Blocked buckets are not evicted in a way that simply resets an active limit. The limiter uses per-process state, so it resets on restart and does not coordinate across application instances; production multi-instance deployments need a shared rate-limit store or an upstream gateway policy. Fixed-slot collisions can conservatively cause unrelated identities to share a limit.

## 8. Configuration and Database Deployment

JWT and OAuth client secrets and production database credentials are externally configured; no production signing-secret fallback is present. The production profile validates the schema rather than creating it, and H2 console exposure is not part of production configuration.

**Migration readiness is incomplete:** no Flyway/Liquibase framework or production migration was found. Before deploying with schema validation, provide migrations for the authentication tables, including `oauth_accounts` with its local-user foreign key, provider identity uniqueness `(provider, provider_user_id)`, user index, provider enum representation, and creation timestamp, as well as refresh-token and password-reset tables. Check existing user data for email values that differ only by case before deploying case-insensitive identity behavior.

## 9. Verification and Test Coverage

The reviewed and added tests cover local authentication regressions, mixed-case email lookup/duplicate checks/reset lookup, bounded login inputs, OAuth cookie settings, missing/mismatched OAuth state, and temporary-session isolation from protected bearer-authenticated API requests. Existing tests also exercise JWT validation, refresh rotation/reuse/concurrency, password reset, disabled users, CORS preflight, and OAuth2 token issuance. Provider behavior is mocked.

The targeted authentication/security test selection passed after the Phase 4.5 changes. The final Java 21 `clean test` result for this phase must be checked when executing the complete suite; the earlier Phase 4.4 result does not substitute for that run.

## 10. Known Limitations and Production Recommendations

- Stateless access tokens remain usable until expiry after logout or password change; keep their lifetime limited and document this behavior.
- Configure and test a production password-reset delivery adapter.
- Apply reviewed database migrations before using production schema validation.
- Use a distributed rate limiter or gateway policy for multi-instance deployments.
- Verify proxy forwarding and HTTPS cookie/HSTS behavior in the actual deployment topology.
- Externalize secrets and rotate them through the deployment secret manager; do not log credentials, tokens, or provider responses containing sensitive data.
- Passing tests are evidence for covered behavior, not proof of complete production security.

## 11. Important Interview Questions

**Why do all login methods resolve a local user?** Application authorization and token subjects must use one stable identity model, regardless of which mechanism authenticated the user.

**Why can an OAuth2 authorization flow use a session in a stateless API?** The session temporarily stores authorization-request state for the redirect callback; subsequent API requests still authenticate with bearer JWTs.

**Why are refresh tokens hashed but access tokens not stored?** Refresh tokens are long-lived secrets that require server-side lifecycle control. Access JWTs are validated cryptographically and remain valid until expiry unless additional revocation infrastructure is introduced.

**Why does disabling a user not instantly invalidate an already-issued JWT?** Stateless tokens are not centrally tracked. This implementation checks the local user's enabled state on requests, but other account changes do not otherwise revoke a token's signature or expiry.

**Why is an OAuth2 state check still necessary when CSRF is disabled for API routes?** The external browser redirect is a distinct flow; state binds the callback to the authorization request initiated by the client.

**What must be completed before production deployment?** Apply versioned schema migrations, configure reset-token delivery and distributed rate limiting as required, and validate HTTPS/proxy, secret, and cookie settings in the target environment.

---

# Phase 5.1 — Authenticated User Context and Secure Task Management Foundation

## 1. Scope

This phase establishes authenticated-user resolution and owner-scoped task persistence/service operations. It does not add a Task controller, REST endpoints, task DTOs, pagination, sorting, or new authentication behavior. Existing task entity mappings remain unchanged.

## 2. Authentication and the SecurityContext

Spring Security stores the request's authenticated `Authentication` in its `SecurityContext`. The JWT filter validates the bearer token, loads the local account through `TaskManagerUserDetailsService`, and sets a `UsernamePasswordAuthenticationToken` whose principal is Spring Security `UserDetails`. That user-details username is the normalized local email, available through `Authentication.getName()`.

Task operations accept the request's `Authentication` explicitly. `CurrentUserService` rejects null, unauthenticated, anonymous, or blank identities, normalizes the email, resolves the local `User` using a case-insensitive repository lookup, and rejects missing or disabled users. It does not assume the JWT contains a database ID and does not cast the principal to an OAuth-specific type. Social logins continue to use their existing flow; their issued application JWT resolves the same local email through the normal bearer filter on subsequent API requests.

## 3. User-Scoped Database Access and Ownership

Task persistence queries used for user-facing operations require the owner ID in the database predicate:

- `findAllByUserId(userId)` lists only that user's tasks.
- `findByIdAndUserId(taskId, userId)` retrieves a task only when both identifiers match.
- `existsByIdAndUserId(taskId, userId)` checks ownership without a global task lookup.
- Status and title search repository shapes, where present, also require user ID.

The service resolves the user from the trusted authentication and passes the resulting local user ID to these repository methods. It does not accept a client-provided user ID, query all tasks and filter in Java, or perform task updates/deletes by task ID alone. A missing task and another user's task both result in the same not-found behavior, avoiding disclosure that another user's task exists.

Task creation attaches the resolved local user. Updates change task fields only and never reassign the owner. Deletion first retrieves the task using both task and owner IDs.

## 4. Responsibilities, Transactions, and Data Exposure

`CurrentUserService` translates a trusted Spring Security identity into the local user entity. `TaskRepository` enforces ownership in persistence predicates. `TaskService` coordinates those components and retains transaction boundaries: creation, update, and deletion are transactional; reads are read-only transactional operations.

The service currently returns entities for internal use because there is no Task REST controller in this milestone. A later API layer must map entities to response DTOs and must not serialize `User` or expose password hashes. The Task mapping remains a mandatory lazy `ManyToOne` to User, with a non-null `user_id`, string enum persistence, timestamp callbacks, and no cascade relationship added.

## 5. Isolation Tests

Unit tests verify authentication validation, normalized lookup, missing/disabled-user rejection, and the task service's use of owner-scoped repository operations. H2 tests create separate users and tasks, verify each owner sees only their task, and verify one user's get/update/delete attempts against another user's task use not-found behavior and leave the other task unchanged. Tests also verify task ownership persists through an update.

## 6. Future API and CORS Requirement

No CORS methods were changed in Phase 5.1. When task HTTP endpoints are introduced, update the configured allowed methods to include the methods actually used by those endpoints, including `PUT`, `PATCH`, and `DELETE` as applicable, and test preflight behavior. Keep origins explicit and do not enable credentialed requests unless the credential transport requires them.

## 7. Important Interview Questions

**Why use `Authentication.getName()` rather than a user ID from the request?** The authentication was established by the security filter from a verified token; a request-supplied ID is attacker-controlled and could select another user's data.

**Why resolve the local User from the authenticated email?** The JWT principal contains the local email identity, not necessarily a database ID. The repository lookup converts that trusted identity into the entity needed by internal persistence operations.

**Why include owner ID in the database query?** Database-level scoping prevents another user's row from being loaded or modified and avoids relying on application-side filtering after a global query.

**Why return not found for a task owned by someone else?** It provides the same observable result as a nonexistent task, reducing task-existence disclosure.

**Why does an update leave ownership unchanged?** Ownership is assigned at creation from the authenticated user; mutable task fields must not allow a client or service caller to transfer a task to another account.

**Why should a future controller return DTOs rather than JPA entities?** DTO mapping controls the API contract and prevents accidental exposure of internal relationships or sensitive user fields such as password hashes.

**Which CORS change accompanies future task routes?** Add only the required HTTP methods, such as `PUT`, `PATCH`, and `DELETE`, and validate the corresponding preflight requests without broadening allowed origins unnecessarily.

---

# Phase 5.2 — Task REST API, DTOs, Validation, and Exception Handling

## 1. REST Resources and Routes

Tasks are exposed as resources under `/api/v1/tasks`. The route identifies the resource collection or a task by ID; ownership is derived from the authenticated identity, not from URL or request data.

| Method and route | Operation | Success |
|---|---|---|
| `POST /api/v1/tasks` | Create a task for the authenticated user | `201 Created` |
| `GET /api/v1/tasks` | List that user's tasks | `200 OK` |
| `GET /api/v1/tasks/{taskId}` | Retrieve one owned task | `200 OK` |
| `PUT /api/v1/tasks/{taskId}` | Replace the editable fields of an owned task | `200 OK` |
| `DELETE /api/v1/tasks/{taskId}` | Delete an owned task | `204 No Content` |

`POST` creates a resource, `GET` reads it, `PUT` updates it, and `DELETE` removes it. Authentication is required for every task route. A task missing from the current user's scope returns `404`, whether it does not exist or belongs to someone else.

## 2. Controller, DTOs, and Mapping

`TaskController` is a thin HTTP adapter: it accepts the request, obtains Spring Security's `Authentication`, calls the existing owner-secured `TaskService`, and maps results to response DTOs.

`CreateTaskRequest` contains `title`, optional `description`, and optional `dueDate`. New tasks start in `PENDING`. `UpdateTaskRequest` contains the editable fields and a required `TaskStatus`. Neither request accepts a user ID or owner. Both use `LocalDate` for the due date.

`TaskResponse` contains only `id`, `title`, `description`, `status`, `dueDate`, `createdAt`, and `updatedAt`. `TaskResponse.from(Task)` performs the entity-to-response mapping. The API never serializes `Task` or `User` directly.

Why separate DTOs from JPA entities?

- Entity changes and lazy relationships should not implicitly change the public API.
- A `User` relationship could expose internal user information, including password hashes if entity serialization were expanded.
- Request fields must not allow clients to set internal values such as task ownership or persistence timestamps.

## 3. Bean Validation and Request Parsing

The controller uses `@Valid` on both request DTOs. Title validation uses `@NotBlank` and `@Size(max = 255)`. Description is limited to 255 characters, matching the entity's default string-column length. Updates require a non-null status; Jackson maps recognized values to `TaskStatus` and rejects unknown enum values. Invalid `LocalDate` values and malformed JSON are rejected during request parsing.

Bean Validation distinguishes constraints:

- `@NotBlank` rejects null, empty, or whitespace-only text.
- `@Size` limits the length of text.
- `@NotNull` requires a value, including the update status.

Validation and request parsing errors return `400 Bad Request` with a common JSON structure and safe field details. They do not expose framework parsing exceptions.

## 4. ResponseEntity and HTTP Errors

The creation and deletion endpoints use `ResponseEntity` to express `201 Created` and `204 No Content`. Reads and updates return DTOs directly, which Spring serializes as `200 OK`.

`TaskExceptionHandler` is a controller-scoped `@RestControllerAdvice`. Its `@ExceptionHandler` methods translate:

- Validation errors into `400` with field details.
- Malformed JSON, invalid dates, and invalid enum values into `400`.
- `EntityNotFoundException` into a generic `404 Task not found.` response.
- Unexpected task-controller failures into a logged server error and generic `500` response.

Responses use the existing `ApiErrorResponse` fields: timestamp, numeric status, reason phrase, safe message, request path, and details. Authentication failures (`401`) and access denials (`403`) remain the responsibility of the existing Spring Security entry point and access-denied handler; the task advice does not duplicate those handlers or disclose exception internals.

## 5. Ownership, Authentication, and Authorization

The JWT filter validates the bearer token and populates the `SecurityContext`. The controller receives the authenticated `Authentication`; `CurrentUserService` resolves `Authentication.getName()` to the enabled local user. The service passes that user's database ID to owner-scoped repository queries.

Authentication answers “who is making the request?” Authorization answers “may this user operate on this task?” Every task service operation retains the Phase 5.1 ownership check. Client-supplied `userId` or nested owner data is not used to set ownership. Create assigns the resolved user; update changes task fields only. Get, update, and delete queries constrain both task ID and owner ID.

## 6. CORS and Security

The existing stateless JWT security, OAuth2 flow, authentication handlers, and CSRF decision are unchanged. Task routes remain protected by the existing `anyRequest().authenticated()` rule.

CORS now allows only the methods implemented by these routes: `GET`, `POST`, `PUT`, `DELETE`, and `OPTIONS`. A browser preflight `OPTIONS` request announces the intended method and headers before the actual cross-origin request; tests verify preflight for `PUT` and `DELETE`. Origins remain explicit and credentialed CORS remains disabled.

## 7. Testing

`TaskControllerIntegrationTest` exercises authenticated HTTP requests through MockMvc and the real Spring application/H2 database. It covers the full CRUD status/response contract, request validation, malformed dates and enum values, missing tasks, unauthenticated access, CORS preflight, DTO field exposure, and two-user ownership isolation. The two-user case verifies a forged owner field does not change task ownership and that another user's failed update/delete leaves the task unchanged.

Service and repository tests continue to verify owner-scoped query behavior and task ownership persistence. Controller tests validate HTTP translation and serialization; service/repository integration tests validate database predicates and multi-user isolation. Both layers matter because a controller-only mock test cannot prove database ownership enforcement.

## 8. Important Interview Questions

**Why use resource-oriented URLs?** They identify the task collection or a specific task independently from the HTTP operation performed on it.

**Why return `201` from task creation and `204` from deletion?** `201 Created` signals successful resource creation; `204 No Content` signals successful deletion without an unnecessary response body.

**Why use request and response DTOs instead of entities?** DTOs control accepted input and public output, protect internal fields, and decouple the REST contract from persistence mappings.

**What does `@Valid` do?** It asks Spring to run the Bean Validation constraints declared on the request DTO before the controller processes it.

**How are invalid enum values handled?** Jackson cannot deserialize an unknown value to `TaskStatus`; the request parsing exception is mapped to a generic `400` response.

**What is the role of `@RestControllerAdvice`?** It centralizes controller exception-to-HTTP-response mapping while returning response objects as JSON.

**Why use a generic 404 for another user's task?** It avoids telling a caller that a task exists outside their ownership scope.

**Why must an OPTIONS preflight be allowed?** Browsers may send it to ask whether a cross-origin request using methods such as `PUT` or `DELETE` is permitted before sending that request.

**Why is an authenticated user not automatically authorized for every task?** Authentication identifies the caller; owner-scoped service and repository operations enforce authorization for each resource.

**Were pagination, search, PATCH, or task ownership transfer implemented?** No. Those features are outside Phase 5.2; only the defined CRUD routes and owner-scoped behavior are included.

# Phase 5.3 — Pagination, Sorting, Searching, and Filtering

## 1. Why list endpoints need pagination

Pagination splits a large collection into manageable slices. Returning every task at once consumes database, server, network, and client memory, and response time grows with the owner's data. The task listing endpoint therefore asks the database for one requested page instead of loading all matching tasks and slicing them in Java.

For example, `GET /api/v1/tasks?page=0&size=10` requests the first ten tasks. Page numbers start at zero. If parameters are omitted, page `0`, size `10`, `createdAt` descending are used. Page sizes must be between 1 and 100; invalid values are rejected with `400 Bad Request` rather than silently changed.

## 2. Spring Data pagination types

`Pageable` describes a requested page, page size, and ordering. The service builds it using `PageRequest.of(page, size, sort)`. The repository accepts that `Pageable` and returns `Page<Task>`, which includes the current database results and metadata such as total elements, total pages, and whether the page is first or last. Spring Data applies the limit and offset in the database query; it does not fetch every task and paginate in application memory.

Offset-based pagination is straightforward and fits this API: page 2 with size 10 represents an offset of 20. Very deep offsets can become expensive for large tables; cursor/keyset pagination may be a later optimization if measured usage justifies the extra complexity.

## 3. Sorting

The endpoint accepts `sortBy` and `direction`, for example `GET /api/v1/tasks?page=0&size=10&sortBy=title&direction=asc`. Supported fields are `createdAt`, `updatedAt`, `dueDate`, `title`, and `status`. Direction is `asc` or `desc`, case-insensitively. Defaults are `createdAt` and `desc`.

The service validates the sort field against a fixed allowlist before constructing Spring Data `Sort`; arbitrary client input never becomes a SQL fragment. An ascending sort places values in increasing order and a descending sort reverses that order. The service adds task `id` as a stable tie-breaker so tasks with equal primary sort values have deterministic ordering across page requests.

## 4. Search and filters

`search` performs a case-insensitive partial match against the task title. For example, `?search=assignment` matches titles containing that word regardless of letter case. Search text is trimmed; a missing or blank value turns off the search condition. `status` accepts the existing `PENDING`, `IN_PROGRESS`, or `COMPLETED` enum value. Unknown values are rejected as `400 Bad Request`.

The API also accepts inclusive ISO date bounds `dueFrom=YYYY-MM-DD` and `dueTo=YYYY-MM-DD`. Either bound can be provided independently; when both are supplied, `dueFrom` must not be after `dueTo`. Search, status, due-date bounds, sorting, and pagination can be combined in one request.

The repository uses one JPQL query with optional conditions instead of separate query methods for every filter combination. Its mandatory predicate is `task.user.id = :userId`; every optional condition is combined with this owner predicate. This keeps pagination totals and content restricted to the authenticated user. Empty search and missing filters do not create global, unscoped task queries.

## 5. Stable response DTO

The controller does not serialize `Page<Task>` or the JPA entity. `TaskPageResponse` maps each page content item through the existing `TaskResponse` and returns `content`, `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `first`, `last`, and `empty`. This provides clients with a stable JSON shape while keeping persistence objects and the user relationship private.

Example request:

```text
GET /api/v1/tasks?page=0&size=10&sortBy=createdAt&direction=desc&search=assignment&status=PENDING
```

## 6. Ownership and authorization

The endpoint does not accept a user ID. `CurrentUserService` resolves the authenticated principal, then the service passes that user's database ID to `TaskRepository.findOwnedTasks`. Search matches or status filters cannot widen access: the owner condition is always present, including in the database count query used to calculate `totalElements`. The same ownership boundary continues to apply to get, update, and delete operations.

Authentication answers who made the request; authorization answers which resources that identity may access. A valid JWT does not grant access to another user's tasks. Tests use two users to verify that content, search results, status results, and pagination totals remain isolated.

## 7. Testing and performance

Repository tests verify that filtering and totals are owner-scoped at the database layer. Service tests verify the sort allowlist, page bounds, and that only the resolved user ID is used. MockMvc integration tests exercise the real HTTP parameter conversion, JSON response metadata, pagination, sorting, combined search/status/date filters, invalid values, and two-user isolation against H2.

Database-side filtering and paging avoids holding an owner's entire task collection in application memory. The configured maximum page size limits accidental or abusive oversized responses. For production data volumes, indexes should be selected from actual query plans and workload measurements; this milestone does not add schema or database migrations.

## 8. Common interview questions

**What is the difference between `Page` and `Pageable`?** `Pageable` describes what to fetch; `Page` contains the fetched content and metadata about the complete matching result.

**Why use `PageRequest`?** It is Spring Data's concrete request implementation and combines page number, size, and `Sort` in a type the repository understands.

**How is pagination applied?** The repository receives the `Pageable`, so Spring Data generates database-level offset/limit behavior and a count for matching rows rather than loading all results first.

**Why validate sort fields?** Sorting is built from entity property names. An allowlist makes the API intentional and prevents arbitrary input from being treated as a dynamic query property.

**How are optional filters combined?** The repository query includes each filter only when its parameter is non-null, while always retaining the authenticated owner's ID predicate.

**Why not return `Page<Task>` directly?** It exposes persistence-oriented types and can leak entity relationships or make the API contract depend on Spring Data. A dedicated page DTO returns only deliberate public fields and metadata.

**Can searching return a different user's task?** No. The case-insensitive title condition is applied alongside the mandatory `task.user.id` condition, so matching is always owner-scoped.

**What is a limitation of offset pagination?** Large offsets can be costly and concurrent inserts can shift later pages. Cursor/keyset pagination can help for high-volume use, but is not part of this phase.

# Phase 5.4 — Task Priority Management

## 1. Priority as a Java enum

`TaskPriority` represents the finite set of task importance labels: `LOW`, `MEDIUM`, `HIGH`, and `URGENT`. An enum is safer than accepting arbitrary strings because Java and Jackson can only map recognized constants. Each task has one non-null priority.

The JPA entity maps it with `@Enumerated(EnumType.STRING)`. This stores readable names such as `HIGH` in the database, rather than ordinal numbers. String storage avoids changing the meaning of existing rows if enum constants are reordered in source. Adding or renaming values still requires an appropriate production schema/data migration strategy.

## 2. Defaults and update semantics

New tasks default to `MEDIUM`: the entity initializes that value and its constructor uses it when no priority is supplied; the service applies the same default for optional create input. The database column is non-null. A create request may omit `priority` or provide any valid enum value.

The API uses `CreateTaskRequest` and `UpdateTaskRequest`, not entities. Create priority is optional; update priority is required because `PUT` is a full update in this API. `@NotNull` rejects a missing update priority through the existing validation error format. Unknown priority strings in JSON are rejected during request deserialization with `400 Bad Request`. Task ownership is still derived from the authenticated user and is not accepted in either DTO.

`TaskResponse` now includes the `priority` value while continuing to expose only approved API fields. User entities, password hashes, refresh tokens, and persistence internals remain excluded.

## 3. Filtering and sorting

The task listing endpoint accepts an optional `priority` query parameter. For example:

```text
GET /api/v1/tasks?priority=HIGH&status=PENDING&search=assignment&page=0&size=10
```

Priority filtering composes with the existing status, case-insensitive title, and inclusive due-date filters. The repository query applies all provided conditions together and always includes the authenticated owner's ID. Spring Data continues to apply pagination and ordering in the database, and its total count uses the same filters and owner condition. Omitting priority leaves tasks at all priority levels eligible.

`priority` is now an approved `sortBy` field as well. Since the entity stores enum names as strings, the database orders them by the database's string collation—not by business urgency. With the usual case-sensitive lexical ordering of these uppercase names, ascending order is `HIGH`, `LOW`, `MEDIUM`, `URGENT`; do not infer that this is a business ranking. The service retains the task ID ascending tie-breaker for deterministic ordering among equal priorities.

An invalid `priority` query value is rejected as a client error using the existing task API exception response. No dynamic SQL is constructed from the input; the service's sort allowlist remains in force.

## 4. Testing and interview notes

Repository tests persist and reload every enum value, check enum sorting, combine priority with status and title search, verify page totals, and include another user's matching task to prove owner isolation. Service tests verify the `MEDIUM` default, explicit priority assignment, priority updates, allowed sorting, and owner resolution. MockMvc integration tests verify create/update/response behavior, all priority filters, combined filters, page counts, enum sorting, invalid values, and two-user isolation.

**Why use `EnumType.STRING` instead of `EnumType.ORDINAL`?** String names remain associated with the same meaning if constants are reordered; ordinals could silently change meaning. Renaming a constant still requires data migration care.

**Does sorting by enum produce a priority ranking?** No. This endpoint sorts the persisted enum names according to database string collation. A business-defined ranking would require explicit ranking logic or a separate sort key.

**Why make update priority required but create priority optional?** Create has a defined default (`MEDIUM`), whereas a full `PUT` should provide the complete task state so the caller does not accidentally retain or omit a field.

**Can a priority filter reveal another user's tasks?** No. Priority is only an additional predicate; every result and total remains constrained by the authenticated user's ID.


----------------------------------------------------------------------------------------

# Phase 5.4 — Task Priority Management: Learning Concepts

**Project:** Task Management REST API  
**Stack:** Java 21, Spring Boot 3.5.7, Spring Data JPA, Hibernate, Spring Security

---

## 1. Java Enums

An enum is a Java type that represents a fixed set of named constants.

Example:

    public enum TaskPriority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }

Use an enum when a field must contain one value from a known set.

Compared with a String, an enum:
- Prevents arbitrary values from being assigned in ordinary Java code.
- Provides compile-time type safety.
- Makes code more readable.
- Represents a clearly defined set of possible values.

Example:

    private TaskPriority priority;

`TaskPriority.valueOf("HIGH")` converts a matching string into an enum constant. An unknown name throws `IllegalArgumentException`.

JSON frameworks such as Jackson commonly serialize enum values using their names, such as `"HIGH"`.

**Interview point:** Enums are appropriate for fixed domain choices such as task status, priority, payment state, and account type.

---

## 2. Persisting Enums with JPA

Use `@Enumerated` to define how JPA stores an enum.

Example:

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority;

With `EnumType.STRING`, the database stores names such as LOW, MEDIUM, HIGH, and URGENT.

### EnumType.STRING vs EnumType.ORDINAL

| Mapping | Stored value | Consideration |
|---|---|---|
| EnumType.ORDINAL | Numeric position, such as 0 or 1 | Reordering constants can change the meaning of existing data |
| EnumType.STRING | Name, such as HIGH | Renaming a constant requires data migration |

For business values, STRING is generally easier to inspect and less dependent on declaration order.

It does not, however, define a custom business ranking.

**Important:** Prefer `EnumType.STRING` for values such as task priority and task status because it avoids depending on the declaration position of enum constants.

---

## 3. Defaults and Nullability

A Java enum field is initially null unless the application assigns a value.

An entity-level default can be expressed as:

    private TaskPriority priority = TaskPriority.MEDIUM;

A service can also apply a default during creation:

    TaskPriority priority = request.priority() == null
        ? TaskPriority.MEDIUM
        : request.priority();

### Difference between default and nullability

- Java/entity default: Assigned by application code.
- Database default: Assigned by the database when an insert omits the column.
- `@Column(nullable = false)`: Declares a non-null constraint; it does not assign MEDIUM.

Choose a consistent defaulting strategy.

If multiple creation paths exist, ensure all of them produce a valid priority.

If a database migration is later introduced, existing rows must be backfilled before a new non-null constraint is enforced.

**Important:** `nullable = false` and a default value solve two different problems.

---

## 4. DTOs and API Contracts

DTOs define the data accepted or returned by the API.

They keep the external API contract separate from persistence entities.

### Create request example

    public record CreateTaskRequest(
        String title,
        String description,
        TaskPriority priority,
        LocalDate dueDate
    ) {}

### Update request example

    public record UpdateTaskRequest(
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate
    ) {}

### Why can Create and Update behave differently?

- Create request: Priority may be omitted, and the application can default it to MEDIUM.
- Update request: A full update (PUT) may require the client to provide priority explicitly.
- Partial update (PATCH): An omitted field should generally remain unchanged rather than being reset to a default.

Document what omission means for each endpoint.

Do not let clients choose task ownership by supplying `userId`. Ownership comes from the authenticated principal and server-side service logic.

---

## 5. Response DTO Mapping

A response DTO can expose priority without exposing the JPA entity.

Example:

    public record TaskResponse(
        Long id,
        String title,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate
    ) {}

The mapper copies:

    task.getPriority()

into the response.

### Benefits of DTOs

- The API returns only intended fields.
- JPA relationships are not serialized accidentally.
- Internal entity changes do not automatically become API changes.
- Sensitive fields such as password hashes and refresh-token data remain outside task responses.

---

## 6. Optional Query Parameters and Spring MVC

A list endpoint can accept priority as an optional query parameter.

Example:

    @RequestParam(required = false) TaskPriority priority

### API examples

    GET /api/v1/tasks

    GET /api/v1/tasks?priority=HIGH

    GET /api/v1/tasks?priority=URGENT&status=PENDING

When a matching enum name is supplied, Spring MVC converts the query string into the corresponding enum value.

For example:

    priority=HIGH

becomes:

    TaskPriority.HIGH

An invalid value such as:

    priority=SUPER_HIGH

cannot be converted and should produce the application's established client-error response.

**Important:** Query-parameter conversion and request-body JSON deserialization are separate input paths. Test invalid enum values in both places if both accept priority.

---

## 7. Combining Optional Repository Filters

The priority predicate in JPQL can follow this pattern:

    (:priority is null or task.priority = :priority)

### How does it work?

- If priority is null, the priority predicate does not restrict the results.
- If priority is HIGH, only HIGH-priority tasks match.

It should combine with the existing owner, status, title, and due-date predicates.

### Owner predicate

The owner predicate is mandatory:

    task.user.id = :userId

### Complete logical condition

    owner matches

    AND (priority absent OR priority matches)

    AND (status absent OR status matches)

    AND (title absent OR title matches)

    AND (due-date lower bound absent OR due date is on/after it)

    AND (due-date upper bound absent OR due date is on/before it)

Both the page-content query and count query must preserve the owner restriction and all active filters.

Otherwise, page metadata may be inaccurate or disclose information about another user's tasks.

### Page and Pageable

A Spring Data repository method returning `Page<Task>` can accept a `Pageable`.

A Page includes:

- Current page content.
- Page number.
- Page size.
- Total matching elements.
- Total pages.
- First/last/empty metadata.

---

## 8. Enum Sorting vs Business-Priority Sorting

A pageable request may allow:

    GET /api/v1/tasks?sortBy=priority&direction=asc

Do not assume that database sorting of enum values equals business importance.

### Technical sorting

Follows the database's comparison of the persisted representation.

### Business sorting

Follows a deliberate order, for example:

    URGENT → HIGH → MEDIUM → LOW

With string persistence, alphabetical ordering may not match urgency.

If the product requires a business order, implement it explicitly, for example with:
- JPQL/SQL CASE expression.
- A dedicated numeric rank.
- Another explicitly defined sorting strategy.

Document and test that behavior.

### Sort-field allow-list

Keep a sort-field allow-list in the service.

It prevents clients from sorting by arbitrary entity properties, including internal relationships.

Retain a deterministic secondary sort such as:

    id ASC

This ensures records with equal primary sort values have a stable order across pages.

---

## 9. How the Feature Flows Through Layered Architecture

Typical request flow:

    HTTP request
        |
        v
    TaskController
        | receives request/query parameters
        v
    TaskService
        | applies defaults, validates rules,
        | resolves current user and builds Pageable
        v
    TaskRepository
        | owner-scoped page and count queries
        v
    Database
        |
        v
    Page<Task>
        |
        v
    TaskPageResponse / TaskResponse
        |
        v
    HTTP response

### Task creation flow

1. The controller receives `CreateTaskRequest`.
2. The service resolves the authenticated user.
3. The service applies MEDIUM if priority was omitted.
4. The service creates the task with the server-resolved owner.
5. The repository persists the entity.
6. The saved entity is mapped to `TaskResponse`.
7. The API returns the response.

Keep controllers thin.

Business defaults and ownership rules belong in the service/domain layer, not in client-controlled input.

---

## 10. Testing Strategy

Testing should cover repository, service, and controller integration layers.

### Repository Tests

- Save and retrieve every priority.
- Verify string enum persistence if using EnumType.STRING.
- Filter by priority.
- Combine priority with status and title search.
- Combine priority with due-date filters.
- Verify owner isolation.
- Verify page content and total count use the same filters and owner scope.

### Service Tests

- Creating without priority applies MEDIUM.
- Creating with an explicit priority preserves it.
- Updating changes priority according to the API contract.
- Existing pagination, sorting, date validation, and ownership checks remain intact.

### Controller Integration Tests

- Valid priority is accepted in request JSON.
- Omitted create priority gets the documented default.
- Response JSON includes priority.
- PUT updates priority.
- Each priority filter works.
- Combined filters work.
- Invalid priority in query parameters and JSON produces the established error response.
- Pagination metadata reflects the filtered count.
- Cross-user isolation still holds.

**Important:** Test observable behavior, not merely whether a method was called.

---

## 11. Common Mistakes to Avoid

1. Using EnumType.ORDINAL without considering enum reordering.
2. Assuming nullable = false creates a default value.
3. Defaulting every update to MEDIUM and unintentionally overwriting an existing priority.
4. Forgetting to apply filters to the count query.
5. Dropping the owner predicate when adding a new filter.
6. Assuming alphabetical enum order represents urgency.
7. Allowing arbitrary sort fields.
8. Returning JPA entities directly instead of DTOs.
9. Testing filters individually but not in combination.
10. Changing existing API behavior unintentionally when adding an optional field.

---

## 12. Interview Questions and Answers

### Q1. What is an enum in Java?

An enum is a special Java type that defines a fixed set of constants. It is useful for values such as task status and priority.

### Q2. Why use an enum instead of a String for task priority?

An enum restricts values to a predefined set and provides compile-time type safety.

A String can contain arbitrary values unless carefully validated.

### Q3. What is the difference between EnumType.STRING and EnumType.ORDINAL?

STRING stores the enum constant's name.

ORDINAL stores its numeric position.

STRING is generally safer when constants may be reordered.

### Q4. Does @Column(nullable = false) set a default value?

No.

It declares a non-null column constraint. Application code or a database default must assign the value.

### Q5. Where should the default priority be applied?

The application should apply it consistently, commonly during task creation in the service and/or through entity initialization.

All creation paths should produce a valid value.

### Q6. Why might create allow an omitted priority while update requires it?

Creation can apply a documented default.

A full update can require a complete representation.

A partial update should leave omitted fields unchanged.

### Q7. How does Spring convert priority=HIGH to an enum?

Spring MVC uses its conversion facilities to convert the query string into the matching enum constant.

An unknown value causes a conversion/binding error.

### Q8. Why must the count query include the same filters and owner condition?

The count is part of pagination metadata.

If it ignores filters or counts another user's tasks, the metadata is inaccurate and may leak information.

### Q9. Does sorting by an enum automatically sort by business priority?

No.

It follows database comparison behavior.

A required business order such as URGENT, HIGH, MEDIUM, LOW must be explicitly implemented and tested.

### Q10. Why keep a secondary sort field such as ID?

It provides deterministic ordering when multiple tasks share the same primary sort value, making pagination more stable.

### Q11. Why use a response DTO instead of returning Task?

A DTO controls exposed fields, avoids accidental relationship serialization, and separates the API contract from the persistence model.

### Q12. How do you prevent priority filtering from exposing another user's tasks?

Include the authenticated user's ID in the repository query independently of optional filters.

Obtain that ID from the security context, not the request.

---

## 13. Phase 5.4 Revision Checklist

- [ ] TaskPriority contains the supported constants.
- [ ] Task maps priority with EnumType.STRING.
- [ ] Persisted priority is non-null.
- [ ] Omitted create priority follows the documented MEDIUM default.
- [ ] Update semantics are explicit and tested.
- [ ] TaskResponse exposes priority but not entity relationships.
- [ ] Priority filtering combines with existing filters.
- [ ] List and count queries remain owner-scoped.
- [ ] Priority sorting behavior is documented and tested.
- [ ] Sort-field allow-list and ID tie-breaker remain intact.
- [ ] Invalid query and JSON enum values produce consistent client errors.
- [ ] Existing CRUD, JWT, OAuth2, and ownership tests still pass.
- [ ] learning_concepts.md is appended to, not overwritten.
- [ ] Full Maven test suite and git diff --check pass.

---

## 14. What You Should Be Able to Explain After This Phase

1. Why TaskPriority is an enum.
2. How @Enumerated(EnumType.STRING) stores enum values.
3. Why a non-null constraint and a default value are separate concerns.
4. How a request DTO carries an optional enum value.
5. How Spring converts query parameters into enums.
6. How the service applies defaults and preserves ownership.
7. How optional filters combine in a paginated repository query.
8. Why page-content and count queries must use the same owner scope.
9. Why enum sorting may not equal business-priority sorting.
10. How integration tests verify the API end to end.

**Learning goal:** Be able to trace a priority value from incoming HTTP JSON/query parameters, through the controller and service, into the JPA entity and database, and back out through the response DTO.

---

## 15. Phase 6.1 — PostgreSQL Integration

### PostgreSQL JDBC driver and DataSource

The PostgreSQL JDBC driver (`org.postgresql:postgresql`) lets Spring Boot create a JDBC `DataSource` for a PostgreSQL JDBC URL. Its version comes from Spring Boot's dependency management, so the project does not pin a separate driver version. H2 remains on the runtime classpath and is still the default for local development and the normal automated test suite.

Spring Boot reads the active `application-{profile}.properties` file in addition to the base `application.properties`. The `postgres` profile requires `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD`; it deliberately has no H2 fallback. If any variable is absent, configuration resolution fails at startup rather than silently selecting another database. Never put real credentials in source control.

### Hibernate schema behavior

`spring.jpa.hibernate.ddl-auto` controls Hibernate schema handling:

- `none`: do not manage the schema.
- `validate`: compare mapped entities with an existing schema and fail on mismatches; do not create or drop tables.
- `update`: attempt to align an existing schema with entity mappings. This is a temporary local-development convenience, not a migration mechanism.
- `create`: create the schema at startup, replacing existing tables as needed.
- `create-drop`: create a schema at startup and drop it when the application stops.

The H2 default retains `create-drop` for isolated development/tests. The `postgres` profile uses `update` only to make local PostgreSQL exploration practical during this phase. The existing `prod` profile uses `validate` and disables SQL output; do not deploy the local `postgres` profile as production. `create` and `create-drop` can destroy persistent production data, while `update` is not a reviewable or reliable upgrade path. This phase is **not migration-ready**: Flyway and managed schema migrations are planned for Phase 6.2.

SQL statement logging is disabled in the PostgreSQL profile. H2 and PostgreSQL are both relational databases, but they differ in SQL behavior, type handling, indexes, constraints, and transaction semantics; a successful H2 test run alone does not establish PostgreSQL compatibility.

### Run PostgreSQL locally (PowerShell)

From the `task-management-api` directory, provide a local username and password without adding them to project files:

```powershell
$env:DATABASE_URL = "jdbc:postgresql://localhost:5432/taskmanager"
$env:DATABASE_USERNAME = "taskmanager"
$env:DATABASE_PASSWORD = Read-Host "Local PostgreSQL password"
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$rng.Dispose()
```

The API also requires a Base64-encoded `JWT_SECRET` with at least 32 random bytes; the commands above create a local-only key in the current PowerShell session.

If PostgreSQL is not already running, a local Docker database can be started using those same environment values:

```powershell
docker run --name taskmanager-postgres -e POSTGRES_DB=taskmanager -e POSTGRES_USER=$env:DATABASE_USERNAME -e POSTGRES_PASSWORD=$env:DATABASE_PASSWORD -p 5432:5432 -d postgres:16
```

Then, still from the `task-management-api` directory, start the application with the development PostgreSQL profile:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

For a non-Docker PostgreSQL server, create the database and user before starting the application. The Docker command creates them and sets the user's password for a fresh local container. Stop/remove only that named local container when finished with `docker stop taskmanager-postgres` and `docker rm taskmanager-postgres`. For a production deployment, set the same three database variables and activate the `prod` profile; it validates an existing schema instead of generating one. Do not use the example local database password as a production secret.

### PostgreSQL integration testing

`PostgreSqlIntegrationTest` runs against a disposable PostgreSQL container through Testcontainers and exercises Spring's PostgreSQL DataSource/JPA startup, user and task persistence, string enum round-tripping, task ownership, and filtered pagination. It is opt-in so the normal H2 test suite never requires Docker. Run the PostgreSQL test from `task-management-api` with Docker available:

```powershell
.\mvnw.cmd -Dtest=PostgreSqlIntegrationTest -DrunPostgresIntegration=true test
```

The test uses container-generated credentials and does not require production secrets. The full Maven test suite uses the H2 test configuration; the PostgreSQL test runs only when `runPostgresIntegration=true` is explicitly supplied.

---

## 16. Phase 6.2 — Flyway Database Migrations

### Why migrations

A database migration is a versioned, reviewable change to a database schema. Flyway applies migrations in order and records their outcome in `flyway_schema_history`. This replaces Hibernate's `update`/`create-drop` schema mutation with an explicit schema contract, while Hibernate `validate` checks that entity mappings agree with the migrated schema at application startup.

Versioned migration files use `V<version>__<description>.sql`; versions increase and applied migrations must not be edited. This phase adds:

- `V1__create_initial_schema.sql`: the five existing entity tables and their mapped constraints/indexes, representing the schema before task priority.
- `V2__add_task_priority.sql`: adds/backfills the priority column and enforces its default, non-null, and allowed-value constraints.

On a new empty H2 or PostgreSQL database, Flyway creates its history table and applies V1 followed by V2 before Hibernate initializes. The normal H2 tests use the same path. The app enables Flyway, disables automatic baseline and Flyway clean, and configures Hibernate `validate`; it does not use `update` or `create-drop` for startup. Flyway's H2 support is supplied by `flyway-core`, and PostgreSQL support by the Spring Boot-managed `flyway-database-postgresql` dependency.

### Schema represented by the migrations

V1 creates:

- `users`: identity `BIGINT` primary key; required name, email, password hash, enabled flag and creation time; unique email.
- `tasks`: identity key; required title, status, timestamps and owner; optional description/due date; foreign key to `users`. Priority is intentionally added in V2.
- `oauth_accounts`: identity key; required owner, provider, provider user ID and creation time; unique `(provider, provider_user_id)`; owner foreign key and owner index. Provider is stored as a string.
- `password_reset_tokens`: identity key; required owner, token hash, creation and expiration times; optional used time; unique token hash, owner foreign key and token-hash index.
- `refresh_tokens`: identity key; required owner, token hash, family ID, creation/expiration times and version; optional revoked time and replaced-token ID; unique token hash, owner foreign key, and hash/family/owner indexes. `replaced_by_token_id` is a scalar mapping, not an entity relationship, so no foreign key is declared for it.

The migrations use SQL `TIMESTAMP` for `LocalDateTime`, `DATE` for `LocalDate`, `BOOLEAN` for enabled, and `BIGINT` identity keys for the existing `GenerationType.IDENTITY` IDs. String enums remain `VARCHAR`; task status and OAuth provider have no database enum type. `TaskPriority` remains persisted using `EnumType.STRING`.

### Existing-task priority backfill

V2's actual statements are:

```sql
ALTER TABLE tasks
    ADD COLUMN IF NOT EXISTS priority VARCHAR(255) DEFAULT 'MEDIUM';

UPDATE tasks
SET priority = 'MEDIUM'
WHERE priority IS NULL;

ALTER TABLE tasks
    ALTER COLUMN priority SET DEFAULT 'MEDIUM';

ALTER TABLE tasks
    ALTER COLUMN priority SET NOT NULL;

ALTER TABLE tasks
    ADD CONSTRAINT ck_tasks_priority
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'));
```

The `IF NOT EXISTS` form also supports a verified pre-Flyway Phase 6.1 database where Hibernate `update` may already have added the priority column. Existing NULL values are explicitly backfilled, and the migration guarantees a database default, `NOT NULL`, and the four allowed string values. It does not drop tables or rewrite non-NULL priorities.

### Existing database baselining

Automatic baseline-on-migrate is deliberately disabled. A non-empty database without Flyway history must not be assumed to match a migration: Flyway will stop rather than silently mark it as migrated.

Before adopting any existing database, take and verify a restorable backup, stop application writers, inspect the schema and data read-only, and compare every table, column, type, identity, nullability rule, foreign key, unique constraint, index, and task priority rule to the migration state. Never baseline an unknown or partial schema. The local PostgreSQL service was not inspectable during this phase because its server rejected the read-only connection without credentials; no baseline or database changes were made there.

For a verified database matching the V1 base schema (the five V1 tables/columns/constraints/indexes, with no incompatible objects), set Flyway's `baselineVersion` to `1` for the one-time `baseline` operation. If that schema already has a Phase 6.1 `priority` column, verify that it is a compatible string column and has no conflicting priority constraint first. The application then applies V2, including the priority backfill and constraints.

For a verified database that already exactly matches the final V2 state—including the V2 default, non-null property, and check constraint—baseline at version `2` so neither migration is replayed. A baseline only records history; it does not repair or validate a schema. Supply the JDBC URL and credentials to the Flyway CLI/API via an approved secret mechanism; do not put passwords in source control or command history. Record and inspect the resulting `flyway_schema_history` baseline row before starting the application. If a database matches neither version, stop and prepare a reviewed, database-specific reconciliation plan rather than forcing a baseline.

### Migration tests

`FlywayMigrationIntegrationTest` boots the application with H2, checks that Flyway applied V1/V2 and created all five entity tables, checks declared constraints and indexes, and persists/reloads a task through JPA. Since Spring's context starts Hibernate with `ddl-auto=validate`, successful startup also verifies the migrated schema against all entity mappings. It checks the stored enum string, owner-scoped repository lookup, non-null priority column, and rejection of values outside the priority enum.

`TaskPriorityMigrationBackfillTest` applies V1 to a separate in-memory H2 database, inserts a pre-priority task, applies V2, and verifies that the task is `MEDIUM` and priority is non-null. Run the tests with the normal H2 suite:

```powershell
.\mvnw.cmd -Dtest=FlywayMigrationIntegrationTest,TaskPriorityMigrationBackfillTest test
```

The PostgreSQL Testcontainers test remains opt-in as described in Phase 6.1. A successful H2 migration test does not prove PostgreSQL execution; Docker/Testcontainers availability is required to verify migrations on PostgreSQL.

---------------------------------------------------------------------------------

# Phase 6.1 — PostgreSQL Integration

## 1. Introduction

Phase 6.1 introduced PostgreSQL support into the Task Management REST API while preserving the existing H2 development and test workflow.

Technology stack:
- Java 21
- Spring Boot 3.5.7
- Maven
- Spring Data JPA
- Hibernate
- PostgreSQL
- H2

### Objectives achieved

- Added PostgreSQL JDBC driver.
- Retained H2.
- Created `application-postgres.properties`.
- Externalized database connection details.
- Introduced environment-specific database configuration.
- Added an opt-in PostgreSQL Testcontainers integration test.
- Preserved existing authentication, authorization and task functionality.

**Verification:** 135 tests passed, 0 failures, 0 errors and 1 skipped. The PostgreSQL Testcontainers test could not execute because of Docker communication issues. PostgreSQL persistence is therefore not yet verified.

---

## 2. H2 vs PostgreSQL

### H2

H2 is a lightweight relational database commonly used for development and testing.

- Can run in memory.
- Does not require a separate database server for an in-memory setup.
- Starts quickly.
- Useful for automated tests.

Example:

```properties
spring.datasource.url=jdbc:h2:mem:taskmanagerdb
```

### PostgreSQL

PostgreSQL is a full-featured relational database management system.

- Runs as a database server.
- Stores data persistently.
- Supports transactions, constraints and indexes.
- Suitable for production workloads.

Example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/taskmanager
```

### Why support both?

| Database | Purpose |
|---|---|
| H2 | Fast development and ordinary tests |
| PostgreSQL | Local integration and production database |

H2 tests do not guarantee complete PostgreSQL compatibility. PostgreSQL integration testing is also necessary.

---

## 3. JDBC and PostgreSQL JDBC Driver

JDBC stands for Java Database Connectivity. It is the standard Java API for communicating with relational databases.

A JDBC driver implements database-specific communication.

PostgreSQL dependency:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

The driver uses Spring Boot dependency management rather than a manually specified version. The reported resolved version was `42.7.8`.

### Runtime communication flow

```text
Spring Boot Application
        |
        v
Spring Data JPA
        |
        v
Hibernate
        |
        v
JDBC / DataSource
        |
        v
PostgreSQL JDBC Driver
        |
        v
PostgreSQL Database
```

---

## 4. DataSource Configuration

A DataSource represents the application's configured source of database connections.

The PostgreSQL profile contains:

```properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
```

| Property | Purpose |
|---|---|
| `spring.datasource.url` | Database JDBC URL |
| `spring.datasource.username` | Database username |
| `spring.datasource.password` | Database password |
| `spring.datasource.driver-class-name` | JDBC driver class |

Example:

```text
DATABASE_URL=jdbc:postgresql://localhost:5432/taskmanager
DATABASE_USERNAME=taskmanager_user
DATABASE_PASSWORD=your_local_password
```

### Why no fallback values?

The PostgreSQL profile does not silently fall back to H2 or default credentials.

If required configuration is missing, startup should fail rather than connecting to an unintended database.

---

## 5. Environment Variables and Secret Management

Environment variables allow configuration to differ between environments without modifying source code.

PowerShell example:

```powershell
$env:DATABASE_URL = "jdbc:postgresql://localhost:5432/taskmanager"
$env:DATABASE_USERNAME = "taskmanager_user"
$env:DATABASE_PASSWORD = "your_local_password"
```

The application also requires `JWT_SECRET` for JWT signing.

Security rules:
- Never commit database passwords.
- Never commit JWT signing secrets.
- Do not hardcode production credentials.
- Do not reuse development credentials in production.

Environment variables are a configuration mechanism, not a complete secret-management platform.

---

## 6. Spring Profiles

Spring profiles allow different configuration settings to be activated for different environments.

Example:

```text
src/main/resources/
    application.properties
    application-postgres.properties
    application-prod.properties
```

The base `application.properties` provides common settings. Profile-specific files override or supplement them.

### PostgreSQL profile

The `postgres` profile configures:
- PostgreSQL connection properties.
- PostgreSQL JDBC driver.
- Temporary local-development Hibernate schema update behavior.
- SQL logging disabled.
- H2 console disabled.

### Production profile

The existing production profile uses:

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
```

### Activate PostgreSQL profile

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

Set all required environment variables before starting the application.

**Important:** The `postgres` profile is for local PostgreSQL development in this phase. It uses Hibernate `update` temporarily. It is not the production profile.

---

## 7. Hibernate Schema Generation: ddl-auto

Hibernate can manage database schema changes based on JPA entity mappings.

The property is:

```properties
spring.jpa.hibernate.ddl-auto=...
```

| Value | Behavior |
|---|---|
| `none` | Hibernate does not manage the schema |
| `validate` | Checks schema compatibility |
| `update` | Attempts to update the schema |
| `create` | Creates the schema at startup |
| `create-drop` | Creates at startup and drops at shutdown |

### Why create-drop is dangerous

If used against a persistent production database, Hibernate can drop the schema when the application shuts down, potentially destroying data.

### Why update is temporary

The PostgreSQL development profile uses:

```properties
spring.jpa.hibernate.ddl-auto=update
```

This is a temporary local-development convenience.

Hibernate automatic updates are not a replacement for controlled, versioned database migrations.

### Why production uses validate

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Hibernate checks schema compatibility without automatically creating, changing or dropping tables.

After Flyway is introduced, Flyway will manage schema changes and Hibernate will validate the resulting schema.

---

## 8. SQL Logging

The PostgreSQL profile uses:

```properties
spring.jpa.show-sql=false
```

SQL logging can help developers understand Hibernate-generated SQL, but it can make logs noisy and potentially expose query details or sensitive values.

SQL logging should be configured deliberately for each environment.

---

## 9. PostgreSQL Integration Testing with Testcontainers

Testcontainers is a testing library that starts disposable services, such as PostgreSQL, in containers for integration tests.

Example:

```java
@Testcontainers
@SpringBootTest
@ActiveProfiles("postgres")
class PostgreSqlIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");
}
```

### What the test verifies when executed

1. DataSource availability.
2. Actual PostgreSQL connection.
3. User persistence and retrieval.
4. Task persistence and retrieval.
5. TaskPriority enum persistence.
6. Task-to-user relationship.
7. Owner-scoped filtering.
8. Combined status, priority, title and due-date filtering.
9. Pagination metadata and results.

### Dynamic properties

`@DynamicPropertySource` supplies Testcontainers connection details to Spring.

This avoids hardcoded database credentials in the test.

### Why the test is opt-in

The test uses:

```java
@EnabledIfSystemProperty(
    named = "runPostgresIntegration",
    matches = "true"
)
```

This keeps the ordinary H2 suite independent of Docker availability.

### Actual verification limitation

Testcontainers received an empty Docker named-pipe response.

Consequently:
- The H2 suite passed.
- The PostgreSQL test was skipped in the default run.
- The separate PostgreSQL attempt could not start its container.
- PostgreSQL persistence assertions were not successfully executed.

Do not describe PostgreSQL integration as verified until the test actually runs and passes.

---

## 10. Local PostgreSQL Setup

A local PostgreSQL server or container is required.

Example Docker command:

```powershell
docker run --name taskmanager-postgres `
  -e POSTGRES_DB=taskmanager `
  -e POSTGRES_USER=taskmanager_user `
  -e POSTGRES_PASSWORD=your_local_password `
  -p 5432:5432 `
  -d postgres:16
```

Configure connection variables:

```powershell
$env:DATABASE_URL = "jdbc:postgresql://localhost:5432/taskmanager"
$env:DATABASE_USERNAME = "taskmanager_user"
$env:DATABASE_PASSWORD = "your_local_password"
```

Also configure the required JWT secret.

Start the application:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

These are local development credentials only. Do not use example credentials in production.

---

## 11. H2 and PostgreSQL Testing Strategy

### H2 tests

Advantages:
- Fast execution.
- No separate database server required.
- Convenient for repository and application tests.
- No Docker requirement.

### PostgreSQL integration tests

Advantages:
- Verify PostgreSQL connection behavior.
- Exercise real PostgreSQL persistence.
- Help identify database differences that H2 may not reveal.

Both approaches are useful. H2 provides a fast default test workflow, while PostgreSQL integration tests provide database-engine-specific confidence.

---

## 12. Important Design Rules

1. Keep database credentials outside source code.
2. Do not silently fall back to H2 when the PostgreSQL profile is selected.
3. Do not use `create-drop` against a persistent production database.
4. Treat Hibernate `update` as temporary local-development behavior.
5. Use Hibernate `validate` when migrations manage the schema.
6. Keep Docker-dependent integration tests separate from the default H2 workflow.
7. Never claim PostgreSQL integration verification unless the test actually passes.
8. Preserve authentication, OAuth2, refresh-token and task-ownership behavior.
9. Do not drop an existing database to make schema configuration easier.
10. Introduce version-controlled migrations before treating PostgreSQL as production-ready.

---

## 13. Phase 6.1 Implementation Summary

### Files created

- `application-postgres.properties`
- `PostgreSqlIntegrationTest.java`

### Files modified

- `pom.xml`
- `learning_concepts.md`

### Dependencies added

- PostgreSQL JDBC driver.
- Testcontainers JUnit Jupiter.
- Testcontainers PostgreSQL.

### Test results

| Verification | Result |
|---|---|
| Full default Maven suite | 135 tests |
| Failures | 0 |
| Errors | 0 |
| Skipped | 1 |
| IDE compilation | Passed |
| PostgreSQL Testcontainers execution | Blocked by Docker communication |
| pom.xml diff check | Passed |
| learning_concepts.md diff check | Existing trailing whitespace in earlier Phase 5.4 content |

No authentication, security, entity, repository or task business logic was intentionally changed in Phase 6.1.

---

## 14. Interview Questions and Answers

### Q1. What is PostgreSQL?

PostgreSQL is an open-source relational database management system used to store structured data. It supports SQL, transactions, constraints, indexes and relationships.

### Q2. What is JDBC?

JDBC is the standard Java API for connecting to relational databases and executing SQL operations.

### Q3. Why do we need a PostgreSQL JDBC driver?

It implements JDBC communication for PostgreSQL, allowing Java applications and Hibernate to communicate with the PostgreSQL server.

### Q4. What is a DataSource?

A DataSource represents the configured source of database connections. Spring Boot creates it using datasource properties.

### Q5. What are Spring profiles?

Spring profiles activate different configurations for different environments, such as development, PostgreSQL local development and production.

### Q6. Why use environment variables for database credentials?

They keep credentials outside source code and allow the same application artifact to use different configuration in different environments.

### Q7. What is the difference between H2 and PostgreSQL?

H2 is a lightweight database often used for development and testing. PostgreSQL is a full database server commonly used for persistent production workloads.

### Q8. What does ddl-auto=update do?

It asks Hibernate to attempt to update the database schema based on entity mappings. It is convenient for local development but is not a controlled migration strategy.

### Q9. Why is create-drop dangerous in production?

It creates the schema at startup and drops it at shutdown, which can destroy persistent data.

### Q10. What does ddl-auto=validate do?

It checks whether the database schema is compatible with entity mappings without automatically changing it.

### Q11. What is Testcontainers?

Testcontainers runs disposable dependencies, such as PostgreSQL, in containers so integration tests can use real services.

### Q12. Why keep H2 tests if PostgreSQL is supported?

H2 allows fast tests without Docker or a database server. PostgreSQL integration tests separately verify behavior against the intended database engine.

### Q13. Does a successful H2 test suite prove PostgreSQL compatibility?

No. H2 and PostgreSQL can differ in SQL behavior, data types and database-specific features. PostgreSQL integration tests are needed for direct verification.

### Q14. Why should production avoid Hibernate automatic schema updates?

Production schema changes should be explicit, reviewed, versioned and repeatable. Flyway provides this control.

---

## 15. Next Phase — Flyway Database Migrations

Phase 6.2 will introduce version-controlled SQL migrations.

Topics:
- Flyway dependencies and configuration.
- Initial database schema creation.
- Migration versioning and history.
- Existing schema baselining.
- Task priority backfill for existing records.
- Foreign keys, constraints and indexes.
- Hibernate schema validation after migration.

The goal is to make database schema changes explicit and safe without losing existing application data.



# Phase 6.2 — Flyway Database Migrations

## 1. Introduction

Phase 6.2 introduces Flyway for version-controlled database schema management.

Previously, Hibernate `ddl-auto=update` was used for local PostgreSQL schema management. Although convenient during development, automatic updates do not provide an explicit, reviewable migration history.

Flyway manages schema changes through ordered SQL migration files.

### Objectives

- Add Flyway support.
- Create the initial database schema.
- Introduce versioned SQL migrations.
- Backfill existing task records with MEDIUM priority.
- Make Flyway responsible for schema changes.
- Configure Hibernate to validate the schema.
- Test fresh database creation and schema upgrades.
- Avoid automatic baselining and destructive database cleanup.

## 2. What is a database migration?

A database migration is a controlled change to a database schema or its data.

Examples:
- Creating a table.
- Adding a column.
- Adding a foreign key.
- Creating an index.
- Changing a constraint.
- Updating existing records.

Migrations ensure that database changes are repeatable, versioned and consistent across environments.

## 3. What is Flyway?

Flyway is a database migration tool that manages schema evolution through ordered SQL files.

Simplified lifecycle:

1. Application starts.
2. Flyway connects to the database.
3. Flyway checks migration history.
4. It discovers migration files.
5. It identifies pending migrations.
6. It executes migrations in version order.
7. It records successful migrations.
8. Hibernate validates the resulting schema.

**Flyway changes the schema; Hibernate validates it.**

## 4. Versioned migration files

Flyway uses a naming convention:

```text
V1__create_initial_schema.sql
V2__add_task_priority.sql
V3__add_future_feature.sql
```

General format:

```text
V<version>__<description>.sql
```

- V identifies a versioned migration.
- Version determines execution order.
- Double underscore separates version and description.
- Description explains the change.

Migration versions should be unique. Avoid changing migrations already applied to shared databases.

## 5. Flyway schema history

Flyway maintains a metadata table commonly named:

```text
flyway_schema_history
```

It records migration versions, scripts, checksums, execution status and timing information.

When the application starts again, Flyway checks this history and identifies migrations already applied.

Do not manually delete migration-history records to bypass migration problems.

## 6. Project migration sequence

The implementation represents five entity tables:

1. users
2. tasks
3. oauth_accounts
4. password_reset_tokens
5. refresh_tokens

The implementation report also identifies four user foreign keys, declared unique constraints and indexes, and string-backed task/status/OAuth enums.

### V1 — Initial schema

V1 creates the database schema before task priority was introduced.

### V2 — Add task priority

Phase 5.4 introduced:

```text
LOW
MEDIUM
HIGH
URGENT
```

V2:
1. Adds the priority column.
2. Backfills existing tasks with MEDIUM.
3. Sets a default for future inserts.
4. Enforces NOT NULL.
5. Restricts values to supported priorities.

### Why MEDIUM?

Existing tasks were created before priority management existed.

MEDIUM is also the application's default priority when a newly created task does not specify one.

## 7. Data backfilling

A backfill updates existing records so they comply with a new schema.

Conceptual SQL:

```sql
ALTER TABLE tasks ADD COLUMN priority VARCHAR(20);

UPDATE tasks
SET priority = 'MEDIUM'
WHERE priority IS NULL;

ALTER TABLE tasks
ALTER COLUMN priority SET DEFAULT 'MEDIUM';

ALTER TABLE tasks
ALTER COLUMN priority SET NOT NULL;
```

The actual migration SQL remains the source of truth.

The important sequence is:

- Add the new column.
- Populate existing records.
- Apply the default.
- Enforce NOT NULL.

## 8. Constraints and indexes

**Primary key:** Uniquely identifies each database row.

**Foreign key:** Enforces relationships between tables.

**Unique constraint:** Prevents duplicate values.

**NOT NULL:** Requires a column to contain a value.

**Check constraint:** Restricts values according to a condition.

**Index:** Helps the database locate rows efficiently for supported queries.

The migration should preserve the constraints and indexes defined by the application's actual entity mappings.

## 9. String-backed enums

JPA:

```java
@Enumerated(EnumType.STRING)
```

stores enum names as text.

Examples:

```text
TaskPriority.HIGH   -> "HIGH"
TaskPriority.URGENT -> "URGENT"
```

This is generally safer than ordinal persistence because reordering enum constants does not silently change the meaning of stored numeric values.

## 10. Hibernate update vs validate

Before Flyway, the PostgreSQL profile used:

```properties
spring.jpa.hibernate.ddl-auto=update
```

After Flyway, the intended configuration is:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

| Setting | Behavior |
|---|---|
| update | Hibernate attempts to change the schema |
| validate | Hibernate checks schema compatibility |
| create | Creates the schema |
| create-drop | Creates at startup and drops at shutdown |
| none | Hibernate does not manage the schema |

Production should not rely on automatic Hibernate schema updates.

Flyway manages schema changes, while Hibernate validates the result.

## 11. Flyway baselining

Baselining tells Flyway that an existing database should be treated as being at a particular migration version.

It is useful when adopting Flyway for a database that already has a schema.

However, automatic baselining is risky if the existing database does not actually match the selected migration version.

Safe process:

1. Back up the database.
2. Inspect the existing schema.
3. Compare tables, columns, constraints and indexes.
4. Confirm the priority column and existing data state.
5. Select the baseline version only after verification.
6. Apply later migrations.
7. Run Hibernate validation.

Do not drop an existing database just to make Flyway work.

The implementation disables automatic baselining because the local PostgreSQL schema could not be inspected.

## 12. Flyway clean

Flyway clean drops database objects in the configured schema.

It is destructive.

Rules:
- Never use clean against production.
- Do not drop tables to fix migration errors.
- Do not delete migration history to force migrations to rerun.
- Diagnose schema differences first.

The implementation keeps Flyway clean disabled.

## 13. Migration testing

The phase adds H2 migration tests for:

- Fresh schema creation.
- Expected constraints.
- JPA persistence.
- V1-to-V2 migration.
- Existing task priority backfill.

Fresh migration tests verify that a new database can be created.

Upgrade tests verify that existing data survives schema changes.

H2 tests do not prove PostgreSQL compatibility. PostgreSQL migration execution must also be tested against PostgreSQL.

## 14. Important design rules

1. Treat migration files as versioned application assets.
2. Do not casually edit migrations already applied to shared databases.
3. Add a new migration for every new schema change.
4. Backfill existing rows before enforcing new non-null constraints.
5. Keep database constraints aligned with application rules.
6. Let Flyway manage schema changes.
7. Let Hibernate validate the migrated schema.
8. Never automatically baseline an uninspected database.
9. Keep Flyway clean disabled outside disposable test environments.
10. Test fresh schema creation and upgrades.
11. Never claim PostgreSQL migration verification unless PostgreSQL tests actually execute successfully.

## 15. Implementation summary

### Schema tables

- users
- tasks
- oauth_accounts
- password_reset_tokens
- refresh_tokens

### Migration sequence

| Migration | Responsibility |
|---|---|
| V1 | Create initial schema before task priority |
| V2 | Add priority, backfill MEDIUM, set default and NOT NULL, restrict values |

### Configuration

- Flyway manages schema changes.
- Hibernate validates the schema.
- Automatic baselining is disabled.
- Flyway clean is disabled.
- Existing database data is not intentionally dropped or recreated.

### Reported test results

| Verification | Result |
|---|---|
| Full Maven suite | 137 tests |
| Failures | 0 |
| Errors | 0 |
| Skipped | 1 |
| Focused migration tests | 2 passed |
| PostgreSQL migration execution | Not verified |
| Existing worktree | Preserved |

## 16. Interview questions

**Q1. What is Flyway?**

Flyway is a database migration tool that applies ordered SQL migration files and records which migrations have executed.

**Q2. Why use Flyway with Hibernate?**

Hibernate maps Java entities to database tables. Flyway provides explicit, version-controlled schema evolution, while Hibernate validates the result.

**Q3. What is a versioned migration?**

A uniquely numbered SQL file executed in migration order, such as V1__create_initial_schema.sql.

**Q4. What is flyway_schema_history?**

It is Flyway's metadata table containing migration versions, scripts, checksums and execution status.

**Q5. Why should an applied migration not be edited?**

Flyway uses migration history and checksums. Editing an applied migration can cause checksum mismatches and inconsistent environments.

**Q6. Why does V2 backfill existing tasks with MEDIUM?**

Those tasks were created before priority existed. MEDIUM gives every legacy task a valid default.

**Q7. What is baselining?**

Baselining marks an existing database as being at a particular migration version so Flyway can apply later migrations.

**Q8. Why is automatic baselining risky?**

The existing schema may not match the selected baseline, causing required migrations to be skipped.

**Q9. Why keep Flyway clean disabled?**

Clean drops database objects and can destroy application data.

**Q10. Do H2 migration tests prove PostgreSQL migrations work?**

No. PostgreSQL-specific migration execution must be tested against PostgreSQL.

## 17. Next phase

**Phase 6.3 — Production Configuration and Hardening**

Topics:
- Environment-specific profiles.
- Externalized secrets.
- Production database configuration.
- HikariCP connection pooling.
- Secure cookies and HTTPS settings.
- Explicit CORS configuration.
- Production logging and error handling.
- Startup validation.
- Authentication and OAuth2 regression testing.

## 18. Phase 6.3 — Production Configuration and Hardening

### Profiles and configuration precedence

Spring Boot loads `application.properties` as the shared baseline and then overlays `application-{profile}.properties` for each active profile. Higher-precedence sources such as environment variables and command-line arguments can override file values. Keep shared settings in the base file, database-specific settings in `postgres`, local-only conveniences in `dev`, and stricter deployment settings in `prod`.

| Profile | Intended use | Database and schema behavior |
|---|---|---|
| default | Existing lightweight H2 workflow | H2 in-memory URL; Flyway migrations; Hibernate `validate`; SQL output off; requires a developer-supplied JWT key |
| `dev` | Local development | H2 remains the default; H2 console and SQL logging enabled; a developer-supplied JWT key is required |
| `postgres` | Local PostgreSQL development | Requires PostgreSQL URL, username and password; Flyway enabled; Hibernate `validate`; SQL output off |
| `prod` | Production deployment | Requires PostgreSQL URL, username and password, JWT key and explicit CORS origins; Flyway enabled; Hibernate `validate`; SQL output off |

`ProductionDatabaseConfigurationValidator` rejects missing/blank database values, any non-PostgreSQL JDBC URL, and combining `prod` with the `dev` profile before production startup proceeds. `JwtService` separately rejects a missing, invalid Base64, or undersized JWT key. CORS configuration rejects missing, blank, or wildcard origins. OAuth provider credentials are only required when that provider is enabled; disabled Google and GitHub providers need no credentials.

### External secrets and production environment

Do not put deployment secrets in source control, command history, application logs, or exception messages. Inject them through the deployment platform's secret manager/environment. Required production variables:

| Variable | Requirement |
|---|---|
| `DATABASE_URL` | PostgreSQL JDBC URL, for example `jdbc:postgresql://db-host:5432/taskmanager` |
| `DATABASE_USERNAME` | Database role with only the permissions the app needs |
| `DATABASE_PASSWORD` | Non-blank password supplied by a secret manager |
| `JWT_SECRET` | Base64-encoded random key decoding to at least 32 bytes for HS256 |
| `CORS_ALLOWED_ORIGINS` | Explicit comma-separated frontend origins; no `*` |

Optional OAuth settings retain their existing names: `GOOGLE_OAUTH2_ENABLED`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GOOGLE_REDIRECT_URI`, and the corresponding `GITHUB_*` variables. The enable flags default to `false`; when enabled, the application fails startup if that provider's client ID or secret is blank. The secret values are not included in validation messages.

Example PowerShell setup for **local PostgreSQL development** (replace placeholders locally; do not use these examples as production secrets):

```powershell
$env:DATABASE_URL = 'jdbc:postgresql://localhost:5432/taskmanager'
$env:DATABASE_USERNAME = '<local-postgres-user>'
$env:DATABASE_PASSWORD = '<local-postgres-password>'
$env:JWT_SECRET = '<base64-random-key-decoding-to-at-least-32-bytes>'
$env:CORS_ALLOWED_ORIGINS = 'http://localhost:3000'
$env:GOOGLE_OAUTH2_ENABLED = 'false'
$env:GITHUB_OAUTH2_ENABLED = 'false'
.\task-management-api\mvnw.cmd -f task-management-api\pom.xml spring-boot:run -Dspring-boot.run.profiles=postgres
```

For local H2, use the `dev` profile and configure a local `JWT_SECRET`; the default datasource remains H2. `JWT_SECRET` is required in every running profile because JWT signing has no weak fallback. Do not copy production credentials into a local `.properties` file. `SESSION_COOKIE_SECURE` defaults to `false` for local HTTP and is set to `true` by the production profile.

### HikariCP connection pooling

Spring Boot's standard DataSource auto-configuration continues to create the HikariCP pool; no custom DataSource bean is introduced. Shared defaults are deliberately conservative and can be overridden through:

| Environment variable | Default | Hikari property |
|---|---:|---|
| `DB_POOL_MAX_SIZE` | `10` | `maximum-pool-size` |
| `DB_POOL_MIN_IDLE` | `2` | `minimum-idle` |
| `DB_POOL_CONNECTION_TIMEOUT_MS` | `30000` | `connection-timeout` |
| `DB_POOL_IDLE_TIMEOUT_MS` | `600000` | `idle-timeout` |
| `DB_POOL_MAX_LIFETIME_MS` | `1800000` | `max-lifetime` |
| `DB_POOL_VALIDATION_TIMEOUT_MS` | `5000` | `validation-timeout` |

Choose the maximum pool size against the database's total connection budget: across all application replicas, `replica count × maximum pool size` plus migration, monitoring, administrative and other clients must remain below the database limit, with headroom reserved. These defaults are not a guarantee of a particular hosting provider's capacity. Set pool values through deployment configuration and ensure `minimum-idle` does not exceed `maximum-pool-size`.

### Cookies, CORS and security headers

The current API returns access and refresh tokens in no-store JSON responses; it does not set or delete a refresh-token cookie. The servlet session cookie supports the OAuth authorization-request state and remains `HttpOnly` with `SameSite=Lax`. Lax allows the normal top-level GET OAuth callback while reducing cross-site cookie sending. Local HTTP keeps `Secure=false`; `prod` sets `Secure=true`, so deployments must serve HTTPS. Any future cookie-based token flow must define matching path, SameSite, Secure and HttpOnly settings for both issuance and deletion rather than changing the current token transport implicitly.

CORS remains explicit: configured origins only, GET/POST/PUT/DELETE/OPTIONS, Authorization and Content-Type headers, and `allowCredentials=false`. Wildcard origins are rejected. This matches the API's bearer-token integration and avoids credentialed wildcard CORS.

The existing security chain retains content-type sniffing protection, `X-Frame-Options: DENY`, and `Referrer-Policy: strict-origin-when-cross-origin`. HSTS remains configured with a one-year max age and `includeSubDomains`; Spring Security emits it only for requests recognized as HTTPS, so local HTTP does not receive the HSTS header. In TLS-terminating deployments, configure trusted proxy/forwarded-header handling at the deployment boundary and do not trust arbitrary client-supplied forwarding headers.

### Logging and startup responsibilities

SQL display is disabled in the shared, PostgreSQL and production configurations; the `dev` profile may enable it for local debugging. Production does not enable Hibernate SQL formatting output. Keep application/security logs useful without adding passwords, JWTs, refresh tokens, OAuth credentials or database credentials to log messages. Existing authentication/OAuth failures and unexpected API errors return generic client messages; unexpected task failures are logged server-side while the response contains no stack trace.

For persistent profiles, startup uses this order: connect to the configured PostgreSQL database, let Flyway validate and apply pending versioned migrations, then let Hibernate validate mapped entities against the resulting schema. Migration or schema-validation failures stop startup. `spring.flyway.clean-disabled=true` and `baseline-on-migrate=false` remain enforced; do not automatically baseline an unknown schema or drop data to make startup succeed. The PostgreSQL migration path has not been verified against a live PostgreSQL server or successful Docker/Testcontainers run in this phase.

Configuration and security tests verify that production rejects empty database settings and non-PostgreSQL URLs without exposing the supplied password, persistent profiles retain Flyway plus Hibernate validation, CORS remains explicit, local HTTP does not receive HSTS, OAuth credentials are required only for enabled providers, and local H2/session-cookie defaults remain available. Existing token, OAuth and task tests remain part of the full suite.

## 19. Phase 6.4 — OpenAPI and Swagger UI

### API documentation and OpenAPI

API documentation describes the HTTP contract clients can rely on: paths, methods, parameters, request bodies, response schemas, status codes, and authentication. OpenAPI is a language-neutral, machine-readable specification for that contract. It can drive interactive tools, generated clients, and contract checks. Swagger UI is a browser application that renders an OpenAPI document and lets developers try requests; it is a viewer and not the specification itself.

This application uses the Springdoc OpenAPI WebMVC UI starter compatible with Spring Boot 3. Springdoc discovers Spring MVC routes and DTO types, while `OpenApiConfig` supplies API metadata and an HTTP Bearer security scheme. Controller annotations add descriptions, response statuses, validation context, and security details. DTO `@Schema` annotations clarify public request/response fields and keep the API contract based on DTOs rather than persistence entities. Bean Validation constraints are reflected as schema constraints where Springdoc supports them.

### Endpoints and response contract

The documented JSON routes are:

| Area | Route | Authentication |
|---|---|---|
| Register | `POST /api/v1/auth/register` | Public |
| Login | `POST /api/v1/auth/login` | Public |
| Rotate refresh token | `POST /api/v1/auth/refresh` | Public; refresh token required in its request body |
| Logout | `POST /api/v1/auth/logout` | Public; revokes the submitted refresh-token session |
| Change password | `POST /api/v1/auth/change-password` | JWT access token |
| Request password reset | `POST /api/v1/auth/forgot-password` | Public |
| Reset password | `POST /api/v1/auth/reset-password` | Public; requires a valid reset token |
| Create task | `POST /api/v1/tasks` | JWT access token |
| List tasks | `GET /api/v1/tasks` | JWT access token |
| Get, update, delete task | `GET`, `PUT`, `DELETE /api/v1/tasks/{taskId}` | JWT access token |
| Sample message | `GET /api/v1/test` | Public |

The task list accepts `page` (default `0`, nonnegative), `size` (default `10`, range 1–100), `sortBy` (default `createdAt`; `createdAt`, `updatedAt`, `dueDate`, `title`, `status`, or `priority`), `direction` (default `desc`; `asc` or `desc`), optional title `search`, `status` (`PENDING`, `IN_PROGRESS`, `COMPLETED`), `priority` (`LOW`, `MEDIUM`, `HIGH`, `URGENT`), and inclusive ISO date filters `dueFrom` and `dueTo`. When both dates are provided, `dueFrom` must not be after `dueTo`. Results include task DTOs and page index, size, total count, total pages, and first/last/empty flags. Task ownership always comes from the authenticated principal, not a request parameter.

The API uses the existing `ApiErrorResponse` shape for documented errors. It contains a timestamp, HTTP status, safe message, request path, and optional validation details; it does not expose stack traces or internal exception data. Endpoint responses document success codes plus relevant `400`, `401`, and `404` errors. Authentication routes that do not require a JWT are deliberately not marked with a Bearer security requirement. A `401` means the request is unauthenticated or the credential is invalid; `403` means an authenticated caller is not allowed to perform an operation.

### Bearer authentication in Swagger UI

For protected routes, obtain an access token from login or refresh, select **Authorize**, and enter the access-token value. Swagger UI applies it as `Authorization: Bearer <access-token>`. Do not enter the refresh token: it is only submitted to the refresh/logout API routes, and is not accepted as a task API credential. Registration, login, refresh, logout, forgot-password, and reset-password remain public according to the current security configuration; password change and all task operations require the Bearer access token.

Google and GitHub OAuth sign-in are browser redirects rather than JSON endpoints. When a provider is enabled, the browser starts at `/oauth2/authorization/google` or `/oauth2/authorization/github`; the provider redirects back to `/login/oauth2/code/{registrationId}`. These Spring Security routes are not described as ordinary controller operations. They remain subject to the existing enabled-provider configuration and OAuth credentials.

### Documentation availability and security

The development defaults expose:

- Swagger UI: `http://localhost:8080/swagger-ui.html` (the UI is served under `/swagger-ui/`).
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`.

The shared and PostgreSQL development profiles enable both routes by default. Set `API_DOCS_ENABLED=false` to disable both OpenAPI JSON and Swagger UI. The production profile defaults that variable to `false`; explicitly setting `API_DOCS_ENABLED=true` turns on both surfaces and should be a deliberate deployment decision. Spring Security permits only the Springdoc route paths required for anonymous viewing; the docs feature itself must also be enabled, so disabling Springdoc removes the routes rather than leaving a permitted API description behind. Documentation settings do not alter authorization on application endpoints.

Do not publish internal APIs or sensitive examples in the generated specification. Never include passwords, password hashes, refresh-token hashes, signing keys, OAuth client secrets, or database credentials. Password request fields are write-only in schemas where applicable; task outputs use response DTOs and do not expose entity ownership relationships. Keep production documentation disabled unless there is a controlled reason to serve it.

### Testing generated documentation

Integration tests request `/v3/api-docs`, inspect the API title/version, Bearer scheme, endpoint paths, and security differences between public auth operations and protected routes, verify request/response schema generation, and check Swagger UI availability. A separate context with both Springdoc switches disabled verifies that the OpenAPI and UI routes return `404`. These H2-based checks test generated API documentation without calling Google, GitHub, or a PostgreSQL service.

### Interview questions

**Q1. What is the difference between OpenAPI and Swagger UI?**

OpenAPI is the machine-readable API contract. Swagger UI is an interactive client that renders that contract.

**Q2. Why should API documentation use DTOs instead of JPA entities?**

DTOs define the public contract and avoid accidentally exposing persistence relationships, internal fields, or data that clients must not control.

**Q3. Why is the access token entered in Swagger's Authorize dialog rather than the refresh token?**

Protected resource requests authenticate with short-lived access tokens. Refresh tokens have a separate rotation/revocation lifecycle and are not resource credentials.

**Q4. Why must public login operations not inherit a global JWT requirement?**

Clients need to call login before possessing a token. Security requirements should be attached to protected operations, not indiscriminately applied to public routes.

**Q5. Why disable documentation by default in production?**

The specification can reveal endpoint structure and accepted input. Restricting or disabling it reduces unnecessary exposure and keeps production availability an explicit deployment decision.

**Q6. Does documenting OAuth2 as a browser flow make it a JSON REST endpoint?**

No. The authorization start and callback are redirects handled by Spring Security; they are separate from controller endpoints that accept and return JSON.


-----------------------------------------------------------------------------------------------


# Phase 6.4 — OpenAPI and Swagger UI

## 1. Phase Objective

The objective of Phase 6.4 was to implement professional API documentation for the Task Management REST API using OpenAPI 3 and Swagger UI.

The documentation allows developers to understand, explore and test REST endpoints directly through a browser without having to inspect the controller source code manually.

The implementation includes:

- OpenAPI 3 specification generation.
- Swagger UI integration.
- JWT Bearer authentication documentation.
- Authentication endpoint documentation.
- Task management endpoint documentation.
- Request and response DTO schemas.
- HTTP response and error documentation.
- API documentation availability controlled by Spring profiles and environment variables.
- Integration tests for enabled and disabled documentation.

---

## 2. What is API Documentation?

API documentation describes how clients and developers interact with an API.

It explains:

- Available endpoints.
- HTTP methods.
- Required request parameters.
- Request body formats.
- Response structures.
- HTTP status codes.
- Authentication requirements.
- Validation constraints.
- Error responses.

For example, a developer integrating with the Task Management API should know:

```http
POST /api/v1/tasks
Authorization: Bearer <access-token>
Content-Type: application/json
```

Request:

```json
{
  "title": "Complete Spring Boot project",
  "description": "Finish API documentation",
  "priority": "HIGH",
  "dueDate": "2026-12-01"
}
```

The documentation explains what fields are accepted and what response the API returns.

### Why is API documentation important?

1. Makes API integration easier.
2. Reduces communication between frontend and backend developers.
3. Provides a central reference for endpoints.
4. Helps developers test APIs.
5. Improves maintainability.
6. Makes APIs easier to understand and consume.

---

## 3. What is OpenAPI?

OpenAPI is a specification used to describe REST APIs in a standardized, machine-readable format.

It describes:

- API information.
- Available paths and HTTP methods.
- Request parameters.
- Request bodies.
- Response schemas.
- Authentication schemes.
- HTTP status codes.

OpenAPI allows tools to understand an API automatically.

For example, an OpenAPI document can describe:

```yaml
openapi: 3.0.1

info:
  title: Task Management REST API
  version: 1.0.0

paths:
  /api/v1/tasks:
    get:
      summary: List the current user's tasks
      responses:
        "200":
          description: Task page returned
```

Springdoc generates the OpenAPI specification from the existing Spring Boot application and its annotations.

### Important

OpenAPI is the API description specification. It is not itself the graphical documentation interface.

---

## 4. What is Swagger UI?

Swagger UI is a browser-based interface that displays API documentation generated from an OpenAPI specification.

It allows developers to:

- View available endpoints.
- Expand endpoint details.
- Inspect request and response schemas.
- Enter request parameters.
- Provide JWT access tokens.
- Execute API requests.
- View actual HTTP responses.

### Swagger UI in this project

The default local URLs are:

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

The Swagger UI URL provides the interactive interface.

The OpenAPI JSON URL provides the machine-readable API specification.

### OpenAPI vs Swagger UI

| OpenAPI | Swagger UI |
|---|---|
| API specification | Interactive documentation interface |
| Describes API structure | Displays API structure |
| Machine-readable | Human-friendly |
| Generated as JSON/YAML | Displayed in a browser |
| Used by documentation tools | Used by developers to explore APIs |

---

## 5. Springdoc OpenAPI

Springdoc OpenAPI integrates OpenAPI documentation generation with Spring Boot applications.

It detects Spring MVC controllers, mappings, DTOs and annotations to generate API documentation.

### Dependency used in this project

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.17</version>
</dependency>
```

This dependency provides:

- OpenAPI specification generation.
- Swagger UI integration.
- Spring MVC endpoint discovery.
- DTO schema generation.
- Support for OpenAPI annotations.

### How it works

The general flow is:

```text
Spring Boot Application
        |
        v
Spring MVC Controllers
        |
        v
Springdoc OpenAPI
        |
        v
OpenAPI Specification
        |
        v
Swagger UI
        |
        v
Developer explores and tests APIs
```

Springdoc generates documentation from the application's existing mappings and annotations.

---

## 6. OpenAPI Configuration

The project contains:

```text
com.taskmanager.config.OpenApiConfig
```

The configuration defines the API metadata and JWT security scheme.

Important configuration:

```java
@Bean
public OpenAPI taskManagementOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("Task Management REST API")
            .description(
                "REST API for user accounts, authentication, and owner-scoped task management."
            )
            .version("1.0.0"))
        .components(new Components()
            .addSecuritySchemes("bearerAuth", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")));
}
```

### Explanation

**@Bean**

Registers the OpenAPI object in the Spring application context.

**OpenAPI**

Represents the complete API specification.

**Info**

Contains general API information.

**title**

Defines the API name.

**description**

Explains the purpose and usage of the API.

**version**

Specifies the API documentation version.

**Components**

Contains reusable OpenAPI components, including security schemes and schemas.

**SecurityScheme**

Defines the authentication mechanism used by protected endpoints.

---

## 7. JWT Bearer Authentication in Swagger UI

The Task Management API uses JWT-based authentication.

After successful login, the application returns:

- Access token.
- Refresh token.
- Token type.
- Expiration information.

The access token is used to authenticate requests to protected endpoints.

### Bearer authentication configuration

```java
new SecurityScheme()
    .type(SecurityScheme.Type.HTTP)
    .scheme("bearer")
    .bearerFormat("JWT")
```

### Explanation

| Property | Meaning |
|---|---|
| type | HTTP authentication |
| scheme | Bearer |
| bearerFormat | JWT |

Swagger UI uses this configuration to provide an Authorize option.

### How to authorize requests

1. Open Swagger UI.
2. Execute the login endpoint.
3. Copy the access token from the response.
4. Click Authorize.
5. Enter the access token.
6. Confirm authorization.
7. Execute protected endpoints.

The token should be the JWT access token, not the refresh token.

Swagger UI automatically sends the appropriate Authorization header for operations configured with the Bearer security requirement.

Example:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Important security distinction

Access tokens and refresh tokens have different responsibilities.

| Access Token | Refresh Token |
|---|---|
| Used to access protected APIs | Used to obtain a new token pair |
| Short-lived | Longer-lived |
| Sent as Bearer authentication | Submitted to the refresh endpoint |
| Validated by JWT security filter | Validated by refresh-token service |

Never use the refresh token as the Swagger Bearer token.

---

## 8. OpenAPI Annotations

OpenAPI annotations provide additional information about controllers and API operations.

The project uses annotations from:

```java
io.swagger.v3.oas.annotations
```

### 8.1 @Tag

Groups related API endpoints.

Example:

```java
@Tag(
    name = "Tasks",
    description = "Task operations scoped to the authenticated user."
)
```

This groups task-related endpoints under the Tasks section in Swagger UI.

### 8.2 @Operation

Documents an individual API operation.

Example:

```java
@Operation(
    summary = "Create a task",
    description = "Creates a task for the authenticated user."
)
```

**summary** provides a short description.

**description** provides additional details about the operation.

### 8.3 @ApiResponse

Documents one HTTP response.

Example:

```java
@ApiResponse(
    responseCode = "201",
    description = "Task created"
)
```

### 8.4 @ApiResponses

Documents multiple possible responses.

Example:

```java
@ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Task created"
    ),
    @ApiResponse(
        responseCode = "400",
        description = "Request validation failed"
    ),
    @ApiResponse(
        responseCode = "401",
        description = "Authentication required"
    )
})
```

### 8.5 @Parameter

Documents request parameters and path variables.

Example:

```java
@Parameter(
    description = "Zero-based page index.",
    example = "0"
)
@RequestParam(defaultValue = "0") int page
```

### 8.6 @SecurityRequirement

Declares that an operation requires a particular security scheme.

Example:

```java
@SecurityRequirement(name = "bearerAuth")
```

The name must match the security scheme configured in OpenApiConfig.

### 8.7 @Schema

Provides documentation for request and response models.

Example:

```java
@Schema(
    description = "Task title",
    example = "Complete Spring Boot project"
)
```

It can describe:

- Field names.
- Data types.
- Examples.
- Required fields.
- Allowed values.
- Validation constraints.
- Read/write access.

---

## 9. Authentication API Documentation

The project groups authentication endpoints under:

```text
/api/v1/auth
```

The controller uses the Authentication tag.

### Documented endpoints

| Endpoint | Method | Purpose |
|---|---|---|
| /register | POST | Register an account |
| /login | POST | Authenticate a user |
| /refresh | POST | Rotate a refresh token |
| /logout | POST | Revoke a refresh-token family |
| /change-password | POST | Change the current user's password |
| /forgot-password | POST | Request password reset |
| /reset-password | POST | Reset a password |

### Public vs protected authentication endpoints

The documentation distinguishes between public and protected endpoints.

Public endpoints:

- Registration.
- Login.
- Refresh.
- Logout.
- Forgot password.
- Reset password.

Protected endpoint:

- Change password.

The change-password operation declares:

```java
@SecurityRequirement(name = "bearerAuth")
```

This makes its JWT requirement visible in Swagger UI.

### Security-conscious documentation

The registration and password-reset descriptions explain that generic responses are used to avoid revealing whether an account exists.

This documents an important security property without exposing internal implementation details.

---

## 10. Task Management API Documentation

The task controller uses:

```text
/api/v1/tasks
```

It is grouped under the Tasks tag.

The controller applies:

```java
@SecurityRequirement(name = "bearerAuth")
```

Therefore, task operations are documented as protected endpoints.

### Documented endpoints

| Endpoint | Method | Purpose |
|---|---|---|
| /api/v1/tasks | POST | Create task |
| /api/v1/tasks | GET | List tasks |
| /api/v1/tasks/{taskId} | GET | Get task |
| /api/v1/tasks/{taskId} | PUT | Update task |
| /api/v1/tasks/{taskId} | DELETE | Delete task |

### Task ownership

The documentation explains that task ownership is derived from the authenticated JWT principal.

The client cannot provide a user ID to create or reassign task ownership.

This is important because the API enforces user-level data isolation.

---

## 11. Pagination, Sorting and Filtering Documentation

The task-list endpoint supports pagination, sorting, search and filters.

These parameters are documented using @Parameter and @Schema annotations.

| Parameter | Description | Default |
|---|---|---|
| page | Zero-based page index | 0 |
| size | Tasks per page, maximum 100 | 10 |
| sortBy | Field used for sorting | createdAt |
| direction | asc or desc | desc |
| search | Case-insensitive title search | Optional |
| status | Task status filter | Optional |
| priority | Task priority filter | Optional |
| dueFrom | Inclusive lower due-date bound | Optional |
| dueTo | Inclusive upper due-date bound | Optional |

### Example request

```http
GET /api/v1/tasks?page=0&size=10&sortBy=createdAt&direction=desc&priority=HIGH
```

### Date filtering

The API accepts ISO date values.

Example:

```text
dueFrom=2026-01-01
dueTo=2026-12-31
```

The lower and upper bounds are inclusive.

The upper date must not be before the lower date.

### Why document parameters?

Clear parameter documentation helps frontend developers construct valid API requests and understand accepted values without reading service-layer implementation.

---

## 12. DTO Schema Documentation

DTO means Data Transfer Object.

DTOs define the data exchanged between clients and the backend.

The project uses DTOs instead of exposing JPA entities directly.

### Authentication DTOs

Examples:

- RegistrationRequest
- LoginRequest
- LoginResponse
- RefreshTokenRequest
- ChangePasswordRequest
- ForgotPasswordRequest
- ResetPasswordRequest
- AuthMessageResponse

### Task DTOs

Examples:

- CreateTaskRequest
- UpdateTaskRequest
- TaskResponse
- TaskPageResponse

### Error DTO

- ApiErrorResponse

### Why DTOs are important for API documentation

DTOs provide a controlled representation of request and response data.

They help:

1. Prevent accidental exposure of database entities.
2. Hide internal relationships.
3. Control which fields clients can submit.
4. Document validation constraints.
5. Keep API contracts independent of database implementation.

### Example

A task creation request accepts fields such as:

```json
{
  "title": "Prepare documentation",
  "description": "Complete API documentation",
  "priority": "HIGH",
  "dueDate": "2026-12-01"
}
```

The client does not submit a user ID.

The authenticated user is resolved by the backend.

---

## 13. Write-Only Sensitive Fields

Some DTO properties contain sensitive information.

Examples:

- Password.
- Current password.
- New password.
- Refresh token.
- Password-reset token.

The project uses OpenAPI schema access settings such as:

```java
@Schema(accessMode = Schema.AccessMode.WRITE_ONLY)
```

### What does WRITE_ONLY mean?

A write-only property is intended to be supplied in requests rather than exposed as an ordinary response property.

This is useful for sensitive credentials.

### Read-only vs write-only

| Access mode | Meaning |
|---|---|
| READ_ONLY | Returned by the API but not intended as input |
| WRITE_ONLY | Accepted as input but not intended as response output |
| READ_WRITE | Used for both input and output |

These settings improve the generated API schema, but they do not replace actual DTO design, serialization controls or security checks.

---

## 14. HTTP Response Documentation

HTTP status codes communicate the outcome of API operations.

The project documents relevant responses such as:

| Status | Meaning |
|---|---|
| 200 | Successful request |
| 201 | Resource created |
| 202 | Request accepted |
| 204 | Successful request with no response body |
| 400 | Invalid request |
| 401 | Authentication required or invalid |
| 403 | Access forbidden |
| 404 | Resource not found |
| 429 | Rate limit exceeded |

### Example: Task creation

```http
POST /api/v1/tasks
```

Expected successful response:

```http
HTTP/1.1 201 Created
Location: /api/v1/tasks/42
```

The response body contains the created TaskResponse.

### Example: Task deletion

```http
DELETE /api/v1/tasks/42
```

Expected successful response:

```http
HTTP/1.1 204 No Content
```

A 204 response does not contain a response body.

### Why document error responses?

Frontend developers need to understand how to handle:

- Validation failures.
- Missing authentication.
- Forbidden operations.
- Missing resources.
- Rate limits.

The project uses ApiErrorResponse for structured API error documentation.

---

## 15. API Documentation and Spring Security

Spring Security protects application endpoints.

Swagger UI and OpenAPI JSON also need appropriate access rules.

The project permits these documentation paths:

```java
.requestMatchers(
    "/v3/api-docs/**",
    "/swagger-ui/**",
    "/swagger-ui.html"
).permitAll()
```

### Why permit these paths?

The documentation interface must be accessible to developers without requiring a JWT just to open Swagger UI.

However, allowing access to documentation does not mean allowing unauthenticated access to protected business endpoints.

The actual task endpoints remain protected by JWT authentication.

### Important distinction

There are two separate concerns:

1. Access to API documentation.
2. Access to actual API operations.

Swagger UI can be publicly accessible in development while protected task endpoints still require valid JWT access tokens.

---

## 16. Enabling and Disabling Documentation by Environment

API documentation is useful during development but may not need to be publicly exposed in production.

The project uses environment-based Springdoc properties.

### Default application.properties

```properties
springdoc.api-docs.enabled=${API_DOCS_ENABLED:true}
springdoc.swagger-ui.enabled=${API_DOCS_ENABLED:true}
springdoc.swagger-ui.path=/swagger-ui.html
```

Documentation is enabled by default for local development.

### Production application-prod.properties

```properties
springdoc.api-docs.enabled=${API_DOCS_ENABLED:false}
springdoc.swagger-ui.enabled=${API_DOCS_ENABLED:false}
```

Documentation is disabled by default in production.

### Override through environment variable

To enable documentation deliberately:

```powershell
$env:API_DOCS_ENABLED = "true"
```

To disable documentation:

```powershell
$env:API_DOCS_ENABLED = "false"
```

### Why is this useful?

It allows developers to use Swagger UI locally without automatically exposing the documentation endpoints in production.

The environment-specific setting should be controlled deliberately during deployment.

---

## 17. OpenAPI Integration Testing

The project contains two dedicated OpenAPI integration tests.

### OpenApiDocumentationIntegrationTest

This test verifies:

- API title.
- API version.
- JWT Bearer security scheme.
- Authentication endpoint paths.
- Task endpoint paths.
- Public login operation.
- Protected change-password operation.
- Protected task-list operation.
- 401 and 403 response documentation.
- Registration and login response schemas.
- Task response schemas.
- Pagination and filtering parameters.
- Task status and priority enum values.
- Password write-only schema.
- Swagger UI availability.

### OpenApiDisabledIntegrationTest

This test verifies that the documentation endpoints are unavailable when OpenAPI and Swagger UI are disabled.

It checks:

```text
/v3/api-docs
/swagger-ui.html
/swagger-ui/index.html
```

Expected result:

```text
404 Not Found
```

### Why integration tests?

Unit tests can verify individual components, but integration tests help verify the actual generated OpenAPI document and the application's HTTP behavior.

---

## 18. How to Test APIs Through Swagger UI

After starting the application locally:

### Step 1

Open:

```text
http://localhost:8080/swagger-ui/index.html
```

### Step 2

Expand the Authentication section.

### Step 3

Execute the login endpoint using valid credentials.

### Step 4

Copy the access token from the response.

### Step 5

Click Authorize.

### Step 6

Enter the JWT access token.

### Step 7

Expand the Tasks section.

### Step 8

Execute protected task operations.

### Step 9

Inspect:

- Request URL.
- Request headers.
- Request body.
- HTTP status.
- Response headers.
- Response body.

Swagger UI is useful for testing and understanding API behavior, but it does not replace automated integration tests.

---

## 19. Important Security Considerations

1. Never expose JWT signing secrets in API documentation.
2. Never use refresh tokens as Bearer access tokens.
3. Do not expose password hashes.
4. Avoid exposing internal JPA entity relationships.
5. Keep sensitive request fields write-only in the generated schema where appropriate.
6. Keep production documentation disabled by default unless there is an explicit reason to enable it.
7. Do not weaken endpoint authorization to make Swagger testing easier.
8. Remember that publicly accessible Swagger UI does not make protected API endpoints public.
9. Do not include real credentials or tokens in OpenAPI examples.
10. Continue enforcing server-side ownership checks independently of Swagger documentation.

---

## 20. Phase 6.4 Test Results and Verification Boundary

The uploaded project contained Maven Surefire reports recording:

- 146 tests.
- 0 failures.
- 0 errors.
- 1 skipped.

The OpenAPI-specific test reports recorded:

- OpenApiDocumentationIntegrationTest: 2 tests passed.
- OpenApiDisabledIntegrationTest: 1 test passed.

The skipped test was the opt-in PostgreSQL integration test.

Important verification boundary:

These results were read from existing Surefire reports in the uploaded project. A fresh Maven test run was not completed during the independent review because the Maven wrapper could not download Maven 3.10.1 in that environment.

Therefore, the reports provide evidence of a previous successful test execution, not a newly executed test run.

PostgreSQL deployment and migration execution remain separate verification tasks.

---

## 21. Key Interview Questions and Answers

### Q1. What is OpenAPI?

OpenAPI is a standardized specification used to describe REST APIs in a machine-readable format.

### Q2. What is Swagger UI?

Swagger UI is a browser-based interface that displays an OpenAPI specification and allows developers to explore and test API endpoints.

### Q3. What is the difference between OpenAPI and Swagger UI?

OpenAPI describes the API structure. Swagger UI displays that specification in an interactive browser interface.

### Q4. What is Springdoc OpenAPI?

Springdoc OpenAPI is a library that integrates OpenAPI documentation generation with Spring Boot applications.

### Q5. Why use Swagger UI?

It allows developers to understand and test APIs without manually constructing requests in another API client.

### Q6. What is JWT Bearer authentication?

It is an HTTP authentication mechanism where the client sends a JWT access token in the Authorization header using the Bearer scheme.

### Q7. What is @Operation?

It documents an individual API operation, including its summary and description.

### Q8. What is @Tag?

It groups related API operations into logical sections in Swagger UI.

### Q9. What is @ApiResponse?

It documents a possible HTTP response for an API operation.

### Q10. What is @SecurityRequirement?

It declares that an API operation requires a configured security scheme, such as JWT Bearer authentication.

### Q11. Why use DTOs with OpenAPI?

DTOs provide controlled request and response structures and prevent direct exposure of internal persistence entities.

### Q12. What is a write-only schema property?

A write-only property is intended to be supplied in requests but not exposed as an ordinary response property.

### Q13. Why disable Swagger UI in production?

It reduces unnecessary exposure of API documentation and internal API structure.

### Q14. Does allowing Swagger UI make protected APIs public?

No. Documentation access and actual endpoint authorization are separate. Protected APIs still require authentication.

### Q15. Does Swagger UI replace Postman or automated testing?

No. Swagger UI is useful for interactive exploration, while Postman and automated tests serve additional testing and regression purposes.

### Q16. What is the OpenAPI JSON endpoint?

The default Springdoc OpenAPI JSON endpoint is:

```text
/v3/api-docs
```

### Q17. What is the default Swagger UI endpoint in this project?

```text
/swagger-ui/index.html
```

### Q18. Can Swagger UI automatically authenticate requests?

It can attach the configured Bearer token to operations that declare the matching security requirement after the developer authorizes the interface.

---

## 22. Phase 6.4 Completion Summary

Phase 6.4 implemented API documentation using Springdoc OpenAPI and Swagger UI.

### Completed areas

- Springdoc OpenAPI dependency.
- OpenAPI 3 specification generation.
- API metadata configuration.
- JWT Bearer security scheme.
- Authentication controller documentation.
- Task controller documentation.
- Public and protected endpoint distinction.
- Request and response DTO schema documentation.
- Pagination, sorting and filtering parameter documentation.
- HTTP status and error response documentation.
- Production documentation disablement by default.
- OpenAPI enabled/disabled integration tests.

### Verification

Existing Surefire reports recorded 146 tests, 0 failures, 0 errors and 1 skipped. The PostgreSQL integration test was skipped.

### Final assessment

Phase 6.4 implementation review was approved based on the supplied source files and existing test reports.

Minor optional refinements identified:

- Document the Location response header on task creation.
- Document Cache-Control response headers for authentication operations.
- Explicitly document the public test endpoint's 200 response and string schema.

These refinements were not considered blockers for Phase 6.4 approval.

### Next Phase

**Phase 6.5 — Production Testing and Security Hardening**

The next phase will focus on validating production behavior, testing security boundaries, reviewing error handling and strengthening application reliability before Docker and deployment.


--------------------------------------------------------------------------------------------------

## 23. Phase 6.5 — Production Testing and Security Hardening

Phase 6.5 reviewed the existing authentication, authorization, token lifecycle, OAuth2, HTTP security, error handling, production configuration and dependency set. The goal is to verify the controls that are actually present, add regression coverage for demonstrated gaps, and document deployment limitations; passing tests are evidence for covered cases, not proof that the application is completely production-secure.

### Authentication and JWT security

- Local credentials are authenticated through Spring Security, with BCrypt password hashes rather than plaintext passwords.
- Registration and login validate request DTOs. Registration responses remain generic for both new and duplicate accounts to reduce account enumeration; login failures use a generic invalid-credentials response, including for nonexistent or disabled users.
- Access tokens are signed JWTs. The verifier checks the configured signature algorithm/key, issuer, expiration and required claims; malformed, expired or incorrectly signed Bearer tokens are rejected before reaching protected endpoints.
- The JWT signing secret is external configuration, Base64-decoded and checked for sufficient key material at startup. Production deployments must use a unique secret from a secret manager or equivalent protected environment, never a committed value.
- A bearer token is a credential: keep it out of URLs, logs, analytics and browser storage where a safer client-side design is available. Use TLS for every production request. Access tokens remain usable until expiry unless additional revocation state is implemented; refresh-token revocation alone does not revoke already issued access JWTs.

### Refresh-token rotation and replay detection

- Refresh tokens are opaque, randomly generated credentials. The database stores a SHA-256 hash, not the raw token, so a database-only disclosure does not directly reveal a usable refresh credential.
- Refresh exchanges rotate the token while preserving a session-family identifier. A rotated token cannot be used again; detected reuse revokes the family rather than silently creating another session.
- Logout revokes the submitted token's family, while password changes and resets revoke the user's active refresh sessions. Database row locking and concurrent-refresh tests verify that two concurrent uses do not both succeed.
- Clients should replace and securely erase the previous refresh token after each successful rotation. Never put refresh tokens in logs or URLs; send them over TLS and use the existing no-store response behavior.

### Authorization and ownership isolation

- The authenticated principal, not a request-body user identifier, determines task ownership. DTOs do not provide an authorization mechanism for selecting another user.
- Task reads, updates and deletes are scoped by both task ID and owner ID. A cross-user lookup follows the same not-found response as a nonexistent task, reducing resource enumeration.
- Listing, search, filters, sorting, pagination totals and page metadata must use the same owner scope. Integration coverage creates two users, verifies isolated lists and metadata, and checks that another user's task cannot be read, modified or deleted.
- Authorization must be enforced at the service/data-access boundary as well as by the URL security rules; an authenticated user is not automatically authorized for every record.

### Password and password-reset security

- BCrypt is a one-way password hashing function designed to make offline guessing more expensive than a fast general-purpose hash. Password requirements and request validation are enforced before authentication data is changed.
- Password reset credentials are random, stored as hashes, expire, and are single-use. Invalid, expired and reused reset tokens must not change a password. Reset requests use generic responses so an unknown email does not reveal account existence.
- A successful password change or reset revokes refresh sessions. Tests should verify the new password works, the old one fails, and any previously issued refresh credentials cannot create another access-token session.
- Do not write passwords, password hashes, reset tokens or refresh-token values to logs. This phase does not configure an email provider; delivery remains an integration responsibility.

### Rate limiting

- Login and password-reset requests use separate, bounded in-memory limiter state. The implementation applies limits to IP and normalized-account dimensions, expires windows, and avoids unbounded per-identity map growth. Rate-limit failures use HTTP 429 where the request is actively rejected; account-specific reset suppression remains generic.
- Bounded fixed buckets can collide: unrelated identities may share a slot and be throttled together. This is a deliberate memory bound, not a distributed quota system.
- The limiter is process-local: counters reset on restart, and separate application instances do not share state. It is not sufficient by itself for a horizontally scaled or hostile public deployment; consider a shared atomic store or gateway/WAF policy when operational requirements demand cross-instance enforcement.
- When deployed behind a proxy, configure trusted client-address forwarding at the edge and ensure untrusted clients cannot spoof forwarded IP headers. Otherwise IP-based limits may see the proxy address or trust forged values.

### OAuth2 and temporary session cleanup

- Google OIDC and GitHub sign-in use Spring Security's authorization-code flow and state validation. Provider credentials are required only when that provider is enabled; tests use synthetic identities and do not contact providers.
- External identities are associated by provider identity, not by accepting an unverified email match as proof of account ownership. Duplicate or conflicting external-account links must fail safely rather than silently attach a provider to another local account.
- Provider access tokens, authorization codes and client secrets are not the application's JWT/refresh-token pair and must not be logged or returned to the frontend. OAuth failures return a generic response without provider exception details.
- OAuth authorization state is held in the temporary HTTP session. The callback success and failure handlers now invalidate that session and expire the session cookie using configured name, path, domain, Secure, HttpOnly and SameSite settings. The success response retains the established JSON token-pair contract; tokens are not placed in cookies.
- Production session cookies must be Secure over HTTPS, HttpOnly, and use an appropriate SameSite policy. TLS termination must be configured so the application and proxy agree on whether a request is secure; never disable Secure merely to compensate for an incorrectly configured proxy.

### CORS and HTTP security headers

- CORS allows explicitly configured origins, methods and headers. Wildcard origins and credentialed browser access are not required for this bearer-token API. A preflight request is distinct from authorization of the subsequent API operation.
- `X-Content-Type-Options`, `X-Frame-Options` and `Referrer-Policy` are checked on API responses. HSTS is emitted only for secure requests so local HTTP development does not receive a policy that forces HTTPS unexpectedly.
- HSTS is meaningful only when the production site is consistently served over HTTPS. Enable it at the application or trusted TLS edge, verify proxy forwarding, and only enable `includeSubDomains` when every covered subdomain supports HTTPS.

### Error handling and information disclosure

- API error bodies use a consistent timestamp/status/error/message/path/details shape. Validation details identify request fields; unexpected exceptions return safe generic messages rather than SQL, stack traces, credentials, hashes, filesystem paths or implementation class names.
- The 401 authentication entry point and 403 access-denied handler do not echo underlying exception details. User-owned resources return consistent 404 responses across users. Rate-limit responses use 429; validation errors use 400; unexpected failures use 500.
- Duplicate registration intentionally remains generic rather than returning 409, because a conflict response would reveal that an account exists. A 409 is appropriate only where the API contract can return it without exposing protected account state.
- Detailed diagnostics belong in access-controlled server logs, with secrets and credentials redacted. A safe client response does not justify suppressing useful server-side investigation data.

### Production configuration and reliability

- Production configuration requires PostgreSQL and explicit database, JWT and CORS configuration. OAuth client credentials are validated only for enabled providers. Cookie Secure is enabled by production defaults; Swagger/OpenAPI is disabled unless explicitly enabled.
- Hibernate uses schema validation, not automatic production schema creation/update. Flyway manages schema migrations; `flyway.clean` remains disabled. SQL display and formatting logs remain off in production. Existing Flyway V1/V2 migrations are not changed by this phase.
- Hikari pool parameters are configurable and must be sized for the database's connection budget and validated under expected concurrency. A successful application startup is not a load test or proof of adequate capacity.
- The PostgreSQL Testcontainers check is opt-in. H2 tests do not establish PostgreSQL locking, SQL-dialect or operational behavior; PostgreSQL verification must be reported only when Docker and the container test actually run.
- Production secrets, OAuth callbacks, proxy/TLS behavior, database backups, monitoring and rate limits must be validated in the real deployment environment. Tests with H2 and mock OAuth providers cannot prove those operational controls.

### Dependency security review (2026-11-25)

- The resolved Maven tree includes Spring Boot `3.5.7`, Spring Security `6.5.6`, Spring Framework `6.2.12`, JJWT `0.12.6`, Springdoc `2.8.17`, Flyway `11.7.2` and PostgreSQL JDBC `42.7.8`. No duplicate application dependency was identified in the inspected tree.
- Current Spring advisories list versions in this dependency set as affected for some conditional issues. For example, [CVE-2026-59273](https://spring.io/security/cve-2026-59273) applies to Spring Security 6.5.x versions through 6.5.12 when attacker-controlled input is bound to an `ELRequestMatcher`/`RequestMatcher` property. No such matcher or binding path was found in this project; this is a version/advisory match, not a confirmed exploitable path here.
- [CVE-2026-59414](https://spring.io/security/cve-2026-59414) covers Spring Boot 3.5.x through 3.5.17 when the affected `EndpointRequest`/`PathRequest` matchers are configured with trailing-slash paths. The project has a custom security chain and no Actuator dependency or use of these matcher APIs was found. The H2 console is enabled only in the development profile and disabled in production.
- [CVE-2026-59374](https://spring.io/security/cve-2026-59374) affects Spring Framework versions through 6.2.20 when restrictive `DataBinder` allowed/disallowed-field patterns are used with indexed properties. No `WebDataBinder`, `@InitBinder`, or allowed/disallowed-field configuration was found; JSON DTO binding is used instead.
- Spring Boot advisories [CVE-2026-40975](https://spring.io/security/cve-2026-40975) (weak `${random.*}` values used as secrets), CVE-2026-40973 (persistent session temporary-directory conditions), and CVE-2026-40977 (`ApplicationPidFileWriter`) are conditional. The reviewed configuration uses an externally supplied JWT secret and does not enable persistent sessions or configure `ApplicationPidFileWriter`.
- These were manual source/advisory checks, not a complete automated CVE/SBOM scan. Conditional exposure must be rechecked whenever dependencies or configuration change. No broad dependency upgrade was made; select supported fixed versions and evaluate compatibility as a separately reviewed change.

### Security testing strategy

- Use JUnit 5 for deterministic unit tests, Mockito for isolated collaborators, and Spring Boot Test + MockMvc for HTTP/security integration tests. H2 provides repeatable local database coverage; PostgreSQL Testcontainers remains opt-in.
- Cover both positive and negative paths: valid/invalid credentials, disabled accounts, absent/malformed/expired/wrong-signature JWTs, token rotation/reuse/concurrency, cross-user access, reset expiry/single-use, rate-limit windows/capacity, OAuth state failures, CORS preflights, secure-cookie cleanup, HSTS, safe error responses and production startup validation.
- Keep tests independent of real email, Google/GitHub credentials, production secrets and external PostgreSQL. Assert observable HTTP status, safe response shape, state changes and ownership—not private implementation details unless they are security invariants such as storing only token hashes.
- Run targeted tests first and the full ordinary Maven suite afterward. Report skipped tests and unavailable infrastructure explicitly; never equate a skipped PostgreSQL container test with PostgreSQL verification.

### Interview questions and answers

#### Q1. Why hash refresh tokens before storing them?

A refresh token is a bearer credential. Storing only its cryptographic hash limits direct token theft if a database backup or read-only database account is exposed.

#### Q2. What is refresh-token rotation?

The server invalidates the presented refresh token and issues a new one for the same session family. A token should be accepted only once.

#### Q3. What is refresh-token reuse detection?

If an already rotated token is presented again, the server treats it as possible theft and revokes the associated token family instead of silently continuing the session.

#### Q4. Why keep access JWT lifetimes short?

JWTs are usually validated without a database lookup, so an already issued access token is difficult to revoke immediately. A short lifetime limits the time window if it is stolen.

#### Q5. How is task ownership enforced?

The server derives the user from the authenticated principal and includes that owner in repository/service queries. A client-supplied user ID is not authorization.

#### Q6. Why return 404 for another user's task?

Using the same not-found response for absent and non-owned records avoids disclosing whether another user's resource exists.

#### Q7. Why use BCrypt instead of encrypting passwords?

Password verification needs to compare a candidate, not recover the original password. A one-way, salted, deliberately expensive hash raises the cost of offline password guessing.

#### Q8. Why should password-reset responses be generic?

Different messages for known and unknown accounts let an attacker enumerate users. A generic response communicates the same public outcome in both cases.

#### Q9. Why is in-memory rate limiting insufficient for multiple instances?

Each instance has independent counters that reset on restart. A client can distribute requests across instances unless a shared gateway or atomic distributed store enforces the limit.

#### Q10. What does OAuth2 `state` protect?

The callback must correlate with an authorization request initiated by that browser session. Checking state helps prevent login CSRF and response substitution.

#### Q11. What is the difference between CORS and authentication?

CORS is a browser rule controlling whether frontend JavaScript may read a cross-origin response. It does not authenticate a caller or authorize an API operation.

#### Q12. Why keep stack traces out of API responses?

Stack traces can reveal internals and secrets to clients. The server should log controlled diagnostic detail while returning a stable, safe error response.

#### Q13. What does HSTS do?

It tells browsers to use HTTPS for future requests to a host. It must be sent only through HTTPS and requires a deliberate deployment decision for subdomains.

#### Q14. What does Hibernate `validate` guarantee?

It checks that mapped entities are compatible with the existing schema at startup. It does not create or migrate schema; Flyway remains responsible for controlled schema changes.

#### Q15. Does a passing security test suite prove production security?

No. Tests cover defined scenarios and configurations. Real identity-provider behavior, PostgreSQL operations, proxy/TLS settings, secrets, monitoring, backups and load still need environment-specific verification.

This concludes Phase 6.5; the next section documents Phase 6.6.
Recheck advisory applicability and fixed-version availability against vendor guidance before release.


## 24. Phase 6.6 — End-to-End API Integration Testing

This phase validates important application workflows through HTTP rather than testing only isolated methods. A passing integration suite is evidence for its covered scenarios and configuration, not proof of complete production readiness.

### Unit, integration, and end-to-end testing

- A unit test isolates one component and substitutes collaborators; an integration test verifies cooperating components such as Spring Security, MVC, services, repositories, migrations, and the test database.
- An end-to-end API test validates a user's workflow through the externally visible request/response contract. `MockMvc` runs in-process, so it is an HTTP application integration boundary, not a real socket, deployment, or browser test.
- Use fast unit tests for focused logic and a smaller set of full-context tests for framework wiring and workflows that cross application layers.

### MockMvc, SpringBootTest, and lifecycle

- `@SpringBootTest` loads the application context; `@AutoConfigureMockMvc` allows requests through the configured MVC and security filter chains without launching a network listener.
- Exercise actual JSON binding, validation, authentication filters, exception advice, DTO mapping, and persistence. Do not bypass JWT behavior with a mocked principal when validating the token workflow.
- Spring caches contexts and test databases can retain records. Use unique emails and resource data, avoid fixed IDs and test ordering, and understand when transactions do or do not include asynchronous/concurrent work.

### HTTP contract and authentication workflows

- Assert HTTP status, `Content-Type`, JSON names and values, validation details, and applicable headers such as `Location` and `Cache-Control`. Check resulting database state where it proves an operation's effect.
- Registration/login tests should cover success, duplicate or unknown accounts, malformed JSON, missing fields, invalid values, and wrong credentials. Keep generic responses generic and assert that responses never serialize password hashes.
- Use the issued access JWT against a protected route; test absent and malformed Bearer headers and expired, wrong-signature, wrong-issuer, and malformed-claim tokens. Safe failures must not disclose secrets, exception details, or stack traces.

### JWT, refresh tokens, and logout

- Generate deterministic JWT test cases with the configured test key and issuer. Successful HTTP authentication checks the whole chain: token parsing, signature/claim validation, principal lookup, and authorization.
- Refresh-token workflow tests should verify replacement issuance, old-token rejection, hashed-at-rest persistence, reuse detection, family revocation, and concurrent-use behavior where locking is important.
- Logout revokes refresh capability, but does not automatically invalidate an already-issued stateless access JWT. Verify the current access-token behavior rather than assuming immediate revocation.

### Authorization and ownership isolation

- Use at least two independently authenticated users. Verify cross-user read/update/delete follows the same not-found contract as an absent resource, and client-supplied ownership fields cannot change the authenticated owner.
- Check task lists, searches, filters, sorting, page totals, and metadata for owner scope. Metadata can leak data even when the returned records are correctly filtered.
- Include unauthenticated requests to protected routes to confirm the security chain rejects them before protected data is returned.

### Database isolation and test data

- H2 provides fast repeatable integration coverage and can run the project's migrations, but it does not prove PostgreSQL dialect, collation, locking, query planning, or production operations.
- Prefer unique test fixtures and targeted state assertions. Avoid destructive shared cleanup and fixed generated IDs. Do not make the default suite depend on Docker, PostgreSQL, real Google/GitHub credentials, email, or external network access.
- PostgreSQL Testcontainers verification is established only when that test actually ran; skipped or unavailable container runs must be reported as such.

### Pagination, sorting, and filtering

- Drive pagination and sorting through the HTTP query parameters. Assert the returned rows and relevant metadata such as page number, size, total elements/pages, and empty/first/last flags where the response provides them.
- Cover ascending/descending supported sorts (including ID, title, and due date), title search, status, priority, date bounds, combined filters, empty results, and invalid page/size/sort/filter values.
- Sorting must use an explicit allowlist, not arbitrary entity paths. Every query and its metadata must remain owner-scoped.

### Error-response contracts and disclosure

- Validate representative 400, 401, 403, 404, 409 where the contract uses it, 429, and 500 responses. Assert consistent safe error fields and avoid brittle assertions on timestamps or localized framework text.
- Client errors must not expose SQL, stack traces, class names, filesystem paths, credentials, password hashes, or token hashes. Keep useful diagnostics in controlled server logs with secrets redacted.
- Account-enumeration protections may intentionally use a generic 202 or 401 response instead of 409; test the existing security contract rather than changing status codes to satisfy a generic convention.

### OAuth callbacks

- Do not contact Google/GitHub in the default suite. Use synthetic provider identities to verify OAuth state handling, invalid/missing state, callback failure, session invalidation, and cookie expiry.
- Assert that successful callbacks retain the existing token-pair JSON contract and failures do not expose provider details or issue tokens. Mock tests do not validate real provider registration, redirect URIs, or remote availability.

### OpenAPI contract validation

- Fetch `/v3/api-docs` and verify paths, methods, request/response schema references, error responses, pagination/filter parameters, and security declarations.
- Public routes must remain public in the document; protected task/password operations should declare bearer authentication. Passwords should be write-only. Compare documentation with runtime HTTP behavior because generated OpenAPI is not itself proof that endpoints behave as described.

### Common integration-test mistakes

- Testing only service methods and assuming that request binding, filters, or exception handling work.
- Weakening security or mocking away the JWT filter to make tests pass.
- Depending on fixed IDs, shared state, or execution order.
- Asserting only status without checking response fields, headers, or resulting state.
- Checking task ownership on point reads while ignoring list counts and search/filter/page metadata.
- Treating H2 as PostgreSQL verification, or a skipped Docker test as a pass.
- Calling external OAuth/email services from ordinary tests or overfitting assertions to incidental serialization details.

### Interview questions and answers

#### Q1. How do unit and integration tests differ?

A unit test isolates one unit with substituted dependencies; an integration test verifies multiple real components work together, often including framework configuration and persistence.

#### Q2. Is MockMvc a network end-to-end test?

No. It invokes Spring MVC and filters in-process without a socket. It does not validate TLS termination, a gateway, or the deployed server.

#### Q3. Why create two users in an ownership test?

Successful access by one user does not prove isolation. A second principal exposes missing owner predicates and metadata leaks.

#### Q4. Why assert pagination totals as well as records?

Totals can reveal another user's matching records even when every returned row is correctly filtered.

#### Q5. What can H2 not verify?

H2 does not establish PostgreSQL-specific SQL, collation, locking, or operational behavior. Use an actual PostgreSQL-backed test for those properties.

#### Q6. Does a passing integration suite prove production security?

No. It demonstrates only the tested cases and settings. Secrets, TLS/proxy configuration, provider behavior, PostgreSQL, monitoring, backups, and load need separate validation.

This concludes Phase 6.6; no subsequent phase is included.

For the complete expanded material, see `Phase_6_6_Learning_Concepts.md`.

## 25. Phase 6.7 — Performance Testing and API Optimization

This phase assessed existing query and connection-pool behavior before changing code. Only two measured improvements were made; API pagination metadata, owner predicates, sorting allowlist, authentication checks, and token behavior remain intact.

### Baseline and findings

- Task listing is database-paginated and every task query is scoped to the authenticated user. `Task.user` is lazy and `TaskResponse` reads scalar task fields, so the list mapping did not show an N+1 relationship fetch.
- The schema had no index beginning with `tasks.user_id`, despite owner-scoped list and filter queries. An isolated H2 plan over 100,000 synthetic rows showed a table scan for `user_id` filtering and default `createdAt DESC, id ASC` ordering; the candidate composite index changed that plan to use the index. This is query-plan evidence, not a production latency benchmark.
- Hibernate statistics on a real `MockMvc` request with 25 tasks and a full page measured four prepared statements: JWT user lookup, duplicate `CurrentUserService` user lookup, page select, and count select. The page and count queries preserve the current response and metadata contract.
- Title search uses a case-insensitive contains pattern with a leading wildcard, which a normal B-tree index generally cannot accelerate. No database-specific text-search infrastructure was added without workload evidence.

### Optimizations and trade-offs

- The JWT principal now carries the user ID already loaded and enabled-checked by the request filter. `CurrentUserService` uses a lazy JPA reference for that principal rather than issuing a second email lookup; other principal types retain the prior lookup fallback. The same integration test now measures three statements for a full page, with the response content and total preserved. This avoids one user query per protected task request; it relies on the request filter's existing database-backed enabled-user check and does not cache authentication across requests.
- Flyway V3 adds `idx_tasks_user_created_at_id` on `(user_id, created_at DESC, id ASC)`. It supports the common owner-scoped default order and helps locate a user's rows before pagination. The additional index costs storage and adds work to task inserts and relevant updates; no index was added for every optional filter.
- No changes were made to HikariCP settings, count queries, page size limits, sorting behavior, title-search semantics, or public endpoints.

### Performance testing approach

- `TaskQueryPerformanceIntegrationTest` enables Hibernate statistics and verifies a full HTTP page uses a bounded three prepared statements after optimization, while retaining the expected 10 records and total count. It also checks the V3 index exists in the H2 schema.
- `task-management-api/performance/k6/task-api.js` is an optional load-test workflow for login, refresh, and task create/list/read/update/delete. It uses a pre-provisioned non-production account, gives each virtual user its own login/refresh-token family, and deletes tasks it creates. Example usage and safe test setup are in `Phase_6_7_Learning_Concepts.md`.
- No k6 run, production-like load test, response-time comparison, or PostgreSQL verification is claimed. H2 query plans and statement counts do not establish PostgreSQL planner choices or deployment performance.

### Connection-pool review

The existing configurable HikariCP defaults remain `maximumPoolSize=10`, `minimumIdle=2`, `connectionTimeout=30000 ms`, `idleTimeout=600000 ms`, `maxLifetime=1800000 ms`, and `validationTimeout=5000 ms`. A saturated pool makes requests wait up to the acquisition timeout before failing; increasing the pool without matching database capacity can worsen contention. See the expanded learning file for pool sizing, leak detection, load testing, and interview material.

This concludes Phase 6.7. The separate learning document contains the detailed concepts, actual optimization rationale, trade-offs, and testing instructions.

