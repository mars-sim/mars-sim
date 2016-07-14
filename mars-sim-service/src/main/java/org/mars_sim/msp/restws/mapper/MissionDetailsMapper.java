package org.mars_sim.msp.restws.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mars_sim.msp.core.person.ai.mission.AreologyStudyFieldMission;
import org.mars_sim.msp.core.person.ai.mission.BiologyStudyFieldMission;
import org.mars_sim.msp.core.person.ai.mission.EmergencySupplyMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.restws.model.MissionDetails;

/**
 * Each mapper method should include the @Mapping lists from the superclasses.
 */
@Mapper(componentModel="spring",
uses={UnitReferenceMapper.class, MarsClockMapper.class, ScientificStudyMapper.class})
public interface MissionDetailsMapper {

	@Mappings({
		@Mapping(source="peopleNumber", target="numPersons"),
		@Mapping(source="phase.name", target="phase")
	})
	MissionDetails missionToMissionDetails(Mission findMission);

	@Mappings({
		@Mapping(source="peopleNumber", target="numPersons"),
		@Mapping(source="phase.name", target="phase")
	})
	MissionDetails vehicleMissionToMissionDetails(VehicleMission found);

	@Mappings({
		@Mapping(source="peopleNumber", target="numPersons"),
		@Mapping(source="phase.name", target="phase"),
		@Mapping(source="tradingSettlement", target="destinationSettlement")
	})
	MissionDetails tradeToMissionDetails(Trade found);

	@Mappings({
		@Mapping(source="peopleNumber", target="numPersons"),
		@Mapping(source="phase.name", target="phase")
	})
	MissionDetails travelToSettlementToMissionDetails(TravelToSettlement found);

	@Mappings({
		@Mapping(source="peopleNumber", target="numPersons"),
		@Mapping(source="vehicleTarget", target="destinationVehicle"),
		@Mapping(source="phase.name", target="phase")
	})
	MissionDetails rescueSalvageVehicleToMissionDetails(RescueSalvageVehicle found);

	@Mappings({
		@Mapping(source="peopleNumber", target="numPersons"),
		@Mapping(source="phase.name", target="phase"),
		@Mapping(source="emergencySettlement", target="destinationSettlement")
	})
	MissionDetails emergencySupplyToMissionDetails(EmergencySupplyMission found);

	@Mappings({
		@Mapping(source="peopleNumber", target="numPersons"),
		@Mapping(source="phase.name", target="phase")
	})
	MissionDetails areologyStudyFieldMissionToMissionDetails(AreologyStudyFieldMission found);

	@Mappings({
		@Mapping(source="peopleNumber", target="numPersons"),
		@Mapping(source="phase.name", target="phase")
	})
	MissionDetails biologyStudyFieldMissionToMissionDetails(BiologyStudyFieldMission found);
}
