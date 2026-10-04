/*
 * Mars Simulation Project
 * MsgContext.java
 * @date 2026-07-06
 * @author Barry Evans
 */
package com.mars_sim.core.tool;

import java.io.Serializable;

/**
 * Represents a message context for internationalization, containing a key and associated content.
 * @param key the key for the message
 * @param content the content associated with the key
 */
public record MsgContext(String key, Object content) implements Serializable {

    /**
     * Get a localised message based on the key and content.
     * @return the localised message as a String
     */
    public String getMessage() {
        return Msg.getString(key, content);
    }
}
