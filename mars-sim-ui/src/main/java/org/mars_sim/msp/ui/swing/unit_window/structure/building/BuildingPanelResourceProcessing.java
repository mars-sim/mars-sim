/*
 * Mars Simulation Project
 * BuildingPanelResourceProcessing.java
 * @date 2022-07-26
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.StyleManager;

/**
 * The BuildingPanelResourceProcessing class is a building function panel representing
 * the resource processes of a building.
 */
@SuppressWarnings("serial")
public class BuildingPanelResourceProcessing extends BuildingFunctionPanel {

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(BuildingPanelResourceProcessing.class.getName());

	private static final String CHEMICAL_ICON = Msg.getString("icon.chemical"); //$NON-NLS-1$
	private static final String KG_SOL = " kg/sol";
	private static final String BR = "<br>";
	private static final String HTML = "<html>";
	private static final String END_HTML = "</html>";
	private static final String INPUTS = "&emsp;&emsp;&nbsp;Inputs:&emsp;";
	private static final String OUTPUTS = "&emsp;&nbsp;&nbsp;Outputs:&emsp;";
	private static final String SPACES = "&nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;";
	private static final String PROCESS = "&emsp;&nbsp;Process:&emsp;";
	private static final String BUILDING_HEADER = "&emsp;&nbsp;Building:&emsp;";
	private static final String POWER_REQ = "Power Req:&emsp;";
	private static final String NOTE = "&emsp;<i>Note:  * denotes an ambient resource</i>";
	
	// Data members
	private ResourceProcessing processor;
	private List<ResourceProcess> processes;
	private JPanel processListPanel;

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
		// Get all processes at the building
		processes = processor.getProcesses();

		// Prepare process list panel.
		processListPanel = new JPanel(new GridLayout(0, 1, 5, 2));
		processListPanel.setAlignmentY(TOP_ALIGNMENT);
		processListPanel.setBorder(new MarsPanelBorder());
		center.add(processListPanel, BorderLayout.NORTH);
		populateProcessList();
	}

	/**
	 * Populates the process list panel with all building processes.
	 */
	private void populateProcessList() {
		// Clear the list.
		processListPanel.removeAll();

		// Add a label for each process in each processing building.
		Iterator<ResourceProcess> j = processes.iterator();
		while (j.hasNext()) {
			ResourceProcess process = j.next();
			processListPanel.add(new ResourceProcessPanel(process, building));
		}
	}
	
	@Override
	public void update() {
		// Update process list.
		Component[] components = processListPanel.getComponents();
		for (Component component : components) {
			ResourceProcessPanel panel = (ResourceProcessPanel) component;
			panel.update();
		}
	}
	
	/**
	 * An internal class for a resource process panel.
	 */
	private static class ResourceProcessPanel
	extends JPanel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members.
		private ResourceProcess process;
		private JLabel label;
		private JButton toggleButton;
		private ImageIcon dotGreen;
		private ImageIcon dotRed;

		/**
		 * Constructor.
		 * 
		 * @param process the resource process.
		 * @param building the building the process is in.
		 */
		ResourceProcessPanel(ResourceProcess process, Building building) {
			// Use JPanel constructor.
			super();

			setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

			this.process = process;

			toggleButton = new JButton();
			toggleButton.setMargin(new Insets(0, 0, 0, 0));
			toggleButton.addActionListener(e -> {
					ResourceProcess p = getProcess();
					boolean isRunning = p.isProcessRunning();
					p.setProcessRunning(!isRunning);
					update();
					if (isRunning)
						logger.log(building, Level.CONFIG, 0L, "Player stops the '" + p.getProcessName() + "'.");
					else
						logger.log(building, Level.CONFIG, 0L, "Player starts the '" + p.getProcessName() + "'.");
			});
			toggleButton.setToolTipText(Msg.getString("TabPanelResourceProcesses.tooltip.toggleButton")); //$NON-NLS-1$
			add(toggleButton);
			label = new JLabel(Msg.getString("TabPanelResourceProcesses.processLabel", //$NON-NLS-1$
					building.getNickName(), process.getProcessName())); 
			add(label);

			// Load green and red dots.
			dotGreen = ImageLoader.getIcon(Msg.getString("img.dotGreen_full")); //$NON-NLS-1$
			dotRed = ImageLoader.getIcon(Msg.getString("img.dotRed")); //$NON-NLS-1$

			if (process.isProcessRunning()) toggleButton.setIcon(dotGreen);
			else toggleButton.setIcon(dotRed);

			setToolTipText(getToolTipString(building));
		}

		/**
		 * Assembles the text for a tool tip.
		 * 
		 * @param building
		 * @return
		 */
		private String getToolTipString(Building building) {
			// NOTE: internationalize the resource processes' dynamic tooltip.
			StringBuilder result = new StringBuilder(HTML);
			// Future: Use another tool tip manager to align text to improve tooltip readability			
			result.append(PROCESS).append(process.getProcessName()).append(BR);
			result.append(BUILDING_HEADER).append(building.getNickName()).append(BR);
			result.append(POWER_REQ).append(StyleManager.DECIMAL_KW.format(process.getPowerRequired()))
			.append(BR);
			result.append(INPUTS);
			Iterator<Integer> i = process.getInputResources().iterator();
			String ambientStr = "";
			int ii = 0;
			while (i.hasNext()) {
				if (ii!=0)	result.append(SPACES);
				Integer resource = i.next();
				double rate = process.getMaxInputRate(resource) * 1000D;
				String rateString = StyleManager.DECIMAL_PLACES2.format(rate);
				if (process.isAmbientInputResource(resource)) 
					ambientStr = "*";
				result.append(ResourceUtil.findAmountResource(resource).getName())
					.append(ambientStr).append(" @ ")
					.append(rateString).append(KG_SOL).append(BR);
				ii++;
			}
			result.append(OUTPUTS);
			Iterator<Integer> j = process.getOutputResources().iterator();
			int jj = 0;
			while (j.hasNext()) {
				if (jj!=0) result.append(SPACES);
				Integer resource = j.next();
				double rate = process.getMaxOutputRate(resource) * 1000D;
				String rateString = StyleManager.DECIMAL_PLACES2.format(rate);
				result.append(ResourceUtil.findAmountResource(resource).getName())
					.append(" @ ").append(rateString).append(KG_SOL).append(BR);
				jj++;
			}
			// Add a note to denote an ambient input resource
			if (ambientStr.equals("*"))
				result.append(NOTE);
			result.append(END_HTML);
			return result.toString();
		}

		/**
		 * Updates the label.
		 */
		void update() {
			if (process.isProcessRunning()) toggleButton.setIcon(dotGreen);
			else toggleButton.setIcon(dotRed);
		}

		private ResourceProcess getProcess() {
			return process;
		}
	}
}
