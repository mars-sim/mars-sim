/**
 * Mars Simulation Project
 * SettlementTableModel.java
 * @version 2.75 2003-11-13
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.standard.tool.monitor;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.structure.building.BuildingManager;
import org.mars_sim.msp.simulation.structure.building.function.Farming;
import org.mars_sim.msp.simulation.malfunction.Malfunction;
import java.util.*;

/**
 * The SettlementTableModel that maintains a list of Settlement objects.
 * It maps key attributes of the Settlement into Columns.
 */
public class SettlementTableModel extends UnitTableModel {

    // Column indexes
    private final static int NAME = 0;           // Person name column
    private final static int POPULATION = 1;
    private final static int PARKED = 2;
    private final static int POWER = 3;
    private final static int GREENHOUSES = 4;
    private final static int CROPS = 5;
    private final static int MALFUNCTION = 6;
    private final static int OXYGEN = 7;
    private final static int HYDROGEN = 8;
    private final static int CO2 = 9;
    private final static int METHANE = 10;
    private final static int FOOD = 11;
    private final static int WATER = 12;
    private final static int WASTE_WATER = 13;
    private final static int ROCK_SAMPLES = 14;
    private final static int COLUMNCOUNT = 15;   // The number of Columns
    private static String columnNames[];          // Names of Columns
    private static Class columnTypes[];           // Types of columns

    static {
        columnNames = new String[COLUMNCOUNT];
        columnTypes = new Class[COLUMNCOUNT];
        columnNames[NAME] = "Name";
        columnTypes[NAME] = String.class;
        columnNames[POPULATION] = "Population";
        columnTypes[POPULATION] = Integer.class;
        columnNames[PARKED] = "Parked Vehicles";
        columnTypes[PARKED] = Integer.class;
        columnNames[POWER] = "Power (kW)";
        columnTypes[POWER] = Integer.class;
        columnNames[GREENHOUSES] = "Greenhouses";
        columnTypes[GREENHOUSES] = Integer.class;
        columnNames[CROPS] = "Crops";
        columnTypes[CROPS] = Integer.class;
        columnNames[MALFUNCTION] = "Malfunction";
        columnTypes[MALFUNCTION] = String.class;
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
        columnNames[HYDROGEN] = "Hydrogen";
        columnTypes[HYDROGEN] = Integer.class;
        columnNames[WASTE_WATER] = "Waste Water";
        columnTypes[WASTE_WATER] = Integer.class;
        columnNames[CO2] = "CO2";
        columnTypes[CO2] = Integer.class;
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
        BuildingManager bMgr = settle.getBuildingManager();

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

            case METHANE : {
                double methane = settle.getInventory().getResourceMass(Resource.METHANE);
                result = new Integer((int) methane);
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

            case PARKED : {
                result = new Integer(settle.getParkedVehicleNum());
            } break;
            
            case POWER : {
                result = new Integer((int) settle.getPowerGrid().getGeneratedPower());
            } break;

            case GREENHOUSES : {
                int greenhouses = bMgr.getBuildings(Farming.class).size();
                result = new Integer(greenhouses);
            } break;

            case CROPS : {
                int crops = 0;
                List greenhouses = bMgr.getBuildings(Farming.class);
                Iterator i = greenhouses.iterator();
                while (i.hasNext()) {
                    Farming farm = (Farming) i.next();
                    crops += farm.getCrops().size();
                }
                
                result = new Integer(crops);
            } break;

            case HYDROGEN : {
                double hydrogen = settle.getInventory().getResourceMass(Resource.HYDROGEN);
                result = new Integer((int) hydrogen);
            } break;

            case WASTE_WATER : {
                double wasteWater = settle.getInventory().getResourceMass(Resource.WASTE_WATER);
                result = new Integer((int) wasteWater);
            } break;
            
            case CO2 : {
                double co2 = settle.getInventory().getResourceMass(Resource.CARBON_DIOXIDE);
                result = new Integer((int) co2);
            } break;
        }

        return result;
    }
}
