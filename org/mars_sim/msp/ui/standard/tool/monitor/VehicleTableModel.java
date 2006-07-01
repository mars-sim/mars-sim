/**
 * Mars Simulation Project
 * VehicleTableModel.java
 * @version 2.79 2006-06-01
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.standard.tool.monitor;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.malfunction.Malfunction;
import org.mars_sim.msp.simulation.person.ai.mission.NavPoint;
import org.mars_sim.msp.simulation.person.ai.mission.TravelMission;
import org.mars_sim.msp.simulation.person.ai.mission.VehicleMission;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.Crewable;
import org.mars_sim.msp.simulation.vehicle.*;

/**
 * The VehicleTableModel that maintains a list of Vehicle objects.
 * It maps key attributes of the Vehicle into Columns.
 */
public class VehicleTableModel extends UnitTableModel {

    // Column indexes
    private final static int  NAME = 0;
    private final static int  TYPE = 1;
    private final static int  LOCATION = 2;
    private final static int  DESTINATION = 3;
    private final static int  DESTDIST = 4;
    private final static int  MISSION = 5;
    private final static int  CREW = 6;
    private final static int  DRIVER = 7;
    private final static int  STATUS = 8;
    private final static int  BEACON = 9;
    private final static int  RESERVED = 10;
    private final static int  SPEED = 11;
    private final static int  MALFUNCTION = 12;
    private final static int  OXYGEN = 13;
    private final static int  METHANE = 14;
    private final static int  WATER = 15;
    private final static int  FOOD = 16;
    private final static int  ROCK_SAMPLES = 17;
    private final static int  ICE = 18;
    private final static int  COLUMNCOUNT = 19; // The number of Columns
    private static String columnNames[]; // Names of Columns
    private static Class columnTypes[]; // Names of Columns

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
        columnNames[DESTINATION] = "Destination";
        columnTypes[DESTINATION] = Coordinates.class;
        columnNames[DESTDIST] = "Dest. Dist.";
        columnTypes[DESTDIST] = Integer.class;
        columnNames[MISSION] = "Mission";
        columnTypes[MISSION] = String.class;
        columnNames[FOOD] = "Food";
        columnTypes[FOOD] = Integer.class;
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

    /**
     * Constructs a VehicleTableModel object. It creates the list of possible
     * Vehicles from the Unit manager.
     *
     * @param unitManager Proxy manager contains displayable Vehicles.
     */
    public VehicleTableModel(UnitManager unitManager) {
        super("All Vehicles", " vehicles", columnNames, columnTypes);

		setSource(unitManager.getVehicles());
    }

    /**
     * Return the value of a Cell
     * @param rowIndex Row index of the cell.
     * @param columnIndex Column index of the cell.
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object result = null;
        Vehicle vehicle = (Vehicle)getUnit(rowIndex);

        // Invoke the appropriate method, switch is the best solution
        // althought disliked by some
        switch (columnIndex) {
            case NAME : {
                result = vehicle.getName();
            } break;

            case TYPE : {
            	result = vehicle.getDescription();
            } break;
            
            case CREW : {
		        if (vehicle instanceof Crewable)
		            result = new Integer(((Crewable) vehicle).getCrewNum());
		        else result = new Integer(0);
            } break;

            case WATER : {
	        double water = vehicle.getInventory().getAmountResourceStored(AmountResource.WATER);
	        result = new Integer((int) water);
            } break;

            case FOOD : {
	        double food = vehicle.getInventory().getAmountResourceStored(AmountResource.FOOD);
	        result = new Integer((int) food);
            } break;

            case OXYGEN : {
	        double oxygen = vehicle.getInventory().getAmountResourceStored(AmountResource.OXYGEN);
	        result = new Integer((int) oxygen);
            } break;

            case METHANE : {
	        double methane = vehicle.getInventory().getAmountResourceStored(AmountResource.METHANE);
	        result = new Integer((int) methane);
            } break;

            case ROCK_SAMPLES : {
	        double rockSamples = vehicle.getInventory().getAmountResourceStored(AmountResource.ROCK_SAMPLES);
	        result = new Integer((int) rockSamples);
            } break;

            case SPEED : {
                result = new Integer(new Float(vehicle.getSpeed()).intValue());
            } break;

            case DRIVER : {
                if (vehicle.getOperator() != null) {
                	result = vehicle.getOperator().getOperatorName();
                }
                else {
                    result = null;
                }
            } break;

            // Status is a combination of Mechical failure and maintenance
            case STATUS : {
                result = vehicle.getStatus();
            } break;
            
            case BEACON : {
            	if (vehicle.isEmergencyBeacon()) result = "on";
            	else result = "off";
            } break;
            
            case RESERVED : {
            	if (vehicle.isReserved()) result = "true";
            	else result = "false";
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
            	VehicleMission mission = (VehicleMission) 
						Simulation.instance().getMissionManager().getMissionForVehicle(vehicle);
            	if (mission != null) {
            		if (mission.getTravelStatus().equals(TravelMission.TRAVEL_TO_NAVPOINT)) {
            			NavPoint destination = mission.getNextNavpoint();
            			if (destination.isSettlementAtNavpoint()) result = destination.getSettlement().getName();
            			else result = destination.getLocation().getFormattedString();
            		}
            	}
            } break;

            case DESTDIST : {
            	VehicleMission mission = (VehicleMission) 
						Simulation.instance().getMissionManager().getMissionForVehicle(vehicle);
            	if (mission != null) {
            		try {
            			result = new Integer(new Float(mission.getCurrentLegRemainingDistance()).intValue());
            		}
            		catch (Exception e) {
            			System.err.println("Error getting current leg remaining distance.");
            			e.printStackTrace(System.err);
            		}
            	}
            	else result = null;
            } break;
            
            case MISSION : {
            	VehicleMission mission = (VehicleMission) 
            			Simulation.instance().getMissionManager().getMissionForVehicle(vehicle);
            	if (mission != null) {
            		result = mission.getName();
            	}
            	else result = null;
            } break;
            
			case ICE : {
				double ice = vehicle.getInventory().getAmountResourceStored(AmountResource.ICE);
				result = new Integer((int) ice);
			} break;
        }

        return result;
    }
    
	/**
	 * Defines the source data from this table
	 */
	private void setSource(VehicleCollection source) {
		VehicleIterator iter = source.iterator();
		while(iter.hasNext()) {
			add(iter.next());
		}

		source.addMspCollectionEventListener(this);
	}
	
	/**
	 * The Model should be updated to reflect any changes in the underlying
	 * data.
	 * @return A status string for the contents of the model.
	 */
	public String update() {
		return update(Simulation.instance().getUnitManager().getVehicles());
	}
}