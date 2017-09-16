/**
 * Mars Simulation Project
 * BuildingPanelResourceProcessing.java
 * @version 3.1.0 2017-09-15
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The ResourceProcessingBuildingPanel class is a building function panel representing
 * the resource processes of a settlement building.
 */
public class BuildingPanelResourceProcessing
extends BuildingFunctionPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private ResourceProcessing processor;
	private List<JLabel> processLabels;
	private ImageIcon greenDot;
	private ImageIcon redDot;

	/**
	 * Constructor.
	 * @param processor the resource processing building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelResourceProcessing(ResourceProcessing processor, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(processor.getBuilding(), desktop);

		// Initialize variables.
		this.processor = processor;

		// Set layout
		setLayout(new BorderLayout());

		// Prepare resource processes label
		// 2014-11-21 Changed font type, size and color and label text
		// 2014-11-21 Added internationalization for labels
		JLabel resourceProcessesLabel = new JLabel(Msg.getString("BuildingPanelResourceProcessing.title"), JLabel.CENTER);
		resourceProcessesLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//resourceProcessesLabel.setForeground(new Color(102, 51, 0)); // dark brown
		add(resourceProcessesLabel, BorderLayout.NORTH);

		JLabel supportedProcessesLabel = new JLabel(Msg.getString("BuildingPanelResourceProcessing.supportedProcesses"), JLabel.CENTER);
		add(supportedProcessesLabel, BorderLayout.CENTER);

		// Get all processes at building.
		List<ResourceProcess> processes = processor.getProcesses();

		// Prepare resource processes list panel.
		JPanel resourceProcessesListPanel = new JPanel(new GridLayout(processes.size(), 2, 10, 3));
		resourceProcessesListPanel.setBorder(new EmptyBorder(3, 20, 3, 20)); //(int top, int left, int bottom, int right)
		//resourceProcessesListPanel.setOpaque(false);
		//resourceProcessesListPanel.setBackground(new Color(0,0,0,128));

		JPanel listPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		resourceProcessesListPanel.setBorder(new MarsPanelBorder());
		listPanel.add(resourceProcessesListPanel);
		add(listPanel, BorderLayout.SOUTH);
		//listPanel.setOpaque(false);
		//listPanel.setBackground(new Color(0,0,0,128));

		// Load green and red dots.
		greenDot = new ImageIcon("images/GreenDot.png", "Process is running.");
		redDot = new ImageIcon("images/RedDot.png", "Process is not running");

		// For each resource process, add a label.
		processLabels = new ArrayList<JLabel>(processes.size());
		Iterator<ResourceProcess> i = processes.iterator();
		while (i.hasNext()) {
			ResourceProcess process = i.next();
			JLabel processLabel = new JLabel(process.getProcessName(), JLabel.LEFT);
			//processLabel.setForeground(Color.DARK_GRAY);
			//processLabel.setBackground(Color.WHITE);
			processLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));

			if (process.isProcessRunning()) {
				processLabel.setIcon(greenDot);
				processLabel.setToolTipText(process.getProcessName() + " process is running.");
			}
			else {
				processLabel.setIcon(redDot);
				processLabel.setToolTipText(process.getProcessName() + " process is not running.");
			}

			resourceProcessesListPanel.add(processLabel);
			processLabels.add(processLabel);
		}
	}

	/**
	 * Update this panel.
	 */
	public void update() {
		List<ResourceProcess> processes = processor.getProcesses();
		for (int x=0; x < processes.size(); x++) {
			ResourceProcess process = processes.get(x);
			JLabel processLabel = processLabels.get(x);
			if (process.isProcessRunning()) {
				processLabel.setIcon(greenDot);
				processLabel.setToolTipText(process.getProcessName() + " process is running.");
			}
			else {
				processLabel.setIcon(redDot);
				processLabel.setToolTipText(process.getProcessName() + " process is not running.");
			}
		}
	}
}