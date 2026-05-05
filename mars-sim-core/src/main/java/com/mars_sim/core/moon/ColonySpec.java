/*
 * Mars Simulation Project
 * ColonySpec.java
 * @date 2026-05-05
 * @author Manny Kung
 */

package com.mars_sim.core.moon;

import java.io.Serializable;

import com.mars_sim.core.map.location.Coordinates;

public record ColonySpec(String name, String sponsor, Coordinates coord) 
implements Serializable {}