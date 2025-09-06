/*
 * Mars Simulation Project
 * ConstructionProjectPanel.java
 * @date 2023-07-24
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.mission.create;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
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
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import com.mars_sim.core.building.construction.ConstructionManager;
import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.building.construction.ConstructionStage;
import com.mars_sim.core.building.construction.ConstructionStageInfo;
import com.mars_sim.core.building.construction.ConstructionVehicleType;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.VehicleType;
import com.mars_sim.ui.swing.MarsPanelBorder;

/**
 * A wizard panel for selecting the mission's
 * construction project information.
 */
@SuppressWarnings("serial")
class ConstructionProjectPanel extends WizardPanel {
	
    /** The wizard panel name. */
    private static final String NAME = "Construction Project";

    private static final String LUV = "Light Utility Vehicle";
    
    // Data members
    private JLabel errorMessageTextPane;
    private DefaultListModel<ConstructionSite> siteListModel;
    private JList<ConstructionSite> siteList;
    private MaterialsTableModel materialsTableModel;
    private JTable materialsTable;
    
    /**
     * Constructor.
     * 
     * @param wizard the create mission wizard.
     */
    public ConstructionProjectPanel(final CreateMissionWizard wizard) {
        // Use WizardPanel constructor.
        super(wizard);
        
        // Set the layout.
        setLayout(new BorderLayout(0, 0));

        // Set the border.
        setBorder(new MarsPanelBorder());

        // Create the select construction project label.
        JLabel titleLabel = createTitleLabel("Select a construction project");
        add(titleLabel, BorderLayout.NORTH);

        // Create the center panel.
        JPanel centerPane = new JPanel(new BorderLayout(0, 0));
        add(centerPane, BorderLayout.CENTER);

        // Create main selection panel.
        JPanel mainSelectionPane = new JPanel(new GridLayout(1, 2));
        mainSelectionPane.setPreferredSize(new Dimension(-1, 200));
        centerPane.add(mainSelectionPane, BorderLayout.NORTH);

        // Create construction site selection panel.
        JPanel constructionSiteSelectionPane = new JPanel(
                new BorderLayout(0, 0));
        constructionSiteSelectionPane.setBorder(new MarsPanelBorder());
        mainSelectionPane.add(constructionSiteSelectionPane);

        // Create construction site selection label.
        JLabel constructionSiteSelectionLabel = new JLabel(
                "Select Construction Site", SwingConstants.CENTER);
        constructionSiteSelectionPane.add(constructionSiteSelectionLabel,
                BorderLayout.NORTH);

        // Create scroll pane for site selection list.
        JScrollPane siteListScrollPane = new JScrollPane();
        siteListScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        constructionSiteSelectionPane.add(siteListScrollPane, BorderLayout.CENTER);

        // Create site selection list.
        siteListModel = new DefaultListModel<>();
        populateSiteListModel();
        siteList = new JList<>(siteListModel);
        siteList.setCellRenderer(new SiteListRenderer());
        siteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        siteList.addListSelectionListener(arg0 -> {
            getWizard().setButtons(false);
            errorMessageTextPane.setText(" ");
            selectSite();
        });
        siteListScrollPane.setViewportView(siteList);

        // Create construction materials panel.
        JPanel constructionMaterialsPane = new JPanel(new BorderLayout(0, 0));
        constructionMaterialsPane.setBorder(new MarsPanelBorder());
        centerPane.add(constructionMaterialsPane, BorderLayout.CENTER);

        // Create construction materials label.
        JLabel constructionMaterialsLabel = new JLabel(
                "Construction Materials Required", SwingConstants.CENTER);
        constructionMaterialsPane.add(constructionMaterialsLabel,
                BorderLayout.NORTH);

        // Create scroll pane for construction materials table.
        JScrollPane materialsTableScrollPane = new JScrollPane();
        materialsTableScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        constructionMaterialsPane.add(materialsTableScrollPane,
                BorderLayout.CENTER);

        // Create the materials table model.
        materialsTableModel = new MaterialsTableModel();
 
        // Create the materials table.
        materialsTable = new JTable(materialsTableModel);
		materialsTable.setAutoCreateRowSorter(true);
        materialsTable.setRowSelectionAllowed(false);
        materialsTable.setDefaultRenderer(Object.class,
                new DefaultTableCellRenderer() {
            /** default serial id. */
            private static final long serialVersionUID = 1L;
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
            	// Clear the background from previous error cell
        		super.setBackground(null); 

                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // If failure cell, mark background red.
                MaterialsTableModel tableModel = (MaterialsTableModel) table
                        .getModel();
                if (tableModel.isFailureCell(row, column)) 
                    l.setBackground(Color.RED);		
                else if (tableModel.isWarningCell(row, column)) {
                    l.setBackground(Color.YELLOW);
                }

                return this;
            }
        });
        materialsTableScrollPane.setViewportView(materialsTable);
        
