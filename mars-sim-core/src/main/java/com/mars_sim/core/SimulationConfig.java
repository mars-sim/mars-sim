/*
 * Mars Simulation Project
 * SimulationConfig.java
 * @date 2021-09-25
 * @author Scott Davis
 */
package com.mars_sim.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import javax.xml.XMLConstants;

import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.mars_sim.core.authority.AuthorityFactory;
import com.mars_sim.core.environment.LandmarkConfig;
import com.mars_sim.core.environment.MineralMapConfig;
import com.mars_sim.core.food.FoodProductionConfig;
import com.mars_sim.core.interplanetary.transport.resupply.ResupplyConfig;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.MalfunctionConfig;
import com.mars_sim.core.manufacture.ManufactureConfig;
import com.mars_sim.core.person.PersonConfig;
import com.mars_sim.core.person.health.MedicalConfig;
import com.mars_sim.core.quotation.QuotationConfig;
import com.mars_sim.core.resource.AmountResourceConfig;
import com.mars_sim.core.resource.PartConfig;
import com.mars_sim.core.resource.PartPackageConfig;
import com.mars_sim.core.robot.RobotConfig;
import com.mars_sim.core.science.ScienceConfig;
import com.mars_sim.core.structure.BuildingPackageConfig;
import com.mars_sim.core.structure.SettlementConfig;
import com.mars_sim.core.structure.building.BuildingConfig;
import com.mars_sim.core.structure.building.ResourceProcessConfig;
import com.mars_sim.core.structure.building.function.cooking.MealConfig;
import com.mars_sim.core.structure.building.function.farming.CropConfig;
import com.mars_sim.core.structure.construction.ConstructionConfig;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.tool.Hash;
import com.mars_sim.core.vehicle.VehicleConfig;
import com.mars_sim.mapdata.common.FileLocator;

/**
 * Loads the simulation configuration XML files as DOM documents. Provides
 * simulation configuration. Provides access to other simulation subset
 * configuration classes.
 */
public class SimulationConfig implements Serializable {

	private static final long serialVersionUID = -5348007442971644450L;

	private static final SimLogger logger = SimLogger.getLogger(SimulationConfig.class.getName());

	private static final int BACKUP_FOLDER_COUNT = 5;

	/** The version.txt denotes the xml build version. */
	private static final String VERSION_FILE = "version.txt";
	/** The exception.txt denotes any user modified xml to be included to bypass the checksum. */
	private static final String EXCEPTION_FILE = "exception.txt";

	private static final String XML_FOLDER = "xml";
	private static final String XML_EXTENSION = ".xml";
	private static final String SIMULATION_FILE = "simulation";
	private static final String GOVERNANCE_FILE = "governance";
	private static final String PEOPLE_FILE = "people";
	private static final String VEHICLE_FILE = "vehicles";
	private static final String SETTLEMENT_FILE = "settlements";
	private static final String RESUPPLY_FILE = "resupplies";
	private static final String MEDICAL_FILE = "medical";
	private static final String MALFUNCTION_FILE = "malfunctions";
	private static final String CROP_FILE = "crops";
	private static final String LANDMARK_FILE = "landmarks";
	private static final String MINERAL_MAP_FILE = "minerals";
	private static final String RESPROCESS_FILE = "resource_process";
	private static final String BUILDING_FILE = "buildings";
	private static final String PART_FILE = "parts";
	private static final String PART_PACKAGE_FILE = "part_packages";
	private static final String BUILDING_PACKAGE_FILE = "building_packages";
	private static final String RESOURCE_FILE = "resources";
	private static final String MANUFACTURE_FILE = "manufacturing";
	private static final String CONSTRUCTION_FILE = "construction";
	private static final String FOODPRODUCTION_FILE = "food_production";
	private static final String MEAL_FILE = "meals";
	private static final String ROBOT_FILE = "robots";
	private static final String QUOTATION_FILE = "quotations";
	private static final String VALUE = "value";

	// Simulation element names.
	private static final String TIME_CONFIGURATION = "time-configuration";

