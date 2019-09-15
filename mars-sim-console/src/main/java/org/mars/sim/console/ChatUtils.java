/**
 * Mars Simulation Project
 * ChatUtils.java
 * @version 3.1.0 2018-09-30
 * @author Manny Kung
 */

package org.mars.sim.console;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.beryx.textio.TextIO;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.mars.OrbitInfo;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.mars.Weather;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.mission.AreologyStudyFieldMission;
import org.mars_sim.msp.core.person.ai.mission.BiologyStudyFieldMission;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.CollectResourcesMission;
import org.mars_sim.msp.core.person.ai.mission.EmergencySupplyMission;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.person.ai.mission.TravelMission;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.EnterAirlock;
import org.mars_sim.msp.core.person.ai.task.ExitAirlock;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.person.ai.task.WalkOutside;
import org.mars_sim.msp.core.person.ai.task.WalkRoverInterior;
import org.mars_sim.msp.core.person.ai.task.WalkSettlementInterior;
import org.mars_sim.msp.core.person.ai.task.WalkingSteps;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.function.BuildingAirlock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleAirlock;

public class ChatUtils {

	/** DEFAULT LOGGER. */
	private static Logger logger = Logger.getLogger(ChatUtils.class.getName());
//	private static String loggerName = logger.getName();
//	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());

	static boolean expertMode = false;

	public static final double RADIANS_TO_DEGREES = 180D / Math.PI;

	public final static String SYSTEM = "System";
	public final static String SYSTEM_PROMPT = "System : ";
	public final static String YOU_PROMPT = "You : ";
	public final static String REQUEST_HEIGHT_CHANGE = YOU_PROMPT + "I'd like to change the chat box height to ";
	public final static String REQUEST_HELP = YOU_PROMPT + "I need some help! What are the available commands ?";

	public final static String REQUEST_KEYS = YOU_PROMPT
			+ "I need a list of the keywords. Would you tell me what they are ?";

	public final static String[] SPECIAL_KEYS = { "key", "keys", "keyword", "keywords", "/k", "help", "/h", "/?", "?",
			"/y1", "/y2", "/y3", "/y4", "hello", "hi", "hey", "expert", "/e", "quit", "/q", "bye", "/b", "exit", "/x",
			"pause", "/p" };

	public final static String[] VEHICLE_KEYS = new String[] { "specs", "status" };

	public final static String[] SETTLEMENT_KEYS = new String[] { "weather", "people", "settler", "person", "robot",
			"bot", "proposal", "vehicle range", "dash", "dashboard", "repair", "maintenance", "evasuit", "eva suit",
			"mission plan", "mission now", "objective", "water", "o2", "oxygen", "co2", "carbon dioxide", "job roster",
			"job demand", "job prospect", "bed", "social", "science" };

	public final static String[] PERSON_KEYS = new String[] { "feeling", "status", "skill", "attribute", "birth", "age",
			"how old", "born", "friend", "country", "nationality", "space agency", "sponsor", "specialty", "job",
			"outside", "inside", "container", "building", "associated", "association", "home", "home town", "garage",
			"vehicle top container", "vehicle container", "vehicle park", "vehicle settlement", "vehicle outside",
			"vehicle inside", "bed time", "sleep hour", "eva time", "airlock time", "shift", "work shift", "job score",
			"bed", "social" };

	public final static String[] ALL_PARTIES_KEYS = new String[] { "time", "date", "where", "location", "located",
			"role", "task", "mission", "sponsor" };

	public final static String[] SYSTEM_KEYS = new String[] { "settlement", "check size", "log", "log help",
			"log timestamp", "log rate limit", "log reset", "log all", "log fine", "log info", "log severe",
			"log finer", "log finest", "log warning", "log config", "log all walk off", "log all eva off",
			"log all mission off", "log all airlock off", "vehicle", "rover", "hi", "hello", "hey", "proposal",
			"reset clock thread", "reset clock pulse", "reset clock listener", "social", "science", "scores" };

	public final static String SWITCHES = "  Type 'bye', '/b', 'exit', '/x', 'quit', '/q' to leave the chat."
			+ System.lineSeparator() + "  Type 'help', '/h', '/?' for this help page." + System.lineSeparator()
			+ "  Type 'expert', '/e' to toggle between normal and expert mode." + System.lineSeparator()
			+ "  Type 'pause', '/p' to pause and unpause the simulation." + System.lineSeparator();

