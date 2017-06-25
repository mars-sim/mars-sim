/*
 *  $Id: OrderedPair.java,v 1.6 2005/10/03 15:11:54 rconner Exp $
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

import java.io.Serializable;
import java.util.*;


/**
 *  A simple mutable ordered pair implementation.  The individual
 *  elements may be changed, but the size of this implementation
 *  cannot.
 *
 *  @version    $Revision: 1.6 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class OrderedPair extends AbstractList
    implements RandomAccess,
               Serializable
{

    private static final long serialVersionUID = 1L;

    /**
     *  The first element.
     *
     *  @serial
     */
    private Object first;

    /**
     *  The second element.
     *
     *  @serial
     */
    private Object second;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    public OrderedPair()
    {
        this( null, null );
    }


    public OrderedPair( Object first, Object second )
    {
        super();
        this.first = first;
        this.second = second;
    }


    ////////////////////////////////////////
    // Convenience accessors
    ////////////////////////////////////////


    public Object getFirst()
    {
        return first;
    }


    public void setFirst( Object first )
    {
        this.first = first;
    }


    public Object getSecond()
    {
        return second;
    }


    public void setSecond( Object second )
    {
        this.second = second;
    }


    ////////////////////////////////////////
    // List
    ////////////////////////////////////////


    public int size()
    {
        return 2;
    }


    public boolean isEmpty()
    {
        return false;
    }


    public boolean contains( Object object )
    {
        if( object == null ) {
            return first == null || second == null;
        }
        return object.equals( first ) || object.equals( second );
    }


    public Object get( int index )
    {
        if( index == 0 ) {
            return first;
        } else if( index == 1 ) {
            return second;
        } else {
            throw new IndexOutOfBoundsException( "Index: " + index );
        }
    }


    public Object set( int index, Object object )
    {
        Object oldObject;
        if( index == 0 ) {
            oldObject = first;
            first = object;
        } else if( index == 1 ) {
            oldObject = second;
            second = object;
        } else {
            throw new IndexOutOfBoundsException( "Index: " + index );
        }
        return oldObject;
    }


    public Object[] toArray()
    {
        return new Object[] { first, second };
    }

}
