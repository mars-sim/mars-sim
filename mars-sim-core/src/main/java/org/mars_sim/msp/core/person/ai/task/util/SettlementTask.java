/*
 * Mars Simulation Project
 * SettlementTask.java
 * @date 2023-06-16
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.util;

/**
 * This represents a TaskJob created by a SettlementMetaTask. 
 * It has a demand property that allows the same task to be used multiple times.
 */
public abstract class SettlementTask extends AbstractTaskJob {

	private static final long serialVersionUID = 1L;
	
    private SettlementMetaTask metaTask;
    private int demand;


    protected SettlementTask(SettlementMetaTask parent, String description, double score) {
        super(description, score);
        this.metaTask = parent;
        this.demand = 1;
    }

    /**
     * Sets a specific level of demand for this job.
     * 
     * @param demand New demand value.
     */
    protected void setDemand(int demand) {
        this.demand = demand;
    }

    /**
     * Returns how much demand is there for this Task.
     * 
     * @return
     */
    public int getDemand() {
        return demand;
    }

    /**
     * This task has been used by a Worker so decrease the demand. 
     * 
     * @return True if no more demand is needed.
     */
    boolean reduceDemand() {
        demand--;
        return (demand == 0);
    }

    /**
     * Returns the parent meta task.
     * 
     * @return
     */
    SettlementMetaTask getMeta() {
        return metaTask;
    }
}
