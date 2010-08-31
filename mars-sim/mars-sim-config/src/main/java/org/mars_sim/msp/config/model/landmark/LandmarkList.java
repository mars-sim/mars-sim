/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.landmark;

/**
 * Class LandmarkList.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class LandmarkList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _landmarkList.
     */
    private java.util.List<org.mars_sim.msp.config.model.landmark.Landmark> _landmarkList;


      //----------------/
     //- Constructors -/
    //----------------/

    public LandmarkList() {
        super();
        this._landmarkList = new java.util.ArrayList<org.mars_sim.msp.config.model.landmark.Landmark>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vLandmark
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addLandmark(
            final org.mars_sim.msp.config.model.landmark.Landmark vLandmark)
    throws java.lang.IndexOutOfBoundsException {
        this._landmarkList.add(vLandmark);
    }

    /**
     * 
     * 
     * @param index
     * @param vLandmark
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addLandmark(
            final int index,
            final org.mars_sim.msp.config.model.landmark.Landmark vLandmark)
    throws java.lang.IndexOutOfBoundsException {
        this._landmarkList.add(index, vLandmark);
    }

    /**
     * Method enumerateLandmark.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.landmark.Landmark> enumerateLandmark(
    ) {
        return java.util.Collections.enumeration(this._landmarkList);
    }

    /**
     * Method getLandmark.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.landmark.Landmark at the given
     * index
     */
    public org.mars_sim.msp.config.model.landmark.Landmark getLandmark(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._landmarkList.size()) {
            throw new IndexOutOfBoundsException("getLandmark: Index value '" + index + "' not in range [0.." + (this._landmarkList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.landmark.Landmark) _landmarkList.get(index);
    }

    /**
     * Method getLandmark.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.landmark.Landmark[] getLandmark(
    ) {
        org.mars_sim.msp.config.model.landmark.Landmark[] array = new org.mars_sim.msp.config.model.landmark.Landmark[0];
        return (org.mars_sim.msp.config.model.landmark.Landmark[]) this._landmarkList.toArray(array);
    }

    /**
     * Method getLandmarkCount.
     * 
     * @return the size of this collection
     */
    public int getLandmarkCount(
    ) {
        return this._landmarkList.size();
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
     * Method iterateLandmark.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.landmark.Landmark> iterateLandmark(
    ) {
        return this._landmarkList.iterator();
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
    public void removeAllLandmark(
    ) {
        this._landmarkList.clear();
    }

    /**
     * Method removeLandmark.
     * 
     * @param vLandmark
     * @return true if the object was removed from the collection.
     */
    public boolean removeLandmark(
            final org.mars_sim.msp.config.model.landmark.Landmark vLandmark) {
        boolean removed = _landmarkList.remove(vLandmark);
        return removed;
    }

    /**
     * Method removeLandmarkAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.landmark.Landmark removeLandmarkAt(
            final int index) {
        java.lang.Object obj = this._landmarkList.remove(index);
        return (org.mars_sim.msp.config.model.landmark.Landmark) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vLandmark
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setLandmark(
            final int index,
            final org.mars_sim.msp.config.model.landmark.Landmark vLandmark)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._landmarkList.size()) {
            throw new IndexOutOfBoundsException("setLandmark: Index value '" + index + "' not in range [0.." + (this._landmarkList.size() - 1) + "]");
        }

        this._landmarkList.set(index, vLandmark);
    }

    /**
     * 
     * 
     * @param vLandmarkArray
     */
    public void setLandmark(
            final org.mars_sim.msp.config.model.landmark.Landmark[] vLandmarkArray) {
        //-- copy array
        _landmarkList.clear();

        for (int i = 0; i < vLandmarkArray.length; i++) {
                this._landmarkList.add(vLandmarkArray[i]);
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
     * org.mars_sim.msp.config.model.landmark.LandmarkList
     */
    public static org.mars_sim.msp.config.model.landmark.LandmarkList unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.landmark.LandmarkList) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.landmark.LandmarkList.class, reader);
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
