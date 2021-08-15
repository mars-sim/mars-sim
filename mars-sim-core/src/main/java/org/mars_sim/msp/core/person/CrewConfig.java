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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;

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

/**
 * Provides configuration information about the crew.
 */
public class CrewConfig {

	private static final Logger logger = Logger.getLogger(CrewConfig.class.getName());

	private static final String CREW_PREFIX = "crew_";
	private static final String CREW_BACKUP = ".bak";
	
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
	private String [] PREDEFINED_CREWS = {"crew_alpha"};
	private Map<String, Integer> bigFiveMap = new HashMap<>();

	private Map<String,Crew> knownCrews = new TreeMap<>();
	
	/**
	 * Constructor
	 */
	public CrewConfig() {
		for (String name : PREDEFINED_CREWS) {
			loadCrew(name, true);
		}
		
		// Scan save crews
		File savedDir = new File(SimulationFiles.getSaveDir());
	    String[] list = savedDir.list();
	    for (String userFile : list) {
	    	if (userFile.startsWith(CREW_PREFIX)
	    			&& userFile.endsWith(SimulationConfig.XML_EXTENSION)) {
	    		loadCrew(userFile, false);
	    	}
		}
	}
	
	/**
	 * Get a crew by it's name
	 * @param name
	 * @return
	 */
	public Crew getCrew(String name) {
		return knownCrews.get(name);
	}
	
	/**
	 * Delete the crew
	 * @param name
	 */
	public void deleteCrew(String name) {
		knownCrews.remove(name);

		String filename = getCrewFilename(name);
		File crewFile = new File(SimulationFiles.getSaveDir(), filename + SimulationConfig.XML_EXTENSION);
		logger.info("Deleting crew file " + crewFile.getAbsolutePath());
		crewFile.delete();
	}
	
	/**
	 * Load a create from external or bundled XML.
	 * @param name
	 * @return
	 */
	private void loadCrew(String file, boolean predefined) {
		
		Document doc = parseXMLFileAsJDOMDocument(file, predefined);
		if (doc == null) {
			throw new IllegalStateException("Can not find " + file);
		}
		Element crewEl = doc.getRootElement();
		String name = crewEl.getAttributeValue(NAME_ATTR);
		if (name == null) {
			name = file.substring(CREW_PREFIX.length(),
						file.length() - SimulationConfig.XML_EXTENSION.length());
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
		
		logger.config("Loaded Crew " + name + " from " + file);
		knownCrews.put(name, roster);
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
			builder = new SAXBuilder();
		    builder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		    builder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			path = SimulationFiles.getXMLDir();
			
			// Alpha is a bundled XML so needs to be copied out
			SimulationConfig.instance().getBundledXML(filename);
			filename += SimulationConfig.XML_EXTENSION;
		}
		else { // for beta crew
			builder = new SAXBuilder();
		    builder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		    builder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			path = SimulationFiles.getSaveDir();
		}

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
	private Document createCrewDoc(Crew roster) {

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
	 * Save an attribute to a Element if it is defined
	 * @param personElement
	 * @param activity2
	 * @param activity3
	 */
	private static void saveOptionalAttribute(Element node, String attrName, String value) {
		if (value != null) {
			node.setAttribute(new Attribute(attrName, value));
		}
	}

	private static String getCrewFilename(String crewName) {
		// Replace spaces 
		return CREW_PREFIX + crewName.toLowerCase().replace(' ', '_');
	}
	
	/**
	 * Save the XML document for this crew.
	 * 
	 * @param roster the crew manifest
	 */
	public void save(Crew crew) {

		String filename = getCrewFilename(crew.getName());
		File crewFile = new File(SimulationFiles.getSaveDir(), filename + SimulationConfig.XML_EXTENSION);
		File crewBackup = new File(SimulationFiles.getSaveDir(), filename + CREW_BACKUP);
		
		// Create save directory if it doesn't exist.
		if (!crewFile.getParentFile().exists()) {
			crewFile.getParentFile().mkdirs();
			logger.config(crewFile.getParentFile().getAbsolutePath() + " created successfully."); 
		}
		
		if (crewFile.exists()) {
			
			try {
				if (Files.deleteIfExists(crewBackup.toPath())) {
					// Delete the beta_crew.bak
				    logger.config("Old " + crewBackup.getName() + " deleted."); 
				} 
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			try {
				// Back up the previous version of beta_crew.xml as beta_crew.bak
				FileUtils.moveFile(crewFile, crewBackup);
			    logger.config("beta_crew.xml --> beta_crew.bak"); 
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				if (Files.deleteIfExists(crewFile.toPath())) {
					// Delete the beta_crew.xml
				    logger.config("Old " + crewFile.getName() + " deleted."); 
				} 

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (!crewFile.exists()) {
			Document outputDoc = createCrewDoc(crew);
			XMLOutputter fmt = new XMLOutputter();
			fmt.setFormat(Format.getPrettyFormat());
				
			try (FileOutputStream stream = new FileOutputStream(crewFile);
				 OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8")) {						 
				fmt.output(outputDoc, writer);
			    logger.config("New " + crewFile.getName() + " created and saved."); 
			    stream.close();
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
		}
		
		// Update or register new crew
		knownCrews.put(crew.getName(), crew);
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

	public List<String> getKnownCrewNames() {
		List<String> names = new ArrayList<>(knownCrews.keySet());
		Collections.sort(names);
		return names;
	}
}
