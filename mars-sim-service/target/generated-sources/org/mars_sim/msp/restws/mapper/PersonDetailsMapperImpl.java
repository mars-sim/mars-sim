package org.mars_sim.msp.restws.mapper;

import javax.annotation.Generated;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.PersonalityType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.task.TaskManager;
import org.mars_sim.msp.core.person.ai.task.TaskPhase;
import org.mars_sim.msp.restws.model.PersonDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2016-07-14T17:06:44-0700",
    comments = "version: 1.0.0.Final, compiler: javac, environment: Java 1.8.0_92 (Oracle Corporation)"
)
@Component
public class PersonDetailsMapperImpl implements PersonDetailsMapper {

    @Autowired
    private UnitReferenceMapper unitReferenceMapper;
    @Autowired
    private MissionReferenceMapper missionReferenceMapper;

    @Override
    public PersonDetails personToPersonDetail(Person person) {
        if ( person == null ) {
            return null;
        }

        PersonDetails personDetails = new PersonDetails();

        personDetails.setStress( personPhysicalConditionStress( person ) );
        personDetails.setHealthSituation( personPhysicalConditionHealthSituation( person ) );
        personDetails.setMass( person.getBaseMass() );
        personDetails.setVehicle( unitReferenceMapper.unitToUnitReference( person.getVehicle() ) );
        personDetails.setSettlement( unitReferenceMapper.unitToUnitReference( person.getSettlement() ) );
        personDetails.setHunger( personPhysicalConditionHunger( person ) );
        personDetails.setFatigue( personPhysicalConditionFatigue( person ) );
        personDetails.setMission( missionReferenceMapper.missionToEntityReference( personMindMission( person ) ) );
        personDetails.setPerformance( personPhysicalConditionPerformanceFactor( person ) );
        personDetails.setPersonalityType( personMindPersonalityTypeTypeString( person ) );
        personDetails.setTask( personMindTaskManagerTaskName( person ) );
        personDetails.setTaskPhase( personMindTaskManagerPhaseName( person ) );
        personDetails.setId( person.getIdentifier() );
        personDetails.setName( person.getName() );
        if ( person.getGender() != null ) {
            personDetails.setGender( person.getGender().name() );
        }
        personDetails.setAge( person.getAge() );
        personDetails.setHeight( (int) person.getHeight() );
        personDetails.setBirthDate( person.getBirthDate() );

        return personDetails;
    }

    private double personPhysicalConditionStress(Person person) {

        if ( person == null ) {
            return 0.0;
        }
        PhysicalCondition physicalCondition = person.getPhysicalCondition();
        if ( physicalCondition == null ) {
            return 0.0;
        }
        double stress = physicalCondition.getStress();
        return stress;
    }

    private String personPhysicalConditionHealthSituation(Person person) {

        if ( person == null ) {
            return null;
        }
        PhysicalCondition physicalCondition = person.getPhysicalCondition();
        if ( physicalCondition == null ) {
            return null;
        }
        String healthSituation = physicalCondition.getHealthSituation();
        if ( healthSituation == null ) {
            return null;
        }
        return healthSituation;
    }

    private double personPhysicalConditionHunger(Person person) {

        if ( person == null ) {
            return 0.0;
        }
        PhysicalCondition physicalCondition = person.getPhysicalCondition();
        if ( physicalCondition == null ) {
            return 0.0;
        }
        double hunger = physicalCondition.getHunger();
        return hunger;
    }

    private double personPhysicalConditionFatigue(Person person) {

        if ( person == null ) {
            return 0.0;
        }
        PhysicalCondition physicalCondition = person.getPhysicalCondition();
        if ( physicalCondition == null ) {
            return 0.0;
        }
        double fatigue = physicalCondition.getFatigue();
        return fatigue;
    }

    private Mission personMindMission(Person person) {

        if ( person == null ) {
            return null;
        }
        Mind mind = person.getMind();
        if ( mind == null ) {
            return null;
        }
        Mission mission = mind.getMission();
        if ( mission == null ) {
            return null;
        }
        return mission;
    }

    private double personPhysicalConditionPerformanceFactor(Person person) {

        if ( person == null ) {
            return 0.0;
        }
        PhysicalCondition physicalCondition = person.getPhysicalCondition();
        if ( physicalCondition == null ) {
            return 0.0;
        }
        double performanceFactor = physicalCondition.getPerformanceFactor();
        return performanceFactor;
    }

    private String personMindPersonalityTypeTypeString(Person person) {

        if ( person == null ) {
            return null;
        }
        Mind mind = person.getMind();
        if ( mind == null ) {
            return null;
        }
        PersonalityType personalityType = mind.getPersonalityType();
        if ( personalityType == null ) {
            return null;
        }
        String typeString = personalityType.getTypeString();
        if ( typeString == null ) {
            return null;
        }
        return typeString;
    }

    private String personMindTaskManagerTaskName(Person person) {

        if ( person == null ) {
            return null;
        }
        Mind mind = person.getMind();
        if ( mind == null ) {
            return null;
        }
        TaskManager taskManager = mind.getTaskManager();
        if ( taskManager == null ) {
            return null;
        }
        String taskName = taskManager.getTaskName();
        if ( taskName == null ) {
            return null;
        }
        return taskName;
    }

    private String personMindTaskManagerPhaseName(Person person) {

        if ( person == null ) {
            return null;
        }
        Mind mind = person.getMind();
        if ( mind == null ) {
            return null;
        }
        TaskManager taskManager = mind.getTaskManager();
        if ( taskManager == null ) {
            return null;
        }
        TaskPhase phase = taskManager.getPhase();
        if ( phase == null ) {
            return null;
        }
        String name = phase.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
