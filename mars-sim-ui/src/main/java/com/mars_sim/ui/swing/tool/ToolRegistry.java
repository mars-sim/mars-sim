/*
 * Mars Simulation Project
 * ToolRegistry.java
 * @date 2025-10-18
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool;

import java.util.Properties;

import com.mars_sim.core.Simulation;
import com.mars_sim.ui.swing.ContentPanel;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.astroarts.OrbitViewer;
import com.mars_sim.ui.swing.tool.browser.BrowserWindow;
import com.mars_sim.ui.swing.tool.commander.CommanderWindow;
import com.mars_sim.ui.swing.tool.guide.GuideWindow;
import com.mars_sim.ui.swing.tool.mission.MissionWindow;
import com.mars_sim.ui.swing.tool.monitor.MonitorWindow;
import com.mars_sim.ui.swing.tool.navigator.NavigatorWindow;
import com.mars_sim.ui.swing.tool.resupply.ResupplyWindow;
import com.mars_sim.ui.swing.tool.search.SearchWindow;
import com.mars_sim.ui.swing.tool.settlement.SettlementWindow;
import com.mars_sim.ui.swing.tool.time.TimeTool;

/**
 * Registry for tools windows.
 */
public class ToolRegistry {

    private ToolRegistry() {
        // Prevent instantiation
    }

    /**
     * Build a tool for the given tool name.
     * @param toolName Name of the tool
     * @param context Context of the tool
     * @return
     */
    public static ContentPanel getTool(String toolName, UIContext context, Properties toolProps) {
        Simulation sim = context.getSimulation();

		return switch(toolName) {
            case BrowserWindow.NAME -> new BrowserWindow(context);
			case OrbitViewer.NAME -> new OrbitViewer(sim.getMasterClock());
			case TimeTool.NAME -> new TimeTool(sim);
			case GuideWindow.NAME -> new GuideWindow(sim.getConfig()); 
			case SearchWindow.NAME -> new SearchWindow(context);
			case CommanderWindow.NAME -> new CommanderWindow(context);
            case ResupplyWindow.NAME -> new ResupplyWindow(context);
            case MissionWindow.NAME -> new MissionWindow(context);
			case SettlementWindow.NAME -> new SettlementWindow(context, toolProps);
            case NavigatorWindow.NAME -> new NavigatorWindow(context, toolProps);
            case MonitorWindow.NAME -> new MonitorWindow(context, toolProps);
			default -> null;
		};
    }

}
