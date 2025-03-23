/*
 * Mars Simulation Project
 * CropConfig.java
 * @date 2023-05-06
 * @author Scott Davis
 */
package com.mars_sim.core.building.function.farming;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.person.PersonConfig;
import com.mars_sim.core.tool.RandomUtil;

/**
 * Provides configuration information about greenhouse crops. Uses a DOM
 * document to get the information.
 */
public class CropConfig {

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
	private static final String GROWING_SOLS = "growing-sols";
	private static final String CROP_CATEGORY = "crop-category";
	private static final String EDIBLE_BIOMASS = "edible-biomass";
	private static final String EDIBLE_WATER_CONTENT = "edible-water-content";
	private static final String INEDIBLE_BIOMASS = "inedible-biomass";
	private static final String DAILY_PAR = "daily-PAR";
	private static final String SEED_NAME = "seed-name";
	private static final String CATEGORY_LIST = "category-list";
	private static final String CATEGORY = "category";
	private static final String DESCRIPTION = "description";
	private static final String PHASE = "phase";
	private static final String WORK_REQUIRED = "work-required";
	private static final String GROWTH_PERC = "growth-perc";


	/** The conversion rate. */
	private double conversionRate = 0;
	/** The average crop growing time. */
	private double averageCropGrowingTime = -1;
	/** The average farming area needed per person. */
	private double farmingAreaNeededPerPerson = -1;
	
	/** The consumption rates for co2, o2, water. **/
	private double[] consumptionRates = new double[] { 0, 0, 0 };

	/** A list of crop specs. */
	private List<CropCategory> cropCategories;
	private List<CropSpec> cropSpecs;
	private Map<String, CropSpec> lookUpCropSpecMap = new HashMap<>();

	private PersonConfig personConfig;
	
	/**
	 * Constructor.
	 * 
	 * @param cropDoc the crop DOM document.
	 */
	public CropConfig(Document cropDoc, PersonConfig personConfig) {
		this.personConfig = personConfig;
		
		consumptionRates[0] = getValueAsDouble(cropDoc, CARBON_DIOXIDE_CONSUMPTION_RATE);
		consumptionRates[1] = getValueAsDouble(cropDoc, OXYGEN_CONSUMPTION_RATE);
		consumptionRates[2] = getValueAsDouble(cropDoc, WATER_CONSUMPTION_RATE);

		conversionRate = getValueAsDouble(cropDoc, WATT_TO_PHOTON_CONVERSION_RATIO);

		// Call to create lists and map
		var cats = parseCropCategories(cropDoc);
		cropCategories = new ArrayList<>(cats.values());
		Collections.sort(cropCategories, (c1, c2) -> c1.getName().compareTo(c2.getName()));
		parseCropTypes(cropDoc, cats);
	}

	/**
	 * The supproted categories of Crop
	 * @return
	 */
	public List<CropCategory> getCropCategories() {
		return cropCategories;
	}

	/**
	 * Gets a list of crop types.
	 * 
	 * @return list of crop types
	 */
	public List<CropSpec> getCropTypes() {
		return cropSpecs;
	}
	
	/**
	 * Parse the CropCategories
	 * @param rootDoc
	 */
	private Map<String,CropCategory> parseCropCategories(Document rootDoc) {
		Map<String,CropCategory> catByName = new HashMap<>();
		Element root = rootDoc.getRootElement();
		Element cropElement = root.getChild(CATEGORY_LIST);
		List<Element> categories = cropElement.getChildren(CATEGORY);
		for (Element c : categories) {
			String name = c.getAttributeValue(NAME);
			String description = c.getAttributeValue(DESCRIPTION);
			boolean needsLight = ConfigHelper.getOptionalAttributeBool(c, "needs-light", true);

			List<Phase> phases = new ArrayList<>();
			List<Element> phaseNodes = c.getChildren(PHASE);
			double cummulativeGrowth = 0;
			for(var p : phaseNodes) {
				String phaseName = p.getAttributeValue(NAME);
				double phaseDuration = ConfigHelper.getAttributeDouble(p, WORK_REQUIRED);
				double growthPerc = ConfigHelper.getAttributeDouble(p, GROWTH_PERC);
				cummulativeGrowth += growthPerc;
				phases.add(new Phase(phaseName, phaseDuration, growthPerc, cummulativeGrowth));
			}
 
			catByName.put(name.toLowerCase(), new CropCategory(name, description, needsLight, phases));
		}
		return catByName;
	}

