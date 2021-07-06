/**
 * Mars Simulation Project
 * DigLocalRegolith.java
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
 * The DigLocalRegolith class is a task for performing
 * collecting regolith outside a settlement.
 */
public class DigLocalRegolith
extends EVAOperation
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(DigLocalRegolith.class.getName());
	
	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.digLocalRegolith"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase COLLECT_REGOLITH = new TaskPhase(Msg.getString(
            "Task.phase.collectRegolith")); //$NON-NLS-1$

	/** Collection rate of regolith during EVA (kg/millisol). */
	public static final double BASE_COLLECTION_RATE = 5D;

	public static final double SMALL_AMOUNT = 0.001;
			
	/** The resource id for a bag. */
//	private static final int BAG  = EquipmentType.convertName2ID("bag");
	
	// Domain members
//	private double duration = 40; // + RandomUtil.getRandomInt(10) - RandomUtil.getRandomInt(10);
	/** Total ice collected in kg. */
	private double totalCollected;

	/** Airlock to be used for EVA. */
	private Airlock airlock;
	/** Bag for collecting regolith. */
//	private Bag bag;
	private Settlement settlement;

	private double compositeRate;
	private double factor = .9;
    
	private boolean ended = false;
	
	private static int regolithID = ResourceUtil.regolithID;

	/**
	 * Constructor.
	 * @param person the person performing the task.
	 */
	public DigLocalRegolith(Person person) {
        // Use EVAOperation constructor.
        super(NAME, person, false, 20, SkillType.AREOLOGY);
		
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
     		return;
     	}
     	
        // Get an available airlock.
     	if (person.isInside()) {
	        airlock = getWalkableAvailableAirlock(person);
	        if (airlock == null) {
	        	ended = true;
	        	endTask();
	     		return;
	        }
     	}

        // Take bags for collecting regolith.
        if (!hasBags() && person.getInventory().findABag(false) == null) {
            takeBag();

            // If bags are not available, end task.
            if (!hasBags()) {
            	if (person.isOutside()){
                    setPhase(WALK_BACK_INSIDE);
                }
            	else {
                	ended = true;
                	endTask();
             		return;
            	}
            }
        }

        if (!ended) {
            // Determine digging location.
            Point2D.Double diggingLoc = determineDiggingLocation();
            setOutsideSiteLocation(diggingLoc.getX(), diggingLoc.getY());

	        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
	        int strength = nManager.getAttribute(NaturalAttributeType.STRENGTH);
	        int agility = nManager.getAttribute(NaturalAttributeType.AGILITY);
	        int eva = person.getSkillManager().getSkillLevel(SkillType.EVA_OPERATIONS);
	        
	        factor = .9 * (1 - (agility + strength) / 200D);
	        compositeRate  = BASE_COLLECTION_RATE * ((.5 * agility + strength) / 150D) * (eva + .1);
	        
			// set the boolean to true so that it won't be done again today
	//		person.getPreference().setTaskDue(this, true);
			
	       	// Add task phases
        	addPhase(COLLECT_REGOLITH);
        	
            setPhase(WALK_TO_OUTSIDE_SITE);
            
//	        logger.info(person.getName() + " was going to start digging for regolith.");
        }
    }

    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time the phase is to be performed.
     * @return the remaining time after the phase has been performed.
     */
    protected double performMappedPhase(double time) {

        time = super.performMappedPhase(time);
		if (!isDone()) {
	        if (getPhase() == null) {
	        	throw new IllegalArgumentException("Task phase is null");
	        }
	        else if (COLLECT_REGOLITH.equals(getPhase())) {
	            time = collectRegolith(time);
	        }
		}
        return time;
    }

			
	/**
     * Perform collect regolith phase.
     * 
     * @param time time (millisol) to perform phase.
     * @return time (millisol) remaining after performing phase.
     * @throws Exception
     */
    private double collectRegolith(double time) {
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
			return time;
		}
		
    	Inventory pInv = person.getInventory();
        Inventory bInv = pInv.findABag(false).getInventory();
        
        double collected = RandomUtil.getRandomDouble(2) * time * compositeRate;
        
		// Modify collection rate by "Areology" skill.
		int areologySkill = person.getSkillManager().getEffectiveSkillLevel(SkillType.AREOLOGY); 
		if (areologySkill >= 1) {
			collected = collected + .1 * collected * areologySkill;
		}
		else { //if (areologySkill == 0) {
			collected /= 1.5D;
		}

        boolean finishedCollecting = false;
        
        // Introduce randomness into the amount collected so that it will NOT
        // always weigh strangely at exactly 50 kg 
        double rand = RandomUtil.getRandomDouble(1.5);
        
        double personRemainingCap = bInv.getAmountResourceRemainingCapacity(
        		regolithID, false, false);
        
        double bagRemainingCap = bInv.getAmountResourceRemainingCapacity(
        		regolithID, false, false);

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
        	bInv.storeAmountResource(regolithID, collected, true);
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
            logger.log(person, Level.FINE, 0, "Collected a total of " 
            	+ Math.round(totalCollected*100D)/100D 
        		+ " kg regolith."); 
