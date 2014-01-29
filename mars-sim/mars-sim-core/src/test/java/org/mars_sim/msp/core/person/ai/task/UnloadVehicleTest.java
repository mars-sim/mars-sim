/**
 * Mars Simulation Project
 * LoadVehicleTest.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import junit.framework.TestCase;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.equipment.SpecimenContainer;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.structure.MockSettlement;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.MockVehicle;
import org.mars_sim.msp.core.vehicle.Vehicle;

public class UnloadVehicleTest extends TestCase {
    @Override
    public void setUp() throws Exception {
        SimulationConfig.loadConfig();
    }

    public void testUnloadingPhase() throws Exception {
		
		AmountResource oxygen = AmountResource.findAmountResource("oxygen");
		AmountResource food = AmountResource.findAmountResource("food");
		AmountResource water = AmountResource.findAmountResource("water");
		AmountResource methane = AmountResource.findAmountResource("methane");

        String resourceName = "hammer";
        double massPerItem = 1.4D;
        ItemResource hammer = ItemResource.createItemResource(resourceName, massPerItem);
		Settlement settlement = new MockSettlement();
		Inventory settlementInv = settlement.getInventory();
		settlementInv.addAmountResourceTypeCapacity(oxygen, 100D);
		settlementInv.addAmountResourceTypeCapacity(food, 100D);
		settlementInv.addAmountResourceTypeCapacity(water, 100D);
		settlementInv.addAmountResourceTypeCapacity(methane, 100D);
		
		Vehicle vehicle = new MockVehicle(settlement);
		Inventory vehicleInv = vehicle.getInventory();
		vehicleInv.addAmountResourceTypeCapacity(oxygen, 100D);
		vehicleInv.storeAmountResource(oxygen, 100D, true);
		vehicleInv.addAmountResourceTypeCapacity(food, 100D);
		vehicleInv.storeAmountResource(food, 100D, true);
		vehicleInv.addAmountResourceTypeCapacity(water, 100D);
		vehicleInv.storeAmountResource(water, 100D, true);
		vehicleInv.addAmountResourceTypeCapacity(methane, 100D);
		vehicleInv.storeAmountResource(methane, 100D, true);
		vehicleInv.addGeneralCapacity(100D);
		vehicleInv.storeItemResources(hammer, 5);
		for (int x = 0; x < 5; x++) {
			vehicleInv.storeUnit(new SpecimenContainer(settlement.getCoordinates()));
		}
		
		Person person = new Person("test person", Person.MALE, "Earth", settlement);
		person.getNaturalAttributeManager().setAttribute(NaturalAttributeManager.STRENGTH, 100);
		
		UnloadVehicleGarage unloadVehicle = new UnloadVehicleGarage(person, vehicle);
		unloadVehicle.unloadingPhase(11D);
		unloadVehicle.unloadingPhase(12D);
		unloadVehicle.unloadingPhase(13D);
		unloadVehicle.unloadingPhase(14D);
		unloadVehicle.unloadingPhase(15D);
		unloadVehicle.unloadingPhase(16D);
		unloadVehicle.unloadingPhase(100D);
		
		assertEquals("Vehicle unloaded correctly.", 0D, vehicleInv.getTotalInventoryMass(false), 0D);
	}
}