# Production Deployment Guide

## Overview

This guide provides step-by-step instructions for deploying the Batch Audit System to production. The system is fully implemented and ready for enterprise deployment with comprehensive REST API, security, and Oracle database integration.

## Prerequisites

### Infrastructure Requirements
- **Oracle Database**: 19c or 21c with appropriate sizing for audit volume
- **Java Runtime**: OpenJDK or Oracle JDK 17+
- **Application Server**: Docker/Kubernetes or traditional application server
- **Load Balancer**: With SSL/TLS termination capability
- **Monitoring**: Prometheus, Grafana, ELK stack, or equivalent

### Security Requirements
- **JWT Provider**: OAuth2/OIDC compliant identity provider
- **SSL Certificates**: Valid certificates for HTTPS endpoints
- **Network Security**: Firewall rules and security groups configured

## Database Setup

### 1. Oracle Database Configuration

```sql
-- Connect as SYSDBA
sqlplus sys/password@database as sysdba

-- Create audit user and schema
CREATE USER audit_user IDENTIFIED BY "SecurePassword123!";
GRANT CONNECT, RESOURCE, CREATE VIEW TO audit_user;
GRANT UNLIMITED TABLESPACE TO audit_user;

-- Grant additional permissions for Liquibase
GRANT CREATE TABLE, CREATE INDEX, CREATE SEQUENCE TO audit_user;
GRANT ALTER ANY TABLE, ALTER ANY INDEX TO audit_user;

-- Verify user creation
SELECT username, account_status FROM dba_users WHERE username = 'AUDIT_USER';
```

### 2. Database Performance Tuning

```sql
-- Configure Oracle for audit workload
ALTER SYSTEM SET shared_pool_size = 512M SCOPE=BOTH;
ALTER SYSTEM SET db_cache_size = 1G SCOPE=BOTH;
ALTER SYSTEM SET pga_aggregate_target = 512M SCOPE=BOTH;

-- Enable automatic statistics gathering
BEGIN
  DBMS_STATS.SET_GLOBAL_PREFS('AUTOSTATS_TARGET', 'ALL');
END;
/
```

### 3. Run Database Migrations

```bash
# Set environment variables
export ORACLE_DB_URL="jdbc:oracle:thin:@//prod-oracle:1521/PRODDB"
export ORACLE_DB_USERNAME="audit_user"
export ORACLE_DB_PASSWORD="SecurePassword123!"

# Run Liquibase migrations
mvn liquibase:update -Dspring.profiles.active=prod \
  -Dliquibase.url=${ORACLE_DB_URL} \
  -Dliquibase.username=${ORACLE_DB_USERNAME} \
  -Dliquibase.password=${ORACLE_DB_PASSWORD}

# Verify migration status
mvn liquibase:status -Dspring.profiles.active=prod
```

## Application Configuration

### 1. Production Configuration File

Create `application-prod.yml`:

```yaml
# Production Configuration
server:
  port: 8080
  servlet:
    context-path: /audit

spring:
  datasource:
    url: ${ORACLE_DB_URL}
    username: ${ORACLE_DB_USERNAME}
    password: ${ORACLE_DB_PASSWORD}
    driver-class-name: oracle.jdbc.OracleDriver
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-test-query: "SELECT 1 FROM DUAL"
      leak-detection-threshold: 60000

  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${JWT_JWK_SET_URI}
          issuer: ${JWT_ISSUER}

  liquibase:
    enabled: false  # Disable in production after initial setup

# Audit system configuration
audit:
  database:
    batch-size: 500
    connection-pool-size: 50
  retry:
    enabled: true
    default:
      max-attempts: 3
      initial-delay: 1000
      max-delay: 30000
      multiplier: 2.0

# Logging configuration
logging:
  level:
    com.company.audit: INFO
    org.springframework.security: WARN
    liquibase: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /var/log/audit-system/application.log

# SpringDoc configuration for production
springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true
    oauth:
      client-id: ${OAUTH_CLIENT_ID}
      use-pkce-with-authorization-code-grant: true

# Actuator endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
  endpoint:
    health:
      show-details: when-authorized
```

