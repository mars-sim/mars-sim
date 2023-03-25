/*
 * Mars Simulation Project
 * ConstructionProjectPanel.java
 * @date 2022-08-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.mission.create;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStage;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.core.structure.construction.ConstructionUtil;
import org.mars_sim.msp.core.structure.construction.ConstructionVehicleType;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.vehicle.VehicleType;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

/**
 * A wizard panel for selecting the mission's
 * construction project information.
 */
@SuppressWarnings("serial")
class ConstructionProjectPanel extends WizardPanel {

	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(ConstructionProjectPanel.class.getName());
	
    /** The wizard panel name. */
    private final static String NAME = "Construction Project";

    // Data members
    private JLabel errorMessageTextPane;
    private DefaultListModel<String> siteListModel;
    private JList<String> siteList;
    private DefaultListModel<ConstructionStageInfo> projectListModel;
    private JList<ConstructionStageInfo> projectList;
    private MaterialsTableModel materialsTableModel;
    private JTable materialsTable;
    private CreateMissionWizard cMWizard;
    
    /**
     * Constructor.
     * @param wizard the create mission wizard.
     */
    public ConstructionProjectPanel(final CreateMissionWizard wizard) {
        // Use WizardPanel constructor.
        super(wizard);
        this.cMWizard  = wizard;
        
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
                "Select Construction Site", JLabel.CENTER);
        constructionSiteSelectionPane.add(constructionSiteSelectionLabel,
                BorderLayout.NORTH);

        // Create scroll pane for site selection list.
        JScrollPane siteListScrollPane = new JScrollPane();
        siteListScrollPane
        .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        constructionSiteSelectionPane.add(siteListScrollPane,
                BorderLayout.CENTER);

