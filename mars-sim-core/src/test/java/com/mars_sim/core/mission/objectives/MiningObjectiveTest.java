package com.mars_sim.core.mission.objectives;
import static com.mars_sim.core.test.SimulationAssertions.assertLessThan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.Collections;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.vehicle.LightUtilityVehicle;

public class MiningObjectiveTest extends MarsSimUnitTest {
    
    @Test
    public void testExtractedMineral() {
        var sf = getSim().getSurfaceFeatures();

        // Use a huge range for search to guarantee a site
        var potentials = sf.getMineralMap().findRandomMineralLocation(new Coordinates(Math.PI, 0),
                    100000, Collections.emptyList());

        // Give big skill to handle small conc
        var site = sf.declareROI(potentials.getKey(), 10);

        LightUtilityVehicle l = null;
		var obj = new MiningObjective(l, site);

        var mins = obj.getMineralStats();
        assertEquals(site.getMinerals().size(), mins.size(), "Minerals detected");

        // Select a mineral
        int targetId = (new ArrayList<>(site.getEstimatedMineralAmounts().keySet())).get(0);
        var initialMass = site.getRemainingMass();
        var initialMineral = site.getEstimatedMineralAmounts().get(targetId);

        // Extract some mineral
        var amount = initialMineral/2;
        obj.extractedMineral(targetId, amount);

        assertLessThan("Site reduced mass", initialMass, site.getRemainingMass());

        var minDetails = obj.getMineralStats().get(targetId);
        assertEquals(amount, minDetails.getExtracted(), "Mineral extracted");
        assertEquals(amount, minDetails.getAvailable(), "Mineral available");

        // Collect some
        amount /= 2D;
        obj.recordResourceCollected(targetId, amount);
        assertEquals(amount, minDetails.getAvailable(), "Mineral available after collection");
        assertEquals(amount, minDetails.getCollected(), "Mineral collected");

    }
}
