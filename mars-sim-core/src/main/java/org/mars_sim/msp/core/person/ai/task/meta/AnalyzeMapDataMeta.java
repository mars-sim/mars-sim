/*
 * Mars Simulation Project
 * AnalyzeMapDataMeta.java
 * @date 2023-06-30
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
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.AnalyzeMapData;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the AnalyzeMapData task.
 */
public class AnalyzeMapDataMeta extends FactoryMetaTask {
    
	/** Task name */
	private static final double VALUE = 0.5;
	
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.analyzeMapData"); //$NON-NLS-1$

    /** default logger. */
//	May add back private static SimLogger logger = SimLogger.getLogger(AnalyzeMapDataMeta.class.getName())

    public AnalyzeMapDataMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.RESEARCH, FavoriteType.OPERATION);
		setTrait(TaskTrait.ACADEMIC);
		
		setPreferredJob(JobType.AREOLOGIST, JobType.PHYSICIST, 
				JobType.COMPUTER_SCIENTIST, JobType.ENGINEER,
				JobType.MATHEMATICIAN, JobType.PILOT);
		setPreferredRole(RoleType.CHIEF_OF_SCIENCE, RoleType.SCIENCE_SPECIALIST);
	}

    @Override
    public Task constructInstance(Person person) {
        return new AnalyzeMapData(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double result = 0D;
	
        // Probability affected by the person's stress and fatigue.
        if (!person.getPhysicalCondition().isFitByLevel(1000, 80, 1000))
        	return 0;
        
        if (person.isInside()) {

        	int numUnimproved = 0;
        	
        	List<ExploredLocation> siteList = surfaceFeatures
        			.getAllRegionOfInterestLocations().stream()
        			.filter(site -> site.isMinable())
        			.collect(Collectors.toList());  	
        	
        	for (ExploredLocation el: siteList) {   	
        		 int est = el.getNumEstimationImprovement();
        		 numUnimproved += ExploredLocation.IMPROVEMENT_THRESHOLD - est;
        	}
        	    	
        	int num = siteList.size();
        	
        	if (num == 0)
        		return 0;
        	
    		result += VALUE * numUnimproved / num;
	
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
