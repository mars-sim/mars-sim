/*
 * Mars Simulation Project
 * FoodInventoryTableModel.java
 * @date 2022-07-22
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.tool.monitor;

import com.mars_sim.core.UnitEvent;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.food.Food;
import com.mars_sim.core.food.FoodUtil;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.tools.Msg;

/**
 * This class model how food data is organized and displayed
 * within the Monitor Window for a settlement.
 */
public class FoodInventoryTableModel extends CategoryTableModel<Food>
 {

	protected static final int NUM_DATA_COL = 7;

	/** Names of Columns. */
	private static final ColumnSpec[] COLUMNS;

	private static final int FOOD_COL = 0;
	private static final int TYPE_COL = FOOD_COL+1;
	private static final int SETTLEMENT_COL = TYPE_COL+1;
	protected static final int NUM_INITIAL_COLUMNS = SETTLEMENT_COL+1;

	private static final int DEMAND_COL = SETTLEMENT_COL+1;
	private static final int SUPPLY_COL = DEMAND_COL+1;
	static final int MASS_COL = SUPPLY_COL+1;
	private static final int LOCAL_VP_COL = MASS_COL+1;
	private static final int NATIONAL_VP_COL = LOCAL_VP_COL+1;
	static final int COST_COL = NATIONAL_VP_COL+1;
	static final int PRICE_COL = COST_COL+1;
	static {
		COLUMNS = new ColumnSpec[NUM_INITIAL_COLUMNS + NUM_DATA_COL];

		COLUMNS[FOOD_COL] = new ColumnSpec("Food", String.class);
		COLUMNS[TYPE_COL] =  new ColumnSpec("Type", String.class);
		COLUMNS[SETTLEMENT_COL] =  new ColumnSpec("Settlement", String.class);

		COLUMNS[DEMAND_COL] = new ColumnSpec("Demand", Double.class);
		COLUMNS[SUPPLY_COL] = new ColumnSpec("Supply", Double.class);
		COLUMNS[MASS_COL] = new ColumnSpec("kg", Double.class);
		COLUMNS[LOCAL_VP_COL] = new ColumnSpec("Local VP", Double.class);
		COLUMNS[NATIONAL_VP_COL] = new ColumnSpec("National VP", Double.class);
		COLUMNS[COST_COL] = new ColumnSpec("Cost [$]", Double.class);
		COLUMNS[PRICE_COL] = new ColumnSpec("Price [$]", Double.class);
	}

	/**
	 * Constructor.
	 */
	public FoodInventoryTableModel() {
		super(Msg.getString("FoodInventoryTableModel.tabName"), "FoodInventoryTabModel.foodCounting",
					COLUMNS, FoodUtil.getFoodList());
		
		setCachedColumns(DEMAND_COL, PRICE_COL);
		setSettlementColumn(SETTLEMENT_COL);
	}

	/**
	 * Catch unit update event.
	 *
	 * @param event the unit event.
	 */
	@Override
	public void unitUpdate(UnitEvent event) {
		UnitEventType eventType = event.getType();
		if ((eventType == UnitEventType.FOOD_EVENT)
					&& (event.getTarget() instanceof Food f) 
					&& (event.getSource() instanceof Settlement s)) {
			CategoryKey<Food> row = new CategoryKey<>(s, f);
			// Update the whole row
			entityValueUpdated(row, DEMAND_COL, PRICE_COL);
		}
	}

	@Override
	protected Object getEntityValue(CategoryKey<Food> selectedRow, int columnIndex) {
		Food selectedFood = selectedRow.getCategory();
		Settlement selectedSettlement = selectedRow.getSettlement();

		switch(columnIndex) {
			case FOOD_COL:
				return selectedFood.getName();
			case TYPE_COL:
				return selectedFood.getType();
			case SETTLEMENT_COL:
				return selectedSettlement.getName();
			
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
}


