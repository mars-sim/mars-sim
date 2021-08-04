/**
 * Mars Simulation Project
 * Good.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.goods;

import java.io.Serializable;
import java.util.List;

import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.foodProduction.FoodProductionProcessInfo;
import org.mars_sim.msp.core.foodProduction.FoodProductionProcessItem;
import org.mars_sim.msp.core.foodProduction.FoodProductionUtil;
import org.mars_sim.msp.core.manufacture.ManufactureProcessInfo;
import org.mars_sim.msp.core.manufacture.ManufactureProcessItem;
import org.mars_sim.msp.core.manufacture.ManufactureUtil;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ItemType;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.vehicle.Drone;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;

/**
 * A meta class describing an economic good in the simulation.
 */
public class Good implements Serializable, Comparable<Good> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static final double DOLLAR_PER_UNIT = 0.5;
	private static final double EVA_SUIT_VALUE = 20D;
	private static final double CONTAINER_VALUE = 1D;

	private static final double VEHICLE_VALUE = 60D;
	private static final double LUV_VALUE = 15D;
	private static final double DRONE_VALUE = 20D;
	
	private static final double CL_VALUE = 0.1;
	private static final double ICE_VALUE = 0.1;
	private static final double FOOD_VALUE = 1.5;
	private static final double DERIVED_VALUE = .01;
	
	private static final double CROP_VALUE = 10000D;
	private static final double OIL_VALUE = 0.01;
	private static final double ROCK_VALUE = 0.01;
	private static final double REGOLITH_VALUE = .02;
	private static final double ORE_VALUE = 0.03;
	private static final double MINERAL_VALUE = 0.1;
	private static final double STANDARD_AMOUNT_VALUE = 0.3; 
	
	private static final double ITEM_VALUE = 1.1D;
	private static final double FC_STACK_VALUE = 30;
	private static final double FC_VALUE = 1;
	private static final double BOARD_VALUE = 5;
	private static final double CPU_VALUE = 10;
	private static final double WAFER_VALUE = 50;
	private static final double BATTERY_VALUE = 20;
	
	private static final double LABOR_FACTOR = 250D ; 
	private static final double PROCESS_TIME_FACTOR = 1000D;
	private static final double POWER_FACTOR = 1D;
	private static final double SKILL_FACTOR = 1D;
	private static final double TECH_FACTOR = 2D;
	
	// Data members
	private String name;
	
	private int id;
	private int count0_out;
	private int count1_out;
