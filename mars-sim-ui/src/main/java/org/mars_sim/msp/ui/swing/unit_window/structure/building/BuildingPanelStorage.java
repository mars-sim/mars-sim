/*
 * Mars Simulation Project
 * BuildingPanelStorage.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.Conversion;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;


/**
 * The BuildingPanelStorage class is a building function panel representing
 * the storage capacity of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelStorage extends BuildingFunctionPanel {

	private static final String STORE_ICON = Msg.getString("icon.stock"); //$NON-NLS-1$

	private Storage storage;

	/**
	 * Constructor.
	 * 
	 * @param storage the storage building function.
	 * @param desktop the main desktop.
	 */
	public BuildingPanelStorage(Storage storage, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelStorage.tabTitle"), 
			ImageLoader.getNewIcon(STORE_ICON),
			storage.getBuilding(), 
			desktop
		);
		
		this.storage = storage;
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {

		Map<Integer, Double> resourceStorage = storage.getResourceStorageCapacity();

		// Create resource storage panel.
		WebPanel resourceStoragePanel = new WebPanel(new GridLayout(resourceStorage.size(), 2, 0, 5));
		addBorder(resourceStoragePanel, "Capacities");
		center.add(resourceStoragePanel, BorderLayout.NORTH);

		SortedSet<Integer> keys = new TreeSet<>(resourceStorage.keySet());
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
}
