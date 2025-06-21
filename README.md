# Cabinet360 Core Service

🏥 **Medical Workflow Management Microservice** - Comprehensive appointment scheduling, patient management, and healthcare analytics platform.

## 🌟 Features

### 📅 Appointment Management
- **Smart scheduling** with conflict detection and resolution
- **Multi-provider support** for doctors, specialists, and assistants
- **Flexible time slots** with customizable duration and availability
- **Automated reminders** and notification integration
- **Recurring appointments** for ongoing treatment plans
- **Emergency slot management** for urgent cases

### 👥 Patient Management
- **Comprehensive patient profiles** with medical history integration
- **Demographic management** with insurance and contact information
- **Patient assignment** to primary care physicians
- **Medical record linking** across service boundaries
- **Patient portal access** for self-service capabilities

### 📊 Healthcare Analytics
- **Real-time dashboards** for appointment statistics
- **Provider performance metrics** and utilization reports
- **Patient flow analysis** and waiting time optimization
- **Revenue tracking** and billing integration
- **Compliance reporting** for healthcare regulations

### 🔄 Service Integration
- **Auth service integration** for secure access control
- **AI service connectivity** for intelligent recommendations
- **Medical record synchronization** across platforms
- **Notification service** for multi-channel communication

## 🏗️ Architecture

~~~
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Auth Service  │    │   AI Service    │    │   Notification  │
│   (Port 8091)   │    │   (Port 8100)   │    │   Service       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
              ┌─────────────────────────────────┐
              │        Core Service             │
              │         (Port 8092)             │
              │                                 │
              │  ┌─────────────────────────┐    │
              │  │  Appointment Engine     │    │
              │  │  - Scheduling Logic     │    │
              │  │  - Conflict Detection   │    │
              │  │  - Availability Mgmt    │    │
              │  └─────────────────────────┘    │
              │                                 │
              │  ┌─────────────────────────┐    │
              │  │  Patient Management     │    │
              │  │  - Demographics         │    │
              │  │  - Medical History      │    │
              │  │  - Provider Assignment  │    │
              │  └─────────────────────────┘    │
              └─────────────────────────────────┘
                                 │
                    ┌─────────────────┐
                    │   PostgreSQL    │
                    │ core_service_db │
                    └─────────────────┘
~~~

## 🚀 Quick Start

### Prerequisites
- **Java 17+**
- **Maven 3.8+**
- **PostgreSQL 15+**
- **Docker** (for containerized deployment)
- **Running Auth Service** (for JWT validation)

### 1. Environment Setup

Create `.env` file:
~~~env
# Server Configuration
PORT=8092

# Database Configuration
DB_HOST=localhost
DB_PORT=5442
DB_NAME=core_service_db
DB_USERNAME=user
DB_PASSWORD=pass

# JWT Configuration (must match auth-service)
JWT_SECRET=cabinet360supersecurekeymustbeatleast32chars!

# External Service URLs
AUTH_SERVICE_URL=http://localhost:8091
AI_SERVICE_URL=http://localhost:8100
NOTIFICATION_SERVICE_URL=http://localhost:8098

# Appointment Configuration
DEFAULT_APPOINTMENT_DURATION=30
ADVANCE_BOOKING_DAYS=90
CANCELLATION_NOTICE_HOURS=24
WORKING_HOURS_START=08:00
WORKING_HOURS_END=18:00

# Business Logic
AUTO_CONFIRM_APPOINTMENTS=false
SEND_REMINDERS=true
REMINDER_HOURS_BEFORE=24,2

# Development Settings
DEV_MODE=false
ENABLE_SWAGGER=true
~~~

### 2. Database Setup

~~~sql
-- Create database and user
CREATE DATABASE core_service_db;
CREATE USER core_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE core_service_db TO core_user;

-- Connect to database
\c core_service_db;

-- Tables will be auto-created by JPA/Hibernate
~~~

### 3. Run the Service

~~~bash
# Development mode
mvn spring-boot:run

# With specific profile
mvn spring-boot:run -Dspring.profiles.active=dev

# Production build
mvn clean package
java -jar target/core-service-0.0.1-SNAPSHOT.jar
~~~