//	private int count0_in;
//	private int count1_in;
	
	private double laborTime;
	private double power;
	private double processTime;
	private double skill;
	private double tech;

	private double modifier = -1;
	//  The inter-market average value among the settlements
	private double averageGoodValue;
	private double costOutput = -1;
	
	private GoodCategory category;

	private List<ManufactureProcessInfo> manufactureProcessInfos;
	private List<FoodProductionProcessInfo> foodProductionProcessInfos;
	
	/**
	 * Constructor with object.
	 * 
	 * @param name     the name of the good.
	 * @param object   the good's object if any.
	 * @param category the good's category.
	 */
	Good (String name, int id, GoodCategory category) {
		if (name != null)
			this.name = name.trim().toLowerCase();
		else
			throw new IllegalArgumentException("name cannot be null.");
		this.id = id;

		if (isValidCategory(category))
			this.category = category;
		else
			throw new IllegalArgumentException("category: " + category + " not valid.");
	}

	/**
	 * Calculate the base cost of each good
	 */
	public void computeCost() {
		manufactureProcessInfos = ManufactureUtil.getManufactureProcessesWithGivenOutput(name);	
		foodProductionProcessInfos = FoodProductionUtil.getFoodProductionProcessesWithGivenOutput(name);
		
//		componentManuInfos = ManufactureUtil.getManufactureProcessesWithGivenInput(name);	
//		componentFoodInfos = FoodProductionUtil.getFoodProductionProcessesWithGivenInput(name);
//		resourceProcesses = BuildingConfig.getResourceProcessMap();
		
		// Compute the cost of output
		computeBaseCost();
		// Compute the cost of output
		computeOutputCost();
	}

	/**
	 * Checks if a category string is valid.
	 * 
	 * @param category the category enum to check.
	 * @return true if valid category.
	 */
	private static boolean isValidCategory(GoodCategory category) {
		for (GoodCategory cat : GoodCategory.values()) {
			if (cat == category)
				return true;
		}
		return false;
		
//		boolean result = false;
//
//		if (GoodType.AMOUNT_RESOURCE == category)
//			result = true;
//		else if (GoodType.ITEM_RESOURCE == category)
//			result = true;
//		else if (GoodType.EQUIPMENT == category)
//			result = true;
//		else if (GoodType.VEHICLE == category)
//			result = true;
//
//		return result;
	}

	/**
	 * Gets the good's name.
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the good's equipment class.
	 * 
	 * @return equipment class
	 */
	public Class<? extends Equipment> getClassType() {
//		if (getCategory() == GoodType.EQUIPMENT)
		return EquipmentFactory.getEquipmentClass(name);
	}

	public int getID() {
		return id;
	}
	
	/**
	 * Gets the good's category enum.
	 * 
	 * @return category.
	 */
	public GoodCategory getCategory() {
		return category;
	}

	public double getlaborTime() {
		return laborTime;
	}
	
	public double getPower() {
		return power;
	}
	
	public double getProcessTime() {
		return processTime;
	}
	
	public double getSkill() {
		return skill;
	}
	
	public double getTech() {
		return tech;
	}

	public double getModifier() {
		return modifier;
	}
		
	public double computeTypeModifier() {
		if (category == GoodCategory.AMOUNT_RESOURCE) {
			
			AmountResource ar = ResourceUtil.findAmountResource(id);
			boolean edible = ar.isEdible();
			String type = ar.getType();
					
			if (ar.getName().equalsIgnoreCase("chlorine"))
				return CL_VALUE;
			
			if (ar.getName().equalsIgnoreCase("ice"))
				return ICE_VALUE;
			
			else if (edible) {
				if (type != null && type.equalsIgnoreCase("derived"))
					return  DERIVED_VALUE;
				else
					return FOOD_VALUE;
			}

			else if (type != null && type.equalsIgnoreCase("oil"))
				return OIL_VALUE ;
			else if (type != null && type.equalsIgnoreCase("crop"))
				return CROP_VALUE ;
			else if (type != null && type.equalsIgnoreCase("rock"))
				return ROCK_VALUE ;
			else if (type != null && type.equalsIgnoreCase("regolith"))
				return REGOLITH_VALUE ;
			else if (type != null && type.equalsIgnoreCase("ore"))
				return ORE_VALUE ;
			else if (type != null && type.equalsIgnoreCase("mineral"))
				return MINERAL_VALUE ;
			else	
				return STANDARD_AMOUNT_VALUE ;
		}
		
		else if (category == GoodCategory.ITEM_RESOURCE) {
//			double weight = ItemResourceUtil.findItemResource(id).getMassPerItem();		
			Part part = ItemResourceUtil.findItemResource(id);
			String name = part.getName().toLowerCase();

			if (name.equalsIgnoreCase("fuel cell stack"))
				return FC_STACK_VALUE;
			else if (name.equalsIgnoreCase("solid oxide fuel cell"))
				return FC_VALUE;
			else if (name.contains("board"))
				return BOARD_VALUE;
			else if (name.equalsIgnoreCase("microcontroller"))
				return CPU_VALUE;
			else if (name.equalsIgnoreCase("small semiconductor wafer"))
				return WAFER_VALUE;
			else if (name.contains("battery"))
				return BATTERY_VALUE;
			
			return ITEM_VALUE;// * weight;
		}
		
		else if (category == GoodCategory.EQUIPMENT) {
//			if (name.contains("suit")) {
				return EVA_SUIT_VALUE;
//			}
		}
		
		else if (category == GoodCategory.CONTAINER) {
			return CONTAINER_VALUE;
		}
		
		else if (category == GoodCategory.VEHICLE) {
			
			if (name.contains(LightUtilityVehicle.NAME))
				return LUV_VALUE;
			else if (name.contains(Drone.NAME))
				return DRONE_VALUE;
			else
				return VEHICLE_VALUE;
		}
		
		else
			return DOLLAR_PER_UNIT;
	}
	
	public double getCount0() {
		return count0_out;
	}
	
	public double getCount1() {
		return count1_out;
	}
	
	/**
	 * Get the cost of output
	 */
	public double getCostOutput() {
		return costOutput;
	}
	
	/**
	 * Calculate the modified cost of output
	 */
	public void computeOutputCost() {
		// First compute the modifier
		if (modifier == -1) {
			modifier = computeTypeModifier();
			// Then compute the total cost
			costOutput = modifier * (1 + getlaborTime() / LABOR_FACTOR 
					+ getProcessTime() / PROCESS_TIME_FACTOR
					+ getPower() / POWER_FACTOR
					+ getSkill() / SKILL_FACTOR
					+ getTech() / TECH_FACTOR);
//			System.out.println(name 
//				+ "'s modifier: " + modifier
//				+ "   cost of output: " + costOutput);
		}
	}
	
	public double getAverageGoodValue() {
		return averageGoodValue;
	}
	
	public void setAverageGoodValue(double value) {
		averageGoodValue = value;
	}
	
	public void adjustGoodValue() {
		// deflate the value by 5%
		if (averageGoodValue > 10)
			averageGoodValue = .95 * averageGoodValue;
		
		// Inflate the value by 5%
		else if (averageGoodValue < 1)
			averageGoodValue = 1.05 * averageGoodValue;
	}
	
	/**
	 * Computes the base cost of each good
	 */
	public void computeBaseCost() {
		double labor0_out = 0;
		double power0_out = 0;
		double process0_out = 0;
		double skill0_out = 0;
		double tech0_out = 0;

		if (manufactureProcessInfos != null || !manufactureProcessInfos.isEmpty()) {

			double goodAmount0_out = 0;
			double otherAmount0_out = 0;
			double goodWeight0_out = 1;
			double otherWeight0_out = 1;
			int numProcesses = manufactureProcessInfos.size();
			
			for (ManufactureProcessInfo i: manufactureProcessInfos) {
				
				List<ManufactureProcessItem> items = i.getManufactureProcessItem(name);
				
				for (ManufactureProcessItem j: items) {
					String goodName = j.getName();
					if (goodName.equalsIgnoreCase(name)) {
						goodAmount0_out += j.getAmount();
						
						if (ItemType.PART == j.getType())
							goodWeight0_out += ItemResourceUtil.findItemResource(name).getMassPerItem();
					}
					else {
						otherAmount0_out += j.getAmount();
						
						if (ItemType.PART == j.getType())
							otherWeight0_out += ItemResourceUtil.findItemResource(name).getMassPerItem();
					}
				}
						
				double laborTime 	= i.getWorkTimeRequired();
				double power 		= i.getPowerRequired();
				double processTime 	= i.getProcessTimeRequired();
				int skillLevel 		= i.getSkillLevelRequired();
				int techLevel 		= i.getTechLevelRequired();	
				
				labor0_out 	 += laborTime;
				power0_out 	 += power;
				process0_out += processTime;
				skill0_out 	 += skillLevel;
				tech0_out 	 += techLevel;
				count0_out++;
				
				if (count0_out != 0) {
//					double fractionalAmount = goodAmount0_out * goodWeight0_out / (goodAmount0_out * goodWeight0_out + otherAmount0_out * otherWeight0_out);
					double fraction = 1 / (goodAmount0_out * goodWeight0_out + otherAmount0_out * otherWeight0_out);
					labor0_out 	 = labor0_out * fraction;
					power0_out 	 = power0_out * fraction;
					process0_out = process0_out * fraction;
					skill0_out	 = skill0_out * fraction;
					tech0_out 	 = tech0_out * fraction;
				}	
			}
			
			if (numProcesses != 0) {
				labor0_out 	 = labor0_out / numProcesses;
				power0_out 	 = power0_out / numProcesses;
				process0_out = process0_out / numProcesses;
				skill0_out	 = skill0_out / numProcesses;
				tech0_out 	 = tech0_out / numProcesses;
			}
//			System.out.println(name + " labor0_out: " + labor0_out);
//			System.out.println(name + " power0_out: " + power0_out);
//			System.out.println(name + " process0_out: " + process0_out);
//			System.out.println(name + " skill0_out: " + skill0_out);
//			System.out.println(name + " tech0_out: " + tech0_out);
		}

		double labor1_out = 0;
		double power1_out = 0;
		double process1_out = 0;
		double skill1_out = 0;
		double tech1_out = 0;
		
		if (foodProductionProcessInfos != null || !foodProductionProcessInfos.isEmpty()) {

			double goodAmount1_out = 0;
			double otherAmount1_out = 0;
			double goodWeight1_out = 1;
			double otherWeight1_out = 1;
			int numProcesses = foodProductionProcessInfos.size();
			
			for (FoodProductionProcessInfo i: foodProductionProcessInfos) {
				List<FoodProductionProcessItem> items = i.getFoodProductionProcessItem(name);
				for (FoodProductionProcessItem j: items) {
					String goodName = j.getName();
					if (goodName.equalsIgnoreCase(name)) {
						goodAmount1_out += j.getAmount();
						
						if (ItemType.PART == j.getType())
							goodWeight1_out += ItemResourceUtil.findItemResource(name).getMassPerItem();
					}
					else {
						otherAmount1_out += j.getAmount();
						
						if (ItemType.PART == j.getType())
							otherWeight1_out += ItemResourceUtil.findItemResource(name).getMassPerItem();
					}
				}
				
				double laborTime 	= i.getWorkTimeRequired();
				double power 		= i.getPowerRequired();
				double processTime 	= i.getProcessTimeRequired();
				int skillLevel 		= i.getSkillLevelRequired();
				int techLevel 		= i.getTechLevelRequired();	
				
				labor1_out 	 += laborTime;
				power1_out 	 += power;
				process1_out += processTime;
				skill1_out 	 += skillLevel;
				tech1_out 	 += techLevel;
				count1_out++;
			}
			
			if (count1_out != 0) {
				double fraction = 1 / (goodAmount1_out * goodWeight1_out + otherAmount1_out * otherWeight1_out);
				labor1_out 	 = labor1_out * fraction;
				power1_out 	 = power1_out * fraction;
				process1_out = process1_out * fraction;
				skill1_out	 = skill1_out * fraction;
				tech1_out 	 = tech1_out * fraction;
			}
			
			if (numProcesses != 0) {
				labor1_out 	 = labor1_out / numProcesses;
				power1_out 	 = power1_out / numProcesses;
				process1_out = process1_out / numProcesses;
				skill1_out	 = skill1_out / numProcesses;
				tech1_out 	 = tech1_out / numProcesses;
			}
			
//			System.out.println(name + " labor1_out: " + labor1_out);
//			System.out.println(name + " power1_out: " + power1_out);
//			System.out.println(name + " process1_out: " + process1_out);
//			System.out.println(name + " skill1_out: " + skill1_out);
//			System.out.println(name + " tech1_out: " + tech1_out);
		}
		
		if (labor0_out == 0)
			laborTime = labor1_out;
		else if (labor1_out == 0)
			laborTime = labor0_out;
		else
			laborTime = (labor0_out + labor1_out)/2D;
		
		if (power0_out == 0)
			power = power1_out;
		else if (power1_out == 0)
			power = power0_out;
		else
			power = (power0_out + power1_out)/2D;
		
		if (process0_out == 0)
			processTime = process1_out;
		else if (process1_out == 0)
			processTime = process0_out;
		else
			processTime = (process0_out + process1_out)/2D;

		if (skill0_out == 0)
			skill = skill1_out;
		else if (skill1_out == 0)
			skill = skill0_out;
		else
			skill = (skill0_out + skill1_out)/2D;

		if (tech0_out == 0)
			tech = tech1_out;
		else if (tech1_out == 0)
			tech = tech0_out;
		else
			tech = (tech0_out + tech1_out)/2D;
		
//		String s = String.format(" %20s\n "
//		+ "%12s %3.0f\n "
//		+ "%12s %3.0f\n "
//		
//		+ "%12s %5.1f\n "
//		+ "%12s %5.1f\n "
//		+ "%12s %5.1f\n "
//		+ "%12s %5.1f\n "
//		+ "%12s %5.1f\n "
//		
//		+ "%12s %.5f\n "
//		+ "%12s %.5f\n "
//		+ "%12s %.5f\n",
//		name,
//		"count", getCount0(),
//		"count1", getCount1(),
//		
//		"labor Time", laborTime,
//		"power", power,
//		"processTime", processTime,
//		"skill", skill,
//		"tech", tech,
//		
//		"Output Cost", getTotalCostOutput(),
//		"Value", getGoodValue(),
//		"Input Price", computeInputPrice()
//		);
//
//		System.out.println(s);
		
		// TODO: how to compute for crop growth
		
//		double power3 = 0;
//		double rate3 = 0;
//		if (resourceProcesses != null || !resourceProcesses.isEmpty()) {
//			Set<String> buildingTypes = BuildingConfig.getBuildingTypes();
//			// Note: each settlement have different # of building types 
//			for (String type : buildingTypes) {
//				for (ResourceProcess i: resourceProcesses.get(type)) {
//					Set<Integer> resources = i.getOutputResources();
//					for (Integer ii : resources) {
//						if (ii == id) {
//							double power = i.getPowerRequired();
//							double rate = i.getMaxOutputResourceRate(ii);
//						}
//					}
//				}
//			}
//		}
	}
	

