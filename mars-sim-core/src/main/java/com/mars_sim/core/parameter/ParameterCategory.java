 /*
 * Mars Simulation Project
 * ParameterCategory.java
 * @date 2023-09-03
 * @author Barry Evans
 */
package com.mars_sim.core.parameter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mars_sim.core.tool.Msg;

/**
 * Category for a set of Parameter values. Each one is defined in terms of a ParameterSpec.
 * The spec defines the type of the value, the display name, and the id.
 * The id is used as the key for an ParameterManager values.
 */
public abstract class ParameterCategory {

	/**
     * Definition of a single Parameter value.
     */
    public record ParameterSpec(String displayName, ParameterValueType type) {}

    private String id;
    private String name;
    private Map<ParameterKey,ParameterSpec> range = new HashMap<>();

    /**
     * Creates new category with the specific id.
     * 
     * @param id
     */
    protected ParameterCategory(String id) {
        this.id = id;
    }

    /**
     * Gets the id of this category.
     * 
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * The internationalized name of this category based on the id.
     * Uses an entry in the message bundle called 'PreferenceCategory.<id>'
     */
    public String getName() {
        if (name == null) {
            name = Msg.getString("PreferenceCategory." + id.toLowerCase());
        }
        return name;
    }

    /**
     * Register a new parameter in this category.
     * @param id Unique id within this category
     * @param displayName Display name for this parameter
     * @param type Type of value for this parameter
     * @return The key for this parameter
     */
    protected ParameterKey addParameter(String id, String displayName, ParameterValueType type) {
        var spec = new ParameterSpec(displayName, type);
        var key = new ParameterKey(this, id);

        range.put(key, spec);
        return key;
    }

    /**
     * Get the range of parameters key and their specs.
     * @return
     */
    public Map<ParameterKey, ParameterSpec> getRange() {
        return Collections.unmodifiableMap(range);
    }

    /**
     * Finds the spec of a single parameter value.
     * 
     * @param id Key to find spec
     * @return
     */
    public ParameterSpec getSpec(ParameterKey id) {
        return range.get(id);
    }

    /**
     * Get the key for the registered parameter name.
     * 
     * @return key or null if there is no such parameter
     */
    public ParameterKey getKey(String pName) {
        for (var entry : range.keySet()) {
            if (entry.getId().equals(pName)) {
                return entry;
            }
        }
        return createMissingKey(pName);
    }

    /**
     * This method is called when a key is requested that does not exist.
     * The default implementation throws an exception, but subclasses can override it to provide a different behavior.
     * @param pName
     * @return
     */
    protected ParameterKey createMissingKey(String pName) {
        throw new IllegalArgumentException("No such parameter " + pName + " in category " + id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ParameterCategory other = (ParameterCategory) obj;
        return id.equals(other.id);
    }
}