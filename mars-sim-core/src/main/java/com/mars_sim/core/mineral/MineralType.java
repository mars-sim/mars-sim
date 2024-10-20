/*
 * Mars Simulation Project
 * MineralType.java
 * @date 2024-10-19
 * @author Barry Evans
 */
package com.mars_sim.core.mineral;

import java.io.Serializable;
import java.util.Set;

/**
 * Represents a type of mineral and it's properties.
 * It provides extended properties to an existign AmountResource of type mineral.
 * This is immutable and the resourceId is unique key.
 * 
 */
public class MineralType implements Serializable {
	private int resourceId;
	private String name;
	private String colour;
	private int frequency;
	private Set<String> locales;

	MineralType(int resourceId, String name, String colour, int frequency, Set<String> locales) {
		this.resourceId = resourceId;
		this.name = name;
		this.colour = colour;
		this.frequency = frequency;
		this.locales = locales;
	}

	public int getResourceId() {
		return resourceId;
	}

	public String getName() {
		return name;
	}

	public String getColour() {
		return colour;
	}

	public int getFrequency() {
		return frequency;
	}

    /**
     * Get the surface region locales where this mineral can be found
     * @return Locales name of favoured regions.
     */
	public Set<String> getLocales() {
		return locales;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + resourceId;
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
		MineralType other = (MineralType) obj;
		return (resourceId != other.resourceId);
	}
}