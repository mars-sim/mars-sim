/**
 * Mars Simulation Project
 * ChatUtils.java
 * @version 3.1.0 2018-09-30
 * @author Manny Kung
 */

package org.mars_sim.msp.core.terminal;

import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.mars.OrbitInfo;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.mars.Weather;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.NaturalAttributeType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.person.health.Complaint;
import org.mars_sim.msp.core.person.health.ComplaintType;
import org.mars_sim.msp.core.person.health.HealthProblem;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RoboticAttributeManager;
import org.mars_sim.msp.core.robot.RoboticAttributeType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;

public class ChatUtils {

	private static boolean expertMode = false;

	private static final double RADIANS_TO_DEGREES = 180D/Math.PI;
	
	public final static String SYSTEM = "System";
	public final static String SYSTEM_PROMPT = "System : ";
	public final static String YOU_PROMPT = "You : ";
	public final static String REQUEST_HEIGHT_CHANGE = YOU_PROMPT + "I'd like to change the chat box height to ";
	public final static String REQUEST_HELP = YOU_PROMPT + "I need some help! What are the available commands ?";

	public final static String REQUEST_KEYS = YOU_PROMPT
			+ "I need a list of the keywords. Would you tell me what they are ?";

	public final static String[] SPECIAL_KEYS = { 
			"key", "keys", "keyword", "keywords", "/k",
			"help", "/h", "/?", "?",	
			"/y1", "/y2", "/y3", "/y4",		
			"hello", "hi", "hey",			
			"expert", "/e",			
			"quit", "/q", 
			"bye", "/b", 
			"exit", "/x",     
			"pause", "/p"
	};
	
	public final static String[] SETTLEMENT_KEYS = new String[] {
			"weather", 
			"people", "settler", "person",
			"robot", "bot"
	};
	
	public final static String[] PERSON_KEYS = new String[] {
			"feeling", "status", "skill", "attribute",
			"birth", "age", "how old", "born",
			"friend",
			"country", "nationality", 
			"space agency", "sponsor", 
			"specialty",
			"outside", "inside", "container", 
			"building", "associated", "association", "home", "home town",		
			"garage", "vehicle top container", "vehicle container",  "vehicle park", "vehicle settlement", "vehicle outside", "vehicle inside",			
			"bed time", "sleep hour"
	};
	
	public final static String[] ALL_PARTIES_KEYS = new String[] {
			"relationship", "social", "relation",
			"bed", "sleep", "lodging", "quarters", "time",
			"where", "location", "located",	
			"job", "role", "career",
			"task", "activity", "action", "doing", 
			"mission", "trip", "excursion"
	};
	
	public final static String[] SYSTEM_KEYS = new String[] {
			"settlement", "check size", 
			"log all", "log fine", "log info", "log severe", "log finer", "log finest", "log warning", "log config",
			"vehicle", "rover", 
			"hi", "hello", "hey"
	};

	public final static String SWITCHES = 
			  "(5) Type 'bye', '/b', 'exit', '/x', 'quit', '/q' to leave the chat." + System.lineSeparator()
			+ "(6) Type 'help', '/h', '/?' for this help page." + System.lineSeparator()
			+ "(7) Type 'expert', '/e' to toggle between normal and expert mode." + System.lineSeparator()
			+ "(8) Type 'pause', '/p' to pause and unpause the simulation." + System.lineSeparator();

	public final static String HELP_TEXT = System.lineSeparator()
			+ "    ------------------------- H E L P ------------------------- " + System.lineSeparator()
			+ "(1) Type in the NAME of a person, a bot, or a settlement to connect with." + System.lineSeparator()
			+ "(2) Use KEYWORDS or type in a number between 0 and 18 (specific QUESTIONS on a party)." + System.lineSeparator() 
			+ "(3) Type '/k' or 'key' to see a list of KEYWORDS." + System.lineSeparator()
			+ "(4) Type 'settlement' to obtain the NAMES of the established settlements." + System.lineSeparator()
			+ SWITCHES;

	public final static String HELP_HEIGHT = "(9) Type 'y_' to change the chat box height; '/y1'-> 256 pixels (default) '/y2'->512 pixels, '/y3'->768 pixels, '/y4'->1024 pixels"
			+ System.lineSeparator();

	public final static String KEYWORDS_TEXT = System.lineSeparator()
			+ "    ------------------------- K E Y W O R D S ------------------------- " + System.lineSeparator()
			+ "(1)       In MarsNet : a settlement/bot/person's name OR " + getKeywordList(SYSTEM_KEYS) + System.lineSeparator() 
			+ "(2) For a Settlement : " + getKeywordList(SETTLEMENT_KEYS) + System.lineSeparator() 
			+ "(3)    For a Settler : " + getKeywordList(PERSON_KEYS) + System.lineSeparator() 
			+ "(4)  For all Parties : " + getKeywordList(ALL_PARTIES_KEYS) + System.lineSeparator() 
//			+ "(5) 0 to 18 are specific QUESTIONS on a person/bot/vehicle/settlement" + System.lineSeparator() 
			+ "    --------------------------  M I S C S -------------------------- " + System.lineSeparator() 
			+ SWITCHES;

	public final static String KEYWORDS_HEIGHT = HELP_HEIGHT; //"(8) '/y1' to reset height to 256 pixels (by default) after closing chat box. '/y2'->512 pixels, '/y3'->768 pixels, '/y4'->1024 pixels" + System.lineSeparator();

	public final static String DASHES = " ----------------------------------------- ";

	public final static String DASHES_1 = "----------";

	public static String helpText;

	public static String keywordText;

	/**
	 * The mode of connection. -1 if none, 0 if headless, 1 if gui
	 */
	private static int connectionMode = -1;

	public static Person personCache;
	public static Robot robotCache;
	public static Settlement settlementCache;
	public static Unit unitCache;
	public static Vehicle vehicleCache;

//	public static Settlement settlement;
//	public static Building building;
//	public static Equipment equipment;

	private static Simulation sim = Simulation.instance();
	private static Weather weather;
	private static SurfaceFeatures surfaceFeatures;
	private static Mars mars;
	private static MasterClock masterClock;
	private static MarsClock marsClock;
	private static OrbitInfo orbitInfo;
	private static RelationshipManager relationshipManager;
	private static SkillManager skillManager;

	private static DecimalFormat fmt = new DecimalFormat("##0");
	private static DecimalFormat fmt1 = new DecimalFormat("#0.0");
	private static DecimalFormat fmt2 = new DecimalFormat("#0.00");
	
