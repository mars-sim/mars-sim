/*
 *  $Id: AllPredicate.java,v 1.12 2006/06/07 22:27:37 rconner Exp $
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
import java.util.*;

import org.apache.commons.collections.Predicate;


/**
 *  A <code>Predicate</code> which returns the logical short-circuit
 *  <em>and</em> of its operands.
 *
 *  <P>This is mostly equivalent to the class of the same name in
 *  Jakarta Commons-Collections 3.0.  The deserialization process
 *  in this version checks for null fields.  No equivalent exists
 *  in version 2.1.
 *
 *  @version    $Revision: 1.12 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public final class AllPredicate
    implements Predicate,
               Serializable
{

    private static final long serialVersionUID = 2L;

    /**
     *  The array of <code>Predicates</code>.
     *
     *  @serial
     */
    private final Predicate[] predicateArray;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>AllPredicate</code>.
     */
    public AllPredicate( Predicate[] predicates )
    {
        super();
        predicateArray = predicates.clone();
        if( containsNull( predicateArray ) ) {
            throw new IllegalArgumentException( "Predicate array has a null element." );
        }
    }


    /**
     *  Creates a new <code>AllPredicate</code>.
     */
    public AllPredicate( Collection predicates )
    {
        super();
        Predicate[] temp = new Predicate[ predicates.size() ];
        predicateArray = (Predicate[]) predicates.toArray( temp );
        if( containsNull( predicateArray ) ) {
            throw new IllegalArgumentException( "Collection argument has a null element." );
        }
    }


    ////////////////////////////////////////
    // Serialization and construction assistance methods
    ////////////////////////////////////////


    private static boolean containsNull( Object[] array )
    {
        for( int i = array.length - 1; i >= 0; i-- ) {
            if( array[i] == null ) {
                return true;
            }
        }
        return false;
    }


    private void readObject( ObjectInputStream in )
        throws ClassNotFoundException,
               IOException
    {
        in.defaultReadObject();
        if( predicateArray == null ) {
            throw new InvalidObjectException( "Predicate array is null." );
        }
        if( containsNull( predicateArray ) ) {
            throw new InvalidObjectException( "Predicate array has a null element." );
        }
    }


    ////////////////////////////////////////
    // Predicate
    ////////////////////////////////////////


    public boolean evaluate( Object object )
    {
        for( int i = 0; i < predicateArray.length; i++ ) {
            if( !predicateArray[i].evaluate( object ) ) {
                return false;
            }
        }
        return true;
    }


    ////////////////////////////////////////
    // Other methods
    ////////////////////////////////////////


    /**
     *  Returns an unmodifiable <code>List</code> of the argument
     *  <code>Predicates</code> being used by this
     *  <code>AllPredicate</code>.
     */
    public List getOperands()
    {
        return Collections.unmodifiableList( Arrays.asList( predicateArray ) );
    }


    public boolean equals( Object object )
    {
        if( object == this ) {
            return true;
        }
        if( !(object instanceof AllPredicate) ) {
            return false;
        }
        AllPredicate pred = (AllPredicate) object;
        return Arrays.equals( predicateArray, pred.predicateArray );
    }


    public int hashCode()
    {
        return Arrays.hashCode( predicateArray );
    }


    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append( "(ALL " );
        s.append( Arrays.toString( predicateArray ) );
        s.append( ")" );
        return s.toString();
    }

}
