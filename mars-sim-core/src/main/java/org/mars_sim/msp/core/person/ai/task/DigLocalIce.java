/**
 * Mars Simulation Project
 * DigLocalIce.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.logging.Level;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
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
	private static SimLogger logger = SimLogger.getLogger(DigLocalIce.class.getName());
	
	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.digLocalIce"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase COLLECT_ICE = new TaskPhase(Msg.getString(
            "Task.phase.collectIce")); //$NON-NLS-1$

	public static final double SMALL_AMOUNT = 0.001;
	
	
	private double compositeRate;
    private double factor;
    
	/**  Collection rate of ice during EVA (kg/millisol). */
	private double collectionRate;

	// Domain members
	/** Total ice collected in kg. */
	private double totalCollected;
	
	/** Airlock to be used for EVA. */
	private Airlock airlock;
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
        super(NAME, person, false, 20, SkillType.AREOLOGY);

//		if (shouldEndEVAOperation()) {
//        	if (person.isOutside())
//        		setPhase(WALK_BACK_INSIDE);
//        	else
//        		endTask();
//        	return;
//        }
		
		if (!person.isFit()) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
	      	return;
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
        	
	        logger.log(person, Level.INFO, 4_000, 
					" Started digging for ice.");
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
		if (!isDone()) {
	        if (getPhase() == null) {
				throw new IllegalArgumentException("Task phase is null");
	        }
	        else if (COLLECT_ICE.equals(getPhase())) {
	            time = collectIce(time);
	        }
		}
        return time;
    }
    
    /**
     * Perform collect ice phase.
     * @param time time (millisol) to perform phase.
     * @return time (millisol) remaining after performing phase.
     */
    private double collectIce(double time) {
		// Check for radiation exposure during the EVA operation.
		if (isDone() || isRadiationDetected(time)) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return time;
		}
		
        // Check if there is any EVA problems and if on-site time is over.
		if (shouldEndEVAOperation() || addTimeOnSite(time)) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return time;
		}
		
		if (!person.isFit()) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
		}
		
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
	    	logger.log(person, Level.INFO, 4_000, "Collected a total of " 
	    			+ Math.round(totalCollected*100D)/100D 
	    			+ " kg of ice outside.");
//	    			+ person.getCoordinates().getFormattedString() + ".");
	    	
	    	if (person.isOutside())
            	setPhase(WALK_BACK_INSIDE);
            else {
            	ended = true;
            	endTask();
         		return 0;
            }
        }

        if (fatigue > 1000 || stress > 50 || hunger > 750 || energy < 1000) {
    		logger.log(person, Level.INFO, 4_000,
				"Took a break from collecting ice ("
        		+ Math.round(totalCollected*100D)/100D + " kg collected) " 
        		+ "; fatigue: " + Math.round(fatigue*10D)/10D 
        		+ "; stress: " + Math.round(stress*100D)/100D + " %"
        		+ "; hunger: " + Math.round(hunger*10D)/10D 
        		+ "; energy: " + Math.round(energy*10D)/10D + " kJ.");
            
            if (person.isOutside())
            	setPhase(WALK_BACK_INSIDE);
            
            else {
            	ended = true;
            	endTask();
         		return 0;
            }
        }

     	if (person.isInSettlement()) {
    		logger.log(person, Level.INFO, 4_000,
            	"Going back to the settlement."); 
        	ended = true;
        	endTask();
     	}
        
        // Check for an accident during the EVA operation.
        checkForAccident(time);

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
    		    logger.log(person, Level.WARNING, 10_000,
    		    		"Unable to carry an empty bag.");
            	ended = true;
            	super.endTask();
            }
        }
        else {
        	logger.log(person, Level.WARNING, 10_000,
        			"Unable to find an empty bag in the inventory.");
        	ended = true;
        	super.endTask();
        }
    }
    
    @Override
    protected TaskPhase getOutsideSitePhase() {
        return COLLECT_ICE;
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

    /**
     * If person is inside then transfer inventory to the Settlement
     */
    @Override
    protected void clearDown() {
    	if (person.isOutside()) {
    		setPhase(WALK_BACK_INSIDE);
    	}
    	
    	else {
//	    	ended = true;
	    	Inventory pInv = person.getInventory();
	    	Bag bag = pInv.findABag(false);

            double ice1 = pInv.getAmountResourceStored(iceID, false);

            if (ice1 > .0001) {
	        	Inventory sInv = settlement.getInventory();
	        	
	            double settlementCap = sInv.getAmountResourceRemainingCapacity(
	            		iceID, false, false);

	            if (bag != null && sInv != null) {
		            // Try to store ice in settlement.
		            if (ice1 > settlementCap) {
		            	ice1 = settlementCap;
		            	
		            	logger.log(person, Level.INFO, 10_000,
		            			"Ice storage full. Could only check in " 
		            			+ Math.round(ice1*10.0)/10.0 + " kg ice.");
		            	
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
//			            settlement.getGoodsManager().determineGoodValueWithSupply(GoodsUtil.getResourceGood(iceID), ice1);
		            }
		            
		            else {
		            	if (ice1 > 0) {
			            	logger.log(person, Level.INFO, 4_000, 
			            			"Checking in " + Math.round(ice1*10.0)/10.0 + " kg ice.");		
		
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
				            settlement.getGoodsManager().determineGoodValueWithSupply(GoodsUtil.getResourceGood(iceID), ice1);
		            	}
		            }
	            }
            }
    	}
    }
}
