/*
 * Mars Simulation Project
 * FoodInventoryTableModel.java
 * @date 2022-07-22
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

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
import org.mars_sim.tools.Msg;

/**
 * This class model how food data is organized and displayed
 * within the Monitor Window for a settlement.
 */
public class FoodInventoryTableModel extends EntityTableModel<Food>
implements UnitListener {
	
	protected static final int NUM_INITIAL_COLUMNS = 2;
	protected static final int NUM_DATA_COL = 7;

	/** Names of Columns. */
	private static final ColumnSpec[] COLUMNS;

	private static final int DEMAND_COL = 2;
	private static final int SUPPLY_COL = 3;
	static final int MASS_COL = 4;
	private static final int LOCAL_VP_COL = 5;
	private static final int NATIONAL_VP_COL = 6;
	static final int COST_COL = 7;
	static final int PRICE_COL = 8;

	
	static {
		COLUMNS = new ColumnSpec[NUM_INITIAL_COLUMNS + NUM_DATA_COL];

		COLUMNS[0] = new ColumnSpec("Food", String.class);
		COLUMNS[1] =  new ColumnSpec("Type", String.class);

		COLUMNS[DEMAND_COL] = new ColumnSpec("Demand", Double.class);
		COLUMNS[SUPPLY_COL] = new ColumnSpec("Supply", Double.class);
		COLUMNS[MASS_COL] = new ColumnSpec("kg", Double.class);
		COLUMNS[LOCAL_VP_COL] = new ColumnSpec("Local VP", Double.class);
		COLUMNS[NATIONAL_VP_COL] = new ColumnSpec("National VP", Double.class);
		COLUMNS[COST_COL] = new ColumnSpec("Cost [$]", Double.class);
		COLUMNS[PRICE_COL] = new ColumnSpec("Price [$]", Double.class);
	}

	private Settlement selectedSettlement;
	private boolean monitorSettlement = false;

	/**
	 * Constructor.
	 */
	public FoodInventoryTableModel(Settlement selectedSettlement) {
		super(Msg.getString("FoodInventoryTableModel.tabName"), "FoodInventoryTabModel.foodCounting",
					COLUMNS);
		
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
		if ((eventType == UnitEventType.FOOD_EVENT)
					&& (event.getTarget() instanceof Food f) && unit.equals(selectedSettlement)) {
			// Update the whole row
			entityValueUpdated(f, DEMAND_COL, PRICE_COL);
		}
	}

	@Override
	protected Object getEntityValue(Food selectedFood, int columnIndex) {
		switch(columnIndex) {
			case 0:
				return selectedFood.getName();
			case 1:
				return selectedFood.getType();
			
			case DEMAND_COL:
				return selectedSettlement.getGoodsManager().getDemandValueWithID(selectedFood.getID());
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


