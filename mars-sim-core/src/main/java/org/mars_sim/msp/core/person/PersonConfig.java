/**
 * Mars Simulation Project
 * PersonConfig.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;

/**
 * Provides configuration information about people units. Uses a JDOM document
 * to get the information.
 */
public class PersonConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// private static final Logger logger = Logger.getLogger(PersonConfig.class.getName());

	// Element names
	private final String LAST_NAME_LIST = "last-name-list";
	private final String FIRST_NAME_LIST = "first-name-list";
	private final String LAST_NAME = "last-name";
	private final String FIRST_NAME = "first-name";
	private final String PERSON_NAME_LIST = "person-name-list";
	private final String PERSON_NAME = "person-name";

	private final String GENDER = "gender";

	private final String SPONSOR = "sponsor";
	private final String COUNTRY = "country";

	/** The base carrying capacity (kg) of a person. */
	private final String BASE_CAPACITY = "base-carrying-capacity";
	private final String AVERAGE_TALL_HEIGHT = "average-tall-height";//176.5;
	private final String AVERAGE_SHORT_HEIGHT = "average-short-height";//162.5;
//	private final static String AVERAGE_HEIGHT = "average_height"; // 169.5;// (AVERAGE_TALL_HEIGHT + AVERAGE_SHORT_HEIGHT)/2D;

	private final String AVERAGE_HIGH_WEIGHT = "average-high-weight";// 68.5;
	private final String AVERAGE_LOW_WEIGHT = "average-low-weight";
