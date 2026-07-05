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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityIdentifier;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.EntityListenerManager;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.events.HistoricalEventType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.mission.steps.MissionCloseStep;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionLog;
import com.mars_sim.core.person.ai.mission.MissionPlanning;
import com.mars_sim.core.person.ai.mission.MissionStatus;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.project.Project;
import com.mars_sim.core.project.ProjectStep;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.resource.SuppliesManifest;
import com.mars_sim.core.structure.ObjectiveType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;

/**
 * Is a Mission abstraction that allows a Mission to be defined in terms of a number of MissionSteps.
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

            // Update stats
            fireMissionUpdate(Mission.END_MISSION_EVENT, this);
            getAssociatedSettlement().getMissionControl().finishMission(successful);
            clearDown();
        }

        @Override
        protected void stepStarted(ProjectStep activeStep) {
            log.addEntry(activeStep.getDescription());
            stepStarted = log.getLastEntry().getTime();
            fireMissionUpdate(Mission.PHASE_EVENT, null);

			registerHistoricalEvent(getStartingPerson(), HistoricalEventType.MISSION_PHASE, activeStep.getDescription());
        }

        @Override
        protected void stepCompleted(ProjectStep closedStep) {
            MissionProject.this.stepCompleted((MissionStep) closedStep);
        }
    }

	/** Default logger. */
    private static final SimLogger logger = SimLogger.getLogger(MissionProject.class.getName());
   
	public static final MissionStatus NOT_ENOUGH_MEMBERS = new MissionStatus("Mission.status.noMembers");
    public static final MissionStatus LOW_SETTLEMENT_POPULATION = new MissionStatus("Mission.status.lowPopulation");
    private static final MissionStatus ACCOMPLISHED = new MissionStatus("Mission.status.accomplished");

	/** Entity listeners. */
	private transient EntityListenerManager listeners;
	
    private int priority;
    
    private String missionCallSign;
    
    private MissionType type;

    private Project control;
    private MissionLog log;
    private Person leader;
    private MarsTime stepStarted;

    private Set<Worker> members = new UnitSet<>();
    private Set<Worker> signedUp = new UnitSet<>();
    private Set<MissionStatus> status = new HashSet<>();

    protected MissionProject(String name, MissionType type, int priority, Person leader, Collection<? extends Worker> recruits) {
        this.type = type;
        this.priority = priority;

        var names = leader.getAssociatedSettlement().getMissionControl().generateNames(type);
        this.missionCallSign = names.callSign();
        if (name == null) {
            // Use default generated if no user name defined
            name = names.name();
        }
        
        this.leader = leader;
        leader.setMission(this);
        this.log = new MissionLog();
        this.control = new MissionController(name);

        // Inviite them in
        recruits.forEach(r -> r.setMission(this));
    }

    @Override
    public EntityIdentifier getEntityIdentifier() {
        return new EntityIdentifier("MISSION", getFullMissionDesignation(), 
                Integer.toString(getAssociatedSettlement().getIdentifier()));
    }
    
    /**
     * Aborts the mission for a reason.
     * 
     * @param reason MissionStatus why the mission aborted
     */
    @Override
    public void abortMission(MissionStatus reason) {
        if (!control.isFinished()) {
            logger.warning(leader, "Mission aborted : " + reason.getName());

            log.addEntry("Aborted:" + reason.getName());
            control.abort(reason.getName());
        }
    }


    /**
     * Aborts the current phase/step.
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
     * Gets the owning settlement of the leader.
     * 
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
    public Set<ObjectiveType> getObjectiveSatisified() {
        return Collections.emptySet();
    }

    @Override
    public Stage getStage() {
        return control.getStage();
    }

    /**
     * Defines the step of this Mission. This will trigger finding suitable people.
     * 
     * @param plan
     */
    protected void setSteps(List<MissionStep> plan) {
        for(MissionStep ps : plan) {
            control.addStep(ps);
        }

        // Add the close down step
        control.addStep(new MissionCloseStep(this));
    }

    /**
     * Callbacks method to be notified when a step is completed.
     * 
     * @param ms The Mission step just completed
     */
    protected void stepCompleted(MissionStep ms) {
        // Do nothing
    }

    /**
	 * Registers this historical mission event about a member.
	 * 
	 * @param affected the entity affected by the event
	 * @param type the type of the historical event
	 * @param message 	
	 */
	private void registerHistoricalEvent(Entity affected, HistoricalEventType type, String message) {
		
		// Creating mission joining event.
		HistoricalEvent newEvent = new HistoricalEvent(type, this, getAssociatedSettlement(),
														message, null, affected, null);
		Simulation.instance().getEventManager().registerNewEvent(newEvent);
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
    public void addMember(Worker member) {
        members.add(member);
        signedUp.add(member);

        fireMissionUpdate(Mission.ADD_MEMBER_EVENT, member);
    }

    @Override
    public void removeMember(Worker member) {
        if (members.remove(member)) {
            // Add experience
            if (member instanceof Person p) {
                p.getShiftSlot().setOnCall(false);
            }

            fireMissionUpdate(Mission.REMOVE_MEMBER_EVENT, member);
        }
    }

    /**
     * Gets all workers that are active in the Mission. Does not include those that have left.
     * 
     * @return Workers active
     */
    @Override
    public Collection<Worker> getMembers() {
        return members;
    }

    /**
     * Gets a list of everyone who has signed up to the Mission.
     * 
     * @return Everyone originally signed up
     */
    @Override
    public Set<Worker> getSignup() {
        return signedUp;
    }

    /**
     * Who is leading the Mission ?
     * 
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
     * Notifies that a step has been started so update the Mission log.
     * 
     * @param activeStep Step started
     */
    protected void stepStarted(ProjectStep activeStep) {
        log.addEntry(activeStep.getDescription());
    }

    /**
     * Gets the resources needed to complete the mission.
     * 
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
     * Adds an entity listener to the Mission.
     * 
     * @param newListener Listener
     */
    @Override
    public void addEntityListener(EntityListener newListener) {
		if (listeners == null) {
			listeners = new EntityListenerManager();
		}
		listeners.addEntityListener(newListener);
    }

    /**
     * Removes a previously registered entity listener.
     * 
     * @param oldListener Listener to remove
     */
    @Override
    public void removeEntityListener(EntityListener oldListener) {
		if (listeners != null) {
			listeners.removeEntityListener(oldListener);
		}
    }

	/**
	 * Gets an unmodifiable set of the active listeners on this entity.
	 * 
	 * @return unmodifiable set of entity listeners.
	 */
	@Override
	public Set<EntityListener> getEntityListeners() {
		if (listeners == null ) {
			return Collections.emptySet();
		}
		return listeners.getEntityListeners();
	}

	/**
	 * Fires an entity update event.
	 *
	 * @param eventType the update type.
	 * @param target    the event target or null if none.
	 */
    @Override
	public void fireMissionUpdate(String eventType, Object target) {
		if (listeners != null) {
			listeners.fireEvent(new EntityEvent(this, eventType, target));
		}
	}

    /**
     * Adds an entry to the mission log.
     * 
     * @param string
     */
    public void addMissionLog(String string) {
        log.addEntry(string);
    }

	/**
	 * Adds an entry to the mission log.
	 * 
	 * @param entry
	 * @param enterBy the name of the person who logs this
	 */
	public void addMissionLog(String entry, String enterBy) {
		log.addEntry(entry, enterBy);
	}    
    
	/**
	 * Gets the mission log.
	 */
	@Override
	public MissionLog getLog() {
		return log;
	}

    /**
     * Clears down the mission as it has completed. This should be overridden by subclasses.
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