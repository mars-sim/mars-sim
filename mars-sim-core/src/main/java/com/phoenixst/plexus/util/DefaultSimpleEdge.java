/*
 *  $Id: DefaultSimpleEdge.java,v 1.10 2006/06/21 20:21:32 rconner Exp $
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

import com.phoenixst.plexus.*;


/**
 *  A default {@link com.phoenixst.plexus.Graph.Edge} implementation
 *  for a simple graph in which the user-defined object must be
 *  <code>null</code>.  This class should only be used by
 *  <code>Graphs</code> which create edges lazily on demand.
 *  Otherwise, use {@link DefaultEdge} or {@link DefaultObjectEdge}
 *  instead.  Reference equality is <strong>not</strong> used by
 *  {@link #equals(Object) equals( Object )}.
 *
 *  @version    $Revision: 1.10 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class DefaultSimpleEdge extends DefaultEdge
{

    private static final long serialVersionUID = 3L;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>DefaultSimpleEdge</code>.
     */
    public DefaultSimpleEdge( Object tail, Object head, boolean directed )
    {
        super( tail, head, directed );
    }


    ////////////////////////////////////////
    // Equality and hashing
    ////////////////////////////////////////


    public boolean equals( Object object )
    {
        if( this == object ) {
            return true;
        }
        if( !(object instanceof DefaultSimpleEdge) ) {
            return false;
        }
        DefaultSimpleEdge edge = (DefaultSimpleEdge) object;
        return (isDirected() == edge.isDirected())
            && ( ( GraphUtils.equals( getTail(), edge.getTail() )
                   && GraphUtils.equals( getHead(), edge.getHead() ) )
                 || ( !isDirected()
                      && GraphUtils.equals( getTail(), edge.getHead() )
                      && GraphUtils.equals( getHead(), edge.getTail() )) );
    }


    public int hashCode()
    {
        Object tail = getTail();
        Object head = getHead();
        return ((tail == null) ? 0 : tail.hashCode())
            ^ ((head == null) ? 0 : head.hashCode());
    }

}
