package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonGender;
import org.mars_sim.msp.core.person.ai.task.WalkingSteps.WalkStep;
import org.mars_sim.msp.core.structure.MockSettlement;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.MockBuilding;
import org.mars_sim.msp.core.structure.building.connection.BuildingConnector;
import org.mars_sim.msp.core.structure.building.connection.BuildingConnectorManager;
import org.mars_sim.msp.core.structure.building.function.BuildingAirlock;
import org.mars_sim.msp.core.structure.building.function.EVA;
import org.mars_sim.msp.core.structure.building.function.GroundVehicleMaintenance;
import org.mars_sim.msp.core.vehicle.Rover;

import junit.framework.TestCase;

/**
 * A unit test suite for the WalkingSteps task class.
 */
public class WalkingStepsTest extends TestCase {

    /**
     * Test constructing walking steps from building interior to building interior with a 
     * valid walking path between them.
     */
    public void testWalkingStepsBuildingToBuildingPath() {
        
        Settlement settlement = new MockSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();
        BuildingConnectorManager connectorManager = settlement.getBuildingConnectorManager();
        assertNotNull(connectorManager);
        
        MockBuilding building0 = new MockBuilding(buildingManager);
        building0.setID(0);
        building0.setName("building 0");
        building0.setWidth(9D);
        building0.setLength(9D);
        building0.setXLocation(0D);
        building0.setYLocation(0D);
        building0.setFacing(0D);
        buildingManager.addBuilding(building0);
        
        MockBuilding building1 = new MockBuilding(buildingManager);
        building1.setID(1);
        building1.setName("building 1");
        building1.setWidth(6D);
        building1.setLength(9D);
        building1.setXLocation(-12D);
        building1.setYLocation(0D);
        building1.setFacing(270D);
        buildingManager.addBuilding(building1);
        
        MockBuilding building2 = new MockBuilding(buildingManager);
        building2.setID(2);
        building2.setName("building 2");
        building2.setWidth(2D);
        building2.setLength(3D);
        building2.setXLocation(-6D);
        building2.setYLocation(0D);
        building2.setFacing(270D);
        buildingManager.addBuilding(building2);
        
        connectorManager.addBuildingConnection(new BuildingConnector(building0, 
                -4.5D, 0D, 90D, building2, -4.5D, 0D, 270D));
        connectorManager.addBuildingConnection(new BuildingConnector(building1, 
                -7.5D, 0D, 270D, building2, -7.5D, 0D, 90D));
        
        Person person = new Person("test person", PersonGender.MALE, "Earth", settlement);
        person.setXLocation(0D);
        person.setYLocation(0D);
        BuildingManager.addPersonToBuildingSameLocation(person, building0);
        
        WalkingSteps walkingSteps = new WalkingSteps(person, -6D, 1D, building2);
        assertNotNull(walkingSteps);
        
        assertTrue(walkingSteps.canWalkAllSteps());
        
        assertNotNull(walkingSteps.getWalkingStepsList());
        
        assertEquals(1, walkingSteps.getWalkingStepsNumber());
        
        WalkStep walkStep1 = walkingSteps.getWalkingStepsList().get(0);
        
        assertEquals(WalkStep.SETTLEMENT_INTERIOR_WALK, walkStep1.stepType);
        
        assertEquals(-6D, walkStep1.xLoc);
        
        assertEquals(1D, walkStep1.yLoc);
    }
    
    /**
     * Test constructing walking steps from building interior to building interior with no 
     * valid walking path between them and no airlocks.
     */
    public void testWalkingStepsBuildingToBuildingNoPath() {
        
        Settlement settlement = new MockSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();
        BuildingConnectorManager connectorManager = settlement.getBuildingConnectorManager();
        assertNotNull(connectorManager);
        
        MockBuilding building0 = new MockBuilding(buildingManager);
        building0.setID(0);
        building0.setName("building 0");
        building0.setWidth(9D);
        building0.setLength(9D);
        building0.setXLocation(0D);
        building0.setYLocation(0D);
        building0.setFacing(0D);
        buildingManager.addBuilding(building0);
        
        MockBuilding building1 = new MockBuilding(buildingManager);
        building1.setID(1);
        building1.setName("building 1");
        building1.setWidth(6D);
        building1.setLength(9D);
        building1.setXLocation(-12D);
        building1.setYLocation(0D);
        building1.setFacing(270D);
        buildingManager.addBuilding(building1);
        
        Person person = new Person("test person", PersonGender.MALE, "Earth", settlement);
        person.setXLocation(0D);
        person.setYLocation(0D);
        BuildingManager.addPersonToBuildingSameLocation(person, building0);
        
        WalkingSteps walkingSteps = new WalkingSteps(person, 6D, 9D, building1);
        assertNotNull(walkingSteps);
        
        assertFalse(walkingSteps.canWalkAllSteps());
        
        assertNotNull(walkingSteps.getWalkingStepsList());
        
        assertEquals(0, walkingSteps.getWalkingStepsNumber());
        
        assertEquals(0, walkingSteps.getWalkingStepsList().size());
    }
    
