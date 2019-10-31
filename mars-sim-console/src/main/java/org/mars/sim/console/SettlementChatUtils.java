/**
 * Mars Simulation Project
 * SettlementChatUtils.java
 * @version 3.1.0 2019-09-03
 * @author Manny Kung
 */

package org.mars.sim.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.person.ai.mission.MissionPlanning;
import org.mars_sim.msp.core.person.ai.mission.PlanType;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.ObjectiveType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.vehicle.Vehicle;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class SettlementChatUtils extends ChatUtils {

	private static Logger logger = Logger.getLogger(SettlementChatUtils.class.getName());
	
	/**
	 * Asks the settlement when the input is a number
	 * 
	 * @param text the input number
	 * @return the response string[]
	 */
	public static String[] askSettlementNum(int num) {
//		System.out.println("askSettlementNum() in ChatUtils   partyName: " + partyName);
		
		ChatUtils.personCache = null;
		ChatUtils.robotCache = null;
//		ChatUtils.settlementCache = null;
		ChatUtils.vehicleCache = null;
		
		String questionText = "";
		StringBuffer responseText = new StringBuffer();

		if (num == 1) {
			questionText = YOU_PROMPT + "how many beds are there in total ? ";
			responseText.append("The total # of beds is ");
			responseText.append(settlementCache.getPopulationCapacity());
		} else if (num == 2) {
			questionText = YOU_PROMPT + "how many beds that have already been designated to a person ? ";
			responseText.append("There are ");
			responseText.append(settlementCache.getTotalNumDesignatedBeds());
			responseText.append(" designated beds. ");
		} else if (num == 3) {
			questionText = YOU_PROMPT + "how many beds that are currently NOT occupied ? ";
			responseText.append("There are ");
			responseText.append(settlementCache.getPopulationCapacity() - settlementCache.getSleepers());
			responseText.append(" unoccupied beds. ");
		} else if (num == 4) {
			questionText = YOU_PROMPT + "how many beds are currently occupied ? ";
			responseText.append("There are ");
			responseText.append(settlementCache.getSleepers());
			responseText.append(" occupied beds with people sleeping on it at this moment. ");

		} else {
			questionText = YOU_PROMPT + "You entered '" + num + "'.";
			responseText.append("Sorry. This number is not assigned to a valid question.");
		}

		return new String[] { questionText, responseText.toString() };
	}

	/**
	 * Asks the settlement when the input is a string
	 * 
	 * @param text the input string
	 * @param name the input name of the settlement
	 * @return the response string[]
	 */
	public static String[] askSettlementStr(String text, String name) {
//		System.out.println("askSettlementStr() in ChatUtils   partyName: " + partyName);

		ChatUtils.personCache = null;
		ChatUtils.robotCache = null;
//		ChatUtils.settlementCache = null;
		ChatUtils.vehicleCache = null;
		
		String questionText = "";
		StringBuffer responseText = new StringBuffer();

		String text0 = text;
		String jobStrName = text0.replace("job prospect ", "");

		if (text.toLowerCase().contains("distance")) {

			questionText = YOU_PROMPT + "What are the distances between settlements ?";

			// Creates an array with the names of all of settlements
			List<Settlement> settlementList = new ArrayList<Settlement>(unitManager.getSettlements());
	
			responseText = SystemChatUtils.findLocationInfo(responseText, settlementList);
			
			responseText = findSettlementDistances(responseText, settlementList);
			
		}
		
		else if (text.contains("job prospect") && JobUtil.getJob(jobStrName) != null) {
			List<Person> list = settlementCache.getAllAssociatedPeople().stream()
//					.sorted((p1, p2)-> p1.getMind().getJob().getName(p1.getGender()).compareTo(p2.getMind().getJob().getName(p2.getGender())))
					.sorted((p1, p2) -> p1.getName().compareTo(p2.getName())).collect(Collectors.toList());

			Job job = JobUtil.getJob(jobStrName);
			responseText.append(" ** " + Conversion.capitalize(jobStrName) + " Job Prospect Scores ** ");
//			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());
			for (Person p : list) {
				double jobProspect = Math.round(JobUtil.getJobProspect(p, job, settlementCache, true) * 10.0) / 10.0;
				responseText.append(addWhiteSpacesRightName(" " + p, 20));
				responseText.append(addWhiteSpacesRightName(" " + jobProspect, 6));
				responseText.append(System.lineSeparator());
			}
		}

		else if (text.equalsIgnoreCase("job") || text.equalsIgnoreCase("career")) {
			// The individual's job prospect score of a particular job
			responseText.append(settlementCache + " : please specify if you want to see 'job roster', 'job demand'");
			responseText.append(System.lineSeparator());
			responseText.append(
					"Note : to see job prospect scores, specify the job position such as 'job prospect engineer', or 'job prospect botanist'");
//			responseText.append(System.lineSeparator());
		}

		else if (text.equalsIgnoreCase("job prospect")) {
			responseText
					.append(settlementCache + " : please specify which job you would like to see the prospect scores "
							+ "such as 'job prospect engineer', or 'job prospect botanist'");

		}

		else if (text.equalsIgnoreCase("job roster")) {
//			responseText.append(" Job Roster");
//			responseText.append(System.lineSeparator());

			questionText = YOU_PROMPT + "What is everybody's job ?";
			responseText.append(System.lineSeparator());
			responseText.append(settlementCache + " : ");
			responseText.append("See the job roster below :");
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());
			responseText.append("          --- Sort by Name ---");
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());

			List<Person> list = settlementCache.getAllAssociatedPeople().stream()
//					.sorted((p1, p2)-> p1.getMind().getJob().getName(p1.getGender()).compareTo(p2.getMind().getJob().getName(p2.getGender())))
					.sorted((p1, p2) -> p1.getName().compareTo(p2.getName())).collect(Collectors.toList());

			int length = 0;
			for (Person p : list) {
				int l = p.getName().length();
				if (l > length)
					length = l;
			}

			int num = 1;
			for (Person p : list) {
				String job = p.getMind().getJob().getName(p.getGender());
				if (num < 10)
					responseText.append("  " + num + ". ");
				else
					responseText.append(" " + num + ". ");
				num++;
				responseText.append(p);
				int size = length + 2 - p.getName().length();// - job.length();
				if (size > 0) {
					for (int i = 0; i < size; i++) {
						responseText.append(" ");
					}
				}
				responseText.append(" - " + job);
				responseText.append(System.lineSeparator());
			}

			responseText.append(System.lineSeparator());
			responseText.append("  --- Sort by Job ---");
			responseText.append(System.lineSeparator());

			Map<String, List<Person>> map = JobUtil.getJobMap(settlementCache);

			List<String> jobList = new ArrayList<>(map.keySet());
			Collections.sort(jobList);

			int num1 = 1;

			for (String jobStr : jobList) {
				responseText.append(System.lineSeparator());
				if (num1 < 10)
					responseText.append("  " + num1 + ". ");
				else
					responseText.append(" " + num1 + ". ");
				num1++;

				responseText.append(jobStr);
				responseText.append(System.lineSeparator());
				responseText.append(" ");
				for (int i = 0; i < length; i++) {
					responseText.append("-");
				}
//				responseText.append(" -------------");
				responseText.append(System.lineSeparator());

				List<Person> plist = map.get(jobStr);
				Collections.sort(plist);

				for (Person p : plist) {
					responseText.append("  - " + p.getName());
					responseText.append(System.lineSeparator());
				}
			}
		}

		else if (text.equalsIgnoreCase("job demand")) {
			responseText.append(addWhiteSpacesRightName(" Job", 20));
			responseText.append(addWhiteSpacesRightName(" Demand", 10));
			responseText.append(addWhiteSpacesRightName(" Filled", 8));
			responseText.append(addWhiteSpacesRightName(" Deficit", 10));

			responseText.append(System.lineSeparator());

			Map<String, List<Person>> map = JobUtil.getJobMap(settlementCache);

			List<Job> jobs = JobUtil.getJobs();
//			List<String> jobs = JobManager.getJobs();

			for (Job job : jobs) {
				String jobName = job.getName(GenderType.MALE);
				String n = " " + jobName;
				responseText.append(addWhiteSpacesRightName(n, 20));

				String demand = "" + Math.round(job.getSettlementNeed(settlementCache) * 10.0) / 10.0;
				responseText.append(addWhiteSpacesRightName(demand, 9));


				int num = 0;
				if (map.get(jobName) != null)
					num = map.get(jobName).size();

				String positions = "" + num;
				responseText.append(addWhiteSpacesRightName(positions, 8));
				
				String deficit = ""
						+ Math.round(JobUtil.getRemainingSettlementNeed(settlementCache, job) * 10.0) / 10.0;
				responseText.append(addWhiteSpacesRightName(deficit, 9));
				

				responseText.append(System.lineSeparator());
			}

		}

		else if (text.equalsIgnoreCase("co2") || text.equalsIgnoreCase("carbon dioxide")) {
			int max = 40;
			double usage = 0;
			double reserve = 0;
			double totalArea = 0;

			// Prints the current reserve
			try {
				reserve = settlementCache.getInventory().getAmountResourceStored(ResourceUtil.co2ID, false);
			} catch (Exception e) {
			}

			responseText.append(addWhiteSpacesRightName("--- Greenhouse Farming ---", 50));
			responseText.append(System.lineSeparator());
			String s0 = " Current reserve : ";
			responseText.append(addWhiteSpacesRightName(s0, max));
			responseText.append(Math.round(reserve * 100.0) / 100.0 + " kg");
			responseText.append(System.lineSeparator());

			// Prints greenhouse usage
			List<Building> farms = settlementCache.getBuildingManager().getBuildings(FunctionType.FARMING);
			for (Building b : farms) {
				Farming f = b.getFarming();
				usage += f.computeUsage(2);
				totalArea += f.getGrowingArea();
			}

			String s1 = " Total growing area : ";
			responseText.append(addWhiteSpacesRightName(s1, max));
			responseText.append(Math.round(totalArea * 100.0) / 100.0);
			responseText.append(System.lineSeparator());
			String s3 = " Generated daily per unit area : ";
			responseText.append(addWhiteSpacesRightName(s3, max));
			responseText.append(Math.round(usage / totalArea * 100.0) / 100.0 + " kg/m^2/sol");
			responseText.append(System.lineSeparator());
			String s2 = " Total amount generated Daily : ";
			responseText.append(addWhiteSpacesRightName(s2, max));
			responseText.append(Math.round(usage * 100.0) / 100.0 + " kg/sol");
			responseText.append(System.lineSeparator());

		}

		else if (text.equalsIgnoreCase("o2") || text.equalsIgnoreCase("oxygen")) {
			int max = 40;
			double usage = 0;
			double reserve = 0;
			double totalArea = 0;

			// Prints the current reserve
			try {
				reserve = settlementCache.getInventory().getAmountResourceStored(ResourceUtil.oxygenID, false);
			} catch (Exception e) {
			}

			responseText.append(addWhiteSpacesRightName("--- Greenhouse Farming ---", 50));
			responseText.append(System.lineSeparator());
			String s0 = " Current reserve : ";
			responseText.append(addWhiteSpacesRightName(s0, max));
			responseText.append(Math.round(reserve * 100.0) / 100.0 + " kg");
			responseText.append(System.lineSeparator());

			// Prints greenhouse usage
			List<Building> farms = settlementCache.getBuildingManager().getBuildings(FunctionType.FARMING);
			for (Building b : farms) {
				Farming f = b.getFarming();
				usage += f.computeUsage(1);
				totalArea += f.getGrowingArea();
			}

			String s1 = " Total growing area : ";
			responseText.append(addWhiteSpacesRightName(s1, max));
			responseText.append(Math.round(totalArea * 100.0) / 100.0);
			responseText.append(System.lineSeparator());
			String s3 = " Consumed daily per unit area : ";
			responseText.append(addWhiteSpacesRightName(s3, max));
			responseText.append(Math.round(usage / totalArea * 100.0) / 100.0 + " kg/m^2/sol");
			responseText.append(System.lineSeparator());
			String s2 = " Total amount consumed daily : ";
			responseText.append(addWhiteSpacesRightName(s2, max));
			responseText.append(Math.round(usage * 100.0) / 100.0 + " kg/sol");
			responseText.append(System.lineSeparator());

		}

		else if (text.equalsIgnoreCase("water")) {
			int max0 = 40;
			double usage = 0;
			double totalArea = 0;

			// Prints greenhouse usage
			List<Building> farms = settlementCache.getBuildingManager().getBuildings(FunctionType.FARMING);
			for (Building b : farms) {
				Farming f = b.getFarming();
				usage += f.computeUsage(0);
				totalArea += f.getGrowingArea();
			}

			responseText.append(addWhiteSpacesRightName("--- Greenhouse Farming ---", 50));
			responseText.append(System.lineSeparator());
			String s01 = " Total growing area : ";
			responseText.append(addWhiteSpacesRightName(s01, max0));
			responseText.append(Math.round(totalArea * 100.0) / 100.0);
			responseText.append(System.lineSeparator());
			String s03 = " Consumed daily per unit area : ";
			responseText.append(addWhiteSpacesRightName(s03, max0));
			responseText.append(Math.round(usage / totalArea * 100.0) / 100.0 + " kg/m^2/sol");
			responseText.append(System.lineSeparator());
			String s02 = " Projected daily amount consumed : ";
			responseText.append(addWhiteSpacesRightName(s02, max0));
			responseText.append(Math.round(usage * 100.0) / 100.0 + " kg/sol");
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());

			int max = 14;
			double reserve = 0;
			double greenhouseUsage = 0;
			double consumption = 0;
			double livingUsage = 0;
			double output = 0;
			double cleaning = 0;
			double net = 0;

			String s = " -------------+-------------------------------------------------------+-----";

			responseText.append("                        Water - Rate of Change [kg/sol]");
			responseText.append(System.lineSeparator());
			responseText.append(s);
			responseText.append(System.lineSeparator());

			String t0 = "   Reserve";
			responseText.append(addNameFirstWhiteSpaces(t0, max));

			String t1 = "| Greenhouse";
			responseText.append(addNameFirstWhiteSpaces(t1, max - 1));

			String t2 = " Consumption";
			responseText.append(addNameFirstWhiteSpaces(t2, max - 1));

			String t3 = " Hygiene";
			responseText.append(addNameFirstWhiteSpaces(t3, max - 5));

			String t4 = " Cleaning";
			responseText.append(addNameFirstWhiteSpaces(t4, max - 3));

			String t5 = "Processes";
			responseText.append(addNameFirstWhiteSpaces(t5, max - 4));

			String t6 = "| Net";
			responseText.append(t6);

			responseText.append(System.lineSeparator());
			responseText.append(s);
			responseText.append(System.lineSeparator());

			// Prints the current reserve
			try {
				reserve = settlementCache.getInventory().getAmountResourceStored(ResourceUtil.waterID, false);
			} catch (Exception e) {
			}

			String s0 = " " + Math.round(reserve * 100.0) / 100.0 + " kg";
			responseText.append(addNameFirstWhiteSpaces(s0, max));

			// Prints greenhouse usage
