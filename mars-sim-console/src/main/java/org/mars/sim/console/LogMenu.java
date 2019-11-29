/**
 * Mars Simulation Project
 * LogMenu.java
 * @version 3.1.0 2019-09-15
 * @author Manny Kung
 */
package org.mars.sim.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.swing.SwingTextTerminal;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.person.ai.mission.AreologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.BiologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.BuildingSalvageMission;
import org.mars_sim.msp.core.person.ai.mission.CollectIce;
import org.mars_sim.msp.core.person.ai.mission.CollectRegolith;
import org.mars_sim.msp.core.person.ai.mission.CollectResourcesMission;
import org.mars_sim.msp.core.person.ai.mission.EmergencySupply;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.MeteorologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.MissionPlanning;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.person.ai.mission.TradeUtil;
import org.mars_sim.msp.core.person.ai.mission.TravelMission;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.mission.meta.AreologyFieldStudyMeta;
import org.mars_sim.msp.core.person.ai.mission.meta.BiologyFieldStudyMeta;
import org.mars_sim.msp.core.person.ai.mission.meta.BuildingConstructionMissionMeta;
import org.mars_sim.msp.core.person.ai.mission.meta.BuildingSalvageMissionMeta;
import org.mars_sim.msp.core.person.ai.mission.meta.CollectIceMeta;
import org.mars_sim.msp.core.person.ai.mission.meta.CollectRegolithMeta;
import org.mars_sim.msp.core.person.ai.mission.meta.EmergencySupplyMissionMeta;
import org.mars_sim.msp.core.person.ai.mission.meta.ExplorationMeta;
import org.mars_sim.msp.core.person.ai.mission.meta.MeteorologyFieldStudyMeta;
import org.mars_sim.msp.core.person.ai.mission.meta.MiningMeta;
import org.mars_sim.msp.core.person.ai.mission.meta.RescueSalvageVehicleMeta;
import org.mars_sim.msp.core.person.ai.mission.meta.TradeMeta;
import org.mars_sim.msp.core.person.ai.mission.meta.TravelToSettlementMeta;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.EnterAirlock;
import org.mars_sim.msp.core.person.ai.task.ExitAirlock;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.person.ai.task.WalkOutside;
import org.mars_sim.msp.core.person.ai.task.WalkRoverInterior;
import org.mars_sim.msp.core.person.ai.task.WalkSettlementInterior;
import org.mars_sim.msp.core.person.ai.task.WalkingSteps;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.building.function.BuildingAirlock;
import org.mars_sim.msp.core.vehicle.VehicleAirlock;

/**
 * A menu for choosing the log setting in the console.
 */
public class LogMenu implements BiConsumer<TextIO, RunnerData> {
	  
	private static final Logger logger = Logger.getLogger(LogMenu.class.getName());

	private SwingTextTerminal terminal;
	
	private static TextIO textIO;
	
	static LogManager logManager = LogManager.getLogManager();
	
	static ConcurrentHashMap<String, Level> logLevels = new ConcurrentHashMap<>();

	private static List<String> keywords;

	private static List<String> classString;

	private static List<String> levelString;

	
	static {
		keywords = Arrays.asList(
				"help",
				"/h",
				"h",
				"quit",
				"/quit",
				"q", 
				"/q",
				"log",
				"log level",
				"log help",
				"log timestamp", 
				"log rate limit", 
				"log reset", 
				"log all", 
				"log fine", 
				"log info", 
				"log severe",
				"log finer", 
				"log finest", 
				"log warning", 
				"log config"
				);

		classString = Arrays.asList(
				"log all walk ", 
				"log all eva ",
				"log all mission ", 
				"log all airlock ");
		
		levelString = Arrays.asList(
				"off", 
				"severe",
				"warning",
				"info",
				"config",
				"fine",
				"finer",
				"finest",
				"all");
		
		
		List<String> list = new ArrayList<>(keywords);
		
		for (String ls : levelString) {
			for (String cs : classString) {
			list.add(cs + ls);
			}
		}
		
		keywords = list;
	}

//	public LogMenu() {}
	
    public static void main(String[] args) {
        textIO = TextIoFactory.getTextIO();
        new LogMenu().accept(textIO, null);
    }

