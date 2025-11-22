/*
 * Mars Simulation Project
 * ToolRegistry.java
 * @date 2025-10-18
 * @author Scott Davis
 */
package com.mars_sim.ui.swing;

import com.mars_sim.core.Simulation;
import com.mars_sim.ui.swing.astroarts.OrbitViewer;
import com.mars_sim.ui.swing.tool.commander.CommanderWindow;
import com.mars_sim.ui.swing.tool.guide.GuideWindow;
import com.mars_sim.ui.swing.tool.navigator.NavigatorWindow;
import com.mars_sim.ui.swing.tool.science.ScienceWindow;
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
    public static ContentPanel getTool(String toolName, MainDesktopPane context) {
        MainWindow mainWindow = context.getMainWindow();
        Simulation sim = context.getSimulation();

		return switch(toolName) {
			case OrbitViewer.NAME -> new OrbitViewer(sim.getMasterClock());
			case TimeTool.NAME -> new TimeTool(sim);
			case GuideWindow.NAME -> new GuideWindow(mainWindow.getHelp()); 
			case SearchWindow.NAME -> new SearchWindow(context);
			case ScienceWindow.NAME -> new ScienceWindow(context);
			case CommanderWindow.NAME -> new CommanderWindow(context);
			case SettlementWindow.NAME -> new SettlementWindow(context,
										mainWindow.getConfig().getInternalWindowProps(SettlementWindow.NAME));
            case NavigatorWindow.NAME -> new NavigatorWindow(context,
                                        mainWindow.getConfig().getInternalWindowProps(NavigatorWindow.NAME));

			default -> null;
		};
    }

}
