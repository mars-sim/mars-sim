/*
 * Mars Simulation Project
 * GatherDataMeta.java
 * @date 2026-08-27
 * @author Manny Kung
 */

package com.mars_sim.core.data.collection.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.data.collection.DataCollectionSite;
import com.mars_sim.core.equipment.EquipmentInventory;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.mission.AbstractVehicleMission;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.structure.Airlock;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.unit.UnitHolder;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * The GatherData class is a task for collecting data outside a settlement.
 */
public abstract class GatherData extends EVAOperation {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(GatherData.class.getName());

	public static final int MAX_SITE_DISTANCE = 100;
	
	/** The precision is 1 cm. 1 km = 100_000 cm */
	public static final double PRECISION = 100_000.0;
	
	public static final double SMALL_AMOUNT = 0.001;
	/** The loading speed of the resource at the storage bin [kg/millisols]. */
	public static final double LOADING_RATE = 10.0;

	public static final String WALK = "walk";
	
	static final TaskPhase COLLECT_DATA = new TaskPhase(Msg.getString(
            "Task.phase.collectGroundData"));
	
	static final TaskPhase TEAR_DOWN = new TaskPhase(Msg.getString(
            "Task.phase.tearDownSite"));

	private boolean isSettlement = false;
	
	private boolean doneDroppingOffInstrument = false;
	
	private int selectedInstrument = -1;
	
	private final int minimumNumInstruments = 1;
	
	private final double preparationTimeLimit;

	private final double collectionTimeLimit;
	
	private final double teardownTimeLimit;
	
	private double compositeRate;

	private double fatigueFactor;
	
	private double preparationTime;
	
	private double collectionTime;
	
	private double teardownTime;
	
	/** Airlock to be used for settlement or vehicular EVA. */
	private Airlock airlock;

	private UnitHolder containerUnit;
	
	private TaskPhase preparePhase;

	private DataCollectionSite dataCollectionSite;
	
	private LocalPosition locationPos;
	
	private EquipmentType dataRecorderType;
			
//	private Map<Integer, Person> operatorMap = new HashMap<>();
	
	/**
	 * Constructor.
	 * 
	 * @param name
	 * @param preparePhase
	 * @param containerType
	 * @param person
	 * @param duration
	 */
	protected GatherData(String name, TaskPhase preparePhase,
					EquipmentType containerType, Person person, int duration) {
        // Use EVAOperation constructor.
        super(name, person, duration, preparePhase);

		setMinimumSunlight(LightLevel.NONE);

        this.dataRecorderType = containerType;
        this.preparePhase = preparePhase;
        this.preparationTimeLimit = duration * 0.2;
        this.collectionTimeLimit = duration * 0.7;
        this.teardownTimeLimit = duration * 0.1;
        
        // To dig local, a person must start at a Settlement
        containerUnit = person.getContainerUnit();
        
        Coordinates coord = null;

        if (containerUnit instanceof Settlement s) {
        	isSettlement = true;
        	// Get the  airlock
            airlock = getWalkableAvailableEgressAirlock(person);
            if (airlock == null) {
            	logger.warning(person, 5_000L, "No available settlement airlock.");
            	endTask();
            	return;
            }
            coord = s.getCoordinates();
 
            dataCollectionSite = findSiteMap(s, coord, true);
            
           	if (dataCollectionSite != null) {
            	findInstrument(s.getEquipmentInventory());
        	}
           	else {
           		logger.warning(person, 5_000L, "No available data collection site found near " + s + ".");
           		endTask();
           		return;
           	}
        }
        
        else if (containerUnit instanceof Vehicle v) {
        	// Get the vehicular airlock
            airlock = ((Rover)v).getAirlock();
            if (airlock == null) {
            	logger.warning(person, 5_000L, "No available vehicular airlock.");
            	endTask();
            	return;
            }
            coord = v.getCoordinates();
            
            if (v.getMission() instanceof AbstractVehicleMission avm) {
            	
            	dataCollectionSite = avm.getDataCollectionSite();
            	
            	if (dataCollectionSite == null) {
            		// The site has not been attached to the abstract vehicle mission yet
                	dataCollectionSite = findSiteMap(v.getAssociatedSettlement(), coord, false);

                }
            	
        		// Note: in future, more than one person may work on this data collection site.
            	// Therefore, it's good to set a reference in AbstractVehicleMission
            	
            	if (dataCollectionSite != null) {
                	// Attach this site to AbstractVehicleMission
                	avm.addDataCollectionSite(dataCollectionSite);
                	
                	findInstrument(v.getEquipmentInventory());
            	}
               	else {
               		logger.warning(person, 5_000L, "No available data collection site found near " + v + ".");
               		endTask();
               		return;
               	}
            }
        }
        
        setOutsideSiteLocation(locationPos);
        
        setPhase(WALK_TO_OUTSIDE_SITE);
    }
	
