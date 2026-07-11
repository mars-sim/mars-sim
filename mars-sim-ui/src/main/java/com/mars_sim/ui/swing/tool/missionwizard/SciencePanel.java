/*
 * Mars Simulation Project
 * SciencePanel.java
 * @date 2026-02-16
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.missionwizard;

import java.util.List;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.ScientificStudyManager;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.ui.swing.utils.model.BaseScienceStudyModel;
import com.mars_sim.ui.swing.utils.wizard.WizardItemModel;
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
	private static class StudyTableModel extends BaseScienceStudyModel
                implements WizardItemModel<ScientificStudy> {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private StudyTableModel(MissionDataBean state) {
            super(NAME, PHASE, SCIENCE);

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
                .toList();
            setEntities(studies);
            enableListeners(true);
		}

        @Override
        public ScientificStudy getItem(int row) {
            return (ScientificStudy)getAssociatedEntity(row);
        }

        @Override
        public String isFailureCell(int row, int column) {
            var columnVal = getColumnSpec(column);
            if (columnVal.equals(PHASE.column())) {
                var item = getItem(row);
                return (item.getPhase() != StudyStatus.RESEARCH_PHASE) ? "Study is not in research phase" : null;
            }

            return null;
        }
	}
}
