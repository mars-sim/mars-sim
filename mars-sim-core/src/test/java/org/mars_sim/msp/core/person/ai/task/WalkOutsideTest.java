/**
 * Mars Simulation Project
 * LoadVehicleTest.java
 * @version 3.1.0 2017-04-11
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;

import junit.framework.TestCase;

/**
 * A unit test suite for the WalkOutside task class.
 */
public class WalkOutsideTest
extends TestCase {

	/**
	 * Check the clearPathToDestination method.
	 */
	public void testCheckClearPathToDestination() {

		// Create new simulation instance.
		SimulationConfig.instance().loadConfig();
//		Simulation.instance().createNewSimulation(-1, true);
//
//		// Clear out existing settlements in simulation.
//		UnitManager unitManager = Simulation.instance().getUnitManager();
//		Iterator<Settlement> i = unitManager.getSettlements().iterator();
//		while (i.hasNext()) {
//			unitManager.removeUnit(i.next());
//		}
//		
//		// Create test settlement.
//		Settlement settlement = new MockSettlement();
//		unitManager.addUnit(settlement);
//
//		// Removes all mock buildings and building functions in the settlement.
//		settlement.getBuildingManager().removeAllMockBuildings();
//
//		// Create test building.
//		MockBuilding building = new MockBuilding(settlement.getBuildingManager());
//		building.setWidth(10D);
//		building.setLength(10D);
//		building.setXLocation(0D);
//		building.setYLocation(0D);
//		building.setFacing(0D);
//		settlement.getBuildingManager().addMockBuilding(building);
////		settlement.getBuildingConnectorManager().createBuildingConnections(building);
//		
//		BuildingAirlock airlock0 = new BuildingAirlock(building, 1, 0D, 0D, 0D, 0D, 0D, 0D);
//        building.addFunction(new EVA(building, airlock0));
//
//		// Create test person.
//		// Person person = new Person("test person", PersonGender.MALE, null, settlement, "Mars Society (MS)");
//		// Use Builder Pattern for creating an instance of Person
//		Person person = Person.create("test person", settlement)
//								.setGender(GenderType.MALE)
//								.setCountry(null)
//								.setSponsor("Mars Society (MS)")
//								.build();
//		person.initializeMock();
////		settlement.getInventory().storeUnit(person);
//		settlement.getInventory().retrieveUnit(person);
//		person.setXLocation(10D);
//		person.setYLocation(0D);
//
//		// Create walking task.
//		WalkOutside walkTask = new WalkOutside(person, person.getXLocation(),
//				person.getYLocation(), -20D, 0D, true);
//
//		Point2D startLoc = new Point2D.Double(10D, 0D);
//		Point2D destLoc = new Point2D.Double(-20D, 0D);
//
////		assertFalse(walkTask.checkClearPathToDestination(startLoc, destLoc));
//
//		destLoc.setLocation(10D, 20D);
//
//		assertTrue(walkTask.checkClearPathToDestination(startLoc, destLoc));
//
//		destLoc.setLocation(10D, -20D);
//
//		assertTrue(walkTask.checkClearPathToDestination(startLoc, destLoc));
//
//		destLoc.setLocation(0D, 20D);
//
//		assertTrue(walkTask.checkClearPathToDestination(startLoc, destLoc));
//
//		destLoc.setLocation(0D, -20D);
//
//		assertTrue(walkTask.checkClearPathToDestination(startLoc, destLoc));
//
		// Clear obstacle cache.
		LocalAreaUtil.clearObstacleCache();
	}

//	/**
//	 * Test the determineObstacleAvoidancePath method.
//	 */
//	public void testDetermineObstacleAvoidancePath() {
//
//		// Create new simulation instance.
//		SimulationConfig.loadConfig();
//		Simulation.createNewSimulation(-1, true);
//
//		// Clear out existing settlements in simulation.
//		UnitManager unitManager = Simulation.instance().getUnitManager();
//		Iterator<Settlement> i = unitManager.getSettlements().iterator();
//		while (i.hasNext()) {
//			unitManager.removeUnit(i.next());
//		}
//
//		// Create test settlement.
//		Settlement settlement = new MockSettlement();
//		unitManager.addUnit(settlement);
//
//		// Removes all mock buildings and building functions in the settlement.
//		settlement.getBuildingManager().removeAllMockBuildings();
//		
//		// Create test building.
//		MockBuilding building1 = new MockBuilding(settlement.getBuildingManager());
//		building1.setWidth(10D);
//		building1.setLength(10D);
//		building1.setXLocation(0D);
//		building1.setYLocation(0D);
//		building1.setFacing(0D);
//		settlement.getBuildingManager().addMockBuilding(building1);
//		settlement.getBuildingConnectorManager().createBuildingConnections(building1);
//		
//		// Clear obstacle cache.
////		LocalAreaUtil.clearObstacleCache();
//		
//		BuildingAirlock airlock0 = new BuildingAirlock(building1, 1, 0D, 0D, 0D, 0D, 0D, 0D);
//        building1.addFunction(new EVA(building1, airlock0));
//
//		// Create test person.
//		//Person person = new Person("test person", PersonGender.MALE, null, settlement, "Mars Society (MS)");
//		// Use Builder Pattern for creating an instance of Person
//		Person person = Person.create("test person", settlement)
//								.setGender(GenderType.MALE)
//								.setCountry(null)
//								.setSponsor("Mars Society (MS)")
//								.build();
//		person.initializeMock();
////		settlement.getInventory().storeUnit(person);
//		settlement.getInventory().retrieveUnit(person);
//		person.setXLocation(10D);
//		person.setYLocation(0D);
//
//		// Create walking task.
//		WalkOutside walkTask1 = new WalkOutside(person, person.getXLocation(),
//				person.getYLocation(), -20D, 0D, true);
//
//		// Determine an obstacle avoidance path around building.
//		List<Point2D> path1 = walkTask1.determineObstacleAvoidancePath();
//		assertNotNull(path1);
////		assertEquals(4, path1.size());
//		assertEquals(new Point2D.Double(10D, 0D), path1.get(0));
////		assertEquals(new Point2D.Double(10D, 7D), path1.get(1));
////		assertEquals(new Point2D.Double(-4D, 7D), path1.get(2));
////		assertEquals(new Point2D.Double(-20D, 0D), path1.get(3));
//
////		// Clear obstacle cache.
////		LocalAreaUtil.clearObstacleCache();
//		
//		// Create walking task.
//		person.setYLocation(1D);
//		WalkOutside walkTask2 = new WalkOutside(person, person.getXLocation(),
//				person.getYLocation(), -20D, 0D, true);
//
//		// Determine an obstacle avoidance path around building.
//		List<Point2D> path2 = walkTask2.determineObstacleAvoidancePath();
//		assertNotNull(path2);
////		assertEquals(4, path2.size());
//		assertEquals(new Point2D.Double(10D, 1D), path2.get(0));
////		assertEquals(new Point2D.Double(10D, -6D), path2.get(1));
////		assertEquals(new Point2D.Double(-4D, -6D), path2.get(2));
////		assertEquals(new Point2D.Double(-20D, 0D), path2.get(3));
//
//		// Add a second building to settlement.
//		MockBuilding building2 = new MockBuilding(settlement.getBuildingManager());
//		building2.setWidth(10D);
//		building2.setLength(10D);
//		building2.setXLocation(-12D);
//		building2.setYLocation(4D);
//		building2.setFacing(22D);
//		settlement.getBuildingManager().addMockBuilding(building2);
//		settlement.getBuildingConnectorManager().createBuildingConnections(building2);
//		
//		// Clear obstacle cache.
////		LocalAreaUtil.clearObstacleCache();
//
//		WalkOutside walkTask3 = new WalkOutside(person, person.getXLocation(),
//				person.getYLocation(), -20D, 0D, true);
//
//		// Determine an obstacle avoidance path around both buildings.
//		List<Point2D> path3 = walkTask3.determineObstacleAvoidancePath();
//		assertNotNull(path3);
////		assertEquals(4, path3.size());
//		assertEquals(new Point2D.Double(10D, 1D), path3.get(0));
//		assertEquals(new Point2D.Double(10D, -6D), path3.get(1));
//		assertEquals(new Point2D.Double(-11D, -6D), path3.get(2));
//		assertEquals(new Point2D.Double(-20D, 0D), path3.get(3));
//
//		WalkOutside walkTask4 = new WalkOutside(person, person.getXLocation(),
//				person.getYLocation(), 0D, 0D, true);
//
//		// Determine a failed obstacle avoidance path.
//		List<Point2D> path4 = walkTask4.determineObstacleAvoidancePath();
//		assertNull(path4);
//
//		// Clear obstacle cache.
//		LocalAreaUtil.clearObstacleCache();
//	}

//	/**
//	 * Test the determineWalkingPath method.
//	 */
//	public void testDetermineWalkingPath() {
//
//		// Create new simulation instance.
//		SimulationConfig.instance().loadConfig();
//		Simulation.instance().testRun();
//
//		// Clear out existing settlements in simulation.
//		UnitManager unitManager = Simulation.instance().getUnitManager();
//		Iterator<Settlement> i = unitManager.getSettlements().iterator();
//		while (i.hasNext()) {
//			unitManager.removeUnit(i.next());
//		}
//
//		// Create test settlement.
//		Settlement settlement = new MockSettlement();
//		
//		unitManager.addUnit(settlement);
//
//		BuildingManager buildingManager = settlement.getBuildingManager();
//		// Removes all mock buildings and building functions in the settlement.
//		settlement.getBuildingManager().removeAllMockBuildings();
//		
//		// Create test building.
//		MockBuilding building1 = new MockBuilding(buildingManager);
//		building1.setWidth(10D);
//		building1.setLength(10D);
//		building1.setXLocation(0D);
//		building1.setYLocation(0D);
//		building1.setFacing(0D);
//		settlement.getBuildingManager().addMockBuilding(building1);
//		settlement.getBuildingConnectorManager().createBuildingConnections(building1);
//		
//		BuildingAirlock airlock0 = new BuildingAirlock(building1, 1, 0D, 0D, 0D, 0D, 0D, 0D);
//        building1.addFunction(new EVA(building1, airlock0));
//
//		// Use Builder Pattern for creating an instance of Person
//		Person person = Person.create("test person", settlement)
//								.setGender(GenderType.MALE)
//								.setCountry(null)
//								.setSponsor("Mars Society (MS)")
//								.build();
//		person.initializeMock();
////		settlement.getInventory().storeUnit(person);
//		settlement.getInventory().retrieveUnit(person);
//		person.setXLocation(10D);
//		person.setYLocation(0D);
//
//		// Create walking task.
//		WalkOutside walkTask1 = new WalkOutside(person, person.getXLocation(),
//				person.getYLocation(), 20D, 0D, true);
//
//		// Determine a walking path.
//		List<Point2D> path1 = walkTask1.determineWalkingPath();
//		assertNotNull(path1);
//		assertEquals(2, path1.size());
//		assertEquals(new Point2D.Double(10D, 0D), path1.get(0));
//		assertEquals(new Point2D.Double(20D, 0D), path1.get(1));
//		assertFalse(walkTask1.areObstaclesInPath());
//
//		// Create walking task.
//		WalkOutside walkTask2 = new WalkOutside(person, person.getXLocation(),
//				person.getYLocation(), -20D, 0D, true);
//
//		// Determine walking path around building.
//		List<Point2D> path2 = walkTask2.determineWalkingPath();
//		assertNotNull(path2);
////		assertEquals(4, path2.size());
//		assertEquals(new Point2D.Double(10D, 0D), path2.get(0));
////		assertEquals(new Point2D.Double(10D, 7D), path2.get(1));
////		assertEquals(new Point2D.Double(-4D, 7D), path2.get(2));
////		assertEquals(new Point2D.Double(-20D, 0D), path2.get(3));
//		assertFalse(walkTask2.areObstaclesInPath());
//
//		// Create walking task.
//		WalkOutside walkTask3 = new WalkOutside(person, person.getXLocation(),
//				person.getYLocation(), 0D, 0D, true);
//
//		// Determine a walking path through building.
//		List<Point2D> path3 = walkTask3.determineWalkingPath();
//		assertNotNull(path3);
//		assertEquals(2, path3.size());
//		assertEquals(new Point2D.Double(10D, 0D), path3.get(0));
//		assertEquals(new Point2D.Double(0D, 0D), path3.get(1));
////		assertTrue(walkTask3.areObstaclesInPath());
//
//		// Clear obstacle cache.
//		LocalAreaUtil.clearObstacleCache();
//	}

//	/**
//	 * Test the getLocalObstacleSearchLimits method.
//	 */
//	public void testGetLocalObstacleSearchLimits() {
//
//		// Create new simulation instance.
//		SimulationConfig.instance().loadConfig();
//		Simulation.instance().testRun();
//
//		// Clear out existing settlements in simulation.
//		UnitManager unitManager = Simulation.instance().getUnitManager();
//		Iterator<Settlement> i = unitManager.getSettlements().iterator();
//		while (i.hasNext()) {
//			unitManager.removeUnit(i.next());
//		}
//
//		// Create test settlement.
//		Settlement settlement = new MockSettlement();
//		unitManager.addUnit(settlement);
//
//		// Create test building.
//		MockBuilding building1 = new MockBuilding(settlement.getBuildingManager());
//		building1.setWidth(10D);
//		building1.setLength(10D);
//		building1.setXLocation(0D);
//		building1.setYLocation(0D);
//		building1.setFacing(0D);
//		settlement.getBuildingManager().addMockBuilding(building1);
//		settlement.getBuildingConnectorManager().createBuildingConnections(building1);
//		
//		BuildingAirlock airlock0 = new BuildingAirlock(building1, 1, 0D, 0D, 0D, 0D, 0D, 0D);
//        building1.addFunction(new EVA(building1, airlock0));
//
//		// Use Builder Pattern for creating an instance of Person
//		Person person = Person.create("test person", settlement)
//								.setGender(GenderType.MALE)
//								.setCountry(null)
//								.setSponsor("Mars Society (MS)")
//								.build();
//		person.initializeMock();
////		settlement.getInventory().storeUnit(person);
//		settlement.getInventory().retrieveUnit(person);
//		person.setXLocation(10D);
//		person.setYLocation(0D);
//
//		// Create walking task.
//		WalkOutside walkTask1 = new WalkOutside(person, person.getXLocation(),
//				person.getYLocation(), -20D, 0D, true);
//
//		double[] bounds1 = walkTask1.getLocalObstacleSearchLimits(person.getCoordinates());
//		assertNotNull(bounds1);
//		assertEquals(4, bounds1.length);
//		assertEquals(17D, bounds1[0]);
//		assertEquals(-27D, bounds1[1]);
////		assertEquals(12D, bounds1[2]);
////		assertEquals(-12D, bounds1[3]);
//
//		// Rotate building1 to 45 degrees.
//		building1.setFacing(45D);
//
//		// Clear obstacle cache.
//		LocalAreaUtil.clearObstacleCache();
//
//		double[] bounds2 = walkTask1.getLocalObstacleSearchLimits(person.getCoordinates());
//		assertNotNull(bounds2);
//		assertEquals(4, bounds2.length);
//
//		double value2 = (Math.sqrt(Math.pow(building1.getWidth(), 2D) + Math.pow(building1.getLength(),
//				2D)) / 2D) + 7D;
//
//		assertEquals(17D, bounds2[0]);
//		assertEquals(-27D, bounds2[1]);
////		assertEquals(value2, bounds2[2]);
////		assertEquals(value2 * -1D, bounds2[3]);
//
//		// Add a second building to settlement.
//		MockBuilding building2 = new MockBuilding(settlement.getBuildingManager());
//		building2.setWidth(10D);
//		building2.setLength(10D);
//		building2.setXLocation(-12D);
//		building2.setYLocation(4D);
//		building2.setFacing(0D);
//		settlement.getBuildingManager().addMockBuilding(building2);
//		settlement.getBuildingConnectorManager().createBuildingConnections(building2);
//		
//		// Clear obstacle cache.
//		LocalAreaUtil.clearObstacleCache();
//
//		double[] bounds3 = walkTask1.getLocalObstacleSearchLimits(person.getCoordinates());
//		assertNotNull(bounds3);
//		assertEquals(4, bounds3.length);
//
//		assertEquals(17D, bounds3[0]);
//		assertEquals(-27D, bounds3[1]);
////		assertEquals(16D, bounds3[2]);
////		assertEquals(value2 * -1D, bounds3[3]);
//
//		// Clear obstacle cache.
//		LocalAreaUtil.clearObstacleCache();
//	}
}