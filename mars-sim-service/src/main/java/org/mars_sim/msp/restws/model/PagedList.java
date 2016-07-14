package org.mars_sim.msp.restws.model;

import java.util.List;

/**
 * A single Page returned from a larger source list.
 *
 * @param <T> The DTO object being returned
 */
public class PagedList<T> {
	private int totalSize;
	private int pageNumber;
	private int pageSize;
	private List<T> items;
	
	public PagedList(List<T> items, int pageNumber, int pageSize, int totalSize) {
		super();
		this.items = items;
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		this.totalSize = totalSize;
	}

	public int getTotalSize() {
		return totalSize;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public int getPageSize() {
		return pageSize;
	}

	public List<T> getItems() {
		return items;
	}
	
	
}
