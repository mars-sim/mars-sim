/*
 * Mars Simulation Project
 * WalkingStepsTest.java
 * @date 2024-07-10
 * @author Scott Davis
 */

package com.mars_sim.core.person.ai.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.connection.BuildingConnector;
import com.mars_sim.core.building.connection.BuildingConnectorManager;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.WalkingSteps.WalkStep;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Rover;

/**
 * A unit test suite for the WalkingSteps task class.
 */
public class WalkingStepsTest extends AbstractMarsSimUnitTest {
	

	private static final LocalPosition LOCAL_POSITION2 = new LocalPosition(-7.5D, 0D);
	private static final LocalPosition LOCAL_POSITION1 = new LocalPosition(-4.5D, 0D);
	
	/**
     * Test constructing walking steps from building interior to building interior with a
     * valid walking path between them.
     */
    public void testWalkingStepsBuildingToBuildingPath() {

        Settlement settlement = buildSettlement();
		
        BuildingManager buildingManager = settlement.getBuildingManager();
        BuildingConnectorManager connectorManager = settlement.getBuildingConnectorManager();
        assertNotNull(connectorManager);

        Building building0 = buildAccommodation(buildingManager, LocalPosition.DEFAULT_POSITION, 0D, 0);

        Building building1 = buildBuilding(buildingManager, new LocalPosition(-12D, 0D), 270D, 1);

        Building building2 = buildBuilding(buildingManager, new LocalPosition(-6D, 6D), 270D, 2);

        connectorManager.addBuildingConnection(new BuildingConnector(building0,
                LOCAL_POSITION1, 90D, building2, LOCAL_POSITION1, 270D));
        connectorManager.addBuildingConnection(new BuildingConnector(building1,
                LOCAL_POSITION2, 270D, building2, LOCAL_POSITION2, 90D));

        buildingManager.setupBuildingFunctionsMap();

		Person person = buildPerson("Walker", settlement);		
        BuildingManager.addToBuilding(person, building0);

        LocalPosition target = new LocalPosition(-6D, 0.5D);
        WalkingSteps walkingSteps = new WalkingSteps(person, target, building2);
        assertNotNull(walkingSteps);
        assertTrue(walkingSteps.canWalkAllSteps());
        assertNotNull(walkingSteps.getWalkingStepsList());
        assertEquals(1, walkingSteps.getWalkingStepsNumber());

        WalkStep walkStep1 = walkingSteps.getWalkingStepsList().get(0);

        assertEquals(WalkStep.SETTLEMENT_INTERIOR_WALK, walkStep1.stepType);
        assertEquals(target, walkStep1.loc);
    }

	/**
     * Test constructing walking steps from EVA building interior to building interior with no
     * valid walking path between them and no airlocks.
     */
    public void testWalkingStepsEVABuildingToBuildingNoPath() {

        Settlement settlement = buildSettlement();

        BuildingManager buildingManager = settlement.getBuildingManager();

        LocalPosition target = new LocalPosition(-12D, 0D);
        Building building0 = buildEVA(buildingManager, LOCAL_POSITION1, 0D, 0);
        Building building1 = buildBuilding(buildingManager, target, 270D, 1);

        buildingManager.setupBuildingFunctionsMap();

		Person person = buildPerson("Walker", settlement);

        BuildingManager.addToBuilding(person, building0);
   
        WalkingSteps walkingSteps = new WalkingSteps(person, target, building1);
        assertNotNull(walkingSteps);
        
        assertFalse(walkingSteps.canWalkAllSteps());

        assertNotNull(walkingSteps.getWalkingStepsList());

        assertEquals(2, walkingSteps.getWalkingStepsNumber()); 

        assertEquals(2, walkingSteps.getWalkingStepsList().size()); 
    }
    