    /**
     * Test constructing walking steps from building interior to building interior with no 
     * valid walking path between them and airlocks.
     */
    public void testWalkingStepsBuildingToBuildingNoPathAirlocks() {
        
        Settlement settlement = new MockSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();
        BuildingConnectorManager connectorManager = settlement.getBuildingConnectorManager();
        assertNotNull(connectorManager);
        
        MockBuilding building0 = new MockBuilding(buildingManager);
        building0.setID(0);
        building0.setName("building 0");
        building0.setWidth(9D);
        building0.setLength(9D);
        building0.setXLocation(0D);
        building0.setYLocation(0D);
        building0.setFacing(0D);
        buildingManager.addBuilding(building0);
        
        BuildingAirlock airlock0 = new BuildingAirlock(building0, 1, 0D, 0D, 0D, 0D, 0D, 0D);
        building0.addFunction(new EVA(building0, airlock0));
        
        MockBuilding building1 = new MockBuilding(buildingManager);
        building1.setID(1);
        building1.setName("building 1");
        building1.setWidth(6D);
        building1.setLength(9D);
        building1.setXLocation(-12D);
        building1.setYLocation(0D);
        building1.setFacing(270D);
        buildingManager.addBuilding(building1);
        
        BuildingAirlock airlock1 = new BuildingAirlock(building1, 1, 0D, 0D, 0D, 0D, 0D, 0D);
        building1.addFunction(new EVA(building1, airlock1));
        
        Person person = new Person("test person", PersonGender.MALE, "Earth", settlement);
        person.setXLocation(0D);
        person.setYLocation(0D);
        BuildingManager.addPersonToBuildingSameLocation(person, building0);
        
        WalkingSteps walkingSteps = new WalkingSteps(person, 6D, 9D, building1);
        assertNotNull(walkingSteps);
        
        assertTrue(walkingSteps.canWalkAllSteps());
        
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
        
        assertEquals(WalkStep.SETTLEMENT_INTERIOR_WALK, walkStep5.stepType);
    }
    
    /**
     * Test constructing walking steps from building interior to exterior with an airlock.
     */
    public void testWalkingStepsBuildingToExteriorAirlock() {
        
        Settlement settlement = new MockSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();
        BuildingConnectorManager connectorManager = settlement.getBuildingConnectorManager();
        assertNotNull(connectorManager);
        
        MockBuilding building0 = new MockBuilding(buildingManager);
        building0.setID(0);
        building0.setName("building 0");
        building0.setWidth(9D);
        building0.setLength(9D);
        building0.setXLocation(0D);
        building0.setYLocation(0D);
        building0.setFacing(0D);
        buildingManager.addBuilding(building0);
        
        BuildingAirlock airlock0 = new BuildingAirlock(building0, 1, 0D, 0D, 0D, 0D, 0D, 0D);
        building0.addFunction(new EVA(building0, airlock0));
        
        Person person = new Person("test person", PersonGender.MALE, "Earth", settlement);
        person.setXLocation(0D);
        person.setYLocation(0D);
        BuildingManager.addPersonToBuildingSameLocation(person, building0);
        
        WalkingSteps walkingSteps = new WalkingSteps(person, 10D, 15D, null);
        assertNotNull(walkingSteps);
        
        assertTrue(walkingSteps.canWalkAllSteps());
        
        assertNotNull(walkingSteps.getWalkingStepsList());
        
        assertEquals(3, walkingSteps.getWalkingStepsNumber());
        
        assertEquals(3, walkingSteps.getWalkingStepsList().size());
        
        WalkStep walkStep1 = walkingSteps.getWalkingStepsList().get(0);
        
        assertEquals(WalkStep.SETTLEMENT_INTERIOR_WALK, walkStep1.stepType);
        
        WalkStep walkStep2 = walkingSteps.getWalkingStepsList().get(1);
        
        assertEquals(WalkStep.EXIT_AIRLOCK, walkStep2.stepType);
        
        WalkStep walkStep3 = walkingSteps.getWalkingStepsList().get(2);
        
        assertEquals(WalkStep.EXTERIOR_WALK, walkStep3.stepType);
        
        assertEquals(10D, walkStep3.xLoc);
        
        assertEquals(15D, walkStep3.yLoc);
    }
    
