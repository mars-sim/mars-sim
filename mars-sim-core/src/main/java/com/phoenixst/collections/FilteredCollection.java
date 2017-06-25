/*
 *  $Id: FilteredCollection.java,v 1.11 2006/06/07 18:31:16 rconner Exp $
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
 *  A <code>Collection</code> which presents a filtered view of
 *  another.
 *
 *  @version    $Revision: 1.11 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class FilteredCollection extends AbstractCollection
    implements Serializable
{

    private static final long serialVersionUID = 3L;

    /**
     *  The wrapped collection.
     *
     *  @serial
     */
    private final Collection delegate;

    /**
     *  The filtering predicate.
     *
     *  @serial
     */
    private final Predicate predicate;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>FilteredCollection</code>.  If a
     *  <code>null</code> <code>Predicate</code> is passed to this
     *  constructor, {@link TruePredicate} is used internally.
     */
    public FilteredCollection( Collection delegate,
                               Predicate predicate )
    {
        super();
        this.delegate = delegate;
        this.predicate = (predicate != null)
            ? predicate
            : TruePredicate.INSTANCE;
        if( delegate == null ) {
            throw new IllegalArgumentException( "Wrapped Collection is null." );
        }
    }


    ////////////////////////////////////////
    // Serialization methods
    ////////////////////////////////////////


    private void readObject( ObjectInputStream in )
        throws ClassNotFoundException,
               IOException
    {
        in.defaultReadObject();
        if( delegate == null ) {
            throw new InvalidObjectException( "Wrapped Collection is null." );
        }
        if( predicate == null ) {
            throw new InvalidObjectException( "Predicate is null." );
        }
    }


    ////////////////////////////////////////
    // Collection
    ////////////////////////////////////////


    public int size()
    {
        int size = 0;
        for( Iterator i = delegate.iterator(); i.hasNext(); ) {
            if( predicate.evaluate( i.next() ) ) {
                size++;
            }
        }
        return size;
    }


    public boolean isEmpty()
    {
        for( Iterator i = delegate.iterator(); i.hasNext(); ) {
            if( predicate.evaluate( i.next() ) ) {
                return false;
            }
        }
        return true;
    }


    public boolean add( Object object )
    {
        if( !predicate.evaluate( object ) ) {
            throw new IllegalArgumentException( "Element " + object + " does not satisfy predicate " + predicate );
        }
        return delegate.add( object );
    }


    public boolean remove( Object object )
    {
        return predicate.evaluate( object )
            && delegate.remove( object );
    }


    public boolean contains( Object object )
    {
        return predicate.evaluate( object )
            && delegate.contains( object );
    }


    /**
     *  Returns an <code>Iterator</code> over the elements of this
     *  <code>Collection</code>.  If the <code>remove()</code> method
     *  is called after <code>hasNext()</code> without an intervening
     *  call to <code>next()</code>, the last element returned by
     *  <code>next()</code> will be removed by calling {@link
     *  Collection#remove Collection.remove( object )} on the
     *  underlying <code>Collection</code>.  Depending upon the
     *  underlying <code>Collection</code> implementation, this may
     *  invalidate this <code>Iterator</code>.
     */
    public Iterator iterator()
    {
        return new IteratorImpl( delegate, predicate );
    }


    ////////////////////////////////////////
    // Private classes
    ////////////////////////////////////////


    /**
     *  Protected iterator implementation.
     */
    private static class IteratorImpl extends FilteredIterator
    {
        private final Collection delegate;

        IteratorImpl( Collection delegate, Predicate predicate )
        {
            super( delegate.iterator(), predicate );
            this.delegate = delegate;
        }

        protected void remove( Object object )
        {
            delegate.remove( object );
        }
    }

}
