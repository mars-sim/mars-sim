/**
 * Mars Simulation Project
 * VehicleTableModel.java
 * @version 2.74 2002-02-09
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.standard.monitor;

import org.mars_sim.msp.ui.standard.UIProxyManager;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.*;

/**
 * The VehicleTableModel that maintains a list of Vehicle objects.
 * It maps key attributes of the Vehicle into Columns.
 */
public class VehicleTableModel extends UnitTableModel {

    // Column indexes
    private final static int  NAME = 0;
    private final static int  DESTINATION = 1;
    private final static int  DESTDIST = 2;
    private final static int  LOCATION = 3;
    private final static int  CREW = 4;
    private final static int  SPEED = 5;
    private final static int  DRIVER = 6;
    private final static int  STATUS = 7;
    private final static int  OXYGEN = 8;
    private final static int  FUEL = 9;
    private final static int  WATER = 10;
    private final static int  FOOD = 11;
    private final static int  COLUMNCOUNT = 12;    // The number of Columns
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
        columnNames[FUEL] = "Fuel";
        columnTypes[FUEL] = Integer.class;
    }

    // Data members
    private UIProxyManager proxyManager;

    /**
     * Constructs a VehicleTableModel object. It creates the list of possible
     * Vehicles from the global proxy manager.
     *
     * @param proxyManager Proxy manager contains displayable Vehicles.
     */
    public VehicleTableModel(UIProxyManager proxyManager) {
        super("Vehicle", columnNames, columnTypes);

        this.proxyManager = proxyManager;

        addAll();
    }

    /**
     * Find all the Vehicle units in the simulation and add them to this
     * model
     */
    public void addAll() {
        add(proxyManager.getOrderedVehicleProxies());
    }

    /**
     * Return the value of a Cell
     * @param rowIndex Row index of the cell.
     * @param columnIndex Column index of the cell.
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object result = null;
        Vehicle vehicle = (Vehicle)getUnit(rowIndex).getUnit();

        // Invoke the appropriate method, switch is the best solution
        // althought disliked by some
        switch (columnIndex) {
            case NAME : {
                result = vehicle.getName();
            } break;

            case CREW : {
                result = new Integer(vehicle.getPassengerNum());
            } break;

            case WATER : {
	        double water = vehicle.getInventory().getResourceMass(Inventory.WATER);
	        result = new Integer((int) water);
            } break;

            case FOOD : {
	        double food = vehicle.getInventory().getResourceMass(Inventory.FOOD);
	        result = new Integer((int) food);
            } break;

            case OXYGEN : {
	        double oxygen = vehicle.getInventory().getResourceMass(Inventory.OXYGEN);
	        result = new Integer((int) oxygen);
            } break;

            case FUEL : {
	        double fuel = vehicle.getInventory().getResourceMass(Inventory.FUEL);
	        result = new Integer((int) fuel);
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

            // Status is a combination of Mechincal failure and maintenance
            case STATUS : {
                StringBuffer status = new StringBuffer();
                status.append(vehicle.getStatus());
                MechanicalFailure failure = vehicle.getMechanicalFailure();
                if ((failure != null) && !failure.isFixed()) {
                    status.append(" ");
                    status.append(failure.getName());
                }
                result = status.toString();
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
        }

        return result;
    }
}
