/**
 * Mars Simulation Project
 * BuildingPanelFoodProduction.java
 * @version 3.1.0 2017-10-18
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_window.structure.building.food;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.mars_sim.msp.core.foodProduction.FoodProductionProcess;
import org.mars_sim.msp.core.foodProduction.FoodProductionProcessInfo;
import org.mars_sim.msp.core.foodProduction.FoodProductionUtil;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.function.FoodProduction;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingFunctionPanel;

/**
 * A building panel displaying the foodProduction building function.
 */
public class BuildingPanelFoodProduction
extends BuildingFunctionPanel {

	private static int processStringWidth = 40;

	/** default logger. */
	private static Logger logger = Logger.getLogger(BuildingPanelFoodProduction.class.getName());

	/** The foodProduction building. */
	private FoodProduction foodFactory;
	/** Panel for displaying process panels. */
	private JPanel processListPane;
	private JScrollPane scrollPanel;
	/** List of foodProduction processes in building. */
	private List<FoodProductionProcess> processCache;
	/** Process selector. */
	private JComboBoxMW processComboBox;
	/** List of available processes. */
	private Vector<FoodProductionProcessInfo> processComboBoxCache;

	/** Process selection button. */
	private JButton newProcessButton;

	/**
	 * Constructor.
	 * @param foodFactory the manufacturing building function.
	 * @param desktop the main desktop.
	 */
	public BuildingPanelFoodProduction(FoodProduction foodFactory, MainDesktopPane desktop) {
		// Use BuildingFunctionPanel constructor.
		super(foodFactory.getBuilding(), desktop);

		// Initialize data model.
		this.foodFactory = foodFactory;

        // Set panel layout
        setLayout(new BorderLayout());

        // Prepare label panel
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new GridLayout(3, 1, 0, 0));

        add(labelPanel, BorderLayout.NORTH);

        // Prepare manufacturing label
        JLabel foodProductionLabel = new JLabel("Food Production", JLabel.CENTER);
        foodProductionLabel.setFont(new Font("Serif", Font.BOLD, 16));
        labelPanel.add(foodProductionLabel);

        // Prepare tech level label
        JLabel techLabel = new JLabel("Tech Level: " + foodFactory.getTechLevel(), JLabel.CENTER);
        labelPanel.add(techLabel);

        // Prepare processCapacity label
        JLabel processCapacityLabel = new JLabel("Process Capacity: " + foodFactory.getConcurrentProcesses(), JLabel.CENTER);
        labelPanel.add(processCapacityLabel);

        // Create scroll pane for food production processes
        scrollPanel = new JScrollPane();
        scrollPanel.setPreferredSize(new Dimension(170, 90));
        add(scrollPanel, BorderLayout.CENTER);
        scrollPanel.setOpaque(false);
        scrollPanel.setBackground(new Color(0,0,0,128));
        scrollPanel.getViewport().setOpaque(false);
        scrollPanel.getViewport().setBackground(new Color(0, 0, 0, 0));
        scrollPanel.setBorder( BorderFactory.createLineBorder(Color.LIGHT_GRAY) );


        // Create process list main panel
        JPanel processListMainPane = new JPanel(new BorderLayout(0, 0));
        scrollPanel.setViewportView(processListMainPane);

        // Create process list panel
        processListPane = new JPanel();
        processListPane.setLayout(new BoxLayout(processListPane, BoxLayout.Y_AXIS));
        processListMainPane.add(processListPane, BorderLayout.NORTH);

        List<FoodProductionProcess> list = foodFactory.getProcesses();
		//Collections.sort(list);

        // Create process panels
        processCache = new ArrayList<FoodProductionProcess>(list);
        Iterator<FoodProductionProcess> i = processCache.iterator();
        while (i.hasNext()) processListPane.add(new FoodProductionPanel(i.next(), false, processStringWidth));

        // Create interaction panel.
        JPanel interactionPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        add(interactionPanel, BorderLayout.SOUTH);
 
        // Create new foodProduction process selection.
        processComboBoxCache = getAvailableProcesses();
        //2015-10-15 Enabled Collections.sorts by implementing Comparable<>
        Collections.sort(processComboBoxCache);
        processComboBox = new JComboBoxMW(processComboBoxCache);

        processComboBox.setRenderer(new FoodProductionSelectionListCellRenderer());
        processComboBox.setToolTipText("Select An Available Food Production Process");
        interactionPanel.add(processComboBox);

        // Create new process button.
        JPanel btnPanel = new JPanel(new FlowLayout()); 
        newProcessButton = new JButton("Create New Process");
        btnPanel.add(newProcessButton);

        newProcessButton.setEnabled(processComboBox.getItemCount() > 0);
        newProcessButton.setToolTipText("Create a New Food Production Process or Salvage a Process");
        newProcessButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
        		try {
        		    Object selectedItem = processComboBox.getSelectedItem();
        		    if (selectedItem != null) {
        		        if (selectedItem instanceof FoodProductionProcessInfo) {
        		            FoodProductionProcessInfo selectedProcess = (FoodProductionProcessInfo) selectedItem;
        		            if (FoodProductionUtil.canProcessBeStarted(selectedProcess, getFoodFactory())) {
                                getFoodFactory().addProcess(new FoodProductionProcess(selectedProcess, getFoodFactory()));
                                update();
                            }
        		        }

        		    }
        		}
        		catch (Exception e) {
        			logger.log(Level.SEVERE, "new process button", e);
        		}
        	}
        });
        interactionPanel.add(btnPanel);
	}


	@Override
	public void update() {

		// Update processes and salvage processes if necessary.
		List<FoodProductionProcess> processes = foodFactory.getProcesses();
		if (!processCache.equals(processes)) {

			// Add process panels for new processes.
			Iterator<FoodProductionProcess> i = processes.iterator();
			while (i.hasNext()) {
				FoodProductionProcess process = i.next();
				if (!processCache.contains(process))
					processListPane.add(new FoodProductionPanel(process, false, processStringWidth));
			}

			// Remove process panels for old processes.
			Iterator<FoodProductionProcess> j = processCache.iterator();
			while (j.hasNext()) {
				FoodProductionProcess process = j.next();
				if (!processes.contains(process)) {
					FoodProductionPanel panel = getFoodProductionPanel(process);
					if (panel != null) processListPane.remove(panel);
				}
			}

			// Update processCache
			processCache.clear();
			processCache.addAll(processes);


			scrollPanel.validate();
		}

		// Update all process panels.
		Iterator<FoodProductionProcess> i = processes.iterator();
		while (i.hasNext()) {
			FoodProductionPanel panel = getFoodProductionPanel(i.next());
			if (panel != null) panel.update();

		}
		// Update process selection list.
		Vector<FoodProductionProcessInfo> newProcesses = getAvailableProcesses();

		if (!newProcesses.equals(processComboBoxCache)) {

			processComboBoxCache = newProcesses;
			//salvageSelectionCache = newSalvages;
			Object currentSelection = processComboBox.getSelectedItem();
			processComboBox.removeAllItems();

			Collections.sort(processComboBoxCache);
			
			Iterator<FoodProductionProcessInfo> k = processComboBoxCache.iterator();
			while (k.hasNext()) processComboBox.addItem(k.next());
			if (currentSelection != null) {
				if (processComboBoxCache.contains(currentSelection))
					processComboBox.setSelectedItem(currentSelection);
			}
		}

		// Update new process button.
		newProcessButton.setEnabled(processComboBox.getItemCount() > 0);
	}

	/**
	 * Gets the panel for a foodProduction process.
	 * @param process the foodProduction process.
	 * @return foodProduction panel or null if none.
	 */
	private FoodProductionPanel getFoodProductionPanel(FoodProductionProcess process) {
		FoodProductionPanel result = null;

		for (int x = 0; x < processListPane.getComponentCount(); x++) {
			Component component = processListPane.getComponent(x);
			if (component instanceof FoodProductionPanel) {
				FoodProductionPanel panel = (FoodProductionPanel) component;
				if (panel.getFoodProductionProcess().equals(process)) result = panel;

			}
		}

		return result;
	}

	/**
	 * Gets all manufacturing processes available at the foodFactory.
	 * @return vector of processes.
	 */
	private Vector<FoodProductionProcessInfo> getAvailableProcesses() {
		Vector<FoodProductionProcessInfo> result = new Vector<FoodProductionProcessInfo>();

		if (foodFactory.getProcesses().size() < foodFactory.getConcurrentProcesses()) {

		    // Determine highest materials science skill level at settlement.
		    Settlement settlement = foodFactory.getBuilding().getBuildingManager().getSettlement();
		    int highestSkillLevel = 0;
            Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
            while (i.hasNext()) {
                Person tempPerson = i.next();
                SkillManager skillManager = tempPerson.getMind().getSkillManager();
                int skill = skillManager.getSkillLevel(SkillType.COOKING);
                if (skill > highestSkillLevel) {
                    highestSkillLevel = skill;
                }
            }

			try {
				Iterator<FoodProductionProcessInfo> j = Collections.unmodifiableList(
				        FoodProductionUtil.getFoodProductionProcessesForTechSkillLevel(
				        foodFactory.getTechLevel(), highestSkillLevel)).iterator();
				while (j.hasNext()) {
					FoodProductionProcessInfo process = j.next();
					if (FoodProductionUtil.canProcessBeStarted(process, foodFactory))
						result.add(process);
				}
			}
			catch (Exception e) {}
		}

		return result;
	}

	/**
	 * Gets the foodFactory for this panel.
	 * @return foodFactory
	 */
	private FoodProduction getFoodFactory() {
		return foodFactory;
	}

	/**
	 * Inner class for the foodProduction selection list cell renderer.
	 */
	private static class FoodProductionSelectionListCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {
			Component result = super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			if (value instanceof FoodProductionProcessInfo) {
			    FoodProductionProcessInfo info = (FoodProductionProcessInfo) value;
			    if (info != null) {
			    	// 2014-11-21 Capitalized processName
			        String processName = Conversion.capitalize(info.getName());
			        if (processName.length() > processStringWidth) processName = processName.substring(0, processStringWidth) + "...";
			        ((JLabel) result).setText(processName);
			        ((JComponent) result).setToolTipText(FoodProductionPanel.getToolTipString(info, null));
			    }
			}

			return result;
		}
	}
}