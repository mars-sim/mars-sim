/*
 * Mars Simulation Project
 * InitialSettlementModel.java
 * @date 2022-06-15
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.configeditor;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.authority.AuthorityFactory;
import com.mars_sim.core.configuration.Scenario;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.structure.InitialSettlement;
import com.mars_sim.core.structure.SettlementTemplateConfig;

/**
 * Represents a table model of the initial settlements. This has some intelligence
 * in knowing that populations are derived from a Template by default and similar
 * for Settlement name derived from Reporting Authority.
 * When either of the driving columns are changed the dependent columns are
 * recalculated.
 */
@SuppressWarnings("serial")
class InitialSettlementModel extends PotentialSettlementModel {

	/**
	 * Constructor.
	 * 
	 * @param settlementTemplateConfig
	 * @param raFactory 
	 */
	InitialSettlementModel(SettlementTemplateConfig settlementTemplateConfig, AuthorityFactory raFactory) {
		super(true, settlementTemplateConfig, raFactory);
	}

	
	/**
	 * Load the default settlements in the table.
	 */
	public void loadDefaultSettlements(Scenario selected) {
		settlementInfoList.clear();

		for (InitialSettlement spec : selected.getSettlements()) {
			var info = toSettlementInfo(spec);
				
			settlementInfoList.add(info);
		}
			
		fireTableDataChanged();
	}

	private PotentialSettlementInfo toSettlementInfo(InitialSettlement spec) {
		PotentialSettlementInfo info = new PotentialSettlementInfo();
		info.name = spec.getName();
		info.sponsor = spec.getSponsor();
		info.crew = spec.getCrew();
		info.template = spec.getSettlementTemplate();
		info.population = spec.getPopulationNumber();
		info.location = spec.getLocation();
		return info;
	}


	/**
	 * Get the rows as InitialSettlements.
	 * @return
	 */
	public List<InitialSettlement> getSettlements() {
		return settlementInfoList.stream().map(info -> new InitialSettlement(info.name, info.sponsor, info.template,
												info.population, 0, info.location, info.crew)).toList();
	}

	/**
	 * Add a partial Settlement with the minimum information. The rest is defaulted fron Sponsor & Template.
	 * @param sponsor
	 * @param template
	 * @param location
	 */
	public void addPartialSettlement(String sponsor, String template, Coordinates location) {
		InitialSettlement newRow = new InitialSettlement(tailorSettlementNameBySponsor(sponsor, 
											settlementInfoList.size()),
					sponsor, template,
					ConfigModelHelper.determineNewSettlementPopulation(template, settlementTemplateConfig),
					0,
					location, null);
		settlementInfoList.add(toSettlementInfo(newRow));
		fireTableDataChanged();
	}
}
