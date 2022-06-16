/*
 * Mars Simulation Project
 * ConfigModelHelper.java
 * @date 2022-06-15
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.configeditor;

import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.structure.SettlementTemplate;

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
			SettlementTemplate template = settlementConfig.getSettlementTemplate(templateName);
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
			SettlementTemplate template = settlementConfig.getSettlementTemplate(templateName);
			if (template != null) {
				result = template.getDefaultNumOfRobots();
			}
		}

		return result;
	}


}
