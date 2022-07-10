/*
 * Mars Simulation Project
 * BuildingPanelResourceProcessing.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.text.WebTextArea;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.JPanel;

/**
 * The BuildingPanelResourceProcessing class is a building function panel representing
 * the resource processes of a building.
 */
@SuppressWarnings("serial")
public class BuildingPanelResourceProcessing extends BuildingFunctionPanel {

	private static final String CHEMICAL_ICON = Msg.getString("icon.chemical"); //$NON-NLS-1$
	
	// Data members
	private ResourceProcessing processor;

	/**
	 * Constructor.
	 * @param processor the resource processing building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelResourceProcessing(ResourceProcessing processor, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelResourceProcessing.title"),
			ImageLoader.getNewIcon(CHEMICAL_ICON),
			processor.getBuilding(), 
			desktop
		);

		// Initialize variables.
		this.processor = processor;
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {

		WebLabel supportedProcessesLabel = new WebLabel(Msg.getString("BuildingPanelResourceProcessing.supportedProcesses"), WebLabel.CENTER);
		supportedProcessesLabel.setPadding(10, 5, 5, 5);
		center.add(supportedProcessesLabel, BorderLayout.NORTH);

		// Get all processes at building.
		List<ResourceProcess> processes = processor.getProcesses();
		int size = processes.size();

		WebTextArea processesTA = new WebTextArea();
		processesTA.setEditable(false);
		processesTA.setFont(new Font("SansSerif", Font.ITALIC, 12));
		processesTA.setColumns(12);

		// For each specialty, add specialty name panel.
		for (ResourceProcess p : processes) {
			processesTA.append(" " + p.getProcessName()+ " ");
			if (!p.equals(processes.get(size-1)))
				//if it's NOT the last one
				processesTA.append("\n");
		}

		WebPanel listPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		listPanel.add(processesTA);
		processesTA.setBorder(new MarsPanelBorder());
		center.add(listPanel, BorderLayout.CENTER);

	}

}