    /**
     * Test constructing walking steps from building interior to exterior with no airlock.
     */
    public void testWalkingStepsBuildingToExteriorNoAirlock() {
        
        Settlement settlement = new MockSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();
        BuildingConnectorManager connectorManager = settlement.getBuildingConnectorManager();
        assertNotNull(connectorManager);
        
        MockBuilding building0 = new MockBuilding(buildingManager);
        building0.setID(0);
        building0.setName("building 0");
        building0.setWidth(9D);
        building0.setLength(9D);
        building0.setXLocation(0D);
        building0.setYLocation(0D);
        building0.setFacing(0D);
        buildingManager.addBuilding(building0);
        
        Person person = new Person("test person", PersonGender.MALE, "Earth", settlement);
        person.setXLocation(0D);
        person.setYLocation(0D);
        BuildingManager.addPersonToBuildingSameLocation(person, building0);
        
        WalkingSteps walkingSteps = new WalkingSteps(person, 10D, 15D, null);
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
        
        Settlement settlement = new MockSettlement();
        
        Rover rover = new Rover("Test Rover", "Explorer Rover", settlement);
        rover.setParkedLocation(15D, -10D, 0D);
        
        Person person = new Person("test person", PersonGender.MALE, "Earth", settlement);
        person.setXLocation(15D);
        person.setYLocation(-10D);
        settlement.getInventory().retrieveUnit(person);
        rover.getInventory().storeUnit(person);
        
        WalkingSteps walkingSteps = new WalkingSteps(person, 20D, 15D, null);
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
        
        assertEquals(20D, walkStep3.xLoc);
        
        assertEquals(15D, walkStep3.yLoc);
    }
    