	/**
	 * Finds an instrument to carry it to the site.
	 * 
	 * @param ei
	 */
	private boolean findInstrument(EquipmentInventory ei) {
		// Look at how many types of instruments a settlement/vehicle would have
    	List<Integer> settlementAvailableList = getAvailableWaterDetectionTool(ei);

        List<Integer> siteAvailableList = dataCollectionSite.getInstrumentAvailability();
        
        if (siteAvailableList.size() > 0) {
        	// already available at the site. For now, no need of deploying another one.
        	logger.info(person, 5_000L, "The site at " + locationPos
        			+ " already had instrument(s). No need to bring more for now.");
        	return false;
        }
        else if (settlementAvailableList.size() > 0){
        	int selected = settlementAvailableList.get(0);
        	// Can a person pick up an instrument from a settlement/vehicle and carries it ?
        	if (carryDataInstrument(EquipmentOwner.getAttached(containerUnit), person, selected)) {
        		selectedInstrument = selected;
        		logger.info(person, 5_000L, "Selected " + ItemResourceUtil.findItemResourceName(selected) 
        			+ " to carry it to the site at " + locationPos + ".");
        		return true;
        	}
        	else {
        		logger.warning(person, 5_000L, "Unable to pick up " + ItemResourceUtil.findItemResourceName(selected) 
    				+ " to carry it to the site at " + locationPos + ".");
        		return false;
        	}
        }
        
        logger.warning(person, 5_000L, "No available instrument in " + ei + " to carry it to the site at " + locationPos + "."); 
        return false;
	}
	
	/**
	 * Gets an available list of water detection tool.
	 * 
	 * @return
	 */
	public List<Integer> getAvailableWaterDetectionTool(EquipmentInventory ei) {
		List<Integer> availableTool = new ArrayList<>();
		
		List<Integer> allInstruments = new ArrayList<>(GatherDataMeta.waterDetectionTool);
		for (int instrumentID: allInstruments) {
			if (ei.getItemResourceStored(instrumentID) > 0)
				availableTool.add(instrumentID);
		}

		return availableTool;
	}
	
	/**
	 * Finds the site map.
	 * 
	 * @param s
	 * @param coord
	 * @param local. Is this in a settlement vicinity ?
	 */
	private DataCollectionSite findSiteMap(Settlement s, Coordinates coord, boolean local) {
	
    	if (locationPos == null) {
        	locationPos = determineSiteLocation();
	        if (locationPos == null) {
				endEVA("No good site found.");
	        	return null;
	        }
        }
		
		Map<Double, List<DataCollectionSite>> siteMap = s.getDataCollectionSiteMap();

        if (siteMap == null) {
        	siteMap = new HashMap<>();
        }

        return settleWithSiteList(s, coord, local, siteMap);
	}
	
