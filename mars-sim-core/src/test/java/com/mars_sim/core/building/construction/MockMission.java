package com.mars_sim.core.building.construction;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.EntityListener;
import com.mars_sim.core.mission.MissionObjective;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionLog;
import com.mars_sim.core.person.ai.mission.MissionPlanning;
import com.mars_sim.core.person.ai.mission.MissionStatus;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.structure.ObjectiveType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;

@SuppressWarnings("serial")
class MockMission implements Mission {

    @Override
    public String getContext() {
        throw new UnsupportedOperationException("Unimplemented method 'getContext'");
    }

    @Override
    public void abortMission(MissionStatus reason) {
        throw new UnsupportedOperationException("Unimplemented method 'abortMission'");
    }

    @Override
    public void abortPhase() {
        throw new UnsupportedOperationException("Unimplemented method 'abortPhase'");
    }

    @Override
    public boolean isDone() {
        throw new UnsupportedOperationException("Unimplemented method 'isDone'");
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("Unimplemented method 'setName'");
    }

    @Override
    public Settlement getAssociatedSettlement() {
        throw new UnsupportedOperationException("Unimplemented method 'getAssociatedSettlement'");
    }

    @Override
    public String getFullMissionDesignation() {
        throw new UnsupportedOperationException("Unimplemented method 'getFullMissionDesignation'");
    }

    @Override
    public MissionLog getLog() {
        throw new UnsupportedOperationException("Unimplemented method 'getLog'");
    }

    @Override
    public Set<MissionStatus> getMissionStatus() {
        throw new UnsupportedOperationException("Unimplemented method 'getMissionStatus'");
    }

    @Override
    public MissionType getMissionType() {
        throw new UnsupportedOperationException("Unimplemented method 'getMissionType'");
    }

    @Override
    public Set<ObjectiveType> getObjectiveSatisified() {
        throw new UnsupportedOperationException("Unimplemented method 'getObjectiveSatisified'");
    }

    @Override
    public double getMissionQualification(Worker member) {
        throw new UnsupportedOperationException("Unimplemented method 'getMissionQualification'");
    }

    @Override
    public Stage getStage() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getStage'");
    }

    @Override
    public String getPhaseDescription() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPhaseDescription'");
    }

    @Override
    public MarsTime getPhaseStartTime() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPhaseStartTime'");
    }

    @Override
    public MissionPlanning getPlan() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPlan'");
    }

    @Override
    public int getPriority() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPriority'");
    }

    @Override
    public int getMissionCapacity() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getMissionCapacity'");
    }

    @Override
    public void addMember(Worker member) {
        
        throw new UnsupportedOperationException("Unimplemented method 'addMember'");
    }

    @Override
    public void removeMember(Worker member) {
        
        throw new UnsupportedOperationException("Unimplemented method 'removeMember'");
    }

    @Override
    public Set<Worker> getSignup() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getSignup'");
    }

    @Override
    public Collection<Worker> getMembers() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getMembers'");
    }

    @Override
    public Person getStartingPerson() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getStartingPerson'");
    }

    @Override
    public boolean performMission(Worker member) {
        
        throw new UnsupportedOperationException("Unimplemented method 'performMission'");
    }

    @Override
    public void addEntityListener(EntityListener newListener) {
        
        throw new UnsupportedOperationException("Unimplemented method 'addEntityListener'");
    }

    @Override
    public void removeEntityListener(EntityListener oldListener) {
        
        throw new UnsupportedOperationException("Unimplemented method 'removeEntityListener'");
    }

    @Override
    public List<MissionObjective> getObjectives() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getObjectives'");
    }

}