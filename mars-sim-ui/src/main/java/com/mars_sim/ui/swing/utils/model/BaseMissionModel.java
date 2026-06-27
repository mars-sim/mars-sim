/*
 * Mars Simulation Project
 * BaseMissionModel.java
 * @date 2026-05-23
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.person.ai.mission.ConstructionMission;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionPlanning;
import com.mars_sim.core.person.ai.mission.PlanType;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.components.ColumnSpec;

/**
 * A generic table model showing Missions. It provides a number of predefined available columns.
 * The subclass defines which columns are to be rendered.
 * The model automatically monitors the Mission for changes and updates the table as needed.
 */
public abstract class BaseMissionModel extends AbstractEntityModel<Mission> {

    private static final int NAME_VAL = 0;
    private static final int PHASE_VAL = 1;
    private static final int FILED_VAL = 2;
    private static final int EMBARKED_VAL = 3;
    private static final int COMPLETED_VAL = 4;
    private static final int SETTLEMENT_VAL = 5;
    private static final int LEADER_VAL = 6;
    private static final int DESIGNATION_VAL = 7;
    private static final int VEHICLE_VAL = 8;
    private static final int MEMBER_NUM_VAL = 9;
    private static final int REMAINING_TO_NAVPOINT_VAL = 10;
    private static final int REMAINING_TO_END_VAL = 11;
    private static final int ACTUAL_TRAVELLED_VAL = 12;

    // Show Mission name, passive and unchanging
    protected static final EntityColumnSpec NAME = new EntityColumnSpec(new ColumnSpec(NAME_VAL, Msg.getString("entity.name"), String.class),
                                                            null);
    protected static final EntityColumnSpec PHASE = new EntityColumnSpec(new ColumnSpec(PHASE_VAL, Msg.getString("mission.phase"), String.class),
                                                            Set.of(Mission.PHASE_EVENT, Mission.PHASE_DESCRIPTION_EVENT));
    protected static final EntityColumnSpec DATE_FILED = new EntityColumnSpec(new ColumnSpec(FILED_VAL, Msg.getString("mission.filed"), MarsTime.class),
                                                            Set.of(Mission.PHASE_EVENT));
    protected static final EntityColumnSpec DATE_EMBARKED = new EntityColumnSpec(new ColumnSpec(EMBARKED_VAL, Msg.getString("mission.embarked"), MarsTime.class),
                                                            Set.of(Mission.PHASE_EVENT));
    protected static final EntityColumnSpec DATE_COMPLETED = new EntityColumnSpec(new ColumnSpec(COMPLETED_VAL, Msg.getString("mission.completed"), MarsTime.class),
                                                            Set.of(Mission.PHASE_EVENT));
    protected static final EntityColumnSpec SETTLEMENT = new EntityColumnSpec(new ColumnSpec(SETTLEMENT_VAL, Msg.getString("settlement.singular"), String.class),
                                                            null);
	protected static final EntityColumnSpec LEADER = new EntityColumnSpec(new ColumnSpec(LEADER_VAL, Msg.getString("mission.leader"), String.class),
                                                            null);
    protected static final EntityColumnSpec DESIGNATION = new EntityColumnSpec(new ColumnSpec(DESIGNATION_VAL, Msg.getString("mission.designation"), String.class),
                                                            null);
    protected static final EntityColumnSpec VEHICLE = new EntityColumnSpec(new ColumnSpec(VEHICLE_VAL, Msg.getString("vehicle.singular"), String.class),
                                                            Set.of(VehicleMission.VEHICLE_EVENT));
    protected static final EntityColumnSpec MEMBER_NUM = new EntityColumnSpec(new ColumnSpec(MEMBER_NUM_VAL, Msg.getString("mission.members"), Integer.class),
                                                            Set.of(Mission.ADD_MEMBER_EVENT, Mission.REMOVE_MEMBER_EVENT));

    // The triggering event is converted from a Vehicle.COORDINATE_EVENT
    protected static final EntityColumnSpec REMAINING_TO_NAVPOINT = new EntityColumnSpec(new ColumnSpec(REMAINING_TO_NAVPOINT_VAL, Msg.getString("mission.leg.remaining"),
                                                        Double.class, ColumnSpec.STYLE_INTEGER), Set.of(VehicleMission.DISTANCE_EVENT));  
    protected static final EntityColumnSpec REMAINING_TO_END = new EntityColumnSpec(new ColumnSpec(REMAINING_TO_END_VAL, Msg.getString("mission.total.remaining"),
                                                        Double.class, ColumnSpec.STYLE_INTEGER), Set.of(VehicleMission.DISTANCE_EVENT));
    protected static final EntityColumnSpec ACTUAL_TRAVELLED = new EntityColumnSpec(new ColumnSpec(ACTUAL_TRAVELLED_VAL, Msg.getString("mission.total.travelled"),
                                                        Double.class, ColumnSpec.STYLE_INTEGER), Set.of(VehicleMission.DISTANCE_EVENT));
    
