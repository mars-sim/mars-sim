/**
 * Mars Simulation Project
 * CropConfig.java
 * @version 3.1.0 2017-03-31
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function.farming;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
	private static int cropID;
	/** The total # of crop types. */
	private static int numCropTypes;
	/** The conversion rate. */
	private static double conversionRate = 0;
	/** The average crop growing time. */
	private static double averageCropGrowingTime = -1;
	/** The average farming area needed per person. */
	private static double farmingAreaNeededPerPerson = 0;
	
	/** The consumption rates for co2, o2, water. **/
	private static double[] consumptionRates = new double[] { 0, 0, 0 };

	/** A list of crop type names. */
	private static List<String> cropTypeNames;
	/** A list of crop type. */
	private static List<CropType> cropTypes;
	/** A list of crop category type. */
	private static List<CropCategoryType> cropCategoryTypes = new ArrayList<CropCategoryType>(
			Arrays.asList(CropCategoryType.values()));
	/** The lookup table for crop type. */
	private static Map<Integer, CropType> lookUpCropType = new HashMap<>();

	/** The crop document. */
	private static Document cropDoc;

	/**
	 * Constructor.
	 * 
	 * @param cropDoc the crop DOM document.
	 */
	public CropConfig(Document cropDoc) {
		CropConfig.cropDoc = cropDoc;

		// Call to create lists and map
		getCropTypes();
		getCropTypeNames();
	}

	/**
	 * Gets a list of crop types.
	 * 
	 * @return list of crop types
	 * @throws Exception when crops could not be parsed.
	 */
	public static List<CropType> getCropTypes() {

		if (cropTypes == null) {
			// first time loading the list from crops.xml
			cropTypes = new ArrayList<CropType>();

			Element root = cropDoc.getRootElement();
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
				boolean known = false;
				CropCategoryType cat = null;
				// check to see if this crop category is recognized in mars-sim
				for (CropCategoryType c : cropCategoryTypes) {
					if (CropCategoryType.getType(cropCategory) == c) {
						known = true;
						cat = c;
						// System.out.println("cat is "+ cat);
					}
				}

				if (!known)
					throw new IllegalArgumentException("no such crop category : " + cropCategory);

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

				// 2015-04-08 Added daily PAR
				String dailyPARStr = crop.getAttributeValue(DAILY_PAR);
				double dailyPAR = Double.parseDouble(dailyPARStr);

				// Get harvestIndex
				// String harvestIndexStr = crop.getAttributeValue(HARVEST_INDEX);
				// double harvestIndex = Double.parseDouble(harvestIndexStr);

				// Set up the default growth phases of a crop
				Map<Integer, Phase> phases = setupPhases(cat);

//				int sum = 0;
//				for (int i : phases.keySet()) {
//					sum += phases.get(i).getPercentGrowth();
//				}
//				
//				System.out.println(cat + " : " + sum);
				
				CropType cropType = new CropType(name, growingTime * 1000D, cat, lifeCycle, edibleBiomass,
						edibleWaterContent, inedibleBiomass, dailyPAR, phases);

				cropType.setID(cropID);

				cropTypes.add(cropType);

				lookUpCropType.put(cropID++, cropType);
			}
		}
		
		return cropTypes;
	}

	/**
	 * Sets up phenological stages of a crop type
	 * 
	 * @param cat
	 * @return phase map
	 */
	public static Map<Integer, Phase> setupPhases(CropCategoryType cat) {
		// Set up the default growth phases of a crop
		Map<Integer, Phase> phases = new HashMap<>();

		if (cat == CropCategoryType.BULBS) {

			phases.put(0, new Phase(PhaseType.INCUBATION, INCUBATION_PERIOD, 0D));
			phases.put(1, new Phase(PhaseType.PLANTING, 0.5D, 1D));
			phases.put(2, new Phase(PhaseType.CLOVE_SPROUTING, 1D, 5D));
			phases.put(3, new Phase(PhaseType.POST_EMERGENCE, 1D, 15D));
			phases.put(4, new Phase(PhaseType.LEAFING, 1D, 29D));
			phases.put(5, new Phase(PhaseType.BULB_INITIATION, 1D, 35D));
			phases.put(6, new Phase(PhaseType.MATURATION, 1D, 10D));
			phases.put(7, new Phase(PhaseType.HARVESTING, 0.5, 5D));
			phases.put(8, new Phase(PhaseType.FINISHED, 0.5, 0));

		} else if (cat == CropCategoryType.CORMS) {

			phases.put(0, new Phase(PhaseType.INCUBATION, INCUBATION_PERIOD, 0D));
			phases.put(1, new Phase(PhaseType.PLANTING, 0.5D, 1D));
			phases.put(2, new Phase(PhaseType.BUD_SPROUTING, 1D, 20D));
			phases.put(3, new Phase(PhaseType.VEGETATIVE_DEVELOPMENT, 1D, 20D));
			phases.put(4, new Phase(PhaseType.FLOWERING, 1D, 15D));
			phases.put(5, new Phase(PhaseType.REPLACEMENT_CORMS_DEVELOPMENT, 1D, 30D));
			phases.put(6, new Phase(PhaseType.ANTHESIS, 1D, 9D));
			phases.put(7, new Phase(PhaseType.HARVESTING, 0.5, 5D));
			phases.put(8, new Phase(PhaseType.FINISHED, 0.5, 0));

		} else if (cat == CropCategoryType.FRUITS) {

			phases.put(0, new Phase(PhaseType.INCUBATION, INCUBATION_PERIOD, 0D));
			phases.put(1, new Phase(PhaseType.PLANTING, 0.5D, 1D));
			phases.put(2, new Phase(PhaseType.GERMINATION, 1D, 5D));
			phases.put(3, new Phase(PhaseType.VEGETATIVE_DEVELOPMENT, 1D, 30D));
			phases.put(4, new Phase(PhaseType.FLOWERING, 1D, 25D));
			phases.put(5, new Phase(PhaseType.FRUITING, 1D, 24D));
			phases.put(6, new Phase(PhaseType.MATURATION, 1D, 10D));
			phases.put(7, new Phase(PhaseType.HARVESTING, 0.5, 5D));
			phases.put(8, new Phase(PhaseType.FINISHED, 0.5, 0));

		} else if (cat == CropCategoryType.GRAINS) {

			phases.put(0, new Phase(PhaseType.INCUBATION, INCUBATION_PERIOD, 0D));
			phases.put(1, new Phase(PhaseType.PLANTING, 0.5D, 1D));
			phases.put(2, new Phase(PhaseType.GERMINATION, 1D, 9D));
			phases.put(3, new Phase(PhaseType.TILLERING, 1D, 20D));
			phases.put(4, new Phase(PhaseType.STEM_ELONGATION, 1D, 15D));
			phases.put(5, new Phase(PhaseType.FLOWERING, 1D, 20D));
			phases.put(6, new Phase(PhaseType.MILK_DEVELOPMENT, 1D, 10D));
			phases.put(7, new Phase(PhaseType.DOUGH_DEVELOPING, 1D, 10D));
			phases.put(8, new Phase(PhaseType.MATURATION, 1D, 10D));
			phases.put(9, new Phase(PhaseType.HARVESTING, 0.5, 5D));
			phases.put(10, new Phase(PhaseType.FINISHED, 0.5, 0));

		} else if (cat == CropCategoryType.GRASSES) {

			phases.put(0, new Phase(PhaseType.INCUBATION, INCUBATION_PERIOD, 0D));
			phases.put(1, new Phase(PhaseType.PLANTING, 0.5D, 1D));
			phases.put(2, new Phase(PhaseType.GERMINATION, 1D, 5D));
			phases.put(3, new Phase(PhaseType.TILLERING, 1D, 39D));
			phases.put(4, new Phase(PhaseType.GRAND_GROWTH, 1D, 40D));
			phases.put(5, new Phase(PhaseType.MATURATION, 1D, 10D));
			phases.put(6, new Phase(PhaseType.HARVESTING, 0.5, 5D));
			phases.put(7, new Phase(PhaseType.FINISHED, 0.5, 0));

		} else if (cat == CropCategoryType.LEAVES) {

			phases.put(0, new Phase(PhaseType.INCUBATION, INCUBATION_PERIOD, 0D));
			phases.put(1, new Phase(PhaseType.PLANTING, 0.5D, 1D));
			phases.put(2, new Phase(PhaseType.GERMINATION, 1D, 5D));
			phases.put(3, new Phase(PhaseType.POST_EMERGENCE, 1D, 5D));
			phases.put(4, new Phase(PhaseType.HEAD_DEVELOPMENT, 1D, 39D));
			phases.put(5, new Phase(PhaseType.FIFTY_PERCENT_HEAD_SIZE_REACHED, 1D, 35D));
			phases.put(6, new Phase(PhaseType.MATURATION, 1D, 10D));
			phases.put(7, new Phase(PhaseType.HARVESTING, 0.5, 5D));
			phases.put(8, new Phase(PhaseType.FINISHED, 0.5, 0));

		} else if (cat == CropCategoryType.LEGUMES) {

			phases.put(0, new Phase(PhaseType.INCUBATION, INCUBATION_PERIOD, 0D));
			phases.put(1, new Phase(PhaseType.PLANTING, 0.5D, 1D));
			phases.put(2, new Phase(PhaseType.GERMINATION, 1D, 5D));
			phases.put(3, new Phase(PhaseType.LEAFING, 1D, 30D));
			phases.put(4, new Phase(PhaseType.FLOWERING, 1D, 25D));
			phases.put(5, new Phase(PhaseType.SEED_FILL, 1D, 20D));
			phases.put(6, new Phase(PhaseType.POD_MATURING, 1D, 14D));
			phases.put(7, new Phase(PhaseType.HARVESTING, 0.5, 5D));
			phases.put(8, new Phase(PhaseType.FINISHED, 0.5, 0));

		} else if (cat == CropCategoryType.TUBERS) {

			phases.put(0, new Phase(PhaseType.INCUBATION, INCUBATION_PERIOD, 0D));
			phases.put(1, new Phase(PhaseType.PLANTING, 0.5D, 1D));
			phases.put(2, new Phase(PhaseType.SPROUTING, 1D, 10D));
			phases.put(3, new Phase(PhaseType.LEAF_DEVELOPMENT, 1D, 15D));
			phases.put(4, new Phase(PhaseType.TUBER_INITIATION, 1D, 20D));
			phases.put(5, new Phase(PhaseType.TUBER_FILLING, 1D, 35D));
			phases.put(6, new Phase(PhaseType.MATURATION, 1D, 14D));
			phases.put(7, new Phase(PhaseType.HARVESTING, 0.5, 5D));
			phases.put(8, new Phase(PhaseType.FINISHED, 0.5, 0));

		} else if (cat == CropCategoryType.ROOTS) {

			// roots : carrot, radish, ginger
			phases.put(0, new Phase(PhaseType.INCUBATION, INCUBATION_PERIOD, 0D));
			phases.put(1, new Phase(PhaseType.PLANTING, 0.5, 1D));
			phases.put(2, new Phase(PhaseType.SPROUTING, 1D, 5D));
			phases.put(3, new Phase(PhaseType.LEAF_DEVELOPMENT, 1D, 39D));
			phases.put(4, new Phase(PhaseType.ROOT_DEVELOPMENT, 1D, 40D));
			phases.put(5, new Phase(PhaseType.MATURATION, 1D, 10D));
			phases.put(6, new Phase(PhaseType.HARVESTING, 0.5, 5D));
			phases.put(7, new Phase(PhaseType.FINISHED, 0.5, 0));

		} else if (cat == CropCategoryType.STEMS) {

			// Stems e.g celery
			phases.put(0, new Phase(PhaseType.INCUBATION, INCUBATION_PERIOD, 0D));
			phases.put(1, new Phase(PhaseType.PLANTING, 0.5, 1D));
			phases.put(2, new Phase(PhaseType.EARLY_VEGETATIVE, 1D, 15D));
			phases.put(3, new Phase(PhaseType.MID_VEGETATIVE, 1D, 15D));
			phases.put(4, new Phase(PhaseType.STEM_ELONGATION, 1D, 34D));
			phases.put(5, new Phase(PhaseType.EARLY_BULKING_UP, 1D, 10D));
			phases.put(6, new Phase(PhaseType.MID_BULKING_UP, 1D, 10D));
			phases.put(7, new Phase(PhaseType.LATE_BULKING_UP, 1D, 10D));
			phases.put(8, new Phase(PhaseType.HARVESTING, 0.5, 5D));
			phases.put(9, new Phase(PhaseType.FINISHED, 0.5, 0));

//		} else if (cat == CropCategoryType.SPICES) {
//			
//			// spices e.g. green onion 
//			phases.put(0, new Phase(PhaseType.INCUBATION, INCUBATION_PERIOD, 0D));
//			phases.put(1, new Phase(PhaseType.PLANTING, 0.5, 1D));
//			phases.put(2, new Phase(PhaseType.GERMINATION, 1D, 10D));
//			phases.put(3, new Phase(PhaseType.LEAFING, 1D, 55D));
//			phases.put(4, new Phase(PhaseType.MATURATION, 1D, 33D));
//			phases.put(5, new Phase(PhaseType.HARVESTING, 0.5, 1D));
//			phases.put(6, new Phase(PhaseType.FINISHED, 0.5, 0));

		} else {

			phases.put(0, new Phase(PhaseType.INCUBATION, INCUBATION_PERIOD, 0D));
			phases.put(1, new Phase(PhaseType.PLANTING, 0.5, 1D));
			phases.put(2, new Phase(PhaseType.GERMINATION, 1D, 5D));
			phases.put(3, new Phase(PhaseType.GROWING, 1D, 79D));
			phases.put(4, new Phase(PhaseType.MATURATION, 1D, 10D));
			phases.put(5, new Phase(PhaseType.HARVESTING, 0.5, 5D));
			phases.put(6, new Phase(PhaseType.FINISHED, 0.5, 0));

		}

		return phases;

	}

	/**
	 * Gets the carbon doxide consumption rate.
	 * 
	 * @return carbon doxide rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getCarbonDioxideConsumptionRate() {
		if (consumptionRates[0] != 0)
			return consumptionRates[0];
		else {
			consumptionRates[0] = getValueAsDouble(CARBON_DIOXIDE_CONSUMPTION_RATE);
			return consumptionRates[0];
		}
	}

	/**
	 * Gets the watt to photon energy conversion ratio
	 * 
	 * @return ratio [in u mol / m2 /s / W m-2]
	 * @throws Exception if the ratio could not be found.
	 */
	public double getWattToPhotonConversionRatio() {
		if (conversionRate != 0)
			return conversionRate;
		else {
			conversionRate = getValueAsDouble(WATT_TO_PHOTON_CONVERSION_RATIO);
			return conversionRate;
		}
	}

	/**
	 * Gets the oxygen consumption rate.
	 * 
	 * @return oxygen rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getOxygenConsumptionRate() {
		if (consumptionRates[1] != 0)
			return consumptionRates[1];
		else {
			consumptionRates[1] = getValueAsDouble(OXYGEN_CONSUMPTION_RATE);
			return consumptionRates[1];
		}
	}

	/**
	 * Gets the water consumption rate.
	 * 
	 * @return water rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getWaterConsumptionRate() {
		if (consumptionRates[2] != 0)
			return consumptionRates[2];
		else {
			consumptionRates[2] = getValueAsDouble(WATER_CONSUMPTION_RATE);
			return consumptionRates[2];
		}
	}

	/**
	 * Gets the value of an element as a double
	 * 
	 * @param an element
	 * @return a double
	 */
	private double getValueAsDouble(String child) {
		Element root = cropDoc.getRootElement();
		Element element = root.getChild(child);
		String str = element.getAttributeValue(VALUE);
		return Double.parseDouble(str);
	}

