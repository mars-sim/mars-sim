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

public class JStatusBar extends JPanel {
	
    private static final long serialVersionUID = 1L;
    	   
	public static final int HEIGHT = 19;
	
	private static final Color antiqueBronze = new Color(102,93,30,128);
	private static final Color almond = new Color(239,222,205,128);
	private static final Color cafeNoir = new Color(75,54,33,128);
	
	public JPanel contentPanel ;
	//public FormLayout layout;
    protected JPanel leftPanel;
    protected JPanel rightPanel;
    	 
    public JStatusBar() {
    	createPartControl();
    	//setOpaque(false);
    	//setBackground(new Color(0,0,0,128));
    }
    	 
    protected void createPartControl() {    

		setOpaque(false);
		setBackground(almond);
		
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(getWidth(), HEIGHT));
 
        leftPanel = new JPanel(new FlowLayout(
                FlowLayout.LEADING, 3, 3));
        //leftPanel.setOpaque(false);
		//leftPanel.setBackground(new Color(0,0,0,128));
        add(leftPanel, BorderLayout.WEST);
        
        rightPanel = new JPanel(new FlowLayout(
                FlowLayout.TRAILING, 3, 3));
        //rightPanel.setOpaque(false);
		//rightPanel.setBackground(new Color(0,0,0,128));
        add(rightPanel, BorderLayout.EAST);
        
        leftPanel.setOpaque(false);
        leftPanel.setBackground(almond);
        
        rightPanel.setOpaque(false);
        rightPanel.setBackground(almond);
    }

    
    public void addRightComponent(JComponent component, boolean separator, boolean cornerIcon) {
        JPanel panel = new JPanel(new FlowLayout(
                FlowLayout.LEADING, 0, 0));
        if (separator) 
        	panel.add(new SeparatorPanel(Color.GRAY, Color.WHITE));
        panel.add(component);
        if (cornerIcon) 
        	panel.add(new JLabel(new AngledLinesWindowsCornerIcon()), true);
        rightPanel.add(panel);
    }
    
    public void setLeftComponent(JComponent component, boolean separator) {
        leftPanel.add(component);
        if (separator) 
        	leftPanel.add(new SeparatorPanel(Color.GRAY, Color.WHITE));
        
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
