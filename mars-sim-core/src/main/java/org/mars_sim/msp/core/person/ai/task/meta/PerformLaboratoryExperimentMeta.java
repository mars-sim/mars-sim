/**
 * Mars Simulation Project
 * PerformLaboratoryExperimentMeta.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.task.PerformLaboratoryExperiment;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.Lab;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the PerformLaboratoryExperiment task.
 */
public class PerformLaboratoryExperimentMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.performLaboratoryExperiment"); //$NON-NLS-1$

    /** default logger. */
    private static Logger logger = Logger.getLogger(PerformLaboratoryExperimentMeta.class.getName());

    // Create list of experimental sciences.
    private static List<ScienceType> experimentalSciences = PerformLaboratoryExperiment.getExperimentalSciences();
    
    public PerformLaboratoryExperimentMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		
		addFavorite(FavoriteType.LAB_EXPERIMENTATION);
		addTrait(TaskTrait.ACADEMIC);

	}

    @Override
    public Task constructInstance(Person person) {
        return new PerformLaboratoryExperiment(person);
    }

    @Override
    public double getProbability(Person person) {
        double result = 0D;
        
        // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = person.getPhysicalCondition();
        double fatigue = condition.getFatigue();
        double stress = condition.getStress();
        double hunger = condition.getHunger();
        
        if (fatigue > 500 || stress > 50 || hunger > 500)
        	return 0;
        
        if (person.isInside()) {

	        // Add probability for researcher's primary study (if any).
	        ScientificStudy primaryStudy = person.getStudy();
	        if ((primaryStudy != null) && ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase())) {
	            if (!primaryStudy.isPrimaryResearchCompleted()) {
	                if (experimentalSciences.contains(primaryStudy.getScience())) {
	                    try {
	                        Lab lab = PerformLaboratoryExperiment.getLocalLab(person, primaryStudy.getScience());
	                        if (lab != null) {
	                            double primaryResult = 50D;

	                            // Get lab building crowding modifier.
	                            primaryResult *= PerformLaboratoryExperiment.getLabCrowdingModifier(person, lab);

	                            // If researcher's current job isn't related to study science, divide by two.
	                            JobType job = person.getMind().getJob();
	                            if (job != null) {
	                                ScienceType jobScience = ScienceType.getJobScience(job);
	                                if (primaryStudy.getScience() != jobScience) {
	                                    primaryResult /= 2D;
	                                }
	                            }

	                            result += primaryResult;
	                        }
	                    }
	                    catch (Exception e) {
                            logger.severe("[" + person.getVehicle() + "] " + person + " is unable to perform lab experiements.");// + e.getMessage());
	                    }
	                }
	            }
	        }

	        // Add probability for each study researcher is collaborating on.
	        Iterator<ScientificStudy> i = person.getCollabStudies().iterator();
	        while (i.hasNext()) {
	            ScientificStudy collabStudy = i.next();
	            if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase())) {
	                if (!collabStudy.isCollaborativeResearchCompleted(person)) {
	                    ScienceType collabScience = collabStudy.getContribution(person);
	                    if (experimentalSciences.contains(collabScience)) {
	                        try {
	                            Lab lab = PerformLaboratoryExperiment.getLocalLab(person, collabScience);
	                            if (lab != null) {
	                                double collabResult = 25D;

	                                // Get lab building crowding modifier.
	                                collabResult *= PerformLaboratoryExperiment.getLabCrowdingModifier(person, lab);

	                                // If researcher's current job isn't related to study science, divide by two.
	                                JobType job = person.getMind().getJob();
	                                if (job != null) {
	                                    ScienceType jobScience = ScienceType.getJobScience(job);
	                                    if (!collabScience.equals(jobScience)) {
	                                        collabResult /= 2D;
	                                    }
	                                }

	                                result += collabResult;
	                            }
	                        }
	                        catch (Exception e) {
	                            logger.severe("[" + person.getVehicle() + "] " + person + " is unable to perform lab experiements.");// + e.getMessage());
	                        }
	                    }
	                }
	            }
	        }

	        if (result > 0) {
		        if (person.isInVehicle()) {	
			        // Check if person is in a moving rover.
			        if (Vehicle.inMovingRover(person)) {
			        	result += -20D;
			        }
			        else
			        	// the penalty for performing experiment inside a vehicle
			        	result += 20D;
		        }
	        }
	        else
	        	return 0;
	        
	        // Effort-driven task modifier.
	        result *= person.getPerformanceRating();

	        // Job modifier.
	        JobType job = person.getMind().getJob();
	        if (job != null) {
	            result *= JobUtil.getStartTaskProbabilityModifier(job, PerformLaboratoryExperiment.class)
	            		* person.getAssociatedSettlement().getGoodsManager().getResearchFactor();
	        }

	        // Modify if lab experimentation is the person's favorite activity.
	        if (person.getFavorite().getFavoriteActivity() == FavoriteType.LAB_EXPERIMENTATION) {
	            result += RandomUtil.getRandomInt(1, 20);
	        }

	        // 2015-06-07 Added Preference modifier
            if (result > 0)
            	result = result + result * person.getPreference().getPreferenceScore(this)/2D;

	    }
        
        if (result < 0) result = 0;
        
        return result;
    }
}
