# SplitSmart ✈️💰

SplitSmart is a premium, feature-rich web application designed for smart group expense management and splitting during trips. It features a modern, dark-themed competitive leaderboard dashboard (inspired by coding contest standings) that ranks trip members based on their balances and contributions.

---

## 🚀 Key Features

* **Trip Leaderboard**: An impressive dark-themed standings page ranking members based on their net balance. Highlights the top 3 with custom gold, silver, and bronze glowing podiums.
* **Shared Kitty Pool**: Dedicated kitty pool system with status indicators and real-time funding percentage tracking.
* **Expense Log**: Complete expense ledger categorizing food, transport, accommodation, activity, shopping, and more.
* **Anonymous Budget Voting**: Collaborative budget planning with anonymous range voting and automatic quorum calculations.
* **Automated Settlements**: Intelligent debt-simplification algorithm showing member balances and the exact minimum transfers required to settle the trip.
* **Real-time Synchronization**: WebSockets integration providing live notifications when someone adds expenses, contributes to the kitty, or updates trip status.

---

## 🛠️ Tech Stack

### Frontend
* **Framework**: React 19 (Vite-based)
* **Styling**: Tailwind CSS & custom glassmorphic design system
* **State Management**: Zustand
* **Icons**: Lucide React
* **Real-time**: StompJS / SockJS (WebSockets)

### Backend
* **Framework**: Spring Boot 3.2 (Java 17)
* **Database**: MySQL
* **ORM**: Spring Data JPA / Hibernate
* **Security**: JWT (JSON Web Tokens) & Spring Security
* **Real-time**: Spring WebSocket Messaging

---

## 💻 Local Setup & Installation

### Prerequisites
Make sure you have the following installed on your local system:
1. **Java JDK 17** or higher
2. **Node.js** (v18 or higher) and npm
3. **MySQL Server** running locally

---

### Step 1: Clone the Repository
```bash
git clone <your-repository-url>
cd smart-expense
```

### Step 2: Environment Configuration
Copy the `.env.example` file in the root directory to `.env` and update the values with your local database credentials:
```bash
cp .env.example .env
```
Open the `.env` file and set your MySQL password:
```env
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_mysql_password
```

---

### Step 3: Setup & Start the Backend

The backend is located in the `smart-expense` directory.

1. **Start your local MySQL server** and ensure it's running.
2. **Create the database** (optional, as Spring Boot is configured to auto-create it if it doesn't exist):
   ```sql
   CREATE DATABASE smart_expense_db;
   ```
3. **Run the Spring Boot application**:
   
   * **On Windows (PowerShell)**:
     Set your env variables for the session and run:
     ```powershell
     $env:SPRING_DATASOURCE_PASSWORD="your_mysql_password"
     ./mvnw.cmd spring-boot:run
     ```
   * **On macOS / Linux (Bash/Zsh)**:
     ```bash
     SPRING_DATASOURCE_PASSWORD="your_mysql_password" ./mvnw spring-boot:run
     ```
     *(If you get a permission error on the wrapper, run `chmod +x mvnw` first)*

The backend will start on `http://localhost:8080`.

---

### Step 4: Setup & Start the Frontend

The frontend is located in the `frontend` directory.

1. **Navigate to the frontend directory**:
   ```bash
   cd frontend
   ```
2. **Install the dependencies**:
   ```bash
   npm install
   ```
3. **Start the Vite development server**:
   ```bash
   npm run dev
   ```

The frontend will start on `http://localhost:5173` (or the port shown in your terminal). Open this URL in your browser to access SplitSmart!

---

## 📂 Project Structure

```text
smart-expense/
├── frontend/                 # React frontend application
│   ├── src/
│   │   ├── components/       # Reusable UI components (kitty, trip, vote, etc.)
│   │   ├── pages/            # Page layouts (Dashboard, TripDetail, Settlement)
│   │   ├── store/            # Zustand global stores (auth, trip, expense)
│   │   └── index.css         # Reusable glassmorphic design system classes
│   └── package.json
│
├── smart-expense/            # Spring Boot backend application
│   ├── src/main/java/        # Java source files (controllers, services, repositories)
│   ├── src/main/resources/   # Application properties & configuration
│   └── pom.xml               # Maven dependencies
│
├── .env.example              # Environment variables template
├── .gitignore                # Git ignore configuration
└── README.md                 # Project documentation
```

---

## 🔐 Reusable Design System Classnames
When adding new components to the frontend, you can use the built-in design system classes defined in `index.css`:
* **Cards**: Use `glass-card` for a premium translucent dark card.
* **Typography**: Use `text-heading` (headers), `text-subheading` (labels), and `text-body` (descriptions).
* **Buttons**: Use `btn-primary`, `btn-secondary`, or `btn-danger`.
