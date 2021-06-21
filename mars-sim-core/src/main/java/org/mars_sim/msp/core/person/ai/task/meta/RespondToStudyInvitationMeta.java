/**
 * Mars Simulation Project
 * RespondToStudyInvitationMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.RespondToStudyInvitation;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the RespondToStudyInvitation task.
 */
public class RespondToStudyInvitationMeta extends MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.respondToStudyInvitation"); //$NON-NLS-1$

    public RespondToStudyInvitationMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.RESEARCH);
		setTrait(TaskTrait.ACADEMIC);
		setPreferredJob(JobType.ACADEMICS);
	}


    @Override
    public Task constructInstance(Person person) {
        return new RespondToStudyInvitation(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;
        
        // Probability affected by the person's stress and fatigue.
        if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
        	return 0;
        
        if (person.isInside()) {

            // Check if person has been invited to collaborate on any scientific studies.
        	ScientificStudyManager sm = Simulation.instance().getScientificStudyManager();
	        List<ScientificStudy> invitedStudies = sm.getOpenInvitationStudies(person);
	        if (invitedStudies.size() > 0) {
	            result += invitedStudies.size() * 100D;
	            
	            if (person.isInVehicle()) {	
	    	        // Check if person is in a moving rover.
	    	        if (Vehicle.inMovingRover(person)) {
	    		        // the bonus inside a vehicle 
	    	        	result += 30;
	    	        } 	       
	    	        else
	    		        // the bonus inside a vehicle
	    	        	result += 10;
	            }
	        }
	        
	        if (result <= 0) return 0;

	        // Crowding modifier
            Building adminBuilding = RespondToStudyInvitation.getAvailableAdministrationBuilding(person);
            if (adminBuilding != null) {
                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, adminBuilding);
                result *= TaskProbabilityUtil.getRelationshipModifier(person, adminBuilding);
            }
            result *= person.getAssociatedSettlement().getGoodsManager().getResearchFactor();

            result = applyPersonModifier(result, person);
        }

        if (result <= 0) result = 0;
        
        return result;
    }
}
