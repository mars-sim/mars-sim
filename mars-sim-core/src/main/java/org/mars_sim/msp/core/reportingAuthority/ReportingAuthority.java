/*
 * Mars Simulation Project
 * ReportingAuthority.java
 * @date 2023-05-31
 * @author Manny Kung
 */
package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.configuration.UserConfigurable;
import org.mars_sim.msp.core.person.NationSpecConfig;
import org.mars_sim.msp.core.person.ai.task.util.Worker;

/**
 * Represents a sponsor that owns units such as people, settlement, lunar colonies, etc..
 */
public class ReportingAuthority
implements UserConfigurable, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private boolean predefined;
	
	private boolean isCorporation;

	private double genderRatio;
	
	private String fullName;

	private String acronym;
	
	private MissionAgenda missionAgenda;
	
	private Organization organization;

	private List<String> countries;
	
	private List<Nation> nations = new ArrayList<>();

	private List<String> settlementNames;

	private List<String> vehicleNames;

 
	public ReportingAuthority(String acronym, String fullName, 
			boolean isCorporation, boolean predefined, 
			double genderRatio,
			MissionAgenda agenda, List<String> countries,
			List<String> names, List<String> vehicleNames) {
		
		this.fullName  = fullName;
		this.acronym = acronym;
		this.isCorporation = isCorporation;
		this.predefined = predefined;
		this.genderRatio = genderRatio;
		this.missionAgenda = agenda;
		this.countries = countries;	
		this.settlementNames = names;
		this.vehicleNames = vehicleNames;

		for (String c: countries) {
			nations.add(NationSpecConfig.getNation(c));
    	}
		
		if (isCorporation)
			organization = new Corporation(acronym, fullName);
		else
			organization = new Agency(acronym, fullName);
		
	}

	public Organization getOrganization() {
		return organization;
	}
	
	public boolean isCorporation() {
		return isCorporation;
	}
	
	/**
	 * Works on the mission objectives conducted.
	 * 
	 * @param unit
	 */
	public void conductMissionObjective(Worker unit) {
		missionAgenda.reportFindings(unit);
		missionAgenda.gatherData(unit);
	}

	/**
	 * Gets the Mission Agenda for this authority.
	 * 
	 * @return
	 */
	public MissionAgenda getMissionAgenda() {
		return missionAgenda;
	}

	/**
	 * Gets the code name or acronym of this authority.
	 * 
	 * @return
	 */
	@Override
	public String getName() {
		return acronym;
	}

	/**
	 * Gets the full name of the authority.
	 * 
	 * @return
	 */
	public String getDescription() {
		return fullName;
	}

	@Override
	public boolean isBundled() {
		return predefined;
	}
	
	/**
	 * Gets the countries associated to this Authority.
	 * 
	 * @return
	 */
	public List<String> getCountries() {
		return countries;
	}

	/**
	 * Does it belong to only one country ?
	 * 
	 * @return
	 */
	public boolean isOneCountry() {
		if (countries.size() == 1)
			return true;
		else
			return false;
	}
	
	/**
	 * Gets the only country to which this agency/organization belongs.
	 * 
	 * @return
	 */
	public String getOneCountry() {
		return countries.get(0);
	}
	
	/**
	 * Selects a country at random from those that this authority represents.
	 * 
	 * @return
	 */
	public String getRandomCountry() {
		if (countries.size() == 1) {
			return countries.get(0);
		}
		else {
			return countries.get(RandomUtil.getRandomInt(0, countries.size() - 1));	
		}
	}
	
	/**
	 * Gets the list of nations.
	 * 
	 * @return
	 */
	public List<Nation> getNations() {
		return nations;
	}
	
	/**
	 * Gets the only nation to which this agency/organization belongs.
	 * 
	 * @return
	 */
	public Nation getOneNation() {
		return nations.get(0);
	}
	
	/**
	 * Gets the name of Settlement for this Authority.
	 * 
	 * @return
	 */
	public List<String> getSettlementNames() {
		return settlementNames;
	}

	/**
	 * Gets the potential names of Vehicles.
	 * 
	 * @return
	 */
	public List<String> getVehicleNames() {
		return vehicleNames;
	}
	
	@Override
	public String toString() {
		return "ReportingAuthority [code=" + acronym + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((acronym == null) ? 0 : acronym.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReportingAuthority other = (ReportingAuthority) obj;
		if (acronym == null) {
			if (other.acronym != null)
				return false;
		} else if (!acronym.equals(other.acronym))
			return false;
		return true;
	}

	/**
	 * Gets the male /female ratio for this Authority.
	 * 
	 * @return percentage of Males to Females.
	 */
    public double getGenderRatio() {
        return genderRatio;
    }

	/** 
	 * Gets the predefined Preferences for this authority based on the Agenda/Objectives assigned.
	*/
    public Map<PreferenceKey, Double> getPreferences() {
        Map<PreferenceKey, Double> result = new HashMap<>();
		for (MissionCapability subAgenda : missionAgenda.getCapabilities()) {
			// Merge the various capabilities into one taking the largest
			Map<PreferenceKey, Double> subs = subAgenda.getPreferences();
			subs.forEach((k,v) -> result.merge(k, v, (v1,v2) ->
									Math.max(v1.doubleValue(), v2.doubleValue())));
		}
		return result;
    }
}
