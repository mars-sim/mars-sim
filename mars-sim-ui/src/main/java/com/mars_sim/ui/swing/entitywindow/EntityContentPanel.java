/**
 * Mars Simulation Project
 * EntityContentPanel.java
 * @date 2025-11-30
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JTabbedPane;

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.MonitorableEntity;
import com.mars_sim.core.Unit;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ConfigurableWindow;
import com.mars_sim.ui.swing.ContentPanel;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;

/**
 * The EntityContentPanel is the base panel for displaying entities. It is a subclass of the generic ContentPanel.
 * It automatically is notified of EntityEvent and will forward them to any TabPanels that are also EntityListeners.
 * It also forwards ClockPulse events to any TabPanels that are also TemporalComponents.
 */
@SuppressWarnings("serial")
public class EntityContentPanel<T extends Entity> extends ContentPanel
    implements ConfigurableWindow, EntityListener {

    static final String UNIT_TYPE = "unittype";
	static final String UNIT_NAME = "unitname";
	private static final String SELECTED_TAB = "selected_tab";

    private static final int WIDTH = 550;
	private static final int HEIGHT = 500;

    private T entity;
	private List<EntityTabPanel<?>> tabPanels = new ArrayList<>();
    private UIContext context;
    private JTabbedPane tabPane;
    private EntityTabPanel<?> defaultTab;

    /**
     * Construct a entity panel to render a single Entity. The entity type used for naming is derived
     * from the name of the entity Class.
     * @param entity Entity to display.
     * @param context Overall UI context
     */
    protected EntityContentPanel(T entity, UIContext context) {
        super(entity.getClass().getSimpleName() + ":" + entity.getName(),
                getEntityType(entity) + " : " + entity.getName(), Placement.CENTER);

        this.entity = entity;
        this.context = context;

        tabPane = new JTabbedPane(StyleManager.getTabPlacement(), JTabbedPane.SCROLL_TAB_LAYOUT);
        add(tabPane, BorderLayout.CENTER);

		// Add a listener for the tab changes
		tabPane.addChangeListener(e -> {
			var newTab = getSelected();
			if (!newTab.isUIDone()) {
				newTab.initializeUI();
			}
            else {
                newTab.refreshUI();
            }
		});

        var dim = new Dimension(WIDTH, HEIGHT);
        setMinimumSize(dim);
        setPreferredSize(dim);

        // Some Entities are MonitorableEntities and can send events
        if (entity instanceof MonitorableEntity u) {
            u.addEntityListener(this);
        }
    }

    /**
     * Get the entity type string for the given entity. This uses the lanugage
     * resource bundle to get the singular form of the entity type.
     * @param entity Entity to get the type for.
     * @return Name of the type.
     */
    private static String getEntityType(Entity entity) {
        String typeKey = entity.getClass().getSimpleName();
        return Msg.getString(typeKey + ".singular");
    }

    /**
	 * Returns the currently selected tab.
	 *
	 * @return Monitor tab being displayed.
	 */
	private EntityTabPanel<?> getSelected() {
		// Not using SwingUtilities.updateComponentTreeUI(this)
		EntityTabPanel<?> selected = null;
		int selectedIdx = tabPane.getSelectedIndex();
		if ((selectedIdx != -1) && (selectedIdx < tabPanels.size()))
			selected = tabPanels.get(selectedIdx);
		return selected;
	}

    /**
     * Apply the initial UI Properties to the entity window
     * @param props Any initial properties for the window.
     */
    protected void applyProps(Properties props) {
        // Add the listener panel if the entity is MonitorableEntity
        if (entity instanceof MonitorableEntity m) {
            addTabPanel(new ListenerTabPanel(m, context));
        }
        
        String selectedTab = props.getProperty(SELECTED_TAB);
        if (selectedTab != null) {
            for (int i = 0; i < tabPanels.size(); i++) {
                var tab = tabPanels.get(i);
                if (tab.getTabTitle().equals(selectedTab)) {
                    tabPane.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    /**
     * Returns the entity being displayed.
     * @return
     */
    public T getEntity() {
        return entity;
    }

    /**
     * Returns the UI context.
     * @return
     */
    public UIContext getContext() {
        return context;
    }

    @Override
	public Properties getUIProps() {
		Properties result = new Properties();
		result.setProperty(UNIT_NAME, entity.getName());
        if (entity instanceof Unit u) {
            result.setProperty(UNIT_TYPE, u.getUnitType().name());
        }
        else { 
		    result.setProperty(UNIT_TYPE, entity.getClass().getSimpleName().toUpperCase());
        }
        var selected = getSelected();
        if (selected != null) {
		    result.setProperty(SELECTED_TAB, selected.getTabTitle());
        }

		return result;
	}

    /**
     * Add the default tab panel to the entity window. This tab is always added first
     * and selected by default.
     * @param panel Tab panel to add as the default.
     */
    protected void addDefaultTabPanel(EntityTabPanel<?> panel) {
        defaultTab = panel;

        // Default always goes to the beginning
        addTabPanel(panel, 0);
    }

    /**
     * Add a tab panel to the entity window. This uses the icon and title of the TabPanel to
     * create the tab. This will be inserted after the default tab and according to alphabetical
     * order of the tab titles.
     * @param panel
     */
    protected void addTabPanel(EntityTabPanel<?> panel) {
        int position = (defaultTab != null) ? 1 : 0;
        while (position < tabPanels.size()) {
            var existingPanel = tabPanels.get(position);
            if (panel.getTabTitle().compareTo(existingPanel.getTabTitle()) < 0) {
                break;
            }

            position++;
        }
        addTabPanel(panel, position);
    }
    
    /**
     * Add a tab panel to the entity window at the specified position.
     * @param panel New panel to add.
     * @param position Position to insert the panel at.
     */
    private void addTabPanel(EntityTabPanel<?> panel, int position) { 
        tabPanels.add(position, panel);

        // Have to ignore the title to force the icon to show correctly
        tabPane.insertTab( null, panel.getTabIcon(), panel, panel.getTabToolTip(), position);
    }

    /**
     * Pass the pulse on to the selected tab if it is a TemporalComponent.
     * @param pulse Incoming pulse.
     */
    @Override
    public void clockUpdate(ClockPulse pulse) {
        var selected = getSelected();
        if (selected instanceof TemporalComponent tc && selected.isUIDone()) {
            tc.clockUpdate(pulse);
        }
    }

    /**
     * Pass the entity event on to any tabs that are also EntityListeners.
     * @param event Incoming entity event.
     */
    @Override
    public void entityUpdate(EntityEvent event) {
        for(var t : tabPanels) {
            if (t instanceof EntityListener el && t.isUIDone())
                el.entityUpdate(event);
        }
    }

    /**
     * Prepare to destroy the panel and its resources. This will remove any listeners
     * from the entity and destroy all tab panels.
     */
    @Override
    public void destroy() {
        // Some Entities are MonitorableEntities and can send events
        if (entity instanceof MonitorableEntity u) {
            u.removeEntityListener(this);
        }
        tabPanels.forEach(t -> t.destroy());

        super.destroy();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " : " + entity.getName();
    }
}
