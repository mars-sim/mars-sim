/*
 * Mars Simulation Project
 * RobotTemplate.java
 * @date 2022-12-22
 * @author Barry Evans
 */
package com.mars_sim.core.structure;

import java.io.Serializable;

import com.mars_sim.core.robot.RobotType;

/**
 * Represents are predefined Robot for a settlement template
 */
public class RobotTemplate implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
    private String name;
    private RobotType type;
    private String model;

    RobotTemplate(String name, RobotType type, String model) {
        this.name = name;
        this.type = type;
        this.model = model;
    }

    public String getName() {
        return name;
    }

    public RobotType getType() {
        return type;
    }

    public String getModel() {
        return model;
    }

}
