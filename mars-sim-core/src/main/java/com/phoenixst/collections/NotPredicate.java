/*
 *  $Id: NotPredicate.java,v 1.10 2006/06/07 16:33:29 rconner Exp $
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

import org.apache.commons.collections.Predicate;


/**
 *  A <code>Predicate</code> which returns the logical converse of its
 *  operand.
 *
 *  <P>This is mostly equivalent to the class of the same name in
 *  Jakarta Commons-Collections 3.0.  This version exposes its
 *  constructor argument through a public accessor method.  The
 *  deserialization process in this version checks for null fields.
 *  No equivalent exists in version 2.1.
 *
 *  @version    $Revision: 1.10 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public final class NotPredicate
    implements Predicate,
               Serializable
{

    private static final long serialVersionUID = 1L;

    /**
     *  The operand.
     *
     *  @serial
     */
    private final Predicate pred;


    /**
     *  Creates a new <code>NotPredicate</code>.
     */
    public NotPredicate( Predicate pred )
    {
        super();
        this.pred = pred;
        if( pred == null ) {
            throw new IllegalArgumentException( "Predicate is null." );
        }
    }


    private void readObject( ObjectInputStream in )
        throws ClassNotFoundException,
               IOException
    {
        in.defaultReadObject();
        if( pred == null ) {
            throw new InvalidObjectException( "Predicate is null." );
        }
    }


    public boolean evaluate( Object object )
    {
        return !pred.evaluate( object );
    }


    /**
     *  Returns the operand being used by this
     *  <code>NotPredicate</code>.
     */
    public Predicate getOperand()
    {
        return pred;
    }


    public boolean equals( Object object )
    {
        if( object == this ) {
            return true;
        }
        if( !(object instanceof NotPredicate) ) {
            return false;
        }
        return pred.equals( ((NotPredicate) object).pred );
    }


    public int hashCode()
    {
        // 17 & 37 are arbitrary, but non-zero and prime
        return 17 * 37
            + pred.hashCode();
    }


    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append( "(NOT " );
        s.append( pred );
        s.append( ")" );
        return s.toString();
    }

}
