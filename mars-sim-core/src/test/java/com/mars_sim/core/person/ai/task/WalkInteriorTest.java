/*
 * Mars Simulation Project
 * WalkInteriorTest.java
 * @date 2023-07-05
 * @author Scott Davis
 */

package com.mars_sim.core.person.ai.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.connection.BuildingConnector;
import com.mars_sim.core.building.connection.BuildingConnectorManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.WalkingSteps.WalkStep;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.resource.SuppliesManifest;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.task.LoadControllerTest;
import com.mars_sim.core.vehicle.task.LoadVehicleGarage;

/**
 * A unit test suite for the WalkInterior task class.
 */
public class WalkInteriorTest extends AbstractMarsSimUnitTest {

	private static final LocalPosition HATCH2_POSITION = new LocalPosition(-7.5D, 0D);

	private static final LocalPosition HATCH1_POSITION = new LocalPosition(-4.5D, 0D);

	private static final LocalPosition HATCH3_POSITION = new LocalPosition(-10.5D, 0D);

    
    /**
     * Test the walkingPhase method.
     */
    public void testWalkingInSameBuildings() {

        Settlement settlement = buildSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();

        Building b1 = buildAccommodation(buildingManager, LocalPosition.DEFAULT_POSITION, 0D, 0);

		Person person = buildPerson("Walker", settlement);
		Worker worker = (Worker)person;
		person.setPosition(LocalPosition.DEFAULT_POSITION);
        BuildingManager.addToBuilding(person, b1);

        LocalPosition target = new LocalPosition(-2D, 1D);
        WalkSettlementInterior walkTask = new WalkSettlementInterior(worker, b1, target);
        assertFalse("Walk task can start", walkTask.isDone());
        
        executeTask(person, walkTask, 10);

        assertTrue("Interior walk completed", walkTask.isDone());
        assertEquals("Final Person building", b1, person.getBuildingLocation());
        assertEquals("Final Person position", target, person.getPosition());
    }
    
    /**
     * Test the walking to a garage
     */
    public void testWalkingInGarageBuildings() {

        Settlement settlement = buildSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();

        var g = buildGarage(buildingManager, LocalPosition.DEFAULT_POSITION, 0D, 0);
        Building b1 = g.getBuilding();
        
   		
        var person = buildPerson("Walker Mechanic", settlement, JobType.TECHNICIAN, g.getBuilding(), FunctionType.VEHICLE_MAINTENANCE);
        person.getSkillManager().addNewSkill(SkillType.MECHANICS, 10); // Skilled

		Worker worker = (Worker)person;
		person.setPosition(LocalPosition.DEFAULT_POSITION);
        BuildingManager.addToBuilding(person, b1);

        LocalPosition target = new LocalPosition(-2D, 1D);
        WalkSettlementInterior garageWalk = new WalkSettlementInterior(worker, b1, target);
        assertFalse("Garage Walk task can start", garageWalk.isDone());
        
        executeTask(person, garageWalk, 10);

        assertTrue("Initial Garage walk completed", garageWalk.isDone());
        assertEquals("Person building", b1, person.getBuildingLocation());
        assertEquals("Person position", target, person.getPosition());
        
        // Create a rover
        Rover r = buildRover(settlement, "rover1", new LocalPosition(10, 10));
    	assertTrue("Rover in settlement", r.isInSettlement());
    	assertTrue("Rover in garage", r.addToAGarage());
  		
        // Create a loading plan and preload Settlement
        var resources = new SuppliesManifest();
        resources.addAmount(ResourceUtil.FOOD_ID, 10D, true);
        LoadControllerTest.loadSettlement(settlement, resources);
        Vehicle v = (Vehicle)r;
        v.setLoading(resources);

        var task = new LoadVehicleGarage(person, v);
        assertFalse("Person's container unit", person.getContainerUnit() instanceof Vehicle);
	    assertFalse("Person is a crew member of the rover", r.isCrewmember(person));
		assertFalse("Person in rover", person.isInVehicle());	
		assertFalse("Person in garage", person.isInVehicleInGarage());
		
		assertTrue("Transfer person from settlement to vehicle", person.transfer(v));
	
		assertTrue("Person's container unit", person.getContainerUnit() instanceof Vehicle);
		
		assertTrue("Person in rover", person.isInVehicle());	
		assertTrue("Person in garage", person.isInVehicleInGarage());
		assertTrue("Person is a crew member of the rover", r.isCrewmember(person));
		assertFalse("Task created", task.isDone()); 

        // Do maintenance and advance to return
        executeTaskUntilPhase(person, task, 1000);
        assertGreaterThan("Final stored mass", 0D, v.getStoredMass());
        
        assertEquals("Person building position", b1, person.getBuildingLocation());
		assertFalse("Person in settlement", person.isInSettlement());
		
		assertTrue("Person's container unit.", person.getContainerUnit() instanceof Vehicle);
	    
	    assertTrue("Person is a crew member of the rover", r.isCrewmember(person));
	    assertTrue("Person in rover", person.isInVehicle());	
	    assertTrue("Person in garage", person.isInVehicleInGarage());

	
        // Gets a random location within rover.
     	LocalPosition adjustedLoc = LocalAreaUtil.getRandomLocalPos(r);
   	
     	// If person has not aboard the rover, board the rover and be ready to depart.
		if (!r.isCrewmember(person)) {

			WalkingSteps walkingSteps = new WalkingSteps(person, adjustedLoc, r);

			assertNotNull(walkingSteps);

	        assertTrue(walkingSteps.canWalkAllSteps()); 

	        assertNotNull(walkingSteps.getWalkingStepsList());

	        assertEquals(2, walkingSteps.getWalkingStepsNumber());

	        assertEquals(2, walkingSteps.getWalkingStepsList().size());

	        WalkStep walkStep0 = walkingSteps.getWalkingStepsList().get(0);

	        assertEquals(WalkStep.SETTLEMENT_INTERIOR_WALK, walkStep0.stepType);

	        WalkStep walkStep1 = walkingSteps.getWalkingStepsList().get(1);

	        assertEquals(WalkStep.ENTER_GARAGE_ROVER, walkStep1.stepType);

	        assertEquals(adjustedLoc, walkStep1.loc);
	        
			boolean canWalk = Walk.canWalkAllSteps(person, walkingSteps);
			
			assertTrue("Can walk to garage", canWalk);
			
			if (canWalk) {
				Walk walkToRoverInGarage = new Walk(person, walkingSteps);
				boolean canDo = person.getMind().getTaskManager().checkReplaceTask(walkToRoverInGarage, true);
				assertTrue("Can replace task", canDo);
				
		        assertFalse("Walk to Rover task can start", walkToRoverInGarage.isDone());
		        
		        executeTask(person, walkToRoverInGarage, 10);

		        assertTrue("Initial Garage walk completed", walkToRoverInGarage.isDone());
				System.out.println("task: " + person.getTaskDescription());
			    assertTrue("Garage walk completed", walkToRoverInGarage.isDone());
			}
		}	
    }
    