### 4. Docker Deployment

~~~bash
# Build Docker image
docker build -t cabinet360/core-service .

# Run with Docker Compose
docker-compose up -d core-service

# Standalone run
docker run -p 8092:8092 --env-file .env cabinet360/core-service
~~~

## 📚 API Documentation

### Base URL: `http://localhost:8092/api/v1/core`

### 🔒 Authentication
All endpoints require valid JWT token:
~~~
Authorization: Bearer <jwt_token>
~~~

### 📅 Appointment Endpoints

#### Create Appointment
~~~http
POST /rendez-vous
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "patientUserId": 123,
  "medecinUserId": 456,
  "dateHeure": "2024-12-25T10:00:00",
  "dureeMinutes": 30,
  "motif": "Consultation de routine",
  "notes": "Patient reports mild symptoms",
  "priorite": "NORMALE",
  "typeConsultation": "CONSULTATION"
}
~~~

**Response:**
~~~json
{
  "id": 789,
  "patientUserId": 123,
  "medecinUserId": 456,
  "dateHeure": "2024-12-25T10:00:00",
  "dureeMinutes": 30,
  "statut": "CONFIRME",
  "motif": "Consultation de routine",
  "notes": "Patient reports mild symptoms",
  "createdAt": "2024-12-21T10:00:00Z",
  "confirmationNumber": "APT-789-2024"
}
~~~

#### Get All Appointments
~~~http
GET /rendez-vous
Authorization: Bearer <jwt_token>
~~~

#### Get Appointment by ID
~~~http
GET /rendez-vous/{id}
Authorization: Bearer <jwt_token>
~~~

#### Get Appointments by Patient
~~~http
GET /rendez-vous/patient/{patientUserId}
Authorization: Bearer <jwt_token>
~~~

#### Get Appointments by Doctor
~~~http
GET /rendez-vous/doctor/{medecinUserId}
Authorization: Bearer <jwt_token>
~~~

#### Get Appointments by Date Range
~~~http
GET /rendez-vous/date-range?start=2024-12-20T00:00:00&end=2024-12-27T23:59:59
Authorization: Bearer <jwt_token>
~~~

#### Get Doctor's Today Appointments
~~~http
GET /rendez-vous/doctor/{medecinUserId}/today
Authorization: Bearer <jwt_token>
~~~

**Response:**
~~~json
{
  "appointments": [
    {
      "id": 789,
      "patientUserId": 123,
      "patientName": "Jean Dupont",
      "dateHeure": "2024-12-21T10:00:00",
      "dureeMinutes": 30,
      "statut": "CONFIRME",
      "motif": "Consultation de routine"
    }
  ],
  "totalCount": 1,
  "date": "2024-12-21"
}
~~~

#### Update Appointment
~~~http
PUT /rendez-vous/{id}
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "dateHeure": "2024-12-25T14:00:00",
  "dureeMinutes": 45,
  "statut": "CONFIRME",
  "notes": "Updated appointment time"
}
~~~

#### Cancel Appointment
~~~http
DELETE /rendez-vous/{id}
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "reason": "Patient requested cancellation",
  "notifyPatient": true
}
~~~

### 🗓️ Scheduling Utilities

#### Check Scheduling Conflicts
~~~http
POST /rendez-vous/check-conflicts
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "medecinUserId": 456,
  "patientUserId": 123,
  "dateHeure": "2024-12-25T10:00:00",
  "dureeMinutes": 30
}
~~~

**Response:**
~~~json
{
  "hasConflicts": false,
  "conflicts": [],
  "suggestions": [
    {
      "dateHeure": "2024-12-25T10:30:00",
      "available": true,
      "reason": "Next available slot"
    },
    {
      "dateHeure": "2024-12-25T11:00:00", 
      "available": true,
      "reason": "Alternative time"
    }
  ]
}
~~~

#### Get Available Time Slots
~~~http
GET /rendez-vous/doctor/{medecinUserId}/available-slots?date=2024-12-25&duration=30
Authorization: Bearer <jwt_token>
~~~

