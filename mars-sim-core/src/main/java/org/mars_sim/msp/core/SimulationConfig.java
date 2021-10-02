/*
 * Mars Simulation Project
 * SimulationConfig.java
 * @date 2021-09-25
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

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
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;

import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.mars_sim.msp.core.environment.LandmarkConfig;
import org.mars_sim.msp.core.environment.MineralMapConfig;
import org.mars_sim.msp.core.foodProduction.FoodProductionConfig;
import org.mars_sim.msp.core.interplanetary.transport.resupply.ResupplyConfig;
import org.mars_sim.msp.core.malfunction.MalfunctionConfig;
import org.mars_sim.msp.core.manufacture.ManufactureConfig;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.health.MedicalConfig;
import org.mars_sim.msp.core.quotation.QuotationConfig;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityFactory;
import org.mars_sim.msp.core.resource.AmountResourceConfig;
import org.mars_sim.msp.core.resource.PartConfig;
import org.mars_sim.msp.core.resource.PartPackageConfig;
import org.mars_sim.msp.core.robot.RobotConfig;
import org.mars_sim.msp.core.science.ExperimentConfig;
import org.mars_sim.msp.core.science.ScienceConfig;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.function.cooking.MealConfig;
import org.mars_sim.msp.core.structure.building.function.farming.CropConfig;
import org.mars_sim.msp.core.structure.construction.ConstructionConfig;
import org.mars_sim.msp.core.tool.Hash;
import org.mars_sim.msp.core.vehicle.VehicleConfig;

/**
 * Loads the simulation configuration XML files as DOM documents. Provides
 * simulation configuration. Provides access to other simulation subset
 * configuration classes.
 */
public class SimulationConfig implements Serializable {

	private static final long serialVersionUID = -5348007442971644450L;

	private final Logger logger = Logger.getLogger(SimulationConfig.class.getName());