	public final static String HELP_TEXT = System.lineSeparator()
			+ "    ------------------------- H E L P ------------------------- " + System.lineSeparator()
			+ "  Type in the NAME of a person, bot, vehicle or settlement to connect with." + System.lineSeparator()
//			+ "  Use KEYWORDS or type in a number between 0 and 18 (specific QUESTIONS on a party)." + System.lineSeparator() 
			+ "  Type '/k' or 'key' to see a list of KEYWORDS." + System.lineSeparator()
			+ "  Type 'settlement' to obtain the NAMES of the established settlements." + System.lineSeparator()
			+ SWITCHES;

	public final static String HELP_HEIGHT = "  Type 'y_' to change the chat box height; '/y1'-> 256 pixels (default) '/y2'->512 pixels, '/y3'->768 pixels, '/y4'->1024 pixels"
			+ System.lineSeparator();

//	public final static String KEYWORDS_TEXT = System.lineSeparator()
//			+ "    ------------------------- K E Y W O R D S ------------------------- " + System.lineSeparator()
//			+ "(1)       In MarsNet : a settlement/bot/person's name OR " + getKeywordList(SYSTEM_KEYS) + System.lineSeparator() 
//			+ "(2) For a Settlement : " + getKeywordList(SETTLEMENT_KEYS) + System.lineSeparator() 
//			+ "(3)    For a Settler : " + getKeywordList(PERSON_KEYS) + System.lineSeparator() 
//			+ "(4)  For all Parties : " + getKeywordList(ALL_PARTIES_KEYS) + System.lineSeparator()
//			+ "(5) 0 to 18 are specific QUESTIONS on a person/bot/vehicle/settlement" + System.lineSeparator();
////			+ "    --------------------------  M I S C S -------------------------- " + System.lineSeparator() 
////			+ SWITCHES;

	public final static String SYSTEM_KEYWORDS = System.lineSeparator()
			+ "    ------------------------- K E Y W O R D S ------------------------- " + System.lineSeparator()
			+ "(A). For MarsNet : "
			+ "Type in the NAME of a person, bot, vehicle or settlement to connect with OR keywords below : "
			+ System.lineSeparator() + System.lineSeparator() + getKeywordList(SYSTEM_KEYS) + System.lineSeparator();
//			+ "(B). For all Parties : "  + System.lineSeparator()
//			+ getKeywordList(ALL_PARTIES_KEYS) + System.lineSeparator();
//			+ " 0 to 18 are specific QUESTIONS on a person/bot/vehicle/settlement" + System.lineSeparator();
//			+ "    --------------------------  M I S C S -------------------------- " + System.lineSeparator() 
//			+ SWITCHES;

	public final static String VEHICLE_KEYWORDS = System.lineSeparator()
			+ "    ------------------------- K E Y W O R D S ------------------------- " + System.lineSeparator()
			+ "(A). For Vehicles : " + System.lineSeparator() + getKeywordList(VEHICLE_KEYS) + System.lineSeparator() + System.lineSeparator()
			+ "(B). For all Parties : " + System.lineSeparator() + getKeywordList(ALL_PARTIES_KEYS)
			+ System.lineSeparator();
//			+ "(2) 0 to 18 are specific QUESTIONS on a person/bot/vehicle/settlement" + System.lineSeparator();
//			+ "    --------------------------  M I S C S -------------------------- " + System.lineSeparator() 
//			+ SWITCHES;

	public final static String PERSON_KEYWORDS = System.lineSeparator()
			+ "    ------------------------- K E Y W O R D S ------------------------- " + System.lineSeparator()
			+ "(A). For Settlers : " + System.lineSeparator() + getKeywordList(PERSON_KEYS) + System.lineSeparator() + System.lineSeparator()
			+ "(B). For all Parties : " + System.lineSeparator() + getKeywordList(ALL_PARTIES_KEYS)
			+ System.lineSeparator();
//			+ "(2) 0 to 18 are specific QUESTIONS on a person/bot/vehicle/settlement" + System.lineSeparator();
//			+ "    --------------------------  M I S C S -------------------------- " + System.lineSeparator() 
//			+ SWITCHES;

	public final static String SETTLEMENT_KEYWORDS = System.lineSeparator()
			+ "    ------------------------- K E Y W O R D S ------------------------- " + System.lineSeparator()
			+ "(A). For Settlements : " + System.lineSeparator() + getKeywordList(SETTLEMENT_KEYS)
			+ System.lineSeparator() + System.lineSeparator()
			+ "(B). For all Parties : " + System.lineSeparator()
			+ getKeywordList(ALL_PARTIES_KEYS) + System.lineSeparator();
//			+ "(4)  For all Parties : " + getKeywordList(ALL_PARTIES_KEYS) + System.lineSeparator()
//			+ "(5) 0 to 18 are specific QUESTIONS on a person/bot/vehicle/settlement" + System.lineSeparator() 
//			+ "    --------------------------  M I S C S -------------------------- " + System.lineSeparator() 
//			+ SWITCHES;

