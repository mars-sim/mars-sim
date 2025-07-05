package com.mars_sim.core.mission.objectives;


import java.util.ArrayList;
import java.util.Collections;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.vehicle.LightUtilityVehicle;

public class MiningObjectiveTest extends AbstractMarsSimUnitTest {
    
    public void testExtractedMineral() {
        var s = buildSettlement();
        var sf = getSim().getSurfaceFeatures();

        // Use a huge range for search to guarantee a site
        var potentials = sf.getMineralMap().findRandomMineralLocation(new Coordinates("0N", "0E"),
                    100000, Collections.emptyList());

        // Give big skill to handle small conc
        var site = sf.declareRegionOfInterest(potentials.getKey(), 10, s);

        LightUtilityVehicle l = null;
		var obj = new MiningObjective(l, site);

        var mins = obj.getMineralStats();
        assertEquals("Minerals detected", site.getEstimatedMineralConcentrations().size(), mins.size());

        // Select a mineral
        int targetId = (new ArrayList<>(site.getEstimatedMineralAmounts().keySet())).get(0);
        var initialMass = site.getRemainingMass();
        var initialMineral = site.getEstimatedMineralAmounts().get(targetId);

        // Extract some mineral
        var amount = initialMineral/2;
        obj.extractedMineral(targetId, amount);

        assertLessThan("Site reduced mass", initialMass, site.getRemainingMass());

        var minDetails = obj.getMineralStats().get(targetId);
        assertEquals("Mineral extracted", amount, minDetails.getExtracted());
        assertEquals("Mineral available", amount, minDetails.getAvailable());

        // Collect some
        amount /= 2D;
        obj.recordResourceCollected(targetId, amount);
        assertEquals("Mineral available after collection", amount, minDetails.getAvailable());
        assertEquals("Mineral collected", amount, minDetails.getCollected());

    }
}
