/*
 * Mars Simulation Project
 * JStatusBar.java
 * @date 2023-05-14
 * Modified by Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class JStatusBar extends TexturedPanel {
		   
        private static final Color almond = new Color(239,222,205,128);

        private static final int MARGIN = 1;

        private int barHeight = 30;
        private int leftPadding;
        private int rightPadding;
	
    protected JPanel leftPanel;
    protected JPanel rightPanel;
    protected JPanel centerPanel;
    	 
    public JStatusBar(int leftPadding, int rightPadding, int barHeight) { 
    	if (barHeight != 0) 
    		this.barHeight = barHeight;
    	if (leftPadding != 0)
    		leftPadding = 1;
    	if (rightPadding != 0)
    		rightPadding = 1;
    	this.leftPadding = leftPadding;
    	this.rightPadding = rightPadding;
    	
    	createPartControl();
    }
    	 
    protected void createPartControl() {    
    	
		setOpaque(false);
		setBackground(almond);
		
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(getWidth(), barHeight + (2 * (MARGIN + 2))));
    
        leftPanel = new JPanel(new FlowLayout(
                FlowLayout.CENTER, 3, 1));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        add(leftPanel, BorderLayout.WEST);
        
        centerPanel = new JPanel(new FlowLayout(
                FlowLayout.CENTER, 3, 1));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        add(centerPanel, BorderLayout.CENTER);
        
        rightPanel = new JPanel(new FlowLayout(
                FlowLayout.CENTER, 3, 1));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        add(rightPanel, BorderLayout.EAST);
    }
    
    public void addLeftComponent(JComponent component, boolean separator) {
    	JPanel panel = new JPanel(new FlowLayout(
                FlowLayout.LEFT, 0, leftPadding));
        if (separator) {
            addBorder(panel);
        	panel.add(new SeparatorPanel(Color.GRAY, Color.WHITE));
        }
        panel.add(component);
        leftPanel.add(panel);
    }
    
    public void addCenterComponent(JComponent component, boolean separator) {
    	JPanel panel = new JPanel(new FlowLayout(
                FlowLayout.CENTER, 0, leftPadding));
        if (separator) {
            addBorder(panel);
        	panel.add(new SeparatorPanel(Color.GRAY, Color.WHITE));
        }
        panel.add(component);
        centerPanel.add(panel);
    }
    
    
    public void addRightComponent(JComponent component, boolean separator) {
        JPanel panel = new JPanel(new FlowLayout(
                FlowLayout.RIGHT, 0, rightPadding));
        if (separator) {
            addBorder(panel);
        	panel.add(new SeparatorPanel(Color.GRAY, Color.WHITE));
        }
        panel.add(component);
        rightPanel.add(panel);
    }
    
    private void addBorder(JPanel panel) {
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                                                        BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN)));
    }

    public void addRightCorner() {
        JPanel panel = new JPanel(new FlowLayout(
                FlowLayout.TRAILING, 0, 0));
        JLabel label = new JLabel(new AngledLinesWindowsCornerIcon());
        panel.setAlignmentX(1F);
        panel.setAlignmentY(0);
        label.setHorizontalAlignment(JLabel.RIGHT);
        label.setVerticalAlignment(JLabel.BOTTOM);
        panel.add(label);
        rightPanel.add(panel);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int y = 0;
        g.setColor(new Color(156, 154, 140));
        g.drawLine(0, y, getWidth(), y);
        y++;
        g.setColor(new Color(196, 194, 183));
        g.drawLine(0, y, getWidth(), y);
        y++;
        g.setColor(new Color(218, 215, 201));
        g.drawLine(0, y, getWidth(), y);
        y++;
        g.setColor(new Color(233, 231, 217));
        g.drawLine(0, y, getWidth(), y);

        y = getHeight() - 3;
        g.setColor(new Color(233, 232, 218));
        g.drawLine(0, y, getWidth(), y);
       
        y++;
        g.setColor(new Color(233, 231, 216));
        g.drawLine(0, y, getWidth(), y);
       
        y = getHeight() - 1;
        g.setColor(new Color(221, 221, 220));
        g.drawLine(0, y, getWidth(), y);

	    // Create the 2D copy
	    Graphics2D g2 = (Graphics2D)g.create();

	    // Apply vertical gradient
	    g2.setPaint(almond);
	    g2.fillRect(0, 0, getWidth(), getHeight());

    }
}
