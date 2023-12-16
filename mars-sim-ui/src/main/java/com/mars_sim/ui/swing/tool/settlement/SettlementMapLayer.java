/*
 * Mars Simulation Project
 * SettlementMapLayer.java
 * @date 2023-06-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.settlement;

import com.mars_sim.core.structure.Settlement;

/**
 * An interface for a display layer on the settlement map.
 */
public interface SettlementMapLayer {

	/**
	 * Displays the settlement map layer.
	 * 
	 * @param settlement the settlement to display.
	 * @param viewpoint  the viewpoint of the Map.
	 */
	public void displayLayer(Settlement settlement, MapViewPoint viewpoint);

	/**
	 * Destroy the map layer.
	 */
	public void destroy();
}
