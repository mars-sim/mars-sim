package org.mars_sim.msp.restws.mapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.restws.model.SettlementSummary;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2016-07-14T17:06:44-0700",
    comments = "version: 1.0.0.Final, compiler: javac, environment: Java 1.8.0_92 (Oracle Corporation)"
)
@Component
public class SettlementSummaryMapperImpl implements SettlementSummaryMapper {

    @Override
    public SettlementSummary settlementToSettlementSummary(Settlement settlement) {
        if ( settlement == null ) {
            return null;
        }

        SettlementSummary settlementSummary = new SettlementSummary();

        settlementSummary.setId( settlement.getIdentifier() );
        settlementSummary.setNumPersons( settlement.getCurrentPopulationNum() );
        settlementSummary.setNumParkedVehicles( settlement.getParkedVehicleNum() );
        settlementSummary.setName( settlement.getName() );

        settlementSummary.setNumBuildings( settlement.getBuildingManager().getBuildings().size() );

        return settlementSummary;
    }

    @Override
    public List<SettlementSummary> settlementsToSettlementSummarys(List<Settlement> settlements) {
        if ( settlements == null ) {
            return null;
        }

        List<SettlementSummary> list = new ArrayList<SettlementSummary>();
        for ( Settlement settlement : settlements ) {
            list.add( settlementToSettlementSummary( settlement ) );
        }

        return list;
    }
}