    @Override
    public void accept(TextIO textIO, RunnerData runnerData) {
    	this.textIO = textIO;
    	terminal = (SwingTextTerminal)textIO.getTextTerminal();
        String initData = (runnerData == null) ? null : runnerData.getInitData();
        AppUtil.printGsonMessage(terminal, initData);
        
        Choice choice = new Choice();
        SwingHandler handler = new SwingHandler(textIO, "console", choice);

        terminal.printf(
        		"Type 'h', '/h', or 'help' for instructions. "
        		+ System.lineSeparator() 
        		+ "Type 'q', '/q' or 'quit' to go back to console menu." 
        		+ System.lineSeparator() + System.lineSeparator() );
        
        terminal.printf(processLogChange("log level").toString() + System.lineSeparator());
          	
	    String s = "";
	    
	    boolean quit = false;
	    
        while (!quit) {
            handler.addStringTask("choice", System.lineSeparator() + "Enter your log command: ", false)
        		.addChoices(keywords);//.constrainInputToChoices();
		    handler.executeOneTask();
	
		    s = Choice.choice;
		    
		    String result = "";
		    
			if (s.toLowerCase().contains("log") 
					|| s.toLowerCase().contains("help")
					|| s.toLowerCase().contains("/h")
					|| s.toLowerCase().contains("h")) {
				result = processLogChange(s).toString();
				
				if (result.trim().toLowerCase().equals(""))
					result = "Invalid log usage. Please double check." + System.lineSeparator();
				
				terminal.printf(System.lineSeparator() + result + System.lineSeparator());
			}	    	
		    
			else if (s.equalsIgnoreCase("/q") 
		    		|| s.equalsIgnoreCase("q")
		    		|| s.equalsIgnoreCase("/quit") 
		    		|| s.equalsIgnoreCase("quit")) {
				quit = true;
		    }
			
			else {
				terminal.printf(System.lineSeparator() 
						+ "Invalid log usage. Please double check."  
						+ System.lineSeparator());
			}
        }
    }

    
	/**
	 * Saves the log levels
	 * 
	 * @param key
	 * @param value
	 */
	public static void saveLogLevel(String key, Level value) {
		if (logLevels.isEmpty()) {
			logLevels.put(key, value);
		} else {
			if (logLevels.containsKey(key)) {
				logLevels.replace(key, value);
			} else {
				logLevels.put(key, value);
			}
		}
	}

	/**
	 * Sets the root logger's level
	 * 
	 * @param newLvl
	 */
	public static void setRootLogLevel(Level newLvl) {
		// Java 8 stream
//		Arrays.stream(LogManager.getLogManager().getLogger("").getHandlers()).forEach(h -> h.setLevel(newLvl));

		Logger rootLogger = LogManager.getLogManager().getLogger("");
		Handler[] handlers = rootLogger.getHandlers();
		rootLogger.setLevel(newLvl);
		for (Handler h : handlers) {
			if (h instanceof ConsoleHandler)
				h.setLevel(newLvl);
		}

		logger.config("Global logging is set to " + newLvl);
	}

	/**
	 * Changes the class logger logging level
	 * 
	 * @param clazz
	 * @param lvl
	 */
	public static void changeLogLevel(Class<?> clazz, Level lvl) {
		if (logManager.getLogger(clazz.getName()) != null)
			logManager.getLogger(clazz.getName()).setLevel(lvl);
	}