	/**
     * Test constructing walking steps from EVA building interior to building interior with no
     * valid walking path between them.
     */
    public void testWalkingStepsBuildingToEVABuildingNoPath() {

        Settlement settlement = buildSettlement();

        BuildingManager buildingManager = settlement.getBuildingManager();

        LocalPosition target = new LocalPosition(-12D, 0D);
        Building building0 = buildAccommodation(buildingManager, LOCAL_POSITION1, 0D, 0); 
        Building building1 = buildEVA(buildingManager, target, 270D, 1); 

        buildingManager.setupBuildingFunctionsMap();

		Person person = buildPerson("Walker", settlement);

        BuildingManager.addToBuilding(person, building0);
        assertNotNull("Person's starting building", person.getBuildingLocation());

        WalkingSteps walkingSteps = new WalkingSteps(person, target, building1);
        assertNotNull(walkingSteps);

        boolean canWalk = walkingSteps.canWalkAllSteps();
        
        assertFalse("No walking path found", canWalk);

        assertNotNull(walkingSteps.getWalkingStepsList());
        
        int steps = walkingSteps.getWalkingStepsNumber();
        
        assertEquals(0, steps); 

        assertEquals(0, walkingSteps.getWalkingStepsList().size()); 
    }
 
	/**
     * Test constructing walking steps from building interior to building interior with no
     * valid walking path between them and no airlocks.
     */
    public void testWalkingStepsBuildingToBuildingNoPath() {

        Settlement settlement = buildSettlement();

        BuildingManager buildingManager = settlement.getBuildingManager();

        LocalPosition target = new LocalPosition(-12D, 0D);
        Building building0 = buildAccommodation(buildingManager, LOCAL_POSITION1, 0D, 0);
        Building building1 = buildBuilding(buildingManager, new LocalPosition(BUILDING_LENGTH + 1, 0D), 0D, 1);

        assertFalse("Target is not in the target building", LocalAreaUtil.isPositionWithinLocalBoundedObject(target, building1));
        buildingManager.setupBuildingFunctionsMap();

		Person person = buildPerson("Walker", settlement);

        BuildingManager.addToBuilding(person, building0);
        assertNotNull("Person is in building", person.getBuildingLocation());

        WalkingSteps walkingSteps = new WalkingSteps(person, target, building1);

        assertFalse(walkingSteps.canWalkAllSteps());

        assertNotNull(walkingSteps.getWalkingStepsList());

        int steps = walkingSteps.getWalkingStepsNumber();
        assertEquals(0, steps); 

        assertEquals(0, walkingSteps.getWalkingStepsList().size()); 
    }
    

    /**
     * Test constructing walking steps from building interior to building interior with no
     * valid walking path between them and airlocks.
     */
    public void testWalkingStepsBuildingToBuildingNoPathAirlocks() {

        Settlement settlement = buildSettlement();
	
        BuildingManager buildingManager = settlement.getBuildingManager();

        Building building0 = buildEVA(buildingManager, LocalPosition.DEFAULT_POSITION, 0D, 0);
        var target = new LocalPosition(-12D, 0D);
        Building building1 = buildEVA(buildingManager, target, 270D, 1);
        buildingManager.setupBuildingFunctionsMap();

		Person person = buildPerson("Walker", settlement);

        BuildingManager.addToBuilding(person, building0);
        assertNotNull("Person in start building", person.getBuildingLocation());

        WalkingSteps walkingSteps = new WalkingSteps(person, target, building1);

        assertTrue("Found a walking path", walkingSteps.canWalkAllSteps());

        assertNotNull("Has a walking path", walkingSteps.getWalkingStepsList());

        assertEquals("Walking path steps", 5, walkingSteps.getWalkingStepsNumber());

        assertEquals("Path size", 5, walkingSteps.getWalkingStepsList().size());

        WalkStep walkStep1 = walkingSteps.getWalkingStepsList().get(0);

        assertEquals(WalkStep.SETTLEMENT_INTERIOR_WALK, walkStep1.stepType);

        WalkStep walkStep2 = walkingSteps.getWalkingStepsList().get(1);

        assertEquals(WalkStep.EXIT_AIRLOCK, walkStep2.stepType);

        WalkStep walkStep3 = walkingSteps.getWalkingStepsList().get(2);

        assertEquals(WalkStep.EXTERIOR_WALK, walkStep3.stepType);

        WalkStep walkStep4 = walkingSteps.getWalkingStepsList().get(3);

        assertEquals(WalkStep.ENTER_AIRLOCK, walkStep4.stepType);

        WalkStep walkStep5 = walkingSteps.getWalkingStepsList().get(4);

        assertEquals(WalkStep.SETTLEMENT_INTERIOR_WALK, walkStep5.stepType);
    }

