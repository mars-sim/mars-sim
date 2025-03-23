package com.mars_sim.core.building.function.farming;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.time.MarsTime;

public class CropTest extends AbstractMarsSimUnitTest{
    
	public Building buildGreenhouse(BuildingManager buildingManager) {
		return buildFunction(buildingManager, "Large Greenhouse", BuildingCategory.FARMING,
                FunctionType.FARMING,  LocalPosition.DEFAULT_POSITION, 0D, true);
	}

    public void testGrowingWhiteMustard() {
        testCropGrowth("White Mustard");  // Select a crop with a short growing cycle
    }

    public void testGrowingSpringOnion() {
        testCropGrowth("Spring Onion");  // Select a crop with a short growing cycle
    }
    
    public void testGrowingGreenBellPepper() {
        testCropGrowth("Green Bell Pepper");  // Select a crop with a short growing cycle
    }

    private Crop testCropGrowth(String cropName) {
        var s = buildSettlement("Farm");
        var b = buildGreenhouse(s.getBuildingManager());
        var f = b.getFarming();
        var spec = getConfig().getCropConfiguration().getCropTypeByName(cropName);

        var greyRate = s.getGreyWaterFilteringRate();

        var w = buildRobot("Farmer", s, RobotType.GARDENBOT, b, FunctionType.FARMING);

        var crop = new Crop(1, spec, 2, f, false, 0D);
        var cat = spec.getCropCategory();
        assertEquals("No crop growth", 0D, crop.getPercentGrowth());

        double timePerPulse = 2D;
        var phase = crop.getPhase();
        assertEquals(cropName + " starting phase", cat.getPhases().get(0), phase);

        // Calculate a maximum growing time
        var masterClock = getSim().getMasterClock();
        MarsTime endTime = masterClock.getMarsTime().addTime(1000D * spec.getGrowingSols() * 1.5D);
        var phases = spec.getCropCategory().getPhases();
        int phaseId = 0;

        // Simulate through the phases until Finished
        while(phase.getPhaseType() != PhaseType.FINISHED) {
            double workRequired = crop.getCurrentWorkRequired();

            // Move time until Phase changes
            while(phase.equals(crop.getPhase())) {
                // Check time has not gone too long 
                assertTrue(cropName + " too much time past", endTime.getTimeDiff(masterClock.getMarsTime()) > 0D);

                if (workRequired > 0) {
                    crop.addWork(w, timePerPulse);
                    if (!phase.equals(crop.getPhase())) {
                        // Work has advanced phase so do next phase check
                        break;
                    }

                    workRequired = crop.getCurrentWorkRequired();
                }

                // Move clock onwards
                var pulse = createPulse(timePerPulse);

                crop.timePassing(pulse, 1D, SurfaceFeatures.MAX_SOLAR_IRRADIANCE, greyRate, 1D);
            }
            assertEquals(cropName + " moved to next phase after #" + phaseId, phases.get(phaseId + 1),
                                        crop.getPhase());

            phaseId++;
            phase = crop.getPhase();
        }

        var harvested = crop.getHarvest();
        assertTrue(cropName + " crop harvested over 10%", (harvested.value()/harvested.max()) > 0.5D);
        assertEquals(cropName + " stored", s.getAmountResourceStored(spec.getCropID()), harvested.value(), 0.01);
   
        return crop;
    }
}
