/**
 * Mars Simulation Project
 * SalvageProcessInfo.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.manufacture;

import java.util.List;

import com.mars_sim.core.process.ProcessInfo;
import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.resource.ItemType;

/**
 * Information about a type of salvage.
 */
public class SalvageProcessInfo extends ProcessInfo {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ProcessItem salvaged;

	public SalvageProcessInfo(ProcessItem salvaged, String description, int techLevelRequired, int skillLevelRequired,
			double workTimeRequired, List<ProcessItem> outputList) {
		super("Salvage " + salvaged.getName(), description, techLevelRequired, skillLevelRequired, workTimeRequired,
		        0D, 0D,
		        List.of(salvaged), outputList);
		this.salvaged = salvaged;
	}

	/**
	 * Returns the salvaged item name
	 * @return
	 */
	public String getItemName() {
		return salvaged.getName();		
	}

	/**
	 * Get the type of item being salvaged
	 * @return
	 */
	public ItemType getType() {
		return salvaged.getType();
	}
}
