/**
 * Mars Simulation Project
 * MissionProject.java
 * @date 2023-05-06
 * @author Barry Evans
 */
package com.mars_sim.core.mission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.mission.steps.MissionCloseStep;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionListener;
import com.mars_sim.core.person.ai.mission.MissionLog;
import com.mars_sim.core.person.ai.mission.MissionPlanning;
import com.mars_sim.core.person.ai.mission.MissionStatus;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.project.Project;
import com.mars_sim.core.project.ProjectStep;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.resource.SuppliesManifest;
import com.mars_sim.core.structure.ObjectiveType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;

/**
 * Is a Mission astraction that allows a Mission tobe defiend in terms of a number of MissionSteps.
 */
public abstract class MissionProject implements Mission {

	private static final long serialVersionUID = 1L;

	/**
     * Controller of mission flow. Monitors the start & stop callbacks
     */
    private final class MissionController extends Project {
        private static final long serialVersionUID = 1L;

		private MissionController(String name) {
            super(name);
        }

        @Override
        protected void completed(boolean successful) {
            if (successful) {
                // Assume that the status has already been updated as part of the abort
                log.addEntry("Completed");
                status.add(ACCOMPLISHED);
            }
            clearDown();
        }

        @Override
        protected void stepStarted(ProjectStep activeStep) {
            log.addEntry(activeStep.getDescription());
            stepStarted = log.getLastEntry().getTime();
        }

        @Override
        protected void stepCompleted(ProjectStep closedStep) {
            MissionProject.this.stepCompleted((MissionStep) closedStep);
        }
    }

    // Mission score for Worker
    private static record Candidate(Worker worker, double score) {};

    private static final SimLogger logger = SimLogger.getLogger(MissionProject.class.getName());

    // Key for the user aborted mission
    private static final String MISSION_ABORT = "Mission.status.abortedReason";

	public static final MissionStatus NOT_ENOUGH_MEMBERS = new MissionStatus("Mission.status.noMembers");
    public static final MissionStatus LOW_SETTLEMENT_POPULATION = new MissionStatus("Mission.status.lowPopulation");
    private static final MissionStatus ACCOMPLISHED = new MissionStatus("Mission.status.accomplished");

    // Minimum settlement population after a mission
    public static final int MIN_POP = 2;


    private Project control;
    private String missionCallSign;
    private MissionLog log;
    private MissionType type;
    private int priority;
    private int minMembers;
    private int maxMembers;
    private Person leader;

	/** Mission listeners. */
	private transient Set<MissionListener> listeners = null;
    private MarsTime stepStarted;

    private Set<Worker> members = new UnitSet<>();
    private Set<Worker> signedUp = new UnitSet<>();
    private Set<MissionStatus> status = new HashSet<>();

    protected MissionProject(String name, MissionType type, int priority, int minMembers, int maxMembers, Person leader) {
        this.type = type;
        this.priority = priority;
        this.maxMembers = maxMembers;
        this.minMembers = minMembers;

        this.leader = leader;
        leader.setMission(this);
        this.log = new MissionLog();
        this.control = new MissionController(name);
    }

    /**
     * Aborts the mission for a reason.
     * 
     * @param reason Reason why the mission aborted
     */
    @Override
    public void abortMission(String reason) {
        abortMission(new MissionStatus(MISSION_ABORT, reason));
    }

    public void abortMission(MissionStatus reason) {
        if (!control.isFinished()) {
            logger.warning(leader, "Mission aborted : " + reason.getName());

            log.addEntry("Aborted:" + reason.getName());
            control.abort(reason.getName());
        }
    }


    /**
     * Complete the current step immediately
     */
    @Override
    public void abortPhase() {
        control.abortStep();
    }

    @Override
    public boolean isDone() {
        return control.isFinished();
    }

    @Override
    public String getName() {
        return control.getName();
    }

    @Override
    public String getContext() {
        return leader.getAssociatedSettlement().getName();
    }
    
    @Override
    public void setName(String name) {
        control.setName(name);
    }

    /**
     * The owning settlement is the settlement of the leader.
     * @return Default based on leader
     */
    @Override
    public Settlement getAssociatedSettlement() {
        return leader.getAssociatedSettlement();
    }

    @Override
    public String getFullMissionDesignation() {
        return missionCallSign;
    }

    @Override
    public MissionLog getLog() {
        return log;
    }

    protected void addStatus(MissionStatus s) {
        status.add(s);
    }
    
    @Override
    public Set<MissionStatus> getMissionStatus() {
        return status;
    }

    @Override
    public MissionType getMissionType() {
        return type;
    }

    @Override
    public double getMissionQualification(Worker member) {
        return 1.0;
    }

    @Override
    public Set<ObjectiveType> getObjectiveSatisified() {
        return Collections.emptySet();
    }

    @Override
    public Stage getStage() {
        return control.getStage();
    }

    /**
     * Define the step of this Mission. This will trigger finding suitable people.
     * @param plan
     */
    protected void setSteps(List<MissionStep> plan) {
        for(MissionStep ps : plan) {
            control.addStep(ps);
        }

        // Add the close down step
        control.addStep(new MissionCloseStep(this));
        
        if (members.size() < minMembers) {
            findMembers();
        }
    }