    /**
     * Test constructing walking steps from building interior to exterior with an airlock.
     */
    public void testWalkingStepsBuildingToExteriorAirlock() {

        Settlement settlement = buildSettlement();
		
        BuildingManager buildingManager = settlement.getBuildingManager();

        Building building0 = buildEVA(buildingManager, LocalPosition.DEFAULT_POSITION, 0D, 0);

        buildingManager.setupBuildingFunctionsMap();
        
		Person person = buildPerson("Walker", settlement);

        BuildingManager.addToBuilding(person, building0);

        LocalPosition target = new LocalPosition(10D, 15D);
        WalkingSteps walkingSteps = new WalkingSteps(person, target, null);
        assertNotNull(walkingSteps);

        assertTrue(walkingSteps.canWalkAllSteps()); // junit.framework.AssertionFailedError

        assertNotNull(walkingSteps.getWalkingStepsList());

        assertEquals(3, walkingSteps.getWalkingStepsNumber());

        assertEquals(3, walkingSteps.getWalkingStepsList().size());

        WalkStep walkStep1 = walkingSteps.getWalkingStepsList().get(0);

        assertEquals(WalkStep.SETTLEMENT_INTERIOR_WALK, walkStep1.stepType);

        WalkStep walkStep2 = walkingSteps.getWalkingStepsList().get(1);

        assertEquals(WalkStep.EXIT_AIRLOCK, walkStep2.stepType);

        WalkStep walkStep3 = walkingSteps.getWalkingStepsList().get(2);

        assertEquals(WalkStep.EXTERIOR_WALK, walkStep3.stepType);

        assertEquals(target, walkStep3.loc);
    }

    /**
     * Test constructing walking steps from building interior to exterior with no airlock.
     */
    public void testWalkingStepsBuildingToExteriorNoAirlock() {

        Settlement settlement = buildSettlement();
		
        BuildingManager buildingManager = settlement.getBuildingManager();

        Building building0 = buildAccommodation(buildingManager, LocalPosition.DEFAULT_POSITION, 0D, 0);

        buildingManager.setupBuildingFunctionsMap();

		Person person = buildPerson("Walker", settlement);

		
        BuildingManager.addToBuilding(person, building0);

        WalkingSteps walkingSteps = new WalkingSteps(person, new LocalPosition(10D, 15D), null);

        assertNotNull(walkingSteps);

        assertFalse(walkingSteps.canWalkAllSteps());

        assertNotNull(walkingSteps.getWalkingStepsList());

        assertEquals(0, walkingSteps.getWalkingStepsNumber());

        assertEquals(0, walkingSteps.getWalkingStepsList().size());
    }

