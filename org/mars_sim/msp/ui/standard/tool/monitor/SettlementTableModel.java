/**
 * Mars Simulation Project
 * SettlementTableModel.java
 * @version 2.84 2008-05-01
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.standard.tool.monitor;

import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.malfunction.Malfunction;
import org.mars_sim.msp.simulation.malfunction.MalfunctionManager;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.Farming;
import org.mars_sim.msp.simulation.vehicle.Vehicle;

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
    
    // Data members
    private UnitManagerListener unitManagerListener;
    private Map<Unit, Map<AmountResource, Integer>> resourceCache;

    /**
     * Constructs a SettlementTableModel model that displays all Settlements
     * in the simulation.
     *
     * @param unitManager Unit manager that holds settlements.
     */
    public SettlementTableModel(UnitManager unitManager) {
        super("All Settlements", " settlements", columnNames, columnTypes);

		setSource(unitManager.getSettlements());
		unitManagerListener = new LocalUnitManagerListener();
        unitManager.addUnitManagerListener(unitManagerListener);
    }

    /**
     * Return the value of a Cell
     * @param rowIndex Row index of the cell.
     * @param columnIndex Column index of the cell.
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object result = null;
        
        if (rowIndex < getUnitNumber()) {
        	Settlement settle = (Settlement)getUnit(rowIndex);
        	BuildingManager bMgr = settle.getBuildingManager();
        	Map<AmountResource, Integer> resourceMap = resourceCache.get(settle);

        	// Invoke the appropriate method, switch is the best solution
        	// althought disliked by some
        	switch (columnIndex) {
            	case NAME : {
            		result = settle.getName();
            	} break;

            	case WATER : {
            		result = (Integer) resourceMap.get(AmountResource.WATER);
            	} break;

            	case FOOD : {
            		result = (Integer) resourceMap.get(AmountResource.FOOD);
            	} break;

            	case OXYGEN : {
            		result = (Integer) resourceMap.get(AmountResource.OXYGEN);
            	} break;

            	case METHANE : {
            		result = (Integer) resourceMap.get(AmountResource.METHANE);
            	} break;

            	case ROCK_SAMPLES : {
            		result = (Integer) resourceMap.get(AmountResource.ROCK_SAMPLES);
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
            		result = (Integer) resourceMap.get(AmountResource.HYDROGEN);
            	} break;

            	case WASTE_WATER : {
            		result = (Integer) resourceMap.get(AmountResource.WASTE_WATER);
            	} break;
            
            	case CO2 : {
            		result = (Integer) resourceMap.get(AmountResource.CARBON_DIOXIDE);
            	} break;
            
            	case ICE : {
            		result = (Integer) resourceMap.get(AmountResource.ICE);
            	} break;
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
		Object target = event.getTarget();
		String eventType = event.getType();

		int columnNum = -1;
		if (eventType.equals(Unit.NAME_EVENT)) columnNum = NAME;
		else if (eventType.equals(Inventory.INVENTORY_STORING_UNIT_EVENT) || 
				eventType.equals(Inventory.INVENTORY_RETRIEVING_UNIT_EVENT)) {
			if (target instanceof Person) columnNum = POPULATION;
			else if (target instanceof Vehicle) columnNum = PARKED;
		}
		else if (eventType.equals(PowerGrid.GENERATED_POWER_EVENT)) columnNum = POWER;
		else if (eventType.equals(BuildingManager.ADD_BUILDING_EVENT)) {
			if (target instanceof Farming) columnNum = GREENHOUSES;
		}
		else if (eventType.equals(Farming.CROP_EVENT)) columnNum = CROPS;
		else if (eventType.equals(MalfunctionManager.MALFUNCTION_EVENT)) columnNum = MALFUNCTION;
		else if (eventType.equals(Inventory.INVENTORY_RESOURCE_EVENT)) {
			int tempColumnNum = -1;
			if (target.equals(AmountResource.OXYGEN)) tempColumnNum = OXYGEN;
			else if (target.equals(AmountResource.HYDROGEN)) tempColumnNum = HYDROGEN;
			else if (target.equals(AmountResource.CARBON_DIOXIDE)) tempColumnNum = CO2;
			else if (target.equals(AmountResource.METHANE)) tempColumnNum = METHANE;
			else if (target.equals(AmountResource.FOOD)) tempColumnNum = FOOD;
			else if (target.equals(AmountResource.WATER)) tempColumnNum = WATER;
			else if (target.equals(AmountResource.WASTE_WATER)) tempColumnNum = WASTE_WATER;
			else if (target.equals(AmountResource.ROCK_SAMPLES)) tempColumnNum = ROCK_SAMPLES;
			else if (target.equals(AmountResource.ICE)) tempColumnNum = ICE;
			if (tempColumnNum > -1) {
				// Only update cell if value as int has changed.
				int currentValue = ((Integer) getValueAt(unitIndex, tempColumnNum)).intValue();
				int newValue = getResourceStored(unit, (AmountResource) target).intValue();
				if (currentValue != newValue) {
					columnNum = tempColumnNum;
					Map<AmountResource, Integer> resourceMap = resourceCache.get(unit);
					resourceMap.put((AmountResource) target, newValue);
				}
			}
		}
			
		if (columnNum > -1) fireTableCellUpdated(unitIndex, columnNum);
	}
    
	/**
	 * Defines the source data from this table
	 */
	private void setSource(Collection source) {
		Iterator<Settlement> iter = source.iterator();
		while(iter.hasNext()) addUnit(iter.next());
	}
	
    /**
     * Add a unit to the model.
     * @param newUnit Unit to add to the model.
     */
    protected void addUnit(Unit newUnit) {
    	if (resourceCache == null) resourceCache = new HashMap<Unit, Map<AmountResource, Integer>>();
    	if (!resourceCache.containsKey(newUnit)) {
    		Map<AmountResource, Integer> resourceMap = new HashMap<AmountResource, Integer>(9);
    		resourceMap.put(AmountResource.FOOD, getResourceStored(newUnit, AmountResource.FOOD));
    		resourceMap.put(AmountResource.OXYGEN, getResourceStored(newUnit, AmountResource.OXYGEN));
    		resourceMap.put(AmountResource.WATER, getResourceStored(newUnit, AmountResource.WATER));
    		resourceMap.put(AmountResource.HYDROGEN, getResourceStored(newUnit, AmountResource.HYDROGEN));
    		resourceMap.put(AmountResource.METHANE, getResourceStored(newUnit, AmountResource.METHANE));
    		resourceMap.put(AmountResource.ROCK_SAMPLES, getResourceStored(newUnit, AmountResource.ROCK_SAMPLES));
    		resourceMap.put(AmountResource.WASTE_WATER, getResourceStored(newUnit, AmountResource.WASTE_WATER));
    		resourceMap.put(AmountResource.ICE, getResourceStored(newUnit, AmountResource.ICE));
    		resourceMap.put(AmountResource.CARBON_DIOXIDE, getResourceStored(newUnit, AmountResource.CARBON_DIOXIDE));
    		resourceCache.put(newUnit, resourceMap);
    	}
    	super.addUnit(newUnit);
    }
    
    /**
     * Remove a unit from the model.
     * @param oldUnit Unit to remove from the model.
     */
    protected void removeUnit(Unit oldUnit) {
    	if (resourceCache == null) resourceCache = new HashMap<Unit, Map<AmountResource, Integer>>();
    	if (resourceCache.containsKey(oldUnit)) {
    		Map<AmountResource, Integer> resourceMap = resourceCache.get(oldUnit);
    		resourceMap.clear();
    		resourceCache.remove(oldUnit);
    	}
    	super.removeUnit(oldUnit);
    }
    
    /**
     * Gets the integer amount of resources stored in a unit.
     * @param unit the unit to check.
     * @param resource the resource to check.
     * @return integer amount of resource.
     */
    private Integer getResourceStored(Unit unit, AmountResource resource) {
    	Integer result = null;	
    	try {
    		Inventory inv = unit.getInventory();
    		result = new Integer((int) inv.getAmountResourceStored(resource));
    	}
    	catch (InventoryException e) {
    		e.printStackTrace(System.err);
    	}
    	return result;
    }
	
    /**
     * Prepares the model for deletion.
     */
    public void destroy() {
    	super.destroy();
    	
    	UnitManager unitManager = Simulation.instance().getUnitManager();
    	unitManager.removeUnitManagerListener(unitManagerListener);
    	unitManagerListener = null;
    	
    	resourceCache.clear();
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
    		String eventType = event.getEventType();
    		if (unit instanceof Settlement) {
    			if (eventType.equals(UnitManagerEvent.ADD_UNIT)) {
    				if (!containsUnit(unit)) addUnit(unit);
    			}
    			else if (eventType.equals(UnitManagerEvent.REMOVE_UNIT)) {
    				if (containsUnit(unit)) removeUnit(unit);
    			}
    		}
    	}
    }
}