	public ChatUtils() {
		masterClock = sim.getMasterClock();
		marsClock = masterClock.getMarsClock();
		mars = sim.getMars();
		weather = mars.getWeather();
		surfaceFeatures = mars.getSurfaceFeatures();
		orbitInfo = mars.getOrbitInfo();
		
		relationshipManager = Simulation.instance().getRelationshipManager();
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
		for (int i=0 ; i <last; i++) {
			if (i == last -1)
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
	
		return new String[] { questionText, responseText + System.lineSeparator()};
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
				return "Take care !";
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
	 * Asks the settlement when the input is a number
	 * 
	 * @param text the input number
	 * @return the response string[]
	 */
	public static String[] askSettlementNum(int num) {
//		System.out.println("askSettlementNum() in ChatUtils");
		String questionText = "";
		StringBuilder responseText = new StringBuilder();

		if (num == 1) {
			questionText = YOU_PROMPT + "how many beds are there in total ? ";
			responseText.append("The total # of beds is ");
			responseText.append(settlementCache.getPopulationCapacity());

		}

		else if (num == 2) {
			questionText = YOU_PROMPT + "how many beds that have already been designated to a person ? ";
			responseText.append("There are ");
			responseText.append(settlementCache.getTotalNumDesignatedBeds());
			responseText.append(" designated beds. ");

		}

		else if (num == 3) {
			questionText = YOU_PROMPT + "how many beds that are currently NOT occupied ? ";
			responseText.append("There are ");
			responseText.append(settlementCache.getPopulationCapacity() - settlementCache.getSleepers());
			responseText.append(" unoccupied beds. ");

		}

		else if (num == 4) {
			questionText = YOU_PROMPT + "how many beds are currently occupied ? ";
			responseText.append("There are ");
			responseText.append(settlementCache.getSleepers());
			responseText.append(" occupied beds with people sleeping on it at this moment. ");

		}

		else {
			questionText = YOU_PROMPT + "You entered '" + num + "'.";
			responseText.append("Sorry. This number is not assigned to a valid question.");
		}

		return new String[] { questionText, responseText.toString() };
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
		String earthDate = masterClock.getEarthClock().getDateStringF3();
		String earthTime = masterClock.getEarthClock().getTimeStringF0();
		int missionSol = marsClock.getMissionSol();
//		String marsDateTime = marsClock.getDateTimeStamp();
		String marsDate = marsClock.getDateString();
		String marsTime = marsClock.getDecimalTimeString();
		

		responseText.append(System.lineSeparator());
		
		String s0 = "Mission Sol : ";
		int num = 20 - s0.length();
		for (int i=0; i<num; i++) {
			responseText.append(" ");
		}
		responseText.append(s0);
		responseText.append(missionSol);
		responseText.append(System.lineSeparator());
		responseText.append(System.lineSeparator());
		
		String s1 = "Mars Date : ";
		num = 20 - s1.length();
		for (int i=0; i<num; i++) {
			responseText.append(" ");
		}
		responseText.append(s1);
		responseText.append(marsDate);
		responseText.append(System.lineSeparator());
		
		String s2 = "Mars Time : ";
		num = 20 - s2.length();
		for (int i=0; i<num; i++) {
			responseText.append(" ");
		}
		responseText.append(s2);
		responseText.append(marsTime);
		responseText.append(System.lineSeparator());
		responseText.append(System.lineSeparator());
		
		String s3 = "Earth Date : ";
		num = 20 - s3.length();
		for (int i=0; i<num; i++) {
			responseText.append(" ");
		}
		responseText.append(s3);
		responseText.append(earthDate);
		responseText.append(System.lineSeparator());
		
		String s4 = "Earth Time : ";
		num = 20 - s4.length();
		for (int i=0; i<num; i++) {
			responseText.append(" ");
		}
		responseText.append(s4);
		responseText.append(earthTime);
		responseText.append(System.lineSeparator());
		
		return responseText;
	}
	
	/**
	 * Computes the overall relationship score of a settlement
	 * 
	 * @param s Settlement
	 * @return the score
	 */
	public static double getRelationshipScore(Settlement s) {
		double score = 0;
		if (relationshipManager == null)
			relationshipManager = Simulation.instance().getRelationshipManager();
		
		Collection<Person> col = s.getAllAssociatedPeople();

		List<Person> list0 = new ArrayList<>(col);

		int count = 0;
		for (Person pp : list0) {
			Map<Person, Double> friends = relationshipManager.getFriends(pp);
			if (!friends.isEmpty()) {
				List<Person> list = new ArrayList<>(friends.keySet());
				for (int i = 0; i < list.size(); i++) {
					Person p = list.get(i);
					score += friends.get(p);
					count++;
				}
			}
		}
		
		score = Math.round(score/count *100.0)/100.0;
		
		return score;
	}
	
	/**
	 * Asks the settlement when the input is a string
	 * 
	 * @param text the input string
	 * @param name the input name of the settlement
	 * @return the response string[]
	 */
	public static String[] askSettlementStr(String text, String name) {
//		System.out.println("askSettlementStr() in ChatUtils");
		String questionText = "";
		StringBuilder responseText = new StringBuilder();
		
		if (text.toLowerCase().contains("relationship")
				|| text.toLowerCase().contains("relation")
				|| text.toLowerCase().contains("social")) {
			questionText = YOU_PROMPT + "How is the overall social score in this settlement ?"; 

			double score = getRelationshipScore(settlementCache);
		
			responseText.append(System.lineSeparator())	;		
			responseText.append(settlementCache.getName() + "'s current overall social score is " + fmt1.format(score) + ".");
			responseText.append(System.lineSeparator());
		
		}
		
		else if (text.toLowerCase().contains("time")) {
			questionText = YOU_PROMPT + "What time is it ?"; 
			responseText.append(settlementCache.getName() + " : ");
			responseText.append("Here's the latest time info.");
			responseText.append(System.lineSeparator());
			
			responseText.append(printTime());
			
		}
		
		else if (text.toLowerCase().contains("task") 
				|| text.toLowerCase().contains("activity")
				|| text.toLowerCase().contains("doing")
				|| text.toLowerCase().contains("action")) {
			questionText = YOU_PROMPT + "What is everybody doing at this moment? ";
			responseText.append(settlementCache + " : ");
			responseText.append("Here are the task rosters.");
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());
			responseText.append("(A). Settlers");
			responseText.append(System.lineSeparator());
			
			Map<String, List<Person>> map =
					settlementCache.getAllAssociatedPeople().stream().collect(Collectors.groupingBy(Person::getTaskDescription));
								
			for (Entry<String, List<Person>> entry : map.entrySet()) {
				String task = entry.getKey();
				List<Person> plist = entry.getValue();
			
				if (task != null) {
					responseText.append(System.lineSeparator());
					responseText.append("  ");
					responseText.append(task);
					responseText.append(System.lineSeparator());
					responseText.append(" ");
					int num = task.length() + 2;
					if (num > 0) {
						for (int i=0; i<num; i++) {
							responseText.append("-");
						}
					}
					responseText.append(System.lineSeparator());
									
				}
				else {
					responseText.append(System.lineSeparator());
					responseText.append("  ");	
					responseText.append("None");
					responseText.append(System.lineSeparator());
					responseText.append(" ");
					int num = 6;
					if (num > 0) {
						for (int i=0; i<num; i++) {
							responseText.append("-");
						}
					}
					responseText.append(System.lineSeparator());
				}
				
				for (Person p : plist) {
					responseText.append("  -  ");
					responseText.append(p.getName());
					responseText.append(System.lineSeparator());
				}
			}
			
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());
			responseText.append("(B). Bots");
			responseText.append(System.lineSeparator());
			
			Map<String, List<Robot>> botMap =
					settlementCache.getAllAssociatedRobots().stream().collect(Collectors.groupingBy(Robot::getTaskDescription));
								
			for (Entry<String, List<Robot>> entry : botMap.entrySet()) {
				String task = entry.getKey();
				List<Robot> plist = entry.getValue();
			
				if (task != null) {
					responseText.append(System.lineSeparator());
					responseText.append(" ");					
					responseText.append(task);
					responseText.append(System.lineSeparator());
					responseText.append(" ");
					int num = task.length() + 2;
					if (num > 0) {
						for (int i=0; i<num; i++) {
							responseText.append("-");
						}
					}
					responseText.append(System.lineSeparator());
									
				}
				else {
					responseText.append(System.lineSeparator());
					responseText.append(" ");			
					responseText.append("None");
					responseText.append(System.lineSeparator());
					responseText.append(" ");
					int num = 6;
					if (num > 0) {
						for (int i=0; i<num; i++) {
							responseText.append("-");
						}
					}
					responseText.append(System.lineSeparator());
				}
				
				for (Robot r : plist) {
					responseText.append("  -  ");
					responseText.append(r.getName());
					responseText.append(System.lineSeparator());
				}
			}
		}

		else if (text.toLowerCase().contains("mission") || text.toLowerCase().contains("trip")
				|| text.toLowerCase().contains("excursion")) {
			questionText = YOU_PROMPT + "Are there any on-going missions at this moment? ";
//			responseText.append(settlementCache + " : ");
//			responseText.append("Here's the mission roster.");
//			responseText.append(System.lineSeparator());
			
			List<Mission> missions = sim.getMissionManager().getMissions();
			
			if (missions.isEmpty()) {
				responseText.append(settlementCache + " : ");
				responseText.append("no on-going missions right now.");
			}
			
			else {
				responseText.append(settlementCache + " : ");
				responseText.append("here's the mission roster.");
				responseText.append(System.lineSeparator());
//				responseText.append(System.lineSeparator());

				for (int i=0; i<missions.size(); i++) {
					Mission mission = missions.get(i);
					int num = mission.getName().length() + 17;
					
					Collection<MissionMember> members = mission.getMembers();
					//Collections.sort(members);
					
					Person startingPerson = mission.getStartingMember();
					members.remove(startingPerson);
					
					
					List<MissionMember> plist = new ArrayList<>(members);			
					
					double dist = 0;
					double trav = 0;
					Vehicle v = null;
					
					if (mission instanceof VehicleMission) {
						v = ((VehicleMission) mission).getVehicle();
						dist = Math.round(((VehicleMission)mission).getTotalDistance()*10.0)/10.0;//.getStartingTravelledDistance(); // getTotalDistance();//.
						trav = Math.round(((VehicleMission)mission).getTotalDistanceTravelled()*10.0)/10.0;
					}
									
					if (mission != null) {
//						responseText.append(System.lineSeparator());
//						responseText.append(" ");
//						if (num > 0) {
//							for (int j=0; j<num; j++) {
//								responseText.append("-");
//							}
//						}
						responseText.append(System.lineSeparator());
						responseText.append(" (" + (i+1) + "). " + mission.getName());
//						responseText.append("      Mission : " + mission.getName());
						responseText.append(System.lineSeparator());
						responseText.append(" ");
						if (num > 0) {
							for (int j=0; j<num; j++) {
								responseText.append("-");
							}
						}
						responseText.append(System.lineSeparator());
						
						if (v != null) {
						responseText.append("     Vehicle : " + v.getName());
						responseText.append(System.lineSeparator());
						responseText.append("        Type : " + v.getVehicleType());
						responseText.append(System.lineSeparator());
						responseText.append("  Est. Dist. : " + dist + " km");
						responseText.append(System.lineSeparator());
						responseText.append("   Travelled : " + trav + " km");
						responseText.append(System.lineSeparator());
						}
						responseText.append("       Phase : " + mission.getPhaseDescription());
						responseText.append(System.lineSeparator());
						
						responseText.append(" ");
						if (num > 0) {
							for (int j=0; j<num; j++) {
								responseText.append("-");
							}
						}
						
						responseText.append(System.lineSeparator());
										
					}
				
					responseText.append("  -  ");
					responseText.append(startingPerson.getName());
					responseText.append(" (The Lead)");
					responseText.append(System.lineSeparator());
					
					for (MissionMember p : plist) {
//						if ((Person)p != startingPerson) {
							responseText.append("  -  ");
							responseText.append(p.getName());
							responseText.append(System.lineSeparator());
							responseText.append(System.lineSeparator());
//						}
					}		
					
//					responseText.append(" ");
//					if (num > 0) {
//						for (int j=0; j<num; j++) {
//							responseText.append("-");
//						}
//					}
//					
//					responseText.append(System.lineSeparator());
				}		
			}
			
//			Map<String, List<Person>> map =
//					settlementCache.getAllAssociatedPeople().stream()
//					.collect(Collectors.groupingBy(Person::getMissionDescription));
//								
//			for (Entry<String, List<Person>> entry : map.entrySet()) {
//				String mission = entry.getKey();
//				List<Person> plist = entry.getValue();
			
//				if (mission != null) {
//					responseText.append(System.lineSeparator());
//					responseText.append(" ");			
//					responseText.append(mission);
//					responseText.append(System.lineSeparator());
//					responseText.append(" ");
//					int num = mission.length() + 2;
//					if (num > 0) {
//						for (int i=0; i<num; i++) {
//							responseText.append("-");
//						}
//					}
//					responseText.append(System.lineSeparator());
//									
//				}
//				else {
//					responseText.append(System.lineSeparator());
//					responseText.append(" ");			
//					responseText.append("None");
//					responseText.append(System.lineSeparator());
//					responseText.append(" ");
//					int num = 6;
//					if (num > 0) {
//						for (int i=0; i<num; i++) {
//							responseText.append("-");
//						}
//					}
//					responseText.append(System.lineSeparator());
//				}
//				
//				for (Person p : plist) {
//					responseText.append("  -  ");
//					responseText.append(p.getName());
//					responseText.append(System.lineSeparator());
//				}
//			}
		}
		
		else if (text.toLowerCase().contains("where")
				|| text.toLowerCase().contains("location")
				|| text.toLowerCase().contains("located")) {
			questionText = YOU_PROMPT + "Where is the settlement ?"; 
			// TODO: add to tell nearby georgraphical features. e.g. at what basin
			responseText.append(settlementCache + " : ");
			responseText.append("We're located at ");
			responseText.append(settlementCache.getCoordinates());
		}
		
		else if (text.toLowerCase().contains("country")
				|| text.toLowerCase().contains("nation")
				|| text.toLowerCase().contains("nationality")) {
			questionText = YOU_PROMPT + "What countries are the people of this settlement composed of ?";
			responseText.append(System.lineSeparator());
			responseText.append(settlementCache + " : ");
			responseText.append("See below.");
			
//			List<Person> list = new ArrayList<>(settlementCache.getAllAssociatedPeople());
			
//			String oneLiner = settlementCache.getAllAssociatedPeople().stream()
//					.map(Person::getCountry)  
//					.collect(Collectors.joining(", ", "Countries: ", "."));		
//			responseText.append(oneLiner);
			
//			Map<String, Person> map = settlementCache.getAllAssociatedPeople().stream()
//					.collect(Collectors.toMap(Person::getName, Function.identity())); 
			
			Map<String, List<Person>> map =
					settlementCache.getAllAssociatedPeople().stream().collect(Collectors.groupingBy(Person::getCountry));
								
			for (Entry<String, List<Person>> entry : map.entrySet()) {
				String country = entry.getKey();
				String sponsor = UnitManager.mapCountry2Sponsor(country);
				responseText.append(System.lineSeparator());
				responseText.append(" ----- ");
				responseText.append(country);
				responseText.append(" (");
				responseText.append(sponsor);
				responseText.append(") ");
				responseText.append(" ----- ");
				responseText.append(System.lineSeparator());
				List<Person> list = entry.getValue();
				for (int i=0; i<list.size(); i++) {
					responseText.append("(" + (i+1) + "). ");
					responseText.append(list.get(i));
					responseText.append(System.lineSeparator());
				}
			}
		}
		
		else if (text.toLowerCase().contains("role")) {
			questionText = YOU_PROMPT + "What are the roles ?";
			responseText.append(System.lineSeparator());
			responseText.append(settlementCache + " : ");
			responseText.append("See below.");
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());
			
			List<Person> list = settlementCache.getAllAssociatedPeople().stream()
//					.sorted(Comparator.reverseOrder())
//					.sorted((f1, f2) -> Long.compare(f2.getRole().getType().ordinal(), f1.getRole().getType().ordinal()))
					//.sorted((p1, p2)-> p1.getRole().getType().getName().compareTo(p2.getRole().getType().getName()))
					.sorted(Comparator.comparing(o -> o.getRole().getType().ordinal()))
					.collect(Collectors.toList());
			
			for (Person p : list) {
				String role = p.getRole().getType().getName();
				responseText.append(p);
				int num = 30 - p.getName().length();// - role.length();
				if (num > 0) {
					for (int i=0; i<num; i++) {
						responseText.append(" ");
					}
				}
				responseText.append(role);
				responseText.append(System.lineSeparator());
			} 
		}
		
