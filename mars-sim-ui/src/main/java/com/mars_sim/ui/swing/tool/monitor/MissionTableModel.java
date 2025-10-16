/*
 * Mars Simulation Project
 * MissionTableModel.java
 * @date 2025-10-16
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

import com.mars_sim.core.GameManager;
import com.mars_sim.core.GameManager.GameMode;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.person.ai.mission.ConstructionMission;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionEvent;
import com.mars_sim.core.person.ai.mission.MissionEventType;
import com.mars_sim.core.person.ai.mission.MissionListener;
import com.mars_sim.core.person.ai.mission.MissionManager;
import com.mars_sim.core.person.ai.mission.MissionManagerListener;
import com.mars_sim.core.person.ai.mission.MissionPlanning;
import com.mars_sim.core.person.ai.mission.PlanType;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.GroundVehicle;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.utils.ColumnSpec;

/**
 * This class model how mission data is organized and displayed
 * within the Monitor Window for all settlements.
 */
@SuppressWarnings("serial")
public class MissionTableModel extends EntityTableModel<Mission>
		implements MissionManagerListener, MissionListener {

	// Column indexes
	/** Date filed column. */
	private static final int DATE_FILED = 0;
	/** Date Embarked column. */
	private static final int DATE_EMBARKED = DATE_FILED + 1;
	/** Date Returned column. */
	private static final int DATE_COMPLETED = DATE_EMBARKED + 1;
	/** Starting settlement column. */
	private static final int STARTING_SETTLEMENT = DATE_COMPLETED + 1;
	/** Starting member column. */
	private static final int STARTING_MEMBER = STARTING_SETTLEMENT + 1;
	/** Mission String column. */
	private static final int MISSION_STRING = STARTING_MEMBER + 1;
	/** Description column. */
	private static final int DESIGNATION = MISSION_STRING + 1;
	/** Phase column. */
	private static final int PHASE = DESIGNATION + 1;
	/** Mission vehicle column. */
	private static final int VEHICLE = PHASE + 1;
	/** Member number column. */
	private static final int MEMBER_NUM = VEHICLE + 1;
	/** Navpoint number column. */
	private static final int NAVPOINT_NUM = MEMBER_NUM + 1;
	/** Travelled distance to next navpoint column. */
	private static final int TRAVELLED_DISTANCE_TO_NEXT_NAVPOINT = NAVPOINT_NUM + 1;
	/** Remaining distance to next navpoint column. */
	private static final int REMAINING_DISTANCE_TO_NEXT_NAVPOINT = TRAVELLED_DISTANCE_TO_NEXT_NAVPOINT + 1;
	/** Remaining distance column. */
	private static final int TOTAL_REMAINING_DISTANCE_KM = REMAINING_DISTANCE_TO_NEXT_NAVPOINT + 1;
	/** Travelled distance column. */
	private static final int TOTAL_ACTUAL_TRAVELLED_DISTANCE_KM = TOTAL_REMAINING_DISTANCE_KM + 1;
	/** Proposed route distance column. */
	private static final int TOTAL_ESTIMATED_DISTANCE_KM = TOTAL_ACTUAL_TRAVELLED_DISTANCE_KM + 1;
	/** The number of Columns. */
	
	private static final int COLUMNCOUNT = TOTAL_ESTIMATED_DISTANCE_KM + 1;
	/** Names of Columns. */
	private static final ColumnSpec[] COLUMNS;
	
	private boolean monitorMissions = false;

	private GameMode mode = GameManager.getGameMode();
	
	private List<Mission> missionCache;
	
	private Settlement commanderSettlement;

	private MissionManager missionManager;
	
	protected static UnitManager unitManager = Simulation.instance().getUnitManager();

	static {
		COLUMNS = new ColumnSpec[COLUMNCOUNT];
		COLUMNS[DATE_FILED] = new ColumnSpec(Msg.getString("MissionTableModel.column.filed"), MarsTime.class);
		COLUMNS[DATE_EMBARKED] = new ColumnSpec(Msg.getString("MissionTableModel.column.embarked"), MarsTime.class);
		COLUMNS[DATE_COMPLETED] = new ColumnSpec(Msg.getString("MissionTableModel.column.completed"), MarsTime.class);
		COLUMNS[STARTING_SETTLEMENT] = new ColumnSpec(Msg.getString("MissionTableModel.column.startingSettlement"), String.class);
		COLUMNS[STARTING_MEMBER] = new ColumnSpec(Msg.getString("MissionTableModel.column.name"), String.class);
		COLUMNS[MISSION_STRING] = new ColumnSpec(Msg.getString("MissionTableModel.column.missionString"), String.class);
		COLUMNS[DESIGNATION] = new ColumnSpec(Msg.getString("MissionTableModel.column.designation"), String.class);
		COLUMNS[PHASE] = new ColumnSpec(Msg.getString("MissionTableModel.column.phase"), String.class);
		COLUMNS[VEHICLE] = new ColumnSpec(Msg.getString("MissionTableModel.column.vehicle"), String.class);
		COLUMNS[MEMBER_NUM] = new ColumnSpec(Msg.getString("MissionTableModel.column.members"), Integer.class);
		COLUMNS[NAVPOINT_NUM] = new ColumnSpec(Msg.getString("MissionTableModel.column.navpoints"), Integer.class);
		COLUMNS[TRAVELLED_DISTANCE_TO_NEXT_NAVPOINT] = new ColumnSpec(Msg.getString("MissionTableModel.column.leg.travelled"), Double.class);
		COLUMNS[REMAINING_DISTANCE_TO_NEXT_NAVPOINT] = new ColumnSpec(Msg.getString("MissionTableModel.column.leg.remaining"), Double.class);		
		COLUMNS[TOTAL_REMAINING_DISTANCE_KM] = new ColumnSpec(Msg.getString("MissionTableModel.column.total.remaining"), Double.class);
		COLUMNS[TOTAL_ACTUAL_TRAVELLED_DISTANCE_KM] = new ColumnSpec(Msg.getString("MissionTableModel.column.total.travelled"), Double.class);	
		COLUMNS[TOTAL_ESTIMATED_DISTANCE_KM] = new ColumnSpec(Msg.getString("MissionTableModel.column.total.proposed"), Double.class);
	}
	
	/**
	 * Constructor 1.
	 */
	public MissionTableModel(Simulation sim) {
		super(Msg.getString("MissionTableModel.tabName"), "MissionTableModel.numberOfMissions", COLUMNS);

		missionManager = sim.getMissionManager();
		if (mode == GameMode.COMMAND) {
			commanderSettlement = sim.getUnitManager().getCommanderSettlement();
			// Must take a copy
			missionCache = new ArrayList<>(missionManager.getMissionsForSettlement(commanderSettlement));
		}
		else {
			// Must take my own copy
			missionCache = new ArrayList<>(missionManager.getMissions());
		}

		missionManager.addListener(this);

		// Mark this column up so as to hide it to save space in case of a single settlement view
		setSettlementColumn(STARTING_SETTLEMENT);
	}
	
	/**
	 * Sets whether the changes to the Missions should be monitor for change. Set up the 
	 * Mission listeners for the Mission in the table.
	 * 
	 * @param activate 
	 */
    public void setMonitorEntites(boolean activate) {
		if (activate != monitorMissions) {
			if (activate) {
				for (Mission m : missionCache) {
					if (!m.isDone()) {
						m.addMissionListener(this);
					}
				}
			}
			else {
				for (Mission m : missionCache) {
					m.removeMissionListener(this);
				}
			}
			monitorMissions = activate;
		}
	}
    
	/**
	 * Sets the settlement filter.
	 */
	@Override
	public boolean setSettlementFilter(Set<Settlement> filter) {

		Collection<Mission> missions = missionCache.stream()
				.filter(m -> filter.contains(m.getAssociatedSettlement()))
				.toList();
	
		resetEntities(missions);
		
		return true;
	}

	/**
	 * Adds a new mission.
	 *
	 * @param mission the new mission.
	 */
	@Override
	public void addMission(Mission mission) {
		if (missionCache.contains(mission))
			return;

		boolean goodToGo = true;
		if (mode == GameMode.COMMAND) {
			goodToGo = mission.getStartingPerson().getAssociatedSettlement()
					.equals(commanderSettlement);
		}

		if (goodToGo) {
			synchronized(missionCache) {
				mission.addMissionListener(this);
				
				// Structural changes via Swing thread
				SwingUtilities.invokeLater(() -> {
					missionCache.add(mission);

					// Inform listeners of new row
					int index = missionCache.size() - 1;
					fireTableRowsInserted(index, index);
				});
			}
		}
	}

	/**
	 * Removes an old mission.
	 *
	 * @param mission the old mission.
	 */
	@Override
	public void removeMission(Mission mission) {
		if (missionCache.contains(mission)) {
			mission.removeMissionListener(this);

			// Structural changes via Swing thread
			SwingUtilities.invokeLater(() -> {
				int index = missionCache.indexOf(mission);
				missionCache.remove(mission);

				// Delete a particular row
				fireTableRowsDeleted(index, index);
			});
		}
	}

	/**
	 * Returns the object at the specified row indexes.
	 *
	 * @param row Index of the row object.
	 * @return Object at the specified row.
	 */
	@Override
	public Object getObject(int row) {
		return missionCache.get(row);
	}

	/**
	 * Catches mission update event.
	 *
	 * @param event the mission event.
	 */
	@Override
	public void missionUpdate(MissionEvent event) {

		int index = missionCache.indexOf(event.getSource());

		if (index >= 0) {
			List<Integer> columnsToUpdate = new ArrayList<>();
			MissionEventType eventType = event.getType();
			int column0 = switch (eventType) {
				case VEHICLE_EVENT -> VEHICLE;
				case STARTING_SETTLEMENT_EVENT -> STARTING_SETTLEMENT;
				case MISSION_STRING_EVENT -> MISSION_STRING;
				case DESIGNATION_EVENT ->DESIGNATION;
				case ADD_MEMBER_EVENT, REMOVE_MEMBER_EVENT -> MEMBER_NUM;
				case DATE_EVENT -> DATE_FILED;
				case NAME_EVENT ->STARTING_MEMBER;
				default -> -1;
			};

			if (column0 > -1)
				columnsToUpdate.add(column0);

			if (event.getSource() instanceof VehicleMission) {	
				switch(eventType) {
					case DISTANCE_EVENT: {
						columnsToUpdate.add(TRAVELLED_DISTANCE_TO_NEXT_NAVPOINT);
						columnsToUpdate.add(REMAINING_DISTANCE_TO_NEXT_NAVPOINT);
						columnsToUpdate.add(TOTAL_REMAINING_DISTANCE_KM);
						columnsToUpdate.add(TOTAL_ACTUAL_TRAVELLED_DISTANCE_KM);
						columnsToUpdate.add(TOTAL_ESTIMATED_DISTANCE_KM);
					} break;

					case NAVPOINTS_EVENT:
						columnsToUpdate.add(NAVPOINT_NUM);
						break;

					case PHASE_EVENT, PHASE_DESCRIPTION_EVENT:
						columnsToUpdate.add(PHASE);
						break;
					default:
						break;
				}
			}

			if (!columnsToUpdate.isEmpty())
				SwingUtilities.invokeLater(new MissionTableCellUpdater(index, columnsToUpdate));
		}
	}

	@Override
	public int getRowCount() {
		return missionCache.size();
	}

	/**
	 * Returns the value of a Cell.
	 *
	 * @param rowIndex    Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	@Override
	public Object getEntityValue(Mission mission, int columnIndex) {
//		if (rowIndex >= missionCache.size()) {
//			return null;
//		}

		Object result = null;
//		Mission mission = missionCache.get(rowIndex);

		switch (columnIndex) {
			case DATE_FILED:
				result = mission.getLog().getTimestampFiled();
				break;

			case DATE_EMBARKED:
				result = mission.getLog().getTimestampEmbarked();
				break;

			case DATE_COMPLETED:
				result = mission.getLog().getTimestampCompleted();
				break;

			case STARTING_SETTLEMENT: 
				Settlement s = mission.getAssociatedSettlement();
				result = (s != null? s.getName() : null);
				break;
				
			case STARTING_MEMBER:
				result = mission.getStartingPerson().getName();
				break;

			case MISSION_STRING:
				result = mission.getName();
				break;

			case DESIGNATION:
				result = mission.getFullMissionDesignation();
				break;

			case PHASE:
				MissionPlanning plan = mission.getPlan();
				if ((plan != null) && plan.getStatus() == PlanType.PENDING) {
					int percent = (int) plan.getPercentComplete();
					int score = (int)plan.getScore();
					int min = (int)mission.getAssociatedSettlement().getMinimumPassingScore();
					result = percent + "% Reviewed - Score: " + score + " [Min: " + min + "]";
				}
				else
					result = mission.getPhaseDescription();
				break;

			case VEHICLE:
				Vehicle reserved = null;
				if (mission instanceof VehicleMission vm) {
					reserved = vm.getVehicle();
				} else if (mission instanceof ConstructionMission constructionMission) {
					List<GroundVehicle> constVehicles = constructionMission.getConstructionVehicles();
					if (!constVehicles.isEmpty()) {
						reserved = constVehicles.get(0);
					}
				}
				if (reserved != null) {
					result = reserved.getName();
				}
				break;

			case MEMBER_NUM:
				result = mission.getSignup().size();
				break;

			case NAVPOINT_NUM:
				if (mission instanceof VehicleMission vm) {
					result = vm.getNavpoints().size();
				}
				break;

			case TRAVELLED_DISTANCE_TO_NEXT_NAVPOINT:
				if (mission instanceof VehicleMission vm) {
					result = vm.getDistanceCurrentLegTravelled();
				}
				break;
				
			case REMAINING_DISTANCE_TO_NEXT_NAVPOINT:
				if (mission instanceof VehicleMission vm) {
					result = vm.getDistanceCurrentLegRemaining();
				}
				break;
				
			case TOTAL_REMAINING_DISTANCE_KM:
				if (mission instanceof VehicleMission vm) {
					result = vm.getTotalDistanceRemaining();
				}
				break;
				
			case TOTAL_ACTUAL_TRAVELLED_DISTANCE_KM:
				if (mission instanceof VehicleMission vm) {
					result = vm.getTotalDistanceTravelled();
				}
				break;
				
			case TOTAL_ESTIMATED_DISTANCE_KM:
				if (mission instanceof VehicleMission vm) {
					result = vm.getTotalDistanceProposed();
				}
				break;
			
			default:
		}

		return result;
	}

	/**
	 * Prepares the model for deletion.
	 */
	@Override
	public void destroy() {
		Object[] missions = missionCache.toArray();
		for (int x = 0; x < missions.length; x++) {
			removeMission((Mission) missions[x]);
		}

		missionManager.removeListener(this);
		super.destroy();
	}

	/**
	 * Inner class for updating mission table cell.
	 */
	private class MissionTableCellUpdater implements Runnable {

		private int row;
		private List<Integer> columns;

		private MissionTableCellUpdater(int row, List<Integer>columns) {
			this.row = row;
			this.columns = columns;
		}

		public void run() {
			if (row < getRowCount()) {
				for(int column : columns) {
					if (column < getColumnCount())
						fireTableCellUpdated(row, column);
				}
			}
		}
	}
}
