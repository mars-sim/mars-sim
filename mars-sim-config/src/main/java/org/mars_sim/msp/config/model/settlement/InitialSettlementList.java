/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.settlement;

/**
 * Class InitialSettlementList.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class InitialSettlementList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _settlementList.
     */
    private java.util.List<org.mars_sim.msp.config.model.settlement.Settlement> _settlementList;


      //----------------/
     //- Constructors -/
    //----------------/

    public InitialSettlementList() {
        super();
        this._settlementList = new java.util.ArrayList<org.mars_sim.msp.config.model.settlement.Settlement>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vSettlement
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addSettlement(
            final org.mars_sim.msp.config.model.settlement.Settlement vSettlement)
    throws java.lang.IndexOutOfBoundsException {
        this._settlementList.add(vSettlement);
    }

    /**
     * 
     * 
     * @param index
     * @param vSettlement
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addSettlement(
            final int index,
            final org.mars_sim.msp.config.model.settlement.Settlement vSettlement)
    throws java.lang.IndexOutOfBoundsException {
        this._settlementList.add(index, vSettlement);
    }

    /**
     * Method enumerateSettlement.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.settlement.Settlement> enumerateSettlement(
    ) {
        return java.util.Collections.enumeration(this._settlementList);
    }

    /**
     * Method getSettlement.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.settlement.Settlement at the
     * given index
     */
    public org.mars_sim.msp.config.model.settlement.Settlement getSettlement(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._settlementList.size()) {
            throw new IndexOutOfBoundsException("getSettlement: Index value '" + index + "' not in range [0.." + (this._settlementList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.settlement.Settlement) _settlementList.get(index);
    }

    /**
     * Method getSettlement.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.settlement.Settlement[] getSettlement(
    ) {
        org.mars_sim.msp.config.model.settlement.Settlement[] array = new org.mars_sim.msp.config.model.settlement.Settlement[0];
        return (org.mars_sim.msp.config.model.settlement.Settlement[]) this._settlementList.toArray(array);
    }

    /**
     * Method getSettlementCount.
     * 
     * @return the size of this collection
     */
    public int getSettlementCount(
    ) {
        return this._settlementList.size();
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    public boolean isValid(
    ) {
        try {
            validate();
        } catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    }

    /**
     * Method iterateSettlement.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.settlement.Settlement> iterateSettlement(
    ) {
        return this._settlementList.iterator();
    }

    /**
     * 
     * 
     * @param out
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void marshal(
            final java.io.Writer out)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Marshaller.marshal(this, out);
    }

    /**
     * 
     * 
     * @param handler
     * @throws java.io.IOException if an IOException occurs during
     * marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     */
    public void marshal(
            final org.xml.sax.ContentHandler handler)
    throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Marshaller.marshal(this, handler);
    }

    /**
     */
    public void removeAllSettlement(
    ) {
        this._settlementList.clear();
    }

    /**
     * Method removeSettlement.
     * 
     * @param vSettlement
     * @return true if the object was removed from the collection.
     */
    public boolean removeSettlement(
            final org.mars_sim.msp.config.model.settlement.Settlement vSettlement) {
        boolean removed = _settlementList.remove(vSettlement);
        return removed;
    }

    /**
     * Method removeSettlementAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.settlement.Settlement removeSettlementAt(
            final int index) {
        java.lang.Object obj = this._settlementList.remove(index);
        return (org.mars_sim.msp.config.model.settlement.Settlement) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vSettlement
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setSettlement(
            final int index,
            final org.mars_sim.msp.config.model.settlement.Settlement vSettlement)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._settlementList.size()) {
            throw new IndexOutOfBoundsException("setSettlement: Index value '" + index + "' not in range [0.." + (this._settlementList.size() - 1) + "]");
        }

        this._settlementList.set(index, vSettlement);
    }

    /**
     * 
     * 
     * @param vSettlementArray
     */
    public void setSettlement(
            final org.mars_sim.msp.config.model.settlement.Settlement[] vSettlementArray) {
        //-- copy array
        _settlementList.clear();

        for (int i = 0; i < vSettlementArray.length; i++) {
                this._settlementList.add(vSettlementArray[i]);
        }
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * org.mars_sim.msp.config.model.settlement.InitialSettlementLis
     */
    public static org.mars_sim.msp.config.model.settlement.InitialSettlementList unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.settlement.InitialSettlementList) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.settlement.InitialSettlementList.class, reader);
    }

    /**
     * 
     * 
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate(
    )
    throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
