/*
 *  $Id: CollectionWrapper.java,v 1.9 2006/06/07 16:33:29 rconner Exp $
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
 *  A <code>Collection</code> which wraps another.  This class is
 *  intended to be extended by overriding the {@link #wrapObject} and
 *  {@link #unwrapObject} methods.
 *
 *  @version    $Revision: 1.9 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class CollectionWrapper extends AbstractCollection
{

    /**
     *  The wrapped collection.
     */
    private Collection delegate;

    /**
     *  Whether or not this instance has been initialized.
     */
    private boolean isInitialized = false;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>CollectionWrapper</code>.
     */
    public CollectionWrapper( Collection delegate )
    {
        this();
        initialize( delegate );
    }


    /**
     *  This constructor, along with {@link #initialize}, allows a
     *  subclass to initialize the internal state during
     *  deserialization.
     */
    protected CollectionWrapper()
    {
        super();
    }


    ////////////////////////////////////////
    // Serialization and construction assistance methods
    ////////////////////////////////////////


    /**
     *  This method should only be called by subclasses during
     *  deserialization.
     */
    protected final void initialize( Collection collection )
    {
        if( isInitialized ) {
            throw new IllegalStateException( "This instance is already initialized." );
        }
        this.delegate = collection;
        if( delegate == null ) {
            throw new IllegalArgumentException( "Delegate Collection is null." );
        }
        isInitialized = true;
    }


    /**
     *  This method must be called by all public methods of this
     *  class to ensure that any subclass has properly initialized
     *  this instance.
     */
    private void checkInit()
        throws IllegalStateException
    {
        if( !isInitialized ) {
            throw new IllegalStateException( "This instance is not initialized." );
        }
    }


    ////////////////////////////////////////
    // Accessors to the internal state so that subclasses
    // can manually serialize it.
    ////////////////////////////////////////


    /**
     *  Provides accesss to the internal state so it can be manually
     *  serialized by a subclass's <code>writeObject()</code> method.
     */
    protected final Collection getDelegate()
    {
        return delegate;
    }


    ////////////////////////////////////////
    // Protected wrap/unwrap methods
    ////////////////////////////////////////


    /**
     *  Returns a wrapped object.  This implementation returns the
     *  argument object.
     */
    protected Object wrapObject( Object object )
    {
        return object;
    }


    /**
     *  Returns an unwrapped object.  This implementation returns the
     *  argument object.
     */
    protected Object unwrapObject( Object object )
    {
        return object;
    }


    ////////////////////////////////////////
    // Collection
    ////////////////////////////////////////


    public int size()
    {
        checkInit();
        return delegate.size();
    }


    public boolean isEmpty()
    {
        checkInit();
        return delegate.isEmpty();
    }


    public void clear()
    {
        checkInit();
        delegate.clear();
    }


    public boolean add( Object object )
    {
        checkInit();
        return delegate.add( unwrapObject( object ) );
    }


    public boolean remove( Object object )
    {
        checkInit();
        return delegate.remove( unwrapObject( object ) );
    }


    public boolean contains( Object object )
    {
        checkInit();
        return delegate.contains( unwrapObject( object ) );
    }


    public Iterator iterator()
    {
        checkInit();
        return new IteratorWrapper( this );
    }


    public boolean addAll( Collection collection )
    {
        checkInit();
        return super.addAll( collection );
    }


    public boolean containsAll( Collection collection )
    {
        checkInit();
        return super.containsAll( collection );
    }


    public boolean removeAll( Collection collection )
    {
        checkInit();
        return super.removeAll( collection );
    }


    public boolean retainAll( Collection collection )
    {
        checkInit();
        return super.retainAll( collection );
    }


    public Object[] toArray()
    {
        checkInit();
        return super.toArray();
    }


    public Object[] toArray( Object[] array )
    {
        checkInit();
        return super.toArray( array );
    }


    public String toString()
    {
        checkInit();
        return super.toString();
    }


    ////////////////////////////////////////
    // Private classes
    ////////////////////////////////////////


    /**
     *  Protected iterator implementation.
     */
    private static class IteratorWrapper
        implements Iterator
    {
        private final CollectionWrapper wrapper;
        private final Iterator i;

        IteratorWrapper( CollectionWrapper wrapper )
        {
            super();
            this.wrapper = wrapper;
            i = wrapper.getDelegate().iterator();
        }

        public boolean hasNext()
        {
            return i.hasNext();
        }

        public Object next()
        {
            return wrapper.wrapObject( i.next() );
        }

        public void remove()
        {
            i.remove();
        }
    }

}
