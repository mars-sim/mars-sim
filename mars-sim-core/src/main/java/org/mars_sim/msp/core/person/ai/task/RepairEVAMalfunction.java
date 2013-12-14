/**
 * Mars Simulation Project
 * RepairEVAMalfunction.java
 * @version 3.06 2013-12-12
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.*;

/**
 * The RepairEVAMalfunction class is a task to repair a malfunction requiring an EVA.
 */
public class RepairEVAMalfunction extends EVAOperation implements Repair, Serializable {

    // Phase names
    private static final String REPAIR_MALFUNCTION = "Repair Malfunction";
	
    // Data members
    private Malfunctionable entity; // The malfunctionable entity being repaired.
    private Airlock airlock; // The airlock to be used.
	
    /**
     * Constructs a RepairEVAMalfunction object.
     * @param person the person to perform the task
     * @throws Exception if error constructing task
     */
    public RepairEVAMalfunction(Person person) {
        super("Repairing EVA Malfunction", person);

        // Get the malfunctioning entity.
        entity = getEVAMalfunctionEntity(person, person.getTopContainerUnit());
        if (entity == null) endTask();
        
        // Get an available airlock.
        if (entity instanceof LocalBoundedObject) {
            LocalBoundedObject bounds = (LocalBoundedObject) entity;
            airlock = getClosestWalkableAvailableAirlock(person, bounds.getXLocation(), 
                    bounds.getYLocation());
        }
        else {
            airlock = getWalkableAvailableAirlock(person);
        }
        
        if (airlock == null) {
            endTask();
        }

        // Initialize phase
        addPhase(REPAIR_MALFUNCTION);

        // logger.info(person.getName() + " has started the RepairEVAMalfunction task.");
    }

    /**
     * Checks if the malfunctionable entity has a local EVA malfunction.
     * @param containerUnit the unit the person is doing an EVA from.
     * @return true if malfunction, false if none.
     * @throws Exception if error checking for EVA malfunction.
     */
    private static boolean hasEVAMalfunction(Person person, Unit containerUnit, 
            Malfunctionable entity) {
   
        boolean result = false;

        MalfunctionManager manager = entity.getMalfunctionManager();
        Iterator<Malfunction> i = manager.getEVAMalfunctions().iterator();
        while (i.hasNext() && !result) {
           	if (hasRepairPartsForMalfunction(person, containerUnit, i.next())) result = true;
        }

        return result;
    }
    
    /**
     * Gets a malfunctional entity with an EVA malfunction for a user.
     * @param person the person.
     * @param containerUnit the unit the person is doing an EVA from.
     * @return malfunctionable entity.
     * @throws Exception if error checking if error finding entity.
     */
    private static Malfunctionable getEVAMalfunctionEntity(Person person, Unit containerUnit) {
        Malfunctionable result = null;
        
        Collection<Malfunctionable> malfunctionables = null;
        if (containerUnit instanceof Malfunctionable) {
            malfunctionables = MalfunctionFactory.getMalfunctionables((Malfunctionable) containerUnit);
        }
        else if (containerUnit instanceof Settlement) {
            malfunctionables = MalfunctionFactory.getMalfunctionables((Settlement) containerUnit);
        }
        
        if (malfunctionables != null) {
            Iterator<Malfunctionable> i = malfunctionables.iterator();
            while (i.hasNext() && (result == null)) {
                Malfunctionable entity = i.next();
                if (hasEVAMalfunction(person, containerUnit, entity)) result = entity;
            }
        }
        
        return result;
    }
    
    /**
     * Checks if there are enough repair parts at person's location to fix the malfunction.
     * @param person the person checking.
     * @param containerUnit the unit the person is doing an EVA from.
     * @param malfunction the malfunction.
     * @return true if enough repair parts to fix malfunction.
     * @throws Exception if error checking for repair parts.
     */
    private static boolean hasRepairPartsForMalfunction(Person person, Unit containerUnit, 
    		Malfunction malfunction) {
    	if (person == null) throw new IllegalArgumentException("person is null");
    	if (containerUnit == null) throw new IllegalArgumentException("containerUnit is null");
    	if (malfunction == null) throw new IllegalArgumentException("malfunction is null");
    	
    	boolean result = true;    	
    	Inventory inv = containerUnit.getInventory();

    	Map<Part, Integer> repairParts = malfunction.getRepairParts();
    	Iterator<Part> i = repairParts.keySet().iterator();
    	while (i.hasNext() && result) {
    		Part part = i.next();
    		int number = repairParts.get(part);
    		if (inv.getItemResourceNum(part) < number) result = false;
    	}
    	
    	return result;
    }

