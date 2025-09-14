# Deployment Guide

## Overview

This guide covers the deployment of the Batch Audit System to production environments. The system is built with Spring Boot 3.4+ and requires Oracle Database 19c/21c for persistence.

## Prerequisites

### System Requirements
- **Java Runtime**: OpenJDK 17+ or Oracle JDK 17+
- **Database**: Oracle Database 19c/21c with appropriate schemas
- **Memory**: Minimum 2GB RAM, recommended 4GB+
- **Storage**: 10GB+ for application and logs
- **Network**: HTTPS connectivity for API endpoints

### Database Requirements
- Oracle Database 19c or 21c
- Database schema with appropriate privileges
- Network connectivity from application server to database
- Connection pooling support (HikariCP compatible)

## Configuration

### Environment Variables

Set the following environment variables for production deployment:

```bash
# Database Configuration
export ORACLE_DB_URL="jdbc:oracle:thin:@//prod-oracle-host:1521/ORCLPDB1"
export ORACLE_DB_USERNAME="audit_user"
export ORACLE_DB_PASSWORD="secure_password_here"

# Application Configuration
export SPRING_PROFILES_ACTIVE="prod"
export SERVER_PORT="8080"
export MANAGEMENT_SERVER_PORT="8081"

# Security Configuration
export JWT_ISSUER_URI="https://your-auth-server.com"
export JWT_JWK_SET_URI="https://your-auth-server.com/.well-known/jwks.json"

# Logging Configuration
export LOGGING_LEVEL_ROOT="INFO"
export LOGGING_LEVEL_COM_COMPANY_AUDIT="DEBUG"
export LOG_FILE_PATH="/var/log/batch-audit/application.log"

# Monitoring Configuration
export MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE="health,info,metrics,prometheus"
export MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS="when-authorized"
```

### Production Configuration File

Create `application-prod.yml`:

```yaml
server:
  port: ${SERVER_PORT:8080}
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: batch-audit-api

spring:
  datasource:
    url: ${ORACLE_DB_URL}
    username: ${ORACLE_DB_USERNAME}
    password: ${ORACLE_DB_PASSWORD}
    driver-class-name: oracle.jdbc.OracleDriver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
      connection-test-query: SELECT 1 FROM DUAL

  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
    contexts: prod
    default-schema: ${ORACLE_DB_USERNAME}
    drop-first: false

  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${JWT_ISSUER_URI}
          jwk-set-uri: ${JWT_JWK_SET_URI}

# Audit System Configuration
audit:
  database:
    batch-size: 500
    connection-pool-size: 20
  retention:
    days: 2555  # 7 years for compliance
  reconciliation:
    auto-generate: true
    schedule: "0 0 6 * * ?"  # Daily at 6 AM
  api:
    rate-limit:
      standard: 100  # requests per minute
      statistics: 10
      reports: 5

# Actuator Configuration
management:
  server:
    port: ${MANAGEMENT_SERVER_PORT:8081}
  endpoints:
    web:
      exposure:
        include: ${MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE:health,info,metrics}
  endpoint:
    health:
      show-details: ${MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS:when-authorized}
  metrics:
    export:
      prometheus:
        enabled: true

# Logging Configuration
logging:
  level:
    root: ${LOGGING_LEVEL_ROOT:INFO}
    com.company.audit: ${LOGGING_LEVEL_COM_COMPANY_AUDIT:INFO}
    org.springframework.security: INFO
    org.springframework.web: INFO
  file:
    name: ${LOG_FILE_PATH:/var/log/batch-audit/application.log}
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"

# SpringDoc OpenAPI Configuration
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
  show-actuator: false
```

## Database Setup

### 1. Create Database Schema

```sql
-- Create audit user and schema
CREATE USER audit_user IDENTIFIED BY "secure_password_here";

-- Grant necessary privileges
GRANT CONNECT, RESOURCE TO audit_user;
GRANT CREATE TABLE, CREATE INDEX, CREATE SEQUENCE TO audit_user;
GRANT UNLIMITED TABLESPACE TO audit_user;

-- Grant additional privileges for Liquibase
GRANT CREATE VIEW TO audit_user;
GRANT SELECT ON DBA_TABLES TO audit_user;
GRANT SELECT ON DBA_TAB_COLUMNS TO audit_user;
```

