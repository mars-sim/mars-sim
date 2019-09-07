/**
 * Mars Simulation Project
 * PersonRobotChatUtils.java
 * @version 3.1.0 2019-09-03
 * @author Manny Kung
 */

package org.mars.sim.console;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.robot.RoboticAttributeManager;
import org.mars_sim.msp.core.robot.RoboticAttributeType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;

public class PersonRobotChatUtils extends ChatUtils {

//	private static Logger logger = Logger.getLogger(PersonRobotChatUtils.class.getName());
			
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
		StringBuffer responseText = new StringBuffer();

		responseText.append(name);
		responseText.append(": ");

		if (text.toLowerCase().contains("job score")) {
			questionText = YOU_PROMPT + "What is your job capability and job prospect scores ?";
			responseText.append("See below : ");
			responseText.append(System.lineSeparator());

//			Map<String, List<Person>> map = JobManager.getJobMap(personCache.getAssociatedSettlement());

			List<String> jobList = JobUtil.getJobList();
			// new ArrayList<>(map.keySet());
			Collections.sort(jobList);

			int length = 0;
			for (String jobStr : jobList) {
				int l = jobStr.length();
				if (l > length)
					length = l;
			}

			int num1 = 1;

			responseText.append(System.lineSeparator());
			responseText.append(addhiteSpacesRightName(" Job   ", 14));
			responseText.append(addhiteSpacesRightName(" Capability Score", 22));
			responseText.append(addhiteSpacesRightName(" Prospect Score", 18));
			responseText.append(System.lineSeparator());
			responseText.append(" ------------------------------------------------------- ");
			responseText.append(System.lineSeparator());

			for (String jobStr : jobList) {
				if (num1 < 10)
					responseText.append("  " + num1 + ". ");
				else
					responseText.append(" " + num1 + ". ");
				num1++;

				responseText.append(addNameFirstWhiteSpaces(jobStr, 20));

//				responseText.append(System.lineSeparator());
//				responseText.append(" ");
//				for (int i=0; i<length; i++) {
//					responseText.append("-");
//				}	
				Job job = JobUtil.getJob(jobStr);

				double capScore = Math.round(job.getCapability(personCache) * 10.0) / 10.0;
				responseText.append(addNameFirstWhiteSpaces(" " + capScore, 18));

				double prospectScore = Math.round(
						JobUtil.getJobProspect(personCache, job, personCache.getAssociatedSettlement(), true) * 10.0)
						/ 10.0;
				responseText.append(addNameFirstWhiteSpaces(" " + prospectScore, 5));
				responseText.append(System.lineSeparator());

			}
		}

		else if (text.toLowerCase().contains("shift") || text.toLowerCase().contains("work shift")) {
			questionText = YOU_PROMPT + "What is your work shift ?";

			ShiftType st0 = personCache.getTaskSchedule().getShiftType();
			int score = personCache.getTaskSchedule().getShiftChoice().get(st0);
			responseText.append("My current work shift is ");
			responseText.append(st0);
			responseText.append(" (score : " + score + ")");

			ShiftType[] st = personCache.getTaskSchedule().getPreferredShift();
			if (st[1] != null) {
				int score0 = personCache.getTaskSchedule().getShiftChoice().get(st[0]);
				int score1 = personCache.getTaskSchedule().getShiftChoice().get(st[1]);
				responseText.append(System.lineSeparator());
				responseText.append("My preference is as follows : ");
				responseText.append(System.lineSeparator());
				responseText.append("   - 1st most favorite : " + st[0] + " (score : " + score0 + ")");
				responseText.append(System.lineSeparator());
				responseText.append("   - 2nd most favorite : " + st[1] + " (score : " + score1 + ")");
//				responseText.append(System.lineSeparator());
			} else {
				if (st0 == st[1]) {
					responseText.append("And this is also my preferred work shift.");
					responseText.append(". ");
//					responseText.append(System.lineSeparator());
				} else {
					score = personCache.getTaskSchedule().getShiftChoice().get(st[0]);
					responseText.append("But my preference is work shift " + st[0] + " (score : " + score + ")");
					responseText.append(". ");
//					responseText.append(System.lineSeparator());
				}
			}

		}

