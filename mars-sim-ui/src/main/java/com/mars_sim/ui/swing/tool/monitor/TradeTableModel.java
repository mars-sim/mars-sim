/*
 * Mars Simulation Project
 * TradeTableModel.java
 * @date 2024-07-03
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.monitor;

import com.mars_sim.core.CollectionUtils;
import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitEvent;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.equipment.BinFactory;
import com.mars_sim.core.equipment.BinType;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.VehicleType;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.utils.ColumnSpec;


@SuppressWarnings("serial")
public class TradeTableModel extends CategoryTableModel<Good> {

	/** Names of Columns. */
	private static final ColumnSpec[] COLUMNS;
	private static final int GOOD_COL = 0;
	private static final int CAT_COL = GOOD_COL + 1;
	private static final int TYPE_COL = CAT_COL + 1;
	private static final int SETTLEMENT_COL = TYPE_COL + 1;
	static final int FLATTEN_COL = SETTLEMENT_COL + 1;
	private static final int PROJECTED_COL = FLATTEN_COL + 1;
	private static final int TRADE_COL = PROJECTED_COL + 1;
	private static final int REPAIR_COL = TRADE_COL + 1;
	private static final int DEMAND_COL = REPAIR_COL + 1;
	private static final int SUPPLY_COL = DEMAND_COL + 1;
	static final int QUANTITY_COL = SUPPLY_COL + 1;
	private static final int MASS_COL = QUANTITY_COL + 1;
	private static final int MARKET_COL = MASS_COL + 1;
	private static final int VALUE_COL = MARKET_COL + 1;
	static final int COST_COL = VALUE_COL + 1;
	static final int PRICE_COL = COST_COL + 1;

	static final int NUM_INITIAL_COLUMNS = 4;
	private static final int COLUMNCOUNT = PRICE_COL + 1;

	static {
		COLUMNS = new ColumnSpec[COLUMNCOUNT];
		COLUMNS[GOOD_COL] = new ColumnSpec ("Good", String.class);
		COLUMNS[CAT_COL] = new ColumnSpec ("Category", String.class);
		COLUMNS[TYPE_COL] = new ColumnSpec ("Type", String.class);
		COLUMNS[SETTLEMENT_COL] = new ColumnSpec("Settlement", String.class);
		COLUMNS[FLATTEN_COL] = new ColumnSpec ("Flattened", Number.class);
		COLUMNS[PROJECTED_COL] = new ColumnSpec ("Projected", Double.class);
		COLUMNS[TRADE_COL] = new ColumnSpec ("Trade", Double.class);
		COLUMNS[REPAIR_COL] = new ColumnSpec ("Repair", Double.class);
		COLUMNS[DEMAND_COL] = new ColumnSpec ("Demand", Double.class);
		COLUMNS[SUPPLY_COL] = new ColumnSpec ("Supply", Double.class);
		COLUMNS[QUANTITY_COL] = new ColumnSpec ("Quantity", Double.class);
		COLUMNS[MASS_COL] = new ColumnSpec ("kg Mass", Double.class);
		COLUMNS[MARKET_COL] = new ColumnSpec ("National VP", Double.class);
		COLUMNS[VALUE_COL] = new ColumnSpec ("Local VP", Double.class);
		COLUMNS[COST_COL] = new ColumnSpec ("$ Cost", Double.class);
		COLUMNS[PRICE_COL] = new ColumnSpec ("$ Price", Double.class);
	}

	/**
	 * Constructor 2.
	 * 
	 */
	public TradeTableModel() {
		super(Msg.getString("TradeTableModel.tabName"), "TradeTableModel.counting", COLUMNS,
						GoodsUtil.getGoodsList());
		// Cache the data columns
		setCachedColumns(NUM_INITIAL_COLUMNS, COLUMNCOUNT-1);
		setSettlementColumn(SETTLEMENT_COL);
	}
	
	
	/**
	 * Catches unit update event.
	 *
	 * @param event the unit event.
	 */
	@Override
	public void unitUpdate(UnitEvent event) {
		Unit unit = (Unit) event.getSource();
		UnitEventType eventType = event.getType();
		if ((eventType == UnitEventType.GOODS_VALUE_EVENT
			|| eventType == UnitEventType.FOOD_EVENT)
			&& event.getTarget() instanceof Good g
			&& unit instanceof Settlement s) {
				entityValueUpdated(new CategoryKey<>(s, g), NUM_INITIAL_COLUMNS, COLUMNCOUNT-1);
			}
	}

	/**
	 * get the value for a Good property
	 * @param selectedGood Good selected
	 * @param columnIndex COlumn to get
	 */
	@Override
	protected  Object getEntityValue(CategoryKey<Good> row, int columnIndex) {
		Good selectedGood = row.getCategory();
		Settlement selectedSettlement = row.getSettlement();

		switch(columnIndex) {
			case GOOD_COL:
				return selectedGood.getName();
			case CAT_COL:
				return getGoodCategoryName(selectedGood);
			case TYPE_COL:
				return selectedGood.getGoodType().getName();
			case SETTLEMENT_COL:
				return selectedSettlement.getName();
			case FLATTEN_COL:
				return selectedSettlement.getGoodsManager().getFlattenDemand(selectedGood);
			case PROJECTED_COL:
				return selectedSettlement.getGoodsManager().getProjectedDemand(selectedGood);
			case TRADE_COL:
				return getTrade(selectedSettlement, selectedGood);
			case REPAIR_COL:
				return getRepair(selectedSettlement, selectedGood);
			case DEMAND_COL:
				return selectedSettlement.getGoodsManager().getDemandValue(selectedGood);
			case SUPPLY_COL:
				return selectedSettlement.getGoodsManager().getSupplyValue(selectedGood);
			case QUANTITY_COL:
				return getQuantity(selectedSettlement, selectedGood.getID());
			case MASS_COL:
				return getTotalMass(selectedSettlement, selectedGood);
			case MARKET_COL:
				return selectedGood.getInterMarketGoodValue();
			case VALUE_COL:
				return selectedSettlement.getGoodsManager().getGoodValuePoint(selectedGood.getID());
			case COST_COL:
				return selectedGood.getCostOutput();
			case PRICE_COL:
				return selectedSettlement.getGoodsManager().getPrice(selectedGood);
			default:
				return null;
		}
	}

	/**
	 * Gets the repair demand value of a resource.
	 *
	 * @param settlement
	 * @param id
	 * @return
	 */
    private Object getRepair(Settlement settlement, Good good) {

    	if (good.getID() < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
    		return null;
    	}
    	
    	return settlement.getGoodsManager().getRepairDemand(good);
    }
    
	/**
	 * Gets the trade demand value of a resource.
	 *
	 * @param settlement
	 * @param id
	 * @return
	 */
    private Object getTrade(Settlement settlement, Good good) {

    	double trade = settlement.getGoodsManager().getTradeDemand(good);
    	
    	if (trade == 0)
    		return null;
    	
    	return trade;
    }
    
	/**
	 * Gets the quantity of a resource.
	 *
	 * @param settlement
	 * @param id
	 * @return
	 */
    private Object getQuantity(Settlement settlement, int id) {

    	if (id < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
    		return null;
    	}
    	else if (id < ResourceUtil.FIRST_VEHICLE_RESOURCE_ID) {
    		return settlement.getItemResourceStored(id);
    	}
    	else if (id < ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID) {
    		// For Vehicle
    		return settlement.findNumVehiclesOfType(VehicleType.convertID2Type(id));
    	}
    	else if (id < ResourceUtil.FIRST_ROBOT_RESOURCE_ID) {
    		// For EVA suits 
    		EquipmentType type = EquipmentType.convertID2Type(id);
    		if (type == EquipmentType.EVA_SUIT)
    			return settlement.getNumEVASuit();
    		// For Equipment 
    		return settlement.findNumContainersOfType(type);
    	}
    	else if (id < ResourceUtil.FIRST_BIN_RESOURCE_ID) {
    		// For Robots 
    		return settlement.getNumBots();
    	}
    	else {
    		// For Bins 
    		return settlement.findNumBinsOfType(BinType.convertID2Type(id));
    	}
    }

	/**
	 * Gets the total mass of a good.
	 *
	 * @param settlement
	 * @param good
	 * @return
	 */
    private Object getTotalMass(Settlement settlement, Good good) {
    	int id = good.getID(); 
    	
    	if (id < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
      		// For Amount Resource
    		return Math.round(settlement.getAmountResourceStored(id) * 100.0)/100.0;
    	}
    	else if (id < ResourceUtil.FIRST_VEHICLE_RESOURCE_ID) {
    		// For Item Resource
    		Part p = ItemResourceUtil.findItemResource(id);
    		if (p != null) {
    			return settlement.getItemResourceStored(id) * p.getMassPerItem();
    		}
    			return 0;
    	}
    	else if (id < ResourceUtil.FIRST_EQUIPMENT_RESOURCE_ID) {
    		// For Vehicle
    		VehicleType vehicleType = VehicleType.convertID2Type(id);
    		if (settlement.getAVehicle(vehicleType) == null)
    			return 0;
    		
    		return settlement.findNumVehiclesOfType(vehicleType) * CollectionUtils.getVehicleTypeBaseMass(vehicleType); 
    	}
    	else if (id < ResourceUtil.FIRST_ROBOT_RESOURCE_ID) {
    		// For Equipment   		
    		EquipmentType type = EquipmentType.convertID2Type(id);

    		if (type == EquipmentType.EVA_SUIT)
    			return settlement.getNumEVASuit() * EquipmentFactory.getEquipmentMass(type);
    		
    		return settlement.findNumContainersOfType(type) * EquipmentFactory.getEquipmentMass(type);		
    	}
    	else if (id < ResourceUtil.FIRST_BIN_RESOURCE_ID) {
    		// For Robots   
    		return settlement.getNumBots() * Robot.EMPTY_MASS;
    	}    	
    	else {
    		// For Bins   		
    		BinType type = BinType.convertID2Type(id);
    		return settlement.findNumBinsOfType(type) * BinFactory.getBinMass(type);	
    	}
    }

	/**
	 * Gets the good category name in the internationalized string.
	 *
	 * @param good
	 * @return
	 */
	private static String getGoodCategoryName(Good good) {
		return good.getCategory().getMsgKey();
	}
}
