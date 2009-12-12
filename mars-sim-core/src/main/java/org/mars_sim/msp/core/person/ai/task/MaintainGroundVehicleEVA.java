/**
 * Mars Simulation Project
 * MaintainGroundVehicleEVA.java
 * @version 2.84 2008-05-12
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
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
import org.mars_sim.msp.core.structure.building.function.GroundVehicleMaintenance;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.Vehicle;

/** 
 * The MaintainGroundVehicleGarage class is a task for performing
 * preventive maintenance on ground vehicles outside a settlement.
 */
public class MaintainGroundVehicleEVA extends EVAOperation implements Serializable {

    private static String CLASS_NAME = 
        "org.mars_sim.msp.simulation.person.ai.task.MaintainGroundVehicleEVA";
	
    private static Logger logger = Logger.getLogger(CLASS_NAME);
	
    // Phase names
    private static final String MAINTAIN_VEHICLE = "Maintain Vehicle";
 
    private GroundVehicle vehicle; // Vehicle to be maintained.
    private Airlock airlock; // Airlock to be used for EVA.
    
	/** 
	 * Constructor
	 * @param person the person to perform the task
	 * @throws Exception if error constructing task.
	 */
    public MaintainGroundVehicleEVA(Person person) throws Exception {
        super("Performing Vehicle Maintenance", person);
   
        // Choose an available needy ground vehicle.
        vehicle = getNeedyGroundVehicle(person);
        if (vehicle != null) vehicle.setReservedForMaintenance(true);
        else endTask();
        
        // Get an available airlock.
        airlock = getAvailableAirlock(person);
        if (airlock == null) endTask();
        
        // Initialize phase.
        addPhase(MAINTAIN_VEHICLE);
        
        logger.finest(person.getName() + " starting MaintainGroundVehicleEVA task.");
    }
    
    /** 
     * Returns the weighted probability that a person might perform this task.
     * It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

		// Get all vehicles needing maintenance.
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			Iterator<Vehicle> i = getAllVehicleCandidates(person).iterator();
			while (i.hasNext()) {
				MalfunctionManager manager = i.next().getMalfunctionManager();
				double entityProb = (manager.getEffectiveTimeSinceLastMaintenance() / 20D);
				if (entityProb > 100D) entityProb = 100D;
				result += entityProb;
			}
		}

		// Determine if settlement has a garage.
        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {	
			if (person.getSettlement().getBuildingManager().getBuildings(GroundVehicleMaintenance.NAME).size() > 0) 
				result = 0D;
        }

        // Check if an airlock is available
        if (getAvailableAirlock(person) == null) result = 0D;

        // Check if it is night time.
        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
        if (surface.getSurfaceSunlight(person.getCoordinates()) == 0) {
        	if (!surface.inDarkPolarRegion(person.getCoordinates()))
        		result = 0D;
        } 
        
		// Crowded settlement modifier
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			Settlement settlement = person.getSettlement();
			if (settlement.getCurrentPopulationNum() > settlement.getPopulationCapacity()) result *= 2D;
		}

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
        
		// Job modifier.
        Job job = person.getMind().getJob();
		if (job != null) result *= job.getStartTaskProbabilityModifier(MaintainGroundVehicleEVA.class);        
	
        return result;
    }
    
    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time the phase is to be performed.
     * @return the remaining time after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) throws Exception {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (EVAOperation.EXIT_AIRLOCK.equals(getPhase())) return exitEVA(time);
    	if (MAINTAIN_VEHICLE.equals(getPhase())) return maintainVehicle(time);
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
		
		// If phase is maintain vehicle, add experience to mechanics skill.
		if (MAINTAIN_VEHICLE.equals(getPhase())) {
			// 1 base experience point per 100 millisols of collection time spent.
			// Experience points adjusted by person's "Experience Aptitude" attribute.
			double mechanicsExperience = time / 100D;
			mechanicsExperience += mechanicsExperience * experienceAptitudeModifier;
			person.getMind().getSkillManager().addExperience(Skill.MECHANICS, mechanicsExperience);
		}
	}
   
    /**
     * Perform the exit airlock phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     * @throws Exception if error exiting the airlock.
     */
    private double exitEVA(double time) throws Exception {
    	
    	try {
    		time = exitAirlock(time, airlock);
        
    		// Add experience points
    		addExperience(time);
    	}
		catch (Exception e) {
			// Person unable to exit airlock.
			endTask();
		}
    	
        if (exitedAirlock) setPhase(MAINTAIN_VEHICLE);
        return time;
    }
    