		else if (text.contains("job")
				|| text.contains("career")) {
			questionText = YOU_PROMPT + "What is everybody's job ?";
			responseText.append(System.lineSeparator());
			responseText.append(settlementCache + " : ");
			responseText.append("See below.");
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());

			List<Person> list = settlementCache.getAllAssociatedPeople().stream()
					.sorted((p1, p2)-> p1.getMind().getJob().getName(p1.getGender()).compareTo(p2.getMind().getJob().getName(p2.getGender())))
					.collect(Collectors.toList());
			
			for (Person p : list) {
				String job = p.getMind().getJob().getName(p.getGender());
				responseText.append(p);
				int num = 30 - p.getName().length();// - job.length();
				if (num > 0) {
					for (int i=0; i<num; i++) {
						responseText.append(" ");
					}
				}
				responseText.append(job);
				responseText.append(System.lineSeparator());
			}
			
		}
		
		else if (text.toLowerCase().contains("weather")) {
			questionText = YOU_PROMPT + "How's the weather in " + settlementCache.toString() + " ?";
			responseText.append(System.lineSeparator());
			responseText.append(settlementCache + " : ");
			responseText.append("See below.");
			
			if (marsClock == null) marsClock = Simulation.instance().getMasterClock().getMarsClock();
			if (mars == null) mars = Simulation.instance().getMars();
			if (weather == null) weather = mars.getWeather();
			if (surfaceFeatures == null) surfaceFeatures = mars.getSurfaceFeatures();
			if (orbitInfo == null) orbitInfo = mars.getOrbitInfo();
			
			Coordinates location = settlementCache.getCoordinates();
//			System.out.println("location in ChatUtils : " + location);
//			String lat = location.getFormattedLatitudeString();
//			String lon = location.getFormattedLongitudeString();
			
			responseText.append(System.lineSeparator());
//			responseText.append(settlementCache + " is at " + location);//(" + lat + ", " + lon + ")"); 
			responseText.append("Location : " + location);
			responseText.append(System.lineSeparator());

			String date = marsClock.getDateString();
			responseText.append("On " + date); 

			String time = marsClock.getDecimalTimeString();
			responseText.append(" at " + time); 
			
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());
			
			double t = weather.getTemperature(location);
			String tt = fmt.format(t) + " " + Msg.getString("temperature.sign.degreeCelsius"); //$NON-NLS-1$		
			responseText.append("Current outside temperature : " + tt); 
			responseText.append(System.lineSeparator());
			
			double p = weather.getAirPressure(location);
			String pp = fmt2.format(p) + " " + Msg.getString("pressure.unit.kPa"); //$NON-NLS-1$		
			responseText.append("Current air pressure : " + pp); 
			responseText.append(System.lineSeparator());
			
			double ad = weather.getAirDensity(location);
			String aad = fmt2.format(ad) + " " + Msg.getString("airDensity.unit.gperm3"); //$NON-NLS-1$
			responseText.append("Current air density : " + aad);
			responseText.append(System.lineSeparator());
			
			double ws = weather.getWindSpeed(location);
			String wws = fmt2.format(ws) + " " + Msg.getString("windspeed.unit.meterpersec"); //$NON-NLS-1$
			responseText.append("Current wind speed : " + wws); 		
			responseText.append(System.lineSeparator());
			
			double wd = weather.getWindDirection(location);
			String wwd = fmt.format(wd) + " " + Msg.getString("windDirection.unit.deg"); //$NON-NLS-1$
			responseText.append("Current wind direction : " + wwd); 		
			responseText.append(System.lineSeparator());
			
	 		double od = surfaceFeatures.getOpticalDepth(location);
	 		String ood = fmt2.format(od);
			responseText.append("Current optical depth : " + ood); 
			responseText.append(System.lineSeparator());
			
			double sza = orbitInfo.getSolarZenithAngle(location);
			String ssza = fmt2.format(sza* RADIANS_TO_DEGREES) + " " + Msg.getString("direction.degreeSign"); //$NON-NLS-1$
			responseText.append("Current solar zenith angle : " + ssza); 
			responseText.append(System.lineSeparator());
			
			double sda = orbitInfo.getSolarDeclinationAngleDegree();
			String ssda = fmt2.format(sda) + " " + Msg.getString("direction.degreeSign"); //$NON-NLS-1$
			responseText.append("Current solar declination angel : " + ssda); 
			responseText.append(System.lineSeparator());
			
			double si = surfaceFeatures.getSolarIrradiance(location);
			String ssi = fmt2.format(si) + " " + Msg.getString("solarIrradiance.unit"); //$NON-NLS-1$		
			responseText.append("Current solar irradiance : " + ssi); 		
			responseText.append(System.lineSeparator());
		}
		
		else if (text.toLowerCase().contains("people") || text.toLowerCase().contains("settler") 
				|| text.toLowerCase().contains("person")) {
			
			List<Person> all = new ArrayList<>(settlementCache.getAllAssociatedPeople());
//			int total = settlementCache.getNumCitizens();
//			int indoor = settlementCache.getIndoorPeopleCount();
//			int dead = settlementCache.getNumDeceased();
//			int outdoor = settlementCache.getNumOutsideEVAPeople(); //total - indoor - dead;
			
			List<Person> eva = new ArrayList<>(settlementCache.getOutsideEVAPeople());
			List<Person> indoorP = new ArrayList<>(settlementCache.getIndoorPeople());
			List<Person> deceasedP = new ArrayList<>(settlementCache.getDeceasedPeople());
			List<Person> onMission = new ArrayList<>(settlementCache.getOnMissionPeople());
			
			Collections.sort(all);
			Collections.sort(eva);
			Collections.sort(indoorP);
			Collections.sort(deceasedP);
			Collections.sort(onMission);
			
			int numAll = all.size(); 
			int numIndoor = indoorP.size();//settlementCache.getIndoorPeopleCount();
			int numDead = deceasedP.size();//settlementCache.getNumDeceased();
			int numEva = eva.size();//settlementCache.getNumOutsideEVAPeople(); //total - indoor - dead;
			int numMission = onMission.size();
			
			questionText = YOU_PROMPT + "Who are the settlers ? ";
			responseText.append(settlementCache + " : below is the brief summary of the settlers :"); 
//			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());
			responseText.append("  -----------------------");
			responseText.append(System.lineSeparator());
			responseText.append("         Summary");
			responseText.append(System.lineSeparator());
			responseText.append("  -----------------------");
			responseText.append(System.lineSeparator());
			responseText.append("      Registered : " + numAll);
			responseText.append(System.lineSeparator());
			responseText.append("          Inside : " + numIndoor);
			responseText.append(System.lineSeparator());
			responseText.append("    On a Mission : " + numMission);
			responseText.append(System.lineSeparator());
			responseText.append("   EVA Operation : " + numEva);
			responseText.append(System.lineSeparator());
			responseText.append("        Deceased : " + numDead);
//			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());
			
//			responseText.append("  -----------------------");
//			responseText.append(System.lineSeparator());
//			responseText.append("          Roster");
//			responseText.append(System.lineSeparator());
//			responseText.append("  -----------------------");
//			responseText.append(System.lineSeparator());
						
			// Indoor
			responseText.append(System.lineSeparator());
			responseText.append("  A. Registered");
			responseText.append(System.lineSeparator());
			responseText.append("  -------------");
			responseText.append(System.lineSeparator());
			
			responseText.append(printList(all));
			
			// Indoor
			responseText.append(System.lineSeparator());
			responseText.append("  B. Inside");
			responseText.append(System.lineSeparator());
			responseText.append("  ---------");
			responseText.append(System.lineSeparator());
			
			responseText.append(printList(indoorP));
			
			// Outdoor
			responseText.append(System.lineSeparator());
			responseText.append("  C. EVA Operation");
			responseText.append(System.lineSeparator());
			responseText.append("  ----------------");
			responseText.append(System.lineSeparator());
			
			responseText.append(printList(eva));
			
			// on a mission
			responseText.append(System.lineSeparator());
			responseText.append("  D. On a Mission");
			responseText.append(System.lineSeparator());
			responseText.append("  ---------------");
			responseText.append(System.lineSeparator());
			
			responseText.append(printList(onMission));
			
			// Deceased
			responseText.append(System.lineSeparator());
			responseText.append("  E. Deceased");
			responseText.append(System.lineSeparator());
			responseText.append("  -----------");
			responseText.append(System.lineSeparator());
			
			responseText.append(printList(deceasedP));
		
		}
		
		else if (text.toLowerCase().contains("bed") || text.toLowerCase().contains("sleep") 
				|| text.equalsIgnoreCase("lodging")
				|| text.toLowerCase().contains("quarters")) {

			questionText = YOU_PROMPT + "how well are the beds utilized ? ";
			responseText.append(System.lineSeparator());
			
			responseText.append("Total number of beds : ");
			responseText.append(settlementCache.getPopulationCapacity());
			responseText.append(System.lineSeparator());
			responseText.append("Desginated beds : ");
			responseText.append(settlementCache.getTotalNumDesignatedBeds());
			responseText.append(System.lineSeparator());
			responseText.append("Unoccupied beds : ");
			responseText.append(settlementCache.getPopulationCapacity() - settlementCache.getSleepers());
			responseText.append(System.lineSeparator());
			responseText.append("Occupied beds : ");
			responseText.append(settlementCache.getSleepers());
			responseText.append(System.lineSeparator());
		}

		else if (text.equalsIgnoreCase("vehicle") || text.equalsIgnoreCase("rover")) {

			questionText = YOU_PROMPT + "What are the vehicles in the settlement ? ";
			responseText.append(System.lineSeparator());
			
			Collection<Vehicle> list = settlementCache.getAllAssociatedVehicles();
			
			responseText.append(DASHES);
			responseText.append(System.lineSeparator());
			int num = (DASHES.length() - settlementCache.getName().length())/2;
			if (num > 0) {
				for (int i=0; i<num; i++) {
					responseText.append(" ");
				}
			}
			
			responseText.append(settlementCache.getName());
			responseText.append(System.lineSeparator());
			responseText.append(DASHES);
			responseText.append(System.lineSeparator());
				
			responseText.append("(1).  Grand Total : ");
			responseText.append(settlementCache.getAllAssociatedVehicles().size());
			responseText.append(System.lineSeparator());
			responseText.append("(2).  # on Mission : ");
			responseText.append(settlementCache.getMissionVehicles().size());
			responseText.append(System.lineSeparator());
			responseText.append("(2a). # of Cargo Rovers on Mission : ");
			responseText.append(settlementCache.getCargoRovers(2).size());
			responseText.append(System.lineSeparator());
			responseText.append("(2b). # of Transport Rovers on Mission : ");
			responseText.append(settlementCache.getTransportRovers(2).size());
			responseText.append(System.lineSeparator());
			responseText.append("(2c). # of Explorer Rovers on Mission : ");
			responseText.append(settlementCache.getExplorerRovers(2).size());
			responseText.append(System.lineSeparator());
			responseText.append("(2d). # of Light Utility Vehicles (LUVs) on Mission : ");
			responseText.append(settlementCache.getLUVs(2).size());
			responseText.append(System.lineSeparator());
			
			responseText.append("(3).  # NOT on mission : ");
			responseText.append(settlementCache.getParkedVehicleNum());
			responseText.append(System.lineSeparator());
			responseText.append("(3a). # of Parked Cargo Rovers : ");
			responseText.append(settlementCache.getCargoRovers(1).size());
			responseText.append(System.lineSeparator());
			responseText.append("(3b). # of Parked Transport Rovers : ");
			responseText.append(settlementCache.getTransportRovers(1).size());
			responseText.append(System.lineSeparator());
			responseText.append("(3c). # of Parked Explorer Rovers : ");
			responseText.append(settlementCache.getExplorerRovers(1).size());
			responseText.append(System.lineSeparator());
			responseText.append("(3d). # of Parked Light Utility Vehicles (LUVs) : ");
			responseText.append(settlementCache.getLUVs(1).size());
			responseText.append(System.lineSeparator());
			
			responseText.append(System.lineSeparator());
			responseText.append("      ----------------------------");
			responseText.append(System.lineSeparator());
			responseText.append("           I n v e n t o r y");
			responseText.append(System.lineSeparator());
			responseText.append("      ----------------------------");
			responseText.append(System.lineSeparator());
			
			List<Vehicle> vlist = list.stream()
					.sorted((p1, p2)-> p1.getVehicleType().compareTo(p2.getVehicleType()))
					.collect(Collectors.toList());
			
			for (Vehicle v : vlist) {
				responseText.append(v.getName());
				int num2 = 25 - v.getName().length();
				if (num2 > 0) {
					for (int i=0; i<num2; i++) {
						responseText.append(" ");
					}
				}
				responseText.append(v.getVehicleType());
				responseText.append(System.lineSeparator());
			}
		
			responseText.append(System.lineSeparator());
		}

		else if (text.equalsIgnoreCase("bot") || text.equalsIgnoreCase("bots") 
				|| text.equalsIgnoreCase("robot") || text.equalsIgnoreCase("robot")) {
			questionText = YOU_PROMPT + "What kind of bots do you have? ";
			responseText.append(settlementCache + " : we have " 
					+ settlementCache.getNumBots() + " bots.");
			Collection<Robot> list = settlementCache.getRobots();
			List<Robot> namelist = new ArrayList<>(list);
			Collections.sort(namelist);
			String s = "";
			for (int i = 0; i < namelist.size(); i++) {
				s = s + "(" + (i+1) + "). " + namelist.get(i).toString() + System.lineSeparator();
			}
			//		.replace("[", "").replace("]", "");//.replaceAll(", ", ",\n");
			//System.out.println("list : " + list);
			responseText.append(System.lineSeparator());
			responseText.append(s);
			responseText.append(System.lineSeparator());
		}
		
		else if (text.equalsIgnoreCase("key") 
				|| text.equalsIgnoreCase("keys")  
				|| text.equalsIgnoreCase("keyword")  
				|| text.equalsIgnoreCase("keywords")  
				|| text.equalsIgnoreCase("/k")) {

//			help = true;
			questionText = REQUEST_KEYS;
			if (connectionMode == 0) {
				keywordText = KEYWORDS_TEXT;
			} else {
				keywordText = KEYWORDS_TEXT + KEYWORDS_HEIGHT;
			}
			// responseText.append(System.lineSeparator());
			responseText.append(keywordText);

		}

		else if (text.equalsIgnoreCase("help") || text.equalsIgnoreCase("/h") 
				|| text.equalsIgnoreCase("/?") || text.equalsIgnoreCase("?")) {

//			help = true;
			questionText = REQUEST_HELP;
			if (connectionMode == 0) {
				helpText = HELP_TEXT;
			} else {
				helpText = HELP_TEXT + HELP_HEIGHT;
			}
			// responseText.append(System.lineSeparator());
			responseText.append(helpText);

		}

		else {

			String[] txt = clarify(name);
			questionText = txt[0];
			responseText.append(txt[1]);
		}

		return new String[] { questionText, responseText.toString() };
	}

	
	/**
	 * Asks the person or the robot
	 * 
	 * @param text the string input
	 * @param num  the number input
	 * @param name the number of the person or robot
	 * @param u    the unit
	 * @return string array
	 */
	public static String[] askPersonRobot(String text, int num, String name, Unit u) {
//		System.out.println("askPersonRobot() in ChatUtils");
		String questionText = "";
		StringBuilder responseText = new StringBuilder();

		responseText.append(name);
		responseText.append(": ");

		if (text.toLowerCase().contains("attribute")) {
			questionText = YOU_PROMPT + "What are your natural attributes ?"; 
			
	        
			responseText.append("here's a list of my natural attributes with scores ");
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());
			responseText.append("          Attributes | Score");
			responseText.append(System.lineSeparator());
			responseText.append(" --------------------------");	
			responseText.append(System.lineSeparator());
			
			if (personCache != null) {
				NaturalAttributeManager n_manager = personCache.getNaturalAttributeManager();		
				Hashtable<NaturalAttributeType, Integer> n_attributes = n_manager.getAttributeTable();
				List<String> attributeList = n_manager.getAttributeList();
				int max = 20;
//				String space = "";		
				for (int i=0; i< attributeList.size(); i++) {
					String n = attributeList.get(i);
					int size = n.length();
//					if (i+1 <= 9)
//						space = " ";
					for (int j=0; j< (max-size); j++) {
						responseText.append(" ");
					}
//					responseText.append(space + "(" + (i+1) + ") ");
					responseText.append(n);
					responseText.append(" : ");
					responseText.append(n_attributes.get(NaturalAttributeType.valueOfIgnoreCase(n)));
					responseText.append(System.lineSeparator());			
				}
				
			}
			else if (robotCache != null) {
				RoboticAttributeManager r_manager = robotCache.getRoboticAttributeManager();	
				Hashtable<RoboticAttributeType, Integer> r_attributes = r_manager.getAttributeTable();
				List<String> attributeList = r_manager.getAttributeList();
				int max = 20;
//				String space = "";		
				for (int i=0; i< attributeList.size(); i++) {
					String n = attributeList.get(i);
					int size = n.length();
//					if (i+1 <= 9)
//						space = " ";
					for (int j=0; j< (max-size); j++) {
						responseText.append(" ");
					}
//					responseText.append(space + "(" + (i+1) + ") ");
					responseText.append(n);
					responseText.append(" : ");
					responseText.append(r_attributes.get(RoboticAttributeType.valueOfIgnoreCase(n)));
					responseText.append(System.lineSeparator());			
				}
				
			}			
	
		}
		
		else if (text.toLowerCase().contains("skill")) {
			questionText = YOU_PROMPT + "What are your skills ?"; 
			
			if (personCache != null) {
				skillManager = personCache.getMind().getSkillManager();	
			}
			
			else if (robotCache != null) {
				skillManager = robotCache.getBotMind().getSkillManager();			
			}			
			
			responseText.append("here's a list of my skills with level : ");
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());
			responseText.append("       Type of Skill | Level");
			responseText.append(System.lineSeparator());
			responseText.append("     ------------------------");	
			responseText.append(System.lineSeparator());
			
			Map<String, Integer> skills = skillManager.getSkillsMap();
			List<String> skillNames = skillManager.getSkillNames();
			Collections.sort(skillNames);
