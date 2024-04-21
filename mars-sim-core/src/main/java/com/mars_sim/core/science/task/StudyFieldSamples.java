/*
 * Mars Simulation Project
 * StudyFieldSamples.java
 * @date 2023-04-15
 * @author Scott Davis
 */
package com.mars_sim.core.science.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.environment.ExploredLocation;
import com.mars_sim.core.equipment.ResourceHolder;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.mission.Exploration;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.task.ExploreSite;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * A task for studying collected field samples (rocks, etc).
 */
public class StudyFieldSamples extends LabTask {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(StudyFieldSamples.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.studyFieldSamples"); //$NON-NLS-1$
	
	/** Task phases. */
	private static final TaskPhase STUDYING_SAMPLES = new TaskPhase(Msg.getString("Task.phase.studyingSamples")); //$NON-NLS-1$

	/** Mass (kg) of field sample to study. */
	public static final double SAMPLE_MASS = .5;

	private static final double ESTIMATE_IMPROVEMENT_FACTOR = 5D;


	public static final Set<ScienceType> FIELD_SCIENCES = Set.of(ScienceType.AREOLOGY,
												ScienceType.ASTRONOMY, ScienceType.BIOLOGY,
												ScienceType.CHEMISTRY, ScienceType.ENGINEERING,
												ScienceType.METEOROLOGY, ScienceType.PHYSICS);
	/**
	 * Create a Task to perform lab research. This will select the most appropirate Scientific Study for the Person
	 * and create an appropriate Task.
	 * @param person
	 * @return
	 */
	public static StudyFieldSamples createTask(Person person) {
		var study = determineStudy(person);
		if ((study != null) && (study.getContribution(person) != null)) {
			// Found a suitable study
			var impact = new ExperienceImpact(10D, NaturalAttributeType.ACADEMIC_APTITUDE,
										false, 0.1D,
							Set.of(study.getContribution(person).getSkill()));
			return new StudyFieldSamples(person, study, impact);
		}

		return null;
	}

	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 */
	private StudyFieldSamples(Person person, ScientificStudy study, ExperienceImpact impact) {
		// Use task constructor.
		super(NAME, person, study, impact, 10D + RandomUtil.getRandomDouble(50D),
		STUDYING_SAMPLES, "Task.description.studyFieldSamples");

	}

	/**
	 * Determines the scientific study that will be researched.
	 * 
	 * @return study or null if none available.
	 */
	private static ScientificStudy determineStudy(Person person) {
		List<ScientificStudy> possibleStudies = new ArrayList<>();

		// Add primary study if appropriate science and in research phase.
		ScientificStudy primaryStudy = person.getStudy();
		if ((primaryStudy != null) && ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase())
					&& !primaryStudy.isPrimaryResearchCompleted()
					&& FIELD_SCIENCES.contains(primaryStudy.getScience())) {
			// Primary study added twice to double chance of random selection.
			possibleStudies.add(primaryStudy);
			possibleStudies.add(primaryStudy);
		}

		// Add all collaborative studies with appropriate sciences and in research
		// phase.
		for(ScientificStudy collabStudy : person.getCollabStudies()) {
			if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase())
					&& !collabStudy.isCollaborativeResearchCompleted(person)) {
				ScienceType collabScience = collabStudy.getContribution(person);
				if (FIELD_SCIENCES.contains(collabScience)) {
					possibleStudies.add(collabStudy);
				}
			}
		}

		// Randomly select study.
		return RandomUtil.getRandomElement(possibleStudies);
	}

	/**
	 * Performs the studying samples as part of the lab work.
	 * @param time Time specit on lab reseach
	 */
	@Override
	protected void executeResearchActivity(double time) {
		// Take field samples from inventory.
		double mostStored = 0D;
		int bestID = 0;
		if (person.getContainerUnit() instanceof ResourceHolder rh) {
			for (int i: ResourceUtil.rockIDs) {
				double stored = rh.getAmountResourceStored(i);
				if (mostStored < stored) {
					mostStored = stored;
					bestID = i;
				}
			}
			if (mostStored < SAMPLE_MASS) {
				endTask();
			}

			double fieldSampleMass = RandomUtil.getRandomDouble(SAMPLE_MASS/20.0, SAMPLE_MASS/10.0);
			if (mostStored >= fieldSampleMass) {
				rh.retrieveAmountResource(bestID, fieldSampleMass);
				// Record the amount of rock samples being studied
				person.getAssociatedSettlement().addResourceCollected(bestID, fieldSampleMass);
			}
		}

		// If areology science, improve explored site mineral concentration estimates.
		if (ScienceType.AREOLOGY == getResearchScience())
			improveMineralConcentrationEstimates(time);
	}

	/**
	 * Improve the mineral concentration estimates of an explored site.
	 * 
	 * @param time the amount of time available (millisols).
	 */
	private void improveMineralConcentrationEstimates(double time) {
		ExploredLocation site = determineExplorationSite();
		if (site != null) {
			double probability = (time / 1000D) * getEffectiveSkillLevel() * ESTIMATE_IMPROVEMENT_FACTOR;
			if ((site.getNumEstimationImprovement() == 0) || (RandomUtil.getRandomDouble(1.0D) <= probability)) {
				ExploreSite.improveSiteEstimates(site, getEffectiveSkillLevel());

				logger.log(worker, Level.INFO, 5_000, "Studying field samples at " 
						+ site.getLocation().getFormattedString() 
						+ ". Estimation Improvement: "
						+ site.getNumEstimationImprovement());
			}
		}
	}

	/**
	 * Determines an exploration site to improve mineral concentration estimates.
	 * 
	 * @return exploration site or null if none.
	 */
	private ExploredLocation determineExplorationSite() {

		// Try to use an exploration mission site.
		ExploredLocation result = getExplorationMissionSite();

		// Try to use a site explored previously by the settlement.
		if (result == null) {
			result = getSettlementExploredSite();
		}

		return result;
	}

	/**
	 * Gets an exploration site that's been explored by the person's current
	 * exploration mission (if any).
	 * 
	 * @return exploration site or null if none.
	 */
	private ExploredLocation getExplorationMissionSite() {
		ExploredLocation result = null;

		Mission mission = person.getMind().getMission();
		if (mission instanceof Exploration explorationMission) {
			for(var location : explorationMission.getExploredSites()) {
				if (location.isMinable() && !location.isReserved()) {
					return location;
				}
			}
		}

		return result;
	}

	/**
	 * Gets an exploration site that was previously explored by the person's
	 * settlement.
	 * 
	 * @return exploration site or null if none.
	 */
	private ExploredLocation getSettlementExploredSite() {
		Settlement settlement = person.getAssociatedSettlement();
		if (settlement != null) {
			Set<ExploredLocation> allExploredLocations = surfaceFeatures.getAllRegionOfInterestLocations();
			List<ExploredLocation> settlementExploredLocations = allExploredLocations.stream()
						.filter(l -> (settlement.equals(l.getSettlement()) && l.isMinable() && !l.isReserved()))
						.toList();

			return RandomUtil.getRandomElement(settlementExploredLocations);
		}

		return null;
	}
}
