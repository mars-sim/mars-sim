/**
 * Mars Simulation Project
 * BuildingPanelStorage.java
 * @version 3.07 2014-11-21
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.Conversion;

import javax.swing.*;

import java.awt.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The BuildingPanelStorage class is a building function panel representing
 * the storage capacity of a settlement building.
 */
public class BuildingPanelStorage
extends BuildingFunctionPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

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
		// 2014-11-21 Changed font type, size and color and label text
		// 2014-11-21 Added internationalization for labels
		JLabel storageLabel = new JLabel(Msg.getString("BuildingPanelStorage.title"), JLabel.CENTER);
		storageLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//storageLabel.setForeground(new Color(102, 51, 0)); // dark brown


		JPanel titlePanel = new JPanel(new GridLayout(2,1,0,0));
		add(titlePanel, BorderLayout.NORTH);
		titlePanel.add(storageLabel);
		titlePanel.setOpaque(false);
		titlePanel.setBackground(new Color(0,0,0,128));

		JLabel maxCapLabel = new JLabel(Msg.getString("BuildingPanelStorage.maxCap"), JLabel.CENTER);
		titlePanel.add(maxCapLabel);

		Map<AmountResource, Double> resourceStorage = storage.getResourceStorageCapacity();
	
		// Create resource storage panel.
		JPanel resourceStoragePanel = new JPanel(new GridLayout(resourceStorage.size(), 2, 0, 0));
		add(resourceStoragePanel, BorderLayout.CENTER);
		resourceStoragePanel.setOpaque(false);
		resourceStoragePanel.setBackground(new Color(0,0,0,128));
/*
		Iterator<AmountResource> i = resourceStorage.keySet().iterator();
		while (i.hasNext()) {
			AmountResource resource = i.next();

			// Create resource label.
			// 2014-11-20 Capitalized resource names
			JLabel resourceLabel = new JLabel(Conversion.capitalize(resource.getName()) + ":", JLabel.LEFT);
			resourceStoragePanel.add(resourceLabel);

			double capacity = resourceStorage.get(resource);
			JLabel capacityLabel = new JLabel((int) capacity + " kg", JLabel.RIGHT);
			resourceStoragePanel.add(capacityLabel);
		}
*/
		SortedSet<AmountResource> keys = new TreeSet<AmountResource>(resourceStorage.keySet());
		for (AmountResource resource : keys) { 

			// Create resource label.
			// 2014-11-20 Capitalized resource names
			JLabel resourceLabel = new JLabel(Conversion.capitalize(resource.getName()) + ":", JLabel.LEFT);
			resourceStoragePanel.add(resourceLabel);

			double capacity = resourceStorage.get(resource);
			JLabel capacityLabel = new JLabel((int) capacity + " kg", JLabel.RIGHT);
			resourceStoragePanel.add(capacityLabel);
		}
	}

	@Override
	public void update() {
		// Storage capacity doesn't change so nothing to update.
	}
}