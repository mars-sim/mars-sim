/**
 * Mars Simulation Project
 * SeparatorPanel.java
 * @version 3.1.0 2019-09-20
 * Modified by Manny Kung
 * Based on Java Swing Hacks
 */


package org.mars_sim.msp.ui.swing.tool;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

public class SeparatorPanel extends JPanel {

    private static final long serialVersionUID = 335333326611592227L;
    private static final Color DEFAULT_LEFT_COLOR = Color.WHITE;
    private static final Color DEFAULT_RIGHT_COLOR = Color.LIGHT_GRAY;
    private Color leftColor = DEFAULT_LEFT_COLOR;
    private Color rightColor = DEFAULT_RIGHT_COLOR;

    public SeparatorPanel() {
        setOpaque(false);
        this.setPreferredSize(new Dimension(3, 12));
    }

    public SeparatorPanel(Color leftColor, Color rightColor) {
        this();
        this.leftColor = leftColor;
        this.rightColor = rightColor;
    }

    @Override
	public void paintComponent(Graphics g) {
        g.setColor(leftColor);
        g.drawLine(0, 0, 0, getHeight());
        g.setColor(rightColor);
        g.drawLine(1, 0, 1, getHeight());
    }
}