/*
 * Mars Simulation Project
 * SimulationBuilder.java
 * @date 2022-09-15
 * @author Barry Evans
 */
package org.mars_sim.msp.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.Runtime.Version;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.mars_sim.msp.core.configuration.Scenario;
import org.mars_sim.msp.core.configuration.ScenarioConfig;
import org.mars_sim.msp.core.configuration.UserConfigurableConfig;
import org.mars_sim.msp.core.logging.DiagnosticsManager;
import org.mars_sim.msp.core.person.Crew;
import org.mars_sim.msp.core.person.CrewConfig;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
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
	private static final String LOAD_ARG = "load";
	private static final String NEW_ARG = "new";
	private static final String TIMERATIO_ARG = "timeratio";
	private static final String TEMPLATE_ARG = "template";
	private static final String DATADIR_ARG = "datadir";  
	private static final String SPONSOR_ARG = "sponsor";
	private static final String LATITUDE_ARG = "lat";
	private static final String LONGITUDE_ARG = "lon";
	private static final String CREW_ARG = "crew";
	private static final String DIAGNOSTICS_ARG = "diags";
	private static final String SCENARIO_ARG = "scenario";
	
	private static final Logger logger = Logger.getLogger(SimulationBuilder.class.getName());
	
	private int userTimeRatio = 12;
	private String template;
	private String authorityName = null;
	private boolean newAllowed = false;
	private File simFile;
	private String latitude = null;
	private String longitude = null;
	private boolean useCrews = true;
	private UserConfigurableConfig<Crew> crewConfig;
	private Scenario bootstrap;

	public SimulationBuilder() {
		super();
	}

	/**
	 * Sets the time ratio to a specific rate.
	 * 
	 * @param timeRatio
	 */
	public void setTimeRatio(int timeRatio) {
		this.userTimeRatio = timeRatio;
	}

	/**
	 * Sets the loading of the crews.
	 * 
	 * @param useCrew
	 */
	public void setUseCrews(boolean useCrew) {
		this.useCrews = useCrew;
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
	 * Sets the name of the template for a single Settlement simulation.
	 * 
	 * @param optionValue
	 */
	public void setTemplate(String optionValue) {
		template = optionValue;
	}

	public void setSponsor(String optionValue) {
		authorityName = optionValue;
				//simulationConfig.getReportingAuthorityFactory().getItem(optionValue);
	}

	public void setDiagnostics(String modules) {
		try {
			for (String name : modules.split(",")) {
				if (!DiagnosticsManager.setDiagnostics(name.trim(), true)) {
					throw new IllegalArgumentException("Problem with diagnostics module " + name);
				}
			}
		}
		catch (FileNotFoundException e) {
			throw new IllegalArgumentException("Problem with diagnostics file: " + e.getMessage());			
		}
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
	 * Defines a set of crews to be used.
	 * 
	 * @param crewConfig
	 */
	public void setCrewConfig(UserConfigurableConfig<Crew> crewConfig) {
		this.crewConfig  = crewConfig;
	}

	/**
	 * Sets the scenario for a new simulation.
	 * 
	 * @param scenario
	 */
	public void setScenario(Scenario scenario) {
		this.bootstrap = scenario;
	}
	
	/**
	 * Gets the list of core command line options that are supported by this builder
	 * 
	 * @return the list
	 */
	public List<Option> getCmdLineOptions() {
		List<Option> options = new ArrayList<>();

		options.add(Option.builder(TIMERATIO_ARG).argName("Ratio (power of 2)").hasArg()
								.desc("Define the time ratio of the simulation").build());
		options.add(Option.builder(DATADIR_ARG).argName("path to data directory").hasArg().optionalArg(false)
				.desc("Path to the data directory for simulation files (defaults to user.home)").build());
		
		options.add(Option.builder(NEW_ARG)
						.desc("Create a new simulation if one is not present").build());
		options.add(Option.builder(LOAD_ARG).argName("path to simulation file").hasArg().optionalArg(true)
						.desc("Load the a previously saved sim, default is used if none specifed").build());
		options.add(Option.builder(SCENARIO_ARG).argName("scenario name").hasArg().optionalArg(false)
				.desc("New simulation from a scenario").build());
		options.add(Option.builder(TEMPLATE_ARG).argName("template name").hasArg().optionalArg(false)
						.desc("New simulation from a template").build());
		options.add(Option.builder(SPONSOR_ARG).argName(SPONSOR_ARG).hasArg().optionalArg(false)
						.desc("Set the sponsor for the settlement template").build());		
		options.add(Option.builder(LATITUDE_ARG).argName("latitude").hasArg().optionalArg(false)
				.desc("Set the latitude of the new template Settlement").build());	
		options.add(Option.builder(LONGITUDE_ARG).argName("longitude").hasArg().optionalArg(false)
				.desc("Set the longitude of the new template Settlement").build());	
		options.add(Option.builder(CREW_ARG).argName("true|false").hasArg().optionalArg(false)
				.desc("Enable or disable use of the crews").build());	
		options.add(Option.builder(DIAGNOSTICS_ARG).argName("<module>,<module>.....").hasArg().optionalArg(false)
				.desc("Enable diagnositics modules").build());	
		return options;
	}

	/**
	 * Parses the command line and process the core Simulation arguments.
	 * 
	 * @param line
	 */
	public void parseCommandLine(CommandLine line) {	

		if (line.hasOption(TIMERATIO_ARG)) {
			setTimeRatio(Integer.parseInt(line.getOptionValue(TIMERATIO_ARG)));
		}
		if (line.hasOption(NEW_ARG)) {
			newAllowed = true;
		}
		if (line.hasOption(TEMPLATE_ARG)) {
			setTemplate(line.getOptionValue(TEMPLATE_ARG));
		}
		if (line.hasOption(SPONSOR_ARG)) {
			setSponsor(line.getOptionValue(SPONSOR_ARG));
		}
		if (line.hasOption(SCENARIO_ARG)) {
			setScenarioName(line.getOptionValue(SCENARIO_ARG));
		}
		if (line.hasOption(LOAD_ARG)) {
			setSimFile(line.getOptionValue(LOAD_ARG));
		}
		if (line.hasOption(LATITUDE_ARG)) {
			setLatitude(line.getOptionValue(LATITUDE_ARG));
		}
		if (line.hasOption(LONGITUDE_ARG)) {
			setLongitude(line.getOptionValue(LONGITUDE_ARG));
		}
		if (line.hasOption(DATADIR_ARG)) {
			SimulationFiles.setDataDir(line.getOptionValue(DATADIR_ARG));
		}
		if (line.hasOption(CREW_ARG)) {
			setUseCrews(Boolean.parseBoolean(line.getOptionValue(CREW_ARG)));
		}
		if (line.hasOption(DIAGNOSTICS_ARG)) {
			setDiagnostics(line.getOptionValue(DIAGNOSTICS_ARG));
		}		
	}

	/**
	 * Sets the bootstrap Scenario based on the name.
	 * 
	 * @param name
	 */
	private void setScenarioName(String name) {
		ScenarioConfig config = new ScenarioConfig();
		Scenario found = config.getItem(name);
		if (found == null) {
			throw new IllegalArgumentException("No scenario named '" + name + "'");
		}
		setScenario(found);
	}

	/**
	 * Uses the previously defines options and start the required Simulation.
	 * 
	 * @return The new simulation started
	 */
	public Simulation start() {
		Version version = java.lang.Runtime.version();
		String WHITESPACES = "---------------------------------------------------";
		logger.config(WHITESPACES);
		logger.config("    Java Version Full String = " + version);
		logger.config("Java Version Feature Element = " + version.feature());
		logger.config("Java Version Interim Element = " + version.interim());
		logger.config("  Java Patch Element Version = " + version.patch());
		logger.config(" Java Update Element Version = " + version.update());
		logger.config("          Java Version Build = " + version.build().orElse(0));
		logger.config("  Java additional build Info = " + version.optional().orElse("None"));
		logger.config("       Java Pre-Release Info = " + version.pre().orElse("NA"));
		logger.config(WHITESPACES);
		
		// Load xml files but not until arguments parsed since it may change 
		// the data directory
		SimulationConfig simConfig = SimulationConfig.instance();
		simConfig.loadConfig();
		
		// Initialize storage manager
//		simConfig.createStorageManager();
//		simConfig.createStorageManager().start();
		
		Simulation sim = Simulation.instance();
			
		boolean loaded = false;
		if (simFile != null) {
			loaded  = loadSimulation();
		}
		
		InitialSettlement spec = null;
		if (template != null) {
			spec = loadSettlementTemplate(simConfig);
		}
		if (!loaded) {
			// Create a new simulation
			sim.createNewSimulation(userTimeRatio); 
			
			SettlementBuilder builder = new SettlementBuilder(sim,
					simConfig);
			if (useCrews && crewConfig == null) {
				crewConfig = new CrewConfig();
			}
			builder.setCrew(crewConfig);
			
			// Is the a specific template requested?
			if (spec !=  null) {
				builder.createFullSettlement(spec);
			}
			else {
				if (bootstrap == null) {
					String defaultName = ScenarioConfig.PREDEFINED_SCENARIOS[0];
					ScenarioConfig config = new ScenarioConfig();
					bootstrap = config.getItem(defaultName);
				}
				builder.createInitialSettlements(bootstrap);
				sim.getTransportManager().loadArrivingSettments(bootstrap,
																simConfig.getSettlementConfiguration(),
																simConfig.getReportingAuthorityFactory());
			}
		}

		sim.startClock(false);
		
		if (!loaded) {
			// initialize getTransportManager	
			sim.getTransportManager().init();
		}
		
		return sim;
	}

	/**
	 * Loads a previously saved simulation.
	 * 
	 * @return true if loaded
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
			// initialize ResupplyUtil.
//			new ResupplyUtil();
		}
		else if (!newAllowed) {
			// Not allowed to create a new simulation so throw error
			throw new IllegalArgumentException("Problem: simulation file does not exist: " + simFile.getAbsolutePath());			
		}
		return result;
	}

	/**
	 * Loads the prescribed settlement template.
	 * 
	 * @param simulationConfig 
	 * @return InitialSettlement
	 */
	private InitialSettlement loadSettlementTemplate(SimulationConfig simulationConfig) {
		SettlementConfig settlementConfig = simulationConfig.getSettlementConfiguration();
			
		// Find the template
		SettlementTemplate settlementTemplate = settlementConfig.getItem(template);	
		ReportingAuthority authority;
		if (authorityName == null) {
			// Use the default on the template
			String sponsorCode = settlementTemplate.getSponsor();
			authority = simulationConfig.getReportingAuthorityFactory().getItem(sponsorCode);
		}
		else {
			authority = simulationConfig.getReportingAuthorityFactory().getItem(authorityName);
		}
		
		// Create a random name
		String settlementName = "New Settlement";
		List<String> settlementNames = authority.getSettlementNames();
		if (!settlementNames.isEmpty()) {
			int size = settlementNames.size();
			int rand = RandomUtil.getRandomInt(size-1);
			settlementName = settlementNames.get(rand);
		}
		
		logger.info("Starting a single Settlement sim using template "+ template
				+ " with settlement name = " + settlementName);
		return new InitialSettlement(settlementName, authority.getName(), template, 
									 settlementTemplate.getDefaultPopulation(),
									 settlementTemplate.getDefaultNumOfRobots(),
									 new Coordinates(latitude, longitude), null);
	}

	/**
	 * Are all the pre-condition defined to start a simulation?
	 * 
	 * @return
	 */
	public boolean isFullyDefined() {
		return (template != null) || (simFile != null)
				|| (bootstrap != null);
	}
}
