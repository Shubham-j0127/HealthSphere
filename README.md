# HealthSphere - EHR & Telemedicine Platform

## Project Structure

```
HealthSphere/
├── backend/          # Spring Boot Application
└── frontend/         # React Application
```

## Complete Implementation (Weeks 1-4)

### Backend (Spring Boot)
- ✅ Spring Boot setup with Maven
- ✅ PostgreSQL configuration
- ✅ JWT-based authentication
- ✅ Spring Security with BCrypt password hashing
- ✅ Entities: User, Patient, Doctor, MedicalRecord, Appointment, Prescription, Notification
- ✅ Role-based access control (PATIENT, DOCTOR, ADMIN)
- ✅ Authentication APIs (login, register)
- ✅ Medical Record CRUD operations
- ✅ Appointment booking and management
- ✅ Prescription creation and viewing
- ✅ WebRTC signaling service (REST-based)
- ✅ Notification system with scheduled reminders
- ✅ User info endpoint

### Frontend (React)
- ✅ React app with Vite
- ✅ Login/Register page
- ✅ Role-based routing
- ✅ Patient Dashboard
- ✅ Doctor Dashboard
- ✅ JWT token storage
- ✅ Medical Records view/create/edit (Doctor) and view (Patient)
- ✅ Appointment booking (Patient) and management (Doctor)
- ✅ Status updates for appointments
- ✅ Prescription viewing (Patient) and creation (Doctor)
- ✅ Telemedicine/WebRTC video consultation interface
- ✅ Notification center with real-time updates
- ✅ Unread notification badges

## 🚀 Quick Start

**For cloud deployment (no local installation needed):**
- **[DEPLOYMENT_QUICK.md](DEPLOYMENT_QUICK.md)** - Quick cloud deployment guide (5 steps, ~30 minutes)

**For local development setup:**
- Install Java 17, Maven, PostgreSQL, Node.js 18+
- See setup instructions below

### Local Development Setup

1. **Install Required Software:**
   - Java 17 (JDK) - https://adoptium.net/
   - Maven 3.6+ - https://maven.apache.org/download.cgi
   - PostgreSQL 12+ - https://www.postgresql.org/download/
   - Node.js 18+ - https://nodejs.org/

2. **Setup Database:**
   ```sql
   CREATE DATABASE healthsphere;
   ```

3. **Configure Database:**
   - Edit `backend/src/main/resources/application.properties`
   - Update: `spring.datasource.password=YOUR_PASSWORD`

4. **Run Backend:**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

5. **Run Frontend (new terminal):**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

6. **Access Application:**
   - Open: `http://localhost:5173`
   - Register a new user and login!

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user

### User Management
- `GET /api/users/me` - Get current user info (including patient/doctor IDs)
- `GET /api/users/doctors` - Get list of all doctors

### Medical Records
- `POST /api/medical-records` - Create medical record (Doctor only)
- `GET /api/medical-records/patient/{patientId}` - Get records by patient
- `GET /api/medical-records/doctor/{doctorId}` - Get records by doctor
- `GET /api/medical-records/{id}` - Get specific record
- `PUT /api/medical-records/{id}` - Update record (Doctor only)

### Appointments
- `POST /api/appointments` - Book appointment (Patient)
- `GET /api/appointments/patient/{patientId}` - Get appointments by patient
- `GET /api/appointments/doctor/{doctorId}` - Get appointments by doctor
- `GET /api/appointments/{id}` - Get specific appointment
- `PUT /api/appointments/{id}/status` - Update appointment status
- `PUT /api/appointments/{id}` - Update appointment details (Doctor only)

### Prescriptions
- `POST /api/prescriptions` - Create prescription (Doctor only)
- `GET /api/prescriptions/patient/{patientId}` - Get prescriptions by patient
- `GET /api/prescriptions/doctor/{doctorId}` - Get prescriptions by doctor
- `GET /api/prescriptions/{id}` - Get specific prescription

### WebRTC Signaling
- `POST /api/webrtc/session` - Create WebRTC session
- `POST /api/webrtc/offer` - Set WebRTC offer
- `GET /api/webrtc/offer/{sessionId}` - Get WebRTC offer
- `POST /api/webrtc/answer` - Set WebRTC answer
- `GET /api/webrtc/answer/{sessionId}` - Get WebRTC answer
- `POST /api/webrtc/ice` - Add ICE candidate
- `GET /api/webrtc/ice/{sessionId}` - Get ICE candidates
- `POST /api/webrtc/end/{sessionId}` - End WebRTC session

### Notifications
- `GET /api/notifications` - Get all notifications for current user
- `GET /api/notifications/unread` - Get unread notifications
- `GET /api/notifications/unread/count` - Get unread notification count
- `PUT /api/notifications/{id}/read` - Mark notification as read
- `PUT /api/notifications/read-all` - Mark all notifications as read

## Features

### Notification System
- **Automatic Reminders**: Scheduled task checks for upcoming appointments every hour
  - 24-hour reminder for appointments scheduled tomorrow
  - 2-hour reminder for appointments starting soon
- **Event-Based Notifications**:
  - Appointment scheduled/confirmed/cancelled
  - New prescription created
  - Medical record updates (ready for future implementation)
- **Real-time Updates**: Frontend polls for new notifications every 30 seconds
- **Unread Badges**: Visual indicators for unread notifications

### Security Features
- JWT-based authentication with secure token storage
- Role-based access control (RBAC)
- BCrypt password hashing
- HIPAA-compliant approach (basic academic level)
- Secure API endpoints with authentication checks

### Telemedicine
- WebRTC-based video consultation
- REST-based signaling (no WebSocket server required)
- Google STUN server for NAT traversal
- Real-time video/audio streaming

## Project Status
✅ **COMPLETE** - All planned features implemented and tested

## Notes
- Notification scheduling runs every hour (configurable)
- WebRTC sessions are stored in-memory (suitable for development)
- For production, consider:
  - Redis for session management
  - WebSocket for real-time notifications
  - Email/SMS integration for notifications
  - TURN server for WebRTC in restrictive networks