	private final String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());
	
	/** The version.txt denotes the xml build version. */	
	public static final String VERSION_FILE = "version.txt";
	/** The exception.txt denotes any user modified xml to be included to bypass the checksum. */	
	public static final String EXCEPTION_FILE = "exception.txt";
			
	public static final String XML_FOLDER = "xml";
	public static final String XML_EXTENSION = ".xml";
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
	private static final String FOODPRODUCTION_FILE = "foodProduction";
	private static final String MEAL_FILE = "meals";
	private static final String ROBOT_FILE = "robots";
	private static final String QUOTATION_FILE = "quotations";
	private static final String VALUE = "value";

    public static final String EXPERIMENTS_FILE = "/" + "json" + "/" + "experiments.json";
    
	// Simulation element names.
	private static final String TIME_CONFIGURATION = "time-configuration";

	private static final String BASE_TIME_RATIO = "base-time-ratio";
	private static final String MIN_SIMULATED_PULSE = "min-simulated-pulse";
	private static final String MAX_SIMULATED_PULSE = "max-simulated-pulse";
	private static final String DEFAULT_TIME_PULSE = "default-time-pulse";
	
	private static final String AUTOSAVE_INTERVAL = "autosave-interval";
	private static final String AVERAGE_TRANSIT_TIME = "average-transit-time";

	private static final String EARTH_START_DATE_TIME = "earth-start-date-time";
	private static final String MARS_START_DATE_TIME = "mars-start-date-time";

	private static final String ACCURACY_BIAS = "accuracy-bias";

	private static final String DEFAULT_UNUSEDCORES = "unused-cores";

	private transient double tr = 0;

	private transient int[] data = new int[] { 0, 0, 0, 0 };

	private transient String marsStartDate = null;
	private transient String earthStartDate = null;

	/*
	 * -----------------------------------------------------------------------------
	 * Members
	 * -----------------------------------------------------------------------------
	 */

	/** DOM documents. */
	private transient Document simulationDoc;

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
	
	private transient ExperimentConfig experimentConfig;
	private transient ScienceConfig scienceConfig;

	private transient List<String> excludedList;

	private transient ReportingAuthorityFactory raFactory;	

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

	/*
	 * -----------------------------------------------------------------------------
	 * Static Members
	 * -----------------------------------------------------------------------------
	 */

	/** Eager Instantiation of Singleton Instance. */
	private static SimulationConfig instance = new SimulationConfig();

	/*
	 * -----------------------------------------------------------------------------
	 * Public Static Methods
	 * -----------------------------------------------------------------------------
	 */

	/**
	 * Gets a singleton instance of the simulation config.
	 * 
	 * @return SimulationConfig instance
	 */
	public static SimulationConfig instance() {
		return instance;
	}

	/**
	 * Loads all of the configuration files.
	 * 
	 * @throws Exception if error loading or parsing configuration files.
	 */
	public void loadConfig() {
		if (simulationDoc != null) {
			return;
		}
		
		checkXMLFileVersion();

		try {
			loadDefaultConfiguration();
		} catch (Exception e) {
//          	e.printStackTrace();
          	logger.log(Level.SEVERE, "Cannot load default config : " + e.getMessage(), e);
		}

	}
	
	/**
	 * Reloads the configurations from the XML files including
	 * re-checking the XML versions.
	 * Should need ot be used if the files have changed as Comfig
	 * objects should be immutable.
	 */
	public void reloadConfig() {
		simulationDoc = null;
		
		logger.info("Configurations reloading");
		loadConfig();
	}
	/**
	 * Checks if the xml files are of the same version of the core engine.
	 */
	private void checkXMLFileVersion() {
		boolean sameBuild = false;
		
		String backupDir = SimulationFiles.getBackupDir();
	
        File xmlLoc = new File(SimulationFiles.getXMLDir());		
		File versionLoc = new File(SimulationFiles.getXMLDir() + File.separator + VERSION_FILE);
		File exceptionLoc = new File(SimulationFiles.getXMLDir() + File.separator + EXCEPTION_FILE);
		File backupLoc = new File(backupDir);
			
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
			LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, "'" + xmlLoc +  "'" 
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
		boolean exceptionFileExist = exceptionLoc.exists();
		
		boolean xmlDirDeleted = false;
		boolean invalid = false;

		String buildText = "";
		
		// if the "xml" directory exists, back up everything inside and clean the directory
		if (xmlDirExist && xmlLoc.isDirectory()) {
			LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, 
			"The xml folder already existed.");		

			if (versionFileExist) {
				try (BufferedReader buffer = new BufferedReader(new FileReader(versionLoc))) {   
				    if ((buildText = buffer.readLine()) != null) {
				    	// If the version.txt's build version tag is the same as the core engine's
					    if (buildText.equals(Simulation.BUILD)) {
					    	sameBuild = true;
					    }				    
				    }
				} catch (FileNotFoundException e) {
		          	logger.log(Level.SEVERE, "Cannot find version.txt : " + e.getMessage());
				} catch (IOException e) {
		          	logger.log(Level.SEVERE, "Cannot access version.txt : " + e.getMessage());
				}
			}
			
			if (exceptionFileExist) {
				try (BufferedReader buffer = new BufferedReader(new FileReader(exceptionLoc))) {  
					// Need to figure out how to make use of 
					// exception.txt for tracking user's made changes in xml
				} catch (FileNotFoundException e) {
		          	logger.log(Level.SEVERE, "Cannot find exception.txt : " + e.getMessage());
				} catch (IOException e) {
		          	logger.log(Level.SEVERE, "Cannot access exception.txt : " + e.getMessage());
				}
			}
		}
		
		if (!xmlDirExist)
			LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, 
				"The xml folder does not exist in user home.");	
		else if (!versionFileExist)
			LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, 
				"The version.txt does not exist.");	
		else if (sameBuild)
			LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, 
					"The version.txt has the same BUILD " + buildText
					+ " as the core engine's.");
		else if (!hasNonDigit(buildText))
	    	LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, 
					"The version.txt in your home xml folder shows BUILD " + buildText 
					+ ". The core engine uses BUILD " + Simulation.BUILD + ".");
		else {
			LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, 
				"The version.txt is invalid.");
			invalid = true;
		}
		
		if (xmlDirExist) {
			if (!versionFileExist || buildText.equals("") || !sameBuild || hasNonDigit(buildText)) {
				try {
					if (versionFileExist && !buildText.equals("") && !invalid) {		
						String s0 = backupDir + File.separator + buildText;
				        File dir = new File(s0.trim());
				        if (!dir.exists()) {
				        	// Case A1 : Copy it to /.mars-sim/backup/buildText/
							LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, 
									"Case A1 : The build folder doesn't exist yet. " +
									"Back up to " + s0);
							// Make a copy everything in the /xml to the /{$version}
							FileUtils.moveDirectoryToDirectory(xmlLoc, dir, true);   	
				        }
				        else {
				        	// Case A2 :  Copy it to /.mars-sim/backup/{$buildText}/{$timestamp}/
				        	// if that buildText directory already exists
				        	// Get timestamp in UTC
//				            Instant timestamp = Instant.now();
				            String timestamp = LocalDateTime.now().toString().replace(":", "").replace("-", "");
				            int lastIndxDot = timestamp.lastIndexOf('.');
				            timestamp = timestamp.substring(0, lastIndxDot);				            
				            String s1 = s0 + File.separator + timestamp;
				            dir = new File(s1.trim());
							LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, 
									"Case A2 : The build folder " +
									s0 + " already exists. Back up to " + s1);
							// Make a copy everything in the /xml to the /{$version}
							FileUtils.moveDirectoryToDirectory(xmlLoc, dir, true);
				        }
					}
					else {		
						if (!backupLoc.exists()) {
							// Case B1 : Copy it to /.mars-sim/backup/
							LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, 
									"Case B1 : The backup folder doesn't exist. " +
									"Back up to " + backupDir);
							// Make a copy everything in the /xml to the /backup/xml
							FileUtils.moveDirectoryToDirectory(xmlLoc, backupLoc, true);
				        }		
						else {
							// Case B2 : Copy it to /.mars-sim/backup/{$timestamp}/
//				            Instant timestamp = Instant.now();
				            String timestamp = LocalDateTime.now().toString().replace(":", "").replace("-", "");
				            int lastIndxDot = timestamp.lastIndexOf('.');
				            timestamp = timestamp.substring(0, lastIndxDot);			            
				            String s2 = backupDir + File.separator + "unknown" + File.separator + timestamp;
				            
				            backupLoc = new File(s2);
							LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, 
									"Case B2 : The backup folder " +
									backupDir + " already exists. Back up to " + s2);	
							// Make a copy everything in the /xml to the /backup/xml
							FileUtils.moveDirectoryToDirectory(xmlLoc, backupLoc, true);
						}
					}		

					// delete everything in the xml folder
					xmlDirDeleted = deleteDirectory(xmlLoc);
	
				} catch (IOException e) {
		          	logger.log(Level.SEVERE, "Issues with build folder or backup folder: " + e.getMessage());
				}
			}
		}
		
		xmlDirExist = xmlLoc.exists();

		// if the "xml" folder does NOT exist
		if (!xmlLoc.exists() || xmlDirDeleted) {
			// Create the xml folder
			versionLoc.getParentFile().mkdirs();
			LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, "A new xml folder was just created.");
			
			List<String> lines = Arrays.asList(Simulation.BUILD);
			try {
				// Create the version.txt file
				Files.write(versionPath, lines, StandardCharsets.UTF_8);
				LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, "A new version.txt file was just created.");
			} catch (IOException e) {
	          	logger.log(Level.SEVERE, "Cannot write lines when creating version.txt" + e.getMessage());
			}
			
			lines = new ArrayList<>();
			try {
				// Create the exception.txt file
				Files.write(exceptionPath, lines, StandardCharsets.UTF_8);
				LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, "A new exception.txt file was just created.");
			} catch (IOException e) {
	          	logger.log(Level.SEVERE, "Cannot write lines when creating exception.txt" + e.getMessage());
			}
		}
		
		if (!versionLoc.exists()) {
			List<String> lines = Arrays.asList(Simulation.BUILD);
			try {
				// Create the version.txt file
				Files.write(versionPath, lines, StandardCharsets.UTF_8);
				LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, "A new version.txt file was just created.");
			} catch (IOException e) {
	          	logger.log(Level.SEVERE, "Cannot write lines when creating version.txt" + e.getMessage());
			}
		}
		if (!exceptionLoc.exists()) {	
			List<String> lines = new CopyOnWriteArrayList<>();
			try {
				// Create the exception.txt file
				Files.write(exceptionPath, lines, StandardCharsets.UTF_8);
				LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, "A new exception.txt file was just created.");
			} catch (IOException e) {
	          	logger.log(Level.SEVERE, "Cannot write lines when creating exception.txt" + e.getMessage());
			}
		}
	}

	/**
	 * Checks if the string contains non-digits
	 * 
	 * @param name
	 * @return true if it contains non-digits
	 */
	private boolean hasNonDigit(String name) {
		if (name != null) {
		    char[] chars = name.toCharArray();
	
		    for (char c : chars) {
		        if(!Character.isDigit(c)) {
		            return true;
		        }
		    }
		}
	    return false;
	}
	
	/* 
	* Delete a non empty directory 
	*/ 
	public static boolean deleteDirectory(File dir) {
		if (dir.isDirectory()) {
			File[] children = dir.listFiles();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDirectory(children[i]);
				if (!success) {
					return false;
				}
			}
		}
		 
		// either file or an empty directory 