    /**
     * Perform the maintain vehicle phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     * @throws Exception if error maintaining vehicle.
     */
    private double maintainVehicle(double time) throws Exception {
        
        MalfunctionManager manager = vehicle.getMalfunctionManager();
        boolean malfunction = manager.hasMalfunction();
        boolean finishedMaintenance = (manager.getEffectiveTimeSinceLastMaintenance() == 0D);
        if (finishedMaintenance) vehicle.setReservedForMaintenance(false);
        
        if (finishedMaintenance || malfunction || shouldEndEVAOperation()) {
            setPhase(ENTER_AIRLOCK);
            return time;
        }
        
        // Determine effective work time based on "Mechanic" and "EVA Operations" skills.
        double workTime = time;
        int skill = getEffectiveSkillLevel();
        if (skill == 0) workTime /= 2;
        if (skill > 1) workTime += workTime * (.2D * skill);

        // Add repair parts if necessary.
        Inventory inv = containerUnit.getInventory();
        if (Maintenance.hasMaintenanceParts(inv, vehicle)) {
        	Map<Part, Integer> parts = new HashMap<Part, Integer>(manager.getMaintenanceParts());
        	Iterator<Part> j = parts.keySet().iterator();
        	while (j.hasNext()) {
        		Part part = j.next();
        		int number = parts.get(part);
        		inv.retrieveItemResources(part, number);
        		manager.maintainWithParts(part, number);
        	}
        }
        else {
			setPhase(ENTER_AIRLOCK);
			return time;
		}
        
        // Add work to the maintenance
        manager.addMaintenanceWorkTime(workTime);
        
        // Add experience points
        addExperience(time);
	
        // Check if an accident happens during maintenance.
        checkForAccident(time);

        return 0D;
    }   
    
    /**
     * Perform the enter airlock phase of the task.
     * @param time amount of time to perform the phase
     * @return time remaining after performing the phase
     * @throws Exception if error entering airlock.
     */
    private double enterEVA(double time) throws Exception {
        time = enterAirlock(time, airlock);
        
        // Add experience points
        addExperience(time);
        
        if (enteredAirlock) endTask();
        return time;
    }	
    
    /**
     * Check for accident with entity during maintenance phase.
     * @param time the amount of time (in millisols)
     */
    protected void checkForAccident(double time) {

        // Use EVAOperation checkForAccident() method.
        super.checkForAccident(time);
        
        double chance = .001D;

        // Mechanic skill modification.
        int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.MECHANICS);
        if (skill <= 3) chance *= (4 - skill);
        else chance /= (skill - 2);

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            // logger.info(person.getName() + " has accident while performing maintenance on " + vehicle.getName() + ".");
            vehicle.getMalfunctionManager().accident();
        }
    }
    
    /** 
     * Gets the vehicle  the person is maintaining.
     * Returns null if none.
     * @return entity
     */
    public Malfunctionable getVehicle() {
        return vehicle;
    }
    
    /**
     * Gets all ground vehicles requiring maintenance that are parked outside the settlement.
     *
     * @param person person checking.
     * @return collection of ground vehicles available for maintenance.
     */
    private static Collection<Vehicle> getAllVehicleCandidates(Person person) {
        Collection<Vehicle> result = new ConcurrentLinkedQueue<Vehicle>();
        
        Settlement settlement = person.getSettlement();
        if (settlement != null) {
        	Iterator<Vehicle> vI = settlement.getParkedVehicles().iterator();
        	while (vI.hasNext()) {
        		Vehicle vehicle = vI.next();
        		if ((vehicle instanceof GroundVehicle) && !vehicle.isReservedForMission()) result.add(vehicle);
        	}
        }
        
        return result;
    }
    
    /**
     * Gets a ground vehicle that requires maintenance in a local garage.
     * Returns null if none available.
     * @param person person checking.
     * @return ground vehicle
     * @throws Exception if error finding needy vehicle.
     */
    private GroundVehicle getNeedyGroundVehicle(Person person) throws Exception {
            
        GroundVehicle result = null;

        // Find all vehicles that can be maintained.
        Collection<Vehicle> availableVehicles = getAllVehicleCandidates(person);
        
        // Determine total probability weight.
        double totalProbWeight = 0D;
        Iterator<Vehicle> i = availableVehicles.iterator();
        while (i.hasNext()) {
            totalProbWeight += getProbabilityWeight(i.next());
        }
        
        // Get random value
        double rand = RandomUtil.getRandomDouble(totalProbWeight);
        
        // Determine which vehicle was picked.
        Iterator<Vehicle> i2 = availableVehicles.iterator();
        while (i2.hasNext() && (result == null)) {
            Vehicle vehicle = i2.next();
            double probWeight = getProbabilityWeight(vehicle);
            if (rand < probWeight) {
            	result = (GroundVehicle) vehicle;
            	setDescription("Performing maintenance on " + result.getName());
            	break;
            }
            else rand -= probWeight;
        }
        
        return result;
    }
    
    /**
     * Gets the probability weight for a vehicle.
     * @param vehicle the vehicle.
     * @return the probability weight.
     * @throws Exception if error determining probability weight.
     */
    private double getProbabilityWeight(Vehicle vehicle) throws Exception {
    	double result = 0D;
		MalfunctionManager manager = vehicle.getMalfunctionManager();
		boolean hasMalfunction = manager.hasMalfunction();
		double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
		boolean minTime = (effectiveTime >= 1000D); 
		boolean enoughParts = Maintenance.hasMaintenanceParts(person, vehicle);
		if (!hasMalfunction && minTime && enoughParts) result = effectiveTime;
		return result;
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
}