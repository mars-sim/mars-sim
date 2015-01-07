/**
 * Mars Simulation Project
 * BuildingInfoPanel.java
 * @version 3.07 2015-01-01
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.*;


@SuppressWarnings("serial")
public class BuildingInfoPanel extends JPanel {// JDialog {

	private String buildingName;
	private String text;

    public BuildingInfoPanel() {
		super();
    	setOpaque(false);
    	setBackground(new Color(51,25,0,150));
    }
    
    @Override
	protected void paintComponent(Graphics g) {
        int x = 20;
        int y = 20;
        int w = getWidth() - 40;
        int h = getHeight() - 40;
        int arc = 15;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        //g2.setColor(new Color(0, 0, 0, 220));
        g2.setColor(new Color(51,25,0,128));
        g2.fillRoundRect(x, y, w, h, arc, arc);

        g2.setStroke(new BasicStroke(3f));
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(x, y, w, h, arc, arc); 

        g2.dispose();
    }
    
    public void init(String buildingName, String text) {
		
    	this.buildingName = buildingName;
    	this.text = text;
    	
        this.setLayout(new BorderLayout(20, 10));
    	//this.setSize(350, 400); // undecorated 301, 348 ; decorated : 303, 373

        JPanel mainPanel = new JPanel(new FlowLayout());//new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBackground(new Color(0,0,0,128));
        //setMinimumSize()
        this.add(mainPanel, BorderLayout.NORTH);
        
        JPanel westPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));//new BorderLayout());
        westPanel.setOpaque(false);
        westPanel.setBackground(new Color(0,0,0,128));
        //setMinimumSize()
        this.add(westPanel, BorderLayout.WEST);
        
        JPanel eastPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));//new BorderLayout());
        eastPanel.setOpaque(false);
        eastPanel.setBackground(new Color(0,0,0,128));
        //setMinimumSize()
        this.add(eastPanel, BorderLayout.EAST);
        
        // Creating the text Input
        JTextField tf1 = new JTextField("", 15);
        tf1.setHorizontalAlignment(JTextField.CENTER);
        tf1.setOpaque(false);
        tf1.setFocusable(false);
        tf1.setBackground(new Color(0,0,0,200));
        tf1.setColumns(20);
        tf1.setText(buildingName);
        tf1.setForeground(Color.YELLOW); // orange font
        tf1.setFont( new Font("Arial", Font.BOLD, 14 ) );
        
        mainPanel.add(tf1);

        JTextArea ta = new JTextArea();//290, 300);
        String header = "DESCRIPTION: "; 

        ta.setLineWrap(true);
        ta.setFocusable(false);
        ta.setWrapStyleWord(true);
        ta.setText(header+ "\n");
        ta.append(text);
        ta.setEditable(false);
        ta.setForeground(Color.ORANGE); // orange font
        ta.setFont( new Font( "Dialog", Font.PLAIN, 14 ) );
        ta.setOpaque(false);
        ta.setBackground(new Color(0, 0, 0, 0));
 
        CustomScroll scr = new CustomScroll(ta);
        //scr.setPreferredSize(new Dimension(250, 350));
        //scr.setMaximumSize(new Dimension(250, 350));
        //scr.setBounds(50, 20, 250, 300);
        this.add(scr, BorderLayout.CENTER);
        //JPanel scrPanel = new JPanel(new FlowLayout());
        //scrPanel
        //scrPanel.setOpaque(false);
        //scrPanel.setBackground(new Color(0,0,0,0));
        
        //scrPanel.add(scr);
        //this.add(scrPanel, BorderLayout.CENTER);
        this.setVisible(true);

    }

}