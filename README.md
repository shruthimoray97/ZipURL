# ZipURL — Modern Link Shortening Platform

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-purple.svg)](LICENSE)

**ZipURL** is a high-performance, full-stack URL shortener application engineered with **Java 21**, **Spring Boot**, **Spring Data JPA**, **MySQL**, and a modern **Dark SaaS Glassmorphism UI**.

---

## ✨ Features

- ⚡ **Instant URL Shortening**: Convert long, cumbersome URLs into clean 6-character short codes using Base62 collision-resilient generation.
- 🎨 **Custom Short Aliases**: Choose custom shareable short link codes (e.g. `zipurl/my-alias`).
- 🔀 **HTTP 302 Redirect Engine**: High-throughput redirection with automated click counter updates.
- 📊 **Real-Time Analytics Dashboard**: Track total created links, aggregate click counts, and recent link activity (past 24 hours).
- 📋 **One-Click Copy & Feedback**: Immediate clipboard integration with animated success toasts.
- 🔍 **Live Management & Search**: Filter and search through active URLs instantly; delete links with safety dialogs.
- 🛡️ **Robust Validation & Error Handling**: Protocols automatically normalized (e.g., auto-prepending `https://`); handles invalid domains (400) and duplicate aliases (409) cleanly.
- ⚡ **Polished 404 Experience**: Dedicated dark glassmorphism 404 page for missing or deleted short links.
- 📱 **Fully Responsive Dark SaaS Aesthetic**: Built with custom CSS CSS3 variables, glassmorphism blur filters, glowing borders, and smooth micro-animations.

---

## 🛠️ Tech Stack

- **Backend Framework**: Spring Boot 3.3.5 (Java 21)
- **Web Layer**: Spring Web MVC (RESTful API Design)
- **Data Persistence**: Spring Data JPA / Hibernate ORM
- **Primary Database**: MySQL (via `mysql-connector-j`)
- **Development Profile**: H2 Memory Database (for zero-dependency testing)
- **Build Tool**: Maven (`mvnw.cmd` Maven Wrapper included)
- **Frontend UI**: Vanilla HTML5, CSS3, JavaScript ES6+ (Zero external UI bloat)

---

## 🏗️ System Architecture

```
[ Client Browser ]
       │
       ▼
[ Spring Boot Web Layer ] ──> [ RedirectController ]  ──>  302 Redirect to Original URL
       │                                                      (Increments Click Counter)
       ▼
[ UrlApiController ] ── (REST JSON) ──> [ UrlService ]
                                            │
                                            ▼
                                   [ UrlRepository ]
                                            │
                                 ┌──────────┴──────────┐
                                 ▼                     ▼
                           [ MySQL DB ]          [ H2 Profile ]
```

---

## 📁 Project Structure

```
newvibe/
├── db_setup.sql                       # MySQL database schema setup script
├── pom.xml                            # Maven dependencies & build configuration
├── mvnw.cmd                           # Windows Maven Wrapper
├── README.md                          # Full documentation
├── src/
│   ├── main/
│   │   ├── java/com/zipurl/
│   │   │   ├── ZipUrlApplication.java # Main Spring Boot Entry Point
│   │   │   ├── controller/
│   │   │   │   ├── UrlApiController.java   # REST API Endpoints (/api/urls)
│   │   │   │   └── RedirectController.java # HTTP 302 Short Code Redirection
│   │   │   ├── dto/
│   │   │   │   ├── CreateUrlRequest.java  # URL Creation Payload
│   │   │   │   ├── UrlResponse.java       # Shortened URL DTO
│   │   │   │   ├── StatsResponse.java     # System Metrics Summary
│   │   │   │   └── ErrorResponse.java     # Standard JSON Error Format
│   │   │   ├── entity/
│   │   │   │   └── Url.java               # JPA Entity for `urls` Table
│   │   │   ├── exception/
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── DuplicateShortCodeException.java
│   │   │   │   ├── InvalidUrlException.java
│   │   │   │   └── GlobalExceptionHandler.java # Unified ControllerAdvice
│   │   │   ├── repository/
│   │   │   │   └── UrlRepository.java     # Data JPA Repository
│   │   │   └── service/
│   │   │       └── UrlService.java        # Core Business Logic & Base62 Generator
│   │   └── resources/
│   │       ├── application.properties     # MySQL Primary Database Config
│   │       ├── application-h2.properties  # H2 Testing Profile Config
│   │       └── static/
│   │           ├── index.html             # Single-Page App Markup
│   │           ├── css/styles.css         # Dark SaaS Glassmorphism CSS
│   │           └── js/app.js              # REST API Integration Logic
│   └── test/
│       └── java/com/zipurl/
│           └── ZipUrlApplicationTests.java # Comprehensive JUnit 5 Test Suite
```

