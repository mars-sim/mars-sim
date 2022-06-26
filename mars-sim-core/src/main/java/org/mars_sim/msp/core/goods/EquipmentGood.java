/*
 * Mars Simulation Project
 * EquipmentGood.java
 * @date 2022-06-26
 * @author Barry Evans
 */
package org.mars_sim.msp.core.goods;

import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.equipment.EquipmentType;

/**
 * THis represents the attributes of how an Equipment can be traded
 */
public class EquipmentGood extends Good {

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
}
