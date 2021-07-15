/**
 * Mars Simulation Project
 * CrewConfig.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.SimulationFiles;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;

/**
 * Provides configuration information about the crew.
 */
public class CrewConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(CrewConfig.class.getName());

	public static final int ALPHA_CREW_ID = 0;
	public static final int BETA_CREW_ID = 1;
	
	private static final String ALPHA_CREW_XML = SimulationConfig.ALPHA_CREW_FILE + SimulationConfig.XML_EXTENSION;
	private static final String BETA_CREW = "beta_crew";
	private static final String BETA_CREW_XML = BETA_CREW + SimulationConfig.XML_EXTENSION;
	private static final String BETA_CREW_BACKUP = BETA_CREW + ".bak";
	private static final String BETA_CREW_DTD = BETA_CREW +  ".dtd";
	
	// Element or attribute names
	private final String CREW_COFIG = "crew-configuration";
	private final String CREW_LIST = "crew-list";
	private final String PERSON = "person";
	
	private final String PERSON_NAME = "person-name";
	private final String ALPHA = "alpha";
	private final String BETA = "beta";
	private final String GENDER = "gender";
	private final String AGE = "age";
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

	private int crewID = 0;
	
	private Crew roster = null;
	private Map<String, Integer> naturalAttributeMap = new HashMap<>();
	private Map<String, Integer> bigFiveMap = new HashMap<>();

	private boolean loaded;
	
	/**
	 * Constructor
	 * 
	 * @param crewDoc the crew config DOM document.
	 */
	public CrewConfig(int crewID) {
		this.crewID = crewID;
		this.loaded = loadCrewDoc();
	}

	public String getName() {
		return roster.getName();
	}
	
	public boolean isLoaded() {
		return loaded;
	}
	
	private boolean loadCrewDoc() {
		String file = "";
		String crewName = null;
		Document doc = null;
		if (crewID == ALPHA_CREW_ID) {
			file = ALPHA_CREW_XML;
			crewName = ALPHA;
			doc = parseXMLFileAsJDOMDocument(file, true);
		}
		else if (crewID == BETA_CREW_ID) {
			file = BETA_CREW_XML;
			crewName = BETA;
			doc = parseXMLFileAsJDOMDocument(file, false);
		}
		
		if (doc == null)
			return false;
		
		//this.doc = doc;
		roster = new Crew(crewName);
		Element personList = doc.getRootElement().getChild(CREW_LIST);
		List<Element> personNodes = personList.getChildren(PERSON);
		for (Element personElement : personNodes) {
			Member m = new Member();
			roster.addMember(m);
			m.setCrewName(crewName);
		
			m.setName(personElement.getAttributeValue(NAME));
			m.setGender(personElement.getAttributeValue(GENDER));
			m.setAge(personElement.getAttributeValue(AGE));
			m.setMBTI(personElement.getAttributeValue(PERSONALITY_TYPE));
			m.setDestination(personElement.getAttributeValue(SETTLEMENT));
			m.setSponsor(ReportingAuthorityType.valueOf(personElement.getAttributeValue(SPONSOR)));
			m.setCountry(personElement.getAttributeValue(COUNTRY));
			m.setJob(personElement.getAttributeValue(JOB));
			
			// Optionals
			m.setDessert(personElement.getAttributeValue(DESSERT));
			m.setMainDish(personElement.getAttributeValue(MAIN_DISH));
			m.setSideDish(personElement.getAttributeValue(SIDE_DISH));
			
			m.setSkillsMap(parseSkillsMap(personElement));
			m.setRelationshipMap(parseRelationshipMap(personElement));

		}
		
		return true;
	}
	
	/**
	 * Parses an XML file into a DOM document.
	 * 
	 * @param filename the path of the file.
	 * @param useDTD   true if the XML DTD should be used.
	 * @return DOM document
	 * @throws IOException
	 * @throws JDOMException
	 * @throws Exception     if XML could not be parsed or file could not be found.
	 */
	private Document parseXMLFileAsJDOMDocument(String filename, boolean useDTD) {
		SAXBuilder builder = null;
		String path = "";
		
		if (useDTD) { // for alpha crew
			builder = new SAXBuilder(null, null, null);
			path = SimulationFiles.getXMLDir();
		}
		else { // for beta crew
			builder = new SAXBuilder();
			path = SimulationFiles.getSaveDir();
		}

//	    builder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
	    Document document = null;
	    
		File f = new File(path, filename);

		if (!f.exists()) {
			return null;
		}
		
		if (f.exists() && f.canRead()) {
	        
	        try {
		        document = builder.build(f);
		    }
		    catch (JDOMException | IOException e)
		    {
		        e.printStackTrace();
		    }
		}
		
	    return document;
	}
	

	/**
	 * Creates an XML document for this crew.
	 * 
	 * @return
	 */
	private Document createCrewDoc() {

		Element root = new Element(CREW_COFIG);
		Document doc = new Document(root);
		
		Element crewList = new Element(CREW_LIST);
		
		List<Element> personList = new ArrayList<>(); 
		
		for (Member person : roster.getTeam()) {
			
			Element personElement = new Element(PERSON);

			personElement.setAttribute(new Attribute(NAME, person.getName()));
			personElement.setAttribute(new Attribute(CREW, roster.getName()));
			personElement.setAttribute(new Attribute(GENDER, person.getGender()));
			personElement.setAttribute(new Attribute(AGE, person.getAge()));
			personElement.setAttribute(new Attribute(PERSONALITY_TYPE, person.getMBTI()));
			personElement.setAttribute(new Attribute(SETTLEMENT, person.getDestination()));
			personElement.setAttribute(new Attribute(SPONSOR, person.getSponsor().name()));
			personElement.setAttribute(new Attribute(COUNTRY, person.getCountry()));
			personElement.setAttribute(new Attribute(JOB, person.getJob()));
			
			//TODO this needs adding
//			personElement.setAttribute(new Attribute(MAIN_DISH, "Bean Sprout Garlic Stir Fry"));
//			personElement.setAttribute(new Attribute(SIDE_DISH, "Roasted Carrot Soup"));
//			personElement.setAttribute(new Attribute(DESSERT, "strawberry"));
//			personElement.setAttribute(new Attribute(ACTIVITY, "Field Work"));
//			
//	        Element traitList = new Element(PERSONALITY_TRAIT_LIST);
//
//	        Element trait0 = new Element(PERSONALITY_TRAIT);
//	        trait0.setAttribute(new Attribute(NAME, "openness"));
//	        trait0.setAttribute(new Attribute(VALUE, "25"));
//	        traitList.addContent(trait0);

//	        personElement.addContent(traitList);
	        
	        personList.add(personElement);
		}

		crewList.addContent(personList);
		doc.getRootElement().addContent(crewList);
	        
        return doc;
	}

	/**
	 * Save the XML document for this crew.
	 * 
	 * @param roster the crew manifest
	 */
	public void save() {

		File betaCrewNew = new File(SimulationFiles.getSaveDir(), BETA_CREW_XML);
		File betaCrewBackup = new File(SimulationFiles.getSaveDir(), BETA_CREW_BACKUP);
		
		// Create save directory if it doesn't exist.
		if (!betaCrewNew.getParentFile().exists()) {
			betaCrewNew.getParentFile().mkdirs();
			logger.config(betaCrewNew.getParentFile().getAbsolutePath() + " created successfully."); 
		}
		
		if (betaCrewNew.exists()) {
			
			try {
				if (Files.deleteIfExists(betaCrewBackup.toPath())) {
					// Delete the beta_crew.bak
				    logger.config("Old beta_crew.bak deleted."); 
				} 
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			try {
				// Back up the previous version of beta_crew.xml as beta_crew.bak
				FileUtils.moveFile(betaCrewNew, betaCrewBackup);
			    logger.config("beta_crew.xml --> beta_crew.bak"); 
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				if (Files.deleteIfExists(betaCrewNew.toPath())) {
					// Delete the beta_crew.xml
				    logger.config("Old beta_crew.xml deleted."); 
				} 

			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		if (!betaCrewNew.exists()) {
			
			Document outputDoc = createCrewDoc();
			//DocType dtd = new DocType(CREW_COFIG, SimulationFiles.getSaveDir() + File.separator + BETA_CREW_DTD);
			//outputDoc.setDocType(dtd);


			XMLOutputter fmt = new XMLOutputter();
			fmt.setFormat(Format.getPrettyFormat());
				
			try (FileOutputStream stream = new FileOutputStream(betaCrewNew);
				 OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8")) {						 
				fmt.output(outputDoc, writer);
			    logger.config("New beta_crew.xml created and saved."); 
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
		}
	}
	
	/**
	 * Gets the number of people configured for the simulation.
	 * 
	 * @param crewID the type of crew (Alpha or Beta)
	 * @return number of people.
	 * @throws Exception if error in XML parsing.
	 */
	public int getNumberOfConfiguredPeople() {
		return roster.getTeam().size();
	}
	

	/**
	 * Gets the configured person's name.
	 * 
	 * @param index the person's index.
	 * @return name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonName(int index) {
		return roster.getTeam().get(index).getName();
	}

	/**
	 * Gets the configured person's gender.
	 * 
	 * @param index the person's index.
	 * @return {@link GenderType} or null if not found.
	 * @throws Exception if error in XML parsing.
	 */
	public GenderType getConfiguredPersonGender(int index) {
			return GenderType.valueOfIgnoreCase(roster.getTeam().get(index).getGender());// alphaCrewGender.get(index))																										// ;
	}

	/**
	 *  Gets the configured person's age.
	 *  
	 * @param index
	 * @return
	 */
	public String getConfiguredPersonAge(int index) {
		return roster.getTeam().get(index).getAge();		
	}
	
	/**
	 * Gets the configured person's MBTI personality type.
	 * 
	 * @param index the person's index.
	 * @return four character string for MBTI ex. "ISTJ". Return null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonPersonalityType(int index) {
		return roster.getTeam().get(index).getMBTI();// alphaCrewPersonality.get(index) ;
	}

	/**
	 * Checks if the personality is introvert.
	 * 
	 * @param index the crew index
	 * @return true if introvert
	 */
	public boolean isIntrovert(int index) {
		return getConfiguredPersonPersonalityType(index).substring(0, 1).equals("I");
	}

	/**
	 * Checks if the personality is extrovert.
	 * 
	 * @param index the crew index
	 * @return true if extrovert
	 */
	public boolean isExtrovert(int index) {
		return getConfiguredPersonPersonalityType(index).substring(0, 1).equals("E");
	}

	/**
	 * Checks if the personality is sensor.
	 * 
	 * @param index the crew index
	 * @return true if sensor
	 */
	public boolean isSensor(int index) {
		return getConfiguredPersonPersonalityType(index).substring(1, 2).equals("S");
	}

	/**
	 * Checks if the personality is intuitive.
	 * 
	 * @param index the crew index
	 * @return true if intuitive
	 */
	public boolean isIntuitive(int index) {
		return getConfiguredPersonPersonalityType(index).substring(1, 2).equals("N");
	}

	/**
	 * Checks if the personality is thinker.
	 * 
	 * @param index the crew index
	 * @return true if thinker
	 */
	public boolean isThinker(int index) {
		return getConfiguredPersonPersonalityType(index).substring(2, 3).equals("T");
	}

	/**
	 * Checks if the personality is feeler.
	 * 
	 * @param index the crew index
	 * @return true if feeler
	 */
	public boolean isFeeler(int index) {
		return getConfiguredPersonPersonalityType(index).substring(2, 3).equals("F");
	}

	/**
	 * Checks if the personality is judger.
	 * 
	 * @param index the crew index
	 * @return true if judger
	 */
	public boolean isJudger(int index) {
		return getConfiguredPersonPersonalityType(index).substring(3, 4).equals("J");
	}

	/**
	 * Checks if the personality is perceiver.
	 * 
	 * @param index the crew index
	 * @return true if perceiver
	 */
	public boolean isPerceiver(int index) {
		return getConfiguredPersonPersonalityType(index).substring(3, 4).equals("P");
	}

	
	/**
	 * Gets the configured person's job.
	 * 
	 * @param index the person's index.
	 * @return the job name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonJob(int index) {
		return roster.getTeam().get(index).getJob();// alphaCrewJob.get(index) ;
	}

	/**
	 * Gets the configured person's country.
	 * 
	 * @param index the person's index.
	 * @return the job name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonCountry(int index) {
		return roster.getTeam().get(index).getCountry();// alphaCrewJob.get(index) ;
	}

	/**
	 * Gets the configured person's sponsor.
	 * 
	 * @param index the person's index.
	 * @return the job name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public ReportingAuthorityType getConfiguredPersonSponsor(int index) {
		return roster.getTeam().get(index).getSponsor();// alphaCrewJob.get(index) ;
	}

	/**
	 * Gets the configured person's starting settlement.
	 * 
	 * @param index the person's index.
	 * @return the settlement name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonDestination(int index) {
		return roster.getTeam().get(index).getDestination();// alphaCrewDestination.get(index);
	}

	/**
	 * Sets the name of a member of the alpha crew
	 * 
	 * @param index
	 * @param name
	 */
	public void setPersonName(int index, String value) {
		roster.getTeam().get(index).setName(value);
	}

	/**
	 * Sets the personality of a member of the alpha crew
	 * 
	 * @param index
	 * @param personality
	 */
	public void setPersonPersonality(int index, String value) {
		roster.getTeam().get(index).setMBTI(value);
	}

	/**
	 * Sets the gender of a member of the alpha crew
	 * 
	 * @param index
	 * @param gender
	 */
	public void setPersonGender(int index, String value) {
		roster.getTeam().get(index).setGender(value);
	}

	/**
	 * Sets the age of a member of the alpha crew
	 * 
	 * @param index
	 * @param age
	 */
	public void setPersonAge(int index, String value) {
		roster.getTeam().get(index).setAge(value);
	}
	
	/**
	 * Sets the job of a member of the alpha crew
	 * 
	 * @param index
	 * @param job
	 */
	public void setPersonJob(int index, String value) {
		roster.getTeam().get(index).setJob(value);
	}

	/**
	 * Sets the country of a member of the alpha crew
	 * 
	 * @param index
	 * @param country
	 */
	public void setPersonCountry(int index, String value) {
		roster.getTeam().get(index).setCountry(value);
	}

	/**
	 * Sets the sponsor of a member of the alpha crew
	 * 
	 * @param index
	 * @param sponsor
	 */
	public void setPersonSponsor(int index, ReportingAuthorityType value) {
		roster.getTeam().get(index).setSponsor(value);
	}

	/**
	 * Sets the destination of a member of the alpha crew
	 * 
	 * @param index
	 * @param destination
	 */
	public void setPersonDestination(int index, String value) {
		roster.getTeam().get(index).setDestination(value);
	}

	public void setMainDish(int index, String value) {
		roster.getTeam().get(index).setMainDish(value);
	}
	
	public void setSideDish(int index, String value) {
		roster.getTeam().get(index).setSideDish(value);
	}
	
	public void setDessert(int index, String value) {
		roster.getTeam().get(index).setDessert(value);
	}
	
	public void setActivity(int index, String value) {
		roster.getTeam().get(index).setActivity(value);
	}
	
	public Map<String, Integer> getNaturalAttributeMap(int index) {
		return naturalAttributeMap;
	}
		
	/**
	 * Gets a map of the configured person's natural attributes.
	 * 
	 * @param index the person's index.
	 * @return map of natural attributes (empty map if not found).
	 * @throws Exception if error in XML parsing.
	 */
	public Map<String, Integer> computeNaturalAttributeMap(Document doc, int index) {
		Map<String, Integer> result = new HashMap<String, Integer>();
		Element personList = doc.getRootElement().getChild(CREW_LIST);
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

	/**
	 * Gets a map of the configured person's skills.
	 * 
	 * @param index the person's index.
	 * @return map of skills (empty map if not found).
	 * @throws Exception if error in XML parsing.
	 */
	public Map<String, Integer> getSkillMap(int index) {
		return roster.getTeam().get(index).getSkillMap();
	}
	
	private Map<String, Integer> parseSkillsMap(Element personElement) {
		Map<String, Integer> result = new HashMap<String, Integer>();

		List<Element> skillListNodes = personElement.getChildren(SKILL_LIST);
		if ((skillListNodes != null) && (skillListNodes.size() > 0)) {
			Element skillList = skillListNodes.get(0);
			int skillNum = skillList.getChildren(SKILL).size();
			for (int x = 0; x < skillNum; x++) {
				Element skillElement = (Element) skillList.getChildren(SKILL).get(x);
				String name = skillElement.getAttributeValue(NAME);
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
		return roster.getTeam().get(index).getRelationshipMap();
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

	/**
	 * Gets the configured person's favorite main dish.
	 * 
	 * @param index the person's index.
	 * @return the name of the favorite main dish name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getFavoriteMainDish(int index) {
		return roster.getTeam().get(index).getMainDish();
	}

	/**
	 * Gets the configured person's favorite side dish.
	 * 
	 * @param index the person's index.
	 * @return the name of the favorite side dish name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getFavoriteSideDish(int index) {
		return roster.getTeam().get(index).getSideDish();
	}

	/**
	 * Gets the configured person's favorite dessert.
	 * 
	 * @param index the person's index.
	 * @return the name of the favorite dessert name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getFavoriteDessert(int index) {
		return roster.getTeam().get(index).getDessert();
	}

	/**
	 * Gets the configured person's favorite activity.
	 * 
	 * @param index the person's index.
	 * @return the name of the favorite activity name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getFavoriteActivity(int index) {
		return roster.getTeam().get(index).getActivity();
	}

}
