/**
 * Mars Simulation Project
 * StudyFieldSamples.java
 * @version 3.1.0 2018-12-22
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.mars.ExploredLocation;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.mars.MineralMap;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.Lab;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Research;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A task for studying collected field samples (rocks, etc).
 */
public class StudyFieldSamples extends Task implements ResearchScientificStudy, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(StudyFieldSamples.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.studyFieldSamples"); //$NON-NLS-1$

	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.1D;

	/** Task phases. */
	private static final TaskPhase STUDYING_SAMPLES = new TaskPhase(Msg.getString("Task.phase.studyingSamples")); //$NON-NLS-1$

	/** Mass (kg) of field sample to study. */
	public static final double SAMPLE_MASS = 1D;

	private static final double ESTIMATE_IMPROVEMENT_FACTOR = 5D;

	// Data members.
	/** The scientific study the person is researching for. */
	private ScientificStudy study;
	/** The laboratory the person is working in. */
	private Lab lab;
	/** The science that is being researched. */
	private ScienceType science;
	/** The lab's associated malfunction manager. */
	private MalfunctionManager malfunctions;
	/** The research assistant. */
	private Person researchAssistant;

	/**
	 * Constructor.
	 * 
	 * @param person {@link Person} the person performing the task.
	 */
	public StudyFieldSamples(Person person) {
		// Use Task constructor.
		super(NAME, person, true, false, STRESS_MODIFIER, true, 10D + RandomUtil.getRandomDouble(40D));

		// Determine study.
		study = determineStudy();
		if (study != null) {
			science = getScience(person, study);
			if (science != null) {
				lab = getLocalLab(person, science);
				if (lab != null) {
					addPersonToLab();
				} else {
					logger.info("lab could not be determined.");
					endTask();
				}
			} else {
				logger.info("science could not be determined");
				endTask();
			}
		} else {
			logger.info("study could not be determined");
			endTask();
		}

		// Check if person is in a moving rover.
		if (Vehicle.inMovingRover(person)) {
			endTask();
		}

		// Take field samples from inventory.
		if (!isDone()) {
			Unit container = person.getContainerUnit();
			if (!(container instanceof MarsSurface)) {
				Inventory inv = container.getInventory();
				double totalRockSampleMass = inv.getAmountResourceStored(ResourceUtil.rockSamplesID, false);
				if (totalRockSampleMass >= SAMPLE_MASS) {
					double fieldSampleMass = RandomUtil.getRandomDouble(SAMPLE_MASS * 2D);
					if (fieldSampleMass > totalRockSampleMass) {
						fieldSampleMass = totalRockSampleMass;
					}
					inv.retrieveAmountResource(ResourceUtil.rockSamplesID, fieldSampleMass);
				}
			}
		}

		// Initialize phase
		addPhase(STUDYING_SAMPLES);
		setPhase(STUDYING_SAMPLES);
	}

	@Override
	public FunctionType getLivingFunction() {
		return FunctionType.RESEARCH;
	}

	/**
	 * Gets all the sciences related to studying field samples.
	 * 
	 * @return list of sciences.
	 */
	public static List<ScienceType> getFieldSciences() {

		// Create list of possible sciences for studying field samples.
		List<ScienceType> fieldSciences = new ArrayList<ScienceType>(3);
		fieldSciences.add(ScienceType.AREOLOGY);
		fieldSciences.add(ScienceType.ASTRONOMY);
		fieldSciences.add(ScienceType.BIOLOGY);
		fieldSciences.add(ScienceType.CHEMISTRY);
		fieldSciences.add(ScienceType.METEOROLOGY);
		fieldSciences.add(ScienceType.PHYSICS);
		
		return fieldSciences;
	}

	/**
	 * Gets the crowding modifier for a researcher to use a given laboratory
	 * building.
	 * 
	 * @param researcher the researcher.
	 * @param lab        the laboratory.
	 * @return crowding modifier.
	 */
	public static double getLabCrowdingModifier(Person researcher, Lab lab) {
		double result = 1D;
		if (researcher.isInSettlement()) {
			Building labBuilding = ((Research) lab).getBuilding();
			if (labBuilding != null) {
				result *= Task.getCrowdingProbabilityModifier(researcher, labBuilding);
				result *= Task.getRelationshipModifier(researcher, labBuilding);
			}
		}
		return result;
	}

	/**
	 * Determines the scientific study that will be researched.
	 * 
	 * @return study or null if none available.
	 */
	private ScientificStudy determineStudy() {
		ScientificStudy result = null;

		List<ScientificStudy> possibleStudies = new ArrayList<ScientificStudy>();

		// Create list of possible sciences for studying field samples.
		List<ScienceType> fieldSciences = getFieldSciences();

		// Add primary study if appropriate science and in research phase.
//		ScientificStudyManager manager = Simulation.instance().getScientificStudyManager();
		ScientificStudy primaryStudy = scientificStudyManager.getOngoingPrimaryStudy(person);
		if (primaryStudy != null) {
			if (ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase())
					&& !primaryStudy.isPrimaryResearchCompleted()) {
				if (fieldSciences.contains(primaryStudy.getScience())) {
					// Primary study added twice to double chance of random selection.
					possibleStudies.add(primaryStudy);
					possibleStudies.add(primaryStudy);
				}
			}
		}

		// Add all collaborative studies with appropriate sciences and in research
		// phase.
		Iterator<ScientificStudy> i = scientificStudyManager.getOngoingCollaborativeStudies(person).iterator();
		while (i.hasNext()) {
			ScientificStudy collabStudy = i.next();
			if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase())
					&& !collabStudy.isCollaborativeResearchCompleted(person)) {
				ScienceType collabScience = collabStudy.getCollaborativeResearchers().get(person.getIdentifier());
				if (fieldSciences.contains(collabScience)) {
					possibleStudies.add(collabStudy);
				}
			}
		}

		// Randomly select study.
		if (possibleStudies.size() > 0) {
			int selected = RandomUtil.getRandomInt(possibleStudies.size() - 1);
			result = possibleStudies.get(selected);
		}

		return result;
	}

	/**
	 * Gets the field of science that the researcher is involved with in a study.
	 * 
	 * @param researcher the researcher.
	 * @param study      the scientific study.
	 * @return the field of science or null if researcher is not involved with
	 *         study.
	 */
	private static ScienceType getScience(Person researcher, ScientificStudy study) {
		ScienceType result = null;

		if (study.getPrimaryResearcher().equals(researcher)) {
			result = study.getScience();
		} else if (study.getCollaborativeResearchers().containsKey(researcher.getIdentifier())) {
			result = study.getCollaborativeResearchers().get(researcher.getIdentifier());
		}

		return result;
	}

	/**
	 * Gets a local lab for studying field samples.
	 * 
	 * @param person  the person checking for the lab.
	 * @param science the science to research.
	 * @return laboratory found or null if none.
	 */
	public static Lab getLocalLab(Person person, ScienceType science) {
		Lab result = null;

		if (person.isInSettlement()) {
			result = getSettlementLab(person, science);
		} else if (person.isInVehicle()) {
			result = getVehicleLab(person.getVehicle(), science);
		}

		return result;
	}

	/**
	 * Gets a settlement lab for studying field samples.
	 * 
	 * @param person  the person looking for a lab.
	 * @param science the science to research.
	 * @return a valid research lab.
	 */
	private static Lab getSettlementLab(Person person, ScienceType science) {
		Lab result = null;

		BuildingManager manager = person.getSettlement().getBuildingManager();
		List<Building> labBuildings = manager.getBuildings(FunctionType.RESEARCH);
		labBuildings = getSettlementLabsWithSpecialty(science, labBuildings);
		labBuildings = BuildingManager.getNonMalfunctioningBuildings(labBuildings);
		labBuildings = getSettlementLabsWithAvailableSpace(labBuildings);
		labBuildings = BuildingManager.getLeastCrowdedBuildings(labBuildings);

		if (labBuildings.size() > 0) {
			Map<Building, Double> labBuildingProbs = BuildingManager.getBestRelationshipBuildings(person, labBuildings);
			Building building = RandomUtil.getWeightedRandomObject(labBuildingProbs);
			result = (Research) building.getFunction(FunctionType.RESEARCH);
		}

		return result;
	}

	/**
	 * Gets a list of research buildings with available research space from a list
	 * of buildings with the research function.
	 * 
	 * @param buildingList list of buildings with research function.
	 * @return research buildings with available lab space.
	 */
	private static List<Building> getSettlementLabsWithAvailableSpace(List<Building> buildingList) {
		List<Building> result = new ArrayList<Building>();

		Iterator<Building> i = buildingList.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Research lab = building.getResearch();
			if (lab.getResearcherNum() < lab.getLaboratorySize()) {
				result.add(building);
			}
		}

		return result;
	}

	/**
	 * Gets a list of research buildings with a given science specialty from a list
	 * of buildings with the research function.
	 * 
	 * @param science      the science specialty.
	 * @param buildingList list of buildings with research function.
	 * @return research buildings with science specialty.
	 */
	private static List<Building> getSettlementLabsWithSpecialty(ScienceType science, List<Building> buildingList) {
		List<Building> result = new ArrayList<Building>();

		Iterator<Building> i = buildingList.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Research lab = building.getResearch();
			if (lab.hasSpecialty(science)) {
				result.add(building);
			}
		}

		return result;
	}

	/**
	 * Gets an available lab in a vehicle. Returns null if no lab is currently
	 * available.
	 * 
	 * @param vehicle the vehicle
	 * @param science the science to research.
	 * @return available lab
	 */
	private static Lab getVehicleLab(Vehicle vehicle, ScienceType science) {

		Lab result = null;

		if (vehicle instanceof Rover) {
			Rover rover = (Rover) vehicle;
			if (rover.hasLab()) {
				Lab lab = rover.getLab();
				boolean availableSpace = (lab.getResearcherNum() < lab.getLaboratorySize());
				boolean speciality = lab.hasSpecialty(science);
				boolean malfunction = (rover.getMalfunctionManager().hasMalfunction());
				if (availableSpace && speciality && !malfunction)
					result = lab;
			}
		}

		return result;
	}

	/**
	 * Adds a person to a lab.
	 */
	private void addPersonToLab() {

		try {
			if (person.isInSettlement()) {
				Building labBuilding = ((Research) lab).getBuilding();

				// Walk to lab building.
				walkToActivitySpotInBuilding(labBuilding, false);
				lab.addResearcher();
				malfunctions = labBuilding.getMalfunctionManager();
			} else if (person.isInVehicle()) {

				// Walk to lab internal location in rover.
				walkToLabActivitySpotInRover((Rover) person.getVehicle(), false);
				lab.addResearcher();
				malfunctions = person.getVehicle().getMalfunctionManager();
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "addPersonToLab(): " + e.getMessage());
		}
	}

	@Override
	protected void addExperience(double time) {
		// Add experience to relevant science skill
		// (1 base experience point per 10 millisols of research time)
		// Experience points adjusted by person's "Academic Aptitude" attribute.
		double newPoints = time / 10D;
		int academicAptitude = person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
		newPoints += newPoints * ((double) academicAptitude - 50D) / 100D;
		newPoints *= getTeachingExperienceModifier();
		person.getSkillManager().addExperience(science.getSkill(), newPoints, time);
	}

	/**
	 * Gets the effective research time based on the person's science skill.
	 * 
	 * @param time the real amount of time (millisol) for research.
	 * @return the effective amount of time (millisol) for research.
	 */
	private double getEffectiveResearchTime(double time) {
		// Determine effective research time based on the science skill.
		double researchTime = time;
		int scienceSkill = getEffectiveSkillLevel();
		if (scienceSkill == 0) {
			researchTime /= 2D;
		} else if (scienceSkill > 1) {
			researchTime += researchTime * (.2D * scienceSkill);
		}

		// Modify by tech level of laboratory.
		int techLevel = lab.getTechnologyLevel();
		if (techLevel > 0) {
			researchTime *= techLevel;
		}

		// If research assistant, modify by assistant's effective skill.
		if (hasResearchAssistant()) {
			SkillManager manager = researchAssistant.getSkillManager();
			int assistantSkill = manager.getEffectiveSkillLevel(science.getSkill());
			if (scienceSkill > 0) {
				researchTime *= 1D + ((double) assistantSkill / (double) scienceSkill);
			}
		}

		return researchTime;
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(1);
		results.add(science.getSkill());
		return results;
	}

	@Override
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getSkillManager();
		return manager.getEffectiveSkillLevel(science.getSkill());
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (STUDYING_SAMPLES.equals(getPhase())) {
			return studyingSamplesPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the studying samples phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double studyingSamplesPhase(double time) {
		// If person is incapacitated, end task.
		if (person.getPerformanceRating() == 0D) {
			endTask();
		}

		// Check for laboratory malfunction.
		if (malfunctions.hasMalfunction()) {
			endTask();
		}

		// Check if research in study is completed.
		boolean isPrimary = study.getPrimaryResearcher().equals(person);


		// Check if person is in a moving rover.
		if (Vehicle.inMovingRover(person)) {
			endTask();
		}

		if (isDone()) {
			return time;
		}

		// Add research work time to study.
		double researchTime = getEffectiveResearchTime(time);
		if (isPrimary) {
			study.addPrimaryResearchWorkTime(researchTime);
		} else {
			study.addCollaborativeResearchWorkTime(person, researchTime);
		}

		if (isPrimary) {
			if (study.isPrimaryResearchCompleted()) {
				LogConsolidated.log(Level.INFO, 0, sourceName,
						"[" + person.getLocationTag().getLocale() + "] " + person.getName() + " just spent "
								+ Math.round(study.getPrimaryResearchWorkTimeCompleted() * 10.0) / 10.0
								+ " millisols in studying the field samples for a primary research study in "
								+ study.getScience().getName() + " in "
								+ person.getLocationTag().getImmediateLocation());
				endTask();
			}
		} else {
			if (study.isCollaborativeResearchCompleted(person)) {
				LogConsolidated.log(Level.INFO, 0, sourceName,
						"[" + person.getLocationTag().getLocale() + "] " + person.getName() + " just spent "
								+ Math.round(study.getCollaborativeResearchWorkTimeCompleted(person) * 10.0) / 10.0
								+ " millisols in studying the field samples for a collaborative research study in " 
								+ study.getScience().getName() + " in "
								+ person.getLocationTag().getImmediateLocation());
				endTask();
			}
		}
		
		// If areology science, improve explored site mineral concentration estimates.
		if (ScienceType.AREOLOGY == science)
			improveMineralConcentrationEstimates(time);

		// Add experience
		addExperience(researchTime);

		// Check for lab accident.
		checkForAccident(time);

		return 0D;
	}

	/**
	 * Check for accident in laboratory.
	 * 
	 * @param time the amount of time researching (in millisols)
	 */
	private void checkForAccident(double time) {

		double chance = .005D;

		// Science skill modification.
		int skill = person.getSkillManager().getEffectiveSkillLevel(science.getSkill());
		if (skill <= 3) {
			chance *= (4 - skill);
		} else {
			chance /= (skill - 2);
		}

		Malfunctionable entity = null;
		if (lab instanceof Research) {
			entity = ((Research) lab).getBuilding();
		} else {
			entity = person.getVehicle();
		}

		if (entity != null) {

			// Modify based on the entity's wear condition.
			chance *= entity.getMalfunctionManager().getWearConditionAccidentModifier();

			if (RandomUtil.lessThanRandPercent(chance * time)) {
//                logger.info("[" + person.getLocationTag().getShortLocationName() +  "] " + person.getName() + " has a lab accident while studying field samples.");
				entity.getMalfunctionManager().createASeriesOfMalfunctions(person);
			}
		}
	}

	/**
	 * Improve the mineral concentration estimates of an explored site.
	 * 
	 * @param time the amount of time available (millisols).
	 */
	private void improveMineralConcentrationEstimates(double time) {
		double probability = (time / 1000D) * getEffectiveSkillLevel() * ESTIMATE_IMPROVEMENT_FACTOR;
		if (RandomUtil.getRandomDouble(1.0D) <= probability) {

			// Determine explored site to improve estimations.
			ExploredLocation site = determineExplorationSite();
			if (site != null) {
				MineralMap mineralMap = surfaceFeatures.getMineralMap();
				Map<String, Double> estimatedMineralConcentrations = site.getEstimatedMineralConcentrations();
				Iterator<String> i = estimatedMineralConcentrations.keySet().iterator();
				while (i.hasNext()) {
					String mineralType = i.next();
					double actualConcentration = mineralMap.getMineralConcentration(mineralType, site.getLocation());
					double estimatedConcentration = estimatedMineralConcentrations.get(mineralType);
					double estimationDiff = Math.abs(actualConcentration - estimatedConcentration);
					double estimationImprovement = RandomUtil.getRandomDouble(1D * getEffectiveSkillLevel());
					if (estimationImprovement > estimationDiff) {
						estimationImprovement = estimationDiff;
					}
					if (estimatedConcentration < actualConcentration) {
						estimatedConcentration += estimationImprovement;
					} else {
						estimatedConcentration -= estimationImprovement;
					}
					estimatedMineralConcentrations.put(mineralType, estimatedConcentration);
				}

				// Add to site mineral concentration estimation improvement number.
				site.addEstimationImprovement();
				LogConsolidated.log(Level.FINE, 5000, sourceName, "[" + person.getLocationTag().getLocale() + "] "
						+ person.getName() + " was studying field samples at " + site.getLocation().getFormattedString() 
						+ ". Estimation Improvement: "
						+ site.getNumEstimationImprovement() + ".");
//				logger.fine("Explored site " + site.getLocation().getFormattedString() + " estimation improvement: "
//						+ site.getNumEstimationImprovement() + " from studying field samples");
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
		if ((mission != null) && (mission instanceof Exploration)) {
			Exploration explorationMission = (Exploration) mission;
			List<ExploredLocation> exploredSites = explorationMission.getExploredSites();
			if (exploredSites.size() > 0) {
				int siteIndex = RandomUtil.getRandomInt(exploredSites.size() - 1);
				ExploredLocation location = exploredSites.get(siteIndex);
				if (!location.isMined() && !location.isReserved()) {
					result = location;
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
		ExploredLocation result = null;

		Settlement settlement = person.getAssociatedSettlement();
		if (settlement != null) {
			List<ExploredLocation> settlementExploredLocations = new ArrayList<ExploredLocation>();
			List<ExploredLocation> allExploredLocations = surfaceFeatures.getExploredLocations();
			Iterator<ExploredLocation> i = allExploredLocations.iterator();
			while (i.hasNext()) {
				ExploredLocation location = i.next();
				if (settlement.equals(location.getSettlement()) && !location.isMined() && !location.isReserved()) {
					settlementExploredLocations.add(location);
				}
			}

			if (settlementExploredLocations.size() > 0) {
				int siteIndex = RandomUtil.getRandomInt(settlementExploredLocations.size() - 1);
				result = settlementExploredLocations.get(siteIndex);
			}
		}

		return result;
	}

	@Override
	public void endTask() {
		super.endTask();

		// Remove person from lab so others can use it.
		try {
			if (lab != null) {
				lab.removeResearcher();
			}
		} catch (Exception e) {
		}
	}

	@Override
	public ScienceType getResearchScience() {
		return science;
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

	@Override
	public void destroy() {
		super.destroy();

		study = null;
		lab = null;
		science = null;
		malfunctions = null;
		researchAssistant = null;
	}
}