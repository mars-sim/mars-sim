package org.mars_sim.msp.restws.mapper;

import javax.annotation.Generated;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.restws.model.EntityReference;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2016-07-14T17:06:44-0700",
    comments = "version: 1.0.0.Final, compiler: javac, environment: Java 1.8.0_92 (Oracle Corporation)"
)
@Component
public class MissionReferenceMapperImpl implements MissionReferenceMapper {

    @Override
    public EntityReference missionToEntityReference(Mission reference) {
        if ( reference == null ) {
            return null;
        }

        EntityReference entityReference = new EntityReference();

        entityReference.setId( reference.getIdentifier() );
        entityReference.setName( reference.getName() );

        return entityReference;
    }
}