	/**
	 * Settles with a list of sites.
	 * 
	 * @param s
	 * @param coord
	 * @param local
	 * @param siteMap
	 * @return
	 */
	private DataCollectionSite settleWithSiteList(Settlement s, Coordinates coord, 
			boolean local, Map<Double, List<DataCollectionSite>> siteMap) {
		
		List<DataCollectionSite> siteList = null;
		
		if (local) {
			if (siteMap.isEmpty()) {
				siteList = new ArrayList<>();
			}
			else {
				// Note: for now, must force distance to be 0.0. 
				// No need of computing distance since it's in settlement vicinity 
				siteList = siteMap.getOrDefault(0.0, new ArrayList<>());
			}
			
			// Note: for now, must force distance to be 0.0. 
			// No need of computing distance since it's in settlement vicinity 
   			return registerSite(s, coord, siteList, 0.0);
		}
		else {
			// Set accuracy to 1 cm
			double distance = Math.round(s.getCoordinates().getDistance(coord) * PRECISION) / PRECISION;
			siteList = siteMap.getOrDefault(distance, new ArrayList<>());

			return registerSite(s, coord, siteList, distance);
		}
	}
	
	/**
	 * Registers a site.
	 * 
	 * @param s
	 * @param coord
	 * @param siteList
	 * @param distance
	 * @return
	 */
	private DataCollectionSite registerSite(Settlement s, Coordinates coord, List<DataCollectionSite> siteList, double distance) {
		DataCollectionSite dataCollectionSite = null;
		
		if (siteList.isEmpty()) {
			// Create and add this site to settlement
        	dataCollectionSite = new DataCollectionSite(coord, locationPos);
        	
        	s.addSite(distance, dataCollectionSite);
		}
		else {
			// Give it a 5% chance to create a new data collection site
			if (RandomUtil.getRandomInt(19) == 0) {
				// Create and add this site to settlement
	        	dataCollectionSite = new DataCollectionSite(coord, locationPos);
	        	
	        	s.addSite(distance, dataCollectionSite);
			}
			else {
//
	        	// Randomly rearrange the site list order 
	        	Collections.shuffle(siteList);
	        	
	        	return siteList.get(0);
	        	
	        	// Note: in future, select a site by probability of its familiarity value
//				int mostNumInstrument = -1;
//	            for (DataCollectionSite site: siteList) {
//	//            	int familiar = site.getFamiliarity();
//	            	int num = site.getNumInstrumentAvailable();
//	            	if (num > mostNumInstrument) {
//	            		dataCollectionSite = site;
//	            		break;
//	            	}
//	            }
			}
		}
		
		return dataCollectionSite;
	}
	
	/**
	 * Where will any resources be dropped off on the surface
	 */
	LocalPosition getSelectedSite() {
		return locationPos;
	}

	/**
	 * Moves an instrument from a settlement/vehicle to a person.
	 *
	 * @param holder the current equipment holder
	 * @param person
	 * @param intrumentID
	 */
	public boolean carryDataInstrument(EquipmentOwner holder, Person person, int intrumentID) {
		if (!hasInstrument(person, intrumentID) && holder.hasItemResource(intrumentID)) {
			if (retrieveInstrument(holder, intrumentID)) {
				return person.getEquipmentInventory().storeItemResource(intrumentID, 1) == 0;
			}
		}
		
		return false;
	}

	/**
	 * Moves an instrument from a person to a site
	 *
	 * @param holder the current equipment holder
	 * @param person
	 * @param intrumentID
	 */
	public boolean moveInstrumentPersonToSite(Person person, DataCollectionSite site, int intrumentID) {
		if (hasInstrument(person, intrumentID) && !site.hasInstrument(intrumentID)) {
			if (person.getEquipmentInventory().retrieveItemResource(intrumentID, 1) == 0) {
				return site.addInstrument(intrumentID);
			}
		}
		
		return false;
	}

	/**
	 * Moves an instrument from a site to a person
	 *
	 * @param holder the current equipment holder
	 * @param person
	 * @param intrumentID
	 */
	public boolean moveInstrumentSiteToPerson(DataCollectionSite site, Person person, int intrumentID) {
		if (!hasInstrument(person, intrumentID) && site.hasInstrument(intrumentID)) {
			if (site.removeInstrument(intrumentID)) {
				return person.getEquipmentInventory().storeItemResource(intrumentID, 1) == 0;
			}
		}
		
		return false;
	}
	