    /**
     * Test constructing walking steps from a rover to exterior.
     */
    public void testWalkingStepsRoverToExterior() {

        Settlement settlement = buildSettlement();

        BuildingManager buildingManager = settlement.getBuildingManager();

        Building building0 = buildEVA(buildingManager, LocalPosition.DEFAULT_POSITION, 0D, 0);

        buildingManager.setupBuildingFunctionsMap();

		Person person = buildPerson("Walker", settlement);

        BuildingManager.addToBuilding(person, building0);

        Rover rover = buildRover(settlement, "Test Rover", new LocalPosition(15D, -10D));

        person.transfer(rover);

        LocalPosition target = new LocalPosition(20D, 15D);
        WalkingSteps walkingSteps = new WalkingSteps(person, target, null);
        assertNotNull(walkingSteps);

        assertTrue(walkingSteps.canWalkAllSteps());

        assertNotNull(walkingSteps.getWalkingStepsList());

        assertEquals(3, walkingSteps.getWalkingStepsNumber());

        assertEquals(3, walkingSteps.getWalkingStepsList().size());

        WalkStep walkStep1 = walkingSteps.getWalkingStepsList().get(0);

        assertEquals(WalkStep.ROVER_INTERIOR_WALK, walkStep1.stepType);

        WalkStep walkStep2 = walkingSteps.getWalkingStepsList().get(1);

        assertEquals(WalkStep.EXIT_AIRLOCK, walkStep2.stepType);

        WalkStep walkStep3 = walkingSteps.getWalkingStepsList().get(2);

        assertEquals(WalkStep.EXTERIOR_WALK, walkStep3.stepType);

        assertEquals(target, walkStep3.loc);
    }

    /**
     * Test constructing walking steps from a rover to a building.
     */
    public void testWalkingStepsRoverToBuilding() {

        Settlement settlement = buildSettlement();
		
        BuildingManager buildingManager = settlement.getBuildingManager();
                
        Rover rover = buildRover(settlement, "Test Rover", new LocalPosition(15D, -10D));
        
        Building building0 = buildEVA(buildingManager, LocalPosition.DEFAULT_POSITION, 0D, 0);

        buildingManager.setupBuildingFunctionsMap();

		Person person = buildPerson("Walker", settlement);

        person.transfer(rover);

        WalkingSteps walkingSteps = new WalkingSteps(person, LocalPosition.DEFAULT_POSITION, building0);
        assertNotNull(walkingSteps);

        assertTrue(walkingSteps.canWalkAllSteps());

        assertNotNull(walkingSteps.getWalkingStepsList());

        assertEquals(5, walkingSteps.getWalkingStepsNumber()); 

        assertEquals(5, walkingSteps.getWalkingStepsList().size());

        WalkStep walkStep1 = walkingSteps.getWalkingStepsList().get(0);

        assertEquals(WalkStep.ROVER_INTERIOR_WALK, walkStep1.stepType);

        WalkStep walkStep2 = walkingSteps.getWalkingStepsList().get(1);

        assertEquals(WalkStep.EXIT_AIRLOCK, walkStep2.stepType);

        WalkStep walkStep3 = walkingSteps.getWalkingStepsList().get(2);

        assertEquals(WalkStep.EXTERIOR_WALK, walkStep3.stepType);

        WalkStep walkStep4 = walkingSteps.getWalkingStepsList().get(3);

        assertEquals(WalkStep.ENTER_AIRLOCK, walkStep4.stepType);

        WalkStep walkStep5 = walkingSteps.getWalkingStepsList().get(4);

        assertEquals(WalkStep.SETTLEMENT_INTERIOR_WALK, walkStep5.stepType);
    }

