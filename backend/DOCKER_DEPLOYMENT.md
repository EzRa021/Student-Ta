# Docker Deployment Guide

## Quick Start with Docker

### Prerequisites
- Docker installed (https://www.docker.com/products/docker-desktop)
- Docker Compose installed (included with Docker Desktop)

### Option 1: Docker Compose (Recommended - includes database)

**Step 1: Create `.env` file**
```bash
cd backend
cp .env.example .env
```

**Step 2: Edit `.env` with your values**
```
DB_NAME=lab_management
DB_USER=labuser
DB_PASS=labpass123
DB_ROOT_PASSWORD=root123
JWT_SECRET=your-jwt-secret-key-here
REFRESH_SECRET=your-refresh-secret-key-here
AES_KEY=0123456789ABCDEF0123456789ABCDEF
CORS_ORIGINS=http://localhost:8080
```

**Step 3: Start both database and app**
```bash
docker-compose up
```

**Step 4: Access the app**
- Backend: http://localhost:8080
- Health check: http://localhost:8080/actuator/health
- Database: localhost:3306 (use your DB_USER and DB_PASS)

**Step 5: Stop services**
```bash
docker-compose down
```

### Option 2: Just the Backend (bring your own database)

**Step 1: Build the image**
```bash
docker build -t lab-ms-app:latest .
```

**Step 2: Run the container**
```bash
docker run -d \
  --name lab-ms-app \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://your-db-host:3306/lab_management?useSSL=false \
  -e SPRING_DATASOURCE_USERNAME=labuser \
  -e SPRING_DATASOURCE_PASSWORD=labpass123 \
  -e JWT_SECRET=your-jwt-secret \
  -e REFRESH_SECRET=your-refresh-secret \
  -e AES_KEY=0123456789ABCDEF0123456789ABCDEF \
  -e CORS_ORIGINS=http://your-frontend-url \
  lab-ms-app:latest
```

**Step 3: View logs**
```bash
docker logs -f lab-ms-app
```

### Option 3: Deploy to Cloud with Docker

**Heroku:**
```bash
heroku container:push web
heroku container:release web
```

**AWS ECS/ECR:**
```bash
# Tag image
docker tag lab-ms-app:latest YOUR_AWS_ACCOUNT.dkr.ecr.REGION.amazonaws.com/lab-ms-app:latest

# Push to ECR
aws ecr get-login-password --region REGION | docker login --username AWS --password-stdin YOUR_AWS_ACCOUNT.dkr.ecr.REGION.amazonaws.com
docker push YOUR_AWS_ACCOUNT.dkr.ecr.REGION.amazonaws.com/lab-ms-app:latest
```

**Docker Hub:**
```bash
docker tag lab-ms-app:latest your-username/lab-ms-app:latest
docker login
docker push your-username/lab-ms-app:latest
```

## Docker Commands

**View running containers:**
```bash
docker ps
```

**View all containers (including stopped):**
```bash
docker ps -a
```

**Stop container:**
```bash
docker stop lab-ms-app
```

**Remove container:**
```bash
docker rm lab-ms-app
```

**View logs:**
```bash
docker logs lab-ms-app
```

**Follow logs in real-time:**
```bash
docker logs -f lab-ms-app
```

**Execute command in running container:**
```bash
docker exec -it lab-ms-app /bin/sh
```

**Remove image:**
```bash
docker rmi lab-ms-app
```

## Troubleshooting

**Container exits immediately:**
- Check logs: `docker logs lab-ms-app`
- Verify environment variables are set
- Ensure database is accessible

**Database connection refused:**
- Check if MySQL container is running: `docker ps`
- Verify database credentials in .env
- For compose: ensure both services are in same network

**Port already in use:**
```bash
# Change port in docker run
docker run -p 9090:8080 ...

# Or stop the service using the port
docker stop container-name
```

**Health check failing:**
- Wait 40+ seconds for app to start
- Check if app is running: `docker logs lab-ms-app`
- Verify health endpoint: `curl http://localhost:8080/actuator/health`

## Environment Variables Reference

| Variable | Example | Required |
|----------|---------|----------|
| SPRING_DATASOURCE_URL | jdbc:mysql://mysql:3306/lab_management | Yes |
| SPRING_DATASOURCE_USERNAME | labuser | Yes |
| SPRING_DATASOURCE_PASSWORD | labpass123 | Yes |
| JWT_SECRET | (32+ char random string) | Yes |
| REFRESH_SECRET | (32+ char random string) | Yes |
| AES_KEY | 0123456789ABCDEF0123456789ABCDEF | Yes |
| CORS_ORIGINS | http://localhost:3000 | No |
| SPRING_JPA_HIBERNATE_DDL_AUTO | validate | No |

## Generate Secrets

```bash
# JWT_SECRET (Linux/Mac)
openssl rand -base64 32

# JWT_SECRET (Windows PowerShell)
[Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes([guid]::NewGuid().ToString() + [guid]::NewGuid().ToString()))

# AES_KEY (32-char hex for AES-256)
openssl rand -hex 16
```

## Performance Tuning

**Increase memory (in docker-compose.yml or docker run):**
```yaml
environment:
  _JAVA_OPTIONS: "-Xmx1024m -Xms512m"
```

**Reduce startup time:**
```dockerfile
ENV _JAVA_OPTIONS="-Xmx512m -Xms256m -XX:+TieredCompilation -XX:TieredStopAtLevel=1"
```

## Security Best Practices

1. **Never commit .env** - It's in .gitignore
2. **Change default passwords** - Edit .env before running
3. **Use secrets manager** - For production use Docker Secrets or Cloud provider's secret management
4. **Enable SSL/TLS** - Configure in Nginx reverse proxy or cloud provider
5. **Restrict network access** - Use firewall rules
6. **Regular backups** - Backup database volume regularly

## Cleanup

**Remove everything:**
```bash
docker-compose down -v  # Removes containers, networks, and volumes
```

**Prune unused resources:**
```bash
docker system prune -a
```

## Next Steps

1. Create `.env` file with your secrets
2. Run `docker-compose up`
3. Verify app is running: `curl http://localhost:8080/actuator/health`
4. Test login endpoint
5. Monitor logs for errors

---

**Docker deployment ready!** ✅
