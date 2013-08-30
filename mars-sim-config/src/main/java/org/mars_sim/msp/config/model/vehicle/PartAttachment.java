/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.vehicle;

/**
 * Class PartAttachment.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class PartAttachment implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _numberSlots.
     */
    private long _numberSlots;

    /**
     * keeps track of state for field: _numberSlots
     */
    private boolean _has_numberSlots;

    /**
     * Field _partList.
     */
    private java.util.List<org.mars_sim.msp.config.model.vehicle.Part> _partList;


      //----------------/
     //- Constructors -/
    //----------------/

    public PartAttachment() {
        super();
        this._partList = new java.util.ArrayList<org.mars_sim.msp.config.model.vehicle.Part>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vPart
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPart(
            final org.mars_sim.msp.config.model.vehicle.Part vPart)
    throws java.lang.IndexOutOfBoundsException {
        this._partList.add(vPart);
    }

    /**
     * 
     * 
     * @param index
     * @param vPart
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addPart(
            final int index,
            final org.mars_sim.msp.config.model.vehicle.Part vPart)
    throws java.lang.IndexOutOfBoundsException {
        this._partList.add(index, vPart);
    }

    /**
     */
    public void deleteNumberSlots(
    ) {
        this._has_numberSlots= false;
    }

    /**
     * Method enumeratePart.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.vehicle.Part> enumeratePart(
    ) {
        return java.util.Collections.enumeration(this._partList);
    }

    /**
     * Returns the value of field 'numberSlots'.
     * 
     * @return the value of field 'NumberSlots'.
     */
    public long getNumberSlots(
    ) {
        return this._numberSlots;
    }

    /**
     * Method getPart.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.vehicle.Part at the given index
     */
    public org.mars_sim.msp.config.model.vehicle.Part getPart(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._partList.size()) {
            throw new IndexOutOfBoundsException("getPart: Index value '" + index + "' not in range [0.." + (this._partList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.vehicle.Part) _partList.get(index);
    }

    /**
     * Method getPart.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.vehicle.Part[] getPart(
    ) {
        org.mars_sim.msp.config.model.vehicle.Part[] array = new org.mars_sim.msp.config.model.vehicle.Part[0];
        return (org.mars_sim.msp.config.model.vehicle.Part[]) this._partList.toArray(array);
    }

    /**
     * Method getPartCount.
     * 
     * @return the size of this collection
     */
    public int getPartCount(
    ) {
        return this._partList.size();
    }

    /**
     * Method hasNumberSlots.
     * 
     * @return true if at least one NumberSlots has been added
     */
    public boolean hasNumberSlots(
    ) {
        return this._has_numberSlots;
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
     * Method iteratePart.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.vehicle.Part> iteratePart(
    ) {
        return this._partList.iterator();
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
    public void removeAllPart(
    ) {
        this._partList.clear();
    }

    /**
     * Method removePart.
     * 
     * @param vPart
     * @return true if the object was removed from the collection.
     */
    public boolean removePart(
            final org.mars_sim.msp.config.model.vehicle.Part vPart) {
        boolean removed = _partList.remove(vPart);
        return removed;
    }

    /**
     * Method removePartAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.vehicle.Part removePartAt(
            final int index) {
        java.lang.Object obj = this._partList.remove(index);
        return (org.mars_sim.msp.config.model.vehicle.Part) obj;
    }

    /**
     * Sets the value of field 'numberSlots'.
     * 
     * @param numberSlots the value of field 'numberSlots'.
     */
    public void setNumberSlots(
            final long numberSlots) {
        this._numberSlots = numberSlots;
        this._has_numberSlots = true;
    }

    /**
     * 
     * 
     * @param index
     * @param vPart
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setPart(
            final int index,
            final org.mars_sim.msp.config.model.vehicle.Part vPart)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._partList.size()) {
            throw new IndexOutOfBoundsException("setPart: Index value '" + index + "' not in range [0.." + (this._partList.size() - 1) + "]");
        }

        this._partList.set(index, vPart);
    }

    /**
     * 
     * 
     * @param vPartArray
     */
    public void setPart(
            final org.mars_sim.msp.config.model.vehicle.Part[] vPartArray) {
        //-- copy array
        _partList.clear();

        for (int i = 0; i < vPartArray.length; i++) {
                this._partList.add(vPartArray[i]);
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
     * org.mars_sim.msp.config.model.vehicle.PartAttachment
     */
    public static org.mars_sim.msp.config.model.vehicle.PartAttachment unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.vehicle.PartAttachment) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.vehicle.PartAttachment.class, reader);
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