    /**
     * Test constructing walking steps from a building to a rover.
     */
    public void testWalkingStepsBuildingToRover() {

        Settlement settlement = buildSettlement();
		
        BuildingManager buildingManager = settlement.getBuildingManager();

        LocalPosition parked = new LocalPosition(15D, -10D);
        Rover rover = buildRover(settlement, "Test Rover", parked);
        
        Building building0 = buildEVA(buildingManager, LocalPosition.DEFAULT_POSITION, 0D, 0);
        buildingManager.setupBuildingFunctionsMap();

		Person person = buildPerson("Walker", settlement);

        BuildingManager.addToBuilding(person, building0);

        WalkingSteps walkingSteps = new WalkingSteps(person, parked, rover);
        
        assertNotNull(walkingSteps);

        assertTrue(walkingSteps.canWalkAllSteps()); // junit.framework.AssertionFailedError

        assertNotNull(walkingSteps.getWalkingStepsList());

        assertEquals(5, walkingSteps.getWalkingStepsNumber());

        assertEquals(5, walkingSteps.getWalkingStepsList().size());

        WalkStep walkStep1 = walkingSteps.getWalkingStepsList().get(0);

        assertEquals(WalkStep.SETTLEMENT_INTERIOR_WALK, walkStep1.stepType);

        WalkStep walkStep2 = walkingSteps.getWalkingStepsList().get(1);

        assertEquals(WalkStep.EXIT_AIRLOCK, walkStep2.stepType);

        WalkStep walkStep3 = walkingSteps.getWalkingStepsList().get(2);

        assertEquals(WalkStep.EXTERIOR_WALK, walkStep3.stepType);

        WalkStep walkStep4 = walkingSteps.getWalkingStepsList().get(3);

        assertEquals(WalkStep.ENTER_AIRLOCK, walkStep4.stepType);

        WalkStep walkStep5 = walkingSteps.getWalkingStepsList().get(4);

        assertEquals(WalkStep.ROVER_INTERIOR_WALK, walkStep5.stepType);
    }

    /**
     * Test constructing walking steps from a building to a rover with no building airlock.
     */
    public void testWalkingStepsBuildingToRoverNoAirlock() {

        Settlement settlement = buildSettlement();

        BuildingManager buildingManager = settlement.getBuildingManager();

        LocalPosition parked = new LocalPosition(15D, -10D);
        Rover rover = buildRover(settlement, "Test Rover", parked);

        Building building0 = buildAccommodation(buildingManager, LocalPosition.DEFAULT_POSITION, 0D, 0);
        buildingManager.setupBuildingFunctionsMap();
        
		Person person = buildPerson("Walker", settlement);

        BuildingManager.addToBuilding(person, building0);

        WalkingSteps walkingSteps = new WalkingSteps(person, parked, rover);
        
        assertNotNull(walkingSteps);

        assertFalse(walkingSteps.canWalkAllSteps());

        assertNotNull(walkingSteps.getWalkingStepsList());

        assertEquals(0, walkingSteps.getWalkingStepsNumber());

        assertEquals(0, walkingSteps.getWalkingStepsList().size());
    }

    /**
     * Test constructing walking steps from a rover to a rover.
     */
    public void testWalkingStepsRoverToRover() {

        Settlement settlement = buildSettlement();
		
		LocalPosition parked1 = new LocalPosition(15D, -10D);
		Rover rover1 = buildRover(settlement, "test Rover 1", parked1);

		LocalPosition parked2 = new LocalPosition(-50D, 20D);
        Rover rover2 = buildRover(settlement, "Test Rover 2", parked2);
        
		Person person = buildPerson("Walker", settlement);

		person.transfer(rover1);
		
        WalkingSteps walkingSteps = new WalkingSteps(person, parked2, rover2);
        
        assertNotNull(walkingSteps);

        assertTrue(walkingSteps.canWalkAllSteps());

        assertNotNull(walkingSteps.getWalkingStepsList());

        assertEquals(5, walkingSteps.getWalkingStepsNumber());

        assertEquals(5, walkingSteps.getWalkingStepsList().size());

        WalkStep walkStep1 = walkingSteps.getWalkingStepsList().get(0);

        assertEquals(WalkStep.ROVER_INTERIOR_WALK, walkStep1.stepType);

        WalkStep walkStep2 = walkingSteps.getWalkingStepsList().get(1);

        assertEquals(WalkStep.EXIT_AIRLOCK, walkStep2.stepType);

        WalkStep walkStep3 = walkingSteps.getWalkingStepsList().get(2);

        assertEquals(WalkStep.EXTERIOR_WALK, walkStep3.stepType);

        WalkStep walkStep4 = walkingSteps.getWalkingStepsList().get(3);

        assertEquals(WalkStep.ENTER_AIRLOCK, walkStep4.stepType);

        WalkStep walkStep5 = walkingSteps.getWalkingStepsList().get(4);

        assertEquals(WalkStep.ROVER_INTERIOR_WALK, walkStep5.stepType);
    }

