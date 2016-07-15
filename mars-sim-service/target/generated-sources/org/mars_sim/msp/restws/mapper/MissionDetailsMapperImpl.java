package org.mars_sim.msp.restws.mapper;

import javax.annotation.Generated;
import org.mars_sim.msp.core.person.ai.mission.AreologyStudyFieldMission;
import org.mars_sim.msp.core.person.ai.mission.BiologyStudyFieldMission;
import org.mars_sim.msp.core.person.ai.mission.EmergencySupplyMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionPhase;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.restws.model.MissionDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2016-07-14T17:06:43-0700",
    comments = "version: 1.0.0.Final, compiler: javac, environment: Java 1.8.0_92 (Oracle Corporation)"
)
@Component
public class MissionDetailsMapperImpl implements MissionDetailsMapper {

    @Autowired
    private UnitReferenceMapper unitReferenceMapper;
    @Autowired
    private MarsClockMapper marsClockMapper;
    @Autowired
    private ScientificStudyMapper scientificStudyMapper;

    @Override
    public MissionDetails missionToMissionDetails(Mission findMission) {
        if ( findMission == null ) {
            return null;
        }

        MissionDetails missionDetails = new MissionDetails();

        missionDetails.setPhase( findMissionPhaseName( findMission ) );
        missionDetails.setNumPersons( findMission.getPeopleNumber() );
        missionDetails.setName( findMission.getName() );
        missionDetails.setType( findMission.getType() );
        missionDetails.setPhaseDescription( findMission.getPhaseDescription() );
        missionDetails.setAssociatedSettlement( unitReferenceMapper.unitToUnitReference( findMission.getAssociatedSettlement() ) );

        return missionDetails;
    }

    @Override
    public MissionDetails vehicleMissionToMissionDetails(VehicleMission found) {
        if ( found == null ) {
            return null;
        }

        MissionDetails missionDetails = new MissionDetails();

        missionDetails.setPhase( foundPhaseName( found ) );
        missionDetails.setNumPersons( found.getPeopleNumber() );
        missionDetails.setName( found.getName() );
        missionDetails.setType( found.getType() );
        missionDetails.setPhaseDescription( found.getPhaseDescription() );
        missionDetails.setAssociatedSettlement( unitReferenceMapper.unitToUnitReference( found.getAssociatedSettlement() ) );
        missionDetails.setVehicle( unitReferenceMapper.unitToUnitReference( found.getVehicle() ) );
        missionDetails.setLegETA( marsClockMapper.toString( found.getLegETA() ) );
        missionDetails.setTotalDistanceTravelled( found.getTotalDistanceTravelled() );
        missionDetails.setTotalRemainingDistance( found.getTotalRemainingDistance() );

        return missionDetails;
    }

    @Override
    public MissionDetails tradeToMissionDetails(Trade found) {
        if ( found == null ) {
            return null;
        }

        MissionDetails missionDetails = new MissionDetails();

        missionDetails.setPhase( foundPhaseName_( found ) );
        missionDetails.setDestinationSettlement( unitReferenceMapper.unitToUnitReference( found.getTradingSettlement() ) );
        missionDetails.setNumPersons( found.getPeopleNumber() );
        missionDetails.setName( found.getName() );
        missionDetails.setType( found.getType() );
        missionDetails.setPhaseDescription( found.getPhaseDescription() );
        missionDetails.setAssociatedSettlement( unitReferenceMapper.unitToUnitReference( found.getAssociatedSettlement() ) );
        missionDetails.setVehicle( unitReferenceMapper.unitToUnitReference( found.getVehicle() ) );
        missionDetails.setLegETA( marsClockMapper.toString( found.getLegETA() ) );
        missionDetails.setTotalDistanceTravelled( found.getTotalDistanceTravelled() );
        missionDetails.setTotalRemainingDistance( found.getTotalRemainingDistance() );
        missionDetails.setProfit( found.getProfit() );

        return missionDetails;
    }

