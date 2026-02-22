/*
 * Mars Simulation Project
 * SciencePanel.java
 * @date 2026-02-16
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.Comparator;
import java.util.List;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.ScientificStudyManager;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.utils.wizard.AbstractWizardItemModel;
import com.mars_sim.ui.swing.utils.wizard.WizardItemStep;

/**
 * A wizard panel for selecting the scientific study for a science mission.
 * All studies that are at the starting settlement and match the science type are shown.
 * Only studies in the research phase with assigned researchers can be selected.
 */
class SciencePanel extends WizardItemStep<MissionDataBean,ScientificStudy> {
    
    public static final String ID = "science";

    public SciencePanel(MissionCreate wizard, MissionDataBean state) {
        super(ID, wizard, new StudyTableModel(state), 1, 1);
    }

    /**
	 * Clear information on the wizard panel.
	 */
	@Override
	public void clearState(MissionDataBean state) {
		state.setScientificStudy(null);
		super.clearState(state);
	}

    /**
     * Update the state with the selected scientific study.
     */
    @Override
    protected void updateState(MissionDataBean state, List<ScientificStudy> selectedItems) {
        state.setScientificStudy(selectedItems.get(0));
    }

    /**
	 * A table model for scientific studies.
	 */
	private static class StudyTableModel extends AbstractWizardItemModel<ScientificStudy> {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
        private static final List<ColumnSpec> COLUMNS = List.of(
				new ColumnSpec(Msg.getString("entity.name"), String.class),
				new ColumnSpec(Msg.getString("scientificstudy.phase"), String.class),
				new ColumnSpec(Msg.getString("scientificstudy.collaborator.plural"), Integer.class)
		);

		private StudyTableModel(MissionDataBean state) {
            super(COLUMNS);

            ScientificStudyManager manager = Simulation.instance().getScientificStudyManager();
            var studyScience = switch(state.getMissionType()) {
                case AREOLOGY -> ScienceType.AREOLOGY;
                case BIOLOGY -> ScienceType.ASTROBIOLOGY;
                case METEOROLOGY -> ScienceType.METEOROLOGY;
                default ->null;
            };

            // Hide suitable studies for the starting settlement, and sort by name.
            var studies = manager.getAllStudies(state.getStartingSettlement()).stream()
                .filter(s -> s.getScience() == studyScience)
                .sorted(Comparator.comparing(ScientificStudy::getName))
                .toList();
            setItems(studies);
		}

        @Override
        protected String isFailureCell(ScientificStudy item, int column) {
            return switch(column) {
                case 1 -> item.getPhase() != StudyStatus.RESEARCH_PHASE ? "Study is not in research phase" : null;
                case 2 -> item.getCollaborativeResearchers().isEmpty() ? "No researchers assigned to study" : null;
                default -> null;
            };
        }

        @Override
        protected Object getItemValue(ScientificStudy item, int column) {
            return switch(column) {
                case 0 -> item.getName();
                case 1 -> item.getPhase().getName();
                case 2 -> item.getCollaborativeResearchers().size();
                default -> null;
            };
        }
	}
}
