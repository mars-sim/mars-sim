package org.mars_sim.msp.core.goods;

import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;

class AmountResourceGood extends Good {

    AmountResourceGood(AmountResource ar) {
        super(ar.getName(), ar.getID());
    }

    @Override
    public GoodCategory getCategory() {
        return GoodCategory.AMOUNT_RESOURCE;
    }

    @Override
    public double getMassPerItem() {
        return 1D;
    }

    @Override
    public GoodType getGoodType() {
        return getAmountResource().getGoodType();

    }

    private AmountResource getAmountResource() {
        return ResourceUtil.findAmountResource(getID());
    }

}
