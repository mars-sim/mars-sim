/*
 *  $Id: SimpleStack.java,v 1.2 2004/03/09 20:34:34 rconner Exp $
 *
 *  Copyright (C) 1994-2004 by Phoenix Software Technologists,
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

import java.util.*;


/**
 *  A simple stack data structure, basically a convenience extension
 *  of <code>ArrayList</code>.
 *
 *  @version    $Revision: 1.2 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public final class SimpleStack extends ArrayList
{

    private static final long serialVersionUID = 1L;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>SimpleStack</code>.
     */
    public SimpleStack()
    {
        super();
    }


    /**
     *  Creates a new <code>SimpleStack</code>.
     */
    public SimpleStack( Collection collection )
    {
        super( collection );
    }


    /**
     *  Creates a new <code>SimpleStack</code>.
     */
    public SimpleStack( int initialCapacity )
    {
        super( initialCapacity );
    }


    ////////////////////////////////////////
    // Methods
    ////////////////////////////////////////


    /**
     *  Pushes an object onto this <code>SimpleStack</code>.
     */
    public void push( Object object )
    {
        add( object );
    }


    /**
     *  Pops an object off of this <code>SimpleStack</code>.
     */
    public Object pop()
        throws EmptyStackException
    {
        if( isEmpty() ) {
            throw new EmptyStackException();
        }
        return remove( size() - 1 );
    }


    /**
     *  Return the top element of this <code>SimpleStack</code>.
     */
    public Object peek()
        throws EmptyStackException
    {
        if( isEmpty() ) {
            throw new EmptyStackException();
        }
        return get( size() - 1 );
    }

}
