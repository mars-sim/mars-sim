/*
 * Mars Simulation Project
 * SettlementMapLayer.java
 * @date 2023-06-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.JMenuItem;

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
	 * @return 
	 */
	public Collection<? extends MapHotspot<?>> displayLayer(Settlement settlement, MapViewPoint viewpoint);

	/**
	 * Destroy the map layer.
	 */
	public void destroy();

	/**
	 * Gets the filter controls for the map layer.
	 * 
	 * @return list of filter controls as JMenuItems.
	 */
	public default List<JMenuItem> getFilterControls() {
		return Collections.emptyList();
	}

	/**
	 * Saves any layer-specific UI properties.
	 *
	 * @param props the target properties collection.
	 */
	public default void saveUIProperties(Properties props) {
		// Default no-op.
	}
}
