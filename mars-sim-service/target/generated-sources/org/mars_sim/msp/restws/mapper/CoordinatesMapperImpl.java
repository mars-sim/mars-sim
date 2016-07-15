package org.mars_sim.msp.restws.mapper;

import javax.annotation.Generated;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.restws.model.CoordinateDTO;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2016-07-14T17:06:43-0700",
    comments = "version: 1.0.0.Final, compiler: javac, environment: Java 1.8.0_92 (Oracle Corporation)"
)
@Component
public class CoordinatesMapperImpl implements CoordinatesMapper {

    @Override
    public CoordinateDTO coordinatesToCoordinateDTO(Coordinates location) {
        if ( location == null ) {
            return null;
        }

        CoordinateDTO coordinateDTO = new CoordinateDTO();

        coordinateDTO.setLatitude( location.getFormattedLatitudeString() );
        coordinateDTO.setLongitude( location.getFormattedLongitudeString() );
        coordinateDTO.setTheta( location.getTheta() );
        coordinateDTO.setPhi( location.getPhi() );

        return coordinateDTO;
    }
}
