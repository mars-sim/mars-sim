/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.building;

/**
 * Class ResourceProcessing.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class ResourceProcessing implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _powerDownLevel.
     */
    private float _powerDownLevel;

    /**
     * keeps track of state for field: _powerDownLevel
     */
    private boolean _has_powerDownLevel;

    /**
     * Field _processList.
     */
    private java.util.List<org.mars_sim.msp.config.model.building.Process> _processList;


      //----------------/
     //- Constructors -/
    //----------------/

    public ResourceProcessing() {
        super();
        this._processList = new java.util.ArrayList<org.mars_sim.msp.config.model.building.Process>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vProcess
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addProcess(
            final org.mars_sim.msp.config.model.building.Process vProcess)
    throws java.lang.IndexOutOfBoundsException {
        this._processList.add(vProcess);
    }

    /**
     * 
     * 
     * @param index
     * @param vProcess
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addProcess(
            final int index,
            final org.mars_sim.msp.config.model.building.Process vProcess)
    throws java.lang.IndexOutOfBoundsException {
        this._processList.add(index, vProcess);
    }

    /**
     */
    public void deletePowerDownLevel(
    ) {
        this._has_powerDownLevel= false;
    }

    /**
     * Method enumerateProcess.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.building.Process> enumerateProcess(
    ) {
        return java.util.Collections.enumeration(this._processList);
    }

    /**
     * Returns the value of field 'powerDownLevel'.
     * 
     * @return the value of field 'PowerDownLevel'.
     */
    public float getPowerDownLevel(
    ) {
        return this._powerDownLevel;
    }

    /**
     * Method getProcess.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.building.Process at the given
     * index
     */
    public org.mars_sim.msp.config.model.building.Process getProcess(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._processList.size()) {
            throw new IndexOutOfBoundsException("getProcess: Index value '" + index + "' not in range [0.." + (this._processList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.building.Process) _processList.get(index);
    }

    /**
     * Method getProcess.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.building.Process[] getProcess(
    ) {
        org.mars_sim.msp.config.model.building.Process[] array = new org.mars_sim.msp.config.model.building.Process[0];
        return (org.mars_sim.msp.config.model.building.Process[]) this._processList.toArray(array);
    }

    /**
     * Method getProcessCount.
     * 
     * @return the size of this collection
     */
    public int getProcessCount(
    ) {
        return this._processList.size();
    }

    /**
     * Method hasPowerDownLevel.
     * 
     * @return true if at least one PowerDownLevel has been added
     */
    public boolean hasPowerDownLevel(
    ) {
        return this._has_powerDownLevel;
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
     * Method iterateProcess.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.building.Process> iterateProcess(
    ) {
        return this._processList.iterator();
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
    public void removeAllProcess(
    ) {
        this._processList.clear();
    }

    /**
     * Method removeProcess.
     * 
     * @param vProcess
     * @return true if the object was removed from the collection.
     */
    public boolean removeProcess(
            final org.mars_sim.msp.config.model.building.Process vProcess) {
        boolean removed = _processList.remove(vProcess);
        return removed;
    }

    /**
     * Method removeProcessAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.building.Process removeProcessAt(
            final int index) {
        java.lang.Object obj = this._processList.remove(index);
        return (org.mars_sim.msp.config.model.building.Process) obj;
    }

    /**
     * Sets the value of field 'powerDownLevel'.
     * 
     * @param powerDownLevel the value of field 'powerDownLevel'.
     */
    public void setPowerDownLevel(
            final float powerDownLevel) {
        this._powerDownLevel = powerDownLevel;
        this._has_powerDownLevel = true;
    }

    /**
     * 
     * 
     * @param index
     * @param vProcess
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setProcess(
            final int index,
            final org.mars_sim.msp.config.model.building.Process vProcess)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._processList.size()) {
            throw new IndexOutOfBoundsException("setProcess: Index value '" + index + "' not in range [0.." + (this._processList.size() - 1) + "]");
        }

        this._processList.set(index, vProcess);
    }

    /**
     * 
     * 
     * @param vProcessArray
     */
    public void setProcess(
            final org.mars_sim.msp.config.model.building.Process[] vProcessArray) {
        //-- copy array
        _processList.clear();

        for (int i = 0; i < vProcessArray.length; i++) {
                this._processList.add(vProcessArray[i]);
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
     * org.mars_sim.msp.config.model.building.ResourceProcessing
     */
    public static org.mars_sim.msp.config.model.building.ResourceProcessing unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.building.ResourceProcessing) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.building.ResourceProcessing.class, reader);
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