    /** Returns the weighted probability that a person might perform this task.
     *  @param person the person to perform the task
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

        // Total probabilities for all malfunctionable entities in person's local.
        Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
        while (i.hasNext()) {
            MalfunctionManager manager = i.next().getMalfunctionManager();
            Iterator<Malfunction> j = manager.getEVAMalfunctions().iterator();
            while (j.hasNext()) {
            	Malfunction malfunction = j.next();
            	try {
            		if (hasRepairPartsForMalfunction(person, person.getTopContainerUnit(), malfunction)) {
            			result += 100D;
            		}
            	}
            	catch (Exception e) {
            		e.printStackTrace(System.err);
            	}
            }
        }

        // Check if an airlock is available
        if (getWalkableAvailableAirlock(person) == null) {
            result = 0D;
        }

        // Check if it is night time.
        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
        if (surface.getSurfaceSunlight(person.getCoordinates()) == 0) {
			if (!surface.inDarkPolarRegion(person.getCoordinates())) {
        		result = 0D;
			}
        } 
	
        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
        
        // Check if person is in vehicle.
        boolean inVehicle = Person.INVEHICLE.equals(person.getLocationSituation());
        
		// Job modifier if not in vehicle.
        Job job = person.getMind().getJob();
		if ((job != null) && !inVehicle) {
		    result *= job.getStartTaskProbabilityModifier(RepairEVAMalfunction.class);        
		}

        return result;
    }
    
    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time the phase is to be performed.
     * @return the remaining time after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (EVAOperation.EXIT_AIRLOCK.equals(getPhase())) return exitEVA(time);
    	if (REPAIR_MALFUNCTION.equals(getPhase())) return repairMalfunction(time);
    	if (EVAOperation.ENTER_AIRLOCK.equals(getPhase())) return enterEVA(time);
    	else return time;
    }
    
	/**
	 * Adds experience to the person's skills used in this task.
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		
		// Add experience to "EVA Operations" skill.
		// (1 base experience point per 100 millisols of time spent)
		double evaExperience = time / 100D;
		
		// Experience points adjusted by person's "Experience Aptitude" attribute.
		NaturalAttributeManager nManager = person.getNaturalAttributeManager();
		int experienceAptitude = nManager.getAttribute(NaturalAttributeManager.EXPERIENCE_APTITUDE);
		double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
		evaExperience += evaExperience * experienceAptitudeModifier;
		evaExperience *= getTeachingExperienceModifier();
		person.getMind().getSkillManager().addExperience(Skill.EVA_OPERATIONS, evaExperience);
		
		// If phase is repair malfunction, add experience to mechanics skill.
		if (REPAIR_MALFUNCTION.equals(getPhase())) {
			// 1 base experience point per 20 millisols of collection time spent.
			// Experience points adjusted by person's "Experience Aptitude" attribute.
			double mechanicsExperience = time / 20D;
			mechanicsExperience += mechanicsExperience * experienceAptitudeModifier;
			person.getMind().getSkillManager().addExperience(Skill.MECHANICS, mechanicsExperience);
		}
	}

    /**
     * Perform the exit airlock phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     * @throws Exception if error exiting airlock.
     */
    private double exitEVA(double time) {
    	
    	try {
    		time = exitAirlock(time, airlock);
        
    		// Add experience points
    		addExperience(time);
    	}
		catch (Exception e) {
			// Person unable to exit airlock.
			endTask();
		}
        
        if (exitedAirlock) {
            setPhase(REPAIR_MALFUNCTION);
            
            // Move person outside next to malfunctionable entity.
            moveToRepairLocation();
        }
        return time;
    }
    
