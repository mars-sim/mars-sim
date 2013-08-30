/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.construction;

/**
 * Class Vehicle.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Vehicle implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _type.
     */
    private java.lang.String _type;

    /**
     * Field _attachmentPartList.
     */
    private java.util.List<org.mars_sim.msp.config.model.construction.AttachmentPart> _attachmentPartList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Vehicle() {
        super();
        this._attachmentPartList = new java.util.ArrayList<org.mars_sim.msp.config.model.construction.AttachmentPart>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vAttachmentPart
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addAttachmentPart(
            final org.mars_sim.msp.config.model.construction.AttachmentPart vAttachmentPart)
    throws java.lang.IndexOutOfBoundsException {
        this._attachmentPartList.add(vAttachmentPart);
    }

    /**
     * 
     * 
     * @param index
     * @param vAttachmentPart
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addAttachmentPart(
            final int index,
            final org.mars_sim.msp.config.model.construction.AttachmentPart vAttachmentPart)
    throws java.lang.IndexOutOfBoundsException {
        this._attachmentPartList.add(index, vAttachmentPart);
    }

    /**
     * Method enumerateAttachmentPart.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.construction.AttachmentPart> enumerateAttachmentPart(
    ) {
        return java.util.Collections.enumeration(this._attachmentPartList);
    }

    /**
     * Method getAttachmentPart.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.construction.AttachmentPart at
     * the given index
     */
    public org.mars_sim.msp.config.model.construction.AttachmentPart getAttachmentPart(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._attachmentPartList.size()) {
            throw new IndexOutOfBoundsException("getAttachmentPart: Index value '" + index + "' not in range [0.." + (this._attachmentPartList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.construction.AttachmentPart) _attachmentPartList.get(index);
    }

    /**
     * Method getAttachmentPart.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.construction.AttachmentPart[] getAttachmentPart(
    ) {
        org.mars_sim.msp.config.model.construction.AttachmentPart[] array = new org.mars_sim.msp.config.model.construction.AttachmentPart[0];
        return (org.mars_sim.msp.config.model.construction.AttachmentPart[]) this._attachmentPartList.toArray(array);
    }

    /**
     * Method getAttachmentPartCount.
     * 
     * @return the size of this collection
     */
    public int getAttachmentPartCount(
    ) {
        return this._attachmentPartList.size();
    }

    /**
     * Returns the value of field 'type'.
     * 
     * @return the value of field 'Type'.
     */
    public java.lang.String getType(
    ) {
        return this._type;
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
     * Method iterateAttachmentPart.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.construction.AttachmentPart> iterateAttachmentPart(
    ) {
        return this._attachmentPartList.iterator();
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
    public void removeAllAttachmentPart(
    ) {
        this._attachmentPartList.clear();
    }

    /**
     * Method removeAttachmentPart.
     * 
     * @param vAttachmentPart
     * @return true if the object was removed from the collection.
     */
    public boolean removeAttachmentPart(
            final org.mars_sim.msp.config.model.construction.AttachmentPart vAttachmentPart) {
        boolean removed = _attachmentPartList.remove(vAttachmentPart);
        return removed;
    }

    /**
     * Method removeAttachmentPartAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.construction.AttachmentPart removeAttachmentPartAt(
            final int index) {
        java.lang.Object obj = this._attachmentPartList.remove(index);
        return (org.mars_sim.msp.config.model.construction.AttachmentPart) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vAttachmentPart
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setAttachmentPart(
            final int index,
            final org.mars_sim.msp.config.model.construction.AttachmentPart vAttachmentPart)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._attachmentPartList.size()) {
            throw new IndexOutOfBoundsException("setAttachmentPart: Index value '" + index + "' not in range [0.." + (this._attachmentPartList.size() - 1) + "]");
        }

        this._attachmentPartList.set(index, vAttachmentPart);
    }

    /**
     * 
     * 
     * @param vAttachmentPartArray
     */
    public void setAttachmentPart(
            final org.mars_sim.msp.config.model.construction.AttachmentPart[] vAttachmentPartArray) {
        //-- copy array
        _attachmentPartList.clear();

        for (int i = 0; i < vAttachmentPartArray.length; i++) {
                this._attachmentPartList.add(vAttachmentPartArray[i]);
        }
    }

    /**
     * Sets the value of field 'type'.
     * 
     * @param type the value of field 'type'.
     */
    public void setType(
            final java.lang.String type) {
        this._type = type;
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
     * org.mars_sim.msp.config.model.construction.Vehicle
     */
    public static org.mars_sim.msp.config.model.construction.Vehicle unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.construction.Vehicle) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.construction.Vehicle.class, reader);
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
