/*
 * Mars Simulation Project
 * DigLocalIce.java
 * @date 2023-07-04
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
 * The DigLocalRegolith class is a task for digging and collecting
 * ice right outside in the vicinity of a settlement.
 */
public class DigLocalIce
extends DigLocal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.digLocalIce"); //$NON-NLS-1$
	
	static final EquipmentType CONTAINER_TYPE = EquipmentType.BAG;

    /** Task phases. */
    static final TaskPhase COLLECT_ICE = new TaskPhase(Msg.getString(
            "Task.phase.collectIce"), createPhaseImpact(SkillType.PROSPECTING, SkillType.AREOLOGY));


	/**
	 * Constructor 1.
	 * 
	 * @param person the person performing the task.
	 * @throws Exception if error constructing the task.
	 */
	public DigLocalIce(Person person) {
        // Use EVAOperation constructor.
        super(NAME, COLLECT_ICE, ResourceUtil.ICE_ID, CONTAINER_TYPE, person, 150); 
        if (!isDone()) {
        	setCollectionRate(person.getAssociatedSettlement().getIceCollectionRate());
        }
	}
}
