/**
 * Mars Simulation Project
 * CropConfig.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function.farming;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Provides configuration information about greenhouse crops. Uses a DOM
 * document to get the information.
 */
public class CropConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	public static final double INCUBATION_PERIOD = 10D;

	// Element names
	private static final String OXYGEN_CONSUMPTION_RATE = "oxygen-consumption-rate";
	private static final String WATER_CONSUMPTION_RATE = "water-consumption-rate";
	private static final String CARBON_DIOXIDE_CONSUMPTION_RATE = "carbon-dioxide-consumption-rate";
	private static final String WATT_TO_PHOTON_CONVERSION_RATIO = "watt-to-photon-conversion-ratio";

	private static final String VALUE = "value";
	private static final String CROP_LIST = "crop-list";
	private static final String CROP = "crop";
	private static final String NAME = "name";
	private static final String GROWING_TIME = "growing-time";
	private static final String CROP_CATEGORY = "crop-category";
	private static final String LIFE_CYCLE = "life-cycle";
	// private static final String PPF = "ppf";
	// private static final String PHOTOPERIOD = "photoperiod";
	private static final String EDIBLE_BIOMASS = "edible-biomass";
	private static final String EDIBLE_WATER_CONTENT = "edible-water-content";
	private static final String INEDIBLE_BIOMASS = "inedible-biomass";
	private static final String DAILY_PAR = "daily-PAR";
	// private static final String HARVEST_INDEX = "harvest-index";

	/** The next crop ID. */
	private int cropID = 0;
	/** The conversion rate. */
	private double conversionRate = 0;
	/** The average crop growing time. */
	private double averageCropGrowingTime = -1;
	/** The average farming area needed per person. */
	private double farmingAreaNeededPerPerson = -1;
	
	/** The consumption rates for co2, o2, water. **/
	private double[] consumptionRates = new double[] { 0, 0, 0 };

	/** A list of crop type names. */
	private List<String> cropTypeNames;
	/** A list of crop type. */
	private List<CropType> cropTypes;
	/** The lookup table for crop type. */
	private Map<Integer, CropType> lookUpCropType = new HashMap<>();

	/** Lookup of crop phases **/
	private transient Map <CropCategoryType, List<Phase>> lookupPhases = new EnumMap<>(CropCategoryType.class);

	/**
	 * Constructor.
	 * 
	 * @param cropDoc the crop DOM document.
	 */
	public CropConfig(Document cropDoc) {

		buildPhases();	

		consumptionRates[0] = getValueAsDouble(cropDoc, CARBON_DIOXIDE_CONSUMPTION_RATE);
		consumptionRates[1] = getValueAsDouble(cropDoc, OXYGEN_CONSUMPTION_RATE);
		consumptionRates[2] = getValueAsDouble(cropDoc, WATER_CONSUMPTION_RATE);

		conversionRate = getValueAsDouble(cropDoc, WATT_TO_PHOTON_CONVERSION_RATIO);

		// Call to create lists and map
		parseCropTypes(cropDoc);
		getCropTypeNames();
	}

	/**
	 * Gets a list of crop types.
	 * 
	 * @return list of crop types
	 */
	public List<CropType> getCropTypes() {
		return cropTypes;
	}
	
	/**
	 * Parse the crops configured in the XML and make an internal representation
	 * @param rootDoc
	 */
	private synchronized void parseCropTypes(Document rootDoc) {
		if (cropTypes != null) {
			// just in case if another thread is being created
			return;
		}
			
		// Build the global list in a temp to avoid access before it is built
		List<CropType> newList = new ArrayList<CropType>();

		Element root = rootDoc.getRootElement();
		Element cropElement = root.getChild(CROP_LIST);
		List<Element> crops = cropElement.getChildren(CROP);

		for (Element crop : crops) {
			String name = "";
			// Get name.
			name = crop.getAttributeValue(NAME).toLowerCase();

			// Get growing time.
			String growingTimeStr = crop.getAttributeValue(GROWING_TIME);
			double growingTime = Double.parseDouble(growingTimeStr);

			// Get crop category
			String cropCategory = crop.getAttributeValue(CROP_CATEGORY);

			// Get crop category
			String lifeCycle = crop.getAttributeValue(LIFE_CYCLE);

			// Add checking against the crop category enum
			CropCategoryType cat = CropCategoryType.valueOf(cropCategory.toUpperCase());

			// Get ppf
			// String ppfStr = crop.getAttributeValue(PPF);
			// double ppf = Double.parseDouble(ppfStr);

			// Get photoperiod
			// String photoperiodStr = crop.getAttributeValue(PHOTOPERIOD);
			// double photoperiod = Double.parseDouble(photoperiodStr);

			// Get edibleBiomass
			String edibleBiomassStr = crop.getAttributeValue(EDIBLE_BIOMASS);
			double edibleBiomass = Double.parseDouble(edibleBiomassStr);

			// Get edible biomass water content [ from 0 to 1 ]
			String edibleWaterContentStr = crop.getAttributeValue(EDIBLE_WATER_CONTENT);
			double edibleWaterContent = Double.parseDouble(edibleWaterContentStr);

			// Get inedibleBiomass
			String inedibleBiomassStr = crop.getAttributeValue(INEDIBLE_BIOMASS);
			double inedibleBiomass = Double.parseDouble(inedibleBiomassStr);

			// Get daily PAR
			String dailyPARStr = crop.getAttributeValue(DAILY_PAR);
			double dailyPAR = Double.parseDouble(dailyPARStr);

			// Get harvestIndex
			// String harvestIndexStr = crop.getAttributeValue(HARVEST_INDEX);
			// double harvestIndex = Double.parseDouble(harvestIndexStr);

			// Set up the default growth phases of a crop
			List<Phase> phases = lookupPhases.get(cat);

//				int sum = 0;
//				for (int i : phases.keySet()) {
//					sum += phases.get(i).getPercentGrowth();
//				}
//				
//				System.out.println(cat + " : " + sum);
			
			CropType cropType = new CropType(name, growingTime * 1000D, cat, lifeCycle, edibleBiomass,
					edibleWaterContent, inedibleBiomass, dailyPAR, phases);

			cropType.setID(cropID);

			newList.add(cropType);

			lookUpCropType.put(cropID++, cropType);
		}
		

		// Assign the newList now built
		cropTypes = Collections.unmodifiableList(newList);
	}
	

	/**
	 * Build the hard-coded crop phases
	 * TODO - Should come from the crops.xml
	 */
	private void buildPhases() {
		for (CropCategoryType cat : CropCategoryType.values()) {
			List<Phase> phases = new ArrayList<>();
			switch (cat) {
			case BULBS:
				phases.add(new Phase(PhaseType.INCUBATION, INCUBATION_PERIOD, 0D));
				phases.add(new Phase(PhaseType.PLANTING, 0.5D, 1D));
				phases.add(new Phase(PhaseType.CLOVE_SPROUTING, 1D, 5D));
				phases.add(new Phase(PhaseType.POST_EMERGENCE, 1D, 15D));
				phases.add(new Phase(PhaseType.LEAFING, 1D, 29D));
				phases.add(new Phase(PhaseType.BULB_INITIATION, 1D, 35D));
				phases.add(new Phase(PhaseType.MATURATION, 1D, 10D));
				phases.add(new Phase(PhaseType.HARVESTING, 0.5, 5D));
				phases.add(new Phase(PhaseType.FINISHED, 0.5, 0));
				break;
				
			case CORMS:
				phases.add(new Phase(PhaseType.INCUBATION, INCUBATION_PERIOD, 0D));
				phases.add(new Phase(PhaseType.PLANTING, 0.5D, 1D));
				phases.add(new Phase(PhaseType.BUD_SPROUTING, 1D, 20D));
				phases.add(new Phase(PhaseType.VEGETATIVE_DEVELOPMENT, 1D, 20D));
				phases.add(new Phase(PhaseType.FLOWERING, 1D, 15D));
				phases.add(new Phase(PhaseType.REPLACEMENT_CORMS_DEVELOPMENT, 1D, 30D));
				phases.add(new Phase(PhaseType.ANTHESIS, 1D, 9D));
				phases.add(new Phase(PhaseType.HARVESTING, 0.5, 5D));
				phases.add(new Phase(PhaseType.FINISHED, 0.5, 0));
				break;
				
			case FRUITS:
				phases.add(new Phase(PhaseType.INCUBATION, INCUBATION_PERIOD, 0D));
				phases.add(new Phase(PhaseType.PLANTING, 0.5D, 1D));
				phases.add(new Phase(PhaseType.GERMINATION, 1D, 5D));
				phases.add(new Phase(PhaseType.VEGETATIVE_DEVELOPMENT, 1D, 30D));
				phases.add(new Phase(PhaseType.FLOWERING, 1D, 25D));
				phases.add(new Phase(PhaseType.FRUITING, 1D, 24D));
				phases.add(new Phase(PhaseType.MATURATION, 1D, 10D));
				phases.add(new Phase(PhaseType.HARVESTING, 0.5, 5D));
				phases.add(new Phase(PhaseType.FINISHED, 0.5, 0));
				break;
				
			case GRAINS:
				phases.add(new Phase(PhaseType.INCUBATION, INCUBATION_PERIOD, 0D));
				phases.add(new Phase(PhaseType.PLANTING, 0.5D, 1D));
				phases.add(new Phase(PhaseType.GERMINATION, 1D, 9D));
				phases.add(new Phase(PhaseType.TILLERING, 1D, 20D));
				phases.add(new Phase(PhaseType.STEM_ELONGATION, 1D, 15D));
				phases.add(new Phase(PhaseType.FLOWERING, 1D, 20D));
				phases.add(new Phase(PhaseType.MILK_DEVELOPMENT, 1D, 10D));
				phases.add(new Phase(PhaseType.DOUGH_DEVELOPING, 1D, 10D));
				phases.add(new Phase(PhaseType.MATURATION, 1D, 10D));
				phases.add(new Phase(PhaseType.HARVESTING, 0.5, 5D));
				phases.add(new Phase(PhaseType.FINISHED, 0.5, 0));
				break;
				
			case GRASSES:
				phases.add(new Phase(PhaseType.INCUBATION, INCUBATION_PERIOD, 0D));
				phases.add(new Phase(PhaseType.PLANTING, 0.5D, 1D));
				phases.add(new Phase(PhaseType.GERMINATION, 1D, 5D));
				phases.add(new Phase(PhaseType.TILLERING, 1D, 39D));
				phases.add(new Phase(PhaseType.GRAND_GROWTH, 1D, 40D));
				phases.add(new Phase(PhaseType.MATURATION, 1D, 10D));
				phases.add(new Phase(PhaseType.HARVESTING, 0.5, 5D));
				phases.add(new Phase(PhaseType.FINISHED, 0.5, 0));
				break;
				
			case LEAVES:
				phases.add(new Phase(PhaseType.INCUBATION, INCUBATION_PERIOD, 0D));
				phases.add(new Phase(PhaseType.PLANTING, 0.5D, 1D));
				phases.add(new Phase(PhaseType.GERMINATION, 1D, 5D));
				phases.add(new Phase(PhaseType.POST_EMERGENCE, 1D, 5D));
				phases.add(new Phase(PhaseType.HEAD_DEVELOPMENT, 1D, 39D));
				phases.add(new Phase(PhaseType.FIFTY_PERCENT_HEAD_SIZE_REACHED, 1D, 35D));
				phases.add(new Phase(PhaseType.MATURATION, 1D, 10D));
				phases.add(new Phase(PhaseType.HARVESTING, 0.5, 5D));
				phases.add(new Phase(PhaseType.FINISHED, 0.5, 0));
				break;
				
			case LEGUMES:
				phases.add(new Phase(PhaseType.INCUBATION, INCUBATION_PERIOD, 0D));
				phases.add(new Phase(PhaseType.PLANTING, 0.5D, 1D));
				phases.add(new Phase(PhaseType.GERMINATION, 1D, 5D));
				phases.add(new Phase(PhaseType.LEAFING, 1D, 30D));
				phases.add(new Phase(PhaseType.FLOWERING, 1D, 25D));
				phases.add(new Phase(PhaseType.SEED_FILL, 1D, 20D));
				phases.add(new Phase(PhaseType.POD_MATURING, 1D, 14D));
				phases.add(new Phase(PhaseType.HARVESTING, 0.5, 5D));
				phases.add(new Phase(PhaseType.FINISHED, 0.5, 0));
				break;
				
			case TUBERS:
				phases.add(new Phase(PhaseType.INCUBATION, INCUBATION_PERIOD, 0D));
				phases.add(new Phase(PhaseType.PLANTING, 0.5D, 1D));
				phases.add(new Phase(PhaseType.SPROUTING, 1D, 10D));
				phases.add(new Phase(PhaseType.LEAF_DEVELOPMENT, 1D, 15D));
				phases.add(new Phase(PhaseType.TUBER_INITIATION, 1D, 20D));
				phases.add(new Phase(PhaseType.TUBER_FILLING, 1D, 35D));
				phases.add(new Phase(PhaseType.MATURATION, 1D, 14D));
				phases.add(new Phase(PhaseType.HARVESTING, 0.5, 5D));
				phases.add(new Phase(PhaseType.FINISHED, 0.5, 0));
				break;
				
			case ROOTS:
				// roots : carrot, radish, ginger
				phases.add(new Phase(PhaseType.INCUBATION, INCUBATION_PERIOD, 0D));
				phases.add(new Phase(PhaseType.PLANTING, 0.5, 1D));
				phases.add(new Phase(PhaseType.SPROUTING, 1D, 5D));
				phases.add(new Phase(PhaseType.LEAF_DEVELOPMENT, 1D, 39D));
				phases.add(new Phase(PhaseType.ROOT_DEVELOPMENT, 1D, 40D));
				phases.add(new Phase(PhaseType.MATURATION, 1D, 10D));
				phases.add(new Phase(PhaseType.HARVESTING, 0.5, 5D));
				phases.add(new Phase(PhaseType.FINISHED, 0.5, 0));
				break;
				
			case STEMS:
				// Stems e.g celery
				phases.add(new Phase(PhaseType.INCUBATION, INCUBATION_PERIOD, 0D));
				phases.add(new Phase(PhaseType.PLANTING, 0.5, 1D));
				phases.add(new Phase(PhaseType.EARLY_VEGETATIVE, 1D, 15D));
				phases.add(new Phase(PhaseType.MID_VEGETATIVE, 1D, 15D));
				phases.add(new Phase(PhaseType.STEM_ELONGATION, 1D, 34D));
				phases.add(new Phase(PhaseType.EARLY_BULKING_UP, 1D, 10D));
				phases.add(new Phase(PhaseType.MID_BULKING_UP, 1D, 10D));
				phases.add(new Phase(PhaseType.LATE_BULKING_UP, 1D, 10D));
				phases.add(new Phase(PhaseType.HARVESTING, 0.5, 5D));
				phases.add(new Phase(PhaseType.FINISHED, 0.5, 0));
				break;

//			case SPICES:
//				// spices e.g. green onion 
//				phases.add(new Phase(PhaseType.INCUBATION, INCUBATION_PERIOD, 0D));
//				phases.add(new Phase(PhaseType.PLANTING, 0.5, 1D));
//				phases.add(new Phase(PhaseType.GERMINATION, 1D, 10D));
//				phases.add(new Phase(PhaseType.LEAFING, 1D, 55D));
//				phases.add(new Phase(PhaseType.MATURATION, 1D, 33D));
//				phases.add(new Phase(PhaseType.HARVESTING, 0.5, 1D));
//				phases.add(new Phase(PhaseType.FINISHED, 0.5, 0));
//				break;
				
			default:
				phases.add(new Phase(PhaseType.INCUBATION, INCUBATION_PERIOD, 0D));
				phases.add(new Phase(PhaseType.PLANTING, 0.5, 1D));
				phases.add(new Phase(PhaseType.GERMINATION, 1D, 5D));
				phases.add(new Phase(PhaseType.GROWING, 1D, 79D));
				phases.add(new Phase(PhaseType.MATURATION, 1D, 10D));
				phases.add(new Phase(PhaseType.HARVESTING, 0.5, 5D));
				phases.add(new Phase(PhaseType.FINISHED, 0.5, 0));
				break;
			}
			
			// Add as an immutable map
			lookupPhases.put(cat, Collections.unmodifiableList(phases));
		}
	}

	/**
	 * Gets the carbon doxide consumption rate.
	 * 
	 * @return carbon doxide rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getCarbonDioxideConsumptionRate() {
		return consumptionRates[0];
	}

	/**
	 * Gets the watt to photon energy conversion ratio
	 * 
	 * @return ratio [in u mol / m2 /s / W m-2]
	 * @throws Exception if the ratio could not be found.
	 */
	public double getWattToPhotonConversionRatio() {
		return conversionRate;
	}

	/**
	 * Gets the oxygen consumption rate.
	 * 
	 * @return oxygen rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getOxygenConsumptionRate() {
		return consumptionRates[1];
	}

	/**
	 * Gets the water consumption rate.
	 * 
	 * @return water rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getWaterConsumptionRate() {
		return consumptionRates[2];
	}

	/**
	 * Gets the value of an element as a double
	 * 
	 * @param an element
	 * @return a double
	 */
	private double getValueAsDouble(Document cropDoc, String child) {
		Element root = cropDoc.getRootElement();
		Element element = root.getChild(child);
		String str = element.getAttributeValue(VALUE);
		return Double.parseDouble(str);
	}

	/**
	 * Gets the total number of crop types
	 * 
	 * @return
	 */
	public int getNumCropTypes() {
		return cropTypes.size();
	}

	/**
	 * Gets the crop type
	 * 
	 * @param id
	 * @return
	 */
	public CropType getCropTypeByID(int id) {
		return lookUpCropType.get(id);
	}

	/**
	 * Returns the crop type instance by its name
	 * 
	 * @param name
	 * @return
	 */
	public CropType getCropTypeByName(String name) {
		CropType c = null;
		for (CropType ct : lookUpCropType.values()) {
			String n = ct.getName();
			if (n.equalsIgnoreCase(name)) {
				return ct;
			}
		}
		
		return c;
	}
	
	/**
	 * Gets a list of crop type names
	 * 
	 * @return
	 */
	public List<String> getCropTypeNames() {
		if  (cropTypeNames == null) {
			cropTypeNames = new ArrayList<>();
			for (CropType ct : cropTypes) {
				cropTypeNames.add(ct.getName());
			}
			
			Collections.sort(cropTypeNames);
		}
		return cropTypeNames;
	}
	
	/**
	 * Picks a crop type randomly
	 * 
	 * @return crop type
	 */
	public CropType getRandomCropType() {
		return cropTypes.get(RandomUtil.getRandomInt(cropTypes.size() - 1));
	}
	
	/**
	 * Picks a crop type randomly
	 * 
	 * @return crop type
	 */
	public String getCropTypeNameByID(int id) {
		return getCropTypeByID(id).getName();
	}
	
	public CropCategoryType getCropCategoryType(int id) {
		return getCropTypeByID(id).getCropCategoryType();
	}
	
	/**
	 * Gets the average growing time for all crops.
	 * 
	 * @return average growing time (millisols)
	 */
	public double getAverageCropGrowingTime() {
		if (averageCropGrowingTime == -1) {
			double totalGrowingTime = 0D;
			for (CropType ct : cropTypes) {
				totalGrowingTime += ct.getGrowingTime(); 
			}
			averageCropGrowingTime = totalGrowingTime / cropTypes.size();
		}
		return averageCropGrowingTime;
	}
	
	/**
	 * Gets the average area (m^2) of farming surface required to sustain one
	 * person.
	 * 
	 * @return area (m^2) of farming surface.
	 */
	public double getFarmingAreaNeededPerPerson() {
		if (farmingAreaNeededPerPerson < 0) {
			// Determine average amount (kg) of food required per person per orbit.
			double neededFoodPerSol = SimulationConfig.instance().getPersonConfig().getFoodConsumptionRate();
	
			// Determine average amount (kg) of food produced per farm area (m^2).
			// CropConfig cropConfig = SimulationConfig.instance().getCropConfiguration();
			double totalFoodPerSolPerArea = 0D;
			for (CropType c : cropTypes)
				// Crop type average edible biomass (kg) per Sol.
				totalFoodPerSolPerArea += c.getEdibleBiomass() / 1000D;
	
			double producedFoodPerSolPerArea = totalFoodPerSolPerArea / cropTypes.size();
	
			if (producedFoodPerSolPerArea > 0) {
				farmingAreaNeededPerPerson = neededFoodPerSol / producedFoodPerSolPerArea;
			}
			else {
				// Must be no crops defined !!
				farmingAreaNeededPerPerson = 0;
			}
		}
		
		return farmingAreaNeededPerPerson;
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		if (cropTypes != null) {
			cropTypes = null;
		}
		consumptionRates = null;
		cropTypeNames = null;
		cropTypes = null;
		lookUpCropType = null;
		lookupPhases = null;
	}
}
