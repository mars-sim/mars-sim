package com.mars_sim.core.building;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.map.location.BoundedObject;

public class BuildingCategoryTest extends MarsSimUnitTest {
    private static final BoundedObject BOUNDS = new BoundedObject(0, 0, 20, 10, 0D);

    @Test
    public void testGarage() {
        assertBuildingType("Garage", BuildingCategory.VEHICLE);
    }

    @Test
    public void testLargeGreenhouse() {
        assertBuildingType("Large Greenhouse", BuildingCategory.FARMING);
    }

    @Test
    public void testResearch() {
        assertBuildingType("Research Hab", BuildingCategory.LABORATORY);
    }
    

    @Test
    public void testHallway() {
        assertBuildingType("Hallway", BuildingCategory.CONNECTION);
    }

    @Test
    public void testInfirmary() {
        assertBuildingType("Infirmary", BuildingCategory.MEDICAL);
    }
   
    @Test
    public void testCommand() {
        assertBuildingType("Command Center", BuildingCategory.COMMAND);
    }
    
    @Test
    public void testQuarters() {
        assertBuildingType("Residential Quarters", BuildingCategory.LIVING);
    }
    
    private void assertBuildingType(String buildingType, BuildingCategory expected) {
                
        var s = buildSettlement("mock");
        var spec = getConfig().getBuildingConfiguration().getBuildingSpec(buildingType);

        var b = new Building(s, "1", 1, spec.getName(), BOUNDS, spec);
        assertEquals(expected, b.getCategory(), buildingType);
    }
}