**Response:**
~~~json
{
  "date": "2024-12-25",
  "doctorId": 456,
  "availableSlots": [
    {
      "startTime": "08:00:00",
      "endTime": "08:30:00",
      "available": true
    },
    {
      "startTime": "08:30:00", 
      "endTime": "09:00:00",
      "available": true
    },
    {
      "startTime": "09:00:00",
      "endTime": "09:30:00",
      "available": false,
      "reason": "Already booked"
    }
  ],
  "workingHours": {
    "start": "08:00:00",
    "end": "18:00:00"
  }
}
~~~

### 👥 Patient Management

#### Get Patient Information
~~~http
GET /patients/{patientUserId}
Authorization: Bearer <jwt_token>
~~~

**Response:**
~~~json
{
  "userId": 123,
  "firstName": "Jean",
  "lastName": "Dupont",
  "email": "jean.dupont@email.com",
  "dateOfBirth": "1980-05-15",
  "gender": "M",
  "phone": "+33-1-23-45-67-89",
  "address": "123 Rue de la Santé, Paris",
  "assignedDoctorId": 456,
  "insuranceNumber": "1234567890123",
  "emergencyContact": {
    "name": "Marie Dupont",
    "phone": "+33-1-98-76-54-32",
    "relationship": "Spouse"
  },
  "medicalInfo": {
    "bloodType": "A+",
    "allergies": ["Penicillin", "Nuts"],
    "chronicConditions": ["Diabetes Type 2"]
  }
}
~~~

#### Get Patient Appointments History
~~~http
GET /patients/{patientUserId}/appointments?limit=10&status=TERMINE
Authorization: Bearer <jwt_token>
~~~

#### Update Patient Assignment
~~~http
PUT /patients/{patientUserId}/assign-doctor
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "doctorUserId": 789,
  "reason": "Specialty referral",
  "effectiveDate": "2024-12-25"
}
~~~

### 👨‍⚕️ Doctor Management

#### Get Doctor Schedule
~~~http
GET /doctors/{doctorUserId}/schedule?date=2024-12-25
Authorization: Bearer <jwt_token>
~~~

**Response:**
~~~json
{
  "doctorId": 456,
  "date": "2024-12-25",
  "workingHours": {
    "start": "08:00:00",
    "end": "18:00:00"
  },
  "appointments": [
    {
      "id": 789,
      "patientName": "Jean Dupont",
      "startTime": "10:00:00",
      "endTime": "10:30:00",
      "status": "CONFIRME",
      "type": "CONSULTATION"
    }
  ],
  "availableSlots": [
    {
      "start": "08:00:00",
      "end": "08:30:00"
    },
    {
      "start": "08:30:00", 
      "end": "09:00:00"
    }
  ],
  "statistics": {
    "totalAppointments": 8,
    "confirmedAppointments": 7,
    "cancelledAppointments": 1,
    "utilizationRate": 75.5
  }
}
~~~

#### Get Doctor Statistics
~~~http
GET /doctors/{doctorUserId}/stats?period=month
Authorization: Bearer <jwt_token>
~~~

**Response:**
~~~json
{
  "doctorId": 456,
  "period": "2024-12",
  "statistics": {
    "totalAppointments": 156,
    "completedAppointments": 142,
    "cancelledAppointments": 8,
    "noShowAppointments": 6,
    "averageAppointmentDuration": 32.5,
    "patientSatisfactionScore": 4.7,
    "utilizationRate": 87.3,
    "revenueGenerated": 15600.00
  },
  "trendsComparison": {
    "previousPeriod": "2024-11",
    "appointmentChange": "+12%",
    "utilizationChange": "+5.2%",
    "satisfactionChange": "+0.3"
  }
}
~~~

### 📊 Analytics & Reporting

#### Get Overall Statistics
~~~http
GET /stats/overview
Authorization: Bearer <admin_jwt_token>
~~~

**Response:**
~~~json
{
  "period": "2024-12-21",
  "appointments": {
    "total": 1250,
    "today": 45,
    "confirmed": 1134,
    "cancelled": 89,
    "noShow": 27
  },
  "patients": {
    "total": 3456,
    "active": 2890,
    "newThisMonth": 123
  },
  "doctors": {
    "total": 67,
    "activeToday": 42,
    "averageUtilization": 82.5
  },
  "revenue": {
    "today": 4500.00,
    "thisMonth": 125000.00,
    "previousMonth": 118000.00,
    "growth": 5.9
  }
}
~~~

