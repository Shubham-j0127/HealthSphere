HealthSphere – EHR & Telemedicine Platform

HealthSphere is a full-stack Electronic Health Record (EHR) and Telemedicine platform designed to securely manage patient data, enable doctor–patient interaction, and support virtual consultations.

Project Structure
HealthSphere/
├── backend/          # Spring Boot application
└── frontend/         # React application

Technology Stack

Backend

Java 17
Spring Boot
Spring Security
JWT Authentication
Spring Data JPA
PostgreSQL
Maven
Frontend
React (Vite)
Axios
React Router
Basic CSS

Core Features
Security

JWT-based authentication

Role-Based Access Control (PATIENT, DOCTOR, ADMIN)

BCrypt password hashing

Secured REST APIs

Backend Modules

User authentication (Login & Register)

Electronic Health Record (EHR) management

Appointment booking and management

Prescription creation and viewing

Basic telemedicine using WebRTC signaling

Notification system for reminders and updates

#Frontend Features

Login & Register pages

Role-based dashboards (Patient & Doctor)

Medical record view and management

Appointment scheduling and status updates

Prescription management

Telemedicine video consultation interface

Notification center with unread indicators

#Database

PostgreSQL

JPA/Hibernate OR

Proper entity relationships between users, patients, doctors, and records

Local Setup Instructions
1. Prerequisites

Java 17
Maven
PostgreSQL
Node.js (18+)

2. Database Setup
CREATE DATABASE healthsphere;

Update database credentials in:

backend/src/main/resources/application.properties

3. Run Backend
cd backend
mvn spring-boot:run

Backend runs on:

http://localhost:8080

4. Run Frontend
cd frontend
npm install
npm run dev


Frontend runs on:

http://localhost:5173

API Overview
Authentication

POST /api/auth/register
POST /api/auth/login

Medical Records
Create, view, and update medical records (role-based access)

Appointments
Book and manage appointments

Update appointment status
Prescriptions
Doctor creates prescriptions

Patient views prescriptions

Telemedicine
WebRTC-based video consultation

REST-based signaling endpoints

Notifications

Appointment reminders

Prescription notifications

Read/unread notification tracking

Project Status

✅ Completed
All planned backend and frontend features are implemented and tested.

Future Enhancements

WebSocket-based real-time notifications

TURN server for advanced WebRTC support

Mobile application support

Email/SMS notification integration

Internship Information

This project was developed during my Java Full Stack Internship at Zaalima Development, focusing on real-world healthcare system design and secure application development.