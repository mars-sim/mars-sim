/**
 * Mars Simulation Project
 * SalvageProcessInfo.java
 * @version 3.1.0 2019-01-09
 * @author Scott Davis
 */
package org.mars_sim.msp.core.manufacture;

import java.io.Serializable;
import java.util.List;

/**
 * Information about a type of salvage.
 */
public class SalvageProcessInfo implements Serializable, Comparable<SalvageProcessInfo> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private String itemName;
	private String type;
	private int techLevelRequired;
	private int skillLevelRequired;
	private double workTimeRequired;
	private List<PartSalvage> partSalvageList;

	/**
	 * Gets the salvage item name.
	 * 
	 * @return item name.
	 */
	public String getItemName() {
		return itemName;
	}

	/**
	 * Sets the salvage item name.
	 * 
	 * @param itemName the item name.
	 */
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	/**
	 * Gets the salvage item type.
	 * 
	 * @return item type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the salvage item type.
	 * 
	 * @param type the item type.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the manufacturing tech level required for the salvage process.
	 * 
	 * @return tech level.
	 */
	public int getTechLevelRequired() {
		return techLevelRequired;
	}

	/**
	 * Sets the manufacturing tech level required for the salvate process.
	 * 
	 * @param techLevelRequired the required tech level.
	 */
	public void setTechLevelRequired(int techLevelRequired) {
		this.techLevelRequired = techLevelRequired;
	}

	/**
	 * Gets the material science skill level required to work on the salvage
	 * process.
	 * 
	 * @return skill level.
	 */
	public int getSkillLevelRequired() {
		return skillLevelRequired;
	}

	/**
	 * Sets the material science skill level required to work on the salvage
	 * process.
	 * 
	 * @param skillLevelRequired skill level.
	 */
	public void setSkillLevelRequired(int skillLevelRequired) {
		this.skillLevelRequired = skillLevelRequired;
	}

	/**
	 * Gets the work time required to complete the salvage process.
	 * 
	 * @return work time (millisols).
	 */
	public double getWorkTimeRequired() {
		return workTimeRequired;
	}

	/**
	 * Sets the work time required to complete the salvage process.
	 * 
	 * @param workTimeRequired work time (millisols).
	 */
	public void setWorkTimeRequired(double workTimeRequired) {
		this.workTimeRequired = workTimeRequired;
	}

	/**
	 * Gets a list of the parts that can be salvaged.
	 * 
	 * @return salvage parts.
	 */
	public List<PartSalvage> getPartSalvageList() {
		return partSalvageList;
	}

	/**
	 * Sets the list of the parts that can be salvaged.
	 * 
	 * @param partSalvageList the list of salvage parts.
	 */
	public void setPartSalvageList(List<PartSalvage> partSalvageList) {
		this.partSalvageList = partSalvageList;
	}

	/**
	 * Compares this object with the specified object for order.
	 * 
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	public int compareTo(SalvageProcessInfo p) {
		return itemName.compareToIgnoreCase(p.itemName);
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		itemName = null;
		type = null;
		if (partSalvageList != null)
			partSalvageList.clear();
		partSalvageList = null;
	}

	@Override
	public String toString() {
		return "salvage " + itemName;
	}
}