	/**
	 * Processes the log level configurations
	 * 
	 * @param text
	 * @param responseText
	 * @return
	 */
	public static StringBuffer processLogChange(String text) {
		StringBuffer responseText = new StringBuffer();
		
		if (text.equalsIgnoreCase("log level")) {

			Level level = LogManager.getLogManager().getLogger("").getLevel();

			responseText.append("The current global logging level is at " + level);
//			responseText.append(System.lineSeparator());
			if (!logLevels.isEmpty()) {
				for (String s : logLevels.keySet()) {
					responseText.append("The " + s + "-related logging level is at " + logLevels.get(s));
					responseText.append(System.lineSeparator());
				}
			}

			return responseText;
		}
		
		else if (text.equalsIgnoreCase("log reset")) {
			LogManager.getLogManager().reset();
			logLevels.clear();
			responseText.append("All logging levels have been reset back to the default.");
			responseText.append(System.lineSeparator());
		}

		else if (text.equalsIgnoreCase("help") || text.equalsIgnoreCase("log help")
				|| text.equalsIgnoreCase("/h") || text.equalsIgnoreCase("h")) {

			responseText.append("(A). There are 9 logging levels as follows : ");
			responseText.append(System.lineSeparator());
			responseText.append(" 1. log off     : turn off logging.");
			responseText.append(System.lineSeparator());
			responseText.append(" 2. log severe  : show serious failure.");
			responseText.append(System.lineSeparator());
			responseText.append(" 3. log warning : show potential problem.");
			responseText.append(System.lineSeparator());
			responseText.append(" 4. log info    : show informational messages.");
			responseText.append(System.lineSeparator());
			responseText.append(" 5. log config  : show static configuration messages.");
			responseText.append(System.lineSeparator());
			responseText.append(" 6. log fine    : show tracing information.");
			responseText.append(System.lineSeparator());
			responseText.append(" 7. log finer   : show fairly detailed tracing information.");
			responseText.append(System.lineSeparator());
			responseText.append(" 8. log finest  : show highly detailed tracing information.");
			responseText.append(System.lineSeparator());
			responseText.append(" 9. log all     : show all messages.");
			responseText.append(System.lineSeparator());
			
			responseText.append(System.lineSeparator());
			responseText.append("   e.g. Type 'log info' to set all loggers to INFO level");	
			responseText.append(System.lineSeparator());
			
			responseText.append(System.lineSeparator());
			responseText.append("(B). class-related log commands :");
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());
			responseText.append(" f. log <Class Name> <lvl> : set the logger of a class to a particular log level");
			responseText.append(System.lineSeparator());
			responseText.append(" g. log all airlock <lvl>    : set ALL airlock-related class to <lvl>");
			responseText.append(System.lineSeparator());
			responseText.append(" h. log all walk <lvl>       : set ALL walk-related class to <lvl>");
			responseText.append(System.lineSeparator());
			responseText.append(" i. log all eva <lvl>        : set ALL airlock-related class to <lvl>");
			responseText.append(System.lineSeparator());
			responseText.append(" j. log all mission <lvl>    : set ALL mission-related class to <lvl>");
			responseText.append(System.lineSeparator());
			
			responseText.append(System.lineSeparator());
			responseText.append("(C). Other log commands ");
			responseText.append(" a. log level                : see current log level");
			responseText.append(System.lineSeparator());
			responseText.append(" b. log timestamp            : change the logging timestamp for the simulation"); 
			responseText.append(System.lineSeparator());
			responseText.append(" c. log rate limit           : change the rate limit on the log statements"); 
			responseText.append(System.lineSeparator());
			responseText.append(" d. log reset                : reset the log level back to default");  
			responseText.append(System.lineSeparator());
			responseText.append(" e. log help, help, /h, or h : get instructions and log related keywords");
			responseText.append(System.lineSeparator());
			responseText.append(" f. quit, /q, or q           : Go back to the console menu.");
	        responseText.append(System.lineSeparator());

			return responseText;
		}

		else if (text.contains("log timestamp")) {

			int ans = LogConsolidated.getTimeStampType();
			String now = "";
			if (ans == 0)
				now = "Local Time";
			else if (ans == 1)
				now = "Future Earth Time";
			else if (ans == 2)
				now = "Future Martian Time";

			String prompt = "Currently, the log timestamp is " + now + "." + System.lineSeparator()
					+ "Would you like to change it?";

			boolean change = textIO.newBooleanInputReader().read(prompt);

			if (change) {

				String prompt1 = System.lineSeparator() + "(1) Local Time" + System.lineSeparator()
						+ "(2) Earth Simulation Time" + System.lineSeparator() + "(3) Mars Simulation Time"
						+ System.lineSeparator() + "Which timestamp do you want to use (1, 2 or 3)?";

				int choice = textIO.newIntInputReader().read(prompt1);

				String s = "";

				if (choice == 1) {
					LogConsolidated.setTimeStampChoice(0);
					s = "The timestamp has been updated to using Local Time.";
				} else if (choice == 2) {
					LogConsolidated.setTimeStampChoice(1);
					s = "The timestamp has been updated to using Earth Simulation Time.";
				} else if (choice == 3) {
					LogConsolidated.setTimeStampChoice(2);
					s = "The timestamp has been updated to using Mars Simulation Time.";
				} else {
					responseText.append("No change has been made.");
					responseText.append(System.lineSeparator());
					return responseText;
				}

				responseText.append(System.lineSeparator());
				responseText.append(s);
				logger.config(s);
			} else {
				responseText.append("No change has been made.");
				responseText.append(System.lineSeparator());
			}

			return responseText;
		}

