/*
 *  $Id: ClosureChain.java,v 1.4 2006/06/07 19:14:57 rconner Exp $
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

import org.apache.commons.collections.Closure;


/**
 *  A chain of <code>Closures</code>.
 *
 *  @version    $Revision: 1.4 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class ClosureChain
    implements Closure,
               Serializable
{

    private static final long serialVersionUID = 1L;

    /**
     *  The array of <code>Closures</code>.
     *
     *  @serial
     */
    private final Closure[] closureArray;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>ClosureChain</code>.
     */
    public ClosureChain( Closure[] closures )
    {
        super();
        closureArray = closures.clone();
        if( containsNull( closureArray ) ) {
            throw new IllegalArgumentException( "Closure array has a null element." );
        }
    }


    /**
     *  Creates a new <code>ClosureChain</code>.
     */
    public ClosureChain( Collection closures )
    {
        super();
        Closure[] temp = new Closure[ closures.size() ];
        closureArray = (Closure[]) closures.toArray( temp );
        if( containsNull( closureArray ) ) {
            throw new IllegalArgumentException( "Collection argument has a null element." );
        }
    }


    /**
     *  Creates a new <code>ClosureChain</code>.
     */
    public ClosureChain( Closure first, Closure second )
    {
        super();
        closureArray = new Closure[] { first, second };
        if( first == null ) {
            throw new IllegalArgumentException( "First Closure is null." );
        }
        if( second == null ) {
            throw new IllegalArgumentException( "Second Closure is null." );
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
        if( closureArray == null ) {
            throw new InvalidObjectException( "Closure array is null." );
        }
        if( containsNull( closureArray ) ) {
            throw new InvalidObjectException( "Closure array has a null element." );
        }
    }


    ////////////////////////////////////////
    // Closure
    ////////////////////////////////////////


    public void execute( Object object )
    {
        for( int i = 0; i < closureArray.length; i++ ) {
            closureArray[i].execute( object );
        }
    }


    ////////////////////////////////////////
    // Other methods
    ////////////////////////////////////////


    /**
     *  Returns an unmodifiable <code>List</code> of the argument
     *  <code>Closures</code> being used by this
     *  <code>ClosureChain</code>.
     */
    public List getOperands()
    {
        return Collections.unmodifiableList( Arrays.asList( closureArray ) );
    }


    public String toString()
    {
        StringBuilder s = new StringBuilder( getClass().getSimpleName() );
        s.append( Arrays.toString( closureArray ) );
        return s.toString();
    }

}
