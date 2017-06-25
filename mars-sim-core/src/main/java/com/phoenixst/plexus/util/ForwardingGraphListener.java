/*
 *  $Id: ForwardingGraphListener.java,v 1.3 2006/06/19 23:34:56 rconner Exp $
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

import java.lang.ref.*;

import com.phoenixst.plexus.*;


/**
 *  A <code>GraphListener</code> which forwards events to an {@link
 *  ObservableGraphDelegate}.  Instances of this class only keep a
 *  <code>WeakReference</code> to their delegates.  If that Reference
 *  has been cleared when an event is received, this listener will
 *  remove itself as a listener of the <code>Graph</code> which sent
 *  the event.  Because of this, it is necessary for the
 *  <code>Graph</code> which is using this listener to maintain a
 *  strong reference to the <code>ObservableGraphDelegate</code>.
 *
 *  @version    $Revision: 1.3 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class ForwardingGraphListener
    implements GraphListener
{

    private final Reference<ObservableGraphDelegate> delegateRef;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    public ForwardingGraphListener( ObservableGraphDelegate observableDelegate )
    {
        super();
        delegateRef = new WeakReference( observableDelegate );
    }


    ////////////////////////////////////////
    // Methods for subclasses to call to actually deliver the event
    // or otherwise interact with the ObservableGraphDelegate.
    ////////////////////////////////////////


    /**
     *  Returns <code>true</code> only if the Reference has not been
     *  cleared <em>and</em> the delegate currently has listeners.  If
     *  the Reference has been cleared, then this listener is removed
     *  as a listener from the source of the event before returning
     *  <code>false</code>.  This method should be called by any
     *  public method of this class prior to performing any actual
     *  work.
     */
    protected final boolean checkDelegate( GraphEvent event )
    {
        ObservableGraphDelegate observableDelegate = delegateRef.get();
        if( observableDelegate == null ) {
            ((ObservableGraph) event.getSource()).removeGraphListener( this );
            return false;
        }
        return observableDelegate.hasListeners();
    }


    protected final void fireNodeAdded( Object node )
    {
        ObservableGraphDelegate observableDelegate = delegateRef.get();
        if( observableDelegate == null ) {
            return;
        }
        observableDelegate.fireNodeAdded( node );
    }


    protected final void fireNodeRemoved( Object node )
    {
        ObservableGraphDelegate observableDelegate = delegateRef.get();
        if( observableDelegate == null ) {
            return;
        }
        observableDelegate.fireNodeRemoved( node );
    }


    protected final void fireEdgeAdded( Graph.Edge edge )
    {
        ObservableGraphDelegate observableDelegate = delegateRef.get();
        if( observableDelegate == null ) {
            return;
        }
        observableDelegate.fireEdgeAdded( edge );
    }


    protected final void fireEdgeRemoved( Graph.Edge edge )
    {
        ObservableGraphDelegate observableDelegate = delegateRef.get();
        if( observableDelegate == null ) {
            return;
        }
        observableDelegate.fireEdgeRemoved( edge );
    }


    ////////////////////////////////////////
    // GraphListener methods, can be overridden
    ////////////////////////////////////////


    public void nodeAdded( GraphEvent event )
    {
        if( !checkDelegate( event ) ) {
            return;
        }
        fireNodeAdded( event.getObject() );
    }


    public void nodeRemoved( GraphEvent event )
    {
        if( !checkDelegate( event ) ) {
            return;
        }
        fireNodeRemoved( event.getObject() );
    }


    public void edgeAdded( GraphEvent event )
    {
        if( !checkDelegate( event ) ) {
            return;
        }
        fireEdgeAdded( (Graph.Edge) event.getObject() );
    }


    public void edgeRemoved( GraphEvent event )
    {
        if( !checkDelegate( event ) ) {
            return;
        }
        fireEdgeRemoved( (Graph.Edge) event.getObject() );
    }

}
