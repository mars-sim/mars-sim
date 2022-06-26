/*
 * Mars Simulation Project
 * AmountResourceGood.java
 * @date 2022-06-26
 * @author Barry Evans
 */
package org.mars_sim.msp.core.goods;

import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;

/**
 * This represents how a Amount Resource can be traded.
 */
class AmountResourceGood extends Good {
	
    // TODO, move these to the AmountResource class via XML config
    private static final double CO2_VALUE = 0.0001;
	private static final double CL_VALUE = 0.01;
	private static final double ICE_VALUE = 1.5;
	private static final double FOOD_VALUE = 0.1;
	private static final double DERIVED_VALUE = .07;
	private static final double SOY_VALUE = .05;
	
	private static final int CROP_VALUE = 3;
	
	private static final double ANIMAL_VALUE = .1;
	private static final double CHEMICAL_VALUE = 0.01;
	private static final double MEDICAL_VALUE = 0.001;
	private static final double WASTE_VALUE = 0.0001;
	private static final double OIL_VALUE = 0.001;
	private static final double ROCK_VALUE = 0.005;
	private static final double REGOLITH_VALUE = .02;
	private static final double ORE_VALUE = 0.03;
	private static final double MINERAL_VALUE = 0.1;
	private static final double STANDARD_AMOUNT_VALUE = 0.3;
	private static final double ELEMENT_VALUE = 0.5;
	private static final double LIFE_SUPPORT_VALUE = 1;

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

    @Override
    protected double computeCostModifier() {
        AmountResource ar =getAmountResource();
        boolean edible = ar.isEdible();
        boolean lifeSupport = ar.isLifeSupport();
        GoodType type = ar.getGoodType();
        double result = 0D;

        if (lifeSupport)
            result += LIFE_SUPPORT_VALUE;
        
        else if (edible) {
            if (type != null && type == GoodType.DERIVED)
                result += DERIVED_VALUE;
            else if (type != null && type == GoodType.SOY_BASED)
                result += SOY_VALUE;
            else if (type != null && type == GoodType.ANIMAL)
                result += ANIMAL_VALUE;
            else
                result += FOOD_VALUE;
        }
        
        else if (type != null && type == GoodType.WASTE)
            result += WASTE_VALUE ;
        
        else if (ar.getName().equalsIgnoreCase("chlorine"))
            result += CL_VALUE;
        else if (ar.getName().equalsIgnoreCase("carbon dioxide"))
            result += CO2_VALUE;
        else if (ar.getName().equalsIgnoreCase("ice"))
            result += ICE_VALUE;


        else if (type != null && type == GoodType.MEDICAL)
            result += MEDICAL_VALUE;
        else if (type != null && type == GoodType.OIL)
            result += OIL_VALUE;
        else if (type != null && type == GoodType.CROP)
            result += CROP_VALUE;
        else if (type != null && type == GoodType.ROCK)
            result += ROCK_VALUE;
        else if (type != null && type == GoodType.REGOLITH)
            result += REGOLITH_VALUE;
        else if (type != null && type == GoodType.ORE)
            result += ORE_VALUE;
        else if (type != null && type == GoodType.MINERAL)
            result += MINERAL_VALUE;
        else if (type != null && type == GoodType.ELEMENT)
            result += ELEMENT_VALUE;
        else if (type != null && type == GoodType.CHEMICAL)
            result += CHEMICAL_VALUE;
        else
            result += STANDARD_AMOUNT_VALUE ;

        return result;
    }
}
