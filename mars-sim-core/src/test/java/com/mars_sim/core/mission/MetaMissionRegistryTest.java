package com.mars_sim.core.mission;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.MissionLimitParameters;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.test.MarsSimUnitTest;

class MetaMissionRegistryTest extends MarsSimUnitTest{
    @Test
    void testGetAutomaticMetaMissionsProbability() {
        var settlement = buildSettlement("Test Settlement", true, 10);
        
        // Disable SolThreshold on missions
        settlement.getPreferences().putValue(MissionLimitParameters.MISSION_CHECK_SOL, false);
        
        for(int i = 0; i < 30; i++) {
            buildPerson("Test " + i, settlement);
        }

        var person = buildPerson("Test Person", settlement, RoleType.MISSION_SPECIALIST, JobType.PILOT);
        buildRover(settlement, "Test Rover", LocalPosition.DEFAULT_POSITION, TRANSPORT_ROVER);

        var metaMissions = MetaMissionRegistry.getAutomaticMetaMissions();
        assertFalse(metaMissions.isEmpty(), "There should be at least one automatic meta mission");

        for (var metaMission : metaMissions) {
            var probability = assertDoesNotThrow(() -> metaMission.getProbability(person),
                            () -> "getProbability should not throw for " + metaMission.getName());
            assertNotNull(probability, "Probability should not be null for " + metaMission.getName());
        }
    }
}
