/*
 * Mars Simulation Project
 * AnalyzeMapDataMeta.java
 * @date 2022-07-24
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.List;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.environment.ExploredLocation;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.AnalyzeMapData;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the AnalyzeMapDataMeta task.
 */
public class AnalyzeMapDataMeta extends MetaTask {
    
	/** Task name */
	private static final int VALUE = 10;
	
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
        if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
        	return 0;
        
        if (person.isInside()) {

        	List<ExploredLocation> siteList = Simulation.instance().getSurfaceFeatures()
        			.getExploredLocations().stream()
        			.filter(site -> !site.isMined()
        					&& site.getNumEstimationImprovement() < 15)
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
            	result *= 2.0;
        }

        if (result == 0) return 0;
		result *= person.getAssociatedSettlement().getGoodsManager().getResearchFactor();

        result = applyPersonModifier(result, person);

        if (result < 0) result = 0;
        
        return result;
    }
}
