/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.settlement;

/**
 * Class Resupply.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Resupply implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _resupplyMissionList.
     */
    private java.util.List<org.mars_sim.msp.config.model.settlement.ResupplyMission> _resupplyMissionList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Resupply() {
        super();
        this._resupplyMissionList = new java.util.ArrayList<org.mars_sim.msp.config.model.settlement.ResupplyMission>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vResupplyMission
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addResupplyMission(
            final org.mars_sim.msp.config.model.settlement.ResupplyMission vResupplyMission)
    throws java.lang.IndexOutOfBoundsException {
        this._resupplyMissionList.add(vResupplyMission);
    }

    /**
     * 
     * 
     * @param index
     * @param vResupplyMission
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addResupplyMission(
            final int index,
            final org.mars_sim.msp.config.model.settlement.ResupplyMission vResupplyMission)
    throws java.lang.IndexOutOfBoundsException {
        this._resupplyMissionList.add(index, vResupplyMission);
    }

    /**
     * Method enumerateResupplyMission.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.settlement.ResupplyMission> enumerateResupplyMission(
    ) {
        return java.util.Collections.enumeration(this._resupplyMissionList);
    }

    /**
     * Method getResupplyMission.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.settlement.ResupplyMission at
     * the given index
     */
    public org.mars_sim.msp.config.model.settlement.ResupplyMission getResupplyMission(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._resupplyMissionList.size()) {
            throw new IndexOutOfBoundsException("getResupplyMission: Index value '" + index + "' not in range [0.." + (this._resupplyMissionList.size() - 1) + "]");
        }

        return _resupplyMissionList.get(index);
    }

    /**
     * Method getResupplyMission.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.settlement.ResupplyMission[] getResupplyMission(
    ) {
        org.mars_sim.msp.config.model.settlement.ResupplyMission[] array = new org.mars_sim.msp.config.model.settlement.ResupplyMission[0];
        return this._resupplyMissionList.toArray(array);
    }

    /**
     * Method getResupplyMissionCount.
     * 
     * @return the size of this collection
     */
    public int getResupplyMissionCount(
    ) {
        return this._resupplyMissionList.size();
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
     * Method iterateResupplyMission.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.settlement.ResupplyMission> iterateResupplyMission(
    ) {
        return this._resupplyMissionList.iterator();
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
    public void removeAllResupplyMission(
    ) {
        this._resupplyMissionList.clear();
    }

    /**
     * Method removeResupplyMission.
     * 
     * @param vResupplyMission
     * @return true if the object was removed from the collection.
     */
    public boolean removeResupplyMission(
            final org.mars_sim.msp.config.model.settlement.ResupplyMission vResupplyMission) {
        boolean removed = _resupplyMissionList.remove(vResupplyMission);
        return removed;
    }

    /**
     * Method removeResupplyMissionAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.settlement.ResupplyMission removeResupplyMissionAt(
            final int index) {
        java.lang.Object obj = this._resupplyMissionList.remove(index);
        return (org.mars_sim.msp.config.model.settlement.ResupplyMission) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vResupplyMission
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setResupplyMission(
            final int index,
            final org.mars_sim.msp.config.model.settlement.ResupplyMission vResupplyMission)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._resupplyMissionList.size()) {
            throw new IndexOutOfBoundsException("setResupplyMission: Index value '" + index + "' not in range [0.." + (this._resupplyMissionList.size() - 1) + "]");
        }

        this._resupplyMissionList.set(index, vResupplyMission);
    }

    /**
     * 
     * 
     * @param vResupplyMissionArray
     */
    public void setResupplyMission(
            final org.mars_sim.msp.config.model.settlement.ResupplyMission[] vResupplyMissionArray) {
        //-- copy array
        _resupplyMissionList.clear();

        for (int i = 0; i < vResupplyMissionArray.length; i++) {
                this._resupplyMissionList.add(vResupplyMissionArray[i]);
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
     * org.mars_sim.msp.config.model.settlement.Resupply
     */
    public static org.mars_sim.msp.config.model.settlement.Resupply unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.settlement.Resupply) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.settlement.Resupply.class, reader);
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
