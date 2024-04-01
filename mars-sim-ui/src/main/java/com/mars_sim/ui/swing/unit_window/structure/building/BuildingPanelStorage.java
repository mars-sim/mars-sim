/*
 * Mars Simulation Project
 * BuildingPanelStorage.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure.building;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.equipment.ResourceHolder;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.building.function.Storage;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.NumberCellRenderer;
import com.mars_sim.ui.swing.unit_window.TabPanelTable;


/**
 * The BuildingPanelStorage class is a building function panel representing
 * the storage capacity of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelStorage extends TabPanelTable {

	private static final String STORE_ICON = "stock";

	private static class StorageTableModel extends AbstractTableModel {
		private List<String> nameList = new ArrayList<>();
		private Map<String, Double> buildingStorage = new HashMap<>();
		private Map<String, Double> settlementStorage = new HashMap<>();
		private Map<String, Double> available = new HashMap<>();
		
		private ResourceHolder holder;
		
		public StorageTableModel(Storage storage) {	
			holder = storage.getBuilding().getAssociatedSettlement();
			
			Map<Integer, Double> resourceStorage = storage.getResourceStorageCapacity();
			for (Entry<Integer, Double> resource : resourceStorage.entrySet()) {
				int id = resource.getKey();
				String name = ResourceUtil.findAmountResourceName(id);
				nameList.add(name);
				buildingStorage.put(name, resource.getValue());

				available.put(name, holder.getAllAmountResourceStored(id));
				
				ResourceHolder rh = (ResourceHolder)storage.getBuilding().getSettlement();
				settlementStorage.put(name, rh.getAmountResourceCapacity(id));
			}
			
			Collections.sort(nameList);
		}

		
		public int getRowCount() {
			return nameList.size();
		}

		public int getColumnCount() {
			return 4;
		}

		public Class<?> getColumnClass(int column) {
			if (column == 0) return String.class;
			else return Double.class;
		}

		public String getColumnName(int column) {
			if (column == 0) {
				return "Resource";
			}
			else if (column == 1) {
				return "Total Stored (kg)";
			}
			else if (column == 2) {
				return "Building Cap (kg)";
			}
			else {
				return "Settlement Cap (kg)";
			}
		}

		public Object getValueAt(int row, int column) {
			if (column == 0) {
				return nameList.get(row);
			}
			else if (column == 1) {
				return available.get(nameList.get(row));
			}
			else if (column == 2) {
				return buildingStorage.get(nameList.get(row));
			}
			else {
				return settlementStorage.get(nameList.get(row));
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
			Msg.getString("BuildingPanelStorage.tabTitle"), 
			desktop
		);
		
		this.storage = storage;
	}
	
	@Override
	protected void setColumnDetails(TableColumnModel columnModel) {
		var r = new NumberCellRenderer(0);
		columnModel.getColumn(1).setCellRenderer(r);
		columnModel.getColumn(2).setCellRenderer(r);

		columnModel.getColumn(3).setCellRenderer(new NumberCellRenderer(0));
	}

	/**
	 * Builds the UI.
	 */
	@Override
	protected TableModel createModel() {

		// Prepare medical table model
		return new StorageTableModel(storage);
	}
}