### 2. Environment Variables

Set the following environment variables:

```bash
# Database Configuration
export ORACLE_DB_URL="jdbc:oracle:thin:@//prod-oracle:1521/PRODDB"
export ORACLE_DB_USERNAME="audit_user"
export ORACLE_DB_PASSWORD="SecurePassword123!"

# JWT Configuration
export JWT_JWK_SET_URI="https://your-auth-server/.well-known/jwks.json"
export JWT_ISSUER="https://your-auth-server"

# OAuth2 Configuration (for Swagger UI)
export OAUTH_CLIENT_ID="audit-system-client"

# JVM Configuration
export JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

## Application Deployment

### 1. Build Production Artifact

```bash
# Clean build with production profile
mvn clean package -Dspring.profiles.active=prod -DskipTests=false

# Verify artifact creation
ls -la target/batch-audit-system-*.jar
```

### 2. Docker Deployment (Recommended)

Create `Dockerfile`:

```dockerfile
FROM openjdk:17-jre-slim

# Create application user
RUN groupadd -r audit && useradd -r -g audit audit

# Set working directory
WORKDIR /app

# Copy application jar
COPY target/batch-audit-system-*.jar app.jar

# Create log directory
RUN mkdir -p /var/log/audit-system && chown audit:audit /var/log/audit-system

# Switch to application user
USER audit

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/audit/actuator/health || exit 1

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and deploy:

```bash
# Build Docker image
docker build -t batch-audit-system:1.0.0 .

# Run container
docker run -d \
  --name audit-system \
  -p 8080:8080 \
  -e ORACLE_DB_URL="${ORACLE_DB_URL}" \
  -e ORACLE_DB_USERNAME="${ORACLE_DB_USERNAME}" \
  -e ORACLE_DB_PASSWORD="${ORACLE_DB_PASSWORD}" \
  -e JWT_JWK_SET_URI="${JWT_JWK_SET_URI}" \
  -e JWT_ISSUER="${JWT_ISSUER}" \
  -e SPRING_PROFILES_ACTIVE="prod" \
  -v /var/log/audit-system:/var/log/audit-system \
  batch-audit-system:1.0.0
```

### 3. Kubernetes Deployment

Create `k8s-deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: batch-audit-system
  labels:
    app: batch-audit-system
spec:
  replicas: 3
  selector:
    matchLabels:
      app: batch-audit-system
  template:
    metadata:
      labels:
        app: batch-audit-system
    spec:
      containers:
      - name: audit-system
        image: batch-audit-system:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: ORACLE_DB_URL
          valueFrom:
            secretKeyRef:
              name: audit-db-secret
              key: url
        - name: ORACLE_DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: audit-db-secret
              key: username
        - name: ORACLE_DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: audit-db-secret
              key: password
        - name: JWT_JWK_SET_URI
          valueFrom:
            configMapKeyRef:
              name: audit-config
              key: jwt-jwk-set-uri
        - name: JWT_ISSUER
          valueFrom:
            configMapKeyRef:
              name: audit-config
              key: jwt-issuer
        resources:
          requests:
            memory: "2Gi"
            cpu: "500m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /audit/actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /audit/actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10

---
apiVersion: v1
kind: Service
metadata:
  name: batch-audit-system-service
spec:
  selector:
    app: batch-audit-system
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: ClusterIP
```

Deploy to Kubernetes:

```bash
# Create secrets and config maps
kubectl create secret generic audit-db-secret \
  --from-literal=url="${ORACLE_DB_URL}" \
  --from-literal=username="${ORACLE_DB_USERNAME}" \
  --from-literal=password="${ORACLE_DB_PASSWORD}"

kubectl create configmap audit-config \
  --from-literal=jwt-jwk-set-uri="${JWT_JWK_SET_URI}" \
  --from-literal=jwt-issuer="${JWT_ISSUER}"

# Deploy application
kubectl apply -f k8s-deployment.yaml

# Verify deployment
kubectl get pods -l app=batch-audit-system
kubectl logs -l app=batch-audit-system
```

