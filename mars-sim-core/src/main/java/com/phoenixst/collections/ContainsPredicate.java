/*
 *  $Id: ContainsPredicate.java,v 1.2 2006/06/07 16:33:29 rconner Exp $
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
import java.util.Collection;

import org.apache.commons.collections.Predicate;


/**
 *  A <code>Predicate</code> which tests whether an object is an
 *  element of a <code>Collection</code>.
 *
 *  @version    $Revision: 1.2 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public final class ContainsPredicate
    implements Predicate,
               Serializable
{

    private static final long serialVersionUID = 1L;

    /**
     *  The collection in which to test for membership.
     *
     *  @serial
     */
    private final Collection collection;


    /**
     *  Creates a new <code>ContainsPredicate</code>.
     *  <strong>Note:</strong> This class does not copy the
     *  constructor argument, so be careful not to alter it after
     *  creating this <code>ContainsPredicate</code>.
     */
    public ContainsPredicate( Collection collection )
    {
        super();
        this.collection = collection;
        if( collection == null ) {
            throw new IllegalArgumentException( "Argument Collection is null." );
        }
    }


    private void readObject( ObjectInputStream in )
        throws ClassNotFoundException,
               IOException
    {
        in.defaultReadObject();
        if( collection == null ) {
            throw new InvalidObjectException( "Collection is null." );
        }
    }


    public boolean evaluate( Object object )
    {
        return collection.contains( object );
    }


    /**
     *  Returns the <code>Collection</code> being used by this
     *  <code>ContainsPredicate</code>.
     */
    public Collection getCollection()
    {
        return collection;
    }


    public boolean equals( Object object )
    {
        if( object == this ) {
            return true;
        }
        if( !(object instanceof ContainsPredicate) ) {
            return false;
        }
        ContainsPredicate pred = (ContainsPredicate) object;
        return collection.equals( pred.collection );
    }


    public int hashCode()
    {
        return collection.hashCode();
    }


    public String toString()
    {
        StringBuilder s = new StringBuilder( getClass().getSimpleName() );
        s.append( collection );
        return s.toString();
    }

}
