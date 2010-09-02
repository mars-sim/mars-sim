/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.settlement;

/**
 * Class SettlementTemplateList.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class SettlementTemplateList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _templateList.
     */
    private java.util.List<org.mars_sim.msp.config.model.settlement.Template> _templateList;


      //----------------/
     //- Constructors -/
    //----------------/

    public SettlementTemplateList() {
        super();
        this._templateList = new java.util.ArrayList<org.mars_sim.msp.config.model.settlement.Template>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vTemplate
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addTemplate(
            final org.mars_sim.msp.config.model.settlement.Template vTemplate)
    throws java.lang.IndexOutOfBoundsException {
        this._templateList.add(vTemplate);
    }

    /**
     * 
     * 
     * @param index
     * @param vTemplate
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addTemplate(
            final int index,
            final org.mars_sim.msp.config.model.settlement.Template vTemplate)
    throws java.lang.IndexOutOfBoundsException {
        this._templateList.add(index, vTemplate);
    }

    /**
     * Method enumerateTemplate.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.settlement.Template> enumerateTemplate(
    ) {
        return java.util.Collections.enumeration(this._templateList);
    }

    /**
     * Method getTemplate.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.settlement.Template at the
     * given index
     */
    public org.mars_sim.msp.config.model.settlement.Template getTemplate(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._templateList.size()) {
            throw new IndexOutOfBoundsException("getTemplate: Index value '" + index + "' not in range [0.." + (this._templateList.size() - 1) + "]");
        }

        return _templateList.get(index);
    }

    /**
     * Method getTemplate.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.settlement.Template[] getTemplate(
    ) {
        org.mars_sim.msp.config.model.settlement.Template[] array = new org.mars_sim.msp.config.model.settlement.Template[0];
        return this._templateList.toArray(array);
    }

    /**
     * Method getTemplateCount.
     * 
     * @return the size of this collection
     */
    public int getTemplateCount(
    ) {
        return this._templateList.size();
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
     * Method iterateTemplate.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.settlement.Template> iterateTemplate(
    ) {
        return this._templateList.iterator();
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
    public void removeAllTemplate(
    ) {
        this._templateList.clear();
    }

    /**
     * Method removeTemplate.
     * 
     * @param vTemplate
     * @return true if the object was removed from the collection.
     */
    public boolean removeTemplate(
            final org.mars_sim.msp.config.model.settlement.Template vTemplate) {
        boolean removed = _templateList.remove(vTemplate);
        return removed;
    }

    /**
     * Method removeTemplateAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.settlement.Template removeTemplateAt(
            final int index) {
        java.lang.Object obj = this._templateList.remove(index);
        return (org.mars_sim.msp.config.model.settlement.Template) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vTemplate
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setTemplate(
            final int index,
            final org.mars_sim.msp.config.model.settlement.Template vTemplate)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._templateList.size()) {
            throw new IndexOutOfBoundsException("setTemplate: Index value '" + index + "' not in range [0.." + (this._templateList.size() - 1) + "]");
        }

        this._templateList.set(index, vTemplate);
    }

    /**
     * 
     * 
     * @param vTemplateArray
     */
    public void setTemplate(
            final org.mars_sim.msp.config.model.settlement.Template[] vTemplateArray) {
        //-- copy array
        _templateList.clear();

        for (int i = 0; i < vTemplateArray.length; i++) {
                this._templateList.add(vTemplateArray[i]);
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
     * org.mars_sim.msp.config.model.settlement.SettlementTemplateList
     */
    public static org.mars_sim.msp.config.model.settlement.SettlementTemplateList unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.settlement.SettlementTemplateList) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.settlement.SettlementTemplateList.class, reader);
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
