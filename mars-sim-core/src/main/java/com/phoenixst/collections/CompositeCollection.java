/*
 *  $Id: CompositeCollection.java,v 1.11 2006/06/07 19:14:57 rconner Exp $
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


/**
 *  A <code>Collection</code> view of a number of other
 *  <code>Collections</code>.  New elements are added to the last
 *  <code>Collection</code> and elements are removed from the first
 *  <code>Collection</code> in which they are found.
 *
 *  @version    $Revision: 1.11 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class CompositeCollection extends AbstractCollection
    implements Serializable
{

    private static final long serialVersionUID = 1L;

    /**
     *  The array of <code>Collections</code>.
     *
     *  @serial
     */
    private final Collection[] collectionArray;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>CompositeCollection</code>.
     */
    public CompositeCollection( Collection[] collections )
    {
        super();
        collectionArray = collections.clone();
        if( containsNull( collectionArray ) ) {
            throw new IllegalArgumentException( "Collection array has a null element." );
        }
    }


    /**
     *  Creates a new <code>CompositeCollection</code>.
     */
    public CompositeCollection( Collection collections )
    {
        super();
        Collection[] temp = new Collection[ collections.size() ];
        collectionArray = (Collection[]) collections.toArray( temp );
        if( containsNull( collectionArray ) ) {
            throw new IllegalArgumentException( "Collection argument has a null element." );
        }
    }


    /**
     *  Creates a new <code>CompositeCollection</code>.
     */
    public CompositeCollection( Collection first, Collection second )
    {
        super();
        collectionArray = new Collection[] { first, second };
        if( first == null ) {
            throw new IllegalArgumentException( "First Collection is null." );
        }
        if( second == null ) {
            throw new IllegalArgumentException( "Second Collection is null." );
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
        if( collectionArray == null ) {
            throw new InvalidObjectException( "Collection array is null." );
        }
        if( containsNull( collectionArray ) ) {
            throw new InvalidObjectException( "Collection array has a null element." );
        }
    }


    ////////////////////////////////////////
    // Collection
    ////////////////////////////////////////


    public int size()
    {
        int size = 0;
        for( int i = collectionArray.length - 1; i >= 0; i-- ) {
            size += collectionArray[i].size();
        }
        return size;
    }


    public boolean isEmpty()
    {
        for( int i = collectionArray.length - 1; i >= 0; i-- ) {
            if( !collectionArray[i].isEmpty() ) {
                return false;
            }
        }
        return true;
    }


    public boolean add( Object object )
    {
        return collectionArray[ collectionArray.length - 1 ].add( object );
    }


    public boolean remove( Object object )
    {
        for( int i = 0; i < collectionArray.length; i++ ) {
            if( collectionArray[i].remove( object ) ) {
                return true;
            }
        }
        return false;
    }


    public boolean contains( Object object )
    {
        for( int i = collectionArray.length - 1; i >= 0; i-- ) {
            if( collectionArray[i].contains( object ) ) {
                return true;
            }
        }
        return false;
    }


    public Iterator iterator()
    {
        Iterator[] iteratorArray = new Iterator[ collectionArray.length ];
        for( int i = collectionArray.length - 1; i >= 0; i-- ) {
            iteratorArray[i] = collectionArray[i].iterator();
        }
        return new IteratorChain( iteratorArray );
    }


    public boolean addAll( Collection collection )
    {
        return collectionArray[ collectionArray.length - 1 ].addAll( collection );
    }


    public boolean removeAll( Collection collection )
    {
        if( collection.isEmpty() ) {
            return false;
        }
        boolean modified = false;
        for( int i = collectionArray.length - 1; i >= 0; i-- ) {
            modified |= collectionArray[i].removeAll( collection );
        }
        return modified;
    }


    public boolean retainAll( Collection collection )
    {
        if( collection.isEmpty() ) {
            if( isEmpty() ) {
                return false;
            }
            clear();
            return true;
        }
        boolean modified = false;
        for( int i = collectionArray.length - 1; i >= 0; i-- ) {
            modified |= collectionArray[i].retainAll( collection );
        }
        return modified;
    }


    public void clear()
    {
        for( int i = collectionArray.length - 1; i >= 0; i-- ) {
            collectionArray[i].clear();
        }
    }


    ////////////////////////////////////////
    // Other methods
    ////////////////////////////////////////


    /**
     *  Returns an unmodifiable <code>List</code> of the argument
     *  <code>Collections</code> being used by this
     *  <code>CollectionChain</code>.
     */
    public List getOperands()
    {
        return Collections.unmodifiableList( Arrays.asList( collectionArray ) );
    }

}
