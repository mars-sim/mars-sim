/*
 * Mars Simulation Project
 * DoInventoryMeta.java
 * @date 2023-11-30
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.DoInventory;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.tool.Msg;

/**
 * Meta task for doing an inventory of a facility.
 */
public class DoInventoryMeta extends FactoryMetaTask {

	private static final int LAB_FACTOR = 10;

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.doInventory"); //$NON-NLS-1$
	/** default logger. */
	// May add back private static SimLogger logger = SimLogger.getLogger(DoInventoryMeta.class.getName())

	
    public DoInventoryMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.TINKERING);
	
		setPreferredRole(RoleType.RESOURCE_SPECIALIST, 
				RoleType.CHIEF_OF_SUPPLY_N_RESOURCES,
				RoleType.CREW_OPERATION_OFFICER);
    }
    
	@Override
	public Task constructInstance(Person person) {
		return new DoInventory(person);
	}

	@Override
	public List<TaskJob> getTaskJobs(Person person) {
        
		if (!person.isInSettlement()) {
			return EMPTY_TASKLIST;
		}

		// Compute total entropy and average minimum entropy per node
		double totENPerL = person.getAssociatedSettlement().getBuildingManager()
					.getTotalEntropyPerLab();

		if (totENPerL < 0) {
			return EMPTY_TASKLIST;
		}
		
		RatingScore score = new RatingScore(ENTROPY_LAB, LAB_FACTOR * totENPerL);

		NaturalAttributeManager manager = person.getNaturalAttributeManager();
		
		double att = (.3 * manager.getAttribute(NaturalAttributeType.ORGANIZATION)
				+ .3 * manager.getAttribute(NaturalAttributeType.METICULOUSNESS)
				+ .4 * manager.getAttribute(NaturalAttributeType.DISCIPLINE)) / 20;
		
        score.addModifier(ATTRIBUTE, att);
        
        score = assessPersonSuitability(score, person);
        
		return createTaskJobs(score);
	}
}
