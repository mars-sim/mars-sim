/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.settlement.descriptors;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.mars_sim.msp.config.model.settlement.Template;

/**
 * Class TemplateDescriptor.
 * 
 * @version $Revision$ $Date$
 */
public class TemplateDescriptor extends org.exolab.castor.xml.util.XMLClassDescriptorImpl {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _elementDefinition.
     */
    private boolean _elementDefinition;

    /**
     * Field _nsPrefix.
     */
    private java.lang.String _nsPrefix;

    /**
     * Field _nsURI.
     */
    private java.lang.String _nsURI;

    /**
     * Field _xmlName.
     */
    private java.lang.String _xmlName;

    /**
     * Field _identity.
     */
    private org.exolab.castor.xml.XMLFieldDescriptor _identity;


      //----------------/
     //- Constructors -/
    //----------------/

    public TemplateDescriptor() {
        super();
        _nsURI = "http://mars-sim.sourceforge.net/settlements";
        _xmlName = "template";
        _elementDefinition = true;

        //-- set grouping compositor
        setCompositorAsSequence();
        org.exolab.castor.xml.util.XMLFieldDescriptorImpl  desc           = null;
        org.exolab.castor.mapping.FieldHandler             handler        = null;
        org.exolab.castor.xml.FieldValidator               fieldValidator = null;
        //-- initialize attribute descriptors

        //-- _name
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_name", "name", org.exolab.castor.xml.NodeType.Attribute);
        desc.setImmutable(true);
        handler = new org.exolab.castor.xml.XMLFieldHandler() {
            @Override
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Template target = (Template) object;
                return target.getName();
            }
            @Override
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Template target = (Template) object;
                    target.setName( (java.lang.String) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            @Override
            @SuppressWarnings("unused")
            public java.lang.Object newInstance(java.lang.Object parent) {
                return null;
            }
        };
        desc.setSchemaType("string");
        desc.setHandler(handler);
        desc.setMultivalued(false);
        addFieldDescriptor(desc);

        //-- validation code for: _name
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        { //-- local scope
            org.exolab.castor.xml.validators.StringValidator typeValidator;
            typeValidator = new org.exolab.castor.xml.validators.StringValidator();
            fieldValidator.setValidator(typeValidator);
            typeValidator.setWhiteSpace("preserve");
        }
        desc.setValidator(fieldValidator);
        //-- initialize element descriptors

        //-- _buildingList
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.mars_sim.msp.config.model.settlement.Building.class, "_buildingList", "building", org.exolab.castor.xml.NodeType.Element);
        handler = new org.exolab.castor.xml.XMLFieldHandler() {
            @Override
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Template target = (Template) object;
                return target.getBuilding();
            }
            @Override
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Template target = (Template) object;
                    target.addBuilding( (org.mars_sim.msp.config.model.settlement.Building) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            @Override
			public void resetValue(Object object) throws IllegalStateException, IllegalArgumentException {
                try {
                    Template target = (Template) object;
                    target.removeAllBuilding();
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            @Override
            @SuppressWarnings("unused")
            public java.lang.Object newInstance(java.lang.Object parent) {
                return new org.mars_sim.msp.config.model.settlement.Building();
            }
        };
        desc.setSchemaType("list");
        desc.setComponentType("org.mars_sim.msp.config.model.settlement.Building");
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://mars-sim.sourceforge.net/settlements");
        desc.setMultivalued(true);
        addFieldDescriptor(desc);
        addSequenceElement(desc);

        //-- validation code for: _buildingList
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(0);
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        //-- _vehicleList
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.mars_sim.msp.config.model.settlement.Vehicle.class, "_vehicleList", "vehicle", org.exolab.castor.xml.NodeType.Element);
        handler = new org.exolab.castor.xml.XMLFieldHandler() {
            @Override
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Template target = (Template) object;
                return target.getVehicle();
            }
            @Override
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Template target = (Template) object;
                    target.addVehicle( (org.mars_sim.msp.config.model.settlement.Vehicle) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            @Override
			public void resetValue(Object object) throws IllegalStateException, IllegalArgumentException {
                try {
                    Template target = (Template) object;
                    target.removeAllVehicle();
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            @Override
            @SuppressWarnings("unused")
            public java.lang.Object newInstance(java.lang.Object parent) {
                return new org.mars_sim.msp.config.model.settlement.Vehicle();
            }
        };
        desc.setSchemaType("list");
        desc.setComponentType("org.mars_sim.msp.config.model.settlement.Vehicle");
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://mars-sim.sourceforge.net/settlements");
        desc.setMultivalued(true);
        addFieldDescriptor(desc);
        addSequenceElement(desc);

        //-- validation code for: _vehicleList
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(0);
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        //-- _equipmentList
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.mars_sim.msp.config.model.settlement.Equipment.class, "_equipmentList", "equipment", org.exolab.castor.xml.NodeType.Element);
        handler = new org.exolab.castor.xml.XMLFieldHandler() {
            @Override
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Template target = (Template) object;
                return target.getEquipment();
            }
            @Override
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Template target = (Template) object;
                    target.addEquipment( (org.mars_sim.msp.config.model.settlement.Equipment) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            @Override
			public void resetValue(Object object) throws IllegalStateException, IllegalArgumentException {
                try {
                    Template target = (Template) object;
                    target.removeAllEquipment();
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            @Override
            @SuppressWarnings("unused")
            public java.lang.Object newInstance(java.lang.Object parent) {
                return new org.mars_sim.msp.config.model.settlement.Equipment();
            }
        };
        desc.setSchemaType("list");
        desc.setComponentType("org.mars_sim.msp.config.model.settlement.Equipment");
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://mars-sim.sourceforge.net/settlements");
        desc.setMultivalued(true);
        addFieldDescriptor(desc);
        addSequenceElement(desc);

        //-- validation code for: _equipmentList
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(0);
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        //-- _resourceList
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.mars_sim.msp.config.model.settlement.Resource.class, "_resourceList", "resource", org.exolab.castor.xml.NodeType.Element);
        handler = new org.exolab.castor.xml.XMLFieldHandler() {
            @Override
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Template target = (Template) object;
                return target.getResource();
            }
            @Override
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Template target = (Template) object;
                    target.addResource( (org.mars_sim.msp.config.model.settlement.Resource) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            @Override
			public void resetValue(Object object) throws IllegalStateException, IllegalArgumentException {
                try {
                    Template target = (Template) object;
                    target.removeAllResource();
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            @Override
            @SuppressWarnings("unused")
            public java.lang.Object newInstance(java.lang.Object parent) {
                return new org.mars_sim.msp.config.model.settlement.Resource();
            }
        };
        desc.setSchemaType("list");
        desc.setComponentType("org.mars_sim.msp.config.model.settlement.Resource");
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://mars-sim.sourceforge.net/settlements");
        desc.setMultivalued(true);
        addFieldDescriptor(desc);
        addSequenceElement(desc);

        //-- validation code for: _resourceList
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(0);
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        //-- _partList
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.mars_sim.msp.config.model.settlement.Part.class, "_partList", "part", org.exolab.castor.xml.NodeType.Element);
        handler = new org.exolab.castor.xml.XMLFieldHandler() {
            @Override
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Template target = (Template) object;
                return target.getPart();
            }
            @Override
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Template target = (Template) object;
                    target.addPart( (org.mars_sim.msp.config.model.settlement.Part) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            @Override
			public void resetValue(Object object) throws IllegalStateException, IllegalArgumentException {
                try {
                    Template target = (Template) object;
                    target.removeAllPart();
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            @Override
            @SuppressWarnings("unused")
            public java.lang.Object newInstance(java.lang.Object parent) {
                return new org.mars_sim.msp.config.model.settlement.Part();
            }
        };
        desc.setSchemaType("list");
        desc.setComponentType("org.mars_sim.msp.config.model.settlement.Part");
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://mars-sim.sourceforge.net/settlements");
        desc.setMultivalued(true);
        addFieldDescriptor(desc);
        addSequenceElement(desc);

        //-- validation code for: _partList
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(0);
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        //-- _partPackageList
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.mars_sim.msp.config.model.settlement.PartPackage.class, "_partPackageList", "part-package", org.exolab.castor.xml.NodeType.Element);
        handler = new org.exolab.castor.xml.XMLFieldHandler() {
            @Override
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Template target = (Template) object;
                return target.getPartPackage();
            }
            @Override
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Template target = (Template) object;
                    target.addPartPackage( (org.mars_sim.msp.config.model.settlement.PartPackage) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            @Override
			public void resetValue(Object object) throws IllegalStateException, IllegalArgumentException {
                try {
                    Template target = (Template) object;
                    target.removeAllPartPackage();
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            @Override
            @SuppressWarnings("unused")
            public java.lang.Object newInstance(java.lang.Object parent) {
                return new org.mars_sim.msp.config.model.settlement.PartPackage();
            }
        };
        desc.setSchemaType("list");
        desc.setComponentType("org.mars_sim.msp.config.model.settlement.PartPackage");
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://mars-sim.sourceforge.net/settlements");
        desc.setMultivalued(true);
        addFieldDescriptor(desc);
        addSequenceElement(desc);

        //-- validation code for: _partPackageList
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(0);
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        //-- _resupply
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.mars_sim.msp.config.model.settlement.Resupply.class, "_resupply", "resupply", org.exolab.castor.xml.NodeType.Element);
        handler = new org.exolab.castor.xml.XMLFieldHandler() {
            @Override
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Template target = (Template) object;
                return target.getResupply();
            }
            @Override
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Template target = (Template) object;
                    target.setResupply( (org.mars_sim.msp.config.model.settlement.Resupply) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            @Override
            @SuppressWarnings("unused")
            public java.lang.Object newInstance(java.lang.Object parent) {
                return new org.mars_sim.msp.config.model.settlement.Resupply();
            }
        };
        desc.setSchemaType("org.mars_sim.msp.config.model.settlement.Resupply");
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://mars-sim.sourceforge.net/settlements");
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        addSequenceElement(desc);

        //-- validation code for: _resupply
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method getAccessMode.
     * 
     * @return the access mode specified for this class.
     */
    @Override()
    public org.exolab.castor.mapping.AccessMode getAccessMode(
    ) {
        return null;
    }

    /**
     * Method getIdentity.
     * 
     * @return the identity field, null if this class has no
     * identity.
     */
    @Override()
    public org.exolab.castor.mapping.FieldDescriptor getIdentity(
    ) {
        return _identity;
    }

    /**
     * Method getJavaClass.
     * 
     * @return the Java class represented by this descriptor.
     */
    @Override()
    public java.lang.Class getJavaClass(
    ) {
        return org.mars_sim.msp.config.model.settlement.Template.class;
    }

    /**
     * Method getNameSpacePrefix.
     * 
     * @return the namespace prefix to use when marshaling as XML.
     */
    @Override()
    public java.lang.String getNameSpacePrefix(
    ) {
        return _nsPrefix;
    }

    /**
     * Method getNameSpaceURI.
     * 
     * @return the namespace URI used when marshaling and
     * unmarshaling as XML.
     */
    @Override()
    public java.lang.String getNameSpaceURI(
    ) {
        return _nsURI;
    }

    /**
     * Method getValidator.
     * 
     * @return a specific validator for the class described by this
     * ClassDescriptor.
     */
    @Override()
    public org.exolab.castor.xml.TypeValidator getValidator(
    ) {
        return this;
    }

    /**
     * Method getXMLName.
     * 
     * @return the XML Name for the Class being described.
     */
    @Override()
    public java.lang.String getXMLName(
    ) {
        return _xmlName;
    }

    /**
     * Method isElementDefinition.
     * 
     * @return true if XML schema definition of this Class is that
     * of a global
     * element or element with anonymous type definition.
     */
    @Override
	public boolean isElementDefinition(
    ) {
        return _elementDefinition;
    }

}
