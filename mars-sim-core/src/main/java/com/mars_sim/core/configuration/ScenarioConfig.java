/*
 * Mars Simulation Project
 * ScenarioConfig.java
 * @date 2023-06-01
 * @author Barry Evans
 */
package com.mars_sim.core.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.jdom2.Document;
import org.jdom2.Element;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.interplanetary.transport.settlement.ArrivingSettlement;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.CoordinatesFormat;
import com.mars_sim.core.person.Crew;
import com.mars_sim.core.person.Member;
import com.mars_sim.core.structure.InitialSettlement;
import com.mars_sim.core.tool.RandomUtil;

/**
 * Loads and maintains a repository Scenario instances from XML files.
 */
public class ScenarioConfig extends UserConfigurableConfig<Scenario> {
	
	
	private static final String PREFIX = "scenario";
	private static final String INITIAL_SETTLEMENT_LIST = "initial-settlement-list";
	private static final String SETTLEMENT_EL = "settlement";
	private static final String CREW_ATTR = "crew";
	private static final String NAME_ATTR = "name";
	private static final String DESCRIPTION_ATTR = "description";
	private static final String TEMPLATE_ATTR = "template";
	private static final String LOCATION_EL = "location";
	private static final String COORDINATE_ATTR = "coordinates";	
	private static final String PERSONS_ATTR = "persons";
	private static final String ROBOTS_ATTR = "robots";
	private static final String SPONSOR_ATTR = "sponsor";
	private static final String SCENARIO_CONFIG = "scenario-configuration";
	private static final String ARRIVING_SETTLEMENT_LIST = "arriving-settlement-list";
	private static final String ARRIVING_SETTLEMENT = "arriving-settlement";
	private static final String ARRIVAL_ATTR = "arrival-in-sols";
	
	// Default scenario
	public static final String[] PREDEFINED_SCENARIOS = {"Default", "Single Settlement"};
	
	public ScenarioConfig(SimulationConfig config) {
		super(PREFIX);

		setXSDName("scenario.xsd", config);
		
		loadDefaults(PREDEFINED_SCENARIOS);
		loadUserDefined();
	}

	/**
	 * Generate an export of a Scenario including any custom UserConfigurables
	 * 
	 * @param item
	 * @return Items exported
	 * @throws IOException 
	 */
	public List<String> createExport(Scenario item,
							 UserConfigurableConfig<Authority> raFactory,
							 UserConfigurableConfig<Crew> crewFactory,
							 OutputStream output) throws IOException {
		
		ZipOutputStream zos = new ZipOutputStream(output);
		
		List<String> manifest = new ArrayList<>();
		
		// Find any Reporting authority & crew that are not bundled
		// ReportingAuthority are exported as one
		Set<UserConfigurable> crewExported = new HashSet<>();
		Set<UserConfigurable> raToExport = new HashSet<>();
		for(InitialSettlement initial : item.getSettlements()) {
			// Extract RA
			queueReportingAuthority(raFactory, raToExport, initial.getSponsor());

			// Don't extract bundled crew or those already exported
			if (initial.getCrew() != null) {
				Crew crew = crewFactory.getItem(initial.getCrew());
				if (!crew.isBundled() && !crewExported.contains(crew)) {
					extractUserConfigurable(crewFactory, crew, zos, manifest);
					crewExported.add(crew);
					
					// Also check the Members are not using an unbundled RA
					for(Member member : crew.getTeam()) {
						queueReportingAuthority(raFactory, raToExport, member.getSponsorCode());
					}
				}
			}
		}
		
		// Export any found unbundled Reporting Authorities
		for(UserConfigurable ra : raToExport) {
			extractUserConfigurable(raFactory, ra, zos, manifest);
		}
		
		// Finally add the Scenario contents itself
		extractUserConfigurable(this, item, zos, manifest);
		zos.close();
		
		return manifest;
	}

	/**
	 * Add unbundled ReportingAuthority to the list
	 * @param raFactory
	 * @param raToExport
	 * @param name
	 */
	private void queueReportingAuthority(UserConfigurableConfig<Authority> raFactory,
			Set<UserConfigurable> raToExport, String name) {
		if (name != null) {
			Authority found = raFactory.getItem(name);
			if (!found.isBundled()) {
				raToExport.add(found);
			}
		}
	}
	
	/**
	 * Adds a single UserConfigurable to a Zip as a separate Zip Entry.
	 * 
	 * @param manifest 
	 * @throws IOException
	 */
	private void extractUserConfigurable(UserConfigurableConfig<? extends UserConfigurable> factory, 
										UserConfigurable item,
										ZipOutputStream zos,
										List<String> manifest) throws IOException {
		
		 // Add the non bundled ReportingAuthority as a ZIP Entry
		 String filename = factory.getItemFilename(item.getName());
		 manifest.add(getEstimateName(filename));
		 
		 zos.putNextEntry(new ZipEntry(filename));
		 InputStream raContents = factory.getRawConfigContents(filename, false);
		 raContents.transferTo(zos);		
		 zos.closeEntry();
	}
	
