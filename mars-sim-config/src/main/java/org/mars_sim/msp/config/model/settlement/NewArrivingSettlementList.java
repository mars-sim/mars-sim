/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.settlement;

/**
 * Class NewArrivingSettlementList.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class NewArrivingSettlementList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _arrivingSettlementList.
     */
    private java.util.List<org.mars_sim.msp.config.model.settlement.ArrivingSettlement> _arrivingSettlementList;


      //----------------/
     //- Constructors -/
    //----------------/

    public NewArrivingSettlementList() {
        super();
        this._arrivingSettlementList = new java.util.ArrayList<org.mars_sim.msp.config.model.settlement.ArrivingSettlement>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vArrivingSettlement
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addArrivingSettlement(
            final org.mars_sim.msp.config.model.settlement.ArrivingSettlement vArrivingSettlement)
    throws java.lang.IndexOutOfBoundsException {
        this._arrivingSettlementList.add(vArrivingSettlement);
    }

    /**
     * 
     * 
     * @param index
     * @param vArrivingSettlement
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addArrivingSettlement(
            final int index,
            final org.mars_sim.msp.config.model.settlement.ArrivingSettlement vArrivingSettlement)
    throws java.lang.IndexOutOfBoundsException {
        this._arrivingSettlementList.add(index, vArrivingSettlement);
    }

    /**
     * Method enumerateArrivingSettlement.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.settlement.ArrivingSettlement> enumerateArrivingSettlement(
    ) {
        return java.util.Collections.enumeration(this._arrivingSettlementList);
    }

    /**
     * Method getArrivingSettlement.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.settlement.ArrivingSettlement
     * at the given index
     */
    public org.mars_sim.msp.config.model.settlement.ArrivingSettlement getArrivingSettlement(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._arrivingSettlementList.size()) {
            throw new IndexOutOfBoundsException("getArrivingSettlement: Index value '" + index + "' not in range [0.." + (this._arrivingSettlementList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.settlement.ArrivingSettlement) _arrivingSettlementList.get(index);
    }

    /**
     * Method getArrivingSettlement.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.settlement.ArrivingSettlement[] getArrivingSettlement(
    ) {
        org.mars_sim.msp.config.model.settlement.ArrivingSettlement[] array = new org.mars_sim.msp.config.model.settlement.ArrivingSettlement[0];
        return (org.mars_sim.msp.config.model.settlement.ArrivingSettlement[]) this._arrivingSettlementList.toArray(array);
    }

    /**
     * Method getArrivingSettlementCount.
     * 
     * @return the size of this collection
     */
    public int getArrivingSettlementCount(
    ) {
        return this._arrivingSettlementList.size();
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
     * Method iterateArrivingSettlement.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.settlement.ArrivingSettlement> iterateArrivingSettlement(
    ) {
        return this._arrivingSettlementList.iterator();
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
    public void removeAllArrivingSettlement(
    ) {
        this._arrivingSettlementList.clear();
    }

    /**
     * Method removeArrivingSettlement.
     * 
     * @param vArrivingSettlement
     * @return true if the object was removed from the collection.
     */
    public boolean removeArrivingSettlement(
            final org.mars_sim.msp.config.model.settlement.ArrivingSettlement vArrivingSettlement) {
        boolean removed = _arrivingSettlementList.remove(vArrivingSettlement);
        return removed;
    }

    /**
     * Method removeArrivingSettlementAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.settlement.ArrivingSettlement removeArrivingSettlementAt(
            final int index) {
        java.lang.Object obj = this._arrivingSettlementList.remove(index);
        return (org.mars_sim.msp.config.model.settlement.ArrivingSettlement) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vArrivingSettlement
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setArrivingSettlement(
            final int index,
            final org.mars_sim.msp.config.model.settlement.ArrivingSettlement vArrivingSettlement)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._arrivingSettlementList.size()) {
            throw new IndexOutOfBoundsException("setArrivingSettlement: Index value '" + index + "' not in range [0.." + (this._arrivingSettlementList.size() - 1) + "]");
        }

        this._arrivingSettlementList.set(index, vArrivingSettlement);
    }

    /**
     * 
     * 
     * @param vArrivingSettlementArray
     */
    public void setArrivingSettlement(
            final org.mars_sim.msp.config.model.settlement.ArrivingSettlement[] vArrivingSettlementArray) {
        //-- copy array
        _arrivingSettlementList.clear();

        for (int i = 0; i < vArrivingSettlementArray.length; i++) {
                this._arrivingSettlementList.add(vArrivingSettlementArray[i]);
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
     * org.mars_sim.msp.config.model.settlement.NewArrivingSettlementList
     */
    public static org.mars_sim.msp.config.model.settlement.NewArrivingSettlementList unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.settlement.NewArrivingSettlementList) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.settlement.NewArrivingSettlementList.class, reader);
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
