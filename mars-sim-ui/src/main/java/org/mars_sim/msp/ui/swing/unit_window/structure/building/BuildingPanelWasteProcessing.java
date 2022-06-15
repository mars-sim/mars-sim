/*
 * Mars Simulation Project
 * BuildingPanelWasteProcessing.java
 * @date 2022-06-15
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.WasteProcess;
import org.mars_sim.msp.core.structure.building.function.WasteProcessing;
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
 * The BuildingPanelWasteProcessing class is a building function panel representing
 * the waste processes of a building.
 */
@SuppressWarnings("serial")
public class BuildingPanelWasteProcessing
extends BuildingFunctionPanel {

	// Data members
	private WasteProcessing processor;

	/**
	 * Constructor.
	 * @param processor the waste processing building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelWasteProcessing(WasteProcessing processor, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(Msg.getString("BuildingPanelWasteProcessing.tabTitle"), Msg.getString("BuildingPanelWasteProcessing.title"),
			  processor.getBuilding(), desktop);

		// Initialize variables.
		this.processor = processor;
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {

		WebLabel supportedProcessesLabel = new WebLabel(Msg.getString("BuildingPanelWasteProcessing.supportedProcesses"), WebLabel.CENTER);
		supportedProcessesLabel.setPadding(10, 5, 5, 5);
		center.add(supportedProcessesLabel, BorderLayout.NORTH);

		// Get all processes at building.
		List<WasteProcess> processes = processor.getProcesses();
		int size = processes.size();

		WebTextArea processesTA = new WebTextArea();
		processesTA.setEditable(false);
		processesTA.setFont(new Font("SansSerif", Font.ITALIC, 12));
		processesTA.setColumns(12);

		// For each specialty, add specialty name panel.
		for (WasteProcess p : processes) {
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
