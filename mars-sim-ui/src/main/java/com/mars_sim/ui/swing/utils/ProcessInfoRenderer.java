/*
 * Mars Simulation Project
 * ProcessInfoRenderer.java
 * @date 2024-03-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import java.util.stream.Collectors;

import com.mars_sim.core.manufacture.SalvageProcessInfo;
import com.mars_sim.core.process.ProcessInfo;
import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.resource.ItemType;

/**
 * This is a static helper class that can render ProcessInfo objects.
 */
public final class ProcessInfoRenderer {

    private static final String TOOLTIP_ROW = "<tr><td VALIGN=TOP align=\"right\">%s:</td><td>%s</td></tr>";

    private ProcessInfoRenderer() {
        // Stop creation of static class
    }

    /**
     * Gets a tool tip string for a process.
     * 
     * @param info the process info.
     */
    public static String getToolTipString(ProcessInfo info) {
        StringBuilder result = new StringBuilder("<html>");

        result.append("<table>");
        result.append(String.format(TOOLTIP_ROW, "Process", info.getName()));
        result.append(String.format(TOOLTIP_ROW, "Labor Req", info.getWorkTimeRequired() + "millisols"));
        result.append(String.format(TOOLTIP_ROW, "Time Req", info.getProcessTimeRequired() + "millisols"));
        result.append(String.format(TOOLTIP_ROW, "Power Req", info.getPowerRequired() + " Kw"));
        result.append(String.format(TOOLTIP_ROW, "Bldg Tech Req", info.getTechLevelRequired()));
        result.append(String.format(TOOLTIP_ROW, "Skill Req", info.getSkillLevelRequired()));
        result.append(String.format(TOOLTIP_ROW, "Inputs",
                                        info.getInputList().stream()
                                                    .map(i -> getItemAmountString(i) + " " + i.getName())
                                                    .collect(Collectors.joining("<br>"))));
        result.append(String.format(TOOLTIP_ROW, "Outputs",
                                        info.getOutputList().stream()
                                                    .map(i -> getItemAmountString(i) + " " + i.getName())
                                                    .collect(Collectors.joining("<br>"))));
        if (info instanceof SalvageProcessInfo salvageInfo) {
            result.append("Salvaged Item Type: ").append(salvageInfo.getItemName()).append("<br>");
        }
        
        result.append("</table>");
    
    	result.append("</html>");

    	return result.toString();
    }

    /**
     * Gets a string representing a manufacture process item amount.
     * 
     * @param item the manufacture process item.
     * @return amount string.
     */
    private static String getItemAmountString(ProcessItem item) {
    	if (ItemType.AMOUNT_RESOURCE == item.getType()) {
			return item.getAmount() + " kg";
    	}
		else return Integer.toString((int) item.getAmount());
    }
}
