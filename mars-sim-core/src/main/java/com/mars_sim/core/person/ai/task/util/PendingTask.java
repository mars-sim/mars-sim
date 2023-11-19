/*
 * Mars Simulation Project
 * PendingTask.java
 * @date 2023-11-19
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task.util;

import java.io.Serializable;

import com.mars_sim.core.time.MarsTime;

/**
 * Represents a Task that is on the Pending Queue
 */
public record PendingTask(MarsTime when, TaskJob job) implements Serializable
{}
