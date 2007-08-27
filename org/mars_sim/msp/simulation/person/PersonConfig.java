/**
 * Mars Simulation Project
 * PersonConfig.java
 * @version 2.81 2007-08-26
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person;

import java.io.Serializable;
import java.util.*;
import org.w3c.dom.*;

/**
 * Provides configuration information about people units.
 * Uses a DOM document to get the information. 
 */
public class PersonConfig implements Serializable {
	
	// Element names
	private static final String PERSON_NAME_LIST = "person-name-list";
	private static final String PERSON_NAME = "person-name";
	private static final String GENDER = "gender";
	private static final String OXYGEN_CONSUMPTION_RATE = "oxygen-consumption-rate";
	private static final String WATER_CONSUMPTION_RATE = "water-consumption-rate";
	private static final String FOOD_CONSUMPTION_RATE = "food-consumption-rate";
	private static final String OXYGEN_DEPRIVATION_TIME = "oxygen-deprivation-time";
	private static final String WATER_DEPRIVATION_TIME = "water-deprivation-time";
	private static final String FOOD_DEPRIVATION_TIME = "food-deprivation-time";
	private static final String STARVATION_START_TIME = "starvation-start-time";
	private static final String MIN_AIR_PRESSURE = "min-air-pressure";
	private static final String DECOMPRESSION_TIME = "decompression-time";
	private static final String MIN_TEMPERATURE = "min-temperature";
	private static final String MAX_TEMPERATURE = "max-temperature";
	private static final String FREEZING_TIME = "freezing-time";
	private static final String STRESS_BREAKDOWN_CHANCE = "stress-breakdown-chance";
	private static final String GENDER_MALE_PERCENTAGE = "gender-male-percentage";
	private static final String PERSONALITY_TYPES = "personality-types";
	private static final String MBTI = "mbti";
	private static final String PERSON_LIST = "person-list";
	private static final String PERSON = "person";
	private static final String NAME = "name";
	private static final String PERSONALITY_TYPE = "personality-type";
	private static final String SETTLEMENT = "settlement";
	private static final String JOB = "job";
	private static final String NATURAL_ATTRIBUTE_LIST = "natural-attribute-list";
	private static final String NATURAL_ATTRIBUTE = "natural-attribute";
	private static final String VALUE= "value";
	private static final String SKILL_LIST = "skill-list";
	private static final String SKILL = "skill";
	private static final String LEVEL = "level";
	private static final String RELATIONSHIP_LIST = "relationship-list";
	private static final String RELATIONSHIP = "relationship";
	private static final String OPINION = "opinion";
	
	private Document personDoc;
	private List<String> nameList;

	/**
	 * Constructor
	 * @param personDoc the person congif DOM document.
	 */
	public PersonConfig(Document personDoc) {
		this.personDoc = personDoc;
	}
	
	/**
	 * Gets a list of person names for settlers.
	 * @return List of person names.
	 * @throws Exception if person names could not be found.
	 */
	public List<String> getPersonNameList() throws Exception {
		
		if (nameList == null) {
			nameList = new ArrayList<String>();
			Element root = personDoc.getDocumentElement();
			Element personNameList = (Element) root.getElementsByTagName(PERSON_NAME_LIST).item(0);
			NodeList personNames = personNameList.getElementsByTagName(PERSON_NAME);
			for (int x=0; x < personNames.getLength(); x++) {
				Element nameElement = (Element) personNames.item(x);
				nameList.add(nameElement.getAttribute("value"));
			}
		}
		
		return nameList;
	}
	
	/**
	 * Gets the gender of a given person name.
	 * @param name the name of the person
	 * @return the gender of the person name ("male", "female", "unknown")
	 * @throws Exception if person names could not be found.
	 */
	public String getPersonGender(String name) throws Exception {
		String result = "unknown";
		
		Element root = personDoc.getDocumentElement();
		Element personNameList = (Element) root.getElementsByTagName(PERSON_NAME_LIST).item(0);
		NodeList personNames = personNameList.getElementsByTagName(PERSON_NAME);
		for (int x=0; x < personNames.getLength(); x++) {
			Element nameElement = (Element) personNames.item(x);
			String personName = nameElement.getAttribute("value");
			if (personName.equals(name)) result = nameElement.getAttribute(GENDER);
		}
		
		return result;
	}
	
