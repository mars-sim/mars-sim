/*
 * Mars Simulation Project
 * ReportingAuthority.java
 * @date 2023-05-31
 * @author Manny Kung
 */
package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.configuration.UserConfigurable;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Represents a Reporting Authority that "owns" units.
 */
public class ReportingAuthority
implements UserConfigurable, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private MissionAgenda missionAgenda;

	private String description;

	private String name;

	private List<String> countries;

	private List<String> settlementNames;

	private List<String> vehicleNames;

	private boolean predefined;

	private double genderRatio;
 
	public ReportingAuthority(String name, String description, boolean predefined, double genderRatio,
							  MissionAgenda agenda, List<String> countries,
							  List<String> names, List<String> vehicleNames) {
		this.description  = description;
		this.name = name;
		this.predefined = predefined;
		this.genderRatio = genderRatio;
		this.missionAgenda = agenda;
		this.countries = countries;
		this.settlementNames = names;
		this.vehicleNames = vehicleNames;
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
	 * Gets the unique name of this authority which is the short code.
	 * 
	 * @return
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Gets the full name of the authority.
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
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
	 * Gets the default Country for Authority. If there are multiples
	 * then one is chosen.
	 * 
	 * @return
	 */
	public String getDefaultCountry() {
		if (countries.size() == 1) {
			return countries.get(0);
		}
		else {
			return countries.get(RandomUtil.getRandomInt(0, countries.size() - 1));	
		}
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
		return "ReportingAuthority [code=" + name + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}


	/**
	 * Gets the male /female ratio for this Authority.
	 * 
	 * @return percetnage of Males to Females.
	 */
    public double getGenderRatio() {
        return genderRatio;
    }

	/** 
	 * Get the predefined Preferences for this authority based on the Agenda/Objectives assigned
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
