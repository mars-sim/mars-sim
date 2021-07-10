/**
 * Mars Simulation Project
 * SimulationBuilder.java
 * @version 3.2.0 2021-06-28
 * @author Barry Evans
 */
package org.mars_sim.msp.core;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;
import org.mars_sim.msp.core.structure.InitialSettlement;
import org.mars_sim.msp.core.structure.SettlementBuilder;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.structure.SettlementTemplate;
import org.mars_sim.msp.core.tool.RandomUtil;

/*
 * This class is a Factory to bootstrap a new simulation according to
 * he various options.
 */
public class SimulationBuilder {
	private static final String LOAD = "load";
	private static final String NEW = "new";
	private static final String TIMERATIO = "timeratio";
	private static final String TEMPLATE = "template";
	private static final String DATADIR = "datadir";  
	private static final String SPONSOR = "sponsor";
	private static final String LATITUDE = "lat";
	private static final String LONGITUDE = "lon";
	
	private static final Logger logger = Logger.getLogger(SimulationBuilder.class.getName());

	private SimulationConfig simulationConfig;
	
	private int userTimeRatio = 12;
	private String template;
	private ReportingAuthorityType authority = ReportingAuthorityType.MS;
	private boolean newAllowed = false;
	private File simFile;
	private String latitude = null;
	private String longitude = null;

	public SimulationBuilder(SimulationConfig simulationConfig) {
		super();
		this.simulationConfig = simulationConfig;
	}

	/**
	 * Set the time ratio to a specific rate
	 * @param timeRatio
	 */
	public void setTimeRatio(int timeRatio) {
		this.userTimeRatio = timeRatio;
	}

	public void setLatitude(String lat) {
		String error = Coordinates.checkLat(lat);
		if (error != null) {
			throw new IllegalArgumentException(error);
		}
		latitude = lat;
	}
	
	public void setLongitude(String lon) {
		String error = Coordinates.checkLon(lon);
		if (error != null) {
			throw new IllegalArgumentException(error);
		}
		longitude = lon;
	}
	
	/**
	 * Set the name of the template for a single Settlement simulation
	 * @param optionValue
	 */
	public void setTemplate(String optionValue) {
		template = optionValue;
	}

	public String getTemplate() {
		return template;
	}

	public void setSponsor(String optionValue) {
		authority = ReportingAuthorityType.valueOf(optionValue);
	}

	public void setSimFile(String filename) {
		if (filename == null) {
			this.simFile = new File(SimulationFiles.getSaveDir(),
						Simulation.SAVE_FILE + Simulation.SAVE_FILE_EXTENSION);
		}
		else {
			if (Paths.get(filename).isAbsolute()) {
				this.simFile = new File(filename);
			}
			else {
				this.simFile = new File(SimulationFiles.getSaveDir(),
										filename);
			}
		}
	}

	/**
	 * Is there a simulation file defined for this launch
	 * @return
	 */
	public File getSimFile() {
		return this.simFile;
	}

	/**
	 * Get the list of core command line options that are supported bu this builder
	 * @return
	 */
	public List<Option> getCmdLineOptions() {
		List<Option> options = new ArrayList<>();

		options.add(Option.builder(TIMERATIO).argName("Ratio (power of 2)").hasArg()
								.desc("Define the time ratio of the simulation").build());
		options.add(Option.builder(DATADIR).argName("path to data directory").hasArg().optionalArg(false)
				.desc("Path to the data directory for simulation files (defaults to user.home)").build());
		
		options.add(Option.builder(NEW)
						.desc("Create a new simulation if one is not present").build());
		options.add(Option.builder(LOAD).argName("path to simulation file").hasArg().optionalArg(true)
						.desc("Load the a previously saved sim, default is used if none specifed").build());
		options.add(Option.builder(TEMPLATE).argName("template").hasArg().optionalArg(false)
						.desc("New simulation from a template").build());
		options.add(Option.builder(SPONSOR).argName("sponsor").hasArg().optionalArg(false)
						.desc("Set the sponsor for the settlement template").build());		
		options.add(Option.builder(LATITUDE).argName("latitude").hasArg().optionalArg(false)
				.desc("Set the latitude of the new template Settlement").build());	
		options.add(Option.builder(LONGITUDE).argName("longitude").hasArg().optionalArg(false)
				.desc("Set the longitude of the new template Settlement").build());	
		return options;
	}

