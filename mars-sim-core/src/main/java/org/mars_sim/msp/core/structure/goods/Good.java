/**
 * Mars Simulation Project
 * Good.java
 * @version 3.1.0 2018-12-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.goods;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.foodProduction.FoodProductionProcessInfo;
import org.mars_sim.msp.core.foodProduction.FoodProductionProcessItem;
import org.mars_sim.msp.core.foodProduction.FoodProductionUtil;
import org.mars_sim.msp.core.manufacture.ManufactureProcessInfo;
import org.mars_sim.msp.core.manufacture.ManufactureProcessItem;
import org.mars_sim.msp.core.manufacture.ManufactureUtil;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;

/**
 * A meta class describing an economic good in the simulation.
 */
public class Good implements Serializable, Comparable<Good> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(Good.class.getName());
	
	private static final double DOLLAR_PER_UNIT = 0.5;
	private static final double EVA_SUIT_VALUE = 100D;
	private static final double VEHICLE_VALUE = 1_000D;
	private static final double LUV_VALUE = 300D;
	
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
	
	private double goodValueBuffer;
	private double modifier;
	private double goodValue;
	private double costOutput;
	
	private GoodType category;

	private List<ManufactureProcessInfo> manufactureProcessInfos;
	private List<FoodProductionProcessInfo> foodProductionProcessInfos;
	
