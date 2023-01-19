/**
 * Mars Simulation Project
 * UnitInfoPanel.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.mars_sim.msp.ui.swing.ComponentMover;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.LineBreakPanel;


@SuppressWarnings("serial")
public class UnitInfoPanel extends JPanel {

	public static final int MARGIN_WIDTH = 2;
	public static final int MARGIN_HEIGHT = 2;
	
	private MainDesktopPane desktop;
	
	public UnitInfoPanel(MainDesktopPane desktop) {
		super();
		this.desktop = desktop;
		setOpaque(false);
		setBackground(new Color(0, 0, 0, 128));
    }

	public void init(String unitName, String unitType, String unitDescription) {

		setOpaque(false);
        setBackground(new Color(51, 25, 0, 128));
        
		setLayout(new BorderLayout(1, 1));
		setSize(PopUpUnitMenu.WIDTH_1 - 10, PopUpUnitMenu.HEIGHT_1 - 10); 
		
		String type = "Type: ";
		String description = "Descripion: ";

		
    	List<String> list = new ArrayList<>();
    	list.add(unitName);
    	list.add(" \n");
    	list.add(type);
    	list.add(unitType);
    	list.add(" \n");
    	list.add(description);
    	list.add(unitDescription);
    	list.add(" \n");
    	
    	LineBreakPanel lineBreakPanel = new LineBreakPanel(list);
        add(lineBreakPanel, BorderLayout.CENTER);

		
		setVisible(true);

        // Make panel drag-able
	    ComponentMover mover = new ComponentMover(this, desktop);
	    mover.registerComponent(this);	
	}

}
