/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.manufacturing;

/**
 * Class ManufacturingProcesses.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class ManufacturingProcesses implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _processList.
     */
    private java.util.List<org.mars_sim.msp.config.model.manufacturing.Process> _processList;

    /**
     * Field _salvageList.
     */
    private java.util.List<org.mars_sim.msp.config.model.manufacturing.Salvage> _salvageList;


      //----------------/
     //- Constructors -/
    //----------------/

    public ManufacturingProcesses() {
        super();
        this._processList = new java.util.ArrayList<org.mars_sim.msp.config.model.manufacturing.Process>();
        this._salvageList = new java.util.ArrayList<org.mars_sim.msp.config.model.manufacturing.Salvage>();
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
            final org.mars_sim.msp.config.model.manufacturing.Process vProcess)
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
            final org.mars_sim.msp.config.model.manufacturing.Process vProcess)
    throws java.lang.IndexOutOfBoundsException {
        this._processList.add(index, vProcess);
    }

    /**
     * 
     * 
     * @param vSalvage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addSalvage(
            final org.mars_sim.msp.config.model.manufacturing.Salvage vSalvage)
    throws java.lang.IndexOutOfBoundsException {
        this._salvageList.add(vSalvage);
    }

    /**
     * 
     * 
     * @param index
     * @param vSalvage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addSalvage(
            final int index,
            final org.mars_sim.msp.config.model.manufacturing.Salvage vSalvage)
    throws java.lang.IndexOutOfBoundsException {
        this._salvageList.add(index, vSalvage);
    }

    /**
     * Method enumerateProcess.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.manufacturing.Process> enumerateProcess(
    ) {
        return java.util.Collections.enumeration(this._processList);
    }

    /**
     * Method enumerateSalvage.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.manufacturing.Salvage> enumerateSalvage(
    ) {
        return java.util.Collections.enumeration(this._salvageList);
    }

    /**
     * Method getProcess.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.manufacturing.Process at the
     * given index
     */
    public org.mars_sim.msp.config.model.manufacturing.Process getProcess(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._processList.size()) {
            throw new IndexOutOfBoundsException("getProcess: Index value '" + index + "' not in range [0.." + (this._processList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.manufacturing.Process) _processList.get(index);
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
    public org.mars_sim.msp.config.model.manufacturing.Process[] getProcess(
    ) {
        org.mars_sim.msp.config.model.manufacturing.Process[] array = new org.mars_sim.msp.config.model.manufacturing.Process[0];
        return (org.mars_sim.msp.config.model.manufacturing.Process[]) this._processList.toArray(array);
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
     * Method getSalvage.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.manufacturing.Salvage at the
     * given index
     */
    public org.mars_sim.msp.config.model.manufacturing.Salvage getSalvage(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._salvageList.size()) {
            throw new IndexOutOfBoundsException("getSalvage: Index value '" + index + "' not in range [0.." + (this._salvageList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.manufacturing.Salvage) _salvageList.get(index);
    }

    /**
     * Method getSalvage.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.manufacturing.Salvage[] getSalvage(
    ) {
        org.mars_sim.msp.config.model.manufacturing.Salvage[] array = new org.mars_sim.msp.config.model.manufacturing.Salvage[0];
        return (org.mars_sim.msp.config.model.manufacturing.Salvage[]) this._salvageList.toArray(array);
    }

    /**
     * Method getSalvageCount.
     * 
     * @return the size of this collection
     */
    public int getSalvageCount(
    ) {
        return this._salvageList.size();
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
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.manufacturing.Process> iterateProcess(
    ) {
        return this._processList.iterator();
    }

    /**
     * Method iterateSalvage.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.manufacturing.Salvage> iterateSalvage(
    ) {
        return this._salvageList.iterator();
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
     */
    public void removeAllSalvage(
    ) {
        this._salvageList.clear();
    }

    /**
     * Method removeProcess.
     * 
     * @param vProcess
     * @return true if the object was removed from the collection.
     */
    public boolean removeProcess(
            final org.mars_sim.msp.config.model.manufacturing.Process vProcess) {
        boolean removed = _processList.remove(vProcess);
        return removed;
    }

    /**
     * Method removeProcessAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.manufacturing.Process removeProcessAt(
            final int index) {
        java.lang.Object obj = this._processList.remove(index);
        return (org.mars_sim.msp.config.model.manufacturing.Process) obj;
    }

    /**
     * Method removeSalvage.
     * 
     * @param vSalvage
     * @return true if the object was removed from the collection.
     */
    public boolean removeSalvage(
            final org.mars_sim.msp.config.model.manufacturing.Salvage vSalvage) {
        boolean removed = _salvageList.remove(vSalvage);
        return removed;
    }

    /**
     * Method removeSalvageAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.manufacturing.Salvage removeSalvageAt(
            final int index) {
        java.lang.Object obj = this._salvageList.remove(index);
        return (org.mars_sim.msp.config.model.manufacturing.Salvage) obj;
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
            final org.mars_sim.msp.config.model.manufacturing.Process vProcess)
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
            final org.mars_sim.msp.config.model.manufacturing.Process[] vProcessArray) {
        //-- copy array
        _processList.clear();

        for (int i = 0; i < vProcessArray.length; i++) {
                this._processList.add(vProcessArray[i]);
        }
    }

    /**
     * 
     * 
     * @param index
     * @param vSalvage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setSalvage(
            final int index,
            final org.mars_sim.msp.config.model.manufacturing.Salvage vSalvage)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._salvageList.size()) {
            throw new IndexOutOfBoundsException("setSalvage: Index value '" + index + "' not in range [0.." + (this._salvageList.size() - 1) + "]");
        }

        this._salvageList.set(index, vSalvage);
    }

    /**
     * 
     * 
     * @param vSalvageArray
     */
    public void setSalvage(
            final org.mars_sim.msp.config.model.manufacturing.Salvage[] vSalvageArray) {
        //-- copy array
        _salvageList.clear();

        for (int i = 0; i < vSalvageArray.length; i++) {
                this._salvageList.add(vSalvageArray[i]);
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
     * org.mars_sim.msp.config.model.manufacturing.ManufacturingProcesses
     */
    public static org.mars_sim.msp.config.model.manufacturing.ManufacturingProcesses unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.manufacturing.ManufacturingProcesses) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.manufacturing.ManufacturingProcesses.class, reader);
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
