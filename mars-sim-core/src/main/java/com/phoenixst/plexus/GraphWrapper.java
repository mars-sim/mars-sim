/*
 *  $Id: GraphWrapper.java,v 1.69 2006/06/20 00:04:55 rconner Exp $
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

import java.lang.ref.*;
import java.util.Collection;

import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;

import com.phoenixst.collections.*;
import com.phoenixst.plexus.util.*;


/**
 *  A <code>Graph</code> which wraps another.  If the wrapped graph
 *  does not implement {@link ObservableGraph}, then {@link
 *  #addGraphListener} and {@link #removeGraphListener} with throw
 *  <code>UnsupportedOperationExceptions</code>.  This class is
 *  intended to be extended.
 *
 *  @version    $Revision: 1.69 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class GraphWrapper
    implements ObservableGraph
{

    /**
     *  The logger.
     */
    private static final Logger LOGGER = Logger.getLogger( GraphWrapper.class );


    /**
     *  The wrapped graph.
     */
    private Graph delegate;

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
     *  Creates a new <code>GraphWrapper</code>.
     */
    public GraphWrapper( Graph delegate )
    {
        super();
        initialize( delegate );
    }


    /**
     *  This constructor, together with {@link #initialize(Graph)},
     *  allows a subclass to initialize the internal state during
     *  deserialization.
     */
    protected GraphWrapper()
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
    protected final void initialize( Graph delegateGraph )
    {
        if( isInitialized ) {
            throw new IllegalStateException( "This instance is already initialized." );
        }
        this.delegate = delegateGraph;
        if( delegate == null ) {
            throw new IllegalArgumentException( "Delegate Graph is null." );
        }
        if( delegate instanceof ObservableGraph ) {
            observableDelegate = new ObservableGraphDelegate( this, LOGGER );
            GraphListener delegateListener = new DelegateGraphListener( this, observableDelegate );
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


    ////////////////////////////////////////
    // Protected wrap/unwrap methods - to be overridden
    ////////////////////////////////////////


    /**
     *  Returns a wrapped node.
     */
    protected Object wrapNode( Object node )
    {
        return node;
    }


    /**
     *  Returns an unwrapped node.
     */
    protected Object unwrapNode( Object node )
    {
        return node;
    }


    /**
     *  Returns a wrapped edge Object.
     */
    protected Object wrapEdgeObject( Object edgeObject )
    {
        return edgeObject;
    }


    /**
     *  Returns an unwrapped edge Object.
     */
    protected Object unwrapEdgeObject( Object edgeObject )
    {
        return edgeObject;
    }


    /**
     *  Creates a wrapped <code>Graph.Edge</code>.
     */
    protected EdgeWrapper createEdge( Graph.Edge edge )
    {
        return new EdgeWrapper( this, edge );
    }


    /**
     *  Returns a wrapped traverser.
     */
    protected Traverser wrapTraverser( Traverser traverser )
    {
        return new TraverserWrapper( traverser );
    }


    /**
     *  Returns a wrapped node predicate, if necessary.
     */
    protected Predicate wrapNodePredicate( Predicate nodePredicate )
    {
        if( nodePredicate == null
            || nodePredicate == FalsePredicate.INSTANCE
            || nodePredicate == TruePredicate.INSTANCE ) {
            return nodePredicate;
        } else if( nodePredicate instanceof EqualPredicate ) {
            Object wrapperNode = ((EqualPredicate) nodePredicate).getTestObject();
            return new EqualPredicate( toDelegateNode( wrapperNode ) );
        } else {
            return new NodePredicateWrapper( nodePredicate );
        }
    }


    /**
     *  Returns a wrapped edge predicate, if necessary.
     */
    protected Predicate wrapEdgePredicate( Predicate edgePredicate )
    {
        if( edgePredicate == null
            || edgePredicate == FalsePredicate.INSTANCE
            || edgePredicate == TruePredicate.INSTANCE
            || edgePredicate == GraphUtils.DIRECTED_EDGE_PREDICATE
            || edgePredicate == GraphUtils.UNDIRECTED_EDGE_PREDICATE
            || edgePredicate == GraphUtils.SELF_EDGE_PREDICATE ) {
            return edgePredicate;
        } else if( edgePredicate instanceof EqualPredicate ) {
            Graph.Edge wrapperEdge = (Graph.Edge) ((EqualPredicate) edgePredicate).getTestObject();
            return new EqualPredicate( toDelegateEdge( wrapperEdge ) );
        } else if( edgePredicate instanceof EdgePredicate ) {
            EdgePredicate predicate = (EdgePredicate) edgePredicate;
            return EdgePredicateFactory.createPredicated( convertEdgeObjectSpec( predicate.getUserObjectSpecification() ),
                                                          convertNodeSpec( predicate.getFirstNodeSpecification() ),
                                                          convertNodeSpec( predicate.getSecondNodeSpecification() ),
                                                          predicate.getDirectionFlags() );
        } else {
            return new EdgePredicateWrapper( edgePredicate );
        }
    }


    /**
     *  Returns a wrapped traverser predicate, if necessary.
     */
    protected Predicate wrapTraverserPredicate( Predicate traverserPredicate )
    {
        if( traverserPredicate == null
            || traverserPredicate == FalsePredicate.INSTANCE
            || traverserPredicate == TruePredicate.INSTANCE
            || traverserPredicate == GraphUtils.OUT_TRAVERSER_PREDICATE
            || traverserPredicate == GraphUtils.IN_TRAVERSER_PREDICATE
            || traverserPredicate == GraphUtils.DIRECTED_TRAVERSER_PREDICATE
            || traverserPredicate == GraphUtils.UNDIRECTED_TRAVERSER_PREDICATE
            || traverserPredicate == GraphUtils.SELF_TRAVERSER_PREDICATE ) {
            return traverserPredicate;
        } else if( traverserPredicate instanceof EqualsTraverserPredicate ) {
            Graph.Edge wrapperEdge = ((EqualsTraverserPredicate) traverserPredicate).getTestEdge();
            return new EqualsTraverserPredicate( toDelegateEdge( wrapperEdge ) );
        } else if( traverserPredicate instanceof TraverserPredicate ) {
            TraverserPredicate predicate = (TraverserPredicate) traverserPredicate;
            return TraverserPredicateFactory.createPredicated( convertEdgeObjectSpec( predicate.getUserObjectSpecification() ),
                                                               convertNodeSpec( predicate.getNodeSpecification() ),
                                                               predicate.getDirectionFlags() );
        } else {
            return new TraverserPredicateWrapper( traverserPredicate );
        }
    }


    ////////////////////////////////////////
    // Private wrap/unwrap methods
    ////////////////////////////////////////


    /**
     *  Returns a wrapped node.  If the argument node is an
     *  <code>Graph.Edge</code> produced by the wrapped graph, then it
     *  wraps it as an <code>Graph.Edge</code>.
     */
    final Object toWrapperNode( Object node )
    {
        if( node instanceof Graph.Edge ) {
            Graph.Edge edge = (Graph.Edge) node;
            if( delegate.containsEdge( edge ) ) {
                return toWrapperEdge( edge );
            }
        }
        return wrapNode( node );
    }


    /**
     *  Returns an unwrapped node.  If the argument node is an
     *  <code>Graph.Edge</code> produced by this graph, then it
     *  returns the unwrapped <code>Graph.Edge</code>.
     */
    final Object toDelegateNode( Object node )
    {
        if( node instanceof Graph.Edge ) {
            Graph.Edge edge = toDelegateEdge( (Graph.Edge) node );
            if( edge != null ) {
                return edge;
            }
        }
        return unwrapNode( node );
    }


    /**
     *  Returns a wrapped <code>Graph.Edge</code>.
     */
    final EdgeWrapper toWrapperEdge( Graph.Edge edge )
    {
        return (edge != null)
            ? createEdge( edge )
            : null;
    }


    /**
     *  Returns an unwrapped <code>Graph.Edge</code>, or null if the
     *  edge didn't come from this graph.
     */
    final Graph.Edge toDelegateEdge( Graph.Edge edge )
    {
        if( edge instanceof EdgeWrapper ) {
            EdgeWrapper edgeWrapper = (EdgeWrapper) edge;
            if( edgeWrapper.isFromGraph( this ) ) {
                return edgeWrapper.getDelegate();
            }
        }
        return null;
    }


    /**
     *  Converts a node spec for a Edge/TraverserPredicate.
     */
    private Predicate convertNodeSpec( Object object )
    {
        if( object == FalsePredicate.INSTANCE || object == TruePredicate.INSTANCE ) {
            return (Predicate) object;
        } else if( object instanceof EqualPredicate ) {
            Object wrapperNode = ((EqualPredicate) object).getTestObject();
            return new EqualPredicate( toDelegateNode( wrapperNode ) );
        } else if( object instanceof Predicate ) {
            return new NodePredicateWrapper( (Predicate) object );
        } else {
            return new EqualPredicate( toDelegateNode( object ) );
        }
    }


    /**
     *  Converts an edge user-object spec for a
     *  Edge/TraverserPredicate.
     */
    private Predicate convertEdgeObjectSpec( Object object )
    {
        if( object == FalsePredicate.INSTANCE || object == TruePredicate.INSTANCE ) {
            return (Predicate) object;
        } else if( object instanceof EqualPredicate ) {
            Object wrapperEdgeObject = ((EqualPredicate) object).getTestObject();
            return new EqualPredicate( unwrapEdgeObject( wrapperEdgeObject ) );
        } else if( object instanceof Predicate ) {
            return new EdgeObjectPredicateWrapper( (Predicate) object );
        } else {
            return new EqualPredicate( unwrapEdgeObject( object ) );
        }
    }


    ////////////////////////////////////////
    // Graph methods
    ////////////////////////////////////////


    public boolean addNode( Object node )
    {
        checkInit();
        return delegate.addNode( toDelegateNode( node ) );
    }


    public boolean removeNode( Object node )
    {
        checkInit();
        return delegate.removeNode( toDelegateNode( node ) );
    }


    public boolean containsNode( Object node )
    {
        checkInit();
        return delegate.containsNode( toDelegateNode( node ) );
    }


    public Graph.Edge addEdge( Object object,
                               Object tail,
                               Object head,
                               boolean isDirected )
    {
        checkInit();
        return toWrapperEdge( delegate.addEdge( unwrapEdgeObject( object ),
                                                toDelegateNode( tail ),
                                                toDelegateNode( head ),
                                                isDirected ) );
    }


    public boolean removeEdge( Graph.Edge edge )
    {
        checkInit();
        return delegate.removeEdge( toDelegateEdge( edge ) );
    }


    public boolean containsEdge( Graph.Edge edge )
    {
        checkInit();
        return delegate.containsEdge( toDelegateEdge( edge ) );
    }


    public int degree( Object node )
    {
        checkInit();
        return delegate.degree( toDelegateNode( node ) );
    }


    public int degree( Object node,
                       Predicate traverserPredicate )
    {
        checkInit();
        return delegate.degree( toDelegateNode( node ),
                                wrapTraverserPredicate( traverserPredicate ) );
    }


    public Collection nodes( Predicate nodePredicate )
    {
        checkInit();
        return new NodeCollection( delegate.nodes( wrapNodePredicate( nodePredicate ) ) );
    }


    public Collection edges( Predicate edgePredicate )
    {
        checkInit();
        return new EdgeCollection( delegate.edges( wrapEdgePredicate( edgePredicate ) ) );
    }


    public Collection adjacentNodes( Object node,
                                     Predicate traverserPredicate )
    {
        checkInit();
        return new NodeCollection( delegate.adjacentNodes( toDelegateNode( node ),
                                                           wrapTraverserPredicate( traverserPredicate ) ) );
    }


    public Collection incidentEdges( Object node,
                                     Predicate traverserPredicate )
    {
        checkInit();
        return new EdgeCollection( delegate.incidentEdges( toDelegateNode( node ),
                                                           wrapTraverserPredicate( traverserPredicate ) ) );
    }


    /**
     *  This implementation
     */
    public Object getNode( Predicate nodePredicate )
    {
        checkInit();
        return toWrapperNode( delegate.getNode( wrapNodePredicate( nodePredicate ) ) );
    }


    /**
     *  This implementation
     */
    public Graph.Edge getEdge( Predicate edgePredicate )
    {
        checkInit();
        return toWrapperEdge( delegate.getEdge( wrapEdgePredicate( edgePredicate ) ) );
    }


    /**
     *  This implementation returns the other endpoint of the
     *  <code>Graph.Edge</code> returned by {@link
     *  #getIncidentEdge(Object,Predicate)} if present, otherwise it
     *  returns <code>null</code>.
     */
    public Object getAdjacentNode( Object node,
                                   Predicate traverserPredicate )
    {
        checkInit();
        Graph.Edge edge = getIncidentEdge( node, traverserPredicate );
        return (edge != null)
            ? edge.getOtherEndpoint( node )
            : null;
    }


    /**
     *  This implementation
     */
    public Graph.Edge getIncidentEdge( Object node,
                                       Predicate traverserPredicate )
    {
        checkInit();
        return toWrapperEdge( delegate.getIncidentEdge( toDelegateNode( node ),
                                                        wrapTraverserPredicate( traverserPredicate ) ) );
    }


    /**
     *  This implementation
     */
    public Traverser traverser( Object node,
                                Predicate traverserPredicate )
    {
        checkInit();
        return wrapTraverser( delegate.traverser( toDelegateNode( node ),
                                                  wrapTraverserPredicate( traverserPredicate ) ) );
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


    /**
     *  <code>Graph.Edge</code> wrapper.
     */
    private static class EdgeWrapper
        implements Graph.Edge
    {

        /**
         *  The GraphWrapper which created this.
         */
        private final GraphWrapper graphWrapper;

        /**
         *  The wrapped edge.
         */
        private final Graph.Edge delegate;

        // Constructor

        EdgeWrapper( GraphWrapper graphWrapper,
                     Graph.Edge edge )
        {
            super();
            this.graphWrapper = graphWrapper;
            this.delegate = edge;
        }

        // Helper methods

        boolean isFromGraph( GraphWrapper other )
        {
            return graphWrapper == other;
        }

        Graph.Edge getDelegate()
        {
            return delegate;
        }

        // Graph.Edge methods

        public boolean isDirected()
        {
            return delegate.isDirected();
        }

        public Object getUserObject()
        {
            return graphWrapper.wrapEdgeObject( delegate.getUserObject() );
        }

        public void setUserObject( Object object )
        {
            delegate.setUserObject( graphWrapper.unwrapEdgeObject( object ) );
        }

        public Object getTail()
        {
            return graphWrapper.toWrapperNode( delegate.getTail() );
        }

        public Object getHead()
        {
            return graphWrapper.toWrapperNode( delegate.getHead() );
        }

        public Object getOtherEndpoint( Object node )
        {
            return graphWrapper.toWrapperNode( delegate.getOtherEndpoint( graphWrapper.toDelegateNode( node ) ) );
        }

        // Other methods

        public boolean equals( Object object )
        {
            if( this == object ) {
                return true;
            }
            if( !(object instanceof EdgeWrapper) ) {
                return false;
            }
            EdgeWrapper edgeWrapper = (EdgeWrapper) object;
            return edgeWrapper.graphWrapper == graphWrapper
                && delegate.equals( edgeWrapper.delegate );
        }

        public int hashCode()
        {
            return delegate.hashCode();
        }

        public String toString()
        {
            return GraphUtils.getTextValue( this, true ).toString();
        }
    }


    /**
     *  Private node predicate implementation.
     */
    private class NodePredicateWrapper
        implements Predicate
    {
        private final Predicate nodePredicate;

        NodePredicateWrapper( Predicate nodePredicate )
        {
            super();
            this.nodePredicate = nodePredicate;
        }

        public boolean evaluate( Object object )
        {
            return nodePredicate.evaluate( toWrapperNode( object ) );
        }
    }


    /**
     *  Private edge predicate implementation.
     */
    private class EdgePredicateWrapper
        implements Predicate
    {
        private final Predicate edgePredicate;

        EdgePredicateWrapper( Predicate edgePredicate )
        {
            super();
            this.edgePredicate = edgePredicate;
        }

        public boolean evaluate( Object object )
        {
            return edgePredicate.evaluate( toWrapperEdge( (Graph.Edge) object ) );
        }
    }


    /**
     *  Private edge user-object predicate implementation.
     */
    private class EdgeObjectPredicateWrapper
        implements Predicate
    {
        private final Predicate userPredicate;

        EdgeObjectPredicateWrapper( Predicate userPredicate )
        {
            super();
            this.userPredicate = userPredicate;
        }

        public boolean evaluate( Object object )
        {
            return userPredicate.evaluate( wrapEdgeObject( object ) );
        }
    }


    /**
     *  Private traverser predicate implementation.
     */
    private class TraverserPredicateWrapper
        implements Predicate
    {
        private final Predicate traverserPredicate;

        TraverserPredicateWrapper( Predicate traverserPredicate )
        {
            super();
            this.traverserPredicate = traverserPredicate;
        }

        public boolean evaluate( Object object )
        {
            OrderedPair argPair = (OrderedPair) object;
            OrderedPair pair = new OrderedPair( toWrapperNode( argPair.getFirst() ),
                                                toWrapperEdge( (Graph.Edge) argPair.getSecond() ) );
            return traverserPredicate.evaluate( pair );
        }
    }


    /**
     *  Private node collection implementation.
     */
    private class NodeCollection extends CollectionWrapper
    {
        NodeCollection( Collection delegateCollection )
        {
            super( delegateCollection );
        }

        protected Object wrapObject( Object object )
        {
            return toWrapperNode( object );
        }

        protected Object unwrapObject( Object object )
        {
            return toDelegateNode( object );
        }

        public boolean add( Object object )
        {
            throw new UnsupportedOperationException();
        }

        public boolean addAll( Collection collection )
        {
            throw new UnsupportedOperationException();
        }
    }


    /**
     *  Private edge collection implementation.
     */
    private class EdgeCollection extends CollectionWrapper
    {
        EdgeCollection( Collection delegateCollection )
        {
            super( delegateCollection );
        }

        protected Object wrapObject( Object object )
        {
            return toWrapperEdge( (Graph.Edge) object );
        }

        protected Object unwrapObject( Object object )
        {
            return toDelegateEdge( (Graph.Edge) object );
        }

        public boolean add( Object object )
        {
            throw new UnsupportedOperationException();
        }

        public boolean remove( Object object )
        {
            return object instanceof Graph.Edge
                && super.remove( object );
        }

        public boolean contains( Object object )
        {
            return object instanceof Graph.Edge
                && super.contains( object );
        }

        public boolean addAll( Collection collection )
        {
            throw new UnsupportedOperationException();
        }
    }


    /**
     *  Private implementation of Traverser.
     */
    private class TraverserWrapper
        implements Traverser
    {
        private final Traverser t;

        TraverserWrapper( Traverser t )
        {
            super();
            this.t = t;
        }

        public boolean hasNext()
        {
            return t.hasNext();
        }

        public Object next()
        {
            return toWrapperNode( t.next() );
        }

        public void remove()
        {
            t.remove();
        }

        public Graph.Edge getEdge()
        {
            return toWrapperEdge( t.getEdge() );
        }

        public void removeEdge()
        {
            t.removeEdge();
        }
    }


    /**
     *  GraphListener to the wrapped graph.
     */
    private static class DelegateGraphListener extends TransformingGraphListener
    {
        // This has to be a reference to make sure this listener
        // doesn't inadvertantly keep a strong reference to the
        // ObservableGraphDelegate.

        private final Reference<GraphWrapper> graphRef;

        DelegateGraphListener( GraphWrapper wrapper,
                               ObservableGraphDelegate observableDelegate )
        {
            super( observableDelegate );
            graphRef = new WeakReference( wrapper );
        }

        protected Object transformNode( Object node )
        {
            GraphWrapper graphWrapper = graphRef.get();
            if( graphWrapper == null ) {
                // This should never happen
                return null;
            }
            return graphWrapper.toWrapperNode( node );
        }

        protected Graph.Edge transformEdge( Graph.Edge edge )
        {
            GraphWrapper graphWrapper = graphRef.get();
            if( graphWrapper == null ) {
                // This should never happen
                return null;
            }
            return graphWrapper.toWrapperEdge( edge );
        }
    }

}
