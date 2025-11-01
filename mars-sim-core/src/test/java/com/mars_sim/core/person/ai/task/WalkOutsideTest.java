/*
 * Mars Simulation Project
 * WalkOutsideTest.java
 * @date 2025-07-22
 * @author Scott Davis
 */

package com.mars_sim.core.person.ai.task;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Settlement;

/**
 * A unit test suite for the WalkOutside task class.
 */
public class WalkOutsideTest extends AbstractMarsSimUnitTest {

	/**
	 * Check the clearPathToDestination method.
	 */
	public void testClearPath() {

		// Create test settlement.
		Settlement settlement = buildSettlement();

		// Create test person.
		Person person = buildPerson("Outsider", settlement);
		person.transfer(getSurface());
		person.setPosition(LocalPosition.DEFAULT_POSITION);
		assertTrue("Person starts outside", person.isOutside());

		LocalPosition target = new LocalPosition(-10D, 10D);
		WalkOutside outsideWalk = new WalkOutside(person, person.getPosition(), target, true);
		assertFalse("Person can do walk", outsideWalk.isDone());
		assertEquals("Waypoints direct", 2, outsideWalk.getNumberWayPoints());

	    int calls = executeTask(person, outsideWalk, 20);
		
	    assertTrue("Walk calls more than zero", (calls > 0));
		assertTrue("Person completed walk", outsideWalk.isDone());
		assertEquals("Person final position", target, person.getPosition());
		assertTrue("Person still outside", person.isOutside());
		
		// Clear obstacle cache.
		LocalAreaUtil.clearObstacleCache();
	}

	/**
	 * Test the determineObstacleAvoidancePath method.
	 */
	public void testAvoidBuilding() {
		// Create test settlement.
		Settlement settlement = buildSettlement();
	
		// Create test person.
		Person person = buildPerson("Outsider", settlement);
		person.transfer(getSurface());
		person.setPosition(LocalPosition.DEFAULT_POSITION);
		assertTrue("Person starts outside", person.isOutside());
	
		// Add a Building in the way
		buildBuilding(settlement.getBuildingManager(), new LocalPosition(1D + BUILDING_WIDTH, -BUILDING_LENGTH/2), 0D, 0);
		
		LocalPosition target = new LocalPosition(20D, 0D);
		WalkOutside outsideWalk = new WalkOutside(person, person.getPosition(), target, true);
		assertFalse("Person can do walk", outsideWalk.isDone());
		assertNotEquals(2, outsideWalk.getNumberWayPoints(), "Waypoints has a path");
		
	    int calls = executeTask(person, outsideWalk, 40);
		
	    assertTrue("Walk calls more than zero", (calls > 0));
		assertEquals("Person final position", target, person.getPosition());
		assertTrue("Person still outside", person.isOutside());
		assertTrue("Person completed walk", outsideWalk.isDone());
		
		// Clear obstacle cache.
		LocalAreaUtil.clearObstacleCache();
	}

	/**
	 * Test the determineObstacleAvoidancePath method.
	 */
	public void testAvoidRover() {
		// Create test settlement.
		Settlement settlement = buildSettlement();
	
		// Create test person.
		Person person = buildPerson("Outsider", settlement);
		person.transfer(getSurface());
		person.setPosition(LocalPosition.DEFAULT_POSITION);
		assertTrue("Person starts outside", person.isOutside());
	
		// Add a Building in the way
		buildRover(settlement, "Rover", new LocalPosition(10D, -1D));
		
		LocalPosition target = new LocalPosition(20D, 0D);
		WalkOutside outsideWalk = new WalkOutside(person, person.getPosition(), target, true);
		assertFalse("Person can do walk", outsideWalk.isDone());
		assertNotEquals(2, outsideWalk.getNumberWayPoints(), "Waypoints has a path");

	    int calls = executeTask(person, outsideWalk, 40);
		
	    assertTrue("Walk calls more than zero", (calls > 0));
		assertTrue("Person completed walk", outsideWalk.isDone());
		assertEquals("Person final position", target, person.getPosition());
		assertTrue("Person still outside", person.isOutside());
		
		// Clear obstacle cache.
		LocalAreaUtil.clearObstacleCache();
	}

	/**
	 * Test the determineObstacleAvoidancePath method.
	 */
	public void testAvoidBuildingVehicle() {
		// Create test settlement.
		Settlement settlement = buildSettlement();
	
		// Create test person.
		Person person = buildPerson("Outsider", settlement);
		person.transfer(getSurface());
		person.setPosition(LocalPosition.DEFAULT_POSITION);
		assertTrue("Person starts outside", person.isOutside());
	
		// Add a Building in the way
		buildBuilding(settlement.getBuildingManager(), new LocalPosition(10D + BUILDING_WIDTH, -BUILDING_LENGTH/2), 0D, 0);
		buildRover(settlement, "Rover", new LocalPosition(10D, -1D));
		
		LocalPosition target = new LocalPosition(30D, 0D);
		WalkOutside outsideWalk = new WalkOutside(person, person.getPosition(), target, true);
		assertFalse("Person can do walk", outsideWalk.isDone());
		assertNotEquals(2, outsideWalk.getNumberWayPoints(), "Waypoints has a path");
		
	    int calls = executeTask(person, outsideWalk, 20);
		
	    assertTrue("Walk calls more than zero", (calls > 0));
//		assertTrue("Person completed walk", outsideWalk.isDone());
//		assertEquals("Person final position", target, person.getPosition());
		assertTrue("Person still outside", person.isOutside());
		
		// Clear obstacle cache.
		LocalAreaUtil.clearObstacleCache();
	}
}