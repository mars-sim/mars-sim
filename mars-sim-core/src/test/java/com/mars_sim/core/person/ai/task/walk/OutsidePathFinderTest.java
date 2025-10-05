/*
 * Mars Simulation Project
 * CollectResources.java
 * @date 2017-07-17
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai.task.walk;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Settlement;

/**
 * A unit test suite for the CollectResources task class.
 */
public class OutsidePathFinderTest extends AbstractMarsSimUnitTest {

	/**
	 * Factory method to create the path finder.
	 * @param person
	 * @param start
	 * @return
	 */
	private static OutsidePathFinder createPathFinder(Person person, LocalPosition start) {		
		// Clear obstacle cache.
		LocalAreaUtil.clearObstacleCache();

		return new CollisionPathFinder(person, start);
	}

	/**
	 * Check the clearPathToDestination method.
	 */
	public void testClearPath() {

		// Create test settlement.
		Settlement settlement = buildSettlement();

		// Create test person.
		Person person = buildPerson("Outsider", settlement);
		person.transfer(surface);
		person.setPosition(LocalPosition.DEFAULT_POSITION);
		assertTrue("Person starts outside", person.isOutside());

		LocalPosition target = new LocalPosition(-10D, 10D);
		var outsideWalk = createPathFinder(person, person.getPosition());
		var solution = outsideWalk.determineWalkingPath(target);
		assertFalse("No obstacles", solution.obstaclesInPath());
		assertEquals("Waypoints direct", 2, solution.path().size());

		assertClearPath("Clear path", person, solution.path());
	}

	private void assertClearPath(String context, Person person, List<LocalPosition> path) {
		LocalPosition from = path.get(0);
		for(int idx = 1; idx < path.size(); idx++) {
			var to = path.get(idx);
			assertTrue(context + " segment " + idx + " clear", 
						LocalAreaUtil.isLinePathCollisionFree(from, to, person.getCoordinates()));
			from = to;
		}
	}

	/**
	 * Test the determineObstacleAvoidancePath method.
	 */
	public void testAvoidBuilding() {
		// Create test settlement.
		Settlement settlement = buildSettlement();
	
		// Create test person.
		Person person = buildPerson("Outsider", settlement);
		person.transfer(surface);
		person.setPosition(LocalPosition.DEFAULT_POSITION);
		assertTrue("Person starts outside", person.isOutside());
	
		// Add a Building in the way
		buildBuilding(settlement.getBuildingManager(), new LocalPosition(1D + BUILDING_WIDTH, -BUILDING_LENGTH/2), 0D, 0);
		
		LocalPosition target = new LocalPosition(20D, 0D);
		var outsideWalk = createPathFinder(person, person.getPosition());
		var solution = outsideWalk.determineWalkingPath(target);
		assertEquals("Waypoints has a path", 3, solution.path().size());
		assertFalse("Has no obstacles in calculated path", solution.obstaclesInPath());

		assertClearPath("Avoid building", person, solution.path());

	}

	/**
	 * Test the determineObstacleAvoidancePath method.
	 */
	public void testAvoidVehicle() {
		// Create test settlement.
		Settlement settlement = buildSettlement();
	
		// Create test person.
		Person person = buildPerson("Outsider", settlement);
		person.transfer(surface);
		person.setPosition(LocalPosition.DEFAULT_POSITION);
		assertTrue("Person starts outside", person.isOutside());
	
		// Add a Building in the way
		buildRover(settlement, "Rover", new LocalPosition(10D, -1D));
		
		LocalPosition target = new LocalPosition(20D, 0D);
		var outsideWalk = createPathFinder(person, person.getPosition());
		var solution = outsideWalk.determineWalkingPath(target);
		assertFalse("Has no obstacles in calculated path", solution.obstaclesInPath());
		assertNotEquals(2, solution.path().size(), "Waypoints has a path");
		
		assertClearPath("Avoid Vehicle", person, solution.path());
	}

	/**
	 * Test the determineObstacleAvoidancePath method.
	 */
	public void testAvoidBuildingVehicle() {
		// Create test settlement.
		Settlement settlement = buildSettlement();
	
		// Create test person.
		Person person = buildPerson("Outsider", settlement);
		person.transfer(surface);
		person.setPosition(LocalPosition.DEFAULT_POSITION);
		assertTrue("Person starts outside", person.isOutside());
	
		// Add a Building in the way
		buildBuilding(settlement.getBuildingManager(), new LocalPosition(10D + BUILDING_WIDTH, -BUILDING_LENGTH/2), 0D, 0);
		buildRover(settlement, "Rover", new LocalPosition(10D, -1D));
		
		LocalPosition target = new LocalPosition(30D, 0D);
		var outsideWalk = createPathFinder(person, person.getPosition());
		var solution = outsideWalk.determineWalkingPath(target);
		assertFalse("Has no obstacles in calculated path", solution.obstaclesInPath());
		assertNotEquals(2, solution.path().size(), "Waypoints has a path");

		assertClearPath("Avoid building & Vehicle", person, solution.path());
	}
}