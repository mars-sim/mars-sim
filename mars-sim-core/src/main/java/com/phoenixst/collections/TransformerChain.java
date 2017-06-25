/*
 *  $Id: TransformerChain.java,v 1.4 2006/06/07 19:14:57 rconner Exp $
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

import org.apache.commons.collections.Transformer;


/**
 *  A chain of <code>Transformers</code>.
 *
 *  @version    $Revision: 1.4 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class TransformerChain
    implements Transformer,
               Serializable
{

    private static final long serialVersionUID = 1L;

    /**
     *  The array of <code>Transformers</code>.
     *
     *  @serial
     */
    private final Transformer[] transformerArray;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>TransformerChain</code>.
     */
    public TransformerChain( Transformer[] transformers )
    {
        super();
        transformerArray = transformers.clone();
        if( containsNull( transformerArray ) ) {
            throw new IllegalArgumentException( "Transformer array has a null element." );
        }
    }


    /**
     *  Creates a new <code>TransformerChain</code>.
     */
    public TransformerChain( Collection transformers )
    {
        super();
        Transformer[] temp = new Transformer[ transformers.size() ];
        transformerArray = (Transformer[]) transformers.toArray( temp );
        if( containsNull( transformerArray ) ) {
            throw new IllegalArgumentException( "Collection argument has a null element." );
        }
    }


    /**
     *  Creates a new <code>TransformerChain</code>.
     */
    public TransformerChain( Transformer first, Transformer second )
    {
        super();
        transformerArray = new Transformer[] { first, second };
        if( first == null ) {
            throw new IllegalArgumentException( "First Transformer is null." );
        }
        if( second == null ) {
            throw new IllegalArgumentException( "Second Transformer is null." );
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
        if( transformerArray == null ) {
            throw new InvalidObjectException( "Transformer array is null." );
        }
        if( containsNull( transformerArray ) ) {
            throw new InvalidObjectException( "Transformer array has a null element." );
        }
    }


    ////////////////////////////////////////
    // Transformer
    ////////////////////////////////////////


    public Object transform( Object object )
    {
        Object result = object;
        for( int i = 0; i < transformerArray.length; i++ ) {
            result = transformerArray[i].transform( result );
        }
        return result;
    }


    ////////////////////////////////////////
    // Other methods
    ////////////////////////////////////////


    /**
     *  Returns an unmodifiable <code>List</code> of the argument
     *  <code>Transformers</code> being used by this
     *  <code>TransformerChain</code>.
     */
    public List getOperands()
    {
        return Collections.unmodifiableList( Arrays.asList( transformerArray ) );
    }


    public String toString()
    {
        StringBuilder s = new StringBuilder( getClass().getSimpleName() );
        s.append( Arrays.toString( transformerArray ) );
        return s.toString();
    }

}
