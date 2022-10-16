/*
 * Mars Simulation Project
 * VehicleTableModel.java
 * @date 2021-10-23
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.List;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.person.ai.mission.MissionEventType;
import org.mars_sim.msp.core.person.ai.mission.MissionListener;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.MissionManagerListener;
import org.mars_sim.msp.core.person.ai.mission.NavPoint;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.tool.Conversion;

/**
 * The VehicleTableModel that maintains a list of Vehicle objects.
 * It maps key attributes of the Vehicle into Columns.
 */
@SuppressWarnings("serial")
public class VehicleTableModel extends UnitTableModel<Vehicle> {

	private static final Logger logger = Logger.getLogger(VehicleTableModel.class.getName());

	private static final String ON = "On";
	private static final String OFF = "Off";
	private static final String TRUE = "True";
	private static final String FALSE = "False";

	// Column indexes
	private static final int NAME = 0;
	private static final int TYPE = 1;
	private static final int HOME = 2;
	private static final int LOCATION = 3;
	private static final int DESTINATION = 4;
	private static final int DESTDIST = 5;
	private static final int MISSION = 6;
	private static final int CREW = 7;
	private static final int DRIVER = 8;
	private static final int STATUS = 9;
	private static final int BEACON = 10;
	private static final int RESERVED = 11;
	private static final int SPEED = 12;
	private static final int MALFUNCTION = 13;
	private static final int OXYGEN = 14;
	private static final int METHANE = 15;
	private static final int WATER = 16;
	private static final int FOOD = 17;
	private static final int DESSERT = 18;
	private static final int ROCK_SAMPLES = 19;
	private static final int ICE = 20;
	/** The number of Columns. */
	private static final int COLUMNCOUNT = 21;
	/** Names of Columns. */
	private static String[] columnNames;

	/** Names of Columns. */
	private static Class<?>[] columnTypes;

	/**
	 * Class initialiser creates the static names and classes.
	 */
	static {
		columnNames = new String[COLUMNCOUNT];
		columnTypes = new Class[COLUMNCOUNT];
		columnNames[NAME] = "Name";
		columnTypes[NAME] = String.class;
		columnNames[TYPE] = "Type";
		columnTypes[TYPE] = String.class;
		columnNames[HOME] = "Home";
		columnTypes[HOME] = String.class;
		columnNames[LOCATION] = "Location";
		columnTypes[LOCATION] = String.class;
		columnNames[DESTINATION] = "Next Waypoint";
		columnTypes[DESTINATION] = Coordinates.class;
		columnNames[DESTDIST] = "Dist. to next [km]";
		columnTypes[DESTDIST] = Integer.class;
		columnNames[MISSION] = "Mission";
		columnTypes[MISSION] = String.class;
		columnNames[CREW] = "Crew";
		columnTypes[CREW] = Integer.class;
		columnNames[DRIVER] = "Driver";
		columnTypes[DRIVER] = String.class;
		columnNames[STATUS] = "Status";
		columnTypes[STATUS] = String.class;
		columnNames[BEACON] = "Beacon";
		columnTypes[BEACON] = String.class;
		columnNames[RESERVED] = "Reserved";
		columnTypes[RESERVED] = String.class;
		columnNames[SPEED] = "Speed";
		columnTypes[SPEED] = Integer.class;
		columnNames[MALFUNCTION] = "Malfunction";
		columnTypes[MALFUNCTION] = String.class;
		columnNames[OXYGEN] = "Oxygen";
		columnTypes[OXYGEN] = Integer.class;
		columnNames[METHANE] = "Methane";
		columnTypes[METHANE] = Integer.class;
		columnNames[WATER] = "Water";
		columnTypes[WATER] = Integer.class;
		columnNames[FOOD] = "Food";
		columnTypes[FOOD] = Integer.class;
		columnNames[DESSERT] = "Dessert";
		columnTypes[DESSERT] = Integer.class;
		columnNames[ROCK_SAMPLES] = "Rock Samples";
		columnTypes[ROCK_SAMPLES] = Integer.class;
		columnNames[ICE] = "Ice";
		columnTypes[ICE] = Integer.class;
	}

	private static final int FOOD_ID = ResourceUtil.foodID;
	private static final int OXYGEN_ID = ResourceUtil.oxygenID;
	private static final int WATER_ID = ResourceUtil.waterID;
	private static final int METHANE_ID = ResourceUtil.methaneID;
	private static final int ROCK_SAMPLES_ID = ResourceUtil.rockSamplesID;
	private static final int ICE_ID = ResourceUtil.iceID;

	private static final AmountResource [] availableDesserts = PreparingDessert.getArrayOfDessertsAR();

