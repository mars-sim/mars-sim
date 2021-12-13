/**
 * Mars Simulation Project
 * RobotTableModel.java
 * @date 2021-12-07
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.person.ai.mission.MissionEventType;
import org.mars_sim.msp.core.person.ai.mission.MissionListener;
import org.mars_sim.msp.core.person.ai.task.utils.TaskManager;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * The BuildingTableModel maintains a list of Building objects. By defaults the source
 * of the list is the Unit Manager. 
 */
@SuppressWarnings("serial")
public class BuildingTableModel extends UnitTableModel {

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

	/**
	 * The static initializer creates the name & type arrays.
	 */
	static {
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
		//SwingUtilities.invokeLater(new RobotTableUpdater(event, this));
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
				result = building.getCurrentTemperature();
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
//
//		if (sourceType == ValidSourceType.ALL_ROBOTS) {
//			UnitManager unitManager = Simulation.instance().getUnitManager();
//			unitManager.removeUnitManagerListener(unitManagerListener);
//			unitManagerListener = null;
//		} else if (sourceType == ValidSourceType.VEHICLE_ROBOTS) {
//			((Unit) vehicle).removeUnitListener(crewListener);
//			crewListener = null;
//			vehicle = null;
//		} else if (sourceType == ValidSourceType.MISSION_ROBOTS) {
//			mission.removeMissionListener(missionListener);
//			missionListener = null;
//			mission = null;
//		} else {
//			settlement.removeUnitListener(settlementListener);
//			settlementListener = null;
//			settlement = null;
//		}
	}

}
