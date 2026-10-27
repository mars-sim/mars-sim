/*
 * Mars Simulation Project
 * BaseDataModel.java
 * @date 2026-10-13
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.utils.model;

import com.mars_sim.core.data.collection.FieldDataSet;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;

/**
 * A generic table model showing Buildings. It provides a number of predefined available columns.
 * The subclass defines which columns are to be rendered.
 * The model automatically monitors the Building for changes and updates the table as needed.
 */
@SuppressWarnings("serial")
public abstract class BaseDataModel extends AbstractEntityModel<FieldDataSet> {

	private static final int ID_VAL = 300;
	private static final int SETTLEMENT_VAL = 301;
	private static final int TYPE_VAL = 302;
	private static final int TIME_VAL = 303;
	private static final int QUALITY_VAL = 304;
	private static final int OWNER_VAL = 305;
	private static final int WORKTIME_VAL = 306;
	private static final int COORD_VAL = 307;
	
	protected static final EntityColumnSpec ID = new EntityColumnSpec(
					new ColumnSpec(ID_VAL, Msg.getString("TabPanelData.column.id"), Integer.class), null);
	protected static final EntityColumnSpec SETTLEMENT = new EntityColumnSpec(
					new ColumnSpec(SETTLEMENT_VAL, Msg.getString("TabPanelData.column.settlement"), String.class), null);
	protected static final EntityColumnSpec TYPE = new EntityColumnSpec(
					new ColumnSpec(TYPE_VAL, Msg.getString("TabPanelData.column.type"), String.class), null);
	protected static final EntityColumnSpec TIME = new EntityColumnSpec(
					new ColumnSpec(TIME_VAL, Msg.getString("TabPanelData.column.time"), MarsTime.class), null);
	protected static final EntityColumnSpec QUALITY = new EntityColumnSpec(
					new ColumnSpec(QUALITY_VAL, Msg.getString("TabPanelData.column.quality"), Double.class), null);
	protected static final EntityColumnSpec OWNER = new EntityColumnSpec(
					new ColumnSpec(OWNER_VAL, Msg.getString("TabPanelData.column.owner"), String.class), null);
	protected static final EntityColumnSpec WORKTIME = new EntityColumnSpec(
					new ColumnSpec(WORKTIME_VAL, Msg.getString("TabPanelData.column.workTime"), Double.class), null);
	protected static final EntityColumnSpec COORD = new EntityColumnSpec(
					new ColumnSpec(COORD_VAL, Msg.getString("TabPanelData.column.coordinates"), Coordinates.class), null);
	
    /**
     * Creates a generic model with the specified columns.
     * 
     * @param columns Columns to show.
     */
    protected BaseDataModel(EntityColumnSpec... columns) {
        super(columns);
    }

    /**
     * Gets a cell value for the associated data set. Column index maps to the associated ColumnSpec where the id
     * is used to determine the value to return.
     * 
     * @param entity The data entity.
     * @param valueIndex Column index.
     * @return Associated value.
     */
    @Override
    protected Object getEntityValue(FieldDataSet entity, int valueIndex) {
        return switch (valueIndex) {
            case ID_VAL -> entity.getIdentifier();
            case SETTLEMENT_VAL  -> entity.getOwningSettlement().getName();
            case TYPE_VAL -> entity.getDataType().getName();
            case TIME_VAL -> entity.getTimpstamp();
            case QUALITY_VAL -> entity.getQuality();
            case OWNER_VAL -> entity.getOwner().getName();
            case WORKTIME_VAL -> entity.getWorkTime();
			case COORD_VAL -> entity.getCoordinates();

            default -> null;
        };
    }
}