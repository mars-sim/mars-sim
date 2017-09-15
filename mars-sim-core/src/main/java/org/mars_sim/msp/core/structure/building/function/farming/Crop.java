/**
 * Mars Simulation Project
 * Crop.java
 * @version 3.1.0 2016-10-11
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function.farming;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Research;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.Conversion;


/**
 * The Crop class describes the behavior of one particular crop growing on a greenhouse.
 */
public class Crop implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static Logger logger = Logger.getLogger(Crop.class.getName());
	//private static org.apache.log4j.Logger log4j = LogManager.getLogger(Crop.class);
    private static String sourceName = logger.getName();
    
	// TODO Static members of crops should be initialized from some xml instead of being hard coded.

	// 2016-10-12 Added the limiting factor that determines how fast and how much PAR can be absorbed in one frame.
	public static final double PHYSIOLOGICAL_LIMIT = 0.9; // 1 is max. if set to 1, a lot of lights will toggle on and off indesirably.
	public static final double TISSUE_EXTRACTED_PERCENT = .1D;

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
	//public static final double WATT_TO_PHOTON_CONVERSION_RATIO = 4.609; // in u mol / m2 /s / W m-2 for Mars only
	public static final double kW_PER_HPS = .4; // using 400W HPS_LAMP
	public static final double VISIBLE_RADIATION_HPS = .4; // high pressure sodium (HPS_LAMP) lamps efficiency
	public static final double BALLAST_LOSS_HPS = .1; // for high pressure sodium (HPS_LAMP)
	public static final double NON_VISIBLE_RADIATION_HPS = .37; // for high pressure sodium (HPS_LAMP)
	public static final double CONDUCTION_CONVECTION_HPS = .13; // for high pressure sodium (HPS_LAMP)
	public static final double LOSS_FACTOR_HPS = NON_VISIBLE_RADIATION_HPS*.75 + CONDUCTION_CONVECTION_HPS/2D;
	//public static final double MEAN_DAILY_PAR = 237.2217D ; // in [mol/m2/day]
	// SurfaceFeatures.MEAN_SOLAR_IRRADIANCE * 4.56 * (not 88775.244)/1e6 = 237.2217
    private static final double T_TOLERANCE = 3D;

    public static final String TISSUE_CULTURE = "tissue culture";

    //public static final String TABLE_SALT = "table salt";
	//public static final String FERTILIZER = "fertilizer";
	//public static final String GREY_WATER = "grey water";
    //public static final String SOIL = "soil";
    //public static final String CROP_WASTE = "crop waste";
    //public static final String FOOD_WASTE = "food waste";
    //public static final String SOLID_WASTE = "solid waste";

    //public static final String NAPKIN = "napkin";
    //public static final String NaClO4 = "sodium hypochlorite";

	//public static final String OXYGEN = "oxygen";
	//public static final String WATER = "water";
	//public static final String FOOD = "food";
	//public static final String CO2 = "carbon dioxide";

	//public static final String METHANE = "methane";			// 8
	//public static final String ICE = "ice";
	//public static final String REGOLITH = "regolith";
	//public static final String ROCK_SAMPLE = "rock samples";


	// Data members
	/**	true if this crop is generated at the start of the sim  */
	private boolean isStartup;
	private boolean hasSeed = false;
	private boolean isSeedPlant = false;

	//private int numLampCache;
    /** Current sol since the start of sim. */
	//private int solCache = 1;
	/** Current sol of month. */
	private int currentSol = 1;
	/** ratio between inedible and edible biomass */
	private double ratio;
	/** a factor based on wattToPhotonConversionRatio on Mars */
    private double conversion_factor;
	/** Maximum possible food harvest for crop [in kg]. */
	private double maxHarvest;
	/** Completed work time in current phase [in millisols]. */
	private double currentPhaseWorkCompleted = 0;
	/** Actual food harvest for crop [in kg]. */
	private double actualHarvest;
	/** max possible daily harvest for crop [in kg]. */
	private double dailyMaxHarvest;
	/** Growing phase time completed thus far [in millisols]. */
	private double growingTimeCompleted;
	/** the area the crop occupies in square meters. */
	private double growingArea;
	/** Total growing days [in millisols]. */
	private double growingTime;
	/** growingTimeCompleted divided by growingTime */	
	private double fractionalGrowingTimeCompleted;
	
	private double t_initial;
	private double dailyPARRequired;
	private double cumulativeDailyPAR = 0;
	private double lightingPower = 0; // in kW
	private double healthCondition = 0;
    private double averageWaterNeeded;
    private double averageOxygenNeeded;
    private double averageCarbonDioxideNeeded;
	private double wattToPhotonConversionRatio;
	private double diseaseIndex = 0;

	/**	Past Environment Factors influencing the crop */
	private Double[] memory = new Double[]{ 1.0,  // light
											1.0,  // fertilizer
											1.0,  // temperature
											1.0,  // water
											1.0,  // o2
											1.0}; // co2
			
	private String cropName, capitalizedCropName, farmName;

	/** Current phase of crop. */
	private PhaseType phaseType;
	private CropCategoryType cropCategoryType;
	private CropType cropType;
	private Inventory inv;
	private Farming farm;
	private Settlement settlement;
	private SurfaceFeatures surface;
	private MarsClock marsClock;
	private MasterClock masterClock;
	private static CropConfig cropConfig;

	private AmountResource cropAR, tissueAR;

	//private static AmountResource foodAR = ResourceUtil.findAmountResource(FOOD); // 1 / needed by Farming 
	private static AmountResource waterAR = ResourceUtil.waterAR;//.findAmountResource(WATER); // 2
	private static AmountResource oxygenAR = ResourceUtil.oxygenAR;//.findAmountResource(OXYGEN); // 3
	private static AmountResource carbonDioxideAR = ResourceUtil.carbonDioxideAR;//.findAmountResource(CO2); // 4

	//private static AmountResource methaneAR =  ResourceUtil.findAmountResource(METHANE);			// 8
	//private static AmountResource iceAR =  ResourceUtil.findAmountResource(ICE);					// 12
	//private static AmountResource foodWasteAR =  ResourceUtil.findAmountResource(FOOD_WASTE);			// 16
	//private static AmountResource solidWasteAR =  ResourceUtil.findAmountResource(SOLID_WASTE);		// 17
	private static AmountResource greyWaterAR =  ResourceUtil.greyWaterAR;//.findAmountResource(GREY_WATER);			// 19
	//private static AmountResource tableSaltAR =  ResourceUtil.findAmountResource(TABLE_SALT); 		// 23

	//private static AmountResource regolithAR =  ResourceUtil.findAmountResource(REGOLITH);		// 142
	//private static AmountResource rockSamplesAR =  ResourceUtil.findAmountResource(ROCK_SAMPLE);	// 143

	//private static AmountResource NaClO4AR =  ResourceUtil.findAmountResource(NaClO4);	// 145
	//private static AmountResource napkinAR =  ResourceUtil.findAmountResource(NAPKIN);				// 150

	private static AmountResource cropWasteAR =  ResourceUtil.cropWasteAR;//.findAmountResource(CROP_WASTE);
	private static AmountResource fertilizerAR =  ResourceUtil.fertilizerAR;//.findAmountResource(FERTILIZER);
	//private static AmountResource soilAR =  ResourceUtil.findAmountResource(SOIL);
    
	private static AmountResource seedAR;

	private Map<Integer, Phase> phases = new HashMap<>();

	DecimalFormat fmt = new DecimalFormat("0.00000");

	/**
	 * Constructor.
	 * @param cropType the type of crop.
	 * @param growingArea the area occupied by the crop
	 * @param dailyMaxHarvest - Maximum possible food harvest for crop. (kg)
	 * @param farm - Farm crop being grown in.
	 * @param settlement - the settlement the crop is located at.
	 * @param isStartup - true if this crop is generated at the start of the sim)  
	 * @param tissuePercent the percentage of ticarbonDioxideARure available based on the requested amount
	 */
	// Called by Farming.java constructor and timePassing()
	// 2015-08-26 Added new param percentGrowth
	public Crop(CropType cropType, double growingArea, double dailyMaxHarvest, Farming farm,
			Settlement settlement, boolean isStartup, double tissuePercent) {
		this.cropType = cropType;
		this.cropCategoryType = cropType.getCropCategoryType();
		this.isStartup = isStartup;
		
		this.farm = farm;
		farmName = farm.getBuilding().getNickName();
		this.settlement = settlement;
		this.growingArea = growingArea;
		this.dailyMaxHarvest = dailyMaxHarvest;

        sourceName = sourceName.substring(sourceName.lastIndexOf(".") + 1, sourceName.length());
        
		phases = cropType.getPhases();

		for (Phase p : phases.values()) {
			p.setHarvestFactor(1);
		}
		
		cropName = cropType.getName();
		String tissue = cropName + " " + TISSUE_CULTURE;

		//2017-03-30 Add special case for extracting seeds from White Mustard
		if (cropName.equalsIgnoreCase("White Mustard"))
			hasSeed = true;

		else if (cropName.equalsIgnoreCase("Sesame"))
			isSeedPlant = true;

		if (hasSeed) {
			ratio = cropType.getInedibleBiomass()/cropType.getEdibleBiomass();
			seedAR = AmountResource.findAmountResource("Mustard Seed");
		}

		if (isSeedPlant) {
			ratio = cropType.getInedibleBiomass()/cropType.getEdibleBiomass();
			seedAR = AmountResource.findAmountResource(cropName + " Seed");
		}

		cropAR = AmountResource.findAmountResource(cropName);
		tissueAR = AmountResource.findAmountResource(tissue);

		cropConfig = SimulationConfig.instance().getCropConfiguration();
	    averageWaterNeeded = cropConfig.getWaterConsumptionRate();
	    averageOxygenNeeded = cropConfig.getOxygenConsumptionRate();
	    averageCarbonDioxideNeeded = cropConfig.getCarbonDioxideConsumptionRate();
		wattToPhotonConversionRatio = cropConfig.getWattToPhotonConversionRatio();

		surface = Simulation.instance().getMars().getSurfaceFeatures();
	    conversion_factor = 1000D * wattToPhotonConversionRatio / MarsClock.SECONDS_IN_MILLISOL ;

		masterClock = Simulation.instance().getMasterClock();
		marsClock = masterClock.getMarsClock();

		inv = settlement.getInventory();
		t_initial = farm.getBuilding().getInitialTemperature();

		// 2015-04-08  Added dailyPARRequired
		dailyPARRequired = cropType.getDailyPAR();
		cropName = cropType.getName();
		capitalizedCropName = Conversion.capitalize(cropType.getName());
		// growingTime in millisols
		growingTime = cropType.getGrowingTime();
		// growingDay in sols
		double growingDay = growingTime/1000D;
		maxHarvest = dailyMaxHarvest * growingDay;

		if (!isStartup) {
			
			if (tissuePercent <= 0) {
				// assume a max 2-day incubation period if no 0% tissue culture is available
				currentPhaseWorkCompleted = 0;
				phaseType = PhaseType.INCUBATION;
				logger.info(capitalizedCropName
						+ " has no tissue culture left. Will start "
						//+ "a full work period of "
						//+ Math.round(growingTimeCompleted/1000D*10D)/10D + " sols "
						+ "restocking it via incubation in " + farmName + " at " + settlement);
			}

			else if (tissuePercent >= 100) {
				// assume zero day incubation period if 100% tissue culture is available
				currentPhaseWorkCompleted = 0;
				phaseType = PhaseType.PLANTING;
				logger.info("Proceeds to transferring plantflets from "
						+ capitalizedCropName + "'s tissue culture into the field.");
			}

			else {
				currentPhaseWorkCompleted = 1000D * phases.get(0).getWorkRequired() * (100D - tissuePercent) / 100D;
				phaseType = PhaseType.INCUBATION;
				logger.info(capitalizedCropName + " needs a work period of "
						+ Math.round(currentPhaseWorkCompleted/1000D*10D)/10D 
						+ " sols to clone enough tissues before planting in " + farmName + " at " + settlement);
			}

		}

		else {
			// At the start of the sim, set up a crop's "initial" percentage of growth randomly
			growingTimeCompleted = RandomUtil.getRandomDouble(growingTime * .95); // for testing only : growingTimeCompleted = growingTime - 3000 + RandomUtil.getRandomDouble(3000D); or = growingTime * .975;

			fractionalGrowingTimeCompleted = growingTimeCompleted / growingTime;
/*
			int current = getCurrentPhaseNum();
			
			if (fractionalGrowingTimeCompleted * 100D > getUpperPercent(current)) {
				phaseType = phases.get(current + 1).getPhaseType();
			}
*/
			int size = phases.size();
			for (int i = 0; i < size-1; i++) {
				if (fractionalGrowingTimeCompleted * 100D > getUpperPercent(i)) {
					phaseType = cropType.getPhases().get(i+1).getPhaseType();
					//System.out.println(cropType.getName() + "   i : " + i + "   phaseType : " + phaseType);
				}
			}

			actualHarvest = maxHarvest * fractionalGrowingTimeCompleted;
		}
		
		computeHealth();

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
	//public String getCategory() {
	//	return cropType.getCropCategory();
	//}

	/**
	 * Gets the maximum possible food harvest for crop.
	 * @return food harvest (kg.)
	 */
	public double getMaxHarvest() {
		return maxHarvest;
	}

	/**
	 * Gets the amount of growing time completed.
	 * @return growing time (millisols)
	 */
	public double getGrowingTimeCompleted() {
		return growingTimeCompleted;
	}

	/**
	 * Checks if crop needs additional work on current sol.
	 * @return true if more work needed.
	 */
	public boolean requiresWork() {
		int n = getCurrentPhaseNum();
		if (n == -1)
			return false;
		else if (phaseType == PhaseType.HARVESTING)
			return true;
		else 
			return phases.get(n).getWorkRequired() * 1000D >= currentPhaseWorkCompleted;
	}

	/**
	 * Compute the overall health condition of the crop.
	 * @return condition as value from 0 (poor) to 1 (healthy)
	 */
	public double computeHealth() {
		// 0:bad, 1:good
		double health = 0D;

		//fractionalGrowingTimeCompleted = growingTimeCompleted/growingTime;
		
		int current = getCurrentPhaseNum();
		int length = phases.size();

		if (current < 2) {
			health = 1D;
		}
		
		else if (current == 2) {
			if (fractionalGrowingTimeCompleted <= .02 ) {
				// avoid initial spurious data at the start of the sim
				health = 1D;
			}
			else {
				health = getHealth();
			}
		}
		
		else if (current > 2 && current < length - 1) { 
			// Including the harvesting phase
			// Note : the crop will spend most of the time here
			health = getHealth();
		}

		else
			health = getHealth();

		if (health > 1D) health = 1D;
		else if (health < 0D) health = 0D;

		if (fractionalGrowingTimeCompleted >= .1) {
			// Check on the health of a >10% growing crop
			if (health < .07) {
				logger.info("Crop " + capitalizedCropName + " at " + settlement.getName() + " died of very poor health (" + Math.round(health*100D)/100D + " %) in " 
						+ settlement.getName() + " and didn't survive.");
				// 2015-02-06 Added Crop Waste
				if (actualHarvest > 0)
					Storage.storeAnResource(actualHarvest, cropWasteAR, inv, "::computeHealth");
				logger.info(actualHarvest + " kg Crop Waste generated from the dead "+ capitalizedCropName);
				phaseType = PhaseType.FINISHED;
			}
		}

		//else if (fractionalGrowingTimeCompleted == 0D) {
		//	; // do nothing
		//}
		
		else { // fractionalGrowthCompleted < .1 && fractionalGrowthCompleted > 0D
			// Seedling (<10% grown crop) is less resilient and more prone to environmental factors			
			if (health < .2) {
				logger.info("The seedlings of " + capitalizedCropName + " had poor health (" + Math.round(health*100D)/100D + " %) in " 
						+ settlement.getName() + " and didn't survive.");
				// 2015-02-06 Added Crop Waste
				if (actualHarvest > 0)
					Storage.storeAnResource(actualHarvest, cropWasteAR, inv, "::computeHealth");
				logger.info(actualHarvest + " kg Crop Waste generated from the dead "+ capitalizedCropName);
				//actualHarvest = 0;
				//growingTimeCompleted = 0;
				phaseType = PhaseType.FINISHED;
			}
		}

		// set healthCondition so that it can be accessed outside of this class 
		healthCondition = health;
		//logger.info(capitalizedCropName + "'s health : " + Math.round(health*100D)/100D);
		
		return health;
	}

	/*
	 * Computes the health of a crop
	 */
	//2016-07-15 Added getHealth()
	public double getHealth() {
		double env_factor = 0;
		for (double m : memory) {
			env_factor = env_factor + m;
		}
		if (env_factor/5D > 1.1)
			env_factor = 1.1;
		else
			env_factor = env_factor/5D;
		//logger.info(capitalizedCropName + "'s fractionalGrowingTimeCompleted : " + Math.round(fractionalGrowingTimeCompleted*100D)/100D + "   env memory : " + Math.round(env_factor*100D)/100D + "   actualHarvest/maxHarvest : " + Math.round(actualHarvest*100D)/100D + "/" + Math.round(maxHarvest*100D)/100D + " = " + Math.round(actualHarvest/maxHarvest*100D)/100D);
		return (1 - diseaseIndex) * env_factor;// * actualHarvest / maxHarvest / fractionalGrowingTimeCompleted ;
	}

	/**
	 * Adds work time to the crops current phase.
	 * @param workTime - Work time to be added (millisols)
	 * @return workTime remaining after working on crop (millisols)
	 * @throws Exception if error adding work.
	 */
	// Called by Farming.java's addWork()
	public double addWork(Unit unit, double workTime) {
		
		double remainingWorkTime = workTime;
		int current = getCurrentPhaseNum();
		int length = phases.size();
		//if (current == -1)
        //    throw new IllegalArgumentException("The current phase is invalid");
		//else if (current == length - 2)
		double w = phases.get(current).getWorkRequired() * 1000D;
		//logger.info(capitalizedCropName + "'s phaseType : " + phaseType 
		///		+ "   currentPhaseWorkCompleted is " + Math.round(currentPhaseWorkCompleted*10D)/10D
		//		+ "   Work Required is " + w);

		if (actualHarvest < 0D) {
			actualHarvest = 0;
			growingTimeCompleted = 0;
		}

		if (current == 0 && current == 1) {
			// at a particular growing phase (NOT including the harvesting phase)
			currentPhaseWorkCompleted += remainingWorkTime;

			if (currentPhaseWorkCompleted >= w) {
				remainingWorkTime = currentPhaseWorkCompleted - w;
				currentPhaseWorkCompleted = 0D;
				phaseType = phases.get(current + 1).getPhaseType();
				logger.info(capitalizedCropName + "'s phaseType has become " + phaseType 
						+ "   currentPhaseWorkCompleted is " + Math.round(currentPhaseWorkCompleted*10D)/10D
						+ "   Work Required is " + Math.round(w*10D)/10D);
			}
			else
				remainingWorkTime = 0D;
		}

		else if (current < length - 2) {
			// at a particular growing phase (NOT including the harvesting phase)
			currentPhaseWorkCompleted += remainingWorkTime;

			if (currentPhaseWorkCompleted >= w) {
				remainingWorkTime = currentPhaseWorkCompleted - w;
				currentPhaseWorkCompleted = 0D;
				//phaseType = phases.get(current + 1).getPhaseType();
			}
			else
				remainingWorkTime = 0D;
		}
		
		// TODO: for leaves crop, one should be able to harvest leaves ANYTIME and NOT to have to wait until harvest
		else if (current == length - 2) {
			// at the harvesting phase
			currentPhaseWorkCompleted += remainingWorkTime;
			//logger.info(capitalizedCropName + " is in the Harvesting phase"); //+ "     currentPhaseWorkCompleted : " + currentPhaseWorkCompleted + "    Work Required : " + w);
			if (currentPhaseWorkCompleted >= w) {
				// Harvest is over. Close out this phase
				//logger.info("addWork() : done harvesting. remainingWorkTime is " + Math.round(remainingWorkTime));
				double overWorkTime = currentPhaseWorkCompleted - w;
				// 2014-10-07 modified parameter list to include crop name
				double lastHarvest = actualHarvest * (remainingWorkTime - overWorkTime) / w;
				
				if (lastHarvest > 0) {
					// Store the crop harvest
					if (isSeedPlant)
						Storage.storeAnResource(lastHarvest, seedAR, inv, sourceName + "::addWork");
					else
						Storage.storeAnResource(lastHarvest, cropAR, inv, sourceName + "::addWork");
	
					logger.info(unit.getName() + " just closed out the harvest of " + capitalizedCropName
							+ " in " + farmName
							+ " at " + settlement.getName());
					// 2017-03-30 Extract Mustard Seed
					if (hasSeed)
						Storage.storeAnResource(lastHarvest * ratio, seedAR, inv);
					else
						//2017-03-30 in case of white mustard, the inedible biomass is used as the seed mass
						// thus no crop waste
						generateCropWaste(lastHarvest);
					// 2015-10-13 Check to see if a botany lab is available
					if (!checkBotanyLab())
						logger.info("Can't find an available lab bench to work on the tissue culture for " + cropName);
	
					remainingWorkTime = overWorkTime;
					
				}
				
				phaseType = PhaseType.FINISHED;
			}
			
			else { 	
				
				if (actualHarvest > 0.0001) {
					// continue the harvesting process
					// 2014-10-07 modified parameter list to include crop name
					double modifiedHarvest = actualHarvest * workTime / w;
					// Store the crop harvest
					if (modifiedHarvest > 0) {
						if (isSeedPlant)
							Storage.storeAnResource(modifiedHarvest, seedAR, inv, sourceName + "::addWork");
						else
							Storage.storeAnResource(modifiedHarvest, cropAR, inv, sourceName + "::addWork");
		
						// 2017-03-30 Extract Mustard Seed
						if (hasSeed)
							Storage.storeAnResource(modifiedHarvest * ratio, seedAR, inv, sourceName + "addWork");
						else
							//2017-03-30 in case of white mustard, the inedible biomass is used as the seed mass
							// thus no crop waste
							generateCropWaste(modifiedHarvest);
					}
					
					//logger.info(unit.getName() + " harvested " + Math.round(modifiedHarvest * 10_000.0)/10_000.0 
					//		+ " kg of " + capitalizedCropName + " in " + farm.getBuilding().getNickName()
					//		+ " at " + settlement.getName());
					
				    LogConsolidated.log(logger, Level.INFO, 5000, sourceName, 
				    		unit.getName() + " harvested " + Math.round(modifiedHarvest * 10_000.0)/10_000.0 
							+ " kg of " + capitalizedCropName + " in " + farm.getBuilding().getNickName()
							+ " at " + settlement.getName()
							, null);
				    
					remainingWorkTime = 0D;

				}
			}
		}
		

		return remainingWorkTime;
	}

	/*
	 * Checks to see if a botany lab with an open research slot is available and performs cell tissue extraction
	 */
	public boolean checkBotanyLab() {
		// 2015-10-13 Check to see if a botany lab is available
		boolean hasEmptySpace = false;
		boolean done = false;
		Research lab0 = (Research) farm.getBuilding().getFunction(FunctionType.RESEARCH);
		// Check to see if the local greenhouse has a research slot
		if (lab0.hasSpecialty(ScienceType.BOTANY)) {
			hasEmptySpace = lab0.checkAvailability();
		}

		if (hasEmptySpace) {

			hasEmptySpace = lab0.addResearcher();
			if (hasEmptySpace) {
				preserveCropTissue(lab0, true);
				lab0.removeResearcher();
			}

			done = true;
		}

		else {
			// Check available research slot in another lab located in another greenhouse
			List<Building> laboratoryBuildings = settlement.getBuildingManager().getBuildings(FunctionType.RESEARCH);
			Iterator<Building> i = laboratoryBuildings.iterator();
			while (i.hasNext() && !hasEmptySpace) {
				Building building = i.next();
				Research lab1 = (Research) building.getFunction(FunctionType.RESEARCH);
				if (lab1.hasSpecialty(ScienceType.BOTANY)) {
					hasEmptySpace = lab1.checkAvailability();
					if (hasEmptySpace) {
						hasEmptySpace = lab1.addResearcher();
						if (hasEmptySpace) {
							preserveCropTissue(lab1, true);
							lab0.removeResearcher();
						}

						// TODO: compute research ooints to determine if it can be carried out.
						// int points += (double) (lab.getResearcherNum() * lab.getTechnologyLevel()) / 2D;
						done = true;
					}
				}
			}
		}

		// check to see if a person can still "squeeze into" this busy lab to get lab time
		if (!hasEmptySpace && (lab0.getLaboratorySize() == lab0.getResearcherNum())) {
			preserveCropTissue(lab0, false);
			done = true;
		}
		else {

			// Check available research slot in another lab located in another greenhouse
			List<Building> laboratoryBuildings = settlement.getBuildingManager().getBuildings(FunctionType.RESEARCH);
			Iterator<Building> i = laboratoryBuildings.iterator();
			while (i.hasNext() && !hasEmptySpace) {
				Building building = i.next();
				Research lab2 = (Research) building.getFunction(FunctionType.RESEARCH);
				if (lab2.hasSpecialty(ScienceType.BOTANY)) {
					hasEmptySpace = lab2.checkAvailability();
					if (lab2.getLaboratorySize() == lab2.getResearcherNum()) {
						preserveCropTissue(lab2, false);
						done = true;
					}
				}
			}
		}

		return done;
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
		amount = Math.round((amount * .5 + amount * rand)*10_000.0)/10_000.0;

		if (amount > 0)
			Storage.storeAnResource(amount, tissueAR, inv, "::preserveCropTissue");

		// 2015-10-13 if no dedicated research space is available, work can still be performed but productivity is cut half
		if (hasSpace) {
			logger.info(amount + " kg " + capitalizedCropName + " " + TISSUE_CULTURE + " extracted & cryo-preserved in "
					+ lab.getBuilding().getNickName() + " at " + settlement.getName());
		}
		else {
			amount = amount / 2D;
			logger.info("Not enough botany research space. Only " + amount + " kg (at reduced capacity) "
					+ capitalizedCropName + " " + TISSUE_CULTURE
					+ " extracted & cryo-preserved in "
					+ lab.getBuilding().getNickName() + " at " + settlement.getName());
		}
	}

    /**
     * Compute the amount of crop waste generated
     */
	public void generateCropWaste(double harvestMass) {
		// 2015-02-06 Added Crop Waste
		double amountCropWaste = harvestMass * cropType.getInedibleBiomass() / (cropType.getInedibleBiomass() + cropType.getEdibleBiomass());
		if (amountCropWaste > 0)
			Storage.storeAnResource(amountCropWaste, cropWasteAR, inv, "::generateCropWaste");
		//logger.info("addWork() : " + cropName + " amountCropWaste " + Math.round(amountCropWaste * 1000.0)/1000.0);
	}

	/**
	 * Time passing for crop.
	 * @param time - amount of time passing (millisols)
	 */
	public void timePassing(double time) {

		int current = getCurrentPhaseNum();
		int length = phases.size();
		
		fractionalGrowingTimeCompleted = growingTimeCompleted / growingTime;
		
		growingTimeCompleted += time;
		//double w = phases.get(current).getWorkRequired() * 1000D;
		//System.out.print("timePassing() : current phase is " + current);
		//System.out.print("   growingTimeCompleted is " + Math.round(growingTimeCompleted*10D)/10D);
		//System.out.println("   growingTime is " + growingTime);
		
		//+ "    " + Math.round(growingTimeCompleted/w *10D)/10D + "%");
/*		
		if (current <= 1) {
			growingTimeCompleted += time;
			
			if (growingTimeCompleted / growingTime * 100D > getUpperPercent(current)) {
				phaseType = phases.get(current + 1).getPhaseType();//PhaseType.PLANTING;
			}
		}
*/
		
		if (current > 1 && current < length - 1) { // Note: (length - 1) does include the harvesting phase.
			if (time > 0D) {
				//growingTimeCompleted += time;

				if (current < length - 2) { 
					// in a growing phase (excluding the harvesting phase).
					//System.out.println("   upper percent is " + getUpperPercent(current));
					if (fractionalGrowingTimeCompleted * 100D > getUpperPercent(current)) {
						// Advance onto the next phase
						phaseType = cropType.getPhases().get(current + 1).getPhaseType();
						//currentPhaseWorkCompleted = 0D;
					} 
				}
				
				// check for the passing of each day
				int newSol = marsClock.getMissionSol();
				if (newSol != currentSol) {
					// TODO: what needs to be done at the end of each sol ?
					currentSol = newSol;
					//double maxDailyHarvest = maxHarvest / cropGrowingDay;
					double w = phases.get(current).getWorkRequired() * 1000D;
					
					double dailyWorkCompleted = currentPhaseWorkCompleted / w;
					// Modify actual harvest amount based on daily tending work.
					actualHarvest += (dailyMaxHarvest * (dailyWorkCompleted - .5D));
					
					if (actualHarvest < 0) {
						phaseType = PhaseType.FINISHED;
						actualHarvest = 0;
						return;
					}
					// TODO: is it better off doing the actualHarvest computation once a day or every time
					// Reset the daily work counter currentPhaseWorkCompleted back to zero
					//currentPhaseWorkCompleted = 0D;
					cumulativeDailyPAR = 0;
				}
				
			    int currentMillisols = (int) marsClock.getMillisol();
				if (currentMillisols % 250 == 0) {
					// Compute health condition 4 times a day
					computeHealth();
				}

				// max possible harvest within this period of time
				double maxPeriodHarvest = maxHarvest * (time / growingTime);
				// Compute each harvestModifiers and sum them up below
				double harvestModifier = computeHarvest(maxPeriodHarvest, time);
				// Modify harvest amount.
				actualHarvest += maxPeriodHarvest * harvestModifier;// * 10D; // assuming the standard area of 10 sq m

				if (actualHarvest < 0) {
					phaseType = PhaseType.FINISHED;
					actualHarvest = 0;
					return;
				}

			}
		}

		else if (phaseType == PhaseType.FINISHED) {
			actualHarvest = 0;
			growingTimeCompleted = 0;
		}

	}

	public void turnOnLighting(double kW) {
		lightingPower = kW;
	}

	public void turnOffLighting() {
		lightingPower = 0;
	}

	
	/**
	 * Computes the effects of the available sunlight and artificial light
	 * @param time
	 * @return instantaneous PAR or uPAR
	 */
	public double computeLight(double time) {
		double lightModifier = 0;
		
	    double currentMillisols = marsClock.getMillisol();
	    // Note : The average PAR is estimated to be 20.8 mol/(m² day) (Gertner, 1999)
		// 2015-04-09 Calculate instantaneous PAR from solar irradiance
		double uPAR = wattToPhotonConversionRatio * surface.getSolarIrradiance(settlement.getCoordinates());
		// [umol /m^2 /s] = [u mol /m^2 /s /(Wm^-2)]  * [Wm^-2]
		double PAR_interval = uPAR / 1_000_000D * time * MarsClock.SECONDS_IN_MILLISOL ; // in mol / m^2 within this period of time
		// [mol /m^2] = [umol /m^2 /s] / u  * [millisols] * [s /millisols]
		// 1 u = 1 micro = 1/1_000_000
		// Note : daily-PAR has the unit of [mol /m^2 /day]
	    // Gauge if there is enough sunlight
	    double progress = cumulativeDailyPAR / dailyPARRequired; //[max is 1]
	    
	    double clock = currentMillisols / 1000D; //[max is 1]
	/*		logger.info("uPAR : "+ fmt.format(uPAR)
				+ "\tPAR_interval : " + fmt.format(PAR_interval)
				+ "\tprogress : "+ fmt.format(progress)
				+ "\truler : " + fmt.format(clock));
	*/
	    // When enough PAR have been administered to the crop, the HPS_LAMP will turn off.
	    // TODO: what if the time zone of a settlement causes sunlight to shine at near the tail end of the currentMillisols time ?
	    // 2015-04-09 Compared cumulativeDailyPAR / dailyPARRequired  vs. current time / 1000D
		// 2016-10-12 Reduce the frequent toggling on and off of lamp and to check on the time of day to anticipate the need of sunlight.
	    if (0.5 * progress < clock && currentMillisols <= 333
			|| 0.7 * progress < clock && currentMillisols > 333 && currentMillisols <= 666
			|| progress < clock && currentMillisols > 666
			) {
	    	// TODO: also compare also how much more sunlight will still be available
	    	if (uPAR > 40) { // if sunlight is available
				turnOffLighting();
	    		cumulativeDailyPAR = cumulativeDailyPAR + PAR_interval ;
	/*			    logger.info(cropType.getName()
			    		+ "\tcumulativeDailyPAR : " + fmt.format(cumulativeDailyPAR)
						+ "\tdelta_PAR_sunlight : "+ fmt.format(delta_PAR_sunlight));
	*/
	    	}
	
	    	else { //if no sunlight, turn on artificial lighting
	    		//double conversion_factor = 1000D * wattToPhotonConversionRatio / MarsClock.SECONDS_IN_MILLISOL  ;
	    		// DLI is Daily Light Integral is the unit for for cumulative light -- the accumulation of all the PAR received during a day.
	    		double DLI = dailyPARRequired - cumulativeDailyPAR; // [in mol / m^2 / day]	    		
				double delta_PAR_outstanding = DLI * (time / 1000D) * growingArea;
				// in mol needed at this delta time [mol] = [mol /m^2 /day] * [millisol] / [millisols /day] * m^2
				double delta_kW = delta_PAR_outstanding / time / conversion_factor ;
				// [kW] =  [mol] / [u mol /m^2 /s /(Wm^-2)] / [millisols] / [s /millisols]  = [W /u] * u * k/10e-3 = [kW];  since 1 u = 10e-6
		    	// TODO: Typically, 5 lamps per square meter for a level of ~1000 mol/ m^2 /s
				// 2016-10-12 Added PHYSIOLOGICAL_LIMIT sets a realistic limit for tuning how much PAR a food crop can absorb per frame.
				// Note 1 : PHYSIOLOGICAL_LIMIT minimize too many lights turned on and off too frequently
				// Note 2 : It serves to smooth out the instantaneous power demand over a period of time
		    	// each HPS_LAMP lamp supplies 400W has only 40% visible radiation efficiency
				int numLamp = (int) (Math.ceil(delta_kW / kW_PER_HPS / VISIBLE_RADIATION_HPS / (1-BALLAST_LOSS_HPS) * PHYSIOLOGICAL_LIMIT));
				// TODO: should also allow the use of LED_KIT for lighting
				// For converting lumens to PAR/PPF, see http://www.thctalk.com/cannabis-forum/showthread.php?55580-Converting-lumens-to-PAR-PPF
				// Note: do NOT include any losses below
		    	double supplykW = numLamp * kW_PER_HPS * VISIBLE_RADIATION_HPS * (1-BALLAST_LOSS_HPS) / PHYSIOLOGICAL_LIMIT;
				turnOnLighting(supplykW);
		    	double delta_PAR_supplied = supplykW * time  * conversion_factor / growingArea; // in mol / m2
				// [ mol / m^2]  = [kW] * [u mol /m^2 /s /(Wm^-2)] * [millisols] * [s /millisols] /  [m^2] = k u mol / W / m^2 * (10e-3 / u / k) = [mol / m^-2]
			    cumulativeDailyPAR = cumulativeDailyPAR + delta_PAR_supplied + PAR_interval;
				// [mol /m^2 /d]
	/*			    logger.info(cropType.getName()
			    		+ "\tPAR_outstanding_persqm_daily : " + fmt.format(PAR_outstanding_persqm_daily)
			    		+ "\tdelta_PAR_outstanding : " + fmt.format(delta_PAR_outstanding)
						+ "\tdelta_kW : "+ fmt.format(delta_kW)
			    		+ "\tnumLamp : " + numLamp
						+ "\tsupplykW : "+ fmt.format(supplykW)
						+ "\tdelta_PAR_supplied : "+ fmt.format(delta_PAR_supplied)
						+ "\tdelta_PAR_sunlight : "+ fmt.format(delta_PAR_sunlight)
	    				+ "\tcumulativeDailyPAR : " + fmt.format(cumulativeDailyPAR));
	*/
	    	}
	    }
	
	    else {
	
	    	turnOffLighting();
			// TODO : move the curtain out to block excessive sunlight
	/*		    logger.info(cropType.getName()
		    		+ "\tcumulativeDailyPAR : " + fmt.format(cumulativeDailyPAR));
					//+ "\tdelta_PAR_sunlight : "+ fmt.format(delta_PAR_sunlight));
	*/
	    }
	
	    // check for the passing of each day
	    int newSol = marsClock.getMissionSol();
		// the crop has memory of the past lighting condition
		lightModifier = cumulativeDailyPAR / dailyPARRequired;
		// TODO: If too much light, the crop's health may suffer unless a person comes to intervene
		if (isStartup && newSol == 1) {
			// if this crop is generated at the start of the sim, lightModifier will be artificially lower
			// need to add adjustment
			lightModifier = lightModifier / fractionalGrowingTimeCompleted ;
		}
			
		memory[0] = .33 + .33 * lightModifier + .33 * memory[0];
		// use .2 instead of .5 since it's normal for crop to go through day/night cycle
		if (memory[0] > 1.5)
			memory[0] = 1.5;
		else if (memory[0] < 0.5)
			memory[0] = 0.5;
		
		return uPAR;

	}

	/**
	 * Compute the effect of the temperature
	 */
	public void computeTemperature() {
		
		double temperatureModifier = 0;
		double t_now = farm.getBuilding().getCurrentTemperature();

		if (t_now > (t_initial + T_TOLERANCE))
			temperatureModifier = t_initial / t_now;
		else if (t_now < (t_initial - T_TOLERANCE))
			temperatureModifier = t_now / t_initial;
		else
			// TODO: implement optimal growing temperature for each particular crop
			temperatureModifier = 1D;

		memory[2] = .5 * temperatureModifier + .5 * memory[2];
		if (memory[2] > 1.1)
			memory[2] = 1.1;

	}
		
	
	/***
	 * Computes the effect of water and fertilizer
	 * @param needFactor
	 * @param maxPeriodHarvest
	 * @param time
	 */
	public void computeWaterFertilizer(double needFactor, double maxPeriodHarvest, double time) {

		// Calculate water usage
		double waterRequired = needFactor * maxPeriodHarvest * growingArea * time / 1000D * averageWaterNeeded;
		// Determine the amount of grey water available.
		double greyWaterAvailable = inv.getAmountResourceStored(greyWaterAR, false);
		double waterUsed = 0;
		double totalWaterUsed = 0;
		
		double waterModifier = 0;
		double fertilizerModifier = 0;

		// First water crops with grey water if it is available.
		if (greyWaterAvailable >= waterRequired) {
			waterUsed = waterRequired;
		    Storage.retrieveAnResource(waterUsed, greyWaterAR, inv, true);
		    waterModifier = 1D;
		}
		// If not enough grey water, use water mixed with fertilizer.
		else if (greyWaterAvailable < waterRequired) {
		    Storage.retrieveAnResource(greyWaterAvailable, greyWaterAR, inv, true);

		    double waterAvailable = inv.getAmountResourceStored(waterAR, false);
		    waterUsed = waterRequired - greyWaterAvailable;
		    
		    if (waterUsed > waterAvailable) {
		    	waterUsed = waterAvailable;
		        Storage.retrieveAnResource(waterUsed, waterAR, inv, true);
		    }

	        // Incur penalty if water is NOT available 
		    waterModifier = (greyWaterAvailable + waterUsed)/waterRequired;
		    
		    double fertilizerAvailable = inv.getAmountResourceStored(fertilizerAR, false);
		    double fertilizerRequired = FERTILIZER_NEEDED_WATERING * growingArea * time;
		    double fertilizerUsed = fertilizerRequired;

		    if (fertilizerUsed > fertilizerAvailable) {
		        fertilizerUsed = fertilizerAvailable;
		        // should incur penalty due to insufficient fertilizer
		        fertilizerModifier = fertilizerUsed / fertilizerAvailable;
		    }
		    else
		    	fertilizerModifier = 1D;
		    
		    if (fertilizerUsed > 0D) {
		        Storage.retrieveAnResource(fertilizerUsed, fertilizerAR, inv, true);
		    }

			memory[1] = .5 * fertilizerModifier + .5 * memory[1];
			if (memory[1] > 1.1)
				memory[1] = 1.1;
			
			totalWaterUsed = greyWaterAvailable + waterUsed;
		}


		// Amount of water reclaimed through a Moisture Harvesting System inside the Greenhouse
		// TODO: Modify harvest modifier according to the moisture level
		double waterReclaimed = totalWaterUsed * growingArea * time / 1000D * MOISTURE_RECLAMATION_FRACTION;
		if (waterReclaimed > 0)
			Storage.storeAnResource(waterReclaimed, waterAR, inv, sourceName + "::computeWaterFertilizer");

		memory[3] = .5 * waterModifier + .5 * memory[3];
		if (memory[3] > 1.1)
			memory[3] = 1.1;

	}
	
	/***
	 * Computes the effects of the concentration of O2 and CO2
	 * @param uPAR
	 * @param needFactor
	 * @param maxPeriodHarvest
	 * @param time
	 */
	public void computeGases(double uPAR, double needFactor, double maxPeriodHarvest, double time) {
		
		// Calculate O2 and CO2 usage
		double o2Modifier = 0, co2Modifier = 0;

		if (uPAR < 40) {
			// during the night
			double o2Required = needFactor * maxPeriodHarvest * growingArea * time / 1000D * averageOxygenNeeded;
			double o2Available = inv.getAmountResourceStored(oxygenAR, false);
			double o2Used = o2Required;

			if (o2Used > o2Available)
				o2Used = o2Available;
			if (o2Used > 0)
				Storage.retrieveAnResource(o2Used, oxygenAR, inv, true);

			o2Modifier =  o2Used / o2Required;

			memory[4] = .5 * o2Modifier + .5 * memory[4];
			if (memory[4] > 1.1)
				memory[4] = 1.1;
			
			// Determine the amount of co2 generated via gas exchange.
			double co2Amount = o2Used * growingArea * time / 1000D * CO2_GENERATION_RATE;
			if (co2Amount > 0)
				Storage.storeAnResource(co2Amount, carbonDioxideAR, inv, sourceName + "::computeGases");
		}

		else {
			// during the day
			// TODO: gives a better modeling of how the amount of light available will trigger photosynthesis that converts co2 to o2
			// Determine harvest modifier by amount of carbon dioxide available.
			double carbonDioxideRequired = needFactor * maxPeriodHarvest * growingArea * time / 1000D * averageCarbonDioxideNeeded;
			double carbonDioxideAvailable = inv.getAmountResourceStored(carbonDioxideAR, false);
			double carbonDioxideUsed = carbonDioxideRequired;

			// TODO: allow higher concentration of co2 to be pumped to increase the harvest modifier to the harvest.

			if (carbonDioxideUsed > carbonDioxideAvailable)
				carbonDioxideUsed = carbonDioxideAvailable;
			if (carbonDioxideUsed > 0)
				Storage.retrieveAnResource(carbonDioxideUsed, carbonDioxideAR, inv, true);

			// TODO: research how much high amount of CO2 may facilitate the crop growth and reverse past bad health

			co2Modifier = carbonDioxideUsed / carbonDioxideRequired;

			memory[5] = .5 * co2Modifier + .5 * memory[5];
			if (memory[5] > 1.1)
				memory[5] = 1.1;
			
			// Determine the amount of oxygen generated via gas exchange.
			double oxygenAmount = carbonDioxideUsed * growingArea * time / 1000D * OXYGEN_GENERATION_RATE;
			if (oxygenAmount > 0)
				Storage.storeAnResource(oxygenAmount, oxygenAR, inv, sourceName + "::computeGases");

		}
		
	}
	
	/**
	 * Computes each input and output constituent for a crop for the specified period of time and return the overall harvest modifier
	 * @param the maximum possible growth/harvest
	 * @param a period of time in millisols
	 * @return the harvest modifier
	 */
	public double computeHarvest(double maxPeriodHarvest, double time) {

		double harvestModifier = 1D;
		
		// TODO: use theoretical model for crop growth, instead of empirical model below.
		// TODO: the calculation should be uniquely tuned to each crop
		// TODO: Modify harvest modifier according to the pollination by the number of bees in the greenhouse
		// TODO: Modify harvest modifier by amount of artificial light available to the whole greenhouse
		
		if (surface == null)
			surface = Simulation.instance().getMars().getSurfaceFeatures();

		if (masterClock == null)
			masterClock = Simulation.instance().getMasterClock();

		if (marsClock == null)
			marsClock = masterClock.getMarsClock();

		int phaseNum = getCurrentPhaseNum();
		int length = phases.size();

		double needFactor = 0;
		// amount of grey water/water needed is also based on % of growth
		if (phaseNum == 2)
		// if (phaseType == PhaseType.GERMINATION)
			needFactor = .1;
		else if (fractionalGrowingTimeCompleted < .1 )
			needFactor = .2;
		else if (fractionalGrowingTimeCompleted < .2 )
			needFactor = .25;
		else if (fractionalGrowingTimeCompleted < .3 )
			needFactor = .3;
		else if (phaseNum > 2 && phaseNum < length - 2)
			needFactor = fractionalGrowingTimeCompleted;
		
		// STEP 1 : COMPUTE THE EFFECTS OF THE SUNLIGHT AND ARTIFICIAL LIGHT
		double uPAR = computeLight(time);

		// STEP 2 : COMPUTE THE EFFECTS OF THE TEMPERATURE
		computeTemperature();

		// STEP 3 : COMPUTE THE EFFECTS OF THE WATER AND FERTIZILER
		computeWaterFertilizer(needFactor, maxPeriodHarvest, time);
		
		// STEP 4 : COMPUTE THE EFFECTS OF GASES (O2 and CO2 USAGE) 
		computeGases(uPAR, needFactor, maxPeriodHarvest, time);

		// TODO: add air pressure modifier in future

		// 2015-08-26 Tuned harvestModifier
		if (phaseNum > 2 && phaseNum < length - 2) {
			harvestModifier = .6 * harvestModifier + .4 * harvestModifier * memory[0];
		}
		else if (phaseNum == 2)
			harvestModifier = .8 * harvestModifier + .2 * harvestModifier * memory[0];

		harvestModifier = .25 * harvestModifier
				+ .15 * harvestModifier * memory[1]
				+ .15 * harvestModifier * memory[2]
				+ .15 * harvestModifier * memory[3]
				+ .15 * harvestModifier * memory[4]
				+ .15 * harvestModifier * memory[5];
		
		// TODO: research how the above 6 factors may affect crop growth for different crop categories

		return harvestModifier;
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
		while (i.hasNext()) totalGrowingTime += i.next().getGrowingTime();
		return totalGrowingTime / cropTypes.size();
	}

	public int getCurrentPhaseNum() {
		for (Entry<Integer, Phase> entry : phases.entrySet()) {
	        if (entry.getValue().getPhaseType() == phaseType) {
	        	return entry.getKey();
	        }
	    }
		return -1;
	}

	/**
	 * Gets the upper limit percentage of the phase
	 */
	public double getUpperPercent(int phase) {
		double result = 0;
		for (int i = 1; i < phase + 1; i++) {
			if (phases.get(i) != null)
				result = result + phases.get(i).getPercentGrowth();
		}
		return result;
	}

	public Map<Integer, Phase> getPhases() {
		return phases;
	}


	// 2016-10-12 reset cumulativeDailyPAR
	public void resetPAR() {
		cumulativeDailyPAR = 0;
	}

	
	public double getHealthCondition() {
		return healthCondition;
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {

		phaseType = null;
		cropCategoryType = null;
		
		cropAR = null;
		tissueAR = null;
		
		cropType = null;
		farm = null;
		inv = null;
		settlement = null;

		surface = null;
		marsClock = null;
		masterClock = null;
		phases = null;
		fmt = null;

	}
}
