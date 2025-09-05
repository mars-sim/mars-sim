/*
 * Mars Simulation Project
 * CategoryKey.java
 * @date 2023-12-27
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import com.mars_sim.core.Unit;
import com.mars_sim.core.building.function.farming.Crop;
import com.mars_sim.core.food.Food;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.structure.Settlement;

/**
 * This represents the unique identifiers of a row in the CategoryTableModel.
 */
class CategoryKey<T> {
    private Settlement host;
    private T category;

    public CategoryKey(Settlement host, T category) {
        this.host = host;
        this.category = category;
    }

    public Settlement getSettlement() {
        return host;
    }
    public T getCategory() {
        return category;
    }
    
    public String getName() {
    	Object obj = ((Object) category);
    	if (obj instanceof Unit u)
    		return u.getName();
    	else if (obj instanceof Good g)
    		return g.getName();
    	else if (obj instanceof Food f)
    		return f.getName();
    	else if (obj instanceof Crop c)
    		return c.getName();
    	
    	return "";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        var other = (CategoryKey<T>) obj;
        if (host == null) {
            if (other.host != null)
                return false;
        } else if (!host.equals(other.host))
            return false;
        if (category == null) {
            if (other.category != null)
                return false;
        } else if (!category.equals(other.category))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        return result;
    }
}
