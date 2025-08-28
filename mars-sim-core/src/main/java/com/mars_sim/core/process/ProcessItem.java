/*
 * Mars Simulation Project
 * ProcessItem.java
 * @date 2024-02-25
 * @author Barry Evans
 */
package com.mars_sim.core.process;

import java.io.Serializable;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.equipment.Bin;
import com.mars_sim.core.equipment.BinFactory;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleFactory;
import com.mars_sim.core.vehicle.VehicleType;

/**
 * A Process input or output item.
 * Note: Ideally this should be a simple 'record' but big impact with the method name change.
 */
public class ProcessItem implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

    private static final SimLogger logger = SimLogger.getLogger(ProcessItem.class.getName());

	// Data members
	private String name;
	private ItemType type;
	private double amount;
    private int id;

    
	public ProcessItem(int id, String name, ItemType type, double amount) {
        this.name = name;
        this.type = type;
        this.amount = amount;
        this.id = id;
    }

    public int getId() {
		return id;
	}

    public String getName() {
		return name;
	}

	public ItemType getType() {
		return type;
	}

	public double getAmount() {
		return amount;
	}

    /**
     * Deposits the item into the settlement.
     * 
     * @param settlement
     * @param context The process driving the deposit
     * @param updateGoods Updated the Goods manager for this item
     */
    void deposit(Settlement settlement, ProcessInfo context, boolean updateGoods) {
		int outputId = -1;
		double outputAmount = amount;
		
		switch(type) {
			case AMOUNT_RESOURCE: {

				// Produce amount resources.
				outputId = id;
				double capacity = settlement.getRemainingCombinedCapacity(outputId);
				if (outputAmount> capacity) {
					double overAmount = amount - capacity;
					logger.severe(settlement, "Process " + context.getName() + " Not enough storage capacity to store " 
						+ Math.round(overAmount * 10.0)/10.0 + " kg " + name
							+ " from '" + name + "'.");
					outputAmount = capacity;
				}
				settlement.storeAmountResource(outputId, outputAmount);
			} break;

			case PART: {
				// Produce parts.
				outputId = id;
				Part part = ItemResourceUtil.findItemResource(outputId);
				int num = (int)outputAmount;
				double mass = num * part.getMassPerItem();
				double capacity = settlement.getCargoCapacity();
				if (mass <= capacity) {
					settlement.storeItemResource(outputId, num);
				}
				else {
					outputId = -1;
				}
			} break;

			case EQUIPMENT: {
				// Produce equipment.
				var equipmentType = EquipmentType.convertName2Enum(name);
				outputId = EquipmentType.getResourceID(equipmentType);
				int number = (int) outputAmount;
				for (int x = 0; x < number; x++) {
					EquipmentFactory.createEquipment(equipmentType, settlement);
				}

			} break;

			case BIN: {
				// Produce bins.
				int number = (int) outputAmount;
				for (int x = 0; x < number; x++) {
					Bin bin = BinFactory.createBins(name, settlement);
					settlement.addBin(bin);
				}
			} break;
		
			case VEHICLE: {
				// Produce vehicles.
				int number = (int) outputAmount;
				var unitMgr = Simulation.instance().getUnitManager();// Don't like this
				for (int x = 0; x < number; x++) {
					Vehicle v = VehicleFactory.createVehicle(unitMgr, settlement, name);
					settlement.addOwnedVehicle(v);
					outputId = VehicleType.getVehicleID(v.getVehicleType());
				}
			} break;
		}

		// Record goods benefit
		if (updateGoods) {
			if (outputId >= 0) {
				settlement.addOutput(outputId, outputAmount, context.getWorkTimeRequired());
			}
			
			Good good = GoodsUtil.getGood(outputId);
			
			if (good == null) {
				logger.severe(name + " is not a good.");
			}
			else
				// Recalculate settlement good value for the output item.
				settlement.getGoodsManager().determineGoodValue(good);
		}
	}

	@Override
	public String toString() {
		return name;
	}

    /**
     * Checks if another object is equal to this one.
     */
    public boolean equals(Object object) {
        boolean result = false;
        if (object instanceof ProcessItem item) {
            result = ((id == item.getId())
                    && type.equals(item.getType())
                    && (amount == item.getAmount()));
        }

        return result;
    }

    /**
     * Gets the hash code for this object.
     * 
     * @return hash code.
     */
    public int hashCode() {
        return name.hashCode();
    }
}
