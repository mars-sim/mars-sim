/*
 * Mars Simulation Project
 * TestContainment.java
 * @date 2025-07-25
 * @author Scott Davis
 */

package com.mars_sim.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.VehicleMaintenance;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.location.LocationStateType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.unit.AbstractMobileUnit;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;


class TestContainment extends MarsSimUnitTest {

	private VehicleMaintenance garage;
	private Settlement settlement;

	@BeforeEach
	@Override
    public void init() {
		super.init();
              
		settlement = buildSettlement("Test Containment");
        
		garage = buildGarage(settlement.getBuildingManager(), new LocalPosition(0, 0), 0D);
    }

	private void assertContainment(AbstractMobileUnit source, Unit container, LocationStateType lon) {
		assertEquals(lon, source.getLocationStateType());
		assertEquals(container, source.getContainerUnit());
	}

	private static void assertInsideSettlement(String msg, AbstractMobileUnit source, Settlement base) {
		assertEquals(LocationStateType.INSIDE_SETTLEMENT, source.getLocationStateType(), msg + ": Location state type");
		assertEquals(base, source.getSettlement(), msg + ": Settlement");
		
		assertTrue(source.isInSettlement(), msg + ": InSettlement");
		assertTrue(source.isInside(), msg + ": IsInside");
		assertFalse(source.isOutside(), msg + ": IsOutside");

		assertFalse(source.isInVehicle(), msg + ": isInVehicle");
		assertNull(source.getVehicle(), msg + ": Vehicle");

		assertEquals(base, source.getContainerUnit(), msg + ": Container");
	}

	/**
	 * Tests condition of a vehicle parked in the vicinity of a settlement.
	 * 
	 * @param msg
	 * @param source
	 * @param base
	 */
	private void assertVehicleParked(String msg, Vehicle source, Settlement base) {
		
		assertEquals(LocationStateType.SETTLEMENT_VICINITY, source.getLocationStateType(), msg + ": Location state type");
		assertEquals(base, source.getSettlement(), msg + ": Settlement");
		
		assertTrue(source.isInSettlement(), msg + ": InSettlement");
		assertFalse(source.isInside(), msg + ": IsInside");
		assertTrue(source.isOutside(), msg + ": IsOutside");

		assertFalse(source.isInVehicle(), msg + ": isInVehicle");
		assertNull(source.getVehicle(), msg + ": Vehicle");
		
		assertEquals(base, source.getContainerUnit(), msg + ": Container");
	}

	/**
	 * Tests condition of a vehicle garaged inside a settlement.
	 * 
	 * @param msg
	 * @param source
	 * @param base
	 */
	private void assertVehicleGaraged(String msg, Vehicle source, Settlement base) {
		
		// If a vehicle is in a garage, then it's local state 
		boolean isInGarage = source.isInGarage();
		
		LocationStateType state = LocationStateType.SETTLEMENT_VICINITY;
		if (isInGarage)
			state = LocationStateType.INSIDE_SETTLEMENT;
	
		assertEquals(state, source.getLocationStateType(), msg + ": Location state type");
		
		assertEquals(base, source.getSettlement(), msg + ": Settlement");
		
		assertTrue(source.isInSettlement(), msg + ": InSettlement");
		
		if (isInGarage) {
			assertTrue(source.isInside(), msg + ": IsInside");
			assertFalse(source.isOutside(), msg + ": IsOutside");
		}
		else {
			assertFalse(source.isInside(), msg + ": IsInside");
			assertTrue(source.isOutside(), msg + ": IsOutside");
		}
			
		assertFalse(source.isInVehicle(), msg + ": isInVehicle");
		
		assertNull(source.getVehicle(), msg + ": Vehicle");
		assertEquals(isInGarage, source.isInGarage(), msg + ": isGaraged");
		
		assertEquals(base, source.getContainerUnit(), msg + ": Container");
	}
	
	private static void assertInBuilding(String msg, Person source, Building base, Settlement home) {
		assertInsideSettlement(msg, source, home);
		assertEquals(base, source.getBuildingLocation(), msg + ": Building");
	}

	
	
	private static void assertInVehicle(String msg, Person source, Vehicle vehicle) {
		assertEquals(LocationStateType.INSIDE_VEHICLE, source.getLocationStateType(), msg + ": person's location state type is INSIDE_VEHICLE");
		
		assertTrue(source.isInVehicleInGarage(), msg + ": isInVehicleInGarage");
		
		assertFalse(source.isInSettlement(), msg + ": InSettlement");
		
		assertTrue(source.isInside(), msg + ": IsInside");
		assertFalse(source.isOutside(), msg + ": IsOutside");

		assertTrue(source.isInVehicle(), msg + ": isInVehicle");
		assertEquals(vehicle, source.getVehicle(), msg + ": Vehicle");
		
		assertEquals(vehicle, source.getContainerUnit(), msg + ": Container");
	}

