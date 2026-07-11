/*
 * Mars Simulation Project
 * BotsPanel.java
 * @date 2026-02-08
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.List;

import com.mars_sim.core.robot.Robot;
import com.mars_sim.ui.swing.utils.model.BaseRobotModel;
import com.mars_sim.ui.swing.utils.wizard.WizardItemModel;
import com.mars_sim.ui.swing.utils.wizard.WizardItemStep;

/**
 * A wizard panel for selecting bots.
 */
@SuppressWarnings("serial")
class BotsPanel extends WizardItemStep<MissionDataBean, Robot>
{

	/** The wizard panel name. */
	public static final String ID = "Robots";
	
	/**
	 * Constructor.
	 * 
	 * @param wizard the create mission wizard
	 * @param state the mission data bean.
	 */
	public BotsPanel(MissionCreate wizard, MissionDataBean state) {
		// Use WizardPanel constructor
		super(ID, wizard, new RobotTableModel(state), 0, 2);
	}

	/**
	 * Update 
	 */
	@Override
	protected void updateState(MissionDataBean state, List<Robot> selection) {
		state.setBotMembers(selection);
	}

	/**
	 * Clears information on the wizard panel.
	 */
	@Override
	public void clearState(MissionDataBean state) {
		state.setBotMembers(null);
		super.clearState(state);
	}

	/**
	 * Table model for people.
	 */
	private static class RobotTableModel extends BaseRobotModel
				implements WizardItemModel<Robot> {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		/** Constructor. */
		private RobotTableModel(MissionDataBean state) {
			super(NAME, TYPE, MISSION, PERFORMANCE);

			var robots = state.getStartingSettlement().getAllAssociatedRobots();
			setEntities(robots);
			enableListeners(true);
		}

		@Override
		public Robot getItem(int row) {
			return (Robot) getAssociatedEntity(row);
		}

		/**
		 * Failure is if the Person is already assigned to a mission.
		 */
		@Override
		public String isFailureCell(int row, int column) {
			var colSpec = getColumnSpec(column);
			if (colSpec.equals(MISSION.column())) {
				var item = getItem(row);
				if (item.getMission() != null) {
					return MissionCreate.ALREADY_ON_MISSION;
				}
			}
			return null;
		}
	}
}
