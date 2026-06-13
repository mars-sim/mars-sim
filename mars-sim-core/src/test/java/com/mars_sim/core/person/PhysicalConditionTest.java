package com.mars_sim.core.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.TestEntityListener;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.person.health.HealthProblemState;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.test.MarsSimUnitTest;

class PhysicalConditionTest extends MarsSimUnitTest{

    @Test
    void testAttributesChangeOverTime() {
        Settlement s = buildSettlement("Test");
        Person person = buildPerson("Person", s);
        PhysicalCondition physicalCondition = person.getPhysicalCondition();

        // Initial values
        double initialStress = physicalCondition.getStress();
        double initialThirst = physicalCondition.getThirst();
        double initialFatigue = physicalCondition.getFatigue();
        double initialHunger = physicalCondition.getHunger();

        // Simulate time passing
        physicalCondition.timePassing(createPulse(10), s);

        // Check that the attributes have changed
        assertFalse(initialStress < physicalCondition.getStress(), "Stress should decrease over time");
        assertTrue(initialThirst < physicalCondition.getThirst(), "Thirst should increase over time");
        assertTrue(initialFatigue < physicalCondition.getFatigue(), "Fatigue should increase over time");
        assertTrue(initialHunger < physicalCondition.getHunger(), "Hunger should increase over time");
    }

    @Test
    void testStressLevelChangeFiresEvent() {
        Settlement s = buildSettlement("Test");
        Person person = buildPerson("Person", s);
        PhysicalCondition physicalCondition = person.getPhysicalCondition();

        // Add a listener to capture the event
        TestEntityListener listener = new TestEntityListener(PhysicalCondition.STRESS_EVENT);
        person.addEntityListener(listener);

        // Change the stress level and check if the event is fired
        physicalCondition.setStress(5);
        physicalCondition.timePassing(createPulse(10), s);
        assertEquals(0, listener.getEventsReceived(), "No events fired");

        var level = physicalCondition.getStressLevel();

        // Change to a higher level
        physicalCondition.setStress(80);
        physicalCondition.timePassing(createPulse(10), s);
        assertEquals(1, listener.getEventsReceived(), "Stress level change should fire an event");
        assertNotEquals(level, physicalCondition.getStressLevel(), "Stress level should have changed");
    }

    @Test
    void testPanicAttack() {
        Settlement s = buildSettlement("Test");
        Person person = buildPerson("Person", s);
        PhysicalCondition physicalCondition = person.getPhysicalCondition();

        // Change the stress level and check if the event is fired
        physicalCondition.setStress(5);
        physicalCondition.timePassing(createPulse(10), s);
        assertTrue(physicalCondition.getProblems().isEmpty(), "No problems should be present");

        // Change to a higher level
        physicalCondition.setStress(100);
        physicalCondition.timePassing(createPulse(10), s);
        var problems = physicalCondition.getProblems();
        assertEquals(1, problems.size(), "Problems should be present after stress increase");

        var panic = problems.get(0);
        assertEquals(ComplaintType.PANIC_ATTACK, panic.getComplaint().getType(), "Panic attack should be present");
        assertEquals(HealthProblemState.DEGRADING, panic.getState(), "Panic attack active");

        // Stay paniced
        physicalCondition.timePassing(createPulse(10), s);
        problems = physicalCondition.getProblems();
        assertEquals(1, problems.size(), "Problems stay same");
        assertEquals(HealthProblemState.DEGRADING, panic.getState(), "Panic attack still be active");
        assertTrue(problems.contains(panic), "Panic attack should still be present");

        // Cure
        physicalCondition.setStress(0);
        physicalCondition.timePassing(createPulse(10), s);
        assertTrue(physicalCondition.getProblems().isEmpty(), "Problems should be cured after stress decrease");
        assertEquals(HealthProblemState.CURED, panic.getState(), "Panic attack should be cured");
        
        var cured = physicalCondition.getHealthHistory();
        assertFalse(cured.isEmpty(), "Cured history should not be empty");
        assertEquals(ComplaintType.PANIC_ATTACK, cured.get(0).complaint().getType(), "Cured history should contain the panic attack");
    }

