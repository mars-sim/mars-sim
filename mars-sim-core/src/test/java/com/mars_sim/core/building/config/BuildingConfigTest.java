package com.mars_sim.core.building.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.building.ConstructionType;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.manufacture.Tooling;
import com.mars_sim.core.resource.ResourceUtil;


public class BuildingConfigTest {

    private static final String INFLATABLE_GREENHOUSE = "Inflatable Greenhouse";

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


    private SimulationConfig config;

    @BeforeEach
    void setUp() {
        config = SimulationConfig.loadConfig();
    }

    @Test
    void testLanderHabFunctions() {
        var bc = config.getBuildingConfiguration();

        BuildingSpec spec = bc.getBuildingSpec(LANDER_HAB);
        assertNotNull(spec, "Building spec " + LANDER_HAB);

        // Lander Hab has many functions
        assertEquals(21, spec.getFunctionSupported().size(), "Number of Functions in " + LANDER_HAB);
    }

    @Test
    void testLanderHabActivitySpots() {
        var bc = config.getBuildingConfiguration();


        BuildingSpec spec = bc.getBuildingSpec(LANDER_HAB);

        // Check names of beds but could check any Function
        FunctionSpec beds = spec.getFunctionSpec(FunctionType.LIVING_ACCOMMODATION);
        assertTrue(!beds.getActivitySpots().isEmpty(), "Has beds in " + LANDER_HAB);

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
        	assertEquals(beds.getActivitySpots().size(), bedNames.size(), "Number of unique beds names");      
        else
        	assertEquals(beds.getActivitySpots().size(), totalNum, "Number of beds including bunk beds");   
        
        
        Set<String> names = beds.getActivitySpots().stream()
                                .map(NamedPosition::name)
                                .collect(Collectors.toSet());
             
        // Note: Each bunk bed will have 2 activity spots. Thus activity spots' names may NOT be unique 
        
        assertEquals(names.size(), bedNames.keySet().size(), "Number of unique names");
    }

    /**
     * This test is very tied to the building spec of LANDER_HAB
     */
    @Test
    void testLanderHabNamedSpots() {
        var bc = config.getBuildingConfiguration();

        BuildingSpec spec = bc.getBuildingSpec(LANDER_HAB);

        // Check names of exercise stations but could check any Function
        FunctionSpec exercise = spec.getFunctionSpec(FunctionType.EXERCISE);
        assertTrue(!exercise.getActivitySpots().isEmpty(), "Has exercise in " + LANDER_HAB);

        Set<String> names = exercise.getActivitySpots().stream()
                                .map(NamedPosition::name)
                                .collect(Collectors.toSet());
        assertTrue(names.contains("Bike"), "Exercise spot called 'Bike'");
        assertTrue(names.contains("Weight Lifting"), "Exercise spot called 'Running Machine'");
    }

        /**
     * This test is very tied to the building spec of LANDER_HAB
     */
    @Test
    void testGreenhouseScope() {
        var bc = config.getBuildingConfiguration();

        BuildingSpec spec = bc.getBuildingSpec(INFLATABLE_GREENHOUSE);

        var scopes = spec.getSystemScopes();

        assertEquals(4, scopes.size(), "Scope size");
        assertTrue(scopes.contains("building"), "Building scope");
        assertTrue(scopes.contains("habitable"), "Habitable Scope");
        assertTrue(scopes.contains(INFLATABLE_GREENHOUSE), "Type Scope");
        assertTrue(scopes.contains("metallic element"), "Metalic Scope");
    }   

    /**
     * This test is very tied to the building spec of the inflatable greenhouse
     */
    @Test
    void testInflatableGreenhouse() {
        var bc = config.getBuildingConfiguration();

        var found = bc.getBuildingSpec(INFLATABLE_GREENHOUSE);
        
        assertNotNull(found, "Found");

        assertEquals(6D, found.getWidth(), "width");
        assertEquals(9D, found.getLength(), "length");
        assertEquals(6D, found.getWidth(), "width");

        assertEquals(ConstructionType.INFLATABLE, found.getConstruction(), "Construction");

        assertEquals(Set.of(FunctionType.FARMING, FunctionType.LIFE_SUPPORT,
                                         FunctionType.POWER_GENERATION, FunctionType.POWER_STORAGE,
                                         FunctionType.RECREATION, FunctionType.RESEARCH,
                                         FunctionType.RESOURCE_PROCESSING, FunctionType.ROBOTIC_STATION,
                                         FunctionType.STORAGE, FunctionType.THERMAL_GENERATION, 
                                         FunctionType.WASTE_PROCESSING),
                                        found.getFunctionSupported(), "Functions");

        var fSpec = (StorageSpec) found.getFunctionSpec(FunctionType.STORAGE);

        var storage = fSpec.getCapacityResources();
        assertEquals(5000D, storage.get(ResourceUtil.OXYGEN_ID), "Oxygen capacity");
        assertEquals(2500D, storage.get(ResourceUtil.NITROGEN_ID), "Nitrogen capacity");

        var initial = fSpec.getInitialResources();
        assertEquals(100D, initial.get(ResourceUtil.CO2_ID), "Carbon stored");
    }

    
    /**
     * This test is very tied to the building spec of LANDER_HAB
     */
    @Test
    @SuppressWarnings("unchecked")
	void testGarage() {
        var bc = config.getBuildingConfiguration();

        var found = bc.getBuildingSpec("Garage");
        
        assertNotNull(found, "Found");

        assertEquals(12D, found.getWidth(), "width");
        assertEquals(16D, found.getLength(), "length");

        assertEquals(ConstructionType.PRE_FABRICATED, found.getConstruction(), "Construction");

        assertEquals(Set.of(FunctionType.VEHICLE_MAINTENANCE, FunctionType.LIFE_SUPPORT,
                                         FunctionType.POWER_GENERATION, FunctionType.POWER_STORAGE,
                                         FunctionType.ROBOTIC_STATION,
                                         FunctionType.STORAGE, FunctionType.THERMAL_GENERATION),
                                        found.getFunctionSupported(), "Functions");

        VehicleMaintenanceSpec spec = (VehicleMaintenanceSpec) found.getFunctionSpec(FunctionType.VEHICLE_MAINTENANCE);

        assertEquals(2, spec.getFlyerParking().size(), "Flyer parking");
        assertEquals(2, spec.getRoverParking().size(), "Rover parking");
        assertEquals(2, spec.getUtilityParking().size(), "LUV parking");

    }

