/*
 * Mars Simulation Project
 * JProcessButton.java
 * @date 2023-02-18
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.utils;

import javax.swing.Icon;
import javax.swing.JButton;

import org.mars_sim.msp.ui.swing.ImageLoader;

/**
 * A JButton implementation that can be used to show running processes.
 * It is represented as a Red or Green dot.
 */
@SuppressWarnings("serial")
public class JProcessButton extends JButton {
    private static final Icon RED_DOT = ImageLoader.getIconByName("dot/red");
    private static final Icon GREEN_DOT = ImageLoader.getIconByName("dot/green");

    public JProcessButton() {
        super();
    }

    /**
     * Set the button in a running state
     * @param running Current running state
     */
    public void setRunning(boolean running) {
		if (running) {
            setIcon(GREEN_DOT);
        }
        else {
			setIcon(RED_DOT);
        }
    }
};
