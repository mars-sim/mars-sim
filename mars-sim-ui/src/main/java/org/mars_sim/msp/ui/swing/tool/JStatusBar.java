/**
 * Mars Simulation Project
 * JStatusBar.java
 * @version 3.1.0 2019-09-20
 * Modified by Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool;

//import com.jgoodies.forms.layout.CellConstraints;
//import com.jgoodies.forms.layout.FormLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class JStatusBar extends TexturedPanel {
	
    private static final long serialVersionUID = 1L;
    	   

	private static final Color antiqueBronze = new Color(102,93,30,128);
	private static final Color almond = new Color(239,222,205,128);
	private static final Color cafeNoir = new Color(75,54,33,128);
	
	public int height = 25;
	private int leftPadding;
	private int rightPadding;
	
    protected JPanel leftPanel;
    protected JPanel rightPanel;
    protected JPanel centerPanel;
    	 
    public JStatusBar(int leftPadding, int rightPadding, int barHeight) { 
    	if (barHeight != 0) 
    		height = barHeight;
    	if (leftPadding != 0)
    		leftPadding = 1;
    	if (rightPadding != 0)
    		rightPadding = 1;
    	this.leftPadding = leftPadding;
    	this.rightPadding = rightPadding;
    	
    	createPartControl();
    	//setOpaque(false);
    	//setBackground(new Color(0,0,0,128));
    }
    	 
    protected void createPartControl() {    
    	
		setOpaque(false);
		setBackground(almond);
		
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(getWidth(), height));
 
        leftPanel = new JPanel(new FlowLayout(
                FlowLayout.LEADING, 3, 0));
//        leftPanel.setAlignmentX(.5F);
//        leftPanel.setAlignmentY(.5F);
//        leftPanel.setOpaque(false);
//		leftPanel.setBackground(new Color(0,0,0,128));
        add(leftPanel, BorderLayout.WEST);
        
        centerPanel = new JPanel(new FlowLayout(
                FlowLayout.CENTER, 3, 0));
//        centerPanel.setAlignmentX(.5F);
//        centerPanel.setAlignmentY(.5F);
//        centerPanel.setOpaque(false);
//        centerPanel.setBackground(new Color(0,0,0,128));
        add(centerPanel, BorderLayout.CENTER);
        
        rightPanel = new JPanel(new FlowLayout(
                FlowLayout.TRAILING, 3, 0));
//        rightPanel.setAlignmentX(.5F);
//        rightPanel.setAlignmentY(.5F);
//        rightPanel.setOpaque(false);
//		rightPanel.setBackground(new Color(0,0,0,128));
        add(rightPanel, BorderLayout.EAST);
        
//        JLabel label = new JLabel(new AngledLinesWindowsCornerIcon());
//        label.setAlignmentX(1F);
//        label.setAlignmentY(1F);
//        add(label);
        
//        leftPanel.setOpaque(false);
//        leftPanel.setBackground(almond);
//        
//        centerPanel.setOpaque(false);
//        centerPanel.setBackground(almond);
//        
//        rightPanel.setOpaque(false);
//        rightPanel.setBackground(almond);
    }
    
    public void addLeftComponent(JComponent component, boolean separator) {
    	JPanel panel = new JPanel(new FlowLayout(
                FlowLayout.LEADING, 0, leftPadding));
//        panel.setOpaque(false);
//        panel.setBackground(almond);
        if (separator) 
        	panel.add(new SeparatorPanel(Color.GRAY, Color.WHITE));
        panel.add(component);
        leftPanel.add(panel);
    }
    
    public void addCenterComponent(JComponent component, boolean separator) {
    	JPanel panel = new JPanel(new FlowLayout(
                FlowLayout.LEADING, 0, leftPadding));
//      panel.setOpaque(false);
//      panel.setBackground(almond);
        if (separator) 
        	panel.add(new SeparatorPanel(Color.GRAY, Color.WHITE));
        panel.add(component);
        centerPanel.add(panel);
    }
    
    
    public void addRightComponent(JComponent component, boolean separator) {
        JPanel panel = new JPanel(new FlowLayout(
                FlowLayout.LEADING, 0, rightPadding));
//        panel.setOpaque(false);
//        panel.setBackground(almond);
        if (separator) 
        	panel.add(new SeparatorPanel(Color.GRAY, Color.WHITE));
        panel.add(component);
        rightPanel.add(panel);
    }
    
    public void addRightCorner() {
        JPanel panel = new JPanel(new FlowLayout(
                FlowLayout.TRAILING, 0, 0));
//        panel.setOpaque(false);
//        panel.setBackground(almond);
        JLabel label = new JLabel(new AngledLinesWindowsCornerIcon());
//        label.setAlignmentX(1F);
//        label.setAlignmentY(1F);
        panel.setAlignmentX(1F);
        panel.setAlignmentY(1F);
        label.setHorizontalAlignment(JLabel.RIGHT);
        label.setVerticalAlignment(JLabel.BOTTOM);
        panel.add(label);
        rightPanel.add(panel);
//        rightPanel.add(label);
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
