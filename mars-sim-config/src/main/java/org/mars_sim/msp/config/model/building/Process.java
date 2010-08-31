/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.building;

/**
 * Class Process.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Process implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _name.
     */
    private java.lang.String _name;

    /**
     * Field _powerRequired.
     */
    private float _powerRequired;

    /**
     * keeps track of state for field: _powerRequired
     */
    private boolean _has_powerRequired;

    /**
     * Field _default.
     */
    private org.mars_sim.msp.config.model.types.ProcessDefaultType _default;

    /**
     * Field _inputList.
     */
    private java.util.List<org.mars_sim.msp.config.model.building.Input> _inputList;

    /**
     * Field _outputList.
     */
    private java.util.List<org.mars_sim.msp.config.model.building.Output> _outputList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Process() {
        super();
        this._inputList = new java.util.ArrayList<org.mars_sim.msp.config.model.building.Input>();
        this._outputList = new java.util.ArrayList<org.mars_sim.msp.config.model.building.Output>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vInput
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addInput(
            final org.mars_sim.msp.config.model.building.Input vInput)
    throws java.lang.IndexOutOfBoundsException {
        this._inputList.add(vInput);
    }

    /**
     * 
     * 
     * @param index
     * @param vInput
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addInput(
            final int index,
            final org.mars_sim.msp.config.model.building.Input vInput)
    throws java.lang.IndexOutOfBoundsException {
        this._inputList.add(index, vInput);
    }

    /**
     * 
     * 
     * @param vOutput
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addOutput(
            final org.mars_sim.msp.config.model.building.Output vOutput)
    throws java.lang.IndexOutOfBoundsException {
        this._outputList.add(vOutput);
    }

    /**
     * 
     * 
     * @param index
     * @param vOutput
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addOutput(
            final int index,
            final org.mars_sim.msp.config.model.building.Output vOutput)
    throws java.lang.IndexOutOfBoundsException {
        this._outputList.add(index, vOutput);
    }

    /**
     */
    public void deletePowerRequired(
    ) {
        this._has_powerRequired= false;
    }

    /**
     * Method enumerateInput.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.building.Input> enumerateInput(
    ) {
        return java.util.Collections.enumeration(this._inputList);
    }

    /**
     * Method enumerateOutput.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.building.Output> enumerateOutput(
    ) {
        return java.util.Collections.enumeration(this._outputList);
    }

    /**
     * Returns the value of field 'default'.
     * 
     * @return the value of field 'Default'.
     */
    public org.mars_sim.msp.config.model.types.ProcessDefaultType getDefault(
    ) {
        return this._default;
    }

    /**
     * Method getInput.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.building.Input at the given
     * index
     */
    public org.mars_sim.msp.config.model.building.Input getInput(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._inputList.size()) {
            throw new IndexOutOfBoundsException("getInput: Index value '" + index + "' not in range [0.." + (this._inputList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.building.Input) _inputList.get(index);
    }

    /**
     * Method getInput.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.building.Input[] getInput(
    ) {
        org.mars_sim.msp.config.model.building.Input[] array = new org.mars_sim.msp.config.model.building.Input[0];
        return (org.mars_sim.msp.config.model.building.Input[]) this._inputList.toArray(array);
    }

    /**
     * Method getInputCount.
     * 
     * @return the size of this collection
     */
    public int getInputCount(
    ) {
        return this._inputList.size();
    }

    /**
     * Returns the value of field 'name'.
     * 
     * @return the value of field 'Name'.
     */
    public java.lang.String getName(
    ) {
        return this._name;
    }

    /**
     * Method getOutput.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.building.Output at the given
     * index
     */
    public org.mars_sim.msp.config.model.building.Output getOutput(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._outputList.size()) {
            throw new IndexOutOfBoundsException("getOutput: Index value '" + index + "' not in range [0.." + (this._outputList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.building.Output) _outputList.get(index);
    }

    /**
     * Method getOutput.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.building.Output[] getOutput(
    ) {
        org.mars_sim.msp.config.model.building.Output[] array = new org.mars_sim.msp.config.model.building.Output[0];
        return (org.mars_sim.msp.config.model.building.Output[]) this._outputList.toArray(array);
    }

    /**
     * Method getOutputCount.
     * 
     * @return the size of this collection
     */
    public int getOutputCount(
    ) {
        return this._outputList.size();
    }

    /**
     * Returns the value of field 'powerRequired'.
     * 
     * @return the value of field 'PowerRequired'.
     */
    public float getPowerRequired(
    ) {
        return this._powerRequired;
    }

    /**
     * Method hasPowerRequired.
     * 
     * @return true if at least one PowerRequired has been added
     */
    public boolean hasPowerRequired(
    ) {
        return this._has_powerRequired;
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
     * Method iterateInput.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.building.Input> iterateInput(
    ) {
        return this._inputList.iterator();
    }

    /**
     * Method iterateOutput.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.building.Output> iterateOutput(
    ) {
        return this._outputList.iterator();
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
    public void removeAllInput(
    ) {
        this._inputList.clear();
    }

    /**
     */
    public void removeAllOutput(
    ) {
        this._outputList.clear();
    }

    /**
     * Method removeInput.
     * 
     * @param vInput
     * @return true if the object was removed from the collection.
     */
    public boolean removeInput(
            final org.mars_sim.msp.config.model.building.Input vInput) {
        boolean removed = _inputList.remove(vInput);
        return removed;
    }

    /**
     * Method removeInputAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.building.Input removeInputAt(
            final int index) {
        java.lang.Object obj = this._inputList.remove(index);
        return (org.mars_sim.msp.config.model.building.Input) obj;
    }

    /**
     * Method removeOutput.
     * 
     * @param vOutput
     * @return true if the object was removed from the collection.
     */
    public boolean removeOutput(
            final org.mars_sim.msp.config.model.building.Output vOutput) {
        boolean removed = _outputList.remove(vOutput);
        return removed;
    }

    /**
     * Method removeOutputAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.building.Output removeOutputAt(
            final int index) {
        java.lang.Object obj = this._outputList.remove(index);
        return (org.mars_sim.msp.config.model.building.Output) obj;
    }

    /**
     * Sets the value of field 'default'.
     * 
     * @param _default
     * @param default the value of field 'default'.
     */
    public void setDefault(
            final org.mars_sim.msp.config.model.types.ProcessDefaultType _default) {
        this._default = _default;
    }

    /**
     * 
     * 
     * @param index
     * @param vInput
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setInput(
            final int index,
            final org.mars_sim.msp.config.model.building.Input vInput)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._inputList.size()) {
            throw new IndexOutOfBoundsException("setInput: Index value '" + index + "' not in range [0.." + (this._inputList.size() - 1) + "]");
        }

        this._inputList.set(index, vInput);
    }

    /**
     * 
     * 
     * @param vInputArray
     */
    public void setInput(
            final org.mars_sim.msp.config.model.building.Input[] vInputArray) {
        //-- copy array
        _inputList.clear();

        for (int i = 0; i < vInputArray.length; i++) {
                this._inputList.add(vInputArray[i]);
        }
    }

    /**
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(
            final java.lang.String name) {
        this._name = name;
    }

    /**
     * 
     * 
     * @param index
     * @param vOutput
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setOutput(
            final int index,
            final org.mars_sim.msp.config.model.building.Output vOutput)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._outputList.size()) {
            throw new IndexOutOfBoundsException("setOutput: Index value '" + index + "' not in range [0.." + (this._outputList.size() - 1) + "]");
        }

        this._outputList.set(index, vOutput);
    }

    /**
     * 
     * 
     * @param vOutputArray
     */
    public void setOutput(
            final org.mars_sim.msp.config.model.building.Output[] vOutputArray) {
        //-- copy array
        _outputList.clear();

        for (int i = 0; i < vOutputArray.length; i++) {
                this._outputList.add(vOutputArray[i]);
        }
    }

    /**
     * Sets the value of field 'powerRequired'.
     * 
     * @param powerRequired the value of field 'powerRequired'.
     */
    public void setPowerRequired(
            final float powerRequired) {
        this._powerRequired = powerRequired;
        this._has_powerRequired = true;
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
     * org.mars_sim.msp.config.model.building.Process
     */
    public static org.mars_sim.msp.config.model.building.Process unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.building.Process) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.building.Process.class, reader);
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