    @Override
    public MissionDetails travelToSettlementToMissionDetails(TravelToSettlement found) {
        if ( found == null ) {
            return null;
        }

        MissionDetails missionDetails = new MissionDetails();

        missionDetails.setPhase( foundPhaseName__( found ) );
        missionDetails.setNumPersons( found.getPeopleNumber() );
        missionDetails.setName( found.getName() );
        missionDetails.setType( found.getType() );
        missionDetails.setPhaseDescription( found.getPhaseDescription() );
        missionDetails.setAssociatedSettlement( unitReferenceMapper.unitToUnitReference( found.getAssociatedSettlement() ) );
        missionDetails.setVehicle( unitReferenceMapper.unitToUnitReference( found.getVehicle() ) );
        missionDetails.setLegETA( marsClockMapper.toString( found.getLegETA() ) );
        missionDetails.setTotalDistanceTravelled( found.getTotalDistanceTravelled() );
        missionDetails.setTotalRemainingDistance( found.getTotalRemainingDistance() );
        missionDetails.setDestinationSettlement( unitReferenceMapper.unitToUnitReference( found.getDestinationSettlement() ) );

        return missionDetails;
    }

    @Override
    public MissionDetails rescueSalvageVehicleToMissionDetails(RescueSalvageVehicle found) {
        if ( found == null ) {
            return null;
        }

        MissionDetails missionDetails = new MissionDetails();

        missionDetails.setPhase( foundPhaseName___( found ) );
        missionDetails.setNumPersons( found.getPeopleNumber() );
        missionDetails.setDestinationVehicle( unitReferenceMapper.unitToUnitReference( found.getVehicleTarget() ) );
        missionDetails.setName( found.getName() );
        missionDetails.setType( found.getType() );
        missionDetails.setPhaseDescription( found.getPhaseDescription() );
        missionDetails.setAssociatedSettlement( unitReferenceMapper.unitToUnitReference( found.getAssociatedSettlement() ) );
        missionDetails.setVehicle( unitReferenceMapper.unitToUnitReference( found.getVehicle() ) );
        missionDetails.setLegETA( marsClockMapper.toString( found.getLegETA() ) );
        missionDetails.setTotalDistanceTravelled( found.getTotalDistanceTravelled() );
        missionDetails.setTotalRemainingDistance( found.getTotalRemainingDistance() );

        return missionDetails;
    }

    @Override
    public MissionDetails emergencySupplyToMissionDetails(EmergencySupplyMission found) {
        if ( found == null ) {
            return null;
        }

        MissionDetails missionDetails = new MissionDetails();

        missionDetails.setPhase( foundPhaseName____( found ) );
        missionDetails.setDestinationSettlement( unitReferenceMapper.unitToUnitReference( found.getEmergencySettlement() ) );
        missionDetails.setNumPersons( found.getPeopleNumber() );
        missionDetails.setName( found.getName() );
        missionDetails.setType( found.getType() );
        missionDetails.setPhaseDescription( found.getPhaseDescription() );
        missionDetails.setAssociatedSettlement( unitReferenceMapper.unitToUnitReference( found.getAssociatedSettlement() ) );
        missionDetails.setVehicle( unitReferenceMapper.unitToUnitReference( found.getVehicle() ) );
        missionDetails.setLegETA( marsClockMapper.toString( found.getLegETA() ) );
        missionDetails.setTotalDistanceTravelled( found.getTotalDistanceTravelled() );
        missionDetails.setTotalRemainingDistance( found.getTotalRemainingDistance() );

        return missionDetails;
    }

