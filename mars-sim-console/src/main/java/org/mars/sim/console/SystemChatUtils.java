/**
 * Mars Simulation Project
 * SystemChatUtils.java
 * @version 3.1.0 2019-09-03
 * @author Manny Kung
 */

package org.mars.sim.console;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.health.Complaint;
import org.mars_sim.msp.core.person.health.ComplaintType;
import org.mars_sim.msp.core.person.health.HealthProblem;
import org.mars_sim.msp.core.tool.RandomUtil;

public class SystemChatUtils extends ChatUtils {

//	private static Logger logger = Logger.getLogger(SystemChatUtils.class.getName());

	/**
	 * Processes the question and return the answer regarding an unit
	 * 
	 * @param text
	 * @return an array of String
	 */
	public static String[] askQuestion(String text) {
//		System.out.println("askQuestion() in ChatUtils");

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
}
