/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.resupply;

/**
 * Class ResupplyList.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class ResupplyList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _resupplyList.
     */
    private java.util.List<org.mars_sim.msp.config.model.resupply.Resupply> _resupplyList;


      //----------------/
     //- Constructors -/
    //----------------/

    public ResupplyList() {
        super();
        this._resupplyList = new java.util.ArrayList<org.mars_sim.msp.config.model.resupply.Resupply>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vResupply
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addResupply(
            final org.mars_sim.msp.config.model.resupply.Resupply vResupply)
    throws java.lang.IndexOutOfBoundsException {
        this._resupplyList.add(vResupply);
    }

    /**
     * 
     * 
     * @param index
     * @param vResupply
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addResupply(
            final int index,
            final org.mars_sim.msp.config.model.resupply.Resupply vResupply)
    throws java.lang.IndexOutOfBoundsException {
        this._resupplyList.add(index, vResupply);
    }

    /**
     * Method enumerateResupply.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.resupply.Resupply> enumerateResupply(
    ) {
        return java.util.Collections.enumeration(this._resupplyList);
    }

    /**
     * Method getResupply.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.resupply.Resupply at the given
     * index
     */
    public org.mars_sim.msp.config.model.resupply.Resupply getResupply(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._resupplyList.size()) {
            throw new IndexOutOfBoundsException("getResupply: Index value '" + index + "' not in range [0.." + (this._resupplyList.size() - 1) + "]");
        }

        return _resupplyList.get(index);
    }

    /**
     * Method getResupply.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.resupply.Resupply[] getResupply(
    ) {
        org.mars_sim.msp.config.model.resupply.Resupply[] array = new org.mars_sim.msp.config.model.resupply.Resupply[0];
        return this._resupplyList.toArray(array);
    }

    /**
     * Method getResupplyCount.
     * 
     * @return the size of this collection
     */
    public int getResupplyCount(
    ) {
        return this._resupplyList.size();
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
     * Method iterateResupply.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.resupply.Resupply> iterateResupply(
    ) {
        return this._resupplyList.iterator();
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
    public void removeAllResupply(
    ) {
        this._resupplyList.clear();
    }

    /**
     * Method removeResupply.
     * 
     * @param vResupply
     * @return true if the object was removed from the collection.
     */
    public boolean removeResupply(
            final org.mars_sim.msp.config.model.resupply.Resupply vResupply) {
        boolean removed = _resupplyList.remove(vResupply);
        return removed;
    }

    /**
     * Method removeResupplyAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.resupply.Resupply removeResupplyAt(
            final int index) {
        java.lang.Object obj = this._resupplyList.remove(index);
        return (org.mars_sim.msp.config.model.resupply.Resupply) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vResupply
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setResupply(
            final int index,
            final org.mars_sim.msp.config.model.resupply.Resupply vResupply)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._resupplyList.size()) {
            throw new IndexOutOfBoundsException("setResupply: Index value '" + index + "' not in range [0.." + (this._resupplyList.size() - 1) + "]");
        }

        this._resupplyList.set(index, vResupply);
    }

    /**
     * 
     * 
     * @param vResupplyArray
     */
    public void setResupply(
            final org.mars_sim.msp.config.model.resupply.Resupply[] vResupplyArray) {
        //-- copy array
        _resupplyList.clear();

        for (int i = 0; i < vResupplyArray.length; i++) {
                this._resupplyList.add(vResupplyArray[i]);
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
     * org.mars_sim.msp.config.model.resupply.ResupplyList
     */
    public static org.mars_sim.msp.config.model.resupply.ResupplyList unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.resupply.ResupplyList) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.resupply.ResupplyList.class, reader);
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
