/*
 * Mars Simulation Project
 * ScientificStudyEvent.java
 * @date 2023-10-19
 * @author Scott Davis
 */
package com.mars_sim.core.science;

import java.util.EventObject;

import com.mars_sim.core.person.Person;

/**
 * A scientific study update event.
 */
public class ScientificStudyEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	// Scientific study event types.
    public static final String STUDY_COMPLETION_EVENT = "study completion event";
    public static final String PHASE_CHANGE_EVENT = "study phase change event";
    public static final String PROPOSAL_WORK_EVENT = "study proposal work event";
    public static final String ADD_COLLABORATOR_EVENT = "add study collaborator event";
    public static final String REMOVE_COLLABORATOR_EVENT = "remove study collaborator event";
    public static final String PRIMARY_RESEARCH_WORK_EVENT = "study primary research work event";
    public static final String COLLABORATION_RESEARCH_WORK_EVENT = "study collaboration research work event";
    public static final String PRIMARY_PAPER_WORK_EVENT = "study primary paper work event";
    public static final String COLLABORATION_PAPER_WORK_EVENT = "study collaboration paper work event";
    
    // Data members
    private ScientificStudy study;
    private Person researcher;
    private String type;
    
    /**
     * Constructor 1.
     * 
     * @param study the scientific study.
     * @param type the event type.
     */
    public ScientificStudyEvent(ScientificStudy study, String type) {
        this(study, null, type);
    }
    
    /**
     * Constructor 2.
     * 
     * @param study the scientific study.
     * @param researcher the study researcher.
     * @param type the event type.
     */
    public ScientificStudyEvent(ScientificStudy study, Person researcher, String type) {
        // Use EventObject
        super(study);
        
        // Initialize data members.
        this.study = study;
        this.researcher = researcher;
        this.type = type;
    }
    
    /**
     * Gets the scientific study.
     * 
     * @return study.
     */
    public ScientificStudy getStudy() {
        return study;
    }
    
    /**
     * Gets the study researcher the event is about.
     * 
     * @return researcher or null if none.
     */
    public Person getResearcher() {
        return researcher;
    }
    
    /**
     * Gets the event type.
     * 
     * @return event type string.
     */
    public String getType() {
        return type;
    }
}
