/**
 * Mars Simulation Project
 * ExamineBody.java
 * @version 3.1.0 2018-11-17
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
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.person.health.DeathInfo;
import org.mars_sim.msp.core.person.health.HealthProblem;
import org.mars_sim.msp.core.person.health.MedicalAid;
import org.mars_sim.msp.core.person.health.MedicalEvent;
import org.mars_sim.msp.core.person.health.MedicalManager;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.MedicalCare;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.SickBay;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A task for performing a physical exam over a patient or a postmortem exam on
 * a deceased person at a medical station.
 */
public class ExamineBody extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(ExamineBody.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.examineBody"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase EXAMINING = new TaskPhase(Msg.getString("Task.phase.examineBody.examining")); //$NON-NLS-1$

	private static final TaskPhase RECORDING = new TaskPhase(Msg.getString("Task.phase.examineBody.recording")); //$NON-NLS-1$

	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = 1D;

	// Data members.
	private double duration;

	private DeathInfo deathInfo;
	private MedicalAid medicalAid;
	private Person patient;

	private static MedicalManager medicalManager = Simulation.instance().getMedicalManager();
	
	/**
	 * Constructor.
	 * 
	 * @param person the person to perform the task
	 */
	public ExamineBody(Person person) {
		super(NAME, person, true, true, STRESS_MODIFIER, false, 10D);

		if (person.isInSettlement()) {
			// Probability affected by the person's stress and fatigue.
	        PhysicalCondition condition = person.getPhysicalCondition();
	        double fatigue = condition.getFatigue();
	        double stress = condition.getStress();
	        double hunger = condition.getHunger();
	        
	        if (fatigue > 1000 || stress > 50 || hunger > 500)
	        	endTask();
	        
			// Choose available medical aid for treatment.
			medicalAid = determineMedicalAid();
	
			if (medicalAid != null) {
	
				// Determine patient and health problem to treat.
				List<DeathInfo> list = medicalManager.getPostmortemExams(person.getSettlement());
				int num = list.size();
				boolean done = false;
				
				if (num > 0) {
					
					while (!done) {
						for (int i=0; i < num; i++) {
							if (list.get(i).getBodyRetrieved()) {
								// Work on the body already retrieved
								deathInfo = list.get(i);
								done = true;
							}
						}
						
						// If no body has been examined yet, pick one
						if (deathInfo == null) {
							int rand = RandomUtil.getRandomInt(num-1);
							deathInfo = list.get(rand);
							retrieveBody();
						}			
					}
								
					patient = deathInfo.getPerson();
	
					// Get the person's medical skill.
					double skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);
					if (skill == 0)
						skill = .5;
					// Get the person's emotion stability
					int stab = person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.EMOTIONAL_STABILITY);
					// Get the person's stress resilience						
					int resilient = person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.STRESS_RESILIENCE);
					
					// TODO: how to determine how long it takes ? Cause of death ?
					duration = 150 + STRESS_MODIFIER * (200 - stab - resilient) / 5D / skill + 2 * RandomUtil.getRandomInt(num+5);
					
					deathInfo.setEstTimeExam(duration);
	
				} else {
					logger.severe(person + " could not find any bodies to do autopsy at " + medicalAid);
					endTask();
				}
	
				// Walk to medical aid.
				if (medicalAid instanceof MedicalCare) {
					// Walk to medical care building.
					MedicalCare medicalCare = (MedicalCare) medicalAid;
					// Walk to medical care building.
					walkToActivitySpotInBuilding(medicalCare.getBuilding(), false);
					
				} else if (medicalAid instanceof SickBay) {
					// Walk to medical activity spot in rover.
					Vehicle vehicle = ((SickBay) medicalAid).getVehicle();
					if (vehicle instanceof Rover) {
						// Walk to rover sick bay activity spot.
						walkToSickBayActivitySpotInRover((Rover) vehicle, false);
					}
				}
			} else {
				logger.severe("Medical aid could not be determined.");
				endTask();
			}
	
			// Initialize phase.
			addPhase(EXAMINING);
			addPhase(RECORDING);
			setPhase(EXAMINING);
		}
		else
			endTask();
		
	}

	/**
	 * Determines a local medical aid to use.
	 * 
	 * @return medical aid or null if none found.
	 */
	private MedicalAid determineMedicalAid() {

		MedicalAid result = null;

		if (person.isInSettlement()) {
			result = determineMedicalAidAtSettlement();
		} else if (person.isInVehicle()) {
			result = determineMedicalAidInVehicle();
		}

		return result;
	}

	/**
	 * Determines a medical aid at a settlement to use.
	 * 
	 * @return medical aid or null if none found.
	 */
	private MedicalAid determineMedicalAidAtSettlement() {

		MedicalAid result = null;

		List<MedicalAid> goodMedicalAids = new ArrayList<MedicalAid>();

		// Check all medical care buildings.
		Iterator<Building> i = person.getSettlement().getBuildingManager().getBuildings(FunctionType.MEDICAL_CARE)
				.iterator();
		while (i.hasNext()) {
			// Check if there are any treatable medical problems at building.
			MedicalCare medicalCare = i.next().getMedical();
			if (medicalCare.hasEmptyBeds()) {
				goodMedicalAids.add(medicalCare);
			}
		}

		// Randomly select an valid medical care building.
		if (goodMedicalAids.size() > 0) {
			int index = RandomUtil.getRandomInt(goodMedicalAids.size() - 1);
			result = goodMedicalAids.get(index);
		}

		return result;
	}

	/**
	 * Determines a medical aid in a vehicle to use.
	 * 
	 * @return medical aid or null if none found.
	 */
	private MedicalAid determineMedicalAidInVehicle() {

		MedicalAid result = null;

		if (person.getVehicle() instanceof Rover) {
			Rover rover = (Rover) person.getVehicle();
			if (rover.hasSickBay()) {
				SickBay sickBay = rover.getSickBay();
				if (sickBay.hasEmptyBeds()) {
					result = sickBay;
				}
			}
		}

		return result;
	}


	@Override
	public FunctionType getLivingFunction() {
		return FunctionType.MEDICAL_CARE;
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (EXAMINING.equals(getPhase())) {
			return examiningPhase(time);
		} else if (RECORDING.equals(getPhase())) {
			return recordingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the examining phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the amount of time (millisol) left over after performing the phase.
	 */
	private double examiningPhase(double time) {

		double timeLeft = 0D;

		// If medical aid has malfunction, end task.
		if (getMalfunctionable().getMalfunctionManager().hasMalfunction()) {
			endTask();
		}
		
		double timeExam = deathInfo.getTimeExam();
		
		if (timeExam == 0)
			deathInfo.getBodyRetrieved();
		
		if (timeExam < 10) {
			LogConsolidated.log(logger, Level.WARNING, 1000, sourceName, 
					"[" + person.getLocationTag().getLocale() + "] " + person.getName() + " began performing a postmortem exam on " 
						+ patient.getName() + ".", null);
		}
		
		if (timeExam > (deathInfo.getEstTimeExam() + duration)/2D) {
			deathInfo.setExamDone(true);
			
			// Check for accident in medical aid.
			checkForAccident(timeExam);
	
			// Add experience.
			addExperience(timeExam);
			
			// Ready to go to the next task phase
			setPhase(RECORDING);
			
			return timeLeft;
		}

		else {
//			System.out.println("timeExam : " + timeExam);
			// Get the person's medical skill.
			double skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);
			if (skill == 0)
				skill = .5;
			// Get the person's emotion stability
			int stab = person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.EMOTIONAL_STABILITY);
					
			deathInfo.addTimeExam(time + time * skill / 4D);
			
			double stress = STRESS_MODIFIER * (1-stab) / 5D / skill + RandomUtil.getRandomInt(3);
		
			setStressModifier(stress);

		}
		
		return timeLeft;
	}

	/**
	 * Performs the recording phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the amount of time (millisol) left over after performing the phase.
	 */
	private double recordingPhase(double time) {

		double timeLeft = 0D;

		int num = medicalManager.getPostmortemExams(person.getSettlement()).size();
		
		if (num > 0) {
			// If medical aid has malfunction, end task.
			if (getMalfunctionable().getMalfunctionManager().hasMalfunction()) {
				endTask();
			}
	
			// Record the cause
			recordCause(deathInfo.getProblem());
			
			// Bury the body
			patient.buryBody();
			
			medicalManager.addDeathRegistry(person.getSettlement(), deathInfo);
					
			// Check for accident in medical aid.
			checkForAccident(time);
	
			// Add experience.
			addExperience(time);
	
			endTask();
		
		}
		else
			endTask();
		
		return timeLeft;
	}

	/**
	 * Retrieves the body.
	 * 
	 * @param problem
	 */
	public void retrieveBody() {
		deathInfo.setBodyRetrieved(true);
	}

	/**
	 * Records the cause of death and creates the medical event.
	 * 
	 * @param problem
	 */
	public void recordCause(HealthProblem problem) {
		String cause = deathInfo.getCause();
		if (!cause.toLowerCase().equals("suicide")) {
			cause = problem.toString().toLowerCase();
			deathInfo.setCause(cause);
		}
//		logger.log(Level.WARNING,
//				"[" + person.getLocationTag().getQuickLocation() + "] A post-mortem examination had been completed on "
//						+ person + ". Cause of death : " + cause);
		LogConsolidated.log(logger, Level.WARNING, 1000, sourceName, 
				"[" + person.getLocationTag().getLocale() + "] " + person + " completed the postmortem exam on " 
					+ patient.getName() + ". Cause of death : " + cause + ".", null);

		// Create medical event for death.
		MedicalEvent event = new MedicalEvent(person, problem, EventType.MEDICAL_DEATH);
		Simulation.instance().getEventManager().registerNewEvent(event);
	}
	
	@Override
	protected void addExperience(double time) {
		// Add experience to "Medical" skill
		// (1 base experience point per 25 millisols of work)
		// Experience points adjusted by person's "Experience Aptitude" attribute.
		double newPoints = time / 25D;
		int experienceAptitude = person.getNaturalAttributeManager()
				.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
		newPoints *= getTeachingExperienceModifier();
		person.getSkillManager().addExperience(SkillType.MEDICINE, newPoints, time);
	}

	/**
	 * Gets the malfunctionable associated with the medical aid.
	 * 
	 * @return the associated Malfunctionable
	 */
	private Malfunctionable getMalfunctionable() {
		Malfunctionable result = null;

		if (medicalAid instanceof SickBay) {
			result = ((SickBay) medicalAid).getVehicle();
		} else if (medicalAid instanceof MedicalCare) {
			result = ((MedicalCare) medicalAid).getBuilding();
		} else {
			result = (Malfunctionable) medicalAid;
		}

		return result;
	}

	/**
	 * Check for accident in the medical aid.
	 * 
	 * @param time the amount of time working (in millisols)
	 */
	private void checkForAccident(double time) {

		Malfunctionable entity = getMalfunctionable();

		double chance = .005D;

		// Medical skill modification.
		int skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.MEDICINE);
		if (skill <= 3) {
			chance *= (4 - skill);
		} else {
			chance /= (skill - 2);
		}

		// Modify based on the entity's wear condition.
		chance *= entity.getMalfunctionManager().getWearConditionAccidentModifier();

		if (RandomUtil.lessThanRandPercent(chance * time)) {

			if (person != null) {
//				logger.info("[" + person.getLocationTag().getShortLocationName() +  "] " + person.getName() + " has accident while providing medical treatment.");
				entity.getMalfunctionManager().createASeriesOfMalfunctions(person);
			} else if (robot != null) {
//				logger.info("[" + robot.getLocationTag().getShortLocationName() +  "] " + robot.getName() + " has accident while providing medical treatment.");
				entity.getMalfunctionManager().createASeriesOfMalfunctions(robot);
			}
		}
	}

	@Override
	public void endTask() {
		super.endTask();
	}

	@Override
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getSkillManager();
		return manager.getEffectiveSkillLevel(SkillType.MEDICINE);
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(1);
		results.add(SkillType.MEDICINE);
		return results;
	}

	@Override
	public void destroy() {
		super.destroy();
		medicalAid = null;
		patient = null;
	}
}