	private static final String ACCURACY_BIAS = "accuracy-bias";
	private static final String MIN_SIMULATED_PULSE = "min-simulated-pulse";
	private static final String MAX_SIMULATED_PULSE = "max-simulated-pulse";

	private static final String EARTH_START_DATE_TIME = "earth-start-date-time";
	private static final String MARS_START_DATE_TIME = "mars-start-date-time";

	private static final String AUTOSAVE_INTERVAL = "autosave-interval";
	private static final String AUTOSAVE_NUMBER = "autosave-number";
	private static final String AVERAGE_TRANSIT_TIME = "average-transit-time";
	private static final String DEFAULT_TIME_PULSE = "default-time-pulse";
	private static final String BASE_TIME_RATIO = "base-time-ratio";
	private static final String DEFAULT_UNUSEDCORES = "unused-cores";

	private static final String MISSION_CONFIGURATION = "mission-configuration";
	private static final String EVA_LIGHT = "min-eva-light";
	private static final String CONTENT_URL = "content-url";

	private transient String marsStartDate = null;
	private transient String earthStartDate = null;

	private double accuracyBias = 0;
	private double maxSimulatedPulse = 0;
	private double minSimulatedPulse = 0;
	
	private int defaultTimePulse = 0; 
	private int baseTimeRatio = 0;
	private int autosaveInterval = 0;
	private int numberOfAutoSaves = 0;
	private int averageTransitTime = 0;
	private int unusedCores = 0;	
	private transient boolean loaded = false;
	
	/*
	 * -----------------------------------------------------------------------------
	 * Members
	 * -----------------------------------------------------------------------------
	 */

	// Subset configuration classes
	private transient PartConfig partConfig;
	private transient PartPackageConfig partPackageConfig;
	private transient AmountResourceConfig resourceConfig;
	private transient PersonConfig personConfig;
	private transient MedicalConfig medicalConfig;
	private transient LandmarkConfig landmarkConfig;
	private transient MineralMapConfig mineralMapConfig;
	private transient MalfunctionConfig malfunctionConfig;
	private transient CropConfig cropConfig;
	private transient VehicleConfig vehicleConfig;
	private transient BuildingConfig buildingConfig;
	private transient SettlementConfig settlementConfig;
	private transient ManufactureConfig manufactureConfig;
	private transient ResupplyConfig resupplyConfig;
	private transient ConstructionConfig constructionConfig;

	private transient FoodProductionConfig foodProductionConfig;
	private transient MealConfig mealConfig;
	private transient RobotConfig robotConfig;
	private transient QuotationConfig quotationConfig;
	private transient ScienceConfig scienceConfig;

	private transient List<String> excludedList;

	private transient AuthorityFactory raFactory;

	private double minEVALight;

	private File backupLocation;

	/*
	 * -----------------------------------------------------------------------------
	 * Constructors
	 * -----------------------------------------------------------------------------
	 */

	/** hidden constructor. */
	private SimulationConfig() {
	}

	/**
	 * Prevents the singleton pattern from being destroyed at the time of
	 * serialization
	 *
	 * @return SimulationConfig instance
	 */
	protected Object readResolve() throws ObjectStreamException {
		return instance();
	}

	/**
	 * Initializes an inner static helper class for Bill Pugh Singleton Pattern
	 * Note: as soon as the instance() method is called the first time, the class is
	 * loaded into memory and an instance gets created. Advantage: it supports
	 * multiple threads calling instance() simultaneously with no synchronized
	 * keyword needed (which slows down the VM)
	 */
	private static class SingletonHelper {
		private static final SimulationConfig INSTANCE = new SimulationConfig();
	}

	/**
	 * Gets a Bill Pugh Singleton instance of the SimulationConfig.
	 *
	 * @return SimulationConfig instance
	 */
	public static SimulationConfig instance() {
		// NOTE: SimulationConfig.instance() is accessible on any threads or by any threads
		return SingletonHelper.INSTANCE;
	}