		else if (text.contains("log rate limit")) {

			boolean ans = LogConsolidated.showRateLimit();
			String now = "";
			if (ans)
				now = "enabled";
			else
				now = "disabled";
			String prompt = "Currently, the log rate limit is " + now + "." + System.lineSeparator()
					+ "Would you like to change it?";

			boolean change = textIO.newBooleanInputReader().read(prompt);

			if (change) {
				String s = "";
				if (ans) {
					LogConsolidated.setRateLimit(false);
					s = "The log rate limit has been disabled.";
				} else {
					LogConsolidated.setRateLimit(true);
					s = "The log rate limit has been enabled.";
				}

				responseText.append(System.lineSeparator());
				responseText.append(s);
				logger.config(s);
			} else {
				responseText.append("No change has been made.");
				responseText.append(System.lineSeparator());
			}

			return responseText;
		}

		else if (text.contains("log all walk")) {
			Level lvl = null;
			if (text.trim().equalsIgnoreCase("log all airlock")) {
				responseText.append("Plese specify which log level.");
				logger.config("Plese specify which log level.");
			}
			else if (text.equalsIgnoreCase("log all walk all")) {
				lvl = Level.ALL;
			}

			else if (text.equalsIgnoreCase("log all walk finest")) {
				lvl = Level.FINEST;
			}

			else if (text.equalsIgnoreCase("log all walk finer")) {
				lvl = Level.FINER;
			}

			else if (text.equalsIgnoreCase("log all walk fine")) {
				lvl = Level.FINE;
			}

			else if (text.equalsIgnoreCase("log all walk config")) {
				lvl = Level.CONFIG;
			}

			else if (text.equalsIgnoreCase("log all walk info")) {
				lvl = Level.INFO;
			}

			else if (text.equalsIgnoreCase("log all walk warning")) {
				lvl = Level.WARNING;
			}

			else if (text.equalsIgnoreCase("log all walk severe")) {
				lvl = Level.SEVERE;
			}

			else if (text.equalsIgnoreCase("log all walk off")) {
				lvl = Level.OFF;
			}

			if (lvl != null) {
				changeLogLevel(Walk.class, lvl);
				changeLogLevel(WalkOutside.class, lvl);
				changeLogLevel(WalkingSteps.class, lvl);
				changeLogLevel(WalkRoverInterior.class, lvl);
				changeLogLevel(WalkSettlementInterior.class, lvl);
	
				saveLogLevel("all walk", lvl);
	
				responseText.append("Walk-related Loggers are set to " + lvl);
				logger.config("Walk-related Loggers are set to " + lvl);
			}
		}

		else if (text.contains("log all airlock")) {
			Level lvl = null;
			if (text.trim().equalsIgnoreCase("log all airlock")) {
				responseText.append("Plese specify which log level.");
				logger.config("Plese specify which log level.");
			}
			else if (text.equalsIgnoreCase("log all airlock all")) {
				lvl = Level.ALL;
			}

			else if (text.equalsIgnoreCase("log all airlock finest")) {
				lvl = Level.FINEST;
			}

			else if (text.equalsIgnoreCase("log all airlock finer")) {
				lvl = Level.FINER;
			}

			else if (text.equalsIgnoreCase("log all airlock fine")) {
				lvl = Level.FINE;
			}

			else if (text.equalsIgnoreCase("log all airlock config")) {
				lvl = Level.CONFIG;
			}

			else if (text.equalsIgnoreCase("log all airlock info")) {
				lvl = Level.INFO;
			}

			else if (text.equalsIgnoreCase("log all airlock warning")) {
				lvl = Level.WARNING;
			}

			else if (text.equalsIgnoreCase("log all airlock severe")) {
				lvl = Level.SEVERE;
			}

			else if (text.equalsIgnoreCase("log all airlock off")) {
				lvl = Level.OFF;
			}

			if (lvl != null) {
				changeLogLevel(EnterAirlock.class, lvl);
				changeLogLevel(ExitAirlock.class, lvl);
				changeLogLevel(Airlock.class, lvl);
				changeLogLevel(BuildingAirlock.class, lvl);
				changeLogLevel(VehicleAirlock.class, lvl);
	
				saveLogLevel("all airlock", lvl);
	
				responseText.append("Airlock-related Loggers are set to " + lvl);
				logger.config("Airlock-related Loggers are set to " + lvl);
			}
		}

