/*
 * Mars Simulation Project
 * BaseVehicleModel.java
 * @date 2026-05-23
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.malfunction.Malfunction;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.person.ai.mission.AbstractVehicleMission;
import com.mars_sim.core.person.ai.mission.NavPoint;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.unit.MobileUnit;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.components.ColumnSpec;

/**
 * A generic table model showing Vehicles. It provides a number of predefined available columns.
 * The subclass defines which columns are to be rendered.
 * The model automatically monitors the Vehicle for changes and updates the table as needed.
 */
public abstract class BaseVehicleModel extends AbstractEntityModel<Vehicle> {

    // Forwarded events from the VehicleMission to the Vehicle
    private static final Set<String> FORWARED_EVENTS = Set.of(VehicleMission.DISTANCE_EVENT, VehicleMission.NAVPOINTS_EVENT,
                                        VehicleMission.TRAVEL_STATUS_EVENT);

    private static final int NAME_VAL = 0;
    private static final int MISSION_VAL = 1;
    private static final int TYPE_VAL = 2;
    private static final int STATUS_VAL = 3;
    private static final int SETTLEMENT_VAL = 4;
    private static final int LOCATION_VAL = 5;  
    private static final int DESTINATION_VAL = 6;
    private static final int DESTDIST_VAL = 7;
    private static final int DRIVER_VAL = 9;
    private static final int BEACON_VAL = 10;
    private static final int RESERVED_VAL = 11;
    private static final int SPEED_VAL = 12;
    private static final int MALFUNCTION_VAL = 13;
    private static final int BATTERY_VAL = 14;
    private static final int FUEL_VAL = 15;

    // Basic fixed properties of a Vehicle
    protected static final EntityColumnSpec NAME = new EntityColumnSpec(new ColumnSpec(NAME_VAL, Msg.getString("entity.name"), String.class), null);
    protected static final EntityColumnSpec TYPE = new EntityColumnSpec(new ColumnSpec(TYPE_VAL, Msg.getString("vehicle.type"), String.class), null);
    protected static final EntityColumnSpec MISSION = new EntityColumnSpec(new ColumnSpec(MISSION_VAL, Msg.getString("mission.singular"),
                                String.class), Set.of(Vehicle.MISSION_EVENT));
    protected static final EntityColumnSpec STATUS = new EntityColumnSpec(new ColumnSpec(STATUS_VAL, Msg.getString("vehicle.status"), String.class),
                                Set.of(EntityEventType.STATUS_EVENT));    
    protected static final EntityColumnSpec SETTLEMENT = new EntityColumnSpec(new ColumnSpec(SETTLEMENT_VAL, Msg.getString("settlement.singular"), String.class), null);
    protected static final EntityColumnSpec LOCATION = new EntityColumnSpec(new ColumnSpec(LOCATION_VAL, "Location",
                            String.class), Set.of(EntityEventType.COORDINATE_EVENT, MobileUnit.CONTAINER_EVENT));
    protected static final EntityColumnSpec DESTINATION = new EntityColumnSpec(new ColumnSpec(DESTINATION_VAL, "Destination", String.class),
                                Set.of(VehicleMission.NAVPOINTS_EVENT, VehicleMission.TRAVEL_STATUS_EVENT));
    protected static final EntityColumnSpec DESTDIST = new EntityColumnSpec(new ColumnSpec(DESTDIST_VAL, "Dist. to next [km]", Double.class),
                                Set.of(VehicleMission.DISTANCE_EVENT, VehicleMission.TRAVEL_STATUS_EVENT));
    protected static final EntityColumnSpec DRIVER = new EntityColumnSpec(new ColumnSpec(DRIVER_VAL, Msg.getString("vehicle.operator"),
                            String.class), Set.of(EntityEventType.OPERATOR_EVENT));
    protected static final EntityColumnSpec BEACON = new EntityColumnSpec(new ColumnSpec(BEACON_VAL, "Beacon",
                            Boolean.class), Set.of(EntityEventType.EMERGENCY_BEACON_EVENT));
    protected static final EntityColumnSpec RESERVED = new EntityColumnSpec(new ColumnSpec(RESERVED_VAL, "Reserved",
                            Boolean.class), Set.of(EntityEventType.RESERVED_EVENT));
    protected static final EntityColumnSpec SPEED = new EntityColumnSpec(new ColumnSpec(SPEED_VAL, Msg.getString("vehicle.speed"),
                            Double.class, ColumnSpec.STYLE_INTEGER), Set.of(EntityEventType.SPEED_EVENT));
    protected static final EntityColumnSpec MALFUNCTION = new EntityColumnSpec(new ColumnSpec(MALFUNCTION_VAL, "Malfunction",
                            String.class), Set.of(MalfunctionManager.MALFUNCTION_EVENT));
    protected static final EntityColumnSpec BATTERY = new EntityColumnSpec(new ColumnSpec(BATTERY_VAL, "Battery", String.class), null);
    protected static final EntityColumnSpec FUEL = new EntityColumnSpec(new ColumnSpec(FUEL_VAL, "Fuel %", Double.class, ColumnSpec.STYLE_INTEGER), null);
    
    private Map<Vehicle, VehicleMission> vehicleToMission = new HashMap<>();
    private List<Integer> resources = new ArrayList<>();

