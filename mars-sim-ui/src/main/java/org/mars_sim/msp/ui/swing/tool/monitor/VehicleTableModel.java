/**
 * Mars Simulation Project
 * VehicleTableModel.java
 * @version 3.1.0 2017-03-03
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.person.ai.mission.MissionEventType;
import org.mars_sim.msp.core.person.ai.mission.MissionListener;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.MissionManagerListener;
import org.mars_sim.msp.core.person.ai.mission.NavPoint;
import org.mars_sim.msp.core.person.ai.mission.TravelMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.tool.Conversion;

/**
 * The VehicleTableModel that maintains a list of Vehicle objects.
 * It maps key attributes of the Vehicle into Columns.
 */
public class VehicleTableModel
extends UnitTableModel {

	//private DecimalFormat decFormatter = new DecimalFormat("#,###,###.#");

	private static Logger logger = Logger.getLogger(VehicleTableModel.class.getName());

	// Column indexes
	private final static int NAME = 0;
	private final static int TYPE = 1;
	private final static int LOCATION = 2;
	private final static int DESTINATION = 3;
	private final static int DESTDIST = 4;
	private final static int MISSION = 5;
	private final static int CREW = 6;
	private final static int BOTS = 7;
	private final static int DRIVER = 8;
	private final static int STATUS = 9;
	private final static int BEACON = 10;
	private final static int RESERVED = 11;
	private final static int SPEED = 12;
	private final static int MALFUNCTION = 13;
	private final static int OXYGEN = 14;
	private final static int METHANE = 15;
	private final static int WATER = 16;
	private final static int FOOD = 17;
	private final static int DESSERT = 18;
	private final static int ROCK_SAMPLES = 19;
	private final static int ICE = 20;
	/** The number of Columns. */
	private final static int COLUMNCOUNT = 21;
	/** Names of Columns. */
	private static String columnNames[];
	
	private static String ON = "On";	
	private static String OFF = "Off";
	private static String TRUE = "True";	
	private static String FALSE = "False";
		
	/** Names of Columns. */
	private static Class<?> columnTypes[];
	
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
		columnNames[DRIVER] = "Driver";
		columnTypes[DRIVER] = String.class;
		columnNames[STATUS] = "Status";
		columnTypes[STATUS] = String.class;
		columnNames[BEACON] = "Beacon";
		columnTypes[BEACON] = String.class;
		columnNames[RESERVED] = "Reserved";
		columnTypes[RESERVED] = String.class;
		columnNames[LOCATION] = "Location";
		columnTypes[LOCATION] = String.class;
		columnNames[SPEED] = "Speed";
		columnTypes[SPEED] = Integer.class;
		columnNames[MALFUNCTION] = "Malfunction";
		columnTypes[MALFUNCTION] = String.class;
		columnNames[CREW] = "Crew";
		columnTypes[CREW] = Integer.class;
		columnNames[BOTS] = "Bots";
		columnTypes[BOTS] = Integer.class;
		columnNames[DESTINATION] = "Destination";
		columnTypes[DESTINATION] = Coordinates.class;
		columnNames[DESTDIST] = "Dest. Dist.";
		columnTypes[DESTDIST] = Integer.class;
		columnNames[MISSION] = "Mission";
		columnTypes[MISSION] = String.class;
		columnNames[FOOD] = "Food";
		columnTypes[FOOD] = Integer.class;
		columnNames[DESSERT] = "Dessert";
		columnTypes[DESSERT] = Integer.class;
		columnNames[OXYGEN] = "Oxygen";
		columnTypes[OXYGEN] = Integer.class;
		columnNames[WATER] = "Water";
		columnTypes[WATER] = Integer.class;
		columnNames[METHANE] = "Methane";
		columnTypes[METHANE] = Integer.class;
		columnNames[ROCK_SAMPLES] = "Rock Samples";
		columnTypes[ROCK_SAMPLES] = Integer.class;
		columnNames[ICE] = "Ice";
		columnTypes[ICE] = Integer.class;
	}

	// Data members
	private UnitManagerListener unitManagerListener;
	private LocalMissionManagerListener missionManagerListener;
	private Map<Unit, Map<Integer, Double>> resourceCache;

	private static int foodID = ResourceUtil.foodID;
	private static int oxygenID = ResourceUtil.oxygenID;
	private static int waterID = ResourceUtil.waterID;
	private static int methaneID = ResourceUtil.methaneID;
	private static int rockSamplesID = ResourceUtil.rockSamplesID;
	private static int iceID = ResourceUtil.iceID;

	private static AmountResource [] availableDesserts = PreparingDessert.getArrayOfDessertsAR();

	private static UnitManager unitManager = Simulation.instance().getUnitManager();

	private static MissionManager missionManager = Simulation.instance().getMissionManager();
	
	private int mapSizeCache = 0;
	
	/**
	 * Constructs a VehicleTableModel object. It creates the list of possible
	 * Vehicles from the Unit manager.
	 *
	 * @param unitManager Proxy manager contains displayable Vehicles.
	 */
	public VehicleTableModel() { //UnitManager unitManager) {
		super(
			Msg.getString("VehicleTableModel.tabName"),
			"VehicleTableModel.countingVehicles", //$NON-NLS-1$
			columnNames,
			columnTypes
		);

		setSource(unitManager.getVehicles());
		unitManagerListener = new LocalUnitManagerListener();
		unitManager.addUnitManagerListener(unitManagerListener);

		missionManagerListener = new LocalMissionManagerListener();
	}

	/**
	 * Return the value of a Cell
	 * @param rowIndex Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object result = null;

		if (rowIndex < getUnitNumber()) {
			Vehicle vehicle = (Vehicle)getUnit(rowIndex);
			Map<Integer, Double> resourceMap = resourceCache.get(vehicle);

			try {
				// Invoke the appropriate method, switch is the best solution
				// although disliked by some
			switch (columnIndex) {
				case NAME : {
					result = vehicle.getName();
				} break;

				case TYPE : {
					result = Conversion.capitalize(vehicle.getDescription());
				} break;

				case CREW : {
					if (vehicle instanceof Crewable)
						result = ((Crewable) vehicle).getCrewNum();
					else result = 0;
				} break;

				case BOTS : {
					if (vehicle instanceof Crewable)
						result = ((Crewable) vehicle).getRobotCrewNum();
					else result = 0;
				} break;

				case WATER : {
					//result = decFormatter.format(resourceMap.get(AmountResource.findAmountResource(LifeSupport.WATER)));
					result = resourceMap.get(waterID);
				} break;

				case FOOD : {
					//result = decFormatter.format(resourceMap.get(AmountResource.findAmountResource(LifeSupport.FOOD)));
					result= resourceMap.get(foodID);
				} break;

				case DESSERT : {
					double sum = 0;
					int mapSize = resourceMap.size();
					if (mapSizeCache != mapSize) {
						mapSizeCache = mapSize ;
					}
		    		for (Integer ar : resourceMap.keySet()) {
		    	        for(AmountResource n : availableDesserts) {
		    	        	//if (n.getName().equals(ar.getName())) {
		    	        	//if (n.equals(ar)) {
			    	        if (n.getID() == ar) {
		    	        		double amount = resourceMap.get(ar);
		    	        		sum += amount;
		    	    			break;
		    	        	}
		    			}
		    		}
				    result = Double.valueOf(sum);

				} break;

				case OXYGEN : {
					//result = decFormatter.format(resourceMap.get(AmountResource.findAmountResource(LifeSupport.OXYGEN)));
					result = resourceMap.get(oxygenID);
				} break;

				case METHANE : {
					//result = decFormatter.format(resourceMap.get(AmountResource.findAmountResource("methane")));
					result = resourceMap.get(methaneID);
				} break;

				case ROCK_SAMPLES : {
					//result = decFormatter.format(resourceMap.get(AmountResource.findAmountResource("rock samples")));
					result = resourceMap.get(rockSamplesID);
				} break;

				case SPEED : {
					result = Math.round(vehicle.getSpeed()*10.0)/10.0;
				} break;

				case DRIVER : {
					if (vehicle.getOperator() != null) {
						result = vehicle.getOperator().getOperatorName();
					}
					else {
						result = null;
					}
				} break;

				// Status is a combination of Mechanical failure and maintenance
				case STATUS : {
					result = vehicle.getStatus();
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
					result = null;
					Mission mission = missionManager.getMissionForVehicle(vehicle);
					if ((mission != null) && (mission instanceof VehicleMission)) {
						VehicleMission vehicleMission = (VehicleMission) mission;
						if (vehicleMission.getTravelStatus() != null 
								&& vehicleMission.getTravelStatus().equals(TravelMission.TRAVEL_TO_NAVPOINT)) {
							NavPoint destination = vehicleMission.getNextNavpoint();
							if (destination.isSettlementAtNavpoint()) result = destination.getSettlement().getName();
							else result = destination.getLocation().getFormattedString();
						}
					}
				} break;

				case DESTDIST : {
					Mission mission = missionManager.getMissionForVehicle(vehicle);
					if ((mission != null) && (mission instanceof VehicleMission)) {
						VehicleMission vehicleMission = (VehicleMission) mission;
						try {
							result = Math.round(vehicleMission.getCurrentLegRemainingDistance()*10.0)/10.0;
						}
						catch (Exception e) {
							logger.log(Level.SEVERE,"Error getting current leg remaining distance.");
							e.printStackTrace(System.err);
						}
					}
					else result = null;
				} break;

				case MISSION : {
					Mission mission = missionManager.getMissionForVehicle(vehicle);
					if (mission != null) {
						result = mission.getName();
					}
					else result = null;
				} break;

				case ICE : {
					//result = decFormatter.format(resourceMap.get(AmountResource.findAmountResource("ice")));
					result = resourceMap.get(iceID);
				} break;

				}
			}
			catch (Exception e) {
				logger.log(Level.SEVERE, "getValueAt() cannot return a valid result", e);
				e.printStackTrace(System.err);
			}
		}

		return result;
	}

	/**
	 * Catch unit update event.
	 * @param event the unit event.
	 */
	public void unitUpdate(UnitEvent event) {
		Unit unit = (Unit) event.getSource();
		int unitIndex = getUnitIndex(unit);
		Object source = event.getTarget();
		UnitEventType eventType = event.getType();

		
		int columnNum = -1;
		if (eventType == UnitEventType.NAME_EVENT) columnNum = NAME;
		else if (eventType == UnitEventType.LOCATION_EVENT) columnNum = LOCATION;
		else if (eventType == UnitEventType.INVENTORY_STORING_UNIT_EVENT ||
				eventType == UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT) {
			if (source instanceof Person) columnNum = CREW;
			else if (source instanceof Robot) columnNum = BOTS;
		}
		else if (eventType == UnitEventType.OPERATOR_EVENT) columnNum = DRIVER;
		else if (eventType == UnitEventType.STATUS_EVENT) columnNum = STATUS;
		else if (eventType == UnitEventType.EMERGENCY_BEACON_EVENT) columnNum = BEACON;
		else if (eventType == UnitEventType.RESERVED_EVENT) columnNum = RESERVED;
		else if (eventType == UnitEventType.SPEED_EVENT) columnNum = SPEED;
		else if (eventType == UnitEventType.MALFUNCTION_EVENT) columnNum = MALFUNCTION;
		else if (eventType == UnitEventType.INVENTORY_RESOURCE_EVENT) {
			try {
				int tempColumnNum = -1;
				int target = ((AmountResource)source).getID();
				
				if (target == oxygenID)
					tempColumnNum = OXYGEN;
				else if (target == methaneID)
					tempColumnNum = METHANE;
				else if (target == foodID)
					tempColumnNum = FOOD;
				else if (target == waterID)
					tempColumnNum = WATER;
				else if (target == rockSamplesID)
					tempColumnNum = ROCK_SAMPLES;
				else if (target == iceID)
					tempColumnNum = ICE;
				else {
				  	// Put together a list of available dessert
			        for(AmountResource ar : availableDesserts) {
			        	if (target == ar.getID())
			        		tempColumnNum = DESSERT;
			        }
				}

				if (tempColumnNum > -1) {
					// Convert resourceCache and resourceMap from Map<AmountResource, Integer> to Map<AmountResource, Double> in VehicleTableModel.java.
					double currentValue =  Math.round ( (Double) getValueAt(unitIndex, tempColumnNum) * 10.0 ) / 10.0;
					double newValue = Math.round (getResourceStored(unit, target) * 10.0 ) / 10.0;
					if (currentValue != newValue) {
						//System.out.println("Column : " + tempColumnNum + "  currentValue : " + currentValue + "   newValue : " + newValue);
						columnNum = tempColumnNum;
						Map<Integer, Double> resourceMap = resourceCache.get(unit);
						resourceMap.put(target, newValue);
					}
				}
			}
			catch (Exception e) {
				logger.log(Level.SEVERE, "Issues with unitUpdate()", e);
			}
		}

		if (columnNum > -1) {
			SwingUtilities.invokeLater(new VehicleTableCellUpdater(unitIndex, columnNum));
		}
	}

	/**
	 * Defines the source data from this table
	 */
	private void setSource(Collection<Vehicle> source) {
		Iterator<Vehicle> iter = source.iterator();
		while(iter.hasNext()) addUnit(iter.next());
	}

	/**
	 * Add a unit to the model.
	 * @param newUnit Unit to add to the model.
	 */
	protected void addUnit(Unit newUnit) {
		if (resourceCache == null) resourceCache = new HashMap<Unit, Map<Integer, Double>>();
		if (!resourceCache.containsKey(newUnit)) {
			try {
				Map<Integer, Double> resourceMap = new HashMap<Integer, Double>();
				resourceMap.put(foodID, Math.round(100.0 * getResourceStored(newUnit, foodID))/100.0);
				resourceMap.put(oxygenID, Math.round(100.0 * getResourceStored(newUnit, oxygenID))/100.0);
				resourceMap.put(waterID, Math.round(100.0 * getResourceStored(newUnit, waterID))/100.0);
				resourceMap.put(methaneID, Math.round(100.0 *getResourceStored(newUnit, methaneID))/100.0);
				resourceMap.put(rockSamplesID, Math.round(100.0 *getResourceStored(newUnit, rockSamplesID))/100.0);
				resourceMap.put(iceID, Math.round(100.0 *getResourceStored(newUnit, iceID))/100.0);
			  	// Put together a list of available dessert
		        for(AmountResource ar : availableDesserts) {
		        	resourceMap.put(ar.getID(), Math.round(100.0 *getResourceStored(newUnit, ar.getID()))/100.0);
		        }

				resourceCache.put(newUnit, resourceMap);

			}
			catch (Exception e) {
				logger.log(Level.SEVERE, "addUnit() does not work when creating resourceCache", e);
			}
		}
		super.addUnit(newUnit);
	}

	/**
	 * Remove a unit to the model.
	 * @param oldUnit Unit to remove from the model.
	 */
	protected void removeUnit(Unit oldUnit) {
		if (resourceCache == null) resourceCache = new HashMap<Unit, Map<Integer, Double>>();
		if (resourceCache.containsKey(oldUnit)) {
			Map<Integer, Double> resourceMap = resourceCache.get(oldUnit);
			resourceMap.clear();
			resourceCache.remove(oldUnit);
		}
		super.removeUnit(oldUnit);
	}

	/**
	 * Gets the Double amount of resources stored in a unit.
	 * @param unit the unit to check.
	 * @param resource the resource to check.
	 * @return  amount of resource.
	 */
	private double getResourceStored(Unit unit, int resource) {
//		return unit.getInventory().getAmountResourceStored(resource, false);
		return Math.round(unit.getInventory().getAmountResourceStored(resource, true) * 100.0) / 100.0;
	}

	/**
	 * Prepares the model for deletion.
	 */
	public void destroy() {
		super.destroy();

		unitManager.removeUnitManagerListener(unitManagerListener);
		unitManager = null;
		unitManagerListener = null;

		if (missionManagerListener != null) {
			missionManagerListener.destroy();
		}
		missionManagerListener = null;

		if (resourceCache != null) {
			resourceCache.clear();
		}
		resourceCache = null;
	}

	/**
	 * UnitManagerListener inner class.
	 */
	private class LocalUnitManagerListener implements UnitManagerListener {

		/**
		 * Catch unit manager update event.
		 * @param event the unit event.
		 */
		public void unitManagerUpdate(UnitManagerEvent event) {
			Unit unit = event.getUnit();
			UnitManagerEventType eventType = event.getEventType();
			if (unit instanceof Vehicle) {
				if (eventType == UnitManagerEventType.ADD_UNIT) {
					if (!containsUnit(unit)) addUnit(unit);
				}
				else if (eventType == UnitManagerEventType.REMOVE_UNIT) {
					if (containsUnit(unit)) removeUnit(unit);
				}
			}
		}
	}

	private class LocalMissionManagerListener implements MissionManagerListener {

		private List<Mission> missions;
		private MissionListener missionListener;

		LocalMissionManagerListener() {
			missionListener = new LocalMissionListener();
			missions = missionManager.getMissions();
			//Iterator<Mission> i = missions.iterator();
			//while (i.hasNext()) addMission(i.next());
			for (Mission m : missions) addMission(m);			
		}

		/**
		 * Adds a new mission.
		 * @param mission the new mission.
		 */
		public void addMission(Mission mission) {
			mission.addMissionListener(missionListener);
			updateVehicleMissionCell(mission);
		}

		/**
		 * Removes an old mission.
		 * @param mission the old mission.
		 */
		public void removeMission(Mission mission){
			mission.removeMissionListener(missionListener);
			updateVehicleMissionCell(mission);
		}

		private void updateVehicleMissionCell(Mission mission) {
			//    		if (mission instanceof VehicleMission) {
			//    			Vehicle vehicle = ((VehicleMission) mission).getVehicle();
			//    			if (vehicle != null) {
			//    				int unitIndex = getUnitIndex(vehicle);
			//    				SwingUtilities.invokeLater(new VehicleTableCellUpdater(unitIndex, MISSION));
			//    			}
			//    		}

			// Update all table cells because construction/salvage mission may affect more than one vehicle.
			fireTableDataChanged();
		}

		/**
		 * Prepares for deletion.
		 */
		public void destroy() {
			//Iterator<Mission> i = missions.iterator();
			//while (i.hasNext()) removeMission(i.next());
			for (Mission m : missions) removeMission(m);
			missions = null;
			missionListener = null;
		}
	}

	/**
	 * Inner class for updating a cell in the vehicle table.
	 */
	private class VehicleTableCellUpdater implements Runnable {

		private int row;
		private int column;

		private VehicleTableCellUpdater(int row, int column) {
			this.row = row;
			this.column = column;
		}

		public void run() {
			fireTableCellUpdated(row, column);
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
			if (
					eventType == MissionEventType.TRAVEL_STATUS_EVENT ||
					eventType == MissionEventType.NAVPOINTS_EVENT
					) columnNum = DESTINATION;
			else if (eventType == MissionEventType.DISTANCE_EVENT) columnNum = DESTDIST;
			else if (eventType == MissionEventType.VEHICLE_EVENT) columnNum = MISSION;

			if (columnNum > -1) {
				if (mission instanceof VehicleMission) {
					Vehicle vehicle = ((VehicleMission) mission).getVehicle();
					if (vehicle != null) {
						int unitIndex = getUnitIndex(vehicle);
						SwingUtilities.invokeLater(new VehicleTableCellUpdater(unitIndex, columnNum));
					}
				}
			}
		}
	}
}