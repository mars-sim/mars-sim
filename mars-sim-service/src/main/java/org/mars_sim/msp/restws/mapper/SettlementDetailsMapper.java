package org.mars_sim.msp.restws.mapper;


import org.mapstruct.Mapper;

import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.restws.model.SettlementDetails;

@Mapper(componentModel="spring",
		uses=CoordinatesMapper.class)
public interface SettlementDetailsMapper {
	@Mappings({
		@Mapping(source="identifier", target="id"),
		@Mapping(source="numCurrentPopulation", target="numPersons"),
		@Mapping(source="parkedVehicleNum", target="numParkedVehicles"),
		
		// Have to use expression because the Set class does not follow JavaBeans style
		@Mapping(expression="java(settlement.getInventory().getAllAmountResourcesStored(false).size())", target="numResources"),
		@Mapping(expression="java(settlement.getInventory().getAllItemResourcesStored().size())", target="numItems"),
		@Mapping(expression="java(settlement.getBuildingManager().getBuildings().size())", target="numBuildings")
	})
	SettlementDetails settlementToSettlementDetails(Settlement settlement);
}
