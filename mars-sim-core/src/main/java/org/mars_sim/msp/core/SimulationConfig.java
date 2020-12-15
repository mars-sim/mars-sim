/**
 * Mars Simulation Project
 * SimulationConfig.java
 * @version 3.1.2 2020-09-02
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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.mars_sim.msp.core.foodProduction.FoodProductionConfig;
import org.mars_sim.msp.core.interplanetary.transport.resupply.ResupplyConfig;
import org.mars_sim.msp.core.malfunction.MalfunctionConfig;
import org.mars_sim.msp.core.manufacture.ManufactureConfig;
import org.mars_sim.msp.core.mars.LandmarkConfig;
import org.mars_sim.msp.core.mars.MineralMapConfig;
import org.mars_sim.msp.core.person.CrewConfig;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.mission.ExperimentConfig;
import org.mars_sim.msp.core.person.health.MedicalConfig;
import org.mars_sim.msp.core.quotation.QuotationConfig;
import org.mars_sim.msp.core.resource.AmountResourceConfig;
import org.mars_sim.msp.core.resource.PartConfig;
import org.mars_sim.msp.core.resource.PartPackageConfig;
import org.mars_sim.msp.core.robot.RobotConfig;
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
	
	// Configuration files to load.
	public final String xmlDir = Simulation.XML_DIR;
	public final String backupDir = Simulation.BACKUP_DIR;
	public final String versionFilePathStr = Simulation.XML_DIR + File.separator + Simulation.VERSION_FILE;
	public final String exceptionFilePathStr = Simulation.XML_DIR + File.separator + Simulation.EXCEPTION_FILE;
	
	public final File xmlLocation = new File(xmlDir);		
	public final File versionFile = new File(versionFilePathStr);
	public final File exceptionFile = new File(exceptionFilePathStr);
	public File backupLocation = new File(backupDir);
	
	public final String XML_FOLDER = "/" + Msg.getString("Simulation.xmlFolder") + "/";
	public final String XML_EXTENSION = ".xml";
	public final String SIMULATION_FILE = "simulation";
	public final String PEOPLE_FILE = "people";
	public final String CREW_FILE = "crew";
	public final String VEHICLE_FILE = "vehicles";
	public final String SETTLEMENT_FILE = "settlements";
	public final String RESUPPLY_FILE = "resupplies";
	public final String MEDICAL_FILE = "medical";
	public final String MALFUNCTION_FILE = "malfunctions";
	public final String CROP_FILE = "crops";
	public final String LANDMARK_FILE = "landmarks";
	public final String MINERAL_MAP_FILE = "minerals";
	public final String BUILDING_FILE = "buildings";
	public final String PART_FILE = "parts";
	public final String PART_PACKAGE_FILE = "part_packages";
	public final String RESOURCE_FILE = "resources";
	public final String MANUFACTURE_FILE = "manufacturing";
	public final String CONSTRUCTION_FILE = "construction";
	public final String FOODPRODUCTION_FILE = "foodProduction";
	public final String MEAL_FILE = "meals";
	public final String ROBOT_FILE = "robots";
	public final String QUOTATION_FILE = "quotations";
	public final String VALUE = "value";

    public final String EXPERIMENTS_FILE = "/json/experiments.json";
    
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

	private transient double tr = 0;

	private transient int[] data = new int[] { 0, 0, 0, 0 };

	public transient String marsStartDate = null;
	public transient String earthStartDate = null;

	/*
	 * -----------------------------------------------------------------------------
	 * Members
	 * -----------------------------------------------------------------------------
	 */

	/** DOM documents. */
	private transient static Document simulationDoc;

	// Subset configuration classes
	private transient static PartConfig partConfig;
	private transient static PartPackageConfig partPackageConfig;
	private transient static AmountResourceConfig resourceConfig;
	private transient static PersonConfig personConfig;
	private transient static CrewConfig crewConfig;
	private transient static MedicalConfig medicalConfig;
	private transient static LandmarkConfig landmarkConfig;
	private transient static MineralMapConfig mineralMapConfig;
	private transient static MalfunctionConfig malfunctionConfig;
	private transient static CropConfig cropConfig;
	private transient static VehicleConfig vehicleConfig;
	private transient static BuildingConfig buildingConfig;
	private transient static SettlementConfig settlementConfig;
	private transient static ManufactureConfig manufactureConfig;
	private transient static ResupplyConfig resupplyConfig;
	private transient static ConstructionConfig constructionConfig;

	private transient static FoodProductionConfig foodProductionConfig;
	private transient static MealConfig mealConfig;
	private transient static RobotConfig robotConfig;
	private transient static QuotationConfig quotationConfig;
	
	private transient static ExperimentConfig experimentConfig;
	private transient static ScienceConfig scienceConfig;	

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
	 * Sets the singleton instance.
	 * 
	 * @param instance the singleton instance.
	 */
	public static void setInstance(SimulationConfig instance) {
		SimulationConfig.instance = instance;
	}

	/**
	 * Reloads all of the configuration files.
	 * 
	 * @throws Exception if error loading or parsing configuration files.
	 */
	public void loadConfig() {
//		logger.config("Staring loadConfig() on " + Thread.currentThread().getName());
		SimulationConfig.instance();

		if (simulationDoc != null) {
			instance.destroyOldConfiguration();
		}
		
		checkXMLFileVersion();
    	
		loadDefaultConfiguration();
		
//		logger.config("Done with loadConfig() on " + Thread.currentThread().getName());
	}
	
	/**
	 * Checks if the xml files are of the same version of the core engine.
	 */
	private void checkXMLFileVersion() {
		boolean sameBuild = false;
		    
        FileSystem fileSys = FileSystems.getDefault();
        Path versionPath = fileSys.getPath(versionFile.getPath());
        Path exceptionPath = fileSys.getPath(exceptionFile.getPath());
		Path xmlPath = fileSys.getPath(xmlLocation.getPath());
		
		// Query if the xml folder exists in user home directory
		// Query if the xml version matches
		// If not, copy all xml over

        boolean xmlDirExist = xmlPath.toFile().exists();
		
		// if "xml" exits as a file, delete it
		if (xmlDirExist && xmlLocation.isFile()) {
			LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, "'" + xmlLocation +  "'" 
					+ " is a folder and NOT supposed to exist as a file. Deleting it.");
			try {
				FileUtils.forceDelete(xmlLocation);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// Check again xmlDirExist
		xmlDirExist = xmlLocation.exists();
		String buildText = "";
		boolean versionFileExist = false;
		boolean exceptionFileExist = false;
		boolean xmlDirDeleted = false;
		boolean invalid = false;
		

		// if the "xml" directory exists, back up everything inside and clean the directory
		if (xmlDirExist && xmlLocation.isDirectory()) {
			LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, 
			"The xml folder already existed.");		
			
			versionFileExist = versionFile.exists();
			exceptionFileExist = exceptionFile.exists();
			
			if (versionFileExist) {
				BufferedReader buffer;
				try {
					buffer = new BufferedReader(new FileReader(versionFile));    
				    if ((buildText = buffer.readLine()) != null) {
				    	// If the version.txt's build version tag is the same as the core engine's
					    if (buildText.equals(Simulation.BUILD)) {
					    	sameBuild = true;
					    }				    
				    }
				    
					buffer.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
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
		
//		if (xmlDirExist && (!versionFileExist || !sameBuild)) {
//			LogConsolidated.log(Level.CONFIG, 0, sourceName, 
//					"Backing up existing xml files into a 'backup' folder. Cleaning the xml folder.");
//		}
		
		if (xmlDirExist) {
			
			if (!versionFileExist || buildText.equals("") || !sameBuild || hasNonDigit(buildText)) {
			
				try {
	
					if (versionFileExist && !buildText.equals("") && !invalid) {
						
						String s0 = backupDir + File.separator + buildText;		
				        File dir = new File(s0.trim());
				        if (!dir.exists()) {
				        	// Case A1 : Copy it to /.mars-sim/backup/buildText/
							LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, 
									"Case A1 : (The build folder doesn't exist yet) " +
									"Back up to " + s0);
							// Make a copy everything in the /xml to the /{$version}
							FileUtils.moveDirectoryToDirectory(xmlLocation, dir, true);   	
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
									"Case A2 : (The build folder " +
									s0 + " already exists) Back up to " + s1);
							// Make a copy everything in the /xml to the /{$version}
							FileUtils.moveDirectoryToDirectory(xmlLocation, dir, true);
				        }
					}
	
					else {
						
						if (!backupLocation.exists()) {
							// Case B1 : Copy it to /.mars-sim/backup/
							LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, 
									"Case B1 : (The backup folder doesn't exist) " +
									"Back up to " + backupDir);
							// Make a copy everything in the /xml to the /backup/xml
							FileUtils.moveDirectoryToDirectory(xmlLocation, backupLocation, true);
				        }
						
						else {
							// Case B2 : Copy it to /.mars-sim/backup/{$timestamp}/
//				            Instant timestamp = Instant.now();
				            String timestamp = LocalDateTime.now().toString().replace(":", "").replace("-", "");
				            int lastIndxDot = timestamp.lastIndexOf('.');
				            timestamp = timestamp.substring(0, lastIndxDot);			            
				            String s2 = backupDir + File.separator + "unknown" + File.separator + timestamp;
				            
				            backupLocation = new File(s2);
							LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, 
									"Case B2 : (The backup folder " +
									backupDir + " already exists. Back up to " + s2);	
							// Make a copy everything in the /xml to the /backup/xml
							FileUtils.moveDirectoryToDirectory(xmlLocation, backupLocation, true);

						}
					}
					
//					if (buildText.equals("") || isNotNumber(buildText) || !sameBuild)
//						// delete the version.txt file 
//						versionFile.delete();
	
					// delete everything in the xml folder
	//				FileUtils.deleteDirectory(xmlLocation);
					xmlDirDeleted = deleteDirectory(xmlLocation);
	
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		xmlDirExist = xmlLocation.exists();

		// if the "xml" folder does NOT exist
		if (!xmlLocation.exists() || xmlDirDeleted) {
			// Create the xml folder
			versionFile.getParentFile().mkdirs();
			LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, "A new xml folder was just created.");
		}
		
//		if (!sameBuild || invalid || !xmlDirExist) {
			if (!versionFileExist) {
				List<String> lines = Arrays.asList(Simulation.BUILD);
				try {
					// Create the version.txt file
					Files.write(versionPath, lines, StandardCharsets.UTF_8);
					LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, "A new version.txt file was just created.");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (!exceptionFileExist) {				
				List<String> lines = new CopyOnWriteArrayList<>();
				try {
					// Create the exception.txt file
					Files.write(exceptionPath, lines, StandardCharsets.UTF_8);
					LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, "A new exception.txt file was just created.");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
//		}
	}

	/**
	 * Checks if the string contains non-digits
	 * 
	 * @param name
	 * @return true if it contains non-digits
	 */
	private boolean hasNonDigit(String name) {
	    char[] chars = name.toCharArray();

	    for (char c : chars) {
	        if(!Character.isDigit(c)) {
	            return true;
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
	 * Gets the crew config subset.
	 * 
	 * @return crew config
	 */
	public CrewConfig getCrewConfig() {
		return crewConfig;
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
	 * Parses an XML file into a DOM document.
	 * 
	 * @param filename the path of the file.
	 * @param useDTD   true if the XML DTD should be used.
	 * @return DOM document
	 * @throws Exception if XML could not be parsed or file could not be found.
	 */
//	 public static Document parseXMLFileAsJDOMDocument(String filename, boolean useDTD) {
//		SAXBuilder builder = new SAXBuilder(XMLReaders.DTDVALIDATING);
//	  
//	    File xmlFile = new File(filename); 
//		Document document = null;
//		System.out.println("Parsing FILE: "+ xmlFile.getAbsolutePath()); 
//		try {
//	  
//	         document = builder.build(xmlFile);
//	  
//	       } catch (IOException | JDOMException e) {
//	          System.out.println(e.getMessage()); 
//			}
//	  		return result; 
//		}

//	public static Document parseXMLFileAsJDOMDocument(String filename, boolean useDTD)
//			throws IOException, JDOMException {
//		InputStream stream = getInputStream(filename);
//		
////		bug 2909888: read the inputstream with a specific encoding instead of the
////		system default.	 
//		InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
//		SAXBuilder saxBuilder = new SAXBuilder(useDTD);
//		
////		[landrus, 26.11.09]: Use an entity resolver to load dtds from the classpath	 
//		saxBuilder.setEntityResolver(new ClasspathEntityResolver());
//		Document result = saxBuilder.build(reader);
//		stream.close();
//
//		return result;
//	}

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
	    
	    boolean exceptionFileExist = exceptionFile.exists();
	    
		String fullPathName = XML_FOLDER + filename + XML_EXTENSION;
		
		File f = new File(Simulation.XML_DIR, filename + XML_EXTENSION);
		String checksumOldFile = null;
		
		File testf = new File(Simulation.XML_DIR, filename); // no xml extension
		String checksumTestFile = null;
				
//		if (!f.exists()) {
			// Since the xml file does NOT exist in the home directory, start the input stream for copying
			InputStream stream = SimulationConfig.class.getResourceAsStream(fullPathName);
			int bytes = 0;
			try {
				bytes = stream.available();
			} catch (IOException e1) {
				e1.printStackTrace();
			}		
			
			if (bytes != 0) {
//				File targetFile = new File(Simulation.XML_DIR + File.separator + filename + XML_EXTENSION);
				Path testPath = testf.getAbsoluteFile().toPath();
				try {
					// Copy the xml files from within the jar to user home's xml directory
					Files.copy(stream, testPath, StandardCopyOption.REPLACE_EXISTING);
					
					// Obtain the checksum of this file
					checksumTestFile = Hash.MD5.getChecksumString(testf);
									
//					LogConsolidated.log(logger, Level.CONFIG, 0, sourceName, 
//							"Copying " + filename + XML_EXTENSION + " to the xml folder.");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
//		}
		
			
		if (f.exists()) {
			try {
				checksumOldFile = Hash.MD5.getChecksumString(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		else {
			// if the xml file doesn't exist
			if(testf.renameTo(f)) {
				logger.config("Case C1 : " + f.getName() + " didn't exist. Just got created.");
			} else {
				logger.config("Case C1 : " + "Error in renaming the test xml file " + testf.getName());
			}  
		}
			
		if (f.exists() && f.canRead()) {
	        
	        try {

				if (checksumOldFile != null && !checksumOldFile.equals(checksumTestFile)) {
					// need to back it up.
					logger.config("Old MD5: "+ checksumOldFile + "  New MD5: "+ checksumTestFile);

					boolean xmlFileMentioned = false;
					if (exceptionFileExist) {
						// Read the exception.txt file to see if it mentions this particular xml file
						BufferedReader buffer;
						String line = "";
						try {
							buffer = new BufferedReader(new FileReader(exceptionFile));
							line = buffer.readLine();
							
						    while (line != null && !line.equals("")) {
						    	// If the exception.txt does mention this xml file
							    if (line.equals(filename)) {						
							    	xmlFileMentioned = true;
							    	break;
							    }
							    line = buffer.readLine();
						    }
						    
							buffer.close();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					if (!xmlFileMentioned) {
				    	
							String backupDir = Simulation.BACKUP_DIR;
							String s0 = backupDir + File.separator + Simulation.BUILD;		
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
									"Case C2 : checksum mismatched on " + f.getName() + ". "
									+ s0 + " folder already exists. Back up " 
									+ f.toString() + " to " + s1);
							
							// Backup this old (checksum failed) xml file
							FileUtils.copyFileToDirectory(f, dir, true);
							FileUtils.deleteQuietly(f);
							
							if(testf.renameTo(f)) {
								logger.config("A new version of " + f.getName() + " just got created.");
							} else {
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
	
	/*
	 * -----------------------------------------------------------------------------
	 * Private Methods
	 * -----------------------------------------------------------------------------
	 */

	private void loadDefaultConfiguration() {
		try {
//			logger.config("Loading xml files...");
			// Load simulation document
			simulationDoc = parseXMLFileAsJDOMDocument(SIMULATION_FILE, true);
			// Load subset configuration classes.
			resourceConfig = new AmountResourceConfig(parseXMLFileAsJDOMDocument(RESOURCE_FILE, true));	
			partConfig = new PartConfig(parseXMLFileAsJDOMDocument(PART_FILE, true));
			partPackageConfig = new PartPackageConfig(parseXMLFileAsJDOMDocument(PART_PACKAGE_FILE, true));
			personConfig = new PersonConfig(parseXMLFileAsJDOMDocument(PEOPLE_FILE, true));
			crewConfig = new CrewConfig(parseXMLFileAsJDOMDocument(CREW_FILE, true));
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
			foodProductionConfig = new FoodProductionConfig(parseXMLFileAsJDOMDocument(FOODPRODUCTION_FILE, true));
			mealConfig = new MealConfig(parseXMLFileAsJDOMDocument(MEAL_FILE, true));
			robotConfig = new RobotConfig(parseXMLFileAsJDOMDocument(ROBOT_FILE, true));
			quotationConfig = new QuotationConfig(parseXMLFileAsJDOMDocument(QUOTATION_FILE, true));
			
			experimentConfig = new ExperimentConfig(EXPERIMENTS_FILE);
			scienceConfig = new ScienceConfig();
			
			logger.config("Done loading all xml files.");
			logger.config("Please go to the mars-sim console's Main Menu to choose an option.");
			
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error reading config file(s) below : " + e.getMessage());
			e.printStackTrace();
		}
	}


	/**
	 * Prepares all configuration objects for garbage collection.
	 */
	private void destroyOldConfiguration() {
		simulationDoc = null;
		resourceConfig = null;
		partConfig = null;
		partPackageConfig.destroy();
		partPackageConfig = null;
		personConfig.destroy();
		personConfig = null;
		medicalConfig.destroy();
		medicalConfig = null;
		landmarkConfig.destroy();
		landmarkConfig = null;
		mineralMapConfig.destroy();
		mineralMapConfig = null;
		malfunctionConfig.destroy();
		malfunctionConfig = null;
		cropConfig.destroy();
		cropConfig = null;
		vehicleConfig.destroy();
		vehicleConfig = null;
		buildingConfig.destroy();
		buildingConfig = null;
		resupplyConfig.destroy();
		resupplyConfig = null;
		settlementConfig.destroy();
		settlementConfig = null;
		manufactureConfig.destroy();
		manufactureConfig = null;
		constructionConfig.destroy();
		constructionConfig = null;
		foodProductionConfig.destroy();
		foodProductionConfig = null;
		mealConfig.destroy();
		mealConfig = null;
		robotConfig.destroy();
		robotConfig = null;
		quotationConfig.destroy();
		quotationConfig = null;
//		experimentConfig.destroy();
//		experimentConfig = null;
//		scienceConfig.destroy();
//		scienceConfig = null;
	}

}
