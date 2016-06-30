/**
 * Mars Simulation Project
 * Crop.java
 * @version 3.08 2015-04-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function.farming;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupportType;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.Research;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;


/**
 * The Crop class is a food crop grown on a farm.
 */
public class Crop
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(Crop.class.getName());

	// TODO Static members of crops should be initialized from some xml instead of being hard coded.
		
	public static final double TISSUE_EXTRACTED_PERCENT = .1D;
	
	public static final String CROP_WASTE = "crop waste";
	public static final String TISSUE_CULTURE = "tissue culture"; 
	public static final String FERTILIZER = "fertilizer";
	public static final String SOIL = "soil";
	
	/** Amount of carbon dioxide needed per harvest mass. */
	public static final double CARBON_DIOXIDE_NEEDED = 2D;

	public static final double NEW_SOIL_NEEDED_PER_SQM = .2D;

	//  Be sure that FERTILIZER_NEEDED is static, but NOT "static final"
	public static final double FERTILIZER_NEEDED_WATERING = 0.0001D;  // a very minute amount needed per unit time, called if grey water is not available

	public static final double FERTILIZER_NEEDED_IN_SOIL_PER_SQM = 1D; // amount needed when planting a new crop

	public static final double MOISTURE_RECLAMATION_FRACTION = .1D;
	public static final double OXYGEN_GENERATION_RATE = .9D;
	public static final double CO2_GENERATION_RATE = .9D;

	// public static final double SOLAR_IRRADIANCE_TO_PAR_RATIO = .42; // only 42% are EM within 400 to 700 nm
	// see http://ccar.colorado.edu/asen5050/projects/projects_2001/benoit/solar_irradiance_on_mars.htm#_top

	public static final double WATT_TO_PHOTON_CONVERSION_RATIO = 4.609; // in u mol / m2 /s / W m-2 for Mars only

	public static final double kW_PER_HPS = .4;
	public static final double VISIBLE_RADIATION_HPS = .4; // high pressure sodium (HPS) lamps efficiency
	public static final double BALLAST_LOSS_HPS = .1; // for high pressure sodium (HPS)
	public static final double NON_VISIBLE_RADIATION_HPS = .37; // for high pressure sodium (HPS)
	public static final double CONDUCTION_CONVECTION_HPS = .13; // for high pressure sodium (HPS)
	public static final double LOSS_AS_HEAT_HPS = NON_VISIBLE_RADIATION_HPS*.75 + CONDUCTION_CONVECTION_HPS/2D;
	//public static final double MEAN_DAILY_PAR = 237.2217D ; // in [mol/m2/day]
	// SurfaceFeatures.MEAN_SOLAR_IRRADIANCE * 4.56 * (not 88775.244)/1e6 = 237.2217

	// TODO Crop phases should be an internationalizable enum.
	
	//public static final double INCUBATION_PERIOD  = 2000D;
	// 2016-06-28 Added incubation 
	//public static final String INCUBATION = "Incubation";
	//public static final String PLANTING = "Planting";
	//public static final String GERMINATION = "Germination"; // include initial sprouting of a seedling
	//public static final String GROWING = "Growing";
	//public static final String HARVESTING = "Harvesting";
	//public static final String FINISHED = "Finished";

    private static final double T_TOLERANCE = 3D;

    //private static final double PERCENT_IN_GERMINATION_PHASE = 5; // Assuming the first 5% of a crop's life is in germination phase
    //private static final double PERCENT_IN_INCUBATION_PHASE = 2;

	// Data members
    /** Current sol since the start of sim. */
	private int solCache = 1;
	/** Current sol of month. */
	private int currentSol = 1;
	/** Maximum possible food harvest for crop. (kg) */
	private double maxHarvest;
	/** 2016-06-28 Added Required work time for incubation (millisols). */
	//private double incubationWorkRequired;
	/** Required work time for planting (millisols). */
	//private double plantingWorkRequired;
	/** Required work time to tend crop daily (millisols). */
	//private double dailyTendingWorkRequired;
	/** Required work time for harvesting (millisols). */
	//private double harvestingWorkRequired;
	/** Completed work time in current phase (millisols). */
	private double currentPhaseWorkCompleted;
	/** Actual food harvest for crop. (kg) */
	private double actualHarvest;
	/** max possible daily harvest for crop. (kg) */
	private double maxHarvest_kg_daily;
	/** Growing phase time completed thus far (millisols). */
	private double growingTimeCompleted; // this parameter is initially randomly generated at the beginning of a sim for a growing crop
	/** the area the crop occupies in square meters. */
	private double growingArea;
	/** Growing day in number of millisols. */
	private double cropGrowingTime;
	/** Growing day in number of sols. */
	//private double cropGrowingDay;
	private double fractionalGrowthCompleted;
	private double t_initial;
	private double dailyPARRequired;
	private double dailyPARCache = 0;
	private double sunlightModifierCache = 1;
	private double lightingPower = 0; // in kW
	private double healthCondition = 0;
    private double averageWaterNeeded;
    private double averageOxygenNeeded;
    private double averageCarbonDioxideNeeded;


	/** Current phase of crop. */
	//private String phase;
	private PhaseType phaseType;
	private String cropName;
	/** The type of crop. */
	private CropType cropType;
	private Inventory inv;
	/** Farm crop being grown in. */
	private Farming farm;
	/** The settlement the crop is located at. */
	private Settlement settlement;
	private SurfaceFeatures surface;
	private MarsClock marsClock;
	private MasterClock masterClock;
	private CropConfig cropConfig = SimulationConfig.instance().getCropConfiguration();
	   
	private Map<Integer, Phase> phases = new HashMap<>();
	
	DecimalFormat fmt = new DecimalFormat("0.000");

	/**
	 * Constructor.
	 * @param cropType the type of crop.
	 * @param maxHarvest - Maximum possible food harvest for crop. (kg)
	 * @param farm - Farm crop being grown in.
	 * @param settlement - the settlement the crop is located at.
	 * @param newCrop - true if this crop starts in it's planting phase.
	 * @param tissuePercent
	 */
	// Called by Farming.java constructor and timePassing()
	// 2015-08-26 Added new param percentGrowth
	public Crop(CropType cropType, double growingArea, double maxHarvestinKgPerDay, Farming farm,
			Settlement settlement, boolean newCrop, double tissuePercent) {
		this.cropType = cropType;

		this.farm = farm;
		this.settlement = settlement;
		this.growingArea = growingArea;
		this.maxHarvest_kg_daily = maxHarvestinKgPerDay;

	    averageWaterNeeded = cropConfig.getWaterConsumptionRate();
	    averageOxygenNeeded = cropConfig.getOxygenConsumptionRate();
	    averageCarbonDioxideNeeded = cropConfig.getCarbonDioxideConsumptionRate();

		surface = Simulation.instance().getMars().getSurfaceFeatures();
        masterClock = Simulation.instance().getMasterClock();
		marsClock = masterClock.getMarsClock();

		inv = settlement.getInventory();
		t_initial = farm.getBuilding().getInitialTemperature();

		// 2015-04-08  Added dailyPARRequired
		dailyPARRequired = cropType.getDailyPAR();

		cropName = cropType.getName();
		// cropGrowingTime in millisols
		cropGrowingTime = cropType.getGrowingTime();
		//System.out.println(cropType.getName() + " cropGrowingTime : " + cropGrowingTime);
		// cropGrowingDay in sols
		double cropGrowingDay = cropGrowingTime/1000D;
		//System.out.println(cropType.getName() + " cropGrowingDay : " + cropGrowingDay);
		maxHarvest = maxHarvest_kg_daily * cropGrowingDay;

		phases = cropType.getPhases();
		
		for (Phase p : phases.values()) {
			p.setHarvestFactor(maxHarvest);
		}
/*		
		int size = phases.size();
		for (Map.Entry<Integer, Phase> entry : phases.entrySet()) {
		    int key = entry.getKey();	    
		    if (key < size) {
		    	Phase value = entry.getValue();
		    	value.setHarvestFactor(maxHarvest);
		    }
		}
*/		
		//String cropCategory = cropType.getCropCategory();

				
		// Determine work required.
		// TODO: redetermine how much plantingWorkRequired should be
		//plantingWorkRequired = maxHarvest * 1.5D;
		//dailyTendingWorkRequired = maxHarvest;
		//harvestingWorkRequired = maxHarvest * 3D; // old default is 5. why?
		// 2016-06-29 Added incubationWorkRequired
		//incubationWorkRequired = INCUBATION_PERIOD;
		
		if (newCrop) {
			phaseType = PhaseType.INCUBATION; 
			if (tissuePercent <= 0) {
				// assume a max 2-day incubation period if no 0% tissue culture is available
				growingTimeCompleted = phases.get(0).getWorkRequired();
				logger.info(cropType + " is not available and needs " + growingTimeCompleted + " millisols to incubate enough to restock before planting.");

			}
			else if (tissuePercent >= 100) {
				// assume zero day incubation period if 100% tissue culture is available
				growingTimeCompleted = 0;
				logger.info(cropType + " is fully available. Proceed to planting.");

			}
			else {
				growingTimeCompleted = 2D * (100D - tissuePercent);
				//growingTimeCompleted = tissuePercent /100D * PERCENT_IN_INCUBATION_PHASE /100D * cropGrowingTime;
				logger.info(cropType + " needs " + growingTimeCompleted + " millisols to incubate enough to restock before planting.");

			}
	
		}
		else { 

			// At the start of the sim, set up a crop's "initial" percentage of growth randomly 
			growingTimeCompleted = RandomUtil.getRandomDouble(cropGrowingTime);
			
			fractionalGrowthCompleted = growingTimeCompleted/cropGrowingTime;
	
			if ( fractionalGrowthCompleted * 100D <= phases.get(2).getPercentGrowth()) {//PERCENT_IN_GERMINATION_PHASE) {	// assuming the first 10% growing day of each crop is germination
				phaseType = PhaseType.GERMINATION;
			}
			else if ( fractionalGrowthCompleted * 100D > phases.get(2).getPercentGrowth()) {
				phaseType = PhaseType.GROWING;
			}
			//else
			//	phase = PLANTING; 		
		
			actualHarvest = maxHarvest * fractionalGrowthCompleted;
			//System.out.println(cropType.getName() + " growingTimeCompleted : " + growingTimeCompleted);
			//System.out.println(cropType.getName() + " maxHarvest : " + maxHarvest);
			//System.out.println(cropType.getName() + " fractionalGrowthCompleted : " + fractionalGrowthCompleted);
			//System.out.println(cropType.getName() + " actualHarvest : " + actualHarvest);

		}

	}

	public double getLightingPower() {
		return 	lightingPower;
	}

	public double getGrowingArea() {
		return growingArea;
	}

	/**
	 * Gets the type of crop.
	 *
	 * @return crop type
	 */
	public CropType getCropType() {
		return cropType;
	}

	/**
	 * Gets the phase of the crop.
	 * @return phase
	 */
	// Called by BuildingPanelFarming.java to retrieve the phase of the crop
	//public String getPhase() {
	//	return phase;
	//}

	/**
	 * Gets the phase type of the crop.
	 * @return phaseType
	 */
	// 2016-06-29 Called by Farming and BuildingPanelFarming to retrieve the phase of the crop
	public PhaseType getPhaseType() {
		return phaseType;
	}


	/**
	 * Gets the crop category
	 * @return category
	 */
	// 2014-10-10 Added this method for UI to show crop category
	// Called by BuildingPanelFarming.java to retrieve the crop category
	public String getCategory() {
		return cropType.getCropCategory();
	}

	/**
	 * Gets the maximum possible food harvest for crop.
	 * @return food harvest (kg.)
	 */
	public double getMaxHarvest() { return maxHarvest; }

	/**
	 * Gets the amount of growing time completed.
	 * @return growing time (millisols)
	 */
	public double getGrowingTimeCompleted() {
		return growingTimeCompleted;
	}

	/**
	 * Sets the amount of growing time completed.
	 * @param percent
	 */
	//public void setGrowingTimeCompleted(double percent) {
	//	this.growingTimeCompleted = percent * cropGrowingTime / 100D ;
	//}


	/**
	 * Checks if crop needs additional work on current sol.
	 * @return true if more work needed.
	 */
	public boolean requiresWork() {
		boolean result = false;
		if (phaseType == PhaseType.INCUBATION || phaseType == PhaseType.PLANTING || phaseType == PhaseType.HARVESTING) 
			result = true;
		else if (phaseType == PhaseType.GROWING) {
			if (phases.get(2).getWorkRequired() > currentPhaseWorkCompleted) 
				result = true;
			else
				result = false;
		}
		else if (phaseType == PhaseType.GERMINATION ) {
			if (phases.get(3).getWorkRequired() > currentPhaseWorkCompleted) 
				result = true;
			else
				result = false;
		}

		return result;
	}

	/**
	 * Gets the overall health condition of the crop.
	 *
	 * @return condition as value from 0 (poor) to 1 (healthy)
	 */
	// Called by BuildingPanelFarming.java to retrieve the health condition status
	// 2015-08-26 Revised getCondition()
	public double getCondition() {
		// O:bad, 1:good
		double result = 0D;

		//System.out.println(cropType.getName() + " fractionalGrowthCompleted : " + fractionalGrowthCompleted);
		//System.out.println(cropType.getName() + " actualHarvest : " + actualHarvest);

		if (phaseType == PhaseType.GROWING || phaseType == PhaseType.HARVESTING) {
			result = actualHarvest / maxHarvest * cropGrowingTime / growingTimeCompleted ;
			//System.out.println("condition is "+ result + " for " + cropName);
		}

		else if (phaseType == PhaseType.GERMINATION) {
			if ( (growingTimeCompleted / cropGrowingTime) <= .02 ) {
				// avoid initial spurious data
				result = 1; //actualHarvest / maxHarvest * cropGrowingTime / growingTimeCompleted ;
				//System.out.println("condition is "+ result + " for " + cropName);
			}
			else {
				result = actualHarvest / maxHarvest * cropGrowingTime / growingTimeCompleted ;
				//System.out.println("condition is "+ result + " for " + cropName);
			}
		}
		
		else if (phaseType == PhaseType.INCUBATION) {
			result = 1D;
		}
		
		else if (phaseType == PhaseType.PLANTING) {
			result = 1D;
		}
		
		if (result > 1D) result = 1D;
		else if (result < 0D) result = 0D;

		healthCondition = result;

		return result;
	}

	/**
	 * Adds work time to the crops current phase.
	 * @param workTime - Work time to be added (millisols)
	 * @return workTime remaining after working on crop (millisols)
	 * @throws Exception if error adding work.
	 */
	// Called by Farming.java's addWork()
	public double addWork(double workTime) {
		double remainingWorkTime = workTime;

		if (actualHarvest <= 0D) {
			actualHarvest = 0;
			growingTimeCompleted = 0;
		}

		if (phaseType == PhaseType.INCUBATION) {
			currentPhaseWorkCompleted += remainingWorkTime;		
			
			if (currentPhaseWorkCompleted >= phases.get(0).getWorkRequired()) {
				remainingWorkTime = currentPhaseWorkCompleted - phases.get(0).getWorkRequired();
				currentPhaseWorkCompleted = 0D;
				phaseType = PhaseType.PLANTING;
				
			} else {
				remainingWorkTime = 0D;
			}
			
		}
		
		
		
		if (phaseType == PhaseType.PLANTING) {
			currentPhaseWorkCompleted += remainingWorkTime;
			
			if (currentPhaseWorkCompleted >= phases.get(1).getWorkRequired()) {
				remainingWorkTime = currentPhaseWorkCompleted - phases.get(1).getWorkRequired();
				currentPhaseWorkCompleted = 0D;

				// 2015-08-26 Added checking the following two conditions
				fractionalGrowthCompleted = growingTimeCompleted/cropGrowingTime;
				if ( fractionalGrowthCompleted * 100D <= phases.get(2).getPercentGrowth()) {	// assuming the first 10% growing day of each crop is germination
					phaseType = PhaseType.GERMINATION;
				}
				else if ( fractionalGrowthCompleted * 100D > phases.get(2).getPercentGrowth()) {
					phaseType = PhaseType.GROWING;
				}

			}
			else {
				remainingWorkTime = 0D;
			}
		}

		// 2015-02-15 Added GERMINATION
		if (phaseType == PhaseType.GERMINATION ) {
			currentPhaseWorkCompleted += remainingWorkTime;
	        //System.out.println("addWork() : currentPhaseWorkCompleted is " + currentPhaseWorkCompleted);
			double w = phases.get(2).getWorkRequired();
			if (currentPhaseWorkCompleted >= w) {
				remainingWorkTime = currentPhaseWorkCompleted - w;
				currentPhaseWorkCompleted = w;
			}
			else {
				remainingWorkTime = 0D;
			}
		}

		else if (phaseType == PhaseType.GROWING) {
			currentPhaseWorkCompleted += remainingWorkTime;
	        //System.out.println("addWork() : currentPhaseWorkCompleted is " + currentPhaseWorkCompleted);
			double w = phases.get(3).getWorkRequired();
			if (currentPhaseWorkCompleted >= w) {
				remainingWorkTime = currentPhaseWorkCompleted - w;
				currentPhaseWorkCompleted = w;
			}
			else {
				remainingWorkTime = 0D;
			}
		}
		
		else if (phaseType == PhaseType.HARVESTING) {
			//logger.info("addWork() : crop is in Harvesting phase");
			currentPhaseWorkCompleted += remainingWorkTime;
			double w = phases.get(4).getWorkRequired();
			if (currentPhaseWorkCompleted >= w) {
				// Harvest is over. Close out this phase
				//logger.info("addWork() : done harvesting. remainingWorkTime is " + Math.round(remainingWorkTime));
				double overWorkTime = currentPhaseWorkCompleted - w;
				// 2014-10-07 modified parameter list to include crop name
				double lastHarvest = actualHarvest * (remainingWorkTime - overWorkTime) / w;
				// Store the crop harvest
				Storage.storeAnResource(lastHarvest, cropName, inv);
				//logger.info("addWork() : harvesting " + cropName + " : " + Math.round(lastHarvest * 1000.0)/1000.0 + " kg. All Done.");
				remainingWorkTime = overWorkTime;
				
				phaseType = PhaseType.FINISHED;
				generateCropWaste(lastHarvest);

				// 2015-10-13 Check to see if a botany lab is available
				checkBotanyLab();
				//actualHarvest = 0;
				//growingTimeCompleted = 0;
			}
			else { 	// continue the harvesting process
				// 2014-10-07 modified parameter list to include crop name
				double modifiedHarvest = actualHarvest * workTime / phases.get(4).getWorkRequired();
				//logger.info("addWork() : " + cropName + " modifiedHarvest is " + Math.round(modifiedHarvest * 1000.0)/1000.0);
				// Store the crop harvest
				Storage.storeAnResource(modifiedHarvest, cropName, inv);
				//logger.info("addWork() : harvesting " + cropName + " : " + Math.round(modifiedHarvest * 1000.0)/1000.0 + " kg.");
				remainingWorkTime = 0D;
				generateCropWaste(modifiedHarvest);
			}
		}

		return remainingWorkTime;
	}

	/*
	 * Checks to see if a botany lab with an open research slot is available and performs cell tissue extraction
	 */
	public void checkBotanyLab() {
		// 2015-10-13 Check to see if a botany lab is available
		boolean hasEmptySpace = false;
		//boolean full = false;
		Building bldg = farm.getBuilding();
		Research lab0 = (Research) bldg.getFunction(BuildingFunction.RESEARCH);
		// Check to see if the local greenhouse has a research slot
		if (lab0.hasSpecialty(ScienceType.BOTANY)) {
			hasEmptySpace = lab0.checkAvailability();
		}

		if (hasEmptySpace) {
			lab0.addResearcher();
			preserveCropTissue(lab0, true);
			lab0.removeResearcher();
		}
		else {
			// Check available research slot in another lab located in another greenhouse
			List<Building> laboratoryBuildings = settlement.getBuildingManager().getBuildings(BuildingFunction.RESEARCH);
			Iterator<Building> i = laboratoryBuildings.iterator();
			while (i.hasNext() && !hasEmptySpace) {
				Building building = i.next();
				Research lab = (Research) building.getFunction(BuildingFunction.RESEARCH);
				if (lab.hasSpecialty(ScienceType.BOTANY)) {
					hasEmptySpace = lab.checkAvailability();
					if (hasEmptySpace) {
						lab.addResearcher();
						preserveCropTissue(lab, true);
						lab.removeResearcher();
						// TODO: compute research ooints to determine if it can be carried out.
						// int points += (double) (lab.getResearcherNum() * lab.getTechnologyLevel()) / 2D;
					}
				}
			}
		}

		// check to see if a person can still "squeeze into" this busy lab to get lab time
		if (!hasEmptySpace && (lab0.getLaboratorySize() == lab0.getResearcherNum())) {
			preserveCropTissue(lab0, false);
		}
		else {

			// Check available research slot in another lab located in another greenhouse
			List<Building> laboratoryBuildings = settlement.getBuildingManager().getBuildings(BuildingFunction.RESEARCH);
			Iterator<Building> i = laboratoryBuildings.iterator();
			while (i.hasNext() && !hasEmptySpace) {
				Building building = i.next();
				Research lab = (Research) building.getFunction(BuildingFunction.RESEARCH);
				if (lab.hasSpecialty(ScienceType.BOTANY)) {
					hasEmptySpace = lab.checkAvailability();
					if (lab.getLaboratorySize() == lab.getResearcherNum()) {
						preserveCropTissue(lab, false);
					}

				}
			}
		}
	}

    /**
     * Compute the amount of crop tissues extracted
     */
	//2015-10-13 Changed to preserveCropTissue();
	public void preserveCropTissue(Research lab, boolean hasSpace) {
		// Added the contributing factor based on the health condition
		// TODO: re-tune the amount of tissue culture based on not just based on the edible biomass (actualHarvest)
		// but also the inedible biomass and the crop category
		double amount = healthCondition * actualHarvest * TISSUE_EXTRACTED_PERCENT;

		// Added randomness
		double rand = RandomUtil.getRandomDouble(1);
		amount = Math.round((amount * .5 + amount * rand)*10000.0)/10000.0;

		String tissue = cropName + " " + TISSUE_CULTURE;
		Storage.storeAnResource(amount, tissue, inv);

		// 2015-10-13 if no dedicated research space is available, work can still be performed but productivity is cut half
		if (hasSpace) {
			logger.info(amount + " kg " + tissue + " extracted & cryo-preserved in " 
					+ lab.getBuilding().getNickName() + " at " + settlement.getName());
		}
		else {
			amount = amount / 2D;
			logger.info("Not enough botany research space. Only " + amount + " kg (at reduced capacity) " + tissue 
					+ " extracted & cryo-preserved in " 
					+ lab.getBuilding().getNickName() + " at " + settlement.getName());
		}


	}

    /**
     * Compute the amount of crop waste generated
     */
	public void generateCropWaste(double harvestMass) {
		// 2015-02-06 Added Crop Waste
		double amountCropWaste = harvestMass * cropType.getInedibleBiomass() / (cropType.getInedibleBiomass() +cropType.getEdibleBiomass());
		Storage.storeAnResource(amountCropWaste, CROP_WASTE, inv);
		//logger.info("addWork() : " + cropName + " amountCropWaste " + Math.round(amountCropWaste * 1000.0)/1000.0);
	}


    /**
     * Stores an resource
     * @param amount
     * @param name

	// 2015-02-06 Added storeAnResource()
    public void storeAnResource(double amount, String name) {
    	try {
            AmountResource ar = AmountResource.findAmountResource(name);
            double remainingCapacity = inv.getAmountResourceRemainingCapacity(ar, false, false);

            if (remainingCapacity < amount) {
                // if the remaining capacity is smaller than the harvested amount, set remaining capacity to full
            	amount = remainingCapacity;
                //logger.info(" storage is full!");
            }
            // TODO: consider the case when it is full
            inv.storeAmountResource(ar, amount, true);
            inv.addAmountSupplyAmount(ar, amount);
        }  catch (Exception e) {
    		logger.log(Level.SEVERE,e.getMessage());
        }
    }
     */

	/**
	 * Time passing for crop.
	 * @param time - amount of time passing (millisols)
	 */
	public void timePassing(double time) {

		if (phaseType == PhaseType.GROWING|| phaseType == PhaseType.GERMINATION) {
			//System.out.println("timePassing() : GROWING or GERMINATION");
			//System.out.println("timePassing() : time is " + time);
			if (time > 0D) {
				growingTimeCompleted += time;
				//System.out.println("timePassing() : growingTimeCompleted : " + growingTimeCompleted );
				//System.out.println("timePassing() : growingTimeCompleted / cropGrowingTime : " + growingTimeCompleted / cropGrowingTime);
				
				if (growingTimeCompleted  <= cropGrowingTime * phases.get(2).getPercentGrowth() / 100D) {
					phaseType = PhaseType.GERMINATION;
					currentPhaseWorkCompleted = 0D;
				}

				else if (growingTimeCompleted < cropGrowingTime) {
					phaseType = PhaseType.GROWING;
					currentPhaseWorkCompleted = 0D;
				}

				if (growingTimeCompleted >= cropGrowingTime) {
					phaseType = PhaseType.HARVESTING;
					currentPhaseWorkCompleted = 0D;
					
				} else if (growingTimeCompleted == 0) {
					
					;//
					
				} else { // still in phase.equals(GROWING)|| phase.equals(GERMINATION)
					//System.out.println("timePassing() : at else{}");

/*					// getSolOfMonth() is unreliable for some reason. use MarsClock.getSolOfYear(clock) instead
					if (marsClock == null)
						marsClock = Simulation.instance().getMasterClock().getMarsClock();
					int newSol = marsClock.getSolOfMonth();
*/
					// Modify actual harvest amount based on daily tending work.
					if (masterClock == null)
						masterClock = Simulation.instance().getMasterClock();
					// get the current time
					MarsClock clock = masterClock.getMarsClock();
					// check for the passing of each day
					int newSol = MarsClock.getSolOfYear(clock);
					if (newSol != currentSol) {
						// TODO: why doing this at the end of a sol?
						//double maxDailyHarvest = maxHarvest / cropGrowingDay;
						double dailyWorkCompleted = currentPhaseWorkCompleted / phases.get(3).getWorkRequired();
						// TODO: is it better off doing the actualHarvest computation once a day or every time
						actualHarvest += (maxHarvest_kg_daily * (dailyWorkCompleted - .5D));
						currentSol = newSol;
						//System.out.println(" a new sol");
						// reset the daily work counter currentPhaseWorkCompleted back to zero
						currentPhaseWorkCompleted = 0D;
					}

					// max possible harvest within this period of time
					double maxPeriodHarvest = maxHarvest * (time / cropGrowingTime);
					// Compute each harvestModifiers and sum them up below
					double harvestModifier = calculateHarvestModifier(maxPeriodHarvest, time);
					//System.out.println("Crop.java : just done calling calculateHarvestModifier()");

					// Modify harvest amount.
					actualHarvest += maxPeriodHarvest * harvestModifier * 10D; // assuming the standard area of 10 sq m

					if (actualHarvest < 0)
						actualHarvest = 0;

					//System.out.println("timePassing() : maxPeriodHarvest is " + maxPeriodHarvest);
					//System.out.println("timePassing() : harvestModifier is " + harvestModifier);
					//System.out.println("timePassing() : actualHarvest is " + actualHarvest);
					//System.out.println("timePassing() : maxHarvest is " + maxHarvest);

					// Compute health condition
					getCondition();
					fractionalGrowthCompleted = growingTimeCompleted/cropGrowingTime;
					// Check on the health of a >25% grown crop
					if ( (fractionalGrowthCompleted > .25D) && (healthCondition < .1D) ) {
						phaseType = PhaseType.FINISHED;
						logger.info("Crop " + cropName + " at " + settlement.getName() + " died of poor health.");
						// 2015-02-06 Added Crop Waste
						double amountCropWaste = actualHarvest * cropType.getInedibleBiomass() / ( cropType.getInedibleBiomass() + cropType.getEdibleBiomass());
						Storage.storeAnResource(amountCropWaste, CROP_WASTE, inv);
						logger.info(amountCropWaste + " kg Crop Waste generated from the dead "+ cropName);
						//actualHarvest = 0;
						//growingTimeCompleted = 0;
					}

					// Seedling (<10% grown crop) is less resilient and more prone to environmental factors
					if ( (fractionalGrowthCompleted > 0) && (fractionalGrowthCompleted < .1D) && (healthCondition < .15D) ) {
						phaseType = PhaseType.FINISHED;
						logger.info("The seedlings of " + cropName + " at " + settlement.getName() + " did not survive.");
						// 2015-02-06 Added Crop Waste
						double amountCropWaste = actualHarvest * cropType.getInedibleBiomass() / ( cropType.getInedibleBiomass() + cropType.getEdibleBiomass());
						Storage.storeAnResource(amountCropWaste, CROP_WASTE, inv);
						logger.info(amountCropWaste + " kg Crop Waste generated from the dead "+ cropName);
						//actualHarvest = 0;
						//growingTimeCompleted = 0;
					}
				}
			}
		}
		
		else if (phaseType == PhaseType.INCUBATION) {
			growingTimeCompleted += time;
			if (growingTimeCompleted >= phases.get(0).getWorkRequired()) {			
				phaseType = PhaseType.PLANTING;
				currentPhaseWorkCompleted = 0D;
			}
		}
		
		else if (phaseType == PhaseType.FINISHED) {
			actualHarvest = 0;
			growingTimeCompleted = 0;
			//System.out.println("timePassing() : FINISHED");
		}

	}

	public void turnOnLighting(double kW) {
		lightingPower = kW;
	}

	public void turnOffLighting() {
		lightingPower = 0;
		//return lightingPower;
	}

	/*
	 * Computes each input and output constituent for a crop for the specified period of time and return the overall harvest modifier
	 * @param the maximum possible growth/harvest
	 * @param a period of time in millisols
	 * @return the havest modifier
	 */
	// 2015-02-16 Added calculateHarvestModifier()
	public double calculateHarvestModifier(double maxPeriodHarvest, double time) {
		// TODO: use theoretical model for crop growth, instead of empirical model below.
		// TODO: the calculation should be uniquely tuned to each crop

		double harvestModifier = 1D;
		//timeCache = timeCache + time;

		// TODO: Modify harvest modifier according to the moisture level
		// TODO: Modify harvest modifier according to the pollination by the number of bees in the greenhouse

		// Determine harvest modifier according to amount of light.
		// TODO: Modify harvest modifier by amount of artificial light available to the whole greenhouse
		double sunlightModifier = 0;
		if (surface == null)
			surface = Simulation.instance().getMars().getSurfaceFeatures();

		if (masterClock == null)
			masterClock = Simulation.instance().getMasterClock();
		// get the current time

		if (marsClock == null)
			marsClock = masterClock.getMarsClock();

	    int currentMillisols = (int) marsClock.getMillisol();


		// 2015-04-09 Add instantaneous PAR from solar irradiance
		//double uPAR = SOLAR_IRRADIANCE_TO_PAR_RATIO * surface.getSolarIrradiance(settlement.getCoordinates());
		double uPAR = WATT_TO_PHOTON_CONVERSION_RATIO * surface.getSolarIrradiance(settlement.getCoordinates());

		double instantaneousPAR	= 0;
		if (uPAR > 10)
			instantaneousPAR = uPAR * time * MarsClock.SECONDS_IN_MILLISOL / 1_000_000D; // in mol / m2 within this period of time

	    // Gauge if there is enough sunlight
	    double progress = dailyPARCache / dailyPARRequired;
	    double ruler = currentMillisols / 1000D;
		//System.out.println("uPAR : "+ fmt.format(uPAR) + "\tinstantaneousPAR : " + fmt.format(instantaneousPAR)
		//		+ "\tprogress : "+ fmt.format(progress) + "\truler : " + fmt.format(ruler));

	    // When enough PAR have been administered to the crop, the HPS will turn off.
	    // TODO: what if the time zone of a settlement causes sunlight to shine at near the tail end of the currentMillisols time ?

	    // 2015-04-09 Compare dailyPARCache / dailyPARRequired  vs. current time / 1000D
	    if (progress < ruler) {
	    	// TODO: also compare also how much more sunlight will still be available
	    	// if sunlight is available
	    	if (uPAR > 10) {
	    		dailyPARCache = dailyPARCache + instantaneousPAR ;
	 		    //System.out.print("\tdailyPARCache : " + fmt.format(dailyPARCache));
	    	}
	    	else {
		    	//if no sunlight, turn on artificial lighting
	    		double d_PAR = dailyPARRequired - dailyPARCache; // in mol / m2 / d

	    		double d_PAR_area = d_PAR / (1000 - currentMillisols) / MarsClock.SECONDS_IN_MILLISOL * growingArea; // in mol / msol

		    	double d_kW_area = d_PAR_area * 1000 *  WATT_TO_PHOTON_CONVERSION_RATIO ;

		    	//double d_Joules_now = d_kWatt * time * MarsClock.SECONDS_IN_MILLISOL;

		    	// TODO: Typically, 5 lamps per square meter for a level of ~1000 mol/ m^2 /s

		    	int numLamp = (int) (d_kW_area / kW_PER_HPS / VISIBLE_RADIATION_HPS);
		    	// each HPS lamp supplies 400W with 40% visible radiation efficiency
		    	double supplykW = numLamp * kW_PER_HPS;

		    	// TODO: should also allow the use of LED for lighting

		    	//System.out.println("time : "+ time);
		    	double supplyIntantaneousPAR = supplykW * time * MarsClock.SECONDS_IN_MILLISOL /1000 / WATT_TO_PHOTON_CONVERSION_RATIO / growingArea ; // in mol / m2

		    	turnOnLighting(supplykW);

			    dailyPARCache = dailyPARCache + supplyIntantaneousPAR;
			    //System.out.println("\td_kW_area : "+ fmt.format(d_kW_area) + "\tsupplykW : "+ fmt.format(supplykW)
			    //+ "\tsPAR : "+ fmt.format(supplyIntantaneousPAR) + "\tdailyPARCache : " + fmt.format(dailyPARCache));
	    	}

	    }
	    else {
	    	turnOffLighting();
			dailyPARCache = dailyPARCache + instantaneousPAR;
			//System.out.println("\tdailyPARCache : " + fmt.format(dailyPARCache));

	    }
	        // check for the passing of each day
	    int newSol = MarsClock.getSolOfYear(marsClock);
		if (newSol != solCache) {
			//logger.info("Crop.java : calculateHarvestModifier() : instantaneousPAR is "+instantaneousPAR);
			// the crop has memory of the past lighting condition
			sunlightModifier = 0.5 * sunlightModifierCache  + 0.5 * dailyPARCache / dailyPARRequired;
			if (sunlightModifier > 1.5)
				sunlightModifier = 1.5;
			// TODO: If too much light, the crop's health may suffer unless a person comes to intervene
			solCache = newSol;
			//logger.info("dailyPARRequired is " + dailyPARRequired);
			//logger.info("dailyPARCache is " + dailyPARCache);
			//logger.info("sunlightModifier is " + sunlightModifier);
			dailyPARCache = 0;
			//logger.info("timeCache is "+ timeCache);
			//timeCache = 0;
		}
		else {
			//System.out.println(" currentSol : newSol   " + currentSol + " : " + newSol);
			sunlightModifier = sunlightModifierCache;
		}

		double T_NOW = farm.getBuilding().getCurrentTemperature();
		double temperatureModifier = 0 ;
		if (T_NOW > (t_initial + T_TOLERANCE))
			temperatureModifier = t_initial / T_NOW;
		else if (T_NOW < (t_initial - T_TOLERANCE))
			temperatureModifier = T_NOW / t_initial;
		else // if (T_NOW < (t + T_TOLERANCE ) && T_NOW > (t - T_TOLERANCE )) {
			// TODO: implement optimal growing temperature for each particular crop
			temperatureModifier = 1.1;

		//System.out.println("Farming.java: temperatureModifier is " + temperatureModifier);

		// Determine harvest modifier according to amount of grey water available.

		double needFactor = 0;
		// amount of wastewater/water needed is also based on % of growth
		if (phaseType == PhaseType.GERMINATION)
			needFactor = .1;
		else if (fractionalGrowthCompleted < .1 )
			needFactor = .2;
		else if (fractionalGrowthCompleted < .2 )
			needFactor = .25;
		else if (fractionalGrowthCompleted < .3 )
			needFactor = .3;
		else if (phaseType == PhaseType.GROWING)
			needFactor = fractionalGrowthCompleted;

		// Calculate water usage
		double waterRequired = needFactor * maxPeriodHarvest * growingArea * time / 1000D * averageWaterNeeded;
		AmountResource waterAR = AmountResource.findAmountResource(LifeSupportType.WATER);
		double waterAvailable = inv.getAmountResourceStored(waterAR, false);		

		double waterUsed = waterRequired;
		if (waterRequired > waterAvailable) {
			// 2015-01-25 Added diff, waterUsed and consumeWater() when grey water is not available
			double diff = waterUsed - waterAvailable;
			waterUsed = waterAvailable;
		}	
			
		Storage.retrieveAnResource(FERTILIZER_NEEDED_WATERING, FERTILIZER, inv, true);
		Storage.retrieveAnResource(waterRequired, LifeSupportType.WATER, inv, true);
	
		// Amount of water reclaimed through a Moisture Harvesting System inside the Greenhouse
		// TODO: need more work
		double waterReclaimed = waterRequired * growingArea * time / 1000D * MOISTURE_RECLAMATION_FRACTION;
		Storage.storeAnResource(waterReclaimed, LifeSupportType.WATER, inv);

		double waterModifier = waterUsed / waterRequired * .5D + .5D;
		if (waterModifier > 1.1)
			waterModifier = 1.1;
			
			
		// Calculate O2 and CO2 usage
		double o2Modifier = 0, co2Modifier = 0;

		if (sunlightModifier <= .5) {
			AmountResource o2ar = AmountResource.findAmountResource(LifeSupportType.OXYGEN);
			double o2Required = needFactor * maxPeriodHarvest * growingArea * time / 1000D * averageOxygenNeeded;
			double o2Available = inv.getAmountResourceStored(o2ar, false);
			double o2Used = o2Required;

			if (o2Used > o2Available)
				o2Used = o2Available;
			//retrieveAnResource(o2ar, o2Used);
			Storage.retrieveAnResource(o2Used, LifeSupportType.OXYGEN, inv, true);

			o2Modifier =  o2Used / o2Required * .5D + .5D;
			if (o2Modifier > 1.05)
				o2Modifier = 1.05;

			//System.out.println("Farming.java: o2Modifier is " + o2Modifier);


			// Determine the amount of co2 generated via gas exchange.
			double co2Amount = o2Used * growingArea * time / 1000D * CO2_GENERATION_RATE;
			Storage.storeAnResource(co2Amount, "carbon dioxide", inv);
		}

		else if (sunlightModifier > .5) {
			// TODO: gives a better modeling of how the amount of light available will trigger photosynthesis that converts co2 to o2
			// Determine harvest modifier by amount of carbon dioxide available.
			AmountResource carbonDioxide = AmountResource.findAmountResource("carbon dioxide");
			double carbonDioxideRequired = needFactor * maxPeriodHarvest * growingArea * time / 1000D * averageCarbonDioxideNeeded;
			double carbonDioxideAvailable = inv.getAmountResourceStored(carbonDioxide, false);
			double carbonDioxideUsed = carbonDioxideRequired;

			if (carbonDioxideUsed > carbonDioxideAvailable)
				carbonDioxideUsed = carbonDioxideAvailable;
			//retrieveAnResource(carbonDioxide, carbonDioxideUsed);
			Storage.retrieveAnResource(carbonDioxideUsed, "carbon dioxide", inv, true);

			// TODO: allow higher concentration of co2 to be pumped to increase the harvest modifier to the harvest.
			co2Modifier = carbonDioxideUsed / carbonDioxideRequired * .5D + .5D;
			// TODO: high amount of CO2 may facilitate the crop growth and reverse past bad health
			if (co2Modifier > 1.1)
				co2Modifier = 1.1;
			//System.out.println("Farming.java: co2Modifier is " + co2Modifier);

			// Determine the amount of oxygen generated via gas exchange.
			double oxygenAmount = carbonDioxideUsed * growingArea * time / 1000D * OXYGEN_GENERATION_RATE;
			Storage.storeAnResource(oxygenAmount, LifeSupportType.OXYGEN, inv);

		}


		// 2015-08-26 Tuned harvestModifier
		if (phaseType == PhaseType.GROWING) {
			// 2015-08-26 Tuned harvestModifier
			harvestModifier = .6 * harvestModifier + .4 * harvestModifier * sunlightModifier;
			//System.out.println("Farming.java: sunlight harvestModifier is " + harvestModifier);
		}
		else if (phaseType == PhaseType.GERMINATION)
			harvestModifier = .8 * harvestModifier + .2 * harvestModifier * sunlightModifier;

		if (sunlightModifier > .5) {
			// 2015-08-26 Tuned harvestModifier
			harvestModifier = .7 * harvestModifier
					+ .1 * harvestModifier * temperatureModifier
					+ .1 * harvestModifier * waterModifier
					+ .1 * harvestModifier * co2Modifier;
		}

		else {
			harvestModifier = .7 * harvestModifier
					+ .1 * harvestModifier * temperatureModifier
					+ .1 * harvestModifier * waterModifier
					+ .1 * harvestModifier * o2Modifier;
		}

		// TODO: add airPressureModifier in future

		//System.out.println("harvestModifier is "+ harvestModifier);
		return harvestModifier;
	}


	/**
	 * Retrieves an amount from an Amount Resource.
	 * @param AmountResource resource
	 * @param double amount

	// 2015-01-25 Added retrieveAnResource()
	public void retrieveAnResource(AmountResource resource, double amount) {
		try {
			inv.retrieveAmountResource(resource, amount);
		    inv.addAmountDemandTotalRequest(resource);
		    inv.addAmountDemand(resource, amount);

	    } catch (Exception e) {
	        logger.log(Level.SEVERE,e.getMessage());
		}
	}
	*/
	   /**
     * Retrieves the resource
     * @param name
     * @parama requestedAmount
    */
    //2015-02-28 Added retrieveAnResource()
    public double retrieveAnResource(String name, double requestedAmount) {
    	try {
	    	AmountResource nameAR = AmountResource.findAmountResource(name);
	        double amountStored = inv.getAmountResourceStored(nameAR, false);
	    	inv.addAmountDemandTotalRequest(nameAR);
	        if (amountStored < requestedAmount) {
	     		requestedAmount = amountStored;
	    		logger.warning("Just used up all " + name);
	        }
	    	else if (amountStored < 0.00001)
	    		logger.warning("no more " + name + " in " + settlement.getName());
	    	else {
	    		inv.retrieveAmountResource(nameAR, requestedAmount);
	    		inv.addAmountDemand(nameAR, requestedAmount);
	    	}
	    }  catch (Exception e) {
    		logger.log(Level.SEVERE,e.getMessage());
	    }

    	return requestedAmount;
    }



	/**
	 * Gets the average growing time for a crop.
	 * @return average growing time (millisols)
	 * @throws Exception if error reading crop config.
	 */
	public static double getAverageCropGrowingTime() {
		CropConfig cropConfig = SimulationConfig.instance().getCropConfiguration();
		double totalGrowingTime = 0D;
		List<CropType> cropTypes = cropConfig.getCropList();
		Iterator<CropType> i = cropTypes.iterator();
		while (i.hasNext()) totalGrowingTime += i.next().getGrowingTime()*1000D;
		return totalGrowingTime / cropTypes.size();
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		cropType = null;
		farm = null;
		inv = null;
		settlement = null;
		phaseType = null;
	}
}
