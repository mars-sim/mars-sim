/*
 * Mars Simulation Project
 * PersonConfig.java
 * @date 2021-09-04
 * @author Scott Davis
 */
package com.mars_sim.core.person;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.training.TrainingType;

/**
 * Provides configuration information about people units. Uses a JDOM document
 * to get the information.
 */
public class PersonConfig {

	// Key class to combine role & training types
	private static final record KeyClass(RoleType r, TrainingType t) {}

	// Element names
	private static final String NAME = "name";

	private static final String PERSON_ATTRIBUTES = "person-attributes";

	private static final String BASE_CAPACITY = "base-carrying-capacity";

	private static final String LOW_O2_RATE = "low-activity-metaboic-load-o2-consumption-rate";
	private static final String NOMINAL_O2_RATE = "nominal-activity-metaboic-load-o2-consumption-rate";
	private static final String HIGH_O2_RATE = "high-activity-metaboic-load-o2-consumption-rate";

	private static final String CO2_EXPELLED_RATE = "co2-expelled-rate";

	private static final String WATER_CONSUMPTION_RATE = "water-consumption-rate";
	private static final String WATER_USAGE_RATE = "water-usage-rate";
	private static final String GREY_TO_BLACK_WATER_RATIO = "grey-to-black-water-ratio";

	private static final String FOOD_CONSUMPTION_RATE = "food-consumption-rate";
	private static final String DESSERT_CONSUMPTION_RATE = "dessert-consumption-rate";

	private static final String OXYGEN_DEPRIVATION_TIME = "oxygen-deprivation-time";
	private static final String WATER_DEPRIVATION_TIME = "water-deprivation-time";
	private static final String FOOD_DEPRIVATION_TIME = "food-deprivation-time";

	private static final String DEHYDRATION_START_TIME = "dehydration-start-time";
	private static final String STARVATION_START_TIME = "starvation-start-time";

	private static final String MIN_AIR_PRESSURE = "min-air-pressure";
	private static final String MIN_O2_PARTIAL_PRESSURE = "min-o2-partial-pressure";

	private static final String MIN_TEMPERATURE = "min-temperature";
	private static final String MAX_TEMPERATURE = "max-temperature";

	private static final String DECOMPRESSION_TIME = "decompression-time";
	private static final String FREEZING_TIME = "freezing-time";

	private static final String STRESS_BREAKDOWN_CHANCE = "stress-breakdown-chance";
	private static final String HIGH_FATIGUE_COLLAPSE = "high-fatigue-collapse-chance";

	private static final String PERSONALITY_TYPES = "personality-types";
	private static final String MBTI = "mbti";

	private static final String TYPE = "type";
	private static final String VALUE = "value";

	private static final String PERCENTAGE = "percentage";

	/** Default Country to use for name creation */
	private static final String TRAINING_LIST = "training-list";
	private static final String TRAINING = "training";
	private static final String BENEFITS = "benefit";
	private static final String MODIFIER = "modifier";
	private static final String ROLE = "role";

	private static final String CHARACTERISTICS = "characteristics";

	/** The base load-carrying capacity. */
	private double baseCap = -1;
	/** The grey2BlackWater ratio. */
	private double grey2BlackWaterRatio = -1;
	/** The average rate of water usage [kg/sol]. */
	private double waterUsage = -1;
	/** The in air pressure [kPa]. */
	private double pressure = -1;
	/** The min o2 partial pressure [kPa]. */
	private double o2pressure = -1;
	/** The co2 expulsion rate [kg/sol]. */
	private double co2Rate = -1;

	/** The personality distribution map. */
	private Map<String, Double> personalityDistribution;

	private Commander commander;

	private Map<KeyClass, Integer> trainingMods = new HashMap<>();

	private PopulationCharacteristics defaultCharacteristics;

	private Double waterConsumption;
	private double foodConsumption;
	private double dessertConsumption;
	private double o2DeprivationTime;
	private double dehyrationStartTime;
	private double waterDeprivationTime;
	private double foodDeprivationTime;
	private double starvationStartTime;
	private double decompressionTime;
	private double minTemperature;
	private double maxTemperature;
	private double freezingTime;
	private double highFatigureCollapseChance;
	private double stressBreakdownChance;
	private double lowO2ConsumptionRate;
	private double nominalO2ConsumptionRate;
	private double highO2ConsumptionRate;
	
	public PersonConfig(Document personDoc) {
		commander = new Commander();

		parsePersonAttrs(personDoc);
		createPersonalityDistribution(personDoc);
		loadTrainingMods(personDoc);
	}

