/**
 * Mars Simulation Project
 * BuildingPanelResourceProcessing.java
 * @version 3.1.0 2017-09-15
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.text.WebTextArea;

import javax.swing.ImageIcon;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The ResourceProcessingBuildingPanel class is a building function panel representing
 * the resource processes of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelResourceProcessing
extends BuildingFunctionPanel {

	// Data members
	private ResourceProcessing processor;
//	private List<WebLabel> processLabels;
//	private ImageIcon greenDot;
//	private ImageIcon redDot;

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
		WebLabel resourceProcessesLabel = new WebLabel(Msg.getString("BuildingPanelResourceProcessing.title"), WebLabel.CENTER);
		resourceProcessesLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//resourceProcessesLabel.setForeground(new Color(102, 51, 0)); // dark brown
		add(resourceProcessesLabel, BorderLayout.NORTH);

		WebLabel supportedProcessesLabel = new WebLabel(Msg.getString("BuildingPanelResourceProcessing.supportedProcesses"), WebLabel.CENTER);
		add(supportedProcessesLabel, BorderLayout.CENTER);

		// Get all processes at building.
		List<ResourceProcess> processes = processor.getProcesses();
		int size = processes.size();
		// Prepare resource processes list panel.
//		WebPanel resourceProcessesListPanel = new WebPanel(new GridLayout(processes.size(), 2, 10, 3));
//		resourceProcessesListPanel.setBorder(new EmptyBorder(3, 20, 3, 20)); //(int top, int left, int bottom, int right)
		//resourceProcessesListPanel.setOpaque(false);
		//resourceProcessesListPanel.setBackground(new Color(0,0,0,128));

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
			
//			if (p.isProcessRunning()) {
//				processLabel.setIcon(greenDot);
//				processLabel.setToolTipText(p.getProcessName() + " process is running.");
//			}
//			else {
//				processLabel.setIcon(redDot);
//				processLabel.setToolTipText(p.getProcessName() + " process is not running.");
//			}
			
		}

		WebPanel listPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		listPanel.add(processesTA);
		processesTA.setBorder(new MarsPanelBorder());
//		resourceProcessesListPanel.setBorder(new MarsPanelBorder());
//		listPanel.add(resourceProcessesListPanel);
		add(listPanel, BorderLayout.SOUTH);
		//listPanel.setOpaque(false);
		//listPanel.setBackground(new Color(0,0,0,128));

		// Load green and red dots.
//		greenDot = new ImageIcon("images/GreenDot.png", "Process is running.");
//		redDot = new ImageIcon("images/RedDot.png", "Process is not running");

		// For each resource process, add a label.
//		processLabels = new ArrayList<WebLabel>(processes.size());
//		Iterator<ResourceProcess> i = processes.iterator();
//		while (i.hasNext()) {
//			ResourceProcess process = i.next();
//			WebLabel processLabel = new WebLabel(process.getProcessName(), WebLabel.LEFT);
//			//processLabel.setForeground(Color.DARK_GRAY);
//			//processLabel.setBackground(Color.WHITE);
//			processLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
//
//			if (process.isProcessRunning()) {
//				processLabel.setIcon(greenDot);
//				processLabel.setToolTipText(process.getProcessName() + " process is running.");
//			}
//			else {
//				processLabel.setIcon(redDot);
//				processLabel.setToolTipText(process.getProcessName() + " process is not running.");
//			}
//
////			resourceProcessesListPanel.add(processLabel);
//			processLabels.add(processLabel);
//		}
	}

	/**
	 * Update this panel.
	 */
	public void update() {
//		List<ResourceProcess> processes = processor.getProcesses();
//		for (int x=0; x < processes.size(); x++) {
//			ResourceProcess process = processes.get(x);
//			WebLabel processLabel = processLabels.get(x);
//			if (process.isProcessRunning()) {
//				processLabel.setIcon(greenDot);
//				processLabel.setToolTipText(process.getProcessName() + " process is running.");
//			}
//			else {
//				processLabel.setIcon(redDot);
//				processLabel.setToolTipText(process.getProcessName() + " process is not running.");
//			}
//		}
	}
}