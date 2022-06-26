package org.mars_sim.msp.core.goods;

import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.equipment.EquipmentType;

class EquipmentGood extends Good {

    private EquipmentType equipmentType;

    public EquipmentGood(EquipmentType type) {
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
}
