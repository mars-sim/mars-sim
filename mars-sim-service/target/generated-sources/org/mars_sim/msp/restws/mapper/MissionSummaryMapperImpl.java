package org.mars_sim.msp.restws.mapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionPhase;
import org.mars_sim.msp.restws.model.MissionSummary;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2016-07-14T17:06:44-0700",
    comments = "version: 1.0.0.Final, compiler: javac, environment: Java 1.8.0_92 (Oracle Corporation)"
)
@Component
public class MissionSummaryMapperImpl implements MissionSummaryMapper {

    @Override
    public MissionSummary missionToMissionSummary(Mission mission) {
        if ( mission == null ) {
            return null;
        }

        MissionSummary missionSummary = new MissionSummary();

        missionSummary.setPhase( missionPhaseName( mission ) );
        missionSummary.setId( mission.getIdentifier() );
        missionSummary.setNumPersons( mission.getPeopleNumber() );
        missionSummary.setName( mission.getName() );
        missionSummary.setType( mission.getType() );

        return missionSummary;
    }

    @Override
    public List<MissionSummary> missionsToMissionSummarys(List<Mission> asList) {
        if ( asList == null ) {
            return null;
        }

        List<MissionSummary> list = new ArrayList<MissionSummary>();
        for ( Mission mission : asList ) {
            list.add( missionToMissionSummary( mission ) );
        }

        return list;
    }

    private String missionPhaseName(Mission mission) {

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
}
