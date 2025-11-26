# Render.com Deployment Checklist - Fixed Environment Variables

## Problem Fixed ✅
The error `Could not resolve placeholder 'jwt.refresh-secret'` occurred because:
- ❌ `application.yml` was missing the `jwt.refresh-secret` property definition
- ❌ Development wasn't loading `.env` file automatically
- ❌ Production wasn't properly configured to use environment variables

## Solution Implemented ✅

### Changes Made:
1. ✅ Added `jwt.refresh-secret` property to `application.yml`
2. ✅ Created `EnvironmentConfig.java` to auto-load `.env` in development
3. ✅ Added `dotenv-java` dependency to `pom.xml`
4. ✅ Created `application-prod.yml` for production configuration
5. ✅ Registered `EnvironmentPostProcessor` in `META-INF/spring.factories`
6. ✅ Updated `.env` and `.env.example` files

## Before Deployment to Render

### Step 1: Build and Test Locally

```bash
# Clean build
mvn clean package -DskipTests

# Run locally (uses .env file)
java -jar target/lab-ms.jar

# Or run with Maven
mvn spring-boot:run
```

Expected output:
```
Successfully loaded .env file for development environment
Loaded from .env: JWT_SECRET
Loaded from .env: REFRESH_SECRET
```

### Step 2: Prepare for Render Deployment

1. Commit all changes to GitHub:
```bash
git add .
git commit -m "fix: Add environment variable support for dev and prod"
git push origin main
```

2. Do NOT commit `.env` file (already in `.gitignore`)

### Step 3: Set Environment Variables on Render

Go to Render Dashboard → Your Service → Environment and add:

**Required Variables:**
```
SPRING_PROFILE=prod
DB_URL=jdbc:postgresql://[user]:[password]@[host]:[port]/[database]?sslmode=require
DB_USER=your_db_user
DB_PASS=your_secure_database_password

JWT_SECRET=[Generate with: openssl rand -base64 32]
REFRESH_SECRET=[Generate with: openssl rand -base64 32]
JWT_EXPIRY=900000
REFRESH_EXPIRY=604800000

AES_KEY=0123456789ABCDEF0123456789ABCDEF
SERVER_PORT=8080
```

**Generate Secrets:**
```bash
# In your terminal, run:
openssl rand -base64 32

# Run this twice to get two different values for:
# 1. JWT_SECRET
# 2. REFRESH_SECRET
```

Example output (DO NOT USE THESE - GENERATE YOUR OWN):
```
rB9qX8vK2mN5pL7jZ3kW6dQ1sT8vY4aM9bP2hR5fG7jK9lM1nO3rU6wV8xY0zC2e
aX3yL5mZ7kW9qP2sT4vN6bM8fR1jH3lK5nO7rQ9tU2wV4xY6zB8cE1dG3fJ5hM7
```

### Step 4: Deploy

1. **Option A: Automatic Deploy**
   - Push to main branch - Render auto-deploys

2. **Option B: Manual Redeploy**
   - Go to Render Dashboard
   - Select your service
   - Click "Manual Deploy" → "Clear Build Cache & Deploy"

### Step 5: Verify Deployment

Check logs on Render:
```
Render Dashboard → Your Service → Logs
```

Look for success indicators:
```
Running in production mode - using system environment variables
Loaded by Spring in: 4.567s
Started LabManagementSystemApplication
Server started on port 8080
```

Or test the API:
```bash
curl https://your-app.render.com/actuator/health
```

Expected response:
```json
{"status":"UP","components":{"db":{"status":"UP"}}}
```

## Important Notes

### Never Do This:
- ❌ Commit `.env` file with real secrets
- ❌ Use development secrets in production
- ❌ Leave JWT_SECRET or REFRESH_SECRET unset
- ❌ Use the same secret for JWT_SECRET and REFRESH_SECRET

### Always Do This:
- ✅ Generate new secrets for production with `openssl rand -base64 32`
- ✅ Set `SPRING_PROFILE=prod` on Render
- ✅ Verify all 9 environment variables are set
- ✅ Test health endpoint after deployment
- ✅ Monitor logs for errors

## If Deployment Fails

### Error: "Could not resolve placeholder 'jwt.refresh-secret'"
**Fix**: Make sure `REFRESH_SECRET` is set as an environment variable on Render

### Error: "Connection refused" to database
**Fix**: Check `DB_URL` format includes correct host and port

### Error: "Port is already in use"
**Fix**: Render automatically sets `PORT` - don't override it, keep `SERVER_PORT=8080`

## Testing Authentication

After successful deployment:

```bash
# Register
curl -X POST https://your-app.render.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123","email":"test@test.com"}'

# Login (should work with proper JWT_SECRET)
curl -X POST https://your-app.render.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123"}'

# Check health
curl https://your-app.render.com/actuator/health
```

## Documentation

For more detailed information, see:
- `ENVIRONMENT_SETUP.md` - Complete environment configuration guide
- `.env.example` - Template for environment variables
- `application.yml` - Default configuration with variable references
- `application-prod.yml` - Production-specific configuration

---

**Status**: ✅ Ready for Deployment
**Tested**: ✅ Locally verified
**Last Updated**: November 26, 2025
