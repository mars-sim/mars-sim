/*
 *  $Id: EqualPredicate.java,v 1.10 2005/10/03 15:11:54 rconner Exp $
 *
 *  Copyright (C) 1994-2005 by Phoenix Software Technologists,
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

import org.apache.commons.collections.Predicate;


/**
 *  A <code>Predicate</code> which simply tests the {@link #evaluate
 *  evaluate( object )} argument for equality with a specified object.
 *
 *  <P>This is mostly equivalent to the class of the same name in
 *  Jakarta Commons-Collections 3.0, except that it exposes the test
 *  object used in the constructor.  No equivalent exists in version
 *  2.1.
 *
 *  @version    $Revision: 1.10 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public final class EqualPredicate
    implements Predicate,
               java.io.Serializable
{

    private static final long serialVersionUID = 1L;


    /**
     *  The object to test for equality.
     *
     *  @serial
     */
    private final Object testObject;


    /**
     *  Creates a new <code>EqualPredicate</code> with the specified
     *  test object.
     */
    public EqualPredicate( Object testObject )
    {
        super();
        this.testObject = testObject;
    }


    /**
     *  Returns <code>true</code> if the specified <code>object</code>
     *  is <code>.equals()</code> to the test object, or if both are
     *  <code>null</code>.
     */
    public boolean evaluate( Object object )
    {
        return (testObject == object)
            || (testObject != null
                && testObject.equals( object ));
    }


    /**
     *  Returns the test object being used by this
     *  <code>EqualPredicate</code>.
     */
    public Object getTestObject()
    {
        return testObject;
    }


    public boolean equals( Object object )
    {
        if( object == this ) {
            return true;
        }
        if( !(object instanceof EqualPredicate) ) {
            return false;
        }
        Object otherObject = ((EqualPredicate) object).testObject;
        return (testObject == otherObject)
            || (testObject != null
                && testObject.equals( otherObject ));
    }


    public int hashCode()
    {
        // 17 & 37 are arbitrary, but non-zero and prime
        return 17 * 37
            + ((testObject == null) ? 0 : testObject.hashCode());
    }


    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append( "Equals(" );
        s.append( testObject );
        s.append( ")" );
        return s.toString();
    }

}