	/**
	 * Moves an instrument from a person to a settlement/vehicle.
	 *
	 * @param holder the current equipment holder
	 * @param person
	 * @param intrumentID
	 */
	public boolean carryDataInstrument(Person person, EquipmentOwner holder, int intrumentID) {
		if (hasInstrument(person, intrumentID) && !holder.hasItemResource(intrumentID)) {
			if (person.getEquipmentInventory().retrieveItemResource(intrumentID, 1) == 0) {
				return holder.storeItemResource(intrumentID, 1) == 0;
			}
		}
		
		return false;
	}
	
	/**
	 * Does this equipment owner have this instrument ?
	 * 
	 * @param holder
	 * @param intrumentID
	 * @return
	 */
	public boolean hasInstrument(EquipmentOwner holder, int intrumentID) {
		return holder.getItemResourceStored(intrumentID) > 0;
	}

	/**
	 * Does this person have this instrument ?
	 * 
	 * @param person
	 * @param intrumentID
	 * @return
	 */
	public boolean hasInstrument(Person person, int intrumentID) {
		return person.getEquipmentInventory().getItemResourceStored(intrumentID) > 0;
	}
	
	
	/**
	 * Retrieves an instrument.
	 * 
	 * @param holder
	 * @param intrumentID
	 * @return
	 */
	private boolean retrieveInstrument(EquipmentOwner holder, int intrumentID) {
		return holder.retrieveItemResource(intrumentID, 1) == 0;
	}
	
	/**
	 * Determines the data collection factors.
	 * 
	 * @param collectionRate
	 */
	protected void determineCollectionFactors(double collectionRate) {
        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        int acad = nManager.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
        int areo = person.getSkillManager().getSkillLevel(SkillType.AREOLOGY);
        int meti = nManager.getAttribute(NaturalAttributeType.METICULOUSNESS);
        
        int creat = nManager.getAttribute(NaturalAttributeType.CREATIVITY);
        int agility = nManager.getAttribute(NaturalAttributeType.AGILITY);
        
        // Increase the duration of this task based upon one's attribute
        setDuration(getDuration() * (1 + meti/200.0));
        
        fatigueFactor = .5 * (1 - (agility + creat) / 200D) / (1 + collectionRate);
        // The higher the collection rate, the faster the data can be gathered
		compositeRate = collectionRate * ((acad + meti) / 100D) * areo / 50;
	}

    /**
     * Performs the method mapped to the task's current phase.
     * 
     * @param time the amount of time the phase is to be performed.
     * @return the remaining time after the phase has been performed.
     */
	@Override
    protected double performMappedPhase(double time) {

        time = super.performMappedPhase(time);
		if (!isDone()) {
	        if (getPhase() == null) {
				logger.severe(worker, "Task phase is null.");
	        }
	        if (preparePhase.equals(getPhase())) {
	            time = prepareInstrumentPhase(time);
	        }
	        else if (COLLECT_DATA.equals(getPhase())) {
	            time = collectDataPhase(time);
	        }
	        else if (TEAR_DOWN.equals(getPhase())) {
	            time = teardownPhase(time);
	        }
		}
        return time;
    }

