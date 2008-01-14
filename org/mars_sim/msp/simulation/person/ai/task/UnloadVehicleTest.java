package org.mars_sim.msp.simulation.person.ai.task;

import junit.framework.TestCase;

import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.equipment.SpecimenContainer;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.NaturalAttributeManager;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.ItemResource;
import org.mars_sim.msp.simulation.structure.MockSettlement;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.MockVehicle;
import org.mars_sim.msp.simulation.vehicle.Vehicle;

public class UnloadVehicleTest extends TestCase {

	public void testUnloadingPhase() throws Exception {
		
		ItemResource hammer = ItemResource.getTestResourceHammer();
		Settlement settlement = new MockSettlement();
		Inventory settlementInv = settlement.getInventory();
		settlementInv.addAmountResourceTypeCapacity(AmountResource.OXYGEN, 100D);
		settlementInv.addAmountResourceTypeCapacity(AmountResource.FOOD, 100D);
		settlementInv.addAmountResourceTypeCapacity(AmountResource.WATER, 100D);
		settlementInv.addAmountResourceTypeCapacity(AmountResource.METHANE, 100D);
		
		Vehicle vehicle = new MockVehicle(settlement);
		Inventory vehicleInv = vehicle.getInventory();
		vehicleInv.addAmountResourceTypeCapacity(AmountResource.OXYGEN, 100D);
		vehicleInv.storeAmountResource(AmountResource.OXYGEN, 100D, true);
		vehicleInv.addAmountResourceTypeCapacity(AmountResource.FOOD, 100D);
		vehicleInv.storeAmountResource(AmountResource.FOOD, 100D, true);
		vehicleInv.addAmountResourceTypeCapacity(AmountResource.WATER, 100D);
		vehicleInv.storeAmountResource(AmountResource.WATER, 100D, true);
		vehicleInv.addAmountResourceTypeCapacity(AmountResource.METHANE, 100D);
		vehicleInv.storeAmountResource(AmountResource.METHANE, 100D, true);
		vehicleInv.addGeneralCapacity(100D);
		vehicleInv.storeItemResources(hammer, 5);
		for (int x = 0; x < 5; x++) {
			vehicleInv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
		}
		
		Person person = new Person("test person", Person.MALE, settlement);
		person.getNaturalAttributeManager().setAttribute(NaturalAttributeManager.STRENGTH, 100);
		
		UnloadVehicle unloadVehicle = new UnloadVehicle(person, vehicle);
		unloadVehicle.unloadingPhase(11D);
		unloadVehicle.unloadingPhase(12D);
		unloadVehicle.unloadingPhase(13D);
		unloadVehicle.unloadingPhase(14D);
		unloadVehicle.unloadingPhase(15D);
		unloadVehicle.unloadingPhase(16D);
		unloadVehicle.unloadingPhase(100D);
		
		assertEquals("Vehicle unloaded correctly.", 0D, vehicleInv.getTotalInventoryMass(), 0D);
	}
}