/*
 * Mars Simulation Project
 * ProposeScientificStudyMeta.java
 * @date 2023-08-12
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import com.mars_sim.core.authority.PreferenceCategory;
import com.mars_sim.core.authority.PreferenceKey;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.Role;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.ProposeScientificStudy;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.tools.Msg;

/**
 * Meta task for proposing a scientific study.
 */
public class ProposeScientificStudyMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.proposeScientificStudy"); //$NON-NLS-1$

    public ProposeScientificStudyMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
		setFavorite(FavoriteType.RESEARCH);
		setTrait(TaskTrait.ACADEMIC, TaskTrait.LEADERSHIP);
		setPreferredJob(JobType.ACADEMICS);
		setPreferredRole(RoleType.CHIEF_OF_SCIENCE, RoleType.SCIENCE_SPECIALIST);
	}

    @Override
    public Task constructInstance(Person person) {
        return new ProposeScientificStudy(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

		Settlement settlement = person.getAssociatedSettlement();
        int pop = settlement.getInitialPopulation();
		JobType job = person.getMind().getJob();

        if (pop <= 6) {
	        if (!JobType.ACADEMICS.contains(job)) {
	        	return 0;
	        }
        }
        else if (pop <= 12) {
        	if (!JobType.MEDICS.contains(job)
        			&& !JobType.SCIENTISTS.contains(job)) {
	        	return 0;
	        }
        }
        else {
        	if (!JobType.SCIENTISTS.contains(job)) {
	        	return 0;
	        }
        }

		ScienceType science = ScienceType.getJobScience(job);
		
        if (science == null)
        	return 0;
        
        // Probability affected by the person's stress and fatigue.
        if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
        	return 0;

        if (person.isInside()) {

            Role role = person.getRole();
            
	        ScientificStudy study = person.getStudy();
	        if (study != null) {
	            // Check if study is in proposal phase.
	            if (study.getPhase().equals(ScientificStudy.PROPOSAL_PHASE)) {
	                // Once a person starts a study in the proposal phase,
	            	// there's a greater chance to continue on the proposal.
	            	result += 100D;
	            	
	            	// Check person has a science role
		            if (role != null) {
						switch(role.getType()) {
		            		case CHIEF_OF_SCIENCE:
		            			result += 100D;
								break;
		            		case SCIENCE_SPECIALIST:
		            			result += 200D;
								break;
							default:
								break;
	            		}
		            }
	            }
	            else {
		            // If the person already have a study and it's not in the proposal phase,
	            	// do not start a new proposal
	            	return 0;
	            }
	        }

	        else {
	            // Probability of starting a new scientific study.	        	
	        	result += 500D;
	        	
	            // Check person has a science role
	            if (role != null) {
					switch(role.getType()) {
	            		case CHIEF_OF_SCIENCE:
	            		case SCIENCE_SPECIALIST:
	            			result += 50D;
							break;
	            		case CHIEF_OF_COMPUTING:
		            	case COMPUTING_SPECIALIST:
            				result += 20D;
							break;
            			case CHIEF_OF_AGRICULTURE:
		            	case AGRICULTURE_SPECIALIST:
            				result += 10D;
							break;
						default:
							break;
            		}
	            }
	            else {
					result += 5D;
				}

				// Check the favourite research of the Reporting Authority
				result *= settlement.getPreferenceModifier(
								new PreferenceKey(PreferenceCategory.SCIENCE, science.name()));
	        }

	        // Crowding modifier
	        if (person.isInSettlement()) {
	            Building b = BuildingManager.getAvailableBuilding(study, person);
				result *= getBuildingModifier(b, person);
	        }

	        result *= settlement.getGoodsManager().getResearchFactor();
	        result *= getPersonModifier(person);
        }

        if (result < 0) result = 0;

        return result;
    }
}