	private static void assertInVehicle(String msg, Equipment source, Vehicle vehicle) {
		assertEquals(LocationStateType.INSIDE_VEHICLE, source.getLocationStateType(), msg + ": bag's location state type is INSIDE_VEHICLE");
		assertNull(source.getSettlement(), msg + ": bag is still in settlement as vehicle is in settlement");
		
		assertFalse(source.isInVehicleInGarage(), msg + ": isInVehicleInGarage");
		assertFalse(source.isInSettlement(), msg + ": InSettlement");
		
		assertTrue(source.isInside(), msg + ": IsInside");
		assertFalse(source.isOutside(), msg + ": IsOutside");

		assertTrue(source.isInVehicle(), msg + ": isInVehicle");
		assertEquals(vehicle, source.getVehicle(), msg + ": Vehicle");
		
		assertEquals(vehicle, source.getContainerUnit(), msg + ": Container");
	}
	
	private void assertWithinSettlementVicinity(String msg, AbstractMobileUnit source) {
		
		assertFalse(source.isInSettlement(), msg + ": InSettlement");
		assertNull(source.getSettlement(), msg + ": Settlement");
		assertFalse(source.isInside(), msg + ": IsInside");
		assertTrue(source.isOutside(), msg + ": IsOutside");
		assertFalse(source.isInVehicle(), msg + ": isInVehicle");
		assertNull(source.getVehicle(), msg + ": Vehicle");
		
		assertEquals(getMarsSurface(), source.getContainerUnit(), msg + ": Container");
	}

	
	/*
	 * Test method for 'com.mars_sim.simulation.person.ai.task.LoadVehicle.isFullyLoaded()'
	 */
	@Test
	public void testPersonInGarage() {
		var person = buildPerson("Worker One", settlement);

		assertInsideSettlement("Initial person", person, settlement);
		
		person.setCurrentBuilding(garage.getBuilding());
		
		assertInBuilding("Person in garage", person, garage.getBuilding(), settlement);
	}
	
	/*
	 * Test method for 'com.mars_sim.simulation.person.ai.task.LoadVehicle.isFullyLoaded()'
	 */
	@Test
	public void testVehicleInGarage() {
		Vehicle vehicle = buildRover(settlement, "Garage Rover", new LocalPosition(1,1), EXPLORER_ROVER);
        
		// Since garage has been built at constructor, once the vehicle is built, 
		// it goes into a garage automatically 
		
		assertVehicleGaraged("Vehicle in garage", vehicle, settlement);
	
		boolean toRemove = BuildingManager.removeFromGarage(vehicle);
			
		assertVehicleParked("Initial Vehicle", vehicle, settlement);
		
		assertTrue(toRemove, "Vehicle parking outside");

	}

	/*
	 * Test method for 'com.mars_sim.simulation.person.ai.task.LoadVehicle.isFullyLoaded()'
	 */
	@Test
	public void testVehicleNearSettlement() {
		Vehicle vehicle = buildRover(settlement, "Near Rover", new LocalPosition(1,1), EXPLORER_ROVER);
		
		// Vehicle leaves garage
		BuildingManager.removeFromGarage(vehicle);

		vehicle.transfer(settlement);

		// Since garage has been built at constructor, once the vehicle is built, 
		// it goes into a garage automatically 
		
		boolean isInGarage = vehicle.isInGarage();
		System.out.println("In testVehicleNearSettlement(), isInGarage: " + isInGarage);
		
		assertVehicleGaraged("Vehicle in garage", vehicle, settlement);
		
		LocationStateType state = LocationStateType.SETTLEMENT_VICINITY;
		if (isInGarage)
			state = LocationStateType.INSIDE_SETTLEMENT;
		
		assertContainment(vehicle, settlement, state);
	}
	
	/*
	 * Test method for 'com.mars_sim.simulation.person.ai.task.LoadVehicle.isFullyLoaded()'
	 */
	@Test
	public void testVehicleOnSurface() {
		Vehicle vehicle = buildRover(settlement, "Garage Rover", new LocalPosition(1,1), EXPLORER_ROVER);

		assertTrue(vehicle.transfer(getMarsSurface()),
					"Transfer to Mars surface but still within settlement vicinity");

		assertWithinSettlementVicinity("After transfer from Settlement", vehicle);
	}

	/*
	 * Test method for 'com.mars_sim.simulation.person.ai.task.LoadVehicle.isFullyLoaded()'
	 */
	@Test
	public void testBagOnSurface() {

		Equipment bag = EquipmentFactory.createEquipment(EquipmentType.BAG, settlement);
		
		assertInsideSettlement("Initial equipment", bag, settlement);

		assertTrue(bag.transfer(getMarsSurface()), "Transfer to Mars surface but still within settlement vicinity");
		assertWithinSettlementVicinity("in a settlement vicinity", bag);
		
		assertTrue(bag.transfer(settlement), "Transfer to settlement");
		assertInsideSettlement("After return", bag, settlement);

	}
	
