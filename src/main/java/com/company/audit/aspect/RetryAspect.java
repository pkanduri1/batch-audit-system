package com.company.audit.aspect;

import com.company.audit.exception.AuditPersistenceException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

/**
 * Aspect for applying retry logic to audit repository operations using Spring Boot 3.4+ AOP features.
 * 
 * This aspect automatically applies retry logic to repository methods that interact with the Oracle database,
 * providing resilience against transient connection issues and temporary database unavailability.
 * It uses the configured retry templates to handle different types of operations with appropriate retry policies.
 * 
 * Features:
 * - Automatic retry for repository methods
 * - Operation-specific retry templates
 * - Comprehensive error logging and monitoring
 * - Integration with audit exception hierarchy
 * - Support for Java 17+ enhanced exception handling
 * 
 * @author Audit Team
 * @version 1.0
 * @since 1.0
 */
@Aspect
@Component
public class RetryAspect {

    private static final Logger logger = LoggerFactory.getLogger(RetryAspect.class);

    private final RetryTemplate auditRetryTemplate;
    private final RetryTemplate databaseRetryTemplate;
    private final RetryTemplate quickRetryTemplate;

    /**
     * Constructor with retry template dependencies.
     * 
     * @param auditRetryTemplate general audit retry template
     * @param databaseRetryTemplate database-specific retry template
     * @param quickRetryTemplate quick operation retry template
     */
    public RetryAspect(
            @Qualifier("auditRetryTemplate") RetryTemplate auditRetryTemplate,
            @Qualifier("databaseRetryTemplate") RetryTemplate databaseRetryTemplate,
            @Qualifier("quickRetryTemplate") RetryTemplate quickRetryTemplate) {
        this.auditRetryTemplate = auditRetryTemplate;
        this.databaseRetryTemplate = databaseRetryTemplate;
        this.quickRetryTemplate = quickRetryTemplate;
    }

    /**
     * Applies retry logic to all repository save operations.
     * Uses database retry template for persistence operations.
     * 
     * @param joinPoint the method execution join point
     * @return the method result
     * @throws Throwable if the operation fails after all retries
     */
    @Around("execution(* com.company.audit.repository.AuditRepository.save(..))")
    public Object retryRepositorySave(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String operationName = className + "." + methodName;

        logger.debug("Applying database retry to operation: {}", operationName);

        return databaseRetryTemplate.execute(context -> {
            context.setAttribute("operation.name", operationName);
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                logger.debug("Repository save operation failed, will retry if applicable: {}", throwable.getMessage());
                
                // Wrap in AuditPersistenceException if not already
                if (!(throwable instanceof AuditPersistenceException)) {
                    throw new AuditPersistenceException("Repository save operation failed", throwable);
                }
                throw throwable;
            }
        });
    }

    /**
     * Applies retry logic to repository find operations.
     * Uses quick retry template for read operations.
     * 
     * @param joinPoint the method execution join point
     * @return the method result
     * @throws Throwable if the operation fails after all retries
     */
    @Around("execution(* com.company.audit.repository.AuditRepository.find*(..))")
    public Object retryRepositoryFind(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String operationName = className + "." + methodName;

        logger.debug("Applying quick retry to operation: {}", operationName);

        return quickRetryTemplate.execute(context -> {
            context.setAttribute("operation.name", operationName);
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                logger.debug("Repository find operation failed, will retry if applicable: {}", throwable.getMessage());
                
                // Wrap in AuditPersistenceException if not already
                if (!(throwable instanceof AuditPersistenceException)) {
                    throw new AuditPersistenceException("Repository find operation failed", throwable);
                }
                throw throwable;
            }
        });
    }

    /**
     * Applies retry logic to repository count operations.
     * Uses quick retry template for count operations.
     * 
     * @param joinPoint the method execution join point
     * @return the method result
     * @throws Throwable if the operation fails after all retries
     */
    @Around("execution(* com.company.audit.repository.AuditRepository.count*(..))")
    public Object retryRepositoryCount(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String operationName = className + "." + methodName;

        logger.debug("Applying quick retry to operation: {}", operationName);

        return quickRetryTemplate.execute(context -> {
            context.setAttribute("operation.name", operationName);
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                logger.debug("Repository count operation failed, will retry if applicable: {}", throwable.getMessage());
                
                // Wrap in AuditPersistenceException if not already
                if (!(throwable instanceof AuditPersistenceException)) {
                    throw new AuditPersistenceException("Repository count operation failed", throwable);
                }
                throw throwable;
            }
        });
    }

    /**
     * Applies retry logic to service-level audit operations.
     * Uses general audit retry template for service operations.
     * 
     * @param joinPoint the method execution join point
     * @return the method result
     * @throws Throwable if the operation fails after all retries
     */
    @Around("execution(* com.company.audit.service.AuditServiceImpl.log*(..))")
    public Object retryServiceAuditLog(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String operationName = className + "." + methodName;

        logger.debug("Applying audit retry to operation: {}", operationName);

        return auditRetryTemplate.execute(context -> {
            context.setAttribute("operation.name", operationName);
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                logger.debug("Service audit log operation failed, will retry if applicable: {}", throwable.getMessage());
                
                // Let AuditPersistenceException pass through as-is
                if (throwable instanceof AuditPersistenceException) {
                    throw throwable;
                }
                
                // Wrap other exceptions
                throw new AuditPersistenceException("Service audit log operation failed", throwable);
            }
        });
    }

    /**
     * Applies retry logic to service-level report generation operations.
     * Uses audit retry template for report operations.
     * 
     * @param joinPoint the method execution join point
     * @return the method result
     * @throws Throwable if the operation fails after all retries
     */
    @Around("execution(* com.company.audit.service.AuditServiceImpl.generate*(..))")
    public Object retryServiceReportGeneration(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String operationName = className + "." + methodName;

        logger.debug("Applying audit retry to operation: {}", operationName);

        return auditRetryTemplate.execute(context -> {
            context.setAttribute("operation.name", operationName);
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                logger.debug("Service report generation operation failed, will retry if applicable: {}", throwable.getMessage());
                
                // Let AuditPersistenceException pass through as-is
                if (throwable instanceof AuditPersistenceException) {
                    throw throwable;
                }
                
                // Wrap other exceptions
                throw new AuditPersistenceException("Service report generation operation failed", throwable);
            }
        });
    }
}