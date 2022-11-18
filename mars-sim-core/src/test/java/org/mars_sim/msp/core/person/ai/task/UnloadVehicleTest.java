/*
 * Mars Simulation Project
 * UnloadVehicleTest.java
 * @date 2021-10-21
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.AbstractMarsSimUnitTest;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.MockSettlement;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.MockVehicle;
import org.mars_sim.msp.core.vehicle.Vehicle;

public class UnloadVehicleTest
extends AbstractMarsSimUnitTest {




    public void testUnloadingPhase() throws Exception {

		AmountResource oxygen = ResourceUtil.findAmountResource(ResourceUtil.OXYGEN);
		AmountResource food = ResourceUtil.findAmountResource(ResourceUtil.FOOD);
		AmountResource water = ResourceUtil.findAmountResource(ResourceUtil.WATER);
		AmountResource methane = ResourceUtil.findAmountResource("methane");

		Settlement settlement = new MockSettlement();
		unitManager.addUnit(settlement);

		settlement.getEquipmentInventory().addCapacity(oxygen.getID(), 100D);
		settlement.getEquipmentInventory().addCapacity(food.getID(), 100D);
		settlement.getEquipmentInventory().addCapacity(water.getID(), 100D);
		settlement.getEquipmentInventory().addCapacity(methane.getID(), 100D);

		Vehicle vehicle = new MockVehicle(settlement);
		unitManager.addUnit(vehicle);
	}
}