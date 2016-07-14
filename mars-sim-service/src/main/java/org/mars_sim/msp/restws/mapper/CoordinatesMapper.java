package org.mars_sim.msp.restws.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.restws.model.CoordinateDTO;

/**
 * This provides a mapper from a msp-core Person entity to the lightweight PersonSummary DTO.
 */
@Mapper(componentModel="spring")
public interface CoordinatesMapper {
	@Mappings({
		@Mapping(source="formattedLongitudeString", target="longitude"),
		@Mapping(source="formattedLatitudeString", target="latitude")
	})
	CoordinateDTO coordinatesToCoordinateDTO(Coordinates location);
}