//		System.out.println("removing file or directory : " + dir.getName());
		return true; 
	}
    
	/**
	 * Find a string value.
	 * @param parent Parent element
	 * @param child Value element
	 * @return String value found
	 */
	private String findValue(String parent, String child) {
		Element root = simulationDoc.getRootElement();
		Element timeConfig = root.getChild(parent);
		Element timeRatioEL = timeConfig.getChild(child);
		String str = timeRatioEL.getAttributeValue(VALUE);
	
	
		if ((str == null) || str.trim().length() == 0)
			throw new IllegalStateException(parent + "->" + child + " must be greater than zero and cannot be blank.");
		return str;
	}
	
	private int loadIntValue(String parent, String child) {
		String str = findValue(parent, child);
		int i = 0;
		try {
			i = Integer.parseInt(str.trim());

		} catch (NumberFormatException nfe) {
			System.out.println("SimulationConfig : NumberFormatException found in " + parent + "->" + child
								+ " : " + nfe.getMessage());
			throw nfe;
		}
		return i;
	}

	private double loadDoubleValue(String parent, String child) {
		String str = findValue(parent, child);
		double d = 0;
		try {
			d = Double.valueOf(str.trim()).doubleValue();

		} catch (NumberFormatException nfe) {
			System.out.println("SimulationConfig : NumberFormatException found in " + parent + "->" + child
								+ " : " + nfe.getMessage());
			throw nfe;
		}
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
	public double getTimeRatio() {
		if (tr != 0) {
			return tr;
		}

		else {
			double d = loadDoubleValue(TIME_CONFIGURATION, BASE_TIME_RATIO);
			if (d < 16 && d > 2048)
				throw new IllegalStateException("time_ratio must be between 16.0 and 2048.0");
			tr = d;
			return d;
		}
	}
	
	/**
	 * Gets the minimum simulation pulse size in terms of MilliSol in simulation.xml
	 * 
	 * @return minimum
	 * @throws Exception if ratio is not in configuration or is not valid.
	 */
	public double getMinSimulatedPulse() {
		double result = loadDoubleValue(TIME_CONFIGURATION, MIN_SIMULATED_PULSE);
						
		if (result <= 0)
			throw new IllegalStateException(TIME_CONFIGURATION + "->" + MIN_SIMULATED_PULSE
					                        + " must be greater then zero");
		return result;
	}

	/**
	 * Gets the maximum simulation pulse size in terms of MilliSol in simulation.xml
	 * 
	 * @return minimum
	 * @throws Exception if ratio is not in configuration or is not valid.
	 */
	public double getMaxSimulatedPulse() {
		double result = loadDoubleValue(TIME_CONFIGURATION, MAX_SIMULATED_PULSE);
		
		if (result <= 0)
			throw new IllegalStateException(TIME_CONFIGURATION + "->" + MAX_SIMULATED_PULSE
					                        + " must be greater then zero");
		return result;
	}

	/**
	 * Load the accuracy bias. Must be between 0 -> 10.
	 * @return
	 */
	public double getAccuracyBias() {
		double result = loadDoubleValue(TIME_CONFIGURATION, ACCURACY_BIAS);
		
		if ((result < 0) || (result > 10)) {
			throw new IllegalStateException(TIME_CONFIGURATION + "->" + ACCURACY_BIAS
					                        + " must be between 0 & 10");
		}
		return result;
	}

	/**
	 * Load the default elapsed time for each pulse. Must be positive.
	 * @return Millisec
	 */
	public int getDefaultPulsePeriod() {
		int result = loadIntValue(TIME_CONFIGURATION, DEFAULT_TIME_PULSE);
		
		if (result <= 0)
			throw new IllegalStateException(TIME_CONFIGURATION + "->" + DEFAULT_TIME_PULSE
					                        + " must be greater then zero");
		return result;
	}
	

	/**
	 * The difference between number of cores and Simulation threads created, i.e. unused cores. Must be positive.
	 * @return Millisec
	 */
	public int getUnusedCores() {
		int result = loadIntValue(TIME_CONFIGURATION, DEFAULT_UNUSEDCORES);
		
		if (result < 0)
			throw new IllegalStateException(TIME_CONFIGURATION + "->" + DEFAULT_UNUSEDCORES
					                        + " cannot be negative");
		return result;
	}

	/**
	 * Gets the Earth date/time when the simulation starts.
	 * 
	 * @return date/time as string in "MM/dd/yyyy hh:mm:ss" format.
	 * @throws Exception if value is null or empty.
	 */
	public String getEarthStartDateTime() {
		if (earthStartDate == null) {
			Element root = simulationDoc.getRootElement();
			Element timeConfig = root.getChild(TIME_CONFIGURATION);
			Element date = timeConfig.getChild(EARTH_START_DATE_TIME);
			earthStartDate = date.getAttributeValue(VALUE);
			if ((earthStartDate == null) || earthStartDate.trim().length() == 0)
				throw new IllegalStateException("Earth start date time must not be blank.");
		}
		return earthStartDate;
	}

	/**
	 * Gets the Mars date/time when the simulation starts.
	 * 
	 * @return date/time as string in "orbit-month-sol:millisol" format.
	 * @throws Exception if value is null or empty.
	 */
	public String getMarsStartDateTime() {
		if (marsStartDate == null) {
			Element root = simulationDoc.getRootElement();
			Element timeConfig = root.getChild(TIME_CONFIGURATION);
			Element date = timeConfig.getChild(MARS_START_DATE_TIME);
			marsStartDate = date.getAttributeValue(VALUE);
			if ((marsStartDate == null) || marsStartDate.trim().length() == 0)
				throw new IllegalStateException("Mars start date time must not be blank.");
		}

		return marsStartDate;
	}

	/**
	 * Manually sets the autosave interval.
	 * 
	 * @param value
	 */
	public void setAutosaveInterval(int value) {
		data[2] = value;
	}
	
	/**
	 * Gets the autosave interval when the simulation starts.
	 * 
	 * @return number of minutes.
	 * @throws Exception if value is null or empty.
	 */
	public int getAutosaveInterval() {
		if (data[2] != 0) {
			return data[2];
		}

		else {
			int d = loadIntValue(TIME_CONFIGURATION, AUTOSAVE_INTERVAL);
			if (d < 1 || d > 1440)
				throw new IllegalStateException("autosave_interval must be between 1 and 1440.");
			data[2] = d;
			return d;
		}
	}

	/**
	 * Gets the AverageTransitTime when the simulation starts.
	 * 
	 * @return number of sols.
	 * @throws Exception if value is null or empty.
	 */
	public int getAverageTransitTime() {
		if (data[3] != 0) {
			return data[3];
		}

		else {
			int d = loadIntValue(TIME_CONFIGURATION, AVERAGE_TRANSIT_TIME);
			if (d < 0 || d > 430)
				throw new IllegalStateException("average-transit-time must be between 0 and 430.");
			data[3] = d;
			return d;
		}
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
	 * Get teh manager to the ReportingAuthority
	 * @return
	 */
	public ReportingAuthorityFactory getReportingAuthorityFactory() {
		return raFactory;
	}
	
	/**
	 * Finds the requested XML file in the bundled JAR and extracts to the xml sub-directory.
	 */
	public File getBundledXML(String filename) {
//		String fullPathName = SimulationFiles.getXMLDir() + File.separator + filename + XML_EXTENSION;
//		String fullPathName = File.separator + XML_FOLDER + File.separator + filename + XML_EXTENSION;
		String fullPathName = "/" + XML_FOLDER + "/" + filename + XML_EXTENSION;
		
		File f = new File(SimulationFiles.getXMLDir(), filename + XML_EXTENSION);
		String checksumOldFile = null;
		
		File testf = new File(SimulationFiles.getXMLDir(), filename); // no xml extension
		String checksumTestFile = null;
				
		// Since the xml file does NOT exist in the home directory, start the input stream for copying
		try (InputStream stream = SimulationConfig.class.getResourceAsStream(fullPathName)) {
			if (stream == null) {
				logger.severe("Cannot find the bundled XML " + fullPathName);
				return null;
			}
			int bytes = stream.available();
			
			if (bytes != 0) {
				Path testPath = testf.getAbsoluteFile().toPath();
				testf.mkdirs();
	
				// Copy the xml files from within the jar to user home's xml directory
				Files.copy(stream, testPath, StandardCopyOption.REPLACE_EXISTING);
				
				// Obtain the checksum of this file
				checksumTestFile = Hash.MD5.getChecksumString(testf);
			}
						
			if (f.exists()) {
				checksumOldFile = Hash.MD5.getChecksumString(f);
			}
			else {
				// if the xml file doesn't exist
				if(testf.renameTo(f)) {
					logger.config(f.getName() + " didn't exist. Just got created.");
				} else {
					logger.config("Error in renaming the test xml file " + testf.getName());
				}  
			}
				
			if (f.exists() && f.canRead()) {        
				if (checksumOldFile != null && !checksumOldFile.equals(checksumTestFile)) {
					// need to back it up.
					logger.config("Old MD5: "+ checksumOldFile + "  New MD5: "+ checksumTestFile);
	
					if (!excludeXMLFile(filename)) {
						String s0 = SimulationFiles.getBackupDir() + File.separator + Simulation.BUILD;		
				        File dir = null;//new File(s0.trim());
				        
						// Case C2 :  Copy it to /.mars-sim/backup/{$buildText}/{$timestamp}/
			        	// if that buildText directory already exists
			        	// Get timestamp in UTC
	//				            Instant timestamp = Instant.now();
			            String timestamp = LocalDateTime.now().toString().replace(":", "").replace("-", "");
			            int lastIndxDot = timestamp.lastIndexOf('.');
			            timestamp = timestamp.substring(0, lastIndxDot);		            
			            String s1 = s0 + File.separator + timestamp;
	
			            dir = new File(s1.trim());
				            
						LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, 
								"Checksum mismatched on " + f.getName() + ". "
								+ s0 + " folder already exists. Back up " 
								+ f.toString() + " to " + s1);
						
						// Backup this old (checksum failed) xml file
						FileUtils.copyFileToDirectory(f, dir, true);
						FileUtils.deleteQuietly(f);
						
						if(testf.renameTo(f)) {
							logger.config("A new version of " + f.getName() + " just got created.");
						}
						else {
							logger.config("Error in renaming the test xml file " + testf.getName());
						}		  	
					}
					else {
				    	// The xml file is found
						logger.config(filename + " was found being referenced inside exception.txt, thus bypassing its checksum.");
					}
				}
				else {
					FileUtils.deleteQuietly(testf);
				}
	        }
		}
		catch (IOException e) {
			logger.severe("Problem getting bundled XML " + e.getMessage());
		}
        return f;
	}

	/**
	 * Checks if this bundled XML file is excluded from the automatic extraction.
	 * @param filename
	 * @return
	 */
	private boolean excludeXMLFile(String filename) {
		if (excludedList == null) {
			excludedList = new ArrayList<String>();

			File exceptionFile = new File(SimulationFiles.getXMLDir() + File.separator
					+ EXCEPTION_FILE);
			if (exceptionFile.exists()) {
				// Read the exception.txt file to see if it mentions this particular xml file
				try (BufferedReader buffer = new BufferedReader(new FileReader(exceptionFile))) {
					excludedList.add(buffer.readLine());
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
	 * @throws Exception     if XML could not be parsed or file could not be found.
	 */
	public Document parseXMLFileAsJDOMDocument(String filename, boolean useDTD) {
		File f = getBundledXML(filename);
		if (f != null) {
			try {
			    SAXBuilder builder = new SAXBuilder();//null, null, null);
			    builder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			    builder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
				return builder.build(f);
		    }
		    catch (JDOMException | IOException e)
		    {
		        logger.severe("Can parse XML " + filename + ", " + e.getMessage());
		    }
		}
		else {
			logger.warning("Can not find default XML " + filename);
		}
		return null;
	}

	
	/**
	 * load the default config files
	 */
	private void loadDefaultConfiguration() {
		raFactory = new ReportingAuthorityFactory();

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
//		logger.config("cropConfig");
		vehicleConfig = new VehicleConfig(parseXMLFileAsJDOMDocument(VEHICLE_FILE, true));
//		logger.config("vehicleConfig");
		buildingConfig = new BuildingConfig(parseXMLFileAsJDOMDocument(BUILDING_FILE, true));
//		logger.config("buildingConfig");
		resupplyConfig = new ResupplyConfig(parseXMLFileAsJDOMDocument(RESUPPLY_FILE, true), partPackageConfig);
		settlementConfig = new SettlementConfig(parseXMLFileAsJDOMDocument(SETTLEMENT_FILE, true), partPackageConfig);
		manufactureConfig = new ManufactureConfig(parseXMLFileAsJDOMDocument(MANUFACTURE_FILE, true));
//		logger.config("manufactureConfig");
		constructionConfig = new ConstructionConfig(parseXMLFileAsJDOMDocument(CONSTRUCTION_FILE, true));
		foodProductionConfig = new FoodProductionConfig(parseXMLFileAsJDOMDocument(FOODPRODUCTION_FILE, true));
		mealConfig = new MealConfig(parseXMLFileAsJDOMDocument(MEAL_FILE, true));
		robotConfig = new RobotConfig(parseXMLFileAsJDOMDocument(ROBOT_FILE, true));
		quotationConfig = new QuotationConfig(parseXMLFileAsJDOMDocument(QUOTATION_FILE, true));
		
		experimentConfig = new ExperimentConfig(EXPERIMENTS_FILE);
		scienceConfig = new ScienceConfig();
		
		
		logger.config("Done loading all xml config files.");
	}

}
