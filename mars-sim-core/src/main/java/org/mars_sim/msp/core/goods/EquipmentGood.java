/*
 * Mars Simulation Project
 * EquipmentGood.java
 * @date 2022-06-26
 * @author Barry Evans
 */
package org.mars_sim.msp.core.goods;

import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * THis represents the attributes of how an Equipment can be traded
 */
public class EquipmentGood extends Good {
	
	private static final long serialVersionUID = 1L;
	
	private static final double EVA_SUIT_VALUE = 50;
	private static final double CONTAINER_VALUE = .1;

    private EquipmentType equipmentType;

    EquipmentGood(EquipmentType type) {
        super( type.getName(), EquipmentType.getResourceID(type));
        this.equipmentType = type;
    }

    @Override
    public GoodCategory getCategory() {
        if (equipmentType == EquipmentType.EVA_SUIT) {
            return GoodCategory.EQUIPMENT;
        }
        
        return GoodCategory.CONTAINER;
    }

    @Override
    public double getMassPerItem() {
        return EquipmentFactory.getEquipmentMass(equipmentType);
    }

    @Override
    public GoodType getGoodType() {
        if (equipmentType == EquipmentType.EVA_SUIT) {
            return GoodType.EVA;
        }
        
        return GoodType.CONTAINER;
    }

    @Override
    protected double computeCostModifier() {
        if (equipmentType == EquipmentType.EVA_SUIT) {
			return EVA_SUIT_VALUE;
		}
        return CONTAINER_VALUE;
    }

    @Override
    public double getNumberForSettlement(Settlement settlement) {
		double number = 0D;

		// Get number of the equipment in settlement storage.
		number += settlement.findNumEmptyContainersOfType(equipmentType, false);

		// Get number of equipment out on mission vehicles.
		for(Mission mission : missionManager.getMissionsForSettlement(settlement)) {
			if (mission instanceof VehicleMission) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if ((vehicle != null) && !settlement.equals(vehicle.getSettlement()))
					number += vehicle.findNumEmptyContainersOfType(equipmentType, false);
			}
		}

		// Get number of equipment carried by people on EVA.
		for(Person person : settlement.getAllAssociatedPeople()) {
			if (person.isOutside())
				number += person.findNumEmptyContainersOfType(equipmentType, false);
		}

		// Get the number of equipment that will be produced by ongoing manufacturing
		// processes.
		number += getManufacturingProcessOutput(settlement);

		return number;
    }
}
