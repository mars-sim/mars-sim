 /*
 * Mars Simulation Project
 * ParameterEnumCategory.java
 * @date 2025-10-12
 * @author Barry Evans
 */
package com.mars_sim.core.parameter;

import com.mars_sim.core.Named;

/**
 * A ParameterCategory that defines parameters based on an Enum type.
 * Each enum value is used to define a parameter in the category.
 */
public abstract class ParameterEnumCategory<T extends Enum<T>> 
                extends ParameterCategory {

    /**
     * Creates a new category with the specific id and parameter value type.
     * @param id The unique id for this category
     * @param type The type of value for each parameter in this category
     */
    protected ParameterEnumCategory(String id, ParameterValueType type, Class<T> enumClass) {
        super(id);

        for(var t : enumClass.getEnumConstants()) {
            String display = t.name();
            if (t instanceof Named n) {
                display = n.getName();
            }
            addParameter(t.name(), display, type);
        }
    }

    /**
     * Get the key for the specific enum type.
     * @param type
     * @return
     */
    public ParameterKey getKey(T type) {
        return getKey(type.name());
    }
}
