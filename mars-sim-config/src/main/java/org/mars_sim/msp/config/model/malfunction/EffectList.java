/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.malfunction;

/**
 * Class EffectList.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class EffectList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _effectList.
     */
    private java.util.List<org.mars_sim.msp.config.model.malfunction.Effect> _effectList;


      //----------------/
     //- Constructors -/
    //----------------/

    public EffectList() {
        super();
        this._effectList = new java.util.ArrayList<org.mars_sim.msp.config.model.malfunction.Effect>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vEffect
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addEffect(
            final org.mars_sim.msp.config.model.malfunction.Effect vEffect)
    throws java.lang.IndexOutOfBoundsException {
        this._effectList.add(vEffect);
    }

    /**
     * 
     * 
     * @param index
     * @param vEffect
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addEffect(
            final int index,
            final org.mars_sim.msp.config.model.malfunction.Effect vEffect)
    throws java.lang.IndexOutOfBoundsException {
        this._effectList.add(index, vEffect);
    }

    /**
     * Method enumerateEffect.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.malfunction.Effect> enumerateEffect(
    ) {
        return java.util.Collections.enumeration(this._effectList);
    }

    /**
     * Method getEffect.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.malfunction.Effect at the
     * given index
     */
    public org.mars_sim.msp.config.model.malfunction.Effect getEffect(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._effectList.size()) {
            throw new IndexOutOfBoundsException("getEffect: Index value '" + index + "' not in range [0.." + (this._effectList.size() - 1) + "]");
        }

        return _effectList.get(index);
    }

    /**
     * Method getEffect.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.malfunction.Effect[] getEffect(
    ) {
        org.mars_sim.msp.config.model.malfunction.Effect[] array = new org.mars_sim.msp.config.model.malfunction.Effect[0];
        return this._effectList.toArray(array);
    }

    /**
     * Method getEffectCount.
     * 
     * @return the size of this collection
     */
    public int getEffectCount(
    ) {
        return this._effectList.size();
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
     * Method iterateEffect.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.malfunction.Effect> iterateEffect(
    ) {
        return this._effectList.iterator();
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
    public void removeAllEffect(
    ) {
        this._effectList.clear();
    }

    /**
     * Method removeEffect.
     * 
     * @param vEffect
     * @return true if the object was removed from the collection.
     */
    public boolean removeEffect(
            final org.mars_sim.msp.config.model.malfunction.Effect vEffect) {
        boolean removed = _effectList.remove(vEffect);
        return removed;
    }

    /**
     * Method removeEffectAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.malfunction.Effect removeEffectAt(
            final int index) {
        java.lang.Object obj = this._effectList.remove(index);
        return (org.mars_sim.msp.config.model.malfunction.Effect) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vEffect
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setEffect(
            final int index,
            final org.mars_sim.msp.config.model.malfunction.Effect vEffect)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._effectList.size()) {
            throw new IndexOutOfBoundsException("setEffect: Index value '" + index + "' not in range [0.." + (this._effectList.size() - 1) + "]");
        }

        this._effectList.set(index, vEffect);
    }

    /**
     * 
     * 
     * @param vEffectArray
     */
    public void setEffect(
            final org.mars_sim.msp.config.model.malfunction.Effect[] vEffectArray) {
        //-- copy array
        _effectList.clear();

        for (int i = 0; i < vEffectArray.length; i++) {
                this._effectList.add(vEffectArray[i]);
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
     * org.mars_sim.msp.config.model.malfunction.EffectList
     */
    public static org.mars_sim.msp.config.model.malfunction.EffectList unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.malfunction.EffectList) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.malfunction.EffectList.class, reader);
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
