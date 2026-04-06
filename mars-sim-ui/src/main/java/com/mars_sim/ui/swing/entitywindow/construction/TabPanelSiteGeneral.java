/**
 * Mars Simulation Project
 * TabPanelSiteGeneral.java
 * @date 2023-06-07
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.entitywindow.construction;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.building.construction.ConstructionSite.ConstructionPhase;
import com.mars_sim.core.building.construction.ConstructionStage;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.components.JDoubleLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.tool.svg.SVGMapUtil;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.ConstructionStageFormat;
import com.mars_sim.ui.swing.utils.ToolTipTableModel;

/**
 * The TabPanelSiteGeneral is a tab panel for general information about a construction site.
 */
@SuppressWarnings("serial")
class TabPanelSiteGeneral extends EntityTabPanel<ConstructionSite>
		implements EntityListener {

	private EntityLabel missionLabel;

	private PhaseTableModel phaseModel;
	private ConstructionStage currentStage;

	private JLabel stageName;
	private JLabel stageType;
	private JLabel workType;

	private JDoubleLabel workLeft;
	
	/**
	 * Constructor.
	 * 
	 * @param unit the unit to display.
	 * @param context the UI context.
	 */
	public TabPanelSiteGeneral(ConstructionSite unit, UIContext context) {
		super(
			GENERAL_TITLE,
			ImageLoader.getIconByName(GENERAL_ICON), GENERAL_TOOLTIP,	
			context, unit
		);
	}
	
	@Override
	protected void buildUI(JPanel content) {
		var constructionSite = getEntity();

		JPanel topPanel = new JPanel(new BorderLayout());
		content.add(topPanel, BorderLayout.NORTH);

		// Add SVG Image loading for the building
		JPanel svgPanel = SVGMapUtil.createBuildingPanel(constructionSite.getBuildingName(), 220, 110);
		topPanel.add(svgPanel, BorderLayout.NORTH);

		// Prepare spring layout info panel.
		AttributePanel infoPanel = new AttributePanel();
		topPanel.add(infoPanel, BorderLayout.CENTER);

		String name = constructionSite.getName();

		infoPanel.addTextField("Site Name", name, null);
		infoPanel.addTextField("Building Type", constructionSite.getBuildingName(), null);
		stageName = infoPanel.addTextField("Current Stage", "", null);
		stageType = infoPanel.addTextField("Current Stage Type", "", null);
		workType = infoPanel.addTextField("Current Work Type", "", null);

		missionLabel = new EntityLabel(constructionSite.getWorkOnSite(), getContext());
		infoPanel.addLabelledItem("Work Mission", missionLabel);
        workLeft = new JDoubleLabel(StyleManager.DECIMAL_MSOL);
		infoPanel.addLabelledItem("Stage Work", workLeft);

		phaseModel = new PhaseTableModel();
		var phaseTable = new JTable(phaseModel) {
			@Override
            public String getToolTipText(MouseEvent e) {
                return ToolTipTableModel.extractToolTip(e, this);
            }
		};
		phaseTable.setPreferredScrollableViewportSize(new Dimension(225, -1));

		var scrollPane = StyleManager.createScrollBorder("Remaining Phases", phaseTable);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		content.add(scrollPane, BorderLayout.CENTER);

		updateInfo();
	}

	
	@Override
	public void entityUpdate(EntityEvent event) {
		updateInfo();
	}

	/**
	 * Update the dynamic information in the panel.S
	 */
	private void updateInfo() {
		var constructionSite = getEntity();

		missionLabel.setEntity(constructionSite.getWorkOnSite());
		phaseModel.update(constructionSite.getRemainingPhases());

		ConstructionStage stage = constructionSite.getCurrentConstructionStage();
        if (stage != null) {
			double workRemaining = stage.getRequiredWorkTime() - stage.getCompletedWorkTime();
			workLeft.setValue(workRemaining);
        }

		var activeStage = constructionSite.getCurrentConstructionStage();
		if (!activeStage.equals(currentStage)) {
			currentStage = activeStage;

			var info = currentStage.getInfo();
			stageName.setText(info.getName());
			stageType.setText(info.getType().name().toLowerCase());
			workType.setText(constructionSite.isConstruction() ? "Construct" : "Demolish");
		}
	}

	/**
	 * Table model for construction phases.
	 */
	private static class PhaseTableModel extends AbstractTableModel 
						implements ToolTipTableModel {

		private List<ConstructionPhase> phases = Collections.emptyList();
		
		@Override
		public int getRowCount() {
			return phases.size();
		}

		public void update(List<ConstructionPhase> remainingPhases) {
			if (!phases.equals(remainingPhases)) {
				phases = remainingPhases;
				fireTableDataChanged();
			}
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public String getColumnName(int column) {	
			return switch (column) {
				case 0 -> "Stage";
				case 1 -> "Type";
				case 2 -> "Work";
				case 3 -> "Work Time";
				default -> null;
			};
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			var p = phases.get(rowIndex);
			return switch (columnIndex) {
				case 0 -> p.stageInfo().getName();
				case 1 -> p.stageInfo().getType().name().toLowerCase();
				case 2 -> p.construct() ? "Construct" : "Demolish";
				case 3 -> p.stageInfo().getWorkTime();
				default -> null;
			};
		}
		
		@Override
		public String getToolTipAt(int row, int col) {
			var selected = phases.get(row);

			return ConstructionStageFormat.getTooltip(selected.stageInfo());
		}
	}
}
