/**
 * Mars Simulation Project
 * SettlementTableModel.java
 * @version 2.75 2003-02-05
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.standard.monitor;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.malfunction.Malfunction;
import java.util.Iterator;

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
    private final static int GREENHOUSE = 4;
    private final static int GREEN_GROW = 5;
    private final static int GREEN_WORK = 6;
    private final static int MALFUNCTION = 7;
    private final static int OXYGEN = 8;
    private final static int WATER = 9;
    private final static int FOOD = 10;
    private final static int FUEL = 11;
    private final static int ROCK_SAMPLES = 12;
    private final static int COLUMNCOUNT = 13;    // The number of Columns
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
        columnNames[GREENHOUSE] = "Greenhouse";
        columnTypes[GREENHOUSE] = String.class;
        columnNames[GREEN_GROW] = "Growing %";
        columnTypes[GREEN_GROW] = Integer.class;
        columnNames[GREEN_WORK] = "Work %";
        columnTypes[GREEN_WORK] = Integer.class;
        columnNames[MALFUNCTION] = "Malfunction";
        columnTypes[MALFUNCTION] = String.class;
        columnNames[FOOD] = "Food";
        columnTypes[FOOD] = Integer.class;
        columnNames[OXYGEN] = "Oxygen";
        columnTypes[OXYGEN] = Integer.class;
        columnNames[WATER] = "Water";
        columnTypes[WATER] = Integer.class;
        columnNames[FUEL] = "Fuel";
        columnTypes[FUEL] = Integer.class;
        columnNames[ROCK_SAMPLES] = "Rock Samples";
        columnTypes[ROCK_SAMPLES] = Integer.class;
    };

    /**
     * Constructs a SettlementTableModel model that displays all Settlements
     * from a UIProxymanager.
     *
     * @param unitManager Unit manager that holds settlements.
     */
    public SettlementTableModel(UnitManager unitManager) {
        super("All Settlement", " settlements", columnNames, columnTypes);

        SettlementIterator iter = unitManager.getSettlements().sortByName().iterator();
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
        Settlement settle = (Settlement)getUnit(rowIndex);
        FacilityManager fMgr = settle.getFacilityManager();

        // Invoke the appropriate method, switch is the best solution
        // althought disliked by some
        switch (columnIndex) {
            case NAME : {
                result = settle.getName();
            } break;

            case WATER : {
	        double water = settle.getInventory().getResourceMass(Resource.WATER);
                result = new Integer((int) water);
            } break;

            case FOOD : {
	        double food = settle.getInventory().getResourceMass(Resource.FOOD);
                result = new Integer((int) food);
            } break;

            case OXYGEN : {
	        double oxygen = settle.getInventory().getResourceMass(Resource.OXYGEN);
                result = new Integer((int) oxygen);
            } break;

            case FUEL : {
	        double fuel = settle.getInventory().getResourceMass(Resource.FUEL);
                result = new Integer((int) fuel);
            } break;

            case ROCK_SAMPLES : {
                double rockSamples = settle.getInventory().getResourceMass(Resource.ROCK_SAMPLES);
		        result = new Integer((int) rockSamples);
	        } break;

            case MALFUNCTION: {
                int severity = 0;
                Malfunction malfunction = settle.getMalfunctionManager().getMostSeriousMalfunction();
		if (malfunction != null) severity = malfunction.getSeverity();
		Iterator i = settle.getFacilityManager().getFacilities();
		while (i.hasNext()) {
                    Facility facility = (Facility) i.next();
		    Malfunction tempMalfunction = facility.getMalfunctionManager().getMostSeriousMalfunction();
		    if ((tempMalfunction != null) && (tempMalfunction.getSeverity() > severity)) {
                        malfunction = tempMalfunction;
			severity = tempMalfunction.getSeverity();
		    }
		}
                if (malfunction != null) result = malfunction.getName();
            } break;

            case POPULATION : {
                result = new Integer(settle.getCurrentPopulationNum());
            } break;

            case GARAGED : {
                MaintenanceGarage garage = (MaintenanceGarage) fMgr.getFacility("Maintenance Garage");
                result = new Integer((int) garage.getVehicleCapacity());
            } break;

            case PARKED : {
                result = new Integer(settle.getParkedVehicleNum());
            } break;

            case GREENHOUSE : {
                Greenhouse greenhouse = (Greenhouse) fMgr.getFacility("Greenhouse");
                result = greenhouse.getPhase();
            } break;

            case GREEN_GROW : {
                Greenhouse greenhouse = (Greenhouse) fMgr.getFacility("Greenhouse");
                result = new Integer((int)(100F * (greenhouse.getTimeCompleted()
                                        / greenhouse.getGrowthPeriod())));
            } break;

            case GREEN_WORK : {
                Greenhouse greenhouse = (Greenhouse) fMgr.getFacility("Greenhouse");

                result = new Integer((int)(100F * (greenhouse.getWorkCompleted()
                                        / greenhouse.getWorkLoad())));
            } break;

        }

        return result;
    }
}
