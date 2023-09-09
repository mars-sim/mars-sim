/*
 * Mars Simulation Project
 * SettlementTask.java
 * @date 2023-06-16
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.util;

import org.mars_sim.msp.core.Entity;

/**
 * This represents a TaskJob created by a SettlementMetaTask. 
 * It has a demand property that allows the same task to be used multiple times.
 */
public abstract class SettlementTask extends AbstractTaskJob {

	private static final long serialVersionUID = 1L;
	
    private int demand;
    
    private SettlementMetaTask metaTask;
    private Entity focus;

    protected SettlementTask(SettlementMetaTask parent, String description, Entity focus, double score) {
        super(description, score);
        this.metaTask = parent;
        this.demand = 1;
        this.focus = focus;
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

    /**
     * Get the Entity that is the focus of this Task. Default returns null as this is overridden
     * @return
     */
    public Entity getFocus() {
        return focus;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((metaTask == null) ? 0 : metaTask.hashCode());
        result = prime * result + ((focus == null) ? 0 : focus.hashCode());
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
        SettlementTask other = (SettlementTask) obj;
        if (metaTask == null) {
            if (other.metaTask != null)
                return false;
        } else if (!metaTask.equals(other.metaTask))
            return false;
        if (focus == null) {
            if (other.focus != null)
                return false;
        } else if (!focus.equals(other.focus))
            return false;
        return true;
    }
}