	public final static String KEYWORDS_HEIGHT = HELP_HEIGHT; // "(8) '/y1' to reset height to 256 pixels (by default)
																// after closing chat box. '/y2'->512 pixels, '/y3'->768
																// pixels, '/y4'->1024 pixels" + System.lineSeparator();

	public final static String DASHES_0 = " ----------------------------------------------------";
	public final static String DASHES = " ----------------------------------------- ";
	public final static String DASHES_1 = "----------";
	public final static String ONE_SPACE = " ";

	public static String helpText;

	public static String keywordText;

	/**
	 * The mode of connection. -1 if none, 0 if headless, 1 if gui
	 */
	public static int connectionMode = -1;

	public static Person personCache;
	public static Robot robotCache;
	public static Settlement settlementCache;
	public static Unit unitCache;
	public static Vehicle vehicleCache;

//	public static Settlement settlement;
//	public static Building building;
//	public static Equipment equipment;

	static Simulation sim = Simulation.instance();
	static Weather weather;
	static SurfaceFeatures surfaceFeatures;
	static Mars mars;
	static MasterClock masterClock;
	static MarsClock marsClock;
	static OrbitInfo orbitInfo;
	static RelationshipManager relationshipManager;
	static ScientificStudyManager scientificManager;
	static SkillManager skillManager;
	static LogManager logManager;
	static UnitManager unitManager;
	static MissionManager missionManager;

	static DecimalFormat fmt = new DecimalFormat("##0");
	static DecimalFormat fmt1 = new DecimalFormat("#0.0");
	static DecimalFormat fmt2 = new DecimalFormat("#0.00");

	static ConcurrentHashMap<String, Level> logLevels = new ConcurrentHashMap<>();

	static TextIO textIO = InteractiveTerm.getTextIO();

	public ChatUtils() {
		masterClock = sim.getMasterClock();
		if (masterClock != null)
			marsClock = masterClock.getMarsClock();
		mars = sim.getMars();
		weather = mars.getWeather();
		surfaceFeatures = mars.getSurfaceFeatures();
		orbitInfo = mars.getOrbitInfo();

		relationshipManager = sim.getRelationshipManager();
		scientificManager = sim.getScientificStudyManager();
		logManager = LogManager.getLogManager();
		unitManager = sim.getUnitManager();
		missionManager = sim.getMissionManager();
	}

	/**
	 * Returns a list of keywords
	 * 
	 * @param keywords
	 * @return list
	 */
	public static String getKeywordList(String[] keywords) {
		String text = "";
		int last = keywords.length;
		if (last == 1)
			return keywords[0];

		Arrays.sort(keywords);

		for (int i = 0; i < last; i++) {
			if (i == last - 1)
				text = text + "and " + keywords[i] + ".";
			else
				text = text + keywords[i] + ", ";
		}
		return text;
	}

	/**
	 * Asks for clarification
	 * 
	 * @param prompt
	 * @return a string array
	 */
	public static String[] clarify(String prompt) {
		String questionText = YOU_PROMPT + "You were mumbling something about....";
		String responseText = null;
		int rand0 = RandomUtil.getRandomInt(4);
		if (rand0 == 0)
			responseText = prompt + " : Could you repeat that?   [/h for help]";
		else if (rand0 == 1)
			responseText = prompt + " : Pardon me?   [/h for help]";
		else if (rand0 == 2)
			responseText = prompt + " : What did you say?   [/h for help]";
		else if (rand0 == 3)
			responseText = prompt + " : I beg your pardon?   [/h for help]";
		else
			responseText = prompt + " : Can you be more specific?   [/h for help]";

		return new String[] { questionText, responseText + System.lineSeparator() };
	}

	/**
	 * Assembles a farewell phrase
	 * 
	 * @param respondent
	 * @return a string array
	 */
	public static String[] farewell(String respondent, boolean isHuman) {
		String questionText = YOU_PROMPT + farewellText(true);// + System.lineSeparator();
		String responseText = respondent + " : " + farewellText(isHuman);// + System.lineSeparator();
		return new String[] { questionText, responseText };
	}

