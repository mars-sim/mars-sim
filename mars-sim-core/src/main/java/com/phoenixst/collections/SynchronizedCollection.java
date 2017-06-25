/*
 *  $Id: SynchronizedCollection.java,v 1.5 2005/10/03 15:11:54 rconner Exp $
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

import java.io.*;
import java.util.*;


/**
 *  A synchronized view of another <code>Collection</code>.
 *
 *  @version    $Revision: 1.5 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class SynchronizedCollection
    implements Collection,
               Serializable
{

    private static final long serialVersionUID = 1L;

    /**
     *  The wrapped collection.
     *
     *  @serial
     */
    private final Collection delegate;

    /**
     *  The object upon which to synchronize.
     *
     *  @serial
     */
    private final Object mutex;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a synchronized view of the specified
     *  <code>Collection</code>.  It is the user's responsibility to
     *  manually synchronize on the created <code>Collection</code>
     *  when iterating over it.  The created <code>Collection</code>
     *  will be serializable if the specified <code>delegate</code> is
     *  serializable.
     *
     *  @param delegate the <code>Collection</code> for which a
     *  synchronized view is to be created.
     */
    public SynchronizedCollection( Collection delegate )
    {
        super();
        this.delegate = delegate;
        this.mutex = this;
        if( delegate == null ) {
            throw new IllegalArgumentException( "Delegate Collection is null." );
        }
    }


    /**
     *  Creates a synchronized view of the specified
     *  <code>Collection</code> and synchronized upon the specified
     *  object.  It is the user's responsibility to manually
     *  synchronize on the created <code>Collection</code> when
     *  iterating over it.  The created <code>Collection</code> will
     *  be serializable if the specified <code>delegate</code> is
     *  serializable.
     *
     *  @param delegate the <code>Collection</code> for which a
     *  synchronized view is to be created.
     */
    public SynchronizedCollection( Collection delegate, Object mutex )
    {
        super();
        this.delegate = delegate;
        this.mutex = mutex;
        if( delegate == null ) {
            throw new IllegalArgumentException( "Delegate Collection is null." );
        }
        if( mutex == null ) {
            throw new IllegalArgumentException( "Mutex is null." );
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
        if( mutex == null ) {
            throw new InvalidObjectException( "Mutex is null." );
        }
    }


    ////////////////////////////////////////
    // Collection
    ////////////////////////////////////////


    public int size()
    {
        synchronized( mutex ) {
            return delegate.size();
        }
    }


    public boolean isEmpty()
    {
        synchronized( mutex ) {
            return delegate.isEmpty();
        }
    }


    public void clear()
    {
        synchronized( mutex ) {
            delegate.clear();
        }
    }


    public boolean add( Object object )
    {
        synchronized( mutex ) {
            return delegate.add( object );
        }
    }


    public boolean remove( Object object )
    {
        synchronized( mutex ) {
            return delegate.remove( object );
        }
    }


    public boolean contains( Object object )
    {
        synchronized( mutex ) {
            return delegate.contains( object );
        }
    }


    public Iterator iterator()
    {
        return delegate.iterator();
    }


    public boolean addAll( Collection collection )
    {
        synchronized( mutex ) {
            return delegate.addAll( collection );
        }
    }


    public boolean containsAll( Collection collection )
    {
        synchronized( mutex ) {
            return delegate.containsAll( collection );
        }
    }


    public boolean removeAll( Collection collection )
    {
        synchronized( mutex ) {
            return delegate.removeAll( collection );
        }
    }


    public boolean retainAll( Collection collection )
    {
        synchronized( mutex ) {
            return delegate.retainAll( collection );
        }
    }


    public Object[] toArray()
    {
        synchronized( mutex ) {
            return delegate.toArray();
        }
    }


    public Object[] toArray( Object[] array )
    {
        synchronized( mutex ) {
            return delegate.toArray( array );
        }
    }


    public String toString()
    {
        synchronized( mutex ) {
            return delegate.toString();
        }
    }

}
