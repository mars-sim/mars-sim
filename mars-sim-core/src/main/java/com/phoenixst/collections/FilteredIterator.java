/*
 *  $Id: FilteredIterator.java,v 1.7 2005/10/03 15:11:54 rconner Exp $
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

import java.util.*;

import org.apache.commons.collections.Predicate;


/**
 *  A filtered <code>Iterator</code>.  Because this class must advance
 *  the underlying <code>Iterator</code> to correctly implement {@link
 *  #hasNext()}, {@link #remove()} has unusual semantics.  See the
 *  javadocs for that method for details.
 *
 *  @version    $Revision: 1.7 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class FilteredIterator
    implements Iterator
{

    private final Iterator delegate;
    private final Predicate predicate;

    private Object current;
    private Object next;

    private boolean isCurrentValid = false;
    private boolean isNextValid = false;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>FilteredIterator</code>.
     */
    public FilteredIterator( Iterator delegate,
                             Predicate predicate )
    {
        super();
        this.delegate = delegate;
        this.predicate = predicate;
    }


    ////////////////////////////////////////
    // Iterator
    ////////////////////////////////////////


    public boolean hasNext()
    {
        if( !isNextValid ) {
            while( delegate.hasNext() ) {
                Object object = delegate.next();
                if( predicate.evaluate( object ) ) {
                    next = object;
                    isNextValid = true;
                    break;
                }
            }
        }
        return isNextValid;
    }


    public Object next()
    {
        if( !hasNext() ) {
            throw new NoSuchElementException();
        }
        current = next;
        isCurrentValid = true;
        isNextValid = false;
        return current;
    }


    /**
     *  Removes from the last object returned by {@link #next}.
     *  Because the underlying <code>Iterator</code> must be advanced
     *  to correctly implement {@link #hasNext}, this method has
     *  unusual semantics.  These are the possible states that an
     *  instance of this class may be in, and how this method will
     *  behave:
     *
     *  <UL>
     *
     *    <LI>If <code>next()</code> has not yet been called or
     *        <code>remove()</code> was called after the last call to
     *        <code>next()</code>, throws an
     *        <code>IllegalStateException</code>.  These are the same
     *        conditions under which any implementation of
     *        <code>Iterator.remove</code> should throw an exception.
     *
     *    <LI>If <code>next()</code> was called after the last calls
     *        to both <code>hasNext()</code> and <code>remove()</code>
     *        (if any), delegates to the <code>remove()</code> method
     *        of the underlying <code>Iterator</code>.
     *
     *    <LI>If <code>hasNext()</code> was called after the last
     *        calls to both <code>next()</code> and
     *        <code>remove()</code> (if any) and <code>remove()</code>
     *        has not been called after the last call to
     *        <code>next()</code>, calls {@link #remove(Object)
     *        remove( Object )} with the element last returned by
     *        <code>next()</code>.  In other words, the underlying
     *        <code>Iterator</code> has been advanced beyond the
     *        element to be removed.
     *
     *  </UL>
     *
     *  <P><b>Description copied from interface: {@link
     *  Iterator}</b><br> {@inheritDoc}
     */
    public void remove()
    {
        if( !isCurrentValid ) {
            throw new IllegalStateException();
        }
        if( !isNextValid ) {
            delegate.remove();
        } else {
            remove( current );
        }
        isCurrentValid = false;
    }


    /**
     *  This method is called by {@link #remove()} if
     *  <code>hasNext()</code> was called after the last calls to both
     *  <code>next()</code> and <code>remove()</code> (if any) and
     *  <code>remove()</code> has not been called after the last call
     *  to <code>next()</code>.  In other words, this method is called
     *  if the call sequence goes something like <code>next(),
     *  hasNext(), remove()</code>, which means the underlying
     *  <code>Iterator</code> has been advanced beyond the element to
     *  be removed.  This provides an extension point for an
     *  implementation to correctly handle this case if it is capable
     *  of doing so.
     *
     *  <P>This implementation throws an
     *  <code>IllegalStateException</code>.
     */
    protected void remove( Object object )
    {
        throw new IllegalStateException( "The remove() method cannot be called after hasNext()"
                                         + " without an intervening call to next()." );
    }

}
