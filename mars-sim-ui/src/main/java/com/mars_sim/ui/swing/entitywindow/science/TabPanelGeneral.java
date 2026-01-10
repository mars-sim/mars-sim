package com.mars_sim.ui.swing.entitywindow.science;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * Tab panel that displayed the basic properties of a Scientific Study.
 * This includes the topics.
 */
class TabPanelGeneral extends EntityTabPanel<ScientificStudy> implements EntityListener {

	private JProgressBar progress;
	private DefaultListModel<String> topics;

    TabPanelGeneral(ScientificStudy study, UIContext context) {
		super(
			GENERAL_TITLE,
			ImageLoader.getIconByName(GENERAL_ICON),		
			GENERAL_TOOLTIP,
			context, study
		);
    }

    @Override
	protected void buildUI(JPanel pane) {

        var study = getEntity();

		var content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		pane.add(content, BorderLayout.CENTER);

		// Prepare spring layout info panel.
		AttributePanel infoPane = new AttributePanel();
		
		content.add(infoPane);
		infoPane.addTextField(Msg.getString("Entity.name"), study.getName(), null);
		infoPane.addTextField(Msg.getString("ScientificStudy.science"), study.getScience().getName(), null);
		infoPane.addTextField(Msg.getString("ScientificStudy.level"), Integer.toString(study.getDifficultyLevel()), null);
		infoPane.addTextField(Msg.getString("ScientificStudy.phase"), study.getPhase().getName(), null);
		
		infoPane.addLabelledItem(Msg.getString("ScientificStudy.lead"),
									new EntityLabel(study.getPrimaryResearcher(), getContext()));
		infoPane.addLabelledItem(Msg.getString("Settlement.singular"),
									new EntityLabel(study.getPrimarySettlement(), getContext()));

		progress = new JProgressBar(0, 100);
		progress.setStringPainted(true);
		infoPane.addLabelledItem(Msg.getString("ScientificStudy.completed"), progress);

		// Topics block
		var topicsPanel = new JPanel(new BorderLayout());
		topicsPanel.setBorder(SwingHelper.createLabelBorder(Msg.getString("ScientificStudy.topics")));
		content.add(topicsPanel);
		topics = new DefaultListModel<>();
		var topicsList = new JList<>(topics);
		topicsPanel.add(topicsList, BorderLayout.CENTER);

		updateProgress();
		updateTopics();
	}

	private void updateProgress() {
		var study = getEntity();
		int percent = (int)(study.getPhaseProgress() * 100D);
		progress.setValue(percent);
		progress.setString(percent + "%");
	}

	private void updateTopics() {
		var study = getEntity();
		
		topics.clear();
		study.getTopic().stream().forEach(t -> topics.addElement(t));
	}

	@Override
	public void entityUpdate(EntityEvent event) {
		updateProgress();
		if (ScientificStudy.PHASE_CHANGE_EVENT.equals(event.getType())) {
			updateTopics();
		}
	}
}
