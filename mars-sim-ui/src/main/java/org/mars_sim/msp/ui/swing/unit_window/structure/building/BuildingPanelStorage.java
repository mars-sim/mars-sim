/*
 * Mars Simulation Project
 * BuildingPanelStorage.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;


/**
 * The BuildingPanelStorage class is a building function panel representing
 * the storage capacity of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelStorage extends BuildingFunctionPanel {

	private static final String STORE_ICON = "stock";

	private static class StorageTableModel extends AbstractTableModel {
		private List<String> name = new ArrayList<>();
		private List<Integer> caps = new ArrayList<>();
		
		public StorageTableModel(Storage storage) {
			Map<Integer, Double> resourceStorage = storage.getResourceStorageCapacity();
			for (Entry<Integer, Double> resource : resourceStorage.entrySet()) {
				// Create resource label.
				name.add(ResourceUtil.findAmountResourceName(resource.getKey()));
				caps.add(resource.getValue().intValue());
			}
		}

		
		public int getRowCount() {
			return name.size();
		}

		public int getColumnCount() {
			return 2;
		}

		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) return String.class;
			else return Integer.class;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return "Resource";
			else return "Capacity (kg)";
		}

		public Object getValueAt(int row, int column) {
			if (column == 0) {
				return name.get(row);
			}
			else {
				return caps.get(row);
			}
		}

	}

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
			ImageLoader.getIconByName(STORE_ICON),
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

		// Create scroll panel for storage table
		JScrollPane scrollPanel = new JScrollPane();
		scrollPanel.setPreferredSize(new Dimension(160, 120));
		center.add(scrollPanel, BorderLayout.CENTER);
	    scrollPanel.getViewport().setOpaque(false);
	    scrollPanel.setOpaque(false);

		// Prepare medical table model
		StorageTableModel model = new StorageTableModel(storage);

		// Prepare medical table
		JTable storageTable = new JTable(model);
		storageTable.setCellSelectionEnabled(false);
		storageTable.setAutoCreateRowSorter(true);
		scrollPanel.setViewportView(storageTable);
	}
}
