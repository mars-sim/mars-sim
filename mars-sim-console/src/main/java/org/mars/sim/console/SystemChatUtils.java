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
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.mars.TerrainElevation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.health.Complaint;
import org.mars_sim.msp.core.person.health.ComplaintType;
import org.mars_sim.msp.core.person.health.HealthProblem;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.CheckSerializedSize;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class SystemChatUtils extends ChatUtils {

	private static Logger logger = Logger.getLogger(SystemChatUtils.class.getName());

	static Map<Integer, String> topographicExcursionNames = new HashMap<>();
	static Map<Integer, String[]> topographicExcursionCoords = new HashMap<>();
			
	protected static TerrainElevation terrainElevation;
	
	static {
		topographicExcursionNames.put(1, "Olympus Mons");
		topographicExcursionNames.put(2, "Ascraeus Mons");
		topographicExcursionNames.put(3, "Arsia Mons");
		topographicExcursionNames.put(4, "Elysium Mons");
		topographicExcursionNames.put(5, "Pavonis Mons");
		topographicExcursionNames.put(6, "Hecates Tholus");
		topographicExcursionNames.put(7, "Albor Tholus");
		
		topographicExcursionNames.put(8, "Hellas");
		topographicExcursionNames.put(9, "Argyre");
		topographicExcursionNames.put(10, "Utopia");
		topographicExcursionNames.put(11, "Lyot");
		topographicExcursionNames.put(12, "Valles Marineris");

		topographicExcursionNames.put(13, "Viking Lander 1");
		topographicExcursionNames.put(14, "Viking Lander 2");
		topographicExcursionNames.put(15, "Pathfinder");

		topographicExcursionCoords.put(1, new String[] {"17.3495", "226.31", "21287.4"});
		topographicExcursionCoords.put(2, new String[] {"11.7082", "255.177", "18219.0"});
		topographicExcursionCoords.put(3, new String[] {"-9.12736 ", "238.261", "17780.7"});
		topographicExcursionCoords.put(4, new String[] {"24.7478", "146.437", "14126.6"});
		topographicExcursionCoords.put(5, new String[] {"-0.06261", "246.674", "14057.4"});
		topographicExcursionCoords.put(6, new String[] {"31.8125", "149.875", "4853.26"});
		topographicExcursionCoords.put(7, new String[] {"18.6562", "149.875", "3925.49"});
		
		topographicExcursionCoords.put(8, new String[] {"-32.8132", "62.0172", "-8180"});
		topographicExcursionCoords.put(9, new String[] {"-44.819", "315.088", "-5240.7"});
		topographicExcursionCoords.put(10, new String[] {"39.5 ", "110.2", "-5050"});
		topographicExcursionCoords.put(11, new String[] {"28.9277", "50.6031", "-7036"});
		topographicExcursionCoords.put(12, new String[] {"-14.2969", "301.969", "-5679.47"});
		
		topographicExcursionCoords.put(13, new String[] {"22.2692", "311.8113", "-3627"});
		topographicExcursionCoords.put(14, new String[] {"47.6680", "134.0430", "-4505"});
		topographicExcursionCoords.put(15, new String[] {"19.0949", "326.5092", "-3682"});
	}
	
//	public SystemChatUtils() {
//		StringBuffer sb = new StringBuffer();
//		System.out.println(displayReferenceElevation(sb).toString());
//	}

	
	/**
	 * Asks a question in Expert Mode
	 * 
	 * @param text
	 * @param responseText
	 * @return
	 */
	public static String[] askExpertMode(String text, StringBuffer responseText) {
		
		String questionText = "";
		
		if (text.toLowerCase().contains("reset clock thread")) {
			String s = "Resetting the clock executor thread...";
			responseText.append(s + System.lineSeparator());
			logger.config(s);
			sim.restartClockExecutor();

		}

		else if (text.toLowerCase().contains("reset clock pulse")) {
			String s = "Resetting the # of clock pulses according to the default TBU value...";
			responseText.append(s + System.lineSeparator());
			logger.config(s);
			masterClock.resetTotalPulses();

		}

		else if (text.toLowerCase().contains("reset clock listener")) {
			String s = "Resetting the clock listeners...";
			responseText.append(s + System.lineSeparator());
			logger.config(s);
			masterClock.resetClockListeners();

		}
		
		else if (text.toLowerCase().contains("object size")) {

			List<Person> list = new ArrayList<>(unitManager.getPeople());
			List<Person> list1 = new ArrayList<>();
			
//			List<Equipment> list = new ArrayList<>(unitManager.getEquipment());
//			List<Equipment> list1 = new ArrayList<>();
			
			list1.add(list.get(0));
			
			long sumSize = 0;
			String SPACE = " ";
			
//			for (Equipment i : list) {
			for (Person i : list1) {	
//				MemoryMeter meter = new MemoryMeter();
//			    long size0 = meter.measure(p);
//			    long size1 = meter.measureDeep(p);
//			    long size2 = meter.countChildren(i);
//			    
////				long size = ObjectSizeCalculator.getObjectSize(list.get(0));
//				String s = "The Object Size of " + p + " is " + size0 + " | " + size1 + " | " + size2;
				String unit = "";
				
				long size = CheckSerializedSize.getSerializedSize(i);
				
				sumSize += size;
				
				if (size < 1_000D) {
					unit = SPACE + "B" + SPACE;
				}
				else if (size < 1_000_000) {
					size = size/1_000;
					unit = SPACE + "KB";
				}
				else if (size < 1_000_000_000) {
					size = size/1_000_000;
					unit = SPACE + "MB";
				}
				
				String s = i + " : " + size + unit;// + "   # : " + size2;
				
				
				responseText.append(s + System.lineSeparator());
			}
			
			String sumUnit = "";
			
			if (sumSize < 1_000D) {
				sumUnit = SPACE + "B" + SPACE;
			}
			else if (sumSize < 1_000_000) {
				sumSize = sumSize/1_000;
				sumUnit = SPACE + "KB";
			}
			else if (sumSize < 1_000_000_000) {
				sumSize = sumSize/1_000_000;
				sumUnit = SPACE + "MB";
			}
			
			String s = "The Total Size of " + list.size() + " is " + sumSize + sumUnit;
			
			responseText.append(s + System.lineSeparator());

		}
		
		return new String[] {questionText, responseText.toString()};
	}
	
	/**
	 * Connects to an unit (Person, Robot, Vehicle, or Settlement)
	 * 
	 * @param text
	 * @return an array of String
	 */
	public static String[] connectToAUnit(String text) {
//		System.out.println("connectToAUnit() in SystemChatUtils   partyName: " + partyName);

		String questionText = "";
		StringBuffer responseText = new StringBuffer("");
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
		
		if (!partyName.equalsIgnoreCase("") && !partyName.equalsIgnoreCase(text)) {
			List<String> properNames = ChatUtils.createProperNounsList();
			for (String s : properNames) {
				if (s.equalsIgnoreCase(text)) {
//					System.out.println("text is " + text);				
					// Switch the target of the conversation to this unit with the name "text"			
					String response = askSystem(text)[1];			
//					// Print the new connection status line 			
		        	questionText = "Disconnecting from " + name + ". Connecting with " + partyName + "...";
		        	
		        	partyName = "";
		        	
		        	return new String[] {questionText, response};			
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

				else if (personCache != null) {
					int rand1 = RandomUtil.getRandomInt(1);

					if (rand1 == 0)
						responseText.append(" has left the conversation.");
					else if (rand1 == 1)
						responseText.append(" just hung up.");
				}		}

			else {
				bye = farewell(name, false);
				questionText = bye[0];
				responseText.append(bye[1]);
				responseText.append(System.lineSeparator());
			}
			
			personCache = null;
			robotCache = null;
			settlementCache = null;
			vehicleCache = null;
		
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
//			Party.isSettlement = true;
			
			if (isInteger(text, 10)) {

				int num = Integer.parseUnsignedInt(text, 10);

				try {
					String[] ans = SettlementChatUtils.askSettlementNum(num);
					questionText = ans[0];
					responseText.append(ans[1]);
				} catch (NullPointerException ne) {
					ne.printStackTrace();
				}			

			}

			else {
				// if it's not a integer input

				try {
					String[] ans = SettlementChatUtils.askSettlementStr(text, name);
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

			try {
				String[] ans = VehicleChatUtils.askVehicle(text, name);
				questionText = ans[0];
				responseText.append(ans[1]);
			} catch (NullPointerException ne) {
				ne.printStackTrace();
			}
	
		}

		// Case 2: ask to talk to a person or robot
		else if (personCache != null || robotCache != null && settlementCache == null) {
			// Note : this is better than personCache != null || robotCache != null since it
			// can
			// incorporate help and other commands
			int num = -1;

//			System.out.println("settlementCache == null");

			if (isInteger(text, 10)) {
				num = Integer.parseUnsignedInt(text, 10);
			}

			// Add command "die" for expert mode
			if (expertMode && personCache != null && text.equalsIgnoreCase("die")) {
				
//				personCache = null;
				robotCache = null;
				settlementCache = null;
				vehicleCache = null;
				
				try {
					String[] ans = suicide(questionText, responseText, name);
					questionText = ans[0];
					responseText.append(ans[1]);
				} catch (Exception ne) {
					ne.printStackTrace();
				}
			}

			else {
				// if not using expert mode
				// ask a person or a robot
				
//				personCache = null;
//				robotCache = null;
				settlementCache = null;
				vehicleCache = null;
				
				try {
					String[] ans = PersonRobotChatUtils.askPersonRobot(text, num, name, u);
					questionText = ans[0];
					responseText.append(ans[1]);
					System.out.println(responseText.toString());
				} catch (Exception ne) {
					ne.printStackTrace();
				}

			}
			
//			else {
//				// set personCache and robotCache to null only if you want to quit the
//				// conversation
//				String[] txt = clarify(name);
//				questionText = txt[0];
//				responseText.append(txt[1]);
//				
//				return new String[] { questionText, responseText.toString()};
//			}
			
		}

		else {
			// set personCache and robotCache to null only if you want to quit the
			// conversation
			String[] txt = clarify(name, text);
			questionText = txt[0];
			responseText.append(txt[1]);
		}

		return new String[] { questionText, responseText.toString()};
	}
	
	/**
	 * Requests the person to die
	 * 
	 * @param questionText
	 * @param responseText
	 * @return
	 */
	public static String[] suicide(String questionText, StringBuffer responseText, String name) {
		
		if (personCache != null) {
			questionText = YOU_PROMPT + " I hereby pronounce you dead.";

			if (personCache.isOutside()) {
				responseText
						.append("Can you at least tell me why ? I'm outside right now. "
								+ "Let's wait till I'm done with my task and/or mission :( ");
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
		
		else {
			// set personCache and robotCache to null only if you want to quit the
			// conversation
			String[] txt = clarify(name, "dying");
			questionText = txt[0];
			responseText.append(txt[1]);
			
			return new String[] { questionText, responseText.toString()};
		}
		
		return new String[] { questionText, responseText.toString()};
	}
	
	
	/**
	 * Gets the elevation of the given latitude and longitude
	 * 
	 * @param latitudeStr
	 * @param longitudeStr
	 * @return
	 */
	public static double getElevation(String latitudeStr, String longitudeStr) {
		try {
//			double lat = Double.parseDouble(latitudeStr); //Coordinates.parseLatitude(latitudeStr);
//			double lon = Double.parseDouble(longitudeStr); //Coordinates.parseLatitude(longitudeStr);
//			System.out.println("lat : " + lat + "  lon : " + lon);
			if (mars == null)
				mars = sim.getMars();
			if (terrainElevation == null)
				terrainElevation =  mars.getSurfaceFeatures().getTerrainElevation();
			return terrainElevation.getMOLAElevation(new Coordinates(latitudeStr, longitudeStr));//lon, lat));
		} catch(IllegalStateException e) {
			System.out.println(e);
			return 0;
		}
	}
	
	/**
	 * Gets the elevation of the given latitude and longitude
	 * 
	 * @param latitudeStr
	 * @param longitudeStr
	 * @return
	 */
	public static double getElevationNoDir(String latitudeStr, String longitudeStr) {
		try {
//		System.out.println("lat : " + latitudeStr + "  lon : " + longitudeStr);
			double phi = Coordinates.parseLatitude2Phi(latitudeStr); // Double.parseDouble(latitudeStr); //
			double theta = Coordinates.parseLongitude2Theta(longitudeStr); // Double.parseDouble(longitudeStr); //
			String s = String.format("%10f %10f ", 
					Math.round(phi*1_000_000.0)/1_000_000.0,
					Math.round(theta*1_000_000.0)/1_000_000.0);
			System.out.print(s);
			if (mars == null)
				mars = sim.getMars();
			if (terrainElevation == null)
				terrainElevation =  mars.getSurfaceFeatures().getTerrainElevation();
			
			Coordinates location = new Coordinates(latitudeStr, longitudeStr);
			
			int rgb[] = terrainElevation.getRGB(location);
			int red = rgb[0];
			int green = rgb[1];
			int blue = rgb[2];
			
			String s00 = String.format("  %3d %3d %3d  ", 
							red,
							green,
							blue); 
			System.out.print(s00);
			
			float[] hsb = TerrainElevation.getHSB(rgb);
			float hue = hsb[0];
			float saturation = hsb[1];
			float brightness = hsb[2];
			
			String s1 = String.format(" %5.3f %5.3f %5.3f  ", 
					Math.round(hue*1000.0)/1000.0, 
					Math.round(saturation*1000.0)/1000.0,
					Math.round(brightness*1000.0)/1000.0); 
			System.out.print(s1);
			
			double re = terrainElevation.getRawElevation(location);
			String s2 = String.format("%8.3f ", 
					Math.round(re*1_000.0)/1_000.0);
			System.out.print(s2);
			
			double pe = terrainElevation.getPatchedElevation(location);
			
			String s3 = String.format("%8.3f ", 
					Math.round(pe*1_000.0)/1_000.0);
			System.out.print(s3);
						
			double mola = terrainElevation.getMOLAElevation(location);
			
			String s4 = String.format("%8.3f ", 
					Math.round(mola*1_000.0)/1_000.0);
			System.out.print(s4);
			
//			System.out.println("e : " + e);
			return mola;
		} catch(IllegalStateException e) {
			System.out.println(e);
			return 0;
		}
	}
	
	
	public static StringBuffer displayReferenceElevation(StringBuffer responseText) {
		int size = topographicExcursionNames.size();
		
		responseText.append(System.lineSeparator());
		responseText.append(addWhiteSpacesLeftName(" Location", 15));
		responseText.append(addWhiteSpacesRightName(" Latitude", 12));
		responseText.append(addWhiteSpacesRightName(" Longitude", 12));
		responseText.append(addWhiteSpacesRightName(" Est Elevation", 15));
		responseText.append(addWhiteSpacesRightName(" Ref Elevation", 15));	
		responseText.append(addWhiteSpacesRightName(" delta", 10));
		
		responseText.append(System.lineSeparator());
		responseText.append(" ----------------------------------------------------------------------");
		responseText.append(System.lineSeparator());

		for (int i = 1; i <= size; i++) {
			String value[] = topographicExcursionCoords.get(i);
			String latStr = value[0];
			String lonStr = value[1];
			String elevStr = value[2];
			double ref = Double.parseDouble(elevStr)/1_000.0;
			System.out.println("ref : " + ref);
									
			double e = getElevationNoDir(latStr, lonStr);
			System.out.println("e : " + e);
			double delta = Math.round((e-ref)/e *100_000.0)/1_000.0;
			System.out.println("delta : " + delta);
			
			responseText.append(addWhiteSpacesLeftName(" " + topographicExcursionNames.get(i), 15));
			responseText.append(addWhiteSpacesRightName("" + latStr, 12));
			responseText.append(addWhiteSpacesRightName("" + lonStr, 12));
			responseText.append(addWhiteSpacesRightName(Math.round(e *1_000.0)/1_000.0 + " km", 15));
			responseText.append(addWhiteSpacesRightName(Math.round(ref *1_000.0)/1_000.0 + " km", 15));	
			responseText.append(addWhiteSpacesRightName(" " + delta + " percent", 10));
			responseText.append(System.lineSeparator());
//			responseText.append("The estimated elevation of " + topographicExcursionNames.get(i) 
//					+ " at (" + latStr + ", " + lonStr + ") is "
//					+ Math.round(e *10_000.0)/10.0 
//					+ " m. (Reference value : " + Math.round(ref *1_000.0)/1_000.0 + " m)");
//					responseText.append(System.lineSeparator());
		}
		responseText.append(System.lineSeparator());
		
		return responseText;
	}
	
	/*
	 * Asks a system-wide, overall question. e.g. "scores" on all settlements
	 * 
	 * @param input text
	 */
	public static String[] askSystem(String text) {
//		System.out.println("askSystem() in SystemChatUtils   partyName: " + partyName);
//		ChatUtils.personCache = null;
//		ChatUtils.robotCache = null;
//		ChatUtils.settlementCache = null;
//		ChatUtils.vehicleCache = null;
		
		String questionText = "";
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
			return askExpertMode(text, responseText);
		}

		// Add asking about settlements in general
		else if (text.toLowerCase().contains("distance")) {

			// Note: can't use questionText since it's asking for player's input
//			questionText = YOU_PROMPT + "What is the distance between two given coordinates ?";
	
//			List<Settlement> settlementList = new ArrayList<Settlement>(unitManager.getSettlements());
//			responseText = SettlementChatUtils.findSettlementDistances(responseText, settlementList);
				
			boolean good0 = false;
			boolean good1 = false;
			String latitudeStr = "";
			String longitudeStr = "";
			double lat = -1;
			double lon = -1;
			
			boolean good2 = false;
			boolean good3 = false;
			String latitudeStr1 = "";
			String longitudeStr1 = "";
			double lat1 = -1;
			double lon1 = -1;
			final String NOTE = "[Note: '/q' to quit]";
			
			boolean change = true;
			
			while (change) {
				
				do {
					try {
						String prompt0 = YOU_PROMPT + "What is the latitude (e.g. 10.03 N, 5.01 S) of the 1st coordinate ? " 
								+ System.lineSeparator() + NOTE;
						latitudeStr = textIO.newStringInputReader().read(prompt0);
						if (latitudeStr.equalsIgnoreCase("quit") || latitudeStr.equalsIgnoreCase("/q"))
							return new String[] { questionText, responseText.toString() };
						else {
							lat = Coordinates.parseLatitude2Phi(latitudeStr);
							good0 = true;
						}
					} catch(IllegalStateException e) {
						good0 = false;
					}
				} while (!good0);
				
				do {
					try {
						String prompt0 = YOU_PROMPT + "What is the longitude (e.g. 5.09 E, 18.04 W) of the 1st coordinate ?" 
								+ System.lineSeparator() + NOTE;
						longitudeStr = textIO.newStringInputReader().read(prompt0);
						if (longitudeStr.equalsIgnoreCase("quit") || longitudeStr.equalsIgnoreCase("/q"))
							return new String[] { questionText, responseText.toString() };
						else {
							lon = Coordinates.parseLatitude2Phi(longitudeStr);
							good1 = true;
						}
					} catch(IllegalStateException e) {
						good1 = false;
					}
				} while (!good1);
				
				do {
					try {
						String prompt0 = YOU_PROMPT + "What is the latitude (e.g. 10.03 N, 5.01 S) of the 2nd coordinate ?" 
								+ System.lineSeparator() + NOTE;
						latitudeStr1 = textIO.newStringInputReader().read(prompt0);
						if (latitudeStr1.equalsIgnoreCase("quit") || latitudeStr1.equalsIgnoreCase("/q"))
							return new String[] { questionText, responseText.toString() };
						else {
							lat1 = Coordinates.parseLatitude2Phi(latitudeStr1);
							good2 = true;
						}
					} catch(IllegalStateException e) {
						good2 = false;
					}
				} while (!good2);
				
				do {
					try {
						String prompt0 = YOU_PROMPT + "What is the longitude (e.g. 5.09 E, 18.04 W) of the 2nd coordinate ?" 
								+ System.lineSeparator() + NOTE;
						longitudeStr1 = textIO.newStringInputReader().read(prompt0);
						if (longitudeStr1.equalsIgnoreCase("quit") || longitudeStr1.equalsIgnoreCase("/q"))
							return new String[] { questionText, responseText.toString() };
						else {
							lon1 = Coordinates.parseLatitude2Phi(longitudeStr);
							good3 = true;
						}
					} catch(IllegalStateException e) {
						good3 = false;
					}
				} while (!good3);
				
				if (good0 && good1 && good2 && good3) {
					
					double distance = new Coordinates(lat, lon).getDistance(new Coordinates(lat1, lon1));
					responseText.append(System.lineSeparator());
					
					String ans = "The distance between "
							+ "(" + latitudeStr + ", " + longitudeStr + ") and "
							+ "(" + latitudeStr1 + ", " + longitudeStr1 + ") is "
							+ Math.round(distance *1_000.0)/1_000.0 + " km" + System.lineSeparator();
					
					String prompt = ans + SYSTEM_PROMPT + "Another pair of coordinates ? " 
							+ System.lineSeparator() + NOTE;
					
					change = textIO.newBooleanInputReader().read(prompt); 
				}
			} // the end of the outer while loop
			
		}
		

	
		else if (text.equalsIgnoreCase("excursions")) {
	
			responseText = displayReferenceElevation(responseText);
			
//			responseText.append(System.lineSeparator());
			
		}
		
		else if (text.equalsIgnoreCase("elevation")) {			
//			responseText = displayReferenceElevation(responseText);		
			
			// Note: can't use questionText since it's asking for player's input
//			questionText = YOU_PROMPT + "What is the elevation of a given coordinate ?";
			
			boolean good0 = false;
			boolean good1 = false;
			String latitudeStr = "";
			String longitudeStr = "";
			double lat = -1;
			double lon = -1;
			final String NOTE = "[Note: '/q' to quit]";
			
			boolean change = true;
			
			while (change) {
				
				do {
					try {
						String prompt0 = YOU_PROMPT + "What is the latitude (e.g. 10.03 N, 5.01 S) ? " 
								+ System.lineSeparator() + NOTE;
						latitudeStr = textIO.newStringInputReader().read(prompt0);
						if (latitudeStr.equalsIgnoreCase("quit") || latitudeStr.equalsIgnoreCase("/q"))
							return new String[] { questionText, responseText.toString() };
						else {
							lat = Coordinates.parseLatitude2Phi(latitudeStr);
							good0 = true;
						}
					} catch(IllegalStateException e) {
						good0 = false;
					}
				} while (!good0);
				
				do {
					try {
						String prompt0 = YOU_PROMPT + "What is the longitude (e.g. 5.09 E, 18.04 W) ? " 
								+ System.lineSeparator() + NOTE;
						longitudeStr = textIO.newStringInputReader().read(prompt0);
						if (longitudeStr.equalsIgnoreCase("quit") || longitudeStr.equalsIgnoreCase("/q"))
							return new String[] { questionText, responseText.toString() };
						else {
							lon = Coordinates.parseLatitude2Phi(longitudeStr);
							good1 = true;
						}
					} catch(IllegalStateException e) {
						good1 = false;
					}
				} while (!good1);
				
				if (good0 && good1) {
					if (mars == null)
						mars = sim.getMars();
					if (terrainElevation == null)
						terrainElevation =  mars.getSurfaceFeatures().getTerrainElevation();							
					double elevation = terrainElevation.getMOLAElevation(lat, lon);
					responseText.append(System.lineSeparator());
					
					String ans = "The elevation of (" + latitudeStr + ", " + longitudeStr + ") is "
							+ Math.round(elevation *1000.0)/1000.0 + " km" + System.lineSeparator();
					
					String prompt = ans + SYSTEM_PROMPT + "Another location ? " 
							+ System.lineSeparator() + NOTE;
					
					change = textIO.newBooleanInputReader().read(prompt); 
				}
			}
			
			responseText.append(System.lineSeparator());
			
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
			responseText.append(addWhiteSpacesRightName("Average : ", 36));
			responseText.append(addWhiteSpacesRightName("" + Math.round(aveSocial * 10.0) / 10.0, 6));
			responseText.append(addWhiteSpacesRightName("" + Math.round(aveSci * 10.0) / 10.0, 8));
			responseText.append(System.lineSeparator());



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

		}

		
		// Add asking about settlements in general
		else if (text.toLowerCase().contains("settlement")) {

			questionText = YOU_PROMPT + "What are the location informations regarding the settlements ?";

			responseText = findLocationInfo(responseText, unitManager.getSettlements());
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
					responseText.append(Conversion.capitalize(v.getVehicleType()));
					responseText.append(System.lineSeparator());
				}

				responseText.append(System.lineSeparator());
			}

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

		}

		else if (len >= 1) {
			proceed = true;
		}
		
		
		if (proceed) {
			responseText = matchName(responseText, text, nameCase);
		}
		
//		else {//if (len == 0 || text == null) {// || text.length() == ) {{
////			responseText.append(clarify(SYSTEM)[1]);
//			String[] txt = clarify(SYSTEM, text);
//			questionText = txt[0];
//			responseText.append(txt[1]);
//			
//			return new String[] { questionText, responseText.toString()};
//		}
		
		return new String[] { questionText, responseText.toString() };
	}
	
	
	/**
	 * Checks to see if the input text matches a name
	 * 
	 * @param responseText
	 * @param text
	 * @param nameCase
	 * @return
	 */
	public static StringBuffer matchName(StringBuffer responseText, String text, int nameCase) {

		List<Person> exactPersonList = new ArrayList<>();
		List<Robot> exactRobotList = new ArrayList<>();
		List<Vehicle> exactVehicleList = new ArrayList<>();
		List<Settlement> exactSettlementList = new ArrayList<>();
		
		List<Person> personList = new ArrayList<>();
		List<Robot> robotList = new ArrayList<>();
		List<Vehicle> vehicleList = new ArrayList<>();
		List<Settlement> settlementList = new ArrayList<>();

		// check settlements
		settlementList = CollectionUtils.matchSettlementList(text, false);
		exactSettlementList = CollectionUtils.matchSettlementList(text, true);
		
		// person and robot
		Iterator<Settlement> i = unitManager.getSettlements().iterator();
		while (i.hasNext()) {
			Settlement s = i.next();
			// Check if anyone has this name (as first/last name) in any settlements
			// and if he/she is still alive
			if (text.contains("bot") || text.contains("Bot")) {
				// Check if it is a bot
				robotList.addAll(s.returnRobotList(text, false));
				nameCase = robotList.size();
				
				exactRobotList.addAll(s.returnRobotList(text, true));
			}

			else { // if (text.contains("rover") || text.contains("vehicle")) {
					// check vehicles/rovers
				vehicleList.addAll(s.returnVehicleList(text, false));
				exactVehicleList.addAll(s.returnVehicleList(text, true));
				// check persons
				personList.addAll(s.returnPersonList(text, false));
				exactPersonList.addAll(s.returnPersonList(text, true));
			}
		}

		int nameType = 0;
		
		int numOfSameNames = exactVehicleList.size() + exactRobotList.size() + exactSettlementList.size() + exactPersonList.size();
		
		if (numOfSameNames == 1) {
			nameCase = 1;
			
			if (exactPersonList.size() == 1)
				nameType = 1;
			
			else if (exactRobotList.size() == 1)
				nameType = 2;
			
			else if (exactVehicleList.size() == 1)
				nameType = 3;
			
			else if (exactSettlementList.size() == 1)
				nameType = 4;
			
			// capitalize the first initial of a name
//			text = Conversion.capitalize0(text);

			responseText = PartyUtils.acquireParty(responseText, nameCase, text, nameType, 
					exactPersonList, 
					exactRobotList, 
					exactVehicleList, 
					exactSettlementList);
		}
		
		else {

			numOfSameNames = vehicleList.size() + robotList.size() + settlementList.size() + personList.size();
			
			if (numOfSameNames == 1) {
				nameCase = numOfSameNames;
				
				if (personList.size() == 1)
					nameType = 1;
				
				else if (robotList.size() == 1)
					nameType = 2;
				
				else if (vehicleList.size() == 1)
					nameType = 3;
				
				else if (settlementList.size() == 1)
					nameType = 4;
			
				responseText = PartyUtils.acquireParty(responseText, nameCase, text, nameType, 
					personList, 
					robotList, 
					vehicleList, 
					settlementList);
			}
			
			else if (numOfSameNames == 0) {
				nameType = 0;
				nameCase = 0;
				
				responseText = PartyUtils.acquireParty(responseText, nameCase, text, nameType, 
						personList, 
						robotList, 
						vehicleList, 
						settlementList);
			}
			
			else {//if (numOfSameNames > 1) {
				nameType = -1;
				nameCase = numOfSameNames;
				
				responseText = PartyUtils.acquireParty(responseText, nameCase, text, nameType, 
						personList, 
						robotList, 
						vehicleList, 
						settlementList);
			}

		}

		return responseText;
	}
	
	/**
	 * Obtains the location information regarding the settlements
	 * 
	 * @param responseText
	 * @param collection
	 * @return
	 */
	public static StringBuffer findLocationInfo(StringBuffer responseText, Collection<Settlement> collection) {
		
		responseText.append("Settlement Location Information :" + System.lineSeparator() + System.lineSeparator());
		
		String[] header = new String[] { " Settlement ", " Lat & long ", " Elev [km]" };

//		responseText.append(System.lineSeparator());
//		responseText.append(System.lineSeparator());
		responseText.append(System.lineSeparator());
		responseText.append(addWhiteSpacesLeftName(header[0], 20));
		responseText.append(addWhiteSpacesRightName(header[1], 20));
		responseText.append(addWhiteSpacesRightName(header[2], 13));
		responseText.append(System.lineSeparator());
		responseText.append(" " +  addDashes(66) + " ");
		responseText.append(System.lineSeparator());

		for (Settlement s: collection) {
			responseText.append(addWhiteSpacesLeftName(" " + s.getName(), 20));
			responseText.append(addWhiteSpacesRightName(s.getCoordinates().getFormattedString(), 20));
			if (mars == null)
				mars = sim.getMars();
			if (terrainElevation == null)
				terrainElevation =  mars.getSurfaceFeatures().getTerrainElevation();
			double elevation = Math.round(terrainElevation.getMOLAElevation(s.getCoordinates())*1000.0)/1000.0;
			responseText.append(addWhiteSpacesRightName(elevation +"", 13));
			responseText.append(System.lineSeparator());
		}
		
		return responseText;
	}
}