    /**
     * Test constructing walking steps from a rover to a building.
     */
    public void testWalkingStepsRoverToBuilding() {
        
        Settlement settlement = new MockSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();
        BuildingConnectorManager connectorManager = settlement.getBuildingConnectorManager();
        assertNotNull(connectorManager);
        
        Rover rover = new Rover("Test Rover", "Explorer Rover", settlement);
        rover.setParkedLocation(15D, -10D, 0D);
        
        MockBuilding building0 = new MockBuilding(buildingManager);
        building0.setID(0);
        building0.setName("building 0");
        building0.setWidth(9D);
        building0.setLength(9D);
        building0.setXLocation(0D);
        building0.setYLocation(0D);
        building0.setFacing(0D);
        buildingManager.addBuilding(building0);
        
        BuildingAirlock airlock0 = new BuildingAirlock(building0, 1, 0D, 0D, 0D, 0D, 0D, 0D);
        building0.addFunction(new EVA(building0, airlock0));
        
        Person person = new Person("test person", PersonGender.MALE, "Earth", settlement);
        person.setXLocation(15D);
        person.setYLocation(-10D);
        settlement.getInventory().retrieveUnit(person);
        rover.getInventory().storeUnit(person);
        
        WalkingSteps walkingSteps = new WalkingSteps(person, 0D, 0D, building0);
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
        
        Settlement settlement = new MockSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();
        BuildingConnectorManager connectorManager = settlement.getBuildingConnectorManager();
        assertNotNull(connectorManager);
        
        Rover rover = new Rover("Test Rover", "Explorer Rover", settlement);
        rover.setParkedLocation(15D, -10D, 0D);
        
        MockBuilding building0 = new MockBuilding(buildingManager);
        building0.setID(0);
        building0.setName("building 0");
        building0.setWidth(9D);
        building0.setLength(9D);
        building0.setXLocation(0D);
        building0.setYLocation(0D);
        building0.setFacing(0D);
        buildingManager.addBuilding(building0);
        
        BuildingAirlock airlock0 = new BuildingAirlock(building0, 1, 0D, 0D, 0D, 0D, 0D, 0D);
        building0.addFunction(new EVA(building0, airlock0));
        
        Person person = new Person("test person", PersonGender.MALE, "Earth", settlement);
        person.setXLocation(0D);
        person.setYLocation(0D);
        BuildingManager.addPersonToBuildingSameLocation(person, building0);
        
        WalkingSteps walkingSteps = new WalkingSteps(person, 15D, -10D, rover);
        assertNotNull(walkingSteps);
        
        assertTrue(walkingSteps.canWalkAllSteps());
        
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
        
        Settlement settlement = new MockSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();
        BuildingConnectorManager connectorManager = settlement.getBuildingConnectorManager();
        assertNotNull(connectorManager);
        
        Rover rover = new Rover("Test Rover", "Explorer Rover", settlement);
        rover.setParkedLocation(15D, -10D, 0D);
        
        MockBuilding building0 = new MockBuilding(buildingManager);
        building0.setID(0);
        building0.setName("building 0");
        building0.setWidth(9D);
        building0.setLength(9D);
        building0.setXLocation(0D);
        building0.setYLocation(0D);
        building0.setFacing(0D);
        buildingManager.addBuilding(building0);
        
        Person person = new Person("test person", PersonGender.MALE, "Earth", settlement);
        person.setXLocation(0D);
        person.setYLocation(0D);
        BuildingManager.addPersonToBuildingSameLocation(person, building0);
        
        WalkingSteps walkingSteps = new WalkingSteps(person, 15D, -10D, rover);
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
        
        Settlement settlement = new MockSettlement();
        
        Rover rover1 = new Rover("Test Rover 1", "Explorer Rover", settlement);
        rover1.setParkedLocation(15D, -10D, 0D);
        
        Rover rover2 = new Rover("Test Rover 2", "Explorer Rover", settlement);
        rover2.setParkedLocation(-50D, 20D, 15D);
        
        Person person = new Person("test person", PersonGender.MALE, "Earth", settlement);
        person.setXLocation(15D);
        person.setYLocation(-10D);
        settlement.getInventory().retrieveUnit(person);
        rover1.getInventory().storeUnit(person);
        
        WalkingSteps walkingSteps = new WalkingSteps(person, -50D, 20D, rover2);
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
        
        Settlement settlement = new MockSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();
        BuildingConnectorManager connectorManager = settlement.getBuildingConnectorManager();
        assertNotNull(connectorManager);
        
        Rover rover = new Rover("Test Rover", "Explorer Rover", settlement);
        rover.setParkedLocation(0D, 0D, 0D);
        
        MockBuilding building0 = new MockBuilding(buildingManager);
        building0.setID(0);
        building0.setName("building 0");
        building0.setWidth(9D);
        building0.setLength(9D);
        building0.setXLocation(0D);
        building0.setYLocation(0D);
        building0.setFacing(0D);
        buildingManager.addBuilding(building0);
        
        BuildingAirlock airlock0 = new BuildingAirlock(building0, 1, 0D, 0D, 0D, 0D, 0D, 0D);
        building0.addFunction(new EVA(building0, airlock0));
        
        Point2D parkingLocation = new Point2D.Double(0D, 0D);
        GroundVehicleMaintenance garage = new GroundVehicleMaintenance(building0, 1, 
                new Point2D[] { parkingLocation });
        building0.addFunction(garage);
        garage.addVehicle(rover);
        
        Person person = new Person("test person", PersonGender.MALE, "Earth", settlement);
        person.setXLocation(4D);
        person.setYLocation(4D);
        BuildingManager.addPersonToBuildingSameLocation(person, building0);
        
        WalkingSteps walkingSteps = new WalkingSteps(person, 0D, 0D, rover);
        assertNotNull(walkingSteps);
        
        assertTrue(walkingSteps.canWalkAllSteps());
        
        assertNotNull(walkingSteps.getWalkingStepsList());
        
        assertEquals(2, walkingSteps.getWalkingStepsNumber());
        
        assertEquals(2, walkingSteps.getWalkingStepsList().size());
        
        WalkStep walkStep1 = walkingSteps.getWalkingStepsList().get(0);
        
        assertEquals(WalkStep.SETTLEMENT_INTERIOR_WALK, walkStep1.stepType);
        
        WalkStep walkStep2 = walkingSteps.getWalkingStepsList().get(1);
        
        assertEquals(WalkStep.ENTER_GARAGE_ROVER, walkStep2.stepType);
        
        assertEquals(0D, walkStep2.xLoc);
        
        assertEquals(0D, walkStep2.yLoc);
    }
    
