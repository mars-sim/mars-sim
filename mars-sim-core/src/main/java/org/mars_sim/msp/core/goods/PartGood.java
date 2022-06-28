/*
 * Mars Simulation Project
 * PartGood.java
 * @date 2022-06-26
 * @author Barry Evans
 */
package org.mars_sim.msp.core.goods;

import java.util.stream.Collectors;

import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.Settlement;

/*
 * This class is the representation of a Part instance as a Good that is tradable.
 */
class PartGood extends Good {
	
	private static final long serialVersionUID = 1L;
    	
	private static final int VEHICLE_PART_VALUE = 3;
	private static final double ITEM_VALUE = 1.1D;
	private static final double FC_STACK_VALUE = 8;
	private static final double FC_VALUE = 1;
	private static final double BOARD_VALUE = 1;
	private static final double CPU_VALUE = 10;
	private static final double WAFER_VALUE = 50;
	private static final double BATTERY_VALUE = 2;
	private static final double INSTRUMENT_VALUE = 1;
	private static final double WIRE_VALUE = .005;
	private static final double ELECTRONIC_VALUE = .5;

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

    /**
	 * Computes the cost modifier for calculating output cost.
	 * 
	 * @return
	 */
    @Override
    protected double computeCostModifier() {
        Part part = getPart();
        String name = part.getName().toLowerCase();
        
        if (name.contains("wire"))
            return WIRE_VALUE;
        
        GoodType type = part.getGoodType();
        
        if (type == GoodType.VEHICLE)
            return VEHICLE_PART_VALUE;
        
        else if (type == GoodType.ELECTRONIC)
            return ELECTRONIC_VALUE;
        
        else if (type == GoodType.INSTRUMENT)
            return INSTRUMENT_VALUE;
        
        if (name.equalsIgnoreCase("fuel cell stack"))
            return FC_STACK_VALUE;
        else if (name.equalsIgnoreCase("solid oxide fuel cell"))
            return FC_VALUE;
        else if (name.contains("board"))
            return BOARD_VALUE;
        else if (name.equalsIgnoreCase("microcontroller"))
            return CPU_VALUE;
        else if (name.equalsIgnoreCase("semiconductor wafer"))
            return WAFER_VALUE;
        else if (name.contains("battery"))
            return BATTERY_VALUE;
        
        return ITEM_VALUE;
    }

    @Override
    public double getNumberForSettlement(Settlement settlement) {
		double number = 0D;

		// Get number of resources in settlement storage.
		number += settlement.getItemResourceStored(getID());

		// Get number of resources out on mission vehicles.
        number += getVehiclesOnMissions(settlement)
               .map(v -> v.getItemResourceStored(getID()))
               .collect(Collectors.summingInt(Integer::intValue));

		// Get number of resources carried by people on EVA.
        number += getPersonOnEVA(settlement)
                    .map(p -> p.getItemResourceStored(getID()))
                    .collect(Collectors.summingInt(Integer::intValue));

		// Get the number of resources that will be produced by ongoing manufacturing
		// processes.
		number += getManufacturingProcessOutput(settlement);

		return number;
    }
}
