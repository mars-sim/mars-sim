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
import com.mars_sim.core.Named;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.farming.Crop;
import com.mars_sim.core.building.function.farming.CropCategory;
import com.mars_sim.core.building.function.farming.CropConfig;
import com.mars_sim.core.building.function.farming.Farming;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.utils.model.BaseBuildingModel;

/**
 * The CropTableModel keeps track of the quantity of the growing crops in each greenhouse by categories.
 */
@SuppressWarnings("serial")
class CropTableModel extends BaseBuildingModel implements MonitorModel {

	// Column indexes

	private static final int CROP_COUNT_VAL = 200;
	private static final int FIRST_CROP_CAT = 1000;
	
	// Pseudo event to trigger refresh of CROP_VAL column
	private static final String CROP_COUNT_EVENT= "crop count";

	/** Names of Columns. */
	private static EntityColumnSpec[] columns;
	private static List<CropCategory> cropCats;

	private static EntityColumnSpec[] getColumns(CropConfig config) {
		if (columns == null) {
			cropCats = config.getCropCategories();
			columns = new EntityColumnSpec[3 + cropCats.size()];
			columns[0] = NAME;
			columns[1] = SETTLEMENT;
			columns[2] = new EntityColumnSpec(new ColumnSpec(CROP_COUNT_VAL, Msg.getString("crop.plural"), String.class), Set.of(CROP_COUNT_EVENT));

			int idx = 0;
			for (CropCategory cat : cropCats) {
				columns[3 + idx] = new EntityColumnSpec(new ColumnSpec(FIRST_CROP_CAT + idx, cat.getName(), Integer.class), Set.of(cat.getName()));
				idx++;
			}
		}
		return columns;
	}

	public CropTableModel(SimulationConfig config) {
		super (getColumns(config.getCropConfiguration()));
	}

	@Override
	public String getName() {
		return Msg.getString("crop.plural");
	}

	@Override
	public int getSettlementColumn() {
		return 1;
	}

	/**
	 * Filter the Greenhouses according to a Settlement
	 */
	@Override
	public boolean setSettlementFilter(Set<Settlement> filter) {
		
		Set<Building> buildings = filter.stream()
				.flatMap(s -> s.getBuildingManager().getBuildingSet(FunctionType.FARMING).stream())
				.collect(Collectors.toSet());

		setEntities(buildings);

		return true;
	}

	/**
	 * Gets the total number of crop in a crop group from cropMap or cropCache.
	 *
	 * @param return a number
	 */
	private long getValueAtCropCat(Building greenhouse, int cropColumn) {
		CropCategory cropCat = cropCats.get(cropColumn - FIRST_CROP_CAT);

		return(int) greenhouse.getFarming().getCrops()
				.stream()
				.filter(c -> c.getCropSpec().getCropCategory().equals(cropCat))
				.count();
	}

	@Override
	protected String getEntityDescription(Building entity, int cropColumn) {
		if (cropColumn >= FIRST_CROP_CAT) {
			CropCategory cropCat = cropCats.get(cropColumn - FIRST_CROP_CAT);
			
			var crops = entity.getFarming().getCrops().stream()
					.filter(c -> c.getCropSpec().getCropCategory().equals(cropCat))
					.map(Named::getName)
					.collect(Collectors.groupingBy(c -> c, Collectors.counting()));
			if (crops.isEmpty()) {
				return null;
			}

			return crops.entrySet().stream()
					.map(e -> e.getKey() + " (" + e.getValue() + ")")
					.collect(Collectors.joining("<br/>", "<html>", "</html>"));
		}

		return super.getEntityDescription(entity, cropColumn);
	}

	/**
	 * Return the value of a Cell.
	 *
	 * @param rowIndex    Row index of the cell.
	 * @param columnVal Column value index of the cell.
	 */
	@Override
	protected Object getEntityValue(Building greenhouse, int columnVal) {

		if (columnVal >= FIRST_CROP_CAT) {
			return getValueAtCropCat(greenhouse, columnVal);
		}

		return switch (columnVal) {
			case CROP_COUNT_VAL -> greenhouse.getFarming().getCrops().size() + "/" + greenhouse.getFarming().getMaxCrops();
			default -> super.getEntityValue(greenhouse, columnVal);
		};
	}

	/**
	 * Catch unit update event.
	 *
	 * @param event the unit event.
	 */
	@Override
	public void entityUpdate(EntityEvent event) {
		if (event.getTarget() instanceof Crop crop) {
			String eventType = event.getType();
	
			if (Farming.CROP_EVENT.equals(eventType)) {
				CropCategory cat = crop.getCropSpec().getCropCategory();

				// Make a Psuedo event
				event = new EntityEvent(event.getSource(), cat.getName(), event.getTarget());

				// Force an event to update total
				super.entityUpdate(new EntityEvent(event.getSource(), CROP_COUNT_EVENT, null));
			}
		}

		super.entityUpdate(event);
	}
}
