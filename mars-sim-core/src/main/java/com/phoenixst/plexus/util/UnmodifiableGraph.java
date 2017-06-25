/*
 *  $Id: UnmodifiableGraph.java,v 1.6 2006/06/20 00:03:43 rconner Exp $
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
import java.util.*;

import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;

import com.phoenixst.plexus.*;


/**
 *  An unmodifiable view of a <code>Graph</code>.  This
 *  <code>Graph</code> will be serializable if the delegate
 *  <code>Graph</code> is serializable.  Note that the {@link
 *  com.phoenixst.plexus.Graph.Edge} objects are not wrapped, and
 *  therefore <strong>are</strong> modifiable.
 *
 *  @version    $Revision: 1.6 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class UnmodifiableGraph
    implements ObservableGraph,
               Serializable
{

    private static final long serialVersionUID = 1L;

    /**
     *  The logger.
     */
    private static final Logger LOGGER = Logger.getLogger( UnmodifiableGraph.class );


    /**
     *  The wrapped graph.
     *
     *  @serial
     */
    private final Graph delegate;

    /**
     *  The delegate to handle observable functionality.
     */
    private transient ObservableGraphDelegate observableDelegate;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates an unmodifiable view of the specified
     *  <code>Graph</code>.  This <code>Graph</code> will be
     *  serializable if the specified <code>Graph</code> is
     *  serializable.
     *
     *  @param delegate the <code>Graph</code> for which an unmodifiable
     *  view is to be created.
     */
    public UnmodifiableGraph( Graph delegate )
    {
        this.delegate = delegate;
        if( delegate == null ) {
            throw new IllegalArgumentException( "Delegate Graph is null." );
        }
        initialize();
    }


    ////////////////////////////////////////
    // Serialization methods
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
        initialize();
    }


    ////////////////////////////////////////
    // Modifying operations throw an exception
    ////////////////////////////////////////


    /**
     *  Throws an <code>UnsupportedOperationException</code>.
     */
    public boolean addNode( Object node )
    {
        throw new UnsupportedOperationException();
    }


    /**
     *  Throws an <code>UnsupportedOperationException</code>.
     */
    public boolean removeNode( Object node )
    {
        throw new UnsupportedOperationException();
    }


    /**
     *  Throws an <code>UnsupportedOperationException</code>.
     */
    public Graph.Edge addEdge( Object object, Object tail, Object head, boolean isDirected )
    {
        throw new UnsupportedOperationException();
    }


    /**
     *  Throws an <code>UnsupportedOperationException</code>.
     */
    public boolean removeEdge( Graph.Edge edge )
    {
        throw new UnsupportedOperationException();
    }


    ////////////////////////////////////////
    // Collection views and iterators must be wrapped
    ////////////////////////////////////////


    public Collection nodes( Predicate nodePredicate )
    {
        return Collections.unmodifiableCollection( delegate.nodes( nodePredicate ) );
    }


    public Collection edges( Predicate edgePredicate )
    {
        return Collections.unmodifiableCollection( delegate.edges( edgePredicate ) );
    }


    public Collection adjacentNodes( Object node,
                                     Predicate traverserPredicate )
    {
        return Collections.unmodifiableCollection( delegate.adjacentNodes( node,
                                                                           traverserPredicate ) );
    }


    public Collection incidentEdges( Object node,
                                     Predicate traverserPredicate )
    {
        return Collections.unmodifiableCollection( delegate.incidentEdges( node,
                                                                           traverserPredicate ) );
    }


    public Traverser traverser( Object node,
                                Predicate traverserPredicate )
    {
        return new UnmodifiableTraverser( delegate.traverser( node, traverserPredicate ) );
    }


    ////////////////////////////////////////
    // Most Graph operations just delegate
    ////////////////////////////////////////


    public boolean containsNode( Object node )
    {
        return delegate.containsNode( node );
    }


    public boolean containsEdge( Edge edge )
    {
        return delegate.containsEdge( edge );
    }

    public int degree( Object node )
    {
        return delegate.degree( node );
    }


    public int degree( Object node,
                       Predicate traverserPredicate )
    {
        return delegate.degree( node, traverserPredicate );
    }


    public Object getNode( Predicate nodePredicate )
    {
        return delegate.getNode( nodePredicate );
    }


    public Edge getEdge( Predicate edgePredicate )
    {
        return delegate.getEdge( edgePredicate );
    }


    public Object getAdjacentNode( Object node,
                                   Predicate traverserPredicate )
    {
        return delegate.getAdjacentNode( node, traverserPredicate );
    }


    public Edge getIncidentEdge( Object node,
                                 Predicate traverserPredicate )
    {
        return delegate.getIncidentEdge( node, traverserPredicate );
    }


    ////////////////////////////////////////
    // ObservableGraph
    ////////////////////////////////////////


    public void addGraphListener( GraphListener listener )
    {
        if( observableDelegate == null ) {
            throw new UnsupportedOperationException( "Wrapped graph is not observable." );
        }
        observableDelegate.addGraphListener( listener );
    }


    public void removeGraphListener( GraphListener listener )
    {
        if( observableDelegate == null ) {
            throw new UnsupportedOperationException( "Wrapped graph is not observable." );
        }
        observableDelegate.removeGraphListener( listener );
    }


    ////////////////////////////////////////
    // Object methods
    ////////////////////////////////////////


    public String toString()
    {
        return delegate.toString();
    }

}
