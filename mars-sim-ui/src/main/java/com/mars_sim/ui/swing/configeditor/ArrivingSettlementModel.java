/*
 * Mars Simulation Project
 * ArrivingSettlementModel.java
 * @date 2022-06-15
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.configeditor;

import java.util.List;

import com.mars_sim.core.authority.AuthorityFactory;
import com.mars_sim.core.configuration.FutureSettlement;
import com.mars_sim.core.configuration.Scenario;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.structure.SettlementTemplateConfig;

/**
 * Represents a table model of the initial settlements. This has some intelligence
 * in knowing that populations are derived from a Template by default and similar
 * for Settlement name derived from Reporting Authority.
 * When either of the driving columns are changed the dependent columns are
 * recalculated.
 */
@SuppressWarnings("serial")
class ArrivingSettlementModel extends PotentialSettlementModel {

	/**
	 * @param settlementTemplateConfig
	 * @param raFactory 
	 */
	ArrivingSettlementModel(SettlementTemplateConfig settlementTemplateConfig, AuthorityFactory raFactory) {
		super(false, settlementTemplateConfig, raFactory);
	}

	
	/**
	 * Loads the default settlements in the table.
	 */
	public void loadDefaultSettlements(Scenario selected) {
		settlementInfoList.clear();

		for (var spec : selected.getArrivals()) {
			var info = toSettlementInfo(spec);
			settlementInfoList.add(info);
		}
			
		fireTableDataChanged();
	}

	/**
	 * Returns the arrival info, given the spec.
	 * 
	 * @param spec
	 * @return
	 */
	private PotentialSettlementInfo toSettlementInfo(FutureSettlement spec) {
		var info = new PotentialSettlementInfo();
		info.name = spec.name();
		info.sponsor = spec.sponsorCode();
		info.arrivalIn = spec.arrivalSols();
		info.template = spec.template();
		info.population = spec.populationNum();
		info.location = spec.landingLocation();
		return info;
	}


	/**
	 * Get the rows as Arriving Settlements
	 * @return
	 */
	public List<FutureSettlement> getArrivals() {
		return settlementInfoList.stream().map(info -> new FutureSettlement(info.name, info.template, info.sponsor,
				info.arrivalIn, info.location, info.population)).toList();
	}
	

	/**
	 * Adds a partial Settlement with the minimum information. The rest is defaulted from Sponsor & Template.
	 * 
	 * @param sponsor
	 * @param template
	 * @param location
	 */
	public void addPartialArrival(String sponsor, String template, Coordinates location) {
		var newRow = new FutureSettlement(tailorSettlementNameBySponsor(sponsor, 
											settlementInfoList.size()),
					template, sponsor, 1, location,
					ConfigModelHelper.determineNewSettlementPopulation(template, settlementTemplateConfig));
		settlementInfoList.add(toSettlementInfo(newRow));
		fireTableDataChanged();
	}
}