//			List<Building> farms = settlementCache.getBuildingManager().getBuildings(FunctionType.FARMING);
			for (Building b : farms) {
				Farming f = b.getFarming();
				greenhouseUsage += f.getDailyAverageWaterUsage();
//				area += f.getGrowingArea();
			}
			String s1 = "|   -" + Math.round(greenhouseUsage * 100.0) / 100.0;
			responseText.append(addNameFirstWhiteSpaces(s1, max - 1));
			net = net - greenhouseUsage;

			// Prints consumption
			List<Person> ppl = new ArrayList<>(settlementCache.getAllAssociatedPeople());
			for (Person p : ppl) {
				consumption += p.getDailyUsage(1);
			}
			// Add water usage from making meal and dessert
			consumption += settlementCache.getDailyUsage(0) + settlementCache.getDailyUsage(1);
			String s2 = "    -" + Math.round(consumption * 100.0) / 100.0;
			responseText.append(addNameFirstWhiteSpaces(s2, max - 1));
			net = net - consumption;

			// Prints living usage
			List<Building> quarters = settlementCache.getBuildingManager()
					.getBuildings(FunctionType.LIVING_ACCOMODATIONS);
			for (Building b : quarters) {
				LivingAccommodations la = b.getLivingAccommodations();
				livingUsage += la.getDailyAverageWaterUsage();

			}
			String s3 = " -" + Math.round(livingUsage * 100.0) / 100.0;
			responseText.append(addNameFirstWhiteSpaces(s3, max - 5));
			net = net - livingUsage;

			// Prints cleaning usage
			cleaning = settlementCache.getDailyUsage(2) + settlementCache.getDailyUsage(3);
			String s4 = "  -" + Math.round(cleaning * 100.0) / 100.0;
			responseText.append(addNameFirstWhiteSpaces(s4, max - 3));
			net = net - cleaning;

			// Prints output from resource processing
			List<Building> bldgs = settlementCache.getBuildingManager().getBuildings(FunctionType.RESOURCE_PROCESSING);
			for (Building b : bldgs) {
				ResourceProcessing rp = b.getResourceProcessing();
				List<ResourceProcess> processes = rp.getProcesses();
				for (ResourceProcess p : processes) {
					if (p.isProcessRunning())
						output += p.getMaxOutputResourceRate(ResourceUtil.waterID);
				}
			}
			String s5 = " +" + Math.round(output * 1_000 * 100.0) / 100.0;
			responseText.append(addNameFirstWhiteSpaces(s5, max - 4));
			net = net + output * 1_000;

			// Prints net change
			String s6 = "";
			if (net > 0) {
				s6 = " +" + Math.round(net * 100.0) / 100.0;// + " [kg/sol]";
			} else {
				s6 = " " + Math.round(net * 100.0) / 100.0;// + " [kg/sol]";
			}

			responseText.append(s6);
			responseText.append(System.lineSeparator());
		}

		else if (text.equalsIgnoreCase("repair")) {
			responseText.append(System.lineSeparator());

			GoodsManager goodsManager = settlementCache.getGoodsManager();

			int level = goodsManager.getRepairLevel();

			String prompt = System.lineSeparator() + "Current Outstanding Repair Priority Level is " + level
					+ System.lineSeparator() + System.lineSeparator() + "Would you like to change it?";
			boolean change = textIO.newBooleanInputReader().read(prompt); // .withDefaultValue(true)

			if (change) {
				int newLevel = textIO.newIntInputReader().withMinVal(1).withMaxVal(9)
						.read("Enter the Priority Level (1 = lowest; 9 = highest)");
				String s = "";

				if (newLevel > 0 && newLevel < 10) {

					goodsManager.setRepairPriority(newLevel);

					responseText.append(settlementCache + " : I've updated it for you as follows : ");
					responseText.append(System.lineSeparator());
					responseText.append(System.lineSeparator());
					s = "     New Outstanding Repair Priority Level : " + newLevel;
					responseText.append(s);
					logger.config(s);

				} else {
					s = settlementCache + " : It's outside of the normal range. Aborted.";
					responseText.append(s);
					logger.config(s);
				}
			}

		}

		else if (text.equalsIgnoreCase("maintenance")) {
			responseText.append(System.lineSeparator());

			GoodsManager goodsManager = settlementCache.getGoodsManager();

			int level = goodsManager.getMaintenanceLevel();

			String prompt = System.lineSeparator() + "Current Outstanding Maintenance Priority Level is " + level
					+ System.lineSeparator() + System.lineSeparator() + "Would you like to change it?";
			boolean change = textIO.newBooleanInputReader().read(prompt); // .withDefaultValue(true)

			if (change) {
				int newLevel = textIO.newIntInputReader().withMinVal(1).withMaxVal(9)
						.read("Enter the Priority Level (1 = lowest; 9 = highest)");
				String s = "";

				if (newLevel > 0 && newLevel < 10) {

					goodsManager.setMaintenancePriority(newLevel);

					responseText.append(settlementCache + " : I've updated it for you as follows : ");
					responseText.append(System.lineSeparator());
					responseText.append(System.lineSeparator());
					s = "     New Outstanding Maintenance Priority Level : " + newLevel;
					responseText.append(s);
					logger.config(s);

				} else {
					s = settlementCache + " : It's outside of the normal range. Aborted.";
					responseText.append(s);
					logger.config(s);
				}
			}

		}

		else if (text.equalsIgnoreCase("evasuit") || text.equalsIgnoreCase("eva suit")) {
			responseText.append(System.lineSeparator());

			GoodsManager goodsManager = settlementCache.getGoodsManager();

			int level = goodsManager.getEVASuitLevel();

			String prompt = System.lineSeparator() + "Current EVA Suit Production Priority Level is " + level
					+ System.lineSeparator() + System.lineSeparator() + "Would you like to change it?";
			boolean change = textIO.newBooleanInputReader().read(prompt); // .withDefaultValue(true)

			if (change) {
				int newLevel = textIO.newIntInputReader().withMinVal(1).withMaxVal(9)
						.read("Enter the Priority Level (1 = lowest; 9 = highest)");
				String s = "";

				if (newLevel > 0 && newLevel < 10) {

					goodsManager.setEVASuitPriority(newLevel);

					responseText.append(settlementCache + " : I've updated it for you as follows : ");
					responseText.append(System.lineSeparator());
					responseText.append(System.lineSeparator());
					s = "     New EVA Suit Production Priority Level : " + newLevel;
					responseText.append(s);
					logger.config(s);

				} else {
					s = settlementCache + " : Invald input. Please try it again.";
					responseText.append(s);
					logger.config(s);
				}
			}

		}

		else if (text.equalsIgnoreCase("obj") || text.equalsIgnoreCase("objective")) {
			String obj = settlementCache.getObjective().getName();

			String prompt = YOU_PROMPT + "What is the current settlement objective ?" + System.lineSeparator() + System.lineSeparator()
					+ "Current Development Objective : " + obj + System.lineSeparator() + System.lineSeparator()
					+ "Would you like to change it?";
			boolean change = textIO.newBooleanInputReader().read(prompt); // .withDefaultValue(true)

			if (change) {
				String prompt2 = " 1. " + Msg.getString("ObjectiveType.crop") + System.lineSeparator() + " 2. "
						+ Msg.getString("ObjectiveType.manu") + System.lineSeparator() + " 3. "
						+ Msg.getString("ObjectiveType.research") + System.lineSeparator() + " 4. "
						+ Msg.getString("ObjectiveType.transportation") + System.lineSeparator() + " 5. "
						+ Msg.getString("ObjectiveType.trade") + System.lineSeparator() + " 6. "
						+ Msg.getString("ObjectiveType.tourism") + System.lineSeparator() + "Enter your choice (1-6)";
				int newObj = textIO.newIntInputReader().withMinVal(1).withMaxVal(6).read(prompt2);
				String s = "";

				if (newObj > 0 && newObj < 7) {

					String prompt3 = "Enter the level choice (1-3)";
					int newLevel = InteractiveTerm.getTextIO().newIntInputReader().withMinVal(1).withMaxVal(3)
							.read(prompt3);

					if (newLevel > 0 && newLevel < 4) {
						String newObjStr = settlementCache.getObjectiveArray()[newObj - 1];

						responseText.append(settlementCache + " : I've updated it for you as follows : ");
						responseText.append(System.lineSeparator());
						responseText.append(System.lineSeparator());
						// responseText.append(System.lineSeparator());

						s = "New Development Objective : " + newObjStr;

						settlementCache.setObjective(ObjectiveType.getType(newObjStr), newLevel);
						responseText.append(s);
						logger.config(s);
					}

					else {
						s = settlementCache + " : Invald level. Please try it again.";
						responseText.append(s);
						logger.config(s);
					}
				}

				else {
					s = settlementCache + " : Invald objective. Please try it again.";
					responseText.append(s);
					logger.config(s);
				}
			}
		}

		else if (text.equalsIgnoreCase("dash") || text.equalsIgnoreCase("dashboard")) {
			questionText = YOU_PROMPT + "I'd like to see the Commander's Dashboard";
			responseText.append(System.lineSeparator());

			String obj = settlementCache.getObjective().getName();
			double level = Math.round(settlementCache.getObjectiveLevel(ObjectiveType.getType(obj)) * 10.0)/10.0;
			
			responseText.append(addWhiteSpacesLeftName(" Development Objective", 25));
			responseText.append(addWhiteSpacesRightName("Level", 8));
			responseText.append(System.lineSeparator());
			responseText.append(" ----------------------------------- ");
			responseText.append(System.lineSeparator());
			responseText.append(addWhiteSpacesLeftName(" " + obj, 25));
			responseText.append(addWhiteSpacesRightName("" + level, 7));	
					
			String[] s = new String[] { " Repair", " Maintenance", " EVA Suit Production" };

			GoodsManager goodsManager = settlementCache.getGoodsManager();

			int[] mods = new int[] { goodsManager.getRepairLevel(), goodsManager.getMaintenanceLevel(),
					goodsManager.getEVASuitLevel() };
			
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());
			responseText.append(addWhiteSpacesLeftName(" Category of Operations     Level ", 33));
			responseText.append(System.lineSeparator());
			responseText.append(" ----------------------------------- ");
			responseText.append(System.lineSeparator());
			responseText.append(printLevel(s, mods));
		}

		else if (text.equalsIgnoreCase("mission radius")) {
//			questionText = YOU_PROMPT + "I'd like to change the vehicle range for this settlement." ; 

//			double oldRange = settlementCache.getMaxMssionRange();

			List<String> missionNames = Settlement.getTravelMissionNames();
			int size = missionNames.size();
			
			String prompt = System.lineSeparator() 
					+ " Type of Mission             Mission Radius" + System.lineSeparator() 
					+ "--------------------------------------------" + System.lineSeparator();
			
			String WHITESPACE_1 = " ";
			String WHITESPACE_2 = "  ";
			String DOT = ". ";
			String KM = "  km";
			
			StringBuilder sb = new StringBuilder(prompt);
			for (int i=0; i < size; i++) {
				if (i < 10)
					sb.append(WHITESPACE_1);

				sb.append(i)
				.append(DOT)
				.append(addWhiteSpacesLeftName(missionNames.get(i), 28))
				.append(WHITESPACE_2)
				.append(settlementCache.getMissionRadius(i))
				.append(KM)
				.append(System.lineSeparator());
			}
			
			sb.append(System.lineSeparator()).append("Which one would you like to change ?");
			
			int selected = textIO.newIntInputReader().withMinVal(0).withMaxVal(10).read(sb.toString());
					
			double newRange = textIO.newDoubleInputReader().withMinVal(50.0).withMaxVal(2200.0)
					.read(System.lineSeparator() + "Enter the new mission radius (a number between 50.0 and 2200.0 [in km])");
			
			newRange = Math.round(newRange*10.0)/10.0;
			
			double oldRange = Math.round(settlementCache.getMissionRadius(selected)*10.0)/10.0;
			
			settlementCache.setMissionRadius(selected, newRange);
		
			String s = "";
			
			if (newRange >= 50.0 && newRange <= 2200.0) {

				settlementCache.setMaxMssionRange(newRange);

				responseText.append(settlementCache + " : I've updated the mission range for '" 
							+ missionNames.get(selected) + "' as follows : ");
				responseText.append(System.lineSeparator());
				responseText.append(System.lineSeparator());
				s = "  Old Mission Radius :  " + oldRange + " km";
				responseText.append("  Old Mission Radius :  ");
				responseText.append(addWhiteSpacesLeftName(oldRange + "", 7));
				responseText.append(" km");
				logger.config(s);
				responseText.append(System.lineSeparator());
				s = "  New Mission Radius :  " + newRange + " km";
				responseText.append("  New Mission Radius :  ");
				responseText.append(addWhiteSpacesLeftName(newRange + "", 7));
				responseText.append(" km");
				logger.config(s);

			} else {
//	    	       responseText.append(System.lineSeparator());
				s = settlementCache + " : It's outside the normal range of radius. Aborted.";
				responseText.append(s);
				logger.config(s);
			}

		}

		else if (text.equalsIgnoreCase("proposal")) {
			questionText = YOU_PROMPT + "Can you show me the list of proposals ?";

//			responseText.append(System.lineSeparator());
//			responseText.append(SYSTEM_PROMPT);
			responseText.append("[EXPERIMENTAL & NON-FUNCTIONAL] Below is a list of proposals for your review :");
			responseText.append(System.lineSeparator());
			responseText.append("1. Safety and Health Measures");
			responseText.append(System.lineSeparator());
			responseText.append("2. Manufacturing Priority");
			responseText.append(System.lineSeparator());
			responseText.append("3. Food Allocation Plan");
			responseText.append(System.lineSeparator());

		}

		else if (text.toLowerCase().contains("researchers")) {
			questionText = YOU_PROMPT + "Can you show me a list of researchers and their endeavors ?";
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());
			
			List<Person> people = new ArrayList<>(settlementCache.getAllAssociatedPeople());
			int size0 = people.size();

			List<ScienceType> sciences = Arrays.asList(ScienceType.values());			
			int size1 = sciences.size();

			for (int i = 0; i < size0; i++) {
				Person p = people.get(i);
				responseText.append("  Name                           Job");
				responseText.append(System.lineSeparator());
				responseText.append(" ------------------------------------------ ");
				responseText.append(System.lineSeparator());
				responseText.append(addWhiteSpacesLeftName(" #" +  i+1 + ". " + p.getName(), 28));
				responseText.append(addWhiteSpacesLeftName(" " + p.getJobName(), 20));
				responseText.append(System.lineSeparator());
				responseText.append(System.lineSeparator());
				responseText.append(System.lineSeparator());
				responseText.append(" Ongoing Primary Study             Phase");
				responseText.append(System.lineSeparator());
				responseText.append(" ------------------------------------------------ ");
				responseText.append(System.lineSeparator());

				ScientificStudy ss = scientificManager.getOngoingPrimaryStudy(p);
				String priName = "";
				String priPhase = "";
				if (ss != null) {
					priName = ss.getScienceName();
					priPhase = ss.getPhase();
				}
				else {
					responseText.append("   None");
					responseText.append(System.lineSeparator());
					responseText.append(System.lineSeparator());
				}
				
				responseText.append(addWhiteSpacesLeftName("  " + priName, 15));
				responseText.append(addWhiteSpacesLeftName("  " + priPhase, 20));
				
				responseText.append(System.lineSeparator());
				responseText.append(System.lineSeparator());
				responseText.append(System.lineSeparator());
				
				responseText.append(" Ongoing Collaborative Studies     Phase");
				responseText.append(System.lineSeparator());
				responseText.append(" ------------------------------------------------ ");
				responseText.append(System.lineSeparator());
				
				List<ScientificStudy> cols = scientificManager.getOngoingCollaborativeStudies(p);
				int size2 = cols.size();

				if (size2 == 0) {
					responseText.append("   None");
					responseText.append(System.lineSeparator());
					responseText.append(System.lineSeparator());
				}
				else {
					for (int k = 0; k < size2; k++) {
						String secName = "None";
						String secPhase = "";
						if (ss != null) {
							secName = cols.get(k).getScienceName();
							secPhase = cols.get(k).getPhase();
						}
						
						responseText.append(addWhiteSpacesLeftName(" " + secName, 15));
						responseText.append(addWhiteSpacesLeftName(" " + secPhase, 20));
						responseText.append(System.lineSeparator());
						responseText.append(System.lineSeparator());
					}
				}
				
				
				responseText.append(System.lineSeparator());
				responseText.append("  Subject         Achievement Score ");
				responseText.append(System.lineSeparator());
				responseText.append(" --------------------------------------- ");
				responseText.append(System.lineSeparator());
				
				for (int j = 0; j < size1; j++) {
					ScienceType t = sciences.get(j);

					double score = p.getScientificAchievement(t);
					responseText.append(addWhiteSpacesLeftName(" " + t.getName(), 18));
					responseText.append(addWhiteSpacesRightName("     " + score, 8));
					responseText.append(System.lineSeparator());
				}
				
				responseText.append(System.lineSeparator());
				responseText.append(System.lineSeparator());
			}
		}
		
		else if (text.toLowerCase().contains("science")) {

			questionText = YOU_PROMPT + "How are the scientific endeavors in this settlement ?";

			int WIDTH = 9;
			double total = 0;
			// Use Guava's multimap to handle duplicate key
			Multimap<Double, ScienceType> map = ArrayListMultimap.create();
//			Map<Double, String> map = new HashMap<>();
			List<Double> list = new ArrayList<>();

			List<ScienceType> sciences = Arrays.asList(ScienceType.values());
			int size = sciences.size();
//			double[][] array2D = new double[size][5];
//			double[] scoreArray = new double[] {};

			for (int i = 0; i < size; i++) {
				ScienceType t = sciences.get(i);
				double score = scientificManager.getScienceScore(settlementCache, t);
				double achievement = settlementCache.getScientificAchievement(t);
//				String n = Conversion.capitalize(sciences.get(i).toString().toLowerCase());
//				double[] array = scientificManager.getNumScienceStudy(settlementCache, sciences.get(i));
//				array2D[i] = array;
				double subtotal = score + achievement;
				total += subtotal;
				list.add(subtotal);
				map.put(subtotal, t);
			}

//			responseText.append(System.lineSeparator());
//			responseText.append("The scientific studies undertaken in " + settlementCache + " :");
			responseText.append(System.lineSeparator());
			responseText.append(" ----------------------------------------------------------------------------------");
			responseText.append(System.lineSeparator());

			responseText.append(" Rank  ");
			responseText.append(addWhiteSpacesRightName("Score", 5));
			responseText.append(addWhiteSpacesRightName("Science", 13));

			responseText.append(addWhiteSpacesRightName(" Succ ", WIDTH));
			responseText.append(addWhiteSpacesRightName(" Fail ", WIDTH));
			responseText.append(addWhiteSpacesRightName(" Canx ", WIDTH));
			responseText.append(addWhiteSpacesRightName(" Prim ", WIDTH));
			responseText.append(addWhiteSpacesRightName("Collab", WIDTH));
			responseText.append(addWhiteSpacesRightName("Achiev", WIDTH));
			
			responseText.append(System.lineSeparator());
			responseText.append(" ----------------------------------------------------------------------------------");
			responseText.append(System.lineSeparator());

			list.sort((Double d1, Double d2) -> -d1.compareTo(d2));

//			int size = list.size();
			for (int i = 0; i < size; i++) {
				double score = list.get(i);
//				String space = "";
				String scoreStr = Math.round(score * 10.0) / 10.0 + "";
//				int num = scoreStr.length();
//				if (num == 2)
//					space = "   ";
//				else if (num == 3)
//					space = "  ";
//				else if (num == 4)
//					space = " ";
//				else if (num == 5)
//					space = "";

				List<ScienceType> sciTypes = new ArrayList<>(map.get(score));
				// Pick the first one
				ScienceType t = sciTypes.get(0);
				String n = Conversion.capitalize(t.getName().toLowerCase());

				// List<ScientificStudy>
				int suc = scientificManager.getAllSuccessfulStudies(settlementCache, t).size();
				int fail = scientificManager.getAllFailedStudies(settlementCache, t).size();
				int canx = scientificManager.getAllCanceledStudies(settlementCache, t).size();
				int oPri = scientificManager.getOngoingPrimaryStudies(settlementCache, t).size();
				int oCol = scientificManager.getOngoingCollaborativeStudies(settlementCache, t).size();
				double achieve = Math.round(10.0 *settlementCache.getScientificAchievement(t))/10.0;
				
				int rank = i + 1;
				String rankStr;
				if (rank >= 10)
					rankStr = "  " + (i + 1) + "   ";
				else
					rankStr = "   " + (i + 1) + "   ";
						
				responseText.append(rankStr);
				responseText.append(addWhiteSpacesRightName(scoreStr, 5));
				responseText.append(addWhiteSpacesRightName("  " + n, 14));

				responseText.append(addWhiteSpacesRightName(suc + "", WIDTH));
				responseText.append(addWhiteSpacesRightName(fail + "", WIDTH));
				responseText.append(addWhiteSpacesRightName(canx + "", WIDTH));
				responseText.append(addWhiteSpacesRightName(oPri + "", WIDTH));
				responseText.append(addWhiteSpacesRightName(oCol + "", WIDTH));
				responseText.append(addWhiteSpacesRightName(achieve + "", WIDTH));
				
				map.remove(score, t);
				responseText.append(System.lineSeparator());
			}

			responseText.append(" ----------------------------------------------------------------------------------");
			responseText.append(System.lineSeparator());
			responseText.append(" Overall : " + Math.round(total * 10.0) / 10.0);
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());
			responseText.append("Notes:");
			responseText.append(System.lineSeparator());
			responseText.append("1. Succ   : # of Successfully Completed Research");
			responseText.append(System.lineSeparator());
			responseText.append("2. Fail   : # of Failed Research");
			responseText.append(System.lineSeparator());
			responseText.append("3. Canx   : # of Cancelled Research");
			responseText.append(System.lineSeparator());
			responseText.append("4. Prim   : # of Ongoing Primary Research");
			responseText.append(System.lineSeparator());
			responseText.append("5. Collab : # of Ongoing Collaborative Research");
			responseText.append(System.lineSeparator());
			responseText.append("6. Achiev : the settlement's achievement score on completed studies");
			
		}

		else if (text.toLowerCase().contains("social")) {
//			text.toLowerCase().contains("relationship")
//			|| text.toLowerCase().contains("relation")

			questionText = YOU_PROMPT + "How is the overall social score in this settlement ?";

			double score = relationshipManager.getRelationshipScore(settlementCache);

			responseText.append(System.lineSeparator());
			responseText.append(settlementCache.getName() + "'s social score : " + fmt1.format(score));
//			responseText.append(System.lineSeparator());

		}

		else if (text.toLowerCase().contains("time") || text.toLowerCase().contains("date")) {
			questionText = YOU_PROMPT + "What day or time is it ?";
			responseText.append(settlementCache.getName() + " : ");
			responseText.append("see below");
			responseText.append(System.lineSeparator());

			responseText.append(printTime());

		}

		else if (text.toLowerCase().contains("task")) {
//				|| text.toLowerCase().contains("activity")
//				|| text.toLowerCase().contains("doing")
//				|| text.toLowerCase().contains("action")) {
			questionText = YOU_PROMPT + "What is everybody doing at this moment? ";
			responseText.append(settlementCache + " : ");
			responseText.append("Here is the task roster : ");
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());
			responseText.append("(A). Settlers");
			responseText.append(System.lineSeparator());

			Map<String, List<Person>> map = settlementCache.getAllAssociatedPeople().stream()
					.collect(Collectors.groupingBy(Person::getTaskDescription));

			for (Map.Entry<String, List<Person>> entry : map.entrySet()) {
				String task = entry.getKey();
				List<Person> plist = entry.getValue();

				if (task != null && !task.replaceAll(" ", "").equals("")) {
					responseText.append(System.lineSeparator());
					responseText.append("  ");
					responseText.append(task);
					responseText.append(System.lineSeparator());
					responseText.append(" ");
					int num = task.length() + 2;
					if (num > 0) {
						for (int i = 0; i < num; i++) {
							responseText.append("-");
						}
					}
					responseText.append(System.lineSeparator());

				} else {
					responseText.append(System.lineSeparator());
					responseText.append("  ");
					responseText.append("None");
					responseText.append(System.lineSeparator());
					responseText.append(" ");
					int num = 6;
					if (num > 0) {
						for (int i = 0; i < num; i++) {
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

			Map<String, List<Robot>> botMap = settlementCache.getAllAssociatedRobots().stream()
					.collect(Collectors.groupingBy(Robot::getTaskDescription));

			for (Map.Entry<String, List<Robot>> entry : botMap.entrySet()) {
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
						for (int i = 0; i < num; i++) {
							responseText.append("-");
						}
					}
					responseText.append(System.lineSeparator());

				} else {
					responseText.append(System.lineSeparator());
					responseText.append(" ");
					responseText.append("None");
					responseText.append(System.lineSeparator());
					responseText.append(" ");
					int num = 6;
					if (num > 0) {
						for (int i = 0; i < num; i++) {
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

		else if (text.equalsIgnoreCase("mission")) {
//			|| text.equalsIgnoreCase("trip")
//				|| text.equalsIgnoreCase("excursion")) {
			responseText.append("What would you like to know about mission ? ");
			responseText.append(System.lineSeparator());
			responseText.append("Say 'mission plan', 'mission now', etc.");
//			responseText.append(System.lineSeparator());
		}

		else if (text.equalsIgnoreCase("mission plan")) {

			questionText = "";// YOU_PROMPT + "Show me the statistics on the mission plans submitted.";

			String prompt2 = YOU_PROMPT + "Show me the statistics on the mission plans submitted."
					+ System.lineSeparator() + System.lineSeparator() + " 1. Today" + System.lineSeparator()
					+ " 2. last 3 sols (Each)" + System.lineSeparator() + " 3. Last 7 sols (Each)"
					+ System.lineSeparator() + " 4. last 3 sols (Combined)" + System.lineSeparator()
					+ " 5. Last 7 sols (Combined)" + System.lineSeparator() + " 6. Last 14 sols (Combined)"
					+ System.lineSeparator() + " 7. Last 28 sols (Combined)" + System.lineSeparator()
					+ " 8. Since the beginning (Combined)" + System.lineSeparator() + System.lineSeparator()
					+ "Enter your choice (1-8)";

			int newObj = textIO.newIntInputReader().withMinVal(1).withMaxVal(8).read(prompt2);

			if (newObj > 0 && newObj <= 3) {

				int today = marsClock.getMissionSol();
				int max = 0;
				if (newObj == 1) {
					max = 1;
//					responseText.append(System.lineSeparator());
					responseText.append("On Sol " + today + ", it shows ");
				} else if (newObj == 2) {
					max = 3;
//					responseText.append(System.lineSeparator());
					responseText.append("On Sol " + today + ", the last " + max + " sols shows ");
				} else if (newObj == 3) {
					max = 7;
//					responseText.append(System.lineSeparator());
					responseText.append("On Sol " + today + ", the last " + max + " sols shows ");
				}

				Map<Integer, List<MissionPlanning>> plannings = missionManager.getHistoricalMissions();

				int size = plannings.size();

//				int min = Math.min(size, max);

//				System.out.println("# of sols : " + size);

				if (size == 0) {
					responseText.append(System.lineSeparator());
					responseText.append("              # of plans : 0");
//					responseText.append("No mission plans have been submitted.");
				}

				else {
					int sol = marsClock.getMissionSol();
					for (int i = 0; i < max; i++) {
						if (sol - i > 0) {
							List<MissionPlanning> plans = plannings.get(sol - i);
							int approved = 0;
							int notApproved = 0;
							int pending = 0;

							responseText.append(System.lineSeparator());
							responseText.append(System.lineSeparator());
							responseText.append("           < Sol " + (sol - i) + " >");
							responseText.append(System.lineSeparator());
							responseText.append(" -----------------------------");

							if (plans != null && !plans.isEmpty()) {

								for (MissionPlanning mp : plans) {
									if (PlanType.PENDING == mp.getStatus())
										pending++;
									else if (PlanType.NOT_APPROVED == mp.getStatus())
										notApproved++;
									else if (PlanType.APPROVED == mp.getStatus())
										approved++;
								}

								responseText.append(System.lineSeparator());
								responseText.append("     # of plans approved : " + approved);
								responseText.append(System.lineSeparator());
								responseText.append(" # of plans not approved : " + notApproved);
								responseText.append(System.lineSeparator());
								responseText.append("      # of plans pending : " + pending);
								responseText.append(System.lineSeparator());
							}

							else {
								responseText.append(System.lineSeparator());
								responseText.append("              # of plans : 0");
								responseText.append(System.lineSeparator());
							}
						}
					}
				}
			}

			else if (newObj > 3 && newObj <= 8) {
				// 4. last 3 sols (Combined)
				// 5. Last 7 sols (Combined)
				// 6. Last 14 sols (Combined)
				// 7. Last 28 sols (Combined)
				// 8. Since the beginning (Combined)
				int today = marsClock.getMissionSol();
				int max = 0;
				if (newObj == 4) {
					max = 3;
					responseText.append("On Sol " + today + ", the combined data for the last 3 sols shows ");
				} else if (newObj == 5) {
					max = 7;
					responseText.append("On Sol " + today + ", the combined data for the last 7 sols shows ");
				} else if (newObj == 6) {
					max = 14;
					responseText.append("On Sol " + today + ", the combined data for the last 14 sols shows ");
				} else if (newObj == 7) {
					max = 28;
					responseText.append("On Sol " + today + ", the combined data for the last 28 sols shows ");
				} else if (newObj == 8) {
					max = Integer.MAX_VALUE;
					responseText.append("Since the beginning, the combined data shows ");
				}

				Map<Integer, List<MissionPlanning>> plannings = missionManager.getHistoricalMissions();

				int size = Math.min(plannings.size(), max);

				if (size == 0) {
					responseText.append(System.lineSeparator());
					responseText.append("              # of plans : 0");
//					responseText.append("No mission plans have been submitted.");
				}

				else {
					int sol = marsClock.getMissionSol();
					int approved = 0;
					int notApproved = 0;
					int pending = 0;

					for (int i = 0; i < size + 1; i++) {
						if (sol - i > 0) {
							List<MissionPlanning> plans = plannings.get(sol - i);

							if (plans != null && !plans.isEmpty()) {

								for (MissionPlanning mp : plans) {
									if (PlanType.PENDING == mp.getStatus())
										pending++;
									else if (PlanType.NOT_APPROVED == mp.getStatus())
										notApproved++;
									else if (PlanType.APPROVED == mp.getStatus())
										approved++;
								}
							}
						}
					}

					responseText.append(System.lineSeparator());
					responseText.append("     # of plans approved : " + approved);
					responseText.append(System.lineSeparator());
					responseText.append(" # of plans not approved : " + notApproved);
					responseText.append(System.lineSeparator());
					responseText.append("      # of plans pending : " + pending);
					responseText.append(System.lineSeparator());
				}
			}

			else {
				responseText.append("Invalid choice. Please try again.");
			}
		}

		else if (text.equalsIgnoreCase("mission now")) {
			questionText = YOU_PROMPT + "Are there any on-going/pending missions at this moment? ";

			List<Mission> missions = missionManager.getMissionsForSettlement(settlementCache);
//			missions = missions.stream()
//					.filter(m -> m.getAssociatedSettlement() == settlementCache)
//					.collect(Collectors.toList());

			if (missions.isEmpty()) {
				responseText.append(settlementCache + " : ");
				responseText.append("no on-going/pending missions right now.");
			}

			else {
				responseText.append(settlementCache + " : ");
				responseText.append("here's the mission roster.");
				responseText.append(System.lineSeparator());
//				responseText.append(System.lineSeparator());

				for (int i = 0; i < missions.size(); i++) {
					Mission mission = missions.get(i);
					int num = mission.getName().length() + 17;

					Collection<MissionMember> members = mission.getMembers();
					// Collections.sort(members);

					Person startingPerson = mission.getStartingMember();
					members.remove(startingPerson);

					List<MissionMember> plist = new ArrayList<>(members);

					double dist = 0;
					double trav = 0;
					Vehicle v = null;

					if (mission instanceof VehicleMission) {
						v = ((VehicleMission) mission).getVehicle();
						dist = Math.round(((VehicleMission) mission).getProposedRouteTotalDistance() * 10.0) / 10.0;
						trav = Math.round(((VehicleMission) mission).getActualTotalDistanceTravelled() * 10.0) / 10.0;
					}

					if (mission != null) {
						responseText.append(System.lineSeparator());
						responseText.append(" (" + (i + 1) + "). " + mission.getName());
						responseText.append(System.lineSeparator());
						responseText.append(" ");
						if (num > 0) {
							for (int j = 0; j < num; j++) {
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
							for (int j = 0; j < num; j++) {
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
//							responseText.append(System.lineSeparator());
//						}
					}

//					responseText.append(" ");
//					if (num > 0) {
//						for (int j=0; j<num; j++) {
//							responseText.append("-");
//						}
//					}
//					
					responseText.append(System.lineSeparator());
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

		else if (text.toLowerCase().contains("where") || text.toLowerCase().contains("location")
				|| text.toLowerCase().contains("located")) {
			questionText = YOU_PROMPT + "What settlement are you at?";
			// TODO: add to tell nearby georgraphical features. e.g. at what basin
			responseText.append(settlementCache + " : ");
			responseText.append("We're located at ");
			responseText.append(settlementCache.getCoordinates());
		}

		else if (text.toLowerCase().contains("country") || text.toLowerCase().contains("nation")
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

			Map<String, List<Person>> map = settlementCache.getAllAssociatedPeople().stream()
					.collect(Collectors.groupingBy(Person::getCountry));

			for (Map.Entry<String, List<Person>> entry : map.entrySet()) {
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
				for (int i = 0; i < list.size(); i++) {
					responseText.append("(" + (i + 1) + "). ");
					responseText.append(list.get(i));
					responseText.append(System.lineSeparator());
				}
			}
		}

		else if (text.toLowerCase().equalsIgnoreCase("role")) {
			questionText = YOU_PROMPT + "What are the roles ?";
			responseText.append(settlementCache + " : ");
			responseText.append("See the table below.");
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());
			
			responseText.append(addWhiteSpacesLeftName("   Name ", 30));
			responseText.append(addWhiteSpacesLeftName(" Role ", 25));
			responseText.append(System.lineSeparator());
			responseText.append(" -------------------------------------------- ");
			responseText.append(System.lineSeparator());		

			List<Person> list = settlementCache.getAllAssociatedPeople().stream()
//					.sorted(Comparator.reverseOrder())
//					.sorted((f1, f2) -> Long.compare(f2.getRole().getType().ordinal(), f1.getRole().getType().ordinal()))
					// .sorted((p1, p2)->
					// p1.getRole().getType().getName().compareTo(p2.getRole().getType().getName()))
					.sorted(Comparator.comparing(o -> o.getRole().getType().ordinal())).collect(Collectors.toList());

//			for (Person p : list) {
			for (int i = 0; i < list.size(); i++) {	
				String role = list.get(i).getRole().getType().getName();
				String numStr = " " + (i+1) + ". ";
				if (i > 9)
					numStr = (i+1) + ". ";
				
				responseText.append(addWhiteSpacesLeftName(numStr + list.get(i), 30));

				responseText.append(addWhiteSpacesLeftName(" " + role, 25));
				responseText.append(System.lineSeparator());
			}
		}

		else if (text.toLowerCase().contains("weather")) {

			questionText = YOU_PROMPT + "How's the weather in " + settlementCache.toString() + " ?";
			responseText.append(System.lineSeparator());

			int max = 28;
			responseText.append(addWhiteSpacesRightName("Name : ", max));
			responseText.append(settlementCache);
			if (masterClock == null)
				masterClock = sim.getMasterClock();
			if (marsClock == null)
				marsClock = masterClock.getMarsClock();
			if (mars == null)
				mars = sim.getMars();
			if (weather == null)
				weather = mars.getWeather();
			if (surfaceFeatures == null)
				surfaceFeatures = mars.getSurfaceFeatures();
			if (orbitInfo == null)
				orbitInfo = mars.getOrbitInfo();

			String DEGREE_CELSIUS = Msg.getString("direction.degreeSign"); //$NON-NLS-1$
			String DEGREE = Msg.getString("direction.degreeSign"); //$NON-NLS-1$

			Coordinates location = settlementCache.getCoordinates();
//			System.out.println("location in ChatUtils : " + location);
//			String lat = location.getFormattedLatitudeString();
//			String lon = location.getFormattedLongitudeString();

			responseText.append(System.lineSeparator());
//			responseText.append(settlementCache + " is at " + location);//(" + lat + ", " + lon + ")"); 
			responseText.append(addWhiteSpacesRightName("Location : ", max) + location.toString());
			responseText.append(System.lineSeparator());

			String date = marsClock.getDateString();
			responseText.append(addWhiteSpacesRightName("Date and Time : ", max) + date);

			String time = marsClock.getDecimalTimeString();
			responseText.append(" at " + time);

			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());

			double t = weather.getTemperature(location);
			String tt = fmt.format(t) + DEGREE_CELSIUS;
			responseText.append(addWhiteSpacesRightName("Outside temperature : ", max) + tt);
			responseText.append(System.lineSeparator());

			double p = weather.getAirPressure(location);
			String pp = fmt2.format(p) + " " + Msg.getString("pressure.unit.kPa"); //$NON-NLS-1$
			responseText.append(addWhiteSpacesRightName("Air Pressure : ", max) + pp);
			responseText.append(System.lineSeparator());

			double ad = weather.getAirDensity(location);
			String aad = fmt2.format(ad) + " " + Msg.getString("airDensity.unit.gperm3"); //$NON-NLS-1$
			responseText.append(addWhiteSpacesRightName("Air Density : ", max) + aad);
			responseText.append(System.lineSeparator());

			double ws = weather.getWindSpeed(location);
			String wws = fmt2.format(ws) + " " + Msg.getString("windspeed.unit.meterpersec"); //$NON-NLS-1$
			responseText.append(addWhiteSpacesRightName("Wind Speed : ", max) + wws);
			responseText.append(System.lineSeparator());

			double wd = weather.getWindDirection(location);
			String wwd = fmt.format(wd) + Msg.getString("windDirection.unit.deg"); //$NON-NLS-1$
			responseText.append(addWhiteSpacesRightName("Wind Direction : ", max) + wwd);
			responseText.append(System.lineSeparator());

			double od = surfaceFeatures.getOpticalDepth(location);
			String ood = fmt2.format(od);
			responseText.append(addWhiteSpacesRightName("Optical Depth : ", max) + ood);
			responseText.append(System.lineSeparator());

			double sza = orbitInfo.getSolarZenithAngle(location);
			String ssza = fmt2.format(sza * RADIANS_TO_DEGREES) + DEGREE;
			responseText.append(addWhiteSpacesRightName("Solar Zenith Angle : ", max) + ssza);
			responseText.append(System.lineSeparator());

			double sda = orbitInfo.getSolarDeclinationAngleDegree();
			String ssda = fmt2.format(sda) + DEGREE;
			responseText.append(addWhiteSpacesRightName("Solar Declination Angle : ", max) + ssda);
			responseText.append(System.lineSeparator());

			double si = surfaceFeatures.getSolarIrradiance(location);
			String ssi = fmt2.format(si) + " " + Msg.getString("solarIrradiance.unit"); //$NON-NLS-1$
			responseText.append(addWhiteSpacesRightName("Solar Irradiance : ", max) + ssi);
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
			List<Person> buriedP = new ArrayList<>(settlementCache.getBuriedPeople());
			List<Person> onMission = new ArrayList<>(settlementCache.getOnMissionPeople());

			Collections.sort(all);
			Collections.sort(eva);
			Collections.sort(indoorP);
			Collections.sort(deceasedP);
			Collections.sort(buriedP);
			Collections.sort(onMission);

			int numAll = all.size();
			int numIndoor = indoorP.size();// settlementCache.getIndoorPeopleCount();
			int numDead = deceasedP.size();// settlementCache.getNumDeceased();
			int numBuried = buriedP.size();
			int numEva = eva.size();// settlementCache.getNumOutsideEVAPeople(); //total - indoor - dead;
			int numMission = onMission.size();

			questionText = YOU_PROMPT + "Who are the settlers ? ";
			responseText.append(settlementCache + " : below is the brief summary of the whereabout of the settlers :");
//			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());
			responseText.append(System.lineSeparator());
			responseText.append("          Summary");
			responseText.append(System.lineSeparator());
			responseText.append(" --------------------------");
			responseText.append(System.lineSeparator());
			responseText.append("        Registered : " + numAll);
			responseText.append(System.lineSeparator());
			responseText.append("            Inside : " + numIndoor);
			responseText.append(System.lineSeparator());
			responseText.append("      On a Mission : " + numMission);
			responseText.append(System.lineSeparator());
			responseText.append("     EVA Operation : " + numEva);
			responseText.append(System.lineSeparator());
			responseText.append(" Deceased (Buried) : " + numDead + " (" + numBuried + ")");
			responseText.append(System.lineSeparator());

			// Indoor
			responseText.append(System.lineSeparator());
			responseText.append("  A. Registered Citizens");
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

			// Buried
			responseText.append(System.lineSeparator());
			responseText.append("  F. Buried");
			responseText.append(System.lineSeparator());
			responseText.append("  ---------");
			responseText.append(System.lineSeparator());

			responseText.append(printList(buriedP));

		}

		else if (text.toLowerCase().contains("bed")) {
//				|| text.toLowerCase().contains("sleep") 
//				|| text.equalsIgnoreCase("lodging")
//				|| text.toLowerCase().contains("quarters")) 

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

			questionText = YOU_PROMPT + "What are the status of the vehicles in the settlement ? ";
			responseText.append(System.lineSeparator());

			Collection<Vehicle> list = settlementCache.getAllAssociatedVehicles();

			// Sort the vehicle list according to the type
			List<Vehicle> vlist = list.stream().sorted((p1, p2) -> p1.getVehicleType().compareTo(p2.getVehicleType()))
					.collect(Collectors.toList());

			int SPACING = 20;
			// Print the heading of each column
			String nameStr = "     Name  ";
			responseText.append(nameStr);
			// Add spaces
			int num0 = SPACING - nameStr.length() - 2;
			for (int i = 0; i < num0; i++) {
				responseText.append(ONE_SPACE);
			}

			String typeStr = "     Type  ";
			responseText.append(typeStr);
			// Add spaces
			int num00 = SPACING - typeStr.length();
			for (int i = 0; i < num00; i++) {
				responseText.append(ONE_SPACE);
			}

			String missionStr = "     Mission  ";
			responseText.append(missionStr);
			// Add spaces
			int num000 = SPACING - missionStr.length() + 2;
			for (int i = 0; i < num000; i++) {
				responseText.append(ONE_SPACE);
			}

			String personStr = "     Lead  ";
			responseText.append(personStr);

			responseText.append(System.lineSeparator());
			responseText.append("  ------------------------------------------------------------------------");
			responseText.append(System.lineSeparator());

			for (Vehicle v : vlist) {
				// Print vehicle name
				responseText.append("  " + v.getName());
				int num2 = SPACING - v.getName().length() - 2;
				if (num2 > 0) {
					for (int i = 0; i < num2; i++) {
						responseText.append(ONE_SPACE);
					}
				}

				String vTypeStr = Conversion.capitalize(v.getVehicleType());
				if (vTypeStr.equalsIgnoreCase("Light Utility Vehicle"))
					vTypeStr = "LUV";

				responseText.append(vTypeStr);

				// Print vehicle type
				int num3 = SPACING - vTypeStr.length();
				if (num3 > 0) {
					for (int i = 0; i < num3; i++) {
						responseText.append(ONE_SPACE);
					}
				}

				// Print mission name
				String missionName = " ";
				Mission mission = null;
				List<Mission> missions = missionManager.getMissions();
				for (Mission m : missions) {
					if (m instanceof VehicleMission) {
						Vehicle vv = ((VehicleMission) m).getVehicle();
						if (vv.getName().equals(v.getName())) {
							mission = m;
							missionName = m.getDescription();
						}
					}
				}
				responseText.append(missionName);

				// Print the starting member
				int num4 = SPACING - missionName.length() + 2;
				if (num4 > 0) {
					for (int i = 0; i < num4; i++) {
						responseText.append(ONE_SPACE);
					}
				}

				String personName = " ";

				if (!missionName.equalsIgnoreCase(" "))
					personName = mission.getStartingMember().getName();

				responseText.append(personName);

				responseText.append(System.lineSeparator());
			}

//			responseText.append(DASHES_0);
			responseText.append(System.lineSeparator());
			// Center the name of the settlement
//			int num = (DASHES_0.length() - settlementCache.getName().length())/2;
//			if (num > 0) {
//				for (int i=0; i<num; i++) {
//					responseText.append(" ");
//				}
//			}

//			responseText.append(settlementCache.getName());
//			responseText.append(System.lineSeparator());
			responseText.append(DASHES_0);
			responseText.append(System.lineSeparator());
			responseText.append("                             Total # of Rovers : ");
			responseText.append(settlementCache.getAllAssociatedVehicles().size());
			responseText.append(System.lineSeparator());
			responseText.append(DASHES_0);
			responseText.append(System.lineSeparator());
			responseText.append("                  # of Cargo Rovers on Mission : ");
			responseText.append(settlementCache.getCargoRovers(2).size());
			responseText.append(System.lineSeparator());
			responseText.append("              # of Transport Rovers on Mission : ");
			responseText.append(settlementCache.getTransportRovers(2).size());
			responseText.append(System.lineSeparator());
			responseText.append("               # of Explorer Rovers on Mission : ");
			responseText.append(settlementCache.getExplorerRovers(2).size());
			responseText.append(System.lineSeparator());
			responseText.append(" # of Light Utility Vehicles (LUVs) on Mission : ");
			responseText.append(settlementCache.getLUVs(2).size());
			responseText.append(System.lineSeparator());
			responseText.append(DASHES_0);
			responseText.append(System.lineSeparator());
			responseText.append("                        # of Rovers on Mission : ");
			responseText.append(settlementCache.getMissionVehicles().size());
			responseText.append(System.lineSeparator());
			responseText.append(DASHES_0);
			responseText.append(System.lineSeparator());

			responseText.append("              # of Parked/Garaged Cargo Rovers : ");
			responseText.append(settlementCache.getCargoRovers(1).size());
			responseText.append(System.lineSeparator());
			responseText.append("          # of Parked/Garaged Transport Rovers : ");
			responseText.append(settlementCache.getTransportRovers(1).size());
			responseText.append(System.lineSeparator());
			responseText.append("           # of Parked/Garaged Explorer Rovers : ");
			responseText.append(settlementCache.getExplorerRovers(1).size());
			responseText.append(System.lineSeparator());
			responseText.append("                      # of Parked/Garaged LUVs : ");
			responseText.append(settlementCache.getLUVs(1).size());
			responseText.append(System.lineSeparator());
			responseText.append(DASHES_0);
			responseText.append(System.lineSeparator());
			responseText.append("                    # of Rovers NOT on mission : ");
			responseText.append(settlementCache.getParkedVehicleNum());
			responseText.append(System.lineSeparator());

			responseText.append(System.lineSeparator());
//			responseText.append("      ----------------------------");
			responseText.append(System.lineSeparator());
//			responseText.append("  Inventory");
//			responseText.append(System.lineSeparator());
//			responseText.append("  ------------------------------------");
			responseText.append(System.lineSeparator());

//			responseText.append(System.lineSeparator());
		}

		else if (text.equalsIgnoreCase("bot") || text.equalsIgnoreCase("bots") || text.equalsIgnoreCase("robot")
				|| text.equalsIgnoreCase("robot")) {
			questionText = YOU_PROMPT + "What kind of bots do you have? ";
			responseText.append(settlementCache + " : we have " + settlementCache.getNumBots() + " bots.");
			Collection<Robot> list = settlementCache.getRobots();
			List<Robot> namelist = new ArrayList<>(list);
			Collections.sort(namelist);
			String s = "";
			for (int i = 0; i < namelist.size(); i++) {
				s = s + "(" + (i + 1) + "). " + namelist.get(i).getName() + System.lineSeparator();
			}
			// .replace("[", "").replace("]", "");//.replaceAll(", ", ",\n");
			// System.out.println("list : " + list);
			responseText.append(System.lineSeparator());
			responseText.append(s);
			responseText.append(System.lineSeparator());
		}

		else if (text.equalsIgnoreCase("key") || text.equalsIgnoreCase("keys") || text.equalsIgnoreCase("keyword")
				|| text.equalsIgnoreCase("keywords") || text.equalsIgnoreCase("/k")) {

//			help = true;
			questionText = REQUEST_KEYS;
			if (connectionMode == 0) {
				keywordText = SETTLEMENT_KEYWORDS;
			} else {
				keywordText = SETTLEMENT_KEYWORDS + KEYWORDS_HEIGHT;
			}
			// responseText.append(System.lineSeparator());
			responseText.append(keywordText);

		}

		else if (text.equalsIgnoreCase("help") || text.equalsIgnoreCase("/h") || text.equalsIgnoreCase("/?")
				|| text.equalsIgnoreCase("?")) {

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
//			responseText.append(clarify(SYSTEM)[1]);
			String[] txt = clarify(name, text);
			questionText = txt[0];
			responseText.append(txt[1]);
			
			return new String[] { questionText, responseText.toString() };
		}

		return new String[] { questionText, responseText.toString() };
	}

	/**
	 * Obtains the distances between settlements
	 * 
	 * @param responseText
	 * @param settlementList
	 * @return
	 */
	public static StringBuffer findSettlementDistances(StringBuffer responseText, List<Settlement> settlementList) {
		
		responseText.append("Inter-Settlement Distances :" + System.lineSeparator() + System.lineSeparator());
		
		String[] header = new String[] { " Settlement 1", " Settlement 2", " Distance [km]"};
		
		responseText.append(System.lineSeparator());
		responseText.append(System.lineSeparator());
		responseText.append(System.lineSeparator());
		responseText.append(addWhiteSpacesLeftName(header[0], 25));
		responseText.append(addWhiteSpacesLeftName(header[1], 25));
		responseText.append(addWhiteSpacesRightName(header[2], 12));
		responseText.append(System.lineSeparator());
		responseText.append(" " +  addDashes(67) + " ");
		responseText.append(System.lineSeparator());
		
		for (Settlement s1: settlementList) {
			for (Settlement s2: settlementList) {
				if (!s1.equals(s2)) {
					responseText.append(addWhiteSpacesLeftName(" " + s1.getName(), 25));
					responseText.append(addWhiteSpacesLeftName(" " + s2.getName(), 25));
					double distance = Math.round(s1.getCoordinates().getDistance(s2.getCoordinates())*100.0)/100.0;
					responseText.append(addWhiteSpacesRightName(distance +"", 12));
					responseText.append(System.lineSeparator());
				}
			}
			responseText.append(System.lineSeparator());
		}
		
		return responseText;
	}
	
}
