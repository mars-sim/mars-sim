/**
 * Mars Simulation Project
 * MissionProject.java
 * @date 2023-05-06
 * @author Barry Evans
 */
package org.mars_sim.msp.core.mission;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionListener;
import org.mars_sim.msp.core.person.ai.mission.MissionLog;
import org.mars_sim.msp.core.person.ai.mission.MissionPlanning;
import org.mars_sim.msp.core.person.ai.mission.MissionStatus;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.project.Project;
import org.mars_sim.msp.core.project.ProjectStep;
import org.mars_sim.msp.core.project.Stage;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * Is a Mission astraction that allows a Mission tobe defiend in terms of a number of MissionSteps.
 */
public abstract class MissionProject implements Mission {
    /**
     * Controller of mission flow. Monitors the start & stop callbacks
     */
    private final class MissionController extends Project {
        private MissionController(String name) {
            super(name);
        }

        @Override
        protected void stepCompleted(ProjectStep completedStep) {
            log.addEntry("Completed " + completedStep.getDescription());
        }

        @Override
        protected void stepStarted(ProjectStep activeStep) {
            log.addEntry("Started " + activeStep.getDescription());
            stepStarted = log.getLastEntry().getTime();
        }
    }

    private Project control;
    private String missionCallSign;
    private MissionLog log;
    private MissionType type;
    private int priority;
    private int maxMembers;
    private Person leader;

	/** Mission listeners. */
	private transient Set<MissionListener> listeners = null;
    private MarsClock stepStarted;

    public MissionProject(String name, MissionType type, int priority, int maxMembers, Person leader) {
        this.type = type;
        this.priority = priority;
        this.maxMembers = maxMembers;
        this.leader = leader;
        this.log = new MissionLog();
        this.control = new MissionController(name);
    }

    /**
     * Abort the mission for a reason
     * @param reason Reason why the mission aborted
     */
    @Override
    public void abortMission(String reason) {
        log.addEntry("Aborted:" + reason);
        control.abort(reason);
    }

    @Override
    public void abortPhase() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'abortPhase'");
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
    public void setName(String name) {
        control.setName(name);
    }

    /**
     * Return the location which is the home settlement
     * @return Coordinates of home
     */
    @Override
    public Coordinates getCurrentMissionLocation() {
        return getAssociatedSettlement().getCoordinates();
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

    @Override
    public Set<MissionStatus> getMissionStatus() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMissionStatus'");
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
    public Stage getStage() {
        return control.getStage();
    }

    @Override
    public String getPhaseDescription() {
       return control.getStepName();
    }

    @Override
    public MarsClock getPhaseStartTime() {
        return stepStarted;
    }

    @Override
    public MissionPlanning getPlan() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPlan'");
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addMember'");
    }

    @Override
    public void removeMember(Worker member) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeMember'");
    }

    @Override
    public Collection<Worker> getMembers() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMembers'");
    }

    @Override
    public Set<Worker> getSignup() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSignup'");
    }

    @Override
    public Person getStartingPerson() {
        return leader;
    }

    @Override
    public boolean performMission(Worker member) {
        return control.execute(member);
    }

    /**
     * Notification that a step has been started so update the Mission log
     * @param activeStep Step started
     */
    protected void stepStarted(ProjectStep activeStep) {
        log.addEntry(activeStep.getDescription());
    }

    /**
     * Add a new step to the mission
     * @param newStep
     */
    protected void addMissionStep(MissionStep newStep) {
        control.addStep(newStep);
    }

    /**
     * Get the resources needed to complete the mission
     * @return
     */
    public Map<Integer, Number> getResources() {
        List<ProjectStep> steps = control.getRemainingSteps();
        Map<Integer, Number> resources = new HashMap<>();
        for(ProjectStep ps : steps) {
            if (ps instanceof MissionStep ms) {
                ms.getRequiredResources().forEach((key, value)
                            -> resources.merge(key, value, (v1, v2) ->  MissionUtil.numberAdd(v1, v2)));
            }
        }

        return resources;
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
}