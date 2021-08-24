/**
 * Mars Simulation Project
 * CrewConfig.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mars_sim.msp.core.configuration.UserConfigurableConfig;

/**
 * Provides configuration information about the crew.
 */
public class CrewConfig extends UserConfigurableConfig<Crew> {

	private static final Logger logger = Logger.getLogger(CrewConfig.class.getName());

	private static final String CREW_PREFIX = "crew_";
	
	// Element or attribute names
	private static final String CREW_COFIG = "crew-configuration";
	private static final String CREW_LIST = "crew-list";
	private static final String PERSON = "person";
	
	private static final String PERSON_NAME = "person-name";
	private static final String GENDER = "gender";
	private static final String AGE = "age";
	private static final String SPONSOR = "sponsor";
	private static final String COUNTRY = "country";

	private static final String PERSONALITY_TYPE = "personality-type";
	private static final String PERSONALITY_TRAIT_LIST = "personality-trait-list";
	private static final String PERSONALITY_TRAIT = "personality-trait";

	private static final String NAME_ATTR = "name";
	private static final String DESC_ATTR = "description";
	private static final String JOB = "job";
	private static final String NATURAL_ATTRIBUTE_LIST = "natural-attribute-list";
	private static final String NATURAL_ATTRIBUTE = "natural-attribute";
	private static final String VALUE = "value";
	private static final String SKILL_LIST = "skill-list";
	private static final String SKILL = "skill";
	private static final String LEVEL = "level";
	private static final String RELATIONSHIP_LIST = "relationship-list";
	private static final String RELATIONSHIP = "relationship";
	private static final String OPINION = "opinion";

	private static final String MAIN_DISH = "favorite-main-dish";
	private static final String SIDE_DISH = "favorite-side-dish";

	private static final String DESSERT = "favorite-dessert";
	private static final String ACTIVITY = "favorite-activity";

	/** 
	 * Crew files preloaded in the code
	 */
	private static final String [] PREDEFINED_CREWS = {"Alpha", "Founders"};
	private Map<String, Integer> bigFiveMap = new HashMap<>();
	
	/**
	 * Constructor
	 */
	public CrewConfig() {
		super(CREW_PREFIX);
		
		loadDefaults(PREDEFINED_CREWS);
		loadUserDefined();
	}

	/**
	 * Parse an XML document to create an Crew instance.
	 * @param doc
	 * @param predefined
	 * @return
	 */
	@Override
	protected Crew parseItemXML(Document doc, boolean predefined) {
		Element crewEl = doc.getRootElement();
		String name = crewEl.getAttributeValue(NAME_ATTR);
		if (name == null) {
			logger.warning("Crew has no name");
			name = "Unknown";
		}
		String desc = crewEl.getAttributeValue(DESC_ATTR);
		if (desc == null) {
			desc = "";
		}
		
		Crew roster = new Crew(name, predefined);
		roster.setDescription(desc);
		Element personList = crewEl.getChild(CREW_LIST);
		List<Element> personNodes = personList.getChildren(PERSON);
		for (Element personElement : personNodes) {
			Member m = new Member();
			roster.addMember(m);
		
			m.setName(personElement.getAttributeValue(NAME_ATTR));
			m.setGender(GenderType.valueOf(personElement.getAttributeValue(GENDER).toUpperCase()));
			m.setAge(personElement.getAttributeValue(AGE));
			m.setMBTI(personElement.getAttributeValue(PERSONALITY_TYPE));
			String sponsor = personElement.getAttributeValue(SPONSOR);
			if (sponsor != null) {
				m.setSponsorCode(sponsor);
			}
			m.setCountry(personElement.getAttributeValue(COUNTRY));
			m.setJob(personElement.getAttributeValue(JOB));
			
			// Optionals
			m.setDessert(personElement.getAttributeValue(DESSERT));
			m.setMainDish(personElement.getAttributeValue(MAIN_DISH));
			m.setSideDish(personElement.getAttributeValue(SIDE_DISH));
			
			m.setSkillsMap(parseSkillsMap(personElement));
			m.setRelationshipMap(parseRelationshipMap(personElement));
		}
		
		return roster;
	}
	


