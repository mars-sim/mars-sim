package com.mars_sim.ui.swing.entitywindow.science;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.EntityModel;


class TabPanelCollaborators extends EntityTableTabPanel<ScientificStudy>
    implements EntityListener {

    private static final String ID_ICON = "people";
	private ResearchTableModel researcherModel;

    TabPanelCollaborators(ScientificStudy study, UIContext context) {
		super(
			"Collaborators",
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
								researcherModel.addPerson((Person)event.getTarget());
				case ScientificStudy.REMOVE_COLLABORATOR_EVENT ->
								researcherModel.removePerson((Person)event.getTarget());
				default -> researcherModel.fireTableDataChanged();
			}
			
		}
    }


    private static class ResearchTableModel extends AbstractTableModel implements EntityModel {

		public static final int NAME = 0;
		public static final int CONTRIBUTION = 1;
		public static final int WORK = 2;

		private List<Person> researchers = new ArrayList<>();
		private ScientificStudy study;

		public ResearchTableModel(ScientificStudy study) {
			this.study = study;

			researchers.add(study.getPrimaryResearcher());
			researchers.addAll(study.getCollaborativeResearchers());
		}

		public void removePerson(Person target) {
			int index = researchers.indexOf(target);
			if (index >= 0) {
				researchers.remove(index);
				fireTableRowsDeleted(index, index);
			}
		}

		public void addPerson(Person target) {
			if (!researchers.contains(target)) {
				researchers.add(target);
				int index = researchers.size() - 1;
				fireTableRowsInserted(index, index);
			}
		}

		@Override
		public int getRowCount() {
			return researchers.size();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch(columnIndex) {
				case NAME: return String.class;
				case CONTRIBUTION: return String.class;
				case WORK: return Double.class;
				default: return null;
			}
		}

		@Override
		public String getColumnName(int columnIndex) {
			switch(columnIndex) {
				case NAME: return "Name";
				case CONTRIBUTION: return "Contribution";
				case WORK: return "Work";
				default: return "";
			}
		}

		@Override
		public Object getValueAt(int row, int column) {
			Person p = researchers.get(row);
			boolean isPrimary = (p.equals(study.getPrimaryResearcher()));

			// Safety check
			if (!isPrimary && !study.getCollaborativeResearchers().contains(p)) {
				return null;
			}

			switch(column) {
				case NAME: return p.getName();
				case CONTRIBUTION: return (isPrimary ? study.getScience().getName()
									: study.getContribution(p).getName());
				case WORK: {
					if (study.getPhase() == StudyStatus.PAPER_PHASE) {
						return (isPrimary ? study.getPrimaryPaperWorkTimeCompleted() 
											: study.getCollaborativePaperWorkTimeCompleted(p));
					}
					else if (study.getPhase() == StudyStatus.RESEARCH_PHASE) {
						return (isPrimary ? study.getPrimaryResearchWorkTimeCompleted() 
											: study.getCollaborativeResearchWorkTimeCompleted(p));
					}
					else {
						return 0D;
					}
				}
				default: return "";
			}
		}

        @Override
		public Entity getAssociatedEntity(int row) {
			return researchers.get(row);
		}
    }
}