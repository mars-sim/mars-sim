package com.mars_sim.core.building.config;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.building.ConstructionType;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.manufacture.Tooling;
import com.mars_sim.core.resource.ResourceUtil;


public class BuildingConfigTest extends AbstractMarsSimUnitTest {

    private static final String LANDER_HAB = "Lander Hab";

    private static final String BUNK = "Bunk";

    private static FunctionSpec lifeSupportSpec;
    
    public static FunctionSpec getLifeSupportSpec() {
		if (lifeSupportSpec == null) {
			
			lifeSupportSpec = new FunctionSpec(FunctionType.LIFE_SUPPORT, Map.of(BuildingConfig.POWER_REQUIRED, 1D,
													  FunctionSpec.CAPACITY, 10),
														null);
		}
		return lifeSupportSpec;
	}

    public void testLanderHabFunctions() {
        var bc = simConfig.getBuildingConfiguration();

        BuildingSpec spec = bc.getBuildingSpec(LANDER_HAB);
        assertNotNull("Building spec " + LANDER_HAB, spec);

        // Lander Hab has many functions
        assertEquals("Number of Functions in " + LANDER_HAB, 21, spec.getFunctionSupported().size());
    }

    public void testLanderHabActivitySpots() {
        var bc = simConfig.getBuildingConfiguration();


        BuildingSpec spec = bc.getBuildingSpec(LANDER_HAB);

        // Check names of beds but could check any Function
        FunctionSpec beds = spec.getFunctionSpec(FunctionType.LIVING_ACCOMMODATION);
        assertTrue("Has beds in " + LANDER_HAB, !beds.getActivitySpots().isEmpty());

        Map<String, Integer> bedNames = new HashMap<>();
        for (var b : beds.getActivitySpots()) { 
        	String name = b.name();
        	if (bedNames.containsKey(name)) {
    			int num = bedNames.get(name);
    			bedNames.put(name, ++num);
    		}
    		else {
    			bedNames.put(name, 1);
    		}	
        }
           
        int totalNum = 0;
        
        for (int i: bedNames.values()) {
        	totalNum += i;
        } 
        
        boolean hasBunkBeds = false;
        for (String n: bedNames.keySet()) {
        	if (n.startsWith(BUNK)) {
        		hasBunkBeds = true;
        		break;
        	}
        }
        
        if (!hasBunkBeds)
        	assertEquals("Number of unique beds names", beds.getActivitySpots().size(), bedNames.size());      
        else
        	assertEquals("Number of beds including bunk beds", beds.getActivitySpots().size(), totalNum);   
        
        
        Set<String> names = beds.getActivitySpots().stream()
                                .map(NamedPosition::name)
                                .collect(Collectors.toSet());
             
        // Note: Each bunk bed will have 2 activity spots. Thus activity spots' names may NOT be unique 
        
        assertEquals("Number of unique names", names.size(), bedNames.keySet().size());
    }

    /**
     * This test is very tied to the building spec of LANDER_HAB
     */
    public void testLanderHabNamedSpots() {
        var bc = simConfig.getBuildingConfiguration();

        BuildingSpec spec = bc.getBuildingSpec(LANDER_HAB);

        // Check names of exercise stations but could check any Function
        FunctionSpec exercise = spec.getFunctionSpec(FunctionType.EXERCISE);
        assertTrue("Has exercise in " + LANDER_HAB, !exercise.getActivitySpots().isEmpty());

        Set<String> names = exercise.getActivitySpots().stream()
                                .map(NamedPosition::name)
                                .collect(Collectors.toSet());
        assertTrue("Exercise spot called 'Bike'", names.contains("Bike"));
        assertTrue("Exercise spot called 'Running Machine'", names.contains("Weight Lifting"));
    }