    /**
     * Move person outside next to malfunctionable entity.
     */
    private void moveToRepairLocation() {
        
        LocalBoundedObject boundedObject = null;
        if (entity instanceof LocalBoundedObject) {
            boundedObject = (LocalBoundedObject) entity;
        }
        else if (entity instanceof Settlement) {
            Settlement settlement = (Settlement) entity;
            List<Building> allBuildings = settlement.getBuildingManager().getBuildings();
            if (allBuildings.size() > 0) {
                int index = RandomUtil.getRandomInt(allBuildings.size() - 1);
                boundedObject = allBuildings.get(index);
            }
        }
         
        if (boundedObject != null) {
            Point2D.Double newLocation = null;
            boolean goodLocation = false;
            for (int x = 0; (x < 20) && !goodLocation; x++) {
                Point2D.Double boundedLocalPoint = LocalAreaUtil.getRandomExteriorLocation(boundedObject, 1D);
                newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(), 
                        boundedLocalPoint.getY(), boundedObject);
                goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(), 
                        person.getCoordinates());
            }
            
            person.setXLocation(newLocation.getX());
            person.setYLocation(newLocation.getY());
        }
        
    }

    /**
     * Perform the repair malfunction phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     * @throws Exception if error repairing malfunction.
     */
    private double repairMalfunction(double time) {
        
        if (!hasEVAMalfunction(person, containerUnit, entity) || shouldEndEVAOperation()) {
            setPhase(ENTER_AIRLOCK);
            return time;
        }
	    
        // Determine effective work time based on "Mechanic" skill.
        double workTime = time;
        int mechanicSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.MECHANICS);
        if (mechanicSkill == 0) workTime /= 2;
        if (mechanicSkill > 1) workTime += workTime * (.2D * mechanicSkill);

        // Get a local malfunction.
        Malfunction malfunction = null;
        Iterator<Malfunction> i = entity.getMalfunctionManager().getEVAMalfunctions().iterator();
        while (i.hasNext() && (malfunction == null)) {
           	Malfunction tempMalfunction = i.next();
           	if (hasRepairPartsForMalfunction(person, containerUnit, tempMalfunction)) {
           		malfunction = tempMalfunction;
           		setDescription("Repairing " + malfunction.getName() + " on " + entity.getName());
            }
        }
        
        // Add repair parts if necessary.
        Inventory inv = containerUnit.getInventory();
        if (hasRepairPartsForMalfunction(person, containerUnit, malfunction)) {
        	Map<Part, Integer> parts = new HashMap<Part, Integer>(malfunction.getRepairParts());
        	Iterator<Part> j = parts.keySet().iterator();
        	while (j.hasNext()) {
        		Part part = j.next();
        		int number = parts.get(part);
        		inv.retrieveItemResources(part, number);
        		malfunction.repairWithParts(part, number);
        	}
        }
        else {
			setPhase(ENTER_AIRLOCK);
			return time;
		}
	
        // Add EVA work to malfunction.
        double workTimeLeft = malfunction.addEVAWorkTime(workTime);
        
        // Add experience points
        addExperience(time);
	
        // Check if there are no more malfunctions. 
        if (!hasEVAMalfunction(person, containerUnit, entity)) setPhase(ENTER_AIRLOCK);
	
        // Check if an accident happens during maintenance.
        checkForAccident(time);

        return (workTimeLeft / workTime) * time;
    }

    /**
     * Perform the enter airlock phase of the task.
     * @param time amount of time to perform the phase
     * @return time remaining after performing the phase
     * @throws Exception if error entering airlock.
     */
    private double enterEVA(double time) {
        time = enterAirlock(time, airlock);
        
        // Add experience points
        addExperience(time);
        
        if (enteredAirlock) endTask();
        return time;
    }	

    /**
     * Gets the malfunctionable entity the person is currently repairing or null if none.
     * @return entity
     */
    public Malfunctionable getEntity() {
        return entity;
    }
    
	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getMind().getSkillManager();
		int EVAOperationsSkill = manager.getEffectiveSkillLevel(Skill.EVA_OPERATIONS);
		int mechanicsSkill = manager.getEffectiveSkillLevel(Skill.MECHANICS);
		return (int) Math.round((double)(EVAOperationsSkill + mechanicsSkill) / 2D); 
	}
	
	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List<String> getAssociatedSkills() {
		List<String> results = new ArrayList<String>(2);
		results.add(Skill.EVA_OPERATIONS);
		results.add(Skill.MECHANICS);
		return results;
	}
	
	@Override
	public void destroy() {
	    super.destroy();
	    
	    entity = null;
	    airlock = null;
	}
}