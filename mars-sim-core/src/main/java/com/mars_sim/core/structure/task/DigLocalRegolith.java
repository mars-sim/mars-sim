/*
 * Mars Simulation Project
 * DigLocalRegolith.java
 * @date 2023-08-22
 * @author Scott Davis
 */

package com.mars_sim.core.structure.task;

import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.tool.Msg;

/**
 * This class is a task for digging and collecting
 * regolith right outside in the vicinity of a settlement.
 */
public class DigLocalRegolith
extends DigLocal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Simple Task name */
	public static final String SIMPLE_NAME = DigLocalRegolith.class.getSimpleName();
	static final EquipmentType CONTAINER_TYPE = EquipmentType.WHEELBARROW;
	
	/** Task name */
    public static final String NAME = Msg.getString(
            "Task.description.digLocalRegolith"); //$NON-NLS-1$

    /** Task phases. */
    static final TaskPhase COLLECT_REGOLITH = new TaskPhase(Msg.getString(
            "Task.phase.collectRegolith"),
			createPhaseImpact(SkillType.PROSPECTING, SkillType.AREOLOGY));

	/**
	 * Constructor 1.
	 * 
	 * @param person the person performing the task.
	 */
	DigLocalRegolith(Person person) {
        // Use EVAOperation constructor.
        super(NAME, COLLECT_REGOLITH, ResourceUtil.REGOLITH_ID, 
        	  CONTAINER_TYPE, person, 150);
        if (!isDone()) {
        	setCollectionRate(person.getAssociatedSettlement().getRegolithCollectionRate());
        }
    }
}
