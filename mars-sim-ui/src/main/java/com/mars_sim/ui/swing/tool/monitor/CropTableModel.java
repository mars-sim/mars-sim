/*
 * Mars Simulation Project
 * CropTableModel.java
 * @date 2022-06-28
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.farming.Crop;
import com.mars_sim.core.building.function.farming.CropCategory;
import com.mars_sim.core.building.function.farming.CropConfig;
import com.mars_sim.core.building.function.farming.Farming;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.utils.ColumnSpec;

/**
 * The CropTableModel keeps track of the quantity of the growing crops in each greenhouse by categories.
 */
@SuppressWarnings("serial")
class CropTableModel extends EntityMonitorModel<Building> {

	// Column indexes
	private static final int GREENHOUSE_NAME = 0;
	private static final int SETTLEMENT_NAME = 1;
	private static final int INITIAL_COLS = 2;
	private static final int FIRST_CROP_CAT = INITIAL_COLS + 1;
	
	/** Names of Columns. */
	private static ColumnSpec[] columns;
	private static List<CropCategory> cropCats;

	private static ColumnSpec[] getColumns(CropConfig config) {
		if (columns == null) {
			cropCats = config.getCropCategories();
			columns = new ColumnSpec[FIRST_CROP_CAT + cropCats.size()];
			columns[GREENHOUSE_NAME] = new ColumnSpec("Greenhouse", String.class);
			columns[SETTLEMENT_NAME] = new ColumnSpec("Settlement", String.class);
			columns[INITIAL_COLS] = new ColumnSpec("# Crops", Integer.class);

			int idx = FIRST_CROP_CAT;
			for (CropCategory cat : cropCats) {
				columns[idx] = new ColumnSpec(cat.getName(), Integer.class);
				idx++;
			}
		}
		return columns;
	}

	public CropTableModel(SimulationConfig config) {
		super (Msg.getString("CropTableModel.tabName"), //$NON-NLS-1$
				"CropTableModel.countingCrops", getColumns(config.getCropConfiguration()));

		// Cache all crop categories
		setCachedColumns(INITIAL_COLS, FIRST_CROP_CAT + cropCats.size());
		setSettlementColumn(SETTLEMENT_NAME);
	}

	/**
	 * Filter the Greenhouses according to a Settlement
	 */
	@Override
	protected boolean applySettlementFilter(Set<Settlement> filter) {
		
		Set<Building> buildings = filter.stream()
				.flatMap(s -> s.getBuildingManager().getBuildingSet(FunctionType.FARMING).stream())
				.collect(Collectors.toSet());

		resetItems(buildings);

		return true;
	}

	/**
	 * Gives the position number for a particular crop group.
	 *
	 * @param cropCat Crop category to search
	 * @return a position number
	 */
	private int getCategoryNum(CropCategory cat) {
		return cropCats.indexOf(cat);
	}

	/**
	 * Gets the total number of crop in a crop group from cropMap or cropCache.
	 *
	 * @param return a number
	 */
	private Object getValueAtCropCat(Building greenhouse, int cropColumn) {
		CropCategory cropCat = cropCats.get(cropColumn - FIRST_CROP_CAT);

		return(int) greenhouse.getFarming().getCrops()
				.stream()
				.filter(c -> c.getCropSpec().getCropCategory().equals(cropCat))
				.count();
	}

	/**
	 * Return the value of a Cell.
	 *
	 * @param rowIndex    Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	@Override
	public Object getItemValue(Building greenhouse, int columnIndex) {
		Object result = null;

		switch (columnIndex) {
			case GREENHOUSE_NAME: 
				result = greenhouse.getName();
				break;
			case SETTLEMENT_NAME: 
				result = greenhouse.getSettlement().getName();
				break;
			case INITIAL_COLS: 
				result = getTotalNumOfAllCrops(greenhouse);
				break;
			default: 
				result = getValueAtCropCat(greenhouse, columnIndex);
				break;
		}

		return result;
	}


	/**
	 * Gets the total numbers of all crops in a greenhouse building
	 *
	 * @param b Building
	 * @return total num of crops
	 */
	private int getTotalNumOfAllCrops(Building b) {
		if (!b.getFarming().getCrops().isEmpty())
			return b.getFarming().getCrops().size();
		return 0;
	}

	/**
	 * Catch unit update event.
	 *
	 * @param event the unit event.
	 */
	@Override
	public void entityUpdate(EntityEvent event) {
		if (event.getTarget() instanceof Crop crop) {
			Building building = (Building) event.getSource();
			String eventType = event.getType();
			Object target = event.getTarget();
	
			int columnNum = -1;
			if (EntityEventType.ADD_BUILDING_EVENT.equals(eventType)) {
				if (target instanceof Farming)
					columnNum = GREENHOUSE_NAME; // = 1
			}
	
			else if (Farming.CROP_EVENT.equals(eventType)) {
				CropCategory cat = crop.getCropSpec().getCropCategory();
				columnNum = getCategoryNum(cat);
			}
			if (columnNum > -1) {
				entityValueUpdated(building, columnNum, columnNum);
			}
		}
	}
}
