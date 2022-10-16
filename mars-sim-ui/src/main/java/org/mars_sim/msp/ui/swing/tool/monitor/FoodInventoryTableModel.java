/*
 * Mars Simulation Project
 * FoodInventoryTableModel.java
 * @date 2022-07-22
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.food.Food;
import org.mars_sim.msp.core.food.FoodUtil;
import org.mars_sim.msp.core.goods.Good;
import org.mars_sim.msp.core.goods.GoodsUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * This class model how food data is organized and displayed
 * within the Monitor Window for a settlement.
 */
@SuppressWarnings("serial")
public class FoodInventoryTableModel extends EntityTableModel<Food>
implements UnitListener {
	
	protected static final int NUM_INITIAL_COLUMNS = 2;
	protected static final int NUM_DATA_COL = 7;

	/** Names of Columns. */
	private static final String[] columnNames;
	/** Types of columns. */
	private static final Class<?>[] columnTypes;

	private static final int DEMAND_COL = 2;
	private static final int SUPPLY_COL = 3;
	static final int MASS_COL = 4;
	private static final int LOCAL_VP_COL = 5;
	private static final int NATIONAL_VP_COL = 6;
	static final int COST_COL = 7;
	static final int PRICE_COL = 8;

	
	static {
		columnNames = new String[NUM_INITIAL_COLUMNS + NUM_DATA_COL];
		columnTypes = new Class[NUM_INITIAL_COLUMNS + NUM_DATA_COL];

		columnNames[0] = "Food";
		columnTypes[0] = String.class;
		columnNames[1] =  "Type";
		columnTypes[1] = String.class;

		columnNames[DEMAND_COL] = "Demand";
		columnTypes[DEMAND_COL] = Double.class;
		columnNames[SUPPLY_COL] = "Supply";
		columnTypes[SUPPLY_COL] = Double.class;
		columnNames[MASS_COL] = "kg";
		columnTypes[MASS_COL] = Double.class;
		columnNames[LOCAL_VP_COL] = "Local VP";
		columnTypes[LOCAL_VP_COL] = Double.class;
		columnNames[NATIONAL_VP_COL] = "National VP";
		columnTypes[NATIONAL_VP_COL] = Double.class;
		columnNames[COST_COL] = "Cost [$]";
		columnTypes[COST_COL] = Double.class;
		columnNames[PRICE_COL] = "Price [$]";
		columnTypes[PRICE_COL] = Double.class;
	};

	private Settlement selectedSettlement;
	private boolean monitorSettlement = false;

	/**
	 * Constructor.
	 */
	public FoodInventoryTableModel(Settlement selectedSettlement) {
		super(Msg.getString("FoodInventoryTableModel.tabName"), "FoodInventoryTabModel.foodCounting", columnNames, columnTypes);
		
		setCachedColumns(DEMAND_COL, PRICE_COL);

		setSettlementFilter(selectedSettlement);
	}

	/**
	 * Catch unit update event.
	 *
	 * @param event the unit event.
	 */
	@Override
	public void unitUpdate(UnitEvent event) {
		Unit unit = (Unit) event.getSource();
		UnitEventType eventType = event.getType();
		if (eventType == UnitEventType.FOOD_EVENT) {
			if (event.getTarget() instanceof Good) {
				if (unit.equals(selectedSettlement)) {
					SwingUtilities.invokeLater(new FoodTableUpdater(event));			
				}
			}
		}
	}

	/**
	 * Has this model got a natural order that the model conforms to. If this value
	 * is true, then it implies that the user should not be allowed to order.
	 */
	public boolean getOrdered() {
		return false;
	}

	protected Object getEntityValue(Food selectedFood, int columnIndex) {
		switch(columnIndex) {
			case 0:
				return selectedFood.getName();
			case 1:
				return selectedFood.getType();
			
			case DEMAND_COL:
				return selectedSettlement.getGoodsManager().getAmountDemandValue(selectedFood.getID());
			case SUPPLY_COL:
				return selectedSettlement.getGoodsManager().getSupplyValue(selectedFood.getID());
			case MASS_COL:
				return getTotalMass(selectedSettlement, selectedFood);
			case LOCAL_VP_COL:
				return convertFoodToGood(selectedFood).getInterMarketGoodValue();
			case NATIONAL_VP_COL:
				return selectedSettlement.getGoodsManager().getGoodValuePoint(selectedFood.getID());
			case COST_COL:
				return convertFoodToGood(selectedFood).getCostOutput();
			case PRICE_COL:
				return selectedSettlement.getGoodsManager().getPricePerItem(selectedFood.getID()); 
			default:
				return null;
		}
	}

	/**
	 * Converts food object to good object.
	 * 
	 * @param food
	 * @return
	 */
	private static Good convertFoodToGood(Food food) {
		return GoodsUtil.getGood(food.getID());
	}
	
	/**
	 * Gets the total mass of a food resource.
	 *
	 * @param settlement
	 * @param good
	 * @return
	 */
    private Object getTotalMass(Settlement settlement, Food food) {
    	int id = food.getID(); 
    	
    	if (id < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
      		// For Amount Resource
    		return settlement.getAmountResourceStored(id);
    	}
    	
    	return null;
    }
    
	/**
	 * Set whether the changes to the Entities should be monitor for change. Set up the 
	 * Unitlisteners for the selected Settlement where Food comes from for the table.
	 * @param activate 
	 */
    public void setMonitorEntites(boolean activate) {
		if (activate != monitorSettlement) {
			if (activate) {
				selectedSettlement.addUnitListener(this);
			}
			else {
				selectedSettlement.removeUnitListener(this);
			}
			monitorSettlement = activate;
		}
	}

	/**
	 * Prepares the model for deletion.
	 */
	@Override
	public void destroy() {
		super.destroy();

		// Remove as listener for all settlements.
		selectedSettlement.removeUnitListener(this);
	}
	
	/**
	 * Inner class for updating food table.
	 */
	private class FoodTableUpdater implements Runnable {

		private UnitEvent event;

		private FoodTableUpdater(UnitEvent event) {
			this.event = event;
		}

		public void run() {
			entityValueUpdated((Food)event.getTarget(), DEMAND_COL, PRICE_COL);
		}
	}

	/**
	 * Set the Settlement filter
	 * @param filter Settlement
	 */
    public boolean setSettlementFilter(Settlement filter) {
		if (selectedSettlement != null) {
			selectedSettlement.removeUnitListener(this);
		}

		// Initialize settlements.
		selectedSettlement = filter;	

		// Initialize goods list.
		resetEntities(FoodUtil.getFoodList());
			
		// Add table as listener to each settlement.
		if (monitorSettlement) {
			selectedSettlement.addUnitListener(this);
		}

		return true;
    }
}


