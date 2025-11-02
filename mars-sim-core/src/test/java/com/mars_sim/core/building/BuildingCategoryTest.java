package com.mars_sim.core.building;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.map.location.BoundedObject;

public class BuildingCategoryTest extends AbstractMarsSimUnitTest {
    private static final BoundedObject BOUNDS = new BoundedObject(0, 0, 20, 10, 0D);

    public void testGarage() {
        assertBuildingType("Garage", BuildingCategory.VEHICLE);
    }

    public void testLargeGreenhouse() {
        assertBuildingType("Large Greenhouse", BuildingCategory.FARMING);
    }

    public void testResearch() {
        assertBuildingType("Research Hab", BuildingCategory.LABORATORY);
    }
    

    public void testHallway() {
        assertBuildingType("Hallway", BuildingCategory.CONNECTION);
    }

    public void testInfirmary() {
        assertBuildingType("Infirmary", BuildingCategory.MEDICAL);
    }
   
    public void testCommand() {
        assertBuildingType("Command Center", BuildingCategory.COMMAND);
    }
    
    public void testQuarters() {
        assertBuildingType("Residential Quarters", BuildingCategory.LIVING);
    }
    
    private void assertBuildingType(String buildingType, BuildingCategory expected) {
                
        var s = buildSettlement();
        var spec = getConfig().getBuildingConfiguration().getBuildingSpec(buildingType);

        var b = new Building(s, "1", 1, spec.getName(), BOUNDS, spec);
        assertEquals(buildingType, expected, b.getCategory());
    }
}
