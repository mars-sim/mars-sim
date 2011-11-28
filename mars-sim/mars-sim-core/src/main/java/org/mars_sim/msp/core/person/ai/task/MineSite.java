/**
 * Mars Simulation Project
 * MineSite.java
 * @version 3.02 2011-11-27
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Task for mining minerals at a site.
 */
public class MineSite extends EVAOperation implements Serializable {

    private static Logger logger = Logger.getLogger(MineSite.class.getName());
    
	// Task phases
	private static final String MINING = "Mining";
	
	// Excavation rates (kg/millisol)
	private static final double HAND_EXCAVATION_RATE = .1D;
	private static final double LUV_EXCAVATION_RATE = 1D;
	
	// Time limit for mining (millisol)
	private static final double MINING_TIME_LIMIT = 100D;
	
	// The base chance of an accident while operating LUV per millisol.
	public static final double BASE_LUV_ACCIDENT_CHANCE = .001;
	
	// Data members
	private Coordinates site;
	private Rover rover;
	private LightUtilityVehicle luv;
	private boolean operatingLUV;
	private double miningTime;
	
	/**
	 * Constructor
	 * @param person the person performing the task.
	 * @param site the explored site to mine.
	 * @param rover the rover used for the EVA operation.
	 * @param luv the light utility vehicle used for mining.
	 * @throws Exception if error creating task.
	 */
	public MineSite(Person person, Coordinates site, Rover rover, 
			LightUtilityVehicle luv) {
		
		// Use EVAOperation parent constructor.
		super("Mine Site", person);
		
		// Initialize data members.
		this.site = site;
		this.rover = rover;
		this.luv = luv;
		operatingLUV = false;
		
		// Add task phase
		addPhase(MINING);
	}
	
	/**
	 * Checks if a person can mine a site.
	 * @param person the person
	 * @param rover the rover
	 * @return true if person can mine a site.
	 */
	public static boolean canMineSite(Person person, Rover rover) {
		// Check if person can exit the rover.
		boolean exitable = ExitAirlock.canExitAirlock(person, rover.getAirlock());

		SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();

		// Check if it is night time outside.
		boolean sunlight = surface.getSurfaceSunlight(rover.getCoordinates()) > 0;
		
		// Check if in dark polar region.
		boolean darkRegion = surface.inDarkPolarRegion(rover.getCoordinates());

		// Check if person's medical condition will not allow task.
		boolean medical = person.getPerformanceRating() < .5D;
	
		return (exitable && (sunlight || darkRegion) && !medical);
	}
	
	/**
	 * Perform the exit rover phase of the task.
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 * @throws Exception if error exiting rover.
	 */
	private double exitRover(double time) {
		
		try {
			time = exitAirlock(time, rover.getAirlock());
		
			// Add experience points
			addExperience(time);
		}
		catch (Exception e) {
			// Person unable to exit airlock.
			endTask();
		}
		
		if (exitedAirlock) setPhase(MINING);

		return time;
	}
	
	/**
	 * Perform the enter rover phase of the task.
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 * @throws Exception if error entering rover.
	 */
	private double enterRover(double time) {

		time = enterAirlock(time, rover.getAirlock());

        // Add experience points
        addExperience(time);
        
        if (enteredAirlock) {
        	endTask();
        	return time;
        }
        
		return 0D;
	}
	
	/**
	 * Perform the mining phase of the task.
	 * @param time the time available (millisols).
	 * @return remaining time after performing phase (millisols).
	 * @throws Exception if error performing phase.
	 */
	private double miningPhase(double time) {
		
		// Check for an accident during the EVA operation.
		checkForAccident(time);
		
		// Add mining time.
		miningTime += time;
		
		// Check if there is reason to cut the mining phase short and return
		// to the rover.
		if (shouldEndEVAOperation() || (miningTime >= MINING_TIME_LIMIT)) {
			// End operating light utility vehicle.
			if (luv.getInventory().containsUnit(person)) { 
				luv.getInventory().retrieveUnit(person);
				luv.setOperator(null);
				operatingLUV = false;
			}
			setPhase(EVAOperation.ENTER_AIRLOCK);
			return time;
		}
		
		// Operate light utility vehicle if no one else is operating it.
		if (!luv.getMalfunctionManager().hasMalfunction() && (luv.getCrewNum() == 0)) {
		    if (luv.getInventory().canStoreUnit(person)) {
		        luv.getInventory().storeUnit(person);
		        luv.setOperator(person);
		        operatingLUV = true;
		        setDescription("Excavating site with " + luv.getName());
		    }
		    else {
		        logger.info(person.getName() + " could not operate " + luv.getName());
		    }
		}
		
		// Excavate minerals.
		excavateMinerals(time);
		
		// Add experience points
        addExperience(time);
        
        return 0D;
	}
	