//	private	final static String AVERAGE_WEIGHT = "average_low_weight"; //62.85;
	
	private final String LOW_O2_RATE = "low-activity-metaboic-load-o2-consumption-rate";
	private final String NOMINAL_O2_RATE = "nominal-activity-metaboic-load-o2-consumption-rate";
	private final String HIGH_O2_RATE = "high-activity-metaboic-load-o2-consumption-rate";

	private final String CO2_EXPELLED_RATE = "co2-expelled-rate";

	private final String WATER_CONSUMPTION_RATE = "water-consumption-rate";
	private final String WATER_USAGE_RATE = "water-usage-rate";
	private final String GREY_TO_BLACK_WATER_RATIO = "grey-to-black-water-ratio";

	private final String FOOD_CONSUMPTION_RATE = "food-consumption-rate";
	private final String DESSERT_CONSUMPTION_RATE = "dessert-consumption-rate";

	private final String OXYGEN_DEPRIVATION_TIME = "oxygen-deprivation-time";
	private final String WATER_DEPRIVATION_TIME = "water-deprivation-time";
	private final String FOOD_DEPRIVATION_TIME = "food-deprivation-time";

	private final String DEHYDRATION_START_TIME = "dehydration-start-time";
	private final String STARVATION_START_TIME = "starvation-start-time";

	private final String MIN_AIR_PRESSURE = "min-air-pressure";
	private final String MIN_O2_PARTIAL_PRESSURE = "min-o2-partial-pressure";
	
	private final String MIN_TEMPERATURE = "min-temperature";
	private final String MAX_TEMPERATURE = "max-temperature";
	
	private final String DECOMPRESSION_TIME = "decompression-time";
	private final String FREEZING_TIME = "freezing-time";
	
	private final String STRESS_BREAKDOWN_CHANCE = "stress-breakdown-chance";
	private final String HIGH_FATIGUE_COLLAPSE = "high-fatigue-collapse-chance";

	private final String GENDER_MALE_PERCENTAGE = "gender-male-percentage";
	
	private final String PERSONALITY_TYPES = "personality-types";
	private final String MBTI = "mbti";

	private final String TYPE = "type";
	private final String VALUE = "value";

	private final String PERCENTAGE = "percentage";

	/** The base load-carrying capacity. */
	private transient double baseCap = -1;
	/** The upper and lower height. */
	private transient double[] height = new double[] {-1, -1};
	/** The high and lor weight. */
	private transient double[] weight = new double[] {-1, -1};
	/** The 3 types of metabolic loads. */
	private transient double[] o2ConsumptionRate = new double[] {-1, -1, -1};
	/** The consumption rate for water, dessert, food. */
	private transient double[] consumptionRates = new double[] {-1, -1, -1};
	/** The grey2BlackWaterRatio and the gender ratio. */
	private transient double[] ratio = new double[] {-1, -1};
	/** The stress breakdown and high fatigue collapse chance. */
	private transient double[] chance = new double[] {-1, -1};
	/** Various time values. */
	private transient double[] time = new double[] {-1, -1, -1, -1, -1, -1, -1};
	/** The min and max temperature. */
	private transient double[] temperature = new double[] {-1, -1};
	/** The average rate of water usage [kg/sol]. */
	private transient double waterUsage = -1;
	/** The min air pressure [kPa]. */
	private transient double pressure = -1;
	/** The min o2 partial pressure [kPa]. */
	private transient double o2pressure = -1;
	/** The co2 expulsion rate [kg/sol]. */
	private transient double co2Rate = -1;
	
	/** The document object. */
	private transient Document personDoc;
	/** The personality distribution map. */
	private transient Map<String, Double> personalityDistribution;
	/** The lists. */
	private transient List<String> personNameList;
	private transient List<String> allCountries;
	private transient List<String> ESACountries;
	
	private transient Map<Integer,PersonNameSpec> namesByCountry = new HashMap<>();
	private transient Map<ReportingAuthorityType,PersonNameSpec> namesBySponsor = new HashMap<>();
	
	private transient Commander commander;

	/**
	 * Constructor
	 * 
	 * @param personDoc the person config DOM document.
	 */
	public PersonConfig(Document personDoc) {
		this.personDoc = personDoc;
		commander = new Commander();

		getPersonNameList();
		parseNames(personDoc);
		createPersonalityDistribution();
	}

	/**
	 * Gets a list of person names for settlers.
	 * 
	 * @return List of person names.
	 * @throws Exception if person names could not be found.
	 */
	public List<String> getPersonNameList() {

		if (personNameList == null) {
			personNameList = new ArrayList<String>();
			Element personNameEl = personDoc.getRootElement().getChild(PERSON_NAME_LIST);
			List<Element> personNames = personNameEl.getChildren(PERSON_NAME);

			for (Element nameElement : personNames) {
				personNameList.add(nameElement.getAttributeValue(VALUE));
			}
		}

		return personNameList;
	}

	/**
	 * Parse the names element of the document.
	 * @param doc XML document
	 */
	private void parseNames(Document doc) {
		List<String> countryNames = createAllCountryList();

		// Scan last names
		Element lastNameEl = doc.getRootElement().getChild(LAST_NAME_LIST);
		List<Element> lastNamesList = lastNameEl.getChildren(LAST_NAME);
		for (Element nameElement : lastNamesList) {
	
			String sponsor = nameElement.getAttributeValue(SPONSOR);
			String name = nameElement.getAttributeValue(VALUE);
			String country = nameElement.getAttributeValue(COUNTRY);
	
			ReportingAuthorityType ra = ReportingAuthorityType.valueOf(sponsor);
			PersonNameSpec raSpec = namesBySponsor.computeIfAbsent(ra,
											k -> new PersonNameSpec());
			raSpec.addLastName(name);
	
			int countryId = countryNames.indexOf(country);
			PersonNameSpec countrySpec = namesByCountry.computeIfAbsent(countryId,
					k -> new PersonNameSpec());
			countrySpec.addLastName(name);
		}

		// Scan efirst names
		Element firstNameEl = personDoc.getRootElement().getChild(FIRST_NAME_LIST);
		List<Element> firstNamesList = firstNameEl.getChildren(FIRST_NAME);
		for (Element nameElement : firstNamesList) {

			String gender = nameElement.getAttributeValue(GENDER);
			String sponsor = nameElement.getAttributeValue(SPONSOR);
			String name = nameElement.getAttributeValue(VALUE);
			String country = nameElement.getAttributeValue(COUNTRY);

			ReportingAuthorityType ra = ReportingAuthorityType.valueOf(sponsor);
			PersonNameSpec raSpec = namesBySponsor.computeIfAbsent(ra,
											k -> new PersonNameSpec());
	
			int countryId = countryNames.indexOf(country);
			PersonNameSpec countrySpec = namesByCountry.computeIfAbsent(countryId,
					k -> new PersonNameSpec());

			if (gender.equals("male")) {
				countrySpec.addMaleName(name);
				raSpec.addMaleName(name);
			} else if (gender.equals("female")) {
				countrySpec.addFemaleName(name);
				raSpec.addFemaleName(name);
			}
		}
	}
	

	/**
	 * Gets the gender of a given person name.
	 * 
	 * @param name the name of the person
	 * @return {@link GenderType} the gender of the person name
	 * @throws Exception if person names could not be found.
	 */
	public GenderType getPersonGender(String name) {
		GenderType result = GenderType.UNKNOWN;

		Element personNameList = personDoc.getRootElement().getChild(PERSON_NAME_LIST);
		List<Element> personNames = personNameList.getChildren(PERSON_NAME);
		for (Element nameElement : personNames) {
			String personName = nameElement.getAttributeValue(VALUE);
			if (personName.equals(name))
				result = GenderType.valueOfIgnoreCase(nameElement.getAttributeValue(GENDER));
		}

		return result;
	}
	
	/**
	 * Gets the base load capacity of a person.
	 * 
	 * @return capacity in kg
	 */
	public double getBaseCapacity() {
		if (baseCap >= 0)
			return baseCap;
		else {
			baseCap = getValueAsDouble(BASE_CAPACITY);
			return baseCap;
		}
	}
	
	
	/**
	 * Gets the upper average height of a person.
	 * 
	 * @return height in cm
	 */
	public double getTallAverageHeight() {
		double r = height[0];
		if (r >= 0)
			return r;
		else {
			r = getValueAsDouble(AVERAGE_TALL_HEIGHT);
			height[0] = r;
			return r;
		}
	}
	
	/**
	 * Gets the lower average height of a person.
	 * 
	 * @return height in cm
	 */
	public double getShortAverageHeight() {
		double r = height[1];
		if (r >= 0)
			return r;
		else {
			r = getValueAsDouble(AVERAGE_SHORT_HEIGHT);
			height[1] = r;
			return r;
		}
	}
	
	
	
	
	/**
	 * Gets the high average weight of a person.
	 * 
	 * @return weight in kg
	 */
	public double getHighAverageWeight() {
		double r = weight[0];
		if (r >= 0)
			return r;
		else {
			r = getValueAsDouble(AVERAGE_HIGH_WEIGHT);
			weight[0] = r;
			return r;
		}
	}
	
	/**
	 * Gets the low average weight of a person.
	 * 
	 * @return weight in kg
	 */
	public double getLowAverageWeight() {
		double r = weight[1];
		if (r >= 0)
			return r;
		else {
			r = getValueAsDouble(AVERAGE_LOW_WEIGHT);
			weight[1] = r;
			return r;
		}
	}

	/**
	 * Gets the nominal oxygen consumption rate.
	 * 
	 * @return oxygen rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getNominalO2ConsumptionRate() {
		double r = o2ConsumptionRate[1];
		if (r >= 0)
			return r;
		else {
			r = getValueAsDouble(NOMINAL_O2_RATE);
			o2ConsumptionRate[1] = r;
			return r;
		}
	}

	/**
	 * Gets the low oxygen consumption rate.
	 * 
	 * @return oxygen rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getLowO2ConsumptionRate() {
		double r = o2ConsumptionRate[0];
		if (r >= 0)
			return r;
		else {
			r = getValueAsDouble(LOW_O2_RATE);
			o2ConsumptionRate[0] = r;
			return r;
		}
	}

	/**
	 * Gets the high oxygen consumption rate.
	 * 
	 * @return oxygen rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getHighO2ConsumptionRate() {
		double r = o2ConsumptionRate[2];
		if (r >= 0)
			return r;
		else {
			r = getValueAsDouble(HIGH_O2_RATE);
			o2ConsumptionRate[2] = r;
			return r;
		}
	}

	/**
	 * Gets the carbon dioxide expelled rate.
	 * 
	 * @return carbon dioxide expelled rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getCO2ExpelledRate() {
		if (co2Rate >= 0)
			return co2Rate;
		else {
			co2Rate = getValueAsDouble(CO2_EXPELLED_RATE);
			return co2Rate;
		}
	}

	/**
	 * Gets the water consumption rate.
	 * 
	 * @return water rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getWaterConsumptionRate() {
		double r = consumptionRates[0];
		if (r >= 0)
			return r;
		else {
			r = getValueAsDouble(WATER_CONSUMPTION_RATE);
			consumptionRates[0] = r;
			return r;
		}
	}

	/**
	 * Gets the water usage rate.
	 * 
	 * @return water rate (kg/sol)
	 * @throws Exception if usage rate could not be found.
	 */
	public double getWaterUsageRate() {
		if (waterUsage >= 0)
			return waterUsage;
		else {
			waterUsage = getValueAsDouble(WATER_USAGE_RATE);
			return waterUsage;
		}
	}

	/**
	 * Gets the grey to black water ratio.
	 * 
	 * @return ratio
	 * @throws Exception if the ratio could not be found.
	 */
	public double getGrey2BlackWaterRatio() {
		double r = ratio[0];
		if (r >= 0)
			return r;
		else {
			r = getValueAsDouble(GREY_TO_BLACK_WATER_RATIO);
			ratio[0] = r;
			return r;
		}
	}

	/**
	 * Gets the food consumption rate.
	 * 
	 * @return food rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getFoodConsumptionRate() {
		double r = consumptionRates[2];
		if (r >= 0)
			return r;
		else {
			r = getValueAsDouble(FOOD_CONSUMPTION_RATE);
			consumptionRates[2] = r;
			return r;
		}
	}

	/**
	 * Gets the dessert consumption rate.
	 * 
	 * @return dessert rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getDessertConsumptionRate() {
		double r = consumptionRates[1];
		if (r >= 0)
			return r;
		else {
			r = getValueAsDouble(DESSERT_CONSUMPTION_RATE);
			consumptionRates[1] = r;
			return r;
		}
	}

	/**
	 * Gets the oxygen deprivation time.
	 * 
	 * @return oxygen time in millisols.
	 * @throws Exception if oxygen deprivation time could not be found.
	 */
	public double getOxygenDeprivationTime() {
		double r = time[0];
		if (r >= 0)
			return r;
		else {
			r = getValueAsDouble(OXYGEN_DEPRIVATION_TIME);
			time[0] = r;
			return r;
		}
	}

	/**
	 * Gets the water deprivation time.
	 * 
	 * @return water time in sols.
	 * @throws Exception if water deprivation time could not be found.
	 */
	public double getWaterDeprivationTime() {
		double r = time[1];
		if (r >= 0)
			return r;
		else {
			r = getValueAsDouble(WATER_DEPRIVATION_TIME);
			time[1] = r;
			return r;
		}
	}

	/**
	 * Gets the dehydration start time.
	 * 
	 * @return dehydration time in sols.
	 * @throws Exception if dehydration start time could not be found.
	 */
	public double getDehydrationStartTime() {
		double r = time[2];
		if (r >= 0)
			return r;
		else {
			r = getValueAsDouble(DEHYDRATION_START_TIME);
			time[2] = r;
			return r;
		}
	}

	/**
	 * Gets the food deprivation time.
	 * 
	 * @return food time in sols.
	 * @throws Exception if food deprivation time could not be found.
	 */
	public double getFoodDeprivationTime() {
		double r = time[3];
		if (r >= 0)
			return r;
		else {
			r = getValueAsDouble(FOOD_DEPRIVATION_TIME);
			time[3] = r;
			return r;
		}
	}

	/**
	 * Gets the starvation start time.
	 * 
	 * @return starvation time in sols.
	 * @throws Exception if starvation start time could not be found.
	 */
	public double getStarvationStartTime() {
		double r = time[4];
		if (r >= 0)
			return r;
		else {
			r = getValueAsDouble(STARVATION_START_TIME);
			time[4] = r;
			return r;
		}
	}

	/**
	 * Gets the minimum air pressure.
	 * 
	 * @return air pressure in kPa.
	 * @throws Exception if air pressure could not be found.
	 */
	public double getMinAirPressure() {
		if (pressure >= 0)
			return pressure;
		else {
			pressure = getValueAsDouble(MIN_AIR_PRESSURE);
			return pressure;
		}
	}
		
	/**
	 * Gets the absolute minimum oxygen partial pressure of a spacesuit.
	 * 
	 * @return partial pressure in kPa.
	 * @throws Exception if air pressure could not be found.
	 */
	public double getMinSuitO2Pressure() {
		if (o2pressure >= 0)
			return o2pressure;
		else {
			o2pressure = getValueAsDouble(MIN_O2_PARTIAL_PRESSURE);
			return o2pressure;
		}
	}
	
	/**
	 * Gets the max decompression time a person can survive.
	 * 
	 * @return decompression time in millisols.
	 * @throws Exception if decompression time could not be found.
	 */
	public double getDecompressionTime() {
		double r = time[5];
		if (r >= 0)
			return r;
		else {
			r = getValueAsDouble(DECOMPRESSION_TIME);
			time[5] = r;
			return r;
		}
	}

	/**
	 * Gets the minimum temperature a person can tolerate.
	 * 
	 * @return temperature in celsius
	 * @throws Exception if min temperature cannot be found.
	 */
	public double getMinTemperature() {
		double r = temperature[0];
		if (r >= 0)
			return r;
		else {
			r = getValueAsDouble(MIN_TEMPERATURE);
			temperature[0] = r;
			return r;
		}
	}

	/**
	 * Gets the maximum temperature a person can tolerate.
	 * 
	 * @return temperature in celsius
	 * @throws Exception if max temperature cannot be found.
	 */
	public double getMaxTemperature() {
		double r = temperature[1];
		if (r >= 0)
			return r;
		else {
			r = getValueAsDouble(MAX_TEMPERATURE);
			temperature[1] = r;
			return r;
		}
	}

	/**
	 * Gets the time a person can survive below minimum temperature.
	 * 
	 * @return freezing time in millisols.
	 * @throws Exception if freezing time could not be found.
	 */
	public double getFreezingTime() {
		double r = time[6];
		if (r >= 0)
			return r;
		else {
			r = getValueAsDouble(FREEZING_TIME);
			time[6] = r;
			return r;
		}
	}

	/**
	 * Gets the base percent chance that a person will have a stress breakdown when
	 * at maximum stress.
	 * 
	 * @return percent chance of a breakdown per millisol.
	 * @throws Exception if stress breakdown time could not be found.
	 */
	public double getStressBreakdownChance() {
		double r = chance[0];
		if (r >= 0)
			return r;
		else {
			r = getValueAsDouble(STRESS_BREAKDOWN_CHANCE);
			chance[0] = r;
			return r;
		}
	}

	/**
	 * Gets the base percent chance that a person will collapse under high fatigue.
	 * 
	 * @return percent chance of a collapse per millisol.
	 * @throws Exception if collapse time could not be found.
	 */
	public double getHighFatigueCollapseChance() {
		double r = chance[1];
		if (r >= 0)
			return r;
		else {
			r = getValueAsDouble(HIGH_FATIGUE_COLLAPSE);
			chance[1] = r;
			return r;
		}
	}

	/**
	 * Gets the gender ratio between males and the total population on Mars.
	 * 
	 * @return gender ratio between males and total population.
	 * @throws Exception if gender ratio could not be found.
	 */
	public double getGenderRatio() {
		double r = ratio[1];
		if (r >= 0)
			return r;
		else {
			r = getValueAsDouble(GENDER_MALE_PERCENTAGE) / 100D;
			ratio[1] = r;
			return r;
		}
	}

	/**
	 * Gets the average percentage for a particular MBTI personality type for
	 * settlers.
	 * 
	 * @param personalityType the MBTI personality type
	 * @return percentage
	 * @throws Exception if personality type could not be found.
	 */
	public double getPersonalityTypePercentage(String personalityType) {
		double result = 0D;

		Element personalityTypeList = personDoc.getRootElement().getChild(PERSONALITY_TYPES);
		List<Element> personalityTypes = personalityTypeList.getChildren(MBTI);

		for (Element mbtiElement : personalityTypes) {
			String type = mbtiElement.getAttributeValue(TYPE);
			if (type.equals(personalityType)) {
				result = Double.parseDouble(mbtiElement.getAttributeValue(PERCENTAGE));
				break;
			}
		}

		return result;
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
	// Relocate createPersonalityDistribution() from MBTI to here
	public void createPersonalityDistribution() {
		if (personalityDistribution == null) {
			personalityDistribution = new HashMap<String, Double>(16);
	
			personalityDistribution.put("ISTP", getPersonalityTypePercentage("ISTP"));
			personalityDistribution.put("ISTJ", getPersonalityTypePercentage("ISTJ"));
			personalityDistribution.put("ISFP", getPersonalityTypePercentage("ISFP"));
			personalityDistribution.put("ISFJ", getPersonalityTypePercentage("ISFJ"));
			personalityDistribution.put("INTP", getPersonalityTypePercentage("INTP"));
			personalityDistribution.put("INTJ", getPersonalityTypePercentage("INTJ"));
			personalityDistribution.put("INFP", getPersonalityTypePercentage("INFP"));
			personalityDistribution.put("INFJ", getPersonalityTypePercentage("INFJ"));
			personalityDistribution.put("ESTP", getPersonalityTypePercentage("ESTP"));
			personalityDistribution.put("ESTJ", getPersonalityTypePercentage("ESTJ"));
			personalityDistribution.put("ESFP", getPersonalityTypePercentage("ESFP"));
			personalityDistribution.put("ESFJ", getPersonalityTypePercentage("ESFJ"));
			personalityDistribution.put("ENTP", getPersonalityTypePercentage("ENTP"));
			personalityDistribution.put("ENTJ", getPersonalityTypePercentage("ENTJ"));
			personalityDistribution.put("ENFP", getPersonalityTypePercentage("ENFP"));
			personalityDistribution.put("ENFJ", getPersonalityTypePercentage("ENFJ"));
	
	
			Iterator<String> i = personalityDistribution.keySet().iterator();
			double count = 0D;
			while (i.hasNext())
				count += personalityDistribution.get(i.next());
			if (count != 100D)
				throw new IllegalStateException(
						"PersonalityType.loadPersonalityTypes(): percentages don't add up to 100%. (total: " + count + ")");
		}
	}


	/**
	 * Gets the value of an element as a double
	 * 
	 * @param an element
	 * 
	 * @return a double
	 */
	private double getValueAsDouble(String child) {
		Element element = personDoc.getRootElement().getChild(child);
		String str = element.getAttributeValue(VALUE);
//		System.out.println("str : " + str);
		return Double.parseDouble(str);
	}


	/**
	 * Create country list
	 * 
	 * @return
	 */
	public List<String> createAllCountryList() {

		if (allCountries == null) {
			List<String> countries = new ArrayList<>();

			countries.add("China"); // 0
			countries.add("Canada"); // 1
			countries.add("India"); // 2
			countries.add("Japan"); // 3
			countries.add("USA"); // 4
			countries.add("Russia"); // 5

			countries.addAll(createESACountryList()); // 6
			
			allCountries = Collections.unmodifiableList(countries);

		}

		return allCountries;
	}

	public String getCountry(int id) {
		if (allCountries == null) {
			allCountries = createAllCountryList();
		}
		return allCountries.get(id);
	}

	/**
	 * Gets the country number from its name
	 * 
	 * @param country
	 * @return
	 */
	public int getCountryNum(String country) {
		if (allCountries == null) {
			allCountries = createAllCountryList();
		}
		for (int i = 0; i < allCountries.size(); i++) {
			if (allCountries.get(i).equalsIgnoreCase(country))
				return i;
		}
		
		return -1;
	}
	
	/**
	 * Create ESA 22 country list
	 * 
	 * @return
	 */
	public List<String> createESACountryList() {

		if (ESACountries == null) {
			List<String> countries = new ArrayList<>();

			countries.add("Austria");
			countries.add("Belgium");
			countries.add("Czech Republic");
			countries.add("Denmark");
			countries.add("Estonia");
			
			countries.add("Finland");
			countries.add("France");
			countries.add("Germany");
			countries.add("Greece");
			countries.add("Hungary");
			
			countries.add("Ireland");
			countries.add("Italy");
			countries.add("Luxembourg");
			countries.add("The Netherlands");
			countries.add("Norway");
			
			countries.add("Poland");
			countries.add("Portugal");
			countries.add("Romania");
			countries.add("Spain");
			countries.add("Sweden");
			
			countries.add("Switzerland");
			countries.add("UK");
			
			ESACountries = Collections.unmodifiableList(countries);

		}

		return ESACountries;
	}

	/**
	 * Computes the country id. If none, return -1.
	 * 
	 * @param country
	 * @return
	 */
	public int computeCountryID(String country) {
		if (allCountries.contains(country))
			return allCountries.indexOf(country);
		else 
			return -1;
	}

	/**
	 * Get the Commander's profile
	 * 
	 * @return profile
	 */
	public Commander getCommander() {
		return commander;
	}

	public PersonNameSpec getNamesByCountry(String country) {
		int id = getCountryNum(country);
		return namesByCountry.get(id);
	}

	public PersonNameSpec getNamesBySponsor(ReportingAuthorityType sponsor) {
		return namesBySponsor.get(sponsor);
	}
}
