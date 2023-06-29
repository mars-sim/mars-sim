/*
 * Mars Simulation Project
 * AnalyzeMapDataMeta.java
 * @date 2022-10-01
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.List;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.environment.ExploredLocation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.AnalyzeMapData;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the AnalyzeMapData task.
 */
public class AnalyzeMapDataMeta extends FactoryMetaTask {
    
	/** Task name */
	private static final int VALUE = 30;
	
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.analyzeMapData"); //$NON-NLS-1$

    /** default logger. */
//	private static SimLogger logger = SimLogger.getLogger(AnalyzeMapDataMeta.class.getName());

    public AnalyzeMapDataMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.RESEARCH);
		setTrait(TaskTrait.ACADEMIC);
		setPreferredJob(JobType.AREOLOGIST, JobType.PHYSICIST, JobType.COMPUTER_SCIENTIST, JobType.ENGINEER);
	}

    @Override
    public Task constructInstance(Person person) {
        return new AnalyzeMapData(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double result = 0D;

        // Probability affected by the person's stress and fatigue.
        if (!person.getPhysicalCondition().isFitByLevel(500, 50, 500))
        	return 0;
        
        if (person.isInside()) {

        	List<ExploredLocation> siteList = surfaceFeatures
        			.getExploredLocations().stream()
        			.filter(site -> site.isMinable()
        					&& site.getNumEstimationImprovement() < 
        						RandomUtil.getRandomInt(0, Mining.MATURE_ESTIMATE_NUM * 10))
        			.collect(Collectors.toList());  	
        	
        	int num = siteList.size();
        	
        	if (num == 0)
        		return 0;
        	
    		result += num * VALUE;
    		
            // Check if person is in a moving rover.
            if (person.isInVehicle() && Vehicle.inMovingRover(person)) {
    	        // the bonus for being inside a vehicle since there's little things to do
                result += 20D;
            }
            
            if (JobType.COMPUTER_SCIENTIST == person.getMind().getJob())
            	result *= 1.5;
            
            if (RoleType.COMPUTING_SPECIALIST == person.getRole().getType())
            	result *= 1.5;
            
            else if (RoleType.CHIEF_OF_COMPUTING == person.getRole().getType())
            	result *= 1.25;
        }

        if (result == 0) return 0;
		result *= person.getAssociatedSettlement().getGoodsManager().getResearchFactor();

        result *= getPersonModifier(person);

        if (result < 0) result = 0;
        
        return result;
    }
}
