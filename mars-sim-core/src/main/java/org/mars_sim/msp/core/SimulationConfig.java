/**
 * Mars Simulation Project
 * SimulationConfig.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.mars_sim.msp.core.malfunction.MalfunctionConfig;
import org.mars_sim.msp.core.manufacture.ManufactureConfig;
import org.mars_sim.msp.core.mars.LandmarkConfig;
import org.mars_sim.msp.core.mars.MineralMapConfig;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.medical.MedicalConfig;
import org.mars_sim.msp.core.resource.AmountResourceConfig;
import org.mars_sim.msp.core.resource.PartConfig;
import org.mars_sim.msp.core.resource.PartPackageConfig;
import org.mars_sim.msp.core.structure.ResupplyConfig;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.function.CropConfig;
import org.mars_sim.msp.core.structure.construction.ConstructionConfig;
import org.mars_sim.msp.core.vehicle.VehicleConfig;

/**
 * Loads the simulation configuration XML files as DOM documents.
 * Provides simulation configuration.
 * Provides access to other simulation subset configuration classes.
 */
public class SimulationConfig implements Serializable {
    
    /* ---------------------------------------------------------------------------------------------------- *
	 * SUID
	 * ---------------------------------------------------------------------------------------------------- */

	private static final long serialVersionUID = 8571791274015892904L;

	/* ---------------------------------------------------------------------------------------------------- *
	 * Constants
	 * ---------------------------------------------------------------------------------------------------- */
	
    private static final Logger logger = Logger.getLogger(SimulationConfig.class.getName());
	
	// Configuration files to load.
	private static final String SIMULATION_FILE = "simulation";
	private static final String PEOPLE_FILE = "people";
	private static final String VEHICLE_FILE = "vehicles";
	private static final String SETTLEMENT_FILE = "settlements";
	private static final String RESUPPLY_FILE = "resupplies";
	private static final String MEDICAL_FILE = "medical";
	private static final String MALFUNCTION_FILE = "malfunctions";
	private static final String CROP_FILE = "crops";
	private static final String LANDMARK_FILE = "landmarks";
	private static final String MINERAL_MAP_FILE = "minerals";
	private static final String BUILDING_FILE = "buildings";
	private static final String PART_FILE = "parts";
	private static final String PART_PACKAGE_FILE = "part_packages";
	private static final String RESOURCE_FILE = "resources";
	private static final String MANUFACTURE_FILE = "manufacturing";
    private static final String CONSTRUCTION_FILE = "construction";
    private static final String VALUE = "value";
    
	// Simulation element names.
	private static final String TIME_CONFIGURATION = "time-configuration";
	private static final String TIME_RATIO = "time-ratio";
	private static final String EARTH_START_DATE_TIME = "earth-start-date-time";
	private static final String MARS_START_DATE_TIME = "mars-start-date-time";
	
	/* ---------------------------------------------------------------------------------------------------- *
	 * Static Members
	 * ---------------------------------------------------------------------------------------------------- */

	// Singleton instance
	private static SimulationConfig instance = new SimulationConfig();
	
	/* ---------------------------------------------------------------------------------------------------- *
	 * Members
	 * ---------------------------------------------------------------------------------------------------- */
	
	// DOM documents
	private Document simulationDoc;
	
	// Subset configuration classes
	private PartConfig partConfig;
	private PartPackageConfig partPackageConfig;
	private AmountResourceConfig resourceConfig;
	private PersonConfig personConfig;
	private MedicalConfig medicalConfig;
	private LandmarkConfig landmarkConfig;
	private MineralMapConfig mineralMapConfig;
	private MalfunctionConfig malfunctionConfig;
	private CropConfig cropConfig;
	private VehicleConfig vehicleConfig;
	private BuildingConfig buildingConfig;
	private SettlementConfig settlementConfig;
	private ManufactureConfig manufactureConfig;
	private ResupplyConfig resupplyConfig;
    private ConstructionConfig constructionConfig;
    
    /* ---------------------------------------------------------------------------------------------------- *
	 * Constructors
	 * ---------------------------------------------------------------------------------------------------- */

	private SimulationConfig() {
		loadDefaultConfiguration();
	}
	
	/* ---------------------------------------------------------------------------------------------------- *
	 * Public Static Methods
	 * ---------------------------------------------------------------------------------------------------- */
	
	/**
	 * Gets a singleton instance of the simulation config.
	 * @return SimulationConfig instance
	 */
	public static SimulationConfig instance() {
		return instance;
	}
	
	/**
	 * Sets the singleton instance .
	 * @param instance the singleton instance.
	 */
	public static void setInstance(SimulationConfig instance) {
		SimulationConfig.instance = instance;
	}
	
	/**
	 * Reloads all of the configuration files.
	 * @throws Exception if error loading or parsing configuration files.
	 */
	public static void loadConfig() throws Exception {
		instance.loadDefaultConfiguration();
	}
	
	/* ---------------------------------------------------------------------------------------------------- *
	 * Getter
	 * ---------------------------------------------------------------------------------------------------- */
	
	/**
	 * Gets the simulation time to real time ratio.
	 * Example: 100.0 mean 100 simulation seconds per 1 real second.
	 * @return ratio
	 * @throws Exception if ratio is not in configuration or is not valid.
	 */
	public double getSimulationTimeRatio() throws Exception {
		
		Element root = simulationDoc.getRootElement();
		Element timeConfig = root.getChild(TIME_CONFIGURATION);
		Element timeRatio = timeConfig.getChild(TIME_RATIO);
		double ratio = Double.parseDouble(timeRatio.getAttributeValue(VALUE));
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
		
		Element root = simulationDoc.getRootElement();
		Element timeConfig = root.getChild(TIME_CONFIGURATION);
		Element earthStartDate = timeConfig.getChild(EARTH_START_DATE_TIME);
		String startDate = earthStartDate.getAttributeValue(VALUE);
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
		
		Element root = simulationDoc.getRootElement();
		Element timeConfig = root.getChild(TIME_CONFIGURATION);
		Element marsStartDate = timeConfig.getChild(MARS_START_DATE_TIME);
		String startDate = marsStartDate.getAttributeValue(VALUE);
		if ((startDate == null) || startDate.trim().equals("")) 
			throw new Exception("Mars start date time must not be blank.");
		
		return startDate;
	}
	
