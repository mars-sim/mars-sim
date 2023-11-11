/*
 * Mars Simulation Project
 * SettlementTask.java
 * @date 2023-06-16
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task.util;

import com.mars_sim.core.Entity;
import com.mars_sim.core.data.RatingScore;

/**
 * This represents a TaskJob created by a SettlementMetaTask. 
 * It has a demand property that allows the same task to be used multiple times.
 */
public abstract class SettlementTask extends AbstractTaskJob {

	private static final long serialVersionUID = 1L;
	
    private int demand;
    
    private SettlementMetaTask metaTask;
    private Entity focus;
    private String shortName;
    private boolean needsEVA = false;

    /**
     * Creates an abstract Settlement task for the backlog that relates to an Entity within a Settlement
     * that can be executed by any Citizen.
     * 
     * @param parent The metatask that defines the eventual Task.
     * @param name Name to the potential task
     * @param focus Entity the focus of the work; maybe null
     * @param score The Rating score for this work
     */
    protected SettlementTask(SettlementMetaTask parent, String name, Entity focus, RatingScore score) {
        super(name + (focus != null ? " @ " + focus.getName() : ""), score);
        this.metaTask = parent;
        this.demand = 1;
        this.focus = focus;
        this.shortName = name;
    }

    /**
     * Gets a short name that does not include the Entity reference.
     * 
     * @return
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Does this task need a an EVA activity ?
     */
    public boolean isEVA() {
        return needsEVA;
    }

    protected void setEVA(boolean eva) {
        needsEVA = eva;
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
     * Gets the Entity that is the focus of this Task. Default returns null as this is overridden.
     * 
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
        if (needsEVA != other.needsEVA)
            return false;
        if (focus == null) {
            if (other.focus != null)
                return false;
        } else if (!focus.equals(other.focus))
            return false;
        return true;
    }
}
