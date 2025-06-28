/*
 * Mars Simulation Project
 * FieldStudyObjectives.java
 * @date 2025-06-22
 * @author Barry Evans
 */
package com.mars_sim.core.mission.objectives;

import com.mars_sim.core.mission.MissionObjective;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;

/**
 * THis represents the objectives for a field study mission.
 */
public class FieldStudyObjectives implements MissionObjective {

    private static final long serialVersionUID = 1L;

    /** Scientific study to research. */
    private ScientificStudy study;

    private double fieldSiteTime;

    private ScienceType science;


    /**
     * Constructor.
     * 
     * @param study     the scientific study to be conducted.
     * @param science   the type of science being studied.
     * @param fieldSiteTime   time on site
     */
    public FieldStudyObjectives(ScientificStudy study, ScienceType science, double fieldSiteTime){
        this.fieldSiteTime = fieldSiteTime;
        this.study = study;
        this.science = science;
    }

    public ScientificStudy getStudy() {
        return study;
    }

    public double getFieldSiteTime() {
        return fieldSiteTime;
    }

    public ScienceType getScience() {
        return science;
    }

    @Override
    public String getName() {
        return "Field Study: " + science.getName();
    }
}
