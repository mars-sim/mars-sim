/**
 * Mars Simulation Project
 * CollectMinedMinerals.java
 * @version 3.1.0 2017-09-13
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.RoboticAttributeManager;
import org.mars_sim.msp.core.robot.RoboticAttributeType;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * Task for collecting minerals that have been mined at a site.
 */
public class CollectMinedMinerals extends EVAOperation implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(CollectMinedMinerals.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.collectMinedMinerals"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase COLLECT_MINERALS = new TaskPhase(Msg.getString("Task.phase.collectMinerals")); //$NON-NLS-1$

	/** Rate of mineral collection (kg/millisol). */
	private static final double MINERAL_COLLECTION_RATE = 10D;

	/** The average labor time it takes to find the minerel */
	public static final double LABOR_TIME = 50D;
	
	// Data members
	private Rover rover; // Rover used.
	protected AmountResource mineralType;

	private static int oxygenID = ResourceUtil.oxygenID;
	private static int waterID = ResourceUtil.waterID;

	/**
	 * Constructor
	 * 
	 * @param person      the person performing the task.
	 * @param rover       the rover used for the EVA operation.
	 * @param mineralType the type of mineral to collect.
	 */
	public CollectMinedMinerals(Person person, Rover rover, AmountResource mineralType) {

		// Use EVAOperation parent constructor.
		super(NAME, person, true, LABOR_TIME + RandomUtil.getRandomDouble(10D) - RandomUtil.getRandomDouble(10D));

		// Initialize data members.
		this.rover = rover;
		this.mineralType = mineralType;

		// Determine location for collection site.
		Point2D collectionSiteLoc = determineCollectionSiteLocation();
		setOutsideSiteLocation(collectionSiteLoc.getX(), collectionSiteLoc.getY());

		// Take bags for collecting mined minerals.
		if (!hasBags()) {
			takeBag();

			// If bags are not available, end task.
			if (!hasBags()) {
				LogConsolidated.log(logger, Level.INFO, 5_000, sourceName, 
		        		"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
		        		+ " was not able to find more bags to collect mined minerals.", null);
				endTask();
			}
		}

		// Add task phases
		addPhase(COLLECT_MINERALS);
	}

	/**
	 * Determine location for the collection site.
	 * 
	 * @return site X and Y location outside rover.
	 */
	private Point2D determineCollectionSiteLocation() {

		Point2D newLocation = null;
		boolean goodLocation = false;
		for (int x = 0; (x < 5) && !goodLocation; x++) {
			for (int y = 0; (y < 10) && !goodLocation; y++) {

				double distance = RandomUtil.getRandomDouble(50D) + (x * 100D) + 50D;
				double radianDirection = RandomUtil.getRandomDouble(Math.PI * 2D);
				double newXLoc = rover.getXLocation() - (distance * Math.sin(radianDirection));
				double newYLoc = rover.getYLocation() + (distance * Math.cos(radianDirection));
				Point2D boundedLocalPoint = new Point2D.Double(newXLoc, newYLoc);

				newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(), boundedLocalPoint.getY(),
						rover);
				if (person != null)
					goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(),
							person.getCoordinates());
				else if (robot != null)
					goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(),
							robot.getCoordinates());

			}
		}

		return newLocation;
	}

	/**
	 * Checks if the person is carrying any bags.
	 * 
	 * @return true if carrying bags.
	 */
	private boolean hasBags() {
		boolean result = false;
		if (person != null)
			result = person.getInventory().containsUnitClass(Bag.class);
		else if (robot != null)
			result = robot.getInventory().containsUnitClass(Bag.class);
		return result;
	}

	/**
	 * Takes the most full bag from the rover.
	 * 
	 * @throws Exception if error taking bag.
	 */
	private void takeBag() {
		Bag bag = findMostFullBag(rover.getInventory(), mineralType);
		if (bag != null) {
			if (person != null) {
				if (person.getInventory().canStoreUnit(bag, false)) {
					bag.transfer(rover, person);
//					rover.getInventory().retrieveUnit(bag);
//					person.getInventory().storeUnit(bag);
				}
			} else if (robot != null) {
				if (robot.getInventory().canStoreUnit(bag, false)) {
					bag.transfer(rover, robot);
//					rover.getInventory().retrieveUnit(bag);
//					robot.getInventory().storeUnit(bag);
				}
			}
		}
	}

	/**
	 * Gets the most but not completely full bag of the resource in the rover.
	 * 
	 * @param inv          the inventory to look in.
	 * @param resourceType the resource for capacity.
	 * @return container.
	 */
	private static Bag findMostFullBag(Inventory inv, AmountResource resource) {
		Bag result = null;
		double leastCapacity = Double.MAX_VALUE;

		Iterator<Bag> i = inv.findAllBags().iterator();
		while (i.hasNext()) {
			Bag bag = i.next();
			double remainingCapacity = bag.getInventory().getAmountResourceRemainingCapacity(resource, true, false);

			if ((remainingCapacity > 0D) && (remainingCapacity < leastCapacity)) {
				result = bag;
				leastCapacity = remainingCapacity;
			}
		}

		return result;
	}

	@Override
	protected TaskPhase getOutsideSitePhase() {
		return COLLECT_MINERALS;
	}

	@Override
	protected double performMappedPhase(double time) {

		time = super.performMappedPhase(time);

		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (COLLECT_MINERALS.equals(getPhase())) {
			return collectMineralsPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Perform the collect minerals phase of the task.
	 * 
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 * @throws Exception if error collecting minerals.
	 */
	private double collectMineralsPhase(double time) {

		// Check for an accident during the EVA operation.
		checkForAccident(time);

		// Check for radiation exposure during the EVA operation.
		if (isRadiationDetected(time)) {
			setPhase(WALK_BACK_INSIDE);
			return .5 * time;
		}
		// Check if site duration has ended or there is reason to cut the collect
		// minerals phase short and return to the rover.
		if (shouldEndEVAOperation() || addTimeOnSite(time)) {
			setPhase(WALK_BACK_INSIDE);
			return .5 * time;
		}

		Mining mission = null;
		if (person != null)
			mission = (Mining) person.getMind().getMission();
		else if (robot != null)
			mission = (Mining) robot.getBotMind().getMission();

		double mineralsExcavated = mission.getMineralExcavationAmount(mineralType);
		double remainingPersonCapacity = 0;

		double roverRemainingCap = rover.getInventory().getAmountResourceRemainingCapacity(mineralType, true,
				false);
		
		double weight = 0;
		if (person != null)
			weight = person.getMass();
		else if (robot != null)
			weight = robot.getMass();
		
		if (roverRemainingCap < weight + 5) {
			setPhase(WALK_BACK_INSIDE);
			return .5 * time;
		}
			
		if (person != null)
			remainingPersonCapacity = person.getInventory().getAmountResourceRemainingCapacity(mineralType, true,
					false);
		else if (robot != null)
			remainingPersonCapacity = robot.getInventory().getAmountResourceRemainingCapacity(mineralType, true, false);

		double mineralsCollected = time * MINERAL_COLLECTION_RATE;

		// Modify collection rate by "Areology" skill.
		int areologySkill = 0;
		if (person != null)
			areologySkill = person.getSkillManager().getEffectiveSkillLevel(SkillType.AREOLOGY);
		else if (robot != null)
			areologySkill = robot.getSkillManager().getEffectiveSkillLevel(SkillType.AREOLOGY);
		if (areologySkill == 0)
			mineralsCollected /= 2D;
		if (areologySkill > 1)
			mineralsCollected += mineralsCollected * (.2D * areologySkill);

		if (mineralsCollected > remainingPersonCapacity)
			mineralsCollected = remainingPersonCapacity;
		if (mineralsCollected > mineralsExcavated)
			mineralsCollected = mineralsExcavated;

		// Add experience points
		addExperience(time);

		// Collect minerals.
		if (person != null) {
			person.getInventory().storeAmountResource(mineralType, mineralsCollected, true);
		}
		else if (robot != null) {
			robot.getInventory().storeAmountResource(mineralType, mineralsCollected, true);
		}
		
		mission.collectMineral(mineralType, mineralsCollected);
		
		if (((mineralsExcavated - mineralsCollected) <= 0D) || (mineralsCollected >= remainingPersonCapacity)) {
			setPhase(WALK_BACK_INSIDE);
		}

		return 0D;
	}

	@Override
	public void endTask() {

		// Unload bag to rover's inventory.
		Inventory pInv = null;
		if (person != null)
			pInv = person.getInventory();
		else if (robot != null)
			pInv = robot.getInventory();
		if (pInv.containsUnitClass(Bag.class)) {
			// Load bags in rover.
			Iterator<Bag> i = pInv.findAllBags().iterator();
			while (i.hasNext()) {
//				Bag bag = i.next();
				// Place this equipment within a rover outside on Mars
				i.next().transfer(pInv, rover);
//				pInv.retrieveUnit(bag);
//				rover.getInventory().storeUnit(bag);
			}
		}

		super.endTask();
	}

	/**
	 * Checks if a person can perform a CollectMinedMinerals task.
	 * 
	 * @param member      the member to perform the task
	 * @param rover       the rover the person will EVA from
	 * @param mineralType the resource to collect.
	 * @return true if person can perform the task.
	 */
	public static boolean canCollectMinerals(MissionMember member, Rover rover, AmountResource mineralType) {

		boolean result = false;

		if (member instanceof Person) {

			Person person = (Person) member;

			// Check if person can exit the rover.
			if (!ExitAirlock.canExitAirlock(person, rover.getAirlock()))
				return false;

			if (surfaceFeatures.getSolarIrradiance(person.getCoordinates()) <= 15D
				&& !surfaceFeatures.inDarkPolarRegion(person.getCoordinates())) {
					LogConsolidated.log(Level.FINE, 5_000, sourceName, 
			        		"[" + person.getLocationTag().getLocale() + "] " + person.getName()
			        			+ " ended mining minerals due to low light level outside at " 
			        				+ person.getCoordinates().getFormattedString());
					return false;
			}

			// Check if person's medical condition will not allow task.
			if (person.getPerformanceRating() < .5D)
				return false;

			// Checks if available bags with remaining capacity for resource.
			Bag bag = findMostFullBag(rover.getInventory(), mineralType);
			boolean bagAvailable = (bag != null);

			// Check if bag and full EVA suit can be carried by person or is too heavy.
			double carryMass = 0D;
			if (bag != null) {
				carryMass += bag.getMass();
			}

			EVASuit suit = rover.getInventory().findAnEVAsuit(); //(EVASuit) rover.getInventory().findUnitOfClass(EVASuit.class);
			if (suit != null) {
				carryMass += suit.getMass();
				carryMass += suit.getInventory().getAmountResourceRemainingCapacity(oxygenID, false, false);
				carryMass += suit.getInventory().getAmountResourceRemainingCapacity(waterID, false, false);
			}
			double carryCapacity = person.getInventory().getGeneralCapacity();
			boolean canCarryEquipment = (carryCapacity >= carryMass);

			result = (bagAvailable && canCarryEquipment);
		}

		return result;
	}

	@Override
	protected void addExperience(double time) {
		// Add experience to "EVA Operations" skill.
		// (1 base experience point per 100 millisols of time spent)
		double evaExperience = time / 100D;

		// Experience points adjusted by person's "Experience Aptitude" attribute.
		NaturalAttributeManager nManager = null;
		RoboticAttributeManager rManager = null;
		int experienceAptitude = 0;
		if (person != null) {
			nManager = person.getNaturalAttributeManager();
			experienceAptitude = nManager.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		} else if (robot != null) {
			rManager = robot.getRoboticAttributeManager();
			experienceAptitude = rManager.getAttribute(RoboticAttributeType.EXPERIENCE_APTITUDE);
		}

		double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
		evaExperience += evaExperience * experienceAptitudeModifier;
		evaExperience *= getTeachingExperienceModifier();
		if (person != null)
			person.getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience, time);
		else if (robot != null)
			robot.getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience, time);

		// If phase is collect minerals, add experience to areology skill.
		if (COLLECT_MINERALS.equals(getPhase())) {
			// 1 base experience point per 10 millisols of collection time spent.
			// Experience points adjusted by person's "Experience Aptitude" attribute.
			double areologyExperience = time / 10D;
			areologyExperience += areologyExperience * experienceAptitudeModifier;
			if (person != null)
				person.getSkillManager().addExperience(SkillType.AREOLOGY, areologyExperience, time);
			else if (robot != null)
				robot.getSkillManager().addExperience(SkillType.AREOLOGY, areologyExperience, time);
		}
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(2);
		results.add(SkillType.EVA_OPERATIONS);
		results.add(SkillType.AREOLOGY);
		return results;
	}

	@Override
	public int getEffectiveSkillLevel() {
		SkillManager manager = null;
		if (person != null)
			manager = person.getSkillManager();
		else if (robot != null)
			manager = robot.getSkillManager();
		int EVAOperationsSkill = manager.getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
		int areologySkill = manager.getEffectiveSkillLevel(SkillType.AREOLOGY);
		return (int) Math.round((double) (EVAOperationsSkill + areologySkill) / 2D);
	}

	@Override
	public void destroy() {
		super.destroy();

		rover = null;
		mineralType = null;
	}
}