 /*
 * Mars Simulation Project
 * ParameterCategory.java
 * @date 2023-09-03
 * @author Barry Evans
 */
package com.mars_sim.core.parameter;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.mars_sim.tools.Msg;

/**
 * Category for a set of Parameter values. Each one is defined in terms of a ParameterSpec.
 * The spec defines the type of the value, the display name, and the id.
 * The id is used as the key for an ParameterManager values.
 */
public class ParameterCategory implements Serializable {

    private static final long serialVersionUID = 1L;

	/**
     * Definition of a single Parameter value.
     */
    public record ParameterSpec(String id, String displayName, ParameterValueType type) {}

    private String id;
    private transient String name;
    private transient Map<String,ParameterSpec> range;

    /**
     * Creates new category with the specific id.
     * 
     * @param id
     */
    public ParameterCategory(String id) {
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
     * Gets the possible parameters that fit into this category.
     * 
     * @return Could be an empty collection if the parameters are dynamic.
     */
    public final Collection<ParameterSpec> getRange() {
        if (range == null) {
            range = calculateSpecs();
        }
        return range.values();
    }

    /**
     * Finds the spec of a single parameter value.
     * 
     * @param id
     * @return
     */
    public ParameterSpec getSpec(String id) {
        if (range == null) {
            range = calculateSpecs();
        }
        return range.get(id);
    }

    /**
     * Subclasses should override this method to provide the list of specs.
     * 
     * @return Default implementation return an empty Map
     */
    protected Map<String, ParameterSpec> calculateSpecs() {
        return Collections.emptyMap();
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