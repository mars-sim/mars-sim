/*
 * Mars Simulation Project
 * TradeTableModel.java
 * @date 2022-07-22
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.monitor;

import com.mars_sim.core.CollectionUtils;
import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitEvent;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitListener;
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


public class TradeTableModel extends EntityTableModel<Good>
implements UnitListener {

	static final int NUM_INITIAL_COLUMNS = 3;
	private static final int NUM_DATA_COL = 8;
	private static final int COLUMNCOUNT = NUM_INITIAL_COLUMNS + NUM_DATA_COL;

	/** Names of Columns. */
	private static final ColumnSpec[] COLUMNS;


	private static final int DEMAND_COL = 3;
	private static final int SUPPLY_COL = 4;
	static final int QUANTITY_COL = 5;
	private static final int MASS_COL = 6;
	private static final int MARKET_COL = 7;
	private static final int VALUE_COL = 8;
	static final int COST_COL = 9;
	static final int PRICE_COL = 10;

	static {
		COLUMNS = new ColumnSpec[NUM_INITIAL_COLUMNS + NUM_DATA_COL];
		COLUMNS[0] = new ColumnSpec ("Good", String.class);
		COLUMNS[1] = new ColumnSpec ("Category", String.class);
		COLUMNS[2] = new ColumnSpec ("Type", String.class);

		COLUMNS[DEMAND_COL] = new ColumnSpec ("Demand", Double.class);
		COLUMNS[SUPPLY_COL] = new ColumnSpec ("Supply", Double.class);
		COLUMNS[QUANTITY_COL] = new ColumnSpec ("Quantity", Double.class);
		COLUMNS[MASS_COL] = new ColumnSpec ("Tot Mass [kg]", Double.class);
		COLUMNS[MARKET_COL] = new ColumnSpec ("National VP", Double.class);
		COLUMNS[VALUE_COL] = new ColumnSpec ("Local VP", Double.class);
		COLUMNS[COST_COL] = new ColumnSpec ("Cost [$]", Double.class);
		COLUMNS[PRICE_COL] = new ColumnSpec ("Price [$]", Double.class);
	};

	// Data members
	private Settlement selectedSettlement;
	private boolean monitorSettlement = false;

	/**
	 * Constructor 2.
	 * 
	 * @param selectedSettlement
	 * @param window
	 */
	public TradeTableModel(Settlement selectedSettlement) {
		super(Msg.getString("TradeTableModel.tabName"), "TradeTableModel.counting",COLUMNS);

		// Cache the data columns
		setCachedColumns(NUM_INITIAL_COLUMNS, COLUMNCOUNT-1);

		this.selectedSettlement = selectedSettlement;

		setSettlementFilter(selectedSettlement);
	}
	
	@Override
	public boolean setSettlementFilter(Settlement filter) {
		if (selectedSettlement != null) {
			selectedSettlement.removeUnitListener(this);
		}

		// Initialize settlements.
		selectedSettlement = filter;

		// Initialize goods list.
		resetEntities(GoodsUtil.getGoodsList());

		// Add table as listener to each settlement.
		if (monitorSettlement) {
			selectedSettlement.addUnitListener(this);
		}

		return true;
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
			&& event.getTarget() instanceof Good 
			&& unit.equals(selectedSettlement)) {
				entityValueUpdated((Good)event.getTarget(), NUM_INITIAL_COLUMNS, COLUMNCOUNT-1);
			}
	}

	/**
	 * get the value for a Good property
	 * @param selectedGood Good selected
	 * @param columnIndex COlumn to get
	 */
	@Override
	protected  Object getEntityValue(Good selectedGood, int columnIndex) {
		switch(columnIndex) {
			case 0:
				return selectedGood.getName();
			case 1:
				return getGoodCategoryName(selectedGood);
			case 2:
				return selectedGood.getGoodType().getName();
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

	/**
	 * Prepares the model for deletion.
	 */
	@Override
	public void destroy() {
		super.destroy();

		// Remove as listener for all settlements.
		selectedSettlement.removeUnitListener(this);

		selectedSettlement = null;
	}
}