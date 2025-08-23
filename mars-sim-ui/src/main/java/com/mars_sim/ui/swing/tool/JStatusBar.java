/*
 * Mars Simulation Project
 * JStatusBar.java
 * @date 2023-05-14
 * Modified by Manny Kung
 */

package com.mars_sim.ui.swing.tool;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class JStatusBar extends TexturedPanel {
		   
    private static final Color almond = new Color(239,222,205,128);

    private static final int MARGIN = 1;

    private int barHeight = 25;
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
    	
    	createUI();	
    }
    	 
    protected void createUI() {    
    	
		setOpaque(false);
		setBackground(almond);
		
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(getWidth(), barHeight + MARGIN + 1));
    
        leftPanel = new JPanel(new BorderLayout());
//        leftPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));//BorderFactory.createEmptyBorder(0, 0, 0, 0));
        leftPanel.setAlignmentY(CENTER_ALIGNMENT);
        leftPanel.setAlignmentX(LEFT_ALIGNMENT);
        add(leftPanel, BorderLayout.WEST);
        
        centerPanel = new JPanel(new BorderLayout());
//        centerPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        centerPanel.setAlignmentY(CENTER_ALIGNMENT);
        centerPanel.setAlignmentX(CENTER_ALIGNMENT);
        add(centerPanel, BorderLayout.CENTER);
        
        rightPanel = new JPanel(new BorderLayout());
//        rightPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        rightPanel.setAlignmentY(CENTER_ALIGNMENT);
        rightPanel.setAlignmentX(RIGHT_ALIGNMENT);
        add(rightPanel, BorderLayout.EAST);
    }
    
    public void addFullBarComponent(JPanel panel, boolean separator) {
    	JPanel fpanel = new JPanel(new BorderLayout());
    	fpanel.setAlignmentY(CENTER_ALIGNMENT);
        if (separator) {
//            addBorder(panel);
            fpanel.add(new SeparatorPanel(Color.GRAY, Color.WHITE));
        }
        fpanel.add(panel, BorderLayout.CENTER);
        centerPanel.add(fpanel, BorderLayout.CENTER);
    }
    
    public void addLeftComponent(JComponent component, boolean separator) {
    	JPanel panel = new JPanel(new FlowLayout(
                FlowLayout.LEFT, 0, leftPadding));
    	panel.setAlignmentY(CENTER_ALIGNMENT);
        if (separator) {
//            addBorder(panel);
        	add(new SeparatorPanel(Color.GRAY, Color.WHITE));
        }
        panel.add(component, BorderLayout.WEST);
        leftPanel.add(panel, BorderLayout.WEST);
    }
    
    public void addCenterComponent(JComponent component, boolean separator) {
    	JPanel panel = new JPanel(new FlowLayout(
                FlowLayout.CENTER, 0, leftPadding));
    	panel.setAlignmentY(CENTER_ALIGNMENT);
        if (separator) {
//            addBorder(panel);
        	add(new SeparatorPanel(Color.GRAY, Color.WHITE));
        }
        panel.add(component, BorderLayout.CENTER);
        centerPanel.add(panel, BorderLayout.CENTER);
    }
    
    
    public void addRightComponent(JComponent component, boolean separator) {
        JPanel panel = new JPanel(new FlowLayout(
                FlowLayout.RIGHT, 0, rightPadding));
        panel.setAlignmentY(CENTER_ALIGNMENT);
        if (separator) {
//            addBorder(panel);
        	add(new SeparatorPanel(Color.GRAY, Color.WHITE));
        }
        panel.add(component, BorderLayout.EAST);
        rightPanel.add(panel, BorderLayout.EAST);
    }
    
//    private void addBorder(JPanel panel) {
//        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
//                                                        BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN)));
//    }

    public void addRightCorner() {
        JPanel panel = new JPanel(new FlowLayout(
                FlowLayout.TRAILING, 0, 0));
        JLabel label = new JLabel(new AngledLinesWindowsCornerIcon());
        panel.setAlignmentX(1F);
        panel.setAlignmentY(0);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setVerticalAlignment(SwingConstants.BOTTOM);
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

	    g2.dispose();
    }
}
