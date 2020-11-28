/**
 * Mars Simulation Project
 * CrewConfig.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jdom2.Attribute;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mars_sim.msp.core.Simulation;

/**
 * Provides configuration information about the crew.
 */
public class CrewConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(CrewConfig.class.getName());

	public static final int ALPHA_CREW_ID = 0;
	public static final int BETA_CREW_ID = 1;
	
	private static final String BETA_CREW = "beta_crew.xml";
	private static final String BETA_CREW_BACKUP = "beta_crew.bak";
	private static final String BETA_CREW_DTD = "beta_crew.dtd";

	// A map of crew members
	private Map<Integer, Crew> roster = new ConcurrentHashMap<>();

	private int selectedCrew = 0;
	
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

	private Document alphaCrewDoc;
	private Document betaCrewDoc;
	
	/**
	 * Constructor
	 * 
	 * @param crewDoc the crew config DOM document.
	 */
	public CrewConfig(Document alphaCrewDoc) {//, Document betaCrewDoc) {
		this.alphaCrewDoc = alphaCrewDoc;
//		this.betaCrewDoc = betaCrewDoc;
//		root = crewDoc.getRootElement();
		
		int size = getNumberOfConfiguredPeople(ALPHA_CREW_ID);

		// Load a list of crew
		for (int x = 0; x < size; x++) {
			// Create this member
			createMember(x, ALPHA_CREW_ID);
		}
	}

	public boolean loadCrewDoc() {
		this.betaCrewDoc = parseXMLFileAsJDOMDocument(BETA_CREW, false);

		if (betaCrewDoc == null)
			return false;
		
		int size = getNumberOfConfiguredPeople(BETA_CREW_ID);

		// Load a list of crew
		for (int x = 0; x < size; x++) {
			// Create this member
			createMember(x, BETA_CREW_ID);
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
//	    SAXBuilder builder = new SAXBuilder(useDTD);
	    SAXBuilder builder = new SAXBuilder(null, null, null);
	    
	    Document document = null;
	    
		File f = new File(Simulation.SAVE_DIR, BETA_CREW);

		if (!f.exists()) {
			return null;
		}
		
		if (f.exists() && f.canRead()) {
	        
	        try {
//	        	FileInputStream fi = new FileInputStream(Simulation.XML_DIR);
		        document = builder.build(f);
		    }
		    catch (JDOMException | IOException e)
		    {
		        e.printStackTrace();
		    }
		}
		
	    return document;
	}
	
	public void setSelectedCrew(int value) {
		selectedCrew = value;
	}
	
	public int getSelectedCrew() {
		return selectedCrew;
	}
	
	/**
	 * Creates an XML document for this crew.
	 * 
	 * @param roster the crew manifest
	 * @return
	 */
	public Document createDoc(List<List<String>> roster) {

		Element root = new Element(CREW_COFIG);
		betaCrewDoc = new Document(root);
		
		Element crewList = new Element(CREW_LIST);
		
		List<Element> personList = new CopyOnWriteArrayList<>(); 
		
		int num = 5;
		
		for (int x = 0; x < num; x++) {
			List<String> person = roster.get(x);
			
			Element personElement = new Element(PERSON);

			personElement.setAttribute(new Attribute(NAME, person.get(0)));
			personElement.setAttribute(new Attribute(CREW, BETA));
			personElement.setAttribute(new Attribute(GENDER, person.get(1)));
			personElement.setAttribute(new Attribute(AGE, person.get(2)));
			personElement.setAttribute(new Attribute(PERSONALITY_TYPE, person.get(3)));
			personElement.setAttribute(new Attribute(SETTLEMENT, person.get(4)));//"Schiaparelli Point"));
			personElement.setAttribute(new Attribute(SPONSOR, person.get(5)));//"Mars Society (MS)"));
			personElement.setAttribute(new Attribute(COUNTRY, person.get(6)));//"USA"));
			personElement.setAttribute(new Attribute(JOB, person.get(7)));//"Botanist"));
			
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
		betaCrewDoc.getRootElement().addContent(crewList);
	        
//        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
//        try {
//			xmlOutputter.output(document, System.out);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

        return betaCrewDoc;
	}

	/**
	 * Save the XML document for this crew.
	 * 
	 * @param roster the crew manifest
	 */
	public void writeCrewXML(List<List<String>> roster) {

		File betaCrewNew = new File(Simulation.SAVE_DIR, BETA_CREW);
		File betaCrewBackup = new File(Simulation.SAVE_DIR, BETA_CREW_BACKUP);
		
		// Create save directory if it doesn't exist.
		if (!betaCrewNew.getParentFile().exists()) {
			betaCrewNew.getParentFile().mkdirs();
			logger.config(Simulation.SAVE_DIR + " created successfully."); 
		}
		
		if (betaCrewNew.exists()) {
			
			try {
				if (Files.deleteIfExists(betaCrewBackup.toPath())) {
					// Delete the beta_crew.bak
				    logger.config("Old beta_crew.bak deleted."); 
				} 
//				else { 
//					logger.config("beta_crew.bak does not exist yet."); 
//				}
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
//				else { 
//					logger.config("beta_crew.xml does not exist yet."); 
//				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		if (!betaCrewNew.exists()) {
			
			FileOutputStream stream = null;

			try {
				Document outputDoc = createDoc(roster);
				DocType dtd = new DocType(CREW_COFIG, Simulation.SAVE_DIR + File.separator + BETA_CREW_DTD);
				outputDoc.setDocType(dtd);

//				InputStream in = getClass().getResourceAsStream("/dtd/" + BETA_CREW_DTD);
//				IOUtils.copy(in, new FileOutputStream(new File(Simulation.SAVE_DIR, BETA_CREW_DTD)));

				XMLOutputter fmt = new XMLOutputter();
				fmt.setFormat(Format.getPrettyFormat());
				
				// Print out the beta_crew.xml
//				fmt.output(outputDoc, System.out);
				
				stream = new FileOutputStream(betaCrewNew);
						 
				OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
				fmt.output(outputDoc, writer);
			    logger.config("New beta_crew.xml created and saved."); 
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage());
			} finally {
				IOUtils.closeQuietly(stream);
			}
		}
	}
	
	/**
	 * Gets the number of people configured for the simulation.
	 * 
	 * @return number of people.
	 * @throws Exception if error in XML parsing.
	 */
	public int getNumberOfConfiguredPeople(int crewID) {
		Element personList = null;
		if (crewID == ALPHA_CREW_ID) {
			personList = alphaCrewDoc.getRootElement().getChild(CREW_LIST);
		}
		else if (crewID == BETA_CREW_ID) {
			if (betaCrewDoc == null)
				loadCrewDoc();
			personList = betaCrewDoc.getRootElement().getChild(CREW_LIST);
		}		
		
		List<Element> personNodes = personList.getChildren(PERSON);
		
		int crewNum = 0;
		if (personNodes != null) {
			crewNum = personNodes.size();
		}
			
		return crewNum;
	}

//	/**
//	 * Get person's crew designation
//	 * 
//	 * @param index the person's index.
//	 * @return name or null if none.
//	 * @throws Exception if error in XML parsing.
//	 */
//	public int loadRoster(int index) {
//		// retrieve the person's crew designation
//		String crewString = getValueAsString(ALPHA_CREW_ID, index, CREW);
//
//		if (crewString == null) {
//			throw new IllegalStateException("The crew designation of a person is null");
//
//		} else {
//
//			boolean oldCrewName = false;
//
//			Iterator<Crew> i = roster.iterator();
//			while (i.hasNext()) {
//				// e.g. alpha crew, beta crew, etc.
//				Crew crew = i.next();
//				// if the name does not exist, create a new crew with this name
//				if (crewString.equals(crew.getName())) {
//					oldCrewName = true;
//					// add a new member
//					// Member m = new Member();
//					crew.add(new Member());
//					break;
//				}
//			}
//
//			// if this is crew name doesn't exist
//			if (!oldCrewName) {
//				Crew c = new Crew(crewString);
//				c.add(new Member());
//				roster.add(c);
//			}
//
//			return roster.size() - 1;
//		}
//	}

	/**
	 * Creates a member
	 * 
	 * @param index the person's index.
	 */
	public void createMember(int index, int crewID) {
		String crewString = getValueAsString(index, crewID, CREW);
		
		if (roster.containsKey(crewID)) {
			Crew c = roster.get(crewID);
			// add a new member
			c.addMember(new Member());
			roster.put(crewID, c);
		}
		
		else {
		// if this crew doesn't exist yet
			Crew c = new Crew(crewString);
			
			if (crewID == ALPHA_CREW_ID)
				c.setCrewName(ALPHA);
			else if (crewID == BETA_CREW_ID)
				c.setCrewName(BETA);
			
			// add a new member
			c.addMember(new Member());
			roster.put(crewID, c);
		}
	}
	
	/**
	 * Gets the configured person's name.
	 * 
	 * @param index the person's index.
	 * @param crewID
	 * @param loadFromXML true if it is loading from crew.xml (instead of the roster)
	 * @return name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonCrew(int index, int crewID, boolean loadFromXML) {
		if (loadFromXML) {
			return getValueAsString(index, crewID, CREW);
		}
		
		else if (roster.get(crewID) != null) {
			if (roster.get(crewID).getTeam().get(index).getCrewName() != null) {
				return roster.get(crewID).getTeam().get(index).getCrewName();
			} 
			
			else {
				return getValueAsString(index, crewID, CREW);
			}
		} 
		
		else {
			return getValueAsString(index, crewID, NAME);
		}
		
	}
	
	/**
	 * Gets the configured person's name.
	 * 
	 * @param index the person's index.
	 * @param crewID
	 * @param loadFromXML true if it is loading from crew.xml (instead of the roster)
	 * @return name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonName(int index, int crewID, boolean loadFromXML) {
		if (loadFromXML) {
			return getValueAsString(index, crewID, NAME);
		}
		
		else if (roster.get(crewID) != null) {
			if (roster.get(crewID).getTeam().get(index).getName() != null) {
				return roster.get(crewID).getTeam().get(index).getName();
			} 
			
			else {
				return getValueAsString(index, crewID, NAME);
			}
		} 
		
		else {
			return getValueAsString(index, crewID, NAME);
		}
		
	}

	/**
	 * Gets the configured person's gender.
	 * 
	 * @param index the person's index.
	 * @return {@link GenderType} or null if not found.
	 * @throws Exception if error in XML parsing.
	 */
	public GenderType getConfiguredPersonGender(int index, int crewID, boolean loadFromXML) {
		if (loadFromXML)
			return GenderType.valueOfIgnoreCase(getValueAsString(index, crewID, GENDER));
		else if (roster.get(crewID).getTeam().get(index).getGender() != null)
			return GenderType.valueOfIgnoreCase(roster.get(crewID).getTeam().get(index).getGender());// alphaCrewGender.get(index))																										// ;
		else
			return GenderType.valueOfIgnoreCase(getValueAsString(index, crewID, GENDER));
	}

	/**
	 *  Gets the configured person's age.
	 *  
	 * @param index
	 * @param crewID
	 * @param loadFromXML
	 * @return
	 */
	public String getConfiguredPersonAge(int index, int crewID, boolean loadFromXML) {
		if (loadFromXML)
			return getValueAsString(index, crewID, AGE);
		else if (roster.get(crewID).getTeam().get(index).getAge() != null)
			return roster.get(crewID).getTeam().get(index).getAge();		
		else 
			return getValueAsString(index, crewID, AGE);
	}
	
	/**
	 * Gets the configured person's MBTI personality type.
	 * 
	 * @param index the person's index.
	 * @return four character string for MBTI ex. "ISTJ". Return null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonPersonalityType(int index, int crewID, boolean loadFromXML) {
		if (loadFromXML)
			return getValueAsString(index, crewID, PERSONALITY_TYPE);
		else if (roster.get(crewID).getTeam().get(index).getMBTI() != null)
			return roster.get(crewID).getTeam().get(index).getMBTI();// alphaCrewPersonality.get(index) ;
		else
			return getValueAsString(index, crewID, PERSONALITY_TYPE);
	}

	/**
	 * Checks if the personality is introvert.
	 * 
	 * @param index the crew index
	 * @return true if introvert
	 */
	public boolean isIntrovert(int index, int crewID, boolean loadFromXML) {
		return getConfiguredPersonPersonalityType(index, crewID, loadFromXML).substring(0, 1).equals("I");
	}

	/**
	 * Checks if the personality is extrovert.
	 * 
	 * @param index the crew index
	 * @return true if extrovert
	 */
	public boolean isExtrovert(int index, int crewID, boolean loadFromXML ) {
		return getConfiguredPersonPersonalityType(index, crewID, loadFromXML).substring(0, 1).equals("E");
	}

	/**
	 * Checks if the personality is sensor.
	 * 
	 * @param index the crew index
	 * @return true if sensor
	 */
	public boolean isSensor(int index, int crewID, boolean loadFromXML) {
		return getConfiguredPersonPersonalityType(index, crewID, loadFromXML).substring(1, 2).equals("S");
	}

	/**
	 * Checks if the personality is intuitive.
	 * 
	 * @param index the crew index
	 * @return true if intuitive
	 */
	public boolean isIntuitive(int index, int crewID, boolean loadFromXML) {
		return getConfiguredPersonPersonalityType(index, crewID, loadFromXML).substring(1, 2).equals("N");
	}

	/**
	 * Checks if the personality is thinker.
	 * 
	 * @param index the crew index
	 * @return true if thinker
	 */
	public boolean isThinker(int index, int crewID, boolean loadFromXML) {
		return getConfiguredPersonPersonalityType(index, crewID, loadFromXML).substring(2, 3).equals("T");
	}

	/**
	 * Checks if the personality is feeler.
	 * 
	 * @param index the crew index
	 * @return true if feeler
	 */
	public boolean isFeeler(int index, int crewID, boolean loadFromXML) {
		return getConfiguredPersonPersonalityType(index, crewID, loadFromXML).substring(2, 3).equals("F");
	}

	/**
	 * Checks if the personality is judger.
	 * 
	 * @param index the crew index
	 * @return true if judger
	 */
	public boolean isJudger(int index, int crewID, boolean loadFromXML) {
		return getConfiguredPersonPersonalityType(index, crewID, loadFromXML).substring(3, 4).equals("J");
	}

	/**
	 * Checks if the personality is perceiver.
	 * 
	 * @param index the crew index
	 * @return true if perceiver
	 */
	public boolean isPerceiver(int index, int crewID, boolean loadFromXML) {
		return getConfiguredPersonPersonalityType(index, crewID, loadFromXML).substring(3, 4).equals("P");
	}

	
	/**
	 * Gets the configured person's job.
	 * 
	 * @param index the person's index.
	 * @return the job name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonJob(int index, int crewID, boolean loadFromXML) {
		if (loadFromXML)
			return getValueAsString(index, crewID, JOB);
		else if (roster.get(crewID).getTeam().get(index).getJob() != null)
			return roster.get(crewID).getTeam().get(index).getJob();// alphaCrewJob.get(index) ;
		else
			return getValueAsString(index, crewID, JOB);
	}

	/**
	 * Gets the configured person's country.
	 * 
	 * @param index the person's index.
	 * @return the job name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonCountry(int index, int crewID, boolean loadFromXML) {
		if (loadFromXML)
			return getValueAsString(index, crewID, COUNTRY);
		else if (roster.get(crewID).getTeam().get(index).getCountry() != null)
			return roster.get(crewID).getTeam().get(index).getCountry();// alphaCrewJob.get(index) ;
		else
			return getValueAsString(index, crewID, COUNTRY);
	}

	/**
	 * Gets the configured person's sponsor.
	 * 
	 * @param index the person's index.
	 * @return the job name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonSponsor(int index, int crewID, boolean loadFromXML) {
		if (loadFromXML)
			return getValueAsString(index, crewID, SPONSOR);	
		else if (roster.get(crewID).getTeam().get(index).getSponsor() != null)
			return roster.get(crewID).getTeam().get(index).getSponsor();// alphaCrewJob.get(index) ;
		else
			return getValueAsString(index, crewID, SPONSOR);
	}

	/**
	 * Gets the configured person's starting settlement.
	 * 
	 * @param index the person's index.
	 * @return the settlement name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredPersonDestination(int index, int crewID, boolean loadFromXML) {
		if (loadFromXML)
			return getValueAsString(index, crewID, SETTLEMENT);
		else if (roster.get(crewID).getTeam().get(index).getDestination() != null)
			return roster.get(crewID).getTeam().get(index).getDestination();// alphaCrewDestination.get(index);
		else
			return getValueAsString(index, crewID, SETTLEMENT);
	}

	/**
	 * Sets the name of a member of the alpha crew
	 * 
	 * @param index
	 * @param name
	 */
	public void setPersonName(int index, String value, int crewID) {
		if (roster.get(crewID).getTeam().get(index).getName() == null)
			roster.get(crewID).getTeam().get(index).setName(value);
	}

	/**
	 * Sets the name of a member of the alpha crew
	 * 
	 * @param index
	 * @param name
	 */
	public void setPersonCrewName(int index, String value, int crewID) {
		if (roster.get(crewID).getTeam().get(index).getCrewName() == null)
			roster.get(crewID).getTeam().get(index).setCrewName(value);
	}
	
	/**
	 * Sets the personality of a member of the alpha crew
	 * 
	 * @param index
	 * @param personality
	 */
	public void setPersonPersonality(int index, String value, int crewID) {
		if (roster.get(crewID).getTeam().get(index).getMBTI() == null)
			roster.get(crewID).getTeam().get(index).setMBTI(value);
	}

	/**
	 * Sets the gender of a member of the alpha crew
	 * 
	 * @param index
	 * @param gender
	 */
	public void setPersonGender(int index, String value, int crewID) {
		if (roster.get(crewID).getTeam().get(index).getGender() == null)
			roster.get(crewID).getTeam().get(index).setGender(value);
	}

	/**
	 * Sets the age of a member of the alpha crew
	 * 
	 * @param index
	 * @param age
	 */
	public void setPersonAge(int index, String value, int crewID) {
		if (roster.get(crewID).getTeam().get(index).getAge() == null)
			roster.get(crewID).getTeam().get(index).setAge(value);
	}
	
	/**
	 * Sets the job of a member of the alpha crew
	 * 
	 * @param index
	 * @param job
	 */
	public void setPersonJob(int index, String value, int crewID) {
		if (roster.get(crewID).getTeam().get(index).getJob() == null)
			roster.get(crewID).getTeam().get(index).setJob(value);
	}

	/**
	 * Sets the country of a member of the alpha crew
	 * 
	 * @param index
	 * @param country
	 */
	public void setPersonCountry(int index, String value, int crewID) {
		if (roster.get(crewID).getTeam().get(index).getCountry() == null)
			roster.get(crewID).getTeam().get(index).setCountry(value);
	}

	/**
	 * Sets the sponsor of a member of the alpha crew
	 * 
	 * @param index
	 * @param sponsor
	 */
	public void setPersonSponsor(int index, String value, int crewID) {
		if (roster.get(crewID).getTeam().get(index).getSponsor() == null)
			roster.get(crewID).getTeam().get(index).setSponsor(value);
	}

	/**
	 * Sets the destination of a member of the alpha crew
	 * 
	 * @param index
	 * @param destination
	 */
	public void setPersonDestination(int index, String value, int crewID) {
		if (roster.get(crewID).getTeam().get(index).getDestination() == null)
			roster.get(crewID).getTeam().get(index).setDestination(value);
	}

	public void setMainDish(int index, String value, int crewID) {
		if (roster.get(crewID).getTeam().get(index).getMainDish() == null)
			roster.get(crewID).getTeam().get(index).setMainDish(value);
	}
	
	public void setSideDish(int index, String value, int crewID) {
		if (roster.get(crewID).getTeam().get(index).getSideDish() == null)
			roster.get(crewID).getTeam().get(index).setSideDish(value);
	}
	
	public void setDessert(int index, String value, int crewID) {
		if (roster.get(crewID).getTeam().get(index).getDessert() == null)
			roster.get(crewID).getTeam().get(index).setDessert(value);
	}
	
	public void setActivity(int index, String value, int crewID) {
		if (roster.get(crewID).getTeam().get(index).getActivity() == null)
			roster.get(crewID).getTeam().get(index).setActivity(value);
	}
	
	/**
	 * Gets a map of the configured person's natural attributes.
	 * 
	 * @param index the person's index.
	 * @return map of natural attributes (empty map if not found).
	 * @throws Exception if error in XML parsing.
	 */
	public Map<String, Integer> getNaturalAttributeMap(int index) {
		Map<String, Integer> result = new ConcurrentHashMap<String, Integer>();
		Element personList = alphaCrewDoc.getRootElement().getChild(CREW_LIST);
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
		Map<String, Integer> result = new ConcurrentHashMap<String, Integer>();
		Element personList = alphaCrewDoc.getRootElement().getChild(CREW_LIST);
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
	 * @param an index
	 * @return a String
	 */
	private String getValueAsString(int index, int crewID, String param) {
		if (crewID == ALPHA_CREW_ID) {
			Element personList = alphaCrewDoc.getRootElement().getChild(CREW_LIST);
			Element personElement = (Element) personList.getChildren(PERSON).get(index);
			return personElement.getAttributeValue(param);
		}
		else if (crewID == BETA_CREW_ID) {
			if (betaCrewDoc == null)
				loadCrewDoc();
			Element personList = betaCrewDoc.getRootElement().getChild(CREW_LIST);
			Element personElement = (Element) personList.getChildren(PERSON).get(index);
			return personElement.getAttributeValue(param);
		}
		return null;
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
	public Map<String, Integer> getSkillMap(int index, int crewID) {
		Map<String, Integer> result = new ConcurrentHashMap<String, Integer>();
		Element personList = null;
		if (crewID == ALPHA_CREW_ID) {
			personList = alphaCrewDoc.getRootElement().getChild(CREW_LIST);
		}
		
		else if (crewID == BETA_CREW_ID) {
			personList = betaCrewDoc.getRootElement().getChild(CREW_LIST);
		}
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
	public Map<String, Integer> getRelationshipMap(int index, int crewID) {
		Map<String, Integer> result = new ConcurrentHashMap<String, Integer>();
		Element personList = null;
		if (crewID == ALPHA_CREW_ID) {
			personList = alphaCrewDoc.getRootElement().getChild(CREW_LIST);
		}
		
		else if (crewID == BETA_CREW_ID) {
			personList = betaCrewDoc.getRootElement().getChild(CREW_LIST);
		}
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
	public String getFavoriteMainDish(int index, int crewID) {
		if (roster.get(crewID).getTeam().get(index).getMainDish() != null)
			return roster.get(crewID).getTeam().get(index).getMainDish();
		else
			return getValueAsString(index, crewID, MAIN_DISH);
	}

	/**
	 * Gets the configured person's favorite side dish.
	 * 
	 * @param index the person's index.
	 * @return the name of the favorite side dish name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getFavoriteSideDish(int index, int crewID) {
		if (roster.get(crewID).getTeam().get(index).getSideDish() != null)
			return roster.get(crewID).getTeam().get(index).getSideDish();
		else
			return getValueAsString(index, crewID, SIDE_DISH);
	}

	/**
	 * Gets the configured person's favorite dessert.
	 * 
	 * @param index the person's index.
	 * @return the name of the favorite dessert name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getFavoriteDessert(int index, int crewID) {
		if (roster.get(crewID).getTeam().get(index).getDessert() != null)
			return roster.get(crewID).getTeam().get(index).getDessert();
		else
			return getValueAsString(index, crewID, DESSERT);
	}

	/**
	 * Gets the configured person's favorite activity.
	 * 
	 * @param index the person's index.
	 * @return the name of the favorite activity name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getFavoriteActivity(int index, int crewID) {
		if (roster.get(crewID).getTeam().get(index).getActivity() != null)
			return roster.get(crewID).getTeam().get(index).getActivity();
		else
			return getValueAsString(index, crewID, ACTIVITY);
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		alphaCrewDoc = null;
		roster = null;
	}
}
