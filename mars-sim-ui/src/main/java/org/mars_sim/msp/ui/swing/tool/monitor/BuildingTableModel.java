/*
 * Mars Simulation Project
 * BuildingTableModel.java
 * @date 2022-06-28
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;

/**
 * The BuildingTableModel maintains a list of Building objects. By defaults the source
 * of the list is the Unit Manager. 
 */
@SuppressWarnings("serial")
public class BuildingTableModel extends UnitTableModel<Building> {

	// Column indexes
	private static final int NAME = 0;
	private static final int TYPE = 1;
	private static final int CATEGORY = 2;
	private static final int POWER_MODE = 3;
	private static final int POWER_REQUIRED = 4;
	private static final int POWER_GEN = 5;
	private static final int HEAT_MODE = 6;
	private static final int TEMPERATURE = 7;
	
	private static final int COLUMNCOUNT = 8;

	/** Names of Columns. */
	private static ColumnSpec[] COLUMNS;

	/**
	 * The static initializer creates the name & type arrays.
	 */
	static {
		COLUMNS = new ColumnSpec[COLUMNCOUNT];
		COLUMNS[NAME] = new ColumnSpec(Msg.getString("BuildingTableModel.column.name"), String.class);
		COLUMNS[TYPE] = new ColumnSpec(Msg.getString("BuildingTableModel.column.type"), String.class);
		COLUMNS[CATEGORY] = new ColumnSpec(Msg.getString("BuildingTableModel.column.category"), String.class);	
		COLUMNS[POWER_MODE] = new ColumnSpec(Msg.getString("BuildingTableModel.column.powerMode"), String.class);		
		COLUMNS[HEAT_MODE] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heatMode"), Double.class);
		COLUMNS[TEMPERATURE] = new ColumnSpec(Msg.getString("BuildingTableModel.column.temperature"),Double.class);
		COLUMNS[POWER_REQUIRED]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.powerRequired"), Double.class);
		COLUMNS[POWER_GEN]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.powerGenerated"), Double.class);
	}


	private Settlement selectedSettlement;

	/**
	 * Constructor.
	 * 
	 * @param settlement
	 * @throws Exception
	 */
	public BuildingTableModel(Settlement settlement) {
		super(UnitType.BUILDING, Msg.getString("BuildingTableModel.nameBuildings", //$NON-NLS-1$
				settlement.getName()),
				"BuildingTableModel.countingBuilding", //$NON-NLS-1$
				COLUMNS);
		
		listenForUnits();

		setSettlementFilter(settlement);
	}

	@Override
	public boolean setSettlementFilter(Settlement filter) {
		if (selectedSettlement != null) {
			selectedSettlement.removeUnitListener(this);
		}

		selectedSettlement = filter;
 		BuildingManager bm = selectedSettlement.getBuildingManager();
		resetEntities(bm.getBuildingSet());

		selectedSettlement.addUnitListener(this);

		return true;
	}

	/**
	 * Returns the value of a Cell.
	 *
	 * @param rowIndex    Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	@Override
	protected Object getEntityValue(Building building, int columnIndex) {
		Object result = null;

		switch (columnIndex) {

		case NAME: 
			result = building.getName();
			break;

		case TYPE: 
			result = building.getBuildingType();
			break;
		
		case CATEGORY:
			result = building.getCategory().getName();
			break;

		case POWER_MODE:
			result = building.getPowerMode().getName();
			break;
						
		case POWER_REQUIRED:
			result =  building.getFullPowerRequired();
			break;
			
		case POWER_GEN:
			if (building.getPowerGeneration() != null)
				result =  building.getPowerGeneration().getGeneratedPower();
			break;
			
		case HEAT_MODE:
			result = building.getHeatMode().getPercentage();
			break;
			
		case TEMPERATURE:
			result = building.getCurrentTemperature();
			break;
			
		default:
			break;
		}

		return result;
	}
	
	@Override
	public void destroy() {
		if (selectedSettlement != null) {
			selectedSettlement.removeUnitListener(this);
		}
		super.destroy();
	}
	
	/**
	 * Catches unit update event.
	 *
	 * @param event the unit event.
	 */
	@Override
	public void unitUpdate(UnitEvent event) {
		Unit unit = (Unit) event.getSource();
		UnitEventType eventType = event.getType();
		
		if (eventType == UnitEventType.REMOVE_BUILDING_EVENT) {
			removeEntity((Building) unit);
		}
		else if (eventType == UnitEventType.ADD_BUILDING_EVENT) {
			// Determine the new row to be added
			Building building = (Building)unit;
			addEntity(building);			
		}

		else if (event.getSource() instanceof Building) { 
			int columnIndex = 51;
			switch(eventType) {
				case POWER_MODE_EVENT:
					columnIndex = POWER_MODE;
					break;
				case GENERATED_POWER_EVENT:
					columnIndex = POWER_GEN;
					break;
				case REQUIRED_POWER_EVENT:
					columnIndex = POWER_REQUIRED;
					break;
				case HEAT_MODE_EVENT:
					columnIndex = HEAT_MODE;
					break;
				default:
			}
			if (columnIndex > 0) {
				entityValueUpdated((Building) event.getSource(), columnIndex, columnIndex);
			}
		}
	}
}
