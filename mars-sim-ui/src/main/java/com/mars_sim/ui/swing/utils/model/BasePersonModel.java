/*
 * Mars Simulation Project
 * BasePersonModel.java
 * @date 2026-05-21
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.model;

import java.util.Set;

import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.shift.ShiftSlot;
import com.mars_sim.core.person.ai.shift.ShiftSlot.WorkStatus;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.unit.MobileUnit;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.components.ColumnSpec;

/**
 * A generic table model showing Persons. It provides a number of predefined available columns.
 * The subclass defines which columns are to be rendered.
 * The model automatically monitors the Person for changes and updates the table as needed.
 */
public abstract class BasePersonModel extends AbstractEntityModel<Person> {

    private static final int INSIDE_VAL = 100;
	private static final int HEALTH_VAL = 101;
    private static final int ENERGY_VAL = 102;
    private static final int WATER_VAL = 103;
    private static final int FATIGUE_VAL = 104;
    private static final int STRESS_VAL = 105;
    private static final int PERFORMANCE_VAL = 106;
    private static final int EMOTION_VAL = 107;
    private static final int LOCATION_VAL = 108;
    private static final int LOCALE_VAL = 109;
    private static final int ROLE_VAL = 110;
    private static final int JOB_VAL = 111;
    private static final int SHIFT_VAL = 112;


    // Columns based on Worker
    protected static final EntityColumnSpec NAME = BaseWorkerModel.NAME;
    protected static final EntityColumnSpec TASK = BaseWorkerModel.TASK;
    protected static final EntityColumnSpec SETTLEMENT = BaseWorkerModel.SETTLEMENT;
    protected static final EntityColumnSpec MISSION = BaseWorkerModel.MISSION;

    // Display whether the Person is inside based on a changed of container
    protected static final EntityColumnSpec INSIDE = new EntityColumnSpec(new ColumnSpec(INSIDE_VAL, "Inside", Boolean.class),
                                                Set.of(MobileUnit.CONTAINER_EVENT));
	protected static final EntityColumnSpec HEALTH = new EntityColumnSpec(new ColumnSpec(HEALTH_VAL, Msg.getString("person.health"), String.class),
                                                Set.of(PhysicalCondition.ILLNESS_EVENT, EntityEventType.DEATH_EVENT,
                                                    EntityEventType.BURIAL_EVENT, EntityEventType.REVIVED_EVENT));
	protected static final EntityColumnSpec ENERGY = new EntityColumnSpec(new ColumnSpec(ENERGY_VAL, Msg.getString("person.energy"), String.class),
                                                Set.of(PhysicalCondition.HUNGER_EVENT, PhysicalCondition.THIRST_EVENT));
	protected static final EntityColumnSpec WATER = new EntityColumnSpec(new ColumnSpec(WATER_VAL, Msg.getString("person.water"), String.class), null);
	protected static final EntityColumnSpec FATIGUE = new EntityColumnSpec(new ColumnSpec(FATIGUE_VAL, Msg.getString("person.fatigue"), String.class),
                                                Set.of(PhysicalCondition.FATIGUE_EVENT));
	protected static final EntityColumnSpec STRESS = new EntityColumnSpec(new ColumnSpec(STRESS_VAL, Msg.getString("person.stress"), String.class),
                                                Set.of(PhysicalCondition.STRESS_EVENT));
	protected static final EntityColumnSpec PERFORMANCE = new EntityColumnSpec(new ColumnSpec(PERFORMANCE_VAL, Msg.getString("person.performance"), String.class),
                                                Set.of(PhysicalCondition.PERFORMANCE_EVENT));
	protected static final EntityColumnSpec EMOTION = new EntityColumnSpec(new ColumnSpec(EMOTION_VAL, Msg.getString("person.emotion"), String.class),
                                                Set.of(EntityEventType.EMOTION_EVENT));
	protected static final EntityColumnSpec LOCATION = new EntityColumnSpec(new ColumnSpec(LOCATION_VAL, Msg.getString("PersonTableModel.column.location"), String.class),
                                                Set.of(EntityEventType.COORDINATE_EVENT, MobileUnit.CONTAINER_EVENT));
	protected static final EntityColumnSpec LOCALE = new EntityColumnSpec(new ColumnSpec(LOCALE_VAL, Msg.getString("PersonTableModel.column.locale"), String.class), null);
	protected static final EntityColumnSpec ROLE = new EntityColumnSpec(new ColumnSpec(ROLE_VAL, Msg.getString("person.role"), String.class), null);
	protected static final EntityColumnSpec JOB = new EntityColumnSpec(new ColumnSpec(JOB_VAL, Msg.getString("person.job"), String.class),
                                                Set.of(EntityEventType.JOB_EVENT));
	protected static final EntityColumnSpec SHIFT = new EntityColumnSpec(new ColumnSpec(SHIFT_VAL, Msg.getString("person.shift"), String.class),
                                                Set.of(ShiftSlot.SHIFT_EVENT));
      
