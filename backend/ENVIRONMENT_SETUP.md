# Environment Configuration Guide

This guide explains how to configure environment variables for development and production deployments.

## Quick Summary

- **Development (Local)**: Uses `.env` file automatically loaded by `EnvironmentConfig`
- **Production (Render)**: Uses environment variables set on Render platform
- **No Manual Setup Needed**: The application automatically detects the profile and loads configuration accordingly

## Development Setup

### Prerequisites
- Java 21
- Maven
- MySQL running locally on port 3306

### Step 1: Create `.env` File

Copy the example and fill in your values:
```bash
cp .env.example .env
```

Edit `.env` with your local database credentials:
```dotenv
SPRING_PROFILE=local
DB_URL=jdbc:mysql://localhost:3306/lms_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USER=lms_user
DB_PASS=your_local_password

JWT_SECRET=0987654321qwertyuioplkjhgfdsazxcvbnm1234567890abcdef
REFRESH_SECRET=0987654321qwertyuioppoiuytrewqlkjhgfdsazxcvbnm1234567890
```

### Step 2: Run Application

The `.env` file will be automatically loaded:
```bash
mvn spring-boot:run
```

Or run the built JAR:
```bash
java -jar target/lab-ms.jar
```

## Production Deployment (Render.com)

### Prerequisites
- Render account
- PostgreSQL or MySQL database on Render
- GitHub repository

### Step 1: Set Environment Variables on Render

1. Go to your Render service dashboard
2. Navigate to **Environment** tab
3. Add the following variables:

```
SPRING_PROFILE=prod
DB_URL=jdbc:postgresql://user:password@host:5432/db_name?sslmode=require
DB_USER=your_db_user
DB_PASS=your_secure_password

JWT_SECRET=generate_strong_secret_with_openssl_rand_-base64_32
JWT_EXPIRY=900000

REFRESH_SECRET=generate_different_strong_secret_with_openssl_rand_-base64_32
REFRESH_EXPIRY=604800000

AES_KEY=0123456789ABCDEF0123456789ABCDEF
SERVER_PORT=8080
```

### Step 2: Generate Strong Secrets

Use OpenSSL to generate cryptographically secure secrets:

```bash
# Generate JWT_SECRET
openssl rand -base64 32

# Generate REFRESH_SECRET (different from JWT_SECRET)
openssl rand -base64 32
```

Output example:
```
rB9qX8vK2mN5pL7jZ3kW6dQ1sT8vY4aM9bP2hR5fG7jK9lM1nO3rU6wV8xY0zC2e
```

Copy each generated value to the corresponding Render environment variable.

### Step 3: Deploy

Push your code to GitHub and Render will automatically deploy:
```bash
git push origin main
```

## How It Works

### EnvironmentConfig Class

The `EnvironmentConfig` class is an `EnvironmentPostProcessor` that:

1. **Detects the Active Profile**: Checks `spring.profiles.active`
2. **For Development (local/dev)**:
   - Loads the `.env` file using `dotenv-java`
   - Adds all variables to Spring's environment
   - Falls back to defaults if `.env` is missing
3. **For Production (prod)**:
   - Uses system environment variables from Render
   - Validates that required variables are set
   - Never attempts to load `.env` file

### Configuration Files

- `application.yml`: Main configuration with defaults
- `application-prod.yml`: Production-specific overrides
- `.env`: Development environment variables (NOT committed to git)
- `.env.example`: Template for `.env` (committed to git)

## Configuration Priority

Spring resolves configuration in this order (highest to lowest priority):

1. Environment variables (set on Render)
2. `.env` file variables (development only)
3. System properties (`-D` flags)
4. `application-{profile}.yml` defaults
5. `application.yml` defaults

## Important Security Notes

### Never in Production
- ❌ Do NOT commit `.env` file to git
- ❌ Do NOT hardcode secrets in configuration files
- ❌ Do NOT use development secrets in production

### Local Development
- ✅ Use `.env` for development-only credentials
- ✅ Safe to commit `.env.example` (no actual secrets)
- ✅ Share `.env` only through secure channels

### Production (Render)
- ✅ Set secrets as environment variables on Render dashboard
- ✅ Generate new secrets using `openssl rand -base64 32`
- ✅ Rotate secrets every 6 months
- ✅ Use different JWT_SECRET and REFRESH_SECRET

## Troubleshooting

### Issue: "Could not resolve placeholder 'jwt.refresh-secret'"

**Cause**: Missing environment variable or `.env` file

**Solution for Development**:
- Ensure `.env` file exists in project root
- Check that `REFRESH_SECRET` is set in `.env`
- Verify `SPRING_PROFILE=local` (not `prod`)

**Solution for Production**:
- Add `REFRESH_SECRET` to Render environment variables
- Verify `SPRING_PROFILE=prod` on Render
- Check variable names (case-sensitive: `REFRESH_SECRET`, not `refresh_secret`)

### Issue: Application works locally but fails on Render

**Cause**: Missing or incorrect environment variables on Render

**Solution**:
1. Open Render dashboard → Your Service → Environment
2. Verify all required variables are set:
   - `JWT_SECRET` ✓
   - `REFRESH_SECRET` ✓
   - `DB_URL` ✓
   - `DB_USER` ✓
   - `DB_PASS` ✓
3. Check for typos in variable names
4. Redeploy after updating variables

### Issue: ".env file not found" warning in production logs

**This is expected and OK** - The application should use environment variables in production, not `.env` file.

## .gitignore Configuration

Ensure your `.gitignore` includes:
```
# Environment files (secrets)
.env
.env.local
.env.*.local

# IDE
.idea/
.vscode/
*.swp
*.swo

# Build
target/
/out/
```

## Profiles Overview

| Profile | Use Case | Env File | Logging | Cache SQL |
|---------|----------|----------|---------|-----------|
| `local` | Development | ✅ Yes | DEBUG | Yes |
| `dev` | Integration/CI | ✅ Yes | DEBUG | No |
| `prod` | Production (Render) | ❌ No | WARN | No |

## Testing Locally

Verify configuration is loaded correctly:

```bash
# Run with debug logging
export SPRING_PROFILE=local
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Ddebug"
```

Check logs for:
```
Successfully loaded .env file for development environment
Loaded from .env: JWT_SECRET
Loaded from .env: REFRESH_SECRET
```

## Additional Resources

- [Spring Boot Profiles](https://spring.io/blog/2015/04/12/deploying-spring-boot-applications)
- [Environment Variables in Spring](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [dotenv-java Documentation](https://github.com/cdimascio/dotenv-java)
- [Render Environment Variables](https://render.com/docs/configure-environment)
