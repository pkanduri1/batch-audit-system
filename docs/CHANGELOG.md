# Changelog

## Database Version Update - December 2024

### Changed
- **Oracle Database Support**: Updated supported Oracle Database versions
  - Removed: Oracle Database 12c Release 2 (12.2.0.1)
  - Current supported versions: Oracle Database 19c (recommended) and Oracle Database 21c
  
### Updated Files
- `docs/database-setup.md`: Removed Oracle 12c from supported versions list
- `README.md`: Updated technology stack and prerequisites sections
- `.kiro/steering/tech.md`: Updated database technology specifications
- `.kiro/specs/batch-audit-system/tasks.md`: Updated task descriptions for Oracle configuration
- `.kiro/specs/batch-audit-system/design.md`: Updated Hibernate dialect configuration
- `src/main/resources/application-local.properties`: Updated Hibernate dialect from Oracle12cDialect to Oracle19cDialect

### Technical Impact
- **Hibernate Dialect**: Changed from `org.hibernate.dialect.Oracle12cDialect` to `org.hibernate.dialect.Oracle19cDialect`
- **Database Features**: Now leveraging Oracle 19c/21c specific features and optimizations
- **Compatibility**: Ensures compatibility with modern Oracle database versions

### Migration Notes
- Existing Oracle 12c installations should be upgraded to Oracle 19c or 21c
- No schema changes required - Liquibase migrations remain compatible
- Application configuration may need dialect updates in production environments