	/**
	 * Gets the oxygen consumption rate.
	 * @return oxygen rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getOxygenConsumptionRate() throws Exception {
		Element root = personDoc.getDocumentElement();
		Element oxygenRateElement = (Element) root.getElementsByTagName(OXYGEN_CONSUMPTION_RATE).item(0);
		String oxygenRateStr = oxygenRateElement.getAttribute("value");
		double oxygenRate = Double.parseDouble(oxygenRateStr);
		return oxygenRate;
	}
	
	/**
	 * Gets the water consumption rate.
	 * @return water rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getWaterConsumptionRate() throws Exception {
		Element root = personDoc.getDocumentElement();
		Element waterRateElement = (Element) root.getElementsByTagName(WATER_CONSUMPTION_RATE).item(0);
		String waterRateStr = waterRateElement.getAttribute("value");
		double waterRate = Double.parseDouble(waterRateStr);
		return waterRate;
	}
	
	/**
	 * Gets the food consumption rate.
	 * @return food rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getFoodConsumptionRate() throws Exception {
		Element root = personDoc.getDocumentElement();
		Element foodRateElement = (Element) root.getElementsByTagName(FOOD_CONSUMPTION_RATE).item(0);
		String foodRateStr = foodRateElement.getAttribute("value");
		double foodRate = Double.parseDouble(foodRateStr);
		return foodRate;
	}
	
	/**
	 * Gets the oxygen deprivation time.
	 * @return oxygen time in millisols.
	 * @throws Exception if oxygen deprivation time could not be found.
	 */
	public double getOxygenDeprivationTime() throws Exception {
		Element root = personDoc.getDocumentElement();
		Element oxygenTimeElement = (Element) root.getElementsByTagName(OXYGEN_DEPRIVATION_TIME).item(0);
		String oxygenTimeStr = oxygenTimeElement.getAttribute("value");
		double oxygenTime = Double.parseDouble(oxygenTimeStr);
		return oxygenTime;
	}
	
	/**
	 * Gets the water deprivation time.
	 * @return water time in sols.
	 * @throws Exception if water deprivation time could not be found.
	 */
	public double getWaterDeprivationTime() throws Exception {
		Element root = personDoc.getDocumentElement();
		Element waterTimeElement = (Element) root.getElementsByTagName(WATER_DEPRIVATION_TIME).item(0);
		String waterTimeStr = waterTimeElement.getAttribute("value");
		double waterTime = Double.parseDouble(waterTimeStr);
		return waterTime;
	}
	
	/**
	 * Gets the food deprivation time.
	 * @return food time in sols.
	 * @throws Exception if food deprivation time could not be found.
	 */
	public double getFoodDeprivationTime() throws Exception {
		Element root = personDoc.getDocumentElement();
		Element foodTimeElement = (Element) root.getElementsByTagName(FOOD_DEPRIVATION_TIME).item(0);
		String foodTimeStr = foodTimeElement.getAttribute("value");
		double foodTime = Double.parseDouble(foodTimeStr);
		return foodTime;
	}
	
	/**
	 * Gets the starvation start time.
	 * @return starvation time in sols.
	 * @throws Exception if starvation start time could not be found.
	 */
	public double getStarvationStartTime() throws Exception {
		Element root = personDoc.getDocumentElement();
		Element starvationTimeElement = (Element) root.getElementsByTagName(STARVATION_START_TIME).item(0);
		String starvationTimeStr = starvationTimeElement.getAttribute("value");
		double starvationTime = Double.parseDouble(starvationTimeStr);
		return starvationTime;
	}
	
