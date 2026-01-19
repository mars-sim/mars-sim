/*
 * Mars Simulation Project
 * ScientificStudyWindow.java
 * @date 2025-12-15
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow.science;

import java.util.Properties;

import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityContentPanel;

/**
 * This content displays the details of a Scientific Study.
 */
public class ScientificStudyWindow extends EntityContentPanel<ScientificStudy> {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param study the scientific study this window is for.
     * @param context the UI context.
     * @param props Saved properties for the window.
     */
    public ScientificStudyWindow(ScientificStudy study, UIContext context, Properties props) {
        super(study, context);

        setHeading(study.getPrimarySettlement(), "science", 
				Msg.getString("ScientificStudy.science"), study.getScience().getName());

        addDefaultTabPanel(new TabPanelGeneral(study, context));
        addTabPanel(new TabPanelCollaborators(study, context));

        applyProps(props);
    }   

}
