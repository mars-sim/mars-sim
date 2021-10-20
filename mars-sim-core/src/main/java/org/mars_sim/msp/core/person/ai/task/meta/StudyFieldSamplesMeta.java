/**
 * Mars Simulation Project
 * StudyFieldSamplesMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.data.ResourceHolder;
import org.mars_sim.msp.core.environment.MarsSurface;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.StudyFieldSamples;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.Lab;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the StudyFieldSamples task.
 */
public class StudyFieldSamplesMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.studyFieldSamples"); //$NON-NLS-1$

    /** default logger. */
    private static final Logger logger = Logger.getLogger(StudyFieldSamplesMeta.class.getName());
    
    public StudyFieldSamplesMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		
		setFavorite(FavoriteType.FIELD_WORK);
		setTrait(TaskTrait.ACADEMIC);
		setPreferredJob(JobType.AREOLOGIST, JobType.BIOLOGIST,
						JobType.BOTANIST, JobType.CHEMIST);
	}
    
    @Override
    public Task constructInstance(Person person) {
        return new StudyFieldSamples(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        // Probability affected by the person's stress and fatigue.
        if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
        	return 0;
        
        if (person.isInside()) {
	
	        // Check that there are available field samples to study.
	        try {
	        	double samplesStored = 0D;
	            Unit container = person.getContainerUnit();
	            if (container instanceof ResourceHolder) {
	            	samplesStored = ((ResourceHolder)container).getAmountResourceStored(ResourceUtil.rockSamplesID);
	            }
	            else if (!(container instanceof MarsSurface)) {
	                Inventory inv = container.getInventory();
	                samplesStored = inv.getAmountResourceStored(ResourceUtil.rockSamplesID, false);
	            }
	            
	            if (samplesStored < StudyFieldSamples.SAMPLE_MASS) {
                    return 0;
                }
	        }
	        catch (Exception e) {
	            logger.severe("getProbability(): " + e.getMessage());
	        }
	        
	        // Create list of possible sciences for studying field samples.
	        List<ScienceType> fieldSciences = StudyFieldSamples.getFieldSciences();
	
	        // Add probability for researcher's primary study (if any).
	        ScientificStudy primaryStudy = person.getStudy();
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
	                            JobType job = person.getMind().getJob();
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
	        Iterator<ScientificStudy> i = person.getCollabStudies().iterator();
	        while (i.hasNext()) {
	            ScientificStudy collabStudy = i.next();
	            if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase())) {
	                if (!collabStudy.isCollaborativeResearchCompleted(person)) {
	                    ScienceType collabScience = collabStudy.getContribution(person);
	                    if (fieldSciences.contains(collabScience)) {
	                        try {
	                            Lab lab = StudyFieldSamples.getLocalLab(person, collabScience);
	                            if (lab != null) {
	                                double collabResult = 25D;
	
	                                // Get lab building crowding modifier.
	                                collabResult *= StudyFieldSamples.getLabCrowdingModifier(person, lab);
	
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
	                            logger.severe("getProbability(): " + e.getMessage());
	                        }
	                    }
	                }
	            }
	        }
	
	        result = applyPersonModifier(result, person);
	    }
        
        if (result <= 0) 
        	result = 0;
        
        else if (person.isInSettlement()) {
        	result *= 1.5;
        }
        
        else if (person.isInVehicle()) {	
	        // Check if person is in a moving rover.
	        if (!Vehicle.inMovingRover(person)) {
		        // Easier to examine if not moving
	        	// rather than having nothing to do if a person is not driving
	        	result /= 1.5;
	        } 	       
	        else
	        	// harder to examine if moving
	        	result /= 3D;
        }
        
        return result;
    }
}