	private static MissionManager missionManager = Simulation.instance().getMissionManager();

	private transient LocalMissionManagerListener missionManagerListener;
	
	public VehicleTableModel(Settlement settlement) {
		super(UnitType.VEHICLE,
			Msg.getString("VehicleTableModel.tabName"),
			"VehicleTableModel.countingVehicles", //$NON-NLS-1$
			columnNames,
			columnTypes
		);

		setSettlementFilter(settlement);

		setCachedColumns(OXYGEN, ICE);

		missionManagerListener = new LocalMissionManagerListener();
	}

	/**
	 * Filter the vehicles to a settlement
	 */
	@Override
	public boolean setSettlementFilter(Settlement filter) {
		resetEntities(filter.getAllAssociatedVehicles());

		return true;
	}

	/**
	 * Returns the value of a Cell.
	 * 
	 * @param rowIndex Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	@Override
	protected Object getEntityValue(Vehicle vehicle, int columnIndex) {
		Object result = null;

		switch (columnIndex) {
			case NAME : {
				result = vehicle.getName();
			} break;

			case TYPE : {
				result = vehicle.getDescription();
			} break;

			case HOME : {
				Settlement as = vehicle.getAssociatedSettlement();
				if (as != null) {
					result = as.getName();
				}
				else {
					result = vehicle.getCoordinates().getFormattedString();
				}
			} break;

			case LOCATION : {
				Settlement settle = vehicle.getSettlement();
				if (settle != null) {
					result = settle.getName();
				}
				else {
					result = vehicle.getCoordinates().getFormattedString();
				}
			} break;

			case DESTINATION : {
				Mission mission = missionManager.getMissionForVehicle(vehicle);
				if (mission instanceof VehicleMission) {
					VehicleMission vehicleMission = (VehicleMission) mission;

					NavPoint destination = vehicleMission.getCurrentDestination();
					if (destination.isSettlementAtNavpoint())
						result = destination.getSettlement().getName();
					else
						result = Conversion.capitalize(destination.getDescription()) 
							+ " - " + destination.getLocation().getFormattedString();
				}
			} break;

			case DESTDIST : {
				Mission mission = missionManager.getMissionForVehicle(vehicle);
				if (mission instanceof VehicleMission) {
					VehicleMission vehicleMission = (VehicleMission) mission;
					result = vehicleMission.getDistanceCurrentLegRemaining();
				}
				else result = null;
			} break;

			case MISSION : {
				Mission mission = missionManager.getMissionForVehicle(vehicle);
				if (mission != null) {
					result = mission.getFullMissionDesignation();
				}
				else result = null;
			} break;

			case CREW : {
				if (vehicle instanceof Crewable)
					result = ((Crewable) vehicle).getCrewNum();
				else result = 0;
			} break;

			case DRIVER : {
				if (vehicle.getOperator() != null) {
					result = vehicle.getOperator().getName();
				}
				else {
					result = null;
				}
			} break;

			case SPEED : {
				result = vehicle.getSpeed();
			} break;

			// Status is a combination of Mechanical failure and maintenance
			case STATUS : {
				result = vehicle.printStatusTypes();
			} break;

			case BEACON : {
				if (vehicle.isBeaconOn()) result = ON;
				else result = OFF;
			} break;

			case RESERVED : {
				if (vehicle.isReserved()) result = TRUE;
				else result = FALSE;
			} break;

			case MALFUNCTION: {
				Malfunction failure = vehicle.getMalfunctionManager().getMostSeriousMalfunction();
				if (failure != null) result = failure.getName();
			} break;


			case WATER : 
				result = vehicle.getAmountResourceStored(WATER_ID);
				break;

			case FOOD : 
				result = vehicle.getAmountResourceStored(FOOD_ID);
				break;

			case DESSERT : 
				double sum = 0;
				for (AmountResource n : availableDesserts) {
					double amount = vehicle.getAmountResourceStored(n.getID());
					sum += amount;
				}
				result = sum;
				break;

			case OXYGEN : 
				result = vehicle.getAmountResourceStored(OXYGEN_ID);
				break;

			case METHANE : 
				result = vehicle.getAmountResourceStored(METHANE_ID);
				break;

			case ROCK_SAMPLES : 
				result = vehicle.getAmountResourceStored(ROCK_SAMPLES_ID);
				break;

			case ICE : 
				result = vehicle.getAmountResourceStored(ICE_ID);
				break;
			
			default:
				result = null;
		}

		return result;
	}

	/**
	 * Catches unit update event.
	 * 
	 * @param event the unit event.
	 */
	@Override
	public void unitUpdate(UnitEvent event) {
		Unit unit = (Unit) event.getSource();

		if (unit.getUnitType() == UnitType.VEHICLE) {
			Vehicle vehicle = (Vehicle) unit;
			Object target = event.getTarget();
			UnitEventType eventType = event.getType();

			int columnNum = -1;
			if (eventType == UnitEventType.NAME_EVENT) columnNum = NAME;
			else if (eventType == UnitEventType.LOCATION_EVENT) columnNum = LOCATION;
			else if (eventType == UnitEventType.INVENTORY_STORING_UNIT_EVENT ||
					eventType == UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT) {
				if (((Unit)target).getUnitType() == UnitType.PERSON) columnNum = CREW;
			}
			else if (eventType == UnitEventType.OPERATOR_EVENT) columnNum = DRIVER;
			else if (eventType == UnitEventType.STATUS_EVENT) columnNum = STATUS;
			else if (eventType == UnitEventType.EMERGENCY_BEACON_EVENT) columnNum = BEACON;
			else if (eventType == UnitEventType.RESERVED_EVENT) columnNum = RESERVED;
			else if (eventType == UnitEventType.SPEED_EVENT) columnNum = SPEED;
			else if (eventType == UnitEventType.MALFUNCTION_EVENT) columnNum = MALFUNCTION;
			else if (eventType == UnitEventType.INVENTORY_RESOURCE_EVENT) {
				int resourceId = -1;
				if (target instanceof AmountResource) {
					resourceId = ((AmountResource)target).getID();
				}
				else if (target instanceof Integer) {
					resourceId = (Integer)target;
					if (resourceId >= ResourceUtil.FIRST_ITEM_RESOURCE_ID)
						// if it's an item resource, quit
						return;
				}

				if (resourceId == OXYGEN_ID) 
					columnNum = OXYGEN;
				else if (resourceId == METHANE_ID)
					columnNum = METHANE;
				else if (resourceId == FOOD_ID)
					columnNum = FOOD;
				else if (resourceId == WATER_ID)
					columnNum = WATER;
				else if (resourceId == ROCK_SAMPLES_ID)
					columnNum = ROCK_SAMPLES;
				else if (resourceId == ICE_ID)
					columnNum = ICE;
				else {
					// Put together a list of available dessert
					for(AmountResource ar : availableDesserts) {
						if (resourceId == ar.getID()) {
							columnNum = DESSERT;
						}
					}
				}
			}

			if (columnNum > -1) {
				SwingUtilities.invokeLater(new VehicleTableCellUpdater(vehicle, columnNum));
			}
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
		private MissionListener missionListener;

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
			mission.addMissionListener(missionListener);
			updateVehicleMissionCell(mission);
		}

		/**
		 * Removes an old mission.
		 * 
		 * @param mission the old mission.
		 */
		public void removeMission(Mission mission){
			mission.removeMissionListener(missionListener);
			updateVehicleMissionCell(mission);
		}

		private void updateVehicleMissionCell(Mission mission) {
//			if (mission instanceof VehicleMission) {
//				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
//				if (vehicle != null) {
//					int unitIndex = getUnitIndex(vehicle);
//					SwingUtilities.invokeLater(new VehicleTableCellUpdater(unitIndex, MISSION));
//				}
//			}

			// Update all table cells because construction/salvage mission may affect more than one vehicle.
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
	 * Inner class for updating a cell in the vehicle table.
	 */
	private class VehicleTableCellUpdater implements Runnable {

		private Vehicle vehicle;
		private int column;

		private VehicleTableCellUpdater(Vehicle vehicle, int column) {
			this.vehicle = vehicle;
			this.column = column;
		}

		public void run() {
			entityValueUpdated(vehicle, column, column);
		}
	}

	/**
	 * MissionListener inner class.
	 */
	private class LocalMissionListener implements MissionListener {

		/**
		 * Catch mission update event.
		 * @param event the mission event.
		 */
		public void missionUpdate(MissionEvent event) {
			Mission mission = (Mission) event.getSource();
			MissionEventType eventType = event.getType();
			int columnNum = -1;
			if (eventType == MissionEventType.TRAVEL_STATUS_EVENT ||
					eventType == MissionEventType.NAVPOINTS_EVENT
					) columnNum = DESTINATION;
			else if (eventType == MissionEventType.DISTANCE_EVENT) columnNum = DESTDIST;
			else if (eventType == MissionEventType.VEHICLE_EVENT) columnNum = MISSION;

			if (columnNum > -1) {
				if (mission instanceof VehicleMission) {
					Vehicle vehicle = ((VehicleMission) mission).getVehicle();
					if (vehicle != null) {
						SwingUtilities.invokeLater(new VehicleTableCellUpdater(vehicle, columnNum));
					}
				}
			}
		}
	}
}
