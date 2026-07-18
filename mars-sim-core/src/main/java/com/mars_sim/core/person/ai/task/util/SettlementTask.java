/*
 * Mars Simulation Project
 * SettlementTask.java
 * @date 2023-06-16
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task.util;

import com.mars_sim.core.Entity;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.ai.task.util.MetaTask.TaskScope;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;

/**
 * This represents a TaskJob created by a SettlementMetaTask. 
 * It has a demand property that allows the same task to be used multiple times.
 */
public abstract class SettlementTask extends AbstractTaskJob {

	private static final long serialVersionUID = 1L;
	
    private final Settlement owner;
    
    private final SettlementMetaTask metaTask;
    private final Entity focus;
    private String shortName;
    private boolean needsEVA = false;
    private TaskScope scope = TaskScope.ANY_HOUR;
    private int demand;
    private MarsTime createdOn;

    /**
     * Creates an abstract Settlement task for the backlog that relates to an Entity within a Settlement
     * that can be executed by any Citizen.
     * 
     * @param parent The meta task that defines the eventual Task.
     * @param owner Settlement owning this shared task.
     * @param name Name to the potential task
     * @param focus Entity the focus of the work; maybe null
     * @param score The Rating score for this work
     */
    protected SettlementTask(SettlementMetaTask parent, Settlement owner, String name, Entity focus, RatingScore score) {
        super(name + (focus != null ? " @ " + focus.getName() : ""), score);
        
        this.demand = 1;
        this.owner = owner;
        this.focus = focus;
        this.shortName = name;
        this.metaTask = parent;
        if (parent instanceof MetaTask mt) {
            this.scope = mt.getScope();
        } else {
            this.scope = TaskScope.ANY_HOUR;
        }

        // Easiest way to timestamp
        this.createdOn = Simulation.instance().getMasterClock().getMarsTime();
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
     * When was this task created.
     */
    public MarsTime getCreatedOn() {
        return createdOn;
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
     * Overrides the default scope.
     * 
     * @param newScope
     */
    protected void setScope(TaskScope newScope) {
        scope = newScope;
    }

    /**
     * Gets the scope of the working hours for this Task.
     */
    public TaskScope getScope() {
        return scope;
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
     * Decreases the demand as this task has been used by a worker. 
     * 
     * @return True if no more demand is needed.
     */
    void reduceDemand() {
        demand--;
        if (demand == 0) {
            owner.getTaskManager().removeTask(this);
        }
    }

    /**
     * Returns the parent meta task.
     * 
     * @return
     */
    public SettlementMetaTask getMeta() {
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

    public Settlement getOwner() {
        return owner;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + metaTask.hashCode();
        result = prime * result + owner.hashCode();
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
        if (!metaTask.equals(other.metaTask))
            return false;
        if (!owner.equals(other.owner))
            return false;
        if (focus == null) {
            if (other.focus != null)
                return false;
        } else if (!focus.equals(other.focus))
            return false;
        return true;
    }

    /**
     * Updates the parameters of this task based on another Task.
     * Parameters cover the demand and score.
     * @param newTask The new task to update from.
     * @return True if parameters were updated
     */
    boolean updateParameters(SettlementTask newTask) {
        if (demand != newTask.demand || getScore().getScore() != newTask.getScore().getScore()) {
            demand = newTask.demand;
            setScore(newTask.getScore());
            createdOn = newTask.createdOn;
            return true;
        }
        return false;
    }
}
