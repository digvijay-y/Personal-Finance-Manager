# Personal Finance Manager API

A RESTful Spring Boot application for personal finance management. Track income, expenses, set financial goals, and generate financial reports.

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Design Decisions](#design-decisions)

## Features

- **User Authentication**: Secure registration and session-based login/logout
- **Transaction Management**: Record and track income and expenses with categories
- **Category Management**: Default categories plus custom user-defined categories
- **Financial Goals**: Set savings targets with progress tracking
- **Reports**: Generate monthly and yearly financial summaries

## Technology Stack

- **Java 17+**
- **Spring Boot 3.x**
- **Spring Security** - Session-based authentication
- **Spring Data JPA** - Database persistence
- **H2 Database** - In-memory database (easily switchable to PostgreSQL/MySQL)
- **Maven** - Dependency management and build tool
- **JUnit 5 & Mockito** - Unit testing

## Architecture

The application follows a **layered architecture** pattern:

```
┌─────────────────────────────────────────────┐
│              Controllers                     │
│  (REST API endpoints, request validation)   │
├─────────────────────────────────────────────┤
│               Services                       │
│  (Business logic, transaction management)   │
├─────────────────────────────────────────────┤
│              Repositories                    │
│  (Data access, JPA queries)                 │
├─────────────────────────────────────────────┤
│               Entities                       │
│  (Domain models, database mapping)          │
└─────────────────────────────────────────────┘
```

### Project Structure

```
src/main/java/com/example/pfm/
├── config/          # Security configuration
├── controller/      # REST controllers
├── dto/             # Data Transfer Objects
├── entity/          # JPA entities
├── exception/       # Custom exceptions & global handler
├── repository/      # Spring Data JPA repositories
├── security/        # Custom UserDetailsService
└── service/         # Business logic services
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd demo
   ```

2. **Build the application**
   ```bash
   ./mvnw clean package
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

The application will start on `http://localhost:8080`

### Running Tests

```bash
./mvnw test
```

## API Documentation

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and create session |
| POST | `/api/auth/logout` | Logout and invalidate session |
| GET | `/api/auth/me` | Get current user info |

#### Register User
```json
POST /api/auth/register
{
  "username": "user@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "phoneNumber": "+1234567890"
}
```

#### Login
```json
POST /api/auth/login
{
  "username": "user@example.com",
  "password": "password123"
}
```

### Transactions

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/transactions` | List all transactions (with optional filters) |
| POST | `/api/transactions` | Create a new transaction |
| PUT | `/api/transactions/{id}` | Update a transaction |
| DELETE | `/api/transactions/{id}` | Delete a transaction |

#### Create Transaction
```json
POST /api/transactions
{
  "amount": 5000.00,
  "date": "2024-01-15",
  "category": "Salary",
  "description": "Monthly salary"
}
```

#### Query Parameters for Filtering
- `type`: Filter by INCOME or EXPENSE
- `startDate`: Filter from date (YYYY-MM-DD)
- `endDate`: Filter to date (YYYY-MM-DD)

### Categories

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/categories` | List all categories (default + custom) |
| POST | `/api/categories` | Create a custom category |
| DELETE | `/api/categories/{name}` | Delete a custom category |

#### Create Category
```json
POST /api/categories
{
  "name": "Freelance",
  "type": "INCOME"
}
```

### Goals

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/goals` | List all goals |
| GET | `/api/goals/{id}` | Get goal details with progress |
| POST | `/api/goals` | Create a new goal |
| PUT | `/api/goals/{id}` | Update a goal |
| DELETE | `/api/goals/{id}` | Delete a goal |

#### Create Goal
```json
POST /api/goals
{
  "goalName": "Emergency Fund",
  "targetAmount": 10000.00,
  "targetDate": "2025-12-31",
  "startDate": "2024-01-01"
}
```

### Reports

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/reports/monthly?year={year}&month={month}` | Get monthly report |
| GET | `/api/reports/yearly?year={year}` | Get yearly report |

## Design Decisions

### 1. Layered Architecture
Separation of concerns with distinct layers for presentation (controllers), business logic (services), and data access (repositories). This promotes maintainability and testability.

### 2. DTOs for API Communication
Request and Response DTOs are used to decouple the API contract from internal domain models. This allows:
- Independent evolution of API and domain
- Validation at the API boundary
- Controlled data exposure

### 3. Session-Based Authentication
HTTP sessions with secure cookies provide:
- Simplified client implementation (no token management)
- Server-side session control
- Easy logout/session invalidation

### 4. Global Exception Handling
A centralized `@ControllerAdvice` handler converts exceptions to consistent API responses:
- `ResourceNotFoundException` → 404 Not Found
- `ConflictException` → 409 Conflict
- `BadRequestException` → 400 Bad Request
- `ForbiddenException` → 403 Forbidden

### 5. Default Categories
System-provided default categories (Salary, Food, Rent, etc.) are available to all users. Users can create custom categories that are private to their account.

### 6. Goal Progress Calculation
Goal progress is calculated dynamically based on net savings (income - expenses) since the goal's start date, providing real-time progress tracking.

### 7. Externalized Configuration
All configuration is externalized to `application.properties`, allowing easy environment-specific overrides without code changes.

## License

This project is licensed under the MIT License.
