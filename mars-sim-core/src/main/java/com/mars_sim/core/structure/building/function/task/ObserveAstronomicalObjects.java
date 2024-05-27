/*
 * Mars Simulation Project
 * ObserveAstronomicalObjects.java
 * @date 2022-07-17
 * @author Sebastien Venot
 */

package com.mars_sim.core.structure.building.function.task;

import java.util.logging.Level;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillManager;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.task.ResearchScientificStudy;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.function.AstronomicalObservation;
import com.mars_sim.core.structure.building.function.ComputingJob;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * A task for observing the night sky with an astronomical observatory.
 */
public class ObserveAstronomicalObjects extends Task implements ResearchScientificStudy {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ObserveAstronomicalObjects.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.observeAstronomicalObjects"); //$NON-NLS-1$

	/** The stress modified per millisol. */	    
    private static final ExperienceImpact IMPACT = new ExperienceImpact(25D,
										NaturalAttributeType.ACADEMIC_APTITUDE, false, -0.2D,
										SkillType.ASTRONOMY);

	/** Task phases. */
	private static final TaskPhase OBSERVING = new TaskPhase(Msg.getString("Task.phase.observing")); //$NON-NLS-1$

	// Data members.
	/** True if person is active observer. */
	private boolean isActiveObserver = false;
    	
	/** The scientific study the person is researching for. */
	private ScientificStudy study;
	/** The observatory the person is using. */
	private AstronomicalObservation observatory;
	/** The research assistant. */
	private Person researchAssistant;

	private ComputingJob compute;
	
	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 * @param scientificStudy Target building
	 */
	public ObserveAstronomicalObjects(Person person, ScientificStudy study) {
		// Use task constructor.
		super(NAME, person, false, IMPACT,
			  			100D + RandomUtil.getRandomDouble(100D));
						
		observatory = ObserveAstronomicalObjectsMeta.determineObservatory(person.getAssociatedSettlement());
		
		this.study = study;
		// Determine observatory to use.
		if (observatory != null) {
			// Walk to observatory building.
			walkToTaskSpecificActivitySpotInBuilding(observatory.getBuilding(),
													FunctionType.ASTRONOMICAL_OBSERVATION, false);
			if (!observatory.addObserver()) {
				endTask();
				return;
			}
			
			compute = new ComputingJob(person.getAssociatedSettlement(), getDuration(), NAME);
			isActiveObserver = true;
			
			// Initialize phase
			setPhase(OBSERVING);
		}
		else {
			logger.log(person, Level.SEVERE, 5000, 
					"Could not find the observatory.");
			endTask();
		}
	}
	
	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (OBSERVING.equals(getPhase())) {
			return observingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the observing phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	protected double observingPhase(double time) {

		if (person.getPhysicalCondition().computeFitnessLevel() < 2) {
			logger.log(person, Level.INFO, 0, 
				"Ended observing astronomical objects. Not feeling well.");
			endTask();
		}
		
		// If person is incapacitated, end task.
		if (person.getPerformanceRating() < 0.1) {
			endTask();
		}

		if (isDone()) {
    		logger.info(person, 30_000L, NAME + " - " 
    				+ Math.round((compute.getConsumed()) * 100.0)/100.0 
    				+ " CUs Used.");
			endTask();
			return time;
		}
		
		// Check for observatory malfunction.
		if (observatory != null && observatory.getBuilding().getMalfunctionManager().hasMalfunction()) {
			endTask();
		}

		// Check sunlight and end the task if sunrise
		if (!areConditionsSuitable(person.getAssociatedSettlement())) {
			endTask();
		}

		if (isDone() || ((getTimeCompleted() + time) > getDuration()) || compute.isCompleted()) {
        	// this task has ended
	  		logger.info(person, 30_000L, NAME + " - " 
    				+ Math.round(compute.getConsumed() * 100.0)/100.0 
    				+ " CUs Used.");
			endTask();
			return time;
		}
		
		compute.consumeProcessing(time, getMarsTime());
        
		// Add research work time to study.
		double observingTime = getEffectiveObservingTime(time);		
		if (study.getPrimaryResearcher().equals(person)) {
			study.addPrimaryResearchWorkTime(observingTime);
			if (study.isPrimaryResearchCompleted()) {
				logger.log(person, Level.INFO, 0, 
						"Just spent " 
						+ Math.round(study.getPrimaryResearchWorkTimeCompleted() *10.0)/10.0
						+ " millisols to complete a primary research using " + person.getLocationTag().getImmediateLocation());				
				endTask();
			}
		}
		else {
			study.addCollaborativeResearchWorkTime(person, observingTime);
			if (study.isCollaborativeResearchCompleted(person)) {
				logger.log(person, Level.INFO, 0, 
						"Just spent " 
						+ Math.round(study.getCollaborativeResearchWorkTimeCompleted(person) *10.0)/10.0
						+ " millisols to complete a collaborative research using " + person.getLocationTag().getImmediateLocation());
				endTask();
			}
		}

		// Add experience
		addExperience(observingTime);

		// Check for lab accident.
		checkForAccident(observatory.getBuilding(), time, 0.002);

		return 0D;
	}

	/**
	 * Are the conditions suitable to use the astronomy. Must be low light and no dust storm.
	 * @param target
	 * @return
	 */
	public static boolean areConditionsSuitable(Settlement target) {
		double sunlight = surfaceFeatures.getSolarIrradiance(target.getCoordinates());
		return ((sunlight < 12) && (target.getDustStorm() == null));
	}

	/**
	 * Gets the effective observing time based on the person's astronomy skill.
	 * 
	 * @param time the real amount of time (millisol) for observing.
	 * @return the effective amount of time (millisol) for observing.
	 */
	private double getEffectiveObservingTime(double time) {
		// Determine effective observing time based on the astronomy skill.
		double observingTime = time;
		int astronomySkill = getEffectiveSkillLevel();
		if (astronomySkill == 0) {
			observingTime /= 2D;
		}
		if (astronomySkill > 1) {
			observingTime += observingTime * (.2D * astronomySkill);
		}

		// Modify by tech level of observatory.
		int techLevel = observatory.getTechnologyLevel();
		if (techLevel > 0) {
			observingTime *= techLevel;
		}

		// If research assistant, modify by assistant's effective skill.
		if (hasResearchAssistant()) {
			SkillManager manager = researchAssistant.getSkillManager();
			int assistantSkill = manager.getEffectiveSkillLevel(ScienceType.ASTRONOMY.getSkill());
			if (astronomySkill > 0) {
				observingTime *= 1D + ((double) assistantSkill / (double) astronomySkill);
			}
		}

		return observingTime;
	}

	
	/**
	 * Release Observatory
	 */
	@Override
	protected void clearDown() {

		// Remove person from observatory so others can use it.
		if ((observatory != null) && isActiveObserver) {
			observatory.removeObserver();
			isActiveObserver = false;
		}

		super.clearDown();
	}

	@Override
	public ScienceType getResearchScience() {
		return ScienceType.ASTRONOMY;
	}

	@Override
	public Person getResearcher() {
		return person;
	}

	@Override
	public boolean hasResearchAssistant() {
		return (researchAssistant != null);
	}

	@Override
	public Person getResearchAssistant() {
		return researchAssistant;
	}

	@Override
	public void setResearchAssistant(Person researchAssistant) {
		this.researchAssistant = researchAssistant;
	}
}
