/*
 * Mars Simulation Project
 * VehicleTableModel.java
 * @date 2025-09-25
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.CollectionUtils;
import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.malfunction.Malfunction;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.person.ai.mission.AbstractVehicleMission;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionManager;
import com.mars_sim.core.person.ai.mission.MissionManagerListener;
import com.mars_sim.core.person.ai.mission.NavPoint;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.utils.ColumnSpec;

/**
 * The VehicleTableModel that maintains a list of Vehicle objects.
 * It maps key attributes of the Vehicle into Columns.
 */
@SuppressWarnings("serial")
public class VehicleTableModel extends EntityMonitorModel<Vehicle> {

	private static final String ON = "On";
	private static final String OFF = "Off";
	private static final String TRUE = "True";
	private static final String FALSE = "False";
	private static final String PERCENT = " %";
	
	// Column indexes
	private static final int NAME = 0;
	private static final int TYPE = NAME+1;
	private static final int MODEL = TYPE+1;
	private static final int SETTLEMENT = MODEL+1;
	private static final int LOCATION = SETTLEMENT+1;
	private static final int DESTINATION = LOCATION+1;
	private static final int DESTDIST = DESTINATION+1;
	private static final int MISSION = DESTDIST+1;
	private static final int CREW = MISSION+1;
	private static final int DRIVER = CREW+1;
	private static final int STATUS = DRIVER+1;
	private static final int BEACON = STATUS+1;
	private static final int RESERVED = BEACON+1;
	private static final int SPEED = RESERVED+1;
	private static final int MALFUNCTION = SPEED+1;
	private static final int BATTERY = MALFUNCTION+1;
	private static final int FUEL = BATTERY+1;
	private static final int METHANE = FUEL+1;
	private static final int METHANOL = METHANE+1;
	private static final int OXYGEN = METHANOL+1;
	private static final int WATER = OXYGEN+1;
	private static final int FOOD = WATER+1;
	private static final int ROCK_SAMPLES = FOOD+1;
	private static final int ICE = ROCK_SAMPLES+1;
	/** The number of Columns. */
	private static final int COLUMNCOUNT = ICE+1;
	/** Names of Columns. */
	private static final ColumnSpec[] COLUMNS;
	private static final Map<Integer,Integer> RESOURCE_TO_COL;

	/**
	 * Class initialiser creates the static names and classes.
	 */
	static {
		COLUMNS = new ColumnSpec[COLUMNCOUNT];
		COLUMNS[NAME] = new ColumnSpec("Name", String.class);
		COLUMNS[TYPE] = new ColumnSpec("Type", String.class);
		COLUMNS[MODEL] = new ColumnSpec("Model", String.class);
		COLUMNS[SETTLEMENT] = new ColumnSpec("Settlement", String.class);
		COLUMNS[LOCATION] = new ColumnSpec("Location", String.class);
		COLUMNS[DESTINATION] = new ColumnSpec("Next Waypoint", String.class);
		COLUMNS[DESTDIST] = new ColumnSpec("Dist. to next [km]", Double.class);
		COLUMNS[MISSION] = new ColumnSpec("Mission", String.class);
		COLUMNS[CREW] = new ColumnSpec("Crew", Integer.class);
		COLUMNS[DRIVER] = new ColumnSpec("Driver", String.class);
		COLUMNS[STATUS] = new ColumnSpec("Status", String.class);
		COLUMNS[BEACON] = new ColumnSpec("Beacon", String.class);
		COLUMNS[RESERVED] = new ColumnSpec("Reserved", String.class);
		COLUMNS[SPEED] = new ColumnSpec("Speed", Double.class);
		COLUMNS[MALFUNCTION] = new ColumnSpec("Malfunction", String.class);
		COLUMNS[BATTERY] = new ColumnSpec("Battery", String.class);
		COLUMNS[FUEL] = new ColumnSpec("Fuel", String.class);
		COLUMNS[METHANE] = new ColumnSpec("Methane", Double.class);
		COLUMNS[METHANOL] = new ColumnSpec("Methanol", Double.class);
		COLUMNS[OXYGEN] = new ColumnSpec("Oxygen", Double.class);
		COLUMNS[WATER] = new ColumnSpec("Water", Double.class);
		COLUMNS[FOOD] = new ColumnSpec("Food", Double.class);
		COLUMNS[ROCK_SAMPLES] = new ColumnSpec("Rock Samples", Double.class);
		COLUMNS[ICE] = new ColumnSpec("Ice", Double.class);

		RESOURCE_TO_COL = new HashMap<>();
		RESOURCE_TO_COL.put(ResourceUtil.OXYGEN_ID, OXYGEN);
		RESOURCE_TO_COL.put(ResourceUtil.METHANOL_ID, METHANOL);
		RESOURCE_TO_COL.put(ResourceUtil.FOOD_ID, FOOD);
		RESOURCE_TO_COL.put(ResourceUtil.WATER_ID, WATER);
		RESOURCE_TO_COL.put(ResourceUtil.ROCK_SAMPLES_ID, ROCK_SAMPLES);
		RESOURCE_TO_COL.put(ResourceUtil.ICE_ID, ICE);
	}

