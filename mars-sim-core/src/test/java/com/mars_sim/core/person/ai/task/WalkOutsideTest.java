/*
 * Mars Simulation Project
 * WalkOutsideTest.java
 * @date 2025-07-22
 * @author Scott Davis
 */

package com.mars_sim.core.person.ai.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Settlement;

/**
 * A unit test suite for the WalkOutside task class.
 */
public class WalkOutsideTest extends MarsSimUnitTest {

	/**
	 * Check the clearPathToDestination method.
	 */
	@Test
	public void testClearPath() {

		// Create test settlement.
		Settlement settlement = buildSettlement("mock");

		// Create test person.
		Person person = buildPerson("Outsider", settlement);
		person.transfer(getSurface());
		person.setPosition(LocalPosition.DEFAULT_POSITION);
		assertTrue(person.isOutside(), "Person starts outside");

		LocalPosition target = new LocalPosition(-10D, 10D);
		WalkOutside outsideWalk = new WalkOutside(person, person.getPosition(), target, true);
		assertFalse(outsideWalk.isDone(), "Person can do walk");
		assertEquals(2, outsideWalk.getNumberWayPoints(), "Waypoints direct");

	    int calls = executeTask(person, outsideWalk, 20);
		
	    assertTrue((calls > 0), "Walk calls more than zero");
		assertTrue(outsideWalk.isDone(), "Person completed walk");
		assertEquals(target, person.getPosition(), "Person final position");
		assertTrue(person.isOutside(), "Person still outside");
		
		// Clear obstacle cache.
		LocalAreaUtil.clearObstacleCache();
	}

	/**
	 * Test the determineObstacleAvoidancePath method.
	 */
	@Test
	public void testAvoidBuilding() {
		// Create test settlement.
		Settlement settlement = buildSettlement("mock");
	
		// Create test person.
		Person person = buildPerson("Outsider", settlement);
		person.transfer(getSurface());
		person.setPosition(LocalPosition.DEFAULT_POSITION);
		assertTrue(person.isOutside(), "Person starts outside");
	
		// Add a Building in the way
		buildBuilding(settlement.getBuildingManager(), new LocalPosition(1D + BUILDING_WIDTH, -BUILDING_LENGTH/2), 0D);
		
		LocalPosition target = new LocalPosition(20D, 0D);
		WalkOutside outsideWalk = new WalkOutside(person, person.getPosition(), target, true);
		assertFalse(outsideWalk.isDone(), "Person can do walk");
		assertNotEquals(2, outsideWalk.getNumberWayPoints(), "Waypoints has a path");
		
	    int calls = executeTask(person, outsideWalk, 40);
		
	    assertTrue((calls > 0), "Walk calls more than zero");
		assertEquals(target, person.getPosition(), "Person final position");
		assertTrue(person.isOutside(), "Person still outside");
		assertTrue(outsideWalk.isDone(), "Person completed walk");
		
		// Clear obstacle cache.
		LocalAreaUtil.clearObstacleCache();
	}

	/**
	 * Test the determineObstacleAvoidancePath method.
	 */
	@Test
	public void testAvoidRover() {
		// Create test settlement.
		Settlement settlement = buildSettlement("mock");
	
		// Create test person.
		Person person = buildPerson("Outsider", settlement);
		person.transfer(getSurface());
		person.setPosition(LocalPosition.DEFAULT_POSITION);
		assertTrue(person.isOutside(), "Person starts outside");
	
		// Add a Building in the way
		buildRover(settlement, "Rover", new LocalPosition(10D, -1D), EXPLORER_ROVER);
		
		LocalPosition target = new LocalPosition(20D, 0D);
		WalkOutside outsideWalk = new WalkOutside(person, person.getPosition(), target, true);
		assertFalse(outsideWalk.isDone(), "Person can do walk");
		assertNotEquals(2, outsideWalk.getNumberWayPoints(), "Waypoints has a path");

	    int calls = executeTask(person, outsideWalk, 40);
		
	    assertTrue((calls > 0), "Walk calls more than zero");
		assertTrue(outsideWalk.isDone(), "Person completed walk");
		assertEquals(target, person.getPosition(), "Person final position");
		assertTrue(person.isOutside(), "Person still outside");
		
		// Clear obstacle cache.
		LocalAreaUtil.clearObstacleCache();
	}

	/**
	 * Test the determineObstacleAvoidancePath method.
	 */
	@Test
	public void testAvoidBuildingVehicle() {
		// Create test settlement.
		Settlement settlement = buildSettlement("mock");
	
		// Create test person.
		Person person = buildPerson("Outsider", settlement);
		person.transfer(getSurface());
		person.setPosition(LocalPosition.DEFAULT_POSITION);
		assertTrue(person.isOutside(), "Person starts outside");
	
		// Add a Building in the way
		buildBuilding(settlement.getBuildingManager(), new LocalPosition(10D + BUILDING_WIDTH, -BUILDING_LENGTH/2), 0D);
		buildRover(settlement, "Rover", new LocalPosition(10D, -1D), EXPLORER_ROVER);
		
		LocalPosition target = new LocalPosition(30D, 0D);
		WalkOutside outsideWalk = new WalkOutside(person, person.getPosition(), target, true);
		assertFalse(outsideWalk.isDone(), "Person can do walk");
		assertNotEquals(2, outsideWalk.getNumberWayPoints(), "Waypoints has a path");
		
	    int calls = executeTask(person, outsideWalk, 20);
		
	    assertTrue((calls > 0), "Walk calls more than zero");
//		assertTrue(outsideWalk.isDone(), "Person completed walk");
//		assertEquals(target, person.getPosition(), "Person final position");
		assertTrue(person.isOutside(), "Person still outside");
		
		// Clear obstacle cache.
		LocalAreaUtil.clearObstacleCache();
	}
}
