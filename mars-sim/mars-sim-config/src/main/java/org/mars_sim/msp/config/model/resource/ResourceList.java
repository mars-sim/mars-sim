/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.resource;

/**
 * Class ResourceList.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class ResourceList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _resourceList.
     */
    private java.util.List<org.mars_sim.msp.config.model.resource.Resource> _resourceList;


      //----------------/
     //- Constructors -/
    //----------------/

    public ResourceList() {
        super();
        this._resourceList = new java.util.ArrayList<org.mars_sim.msp.config.model.resource.Resource>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vResource
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addResource(
            final org.mars_sim.msp.config.model.resource.Resource vResource)
    throws java.lang.IndexOutOfBoundsException {
        this._resourceList.add(vResource);
    }

    /**
     * 
     * 
     * @param index
     * @param vResource
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addResource(
            final int index,
            final org.mars_sim.msp.config.model.resource.Resource vResource)
    throws java.lang.IndexOutOfBoundsException {
        this._resourceList.add(index, vResource);
    }

    /**
     * Method enumerateResource.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public java.util.Enumeration<? extends org.mars_sim.msp.config.model.resource.Resource> enumerateResource(
    ) {
        return java.util.Collections.enumeration(this._resourceList);
    }

    /**
     * Method getResource.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.mars_sim.msp.config.model.resource.Resource at the given
     * index
     */
    public org.mars_sim.msp.config.model.resource.Resource getResource(
            final int index)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._resourceList.size()) {
            throw new IndexOutOfBoundsException("getResource: Index value '" + index + "' not in range [0.." + (this._resourceList.size() - 1) + "]");
        }

        return _resourceList.get(index);
    }

    /**
     * Method getResource.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.mars_sim.msp.config.model.resource.Resource[] getResource(
    ) {
        org.mars_sim.msp.config.model.resource.Resource[] array = new org.mars_sim.msp.config.model.resource.Resource[0];
        return this._resourceList.toArray(array);
    }

    /**
     * Method getResourceCount.
     * 
     * @return the size of this collection
     */
    public int getResourceCount(
    ) {
        return this._resourceList.size();
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
     * Method iterateResource.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public java.util.Iterator<? extends org.mars_sim.msp.config.model.resource.Resource> iterateResource(
    ) {
        return this._resourceList.iterator();
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
    public void removeAllResource(
    ) {
        this._resourceList.clear();
    }

    /**
     * Method removeResource.
     * 
     * @param vResource
     * @return true if the object was removed from the collection.
     */
    public boolean removeResource(
            final org.mars_sim.msp.config.model.resource.Resource vResource) {
        boolean removed = _resourceList.remove(vResource);
        return removed;
    }

    /**
     * Method removeResourceAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.mars_sim.msp.config.model.resource.Resource removeResourceAt(
            final int index) {
        java.lang.Object obj = this._resourceList.remove(index);
        return (org.mars_sim.msp.config.model.resource.Resource) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vResource
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setResource(
            final int index,
            final org.mars_sim.msp.config.model.resource.Resource vResource)
    throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._resourceList.size()) {
            throw new IndexOutOfBoundsException("setResource: Index value '" + index + "' not in range [0.." + (this._resourceList.size() - 1) + "]");
        }

        this._resourceList.set(index, vResource);
    }

    /**
     * 
     * 
     * @param vResourceArray
     */
    public void setResource(
            final org.mars_sim.msp.config.model.resource.Resource[] vResourceArray) {
        //-- copy array
        _resourceList.clear();

        for (int i = 0; i < vResourceArray.length; i++) {
                this._resourceList.add(vResourceArray[i]);
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
     * org.mars_sim.msp.config.model.resource.ResourceList
     */
    public static org.mars_sim.msp.config.model.resource.ResourceList unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.mars_sim.msp.config.model.resource.ResourceList) org.exolab.castor.xml.Unmarshaller.unmarshal(org.mars_sim.msp.config.model.resource.ResourceList.class, reader);
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
