# Lab Management System - Backend

Real-time Lab Management System with Spring Boot 3.4.0, MySQL, WebSocket, and JWT authentication.

## Features

- ✅ **Real-time Communication** - WebSocket support for live updates
- ✅ **JWT Authentication** - Secure token-based authentication with refresh tokens
- ✅ **Request Management** - Create, assign, and resolve lab requests
- ✅ **TA Assignment** - Assign requests to Teaching Assistants
- ✅ **Real-time Replies** - TA replies broadcast to students instantly
- ✅ **Role-based Access** - Student and TA roles with appropriate permissions
- ✅ **Database Migrations** - Flyway for schema versioning
- ✅ **Health Monitoring** - Actuator endpoints for service health
- ✅ **Metrics Export** - Prometheus-compatible metrics
- ✅ **Production Ready** - Deployable to Render, AWS, Azure, and other platforms

## Tech Stack

- **Framework:** Spring Boot 3.4.0
- **Language:** Java 21
- **Database:** MySQL 8.0+
- **Security:** Spring Security 6.2.0 + JWT HS256
- **Real-time:** WebSocket + STOMP Protocol
- **Build:** Maven 3.9+
- **ORM:** Hibernate + JPA

## Quick Start

### Prerequisites
- Java 21 or higher
- Maven 3.9+
- MySQL 8.0+ (or Docker)

### Installation

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd backend
   ```

2. **Create MySQL database:**
   ```sql
   CREATE DATABASE lms_db;
   CREATE USER 'lms_user'@'localhost' IDENTIFIED BY 'lms_password';
   GRANT ALL PRIVILEGES ON lms_db.* TO 'lms_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **Build the application:**
   ```bash
   mvn clean package -DskipTests
   ```

4. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

   Application will start at `http://localhost:8080`

## Configuration

### Environment Variables

```bash
# Server
SPRING_PROFILE=local          # local, dev, or prod
SERVER_PORT=8080

# Database
DB_URL=jdbc:mysql://localhost:3306/lms_db?useSSL=false&serverTimezone=UTC
DB_USER=lms_user
DB_PASS=lms_password

# JWT (REQUIRED for production)
JWT_SECRET=your-secret-key-min-32-chars
JWT_EXPIRY=900000             # 15 minutes
REFRESH_SECRET=your-refresh-key-min-32-chars
REFRESH_EXPIRY=604800000      # 7 days

# Optional
AES_KEY=0123456789ABCDEF0123456789ABCDEF
JAVA_OPTS=-Xmx512m -Xms256m
```

## Deployment

### Deploy to Render (Recommended)

See [DEPLOYMENT.md](./DEPLOYMENT.md) for complete production deployment guide.

**Quick Summary:**
1. Push backend to GitHub
2. Connect Render to GitHub
3. Create Web Service
4. Set environment variables
5. Deploy

### Deploy to Other Platforms
- **AWS:** EC2 + RDS
- **Azure:** App Service + Database for MySQL
- **DigitalOcean:** Droplet + Managed Database
- **Heroku:** Using Procfile

## API Documentation

### Base URLs
- **Development:** `http://localhost:8080/api`
- **Production:** `https://your-app.render.com/api`

### Key Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/login` | User login |
| POST | `/auth/register` | Student registration |
| POST | `/auth/register-ta` | TA registration |
| GET | `/auth/me` | Get current user |
| POST | `/requests` | Create request (student) |
| GET | `/requests` | Get all requests (TA) |
| PUT | `/requests/{id}` | Update request |
| PUT | `/requests/{id}/assign` | Assign to TA |
| GET | `/replies/{requestId}` | Get request replies |
| POST | `/replies` | Add reply |
| GET | `/actuator/health` | Health check |

## WebSocket

### Connection
- **URL:** `ws://localhost:8080/ws` (dev)
- **URL:** `wss://your-app.render.com/ws` (prod)

### Topics
- `/topic/requests` - Broadcast to all TAs
- `/user/queue/requests` - Private messages to students

## Project Structure

```
backend/
├── pom.xml                          # Maven config
├── Procfile                         # Render deployment
├── render.yaml                      # Render config
├── .env.example                     # Environment template
├── DEPLOYMENT.md                    # Deployment guide
├── README.md                        # This file
├── src/
│   ├── main/
│   │   ├── java/com/lms/
│   │   │   ├── config/              # Spring configuration
│   │   │   ├── controller/          # REST endpoints
│   │   │   ├── dto/                 # Data transfer objects
│   │   │   ├── entity/              # JPA entities
│   │   │   ├── exception/           # Error handling
│   │   │   ├── repository/          # Database access
│   │   │   ├── security/            # JWT & security
│   │   │   ├── service/             # Business logic
│   │   │   └── LabManagementSystemApplication.java
│   │   └── resources/
│   │       ├── application.yml      # Configuration profiles
│   │       └── db/migration/        # SQL migrations (Flyway)
│   └── test/
│       └── java/com/lms/            # Unit tests
└── target/                          # Build output
```

