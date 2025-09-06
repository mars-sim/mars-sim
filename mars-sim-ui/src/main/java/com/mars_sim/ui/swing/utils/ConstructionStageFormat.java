/*
 * Mars Simulation Project
 * ConstructionStageFormat.java
 * @date 2025-07-31
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

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
            boolean isConstruction = stage.isConstruction();
            ConstructionStageInfo info = stage.getInfo();
            result.append("Status: ").append(isConstruction ? "building " : "salvaging ")
                                .append(info.getName()).append(Msg.BR);
            result.append("Stage Type: ").append(info.getType()).append(Msg.BR);
            if (isConstruction)
                result.append("Work Type: salvage<br>");
			else
                result.append("Work Type: Construction<br>");
            result.append("Work Time Required: ").append(StyleManager.DECIMAL1_SOLS.format(stage.getRequiredWorkTime())).append(Msg.BR);
            result.append("Work Time Completed: ").append(StyleManager.DECIMAL1_SOLS.format(stage.getCompletedWorkTime())).append(Msg.BR);
            result.append("Architect Construction Skill Required: ").append(info.getArchitectConstructionSkill()).append(Msg.BR);

            if (isConstruction) {
                // Add remaining construction resources.
                String missingRes = stage.getResources().entrySet().stream()
                                    .filter(m -> m.getValue().getMissing() > 0)
                                    .map(e -> Msg.NBSP + ResourceUtil.findAmountResourceName(e.getKey()) + ": " + e.getValue().getMissing()
                                            + " kg")
                                    .collect(Collectors.joining(Msg.BR));

                if (!missingRes.isEmpty()) {
                    result.append(Msg.BR).append("Missing Construction Resources:").append(Msg.BR).append(missingRes);
                }

                // Add Missing construction parts.
                var missingParts = stage.getParts().entrySet().stream()
                        .map(e -> Msg.NBSP + ItemResourceUtil.findItemResourceName(e.getKey()) + ": " + e.getValue().getMissing())
                        .collect(Collectors.joining(Msg.BR));
                if (!missingParts.isEmpty()) {
                    result.append(Msg.BR).append("Missing Construction Parts:").append(Msg.BR).append(missingParts);

                }
            }
            else {
                result.append("<br>Salvagable Parts:<br>");
                result.append(getParts(info));
            }

            // Add construction vehicles.
            if (!info.getVehicles().isEmpty()) {
                result.append(Msg.BR).append("Construction Vehicles").append(Msg.BR);
                result.append(getVehicles(info));
            }
        }

        result.append(Msg.HTML_STOP);

        return result.toString();
    }

    /**
     * Gets a HTML tool tip for a Stage Info
     */
    public static String getTooltip(ConstructionStageInfo info) {
        StringBuilder result = new StringBuilder(Msg.HTML_START);

        result.append("Name: ").append(info.getName()).append(Msg.BR);
        result.append("Stage Type: ").append(info.getType()).append(Msg.BR);
        String requiredWorkTime = StyleManager.DECIMAL_PLACES1.format(info.getWorkTime() / 1000D);
        result.append("Work Time Required: ").append(requiredWorkTime).append(" Sols").append(Msg.BR);
        result.append("Architect Construction Skill Required: ").append(info.getArchitectConstructionSkill()).append(Msg.BR);

        if (!info.getResources().isEmpty()) {
            result.append(Msg.BR).append("Resources").append(Msg.BR);
            result.append(getResources(info));
        }

        // Add parts
        if (!info.getParts().isEmpty()) {
            result.append(Msg.BR).append("Parts").append(Msg.BR);
            result.append(getParts(info));
        }

        // Add construction vehicles.
        if (!info.getVehicles().isEmpty()) {
            result.append(Msg.BR).append("Construction Vehicles").append(Msg.BR);
            result.append(getVehicles(info));
        }

        result.append(Msg.HTML_STOP);

        return result.toString();
    }

    private static String getVehicles(ConstructionStageInfo info) {
        StringBuilder result = new StringBuilder();
        for(ConstructionVehicleType vehicle : info.getVehicles()) {
            result.append(Msg.NBSP).append(Msg.NBSP).append("Vehicle Type: ").append(vehicle.getVehicleType()).append(Msg.BR);
            result.append(Msg.NBSP).append(Msg.NBSP).append("Attachment Parts:").append(Msg.BR);
            for(var p : vehicle.getAttachmentParts()) {
                result.append(Msg.NBSP).append(Msg.NBSP).append(Msg.NBSP).append(Msg.NBSP)
                .append("-").append(ItemResourceUtil.findItemResourceName(p)).append(Msg.BR);
            }
        }
        return result.toString();
    }

    private static String getResources(ConstructionStageInfo info) {
        return info.getResources().entrySet().stream()
                        .map(e -> Msg.NBSP + ResourceUtil.findAmountResourceName(e.getKey()) + ": " + e.getValue())
                        .collect(Collectors.joining(Msg.BR));
    }

    private static String getParts(ConstructionStageInfo info) {
        return info.getParts().entrySet().stream()
                        .map(e -> Msg.NBSP + ItemResourceUtil.findItemResourceName(e.getKey()) + ": " + e.getValue())
                        .collect(Collectors.joining(Msg.BR));
    }
}
