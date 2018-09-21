package org.mars_sim.msp.restws.mapper;


import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.restws.model.SettlementSummary;

@Mapper(componentModel="spring")
public interface SettlementSummaryMapper {
	@Mappings({
		@Mapping(source="identifier", target="id"),
		@Mapping(source="numCurrentPopulation", target="numPersons"),
		@Mapping(source="parkedVehicleNum", target="numParkedVehicles"),
		
		@Mapping(expression="java(settlement.getBuildingManager().getBuildings().size())", target="numBuildings")
	})
	SettlementSummary settlementToSettlementSummary(Settlement settlement);
	
	List<SettlementSummary> settlementsToSettlementSummarys(List<Settlement> settlements);
}
