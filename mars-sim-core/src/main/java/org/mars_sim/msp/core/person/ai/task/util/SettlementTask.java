/*
 * Mars Simulation Project
 * SettlementTask.java
 * @date 2022-11-28
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.util;

/**
 * This represents a TaskJob created by a SettlementMetaTask.
 */
public abstract class SettlementTask extends AbstractTaskJob {

    private SettlementMetaTask metaTask;


    protected SettlementTask(SettlementMetaTask parent, String description, double score) {
        super(description, score);
        this.metaTask = parent;
    }

    /**
     * What is the parent meta task.
     * @return
     */
    SettlementMetaTask getMeta() {
        return metaTask;
    }
}
