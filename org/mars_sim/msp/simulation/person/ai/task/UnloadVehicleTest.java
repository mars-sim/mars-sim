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
		
		Settlement settlement = new MockSettlement();
		Inventory settlementInv = settlement.getInventory();
		settlementInv.addAmountResourceTypeCapacity(AmountResource.OXYGEN, 100D);
		settlementInv.addAmountResourceTypeCapacity(AmountResource.FOOD, 100D);
		settlementInv.addAmountResourceTypeCapacity(AmountResource.WATER, 100D);
		settlementInv.addAmountResourceTypeCapacity(AmountResource.METHANE, 100D);
		
		Vehicle vehicle = new MockVehicle(settlement);
		Inventory vehicleInv = vehicle.getInventory();
		vehicleInv.addAmountResourceTypeCapacity(AmountResource.OXYGEN, 100D);
		vehicleInv.storeAmountResource(AmountResource.OXYGEN, 100D);
		vehicleInv.addAmountResourceTypeCapacity(AmountResource.FOOD, 100D);
		vehicleInv.storeAmountResource(AmountResource.FOOD, 100D);
		vehicleInv.addAmountResourceTypeCapacity(AmountResource.WATER, 100D);
		vehicleInv.storeAmountResource(AmountResource.WATER, 100D);
		vehicleInv.addAmountResourceTypeCapacity(AmountResource.METHANE, 100D);
		vehicleInv.storeAmountResource(AmountResource.METHANE, 100D);
		vehicleInv.addGeneralCapacity(100D);
		vehicleInv.storeItemResources(ItemResource.HAMMER, 5);
		for (int x = 0; x < 5; x++) {
			vehicleInv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
		}
		
		Person person = new Person("test person", Person.MALE, settlement);
		person.getNaturalAttributeManager().setAttribute(NaturalAttributeManager.STRENGTH, 100);
		
		UnloadVehicle unloadVehicle = new UnloadVehicle(person, vehicle);
		unloadVehicle.unloadingPhase(10D);
		
		assertTrue("Vehicle unloaded correctly.", (vehicleInv.getAmountResourceStored(AmountResource.OXYGEN) == 50D));
	}
}