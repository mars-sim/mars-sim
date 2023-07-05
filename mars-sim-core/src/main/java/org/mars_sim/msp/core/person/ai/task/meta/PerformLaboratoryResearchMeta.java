/*
 * Mars Simulation Project
 * PerformLaboratoryResearchMeta.java
 * @Date 2021-10-05
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.PerformLaboratoryResearch;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.Lab;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the PerformLaboratoryResearch task.
 */
public class PerformLaboratoryResearchMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.performLaboratoryResearch"); //$NON-NLS-1$

    /** default logger. */
    private static SimLogger logger = SimLogger.getLogger(PerformLaboratoryResearchMeta.class.getName());

    public PerformLaboratoryResearchMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);

		setFavorite(FavoriteType.LAB_EXPERIMENTATION, FavoriteType.RESEARCH);
		setTrait(TaskTrait.ACADEMIC);

		// Jobs are the lab technicans and some scientists
		Set<JobType> jobs = new HashSet<>(JobType.SCIENTISTS);
		jobs.add(JobType.MATHEMATICIAN);
		jobs.add(JobType.METEOROLOGIST);
		setPreferredJob(jobs);
		setPreferredRole(RoleType.CHIEF_OF_SCIENCE, RoleType.SCIENCE_SPECIALIST);
	}

    @Override
    public Task constructInstance(Person person) {
        return new PerformLaboratoryResearch(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        ScientificStudy primaryStudy = person.getStudy();
        if (primaryStudy == null)
        	return 0;

        if (person.isInSettlement()) {

            // Probability affected by the person's stress and fatigue.
            if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
            	return 0;

	        // Add probability for researcher's primary study (if any).
	        if (primaryStudy != null
	        		&& ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase())
	        		&& !primaryStudy.isPrimaryResearchCompleted()) {
                try {
                    Lab lab = PerformLaboratoryResearch.getLocalLab(person, primaryStudy.getScience());
                    if (lab != null) {
                        double primaryResult = 50D;

                        // Get lab building crowding modifier.
                        primaryResult *= PerformLaboratoryResearch.getLabCrowdingModifier(person, lab);

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
                	logger.severe(person.getVehicle(), 10_000L, person + " was unable to perform lab research.", e);
                }
            }

	        // Add probability for each study researcher is collaborating on.
	        Iterator<ScientificStudy> i = person.getCollabStudies().iterator();
	        while (i.hasNext()) {
	            ScientificStudy collabStudy = i.next();
	            if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase())
	            		&& !collabStudy.isCollaborativeResearchCompleted(person)) {
                    try {
                        ScienceType collabScience = collabStudy.getContribution(person);

                        Lab lab = PerformLaboratoryResearch.getLocalLab(person, collabScience);
                        if (lab != null) {
                            double collabResult = 25D;

                            // Get lab building crowding modifier.
                            collabResult *= PerformLaboratoryResearch.getLabCrowdingModifier(person, lab);

                            // If researcher's current job isn't related to study science, divide by two.
                            JobType job = person.getMind().getJob();
                            if (job != null) {
                                ScienceType jobScience = ScienceType.getJobScience(job);
                                if (collabScience != jobScience) {
                                    collabResult /= 2D;
                                }
                            }

                            result += collabResult;
                        }
                    }
                    catch (Exception e) {
                    	logger.severe(person.getVehicle(), 10_000L, person + " was unable to perform lab research.", e);
                    }
	            }
	        }

	        if (result <= 0)
	        	return 0;

	        else {
	            if (person.isInVehicle()) {
	    	        // Check if person is in a moving rover.
	    	        if (Vehicle.inMovingRover(person)) {
	    		        // the bonus for proposing scientific study inside a vehicle,
	    	        	// rather than having nothing to do if a person is not driving
	    	        	result += -20;
	    	        }
	    	        else
	    		        // the bonus for proposing scientific study inside a vehicle,
	    	        	// rather than having nothing to do if a person is not driving
	    	        	result += 20;
	            }
	        }
            result *= person.getAssociatedSettlement().getGoodsManager().getResearchFactor();

            result *= getPersonModifier(person);
        }

        if (result < 0) result = 0;

        return result;
    }
}
