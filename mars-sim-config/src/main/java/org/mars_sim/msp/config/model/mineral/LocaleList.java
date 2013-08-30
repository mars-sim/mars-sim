/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.mineral;

/**
 * Class LocaleList.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class LocaleList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _localeList.
     */
    private java.util.List<org.mars_sim.msp.config.model.mineral.Locale> _localeList;


      //----------------/
     //- Constructors -/
    //----------------/

    public LocaleList() {
        super();
        this._localeList = new java.util.ArrayList<org.mars_sim.msp.config.model.mineral.Locale>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vLocale
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addLocale(
            final org.mars_sim.msp.config.model.mineral.Locale vLocale)
    throws java.lang.IndexOutOfBoundsException {
        this._localeList.add(vLocale);
    }

    /**
     * 
     * 
     * @param index
     * @param vLocale
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addLocale(
            final int index,
            final org.mars_sim.msp.config.model.mineral.Locale vLocale)
    throws java.lang.IndexOutOfBoundsException {
        this._localeList.add(index, vLocale);
    }

    /**
     * Method enumerateLocale.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.mineral.Locale> enumerateLocale(
    ) {
        return java.util.Collections.enumeration(this._localeList);
    }

    /**
     * Method getLocale.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.mineral.Locale at the given
     * index
     */
    public org.mars_sim.msp.config.model.mineral.Locale getLocale(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._localeList.size()) {
            throw new IndexOutOfBoundsException("getLocale: Index value '" + index + "' not in range [0.." + (this._localeList.size() - 1) + "]");
        }

        return (org.mars_sim.msp.config.model.mineral.Locale) _localeList.get(index);
    }

    /**
     * Method getLocale.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.mineral.Locale[] getLocale(
    ) {
        org.mars_sim.msp.config.model.mineral.Locale[] array = new org.mars_sim.msp.config.model.mineral.Locale[0];
        return (org.mars_sim.msp.config.model.mineral.Locale[]) this._localeList.toArray(array);
    }

    /**
     * Method getLocaleCount.
     * 
     * @return the size of this collection
     */
    public int getLocaleCount(
    ) {
        return this._localeList.size();
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
     * Method iterateLocale.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.mineral.Locale> iterateLocale(
    ) {
        return this._localeList.iterator();
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
    public void removeAllLocale(
    ) {
        this._localeList.clear();
    }

    /**
     * Method removeLocale.
     * 
     * @param vLocale
     * @return true if the object was removed from the collection.
     */
    public boolean removeLocale(
            final org.mars_sim.msp.config.model.mineral.Locale vLocale) {
        boolean removed = _localeList.remove(vLocale);
        return removed;
    }

    /**
     * Method removeLocaleAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.mineral.Locale removeLocaleAt(
            final int index) {
        java.lang.Object obj = this._localeList.remove(index);
        return (org.mars_sim.msp.config.model.mineral.Locale) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vLocale
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setLocale(
            final int index,
            final org.mars_sim.msp.config.model.mineral.Locale vLocale)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._localeList.size()) {
            throw new IndexOutOfBoundsException("setLocale: Index value '" + index + "' not in range [0.." + (this._localeList.size() - 1) + "]");
        }

        this._localeList.set(index, vLocale);
    }

    /**
     * 
     * 
     * @param vLocaleArray
     */
    public void setLocale(
            final org.mars_sim.msp.config.model.mineral.Locale[] vLocaleArray) {
        //-- copy array
        _localeList.clear();

        for (int i = 0; i < vLocaleArray.length; i++) {
                this._localeList.add(vLocaleArray[i]);
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
     * org.mars_sim.msp.config.model.mineral.LocaleList
     */
    public static org.mars_sim.msp.config.model.mineral.LocaleList unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.mineral.LocaleList) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.mineral.LocaleList.class, reader);
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
