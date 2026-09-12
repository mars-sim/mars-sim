/*
 * Mars Simulation Project
 * JProcessButton.java
 * @date 2023-02-18
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import javax.swing.Icon;
import javax.swing.JButton;

import com.mars_sim.core.resourceprocess.ResourceProcess.ProcessState;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.entitywindow.structure.ResourceProcessPanel;

/**
 * A JButton implementation that can be used to show running processes.
 * It is represented as a Red or Green dot.
 */
@SuppressWarnings("serial")
public class JProcessButton extends JButton {
    public static final Icon STOPPED_DOT = ImageLoader.getIconByName("dot/red");
    public static final Icon RUNNING_DOT = ImageLoader.getIconByName("dot/green");
    public static final Icon LOCK_ON_DOT = ImageLoader.getIconByName("dot/orange");
    
    public JProcessButton() {
        super();
    }

    /**
     * Sets the button's icon
     * 
     * @param ProcessState Current running state
     */
    public void setIcon(ProcessState state) {
    	if (state == ProcessState.LOCK_ON) {
    		setIcon(LOCK_ON_DOT);
    	}
    	else if (state == ProcessState.RUNNING) {
            setIcon(RUNNING_DOT);
        }
        else if (state == ProcessState.INPUTS_UNAVAILABLE) {
			setIcon(STOPPED_DOT);
        }
        else if (state == ProcessState.IDLE) {
 			setIcon(ResourceProcessPanel.IDLE_DOT);
        }
    }
}
