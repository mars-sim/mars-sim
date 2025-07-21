/*
 * Mars Simulation Project
 * AnalyzeMapDataMeta.java
 * @date 2025-07-06
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai.task.meta;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.environment.MineralSite;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.AnalyzeMapData;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.tool.Msg;

/**
 * Meta task for the AnalyzeMapData task.
 */
public class AnalyzeMapDataMeta extends FactoryMetaTask {
    
	/** Task name */
	private static final double UNIMPROVED_FACTOR = 1.25;
	private static final double CLAIM_FACTOR = 1.75;
	private static final double POTENTIAL_FACTOR = 1.75;
	private static final double MAX = 3000;
	
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.analyzeMapData"); //$NON-NLS-1$

    public AnalyzeMapDataMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.RESEARCH, FavoriteType.OPERATION);
		setTrait(TaskTrait.ACADEMIC);
		
		setPreferredJob(JobType.AREOLOGIST, JobType.PHYSICIST, 
				JobType.ENGINEER,
				JobType.MATHEMATICIAN, JobType.PILOT);
		addPreferredJob(JobType.COMPUTER_SCIENTIST, 1.5D);
		
		setPreferredRole(RoleType.CREW_ENGINEER, RoleType.SCIENCE_SPECIALIST,
				RoleType.CREW_OPERATION_OFFICER);
		addPreferredRole(RoleType.CHIEF_OF_SCIENCE, 1.25D);
		addPreferredRole(RoleType.COMPUTING_SPECIALIST, 1.5D);
		addPreferredRole(RoleType.CHIEF_OF_COMPUTING, 1.5D);
		
	}
    @Override
    public Task constructInstance(Person person) {
        return new AnalyzeMapData(person);
    }
    
	/**
	 * Gets the list of analyzing map tasks that this Person can perform all individually scored.
	 * 
	 * @param person the Person to perform the task.
	 * @return List of TasksJob specifications.
	 */
	@Override
	public List<TaskJob> getTaskJobs(Person person) {
        	
        // Probability affected by the person's stress and fatigue.
        if (!person.getPhysicalCondition().isFitByLevel(1000, 80, 1000)
			|| person.isOutside()) {
        	return EMPTY_TASKLIST;
		}
		
		var eMgr = person.getAssociatedSettlement().getExplorations();
		int unclaimedSites = eMgr.numDeclaredROIs(false);
			
		Set<Coordinates> nearbySites = eMgr.getNearbyMineralLocations();	
	
		int numNearby = nearbySites.size();
		
		Set<MineralSite> minableLocs = eMgr.getDeclaredROIs()
				.stream()
				.filter(el -> el != null && el.isMinable())
				.collect(Collectors.toSet());

		int unimprovedScore = 0;
		double certainty = 0.0;
		for (MineralSite s: minableLocs) {
			int est = s.getNumEstimationImprovement();
			unimprovedScore += MineralSite.IMPROVEMENT_THRESHOLD - est;
			certainty += s.getAverageCertainty();
		}
	
		if (!minableLocs.isEmpty())
			certainty = certainty/minableLocs.size();
		
		int uncertainty = (int)(100 - certainty) + 1;
		
		int potentialScore = numNearby;

		double unclaimedScore = uncertainty * (1.0 + (int)Math.ceil(unclaimedSites / 5D));
			
		var result = new RatingScore("mapdata.uncertainty", CLAIM_FACTOR * unclaimedScore);
		
		result.addBase("mapdata.unimproved", Math.min(MAX, UNIMPROVED_FACTOR * unimprovedScore));
		
		result.addBase("mapdata.potential", POTENTIAL_FACTOR * potentialScore);

		result = applyCommerceFactor(result, person.getAssociatedSettlement(), CommerceType.RESEARCH);
		
        result = assessPersonSuitability(result, person);

        return createTaskJobs(result);
    }
}