    @Override
    public MissionDetails areologyStudyFieldMissionToMissionDetails(AreologyStudyFieldMission found) {
        if ( found == null ) {
            return null;
        }

        MissionDetails missionDetails = new MissionDetails();

        missionDetails.setPhase( foundPhaseName_____( found ) );
        missionDetails.setNumPersons( found.getPeopleNumber() );
        missionDetails.setName( found.getName() );
        missionDetails.setType( found.getType() );
        missionDetails.setPhaseDescription( found.getPhaseDescription() );
        missionDetails.setAssociatedSettlement( unitReferenceMapper.unitToUnitReference( found.getAssociatedSettlement() ) );
        missionDetails.setVehicle( unitReferenceMapper.unitToUnitReference( found.getVehicle() ) );
        missionDetails.setLegETA( marsClockMapper.toString( found.getLegETA() ) );
        missionDetails.setTotalDistanceTravelled( found.getTotalDistanceTravelled() );
        missionDetails.setTotalRemainingDistance( found.getTotalRemainingDistance() );
        missionDetails.setScientificStudy( scientificStudyMapper.studyToScientificStudy( found.getScientificStudy() ) );

        return missionDetails;
    }

    @Override
    public MissionDetails biologyStudyFieldMissionToMissionDetails(BiologyStudyFieldMission found) {
        if ( found == null ) {
            return null;
        }

        MissionDetails missionDetails = new MissionDetails();

        missionDetails.setPhase( foundPhaseName______( found ) );
        missionDetails.setNumPersons( found.getPeopleNumber() );
        missionDetails.setName( found.getName() );
        missionDetails.setType( found.getType() );
        missionDetails.setPhaseDescription( found.getPhaseDescription() );
        missionDetails.setAssociatedSettlement( unitReferenceMapper.unitToUnitReference( found.getAssociatedSettlement() ) );
        missionDetails.setVehicle( unitReferenceMapper.unitToUnitReference( found.getVehicle() ) );
        missionDetails.setLegETA( marsClockMapper.toString( found.getLegETA() ) );
        missionDetails.setTotalDistanceTravelled( found.getTotalDistanceTravelled() );
        missionDetails.setTotalRemainingDistance( found.getTotalRemainingDistance() );
        missionDetails.setScientificStudy( scientificStudyMapper.studyToScientificStudy( found.getScientificStudy() ) );

        return missionDetails;
    }

    private String findMissionPhaseName(Mission mission) {

        if ( mission == null ) {
            return null;
        }
        MissionPhase phase = mission.getPhase();
        if ( phase == null ) {
            return null;
        }
        String name = phase.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String foundPhaseName(VehicleMission vehicleMission) {

        if ( vehicleMission == null ) {
            return null;
        }
        MissionPhase phase = vehicleMission.getPhase();
        if ( phase == null ) {
            return null;
        }
        String name = phase.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String foundPhaseName_(Trade trade) {

        if ( trade == null ) {
            return null;
        }
        MissionPhase phase = trade.getPhase();
        if ( phase == null ) {
            return null;
        }
        String name = phase.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String foundPhaseName__(TravelToSettlement travelToSettlement) {

        if ( travelToSettlement == null ) {
            return null;
        }
        MissionPhase phase = travelToSettlement.getPhase();
        if ( phase == null ) {
            return null;
        }
        String name = phase.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String foundPhaseName___(RescueSalvageVehicle rescueSalvageVehicle) {

        if ( rescueSalvageVehicle == null ) {
            return null;
        }
        MissionPhase phase = rescueSalvageVehicle.getPhase();
        if ( phase == null ) {
            return null;
        }
        String name = phase.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String foundPhaseName____(EmergencySupplyMission emergencySupplyMission) {

        if ( emergencySupplyMission == null ) {
            return null;
        }
        MissionPhase phase = emergencySupplyMission.getPhase();
        if ( phase == null ) {
            return null;
        }
        String name = phase.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String foundPhaseName_____(AreologyStudyFieldMission areologyStudyFieldMission) {

        if ( areologyStudyFieldMission == null ) {
            return null;
        }
        MissionPhase phase = areologyStudyFieldMission.getPhase();
        if ( phase == null ) {
            return null;
        }
        String name = phase.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String foundPhaseName______(BiologyStudyFieldMission biologyStudyFieldMission) {

        if ( biologyStudyFieldMission == null ) {
            return null;
        }
        MissionPhase phase = biologyStudyFieldMission.getPhase();
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
