package com.mars_sim.ui.swing.entitywindow.science;

import javax.swing.table.TableModel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.model.BasePersonModel;


/**
 * Table panel that shows collaborators for a scientific study. It shows the primary researcher and any collaborative researchers.
 * It listens for changes to the collaborators and updates the table as needed.
 */
class TabPanelCollaborators extends EntityTableTabPanel<ScientificStudy>
    implements EntityListener {

    private static final String ID_ICON = "people";
	private ResearchTableModel researcherModel;

    TabPanelCollaborators(ScientificStudy study, UIContext context) {
		super(
			Msg.getString("scientificstudy.collaborator.plural"),
			ImageLoader.getIconByName(ID_ICON),		
			null,
			study,
			context
		);
    }

    @Override
    protected TableModel createModel() {
		researcherModel = new ResearchTableModel(getEntity());
        return researcherModel;
    }

    @Override
    public void entityUpdate(EntityEvent event) {
		if (researcherModel != null) {
			switch (event.getType()) {
				case ScientificStudy.ADD_COLLABORATOR_EVENT ->
								researcherModel.addEntity((Person)event.getTarget());
				case ScientificStudy.REMOVE_COLLABORATOR_EVENT ->
								researcherModel.removeEntity((Person)event.getTarget());
				default -> researcherModel.fireTableDataChanged();
			}	
		}
    }


    private static class ResearchTableModel extends BasePersonModel {

		private static final int CONTRIBUTION_VAL = 200;
		private static final int WORK_VAL = 201;
    	private static final EntityColumnSpec CONTRIBUTION = new EntityColumnSpec(new ColumnSpec(CONTRIBUTION_VAL, "Contribution", String.class),
                                                            null);
    	private static final EntityColumnSpec WORK = new EntityColumnSpec(new ColumnSpec(WORK_VAL, "Work", Double.class,
															ColumnSpec.STYLE_DIGIT2), null);

		private ScientificStudy study;

		public ResearchTableModel(ScientificStudy study) {
			super(NAME, CONTRIBUTION, WORK);
			this.study = study;

			setEntities(study.getCollaborativeResearchers());
			addEntity(study.getPrimaryResearcher());
		}

		@Override
		protected Object getEntityValue(Person p, int valueIndex) {
			boolean isPrimary = (p.equals(study.getPrimaryResearcher()));

			// Safety check
			if (!isPrimary && !study.getCollaborativeResearchers().contains(p)) {
				return null;
			}

			return switch(valueIndex) {
				case CONTRIBUTION_VAL -> 
					isPrimary ? study.getScience().getName() :  study.getContribution(p).getName();

				case WORK_VAL -> {
					if (study.getPhase() == StudyStatus.PAPER_PHASE) {
						yield (isPrimary ? study.getPrimaryPaperWorkTimeCompleted() 
											: study.getCollaborativePaperWorkTimeCompleted(p));
					}
					else if (study.getPhase() == StudyStatus.RESEARCH_PHASE) {
						yield (isPrimary ? study.getPrimaryResearchWorkTimeCompleted() 
											: study.getCollaborativeResearchWorkTimeCompleted(p));
					}
					else {
						yield 0D;
					}
				}
				default -> super.getEntityValue(p, valueIndex);
			};
		}
    }
}