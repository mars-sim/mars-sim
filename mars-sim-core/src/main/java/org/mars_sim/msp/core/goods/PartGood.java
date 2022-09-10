/*
 * Mars Simulation Project
 * PartGood.java
 * @date 2022-06-26
 * @author Barry Evans
 */
package org.mars_sim.msp.core.goods;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.food.FoodProductionProcessInfo;
import org.mars_sim.msp.core.food.FoodProductionProcessItem;
import org.mars_sim.msp.core.food.FoodProductionUtil;
import org.mars_sim.msp.core.manufacture.ManufactureProcessInfo;
import org.mars_sim.msp.core.manufacture.ManufactureProcessItem;
import org.mars_sim.msp.core.manufacture.ManufactureUtil;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ItemType;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStage;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.core.structure.construction.ConstructionUtil;
import org.mars_sim.msp.core.structure.construction.ConstructionValues;
import org.mars_sim.msp.core.time.MarsClock;

/*
 * This class is the representation of a Part instance as a Good that is tradable.
 */
class PartGood extends Good {
	
	private static final long serialVersionUID = 1L;
    
	private static final String FIBERGLASS = "fiberglass";
	private static final String SCRAP = "scrap";
	private static final String INGOT = "ingot";
	private static final String SHEET = "sheet";
	private static final String TRUSS = "steel truss";
	private static final String STEEL = "steel";
	private static final String BRICK = "brick";
	private static final String HEAT_PROBE = "heat probe";
	private static final String BOTTLE = "bottle";
	private static final String GLASS_TUBE = "glass";
	private static final String GLASS_SHEET = "glass sheet";
	private static final String DRILL = "drill";

	private static final double DRILL_DEMAND  = 12;
	private static final double BOTTLE_DEMAND = .02;
	private static final double FIBERGLASS_DEMAND = .01;
	private static final double VEHICLE_PART_DEMAND = 4;
	private static final double EVA_PART_DEMAND = 7;
    private static final double KITCHEN_DEMAND = 1.5;
	private static final double SCRAP_METAL_DEMAND = .01;
	private static final double INGOT_METAL_DEMAND = .01;
	private static final double SHEET_METAL_DEMAND = .1;
	private static final double TRUSS_DEMAND = .05;
	private static final double STEEL_DEMAND = .1;
	private static final double BRICK_DEMAND = .005;
	private static final double ELECTRICAL_DEMAND = 70;
	private static final double INSTRUMENT_DEMAND = 60;
	private static final double METALLIC_DEMAND = 30;
	private static final double UTILITY_DEMAND = 50;
	private static final double TOOL_DEMAND = 40;
	private static final double CONSTRUCTION_DEMAND = .8;
	private static final double GLASS_SHEET_DEMAND = .1;
	private static final double GLASS_TUBE_DEMAND  = 80;
	private static final double ITEM_DEMAND = 1;
	
	
	private static final double CONSTRUCTION_SITE_REQUIRED_PART_FACTOR = 100D;

	private static final int VEHICLE_PART_COST = 3;
	private static final int EVA_PARTS_VALUE = 20;
	private static final double ATTACHMENT_PARTS_DEMAND = 20;

	// Cost modifiers
	private static final double ITEM_COST = 1.1D;
	private static final double FC_STACK_COST = 8;
	private static final double FC_COST = 1;
	private static final double BOARD_COST = 1;
	private static final double CPU_COST = 10;
	private static final double WAFER_COST = 50;
	private static final double BATTERY_COST = 5;
	private static final double INSTRUMENT_COST = 1;
	private static final double WIRE_COST = .005;
	private static final double ELECTRONIC_COST = .5;
	
    private static final double INITIAL_PART_DEMAND = 1;
	private static final double INITIAL_PART_SUPPLY = 0;

	private static final double MANUFACTURING_INPUT_FACTOR = 2D;

	private double flattenDemand;
	private double flattenRawDemand;
	private double costModifier;

