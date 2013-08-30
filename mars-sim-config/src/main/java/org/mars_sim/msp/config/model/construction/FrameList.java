/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.construction;

/**
 * Class FrameList.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class FrameList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _frameList.
     */
    private java.util.List<org.mars_sim.msp.config.model.construction.Frame> _frameList;


      //----------------/
     //- Constructors -/
    //----------------/

    public FrameList() {
        super();
        this._frameList = new java.util.ArrayList<org.mars_sim.msp.config.model.construction.Frame>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vFrame
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addFrame(
            final org.mars_sim.msp.config.model.construction.Frame vFrame)
    throws java.lang.IndexOutOfBoundsException {
        this._frameList.add(vFrame);
    }

    /**
     * 
     * 
     * @param index
     * @param vFrame
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addFrame(
            final int index,
            final org.mars_sim.msp.config.model.construction.Frame vFrame)
    throws java.lang.IndexOutOfBoundsException {
        this._frameList.add(index, vFrame);
    }

    /**
     * Method enumerateFrame.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.construction.Frame> enumerateFrame(
    ) {
        return java.util.Collections.enumeration(this._frameList);
    }

    /**
     * Method getFrame.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.construction.Frame at the
     * given index
     */
    public org.mars_sim.msp.config.model.construction.Frame getFrame(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._frameList.size()) {
            throw new IndexOutOfBoundsException("getFrame: Index value '" + index + "' not in range [0.." + (this._frameList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.construction.Frame) _frameList.get(index);
    }

    /**
     * Method getFrame.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.construction.Frame[] getFrame(
    ) {
        org.mars_sim.msp.config.model.construction.Frame[] array = new org.mars_sim.msp.config.model.construction.Frame[0];
        return (org.mars_sim.msp.config.model.construction.Frame[]) this._frameList.toArray(array);
    }

    /**
     * Method getFrameCount.
     * 
     * @return the size of this collection
     */
    public int getFrameCount(
    ) {
        return this._frameList.size();
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
     * Method iterateFrame.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.construction.Frame> iterateFrame(
    ) {
        return this._frameList.iterator();
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
    public void removeAllFrame(
    ) {
        this._frameList.clear();
    }

    /**
     * Method removeFrame.
     * 
     * @param vFrame
     * @return true if the object was removed from the collection.
     */
    public boolean removeFrame(
            final org.mars_sim.msp.config.model.construction.Frame vFrame) {
        boolean removed = _frameList.remove(vFrame);
        return removed;
    }

    /**
     * Method removeFrameAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.construction.Frame removeFrameAt(
            final int index) {
        java.lang.Object obj = this._frameList.remove(index);
        return (org.mars_sim.msp.config.model.construction.Frame) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vFrame
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setFrame(
            final int index,
            final org.mars_sim.msp.config.model.construction.Frame vFrame)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._frameList.size()) {
            throw new IndexOutOfBoundsException("setFrame: Index value '" + index + "' not in range [0.." + (this._frameList.size() - 1) + "]");
        }

        this._frameList.set(index, vFrame);
    }

    /**
     * 
     * 
     * @param vFrameArray
     */
    public void setFrame(
            final org.mars_sim.msp.config.model.construction.Frame[] vFrameArray) {
        //-- copy array
        _frameList.clear();

        for (int i = 0; i < vFrameArray.length; i++) {
                this._frameList.add(vFrameArray[i]);
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
     * org.mars_sim.msp.config.model.construction.FrameList
     */
    public static org.mars_sim.msp.config.model.construction.FrameList unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.construction.FrameList) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.construction.FrameList.class, reader);
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
