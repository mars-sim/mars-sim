/**
 * Mars Simulation Project
 * StudyFieldSamplesMeta.java
 * @version 3.1.0 2019-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.StudyFieldSamples;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.Lab;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the StudyFieldSamples task.
 */
public class StudyFieldSamplesMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.studyFieldSamples"); //$NON-NLS-1$

    /** default logger. */
    private static Logger logger = Logger.getLogger(StudyFieldSamplesMeta.class.getName());

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new StudyFieldSamples(person);
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
	
	        // Check that there are available field samples to study.
	        try {
	            Unit container = person.getContainerUnit();
				if (!(container instanceof MarsSurface)) {
	                Inventory inv = container.getInventory();
	                //AmountResource rockSamples = AmountResource.findAmountResource("rock samples");
	                if (inv.getAmountResourceStored(ResourceUtil.rockSamplesID, false) < StudyFieldSamples.SAMPLE_MASS) {
	                    return 0;
	                }
	            }
	        }
	        catch (Exception e) {
	            logger.severe("getProbability(): " + e.getMessage());
	        }
	        
	        // Create list of possible sciences for studying field samples.
	        List<ScienceType> fieldSciences = StudyFieldSamples.getFieldSciences();
	
	        // Add probability for researcher's primary study (if any).
//	        ScientificStudyManager studyManager = Simulation.instance().getScientificStudyManager();
	        ScientificStudy primaryStudy = scientificStudyManager.getOngoingPrimaryStudy(person);
	        if ((primaryStudy != null) && ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase())) {
	            if (!primaryStudy.isPrimaryResearchCompleted()) {
	                if (fieldSciences.contains(primaryStudy.getScience())) {
	                    try {
	                        Lab lab = StudyFieldSamples.getLocalLab(person, primaryStudy.getScience());
	                        if (lab != null) {
	                            double primaryResult = 50D;
	
	                            // Get lab building crowding modifier.
	                            primaryResult *= StudyFieldSamples.getLabCrowdingModifier(person, lab);
	
	                            // If researcher's current job isn't related to study science, divide by two.
	                            Job job = person.getMind().getJob();
	                            if (job != null) {
	                                ScienceType jobScience = ScienceType.getJobScience(job);
	                                if (!primaryStudy.getScience().equals(jobScience)) {
	                                    primaryResult /= 2D;
	                                }
	                            }
	
	                            result += primaryResult;
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
	                    if (fieldSciences.contains(collabScience)) {
	                        try {
	                            Lab lab = StudyFieldSamples.getLocalLab(person, collabScience);
	                            if (lab != null) {
	                                double collabResult = 25D;
	
	                                // Get lab building crowding modifier.
	                                collabResult *= StudyFieldSamples.getLabCrowdingModifier(person, lab);
	
	                                // If researcher's current job isn't related to study science, divide by two.
	                                Job job = person.getMind().getJob();
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
	                            logger.severe("getProbability(): " + e.getMessage());
	                        }
	                    }
	                }
	            }
	        }
	
	
	        // Effort-driven task modifier.
	        result *= person.getPerformanceRating();
	
	        // Job modifier.
	        Job job = person.getMind().getJob();
	        if (job != null) {
	            result *= job.getStartTaskProbabilityModifier(StudyFieldSamples.class)
	            		* person.getAssociatedSettlement().getGoodsManager().getResearchFactor();
	        }
	
	        // Modify if research is the person's favorite activity.
	        if (person.getFavorite().getFavoriteActivity() == FavoriteType.FIELD_WORK) {
	            result *= 2D;
	        }
	
	        // 2015-06-07 Added Preference modifier
	        if (result > 0)
	         	result = result + result * person.getPreference().getPreferenceScore(this)/2D;
	
	    }
        
        if (result <= 0) 
        	result = 0;
        else  if (person.isInVehicle()) {	
	        // Check if person is in a moving rover.
	        if (Vehicle.inMovingRover(person)) {
		        // the bonus inside a vehicle, 
	        	// rather than having nothing to do if a person is not driving
	        	result += 30;
	        } 	       
	        else
		        // the bonus inside a vehicle, 
	        	// rather than having nothing to do if a person is not driving
	        	result += 10;
        }
        
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