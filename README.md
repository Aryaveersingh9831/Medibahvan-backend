# MediBhavan — Spring Boot Backend

Full REST API backend for the MediBhavan medical records platform.
Built with Java 17 + Spring Boot 3 + MongoDB.

---

## Prerequisites

| Tool | Version | Download |
|------|---------|----------|
| Java JDK | 17+ | https://adoptium.net |
| Maven | 3.9+ | Bundled with IntelliJ, or https://maven.apache.org |
| MongoDB | 6+ | https://www.mongodb.com/try/download/community OR use Atlas |
| IntelliJ IDEA | Any | https://www.jetbrains.com/idea (Community edition is free) |

---
## Project Structure

```
src/main/java/com/medibhavan/
├── MediBhavanApplication.java        ← Entry point
│
├── config/
│   └── SecurityConfig.java           ← Spring Security + CORS + JWT filter chain
│
├── controller/                       ← HTTP layer — handles requests/responses
│   ├── AuthController.java
│   ├── ConnectionController.java
│   ├── FileController.java
│   ├── MessageController.java
│   ├── AppointmentController.java
│   └── HealthController.java
│
├── service/                          ← Business logic layer
│   ├── AuthService.java
│   ├── ConnectionService.java
│   ├── FileService.java
│   ├── MessageService.java
│   └── AppointmentService.java
│
├── repository/                       ← Database layer (Spring Data MongoDB)
│   ├── UserRepository.java
│   ├── ConnectionRepository.java
│   ├── MedicalFileRepository.java
│   ├── MessageRepository.java
│   └── AppointmentRepository.java
│
├── model/                            ← MongoDB document classes
│   ├── User.java
│   ├── Connection.java
│   ├── MedicalFile.java
│   ├── Message.java
│   └── Appointment.java
│
├── dto/
│   ├── request/                      ← What the API receives
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── ProfileUpdateRequest.java
│   │   ├── ChangePasswordRequest.java
│   │   ├── ConnectRequest.java
│   │   ├── SendMessageRequest.java
│   │   ├── CreateAppointmentRequest.java
│   │   └── UpdateAppointmentRequest.java
│   └── response/                     ← What the API returns
│       ├── AuthResponse.java
│       ├── UserResponse.java
│       ├── ConnectionResponse.java
│       ├── FileResponse.java
│       ├── ChatMessageResponse.java
│       ├── AppointmentResponse.java
│       ├── MessageResponse.java
│       └── UnreadCountResponse.java
│
├── security/                         ← JWT + Spring Security classes
│   ├── JwtUtil.java
│   ├── JwtAuthFilter.java
│   └── UserDetailsServiceImpl.java
│
├── exception/                        ← Error handling
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── BadRequestException.java
│
└── util/
    └── UserIdGenerator.java          ← Generates Dr_XXXXXX / P_XXXXXX IDs
```

---

## API Endpoints

All endpoints except `/api/auth/login`, `/api/auth/register`, and `/api/health`
require a Bearer token in the Authorization header:
```
Authorization: Bearer <your_jwt_token>
```

### Auth
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/auth/register` | Create account |
| POST | `/api/auth/login` | Login, get JWT token |
| GET | `/api/auth/me` | Get current user |
| PUT | `/api/auth/profile` | Update profile |
| PUT | `/api/auth/change-password` | Change password |

### Connections
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/connections/find/{userId}` | Find user by Dr_XXX or P_XXX |
| POST | `/api/connections` | Connect with doctor/patient |
| GET | `/api/connections` | List my connections |
| DELETE | `/api/connections/{id}` | Disconnect |

### Files
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/files/upload` | Upload file (multipart/form-data) |
| GET | `/api/files/my` | Get my files (patient) |
| GET | `/api/files/patient/{id}` | Get patient files (doctor) |
| GET | `/api/files/download/{id}` | Download file |
| DELETE | `/api/files/{id}` | Delete file |

### Messages
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/messages/unread` | Unread count |
| GET | `/api/messages/{connectionId}` | Get chat messages |
| POST | `/api/messages` | Send message |

### Appointments
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/appointments` | List appointments |
| POST | `/api/appointments` | Schedule appointment |
| PUT | `/api/appointments/{id}` | Update status/details |
| DELETE | `/api/appointments/{id}` | Cancel |

---

## Connect with Frontend

The frontend `index.html` is already configured to call `http://localhost:5000/api`.
Just open the HTML file in your browser while the Spring Boot server is running.

---

## Testing with Postman

1. POST `/api/auth/register` with:
```json
{
  "name": "Dr. Arya Singh",
  "email": "arya@example.com",
  "password": "password123",
  "role": "doctor"
}
```
2. Copy the `token` from the response
3. For all subsequent requests, add header: `Authorization: Bearer <token>`

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security 6 + JWT (jjwt 0.12) |
| Database | MongoDB + Spring Data MongoDB |
| Passwords | BCrypt (12 rounds) |
| File Upload | Spring MultipartFile |
| Boilerplate reduction | Lombok |
| Build tool | Maven |