---

## 🗄️ Database Structure

### Table: `urls`

| Field | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `PRIMARY KEY, AUTO_INCREMENT` | Unique Identifier |
| `original_url` | `VARCHAR(2048)` | `NOT NULL` | Original target destination |
| `short_code` | `VARCHAR(32)` | `NOT NULL, UNIQUE, INDEX` | Base62 short code or alias |
| `created_at` | `DATETIME` | `NOT NULL` | Timestamp when link was created |
| `click_count` | `BIGINT` | `NOT NULL, DEFAULT 0` | Cumulative redirect visits |

---

## 📡 REST API Documentation

### 1. Create Short URL
- **Endpoint**: `POST /api/urls`
- **Content-Type**: `application/json`
- **Request Body**:
  ```json
  {
    "originalUrl": "https://example.com/very/long/destination/url",
    "customCode": "my-alias"
  }
  ```
- **Response** (`201 Created`):
  ```json
  {
    "id": 1,
    "originalUrl": "https://example.com/very/long/destination/url",
    "shortCode": "my-alias",
    "shortUrl": "http://localhost:8080/my-alias",
    "createdAt": "2026-08-11T18:30:00",
    "clickCount": 0
  }
  ```

### 2. Get All Stored URLs
- **Endpoint**: `GET /api/urls`
- **Response** (`200 OK`): Array of URL objects ordered by newest first.

### 3. Get Specific URL Info
- **Endpoint**: `GET /api/urls/{shortCode}`
- **Response** (`200 OK` or `404 Not Found`).

### 4. Delete Short URL
- **Endpoint**: `DELETE /api/urls/{id}`
- **Response** (`204 No Content`).

### 5. Summary Statistics
- **Endpoint**: `GET /api/urls/stats`
- **Response** (`200 OK`):
  ```json
  {
    "totalUrls": 42,
    "totalClicks": 1280,
    "recentUrls": 15
  }
  ```

### 6. Short Link Redirection
- **Endpoint**: `GET /{shortCode}`
- **Response**: `302 Found` (Redirects browser to `originalUrl` and increments `clickCount`).

---

## 🚀 How to Run Locally

### Prerequisites
- **Java 21 JDK** installed
- **MySQL 8.0+** running locally (or H2 test profile)

### Step 1: Database Setup (MySQL)
Run the setup script using MySQL Workbench or CLI:
```bash
mysql -u root -p < db_setup.sql
```
*(Or allow Hibernate `ddl-auto=update` to automatically create `zipurl_db` database on application startup).*

### Step 2: Configure Database Credentials
Adjust database settings via environment variables or inside `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/zipurl_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### Step 3: Run the Application (Windows)
Execute using the included Maven Wrapper:
```cmd
.\mvnw.cmd spring-boot:run
```

To run using the zero-dependency **H2 Profile**:
```cmd
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=h2
```

### Step 4: Access in Browser
Open your browser and navigate to:
```
http://localhost:8080
```

---

## 🧪 Testing

Run the automated test suite with JUnit 5 and H2 profile:
```cmd
.\mvnw.cmd test
```

---

## 📸 Screenshots

*(Dashboard previews and result card demonstrations)*
- **Hero Section & Shortener Card**: Glassmorphism input with URL validation.
- **Analytics Dashboard**: Live click metrics and management grid.
- **Dark SaaS Aesthetic**: Custom glowing ambient gradients.

---

## 🔮 Future Improvements

- Custom expiration dates (TTL) for temporary short links.
- Password-protected short URLs.
- Geolocation and browser analytics tracking.
