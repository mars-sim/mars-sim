/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.manufacturing;

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
     * Field _tech.
     */
    private long _tech;

    /**
     * keeps track of state for field: _tech
     */
    private boolean _has_tech;

    /**
     * Field _skill.
     */
    private long _skill;

    /**
     * keeps track of state for field: _skill
     */
    private boolean _has_skill;

    /**
     * Field _workTime.
     */
    private long _workTime;

    /**
     * keeps track of state for field: _workTime
     */
    private boolean _has_workTime;

    /**
     * Field _processTime.
     */
    private long _processTime;

    /**
     * keeps track of state for field: _processTime
     */
    private boolean _has_processTime;

    /**
     * Field _powerRequired.
     */
    private double _powerRequired;

    /**
     * keeps track of state for field: _powerRequired
     */
    private boolean _has_powerRequired;

    /**
     * Field _inputs.
     */
    private org.mars_sim.msp.config.model.manufacturing.Inputs _inputs;

    /**
     * Field _outputs.
     */
    private org.mars_sim.msp.config.model.manufacturing.Outputs _outputs;


      //----------------/
     //- Constructors -/
    //----------------/

    public Process() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deletePowerRequired(
    ) {
        this._has_powerRequired= false;
    }

    /**
     */
    public void deleteProcessTime(
    ) {
        this._has_processTime= false;
    }

    /**
     */
    public void deleteSkill(
    ) {
        this._has_skill= false;
    }

    /**
     */
    public void deleteTech(
    ) {
        this._has_tech= false;
    }

    /**
     */
    public void deleteWorkTime(
    ) {
        this._has_workTime= false;
    }

    /**
     * Returns the value of field 'inputs'.
     * 
     * @return the value of field 'Inputs'.
     */
    public org.mars_sim.msp.config.model.manufacturing.Inputs getInputs(
    ) {
        return this._inputs;
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
     * Returns the value of field 'outputs'.
     * 
     * @return the value of field 'Outputs'.
     */
    public org.mars_sim.msp.config.model.manufacturing.Outputs getOutputs(
    ) {
        return this._outputs;
    }

    /**
     * Returns the value of field 'powerRequired'.
     * 
     * @return the value of field 'PowerRequired'.
     */
    public double getPowerRequired(
    ) {
        return this._powerRequired;
    }

    /**
     * Returns the value of field 'processTime'.
     * 
     * @return the value of field 'ProcessTime'.
     */
    public long getProcessTime(
    ) {
        return this._processTime;
    }

    /**
     * Returns the value of field 'skill'.
     * 
     * @return the value of field 'Skill'.
     */
    public long getSkill(
    ) {
        return this._skill;
    }

    /**
     * Returns the value of field 'tech'.
     * 
     * @return the value of field 'Tech'.
     */
    public long getTech(
    ) {
        return this._tech;
    }

    /**
     * Returns the value of field 'workTime'.
     * 
     * @return the value of field 'WorkTime'.
     */
    public long getWorkTime(
    ) {
        return this._workTime;
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
     * Method hasProcessTime.
     * 
     * @return true if at least one ProcessTime has been added
     */
    public boolean hasProcessTime(
    ) {
        return this._has_processTime;
    }

    /**
     * Method hasSkill.
     * 
     * @return true if at least one Skill has been added
     */
    public boolean hasSkill(
    ) {
        return this._has_skill;
    }

    /**
     * Method hasTech.
     * 
     * @return true if at least one Tech has been added
     */
    public boolean hasTech(
    ) {
        return this._has_tech;
    }

    /**
     * Method hasWorkTime.
     * 
     * @return true if at least one WorkTime has been added
     */
    public boolean hasWorkTime(
    ) {
        return this._has_workTime;
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
     * Sets the value of field 'inputs'.
     * 
     * @param inputs the value of field 'inputs'.
     */
    public void setInputs(
            final org.mars_sim.msp.config.model.manufacturing.Inputs inputs) {
        this._inputs = inputs;
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
     * Sets the value of field 'outputs'.
     * 
     * @param outputs the value of field 'outputs'.
     */
    public void setOutputs(
            final org.mars_sim.msp.config.model.manufacturing.Outputs outputs) {
        this._outputs = outputs;
    }

    /**
     * Sets the value of field 'powerRequired'.
     * 
     * @param powerRequired the value of field 'powerRequired'.
     */
    public void setPowerRequired(
            final double powerRequired) {
        this._powerRequired = powerRequired;
        this._has_powerRequired = true;
    }

    /**
     * Sets the value of field 'processTime'.
     * 
     * @param processTime the value of field 'processTime'.
     */
    public void setProcessTime(
            final long processTime) {
        this._processTime = processTime;
        this._has_processTime = true;
    }

    /**
     * Sets the value of field 'skill'.
     * 
     * @param skill the value of field 'skill'.
     */
    public void setSkill(
            final long skill) {
        this._skill = skill;
        this._has_skill = true;
    }

    /**
     * Sets the value of field 'tech'.
     * 
     * @param tech the value of field 'tech'.
     */
    public void setTech(
            final long tech) {
        this._tech = tech;
        this._has_tech = true;
    }

    /**
     * Sets the value of field 'workTime'.
     * 
     * @param workTime the value of field 'workTime'.
     */
    public void setWorkTime(
            final long workTime) {
        this._workTime = workTime;
        this._has_workTime = true;
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
     * org.mars_sim.msp.config.model.manufacturing.Process
     */
    public static org.mars_sim.msp.config.model.manufacturing.Process unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.manufacturing.Process) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.manufacturing.Process.class, reader);
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