	/**
	 * Gets the required air pressure.
	 * @return air pressure in atm.
	 * @throws Exception if air pressure could not be found.
	 */
	public double getMinAirPressure() throws Exception {
		Element root = personDoc.getDocumentElement();
		Element airPressureElement = (Element) root.getElementsByTagName(MIN_AIR_PRESSURE).item(0);
		String airPressureStr = airPressureElement.getAttribute("value");
		double airPressure = Double.parseDouble(airPressureStr);
		return airPressure;
	}
	
	/**
	 * Gets the max decompression time a person can survive.
	 * @return decompression time in millisols.
	 * @throws Exception if decompression time could not be found.
	 */
	public double getDecompressionTime() throws Exception {
		Element root = personDoc.getDocumentElement();
		Element decompressionTimeElement = (Element) root.getElementsByTagName(DECOMPRESSION_TIME).item(0);
		String decompressionTimeStr = decompressionTimeElement.getAttribute("value");
		double decompressionTime = Double.parseDouble(decompressionTimeStr);
		return decompressionTime;
	}
	
	/**
	 * Gets the minimum temperature a person can tolerate.
	 * @return temperature in celsius
	 * @throws Exception if min temperature cannot be found.
	 */
	public double getMinTemperature() throws Exception {
		Element root = personDoc.getDocumentElement();
		Element minTemperatureElement = (Element) root.getElementsByTagName(MIN_TEMPERATURE).item(0);
		String minTemperatureStr = minTemperatureElement.getAttribute("value");
		double minTemperature = Double.parseDouble(minTemperatureStr);
		return minTemperature;
	}
	
	/**
	 * Gets the maximum temperature a person can tolerate.
	 * @return temperature in celsius
	 * @throws Exception if max temperature cannot be found.
	 */
	public double getMaxTemperature() throws Exception {
		Element root = personDoc.getDocumentElement();
		Element maxTemperatureElement = (Element) root.getElementsByTagName(MAX_TEMPERATURE).item(0);
		String maxTemperatureStr = maxTemperatureElement.getAttribute("value");
		double maxTemperature = Double.parseDouble(maxTemperatureStr);
		return maxTemperature;
	}
	
	/**
	 * Gets the time a person can survive below minimum temperature.
	 * @return freezing time in millisols.
	 * @throws Exception if freezing time could not be found.
	 */
	public double getFreezingTime() throws Exception {
		Element root = personDoc.getDocumentElement();
		Element freezingTimeElement = (Element) root.getElementsByTagName(FREEZING_TIME).item(0);
		String freezingTimeStr = freezingTimeElement.getAttribute("value");
		double freezingTime = Double.parseDouble(freezingTimeStr);
		return freezingTime;
	}
	
	/**
	 * Gets the base percent chance that a person will have a stress breakdown when at maximum stress.
	 * @return percent chance of a breakdown per millisol.
	 * @throws Exception if stress breakdown time could not be found.
	 */
	public double getStressBreakdownChance() throws Exception {
		Element root = personDoc.getDocumentElement();
		Element stressBreakdownChanceElement = (Element) root.getElementsByTagName(STRESS_BREAKDOWN_CHANCE).item(0);
		String stressBreakdownChanceStr = stressBreakdownChanceElement.getAttribute("value");
		double stressBreakdownChance = Double.parseDouble(stressBreakdownChanceStr);
		return stressBreakdownChance;
	}
	
	/**
	 * Gets the gender ratio between males and the total population on Mars.
	 * @return gender ratio between males and total population.
	 * @throws Exception if gender ratio could not be found.
	 */
	public double getGenderRatio() throws Exception {
		Element root = personDoc.getDocumentElement();
		Element genderRatioElement = (Element) root.getElementsByTagName(GENDER_MALE_PERCENTAGE).item(0);
		String genderRatioStr = genderRatioElement.getAttribute("value");
		double genderRatio = Double.parseDouble(genderRatioStr) / 100D;
		return genderRatio;
	}
	
