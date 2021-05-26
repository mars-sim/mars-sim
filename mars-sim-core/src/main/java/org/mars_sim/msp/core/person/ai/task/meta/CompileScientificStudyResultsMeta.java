/**
 * Mars Simulation Project
 * CompileScientificStudyResultsMeta.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Iterator;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.CompileScientificStudyResults;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the CompileScientificStudyResults task.
 */
public class CompileScientificStudyResultsMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.compileScientificStudyResults"); //$NON-NLS-1$

    /** default logger. */
    private static SimLogger logger = SimLogger.getLogger(CompileScientificStudyResultsMeta.class.getName());

    public CompileScientificStudyResultsMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		addFavorite(FavoriteType.RESEARCH);
		addTrait(TaskTrait.ACADEMIC);
		setPreferredJob(JobType.ACADEMICS);
	}

    @Override
    public Task constructInstance(Person person) {
        return new CompileScientificStudyResults(person);
    }

    @Override
    public double getProbability(Person person) {
        double result = 0D;
        
        // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = person.getPhysicalCondition();
        double fatigue = condition.getFatigue();
        double stress = condition.getStress();
        double hunger = condition.getHunger();
        
        if (fatigue > 1000 || stress > 50 || hunger > 500)
        	return 0;
        
        if (person.isInside()) {
        	
	        // Add probability for researcher's primary study (if any).
            ScientificStudy primaryStudy = person.getStudy();
	        if ((primaryStudy != null) 
        		&& ScientificStudy.PAPER_PHASE.equals(primaryStudy.getPhase())
            	&& !primaryStudy.isPrimaryPaperCompleted()) {
                try {
                    double primaryResult = 50D;

                    // If researcher's current job isn't related to study science, divide by two.
                    JobType job = person.getMind().getJob();
                    if (job != null) {
                        //ScienceType jobScience = ScienceType.getJobScience(job);
                        if (!primaryStudy.getScience().equals(ScienceType.getJobScience(job))) 
                        	primaryResult /= 2D;
                    }

                    result += primaryResult;
                }
                catch (Exception e) {
                    logger.severe(person, "getProbability(): ", e);
                }
	        }

	        // Add probability for each study researcher is collaborating on.
	        Iterator<ScientificStudy> i = person.getCollabStudies().iterator();
	        while (i.hasNext()) {
	            ScientificStudy collabStudy = i.next();
	            if (ScientificStudy.PAPER_PHASE.equals(collabStudy.getPhase())
	            		&& !collabStudy.isCollaborativePaperCompleted(person)) {
                    try {
                        ScienceType collabScience = collabStudy.getContribution(person);

                        double collabResult = 25D;

                        // If researcher's current job isn't related to study science, divide by two.
                        JobType job = person.getMind().getJob();
                        if (job != null) {
                            ScienceType jobScience = ScienceType.getJobScience(job);
                            if (!collabScience.equals(jobScience)) collabResult /= 2D;
                        }

                        result += collabResult;
                    }
                    catch (Exception e) {
                        logger.severe(person, "getProbability(): ", e);
                    }
                }
	        }

	        if (result > 0) {
	            if (person.isInVehicle()) {	
	    	        // Check if person is in a moving rover.
	    	        if (Vehicle.inMovingRover(person)) {
	    	        	result += 20;
	    	        }
	    	        else
	    	        	result += 10;
	            }
	        }
	        else
	        	return 0;
	        
	        // Crowding modifier
            Building b = BuildingManager.getAvailableBuilding(null, person);
            if (b != null) {
                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, b);
                result *= TaskProbabilityUtil.getRelationshipModifier(person, b);
            }
            result *= person.getAssociatedSettlement().getGoodsManager().getResearchFactor();
            
            result = applyPersonModifier(result, person);
        }

        return result;
    }
}
