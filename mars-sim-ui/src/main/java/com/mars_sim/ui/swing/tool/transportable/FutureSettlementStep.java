/*
 * Mars Simulation Project
 * ArrivingSettlementStep.java
 * @date 2026-03-03
 * @author Barry Evans
 */

package com.mars_sim.ui.swing.tool.transportable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.mars_sim.core.structure.SettlementTemplateConfig;
import com.mars_sim.ui.swing.utils.wizard.WizardPane;
import com.mars_sim.ui.swing.utils.wizard.WizardStep;

/**
 * Step for entering arriving settlement transportable details.
 */
class FutureSettlementStep extends WizardStep<TransportState> {

	static final String ID = "Future_Settlement";

	private JComboBox<String> templateCB;
	private JComboBox<String> sponsorCB;
	private JSpinner population;
	private JSpinner robots;

	private SettlementTemplateConfig templateConfig;

	FutureSettlementStep(WizardPane<TransportState> parent, TransportState state) {
		super(ID, parent);

		var content = Box.createVerticalBox();
		add(content, BorderLayout.NORTH);

		var config = parent.getContext().getSimulation().getConfig();
		templateConfig = config.getSettlementTemplateConfiguration();
		var templates = templateConfig.getItemNames().stream().sorted().toArray(String[]::new);

		JPanel templatePane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		templatePane.setAlignmentX(Component.LEFT_ALIGNMENT);
		templatePane.add(new JLabel("Template: "));
		templateCB = new JComboBox<>(templates);
		templatePane.add(templateCB);
		content.add(templatePane);

		var sponsors = config.getReportingAuthorityFactory()
				.getItemNames().stream().sorted().toArray(String[]::new);
		JPanel sponsorPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		sponsorPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		sponsorPane.add(new JLabel("Sponsor: "));
		sponsorCB = new JComboBox<>(sponsors);
		sponsorPane.add(sponsorCB);
		content.add(sponsorPane);

		JPanel populationPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		populationPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		populationPane.add(new JLabel("Population: "));
		population = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
		populationPane.add(population);
		populationPane.add(new JLabel("Robots: "));
		robots = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
		populationPane.add(robots);
		content.add(populationPane);

		content.add(Box.createVerticalGlue());
		templateCB.addActionListener(e -> templateChanged());
		sponsorCB.addActionListener(e -> refreshMandatory());
		refreshMandatory();
	}

	private void templateChanged() {
		String templateName = (String) templateCB.getSelectedItem();
		if (templateName != null) {
			var template = templateConfig.getItem(templateName);
			population.setValue(template.getDefaultPopulation());
			robots.setValue(template.getDefaultNumOfRobots());

			// Preload Sponsor based on template default if available.
			var defaultSponsor = template.getSponsor();
			if (defaultSponsor != null) {
				sponsorCB.setSelectedItem(defaultSponsor.getName());
			}
		}
		refreshMandatory();
	}

	private void refreshMandatory() {
		setMandatoryDone(templateCB.getSelectedItem() != null
				&& sponsorCB.getSelectedItem() != null);
	}

	@Override
	public void updateState(TransportState state) {
		state.setArrivingTemplate((String) templateCB.getSelectedItem());
		state.setArrivingSponsor((String) sponsorCB.getSelectedItem());
		state.setPopulation((Integer) population.getValue());
		state.setRobots((Integer) robots.getValue());
	}
}
