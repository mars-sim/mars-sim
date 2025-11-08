package com.mars_sim.core.structure;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.MetaTask.TaskScope;

class SettlementConfigTest {

    private static final String STANDARD_4_SHIFT = "Standard 4 Shift";
	private static final String STANDARD_3_SHIFT = "Standard 3 Shift";
	private static final String STANDARD_2_SHIFT = "Standard 2 Shift";
    private static final Object MIGHT_PARTY = "Night Sky Party";
    private SettlementConfig config;

    @BeforeEach
    void setUp() {
        var simConfig = SimulationConfig.loadConfig();
        config = simConfig.getSettlementConfiguration();
    }

    @Test
    void testGetEssentialResources() {

        assertTrue(!config.getEssentialResources().isEmpty(), "Config has essential resources");
    }

    @Test
    void testDefaultShiftPattern() {

        // Check standard shifts
        testShiftSize(config, STANDARD_2_SHIFT, 2);
        testShiftSize(config, STANDARD_3_SHIFT, 3);
        testShiftSize(config, STANDARD_4_SHIFT, 4);   
    }

    private void testShiftSize(SettlementConfig config, String name, int shifts) {
        var shift = config.getShiftByName(name);
        assertNotNull(shift, "Shift pattern " + name);
        assertEquals(shifts, shift.getShifts().size(), "Shift size for " + name);
    }

    @Test
    void testShiftByPopulation() {
    
        var large = config.getShiftByPopulation(30);
        assertTrue(30 > large.getMinPopulation(), "Large shift has smaller min pop");
        assertEquals(large.getName(), "Standard 3 Shift", "Shift pattern for large");


        var small = config.getShiftByPopulation(8);
        assertTrue(8 >= small.getMinPopulation(), "Small shift has smaller min pop");
        assertEquals(small.getName(), "Standard 2 Shift", "Shift pattern for small");

    }

    @Test
    void testGetActivityByPopulation() {

        var small = config.getActivityByPopulation(8);
        assertTrue(8 > small.minPop(), "Large ruleset has medium min pop");
        assertEquals(small.name(), "Small Settlement", "Activity Ruleset for small");
        assertFalse(small.meetings().isEmpty(), "Small Activity Ruleset has meetings");


        var verysmall = config.getActivityByPopulation(4);
        assertNull(verysmall, "No activities for vry small population");

        var large = config.getActivityByPopulation(30);
        assertTrue(30 > large.minPop(), "Large ruleset has smaller min pop");
        assertEquals(large.name(), "Large Settlement", "Activity Ruleset for large");

        // Large settlement should have night party
        var training = large.meetings().stream().filter(ga -> ga.name().equals(MIGHT_PARTY)).findFirst().get();
        assertNotNull(training, "Movie Night Group Activity found");
        assertEquals(400, training.score(), "Activity Score");
        assertEquals(0.2D, training.percentagePop(), "Activity Pop");
        assertEquals(80, training.waitDuration(), "Activity Wait");
        assertEquals(150, training.activityDuration(), "Activity Duration");
        assertEquals(TaskScope.NONWORK_HOUR, training.scope(), "Activity Scope");
        assertEquals(850, training.calendar().getTimeOfDay(), "Activity Start Time");
        assertEquals(10, training.calendar().getFrequency(), "Activity Freq");
        assertEquals(2, training.calendar().getFirstSol(), "Activity 1st Event");

        var impact = training.impact();
        assertEquals(1, impact.getImpactedSkills().size(), "Activity has skills");
        assertTrue(impact.getImpactedSkills().contains(SkillType.ASTRONOMY), "Activity has correct skill");
    }
}
