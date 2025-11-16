/*
 * Mars Simulation Project
 * ConstructionPanel.java
 * @date 2023-07-24
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.mission.objectives;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.building.construction.ConstructionStage;
import com.mars_sim.core.building.construction.ConstructionStage.Material;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.mission.objectives.ConstructionObjective;
import com.mars_sim.core.person.ai.mission.MissionEvent;
import com.mars_sim.core.person.ai.mission.MissionListener;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.tool.mission.ObjectivesPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.ConstructionStageFormat;

/**
 * A panel for displaying construction custom mission information.
 */
@SuppressWarnings("serial")
public class ConstructionPanel extends JPanel implements MissionListener, ObjectivesPanel, EntityListener {

    private MaterialsTableModel materialsTableModel;
    private JScrollPane scrollPane;
    private ConstructionObjective objective;
    private JLabel stageLabel;
    private JLabel workRemaining;

    /**
     * Constructor.
     * 
     * @param desktop the main desktop panel.
     */
    public ConstructionPanel(ConstructionObjective objective, MainDesktopPane desktop) {
        // Use MissionCustomInfoPanel constructor.
        super();
        setName(objective.getName());
        this.objective = objective;

        // Set layout.
        setLayout(new BorderLayout());

        AttributePanel infoPanel = new AttributePanel();
        add(infoPanel, BorderLayout.NORTH);

        var site = objective.getSite();

        String siteLabelString = Msg.getString("ConstructionMissionCustomInfoPanel.titleLabel"); //-NLS-1$
        infoPanel.addLabelledItem(siteLabelString, new EntityLabel(site, desktop));

        var stage = objective.getStage();
        String stageLabelString = Msg.getString("ConstructionMissionCustomInfoPanel.stageLabel"); //-NLS-1$
        stageLabel = infoPanel.addTextField(stageLabelString, stage.getInfo().getName(),
                        ConstructionStageFormat.getTooltip(stage));
        infoPanel.addTextField("Work Type", site.isConstruction() ? "Build" : "Demolish", null);

        workRemaining = infoPanel.addTextField("Work Remaining", "",  null);
         
        // Create remaining construction materials label panel.
        JPanel remainingMaterialsLabelPane = new JPanel(new BorderLayout(1, 1));
        add(remainingMaterialsLabelPane, BorderLayout.CENTER);
        
        // Create remaining construction materials label.
        String remainingMaterialsLabelString = Msg.getString("ConstructionMissionCustomInfoPanel.constructionMaterials"); //$NON-NLS-1$
        Border blackline = StyleManager.createLabelBorder(remainingMaterialsLabelString);
        remainingMaterialsLabelPane.setBorder(blackline);
        
        // Create the materials table and model.
        materialsTableModel = new MaterialsTableModel(stage.isConstruction());
        JTable materialsTable = new JTable(materialsTableModel);

        // Create a scroll pane for the remaining construction materials table.
        scrollPane = new JScrollPane();
        remainingMaterialsLabelPane.add(scrollPane);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(-1, 100));  
        scrollPane.setViewportView(materialsTable);

        // Update remaining construction materials table.
        materialsTableModel.updateTable();

        site.addEntityListener(this);    
        