    /**
     * Test constructing walking steps from a building to a rover in a garage.
     */
    public void testWalkingStepsBuildingToRoverInGarage() {

        Settlement settlement = buildSettlement();
		
        BuildingManager buildingManager = settlement.getBuildingManager();

        Rover rover = buildRover(settlement, "Test Rover", LocalPosition.DEFAULT_POSITION);
        var garage = buildGarage(buildingManager, LocalPosition.DEFAULT_POSITION, 0D,  0);
        garage.addRover(rover);

        buildingManager.setupBuildingFunctionsMap();

		Person person = buildPerson("Walker", settlement);

        person.setPosition(new LocalPosition(4D, 4D));
        BuildingManager.addToBuilding(person, garage.getBuilding());

        WalkingSteps walkingSteps = new WalkingSteps(person, LocalPosition.DEFAULT_POSITION, rover);
        
        assertNotNull(walkingSteps);

        assertTrue(walkingSteps.canWalkAllSteps()); 

        assertNotNull(walkingSteps.getWalkingStepsList());

        assertEquals(2, walkingSteps.getWalkingStepsNumber());

        assertEquals(2, walkingSteps.getWalkingStepsList().size());

        WalkStep walkStep1 = walkingSteps.getWalkingStepsList().get(0);

        assertEquals(WalkStep.SETTLEMENT_INTERIOR_WALK, walkStep1.stepType);

        WalkStep walkStep2 = walkingSteps.getWalkingStepsList().get(1);

        assertEquals(WalkStep.ENTER_GARAGE_ROVER, walkStep2.stepType);

        assertEquals(LocalPosition.DEFAULT_POSITION, walkStep2.loc);
    }

    /**
     * Test constructing walking steps from a rover in a garage to a building.
     */
    public void testWalkingStepsRoverToBuildingInGarage() {
        
        Settlement settlement = buildSettlement();
		
        BuildingManager buildingManager = settlement.getBuildingManager();

        Rover rover = buildRover(settlement, "Test Rover", LocalPosition.DEFAULT_POSITION);
        rover.transfer(settlement);
        
        var garage = buildGarage(buildingManager, LocalPosition.DEFAULT_POSITION, 0D, 0);
        garage.addRover(rover);
        buildingManager.setupBuildingFunctionsMap();
        
		Person person = buildPerson("Walker", settlement);

        person.setPosition(LocalPosition.DEFAULT_POSITION);
        person.transfer(rover);
        
        LocalPosition target = new LocalPosition(4D, 4D);
        WalkingSteps walkingSteps = new WalkingSteps(person, target, garage.getBuilding());
        
        assertNotNull(walkingSteps);
        
        assertTrue(walkingSteps.canWalkAllSteps()); 

        assertNotNull(walkingSteps.getWalkingStepsList());

        assertEquals(2, walkingSteps.getWalkingStepsNumber());

        assertEquals(2, walkingSteps.getWalkingStepsList().size());

        WalkStep walkStep1 = walkingSteps.getWalkingStepsList().get(0);

        assertEquals(WalkStep.EXIT_GARAGE_ROVER, walkStep1.stepType); // junit.framework.AssertionFailedError: expected:<6> but was:<1>

        WalkStep walkStep2 = walkingSteps.getWalkingStepsList().get(1);

        assertEquals(WalkStep.SETTLEMENT_INTERIOR_WALK, walkStep2.stepType);

        assertEquals(target, walkStep2.loc);
    }