    /**
     * Test the walkingPhase method.
     */
    public void testWalkingBetweenThreeBuildings() {

        Settlement settlement = buildSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();
        BuildingConnectorManager connectorManager = settlement.getBuildingConnectorManager();


        Building b1 = buildAccommodation(buildingManager, LocalPosition.DEFAULT_POSITION, 0D, 0);
        Building b2 = buildBuilding(buildingManager, new LocalPosition(-6D, 0D), 270D, 1);
        Building b3 = buildBuilding(buildingManager, new LocalPosition(-12D, 0D), 270D, 2);
        Building b4 = buildBuilding(buildingManager, new LocalPosition(-18D, 0D), 270D, 3);

        connectorManager.addBuildingConnection(new BuildingConnector(b1, HATCH1_POSITION, 90D, b2, HATCH1_POSITION, 270D));
        connectorManager.addBuildingConnection(new BuildingConnector(b3, HATCH2_POSITION, 270D, b2, HATCH2_POSITION, 90D));
        connectorManager.addBuildingConnection(new BuildingConnector(b4, HATCH3_POSITION, 270D, b3, HATCH3_POSITION, 90D));
        
		Person person = buildPerson("Walker", settlement);
		Worker worker = (Worker)person;
		person.setPosition(LocalPosition.DEFAULT_POSITION);
        BuildingManager.addToBuilding(person, b1);

        LocalPosition target = new LocalPosition(-16D, 1D);
        WalkSettlementInterior walkTask = new WalkSettlementInterior(worker, b4, target);
        assertFalse("Walk task can start", walkTask.isDone());
        
        executeTask(person, walkTask, 10);

        assertTrue("Interior walk completed", walkTask.isDone());
        assertEquals("Final Person building", b4, person.getBuildingLocation());
        assertEquals("Final Person position", target, person.getPosition());
    }
    
    /**
     * Test the walkingPhase method.
     */
    public void testWalkingBetweenTwoBuildings() {

        Settlement settlement = buildSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();
        BuildingConnectorManager connectorManager = settlement.getBuildingConnectorManager();


        Building b3 = buildAccommodation(buildingManager, LocalPosition.DEFAULT_POSITION, 0D, 0);
        Building b4 = buildBuilding(buildingManager, new LocalPosition(-6D, 0D), 270D, 2);
        Building b5 = buildBuilding(buildingManager, new LocalPosition(-12D, 0D), 270D, 1);

        connectorManager.addBuildingConnection(new BuildingConnector(b3, HATCH1_POSITION, 90D, b4, HATCH1_POSITION, 270D));
        connectorManager.addBuildingConnection(new BuildingConnector(b5, HATCH2_POSITION, 270D, b4, HATCH2_POSITION, 90D));

		Person person = buildPerson("Walker", settlement);
		Worker worker = (Worker)person;
		person.setPosition(LocalPosition.DEFAULT_POSITION);
        BuildingManager.addToBuilding(person, b3);

        LocalPosition target = new LocalPosition(-11D, 1D);
        WalkSettlementInterior walkTask =  new WalkSettlementInterior(worker, b5, target);
        assertFalse("Walk task can start", walkTask.isDone());
        
        executeTask(person, walkTask, 10);

        assertTrue("Interior walk completed", walkTask.isDone());
        assertEquals("Final Person building", b5, person.getBuildingLocation());
        assertEquals("Final Person position", target, person.getPosition());
    }
}