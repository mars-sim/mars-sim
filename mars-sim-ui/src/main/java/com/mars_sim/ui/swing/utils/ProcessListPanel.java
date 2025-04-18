/*
 * Mars Simulation Project
 * ProcessListPanel.java
 * @date 2024-11-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.mars_sim.core.manufacture.ManufactureProcess;
import com.mars_sim.core.manufacture.SalvageProcess;
import com.mars_sim.core.manufacture.WorkshopProcess;

/**
 * This is a panel that renders a list of workshop processes that are active
 */
public class ProcessListPanel extends JPanel {
	private static final int WORD_WIDTH = 70;

    private List<ManufactureProcess> processCache = Collections.emptyList();
	private List<SalvageProcess> salvageCache = Collections.emptyList();
    
    private boolean showBuilding;
    
    public ProcessListPanel(boolean showBuilding) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.showBuilding = showBuilding;
    }
    
    /**
     * Update the panel with updated processes. These maybe the same as those already displayed
     * @param processes
     * @param salvages
     */
    public void update(List<ManufactureProcess> processes, List<SalvageProcess> salvages) {
        // Update existing list contents
        updateManuProcess(processes);
        updateSalvageProcesses(salvages);
                                
        // Update actual panels
        for(var p : getComponents()) {
            if (p instanceof WorkshopProcessPanel mp) {
                mp.update();
            }
        }
    }
        
    private void updateSalvageProcesses(List<SalvageProcess> salvages) {
        if (!salvageCache.equals(salvages)) {
            // Add salvage panels for new salvage processes.
            for(var salvage : salvages) {
                if (!salvageCache.contains(salvage))
                    add(new WorkshopProcessPanel(salvage, showBuilding, WORD_WIDTH));
            }

            // Remove salvage panels for old salvages.
            for(var salvage : salvageCache) {
                if (!salvages.contains(salvage)) {
                    var panel = getProcessPanel(salvage);
                    if (panel != null)
                        remove(panel);
                }
            }

            // Update salvageCache
            salvageCache = new ArrayList<>(salvages);
        }
    }
                
    private void updateManuProcess(List<ManufactureProcess> processes) {
        if (!processCache.equals(processes)) {
            // Add manu panels for new processes.
            for(var process : processes) {
                if (!processCache.contains(process))
                    add(new WorkshopProcessPanel(process, showBuilding, WORD_WIDTH));
            }

            // Remove  panels for old processes.
            for(var process : processCache) {
                if (!processes.contains(process)) {
                    var panel = getProcessPanel(process);
                    if (panel != null)
                        remove(panel);
                }
            }

            // Update salvageCache
            processCache = new ArrayList<>(processes);
        }
    }           
        
    /**
	 * Gets the panel for a  process.
	 * 
	 * @param process the  process.
	 * @return  panel or null if none.
	 */
	private WorkshopProcessPanel getProcessPanel(WorkshopProcess process) {
		for (int x = 0; x < getComponentCount(); x++) {
			Component component = getComponent(x);
			if ((component instanceof WorkshopProcessPanel panel)
				&& panel.getProcess().equals(process)) {
					return panel;
			}
		}
		return null;
	}
}
