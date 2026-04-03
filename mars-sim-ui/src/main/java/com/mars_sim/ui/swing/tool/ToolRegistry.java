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
import com.mars_sim.ui.swing.tool.commander.CommanderWindow;
import com.mars_sim.ui.swing.tool.console.ConsolePanel;
import com.mars_sim.ui.swing.tool.entitybrowser.EntityBrowser;
import com.mars_sim.ui.swing.tool.guide.GuideWindow;
import com.mars_sim.ui.swing.tool.monitor.MonitorWindow;
import com.mars_sim.ui.swing.tool.navigator.NavigatorWindow;
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

    public enum ToolCategory {
        GENERIC,
        HELP,
        UTILITY
    }

    /**
     * Information about a tool.
     */
    public record ToolInfo(String name, ToolCategory category, String title, String iconName) {}

    // List of all available tools
    public static final ToolInfo[] TOOL_INFOS = {
        new ToolInfo(NavigatorWindow.NAME, ToolCategory.GENERIC, NavigatorWindow.TITLE, NavigatorWindow.ICON),
        new ToolInfo(SettlementWindow.NAME, ToolCategory.GENERIC, SettlementWindow.TITLE, SettlementWindow.ICON),
        new ToolInfo(EntityBrowser.NAME, ToolCategory.GENERIC, EntityBrowser.TITLE, EntityBrowser.ICON),
        new ToolInfo(MonitorWindow.NAME, ToolCategory.GENERIC, MonitorWindow.TITLE, MonitorWindow.ICON),
        new ToolInfo(CommanderWindow.NAME, ToolCategory.GENERIC, CommanderWindow.TITLE, CommanderWindow.ICON),
        new ToolInfo(OrbitViewer.NAME, ToolCategory.UTILITY, OrbitViewer.TITLE, OrbitViewer.ICON),
        new ToolInfo(TimeTool.NAME, ToolCategory.UTILITY, TimeTool.TITLE, TimeTool.ICON),
        new ToolInfo(GuideWindow.NAME, ToolCategory.HELP, GuideWindow.TITLE, GuideWindow.ICON),
        new ToolInfo(SearchWindow.NAME, ToolCategory.UTILITY, SearchWindow.TITLE, SearchWindow.ICON),
        new ToolInfo(ConsolePanel.NAME, ToolCategory.GENERIC, ConsolePanel.TITLE, ConsolePanel.ICON)
    };

    /**
     * Build a tool for the given tool name.
     * @param toolName Name of the tool
     * @param context Context of the tool
     * @return
     */
    public static ContentPanel getTool(String toolName, UIContext context, Properties toolProps) {
        Simulation sim = context.getSimulation();

		return switch(toolName) {
            case EntityBrowser.NAME -> new EntityBrowser(context);
			case OrbitViewer.NAME -> new OrbitViewer(sim.getMasterClock());
			case TimeTool.NAME -> new TimeTool(sim);
			case GuideWindow.NAME -> new GuideWindow(sim.getConfig()); 
			case SearchWindow.NAME -> new SearchWindow(context);
			case CommanderWindow.NAME -> new CommanderWindow(context);
			case SettlementWindow.NAME -> new SettlementWindow(context, toolProps);
            case NavigatorWindow.NAME -> new NavigatorWindow(context, toolProps);
            case MonitorWindow.NAME -> new MonitorWindow(context, toolProps);
            case ConsolePanel.NAME -> new ConsolePanel(context, toolProps);
			default -> null;
		};
    }

}
