/**
 * Mars Simulation Project
 * SettlementTableModel.java
 * @version 2.74 2002-02-11
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.standard.monitor;

import org.mars_sim.msp.ui.standard.UIProxyManager;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.*;

/**
 * The SettlementTableModel that maintains a list of Settlement objects.
 * It maps key attributes of the Settlement into Columns.
 */
public class SettlementTableModel extends UnitTableModel {

    // Column indexes
    private final static int NAME = 0;           // Person name column
    private final static int POPULATION = 1;
    private final static int GARAGED = 2;
    private final static int PARKED = 3;
    private final static int GROWING = 4;
    private final static int GREENHOUSE = 5;
    private final static int OXYGEN = 6;
    private final static int WATER = 7;
    private final static int FOOD = 8;
    private final static int FUEL = 9;
    private final static int COLUMNCOUNT = 10;    // The number of Columns
    private static String columnNames[];          // Names of Columns
    private static Class columnTypes[];           // Types of columns

    static {
        columnNames = new String[COLUMNCOUNT];
        columnTypes = new Class[COLUMNCOUNT];
        columnNames[NAME] = "Name";
        columnTypes[NAME] = String.class;
        columnNames[GARAGED] = "Garaged";
        columnTypes[GARAGED] = Integer.class;
        columnNames[POPULATION] = "Pop.";
        columnTypes[POPULATION] = Integer.class;
        columnNames[PARKED] = "Parked";
        columnTypes[PARKED] = Integer.class;
        columnNames[GROWING] = "Growing";
        columnTypes[GROWING] = String.class;
        columnNames[GREENHOUSE] = "Greenhouse";
        columnTypes[GREENHOUSE] = String.class;
        columnNames[FOOD] = "Food";
        columnTypes[FOOD] = Integer.class;
        columnNames[OXYGEN] = "Oxygen";
        columnTypes[OXYGEN] = Integer.class;
        columnNames[WATER] = "Water";
        columnTypes[WATER] = Integer.class;
        columnNames[FUEL] = "Fuel";
        columnTypes[FUEL] = Integer.class;
    };

    private UIProxyManager proxyManager;

    /**
     * Constructs a SettlementTableModel model that displays all Settlements
     * from a UIProxymanager.
     *
     * @param proxyManager Proxy manager that holds settlements.
     */
    public SettlementTableModel(UIProxyManager proxyManager) {
        super("Settlement", columnNames, columnTypes);

        this.proxyManager = proxyManager;
        addAll();
    }

    /**
     * Find all the Settlement units in the simulation and add them to this
     * model
     */
    public void addAll() {
        add(proxyManager.getOrderedSettlementProxies());
    }

    /**
     * Return the value of a Cell
     * @param rowIndex Row index of the cell.
     * @param columnIndex Column index of the cell.
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object result = null;
        Settlement settle = (Settlement)getUnit(rowIndex).getUnit();
        FacilityManager fMgr = settle.getFacilityManager();

        // Invoke the appropriate method, switch is the best solution
        // althought disliked by some
        switch (columnIndex) {
            case NAME : {
                result = settle.getName();
            } break;

            case WATER : {
	        double water = settle.getInventory().getResourceMass(Inventory.WATER);
                result = new Integer((int) water);
            } break;

            case FOOD : {
	        double food = settle.getInventory().getResourceMass(Inventory.FOOD);
                result = new Integer((int) food);
            } break;

            case OXYGEN : {
	        double oxygen = settle.getInventory().getResourceMass(Inventory.OXYGEN);
                result = new Integer((int) oxygen);
            } break;

            case FUEL : {
	        double fuel = settle.getInventory().getResourceMass(Inventory.FUEL);
                result = new Integer((int) fuel);
            } break;

            case POPULATION : {
                result = new Integer(settle.getCurrentPopulationNum());
            } break;

            case GARAGED : {
                MaintenanceGarageFacility garage = (MaintenanceGarageFacility)
                                fMgr.getFacility("Maintenance Garage");
                result = new Integer(garage.getTotalSize());
            } break;

            case PARKED : {
                result = new Integer(settle.getParkedVehicleNum());
            } break;

            case GROWING : {
                GreenhouseFacility greenhouse = (GreenhouseFacility)
                                fMgr.getFacility("Greenhouse");
                result = greenhouse.getPhase();
            } break;

            case GREENHOUSE : {
                StringBuffer buffer = new StringBuffer();
                GreenhouseFacility greenhouse = (GreenhouseFacility)
                                fMgr.getFacility("Greenhouse");
                buffer.append("Grow ");
                buffer.append((int)(100F * (greenhouse.getTimeCompleted()
                                        / greenhouse.getGrowthPeriod())));
                buffer.append("% Work ");
                buffer.append((int)(100F * (greenhouse.getWorkCompleted()
                                        / greenhouse.getWorkLoad())));
                buffer.append('%');
                result = buffer.toString();
            } break;

        }

        return result;
    }
}