	/**
	 * Loads all of the configuration files.
	 *
	 * @throws Exception if error loading or parsing configuration files.
	 */
	public void loadConfig() {
		if (loaded) {
			return;
		}
		loaded = true;
		
		try {
			// Load simulation document
			Document simulationDoc = parseXMLFileAsJDOMDocument(SIMULATION_FILE, true);

			// Load key attributes
			Element root = simulationDoc.getRootElement();
			String contentURL = root.getAttributeValue(CONTENT_URL);
			if (contentURL != null) {
				FileLocator.setContentURL(contentURL);
			}
			// Load time configurations
			Element timeConfig = root.getChild(TIME_CONFIGURATION);
			earthStartDate = loadValue(timeConfig, EARTH_START_DATE_TIME);	
			marsStartDate = loadValue(timeConfig, MARS_START_DATE_TIME);
			
			accuracyBias =  loadDoubleValue(timeConfig, ACCURACY_BIAS, 0D, 1D);
			minSimulatedPulse = loadDoubleValue(timeConfig, MIN_SIMULATED_PULSE, 0.01, 1.4795874);
			maxSimulatedPulse = loadDoubleValue(timeConfig, MAX_SIMULATED_PULSE, 1.4795874, 40.55184573753467);
		
			defaultTimePulse = loadIntValue(timeConfig, DEFAULT_TIME_PULSE, 1, 2048);
			baseTimeRatio = loadIntValue(timeConfig, BASE_TIME_RATIO, 1, (int)MasterClock.MAX_TIME_RATIO);
			unusedCores = loadIntValue(timeConfig, DEFAULT_UNUSEDCORES, 1, 360);
			averageTransitTime = loadIntValue(timeConfig, AVERAGE_TRANSIT_TIME, 0, 430);
			autosaveInterval = loadIntValue(timeConfig, AUTOSAVE_INTERVAL, 1, 360);
			numberOfAutoSaves = loadIntValue(timeConfig, AUTOSAVE_NUMBER, 1, 100);

			// LOad MIssion Types
			Element missionConfig = root.getChild(MISSION_CONFIGURATION);
			minEVALight = loadDoubleValue(missionConfig, EVA_LIGHT, 0D, 1000D);
			checkXMLFileVersion();

			loadDefaultConfiguration();
		} catch (RuntimeException | JDOMException | IOException rte) {
          	logger.log  (Level.SEVERE, "Cannot load default config : " + rte.getMessage(), rte);
			throw new IllegalStateException("Cannot load the configurations", rte);
		}
	}

	/**
	 * Reloads the configurations from the XML files including
	 * re-checking the XML versions.
	 * Should need to be used if the files have changed as Config
	 * objects should be immutable.
	 */
	public void reloadConfig() {
		loadConfig();
	}