	/**
	 * Parses the crops configured in the XML and make an internal representation.
	 * 
	 * @param rootDoc
	 * @param cropCategories Configured categories
	 */
	private void parseCropTypes(Document rootDoc, Map<String,CropCategory> cats) {

		Element root = rootDoc.getRootElement();
		Element cropElement = root.getChild(CROP_LIST);
		List<Element> crops = cropElement.getChildren(CROP);

		for (Element crop : crops) {
			String name = crop.getAttributeValue(NAME);
			int growingTime = ConfigHelper.getAttributeInt(crop, GROWING_SOLS);
			double edibleBiomass = ConfigHelper.getAttributeDouble(crop, EDIBLE_BIOMASS);
			double edibleWaterContent = ConfigHelper.getAttributeDouble(crop, EDIBLE_WATER_CONTENT);
			double inedibleBiomass = ConfigHelper.getAttributeDouble(crop, INEDIBLE_BIOMASS);
			double dailyPAR = ConfigHelper.getAttributeDouble(crop, DAILY_PAR);

			String cropCategory = crop.getAttributeValue(CROP_CATEGORY);
			CropCategory cat = cats.get(cropCategory.toLowerCase());

			// Get Seed values
			String seedName = crop.getAttributeValue(SEED_NAME);
	
			CropSpec spec = new CropSpec(name, growingTime,
									cat, edibleBiomass,
									edibleWaterContent, inedibleBiomass,
									dailyPAR, seedName);

			lookUpCropSpecMap.put(name.toLowerCase(), spec);
		}

		cropSpecs = new ArrayList<>(lookUpCropSpecMap.values());
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
	private static double getValueAsDouble(Document cropDoc, String child) {
		Element root = cropDoc.getRootElement();
		Element element = root.getChild(child);
		String str = element.getAttributeValue(VALUE);
		return Double.parseDouble(str);
	}

	/**
	 * Returns the crop type instance by its name
	 * 
	 * @param name
	 * @return
	 */
	public CropSpec getCropTypeByName(String name) {
		return lookUpCropSpecMap.get(name.toLowerCase());
	}
	
	/**
	 * Picks a crop type randomly
	 * 
	 * @return crop type
	 */
	public CropSpec getRandomCropType() {
		return RandomUtil.getRandomElement(cropSpecs);
	}

	/**
	 * Gets the average growing time for all crops.
	 * 
	 * @return average growing time (millisols)
	 */
	public double getAverageCropGrowingTime() {
		if (averageCropGrowingTime == -1) {
			double totalGrowingTime = 0D;
			for (CropSpec ct : cropSpecs) {
				totalGrowingTime += ct.getGrowingSols(); 
			}
			averageCropGrowingTime = (totalGrowingTime * 1000D) / cropSpecs.size();
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
			double neededFoodPerSol = personConfig.getFoodConsumptionRate();
	
			// Determine average amount (kg) of food produced per farm area (m^2).
			double totalFoodPerSolPerArea = 0D;
			for (CropSpec c : cropSpecs)
				// Crop type average edible biomass (kg) per Sol.
				totalFoodPerSolPerArea += c.getEdibleBiomass() / 1000D;
	
			double producedFoodPerSolPerArea = totalFoodPerSolPerArea / cropSpecs.size();
	
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
}
