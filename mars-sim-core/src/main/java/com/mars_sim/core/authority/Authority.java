/*
 * Mars Simulation Project
 * Authority.java
 * @date 2023-05-31
 * @author Manny Kung
 */
package com.mars_sim.core.authority;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.Entity;
import com.mars_sim.core.configuration.UserConfigurable;
import com.mars_sim.core.parameter.ParameterManager;
import com.mars_sim.core.person.NationSpecConfig;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.tool.RandomUtil;

/**
 * Represents a sponsor that owns units such as people, settlement, lunar colonies, etc..
 */
public class Authority
implements Entity, UserConfigurable {

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

	/**
	 * Constructor.
	 * 
	 * @param acronym
	 * @param fullName
	 * @param isCorporation
	 * @param predefined
	 * @param genderRatio
	 * @param agenda
	 * @param countries
	 * @param names
	 * @param vehicleNames
	 */
	public Authority(String acronym, String fullName, 
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
	 * The context of the Authority is null as it is a top-level entity.
	 * @return Returns null.
	 */
	@Override
	public String getContext() {
		return null;
	}

	/**
	 * Gets the full name of the authority.
	 * 
	 * @return
	 */
	@Override
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
		return (countries.size() == 1);
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
		Authority other = (Authority) obj;
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
    public ParameterManager getPreferences() {
		// Construct preferences as a combination of the capabilities
		return new ParameterManager(missionAgenda.getCapabilities().stream()
								.map(m -> m.getPreferences()).toList());
    }
}