		else if (text.contains("log all eva")) {
			Level lvl = null;
			if (text.trim().equalsIgnoreCase("log all airlock")) {
				responseText.append("Plese specify which log level.");
				logger.config("Plese specify which log level.");
			}
			else if (text.equalsIgnoreCase("log all eva all")) {
				lvl = Level.ALL;
			}

			else if (text.equalsIgnoreCase("log all eva finest")) {
				lvl = Level.FINEST;
			}

			else if (text.equalsIgnoreCase("log all eva finer")) {
				lvl = Level.FINER;
			}

			else if (text.equalsIgnoreCase("log all eva fine")) {
				lvl = Level.FINE;
			}

			else if (text.equalsIgnoreCase("log all eva config")) {
				lvl = Level.CONFIG;
			}

			else if (text.equalsIgnoreCase("log all eva info")) {
				lvl = Level.INFO;
			}

			else if (text.equalsIgnoreCase("log all eva warning")) {
				lvl = Level.WARNING;
			}

			else if (text.equalsIgnoreCase("log all eva severe")) {
				lvl = Level.SEVERE;
			}

			else if (text.equalsIgnoreCase("log all eva off")) {
				lvl = Level.OFF;
			}

			if (lvl != null) {
				changeLogLevel(EVAOperation.class, lvl);
	
				saveLogLevel("all eva", lvl);
	
				responseText.append("EVAOperation Logger is set to " + lvl);
				logger.config("EVAOperation Logger is set to " + lvl);
			}
		}

		else if (text.contains("log all mission")) {
			Level lvl = null;
			if (text.trim().equalsIgnoreCase("log all airlock")) {
				responseText.append("Plese specify which log level.");
				logger.config("Plese specify which log level.");
			}
			else if (text.equalsIgnoreCase("log all mission all")) {
				lvl = Level.ALL;
			}

			else if (text.equalsIgnoreCase("log all mission finest")) {
				lvl = Level.FINEST;
			}

			else if (text.equalsIgnoreCase("log all mission finer")) {
				lvl = Level.FINER;
			}

			else if (text.equalsIgnoreCase("log all mission fine")) {
				lvl = Level.FINE;
			}

			else if (text.equalsIgnoreCase("log all mission config")) {
				lvl = Level.CONFIG;
			}

			else if (text.equalsIgnoreCase("log all mission info")) {
				lvl = Level.INFO;
			}

			else if (text.equalsIgnoreCase("log all mission warning")) {
				lvl = Level.WARNING;
			}

			else if (text.equalsIgnoreCase("log all mission severe")) {
				lvl = Level.SEVERE;
			}

			else if (text.equalsIgnoreCase("log all mission off")) {
				lvl = Level.OFF;
			}

			if (lvl != null) {
				changeLogLevel(Mission.class, lvl);
				changeLogLevel(MissionManager.class, lvl);
				changeLogLevel(MissionPlanning.class, lvl);
				
				changeLogLevel(RescueSalvageVehicle.class, lvl);
				changeLogLevel(CollectResourcesMission.class, lvl);
				changeLogLevel(CollectIce.class, lvl);				
				changeLogLevel(CollectRegolith.class, lvl);	
				
				changeLogLevel(RescueSalvageVehicleMeta.class, lvl);
				changeLogLevel(CollectIceMeta.class, lvl);				
				changeLogLevel(CollectRegolithMeta.class, lvl);	
				
				changeLogLevel(Exploration.class, lvl);
				changeLogLevel(Mining.class, lvl);
				changeLogLevel(BuildingConstructionMission.class, lvl);
				changeLogLevel(BuildingSalvageMission.class, lvl);
				changeLogLevel(EmergencySupply.class, lvl);
				changeLogLevel(RescueSalvageVehicle.class, lvl);
				
				changeLogLevel(ExplorationMeta.class, lvl);
				changeLogLevel(MiningMeta.class, lvl);
				changeLogLevel(BuildingConstructionMissionMeta.class, lvl);
				changeLogLevel(BuildingSalvageMissionMeta.class, lvl);
				changeLogLevel(EmergencySupplyMissionMeta.class, lvl);
				changeLogLevel(RescueSalvageVehicleMeta.class, lvl);
				
				changeLogLevel(Trade.class, lvl);
				changeLogLevel(TradeUtil.class, lvl);
				
				changeLogLevel(TradeMeta.class, lvl);
				
				changeLogLevel(VehicleMission.class, lvl);
				changeLogLevel(RoverMission.class, lvl);
				changeLogLevel(TravelMission.class, lvl);
				
				changeLogLevel(TravelToSettlement.class, lvl);
				
				changeLogLevel(AreologyFieldStudy.class, lvl);
				changeLogLevel(BiologyFieldStudy.class, lvl);
				changeLogLevel(MeteorologyFieldStudy.class, lvl);
				
				changeLogLevel(TravelToSettlementMeta.class, lvl);
				
				changeLogLevel(AreologyFieldStudyMeta.class, lvl);
				changeLogLevel(BiologyFieldStudyMeta.class, lvl);
				changeLogLevel(MeteorologyFieldStudyMeta.class, lvl);
				
				saveLogLevel("all mission", lvl);
	
				responseText.append("Mission-related loggers are set to " + lvl);
				logger.config("Mission-related loggers are set to " + lvl);
			}
		}

