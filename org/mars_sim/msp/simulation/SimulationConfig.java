/**
 * Mars Simulation Project
 * SimulationProperties.java
 * @version 2.75 2004-03-09
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation;

import java.io.*;
import javax.xml.parsers.*;
import org.mars_sim.msp.simulation.person.PersonConfig;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * Loads the simulation configuration XML files as DOM documents.
 * Provides simulation configuration.
 * Provides access to other simulation subset configuration classes.
 */
public class SimulationConfig {

	// Configuration files to load.
	private static final String SIMULATION_FILE = "simulation2";
	private static final String PEOPLE_FILE = "people2";
	private static final String VEHICLE_FILE = "vehicles2";
	private static final String SETTLEMENT_FILE = "settlements2";
	private static final String MEDICAL_FILE = "medical2";
	private static final String MALFUNCTION_FILE = "malfunctions2";
	private static final String CROP_FILE = "crops2";
	private static final String LANDMARKS_FILE = "landmarks2";
	private static final String BUILDINGS_FILE = "buildings2";
	
	// Simulation element names.
	private static final String TIME_CONFIGURATION = "time-configuration";
	private static final String TIME_RATIO = "time-ratio";
	private static final String EARTH_START_DATE_TIME = "earth-start-date-time";
	private static final String MARS_START_DATE_TIME = "mars-start-date-time";

	// DOM documents
	private Document simulationDoc;
	private Document peopleDoc;
	private Document vehicleDoc;
	private Document settlementDoc;
	private Document medicalDoc;
	private Document malfunctionDoc;
	private Document cropDoc;
	private Document landmarksDoc;
	private Document buildingsDoc;
	
	// Subset configuration
	private PersonConfig personConfig;

	/**
	 * Constructor
	 */
	SimulationConfig() throws Exception {
		
		// Load configurations files into DOM documents.
		loadConfigFiles();
		
		// Create subset configuration files.
		personConfig = new PersonConfig(peopleDoc);
	}
	
	/**
	 * Loads all XML configuration files for the simulation.
	 * @throws Exception if any XML could not be parsed or files could not be found.
	 */
	private void loadConfigFiles() throws Exception {
		
		// Load and parse each XML configuration file.
		simulationDoc = parseXMLFile(SIMULATION_FILE);
		peopleDoc = parseXMLFile(PEOPLE_FILE);
		vehicleDoc = parseXMLFile(VEHICLE_FILE);
		settlementDoc = parseXMLFile(SETTLEMENT_FILE);
		medicalDoc = parseXMLFile(MEDICAL_FILE);
		malfunctionDoc = parseXMLFile(MALFUNCTION_FILE);
		cropDoc = parseXMLFile(CROP_FILE);
		landmarksDoc = parseXMLFile(LANDMARKS_FILE);
		buildingsDoc = parseXMLFile(BUILDINGS_FILE);
	}
	
	/**
	 * Parses an XML file into a DOM document.
	 * @param filename the path of the file.
	 * @return DOM document
	 * @throws Exception if XML could not be parsed or file could not be found.
	 */
	private Document parseXMLFile(String filename) throws Exception {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		try {
			return builder.parse(getInputStream(filename));
		}
		catch (SAXException e) {
			throw new SAXException("XML Parsing failed on " + filename + ": " + e.getMessage());
		}
	}
	
	/**
	 * Gets a configuration file as an input stream.
	 * @param filename the filename of the configuration file.
	 * @return input stream
	 * @throws IOException if file cannot be found.
	 */
	private InputStream getInputStream(String filename) throws IOException {
		String fullPathName = "conf" + File.separator + filename + ".xml";
		InputStream stream = getClass().getClassLoader().getResourceAsStream(fullPathName);
		if (stream == null) throw new IOException(fullPathName + " failed to load");

		return stream;
	}
	
	/**
	 * Gets the simulation time to real time ratio.
	 * Example: 100.0 mean 100 simulation seconds per 1 real second.
	 * @return ratio
	 * @throws Exception if ratio is not in configuration or is not valid.
	 */
	public double getSimulationTimeRatio() throws Exception {
		
		Element root = simulationDoc.getDocumentElement();
		Element timeConfig = (Element) root.getElementsByTagName(TIME_CONFIGURATION).item(0);
		Element timeRatio = (Element) timeConfig.getElementsByTagName(TIME_RATIO).item(0);
		double ratio = Double.parseDouble(timeRatio.getAttribute("value"));
		if (ratio < 0D) throw new Exception("Simulation time ratio must be positive number.");
		else if (ratio == 0D) throw new Exception("Simulation time ratio cannot be zero.");
		
		return ratio;
	}
	
	/**
	 * Gets the Earth date/time for when the simulation starts.
	 * @return date/time as string in "MM/dd/yyyy hh:mm:ss" format.
	 * @throws Exception if value is null or empty.
	 */
	public String getEarthStartDateTime() throws Exception {
		
		Element root = simulationDoc.getDocumentElement();
		Element timeConfig = (Element) root.getElementsByTagName(TIME_CONFIGURATION).item(0);
		Element earthStartDate = (Element) timeConfig.getElementsByTagName(EARTH_START_DATE_TIME).item(0);
		String startDate = earthStartDate.getAttribute("value");
		if ((startDate == null) || startDate.trim().equals("")) 
			throw new Exception("Earth start date time must not be blank.");
			
		return startDate;
	}
	
	/**
	 * Gets the Mars dat/time for when the simulation starts.
	 * @return date/time as string in "orbit-month-sol:millisol" format.
	 * @throws Exception if value is null or empty.
	 */
	public String getMarsStartDateTime() throws Exception {
		
		Element root = simulationDoc.getDocumentElement();
		Element timeConfig = (Element) root.getElementsByTagName(TIME_CONFIGURATION).item(0);
		Element marsStartDate = (Element) timeConfig.getElementsByTagName(MARS_START_DATE_TIME).item(0);
		String startDate = marsStartDate.getAttribute("value");
		if ((startDate == null) || startDate.trim().equals("")) 
			throw new Exception("Mars start date time must not be blank.");
		
		return startDate;
	}
	
	/**
	 * Gets the person config subset.
	 * @return person config
	 */	
	public PersonConfig getPersonConfiguration() {
		return personConfig;
	}
}