	/**
	 * Creates an XML document for this crew.
	 * 
	 * @return
	 */
	@Override
	protected Document createItemDoc(Crew roster) {

		Element root = new Element(CREW_COFIG);
		Document doc = new Document(root);
		
		root.setAttribute(new Attribute(NAME_ATTR, roster.getName()));
		root.setAttribute(new Attribute(DESC_ATTR, roster.getDescription()));

		Element crewList = new Element(CREW_LIST);
		
		List<Element> personList = new ArrayList<>(); 
		
		for (Member person : roster.getTeam()) {
			
			Element personElement = new Element(PERSON);

			personElement.setAttribute(new Attribute(NAME_ATTR, person.getName()));
			personElement.setAttribute(new Attribute(GENDER, person.getGender().name()));
			personElement.setAttribute(new Attribute(AGE, person.getAge()));
			personElement.setAttribute(new Attribute(PERSONALITY_TYPE, person.getMBTI()));
			saveOptionalAttribute(personElement, SPONSOR, person.getSponsorCode());

			personElement.setAttribute(new Attribute(COUNTRY, person.getCountry()));
			personElement.setAttribute(new Attribute(JOB, person.getJob()));
			
			saveOptionalAttribute(personElement, ACTIVITY, person.getActivity());
			saveOptionalAttribute(personElement, MAIN_DISH, person.getMainDish());
			saveOptionalAttribute(personElement, SIDE_DISH, person.getSideDish());
			saveOptionalAttribute(personElement, DESSERT, person.getDessert());
	
//			
//	        Element traitList = new Element(PERSONALITY_TRAIT_LIST);
//
//	        Element trait0 = new Element(PERSONALITY_TRAIT);
//	        trait0.setAttribute(new Attribute(NAME, "openness"));
//	        trait0.setAttribute(new Attribute(VALUE, "25"));
//	        traitList.addContent(trait0);
	        
	        personList.add(personElement);
		}

		crewList.addContent(personList);
		doc.getRootElement().addContent(crewList);
	        
        return doc;
	}

	/**
	 * Gets a map of the configured person's natural attributes.
	 * 
	 * @param index the person's index.
	 * @return map of natural attributes (empty map if not found).
	 * @throws Exception if error in XML parsing.
	 */
	private Map<String, Integer> computeNaturalAttributeMap(Document doc, int index) {
		Map<String, Integer> result = new HashMap<String, Integer>();
		Element personList = doc.getRootElement().getChild(CREW_LIST);
		Element personElement = (Element) personList.getChildren(PERSON).get(index);
		List<Element> naturalAttributeListNodes = personElement.getChildren(NATURAL_ATTRIBUTE_LIST);

		if ((naturalAttributeListNodes != null) && (naturalAttributeListNodes.size() > 0)) {
			Element naturalAttributeList = naturalAttributeListNodes.get(0);
			int attributeNum = naturalAttributeList.getChildren(NATURAL_ATTRIBUTE).size();

			for (int x = 0; x < attributeNum; x++) {
				Element naturalAttributeElement = (Element) naturalAttributeList.getChildren(NATURAL_ATTRIBUTE).get(x);
				String name = naturalAttributeElement.getAttributeValue(NAME_ATTR);
//				Integer value = new Integer(naturalAttributeElement.getAttributeValue(VALUE));
				String value = naturalAttributeElement.getAttributeValue(VALUE);
				int intValue = Integer.parseInt(value);
				result.put(name, intValue);
			}
		}
		return result;
	}

	public Map<String, Integer> getBigFiveMap(int index) {
		return bigFiveMap;
	}
	
	/**
	 * Gets a map of the configured person's traits according to the Big Five Model.
	 * 
	 * @param index the person's index.
	 * @return map of Big Five Model (empty map if not found).
	 * @throws Exception if error in XML parsing.
	 */
//	public Map<String, Integer> computeBigFiveMap(Document doc, int index) {
//		Map<String, Integer> result = new HashMap<String, Integer>();
//		Element personList = doc.getRootElement().getChild(CREW_LIST);
//		Element personElement = (Element) personList.getChildren(PERSON).get(index);
//		List<Element> listNodes = personElement.getChildren(PERSONALITY_TRAIT_LIST);
//
//		if ((listNodes != null) && (listNodes.size() > 0)) {
//			Element list = listNodes.get(0);
//			int attributeNum = list.getChildren(PERSONALITY_TRAIT).size();
//
//			for (int x = 0; x < attributeNum; x++) {
//				Element naturalAttributeElement = (Element) list.getChildren(PERSONALITY_TRAIT).get(x);
//				String name = naturalAttributeElement.getAttributeValue(NAME);
//				String value = naturalAttributeElement.getAttributeValue(VALUE);
//				int intValue = Integer.parseInt(value);
//				// System.out.println(name + " : " + value);
//				result.put(name, intValue);
//			}
//		}
//		return result;
//	}

	private Map<String, Integer> parseSkillsMap(Element personElement) {
		Map<String, Integer> result = new HashMap<String, Integer>();

		List<Element> skillListNodes = personElement.getChildren(SKILL_LIST);
		if ((skillListNodes != null) && (skillListNodes.size() > 0)) {
			Element skillList = skillListNodes.get(0);
			int skillNum = skillList.getChildren(SKILL).size();
			for (int x = 0; x < skillNum; x++) {
				Element skillElement = (Element) skillList.getChildren(SKILL).get(x);
				String name = skillElement.getAttributeValue(NAME_ATTR);
				String level = skillElement.getAttributeValue(LEVEL);
				int intLevel = Integer.parseInt(level);
				result.put(name, intLevel);
			}
		}
		return result;
	}


	private Map<String, Integer> parseRelationshipMap(Element personElement) {
		Map<String, Integer> result = new HashMap<String, Integer>();

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
}
