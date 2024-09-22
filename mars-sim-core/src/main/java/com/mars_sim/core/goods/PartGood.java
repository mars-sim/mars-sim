/*
 * Mars Simulation Project
 * PartGood.java
 * @date 2024-06-29 
 * @author Barry Evans
 */
package com.mars_sim.core.goods;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.food.FoodProductionProcessInfo;
import com.mars_sim.core.food.FoodProductionUtil;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.manufacture.ManufactureUtil;
import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.construction.ConstructionSite;
import com.mars_sim.core.structure.construction.ConstructionStage;
import com.mars_sim.core.structure.construction.ConstructionStageInfo;
import com.mars_sim.core.structure.construction.ConstructionUtil;
import com.mars_sim.core.structure.construction.ConstructionValues;
import com.mars_sim.core.time.MarsTime;

/*
 * This class is the representation of a Part instance as a Good that is tradable.
 */
public class PartGood extends Good {
	
	private static final long serialVersionUID = 1L;
    
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(PartGood.class.getName());

	
	private static final String FIBERGLASS = "fiberglass";
	private static final String SCRAP = "scrap";
	private static final String INGOT = "ingot";
	private static final String SHEET = "sheet";
	private static final String TRUSS = "steel truss";
	private static final String STEEL = "steel";
	private static final String BRICK = "brick";
	private static final String HEAT_PROBE = "heat probe";
	private static final String BOTTLE = "bottle";
	private static final String GLASS_TUBE = "glass tube";
	private static final String GLASS_SHEET = "glass sheet";
	private static final String DRILL = "drill";
	private static final String STACK = "stack";
	private static final String FUEL_CELL = "fuel cell";
	private static final String BOARD = "board";
	private static final String MICROCONTROLLER = "microcontroller";
	private static final String WAFER = "semiconductor wafer";
	private static final String WIRE = "wire";
	private static final String BATTERY = "battery";
	private static final String AEROGEL_TILE = "aerogel tile";
	
	private static final String PIPE = "pipe";
	private static final String VALVE = "valve";
	private static final String PLASTIC = "plastic";
	private static final String TANK = "tank";
	private static final String DUCT = "duct";
	private static final String GASKET = "gasket";
	private static final String LIGHT = "light";
	private static final String RESISTOR = "resistor";
	private static final String CAPACITOR = "capacitor";
	private static final String DIODE = "diode";
	private static final String STEEL_WIRE = "steel wire";
	private static final String ELECTRICAL_WIRE = "electrical wire";
	private static final String POWER_CABLE = "power cable";
	private static final String PLASTIC_PIPE = "plastic pipe";
	private static final String WIRE_CONNECTOR = "wire connector";

	private static final int VEHICLE_PART_COST = 3;
	private static final int EVA_PARTS_VALUE = 20;
	private static final double CONSTRUCTING_INPUT_FACTOR = 0.2;
	private static final double FOOD_PRODUCTION_INPUT_FACTOR = 0.1;
	
	private static final double DRILL_DEMAND  = .5;
	private static final double BOTTLE_DEMAND = .02;
	private static final double FIBERGLASS_DEMAND = .00005;
	private static final double GASKET_DEMAND = .05;
	private static final double VEHICLE_PART_DEMAND = .4;
	private static final double EVA_PART_DEMAND = 1;
    private static final double KITCHEN_DEMAND = 1.5;
	private static final double SCRAP_METAL_DEMAND = .01;
	private static final double INGOT_METAL_DEMAND = .01;
	private static final double SHEET_METAL_DEMAND = .025;
	private static final double TRUSS_DEMAND = .05;
	private static final double STEEL_DEMAND = .1;
	private static final double BRICK_DEMAND = .005;
	private static final double ELECTRICAL_DEMAND = .15;
	private static final double INSTRUMENT_DEMAND = 6;
	private static final double METALLIC_DEMAND = .25;
	private static final double UTILITY_DEMAND = .25;
	private static final double TOOL_DEMAND = 4;
	private static final double CONSTRUCTION_DEMAND = 0.5;
	private static final double GLASS_SHEET_DEMAND = .025;
	private static final double GLASS_TUBE_DEMAND  = 8;
	private static final double BASE_DEMAND = 0.5;
	private static final double PARTS_MAINTENANCE_VALUE = 1000;
	private static final double CONSTRUCTION_SITE_REQUIRED_PART_FACTOR = 100D;
	private static final double ATTACHMENT_PARTS_DEMAND = 20;
	private static final double AEROGEL_TILE_DEMAND = 0.05;
	private static final double PLASTIC_PIPE_DEMAND = .1;
	
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
    private static final double INITIAL_PART_DEMAND = 30;
	private static final double INITIAL_PART_SUPPLY = 1;
	private static final double MANUFACTURING_INPUT_FACTOR = 2D;

