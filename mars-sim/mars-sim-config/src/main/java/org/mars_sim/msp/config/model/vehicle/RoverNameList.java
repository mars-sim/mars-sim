/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.vehicle;

/**
 * Class RoverNameList.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class RoverNameList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _roverNameList.
     */
    private java.util.List<org.mars_sim.msp.config.model.vehicle.RoverName> _roverNameList;


      //----------------/
     //- Constructors -/
    //----------------/

    public RoverNameList() {
        super();
        this._roverNameList = new java.util.ArrayList<org.mars_sim.msp.config.model.vehicle.RoverName>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vRoverName
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addRoverName(
            final org.mars_sim.msp.config.model.vehicle.RoverName vRoverName)
    throws java.lang.IndexOutOfBoundsException {
        this._roverNameList.add(vRoverName);
    }

    /**
     * 
     * 
     * @param index
     * @param vRoverName
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addRoverName(
            final int index,
            final org.mars_sim.msp.config.model.vehicle.RoverName vRoverName)
    throws java.lang.IndexOutOfBoundsException {
        this._roverNameList.add(index, vRoverName);
    }

    /**
     * Method enumerateRoverName.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.vehicle.RoverName> enumerateRoverName(
    ) {
        return java.util.Collections.enumeration(this._roverNameList);
    }

    /**
     * Method getRoverName.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.vehicle.RoverName at the given
     * index
     */
    public org.mars_sim.msp.config.model.vehicle.RoverName getRoverName(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._roverNameList.size()) {
            throw new IndexOutOfBoundsException("getRoverName: Index value '" + index + "' not in range [0.." + (this._roverNameList.size() - 1) + "]");
        }

        return _roverNameList.get(index);
    }

    /**
     * Method getRoverName.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.vehicle.RoverName[] getRoverName(
    ) {
        org.mars_sim.msp.config.model.vehicle.RoverName[] array = new org.mars_sim.msp.config.model.vehicle.RoverName[0];
        return this._roverNameList.toArray(array);
    }

    /**
     * Method getRoverNameCount.
     * 
     * @return the size of this collection
     */
    public int getRoverNameCount(
    ) {
        return this._roverNameList.size();
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
     * Method iterateRoverName.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.vehicle.RoverName> iterateRoverName(
    ) {
        return this._roverNameList.iterator();
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
    public void removeAllRoverName(
    ) {
        this._roverNameList.clear();
    }

    /**
     * Method removeRoverName.
     * 
     * @param vRoverName
     * @return true if the object was removed from the collection.
     */
    public boolean removeRoverName(
            final org.mars_sim.msp.config.model.vehicle.RoverName vRoverName) {
        boolean removed = _roverNameList.remove(vRoverName);
        return removed;
    }

    /**
     * Method removeRoverNameAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.vehicle.RoverName removeRoverNameAt(
            final int index) {
        java.lang.Object obj = this._roverNameList.remove(index);
        return (org.mars_sim.msp.config.model.vehicle.RoverName) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vRoverName
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setRoverName(
            final int index,
            final org.mars_sim.msp.config.model.vehicle.RoverName vRoverName)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._roverNameList.size()) {
            throw new IndexOutOfBoundsException("setRoverName: Index value '" + index + "' not in range [0.." + (this._roverNameList.size() - 1) + "]");
        }

        this._roverNameList.set(index, vRoverName);
    }

    /**
     * 
     * 
     * @param vRoverNameArray
     */
    public void setRoverName(
            final org.mars_sim.msp.config.model.vehicle.RoverName[] vRoverNameArray) {
        //-- copy array
        _roverNameList.clear();

        for (int i = 0; i < vRoverNameArray.length; i++) {
                this._roverNameList.add(vRoverNameArray[i]);
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
     * org.mars_sim.msp.config.model.vehicle.RoverNameList
     */
    public static org.mars_sim.msp.config.model.vehicle.RoverNameList unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.vehicle.RoverNameList) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.vehicle.RoverNameList.class, reader);
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
