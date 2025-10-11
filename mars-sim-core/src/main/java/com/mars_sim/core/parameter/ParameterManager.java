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
 * aspects of the simulation.
 */
public class ParameterManager implements Serializable {
    
    private static final long serialVersionUID = 1L;

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
     * Puts a new parameter value into the manager.
     * 
     * @param category Category for the new value
     * @param id Identifier of the value being defined
     * @param value Actual new value
     */
    @Deprecated
    public void putValue(ParameterCategory category, String id, Serializable value) {
        var key = new ParameterKey(category, id);
        if (category.getSpec(key) == null) {
            throw new IllegalArgumentException("No such parameter defined: " + category.getId() + "." + id);
        }

        putValue(key, value);
    }

    /**
     * Puts a new parameter value into the manager.
     * @param key Key for the new value
     * @param value Actual new value
     */
    public void putValue(ParameterKey key, Serializable value) { 
        values.put(key, value);
    }
    
    /**
     * Removes a value from the parameter manager.
     * 
     * @param key The key of the value to be removed
     */
    public void removeValue(ParameterKey key) {
        values.remove(key);
    }

    /**
     * Gets the known values held in the manager.
     * 
     * @return Keys and associated values.
     */
    public Map<ParameterKey,Object> getValues() {
        return Collections.unmodifiableMap(values);
    }

    /**
     * Gets a parameter value that is type Double. 
     * 
     * @param category Category of the value
     * @param id Identifier of the value
     * @param defaultValue Default value if is is not defined
     * @return Found value matching category & id or the default
     */
    @Deprecated
    public double getDoubleValue(ParameterCategory category, String id, double defaultValue) {
        var key = new ParameterKey(category, id);
        return getDoubleValue(key, defaultValue);
    }

    /**
     * Gets a parameter value that is type Double. 
     * 
     * @param key Key of the value
     * @param defaultValue Default value if is is not defined
     * @return Found value matching category & id or the default
     */
    public double getDoubleValue(ParameterKey key, double defaultValue) {
        var value = values.get(key);
        if (value == null) {
            return defaultValue;
        }
        return (Double)value;
    }

    /**
     * Gets a parameter value that is type Integer. 
     * 
     * @param category Category of the value
     * @param id Identifier of the value
     * @param defaultValue Default value if is is not defined
     * @return Found value matching category & id or the default
     */
    @Deprecated
    public int getIntValue(ParameterCategory category, String id, int defaultValue) {
        var key = new ParameterKey(category, id);
        return getIntValue(key, defaultValue);
    }

    /**
     * Gets a parameter value that is type Integer. 
     * 
     * @param key Key of the value
     * @param defaultValue Default value if is is not defined
     * @return Found value matching category & id or the default
     */
    public int getIntValue(ParameterKey key, int defaultValue) {
        var value = values.get(key);
        if (value == null) {
            return defaultValue;
        }
        return (Integer)value;
    }

    /**
     * Gets a parameter value that is type Boolean.
     *  
     * @param category Category of the value
     * @param key Key of the value
     * @return Found value matching category & id or the default
     */
    public boolean getBooleanValue(ParameterKey key, boolean defaultValue) {

        var value = values.get(key);
        if (value == null) {
            return defaultValue;
        }
        return (Boolean)value;
    }

    /**
     * Resets all values to a new set of values.
     * 
     * @param preferences Source of new values.
     */
    public void resetValues(ParameterManager preferences) {
        values.clear();
        values.putAll(preferences.values);
    }
}