        updateProgressBar();
    }

    @Override
    public void missionUpdate(MissionEvent event) {
        if (materialsTableModel != null) {
            materialsTableModel.updateTable();
        }
    }

    /**
     * Catches construction update event.
     * 
     * @param event the mission event.
     */
    @Override
    public void entityUpdate(EntityEvent event) {
        if (EntityEventType.ADD_CONSTRUCTION_WORK_EVENT == event.getType()) {
            // Update the progress bar
            updateProgressBar();

        }
        else if (EntityEventType.ADD_CONSTRUCTION_MATERIALS_EVENT == event.getType()
                && materialsTableModel != null) {
            // Update remaining construction materials table.
            materialsTableModel.updateTable();
        }
    }

    /**
     * Updates the progress bar.
     */
    private void updateProgressBar() {
        ConstructionStage stage = objective.getStage();
        if (stage != null) {
            double workLeft = stage.getRequiredWorkTime() - stage.getCompletedWorkTime();
            workRemaining.setText(StyleManager.DECIMAL2_MSOL.format(workLeft));
        
            // Update the tool tip string.
            stageLabel.setToolTipText(ConstructionStageFormat.getTooltip(stage));
        }
    }

  

    /**
     * Model for the construction materials table.
     */
    private final class MaterialsTableModel extends AbstractTableModel {

        /** default serial id. */
        private static final long serialVersionUID = 1L;

        private static final String[] CONS_LABELS = {Msg.getString("ConstructionMissionCustomInfoPanel.column.material"),
                                                    Msg.getString("ConstructionMissionCustomInfoPanel.column.type"),
                                                    Msg.getString("ConstructionMissionCustomInfoPanel.column.original"),
                                                    Msg.getString("ConstructionMissionCustomInfoPanel.column.available"),
                                                    Msg.getString("ConstructionMissionCustomInfoPanel.column.missing")};
        private static final String[] SALV_LABELS = {Msg.getString("ConstructionMissionCustomInfoPanel.column.material"),
                                                    Msg.getString("ConstructionMissionCustomInfoPanel.column.type"),
                                                    Msg.getString("ConstructionMissionCustomInfoPanel.column.available"),
                                                    "Reclaimed"};

        // Data members.
        private Map<Good, Material> materials;
        private List<Good> goodsList;

        private String[] columns;

        /**
         * Constructor.
         */
        private MaterialsTableModel(boolean construction) {
            this.columns = (construction ? CONS_LABELS : SALV_LABELS);

            // Initialize goods map and list.
            goodsList = new ArrayList<>();
            materials = new HashMap<>();
        }

        /**
         * Returns the number of rows in the model.
         * 
         * @return number of rows.
         */
        @Override
        public int getRowCount() {
            return goodsList.size();
        }

        /**
         * Returns the number of columns in the model.
         * 
         * @return number of columns.
         */
        @Override
        public int getColumnCount() {
            return columns.length;
        }

        /**
         * Returns the name of the column at columnIndex.
         * 
         * @param columnIndex the column index.
         * @return column name.
         */
        @Override
        public String getColumnName(int columnIndex) {
            return columns[columnIndex];
        }

        /**
         * Returns the value for the cell at columnIndex and rowIndex.
         * 
         * @param row the row whose value is to be queried.
         * @param column the column whose value is to be queried.
         * @return the value Object at the specified cell.
         */
        @Override
        public Object getValueAt(int row, int column) {
            if (row < goodsList.size()) {
                Good good = goodsList.get(row);
                return switch (column) {
                  case 0 ->  good.getName();
                  case 1 ->  good.getCategory().getName();
                  case 2 -> materials.get(good).getRequired();
                  case 3 -> materials.get(good).getAvailable();
                  default -> materials.get(good).getMissing();
                };
            }

            return null;
        }

        /**
         * Updates the table data.
         */
        protected void updateTable() {
        	
            // Populate originalMap.
            ConstructionStage stage = objective.getStage();
            if (stage != null) {
                materials = new HashMap<>();
                materials.putAll(stage.getParts().entrySet().stream()
                        .collect(Collectors.toMap(e -> GoodsUtil.getGood(e.getKey()), Entry::getValue)));
               
                materials.putAll(stage.getResources().entrySet().stream()
                        .collect(Collectors.toMap(e -> GoodsUtil.getGood(e.getKey()), Entry::getValue)));

                goodsList = new ArrayList<>(materials.keySet());
                Collections.sort(goodsList);
            }

            fireTableDataChanged();
        }
    }

    /**
     * Stop listening to event on the Site
     */
    @Override
    public void unregister() {
        objective.getSite().addEntityListener(this);    
    }
}
