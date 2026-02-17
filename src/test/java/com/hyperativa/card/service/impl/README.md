# Unit Tests - Service Layer

## Overview

This directory contains unit tests for the service layer, validating business rules and error handling scenarios.

## Test Classes

### CardServiceImplTest

Tests for the `CardServiceImpl` service, which handles individual card operations.

**Test Scenarios:**

✅ **Success Cases:**
- `save_ShouldSaveCardSuccessfully_WhenValidCardNumberProvided()` - Validates successful card save
- `save_ShouldCallRepository_WithCorrectCardNumber()` - Ensures correct data is passed to repository
- `findIdByCardNumber_ShouldReturnCardId_WhenCardExists()` - Tests successful card lookup
- `save_ShouldReturnDtoWithIdOnly_NotExposingCardNumber()` - Validates security (card number not exposed)

❌ **Failure Cases:**
- `findIdByCardNumber_ShouldThrowCardNotFoundException_WhenCardDoesNotExist()` - Tests not found scenario
- `findIdByCardNumber_ShouldThrowCardNotFoundException_WhenNullCardNumber()` - Tests null handling

**Key Validations:**
- Repository is called with correct parameters
- Business rules are enforced (e.g., card number not exposed in response)
- Exceptions are thrown appropriately

---

### FileUploadServiceImplTest

Tests for the `FileUploadServiceImpl` service, which processes batch card file uploads.

**Test Scenarios:**

✅ **Success Cases:**
- `processCardFile_ShouldProcessValidFile_Successfully()` - Validates complete file processing
- `processCardFile_ShouldParseHeader_Correctly()` - Tests header parsing (lote name, date)
- `processCardFile_ShouldParseFooter_AndValidateCount()` - Tests footer parsing and validation
- `processCardFile_ShouldHandleEmptyFile_Gracefully()` - Tests empty file handling

❌ **Failure Cases:**
- `processCardFile_ShouldHandleDuplicateCards_Correctly()` - Tests duplicate card detection
- `processCardFile_ShouldHandleInvalidCardNumber_Gracefully()` - Tests invalid number format
- `processCardFile_ShouldHandleInvalidCardNumberLength_BySkipping()` - Tests wrong length cards
- `processCardFile_ShouldHandleIOException_WithErrorStatus()` - Tests I/O error handling
- `processCardFile_ShouldProcessMultipleDuplicates_AndCountCorrectly()` - Tests multiple duplicates

**Key Validations:**
- Duplicate cards are detected and counted correctly
- Invalid card numbers don't break processing
- File format errors are handled gracefully
- Counters (processed, duplicated, errors) are accurate
- Header and footer are parsed correctly

---

## Test Approach

### Mocking Strategy
- **CardRepository**: Mocked to isolate service logic
- **No external dependencies**: Tests run in-memory without database

### Test Structure
```java
@BeforeEach  // Setup test data
@Test        // Individual test case
// Arrange   - Prepare test data and mocks
// Act       - Execute the method under test
// Assert    - Verify the results
```

### Assertions Used
- `assertEquals()` - Value comparison
- `assertNotNull()` - Null checking
- `assertTrue()` / `assertFalse()` - Boolean conditions
- `assertThrows()` - Exception verification
- `verify()` - Mock interaction verification

---

## Running Tests

### All Service Tests
```bash
mvn test -Dtest="*ServiceImplTest"
```

### Specific Test Class
```bash
mvn test -Dtest=CardServiceImplTest
mvn test -Dtest=FileUploadServiceImplTest
```

### Specific Test Method
```bash
mvn test -Dtest=CardServiceImplTest#save_ShouldSaveCardSuccessfully_WhenValidCardNumberProvided
```

### All Tests
```bash
mvn test
```

---

## Coverage

These tests cover:

1. ✅ **Business Logic**: Save operations, lookups, file processing
2. ✅ **Error Handling**: Not found, duplicates, invalid data
3. ✅ **Edge Cases**: Null values, empty files, I/O errors
4. ✅ **Security**: Card numbers not exposed in responses
5. ✅ **Data Validation**: Card number format, file structure

---

## Test Data

### Valid Card Numbers
- `4456897999999999L` - Standard test card
- `4456897919999999L` - Alternative test card

### File Format
```
HLOTE0001              000001020180524
C000001445689791999999900000000000000000000000000000000000000000000000000000000
C000002445689792999999900000000000000000000000000000000000000000000000000000000
LOTE00010002
```

**Format:**
- **Line 1 (Header)**: `H` + Lote Name (8 chars) + padding + quantity (6 digits) + date (8 digits)
- **Lines 2-N (Cards)**: `C` + sequence (6 digits) + card number (19 digits) + padding
- **Last Line (Footer)**: Lote Name + sequence (4 digits) + quantity (4 digits)

---

## Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

Includes:
- JUnit 5 (Jupiter)
- Mockito
- AssertJ
- Spring Test

