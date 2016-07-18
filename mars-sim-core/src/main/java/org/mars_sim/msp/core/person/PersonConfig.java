/**
 * Mars Simulation Project
 * PersonConfig.java
 * @version 3.08 2016-03-01
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Provides configuration information about people units.
 * Uses a JDOM document to get the information.
 */
public class PersonConfig
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	public static final int SIZE_OF_CREW = 4;
	public static final int ALPHA_CREW = 0; ;

	// 2016-06-25 Added a list of crew 
	private List<Crew> roster = new ArrayList<>();
	
	// Element names
	private static final String LAST_NAME_LIST = "last-name-list";
	private static final String FIRST_NAME_LIST = "first-name-list";
	private static final String LAST_NAME = "last-name";
	private static final String FIRST_NAME = "first-name";
	private static final String PERSON_NAME_LIST = "person-name-list";
	private static final String PERSON_NAME = "person-name";
	
	private static final String GENDER = "gender";
	
	private static final String SPONSOR = "sponsor";
	
	private static final String LOW_O2_RATE = "low-activity-metaboic-load-o2-consumption-rate";
	private static final String NOMINAL_O2_RATE = "low-activity-metaboic-load-o2-consumption-rate";
	private static final String HIGH_O2_RATE = "low-activity-metaboic-load-o2-consumption-rate";	

	private static final String WATER_CONSUMPTION_RATE = "water-consumption-rate";
	private static final String WATER_USAGE_RATE = "water-usage-rate";
	private static final String	GREY_TO_BLACK_WATER_RATIO = "grey-to-black-water-ratio";

	private static final String FOOD_CONSUMPTION_RATE = "food-consumption-rate";
	private static final String DESSERT_CONSUMPTION_RATE = "dessert-consumption-rate";
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
	private static final String HIGH_FATIGUE_COLLAPSE = "high-fatigue-collapse-chance";

	private static final String GENDER_MALE_PERCENTAGE = "gender-male-percentage";
	private static final String PERSONALITY_TYPES = "personality-types";
	private static final String MBTI = "mbti";
	private static final String PERSONALITY_TYPE = "personality-type";
	//2015-11-22 Added PERSONALITY_TRAIT_LIST and PERSONALITY_TRAIT
	private static final String PERSONALITY_TRAIT_LIST = "personality-trait-list";
	private static final String PERSONALITY_TRAIT = "personality-trait";
	// 2014-10-14 Changed element from "alpha-team" to "person-list" , from alphaTeam to personList
	//private static final String ALPHA_TEAM = "alpha-team";
	private static final String PERSON_LIST = "person-list";
	private static final String PERSON = "person";
	// 2016-06-26 Added CREW
	private static final String CREW = "crew";
	private static final String NAME = "name";
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
	// 2015-02-27 Added MAIN_DISH and SIDE_DISH
	private static final String MAIN_DISH = "favorite-main-dish";
	private static final String SIDE_DISH = "favorite-side-dish";
	// 2015-03-24 Added DESSERT
	private static final String DESSERT = "favorite-dessert";
	private static final String ACTIVITY = "favorite-activity";
	
	private Document personDoc;
	private List<String> personNameList;

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
    //@SuppressWarnings("unchecked")
	public List<String> getPersonNameList() {

		if (personNameList == null) {
			personNameList = new ArrayList<String>();
			Element root = personDoc.getRootElement();
			Element personNameEl = root.getChild(PERSON_NAME_LIST);
			List<Element> personNames = personNameEl.getChildren(PERSON_NAME);

			for (Element nameElement : personNames) {
				personNameList.add(nameElement.getAttributeValue(VALUE));
			}
		}

		return personNameList;
	}

	/**
	 * Gets a list of first names for settlers.
	 * @return List of first names.
	 * @throws Exception if first names could not be found.
	 
    //@SuppressWarnings("unchecked")
    // 2016-04-06 Added getFirstNameList()
	public List<String> getFirstNameList() {

		if (nameList == null) {
			nameList = new ArrayList<String>();
			Element root = personDoc.getRootElement();
			Element personNameList = root.getChild(FIRST_NAME_LIST);
			List<Element> personNames = personNameList.getChildren(FIRST_NAME);

			for (Element nameElement : personNames) {
				nameList.add(nameElement.getAttributeValue(VALUE));
			}
		}

		//System.out.println("done with getFirstNameList()");		
		return nameList;
	}
*/
    
	/**
	 * Gets a list of last names for settlers.
	 * @return List of last names.
	 * @throws Exception if last names could not be found.
	 */
    //@SuppressWarnings("unchecked")
    // 2016-04-06 Added getLastNameList()
	public Map<Integer, List<String>> getLastNameList(ReportingAuthorityType[] type) {

		Map<Integer, List<String>> lastNames = new HashMap<>();
		List<List<String>> lists = new ArrayList<>();
		for (int i= 0; i<7; i++){
	    	List<String> list = new ArrayList<String>();
			lists.add(list);
		}
    	
    	Element root = personDoc.getRootElement();
		Element lastNameEl = root.getChild(LAST_NAME_LIST);
		List<Element> lastNamesEl = lastNameEl.getChildren(LAST_NAME);

		for (Element nameElement : lastNamesEl) {
			
			String sponsor = nameElement.getAttributeValue(SPONSOR);
		
			for (int i=0; i<7; i++) {
				if (sponsor.equals("CNSA") && type[i] == ReportingAuthorityType.CNSA)
					lists.get(0).add(nameElement.getAttributeValue(VALUE));		
				else if (sponsor.equals("CSA") && type[i] == ReportingAuthorityType.CSA)
					lists.get(1).add(nameElement.getAttributeValue(VALUE));		
				else if (sponsor.equals("ESA") && type[i] == ReportingAuthorityType.ESA)
					lists.get(2).add(nameElement.getAttributeValue(VALUE));			
				else if (sponsor.equals("ISRO") && type[i] == ReportingAuthorityType.ISRO)
					lists.get(3).add(nameElement.getAttributeValue(VALUE));			
				else if (sponsor.equals("JAXA") && type[i] == ReportingAuthorityType.JAXA)
					lists.get(4).add(nameElement.getAttributeValue(VALUE));				
				else if (sponsor.equals("NASA") && type[i] == ReportingAuthorityType.NASA)
					lists.get(5).add(nameElement.getAttributeValue(VALUE));			
				else if (sponsor.equals("RKA") && type[i] == ReportingAuthorityType.RKA)
					lists.get(6).add(nameElement.getAttributeValue(VALUE));
			}
		}
		
		for (int i= 0; i<7; i++){
			lastNames.put(i, lists.get(i));
		}
		
		//System.out.println("done with getLastNameList()");	
		return lastNames;
	}


	/**
	 * Gets a list of first names for settlers.
	 * @return List of first names.
	 * @throws Exception if first names could not be found.
	 */
    //@SuppressWarnings("unchecked")
    // 2016-04-06 Added getFirstNameList()
	public Map<Integer, Map<Integer, List<String>>> getFirstNameList(ReportingAuthorityType[] type) {//, boolean isMale) {

		Map<Integer, Map<Integer, List<String>>> firstNames = new HashMap<>();
		Map<Integer, List<String>> maleFirstNames = new HashMap<>();
		Map<Integer, List<String>> femaleFirstNames = new HashMap<>();

		List<List<String>> mlists = new ArrayList<>();
		for (int i= 0; i<7; i++){
	    	List<String> list = new ArrayList<String>();
			mlists.add(list);
		}

		List<List<String>> flists = new ArrayList<>();
		for (int i= 0; i<7; i++){
	    	List<String> list = new ArrayList<String>();
			flists.add(list);
		}
		
    	//List<String> nameList = new ArrayList<String>();
		Element root = personDoc.getRootElement();
		Element firstNameEl = root.getChild(FIRST_NAME_LIST);
		List<Element> firstNamesEl = firstNameEl.getChildren(FIRST_NAME);

		for (Element nameElement : firstNamesEl) {

			String gender = nameElement.getAttributeValue(GENDER);
			String sponsor = nameElement.getAttributeValue(SPONSOR);
		
			if (gender.equals("male")) {
				
				for (int i=0; i<7; i++) {

					if (sponsor.equals("CNSA") && type[i] == ReportingAuthorityType.CNSA)
						mlists.get(0).add(nameElement.getAttributeValue(VALUE));
					
					else if (sponsor.equals("CSA") && type[i] == ReportingAuthorityType.CSA)
						mlists.get(1).add(nameElement.getAttributeValue(VALUE));
					
					else if (sponsor.equals("ESA") && type[i] == ReportingAuthorityType.ESA)
						mlists.get(2).add(nameElement.getAttributeValue(VALUE));
						
					else if (sponsor.equals("ISRO") && type[i] == ReportingAuthorityType.ISRO)
						mlists.get(3).add(nameElement.getAttributeValue(VALUE));
						
					else if (sponsor.equals("JAXA") && type[i] == ReportingAuthorityType.JAXA)
						mlists.get(4).add(nameElement.getAttributeValue(VALUE));	
						
					else if (sponsor.equals("NASA") && type[i] == ReportingAuthorityType.NASA)
						mlists.get(5).add(nameElement.getAttributeValue(VALUE));
						
					else if (sponsor.equals("RKA") && type[i] == ReportingAuthorityType.RKA)
						mlists.get(6).add(nameElement.getAttributeValue(VALUE));
				
				}
				
			} else if (gender.equals("female")) {
				
				for (int i=0; i<7; i++) {

					if (sponsor.equals("CNSA") && type[i] == ReportingAuthorityType.CNSA)
						flists.get(0).add(nameElement.getAttributeValue(VALUE));
					
					else if (sponsor.equals("CSA") && type[i] == ReportingAuthorityType.CSA)
						flists.get(1).add(nameElement.getAttributeValue(VALUE));
					
					else if (sponsor.equals("ESA") && type[i] == ReportingAuthorityType.ESA)
						flists.get(2).add(nameElement.getAttributeValue(VALUE));
						
					else if (sponsor.equals("ISRO") && type[i] == ReportingAuthorityType.ISRO)
						flists.get(3).add(nameElement.getAttributeValue(VALUE));
						
					else if (sponsor.equals("JAXA") && type[i] == ReportingAuthorityType.JAXA)
						flists.get(4).add(nameElement.getAttributeValue(VALUE));	
						
					else if (sponsor.equals("NASA") && type[i] == ReportingAuthorityType.NASA)
						flists.get(5).add(nameElement.getAttributeValue(VALUE));
						
					else if (sponsor.equals("RKA") && type[i] == ReportingAuthorityType.RKA)
						flists.get(6).add(nameElement.getAttributeValue(VALUE));
				}
			}
		}

		for (int i= 0; i<7; i++){
			maleFirstNames.put(i, mlists.get(i));
			femaleFirstNames.put(i, mlists.get(i));
		}
		
		firstNames.put(0, maleFirstNames);
		firstNames.put(1, femaleFirstNames);
		
		return firstNames;
	}
    
	/**
	 * Gets the sponsor of a given person name.
	 * @param name the name of the person
	 * @return the sponsor of the person 
	 */
	//2015-11-29 Added getPersonSponsor
    @SuppressWarnings("unchecked")
	public ReportingAuthorityType getPersonSponsor(String name) {
    	ReportingAuthorityType type = null;
    	
    	Element root = personDoc.getRootElement();
    	
		Element personNameList = root.getChild(PERSON_NAME_LIST);
		List<Element> personNames = personNameList.getChildren(PERSON_NAME);
		for (Element nameElement : personNames ) {
			String personName = nameElement.getAttributeValue(VALUE);
			String sponsor = null;
			if (personName.equals(name))  {
				sponsor = nameElement.getAttributeValue(SPONSOR);
/*		
				if (sponsor.equals("CNSA"))
					type = ReportingAuthorityType.CNSA;
	
				else if (sponsor.equals("CSA"))
					type = ReportingAuthorityType.CSA;
	
				else if (sponsor.equals("ESA"))				
					type = ReportingAuthorityType.ESA;
				
				else if (sponsor.equals("ISRO"))
					type = ReportingAuthorityType.ISRO;
	
				else if (sponsor.equals("JAXA"))				
					type = ReportingAuthorityType.JAXA;
*/				
				if (sponsor.equals("Mars Society"))				
					type = ReportingAuthorityType.MARS_SOCIETY;
/*				
				else if (sponsor.equals("NASA"))
					type = ReportingAuthorityType.NASA;
	
				else if (sponsor.equals("RKA"))				
					type = ReportingAuthorityType.RKA;
*/
			}
				
		}	
			
		return type;
	}
    

	/**
	 * Gets the gender of a given person name.
	 * @param name the name of the person
	 * @return {@link PersonGender} the gender of the person name
	 * @throws Exception if person names could not be found.
	 */
	@SuppressWarnings("unchecked")
	public PersonGender getPersonGender(String name) {
		PersonGender result = PersonGender.UNKNOWN;

		Element root = personDoc.getRootElement();
		Element personNameList = root.getChild(PERSON_NAME_LIST);
		List<Element> personNames = personNameList.getChildren(PERSON_NAME);
		for (Element nameElement : personNames ) {
			String personName = nameElement.getAttributeValue(VALUE);
			if (personName.equals(name)) result = PersonGender.valueOfIgnoreCase(nameElement.getAttributeValue(GENDER));
		}

		return result;
	}

	/**
	 * Gets the nominal oxygen consumption rate.
	 * @return oxygen rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	// 2016-03-31 Added getNominalO2Rate()
	public double getNominalO2Rate() {
		return getValueAsDouble(NOMINAL_O2_RATE);
	}

	/**
	 * Gets the low oxygen consumption rate.
	 * @return oxygen rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	// 2016-03-31 Added getLowO2Rate()
	public double getLowO2Rate() {
		return getValueAsDouble(NOMINAL_O2_RATE);
	}
	
	/**
	 * Gets the high oxygen consumption rate.
	 * @return oxygen rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	// 2016-03-31 Added getHIghO2Rate()
	public double getHighO2Rate() {
		return getValueAsDouble(NOMINAL_O2_RATE);
	}
	
	/**
	 * Gets the water consumption rate.
	 * @return water rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getWaterConsumptionRate() {
		return getValueAsDouble(WATER_CONSUMPTION_RATE);
	}

	/**
	 * Gets the water usage rate.
	 * @return water rate (kg/sol)
	 * @throws Exception if usage rate could not be found.
	 */
	// 2015-12-04 Added getWaterUsageRate()
	public double getWaterUsageRate() {
		return getValueAsDouble(WATER_USAGE_RATE);
	}

	/**
	 * Gets the grey to black water ratio.
	 * @return ratio 
	 * @throws Exception if the ratio could not be found.
	 */
	// 2015-12-04 Added getGrey2BlackWaterRatio()
	public double getGrey2BlackWaterRatio() {
		return getValueAsDouble(GREY_TO_BLACK_WATER_RATIO);
	}

	
	/**
	 * Gets the food consumption rate.
	 * @return food rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getFoodConsumptionRate() {
		return getValueAsDouble(FOOD_CONSUMPTION_RATE);
	}

	/**
	 * Gets the dessert consumption rate.
	 * @return dessert rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getDessertConsumptionRate() {
		return getValueAsDouble(DESSERT_CONSUMPTION_RATE);
	}
	
	/**
	 * Gets the oxygen deprivation time.
	 * @return oxygen time in millisols.
	 * @throws Exception if oxygen deprivation time could not be found.
	 */
	public double getOxygenDeprivationTime() {
		return getValueAsDouble(OXYGEN_DEPRIVATION_TIME);
	}

	/**
	 * Gets the water deprivation time.
	 * @return water time in sols.
	 * @throws Exception if water deprivation time could not be found.
	 */
	public double getWaterDeprivationTime() {
		return getValueAsDouble(WATER_DEPRIVATION_TIME);
	}

	/**
	 * Gets the food deprivation time.
	 * @return food time in sols.
	 * @throws Exception if food deprivation time could not be found.
	 */
	public double getFoodDeprivationTime() {
		return getValueAsDouble(FOOD_DEPRIVATION_TIME);
	}

	/**
	 * Gets the starvation start time.
	 * @return starvation time in sols.
	 * @throws Exception if starvation start time could not be found.
	 */
	public double getStarvationStartTime() {
		return getValueAsDouble(STARVATION_START_TIME);
	}

	/**
	 * Gets the required air pressure.
	 * @return air pressure in Pa.
	 * @throws Exception if air pressure could not be found.
	 */
	public double getMinAirPressure() {
		return getValueAsDouble(MIN_AIR_PRESSURE);
	}

	/**
	 * Gets the max decompression time a person can survive.
	 * @return decompression time in millisols.
	 * @throws Exception if decompression time could not be found.
	 */
	public double getDecompressionTime() {
		return getValueAsDouble(DECOMPRESSION_TIME);
	}

	/**
	 * Gets the minimum temperature a person can tolerate.
	 * @return temperature in celsius
	 * @throws Exception if min temperature cannot be found.
	 */
	public double getMinTemperature() {
		return getValueAsDouble(MIN_TEMPERATURE);
	}

	/**
	 * Gets the maximum temperature a person can tolerate.
	 * @return temperature in celsius
	 * @throws Exception if max temperature cannot be found.
	 */
	public double getMaxTemperature() {
		return getValueAsDouble(MAX_TEMPERATURE);
	}

	/**
	 * Gets the time a person can survive below minimum temperature.
	 * @return freezing time in millisols.
	 * @throws Exception if freezing time could not be found.
	 */
	public double getFreezingTime() {
		return getValueAsDouble(FREEZING_TIME);
	}

	/**
	 * Gets the base percent chance that a person will have a stress breakdown when at maximum stress.
	 * @return percent chance of a breakdown per millisol.
	 * @throws Exception if stress breakdown time could not be found.
	 */
	public double getStressBreakdownChance() {
		return getValueAsDouble(STRESS_BREAKDOWN_CHANCE);
	}
	
	/**
	 * Gets the base percent chance that a person will collapse under high fatigue.
	 * @return percent chance of a collapse per millisol.
	 * @throws Exception if collapse time could not be found.
	 */
	public double getHighFatigueCollapseChance() {
		return getValueAsDouble(HIGH_FATIGUE_COLLAPSE);
	}


	/**
	 * Gets the gender ratio between males and the total population on Mars.
	 * @return gender ratio between males and total population.
	 * @throws Exception if gender ratio could not be found.
	 */
	public double getGenderRatio() {
		return getValueAsDouble(GENDER_MALE_PERCENTAGE) / 100D;
	}

	/**
	 * Gets the average percentage for a particular MBTI personality type for settlers.
	 * @param personalityType the MBTI personality type
	 * @return percentage
	 * @throws Exception if personality type could not be found.
	 */
    @SuppressWarnings("unchecked")
	public double getPersonalityTypePercentage(String personalityType) {
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
	public int getNumberOfConfiguredPeople() {
		Element root = personDoc.getRootElement();
		Element personList = root.getChild(PERSON_LIST);
		List personNodes = personList.getChildren(PERSON);
		if (personNodes != null) return personNodes.size();
		else return 0;
	}

	
	/**
	 * Get person's crew designation 
	 * @param index the person's index.
	 * @return name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public int getCrew(int index) {
		// retrieve the person's crew designation
		String crewString = getValueAsString(index, CREW);
		
		if (crewString == null) {			
			throw new IllegalStateException("The crew designation of a person is null");
			
		}
		else { 
				
			boolean oldCrew = false;
			
			Iterator<Crew> i = roster.iterator();
			while (i.hasNext()) {
				Crew crew = i.next();
				// if the name does not exist, create a new crew with this name
				if (crewString.equals(crew.getName())) {				
					oldCrew = true;						
					// add a new member
					//Member m = new Member();
					crew.add(new Member());				
					break;
				}
			}
	
			// if this is crew name doesn't exist
			if (!oldCrew) {
				Crew c = new Crew(crewString);
				c.add(new Member());
				roster.add(c);
			}		
			
			//System.out.println("crewString : " + crewString);	
			
			return roster.size() - 1;
		}
		
	}
	
	/**
	 * Gets the configured person's name.
	 * @param index the person's index.
	 * @return name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonName(int index, int crew_id) {
		//System.out.println("roster.get(crew_id) : " + roster.get(crew_id));
		//System.out.println("roster.get(crew_id).getTeam().get(index) : " + roster.get(crew_id).getTeam().get(index));		
		//System.out.println("name : " + roster.get(crew_id).getTeam().get(index).getName());		

		if (roster.get(crew_id).getTeam().get(index).getName() != null)		
			return roster.get(crew_id).getTeam().get(index).getName();   //alphaCrewName.get(index) ;
		else {		
			String name = getValueAsString(index,NAME);
			//System.out.println("name : " + name);
			return name;
			//return getValueAsString(index,NAME);
		}
	}

	/**
	 * Gets the configured person's gender.
	 * @param index the person's index.
	 * @return {@link PersonGender} or null if not found.
	 * @throws Exception if error in XML parsing.
	 */
	public PersonGender getConfiguredPersonGender(int index, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getGender() != null)
			return PersonGender.valueOfIgnoreCase(roster.get(crew_id).getTeam().get(index).getGender());//alphaCrewGender.get(index)) ;
		else 
			return PersonGender.valueOfIgnoreCase(getValueAsString(index,GENDER));
	}

	/**
	 * Gets the configured person's MBTI personality type.
	 * @param index the person's index.
	 * @return four character string for MBTI ex. "ISTJ". Return null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonPersonalityType(int index, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getmbti() != null)
			return roster.get(crew_id).getTeam().get(index).getmbti();//alphaCrewPersonality.get(index) ;
		else
			return getValueAsString(index,PERSONALITY_TYPE);
	}

	
	/**
	 * Gets the configured person's job.
	 * @param index the person's index.
	 * @return the job name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonJob(int index, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getJob() != null)
			return roster.get(crew_id).getTeam().get(index).getJob();//alphaCrewJob.get(index) ;
		else
			return getValueAsString(index,JOB);
	}

	/**
	 * Gets the configured person's starting settlement.
	 * @param index the person's index.
	 * @return the settlement name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonDestination(int index, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getDestination() != null)
			return roster.get(crew_id).getTeam().get(index).getDestination();//alphaCrewDestination.get(index);
		else 
			return getValueAsString(index,SETTLEMENT);
	}

	/*
	 * Sets the name of a member of the alpha crew
	 * @param index
	 * @param name
	 */
	public void setPersonName(int index, String value, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getName() == null) 
			roster.get(crew_id).getTeam().get(index).setName(value);//alphaCrewName = new ArrayList<String>(SIZE_OF_CREW);
		
		//if (alphaCrewName.size() == SIZE_OF_CREW) {
		//	alphaCrewName.set(index, value);
		//} else
		//	alphaCrewName.add(value);
		
	}

	/*
	 * Sets the personality of a member of the alpha crew
	 * @param index
	 * @param personality 
	 */
	public void setPersonPersonality(int index, String value, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getmbti() == null) 
			roster.get(crew_id).getTeam().get(index).setmbti(value);
		
		
		//if (alphaCrewPersonality == null)  
		//	alphaCrewPersonality = new ArrayList<String>(SIZE_OF_CREW);
		//if (alphaCrewPersonality.size() == SIZE_OF_CREW) {
		//	alphaCrewPersonality.set(index, value);
		//} else
		//	alphaCrewPersonality.add(value);
	}

	/*
	 * Sets the gender of a member of the alpha crew
	 * @param index
	 * @param gender 
	 */
	public void setPersonGender(int index, String value, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getGender() == null) 
			roster.get(crew_id).getTeam().get(index).setGender(value);
		
		
		//if (alphaCrewGender == null)  
		//	alphaCrewGender = new ArrayList<String>(SIZE_OF_CREW);
		//if (alphaCrewGender.size() == SIZE_OF_CREW) {
		//	alphaCrewGender.set(index, value);
		//} else
		//	alphaCrewGender.add(value);
	}

	/*
	 * Sets the job of a member of the alpha crew
	 * @param index
	 * @param job 
	 */
	public void setPersonJob(int index,String value, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getJob() == null) 
			roster.get(crew_id).getTeam().get(index).setJob(value);
		
		
		//if (alphaCrewJob == null)  
		//	alphaCrewJob = new ArrayList<String>(SIZE_OF_CREW);
		//if (alphaCrewJob.size() == SIZE_OF_CREW) {
		//	alphaCrewJob.set(index, value);
		//} else
		//	alphaCrewJob.add(value);
	}
	
	/*
	 * Sets the destination of a member of the alpha crew
	 * @param index
	 * @param destination 
	 */
	public void setPersonDestination(int index, String value, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getDestination() == null) 
			roster.get(crew_id).getTeam().get(index).setDestination(value);
				
		
		//if (alphaCrewDestination == null)  
		//	alphaCrewDestination = new ArrayList<String>(SIZE_OF_CREW);
		//if (alphaCrewDestination.size() == SIZE_OF_CREW) {
		//	alphaCrewDestination.set(index, value);
		//} else
		//	alphaCrewDestination.add(value);
	}
	
	/**
	 * Gets a map of the configured person's natural attributes.
	 * @param index the person's index.
	 * @return map of natural attributes (empty map if not found).
	 * @throws Exception if error in XML parsing.
	 */
    @SuppressWarnings("unchecked")
	public Map<String, Integer> getNaturalAttributeMap(int index) {
		Map<String, Integer> result = new HashMap<String, Integer>();
		Element root = personDoc.getRootElement();
		Element personList = root.getChild(PERSON_LIST);
		Element personElement = (Element) personList.getChildren(PERSON).get(index);
		List<Element> naturalAttributeListNodes = personElement.getChildren(NATURAL_ATTRIBUTE_LIST);

		if ((naturalAttributeListNodes != null) && (naturalAttributeListNodes.size() > 0)) {
			Element naturalAttributeList = naturalAttributeListNodes.get(0);
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
	 * Gets a map of the configured person's traits according to the Big Five Model.
	 * @param index the person's index.
	 * @return map of Big Five Model (empty map if not found).
	 * @throws Exception if error in XML parsing.
	 */
    @SuppressWarnings("unchecked")
	public Map<String, Integer> getBigFiveMap(int index) {
		Map<String, Integer> result = new HashMap<String, Integer>();
		Element root = personDoc.getRootElement();
		Element personList = root.getChild(PERSON_LIST);
		Element personElement = (Element) personList.getChildren(PERSON).get(index);
		List<Element> listNodes = personElement.getChildren(PERSONALITY_TRAIT_LIST);

		if ((listNodes != null) && (listNodes.size() > 0)) {
			Element list = listNodes.get(0);
			int attributeNum = list.getChildren(PERSONALITY_TRAIT_LIST).size();

			for (int x=0; x < attributeNum; x++) {
				Element naturalAttributeElement = (Element) list.getChildren(PERSONALITY_TRAIT).get(x);
				String name = naturalAttributeElement.getAttributeValue(NAME);
				Integer value = new Integer(naturalAttributeElement.getAttributeValue(VALUE));
				result.put(name, value);
			}
		}
		return result;
	}
    
	/*
	 * Gets the value of an element as a String
	 * @param an element
	 * @param an index
	 * @return a String
	 */
	private String getValueAsString(int index, String param){
		Element root = personDoc.getRootElement();
		Element personList = root.getChild(PERSON_LIST);
		Element personElement = (Element) personList.getChildren(PERSON).get(index);
		return personElement.getAttributeValue(param);
	}

	/*
	 * Gets the value of an element as a double
	 * @param an element
	 * @return a double 
	 */
	private double getValueAsDouble(String child) {
		Element root = personDoc.getRootElement();
		Element element = root.getChild(child);
		String str = element.getAttributeValue(VALUE);
		return Double.parseDouble(str);
	}
	
	/**
	 * Gets a map of the configured person's skills.
	 * @param index the person's index.
	 * @return map of skills (empty map if not found).
	 * @throws Exception if error in XML parsing.
	 */
    @SuppressWarnings("unchecked")
	public Map<String, Integer> getSkillMap(int index) {
		Map<String, Integer> result = new HashMap<String, Integer>();
		Element root = personDoc.getRootElement();
		// 2014-10-07 mkung: changed the people.xml element from "person-list" to "alpha-team"
		Element personList = root.getChild(PERSON_LIST);
		Element personElement = (Element) personList.getChildren(PERSON).get(index);
		List<Element> skillListNodes = personElement.getChildren(SKILL_LIST);
		if ((skillListNodes != null) && (skillListNodes.size() > 0)) {
			Element skillList = skillListNodes.get(0);
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
    @SuppressWarnings("unchecked")
	public Map<String, Integer> getRelationshipMap(int index) {
		Map<String, Integer> result = new HashMap<String, Integer>();
		Element root = personDoc.getRootElement();
		Element personList = root.getChild(PERSON_LIST);
		Element personElement = (Element) personList.getChildren(PERSON).get(index);
		List<Element> relationshipListNodes = personElement.getChildren(RELATIONSHIP_LIST);
		if ((relationshipListNodes != null) && (relationshipListNodes.size() > 0)) {
			Element relationshipList = relationshipListNodes.get(0);
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

	
	/**
	 * Gets the configured person's favorite main dish.
	 * @param index the person's index.
	 * @return the name of the favorite main dish name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getFavoriteMainDish(int index, int crew_id) {
			
		if (roster.get(crew_id).getTeam().get(index).getMainDish() != null)
			return roster.get(crew_id).getTeam().get(index).getMainDish() ;
		else
			return getValueAsString(index,MAIN_DISH);
	}
	
	/**
	 * Gets the configured person's favorite side dish.
	 * @param index the person's index.
	 * @return the name of the favorite side dish name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getFavoriteSideDish(int index, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getSideDish() != null)
			return roster.get(crew_id).getTeam().get(index).getSideDish() ;
		else
			return getValueAsString(index,SIDE_DISH);
	}

	/**
	 * Gets the configured person's favorite dessert.
	 * @param index the person's index.
	 * @return the name of the favorite dessert name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getFavoriteDessert(int index, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getDessert() != null)
			return roster.get(crew_id).getTeam().get(index).getDessert() ;
		else
			return getValueAsString(index,DESSERT);
	}
  
	/**
	 * Gets the configured person's favorite activity.
	 * @param index the person's index.
	 * @return the name of the favorite activity name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getFavoriteActivity(int index, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getActivity() != null)
			return roster.get(crew_id).getTeam().get(index).getActivity();
		else
			return getValueAsString(index,ACTIVITY);
	}
	
    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
        personDoc = null;
        if(personNameList != null){

            personNameList.clear();
            personNameList = null;
        }
    }
}
