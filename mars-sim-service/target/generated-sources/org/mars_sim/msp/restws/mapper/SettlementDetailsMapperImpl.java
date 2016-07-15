package org.mars_sim.msp.restws.mapper;

import javax.annotation.Generated;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.restws.model.SettlementDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2016-07-14T17:06:44-0700",
    comments = "version: 1.0.0.Final, compiler: javac, environment: Java 1.8.0_92 (Oracle Corporation)"
)
@Component
public class SettlementDetailsMapperImpl implements SettlementDetailsMapper {

    @Autowired
    private CoordinatesMapper coordinatesMapper;

    @Override
    public SettlementDetails settlementToSettlementDetails(Settlement settlement) {
        if ( settlement == null ) {
            return null;
        }

        SettlementDetails settlementDetails = new SettlementDetails();

        settlementDetails.setNumPersons( settlement.getCurrentPopulationNum() );
        settlementDetails.setId( settlement.getIdentifier() );
        settlementDetails.setNumParkedVehicles( settlement.getParkedVehicleNum() );
        settlementDetails.setName( settlement.getName() );
        settlementDetails.setPopulationCapacity( settlement.getPopulationCapacity() );
        settlementDetails.setAirPressure( settlement.getAirPressure() );
        settlementDetails.setTemperature( settlement.getTemperature() );
        settlementDetails.setTotalScientificAchievement( settlement.getTotalScientificAchievement() );
        settlementDetails.setCoordinates( coordinatesMapper.coordinatesToCoordinateDTO( settlement.getCoordinates() ) );

        settlementDetails.setNumResources( settlement.getInventory().getAllAmountResourcesStored(false).size() );
        settlementDetails.setNumBuildings( settlement.getBuildingManager().getBuildings().size() );
        settlementDetails.setNumItems( settlement.getInventory().getAllItemResourcesStored().size() );

        return settlementDetails;
    }
}
