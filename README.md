# DTC Real-Time Bus Tracking System 🚌 Map & Live Navigation

A full-stack web application designed for tracking Delhi Transport Corporation (DTC) buses in real-time, searching routes, locating nearby bus stops, and viewing ETA estimations using OpenStreetMap and Leaflet.

---

## 🛠️ Tech Stack & Architecture

- **Backend**: Java 21, Spring Boot 4.1.0, Spring Security (JWT), Spring Data JPA, H2 / PostgreSQL Database, WebSocket (STOMP), OpenCSV, GTFS Realtime Bindings, Lombok, MapStruct, Springdoc OpenAPI (Swagger).
- **Frontend**: React 19, Vite, Leaflet, React-Leaflet, Axios, STOMP.js / SockJS, React Router DOM, Oxlint.

---

## 📁 Repository & Folder Structure

```text
BusTracking/
├── backend/                        # Spring Boot Backend Service
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/dtc/bus_tracker/
│   │   │   │   ├── config/          # CORS, Security, WebSocket configurations
│   │   │   │   ├── controller/      # REST API Controllers (Buses, Routes, Stops, Auth)
│   │   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   ├── entity/          # JPA Database Entities
│   │   │   │   ├── exception/       # Global exception handlers
│   │   │   │   ├── mapper/          # MapStruct mappers
│   │   │   │   ├── repository/      # JPA Repositories
│   │   │   │   ├── security/        # JWT Authentication filters & handlers
│   │   │   │   ├── service/         # Business logic & GTFS/CSV processing
│   │   │   │   └── util/            # Helper utilities & Haversine formula math
│   │   │   └── resources/           # application.properties & static assets
│   │   └── test/                    # Backend unit & integration tests
│   ├── data/                        # Static GTFS/CSV data files
│   ├── pom.xml                      # Maven project dependencies configuration
│   ├── mvnw / mvnw.cmd              # Maven wrapper scripts
│   └── test.pb                      # GTFS Realtime test feed binary
│
└── frontend/                       # React + Vite Frontend Web App
    ├── public/                      # Static web assets
    ├── src/
    │   ├── api/                     # Axios HTTP client configuration & API calls
    │   ├── assets/                  # Images, SVGs, and visual icons
    │   ├── components/              # UI Components (BusMap, SearchBar, Modals, etc.)
    │   ├── hooks/                   # Custom React hooks (Geolocation, Polling, STOMP)
    │   ├── pages/                   # Application view pages
    │   ├── utils/                   # Leaflet icon overrides & helper utilities
    │   ├── App.jsx                  # Main application container & state management
    │   ├── App.css / index.css      # Custom styling & Design system
    │   └── main.jsx                 # React DOM root entry point
    ├── .env.example                 # Frontend environment variables template
    ├── .oxlintrc.json               # Oxlint configuration
    ├── BACKEND_NOTES.md             # Developer API specifications & integration notes
    ├── package.json                 # Node dependencies & npm scripts
    └── vite.config.js               # Vite build configuration
```

---

## 🔌 API Endpoints Summary

Base URL: `http://localhost:8080/api`

| Method | Endpoint | Description | Query Parameters / Payload |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/buses/nearby` | Fetch live buses near location | `lat`, `lng`, `radiusMeters` (default: 5000), `limit` |
| `GET` | `/api/routes` | Fetch all bus routes | Pagination / Search params |
| `GET` | `/api/routes/{id}` | Get route details by ID | — |
| `GET` | `/api/routes/search` | Search routes by code/name | `query`, `limit` |
| `GET` | `/api/stops` | Fetch bus stops | `page`, `size` |
| `GET` | `/api/stops/{id}` | Get stop details by ID | — |
| `GET` | `/api/stops/nearby` | Fetch stops near location | `lat`, `lng`, `radiusMeters`, `limit` |

---

## 🚀 Getting Started

### Prerequisites
- **Java Development Kit (JDK 21+)**
- **Maven** (or use included Maven Wrapper `./mvnw`)
- **Node.js** (v18+ recommended) & **npm**

---

### 1. Running the Backend

```bash
cd BusTracking/backend

# Using Maven Wrapper (Windows Command Prompt)
mvnw.cmd spring-boot:run

# Using Maven Wrapper (Bash/Linux/macOS)
./mvnw spring-boot:run
```
> The backend server will start at **`http://localhost:8080`**.
> Swagger API documentation is available at `http://localhost:8080/swagger-ui.html` (or `/v3/api-docs`).

---

### 2. Running the Frontend

```bash
cd BusTracking/frontend

# Install dependencies
npm install

# Start Vite development server
npm run dev
```
> The React application will start at **`http://localhost:5173`** (or `http://localhost:3000`).

---

## ⚡ Features & Workflows

1. **Interactive OpenStreetMap Centered on Delhi**: Renders live bus coordinates, stop markers, user location, and route geometry.
2. **Geolocation & Dynamic Radius Polling**: Automatically retrieves user location to fetch nearby buses and calculate ETA.
3. **Route Search & Autocomplete**: Search routes by code or name and visualize route paths on the map.
4. **Real-Time Updates**: Polls backend APIs every 10 seconds and supports WebSocket connections for updates.

---

## 🐘 Deploying Backend with Neon Database (PostgreSQL)

### 1. Obtain Database Connection String from Neon
1. Log into your [Neon Console](https://console.neon.tech/).
2. Create a new PostgreSQL Project.
3. In the Dashboard, copy your connection details or connection string:
   - **Host**: `ep-xyz-123.us-east-2.aws.neon.tech`
   - **Database**: `neondb`
   - **User**: `neondb_owner`
   - **Password**: `<your-neon-password>`

### 2. Environment Variables Setup
Set the following environment variables on your deployment platform (e.g. Render, Railway, Heroku, or locally):

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-xyz-123.us-east-2.aws.neon.tech/neondb?sslmode=require
SPRING_DATASOURCE_USERNAME=neondb_owner
SPRING_DATASOURCE_PASSWORD=your_neon_password
```

### 3. Connection Pool Tuning
Neon uses serverless PostgreSQL. The application is pre-configured with optimized HikariCP settings:
- `HIKARI_MAX_POOL_SIZE=10` (default)
- `HIKARI_MIN_IDLE=2` (default)

---

## 📄 License
This project is open source and available under the [MIT License](LICENSE).