    /**
     * Drops off and assemble instruments at the site.
     * 
     * @param time
     * @return
     */
	private double prepareInstrumentPhase(double time) {
		
		if (checkReadiness(time) > 0) {
			endEVA("Failing readiness.");
			return time;
		}

		if (selectedInstrument != -1 && !doneDroppingOffInstrument) {
	    	if (moveInstrumentPersonToSite(person, dataCollectionSite, selectedInstrument)) {
	    		logger.info(person, 5_000L, "Successfully dropped off " + ItemResourceUtil.findItemResourceName(selectedInstrument) 
	    			+ " at " + locationPos + ".");
	    		doneDroppingOffInstrument = true;
	    	}
	    	else {
	    		logger.warning(person, 5_000L, "Unable to drop off " + ItemResourceUtil.findItemResourceName(selectedInstrument) 
					+ " at " + locationPos + ".");
	    		return time * .5;
	    	}
		}
		
		int num = dataCollectionSite.getNumInstrumentAvailable();
		
		// Note: the code below is temporary and is for testing only
		if (num >= minimumNumInstruments) {	
		
	       double skillFactor = 1;

			// Modify collection rate by skill.
			int skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);
			if (skill >= 1) {
				skillFactor = skillFactor + .1 * skillFactor * skill;
			}
			else {
				skillFactor /= 1.5D;
			}

	        boolean finishedPreparing = false;

	        preparationTime += time * skillFactor * compositeRate;

	        // See if it exceeds the prescribed preparation time limit
			finishedPreparing = preparationTime >= preparationTimeLimit || getTimeCompleted() >= preparationTimeLimit;

	        PhysicalCondition condition = person.getPhysicalCondition();
	        double strengthMod = condition.getStrengthMod();
	        double skillMod = 1.0 + skill;		
	        		
	        // Add penalty to the fatigue
	        condition.increaseFatigue(time / 2 * fatigueFactor * (1.1D - strengthMod)/skillMod);

	        // Account for hormone regulation, musculosketetal impact and record exercise time
	        condition.trackExercise(time / 2);
			
	        // Add experience points
	        addExperience(time);
	        
		    // Check for an accident during the EVA operation.
		    checkForAccident(time);

	        if (finishedPreparing) {
	            logger.info(person, 5_000, "Done with site and '" + ItemResourceUtil.findItemResourceName(selectedInstrument) 
	            	+ "' preparation at " + locationPos + ".");
	           
	            setPhase(COLLECT_DATA);
	            
				logger.info(person, 5_000, "Starting the collecting data phase at " + locationPos + ".");
	    	}
		}
		
