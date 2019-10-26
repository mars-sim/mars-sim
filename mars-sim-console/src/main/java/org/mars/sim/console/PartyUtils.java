package org.mars.sim.console;

import java.util.List;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.health.DeathInfo;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;

public class PartyUtils extends ChatUtils {
	
	/**
	 * Connects to a specific party based on player's input
	 * 
	 * @param responseText
	 * @param nameCase
	 * @param text
	 * @param nameType
	 * @param personList
	 * @param robotList
	 * @param vehicleList
	 * @param settlementList
	 * @return
	 */
	public static StringBuffer acquireParty(StringBuffer responseText, int nameCase, String text, int nameType,
			List<Person> personList, 
			List<Robot> robotList, 
			List<Vehicle> vehicleList, 
			List<Settlement> settlementList) {
//		System.out.println("acquireParty() in PartyUtils   partyName: " + partyName);
		
		// Note: 
		// nameType = 1 -> person
		// nameType = 2 -> robot
		// nameType = 3 -> vehicle
		// nameType = 4 -> settlement
		
		ChatUtils.personCache = null;
		ChatUtils.robotCache = null;
		ChatUtils.settlementCache = null;
		ChatUtils.vehicleCache = null;
		
		// Case 1: more than one with the same name
		if (nameCase >= 2) {
			responseText.append(SYSTEM_PROMPT);
			responseText.append("There is more than one party with the name of '");
			responseText.append(text);
			responseText.append(
					"'. Please be more specific by spelling out the full name of the party you would like to reach.");
			
			return responseText;

		// Case 2: there is one person
		} else if (nameCase == 1) {
			String taskStr = "";

			// for people
//			if (!personList.isEmpty()) {
			if (nameType == 1) {

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
						responseText = printDeathNotice(responseText, person, text);
						
						return responseText;
					}

					else {
						personCache = person;
						partyName = person.getName();

						responseText.append(personCache.getName());
						responseText.append(" : This is ");
						responseText.append(text);
						responseText.append(". " + getGreeting(1));
						
						return responseText;
					}
				}
			}

			// for robots
//			else if (!robotList.isEmpty()) {
			else if (nameType == 2) {	
				
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
					partyName = robot.getName();
					
					responseText.append(robotCache.getName());
					responseText.append(" : This is ");
					responseText.append(text);
					responseText.append(". " + getGreeting(2));
					
					return responseText;
				}
			}

			// For vehicles
//			else if (!vehicleList.isEmpty()) {
			else if (nameType == 3) {	
				
				Vehicle vehicle = vehicleList.get(0);
				if (vehicle.haveStatusType(StatusType.MAINTENANCE)) {
					// Case 4: decomissioned
					responseText.append(SYSTEM_PROMPT);
					responseText.append("I'm sorry. ");
					responseText.append(text);
					responseText.append(" is down for maintenance and connection cannot be established.");
					
					return responseText;
				}

				else {
					vehicleCache = vehicle;
					partyName = vehicle.getName();
					
					responseText.append(vehicleCache.getName());
					responseText.append(" : This is ");
					responseText.append(text);
					responseText.append(". " + getGreeting(3));
					
					return responseText;
				}
			}

			// For settlements
//			else if (!settlementList.isEmpty()) {
			else if (nameType == 4) {
				Settlement settlement = settlementList.get(0);
				settlementCache = settlement;
				partyName = settlement.getName();
						
//				responseText.append(SYSTEM_PROMPT);
				responseText.append("Switching over to ");
				responseText.append(settlement.getName());
				responseText.append(System.lineSeparator());
				responseText.append(System.lineSeparator());

				responseText.append(settlement.getName());
				responseText.append(" : ");
				responseText.append(getGreeting(1));

				return responseText;
			}
			
			else if (nameType == -1) {
				
				responseText.append(SYSTEM_PROMPT);
				responseText.append("There is more than one party with the name of '");
				responseText.append(text);
				responseText.append(
						"'. Please be more specific by spelling out the full name of the party you would like to reach.");
				// System.out.println(responseText);
				
				return responseText;
			}
			
			else { //if (nameType == 0) {
				
				String[] txt = clarify(SYSTEM, text);
//				questionText = txt[0];
				responseText.append(txt[1]);	
				
				return responseText;
			}
		}
		
		else { //if (nameCase <= 0) {
			String[] txt = clarify(SYSTEM, text);
//			questionText = txt[0];
			responseText.append(txt[1]);	
			
			return responseText;
		}
	}
	
	/**
	 * Prints the death notice of a person
	 * 
	 * @param responseText
	 * @param person
	 * @param text
	 * @return
	 */
	public static StringBuffer printDeathNotice(StringBuffer responseText, Person person, String text) {
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

//		responseText.append(System.lineSeparator());
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
}
