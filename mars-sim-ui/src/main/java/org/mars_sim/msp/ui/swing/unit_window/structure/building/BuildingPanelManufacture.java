/*
 * Mars Simulation Project
 * BuildingPanelManufacture.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.manufacture.ManufactureProcess;
import org.mars_sim.msp.core.manufacture.ManufactureProcessInfo;
import org.mars_sim.msp.core.manufacture.ManufactureUtil;
import org.mars_sim.msp.core.manufacture.SalvageProcess;
import org.mars_sim.msp.core.manufacture.SalvageProcessInfo;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.function.Manufacture;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.structure.ManufacturePanel;
import org.mars_sim.msp.ui.swing.unit_window.structure.SalvagePanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;


/**
 * A building panel displaying the manufacture building function.
 */
@SuppressWarnings("serial")
public class BuildingPanelManufacture extends BuildingFunctionPanel {

	/** default logger. */
	private static final Logger logger = Logger.getLogger(BuildingPanelManufacture.class.getName());

	private static final String MANU_ICON = "manufacture";
	
	private static int processStringWidth = 170;

	/** The manufacture building. */
	private Manufacture workshop;
	/** Panel for displaying process panels. */
	private JPanel processListPane;
	/** The scroll panel for the process list. */
	private JScrollPane scrollPanel;
	/** List of manufacture processes in building. */
	private List<ManufactureProcess> processCache;
	/** List of salvage processes in building. */
	private List<SalvageProcess> salvageCache;
	/** Process selector. */
	private JComboBoxMW<?> processComboBox;
	/** List of available processes. */
	private List<ManufactureProcessInfo> processComboBoxCache;
	/** List of available salvage processes. */
	private List<SalvageProcessInfo> salvageSelectionCache;
	/** Process selection button. */
	private JButton newProcessButton;

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
	 * Build the UI
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
		labelPanel.addTextField("# of Printers In Use",
								Integer.toString(workshop.getNumPrintersInUse()), null);
			
		// Create scroll pane for manufacturing processes
		scrollPanel = new JScrollPane();
		scrollPanel.setPreferredSize(new Dimension(170, 90));
		center.add(scrollPanel, BorderLayout.CENTER);

		// Create process list main panel
		JPanel processListMainPane = new JPanel(new BorderLayout(0, 0));
		scrollPanel.setViewportView(processListMainPane);

		// Create process list panel
		processListPane = new JPanel(new FlowLayout(10, 10 ,10));
		processListPane.setLayout(new BoxLayout(processListPane, BoxLayout.Y_AXIS));
		processListMainPane.add(processListPane, BorderLayout.NORTH);

		List<ManufactureProcess> list = workshop.getProcesses();

		// Create process panels
		processCache = new ArrayList<>(list);
		Iterator<ManufactureProcess> i = processCache.iterator();
		while (i.hasNext())
			processListPane.add(new ManufacturePanel(i.next(), false, processStringWidth));

		// Create salvage panels.
		salvageCache = new ArrayList<>(workshop.getSalvageProcesses());
		Iterator<SalvageProcess> j = salvageCache.iterator();
		while (j.hasNext())
			processListPane.add(new SalvagePanel(j.next(), false, processStringWidth));

		// Create interaction panel.
		JPanel interactionPanel = new JPanel(new GridLayout(2, 1, 10, 10));
		center.add(interactionPanel, BorderLayout.SOUTH);
		
		// Create new manufacture process selection.
		processComboBoxCache = getAvailableProcesses();
		processComboBox = new JComboBoxMW<>();
		
		Iterator<ManufactureProcessInfo> k = processComboBoxCache.iterator();
		while (k.hasNext()) processComboBox.addItem(k.next());
		
		processComboBox.setRenderer(new ManufactureSelectionListCellRenderer());
		processComboBox.setToolTipText("Select an Available Manufacturing Process");
		interactionPanel.add(processComboBox);

		// Add available salvage processes.
		salvageSelectionCache = getAvailableSalvageProcesses();

		Iterator<SalvageProcessInfo> l = salvageSelectionCache.iterator();
		while (l.hasNext()) processComboBox.addItem(l.next());
		
		if (processComboBoxCache.size() + salvageSelectionCache.size() > 0)
			processComboBox.setSelectedIndex(0);
		