	/**
	 * Parse the command line and process the core Simulation arguments
	 * @param line
	 */
	public void parseCommandLine(CommandLine line) {	

		if (line.hasOption(TIMERATIO)) {
			setTimeRatio(Integer.parseInt(line.getOptionValue(TIMERATIO)));
		}
		if (line.hasOption(NEW)) {
			newAllowed = true;
		}
		if (line.hasOption(TEMPLATE)) {
			setTemplate(line.getOptionValue(TEMPLATE));
		}
		if (line.hasOption(SPONSOR)) {
			setSponsor(line.getOptionValue(SPONSOR));
		}
		if (line.hasOption(LOAD)) {
			setSimFile(line.getOptionValue(LOAD));
		}
		if (line.hasOption(LATITUDE)) {
			setLatitude(line.getOptionValue(LATITUDE));
		}
		if (line.hasOption(LONGITUDE)) {
			setLongitude(line.getOptionValue(LONGITUDE));
		}
		if (line.hasOption(DATADIR)) {
			SimulationFiles.setDataDir(line.getOptionValue(DATADIR));
		}
	}

	/**
	 * Uses the previously defiens options and start the required Simulation
	 */
	public void start() {
		// Load xml files
		simulationConfig.loadConfig();
		
		Simulation sim = Simulation.instance();
		
		boolean loaded = false;
		if (simFile != null) {
			loaded  = loadSimulation();
		}
		
		InitialSettlement spec = null;
		if (template != null) {
			spec = loadSettlementTemplate();
		}
		if (!loaded) {
			// Create a new simulation
			sim.createNewSimulation(userTimeRatio); 
			
			SettlementBuilder builder = new SettlementBuilder(sim.getUnitManager(),
											sim.getRelationshipManager(),
											simulationConfig);
			if (spec !=  null) {
				builder.createFullSettlement(spec);
			}
			else {
				builder.createInitialSettlements();
			}
		}

		sim.startClock(false);
	}

	/**
	 * Load a previously saved simulation.
	 */
	private boolean loadSimulation() {
		
		boolean result = false;
	
		if (simFile.exists()) {
			if (!simFile.canRead()) {
				throw new IllegalArgumentException("Problem: simulation file can not be opened: " + simFile.getAbsolutePath());
			}
			logger.config("Loading from " + simFile.getAbsolutePath());

			
			Simulation sim = Simulation.instance();
			
			// Create class instances
			sim.createNewSimulation(userTimeRatio);
			
			sim.loadSimulation(simFile);			
			result  = true;
		}
		else if (!newAllowed) {
			// Not allowed to create a new simulation so throw error
			throw new IllegalArgumentException("Problem: simulation file does not exist: " + simFile.getAbsolutePath());			
		}
		return result;
	}

	/**
	 * Loads the prescribed settlement template
	 */
	private InitialSettlement loadSettlementTemplate() {
		SettlementConfig settlementConfig = simulationConfig.getSettlementConfiguration();
			
		// Find the template
		SettlementTemplate settlementTemplate = settlementConfig.getSettlementTemplate(template);	
		if (authority == null) {
			// Use the default on the template
			authority = settlementTemplate.getSponsor();
		}
		
		// Create a random name
		String settlementName = "New Settlement";
		List<String> settlementNames = settlementConfig.getSettlementNameList(authority);
		if (!settlementNames.isEmpty()) {
			settlementNames = settlementConfig.getSettlementNameList(authority);
			int size = settlementNames.size();
			int rand = RandomUtil.getRandomInt(size-1);
			settlementName = settlementNames.get(rand);
		}
		
		logger.info("Starting a single Settlement sim using template "+ template
				+ " with settlement name = " + settlementName);
		return new InitialSettlement(settlementName, authority, template, 
									 settlementTemplate.getDefaultPopulation(),
									 settlementTemplate.getDefaultNumOfRobots(),
									 longitude, latitude);
	}


}
