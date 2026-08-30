/*
 * Mars Simulation Project
 * GatherDataMeta.java
 * @date 2026-08-27
 * @author Manny Kung
 */

package com.mars_sim.core.data.collection.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.WalkOutside;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.structure.Airlock;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
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

	public static final int MAX_SITE_DISTANCE = 50;
	
	public static final double SMALL_AMOUNT = 0.001;
	/** The loading speed of the resource at the storage bin [kg/millisols]. */
	public static final double LOADING_RATE = 10.0;

	public static final String WALK = "walk";
	
	static final TaskPhase WALK_TO_SITE = new TaskPhase(Msg.getString("Task.phase.walkToOutsideSite")); //$NON-NLS-1$

	static final TaskPhase ASSEMBLE_INSTRUMENT_AT_SITE = new TaskPhase("Assemble and set up instruments at the site");
	
	static final TaskPhase DISASSEMBLE_INSTRUMENT_AT_SITE = new TaskPhase("Disassemble and tear down instruments at the site");

	private double compositeRate;

	private double fatigueFactor;
	/** The amount of resource that can be collected by this person per trip [in kg]. */
	private double collectionLimit;
	
	private String resourceName;
	
	/** Airlock to be used for settlement or vehicular EVA. */
	private Airlock airlock;

	private UnitHolder containerUnit;
	
	private TaskPhase dataCollectionPhase;

	private LocalPosition selectedSite;
	
	private EquipmentType dataRecorderType;

	private Set<Integer> dataInstrumentSet = Set.of(
			ItemResourceUtil.GAS_CHROMATOGRAPH_ID, 
			ItemResourceUtil.IR_SPECTROMETER_ID,
			ItemResourceUtil.MASS_SPECTROMETER_ID, 
			ItemResourceUtil.XRAY_SPECTROMETER_ID, 
			ItemResourceUtil.HEAT_PROBE_ID);
			
	private Map<Integer, Person> operatorMap = new HashMap<>();
	
	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 */
	protected GatherData(String name, TaskPhase dataCollectionPhase,
					EquipmentType containerType, Person person, int duration) {
        // Use EVAOperation constructor.
        super(name, person, duration, dataCollectionPhase);

		setMinimumSunlight(LightLevel.NONE);

        this.dataRecorderType = containerType;
        this.dataCollectionPhase = dataCollectionPhase;

        // To dig local, a person must start at a Settlement
        containerUnit = person.getContainerUnit();
        
        if (containerUnit instanceof Settlement) {
        	// Get the vehicular airlock
            airlock = getWalkableAvailableEgressAirlock(person);
        }
        else if (containerUnit instanceof Vehicle v) {
        	// Get the vehicular airlock
            airlock = ((Rover)v).getAirlock();
        }

//        // If data recorder is not available, end task.
//        if (getDataRecorder() == null) {
//        	endEVA("No " + dataRecorderType.name() + " for " + resourceName + " are available.");
//        	return;
//        }

        if (selectedSite == null) {
        	selectedSite = determineSiteLocation();
	        if (selectedSite == null) {
				endEVA("No good site found.");
	        	return;
	        }
        }

        setPhase(WALK_TO_OUTSIDE_SITE);
    }
	
	/**
	 * Where will any resources be dropped off on the surface
	 */
	LocalPosition getSelectedSite() {
		return selectedSite;
	}

	/**
	 * Carries a data instrument.
	 *
	 * @param holder the current equipment holder
	 * @param person
	 * @param intrumentID
	 * 
	 */
	public void carryDataInstrument(EquipmentOwner holder, Person person, int intrumentID) {
		
		if (!hasInstrument(person, intrumentID) && holder.retrieveItemResource(intrumentID, 1) == 0) {
			person.getEquipmentInventory().storeItemResource(intrumentID, 1);
		}
	}
	
	/**
	 * Does this person have this instrument ?
	 */
	public boolean hasInstrument(Person person, int intrumentID) {
		return person.getEquipmentInventory().getItemResourceStored(intrumentID) > 0;
	}
	
	/**
	 * Determines the data collection rate
	 * 
	 * @param collectionRate
	 */
	protected void determineRates(double collectionRate) {
        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        int acad = nManager.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
        int areo = person.getSkillManager().getSkillLevel(SkillType.AREOLOGY);
        int meti = nManager.getAttribute(NaturalAttributeType.METICULOUSNESS);
        
        int creat = nManager.getAttribute(NaturalAttributeType.CREATIVITY);
        int agility = nManager.getAttribute(NaturalAttributeType.AGILITY);
        
        // Increase the duration of this task based upon one's attribute
        setDuration(getDuration() * (1 + meti/200.0));
        
        fatigueFactor = .5 * (1 - (agility + creat) / 200D);
		compositeRate = collectionRate * ((acad + meti) / 200D) * (areo + .1);
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
			else if (WALK_TO_SITE.equals(getPhase())) {
				time = walkToSite();
			}
	        else if (ASSEMBLE_INSTRUMENT_AT_SITE.equals(getPhase())) {
	            time = assembleInstrument(time);
	        }
	        else if (dataCollectionPhase.equals(getPhase())) {
	            time = collectDataPhase(time);
	        }
	        else if (DISASSEMBLE_INSTRUMENT_AT_SITE.equals(getPhase())) {
//	            time = dissembleInstrument(time);
	        }
		}
        return time;
    }

	/**
	 * Walks to a site by adding a walking sub task. 
	 * 
	 * @return
	 */
    private double walkToSite() {
    	// Go to the drop off location
        if (person.isOutside()) {
        	
    		if (!selectedSite.isNear(person.getPosition())) { 	
        		// FUTURE: how to get the walk time and return the remaining time ?
    			
        		// Note that addSubTask() will internally check if the task is a duplicate

				boolean canAdd = addSubTask(new WalkOutside(person, person.getPosition(), selectedSite, false));
				if (!canAdd) {
					logger.log(person, Level.WARNING, 4_000,
							". Unable to add subtask WalkOutside.");
					// Note: may call below many times
					endEVA("Unable to walk outside.");
				}
        	}
    		
        	else {
        		setPhase(ASSEMBLE_INSTRUMENT_AT_SITE);
        	}
        }
        
        else {
        	logger.severe(person, "Not outside. Unable to walk to the storage bin.");
        	endEVA("Not Outside");
        }

        return 0;
    }

    /**
     * Drops off and assemble instruments at the site.
     * 
     * @param time
     * @return
     */
	private double assembleInstrument(double time) {
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
        return 0;
    }
    
	
	/**
	 * Finds a map of buildings having storage functions that can or cannot 
	 * hold resources being collected.
	 * 
	 * @param worker
	 * @param resourceID
	 * @return
	 */
	private static Map<Boolean, List<Building>> findStorageBuildings(Worker worker, int resourceID) {
		// Find any Storage function that can hold the resource being collected but
		// group by Buildings that are categorised as Storage
		return worker.getSettlement().getBuildingManager()
			.getBuildingSet(FunctionType.STORAGE).stream()
			.filter(b -> b.getStorage().getResourceStorageCapacity().containsKey(resourceID))
			.collect(Collectors.groupingBy(x -> (x.getCategory() == BuildingCategory.STORAGE)));
	}
	
    
    /**
     * Determines location for the site.
     * 
     * @return digging X and Y location outside settlement.
     */
    private LocalPosition determineSiteLocation() {
		if (airlock.getEntity() instanceof LocalBoundedObject boundedObject) {
			return LocalAreaUtil.getCollisionFreeRandomPosition(boundedObject,
																 person.getCoordinates(), MAX_SITE_DISTANCE);
		}

        return null;
    }

    /**
     * Closes out this task. If person is inside then transfer the resource from the container to the Settlement.
     */
    @Override
    protected void clearDown() {
		if (containerUnit == null) {
			return;
		}
	
		// Assign thermal bottle
		person.assignThermalBottle();

		super.clearDown();
    }
    
	/**
	 * Is the person qualified for gathering data ?
	 * 
	 * @return
	 */
	public static boolean canGatherData(Person person) {

		// Note: check egress airlock is already covered by another method 

		// Check if sunlight is insufficient
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
		selectedSite = null;
		dataRecorderType = null;

		super.destroy();
	}
}
