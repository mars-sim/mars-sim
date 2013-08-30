/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.crop;

/**
 * Class CropList.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class CropList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _cropList.
     */
    private java.util.List<org.mars_sim.msp.config.model.crop.Crop> _cropList;


      //----------------/
     //- Constructors -/
    //----------------/

    public CropList() {
        super();
        this._cropList = new java.util.ArrayList<org.mars_sim.msp.config.model.crop.Crop>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vCrop
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addCrop(
            final org.mars_sim.msp.config.model.crop.Crop vCrop)
    throws java.lang.IndexOutOfBoundsException {
        this._cropList.add(vCrop);
    }

    /**
     * 
     * 
     * @param index
     * @param vCrop
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addCrop(
            final int index,
            final org.mars_sim.msp.config.model.crop.Crop vCrop)
    throws java.lang.IndexOutOfBoundsException {
        this._cropList.add(index, vCrop);
    }

    /**
     * Method enumerateCrop.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.crop.Crop> enumerateCrop(
    ) {
        return java.util.Collections.enumeration(this._cropList);
    }

    /**
     * Method getCrop.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.crop.Crop at the given index
     */
    public org.mars_sim.msp.config.model.crop.Crop getCrop(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._cropList.size()) {
            throw new IndexOutOfBoundsException("getCrop: Index value '" + index + "' not in range [0.." + (this._cropList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.crop.Crop) _cropList.get(index);
    }

    /**
     * Method getCrop.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.crop.Crop[] getCrop(
    ) {
        org.mars_sim.msp.config.model.crop.Crop[] array = new org.mars_sim.msp.config.model.crop.Crop[0];
        return (org.mars_sim.msp.config.model.crop.Crop[]) this._cropList.toArray(array);
    }

    /**
     * Method getCropCount.
     * 
     * @return the size of this collection
     */
    public int getCropCount(
    ) {
        return this._cropList.size();
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
     * Method iterateCrop.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.crop.Crop> iterateCrop(
    ) {
        return this._cropList.iterator();
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
    public void removeAllCrop(
    ) {
        this._cropList.clear();
    }

    /**
     * Method removeCrop.
     * 
     * @param vCrop
     * @return true if the object was removed from the collection.
     */
    public boolean removeCrop(
            final org.mars_sim.msp.config.model.crop.Crop vCrop) {
        boolean removed = _cropList.remove(vCrop);
        return removed;
    }

    /**
     * Method removeCropAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.crop.Crop removeCropAt(
            final int index) {
        java.lang.Object obj = this._cropList.remove(index);
        return (org.mars_sim.msp.config.model.crop.Crop) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vCrop
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setCrop(
            final int index,
            final org.mars_sim.msp.config.model.crop.Crop vCrop)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._cropList.size()) {
            throw new IndexOutOfBoundsException("setCrop: Index value '" + index + "' not in range [0.." + (this._cropList.size() - 1) + "]");
        }

        this._cropList.set(index, vCrop);
    }

    /**
     * 
     * 
     * @param vCropArray
     */
    public void setCrop(
            final org.mars_sim.msp.config.model.crop.Crop[] vCropArray) {
        //-- copy array
        _cropList.clear();

        for (int i = 0; i < vCropArray.length; i++) {
                this._cropList.add(vCropArray[i]);
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
     * org.mars_sim.msp.config.model.crop.CropList
     */
    public static org.mars_sim.msp.config.model.crop.CropList unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.crop.CropList) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.crop.CropList.class, reader);
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
