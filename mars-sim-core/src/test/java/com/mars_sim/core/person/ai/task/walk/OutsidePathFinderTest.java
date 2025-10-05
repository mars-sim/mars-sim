/*
 * Mars Simulation Project
 * CollectResources.java
 * @date 2017-07-17
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai.task.walk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.test.MarsSimUnitTest;

/**
 * A unit test suite for the CollectResources task class.
 */
class OutsidePathFinderTest extends MarsSimUnitTest {

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
	@Test
	void testClearPath() {

		// Create test settlement.
		Settlement settlement = buildSettlement("Outside");

		// Create test person.
		Person person = buildPerson("Outsider", settlement);
		person.transfer(getMarsSurface());
		person.setPosition(LocalPosition.DEFAULT_POSITION);
		assertTrue(person.isOutside(), "Person starts outside");

		LocalPosition target = new LocalPosition(-10D, 10D);
		var outsideWalk = createPathFinder(person, person.getPosition());
		var solution = outsideWalk.determineWalkingPath(target);
		assertFalse(solution.obstaclesInPath(), "No obstacles");
		assertEquals(2, solution.path().size(), "Waypoints direct");

		assertClearPath("Clear path", person, solution.path());
	}

	private void assertClearPath(String context, Person person, List<LocalPosition> path) {
		LocalPosition from = path.get(0);
		for(int idx = 1; idx < path.size(); idx++) {
			var to = path.get(idx);
			assertTrue(LocalAreaUtil.isLinePathCollisionFree(from, to, person.getCoordinates()),
						context + " segment " + idx + " clear");
			from = to;
		}
	}

	/**
	 * Test the determineObstacleAvoidancePath method.
	 */
	@Test
	void testAvoidBuilding() {
		// Create test settlement.
		Settlement settlement = buildSettlement("AvoidBuilding");
	
		// Create test person.
		Person person = buildPerson("Outsider", settlement);
		person.transfer(getMarsSurface());
		person.setPosition(LocalPosition.DEFAULT_POSITION);
		assertTrue(person.isOutside(), "Person starts outside");
	
		// Add a Building in the way
		buildBuilding(settlement.getBuildingManager(), new LocalPosition(15, -5));
		
		LocalPosition target = new LocalPosition(20D, 0D);
		var outsideWalk = createPathFinder(person, person.getPosition());
		var solution = outsideWalk.determineWalkingPath(target);
		assertEquals(2, solution.path().size(), "Waypoints has a path");
		assertFalse(solution.obstaclesInPath(), "Has no obstacles in calculated path");

		assertClearPath("Avoid building", person, solution.path());

	}

	/**
	 * Test the determineObstacleAvoidancePath method.
	 */
	@Test
	void testAvoidVehicle() {
		// Create test settlement.
		Settlement settlement = buildSettlement("AvoidVehicle");
	
		// Create test person.
		Person person = buildPerson("Outsider", settlement);
		person.transfer(getMarsSurface());
		person.setPosition(LocalPosition.DEFAULT_POSITION);
		assertTrue(person.isOutside(), "Person starts outside");

		// Add a Building in the way
		buildRover(settlement, "Rover", new LocalPosition(10D, -1D), EXPLORER_ROVER);
		
		LocalPosition target = new LocalPosition(20D, 0D);
		var outsideWalk = createPathFinder(person, person.getPosition());
		var solution = outsideWalk.determineWalkingPath(target);
		assertFalse(solution.obstaclesInPath(), "Has no obstacles in calculated path");
		assertNotEquals(2, solution.path().size(), "Waypoints has a path");
		
		assertClearPath("Avoid Vehicle", person, solution.path());
	}

	/**
	 * Test the determineObstacleAvoidancePath method.
	 */
	@Test
	 void testAvoidBuildingVehicle() {
		// Create test settlement.
		Settlement settlement = buildSettlement("BuildingVehicle");
	
		// Create test person.
		Person person = buildPerson("Outsider", settlement);
		person.transfer(getMarsSurface());
		person.setPosition(new LocalPosition(-10, -10));
		assertTrue(person.isOutside(), "Person starts outside");

		// Add a Building in the way
		buildBuilding(settlement.getBuildingManager(), new LocalPosition(20D, -5));
		buildRover(settlement, "Rover", new LocalPosition(10D, -1D), EXPLORER_ROVER);

		LocalPosition target = new LocalPosition(30D, 20D);
		var outsideWalk = createPathFinder(person, person.getPosition());
		var solution = outsideWalk.determineWalkingPath(target);
		assertFalse(solution.obstaclesInPath(), "Has no obstacles in calculated path");
		assertEquals(2, solution.path().size(), "Waypoints has a path");

		assertClearPath("Avoid building & Vehicle", person, solution.path());
	}
}