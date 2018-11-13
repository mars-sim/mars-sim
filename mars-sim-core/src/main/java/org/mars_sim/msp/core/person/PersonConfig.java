/**
 * Mars Simulation Project
 * PersonConfig.java
 * @version 3.1.0 2017-01-24
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
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;
import org.mars_sim.msp.core.terminal.Commander;

/**
 * Provides configuration information about people units. Uses a JDOM document
 * to get the information.
 */
public class PersonConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// private static Logger logger =
	// Logger.getLogger(PersonConfig.class.getName());

	public static final int SIZE_OF_CREW = 4;
	public static final int ALPHA_CREW = 0;;

	// Add a list of crew
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
	private static final String COUNTRY = "country";

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

	private static final String PERSONALITY_TRAIT_LIST = "personality-trait-list";
	private static final String PERSONALITY_TRAIT = "personality-trait";

	private static final String PERSON_LIST = "person-list";
	private static final String PERSON = "person";

	private static final String CREW = "crew";
	private static final String NAME = "name";
	private static final String SETTLEMENT = "settlement";
	private static final String JOB = "job";
	private static final String NATURAL_ATTRIBUTE_LIST = "natural-attribute-list";
	private static final String NATURAL_ATTRIBUTE = "natural-attribute";
	private static final String TYPE = "type";
	private static final String VALUE = "value";
	private static final String SKILL_LIST = "skill-list";
	private static final String SKILL = "skill";
	private static final String LEVEL = "level";
	private static final String RELATIONSHIP_LIST = "relationship-list";
	private static final String RELATIONSHIP = "relationship";
	private static final String OPINION = "opinion";
	private static final String PERCENTAGE = "percentage";

	private static final String MAIN_DISH = "favorite-main-dish";
	private static final String SIDE_DISH = "favorite-side-dish";

	private static final String DESSERT = "favorite-dessert";
	private static final String ACTIVITY = "favorite-activity";

	// for 3 types of metabolic loads
	private double[] o2ConsumptionRate = new double[] { 0, 0, 0 };
	// for water, dessert, food
	private double[] consumptionRates = new double[] { 0, 0, 0 };
	// for grey2BlackWaterRatio, gender ratio
	private double[] ratio = new double[] { 0, 0 };
	// for stress breakdown and high fatigue collapse chance
	private double[] chance = new double[] { 0, 0 };
	// for various time values
	private double[] time = new double[] { 0, 0, 0, 0, 0, 0, 0 };
	// for min and max temperature
	private double[] temperature = new double[] { 0, 0 };

	private double waterUsage = 0;

	private double pressure = 0;

	private Document personDoc;
	private Element root;

	private Map<String, Double> personalityDistribution;

	private List<String> personNameList;
	private List<String> countries;
	private List<String> ESAcountries;
	private List<String> sponsors;
	private List<String> longSponsors;
	
	private List<Map<Integer, List<String>>> lastNames;
	private List<Map<Integer, List<String>>> firstNames;

	private Commander commander;

	/**
	 * Constructor
	 * 
	 * @param personDoc the person config DOM document.
	 */
	public PersonConfig(Document personDoc) {
		// logger.info("PersonConfig's constructor is on " +
		// Thread.currentThread().getName());

		commander = new Commander();

		this.personDoc = personDoc;

		root = personDoc.getRootElement();

		getPersonNameList();
		retrieveLastNameList();
		retrieveFirstNameList();
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
			root = personDoc.getRootElement();
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
	 * 
	 * @return List of first names.
	 * @throws Exception if first names could not be found.
	 */
//	public List<String> getFirstNameList() {
//
//		if (nameList == null) {
//			nameList = new ArrayList<String>();
//			//Element root = personDoc.getRootElement();
//			Element personNameList = root.getChild(FIRST_NAME_LIST);
//			List<Element> personNames = personNameList.getChildren(FIRST_NAME);
//
//			for (Element nameElement : personNames) {
//				nameList.add(nameElement.getAttributeValue(VALUE));
//			}
//		}
//
//		//System.out.println("done with getFirstNameList()");
//		return nameList;
//	}

	/**
	 * Retrieves a list of settlers' last names by sponsors and by countries.
	 * 
	 * @return List of last names.
	 * @throws Exception if last names could not be found.
	 */
	public List<Map<Integer, List<String>>> retrieveLastNameList() {

		if (lastNames == null) {
			lastNames = new ArrayList<Map<Integer, List<String>>>();

			List<List<String>> sponsors = new ArrayList<>();
			for (int i = 0; i < 7; i++) {
				List<String> list = new ArrayList<String>();
				sponsors.add(list);
			}

			// Add lists for countries
			List<List<String>> countries = new ArrayList<>();
			for (int i = 0; i < 28; i++) {
				List<String> countryList = new ArrayList<String>();
				countries.add(countryList);
			}

			// Element root = personDoc.getRootElement();
			Element lastNameEl = root.getChild(LAST_NAME_LIST);
			List<Element> lastNamesList = lastNameEl.getChildren(LAST_NAME);

			for (Element nameElement : lastNamesList) {

				String sponsor = nameElement.getAttributeValue(SPONSOR);
				String name = nameElement.getAttributeValue(VALUE);
				String country = nameElement.getAttributeValue(COUNTRY);

				if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.CNSA
						|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.CNSA_L)
					sponsors.get(0).add(name);
				else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.CSA
						|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.CSA_L)
					sponsors.get(1).add(name);
				else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.ESA
						|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.ESA_L)
					sponsors.get(2).add(name);
				else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.ISRO
						|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.ISRO_L)
					sponsors.get(3).add(name);
				else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.JAXA
						|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.JAXA_L)
					sponsors.get(4).add(name);
				else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.NASA
						|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.NASA_L)
					sponsors.get(5).add(name);
				else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.RKA
						|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.RKA_L)
					sponsors.get(6).add(name);
				else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.MARS_SOCIETY
						|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.MARS_SOCIETY_L)
					sponsors.get(7).add(name);
				else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.SPACEX
						|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.SPACEX_L)
					sponsors.get(8).add(name);

				/* CNSA,CSA,ISRO,JAXA,NASA,RKA */
				if (country.equals("China"))
					countries.get(0).add(name);
				else if (country.equals("Canada"))
					countries.get(1).add(name);
				else if (country.equals("India"))
					countries.get(2).add(name);
				else if (country.equals("Japan"))
					countries.get(3).add(name);
				else if (country.equals("USA"))
					countries.get(4).add(name);
				else if (country.equals("Russia"))
					countries.get(5).add(name);

				/*
				 * ESA has 22 Member States. The national bodies responsible for space in these
				 * countries sit on ESA�s governing Council: Austria, Belgium, Czech Republic,
				 * Denmark, Estonia, Finland, France, Germany, Greece, Hungary, Ireland, Italy,
				 * Luxembourg, The Netherlands, Norway, Poland, Portugal, Romania, Spain,
				 * Sweden, Switzerland and the United Kingdom.
				 */
				else if (country.equals("Austria"))
					countries.get(6).add(name);
				else if (country.equals("Belgium"))
					countries.get(7).add(name);
				else if (country.equals("Czech Republic"))
					countries.get(8).add(name);
				else if (country.equals("Denmark"))
					countries.get(9).add(name);
				else if (country.equals("Estonia"))
					countries.get(10).add(name);
				else if (country.equals("Finland"))
					countries.get(11).add(name);
				else if (country.equals("France"))
					countries.get(12).add(name);
				else if (country.equals("Germany"))
					countries.get(13).add(name);
				else if (country.equals("Greece"))
					countries.get(14).add(name);
				else if (country.equals("Hungary"))
					countries.get(15).add(name);
				else if (country.equals("Ireland"))
					countries.get(16).add(name);
				else if (country.equals("Italy"))
					countries.get(17).add(name);
				else if (country.equals("Luxembourg"))
					countries.get(18).add(name);
				else if (country.equals("The Netherlands"))
					countries.get(19).add(name);
				else if (country.equals("Norway"))
					countries.get(20).add(name);
				else if (country.equals("Poland"))
					countries.get(21).add(name);
				else if (country.equals("Portugal"))
					countries.get(22).add(name);
				else if (country.equals("Romania"))
					countries.get(23).add(name);
				else if (country.equals("Spain"))
					countries.get(24).add(name);
				else if (country.equals("Sweden"))
					countries.get(25).add(name);
				else if (country.equals("Switzerland"))
					countries.get(26).add(name);
				else if (country.equals("UK"))
					countries.get(27).add(name);

			}

			Map<Integer, List<String>> lastNamesBySponsor = new HashMap<>();
			Map<Integer, List<String>> lastNamesByCountry = new HashMap<>();

			for (int i = 0; i < 7; i++) {
				lastNamesBySponsor.put(i, sponsors.get(i));
			}

			for (int i = 0; i < 28; i++) {
				lastNamesByCountry.put(i, countries.get(i));
			}

			lastNames.add(lastNamesBySponsor);
			lastNames.add(lastNamesByCountry);

		}

		return lastNames;
	}

	/**
	 * Retrieves a list of settlers' male and female first names by sponsors and by
	 * countries.
	 * 
	 * @return List of first names.
	 * @throws Exception if first names could not be found.
	 */
	public List<Map<Integer, List<String>>> retrieveFirstNameList() {

		if (firstNames == null) {

			firstNames = new ArrayList<Map<Integer, List<String>>>();

			List<List<String>> malesBySponsor = new ArrayList<>();
			for (int i = 0; i < 7; i++) {
				List<String> list = new ArrayList<String>();
				malesBySponsor.add(list);
			}

			List<List<String>> femalesBySponsor = new ArrayList<>();
			for (int i = 0; i < 7; i++) {
				List<String> list = new ArrayList<String>();
				femalesBySponsor.add(list);
			}

			// 2017-01-21 Added lists for countries
			List<List<String>> malesByCountry = new ArrayList<>();
			for (int i = 0; i < 28; i++) {
				List<String> countryList = new ArrayList<String>();
				malesByCountry.add(countryList);
			}

			List<List<String>> femalesByCountry = new ArrayList<>();
			for (int i = 0; i < 28; i++) {
				List<String> countryList = new ArrayList<String>();
				femalesByCountry.add(countryList);
			}

			// List<String> nameList = new ArrayList<String>();
			// Element root = personDoc.getRootElement();
			Element firstNameEl = root.getChild(FIRST_NAME_LIST);
			List<Element> firstNamesList = firstNameEl.getChildren(FIRST_NAME);

			for (Element nameElement : firstNamesList) {

				String gender = nameElement.getAttributeValue(GENDER);
				String sponsor = nameElement.getAttributeValue(SPONSOR);
				String name = nameElement.getAttributeValue(VALUE);
				String country = nameElement.getAttributeValue(COUNTRY);

				if (gender.equals("male")) {

					if (sponsor.contains("CNSA"))// && type[i] == ReportingAuthorityType.CNSA)
						malesBySponsor.get(0).add(name);

					else if (sponsor.contains("CSA"))// && type[i] == ReportingAuthorityType.CSA)
						malesBySponsor.get(1).add(name);

					else if (sponsor.contains("ESA"))// && type[i] == ReportingAuthorityType.ESA)
						malesBySponsor.get(2).add(name);

					else if (sponsor.contains("ISRO"))// && type[i] == ReportingAuthorityType.ISRO)
						malesBySponsor.get(3).add(name);

					else if (sponsor.contains("JAXA"))// && type[i] == ReportingAuthorityType.JAXA)
						malesBySponsor.get(4).add(name);

					else if (sponsor.contains("NASA"))// && type[i] == ReportingAuthorityType.NASA)
						malesBySponsor.get(5).add(name);

					else if (sponsor.contains("RKA"))// && type[i] == ReportingAuthorityType.RKA)
						malesBySponsor.get(6).add(name);

					else if (sponsor.contains("Mars Society")
							|| sponsor.contains("MS"))// && type[i] == ReportingAuthorityType.NASA)
						malesBySponsor.get(7).add(name);

					else if (sponsor.contains("SpaceX"))// && type[i] == ReportingAuthorityType.RKA)
						malesBySponsor.get(8).add(name);

					/* CNSA,CSA,ISRO,JAXA,NASA,RKA */
					if (country.equals("China"))
						malesByCountry.get(0).add(name);
					else if (country.equals("Canada"))
						malesByCountry.get(1).add(name);
					else if (country.equals("India"))
						malesByCountry.get(2).add(name);
					else if (country.equals("Japan"))
						malesByCountry.get(3).add(name);
					else if (country.equals("USA"))
						malesByCountry.get(4).add(name);
					else if (country.equals("Russia"))
						malesByCountry.get(5).add(name);

					/*
					 * ESA has 22 Member States. The national bodies responsible for space in these
					 * countries sit on ESA�s governing Council: Austria, Belgium, Czech Republic,
					 * Denmark, Estonia, Finland, France, Germany, Greece, Hungary, Ireland, Italy,
					 * Luxembourg, The Netherlands, Norway, Poland, Portugal, Romania, Spain,
					 * Sweden, Switzerland and the United Kingdom.
					 */
					else if (country.equals("Austria"))
						malesByCountry.get(6).add(name);
					else if (country.equals("Belgium"))
						malesByCountry.get(7).add(name);
					else if (country.equals("Czech Republic"))
						malesByCountry.get(8).add(name);
					else if (country.equals("Denmark"))
						malesByCountry.get(9).add(name);
					else if (country.equals("Estonia"))
						malesByCountry.get(10).add(name);
					else if (country.equals("Finland"))
						malesByCountry.get(11).add(name);
					else if (country.equals("France"))
						malesByCountry.get(12).add(name);
					else if (country.equals("Germany"))
						malesByCountry.get(13).add(name);
					else if (country.equals("Greece"))
						malesByCountry.get(14).add(name);
					else if (country.equals("Hungary"))
						malesByCountry.get(15).add(name);
					else if (country.equals("Ireland"))
						malesByCountry.get(16).add(name);
					else if (country.equals("Italy"))
						malesByCountry.get(17).add(name);
					else if (country.equals("Luxembourg"))
						malesByCountry.get(18).add(name);
					else if (country.equals("The Netherlands"))
						malesByCountry.get(19).add(name);
					else if (country.equals("Norway"))
						malesByCountry.get(20).add(name);
					else if (country.equals("Poland"))
						malesByCountry.get(21).add(name);
					else if (country.equals("Portugal"))
						malesByCountry.get(22).add(name);
					else if (country.equals("Romania"))
						malesByCountry.get(23).add(name);
					else if (country.equals("Spain"))
						malesByCountry.get(24).add(name);
					else if (country.equals("Sweden"))
						malesByCountry.get(25).add(name);
					else if (country.equals("Switzerland"))
						malesByCountry.get(26).add(name);
					else if (country.equals("UK"))
						malesByCountry.get(27).add(name);

				} else if (gender.equals("female")) {

					if (sponsor.contains("CNSA"))// && type[i] == ReportingAuthorityType.CNSA)
						femalesBySponsor.get(0).add(name);

					else if (sponsor.contains("CSA"))// && type[i] == ReportingAuthorityType.CSA)
						femalesBySponsor.get(1).add(name);

					else if (sponsor.contains("ESA"))// && type[i] == ReportingAuthorityType.ESA)
						femalesBySponsor.get(2).add(name);

					else if (sponsor.contains("ISRO"))// && type[i] == ReportingAuthorityType.ISRO)
						femalesBySponsor.get(3).add(name);

					else if (sponsor.contains("JAXA"))// && type[i] == ReportingAuthorityType.JAXA)
						femalesBySponsor.get(4).add(name);

					else if (sponsor.contains("NASA"))// && type[i] == ReportingAuthorityType.NASA)
						femalesBySponsor.get(5).add(name);

					else if (sponsor.contains("RKA"))// && type[i] == ReportingAuthorityType.RKA)
						femalesBySponsor.get(6).add(name);

					else if (sponsor.contains("Mars Society")
							|| sponsor.contains("MS"))// && type[i] == ReportingAuthorityType.NASA)
						femalesBySponsor.get(7).add(name);

					else if (sponsor.contains("SpaceX"))// && type[i] == ReportingAuthorityType.RKA)
						femalesBySponsor.get(8).add(name);

					/* CNSA,CSA,ISRO,JAXA,NASA,RKA */
					if (country.equals("China"))
						femalesByCountry.get(0).add(name);
					else if (country.equals("Canada"))
						femalesByCountry.get(1).add(name);
					else if (country.equals("India"))
						femalesByCountry.get(2).add(name);
					else if (country.equals("Japan"))
						femalesByCountry.get(3).add(name);
					else if (country.equals("USA"))
						femalesByCountry.get(4).add(name);
					else if (country.equals("Russia"))
						femalesByCountry.get(5).add(name);

					/*
					 * ESA has 22 Member States. The national bodies responsible for space in these
					 * countries sit on ESA�s governing Council: Austria, Belgium, Czech Republic,
					 * Denmark, Estonia, Finland, France, Germany, Greece, Hungary, Ireland, Italy,
					 * Luxembourg, The Netherlands, Norway, Poland, Portugal, Romania, Spain,
					 * Sweden, Switzerland and the United Kingdom.
					 */
					else if (country.equals("Austria"))
						femalesByCountry.get(6).add(name);
					else if (country.equals("Belgium"))
						femalesByCountry.get(7).add(name);
					else if (country.equals("Czech Republic"))
						femalesByCountry.get(8).add(name);
					else if (country.equals("Denmark"))
						femalesByCountry.get(9).add(name);
					else if (country.equals("Estonia"))
						femalesByCountry.get(10).add(name);
					else if (country.equals("Finland"))
						femalesByCountry.get(11).add(name);
					else if (country.equals("France"))
						femalesByCountry.get(12).add(name);
					else if (country.equals("Germany"))
						femalesByCountry.get(13).add(name);
					else if (country.equals("Greece"))
						femalesByCountry.get(14).add(name);
					else if (country.equals("Hungary"))
						femalesByCountry.get(15).add(name);
					else if (country.equals("Ireland"))
						femalesByCountry.get(16).add(name);
					else if (country.equals("Italy"))
						femalesByCountry.get(17).add(name);
					else if (country.equals("Luxembourg"))
						femalesByCountry.get(18).add(name);
					else if (country.equals("The Netherlands"))
						femalesByCountry.get(19).add(name);
					else if (country.equals("Norway"))
						femalesByCountry.get(20).add(name);
					else if (country.equals("Poland"))
						femalesByCountry.get(21).add(name);
					else if (country.equals("Portugal"))
						femalesByCountry.get(22).add(name);
					else if (country.equals("Romania"))
						femalesByCountry.get(23).add(name);
					else if (country.equals("Spain"))
						femalesByCountry.get(24).add(name);
					else if (country.equals("Sweden"))
						femalesByCountry.get(25).add(name);
					else if (country.equals("Switzerland"))
						femalesByCountry.get(26).add(name);
					else if (country.equals("UK"))
						femalesByCountry.get(27).add(name);

				}
			}

			Map<Integer, List<String>> maleFirstNamesBySponsor = new HashMap<>();
			Map<Integer, List<String>> femaleFirstNamesBySponsor = new HashMap<>();
			Map<Integer, List<String>> maleFirstNamesByCountry = new HashMap<>();
			Map<Integer, List<String>> femaleFirstNamesByCountry = new HashMap<>();

			for (int i = 0; i < 7; i++) {
				maleFirstNamesBySponsor.put(i, malesBySponsor.get(i));
				femaleFirstNamesBySponsor.put(i, femalesBySponsor.get(i));
			}

			firstNames.add(maleFirstNamesBySponsor);
			firstNames.add(femaleFirstNamesBySponsor);

			for (int i = 0; i < 28; i++) {
				maleFirstNamesByCountry.put(i, malesByCountry.get(i));
				femaleFirstNamesByCountry.put(i, femalesByCountry.get(i));
			}

			firstNames.add(maleFirstNamesByCountry);
			firstNames.add(femaleFirstNamesByCountry);

		}

		return firstNames;
	}

	/**
	 * Gets the sponsor of a given person name.
	 * 
	 * @param name the name of the person
	 * @return the sponsor of the person
	 */
	public ReportingAuthorityType getMarsSocietySponsor(String name) {
		ReportingAuthorityType type = null;

		// Element root = personDoc.getRootElement();

		Element personNameList = root.getChild(PERSON_NAME_LIST);
		List<Element> personNames = personNameList.getChildren(PERSON_NAME);
		for (Element nameElement : personNames) {
			String personName = nameElement.getAttributeValue(VALUE);
			String sponsor = null;
			if (personName.equals(name)) {
				sponsor = nameElement.getAttributeValue(SPONSOR);

//				if (sponsor.equals("CNSA"))
//					type = ReportingAuthorityType.CNSA;
//
//				else if (sponsor.equals("CSA"))
//					type = ReportingAuthorityType.CSA;
//
//				else if (sponsor.equals("ESA"))
//					type = ReportingAuthorityType.ESA;
//
//				else if (sponsor.equals("ISRO"))
//					type = ReportingAuthorityType.ISRO;
//
//				else if (sponsor.equals("JAXA"))
//					type = ReportingAuthorityType.JAXA;

				if (sponsor.contains("Mars Society") || sponsor.contains("MS"))
					type = ReportingAuthorityType.MARS_SOCIETY;

//				else if (sponsor.equals("NASA"))
//					type = ReportingAuthorityType.NASA;
//
//				else if (sponsor.equals("RKA"))
//					type = ReportingAuthorityType.RKA;
			}

		}

		return type;
	}

	/**
	 * Gets the gender of a given person name.
	 * 
	 * @param name the name of the person
	 * @return {@link GenderType} the gender of the person name
	 * @throws Exception if person names could not be found.
	 */
	@SuppressWarnings("unchecked")
	public GenderType getPersonGender(String name) {
		GenderType result = GenderType.UNKNOWN;

		// Element root = personDoc.getRootElement();
		Element personNameList = root.getChild(PERSON_NAME_LIST);
		List<Element> personNames = personNameList.getChildren(PERSON_NAME);
		for (Element nameElement : personNames) {
			String personName = nameElement.getAttributeValue(VALUE);
			if (personName.equals(name))
				result = GenderType.valueOfIgnoreCase(nameElement.getAttributeValue(GENDER));
		}

		return result;
	}

	/**
	 * Gets the nominal oxygen consumption rate.
	 * 
	 * @return oxygen rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getNominalO2ConsumptionRate() {
		if (o2ConsumptionRate[1] != 0)
			return o2ConsumptionRate[1];
		else {
			o2ConsumptionRate[1] = getValueAsDouble(NOMINAL_O2_RATE);
			return o2ConsumptionRate[1];
		}
	}

	/**
	 * Gets the low oxygen consumption rate.
	 * 
	 * @return oxygen rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getLowO2ConsumptionRate() {
		if (o2ConsumptionRate[0] != 0)
			return o2ConsumptionRate[0];
		else {
			o2ConsumptionRate[0] = getValueAsDouble(LOW_O2_RATE);
			return o2ConsumptionRate[0];
		}
	}

	/**
	 * Gets the high oxygen consumption rate.
	 * 
	 * @return oxygen rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getHighO2ConsumptionRate() {
		if (o2ConsumptionRate[2] != 0)
			return o2ConsumptionRate[2];
		else {
			o2ConsumptionRate[2] = getValueAsDouble(HIGH_O2_RATE);
			return o2ConsumptionRate[2];
		}
	}

	/**
	 * Gets the carbon dioxide expelled rate.
	 * 
	 * @return carbon dioxide expelled rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getCO2ExpelledRate() {
		if (o2ConsumptionRate[2] != 0)
			return o2ConsumptionRate[2];
		else {
			o2ConsumptionRate[2] = getValueAsDouble(CO2_EXPELLED_RATE);
			return o2ConsumptionRate[2];
		}
	}

	/**
	 * Gets the water consumption rate.
	 * 
	 * @return water rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getWaterConsumptionRate() {
		if (consumptionRates[0] != 0)
			return consumptionRates[0];
		else {
			consumptionRates[0] = getValueAsDouble(WATER_CONSUMPTION_RATE);
			return consumptionRates[0];
		}
	}

	/**
	 * Gets the water usage rate.
	 * 
	 * @return water rate (kg/sol)
	 * @throws Exception if usage rate could not be found.
	 */
	public double getWaterUsageRate() {
		if (waterUsage != 0)
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
		if (ratio[0] != 0)
			return ratio[0];
		else {
			ratio[0] = getValueAsDouble(GREY_TO_BLACK_WATER_RATIO);
			return ratio[0];
		}
	}

	/**
	 * Gets the food consumption rate.
	 * 
	 * @return food rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getFoodConsumptionRate() {
		if (consumptionRates[2] != 0)
			return consumptionRates[2];
		else {
			consumptionRates[2] = getValueAsDouble(FOOD_CONSUMPTION_RATE);
			return consumptionRates[2];
		}
	}

	/**
	 * Gets the dessert consumption rate.
	 * 
	 * @return dessert rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getDessertConsumptionRate() {
		if (consumptionRates[1] != 0)
			return consumptionRates[1];
		else {
			consumptionRates[1] = getValueAsDouble(DESSERT_CONSUMPTION_RATE);
			return consumptionRates[1];
		}
	}

	/**
	 * Gets the oxygen deprivation time.
	 * 
	 * @return oxygen time in millisols.
	 * @throws Exception if oxygen deprivation time could not be found.
	 */
	public double getOxygenDeprivationTime() {
		if (time[0] != 0)
			return time[0];
		else {
			time[0] = getValueAsDouble(OXYGEN_DEPRIVATION_TIME);
			return time[0];
		}
	}

	/**
	 * Gets the water deprivation time.
	 * 
	 * @return water time in sols.
	 * @throws Exception if water deprivation time could not be found.
	 */
	public double getWaterDeprivationTime() {
		if (time[1] != 0)
			return time[1];
		else {
			time[1] = getValueAsDouble(WATER_DEPRIVATION_TIME);
			return time[1];
		}
	}

	/**
	 * Gets the dehydration start time.
	 * 
	 * @return dehydration time in sols.
	 * @throws Exception if dehydration start time could not be found.
	 */
	public double getDehydrationStartTime() {
		if (time[2] != 0)
			return time[2];
		else {
			time[2] = getValueAsDouble(DEHYDRATION_START_TIME);
			return time[2];
		}
	}

	/**
	 * Gets the food deprivation time.
	 * 
	 * @return food time in sols.
	 * @throws Exception if food deprivation time could not be found.
	 */
	public double getFoodDeprivationTime() {
		if (time[3] != 0)
			return time[3];
		else {
			time[3] = getValueAsDouble(FOOD_DEPRIVATION_TIME);
			return time[3];
		}
	}

	/**
	 * Gets the starvation start time.
	 * 
	 * @return starvation time in sols.
	 * @throws Exception if starvation start time could not be found.
	 */
	public double getStarvationStartTime() {
		if (time[4] != 0)
			return time[4];
		else {
			time[4] = getValueAsDouble(STARVATION_START_TIME);
			return time[4];
		}
	}

	/**
	 * Gets the required air pressure.
	 * 
	 * @return air pressure in kPa.
	 * @throws Exception if air pressure could not be found.
	 */
	public double getMinAirPressure() {
		if (pressure != 0)
			return pressure;
		else {
			pressure = getValueAsDouble(MIN_AIR_PRESSURE);
			return pressure;
		}
	}
			
	/**
	 * Gets the max decompression time a person can survive.
	 * 
	 * @return decompression time in millisols.
	 * @throws Exception if decompression time could not be found.
	 */
	public double getDecompressionTime() {
		if (time[5] != 0)
			return time[5];
		else {
			time[5] = getValueAsDouble(DECOMPRESSION_TIME);
			return time[5];
		}
	}

	/**
	 * Gets the minimum temperature a person can tolerate.
	 * 
	 * @return temperature in celsius
	 * @throws Exception if min temperature cannot be found.
	 */
	public double getMinTemperature() {
		if (temperature[0] != 0)
			return temperature[0];
		else {
			temperature[0] = getValueAsDouble(MIN_TEMPERATURE);
			return temperature[0];
		}
	}

	/**
	 * Gets the maximum temperature a person can tolerate.
	 * 
	 * @return temperature in celsius
	 * @throws Exception if max temperature cannot be found.
	 */
	public double getMaxTemperature() {
		if (temperature[1] != 0)
			return temperature[1];
		else {
			temperature[1] = getValueAsDouble(MAX_TEMPERATURE);
			return temperature[1];
		}
	}

	/**
	 * Gets the time a person can survive below minimum temperature.
	 * 
	 * @return freezing time in millisols.
	 * @throws Exception if freezing time could not be found.
	 */
	public double getFreezingTime() {
		if (time[6] != 0)
			return time[6];
		else {
			time[6] = getValueAsDouble(FREEZING_TIME);
			return time[6];
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
		if (chance[0] != 0)
			return chance[0];
		else {
			chance[0] = getValueAsDouble(STRESS_BREAKDOWN_CHANCE);
			return chance[0];
		}
	}

	/**
	 * Gets the base percent chance that a person will collapse under high fatigue.
	 * 
	 * @return percent chance of a collapse per millisol.
	 * @throws Exception if collapse time could not be found.
	 */
	public double getHighFatigueCollapseChance() {
		if (chance[1] != 0)
			return chance[1];
		else {
			chance[1] = getValueAsDouble(HIGH_FATIGUE_COLLAPSE);
			return chance[1];
		}
	}

	/**
	 * Gets the gender ratio between males and the total population on Mars.
	 * 
	 * @return gender ratio between males and total population.
	 * @throws Exception if gender ratio could not be found.
	 */
	public double getGenderRatio() {
		if (ratio[1] != 0)
			return ratio[1];
		else {
			ratio[1] = getValueAsDouble(GENDER_MALE_PERCENTAGE) / 100D;
			return ratio[1];
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

		// Element root = personDoc.getRootElement();
		Element personalityTypeList = root.getChild(PERSONALITY_TYPES);
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

		personalityDistribution = new HashMap<String, Double>(16);

//		try {
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
//		}
//		catch (Exception e) {
//			throw new Exception("PersonalityType.loadPersonalityTypes(): unable to load a personality type.");
//		}

		Iterator<String> i = personalityDistribution.keySet().iterator();
		double count = 0D;
		while (i.hasNext())
			count += personalityDistribution.get(i.next());
		if (count != 100D)
			throw new IllegalStateException(
					"PersonalityType.loadPersonalityTypes(): percentages don't add up to 100%. (total: " + count + ")");

	}

	/**
	 * Gets the number of people configured for the simulation.
	 * 
	 * @return number of people.
	 * @throws Exception if error in XML parsing.
	 */
	public int getNumberOfConfiguredPeople() {
		// Element root = personDoc.getRootElement();
		Element personList = root.getChild(PERSON_LIST);
		List<String> personNodes = personList.getChildren(PERSON);
		if (personNodes != null)
			return personNodes.size();
		else
			return 0;
	}

	/**
	 * Get person's crew designation
	 * 
	 * @param index the person's index.
	 * @return name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public int getCrew(int index) {
		// retrieve the person's crew designation
		String crewString = getValueAsString(index, CREW);

		if (crewString == null) {
			throw new IllegalStateException("The crew designation of a person is null");

		} else {

			boolean oldCrew = false;

			Iterator<Crew> i = roster.iterator();
			while (i.hasNext()) {
				Crew crew = i.next();
				// if the name does not exist, create a new crew with this name
				if (crewString.equals(crew.getName())) {
					oldCrew = true;
					// add a new member
					// Member m = new Member();
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

//			System.out.println("crewString : " + crewString + " crew size : " + roster.size());

			return roster.size() - 1;
		}

	}

	/**
	 * Gets the configured person's name.
	 * 
	 * @param index the person's index.
	 * @return name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonName(int index, int crew_id) {
		// System.out.println("roster.get(crew_id) : " + roster.get(crew_id));
		// System.out.println("roster.get(crew_id).getTeam().get(index) : " +
		// roster.get(crew_id).getTeam().get(index));
		// System.out.println("name : " +
		// roster.get(crew_id).getTeam().get(index).getName());
//		System.out.println("roster.size : " + roster.size());

		if (roster.get(crew_id) != null) {
			if (roster.get(crew_id).getTeam().get(index).getName() != null) {
				return roster.get(crew_id).getTeam().get(index).getName();
			} else {
				return getValueAsString(index, NAME);
			}

		} else {
			return getValueAsString(index, NAME);
		}
	}

	/**
	 * Gets the configured person's gender.
	 * 
	 * @param index the person's index.
	 * @return {@link GenderType} or null if not found.
	 * @throws Exception if error in XML parsing.
	 */
	public GenderType getConfiguredPersonGender(int index, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getGender() != null)
			return GenderType.valueOfIgnoreCase(roster.get(crew_id).getTeam().get(index).getGender());// alphaCrewGender.get(index))
																										// ;
		else
			return GenderType.valueOfIgnoreCase(getValueAsString(index, GENDER));
	}

	/**
	 * Gets the configured person's MBTI personality type.
	 * 
	 * @param index the person's index.
	 * @return four character string for MBTI ex. "ISTJ". Return null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonPersonalityType(int index, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getMBTI() != null)
			return roster.get(crew_id).getTeam().get(index).getMBTI();// alphaCrewPersonality.get(index) ;
		else
			return getValueAsString(index, PERSONALITY_TYPE);
	}

	/**
	 * Gets the configured person's job.
	 * 
	 * @param index the person's index.
	 * @return the job name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonJob(int index, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getJob() != null)
			return roster.get(crew_id).getTeam().get(index).getJob();// alphaCrewJob.get(index) ;
		else
			return getValueAsString(index, JOB);
	}

	/**
	 * Gets the configured person's country.
	 * 
	 * @param index the person's index.
	 * @return the job name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonCountry(int index, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getCountry() != null)
			return roster.get(crew_id).getTeam().get(index).getCountry();// alphaCrewJob.get(index) ;
		else
			return getValueAsString(index, COUNTRY);
	}

	/**
	 * Gets the configured person's sponsor.
	 * 
	 * @param index the person's index.
	 * @return the job name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonSponsor(int index, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getSponsor() != null)
			return roster.get(crew_id).getTeam().get(index).getSponsor();// alphaCrewJob.get(index) ;
		else
			return getValueAsString(index, SPONSOR);
	}

	/**
	 * Gets the configured person's starting settlement.
	 * 
	 * @param index the person's index.
	 * @return the settlement name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonDestination(int index, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getDestination() != null)
			return roster.get(crew_id).getTeam().get(index).getDestination();// alphaCrewDestination.get(index);
		else
			return getValueAsString(index, SETTLEMENT);
	}

	/**
	 * Sets the name of a member of the alpha crew
	 * 
	 * @param index
	 * @param name
	 */
	public void setPersonName(int index, String value, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getName() == null)
			roster.get(crew_id).getTeam().get(index).setName(value);// alphaCrewName = new
																	// ArrayList<String>(SIZE_OF_CREW);

		// if (alphaCrewName.size() == SIZE_OF_CREW) {
		// alphaCrewName.set(index, value);
		// } else
		// alphaCrewName.add(value);

	}

	/**
	 * Sets the personality of a member of the alpha crew
	 * 
	 * @param index
	 * @param personality
	 */
	public void setPersonPersonality(int index, String value, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getMBTI() == null)
			roster.get(crew_id).getTeam().get(index).setMBTI(value);

		// if (alphaCrewPersonality == null)
		// alphaCrewPersonality = new ArrayList<String>(SIZE_OF_CREW);
		// if (alphaCrewPersonality.size() == SIZE_OF_CREW) {
		// alphaCrewPersonality.set(index, value);
		// } else
		// alphaCrewPersonality.add(value);
	}

	/**
	 * Sets the gender of a member of the alpha crew
	 * 
	 * @param index
	 * @param gender
	 */
	public void setPersonGender(int index, String value, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getGender() == null)
			roster.get(crew_id).getTeam().get(index).setGender(value);

		// if (alphaCrewGender == null)
		// alphaCrewGender = new ArrayList<String>(SIZE_OF_CREW);
		// if (alphaCrewGender.size() == SIZE_OF_CREW) {
		// alphaCrewGender.set(index, value);
		// } else
		// alphaCrewGender.add(value);
	}

	/**
	 * Sets the job of a member of the alpha crew
	 * 
	 * @param index
	 * @param job
	 */
	public void setPersonJob(int index, String value, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getJob() == null)
			roster.get(crew_id).getTeam().get(index).setJob(value);

		// if (alphaCrewJob == null)
		// alphaCrewJob = new ArrayList<String>(SIZE_OF_CREW);
		// if (alphaCrewJob.size() == SIZE_OF_CREW) {
		// alphaCrewJob.set(index, value);
		// } else
		// alphaCrewJob.add(value);
	}

	/**
	 * Sets the country of a member of the alpha crew
	 * 
	 * @param index
	 * @param country
	 */
	public void setPersonCountry(int index, String value, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getCountry() == null)
			roster.get(crew_id).getTeam().get(index).setCountry(value);
	}

	/**
	 * Sets the sponsor of a member of the alpha crew
	 * 
	 * @param index
	 * @param sponsor
	 */
	public void setPersonSponsor(int index, String value, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getSponsor() == null)
			roster.get(crew_id).getTeam().get(index).setSponsor(value);
	}

	/**
	 * Sets the destination of a member of the alpha crew
	 * 
	 * @param index
	 * @param destination
	 */
	public void setPersonDestination(int index, String value, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getDestination() == null)
			roster.get(crew_id).getTeam().get(index).setDestination(value);

		// if (alphaCrewDestination == null)
		// alphaCrewDestination = new ArrayList<String>(SIZE_OF_CREW);
		// if (alphaCrewDestination.size() == SIZE_OF_CREW) {
		// alphaCrewDestination.set(index, value);
		// } else
		// alphaCrewDestination.add(value);
	}

	/**
	 * Gets a map of the configured person's natural attributes.
	 * 
	 * @param index the person's index.
	 * @return map of natural attributes (empty map if not found).
	 * @throws Exception if error in XML parsing.
	 */
	public Map<String, Integer> getNaturalAttributeMap(int index) {
		Map<String, Integer> result = new HashMap<String, Integer>();
		// Element root = personDoc.getRootElement();
		Element personList = root.getChild(PERSON_LIST);
		Element personElement = (Element) personList.getChildren(PERSON).get(index);
		List<Element> naturalAttributeListNodes = personElement.getChildren(NATURAL_ATTRIBUTE_LIST);

		if ((naturalAttributeListNodes != null) && (naturalAttributeListNodes.size() > 0)) {
			Element naturalAttributeList = naturalAttributeListNodes.get(0);
			int attributeNum = naturalAttributeList.getChildren(NATURAL_ATTRIBUTE).size();

			for (int x = 0; x < attributeNum; x++) {
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
	 * 
	 * @param index the person's index.
	 * @return map of Big Five Model (empty map if not found).
	 * @throws Exception if error in XML parsing.
	 */
	public Map<String, Integer> getBigFiveMap(int index) {
		Map<String, Integer> result = new HashMap<String, Integer>();
		// Element root = personDoc.getRootElement();
		Element personList = root.getChild(PERSON_LIST);
		Element personElement = (Element) personList.getChildren(PERSON).get(index);
		List<Element> listNodes = personElement.getChildren(PERSONALITY_TRAIT_LIST);

		if ((listNodes != null) && (listNodes.size() > 0)) {
			Element list = listNodes.get(0);
			int attributeNum = list.getChildren(PERSONALITY_TRAIT).size();

			for (int x = 0; x < attributeNum; x++) {
				Element naturalAttributeElement = (Element) list.getChildren(PERSONALITY_TRAIT).get(x);
				String name = naturalAttributeElement.getAttributeValue(NAME);
				Integer value = new Integer(naturalAttributeElement.getAttributeValue(VALUE));
				// System.out.println(name + " : " + value);
				result.put(name, value);
			}
		}
		return result;
	}

	/**
	 * Gets the value of an element as a String
	 * 
	 * @param an element
	 * 
	 * @param an index
	 * 
	 * @return a String
	 */
	private String getValueAsString(int index, String param) {
		// Element root = personDoc.getRootElement();
		Element personList = root.getChild(PERSON_LIST);
		Element personElement = (Element) personList.getChildren(PERSON).get(index);
		return personElement.getAttributeValue(param);
	}

	/**
	 * Gets the value of an element as a double
	 * 
	 * @param an element
	 * 
	 * @return a double
	 */
	private double getValueAsDouble(String child) {
		// Element root = personDoc.getRootElement();
		Element element = root.getChild(child);
		String str = element.getAttributeValue(VALUE);
		return Double.parseDouble(str);
	}

	/**
	 * Gets a map of the configured person's skills.
	 * 
	 * @param index the person's index.
	 * @return map of skills (empty map if not found).
	 * @throws Exception if error in XML parsing.
	 */
	public Map<String, Integer> getSkillMap(int index) {
		Map<String, Integer> result = new HashMap<String, Integer>();
		// Element root = personDoc.getRootElement();
		// Change the people.xml element from "person-list" to
		// "alpha-team"
		Element personList = root.getChild(PERSON_LIST);
		Element personElement = (Element) personList.getChildren(PERSON).get(index);
		List<Element> skillListNodes = personElement.getChildren(SKILL_LIST);
		if ((skillListNodes != null) && (skillListNodes.size() > 0)) {
			Element skillList = skillListNodes.get(0);
			int skillNum = skillList.getChildren(SKILL).size();
			for (int x = 0; x < skillNum; x++) {
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
	 * 
	 * @param index the person's index.
	 * @return map of relationships (key: person name, value: opinion (0 - 100))
	 *         (empty map if not found).
	 * @throws Exception if error in XML parsing.
	 */
	public Map<String, Integer> getRelationshipMap(int index) {
		Map<String, Integer> result = new HashMap<String, Integer>();
		// Element root = personDoc.getRootElement();
		Element personList = root.getChild(PERSON_LIST);
		Element personElement = (Element) personList.getChildren(PERSON).get(index);
		List<Element> relationshipListNodes = personElement.getChildren(RELATIONSHIP_LIST);
		if ((relationshipListNodes != null) && (relationshipListNodes.size() > 0)) {
			Element relationshipList = relationshipListNodes.get(0);
			int relationshipNum = relationshipList.getChildren(RELATIONSHIP).size();
			for (int x = 0; x < relationshipNum; x++) {
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
	 * 
	 * @param index the person's index.
	 * @return the name of the favorite main dish name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getFavoriteMainDish(int index, int crew_id) {

		if (roster.get(crew_id).getTeam().get(index).getMainDish() != null)
			return roster.get(crew_id).getTeam().get(index).getMainDish();
		else
			return getValueAsString(index, MAIN_DISH);
	}

	/**
	 * Gets the configured person's favorite side dish.
	 * 
	 * @param index the person's index.
	 * @return the name of the favorite side dish name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getFavoriteSideDish(int index, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getSideDish() != null)
			return roster.get(crew_id).getTeam().get(index).getSideDish();
		else
			return getValueAsString(index, SIDE_DISH);
	}

	/**
	 * Gets the configured person's favorite dessert.
	 * 
	 * @param index the person's index.
	 * @return the name of the favorite dessert name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getFavoriteDessert(int index, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getDessert() != null)
			return roster.get(crew_id).getTeam().get(index).getDessert();
		else
			return getValueAsString(index, DESSERT);
	}

	/**
	 * Gets the configured person's favorite activity.
	 * 
	 * @param index the person's index.
	 * @return the name of the favorite activity name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getFavoriteActivity(int index, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getActivity() != null)
			return roster.get(crew_id).getTeam().get(index).getActivity();
		else
			return getValueAsString(index, ACTIVITY);
	}

	/**
	 * Create country list
	 * 
	 * @return
	 */
	public List<String> createCountryList() {

		if (countries == null) {
			countries = new ArrayList<>();

			countries.add("China"); // 0
			countries.add("Canada"); // 1
			countries.add("India"); // 2
			countries.add("Japan"); // 3
			countries.add("USA"); // 4
			countries.add("Russia"); // 5

			countries.addAll(createESACountryList());

		}

		return countries;
	}

	public String getCountry(int id) {
		if (countries == null) {
			countries = createCountryList();
		}
		return countries.get(id);
	}

	/**
	 * Create ESA country list
	 * 
	 * @return
	 */
	public List<String> createESACountryList() {

		if (ESAcountries == null) {
			ESAcountries = new ArrayList<>();

			ESAcountries.add("Austria");
			ESAcountries.add("Belgium");
			ESAcountries.add("Czech Republic");
			ESAcountries.add("Denmark");
			ESAcountries.add("Estonia");
			ESAcountries.add("Finland");
			ESAcountries.add("France");
			ESAcountries.add("Germany");
			ESAcountries.add("Greece");
			ESAcountries.add("Hungary");
			ESAcountries.add("Ireland");
			ESAcountries.add("Italy");
			ESAcountries.add("Luxembourg");
			ESAcountries.add("The Netherlands");
			ESAcountries.add("Norway");
			ESAcountries.add("Poland");
			ESAcountries.add("Portugal");
			ESAcountries.add("Romania");
			ESAcountries.add("Spain");
			ESAcountries.add("Sweden");
			ESAcountries.add("Switzerland");
			ESAcountries.add("UK");

		}

		return ESAcountries;
	}

	public int getCountryID(String country) {
		return countries.indexOf(country);
	}

//	public String convert2Sponsor(int id) {
//
//		if (id == 0) {
//			return Msg.getString("ReportingAuthorityType.CNSA");
//		} else if (id == 1) {
//			return Msg.getString("ReportingAuthorityType.CSA");
//		} else if (id == 2) {
//			return Msg.getString("ReportingAuthorityType.ISRO");
//		} else if (id == 3) {
//			return Msg.getString("ReportingAuthorityType.JAXA");
//		} else if (id == 4) {
//			return Msg.getString("ReportingAuthorityType.NASA");
//		} else if (id == 5) {
//			return Msg.getString("ReportingAuthorityType.RKA");
//		} else {
//			return Msg.getString("ReportingAuthorityType.ESA");
//		}
//
//	}

//	public String getSponsorFromCountry(String c) {
//
//		if (c.equalsIgnoreCase("China")) {
//			return Msg.getString("ReportingAuthorityType.CNSA");
//		} else if (c.equalsIgnoreCase("Canada")) {
//			return Msg.getString("ReportingAuthorityType.CSA");
//		} else if (c.equalsIgnoreCase("India")) {
//			return Msg.getString("ReportingAuthorityType.ISRO");
//		} else if (c.equalsIgnoreCase("Japan")) {
//			return Msg.getString("ReportingAuthorityType.JAXA");
//		} else if (c.equalsIgnoreCase("USA")) {
//			return Msg.getString("ReportingAuthorityType.NASA");
//		} else if (c.equalsIgnoreCase("Russia")) {
//			return Msg.getString("ReportingAuthorityType.RKA");
//		} else {
//			return Msg.getString("ReportingAuthorityType.ESA");
//		}
//
//	}

	/**
	 * Create sponsor list
	 * 
	 * @return
	 */
	public List<String> createLongSponsorList() {

		if (longSponsors == null) {
			longSponsors = new ArrayList<>();

			longSponsors.add(Msg.getString("ReportingAuthorityType.long.CNSA"));
			longSponsors.add(Msg.getString("ReportingAuthorityType.long.CSA"));
			longSponsors.add(Msg.getString("ReportingAuthorityType.long.ISRO"));
			longSponsors.add(Msg.getString("ReportingAuthorityType.long.JAXA"));
			longSponsors.add(Msg.getString("ReportingAuthorityType.long.MarsSociety"));
			longSponsors.add(Msg.getString("ReportingAuthorityType.long.NASA"));
			longSponsors.add(Msg.getString("ReportingAuthorityType.long.RKA"));
			longSponsors.add(Msg.getString("ReportingAuthorityType.long.ESA"));

		}

		return longSponsors;
	}
	
	/**
	 * Create sponsor list
	 * 
	 * @return
	 */
	public List<String> createSponsorList() {

		if (sponsors == null) {
			sponsors = new ArrayList<>();

			sponsors.add(Msg.getString("ReportingAuthorityType.CNSA"));
			sponsors.add(Msg.getString("ReportingAuthorityType.CSA"));
			sponsors.add(Msg.getString("ReportingAuthorityType.ISRO"));
			sponsors.add(Msg.getString("ReportingAuthorityType.JAXA"));
			sponsors.add(Msg.getString("ReportingAuthorityType.MS"));
			sponsors.add(Msg.getString("ReportingAuthorityType.NASA"));
			sponsors.add(Msg.getString("ReportingAuthorityType.RKA"));
			sponsors.add(Msg.getString("ReportingAuthorityType.ESA"));

		}

		return sponsors;
	}

//	/**
//	 * Gets a list of last names by sponsors and by countries
//	 */
//	public List<Map<Integer, List<String>>> getLastNames() {
//		return lastNames;
//	}

//	/**
//	 * Gets a list of first names by sponsors and by countries
//	 */
//	public List<Map<Integer, List<String>>> getFirstNames() {
//		return firstNames;
//	}

	/**
	 * Get the Commander's profile
	 * 
	 * @return profile
	 */
	public Commander getCommander() {
		return commander;
	}

	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		personDoc = null;
		if (personNameList != null) {
			personNameList.clear();
			personNameList = null;
		}
	}
}
