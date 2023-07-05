/*
 * Mars Simulation Project
 * DigLocalRegolith.java
 * @date 2023-07-04
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.resource.ResourceUtil;

/**
 * The DigLocalRegolith class is a task for digging and collecting
 * regolith right outside in the vicinity of a settlement.
 */
public class DigLocalRegolith
extends DigLocal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Simple Task name */
	public static final String SIMPLE_NAME = DigLocalRegolith.class.getSimpleName();
	
	/** Task name */
    public static final String NAME = Msg.getString(
            "Task.description.digLocalRegolith"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase COLLECT_REGOLITH = new TaskPhase(Msg.getString(
            "Task.phase.collectRegolith")); //$NON-NLS-1$

	/**
	 * Constructor 1.
	 * 
	 * @param person the person performing the task.
	 */
	public DigLocalRegolith(Person person) {
        // Use EVAOperation constructor.
        super(NAME, COLLECT_REGOLITH, ResourceUtil.regolithID, 
        	  EquipmentType.WHEELBARROW, person, 150);
        if (!isDone()) {
        	setCollectionRate(person.getAssociatedSettlement().getRegolithCollectionRate());
        }
    }
	
	/**
	 * Constructor 2.
	 * 
	 * @param person the person performing the task.
	 * @param duration
	 */
	public DigLocalRegolith(Person person, int duration) {
        // Use EVAOperation constructor.
        super(NAME, COLLECT_REGOLITH, ResourceUtil.regolithID, 
        	  EquipmentType.WHEELBARROW, person, duration);
        if (!isDone()) {
        	setCollectionRate(person.getAssociatedSettlement().getRegolithCollectionRate());
        }
    }
}