	/**
	 * Checks if the xml files are of the same version of the core engine.
	 */
	private void checkXMLFileVersion() {
		boolean sameBuild = false;

        File xmlLoc = new File(SimulationFiles.getXMLDir());
		File versionLoc = new File(SimulationFiles.getXMLDir() + File.separator + VERSION_FILE);
		File exceptionLoc = new File(SimulationFiles.getXMLDir() + File.separator + EXCEPTION_FILE);

        FileSystem fileSys = FileSystems.getDefault();
		Path xmlPath = fileSys.getPath(xmlLoc.getPath());
        Path versionPath = fileSys.getPath(versionLoc.getPath());
        Path exceptionPath = fileSys.getPath(exceptionLoc.getPath());

		// Query if the xml folder exists in user home directory
		// Query if the xml version matches
		// If not, copy all xml over

        boolean xmlDirExist = xmlPath.toFile().exists();

		// Note: if "xml" exits as a file, delete it
		if (xmlDirExist && xmlLoc.isFile()) {
			logger.config("'" + xmlLoc +  "'"
					+ " is a folder and NOT supposed to exist as a file. Deleting it.");
			try {
				FileUtils.forceDelete(xmlLoc);
			} catch (IOException e) {
	          	logger.log(Level.SEVERE, "Cannot access xml folder: " + e.getMessage());
			}
		}

		// Check again xmlDirExist
		xmlDirExist = xmlLoc.exists();

		boolean versionFileExist = versionLoc.exists();

		boolean xmlDirDeleted = false;

		String buildText = "";

		// if the "xml" directory exists, back up everything inside and clean the directory
		if (xmlDirExist && xmlLoc.isDirectory()) {
			logger.config("The xml folder already existed.");

			if (versionFileExist) {
				try (BufferedReader buffer = new BufferedReader(new FileReader(versionLoc))) {
				    if ((buildText = buffer.readLine()) != null) {
				    	// If the version.txt's build version tag is the same as the core engine's
				    	sameBuild = buildText.equals(Simulation.BUILD);
				    }
				} catch (FileNotFoundException e) {
		          	logger.log(Level.SEVERE, "Cannot find version.txt : " + e.getMessage());
				} catch (IOException e) {
		          	logger.log(Level.SEVERE, "Cannot access version.txt : " + e.getMessage());
				}
			}
		}

		if (!xmlDirExist)
			logger.config("The xml folder does not exist in user home.");
		else if (!versionFileExist)
			logger.config("The version.txt does not exist.");
		else if (sameBuild)
			logger.config("The version.txt has the same build " + buildText
					+ " as the core engine's.");
		else {
			logger.config("The version.txt is invalid.");
		}

		if (xmlDirExist
			&& (!versionFileExist || buildText == null || buildText.equals("") || !sameBuild)) {
				try {
					FileUtils.copyDirectory(xmlLoc, getBackupDir(), true);
				} catch (IOException e) {
		          	logger.log(Level.SEVERE, "Issues with build folder or backup folder: " + e.getMessage());
				}
		}

		xmlDirExist = xmlLoc.exists();

		// if the "xml" folder does NOT exist
		if (!xmlLoc.exists() || xmlDirDeleted) {
			// Create the xml folder
			versionLoc.getParentFile().mkdirs();
			logger.config("A new xml folder was just created.");

			List<String> lines = Arrays.asList(Simulation.BUILD);
			try {
				// Create the version.txt file
				Files.write(versionPath, lines, StandardCharsets.UTF_8);
				logger.config("A new version.txt file was just created.");
			} catch (IOException e) {
	          	logger.log(Level.SEVERE, "Cannot write lines when creating version.txt" + e.getMessage());
			}
		}

		if (!sameBuild) {
			List<String> lines = Arrays.asList(Simulation.BUILD);
			try {
				// Create the version.txt file
				Files.write(versionPath, lines, StandardCharsets.UTF_8);
				logger.config("A new version.txt file was just created.");
			} catch (IOException e) {
	          	logger.log(Level.SEVERE, "Cannot write lines when creating version.txt" + e.getMessage());
			}
		}
		if (!exceptionLoc.exists()) {
			List<String> lines = new CopyOnWriteArrayList<>();
			try {
				// Create the exception.txt file
				Files.write(exceptionPath, lines, StandardCharsets.UTF_8);
				logger.config("A new exception.txt file was just created.");
			} catch (IOException e) {
	          	logger.log(Level.SEVERE, "Cannot write lines when creating exception.txt" + e.getMessage());
			}
		}
	}

	/**
	 * Finds a string value.
	 * 
	 * @param parent Parent element
	 * @param child Value element
	 * @return String value found
	 */
	private String loadValue(Element parentConfig, String child) {
		Element childItem = parentConfig.getChild(child);
		String str = childItem.getAttributeValue(VALUE);


		if ((str == null) || str.trim().length() == 0)
			throw new IllegalStateException(parentConfig.getName() + "->" + child + " must be greater than zero and cannot be blank.");
		return str.trim();
	}

