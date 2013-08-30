/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.types;

/**
 * Enumeration LocaleNameType.
 * 
 * @version $Revision$ $Date$
 */
public enum LocaleNameType {


      //------------------/
     //- Enum Constants -/
    //------------------/

    /**
     * Constant VOLCANIC
     */
    VOLCANIC("volcanic"),
    /**
     * Constant SEDIMENTARY
     */
    SEDIMENTARY("sedimentary"),
    /**
     * Constant CRATER
     */
    CRATER("crater");

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field value.
     */
    private final java.lang.String value;

    /**
     * Field enumConstants.
     */
    private static final java.util.Map<java.lang.String, LocaleNameType> enumConstants = new java.util.HashMap<java.lang.String, LocaleNameType>();


    static {
        for (LocaleNameType c: LocaleNameType.values()) {
            LocaleNameType.enumConstants.put(c.value, c);
        }

    };


      //----------------/
     //- Constructors -/
    //----------------/

    private LocaleNameType(final java.lang.String value) {
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
    public static org.mars_sim.msp.config.model.types.LocaleNameType fromValue(
            final java.lang.String value) {
        LocaleNameType c = LocaleNameType.enumConstants.get(value);
        if (c != null) {
            return c;
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