//	private List<ManufactureProcessInfo> componentManuInfos;	
//	private List<FoodProductionProcessInfo> componentFoodInfos;
//	private Map<String, List<ResourceProcess>> resourceProcesses;
	
	/**
	 * Constructor with object.
	 * 
	 * @param name     the name of the good.
	 * @param object   the good's object if any.
	 * @param category the good's category.
	 */
	Good (String name, int id, GoodType category) {
		if (name != null)
			this.name = name.trim().toLowerCase();
		else
			throw new IllegalArgumentException("name cannot be null.");
		this.id = id;

		if (isValidCategory(category))
			this.category = category;
		else
			throw new IllegalArgumentException("category: " + category + " not valid.");
		
		manufactureProcessInfos = ManufactureUtil.getManufactureProcessesWithGivenOutput(name);	
		foodProductionProcessInfos = FoodProductionUtil.getFoodProductionProcessesWithGivenOutput(name);
		
//		componentManuInfos = ManufactureUtil.getManufactureProcessesWithGivenInput(name);	
//		componentFoodInfos = FoodProductionUtil.getFoodProductionProcessesWithGivenInput(name);
//		resourceProcesses = BuildingConfig.getResourceProcessMap();
		
		// Calculate the base cost of each good
		computeBaseCost();
		
//		String s = String.format(" %20s\n "
//				+ "%12s %3.0f\n "
//				+ "%12s %3.0f\n "
//				
//				+ "%12s %5.1f\n "
//				+ "%12s %5.1f\n "
//				+ "%12s %5.1f\n "
//				+ "%12s %5.1f\n "
//				+ "%12s %5.1f\n "
//				
//				+ "%12s %.5f\n "
//				+ "%12s %.5f\n "
//				+ "%12s %.5f\n",
//				name,
//				"count", getCount0(),
//				"count1", getCount1(),
//				
//				"labor Time", laborTime,
//				"power", power,
//				"processTime", processTime,
//				"skill", skill,
//				"tech", tech,
//				
//				"Output Cost", getTotalCostOutput(),
//				"Value", getGoodValue(),
//				"Input Price", computeInputPrice()
//				);
//		
//		System.out.println(s);
	}


	/**
	 * Checks if a category string is valid.
	 * 
	 * @param category the category enum to check.
	 * @return true if valid category.
	 */
	private static boolean isValidCategory(GoodType category) {
		for (GoodType type : GoodType.values()) {
			if (type == category)
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
	public GoodType getCategory() {
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
		
	public void computeModifier() {
		if (category == GoodType.EQUIPMENT && (name.contains(EVASuit.TYPE) || name.contains("eva suit"))) {
			modifier = EVA_SUIT_VALUE;
		}
		else if (category == GoodType.VEHICLE) {
			
			if (name.contains("LUV") || name.contains(LightUtilityVehicle.NAME))
				modifier = LUV_VALUE;
				
			modifier = VEHICLE_VALUE;
		}
		
		modifier = DOLLAR_PER_UNIT;
	}
	
	public double getCount0() {
		return count0_out;
	}
	
	public double getCount1() {
		return count1_out;
	}
	
	public double getTotalCostOutput() {
		return costOutput;
	}
	
	public void computeCostOutput() {
		// First compute the modifier
		this.computeModifier();
		// Then compute the total cost
		costOutput = (getlaborTime() / 250 + getProcessTime() / 1000
					+ getPower() 
					+ getSkill() 
					+ getTech()) * getModifier();	
	}
	
	public double getGoodValue() {
		return goodValue;
	}
	
	public void setGoodValueBuffer(double value) {
		goodValueBuffer = value;
	}
	
	public double getGoodValueBuffer() {
		return goodValueBuffer;
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

			double totalAmount_out = 0;

			for (ManufactureProcessInfo i: manufactureProcessInfos) {
				List<ManufactureProcessItem> items = i.getManufactureProcessItem(name);
				for (ManufactureProcessItem j: items) {
					totalAmount_out += j.getAmount();
				}
				
				double laborTime = i.getWorkTimeRequired();
				double power = i.getPowerRequired();
				double processTime = i.getProcessTimeRequired();
				int skillLevel = i.getSkillLevelRequired();
				int techLevel = i.getTechLevelRequired();	
				
				labor0_out += laborTime;
				power0_out += power;
				process0_out += processTime;
				skill0_out += skillLevel;
				tech0_out += techLevel;
				count0_out++;
				
				if (count0_out != 0) {
					labor0_out = labor0_out/totalAmount_out;
					power0_out = power0_out/totalAmount_out;
					process0_out = process0_out/totalAmount_out;
					skill0_out = skill0_out/totalAmount_out;
					tech0_out = tech0_out/totalAmount_out;
				}		
			}	
		}

		double labor1_out = 0;
		double power1_out = 0;
		double process1_out = 0;
		double skill1_out = 0;
		double tech1_out = 0;

		if (foodProductionProcessInfos != null || !foodProductionProcessInfos.isEmpty()) {

			double totalAmount1_out = 0;
			for (FoodProductionProcessInfo i: foodProductionProcessInfos) {
				List<FoodProductionProcessItem> items = i.getFoodProductionProcessItem(name);
				for (FoodProductionProcessItem j: items) {
					totalAmount1_out += j.getAmount();
				}
				
				double laborTime = i.getWorkTimeRequired();
				double power = i.getPowerRequired();
				double processTime = i.getProcessTimeRequired();
				int skillLevel = i.getSkillLevelRequired();
				int techLevel = i.getTechLevelRequired();	
				
				labor1_out += laborTime;
				power1_out += power;
				process1_out += processTime;
				skill1_out += skillLevel;
				tech1_out += techLevel;
				count1_out++;
			}
			
			if (count1_out != 0) {
				labor1_out = labor1_out/totalAmount1_out;
				power1_out = power1_out/totalAmount1_out;
				process1_out = process1_out/totalAmount1_out;
				skill1_out = skill1_out/totalAmount1_out;
				tech1_out = tech1_out/totalAmount1_out;
			}
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
		
		// Compute the cost of output
		computeCostOutput();
		
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
	

	/**
	 * Computes the base price of each input resource
	 */
	public double computeInputPrice() {

//		double labor0_in = 0;
//		double power0_in = 0;
//		double process0_in = 0;
//		double skill0_in = 0;
//		double tech0_in = 0;
	
		double totalPrice = 0;
		
		if (manufactureProcessInfos != null || !manufactureProcessInfos.isEmpty()) {

			for (ManufactureProcessInfo i: manufactureProcessInfos) {
				// Compute the input resources
				List<ManufactureProcessItem> inputList = i.getInputList();
				for (ManufactureProcessItem j: inputList) {
					
					Good g = GoodsUtil.getResourceGood(j.getName());
					
					double amount_in = j.getAmount();
					
					if (g != null) {
						double cost = getTotalCostOutput() * amount_in;
						double price = g.getGoodValue() * cost;
						totalPrice += price;
					}
					else
						logger.severe("Can't find the good with the name '" + j.getName() + "'");
				}
				
//				double laborTime01 = i.getWorkTimeRequired();
//				double power01 = i.getPowerRequired();
//				double processTime01 = i.getProcessTimeRequired();
//				int skillLevel01 = i.getSkillLevelRequired();
//				int techLevel01 = i.getTechLevelRequired();	
//				
//				labor0_in += laborTime01;
//				power0_in += power01;
//				process0_in += processTime01;
//				skill0_in += skillLevel01;
//				tech0_in += techLevel01;
//				count0_in++;
//				
//				if (count0_in != 0) {
//					labor0_in = labor0_in/totalAmount_in;
//					power0_in = power0_in/totalAmount_in;
//					process0_in = process0_in/totalAmount_in;
//					skill0_in = skill0_in/totalAmount_in;
//					tech0_in = tech0_in/totalAmount_in;
//				}
			}
		}
		
		if (foodProductionProcessInfos != null || !foodProductionProcessInfos.isEmpty()) {

			for (FoodProductionProcessInfo i: foodProductionProcessInfos) {
				// Compute the input resources
				List<FoodProductionProcessItem> inputList = i.getInputList();
				for (FoodProductionProcessItem j: inputList) {
					
					Good g = GoodsUtil.getResourceGood(j.getName());
					
					double amount_in = j.getAmount();
					
					if (g != null) {
						double cost = getTotalCostOutput() * amount_in;
						double price = g.getGoodValue() * cost;
						totalPrice += price;
					}
						
					else
						logger.severe("Can't find the good with the name '" + j.getName() + "'");
				}
			}	
		}
		
//		totalPriceInput = (labor0_in / 250 + process0_in / 1000
//				+ power0_in
//				+ skill0_in 
//				+ tech0_in) * getModifier();
		
		return totalPrice;
	}
	
	/**
	 * Computes the base good value of each input resource
	 */
	public double computeInputValue() {

//		double labor0_in = 0;
//		double power0_in = 0;
//		double process0_in = 0;
//		double skill0_in = 0;
//		double tech0_in = 0;
	
		double totalValue = 0;
		
		if (manufactureProcessInfos != null || !manufactureProcessInfos.isEmpty()) {

			for (ManufactureProcessInfo i: manufactureProcessInfos) {
				// Compute the input resources
				List<ManufactureProcessItem> inputList = i.getInputList();
				for (ManufactureProcessItem j: inputList) {
					
					Good g = GoodsUtil.getResourceGood(j.getName());
					
					double amount_in = j.getAmount();
					
					if (g != null) {
						double value = g.getGoodValue() / amount_in;
						totalValue += value;
					}
					else
						logger.severe("Can't find the good with the name '" + j.getName() + "'");
				}
				
//				double laborTime01 = i.getWorkTimeRequired();
//				double power01 = i.getPowerRequired();
//				double processTime01 = i.getProcessTimeRequired();
//				int skillLevel01 = i.getSkillLevelRequired();
//				int techLevel01 = i.getTechLevelRequired();	
//				
//				labor0_in += laborTime01;
//				power0_in += power01;
//				process0_in += processTime01;
//				skill0_in += skillLevel01;
//				tech0_in += techLevel01;
//				count0_in++;
//				
//				if (count0_in != 0) {
//					labor0_in = labor0_in/totalAmount_in;
//					power0_in = power0_in/totalAmount_in;
//					process0_in = process0_in/totalAmount_in;
//					skill0_in = skill0_in/totalAmount_in;
//					tech0_in = tech0_in/totalAmount_in;
//				}
			}
		}
		
		if (foodProductionProcessInfos != null || !foodProductionProcessInfos.isEmpty()) {

			for (FoodProductionProcessInfo i: foodProductionProcessInfos) {
				// Compute the input resources
				List<FoodProductionProcessItem> inputList = i.getInputList();
				for (FoodProductionProcessItem j: inputList) {
					
					Good g = GoodsUtil.getResourceGood(j.getName());
					
					double amount_in = j.getAmount();
					
					if (g != null) {
						double value = g.getGoodValue() / amount_in;
						totalValue += value;
					}
						
					else
						logger.severe("Can't find the good with the name '" + j.getName() + "'");
				}
			}	
		}
		
//		totalPriceInput = (labor0_in / 250 + process0_in / 1000
//				+ power0_in
//				+ skill0_in 
//				+ tech0_in) * getModifier();
		
		return totalValue;
	}
	
	public void setGoodValue(double value) {
		goodValue = value;
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