	/**
	 * Excavating minerals from the mining site.
	 * @param time the time to excavate minerals.
	 * @throws Exception if error excavating minerals.
	 */
	private void excavateMinerals(double time) {
		
		Map<String, Double> minerals = Simulation.instance().getMars().getSurfaceFeatures()
				.getMineralMap().getAllMineralConcentrations(site);
		Iterator<String> i = minerals.keySet().iterator();
		while (i.hasNext()) {
			String mineralName = i.next();
			double amountExcavated = 0D;
			if (operatingLUV) {
				amountExcavated = LUV_EXCAVATION_RATE * time;
			}
			else amountExcavated = HAND_EXCAVATION_RATE * time;
			double mineralConcentration = minerals.get(mineralName);
			amountExcavated *= mineralConcentration / 100D;
			amountExcavated *= getEffectiveSkillLevel();
			
			AmountResource mineralResource = AmountResource.findAmountResource(mineralName);
			Mining mission = (Mining) person.getMind().getMission();
			mission.excavateMineral(mineralResource, amountExcavated);
		}
	}
	
	@Override
	protected void addExperience(double time) {
		SkillManager manager = person.getMind().getSkillManager();
		
		// Add experience to "EVA Operations" skill.
		// (1 base experience point per 100 millisols of time spent)
		double evaExperience = time / 100D;
		
		// Experience points adjusted by person's "Experience Aptitude" attribute.
		NaturalAttributeManager nManager = person.getNaturalAttributeManager();
		int experienceAptitude = nManager.getAttribute(NaturalAttributeManager.EXPERIENCE_APTITUDE);
		double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
		evaExperience += evaExperience * experienceAptitudeModifier;
		evaExperience *= getTeachingExperienceModifier();
		manager.addExperience(Skill.EVA_OPERATIONS, evaExperience);
		
		// If phase is mining, add experience to areology skill.
		if (MINING.equals(getPhase())) {
			// 1 base experience point per 10 millisols of mining time spent.
			// Experience points adjusted by person's "Experience Aptitude" attribute.
			double areologyExperience = time / 10D;
			areologyExperience += areologyExperience * experienceAptitudeModifier;
			manager.addExperience(Skill.AREOLOGY, areologyExperience);
			
			// If person is driving the light utility vehicle, add experience to driving skill.
			// 1 base experience point per 10 millisols of mining time spent.
			// Experience points adjusted by person's "Experience Aptitude" attribute.
			if (operatingLUV) {
				double drivingExperience = time / 10D;
				drivingExperience += drivingExperience * experienceAptitudeModifier;
				manager.addExperience(Skill.DRIVING, drivingExperience);
			}
		}
	}

	@Override
	public List<String> getAssociatedSkills() {
		List<String> results = new ArrayList<String>(3);
		results.add(Skill.EVA_OPERATIONS);
		results.add(Skill.AREOLOGY);
		if (operatingLUV) results.add(Skill.DRIVING);
		return results;
	}

	@Override
	public int getEffectiveSkillLevel() {
		int result = 0;
		
		SkillManager manager = person.getMind().getSkillManager();
		int EVAOperationsSkill = manager.getEffectiveSkillLevel(Skill.EVA_OPERATIONS);
		int areologySkill = manager.getEffectiveSkillLevel(Skill.AREOLOGY);
		if (operatingLUV) {
			int drivingSkill = manager.getEffectiveSkillLevel(Skill.DRIVING);
			result = (int) Math.round((double)(EVAOperationsSkill + areologySkill + drivingSkill) / 3D); 
		}
		else result = (int) Math.round((double)(EVAOperationsSkill + areologySkill) / 2D);
		
		return result;
	}

	@Override
	protected double performMappedPhase(double time) {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (EVAOperation.EXIT_AIRLOCK.equals(getPhase())) return exitRover(time);
    	if (MINING.equals(getPhase())) return miningPhase(time);
    	if (EVAOperation.ENTER_AIRLOCK.equals(getPhase())) return enterRover(time);
    	else return time;
	}

	@Override
	protected void checkForAccident(double time) {
		super.checkForAccident(time);
		
		// Check for light utility vehicle accident if operating one.
		if (operatingLUV) {
			double chance = BASE_LUV_ACCIDENT_CHANCE;
			
			// Driving skill modification.
			int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.EVA_OPERATIONS);
            if (skill <= 3) chance *= (4 - skill);
            else chance /= (skill - 2);
            
            // Modify based on the LUV's wear condition.
            chance *= luv.getMalfunctionManager().getWearConditionAccidentModifier();
            
            if (RandomUtil.lessThanRandPercent(chance * time))
    	    	luv.getMalfunctionManager().accident();
		}
    }
	
	@Override
	protected boolean shouldEndEVAOperation() {
        boolean result = super.shouldEndEVAOperation();
        
        // If operating LUV, check if LUV has malfunction.
        if (operatingLUV && luv.getMalfunctionManager().hasMalfunction())
        	result = true;
	
        return result;
    }
	
	@Override
	public void destroy() {
	    super.destroy();
	    
	    site = null;
	    rover = null;
	    luv = null;
	}
}