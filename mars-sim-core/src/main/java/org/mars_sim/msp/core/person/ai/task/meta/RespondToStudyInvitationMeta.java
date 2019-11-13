/**
 * Mars Simulation Project
 * RespondToStudyInvitationMeta.java
 * @version 3.1.0 2019-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.RespondToStudyInvitation;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the RespondToStudyInvitation task.
 */
public class RespondToStudyInvitationMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.respondToStudyInvitation"); //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new RespondToStudyInvitation(person);
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

            // Check if person has been invited to collaborate on any scientific studies.
	        List<ScientificStudy> invitedStudies = scientificStudyManager.getOpenInvitationStudies(person);
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

	        // Job modifier.
	        Job job = person.getMind().getJob();
	        if (job != null) {
	            result *= job.getStartTaskProbabilityModifier(RespondToStudyInvitation.class)
	            		* person.getAssociatedSettlement().getGoodsManager().getResearchFactor();
	        }

	        // Modify if research is the person's favorite activity.
	        if (person.getFavorite().getFavoriteActivity() == FavoriteType.RESEARCH) {
	            result += RandomUtil.getRandomInt(1, 20);
	        }

	        // Add Preference modifier
	        if (result > 0)
	           	result = result + result * person.getPreference().getPreferenceScore(this)/5D;

        }

        if (result <= 0) result = 0;
        
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