## Database Schema

**Users Table**
- `id` - UUID primary key
- `username` - Unique username
- `email` - Unique email
- `password` - Hashed (bcrypt)
- `role` - STUDENT or TA
- `student_id` - For students only
- `created_at`, `updated_at` - Timestamps

**Requests Table**
- `id` - UUID primary key
- `title`, `description` - Request details
- `student_id` - Foreign key to user
- `assigned_to_id` - Foreign key to TA (nullable)
- `status` - PENDING, IN_PROGRESS, RESOLVED, CANCELLED
- `created_at`, `updated_at` - Timestamps

**Replies Table**
- `id` - UUID primary key
- `request_id` - Foreign key to request
- `ta_id` - Foreign key to TA user
- `message` - Reply content
- `created_at` - Timestamp

## Security

### JWT Configuration
- **Algorithm:** HS256 (HMAC SHA-256)
- **Access Token TTL:** 15 minutes
- **Refresh Token TTL:** 7 days
- **Secret:** Minimum 32 characters (required at production)

### Features
✅ Password hashing with bcrypt
✅ SQL injection prevention (prepared statements)
✅ CORS enabled with origin whitelist
✅ Rate limiting on auth endpoints
✅ CSRF protection enabled
✅ JWT signature validation
✅ Token blacklist support

## Building for Production

### Standard Build
```bash
mvn clean package -DskipTests
java -jar target/lab-ms.jar
```

### Production Build
```bash
mvn clean package -DskipTests -P prod
```

### Docker Build
```dockerfile
FROM eclipse-temurin:21-jdk-alpine
COPY backend/target/lab-ms.jar app.jar
ENTRYPOINT ["java", "-Dserver.port=8080", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]
```

## Monitoring & Health

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Metrics (Prometheus)
```bash
curl http://localhost:8080/actuator/metrics
```

### Available Metrics
- JVM memory, garbage collection, threads
- HTTP requests, response times, status codes
- Database connection pool statistics
- Custom application metrics

### Logs
```bash
# Development (verbose)
tail -f target/spring.log

# Production (Render)
# View in Render Dashboard → Service Logs
```

## Performance Optimization

### Connection Pooling (HikariCP)
```yaml
maximum-pool-size: 10
minimum-idle: 5
connection-timeout: 30s
```

### Compression
- HTTP compression enabled by default
- Reduces payload size 60-70%

### Caching Ready
- Spring Cache framework integrated
- Can add Redis for distributed caching

## Troubleshooting

### Application won't start
```
Check:
1. Database is running: mysql -u lms_user -p
2. Environment variables set: echo $JWT_SECRET
3. Port not in use: lsof -i :8080
4. Logs for specific errors
```

### Database connection fails
```
1. Verify MySQL is running
2. Check DB credentials in .env
3. Verify database exists: mysql -u lms_user -p -e "USE lms_db;"
4. Check firewall settings
```

### WebSocket connection fails
- Use WSS (secure) for HTTPS connections
- Check CORS configuration in SecurityConfig
- Verify WebSocket endpoint: GET http://localhost:8080/ws

### High memory usage
```
Increase JVM heap:
JAVA_OPTS=-Xmx1024m -Xms512m
```

## Development Tips

### Enable debug mode
```yaml
logging:
  level:
    com.lms: DEBUG
    org.springframework.security: DEBUG
```

### Run tests
```bash
mvn test
```

### Run specific test
```bash
mvn test -Dtest=RequestServiceTest
```

### Create database migration
```bash
# File: src/main/resources/db/migration/VN__description.sql
# Run automatically on startup
```

## Version History

### 1.0.0 (Current - Production Ready)
- Core functionality: requests, replies, real-time updates
- JWT authentication with refresh tokens
- WebSocket support with STOMP
- Role-based access control (Student/TA)
- Database migrations with Flyway
- Health monitoring and metrics
- Production deployment ready

---

## Contributing

1. Create feature branch: `git checkout -b feature/your-feature`
2. Make changes and test locally
3. Commit: `git commit -am 'Add feature'`
4. Push: `git push origin feature/your-feature`
5. Create Pull Request

## Support

- **Issues:** Create GitHub issue
- **Documentation:** Check DEPLOYMENT.md
- **Logs:** Review application logs
- **Contact:** [Support email/contact]

---

**Status:** ✅ Production Ready
**Last Updated:** November 25, 2025
**Java:** 21
**Spring Boot:** 3.4.0
