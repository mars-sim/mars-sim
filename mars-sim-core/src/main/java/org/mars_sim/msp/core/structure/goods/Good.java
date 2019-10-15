/**
 * Mars Simulation Project
 * Good.java
 * @version 3.1.0 2018-12-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.goods;

import java.io.Serializable;

import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;

/**
 * A meta class describing an economic good in the simulation.
 */
public class Good implements Serializable, Comparable<Good> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	// Data members
	private String name;
	
	private int id;
	
	private GoodType category;
	
//	private List<ManufactureProcessInfo> manufactureProcessInfos;
//	private List<FoodProductionProcessInfo> foodProductionProcessInfos;
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
		
//		manufactureProcessInfos = ManufactureUtil.getManufactureProcessesWithGivenOutput(name);	
//		foodProductionProcessInfos = FoodProductionUtil.getFoodProductionProcessesWithGivenOutput(name);
//		resourceProcesses = BuildingConfig.getResourceProcessMap();
		
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

//	public void getCost() {
//		double aveLaborTime = 0;
//		double avePower = 0;
//		double aveProcessTime = 0;
//		double aveSkillLevel = 0;
//		double aveTechLevel = 0;
//		
//		if (manufactureProcessInfos != null || !manufactureProcessInfos.isEmpty()) {
//			int count = 0;
//			for (ManufactureProcessInfo i: manufactureProcessInfos) {
//				double laborTime = i.getWorkTimeRequired();
//				double power = i.getPowerRequired();
//				double processTime = i.getProcessTimeRequired();
//				int skillLevel = i.getSkillLevelRequired();
//				int techLevel = i.getTechLevelRequired();	
//				
//				aveLaborTime += laborTime;
//				avePower += power;
//				aveProcessTime += processTime;
//				aveSkillLevel += skillLevel;
//				aveTechLevel += techLevel;
//				count++;
//			}
//			
//			aveLaborTime += aveLaborTime/count;
//			avePower += avePower/count;
//			aveProcessTime += aveProcessTime/count;
//			aveSkillLevel += aveSkillLevel/count;
//			aveTechLevel += aveTechLevel/count;
//			
//		}
//		
//		if (foodProductionProcessInfos != null || !foodProductionProcessInfos.isEmpty()) {
//			for (FoodProductionProcessInfo i: foodProductionProcessInfos) {
//				double laborTime = i.getWorkTimeRequired();
//				double power = i.getPowerRequired();
//				double processTime = i.getProcessTimeRequired();
//				int skillLevel = i.getSkillLevelRequired();
//				int techLevel = i.getTechLevelRequired();			
//			}
//		}
//		
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
//	}
	
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