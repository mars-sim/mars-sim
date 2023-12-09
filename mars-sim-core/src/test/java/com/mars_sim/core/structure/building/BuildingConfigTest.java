package com.mars_sim.core.structure.building;


import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.structure.building.function.FunctionType;

import junit.framework.TestCase;

public class BuildingConfigTest extends TestCase {

    private static final String LANDER_HAB = "Lander Hab";

    public void testLanderHabFunctions() {
	    var simConfig = SimulationConfig.instance();
        simConfig.loadConfig();
        var bc = simConfig.getBuildingConfiguration();


        BuildingSpec spec = bc.getBuildingSpec(LANDER_HAB);
        assertNotNull("Building spec " + LANDER_HAB, spec);

        // Lander Hab has many functions
        assertEquals("Number of Functions in " + LANDER_HAB, 22, spec.getFunctionSupported().size());
    }

    public void testLanderHabActivitySpots() {
	    var simConfig = SimulationConfig.instance();
        simConfig.loadConfig();
        var bc = simConfig.getBuildingConfiguration();


        BuildingSpec spec = bc.getBuildingSpec(LANDER_HAB);

        // Check names of beds but could check any Function
        FunctionSpec beds = spec.getFunctionSpec(FunctionType.LIVING_ACCOMMODATIONS);
        assertTrue("Has of beds in " + LANDER_HAB, !beds.getActivitySpots().isEmpty());

        for(var b : beds.getActivitySpots()) {
            assertTrue("Bed name", b.name().startsWith("Bed #"));
        }
        Set<String> names = beds.getActivitySpots().stream()
                                .map(NamedPosition::name)
                                .collect(Collectors.toSet());
        assertEquals("Number of unique beds names", beds.getActivitySpots().size(), names.size());
    }

    /**
     * This tets is very tied to the building spec of LANDER_HAB
     */
    public void testLanderHabNamedSpots() {
	    var simConfig = SimulationConfig.instance();
        simConfig.loadConfig();
        var bc = simConfig.getBuildingConfiguration();


        BuildingSpec spec = bc.getBuildingSpec(LANDER_HAB);

        // Check names of exercise stations but could check any Function
        FunctionSpec exercise = spec.getFunctionSpec(FunctionType.EXERCISE);
        assertTrue("Has of exercise in " + LANDER_HAB, !exercise.getActivitySpots().isEmpty());

        Set<String> names = exercise.getActivitySpots().stream()
                                .map(NamedPosition::name)
                                .collect(Collectors.toSet());
        assertTrue("Exercise spot called 'Bike'", names.contains("Bike"));
        assertTrue("Exercise spot called 'Running Machine'", names.contains("Running Machine"));

    }
}
