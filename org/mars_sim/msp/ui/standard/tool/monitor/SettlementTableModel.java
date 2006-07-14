/**
 * Mars Simulation Project
 * SettlementTableModel.java
 * @version 2.77 2004-08-11
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.standard.tool.monitor;

import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.malfunction.Malfunction;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.Farming;

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
    private final static int ICE = 15;
    private final static int COLUMNCOUNT = 16;   // The number of Columns
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
        columnNames[ICE] = "Ice";
        columnTypes[ICE] = Integer.class;
    };

    /**
     * Constructs a SettlementTableModel model that displays all Settlements
     * in the simulation.
     *
     * @param unitManager Unit manager that holds settlements.
     */
    public SettlementTableModel(UnitManager unitManager) {
        super("All Settlement", " settlements", columnNames, columnTypes);

		setSource(unitManager.getSettlements());
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
            	try {
            		double water = settle.getInventory().getAmountResourceStored(AmountResource.WATER);
            		result = new Integer((int) water);
            	}
            	catch (InventoryException e) {}
            } break;

            case FOOD : {
            	try {
            		double food = settle.getInventory().getAmountResourceStored(AmountResource.FOOD);
            		result = new Integer((int) food);
            	}
            	catch (InventoryException e) {}
            } break;

            case OXYGEN : {
            	try {
            		double oxygen = settle.getInventory().getAmountResourceStored(AmountResource.OXYGEN);
            		result = new Integer((int) oxygen);
            	}
            	catch (InventoryException e) {}
            } break;

            case METHANE : {
            	try {
            		double methane = settle.getInventory().getAmountResourceStored(AmountResource.METHANE);
            		result = new Integer((int) methane);
            	}
            	catch (InventoryException e) {}
            } break;

            case ROCK_SAMPLES : {
            	try {
            		double rockSamples = settle.getInventory().getAmountResourceStored(AmountResource.ROCK_SAMPLES);
            		result = new Integer((int) rockSamples);
            	}
            	catch (InventoryException e) {}
	        } break;

            case MALFUNCTION: {
                int severity = 0;
                Malfunction malfunction = settle.getMalfunctionManager().getMostSeriousMalfunction();
                if (malfunction != null) severity = malfunction.getSeverity();
                Iterator i = settle.getBuildingManager().getBuildings().iterator();
                while (i.hasNext()) {
                    Building building = (Building) i.next();
                    Malfunction tempMalfunction = building.getMalfunctionManager().getMostSeriousMalfunction();
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
                int greenhouses = bMgr.getBuildings(Farming.NAME).size();
                result = new Integer(greenhouses);
            } break;

            case CROPS : {
                int crops = 0;
                List greenhouses = bMgr.getBuildings(Farming.NAME);
                Iterator i = greenhouses.iterator();
                while (i.hasNext()) {
                	try {
                		Building greenhouse = (Building) i.next();
                    	Farming farm = (Farming) greenhouse.getFunction(Farming.NAME);
                    	crops += farm.getCrops().size();
                	}
                	catch (Exception e) {}
                }
                
                result = new Integer(crops);
            } break;

            case HYDROGEN : {
            	try {
            		double hydrogen = settle.getInventory().getAmountResourceStored(AmountResource.HYDROGEN);
            		result = new Integer((int) hydrogen);
            	}
            	catch (InventoryException e) {}
            } break;

            case WASTE_WATER : {
            	try {
            		double wasteWater = settle.getInventory().getAmountResourceStored(AmountResource.WASTE_WATER);
            		result = new Integer((int) wasteWater);
            	}
            	catch (InventoryException e) {}
            } break;
            
            case CO2 : {
            	try {
            		double co2 = settle.getInventory().getAmountResourceStored(AmountResource.CARBON_DIOXIDE);
            		result = new Integer((int) co2);
            	}
            	catch (InventoryException e) {}
            } break;
            
            case ICE : {
            	try {
            		double ice = settle.getInventory().getAmountResourceStored(AmountResource.ICE);
            		result = new Integer((int) ice);
            	}
            	catch (InventoryException e) {}
            } break;
        }

        return result;
    }
    
	/**
	 * Defines the source data from this table
	 */
	private void setSource(SettlementCollection source) {
		SettlementIterator iter = source.iterator();
		while(iter.hasNext()) {
			add(iter.next());
		}
	}
	
	/**
	 * The Model should be updated to reflect any changes in the underlying
	 * data.
	 * @return A status string for the contents of the model.
	 */
	public String update() {
		return update(Simulation.instance().getUnitManager().getSettlements());
	}
}