# Microfinance Loan System

A comprehensive, robust Spring Boot backend designed for Microfinance Institutions (MFIs). This system manages the complete lifecycle of microfinance operations, including user onboarding, client KYC/CIBIL verifications, loan origination, dynamic Role-Based Access Control (RBAC), EMI collections, and advanced portfolio analytics.

## 🚀 Key Features

*   **Dynamic Role-Based Access Control (RBAC)**: Secure endpoints based on temporal rules (business hours for staff) and a dynamic permission matrix for roles (e.g., `ADMIN`, `BRANCH_MANAGER`, `CREDIT_OFFICER`, `LOAN_OFFICER`, `COLLECTIONS_AGENT`, `CLIENT`).
*   **Client Management & KYC**: Register clients, verify Aadhaar/PAN based KYC, and generate/enquire CIBIL credit scores.
*   **Loan Lifecycle Management**: Support for loan application, multi-level credit committee approvals, disbursements, rejections, and restructuring.
*   **Group Lending**: Specialized module for Joint Liability Groups (JLG), managing group creations, Group Recognition Tests (GRT), group savings, and meetings.
*   **EMI & Collections Engine**: Generate EMI schedules, process field agent collections with offline-sync support, group payments, and cash reconciliations.
*   **Portfolio Analytics & Admin Dashboards**: Track Portfolio At Risk (PAR), collection efficiency, yield costs, and view delinquency heatmaps.
*   **Mobile Operations Support**: Endpoints tailored for field agents including geolocation check-ins, route optimization, field notes, and digital agreement signing.
*   **Regulatory Compliance & Security**: Built-in audit logs, intrusion checks, data sanitization, RBI/MFIN reporting, and compliance calendars.

## 🛠️ Technology Stack

*   **Language**: Java 17+
*   **Framework**: Spring Boot 3.x
*   **Security**: Spring Security with JWT Authentication
*   **Data Access**: Spring Data JPA / Hibernate
*   **Build Tool**: Maven

## ⚙️ Setup and Installation

### Prerequisites
*   Java Development Kit (JDK) 17 or higher
*   Maven installed locally (or use the provided `mvnw` wrapper)

### Running the Application

1.  **Clone / Download the project** to your local machine.
2.  **Navigate to the project root directory**:
    ```bash
    cd microfinance-loan-system
    ```
3.  **Run the application** using Maven:
    ```bash
    mvn spring-boot:run
    ```
    *Alternatively, using the Maven Wrapper:*
    ```bash
    ./mvnw spring-boot:run
    ```

The application will start up on `http://localhost:8080`.

## 🔐 Default Credentials

The application includes a `DataInitializer` that automatically seeds a default system administrator and some mock employee directory entries on the first run.

You can use the following default credentials to log in:

*   **Credential (Username/Email)**: `admin` (or `admin@microfinance.com`)
*   **Password**: `Admin@1234`

**To log in as a Staff Member:**
1.  Check the `MicrofinanceDirectory` for pre-seeded Employee IDs (e.g., `EMP001` for Branch Manager, `EMP002` for Credit Officer).
2.  Use the `POST /api/auth/register` endpoint to create the account.
3.  Log in using `POST /api/auth/login`.

## 📖 API Documentation

Detailed endpoint documentation, expected JSON payloads, and testing guides are available in the project files:
*   `endpoints.txt`: A comprehensive list of all controllers and endpoints.
*   `postman_collection.json`: A ready-to-import Postman collection to test the APIs.

---
*Developed for modern Microfinance Application Development.*