		else if (text.equalsIgnoreCase("log off")) {
			setRootLogLevel(Level.OFF);
//				responseText.append(System.lineSeparator());
			responseText.append("Logging is set to OFF");
		}

		else if (text.equalsIgnoreCase("log config")) {
			setRootLogLevel(Level.CONFIG);
//				responseText.append(System.lineSeparator());
			responseText.append("Logging is set to CONFIG");
		}

		else if (text.equalsIgnoreCase("log warning")) {
			setRootLogLevel(Level.WARNING);
//				responseText.append(System.lineSeparator());
			responseText.append("Logging is set to WARNING");
		}

		else if (text.equalsIgnoreCase("log fine")) {
			setRootLogLevel(Level.FINE);
//				responseText.append(System.lineSeparator());
			responseText.append("Logging is set to FINE");
		}

		else if (text.equalsIgnoreCase("log finer")) {
			setRootLogLevel(Level.FINER);
//				responseText.append(System.lineSeparator());
			responseText.append("Logging is set to FINER");
		}

		else if (text.equalsIgnoreCase("log finest")) {
			setRootLogLevel(Level.FINEST);
//				responseText.append(System.lineSeparator());
			responseText.append("Logging is set to FINEST");
		}

		else if (text.equalsIgnoreCase("log severe")) {
			setRootLogLevel(Level.SEVERE);
//				responseText.append(System.lineSeparator());
			responseText.append("Logging is set to SEVERE");
		}

		else if (text.equalsIgnoreCase("log info")) {
			setRootLogLevel(Level.INFO);
//				responseText.append(System.lineSeparator());
			responseText.append("Logging is set to INFO");
		}

		else if (text.equalsIgnoreCase("log all")) {
			setRootLogLevel(Level.ALL);
//				responseText.append(System.lineSeparator());
			responseText.append("Logging is set to ALL");
		}

		else {
			responseText.append("Invalid log usage. Please double check.");
//			logger.config("Invalid log usage. Please double check.");
//			responseText.append(System.lineSeparator());
		}

		return responseText;
	}
	
    @Override
    public String toString() {
        return "Set Logging Options";
    }
    
    private static class Choice {
        public static String choice;

//		OFF
//		SEVERE (highest value)
//		WARNING
//		INFO
//		CONFIG
//		FINE
//		FINER
//		FINEST (lowest value)
//		ALL			
        
        @Override
        public String toString() {
        	StringBuffer responseText = new StringBuffer();
			responseText.append(System.lineSeparator());
			responseText.append("Type 'log help' for keywords and instructions");
        	    	
            return responseText.toString();
        }
    }
}
