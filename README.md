# Golden Palm Resort - Hotel Reservation System

A comprehensive web-based hotel reservation system for special events, built for SLIIT project group 2025-Y2-S1-MLB-B1G2-02.

## 🏗️ System Overview

The Golden Palm Resort Hotel Reservation System is a full-stack Java Spring Boot application that provides:

- **Multi-role User Management** (Guests, Admin, Staff)
- **Room & Event Space Booking** with real-time availability
- **Secure Payment Processing** with multiple payment methods
- **Automated Notifications** via email and SMS
- **Comprehensive Reporting** and analytics
- **Mobile-responsive Design**

## 🛠️ Technology Stack

- **Backend:** Java 17, Spring Boot 3.1.4, Spring Security, JPA/Hibernate
- **Frontend:** HTML5, CSS3, JavaScript, Bootstrap 5
- **Database:** MySQL 8.x
- **Build Tool:** Apache Maven
- **Security:** JWT Authentication

## 📋 Prerequisites

Before running this application, make sure you have:

- Java 17 or higher
- MySQL 8.x
- Maven 3.6+
- Git

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd GoldenPalm-Resort
```

### 2. Database Setup

1. Start MySQL server
2. Create a database (or let the application create it automatically)
3. Update database credentials in `src/main/resources/application.properties` if needed

### 3. Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 4. Access the Application

- **Frontend:** http://localhost:8080
- **API Documentation:** http://localhost:8080/swagger-ui.html (if Swagger is configured)

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/sliit/goldenpalmresort/
│   │   ├── config/           # Configuration classes
│   │   ├── controller/       # REST controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── exception/       # Custom exceptions
│   │   ├── model/           # Entity classes
│   │   ├── repository/      # Data access layer
│   │   ├── service/         # Business logic
│   │   └── util/            # Utility classes
│   └── resources/
│       ├── static/          # Static files (HTML, CSS, JS)
│       └── application.properties
└── test/                    # Test files
```

## 🔐 User Roles

| Role | Description | Access Level |
|------|-------------|--------------|
| **GUEST** | Regular users | Book rooms, view bookings |
| **ADMIN** | System administrator | Full system access |
| **MANAGER** | Hotel manager | Reports and analytics |
| **FRONT_DESK** | Front desk staff | Check-in/check-out |
| **PAYMENT_OFFICER** | Payment staff | Payment processing |

## 🗄️ Database Schema

The system uses the following main entities:

- **Users** - User accounts and authentication
- **Rooms** - Individual hotel rooms
- **RoomTypes** - Room categories and pricing
- **EventSpaces** - Event venues and spaces
- **Bookings** - Reservation records
- **Payments** - Payment transactions

## 🔌 API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout

### Bookings
- `GET /api/bookings` - Get user bookings
- `POST /api/bookings` - Create new booking
- `GET /api/bookings/{id}` - Get booking details
- `DELETE /api/bookings/{id}` - Cancel booking
- `GET /api/bookings/available-rooms` - Search available rooms

### Admin (Protected)
- `GET /api/admin/dashboard` - Admin dashboard
- `GET /api/admin/reports` - Generate reports
- `GET /api/admin/users` - Manage users

## 🎨 Frontend Features

- **Responsive Design** - Works on desktop, tablet, and mobile
- **Modern UI** - Clean and professional interface
- **Interactive Booking Form** - Real-time availability search
- **Room Gallery** - Visual room presentations
- **User Dashboard** - Booking management interface

## 🔧 Configuration

### Database Configuration
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/goldenpalm_resort
spring.datasource.username=root
spring.datasource.password=root
```

### JWT Configuration
```properties
jwt.secret=your-secret-key
jwt.expiration=86400000
```

### Email Configuration (Optional)
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${EMAIL_USERNAME}
spring.mail.password=${EMAIL_PASSWORD}
```

## 🧪 Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Generate test coverage report
mvn jacoco:report
```

## 📊 Monitoring and Logging

The application includes:
- **Health Checks** - System status monitoring
- **Request Logging** - API call tracking
- **Error Handling** - Comprehensive exception management
- **Performance Metrics** - Response time monitoring

## 🔒 Security Features

- **JWT Authentication** - Stateless authentication
- **Role-based Authorization** - Access control by user roles
- **Password Encryption** - BCrypt password hashing
- **Input Validation** - Request data validation
- **CSRF Protection** - Cross-site request forgery protection

## 🚀 Deployment

### Local Development
```bash
mvn spring-boot:run
```

### Production Deployment
```bash
# Build JAR file
mvn clean package

# Run JAR file
java -jar target/goldenpalm-resort-0.0.1-SNAPSHOT.jar
```

### Docker Deployment
```bash
# Build Docker image
docker build -t goldenpalm-resort .

# Run container
docker run -p 8080:8080 goldenpalm-resort
```

## 📈 Future Enhancements

- [ ] Email/SMS notification integration
- [ ] Payment gateway integration (Stripe, PayPal)
- [ ] Advanced reporting and analytics
- [ ] Mobile app development
- [ ] Multi-language support
- [ ] Real-time chat support
- [ ] Advanced search and filtering
- [ ] Loyalty program integration

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📞 Support

**Project Team:** SLIIT 2025-Y2-S1-MLB-B1G2-02

For technical support or questions:
- Email: support@goldenpalmresort.com
- Phone: +94 11 234 5678

## 📄 License

This project is developed for educational purposes as part of the SLIIT curriculum.

---

**Golden Palm Resort Hotel Reservation System** - Making luxury accessible, one booking at a time. 