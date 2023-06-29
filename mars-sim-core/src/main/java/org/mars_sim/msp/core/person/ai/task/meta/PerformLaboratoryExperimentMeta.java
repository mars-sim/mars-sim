/*
 * Mars Simulation Project
 * PerformLaboratoryExperimentMeta.java
 * @Date 2021-10-05
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.PerformLaboratoryExperiment;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.Lab;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the PerformLaboratoryExperiment task.
 */
public class PerformLaboratoryExperimentMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.performLaboratoryExperiment"); //$NON-NLS-1$

    /** default logger. */
    private static SimLogger logger = SimLogger.getLogger(PerformLaboratoryExperimentMeta.class.getName());

    // Create list of experimental sciences.
    private static List<ScienceType> experimentalSciences = PerformLaboratoryExperiment.getExperimentalSciences();

    public PerformLaboratoryExperimentMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);

		setFavorite(FavoriteType.LAB_EXPERIMENTATION);
		setTrait(TaskTrait.ACADEMIC);
		setPreferredJob(JobType.ACADEMICS);
		setPreferredRole(RoleType.CHIEF_OF_SCIENCE, RoleType.SCIENCE_SPECIALIST);
	}

    @Override
    public Task constructInstance(Person person) {
        return new PerformLaboratoryExperiment(person);
    }

    @Override
    public double getProbability(Person person) {
        double result = 0D;

        // Probability affected by the person's stress and fatigue.
        if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
        	return 0;

        if (person.isInside()) {

	        // Add probability for researcher's primary study (if any).
	        ScientificStudy primaryStudy = person.getStudy();
	        if ((primaryStudy != null) && ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase())
	        		&& !primaryStudy.isPrimaryResearchCompleted()
	        		&& experimentalSciences.contains(primaryStudy.getScience())) {
                try {
                    Lab lab = PerformLaboratoryExperiment.getLocalLab(person, primaryStudy.getScience());
                    if (lab != null) {
                        double primaryResult = 50D;

                        // Get lab building crowding modifier.
                        primaryResult *= PerformLaboratoryExperiment.getLabCrowdingModifier(person, lab);

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
                    logger.severe(person.getVehicle(), 10_000L, person + " was unable to perform lab experiements.", e);
                }
	        }

	        // Add probability for each study researcher is collaborating on.
	        Iterator<ScientificStudy> i = person.getCollabStudies().iterator();
	        while (i.hasNext()) {
	            ScientificStudy collabStudy = i.next();
	            if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase())
	            		&& !collabStudy.isCollaborativeResearchCompleted(person)) {
                    ScienceType collabScience = collabStudy.getContribution(person);
                    if (experimentalSciences.contains(collabScience)) {
                        try {
                            Lab lab = PerformLaboratoryExperiment.getLocalLab(person, collabScience);
                            if (lab != null) {
                                double collabResult = 25D;

                                // Get lab building crowding modifier.
                                collabResult *= PerformLaboratoryExperiment.getLabCrowdingModifier(person, lab);

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
                            logger.severe(person.getVehicle(), 10_000L, person + " was unable to perform lab experiements.", e);
                        }
                    }
                }
	        }

	        if (result > 0) {
		        if (person.isInVehicle()) {
			        // Check if person is in a moving rover.
			        if (Vehicle.inMovingRover(person)) {
			        	result += -20D;
			        }
			        else
			        	// the penalty for performing experiment inside a vehicle
			        	result += 20D;
		        }
	        }
	        else
	        	return 0;

	        result *= person.getAssociatedSettlement().getGoodsManager().getResearchFactor();

	        result *= getPersonModifier(person);
	    }

        if (result < 0) result = 0;

        return result;
    }
}
