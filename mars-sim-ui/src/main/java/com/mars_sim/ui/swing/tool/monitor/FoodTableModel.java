/*
 * Mars Simulation Project
 * FoodTableModel.java
 * @date 2026-07-03
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.tool.monitor;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.food.Food;
import com.mars_sim.core.food.FoodUtil;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.resource.ResourceType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;

/**
 * This class model how food data is organized and displayed
 * within the Monitor Window for a settlement.
 */
@SuppressWarnings("serial")
class FoodTableModel extends CategoryTableModel<Food> {

	/** Names of Columns. */
	private static final ColumnSpec[] COLUMNS;

	private static final int FOOD_COL = 0;
	private static final int TYPE_COL = FOOD_COL+1;
	private static final int SETTLEMENT_COL = TYPE_COL+1;
	protected static final int NUM_INITIAL_COLUMNS = SETTLEMENT_COL+1;

	private static final int LOCAL_DEMAND_COL = SETTLEMENT_COL+1;
	private static final int MARKET_DEMAND_COL = LOCAL_DEMAND_COL+1;
	private static final int SUPPLY_COL = MARKET_DEMAND_COL+1;
	static final int MASS_COL = SUPPLY_COL+1;
	private static final int LOCAL_VP_COL = MASS_COL+1;
	private static final int MARKET_VP_COL = LOCAL_VP_COL+1;
	static final int COST_COL = MARKET_VP_COL+1;
	static final int PRICE_COL = COST_COL+1;
	
	private static final int COLUMNCOUNT = PRICE_COL + 1;
	
	protected static final int NUM_DATA_COL = COLUMNCOUNT - LOCAL_DEMAND_COL;
	
	static {
		COLUMNS = new ColumnSpec[NUM_INITIAL_COLUMNS + NUM_DATA_COL];

		COLUMNS[FOOD_COL] = new ColumnSpec("Food", String.class);
		COLUMNS[TYPE_COL] =  new ColumnSpec("Type", String.class);
		COLUMNS[SETTLEMENT_COL] =  new ColumnSpec("Settlement", String.class);

		COLUMNS[LOCAL_DEMAND_COL] = new ColumnSpec("Local Demand", Double.class, ColumnSpec.STYLE_DIGIT3);
		COLUMNS[MARKET_DEMAND_COL] = new ColumnSpec("Market Demand", Double.class, ColumnSpec.STYLE_DIGIT3);
		COLUMNS[SUPPLY_COL] = new ColumnSpec("Supply", Double.class, ColumnSpec.STYLE_DIGIT2);
		COLUMNS[MASS_COL] = new ColumnSpec("kg Mass", Double.class, ColumnSpec.STYLE_DIGIT2);
		COLUMNS[LOCAL_VP_COL] = new ColumnSpec("Local Value", Double.class, ColumnSpec.STYLE_DIGIT3);
		COLUMNS[MARKET_VP_COL] = new ColumnSpec("Market Value", Double.class, ColumnSpec.STYLE_DIGIT3);
		COLUMNS[COST_COL] = new ColumnSpec("Cost", Double.class, ColumnSpec.STYLE_CURRENCY);
		COLUMNS[PRICE_COL] = new ColumnSpec("Price", Double.class, ColumnSpec.STYLE_CURRENCY);
	}

	/**
	 * Constructor.
	 */
	public FoodTableModel() {
		super(Msg.getString("FoodInventoryTableModel.tabName"), "FoodInventoryTabModel.foodCounting",
					COLUMNS, FoodUtil.getFoodList());
		
		setCachedColumns(NUM_INITIAL_COLUMNS, COLUMNCOUNT-1);
		setSettlementColumn(SETTLEMENT_COL);
	}

	/**
	 * Catches unit update event.
	 *
	 * @param event the unit event.
	 */
	@Override
	public void entityUpdate(EntityEvent event) {
		String eventType = event.getType();

		if (event.getSource() instanceof Settlement s) {
			if (event.getTarget() instanceof Good g) {
				// Target is a Good
				Food f = FoodUtil.convertGoodToFood(g);
				CategoryKey<Food> key = new CategoryKey<>(s, f);
			
				switch(eventType) {
					case EntityEventType.DEMAND_EVENT -> entityValueUpdated(key, LOCAL_DEMAND_COL);
					case EntityEventType.MARKET_VALUE_EVENT -> {
						entityValueUpdated(key, MARKET_DEMAND_COL);
						entityValueUpdated(key, MARKET_VP_COL);
					}
					case EntityEventType.SUPPLY_EVENT -> entityValueUpdated(key, SUPPLY_COL);
					case EntityEventType.VALUE_EVENT -> entityValueUpdated(key, LOCAL_VP_COL);
					case EntityEventType.COST_EVENT -> entityValueUpdated(key, COST_COL);
					case EntityEventType.PRICE_EVENT -> entityValueUpdated(key, PRICE_COL);
					default -> entityValueUpdated(key, NUM_INITIAL_COLUMNS, COLUMNCOUNT-1);
				}
			}
			else if (EntityEventType.INVENTORY_RESOURCE_EVENT.equals(eventType)
					&& event.getTarget() instanceof Integer resourceID) {
				Food f = FoodUtil.convertResourceToFood(resourceID);
				CategoryKey<Food> key = new CategoryKey<>(s, f);
				entityValueUpdated(key, MASS_COL);
			}
		}
	}

	@Override
	protected Object getItemValue(CategoryKey<Food> selectedRow, int columnIndex) {
		Food selectedFood = selectedRow.getCategory();
		Settlement selectedSettlement = selectedRow.getSettlement();

		switch(columnIndex) {
			case FOOD_COL:
				return selectedFood.getName();
			case TYPE_COL:
				return selectedFood.getFoodType().getName();
			case SETTLEMENT_COL:
				return selectedSettlement.getName();
			
			case LOCAL_DEMAND_COL:
				return selectedSettlement.getGoodsManager().getDemandScoreWithID(selectedFood.getID());
			case MARKET_DEMAND_COL:
				return selectedSettlement.getGoodsManager().getMarketData(FoodUtil.convertFoodToGood(selectedFood)).getDemand();
			case SUPPLY_COL:
				return selectedSettlement.getGoodsManager().getSupplyScore(selectedFood.getID());
			case MASS_COL:
				return getTotalMass(selectedSettlement, selectedFood);
			case LOCAL_VP_COL:
				return selectedSettlement.getGoodsManager().getGoodValuePoint(selectedFood.getID()); 
			case MARKET_VP_COL:
				return selectedSettlement.getGoodsManager().getMarketData(FoodUtil.convertFoodToGood(selectedFood)).getGoodValue();
			case COST_COL:
				return FoodUtil.convertFoodToGood(selectedFood).getCostOutput();
			case PRICE_COL:
				return selectedSettlement.getGoodsManager().getPricePerItem(selectedFood.getID()); 
			default:
				return null;
		}
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
    	
    	if (ResourceType.getType(id) == ResourceType.AMOUNT_RESOURCE) {
      		// For Amount Resource
    		return settlement.getSpecificAmountResourceStored(id);
    	}
    	
    	return null;
    }
}


