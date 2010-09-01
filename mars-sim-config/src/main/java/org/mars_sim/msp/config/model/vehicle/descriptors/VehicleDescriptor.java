/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.mars_sim.msp.config.model.vehicle.descriptors;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.mars_sim.msp.config.model.vehicle.Vehicle;

/**
 * Class VehicleDescriptor.
 * 
 * @version $Revision$ $Date$
 */
public class VehicleDescriptor extends org.exolab.castor.xml.util.XMLClassDescriptorImpl {


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

    public VehicleDescriptor() {
        super();
        _nsURI = "http://mars-sim.sourceforge.net/vehicles";
        _xmlName = "vehicle";
        _elementDefinition = true;

        //-- set grouping compositor
        setCompositorAsSequence();
        org.exolab.castor.xml.util.XMLFieldDescriptorImpl  desc           = null;
        org.exolab.castor.mapping.FieldHandler             handler        = null;
        org.exolab.castor.xml.FieldValidator               fieldValidator = null;
        //-- initialize attribute descriptors

        //-- _type
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_type", "type", org.exolab.castor.xml.NodeType.Attribute);
        desc.setImmutable(true);
        handler = new org.exolab.castor.xml.XMLFieldHandler() {
            @Override
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Vehicle target = (Vehicle) object;
                return target.getType();
            }
            @Override
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Vehicle target = (Vehicle) object;
                    target.setType( (java.lang.String) value);
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

        //-- validation code for: _type
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        { //-- local scope
            org.exolab.castor.xml.validators.StringValidator typeValidator;
            typeValidator = new org.exolab.castor.xml.validators.StringValidator();
            fieldValidator.setValidator(typeValidator);
            typeValidator.setWhiteSpace("preserve");
        }
        desc.setValidator(fieldValidator);
        //-- initialize element descriptors

        //-- _fuelEfficiency
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.mars_sim.msp.config.model.vehicle.FuelEfficiency.class, "_fuelEfficiency", "fuel-efficiency", org.exolab.castor.xml.NodeType.Element);
        handler = new org.exolab.castor.xml.XMLFieldHandler() {
            @Override
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Vehicle target = (Vehicle) object;
                return target.getFuelEfficiency();
            }
            @Override
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Vehicle target = (Vehicle) object;
                    target.setFuelEfficiency( (org.mars_sim.msp.config.model.vehicle.FuelEfficiency) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            @Override
            @SuppressWarnings("unused")
            public java.lang.Object newInstance(java.lang.Object parent) {
                return new org.mars_sim.msp.config.model.vehicle.FuelEfficiency();
            }
        };
        desc.setSchemaType("org.mars_sim.msp.config.model.vehicle.FuelEfficiency");
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://mars-sim.sourceforge.net/vehicles");
        desc.setRequired(true);
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        addSequenceElement(desc);

