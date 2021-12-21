/*
 * Mars Simulation Project
 * PerformMathematicalModelingMeta.java
 * @Date 2021-12-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Iterator;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.PerformMathematicalModeling;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.Lab;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the PerformMathematicalModeling task.
 */
public class PerformMathematicalModelingMeta extends MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.performMathematicalModeling"); //$NON-NLS-1$

    /** default logger. */
    private static final Logger logger = Logger.getLogger(PerformMathematicalModelingMeta.class.getName()); 
    
    public PerformMathematicalModelingMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.RESEARCH);
		setTrait(TaskTrait.ACADEMIC);
		setPreferredJob(JobType.MATHEMATICIAN, JobType.PHYSICIST, JobType.COMPUTER_SCIENTIST, JobType.ENGINEER);
	}


    @Override
    public Task constructInstance(Person person) {
        return new PerformMathematicalModeling(person);
    }

    @Override
    public double getProbability(Person person) {

        ScientificStudy primaryStudy = person.getStudy();
        if (primaryStudy == null)
        	return 0;
        
        double result = 0D;

        // Probability affected by the person's stress and fatigue.
        if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
        	return 0;
        
        if (person.isInside()) {

	        ScienceType mathematics = ScienceType.MATHEMATICS;

	        // Add probability for researcher's primary study (if any).
	        if (primaryStudy != null && ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase())
	            && !primaryStudy.isPrimaryResearchCompleted()
	            && mathematics == primaryStudy.getScience()) {
                try {
                    Lab lab = PerformMathematicalModeling.getLocalLab(person);
                    if (lab != null) {
                        double primaryResult = 50D;
                        // Get lab building crowding modifier.
                        primaryResult *= PerformMathematicalModeling.getLabCrowdingModifier(person, lab);
                        // If researcher's current job isn't related to study science, divide by two.
                        JobType job = person.getMind().getJob();
                        if (job != null && primaryStudy.getScience() != ScienceType.getJobScience(job)) {
                        	primaryResult /= 2D;
                        }

                        result += primaryResult;
                        
                        // Check if person is in a moving rover.
                        if (person.isInVehicle() && Vehicle.inMovingRover(person)) {
                	        // the bonus for being inside a vehicle since there's little things to do
                            result += 20D;
                        }
                    }
                }
                catch (Exception e) {
                    logger.severe("getProbability(): " + e.getMessage());
                }
	        }

	        // Add probability for each study researcher is collaborating on.
	        Iterator<ScientificStudy> i = person.getCollabStudies().iterator();
	        while (i.hasNext()) {
	            ScientificStudy collabStudy = i.next();
	            if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase())
	                 && !collabStudy.isCollaborativeResearchCompleted(person)) {
                    ScienceType collabScience = collabStudy.getContribution(person);
                    if (mathematics == collabScience) {
                        try {
                            Lab lab = PerformMathematicalModeling.getLocalLab(person);
                            if (lab != null) {
                                double collabResult = 25D;
                                // Get lab building crowding modifier.
                                collabResult *= PerformMathematicalModeling.getLabCrowdingModifier(person, lab);
                                // If researcher's current job isn't related to study science, divide by two.
                                JobType job = person.getMind().getJob();
                                if (job != null && collabScience != ScienceType.getJobScience(job)) {
                                	collabResult /= 2D;
                                }

                                result += collabResult;
                            }
                        }
                        catch (Exception e) {
                            logger.severe("getProbability(): " + e.getMessage());
                        }
                    }
	            }
	        }

	        if (result == 0) return 0;
    		result *= person.getAssociatedSettlement().getGoodsManager().getResearchFactor();

	        result = applyPersonModifier(result, person);
        }

        if (result < 0) result = 0;
        
        return result;
    }
}
