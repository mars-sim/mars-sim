/**
 * Mars Simulation Project
 * VehicleTableModel.java
 * @version 2.75 2004-01-12
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.standard.tool.monitor;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Resource;
import org.mars_sim.msp.simulation.UnitManager;
import org.mars_sim.msp.simulation.malfunction.Malfunction;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.Crewable;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.simulation.vehicle.VehicleIterator;

/**
 * The VehicleTableModel that maintains a list of Vehicle objects.
 * It maps key attributes of the Vehicle into Columns.
 */
public class VehicleTableModel extends UnitTableModel {

    // Column indexes
    private final static int  NAME = 0;
    private final static int  LOCATION = 1;
    private final static int  DESTINATION = 2;
    private final static int  DESTDIST = 3;
    private final static int  CREW = 4;
    private final static int  DRIVER = 5;
    private final static int  STATUS = 6;
    private final static int  SPEED = 7;
    private final static int  MALFUNCTION = 8;
    private final static int  OXYGEN = 9;
    private final static int  METHANE = 10;
    private final static int  WATER = 11;
    private final static int  FOOD = 12;
    private final static int  ROCK_SAMPLES = 13;
    private final static int  ICE = 14;
    private final static int  COLUMNCOUNT = 15; // The number of Columns
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
        columnNames[DRIVER] = "Driver";
        columnTypes[DRIVER] = String.class;
        columnNames[STATUS] = "Status";
        columnTypes[STATUS] = String.class;
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

        VehicleIterator iter = unitManager.getVehicles().sortByName().iterator();
        while(iter.hasNext()) {
            add(iter.next());
        }
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

            case CREW : {
		        if (vehicle instanceof Crewable)
		            result = new Integer(((Crewable) vehicle).getCrewNum());
		        else result = new Integer(0);
            } break;

            case WATER : {
	        double water = vehicle.getInventory().getResourceMass(Resource.WATER);
	        result = new Integer((int) water);
            } break;

            case FOOD : {
	        double food = vehicle.getInventory().getResourceMass(Resource.FOOD);
	        result = new Integer((int) food);
            } break;

            case OXYGEN : {
	        double oxygen = vehicle.getInventory().getResourceMass(Resource.OXYGEN);
	        result = new Integer((int) oxygen);
            } break;

            case METHANE : {
	        double methane = vehicle.getInventory().getResourceMass(Resource.METHANE);
	        result = new Integer((int) methane);
            } break;

            case ROCK_SAMPLES : {
	        double rockSamples = vehicle.getInventory().getResourceMass(Resource.ROCK_SAMPLES);
	        result = new Integer((int) rockSamples);
            } break;

            case SPEED : {
                result = new Integer(new Float(vehicle.getSpeed()).intValue());
            } break;

            case DRIVER : {
                if (vehicle.getDriver() != null) {
                    result = vehicle.getDriver().getName();
                }
                else {
                    result = null;
                }
            } break;

            // Status is a combination of Mechical failure and maintenance
            case STATUS : {
                result = vehicle.getStatus();
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
                if (!vehicle.getDestinationType().equals("None")) {
                    Settlement settle = vehicle.getDestinationSettlement();
                    if (settle != null) {
                        result = settle.getName();
                    }
                    else {
                        result = vehicle.getDestination().getFormattedString();
                    }
                }
            } break;

            case DESTDIST : {
                result = new Integer(new Float(
                    vehicle.getDistanceToDestination()).intValue());
            } break;
            
			case ICE : {
				double ice = vehicle.getInventory().getResourceMass(Resource.ICE);
				result = new Integer((int) ice);
			} break;
        }

        return result;
    }
}
