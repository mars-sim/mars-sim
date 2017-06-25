/*
 *  $Id: FilteredGraph.java,v 1.44 2006/06/20 00:03:42 rconner Exp $
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

package com.phoenixst.plexus;

import java.util.Collection;

import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;

import com.phoenixst.collections.*;
import com.phoenixst.plexus.util.*;


/**
 *  A filtered <code>Graph</code> implementation.  Unlike other
 *  <code>Graph</code> wrappers and implementations, and in violation
 *  of the contract for {@link Graph.Edge#equals Graph.Edge.equals()},
 *  the <code>Edges</code> produced by this <code>Graph</code> are not
 *  wrapped.
 *
 *  @version    $Revision: 1.44 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class FilteredGraph extends AbstractGraph
    implements ObservableGraph
{

    /**
     *  The logger.
     */
    private static final Logger LOGGER = Logger.getLogger( FilteredGraph.class );


    /**
     *  The wrapped graph.
     */
    private Graph delegate;

    /**
     *  The node predicate.
     */
    private Predicate nodePredicate;

    /**
     *  The edge predicate.
     */
    private Predicate edgePredicate;

    /**
     *  The traverser predicate delegating to the other predicates.
     */
    private Predicate graphTraverserPredicate;

    /**
     *  All the nodes.
     */
    private Collection nodeCollection;

    /**
     *  All the edges.
     */
    private Collection edgeCollection;

    /**
     *  The delegate to handle observable functionality.
     */
    private ObservableGraphDelegate observableDelegate;

    /**
     *  Whether or not this instance has been initialized.
     */
    private boolean isInitialized = false;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>FilteredGraph</code>.
     */
    public FilteredGraph( Graph delegate,
                          Predicate nodePredicate,
                          Predicate edgePredicate )
    {
        super();
        initialize( delegate, nodePredicate, edgePredicate );
    }


    /**
     *  This constructor, together with {@link #initialize(Graph,
     *  Predicate, Predicate)}, allows a subclass to initialize the
     *  internal state during deserialization.
     */
    protected FilteredGraph()
    {
        super();
    }


    ////////////////////////////////////////
    // Serialization and construction assistance methods
    ////////////////////////////////////////


    /**
     *  This method should only be called by subclasses during
     *  deserialization.
     */
    protected final void initialize( Graph delegateGraph,
                                     Predicate nodePred,
                                     Predicate edgePred )
    {
        if( isInitialized ) {
            throw new IllegalStateException( "This instance is already initialized." );
        }
        this.delegate = delegateGraph;
        this.nodePredicate = nodePred;
        this.edgePredicate = edgePred;
        if( delegate == null ) {
            throw new IllegalArgumentException( "Delegate Graph is null." );
        }
        if( nodePredicate == null ) {
            nodePredicate = TruePredicate.INSTANCE;
        }
        if( edgePredicate == null ) {
            edgePredicate = TruePredicate.INSTANCE;
        }

        // Initialize edgeFilter, which will be the actual edge
        // predicate used with the delegate graph.
        Predicate edgeFilter = null;

        if( nodePredicate == TruePredicate.INSTANCE ) {
            // we don't have to check edge endpoints
            edgeFilter = edgePredicate;

        } else if( nodePredicate == FalsePredicate.INSTANCE ) {
            // no nodes means no edges either
            edgeFilter = FalsePredicate.INSTANCE;

        } else {
            if( edgePredicate == TruePredicate.INSTANCE ) {
                edgeFilter = new EdgeEndpointPredicate( nodePredicate );
            } else if( edgePredicate == FalsePredicate.INSTANCE ) {
                edgeFilter = FalsePredicate.INSTANCE;
            } else {
                edgeFilter = new AndPredicate( new EdgeEndpointPredicate( nodePredicate ),
                                               edgePredicate );
            }
        }

        if( edgeFilter == TruePredicate.INSTANCE ) {
            graphTraverserPredicate = TruePredicate.INSTANCE;
        } else if( edgeFilter == FalsePredicate.INSTANCE ) {
            graphTraverserPredicate = FalsePredicate.INSTANCE;
        } else {
            graphTraverserPredicate = new EdgeTraverserPredicateAdapter( edgeFilter );
        }

        nodeCollection = delegate.nodes( nodePredicate );
        edgeCollection = delegate.edges( edgeFilter );

        if( delegate instanceof ObservableGraph ) {
            observableDelegate = new ObservableGraphDelegate( this, LOGGER );
            GraphListener delegateListener = new FilteredGraphListener( nodePredicate,
                                                                        edgePredicate,
                                                                        observableDelegate );
            ((ObservableGraph) delegate).addGraphListener( delegateListener );
        }

        isInitialized = true;
    }


    /**
     *  This method must be called by all public methods of this
     *  class to ensure that any subclass has properly initialized
     *  this instance.
     */
    private void checkInit()
    {
        if( !isInitialized ) {
            throw new IllegalStateException( "This instance is not initialized." );
        }
    }


    ////////////////////////////////////////
    // Accessors to the internal state so that subclasses
    // can manually serialize it.
    ////////////////////////////////////////


    /**
     *  Provides accesss to the internal state so it can be manually
     *  serialized by a subclass's <code>writeObject()</code> method.
     */
    protected final Graph getDelegate()
    {
        return delegate;
    }


    /**
     *  Provides accesss to the internal state so it can be manually
     *  serialized by a subclass's <code>writeObject()</code> method.
     */
    protected final Predicate getEdgePredicate()
    {
        return edgePredicate;
    }


    /**
     *  Provides accesss to the internal state so it can be manually
     *  serialized by a subclass's <code>writeObject()</code> method.
     */
    protected final Predicate getNodePredicate()
    {
        return nodePredicate;
    }


    ////////////////////////////////////////
    // Private methods
    ////////////////////////////////////////


    private void checkNodePresent( Object node )
    {
        if( !nodePredicate.evaluate( node ) ) {
            throw new NoSuchNodeException( "Node is not in this graph: " + node );
        }
    }


    ////////////////////////////////////////
    // AbstractGraph
    ////////////////////////////////////////


    protected Collection nodes()
    {
        checkInit();
        return nodeCollection;
    }


    protected Collection edges()
    {
        checkInit();
        return edgeCollection;
    }


    protected Traverser traverser( Object node )
    {
        checkInit();
        checkNodePresent( node );
        return delegate.traverser( node, graphTraverserPredicate );
    }


    ////////////////////////////////////////
    // Graph
    ////////////////////////////////////////


    public boolean addNode( Object node )
    {
        checkInit();
        if( !nodePredicate.evaluate( node ) ) {
            throw new IllegalArgumentException( "Node is not allowed in this graph: " + node );
        }
        return delegate.addNode( node );
    }


    public boolean removeNode( Object node )
    {
        checkInit();
        return nodeCollection.remove( node );
    }


    public boolean containsNode( Object node )
    {
        checkInit();
        return nodeCollection.contains( node );
    }


    public Graph.Edge addEdge( Object object, Object tail, Object head, boolean isDirected )
    {
        checkInit();
        checkNodePresent( tail );
        checkNodePresent( head );
        Graph.Edge edge = delegate.addEdge( object, tail, head, isDirected );
        if( edge == null ) {
            return null;
        }
        if( !edgePredicate.evaluate( edge ) ) {
            // FIXME - It worked in the underlying graph, but it
            // doesn't pass this filter.  Note that this will fire a
            // couple of events!
            delegate.removeEdge( edge );
            return null;
        }
        return edge;
    }


    public boolean removeEdge( Graph.Edge edge )
    {
        checkInit();
        return edgeCollection.remove( edge );
    }


    public boolean containsEdge( Graph.Edge edge )
    {
        checkInit();
        return edgeCollection.contains( edge );
    }


    ////////////////////////////////////////
    // ObservableGraph
    ////////////////////////////////////////


    /**
     *  Adds the specified <code>GraphListener</code> which will be
     *  notified whenever this <code>ObservableGraph's</code>
     *  structure changes.  If the wrapped graph does not implement
     *  {@link ObservableGraph}, then this method with throw an
     *  <code>UnsupportedOperationException</code>.
     */
    public void addGraphListener( GraphListener listener )
    {
        checkInit();
        if( observableDelegate == null ) {
            throw new UnsupportedOperationException( "Wrapped graph is not observable." );
        }
        observableDelegate.addGraphListener( listener );
    }


    /**
     *  Removes a previously added <code>GraphListener</code>.  If the
     *  wrapped graph does not implement {@link ObservableGraph}, then
     *  this method with throw an
     *  <code>UnsupportedOperationException</code>.
     */
    public void removeGraphListener( GraphListener listener )
    {
        checkInit();
        if( observableDelegate == null ) {
            throw new UnsupportedOperationException( "Wrapped graph is not observable." );
        }
        observableDelegate.removeGraphListener( listener );
    }


    ////////////////////////////////////////
    // Private classes
    ////////////////////////////////////////


    private static class EdgeEndpointPredicate
        implements Predicate,
                   java.io.Serializable
    {
        private static final long serialVersionUID = 1L;

        /**
         *  @serial
         */
        private final Predicate nodePredicate;

        EdgeEndpointPredicate( Predicate nodePredicate )
        {
            super();
            this.nodePredicate = nodePredicate;
        }

        public boolean evaluate( Object object )
        {
            Graph.Edge edge = (Graph.Edge) object;
            return nodePredicate.evaluate( edge.getTail() )
                && nodePredicate.evaluate( edge.getHead() );
        }

        public boolean equals( Object object )
        {
            if( object == this ) {
                return true;
            }
            if( !(object instanceof EdgeEndpointPredicate) ) {
                return false;
            }
            return nodePredicate.equals( ((EdgeEndpointPredicate) object).nodePredicate );
        }

        public int hashCode()
        {
            return nodePredicate.hashCode();
        }
    }


    private static class EdgeTraverserPredicateAdapter
        implements Predicate,
                   java.io.Serializable
    {
        private static final long serialVersionUID = 1L;

        /**
         *  @serial
         */
        private final Predicate edgePredicate;

        EdgeTraverserPredicateAdapter( Predicate edgePredicate )
        {
            super();
            this.edgePredicate = edgePredicate;
        }

        public boolean evaluate( Object object )
        {
            return edgePredicate.evaluate( ((OrderedPair) object).getSecond() );
        }

        public boolean equals( Object object )
        {
            if( object == this ) {
                return true;
            }
            if( !(object instanceof EdgeTraverserPredicateAdapter) ) {
                return false;
            }
            return edgePredicate.equals( ((EdgeTraverserPredicateAdapter) object).edgePredicate );
        }

        public int hashCode()
        {
            return edgePredicate.hashCode();
        }
    }

}