    /**
     * Test constructing walking steps from a rover in a garage to a building.
     */
    public void testWalkingStepsRoverToBuildingInGarage() {
        
        Settlement settlement = new MockSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();
        BuildingConnectorManager connectorManager = settlement.getBuildingConnectorManager();
        assertNotNull(connectorManager);
        
        Rover rover = new Rover("Test Rover", "Explorer Rover", settlement);
        rover.setParkedLocation(0D, 0D, 0D);
        
        MockBuilding building0 = new MockBuilding(buildingManager);
        building0.setID(0);
        building0.setName("building 0");
        building0.setWidth(9D);
        building0.setLength(9D);
        building0.setXLocation(0D);
        building0.setYLocation(0D);
        building0.setFacing(0D);
        buildingManager.addBuilding(building0);
        
        BuildingAirlock airlock0 = new BuildingAirlock(building0, 1, 0D, 0D, 0D, 0D, 0D, 0D);
        building0.addFunction(new EVA(building0, airlock0));
        
        Point2D parkingLocation = new Point2D.Double(0D, 0D);
        GroundVehicleMaintenance garage = new GroundVehicleMaintenance(building0, 1, 
                new Point2D[] { parkingLocation });
        building0.addFunction(garage);
        garage.addVehicle(rover);
        
        Person person = new Person("test person", PersonGender.MALE, "Earth", settlement);
        person.setXLocation(0D);
        person.setYLocation(0D);
        settlement.getInventory().retrieveUnit(person);
        rover.getInventory().storeUnit(person);
        
        WalkingSteps walkingSteps = new WalkingSteps(person, 4D, 4D, building0);
        assertNotNull(walkingSteps);
        
        assertTrue(walkingSteps.canWalkAllSteps());
        
        assertNotNull(walkingSteps.getWalkingStepsList());
        
        assertEquals(2, walkingSteps.getWalkingStepsNumber());
        
        assertEquals(2, walkingSteps.getWalkingStepsList().size());
        
        WalkStep walkStep1 = walkingSteps.getWalkingStepsList().get(0);
        
        assertEquals(WalkStep.EXIT_GARAGE_ROVER, walkStep1.stepType);
        
        WalkStep walkStep2 = walkingSteps.getWalkingStepsList().get(1);
        
        assertEquals(WalkStep.SETTLEMENT_INTERIOR_WALK, walkStep2.stepType);
        
        assertEquals(4D, walkStep2.xLoc);
        
        assertEquals(4D, walkStep2.yLoc);
    }
    
    /**
     * Test constructing walking steps from exterior to building interior with an airlock.
     */
    public void testWalkingStepsExteriorToBuildingAirlock() {
        
        Settlement settlement = new MockSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();
        BuildingConnectorManager connectorManager = settlement.getBuildingConnectorManager();
        assertNotNull(connectorManager);
        
        MockBuilding building0 = new MockBuilding(buildingManager);
        building0.setID(0);
        building0.setName("building 0");
        building0.setWidth(9D);
        building0.setLength(9D);
        building0.setXLocation(0D);
        building0.setYLocation(0D);
        building0.setFacing(0D);
        buildingManager.addBuilding(building0);
        
        BuildingAirlock airlock0 = new BuildingAirlock(building0, 1, 0D, 0D, 0D, 0D, 0D, 0D);
        building0.addFunction(new EVA(building0, airlock0));
        
        Person person = new Person("test person", PersonGender.MALE, "Earth", settlement);
        person.setXLocation(50D);
        person.setYLocation(50D);
        settlement.getInventory().retrieveUnit(person);
        
        WalkingSteps walkingSteps = new WalkingSteps(person, 5D, 5D, building0);
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
        
        assertEquals(5D, walkStep3.xLoc);
        
        assertEquals(5D, walkStep3.yLoc);
    }
    
    /**
     * Test constructing walking steps from exterior to building interior with no airlock.
     */
    public void testWalkingStepsExteriorToBuildingNoAirlock() {
        
        Settlement settlement = new MockSettlement();
        BuildingManager buildingManager = settlement.getBuildingManager();
        BuildingConnectorManager connectorManager = settlement.getBuildingConnectorManager();
        assertNotNull(connectorManager);
        
        MockBuilding building0 = new MockBuilding(buildingManager);
        building0.setID(0);
        building0.setName("building 0");
        building0.setWidth(9D);
        building0.setLength(9D);
        building0.setXLocation(0D);
        building0.setYLocation(0D);
        building0.setFacing(0D);
        buildingManager.addBuilding(building0);
        
        Person person = new Person("test person", PersonGender.MALE, "Earth", settlement);
        person.setXLocation(0D);
        person.setYLocation(0D);
        settlement.getInventory().retrieveUnit(person);
        
        WalkingSteps walkingSteps = new WalkingSteps(person, 5D, 5D, building0);
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
        
        Settlement settlement = new MockSettlement();
        
        Rover rover = new Rover("Test Rover", "Explorer Rover", settlement);
        rover.setParkedLocation(15D, -10D, 0D);
        
        Person person = new Person("test person", PersonGender.MALE, "Earth", settlement);
        person.setXLocation(20D);
        person.setYLocation(15D);
        settlement.getInventory().retrieveUnit(person);
        
        WalkingSteps walkingSteps = new WalkingSteps(person, 15D, -10D, rover);
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
        
        assertEquals(15D, walkStep3.xLoc);
        
        assertEquals(-10D, walkStep3.yLoc);
    }
}