    /**
     * Create a generic person model with the specified columns.
     * @param columns Columns to show.
     */
    protected BasePersonModel(EntityColumnSpec... columns) {
        super(columns);
    }

    /**
     * Get a cell value for the associated Person. Column index maps to the associated ColumnSpec where the id
     * is used to determine the value to return.
     * @param entity The Person entity.
     * @param valueIndex Column index. 
     * @return Associated value.
     */
    @Override
    protected Object getEntityValue(Person entity, int valueIndex) {
        var pc = entity.getPhysicalCondition();
        boolean isDead = pc.isDead();
        
        return switch(valueIndex) {
            case INSIDE_VAL -> entity.isInside();  
            case ENERGY_VAL -> isDead ? null : pc.getHungerLevel().getName();
			case WATER_VAL -> isDead ? null : pc.getThirstLevel().getName();
            case FATIGUE_VAL -> isDead ? null : pc.getFatigueLevel().getName();
			case STRESS_VAL -> isDead ? null : pc.getStressLevel().getName();
			case PERFORMANCE_VAL -> isDead ? null : pc.getPerformanceLevel().getName();
			case EMOTION_VAL -> isDead ? null : entity.getMind().getEmotion().getDescription();
			case HEALTH_VAL -> entity.getPhysicalCondition().getStatus();
			case LOCATION_VAL -> entity.getLocationTag().getImmediateLocation();
			case LOCALE_VAL -> entity.getLocationTag().getLocale();

			case ROLE_VAL -> {
				if (!isDead) {
					var role = entity.getRole();
					yield (role != null) ? role.getType().getName() : null;
				}
                else {
                    yield null;
                }
			}

			case JOB_VAL -> {
				// If person is dead, get job from death info.
				if (isDead) {
					yield pc.getDeathDetails().getJob().getName();
				} else if (entity.getMind().getJobType() != null) {
					yield entity.getMind().getJobType().getName();
				}
                else {
                    yield null;
                }
			}

			case SHIFT_VAL -> {
				// If person is dead, disable it.
				if (!isDead) {
					ShiftSlot shift = entity.getShiftSlot();		
					if (shift.getStatus() == WorkStatus.ON_CALL) {
						yield WorkStatus.ON_CALL.getName();
					}
					else {
						yield shift.getStatusDescription();
					}
				}
                else {
                    yield null;
                }
            }
			      
            default -> BaseWorkerModel.getWorkerValue(entity, valueIndex);
        };
    }

    
    /**
     * Show raw value for level band columns.
     */
    @Override
    protected String getEntityDescription(Person entity, int colValue) {
        var pc = entity.getPhysicalCondition();
        if (pc.isDead()) {
            return null;
        }

        return switch (colValue) {
            case ENERGY_VAL -> StyleManager.DECIMAL_PLACES1.format(pc.getHunger());
			case WATER_VAL -> StyleManager.DECIMAL_PLACES1.format(pc.getThirst());
            case FATIGUE_VAL -> StyleManager.DECIMAL_PLACES1.format(pc.getFatigue());
			case STRESS_VAL -> StyleManager.DECIMAL_PLACES1.format(pc.getStress());
			case PERFORMANCE_VAL -> StyleManager.DECIMAL_PLACES1.format(pc.getPerformanceFactor());
            default -> null;
        };
    }
}