    /**
     * This test is very tied to the building spec of the inflatable greenhouse
     */
    public void testInflatableGreenhouse() {
	    var simConfig = SimulationConfig.loadConfig();
        var bc = simConfig.getBuildingConfiguration();

        var found = bc.getBuildingSpec("Inflatable Greenhouse");
        
        assertNotNull("Found", found);

        assertEquals("width", 6D, found.getWidth());
        assertEquals("length", 9D, found.getLength());
        assertEquals("width", 6D, found.getWidth());

        assertEquals("Construction", ConstructionType.INFLATABLE, found.getConstruction());

        assertEquals("Functions", Set.of(FunctionType.FARMING, FunctionType.LIFE_SUPPORT,
                                         FunctionType.POWER_GENERATION, FunctionType.POWER_STORAGE,
                                         FunctionType.RECREATION, FunctionType.RESEARCH,
                                         FunctionType.RESOURCE_PROCESSING, FunctionType.ROBOTIC_STATION,
                                         FunctionType.STORAGE, FunctionType.THERMAL_GENERATION, 
                                         FunctionType.WASTE_PROCESSING),
                                        found.getFunctionSupported());

        
        var storage = found.getStorage();
        assertEquals("Oxygen capacity", 5000D, storage.get(ResourceUtil.OXYGEN_ID));
        assertEquals("Nitrogen capacity", 2500D, storage.get(ResourceUtil.NITROGEN_ID));

        var initial = found.getInitialResources();
        assertEquals("Carbon stored", 100D, initial.get(ResourceUtil.CO2_ID));
    }

    
    /**
     * This test is very tied to the building spec of LANDER_HAB
     */
    @SuppressWarnings("unchecked")
	public void testGarage() {
	    var simConfig = SimulationConfig.loadConfig();
        var bc = simConfig.getBuildingConfiguration();

        var found = bc.getBuildingSpec("Garage");
        
        assertNotNull("Found", found);

        assertEquals("width", 12D, found.getWidth());
        assertEquals("length", 16D, found.getLength());

        assertEquals("Construction", ConstructionType.PRE_FABRICATED, found.getConstruction());

        assertEquals("Functions", Set.of(FunctionType.VEHICLE_MAINTENANCE, FunctionType.LIFE_SUPPORT,
                                         FunctionType.POWER_GENERATION, FunctionType.POWER_STORAGE,
                                         FunctionType.ROBOTIC_STATION,
                                         FunctionType.STORAGE, FunctionType.THERMAL_GENERATION),
                                        found.getFunctionSupported());

        VehicleMaintenanceSpec spec = (VehicleMaintenanceSpec) found.getFunctionSpec(FunctionType.VEHICLE_MAINTENANCE);

        assertEquals("Flyer parking", 2, spec.getFlyerParking().size());
        assertEquals("Rover parking", 2, spec.getRoverParking().size());
        assertEquals("LUV parking", 2, spec.getUtilityParking().size());

    }

    /**
     * This test is very tied to the building spec of LANDER_HAB
     */
    @SuppressWarnings("unchecked")
	public void testResearchFunction() {
	    var simConfig = SimulationConfig.loadConfig();
        var bc = simConfig.getBuildingConfiguration();

        var found = (ResearchSpec) bc.getFunctionSpec(LANDER_HAB, FunctionType.RESEARCH);
        
        assertNotNull("Found", found);

        var science = found.getScience();
        assertEquals("Science", 8, science.size());

        assertEquals("Research tech", 3, found.getTechLevel());
        assertEquals("Research capacity", 3, found.getCapacity());
        assertEquals("Research spots", 3, found.getActivitySpots().size());
    }


    /**
     * This test is very tied to the building spec of LANDER_HAB
     */
    @SuppressWarnings("unchecked")
	public void testMedicalFunction() {
	    var simConfig = SimulationConfig.loadConfig();
        var bc = simConfig.getBuildingConfiguration();

        var found = (MedicalCareSpec) bc.getFunctionSpec(LANDER_HAB, FunctionType.MEDICAL_CARE);
        
        assertNotNull("Found", found);

        var beds = found.getBeds();
        assertEquals("Beds", 2, beds.size());

        assertEquals("Medical tech", 4, found.getTechLevel());
        assertEquals("Medical spots", 2, found.getActivitySpots().size());
    }


    /**
     * This test is very tied to the building spec of LANDER_HAB
     */
    @SuppressWarnings("unchecked")
	public void testWorkshop() {
	    var simConfig = SimulationConfig.loadConfig();
        var bc = simConfig.getBuildingConfiguration();

        var found = bc.getBuildingSpec("Workshop");
        
        assertNotNull("Found", found);

        assertEquals("width", 7D, found.getWidth());
        assertEquals("length", 9D, found.getLength());

        assertEquals("Construction", ConstructionType.PRE_FABRICATED, found.getConstruction());

        assertEquals("Functions", Set.of(FunctionType.COMPUTATION, FunctionType.LIFE_SUPPORT,
                                         FunctionType.MANUFACTURE, FunctionType.POWER_GENERATION,
                                         FunctionType.POWER_STORAGE, FunctionType.ROBOTIC_STATION,
                                         FunctionType.STORAGE, FunctionType.THERMAL_GENERATION),
                                        found.getFunctionSupported());

        
        var storage = found.getStorage();
        assertEquals("Cement capacity", 500D, storage.get(ResourceUtil.CEMENT_ID));

        FunctionSpec manufacture = found.getFunctionSpec(FunctionType.MANUFACTURE);
        Map<Tooling, Integer> tools = (Map<Tooling, Integer>) manufacture.getProperty("tooling");
        assertTrue("Multiple tools", tools.size() > 1); // 3D printers, furnace, and lifting

        var furnace = simConfig.getManufactureConfiguration().getTooling("furnace");
        assertEquals("Furnaces", 1, tools.get(furnace).intValue());
    }
}
