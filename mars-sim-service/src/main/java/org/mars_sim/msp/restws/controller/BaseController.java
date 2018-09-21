package org.mars_sim.msp.restws.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BaseController {

	public BaseController() {
		super();
	}

	protected <T> List<T> filter(List<T> source, int page, int pageSize) {
		int start = 0;
		int end = Integer.MAX_VALUE;
		
		if (page  > 0) {
			start = (page - 1) * pageSize;
			end = start + pageSize;
		}
		end = (end < source.size() ? end : (source.size() - 1));	
		return source.subList(start, end);
	}

	protected <T> List<T> filter(Collection<T> source, int page, int pageSize) {
		return filter(new ArrayList<T>(source), page, pageSize);
	}

}