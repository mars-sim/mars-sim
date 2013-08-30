/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.part;

/**
 * Class MaintenanceEntityList.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class MaintenanceEntityList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _name.
     */
    private java.lang.String _name;

    /**
     * Field _mass.
     */
    private double _mass;

    /**
     * keeps track of state for field: _mass
     */
    private boolean _has_mass;

    /**
     * Field _entityList.
     */
    private java.util.List<org.mars_sim.msp.config.model.part.Entity> _entityList;


      //----------------/
     //- Constructors -/
    //----------------/

    public MaintenanceEntityList() {
        super();
        this._entityList = new java.util.ArrayList<org.mars_sim.msp.config.model.part.Entity>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vEntity
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addEntity(
            final org.mars_sim.msp.config.model.part.Entity vEntity)
    throws java.lang.IndexOutOfBoundsException {
        this._entityList.add(vEntity);
    }

    /**
     * 
     * 
     * @param index
     * @param vEntity
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addEntity(
            final int index,
            final org.mars_sim.msp.config.model.part.Entity vEntity)
    throws java.lang.IndexOutOfBoundsException {
        this._entityList.add(index, vEntity);
    }

    /**
     */
    public void deleteMass(
    ) {
        this._has_mass= false;
    }

    /**
     * Method enumerateEntity.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.part.Entity> enumerateEntity(
    ) {
        return java.util.Collections.enumeration(this._entityList);
    }

    /**
     * Method getEntity.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.part.Entity at the given index
     */
    public org.mars_sim.msp.config.model.part.Entity getEntity(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._entityList.size()) {
            throw new IndexOutOfBoundsException("getEntity: Index value '" + index + "' not in range [0.." + (this._entityList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.part.Entity) _entityList.get(index);
    }

    /**
     * Method getEntity.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.part.Entity[] getEntity(
    ) {
        org.mars_sim.msp.config.model.part.Entity[] array = new org.mars_sim.msp.config.model.part.Entity[0];
        return (org.mars_sim.msp.config.model.part.Entity[]) this._entityList.toArray(array);
    }

    /**
     * Method getEntityCount.
     * 
     * @return the size of this collection
     */
    public int getEntityCount(
    ) {
        return this._entityList.size();
    }

    /**
     * Returns the value of field 'mass'.
     * 
     * @return the value of field 'Mass'.
     */
    public double getMass(
    ) {
        return this._mass;
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
     * Method hasMass.
     * 
     * @return true if at least one Mass has been added
     */
    public boolean hasMass(
    ) {
        return this._has_mass;
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
     * Method iterateEntity.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.part.Entity> iterateEntity(
    ) {
        return this._entityList.iterator();
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
    public void removeAllEntity(
    ) {
        this._entityList.clear();
    }

    /**
     * Method removeEntity.
     * 
     * @param vEntity
     * @return true if the object was removed from the collection.
     */
    public boolean removeEntity(
            final org.mars_sim.msp.config.model.part.Entity vEntity) {
        boolean removed = _entityList.remove(vEntity);
        return removed;
    }

    /**
     * Method removeEntityAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.part.Entity removeEntityAt(
            final int index) {
        java.lang.Object obj = this._entityList.remove(index);
        return (org.mars_sim.msp.config.model.part.Entity) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vEntity
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setEntity(
            final int index,
            final org.mars_sim.msp.config.model.part.Entity vEntity)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._entityList.size()) {
            throw new IndexOutOfBoundsException("setEntity: Index value '" + index + "' not in range [0.." + (this._entityList.size() - 1) + "]");
        }

        this._entityList.set(index, vEntity);
    }

    /**
     * 
     * 
     * @param vEntityArray
     */
    public void setEntity(
            final org.mars_sim.msp.config.model.part.Entity[] vEntityArray) {
        //-- copy array
        _entityList.clear();

        for (int i = 0; i < vEntityArray.length; i++) {
                this._entityList.add(vEntityArray[i]);
        }
    }

    /**
     * Sets the value of field 'mass'.
     * 
     * @param mass the value of field 'mass'.
     */
    public void setMass(
            final double mass) {
        this._mass = mass;
        this._has_mass = true;
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
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * org.mars_sim.msp.config.model.part.MaintenanceEntityList
     */
    public static org.mars_sim.msp.config.model.part.MaintenanceEntityList unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.part.MaintenanceEntityList) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.part.MaintenanceEntityList.class, reader);
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