	/**
	 * Takes the contents.
	 * 
	 * @param contents
	 * @param raFactory 
	 * @throws IOException
	 */
	public List <String> importScenario(InputStream contents,
			 				   UserConfigurableConfig<Authority> raFactory,
			 				   UserConfigurableConfig<Crew> crewFactory)
			 	throws IOException {
		List<String> manifest = new ArrayList<>();
		String targetDirectory = SimulationRuntime.getUserConfigDir();
		
	    ZipInputStream zis = new ZipInputStream(contents);
	    ZipEntry zipEntry = zis.getNextEntry();
	    
	    // Get each entry off the ZIP
	    while (zipEntry != null) {
	    	// All contents go into the User Configuration folder
	        File destFile = new File(targetDirectory, zipEntry.getName());
			manifest.add(getEstimateName(zipEntry.getName()));
			
			// Check for Zip slip
			String canonicalDestinationPath = destFile.getCanonicalPath();
			if (!canonicalDestinationPath.startsWith(targetDirectory)) {
				throw new IOException("Entry is outside of the target directory");
		    }
			
	        // Need to check if the file is already there
	        // write file content and add a comment
	        FileOutputStream fos = new FileOutputStream(destFile);	        
	        zis.transferTo(fos);
	        fos.close();
	   
	        // Get next entry
	        zipEntry = zis.getNextEntry();
	    }
	    zis.closeEntry();
	    zis.close();
	    contents.close();
	    
	    // Reload the user Configurable items after the import
	    raFactory.loadUserDefined();
	    crewFactory.loadUserDefined();
	    loadUserDefined();
	    
	    return manifest;
	}
	
	/**
	 * Converts a Scenario into an XML representation.
	 */
	@Override
	protected Document createItemDoc(Scenario item) {
		Element root = new Element(SCENARIO_CONFIG);
		Document doc = new Document(root);
		
		saveOptionalAttribute(root, NAME_ATTR, item.getName());
		saveOptionalAttribute(root, DESCRIPTION_ATTR, item.getDescription());

		// Add the initial settlements
		Element initialSettlementList = new Element(INITIAL_SETTLEMENT_LIST);
		for (InitialSettlement settlement : item.getSettlements()) {
			Element settlementElement = new Element(SETTLEMENT_EL);
			saveOptionalAttribute(settlementElement, NAME_ATTR, settlement.getName());
			saveOptionalAttribute(settlementElement, TEMPLATE_ATTR, settlement.getSettlementTemplate());
			saveOptionalAttribute(settlementElement, PERSONS_ATTR, Integer.toString(settlement.getPopulationNumber()));
			saveOptionalAttribute(settlementElement, ROBOTS_ATTR, Integer.toString(settlement.getNumOfRobots()));
			saveOptionalAttribute(settlementElement, SPONSOR_ATTR, settlement.getSponsor());
			saveOptionalAttribute(settlementElement, CREW_ATTR, settlement.getCrew());

			Element locationElement = createLocationElement(settlement.getLocation());
			settlementElement.addContent(locationElement);
			
			initialSettlementList.addContent(settlementElement);
		}
		root.addContent(initialSettlementList);
		
		// Add the initial settlements
		Element arrivalList = new Element(ARRIVING_SETTLEMENT_LIST);
		for (ArrivingSettlement arrival : item.getArrivals()) {
			Element aElement = new Element(ARRIVING_SETTLEMENT);
			saveOptionalAttribute(aElement, NAME_ATTR, arrival.getSettlementName());
			saveOptionalAttribute(aElement, TEMPLATE_ATTR, arrival.getTemplate());
			saveOptionalAttribute(aElement, PERSONS_ATTR, Integer.toString(arrival.getPopulationNum()));
			saveOptionalAttribute(aElement, ROBOTS_ATTR, Integer.toString(arrival.getNumOfRobots()));
			saveOptionalAttribute(aElement, SPONSOR_ATTR, arrival.getSponsorCode());
			saveOptionalAttribute(aElement, ARRIVAL_ATTR, Integer.toString(arrival.getArrivalSols()));

			if (arrival.getLandingLocation() != null) {
				Element locationElement = createLocationElement(arrival.getLandingLocation());
				aElement.addContent(locationElement);
			}
			
			arrivalList.addContent(aElement);
		}		
		root.addContent(arrivalList);
		
		return doc;
	}

	@Override
	protected Scenario parseItemXML(Document doc, boolean predefined) {
		Element root = doc.getRootElement();
		String name = root.getAttributeValue(NAME_ATTR);
		String description = root.getAttributeValue(DESCRIPTION_ATTR);

		List<Coordinates> occupiedLocations = new ArrayList<>();
		List<InitialSettlement> is = loadInitialSettlements(root, occupiedLocations);
		List<ArrivingSettlement> arrivals = loadArrivingSettlements(root, occupiedLocations);
		
		return new Scenario(name, description, is, arrivals, predefined);
	}
	
