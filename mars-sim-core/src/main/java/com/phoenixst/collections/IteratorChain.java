/*
 *  $Id: IteratorChain.java,v 1.11 2006/06/07 19:14:57 rconner Exp $
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

import java.util.*;


/**
 *  A chain of <code>Iterators</code>.  The one in Jakarta
 *  commons-collections doesn't work.
 *
 *  <P>This is similar to the class of the same name in Jakarta
 *  Commons-Collections 3.0.  This implementation is missing all of
 *  the locking and safeguards of the other one.  This implementation
 *  fixes a bug in version 2.1 where hasNext() would return false if
 *  the first iterator was empty, but subsequent ones were not.
 *
 *  @version    $Revision: 1.11 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class IteratorChain
    implements Iterator
{

    private final Iterator[] iteratorArray;

    private int index = 0;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>IteratorChain</code>.
     */
    public IteratorChain( Iterator[] iterators )
    {
        super();
        iteratorArray = iterators.clone();
        if( containsNull( iteratorArray ) ) {
            throw new IllegalArgumentException( "Iterator array has a null element." );
        }
    }


    /**
     *  Creates a new <code>IteratorChain</code>.
     */
    public IteratorChain( Collection iterators )
    {
        super();
        Iterator[] temp = new Iterator[ iterators.size() ];
        iteratorArray = (Iterator[]) iterators.toArray( temp );
        if( containsNull( iteratorArray ) ) {
            throw new IllegalArgumentException( "Collection argument has a null element." );
        }
    }


    /**
     *  Creates a new <code>IteratorChain</code>.
     */
    public IteratorChain( Iterator first, Iterator second )
    {
        super();
        iteratorArray = new Iterator[] { first, second };
        if( first == null ) {
            throw new IllegalArgumentException( "First Iterator is null." );
        }
        if( second == null ) {
            throw new IllegalArgumentException( "Second Iterator is null." );
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
    // Iterator
    ////////////////////////////////////////


    public boolean hasNext()
    {
        for( int i = index; i < iteratorArray.length; i++ ) {
            if( iteratorArray[i].hasNext() ) {
                return true;
            }
        }
        return false;
    }


    public Object next()
    {
        for(; index < iteratorArray.length; index++ ) {
            if( iteratorArray[index].hasNext() ) {
                return iteratorArray[index].next();
            }
        }
        throw new NoSuchElementException();
    }


    public void remove()
    {
        if( index >= iteratorArray.length ) {
            throw new IllegalStateException();
        }
        iteratorArray[index].remove();
    }

}