        // Create site selection list.
        siteListModel = new DefaultListModel<String>();
        populateSiteListModel();
        siteList = new JList<String>(siteListModel);
        siteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        siteList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent arg0) {
                getWizard().setButtons(false);
                errorMessageTextPane.setText(" ");
                populateProjectListModel();
            }
        });
        siteListScrollPane.setViewportView(siteList);

        // Create construction project selection panel.
        JPanel constructionProjectSelectionPane = new JPanel(new BorderLayout(
                0, 0));
        constructionProjectSelectionPane.setBorder(new MarsPanelBorder());
        mainSelectionPane.add(constructionProjectSelectionPane);

        // Create construction project selection label.
        JLabel constructionProjectSelectionLabel = new JLabel(
                "Select Construction Project", JLabel.CENTER);
        constructionProjectSelectionPane.add(constructionProjectSelectionLabel,
                BorderLayout.NORTH);

        // Create scroll pane for project selection list.
        JScrollPane projectListScrollPane = new JScrollPane();
        projectListScrollPane
        .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        constructionProjectSelectionPane.add(projectListScrollPane,
                BorderLayout.CENTER);

        // Create project selection list.
        projectListModel = new DefaultListModel<ConstructionStageInfo>();
        populateProjectListModel();
        projectList = new JList<ConstructionStageInfo>(projectListModel);
        projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent arg0) {
                materialsTableModel.update();
                projectSelection();
            }
        });
        
        // call it a click to next button when user double clicks the table
        projectList.addMouseListener(new MouseListener() {
            public void mouseReleased(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !e.isConsumed()) {
                    wizard.buttonClickedNext();
                }
            }
        });
        projectListScrollPane.setViewportView(projectList);

        // Create construction materials panel.
        JPanel constructionMaterialsPane = new JPanel(new BorderLayout(0, 0));
        constructionMaterialsPane.setBorder(new MarsPanelBorder());
        centerPane.add(constructionMaterialsPane, BorderLayout.CENTER);

        // Create construction materials label.
        JLabel constructionMaterialsLabel = new JLabel(
                "Construction Materials Required", JLabel.CENTER);
        constructionMaterialsPane.add(constructionMaterialsLabel,
                BorderLayout.NORTH);

        // Create scroll pane for construction materials table.
        JScrollPane materialsTableScrollPane = new JScrollPane();
        materialsTableScrollPane
        .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                Component result = super
                        .getTableCellRendererComponent(table, value,
                                isSelected, hasFocus, row, column);

                // If failure cell, mark background red.
                MaterialsTableModel tableModel = (MaterialsTableModel) table
                        .getModel();
                if (tableModel.isFailureCell(row, column))
                    result.setBackground(Color.RED);
                else if (tableModel.isWarningCell(row, column)) {
                    result.setBackground(Color.YELLOW);
                }

                return result;
            }
        });
        materialsTableScrollPane.setViewportView(materialsTable);
        
        // Create the error message text pane.
        errorMessageTextPane = createErrorLabel();
        add(errorMessageTextPane, BorderLayout.SOUTH);
    }
    
    /**
     * Perform project selection action.
     */
    private void projectSelection() {
        
        String selectedSite = (String) siteList.getSelectedValue();
        ConstructionStageInfo stageInfo = (ConstructionStageInfo) projectList
                .getSelectedValue();
        projectList.setToolTipText(getToolTipText(stageInfo));
        if (stageInfo != null) {
            if (selectedSite.contains(" Unfinished")) {
                
                // Get construction site.
                Settlement settlement = getConstructionSettlement();
                ConstructionManager manager = settlement.getConstructionManager();
                ConstructionSite site = null;
                int selectedSiteIndex = siteList.getSelectedIndex();
                if (selectedSiteIndex > 0) {
                    int existingSiteIndex = selectedSiteIndex - 1;
                    site = manager.getConstructionSites()
                            .get(existingSiteIndex);
                }
                
                if (!hasConstructionVehicles(stageInfo)) {
                    getWizard().setButtons(false);
                    errorMessageTextPane.setText("Not enough vehicles and/or attachment " +
                            "parts at settlement for construction project.");
                }
                else if (site != null && !hasEnoughRemainingConstructionMaterials(site)) {
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
                try {
                    if (!hasConstructionVehicles(stageInfo)) {
                        getWizard().setButtons(false);
                        errorMessageTextPane.setText("Not enough vehicles and/or attachment " +
                                "parts at settlement for construction project.");
                    }
                    else if (!hasEnoughConstructionMaterials(stageInfo)) {
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
                } catch (Exception e) {
        			logger.severe(getConstructionSettlement(), 
        					"Error with construction materials: ", e);
                }
            }
        } 
        else {
            errorMessageTextPane.setText(" ");
        }
    }

    @Override
    void clearInfo() {
        siteListModel.clear();
        projectListModel.clear();
        materialsTableModel.update();
        getWizard().setButtons(false);
        errorMessageTextPane.setText(" ");
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
            selectedSite = manager.getConstructionSites()
                    .get(existingSiteIndex);
        }
        getWizard().getMissionData().setConstructionSite(selectedSite);

        // Get construction stage info.
        ConstructionStageInfo selectedInfo = (ConstructionStageInfo) projectList
                .getSelectedValue();
        getWizard().getMissionData().setConstructionStageInfo(selectedInfo);

        getWizard().getMissionData().setDescription(selectedInfo.getName());
        
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
     * TODO internationalize the construction process tooltips.
     */
    private String getToolTipText(ConstructionStageInfo stageInfo) {
        String result = null;
        if (stageInfo != null) {
            if (!stageInfo.getType().equals(ConstructionStageInfo.BUILDING)) {
                try {
                    StringBuilder s = new StringBuilder(Msg.HTML_START); //$NON-NLS1$
                    s.append("Next possible stages:");
                    Iterator<ConstructionStageInfo> i = ConstructionUtil
                            .getNextPossibleStages(stageInfo).iterator();
                    while (i.hasNext()) {
                        s.append(Msg.BR)
                        .append(Msg.NBSP)
                        .append(Msg.NBSP)
                        .append(Msg.NBSP)
                        .append(Msg.NBSP)
                        .append(i.next().getName());
                    }
                    s.append(Msg.HTML_STOP); //$NON-NLS1$
                    result = s.toString();
                } catch (Exception e) {
        			logger.severe(getConstructionSettlement(), 
        					"Error with setting up tooltip: ", e);
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

        int num = 1;
        
        Settlement settlement = getConstructionSettlement();
        if (settlement != null) {
            ConstructionManager manager = settlement.getConstructionManager();
            Iterator<ConstructionSite> i = manager.getConstructionSites()
                    .iterator();
            while (i.hasNext()) {
                ConstructionSite site = i.next();
                ConstructionStage stage = site.getCurrentConstructionStage();
                if (site.isUndergoingConstruction()) {
                    siteListModel.addElement("Site " + num + " : " + stage
                            + " - Under Construction");
                } else if (site.isUndergoingSalvage()) {
                    siteListModel.addElement("Site " + num + " : " + stage
                            + " - Under Salvage");
                } else if (site.hasUnfinishedStage()) {
                    if (stage.isSalvaging())
                        siteListModel.addElement("Site " + num + " : " + stage
                                + " - Salvage Unfinished");
                    else
                        siteListModel.addElement("Site " + num + " : " + stage
                                + " - Construction Unfinished");
                } else {
                    siteListModel.addElement("Site " + num + " : " + stage);
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
                    Iterator<ConstructionStageInfo> i = ConstructionUtil
                            .getFoundationConstructionStageInfoList()
                            .iterator();
                    while (i.hasNext()) {
                        ConstructionStageInfo info = i.next();
                        if (info.isConstructable())
                            projectListModel.addElement(info);
                    }
                } catch (Exception e) {
        			logger.severe(getConstructionSettlement(), 
        					"Error checking construction stage info: ", e);
                }
            } else if (selectedSite.contains(" - Under Construction")) {
            	if (cMWizard.getMissionBean().getMixedMembers() == null)
            		if (cMWizard.getMissionBean().getMixedMembers().isEmpty()) {
            			// Add checking if members of an on-going site were departed
            			loadSite(selectedSite, selectedSiteIndex);
            			cMWizard.getMissionWindow().update((ClockPulse) null);
            		}
            	else {            	
	                errorMessageTextPane.setText("Cannot start mission on a site already undergoing construction.");             
	                // Do nothing.
            	}
            } else if (selectedSite.contains(" - Under Salvage")) {
                errorMessageTextPane.setText("Cannot start mission on a site already undergoing salvage.");
                // Do nothing.
            } else {
            	loadSite(selectedSite, selectedSiteIndex);
            }
        }
    }

    public void loadSite(String selectedSite, int selectedSiteIndex) {
        Settlement settlement = getConstructionSettlement();
        if (settlement != null) {
            ConstructionManager manager = settlement
                    .getConstructionManager();
            int siteNum = selectedSiteIndex - 1;
            ConstructionSite site = manager.getConstructionSites().get(
                    siteNum);
            if (site != null) {
                if (selectedSite.contains(" Unfinished")) {
                    // Show current construction stage.
                    projectListModel.addElement(site
                            .getCurrentConstructionStage().getInfo());
                } else {
                    try {
                        // Show all possible stage infos.
                        ConstructionStageInfo info = site
                                .getCurrentConstructionStage()
                                .getInfo();
                        Iterator<ConstructionStageInfo> i = ConstructionUtil
                                .getNextPossibleStages(info).iterator();
                        while (i.hasNext()) {
                            ConstructionStageInfo stageInfo = i.next();
                            if (stageInfo.isConstructable())
                                projectListModel.addElement(stageInfo);
                        }
                    } catch (Exception e) {
            			logger.severe(getConstructionSettlement(), 
            					"Error checking construction stage info: ", e);
                    }
                }
            }
        }	
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
     * Checks if there are enough construction materials at the settlement to
     * construct the stage.
     * 
     * @param stageInfo
     *            the stage information.
     * @return true if enough materials.
     * @throws Exception
     *             if error determining construction material availability.
     */
    private boolean hasEnoughConstructionMaterials(
            ConstructionStageInfo stageInfo) {

        boolean result = true;

        Settlement settlement = getConstructionSettlement();
 
        // Check amount resources.
        Iterator<Integer> i = stageInfo.getResources().keySet()
                .iterator();
        while (i.hasNext()) {
        	Integer resource = i.next();
            double amount = stageInfo.getResources().get(resource);
            if (settlement.getAmountResourceStored(resource) < amount)
                result = false;
        }

        // Check parts.
        Iterator<Integer> j = stageInfo.getParts().keySet().iterator();
        while (j.hasNext()) {
        	Integer part = j.next();
            int number = stageInfo.getParts().get(part);
            if (settlement.getItemResourceStored(part) < number)
                result = false;
        }

        return result;
    }
    
    /**
     * Checks if there are enough remaining construction materials for a construction site.
     * 
     * @param site the construction site.
     * @return true if enough remaining materials available.
     */
    private boolean hasEnoughRemainingConstructionMaterials(ConstructionSite site) {
        
        boolean result = true;
        
        Settlement settlement = getConstructionSettlement();
 
        ConstructionStage stage = site.getCurrentConstructionStage();
        if (stage != null) {
        
            // Check amount resources.
            Iterator<Integer> i = stage.getRemainingResources().keySet()
                    .iterator();
            while (i.hasNext()) {
            	Integer resource = i.next();
                double amount = stage.getRemainingResources().get(resource);
                if (settlement.getAmountResourceStored(resource) < amount) {
                    result = false;
                }
            }

            // Check parts.
            Iterator<Integer> j = stage.getRemainingParts().keySet().iterator();
            while (j.hasNext()) {
            	Integer part = j.next();
                int number = stage.getRemainingParts().get(part);
                if (settlement.getItemResourceStored(part) < number) {
                    result = false;
                }
            }
        }

        return result;
    }

    /**
     * Checks if needed construction vehicles and attachment parts are available.
     * @param stageInfo the construction stage.
     * @return true if construction vehicles and attachment parts are available.
     */
    private boolean hasConstructionVehicles(ConstructionStageInfo stageInfo) {

        boolean result = true;

        Settlement settlement = getWizard().getMissionData()
                .getConstructionSettlement();
 
        // Check for LUV's.
        int luvsNeeded = stageInfo.getVehicles().size();
        int luvsAvailable = settlement.findNumVehiclesOfType(VehicleType.LUV);
        if (luvsAvailable < luvsNeeded)
            result = false;

        // Check for LUV attachment parts.
        Map<Integer, Integer> attachmentParts = new HashMap<Integer, Integer>();
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

            materialsList = new ArrayList<ConstructionMaterial>();
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
                if (col == 0) {
                    return materialsList.get(row).toString();
                } else if (col == 1) {
                    return materialsList.get(row).numRequired;
                } else if (col == 2) {
                    return materialsList.get(row).numAvailable;
                } else
                    return null;
            } else
                return null;
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

            String selectedSite = (String) siteList.getSelectedValue();
            // Get construction site.
            Settlement settlement = getConstructionSettlement();
            
            if (info != null) {
                
                if (selectedSite.indexOf(" Unfinished") > 0) {
                    
                    try {
                        // For site with stage under construction, display remaining
                        // construction resources and parts.
                        ConstructionManager manager = settlement.getConstructionManager();
                        ConstructionSite site = null;
                        int selectedSiteIndex = siteList.getSelectedIndex();
                        if (selectedSiteIndex > 0) {
                            int existingSiteIndex = selectedSiteIndex - 1;
                            site = manager.getConstructionSites()
                                    .get(existingSiteIndex);
                        }
                        else {
                            logger.severe(settlement, 
                                    "No site selected for Construction Project");
                            return;
                        }   
                        ConstructionStage stage = site.getCurrentConstructionStage();
                        
                        // Add resources.
                        Iterator<Integer> i = stage.getRemainingResources().keySet()
                                .iterator();
                        while (i.hasNext()) {
                        	Integer resource = i.next();
                            double amountRequired = info.getResources().get(
                                    resource);
                            double amountAvailable = settlement.getAmountResourceStored(resource);
                            materialsList.add(new ConstructionMaterial(
                            		ResourceUtil.findAmountResource(resource).getName(), (int) amountRequired,
                                    (int) amountAvailable, false));
                        }
                        
                        // Add parts.
                        Iterator<Integer> j = stage.getRemainingParts().keySet().iterator();
                        while (j.hasNext()) {
                        	Integer part = j.next();
                            int numRequired = info.getParts().get(part);
                            int numAvailable = settlement.getItemResourceStored(part);
                            materialsList.add(new ConstructionMaterial(
                            		ItemResourceUtil.findItemResource(part).getName(), 
                            		numRequired, numAvailable, false));
                        }

                        // Add vehicle attachment parts.
                        Map<Integer, Integer> attachmentParts = new HashMap<Integer, Integer>();
                        Iterator<ConstructionVehicleType> k = info.getVehicles()
                                .iterator();
                        while (k.hasNext()) {
                            ConstructionVehicleType vehicleType = k.next();
                            Iterator<Integer> l = vehicleType.getAttachmentParts()
                                    .iterator();
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
                            int numRequired = attachmentParts.get(part);
                            int numAvailable = settlement.getItemResourceStored(part);
                            materialsList.add(new ConstructionMaterial(ItemResourceUtil.findItemResource(part)
                                    .getName(), numRequired, numAvailable, true));
                        }

                        // Add construction vehicles.
                        int numVehiclesRequired = info.getVehicles().size();
                        int numVehiclesAvailable = settlement.findNumVehiclesOfType(VehicleType.LUV);
                        materialsList.add(new ConstructionMaterial(
                                "light utility vehicle", numVehiclesRequired,
                                numVehiclesAvailable, true));
                    }
                    catch (Exception e) {
            			logger.severe(settlement, 
            					"Error checking construction stage info: ", e);
                    }
                }
                else {
                    try {
                        // Add resources.
                        Iterator<Integer> i = info.getResources().keySet()
                                .iterator();
                        while (i.hasNext()) {
                        	Integer resource = i.next();
                            double amountRequired = info.getResources().get(
                                    resource);
                            double amountAvailable = settlement.getAmountResourceStored(resource);
                            materialsList.add(new ConstructionMaterial(ResourceUtil.findAmountResource(resource)
                                    .getName(), (int) amountRequired,
                                    (int) amountAvailable, false));
                        }

                        // Add parts.
                        Iterator<Integer> j = info.getParts().keySet().iterator();
                        while (j.hasNext()) {
                        	Integer part = j.next();
                            int numRequired = info.getParts().get(part);
                            int numAvailable = settlement.getItemResourceStored(part);
                            materialsList.add(new ConstructionMaterial(ItemResourceUtil.findItemResource(part)
                                    .getName(), numRequired, numAvailable, false));
                        }

                        // Add vehicle attachment parts.
                        Map<Integer, Integer> attachmentParts = new HashMap<Integer, Integer>();
                        Iterator<ConstructionVehicleType> k = info.getVehicles()
                                .iterator();
                        while (k.hasNext()) {
                            ConstructionVehicleType vehicleType = k.next();
                            Iterator<Integer> l = vehicleType.getAttachmentParts()
                                    .iterator();
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
                            int numRequired = attachmentParts.get(part);
                            int numAvailable = settlement.getItemResourceStored(part);
                            materialsList.add(new ConstructionMaterial(ItemResourceUtil.findItemResource(part)
                                    .getName(), numRequired, numAvailable, true));
                        }

                        // Add construction vehicles.
                        int numVehiclesRequired = info.getVehicles().size();
                        int numVehiclesAvailable = settlement.findNumVehiclesOfType(VehicleType.LUV);
                        materialsList.add(new ConstructionMaterial(
                                "light utility vehicle", numVehiclesRequired,
                                numVehiclesAvailable, true));
                    } catch (Exception e) {
            			logger.severe(settlement, 
            					"Error preparing for construction resources: ", e);
                    }
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
                    if (material.isVehicleRelated) {
                        if (material.numRequired > material.numAvailable) {
                            result = true;
                        }
                    }
                }
            }

            return result;
        }
        
        /**
         * Checks if the table cell is a warning.
         * @param row the table row.
         * @param col the table column.
         * @return true if cell is a warning.
         */
        private boolean isWarningCell(int row, int col) {
            boolean result = false;
            
            if (col == 2) {
                if (row < materialsList.size()) {
                    ConstructionMaterial material = materialsList.get(row);
                    if (!material.isVehicleRelated) {
                        if (material.numRequired > material.numAvailable) {
                            result = true;
                        }
                    }
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
            private boolean isVehicleRelated;

            private ConstructionMaterial(String name, int numRequired,
                    int numAvailable, boolean isVehicleRelated) {
                this.name = name;
                this.numRequired = numRequired;
                this.numAvailable = numAvailable;
                this.isVehicleRelated = isVehicleRelated;
            }

            @Override
            public String toString() {
                return name;
            }
        }
    }
}