#### Get Appointment Analytics
~~~http
GET /stats/appointments?startDate=2024-12-01&endDate=2024-12-31&groupBy=day
Authorization: Bearer <jwt_token>
~~~

#### Get Doctor Performance Report
~~~http
GET /stats/doctors/performance?period=month
Authorization: Bearer <admin_jwt_token>
~~~

### 🏥 Health & Monitoring

#### Health Check
~~~http
GET /health
~~~

**Response:**
~~~json
{
  "status": "UP",
  "service": "core-service",
  "timestamp": "2024-12-21T10:00:00Z",
  "version": "1.0.0",
  "dependencies": {
    "database": "UP",
    "authService": "UP",
    "aiService": "UP",
    "notificationService": "UP"
  },
  "metrics": {
    "activeAppointments": 1134,
    "dailyAppointments": 45,
    "systemLoad": 0.65
  }
}
~~~

## 🗄️ Database Schema

### Core Tables

~~~sql
-- Appointments table
CREATE TABLE rendez_vous (
    id BIGSERIAL PRIMARY KEY,
    patient_user_id BIGINT NOT NULL,
    medecin_user_id BIGINT NOT NULL,
    date_heure TIMESTAMP NOT NULL,
    duree_minutes INTEGER NOT NULL DEFAULT 30,
    statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE',
    motif VARCHAR(500),
    notes TEXT,
    priorite VARCHAR(20) DEFAULT 'NORMALE',
    type_consultation VARCHAR(50) DEFAULT 'CONSULTATION',
    salle VARCHAR(50),
    confirmation_number VARCHAR(50) UNIQUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    created_by BIGINT,
    cancelled_at TIMESTAMP,
    cancellation_reason TEXT,
    reminder_sent BOOLEAN DEFAULT false,
    
    CONSTRAINT chk_statut CHECK (statut IN ('EN_ATTENTE', 'CONFIRME', 'TERMINE', 'ANNULE', 'REPORTE')),
    CONSTRAINT chk_priorite CHECK (priorite IN ('BASSE', 'NORMALE', 'HAUTE', 'URGENTE')),
    CONSTRAINT chk_duree CHECK (duree_minutes > 0 AND duree_minutes <= 480)
);

-- Patients table (lightweight, references auth-service)
CREATE TABLE patients (
    id BIGSERIAL PRIMARY KEY,
    patient_user_id BIGINT UNIQUE NOT NULL,
    assigned_doctor_id BIGINT,
    date_inscription TIMESTAMP DEFAULT NOW(),
    statut_patient VARCHAR(20) DEFAULT 'ACTIF',
    notes_medicales TEXT,
    preferences_rdv JSONB,
    
    CONSTRAINT chk_statut_patient CHECK (statut_patient IN ('ACTIF', 'INACTIF', 'SUSPENDU'))
);

-- Doctors table (lightweight, references auth-service)
CREATE TABLE medecins (
    id BIGSERIAL PRIMARY KEY,
    medecin_user_id BIGINT UNIQUE NOT NULL,
    specialite VARCHAR(100),
    numero_licence VARCHAR(50) UNIQUE,
    horaires_travail JSONB,
    consultation_fee DECIMAL(10,2),
    accepte_nouveaux_patients BOOLEAN DEFAULT true,
    statut_medecin VARCHAR(20) DEFAULT 'ACTIF',
    
    CONSTRAINT chk_statut_medecin CHECK (statut_medecin IN ('ACTIF', 'INACTIF', 'VACANCES', 'SUSPENDU'))
);

-- Assistants table (lightweight, references auth-service)
CREATE TABLE assistants (
    id BIGSERIAL PRIMARY KEY,
    assistant_user_id BIGINT UNIQUE NOT NULL,
    medecin_assigne_id BIGINT REFERENCES medecins(id),
    permissions JSONB,
    date_affectation TIMESTAMP DEFAULT NOW()
);

