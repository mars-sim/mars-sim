package com.mars_sim.core.structure;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.MetaTask.TaskScope;

public class SettlementConfigTest extends AbstractMarsSimUnitTest {

    private static final String STANDARD_4_SHIFT = "Standard 4 Shift";
	private static final String STANDARD_3_SHIFT = "Standard 3 Shift";
	private static final String STANDARD_2_SHIFT = "Standard 2 Shift";
    private static final Object MIGHT_PARTY = "Night Sky Party";

    public void testGetEssentialResources() {
        var config = simConfig.getSettlementConfiguration();

        assertTrue("Config has essential resources", !config.getEssentialResources().isEmpty());
    }

    public void testDefaultShiftPattern() {
        var config = simConfig.getSettlementConfiguration();

        // Check standard shifts
        testShiftSize(config, STANDARD_2_SHIFT, 2);
        testShiftSize(config, STANDARD_3_SHIFT, 3);
        testShiftSize(config, STANDARD_4_SHIFT, 4);   
    }

    private void testShiftSize(SettlementConfig config, String name, int shifts) {
        var shift = config.getShiftByName(name);
        assertNotNull("Shift pattern " + name, shift);
        assertEquals("Shift size for " + name, shifts, shift.getShifts().size());
    }

    public void testShiftByPopulation() {
        var config = simConfig.getSettlementConfiguration();
    
        var large = config.getShiftByPopulation(30);
        assertTrue("Large shift has smaller min pop", 30 > large.getMinPopulation());
        assertEquals("Shift pattern for large", "Standard 3 Shift", large.getName());


        var small = config.getShiftByPopulation(8);
        assertTrue("Small shift has smaller min pop", 8 >= small.getMinPopulation());
        assertEquals("Shift pattern for small", "Standard 2 Shift", small.getName());

    }

    public void testGetActivityByPopulation() {
        var config = simConfig.getSettlementConfiguration();

        var small = config.getActivityByPopulation(8);
        assertTrue("Large ruleset has medium min pop", 8 > small.minPop());
        assertEquals("Activity Ruleset for small", "Small Settlement", small.name());
        assertFalse("Small Activity Ruleset has meetings", small.meetings().isEmpty());


        var verysmall = config.getActivityByPopulation(4);
        assertNull("No activities for vry small population", verysmall);

        var large = config.getActivityByPopulation(30);
        assertTrue("Large ruleset has smaller min pop", 30 > large.minPop());
        assertEquals("Activity Ruleset for large", "Large Settlement", large.name());

        // Large settlement should have night party
        var training = large.meetings().stream().filter(ga -> ga.name().equals(MIGHT_PARTY)).findFirst().get();
        assertNotNull("Movie Night Group Activity found", training);
        assertEquals("Activity Score", 400, training.score());
        assertEquals("Activity Pop", 0.2D, training.percentagePop());
        assertEquals("Activity Wait", 80, training.waitDuration());
        assertEquals("Activity Duration", 150, training.activityDuration());
        assertEquals("Activity Scope", TaskScope.NONWORK_HOUR, training.scope());
        assertEquals("Activity Start Time", 850, training.calendar().getTimeOfDay());
        assertEquals("Activity Freq", 10, training.calendar().getFrequency());
        assertEquals("Activity 1st Event", 2, training.calendar().getFirstSol());

        var impact = training.impact();
        assertEquals("Activity has skills", 1, impact.getImpactedSkills().size());
        assertTrue("Activity has correct skill", impact.getImpactedSkills().contains(SkillType.ASTRONOMY));
    }
}