        // Create the error message text pane.
        errorMessageTextPane = createErrorLabel();
        add(errorMessageTextPane, BorderLayout.SOUTH);
    }
    
   
    @Override
    void clearInfo() {
        siteListModel.clear();
        materialsTableModel.update();
        getWizard().setButtons(false);
        errorMessageTextPane.setText(" ");
    }

    
	/**
	 * Commits changes from this wizard panel.
	 * 
	 * @param isTesting true if it's only testing conditions
	 * @return true if changes can be committed.
	 */
    @Override
    boolean commitChanges(boolean isTesting) {

        // Get construction site.
        ConstructionSite site = siteList.getSelectedValue();
	    getWizard().getMissionData().setConstructionSite(site);

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
     * Populates the site list model.
     */
    private void populateSiteListModel() {
        siteListModel.clear();
        
        Settlement settlement = getConstructionSettlement();
        if (settlement != null) {
            ConstructionManager manager = settlement.getConstructionManager();
            manager.getConstructionSitesNeedingMission().forEach(cs -> siteListModel.addElement(cs));
        }
    }

    /**
     * Populates the project list model.
     */
    private void selectSite() {

        var selectedSite = siteList.getSelectedValue();
        if (selectedSite != null) {
            loadSite(selectedSite);
        }
        else {
            materialsTableModel.update();
        }
    }

    private void loadSite(ConstructionSite selectedSite) {
        var stage = selectedSite.getCurrentConstructionStage();
        if (stage != null) {
            var stageInfo = stage.getInfo();

            if (!hasConstructionVehicles(stageInfo)) {
                getWizard().setButtons(false);
                errorMessageTextPane.setText("Not enough vehicles and/or attachment " +
                        "parts at settlement for construction project.");
            }
            else if (stage.hasMissingConstructionMaterials()) {
                // Allow construction mission even when insufficient
                // materials available to finish stage.
                getWizard().setButtons(true);
                errorMessageTextPane.setText("Not enough materials at settlement " +
                        "to finish construction stage, but construction mission " +
                        "can still be started.");
            } 
            else {
                getWizard().setButtons(true);
                errorMessageTextPane.setText(" ");
            }
        } 
        else {
            errorMessageTextPane.setText(" ");
        }

        materialsTableModel.update();
    }
    
    
    /**
     * Gets the construction settlement.
     * 
     * @return settlement or null if none.
     */
    private Settlement getConstructionSettlement() {
        return getWizard().getMissionData().getConstructionSettlement();
    }

    /**
     * Checks if needed construction vehicles and attachment parts are available.
     * 
     * @param stageInfo the construction stage.
     * @return true if construction vehicles and attachment parts are available.
     */
    private boolean hasConstructionVehicles(ConstructionStageInfo stageInfo) {

        boolean result = true;

        Settlement settlement = getConstructionSettlement();
 
        // Check for LUV's.
        int luvsNeeded = stageInfo.getVehicles().size();
        int luvsAvailable = settlement.findNumVehiclesOfType(VehicleType.LUV);
        if (luvsAvailable < luvsNeeded)
            result = false;

        // Check for LUV attachment parts.
        Map<Integer, Integer> attachmentParts = new HashMap<>();
        Iterator<ConstructionVehicleType> k = stageInfo.getVehicles()
                .iterator();
        while (k.hasNext()) {
            ConstructionVehicleType vehicleType = k.next();
            Iterator<Integer> l = vehicleType.getAttachmentParts().iterator();
            while (l.hasNext()) {
            	Integer part = l.next();
                int partNum = 1;
                if (attachmentParts.containsKey(part))
                    partNum += attachmentParts.get(part);
                attachmentParts.put(part, partNum);
            }
        }
        Iterator<Integer> m = attachmentParts.keySet().iterator();
        while (m.hasNext()) {
        	Integer part = m.next();
            int number = attachmentParts.get(part);
            if (settlement.getItemResourceStored(part) < number)
                result = false;
        }

        return result;
    }

    /**
     * A table model for construction materials.
     */
    private class MaterialsTableModel extends AbstractTableModel {

        /** default serial id. */
        private static final long serialVersionUID = 1L;

        // Data members.
        private ConstructionStageInfo info = null;
        private List<ConstructionMaterial> materialsList;

        /** Constructor. */
        private MaterialsTableModel() {
            // Use AbstractTableModel constructor.
            super();

            materialsList = new ArrayList<>();
        }

        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int columnIndex) {
            String result = "";
            if (columnIndex == 0)
                result = "Construction Material";
            else if (columnIndex == 1)
                result = "Required by Project";
            else if (columnIndex == 2)
                result = "Available at Settlement";
            return result;
        }

        public int getRowCount() {
            return materialsList.size();
        }

        public Object getValueAt(int row, int col) {
            if ((row < materialsList.size()) && (col < 3)) {
                return switch (col) {
                  case 0 -> materialsList.get(row).name();
                  case 1 -> materialsList.get(row).numRequired();
                  case 2 -> materialsList.get(row).numAvailable();
                  default -> null;
                };
            } else
                return null;
        }

        /**
         * Updates the table.
         */
        private void update() {
            materialsList.clear();
            populateMaterialsList();
            fireTableDataChanged();
        }

        /**
         * Checks and populates site that are unfinished. 
         * 
         * @param settlement
         * @param selectedSite
         */
        private void populateSiteUnfinished(Settlement settlement, ConstructionSite site) {

            ConstructionStage stage = site.getCurrentConstructionStage();
                
            // Add resources.
            for(var mr : stage.getResources().entrySet()) {
                Integer resource = mr.getKey();
                double amountRequired = mr.getValue().getMissing();
                double amountAvailable = settlement.getSpecificAmountResourceStored(resource);
                materialsList.add(new ConstructionMaterial(
                        ResourceUtil.findAmountResource(resource).getName(), (int) amountRequired,
                        (int) amountAvailable, false));
            }
                
            // Add parts.
            for (var mp : stage.getParts().entrySet()) {
                Integer part = mp.getKey();
                int numRequired = (int) mp.getValue().getMissing();
                int numAvailable = settlement.getItemResourceStored(part);
                materialsList.add(new ConstructionMaterial(
                        ItemResourceUtil.findItemResource(part).getName(), 
                        numRequired, numAvailable, false));
            }

            // Add vehicle attachment parts.
            Map<Integer,Integer> attachmentParts = new HashMap<>();
            for (var vehicleType : info.getVehicles()) {
                for (var part : vehicleType.getAttachmentParts()) {
                    attachmentParts.merge(part, 1, Integer::sum);
                }

                for (var ap : attachmentParts.entrySet()) {
                	Integer part = ap.getKey();
                    int numRequired = ap.getValue();
                    int numAvailable = settlement.getItemResourceStored(part);
                    materialsList.add(new ConstructionMaterial(ItemResourceUtil.findItemResource(part)
                            .getName(), numRequired, numAvailable, true));
                }
            }

            // Add construction vehicles.
            int numVehiclesRequired = info.getVehicles().size();
            int numVehiclesAvailable = settlement.findNumVehiclesOfType(VehicleType.LUV);
            materialsList.add(new ConstructionMaterial(
                    LUV, numVehiclesRequired,
                    numVehiclesAvailable, true));
        }
        
        /**
         * Populates the list of construction materials.
         */
        private void populateMaterialsList() {

            var selectedSite = siteList.getSelectedValue();
            if (selectedSite != null) {
                // Get construction site.
                Settlement settlement = getConstructionSettlement();
                info = selectedSite.getCurrentConstructionStage().getInfo();
    
                populateSiteUnfinished(settlement, selectedSite);
            }
            else {
                materialsList.clear();
            }
        }

        /**
         * Is the table cell a failure?
         * 
         * @param row the table row.
         * @param col the table column.
         * @return true if cell is a failure.
         */
        private boolean isFailureCell(int row, int col) {
            boolean result = false;

            if (col == 2 && row < materialsList.size()) {
                ConstructionMaterial material = materialsList.get(row);
                if (material.isVehicleRelated && material.numRequired > material.numAvailable) {
                        result = true;
                    }
                
            }
            

            return result;
        }
        
        /**
         * Checks if the table cell is a warning.
         * 
         * @param row the table row.
         * @param col the table column.
         * @return true if cell is a warning.
         */
        private boolean isWarningCell(int row, int col) {
            boolean result = false;
            
            if (col == 2 && row < materialsList.size()) {
                ConstructionMaterial material = materialsList.get(row);
                if (!material.isVehicleRelated && material.numRequired > material.numAvailable) {
                    result = true;
                }            
            }
            
            return result;
        }

        /**
         * Inner class to represent table rows of construction material.
         */
        private record ConstructionMaterial(String name, int numRequired, int numAvailable, boolean isVehicleRelated) {
        }
    }

    private class SiteListRenderer extends JLabel implements
        ListCellRenderer<ConstructionSite> {

		public SiteListRenderer() {
			setOpaque(true);
		}

		@Override
		public Component getListCellRendererComponent(
				JList<? extends ConstructionSite> list,
				ConstructionSite site, int index, boolean isSelected,
				boolean cellHasFocus) {

			this.setFont(list.getFont());

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

            this.setText(site.getName() + " building " + site.getBuildingName() + " @ " + site.getStatusDescription());

			return this;
		}
	}
}