//			SkillType[] keys = skillManager.getKeys();
			
			int max = 20;
//			String space = "";		
			for (int i=0; i< skillNames.size(); i++) {
				String n = skillNames.get(i);
				int size = n.length();
//				if (i+1 <= 9)
//					space = " ";
				for (int j=0; j< (max-size); j++) {
					responseText.append(" ");
				}
//				responseText.append(space + "(" + (i+1) + ") ");
				responseText.append(n);
				responseText.append(" : ");
				responseText.append(skills.get(n));
				responseText.append(System.lineSeparator());			
			}
			
		}
		

		else if (text.toLowerCase().contains("time")) {
			questionText = YOU_PROMPT + "What time is it ?"; 
			
//			responseText.append(personCache.getName() + " : ");
			responseText.append("According to Marsnet, here's the latest time info.");
			responseText.append(System.lineSeparator());
			
			responseText.append(printTime());
			
		}
		
		else if (text.equalsIgnoreCase("space agency")
				|| text.toLowerCase().contains("sponsor")) {
			questionText = YOU_PROMPT + "What is your sponsoring space agency ? ";

			if (personCache != null) {
				String sponsor = personCache.getReportingAuthority().getOrg().getName();
				responseText.append("I was sponsored by ");
				responseText.append(sponsor);

			} else if (robotCache != null) {
				responseText.append("I was assembled on ");
				responseText.append(robotCache.getSettlement().getName());
			}

		}
	
		else if (text.toLowerCase().contains("friend")) {
			questionText = YOU_PROMPT + "Who's your best friend ?";

			if (relationshipManager == null)
				relationshipManager = Simulation.instance().getRelationshipManager();
			
			Map<Person, Double> bestFriends = relationshipManager.getBestFriends(personCache);
			if (bestFriends.isEmpty()) {
				responseText.append("I don't have any friends yet.");
			}
			else { 
				List<Person> list = new ArrayList<>(bestFriends.keySet());
				int size = list.size();
				
				if (size == 1) {
					Person p = list.get(0);
					double score = bestFriends.get(p);
					String pronoun = "him";
					String relation = RelationshipManager.describeRelationship(score);
					if (!relation.equals("trusting") && !relation.equals("hating"))
						relation = relation + " to ";
					else
						relation = relation + " ";
					if (p.getGender() == GenderType.FEMALE)
						pronoun = "her";
					if (score < 45)
						responseText.append("My friend includes " + p + ". ");
					else
						responseText.append("My best friend is " + p + ". ");
					responseText.append("I'm " + relation + pronoun + " (");
					responseText.append("score : " + fmt1.format(score) + ").");
				}
				else if (size >= 2) {
					responseText.append("My best friends are ");
					responseText.append(System.lineSeparator());
					for (int i = 0; i < size; i++) {

						Person p = list.get(i);
						double score = bestFriends.get(p);
						String pronoun = "him";
						String relation = RelationshipManager.describeRelationship(score);
						if (!relation.equals("trusting") && !relation.equals("hating"))
							relation = relation + " to ";
						else
							relation = relation + " ";
						if (p.getGender() == GenderType.FEMALE)
							pronoun = "her";
						responseText.append("(" + (i+1) + "). " + p + " -- ");
						responseText.append("I'm " + relation + pronoun + " (");
						responseText.append("score : " + fmt1.format(score) + ").");
						responseText.append(System.lineSeparator());
					}
				}	
			}
		}
		
		else if (text.toLowerCase().contains("relationship")
				|| text.toLowerCase().contains("relation")
				|| text.toLowerCase().contains("social")) {
			questionText = YOU_PROMPT + "How are your relationship with others ?"; 

			if (relationshipManager == null)
				relationshipManager = Simulation.instance().getRelationshipManager();
			
			Map<Person, Double> friends = relationshipManager.getFriends(personCache);
//			System.out.println("friends in ChatUtils : " + friends);
			if (friends.isEmpty()) {
				responseText.append("I don't have any friends yet.");
			}
			else {
				responseText.append(" See the table below ");
				responseText.append(System.lineSeparator());	
				responseText.append(System.lineSeparator());
				List<Person> list = new ArrayList<>(friends.keySet());
				int size = list.size();
				
				responseText.append("                   Friend | Score | Attitude "
						+ System.lineSeparator());
				responseText.append("      -----------------------------------------"
						+ System.lineSeparator());		
				
				int max = 25;
				int max1 = 16;
				int max2 = 7;
				String SPACE = " ";
				
				if (size == 1) {
					Person p = list.get(0);
					double score = friends.get(p);
					String relation = RelationshipManager.describeRelationship(score);
					int size0 = max - p.getName().length();
					for (int i=0; i<size0; i++) {
						responseText.append(SPACE);
					}
					responseText.append(p);
					
					int size2 = max2 - fmt1.format(score).length();
					for (int i=0; i<size2; i++) {
						responseText.append(SPACE);
					}
					responseText.append(fmt1.format(score));
					
					int size1 = max1 - relation.length();
					for (int i=0; i<size1; i++) {
						responseText.append(SPACE);
					}
					responseText.append(relation);
					
					responseText.append(System.lineSeparator());	
				}
				else if (size >= 2) {
//					responseText.append(System.lineSeparator());	
					for (int x = 0; x < size; x++) {
						Person p = list.get(x);
						double score = friends.get(p);
						String relation = RelationshipManager.describeRelationship(score);
						int size0 = max - p.getName().length();
						for (int i=0; i<size0; i++) {
							responseText.append(SPACE);
						}
						responseText.append(p);
						
						int size2 = max2 - fmt1.format(score).length();
						for (int i=0; i<size2; i++) {
							responseText.append(SPACE);
						}
						responseText.append(fmt1.format(score));
						
//						int size1 = max1 - relation.length();
//						for (int i=0; i<size1; i++) {
//							responseText.append(SPACE);
//						}
						responseText.append("    ");
						responseText.append(relation);
					
						responseText.append(System.lineSeparator());	
					}
				}	
				
//				if (size == 1) {
//					Person p = list.get(0);
//					double score = friends.get(p);
//					String pronoun = "him";
//					String relation = RelationshipManager.describeRelationship(score);
//					if (!relation.equals("trusting") && !relation.equals("hating"))
//						relation = relation + " to ";
//					else
//						relation = relation + " ";
//					if (p.getGender() == GenderType.FEMALE)
//						pronoun = "her";
//					responseText.append("My relationship is simple with " + p + ". ");
//					responseText.append("I'm " + relation + pronoun + " (");
//					responseText.append("score : " + fmt1.format(score) + ").");
//				}
//				else if (size >= 2) {
//					responseText.append("My relationship with those I know are : ");
//					responseText.append(System.lineSeparator());	
//					for (int i = 0; i < size; i++) {
//						Person p = list.get(i);
//						double score = friends.get(p);
//						String pronoun = "him";
//						String relation = RelationshipManager.describeRelationship(score);
//						if (!relation.equals("trusting") && !relation.equals("hating"))
//							relation = relation + " to ";
//						else
//							relation = relation + " ";
//						if (p.getGender() == GenderType.FEMALE)
//							pronoun = "her";					
//						responseText.append("(" + (i+1) + "). " + p + " -- ");
//						responseText.append("I'm " + relation + pronoun + " (");
//						responseText.append("score : " + fmt1.format(score) + ").");
//						responseText.append(System.lineSeparator());
//					}
//				}	
			}
		}

		else if (text.toLowerCase().contains("feeling") || text.toLowerCase().contains("how you been")) {
			questionText = YOU_PROMPT + "how have you been ?"; // what is your Location Situation [Expert Mode only] ?";

			if (personCache != null) {
				responseText.append("I'm feeling ");
				responseText.append(personCache.getMind().getEmotion().getDescription());

			} else if (robotCache != null) {
				if (robotCache.getSystemCondition().isInoperable())
					responseText.append("I'm inoperable.");
				else 
					responseText.append("I'm operational.");					
			}

		}

		else if (num == 0 || text.toLowerCase().contains("status") || text.toLowerCase().contains("how you doing")) {
			questionText = YOU_PROMPT + "how are you doing ?"; // what is your Location Situation [Expert Mode only] ?";

			if (personCache != null) {
				responseText.append("I'm ");
				responseText.append(personCache.getStatus());

			} else if (robotCache != null) {
				if (robotCache.getSystemCondition().isInoperable())
					responseText.append("I'm inoperable.");
				else 
					responseText.append("I'm operational.");					
//				responseText.append(robotCache...());
			}

		}

		else if (num == 1 || text.toLowerCase().contains("age") || text.toLowerCase().contains("born")
				|| text.toLowerCase().contains("when were you born") || text.toLowerCase().contains("how old")
				|| text.toLowerCase().contains("what is your age")) {
			questionText = YOU_PROMPT + "What is your age ?"; // what is your Location Situation [Expert Mode only] ?";

			if (personCache != null) {
				responseText.append("I was born in ");
				responseText.append(personCache.getBirthDate());
				responseText.append(" and I'm ");
				responseText.append(personCache.updateAge());

			} else if (robotCache != null) {
				responseText.append("I was assembled in ");
				responseText.append(robotCache.getBirthDate());
				responseText.append(" and I'm ");
				responseText.append(robotCache.updateAge());
			}

		}

		else if (num == 2 || text.contains("what your role")
				|| text.toLowerCase().contains("role")) {
			questionText = YOU_PROMPT + "What is your role ?";

			if (personCache != null) {
				String role = personCache.getRole().getType().getName();
				responseText.append("I'm the ");
				responseText.append(role);
				responseText.append(" of ");
				responseText.append(personCache.getAssociatedSettlement().getName());
				responseText.append(".");
			} 
			
			else if (robotCache != null) {
				responseText.append("I'm just a robot.");
			}
		}
		
		else if (num == 2 || text.contains("what your job") || text.toLowerCase().contains("what your specialty")
				|| text.toLowerCase().contains("job") || text.toLowerCase().contains("career")
				|| text.toLowerCase().contains("specialty")) {
			questionText = YOU_PROMPT + "What is your job ? ";

			if (personCache != null) {
				String job = personCache.getMind().getJob().getName(personCache.getGender());
				String article = "";
				if (Conversion.isVowel(job))
					article = "an";
				else
					article = "a";
				responseText.append("I'm ");
				responseText.append(article);
				responseText.append(" ");
				responseText.append(job);
				responseText.append(".");

			} else if (robotCache != null) {
				String job = robotCache.getBotMind().getRobotJob().toString();// .getName(robotCache.getRobotType());
				String article = "";
				if (Conversion.isVowel(job))
					article = "an";
				else
					article = "a";
				responseText.append("I'm ");
				responseText.append(article);
				responseText.append(" ");
				responseText.append(job);
				responseText.append(".");
			}
		}

		else if (num == 3 || text.equalsIgnoreCase("where you from")
				|| text.toLowerCase().contains("nationality")
				|| text.toLowerCase().contains("country")) {
			questionText = YOU_PROMPT + "What country were you from ? ";

			if (personCache != null) {
				responseText.append("I was born in ");
				responseText.append(personCache.getCountry());

			} else if (robotCache != null) {
				responseText.append("I was assembled on ");
				responseText.append(robotCache.getCountry());
			}

		}
	
		else if (num == 4 || text.toLowerCase().contains("outside") || text.toLowerCase().contains("inside")
				|| text.toLowerCase().contains("container")) {
			questionText = YOU_PROMPT + "Are you inside or outside?";
			Unit c = u.getContainerUnit();// getTopContainerUnit();
			if (c != null) {
				responseText.append("I'm inside ").append(c.getName()).append(".");
			}

			else
				responseText.append("I'm outside");// don't have a Top Container unit.";

		}

		else if (num == 5 || text.toLowerCase().contains("where")) {
			questionText = YOU_PROMPT + "Where are you ?"; // what is your Location Situation [Expert Mode only] ?";
			responseText.append("I'm located at ");
			if (personCache != null) {
				responseText.append(Conversion.capitalize(personCache.getLocationTag().getQuickLocation()));// getLocationSituation().getName()));
			} else if (robotCache != null) {
				responseText.append(Conversion.capitalize(robotCache.getLocationTag().getQuickLocation()));// getLocationSituation().getName()));
			} else if (vehicleCache != null) {
				responseText.append(Conversion.capitalize(vehicleCache.getLocationTag().getQuickLocation()));
			}

		}

		else if (num == 6 || text.toLowerCase().contains("located") || text.toLowerCase().contains("location")) {
			questionText = YOU_PROMPT + "What is your exact location ?";
			LocationStateType stateType = null;

			if (personCache != null) {
				stateType = personCache.getLocationStateType();
				responseText.append("I'm ");
				responseText.append(stateType.getName());

				if (personCache.getBuildingLocation() != null) {
					responseText.append(" (");
					responseText.append(personCache.getLocationTag().getExtendedLocations());// .getBuildingLocation().getNickName());
					responseText.append(")");
				}

			} else if (robotCache != null) {
				stateType = robotCache.getLocationStateType();
				responseText.append("I'm ");
				responseText.append(stateType.getName());
				responseText.append(" (");
				responseText.append(robotCache.getLocationTag().getExtendedLocations());// .getBuildingLocation().getNickName());
				responseText.append(")");
			}

			else if (vehicleCache != null) {
				stateType = vehicleCache.getLocationStateType();
				responseText.append("I'm ");
				responseText.append(stateType.getName());

				if (vehicleCache.getBuildingLocation() != null) {
					responseText.append(" (");
					responseText.append(vehicleCache.getLocationTag().getExtendedLocations());// .getBuildingLocation().getNickName());
					responseText.append(")");
				}
			}

		}

		else if (num == 7 || text.toLowerCase().contains("task") 
				|| text.toLowerCase().contains("activity")
				|| text.toLowerCase().contains("doing")
				|| text.toLowerCase().contains("action")) {
			questionText = YOU_PROMPT + "What are you doing ?";
			if (personCache != null) {
				responseText.append(personCache.getTaskDescription());
			} else if (robotCache != null) {
				responseText.append(robotCache.getTaskDescription());
			}

		}

		else if (num == 8 || text.toLowerCase().contains("mission") || text.toLowerCase().contains("trip")
				|| text.toLowerCase().contains("excursion")) {
			// sys = name;
			questionText = YOU_PROMPT + "Are you involved in a particular mission at this moment?";
			Mission mission = null;
			if (personCache != null) {
				mission = personCache.getMind().getMission();
			} else if (robotCache != null) {
				mission = robotCache.getBotMind().getMission();
			} else if (vehicleCache != null) {
				Person p = (Person) vehicleCache.getOperator();
				if (p != null)
					mission = p.getMind().getMission();
//					else
//						mission = "Mission data not available.";
			}

			if (mission == null)
				responseText.append("No. I'm not. ");
			else
				responseText.append(mission.getDescription());

		}

		else if (num == 9 || text.equalsIgnoreCase("building")) {
			questionText = YOU_PROMPT + "What building are you at ?";
			Settlement s = u.getSettlement();
			if (s != null) {
				// Building b1 = s.getBuildingManager().getBuilding(cache);
				Building b = u.getBuildingLocation();
				if (b != null) {// && b1 != null)
					responseText.append("The building I'm in is ").append(b.getNickName());
					// + " (aka " + b1.getNickName() + ").";
				} else
					responseText.append("I'm not in a building.");
			} else
				responseText.append("I'm not in a building.");

		}

		else if (num == 10 || text.equalsIgnoreCase("settlement")) {
			questionText = YOU_PROMPT + "What settlement are you at ?";
			Settlement s = u.getSettlement();
			if (s != null) {
				responseText.append("I'm at ").append(s.getName());
			} else
				responseText.append("I'm not inside a settlement");

		}

		else if (num == 11 || text.equalsIgnoreCase("associated") || text.equalsIgnoreCase("association")
				|| text.equalsIgnoreCase("home") || text.equalsIgnoreCase("home town")) {
			questionText = YOU_PROMPT + "What is your associated settlement ?";
			Settlement s = u.getAssociatedSettlement();
			if (s != null) {
				responseText.append("My associated settlement is ").append(s.getName());
			} else
				responseText.append("I don't have an associated settlement");
		}