    /**
     * Create a generic vehicle model with the specified columns.
     * @param columns Columns to show.
     */
    protected BaseVehicleModel(EntityColumnSpec... columns) {
        super(columns);
    }

    /**
     * Add resource columns to the model. The resource columns are created for the specified list of resource IDs.
     * @param resources Resource IDs to add
     */
    protected void addResourceColumns(List<Integer> resources) {
        addColumns(InventoryColumnHelper.getResourceColumn(resources));
        this.resources.addAll(resources);
    }

    /**
     * Attachs listener to an associated Mission.
     * @param entity Source of events
     * @param activate Activiate listeners
     */
    @Override
    protected void enableListener(Vehicle entity, boolean activate) {
        var vm = entity.getMission();
        if (vm != null) {
            if (activate) {
                vm.addEntityListener(this);
            }
            else {
                vm.removeEntityListener(this);
            }
        }

        // Always disable if Mission is done
        super.enableListener(entity, activate);
    }

    /**
     * Also if the Vehicle's Mission changes it triggers adding/removing listener on the Mission
     */
    @Override
    public void entityUpdate(EntityEvent event) {
        // Trap evetns from Mission
        if (event.getSource() instanceof VehicleMission vm) {
            if (FORWARED_EVENTS.contains(event.getType())) {
                // Make the model refresh vehicle distances
                event = new EntityEvent(vm.getVehicle(), event.getType(), event.getTarget());
            }
            else {
                // Do not pass other Vehicle event to model
                return;
            }
        }

        // Pick up the change of mission
        else if (event.getSource() instanceof Vehicle v && event.getType().equals(Vehicle.MISSION_EVENT)) {
            // Mission changed, update the associated vehicle
            if (v.getMission() instanceof VehicleMission vm) {
                vm.addEntityListener(this);
                vehicleToMission.put(v, vm);
            }
            else {
                // Missiongone
                var oldMission = vehicleToMission.remove(v);
                if (oldMission != null) {
                    oldMission.removeEntityListener(this);
                }
            }
        }
        else if (event.getType().equals(EntityEventType.INVENTORY_RESOURCE_EVENT)) {
            event = InventoryColumnHelper.convertResourceToEvent(event, resources);
            if (event == null) {
                // Not a monitored resource
                return;
            }
        }        

        super.entityUpdate(event);
    }

    /**
     * Get a cell value for the associated Vehicle. Column index maps to the associated ColumnSpec where the id
     * is used to determine the value to return.
     * @param entity The Vehicle entity.
     * @param valueIndex Column index. 
     * @return Associated value.
     */
    @Override
    protected Object getEntityValue(Vehicle entity, int valueIndex) {
        return switch(valueIndex) {
            case NAME_VAL -> entity.getName();
            case MISSION_VAL -> (entity.getMission() != null) ? entity.getMission().getName() : "";
            case TYPE_VAL -> entity.getVehicleType().getName();
            case STATUS_VAL -> entity.printStatusTypes();
            case SETTLEMENT_VAL -> entity.getAssociatedSettlement().getName();
			case SPEED_VAL  -> entity.getSpeed();
			case BEACON_VAL  -> entity.isBeaconOn();
			case RESERVED_VAL -> entity.isReserved();
            case DRIVER_VAL -> (entity.getOperator() != null ? entity.getOperator().getName() : null);
			case BATTERY_VAL -> entity.getController().getBattery().getBatteryStatus().getName();
			case FUEL_VAL -> entity.getFuelPercent();

            case LOCATION_VAL -> {
				if (entity.getMission() instanceof AbstractVehicleMission vm) {
					NavPoint nav = vm.getCurrentNavpoint();
					yield (nav != null ? nav.getDescription() : entity.getCoordinates().getFormattedString());
				}
				else {
					Settlement settle = entity.getSettlement();
					if (settle != null) {
						yield settle.getName();
					}
					else {
						yield entity.getCoordinates().getFormattedString();
					}
				}
            }
    
			case DESTINATION_VAL -> {
				if (entity.getMission() instanceof AbstractVehicleMission vm) {
					yield vm.getNextNavpointDescription();
				}
                else {
                    yield null;
                }
			}

			case DESTDIST_VAL -> {
				if (entity.getMission() instanceof VehicleMission vm) {
					yield vm.getDistanceCurrentLegRemaining();
				}
                else {
                    yield null;
                }
			}

			case MALFUNCTION_VAL -> {
				Malfunction failure = entity.getMalfunctionManager().getMostSeriousMalfunction();
				if (failure != null) yield failure.getName();
                else yield null;
			}

            // Check for a resource column
            default -> InventoryColumnHelper.getValue(entity, valueIndex);
        };
    }

    @Override
    protected String getEntityDescription(Vehicle entity, int valueIndex) {
        if (valueIndex == MISSION_VAL && entity.getMission() != null) {
            return "Phase: " +entity.getMission().getPhaseDescription();
        }
        else if (valueIndex == BATTERY_VAL) {
            return StyleManager.DECIMAL_PERC.format(entity.getController().getBattery().getBatteryPercent());
        }
        else if (valueIndex == FUEL_VAL) {
            return entity.getFuelTypeStr();
        }
        return null;
    }
}