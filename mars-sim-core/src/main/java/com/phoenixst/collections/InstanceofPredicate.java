/*
 *  $Id: InstanceofPredicate.java,v 1.10 2006/06/07 16:33:29 rconner Exp $
 *
 *  Copyright (C) 1994-2006 by Phoenix Software Technologists,
 *  Inc. and others.  All rights reserved.
 *
 *  THIS PROGRAM AND DOCUMENTATION IS PROVIDED UNDER THE TERMS OF THE
 *  COMMON PUBLIC LICENSE ("AGREEMENT") WHICH ACCOMPANIES IT.  ANY
 *  USE, REPRODUCTION OR DISTRIBUTION OF THE PROGRAM CONSTITUTES
 *  RECIPIENT'S ACCEPTANCE OF THE AGREEMENT.
 *
 *  The license text can also be found at
 *    http://opensource.org/licenses/cpl.php
 */

package com.phoenixst.collections;

import java.io.*;

import org.apache.commons.collections.Predicate;


/**
 *  A <code>Predicate</code> which tests the {@link #evaluate
 *  evaluate( object )} argument for being an instance of a particular
 *  class.
 *
 *  <P>This is mostly equivalent to the class of the same name in
 *  Jakarta Commons-Collections 3.0, except that it exposes the test
 *  object used in the constructor.  The deserialization process in
 *  this version checks for null fields.  No equivalent exists in
 *  version 2.1.
 *
 *  @version    $Revision: 1.10 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public final class InstanceofPredicate
    implements Predicate,
               Serializable
{

    private static final long serialVersionUID = 1L;


    /**
     *  The class to test for membership.
     *
     *  @serial
     */
    private final Class testClass;


    /**
     *  Creates a new <code>InstanceofPredicate</code> with the
     *  specified test class.
     */
    public InstanceofPredicate( Class testClass )
    {
        super();
        this.testClass = testClass;
        if( testClass == null ) {
            throw new IllegalArgumentException( "Class cannot be null." );
        }
    }


    private void readObject( ObjectInputStream in )
        throws ClassNotFoundException,
               IOException
    {
        in.defaultReadObject();
        if( testClass == null ) {
            throw new InvalidObjectException( "Class is null." );
        }
    }


    public boolean evaluate( Object object )
    {
        return testClass.isInstance( object );
    }


    /**
     *  Returns the test class being used by this
     *  <code>InstanceofPredicate</code>.
     */
    public Class getTestClass()
    {
        return testClass;
    }


    public boolean equals( Object object )
    {
        if( object == this ) {
            return true;
        }
        if( !(object instanceof InstanceofPredicate) ) {
            return false;
        }
        return testClass.equals( ((InstanceofPredicate) object).testClass );
    }


    public int hashCode()
    {
        return testClass.hashCode();
    }


    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append( "Instanceof(" );
        s.append( testClass );
        s.append( ")" );
        return s.toString();
    }

}
