/*
 * Mars Simulation Project
 * GatherGroundData.java
 * @date 2026-08-27
 * @author Manny Kung
 */

package com.mars_sim.core.data.collection.task;

import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * This class is a task for gathering ground field data.
 */
public class GatherGroundData
extends GatherData {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Simple Task name */
	public static final String SIMPLE_NAME = GatherGroundData.class.getSimpleName();
	static final EquipmentType CONTAINER_TYPE = EquipmentType.DATA_RECORDER;
	
	/** Task name */
    public static final String NAME = Msg.getString(
            "Task.description.gatherGroundData"); //$NON-NLS-1$

    /** Task phases. */
    static final TaskPhase PREPARE_SITE_INSTRUMENTS = new TaskPhase(Msg.getString(
            "Task.phase.prepareSiteInstrument"),
			createPhaseImpact(SkillType.COMPUTING, SkillType.AREOLOGY));

	/**
	 * Constructor 1.
	 * 
	 * @param person the person performing the task.
	 */
	GatherGroundData(Person person) {
        // Use EVAOperation constructor.
        super(NAME, PREPARE_SITE_INSTRUMENTS, CONTAINER_TYPE, person, RandomUtil.getRandomInt(-20 + 20) + 300);
        if (!isDone()) {
        	determineCollectionFactors(person.getAssociatedSettlement().getIceCollectionRate());
        }
    }
}
