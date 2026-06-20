/*
 * Mars Simulation Project
 * BaseRobotModel.java
 * @date 2026-05-23
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.model;

import java.util.Set;

import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.equipment.Battery;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.SystemCondition;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;

/**
 * A generic table model showing Robots. It provides a number of predefined available columns.
 * The subclass defines which columns are to be rendered. This reuses the BaseWorkerModel for common columns.
 * The model automatically monitors the Robot for changes and updates the table as needed.
 */
public abstract class BaseRobotModel extends AbstractEntityModel<Robot> {

    private static final int TYPE_VAL = 101;
    private static final int LOCATION_VAL = 102;
    private static final int MODE_VAL = 103;
    private static final int HEALTH_VAL = 104;
    private static final int BATTERY_VAL = 105;
    private static final int BATT_TEMPERATURE_VAL = 106;
    private static final int PERFORMANCE_VAL = 107;

    private static final String INOPERABLE = "Inoperable";
	private static final String OPERABLE = "Operable";
    
    // Supported values
    protected static final EntityColumnSpec NAME = BaseWorkerModel.NAME;
    protected static final EntityColumnSpec TASK = BaseWorkerModel.TASK;
    protected static final EntityColumnSpec SETTLEMENT = BaseWorkerModel.SETTLEMENT;
    protected static final EntityColumnSpec TYPE = new EntityColumnSpec(new ColumnSpec(TYPE_VAL, Msg.getString("robot.type"),
                                                        String.class), null);
    protected static final EntityColumnSpec LOCATION = new EntityColumnSpec(new ColumnSpec(LOCATION_VAL, Msg.getString("entity.coordinates"),
                                                        String.class), Set.of(EntityEventType.COORDINATE_EVENT));
    protected static final EntityColumnSpec MODE = new EntityColumnSpec(new ColumnSpec(MODE_VAL, Msg.getString("robot.mode"),
                                                        String.class), Set.of(EntityEventType.STATUS_EVENT));
    protected static final EntityColumnSpec HEALTH = new EntityColumnSpec(new ColumnSpec(HEALTH_VAL, Msg.getString("robot.health"),
                                                        String.class), Set.of(EntityEventType.DEATH_EVENT));
    protected static final EntityColumnSpec BATTERY = new EntityColumnSpec(new ColumnSpec(BATTERY_VAL, Msg.getString("battery.singular"),
                                                        String.class), Set.of(Battery.BATTERY_EVENT));
    protected static final EntityColumnSpec BATTERY_TEMPERATURE = new EntityColumnSpec(new ColumnSpec(BATT_TEMPERATURE_VAL, Msg.getString("battery.temperature"),
                                                        Double.class, ColumnSpec.STYLE_DIGIT1), Set.of(Battery.BATTERY_EVENT));
	protected static final EntityColumnSpec PERFORMANCE = new EntityColumnSpec(new ColumnSpec(PERFORMANCE_VAL, Msg.getString("robot.performance"),
                                                        String.class), Set.of(SystemCondition.PERFORMANCE_EVENT));

    /**)
     * Create a generic robot model with the specified columns.
     * @param columns Columns to show.
     */
    protected BaseRobotModel(EntityColumnSpec... columns) {
        super(columns);
    }

    /**
     * Get a cell value for the associated Robot. Column index maps to the associated ColumnSpec where the id
     * is used to determine the value to return.
     * @param entity The Robot entity.
     * @param valueIndex Column index. 
     * @return Associated value.
     */
    @Override
    protected Object getEntityValue(Robot entity, int valueIndex) {
        return switch (valueIndex) {
            case TYPE_VAL -> entity.getModel();
            case LOCATION_VAL -> entity.getLocationTag().getImmediateLocation();
            case MODE_VAL -> entity.printStatusModes();
            case HEALTH_VAL -> entity.isOperable() ? OPERABLE : INOPERABLE;
            case BATTERY_VAL -> entity.getSystemCondition().getBattery().getBatteryStatus().getName();
            case BATT_TEMPERATURE_VAL -> entity.getSystemCondition().getBattery().getInternalTemperature();
            case PERFORMANCE_VAL -> entity.getSystemCondition().getPerformanceLevel().getName();
            default -> BaseWorkerModel.getWorkerValue(entity, valueIndex);
        };
    }
}