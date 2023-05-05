/**
 * Mars Simulation Project
 * Stage.java
 * @date 2023-04-13
 * @author Barry Evans
 */
package org.mars_sim.msp.core.project;

/**
 * Describes the stage a Project is current in. Steps must follow the order of the stage.
 */
public enum Stage {
	WAITING,
	PREPARATION,
	ACTIVE,
	CLOSEDOWN,
	DONE,
	ABORTED
}