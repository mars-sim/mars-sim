/**
 * Mars Simulation Project
 * SimulationProperties.java
 * @version 2.75 2004-02-15
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation;

import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.Document;
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

	/**
	 * Constructor
	 */
	SimulationConfig() throws Exception {
		
		// Load configurations files into DOM documents.
		loadConfigFiles();
	}
	
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
}