    /**
     * This test is very tied to the building spec of LANDER_HAB
     */
    @Test
    @SuppressWarnings("unchecked")
	void testResearchFunction() {
        var bc = config.getBuildingConfiguration();

        var found = (ResearchSpec) bc.getFunctionSpec(LANDER_HAB, FunctionType.RESEARCH);
        
        assertNotNull(found, "Found");

        var science = found.getScience();
        assertEquals(8, science.size(), "Science");

        assertEquals(3, found.getTechLevel(), "Research tech");
        assertEquals(3, found.getCapacity(), "Research capacity");
        assertEquals(3, found.getActivitySpots().size(), "Research spots");
    }

    
    /**
     * This test is very tied to the building spec of LANDER_HAB
     */
    @Test
    @SuppressWarnings("unchecked")
	void testThermalFunction() {
        var bc = config.getBuildingConfiguration();

        var found = (GenerationSpec) bc.getFunctionSpec(LANDER_HAB, FunctionType.THERMAL_GENERATION);
        
        assertNotNull(found, "Found");

        var thermals = found.getSources();
        assertEquals(3, thermals.size(), "Sources");

        assertEquals("Solar Heating", thermals.get(0).getType(), "Type 1");
        assertEquals(5D, thermals.get(0).getCapacity(), "Capacity 1");
        
        assertEquals("Electric Heating", thermals.get(1).getType(), "Type 1");
        assertEquals(20D, thermals.get(1).getCapacity(), "Capacity 1");

        assertEquals("Fuel Heating", thermals.get(2).getType(), "Type 2");
        assertEquals(20D, thermals.get(2).getCapacity(), "Capacity 2");
        assertEquals("methane", thermals.get(2).getAttribute(SourceSpec.FUEL_TYPE), "Fuel 2");
    }

    /**
     * This test is very tied to the building spec of LANDER_HAB
     */
    @Test
    @SuppressWarnings("unchecked")
	void testMedicalFunction() {
        var bc = config.getBuildingConfiguration();

        var found = (MedicalCareSpec) bc.getFunctionSpec(LANDER_HAB, FunctionType.MEDICAL_CARE);
        
        assertNotNull(found, "Found");

        var beds = found.getBeds();
        assertEquals(2, beds.size(), "Beds");

        assertEquals(4, found.getTechLevel(), "Medical tech");
        assertEquals(2, found.getActivitySpots().size(), "Medical spots");
    }


    /**
     * This test is very tied to the building spec of LANDER_HAB
     */
    @Test
    @SuppressWarnings("unchecked")
	void testWorkshop() {
        var sc = config;
        var bc = sc.getBuildingConfiguration();

        var found = bc.getBuildingSpec("Workshop");
        
        assertNotNull(found, "Found");

        assertEquals(7D, found.getWidth(), "width");
        assertEquals(9D, found.getLength(), "length");

        assertEquals(ConstructionType.PRE_FABRICATED, found.getConstruction(), "Construction");

        assertEquals(Set.of(FunctionType.COMPUTATION, FunctionType.LIFE_SUPPORT,
                                         FunctionType.MANUFACTURE, FunctionType.POWER_GENERATION,
                                         FunctionType.POWER_STORAGE, FunctionType.ROBOTIC_STATION,
                                         FunctionType.STORAGE, FunctionType.THERMAL_GENERATION),
                                        found.getFunctionSupported(), "Functions");

        
        var fSpec = (StorageSpec) found.getFunctionSpec(FunctionType.STORAGE);
        assertEquals(500D, fSpec.getCapacityResources().get(ResourceUtil.CEMENT_ID), "Cement capacity");

        FunctionSpec manufacture = found.getFunctionSpec(FunctionType.MANUFACTURE);
        Map<Tooling, Integer> tools = (Map<Tooling, Integer>) manufacture.getProperty("tooling");
        assertTrue(tools.size() > 1, "Multiple tools"); // 3D printers, furnace, and lifting

        var furnace = sc.getManufactureConfiguration().getTooling("furnace");
        assertEquals(1, tools.get(furnace).intValue(), "Furnaces");
    }
}