	private static Set<Integer> kithenWare = ItemResourceUtil.convertNameArray2ResourceIDs(new String [] {
																		"autoclave", "blender", "microwave",
																		"oven", "refrigerator", "stove"});

	private static Set<Integer> attachments = ItemResourceUtil.convertNameArray2ResourceIDs(new String [] {
																		"backhoe", "bulldozer blade",
																		"crane boom", "drilling rig",
																		"pneumatic drill", "soil compactor"});
	
	/** The fixed flatten demand for this resource. */
	private double flattenDemand;
	/** The projected demand for this resource of each refresh cycle. */
	private double projectedDemand;
	/** The trade demand for this resource of each refresh cycle. */
	private double tradeDemand;
	/** The repair demand for this resource of each refresh cycle. */
	private double repairDemand;
	
	private double costModifier;

    public PartGood(Part p) {
        super(p.getName(), p.getID());
		
		// Pre-calculate the fixed values
		flattenDemand = calculateFlattenDemand(p) * calculateFlattenRawPartDemand(p);
		costModifier = calculateCostModifier(p);
    }

	/**
	 * Calculates the flatten demand based on the part.
	 * 
	 * @param part
	 * @return
	 */
	private double calculateFlattenDemand(Part part) {
		String name = part.getName();

		switch(part.getGoodType()) {
			case ELECTRICAL: {
				if (name.contains(LIGHT)
					|| name.contains(RESISTOR)
					|| name.contains(CAPACITOR)
					|| name.contains(DIODE)) {
					return 5;
				}
				if (name.equalsIgnoreCase(ELECTRICAL_WIRE))
					return .01;
				if (name.equalsIgnoreCase(WIRE_CONNECTOR))
					return .01;
				if (name.equalsIgnoreCase(POWER_CABLE))
					return .05;
				if (name.equalsIgnoreCase(STEEL_WIRE))
					return .025;
				if (name.contains(WIRE))
					return .001;
				
				return ELECTRICAL_DEMAND;
			}

			case INSTRUMENT:
				return INSTRUMENT_DEMAND;

			case METALLIC:
				return METALLIC_DEMAND;
		
			case UTILITY:
				if (name.contains(FIBERGLASS)) {
					return FIBERGLASS_DEMAND;
				}
				if (name.equalsIgnoreCase(GASKET)) {
					return GASKET_DEMAND;
				}
				if (name.equalsIgnoreCase(PLASTIC_PIPE)) {
					return PLASTIC_PIPE_DEMAND;
				}				

				return UTILITY_DEMAND;
		
			case TOOL:
				if (name.contains(DRILL)) {
					return DRILL_DEMAND;
				}
				return TOOL_DEMAND;

			case CONSTRUCTION:
				if (name.equalsIgnoreCase(AEROGEL_TILE)) {
					return AEROGEL_TILE_DEMAND;
				}
				return CONSTRUCTION_DEMAND;

			case EVA:
				return EVA_PART_DEMAND;
			
			default:
//				return 1;
		}

		
		if (name.contains(PIPE))
			return .4;
		
		if (name.contains(VALVE) || name.contains(HEAT_PROBE))
			return .02;

		if (name.contains(PLASTIC))
			return .2;
		
		if (name.contains(TANK) || name.contains(DUCT))
			return .1;

		if (name.contains(BOTTLE))
			return BOTTLE_DEMAND;
		
		return 1;
	}

    /**
     * Gets the flattened demand of this part
     * 
     * @return
     */
	@Override
    public double getFlattenDemand() {
    	return flattenDemand;
    }
    
    /**
     * Gets the projected demand of this resource.
     * 
     * @return
     */
	@Override
    public double getProjectedDemand() {
    	return projectedDemand;
    }
	
    /**
     * Gets the trade demand of this resource.
     * 
     * @return
     */
	@Override
    public double getTradeDemand() {
    	return tradeDemand;
    }
	
