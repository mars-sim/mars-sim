/*
 * Mars Simulation Project
 * Good.java
 * @date 2022-06-16
 * @author Scott Davis
 */
package org.mars_sim.msp.core.goods;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.food.FoodProductionProcessInfo;
import org.mars_sim.msp.core.food.FoodProductionProcessItem;
import org.mars_sim.msp.core.food.FoodProductionUtil;
import org.mars_sim.msp.core.manufacture.ManufactureProcess;
import org.mars_sim.msp.core.manufacture.ManufactureProcessInfo;
import org.mars_sim.msp.core.manufacture.ManufactureProcessItem;
import org.mars_sim.msp.core.manufacture.ManufactureUtil;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ItemType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A meta class describing an economic good in the simulation.
 */
public abstract class Good implements Serializable, Comparable<Good> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static final double LABOR_FACTOR = 150D ;
	private static final double PROCESS_TIME_FACTOR = 500D;
	private static final double POWER_FACTOR = 1D;
	private static final double SKILL_FACTOR = 1D;
	private static final double TECH_FACTOR = 2D;

	//TODO Initialise explictly
	protected static MissionManager missionManager = Simulation.instance().getMissionManager();

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

	private List<ManufactureProcessInfo> manufactureProcessInfos;
	private List<FoodProductionProcessInfo> foodProductionProcessInfos;

	/**
	 * Constructor with object.
	 *
	 * @param name     the name of the good.
	 * @param object   the good's object if any.
	 */
	protected Good (String name, int id) {
		this.name = name;
		this.id = id;
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
	 * @deprecated
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
	public abstract GoodCategory getCategory();

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
	protected abstract double computeCostModifier();

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
	 * Gets the amount of this good being produced at the settlement by ongoing
	 * manufacturing processes.
	 *
	 * @param settlement Place producing the Good
	 * @return amount (kg for amount resources, number for parts, equipment, and
	 *         vehicles).
	 */
	protected double getManufacturingProcessOutput(Settlement settlement) {

		double result = 0D;

		for(Building b : settlement.getBuildingManager().getBuildings(FunctionType.MANUFACTURE)) {
			// Go through each ongoing active manufacturing process.
			for(ManufactureProcess process : b.getManufacture().getProcesses()) {
				for(ManufactureProcessItem item : process.getInfo().getOutputList()) {
					if (item.getName().equalsIgnoreCase(name)) {
						result += item.getAmount();
					}
				}
			}
		}

		return result;
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
		return id % 64;
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
		return this.id == g.getID();
	}
    
	/**
     * Gets the mass per item for a good.
     *
     * @return mass (kg) per item (or 1kg for amount resources).
     * @throws Exception if error getting mass per item.
     */
    public abstract double getMassPerItem();

	/**
     * Gets the good type
     * 
     * @return
     */
	public abstract GoodType getGoodType();

	
	/**
	 * Gets the number of this good being in use or being produced at this moment at
	 * the settlement.
	 *
	 * @param settlement Settlement to check
	 * @return the number of the good (or amount (kg) if amount resource good).
	 */
	public abstract double getNumberForSettlement(Settlement settlement);

	    /**
     * Get a stream of all Vehciles on Missions from the target Settlement
     * @param settlement Settlement being checked
     */
    protected Stream<Vehicle> getVehiclesOnMissions(Settlement settlement) {
        return missionManager.getMissionsForSettlement(settlement).stream()
                .filter(VehicleMission.class::isInstance)
                .map(vm -> ((VehicleMission) vm).getVehicle())
                .filter(Objects::nonNull);
    }
    
    /**
     * Get a stream of Person on EVA at the Settlement
     * @param settlement
     * @return
     */
    protected Stream<Person> getPersonOnEVA(Settlement settlement) {
        return  settlement.getAllAssociatedPeople().stream()
			              .filter(p -> p.isOutside());
    }

	/**
	 * Get the price for this Good at a Settlment with a specific Value Point
	 * @param settlement Get the price at
	 * @param value Value Point for the good
	 */
    abstract double getPrice(Settlement settlement, double value);
}
