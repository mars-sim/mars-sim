/**
 * Mars Simulation Project
 * BuildingPanelGeneral.java
 * @date 2021-10-07
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;


/**
 * The BuildingPanelGeneral class is a building function panel showing
 * the general status of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelGeneral
extends BuildingFunctionPanel {

	private Building building;

	/**
	 * Constructor.
	 * @param The panel for the Fishery
	 * @param The main desktop
	 */
	public BuildingPanelGeneral(Building building, MainDesktopPane desktop) {
		super(building, desktop);

		this.building = building;
		
		setLayout(new BorderLayout());
			
		WebLabel titleLabel = new WebLabel("General", WebLabel.CENTER);		
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		add(titleLabel, BorderLayout.NORTH);
		
		// Prepare spring layout info panel.
		JPanel infoPanel = new JPanel(new SpringLayout());
//		infoPanel.setBorder(new MarsPanelBorder());
		add(infoPanel, BorderLayout.CENTER);
		
		// Prepare dimension label
		JLabel dimLabel = new JLabel("Dimension: ", JLabel.RIGHT); //$NON-NLS-1$
		//dimLabel.setSize(2, 2);
		infoPanel.add(dimLabel);

		// Prepare dimension TF
		WebPanel wrapper0 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		JTextField dimTF = new JTextField();
		dimTF.setText(building.getLength() + " x " + building.getWidth() + " x 2.5"); 
		dimTF.setEditable(false);
		wrapper0.setPreferredSize(new Dimension(150, 24));
//		dimTF.setCaretPosition(0);
		TooltipManager.setTooltip (dimTF, 
				"Length[m] x Width[m] x Height[m]",
				TooltipWay.down);
		wrapper0.add(dimTF);
		infoPanel.add(wrapper0);
		
		// Prepare mass label
		JLabel massLabel = new JLabel("Base Mass: ", JLabel.RIGHT); //$NON-NLS-1$
		//massLabel.setSize(2, 2);
		infoPanel.add(massLabel);

		// Prepare mass TF
		WebPanel wrapper1 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		JTextField massTF = new JTextField();
		massTF.setText(building.getBaseMass() + " kg"); 
		massTF.setEditable(false);
		wrapper1.setPreferredSize(new Dimension(150, 24));
//		massTF.setCaretPosition(0);
		TooltipManager.setTooltip (massTF, 
				"The base mass of this building",
				TooltipWay.down);
		wrapper1.add(massTF);
		infoPanel.add(wrapper1);
		
		// Prepare SpringLayout
		SpringUtilities.makeCompactGrid(infoPanel, 2, 2, // rows, cols
				80, 1, // initX, initY
				5, 5); // xPad, yPad
	}

	/**
	 * Update this panel with latest values
	 */
	public void update() {
	}
}
