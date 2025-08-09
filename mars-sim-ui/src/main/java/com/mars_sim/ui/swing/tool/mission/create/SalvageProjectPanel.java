/*
 * Mars Simulation Project
 * SalvageProjectPanel.java
 * @date 2021-09-20
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.mission.create;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.building.construction.ConstructionStageInfo;
import com.mars_sim.core.building.construction.ConstructionUtil;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.ui.swing.MarsPanelBorder;

@SuppressWarnings("serial")
public class SalvageProjectPanel
extends WizardPanel {
 	
	/** The wizard panel name. */
    private static final String NAME = "Salvage Project";
    
    // Data members
    private JLabel errorMessageLabel;
    private DefaultListModel<Object> projectListModel;
    private JList<Object> projectList;
    private PartsTableModel partsTableModel;
    private JTable partsTable;
    
    /**
     * Constructor
     * @param wizard the create mission wizard.
     */
    SalvageProjectPanel(CreateMissionWizard wizard) {
        // Use WizardPanel constructor.
        super(wizard);
        
        // Set the layout.
        setLayout(new BorderLayout(10, 10));
        
        // Set the border.
        setBorder(new MarsPanelBorder());
        
        // Create the select salvage project label.
        JLabel titleLabel = createTitleLabel("Select a salvage project");
        add(titleLabel, BorderLayout.NORTH);

        // Create the center panel.
        JPanel centerPane = new JPanel(new GridLayout(1, 2, 10, 10));
        add(centerPane, BorderLayout.CENTER);
        
        // Create the project panel.
        JPanel projectPane = new JPanel(new BorderLayout(0, 0));
        projectPane.setBorder(new MarsPanelBorder());
        centerPane.add(projectPane);
        
        // Create the salvage project label.
        JLabel projectLabel = new JLabel("Buildings and Construction Sites", SwingConstants.CENTER);
        projectPane.add(projectLabel, BorderLayout.NORTH);
        
        // Create scroll pane for salvage project selection list.
        JScrollPane projectListScrollPane = new JScrollPane();
        projectListScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        projectPane.add(projectListScrollPane, BorderLayout.CENTER);
        
        // Create project selection list.
        projectListModel = new DefaultListModel<>();
        populateProjectListModel();
        projectList = new JList<>(projectListModel);
        projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectList.addListSelectionListener(arg0 -> {
            partsTableModel.update();
            Object project = projectList.getSelectedValue();
            if (project != null) {
                getWizard().setButtons(true);
                errorMessageLabel.setText(" ");
            }
            else {
                getWizard().setButtons(false);
                errorMessageLabel.setText(" ");
            }
        });
        projectListScrollPane.setViewportView(projectList);
        
        // Create the parts panel.
        JPanel partsPane = new JPanel(new BorderLayout(0, 0));
        partsPane.setBorder(new MarsPanelBorder());
        centerPane.add(partsPane);
        
        // Create the parts label.
        JLabel partsLabel = new JLabel("Estimated Salvaged Parts", SwingConstants.CENTER);
        partsPane.add(partsLabel, BorderLayout.NORTH);
        
        // Create scroll pane for parts table.
        JScrollPane partsTableScrollPane = new JScrollPane();
        partsTableScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        partsPane.add(partsTableScrollPane, BorderLayout.CENTER);
        
        // Create the parts table model.
        partsTableModel = new PartsTableModel();
        
        // Create the parts table.
        partsTable = new JTable(partsTableModel);
        partsTable.setRowSelectionAllowed(false);
        partsTableScrollPane.setViewportView(partsTable);
        
        // Create the error message label.
        errorMessageLabel = createErrorLabel();
        add(errorMessageLabel, BorderLayout.SOUTH);
    }
    
    @Override
    void clearInfo() {
        projectListModel.clear();
        partsTableModel.update();
        getWizard().setButtons(false);
        errorMessageLabel.setText(" ");
    }

	/**
	 * Commits changes from this wizard panel.
	 * 
	 * @param isTesting true if it's only testing conditions
	 * @return true if changes can be committed.
	 */
	@Override
    boolean commitChanges(boolean isTesting) {
        
        Object project = projectList.getSelectedValue();
        if (project != null) {
            if (project instanceof Building salvageBuilding) {
                // Set salvage building.
                getWizard().getMissionData().setSalvageBuilding(salvageBuilding);
                return true;
            }
            else if (project instanceof ConstructionSite salvageSite) {
                // Set salvage site.
                getWizard().getMissionData().setSalvageSite(salvageSite);
                return true;
            }
        }
        
        return false;
    }

    @Override
    String getPanelName() {
        return NAME;
    }

    @Override
    void updatePanel() {
        populateProjectListModel();
        getWizard().setButtons(false);
    }
    
    /**
     * Populates the project list model.
     */
    private void populateProjectListModel() {
        // Clear project list model.
        projectListModel.clear();
        
        Settlement salvageSettlement = getWizard().getMissionData().getSalvageSettlement();
        if (salvageSettlement != null) {
            
            // Add settlement buildings to list.
            java.util.List<Building> buildingList = salvageSettlement.getBuildingManager().getSortedBuildings();
            Iterator<Building> i = buildingList.iterator();
            while (i.hasNext()) projectListModel.addElement(i.next());
            
            // Add construction sites.
            Iterator<ConstructionSite> j = salvageSettlement.getConstructionManager().
                    getConstructionSitesNeedingMission(false).iterator();
            while (j.hasNext()) projectListModel.addElement(j.next());
        }
    }
    
    /**
     * A table model for salvage parts.
     */
    private class PartsTableModel extends AbstractTableModel {

        /** default serial id. */
    	private static final long serialVersionUID = 1L;

    	// Data members.
        private ConstructionStageInfo info = null;
        private Map<Integer, Integer> partsNumber;
        
        /**
         * Constructor.
         */
        private PartsTableModel() {
            // Use AbstractTableModel constructor.
            super();
            
            partsNumber = new HashMap<>();
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        @Override
        public String getColumnName(int columnIndex) {
            String result = "";
            if (columnIndex == 0) result = "Part";
            else if (columnIndex == 1) result = "Estimated Salvage";
            return result;
        }

        public int getRowCount() {
            return partsNumber.keySet().size();
        }

        public Object getValueAt(int row, int col) {
            if ((row < partsNumber.keySet().size()) && (col < 2)) {
                Integer part = (int)partsNumber.keySet().toArray()[row];
                return switch (col) {
                  case 0 -> part.toString();
                  case 1 -> partsNumber.get(part);
                  default -> null;
                };
            }
            else return null;
        }
        
        /**
         * Update the table.
         */
        private void update() {
            info = null;
            Object project = projectList.getSelectedValue();
            if (project instanceof Building salvageBuilding) {
                info = ConstructionUtil.getConstructionStageInfo(salvageBuilding.getBuildingType());
            }
            else if (project instanceof ConstructionSite salvageSite) {
                info = salvageSite.getCurrentConstructionStage().getInfo();
            }
            partsNumber.clear();
            populatePartsNumber();
            fireTableStructureChanged();
        }
        
        /**
         * Populate the map of estimated salvage parts and their numbers.
         */
        private void populatePartsNumber() {
            
            if (info != null) {
                // Get average construction skill of mission members.
                double totalSkill = 0D;
                double averageSkill = 0;
                
                int memberNum = getWizard().getMissionData().getAllMembers().size();
                // Add mission members.
                Iterator<Worker> i = getWizard().getMissionData().getAllMembers().iterator();
                while (i.hasNext()) {                     	
                    Worker member = i.next();
                    int constructionSkill = member.getSkillManager().getSkillLevel(SkillType.CONSTRUCTION);
                    totalSkill += constructionSkill;   
                }
                
                averageSkill = totalSkill / memberNum;
                
                // Get chance of salvage.
                double salvageChance = 50D + (averageSkill * 5D);
                if (salvageChance > 100D) salvageChance = 100D;
                
                // Estimate parts salvaged.
                Iterator<Integer> j = info.getParts().keySet().iterator();
                while (j.hasNext()) {
                    Integer part = j.next();
                    int maxSalvage = info.getParts().get(part);
                    int estimatedSalvage = (int) Math.round(maxSalvage * (salvageChance / 100D));
                    partsNumber.put(part, estimatedSalvage);
                }
            }
        }
    }
}