	/**
	 * Gets the average percentage for a particular MBTI personality type for settlers.
	 * @param personalityType the MBTI personality type
	 * @return percentage
	 * @throws Exception if personality type could not be found.
	 */
	public double getPersonalityTypePercentage(String personalityType) throws Exception {
		double result = 0D;
		
		Element root = personDoc.getDocumentElement();
		Element personalityTypeList = (Element) root.getElementsByTagName(PERSONALITY_TYPES).item(0);
		NodeList personalityTypes = personalityTypeList.getElementsByTagName(MBTI);
		for (int x=0; x < personalityTypes.getLength(); x++) {
			Element mbtiElement = (Element) personalityTypes.item(x);
			String type = mbtiElement.getAttribute("type");
			if (type.equals(personalityType)) result = Double.parseDouble(mbtiElement.getAttribute("percentage"));
		}
		
		return result;		
	}
	
	/**
	 * Gets the number of people configured for the simulation.
	 * @return number of people.
	 * @throws Exception if error in XML parsing.
	 */
	public int getNumberOfConfiguredPeople() throws Exception {
		Element root = personDoc.getDocumentElement();
		Element personList = (Element) root.getElementsByTagName(PERSON_LIST).item(0);
		NodeList personNodes = personList.getElementsByTagName(PERSON);
		if (personNodes != null) return personNodes.getLength();
		else return 0;
	}
	
	/**
	 * Gets the configured person's name.
	 * @param index the person's index.
	 * @return name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonName(int index) throws Exception {
		Element root = personDoc.getDocumentElement();
		Element personList = (Element) root.getElementsByTagName(PERSON_LIST).item(0);
		Element personElement = (Element) personList.getElementsByTagName(PERSON).item(index);
		if (personElement.hasAttribute(NAME)) return personElement.getAttribute(NAME);
		else return null;
	}
	
	/**
	 * Gets the configured person's gender.
	 * @param index the person's index.
	 * @return "male", "female" or null if not found.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonGender(int index) throws Exception {
		Element root = personDoc.getDocumentElement();
		Element personList = (Element) root.getElementsByTagName(PERSON_LIST).item(0);
		Element personElement = (Element) personList.getElementsByTagName(PERSON).item(index);
		if (personElement.hasAttribute(GENDER)) return personElement.getAttribute(GENDER);
		else return null;
	}
	
	/**
	 * Gets the configured person's MBTI personality type.
	 * @param index the person's index.
	 * @return four character string for MBTI ex. "ISTJ". Return null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonPersonalityType(int index) throws Exception {
		Element root = personDoc.getDocumentElement();
		Element personList = (Element) root.getElementsByTagName(PERSON_LIST).item(0);
		Element personElement = (Element) personList.getElementsByTagName(PERSON).item(index);
		if (personElement.hasAttribute(PERSONALITY_TYPE)) return personElement.getAttribute(PERSONALITY_TYPE);
		else return null;
	}
	
	/**
	 * Gets the configured person's starting settlement. 
	 * @param index the person's index.
	 * @return the settlement name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonSettlement(int index) throws Exception {
		Element root = personDoc.getDocumentElement();
		Element personList = (Element) root.getElementsByTagName(PERSON_LIST).item(0);
		Element personElement = (Element) personList.getElementsByTagName(PERSON).item(index);
		if (personElement.hasAttribute(SETTLEMENT)) return personElement.getAttribute(SETTLEMENT);
		else return null;
	}
	
	/**
	 * Gets the configured person's job.
	 * @param index the person's index.
	 * @return the job name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonJob(int index) throws Exception {
		Element root = personDoc.getDocumentElement();
		Element personList = (Element) root.getElementsByTagName(PERSON_LIST).item(0);
		Element personElement = (Element) personList.getElementsByTagName(PERSON).item(index);
		if (personElement.hasAttribute(JOB)) return personElement.getAttribute(JOB);
		else return null;
	}
	
	/**
	 * Gets a map of the configured person's natural attributes.
	 * @param index the person's index.
	 * @return map of natural attributes (empty map if not found).
	 * @throws Exception if error in XML parsing.
	 */
	public Map<String, Integer> getNaturalAttributeMap(int index) throws Exception {
		Map<String, Integer> result = new HashMap<String, Integer>();
		Element root = personDoc.getDocumentElement();
		Element personList = (Element) root.getElementsByTagName(PERSON_LIST).item(0);
		Element personElement = (Element) personList.getElementsByTagName(PERSON).item(index);
		NodeList naturalAttributeListNodes = personElement.getElementsByTagName(NATURAL_ATTRIBUTE_LIST);
		if ((naturalAttributeListNodes != null) && (naturalAttributeListNodes.getLength() > 0)) {
			Element naturalAttributeList = (Element) naturalAttributeListNodes.item(0);
			int attributeNum = naturalAttributeList.getElementsByTagName(NATURAL_ATTRIBUTE).getLength();
			for (int x=0; x < attributeNum; x++) {
				Element naturalAttributeElement = (Element) naturalAttributeList.getElementsByTagName(NATURAL_ATTRIBUTE).item(x);
				String name = naturalAttributeElement.getAttribute(NAME);
				Integer value = new Integer(naturalAttributeElement.getAttribute(VALUE));
				result.put(name, value);
			}
		}
		return result;
	}
	
