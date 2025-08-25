package com.mars_sim.core.building.construction;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.mission.MissionObjective;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionListener;
import com.mars_sim.core.person.ai.mission.MissionLog;
import com.mars_sim.core.person.ai.mission.MissionPlanning;
import com.mars_sim.core.person.ai.mission.MissionStatus;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.structure.ObjectiveType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;

class MockMission implements Mission {

    @Override
    public String getContext() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getContext'");
    }

    @Override
    public void abortMission(String reason) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'abortMission'");
    }

    @Override
    public void abortPhase() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'abortPhase'");
    }

    @Override
    public boolean isDone() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isDone'");
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }

    @Override
    public void setName(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setName'");
    }

    @Override
    public Settlement getAssociatedSettlement() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAssociatedSettlement'");
    }

    @Override
    public String getFullMissionDesignation() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFullMissionDesignation'");
    }

    @Override
    public MissionLog getLog() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLog'");
    }

    @Override
    public Set<MissionStatus> getMissionStatus() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMissionStatus'");
    }

    @Override
    public MissionType getMissionType() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMissionType'");
    }

    @Override
    public Set<ObjectiveType> getObjectiveSatisified() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getObjectiveSatisified'");
    }

    @Override
    public double getMissionQualification(Worker member) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMissionQualification'");
    }

    @Override
    public Stage getStage() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStage'");
    }

    @Override
    public String getPhaseDescription() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPhaseDescription'");
    }

    @Override
    public MarsTime getPhaseStartTime() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPhaseStartTime'");
    }

    @Override
    public MissionPlanning getPlan() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPlan'");
    }

    @Override
    public int getPriority() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPriority'");
    }

    @Override
    public int getMissionCapacity() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMissionCapacity'");
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
    public Set<Worker> getSignup() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSignup'");
    }

    @Override
    public Collection<Worker> getMembers() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMembers'");
    }

    @Override
    public Person getStartingPerson() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStartingPerson'");
    }

    @Override
    public boolean performMission(Worker member) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'performMission'");
    }

    @Override
    public void addMissionListener(MissionListener newListener) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addMissionListener'");
    }

    @Override
    public void removeMissionListener(MissionListener oldListener) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeMissionListener'");
    }

    @Override
    public List<MissionObjective> getObjectives() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getObjectives'");
    }

}