/*
 * Mars Simulation Project
 * ConstructionStageFormat.java
 * @date 2025-07-31
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import java.util.Iterator;
import java.util.stream.Collectors;

import com.mars_sim.core.building.construction.ConstructionStage;
import com.mars_sim.core.building.construction.ConstructionStageInfo;
import com.mars_sim.core.building.construction.ConstructionVehicleType;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.StyleManager;

/**
 * Provides common formatting for Construction Stage
 */
public final class ConstructionStageFormat {

    private ConstructionStageFormat() {
        // No public constructor
    }

    /**
     * Gets a tool tip HTML for a Stage
     */
    public static String getTooltip(ConstructionStage stage) {
        StringBuilder result = new StringBuilder(Msg.HTML_START);

        if (stage != null) {
            ConstructionStageInfo info = stage.getInfo();
            result.append("Status: building ").append(info.getName()).append(Msg.BR);
            result.append("Stage Type: ").append(info.getType()).append(Msg.BR);
            result.append("Work Type: Construction").append(Msg.BR);
            String requiredWorkTime = StyleManager.DECIMAL_PLACES1.format(stage.getRequiredWorkTime() / 1000D);
            result.append("Work Time Required: ").append(requiredWorkTime).append(" Sols").append(Msg.BR);
            String completedWorkTime = StyleManager.DECIMAL_PLACES1.format(stage.getCompletedWorkTime() / 1000D);
            result.append("Work Time Completed: ").append(completedWorkTime).append(" Sols").append(Msg.BR);
            result.append("Architect Construction Skill Required: ").append(info.getArchitectConstructionSkill()).append(Msg.BR);

            // Add remaining construction resources.
            if (!stage.getMissingResources().isEmpty()) {
                result.append(Msg.BR).append("Missing Construction Resources:").append(Msg.BR);
                result.append(stage.getMissingResources().entrySet().stream()
                        .map(e -> Msg.NBSP + ResourceUtil.findAmountResourceName(e.getKey()) + ": " + e.getValue()
                                        + " kg")
                        .collect(Collectors.joining(Msg.BR)));
            }

            // Add Missing construction parts.
            if (!stage.getMissingParts().isEmpty()) {
                result.append(Msg.BR).append("Missing Construction Parts:").append(Msg.BR);
                result.append(stage.getMissingParts().entrySet().stream()
                    .map(e -> Msg.NBSP + ItemResourceUtil.findItemResourceName(e.getKey()) + ": " + e.getValue())
                    .collect(Collectors.joining(Msg.BR)));
            }

            // Add construction vehicles.
            if (!info.getVehicles().isEmpty()) {
                result.append(Msg.BR).append("Construction Vehicles").append(Msg.BR);
                
                for(ConstructionVehicleType vehicle : info.getVehicles()) {
                    result.append(Msg.NBSP).append(Msg.NBSP).append("Vehicle Type: ").append(vehicle.getVehicleType()).append(Msg.BR);
                    result.append(Msg.NBSP).append(Msg.NBSP).append("Attachment Parts:").append(Msg.BR);
                    Iterator<Integer> l = vehicle.getAttachmentParts().iterator();
                    while (l.hasNext()) {
                        result.append(Msg.NBSP).append(Msg.NBSP).append(Msg.NBSP).append(Msg.NBSP)
                        .append("-").append(ItemResourceUtil.findItemResourceName(l.next())).append(Msg.BR);
                    }
                }
            }
        }

        result.append(Msg.HTML_STOP);

        return result.toString();
    }
}