    /**
     * Gets the repair demand of this resource.
     * 
     * @return
     */
	@Override
    public double getRepairDemand() {
    	return repairDemand;
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
        
        if (name.contains(WIRE))
            return WIRE_COST;
        
        GoodType type = part.getGoodType();
        
        if (name.contains(BATTERY))
            return BATTERY_COST;    
        if (type == GoodType.VEHICLE)
            return VEHICLE_PART_COST;     
        if (type == GoodType.ELECTRONIC)
            return ELECTRONIC_COST;      
        if (type == GoodType.INSTRUMENT)
            return INSTRUMENT_COST;
        
        if (name.equalsIgnoreCase(STACK))
            return FC_STACK_COST;
        else if (name.equalsIgnoreCase(FUEL_CELL))
            return FC_COST;
        else if (name.contains(BOARD))
            return BOARD_COST;
        else if (name.equalsIgnoreCase(MICROCONTROLLER))
            return CPU_COST;
        else if (name.equalsIgnoreCase(WAFER))
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
		Part part = getPart();
		
		double previousDemand = owner.getDemandValue(this);
		
		Settlement settlement = owner.getSettlement();

		double totalDemand = 0;
		double average = 0;
		double totalSupply = 0;

		average = getAverageItemDemand(owner);

		// Get demand for a part.
		// NOTE: the following estimates are for each orbit (Martian year) :
		double projectedDemand = 
			// Add manufacturing demand.					
			getPartManufacturingDemand(owner, settlement, part)
			// Add food production demand.
			+ getPartFoodProductionDemand(owner, settlement, part)
			// Add construction demand.
			+ getPartConstructionDemand(settlement)
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
			+ geFuelCellDemand()
			// Calculate maintenance part demand.
			+ getMaintenancePartsDemand(settlement, part);
		
		projectedDemand = Math.min(HIGHEST_PROJECTED_VALUE, projectedDemand);
		
		this.projectedDemand = projectedDemand;
		
		double projected = projectedDemand
			// Flatten certain part demand.
			* flattenDemand;

		// Add trade demand.
		tradeDemand = owner.determineTradeDemand(this);

		// Gets the repair part demand
		// Note: need to look into parts reliability in MalfunctionManager to derive the repair value 
		repairDemand = (owner.getMaintenanceLevel() + owner.getRepairLevel())/2.0 * owner.getDemandValue(this);
		
		if (previousDemand == 0) {
			// At the start of the sim
			totalDemand = (
					.1 * repairDemand 
					+ .4 * average 
					+ .4 * projected 
					+ .1 * tradeDemand);
		}

		else {
			// Intentionally loses a tiny percentage (e.g. 0.0008) of its value
			// in order to counter the tendency for all goods to increase 
			// in value over time. 
			
			// Warning: a lot of Goods could easily will hit 10,000 demand
			// if not careful.
			
			// Allows only very small fluctuations of demand as possible
			totalDemand = (
					  .9985 * previousDemand 
					+ .00005 * repairDemand 
					+ .00005 * average 
					+ .00012 * projected 
					+ .0001 * tradeDemand); 
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
	 */
	private static double calculateFlattenRawPartDemand(Part part) {
		double base = BASE_DEMAND; 
		String name = part.getName();
		// Reduce the demand on the steel/aluminum scrap metal
		// since they can only be produced by salvaging a vehicle
		// therefore it's not reasonable to have high VP

		if (name.contains(SCRAP))
			return base * SCRAP_METAL_DEMAND;
		// May recycle the steel/AL scrap back to ingot
		// Note: the VP of a scrap metal could be heavily influence by VP of regolith

		if (name.contains(INGOT))
			return base * INGOT_METAL_DEMAND;

		if (name.contains(GLASS_SHEET))
			return base * GLASS_SHEET_DEMAND;
		
		if (name.contains(GLASS_TUBE))
			return base * GLASS_TUBE_DEMAND;
		
		if (name.contains(SHEET))
			return base * SHEET_METAL_DEMAND;

		if (name.contains(TRUSS))
			return base * TRUSS_DEMAND;

		if (name.contains(STEEL))
			return base * STEEL_DEMAND;

		if (name.contains(FIBERGLASS))
			return base * FIBERGLASS_DEMAND;

		if (name.equalsIgnoreCase(BRICK))
			return base * BRICK_DEMAND;

		return base;
	}


    /**
	 * Gets the current item demand.
	 *
	 * @param resource
	 * @param solElapsed
	 * @return
	 */
	private double getAverageItemDemand(GoodsManager owner) {
		// Future: find the 7 sols average of this resource
		return owner.getDemandValue(this);
	}

	/**
	 * Gets the attachment part demand.
	 *
	 * @param part the part.
	 * @return demand
	 */
	private double getAttachmentPartsDemand(GoodsManager owner) {
		if (attachments.contains(getID())) {
			return ATTACHMENT_PARTS_DEMAND * (1 + owner.getDemandValue(this) / 3);
		}
		return 0;
	}


	/**
	 * Gets the EVA related demand for a part.
	 *
	 * @param part the part.
	 * @return demand
	 */
	private double getEVASuitPartsDemand(GoodsManager owner) {		
		if (ItemResourceUtil.evaSuitPartIDs != null && ItemResourceUtil.evaSuitPartIDs.contains(getID())) {
			return owner.getEVASuitMod() * EVA_PARTS_VALUE * owner.getDemandValue(this) / 3;
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
		double base = 0D;
        int id = getID();

		// Add demand for part required as remaining construction material on
		// construction sites.
		Iterator<ConstructionSite> i = settlement.getConstructionManager().getConstructionSites().iterator();
		while (i.hasNext()) {
			ConstructionSite site = i.next();
			if (site.hasUnfinishedStage() && !site.getCurrentConstructionStage().isSalvaging()) {
				ConstructionStage stage = site.getCurrentConstructionStage();
				if (stage.getMissingParts().containsKey(id)) {
					int requiredNum = stage.getMissingParts().get(id);
					base += requiredNum * CONSTRUCTION_SITE_REQUIRED_PART_FACTOR;
				}
			}
		}

		return Math.min(GoodsManager.MAX_DEMAND, base / 100);
	}

    /**
	 * Gets the manufacturing demand for a part.
	 *
	 * @param part the part.
	 * @return demand (# of parts)
	 */
	private double getPartManufacturingDemand(GoodsManager owner, Settlement settlement, Part part) {
		double base = 0D;

		// Get highest manufacturing tech level in settlement.
		if (ManufactureUtil.doesSettlementHaveManufacturing(settlement)) {
			int techLevel = ManufactureUtil.getHighestManufacturingTechLevel(settlement);
			Iterator<ManufactureProcessInfo> i = ManufactureUtil.getManufactureProcessesForTechLevel(techLevel)
					.iterator();
			while (i.hasNext()) {
				double manufacturingDemand = getPartManufacturingProcessDemand(owner, settlement, part, i.next());
				base += manufacturingDemand * (1 + techLevel);
			}
		}
		
		return Math.min(GoodsManager.MAX_DEMAND, base / 100);
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

		ProcessItem partInput = null;
		for(var item : process.getInputList()) {
			if (part.getName().equalsIgnoreCase(item.getName())) {
				partInput = item;
			}
			totalInputNum += item.getAmount();
		}

		if (partInput != null) {

			double outputsValue = 0D;
			for(var item : process.getOutputList()) {
				if (!process.getInputList().contains(item)) {
					outputsValue += ManufactureUtil.getManufactureProcessItemValue(item, settlement, true);
				}
			}

			// Determine value of required process power.
			double powerHrsRequiredPerMillisol = process.getPowerRequired() * MarsTime.HOURS_PER_MILLISOL;
			double powerValue = powerHrsRequiredPerMillisol * settlement.getPowerGrid().getPowerValue();

			// Obtain the value of this resource
			double totalInputsValue = (outputsValue - powerValue);

			CommerceType cType = null;
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
						cType = CommerceType.BUILDING;
					}
				} break;

				case CROP_FARM: {
					if (GoodType.KITCHEN == type) {
						cType = CommerceType.CROP;
					}
				} break;

				case MANUFACTURING_DEPOT:
					cType = CommerceType.MANUFACTURING;
				break;

				case RESEARCH_CAMPUS: {
					if (GoodType.INSTRUMENT == type
					|| GoodType.ELECTRICAL == type
					|| GoodType.ELECTRONIC == type) {
						cType = CommerceType.RESEARCH;

					}
				} break;

				case TRADE_CENTER: {
					if (type == GoodType.VEHICLE) {
						cType = CommerceType.TRADE;
					}
				} break;

				case TRANSPORTATION_HUB: {
					if (type == GoodType.VEHICLE) {
						cType = CommerceType.TRANSPORT;

					}
				} break;

				case TOURISM: {
					if (GoodType.EVA == type
							|| GoodType.VEHICLE_HEAVY == type
							|| GoodType.VEHICLE == type
							|| GoodType.GEMSTONE == type) {
						cType = CommerceType.TOURISM;
					}
				} break;
			}

			if (cType != null) {
				totalInputsValue *= owner.getCommerceFactor(cType);
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
	private double getPartConstructionDemand(Settlement settlement) {
		double base = 0D;

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
					base += constructionStageDemand;
				}
			}
		}

		return Math.min(GoodsManager.MAX_DEMAND, base / 100);
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
				double totalInputsValue = stageValue * CONSTRUCTING_INPUT_FACTOR;
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
		if (kithenWare.contains(getID())) {
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
			return (1 + owner.getCommerceFactor(CommerceType.TOURISM)/30.0) * VEHICLE_PART_DEMAND;
		}
		return 0;
	}

