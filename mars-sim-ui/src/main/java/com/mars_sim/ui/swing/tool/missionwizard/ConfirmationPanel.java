package com.mars_sim.ui.swing.tool.missionwizard;

import java.awt.BorderLayout;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.JComponent;

import com.mars_sim.core.Entity;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.utils.EntityLabel;
import com.mars_sim.ui.swing.utils.SurfacePOILabel;
import com.mars_sim.ui.swing.utils.wizard.WizardPane;
import com.mars_sim.ui.swing.utils.wizard.WizardStep;

class ConfirmationPanel extends WizardStep<MissionDataBean>  {

    static final String ID = "confirmation";

    ConfirmationPanel(WizardPane<MissionDataBean> parent, MissionDataBean state) {
        super(ID, parent);
        setLayout(new BorderLayout());

        // Mandattory is already done
        setMandatoryDone(true);

        var context = parent.getContext();

        var attrs = new AttributePanel();
        add(attrs, BorderLayout.NORTH);

        // Common
        attrs.addTextField(Msg.getString("mission.type"), state.getMissionType().getName(), null);
        attrs.addLabelledItem(Msg.getString("settlement.singular"), new EntityLabel(state.getStartingSettlement(), context), null);
        attrs.addLabelledItem(Msg.getString("mission.leader"), new EntityLabel(state.getLeader(), context), null);

        var names = state.getWorkerMembers().stream()
                .map(w -> w.getName())
                .collect(Collectors.joining(", "));
        attrs.addTextField(Msg.getString("mission.members"), names, null);

        // How getting there
        if (state.getRover() != null) {
            attrs.addLabelledItem(Msg.getString("vehicle.singular"), new EntityLabel(state.getRover(), context), null);
        }
        if (state.getDrone() != null) {
            attrs.addLabelledItem(Msg.getString("flyer.singular"), new EntityLabel(state.getDrone(), context), null);
        }
        if (state.getLUV() != null) {
            attrs.addLabelledItem(Msg.getString("lightutilityvehicle.singular"), new EntityLabel(state.getLUV(), context), null);
        }

        // Final purpose
        if (state.getConstructionSite() != null) {
            attrs.addLabelledItem(Msg.getString("construction.singular"), new EntityLabel(state.getConstructionSite(), context), null);
        }
        if (state.getDestinationSettlement() != null) {
            attrs.addLabelledItem(Msg.getString("mission.designation"), new EntityLabel(state.getDestinationSettlement(), context), null);
        }
        if (state.getRescueVehicle() != null) {
            attrs.addLabelledItem("Rescue", new EntityLabel(state.getRescueVehicle(), context), null);
        }
        if (state.getMiningSite() != null) {
            attrs.addLabelledItem(Msg.getString("mission.phase.miningSite"), new SurfacePOILabel(state.getMiningSite(), context), null);
        }
        if (state.getLandmark() != null) {
            attrs.addLabelledItem(Msg.getString("landmark.singular"), new SurfacePOILabel(state.getLandmark(), context), null);
        }
        if (state.getScientificStudy() != null) {
            attrs.addLabelledItem(Msg.getString("scientificstudy.singular"), new EntityLabel(state.getScientificStudy(), context), null);
        }
    }

    private static JComponent createEntityList(List<? extends Entity> data, UIContext context) {
        var listPanel = Box.createVerticalBox();
        for (var entity : data) {
            var item = new EntityLabel(entity, context);
            item.setAlignmentX(LEFT_ALIGNMENT);
            listPanel.add(item);
        }

        return listPanel;
    }
    
    @Override
    public void updateState(MissionDataBean state) {
        // nothing to update as this is a confirmation
    }
}
