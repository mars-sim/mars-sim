/**
 * Mars Simulation Project
 * SettlementTableModel.java
 * @version 2.73 2001-11-11
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.task.*;

/**
 * The SettlementTableModel that maintains a list of Settlement objects.
 * It maps key attributes of the Settlement into Columns.
 */
public class SettlementTableModel extends UnitTableModel {

    // Column indexes
    private final static int  NAME = 0;           // Person name column
    private final static int  POPULATION = 1;
    private final static int  GARAGED = 2;
    private final static int  PARKED = 3;
    private final static int  GROWING = 4;
    private final static int  GREENHOUSE = 5;
    private final static int  OXYGEN = 6;
    private final static int  WATER = 7;
    private final static int  FOOD = 8;
    private final static int  PARTS = 9;
    private final static int  FUEL = 10;

    private final static int  COLUMNCOUNT = 11;    // The number of Columns

    // Data members
    private String columnNames[]; // Names of Columns
    private UIProxyManager proxyManager;

    /** Constructs a SettlementTableModel object
     */
    public SettlementTableModel(UIProxyManager proxyManager) {
        super("Settlement");

        columnNames = new String[COLUMNCOUNT];
        columnNames[NAME] = "Name";
        columnNames[GARAGED] = "Garaged";
        columnNames[POPULATION] = "Pop.";
        columnNames[PARKED] = "Parked";
        columnNames[GROWING] = "Growing";
        columnNames[GREENHOUSE] = "Greenhouse";
        columnNames[FOOD] = "Food";
        columnNames[OXYGEN] = "Oxygen";
        columnNames[WATER] = "Water";
        columnNames[FUEL] = "Fuel";
        columnNames[PARTS] = "Part";

        this.proxyManager = proxyManager;
    }

    /**
     * Find all the Settlement units in the simulation and add them to this
     * model
     */
    public void addAll() {
        add(proxyManager.getOrderedSettlementProxies());
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
        Settlement settle = (Settlement)getUnit(rowIndex).getUnit();
        FacilityManager fMgr = settle.getFacilityManager();
        StoreroomFacility store = (StoreroomFacility)
                                fMgr.getFacility("Storerooms");

        // Invoke the appropriate method, switch is the best solution
        // althought disliked by some
        switch (columnIndex) {
            case NAME : {
                result = settle.getName();
            } break;

            case WATER : {
                result = new Integer(new Float(store.getWaterStores()).intValue());
            } break;

            case FOOD : {
                result = new Integer(new Float(store.getFoodStores()).intValue());
            } break;

            case OXYGEN : {
                result = new Integer(new Float(store.getOxygenStores()).intValue());
            } break;

            case FUEL : {
                result = new Integer(new Float(store.getFuelStores()).intValue());
            } break;

            case PARTS : {
                result = new Integer(new Float(store.getPartsStores()).intValue());
            } break;

            case POPULATION : {
                StringBuffer buffer = new StringBuffer();
                buffer.append(settle.getCurrentPopulation());
                buffer.append('/');
                buffer.append(settle.getPopulationCapacity());
                result = buffer.toString();
            } break;

            case GARAGED : {
                MaintenanceGarageFacility garage = (MaintenanceGarageFacility)
                                fMgr.getFacility("Maintenance Garage");
                StringBuffer buffer = new StringBuffer();
                buffer.append(garage.getTotalSize());
                buffer.append('/');
                buffer.append(garage.getMaxSizeCapacity());
                result = buffer.toString();
            } break;

            case PARKED : {
                result = new Integer(settle.getVehicleNum());
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