//        		+ person.getCoordinates().getFormattedString() + ".");
            
            if (person.isOutside())
            	setPhase(WALK_BACK_INSIDE);
            else {
            	ended = true;
            	endTask();
         		return 0;
            }
    	}
        
        if (fatigue > 1000 || stress > 50 || hunger > 750 || energy < 1000) {
        	
            logger.log(person, Level.INFO, 4_000, "Had to take a break from collecting regolith ("
        		+ Math.round(totalCollected*100D)/100D + " kg collected) " 
        		+ "; fatigue: " + Math.round(fatigue*10D)/10D 
        		+ "; stress: " + Math.round(stress*100D)/100D + " %"
        		+ "; hunger: " + Math.round(hunger*10D)/10D 
        		+ "; energy: " + Math.round(energy*10D)/10D + " kJ");
            
            if (person.isOutside())
            	setPhase(WALK_BACK_INSIDE);
            
            else {
            	ended = true;
            	endTask();
         		return 0;
            }
        }
        
     	if (person.isInSettlement()) {
            logger.log(person, Level.INFO, 4_000, "Had already been back to the settlement."); 
        	ended = true;
        	endTask();
     		return 0;
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
     * Takes an empty bag
     */
    private void takeBag() {
    	// TODO: need to take a bag before leaving the airlock
    	// TODO: also consider dropping off the regolith in a shed 
    	// or outside of the workshop building for processing
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
            	logger.log(person, Level.WARNING, 10_000, "Was strangely unable to carry an empty bag.");
            	ended = true;
            	super.endTask();
            }
        }
        else {
        	logger.log(person, Level.WARNING, 10_000, "Was unable to find an empty bag in the inventory.");
        	ended = true;
        	super.endTask();
        }
    }

    @Override
    protected TaskPhase getOutsideSitePhase() {
        return COLLECT_REGOLITH;
    }

    /**
     * Determine location for digging regolith.
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
     * Transfer Inventory if the person is inside to teh settlement
     */
    @Override
    protected void clearDown() {
    	if (person.isOutside()) {
    		setPhase(WALK_BACK_INSIDE);
    	}
    	else {
	    	Inventory pInv = person.getInventory();
	    	Bag bag = pInv.findABag(false);

            double reg1 = pInv.getAmountResourceStored(regolithID, false);

            if (reg1 > .0001) {
	          	Inventory sInv = settlement.getInventory();
	          	
	            double settlementCap = sInv.getAmountResourceRemainingCapacity(
	                    regolithID, false, false);
	            
	            if (bag != null && sInv != null) {
		            // Try to store regolith in settlement.
	            	if (reg1 > settlementCap) {
	            		reg1 = settlementCap;
	            		
		            	logger.log(person, Level.FINE, 20_000, 
		            			"Regolith storage full. Could only check in " 
		            			+ Math.round(reg1*10.0)/10.0 + " kg regolith.");
		                		
		//	            bInv.retrieveAmountResource(regolithID, reg0);
		                pInv.retrieveAmountResource(regolithID, reg1);
		                // Store the ice
		                sInv.storeAmountResource(regolithID, reg1, false);
		                // Track supply
		                sInv.addAmountSupply(regolithID, reg1);
			            // Transfer the bag
			            bag.transfer(person, settlement);
						// Add to the daily output
						settlement.addOutput(regolithID, reg1, getTimeCompleted());
			            // Recalculate settlement good value for output item.
			            settlement.getGoodsManager().determineGoodValueWithSupply(GoodsUtil.getResourceGood(regolithID), reg1);
	            	}
	            	
	            	else {
	            		if (reg1 > 0) {
			            	logger.log(person, Level.FINE, 0, 
			            			"Checking in " + Math.round(reg1*10.0)/10.0 + " kg regolith.");
			                		
			//	            bInv.retrieveAmountResource(regolithID, reg0);
			                pInv.retrieveAmountResource(regolithID, reg1);
			                // Store the ice
			                sInv.storeAmountResource(regolithID, reg1, false);
			                // Track supply
			                sInv.addAmountSupply(regolithID, reg1);
				            // Transfer the bag
				            bag.transfer(person, settlement);
							// Add to the daily output
							settlement.addOutput(regolithID, reg1, getTimeCompleted());
				            // Recalculate settlement good value for output item.
				            settlement.getGoodsManager().determineGoodValueWithSupply(GoodsUtil.getResourceGood(regolithID), reg1);
	            		}
		            }
	            }
            }
    	}
    }
}
