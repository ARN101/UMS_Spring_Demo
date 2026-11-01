# University Management System

A simple, robust, and full-featured University Management System web application built with Java Spring Boot, Spring Security, Spring Data JPA, Thymeleaf, MySQL, and Docker.

---

## 1. Features

- **Role-Based Access Control (RBAC):** Three distinct roles (`ADMIN`, `TEACHER`, `STUDENT`) with strict authorization rules on both the web UI and REST API.
- **Hierarchical Permissions & Ownership Enforcement:**
  - **Admin:** Complete CRUD operations on Admins, Teachers, and Students. Admins can directly add Teachers and Students (auto-approved).
  - **Teacher:** Can view all Teachers and all Students. Can only edit and delete their **own** teacher profile. Teachers can add/register new Students, which require **Admin Approval** before the student can sign in.
  - **Student:** Can view all approved Students. Can only edit and delete their **own** student profile. Cannot edit or delete other students or register new students.
- **Admin Approval Workflow:**
  - Students registered by Teachers are saved in a `PENDING` state (`approved = false`).
  - Pending students cannot log in.
  - The Admin dashboard highlights pending approval requests with a one-click **Approve** or **Reject** action.
- **Pre-Seeded Realistic Bangladeshi Sample Data:** Includes realistic sample accounts for testing and demonstration right out of the box.
- **Cross-Table Email Uniqueness:** Ensures email addresses are unique across all three tables (admins, teachers, and students).
- **Student Roll Number Uniqueness:** Enforces unique student roll numbers across the institution.
- **Secure Authentication:** Session-based authentication with Spring Security and BCrypt password hashing.
- **Clean REST APIs:** Standard RESTful endpoints returning JSON payloads with appropriate HTTP status codes (`200`, `201`, `204`, `400`, `401`, `403`, `404`, `409`).
- **Password Protection:** Passwords and hashes are never exposed in API responses or UI edit forms.
- **Minimal Web Interface:** Clean and responsive UI built using Bootstrap 5 and Thymeleaf templates.
- **Automatic Seed Data Generation:** Seed data is generated automatically on initial startup.
- **Containerization:** Production-ready `Dockerfile` and `compose.yaml` with MySQL health checks and persistent volume storage.

---

## 2. Technologies Used

| Layer / Purpose | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Build Tool | Maven |
| Web Framework | Spring Web (MVC & REST) |
| Security | Spring Security (Session-based, BCrypt) |
| Persistence & ORM | Spring Data JPA / Hibernate |
| Database | MySQL 8.0 (H2 for tests) |
| Validation | Jakarta Bean Validation |
| Frontend | Thymeleaf, HTML5, CSS3, Bootstrap 5 |
| Containerization | Docker & Docker Compose |

---

## 3. Database Tables

The application connects to MySQL and creates three separate tables:

### 1. `admins`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | Primary Key, Auto Increment | Unique record identifier |
| `full_name` | `VARCHAR(255)` | Not Null | Admin's full name |
| `email` | `VARCHAR(255)` | Not Null, Unique | Unique email address |
| `password` | `VARCHAR(255)` | Not Null | BCrypt hashed password |

### 2. `teachers`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | Primary Key, Auto Increment | Unique record identifier |
| `full_name` | `VARCHAR(255)` | Not Null | Teacher's full name |
| `email` | `VARCHAR(255)` | Not Null, Unique | Unique email address |
| `password` | `VARCHAR(255)` | Not Null | BCrypt hashed password |
| `department` | `VARCHAR(255)` | Not Null | Academic department |
| `designation` | `VARCHAR(255)` | Not Null | Academic designation (e.g., Professor) |

### 3. `students`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | Primary Key, Auto Increment | Unique record identifier |
| `full_name` | `VARCHAR(255)` | Not Null | Student's full name |
| `email` | `VARCHAR(255)` | Not Null, Unique | Unique email address |
| `password` | `VARCHAR(255)` | Not Null | BCrypt hashed password |
| `department` | `VARCHAR(255)` | Not Null | Academic department |
| `semester` | `INT` | Not Null | Current semester number |
| `roll_number` | `VARCHAR(255)` | Not Null, Unique | Unique institutional roll number |
| `approved` | `BOOLEAN` | Not Null | `true` if approved; `false` if pending admin approval |

---

## 4. User Roles and Permissions

| Operation | Admin | Teacher | Student |
|---|---|---|---|
| Manage Admins (CRUD) | Full CRUD | No Access | No Access |
| View Teachers | Yes | Yes (Directory) | No Access |
| Edit / Delete Teachers | All Teachers | Own Profile Only | No Access |
| Add Teachers | Yes | No | No |
| View Students | All Students | Approved Students | Approved Students |
| Add Students | Yes (Auto-Approved) | Yes (Requires Approval) | No |
| Approve Students | Yes | No | No |
| Edit / Delete Students | All Students | No | Own Profile Only |

---

## 5. Seeded Accounts & Credentials for Demonstration

