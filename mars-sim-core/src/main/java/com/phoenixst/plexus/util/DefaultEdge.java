/*
 *  $Id: DefaultEdge.java,v 1.10 2005/10/03 15:20:46 rconner Exp $
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

package com.phoenixst.plexus.util;

import com.phoenixst.plexus.*;


/**
 *  A default {@link com.phoenixst.plexus.Graph.Edge} implementation
 *  in which the user-defined object must be <code>null</code>.  The
 *  {@link #equals(Object) equals()} method is inherited from
 *  <code>Object</code> and uses reference equality.  This class
 *  should only be used by <code>Graphs</code> which create edges once
 *  and store them.
 *
 *  @version    $Revision: 1.10 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class DefaultEdge
    implements Graph.Edge,
               java.io.Serializable
{

    private static final long serialVersionUID = 2L;


    /**
     *  The tail of this <code>Edge</code>.
     *
     *  @serial
     */
    private final Object tail;

    /**
     *  The head of this <code>Edge</code>.
     *
     *  @serial
     */
    private final Object head;

    /**
     *  Whether or not this <code>Edge</code> is directed.
     *
     *  @serial
     */
    private final boolean directed;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>DefaultEdge</code>.
     */
    public DefaultEdge( Object tail, Object head, boolean directed )
    {
        super();
        this.tail = tail;
        this.head = head;
        this.directed = directed;
    }


    ////////////////////////////////////////
    // Edge methods
    ////////////////////////////////////////


    public boolean isDirected()
    {
        return directed;
    }


    public Object getUserObject()
    {
        return null;
    }


    public void setUserObject( Object object )
    {
        throw new UnsupportedOperationException();
    }


    public Object getTail()
    {
        return tail;
    }


    public Object getHead()
    {
        return head;
    }


    public Object getOtherEndpoint( Object node )
    {
        if( GraphUtils.equals( tail, node ) ) {
            return head;
        } else if( GraphUtils.equals( head, node ) ) {
            return tail;
        } else {
            throw new IllegalArgumentException( "Edge is not incident on the node: " + node );
        }
    }


    ////////////////////////////////////////
    // Other methods
    ////////////////////////////////////////


    public String toString()
    {
        return GraphUtils.getTextValue( this, false ).toString();
    }

}
