/*
 * Mars Simulation Project
 * MissionTableModel.java
 * @date 2025-10-16
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.person.ai.mission.ConstructionMission;
import com.mars_sim.core.person.ai.mission.Mission;
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
class MissionTableModel extends EntityMonitorModel<Mission>
		implements MissionManagerListener {

	// Column indexes
	private static final int DATE_FILED = 0;
	private static final int DATE_EMBARKED = DATE_FILED + 1;
	private static final int DATE_COMPLETED = DATE_EMBARKED + 1;
	private static final int STARTING_SETTLEMENT = DATE_COMPLETED + 1;
	private static final int STARTING_MEMBER = STARTING_SETTLEMENT + 1;
	private static final int MISSION_STRING = STARTING_MEMBER + 1;
	private static final int DESIGNATION = MISSION_STRING + 1;
	private static final int PHASE = DESIGNATION + 1;
	private static final int VEHICLE = PHASE + 1;
	private static final int MEMBER_NUM = VEHICLE + 1;
	private static final int NAVPOINT_NUM = MEMBER_NUM + 1;
	private static final int TRAVELLED_DISTANCE_TO_NEXT_NAVPOINT = NAVPOINT_NUM + 1;
	private static final int REMAINING_DISTANCE_TO_NEXT_NAVPOINT = TRAVELLED_DISTANCE_TO_NEXT_NAVPOINT + 1;
	private static final int TOTAL_REMAINING_DISTANCE_KM = REMAINING_DISTANCE_TO_NEXT_NAVPOINT + 1;
	private static final int TOTAL_ACTUAL_TRAVELLED_DISTANCE_KM = TOTAL_REMAINING_DISTANCE_KM + 1;
	private static final int TOTAL_ESTIMATED_DISTANCE_KM = TOTAL_ACTUAL_TRAVELLED_DISTANCE_KM + 1;	
	private static final int COLUMNCOUNT = TOTAL_ESTIMATED_DISTANCE_KM + 1;

	/** Names of Columns. */
	private static final ColumnSpec[] COLUMNS;
		
	private MissionManager missionManager;

	static {
		COLUMNS = new ColumnSpec[COLUMNCOUNT];
		COLUMNS[DATE_FILED] = new ColumnSpec(Msg.getString("MissionTableModel.column.filed"), MarsTime.class);
		COLUMNS[DATE_EMBARKED] = new ColumnSpec(Msg.getString("MissionTableModel.column.embarked"), MarsTime.class);
		COLUMNS[DATE_COMPLETED] = new ColumnSpec(Msg.getString("MissionTableModel.column.completed"), MarsTime.class);
		COLUMNS[STARTING_SETTLEMENT] = new ColumnSpec(Msg.getString("settlement.singular"), String.class);
		COLUMNS[STARTING_MEMBER] = new ColumnSpec(Msg.getString("mission.leader"), String.class);
		COLUMNS[MISSION_STRING] = new ColumnSpec(Msg.getString("entity.name"), String.class);
		COLUMNS[DESIGNATION] = new ColumnSpec(Msg.getString("mission.designation"), String.class);
		COLUMNS[PHASE] = new ColumnSpec(Msg.getString("mission.phase"), String.class);
		COLUMNS[VEHICLE] = new ColumnSpec(Msg.getString("vehicle.singular"), String.class);
		COLUMNS[MEMBER_NUM] = new ColumnSpec(Msg.getString("mission.members"), Integer.class);
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
		super(Msg.getString("mission.plural"), COLUMNS);

		missionManager = sim.getMissionManager();
				
		missionManager.addListener(this);

		// Mark this column up so as to hide it to save space in case of a single settlement view
		setSettlementColumn(STARTING_SETTLEMENT);
	}
	

	/**
	 * Sets the settlement filter.
	 */
	@Override
	protected boolean applySettlementFilter(Set<Settlement> filter) {
		
		Collection<Mission> missions = missionManager.getMissions().stream()
				.filter(m -> filter.contains(m.getAssociatedSettlement()))
				.toList();
	
		resetItems(missions);
		
		return true;
	}

	/**
	 * New mission has been added to the Mission Manager
	 *
	 * @param mission the new mission.
	 */
	@Override
	public void addMission(Mission mission) {
		var s = mission.getAssociatedSettlement();
		if (getSelectedSettlements().contains(s)) {
			addItem(mission);
		}
	}

	/**
	 * Removes an old mission.
	 *
	 * @param mission the old mission.
	 */
	@Override
	public void removeMission(Mission mission) {
		removeItem(mission);
	}

	/**
	 * Catches mission update event.
	 *
	 * @param event the entity event.
	 */
	@Override
	public void entityUpdate(EntityEvent event) {

		if (event.getSource() instanceof Mission mission) {

			// Get main column
			String eventType = event.getType();
			int column0 = switch (eventType) {
				case VehicleMission.VEHICLE_EVENT -> VEHICLE;
				case VehicleMission.NAVPOINTS_EVENT -> NAVPOINT_NUM;
				case Mission.STARTING_SETTLEMENT_EVENT -> STARTING_SETTLEMENT;
				case EntityEventType.NAME_EVENT -> MISSION_STRING;
				case Mission.DESIGNATION_EVENT ->DESIGNATION;
				case Mission.ADD_MEMBER_EVENT, Mission.REMOVE_MEMBER_EVENT -> MEMBER_NUM;
				case Mission.PHASE_EVENT, Mission.PHASE_DESCRIPTION_EVENT -> PHASE;
				default -> -1;
			};

			if (column0 > -1)
				entityValueUpdated(mission, column0, column0);

			// Special event that trigger multiple updates
			if (eventType.equals(VehicleMission.DISTANCE_EVENT)) {
				entityValueUpdated(mission,
						TRAVELLED_DISTANCE_TO_NEXT_NAVPOINT,
						TOTAL_ESTIMATED_DISTANCE_KM);
			}
			else if (eventType.equals(Mission.PHASE_EVENT)) {
				entityValueUpdated(mission, DATE_FILED, DATE_COMPLETED);
			}
		}
	}

	/**
	 * Returns the value of a Cell.
	 *
	 * @param rowIndex    Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	@Override
	public Object getItemValue(Mission mission, int columnIndex) {

		Object result = null;

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
		missionManager.removeListener(this);
		super.destroy();
	}
}