	/**
	 * Load an integer value that is held as a 'value' attribute.
	 * @param parent Parent XML node
	 * @param child XML Node containing the 'value'
	 * @param minValue Minimum allowable value
	 * @param maxValue Maximum allowable value
	 */
	private int loadIntValue(Element parent, String child,
									   int minValue, int maxValue) {
		String str = loadValue(parent, child);
		int i = 0;
		try {
			i = Integer.parseInt(str);

		} catch (NumberFormatException nfe) {
			logger.severe("NumberFormatException found in " + parent.getName() + "->" + child
								+ " : " + nfe.getMessage());
			throw nfe;
		}
		if (i < minValue || i > maxValue)
			throw new IllegalStateException(child + " must be between " + minValue + " -> " + maxValue);
		return i;
	}

	/**
	 * Load an double value that is held as a 'value' attribute.
	 * 
	 * @param parent Parent XML node
	 * @param child XML Node containing the 'value'
	 * @param minValue Minimum allowable value
	 * @param maxValue Maximum allowable value
	 */
	private double loadDoubleValue(Element parent, String child, double minValue, double maxValue) {
		String str = loadValue(parent, child);
		double d = 0;
		try {
			d = Double.parseDouble(str);

		} catch (NumberFormatException nfe) {
			logger.severe("NumberFormatException found in " + parent.getName() + "->" + child
								+ " : " + nfe.getMessage());
			throw nfe;
		}
		if (d < minValue || d > maxValue)
			throw new IllegalStateException(child + " must be between " + minValue + " -> " + maxValue);
		return d;
	}

	/*
	 * -----------------------------------------------------------------------------
	 * Getter
	 * -----------------------------------------------------------------------------
	 */

	/**
	 * Gets the ratio of simulation time to real time in simulation.xml
	 *
	 * @return ratio
	 * @throws Exception if ratio is not in configuration or is not valid.
	 */
	public int getTimeRatio() {
		return baseTimeRatio;
	}

	/**
	 * Gets the minimum simulation pulse size in terms of MilliSol in simulation.xml
	 *
	 * @return minimum
	 * @throws Exception if ratio is not in configuration or is not valid.
	 */
	public double getMinSimulatedPulse() {
		return minSimulatedPulse;
	}

	/**
	 * Gets the maximum simulation pulse size in terms of MilliSol in simulation.xml
	 *
	 * @return minimum
	 * @throws Exception if ratio is not in configuration or is not valid.
	 */
	public double getMaxSimulatedPulse() {
		return maxSimulatedPulse;
	}

	/**
	 * Load the accuracy bias. Must be between 0.0 -> 1.0
	 * 
	 * @return
	 */
	public double getAccuracyBias() {
		return accuracyBias;
	}

	/**
	 * Load the default elapsed time for each pulse. Must be positive.
	 * 
	 * @return Millisec
	 */
	public int getDefaultPulsePeriod() {
		return defaultTimePulse;
	}

	/**
	 * The difference between number of cores in the machine and the simulation threads created, 
	 * i.e. the unused cores. Must be positive.
	 * 
	 * @return # of cores
	 */
	public int getUnusedCores() {
		return unusedCores;
	}

	/**
	 * The the minimum light needed for EVAs.
	 * 
	 * @return Light value.
	 */
	public double getMinEVALight() {
		return minEVALight;
	}
	
