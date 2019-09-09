/**
 * Mars Simulation Project
 * CrewConfig.java
 * @version 3.1.0 2019-09-03
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Provides configuration information about the crew.
 */
public class CrewConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// private static Logger logger = Logger.getLogger(CrewConfig.class.getName());

	public static final int ALPHA_CREW = 0;

	// Add a list of crew
	private List<Crew> roster = new ArrayList<>();

	private transient int crewNum = -1;
	
	// Element names
	private final String CREW_LIST = "crew-list";
	private final String PERSON = "person";
	
	private final String PERSON_NAME = "person-name";
	private final String GENDER = "gender";
	private final String SPONSOR = "sponsor";
	private final String COUNTRY = "country";

	private final String PERSONALITY_TYPE = "personality-type";
	private final String PERSONALITY_TRAIT_LIST = "personality-trait-list";
	private final String PERSONALITY_TRAIT = "personality-trait";

	private final String CREW = "crew";
	private final String NAME = "name";
	private final String SETTLEMENT = "settlement";
	private final String JOB = "job";
	private final String NATURAL_ATTRIBUTE_LIST = "natural-attribute-list";
	private final String NATURAL_ATTRIBUTE = "natural-attribute";
	private final String VALUE = "value";
	private final String SKILL_LIST = "skill-list";
	private final String SKILL = "skill";
	private final String LEVEL = "level";
	private final String RELATIONSHIP_LIST = "relationship-list";
	private final String RELATIONSHIP = "relationship";
	private final String OPINION = "opinion";

	private final String MAIN_DISH = "favorite-main-dish";
	private final String SIDE_DISH = "favorite-side-dish";

	private final String DESSERT = "favorite-dessert";
	private final String ACTIVITY = "favorite-activity";

	private Document crewDoc;

	/**
	 * Constructor
	 * 
	 * @param crewDoc the crew config DOM document.
	 */
	public CrewConfig(Document crewDoc) {
		this.crewDoc = crewDoc;
//		root = crewDoc.getRootElement();
	}


	/**
	 * Gets the number of people configured for the simulation.
	 * 
	 * @return number of people.
	 * @throws Exception if error in XML parsing.
	 */
	public int getNumberOfConfiguredPeople() {
		if (crewNum == -1) {
			Element personList = crewDoc.getRootElement().getChild(CREW_LIST);
			List<Element> personNodes = personList.getChildren(PERSON);
			if (personNodes != null) {
				crewNum = personNodes.size();
			}
		}
		return crewNum;
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
	 * Checks if the personality is introvert.
	 * 
	 * @param index the crew index
	 * @return true if introvert
	 */
	public boolean isIntrovert(int index) {
		return getConfiguredPersonPersonalityType(index, getCrew(index)).substring(0, 1).equals("I");
	}

	/**
	 * Checks if the personality is extrovert.
	 * 
	 * @param index the crew index
	 * @return true if extrovert
	 */
	public boolean isExtrovert(int index) {
		return getConfiguredPersonPersonalityType(index, getCrew(index)).substring(0, 1).equals("E");
	}

	/**
	 * Checks if the personality is sensor.
	 * 
	 * @param index the crew index
	 * @return true if sensor
	 */
	public boolean isSensor(int index) {
		return getConfiguredPersonPersonalityType(index, getCrew(index)).substring(1, 2).equals("S");
	}

	/**
	 * Checks if the personality is intuitive.
	 * 
	 * @param index the crew index
	 * @return true if intuitive
	 */
	public boolean isIntuitive(int index) {
		return getConfiguredPersonPersonalityType(index, getCrew(index)).substring(1, 2).equals("N");
	}

	/**
	 * Checks if the personality is thinker.
	 * 
	 * @param index the crew index
	 * @return true if thinker
	 */
	public boolean isThinker(int index) {
		return getConfiguredPersonPersonalityType(index, getCrew(index)).substring(2, 3).equals("T");
	}

	/**
	 * Checks if the personality is feeler.
	 * 
	 * @param index the crew index
	 * @return true if feeler
	 */
	public boolean isFeeler(int index) {
		return getConfiguredPersonPersonalityType(index, getCrew(index)).substring(2, 3).equals("F");
	}

	/**
	 * Checks if the personality is judger.
	 * 
	 * @param index the crew index
	 * @return true if judger
	 */
	public boolean isJudger(int index) {
		return getConfiguredPersonPersonalityType(index, getCrew(index)).substring(3, 4).equals("J");
	}

	/**
	 * Checks if the personality is perceiver.
	 * 
	 * @param index the crew index
	 * @return true if perceiver
	 */
	public boolean isPerceiver(int index) {
		return getConfiguredPersonPersonalityType(index, getCrew(index)).substring(3, 4).equals("P");
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
	}

	public void setMainDish(int index, String value, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getMainDish() == null)
			roster.get(crew_id).getTeam().get(index).setMainDish(value);
	}
	
	public void setSideDish(int index, String value, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getSideDish() == null)
			roster.get(crew_id).getTeam().get(index).setSideDish(value);
	}
	
	public void setDessert(int index, String value, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getDessert() == null)
			roster.get(crew_id).getTeam().get(index).setDessert(value);
	}
	
	public void setActivity(int index, String value, int crew_id) {
		if (roster.get(crew_id).getTeam().get(index).getActivity() == null)
			roster.get(crew_id).getTeam().get(index).setActivity(value);
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
		Element personList = crewDoc.getRootElement().getChild(CREW_LIST);
		Element personElement = (Element) personList.getChildren(PERSON).get(index);
		List<Element> naturalAttributeListNodes = personElement.getChildren(NATURAL_ATTRIBUTE_LIST);

		if ((naturalAttributeListNodes != null) && (naturalAttributeListNodes.size() > 0)) {
			Element naturalAttributeList = naturalAttributeListNodes.get(0);
			int attributeNum = naturalAttributeList.getChildren(NATURAL_ATTRIBUTE).size();

			for (int x = 0; x < attributeNum; x++) {
				Element naturalAttributeElement = (Element) naturalAttributeList.getChildren(NATURAL_ATTRIBUTE).get(x);
				String name = naturalAttributeElement.getAttributeValue(NAME);
//				Integer value = new Integer(naturalAttributeElement.getAttributeValue(VALUE));
				String value = naturalAttributeElement.getAttributeValue(VALUE);
				int intValue = Integer.parseInt(value);
				result.put(name, intValue);
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
		Element personList = crewDoc.getRootElement().getChild(CREW_LIST);
		Element personElement = (Element) personList.getChildren(PERSON).get(index);
		List<Element> listNodes = personElement.getChildren(PERSONALITY_TRAIT_LIST);

		if ((listNodes != null) && (listNodes.size() > 0)) {
			Element list = listNodes.get(0);
			int attributeNum = list.getChildren(PERSONALITY_TRAIT).size();

			for (int x = 0; x < attributeNum; x++) {
				Element naturalAttributeElement = (Element) list.getChildren(PERSONALITY_TRAIT).get(x);
				String name = naturalAttributeElement.getAttributeValue(NAME);
				String value = naturalAttributeElement.getAttributeValue(VALUE);
				int intValue = Integer.parseInt(value);
				// System.out.println(name + " : " + value);
				result.put(name, intValue);
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
		Element personList = crewDoc.getRootElement().getChild(CREW_LIST);
		Element personElement = (Element) personList.getChildren(PERSON).get(index);
		return personElement.getAttributeValue(param);
	}

//	/**
//	 * Gets the value of an element as a double
//	 * 
//	 * @param an element
//	 * 
//	 * @return a double
//	 */
//	private double getValueAsDouble(String child) {
//		Element element = crewDoc.getRootElement().getChild(child);
//		String str = element.getAttributeValue(VALUE);
//		return Double.parseDouble(str);
//	}

	/**
	 * Gets a map of the configured person's skills.
	 * 
	 * @param index the person's index.
	 * @return map of skills (empty map if not found).
	 * @throws Exception if error in XML parsing.
	 */
	public Map<String, Integer> getSkillMap(int index) {
		Map<String, Integer> result = new HashMap<String, Integer>();
		Element personList = crewDoc.getRootElement().getChild(CREW_LIST);
		Element personElement = (Element) personList.getChildren(PERSON).get(index);
		List<Element> skillListNodes = personElement.getChildren(SKILL_LIST);
		if ((skillListNodes != null) && (skillListNodes.size() > 0)) {
			Element skillList = skillListNodes.get(0);
			int skillNum = skillList.getChildren(SKILL).size();
			for (int x = 0; x < skillNum; x++) {
				Element skillElement = (Element) skillList.getChildren(SKILL).get(x);
				String name = skillElement.getAttributeValue(NAME);
//				Integer level = new Integer(skillElement.getAttributeValue(LEVEL));
				String level = skillElement.getAttributeValue(LEVEL);
				int intLevel = Integer.parseInt(level);
				result.put(name, intLevel);
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
		Element personList = crewDoc.getRootElement().getChild(CREW_LIST);
		Element personElement = (Element) personList.getChildren(PERSON).get(index);
		List<Element> relationshipListNodes = personElement.getChildren(RELATIONSHIP_LIST);
		if ((relationshipListNodes != null) && (relationshipListNodes.size() > 0)) {
			Element relationshipList = relationshipListNodes.get(0);
			int relationshipNum = relationshipList.getChildren(RELATIONSHIP).size();
			for (int x = 0; x < relationshipNum; x++) {
				Element relationshipElement = (Element) relationshipList.getChildren(RELATIONSHIP).get(x);
				String personName = relationshipElement.getAttributeValue(PERSON_NAME);
//				Integer opinion = new Integer(relationshipElement.getAttributeValue(OPINION));
				String opinion = relationshipElement.getAttributeValue(OPINION);
				int intOpinion = Integer.parseInt(opinion);
				result.put(personName, intOpinion);
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
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		crewDoc = null;
		roster = null;
	}
}
