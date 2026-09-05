/**
 * Mars Simulation Project
 * UnitInfoPanel.java
 * @date 2023-11-06
 * @author Manny Kung
 */

package com.mars_sim.ui.swing.tool.settlement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.ui.swing.tool.LineBreakPanel;


@SuppressWarnings("serial")
class UnitInfoPanel extends JPanel {

	record UnitSummary(String type, LocalPosition pos, String description) {} 

	public static final int MARGIN_WIDTH = 2;
	public static final int MARGIN_HEIGHT = 2;
		
	public UnitInfoPanel(String unitName, UnitSummary summary) {
		super();

		setOpaque(false);
        setBackground(new Color(51, 25, 0, 128));
        
		setLayout(new BorderLayout(1, 1));
		setSize(PopUpUnitMenu.WIDTH_1 - 10, PopUpUnitMenu.HEIGHT_1 - 10); 
		
    	List<String> list = new ArrayList<>();
    	list.add("Name: ");
    	list.add(unitName);
    	list.add(" \n");
    	list.add("Type: ");
    	list.add(summary.type());
    	list.add(" \n");
    	list.add("Local Position: ");
    	list.add(summary.pos().getShortFormat());
    	list.add(" \n");
    	list.add("Descripion: ");
    	list.add(summary.description());
    	list.add(" \n");
    	
    	LineBreakPanel lineBreakPanel = new LineBreakPanel(list);
        add(lineBreakPanel, BorderLayout.CENTER);

		
		setVisible(true);

	}
}