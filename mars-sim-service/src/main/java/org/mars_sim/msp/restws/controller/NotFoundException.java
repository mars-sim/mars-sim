package org.mars_sim.msp.restws.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This should be part of the generic UnitManager template that is lacking in the core
 */
@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="No such entity")
public class NotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public NotFoundException(String type, int identifier) {
		super(type + " " + identifier + " not found");
	}
}
