package org.mars.sim.console.chat.simcommand;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;

public class LogCommand extends ChatCommand {
	public final static ChatCommand LOG = new LogCommand();
	
	private LogCommand() {
		super(TopLevel.SIMULATION_GROUP, "lo", "log", "Change the simulation logging");
	}

	@Override
	public void execute(Conversation context, String input) {
		context.println("Not implemented");
	}     
	
	/**
	 * Processes the log level configurations
	 * The oild log method. Needs reworking
	 * 
	 * @param text
	 * @param responseText
	 * @return
	 */
	/**
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
	**/
}
