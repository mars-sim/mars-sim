/*
 *  $Id: TransformingGraphListener.java,v 1.3 2006/06/19 23:34:56 rconner Exp $
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
 *  A <code>GraphListener</code> which transforms nodes and edges
 *  before forwarding them to an {@link ObservableGraphDelegate}.
 *  Instances of this class only keep a <code>WeakReference</code> to
 *  their delegates.  If that Reference has been cleared when an event
 *  is received, this listener will remove itself as a listener of the
 *  <code>Graph</code> which sent the event.  Because of this, it is
 *  necessary for the <code>Graph</code> which is using this listener
 *  to maintain a strong reference to the
 *  <code>ObservableGraphDelegate</code>.
 *
 *  @version    $Revision: 1.3 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class TransformingGraphListener extends ForwardingGraphListener
{

    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    public TransformingGraphListener( ObservableGraphDelegate observableDelegate )
    {
        super( observableDelegate );
    }


    ////////////////////////////////////////
    // Methods for subclasses to override
    ////////////////////////////////////////


    protected Object transformNode( Object node )
    {
        return node;
    }


    protected Graph.Edge transformEdge( Graph.Edge edge )
    {
        return edge;
    }


    ////////////////////////////////////////
    // GraphListener
    ////////////////////////////////////////


    public void nodeAdded( GraphEvent event )
    {
        if( !checkDelegate( event ) ) {
            return;
        }
        fireNodeAdded( transformNode( event.getObject() ) );
    }


    public void nodeRemoved( GraphEvent event )
    {
        if( !checkDelegate( event ) ) {
            return;
        }
        fireNodeRemoved( transformNode( event.getObject() ) );
    }


    public void edgeAdded( GraphEvent event )
    {
        if( !checkDelegate( event ) ) {
            return;
        }
        fireEdgeAdded( transformEdge( (Graph.Edge) event.getObject() ) );
    }


    public void edgeRemoved( GraphEvent event )
    {
        if( !checkDelegate( event ) ) {
            return;
        }
        fireEdgeRemoved( transformEdge( (Graph.Edge) event.getObject() ) );
    }

}
