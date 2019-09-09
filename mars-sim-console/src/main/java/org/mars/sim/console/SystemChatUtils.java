/**
 * Mars Simulation Project
 * SystemChatUtils.java
 * @version 3.1.0 2019-09-03
 * @author Manny Kung
 */

package org.mars.sim.console;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.health.Complaint;
import org.mars_sim.msp.core.person.health.ComplaintType;
import org.mars_sim.msp.core.person.health.DeathInfo;
import org.mars_sim.msp.core.person.health.HealthProblem;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class SystemChatUtils extends ChatUtils {

	private static Logger logger = Logger.getLogger(SystemChatUtils.class.getName());

	private static String target = "";
	
	/**
	 * Processes the question and return the answer regarding an unit
	 * 
	 * @param text
	 * @return an array of String
	 */
	public static String[] askQuestion(String text) {
//		System.out.println("askQuestion() in SystemChatUtils");
		String questionText = "";
		StringBuffer responseText = new StringBuffer();
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
		
		if (!target.equalsIgnoreCase("") && !target.equalsIgnoreCase(text)) {
			List<String> properNames = ChatUtils.createProperNounsList();
			for (String s : properNames) {
				if (s.equalsIgnoreCase(text)) {
//					System.out.println("text is " + text);				
					// Switch the target of the conversation to this unit with the name "text"			
					String response = askSystem(text).toString();			
//					// Print the new connection status line 			
		        	questionText = "Disconnecting from " + name + ". Connecting with " + target + "..."
		        			+ System.lineSeparator();        			
		        	return new String[] {questionText, response};//responseText.toString()};			
				}
			}
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

				if (settlementCache != null || vehicleCache != null || robotCache != null) {
					responseText.append(" is disconnected from the line.");
				}

				else {
					int rand1 = RandomUtil.getRandomInt(1);

					if (rand1 == 0)
						responseText.append(" has left the conversation.");
					else if (rand1 == 1)
						responseText.append(" just hung up.");
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

		// Case 0: ask about a particular settlement
		else if (settlementCache != null) {

			personCache = null;
			robotCache = null;
			// settlementCache = null;
			vehicleCache = null;

			if (isInteger(text, 10)) {

				int num = Integer.parseUnsignedInt(text, 10);

				String[] ans = SettlementChatUtils.askSettlementNum(num);

				try {
					questionText = ans[0];
					responseText.append(ans[1]);
				} catch (NullPointerException ne) {
					ne.printStackTrace();
				}

				// if it's not a integer input
			}

			else {

				String[] ans = SettlementChatUtils.askSettlementStr(text, name);

				try {
					questionText = ans[0];
					responseText.append(ans[1]);
				} catch (NullPointerException ne) {
					ne.printStackTrace();
				}
			}

		}

		// Case 1: ask about a particular vehicle
		else if (vehicleCache != null) {

			personCache = null;
			robotCache = null;
			settlementCache = null;
//			vehicleCache = null;

			String[] ans = VehicleChatUtils.askVehicle(text, name);

			try {
				questionText = ans[0];
				responseText.append(ans[1]);
			} catch (NullPointerException ne) {
				ne.printStackTrace();
			}

		}

		// Case 2: ask to talk to a person or robot
		else if (settlementCache == null) {
			// Note : this is better than personCache != null || robotCache != null since it
			// can
			// incorporate help and other commands
			int num = -1;

//			System.out.println("settlementCache == null");

			if (isInteger(text, 10)) {
				num = Integer.parseUnsignedInt(text, 10);
			}

			// Add command "die"
			if (expertMode && text.equalsIgnoreCase("die")) {

				if (personCache != null) {
					questionText = YOU_PROMPT + " I hereby pronounce you dead.";

					if (personCache.isOutside()) {
						responseText
								.append("Can you tell me why? Let's wait till I'm done with my task and/or mission.");
					} else {
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
							lastWord = "Let's have no sadness —— furrowed brow. There's nothing new in dying now. Though living is no newer.";
						else if (rand == 4)
							lastWord = "I myself —— in order to escape the disgrace of deposition or capitulation —— choose death.";
						else if (rand == 5)
							lastWord = "When all usefulness is over, when one is assured of an unavoidable and imminent death, "
									+ "it is the simplest of human rights to choose a quick and easy death in place of a slow and horrible one. ";
						else if (rand == 6)
							lastWord = "I am going to put myself to sleep now for a bit longer than usual. Call it Eternity.";
						else if (rand == 7)
							lastWord = "All fled —— all done, so lift me on the pyre; the feast is over, and the lamps expire.";
						else if (rand == 8)
							lastWord = "No more pain. Wake no more. Nobody owns.";
						else if (rand == 9)
							lastWord = "Dear World, I am leaving because I feel I have lived long enough. I am leaving you with your worries in this sweet cesspool. Good luck.";
						else if (rand == 10)
							lastWord = "This is what I want so don't be sad.";
						else if (rand == 11)
							lastWord = "I don't want to hurt you or anybody so please forget about me. Just try. Find yourself a better friend.";
						else
							lastWord = "They tried to get me —— I got them first!";

						responseText.append(personCache.getName() + " : " + lastWord);

						responseText.append(System.lineSeparator() + System.lineSeparator() + personCache.getName()
								+ " committed suicide as instructed.");

						personCache.getPhysicalCondition().setDead(
								new HealthProblem(new Complaint(ComplaintType.SUICIDE), personCache), true, lastWord);

						personCache = null;
						robotCache = null;
						settlementCache = null;
						vehicleCache = null;
					}
				}
			}

			else {
				// if not using expert mode

//				System.out.println("before askPersonRobot()");

				String[] ans = PersonRobotChatUtils.askPersonRobot(text, num, name, u);

				try {
					questionText = ans[0];
					responseText.append(ans[1]);
				} catch (NullPointerException ne) {
					ne.printStackTrace();
				}
//				System.out.println("after askPersonRobot()");
			}
		}

		else {
			// set personCache and robotCache to null only if you want to quit the
			// conversation
			String[] txt = clarify(name);
			questionText = txt[0];
			responseText.append(txt[1]);
		}

		return new String[] { questionText, responseText.toString()};
	}
	
	/*
	 * Asks the system a question
	 * 
	 * @param input text
	 */
	public static String askSystem(String text) {
		StringBuffer responseText = new StringBuffer();

//		boolean available = true;
		int nameCase = 0;
		boolean proceed = false;

//		Person person = null;
//		Robot robot = null;
//		Vehicle vehicle = null;
//		Settlement settlement = null;

		text = text.trim();
		int len = text.length();

		// Detect "\" backslash and the name that follows
		if (len >= 3 && text.substring(0, 1).equalsIgnoreCase("\\")) {
			text = text.substring(1, len).trim();
			proceed = true;
		}

		if (expertMode) {

			if (text.toLowerCase().contains("reset clock thread")) {
				String s = "Resetting the clock executor thread...";
				responseText.append(s + System.lineSeparator());
				logger.config(s);
				sim.restartClockExecutor();
				return responseText.toString();
			}

			else if (text.toLowerCase().contains("reset clock pulse")) {
				String s = "Resetting the # of clock pulses according to the default TBU value...";
				responseText.append(s + System.lineSeparator());
				logger.config(s);
				masterClock.resetTotalPulses();
				return responseText.toString();
			}

			else if (text.toLowerCase().contains("reset clock listener")) {
				String s = "Resetting the clock listeners...";
				responseText.append(s + System.lineSeparator());
				logger.config(s);
				masterClock.resetClockListeners();
				return responseText.toString();
			}

		}

		else if (text.toLowerCase().contains("log")) {
			return processLogChange(text, responseText).toString();
		}

		else if (text.equalsIgnoreCase("scores")) {

			double aveSocial = 0;
			double aveSci = 0;
//			double totalSciScore = 0;

			Map<Double, String> totalScores = new HashMap<>();
			// Use Guava's multimap to handle duplicate key
//			Multimap<Double, String> scienceMap = ArrayListMultimap.create();
			Map<Double, String> scienceMap = new HashMap<>();
//			Multimap<Double, String> socialMap = ArrayListMultimap.create();
			Map<Double, String> socialMap = new HashMap<>();
			List<Double> totalList = new ArrayList<>();
//			List<Double> sciList = new ArrayList<>();
//			List<Double> socialList = new ArrayList<>();
			Collection<Settlement> col = unitManager.getSettlements();
			for (Settlement s : col) {
				double social = relationshipManager.getRelationshipScore(s);
				double science = scientificManager.getScienceScore(s, null);
				aveSocial += social;
				aveSci += science;
//				totalSciScore += science;

//				socialList.add(social);
//				sciList.add(science);
				totalList.add(science + social);

				socialMap.put(social, s.getName());
				scienceMap.put(science, s.getName());
				totalScores.put(science + social, s.getName());

			}

//			System.out.println("done with for loop.");
			int size = col.size();
			aveSocial = aveSocial / size;
			aveSci = aveSci / size;

			// Compute and save the new science score
//			if (totalSciScore > 0) {
//				for (Settlement s : col) {
//					double oldScore = 0;
//					if (getKey(scienceMap, s.getName()) != null)
//						oldScore = getKey(scienceMap, s.getName());
////					double newScore = Math.round(oldScore/totalSciScore * 100.0 * 10.0)/10.0;
//					scienceMap.remove(oldScore, s.getName());
////					scienceMap.put(newScore, s.getName());
//				}		
//			}

			// Sort the total scores
			totalList.sort((Double d1, Double d2) -> -d1.compareTo(d2));

			responseText.append(System.lineSeparator());
			responseText.append("                    Hall of Fame for Key Achievement");
			responseText.append(System.lineSeparator());
			responseText.append(" ----------------------------------------------------------------------");
			responseText.append(System.lineSeparator());

			StringBuffer space00 = computeWhiteSpaces("Settlement", 20);

			responseText.append(" Rank | Settlement");
			responseText.append(space00);
			responseText.append("| Total | Social | Science ");
			responseText.append(System.lineSeparator());
			responseText.append(" ----------------------------------------------------------------------");
			responseText.append(System.lineSeparator());

//			System.out.println("beginning next for loop.");
			for (int i = 0; i < size; i++) {
				StringBuffer space000 = computeWhiteSpaces("  #" + (i + 1), 8);

				// total score
//				System.out.println("0.");
				double totalScore = totalList.get(i);
				String totalStr = Math.round(totalScore * 10.0) / 10.0 + "";
				String name = totalScores.get(totalScore);
				if (name.length() > 21)
					name = name.substring(0, 21);
				StringBuffer spaces = computeWhiteSpaces(name + totalStr, 26);
				totalScores.remove(totalScore, name);

				// social score
//				System.out.println("1.");
				double socialScore = 0;
				if (getKey(socialMap, name) != null)
					socialScore = getKey(socialMap, name);
				String socialStr = Math.round(socialScore * 10.0) / 10.0 + "";
//				System.out.println("1.1");
				StringBuffer space0 = computeWhiteSpaces(socialStr, 8);
//				System.out.println("1.2");
				socialMap.remove(socialScore, name);

				// science score
//				System.out.println("2.");
				double scienceScore = 0;
				if (getKey(scienceMap, name) != null)
					scienceScore = getKey(scienceMap, name);
//				System.out.println("2.1");
				String scienceStr = Math.round(scienceScore * 10.0) / 10.0 + "";
				StringBuffer space1 = computeWhiteSpaces(scienceStr, 8);
//				System.out.println("2.2");
				scienceMap.remove(scienceScore, name);

//				System.out.println("3.");
				// sub totals
				responseText.append("  #" + (i + 1) + space000 + name + spaces + totalStr + space0 + socialStr + space1
						+ scienceStr);
				// Note : remove the pair will prevent the case when when 2 or more settlements
				// have the exact same score from reappearing

//				System.out.println("4.");
				responseText.append(System.lineSeparator());
			}

			responseText.append(" ----------------------------------------------------------------------");
			responseText.append(System.lineSeparator());
			responseText.append(addhiteSpacesRightName("Average : ", 36));
			responseText.append(addhiteSpacesRightName("" + Math.round(aveSocial * 10.0) / 10.0, 6));
			responseText.append(addhiteSpacesRightName("" + Math.round(aveSci * 10.0) / 10.0, 8));
			responseText.append(System.lineSeparator());

			return responseText.toString();

		}

		else if (text.equalsIgnoreCase("science")) {

			double tot = 0;
			// Use Guava's multimap to handle duplicate key
			Multimap<Double, String> map = ArrayListMultimap.create();
//			Map<Double, String> map = new HashMap<>();
			List<Double> list = new ArrayList<>();
			Collection<Settlement> col = unitManager.getSettlements();
			for (Settlement s : col) {
				double score = scientificManager.getScienceScore(s, null);
				tot += score;
				list.add(score);
				map.put(score, s.getName());
			}

			responseText.append(System.lineSeparator());
			responseText.append("     Hall of Fame for Science");
			responseText.append(System.lineSeparator());
			responseText.append(" -----------------------------------");
			responseText.append(System.lineSeparator());

			responseText.append("   Rank |  Score |  Settlement");
			responseText.append(System.lineSeparator());
			responseText.append(" -----------------------------------");
			responseText.append(System.lineSeparator());

			list.sort((Double d1, Double d2) -> -d1.compareTo(d2));

			int size = list.size();
			for (int i = 0; i < size; i++) {
				double score = list.get(i);
				String space = "";

				String scoreStr = Math.round(score * 10.0) / 10.0 + "";
				int num = scoreStr.length();
				if (num == 2)
					space = "   ";
				else if (num == 3)
					space = "  ";
				else if (num == 4)
					space = " ";
				else if (num == 5)
					space = "";

				List<String> names = new ArrayList<>(map.get(score));
				String n = names.get(0);
				responseText.append("    #" + (i + 1) + "    " + space + scoreStr + "    " + n);
				// Note : remove the pair will prevent the case when when 2 or more settlements
				// have the exact same score from reappearing
				map.remove(score, n);
				responseText.append(System.lineSeparator());
			}

			responseText.append(" -----------------------------------");
			responseText.append(System.lineSeparator());
			responseText.append(" Overall : " + Math.round(tot * 10.0) / 10.0);
			responseText.append(System.lineSeparator());

			return responseText.toString();
		}

		else if (text.toLowerCase().contains("social")) {

			double ave = 0;
			// Use Guava's multimap to handle duplicate key
			Multimap<Double, String> map = ArrayListMultimap.create();
//			Map<Double, String> map = new HashMap<>();
			List<Double> list = new ArrayList<>();
			Collection<Settlement> col = unitManager.getSettlements();
			for (Settlement s : col) {
				double score = relationshipManager.getRelationshipScore(s);
				ave += score;
				list.add(score);
				map.put(score, s.getName());
			}
			int size = list.size();
			ave = ave / size;
			responseText.append(System.lineSeparator());
			responseText.append("      Hall of Fame for Social");
			responseText.append(System.lineSeparator());
			responseText.append(" -----------------------------------");
			responseText.append(System.lineSeparator());

			responseText.append("   Rank  |  Score  |  Settlement");
			responseText.append(System.lineSeparator());
			responseText.append(" -----------------------------------");
			responseText.append(System.lineSeparator());

			list.sort((Double d1, Double d2) -> -d1.compareTo(d2));

			for (int i = 0; i < size; i++) {
				double score = list.get(i);
				String space = "";

				String scoreStr = Math.round(score * 10.0) / 10.0 + "";
				int num = scoreStr.length();
				if (num == 2)
					space = "   ";
				else if (num == 3)
					space = "  ";
				else if (num == 4)
					space = " ";
				else if (num == 5)
					space = "";

				List<String> names = new ArrayList<>(map.get(score));
				String n = names.get(0);
				responseText.append("    #" + (i + 1) + "    " + space + scoreStr + "    " + n);
				// Note : remove the pair will prevent the case when when 2 or more settlements
				// have the exact same score from reappearing
				map.remove(score, n);
				responseText.append(System.lineSeparator());
			}

			responseText.append(" -----------------------------------");
			responseText.append(System.lineSeparator());
			responseText.append(" Average : " + Math.round(ave * 10.0) / 10.0);
			responseText.append(System.lineSeparator());

			return responseText.toString();
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
			responseText.append("   # Settlers : " + unitManager.getTotalNumPeople());
			responseText.append(System.lineSeparator());
			responseText.append("  Mission Sol : " + missionSol);
			responseText.append(System.lineSeparator());
			responseText.append(" Martian Time : " + marsTime);
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());
			responseText.append(sim.printObjectSize(0));

//			responseText.append(System.lineSeparator());
//			responseText.append(System.lineSeparator());
//			responseText.append(sim.printObjectSize(1));

			return responseText.toString();
		}

		else if (text.toLowerCase().contains("time") || text.toLowerCase().contains("date")) {

			responseText.append(SYSTEM_PROMPT);
			responseText.append("see below");
			responseText.append(System.lineSeparator());

			responseText.append(printTime());

			// Life Support System

			// Resource Storage

			// Goal

			// Resource changes

			// Water Ration

			return responseText.toString();
		}

		else if (text.equalsIgnoreCase("key") || text.equalsIgnoreCase("keys") || text.equalsIgnoreCase("keyword")
				|| text.equalsIgnoreCase("keywords") || text.equalsIgnoreCase("/k")) {

			// responseText.append(System.lineSeparator());
			if (connectionMode == 0) {
				keywordText = SYSTEM_KEYWORDS;
			} else {
				keywordText = SYSTEM_KEYWORDS + KEYWORDS_HEIGHT;
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
		else if (text.equalsIgnoreCase("proposal")) {
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
			List<Settlement> settlementList = new ArrayList<Settlement>(unitManager.getSettlements());

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
			responseText.append("Here's the roster list of vehicles associated with each settlement.");
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());

			// Creates an array with the names of all of settlements
			List<Settlement> settlementList = new ArrayList<Settlement>(unitManager.getSettlements());

			for (Settlement s : settlementList) {
				Collection<Vehicle> list = s.getAllAssociatedVehicles();

				responseText.append(DASHES);
				responseText.append(System.lineSeparator());
				int num = (DASHES.length() - s.getName().length()) / 2;
				if (num > 0) {
					for (int i = 0; i < num; i++) {
						responseText.append(" ");
					}
				}

				responseText.append(s.getName());
				responseText.append(System.lineSeparator());
				responseText.append(DASHES);
				responseText.append(System.lineSeparator());

				List<Vehicle> vlist = list.stream()
						.sorted((p1, p2) -> p1.getVehicleType().compareTo(p2.getVehicleType()))
						.collect(Collectors.toList());

				for (Vehicle v : vlist) {
					responseText.append(v.getName());
					int num2 = 25 - v.getName().length();
					if (num2 > 0) {
						for (int i = 0; i < num2; i++) {
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

		else if (proceed) {

			List<Person> personList = new ArrayList<>();
			List<Robot> robotList = new ArrayList<>();
			List<Vehicle> vehicleList = new ArrayList<>();
			List<Settlement> settlementList = new ArrayList<>();

			// check settlements
			settlementList = CollectionUtils.returnSettlementList(text);

			// person and robot
			Iterator<Settlement> i = unitManager.getSettlements().iterator();
			while (i.hasNext()) {
				Settlement s = i.next();
				// Check if anyone has this name (as first/last name) in any settlements
				// and if he/she is still alive
				if (text.contains("bot") || text.contains("Bot")) {
					// Check if it is a bot
					robotList.addAll(s.returnRobotList(text));
					nameCase = robotList.size();
				}

				else { // if (text.contains("rover") || text.contains("vehicle")) {
						// check vehicles/rovers
					vehicleList.addAll(s.returnVehicleList(text));
					// check persons
					personList.addAll(s.returnPersonList(text));
				}
			}

			if (vehicleList.size() + robotList.size() + settlementList.size() + personList.size() > 1) {
				responseText.append(SYSTEM_PROMPT);
				responseText.append("There are more than one '");
				responseText.append(text);
				responseText.append(
						"'. Please be more specific by spelling out the full name of the party you would like to reach.");
				// System.out.println(responseText);
				return responseText.toString();
			}

			else if (robotList.size() > 0) {
				nameCase = robotList.size();
			}

			else if (vehicleList.size() > 0) {
				nameCase = vehicleList.size();
			}

			else if (personList.size() > 0) {
				nameCase = personList.size();
			}

			else if (settlementList.size() > 0) {
				nameCase = settlementList.size();
			}

			// capitalize the first initial of a name
			text = Conversion.capitalize(text);

			responseText = obtainTargetUnit(responseText, nameCase, text, 
					personList, 
					robotList, 
					vehicleList, 
					settlementList);
			return responseText.toString();
		}

		responseText.append(SYSTEM_PROMPT);
		responseText.append("I do not recognize any person, robot, vehicle or settlement by the name of '");
		responseText.append(text);
		responseText.append("'.");
		return responseText.toString();
	}
	
	public static StringBuffer obtainTargetUnit(StringBuffer responseText, int nameCase, String text, 
			List<Person> personList, 
			List<Robot> robotList, 
			List<Vehicle> vehicleList, 
			List<Settlement> settlementList) {
		// Case 1: more than one with the same name
		if (nameCase >= 2) {
			responseText.append(SYSTEM_PROMPT);
			responseText.append("There are more than one '");
			responseText.append(text);
			responseText.append(
					"'.Please be more specific by spelling out the full name of the party you would like to reach.");
			// System.out.println(responseText);
			return responseText;

		// Case 2: there is one person
		} else if (nameCase == 1) {
			String taskStr = "";

			// for people
			if (!personList.isEmpty()) {

				nameCase = personList.size();

				String s = "";
				taskStr = personList.get(0).getMind().getTaskManager().getTaskName();

				// Note: can taskStr be null and thus causing the simulation to hang ?
				if (taskStr == null) {
					int rand = RandomUtil.getRandomInt(2);
					if (rand == 0)
						s = "I'm sorry. " + text + " is occupied at this moment. Please try again later.";
					else if (rand == 1)
						s = text + " does not answer the comm. Please try again later.";
					else
						s = text + " cannot respond to your call at this moment. Please try again later.";

					responseText.append(SYSTEM_PROMPT);
					responseText.append(s);
					return responseText;
				}

				else if (taskStr.toLowerCase().contains("sleep")) {
					s = "I'm sorry. " + text + " is unavailable (" + taskStr
							+ ") at this moment. Please try again later.";
					// TODO: check if the person is available or not (e.g. sleeping or if on a
					// mission and out of comm range
					// broke down)
					responseText.append(SYSTEM_PROMPT);
					responseText.append(s);
					return responseText;
				}

				else {
					Person person = personList.get(0);
					if (person.isDeclaredDead()) {
						// Case 4: passed away
						String buried = "";
						if (person.getBuriedSettlement() != null)
							buried = person.getBuriedSettlement().getName();
						int rand = RandomUtil.getRandomInt(1);
						if (rand == 0) {
							responseText.append(SYSTEM_PROMPT);
							responseText.append("I'm sorry. ");
							responseText.append(text);
							responseText.append(" has passed away.");
						} else if (rand == 1) {
							responseText.append(SYSTEM_PROMPT);
							responseText.append("Regrettably, ");
							responseText.append(text);
							responseText.append(" has passed away.");
						} else {
							responseText.append(SYSTEM_PROMPT);
							responseText.append("Perhaps you haven't heard. ");
							responseText.append(text);
							responseText.append(" is dead.");
						}

						if (!buried.equals("")) {
							responseText.append(" and is buried at ");
							responseText.append(buried);
							responseText.append("." + System.lineSeparator());
						} else {
							responseText.append("." + System.lineSeparator());
						}

						responseText.append(System.lineSeparator());
						responseText.append("                Death Report");
						responseText.append(System.lineSeparator());
						responseText.append("---------------------------------------------");

						DeathInfo info = person.getPhysicalCondition().getDeathDetails();
						String cause = info.getCause();
						String doctor = info.getDoctor();
						boolean examDone = info.getExamDone();
						String time = info.getTimeOfDeath();
						String earthTime = info.getEarthTimeOfDeath();
						int sol = info.getMissionSol();
						String coord = info.getLocationOfDeath().getFormattedString();
						String place = info.getPlaceOfDeath();
						String missionPhase = info.getMissionPhase();
						String mission = info.getMission();
						String task = info.getTask();
						String taskPhase = info.getTaskPhase();
						String problem = info.getProblem().getSituation();
						String mal = info.getMalfunction();
						String job = info.getJob();
						String ill = info.getIllness().toString();
						String health = info.getHealth() + "";
						String lastWord = info.getLastWord();

//						responseText.append(System.lineSeparator());
						responseText.append(System.lineSeparator());
						responseText.append("Time of Death (TOD) : " + time);
						responseText.append(System.lineSeparator());
						responseText.append("          Earth TOD : " + earthTime);
						responseText.append(System.lineSeparator());
						responseText.append("        Mission Sol : " + sol);
						responseText.append(System.lineSeparator());
						responseText.append("     Place of Death : " + place);
						responseText.append(System.lineSeparator());
						if (examDone) {
							responseText.append(" Postmortem Exam by : " + doctor);
							responseText.append(System.lineSeparator());
						}
						responseText.append("     Cause of Death : " + cause);
						responseText.append(System.lineSeparator());
						responseText.append("        Coordinates : " + coord);
						responseText.append(System.lineSeparator());
						responseText.append("                Job : " + job);
						responseText.append(System.lineSeparator());
						responseText.append("               Task : " + task);
						responseText.append(System.lineSeparator());
						responseText.append("         Task Phase : " + taskPhase);
						responseText.append(System.lineSeparator());
						responseText.append("            Mission : " + mission);
						responseText.append(System.lineSeparator());
						responseText.append("      Mission Phase : " + missionPhase);
						responseText.append(System.lineSeparator());
						responseText.append("        Malfunction : " + mal);
						responseText.append(System.lineSeparator());
						responseText.append("            Illness : " + problem);
						responseText.append(System.lineSeparator());
						responseText.append("          Complaint : " + ill);
						responseText.append(System.lineSeparator());
						responseText.append("     General Health : " + health);
						responseText.append(System.lineSeparator());
						responseText.append("         Last Words : '" + lastWord + "'");
						responseText.append(System.lineSeparator());

						return responseText;
					}

					else {
						personCache = person;
						target = person.getName();

						responseText.append(personCache.getName());
						responseText.append(" : This is ");
						responseText.append(text);
						responseText.append(". " + getGreeting(1));
						return responseText;
					}
				}
			}

			// for robots
			else if (!robotList.isEmpty()) {
				nameCase = robotList.size();

				Robot robot = robotList.get(0);
				if (robot.getSystemCondition().isInoperable()) {
					// Case 4: decomissioned
					responseText.append(SYSTEM_PROMPT);
					responseText.append("I'm sorry. ");
					responseText.append(text);
					responseText.append(" has been decomissioned.");
					return responseText;
				}

				else {
					robotCache = robot;
					target = robot.getName();
					
					responseText.append(robotCache.getName());
					responseText.append(" : This is ");
					responseText.append(text);
					responseText.append(". " + getGreeting(2));
					return responseText;
				}
			}

			// For vehicles
			else if (!vehicleList.isEmpty()) {
				Vehicle vehicle = vehicleList.get(0);
				if (vehicle.getStatus() == StatusType.MAINTENANCE) {
					// Case 4: decomissioned
					responseText.append(SYSTEM_PROMPT);
					responseText.append("I'm sorry. ");
					responseText.append(text);
					responseText.append(" is down for maintenance and connection cannot be established.");
					return responseText;
				}

				else {
					vehicleCache = vehicle;
					target = vehicle.getName();
					
					responseText.append(vehicleCache.getName());
					responseText.append(" : This is ");
					responseText.append(text);
					responseText.append(". " + getGreeting(3));
					return responseText;
				}
			}

			// For settlements
			else if (!settlementList.isEmpty()) {
				Settlement settlement = settlementList.get(0);
				responseText.append(SYSTEM_PROMPT);
				responseText.append("You are now connected with ");
				responseText.append(settlement.getName());
				responseText.append(". " + getGreeting(0));

				settlementCache = settlement;
				target = settlement.getName();
				
				return responseText;
			}
		}
		
		return responseText;
	}
}
