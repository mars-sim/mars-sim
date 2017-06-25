/*
 *  $Id: TraverserChain.java,v 1.9 2006/06/07 19:14:57 rconner Exp $
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

import java.util.*;

import com.phoenixst.plexus.*;


/**
 *  A chain of <code>Traversers</code>.
 *
 *  @version    $Revision: 1.9 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class TraverserChain
    implements Traverser
{

    private final Traverser[] traverserArray;

    private int index = 0;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>TraveserChain</code>.
     */
    public TraverserChain( Traverser[] traversers )
    {
        super();
        traverserArray = traversers.clone();
        if( containsNull( traverserArray ) ) {
            throw new IllegalArgumentException( "Traverser array has a null element." );
        }
    }


    /**
     *  Creates a new <code>TraveserChain</code>.
     */
    public TraverserChain( Collection traversers )
    {
        super();
        Traverser[] temp = new Traverser[ traversers.size() ];
        traverserArray = (Traverser[]) traversers.toArray( temp );
        if( containsNull( traverserArray ) ) {
            throw new IllegalArgumentException( "Collection argument has a null element." );
        }
    }


    /**
     *  Creates a new <code>TraveserChain</code>.
     */
    public TraverserChain( Traverser first, Traverser second )
    {
        super();
        traverserArray = new Traverser[] { first, second };
        if( first == null ) {
            throw new IllegalArgumentException( "First Traverser is null." );
        }
        if( second == null ) {
            throw new IllegalArgumentException( "Second Traverser is null." );
        }
    }


    ////////////////////////////////////////
    // Construction assistance methods
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


    ////////////////////////////////////////
    // Traverser
    ////////////////////////////////////////


    public boolean hasNext()
    {
        for( int i = index; i < traverserArray.length; i++ ) {
            if( traverserArray[i].hasNext() ) {
                return true;
            }
        }
        return false;
    }


    public Object next()
    {
        for(; index < traverserArray.length; index++ ) {
            if( traverserArray[index].hasNext() ) {
                return traverserArray[index].next();
            }
        }
        throw new NoSuchElementException();
    }


    public void remove()
    {
        if( index >= traverserArray.length ) {
            throw new IllegalStateException();
        }
        traverserArray[index].remove();
    }


    public Graph.Edge getEdge()
    {
        if( index >= traverserArray.length ) {
            throw new IllegalStateException();
        }
        return traverserArray[index].getEdge();
    }


    public void removeEdge()
    {
        if( index >= traverserArray.length ) {
            throw new IllegalStateException();
        }
        traverserArray[index].removeEdge();
    }

}
