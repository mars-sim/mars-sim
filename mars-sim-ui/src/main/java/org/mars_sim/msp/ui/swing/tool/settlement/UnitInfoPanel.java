/**
 * Mars Simulation Project
 * UnitInfoPanel.java
 * @version 3.1.0 2017-08-30
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

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.CustomScroll;

@SuppressWarnings("serial")
public class UnitInfoPanel extends JPanel {// JDialog {

	public static final int MARGIN_WIDTH = 20;
	public static final int MARGIN_HEIGHT = 10;
	
	public UnitInfoPanel(MainDesktopPane desktop) {
		super();
//		setOpaque(false);
		setBackground(new Color(51, 25, 0, 150));
	}

	@Override
	protected void paintComponent(Graphics g) {

		int x = 20;
		int y = 20;
		int w = getWidth() - MARGIN_WIDTH * 2;
		int h = getHeight() - MARGIN_HEIGHT * 2;
		int arc = 15;

		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(new Color(51, 25, 0, 128));
		g2.fillRoundRect(x, y, w, h, arc, arc);
		g2.setStroke(new BasicStroke(3f));
		g2.setColor(Color.lightGray);
		g2.drawRoundRect(x, y, w, h, arc, arc);
		g2.dispose();
	}

	public void init(String unitName, String unitType, String unitDescription) {

		setOpaque(false);
		setLayout(new BorderLayout(10, 20));
		// this.setSize(350, 400); // undecorated 301, 348 ; decorated : 303, 373

		JPanel mainPanel = new JPanel(new FlowLayout());// new BorderLayout());
		mainPanel.setOpaque(false);
		mainPanel.setBackground(new Color(0, 0, 0, 128));
		// setMinimumSize()
		this.add(mainPanel, BorderLayout.NORTH);

		JPanel westPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));// new BorderLayout());
		westPanel.setOpaque(false);
		westPanel.setBackground(new Color(0, 0, 0, 128));
		// setMinimumSize()
		this.add(westPanel, BorderLayout.WEST);

		JPanel eastPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));// new BorderLayout());
		eastPanel.setOpaque(false);
		eastPanel.setBackground(new Color(0, 0, 0, 128));
		// setMinimumSize()
		this.add(eastPanel, BorderLayout.EAST);

		// Creating the text Input
		JTextField tf1 = new JTextField("", 15);

		tf1.setHorizontalAlignment(JTextField.CENTER);
		tf1.setOpaque(false);
		tf1.setFocusable(false);
		tf1.setBackground(new Color(92, 83, 55, 128));
		tf1.setColumns(20);
		Border border = BorderFactory.createLineBorder(Color.gray, 2);
		tf1.setBorder(border);
		tf1.setText(unitName);
		tf1.setForeground(Color.BLACK);
		tf1.setFont(new Font("Arial", Font.BOLD, 14));

		mainPanel.add(tf1);

		JTextArea ta = new JTextArea();
		String type = "TYPE: ";
		String description = "DESCRIPTION: ";

		ta.setLineWrap(true);
		ta.setFocusable(false);
		ta.setWrapStyleWord(true);
		ta.setText(type + "\n");
		ta.append(unitType + "\n\n");
		ta.append(description + "\n");
		ta.append(unitDescription);
		ta.setCaretPosition(0);
		ta.setEditable(false);
		ta.setForeground(Color.black); 
		ta.setFont(new Font("Dialog", Font.PLAIN, 14));
		ta.setOpaque(false);
		ta.setBackground(new Color(92, 83, 55, 128));

		CustomScroll scr = new CustomScroll(ta);
		scr.setSize(PopUpUnitMenu.D_WIDTH - 50 , PopUpUnitMenu.D_HEIGHT);
		add(scr, BorderLayout.CENTER);

		JPanel southPanel = new JPanel();
		add(southPanel, BorderLayout.SOUTH);
		southPanel.setOpaque(false);
		southPanel.setBackground(new Color(0, 0, 0, 128));
		
		setVisible(true);

	}

}