	/**
	 * Gets a map of the configured person's skills.
	 * @param index the person's index.
	 * @return map of skills (empty map if not found).
	 * @throws Exception if error in XML parsing.
	 */
	public Map<String, Integer> getSkillMap(int index) throws Exception {
		Map<String, Integer> result = new HashMap<String, Integer>();
		Element root = personDoc.getDocumentElement();
		Element personList = (Element) root.getElementsByTagName(PERSON_LIST).item(0);
		Element personElement = (Element) personList.getElementsByTagName(PERSON).item(index);
		NodeList skillListNodes = personElement.getElementsByTagName(SKILL_LIST);
		if ((skillListNodes != null) && (skillListNodes.getLength() > 0)) {
			Element skillList = (Element) skillListNodes.item(0);
			int skillNum = skillList.getElementsByTagName(SKILL).getLength();
			for (int x=0; x < skillNum; x++) {
				Element skillElement = (Element) skillList.getElementsByTagName(SKILL).item(x);
				String name = skillElement.getAttribute(NAME);
				Integer level = new Integer(skillElement.getAttribute(LEVEL));
				result.put(name, level);
			}
		}
		return result;
	}
	
	/**
	 * Gets a map of the configured person's relationships.
	 * @param index the person's index.
	 * @return map of relationships (key: person name, value: opinion (0 - 100)) 
	 * (empty map if not found).
	 * @throws Exception if error in XML parsing.
	 */
	public Map<String, Integer> getRelationshipMap(int index) throws Exception {
		Map<String, Integer> result = new HashMap<String, Integer>();
		Element root = personDoc.getDocumentElement();
		Element personList = (Element) root.getElementsByTagName(PERSON_LIST).item(0);
		Element personElement = (Element) personList.getElementsByTagName(PERSON).item(index);
		NodeList relationshipListNodes = personElement.getElementsByTagName(RELATIONSHIP_LIST);
		if ((relationshipListNodes != null) && (relationshipListNodes.getLength() > 0)) {
			Element relationshipList = (Element) relationshipListNodes.item(0);
			int relationshipNum = relationshipList.getElementsByTagName(RELATIONSHIP).getLength();
			for (int x=0; x < relationshipNum; x++) {
				Element relationshipElement = (Element) relationshipList.getElementsByTagName(RELATIONSHIP).item(x);
				String personName = relationshipElement.getAttribute(PERSON_NAME);
				Integer opinion = new Integer(relationshipElement.getAttribute(OPINION));
				result.put(personName, opinion);
			}
		}
		return result;
	}
}