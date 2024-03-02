/*
 * Mars Simulation Project
 * BuildingTableModel.java
 * @date 2022-06-28
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.Set;

import com.mars_sim.core.UnitEvent;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.utils.ColumnSpec;

/**
 * The BuildingTableModel maintains a list of Building objects. By defaults the source
 * of the list is the Unit Manager. 
 */
public class BuildingTableModel extends UnitTableModel<Building> {

	// Column indexes
	private static final int NAME = 0;
	private static final int SETTLEMENT = NAME+1;
	private static final int TYPE = SETTLEMENT+1;
	private static final int CATEGORY = TYPE+1;
	private static final int POWER_MODE = CATEGORY+1;
	private static final int POWER_REQUIRED = POWER_MODE+1;
	private static final int POWER_GEN = POWER_REQUIRED+1;
	private static final int HEAT_MODE = POWER_GEN+1;
	private static final int TEMPERATURE = HEAT_MODE+1;
	
	private static final int COLUMNCOUNT = TEMPERATURE+1;

	/** Names of Columns. */
	private static final ColumnSpec[] COLUMNS;

	/**
	 * The static initializer creates the name & type arrays.
	 */
	static {
		COLUMNS = new ColumnSpec[COLUMNCOUNT];
		COLUMNS[NAME] = new ColumnSpec(Msg.getString("BuildingTableModel.column.name"), String.class);
		COLUMNS[SETTLEMENT] = new ColumnSpec("Settlement", String.class);
		COLUMNS[TYPE] = new ColumnSpec(Msg.getString("BuildingTableModel.column.type"), String.class);
		COLUMNS[CATEGORY] = new ColumnSpec(Msg.getString("BuildingTableModel.column.category"), String.class);	
		COLUMNS[POWER_MODE] = new ColumnSpec(Msg.getString("BuildingTableModel.column.powerMode"), String.class);		
		COLUMNS[HEAT_MODE] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heatMode"), Double.class);
		COLUMNS[TEMPERATURE] = new ColumnSpec(Msg.getString("BuildingTableModel.column.temperature"),Double.class);
		COLUMNS[POWER_REQUIRED]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.powerRequired"), Double.class);
		COLUMNS[POWER_GEN]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.powerGenerated"), Double.class);
	}

	/**
	 * Constructor.
	 * 
	 * @param settlement
	 * @throws Exception
	 */
	public BuildingTableModel() {
		super(UnitType.BUILDING, Msg.getString("BuildingTableModel.nameBuildings", ""),
				"BuildingTableModel.countingBuilding", //$NON-NLS-1$
				COLUMNS);	
		
		setSettlementColumn(SETTLEMENT);
	}

	@Override
	public boolean setSettlementFilter(Set<Settlement> filter) {
		getEntities().forEach(s -> s.removeUnitListener(this));

		var newBuildings = filter.stream().flatMap(s -> s.getBuildingManager().getBuildingSet().stream()).toList();
		resetEntities(newBuildings);

		newBuildings.forEach(s -> s.addUnitListener(this));

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
		case SETTLEMENT: 
			result = building.getSettlement().getName();
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
		getEntities().forEach(s -> s.removeUnitListener(this));
		super.destroy();
	}
	
	/**
	 * Catches unit update event.
	 *
	 * @param event the unit event.
	 */
	@Override
	public void unitUpdate(UnitEvent event) {
		if (event.getSource() instanceof Building building) {
			UnitEventType eventType = event.getType();

			int columnIndex = switch(eventType) {
				case POWER_MODE_EVENT -> POWER_MODE;
				case GENERATED_POWER_EVENT -> POWER_GEN;
				case REQUIRED_POWER_EVENT -> POWER_REQUIRED;
				case HEAT_MODE_EVENT -> HEAT_MODE;
				default -> -1;
			};

			if (columnIndex >= 0) {
				entityValueUpdated(building, columnIndex, columnIndex);
			}
		}
	}
}