	private static MissionManager missionManager = Simulation.instance().getMissionManager();

	private transient LocalMissionManagerListener missionManagerListener;
	
	public VehicleTableModel() {
		super(
			Msg.getString("VehicleTableModel.tabName"),
			"VehicleTableModel.countingVehicles", //$NON-NLS-1$
			COLUMNS
		);

		setCachedColumns(OXYGEN, ICE);
		setSettlementColumn(SETTLEMENT);
		missionManagerListener = new LocalMissionManagerListener();
	}

	/**
	 * Filters the vehicles to a settlement.
	 */
	@Override
	public boolean setSettlementFilter(Set<Settlement> filter) {
		
		Collection<Vehicle> vehicles = filter.stream()
				.flatMap(s -> s.getAllAssociatedVehicles().stream())
				.sorted(Comparator.comparing(Vehicle::getName))
				.toList();
	
		resetItems(vehicles);
		
		return true;
	}

	/**
	 * Returns the value of a Cell.
	 * 
	 * @param rowIndex Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	@Override
	protected Object getItemValue(Vehicle vehicle, int columnIndex) {
		Object result = null;
		
		switch (columnIndex) {
			case NAME : 
				result = vehicle.getName();
				break;

			case SETTLEMENT : 
				result = vehicle.getAssociatedSettlement().getName();
				break;

			case TYPE :
				result = vehicle.getVehicleSpec().getName();
				break;

			case MODEL :
				result = vehicle.getModelName();
				break;
				
			case LOCATION : {
				Mission mission = vehicle.getMission();
				if (mission instanceof AbstractVehicleMission vm) {
					NavPoint nav = vm.getCurrentNavpoint();
					result = (nav != null ? nav.getDescription() : vehicle.getCoordinates().getFormattedString());
				}
				else {
					Settlement settle = vehicle.getSettlement();
					if (settle != null) {
						result = settle.getName();
					}
					else {
						var c = vehicle.getCoordinates();
						settle = CollectionUtils.findSettlement(c);
						if (settle != null) {
							result = settle.getName();
						}
						else {
							result = c.getFormattedString();
						}
					}
				}
		
			} break;

			case DESTINATION : {
				Mission mission = vehicle.getMission();
				if (mission instanceof AbstractVehicleMission vm) {
					result = vm.getNextNavpointDescription();
				}
			} break;

			case DESTDIST : {
				Mission mission = vehicle.getMission();
				if (mission instanceof VehicleMission vm) {
					result = vm.getDistanceCurrentLegRemaining();
				}
			} break;

			case MISSION : {
				Mission mission = vehicle.getMission();
				if (mission != null) {
					result = mission.getFullMissionDesignation();
				}
			} break;

			case CREW : {
				if (vehicle instanceof Crewable c) {
					int num = c.getCrewNum();
					if (num == 0)
						result = null;
					else
						result = num;
				}
			} break;

			case DRIVER :
				result = (vehicle.getOperator() != null ? vehicle.getOperator().getName() : null);
				break;

			case SPEED :
				var value = vehicle.getSpeed();
				if (value == 0.0)
					result = null;
				else
					result = value;
				break;

			// Status is a combination of Mechanical failure and maintenance
			case STATUS :
				result = vehicle.printStatusTypes();
				break;

			case BEACON :
				result = (vehicle.isBeaconOn() ? ON : OFF);
				break;

			case RESERVED :
				result = (vehicle.isReserved() ? TRUE : FALSE);
				break;

			case MALFUNCTION: {
				Malfunction failure = vehicle.getMalfunctionManager().getMostSeriousMalfunction();
				if (failure != null) result = failure.getName();
			} break;

			case BATTERY : 
				value = vehicle.getController().getBattery().getBatteryPercent();
				result = Math.round(value * 10.0)/10.0 + PERCENT;
				break;
				
			case FUEL :
				value = vehicle.getFuelPercent();
				result = Math.round(value * 10.0)/10.0 + PERCENT;
				break;

			case METHANE : 
				value = vehicle.getSpecificAmountResourceStored(ResourceUtil.METHANE_ID);
				if (value == 0.0)
					result = null;
				else
					result = value;
				break;

			case METHANOL : 
				value = vehicle.getSpecificAmountResourceStored(ResourceUtil.METHANOL_ID);
				if (value == 0.0)
					result = null;
				else
					result = value;
				break;			

			case OXYGEN : 
				value = vehicle.getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID);
				if (value == 0.0)
					result = null;
				else
					result = value;
				break;
				
			case WATER :
				value = vehicle.getSpecificAmountResourceStored(ResourceUtil.WATER_ID);
				if (value == 0.0)
					result = null;
				else
					result = value;
				break;

			case FOOD : 
				value = vehicle.getSpecificAmountResourceStored(ResourceUtil.FOOD_ID);
				if (value == 0.0)
					result = null;
				else
					result = value;
				break;
				
			case ROCK_SAMPLES : ;
				value = vehicle.getSpecificAmountResourceStored(ResourceUtil.ROCK_SAMPLES_ID);
				if (value == 0.0)
					result = null;
				else
					result = value;
				break;

			case ICE :
				value = vehicle.getSpecificAmountResourceStored(ResourceUtil.ICE_ID);
				if (value == 0.0)
					result = null;
				else
					result = value;
				break;
			
			default:
				throw new IllegalArgumentException("Unknown column");
		}

		return result;
	}

	/**
	 * Catches unit update event.
	 * 
	 * @param event the unit event.
	 */
	@Override
	public void entityUpdate(EntityEvent event) {
		Vehicle vehicle = (Vehicle) event.getSource();
		Object target = event.getTarget();
		String eventType = event.getType();

		int columnNum = -1;
		if (EntityEventType.NAME_EVENT.equals(eventType)) {
			columnNum = NAME;
		} else if (EntityEventType.COORDINATE_EVENT.equals(eventType)) {
			columnNum = LOCATION;
		} else if (EntityEventType.INVENTORY_STORING_UNIT_EVENT.equals(eventType) || 
		           EntityEventType.INVENTORY_RETRIEVING_UNIT_EVENT.equals(eventType)) {
			if (((Unit)target).getUnitType() == UnitType.PERSON)
				columnNum = CREW;
		} else if (EntityEventType.OPERATOR_EVENT.equals(eventType)) {
			columnNum = DRIVER;
		} else if (EntityEventType.STATUS_EVENT.equals(eventType)) {
			columnNum = STATUS;
		} else if (EntityEventType.EMERGENCY_BEACON_EVENT.equals(eventType)) {
			columnNum = BEACON;
		} else if (EntityEventType.RESERVED_EVENT.equals(eventType)) {
			columnNum = RESERVED;
		} else if (EntityEventType.SPEED_EVENT.equals(eventType)) {
			columnNum = SPEED;
		} else if (MalfunctionManager.MALFUNCTION_EVENT.equals(eventType)) {
			columnNum = MALFUNCTION;
		} else if (EntityEventType.INVENTORY_RESOURCE_EVENT.equals(eventType)) {
			int resourceId = -1;
			if (target instanceof AmountResource ar) {
				resourceId = ar.getID();
			}
			else if (target instanceof Integer item) {
				resourceId = item;
				if (resourceId >= ResourceUtil.FIRST_ITEM_RESOURCE_ID)
					// if it's an item resource, quit
					return;
			}

			if (RESOURCE_TO_COL.containsKey(resourceId)) 
				columnNum = RESOURCE_TO_COL.get(resourceId);
		}

		if (columnNum > -1) {
			entityValueUpdated(vehicle, columnNum, columnNum);
		}
	}
	