	private void loadTrainingMods(Document doc) {
		Element trainingListEl = doc.getRootElement().getChild(TRAINING_LIST);
		List<Element> trainingsList = trainingListEl.getChildren(TRAINING);
		for (Element trainingElement : trainingsList) {
			String trainingName = trainingElement.getAttributeValue(NAME);
			TrainingType tType = TrainingType.valueOf(ConfigHelper.convertToEnumName(trainingName));
			for(Element benefit : trainingElement.getChildren(BENEFITS)) {
				RoleType rType = RoleType.valueOf(ConfigHelper.convertToEnumName(benefit.getAttributeValue(ROLE)));
				int mod = ConfigHelper.getOptionalAttributeInt(benefit, MODIFIER, 0);
				trainingMods.put(new KeyClass(rType, tType), mod);
			}
		}
	}

	private void parsePersonAttrs(Document personDoc) {
		// Scan the attributes
		Map<String, Double> personAttributes = new HashMap<>();
		Element personAttributeEl = personDoc.getRootElement().getChild(PERSON_ATTRIBUTES);
		for (Element personAttr : personAttributeEl.getChildren()) {
			String str = personAttr.getAttributeValue(VALUE);

			personAttributes.put(personAttr.getName(), Double.parseDouble(str));
		}

		// Pick out values
		baseCap = personAttributes.get(BASE_CAPACITY);

		lowO2ConsumptionRate = personAttributes.get(LOW_O2_RATE);
		nominalO2ConsumptionRate = personAttributes.get(NOMINAL_O2_RATE);
		highO2ConsumptionRate = personAttributes.get(HIGH_O2_RATE);
		o2DeprivationTime= personAttributes.get(OXYGEN_DEPRIVATION_TIME);
		o2pressure = personAttributes.get(MIN_O2_PARTIAL_PRESSURE);

		co2Rate = personAttributes.get(CO2_EXPELLED_RATE);
		waterConsumption = personAttributes.get(WATER_CONSUMPTION_RATE);
		waterUsage = personAttributes.get(WATER_USAGE_RATE);
		waterDeprivationTime = personAttributes.get(WATER_DEPRIVATION_TIME);
		grey2BlackWaterRatio = personAttributes.get(GREY_TO_BLACK_WATER_RATIO);

		minTemperature = personAttributes.get(MIN_TEMPERATURE);
		maxTemperature = personAttributes.get(MAX_TEMPERATURE);

		foodConsumption = personAttributes.get(FOOD_CONSUMPTION_RATE);
		dessertConsumption = personAttributes.get(DESSERT_CONSUMPTION_RATE);

		dehyrationStartTime = personAttributes.get(DEHYDRATION_START_TIME);
		foodDeprivationTime = personAttributes.get(FOOD_DEPRIVATION_TIME);
		starvationStartTime = personAttributes.get(STARVATION_START_TIME);
		pressure = personAttributes.get(MIN_AIR_PRESSURE);
		decompressionTime = personAttributes.get(DECOMPRESSION_TIME);

		freezingTime = personAttributes.get(FREEZING_TIME);
		stressBreakdownChance = personAttributes.get(STRESS_BREAKDOWN_CHANCE);
		highFatigureCollapseChance = personAttributes.get(HIGH_FATIGUE_COLLAPSE);

		Element perChar = personDoc.getRootElement().getChild(CHARACTERISTICS);
		defaultCharacteristics = ConfigHelper.parsePopulation(perChar);
	}

	/**
	 * Gets the base load capacity of a person.
	 * 
	 * @return capacity in kg
	 */
	public double getBaseCapacity() {
		return baseCap;
	}

	/**
	 * Gets the nominal oxygen consumption rate.
	 * 
	 * @return oxygen rate (kg/sol)
	 */
	public double getNominalO2ConsumptionRate() {
		return nominalO2ConsumptionRate;
	}

	/**
	 * Gets the low oxygen consumption rate.
	 * 
	 * @return oxygen rate (kg/sol)
	 */
	public double getLowO2ConsumptionRate() {
		return lowO2ConsumptionRate;
	}

	/**
	 * Gets the high oxygen consumption rate.
	 * 
	 * @return oxygen rate (kg/sol)
	 */
	public double getHighO2ConsumptionRate() {
		return highO2ConsumptionRate;
	}

	/**
	 * Gets the carbon dioxide expelled rate.
	 * 
	 * @return carbon dioxide expelled rate (kg/sol)
	 */
	public double getCO2ExpelledRate() {
		return co2Rate;
	}

	/**
	 * Gets the water consumption rate.
	 * 
	 * @return water rate (kg/sol)
	 */
	public double getWaterConsumptionRate() {
		return waterConsumption;
	}

	/**
	 * Gets the water usage rate.
	 * 
	 * @return water rate (kg/sol)
	 */
	public double getWaterUsageRate() {
		return waterUsage;
	}

	/**
	 * Gets the grey to black water ratio.
	 * 
	 * @return ratio
	 */
	public double getGrey2BlackWaterRatio() {
		return grey2BlackWaterRatio;
	}