        //-- validation code for: _fuelEfficiency
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(1);
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        //-- _baseSpeed
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.mars_sim.msp.config.model.vehicle.BaseSpeed.class, "_baseSpeed", "base-speed", org.exolab.castor.xml.NodeType.Element);
        handler = new org.exolab.castor.xml.XMLFieldHandler() {
            @Override
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Vehicle target = (Vehicle) object;
                return target.getBaseSpeed();
            }
            @Override
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Vehicle target = (Vehicle) object;
                    target.setBaseSpeed( (org.mars_sim.msp.config.model.vehicle.BaseSpeed) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            @Override
            @SuppressWarnings("unused")
            public java.lang.Object newInstance(java.lang.Object parent) {
                return new org.mars_sim.msp.config.model.vehicle.BaseSpeed();
            }
        };
        desc.setSchemaType("org.mars_sim.msp.config.model.vehicle.BaseSpeed");
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://mars-sim.sourceforge.net/vehicles");
        desc.setRequired(true);
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        addSequenceElement(desc);

        //-- validation code for: _baseSpeed
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(1);
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        //-- _emptyMass
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.mars_sim.msp.config.model.vehicle.EmptyMass.class, "_emptyMass", "empty-mass", org.exolab.castor.xml.NodeType.Element);
        handler = new org.exolab.castor.xml.XMLFieldHandler() {
            @Override
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Vehicle target = (Vehicle) object;
                return target.getEmptyMass();
            }
            @Override
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Vehicle target = (Vehicle) object;
                    target.setEmptyMass( (org.mars_sim.msp.config.model.vehicle.EmptyMass) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            @Override
            @SuppressWarnings("unused")
            public java.lang.Object newInstance(java.lang.Object parent) {
                return new org.mars_sim.msp.config.model.vehicle.EmptyMass();
            }
        };
        desc.setSchemaType("org.mars_sim.msp.config.model.vehicle.EmptyMass");
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://mars-sim.sourceforge.net/vehicles");
        desc.setRequired(true);
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        addSequenceElement(desc);

        //-- validation code for: _emptyMass
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(1);
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        //-- _crewSize
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.mars_sim.msp.config.model.vehicle.CrewSize.class, "_crewSize", "crew-size", org.exolab.castor.xml.NodeType.Element);
        handler = new org.exolab.castor.xml.XMLFieldHandler() {
            @Override
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Vehicle target = (Vehicle) object;
                return target.getCrewSize();
            }
            @Override
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Vehicle target = (Vehicle) object;
                    target.setCrewSize( (org.mars_sim.msp.config.model.vehicle.CrewSize) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            @Override
            @SuppressWarnings("unused")
            public java.lang.Object newInstance(java.lang.Object parent) {
                return new org.mars_sim.msp.config.model.vehicle.CrewSize();
            }
        };
        desc.setSchemaType("org.mars_sim.msp.config.model.vehicle.CrewSize");
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://mars-sim.sourceforge.net/vehicles");
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        addSequenceElement(desc);

        //-- validation code for: _crewSize
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        //-- _cargo
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.mars_sim.msp.config.model.vehicle.Cargo.class, "_cargo", "cargo", org.exolab.castor.xml.NodeType.Element);
        handler = new org.exolab.castor.xml.XMLFieldHandler() {
            @Override
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Vehicle target = (Vehicle) object;
                return target.getCargo();
            }
            @Override
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Vehicle target = (Vehicle) object;
                    target.setCargo( (org.mars_sim.msp.config.model.vehicle.Cargo) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            @Override
            @SuppressWarnings("unused")
            public java.lang.Object newInstance(java.lang.Object parent) {
                return new org.mars_sim.msp.config.model.vehicle.Cargo();
            }
        };
        desc.setSchemaType("org.mars_sim.msp.config.model.vehicle.Cargo");
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://mars-sim.sourceforge.net/vehicles");
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        addSequenceElement(desc);

        //-- validation code for: _cargo
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        //-- _partAttachment
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.mars_sim.msp.config.model.vehicle.PartAttachment.class, "_partAttachment", "part-attachment", org.exolab.castor.xml.NodeType.Element);
        handler = new org.exolab.castor.xml.XMLFieldHandler() {
            @Override
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Vehicle target = (Vehicle) object;
                return target.getPartAttachment();
            }
            @Override
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Vehicle target = (Vehicle) object;
                    target.setPartAttachment( (org.mars_sim.msp.config.model.vehicle.PartAttachment) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            @Override
            @SuppressWarnings("unused")
            public java.lang.Object newInstance(java.lang.Object parent) {
                return new org.mars_sim.msp.config.model.vehicle.PartAttachment();
            }
        };
        desc.setSchemaType("org.mars_sim.msp.config.model.vehicle.PartAttachment");
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://mars-sim.sourceforge.net/vehicles");
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        addSequenceElement(desc);

        //-- validation code for: _partAttachment
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        //-- _sickbay
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.mars_sim.msp.config.model.vehicle.Sickbay.class, "_sickbay", "sickbay", org.exolab.castor.xml.NodeType.Element);
        handler = new org.exolab.castor.xml.XMLFieldHandler() {
            @Override
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Vehicle target = (Vehicle) object;
                return target.getSickbay();
            }
            @Override
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Vehicle target = (Vehicle) object;
                    target.setSickbay( (org.mars_sim.msp.config.model.vehicle.Sickbay) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            @Override
            @SuppressWarnings("unused")
            public java.lang.Object newInstance(java.lang.Object parent) {
                return new org.mars_sim.msp.config.model.vehicle.Sickbay();
            }
        };
        desc.setSchemaType("org.mars_sim.msp.config.model.vehicle.Sickbay");
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://mars-sim.sourceforge.net/vehicles");
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        addSequenceElement(desc);

        //-- validation code for: _sickbay
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        //-- _lab
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(org.mars_sim.msp.config.model.vehicle.Lab.class, "_lab", "lab", org.exolab.castor.xml.NodeType.Element);
        handler = new org.exolab.castor.xml.XMLFieldHandler() {
            @Override
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Vehicle target = (Vehicle) object;
                return target.getLab();
            }
            @Override
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Vehicle target = (Vehicle) object;
                    target.setLab( (org.mars_sim.msp.config.model.vehicle.Lab) value);
                } catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            @Override
            @SuppressWarnings("unused")
            public java.lang.Object newInstance(java.lang.Object parent) {
                return new org.mars_sim.msp.config.model.vehicle.Lab();
            }
        };
        desc.setSchemaType("org.mars_sim.msp.config.model.vehicle.Lab");
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://mars-sim.sourceforge.net/vehicles");
        desc.setMultivalued(false);
        addFieldDescriptor(desc);
        addSequenceElement(desc);

        //-- validation code for: _lab
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
        return org.mars_sim.msp.config.model.vehicle.Vehicle.class;
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
    public boolean isElementDefinition(
    ) {
        return _elementDefinition;
    }

}
