/**
 * Mars Simulation Project
 * DigLocalIce.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The DigLocalIce class is a task for performing
 * collecting ice outside of a settlement.
 */
public class DigLocalIce
extends EVAOperation
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(DigLocalIce.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.digLocalIce"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase COLLECT_ICE = new TaskPhase(Msg.getString(
            "Task.phase.collectIce")); //$NON-NLS-1$

	public static final double SMALL_AMOUNT = 0.001;
	
	/** The resource id for a bag. */
//	private static final int BAG  = EquipmentType.convertName2ID("bag");
	
	private double compositeRate;
    private double factor;
    
	/**  Collection rate of ice during EVA (kg/millisol). */
	private double collectionRate;

	// Domain members
	/** Total ice collected in kg. */
	private double totalCollected;
	
	/** Airlock to be used for EVA. */
	private Airlock airlock;
	/** Bag for collecting ice. */
//	private Bag bag;
	/** The Settlement vicinity for collecting ice. */
	private Settlement settlement;

	private boolean ended = false;
	
	private static int iceID = ResourceUtil.iceID;
	
	/**
	 * Constructor.
	 * @param person the person performing the task.
	 * @throws Exception if error constructing the task.
	 */
	public DigLocalIce(Person person) {
        // Use EVAOperation constructor.
        super(NAME, person, false, 20);//+ RandomUtil.getRandomInt(10) - RandomUtil.getRandomInt(10));

		// Checks if a person is tired, too stressful or hungry and need 
		// to take break, eat and/or sleep
		if (!person.getPhysicalCondition().isFit()) {
			LogConsolidated.log(logger, Level.INFO, 4_000, sourceName, 
					"[" + person.getLocale() + "] "
					+ person.getName() 
					+ " was not fit enough to dig local ice ("
					+ Math.round(person.getXLocation()*10.0)/10.0 + ", " 
					+ Math.round(person.getYLocation()*10.0)/10.0 + ").");
			person.getMind().getTaskManager().clearAllTasks();
			walkToRandomLocation(true);
		}
		
     	settlement = CollectionUtils.findSettlement(person.getCoordinates());
     	if (settlement == null) {
     		ended = true;
        	endTask();
     	}
        
        // Get an available airlock.
     	if (person.isInside()) {
	        airlock = getWalkableAvailableAirlock(person);
	        if (airlock == null) {
	        	ended = true;
	        	endTask();
	        }
     	}

        // Take bags for collecting ice.
        if (!hasBags() && person.getInventory().findABag(true) == null) {
            takeBag();

            // If bags are not available, end task.
            if (!hasBags()) {
            	if (person.isOutside()){
                    setPhase(WALK_BACK_INSIDE);
                }
            	else {
                	ended = true;
                	endTask();
            	}
            }
        }

        if (!ended) {
            // Determine digging location.
            Point2D.Double diggingLoc = determineDiggingLocation();
            setOutsideSiteLocation(diggingLoc.getX(), diggingLoc.getY());

	        collectionRate = settlement.getIceCollectionRate();
	        
	        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
	        int strength = nManager.getAttribute(NaturalAttributeType.STRENGTH);
	        int agility = nManager.getAttribute(NaturalAttributeType.AGILITY);
	        int eva = person.getSkillManager().getSkillLevel(SkillType.EVA_OPERATIONS);
	        
	        factor = .9 * (1 - (agility + strength) / 200D);
	        compositeRate  = 10 * collectionRate * ((.5 * agility + strength) / 150D) * (eva + .1);
	        
	        // Add task phases
	        addPhase(COLLECT_ICE);

	        setPhase(WALK_TO_OUTSIDE_SITE);
        	
	        logger.finest(person.getName() + " was going to start digging for ice.");
        }
    }

    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time the phase is to be performed.
     * @return the remaining time after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) {

        time = super.performMappedPhase(time);

        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (COLLECT_ICE.equals(getPhase())) {
            return collectIce(time);
        }
        else {
            return time;
        }
    }
    
    /**
     * Perform collect ice phase.
     * @param time time (millisol) to perform phase.
     * @return time (millisol) remaining after performing phase.
     */
    private double collectIce(double time) {
//    	LogConsolidated.log(Level.INFO, 0, sourceName, 
//        		"[" + person.getLocationTag().getLocale() +  "] " +
//        		person.getName() + " just called collectice()");
     	
        // Check for an accident during the EVA operation.
        checkForAccident(time);

        // Check for radiation exposure during the EVA operation.
        if (person.isOutside() && isRadiationDetected(time)){
            setPhase(WALK_BACK_INSIDE);
            return time;
        }
        
        // Check if there is reason to cut the collect
        // ice phase short and return.
        if (person.isOutside() && shouldEndEVAOperation()) {
            setPhase(WALK_BACK_INSIDE);
            return time;
        }

//        if (person.isInside()) {
//            setPhase(WALK_TO_OUTSIDE_SITE);
//            return time;
//        }
//        
//        else {
        Inventory pInv = person.getInventory();
        Inventory bInv = pInv.findABag(false).getInventory();
        
        double collected = RandomUtil.getRandomDouble(2) * time * compositeRate;
        
		// Modify collection rate by "Areology" skill.
		int areologySkill = person.getSkillManager().getEffectiveSkillLevel(SkillType.AREOLOGY);
		if (areologySkill >= 1) {
			collected = collected + .1 * collected * areologySkill;
		}
		else {//if (areologySkill == 0) {
			collected /= 1.5D;
		}
		
//			LogConsolidated.log(Level.INFO, 0, sourceName, 
//	        		"[" + person.getLocationTag().getLocale() +  "] " +
//	        		person.getName() + " just collected " + Math.round(collected*100D)/100D 
//	        		+ " kg ice outside at " + person.getCoordinates().getFormattedString());
		
        boolean finishedCollecting = false;
        
        // Introduce randomness into the amount collected so that it will NOT
        // always weigh strangely at exactly 50 kg 
        double rand = RandomUtil.getRandomDouble(1.5);
        
        double personRemainingCap = bInv.getAmountResourceRemainingCapacity(
        		iceID, false, false);
        
        double bagRemainingCap = bInv.getAmountResourceRemainingCapacity(
        		iceID, false, false);

        if (personRemainingCap < SMALL_AMOUNT) {
//	        	logger.info(person + " case 1");
            finishedCollecting = true;
            collected = 0;
        }
        
        else if (bagRemainingCap < SMALL_AMOUNT) {
//	        	logger.info(person + " case 2");
            finishedCollecting = true;
            collected = 0;
        }
        		
        else if (//totalCollected + collected >= bInv.getGeneralCapacity()
        		totalCollected + collected + rand >= pInv.getGeneralCapacity()) {    
//	        	logger.info(person + " case 3 (" + bInv.getGeneralCapacity() + ", " + pInv.getGeneralCapacity());
            finishedCollecting = true;
            collected = pInv.getGeneralCapacity() - rand;
        }
        
        else if (collected > SMALL_AMOUNT 
        		&& (collected + rand >= bagRemainingCap || collected + rand >= personRemainingCap)) {
//	        	logger.info(person + " case 4");
        	finishedCollecting = true;
        	collected = bagRemainingCap - rand; 	
        }

        if (collected > 0) {
        	totalCollected += collected;
        	bInv.storeAmountResource(iceID, collected, true);
        }
        
        PhysicalCondition condition = person.getPhysicalCondition();
        double stress = condition.getStress();
        double fatigue = condition.getFatigue();
        double hunger = condition.getHunger();
        double energy = condition.getEnergy(); 
        double strengthMod = condition.getStrengthMod();
        
        // Add penalty to the fatigue
        condition.setFatigue(fatigue + time * factor * (1.1D - strengthMod));
        
        // Add experience points
        addExperience(time);
        
        if (finishedCollecting && totalCollected > 0) {
            LogConsolidated.log(logger, Level.INFO, 4_000, sourceName, 
	    		"[" + person.getLocationTag().getLocale() +  "] " +
	    		person.getName() + " collected a total of " + Math.round(totalCollected*100D)/100D 
	    		+ " kg of ice outside at " + person.getCoordinates().getFormattedString() + ".");
//          if (person.isOutside()) {
//        	setPhase(WALK_BACK_INSIDE);
//        }
//        
//        else {
        	ended = true;
        	endTask();
//        }
        }

        if (fatigue > 1000 || stress > 50 || hunger > 750 || energy < 1000) {
            LogConsolidated.log(logger, Level.INFO, 4_000, sourceName, 
        		"[" + person.getLocationTag().getLocale() +  "] " +
				person.getName() + " had to take a break from collecting ice ("
        		+ Math.round(totalCollected*100D)/100D + " kg collected) " 
        		+ "; fatigue: " + Math.round(fatigue*10D)/10D 
        		+ "; stress: " + Math.round(stress*100D)/100D + " %"
        		+ "; hunger: " + Math.round(hunger*10D)/10D 
        		+ "; energy: " + Math.round(energy*10D)/10D + " kJ");
            
//          if (person.isOutside()) {
//        	setPhase(WALK_BACK_INSIDE);
//        }
//        
//        else {
        	ended = true;
        	endTask();
//        }
        }

     	if (person.isInSettlement()) {
            LogConsolidated.log(logger, Level.INFO, 4_000, sourceName, 
            		"[" + person.getLocationTag().getLocale() +  "] " +
            		person.getName() + " had already been back to the settlement."); 
        	ended = true;
        	endTask();
     	}
        
        return 0D;
    }

    /**
     * Checks if the person is carrying any bags.
     * @return true if carrying bags.
     */
    private boolean hasBags() {
        return person.getInventory().containsUnitClass(Bag.class);
    }

    /**
     * Takes an empty bag (preferably) from the rover.
     */
    private void takeBag() {
        Bag aBag = settlement.getInventory().findABag(true);
        if (aBag == null) {
        	// if no empty bag, take any bags
        	aBag = settlement.getInventory().findABag(false);
	    	// Add the equipment demand for a bag
//	    	settlement.getInventory().addEquipmentDemandTotalRequest(BAG, 1);
//	    	settlement.getInventory().addEquipmentDemand(BAG, 1);
        }
        if (aBag != null) {
            if (person.getInventory().canStoreUnit(aBag, false)) {
            	aBag.transfer(settlement, person);
//                bag = aBag;
            }
            else {
            	LogConsolidated.log(logger, Level.WARNING, 10_000, sourceName,
    					"[" 
    					+ person.getLocationTag().getLocale()
    					+ "] "  + person.getName() 
    					+ " was strangely unable to carry an empty bag.");
            	ended = true;
            	super.endTask();
            }
        }
        else {
        	LogConsolidated.log(logger, Level.WARNING, 10_000, sourceName,
					"[" 
					+ person.getLocationTag().getLocale()
					+ "] "  + person.getName() 
					+ " was unable to find an empty bag in the inventory.");
        	ended = true;
        	super.endTask();
        }
    }
    
    @Override
    protected TaskPhase getOutsideSitePhase() {
        return COLLECT_ICE;
    }

    @Override
    protected void addExperience(double time) {
        // Add experience to "EVA Operations" skill.
        // (1 base experience point per 100 millisols of time spent)
        double evaExperience = time / 100D;

        // Experience points adjusted by person's "Experience Aptitude" attribute.
        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        int experienceAptitude = nManager.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
        double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
        evaExperience += evaExperience * experienceAptitudeModifier;
        evaExperience *= getTeachingExperienceModifier();
        person.getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience, time);

        // If phase is collect ice, add experience to areology skill.
        if (COLLECT_ICE.equals(getPhase())) {
            // 1 base experience point per 10 millisols of collection time spent.
            // Experience points adjusted by person's "Experience Aptitude" attribute.
            double areologyExperience = time / 10D;
            areologyExperience += areologyExperience * experienceAptitudeModifier;
            person.getSkillManager().addExperience(SkillType.AREOLOGY, areologyExperience, time);
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
        SkillManager manager = person.getSkillManager();
        int EVAOperationsSkill = manager.getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
        int areologySkill = manager.getEffectiveSkillLevel(SkillType.AREOLOGY);
        return (int) Math.round((double)(EVAOperationsSkill + areologySkill) / 2D);
    }

    /**
     * Determine location for digging ice.
     * @return digging X and Y location outside settlement.
     */
    private Point2D.Double determineDiggingLocation() {

        Point2D.Double newLocation = null;
        boolean goodLocation = false;
        for (int x = 0; (x < 5) && !goodLocation; x++) {
            for (int y = 0; (y < 10) && !goodLocation; y++) {
                if (airlock.getEntity() instanceof LocalBoundedObject) {
                    LocalBoundedObject boundedObject = (LocalBoundedObject) airlock.getEntity();

                    double distance = RandomUtil.getRandomDouble(100D) + (x * 100D) + 50D;
                    double radianDirection = RandomUtil.getRandomDouble(Math.PI * 2D);
                    double newXLoc = boundedObject.getXLocation() - (distance * Math.sin(radianDirection));
                    double newYLoc = boundedObject.getYLocation() + (distance * Math.cos(radianDirection));
                    Point2D.Double boundedLocalPoint = new Point2D.Double(newXLoc, newYLoc);

                    newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(),
                            boundedLocalPoint.getY(), boundedObject);
                    goodLocation = LocalAreaUtil.isLocationCollisionFree(newLocation.getX(), newLocation.getY(),
                            person.getCoordinates());
                }
            }
        }

        return newLocation;
    }

    @Override
    public void endTask() {
    	if (person.isOutside()) {
    		setPhase(WALK_BACK_INSIDE);
    	}
    	
    	else {
//	    	ended = true;
	    	Inventory pInv = person.getInventory();
	    	Bag bag = pInv.findABag(false);

            double ice1 = pInv.getAmountResourceStored(iceID, false);

            if (ice1 < .0001)
            	super.endTask();
            
        	Inventory sInv = settlement.getInventory();
        	
            double settlementCap = sInv.getAmountResourceRemainingCapacity(
            		iceID, false, false);

            if (sInv != null) {
	            // Try to store ice in settlement.
	            if (ice1 > settlementCap) {
	            	ice1 = settlementCap;
	            	
	            	LogConsolidated.log(logger, Level.INFO, 4_000, sourceName, 
	            			"[" + person.getLocationTag().getLocale() +  "] Ice storage full. " +
	            				person.getName() + " could only check in " + Math.round(ice1*10.0)/10.0 + " kg ice.");
	            	
//	            	bInv.retrieveAmountResource(iceID, ice0);
	            	pInv.retrieveAmountResource(iceID, ice1);
	                // Store the ice
	                sInv.storeAmountResource(iceID, ice1, false);
	                // Track supply
	                sInv.addAmountSupply(iceID, ice1);
	                // Transfer the bag
	                bag.transfer(person, sInv);
					// Add to the daily output
					settlement.addOutput(iceID, ice1, getTimeCompleted());
		            // Recalculate settlement good value for output item.
		            settlement.getGoodsManager().updateGoodValue(GoodsUtil.getResourceGood(iceID), false);
	            }
	            
	            else {
	            	LogConsolidated.log(logger, Level.INFO, 4_000, sourceName, 
	            			"[" + person.getLocationTag().getLocale() +  "] " +
	            				person.getName() + " was checking in " + Math.round(ice1*10.0)/10.0 + " kg ice.");
	                		
//	            	bInv.retrieveAmountResource(iceID, ice0);
	            	pInv.retrieveAmountResource(iceID, ice1);
	                // Store the ice
	                sInv.storeAmountResource(iceID, ice1, false);
	                // Track supply
	                sInv.addAmountSupply(iceID, ice1);
	                // Transfer the bag
	                bag.transfer(person, sInv);
					// Add to the daily output
					settlement.addOutput(iceID, ice1, getTimeCompleted());
		            // Recalculate settlement good value for output item.
		            settlement.getGoodsManager().updateGoodValue(GoodsUtil.getResourceGood(iceID), false);
		            
//		            super.endTask();
	            }
            }
            
        	super.endTask();
    	}
    }

    @Override
    public void destroy() {
        super.destroy();

        airlock = null;
//        bag = null;
        settlement = null;
    }
}