	/**
	 * Adjusts the fuel cell factor for part demand.
	 * 
	 * @param owner
	 * @return
	 */
	private double geFuelCellDemand() {
		String name = getName().toLowerCase();
		if (name.contains(FUEL_CELL)) {
			return FC_COST;
		}
		if (name.contains(STACK)) {
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
			Iterator<FoodProductionProcessInfo> i = FoodProductionUtil.getProcessesForTechSkillLevel(techLevel)
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

		ProcessItem partInput = null;
		Iterator<ProcessItem> i = process.getInputList().iterator();
		while (i.hasNext()) {
			ProcessItem item = i.next();
			if ((ItemType.PART == item.getType()) && part.getName().equalsIgnoreCase(item.getName())) {
				partInput = item;
			}
			totalInputNum += item.getAmount();
		}

		if (partInput != null) {

			double outputsValue = 0D;
			Iterator<ProcessItem> j = process.getOutputList().iterator();
			while (j.hasNext()) {
				ProcessItem item = j.next();
				if (!process.getInputList().contains(item)) {
					outputsValue += FoodProductionUtil.getProcessItemValue(item, settlement, true);
				}
			}

			// Determine value of required process power.
			double powerHrsRequiredPerMillisol = process.getPowerRequired() * MarsTime.HOURS_PER_MILLISOL;
			double powerValue = powerHrsRequiredPerMillisol * settlement.getPowerGrid().getPowerValue();

			double totalInputsValue = (outputsValue - powerValue) * owner.getCommerceFactor(CommerceType.TRADE)
										* owner.getCommerceFactor(CommerceType.CROP)
										* FOOD_PRODUCTION_INPUT_FACTOR;
			if (totalInputsValue > 0D) {
				double partNum = partInput.getAmount();
				demand = totalInputsValue * (partNum / totalInputNum);
			}
		}

		return demand;
	}

	
	/**
	 * Gets the demand from maintenance parts from a particular settlement.
	 * 
	 * @param settlement
	 * @param part
	 */
	private double getMaintenancePartsDemand(Settlement settlement, Part part) {
		double demand = 0;
		int number = settlement.getBuildingManager().getMaintenanceDemand(part);
		if (number > 0) {
			demand = number * PARTS_MAINTENANCE_VALUE;
			logger.info(settlement, 30_000L, "Triggering " + Math.round(demand * 10.0)/10.0 
					+" good demand from " + number + " " + part.getName() + ".");
		}
		return demand;
	}
	
	/**
	 * Injects an individual part demand immediately without waiting for goods manager to update it.
	 * 
	 * @param part
	 * @param good
	 * @param owner
	 */
	public void injectPartsDemand(Part part, GoodsManager owner, int needNum) {
		double previousDemand = owner.getDemandValue(this);
		
		int previousNum = owner.getSettlement().getItemResourceStored(part.getID());
		
		double previousTotalDemand = previousDemand * previousNum;
		
		double newAddedDemand = getMaintenancePartsDemand(owner.getSettlement(), part);

		double newAddedTotalDemand = newAddedDemand * needNum;
		
		double diff = previousTotalDemand - newAddedTotalDemand;
		
		double finalDemand = (previousTotalDemand + newAddedTotalDemand) / (previousNum + needNum);
		String reason = "No change for ";

		if (diff < 0) {
			if (finalDemand < previousDemand) {
				finalDemand = previousDemand + newAddedDemand;
			}
			
			owner.setDemandValue(this, finalDemand);
			
			reason = "Injecting demand for ";
			// Recalculate settlement good value for this part.
			owner.determineGoodValue(GoodsUtil.getGood(part.getID()));
		}

		// Output a detailed message	
		logger.info(owner.getSettlement(), 30_000L, reason
				+ part.getName()
				+ "  Previous demand: "
				+ Math.round(previousDemand * 10.0)/10.0 
				+ " (Quantity: " + previousNum + "}"
				+ "  Proposed demand: " + Math.round(finalDemand * 10.0)/10.0 
				+ " (Quantity: " + needNum + "}"
				);	
	}
}
