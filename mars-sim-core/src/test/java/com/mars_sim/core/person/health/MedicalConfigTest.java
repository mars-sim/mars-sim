package com.mars_sim.core.person.health;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact.PhysicalEffort;

class MedicalConfigTest {

    private MedicalConfig medConfig;

    @BeforeEach
    void setUp() {
        var config = SimulationConfig.loadConfig();
        medConfig = config.getMedicalConfiguration();
    }
    
    @Test
    void testMinorOperationTreatment() {
        // Minor op needed tech level 2
        var c = medConfig.getTreatmentsByLevel(2);
        assertTrue(!c.isEmpty(), "Treatment list is not empty");

        var matched = c.stream()
                    .filter(cp -> cp.getName().equals("Minor Operation"))
                    .toList();
        assertEquals(1, matched.size(), "Found treatment");
        var found = matched.get(0);

        assertFalse(found.getSelfAdminister(), "Self admin");
        assertEquals(3, found.getSkill(), "Skill level");
        assertEquals(2, found.getFacilityLevel(), "Facility level");
        assertEquals(200D, found.getDuration(), "Treatment time");
    }

    @Test
    void testDressingTreatment() {
        var c = medConfig.getTreatmentsByLevel(1);
        assertTrue(!c.isEmpty(), "Treatment list is not empty");

        var matched = c.stream()
                    .filter(cp -> cp.getName().equals("Dressing"))
                    .toList();
        assertEquals(1, matched.size(), "Found treatment");
        var found = matched.get(0);

        assertTrue(found.getSelfAdminister(), "Self admin");
        assertEquals(0, found.getSkill(), "Skill level");
        assertEquals(1, found.getFacilityLevel(), "Facility level");
        assertEquals(50D, found.getDuration(), "Treatment time");
    }

    @Test
    void testTreatmentByLevel() {
        // Check level contents is in the previous level
        var previous = medConfig.getTreatmentsByLevel(1);
        assertTrue(!previous.isEmpty(), "Treatment level #1 is not empty");

        // Check that tech level upto 10 returns what is expected
        for(int i = 2; i <= 10; i++) {
            var n = medConfig.getTreatmentsByLevel(i);
            assertTrue(!n.isEmpty(), "Level #" + i + " has treatments");

            // Level N can do all treatments already in N-1
            assertTrue(n.containsAll(previous), "Level #" + i + " treatments are in previous level");

            previous = n;
        }
    }

    
    @Test
    void testDehydrationComplaint() {
        var c = medConfig.getComplaintList();
        assertTrue(!c.isEmpty(), "Complaint list is not empty");

        var found = medConfig.getComplaintByName(ComplaintType.DEHYDRATION);
        assertNotNull(found, "Found dehydration complaint");

        assertTrue(found.isEnvironmental(), "Is environmental");
        assertTrue(found.requiresBedRestRecovery(), "Needs bed rest");
        assertEquals(0D, found.getProbability(), "Probability");
        assertEquals(20, found.getSeriousness(), "Seriousness");
        assertEquals(3000D, found.getDegradePeriod(), "Degrade period");
        assertRecoveryPeriod(found, 1, 1 * MedicalConfig.RECOVERY_SPAN);
        assertEquals(0.8D, found.getPerformanceFactor(), "Performance impact");
        assertEquals(PhysicalEffort.NONE, found.getEffortInfluence(), "Effort influence");

        assertNull(found.getRecoveryTreatment(), "Treatment");
        assertNull(found.getNextPhase(), "Next complaint");
    }

    @Test
    void testAppendicitisComplaint() {
        var c = medConfig.getComplaintList();
        assertTrue(!c.isEmpty(), "Complaint list is not empty");

        var found = medConfig.getComplaintByName(ComplaintType.APPENDICITIS);
        assertNotNull(found, "Found appendictics complaint");

        assertFalse(found.isEnvironmental(), "Is environmental");
        assertTrue(found.requiresBedRestRecovery(), "Needs bed rest");
        assertEquals(0.5D, found.getProbability(), "Probability");
        assertEquals(60, found.getSeriousness(), "Seriousness");
        assertEquals(7000D, found.getDegradePeriod(), "Degrade period");
        assertRecoveryPeriod(found, 13, 15);
        assertEquals(0.5D, found.getPerformanceFactor(), "Performance impact");
        assertEquals(PhysicalEffort.NONE, found.getEffortInfluence(), "Effort influence");

        assertEquals("Minor Operation", found.getRecoveryTreatment().getName(), "Treatment");
        assertEquals(ComplaintType.RUPTURED_APPENDIX, found.getNextPhase().getType(), "Next complaint");
    }

		
    @Test
    void testBurnsComplaint() {
        var c = medConfig.getComplaintList();
        assertTrue(!c.isEmpty(), "Complaint list is not empty");

        var found = medConfig.getComplaintByName(ComplaintType.BURNS);
        assertNotNull(found, "Found burns complaint");

        assertFalse(found.isEnvironmental(), "Is environmental");
        assertTrue(found.requiresBedRestRecovery(), "Needs bed rest");
        assertEquals(1D, found.getProbability(), "Probability");
        assertEquals(50, found.getSeriousness(), "Seriousness");
        assertEquals(5000D, found.getDegradePeriod(), "Degrade period");
        assertRecoveryPeriod(found, 20, 20 * MedicalConfig.RECOVERY_SPAN);
        assertEquals(0.4D, found.getPerformanceFactor(), "Performance impact");
        assertEquals(PhysicalEffort.LOW, found.getEffortInfluence(), "Effort influence");

        assertEquals("Dressing", found.getRecoveryTreatment().getName(), "Treatment");
    }

    private void assertRecoveryPeriod(Complaint c, double min, double max) {
        var r = c.getRecoveryRange();
        assertEquals(min, r.min(), "Recovery min");
        assertEquals(max, r.max(), "Recovery max");

        var value = c.getRecoveryPeriod();
        assertTrue(((min*1000D <= value) && (value <= max*1000D)), "Value within range");
    }
}