		else if (text.toLowerCase().contains("airlock time")) {
			questionText = YOU_PROMPT + "How long have you spent inside the airlock ?";

			responseText.append("See my records as follows :");
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());

			responseText.append("  Sol  | Millisols");
			responseText.append(System.lineSeparator());
			responseText.append(" ------+------------");
			responseText.append(System.lineSeparator());

			int size = marsClock.getMissionSol();
			int MAX0 = 5;
			int MAX1 = 10;
			for (int i = 0; i < size; i++) {
				double milliSol = personCache.getTaskSchedule().getAirlockTasksTime(i);
				if (milliSol > 0) {
					responseText.append(addhiteSpacesRightName(i + "", MAX0));
					String m = Math.round(milliSol * 10.0) / 10.0 + "";
					responseText.append(addhiteSpacesRightName(m, MAX1));
					responseText.append(System.lineSeparator());
				}
			}
		}

		else if (text.toLowerCase().contains("eva time")) {
			questionText = YOU_PROMPT + "What is your EVA history and experience ?";

			responseText.append("See my EVA records as follows :");
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());

			responseText.append("  Sol  | Millisols");
			responseText.append(System.lineSeparator());
			responseText.append(" ------+------------");
			responseText.append(System.lineSeparator());

			Map<Integer, Double> eVATime = personCache.getTotalEVATaskTimeBySol();
			int size = marsClock.getMissionSol();
			int MAX0 = 5;
			int MAX1 = 10;
			for (int i = 0; i < size; i++) {
				if (eVATime.containsKey(i)) {
					double milliSol = eVATime.get(i);
					responseText.append(addhiteSpacesRightName(i + "", MAX0));
					String m = Math.round(milliSol * 10.0) / 10.0 + "";
					responseText.append(addhiteSpacesRightName(m, MAX1));
					responseText.append(System.lineSeparator());
				}
			}

