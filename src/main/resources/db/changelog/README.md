# Liquibase Changelog Directory

This directory contains all database schema migration files for the Batch Audit System.

## Directory Structure

```
db/changelog/
├── db.changelog-master.xml          # Master changelog file that includes all others
├── README.md                        # This documentation file
├── 001-create-audit-table.xml       # Initial audit table creation
├── 002-create-audit-indexes.xml     # Audit table indexes
└── [future-changesets].xml          # Additional schema changes
```

## Naming Convention

All changelog files must follow this naming pattern:
- **Format**: `{sequential-number}-{descriptive-name}.xml`
- **Sequential Number**: 3-digit zero-padded (001, 002, 003, etc.)
- **Descriptive Name**: Kebab-case description of the change
- **Extension**: Always `.xml`

### Examples
- `001-create-audit-table.xml` - Creates the main PIPELINE_AUDIT_LOG table
- `002-create-audit-indexes.xml` - Adds indexes for query optimization
- `003-add-audit-retention-policy.xml` - Adds data retention policies
- `004-modify-audit-table-columns.xml` - Modifies existing table structure

## Organization Guidelines

Changelog files should be organized by type and executed in this order:

1. **Schema Creation** (001-099): Create tables, sequences, and basic structure
2. **Index Creation** (100-199): Add indexes for performance optimization
3. **Data Migration** (200-299): Insert reference data or migrate existing data
4. **Schema Modifications** (300-399): Alter existing tables, add/remove columns
5. **Constraints & Triggers** (400-499): Add foreign keys, check constraints, triggers

## Best Practices

1. **Atomic Changes**: Each changeset should contain one logical change
2. **Rollback Support**: Include rollback instructions where possible
3. **Oracle Compatibility**: Use Oracle-specific SQL syntax and data types
4. **Comments**: Document the purpose and impact of each change
5. **Testing**: Test changesets against Oracle database before committing

## Master Changelog

The `db.changelog-master.xml` file includes all individual changelog files in the correct order. When adding new changelog files:

1. Create the new changelog file following the naming convention
2. Add an `<include>` statement in the master changelog
3. Ensure the include order maintains logical dependency sequence

## Liquibase Configuration

The system is configured to:
- Use Oracle database dialect
- Run changesets in the order specified in the master changelog
- Track executed changesets in DATABASECHANGELOG table
- Support multiple environments (dev, test, prod) via contexts