package com.mars_sim.core.person.health;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact.PhysicalEffort;

public class MedicalConfigTest extends AbstractMarsSimUnitTest{
    public void testMinorOperationTreatment() {
        // Minor op needed tech level 2
        var c = getConfig().getMedicalConfiguration().getTreatmentsByLevel(2);
        assertTrue("Treatment list is not empty", !c.isEmpty());

        var matched = c.stream()
                    .filter(cp -> cp.getName().equals("Minor Operation"))
                    .toList();
        assertEquals("Found treatment", 1, matched.size());
        var found = matched.get(0);

        assertFalse("Self admin", found.getSelfAdminister());
        assertEquals("Skill level", 3, found.getSkill());
        assertEquals("Facility level", 2, found.getFacilityLevel());
        assertEquals("Treatment time", 200D, found.getDuration());
    }

    public void testDressingTreatment() {
        var c = getConfig().getMedicalConfiguration().getTreatmentsByLevel(1);
        assertTrue("Treatment list is not empty", !c.isEmpty());

        var matched = c.stream()
                    .filter(cp -> cp.getName().equals("Dressing"))
                    .toList();
        assertEquals("Found treatment", 1, matched.size());
        var found = matched.get(0);

        assertTrue("Self admin", found.getSelfAdminister());
        assertEquals("Skill level", 0, found.getSkill());
        assertEquals("Facility level", 1, found.getFacilityLevel());
        assertEquals("Treatment time", 50D, found.getDuration());
    }

    public void testTreatmentByLevel() {
        var medConfig = getConfig().getMedicalConfiguration();

        // Check level contents is in the previous level
        var previous = medConfig.getTreatmentsByLevel(1);
        assertTrue("Treatment level #1 is not empty", !previous.isEmpty());

        // Check that tech level upto 10 returns what is expected
        for(int i = 2; i <= 10; i++) {
            var n = medConfig.getTreatmentsByLevel(i);
            assertTrue("Level #" + i + " has treatments", !n.isEmpty());

            // Level N can do all treatments already in N-1
            assertTrue("Level #" + i + " treatments are in previous level", n.containsAll(previous));

            previous = n;
        }
    }

    
    public void testDehydrationComplaint() {
        var c = getConfig().getMedicalConfiguration().getComplaintList();
        assertTrue("Complaint list is not empty", !c.isEmpty());

        var found = getConfig().getMedicalConfiguration().getComplaintByName(ComplaintType.DEHYDRATION);
        assertNotNull("Found dehydration complaint", found);

        assertTrue("Is environmental", found.isEnvironmental());
        assertTrue("Needs bed rest", found.requiresBedRestRecovery());
        assertEquals("Probability", 0D, found.getProbability());
        assertEquals("Seriousness", 20, found.getSeriousness());
        assertEquals("Degrade period", 3000D, found.getDegradePeriod());
        assertRecoveryPeriod(found, 1, 1 * MedicalConfig.RECOVERY_SPAN);
        assertEquals("Performance impact", 0.8D, found.getPerformanceFactor());
        assertEquals("Effort influence", PhysicalEffort.NONE, found.getEffortInfluence());

        assertNull("Treatment", found.getRecoveryTreatment());
        assertNull("Next complaint", found.getNextPhase());
    }

    public void testAppendicitisComplaint() {
        var c = getConfig().getMedicalConfiguration().getComplaintList();
        assertTrue("Complaint list is not empty", !c.isEmpty());

        var found = getConfig().getMedicalConfiguration().getComplaintByName(ComplaintType.APPENDICITIS);
        assertNotNull("Found appendictics complaint", found);

        assertFalse("Is environmental", found.isEnvironmental());
        assertTrue("Needs bed rest", found.requiresBedRestRecovery());
        assertEquals("Probability", 0.5D, found.getProbability());
        assertEquals("Seriousness", 60, found.getSeriousness());
        assertEquals("Degrade period", 7000D, found.getDegradePeriod());
        assertRecoveryPeriod(found, 13, 15);
        assertEquals("Performance impact", 0.5D, found.getPerformanceFactor());
        assertEquals("Effort influence", PhysicalEffort.NONE, found.getEffortInfluence());

        assertEquals("Treatment", "Minor Operation", found.getRecoveryTreatment().getName());
        assertEquals("Next complaint", ComplaintType.RUPTURED_APPENDIX, found.getNextPhase().getType());
    }

		
    public void testBurnsComplaint() {
        var c = getConfig().getMedicalConfiguration().getComplaintList();
        assertTrue("Complaint list is not empty", !c.isEmpty());

        var found = getConfig().getMedicalConfiguration().getComplaintByName(ComplaintType.BURNS);
        assertNotNull("Found burns complaint", found);

        assertFalse("Is environmental", found.isEnvironmental());
        assertTrue("Needs bed rest", found.requiresBedRestRecovery());
        assertEquals("Probability", 1D, found.getProbability());
        assertEquals("Seriousness", 50, found.getSeriousness());
        assertEquals("Degrade period", 5000D, found.getDegradePeriod());
        assertRecoveryPeriod(found, 20, 20 * MedicalConfig.RECOVERY_SPAN);
        assertEquals("Performance impact", 0.4D, found.getPerformanceFactor());
        assertEquals("Effort influence", PhysicalEffort.LOW, found.getEffortInfluence());

        assertEquals("Treatment", "Dressing", found.getRecoveryTreatment().getName());
    }

    private void assertRecoveryPeriod(Complaint c, double min, double max) {
        var r = c.getRecoveryRange();
        assertEquals("Recovery min", min, r.min());
        assertEquals("Recovery max", max, r.max());

        var value = c.getRecoveryPeriod();
        assertTrue("Value within range", ((min*1000D <= value)
                            && (value <= max*1000D)));
    }
}
