/**
 * Mars Simulation Project
 * ConstructionProjectPanel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission.create;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStage;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.core.structure.construction.ConstructionUtil;
import org.mars_sim.msp.core.structure.construction.ConstructionVehicleType;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

/**
 * A wizard panel for selecting the mission's construction project information.
 */
class ConstructionProjectPanel extends WizardPanel {

    // The wizard panel name.
    private final static String NAME = "Construction Project";
    
    // Data members
    private JLabel errorMessageLabel;
    private DefaultListModel siteListModel;
    private JList siteList;
    private DefaultListModel projectListModel;
    private JList projectList;
    private MaterialsTableModel materialsTableModel;
    private JTable materialsTable;
    
    /**
     * Constructor
     * @param wizard the create mission wizard.
     */
    ConstructionProjectPanel(final CreateMissionWizard wizard) {
        // Use WizardPanel constructor.
        super(wizard);
        
        // Set the layout.
        setLayout(new BorderLayout(0, 0));
        
        // Set the border.
        setBorder(new MarsPanelBorder());
        
        // Create the select construction project label.
        JLabel titleLabel = new JLabel("Select a construction project", JLabel.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // Create the center panel.
        JPanel centerPane = new JPanel(new BorderLayout(0, 0));
        add(centerPane, BorderLayout.CENTER);
        
        // Create main selection panel.
        JPanel mainSelectionPane = new JPanel(new GridLayout(1, 2));
        mainSelectionPane.setPreferredSize(new Dimension(-1, 200));
        centerPane.add(mainSelectionPane, BorderLayout.NORTH);
        
        // Create construction site selection panel.
        JPanel constructionSiteSelectionPane = new JPanel(new BorderLayout(0, 0));
        constructionSiteSelectionPane.setBorder(new MarsPanelBorder());
        mainSelectionPane.add(constructionSiteSelectionPane);
        
        // Create construction site selection label.
        JLabel constructionSiteSelectionLabel = new JLabel("Select Construction Site", JLabel.CENTER);
        constructionSiteSelectionPane.add(constructionSiteSelectionLabel, BorderLayout.NORTH);
        
        // Create scroll pane for site selection list.
        JScrollPane siteListScrollPane = new JScrollPane();
        siteListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        constructionSiteSelectionPane.add(siteListScrollPane, BorderLayout.CENTER);
        
        // Create site selection list.
        siteListModel = new DefaultListModel();
        populateSiteListModel();
        siteList = new JList(siteListModel);
        siteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        siteList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent arg0) {
                getWizard().setButtons(false);
                errorMessageLabel.setText(" ");
                populateProjectListModel();
            }
        });
        siteListScrollPane.setViewportView(siteList);
        
        // Create construction project selection panel.
        JPanel constructionProjectSelectionPane = new JPanel(new BorderLayout(0, 0));
        constructionProjectSelectionPane.setBorder(new MarsPanelBorder());
        mainSelectionPane.add(constructionProjectSelectionPane);
        
        // Create construction project selection label.
        JLabel constructionProjectSelectionLabel = new JLabel("Select Construction Project", JLabel.CENTER);
        constructionProjectSelectionPane.add(constructionProjectSelectionLabel, BorderLayout.NORTH);
        
        // Create scroll pane for project selection list.
        JScrollPane projectListScrollPane = new JScrollPane();
        projectListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        constructionProjectSelectionPane.add(projectListScrollPane, BorderLayout.CENTER);
        
        // Create project selection list.
        projectListModel = new DefaultListModel();
        populateProjectListModel();
        projectList = new JList(projectListModel);
        projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectList.addListSelectionListener(
        	new ListSelectionListener() {
	            public void valueChanged(ListSelectionEvent arg0) {
	                materialsTableModel.update();
	                String selectedSite = (String) siteList.getSelectedValue();
	                ConstructionStageInfo stageInfo = (ConstructionStageInfo) projectList.getSelectedValue();
	                projectList.setToolTipText(getToolTipText(stageInfo));
	                if (stageInfo != null) {
	                    if (selectedSite.indexOf(" unfinished") >= 0) {
	                        getWizard().setButtons(true);
	                        errorMessageLabel.setText(" ");
	                    }
	                    else {
	                        try {
	                            if (hasEnoughConstructionMaterials(stageInfo)) {
	                                getWizard().setButtons(true);
	                                errorMessageLabel.setText(" ");
	                            }
	                            else {
	                                getWizard().setButtons(false);
	                                errorMessageLabel.setText("Not enough materials at settlement " +
	                                        "for construction project.");
	                            }
	                        }
	                        catch (Exception e) {
	                            e.printStackTrace();
	                        }
	                    }
	                }
	                else errorMessageLabel.setText(" ");
	            }
	        }
        );
		// call it a click to next button when user double clicks the table
		projectList.addMouseListener(
			new MouseListener() {
				public void mouseReleased(MouseEvent e) {}
				public void mousePressed(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
				public void mouseEntered(MouseEvent e) {}
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2 && !e.isConsumed()) {
						wizard.buttonClickedNext();
					}
				}
			}
		);
        projectListScrollPane.setViewportView(projectList);
        
        // Create construction materials panel.
        JPanel constructionMaterialsPane = new JPanel(new BorderLayout(0, 0));
        constructionMaterialsPane.setBorder(new MarsPanelBorder());
        centerPane.add(constructionMaterialsPane, BorderLayout.CENTER);
        
        // Create construction materials label.
        JLabel constructionMaterialsLabel = new JLabel("Construction Materials Required", JLabel.CENTER);
        constructionMaterialsPane.add(constructionMaterialsLabel, BorderLayout.NORTH);
        
        // Create scroll pane for construction materials table.
        JScrollPane materialsTableScrollPane = new JScrollPane();
        materialsTableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        constructionMaterialsPane.add(materialsTableScrollPane, BorderLayout.CENTER);
        
        // Create the materials table model.
        materialsTableModel = new MaterialsTableModel();
        
        // Create the materials table.
        materialsTable = new JTable(materialsTableModel);
        materialsTable.setRowSelectionAllowed(false);
        materialsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component result = super.getTableCellRendererComponent(table, value, 
                        isSelected, hasFocus, row, column);
                
                // If failure cell, mark background red.
                MaterialsTableModel tableModel = (MaterialsTableModel) table.getModel();
                if (tableModel.isFailureCell(row, column)) setBackground(Color.RED);
                else if (!isSelected) setBackground(Color.WHITE);
                
                return result;
            }
        });
        materialsTableScrollPane.setViewportView(materialsTable);
        
        // Create the error message label.
        errorMessageLabel = new JLabel(" ", JLabel.CENTER);
        errorMessageLabel.setForeground(Color.RED);
        errorMessageLabel.setFont(errorMessageLabel.getFont().deriveFont(Font.BOLD));
        errorMessageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(errorMessageLabel, BorderLayout.SOUTH);
    }   
    
    @Override
    void clearInfo() {
        siteListModel.clear();
        projectListModel.clear();
        materialsTableModel.update();
        getWizard().setButtons(false);
        errorMessageLabel.setText(" ");
    }

    @Override
    boolean commitChanges() {
        
        Settlement settlement = getConstructionSettlement();
        ConstructionManager manager = settlement.getConstructionManager();
        
        // Get construction site.
        ConstructionSite selectedSite = null;
        int selectedSiteIndex = siteList.getSelectedIndex();
        if (selectedSiteIndex > 0) {
            int existingSiteIndex = selectedSiteIndex - 1;
            selectedSite = manager.getConstructionSites().get(existingSiteIndex);
        }
        getWizard().getMissionData().setConstructionSite(selectedSite);
        
        // Get construction stage info.
        ConstructionStageInfo selectedInfo = (ConstructionStageInfo) projectList.getSelectedValue();
        getWizard().getMissionData().setConstructionStageInfo(selectedInfo);
        
        return true;
    }

    @Override
    String getPanelName() {
        return NAME;
    }

    @Override
    void updatePanel() {
        populateSiteListModel();
        getWizard().setButtons(false);
    }
    
    /**
     * Gets the tool tip text for the project list based on the selected stage.
     * @param stageInfo the selected stage info.
     * @return tool tip text.
     */
    private String getToolTipText(ConstructionStageInfo stageInfo) {
        String result = null;
        
        if (stageInfo != null) {
            if (!stageInfo.getType().equals(ConstructionStageInfo.BUILDING)) {
                try {
                    StringBuilder nextStages = new StringBuilder("<html>Next possible stages:");
                    Iterator<ConstructionStageInfo> i = ConstructionUtil.
                            getNextPossibleStages(stageInfo).iterator();
                    while (i.hasNext()) {
                        nextStages.append("<br>&nbsp;&nbsp;&nbsp;").append(i.next().getName());
                    }
                    nextStages.append("</html>");
                    result = nextStages.toString();
                }
                catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Populates the site list model.
     */
    private void populateSiteListModel() {
        siteListModel.clear();
        siteListModel.addElement("New Site");
        
        Settlement settlement = getConstructionSettlement();
        if (settlement != null) {
            ConstructionManager manager = settlement.getConstructionManager();
            Iterator<ConstructionSite> i = manager.getConstructionSites().iterator();
            while (i.hasNext()) {
                ConstructionSite site = i.next();
                ConstructionStage stage = site.getCurrentConstructionStage();
                if (site.isUndergoingConstruction()) {
                    siteListModel.addElement("Site: " + stage + " - under construction");
                }
                else if (site.isUndergoingSalvage()) {
                    siteListModel.addElement("Site: " + stage + " - under salvage");
                }
                else if (site.hasUnfinishedStage()) {
                    if (stage.isSalvaging()) siteListModel.addElement("Site: " + stage + " salvage unfinished");
                    else siteListModel.addElement("Site: " + stage + " construction unfinished");
                }
                else {
                    siteListModel.addElement("Site: " + stage);
                }
            }
        }
    }
    
    /**
     * Populates the project list model.
     */
    private void populateProjectListModel() {
        projectListModel.clear();
        
        int selectedSiteIndex = siteList.getSelectedIndex();
        String selectedSite = (String) siteList.getSelectedValue();
        if (selectedSite != null) {
            if (selectedSite.equals("New Site")) {
                try {
                    // Show all foundation projects.
                    Iterator<ConstructionStageInfo> i = ConstructionUtil.
                            getFoundationConstructionStageInfoList().iterator();
                    while (i.hasNext()) {
                        ConstructionStageInfo info = i.next();
                        if (info.isConstructable()) projectListModel.addElement(info);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
            else if (selectedSite.indexOf(" - under construction") >= 0) {
                errorMessageLabel.setText("Cannot start mission on site already undergoing construction.");
                // Do nothing.
            }
            else if (selectedSite.indexOf(" - under salvage") >= 0) {
                errorMessageLabel.setText("Cannot start mission on site already undergoing salvage.");
                // Do nothing.
            }
            else {
                Settlement settlement = getConstructionSettlement();
                if (settlement != null) {
                    ConstructionManager manager = settlement.getConstructionManager();
                    int siteNum = selectedSiteIndex - 1;
                    ConstructionSite site = manager.getConstructionSites().get(siteNum);
                    if (site != null) {
                        if (selectedSite.indexOf(" unfinished") >= 0) {
                            // Show current construction stage.
                            projectListModel.addElement(site.getCurrentConstructionStage().getInfo());
                        }
                        else {
                            try {
                                // Show all possible stage infos.
                                ConstructionStageInfo info = site.getCurrentConstructionStage().getInfo();
                                Iterator<ConstructionStageInfo> i = ConstructionUtil.
                                        getNextPossibleStages(info).iterator();
                                while (i.hasNext()) {
                                    ConstructionStageInfo stageInfo = i.next();
                                    if (stageInfo.isConstructable()) projectListModel.addElement(stageInfo);
                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace(System.err);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Gets the construction settlement.
     * @return settlement or null if none.
     */
    private Settlement getConstructionSettlement() {
        return getWizard().getMissionData().getConstructionSettlement();
    }
    
    /**
     * Checks if there are enough construction materials at the settlement to
     * construct the stage.
     * @param stageInfo the stage information.
     * @return true if enough materials.
     * @throws Exception if error determining construction material availability.
     */
    private boolean hasEnoughConstructionMaterials(ConstructionStageInfo stageInfo) {
        
        boolean result = true;
        
        Settlement settlement = getWizard().getMissionData().getConstructionSettlement();
        Inventory inv = settlement.getInventory();
        
        // Check amount resources.
        Iterator<AmountResource> i = stageInfo.getResources().keySet().iterator();
        while (i.hasNext()) {
            AmountResource resource = i.next();
            double amount = stageInfo.getResources().get(resource);
            if (inv.getAmountResourceStored(resource, false) < amount) result = false;
        }
        
        // Check parts.
        Iterator<Part> j = stageInfo.getParts().keySet().iterator();
        while (j.hasNext()) {
            Part part = j.next();
            int number = stageInfo.getParts().get(part);
            if (inv.getItemResourceNum(part) < number) result = false;
        }
        
        // Check for LUV's.
        int luvsNeeded = stageInfo.getVehicles().size();
        int luvsAvailable = inv.findNumUnitsOfClass(LightUtilityVehicle.class);
        if (luvsAvailable < luvsNeeded) result = false;
        
        // Check for LUV attachment parts.
        Map<Part, Integer> attachmentParts = new HashMap<Part, Integer>();
        Iterator<ConstructionVehicleType> k = stageInfo.getVehicles().iterator();
        while (k.hasNext()) {
            ConstructionVehicleType vehicleType = k.next();
            Iterator<Part> l = vehicleType.getAttachmentParts().iterator();
            while (l.hasNext()) {
                Part part = l.next();
                int partNum = 1;
                if (attachmentParts.containsKey(part)) partNum += attachmentParts.get(part);
                attachmentParts.put(part, partNum);
            }
        }
        Iterator<Part> m = attachmentParts.keySet().iterator();
        while (m.hasNext()) {
            Part part = m.next();
            int number = attachmentParts.get(part);
            if (inv.getItemResourceNum(part) < number) result = false;
        }
        
        return result;
    }
    
    /**
     * A table model for construction materials.
     */
    private class MaterialsTableModel extends AbstractTableModel {

        // Data members.
        private ConstructionStageInfo info = null;
        private List<ConstructionMaterial> materialsList;
        
        /**
         * Constructor.
         */
        private MaterialsTableModel() {
            // Use AbstractTableModel constructor.
            super();
            
            materialsList = new ArrayList<ConstructionMaterial>();
        }
        
        public int getColumnCount() {
            return 3;
        }
        
        @Override
        public String getColumnName(int columnIndex) {
            String result = "";
            if (columnIndex == 0) result = "Construction Material";
            else if (columnIndex == 1) result = "Required by Project";
            else if (columnIndex == 2) result = "Available at Settlement";
            return result;
        }

        public int getRowCount() {
            return materialsList.size();
        }

        public Object getValueAt(int row, int col) {
            if ((row < materialsList.size()) && (col < 3)) {
                if (col == 0) {
                    return materialsList.get(row).toString();
                }
                else if (col == 1) {
                    return materialsList.get(row).numRequired;
                }
                else if (col == 2) {
                    return materialsList.get(row).numAvailable;
                }
                else return null;
            }
            else return null;
        }
        
        /**
         * Update the table.
         */
        private void update() {
            info = (ConstructionStageInfo) projectList.getSelectedValue();
            materialsList.clear();
            populateMaterialsList();
            fireTableStructureChanged();
        }
        
        /**
         * Populate the list of construction materials.
         */
        private void populateMaterialsList() {
            
            Inventory inv = getConstructionSettlement().getInventory();
            String selectedSite = (String) siteList.getSelectedValue();
            if ((info != null) && (selectedSite.indexOf(" unfinished") == -1)) {
                try {
                    // Add resources.
                    Iterator<AmountResource> i = info.getResources().keySet().iterator();
                    while (i.hasNext()) {
                        AmountResource resource = i.next();
                        double amountRequired = info.getResources().get(resource);
                        double amountAvailable = inv.getAmountResourceStored(resource, false);
                        materialsList.add(new ConstructionMaterial(resource.getName(), 
                                (int) amountRequired, (int) amountAvailable));
                    }
                    
                    // Add parts.
                    Iterator<Part> j = info.getParts().keySet().iterator();
                    while (j.hasNext()) {
                        Part part = j.next();
                        int numRequired = info.getParts().get(part);
                        int numAvailable = inv.getItemResourceNum(part);
                        materialsList.add(new ConstructionMaterial(part.getName(), numRequired, 
                                numAvailable));
                    }
                    
                    // Add vehicle attachment parts.
                    Map<Part, Integer> attachmentParts = new HashMap<Part, Integer>();
                    Iterator<ConstructionVehicleType> k = info.getVehicles().iterator();
                    while (k.hasNext()) {
                        ConstructionVehicleType vehicleType = k.next();
                        Iterator<Part> l = vehicleType.getAttachmentParts().iterator();
                        while (l.hasNext()) {
                            Part part = l.next();
                            int partNum = 1;
                            if (attachmentParts.containsKey(part)) partNum += attachmentParts.get(part);
                            attachmentParts.put(part, partNum);
                        }
                    }
                    Iterator<Part> m = attachmentParts.keySet().iterator();
                    while (m.hasNext()) {
                        Part part = m.next();
                        int numRequired = attachmentParts.get(part);
                        int numAvailable = inv.getItemResourceNum(part);
                        materialsList.add(new ConstructionMaterial(part.getName(), numRequired, 
                                numAvailable));
                    }
                    
                    // Add construction vehicles.
                    int numVehiclesRequired = info.getVehicles().size();
                    int numVehiclesAvailable = inv.findNumUnitsOfClass(LightUtilityVehicle.class);
                    materialsList.add(new ConstructionMaterial("light utility vehicle", numVehiclesRequired, 
                            numVehiclesAvailable));
                }
                catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        }
        
        /**
         * Is the table cell a failure?
         * @param row the table row.
         * @param col the table column.
         * @return true if cell is a failure.
         */
        private boolean isFailureCell(int row, int col) {
            boolean result = false;
            
            if (col == 2) {
                if (row < materialsList.size()) {
                    ConstructionMaterial material = materialsList.get(row);
                    if (material.numRequired > material.numAvailable) result = true;
                }
            }
            
            return result;
        }
        
        /**
         * Inner class to represent table rows of construction material.
         */
        private class ConstructionMaterial {
            
            // Data members
            private String name;
            private int numRequired;
            private int numAvailable;
            
            private ConstructionMaterial(String name, int numRequired, int numAvailable) {
                this.name = name;
                this.numRequired = numRequired;
                this.numAvailable = numAvailable;
            }
            
            @Override
            public String toString() {
                return name;
            }
        }
    }
}