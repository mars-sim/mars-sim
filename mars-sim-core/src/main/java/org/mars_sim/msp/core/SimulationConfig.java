/**
 * Mars Simulation Project
 * SimulationConfig.java
 * @version 3.1.0 2017-11-06
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
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
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
	private static final String BASE_TIME_BETWEEN_UPDATES = "base-time-between-updates";

	private static final String AUTOSAVE_INTERVAL = "autosave-interval";
	private static final String AVERAGE_TRANSIT_TIME = "average-transit-time";
	private static final String MAX_FRAME_SKIPS = "max-frame-skips";
	private static final String NO_DELAYS_PER_YIELD = "no-delays-per-yield";

	private static final String EARTH_START_DATE_TIME = "earth-start-date-time";
	private static final String MARS_START_DATE_TIME = "mars-start-date-time";

	private transient double tbu = 0;
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

//	/**
//	 * Gets a Bill Pugh Singleton instance of the simulation.
//	 * 
//	 * @return Simulation instance
//	 */
//	 public static SimulationConfig instance() {
//	 logger.info("Simulation's instance() is on " +
//	 Thread.currentThread().getName() + " Thread");
//	 NOTE: Simulation.instance() is accessible on any threads or by any threads
//	 return SingletonHelper.INSTANCE;
//	 }

