/*
 *  $Id: EqualsTraverserPredicate.java,v 1.7 2006/06/07 20:25:53 rconner Exp $
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

package com.phoenixst.plexus.util;

import java.io.*;

import org.apache.commons.collections.Predicate;

import com.phoenixst.collections.OrderedPair;
import com.phoenixst.plexus.Graph;


/**
 *  A <code>Predicate</code> which simply tests for equality with a
 *  specified {@link com.phoenixst.plexus.Graph.Edge}.
 *
 *  @version    $Revision: 1.7 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public final class EqualsTraverserPredicate
    implements Predicate,
               java.io.Serializable
{

    private static final long serialVersionUID = 1L;


    /**
     *  The <code>Edge</code> to test for equality.
     *
     *  @serial
     */
    private final Graph.Edge testEdge;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>EqualsTraverserPredicate</code> with the
     *  specified test <code>Edge</code>.
     */
    public EqualsTraverserPredicate( Graph.Edge testEdge )
    {
        super();
        this.testEdge = testEdge;
        if( testEdge == null ) {
            throw new IllegalArgumentException( "Test Graph.Edge is null." );
        }
    }


    ////////////////////////////////////////
    // Serialization
    ////////////////////////////////////////


    private void readObject( ObjectInputStream in )
        throws ClassNotFoundException,
               IOException
    {
        in.defaultReadObject();
        if( testEdge == null ) {
            throw new InvalidObjectException( "Test Graph.Edge is null." );
        }
    }


    ////////////////////////////////////////
    // Predicate
    ////////////////////////////////////////


    /**
     *  Returns <code>true</code> if the second element of the
     *  specified <code>List</code> is <code>.equals()</code> to the
     *  test <code>Edge</code>.
     */
    public boolean evaluate( Object object )
    {
        OrderedPair pair = (OrderedPair) object;
        return testEdge.equals( pair.getSecond() );
    }


    ////////////////////////////////////////
    // Other Methods
    ////////////////////////////////////////


    /**
     *  Returns the test <code>Edge</code> being used by this
     *  <code>EqualsTraverserPredicate</code>.
     */
    public Graph.Edge getTestEdge()
    {
        return testEdge;
    }


    public boolean equals( Object object )
    {
        if( object == this ) {
            return true;
        }
        if( !(object instanceof EqualsTraverserPredicate) ) {
            return false;
        }
        return testEdge.equals( ((EqualsTraverserPredicate) object).testEdge );
    }


    public int hashCode()
    {
        return testEdge.hashCode();
    }

}