    	return 0;
    }
	
	/**
     * Performs collect resource phase.
     *
     * @param time time (millisol) to perform phase.
     * @return time (millisol) remaining after performing phase.
     * @throws Exception
     */
    private double collectDataPhase(double time) {
    	
		int num = dataCollectionSite.getNumInstrumentAvailable();
		
		if (checkReadiness(time) > 0) {
			if (num == 0) {
				// Has resources in container
				setPhase(WALK_BACK_INSIDE);
			}
			else
				endEVA("Failing readiness.");
			return time;
		}
    	
		// Note: the code below is temporary and is for testing only
		if (num >= minimumNumInstruments) {	
		
	       double skillFactor = 1;

			// Modify collection rate by skill.
			int skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.COMPUTING);
			if (skill >= 1) {
				skillFactor = skillFactor + .1 * skillFactor * skill;
			}
			else {
				skillFactor /= 1.5D;
			}

	        boolean finishedPreparing = false;

	        collectionTime += time * skillFactor * compositeRate;

	        // See if it exceeds the prescribed collection time limit
			finishedPreparing = collectionTime >= collectionTimeLimit 
					|| getTimeCompleted() >= preparationTimeLimit + collectionTimeLimit;

	        PhysicalCondition condition = person.getPhysicalCondition();
	        double strengthMod = condition.getStrengthMod();
	        double skillMod = 1.0 + skill;		
	        		
	        // Add penalty to the fatigue
	        condition.increaseFatigue(time / 2 * fatigueFactor * (1.1D - strengthMod)/skillMod);

	        // Account for hormone regulation, musculosketetal impact and record exercise time
	        condition.trackExercise(time / 2);
			
	        // Add experience points
	        addExperience(time);
	        
		    // Check for an accident during the EVA operation.
		    checkForAccident(time);

	        if (finishedPreparing) {
	            logger.info(person, 5_000, "Done with gathering data using '" + ItemResourceUtil.findItemResourceName(selectedInstrument) 
	            	+ "' at " + locationPos + ".");
	           
	            setPhase(TEAR_DOWN);
	            
				logger.info(person, 5_000, "Starting to disassemble the intrument(s) at " + locationPos + ".");
	    	}
		}

        return 0;
    }
 
    /**
     * Tears down the setup for instruments at the site.
     * 
     * @param time
     * @return
     */
	private double teardownPhase(double time) {
		
		if (checkReadiness(time) > 0) {
			endEVA("Failing readiness.");
			return time;
		}
		
		int num = dataCollectionSite.getNumInstrumentAvailable();
		
		// Note: the code below is temporary and is for testing only
		if (num >= minimumNumInstruments) {	
		
	       double skillFactor = 1;

			// Modify collection rate by skill.
			int skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);
			if (skill >= 1) {
				skillFactor = skillFactor + .1 * skillFactor * skill;
			}
			else {
				skillFactor /= 1.5D;
			}

	        boolean finishedPreparing = false;

	        teardownTime += time * skillFactor * compositeRate;

	        // See if it exceeds the prescribed tear down time limit
			finishedPreparing = teardownTime >= teardownTimeLimit 
					|| getTimeCompleted() >= getDuration();

	        PhysicalCondition condition = person.getPhysicalCondition();
	        double strengthMod = condition.getStrengthMod();
	        double skillMod = 1.0 + skill;		
	        		
	        // Add penalty to the fatigue
	        condition.increaseFatigue(time / 4 * fatigueFactor * (1.1D - strengthMod)/skillMod);

	        // Account for hormone regulation, musculosketetal impact and record exercise time
	        condition.trackExercise(time / 4);
			
	        // Add experience points
	        addExperience(time);
	        
		    // Check for an accident during the EVA operation.
		    checkForAccident(time);
		    
	        if (finishedPreparing) {
	
	    		if (selectedInstrument != -1) {
	    	    	if (moveInstrumentSiteToPerson(dataCollectionSite, person, selectedInstrument)) {
	    	            logger.info(person, 5_000, "Done with tearing down the site, picking up '" 
	    	            		+ ItemResourceUtil.findItemResourceName(selectedInstrument) 
	    	            		+ "' at " + locationPos + ".");
	    	    	}
	    	    	else {
	    	    		logger.warning(person, 5_000L, "Unable to tear down the site and pick up " 
	    	    				+ ItemResourceUtil.findItemResourceName(selectedInstrument) 
	    	    				+ " at " + locationPos + ".");
	    	    		return time * .5;
	    	    	}
	    		}
	            
				logger.info(person, 5_000, "Ending the tear down phase at " + locationPos + ".");
				
	            endTask();
	    	}
		}
		
    	return 0;
    }    
    
    /**
     * Determines location for the site.
     * 
     * @return X and Y location outside settlement.
     */
    private LocalPosition determineSiteLocation() {
    	
    	if (isSettlement) {
    		setRandomOutsideLocation((Building)airlock.getEntity());
        	return getOutsideSiteLocation();
    	}
    	else {
        	setRandomOutsideLocation(person.getVehicle());
        	return getOutsideSiteLocation();
    	}
    	
//		if (airlock.getEntity() instanceof LocalBoundedObject boundedObject) {
//			return LocalAreaUtil.getCollisionFreeRandomPosition(boundedObject,
//																 person.getCoordinates(), MAX_SITE_DISTANCE);
//		}
//      return null;
    }
    
	/**
	 * Is the person qualified for gathering data ?
	 * 
	 * @return
	 */
	public static boolean canGatherData(Person person) {

		// Note: check egress airlock is already covered by another method 
				
		// Check if sunlight is insufficient (will check 
		// if a person is located in dark polar region)
		if (EVAOperation.isGettingDark(person))
			return false;

		// Check if person's medical condition will not allow task.
		if (person.getPerformanceRating() < .2D)
			return false;
		
		// Check for fitness
		if (isSuperUnfit(person)) {
	      	return false;
		}
		
		return true;
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	@Override
	public void destroy() {
		worker = null;
		airlock = null;
		locationPos = null;
		dataRecorderType = null;

		super.destroy();
	}
}
