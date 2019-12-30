/**
 * Mars Simulation Project
 * ReviewMissionPlan.java
 * @version 3.1.0 2018-10-10
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.JobAssignment;
import org.mars_sim.msp.core.person.ai.mission.AreologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.BiologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.CollectIce;
import org.mars_sim.msp.core.person.ai.mission.CollectRegolith;
import org.mars_sim.msp.core.person.ai.mission.EmergencySupply;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.MeteorologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionPlanning;
import org.mars_sim.msp.core.person.ai.mission.PlanType;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.structure.ObjectiveType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.Administration;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * This class is a task for reviewing mission plans
 */
public class ReviewMissionPlan extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static transient Logger logger = Logger.getLogger(ReviewMissionPlan.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.reviewMissionPlan"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase REVIEWING = new TaskPhase(
			Msg.getString("Task.phase.reviewMissionPlan.reviewing")); //$NON-NLS-1$

	private static final TaskPhase APPROVING = new TaskPhase(Msg.getString("Task.phase.reviewMissionPlan.approving")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.1D;

	// Data members
	/** The administration building the person is using. */
	private Administration office;
	/** The role of the person who is reviewing the mission plan. */
	public RoleType roleType;
	/** The total time spent in reviewing a mission plan. */
//	private double timeReviewing;
	/**
	 * Constructor. This is an effort-driven task.
	 * 
	 * @param person the person performing the task.
	 */
	public ReviewMissionPlan(Person person) {
		// Use Task constructor.
		super(NAME, person, true, false, STRESS_MODIFIER, true, 45D + RandomUtil.getRandomInt(-10, 10));

//		logger.info(person + " was reviewing mission plan.");
				
		if (person.isInside()) {

//			int pop = person.getAssociatedSettlement().getNumCitizens();

				// If person is in a settlement, try to find an office building.
				Building officeBuilding = Administration.getAvailableOffice(person);

				// Note: office building is optional
				if (officeBuilding != null) {
					office = officeBuilding.getAdministration();	
					if (!office.isFull()) {
						office.addStaff();
						// Walk to the office building.
						walkToActivitySpotInBuilding(officeBuilding, true);
					}
				}

				else {
					Building dining = EatDrink.getAvailableDiningBuilding(person, false);
					// Note: dining building is optional
					if (dining != null) {
						// Walk to the dining building.
						walkToActivitySpotInBuilding(dining, true);
					}
//					else {
//						// work anywhere
//					}				
				}
				// TODO: add other workplace if administration building is not available

			}
			else {
				endTask();
			}
//		}
//		else {
//			endTask();
//		}

		// Initialize phase
		addPhase(REVIEWING);
		addPhase(APPROVING);
		
		setPhase(REVIEWING);
	}

	
	public static boolean isRoleValid(RoleType roleType) {
		return roleType == RoleType.PRESIDENT || roleType == RoleType.MAYOR
				|| roleType == RoleType.COMMANDER || roleType == RoleType.SUB_COMMANDER
				|| roleType == RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS
				|| roleType == RoleType.CHIEF_OF_MISSION_PLANNING
				|| roleType == RoleType.CHIEF_OF_ENGINEERING
				|| roleType == RoleType.CHIEF_OF_SAFETY_N_HEALTH
				|| roleType == RoleType.CHIEF_OF_SCIENCE
				|| roleType == RoleType.CHIEF_OF_SUPPLY_N_RESOURCES
				|| roleType == RoleType.CHIEF_OF_AGRICULTURE
				|| roleType == RoleType.MISSION_SPECIALIST;
	}
	
	@Override
	public FunctionType getLivingFunction() {
		return FunctionType.ADMINISTRATION;
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (REVIEWING.equals(getPhase())) {
			return reviewingPhase(time);
		} else if (APPROVING.equals(getPhase())) {
			return approvingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the reviewingPhasephase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double reviewingPhase(double time) {
//		LogConsolidated.log(Level.INFO, 20_000, sourceName, 
//				"[" + person.getAssociatedSettlement() + "] " + person + " had time to review some mission plans.");
		
        List<Mission> missions = missionManager.getPendingMissions(person.getAssociatedSettlement());
        
        GoodsManager goodsManager = person.getAssociatedSettlement().getGoodsManager();
        
		// Iterates through each pending mission 
		Iterator<Mission> i = missions.iterator();
		while (i.hasNext()) {
			Mission m = i.next();
			MissionPlanning mp = m.getPlan();
			
			if (m.getPlan() != null) {
	            PlanType status = mp.getStatus();
	
	            if (status != null && status == PlanType.PENDING) {
	            	
		            if (mp.getPercentComplete() >= 100D) {
		            	// Go to the finished phase and finalize the approval
		            	setPhase(APPROVING);
		                return time * .9; // return time is needed
		            }
		            
		            else {
		            	// if not 100% reviewed
		            	
						String reviewedBy = person.getName();
						
						Person p = m.getStartingMember();
						String requestedBy = p.getName();
		
		            	double score = 0;
            			
		            	Settlement reviewerSettlement = person.getAssociatedSettlement();
						String s = reviewerSettlement.getName();
						
						if (!reviewedBy.equals(requestedBy)
								&& mp.isReviewerValid(reviewedBy, reviewerSettlement.getNumCitizens())) {
							
						    if (getTimeCompleted() < getDuration() * .95) {
						    	
								LogConsolidated.log(Level.INFO, 15_000, sourceName, 
										"[" + s + "] " + reviewedBy + " was reviewing " + requestedBy
										+ "'s " + m.getDescription() + " mission plan.");
						    }
						    
						    else {
								List<JobAssignment> list = p.getJobHistory().getJobAssignmentList();
								int last = list.size() - 1;
								
								// 1. Reviews requester's cumulative job rating
								double rating = list.get(last).getJobRating();
								double cumulative_rating = 0;
								int size = list.size();
								for (int j = 0; j < size; j++) {
									cumulative_rating += list.get(j).getJobRating();
								}
								cumulative_rating = cumulative_rating / size;
				
								rating = (rating + cumulative_rating);
										
								// 2. Relationship Score 
								
								// 2a. Reviewer's view of the mission lead
								double relationshipWithReviewer = relationshipManager.getOpinionOfPerson(person, p);
								//Math.round(100D * relationshipManager.getOpinionOfPerson(person, p))/100D;
									
								double relationshipWithOthers = 0;
								int num = reviewerSettlement.getAllAssociatedPeople().size();
								for (Person pp : reviewerSettlement.getAllAssociatedPeople()) {
									relationshipWithOthers += relationshipManager.getOpinionOfPerson(person, pp);
								}
								
								// 2b. Others' view of the mission lead
								relationshipWithOthers = (int)(1.0 * relationshipWithOthers / num);
								
								int relation = (int)((relationshipWithReviewer + relationshipWithOthers) / 10D) ;
								
								// 3. Mission Qualification Score
								double qual = 0;
								
								if (m instanceof AreologyFieldStudy) {
									qual = .5 * ((AreologyFieldStudy)m).getMissionQualification(person);
								}
								else if (m instanceof BiologyFieldStudy) {
									qual = .5 * ((BiologyFieldStudy)m).getMissionQualification(person);
								}
								else if (m instanceof CollectIce) {
									qual = .25 * ((CollectIce)m).getMissionQualification(person);
								}
								else if (m instanceof CollectRegolith) {
									qual = .15 * ((CollectRegolith)m).getMissionQualification(person);
								}
								else if (m instanceof Exploration) {
									qual = .25 * ((Exploration)m).getMissionQualification(person);
								}
								else if (m instanceof MeteorologyFieldStudy) {
									qual = .5 * ((MeteorologyFieldStudy)m).getMissionQualification(person);
								}
								else if (m instanceof Mining) {
									qual = .3 * ((Mining)m).getMissionQualification(person);
								}
								else if (m instanceof RescueSalvageVehicle) {
									qual = .5 * ((RescueSalvageVehicle)m).getMissionQualification(person);
								}
								else if (m instanceof Trade) {
									qual = .35 * ((Trade)m).getMissionQualification(person);
								}
								else if (m instanceof TravelToSettlement) {
									qual = .5 * ((TravelToSettlement)m).getMissionQualification(person);
								}
								else
									qual = .4 * m.getMissionQualification(person);											
								
								qual = 2.5 * Math.round(qual * 10.0)/10.0;
								
								// 4. Settlement objective score
								double obj = 0;
								
								if (person.getAssociatedSettlement().getObjective() == ObjectiveType.CROP_FARM
										&& (m instanceof CollectIce
											|| m instanceof BiologyFieldStudy)) {
									obj += 5D * goodsManager.getCropFarmFactor();
								}	
								
								else if (person.getAssociatedSettlement().getObjective() == ObjectiveType.TOURISM
										&& (m instanceof AreologyFieldStudy
										|| m instanceof BiologyFieldStudy
										|| m instanceof MeteorologyFieldStudy
										|| m instanceof TravelToSettlement
										|| m instanceof Exploration)
										) {
									obj += 5D * goodsManager.getTourismFactor();
								}				
								
								else if (person.getAssociatedSettlement().getObjective() == ObjectiveType.TRADE_CENTER
										&& m instanceof Trade) {
									obj += 5D * goodsManager.getTradeFactor();
								}	
								
								else if (person.getAssociatedSettlement().getObjective() == ObjectiveType.TRANSPORTATION_HUB
										&& (m instanceof TravelToSettlement
										|| m instanceof Exploration)) {
									obj += 5D * goodsManager.getTransportationFactor();
								}	
								
								else if (person.getAssociatedSettlement().getObjective() == ObjectiveType.MANUFACTURING_DEPOT
										&& (m instanceof Mining
										|| m instanceof CollectRegolith)) {
									obj += 5D * goodsManager.getManufacturingFactor();
								}	
								
								// 5. emergency
								int emer = 0;
								if (m instanceof EmergencySupply
										|| m instanceof RescueSalvageVehicle) {
									emer = 50;
								}	
								
								// 6. site
								int site = 0;
								if (m instanceof CollectIce) {
									site = (int)(((CollectIce) m).getTotalSiteScore()/50 - 5);
									logger.info("Ice collection site score is " + site);
								}	
								
								// 7. proposed route distance (0 to 10 points)
								int dist = 0;
								if (m instanceof TravelToSettlement) {
									int max = (int)(((TravelToSettlement) m).getAssociatedSettlement().getMaxMssionRange());
									((TravelToSettlement) m).computeProposedRouteTotalDistance();
									int proposed = (int)(((TravelToSettlement) m).getProposedRouteTotalDistance());
									dist = (int)(1.0 * (max - proposed) / max * 10);
								}
								
								// 8. Leadership and Charisma
								int leadership = (int)(.075 * person.getNaturalAttributeManager()
													.getAttribute(NaturalAttributeType.LEADERSHIP)
												+ .025 * person.getNaturalAttributeManager()
													.getAttribute(NaturalAttributeType.ATTRACTIVENESS));				
		
								// 9. reviewer role weight
								RoleType role = person.getRole().getType();
								int weight = 0;
								
								if (role == RoleType.PRESIDENT)
									weight = 20;
								else if (role == RoleType.MAYOR)
									weight = 15;
								else if (role == RoleType.COMMANDER)
									weight = 10;
								else if (role == RoleType.SUB_COMMANDER)
									weight = 8;
								else if (role == RoleType.CHIEF_OF_MISSION_PLANNING)
									weight = 7;
								else if (role == RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS)
									weight = 6;
								else if (role == RoleType.CHIEF_OF_AGRICULTURE
									|| role == RoleType.CHIEF_OF_ENGINEERING
									|| role == RoleType.CHIEF_OF_SAFETY_N_HEALTH
									|| role == RoleType.CHIEF_OF_SCIENCE
									|| role == RoleType.CHIEF_OF_SUPPLY_N_RESOURCES
									)
									weight = 5;
								else if (role == RoleType.MISSION_SPECIALIST)
									weight = 4;
								else
									weight = 2;
								
								
								// 10. luck
								int luck = RandomUtil.getRandomInt(-5, 5);	
								
								// TODO: 9. Go to him/her to have a chat
								// TODO: 10. mission lead's leadership/charisma
								
								score = Math.round((rating + relation + qual + obj + emer + site + dist + leadership + weight + luck)* 10.0)/10.0;
	
								// Updates the mission plan status
								missionManager.scoreMissionPlan(mp, score, person);
															
								LogConsolidated.log(Level.INFO, 0, sourceName, 
										"[" + s + "] " + reviewedBy + " graded " + requestedBy
										+ "'s " + m.getDescription() + " mission plan as follows :");
								logger.info(" ---------------------------");
								logger.info(" (1)          Rating : " + rating); 
								logger.info(" (2)    Relationship : " + relation); 
								logger.info(" (3)  Qualifications : " + qual); 
								logger.info(" (4)       Objective : " + obj);
								logger.info(" (5)       Emergency : " + emer);
								logger.info(" (6)          Sites  : " + site);
								logger.info(" (7)       Distance  : " + dist);
								logger.info(" (8)      Leadership : " + leadership); 							
								logger.info(" (9)   Reviewer Role : " + weight); 
								logger.info(" (10)           Luck : " + luck); 
								logger.info(" ----------------------------");
								logger.info("           Sub Total : " + score);
								
							      // Add experience
						        addExperience(time);
					        
								// Do only one review each time
						        endTask();
						    }

						}
						
							
						if (mp.getPercentComplete() >= 100D) {
			            	// Go to the finished phase and finalize the approval
			            	setPhase(APPROVING);
			                return time * 0.2;
			            }
				        else if (mp.getPercentComplete() >= 60D) {
				        	int sol = marsClock.getMissionSol();
				        	int solRequest = m.getPlan().getMissionSol();
				        	if (sol - solRequest > 7) {
	    						// If no one else is able to offer the review after x days, 
	    						// do allow the review to go through even if the reviewer is not valid
				            	setPhase(APPROVING);
				                return time * 0.2;
				        	}
				        }
	
		            } // end of else // if (mp.getPercentComplete() >= 100D) {
		            
		            // The break below prevents a person from reviewing another mission plan in the same period of time
		            break;
				} // if (status != null && status == PlanType.PENDING)
			}
		} // end of while
		
        return time * 0.2;
	}

	/**
	 * Performs the finished phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double approvingPhase(double time) {
        
        List<Mission> missions = missionManager.getPendingMissions(person.getAssociatedSettlement());
 		// Iterates through each pending mission 
		Iterator<Mission> i = missions.iterator();
		while (i.hasNext()) {
			Mission m = i.next();		
			MissionPlanning mp = m.getPlan();
			
			if (mp != null) {
	            PlanType status = mp.getStatus();
	
	            if (status != null && status == PlanType.PENDING
	            		&& mp.getPercentComplete() >= 60D) {
	            	
					String reviewedBy = person.getName();
					
					Person p = m.getStartingMember();
					String requestedBy = p.getName();
				
					Settlement settlement = person.getAssociatedSettlement();
					String s = settlement.getName();
					
					double score = mp.getScore();
					
					if (settlement.passMissionScore(score)) {
						// Approved
						// Updates the mission plan status
						missionManager.approveMissionPlan(mp, p, PlanType.APPROVED);
							
						LogConsolidated.log(Level.INFO, 0, sourceName,
								"[" + s + "] " + reviewedBy + " approved " + requestedBy
								+ "'s " + m.getDescription() + " mission plan. Total Score: " 
								+ Math.round(score*10.0)/10.0 
								+ " (Min: " + settlement.getMinimumPassingScore() + ").");
					} else {
						// Not Approved
						// Updates the mission plan status
						missionManager.approveMissionPlan(mp, p, PlanType.NOT_APPROVED);
					
						LogConsolidated.log(Level.INFO, 0, sourceName, 
								"[" + s + "] " + reviewedBy + " did NOT approve " + requestedBy
								+ "'s " + m.getDescription() + " mission plan. Total Score: " 
								+ Math.round(score*10.0)/10.0 
								+ " (Min: " + settlement.getMinimumPassingScore() + ").");
					}
										
					settlement.saveMissionScore(score);
					
				      // Add experience
			        addExperience(time);
		        
					// Do only one review each time
			        return 0;//endTask();
				}
			}
		} // end of while
		
        return 0;
	}

	
	@Override
	protected void addExperience(double time) {
        double newPoints = time / 20D;
        int experienceAptitude = person.getNaturalAttributeManager().getAttribute(
                NaturalAttributeType.EXPERIENCE_APTITUDE);
        int leadershipAptitude = person.getNaturalAttributeManager().getAttribute(
                NaturalAttributeType.LEADERSHIP);
        newPoints += newPoints * (experienceAptitude + leadershipAptitude- 100D) / 100D;
        newPoints *= getTeachingExperienceModifier();
        person.getSkillManager().addExperience(SkillType.MANAGEMENT, newPoints, time);

	}

	@Override
	public void endTask() {
		super.endTask();

		// Remove person from administration function so others can use it.
		if (office != null && office.getNumStaff() > 0) {
			office.removeStaff();
		}
	}

	@Override
	public int getEffectiveSkillLevel() {
		return 0;
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(0);
		return results;
	}
	
	@Override
	public void destroy() {
		super.destroy();

		office = null;
	}
}