-- Appointment history for audit
CREATE TABLE rendez_vous_historique (
    id BIGSERIAL PRIMARY KEY,
    rendez_vous_id BIGINT REFERENCES rendez_vous(id),
    action VARCHAR(50) NOT NULL,
    ancien_statut VARCHAR(20),
    nouveau_statut VARCHAR(20),
    details JSONB,
    user_id BIGINT NOT NULL,
    timestamp TIMESTAMP DEFAULT NOW()
);

-- Doctor availability schedules
CREATE TABLE disponibilites_medecin (
    id BIGSERIAL PRIMARY KEY,
    medecin_user_id BIGINT NOT NULL,
    jour_semaine INTEGER NOT NULL, -- 0=Sunday, 1=Monday, etc.
    heure_debut TIME NOT NULL,
    heure_fin TIME NOT NULL,
    actif BOOLEAN DEFAULT true,
    
    CONSTRAINT chk_jour_semaine CHECK (jour_semaine >= 0 AND jour_semaine <= 6),
    CONSTRAINT chk_heures CHECK (heure_debut < heure_fin)
);

-- Doctor unavailability (vacations, breaks, etc.)
CREATE TABLE indisponibilites_medecin (
    id BIGSERIAL PRIMARY KEY,
    medecin_user_id BIGINT NOT NULL,
    date_debut TIMESTAMP NOT NULL,
    date_fin TIMESTAMP NOT NULL,
    motif VARCHAR(200),
    type_indisponibilite VARCHAR(50) DEFAULT 'PERSONNEL',
    
    CONSTRAINT chk_dates CHECK (date_debut < date_fin)
);

-- Appointment reminders tracking
CREATE TABLE rappels_rdv (
    id BIGSERIAL PRIMARY KEY,
    rendez_vous_id BIGINT REFERENCES rendez_vous(id),
    type_rappel VARCHAR(20) NOT NULL, -- 'EMAIL', 'SMS', 'PUSH'
    statut_envoi VARCHAR(20) DEFAULT 'EN_ATTENTE',
    date_envoi_prevue TIMESTAMP NOT NULL,
    date_envoi_reel TIMESTAMP,
    tentatives INTEGER DEFAULT 0,
    message_erreur TEXT
);
~~~

### Performance Indexes

~~~sql
-- Appointment lookup indexes
CREATE INDEX idx_rdv_patient_user_id ON rendez_vous(patient_user_id);
CREATE INDEX idx_rdv_medecin_user_id ON rendez_vous(medecin_user_id);
CREATE INDEX idx_rdv_date_heure ON rendez_vous(date_heure);
CREATE INDEX idx_rdv_statut ON rendez_vous(statut);
CREATE INDEX idx_rdv_date_statut ON rendez_vous(date_heure, statut);
CREATE INDEX idx_rdv_medecin_date ON rendez_vous(medecin_user_id, date_heure);
CREATE INDEX idx_rdv_patient_date ON rendez_vous(patient_user_id, date_heure);

-- Patient and doctor indexes
CREATE INDEX idx_patients_user_id ON patients(patient_user_id);
CREATE INDEX idx_patients_assigned_doctor ON patients(assigned_doctor_id);
CREATE INDEX idx_medecins_user_id ON medecins(medecin_user_id);
CREATE INDEX idx_medecins_specialite ON medecins(specialite);

-- Availability indexes
CREATE INDEX idx_disponibilites_medecin_jour ON disponibilites_medecin(medecin_user_id, jour_semaine);
CREATE INDEX idx_indisponibilites_medecin_dates ON indisponibilites_medecin(medecin_user_id, date_debut, date_fin);

-- Audit and history indexes
CREATE INDEX idx_rdv_historique_rdv_id ON rendez_vous_historique(rendez_vous_id);
CREATE INDEX idx_rdv_historique_timestamp ON rendez_vous_historique(timestamp DESC);

-- Reminder indexes
CREATE INDEX idx_rappels_rdv_id ON rappels_rdv(rendez_vous_id);
CREATE INDEX idx_rappels_envoi_prevu ON rappels_rdv(date_envoi_prevue);
CREATE INDEX idx_rappels_statut ON rappels_rdv(statut_envoi);
~~~