	/**
	 * Returns a farewell phrase
	 * 
	 * @return a string
	 */
	public static String farewellText(boolean isHuman) {

		if (isHuman) {
			int r0 = RandomUtil.getRandomInt(7);
			if (r0 == 0)
				return "Bye !";
			else if (r0 == 1)
				return "Farewell !";
			else if (r0 == 2)
				return "Next time !";
			else if (r0 == 3)
				return "Have a nice sol !";
			else if (r0 == 4)
				return "Take it easy !";
			else if (r0 == 5)
				return "Take care !";
			else if (r0 == 6)
				return "Hang in there !";
			else
				return "I have to leave. Bye !";
		}

		else {
			int r0 = RandomUtil.getRandomInt(2);
			if (r0 == 0)
				return "Bye !";
			else if (r0 == 1)
				return "Farewell !";
			else
				return "Goodbye !";
		}
	}

	/*
	 * Checks if the user is toggling the expert mode
	 * 
	 * @param text
	 */
	public static boolean checkExpertMode(String text) {
		if (text.equalsIgnoreCase("expert") || text.equalsIgnoreCase("/e")) {
			return true;
		}
		return false;
	}

	/*
	 * Checks if the user wants to quit chatting
	 * 
	 * @param text
	 */
	public static boolean isQuitting(String text) {
		if (text.equalsIgnoreCase("quit") || text.equalsIgnoreCase("/quit") || text.equalsIgnoreCase("/q")
				|| text.equalsIgnoreCase("exit") || text.equalsIgnoreCase("/exit") || text.equalsIgnoreCase("/x")
				|| text.equalsIgnoreCase("bye") || text.equalsIgnoreCase("/bye") || text.equalsIgnoreCase("/b")) {
			return true;
		}

		else
			return false;
	}

	/**
	 * Check if the input string is integer
	 * 
	 * @param s
	 * @param radix
	 * @return true if the input is an integer
	 */
	public static boolean isInteger(String s, int radix) {
		if (s.isEmpty())
			return false;
		for (int i = 0; i < s.length(); i++) {
			if (i == 0 && s.charAt(i) == '-') {
				if (s.length() == 1)
					return false;
				else
					continue;
			}
			if (Character.digit(s.charAt(i), radix) < 0)
				return false;
		}
		return true;
	}


	/**
	 * Prints the mission sol and Mars and Earth's date and time
	 * 
	 * @return StringBuffer
	 */
	public static StringBuffer printTime() {

		StringBuffer responseText = new StringBuffer();
		// Mars/Earth Date and Time
//		String earthDateTime = masterClock.getEarthClock().getTimeStampF2();
		if (masterClock == null)
			masterClock = sim.getMasterClock();
		String earthDate = masterClock.getEarthClock().getDateStringF3();
		String earthTime = masterClock.getEarthClock().getTimeStringF0();
		int missionSol = marsClock.getMissionSol();
//		String marsDateTime = marsClock.getDateTimeStamp();
		String marsDate = marsClock.getDateString();
		String marsTime = marsClock.getDecimalTimeString();

		responseText.append(System.lineSeparator());

		String s0 = "Mission Sol : ";
		int num = 20 - s0.length();
		for (int i = 0; i < num; i++) {
			responseText.append(" ");
		}
		responseText.append(s0);
		responseText.append(missionSol);
		responseText.append(System.lineSeparator());
		responseText.append(System.lineSeparator());

		String s1 = "Mars Date : ";
		num = 20 - s1.length();
		for (int i = 0; i < num; i++) {
			responseText.append(" ");
		}
		responseText.append(s1);
		responseText.append(marsDate);
		responseText.append(System.lineSeparator());

		String s2 = "Mars Time : ";
		num = 20 - s2.length();
		for (int i = 0; i < num; i++) {
			responseText.append(" ");
		}
		responseText.append(s2);
		responseText.append(marsTime);
		responseText.append(System.lineSeparator());
		responseText.append(System.lineSeparator());

		String s3 = "Earth Date : ";
		num = 20 - s3.length();
		for (int i = 0; i < num; i++) {
			responseText.append(" ");
		}
		responseText.append(s3);
		responseText.append(earthDate);
		responseText.append(System.lineSeparator());

		String s4 = "Earth Time : ";
		num = 20 - s4.length();
		for (int i = 0; i < num; i++) {
			responseText.append(" ");
		}
		responseText.append(s4);
		responseText.append(earthTime);
		responseText.append(System.lineSeparator());

		return responseText;
	}

