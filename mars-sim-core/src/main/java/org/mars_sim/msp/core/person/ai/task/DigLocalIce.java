/*
 * Mars Simulation Project
 * DigLocalIce.java
 * @date 2021-08-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.ResourceUtil;

/**
 * The DigLocalIce class is a task for performing
 * collecting ice outside of a settlement.
 */
public class DigLocalIce
extends DigLocal
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.digLocalIce"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase COLLECT_ICE = new TaskPhase(Msg.getString(
            "Task.phase.collectIce")); //$NON-NLS-1$

	/**
	 * Constructor.
	 * @param person the person performing the task.
	 * @throws Exception if error constructing the task.
	 */
	public DigLocalIce(Person person) {
        // Use EVAOperation constructor.
        super(NAME, COLLECT_ICE, ResourceUtil.iceID, EquipmentType.BAG, person); 
        
        setCollectionRate(getSettlement().getIceCollectionRate());
	}
}