//			int size = marsClock.getMissionSol();
//			int MAX0 = 5;
//			int MAX1 = 10;
//			for (int i=0; i<size; i++) {
//				double milliSol = personCache.getTaskSchedule().getEVATasksTime(i);
//				if (milliSol > 0) {
//					responseText.append(addhiteSpacesName(i + "", MAX0));
//					String m = Math.round(milliSol*10.0)/10.0 + "";
//					responseText.append(addhiteSpacesName(m, MAX1));	
//					responseText.append(System.lineSeparator());
//				}
//			}
		}

		else if (text.toLowerCase().contains("attribute")) {
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
				for (int i = 0; i < attributeList.size(); i++) {
					String n = attributeList.get(i);
					int size = n.length();
//					if (i+1 <= 9)
//						space = " ";
					for (int j = 0; j < (max - size); j++) {
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
				for (int i = 0; i < attributeList.size(); i++) {
					String n = attributeList.get(i);
					int size = n.length();
//					if (i+1 <= 9)
//						space = " ";
					for (int j = 0; j < (max - size); j++) {
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

			responseText.append("here's a list of my skills with level ane experience points : ");
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());
			responseText.append("       Type of Skill | Level | Exp");
			responseText.append(System.lineSeparator());
			responseText.append("     ------------------------");
			responseText.append(System.lineSeparator());

			Map<String, Integer> levels = skillManager.getSkillLevelMap();
			Map<String, Integer> exps = skillManager.getSkillExpMap();
			List<String> skillNames = skillManager.getKeyStrings();
			Collections.sort(skillNames);
//			SkillType[] keys = skillManager.getKeys();

			int max = 20;
//			String space = "";		
			for (int i = 0; i < skillNames.size(); i++) {
				String n = skillNames.get(i);
				int size = n.length();
//				if (i+1 <= 9)
//					space = " ";
				for (int j = 0; j < (max - size); j++) {
					responseText.append(" ");
				}
//				responseText.append(space + "(" + (i+1) + ") ");
				responseText.append(n);
//				responseText.append(" : ");
				responseText.append(addhiteSpacesRightName("" + levels.get(n), 5));
				responseText.append(addhiteSpacesRightName("" + exps.get(n), 8));
				responseText.append(System.lineSeparator());
			}

		}

		else if (text.toLowerCase().contains("time") || text.toLowerCase().contains("date")) {
			questionText = YOU_PROMPT + "What day/time is it ?";

//			responseText.append(personCache.getName() + " : ");
			responseText.append("See below");
			responseText.append(System.lineSeparator());

			responseText.append(printTime());

		}

		else if (text.equalsIgnoreCase("space agency") || text.toLowerCase().contains("sponsor")) {
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
				relationshipManager = sim.getRelationshipManager();

			Map<Person, Double> bestFriends = relationshipManager.getBestFriends(personCache);
			if (bestFriends.isEmpty()) {
				responseText.append("I don't have any friends yet.");
			} else {
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
				} else if (size >= 2) {
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
						responseText.append("(" + (i + 1) + "). " + p + " -- ");
						responseText.append("I'm " + relation + pronoun + " (");
						responseText.append("score : " + fmt1.format(score) + ").");
						responseText.append(System.lineSeparator());
					}
				}
			}
		}

		else if (text.toLowerCase().contains("social")) {
//				text.toLowerCase().contains("relationship")
//				|| text.toLowerCase().contains("relation")

			questionText = YOU_PROMPT + "How are your relationship with others ?";

			if (relationshipManager == null)
				relationshipManager = sim.getRelationshipManager();

			// My opinions of them
			Map<Person, Double> friends = relationshipManager.getMyOpinionsOfThem(personCache);
//			System.out.println("friends in ChatUtils : " + friends);
			if (friends.isEmpty()) {
				responseText.append("I don't have any friends yet.");
			} else {
				responseText.append(" See the table below ");
				responseText.append(System.lineSeparator());
				responseText.append(System.lineSeparator());
				List<Person> list = new ArrayList<>(friends.keySet());
				int size = list.size();

//				responseText.append("                   Friend | Score | Attitude "				
				responseText.append("       Toward this Person | Score | My Attitude" + System.lineSeparator());
				responseText.append("      -----------------------------------------" + System.lineSeparator());

				int max0 = 25;
				int max1 = 7;
				int max2 = 16;
				int count = 0;
				int sum = 0;
				String SPACE = " ";

				for (int x = 0; x < size; x++) {
					Person p = list.get(x);
					double score = friends.get(p);
					sum += score;
					count++;
					String relation = RelationshipManager.describeRelationship(score);
					int size0 = max0 - p.getName().length();
					for (int i = 0; i < size0; i++) {
						responseText.append(SPACE);
					}
					responseText.append(p);
					String scoreStr = Math.round(score * 10.0) / 10.0 + "";
					int size2 = max1 - scoreStr.length();
					for (int i = 0; i < size2; i++) {
						responseText.append(SPACE);
					}
					responseText.append(scoreStr);

//						int size1 = max1 - relation.length();
//						for (int i=0; i<size1; i++) {
//							responseText.append(SPACE);
//						}
					responseText.append("    ");
					responseText.append(relation);

					responseText.append(System.lineSeparator());

				}

				responseText.append("      -----------------------------------------");
				responseText.append(System.lineSeparator());
				responseText.append("       My Opinion of Them : ");
				responseText.append(fmt1.format(sum / count));

				responseText.append(System.lineSeparator());
				responseText.append(System.lineSeparator());
//				responseText.append(System.lineSeparator());	

				// Their opinions of me
				friends = relationshipManager.getTheirOpinionsOfMe(personCache);

				list = new ArrayList<>(friends.keySet());
				size = list.size();

//				responseText.append("                   Friend | Score | Attitude "				
				responseText.append("    This Person Toward Me | Score | Attitude Toward Me" + System.lineSeparator());
				responseText.append("      -----------------------------------------" + System.lineSeparator());

				count = 0;
				sum = 0;

				for (int x = 0; x < size; x++) {
					Person p = list.get(x);
					double score = friends.get(p);
					sum += score;
					count++;
					String relation = RelationshipManager.describeRelationship(score);
					int size0 = max0 - p.getName().length();
					for (int i = 0; i < size0; i++) {
						responseText.append(SPACE);
					}
					responseText.append(p);

					String scoreStr = Math.round(score * 10.0) / 10.0 + "";
					int size2 = max1 - scoreStr.length();
					for (int i = 0; i < size2; i++) {
						responseText.append(SPACE);
					}
					responseText.append(scoreStr);

//						int size1 = max1 - relation.length();
//						for (int i=0; i<size1; i++) {
//							responseText.append(SPACE);
//						}
					responseText.append("    ");
					responseText.append(relation);

					responseText.append(System.lineSeparator());

				}

				responseText.append("      -----------------------------------------");
				responseText.append(System.lineSeparator());
				responseText.append("      Their Opinion of Me : ");
				responseText.append(Math.round(sum / count * 10.0) / 10.0);

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

		else if (num == 2 || text.contains("what your role") || text.toLowerCase().contains("role")) {
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

		else if (num == 3 || text.equalsIgnoreCase("where you from") || text.toLowerCase().contains("nationality")
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
			if (c instanceof MarsSurface) {
				responseText.append("I'm outside");
			} else {
				responseText.append("I'm inside ").append(c.getName()).append(".");
			}

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

		else if (num == 7 || text.toLowerCase().contains("task")) {
//				|| text.toLowerCase().contains("activity")
//				|| text.toLowerCase().contains("doing")
//				|| text.toLowerCase().contains("action")) {
			questionText = YOU_PROMPT + "What are you doing ?";
			if (personCache != null) {
				responseText.append(personCache.getTaskDescription());
			} else if (robotCache != null) {
				responseText.append(robotCache.getTaskDescription());
			}

		}

		else if (num == 8 || text.toLowerCase().contains("mission")) {
//			|| text.toLowerCase().contains("trip")
//				|| text.toLowerCase().contains("excursion")) {
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
				if (c instanceof MarsSurface) {
					responseText.append("My vehicle is not inside");
				} else {
					responseText.append("My vehicle is at ");
					responseText.append(c.getName());
				}

			} else
				responseText.append("I'm not in a vehicle.");
		}

		else if (num == 14 || (text.contains("vehicle") && text.contains("outside"))
				|| (text.contains("vehicle") && text.contains("top") && text.contains("container"))) {
			questionText = YOU_PROMPT + "What is your vehicle located?";// 's top container unit ?";
			Vehicle v = u.getVehicle();
			if (v != null) {
//				Unit tc = v.getContainerUnit();
//				if (tc != null) {
				responseText.append("My vehicle is at ");
				responseText.append(v.getLocationTag().getImmediateLocation());// tc.getName());
//				} else
//					responseText.append("My vehicle is not inside");// doesn't have a top container unit.";
			} else
				responseText.append("I'm not in a vehicle.");
		}

		else if (num == 15 || text.equalsIgnoreCase("garage") || (text.contains("vehicle") && text.contains("park"))) {
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

		else if (num == 17 || text.equalsIgnoreCase("bed")) {
//				|| text.contains("quarters")) 

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

		else if (text.equalsIgnoreCase("key") || text.equalsIgnoreCase("keys") || text.equalsIgnoreCase("keyword")
				|| text.equalsIgnoreCase("keywords") || text.equalsIgnoreCase("/k")) {

			questionText = REQUEST_KEYS;
			if (connectionMode == 0) {
				keywordText = PERSON_KEYWORDS;
			} else {
				keywordText = PERSON_KEYWORDS + KEYWORDS_HEIGHT;
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
			

}
