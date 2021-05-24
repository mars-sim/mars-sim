/**
 * Mars Simulation Project
 * ProposeScientificStudyMeta.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.task.ProposeScientificStudy;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the ProposeScientificStudy task.
 */
public class ProposeScientificStudyMeta extends MetaTask {
    
    private static final double FACTOR = 2D;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.proposeScientificStudy"); //$NON-NLS-1$

    public ProposeScientificStudyMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
	}

    @Override
    public Task constructInstance(Person person) {
        return new ProposeScientificStudy(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;
		
		if (person.getPhysicalCondition().computeFitnessLevel() < 3) {
			return 0;
		}
		
        // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = person.getPhysicalCondition();
        double fatigue = condition.getFatigue();
        double stress = condition.getStress();
        double hunger = condition.getHunger();
        
        if (fatigue > 500 || stress > 50 || hunger > 500)
        	return 0;
        
        if (person.isInside()) {

	        ScientificStudy study = person.getStudy();
	        if (study != null) {

	            // Check if study is in proposal phase.
	            if (study.getPhase().equals(ScientificStudy.PROPOSAL_PHASE)) {

	                // Increase probability if person's current job is related to study's science.
	                JobType job = person.getMind().getJob();
	                ScienceType science = study.getScience();
	                if ((job != null) && science == ScienceType.getJobScience(job)) {
	                    result += 20D;
	                }
	                else {
	                    result += 10D;
	                }
	            }
	        }
	        else {
	            // Probability of starting a new scientific study.

	            // Check if scientist job.
	            if (ScienceType.isScienceJob(person.getMind().getJob())) {
	                result += 20D;
	            }

	            // Modify if researcher is already collaborating in studies.
	            int numCollabStudies = person.getCollabStudies().size();
	            result /= (numCollabStudies * 1.5 + 1D);
	        }

	        if (result <= 0) return 0;
	        
	        // Crowding modifier
	        if (person.isInSettlement()) {
	            Building b = BuildingManager.getAvailableBuilding(study, person);
	            if (b != null) {
	                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, b);
	                result *= TaskProbabilityUtil.getRelationshipModifier(person, b);
	            }
	        }

	        if (person.isInVehicle()) {	
		        // Check if person is in a moving rover.
		        if (Vehicle.inMovingRover(person)) {
			        // the bonus for proposing scientific study inside a vehicle, 
		        	// rather than having nothing to do if a person is not driving
		        	result += 20;
		        } 	       
		        else
		        	// the bonus for proposing scientific study inside a vehicle,
		        	// rather than having nothing to do if a person is not driving
		        	result += 10;
	        }
	        
	        // Job modifier.
	        JobType job = person.getMind().getJob();
	        if (job != null) {
	            result *= JobUtil.getStartTaskProbabilityModifier(job, ProposeScientificStudy.class)
	            		* FACTOR * person.getAssociatedSettlement().getGoodsManager().getResearchFactor();
	        }
	        
	        // Modify if research is the person's favorite activity.
	        if (person.getFavorite().getFavoriteActivity() == FavoriteType.RESEARCH) {
	        	result += RandomUtil.getRandomInt(1, 10);
	        }

            if (result > 0D) {
                result = result + result * person.getPreference().getPreferenceScore(this);
            }

        }

        if (result < 0) result = 0;
        
        return result;
    }
}