//	/**
//	 * Initializes an inner static helper class for Bill Pugh Singleton Pattern
//	 * Note: as soon as the instance() method is called the first time, the class is
//	 * loaded into memory and an instance gets created. Advantage: it supports
//	 * multiple threads calling instance() simultaneously with no synchronized
//	 * keyword needed (which slows down the VM) {@link SingletonHelper} is loaded on
//	 * the first execution of {@link Singleton#instance()} or the first access to
//	 * {@link SingletonHelper#INSTANCE}, not before.
//	 */
//	 private static class SingletonHelper{
//	 private static final SimulationConfig INSTANCE = new SimulationConfig();
//	 }

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
	public void checkXMLFileVersion() {
		boolean sameBuild = false;
		
		String xmlDir = Simulation.XML_DIR;
		String backupDir = Simulation.BACKUP_DIR;
		String versionPathStr = Simulation.XML_DIR + File.separator + Simulation.VERSION_FILE;
		
        File xmlLocation = new File(xmlDir);		
        File backupLocation = new File(backupDir);
        File versionFile = new File(versionPathStr);
    
        FileSystem fileSys = FileSystems.getDefault();
        Path versionPath = fileSys.getPath(versionFile.getPath());
		Path xmlPath = fileSys.getPath(xmlLocation.getPath());
		
		// Query if the /xml folder exists in user home directory
		// Query if the xml version matches
		// If not, copy all xml over

        boolean xmlDirExist = xmlPath.toFile().exists();

		// if "xml" exits as a file, delete it
		if (xmlDirExist && xmlLocation.isFile()) {
			LogConsolidated.log(Level.CONFIG, 0, sourceName, "'" + xmlLocation +  "'" 
					+ " is not supposed to exist as a file. Deleting it.");
			try {
				FileUtils.forceDelete(xmlLocation);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		xmlDirExist = xmlLocation.exists();
		String buildText = "";
		boolean versionFileExist = false;
		boolean xmlDirDeleted = false;
		boolean invalid = false;
		
		// if the "xml" directory exists, back up everything inside and clean the directory
		if (xmlDirExist && xmlLocation.isDirectory()) {
			LogConsolidated.log(Level.CONFIG, 0, sourceName, 
			"/xml folder already existed.");		
			
			versionFileExist = versionFile.exists();
			if (versionFileExist) {
				BufferedReader brTest;
				try {
					brTest = new BufferedReader(new FileReader(versionFile));
				    if ((buildText = brTest.readLine()) != null) {
					    if (buildText.equals(Simulation.BUILD)) {	
					    	sameBuild = true;
					    }
				    }
					brTest.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			

			// TODO: need to check if all the xml files exist 
			// TODO: Generate checksum on each of xml to see if it has been altered. 
			// TODO: If it does, back them up into a subdirectory and start off a new batch of xml files
//			String SHA512 = DatatypeConverter.printHexBinary(Hash.SHA512.checksum(file))
//			may use FileUtils.checksum(file, checksum)
		} 
		
		if (!xmlDirExist)
			LogConsolidated.log(Level.CONFIG, 0, sourceName, 
				"The /xml folder does not exist in user home.");	
		else if (!versionFileExist)
			LogConsolidated.log(Level.CONFIG, 0, sourceName, 
				"The version.txt does not exist.");	
		else if (sameBuild)
			LogConsolidated.log(Level.CONFIG, 0, sourceName, 
					"Your version.txt already has the same BUILD " + buildText
					+ " as the core engine.");
		else if (!hasNonDigit(buildText))
	    	LogConsolidated.log(Level.CONFIG, 0, sourceName, 
					"Your version.txt has BUILD " + buildText 
					+ ". The core engine has BUILD " + Simulation.BUILD);
		else {
			LogConsolidated.log(Level.CONFIG, 0, sourceName, 
				"Your version.txt is invalid.");
			invalid = true;
		}
		
//		if (xmlDirExist && (!versionFileExist || !sameBuild)) {
//			LogConsolidated.log(Level.CONFIG, 0, sourceName, 
//					"Backing up existing xml files into a 'backup' folder. Cleaning the /xml folder.");
//		}
		
		if (xmlDirExist) {
			
			if (!versionFileExist || buildText.equals("") || !sameBuild || hasNonDigit(buildText)) {
			
				try {
	
					if (versionFileExist && !buildText.equals("") && !invalid) {
						
						String s0 = backupDir + File.separator + buildText;		
				        File dir = new File(s0.trim());
				        if (!dir.exists()) {
				        	// Case A1 : Copy it to /.mars-sim/backup/buildText/
							LogConsolidated.log(Level.CONFIG, 0, sourceName, "Case A1 : " +
									"Backing up to " + s0);
							// Make a copy everything in the /xml to the /{$version}
							FileUtils.moveDirectoryToDirectory(xmlLocation, dir, true);   	
				        }
				        else {
				        	// Case A2 :  Copy it to /.mars-sim/backup/{$buildText}/{$timestamp}/
				        	// if that buildText directory already exists
				        	// Get timestamp in UTC
				            Instant timestamp = Instant.now();
				            String s1 = s0 + File.separator + timestamp.toString().replace(":", "").replace("-", "");
				            dir = new File(s1.trim());
							LogConsolidated.log(Level.CONFIG, 0, sourceName, "Case A2 : " +
									s0 + " folder already exists. Backing up to " + s1);
							// Make a copy everything in the /xml to the /{$version}
							FileUtils.moveDirectoryToDirectory(xmlLocation, dir, true);
				        }
					}
	
					else {
						
						if (!backupLocation.exists()) {
							// Case B1 : Copy it to /.mars-sim/backup/
							LogConsolidated.log(Level.CONFIG, 0, sourceName, "Case B1 : " +
									"Backing up to " + backupDir);
							// Make a copy everything in the /xml to the /backup/xml
							FileUtils.moveDirectoryToDirectory(xmlLocation, backupLocation, true);
				        }
						else {
							// Case B2 : Copy it to /.mars-sim/backup/{$timestamp}/
				            Instant timestamp = Instant.now();
				            String s2 = backupDir + File.separator + timestamp.toString().replace(":", "").replace("-", "");
				            backupLocation = new File(s2);
							LogConsolidated.log(Level.CONFIG, 0, sourceName, "Case B2 : " +
									backupDir + " already exists. Backing up to " + s2);	
							// Make a copy everything in the /xml to the /backup/xml
							FileUtils.moveDirectoryToDirectory(xmlLocation, backupLocation, true);

						}
					}
					
//					if (buildText.equals("") || isNotNumber(buildText) || !sameBuild)
//						// delete the version.txt file 
//						versionFile.delete();
	
					// delete everything in the /xml folder
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
			// Create the /xml folder
			versionFile.getParentFile().mkdirs();
			LogConsolidated.log(Level.CONFIG, 0, sourceName, "The /xml folder is created.");
		}
		
		if (!sameBuild || invalid || !versionFileExist || !xmlDirExist) {
			List<String> lines = Arrays.asList(Simulation.BUILD);
			try {
				// Create the version.txt file
				Files.write(versionPath, lines, StandardCharsets.UTF_8);
				LogConsolidated.log(Level.CONFIG, 0, sourceName, "The version.txt file is created.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
		
	
	/**
	 * Checks if the string contains non-digits
	 * 
	 * @param name
	 * @return true if it contains non-digits
	 */
	public boolean hasNonDigit(String name) {
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
			Element root = simulationDoc.getRootElement();
			Element timeConfig = root.getChild(TIME_CONFIGURATION);
			Element timeRatioEL = timeConfig.getChild(BASE_TIME_RATIO);
			String str = timeRatioEL.getAttributeValue(VALUE);

			double d = 0;

			if ((str == null) || str.trim().length() == 0)
				throw new IllegalStateException("time_ratio must be greater than zero and cannot be blank.");

			else {
				try {
					d = Double.valueOf(str.trim()).doubleValue();
					// System.out.println("double d = " + d);

					if (d < 16 && d > 2048)
						throw new IllegalStateException("time_ratio must be between 64.0 and 1024.0");

				} catch (NumberFormatException nfe) {
					System.out.println("SimulationConfig : NumberFormatException found in time_ratio : " + nfe.getMessage());
				}
			}
			// if (ratio < 0D) throw new IllegalStateException("Simulation time ratio must
			// be positive number.");
			// else if (ratio == 0D) throw new IllegalStateException("Simulation time ratio
			// cannot be zero.");
			tr = d;
			return d;
		}
	}

	/**
	 * Gets the time between updates in milliseconds in simulation.xml
	 * 
	 * @return the time interval in milliseconds
	 * @throws Exception if the value is not in configuration or is not valid.
	 */
	public double getTimeBetweenUpdates() {
		if (tbu != 0) {
			return tbu;
		}

		else {
			Element root = simulationDoc.getRootElement();
			Element timeConfig = root.getChild(TIME_CONFIGURATION);
			Element el = timeConfig.getChild(BASE_TIME_BETWEEN_UPDATES);
			String str = el.getAttributeValue(VALUE);

			double l = 0;

			if ((str == null) || str.trim().length() == 0)
				throw new IllegalStateException("time-between-updates must be greater than zero and cannot be blank.");
			else {
				try {
					l = Double.valueOf(str.trim()).doubleValue();
					// System.out.println("double tbu = " + l);

					if (l > 250 || l < 40)
						throw new IllegalStateException("time-between-updates must be between 40 and 250");

				} catch (NumberFormatException nfe) {
					System.out.println("SimulationConfig : NumberFormatException found in time-between-updates : " + nfe.getMessage());
				}
			}

			tbu = l;
			return l;
			// double result = Double.parseDouble(el.getAttributeValue(VALUE));
			// if (result < 0D) throw new IllegalStateException("time-between-updates in
			// simulation.xml must be positive number.");
			// else if (result == 0D) throw new IllegalStateException("time-between-updates
			// in simulation.xml cannot be zero.");
			// return result;
		}
	}

	/**
	 * Gets the parameter no-delays-per-yield in simulation.xml
	 * 
	 * @return the number
	 * @throws Exception if the value is not in configuration or is not valid.
	 */
	public int getNoDelaysPerYield() {
		if (data[0] != 0) {
			return data[0];
		}

		else {
			Element root = simulationDoc.getRootElement();
			Element timeConfig = root.getChild(TIME_CONFIGURATION);
			Element el = timeConfig.getChild(NO_DELAYS_PER_YIELD);

			String str = el.getAttributeValue(VALUE);

			int result = 0;

			if ((str == null) || str.trim().length() == 0)
				throw new IllegalStateException("no-delays-per-yield must be greater than zero and cannot be blank.");
			else {
				try {
					result = Integer.parseInt(str);

					if (result > 100 || result < 1)
						throw new IllegalStateException("no-delays-per-yield must be between 1 and 200.");

				} catch (NumberFormatException nfe) {
					System.out.println("SimulationConfig : NumberFormatException found in time-between-updates : " + nfe.getMessage());
				}
			}

			data[0] = result;
			return result;

			// int result = Integer.parseInt(el.getAttributeValue(VALUE));
			// if (result < 0) throw new IllegalStateException("no-delays-per-yield in
			// simulation.xml must be positive number.");
			// else if (result == 0) throw new IllegalStateException("no-delays-per-yield in
			// simulation.xml cannot be zero.");
			// return result;
		}
	}

	/**
	 * Gets the parameter max-frame-skips in simulation.xml
	 * 
	 * @return the number of frames
	 * @throws Exception if the value is not in configuration or is not valid.
	 */
	public int getMaxFrameSkips() {
		if (data[1] != 0) {
			return data[1];
		}

		else {
			Element root = simulationDoc.getRootElement();
			Element timeConfig = root.getChild(TIME_CONFIGURATION);
			Element el = timeConfig.getChild(MAX_FRAME_SKIPS);

			String str = el.getAttributeValue(VALUE);

			int result = 0;

			if ((str == null) || str.trim().length() == 0)
				throw new IllegalStateException("max-frame-skips must be greater than zero and cannot be blank.");
			else {
				try {
					result = Integer.parseInt(str);

					if (result > 50 || result < 1)
						throw new IllegalStateException("max-frame-skips must be between 1 and 200.");

				} catch (NumberFormatException nfe) {
					System.out.println("SimulationConfig : NumberFormatException found in max-frame-skips : " + nfe.getMessage());
				}
			}

			data[1] = result;
			return result;

		}
		// int result = Integer.parseInt(el.getAttributeValue(VALUE));
		// if (result < 0) throw new IllegalStateException("max-frame-skips in
		// simulation.xml must be positive number.");
		// else if (result == 0) throw new IllegalStateException("max-frame-skips in
		// simulation.xml cannot be zero.");

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
			Element root = simulationDoc.getRootElement();
			Element timeConfig = root.getChild(TIME_CONFIGURATION);
			Element el = timeConfig.getChild(AUTOSAVE_INTERVAL);
			String str = el.getAttributeValue(VALUE);

			int d = 0;

			if ((str == null) || str.trim().length() == 0)
				throw new IllegalStateException("autosave_interval must not be blank and must be greater than zero.");
			else {
				try {
					d = (int) Double.valueOf(str.trim()).doubleValue();
					// System.out.println("double d = " + d);

					if (d < 1 || d > 1440)
						throw new IllegalStateException("autosave_interval must be between 1 and 1440.");

				} catch (NumberFormatException nfe) {
					System.out.println("SimulationConfig : NumberFormatException found in autosave_interval : " + nfe.getMessage());
				}

			}

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
			Element root = simulationDoc.getRootElement();
			Element timeConfig = root.getChild(TIME_CONFIGURATION);
			Element el = timeConfig.getChild(AVERAGE_TRANSIT_TIME);
			String str = el.getAttributeValue(VALUE);

			int d = 0;

			if ((str == null) || str.trim().length() == 0)
				throw new IllegalStateException(
						"average-transit-time must not be blank and must be greater than zero.");
			else {
				try {
					d = (int) Double.valueOf(str.trim()).doubleValue();
					// System.out.println("double d = " + d);

					if (d < 0 || d > 430)
						throw new IllegalStateException("average-transit-time must be between 0 and 430.");

				} catch (NumberFormatException nfe) {
					System.out.println("SimulationConfig : NumberFormatException found in average-transit-time : " + nfe.getMessage());
				}

			}

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
	    
		String fullPathName = XML_FOLDER + filename + XML_EXTENSION;
		
		File f = new File(Simulation.XML_DIR, filename + XML_EXTENSION);

		if (!f.exists()) {
			// Since the xml file does NOT exist in the home directory, start the input stream for copying
			InputStream stream = SimulationConfig.class.getResourceAsStream(fullPathName);
			int bytes = 0;
			try {
				bytes = stream.available();
			} catch (IOException e1) {
				e1.printStackTrace();
			}		
			
			if (bytes != 0) {
				File targetFile = new File(Simulation.XML_DIR + File.separator + filename + XML_EXTENSION);
				Path path = targetFile.getAbsoluteFile().toPath();
				try {
					// Copy the xml files from within the jar to user home's xml directory
					Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING);
					LogConsolidated.log(Level.CONFIG, 0, sourceName, 
							"Copying " + filename + XML_EXTENSION + " to /xml folder.");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
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
			
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error reading config file(s) below : " + e.getMessage());
			e.printStackTrace();
		}
	}

//	/**
//	 * Gets a configuration file as an input stream.
//	 * 
//	 * @param filename the filename of the configuration file.
//	 * @return input stream
//	 * @throws IOException if file cannot be found.
//	 */
//	private static InputStream getInputStream(String filename) throws IOException {
//		/*
//		 * [landrus, 28.11.09]: dont use filesystem separators in classloader loading
//		 * envs.
//		 */
//		String fullPathName = CONF + filename + XML;
//		InputStream stream = SimulationConfig.class.getResourceAsStream(fullPathName);
//		if (stream == null)
//			throw new IOException(fullPathName + " failed to load");
//		return stream;
//	}

		
//	 public int testValue(String str, String name) {
//		int result = 0;
//	 	if ((str == null) || str.trim().length() == 0) 
//			throw new IllegalStateException(name + " must be greater than zero and cannot be blank."); 
//		else {
//			try {
//				result = Integer.parseInt(str);
//		 
//			if (result > 200 || result < 1) 
//				throw new IllegalStateException(name + " must be between 1 and 200.");
//
//			} catch (NumberFormatException nfe) { 
//				System.out.println(name + " has NumberFormatException : " + nfe.getMessage()); 
//			}
//		}
//		 
//		 return result;
//	 
//	 } 

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
	}
}