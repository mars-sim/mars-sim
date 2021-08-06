/**
 * Mars Simulation Project
 * SimulationBuilder.java
 * @version 3.2.0 2021-06-28
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
import org.mars_sim.msp.core.logging.DiagnosticsManager;
import org.mars_sim.msp.core.person.CrewConfig;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityFactory;
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
	private static final String ALPHA_CREW_ARG = "alphacrew";
	private static final String DIAGNOSTICS_ARG = "diags";
	
	private static final Logger logger = Logger.getLogger(SimulationBuilder.class.getName());

	private SimulationConfig simulationConfig;
	
	private int userTimeRatio = 12;
	private String template;
	private ReportingAuthority authority = null;
	private boolean newAllowed = false;
	private File simFile;
	private String latitude = null;
	private String longitude = null;
	private boolean useAlphaCrew = true;
	private CrewConfig selectedCrew;

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

	/**
	 * Set the loading of the Alpha crew
	 * @param useCrew
	 */
	public void setUseAlphaCrew(boolean useCrew) {
		this.useAlphaCrew = useCrew;
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
		authority = ReportingAuthorityFactory.getAuthority(optionValue);
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

		options.add(Option.builder(TIMERATIO_ARG).argName("Ratio (power of 2)").hasArg()
								.desc("Define the time ratio of the simulation").build());
		options.add(Option.builder(DATADIR_ARG).argName("path to data directory").hasArg().optionalArg(false)
				.desc("Path to the data directory for simulation files (defaults to user.home)").build());
		
		options.add(Option.builder(NEW_ARG)
						.desc("Create a new simulation if one is not present").build());
		options.add(Option.builder(LOAD_ARG).argName("path to simulation file").hasArg().optionalArg(true)
						.desc("Load the a previously saved sim, default is used if none specifed").build());
		options.add(Option.builder(TEMPLATE_ARG).argName("template").hasArg().optionalArg(false)
						.desc("New simulation from a template").build());
		options.add(Option.builder(SPONSOR_ARG).argName("sponsor").hasArg().optionalArg(false)
						.desc("Set the sponsor for the settlement template").build());		
		options.add(Option.builder(LATITUDE_ARG).argName("latitude").hasArg().optionalArg(false)
				.desc("Set the latitude of the new template Settlement").build());	
		options.add(Option.builder(LONGITUDE_ARG).argName("longitude").hasArg().optionalArg(false)
				.desc("Set the longitude of the new template Settlement").build());	
		options.add(Option.builder(ALPHA_CREW_ARG).argName("true|false").hasArg().optionalArg(false)
				.desc("Enable or disable use of the Alpha crew").build());	
		options.add(Option.builder(DIAGNOSTICS_ARG).argName("<module>,<module>.....").hasArg().optionalArg(false)
				.desc("Enable diagnositics modules").build());	
		return options;
	}

	/**
	 * Parse the command line and process the core Simulation arguments
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
		if (line.hasOption(ALPHA_CREW_ARG)) {
			setUseAlphaCrew(Boolean.parseBoolean(line.getOptionValue(ALPHA_CREW_ARG)));
		}
		if (line.hasOption(DIAGNOSTICS_ARG)) {
			setDiagnostics(line.getOptionValue(DIAGNOSTICS_ARG));
		}		
	}

	/**
	 * Uses the previously defines options and start the required Simulation
	 */
	public void start() {
		Version version = java.lang.Runtime.version();
		logger.config("-----------------------------------------------------------");
		logger.config("    Java Version Full String = "+version);
		logger.config("Java Version Feature Element = "+version.feature());
		logger.config("Java Version Interim Element = "+version.interim());
		logger.config("  Java Patch Element Version = "+version.patch());
		logger.config(" Java Update Element Version = "+version.update());
		logger.config("          Java Version Build = "+version.build().get());
		logger.config("  Java additional build Info = "+version.optional().get());
		logger.config("       Java Pre-Release Info = "+version.pre().orElse("NA"));
		logger.config("-----------------------------------------------------------");
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
			
			SettlementBuilder builder = new SettlementBuilder(sim,
											simulationConfig);
			
			// If the alpha crew is enabled and no selected crew then it's the default
			if ((selectedCrew == null) && useAlphaCrew) {
				selectedCrew = new CrewConfig(CrewConfig.ALPHA_CREW_ID);
			}
			if (selectedCrew != null) {
				builder.setCrew(selectedCrew);
			}
			
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
			String sponsorCode = settlementTemplate.getSponsor();
			authority = ReportingAuthorityFactory.getAuthority(sponsorCode);
		}
		
		// Create a random name
		String settlementName = "New Settlement";
		List<String> settlementNames = settlementConfig.getSettlementNameList(authority.getCode());
		if (!settlementNames.isEmpty()) {
			int size = settlementNames.size();
			int rand = RandomUtil.getRandomInt(size-1);
			settlementName = settlementNames.get(rand);
		}
		
		logger.info("Starting a single Settlement sim using template "+ template
				+ " with settlement name = " + settlementName);
		return new InitialSettlement(settlementName, authority.getCode(), template, 
									 settlementTemplate.getDefaultPopulation(),
									 settlementTemplate.getDefaultNumOfRobots(),
									 new Coordinates(latitude, longitude));
	}

	/**
	 * Define a specific crew to be loaded
	 * @param crew
	 */
	public void setCrew(CrewConfig crew) {
		this.selectedCrew = crew;
	}

}
