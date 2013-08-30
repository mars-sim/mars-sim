/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.types;

/**
 * Enumeration MineralFrequencyType.
 * 
 * @version $Revision$ $Date$
 */
public enum MineralFrequencyType {


      //------------------/
     //- Enum Constants -/
    //------------------/

    /**
     * Constant COMMON
     */
    COMMON("common"),
    /**
     * Constant UNCOMMON
     */
    UNCOMMON("uncommon"),
    /**
     * Constant RARE
     */
    RARE("rare"),
    /**
     * Constant VERY_RARE
     */
    VERY_RARE("very_rare");

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
    private static final java.util.Map<java.lang.String, MineralFrequencyType> enumConstants = new java.util.HashMap<java.lang.String, MineralFrequencyType>();


    static {
        for (MineralFrequencyType c: MineralFrequencyType.values()) {
            MineralFrequencyType.enumConstants.put(c.value, c);
        }

    };


      //----------------/
     //- Constructors -/
    //----------------/

    private MineralFrequencyType(final java.lang.String value) {
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
    public static org.mars_sim.msp.config.model.types.MineralFrequencyType fromValue(
            final java.lang.String value) {
        MineralFrequencyType c = MineralFrequencyType.enumConstants.get(value);
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
