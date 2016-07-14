package org.mars_sim.msp.restws.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.restws.model.BuildingDetails;

@Mapper(componentModel="spring")
public interface BuildingMapper {
	@Mappings({
		@Mapping(source="identifier", target="id")
	})
	BuildingDetails buildingToBuildingDetails(Building building);
	
	List<BuildingDetails> buildingtoBuildingfDetails(List<Building> buildings);
}
