/*
 * Mars Simulation Project
 * BuildingTableModel.java
 * @date 2021-12-07
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;

/**
 * The BuildingTableModel maintains a list of Building objects. By defaults the source
 * of the list is the Unit Manager. 
 */
@SuppressWarnings("serial")
public class BuildingTableModel extends UnitTableModel {

	/** default logger. */
	private static final Logger logger = Logger.getLogger(BuildingTableModel.class.getName());
	
	// Column indexes
	private static final int NAME = 0;
	private static final int TYPE = 1;
	private static final int POWER_MODE = 2;
	private static final int POWER_REQUIRED = 3;
	private static final int HEAT_MODE = 4;
	private static final int TEMPERATURE = 5;
	private static final int COLUMNCOUNT = 6;

	/** Names of Columns. */
	private static String columnNames[];
	/** Types of Columns. */
	private static Class<?> columnTypes[];

	private UnitManagerListener unitManagerListener;

	private static List<UnitEventType> powerEvents = new ArrayList<>();

	/**
	 * The static initializer creates the name & type arrays.
	 */
	static {
		
		powerEvents.add(UnitEventType.POWER_MODE_EVENT);
		powerEvents.add(UnitEventType.GENERATED_POWER_EVENT);
		powerEvents.add(UnitEventType.STORED_POWER_EVENT);
		powerEvents.add(UnitEventType.STORED_POWER_CAPACITY_EVENT);
		powerEvents.add(UnitEventType.REQUIRED_POWER_EVENT);
		powerEvents.add(UnitEventType.POWER_VALUE_EVENT);
		
		columnNames = new String[COLUMNCOUNT];
		columnTypes = new Class[COLUMNCOUNT];
		columnNames[NAME] = Msg.getString("BuildingTableModel.column.name"); //$NON-NLS-1$
		columnTypes[NAME] = String.class;
		columnNames[TYPE] = Msg.getString("BuildingTableModel.column.type"); //$NON-NLS-1$
		columnTypes[TYPE] = String.class;
		columnNames[POWER_MODE] = Msg.getString("BuildingTableModel.column.powerMode"); //$NON-NLS-1$
		columnTypes[POWER_MODE] = String.class;		
		columnNames[HEAT_MODE] = Msg.getString("BuildingTableModel.column.heatMode"); //$NON-NLS-1$
		columnTypes[HEAT_MODE] = Double.class;
		columnNames[TEMPERATURE] = Msg.getString("BuildingTableModel.column.temperature"); //$NON-NLS-1$
		columnTypes[TEMPERATURE] = Double.class;
		columnNames[POWER_REQUIRED]  = Msg.getString("BuildingTableModel.column.powerRequired"); //$NON-NLS-1$
		columnTypes[POWER_REQUIRED] = Double.class;
	}

	public BuildingTableModel(Settlement settlement) throws Exception {
		super(Msg.getString("BuildingTableModel.nameBuildings", //$NON-NLS-1$
				settlement.getName()),
				"BuildingTableModel.countingBuilding", //$NON-NLS-1$
				columnNames, columnTypes);
		
		unitManagerListener = new LocalUnitManagerListener();
		unitManager.addUnitManagerListener(unitManagerListener);

		BuildingManager bm = settlement.getBuildingManager();
		for(Building b : bm.getBuildings()) {
			addUnit(b);
		}
	}

	/**
	 * Catch unit update event.
	 *
	 * @param event the unit event.
	 */
	public void unitUpdate(UnitEvent event) {
		Unit unit = (Unit) event.getSource();
		UnitEventType eventType = event.getType();
		Object target = event.getTarget();

		int unitIndex = -1;
		int columnNum = -1;
		
		if (eventType == UnitEventType.REMOVE_BUILDING_EVENT) {
			
			unitIndex = getUnitIndex(unit);
			fireTableRowsDeleted(unitIndex, unitIndex);
		}
		else if (eventType == UnitEventType.ADD_BUILDING_EVENT) {
			// Determine the new row to be added
			Building building = (Building)unit;
			addUnit(building);
			
			fireTableStructureChanged();
		}

		else { 
			try {
				for (UnitEventType type: powerEvents) {				
					if (type == eventType) {
						int tempColumnNum = -1;
					}
				}
			} catch (Exception e) {
				logger.severe("unitUpdate not working: " + e.getMessage());
			}
		}
			
		if (columnNum > -1) {
			SwingUtilities.invokeLater(new BuildingTableCellUpdater(unitIndex, columnNum));
		}
	}

	/**
	 * Return the value of a Cell
	 *
	 * @param rowIndex    Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object result = null;

		if (rowIndex < getUnitNumber()) {
			Building building = (Building) getUnit(rowIndex);

			switch (columnIndex) {

			case NAME: 
				result = building.getNickName();
				break;

			case TYPE: 
				result = building.getBuildingType();
				break;

			case POWER_MODE:
				result = building.getPowerMode().getName();
				break;
			
			case POWER_REQUIRED:
				result =  building.getFullPowerRequired();
				break;
				
			case HEAT_MODE:
				result = building.getHeatMode().getPercentage();
				break;
				
			case TEMPERATURE:
				result = Math.round(building.getCurrentTemperature() * 10.0)/10.0;
				break;
			}
		}

		return result;
	}

	/**
	 * Prepares the model for deletion.
	 */
	@Override
	public void destroy() {
		super.destroy();
	}
	
	/**
	 * UnitManagerListener inner class.
	 */
	private class LocalUnitManagerListener implements UnitManagerListener {

		/**
		 * Catch unit manager update event.
		 *
		 * @param event the unit event.
		 */
		public void unitManagerUpdate(UnitManagerEvent event) {
			Unit unit = event.getUnit();
			UnitManagerEventType eventType = event.getEventType();

			if (unit.getUnitType() == UnitType.BUILDING) {
				if (eventType == UnitManagerEventType.ADD_UNIT && !containsUnit(unit)) {
					addUnit(unit);
				} else if (eventType == UnitManagerEventType.REMOVE_UNIT && containsUnit(unit)) {
					removeUnit(unit);
				}
			}
		}
	}
	
	private class BuildingTableCellUpdater implements Runnable {
		private int row;
		private int column;

		private BuildingTableCellUpdater(int row, int column) {
			this.row = row;
			this.column = column;
		}

		public void run() {
			fireTableCellUpdated(row, column);
		}
	}
}
