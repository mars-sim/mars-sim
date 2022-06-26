/*
 * Mars Simulation Project
 * TradeTableModel.java
 * @date 2022-06-16
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.goods.Good;
import org.mars_sim.msp.core.goods.GoodsUtil;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.VehicleType;
import org.mars_sim.msp.ui.swing.tool.Conversion;

@SuppressWarnings("serial")
public class TradeTableModel extends AbstractTableModel
implements UnitListener, MonitorModel, UnitManagerListener {

	private static final String TRADE_GOODS = "Trade Goods";
	
	private static final String GOOD_COL = "Good - ";
	private static final String CATEGORY_COL = "Category";
	private static final String TYPE_COL = "Type";

	private static final String DEMAND_COL = "Demand";
	private static final String SUPPLY_COL = "Supply";
	private static final String QUANTITY_COL = "Quantity";
	private static final String MASS_COL = "Tot Mass [kg]";
	private static final String VALUE_COL = "Value";
	private static final String PRICE_COL = "Price [$]";
	private static final String COST_COL = "Cost [$]";
	
	private static final String ONE_SPACE = " ";

	protected static final int NUM_INITIAL_COLUMNS = 3;
	protected static final int NUM_DATA_COL = 7;

	// Data members
	private GameMode mode = GameManager.getGameMode();
	
	private List<Good> goodsList;
	private List<Settlement> settlements = new ArrayList<>();
	
	protected static UnitManager unitManager = Simulation.instance().getUnitManager();

	/**
	 * Constructor 2.
	 */
	public TradeTableModel(Settlement selectedSettlement) {
		
		// Initialize goods list.
		goodsList = GoodsUtil.getGoodsList();
		
		// Initialize settlements.
		settlements.add(selectedSettlement);

		// Add table as listener to each settlement.
		Iterator<Settlement> i = settlements.iterator();
		while (i.hasNext()) i.next().addUnitListener(this);

		// Add as unit manager listener.
		unitManager.addUnitManagerListener(this);
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
		if (eventType == UnitEventType.GOODS_VALUE_EVENT
				|| eventType == UnitEventType.FOOD_EVENT) {
			if (event.getTarget() instanceof Good && unit instanceof Settlement) {
				if ((mode == GameMode.COMMAND && unit.getName().equalsIgnoreCase(settlements.get(0).getName()))
						|| unit.getName().equalsIgnoreCase(settlements.get(0).getName())) {
					SwingUtilities.invokeLater(new TradeTableUpdater(event));			
				}
			}
		}
	}


	/**
	 * Gets the model count string.
	 */
	@Override
	public String getCountString() {
		return new StringBuilder(ONE_SPACE + goodsList.size() + ONE_SPACE + TRADE_GOODS).toString();
	}

	/**
	 * Gets the name of this model. The name will be a description helping
	 * the user understand the contents.
	 *
	 * @return Descriptive name.
	 */
	@Override
	public String getName() {
		return Msg.getString("TradeTableModel.tabName"); //$NON-NLS-1$
	}

	/**
	 * Returns the object at the specified row indexes.
	 *
	 * @param row Index of the row object.
	 * @return Object at the specified row.
	 */
	public Object getObject(int row) {
		return goodsList.get(row);
	}

	/**
	 * Has this model got a natural order that the model conforms to.
	 *
	 * @return If true, it implies that the user should not be allowed to order.
	 */
	public boolean getOrdered() {
		return false;
	}

	/**
	 * Returns the name of the column requested.
	 *
	 * @param columnIndex Index of column.
	 * @return name of specified column.
	 */
	public String getColumnName(int columnIndex) {
		if (columnIndex == 0) return GOOD_COL + settlements.get(0).getName();
		else if (columnIndex == 1) return CATEGORY_COL;
		else if (columnIndex == 2) return TYPE_COL;
		else {
			int col = columnIndex - NUM_INITIAL_COLUMNS;
			int r = col % NUM_DATA_COL;
			if (r == 0)
				return DEMAND_COL;
			else if (r == 1)
				return SUPPLY_COL;
			else if (r == 2)
				return QUANTITY_COL;
			else if (r == 3)
				return MASS_COL;
			else if (r == 4)
				return VALUE_COL;
			else if (r == 5)
				return COST_COL;
			else
				return PRICE_COL;
		}
	}

	/**
	 * Returns the type of the column requested.
	 * 
	 * @param columnIndex Index of column.
	 * @return Class of specified column.
	 */
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex < NUM_INITIAL_COLUMNS)
			return String.class;
		else {
			return Double.class;
		}
	}

	public int getColumnCount() {
		return settlements.size() * NUM_DATA_COL + NUM_INITIAL_COLUMNS;
	}

	public int getRowCount() {
		return goodsList.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return Conversion.capitalize(goodsList.get(rowIndex).getName());
		}

		else if (columnIndex == 1) {
			return Conversion.capitalize(getGoodCategoryName(goodsList.get(rowIndex)));
		}

		else if (columnIndex == 2) {
			return Conversion.capitalize(GoodsUtil.getGoodType(goodsList.get(rowIndex)).getName());
		}

		else {
			int col = columnIndex - NUM_INITIAL_COLUMNS;
			int r = col % NUM_DATA_COL;
			if (r == 0)
				return settlements.get(0).getGoodsManager().getDemandValue(goodsList.get(rowIndex));
			else if (r == 1)
				return settlements.get(0).getGoodsManager().getSupplyValue(goodsList.get(rowIndex));
			else if (r == 2)
				return getQuantity(settlements.get(0), goodsList.get(rowIndex).getID());
			else if (r == 3)
				return getTotalMass(settlements.get(0), goodsList.get(rowIndex));
			else if (r == 4)
				return settlements.get(0).getGoodsManager().getGoodValuePerItem(goodsList.get(rowIndex).getID());
			else if (r == 5)
				return Math.round(goodsList.get(rowIndex).getCostOutput()*100.0)/100.0;
			else
				return Math.round(settlements.get(0).getGoodsManager().getPricePerItem(goodsList.get(rowIndex))*100.0)/100.0; 
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
    		return settlement.findNumVehiclesOfType(VehicleType.convertID2Type(id));
    	}
    	else if (id < ResourceUtil.FIRST_ROBOT_RESOURCE_ID) {
    		EquipmentType type = EquipmentType.convertID2Type(id);
    		if (type == EquipmentType.EVA_SUIT)
    			return settlement.getNumEVASuit();
    		
    		return settlement.findNumContainersOfType(type);
    	}
    	else {
    		return settlement.getInitialNumOfRobots();
    	}
    }

	/**
	 * Gets the total mass of a good.
	 *
	 * @param settlement
	 * @param id
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
    		return settlement.getItemResourceStored(id) * ItemResourceUtil.findItemResource(id).getMassPerItem();
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
    			return settlement.getNumEVASuit() * EVASuit.emptyMass;
    		
    		return settlement.findNumContainersOfType(type) * EquipmentFactory.getEquipmentMass(type);		
    	}
    	else {
    		return settlement.getInitialNumOfRobots() * Robot.EMPTY_MASS;
    	}
    }

	/**
	 * Gets the good category name in the internationalized string.
	 *
	 * @param good
	 * @return
	 */
	public String getGoodCategoryName(Good good) {
		return good.getCategory().getMsgKey();
	}

	/**
	 * Prepares the model for deletion.
	 */
	@Override
	public void destroy() {
		// Remove as listener for all settlements.
		Iterator<Settlement> i = settlements.iterator();
		while (i.hasNext()) i.next().removeUnitListener(this);

		// Remove as listener to unit manager.
		unitManager.removeUnitManagerListener(this);
		
		goodsList = null;
//		settlements = null;
		unitManager = null;
	}
	
	/**
	 * Inner class for updating goods table.
	 */
	private class TradeTableUpdater implements Runnable {

		private UnitEvent event;

		private TradeTableUpdater(UnitEvent event) {
			this.event = event;
		}

		public void run() {
			if (event.getTarget() == null)
				fireTableDataChanged();
			else {
				int rowIndex = goodsList.indexOf(event.getTarget());
				int columnIndex = settlements.indexOf(event.getSource()) * NUM_DATA_COL + NUM_INITIAL_COLUMNS;
				fireTableCellUpdated(rowIndex, columnIndex);
			}
		}
	}

	@Override
	public void unitManagerUpdate(UnitManagerEvent event) {
		Unit unit = event.getUnit();
		if (unit.getUnitType() == UnitType.SETTLEMENT
				&& unit.getName().equalsIgnoreCase(settlements.get(0).getName())) {

			Settlement settlement = (Settlement) unit;

			if (UnitManagerEventType.ADD_UNIT == event.getEventType()) {
				// If settlement is new, add to settlement list.
				if (!settlements.contains(settlement)) {
					settlements.add(settlement);
					settlement.addUnitListener(this);
				}
			} else if (UnitManagerEventType.REMOVE_UNIT == event.getEventType()) {
				// If settlement is gone, remove from settlement list.
				if (settlements.contains(settlement)) {
					settlements.remove(settlement);
					settlement.removeUnitListener(this);
				}
			}

			// Update table structure due to cells changing.
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					fireTableStructureChanged();
					//fireTableStructureChanged();
				}
			});
		}
	}
}
