package org.mars_sim.msp.restws.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.restws.model.EntityReference;

/**
 * This provides a mapper from a reference to a msp-core Unit..
 */
@Mapper(componentModel="spring")
public interface UnitReferenceMapper {
	@Mappings({
		@Mapping(source="identifier", target="id"),
	})
	EntityReference unitToUnitReference(Unit reference);
}
