/*
 * Mars Simulation Project
 * BuildingPanelManufacture.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.mars_sim.core.manufacture.ManufactureProcess;
import com.mars_sim.core.manufacture.SalvageProcess;
import com.mars_sim.core.structure.building.function.Manufacture;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.ProcessListPanel;

/**
 * A building panel displaying the manufacture building function.
 */
@SuppressWarnings("serial")
public class BuildingPanelManufacture extends BuildingFunctionPanel {

	private static final String MANU_ICON = "manufacture";
	
	/** Is UI constructed. */
	private boolean uiDone = false;

	/** The manufacture building. */
	private Manufacture workshop;
	/** Panel for displaying process panels. */
	private ProcessListPanel processListPane;
	/** The scroll panel for the process list. */
	private JScrollPane scrollPanel;

	private JLabel printersUsed;

	/**
	 * Constructor.
	 * 
	 * @param workshop the manufacturing building function.
	 * @param desktop  the main desktop.
	 */
	public BuildingPanelManufacture(Manufacture workshop, MainDesktopPane desktop) {
		// Use BuildingFunctionPanel constructor.
		super(
			Msg.getString("BuildingPanelManufacture.title"),
			ImageLoader.getIconByName(MANU_ICON), 
			workshop.getBuilding(), 
			desktop
		);

		// Initialize data model.
		this.workshop = workshop;
	}
	
	/**
	 * Builds the UI.
	 */
	@Override
	protected void buildUI(JPanel center) {

		// Prepare label panel
		AttributePanel labelPanel = new AttributePanel(3);
		center.add(labelPanel, BorderLayout.NORTH);

		// Prepare tech level label
		labelPanel.addTextField("Tech Level", Integer.toString(workshop.getTechLevel()), null);

		// Prepare processCapacity label
		labelPanel.addTextField("Process Capacity", Integer.toString(workshop.getMaxProcesses()), null);

		// Prepare processCapacity label
		printersUsed = labelPanel.addTextField("# of Printers In Use",
								Integer.toString(workshop.getNumPrintersInUse()), null);
			
		// Create scroll pane for manufacturing processes
		scrollPanel = new JScrollPane();
		scrollPanel.setPreferredSize(new Dimension(170, 90));
		center.add(scrollPanel, BorderLayout.CENTER);

		// Create process list main panel
		JPanel processListMainPane = new JPanel(new BorderLayout(0, 0));
		scrollPanel.setViewportView(processListMainPane);

		// Create process list panel
		processListPane = new ProcessListPanel(false);
		processListMainPane.add(processListPane, BorderLayout.NORTH);
		processListPane.update(workshop.getProcesses(), workshop.getSalvageProcesses());
	}

	/**
	 * Gets all the manufacture processes at the settlement.
	 * 
	 * @return list of manufacture processes.
	 */
	private List<ManufactureProcess> getManufactureProcesses() {
		List<ManufactureProcess> result = new ArrayList<>();

		result.addAll(workshop.getProcesses());

		return result;
	}

	/**
	 * Gets all the salvage processes at the settlement.
	 * 
	 * @return list of salvage processes.
	 */
	private List<SalvageProcess> getSalvageProcesses() {
		return workshop.getSalvageProcesses();
	}
	
	@Override
	public void update() {	
		if (!uiDone)
			initializeUI();

		List<ManufactureProcess> processes = getManufactureProcesses();
		List<SalvageProcess> salvages = getSalvageProcesses();
		
		processListPane.update(processes, salvages);
		printersUsed.setText(Integer.toString(workshop.getNumPrintersInUse()));
	}

	/**
	 * Prepare object for garbage collection.
	 */
	@Override
	public void destroy() {
		super.destroy();
		
		// take care to avoid null exceptions
		workshop = null;
		processListPane = null;
		scrollPanel = null;
	}
}