    public PartGood(Part p) {
        super(p.getName(), p.getID());

		// Pre-calculate the fixed values
		flattenDemand = calculateFlattenPartDemand(p);
		flattenRawDemand = calculateFlattenRawPartDemand(p);
		costModifier = calculateCostModifier(p);
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
		return costModifier;
	}

	/**
	 * Calculate the cost modifier
	 */
	private static double calculateCostModifier(Part part) {
        String name = part.getName().toLowerCase();
        
        if (name.contains("wire"))
            return WIRE_COST;
        
        GoodType type = part.getGoodType();
        
        if (name.contains("battery"))
            return BATTERY_COST;    
        if (type == GoodType.VEHICLE)
            return VEHICLE_PART_COST;     
        if (type == GoodType.ELECTRONIC)
            return ELECTRONIC_COST;      
        if (type == GoodType.INSTRUMENT)
            return INSTRUMENT_COST;  
        
        if (name.equalsIgnoreCase("stack"))
            return FC_STACK_COST;
        else if (name.equalsIgnoreCase("fuel cell"))
            return FC_COST;
        else if (name.contains("board"))
            return BOARD_COST;
        else if (name.equalsIgnoreCase("microcontroller"))
            return CPU_COST;
        else if (name.equalsIgnoreCase("semiconductor wafer"))
            return WAFER_COST;

        return ITEM_COST;
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

    @Override
    double getPrice(Settlement settlement, double value) {
        double mass = getPart().getMassPerItem();
        double quantity = settlement.getItemResourceStored(getID()) ;
        double factor = 1.2 * Math.log(mass + 1) / (1.2 + Math.log(quantity + 1));
        return getCostOutput() * (1 + 5 * factor * Math.log(Math.sqrt(value)/2.0 + 1));
    }

    @Override
    double getDefaultDemandValue() {
        return INITIAL_PART_DEMAND;
    }

    @Override
    double getDefaultSupplyValue() {
        return INITIAL_PART_SUPPLY;
    }

    @Override
    void refreshSupplyDemandValue(GoodsManager owner) {
		int id = getID();
		double previousDemand = owner.getDemandValue(this);
		Settlement settlement = owner.getSettlement();

		double totalDemand = 0;
		double average = 0;
		double totalSupply = 0;

		Part part = getPart();

		average = getAverageItemDemand(owner);

		// Get demand for a part.
		// NOTE: the following estimates are for each orbit (Martian year) :
		double projected = 
			// Add manufacturing demand.					
			getPartManufacturingDemand(owner, settlement, part)
			// Add food production demand.
			+ getPartFoodProductionDemand(owner, settlement, part)
			// Add construction demand.
			+ getPartConstructionDemand(owner, settlement)
			// Add construction site demand.
			+ getPartConstructionSiteDemand(settlement)
			// Calculate individual EVA suit-related part demand.
			+ getEVASuitPartsDemand(owner)
			// Calculate individual attachment part demand.
			+ getAttachmentPartsDemand(owner)
			// Calculate kitchen part demand.
			+ getKitchenPartDemand(owner)
			// Calculate vehicle part demand.
			+ getVehiclePartDemand(owner)
			// Calculate battery cell part demand.
			+ geFuelCellDemand(owner);

			
		projected = projected
			// Flatten raw part demand.
			* flattenRawDemand
			// Flatten certain part demand.
			* flattenDemand;

		// Add trade demand.
		double trade = owner.determineTradeDemand(this);

		// Recalculate the partsDemandCache
        // TODO Why is this doing it for ALL Parts if just a single Part is being processed; that means
        // it becomes N*N calculations
		//determineRepairPartsDemand();

		// Gets the repair part demand
		double repair = owner.getDemandValue(this);

		if (previousDemand == 0) {
			// At the start of the sim
			totalDemand = (
					.1 * repair 
					+ .4 * average 
					+ .4 * projected 
					+ .1 * trade);
		}

		else {
			// Intentionally lose a tiny percentage (e.g. 1%) of its value
			totalDemand = (
					  .9894 * previousDemand 
					+ .0001 * repair 
					+ .0001 * average 
					+ .0002 * projected 
					+ .0002 * trade); 
		}
		
		// Save the goods demand
		owner.setDemandValue(this, totalDemand);
		
		// Calculate total supply
		totalSupply = getAverageItemSupply(settlement.getItemResourceStored(id));

		// Save the average supply
		owner.setSupplyValue(this, totalSupply);
    }

    /**
	 * Gets the total supply for the item resource.
	 *
	 * @param resource
	 * @param supplyStored
	 * @param solElapsed
	 * @return
	 */
	private static double getAverageItemSupply(double supplyStored) {
		return Math.sqrt(1 + supplyStored);
	}

	/**
	 * Limits the demand for a particular raw material part.
	 *
	 * @param part   the part.
	 * TODO Replace this with value off Part class which comes form the XML file
	 */
	private static double calculateFlattenRawPartDemand(Part part) {
		String name = part.getName();
		// Reduce the demand on the steel/aluminum scrap metal
		// since they can only be produced by salvaging a vehicle
		// therefore it's not reasonable to have high VP

		if (name.contains(SCRAP))
			return SCRAP_METAL_DEMAND;
		// May recycle the steel/AL scrap back to ingot
		// Note: the VP of a scrap metal could be heavily influence by VP of regolith

		if (name.contains(INGOT))
			return INGOT_METAL_DEMAND;

		if (name.contains(GLASS_SHEET))
			return GLASS_SHEET_DEMAND;
		
		if (name.contains(GLASS_TUBE))
			return GLASS_TUBE_DEMAND;
		
		if (name.contains(SHEET))
			return SHEET_METAL_DEMAND;

		if (name.contains(TRUSS))
			return TRUSS_DEMAND;

		if (name.contains(STEEL))
			return STEEL_DEMAND;

		if (name.contains(FIBERGLASS))
			return FIBERGLASS_DEMAND;

		if (name.equalsIgnoreCase(BRICK))
			return BRICK_DEMAND;

		
		return ITEM_DEMAND;
	}

	/**
	 * Calculate the part demand based on types.
	 * 
	 * @param part
	 * @return
	 */
	private static double calculateFlattenPartDemand(Part part) {
		String name = part.getName();
		GoodType type = part.getGoodType();

		if (name.contains("pipe"))
			return 1;
		
		if (name.contains("valve"))
			return .05;

		if (name.contains("plastic"))
			return 1.1;
		
		// Note that there are 'plastic pipe', 'plastic sheet', 'plastic tubing'
		if (name.contains("tank"))
			return .1;
		
		if (name.contains(HEAT_PROBE))
			return .05;

		if (name.contains(BOTTLE))
			return BOTTLE_DEMAND;

		if (name.contains("duct"))
			return .1;

		if (name.contains("gasket"))
			return .1;
		
		if (type == GoodType.ELECTRICAL) {
			if (name.contains("light")
				|| name.contains("resistor")
				|| name.contains("capacitor")
				|| name.contains("diode")) {
				return 10;
			}
			else if (name.contains("electrical wire")
					|| name.contains("wire connector"))
				return .25;
			else if (name.contains("steel wire"))
				return 10;
			else if (name.contains("wire"))
				return .05;
			return ELECTRICAL_DEMAND;
		}

		if (type == GoodType.INSTRUMENT)
			return INSTRUMENT_DEMAND;

		if (type == GoodType.METALLIC)
			return  METALLIC_DEMAND;
		
		if (type == GoodType.UTILITY) {
			if (name.contains(FIBERGLASS)) {
				return FIBERGLASS_DEMAND;
			}
			return UTILITY_DEMAND;
		}
		
		if (type == GoodType.TOOL) {
			if (name.contains(DRILL)) {
				return DRILL_DEMAND;
			}
			return TOOL_DEMAND;
		}

		if (type == GoodType.CONSTRUCTION)
			return CONSTRUCTION_DEMAND;

		if (type == GoodType.EVA)
			return EVA_PART_DEMAND;
		
		return 1;
	}

    /**
	 * Gets the new item demand.
	 *
	 * @param resource
	 * @param solElapsed
	 * @return
	 */
	private double getAverageItemDemand(GoodsManager owner) {
		return owner.getDemandValue(this);
	}

	/**
	 * Gets the attachment part demand.
	 *
	 * @param part the part.
	 * @return demand
	 */
	private double getAttachmentPartsDemand(GoodsManager owner) {
		if (ItemResourceUtil.ATTACHMENTS_ID.contains(getID())) {
			return ATTACHMENT_PARTS_DEMAND * (1 + owner.getDemandValue(this));
		}
		return 0;
	}


	/**
	 * Gets the eva related demand for a part.
	 *
	 * @param part the part.
	 * @return demand
	 */
	private double getEVASuitPartsDemand(GoodsManager owner) {
		if (ItemResourceUtil.EVASUIT_PARTS_ID.contains(getID())) {
			return owner.getEVASuitMod() * EVA_PARTS_VALUE * owner.getDemandValue(this);
		}
		return 0;
	}

    /**
	 * Gets the demand for a part from construction sites.
	 *
	 * @param part the part.
	 * @return demand (# of parts).
	 */
	private double getPartConstructionSiteDemand(Settlement settlement) {
		double demand = 0D;
        int id = getID();

		// Add demand for part required as remaining construction material on
		// construction sites.
		Iterator<ConstructionSite> i = settlement.getConstructionManager().getConstructionSites().iterator();
		while (i.hasNext()) {
			ConstructionSite site = i.next();
			if (site.hasUnfinishedStage() && !site.getCurrentConstructionStage().isSalvaging()) {
				ConstructionStage stage = site.getCurrentConstructionStage();
				if (stage.getRemainingParts().containsKey(id)) {
					int requiredNum = stage.getRemainingParts().get(id);
					demand += requiredNum * CONSTRUCTION_SITE_REQUIRED_PART_FACTOR;
				}
			}
		}

		return demand;
	}

    /**
	 * Gets the manufacturing demand for a part.
	 *
	 * @param part the part.
	 * @return demand (# of parts)
	 */
	private double getPartManufacturingDemand(GoodsManager owner, Settlement settlement, Part part) {
		double demand = 0D;

		// Get highest manufacturing tech level in settlement.
		if (ManufactureUtil.doesSettlementHaveManufacturing(settlement)) {
			int techLevel = ManufactureUtil.getHighestManufacturingTechLevel(settlement);
			Iterator<ManufactureProcessInfo> i = ManufactureUtil.getManufactureProcessesForTechLevel(techLevel)
					.iterator();
			while (i.hasNext()) {
				double manufacturingDemand = getPartManufacturingProcessDemand(owner, settlement, part, i.next());
				demand += manufacturingDemand * (1 + techLevel);
			}
		}
		return Math.min(GoodsManager.MAX_DEMAND, demand);
	}

	/**
	 * Gets the demand of an input part in a manufacturing process.
	 *
	 * @param part    the input part.
	 * @param process the manufacturing process.
	 * @return demand (# of parts)
	 */
	private double getPartManufacturingProcessDemand(GoodsManager owner, Settlement settlement,
													Part part, ManufactureProcessInfo process) {
		double demand = 0D;
		double totalInputNum = 0D;

		ManufactureProcessItem partInput = null;
		Iterator<ManufactureProcessItem> i = process.getInputList().iterator();
		while (i.hasNext()) {
			ManufactureProcessItem item = i.next();
			if (part.getName().equalsIgnoreCase(item.getName())) {
				partInput = item;
			}
			totalInputNum += item.getAmount();
		}

		if (partInput != null) {

			double outputsValue = 0D;
			Iterator<ManufactureProcessItem> j = process.getOutputList().iterator();
			while (j.hasNext()) {
				ManufactureProcessItem item = j.next();
				if (!process.getInputList().contains(item)) {
					outputsValue += ManufactureUtil.getManufactureProcessItemValue(item, settlement, true);
				}
			}

			// Determine value of required process power.
			double powerHrsRequiredPerMillisol = process.getPowerRequired() * MarsClock.HOURS_PER_MILLISOL;
			double powerValue = powerHrsRequiredPerMillisol * settlement.getPowerGrid().getPowerValue();

			// Obtain the value of this resource
			double totalInputsValue = (outputsValue - powerValue);

			GoodType type = part.getGoodType();
			switch(settlement.getObjective()) {
				case BUILDERS_HAVEN: {
					if (GoodType.UTILITY == type
					|| GoodType.TOOL == type
					|| GoodType.RAW == type
					|| GoodType.CONSTRUCTION == type
					|| GoodType.ELECTRICAL == type
					|| GoodType.METALLIC == type
					|| GoodType.ATTACHMENT == type) {
						totalInputsValue *= owner.getBuildersFactor();
					}
				} break;

				case CROP_FARM: {
					if (GoodType.KITCHEN == type) {
						totalInputsValue *= owner.getCropFarmFactor();
					}
				} break;

				case MANUFACTURING_DEPOT:
					totalInputsValue *= owner.getManufacturingFactor();
				break;

				case RESEARCH_CAMPUS: {
					if (GoodType.INSTRUMENT == type
					|| GoodType.ELECTRICAL == type
					|| GoodType.ELECTRONIC == type) {
						totalInputsValue *= owner.getResearchFactor();
					}
				} break;

				case TRADE_CENTER: {
					if (type == GoodType.VEHICLE) {
						totalInputsValue *= owner.getTradeFactor();
					}
				} break;

				case TRANSPORTATION_HUB: {
					if (type == GoodType.VEHICLE) {
						totalInputsValue *= owner.getTransportationFactor();
					}
				} break;

				case TOURISM: {
					if (GoodType.EVA == type
							|| GoodType.VEHICLE_HEAVY == type
							|| GoodType.VEHICLE == type
							|| GoodType.GEMSTONE == type) {
						totalInputsValue *= owner.getTourismFactor();
					}
				} break;
			}

			// Modify by other factors
			totalInputsValue *= MANUFACTURING_INPUT_FACTOR;
			if (totalInputsValue > 0D) {
				double partNum = partInput.getAmount();

				demand = totalInputsValue * (partNum / totalInputNum);
			}
		}

		return demand;
	}

    /**
	 * Gets the construction demand for a part.
	 *
	 * @param part the part.
	 * @return demand (# of parts).
	 */
	private double getPartConstructionDemand(GoodsManager owner, Settlement settlement) {
		double demand = 0D;

		ConstructionValues values = settlement.getConstructionManager().getConstructionValues();
		int bestConstructionSkill = ConstructionUtil.getBestConstructionSkillAtSettlement(settlement);
		Map<ConstructionStageInfo, Double> stageValues = values.getAllConstructionStageValues(bestConstructionSkill);
		Iterator<ConstructionStageInfo> i = stageValues.keySet().iterator();
		while (i.hasNext()) {
			ConstructionStageInfo stage = i.next();
			double stageValue = stageValues.get(stage);
			if (stageValue > 0D && ConstructionStageInfo.BUILDING.equals(stage.getType())
					&& isLocallyConstructable(settlement, stage)) {
				double constructionStageDemand = getPartConstructionStageDemand(getID(), stage, stageValue);
				if (constructionStageDemand > 0D) {
					demand += constructionStageDemand;
				}
			}
		}

		return demand;
	}

	/**
	 * Gets the demand for a part as an input for a particular building construction
	 * stage.
	 *
	 * @param part       the part.
	 * @param stage      the building construction stage.
	 * @param stageValue the building construction stage value (VP).
	 * @return demand (# of parts).
	 */
	private double getPartConstructionStageDemand(int part, ConstructionStageInfo stage, double stageValue) {
		double demand = 0D;

		int partNumber = getPrerequisiteConstructionPartNum(part, stage);

		if (partNumber > 0) {
			Map<Integer, Double> resources = getAllPrerequisiteConstructionResources(stage);
			Map<Integer, Integer> parts = getAllPrerequisiteConstructionParts(stage);

			double totalNumber = 0D;

			Iterator<Integer> i = resources.keySet().iterator();
			while (i.hasNext())
				totalNumber += resources.get(i.next());

			Iterator<Integer> j = parts.keySet().iterator();
			while (j.hasNext())
				totalNumber += parts.get(j.next());

			if (totalNumber > 0) {
				double totalInputsValue = stageValue * GoodsManager.CONSTRUCTING_INPUT_FACTOR;
				demand = totalInputsValue * (partNumber / totalNumber);
			}
		}

		return demand;
	}

		/**
	 * Gets the total number of a given part required to build a stage including all
	 * pre-stages.
	 *
	 * @param part  the part.
	 * @param stage the stage.
	 * @return total number of parts required.
	 */
	private static int getPrerequisiteConstructionPartNum(Integer part, ConstructionStageInfo stage) {

		int result = 0;

		// Add all parts from stage.
		Map<Integer, Integer> stageParts = stage.getParts();
		if (stageParts.containsKey(part)) {
			result += stageParts.get(part);
		}

		// Add all parts from first prestage, if any.
		ConstructionStageInfo preStage1 = ConstructionUtil.getPrerequisiteStage(stage);
		if ((preStage1 != null) && preStage1.isConstructable()) {
			Map<Integer, Integer> preStage1Parts = preStage1.getParts();
			if (preStage1Parts.containsKey(part)) {
				result += preStage1Parts.get(part);
			}

			// Add all parts from second prestage, if any.
			ConstructionStageInfo preStage2 = ConstructionUtil.getPrerequisiteStage(preStage1);
			if ((preStage2 != null) && preStage2.isConstructable()) {
				Map<Integer, Integer> preStage2Parts = preStage2.getParts();
				if (preStage2Parts.containsKey(part)) {
					result += preStage2Parts.get(part);
				}
			}
		}

		return result;
	}

	/**
	 * Limit the demand for kitchen parts.
	 *
	 * @param part   the part.
	 * @param demand the original demand.
	 * @return the flattened demand
	 */
	private double getKitchenPartDemand(GoodsManager owner) {
		if (ItemResourceUtil.KITCHEN_WARE_ID.contains(getID())) {
			return owner.getDemandValue(this) * KITCHEN_DEMAND;
		}
		return 0;
	}

	/**
	 * Gets the vehicle part factor for part demand.
	 * 
	 * @param owner
	 * @return
	 */
	private double getVehiclePartDemand(GoodsManager owner) {
		GoodType type = getGoodType();
		if (type == GoodType.VEHICLE) {
			return (1 + owner.getTourismFactor()/30.0) * VEHICLE_PART_DEMAND;
		}
		return 0;
	}

	/**
	 * Adjusts the fuel cell factor for part demand.
	 * 
	 * @param owner
	 * @return
	 */
	private double geFuelCellDemand(GoodsManager owner) {
		String name = getName().toLowerCase();
		if (name.contains("fuel cell")) {
			return FC_COST;
		}
		if (name.contains("stack")) {
			return FC_STACK_COST;
		}
		return 0;
	}
	
    /**
	 * Gets the Food Production demand for a part.
	 *
	 * @param part the part.
	 * @return demand (# of parts)
	 */
	private static double getPartFoodProductionDemand(GoodsManager owner, Settlement settlement, Part part) {
		double demand = 0D;

		// Get highest Food Production tech level in settlement.
		if (FoodProductionUtil.doesSettlementHaveFoodProduction(settlement)) {
			int techLevel = FoodProductionUtil.getHighestFoodProductionTechLevel(settlement);
			Iterator<FoodProductionProcessInfo> i = FoodProductionUtil.getFoodProductionProcessesForTechLevel(techLevel)
					.iterator();
			while (i.hasNext()) {
				double foodProductionDemand = getPartFoodProductionProcessDemand(owner, settlement, part, i.next());
				demand += foodProductionDemand;
			}
		}

		return demand;
	}
    
    	/**
	 * Gets the demand of an input part in a Food Production process.
	 *
	 * @param part    the input part.
	 * @param process the Food Production process.
	 * @return demand (# of parts)
	 */
	private static double getPartFoodProductionProcessDemand(GoodsManager owner, Settlement settlement,
															 Part part, FoodProductionProcessInfo process) {
		double demand = 0D;
		double totalInputNum = 0D;

		FoodProductionProcessItem partInput = null;
		Iterator<FoodProductionProcessItem> i = process.getInputList().iterator();
		while (i.hasNext()) {
			FoodProductionProcessItem item = i.next();
			if ((ItemType.PART == item.getType()) && part.getName().equalsIgnoreCase(item.getName())) {
				partInput = item;
			}
			totalInputNum += item.getAmount();
		}

		if (partInput != null) {

			double outputsValue = 0D;
			Iterator<FoodProductionProcessItem> j = process.getOutputList().iterator();
			while (j.hasNext()) {
				FoodProductionProcessItem item = j.next();
				if (!process.getInputList().contains(item)) {
					outputsValue += FoodProductionUtil.getFoodProductionProcessItemValue(item, settlement, true);
				}
			}

			// Determine value of required process power.
			double powerHrsRequiredPerMillisol = process.getPowerRequired() * MarsClock.HOURS_PER_MILLISOL;
			double powerValue = powerHrsRequiredPerMillisol * settlement.getPowerGrid().getPowerValue();

			double totalInputsValue = (outputsValue - powerValue) * owner.getTradeFactor() * owner.getCropFarmFactor()
					* GoodsManager.FOOD_PRODUCTION_INPUT_FACTOR;
			if (totalInputsValue > 0D) {
				double partNum = partInput.getAmount();
				demand = totalInputsValue * (partNum / totalInputNum);
			}
		}

		return demand;
	}

    /**
	 * Determines the number demand for all parts at the settlement.
	 *
	 * @return map of parts and their demand.
	 */
	// private void determineRepairPartsDemand() {
	// 	Map<Good, Double> partsProbDemand = new HashMap<>();

	// 	// Get all malfunctionables associated with settlement.
	// 	Iterator<Malfunctionable> i = MalfunctionFactory.getAssociatedMalfunctionables(settlement).iterator();
	// 	while (i.hasNext()) {
	// 		Malfunctionable entity = i.next();

	// 		// Determine wear condition modifier.
	// 		double wearModifier = (entity.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;

	// 		// Estimate repair parts needed per orbit for entity.
	// 		sumPartsDemand(partsProbDemand, getEstimatedOrbitRepairParts(entity), wearModifier);

	// 		// Add outstanding repair parts required.
	// 		sumPartsDemand(partsProbDemand, getOutstandingRepairParts(entity), MALFUNCTION_REPAIR_COEF);

	// 		// Estimate maintenance parts needed per orbit for entity.
	// 		sumPartsDemand(partsProbDemand, getEstimatedOrbitMaintenanceParts(entity), wearModifier);

	// 		// Add outstanding maintenance parts required.
	// 		sumPartsDemand(partsProbDemand, getOutstandingMaintenanceParts(entity), MAINTENANCE_REPAIR_COEF);
	// 	}
	
	// 	// Add demand for vehicle attachment parts.
	// 	sumPartsDemand(partsProbDemand, getVehicleAttachmentParts(), 1D);

	// 	// Store in parts demand cache.
	// 	for(Entry<Good, Double> entry : partsProbDemand.entrySet()) {
	// 		Good part = entry.getKey();

	// 		if (getDemandValue(part) < 1)
	// 			setDemandValue(part, 1.0);
	// 		else
	// 			setDemandValue(part, entry.getValue());
	// 	}
	// }
}
