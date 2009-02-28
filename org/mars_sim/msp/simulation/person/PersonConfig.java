/**
 * Mars Simulation Project
 * PersonConfig.java
 * @version 2.81 2007-08-26
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;




/**
 * Provides configuration information about people units.
 * Uses a JDOM document to get the information. 
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
	private static final String TYPE= "type";
	private static final String VALUE= "value";
	private static final String SKILL_LIST = "skill-list";
	private static final String SKILL = "skill";
	private static final String LEVEL = "level";
	private static final String RELATIONSHIP_LIST = "relationship-list";
	private static final String RELATIONSHIP = "relationship";
	private static final String OPINION = "opinion";
	private static final String PERCENTAGE = "percentage";
	
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
			Element root = personDoc.getRootElement();
			Element personNameList = root.getChild(PERSON_NAME_LIST);
			List<Element> personNames = personNameList.getChildren(PERSON_NAME);
			
			for (Element nameElement : personNames) {
				nameList.add(nameElement.getAttributeValue(VALUE));
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
		
		Element root = personDoc.getRootElement();
		Element personNameList = root.getChild(PERSON_NAME_LIST);
		List<Element> personNames = personNameList.getChildren(PERSON_NAME);
		for (Element nameElement : personNames ) {
			String personName = nameElement.getAttributeValue(VALUE);
			if (personName.equals(name)) result = nameElement.getAttributeValue(GENDER);
		}
		
		return result;
	}
	
	/**
	 * Gets the oxygen consumption rate.
	 * @return oxygen rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getOxygenConsumptionRate() throws Exception {
		Element root = personDoc.getRootElement();
		Element oxygenRateElement = root.getChild(OXYGEN_CONSUMPTION_RATE);
		String oxygenRateStr = oxygenRateElement.getAttributeValue(VALUE);
		return Double.parseDouble(oxygenRateStr);
	}
	
	/**
	 * Gets the water consumption rate.
	 * @return water rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getWaterConsumptionRate() throws Exception {
		Element root = personDoc.getRootElement();
		Element waterRateElement = root.getChild(WATER_CONSUMPTION_RATE);
		String waterRateStr = waterRateElement.getAttributeValue(VALUE);
		return Double.parseDouble(waterRateStr);
	}
	
	/**
	 * Gets the food consumption rate.
	 * @return food rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getFoodConsumptionRate() throws Exception {
		Element root = personDoc.getRootElement();
		Element foodRateElement = root.getChild(FOOD_CONSUMPTION_RATE);
		String foodRateStr = foodRateElement.getAttributeValue(VALUE);
		return Double.parseDouble(foodRateStr);
	}
	
	/**
	 * Gets the oxygen deprivation time.
	 * @return oxygen time in millisols.
	 * @throws Exception if oxygen deprivation time could not be found.
	 */
	public double getOxygenDeprivationTime() throws Exception {
		Element root = personDoc.getRootElement();
		Element oxygenTimeElement = root.getChild(OXYGEN_DEPRIVATION_TIME);
		String oxygenTimeStr = oxygenTimeElement.getAttributeValue(VALUE);
		return Double.parseDouble(oxygenTimeStr);
	}
	
	/**
	 * Gets the water deprivation time.
	 * @return water time in sols.
	 * @throws Exception if water deprivation time could not be found.
	 */
	public double getWaterDeprivationTime() throws Exception {
		Element root = personDoc.getRootElement();
		Element waterTimeElement = root.getChild(WATER_DEPRIVATION_TIME);
		String waterTimeStr = waterTimeElement.getAttributeValue(VALUE);
		return Double.parseDouble(waterTimeStr);
	}
	
	/**
	 * Gets the food deprivation time.
	 * @return food time in sols.
	 * @throws Exception if food deprivation time could not be found.
	 */
	public double getFoodDeprivationTime() throws Exception {
		Element root = personDoc.getRootElement();
		Element foodTimeElement = root.getChild(FOOD_DEPRIVATION_TIME);
		String foodTimeStr = foodTimeElement.getAttributeValue(VALUE);
		return  Double.parseDouble(foodTimeStr);
		
	}
	
	/**
	 * Gets the starvation start time.
	 * @return starvation time in sols.
	 * @throws Exception if starvation start time could not be found.
	 */
	public double getStarvationStartTime() throws Exception {
		Element root = personDoc.getRootElement();
		Element starvationTimeElement = root.getChild(STARVATION_START_TIME);
		String starvationTimeStr = starvationTimeElement.getAttributeValue(VALUE);
		return Double.parseDouble(starvationTimeStr);
	}
	
	/**
	 * Gets the required air pressure.
	 * @return air pressure in atm.
	 * @throws Exception if air pressure could not be found.
	 */
	public double getMinAirPressure() throws Exception {
		Element root = personDoc.getRootElement();
		Element airPressureElement = root.getChild(MIN_AIR_PRESSURE);
		String airPressureStr = airPressureElement.getAttributeValue(VALUE);
		return  Double.parseDouble(airPressureStr);
	}
	
	/**
	 * Gets the max decompression time a person can survive.
	 * @return decompression time in millisols.
	 * @throws Exception if decompression time could not be found.
	 */
	public double getDecompressionTime() throws Exception {
		Element root = personDoc.getRootElement();
		Element decompressionTimeElement = root.getChild(DECOMPRESSION_TIME);
		String decompressionTimeStr = decompressionTimeElement.getAttributeValue(VALUE);
		return Double.parseDouble(decompressionTimeStr);
	}
	
	/**
	 * Gets the minimum temperature a person can tolerate.
	 * @return temperature in celsius
	 * @throws Exception if min temperature cannot be found.
	 */
	public double getMinTemperature() throws Exception {
		Element root = personDoc.getRootElement();
		Element minTemperatureElement = root.getChild(MIN_TEMPERATURE);
		String minTemperatureStr = minTemperatureElement.getAttributeValue(VALUE);
		return Double.parseDouble(minTemperatureStr);
	}
	
	/**
	 * Gets the maximum temperature a person can tolerate.
	 * @return temperature in celsius
	 * @throws Exception if max temperature cannot be found.
	 */
	public double getMaxTemperature() throws Exception {
		Element root = personDoc.getRootElement();
		Element maxTemperatureElement = root.getChild(MAX_TEMPERATURE);
		String maxTemperatureStr = maxTemperatureElement.getAttributeValue(VALUE);
		return Double.parseDouble(maxTemperatureStr);
	}
	
	/**
	 * Gets the time a person can survive below minimum temperature.
	 * @return freezing time in millisols.
	 * @throws Exception if freezing time could not be found.
	 */
	public double getFreezingTime() throws Exception {
		Element root = personDoc.getRootElement();
		Element freezingTimeElement = root.getChild(FREEZING_TIME);
		String freezingTimeStr = freezingTimeElement.getAttributeValue(VALUE);
		return Double.parseDouble(freezingTimeStr);
	}
	
	/**
	 * Gets the base percent chance that a person will have a stress breakdown when at maximum stress.
	 * @return percent chance of a breakdown per millisol.
	 * @throws Exception if stress breakdown time could not be found.
	 */
	public double getStressBreakdownChance() throws Exception {
		Element root = personDoc.getRootElement();
		Element stressBreakdownChanceElement = root.getChild(STRESS_BREAKDOWN_CHANCE);
		String stressBreakdownChanceStr = stressBreakdownChanceElement.getAttributeValue(VALUE);
		return Double.parseDouble(stressBreakdownChanceStr);
	}
	
	/**
	 * Gets the gender ratio between males and the total population on Mars.
	 * @return gender ratio between males and total population.
	 * @throws Exception if gender ratio could not be found.
	 */
	public double getGenderRatio() throws Exception {
		Element root = personDoc.getRootElement();
		Element genderRatioElement = root.getChild(GENDER_MALE_PERCENTAGE);
		String genderRatioStr = genderRatioElement.getAttributeValue(VALUE);
		return (Double.parseDouble(genderRatioStr) / 100D);
	}
	
	/**
	 * Gets the average percentage for a particular MBTI personality type for settlers.
	 * @param personalityType the MBTI personality type
	 * @return percentage
	 * @throws Exception if personality type could not be found.
	 */
	public double getPersonalityTypePercentage(String personalityType) throws Exception {
		double result = 0D;
		
		Element root = personDoc.getRootElement();
		Element personalityTypeList = root.getChild(PERSONALITY_TYPES);
		List<Element> personalityTypes = personalityTypeList.getChildren(MBTI);
		
		for (Element mbtiElement : personalityTypes) {
			String type = mbtiElement.getAttributeValue(TYPE);
			if (type.equals(personalityType)){
				result = Double.parseDouble(mbtiElement.getAttributeValue(PERCENTAGE));
				break;
			}
		}
		
		return result;		
	}
	
	/**
	 * Gets the number of people configured for the simulation.
	 * @return number of people.
	 * @throws Exception if error in XML parsing.
	 */
	public int getNumberOfConfiguredPeople() throws Exception {
		Element root = personDoc.getRootElement();
		Element personList = root.getChild(PERSON_LIST);
		List personNodes = personList.getChildren(PERSON);
		if (personNodes != null) return personNodes.size();
		else return 0;
	}
	
	/**
	 * Gets the configured person's name.
	 * @param index the person's index.
	 * @return name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonName(int index) throws Exception {
		Element root = personDoc.getRootElement();
		Element personList = root.getChild(PERSON_LIST);
		Element personElement = (Element) personList.getChildren(PERSON).get(index);
		return personElement.getAttributeValue(NAME);
	}
	
	/**
	 * Gets the configured person's gender.
	 * @param index the person's index.
	 * @return "male", "female" or null if not found.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonGender(int index) throws Exception {
		Element root = personDoc.getRootElement();
		Element personList = root.getChild(PERSON_LIST);
		Element personElement = (Element) personList.getChildren(PERSON).get(index);
		return personElement.getAttributeValue(GENDER);
	}
	
	/**
	 * Gets the configured person's MBTI personality type.
	 * @param index the person's index.
	 * @return four character string for MBTI ex. "ISTJ". Return null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonPersonalityType(int index) throws Exception {
		Element root = personDoc.getRootElement();
		Element personList = root.getChild(PERSON_LIST);
		Element personElement = (Element) personList.getChildren(PERSON).get(index);
		return personElement.getAttributeValue(PERSONALITY_TYPE);
	}
	
	/**
	 * Gets the configured person's starting settlement. 
	 * @param index the person's index.
	 * @return the settlement name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonSettlement(int index) throws Exception {
		Element root = personDoc.getRootElement();
		Element personList = root.getChild(PERSON_LIST);
		Element personElement = (Element) personList.getChildren(PERSON).get(index);
		return personElement.getAttributeValue(SETTLEMENT);
	}
	
	/**
	 * Gets the configured person's job.
	 * @param index the person's index.
	 * @return the job name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonJob(int index) throws Exception {
		Element root = personDoc.getRootElement();
		Element personList = root.getChild(PERSON_LIST);
		Element personElement = (Element) personList.getChildren(PERSON).get(index);
		return personElement.getAttributeValue(JOB);
	}
	
	/**
	 * Gets a map of the configured person's natural attributes.
	 * @param index the person's index.
	 * @return map of natural attributes (empty map if not found).
	 * @throws Exception if error in XML parsing.
	 */
	public Map<String, Integer> getNaturalAttributeMap(int index) throws Exception {
		Map<String, Integer> result = new HashMap<String, Integer>();
		Element root = personDoc.getRootElement();
		Element personList = root.getChild(PERSON_LIST);
		Element personElement = (Element) personList.getChildren(PERSON).get(index);
		List<Element> naturalAttributeListNodes = personElement.getChildren(NATURAL_ATTRIBUTE_LIST);
		
		if ((naturalAttributeListNodes != null) && (naturalAttributeListNodes.size() > 0)) {
			Element naturalAttributeList = (Element) naturalAttributeListNodes.get(0);
			int attributeNum = naturalAttributeList.getChildren(NATURAL_ATTRIBUTE).size();
			
			for (int x=0; x < attributeNum; x++) {
				Element naturalAttributeElement = (Element) naturalAttributeList.getChildren(NATURAL_ATTRIBUTE).get(x);
				String name = naturalAttributeElement.getAttributeValue(NAME);
				Integer value = new Integer(naturalAttributeElement.getAttributeValue(VALUE));
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
		Element root = personDoc.getRootElement();
		Element personList = root.getChild(PERSON_LIST);
		Element personElement = (Element) personList.getChildren(PERSON).get(index);
		List<Element> skillListNodes = personElement.getChildren(SKILL_LIST);
		if ((skillListNodes != null) && (skillListNodes.size() > 0)) {
			Element skillList = (Element) skillListNodes.get(0);
			int skillNum = skillList.getChildren(SKILL).size();
			for (int x=0; x < skillNum; x++) {
				Element skillElement = (Element) skillList.getChildren(SKILL).get(x);
				String name = skillElement.getAttributeValue(NAME);
				Integer level = new Integer(skillElement.getAttributeValue(LEVEL));
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
		Element root = personDoc.getRootElement();
		Element personList = root.getChild(PERSON_LIST);
		Element personElement = (Element) personList.getChildren(PERSON).get(index);
		List<Element> relationshipListNodes = personElement.getChildren(RELATIONSHIP_LIST);
		if ((relationshipListNodes != null) && (relationshipListNodes.size() > 0)) {
			Element relationshipList = (Element) relationshipListNodes.get(0);
			int relationshipNum = relationshipList.getChildren(RELATIONSHIP).size();
			for (int x=0; x < relationshipNum; x++) {
				Element relationshipElement = (Element) relationshipList.getChildren(RELATIONSHIP).get(x);
				String personName = relationshipElement.getAttributeValue(PERSON_NAME);
				Integer opinion = new Integer(relationshipElement.getAttributeValue(OPINION));
				result.put(personName, opinion);
			}
		}
		return result;
	}
}