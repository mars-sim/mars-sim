/*
 * Mars Simulation Project
 * ParameterManager.java
 * @date 2024-01-02
 * @author Barry Evans
 */
package com.mars_sim.core.parameter;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is manages a set of user configurable Parameters that can be used to control
 * aspects of the simulaton
 */
public class ParameterManager implements Serializable {
    
    /**
     * The key used for Parameters
     */
    public record ParameterKey(ParameterCategory category, String id)
                    implements Serializable {}
    
    private Map<ParameterKey,Serializable> values = new HashMap<>();

    public ParameterManager() {
    }

    /**
     * This combines a list of values from Parameter managers into a single list. If there are the 
     * same key then the following rules are applied to resolve:
     * - Double & Integer; sum the values
     * - Boolean take the logical OR of the values.
     * @param list
     */
    public ParameterManager(List<ParameterManager> list) {
        for(var m : list) {
            for(var e : m.values.entrySet()) {
                // Compare incoming value to the existing
                Object existing = values.get(e.getKey());
                if (existing != null) {
                    // Compare
                    var existingType = existing.getClass();
                    if (existingType.equals(Double.class)) {
                        values.put(e.getKey(), ((Double)existing).doubleValue() +
                                                    ((Double)e.getValue()).doubleValue());
                    }
                    else if (existingType.equals(Integer.class)) {
                        values.put(e.getKey(), ((Integer)existing).intValue() +
                                                     ((Integer)e.getValue()).intValue());
                    }
                    else if (existingType.equals(Boolean.class)) {
                        values.put(e.getKey(), ((Boolean)existing || (Boolean)e.getValue()));
                    }
                }
                else {
                    // Add it
                    values.put(e.getKey(), e.getValue());
                }
            }
        }
    }

    /**
     * Put a new parameter value into the manager
     * @param category Categogory fo the new value
     * @param id Identifier of the value being defined
     * @param value Actual new value
     */
    public void putValue(ParameterCategory category, String id, Serializable value) {
        values.put(new ParameterKey(category, id), value);
    }

    /**
     * Get the known values held in the manager.
     * @return Keys and associated values.
     */
    public Map<ParameterKey,Object> getValues() {
        return Collections.unmodifiableMap(values);
    }

    /**
     * Get a parameter value that is type Double. 
     * @param category Category of the value
     * @param id Identifier of the value
     * @param defaultValue Default value if is is not defined
     * @return Found value matching category & id or the default
     */
    public double getDoubleValue(ParameterCategory category, String id, double defaultValue) {
        var key = new ParameterKey(category, id);
        var value = values.get(key);
        if (value == null) {
            return defaultValue;
        }
        return (Double)value;
    }

    /**
     * Get a parameter value that is type Integer. 
     * @param category Category of the value
     * @param id Identifier of the value
     * @param defaultValue Default value if is is not defined
     * @return Found value matching category & id or the default
     */
    public int getIntValue(ParameterCategory category, String id, int defaultValue) {
        var key = new ParameterKey(category, id);
        var value = values.get(key);
        if (value == null) {
            return defaultValue;
        }
        return (Integer)value;
    }

    /**
     * Get a parameter value that is type Boolean. 
     * @param category Category of the value
     * @param id Identifier of the value
     * @param defaultValue Default value if is is not defined
     * @return Found value matching category & id or the default
     */
    public boolean getBooleanValue(ParameterCategory category, String id, boolean defaultValue) {
        var key = new ParameterKey(category, id);
        var value = values.get(key);
        if (value == null) {
            return defaultValue;
        }
        return (Boolean)value;
    }

    /**
     * Reset all values to a new set of values.
     * @param preferences Source of new values.
     */
    public void resetValues(ParameterManager preferences) {
        values.clear();
        values.putAll(preferences.values);
    }
}
