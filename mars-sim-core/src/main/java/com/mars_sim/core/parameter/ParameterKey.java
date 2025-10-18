/*
 * Mars Simulation Project
 * ParameterKey.java
 * @date 2025-10-12
 * @author Barry Evans
 */
package com.mars_sim.core.parameter;

import java.io.Serializable;

/**
 * The key used for Parameters in the ParameterManager.
 * This record encapsulates a parameter category and its identifier.
 */
public class ParameterKey implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // This is not serialised
    private transient ParameterCategory category = null;

    private String id;
    private String categoryId;

    /**
     * Creates a new ParameterKey with the specified category and identifier.
     * 
     * @param category The parameter category
     * @param id The parameter identifier
     */
    public ParameterKey(ParameterCategory category, String id) {
        this.category = category;
        this.id = id;
        this.categoryId = category.getId();
    }

    /**
     * Gets the ParameterCategory for this key.
     * @return
     */
    public ParameterCategory getCategory() {
        if (category == null) {
            category = ParameterCategories.getCategory(categoryId);
        }
        return category;
    }

    /**
     * Gets the identifier of this parameter key.
     * 
     * @return The parameter identifier
     */
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "ParameterKey [id=" + id + ", categoryId=" + categoryId + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((categoryId == null) ? 0 : categoryId.hashCode());
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
        ParameterKey other = (ParameterKey) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (categoryId == null) {
            if (other.categoryId != null)
                return false;
        } else if (!categoryId.equals(other.categoryId))
            return false;
        return true;
    }   
}