/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.types;

/**
 * Enumeration ResourcePhaseType.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public enum ResourcePhaseType {


	//------------------/
	//- Enum Constants -/
	//------------------/

	/**
	 * Constant SOLID
	 */
	SOLID("solid"),
	/**
	 * Constant LIQUID
	 */
	LIQUID("liquid"),
	/**
	 * Constant GAS
	 */
	GAS("gas");

	//--------------------------/
	//- Class/Member Variables -/
	//--------------------------/

	/**
	 * Field value.
	 */
	private final java.lang.String value;


	//----------------/
	//- Constructors -/
	//----------------/

	private ResourcePhaseType(final java.lang.String value) {
		this.value = value;
	}


	//-----------/
	//- Methods -/
	//-----------/

	/**
	 * Method fromValue.
	 * 
	 * @param value
	 * @return the constant for this value
	 */
	public static org.mars_sim.msp.config.model.types.ResourcePhaseType fromValue(
			final java.lang.String value) {
		for (ResourcePhaseType c: ResourcePhaseType.values()) {
			if (c.value.equals(value)) {
				return c;
			}
		}
		throw new IllegalArgumentException(value);
	}

	/**
	 * 
	 * 
	 * @param value
	 */
	public void setValue(
			final java.lang.String value) {
	}

	/**
	 * Method toString.
	 * 
	 * @return the value of this constant
	 */
	@Override
	public java.lang.String toString(
	) {
		return this.value;
	}

	/**
	 * Method value.
	 * 
	 * @return the value of this constant
	 */
	public java.lang.String value(
	) {
		return this.value;
	}

}
