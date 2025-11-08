# JUnit 5 Migration Guide

## Overview
This document describes how to migrate tests from JUnit 3 (TestCase) to JUnit 5 (Jupiter).

## Migration Patterns

### Pattern 1: Simple Unit Tests (No Simulation Infrastructure)

For tests that don't require simulation infrastructure, use standard JUnit 5 annotations:

**Before (JUnit 3):**
```java
import junit.framework.TestCase;

public class MyTest extends TestCase {
    public void testSomething() {
        assertEquals("message", expected, actual);
    }
}
```

**After (JUnit 5):**
```java
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class MyTest {
    @Test
    public void testSomething() {
        assertEquals(expected, actual, "message");
    }
}
```

**Key Changes:**
- Remove `extends TestCase`
- Add `@Test` annotation to each test method
- Import `org.junit.jupiter.api.Assertions.*` instead of using inherited methods
- **Important:** JUnit 5 assertions have a different parameter order: `assertEquals(expected, actual, message)` instead of JUnit 3's `assertEquals(message, expected, actual)`

**Examples:**
- `ShiftTest.java` - Simple test without simulation dependencies
- `JobTypeTest.java` - Tests for enum grouping logic

### Pattern 2: Tests Requiring Simulation Infrastructure

For tests that need to create simulation entities (settlements, persons, buildings, etc.), extend `MarsSimUnitTest`:

**Before (JUnit 3 with AbstractMarsSimUnitTest):**
```java
import junit.framework.TestCase;

public class MySimTest extends TestCase {
    private Simulation sim;
    
    @Override
    public void setUp() {
        sim = Simulation.instance();
        sim.testRun();
    }
    
    public void testWithSettlement() {
        Settlement s = new MockSettlement("test");
        // test code
    }
}
```

**After (JUnit 5 with MarsSimUnitTest):**
```java
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.mars_sim.core.test.MarsSimUnitTest;

public class MySimTest extends MarsSimUnitTest {
    // No need for explicit setup - @BeforeEach is handled by MarsSimUnitTest
    
    @Test
    public void testWithSettlement() {
        Settlement s = buildSettlement("test");
        // test code
    }
}
```

**Key Changes:**
- Extend `MarsSimUnitTest` instead of `TestCase`
- Remove explicit setup methods - `@BeforeEach` is handled by the base class
- Use helper methods from MarsSimUnitTest:
  - `buildSettlement()` - Create test settlements
  - `buildPerson()` - Create test persons
  - `buildBuilding()` - Create mock buildings
  - `buildRover()` - Create test vehicles
  - `executeTask()` - Run tasks for testing
  - `createPulse()` - Create clock pulses
  - `getSim()` - Access simulation instance
  - `getConfig()` - Access configuration

**Examples:**
- `PersonBuilderTest.java` - Tests using settlement and person builders
- `SkillManagerTest.java` - Tests requiring person entities
- Most tests in `mars-sim-core/src/test/java/com/mars_sim/core/` that extend MarsSimUnitTest

### Pattern 3: Custom Assertions

JUnit 5 provides standard assertions, but for domain-specific assertions, use `SimulationAssertions`:

```java
import static com.mars_sim.core.test.SimulationAssertions.*;

@Test
public void testRange() {
    double value = calculateSomething();
    assertGreaterThan("Value should be positive", 0.0, value);
    assertLessThan("Value should be less than 100", 100.0, value);
}
```

**Available Custom Assertions:**
- `assertGreaterThan(message, minValue, actual)` - Assert value > minimum
- `assertLessThan(message, maxValue, actual)` - Assert value < maximum  
- `assertEqualLessThan(message, maxValue, actual)` - Assert value <= maximum

## Migration Checklist

When migrating a test:
- [ ] Replace `extends TestCase` with appropriate base (or none)
- [ ] Add `@Test` annotation to all test methods
- [ ] Update imports from `junit.framework.*` to `org.junit.jupiter.api.*`
- [ ] Update assertion parameter order (JUnit 5 puts message last)
- [ ] If using simulation: extend `MarsSimUnitTest` and use its helper methods
- [ ] Remove explicit setup/teardown if covered by base class
- [ ] Run the test to verify it passes

## Current Status

- **Migrated:** 89+ tests using JUnit 5
- **Remaining:** ~23 tests still using JUnit 3
- Both test styles work side-by-side during gradual migration

## References

- JUnit 5 User Guide: https://junit.org/junit5/docs/current/user-guide/
- `MarsSimUnitTest.java` - Base class for simulation tests
- `MarsSimContextImpl.java` - Context implementation providing test utilities
- `SimulationAssertions.java` - Custom assertion utilities