    /**
     * Callback method to be notified when a stepis completed
     * @param ms The Mission step just completed
     */
    protected void stepCompleted(MissionStep ms) {
        // Do nothing
    }

    /**
	 * Checks to see if a member is capable of joining a mission.
	 *
	 * @param member the member to check.
	 * @return true if member could join mission.
	 */
	private static boolean isCapableOfMission(Worker member) {
		if (member instanceof Person p) {
			// Make sure person isn't already on a mission.
			boolean onMission = (p.getMind().getMission() != null);

			// Make sure person doesn't have any serious health problems.
			boolean healthProblem = p.getPhysicalCondition().hasSeriousMedicalProblems();

			return (!onMission && !healthProblem);
		}
        // TODO need Robot selector
		return false;
	}

    /** 
     * Find new members based on the skils needed for the Mission steps.
     */
    private void findMembers() {
    	// Get all people qualified for the mission.
		Collection<Person> possibles = leader.getAssociatedSettlement().getIndoorPeople();

		List<Candidate> qualifiedPeople = new ArrayList<>();
		for(Person person : possibles) {
			if (isCapableOfMission(person)) {
				// Determine the person's mission qualification.
				double qualification = getMissionQualification(person) * 100D;
                if (qualification > 0) {

                    // Determine how much the recruiter likes the person.
                    double likability = RelationshipUtil.getOpinionOfPerson(leader, person);

                    // Check if person is the best recruit.
                    double personValue = (qualification + likability) / 2D;
                    qualifiedPeople.add(new Candidate(person, personValue));
                }
			}
		}

        // Check numbers
        int needed = minMembers - members.size();
        if (needed > qualifiedPeople.size()) {
            abortMission(NOT_ENOUGH_MEMBERS);
        }
        else if ((possibles.size() - needed) < MIN_POP) {
            abortMission(LOW_SETTLEMENT_POPULATION);
        }
		
		// Recruit the most qualified and most liked people first.
		qualifiedPeople.sort(Comparator.comparing(Candidate::score, Comparator.reverseOrder()));
        for(int i = 0; i < needed; i++) {
            Candidate choosen = qualifiedPeople.get(i);
            choosen.worker.setMission(this);
        }
	}
    
    @Override
    public String getPhaseDescription() {
        ProjectStep current = control.getStep();
        return (current != null ? current.getDescription() : "");
    }

    @Override
    public MarsTime getPhaseStartTime() {
        return stepStarted;
    }

    @Override
    public MissionPlanning getPlan() {
        return null;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public int getMissionCapacity() {
       return maxMembers;
    }

    @Override
    public void addMember(Worker member) {
        members.add(member);
        signedUp.add(member);
    }

    @Override
    public void removeMember(Worker member) {
        if (members.remove(member)) {
            // Add experience
            if (member instanceof Person p) {
                p.getShiftSlot().setOnCall(false);
            }
        }
    }

    /**
     * All the workers that are active in the Mission. Doe snot include thos that have left.
     * @return Workers active
     */
    @Override
    public Collection<Worker> getMembers() {
        return members;
    }

    /**
     * Get a list of everyone thta has signed up to the Mission
     * @return Everyone originally signed up
     */
    @Override
    public Set<Worker> getSignup() {
        return signedUp;
    }

    /**
     * Who is leading the Mission
     * @return Leading person
     */
    @Override
    public Person getStartingPerson() {
        return leader;
    }

    @Override
    public boolean performMission(Worker member) {
        return control.execute(member);
    }

    protected ProjectStep getCurrentStep() {
        return control.getStep();
    }

    /**
     * Notification that a step has been started so update the Mission log
     * @param activeStep Step started
     */
    protected void stepStarted(ProjectStep activeStep) {
        log.addEntry(activeStep.getDescription());
    }

    /**
     * Get the resources needed to complete the mission
     * @return
     */
    public SuppliesManifest getResources(boolean includeOptionals) {
        SuppliesManifest resources = new SuppliesManifest();
        List<ProjectStep> steps = control.getRemainingSteps();
        for(ProjectStep ps : steps) {
            if (ps instanceof MissionStep ms) {
                ms.getRequiredResources(resources, includeOptionals);
            }
        }

        return resources;
    }

    @Override
    public List<MissionObjective> getObjectives() {
        return Collections.emptyList();
    }

    /**
     * Add a listener to the Mission
     * @param newListener Listener
     */
    @Override
    public void addMissionListener(MissionListener newListener) {
		if (listeners == null) {
			listeners = new HashSet<>();
		}
		synchronized (listeners) {
			listeners.add(newListener);
		}
    }

    /**
     * Remove a previously registered listener
     * @param oldListener Listener to remove
     */
    @Override
    public void removeMissionListener(MissionListener oldListener) {
		if (listeners != null) {
			synchronized (listeners) {
				listeners.remove(oldListener);
			}
		}
    }

    /**
     * Add an entry to the mission log
     * @param string
     */
    public void addMissionLog(String string) {
        log.addEntry(string);
    }

    /**
     * Clear down the mission as it has completed. This should be overriden by subclasses/
     */
    protected void clearDown() {
        // Force release of any remaining members
        if (members.isEmpty()) {
            List<Worker> oldmembers = new ArrayList<>(members);
            for(Worker w : oldmembers) {
                logger.warning(w, "Still attached to Mission " + getName() + " at clear down");
                w.setMission(null);
            }
        }
    }
}