//	/**
//	 * Computes the base price of each input resource
//	 */
//	public double computeInputPrice() {
//
////		double labor0_in = 0;
////		double power0_in = 0;
////		double process0_in = 0;
////		double skill0_in = 0;
////		double tech0_in = 0;
//	
//		double totalPrice = 0;
//		
//		if (manufactureProcessInfos != null || !manufactureProcessInfos.isEmpty()) {
//
//			for (ManufactureProcessInfo i: manufactureProcessInfos) {
//				// Compute the input resources
//				List<ManufactureProcessItem> inputList = i.getInputList();
//				for (ManufactureProcessItem j: inputList) {
//					
//					Good g = GoodsUtil.getResourceGood(j.getName());
//					
//					double amount_in = j.getAmount();
//					
//					if (g != null) {
////						double cost = g.getTotalCostOutput() * amount_in;
////						double price = g.getGoodValue() * cost;
////						totalPrice += goodsManager.getPricePerItem(g);
//					}
//					else
//						logger.severe("Can't find the good with the name '" + j.getName() + "'");
//				}
//				
////				double laborTime01 = i.getWorkTimeRequired();
////				double power01 = i.getPowerRequired();
////				double processTime01 = i.getProcessTimeRequired();
////				int skillLevel01 = i.getSkillLevelRequired();
////				int techLevel01 = i.getTechLevelRequired();	
////				
////				labor0_in += laborTime01;
////				power0_in += power01;
////				process0_in += processTime01;
////				skill0_in += skillLevel01;
////				tech0_in += techLevel01;
////				count0_in++;
////				
////				if (count0_in != 0) {
////					labor0_in = labor0_in/totalAmount_in;
////					power0_in = power0_in/totalAmount_in;
////					process0_in = process0_in/totalAmount_in;
////					skill0_in = skill0_in/totalAmount_in;
////					tech0_in = tech0_in/totalAmount_in;
////				}
//			}
//		}
//		
//		if (foodProductionProcessInfos != null || !foodProductionProcessInfos.isEmpty()) {
//
//			for (FoodProductionProcessInfo i: foodProductionProcessInfos) {
//				// Compute the input resources
//				List<FoodProductionProcessItem> inputList = i.getInputList();
//				for (FoodProductionProcessItem j: inputList) {
//					
//					Good g = GoodsUtil.getResourceGood(j.getName());
//					
//					double amount_in = j.getAmount();
//					
//					if (g != null) {
////						double cost = getTotalCostOutput() * amount_in;
////						double price = g.getGoodValue() * cost;
////						totalPrice += price;
////					}
//						
//					else
//						logger.severe("Can't find the good with the name '" + j.getName() + "'");
//				}
//			}	
//		}
//		
////		totalPriceInput = (labor0_in / 250 + process0_in / 1000
////				+ power0_in
////				+ skill0_in 
////				+ tech0_in) * getModifier();
//		
//		return totalPrice;
//	}
	