    // Used to track the associated vehicle for a VehicleMission so that we can listen for distance events 
    private Map<VehicleMission,Vehicle> missionToVehicle = new HashMap<>();

    /**
     * Create a generic mission model with the specified columns.
     * @param columns Columns to show.
     */
    protected BaseMissionModel(EntityColumnSpec... columns) {
        super(columns);
    }

    /**
     * Attachs listener to an associated Vehicle.
     * @param entity Source of events
     * @param activate Activiate listeners
     */
    @Override
    protected void enableListener(Mission entity, boolean activate) {
        if (entity instanceof VehicleMission vm) {
            var vehicle = vm.getVehicle();
            if (vehicle != null) {
                if (activate && !vm.isDone()) {
                    vehicle.addEntityListener(this);
                }
                else {
                    vehicle.removeEntityListener(this);
                }
            }
        }

        // Always disabke if Mission is done
        super.enableListener(entity, activate && !entity.isDone());
    }

    /**
     * This catches Vehicle COORDINATE_EVENT and converts into a DISTANCE_EVENT makes them appears as they have comes from the associated VehicleMission.
     * Also if the Mission Vehicle changes it triggers adding/remving listener on the Vehicle
     */
    @Override
    public void entityUpdate(EntityEvent event) {
        if (event.getSource() instanceof Vehicle v) {
            if (event.getType().equals(EntityEventType.COORDINATE_EVENT)) {
                // Vehicle distance changed, update the associated mission
                var mission = v.getMission();
                if (mission != null) {
                    // Make the model refresh vehicle distances
                    event = new EntityEvent(mission, VehicleMission.DISTANCE_EVENT, event.getTarget());
                }
            }
            else {
                // Do not pass other Vehicle event to model
                return;
            }
        }
        else if ((event.getSource() instanceof VehicleMission vm) && event.getType().equals(VehicleMission.VEHICLE_EVENT)) {
            // Mission changed, update the associated vehicle
            var vehicle = vm.getVehicle();
            if (vehicle != null) {
                vehicle.addEntityListener(this);
                missionToVehicle.put(vm, vehicle);
            }
            else {
                // Vehicle gone
                var oldVehicle = missionToVehicle.remove(vm);
                if (oldVehicle != null) {
                    oldVehicle.removeEntityListener(this);
                }
            }
        }

        super.entityUpdate(event);
    }
    /**
     * Get a cell value for the associated Mission. Column index maps to the associated ColumnSpec where the id
     * is used to determine the value to return.
     * @param entity The Mission entity.
     * @param valueIndex Column index. 
     * @return Associated value.
     */
    @Override
    protected Object getEntityValue(Mission entity, int valueIndex) {
        // Vehicle missions have additional distance values
        if (entity instanceof VehicleMission vm) {
            switch(valueIndex) {
                case REMAINING_TO_NAVPOINT_VAL:
                    return vm.getDistanceCurrentLegRemaining();
                case REMAINING_TO_END_VAL:
                    return vm.getTotalDistanceRemaining();
                case ACTUAL_TRAVELLED_VAL:
                    return vm.getTotalDistanceTravelled();
                default:
                    break;
            }
        }

        // Standard values for all missions
        return switch(valueIndex) {
            case NAME_VAL-> entity.getName();
            case PHASE_VAL -> {
                MissionPlanning plan = entity.getPlan();
                if ((plan != null) && plan.getStatus() == PlanType.PENDING) {
                    int percent = plan.getPercentComplete();
                    int score = (int)plan.getScore();
                    int min = (int)plan.getPassingScore();
                    yield percent + "% Reviewed - Score: " + score + " [Min: " + min + "]";
                } else {
                    yield entity.getPhaseDescription();
                }
            }
            case FILED_VAL -> entity.getLog().getTimestampFiled();
            case EMBARKED_VAL -> entity.getLog().getTimestampEmbarked();
            case COMPLETED_VAL -> entity.getLog().getTimestampCompleted();
            case SETTLEMENT_VAL -> entity.getAssociatedSettlement().getName();
            case LEADER_VAL -> entity.getStartingPerson().getName();
            case DESIGNATION_VAL -> entity.getFullMissionDesignation();
            case MEMBER_NUM_VAL -> entity.getSignup().size();
			case VEHICLE_VAL -> {
				Vehicle reserved = null;
				if (entity instanceof VehicleMission vm) {
					reserved = vm.getVehicle();
				} else if (entity instanceof ConstructionMission constructionMission) {
					var constVehicles = constructionMission.getConstructionVehicles();
					if (!constVehicles.isEmpty()) {
						reserved = constVehicles.get(0);
					}
				}
				if (reserved != null) {
					yield reserved.getName();
				}
                else {
                    yield null;
                }
            }
            default -> null;
        };
    }
}