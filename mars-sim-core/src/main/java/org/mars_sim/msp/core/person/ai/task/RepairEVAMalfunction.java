/**
 * Mars Simulation Project
 * RepairEVAMalfunction.java
 * @version 3.1.0 2017-03-24
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.robot.RoboticAttributeManager;
import org.mars_sim.msp.core.robot.RoboticAttributeType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The RepairEVAMalfunction class is a task to repair a malfunction requiring an
 * EVA.
 */
public class RepairEVAMalfunction extends EVAOperation implements Repair, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(RepairEVAMalfunction.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.repairEVAMalfunction"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase REPAIRING = new TaskPhase(Msg.getString("Task.phase.repairing")); //$NON-NLS-1$

	// Data members
	/** The malfunctionable entity being repaired. */
	private Malfunctionable entity;

	/** The malfunction to be repaired. */
	private Malfunction malfunction;

	/** True if repairing the EVA part of the malfunction. */
	private boolean isEVAMalfunction;

	/** The container unit the person started the mission in. */
	private Unit containerUnit;

	public RepairEVAMalfunction(Person person) {
		super(NAME, person, true, 25);

		containerUnit = person.getTopContainerUnit();

		if (!(containerUnit instanceof MarsSurface)) {
			// Get the malfunctioning entity.
			entity = getEVAMalfunctionEntity(person);
			
			if (entity != null) {
				malfunction = getMalfunction(person, entity);
				
				if (malfunction != null) {
					isEVAMalfunction = malfunction.needEVARepair();
		
					setDescription(Msg.getString("Task.description.repairEVAMalfunction.detail", malfunction.getName(),
							entity.getNickName())); // $NON-NLS-1$
		
					// Determine location for repairing malfunction.
					Point2D malfunctionLoc = determineMalfunctionLocation();
					setOutsideSiteLocation(malfunctionLoc.getX(), malfunctionLoc.getY());
					
					if (!isDone()) {
			            if (person.isInside()) {
			            	setPhase(WALK_TO_OUTSIDE_SITE);
			            }
					}				
			
					String chief = malfunction.getChiefRepairer(3);
					String deputy = malfunction.getDeputyRepairer(3);

					if (chief == null || chief.equals("")) {
						LogConsolidated.log(Level.INFO, 0, sourceName,
								"[" + entity.getLocale() + "] " + person 
								+ " was appointed as the chief repairer handling the EVA Repair for '" 
								+ malfunction.getName() + "' on "
								+ entity.getUnit());
						 malfunction.setChiefRepairer(3, person.getName());						
					}
					else if (deputy == null || deputy.equals("")) {
						LogConsolidated.log(Level.INFO, 0, sourceName,
								"[" + entity.getLocale() + "] " + person 
								+ " was appointed as the deputy repairer handling the EVA Repair for '" 
								+ malfunction.getName() + "' on "
								+ entity.getUnit());
						malfunction.setDeputyRepairer(3, person.getName());
					}
					
					// Initialize phase
					addPhase(REPAIRING);
					
//					logger.info(person.getName() + " started the RepairEVAMalfunction task.");
				}
				else {
					endTask();
				}
				
			} 
			else {
				endTask();
			}
		}
		else 
			endTask();
	}


	public static Malfunctionable getEVAMalfunctionEntity(Person person) {
		Malfunctionable result = null;

		Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
		while (i.hasNext() && (result == null)) {
			Malfunctionable entity = i.next();
			if (getMalfunction(person, entity) != null) {
				result = entity;
			}
			MalfunctionManager manager = entity.getMalfunctionManager();

			// Check if entity has any EVA malfunctions.
			Iterator<Malfunction> j = manager.getEVAMalfunctions().iterator();
			while (j.hasNext() && (result == null)) {
				Malfunction malfunction = j.next();
				try {
					if (RepairEVAMalfunction.hasRepairPartsForMalfunction(person, person.getTopContainerUnit(),
							malfunction)) {
						result = entity;
					}
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}

			// Check if entity requires an EVA and has any normal malfunctions.
//			if ((result == null) && requiresEVA(person, entity)) {
//				Iterator<Malfunction> k = manager.getGeneralMalfunctions().iterator();
//				while (k.hasNext() && (result == null)) {
//					Malfunction malfunction = k.next();
//					try {
//						if (RepairMalfunction.hasRepairPartsForMalfunction(person, malfunction)) {
//							result = entity;
//						}
//					} catch (Exception e) {
//						e.printStackTrace(System.err);
//					}
//				}
//			}
		}

		return result;
	}

	/**
	 * Check if a malfunctionable entity requires an EVA to repair.
	 * 
	 * @param person the person doing the repair.
	 * @param entity the entity with a malfunction.
	 * @return true if entity requires an EVA repair.
	 */
	public static boolean hasEVA(Person person, Malfunctionable entity) {

		boolean result = false;

		if (entity instanceof Vehicle) {
			// Requires EVA repair on outside vehicles that the person isn't inside.
			Vehicle vehicle = (Vehicle) entity;
			boolean outsideVehicle = BuildingManager.getBuilding(vehicle) == null;
			boolean personNotInVehicle = !vehicle.getInventory().containsUnit(person);
			if (outsideVehicle && personNotInVehicle) {
				result = true;
			}
		} 
		
		else if (entity instanceof Building) {
			// Note: a building always has external structures that need EVA repair
			result = true;			
//			// Requires EVA repair on uninhabitable buildings.
//			Building building = (Building) entity;
//			if (!building.hasFunction(FunctionType.LIFE_SUPPORT)) {
//				result = true;
//			}
		}

		return result;
	}

	public static boolean hasEVA(Malfunctionable entity) {

		boolean result = false;

		if (entity instanceof Vehicle) {
			// Requires EVA repair on outside vehicles that the person isn't inside.
			Vehicle vehicle = (Vehicle) entity;
			boolean outsideVehicle = BuildingManager.getBuilding(vehicle) == null;
			if (outsideVehicle) {
				result = true;
			}
		} else if (entity instanceof Building) {
			// Note: a building always has external structures that need EVA repair
			result = true;			
//			// Requires EVA repair on uninhabitable buildings.
//			Building building = (Building) entity;
//			if (!building.hasFunction(FunctionType.LIFE_SUPPORT)) {
//				result = true;
//			}
		}

		return result;
	}
	
	/**
	 * Gets a reparable malfunction requiring an EVA for a given entity.
	 * 
	 * @param person the person to repair.
	 * @param entity the entity with a malfunction.
	 * @return malfunction requiring an EVA repair or null if none found.
	 */
	public static Malfunction getMalfunction(Person person, Malfunctionable entity) {

		Malfunction result = null;

		MalfunctionManager manager = entity.getMalfunctionManager();

		// Check if entity has any EVA malfunctions.
		Iterator<Malfunction> j = manager.getEVAMalfunctions().iterator();
		while (j.hasNext() && (result == null)) {
			Malfunction malfunction = j.next();
			try {
				if (RepairEVAMalfunction.hasRepairPartsForMalfunction(person, person.getTopContainerUnit(),
						malfunction)) {
					result = malfunction;
				}
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}

		// Check if entity requires an EVA and has any normal malfunctions.
		if ((result == null) && hasEVA(person, entity)) {
			Iterator<Malfunction> k = manager.getGeneralMalfunctions().iterator();
			while (k.hasNext() && (result == null)) {
				Malfunction malfunction = k.next();
				try {
					if (RepairMalfunction.hasRepairPartsForMalfunction(person, malfunction)) {
						result = malfunction;
					}
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		}

		return result;
	}


//	/**
//	 * Checks if a malfunction requires EVA repair.
//	 * 
//	 * @param malfunction the malfunction.
//	 * @return true if malfunction requires EVA repair.
//	 */
//	private boolean canRepairEVA(Malfunction malfunction) {
//
//		boolean result = false;
//
//		if (!malfunction.isEVARepairDone()) {
//			result = true;
//		}
//
//		return result;
//	}

	/**
	 * Checks if there are enough repair parts at person's location to fix the
	 * malfunction.
	 * 
	 * @param person        the person checking.
	 * @param containerUnit the unit the person is doing an EVA from.
	 * @param malfunction   the malfunction.
	 * @return true if enough repair parts to fix malfunction.
	 */
	public static boolean hasRepairPartsForMalfunction(Person person, Unit containerUnit, Malfunction malfunction) {

		if (person == null)
			throw new IllegalArgumentException("person is null");

		return hasRepairParts(containerUnit, malfunction);
	}

	public static boolean hasRepairPartsForMalfunction(Settlement settlement, Malfunction malfunction) {

		return hasRepairParts(settlement, malfunction);
	}

	public static boolean hasRepairParts(Unit containerUnit, Malfunction malfunction) {

		boolean result = true;

		if (containerUnit == null)
			throw new IllegalArgumentException("containerUnit is null");

		if (malfunction == null)
			throw new IllegalArgumentException("malfunction is null");

		Inventory inv = containerUnit.getInventory();

		Map<Integer, Integer> repairParts = malfunction.getRepairParts();
		Iterator<Integer> i = repairParts.keySet().iterator();
		while (i.hasNext() && result) {
			Integer part = i.next();
			int number = repairParts.get(part);
			if (inv.getItemResourceNum(part) < number) {
				inv.addItemDemand(part, number);
				result = false;
			}
		}

		return result;
	}

	/**
	 * Determine location to repair malfunction.
	 * 
	 * @return location.
	 */
	private Point2D determineMalfunctionLocation() {

		Point2D.Double newLocation = new Point2D.Double(0D, 0D);

		if (entity instanceof LocalBoundedObject) {
			LocalBoundedObject bounds = (LocalBoundedObject) entity;
			boolean goodLocation = false;
			for (int x = 0; (x < 50) && !goodLocation; x++) {
				Point2D.Double boundedLocalPoint = LocalAreaUtil.getRandomExteriorLocation(bounds, 1D);
				newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(), boundedLocalPoint.getY(),
						bounds);

				if (person != null) {
					goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(),
							person.getCoordinates());
				} else if (robot != null) {
					goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(),
							robot.getCoordinates());
				}

			}
		}

		return newLocation;
	}

	@Override
	protected TaskPhase getOutsideSitePhase() {
		return REPAIRING;
	}

	@Override
	protected double performMappedPhase(double time) {

		time = super.performMappedPhase(time);

		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (REPAIRING.equals(getPhase())) {
			return repairMalfunctionPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Gets a malfunctional entity with a normal malfunction for a user.
	 * 
	 * @return malfunctional entity.
	 */
	private static boolean hasMalfunction(Person person, Malfunctionable entity) {
		boolean result = false;

		if (entity.getMalfunctionManager().hasMalfunction())
			return true;
		
		return result;
	}

	
	/**
	 * Perform the repair malfunction phase of the task.
	 * 
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 */
	private double repairMalfunctionPhase(double time) {
//		logger.info(person + "::repairMalfunctionPhase   time :" + time);
		
		if (isDone()) {
            if (person.isOutside()) {
            	setPhase(WALK_BACK_INSIDE);
            }
            else if (person.isInside()) {
        		endTask();
            }
            return time;
		}
		
		// Check for radiation exposure during the EVA operation.
		if (person.isOutside() && isRadiationDetected(time)) {
			setPhase(WALK_BACK_INSIDE);
			return time;
		}

		if (person != null) {
			// Check if there are no more malfunctions.
			if (!hasMalfunction(person, entity)) {
				setPhase(WALK_BACK_INSIDE);
				return time;
			}
		}
		
		if (person.isOutside() && (shouldEndEVAOperation() || addTimeOnSite(time))) {
			setPhase(WALK_BACK_INSIDE);
			return time;
		}

		double workTime = 0;

		if (person != null) {
			workTime = time;
		} else if (robot != null) {
			// A robot moves slower than a person and incurs penalty on workTime
			workTime = time / 2;
		}

		// Determine effective work time based on "Mechanic" skill.
		int mechanicSkill = 0;

		if (person != null)
			mechanicSkill = person.getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);

		else if (robot != null)
			mechanicSkill = robot.getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);

		if (mechanicSkill == 0)
			workTime /= 2;
		if (mechanicSkill > 1)
			workTime += workTime * (.2D * mechanicSkill);

//		logger.info(person + " workTime: " + workTime);
		
		if (person != null) {
			if (hasRepairPartsForMalfunction(person, containerUnit, malfunction)) {
				Map<Integer, Integer> parts = new HashMap<>(malfunction.getRepairParts());
				Iterator<Integer> j = parts.keySet().iterator();
				// Add repair parts if necessary.
				Inventory inv = containerUnit.getInventory();
				while (j.hasNext()) {
					Integer id = j.next();
					int number = parts.get(id);
					inv.retrieveItemResources(id, number);
					malfunction.repairWithParts(id, number, inv);
//					logger.info(person + " repairWithParts: " + id);
				}
			} else {
				setPhase(WALK_BACK_INSIDE);
				return time;
			}

		}

		// Add EVA work to malfunction.
		double workTimeLeft = 0D;
		if (isEVAMalfunction && !malfunction.isEVARepairDone() ) {
			workTimeLeft = malfunction.addEVAWorkTime(workTime, person.getName());
//			logger.info(person + " addEVAWorkTime() : " + workTimeLeft);
		}
		
		// Add experience points
		addExperience(time);

		// Check if an accident happens during repair.
		checkForAccident(time);

		// Check if there are no more malfunctions.
		if (isEVAMalfunction && malfunction.needEVARepair() && malfunction.isEVARepairDone()) {
			LogConsolidated.log(Level.INFO, 1_000, sourceName,
				"[" + person.getLocationTag().getLocale() + "] " + person.getName()
					+ " wrapped up the EVA Repair of " + malfunction.getName() 
					+ " in "+ entity + " (" + Math.round(malfunction.getCompletedEVAWorkTime()*10.0)/10.0 + " millisols spent).");
            if (person.isOutside()) {
            	setPhase(WALK_BACK_INSIDE);
            	return time;
            }
            else if (person.isInside()) {
        		endTask();
            }
		}
			
//		logger.info(person + "::repairMalfunctionPhase   workTimeLeft : " + workTimeLeft);
		return workTimeLeft;
	}

	@Override
	protected void addExperience(double time) {

		// Add experience to "EVA Operations" skill.
		// (1 base experience point per 100 millisols of time spent)
		double evaExperience = time / 100D;
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

		// If phase is repair malfunction, add experience to mechanics skill.
		if (REPAIRING.equals(getPhase())) {
			// 1 base experience point per 20 millisols of collection time spent.
			// Experience points adjusted by person's "Experience Aptitude" attribute.
			double mechanicsExperience = time / 20D;
			mechanicsExperience += mechanicsExperience * experienceAptitudeModifier;

			if (person != null)
				person.getSkillManager().addExperience(SkillType.MECHANICS, mechanicsExperience, time);
			else if (robot != null)
				robot.getSkillManager().addExperience(SkillType.MECHANICS, mechanicsExperience, time);

		}
	}

	@Override
	public Malfunctionable getEntity() {
		return entity;
	}

	@Override
	public int getEffectiveSkillLevel() {
		SkillManager manager = null;
		if (person != null)
			manager = person.getSkillManager();
		else if (robot != null)
			manager = robot.getSkillManager();

		int EVAOperationsSkill = manager.getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
		int mechanicsSkill = manager.getEffectiveSkillLevel(SkillType.MECHANICS);
		return (int) Math.round((double) (EVAOperationsSkill + mechanicsSkill) / 2D);
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(2);
		results.add(SkillType.EVA_OPERATIONS);
		results.add(SkillType.MECHANICS);
		return results;
	}

	@Override
	public void destroy() {
		super.destroy();

		entity = null;
	}
}