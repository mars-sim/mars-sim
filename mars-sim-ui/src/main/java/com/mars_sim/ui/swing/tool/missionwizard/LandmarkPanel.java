/*
 * Mars Simulation Project
 * LandmarkPanel.java
 * @date 2026-02-08
 * @author Barry Evans
 */

package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.List;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.environment.Landmark;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.utils.wizard.AbstractWizardItemModel;
import com.mars_sim.ui.swing.utils.wizard.WizardItemStep;
import com.mars_sim.ui.swing.utils.wizard.WizardPane;

/**
 * A wizard panel for selecting the mission Landmark.
 */
@SuppressWarnings("serial")
class LandmarkPanel extends WizardItemStep<MissionDataBean, Landmark> {

	/** The wizard panel name. */
	public static final String ID = "landmark";

	/**
	 * Constructor.
	 * 
	 * @param wizard the create mission wizard.
	 */
	LandmarkPanel(WizardPane<MissionDataBean> parent, MissionDataBean state) {
		// Use WizardPanel constructor.
		super(ID, parent, new LandmarkModel(state));
	}

	/**
	 * Clear information on the wizard panel.
	 */
	@Override
	public void clearState(MissionDataBean state) {
		state.setLandmark(null);
		super.clearState(state);
	}

	/**
	 * Update the state with the selected site
	 */
	@Override
	protected void updateState(MissionDataBean state, List<Landmark> sel) {
		state.setLandmark(sel.get(0));
	}

	/**
	 * A table model for reachable landmarks
	 */
	private static class LandmarkModel extends AbstractWizardItemModel<Landmark> {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		private static final List<ColumnSpec> COLUMNS = List.of(
				new ColumnSpec(Msg.getString("entity.name"), String.class),
				new ColumnSpec(Msg.getString("landmark.category"), String.class),
				new ColumnSpec(Msg.getString("entity.description"), String.class),
				new ColumnSpec(Msg.getString("entity.coordinates"), String.class),
				new ColumnSpec("Distance", Double.class, ColumnSpec.STYLE_DIGIT1));
		private Coordinates startPoint;
				
		/**
		 * Constructor
		 */
		private LandmarkModel(MissionDataBean state) {
			super(COLUMNS);
		
			startPoint = state.getStartingSettlement().getLocation();
			var range = state.getRover().getEstimatedRange();
			var rangeAsDegree = (range*1.1)/Coordinates.KM_PER_RADIAN_AT_EQUATOR;

			var surfaceFeatures = SimulationConfig.instance().getLandmarkConfiguration();
			var withinRange = surfaceFeatures.getLandmarks().getFeatures(startPoint, rangeAsDegree).stream()
					.filter(s -> s.getLocation().getDistance(startPoint) < range)
					.toList();
	
			setItems(withinRange);
		}

		/**	
		 * Returns the value for the table cell.
		 * @param site the mineral site.
		 * @param column the table column.
		 * @return the cell value.
		 */
		@Override
		protected Object getItemValue(Landmark site, int column) {
			return switch(column) {
				case 0 -> site.getName();
				case 1 -> site.getType().getName();
				case 2 -> site.getDescription();
				case 3 -> site.getCoordinates().getFormattedString();
				case 4 -> site.getCoordinates().getDistance(startPoint);
				default -> null;
			};
		}

		/**
		 * Check for failure cells.
		 * @param site the mineral site.
		 * @param column the table column.
		 */
		@Override
		protected String isFailureCell(Landmark site, int column) {
			return null;
		}
	}
}