	/**
	 * Gets the part config subset.
	 * @return part config
	 */
	public PartConfig getPartConfiguration() {
		return partConfig;
	}
	
	/**
	 * Gets the part package configuration.
	 * @return part package config
	 */
	public PartPackageConfig getPartPackageConfiguration() {
		return partPackageConfig;
	}
	
	/**
	 * Gets the resource config subset.
	 * @return resource config
	 */
	public AmountResourceConfig getResourceConfiguration() {
		return resourceConfig;
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
	 * Gets the mineral map config subset.
	 * @return mineral map config
	 */
	public MineralMapConfig getMineralMapConfiguration() {
		return mineralMapConfig;
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
	 * Gets the resupply configuration.
	 * @return resupply config
	 */
	public ResupplyConfig getResupplyConfiguration() {
		return resupplyConfig;
	}
	
	/**
	 * Gets the settlement config subset.
	 * @return settlement config
	 */
	public SettlementConfig getSettlementConfiguration() {
		return settlementConfig;
	}
	
	/**
	 * Gets the manufacture config subset.
	 * @return manufacture config
	 */
	public ManufactureConfig getManufactureConfiguration() {
		return manufactureConfig;
	}
    
    /**
     * Gets the construction config subset.
     * @return construction config
     */
    public ConstructionConfig getConstructionConfiguration() {
        return constructionConfig;
    }
	
	/* ---------------------------------------------------------------------------------------------------- *
	 * Private Methods
	 * ---------------------------------------------------------------------------------------------------- */
    
    private void loadDefaultConfiguration() {
    	try {
			// Load simulation document
			simulationDoc = parseXMLFileAsJDOMDocument(SIMULATION_FILE, true);
		
			// Load subset configuration classes.
			resourceConfig = new AmountResourceConfig(parseXMLFileAsJDOMDocument(RESOURCE_FILE, true));
			partConfig = new PartConfig(parseXMLFileAsJDOMDocument(PART_FILE, true));
			partPackageConfig = new PartPackageConfig(parseXMLFileAsJDOMDocument(PART_PACKAGE_FILE, true));
			personConfig = new PersonConfig(parseXMLFileAsJDOMDocument(PEOPLE_FILE, true));
			medicalConfig = new MedicalConfig(parseXMLFileAsJDOMDocument(MEDICAL_FILE, true));
			landmarkConfig = new LandmarkConfig(parseXMLFileAsJDOMDocument(LANDMARK_FILE, true));
			mineralMapConfig = new MineralMapConfig(parseXMLFileAsJDOMDocument(MINERAL_MAP_FILE, true));
			malfunctionConfig = new MalfunctionConfig(parseXMLFileAsJDOMDocument(MALFUNCTION_FILE, true));
			cropConfig = new CropConfig(parseXMLFileAsJDOMDocument(CROP_FILE, true));
			vehicleConfig = new VehicleConfig(parseXMLFileAsJDOMDocument(VEHICLE_FILE, true));
			buildingConfig = new BuildingConfig(parseXMLFileAsJDOMDocument(BUILDING_FILE, true));
			resupplyConfig = new ResupplyConfig(parseXMLFileAsJDOMDocument(RESUPPLY_FILE, true), partPackageConfig);
			settlementConfig = new SettlementConfig(parseXMLFileAsJDOMDocument(SETTLEMENT_FILE, true), partPackageConfig);
			manufactureConfig = new ManufactureConfig(parseXMLFileAsJDOMDocument(MANUFACTURE_FILE, true));
            constructionConfig = new ConstructionConfig(parseXMLFileAsJDOMDocument(CONSTRUCTION_FILE, true));
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error creating simulation config: " + e.getMessage());
			e.printStackTrace();
		}
    }
	
	/**
     * Parses an XML file into a DOM document.
     * @param filename the path of the file.
     * @param useDTD true if the XML DTD should be used.
     * @return DOM document
     * @throws Exception if XML could not be parsed or file could not be found.
     */
    private Document parseXMLFileAsJDOMDocument(String filename, boolean useDTD) throws Exception {
        InputStream stream = getInputStream(filename);
        /* bug 2909888: read the inputstream with a specific encoding instead of the system default. */
        InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
        SAXBuilder saxBuilder = new SAXBuilder(useDTD);
        /* [landrus, 26.11.09]: Use an entity resolver to load dtds from the classpath */
        saxBuilder.setEntityResolver(new ClasspathEntityResolver());
        Document result = saxBuilder.build(reader);
        stream.close();
        return result;
    }
    
	/**
	 * Gets a configuration file as an input stream.
	 * @param filename the filename of the configuration file.
	 * @return input stream
	 * @throws IOException if file cannot be found.
	 */
	private InputStream getInputStream(String filename) throws IOException {
		/* [landrus, 28.11.09]: dont use filesystem separators in classloader loading envs. */
		String fullPathName = "/conf/" + filename + ".xml";
		InputStream stream = getClass().getResourceAsStream(fullPathName);
		if (stream == null) throw new IOException(fullPathName + " failed to load");

		return stream;
	}
	
}