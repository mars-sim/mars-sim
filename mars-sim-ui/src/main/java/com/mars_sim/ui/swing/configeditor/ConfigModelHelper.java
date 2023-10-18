/*
 * Mars Simulation Project
 * ConfigModelHelper.java
 * @date 2022-06-15
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.configeditor;

import com.mars_sim.core.structure.SettlementConfig;
import com.mars_sim.core.structure.SettlementTemplate;

class ConfigModelHelper {

	// Private constructor to stop instantiation
	private ConfigModelHelper() {
	}
	
	/**
	 * Determines the new settlement population.
	 * 
	 * @param templateName
	 *            the settlement template name.
	 * @param settlementConfig 
	 * @return the new population number.
	 */
	static int determineNewSettlementPopulation(String templateName, SettlementConfig settlementConfig) {

		int result = 0; 
		if (templateName != null) {
			SettlementTemplate template = settlementConfig.getItem(templateName);
			if (template != null) {
				result = template.getDefaultPopulation();
			}
		}

		return result;
	}

	/**
	 * Determines the new settlement number of robots.
	 * 
	 * @param templateName
	 *            the settlement template name.
	 * @return number of robots.
	 */
	static int determineNewSettlementNumOfRobots(String templateName, SettlementConfig settlementConfig) {

		int result = 0;

		if (templateName != null) {
			SettlementTemplate template = settlementConfig.getItem(templateName);
			if (template != null) {
				result = template.getDefaultNumOfRobots();
			}
		}

		return result;
	}


}
