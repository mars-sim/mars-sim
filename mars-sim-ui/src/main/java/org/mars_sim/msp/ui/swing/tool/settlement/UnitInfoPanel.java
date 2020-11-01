/**
 * Mars Simulation Project
 * UnitInfoPanel.java
 * @version 3.1.2 2020-09-02
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
import javax.swing.border.Border;

import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.CustomScroll;

import com.alee.laf.panel.WebPanel;
import com.alee.laf.text.WebTextArea;
import com.alee.laf.text.WebTextField;

@SuppressWarnings("serial")
public class UnitInfoPanel extends WebPanel {

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

		WebPanel mainPanel = new WebPanel(new FlowLayout());// new BorderLayout());
		mainPanel.setOpaque(false);
		mainPanel.setBackground(new Color(0, 0, 0, 128));
		// setMinimumSize()
		this.add(mainPanel, BorderLayout.NORTH);

		WebPanel westPanel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));// new BorderLayout());
		westPanel.setOpaque(false);
		westPanel.setBackground(new Color(0, 0, 0, 128));
		// setMinimumSize()
		this.add(westPanel, BorderLayout.WEST);

		WebPanel eastPanel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));// new BorderLayout());
		eastPanel.setOpaque(false);
		eastPanel.setBackground(new Color(0, 0, 0, 128));
		// setMinimumSize()
		this.add(eastPanel, BorderLayout.EAST);

		// Creating the text Input
		WebTextField tf1 = new WebTextField("", 15);

		tf1.setHorizontalAlignment(WebTextField.CENTER);
		tf1.setOpaque(false);
		tf1.setFocusable(false);
//		tf1.setBackground(new Color(92, 83, 55, 128));
		tf1.setBackground(new Color(0, 0, 0, 128));
		tf1.setColumns(20);
		Border border = BorderFactory.createLineBorder(Color.gray, 2);
		tf1.setBorder(border);
		tf1.setText(unitName);
		tf1.setForeground(Color.yellow.brighter());
		tf1.setFont(new Font("Arial", Font.BOLD, 14));

		mainPanel.add(tf1);

		WebTextArea ta = new WebTextArea();
		String type = "Building Type: ";
		String description = "Descripion: ";

		ta.setLineWrap(true);
		ta.setFocusable(false);
		ta.setWrapStyleWord(true);
		ta.setText(type + "\n");
		ta.append(unitType + "\n\n");
		ta.append(description + "\n");
		ta.append(unitDescription);
		ta.setCaretPosition(0);
		ta.setEditable(false);
		ta.setForeground(Color.WHITE); 
		ta.setFont(new Font("Dialog", Font.PLAIN, 14));
		ta.setOpaque(false);
//		ta.setBackground(new Color(92, 83, 55, 128));
		ta.setBackground(new Color(0,0,0,128));
		
		CustomScroll scr = new CustomScroll(ta);
		scr.setSize(PopUpUnitMenu.WIDTH_0 - 10, PopUpUnitMenu.HEIGHT_2 - 10);
		add(scr, BorderLayout.CENTER);

		WebPanel southPanel = new WebPanel();
		add(southPanel, BorderLayout.SOUTH);
		southPanel.setOpaque(false);
		southPanel.setBackground(new Color(0, 0, 0, 128));
		
		setVisible(true);

	}

}
