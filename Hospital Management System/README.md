# Hospital Management System

A Spring Boot + Thymeleaf web application for managing a hospital's
departments, doctors, patients, appointments and prescriptions.

## Tech Stack

- Java 21+ (project targets Java 25 in `pom.xml`)
- Spring Boot 4.1.x (Web MVC, Data JPA, Security, Thymeleaf, Validation)
- MySQL
- Bootstrap 5 (via CDN) for the UI

## Roles

- **Admin** — manages departments and doctors.
- **Doctor** — manages their weekly schedule, reviews/accepts/rejects
  appointments, marks visits complete, and writes prescriptions.
- **Patient** — registers, browses doctors/departments, books appointments,
  tracks appointment status, and views prescriptions.

## Getting Started

### 1. Create the database

```sql
CREATE DATABASE hospital_db;
```

Tables are created/updated automatically on startup
(`spring.jpa.hibernate.ddl-auto=update`).

### 2. Configure the database connection

The app reads its DB connection from environment variables, with local
defaults so it also runs out of the box against `root` with no password:

| Variable      | Default                                                                                  |
|---------------|-------------------------------------------------------------------------------------------|
| `DB_URL`      | `jdbc:mysql://localhost:3306/hospital_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `DB_USERNAME` | `root`                                                                                     |
| `DB_PASSWORD` | *(empty)*                                                                                  |

Set these in your shell/IDE run configuration if your local MySQL uses a
different user or password, e.g.:

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
```

### 3. Run the app

```bash
./mvnw spring-boot:run
```

The app starts on **http://localhost:8080**.

### 4. Log in

On first startup, a default admin account is created automatically:

- **Email:** `admin@hospital.com`
- **Password:** `admin123`

Use the admin account to add departments and doctors. Patients can
self-register from the **Register** page.

## Feature Overview

- Public marketing pages: Home, About, Contact, plus a public Doctors and
  Departments directory that anyone can browse without logging in.
- Admin: dashboard with counts, department CRUD, doctor onboarding
  (creates both the login account and the doctor profile).
- Doctor: dashboard with appointment stats, weekly schedule management
  (day/time/slot duration), appointment accept/reject/complete workflow,
  writing prescriptions for completed appointments, and an editable
  profile.
- Patient: dashboard, doctor search by specialization/department, slot
  based appointment booking, appointment history with cancel support,
  viewing prescriptions written by their doctor, and an editable profile.

## Notes

- `spring.jpa.show-sql` and Thymeleaf caching are left off/disabled for
  development convenience; turn Thymeleaf caching back on
  (`spring.thymeleaf.cache=true`) for production deployments.
- Deleting a department or doctor that still has related records
  (doctors, appointments, or a schedule) is blocked with a friendly
  message instead of a database error.