## Post-Deployment Verification

### 1. Health Checks

```bash
# Application health
curl -k https://your-domain/audit/actuator/health

# Database connectivity
curl -k https://your-domain/audit/actuator/health/db

# Expected response:
# {"status":"UP","components":{"db":{"status":"UP"}}}
```

### 2. API Testing

```bash
# Get JWT token from your auth provider
export JWT_TOKEN="your-jwt-token"

# Test audit events endpoint
curl -H "Authorization: Bearer ${JWT_TOKEN}" \
  "https://your-domain/audit/api/audit/events?page=0&size=10"

# Test statistics endpoint
curl -H "Authorization: Bearer ${JWT_TOKEN}" \
  "https://your-domain/audit/api/audit/statistics?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59"
```

### 3. Swagger UI Access

Navigate to: `https://your-domain/audit/swagger-ui.html`

- Authenticate using OAuth2/JWT
- Test API endpoints interactively
- Verify all endpoints are accessible and functional

## Monitoring and Alerting

### 1. Key Metrics to Monitor

```bash
# Application metrics
curl https://your-domain/audit/actuator/metrics/jvm.memory.used
curl https://your-domain/audit/actuator/metrics/hikaricp.connections.active
curl https://your-domain/audit/actuator/metrics/http.server.requests

# Custom audit metrics (if implemented)
curl https://your-domain/audit/actuator/metrics/audit.events.processed
curl https://your-domain/audit/actuator/metrics/audit.reconciliation.reports.generated
```

### 2. Recommended Alerts

- **Database Connection Pool**: Alert when active connections > 80% of maximum
- **API Response Time**: Alert when 95th percentile > 500ms
- **Error Rate**: Alert when error rate > 5% over 5 minutes
- **Memory Usage**: Alert when heap usage > 85%
- **Disk Space**: Alert when log directory > 80% full

### 3. Log Monitoring

Monitor application logs for:
- Authentication failures
- Database connection errors
- Audit event processing failures
- Reconciliation report generation issues

## Security Considerations

### 1. Network Security
- Configure firewall rules to allow only necessary traffic
- Use private subnets for database connections
- Implement network segmentation between tiers

### 2. Application Security
- Regularly rotate JWT signing keys
- Monitor for suspicious API access patterns
- Implement rate limiting on API endpoints
- Keep dependencies updated for security patches

### 3. Database Security
- Use encrypted connections (SSL/TLS)
- Implement database audit logging
- Regular security patches and updates
- Backup encryption and secure storage

## Backup and Recovery

### 1. Database Backup
```sql
-- Create backup of audit data
expdp audit_user/password@database \
  directory=DATA_PUMP_DIR \
  dumpfile=audit_backup_%U.dmp \
  parallel=4 \
  compression=all
```

### 2. Application Configuration Backup
- Backup all configuration files and environment variables
- Document deployment procedures and rollback steps
- Test recovery procedures regularly

## Troubleshooting

### Common Issues

1. **Database Connection Failures**
   - Check Oracle database status and connectivity
   - Verify connection pool configuration
   - Review network connectivity and firewall rules

2. **JWT Authentication Issues**
   - Verify JWT issuer and JWK set URI configuration
   - Check token expiration and renewal
   - Validate OAuth2 client configuration

3. **Performance Issues**
   - Monitor database query performance
   - Check connection pool utilization
   - Review JVM memory and garbage collection metrics

### Support Contacts
- Database Team: [database-team@company.com]
- Security Team: [security-team@company.com]
- Infrastructure Team: [infrastructure-team@company.com]

This deployment guide provides comprehensive instructions for production deployment of the Batch Audit System. Follow these steps carefully and adapt configurations to your specific environment requirements.