		// Create new process button.
		JPanel btnPanel = new JPanel(new FlowLayout());
		newProcessButton = new JButton("Create New Process");
		btnPanel.add(newProcessButton);
		newProcessButton.setEnabled(processComboBox.getItemCount() > 0);
		newProcessButton.setToolTipText("Create a New Manufacturing Process or Salvage a Process");
		newProcessButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					Object selectedItem = processComboBox.getSelectedItem();
					if (selectedItem != null) {
						if (selectedItem instanceof ManufactureProcessInfo) {
							ManufactureProcessInfo selectedProcess = (ManufactureProcessInfo) selectedItem;
							if (ManufactureUtil.canProcessBeStarted(selectedProcess, getWorkshop())) {
								getWorkshop().addProcess(new ManufactureProcess(selectedProcess, getWorkshop()));
								update();
							}
						} else if (selectedItem instanceof SalvageProcessInfo) {
							SalvageProcessInfo selectedSalvage = (SalvageProcessInfo) selectedItem;
							if (ManufactureUtil.canSalvageProcessBeStarted(selectedSalvage, getWorkshop())) {
								Unit salvagedUnit = ManufactureUtil.findUnitForSalvage(selectedSalvage,
										getWorkshop().getBuilding().getSettlement());
								getWorkshop().addSalvageProcess(
										new SalvageProcess(selectedSalvage, getWorkshop(), salvagedUnit));
								update();
							}
						}
					}
				} catch (Exception e) {
					logger.log(Level.SEVERE, "new process button", e);
				}
			}
		});
		interactionPanel.add(btnPanel);		
	}

	@Override
	public void update() {

		// Update processes and salvage processes if necessary.
		List<ManufactureProcess> processes =  new ArrayList<>(workshop.getProcesses());
		List<SalvageProcess> salvages = new ArrayList<>(workshop.getSalvageProcesses());
		if (!processCache.equals(processes) || !salvageCache.equals(salvages)) {

			// Add process panels for new processes.
			Iterator<ManufactureProcess> i = processes.iterator();
			while (i.hasNext()) {
				ManufactureProcess process = i.next();
				if (!processCache.contains(process))
					processListPane.add(new ManufacturePanel(process, false, processStringWidth));
			}

			// Add salvage panels for new salvage processes.
			Iterator<SalvageProcess> k = salvages.iterator();
			while (k.hasNext()) {
				SalvageProcess salvage = k.next();
				if (!salvageCache.contains(salvage))
					processListPane.add(new SalvagePanel(salvage, false, processStringWidth));
			}

			// Remove process panels for old processes.
			Iterator<ManufactureProcess> j = processCache.iterator();
			while (j.hasNext()) {
				ManufactureProcess process = j.next(); 
				if (!processes.contains(process)) {
					ManufacturePanel panel = getManufacturePanel(process);
					if (panel != null)
						processListPane.remove(panel);
				}
			}

			// Remove salvage panels for old salvages.
			Iterator<SalvageProcess> l = salvageCache.iterator();
			while (l.hasNext()) {
				SalvageProcess salvage = k.next();
				if (!salvages.contains(salvage)) {
					SalvagePanel panel = getSalvagePanel(salvage);
					if (panel != null)
						processListPane.remove(panel);
				}
			}

			// Update processCache
			processCache.clear();
			processCache.addAll(processes);

			// Update salvageCache
			salvageCache.clear();
			salvageCache.addAll(salvages);

			scrollPanel.validate();
		}

		// Update all process panels.
		Iterator<ManufactureProcess> i = processes.iterator();
		while (i.hasNext()) {
			ManufacturePanel panel = getManufacturePanel(i.next());
			if (panel != null)
				panel.update();
		}

		// Update all salvage panels.
		Iterator<SalvageProcess> j = salvages.iterator();
		while (j.hasNext()) {
			SalvagePanel panel = getSalvagePanel(j.next());
			if (panel != null)
				panel.update();
		}

		// Update process selection list.
		List<ManufactureProcessInfo> newProcesses = getAvailableProcesses();
		List<SalvageProcessInfo> newSalvages = getAvailableSalvageProcesses();

		Object currentSelection = processComboBox.getSelectedItem();

		if (!newProcesses.equals(processComboBoxCache)) {
			processComboBoxCache = newProcesses;

			for (ManufactureProcessInfo m : processComboBoxCache) {
				processComboBox.removeItem(m);
			}

			Iterator<ManufactureProcessInfo> k = processComboBoxCache.iterator();
			while (k.hasNext())
				processComboBox.addItem(k.next());
		}

		if (!newSalvages.equals(salvageSelectionCache)) {
			salvageSelectionCache = newSalvages;

			for (SalvageProcessInfo s : salvageSelectionCache) {
				processComboBox.removeItem(s);
			}

			Iterator<SalvageProcessInfo> l = salvageSelectionCache.iterator();
			while (l.hasNext())
				processComboBox.addItem(l.next());
		}

		if (currentSelection != null) {
			if (processComboBoxCache.contains(currentSelection))
				processComboBox.setSelectedItem(currentSelection);
		}
		else if (processComboBoxCache.size() + salvageSelectionCache.size() > 0)
			processComboBox.setSelectedIndex(0);
		
		// Update new process button.
		newProcessButton.setEnabled(processComboBox.getItemCount() > 0);
	}

	/**
	 * Gets the panel for a manufacture process.
	 * 
	 * @param process the manufacture process.
	 * @return manufacture panel or null if none.
	 */
	private ManufacturePanel getManufacturePanel(ManufactureProcess process) {
		ManufacturePanel result = null;

		for (int x = 0; x < processListPane.getComponentCount(); x++) {
			Component component = processListPane.getComponent(x);
			if (component instanceof ManufacturePanel) {
				ManufacturePanel panel = (ManufacturePanel) component;
				if (panel.getManufactureProcess().equals(process))
					result = panel;
			}
		}

		return result;
	}

	/**
	 * Gets the panel for a salvage process.
	 * 
	 * @param process the salvage process.
	 * @return the salvage panel or null if none.
	 */
	private SalvagePanel getSalvagePanel(SalvageProcess process) {
		SalvagePanel result = null;

		for (int x = 0; x < processListPane.getComponentCount(); x++) {
			Component component = processListPane.getComponent(x);
			if (component instanceof SalvagePanel) {
				SalvagePanel panel = (SalvagePanel) component;
				if (panel.getSalvageProcess().equals(process))
					result = panel;
			}
		}

		return result;
	}

	/**
	 * Gets all manufacturing processes available at the workshop.
	 * 
	 * @return vector of processes.
	 */
	private List<ManufactureProcessInfo> getAvailableProcesses() {
		List<ManufactureProcessInfo> result = new ArrayList<>();

		if (workshop.getProcesses().size() < workshop.getNumPrintersInUse()) {

			// Determine highest materials science skill level at settlement.
			Settlement settlement = workshop.getBuilding().getSettlement();
			int highestSkillLevel = 0;
			Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
			while (i.hasNext()) {
				Person tempPerson = i.next();
				SkillManager skillManager = tempPerson.getSkillManager();
				int skill = skillManager.getSkillLevel(SkillType.MATERIALS_SCIENCE);
				if (skill > highestSkillLevel) {
					highestSkillLevel = skill;
				}
			}

			Iterator<Robot> k = settlement.getAllAssociatedRobots().iterator();
			while (k.hasNext()) {
				Robot r = k.next();
				SkillManager skillManager = r.getSkillManager();
				int skill = skillManager.getSkillLevel(SkillType.MATERIALS_SCIENCE);
				if (skill > highestSkillLevel) {
					highestSkillLevel = skill;
				}
			}
			
			try {
				Iterator<ManufactureProcessInfo> j = Collections
						.unmodifiableList(ManufactureUtil
								.getManufactureProcessesForTechSkillLevel(workshop.getTechLevel(), highestSkillLevel))
						.iterator();
				while (j.hasNext()) {
					ManufactureProcessInfo process = j.next();
					if (ManufactureUtil.canProcessBeStarted(process, workshop))
						result.add(process);
				}
			} catch (Exception e) {
			}
		}
		// Enable Collections.sorts by implementing Comparable<>
		Collections.sort(result);
		return result;
	}

	/**
	 * Gets all salvage processes available at the workshop.
	 * 
	 * @return vector of salvage processes.
	 */
	private List<SalvageProcessInfo> getAvailableSalvageProcesses() {
		List<SalvageProcessInfo> result = new Vector<SalvageProcessInfo>();

		if (workshop.getProcesses().size() < workshop.getNumPrintersInUse()) {
			try {
				Iterator<SalvageProcessInfo> i = Collections
						.unmodifiableList(ManufactureUtil.getSalvageProcessesForTechLevel(workshop.getTechLevel()))
						.iterator();
				while (i.hasNext()) {
					SalvageProcessInfo process = i.next();
					if (ManufactureUtil.canSalvageProcessBeStarted(process, workshop))
						result.add(process);
				}
			} catch (Exception e) {
			}
		}
		// Enable Collections.sorts by implementing Comparable<>
		Collections.sort(result);
		return result;
	}

	/**
	 * Gets the workshop for this panel.
	 * 
	 * @return workshop
	 */
	private Manufacture getWorkshop() {
		return workshop;
	}

	/**
	 * Inner class for the manufacture selection list cell renderer.
	 */
	private static class ManufactureSelectionListCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof ManufactureProcessInfo) {
				ManufactureProcessInfo info = (ManufactureProcessInfo) value;
				if (info != null) {
					// Capitalized processName
					String processName = info.getName();
					if (processName.length() > processStringWidth)
						processName = processName.substring(0, processStringWidth) + "...";
					((JLabel) result).setText(processName);
					((JComponent) result).setToolTipText(ManufacturePanel.getToolTipString(info, null));
				}
			} else if (value instanceof SalvageProcessInfo) {
				SalvageProcessInfo info = (SalvageProcessInfo) value;
				if (info != null) {
					// Capitalized processName
					String processName = info.getName();
					if (processName.length() > processStringWidth)
						processName = processName.substring(0, processStringWidth) + "...";
					((JLabel) result).setText(processName);
					((JComponent) result).setToolTipText(SalvagePanel.getToolTipString(null, info, null));
				}
			}
			return result;
		}
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
		processCache = null;
		salvageCache = null;
		processComboBox = null;
		processComboBoxCache = null;
		salvageSelectionCache = null;
		newProcessButton = null;
	}
}
