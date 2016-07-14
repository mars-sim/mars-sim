package org.mars_sim.msp.restws.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.restws.model.StoredAmount;
import org.mars_sim.msp.restws.model.StoredItem;
import org.springframework.stereotype.Component;

/**
 * This is a custom mapper to exrract the various entity groups out of an Inventory object.
 */
@Component
public class InventoryMapper {
	private static final boolean USE_DIRTY = false;

	/**
	 * This creates a list of resources that are stored in an Inventory.
	 * @param inventory The inventory to describe.
	 * @return List of stored resources.
	 */
	public List<StoredAmount> getAmounts(Inventory inventory) {
		List<StoredAmount> results = new ArrayList<StoredAmount>();
		Set<AmountResource> resources = inventory.getAllAmountResourcesStored(USE_DIRTY);
		for (AmountResource resource : resources) {
			results.add(new StoredAmount(resource,
										   inventory.getAmountResourceStored(resource, USE_DIRTY),
										   inventory.getAmountResourceCapacity(resource, USE_DIRTY)));
		}
		return results;
	}

	/**
	 * This creates a list of stored items that are in an Inventory.
	 * @param inventory The inventory to describe.
	 * @return List of stored item.
	 */
	public List<StoredItem> getItems(Inventory inventory) {
		List<StoredItem> results = new ArrayList<StoredItem>();
		Set<ItemResource> items = inventory.getAllItemResourcesStored();
		for (ItemResource item : items) {
			results.add(new StoredItem(item, inventory.getItemResourceNum(item)));
		}
		return results;
	}
}
