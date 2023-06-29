/*
 * Mars Simulation Project
 * PreferenceKey.java
 * @date 2023-06-09
 * @author Barry Evans
 */
package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;

/**
 * Represent a type of preference defined by a Mission agenda.
 */
public class PreferenceKey implements Serializable {
    /**
     * Type of Preference
     */
    public enum Type {
        TASK, MISSION, SCIENCE, CONFIGURATION
    };

    private Type type;
    private String name;
    
    public PreferenceKey(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + type.hashCode();
        result = prime * result + name.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PreferenceKey other = (PreferenceKey) obj;
        if (type != other.type)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PreferenceKey [type=" + type + ", name=" + name + "]";
    }
}
