/*
 * Mars Simulation Project
 * BotsPanel.java
 * @date 2026-02-08
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.tool.mission.create.MissionDataBean;
import com.mars_sim.ui.swing.utils.wizard.AbstractWizardItemModel;
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
		List<Worker> selectedWorkers = new ArrayList<>();
		selection.forEach(selectedWorkers::add);
				
		state.setBotMembers(selectedWorkers);
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
	private static class RobotTableModel extends AbstractWizardItemModel<Robot> {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private static final List<ColumnSpec> COLUMNS = List.of(
				new ColumnSpec(Msg.getString("entity.name"), String.class),
				new ColumnSpec(Msg.getString("robot.type"), String.class),
				new ColumnSpec(Msg.getString("mission.singular"), String.class),
				new ColumnSpec(Msg.getString("robot.performance"), Double.class, ColumnSpec.STYLE_PERCENTAGE)
		);

		/** Constructor. */
		private RobotTableModel(MissionDataBean state) {
			super(COLUMNS);

			List<Robot> robots = state.getStartingSettlement().getAllAssociatedRobots().stream()
					.sorted(Comparator.comparing(Robot::getName))
					.toList();
			setItems(robots);
		}

		/**
		 * Failure is if the Person is already assigned to a mission.
		 */
		@Override
		protected String isFailureCell(Robot item, int column) {
			return (column == 3 && item.getBotMind().getMission() != null) ? MissionCreate.ALREADY_ON_MISSION : null;
		}

		/**
		 * Gets the value for the cell at columnIndex and rowIndex.
		 * 
		 * @param item the item.
		 * @param column the column index.
		 * @return Rendered values
		 */
		@Override
		protected Object getItemValue(Robot item, int column) {
			return switch (column) {
				case 0 -> item.getName();
				case 1 -> item.getRobotType().getName();
				case 2 -> {
					Mission mission = item.getBotMind().getMission();
					if (mission != null) yield mission.getName();
					else yield null;
				}
				case 3 -> item.getPerformanceRating() * 100D;
				default -> null;
			}; 
		}
	}
}