	/**
	 * Prepares the model for deletion.
	 */
	@Override
	public void destroy() {
		super.destroy();

		if (missionManagerListener != null) {
			missionManagerListener.destroy();
		}
		missionManagerListener = null;
	}


	private class LocalMissionManagerListener implements MissionManagerListener {

		private List<Mission> missions;
		private EntityListener missionListener;

		LocalMissionManagerListener() {
			missionListener = new LocalMissionListener();

			missions = missionManager.getMissions();

			for (Mission m : missions)
				addMission(m);
		}

		/**
		 * Adds a new mission.
		 * 
		 * @param mission the new mission.
		 */
		public void addMission(Mission mission) {
			mission.addEntityListener(missionListener);
			fireTableDataChanged();
		}

		/**
		 * Removes an old mission.
		 * 
		 * @param mission the old mission.
		 */
		public void removeMission(Mission mission){
			mission.removeEntityListener(missionListener);
			fireTableDataChanged();
		}

		/**
		 * Prepares for deletion.
		 */
		public void destroy() {
			for (Mission m : missions) removeMission(m);
			missions = null;
			missionListener = null;
		}
	}

	/**
	 * MissionListener inner class.
	 */
	private class LocalMissionListener implements EntityListener {

		/**
		 * Catch entity update event.
		 * @param event the entity event.
		 */
		@Override
		public void entityUpdate(EntityEvent event) {
			if (event.getSource() instanceof VehicleMission vm) {
				String eventType = event.getType();
				int columnNum = switch(eventType) {
					case VehicleMission.TRAVEL_STATUS_EVENT, VehicleMission.NAVPOINTS_EVENT -> DESTINATION;
					case VehicleMission.DISTANCE_EVENT -> DESTDIST;
					case VehicleMission.VEHICLE_EVENT -> MISSION;
					default -> -1;
				};
	
				if (columnNum > -1) {
					Vehicle vehicle = vm.getVehicle();
					if (vehicle != null) {
						entityValueUpdated(vehicle, columnNum, columnNum);
					}
				}
			}
		}
	}
}
