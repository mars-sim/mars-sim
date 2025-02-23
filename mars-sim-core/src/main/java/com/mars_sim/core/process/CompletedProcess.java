/*
 * Mars Simulation Project
 * CompletedProcess.java
 * @date 2024-03-02
 * @author Barry Evans
 */
package com.mars_sim.core.process;

import java.io.Serializable;

/**
 * Records the completion of a Process in a Building.
 */
public record CompletedProcess(String process, String type, String buildingName) 
    implements Serializable {}
