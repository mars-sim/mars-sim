/*
 * Mars Simulation Project
 * Good.java
 * @date 2022-06-16
 * @author Scott Davis
 */
package org.mars_sim.msp.core.goods;

import java.io.Serializable;
import java.util.List;

import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.food.FoodProductionProcessInfo;
import org.mars_sim.msp.core.food.FoodProductionProcessItem;
import org.mars_sim.msp.core.food.FoodProductionUtil;
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
	private static final double EVA_SUIT_VALUE = 50;
	private static final double CONTAINER_VALUE = .1;
	
	private static final int ROBOT_VALUE = 200;
	
	private static final int VEHICLE_PART_VALUE = 3;
	
	private static final int VEHICLE_VALUE = 20;
	private static final int LUV_VALUE = 750;
	private static final int DRONE_VALUE = 50;

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

	private static final double ITEM_VALUE = 1.1D;
	private static final double FC_STACK_VALUE = 8;
	private static final double FC_VALUE = 1;
	private static final double BOARD_VALUE = 1;
	private static final double CPU_VALUE = 10;
	private static final double WAFER_VALUE = 50;
	private static final double BATTERY_VALUE = 2;
	private static final double INSTRUMENT_VALUE = 1;
	private static final double WIRE_VALUE = .005;
	private static final double ELECTRONIC_VALUE = .1;
	
	private static final double LABOR_FACTOR = 150D ;
	private static final double PROCESS_TIME_FACTOR = 500D;
	private static final double POWER_FACTOR = 1D;
	private static final double SKILL_FACTOR = 1D;
	private static final double TECH_FACTOR = 2D;

	// Data members
	private String name;

	private int id;
	private int count0_out;
	private int count1_out;

	private double laborTime;
	private double power;
	private double processTime;
	private double skill;
	private double tech;

	private double costModifier = -1;
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
	public EquipmentType getEquipmentType() {
		return EquipmentType.convertName2Enum(name);
	}

	/**
	 * Gets the good's id.
	 * 
	 * @return
	 */
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
		return costModifier;
	}

	public double getCount0() {
		return count0_out;
	}

	public double getCount1() {
		return count1_out;
	}

	/**
	 * Gets the cost of output.
	 */
	public double getCostOutput() {
		if (costOutput == -1)
			computeOutputCost();
		return costOutput;
	}

	/**
	 * Calculates the modified cost of output.
	 */
	public void computeOutputCost() {
		// First compute the modifier
		if (costModifier == -1) {
			costModifier = computeCostModifier();
			// Then compute the total cost
			costOutput = (0.01 + costModifier) * (
					1 + getlaborTime() / LABOR_FACTOR
					+ getProcessTime() / PROCESS_TIME_FACTOR
					+ getPower() / POWER_FACTOR
					+ getSkill() / SKILL_FACTOR
					+ getTech() / TECH_FACTOR);
		}
	}

	/**
	 * Computes the cost modifier for calculating output cost.
	 * 
	 * @return
	 */
	public double computeCostModifier() {
		double result = 0;
		if (category == GoodCategory.AMOUNT_RESOURCE) {

			AmountResource ar = ResourceUtil.findAmountResource(id);
			boolean edible = ar.isEdible();
			boolean lifeSupport = ar.isLifeSupport();
			String type = ar.getType();

			if (lifeSupport)
				result += LIFE_SUPPORT_VALUE;
			
			if (type != null && type.equalsIgnoreCase("waste"))
				result += WASTE_VALUE ;
			
			else if (ar.getName().equalsIgnoreCase("chlorine"))
				result += CL_VALUE;
			else if (ar.getName().equalsIgnoreCase("carbon dioxide"))
				result += CO2_VALUE;
			else if (ar.getName().equalsIgnoreCase("ice"))
				result += ICE_VALUE;

			else if (edible) {
				if (type != null && type.equalsIgnoreCase("derived"))
					result += DERIVED_VALUE;
				else if (type != null && type.equalsIgnoreCase("soy-based"))
					result += SOY_VALUE;
				else if (type != null && type.equalsIgnoreCase("animal"))
					result += ANIMAL_VALUE;
				else
					result += FOOD_VALUE;
			}

			else if (type != null && type.equalsIgnoreCase("medical"))
				result += MEDICAL_VALUE;
			else if (type != null && type.equalsIgnoreCase("oil"))
				result += OIL_VALUE;
			else if (type != null && type.equalsIgnoreCase("crop"))
				result += CROP_VALUE;
			else if (type != null && type.equalsIgnoreCase("rock"))
				result += ROCK_VALUE;
			else if (type != null && type.equalsIgnoreCase("regolith"))
				result += REGOLITH_VALUE;
			else if (type != null && type.equalsIgnoreCase("ore"))
				result += ORE_VALUE;
			else if (type != null && type.equalsIgnoreCase("mineral"))
				result += MINERAL_VALUE;
			else if (type != null && type.equalsIgnoreCase("element"))
				result += ELEMENT_VALUE;
			else if (type != null && type.equalsIgnoreCase("chemical"))
				result += CHEMICAL_VALUE;
			else
				result += STANDARD_AMOUNT_VALUE ;
		}

		else if (category == GoodCategory.ITEM_RESOURCE) {
			Part part = ItemResourceUtil.findItemResource(id);
			String name = part.getName().toLowerCase();
			String type = part.getType();
			
			if (type != null && type.equalsIgnoreCase("vehicle"))
				result += VEHICLE_PART_VALUE ;
			else if (name.contains("electronic"))
				result += ELECTRONIC_VALUE;
			
			if (name.equalsIgnoreCase("fuel cell stack"))
				result += FC_STACK_VALUE;
			else if (name.equalsIgnoreCase("solid oxide fuel cell"))
				result += FC_VALUE;
			else if (name.contains("board"))
				result += BOARD_VALUE;
			else if (name.equalsIgnoreCase("microcontroller"))
				result += CPU_VALUE;
			else if (name.equalsIgnoreCase("semiconductor wafer"))
				result += WAFER_VALUE;
			else if (name.contains("battery"))
				result += BATTERY_VALUE;
			else if (name.contains("instrument"))
				result += INSTRUMENT_VALUE;
			else if (name.contains("wire"))
				result += WIRE_VALUE;

			result += ITEM_VALUE;// * weight;
		}

		// Note: 
		else if (category == GoodCategory.CONTAINER) {
			return CONTAINER_VALUE;
		}
		
		else if (category == GoodCategory.EQUIPMENT) {
			return EVA_SUIT_VALUE;
		}

		else if (category == GoodCategory.ROBOT) {
			return ROBOT_VALUE;
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
		
		return result;
	}
	
	public double getAverageGoodValue() {
		return averageGoodValue;
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

	}


	/**
	 * Sets the average good value.
	 * 
	 * @param value
	 */
	public void setAverageGoodValue(double value) {
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

	/**
	 * Is this object the same as another object ?
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		Good g = (Good) obj;
		return this.getName().equals(g.getName())
				&& this.id == g.getID();
	}

}
