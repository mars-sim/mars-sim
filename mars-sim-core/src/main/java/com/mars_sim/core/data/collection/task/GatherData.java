/*
 * Mars Simulation Project
 * GatherDataMeta.java
 * @date 2026-08-27
 * @author Manny Kung
 */

package com.mars_sim.core.data.collection.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.data.collection.DataCollectionSite;
import com.mars_sim.core.equipment.DataRecorder;
import com.mars_sim.core.equipment.EquipmentInventory;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.mission.AbstractVehicleMission;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
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
	
	/** The precision is 1 m. 1 km = 100_000 cm */
	public static final double PRECISION = 1_000.0;
	
	public static final double SMALL_AMOUNT = 0.001;
	/** The loading speed of the resource at the storage bin [kg/millisols]. */
	public static final double LOADING_RATE = 10.0;

	public static final String WALK = "walk";
	
	static final TaskPhase COLLECT_DATA = new TaskPhase(Msg.getString(
            "Task.phase.collectGroundData"));
	
	static final TaskPhase TEAR_DOWN = new TaskPhase(Msg.getString(
            "Task.phase.tearDownSite"));

	private boolean isSettlement = false;
	private boolean instrumentDropped = false;
	private boolean dataRecorderDropped = false;
	private boolean isNewRecording = true;

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
	private SiteVisit siteVisit;
	
	private Part selectedInstrument;
	private DataRecorder selectedDataRecorder;
	
	private EquipmentType containerType;

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
        super(name, person, person.getAssociatedSettlement(), duration, preparePhase);

        this.containerType = containerType;
        this.preparePhase = preparePhase;
        this.preparationTimeLimit = duration * 0.1;
        this.collectionTimeLimit = duration * 0.8;
        this.teardownTimeLimit = duration * 0.1;
        this.containerUnit = worker.getContainerUnit();
        	
		setMinimumSunlight(LightLevel.NONE);
        
        Coordinates coord = null;

        if (containerUnit instanceof Settlement s) {
        	isSettlement = true;
        	// Get the  airlock
            airlock = getWalkableAvailableEgressAirlock(worker);
            if (airlock == null) {
            	logger.warning(worker, 15_000L, "No available settlement airlock.");
                endEVA("No available settlement airlock.");
            	return;
            }
            coord = s.getCoordinates();
 
            dataCollectionSite = findSiteMap(s, coord, isSettlement);
            
           	if (dataCollectionSite == null) {
           		logger.warning(worker, 15_000L, "No available DCS found near " + s + ".");
                endEVA("No available DCS found near " + s + ".");
           		return;
           	}
           	
//           	if (locationPos == null) {
//           		logger.warning(worker, 5_000L, "No spare local position at DCS near " + s + ".");
//                endEVA("No spare locationPos near " + s + ".");
//           		return;
//           	}
           	
            boolean hasInstrument = false;
            boolean hasDataRecorder = false;
            
           	if (dataCollectionSite != null && locationPos != null) {
 
               	Worker primary = dataCollectionSite.getPrimaryOperator();
            	Worker secondary = dataCollectionSite.getPrimaryOperator();
               
            	if (primary == null) {
            		dataCollectionSite.setPrimaryOperator(worker);
            	}
            	else if (secondary == null) {
            		dataCollectionSite.setSecondaryOperator(secondary);
            	}
            	else {
                	logger.warning(worker, 15_000L, "No available operator slot.");
                    endEVA("No available operator slot.");
                	return;
            	}
            	
                // Obtain a SiteVisit instance
                siteVisit = dataCollectionSite.checkSiteVisit(Simulation.instance().getMasterClock().getMarsTime().getMissionSol(), worker);
                
           		hasInstrument = findInstrument(s.getEquipmentInventory());
           		hasDataRecorder = findDataRecorder(s.getEquipmentInventory());
        	}

           	if (!hasInstrument) {
           		logger.warning(worker, 15_000L, "No instrument available for DCS near " + s + ".");
                endEVA("No instrument available for DCS near " + s + ".");
           		return;
           	}
           	
           	if (!hasDataRecorder) {
           		logger.warning(worker, 15_000L, "No data recorder available for DCS near " + s + ".");
                endEVA("No data recorder available for DCS near " + s + ".");
           		return;
           	}
        }
        
        else if (containerUnit instanceof Vehicle v) {
        	// Get the vehicular airlock
            airlock = ((Rover)v).getAirlock();
            if (airlock == null) {
            	logger.warning(worker, 15_000L, "No available vehicular airlock.");
                endEVA("No available vehicular airlock.");
            	return;
            }
            coord = v.getCoordinates();
            
            if (v.getMission() instanceof AbstractVehicleMission avm) {
            	
            	dataCollectionSite = avm.getDataCollectionSite();
            	
            	if (dataCollectionSite == null && locationPos != null) {
            		// The site has not been attached to the abstract vehicle mission yet
                	dataCollectionSite = findSiteMap(v.getAssociatedSettlement(), coord, isSettlement);
                }
            	
               	if (dataCollectionSite == null) {
               		logger.warning(worker, 15_000L, "No available DCS found near " + v + ".");
                    endEVA("No available DCS found near " + v + ".");
               		return;
               	}
               	
//               	if (locationPos == null) {
//               		logger.warning(worker, 5_000L, "No spare local position at DCS near " + v + ".");
//                    endEVA("No spare locationPos near " + v + ".");
//               		return;
//               	}
               	
        		// Note: in future, more than one person may work on this data collection site.
            	// Therefore, it's good to set a reference in AbstractVehicleMission
            	 boolean hasInstrument = false;
                 boolean hasDataRecorder = false;
                 
            	if (dataCollectionSite != null) {
            		
                  	Worker primary = dataCollectionSite.getPrimaryOperator();
                	Worker secondary = dataCollectionSite.getPrimaryOperator();
                   
                	if (primary == null) {
                		dataCollectionSite.setPrimaryOperator(worker);
                	}
                	else if (secondary == null) {
                		dataCollectionSite.setSecondaryOperator(secondary);
                	}
                	else {
                    	logger.warning(worker, 15_000L, "No available operator slot.");
                        endEVA("No available operator slot.");
                    	return;
                	}
                	
                    // Obtain a SiteVisit instance
                    siteVisit = dataCollectionSite.checkSiteVisit(Simulation.instance().getMasterClock().getMarsTime().getMissionSol(), worker);
                	// Attach this site to AbstractVehicleMission
                	avm.addDataCollectionSite(dataCollectionSite);
                	hasInstrument = findInstrument(v.getEquipmentInventory());
               		hasDataRecorder = findDataRecorder(v.getEquipmentInventory());
            	}
               	
               	if (!hasInstrument) {
               		logger.warning(worker, 15_000L, "No instrument available for DCS near " + v + ".");
                    endEVA("No instrument available for DCS near " + v + ".");
               		return;
               	}
               	
               	if (!hasDataRecorder) {
               		logger.warning(worker, 15_000L, "No data recorder available for DCS near " + v + ".");
                    endEVA("No data recorder available for DCS near " + v + ".");
               		return;
               	}
            }
        }
        
        setOutsideSiteLocation(locationPos);
        
        setPhase(WALK_TO_OUTSIDE_SITE);
    }
	

	
	/**
	 * Finds a data recorder to carry it to the site.
	 * 
	 * @param ei
	 */
	private boolean findDataRecorder(EquipmentInventory ei) {
		
//		List<DataRecorder> recorders = siteVisit.getDataRecorders();
//		
//		if (!recorders.isEmpty()) {
//        	// already available at the site. For now, no need of deploying another one.
//        	logger.info(worker, 5_000L, "The site at " + locationPos
//        			+ " already had data recorder(s). No need to bring more for now.");
//        	return false;
//        }
//        else {
        	// Can a person pick up an instrument from a settlement/vehicle and carries it ?
        	if (carryDataRecorder(ei, person)) {
        		
        		logger.info(person, 5_000L, "Selected " + selectedDataRecorder.getName() 
        			+ " to carry it to the site at " + locationPos + ".");
        		return true;
        	}
        	else {
        		logger.warning(person, 5_000L, "Unable to pick up any data recorder "
    				+ " to carry it to the site at " + locationPos + ".");
        		return false;
        	}
//        }	
	}
	
	
	/**
	 * Finds an instrument to carry it to the site.
	 * 
	 * @param ei
	 */
	private boolean findInstrument(EquipmentInventory ei) {
		// Look at how many types of instruments a settlement/vehicle would have
    	int availableTool = selectWaterDetectionTool(ei);

    
//        List<Integer> siteAvailableList = siteVisit.getInstrumentAvailability();
//        
//        if (!siteAvailableList.isEmpty()) {
//        	// already available at the site. For now, no need of deploying another one.
//        	logger.info(worker, 5_000L, "The site at " + locationPos
//        			+ " already had instrument(s). No need to bring more for now.");
//        	return true;
//        }
        if (availableTool != -1) {
        	// Can a person pick up an instrument from a settlement/vehicle and carries it ?
        	if (carryDataInstrument(EquipmentOwner.getAttached(containerUnit), person, availableTool)) {
        		selectedInstrument = ItemResourceUtil.findItemResource(availableTool);
        		logger.info(worker, 5_000L, "Selected " + selectedInstrument.getName() 
        			+ " to carry it to the site at " + locationPos + ".");
        		return true;
        	}
        	else {
        		logger.warning(worker, 5_000L, "Unable to pick up any instruments "
    				+ " to carry it to the site at " + locationPos + ".");
        		return false;
        	}
        }
        else {
            logger.warning(containerUnit, 5_000L, "Instruments not available for " + person 
            		+ " to carry to the site at " + locationPos + "."); 
            return false;
        }
	}
	
	/**
	 * Selects an available water detection tool.
	 * 
	 * @return
	 */
	public int selectWaterDetectionTool(EquipmentInventory ei) {
		List<Integer> allInstruments = new ArrayList<>(GatherDataMeta.waterDetectionTool);
		Collections.shuffle(allInstruments);
		
		for (int instrumentID: allInstruments) {
			if (ei.hasItemResource(instrumentID))
				return instrumentID;
		}

		return -1;
	}
	
	/**
	 * Finds the site map.
	 * 
	 * @param settlement the associated settlement
	 * @param coord the coordinates of the prospective site
	 * @param local. Is this in a settlement vicinity ?
	 */
	private DataCollectionSite findSiteMap(Settlement settlement, Coordinates coord, boolean local) {
	
		DataCollectionSite dataCollectionSite = null;
    	List<DataCollectionSite> siteList = null;
    	Map<Double, List<DataCollectionSite>> siteMap = settlement.getDataCollectionSiteMap();
    	
    	double distance = 0.0;
    	int numExistingSites = 0;
    	
		if (local) {
			if (siteMap.isEmpty()) {
				siteList = new ArrayList<>();
			}
			else {
				// Note: for now, must force distance to be 0.0. 
				// No need of computing distance since it's in settlement vicinity 
				siteList = siteMap.getOrDefault(0.0, new ArrayList<>());
				if (siteList == null) {
					siteList = new ArrayList<>();
				}
			}
			
			// Note: for now, must force distance to be 0.0. 
			// No need of computing distance since it's in settlement vicinity 
			
		}
		else {
			// Set accuracy
			distance = Math.round(settlement.getCoordinates().getDistance(coord) * PRECISION) / PRECISION;
			siteList = siteMap.getOrDefault(distance, new ArrayList<>());
			if (!siteList.isEmpty())
				siteList = siteList.stream()
				    .filter(site -> site.getLocation().equals(coord))
				    .collect(Collectors.toList());
			if (siteList == null) {
				siteList = new ArrayList<>();
			}
		}
		
		if (!siteList.isEmpty()) {
			Collections.shuffle(siteList);
			numExistingSites = siteList.size();
		}
		
		// Note: even if there are existing sites, give it a chance to start a new site
		if (siteList.isEmpty() || RandomUtil.getRandomInt(numExistingSites + 100) == 0) {
			
	    	if (locationPos == null) {
	        	locationPos = determineSiteLocation(coord, settlement, local);
		        if (locationPos == null) {
					endEVA("No good location position found.");
		        	return null;
		        }
	        }
	    	
	    	if (locationPos != null) { 
				// Create and add this site to settlement
		    	dataCollectionSite = new DataCollectionSite(coord, settlement, locationPos);
		    	settlement.addSite(distance, dataCollectionSite);
	    	}
		}
		else {
			// Go back to an old DCS
			dataCollectionSite = siteList.get(0);
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
     * Determines location for the site.
     * 
     * @param coord the coordinates of the prospective site
     * @param settlement the associated settlement
     * @return X and Y local position of the site .
     */
    private LocalPosition determineSiteLocation(Coordinates coord, Settlement settlement, boolean local) {
    	
    	if (isSettlement) {
    		LocalBoundedObject lbo = (Building)airlock.getEntity();
    		
    		int num = person.getSettlement().getLocalDataCollectionSitesList().size();
    		// Give it a chance to pick an existing site
    		if (num > 0 && RandomUtil.getRandomInt(5) <= 3) {
    			List<DataCollectionSite> list = person.getSettlement().getLocalDataCollectionSitesList();
    			if (num > 1)
    				Collections.shuffle(list);
    			// Choose an existing DCS as the origin
    			lbo = list.get(0);
    		}
    		
    		boolean found = false;

    		for (int i = 0; i<50 && !found; i++) {	
    			found = findRandomDataCollectionOutsideLoc(lbo, coord, person.getSettlement());
    		}
    		
    		if (found)
    			return getOutsideSiteLocation();
    		else {
                endEVA("No good outside location found.");
    			logger.warning(worker, "Can not find a suitable random EVA location.");
    			return null;
    		}
    	}
    	else {
        	setRandomOutsideLocation(person.getVehicle());
        	return getOutsideSiteLocation();
    	}
    }

	/**
	 * Retrieves a data recorder from a settlement/vehicle and move it to a worker.
	 *
	 * @param inv the current equipment inventory
	 * @param worker
	 */
	public boolean carryDataRecorder(EquipmentInventory inv, Person person) {
		if (!person.getEquipmentInventory().containsEquipment(EquipmentType.DATA_RECORDER)
				&& !inv.getRecorderSet().isEmpty()) {
			DataRecorder dr = inv.retrieveOwnedDataRecorder(person.getIdentifier(), false);
			
			if (dr != null && dr.transfer(person)) {
				selectedDataRecorder = dr;
				dr.setRegisteredOwner(person);
				return true;
			}
		}
		return false;
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
	 * Moves an data recorder from a person to a site.
	 *
	 * @param person
	 * @param recorder
	 */
	public boolean moveDataRecorderPersonToSite(Person person, DataRecorder recorder) {
//		if (person.getEquipmentInventory().containsEquipment(EquipmentType.DATA_RECORDER)) {
			// need to debug 
//		}
		
		if (!siteVisit.hasDataRecorder(recorder)) {
			DataRecorder dr = person.retrieveOwnedDataRecorder(person.getIdentifier(), false);
			if (dr != null) {
				dr.transfer(unitManager.getMarsSurface());
				return siteVisit.addDataRecorder(dr);
			}
		}
		
		return false;
	}
	/**
	 * Moves an instrument from a person to a site.
	 *
	 * @param person
	 * @param intrumentID
	 */
	public boolean moveInstrumentPersonToSite(Person person, int intrumentID) {
		if (hasInstrument(person, intrumentID) && !siteVisit.hasInstrument(intrumentID)) {
			if (person.getEquipmentInventory().retrieveItemResource(intrumentID, 1) == 0) {
				return siteVisit.addInstrument(intrumentID);
			}
		}
		
		return false;
	}
	
	/**
	 * Moves a data recorder from a site to a person.
	 *
	 * @param person
	 * @param intrumentID
	 */
	public boolean moveDataRecorderSiteToPerson(Person person, DataRecorder recorder) {
		if (!person.getEquipmentInventory().containsEquipment(EquipmentType.DATA_RECORDER)
				&& siteVisit.hasDataRecorder(recorder)) {
			if (siteVisit.removeDataRecorder(recorder)) {
				return recorder.transfer(person);
			}
		}
		
		return false;
	}
	
	/**
	 * Moves an instrument from a site to a person.
	 *
	 * @param person
	 * @param intrumentID
	 */
	public boolean moveInstrumentSiteToPerson(Person person, int intrumentID) {
		if (!hasInstrument(person, intrumentID) && siteVisit.hasInstrument(intrumentID)) {
			if (siteVisit.removeInstrument(intrumentID)) {
				return person.getEquipmentInventory().storeItemResource(intrumentID, 1) == 0;
			}
		}
		
		return false;
	}
	
	/**
	 * Retrieves a data recorder from a person and move it to a settlement/vehicle.
	 *
	 * @param worker
	 * @param eo the new equipment owner
	 */
	public boolean carryDataRecorder(Person person, UnitHolder uh) {
//		if (person.getEquipmentInventory().containsEquipment(EquipmentType.DATA_RECORDER)) {
			// need to debug " did own " + selectedDataRecorder);
//		}
		 if (containerUnit instanceof Settlement s) {
			return selectedDataRecorder.transfer(s);
		 }
		 else if (containerUnit instanceof Vehicle v) {
			return selectedDataRecorder.transfer(v);
		}
		return false;
	}
	
	/**
	 * Moves an instrument from a person to a settlement/vehicle.
	 *
	 * @param person
	 * @param holder the current equipment holder
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
		return holder.hasItemResource(intrumentID);
	}

	/**
	 * Does this person have this instrument ?
	 * 
	 * @param person
	 * @param intrumentID
	 * @return
	 */
	public boolean hasInstrument(Person person, int intrumentID) {
		return person.getEquipmentInventory().hasItemResource(intrumentID);
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

		if (!instrumentDropped) {
			
	    	if (moveInstrumentPersonToSite(person, selectedInstrument.getID())) {
	    		logger.info(person, 5_000L, "Successfully dropped off '" + selectedInstrument.getName()
	    			+ "' at " + locationPos + ".");
	    		
	    		instrumentDropped = true;
	    	}

	    	if (!instrumentDropped) {
	    		logger.warning(person, 5_000L, "Unable to drop off '" + selectedInstrument.getName()
					+ "' at " + locationPos + ".");
	    		endEVA("Unable to drop off '" + selectedInstrument.getName() 
						+ "' at " + locationPos + ".");
	    		
	    		return time * .5;
	    	}
		}
		
		if (!dataRecorderDropped) {
			
			if (selectedDataRecorder != null && moveDataRecorderPersonToSite(person, selectedDataRecorder)) {
	    		logger.info(person, 5_000L, "Successfully dropped off '" + selectedDataRecorder 
	    			+ "' at " + locationPos + ".");
	    		
	    		dataRecorderDropped = true;
	    	}
			
	    	if (!dataRecorderDropped) {
	    		logger.warning(worker, 5_000L, "Unable to drop off '" + selectedDataRecorder 
					+ "' at " + locationPos + ".");
	    		endEVA("Unable to drop off '" + selectedDataRecorder 
						+ "' at " + locationPos + ".");
	    		
	    		return time * .5;
	    	}
		}

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

        preparationTime += time * (skillFactor + compositeRate);

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

            logger.info(person, 5_000, "Done with preparing the site and '" + selectedInstrument.getName() 
            	+ "' at " + locationPos + ".");
           
            setPhase(COLLECT_DATA);
            
			logger.info(person, 5_000, "Starting the collecting data phase at " + locationPos + ".");
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

		if (checkReadiness(time) > 0) {
			endEVA("Failing readiness.");
			return time;
		}

		if (!siteVisit.getInstrumentAvailability().isEmpty()
			&& !siteVisit.getDataRecorders().isEmpty()) {
		
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

	        double addTime = time * (skillFactor + compositeRate);
	        
	        collectionTime += addTime;

	        if (isNewRecording) {
	        	// Start a new data session and record data
	        	selectedDataRecorder.recordData(worker, addTime, (int)skillFactor, true);
	        	
	        	isNewRecording = false;
	        }
	        else {
	        	// Record data
	        	selectedDataRecorder.recordData(worker, addTime, (int)skillFactor, false);
	        }

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
	    			
	            logger.info(person, 5_000, "Done with gathering data using '" + selectedInstrument.getName() 
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

        teardownTime += time * (skillFactor + compositeRate);

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

    		if (selectedInstrument != null) {
    	
    	    	if (moveInstrumentSiteToPerson(person, selectedInstrument.getID())) {
    	            logger.info(person, 5_000, "Done tearing down & picking up '" 
    	            		+ selectedInstrument.getName() 
    	            		+ "' at " + locationPos + ".");
    	    	}
    	    	else {
    	    		logger.warning(person, 5_000L, "Unable to tear down & pick up '" 
    	    				+ selectedInstrument.getName()
    	    				+ "' at " + locationPos + ".");
    	    	}
    		}
    		
    		if (selectedDataRecorder != null) {
    			
    	    	if (moveDataRecorderSiteToPerson(person, selectedDataRecorder)) {
    	            logger.info(person, 5_000, "Done tearing down & picking up '" 
    	            		+ selectedDataRecorder 
    	            		+ "' at " + locationPos + ".");
    	    	}
    	    	else {
    	    		logger.warning(person, 5_000L, "Unable to tear down & pick up '" 
    	    				+ selectedDataRecorder
    	    				+ "' at " + locationPos + ".");
    	    	}
    		}
            
			logger.info(person, 5_000, "Ending the tear down phase at " + locationPos + ".");
			
            endEVA("Ended tear down phase.");
    	}
		
    	return 0;
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
	 * Ends the EVA.
	 * 
	 * @param reason
	 */
	public void endEVA(String reason) {
		
    	Worker primary = dataCollectionSite.getPrimaryOperator();
    	Worker secondary = dataCollectionSite.getPrimaryOperator();
       
    	if (primary != null && worker.equals(primary)) {
    		dataCollectionSite.setPrimaryOperator(null);
    	}
    	else if (secondary != null && worker.equals(secondary)) {
    		dataCollectionSite.setSecondaryOperator(null);
    	}
		
		if (selectedInstrument != null) {
			
			if (siteVisit.hasInstrument(selectedInstrument.getID()) 
					&& moveInstrumentSiteToPerson(person, selectedInstrument.getID())) {
	            logger.info(person, 5_000, "Picking up '" 
	            		+ selectedInstrument.getName() 
	            		+ "' at " + locationPos + ".");
	    	}

			// Can a person pick up an instrument from a settlement/vehicle and carries it ?
	    	if (hasInstrument(person, selectedInstrument.getID())
	    			&& carryDataInstrument(person, EquipmentOwner.getAttached(containerUnit), selectedInstrument.getID())) {
	    		logger.info(person, 5_000L, "Returned '" + selectedInstrument.getName() 
	    			+ "' to " + containerUnit + ".");

	    	}
	    	else {
	    		logger.warning(person, 5_000L, "Unable to return '" + selectedInstrument.getName()
					+ "' to " + containerUnit + ".");
	    	}	
		}	
		
		if (selectedDataRecorder != null) {
			
			if (siteVisit.hasDataRecorder(selectedDataRecorder) 
					&& moveDataRecorderSiteToPerson(person, selectedDataRecorder)) {
	            logger.info(person, 5_000, "Done with tearing down the site, picking up '" 
	            		+ selectedDataRecorder 
	            		+ "' at " + locationPos + ".");
	    	}
			
			// Can a person pick up a data recorder from a settlement/vehicle and carries it ?
	    	if (worker.getEquipmentInventory().containsEquipment(EquipmentType.DATA_RECORDER)
	    			&& carryDataRecorder(person, containerUnit)) {
	    		logger.info(person, 5_000L, "Returned '" + selectedDataRecorder
	    			+ "' to " + containerUnit + ".");

	    	}
	    	else {
	    		logger.warning(person, 5_000L, "Unable to return '" + selectedDataRecorder 
					+ "' to " + containerUnit + ".");
	    	}
		}
		
		// Check if there's anyone still at the site, if not, close the site
		if (siteVisit != null && siteVisit.isOnlyInvestigatorLeft(worker)) {
			logger.info(worker, 5_000L, "Site Visit completed.");
			siteVisit.setClosed(true);
		}
		
    	super.endEVA(reason);
	}
	
	
	/**
	 * Prepares object for garbage collection.
	 */
	@Override
	public void destroy() {
		worker = null;
		airlock = null;
		locationPos = null;
		containerType = null;
		containerUnit = null;
		preparePhase = null;
		dataCollectionSite = null;
		locationPos = null;
		siteVisit = null;
		selectedDataRecorder = null;

		super.destroy();
	}
}
