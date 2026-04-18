/*
 * Mars Simulation Project
 * TabPanelGeneral.java
 * @date 2026-03-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow.transport;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.interplanetary.transport.Transportable;
import com.mars_sim.core.interplanetary.transport.resupply.Resupply;
import com.mars_sim.core.interplanetary.transport.resupply.ResupplySchedule;
import com.mars_sim.core.interplanetary.transport.settlement.ArrivingSettlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.CoordinatesLabel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * General info tab panel for a Transportable entity.
 * Displays basic info such as name, state, launch/arrival dates, and landing location.
 * For Resupply missions, also displays the resupply schedule.
 */
class TabPanelGeneral extends EntityTabPanel<Transportable> {
	
	private JLabel nameTextField;
	private JLabel stateTextField;
	private JLabel launchDateTextField;
	private JLabel arrivalDateTextField;
	private CoordinatesLabel landingLocation;

    public TabPanelGeneral(Transportable entity, UIContext context) {
		super(
			GENERAL_TITLE,
			ImageLoader.getIconByName(GENERAL_ICON),
			GENERAL_TOOLTIP,
			context, entity
		);
    }

    @Override
    protected void buildUI(JPanel centerContentPanel) {
        var contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        centerContentPanel.add(contentPanel, BorderLayout.NORTH);

        var attributesPanel = new AttributePanel();
        contentPanel.add(attributesPanel);

        nameTextField = attributesPanel.addTextField(Msg.getString("entity.name"), null, null);
		stateTextField = attributesPanel.addTextField(Msg.getString("transportable.state"), null, null);
		launchDateTextField = attributesPanel.addTextField(Msg.getString("transportable.launchDate"), null, null);
		arrivalDateTextField = attributesPanel.addTextField(Msg.getString("transportable.arrivalDate"), null, null);
		landingLocation = new CoordinatesLabel(getContext());
        attributesPanel.addLabelledItem(Msg.getString("transportable.landingLocation"), landingLocation, null);

        if (getEntity() instanceof Resupply r && r.getTemplate() != null) {            
            contentPanel.add(buildSchedulePanel(r.getTemplate()));
        }
        else if (getEntity() instanceof ArrivingSettlement as) {
            attributesPanel.addTextField(Msg.getString("settlement.template"), as.getTemplate(), null);
        }

        contentPanel.add(Box.createVerticalGlue());
		updateProps();

	}

	private Component buildSchedulePanel(ResupplySchedule template) {
        var schedulePanel = new AttributePanel();
        schedulePanel.setBorder(SwingHelper.createLabelBorder(Msg.getString("resupply.schedule")));
        schedulePanel.addTextField(Msg.getString("entity.name"), template.getName(), null);

        var schedule = template.getSchedule();
        schedulePanel.addTextField(Msg.getString("resupply.frequency"), 
                    StyleManager.DECIMAL_SOLS.format(schedule.getFrequency()), null);
        schedulePanel.addTextField(Msg.getString("resupply.manifest"),
                    template.getManifest().getName(), null);

        return schedulePanel;
    }

    private void updateProps() {
		var entity = getEntity();

		nameTextField.setText(entity.getName());
		stateTextField.setText(entity.getTransitState().getName());
        if (entity.getLaunchDate() != null || entity.getArrivalDate() != null) {
            launchDateTextField.setText(entity.getLaunchDate().getDateTimeStamp());
        }
        
        if (entity.getArrivalDate() != null) {
            arrivalDateTextField.setText(entity.getArrivalDate().getDateTimeStamp());
        }
        if (entity.getLandingLocation() != null) {
            landingLocation.setCoordinates(entity.getLandingLocation());
        } 
	}

	@Override
	public void refreshUI() {
		updateProps();
	}
}