	/**
	 * Gets the Earth date/time when the simulation starts.
	 *
	 * @return 
	 */
	public LocalDateTime getEarthStartDate() {
		return LocalDateTime.parse(earthStartDate,
						DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm:ss.SSS"));
	}
	
	/**
	 * Gets the Mars date/time when the simulation starts.
	 *
	 * @return date/time as string in "orbit-month-sol:millisol" format.
	 * @throws Exception if value is null or empty.
	 */
	public String getMarsStartDateTime() {
		return marsStartDate;
	}

	/**
	 * Manually sets the autosave interval.
	 *
	 * @param value
	 */
	public void setAutosaveInterval(int value) {
		autosaveInterval = value;
	}

	/**
	 * Gets the autosave interval when the simulation starts.
	 *
	 * @return number of minutes.
	 * @throws Exception if value is null or empty.
	 */
	public int getAutosaveInterval() {
		return autosaveInterval;
	}

	/**
	 * How many auto saves should be retained.
	 * @param Integer value.
	 */
	public int getNumberAutoSaves() {
		return numberOfAutoSaves;
	}

	/**
	 * Gets the AverageTransitTime when the simulation starts.
	 *
	 * @return number of sols.
	 * @throws Exception if value is null or empty.
	 */
	public int getAverageTransitTime() {
		return averageTransitTime;
	}

	/**
	 * Gets the part config subset.
	 *
	 * @return part config
	 */
	public PartConfig getPartConfiguration() {
		return partConfig;
	}

	/**
	 * Gets the part package configuration.
	 *
	 * @return part package config
	 */
	public PartPackageConfig getPartPackageConfiguration() {
		return partPackageConfig;
	}

	/**
	 * Gets the resource config subset.
	 *
	 * @return resource config
	 */
	public AmountResourceConfig getResourceConfiguration() {
		return resourceConfig;
	}

	/**
	 * Gets the person config subset.
	 *
	 * @return person config
	 */
	public PersonConfig getPersonConfig() {
		return personConfig;
	}

	/**
	 * Gets the robot config subset.
	 *
	 * @return robot config
	 */
	public RobotConfig getRobotConfiguration() {
		return robotConfig;
	}

	/**
	 * Gets the medical config subset.
	 *
	 * @return medical config
	 */
	public MedicalConfig getMedicalConfiguration() {
		return medicalConfig;
	}

	/**
	 * Gets the landmark config subset.
	 *
	 * @return landmark config
	 */
	public LandmarkConfig getLandmarkConfiguration() {
		return landmarkConfig;
	}

	/**
	 * Gets the mineral map config subset.
	 *
	 * @return mineral map config
	 */
	public MineralMapConfig getMineralMapConfiguration() {
		return mineralMapConfig;
	}

	/**
	 * Gets the malfunction config subset.
	 *
	 * @return malfunction config
	 */
	public MalfunctionConfig getMalfunctionConfiguration() {
		return malfunctionConfig;
	}

	/**
	 * Gets the crop config subset.
	 *
	 * @return crop config
	 */
	public CropConfig getCropConfiguration() {
		return cropConfig;
	}

	/**
	 * Gets the vehicle config subset.
	 *
	 * @return vehicle config
	 */
	public VehicleConfig getVehicleConfiguration() {
		return vehicleConfig;
	}

	/**
	 * Gets the building config subset.
	 *
	 * @return building config
	 */
	public BuildingConfig getBuildingConfiguration() {
		return buildingConfig;
	}

	/**
	 * Gets the resupply configuration.
	 *
	 * @return resupply config
	 */
	public ResupplyConfig getResupplyConfiguration() {
		return resupplyConfig;
	}

	/**
	 * Gets the settlement config subset.
	 *
	 * @return settlement config
	 */
	public SettlementConfig getSettlementConfiguration() {
		return settlementConfig;
	}

	/**
	 * Gets the manufacture config subset.
	 *
	 * @return manufacture config
	 */
	public ManufactureConfig getManufactureConfiguration() {
		return manufactureConfig;
	}

	/**
	 * Gets the foodProduction config subset.
	 *
	 * @return foodProduction config
	 */
	public FoodProductionConfig getFoodProductionConfiguration() {
		return foodProductionConfig;
	}

	/**
	 * Gets the meal config subset.
	 *
	 * @return meal config
	 */
	public MealConfig getMealConfiguration() {
		return mealConfig;
	}

	/**
	 * Gets the construction config subset.
	 *
	 * @return construction config
	 */
	public ConstructionConfig getConstructionConfiguration() {
		return constructionConfig;
	}

	/**
	 * Gets the quotation config subset.
	 *
	 * @return quotation config
	 */
	public QuotationConfig getQuotationConfiguration() {
		return quotationConfig;
	}

	/**
	 * Gets the science config subset.
	 *
	 * @return science config
	 */
	public ScienceConfig getScienceConfig() {
		return scienceConfig;
	}


	/**
	 * Gets the manager to the ReportingAuthority.
	 * 
	 * @return
	 */
	public AuthorityFactory getReportingAuthorityFactory() {
		return raFactory;
	}

	/**
	 * Finds the requested XML file in the bundled JAR and extracts to the xml sub-directory.
	 */
	public File getBundledXML(String filename) {
		if (filename.indexOf('.') == -1) {
			// Ne extension; assume XML
			filename = filename + XML_EXTENSION;
		}
		
		// Check existing file and get checksum
		File existingFile = new File(SimulationFiles.getXMLDir(), filename);
		String existingChecksum = null;
		if (existingFile.exists()) {
			try {
				existingChecksum = Hash.MD5.getChecksumString(existingFile);
			} catch (IOException e) {
				logger.severe("Cannot calculate checksum for " + existingFile, e);
			}
		}

		// Check bundled file
		String resourceName = "/" + XML_FOLDER + "/" + filename;
		try (InputStream stream = SimulationConfig.class.getResourceAsStream(resourceName)) {
			if (stream == null) {
				logger.severe("Cannot find the bundled XML " + resourceName);
				return null;
			}
			String resourceChecksum = null;

			// Checksum the bundled resoruce from a new stream
			try (InputStream checkStream = SimulationConfig.class.getResourceAsStream(resourceName)) {
				resourceChecksum = Hash.MD5.getChecksumString(checkStream);
			}

			// Compare checksums
			if (existingChecksum == null || !existingChecksum.equals(resourceChecksum)) {
				// Take a new copy
				if (!excludeXMLFile(filename)) {
					logger.config(existingFile.getName() + ": " + "Old MD5: "+ existingChecksum + "  New MD5: "+ resourceChecksum);

					// Take backup
					if (existingFile.exists()) {
						File dir = getBackupDir();

						// Backup this old (checksum failed) xml file
						FileUtils.copyFileToDirectory(existingFile, dir, true);
						FileUtils.deleteQuietly(existingFile);
					}

					// Copy the xml files from within the jar to user home's xml directory
					FileUtils.copyToFile(stream, existingFile);
				}
				else {
					// The xml file is found
					logger.config(filename + " was found being referenced inside exception.txt, thus bypassing its checksum.");
				}
	        }
		}
		catch (IOException e) {
			logger.log(Level.SEVERE, "Problem getting bundled XML " + e.getMessage(), e);
		}
        return existingFile;
	}

	/**
	 * Get the most appropriate backup directory
	 */
	private File getBackupDir() {
		if (backupLocation == null) {
			String s0 = SimulationFiles.getBackupDir();

			// Get timestamp in UTC
			String timestamp = LocalDateTime.now().toString().replace(":", "").replace("-", "");
			int lastIndxDot = timestamp.lastIndexOf('.');
			timestamp = timestamp.substring(0, lastIndxDot);
			String s1 = s0 + File.separator + timestamp;

			logger.config("New config backup directory " + s1);

			backupLocation = new File(s1.trim());
			backupLocation.mkdirs();

			// Delete old folders
			SimulationFiles.purgeOldFiles(SimulationFiles.getBackupDir(), BACKUP_FOLDER_COUNT, null);
		}
		return backupLocation;
	}

	/**
	 * Checks if this bundled XML file is excluded from the automatic extraction.
	 * 
	 * @param filename
	 * @return
	 */
	private boolean excludeXMLFile(String filename) {
		if (excludedList == null) {
			excludedList = new ArrayList<>();

			File exceptionFile = new File(SimulationFiles.getXMLDir() + File.separator
					+ EXCEPTION_FILE);
			if (exceptionFile.exists()) {
				// Read the exception.txt file to see if it mentions this particular xml file
				try (BufferedReader buffer = new BufferedReader(new FileReader(exceptionFile))) {
					String nextLine = buffer.readLine();
					// Support commenting out lines
					if (nextLine != null && !nextLine.startsWith("#")) {
						excludedList.add(nextLine);
					}
				} catch (IOException e) {
					logger.warning("Problem loading the exception file " + e.getMessage());
				}
			}
		}

		return excludedList.contains(filename);
	}


	/**
	 * Parses an XML file into a DOM document.
	 *
	 * @param filename the path of the file.
	 * @param useDTD   true if the XML DTD should be used.
	 * @return DOM document
	 * @throws IOException
	 * @throws JDOMException
	 */
	public Document parseXMLFileAsJDOMDocument(String filename, boolean useDTD)
			throws JDOMException, IOException {
		File f = getBundledXML(filename);
		if (f != null) {
			SAXBuilder builder = new SAXBuilder();
			builder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			builder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			return builder.build(f);
		}
		else {
			logger.warning("Can not find default XML " + filename);
			throw new IllegalStateException("Can not find default XML " + filename);
		}
	}


	/**
	 * load the default config files
	 * @throws IOException
	 * @throws JDOMException
	 */
	private void loadDefaultConfiguration() throws JDOMException, IOException {
  BuildingPackageConfig buildingPackageConfig;

		// Load subset configuration classes.
		raFactory = new AuthorityFactory(parseXMLFileAsJDOMDocument(GOVERNANCE_FILE, true));
		resourceConfig = new AmountResourceConfig(parseXMLFileAsJDOMDocument(RESOURCE_FILE, true));
		partConfig = new PartConfig(parseXMLFileAsJDOMDocument(PART_FILE, true));
		partPackageConfig = new PartPackageConfig(parseXMLFileAsJDOMDocument(PART_PACKAGE_FILE, true));
		buildingPackageConfig = new BuildingPackageConfig(parseXMLFileAsJDOMDocument(BUILDING_PACKAGE_FILE, true));
		personConfig = new PersonConfig(parseXMLFileAsJDOMDocument(PEOPLE_FILE, true));
		medicalConfig = new MedicalConfig(parseXMLFileAsJDOMDocument(MEDICAL_FILE, true));
		landmarkConfig = new LandmarkConfig(parseXMLFileAsJDOMDocument(LANDMARK_FILE, true));
		mineralMapConfig = new MineralMapConfig(parseXMLFileAsJDOMDocument(MINERAL_MAP_FILE, true));
		manufactureConfig = new ManufactureConfig(parseXMLFileAsJDOMDocument(MANUFACTURE_FILE, true));
		malfunctionConfig = new MalfunctionConfig(parseXMLFileAsJDOMDocument(MALFUNCTION_FILE, true));
		cropConfig = new CropConfig(parseXMLFileAsJDOMDocument(CROP_FILE, true), personConfig);
		vehicleConfig = new VehicleConfig(parseXMLFileAsJDOMDocument(VEHICLE_FILE, true), manufactureConfig);
		ResourceProcessConfig resourceProcessConfig = new ResourceProcessConfig(parseXMLFileAsJDOMDocument(RESPROCESS_FILE, true));
		buildingConfig = new BuildingConfig(parseXMLFileAsJDOMDocument(BUILDING_FILE, true), resourceProcessConfig);
		resupplyConfig = new ResupplyConfig(parseXMLFileAsJDOMDocument(RESUPPLY_FILE, true), partPackageConfig);
		settlementConfig = new SettlementConfig(parseXMLFileAsJDOMDocument(
									SETTLEMENT_FILE, true),
									partPackageConfig, buildingPackageConfig, resupplyConfig);

		constructionConfig = new ConstructionConfig(parseXMLFileAsJDOMDocument(CONSTRUCTION_FILE, true));
		foodProductionConfig = new FoodProductionConfig(parseXMLFileAsJDOMDocument(FOODPRODUCTION_FILE, true));
		mealConfig = new MealConfig(parseXMLFileAsJDOMDocument(MEAL_FILE, true));
		robotConfig = new RobotConfig(parseXMLFileAsJDOMDocument(ROBOT_FILE, true));
		quotationConfig = new QuotationConfig(parseXMLFileAsJDOMDocument(QUOTATION_FILE, true));
		scienceConfig = new ScienceConfig();

		logger.config("Done loading all xml config files.");
	}
}
