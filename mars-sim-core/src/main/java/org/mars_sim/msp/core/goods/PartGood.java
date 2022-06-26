package org.mars_sim.msp.core.goods;

import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;

class PartGood extends Good {

    public PartGood(Part p) {
        super(p.getName(), p.getID());
    }

    private Part getPart() {
        return ItemResourceUtil.findItemResource(getID());
    }

    @Override
    public GoodCategory getCategory() {
        return GoodCategory.ITEM_RESOURCE;
    }

    @Override
    public double getMassPerItem() {
        return getPart().getMassPerItem();
    }

    @Override
    public GoodType getGoodType() {
        return getPart().getGoodType();
    }
}
