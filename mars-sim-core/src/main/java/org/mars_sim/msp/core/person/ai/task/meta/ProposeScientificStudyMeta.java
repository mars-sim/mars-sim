/**
 * Mars Simulation Project
 * ProposeScientificStudyMeta.java
 * @version 3.1.0 2017-10-23
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.PerformLaboratoryExperiment;
import org.mars_sim.msp.core.person.ai.task.ProposeScientificStudy;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the ProposeScientificStudy task.
 */
public class ProposeScientificStudyMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.proposeScientificStudy"); //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new ProposeScientificStudy(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;
        
        if (person.isInVehicle()) {	
	        // Check if person is in a moving rover.
	        if (PerformLaboratoryExperiment.inMovingRover(person)) {
	            result = -30D;
	            return 0;
	        } 	       
	        else
	        // the penalty for performing experiment inside a vehicle
	        	result = -20D;
        }
        
        if (person.isInside()) {

	        ScientificStudyManager manager = Simulation.instance().getScientificStudyManager();
	        ScientificStudy study = manager.getOngoingPrimaryStudy(person);
	        if (study != null) {

	            // Check if study is in proposal phase.
	            if (study.getPhase().equals(ScientificStudy.PROPOSAL_PHASE)) {

	                // Increase probability if person's current job is related to study's science.
	                Job job = person.getMind().getJob();
	                ScienceType science = study.getScience();
	                if ((job != null) && science == ScienceType.getJobScience(job)) {
	                    result = 50D;
	                }
	                else {
	                    result = 10D;
	                }
	            }
	        }
	        else {
	            // Probability of starting a new scientific study.

	            // Check if scientist job.
	            if (ScienceType.isScienceJob(person.getMind().getJob())) {
	                result = 1D;
	            }

	            // Modify if researcher is already collaborating in studies.
	            int numCollabStudies = manager.getOngoingCollaborativeStudies(person).size();
	            result /= (numCollabStudies + 1D);
	        }

	        // Crowding modifier
	        if (person.isInSettlement()) {
	            Building adminBuilding = ProposeScientificStudy.getAvailableAdministrationBuilding(person);
	            if (adminBuilding != null) {

	                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, adminBuilding);
	                result *= TaskProbabilityUtil.getRelationshipModifier(person, adminBuilding);
	            }
	        }

	        // Job modifier.
	        Job job = person.getMind().getJob();
	        if (job != null) {
	            result *= job.getStartTaskProbabilityModifier(ProposeScientificStudy.class)
	            		* 1.5D * person.getAssociatedSettlement().getGoodsManager().getResearchFactor();
	        }

	        // Modify if research is the person's favorite activity.
	        if (person.getFavorite().getFavoriteActivity() == FavoriteType.RESEARCH) {
	            result *= 2D;
	        }

	        // 2015-06-07 Added Preference modifier
            // 2015-06-07 Added Preference modifier
            if (result > 0D) {
                result = result + result * person.getPreference().getPreferenceScore(this)/2D;
            }

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