	/**
	 * Gets the food consumption rate.
	 * 
	 * @return food rate (kg/sol)
	 */
	public double getFoodConsumptionRate() {
		return foodConsumption;
	}

	/**
	 * Gets the dessert consumption rate.
	 * 
	 * @return dessert rate (kg/sol)
	 */
	public double getDessertConsumptionRate() {
		return dessertConsumption;
	}

	/**
	 * Gets the oxygen deprivation time.
	 * 
	 * @return oxygen time in millisols.
	 */
	public double getOxygenDeprivationTime() {
		return o2DeprivationTime;
	}

	/**
	 * Gets the water deprivation time.
	 * 
	 * @return water time in sols.
	 */
	public double getWaterDeprivationTime() {
		return waterDeprivationTime;
	}

	/**
	 * Gets the dehydration start time.
	 * 
	 * @return dehydration time in sols.
	 */
	public double getDehydrationStartTime() {
		return dehyrationStartTime;
	}

	/**
	 * Gets the food deprivation time.
	 * 
	 * @return food time in sols.
	 */
	public double getFoodDeprivationTime() {
		return foodDeprivationTime;
	}

	/**
	 * Gets the starvation start time.
	 * 
	 * @return starvation time in sols.
	 */
	public double getStarvationStartTime() {
		return starvationStartTime;
	}

	/**
	 * Gets the minimum air pressure.
	 * 
	 * @return air pressure in kPa.
	 */
	public double getMinAirPressure() {
		return pressure;
	}

	/**
	 * Gets the absolute minimum oxygen partial pressure of a spacesuit.
	 * 
	 * @return partial pressure in kPa.
	 */
	public double getMinSuitO2Pressure() {
		return o2pressure;
	}

	/**
	 * Gets the max decompression time a person can survive.
	 * 
	 * @return decompression time in millisols.
	 */
	public double getDecompressionTime() {
		return decompressionTime;
	}

	/**
	 * Gets the minimum temperature a person can tolerate.
	 * 
	 * @return temperature in celsius
	 */
	public double getMinTemperature() {
		return minTemperature;
	}

	/**
	 * Gets the maximum temperature a person can tolerate.
	 * 
	 * @return temperature in celsius
	 */
	public double getMaxTemperature() {
		return maxTemperature;
	}

	/**
	 * Gets the time a person can survive below minimum temperature.
	 * 
	 * @return freezing time in millisols.
	 */
	public double getFreezingTime() {
		return freezingTime;
	}

	/**
	 * Gets the base percent chance that a person will have a stress breakdown when
	 * at maximum stress.
	 * 
	 * @return percent chance of a breakdown per millisol.
	 */
	public double getStressBreakdownChance() {
		return stressBreakdownChance;
	}

	/**
	 * Gets the base percent chance that a person will collapse under high fatigue.
	 * 
	 * @return percent chance of a collapse per millisol.
	 */
	public double getHighFatigueCollapseChance() {
		return highFatigureCollapseChance;
	}

	/**
	 * Gets the average percentages for personality types
	 * 
	 * @param personalityDistribution map
	 */
	public Map<String, Double> loadPersonalityDistribution() {
		return personalityDistribution;
	}

	/**
	 * Loads the average percentages for personality types into a map.
	 * 
	 * @throws Exception if personality type cannot be found or percentages don't
	 *                   add up to 100%.
	 */
	private void createPersonalityDistribution(Document personDoc) {
		personalityDistribution = new HashMap<>();

		double total = 0D;

		Element personalityTypeList = personDoc.getRootElement().getChild(PERSONALITY_TYPES);
		List<Element> personalityTypes = personalityTypeList.getChildren(MBTI);

		for (Element mbtiElement : personalityTypes) {
			String type = mbtiElement.getAttributeValue(TYPE);
			double result = Double.parseDouble(mbtiElement.getAttributeValue(PERCENTAGE));

			personalityDistribution.put(type, result);
			total += result;
		}

		if (total != 100D)
			throw new IllegalStateException(
					"PersonalityType.loadPersonalityTypes(): percentages don't add up to 100%. (total: " + total + ")");
	}

	/**
	 * Get the Commander's profile
	 * 
	 * @return profile
	 */
	public Commander getCommander() {
		return commander;
	}

	/**
	 * Finds the training modifiers for a combination of training and role.
	 * 
	 * @param role
	 * @param tt
	 * @return
	 */
	public int getTrainingModifier(RoleType role, TrainingType tt) {

		// lookup in modifier table
		KeyClass k = new KeyClass(role, tt);
		Integer v = trainingMods.get(k);
		if (v == null) {
			return 0;
		}
		else {
			return v;
		}
	}

	/**
	 * Get the default population characteristics
	 * @return
	 */
    public PopulationCharacteristics getDefaultPhysicalChars() {
        return defaultCharacteristics;
    }
}