    @Test
    void testThirstLevelChangeFiresEvent() {
        Settlement s = buildSettlement("Test");
        Person person = buildPerson("Person", s);
        PhysicalCondition physicalCondition = person.getPhysicalCondition();

        // Add a listener to capture the event
        TestEntityListener listener = new TestEntityListener(PhysicalCondition.THIRST_EVENT);
        person.addEntityListener(listener);

        // Change the thirst level and check if the event is fired
        physicalCondition.setThirst(10);
        physicalCondition.timePassing(createPulse(10), s);
        assertEquals(0, listener.getEventsReceived(), "No events fired");

        var level = physicalCondition.getThirstLevel();

        // Change to a higher level
        physicalCondition.setThirst(ThirstLevel.DRY.getMaxValue() + 1);
        physicalCondition.timePassing(createPulse(10), s);
        assertEquals(1, listener.getEventsReceived(), "Thirst level change should fire an event");
        assertNotEquals(level, physicalCondition.getThirstLevel(), "Thirst level should have changed");
    }

    @Test
    void testDehyrated() {
        Settlement s = buildSettlement("Test");
        Person person = buildPerson("Person", s);
        PhysicalCondition physicalCondition = person.getPhysicalCondition();

        // Change the thirst level to max and check for dehydrated
        physicalCondition.setThirst(ThirstLevel.BONE_DRY.getMaxValue() + 1);
        physicalCondition.timePassing(createPulse(10), s);
        assertTrue(physicalCondition.getProblems().isEmpty(), "No problems should be present");

        // Check still no dehydration
        var timelimit = physicalCondition.getDessicatedWait();
        physicalCondition.timePassing(createPulse(timelimit/2), s);
        assertTrue(physicalCondition.getProblems().isEmpty(), "No problems still not present");

        // Change time expired to trigger dehydration
        physicalCondition.timePassing(createPulse(timelimit/1.5), s);
        var problems = physicalCondition.getProblems();
        assertEquals(1, problems.size(), "Problems should be present after thirst increase");
        var dehydration = problems.get(0);
        assertEquals(ComplaintType.DEHYDRATION, dehydration.getType(), "Dehydration should be present");
        assertEquals(HealthProblemState.DEGRADING, dehydration.getState(), "Dehydration getting worse");

        // Start recovery by changing thirst level
        physicalCondition.setThirst(ThirstLevel.ISOTONIC.getMaxValue() + 1);
        physicalCondition.timePassing(createPulse(10), s);
        assertEquals(1, problems.size(), "Still dehyrated but recovering");
        assertEquals(HealthProblemState.RECOVERING, dehydration.getState(), "Dehydration should be recovering");

        // Marked cured
        physicalCondition.setThirst(10);
        physicalCondition.timePassing(createPulse(10), s);
        assertTrue(physicalCondition.getProblems().isEmpty(), "Problems should be cured after thirst decrease");
        assertEquals(HealthProblemState.CURED, dehydration.getState(), "Dehydration should be cured");
    }

    @Test
    void testDeathByDehydration() {
        var s = buildSettlement("Test");
        var person = buildPerson("Person", s);
        var physicalCondition = person.getPhysicalCondition();
        
        // Change the thirst level to max and check for dehydrated
        physicalCondition.setThirst(ThirstLevel.BONE_DRY.getMaxValue() + 1);
        physicalCondition.timePassing(createPulse(physicalCondition.getDessicatedWait()+1), s);

        physicalCondition.setThirst(PhysicalCondition.MAX_THIRST);
        physicalCondition.timePassing(createPulse(physicalCondition.getDessicatedWait() * 2), s);
        assertTrue(physicalCondition.isDead(), "Person should be dead from dehydration");
        var death = physicalCondition.getDeathDetails();
        assertEquals(ComplaintType.DEHYDRATION, death.getIllness(), "Death should be from dehydration");
    }  

    @Test
    void testMostSeriousProblem() {
        var s = buildSettlement("Test");
        var person = buildPerson("Person", s);
        var physicalCondition = person.getPhysicalCondition();

        var medicConfig = getConfig().getMedicalConfiguration();

        // Add a less serious problem
        var minorProblem = physicalCondition.addMedicalComplaint(medicConfig.getComplaintByName(ComplaintType.FLU));
        assertEquals(minorProblem, physicalCondition.getMostSerious(), "Most serious problem should be flu");

        // Add a more serious problem
        var majorProblem = physicalCondition.addMedicalComplaint(medicConfig.getComplaintByName(ComplaintType.HEART_ATTACK));
        assertEquals(majorProblem, physicalCondition.getMostSerious(), "Most serious problem should be heart attack");

        // Cure the more serious problem
        majorProblem.setCured();
        physicalCondition.timePassing(createPulse(10), s);
        assertEquals(minorProblem, physicalCondition.getMostSerious(), "Most serious problem should revert to flu");

        minorProblem.setCured();
        physicalCondition.timePassing(createPulse(10), s);
        assertNull(physicalCondition.getMostSerious(), "No most serious problem should be present");
    }
}
