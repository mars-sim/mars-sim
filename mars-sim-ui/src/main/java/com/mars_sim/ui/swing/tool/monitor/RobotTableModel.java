/*
 * Mars Simulation Project
 * RobotTableModel.java
 * @date 2026-05-23
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.utils.model.BaseRobotModel;

/**
 * The model maintains a collection of Robots for the selected settlements. 
 * It uses the Base robot model to provide the cells values and montior for changes.
 */
public class RobotTableModel extends BaseRobotModel implements MonitorModel {

    private static final String ROBOTS = Msg.getString("robot.plural");

    private Set<Settlement> settlements = Collections.emptySet();

    public RobotTableModel() {
        super(NAME, SETTLEMENT, TASK, TYPE, LOCATION, HEALTH, MODE, BATTERY, BATTERY_TEMPERATURE, PERFORMANCE);
    }

    @Override
    public String getName() {
        return ROBOTS;
    }

    @Override
    public String getCountString() {
        return ROBOTS + " (" + getRowCount() + ")";
    }

    /**
     * Set the settlement filter for the model. This will select the Robots associated with the selected settlements.
     * @param selectedSettlement Selected settlements to filter by.
     * @return true if the filter was applied, false otherwise.
     */
    @Override
    public boolean setSettlementFilter(Set<Settlement> selectedSettlement) {
        settlements.forEach(s -> s.removeEntityListener(this));
        settlements = selectedSettlement;

		var entities = selectedSettlement.stream()
						.map(Settlement::getAllAssociatedRobots)
						.flatMap(Collection::stream)
						.toList();
        setEntities(entities);

        settlements.forEach(s -> s.addEntityListener(this));
        return true;
    }

    /**
     * Control whether the listeners are enabled or disabled.
     * @param activate Activate the listeners if true, disable if false.
     */
    @Override
    public void setMonitorEntities(boolean activate) {
        if (activate) {
            settlements.forEach(s -> s.addEntityListener(this));
        } else {
            settlements.forEach(s -> s.removeEntityListener(this));
        }
        super.setMonitorEntities(activate);
    }

    @Override
    public int getSettlementColumn() {
        return 1;
    }
}