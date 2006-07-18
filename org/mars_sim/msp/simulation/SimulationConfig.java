/**
 * Mars Simulation Project
 * SimulationConfig.java
 * @version 2.75 2004-04-12
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation;

import java.io.*;
import javax.xml.parsers.*;
import org.mars_sim.msp.simulation.malfunction.MalfunctionConfig;
import org.mars_sim.msp.simulation.mars.*;
import org.mars_sim.msp.simulation.person.PersonConfig;
import org.mars_sim.msp.simulation.person.medical.MedicalConfig;
import org.mars_sim.msp.simulation.structure.SettlementConfig;
import org.mars_sim.msp.simulation.structure.building.BuildingConfig;
import org.mars_sim.msp.simulation.structure.building.function.CropConfig;
import org.mars_sim.msp.simulation.vehicle.VehicleConfig;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * Loads the simulation configuration XML files as DOM documents.
 * Provides simulation configuration.
 * Provides access to other simulation subset configuration classes.
 */
public class SimulationConfig implements Serializable {

	// Configuration files to load.
	private static final String SIMULATION_FILE = "simulation";
	private static final String PEOPLE_FILE = "people";
	private static final String VEHICLE_FILE = "vehicles";
	private static final String SETTLEMENT_FILE = "settlements";
	private static final String MEDICAL_FILE = "medical";
	private static final String MALFUNCTION_FILE = "malfunctions";
	private static final String CROP_FILE = "crops";
	private static final String LANDMARK_FILE = "landmarks";
	private static final String BUILDING_FILE = "buildings";
	
	// Simulation element names.
	private static final String TIME_CONFIGURATION = "time-configuration";
	private static final String TIME_RATIO = "time-ratio";
	private static final String EARTH_START_DATE_TIME = "earth-start-date-time";
	private static final String MARS_START_DATE_TIME = "mars-start-date-time";

	// DOM documents
	private Document simulationDoc;
	
	// Subset configuration classes
	private PersonConfig personConfig;
	private MedicalConfig medicalConfig;
	private LandmarkConfig landmarkConfig;
	private MalfunctionConfig malfunctionConfig;
	private CropConfig cropConfig;
	private VehicleConfig vehicleConfig;
	private BuildingConfig buildingConfig;
	private SettlementConfig settlementConfig;

	/**
	 * Constructor
	 */
	SimulationConfig() throws Exception {
		
		// Load simulation document
		simulationDoc = parseXMLFile(SIMULATION_FILE);
		
		// Load subset configuration classes.
		personConfig = new PersonConfig(parseXMLFile(PEOPLE_FILE));
		medicalConfig = new MedicalConfig(parseXMLFile(MEDICAL_FILE));
		landmarkConfig = new LandmarkConfig(parseXMLFile(LANDMARK_FILE));
		malfunctionConfig = new MalfunctionConfig(parseXMLFile(MALFUNCTION_FILE));
		cropConfig = new CropConfig(parseXMLFile(CROP_FILE));
		vehicleConfig = new VehicleConfig(parseXMLFile(VEHICLE_FILE));
		buildingConfig = new BuildingConfig(parseXMLFile(BUILDING_FILE));
		settlementConfig = new SettlementConfig(parseXMLFile(SETTLEMENT_FILE));
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
	
	/**
	 * Gets the medical config subset.
	 * @return medical config
	 */
	public MedicalConfig getMedicalConfiguration() {
		return medicalConfig;
	}
	
	/**
	 * Gets the landmark config subset.
	 * @return landmark config
	 */
	public LandmarkConfig getLandmarkConfiguration() {
		return landmarkConfig;
	}
	
	/**
	 * Gets the malfunction config subset.
	 * @return malfunction config
	 */
	public MalfunctionConfig getMalfunctionConfiguration() {
		return malfunctionConfig;
	}
	
	/**
	 * Gets the crop config subset.
	 * @return crop config
	 */
	public CropConfig getCropConfiguration() {
		return cropConfig;
	}
	
	/**
	 * Gets the vehicle config subset.
	 * @return vehicle config
	 */
	public VehicleConfig getVehicleConfiguration() {
		return vehicleConfig;
	}
	
	/**
	 * Gets the building config subset.
	 * @return building config
	 */
	public BuildingConfig getBuildingConfiguration() {
		return buildingConfig;
	}
	
	/**
	 * Gets the settlement config subset.
	 * @return settlement config
	 */
	public SettlementConfig getSettlementConfiguration() {
		return settlementConfig;
	}
}