## 🔧 Configuration

### Application Properties

~~~properties
# Server Configuration
server.port=${PORT:8092}

# Database Configuration
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5442}/${DB_NAME:core_service_db}
spring.datasource.username=${DB_USERNAME:user}
spring.datasource.password=${DB_PASSWORD:pass}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=${DEV_MODE:false}
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.jdbc.time_zone=UTC

# JWT Configuration
jwt.secret=${JWT_SECRET:cabinet360supersecurekeymustbeatleast32chars!}

# External Services
auth.service.url=${AUTH_SERVICE_URL:http://auth-service:8091}
ai.service.url=${AI_SERVICE_URL:http://ai-service:8100}
notification.service.url=${NOTIFICATION_SERVICE_URL:http://notification-service:8098}

# Business Logic Configuration
appointment.default-duration=${DEFAULT_APPOINTMENT_DURATION:30}
appointment.advance-booking-days=${ADVANCE_BOOKING_DAYS:90}
appointment.cancellation-notice-hours=${CANCELLATION_NOTICE_HOURS:24}
appointment.auto-confirm=${AUTO_CONFIRM_APPOINTMENTS:false}

# Working Hours
working-hours.start=${WORKING_HOURS_START:08:00}
working-hours.end=${WORKING_HOURS_END:18:00}

# Reminders
reminders.enabled=${SEND_REMINDERS:true}
reminders.hours-before=${REMINDER_HOURS_BEFORE:24,2}

# Performance Configuration
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000

# Caching
spring.cache.type=simple
spring.cache.cache-names=doctors,patients,appointments,availability

# Logging
logging.level.com.cabinet360.core=INFO
logging.level.org.springframework.web=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# Actuator
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when-authorized
management.metrics.export.prometheus.enabled=true

# Swagger/OpenAPI
springdoc.api-docs.enabled=${ENABLE_SWAGGER:true}
springdoc.swagger-ui.enabled=${ENABLE_SWAGGER:true}
springdoc.swagger-ui.path=/swagger-ui.html
~~~

## 🧪 Testing

### Unit Tests
~~~bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=RendezVousServiceTest

# Run with coverage report
mvn test jacoco:report
open target/site/jacoco/index.html
~~~

### Integration Tests
~~~bash
# Run integration tests
mvn test -Dtest=**/*IntegrationTest

# Test with embedded database
mvn test -Dspring.profiles.active=test

# Test with testcontainers
mvn test -Dspring.profiles.active=integration
~~~

### API Testing Examples

#### Test Appointment Creation
~~~bash
# Get JWT token first
TOKEN=$(curl -s -X POST http://localhost:8091/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"doctor@test.com","password":"password"}' | \
  jq -r '.token')

# Create appointment
curl -X POST http://localhost:8092/api/v1/core/rendez-vous \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "patientUserId": 123,
    "medecinUserId": 456,
    "dateHeure": "2024-12-25T10:00:00",
    "motif": "Test appointment"
  }'
~~~

#### Test Conflict Detection
~~~bash
# Check for conflicts
curl -X POST http://localhost:8092/api/v1/core/rendez-vous/check-conflicts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "medecinUserId": 456,
    "dateHeure": "2024-12-25T10:00:00",
    "dureeMinutes": 30
  }'
~~~

### Load Testing
~~~bash
# Install Artillery
npm install -g artillery

# Run appointment booking load test
artillery run tests/load/appointment-booking.yml

# Run availability check load test  
artillery run tests/load/availability-check.yml
~~~

Example load test configuration:
~~~yaml
config:
  target: 'http://localhost:8092'
  phases:
    - duration: 60
      arrivalRate: 10
  variables:
    auth_token: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

scenarios:
  - name: "Book appointments"
    requests:
      - post:
          url: "/api/v1/core/rendez-vous"
          headers:
            Authorization: "Bearer {{ auth_token }}"
            Content-Type: "application/json"
          json:
            patientUserId: 123
            medecinUserId: 456
            dateHeure: "2024-12-25T{{ $randomInt(8, 17) }}:{{ $randomInt(0, 59) }}:00"
            motif: "Load test appointment"
~~~

## 🚀 Production Deployment

### Docker Production Build
~~~dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests -Dmaven.javadoc.skip=true

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8092

HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8092/api/v1/core/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "/app.jar"]
~~~

### Kubernetes Deployment
~~~yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: core-service
  labels:
    app: core-service
    version: v1
spec:
  replicas: 3
  selector:
    matchLabels:
      app: core-service
  template:
    metadata:
      labels:
        app: core-service
        version: v1
    spec:
      containers:
      - name: core-service
        image: cabinet360/core-service:latest
        ports:
        - containerPort: 8092
        env:
        - name: DB_HOST
          value: "postgres-service"
        - name: AUTH_SERVICE_URL
          value: "http://auth-service:8091"
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: cabinet360-secrets
              key: jwt-secret
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /api/v1/core/health
            port: 8092
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /api/v1/core/health
            port: 8092
          initialDelaySeconds: 30
          periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: core-service
spec:
  selector:
    app: core-service
  ports:
    - protocol: TCP
      port: 8092
      targetPort: 8092
  type: ClusterIP
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: core-service-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  rules:
  - host: api.cabinet360.com
    http:
      paths:
      - path: /api/v1/core
        pathType: Prefix
        backend:
          service:
            name: core-service
            port:
              number: 8092
~~~

## 📊 Monitoring & Observability

### Metrics Collection
~~~properties
management.metrics.tags.application=core-service
management.metrics.tags.environment=${ENVIRONMENT:development}
management.metrics.enable.appointment.bookings=true
management.metrics.enable.appointment.cancellations=true
management.metrics.enable.doctor.utilization=true
~~~

### Custom Business Metrics
~~~java
@Component
public class AppointmentMetrics {
    private final MeterRegistry meterRegistry;
    private final Counter appointmentBookings;
    private final Counter appointmentCancellations;
    private final Timer appointmentDuration;
    public AppointmentMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.appointmentBookings = Counter.builder("appointments.booked")
            .description("Number of appointments booked")
            .register(meterRegistry);
        this.appointmentCancellations = Counter.builder("appointments.cancelled")
            .description("Number of appointments cancelled")
            .register(meterRegistry);
        this.appointmentDuration = Timer.builder("appointments.duration")
            .description("Appointment processing time")
            .register(meterRegistry);
    }
    public void recordBooking() { appointmentBookings.increment(); }
    public void recordCancellation() { appointmentCancellations.increment(); }
}
~~~

### Logging Configuration
~~~xml
<configuration>
    <springProfile name="prod">
        <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <version/>
                    <logLevel/>
                    <loggerName/>
                    <mdc/>
                    <message/>
                    <stackTrace/>
                </providers>
            </encoder>
        </appender>
        <logger name="com.cabinet360.core" level="INFO"/>
        <logger name="org.springframework.web" level="WARN"/>
        <logger name="org.hibernate.SQL" level="WARN"/>
        <root level="INFO">
            <appender-ref ref="STDOUT"/>
        </root>
    </springProfile>
    <springProfile name="!prod">
        <include resource="org/springframework/boot/logging/logback/console-appender.xml"/>
        <logger name="com.cabinet360.core" level="DEBUG"/>
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>
</configuration>
~~~

## 🚨 Troubleshooting

### Common Issues

#### Database Connection Problems
~~~bash
docker-compose exec core-service-db psql -U user -d core_service_db -c "SELECT count(*) FROM rendez_vous;"
curl http://localhost:8092/actuator/metrics/hikaricp.connections.active
curl http://localhost:8092/actuator/metrics/hikaricp.connections.usage
docker-compose logs core-service | grep -i "connection"
~~~

#### Appointment Scheduling Issues
~~~bash
docker-compose logs core-service | grep -i "conflict\|schedule"
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8092/api/v1/core/rendez-vous/doctor/456/available-slots?date=2024-12-25"
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8092/api/v1/core/rendez-vous/doctor/456/date/2024-12-25T10:00:00"
~~~

#### Performance Issues
~~~bash
curl http://localhost:8092/actuator/metrics/jvm.memory.used
curl http://localhost:8092/actuator/metrics/http.server.requests
docker-compose exec core-service-db pg_stat_activity
docker-compose exec core-service-db psql -U user -d core_service_db -c "
  SELECT query, mean_time, calls 
  FROM pg_stat_statements 
  ORDER BY mean_time DESC 
  LIMIT 10;"
~~~

#### Service Integration Issues
~~~bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8091/api/v1/auth/validate
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8100/api/v1/ai/health
docker-compose ps
docker network ls
~~~

### Debug Mode
~~~bash
java -jar target/core-service.jar \
  --logging.level.com.cabinet360.core=DEBUG \
  --logging.level.org.springframework.web=DEBUG \
  --spring.jpa.show-sql=true
~~~

### Performance Tuning

#### Database Optimization
~~~sql
ANALYZE rendez_vous;
ANALYZE patients;
ANALYZE medecins;
EXPLAIN (ANALYZE, BUFFERS) 
SELECT * FROM rendez_vous 
WHERE medecin_user_id = 456 
  AND date_heure >= '2024-12-25'::date 
  AND date_heure < '2024-12-26'::date;
CREATE INDEX CONCURRENTLY idx_rdv_doctor_date_status 
ON rendez_vous(medecin_user_id, date_heure, statut);
~~~

#### Application Tuning
~~~properties
JAVA_OPTS=-XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UseStringDeduplication
spring.datasource.hikari.maximum-pool-size=25
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.leak-detection-threshold=60000
spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=10m
~~~

## 🔒 Security Considerations

### API Security
~~~java
@RateLimiter(name = "appointment-booking", fallbackMethod = "bookingFallback")
public ResponseEntity<?> createAppointment(@RequestBody AppointmentDto appointment) {
    // Implementation
}
@Valid
public class AppointmentDto {
    @NotNull
    @Future
    private LocalDateTime dateHeure;
    @Min(15)
    @Max(480)
    private Integer dureeMinutes;
    @NotBlank
    @Size(max = 500)
    private String motif;
}
~~~

### Data Protection
~~~properties
spring.jpa.properties.hibernate.use_sql_comments=false
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.properties.hibernate.integrator_provider=com.cabinet360.core.audit.AuditIntegratorProvider
~~~

## 🤝 Contributing

### Development Guidelines
1. **Clean Architecture**: Follow domain-driven design principles
2. **API First**: Design APIs before implementation
3. **Test Coverage**: Maintain >80% test coverage
4. **Performance**: Consider scalability in all designs
5. **Security**: Validate all inputs and authorize all operations

### Code Review Checklist
- [ ] Business logic properly tested
- [ ] Database queries optimized
- [ ] API endpoints documented
- [ ] Security validations in place
- [ ] Error handling implemented
- [ ] Metrics and logging added

## 📋 Roadmap

### Current Version (v1.0)
- ✅ Basic appointment CRUD operations
- ✅ Conflict detection and resolution
- ✅ Doctor and patient management
- ✅ Basic analytics and reporting
- ✅ Service integration

### Next Release (v1.1)
- 🔄 Advanced scheduling algorithms
- 🔄 Recurring appointment templates
- 🔄 Waitlist management
- 🔄 Mobile push notifications
- 🔄 Enhanced analytics dashboard

### Future Releases (v2.0+)
- 📅 AI-powered scheduling optimization
- 📅 Predictive analytics for no-shows
- 📅 Multi-location support
- 📅 Integration with external calendars
- 📅 Telemedicine scheduling

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

### Documentation
- **API Documentation**: Available at `/swagger-ui.html`
- **Database Schema**: See `docs/database-schema.md`
- **Integration Guide**: See `docs/integration-guide.md`

### Getting Help
- **Issues**: Report bugs via GitHub Issues
- **Feature Requests**: Use GitHub Discussions
- **Email**: [nawfalrazouk7@gmail.com](mailto:nawfalrazouk7@gmailcom)

### Performance Issues
- **Critical Issues**: [Email](mailto:nawfalrazouk7@gmailcom)
- **Response Time**: 2-4 hours for critical issues

---

🏥 **Cabinet360 Core Service** - The Heart of Healthcare Management