//	/**
//	 * Computes the base good value of each input resource
//	 */
//	public double computeInputValue() {
//
////		double labor0_in = 0;
////		double power0_in = 0;
////		double process0_in = 0;
////		double skill0_in = 0;
////		double tech0_in = 0;
//	
//		double totalValue = 0;
//		
//		if (manufactureProcessInfos != null || !manufactureProcessInfos.isEmpty()) {
//
//			for (ManufactureProcessInfo i: manufactureProcessInfos) {
//				// Compute the input resources
//				List<ManufactureProcessItem> inputList = i.getInputList();
//				for (ManufactureProcessItem j: inputList) {
//					
//					Good g = GoodsUtil.getResourceGood(j.getName());
//					
//					double amount_in = j.getAmount();
//					
//					if (g != null) {
//						double value = g.getGoodValue() / amount_in;
//						totalValue += value;
//					}
//					else
//						logger.severe("Can't find the good with the name '" + j.getName() + "'");
//				}
//				
////				double laborTime01 = i.getWorkTimeRequired();
////				double power01 = i.getPowerRequired();
////				double processTime01 = i.getProcessTimeRequired();
////				int skillLevel01 = i.getSkillLevelRequired();
////				int techLevel01 = i.getTechLevelRequired();	
////				
////				labor0_in += laborTime01;
////				power0_in += power01;
////				process0_in += processTime01;
////				skill0_in += skillLevel01;
////				tech0_in += techLevel01;
////				count0_in++;
////				
////				if (count0_in != 0) {
////					labor0_in = labor0_in/totalAmount_in;
////					power0_in = power0_in/totalAmount_in;
////					process0_in = process0_in/totalAmount_in;
////					skill0_in = skill0_in/totalAmount_in;
////					tech0_in = tech0_in/totalAmount_in;
////				}
//			}
//		}
//		
//		if (foodProductionProcessInfos != null || !foodProductionProcessInfos.isEmpty()) {
//
//			for (FoodProductionProcessInfo i: foodProductionProcessInfos) {
//				// Compute the input resources
//				List<FoodProductionProcessItem> inputList = i.getInputList();
//				for (FoodProductionProcessItem j: inputList) {
//					
//					Good g = GoodsUtil.getResourceGood(j.getName());
//					
//					double amount_in = j.getAmount();
//					
//					if (g != null) {
//						double value = g.getGoodValue() / amount_in;
//						totalValue += value;
//					}
//						
//					else
//						logger.severe("Can't find the good with the name '" + j.getName() + "'");
//				}
//			}	
//		}
//		
////		totalPriceInput = (labor0_in / 250 + process0_in / 1000
////				+ power0_in
////				+ skill0_in 
////				+ tech0_in) * getModifier();
//		
//		return totalValue;
//	}
	
	public void setGoodValue(double value) {
		averageGoodValue = value;
	}
	
	/**
	 * Gets a string representation of the good.
	 * 
	 * @return string.
	 */
	public String toString() {
		return name;
	}

	/**
	 * Gets the hash code value.
	 * 
	 * @return hash code
	 */
	public int hashCode() {
		int hashCode = name.hashCode();
		hashCode *= id;
		hashCode *= category.hashCode();
		return hashCode;
	}

	/**
	 * Compares this object with the specified object for order.
	 * 
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	public int compareTo(Good o) {
		return name.compareTo(o.name);
	}
	
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		Good g = (Good) obj;
		return this.getName().equals(g.getName())
				&& this.id == g.getID();
	}
	
}
