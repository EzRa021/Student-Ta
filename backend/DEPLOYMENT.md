# Lab Management System - Deployment Guide

## Quick Start (Render.com)

### Prerequisites
- Render.com account
- MySQL database (managed by Render or external)
- Java 21 runtime

### Step 1: Prepare Backend for Deployment

1. **Build the application:**
   ```bash
   mvn clean package -DskipTests
   ```

2. **Verify the JAR file:**
   ```bash
   ls -lh target/lab-ms.jar
   ```

### Step 2: Set Up Database on Render

1. **Create MySQL Database Service on Render:**
   - Go to Render Dashboard
   - Create new "MySQL" database
   - Note the connection details:
     - Host: `your-db-host.render.com`
     - Database: `lms_db`
     - User: `your_db_user`
     - Password: Generate strong password

2. **Run Flyway Migrations:**
   - Migrations run automatically on startup
   - Check `src/main/resources/db/migration/` for all scripts

### Step 3: Deploy Backend to Render

1. **Connect your GitHub repository to Render**
   - Push this backend to GitHub
   - Connect Render to your GitHub account

2. **Create Web Service on Render:**
   - Select "New Web Service"
   - Connect to your repository
   - Choose Node (we'll use custom Procfile)
   - Set environment variables (see below)
   - Deploy

3. **Set Environment Variables on Render:**
   ```
   SPRING_PROFILE=prod
   SERVER_PORT=5432
   
   DB_URL=jdbc:postgresql://db_user:f22tx3LrUSodquh1yAupicDeXHRKrFdC@dpg-d4j2bt0dl3ps73e5kdb0-a/ta_2z19
   DB_USER=db_user
   DB_PASS=f22tx3LrUSodquh1yAupicDeXHRKrFdC
   
   JWT_SECRET=gfyguugug7857456r56rtv56e5cyw4w3wc3wy45ww3
   JWT_EXPIRY=900000
   REFRESH_SECRET=6t7r56476tyr56r767i6kuhuky78yo8iou89y787y78
   REFRESH_EXPIRY=604800000
   
   AES_KEY=0123456789ABCDEF0123456789ABCDEF
   JAVA_OPTS=-Xmx512m -Xms256m
   ```
### Step 4: Security Checklist

✅ **Secrets Management:**
- [ ] Generate strong JWT secrets using `openssl rand -base64 32`
- [ ] Never commit `.env` files
- [ ] Use Render environment variables for all secrets
- [ ] Rotate secrets every 6 months

✅ **Database Security:**
- [ ] Use strong database password (min 20 characters)
- [ ] Enable SSL for database connections (useSSL=true)
- [ ] Restrict database access to backend service only
- [ ] Enable backups on Render

✅ **API Security:**
- [ ] Enable CORS with allowed frontend origin
- [ ] Use HTTPS for all connections
- [ ] Rate limiting enabled
- [ ] CSRF protection enabled

### Step 5: Monitoring & Logs

1. **View logs on Render:**
   ```
   Render Dashboard → Your Service → Logs
   ```

2. **Monitor health endpoint:**
   ```
   curl https://your-app.render.com/actuator/health
   ```

3. **Check metrics:**
   ```
   https://your-app.render.com/actuator/metrics
   ```

## Environment Variables Reference

| Variable | Required | Default | Notes |
|----------|----------|---------|-------|
| SPRING_PROFILE | Yes | local | Use `prod` for production |
| SERVER_PORT | No | 8080 | Render sets this automatically |
| DB_URL | Yes | - | Must include SSL parameters |
| DB_USER | Yes | - | Database username |
| DB_PASS | Yes | - | Database password (strong) |
| JWT_SECRET | Yes | - | Min 32 chars, use `openssl rand -base64 32` |
| JWT_EXPIRY | No | 900000 | 15 minutes in milliseconds |
| REFRESH_SECRET | Yes | - | Min 32 chars, use `openssl rand -base64 32` |
| REFRESH_EXPIRY | No | 604800000 | 7 days in milliseconds |
| AES_KEY | No | default | 32-char AES-256 key |
| JAVA_OPTS | No | - | JVM options for memory management |

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - Student registration
- `POST /api/auth/register-ta` - TA registration
- `POST /api/auth/refresh` - Refresh JWT token
- `POST /api/auth/me` - Get current user info

### Requests
- `GET /api/requests` - Get all requests (TA only)
- `GET /api/requests/my-requests` - Get student's requests
- `POST /api/requests` - Create new request
- `PUT /api/requests/{id}` - Update request status
- `PUT /api/requests/{id}/assign` - Assign to TA

### Replies
- `GET /api/replies/{requestId}` - Get request replies
- `POST /api/replies` - Add reply to request

### Health & Monitoring
- `GET /actuator/health` - Service health
- `GET /actuator/metrics` - Prometheus metrics

## WebSocket Endpoints

- **WS URL:** `wss://your-app.render.com/ws`
- **Topics:** 
  - `/topic/requests` - Broadcast to all TAs
  - `/user/queue/requests` - Private message to student
- **Subscriptions handled by:** `WebSocketConfig`

## Troubleshooting

### Application won't start
```
Check logs for:
1. Database connection errors
2. Missing environment variables
3. JWT secrets not set
4. Port conflicts (Render sets $PORT)
```

### Database migration fails
```
1. Check Flyway migration files are present
2. Verify DB_URL format includes SSL parameters
3. Ensure database is created and accessible
4. Check migration file naming: V1__*.sql
```

### WebSocket connection fails
- Ensure HTTPS/WSS (not HTTP/WS)
- Check CORS configuration in SecurityConfig
- Verify WebSocket endpoint is exposed (/ws)

### High memory usage
```
Increase JAVA_OPTS:
JAVA_OPTS=-Xmx1024m -Xms512m
```

## Performance Optimization

1. **Database Connection Pooling:**
   - Configured with HikariCP (max 10 connections)
   - Adjust if needed in `application.yml`

2. **Caching:**
   - Consider adding Redis for session cache
   - Spring Cache annotations ready to use

3. **Compression:**
   - HTTP compression enabled in `application.yml`
   - Reduces payload size by 60-70%

## Maintenance

### Regular Tasks
- Monitor logs for errors
- Check database growth
- Review failed login attempts
- Update dependencies quarterly

### Backup Strategy
- Enable Render automated backups
- Daily backups recommended for production
- Test restore procedure quarterly

## Support & Documentation

- Spring Boot: https://spring.io/projects/spring-boot
- Render Docs: https://render.com/docs
- MySQL: https://dev.mysql.com/doc/
- JWT: https://jwt.io

---

**Last Updated:** November 25, 2025
**Backend Version:** 1.0.0
**Java:** 21
**Spring Boot:** 3.4.0
