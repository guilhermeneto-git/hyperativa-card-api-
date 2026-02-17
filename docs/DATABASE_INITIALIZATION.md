# DataInitializer - Automatic Database Setup

## Overview

The `DataInitializer` class is a Spring Boot configuration component that automatically sets up the database when the application starts.

## How It Works

### 1. Check if Tables Exist

When the application starts, `DataInitializer` checks if the database tables (`users` and `cards`) exist:

```java
private boolean checkIfTablesExist(DataSource dataSource)
```

This method:
- Connects to the database
- Uses JDBC metadata to check for table existence
- Returns `true` if tables are found, `false` otherwise

### 2. Execute SQL Script (if needed)

If tables don't exist, it automatically executes the SQL initialization script:

```java
private void executeSqlScript(DataSource dataSource)
```

This method:
- Loads `src/main/resources/db/create_db.sql`
- Uses Spring's `ResourceDatabasePopulator`
- Executes all SQL statements
- Creates tables with proper indexes and constraints

### 3. Create Default Users

After ensuring tables exist, it checks if users need to be created:

```java
if (userRepository.count() == 0) {
    // Create admin and user accounts
}
```

This ensures:
- Admin user: `admin/admin123` (ROLE: ADMIN)
- Regular user: `user/user123` (ROLE: USER)
- Passwords are BCrypt encoded
- Users are only created once

## Configuration

### Application Properties

Key setting in `application.properties`:

```properties
# Validate schema without auto-creating/updating
spring.jpa.hibernate.ddl-auto=validate
```

This prevents Hibernate from automatically modifying the database schema. Instead, we control the schema through SQL scripts.

### SQL Script Location

The script must be located at:
```
src/main/resources/db/create_db.sql
```

## Execution Flow

```
Application Starts
    ↓
DataInitializer.initData() runs
    ↓
Check if tables exist
    ↓
    ├─→ Tables exist?
    │       ↓ YES
    │   Skip script execution
    │       ↓
    └─→ NO ─→ Execute create_db.sql
                    ↓
            Tables created
                    ↓
Check if users exist (count = 0?)
    ↓
    ├─→ Users exist?
    │       ↓ YES
    │   Skip user creation
    │       ↓
    └─→ NO ─→ Create default users
                    ↓
            Admin & User created
                    ↓
        Application ready
```

## Benefits

1. **Zero Configuration**: No manual database setup required
2. **Idempotent**: Safe to run multiple times
3. **Version Control**: Schema is tracked in SQL file
4. **Portable**: Works on any environment with MySQL
5. **Explicit Control**: Clear separation of concerns

## Logs

When the application starts, you'll see:

```
=== Database tables not found. Executing create_db.sql ===
SQL script executed successfully
=== Database and tables created successfully ===
=== Creating default users ===
✓ User 'admin' created successfully
✓ User 'user' created successfully
=== 2 users created ===
```

Or if tables already exist:

```
=== Database tables already exist ===
Users already exist in database. Total: 2
```

## Troubleshooting

### Script Not Found

**Error:** `SQL script not found at: db/create_db.sql`

**Solution:** Ensure the file exists at `src/main/resources/db/create_db.sql`

### Permission Denied

**Error:** `Access denied for user 'root'@'localhost'`

**Solution:** Update credentials in `application.properties`:
```properties
spring.datasource.username=your_user
spring.datasource.password=your_password
```

### Database Doesn't Exist

**Error:** `Unknown database 'card_db'`

**Solution:** Create database manually first:
```sql
CREATE DATABASE card_db;
```

Then restart the application. The tables will be created automatically.

### Schema Validation Failed

**Error:** `Schema-validation: missing table [users]`

**Solution:** 
1. Drop the database: `DROP DATABASE IF EXISTS card_db;`
2. Restart the application to recreate everything

## Manual Database Creation

If you prefer manual control, the database and tables are now created automatically by:

1. **DatabaseInitializer** - Creates the `card_db` database
2. **Hibernate** - Creates tables from JPA entity annotations (`ddl-auto=update`)
3. **DataInitializer** - Creates default users

No manual SQL scripts are needed. Just start the application!

## Development vs Production

### Development
- Let DataInitializer handle everything
- Quick iteration and testing
- No manual steps required
- Uses MySQL database

### Production
- Run SQL scripts manually during deployment
- Review schema changes before applying
- Disable automatic script execution if desired (by manually creating tables first)
- Uses MySQL database

### Testing
- **Uses H2 in-memory database**
- No DataInitializer SQL script execution
- Schema created automatically by Hibernate (`ddl-auto=create-drop`)
- Test configuration: `src/test/resources/application.properties`
- Isolated from production/development databases
- No manual setup required

**Test Database Configuration:**
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
```

## Related Files

- `DatabaseInitializer.java` - Creates database before Spring Boot starts
- `DataInitializer.java` - Creates default users after application starts
- `src/main/resources/db/create_db.sql` - SQL script for table creation (not actively used, Hibernate handles it)
- `application.properties` - Database configuration
- JPA Entity classes (`Card.java`, `User.java`) - Define database schema

