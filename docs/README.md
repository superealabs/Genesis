# Genesis

Genesis brings your applications to life by quickly generating complete projects from your database, making it ideal for your MVPs.

## Why Genesis?

Genesis simplifies the start of your projects by reducing boilerplate code and allowing you to generate production-ready applications—whether microservices, MVC applications, or JavaScript front-ends—through a Database First approach and its flexible built-in template engine.

## Supported Technologies

Genesis can generate projects using various technologies depending on the selected combination.

### Generated Project Types

1. **Spring Boot Web API**

2. **.NET Web API**

3. **Spring Boot Web API + API Gateway**

4. **.NET Web API + API Gateway**

5. **Spring Boot Web API + .NET Web API + Eureka Server + API Gateway**

#### Cross-Cutting Technologies

* For Spring-based projects (Types 1, 3, 5):

  * Java (17-23), Spring Boot (3.3.6, 3.2.12), Spring Web, Spring Actuator, Spring Test, Spring Data JPA, Maven (3.9.9), Swagger (springdoc OpenAPI Starter)

* For .NET-based projects (Types 2, 4):

  * C# (8.0-9.0), Entity Framework Core, ASP.NET Core, Swashbuckle

* Common to projects with API Gateway (Types 3, 4, 5):

  * Spring Cloud (2023) Gateway Reactive Server, Spring Security

* Specific to Type 5:

  * Spring Cloud Netflix Eureka Server & Client, Steeltoe

### Supported Databases

Genesis supports the following database management systems:

1. **PostgreSQL**: versions 15 to 16

2. **SQL Server**: version 2022

3. **Oracle**: version 19c

4. **MySQL**: version 8.4.2

## System Requirements

* **Java 21**
* **Gradle 9.5.1**
* **IntelliJ IDEA 2026.1.3** (required for the plugin)

  * Ultimate Edition preferred, but compatible with Community Edition

## Clone and Configure the Project

The project is organized into multiple modules. Follow these steps to clone and configure the project locally.

1. Clone this repository:

   ```bash
   git clone https://github.com/superealabs/Genesis.git
   ```

## Development

This section explains how to configure and run Genesis in a development environment.

### Run the IntelliJ Plugin in Development Mode

To start an IntelliJ IDEA instance with the plugin loaded from the source code:

```bash
gradle genesis-intellij:runIde
```

## Screenshots

This section presents the different steps involved in configuring and generating a project with Genesis.

---

### Step 02: Enter Project Information and Choose the Technology

First, the user enters the general project information, including the application name and the destination directory for the generated project.

The user then selects the technology stack to use from those supported by Genesis:

* Spring Boot

**Screen:** `pic\2-step.png`

---

### Step 03: Database Connection

The user selects the database management system to use from the databases currently supported by Genesis:

* MySQL
* PostgreSQL
* Oracle
* SQL Server

The user then provides the information required to establish the database connection, including:

* Database name
* Username
* Password
* Database schema (optional)

**Screen:** `pic\3-step.png`

---

### Step 04: AI Interaction to Modify the Database Schema

Genesis includes an AI-powered assistance system that automatically generates SQL scripts to modify or evolve the database structure according to the user's needs.

**Screen:** `pic\4-step.png`

---

### Step 05: Select Entities and Components to Generate

The user selects:

### Entities to Include

### Components to Generate

* Model
* Repository
* Service
* Controller

**Screen:** `pic\5-step.png`

---

### Step 06: Manage Entity Relationship Constraints

Genesis allows the user to manage the relationship constraints between each entity.

By default, relationships are nullable.

**Screen:** `pic\6-step.png`

---

### Step 07: Customize Front-End and User Interface Settings

The user can choose the front-end(Domain Driver Design structure) technology to use for the application as well as its minimum configuration:

* Port
* Development language

For projects that include a front-end interface, the user can customize:

* Primary color
* Navigation bar position
* Logo
* Favicon
* Languages to use for the project

**Screen:** `pic\7-step.png`

---

### Step 08: Configure Backend Settings

Genesis also allows the automatic integration of advanced features such as:

* Application security
* Cache support
* Eureka Server configuration for microservice architectures
* Backend port
* ORM settings

**Screen:** `pic\8-step.png`

---

## Project Generation

Once the settings have been configured, Genesis automatically starts the project generation process.