    /**
     * Test constructing walking steps from exterior to building interior with an airlock.
     */
    public void testWalkingStepsExteriorToBuildingAirlock() {

        Settlement settlement = buildSettlement();
		
        BuildingManager buildingManager = settlement.getBuildingManager();

        Building building0 = buildEVA(buildingManager, LocalPosition.DEFAULT_POSITION, 0D, 0);

        buildingManager.setupBuildingFunctionsMap();

		Person person = buildPerson("Walker", settlement);

		person.transfer(surface);

		LocalPosition target = new LocalPosition(4D, 4D);
        WalkingSteps walkingSteps = new WalkingSteps(person, target, building0);
        
        assertNotNull(walkingSteps);

        assertTrue(walkingSteps.canWalkAllSteps());

        assertNotNull(walkingSteps.getWalkingStepsList());

        assertEquals(3, walkingSteps.getWalkingStepsNumber());

        assertEquals(3, walkingSteps.getWalkingStepsList().size());

        WalkStep walkStep1 = walkingSteps.getWalkingStepsList().get(0);

        assertEquals(WalkStep.EXTERIOR_WALK, walkStep1.stepType);

        WalkStep walkStep2 = walkingSteps.getWalkingStepsList().get(1);

        assertEquals(WalkStep.ENTER_AIRLOCK, walkStep2.stepType);

        WalkStep walkStep3 = walkingSteps.getWalkingStepsList().get(2);

        assertEquals(WalkStep.SETTLEMENT_INTERIOR_WALK, walkStep3.stepType);

        assertEquals(target, walkStep3.loc);
    }



	/**
     * Test constructing walking steps from exterior to building interior with no airlock.
     */
    public void testWalkingStepsExteriorToBuildingNoAirlock() {
        
        Settlement settlement = buildSettlement();
		
        BuildingManager buildingManager = settlement.getBuildingManager();

        Building building0 = buildBuilding(buildingManager, LocalPosition.DEFAULT_POSITION, 0D, 0);
        buildingManager.setupBuildingFunctionsMap();

        Person person = buildPerson("Walker", settlement);

        person.transfer(surface);
        
        WalkingSteps walkingSteps = new WalkingSteps(person, new LocalPosition(3D, 3D), building0);
        
        assertNotNull(walkingSteps);

        assertFalse(walkingSteps.canWalkAllSteps());

        assertNotNull(walkingSteps.getWalkingStepsList());

        assertEquals(0, walkingSteps.getWalkingStepsNumber());

        assertEquals(0, walkingSteps.getWalkingStepsList().size());
    }

    /**
     * Test constructing walking steps from exterior to a rover.
     */
    public void testWalkingStepsExteriorToRover() {

        Settlement settlement = buildSettlement();

		LocalPosition parkedPosition = new LocalPosition(15D, -10D);
        Rover rover = buildRover(settlement, "Test Rover", parkedPosition);
        rover.transfer(surface);
        
		Person person = buildPerson("Walker", settlement);

        person.setPosition(new LocalPosition(20D,15D));
        person.transfer(surface);

        WalkingSteps walkingSteps = new WalkingSteps(person, parkedPosition, rover);
        
        assertNotNull(walkingSteps);
        
        assertTrue(walkingSteps.canWalkAllSteps());

        assertNotNull(walkingSteps.getWalkingStepsList());

        assertEquals(3, walkingSteps.getWalkingStepsNumber());

        assertEquals(3, walkingSteps.getWalkingStepsList().size());

        WalkStep walkStep1 = walkingSteps.getWalkingStepsList().get(0);

        assertEquals(WalkStep.EXTERIOR_WALK, walkStep1.stepType);

        WalkStep walkStep2 = walkingSteps.getWalkingStepsList().get(1);

        assertEquals(WalkStep.ENTER_AIRLOCK, walkStep2.stepType);

        WalkStep walkStep3 = walkingSteps.getWalkingStepsList().get(2);

        assertEquals(WalkStep.ROVER_INTERIOR_WALK, walkStep3.stepType);

        assertEquals(parkedPosition, walkStep3.loc);
    }
}