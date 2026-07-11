/*
 * Mars Simulation Project
 * MissionCreationException.java
 * @date 2026-06-27
 * @author Barry Evans
 */
package com.mars_sim.core.mission;

import com.mars_sim.core.tool.MsgContext;

/**
 * This exception is thrown when a mission cannot be created due to missing or invalid data.
 * It uses an internationalised message key to provide a user-friendly error message.
 */
public class MissionCreationException extends Exception {
    private static final long serialVersionUID = 1L;

    private final MsgContext context;
    
    public MissionCreationException(String key) {
        this.context = new MsgContext(key, null);
    }

    public MissionCreationException(MsgContext message) {
        this.context = message;
    }

    /**
     * Returns the detail message string of this exception.     *
     */
    @Override
    public String getMessage() {
        return context.getMessage();
    }

    public MsgContext getContext() {
        return context;
    }
}