//	public Map<Integer, Phase> getPhases() {
//	try {
//		return shallowCopy(phases);
//	} catch (Exception e) {
//		e.printStackTrace();
//	}
//	return phases;
//}
//
//
//static final Map shallowCopy(final Map source) throws Exception {
//    final Map newMap = source.getClass().newInstance();
//    newMap.putAll(source);
//    return newMap;
//}

	/**
	 * Gets a list of crop category types
	 * 
	 * @return
	 */
	public static List<CropCategoryType> getCropCategoryTypes() {
		return cropCategoryTypes;
	}

	/**
	 * Gets the total number of crop types
	 * 
	 * @return
	 */
	public static int getNumCropTypes() {
		if (numCropTypes == 0)
			numCropTypes = getCropTypes().size();
		return numCropTypes;
	}

	/**
	 * Gets the crop type
	 * 
	 * @param id
	 * @return
	 */
	public static CropType getCropTypeByID(int id) {
		return lookUpCropType.get(id);
	}
	
	/**
	 * Returns the id by the crop type's name
	 * 
	 * @param name
	 * @return
	 */
	public static int getIDByName(String name) {
		int id = -1;
		for (CropType ct : lookUpCropType.values()) {
			String n = ct.getName();
			if (n.equalsIgnoreCase(name)) {
				return ct.getID();
			}
		}
		
		return id;
	}

	/**
	 * Returns the crop type instance by its name
	 * 
	 * @param name
	 * @return
	 */
	public static CropType getCropTypeByName(String name) {
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
	public static List<String> getCropTypeNames() {
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
	public static CropType getRandomCropType() {
		return getCropTypes().get(RandomUtil.getRandomInt(getNumCropTypes() - 1));
	}
	
	/**
	 * Picks a crop type randomly
	 * 
	 * @return crop type
	 */
	public static String getCropTypeNameByID(int id) {
		return getCropTypeByID(id).getName();
	}
	
	public static CropCategoryType getCropCategoryType(int id) {
		return getCropTypeByID(id).getCropCategoryType();
	}
	
	/**
	 * Gets the average growing time for all crops.
	 * 
	 * @return average growing time (millisols)
	 */
	public static double getAverageCropGrowingTime() {
		if (averageCropGrowingTime == -1) {
			double totalGrowingTime = 0D;
			for (CropType ct : CropConfig.getCropTypes()) {
				totalGrowingTime += ct.getGrowingTime(); 
			}
			averageCropGrowingTime = totalGrowingTime / CropConfig.getNumCropTypes();
		}
		return averageCropGrowingTime;
	}
	
	/**
	 * Gets the average area (m^2) of farming surface required to sustain one
	 * person.
	 * 
	 * @return area (m^2) of farming surface.
	 */
	public static double getFarmingAreaNeededPerPerson() {
		if (farmingAreaNeededPerPerson <= 0) {
			// Determine average amount (kg) of food required per person per orbit.
			double neededFoodPerSol = SimulationConfig.instance().getPersonConfig().getFoodConsumptionRate();
	
			// Determine average amount (kg) of food produced per farm area (m^2).
			// CropConfig cropConfig = SimulationConfig.instance().getCropConfiguration();
			double totalFoodPerSolPerArea = 0D;
			for (CropType c : getCropTypes())
				// Crop type average edible biomass (kg) per Sol.
				totalFoodPerSolPerArea += c.getEdibleBiomass() / 1000D;
	
			double producedFoodPerSolPerArea = totalFoodPerSolPerArea / getNumCropTypes();
	
			farmingAreaNeededPerPerson = neededFoodPerSol / producedFoodPerSolPerArea;
		}
		
		return farmingAreaNeededPerPerson;
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		cropDoc = null;
		if (cropTypes != null) {
			cropTypes.clear();
			cropTypes = null;
		}
	}
}
