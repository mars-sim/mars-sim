/**
 * Mars Simulation Project
 * PerformMathematicalModelingMeta.java
 * @version 3.1.0 2017-10-23
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.Iterator;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.PerformMathematicalModeling;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.Lab;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the PerformMathematicalModeling task.
 */
public class PerformMathematicalModelingMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.performMathematicalModeling"); //$NON-NLS-1$

    /** default logger. */
    private static Logger logger = Logger.getLogger(PerformMathematicalModelingMeta.class.getName()); 
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new PerformMathematicalModeling(person);
    }

    @Override
    public double getProbability(Person person) {

        ScientificStudy primaryStudy = scientificStudyManager.getOngoingPrimaryStudy(person);
        if (primaryStudy == null)
        	return 0;
        
        double result = 0D;

        // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = person.getPhysicalCondition();
        double fatigue = condition.getFatigue();
        double stress = condition.getStress();
        double hunger = condition.getHunger();
        
        if (fatigue > 1000 || stress > 50 || hunger > 500)
        	return 0;
        
        
        if (person.isInside()) {

	        ScienceType mathematics = ScienceType.MATHEMATICS;

	        // Add probability for researcher's primary study (if any).
	        if ((primaryStudy != null) && ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase())) {
	            if (!primaryStudy.isPrimaryResearchCompleted()) {
	                if (mathematics == primaryStudy.getScience()) {
	                    try {
	                        Lab lab = PerformMathematicalModeling.getLocalLab(person);
	                        if (lab != null) {
	                            double primaryResult = 50D;

	                            // Get lab building crowding modifier.
	                            primaryResult *= PerformMathematicalModeling.getLabCrowdingModifier(person, lab);

	                            // If researcher's current job isn't related to study science, divide by two.
	                            Job job = person.getMind().getJob();
	                            if (job != null) {
	                                ScienceType jobScience = ScienceType.getJobScience(job);
	                                if (primaryStudy.getScience() != jobScience) {
	                                    primaryResult /= 2D;
	                                }
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
	            }
	        }

	        // Add probability for each study researcher is collaborating on.
	        Iterator<ScientificStudy> i = scientificStudyManager.getOngoingCollaborativeStudies(person).iterator();
	        while (i.hasNext()) {
	            ScientificStudy collabStudy = i.next();
	            if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase())) {
	                if (!collabStudy.isCollaborativeResearchCompleted(person)) {
	                    ScienceType collabScience = collabStudy.getCollaborativeResearchers().get(person.getIdentifier());
	                    if (mathematics == collabScience) {
	                        try {
	                            Lab lab = PerformMathematicalModeling.getLocalLab(person);
	                            if (lab != null) {
	                                double collabResult = 25D;

	                                // Get lab building crowding modifier.
	                                collabResult *= PerformMathematicalModeling.getLabCrowdingModifier(person, lab);

	                                // If researcher's current job isn't related to study science, divide by two.
	                                Job job = person.getMind().getJob();
	                                if (job != null) {
	                                    //ScienceType jobScience = ScienceType.getJobScience(job);
	                                    if (collabScience != ScienceType.getJobScience(job)) {
	                                        collabResult /= 2D;
	                                    }
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
	        }

	        if (result == 0) return 0;
	        
	        // Effort-driven task modifier.
	        result *= person.getPerformanceRating();

	        // Job modifier.
	        Job job = person.getMind().getJob();
	        if (job != null) {
	            result *= job.getStartTaskProbabilityModifier(PerformMathematicalModeling.class)
	            		* person.getAssociatedSettlement().getGoodsManager().getResearchFactor();
	        }

	        // Modify if lab experimentation is the person's favorite activity.
	        if (person.getFavorite().getFavoriteActivity() == FavoriteType.RESEARCH) {
	            result += RandomUtil.getRandomInt(1, 20);
	        }

	        // 2015-06-07 Added Preference modifier
            if (result > 0)
            	result = result + result * person.getPreference().getPreferenceScore(this)/2D;

        }

        if (result < 0) result = 0;
        
        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}