	/*
	 * Test method for 'com.mars_sim.simulation.person.ai.task.LoadVehicle.isFullyLoaded()'
	 */
	@Test
	public void testBagOnVehicle() {

		Equipment bag = EquipmentFactory.createEquipment(EquipmentType.BAG, settlement);
		
		assertInsideSettlement("Initial equipment", bag, settlement);

		Vehicle vehicle = buildRover(settlement, "Garage Rover", new LocalPosition(1,1), EXPLORER_ROVER);

        // Vehicle leaves garage
        BuildingManager.removeFromGarage(vehicle);

		assertTrue(bag.transfer(vehicle), "Vehicle leaving garage. Transfer bag from settlement to vehicle");
		
		assertTrue(bag.getContainerUnit() instanceof Rover, "Bag's container unit");
		
		assertInVehicle("In vehicle", bag, vehicle);
		
		assertTrue(bag.transfer(settlement), "Transfer bag from vehicle back to settlement");
		assertInsideSettlement("After return", bag, settlement);

	}

	
	/*
	 * Test method for 'com.mars_sim.simulation.person.ai.task.LoadVehicle.isFullyLoaded()'
	 */
	@Test
	public void testPersonOnVehicle() {

		Person person = buildPerson("Test Person", settlement);
		
		assertInsideSettlement("Initial Person", person, settlement);

		Rover vehicle = buildRover(settlement, "Person Rover", new LocalPosition(1,1), EXPLORER_ROVER);

		assertTrue(person.transfer(vehicle), "Transfer person from settlement to vehicle");
		// Note that once the vehicle is built, it goes to a garage by default
		assertInVehicle("In vehicle", person, vehicle);
		
		assertEquals(LocationStateType.INSIDE_VEHICLE, person.getLocationStateType(), "Person's location state type is INSIDE_VEHICLE");

		assertTrue(vehicle.getCrew().contains(person), "Person in crew");
		assertFalse(person.isInSettlement(), "Person in a vehicle. Person is not considered to be in a settlement");
		assertTrue(vehicle.isInSettlement(), "Vehicle still in a settlement");
		
		// Vehicle going into a garage
		settlement.getBuildingManager().addToGarageBuilding(vehicle);
		assertTrue(vehicle.isInGarage(), "Vehicle has entered a garage");
		assertInsideSettlement("Vehicle still in a settlement", vehicle, settlement);

		assertEquals(LocationStateType.INSIDE_SETTLEMENT, vehicle.getLocationStateType(), "vehicle location state type is INSIDE_SETTLEMENT");
		assertEquals(LocationStateType.INSIDE_VEHICLE, person.getLocationStateType(), "Person's location state type is INSIDE_VEHICLE");
		
        // Vehicle leaves garage
        BuildingManager.removeFromGarage(vehicle);
        
		assertEquals(LocationStateType.INSIDE_VEHICLE, person.getLocationStateType(), "Person's location state type is INSIDE_VEHICLE");
		
		assertFalse(vehicle.isInGarage(), "Vehicle has left garage");
	
		assertTrue(person.isInside(), "person is inside");
	
		assertTrue(vehicle.getCrew().contains(person), "Person in crew");
		
		assertTrue(person.transfer(settlement), "Transfer person from vehicle back to settlement");
		
		assertInsideSettlement("After return", person, settlement);
	}

	
	/*
	 * Test method for 'com.mars_sim.simulation.person.ai.task.LoadVehicle.isFullyLoaded()'
	 */
//	public void testEVAOnPerson() {
//		Person person = new Person("Worker Two", settlement);
//		person.initialize(); // TODO This is bad. Why do we have to call a 2nd method after the constructor ???
//        unitManager.addUnit(person);
//
//        person.transfer(settlement,  surface);
//
//		EVASuit suit = new EVASuit("EVA Suit", settlement);
//		unitManager.addUnit(suit);
//		assertTrue("transfer suit to Person", suit.transfer(settlement, person));
//
//		assertEquals("Location state type", LocationStateType.ON_PERSON_OR_ROBOT, suit.getLocationStateType());
//		assertNull("Settlement", suit.getSettlement());
//		
//		assertFalse("InSettlement", suit.isInSettlement());
//		assertFalse("IsInside", suit.isInside());
//		assertFalse("IsOutside", suit.isOutside());
//
//		assertFalse("isInVehicle", suit.isInVehicle());
//		assertNull("Vehicle", suit.getVehicle());
//		
//		assertEquals("Container", person, suit.getContainerUnit());
//		assertEquals("Top container", person, suit.getTopContainerUnit());
//	}
}