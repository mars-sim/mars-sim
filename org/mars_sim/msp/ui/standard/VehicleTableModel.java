/**
 * Mars Simulation Project
 * VehicleTableModel.java
 * @version 2.74 2002-01-13
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.*;
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

    // Data members
    private String columnNames[]; // Names of Columns
    private UIProxyManager proxyManager;

    /** Constructs a VehicleTableModel object
     */
    public VehicleTableModel(UIProxyManager proxyManager) {
        super("Vehicle");

        columnNames = new String[COLUMNCOUNT];
        columnNames[NAME] = "Name";
        columnNames[DRIVER] = "Driver";
        columnNames[STATUS] = "Status";
        columnNames[LOCATION] = "Location";
        columnNames[SPEED] = "Speed";
        columnNames[CREW] = "Crew";
        columnNames[DESTINATION] = "Destination";
        columnNames[DESTDIST] = "Dest. Dist.";
        columnNames[FOOD] = "Food";
        columnNames[OXYGEN] = "Oxygen";
        columnNames[WATER] = "Water";
        columnNames[FUEL] = "Fuel";
        this.proxyManager = proxyManager;
    }

    /**
     * Find all the Vehicle units in the simulation and add them to this
     * model
     */
    public void addAll() {
        add(proxyManager.getOrderedVehicleProxies());
    }

    /**
     * Return the number of columns
     * @return column count.
     */
    public int getColumnCount() {
        return COLUMNCOUNT;
    }

    /**
     * Return the name of the column requested.
     * @param columnIndex Index of column.
     * @return name of specified column.
     */
    public String getColumnName(int columnIndex) {
        if ((columnIndex >= 0) && (columnIndex < COLUMNCOUNT)) {
            return columnNames[columnIndex];
        }
        return "Unknown";
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
                Person driver = vehicle.getDriver();
                if (driver != null) {
                    result = driver.getName();
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