//	    	else if (num == 9 || text.equalsIgnoreCase("buried settlement")) {
//	    		questionText = YOU_PROMPT + "What is his/her buried settlement ?";
//	    		if personCache.
//	    		Settlement s = cache.getBuriedSettlement();
//	    		if (s == null) {
//	           		responseText = "The buried settlement is " + s.getName();
//	           		sys = "System : ";
//	       		}
//	       		else
//	       			responseText = "I'm not dead.";
//	    	}

		else if (num == 12 || text.equalsIgnoreCase("vehicle")) {
			questionText = YOU_PROMPT + "What vehicle are you in and where is it ?";
			Vehicle v = u.getVehicle();
			if (v != null) {
				String d = u.getVehicle().getDescription();
				StatusType status = u.getVehicle().getStatus();
				responseText.append("My vehicle is ");
				responseText.append(v.getName());
				responseText.append(" (a ");
				responseText.append(Conversion.capitalize(d));
				responseText.append(" type). It's currently ");
				responseText.append(status.getName().toLowerCase());
				responseText.append(".");
			} else
				responseText.append("I'm not in a vehicle.");
		}

		else if (num == 13 || text.equalsIgnoreCase("vehicle container")
				|| (text.contains("vehicle") && text.contains("container"))
				|| (text.contains("vehicle") && text.contains("inside"))) {
			questionText = YOU_PROMPT + "Where is your vehicle at?";// 's container unit ?";
			Vehicle v = personCache.getVehicle();
			if (v != null) {
				Unit c = v.getContainerUnit();
				if (c != null) {
					responseText.append("My vehicle is at ");
					responseText.append(c.getName());
				}

				else
					responseText.append("My vehicle is not inside");// doesn't have a container unit.";

			} else
				responseText.append("I'm not in a vehicle.");
		}

		else if (num == 14
				|| (text.contains("vehicle") && text.contains("outside"))
				|| (text.contains("vehicle") && text.contains("top") && text.contains("container"))) {
			questionText = YOU_PROMPT + "What is your vehicle located?";// 's top container unit ?";
			Vehicle v = u.getVehicle();
			if (v != null) {
				Unit tc = v.getTopContainerUnit();
				if (tc != null) {
					responseText.append("My vehicle is at ");
					responseText.append(tc.getName());
				} else
					responseText.append("My vehicle is not inside");// doesn't have a top container unit.";
			} else
				responseText.append("I'm not in a vehicle.");
		}

		else if (num == 15 || text.equalsIgnoreCase("garage")
				|| (text.contains("vehicle") && text.contains("park"))) {
			questionText = YOU_PROMPT + "What building does your vehicle park at ?";
			Vehicle v = u.getVehicle();
			if (v != null) {
				Settlement s = v.getSettlement();
				if (s != null) {
					Building b = s.getBuildingManager().getBuilding(v);
					if (b != null) {
						responseText.append("My vehicle is parked inside ");
						responseText.append(b.getNickName());
					}

					else
						responseText.append("My vehicle does not park inside a building/garage");
				} else
					responseText.append("My vehicle is not at a settlement.");
			} else
				responseText.append("I'm not on a vehicle.");
		}

		else if (num == 16 || (text.contains("vehicle") && text.contains("settlement"))) {
			questionText = YOU_PROMPT + "What settlement is your vehicle located at ?";
			Vehicle v = u.getVehicle();
			if (v != null) {
				Settlement s = v.getSettlement();
				if (s != null) {
					responseText.append("My vehicle is at ");
					responseText.append(s.getName());
				} else
					responseText.append("My vehicle is not at a settlement.");
			} else
				responseText.append("I'm not on a vehicle.");
		}

		else if (num == 17 || text.equalsIgnoreCase("bed") 
				|| text.contains("quarters")) {
			questionText = YOU_PROMPT + "Where is your designated quarters ? ";
			Point2D bed = personCache.getBed();
			if (bed == null) {
				if (personCache != null) {
					responseText.append("I haven't got my own private quarters yet.");
				} else if (robotCache != null) {
					responseText.append("I don't need one. My battery can be charged at any robotic station.");
				}
			} else {
				if (personCache != null) {
					Settlement s1 = personCache.getSettlement();
					if (s1 != null) {
						// check to see if a person is on a trading mission
						Settlement s2 = personCache.getAssociatedSettlement();
						if (s2 != null) {
							responseText.append("My designated quarters is at (");
							responseText.append(bed.getX());
							responseText.append(", ");
							responseText.append(bed.getY());
							responseText.append(") in ");
							responseText.append(personCache.getQuarters());
							responseText.append(" at ");

							if (s1 == s2) {
								responseText.append(s1);
							}

							else {
								// yes, a person is on a trading mission
								responseText.append(s2);
							}
						}
					} else {
						responseText.append("My designated quarters is at (");
						responseText.append(bed.getX());
						responseText.append(", ");
						responseText.append(bed.getY());
						responseText.append(") in ");
						responseText.append(personCache.getQuarters());
						responseText.append(" at ");
					}
				}

				else if (robotCache != null) {
					responseText.append("I don't need one. My battery can be charged at any robotic station.");
				}
			}
		}

		else if (num == 18 || text.contains("sleep hour") || text.contains("bed time")) {
			questionText = YOU_PROMPT + "What is your preferred or usual bed time ?";

			int[] twos = ((Person) u).getCircadianClock().getPreferredSleepHours();
			int small = Math.min(twos[0], twos[1]);
			int large = Math.max(twos[0], twos[1]);

			responseText.append("My preferred sleep hours are at either " + small + " or " + large + " millisols.");

		}

		else if (text.equalsIgnoreCase("key") 
				|| text.equalsIgnoreCase("keys")  
				|| text.equalsIgnoreCase("keyword")  
				|| text.equalsIgnoreCase("keywords")  
				|| text.equalsIgnoreCase("/k")) {

			questionText = REQUEST_KEYS;
			if (connectionMode == 0) {
				keywordText = KEYWORDS_TEXT;
			} else {
				keywordText = KEYWORDS_TEXT + KEYWORDS_HEIGHT;
			}
			responseText.append(System.lineSeparator());
			responseText.append(keywordText);

		}

		else if (text.equalsIgnoreCase("help") || text.equalsIgnoreCase("/h") || text.equalsIgnoreCase("/?")
				|| text.equalsIgnoreCase("?")) {
			questionText = REQUEST_HELP;
			if (connectionMode == 0) {
				helpText = HELP_TEXT;
			} else {
				helpText = HELP_TEXT + HELP_HEIGHT;
			}
			responseText.append(System.lineSeparator());
			responseText.append(helpText);

		}

		// Add changing the height of the chat box
		// DELETED

		else {
			String[] txt = clarify(name);
			questionText = txt[0];
			responseText.append(txt[1]);
		}

		return new String[] { questionText, responseText.toString() };
	}

	/**
	 * Processes the question and return the answer regarding an unit
	 * 
	 * @param text
	 * @return an array of String
	 */
	public static String[] askQuestion(String text) {
//		System.out.println("askQuestion() in ChatUtils");

		String questionText = "";
		StringBuilder responseText = new StringBuilder();
		String name = SYSTEM;
		int cacheType = -1;
		
		Unit u = null;

		if (personCache != null) {
			u = personCache;
			name = personCache.getName();
			cacheType = 0;
		}

		else if (robotCache != null) {
			u = robotCache;
			name = robotCache.getName();
			cacheType = 1;
		}

		else if (settlementCache != null) {
			u = settlementCache;
			name = settlementCache.getName();
			cacheType = 2;
		}

		else if (vehicleCache != null) {
			u = vehicleCache;
			name = vehicleCache.getName();
			cacheType = 3;
		}

		// Case 0 : exit the conversation
		if (isQuitting(text)) {
			String[] bye = null;

			if (u != null) {
				if (cacheType == 0 || cacheType == 1)
					bye = farewell(name, true);
				else
					bye = farewell(name, false);
				
				questionText = bye[0];
				responseText.append(bye[1]);
				responseText.append(System.lineSeparator());
				responseText.append(System.lineSeparator());
				responseText.append(name);

				if (settlementCache != null || vehicleCache != null) {
					responseText.append(" is disconnected from the line.");
				}
				
				else {
					int rand1 = RandomUtil.getRandomInt(1);
	
					if (rand1 == 0)
						responseText.append(" has left the conversation.");
					else
						responseText.append(" just hung up the line.");
				}
				
				// set personCache and robotCache to null so as to quit the conversation
				personCache = null;
				robotCache = null;
				settlementCache = null;
				vehicleCache = null;
			}

			else {
				bye = farewell(name, false);
				questionText = bye[0];
				responseText.append(bye[1]);
				responseText.append(System.lineSeparator());
			}

		}
		
		else if (checkExpertMode(text)) {
			toggleExpertMode();
			responseText.append("Set Expert Mode to " + ChatUtils.isExpertMode());
//			responseText.append(System.lineSeparator());
		}

		// Add proposals
//		else if (text.equalsIgnoreCase("/p")) {
//			System.out.println("/p is submitted");
//			questionText = "Below is a list of proposals for your review :";
//			responseText.append(SYSTEM_PROMPT);
//			responseText.append("1. Safety and Health Measures");
//			responseText.append("2. Manufacturing Priority");
//			responseText.append("3. Food Allocation Plan");
//		}

		// Add changing the height of the chat box
		// DELETED

		// Case 1: ask about a particular settlement
		else if (settlementCache != null) {

			personCache = null;
			robotCache = null;
			//settlementCache = null;
			vehicleCache = null;
			
			if (isInteger(text, 10)) {

				int num = Integer.parseUnsignedInt(text, 10);

				String[] ans = askSettlementNum(num);

				questionText = ans[0];
				responseText.append(ans[1]);

				// if it's not a integer input
			}

			else {
				
				String[] ans = askSettlementStr(text, name);

				questionText = ans[0];
				responseText.append(ans[1]);
			}

		}

		// Case 2: ask to talk to a person or robot
		else if (settlementCache == null) {
			// Note : this is better than personCache != null || robotCache != null since it
			// can
			// incorporate help and other commands
			int num = -1;

			if (isInteger(text, 10))
				num = Integer.parseUnsignedInt(text, 10);

			// Add command "die"
			if (text.equalsIgnoreCase("die")) {

				if (personCache != null) {
					questionText = YOU_PROMPT + "I hereby pronounce you dead.";

					String lastWord = null;

					int rand = RandomUtil.getRandomInt(12);
					// Quotes from http://www.phrases.org.uk/quotes/last-words/suicide-notes.html
					// https://www.goodreads.com/quotes/tag/suicide-note
					if (rand == 0)
						lastWord = "This is all too heartbreaking for me. Farewell, my friend.";
					else if (rand == 1)
						lastWord = "Things just seem to have gone too wrong too many times...";
					else if (rand == 2)
						lastWord = "So I leave this world, where the heart must either break or turn to lead.";
					else if (rand == 3)
						lastWord = "Let's have no sadness——furrowed brow. There's nothing new in dying now. Though living is no newer.";
					else if (rand == 4)
						lastWord = "I myself——in order to escape the disgrace of deposition or capitulation——choose death.";
					else if (rand == 5)
						lastWord = "When all usefulness is over, when one is assured of an unavoidable and imminent death, "
								+ "it is the simplest of human rights to choose a quick and easy death in place of a slow and horrible one. ";
					else if (rand == 6)
						lastWord = "I am going to put myself to sleep now for a bit longer than usual. Call it Eternity.";
					else if (rand == 7)
						lastWord = "All fled——all done, so lift me on the pyre; the feast is over, and the lamps expire.";
					else if (rand == 8)
						lastWord = "No more pain. Wake no more. Nobody owns.";
					else if (rand == 9)
						lastWord = "Dear World, I am leaving because I feel I have lived long enough. I am leaving you with your worries in this sweet cesspool. Good luck.";
					else if (rand == 10)
						lastWord = "This is what I want so don't be sad.";
					else if (rand == 11)
						lastWord = "I don't want to hurt you or anybody so please forget about me. Just try. Find yourself a better friend.";
					else
						lastWord = "They tried to get me——I got them first!";

					responseText.append(lastWord);

					personCache.setLastWord(lastWord);

					personCache.getPhysicalCondition()
							.setDead(new HealthProblem(new Complaint(ComplaintType.SUICIDE), personCache), true);

					personCache = null;
					robotCache = null;
					settlementCache = null;
					vehicleCache = null;
				}
			}

			else {
				
				String[] ans = askPersonRobot(text, num, name, u);
				
				questionText = ans[0];
				responseText.append(ans[1]);
			}
		}

		else {
			// set personCache and robotCache to null only if you want to quit the
			// conversation
			String[] txt = clarify(name);
			questionText = txt[0];
			responseText.append(txt[1]);
		}

		return new String[] { questionText, responseText.toString() };

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
	
	public static void setDebugLevel(Level newLvl) {
		// Java 8 stream
//		Arrays.stream(LogManager.getLogManager().getLogger("").getHandlers()).forEach(h -> h.setLevel(newLvl));
		
	    Logger rootLogger = LogManager.getLogManager().getLogger("");
	    Handler[] handlers = rootLogger.getHandlers();
	    rootLogger.setLevel(newLvl);
	    for (Handler h : handlers) {
	        if (h instanceof ConsoleHandler)
	            h.setLevel(newLvl);
	    }
	}
	
	/*
	 * Asks the system a question
	 * 
	 * @param input text
	 */
	public static String askSystem(String text) {
		StringBuilder responseText = new StringBuilder();

		// String SYSTEM_PROMPT = "System : ";
		boolean available = true;
		int nameCase = 0;
		boolean proceed = false;

//		Unit unit = null;
		Person person = null;
		Robot robot = null;

		// System.out.println("A: text is " + text + ". Running parseText()");
		text = text.trim();
		int len = text.length();

//		List<Person> personList = new ArrayList<>();
//		List<Robot> robotList = new ArrayList<>();

		// Detect "\" backslash and the name that follows
		if (len >= 3 && text.substring(0, 1).equalsIgnoreCase("\\")) {
			text = text.substring(1, len).trim();
			proceed = true;
		}

		else if (text.toLowerCase().contains("log")) {			
			
			if (text.equalsIgnoreCase("log")) {		
				
				Level level = LogManager.getLogManager().getLogger("").getLevel();
				
				responseText.append("Current logging level is : " + level);
				responseText.append(System.lineSeparator());
				responseText.append(System.lineSeparator());

//				SEVERE (highest value)
//				WARNING
//				INFO
//				CONFIG
//				FINE
//				FINER
//				FINEST (lowest value)
				
//				responseText.append(System.lineSeparator());
				responseText.append("Please specify the Logging Level as follows : ");
				responseText.append(System.lineSeparator());
				responseText.append(" 1. log off: for turning off logging.");
				responseText.append(System.lineSeparator());
				responseText.append(" 2. log severe : for indicating a serious failure.");
				responseText.append(System.lineSeparator());
				responseText.append(" 3. log warning : for indicating a potential problem.");
				responseText.append(System.lineSeparator());
				responseText.append(" 4. log info : for informational messages.");
				responseText.append(System.lineSeparator());
				responseText.append(" 5. log config : for static configuration messages.");
				responseText.append(System.lineSeparator());
				responseText.append(" 6. log fine : for providing tracing information.");
				responseText.append(System.lineSeparator());
				responseText.append(" 7. log finer : for providing fairly detailed tracing information.");
				responseText.append(System.lineSeparator());
				responseText.append(" 8. log finest : for providing highly detailed tracing information.");
				responseText.append(System.lineSeparator());
				responseText.append(" 9. log all : for indicating that all messages be logged.");
				
				responseText.append(System.lineSeparator());				
				responseText.append(System.lineSeparator());
				responseText.append("See https://docs.oracle.com/javase/8/docs/api/java/util/logging/Level.html");
				
					
				responseText.append(System.lineSeparator());
				responseText.append(System.lineSeparator());
				responseText.append("e.g. Type 'log info' at the prompt to set it to the INFO level");
//				responseText.append(System.lineSeparator());
				
				return responseText.toString();						
			}
			
			if (text.equalsIgnoreCase("log off")) {			
				
				setDebugLevel(Level.OFF);
				
//				responseText.append(System.lineSeparator());
				responseText.append("Logging is set to OFF");	
				
				return responseText.toString();						
			}
			
			
			else if (text.equalsIgnoreCase("log config")) {			
				
				setDebugLevel(Level.CONFIG);
				
//				responseText.append(System.lineSeparator());
				responseText.append("Logging is set to CONFIG");	
				
				return responseText.toString();						
			}


			
			else if (text.equalsIgnoreCase("log warning")) {			
				
				setDebugLevel(Level.WARNING);
				
//				responseText.append(System.lineSeparator());
				responseText.append("Logging is set to WARNING");	
				
				return responseText.toString();						
			}
			
			else if (text.equalsIgnoreCase("log fine")) {			
				
				setDebugLevel(Level.FINE);
				
//				responseText.append(System.lineSeparator());
				responseText.append("Logging is set to FINE");	
				
				return responseText.toString();						
			}
			
			else if (text.equalsIgnoreCase("log finer")) {			
				
				setDebugLevel(Level.FINER);
				
//				responseText.append(System.lineSeparator());
				responseText.append("Logging is set to FINER");	
				
				return responseText.toString();						
			}
			
			else if (text.equalsIgnoreCase("log finest")) {			
				
				setDebugLevel(Level.FINEST);
				
//				responseText.append(System.lineSeparator());
				responseText.append("Logging is set to FINEST");	
				
				return responseText.toString();						
			}
			
			else if (text.equalsIgnoreCase("log severe")) {			
				
				setDebugLevel(Level.SEVERE);
				
//				responseText.append(System.lineSeparator());
				responseText.append("Logging is set to SEVERE");	
				
				return responseText.toString();
			}
			
			else if (text.equalsIgnoreCase("log info")) {			
				
				setDebugLevel(Level.INFO);
						
//				responseText.append(System.lineSeparator());
				responseText.append("Logging is set to INFO");
				
				return responseText.toString();
			}
					
			else if (text.equalsIgnoreCase("log all")) {			
				
				setDebugLevel(Level.ALL);
				
//				responseText.append(System.lineSeparator());
				responseText.append("Logging is set to ALL");	
				
				return responseText.toString();						
			}
		}
		
		else if (text.equalsIgnoreCase("check size")) {			

			int missionSol = marsClock.getMissionSol();
			String marsTime = marsClock.getDecimalTimeString();
//			int num = 20 - s0.length();
//			for (int i=0; i<num; i++) {
//				responseText.append(" ");
//			}
			responseText.append("  Core Engine : r" + Simulation.BUILD);
			responseText.append(System.lineSeparator());
			responseText.append("   # Settlers : " + sim.getUnitManager().getTotalNumPeople());
			responseText.append(System.lineSeparator());
			responseText.append("  Mission Sol : " + missionSol);
			responseText.append(System.lineSeparator());
			responseText.append(" Martian Time : " + marsTime) ;
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());
			responseText.append(Simulation.instance().printObjectSize());
			
			return responseText.toString();
		}
		
		else if (text.toLowerCase().contains("relationship")
				|| text.toLowerCase().contains("relation")
				|| text.toLowerCase().contains("social")) {

			double ave = 0;
			Map<Double, String> map = new HashMap<>();
//			List<String> list = new ArrayList<>();
			List<Double> scores = new ArrayList<>();
			Collection<Settlement> col = sim.getUnitManager().getSettlements();
			for (Settlement s : col) {
				double score = getRelationshipScore(s);
				ave += score;
//				list.add(s.getName());
				scores.add(score);
				map.put(score, s.getName());
			}	
			int size = scores.size();
			ave = ave / size;
			
			responseText.append("Current overall social score for all " + size + " settlements : " + fmt1.format(ave));
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());
			
			responseText.append("   Rank | Score | Settlement");
			responseText.append(System.lineSeparator());
			responseText.append(" -----------------------------------");
			responseText.append(System.lineSeparator());
			
			scores.sort((Double d1, Double d2) -> -d1.compareTo(d2)); 
			
			for (int i=0; i<size; i++) {
				double score = scores.get(i);
				String space = "";
				String scoreStr = score + "";
				int num = scoreStr.length();
				if (num == 2)
					space = "   ";
				else if (num == 3)
					space = "  ";
				else if (num == 4)
					space = " ";
				else if (num == 5)
					space = "";
				
				String name = map.get(scores.get(i));			
				responseText.append("    #" + (i+1) + "    " + space + fmt1.format(score) + "    " + name );
				// Note : remove the pair will prevent the case when when 2 or more settlements have the exact same score from reappearing
				map.remove(score, name);
				responseText.append(System.lineSeparator());
			}
			
//			responseText.append(System.lineSeparator());
			
//			map.entrySet().stream()
//			   .sorted(Map.Entry.comparingByValue())
//			   .forEach(System.out::println);
			
			return responseText.toString();
		}
		
		else if (text.toLowerCase().contains("time")) {
			
			responseText.append(SYSTEM_PROMPT);
			responseText.append("Here's the latest time info.");
			responseText.append(System.lineSeparator());
			
			responseText.append(printTime());
			
			// Life Support System 
			
			// Resource Storage
			
			// Goal
			
			// Resource changes
			
			// Water Ration 
					
			return responseText.toString();
		}
		
		else if (text.equalsIgnoreCase("key") 
				|| text.equalsIgnoreCase("keys")  
				|| text.equalsIgnoreCase("keyword")  
				|| text.equalsIgnoreCase("keywords")  
				|| text.equalsIgnoreCase("/k")) {

			// responseText.append(System.lineSeparator());
			if (connectionMode == 0) {
				keywordText = KEYWORDS_TEXT;
			} else {
				keywordText = KEYWORDS_TEXT + KEYWORDS_HEIGHT;
			}
			responseText.append(keywordText);
			return responseText.toString();
		}

		else if (text.equalsIgnoreCase("help") || text.equalsIgnoreCase("/h") || text.equalsIgnoreCase("/?")
				|| text.equalsIgnoreCase("?")) {

			// responseText.append(System.lineSeparator());
			if (connectionMode == 0) {
				helpText = HELP_TEXT;
			} else {
				helpText = HELP_TEXT + HELP_HEIGHT;
			}
			responseText.append(helpText);
			return responseText.toString();
		}

		// Add proposals
		else if (text.equalsIgnoreCase("/p")) {
//			System.out.println("/p is submitted");
			responseText.append(System.lineSeparator());
			responseText.append(SYSTEM_PROMPT);
			responseText.append("[EXPERIMENTAL & NON-FUNCTIONAL] Below is a list of proposals for your review :");
			responseText.append(System.lineSeparator());
			responseText.append("1. Safety and Health Measures");
			responseText.append(System.lineSeparator());
			responseText.append("2. Manufacturing Priority");
			responseText.append(System.lineSeparator());
			responseText.append("3. Food Allocation Plan");
			responseText.append(System.lineSeparator());
			return responseText.toString();
		}

		// Add asking about settlements in general
		else if (text.toLowerCase().contains("settlement")) {

			// questionText = YOU_PROMPT + "What are the names of the settlements ?";

			// Creates an array with the names of all of settlements
			List<Settlement> settlementList = new ArrayList<Settlement>(
					Simulation.instance().getUnitManager().getSettlements());

			int num = settlementList.size();
			String s = "";

			if (num > 2) {
				for (int i = 0; i < num; i++) {
					if (i == num - 2)
						s = s + settlementList.get(i) + ", and ";
					else if (i == num - 1)
						s = s + settlementList.get(i) + ".";
					else
						s = s + settlementList.get(i) + ", ";
				}
				responseText.append(SYSTEM_PROMPT);
				responseText.append("There is a total of ");
				responseText.append(num);
				responseText.append(" settlements : ");
				responseText.append(s);
				return responseText.toString();
			}

			else if (num == 2) {
				s = settlementList.get(0) + " and " + settlementList.get(1);
				responseText.append(SYSTEM_PROMPT);
				responseText.append("There is a total of ");
				responseText.append(num);
				responseText.append(" settlements : ");
				responseText.append(s);
				return responseText.toString();
			}

			else if (num == 1) {
				responseText.append(SYSTEM_PROMPT);
				responseText.append("There is just one settlement : ");
				responseText.append(settlementList.get(0));
				return responseText.toString();
			}

			else {
				responseText.append(SYSTEM_PROMPT);
				responseText.append("Currently, there is no settlement established on Mars.");
				return responseText.toString();
			}

		}

		// Add asking about vehicles in general
		else if (text.toLowerCase().contains("vehicle") || text.toLowerCase().contains("rover")) {
			// questionText = YOU_PROMPT + "What are the names of the vehicles ?";
			responseText.append(SYSTEM_PROMPT);
			responseText.append("Here's the roster list of vehicles in each settlement.");
			responseText.append(System.lineSeparator());
			
			// Creates an array with the names of all of settlements
			List<Settlement> settlementList = new ArrayList<Settlement>(
					Simulation.instance().getUnitManager().getSettlements());

			for (Settlement s : settlementList) {
				Collection<Vehicle> list = s.getAllAssociatedVehicles();
				
				responseText.append(DASHES);
				responseText.append(System.lineSeparator());
				int num = (DASHES.length() - s.getName().length())/2;
				if (num > 0) {
					for (int i=0; i<num; i++) {
						responseText.append(" ");
					}
				}
				
				responseText.append(s.getName());
				responseText.append(System.lineSeparator());
				responseText.append(DASHES);
				responseText.append(System.lineSeparator());
				
				List<Vehicle> vlist = list.stream()
						.sorted((p1, p2)-> p1.getVehicleType().compareTo(p2.getVehicleType()))
						.collect(Collectors.toList());
				
				for (Vehicle v : vlist) {
					responseText.append(v.getName());
					int num2 = 25 - v.getName().length();
					if (num2 > 0) {
						for (int i=0; i<num2; i++) {
							responseText.append(" ");
						}
					}
					responseText.append(v.getVehicleType());
					responseText.append(System.lineSeparator());
				}
			
				responseText.append(System.lineSeparator());
			}

			return responseText.toString();
		}

		else if (len >= 5 && text.substring(0, 5).equalsIgnoreCase("hello")) {
			if (len > 5) {
				text = text.substring(5, len);
				text = text.trim();
				proceed = true;
			} else {
				responseText.append(SYSTEM_PROMPT);
				responseText.append("Hello, how can I help?    [/h for help]");
			}
			
			return responseText.toString();
		}

		else if (len >= 3 && text.substring(0, 3).equalsIgnoreCase("hey")) {

			if (len > 3) {
				text = text.substring(3, len);
				text = text.trim();
				proceed = true;
			} else {
				responseText.append(SYSTEM_PROMPT);
				responseText.append("Hello, how can I help?    [/h for help]");
			}
			
			return responseText.toString();
		}

		else if (len >= 2 && text.substring(0, 2).equalsIgnoreCase("hi")) {

			if (len > 2) {
				text = text.substring(2, len);
				text = text.trim();
				proceed = true;
			} else {
				responseText.append(SYSTEM_PROMPT);
				responseText.append("Hello, how can I help?    [/h for help]");
			}

			return responseText.toString();
		}

		else if (len >= 1) {
			proceed = true;
		}

		// Part 2 //

		if (len == 0 || text == null) {// || text.length() == ) {
			responseText.append(clarify(SYSTEM)[1]);

		}

		else if (proceed) { // && text.length() > 1) {
//			System.out.println("proceed is true: text is " + text);

			List<Person> personList = new ArrayList<>();
			List<Robot> robotList = new ArrayList<>();
			
			// person and robot
			Iterator<Settlement> i = Simulation.instance().getUnitManager().getSettlements().iterator();
			while (i.hasNext()) {
				Settlement settlement = i.next();
				// Check if anyone has this name (as first/last name) in any settlements
				// and if he/she is still alive
				if (text.contains("bot") || text.contains("Bot")) {
					// Check if it is a bot
					robotList.addAll(settlement.returnRobotList(text));
				} else {
					personList.addAll(settlement.returnPersonList(text));

				}

				if (personList.size() != 0)
					nameCase = personList.size();
				else
					nameCase = robotList.size();

				// System.out.println("nameCase is " + nameCase);
			}

			// System.out.println("total nameCase is " + nameCase);

			// capitalize the first initial of a name
			text = Conversion.capitalize(text);

			// Case 1: more than one person with that name
			if (nameCase >= 2) {
				responseText.append(SYSTEM_PROMPT);
				responseText.append("There are more than one \"");
				responseText.append(text);
				responseText.append("\". Would you be more specific?");
				// System.out.println(responseText);
				return responseText.toString();
				
			// Case 2: there is one person
			} else if (nameCase == 1) {

				if (!available) {
					// TODO: check if the person is available or not (e.g. if on a mission and comm
					// broke down)
					responseText.append(SYSTEM_PROMPT);
					responseText.append("I'm sorry. ");
					responseText.append(text);
					responseText.append(" is unavailable at this moment");
					return responseText.toString();

				} else {
//					System.out.println("personList's size : " + personList.size());
//					System.out.println("personList : " + personList);
					if (!personList.isEmpty()) {
						person = personList.get(0);
						if (person.getPhysicalCondition().isDead()) {
							// Case 4: passed away
							int rand = RandomUtil.getRandomInt(1);
							if (rand == 0) {
								responseText.append(SYSTEM_PROMPT);
								responseText.append("I'm sorry. ");
								responseText.append(text);
								responseText.append(" has passed away and is buried at ");
								responseText.append(person.getBuriedSettlement().getName());
							} else {
								responseText.append(SYSTEM_PROMPT);
								responseText.append("Perhaps you don't know that ");
								responseText.append(text);
								responseText.append(" is dead and is buried at ");
								responseText.append(person.getBuriedSettlement().getName());
							}
							return responseText.toString();
						}
						else {
							personCache = person;
//							unitCache = person;
						}
					}

					if (!robotList.isEmpty()) {
						robot = robotList.get(0);
						if (robot.getSystemCondition().isInoperable()) {
							// Case 4: decomissioned
							responseText.append(SYSTEM_PROMPT);
							responseText.append("I'm sorry. ");
							responseText.append(text);
							responseText.append(" has been decomissioned.");
							return responseText.toString();
						} else {
							robotCache = robot;
//							unitCache = robot;
						}
					}

					if (robotCache != null) {
						responseText.append(robotCache.getName());
						responseText.append(" : This is ");
						responseText.append(text);
						responseText.append(". ");
						return responseText.toString();

					}

					else if (personCache != null) {
						responseText.append(personCache.getName());
						responseText.append(" : This is ");
						responseText.append(text);					
						responseText.append(". ");
						return responseText.toString();
					}
				}

				// Case 3: doesn't exist, check settlement's name
			} else if (nameCase == 0) {

				// System.out.println("nameCase is 0");
				// Match a settlement's name
				if (text.length() > 1) {
					boolean notMatched = true;
					Iterator<Settlement> j = Simulation.instance().getUnitManager().getSettlements().iterator();
					while (j.hasNext()) {
						Settlement settlement = j.next();
						String s_name = settlement.getName();

						if (s_name.equalsIgnoreCase(text.toLowerCase())) {
							// name = "System";
							responseText.append(SYSTEM_PROMPT);
							responseText.append("You are now connected with ");
							responseText.append(s_name);
							responseText.append(". What would like to know ?");

							settlementCache = settlement;
							// System.out.println("matching settlement name " + s_name);
							return responseText.toString();
						}

						else if (s_name.toLowerCase().contains(text.toLowerCase())) {
							responseText.append(SYSTEM_PROMPT);
							responseText.append("Do you mean '");
							responseText.append(s_name);
							responseText.append("' or something else ?");
							// System.out.println("partially matching settlement name " + s_name);
							return responseText.toString();
						} 
						

						// TODO: check vehicle names
						// TODO: check commander's name
					}
						
					if (notMatched) {
						responseText.append(SYSTEM_PROMPT);
						responseText.append("I do not recognize any person or settlement by the name of '");
						responseText.append(text);
						responseText.append("'.");
						return responseText.toString();
					}

				}
				
				else {
					responseText.append(SYSTEM_PROMPT);
					responseText.append("I do not recognize any person or settlement by the name of '");
					responseText.append(text);
					responseText.append("'.");
					return responseText.toString();
				}
			}

			else {
				responseText.append(SYSTEM_PROMPT);
				responseText.append("I do not recognize any person or settlement by the name of '");
				responseText.append(text);
				responseText.append("'.");
				return responseText.toString();
			}
		}

		responseText.append(SYSTEM_PROMPT);
		responseText.append("I do not recognize any person or settlement by the name of '");
		responseText.append(text);
		responseText.append("'.");
		return responseText.toString();
	}

	/**
     * Generates and prints the list that needs to be processed
     * 
     * @param indoorP
     * @return String
     */
    public static StringBuilder printList(List<?> indoorP) {
      	StringBuilder sb = new StringBuilder();
      	
    	if (indoorP.isEmpty()) {
    		sb.append("    None");
    		sb.append(System.lineSeparator());
    		return sb;
    	}
    		
      	List<String> list = new ArrayList<>();
      	for (Object o : indoorP) {
      		list.add(o.toString());
      	}
      	
    	StringBuffer s = new StringBuffer();
    	int SPACES = 22;
    	//int row = 0;
        for (int i=0; i< list.size(); i++) {
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
	        	num = SPACES - list.get(i-1).toString().length();

	        	// Handle the extra space before the parenthesis
	            for (int j=0; j < num; j++) { 
	            	s.append(" ");
	            }    			
    		}

        	if (i+1 < 10)
        		s.append(" ");
        	s.append("(");
        	s.append(i+1);
        	s.append("). ");
        	s.append(c);        		
            
            // if this is the last column
            if (column == 2 || i == list.size()-1) {
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