	/**
	 * Loads arriving settlements.
	 * 
	 * @param occupiedLocations
	 * @param settlementDoc DOM document with settlement configuration.
	 * @throws Exception if XML error.
	 */
	private List<ArrivingSettlement> loadArrivingSettlements(Element arrivalElement, List<Coordinates> occupiedLocations) {
		List<ArrivingSettlement> arrivals = new ArrayList<>();

		Element arrivalList = arrivalElement.getChild(ARRIVING_SETTLEMENT_LIST);
		if (arrivalList == null) {
			// An old style scenario configu with no arriving settlements
			return arrivals;
		}

		List<Element> arrivalNodes = arrivalList.getChildren(ARRIVING_SETTLEMENT);
		for (Element settlementElement : arrivalNodes) {

			String name = settlementElement.getAttributeValue(NAME_ATTR);
			String template = settlementElement.getAttributeValue(TEMPLATE_ATTR);

			Coordinates location = parseLocation(settlementElement, occupiedLocations);
			occupiedLocations.add(location);

			String arrivalStr = settlementElement.getAttributeValue(ARRIVAL_ATTR);
			int arrivalSols = Integer.parseInt(arrivalStr);
			if (arrivalSols < 0) {
				throw new IllegalStateException("Arrival sols cannot be less than zero: " + arrivalSols);
			}
			
			String numberPersonsStr = settlementElement.getAttributeValue(PERSONS_ATTR);
			int numOfPeople = Integer.parseInt(numberPersonsStr);
			if (numOfPeople < 0) {
				throw new IllegalStateException("Number of persons cannot be less than zero: " + numOfPeople);
			}

			String numOfRobotsStr = settlementElement.getAttributeValue(ROBOTS_ATTR);
			int numOfRobots = Integer.parseInt(numOfRobotsStr);
			if (numOfRobots < 0) {
				throw new IllegalStateException("Number Of Robots cannot be less than zero: " + numOfRobots);
			}

			String sponsor = settlementElement.getAttributeValue(SPONSOR_ATTR);

			arrivals.add(new ArrivingSettlement(name, template, sponsor,
												arrivalSols, location ,
												numOfPeople, numOfRobots));
		}	
		return arrivals;
	}
	

	/**
	 * Loads initial settlements.
	 * @param occupiedLocations 
	 * 
	 * @param settlementDoc DOM document with settlement configuration.
	 * @throws Exception if XML error.
	 */
	private List<InitialSettlement> loadInitialSettlements(Element scenarioElement, List<Coordinates> occupiedLocations) {
		Element initialSettlementList = scenarioElement.getChild(INITIAL_SETTLEMENT_LIST);
		List<Element> settlementNodes = initialSettlementList.getChildren(SETTLEMENT_EL);
		List<InitialSettlement> initialSettlements = new ArrayList<>();
		
		for (Element settlementElement : settlementNodes) {

			String settlementName = settlementElement.getAttributeValue(NAME_ATTR);
			String template = settlementElement.getAttributeValue(TEMPLATE_ATTR);

			Coordinates location = parseLocation(settlementElement, occupiedLocations);
			occupiedLocations.add(location);
			
			String numberStr = settlementElement.getAttributeValue(PERSONS_ATTR);
			int popNumber = Integer.parseInt(numberStr);
			if (popNumber < 0) {
				throw new IllegalStateException("populationNumber cannot be less than zero: " + popNumber);
			}

			String numOfRobotsStr = settlementElement.getAttributeValue(ROBOTS_ATTR);
			int numOfRobots = Integer.parseInt(numOfRobotsStr);
			if (numOfRobots < 0) {
				throw new IllegalStateException("The number of robots cannot be less than zero: " + numOfRobots);
			}

			String sponsor = settlementElement.getAttributeValue(SPONSOR_ATTR);
			String crew = settlementElement.getAttributeValue(CREW_ATTR);
			
			initialSettlements .add(new InitialSettlement(settlementName, sponsor, template, popNumber, numOfRobots,
										location, crew));
		}
		
		return initialSettlements;
	}


	private static Element createLocationElement(Coordinates location) {
		Element result = new Element(LOCATION_EL);
		saveOptionalAttribute(result, COORDINATE_ATTR, CoordinatesFormat.getDecimalString(location));
		
		return result;
	}

	/**
	 * Parses the lat and lon of a location.
	 * 
	 * @param parent
	 * @return
	 */
	private static Coordinates parseLocation(Element parent, List<Coordinates> occupiedLocations) {
		
		List<Coordinates> locations = new ArrayList<>();
		List<Element> locationNodes = parent.getChildren(LOCATION_EL);
		for(Element locationElement : locationNodes) {
			String decimalString = locationElement.getAttributeValue(COORDINATE_ATTR);
			if (decimalString == null) {
				throw new IllegalStateException("Settlement Location must have coordinates attribute");
			}

			locations.add(CoordinatesFormat.fromString(decimalString));
		}
		
		locations.removeAll(occupiedLocations);
		
		if (locations.isEmpty()) {
			return null;
		}
		
		return RandomUtil.getRandomElement(locations);	
	}
}
