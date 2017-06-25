/*
 *  $Id: SynchronizedGraph.java,v 1.5 2006/06/20 00:03:43 rconner Exp $
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

import java.io.*;
import java.util.Collection;

import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;

import com.phoenixst.collections.SynchronizedCollection;
import com.phoenixst.plexus.*;


/**
 *  A synchronized view of a <code>Graph</code>.  It is the user's
 *  responsibility to manually synchronize when iterating over the
 *  <code>Graph</code>.  This <code>Graph</code> will be serializable
 *  if the delegate <code>Graph</code> is serializable.  Note that the
 *  {@link com.phoenixst.plexus.Graph.Edge} objects are not wrapped,
 *  and are therefore <strong>not</strong> synchronized.  As with
 *  synchronized collections, iterators are also not synchronized.
 *
 *  @version    $Revision: 1.5 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class SynchronizedGraph
    implements ObservableGraph,
               Serializable
{

    private static final long serialVersionUID = 1L;

    /**
     *  The logger.
     */
    private static final Logger LOGGER = Logger.getLogger( SynchronizedGraph.class );


    /**
     *  The wrapped graph.
     *
     *  @serial
     */
    private final Graph delegate;

    /**
     *  The object upon which to synchronize.
     *
     *  @serial
     */
    private final Object mutex;

    /**
     *  The delegate to handle observable functionality.
     */
    private transient ObservableGraphDelegate observableDelegate;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a synchronized view of the specified
     *  <code>Graph</code>.  It is the user's responsibility to
     *  manually synchronize on the created <code>Graph</code> when
     *  iterating over it.  This <code>Graph</code> will be
     *  serializable if the specified <code>Graph</code> is
     *  serializable.
     *
     *  @param delegate the <code>Graph</code> for which a synchronized
     *  view is to be created.
     */
    public SynchronizedGraph( Graph delegate )
    {
        this.delegate = delegate;
        this.mutex = this;
        if( delegate == null ) {
            throw new IllegalArgumentException( "Delegate Graph is null." );
        }
        initialize();
    }


    /**
     *  Creates a synchronized view of the specified
     *  <code>Graph</code> and synchronized upon the specified object.
     *  It is the user's responsibility to manually synchronize on
     *  <code>mutex</code> when iterating over the <code>Graph</code>.
     *  This <code>Graph</code> will be serializable if the specified
     *  <code>Graph</code> is serializable.
     *
     *  @param delegate the <code>Graph</code> for which a synchronized
     *  view is to be created.
     *
     *  @param mutex the object upon which to synchronize access.
     */
    public SynchronizedGraph( Graph delegate, Object mutex )
    {
        this.delegate = delegate;
        this.mutex = mutex;
        if( delegate == null ) {
            throw new IllegalArgumentException( "Delegate Graph is null." );
        }
        initialize();
    }


    ////////////////////////////////////////
    // Serialization
    ////////////////////////////////////////


    /**
     *  This method should only be called by constructors and readObject().
     */
    private final void initialize()
    {
        if( delegate instanceof ObservableGraph ) {
            observableDelegate = new ObservableGraphDelegate( this, LOGGER );
            GraphListener delegateListener = new ForwardingGraphListener( observableDelegate );
            ((ObservableGraph) delegate).addGraphListener( delegateListener );
        }
    }


    private void readObject( ObjectInputStream in )
        throws ClassNotFoundException,
               IOException
    {
        in.defaultReadObject();
        if( delegate == null ) {
            throw new InvalidObjectException( "Wrapped Graph is null." );
        }
        if( mutex == null ) {
            throw new InvalidObjectException( "Mutex is null." );
        }
        initialize();
    }


    ////////////////////////////////////////
    // Graph
    ////////////////////////////////////////


    public boolean addNode( Object node )
    {
        synchronized( mutex ) {
            return delegate.addNode( node );
        }
    }


    public boolean removeNode( Object node )
    {
        synchronized( mutex ) {
            return delegate.removeNode( node );
        }
    }


    public boolean containsNode( Object node )
    {
        synchronized( mutex ) {
            return delegate.containsNode( node );
        }
    }


    public Graph.Edge addEdge( Object object,
                               Object tail,
                               Object head,
                               boolean isDirected )
    {
        synchronized( mutex ) {
            return delegate.addEdge( object, tail, head, isDirected );
        }
    }


    public boolean removeEdge( Graph.Edge edge )
    {
        synchronized( mutex ) {
            return delegate.removeEdge( edge );
        }
    }


    public boolean containsEdge( Graph.Edge edge )
    {
        synchronized( mutex ) {
            return delegate.containsEdge( edge );
        }
    }


    public int degree( Object node )
    {
        synchronized( mutex ) {
            return delegate.degree( node );
        }
    }


    public int degree( Object node,
                       Predicate traverserPredicate )
    {
        synchronized( mutex ) {
            return delegate.degree( node, traverserPredicate );
        }
    }


    public Object getNode( Predicate nodePredicate )
    {
        synchronized( mutex ) {
            return delegate.getNode( nodePredicate );
        }
    }


    public Graph.Edge getEdge( Predicate edgePredicate )
    {
        synchronized( mutex ) {
            return delegate.getEdge( edgePredicate );
        }
    }


    public Object getAdjacentNode( Object node,
                                   Predicate traverserPredicate )
    {
        synchronized( mutex ) {
            return delegate.getAdjacentNode( node, traverserPredicate );
        }
    }


    public Graph.Edge getIncidentEdge( Object node,
                                       Predicate traverserPredicate )
    {
        synchronized( mutex ) {
            return delegate.getIncidentEdge( node, traverserPredicate );
        }
    }


    public Traverser traverser( Object node,
                                Predicate traverserPredicate )
    {
        return delegate.traverser( node, traverserPredicate );
    }


    ////////////////////////////////////////
    // Collection views must be wrapped
    ////////////////////////////////////////


    public Collection nodes( Predicate nodePredicate )
    {
        synchronized( mutex ) {
            return new SynchronizedCollection( delegate.nodes( nodePredicate ),
                                               mutex );
        }
    }


    public Collection edges( Predicate edgePredicate )
    {
        synchronized( mutex ) {
            return new SynchronizedCollection( delegate.edges( edgePredicate ),
                                               mutex );
        }
    }


    public Collection adjacentNodes( Object node,
                                     Predicate traverserPredicate )
    {
        synchronized( mutex ) {
            return new SynchronizedCollection( delegate.adjacentNodes( node,
                                                                       traverserPredicate ),
                                               mutex );
        }
    }


    public Collection incidentEdges( Object node,
                                     Predicate traverserPredicate )
    {
        synchronized( mutex ) {
            return new SynchronizedCollection( delegate.incidentEdges( node,
                                                                       traverserPredicate ),
                                               mutex );
        }
    }


    ////////////////////////////////////////
    // ObservableGraph
    ////////////////////////////////////////


    public void addGraphListener( GraphListener listener )
    {
        if( observableDelegate == null ) {
            throw new UnsupportedOperationException( "Wrapped graph is not observable." );
        }
        synchronized( mutex ) {
            observableDelegate.addGraphListener( listener );
        }
    }


    public void removeGraphListener( GraphListener listener )
    {
        if( observableDelegate == null ) {
            throw new UnsupportedOperationException( "Wrapped graph is not observable." );
        }
        synchronized( mutex ) {
            observableDelegate.removeGraphListener( listener );
        }
    }


    ////////////////////////////////////////
    // Object methods
    ////////////////////////////////////////


    public String toString()
    {
        synchronized( mutex ) {
            return delegate.toString();
        }
    }

}
