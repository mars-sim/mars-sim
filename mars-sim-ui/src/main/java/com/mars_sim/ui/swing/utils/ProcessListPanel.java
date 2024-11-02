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
            if (p instanceof ManufacturePanel mp) {
                mp.update();
            }
            else if (p instanceof SalvagePanel sp) {
                sp.update();
            }
        }
    }
        
    private void updateSalvageProcesses(List<SalvageProcess> salvages) {
        if (!salvageCache.equals(salvages)) {
            // Add salvage panels for new salvage processes.
            for(var salvage : salvages) {
                if (!salvageCache.contains(salvage))
                    add(new SalvagePanel(salvage, showBuilding, WORD_WIDTH));
            }

            // Remove salvage panels for old salvages.
            for(var salvage : salvageCache) {
                if (!salvages.contains(salvage)) {
                    var panel = getSalvagePanel(salvage);
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
                    add(new ManufacturePanel(process, showBuilding, WORD_WIDTH));
            }

            // Remove  panels for old processes.
            for(var process : processCache) {
                if (!processes.contains(process)) {
                    var panel = getManufacturePanel(process);
                    if (panel != null)
                        remove(panel);
                }
            }

            // Update salvageCache
            processCache = new ArrayList<>(processes);
        }
    }           
        
    /**
	 * Gets the panel for a manufacture process.
	 * 
	 * @param process the manufacture process.
	 * @return manufacture panel or null if none.
	 */
	private ManufacturePanel getManufacturePanel(ManufactureProcess process) {
		ManufacturePanel result = null;
		for (int x = 0; x < getComponentCount(); x++) {
			Component component = getComponent(x);
			if ((component instanceof ManufacturePanel panel)
				&& panel.getManufactureProcess().equals(process)) {
					result = panel;
			}
		}
		return result;
	}

	/**
	 * Gets the panel for a salvage process.
	 * 
	 * @param process the salvage process.
	 * @return the salvage panel or null if none.
	 */
	private SalvagePanel getSalvagePanel(SalvageProcess process) {
		SalvagePanel result = null;
		for (int x = 0; x < getComponentCount(); x++) {
			Component component = getComponent(x);
			if ((component instanceof SalvagePanel panel)
                && panel.getSalvageProcess().equals(process)) {
					result = panel;
			}
		}
		return result;
	}
}