### 2. Run Liquibase Migrations

The application will automatically run Liquibase migrations on startup. To run manually:

```bash
# Set environment variables
export ORACLE_DB_URL="jdbc:oracle:thin:@//prod-oracle-host:1521/ORCLPDB1"
export ORACLE_DB_USERNAME="audit_user"
export ORACLE_DB_PASSWORD="secure_password_here"

# Run migrations
java -jar batch-audit-system.jar --spring.liquibase.contexts=prod
```

### 3. Verify Database Setup

```sql
-- Connect as audit_user and verify tables
SELECT table_name FROM user_tables WHERE table_name LIKE '%AUDIT%';

-- Verify indexes
SELECT index_name, table_name FROM user_indexes WHERE table_name = 'PIPELINE_AUDIT_LOG';

-- Check table structure
DESCRIBE PIPELINE_AUDIT_LOG;
```

## Application Deployment

### 1. Build Application

```bash
# Build with Maven
mvn clean package -Pprod

# Verify JAR file
ls -la target/batch-audit-system-*.jar
```

### 2. Docker Deployment

Create `Dockerfile`:

```dockerfile
FROM openjdk:17-jre-slim

# Create application user
RUN groupadd -r audit && useradd -r -g audit audit

# Set working directory
WORKDIR /app

# Copy application JAR
COPY target/batch-audit-system-*.jar app.jar

# Create log directory
RUN mkdir -p /var/log/batch-audit && chown -R audit:audit /var/log/batch-audit

# Switch to application user
USER audit

# Expose ports
EXPOSE 8080 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run Docker container:

```bash
# Build Docker image
docker build -t batch-audit-system:latest .

# Run container
docker run -d \
  --name batch-audit-system \
  -p 8080:8080 \
  -p 8081:8081 \
  -e ORACLE_DB_URL="jdbc:oracle:thin:@//prod-oracle-host:1521/ORCLPDB1" \
  -e ORACLE_DB_USERNAME="audit_user" \
  -e ORACLE_DB_PASSWORD="secure_password_here" \
  -e SPRING_PROFILES_ACTIVE="prod" \
  -v /var/log/batch-audit:/var/log/batch-audit \
  batch-audit-system:latest
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
  replicas: 2
  selector:
    matchLabels:
      app: batch-audit-system
  template:
    metadata:
      labels:
        app: batch-audit-system
    spec:
      containers:
      - name: batch-audit-system
        image: batch-audit-system:latest
        ports:
        - containerPort: 8080
        - containerPort: 8081
        env:
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
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
        resources:
          requests:
            memory: "2Gi"
            cpu: "500m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
        volumeMounts:
        - name: log-volume
          mountPath: /var/log/batch-audit
      volumes:
      - name: log-volume
        persistentVolumeClaim:
          claimName: audit-logs-pvc

---
apiVersion: v1
kind: Service
metadata:
  name: batch-audit-service
spec:
  selector:
    app: batch-audit-system
  ports:
  - name: api
    port: 8080
    targetPort: 8080
  - name: management
    port: 8081
    targetPort: 8081
  type: ClusterIP

---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: batch-audit-ingress
  annotations:
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
spec:
  tls:
  - hosts:
    - audit-api.company.com
    secretName: audit-api-tls
  rules:
  - host: audit-api.company.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: batch-audit-service
            port:
              number: 8080
```

Deploy to Kubernetes:

```bash
# Create database secret
kubectl create secret generic audit-db-secret \
  --from-literal=url="jdbc:oracle:thin:@//prod-oracle-host:1521/ORCLPDB1" \
  --from-literal=username="audit_user" \
  --from-literal=password="secure_password_here"