The application comes pre-populated with realistic sample accounts:

### 1. Administrator Account
- **Full Name:** Kazi Farhan Ahmed
- **Email:** `admin@university.com`
- **Password:** `admin123`
- **Role:** `ADMIN`

### 2. Teacher Accounts
- **Full Name:** Dr. Mohammad Rafiqul Islam
  - **Email:** `rafiqul.islam@university.com`
  - **Password:** `password123`
  - **Department:** Computer Science and Engineering
  - **Designation:** Professor & Head of Department
- **Full Name:** Nusrat Jahan Chowdhury
  - **Email:** `nusrat.jahan@university.com`
  - **Password:** `password123`
  - **Department:** Electrical and Electronic Engineering
  - **Designation:** Associate Professor

### 3. Student Accounts
- **Full Name:** Md. Shakil Ahmed *(Approved)*
  - **Email:** `shakil.ahmed@university.com`
  - **Password:** `password123`
  - **Department:** Computer Science and Engineering
  - **Semester:** 4
  - **Roll Number:** `CSE-2023-0101`
- **Full Name:** Farzana Rahman Mim *(Approved)*
  - **Email:** `farzana.mim@university.com`
  - **Password:** `password123`
  - **Department:** Software Engineering
  - **Semester:** 3
  - **Roll Number:** `SWE-2023-0205`
- **Full Name:** Abrar Fahim Hossain *(Pending Approval)*
  - **Email:** `abrar.fahim@university.com`
  - **Password:** `password123`
  - **Department:** Electrical and Electronic Engineering
  - **Semester:** 1
  - **Roll Number:** `EEE-2024-0312`
  - *Note: Log in as Admin to see and approve this account!*

---

## 6. REST API Endpoints

### Admin Endpoints (`/api/admins`)
| Method | Endpoint | Description | Status Codes |
|---|---|---|---|
| `POST` | `/api/admins` | Create a new admin | `201 Created`, `400 Bad Request`, `409 Conflict` |
| `GET` | `/api/admins` | Retrieve all admins | `200 OK`, `401 Unauthorized`, `403 Forbidden` |
| `GET` | `/api/admins/{id}` | Retrieve admin by ID | `200 OK`, `404 Not Found` |
| `PUT` | `/api/admins/{id}` | Update admin details | `200 OK`, `400 Bad Request`, `404 Not Found`, `409 Conflict` |
| `DELETE` | `/api/admins/{id}` | Delete an admin | `204 No Content`, `404 Not Found` |

### Teacher Endpoints (`/api/teachers`)
| Method | Endpoint | Description | Status Codes |
|---|---|---|---|
| `POST` | `/api/teachers` | Create a teacher (Admin only) | `201 Created`, `400 Bad Request`, `403 Forbidden`, `409 Conflict` |
| `GET` | `/api/teachers` | Retrieve all teachers | `200 OK`, `401 Unauthorized`, `403 Forbidden` |
| `GET` | `/api/teachers/{id}` | Retrieve teacher by ID | `200 OK`, `404 Not Found` |
| `PUT` | `/api/teachers/{id}` | Update teacher (Admin or Own Profile) | `200 OK`, `400 Bad Request`, `403 Forbidden`, `404 Not Found`, `409 Conflict` |
| `DELETE` | `/api/teachers/{id}` | Delete teacher (Admin or Own Profile) | `204 No Content`, `403 Forbidden`, `404 Not Found` |

### Student Endpoints (`/api/students`)
| Method | Endpoint | Description | Status Codes |
|---|---|---|---|
| `POST` | `/api/students` | Register student (Admin: auto-approved, Teacher: pending approval) | `201 Created`, `400 Bad Request`, `403 Forbidden`, `409 Conflict` |
| `PUT` | `/api/students/{id}/approve` | Approve student (Admin only) | `200 OK`, `403 Forbidden`, `404 Not Found` |
| `GET` | `/api/students` | Retrieve students (Admin: all, Teacher/Student: approved only) | `200 OK`, `401 Unauthorized`, `403 Forbidden` |
| `GET` | `/api/students/{id}` | Retrieve student by ID | `200 OK`, `404 Not Found` |
| `PUT` | `/api/students/{id}` | Update student (Admin or Own Profile only) | `200 OK`, `400 Bad Request`, `403 Forbidden`, `404 Not Found`, `409 Conflict` |
| `DELETE` | `/api/students/{id}` | Delete student (Admin or Own Profile only) | `204 No Content`, `403 Forbidden`, `404 Not Found` |

---

## 7. How to Run Locally Using Maven

```bash
# Run unit and integration tests
./mvnw clean test

# Run application
./mvnw spring-boot:run
```
Once running, open your web browser at:
`http://localhost:8080`

---

## 8. How to Run Using Docker Compose

```bash
# Build and start containers
docker compose up --build -d

# Check container status
docker compose ps

# View live application logs
docker compose logs -f app
```
The application will be accessible at:
`http://localhost:8080`
