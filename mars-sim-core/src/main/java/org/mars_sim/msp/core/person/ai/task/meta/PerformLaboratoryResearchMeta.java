/**
 * Mars Simulation Project
 * PerformLaboratoryResearchMeta.java
 * @version 3.1.0 2017-10-23
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.PerformLaboratoryResearch;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.structure.Lab;

/**
 * Meta task for the PerformLaboratoryResearch task.
 */
public class PerformLaboratoryResearchMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.performLaboratoryResearch"); //$NON-NLS-1$

    /** default logger. */
    private static Logger logger = Logger.getLogger(PerformLaboratoryResearchMeta.class.getName());

    private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1, logger.getName().length());

    private static ScientificStudyManager studyManager;
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new PerformLaboratoryResearch(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;
        
        if (person.isInSettlement()) {
   
	        // Add probability for researcher's primary study (if any).
	        if (studyManager == null)
	        	studyManager = Simulation.instance().getScientificStudyManager();
	        //ScientificStudyManager studyManager = Simulation.instance().getScientificStudyManager();
	        ScientificStudy primaryStudy = studyManager.getOngoingPrimaryStudy(person);
	        if ((primaryStudy != null) && ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase())) {
	            if (!primaryStudy.isPrimaryResearchCompleted()) {
	                try {
	                    Lab lab = PerformLaboratoryResearch.getLocalLab(person, primaryStudy.getScience());
	                    if (lab != null) {
	                        double primaryResult = 50D;

	                        // Get lab building crowding modifier.
	                        primaryResult *= PerformLaboratoryResearch.getLabCrowdingModifier(person, lab);

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
//                        logger.severe("[" + person.getVehicle() + "] " + person + " is unable to perform lab research.");// + e.getMessage());
            			LogConsolidated.log(logger, Level.INFO, 2000, sourceName,
            					"[" + person.getLocationTag().getImmediateLocation() + "] " + person + " is unable to perform lab research.", null);	                
	                }
	            }
	        }

	        // Add probability for each study researcher is collaborating on.
	        Iterator<ScientificStudy> i = studyManager.getOngoingCollaborativeStudies(person).iterator();
	        while (i.hasNext()) {
	            ScientificStudy collabStudy = i.next();
	            if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase())) {
	                if (!collabStudy.isCollaborativeResearchCompleted(person)) {
	                    try {
	                        ScienceType collabScience = collabStudy.getCollaborativeResearchers().get(person);

	                        Lab lab = PerformLaboratoryResearch.getLocalLab(person, collabScience);
	                        if (lab != null) {
	                            double collabResult = 25D;

	                            // Get lab building crowding modifier.
	                            collabResult *= PerformLaboratoryResearch.getLabCrowdingModifier(person, lab);

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
//	                        logger.severe("[" + person.getVehicle() + "] " + person + " is unable to perform lab research.");// + e.getMessage());
	            			LogConsolidated.log(logger, Level.INFO, 2000, sourceName,
	            					"[" + person.getLocationTag().getImmediateLocation() + "] " + person + " is unable to perform lab research.", null);	                

	                    }
	                }
	            }
	        }

	        // Effort-driven task modifier.
	        result *= person.getPerformanceRating();

	        // Job modifier.
	        Job job = person.getMind().getJob();
	        if (job != null) {
	            result *= job.getStartTaskProbabilityModifier(PerformLaboratoryResearch.class)
	            		* person.getAssociatedSettlement().getGoodsManager().getResearchFactor();
	        }

	        // Modify if research is the person's favorite activity.
	        if (person.getFavorite().getFavoriteActivity() == FavoriteType.RESEARCH) {
	            result *= 1.2D;
	        }
	        
	        // Modify if lab experimentation is the person's favorite activity.
	        if (person.getFavorite().getFavoriteActivity() == FavoriteType.LAB_EXPERIMENTATION) {
	            result *= 1.2D;
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