	public static StringBuffer getNextNum(int num) {
//		StringBuffer s = new StringBuffer();
//		if (num < 10)
//			return s.append("   " + num);
//		else
//			return s.append("  " + num);	
		return new StringBuffer();
	}



//	public static <T extends Comparable<? super T>> void customSort(List<T> list) {
//		Object[] a = list.toArray();
//	    Arrays.sort(a);
//	    ListIterator<T> i = list.listIterator();
//	    for (int j=0; j<a.length; j++) {
//	    	i.next();
//	        i.set((T)a[j]);
//	    }
//	}

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
	 * Computes the # of whitespaces
	 * 
	 * @param name
	 * @param max
	 * @return
	 */
	public static StringBuffer computeWhiteSpaces(String name, int max) {
		int size = name.length();
		int num = 1;
		if (max - size > 1)
			num = max - size ;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < num; i++)
			sb.append(ONE_SPACE);

		return sb;
	}

	/**
	 * Computes and add the # of whitespaces and the right-justified text
	 * 
	 * @param name
	 * @param max
	 * @return
	 */
	public static StringBuffer addhiteSpacesRightName(String name, int max) {
		int size = name.length();
		StringBuffer sb = new StringBuffer();
		int num = 1;
		if (max - size > 1)
			num = max - size ;
		for (int i = 0; i < num; i++)
			sb.append(ONE_SPACE);

		sb.append(name);
		return sb;
	}

	/**
	 * Add the left-justified text and computes/add the # of whitespaces 
	 * 
	 * @param name
	 * @param max
	 * @return
	 */
	public static StringBuffer addhiteSpacesLeftName(String name, int max) {
		int size = name.length();
		int num = 1;
		if (max - size > 1)
			num = max - size ;
		StringBuffer sb = new StringBuffer();
		sb.append(ONE_SPACE).append(name);
		for (int i = 0; i < num; i++)
			sb.append(ONE_SPACE);
		return sb;
	}
	
	/**
	 * Add the name and the # of whitespaces 
	 * 
	 * @param name
	 * @param max
	 * @return
	 */
	public static StringBuffer addNameFirstWhiteSpaces(String name, int max) {
		int size = name.length();
		int num = Math.min(max - size, 1);
		StringBuffer sb = new StringBuffer();
		sb.append(name);
		for (int i = 0; i < num; i++)
			sb.append(ONE_SPACE);
		return sb;
	}

	/**
	 * Gets the first available key of a map given its value
	 * 
	 * @param map
	 * @param value
	 * @return
	 */
	public static <K, V> K getKey(Map<K, V> map, V value) {
		for (K key : map.keySet()) {
			if (value.equals(map.get(key))) {
				return key;
			}
		}
		return null;
//		return map.entrySet()
//				       .stream()
//				       .filter(entry -> value.equals(entry.getValue()))
//				       .map(Map.Entry::getKey)
//				       .findFirst().get();
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
	public static StringBuffer processLogChange(String text, StringBuffer responseText) {

//		String[] cmds = text.split("\\s+");
//		System.out.println(cmds[0].toString());
//		System.out.println(cmds[1].toString());
//		System.out.println(cmds[2].toString());			
		if (text.equalsIgnoreCase("log reset")) {
			LogManager.getLogManager().reset();
			logLevels.clear();
			responseText.append("All logging levels have been reset back to the default.");
			responseText.append(System.lineSeparator());
		}

		else if (text.equalsIgnoreCase("log help")) {

			responseText.append("Please specify the Logging Level as follows : ");
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

//			responseText.append(System.lineSeparator());				
//			responseText.append(System.lineSeparator());
//			responseText.append("See https://docs.oracle.com/javase/8/docs/api/java/util/logging/Level.html");

			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());
			responseText.append("e.g. Type 'log info' to set all loggers to INFO level");
			responseText.append(System.lineSeparator());
			responseText.append("e.g. Type 'log <Class Name> <lvl>' to set the logger of a class to a level");
			responseText.append(System.lineSeparator());
			responseText.append("e.g. Type 'log all airlock off' to set ALL airlock-related class to OFF level");
			responseText.append(System.lineSeparator());
			responseText.append("e.g. Type 'log all walk off' to set ALL walk-related class to OFF level");

			return responseText;
		}

		else if (text.equalsIgnoreCase("log")) {

			Level level = LogManager.getLogManager().getLogger("").getLevel();

			responseText.append("Global logging level : " + level);
			responseText.append(System.lineSeparator());
			if (!logLevels.isEmpty()) {
				for (String s : logLevels.keySet()) {
					responseText.append("" + Conversion.capitalize(s) + "-related logging level : " + logLevels.get(s));
					responseText.append(System.lineSeparator());
				}
			}

			responseText.append(System.lineSeparator());
			responseText.append("For instructions, type 'log help'");
			responseText.append(System.lineSeparator());

//				OFF
//				SEVERE (highest value)
//				WARNING
//				INFO
//				CONFIG
//				FINE
//				FINER
//				FINEST (lowest value)
//				ALL			

//				responseText.append(System.lineSeparator());

//			responseText.append(System.lineSeparator());	

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
			Level lvl = Level.OFF;
			if (text.equalsIgnoreCase("log all walk off")) {
				lvl = Level.OFF;
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

			changeLogLevel(Walk.class, lvl);
			changeLogLevel(WalkOutside.class, lvl);
			changeLogLevel(WalkingSteps.class, lvl);
			changeLogLevel(WalkRoverInterior.class, lvl);
			changeLogLevel(WalkSettlementInterior.class, lvl);

			saveLogLevel("all walk", lvl);

			responseText.append("Walk-related Loggers are set to " + lvl);
			logger.config("Walk-related Loggers are set to " + lvl);
		}

		else if (text.contains("log all airlock")) {
			Level lvl = Level.OFF;
			if (text.equalsIgnoreCase("log all airlock off")) {
				lvl = Level.OFF;
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

			changeLogLevel(EnterAirlock.class, lvl);
			changeLogLevel(ExitAirlock.class, lvl);
			changeLogLevel(Airlock.class, lvl);
			changeLogLevel(BuildingAirlock.class, lvl);
			changeLogLevel(VehicleAirlock.class, lvl);

			saveLogLevel("all airlock", lvl);

			responseText.append("Airlock-related Loggers are set to " + lvl);
			logger.config("Airlock-related Loggers are set to " + lvl);
		}

		else if (text.contains("log all eva")) {
			Level lvl = Level.OFF;
			if (text.equalsIgnoreCase("log all eva off")) {
				lvl = Level.OFF;
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

			changeLogLevel(EVAOperation.class, lvl);

			saveLogLevel("all eva", lvl);

			responseText.append("EVAOperation Logger is set to " + lvl);
			logger.config("EVAOperation Logger is set to " + lvl);
		}

		else if (text.contains("log all mission")) {
			Level lvl = Level.OFF;
			if (text.equalsIgnoreCase("log all mission off")) {
				lvl = Level.OFF;
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

			changeLogLevel(Mission.class, lvl);
			changeLogLevel(VehicleMission.class, lvl);
			changeLogLevel(RoverMission.class, lvl);
			changeLogLevel(MissionManager.class, lvl);
			changeLogLevel(RescueSalvageVehicle.class, lvl);
			changeLogLevel(CollectResourcesMission.class, lvl);

			changeLogLevel(Exploration.class, lvl);
			changeLogLevel(Mining.class, lvl);
			changeLogLevel(BuildingConstructionMission.class, lvl);
			changeLogLevel(EmergencySupplyMission.class, lvl);

			changeLogLevel(Trade.class, lvl);
			changeLogLevel(TravelMission.class, lvl);
			changeLogLevel(TravelToSettlement.class, lvl);
			changeLogLevel(AreologyStudyFieldMission.class, lvl);
			changeLogLevel(BiologyStudyFieldMission.class, lvl);

			saveLogLevel("all mission", lvl);

			responseText.append("Mission-related loggers are set to " + lvl);
			logger.config("Mission-related loggers are set to " + lvl);
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
			logger.config("Invalid log usage. Please double check.");
//			responseText.append(System.lineSeparator());
		}
//		
//		else if (cmds[0].equals("log")) {// && !cmds[1].equals("all") && cmds[2] != null) {
//			boolean isGood = false;
//			
//			Level lvl = Level.OFF;
//			
//			if (cmds[2].equalsIgnoreCase("all")) {
//				lvl = Level.ALL;
//			}	
//			
//			else if (cmds[2].equalsIgnoreCase("finest")) {
//				lvl = Level.FINEST;			
//			}				
//
//			else if (cmds[2].equalsIgnoreCase("finer")) {
//				lvl = Level.FINER;
//			}
//			
//			else if (cmds[2].equalsIgnoreCase("fine")) {
//				lvl = Level.FINE;	
//			}
//			
//			else if (cmds[2].equalsIgnoreCase("config")) {
//				lvl = Level.CONFIG;	
//			}
//			
//			else if (cmds[2].equalsIgnoreCase("info")) {
//				lvl = Level.INFO;	
//			}
//			
//			else if (cmds[2].equalsIgnoreCase("warning")) {
//				lvl = Level.WARNING;	
//			}
//			
//			else if (cmds[2].equalsIgnoreCase("severe")) {
//				lvl = Level.SEVERE;	
//			}
//			
//			else if (cmds[2].equalsIgnoreCase("off")) {
//				lvl = Level.OFF;		
//			}
//			
//			else {
//				responseText.append("No such log level. Please double check.");
//				logger.config("No such log level. Please double check.");
//				responseText.append(System.lineSeparator());
//				isGood = false;
//			}
//			
//			if (isGood) {
//				try {
//					Class<?> classType = Class.forName(cmds[1]);
////						ClassLoader cl = null;
////						Class<?> classType = cl.loadClass(cmds[1]);
////						isGood = true;
//					if (LogManager.getLogManager().getLogger(classType.getName()) == null) {
//						responseText.append("No such class exists. Please use exact upper/lowercase letters in class name.");
//						logger.config("No such class exists. Please use exact upper/lowercase letters in class name.");
//						responseText.append(System.lineSeparator());
//					}
//					else {
//						LogManager.getLogManager().getLogger(classType.getName()).setLevel(lvl);		
//						responseText.append(cmds[1] + " logger is set to " + lvl);
//						logger.config(cmds[1] + " logger is set to " + lvl);
////							isGood = true;
//					}
//				} catch (ClassNotFoundException e) {
//					responseText.append("No such class exists. Please use exact upper/lowercase letters in class name.");
//					logger.config("No such class exists. Please use exact upper/lowercase letters in class name.");
//					responseText.append(System.lineSeparator());
////						isGood = false;
//					//e.printStackTrace();
//				}
//			}
//		}

		return responseText;
	}

//	public static boolean hasDuplicateNames(int[] names) {
//		 int size = names.length;
//		
//		 for (int i=0; i<size; i++){
//			 for (int j=0; j<size; j++){
//				 if (names[i] > 1 && names[j] > 1)
//					 return true;
//			 }
//		 }
//
//		 return false;
//	}

	/**
	 * Gets a greeting
	 * 
	 * @param type
	 * @return
	 */
	public static StringBuffer getGreeting(int type) {
		StringBuffer s = new StringBuffer();

		if (type == 1) {
			// For a human being
			int num = RandomUtil.getRandomInt(7);
			if (num == 0)
				s.append("How may I help you?");
			else if (num == 1)
				s.append("How can I help you?");
			else if (num == 2)
				s.append("How can I help?");
			else if (num == 3)
				s.append("How may I assist you?");
			else if (num == 4)
				s.append("Hi, do you need something? ");
			else if (num == 5)
				s.append("How are you, is there anything you need?");
			else if (num == 6)
				s.append("Howdy? anything you meed from me?");
			else
				s.append("Hi, do let me know what you need.");

		} else {
			// for machine AI
			int num = RandomUtil.getRandomInt(5);
			if (num == 0)
				s.append("How may I help you?");
			else if (num == 1)
				s.append("How can I help you?");
			else if (num == 2)
				s.append("How can I help?");
			else if (num == 3)
				s.append("How may I assist you?");
			else if (num == 4)
				s.append("How can I help?");
			else
				s.append("How can I help?");
		}

		return s;
	}

	/**
	 * Generates and prints the list that needs to be processed
	 * 
	 * @param indoorP
	 * @return String
	 */
	public static StringBuffer printList(List<?> list0) {
		StringBuffer sb = new StringBuffer();

		if (list0.isEmpty()) {
			sb.append("    None");
			sb.append(System.lineSeparator());
			return sb;
		}

		List<String> list = new ArrayList<>();
		for (Object o : list0) {
			list.add(o.toString());
		}

		StringBuffer s = new StringBuffer();
		int SPACES = 22;
		// int row = 0;
		for (int i = 0; i < list.size(); i++) {
			int column = 0;

			String c = "";
			int num = 0;

			// Find out what column
			if ((i - 1) % 3 == 0)
				column = 1;
			else if ((i - 2) % 3 == 0)
				column = 2;

			// Look at how many whitespaces needed before printing each column
			if (column == 0) {
				c = list.get(i).toString();
				num = SPACES - c.length();

			}

			else if (column == 1 || column == 2) {
				c = list.get(i).toString();
				num = SPACES - list.get(i - 1).toString().length();

				// Handle the extra space before the parenthesis
				for (int j = 0; j < num; j++) {
					s.append(" ");
				}
			}

			if (i + 1 < 10)
				s.append(" ");
			s.append("(");
			s.append(i + 1);
			s.append("). ");
			s.append(c);

			// if this is the last column
			if (column == 2 || i == list.size() - 1) {
				sb.append(s);
				sb.append(System.lineSeparator());
				s = new StringBuffer();
			}
		}

		return sb;
	}

	public static void setConnectionMode(int value) {
		connectionMode = value;
	}

	public static int getConnectionMode() {
		return connectionMode;
	}

	public static boolean isExpertMode() {
		return expertMode;
	}

	public static void toggleExpertMode() {
		expertMode = !expertMode;
	}

//	public static void setExpertMode(boolean value) {
//		expertMode = value;
//	}

//    private static class Input {
//        public static String change;
//        public static int range ;
//
//        @Override
//        public String toString() {
//            return System.lineSeparator() +">" + range;
//        }
//    }

	/**
	 * Compiles the names of settlements, people, robots and vehicles into one
	 * single list
	 * 
	 * @return a list of string
	 */
	public static List<String> createAutoCompleteKeywords() {
		// Creates a list of proper nouns
		List<String> list = createProperNounsList();

		// Add keywords specifically for MarsNet chat system
		list.addAll(Arrays.asList(ChatUtils.SYSTEM_KEYS));
		// Add keywords for all parties
		list.addAll(Arrays.asList(ChatUtils.ALL_PARTIES_KEYS));
		// Add keywords specifically for settlements
		list.addAll(Arrays.asList(ChatUtils.SETTLEMENT_KEYS));
		// Add keywords specifically for persons/robots
		list.addAll(Arrays.asList(ChatUtils.PERSON_KEYS));
		// Add keywords specifically for a vehicles
		list.addAll(Arrays.asList(ChatUtils.VEHICLE_KEYS));
		// Add shortcuts
		list.addAll(createShortcutHelp());

		return list.stream().sorted((s1, s2) -> s1.compareTo(s2)).collect(Collectors.toList());
	}

	/**
	 * Compiles a list of shortcuts
	 * 
	 * @return a list of string
	 */
	public static List<String> createShortcutHelp() {
		List<String> list = new ArrayList<>();
		list.addAll(Arrays.asList(ChatUtils.SPECIAL_KEYS));

		return list;
	}

	/**
	 * Compiles the names of settlements, people, robots and vehicles into one
	 * single list
	 * 
	 * @return a list of string
	 */
	public static List<String> createProperNounsList() {
		// Creates an empty array
		List<String> list = new ArrayList<>();

		// Creates an array with the names of all of settlements
		Collection<Settlement> settlements = unitManager.getSettlements();
		List<Settlement> settlementList = new ArrayList<Settlement>(settlements);

		// autoCompleteArray = settlementList.toArray(new
		// String[settlementList.size()]);
		// With java 8 stream
		// autoCompleteArray = settlementList.stream().toArray(String[]::new);

		Iterator<Settlement> i = settlementList.iterator();
		while (i.hasNext()) {
			Settlement s = i.next();
			list.add(s.getName());

			// Get two lists of settlers name
			// One having the order of [first name] + [last name]
			// The other having the order of [last name] + "," + [first name]
			Iterator<Person> j = s.getAllAssociatedPeople().iterator();
			while (j.hasNext()) {
				Person p = j.next();

				String first = "";
				String last = "";
				// Added names in both orders, namely, "first last" or "last, first"
				String firstLast = p.getName();
				String lastFirst = "";
				int len1 = firstLast.length();
				// Used for loop to find the last is the best approach instead of int index =
				// firstLast.indexOf(" ");
				int index = 0;

				for (int k = len1 - 1; k > 0; k--) {
					// Note: finding the whitespace from the end to 0 (from right to left) works
					// better than from left to right
					// e.g. Mary L. Smith (last name should be "Smith", not "L. Smith"
					if (firstLast.charAt(k) == ' ') {
						index = k;
						first = firstLast.substring(0, k);
						last = firstLast.substring(k + 1, len1);
						break;
					} else
						first = firstLast;
				}

				if (index == -1) {
					// the person has no last name
					first = firstLast;
					list.add(first);
				} else {
					first = firstLast.substring(0, index);
					last = firstLast.substring(index + 1, firstLast.length());
					lastFirst = last + ", " + first;
					list.add(firstLast);
					list.add(lastFirst);
				}

			}

			// get all robot names
			Iterator<Robot> k = s.getAllAssociatedRobots().iterator();
			while (k.hasNext()) {
				Robot r = k.next();
				list.add(r.getName());
			}
		}

		// Get all vehicles name
		Iterator<Vehicle> k = unitManager.getVehicles().iterator();
		while (k.hasNext()) {
			Vehicle v = k.next();
			list.add(v.getName());
		}

		return list;
	}

	public static StringBuffer printLevel(String[] str, int[] mods) {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < str.length; i++) {
			s.append(" " + str[i] + computeWhiteSpaces(str[i], 25) + " :   " + mods[i]);
			s.append(System.lineSeparator());
		}
		return s;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		personCache = null;
		robotCache = null;
		settlementCache = null;
		vehicleCache = null;
	}
}