# Deploy application
kubectl apply -f k8s-deployment.yaml
```

## SSL/TLS Configuration

### 1. Generate SSL Certificate

```bash
# Generate keystore
keytool -genkeypair -alias batch-audit-api \
  -keyalg RSA -keysize 2048 \
  -storetype PKCS12 \
  -keystore keystore.p12 \
  -validity 365 \
  -dname "CN=audit-api.company.com,OU=IT,O=Company,L=City,ST=State,C=US"

# Export certificate
keytool -exportcert -alias batch-audit-api \
  -keystore keystore.p12 \
  -file audit-api.crt
```

### 2. Configure SSL in Application

Add to `application-prod.yml`:

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: batch-audit-api
```

## Monitoring and Observability

### 1. Health Checks

The application provides comprehensive health checks:

```bash
# Application health
curl https://audit-api.company.com:8081/actuator/health

# Database connectivity
curl https://audit-api.company.com:8081/actuator/health/db

# Readiness probe
curl https://audit-api.company.com:8081/actuator/health/readiness
```

### 2. Metrics Collection

Configure Prometheus metrics collection:

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'batch-audit-system'
    static_configs:
      - targets: ['audit-api.company.com:8081']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 30s
```

### 3. Log Aggregation

Configure log forwarding to centralized logging:

```yaml
# filebeat.yml
filebeat.inputs:
- type: log
  enabled: true
  paths:
    - /var/log/batch-audit/application.log
  fields:
    service: batch-audit-system
    environment: production

output.elasticsearch:
  hosts: ["elasticsearch.company.com:9200"]
  index: "batch-audit-logs-%{+yyyy.MM.dd}"
```

## Security Considerations

### 1. Network Security
- Use HTTPS for all API communications
- Restrict database access to application servers only
- Implement proper firewall rules
- Use VPN or private networks for internal communications

### 2. Authentication and Authorization
- Configure JWT token validation with proper issuer verification
- Implement role-based access control (RBAC)
- Use strong passwords and rotate credentials regularly
- Enable audit logging for authentication events

### 3. Data Protection
- Encrypt sensitive data at rest and in transit
- Implement proper backup and recovery procedures
- Follow data retention policies and compliance requirements
- Regular security assessments and vulnerability scanning

## Backup and Recovery

### 1. Database Backup

```bash
# Oracle RMAN backup
rman target / <<EOF
BACKUP DATABASE PLUS ARCHIVELOG;
DELETE NOPROMPT OBSOLETE;
EOF
```

### 2. Application Backup

```bash
# Backup configuration and logs
tar -czf audit-backup-$(date +%Y%m%d).tar.gz \
  /opt/batch-audit-system/ \
  /var/log/batch-audit/ \
  /etc/batch-audit/
```

### 3. Disaster Recovery

- Maintain standby database with Oracle Data Guard
- Implement automated failover procedures
- Regular disaster recovery testing
- Document recovery time objectives (RTO) and recovery point objectives (RPO)

## Troubleshooting

### Common Issues

1. **Database Connection Failures**
   - Check network connectivity
   - Verify credentials and permissions
   - Review connection pool settings

2. **High Memory Usage**
   - Monitor JVM heap usage
   - Adjust connection pool sizes
   - Review application logs for memory leaks

3. **Performance Issues**
   - Check database query performance
   - Monitor API response times
   - Review indexing strategy

### Log Analysis

```bash
# Check application startup
grep "Started BatchAuditApplication" /var/log/batch-audit/application.log

# Monitor database connections
grep "HikariPool" /var/log/batch-audit/application.log

# Check for errors
grep "ERROR" /var/log/batch-audit/application.log | tail -20
```

## Maintenance

### Regular Tasks

1. **Database Maintenance**
   - Update table statistics
   - Rebuild indexes as needed
   - Archive old audit data based on retention policy

2. **Application Updates**
   - Apply security patches
   - Update dependencies
   - Test in staging environment before production

3. **Monitoring**
   - Review performance metrics
   - Check disk space usage
   - Monitor error rates and response times

### Scheduled Maintenance Windows

- Plan maintenance during low-usage periods
- Coordinate with dependent systems
- Maintain rollback procedures
- Communicate maintenance schedules to stakeholders