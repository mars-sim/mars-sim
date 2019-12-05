/**
 * Mars Simulation Project
 * BuildingPanelStorage.java
 * @version 3.1.0 2017-09-15
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.Conversion;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;


/**
 * The BuildingPanelStorage class is a building function panel representing
 * the storage capacity of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelStorage
extends BuildingFunctionPanel {

	/**
	 * Constructor.
	 * @param storage the storage building function.
	 * @param desktop the main desktop.
	 */
	public BuildingPanelStorage(Storage storage, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(storage.getBuilding(), desktop);

		setLayout(new BorderLayout(0, 0));

		// Create storage label.
		WebLabel storageLabel = new WebLabel(Msg.getString("BuildingPanelStorage.title"), WebLabel.CENTER);
		storageLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//storageLabel.setForeground(new Color(102, 51, 0)); // dark brown

		WebPanel titlePanel = new WebPanel(new GridLayout(2,1,0,0));
		add(titlePanel, BorderLayout.NORTH);
		titlePanel.add(storageLabel);
		titlePanel.setOpaque(false);
		titlePanel.setBackground(new Color(0,0,0,128));

		WebLabel maxCapLabel = new WebLabel(Msg.getString("BuildingPanelStorage.maxCap"), WebLabel.CENTER);
		titlePanel.add(maxCapLabel);

		Map<Integer, Double> resourceStorage = storage.getResourceStorageCapacity();
	
		// Create resource storage panel.
		WebPanel resourceStoragePanel = new WebPanel(new GridLayout(resourceStorage.size(), 2, 0, 0));
		add(resourceStoragePanel, BorderLayout.CENTER);
		resourceStoragePanel.setOpaque(false);
		resourceStoragePanel.setBackground(new Color(0,0,0,128));

		SortedSet<Integer> keys = new TreeSet<Integer>(resourceStorage.keySet());
		for (Integer resource : keys) { 
			// Create resource label.
			WebLabel resourceLabel = new WebLabel(
					Conversion.capitalize(ResourceUtil.findAmountResourceName(resource)) 
					+ ":", WebLabel.LEFT);
			resourceStoragePanel.add(resourceLabel);

			double capacity = resourceStorage.get(resource);
			WebLabel capacityLabel = new WebLabel((int) capacity + " kg", WebLabel.RIGHT);
			resourceStoragePanel.add(capacityLabel);
		}
	}

	@Override
	public void update() {
		// Storage capacity doesn't change so nothing to update.
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		// take care to avoid null exceptions

	}
}