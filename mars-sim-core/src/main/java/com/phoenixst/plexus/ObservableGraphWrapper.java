/*
 *  $Id: ObservableGraphWrapper.java,v 1.53 2006/06/07 20:28:29 rconner Exp $
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

import java.util.*;

import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;

import com.phoenixst.plexus.util.*;


/**
 *  A wrapper around a {@link Graph} so that it can be watched for
 *  structural changes.  <strong>Note:</strong> the {@link
 *  Traverser#remove()} method on <code>Traversers</code> created by
 *  this class will only work if the <code>Traversers</code> created
 *  by the wrapped <code>Graph</code> can tolerate having edges
 *  removed while the traversal is in progress.
 *
 *  @version    $Revision: 1.53 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class ObservableGraphWrapper
    implements ObservableGraph
{

    /**
     *  The logger.
     */
    private static final Logger LOGGER = Logger.getLogger( ObservableGraphWrapper.class );


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
     *  Creates a new <code>ObservableGraphWrapper</code>.
     */
    public ObservableGraphWrapper( Graph delegate )
    {
        super();
        initialize( delegate );
    }


    /**
     *  This constructor, together with {@link #initialize(Graph)},
     *  allows a subclass to initialize the internal state during
     *  deserialization.
     */
    protected ObservableGraphWrapper()
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
        observableDelegate = new ObservableGraphDelegate( this, LOGGER );
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
    // Node removal helper method
    ////////////////////////////////////////


    protected void removeIncidentEdges( Object node )
    {
        for( Traverser t = delegate.traverser( node, null ); t.hasNext(); ) {
            t.next();
            Graph.Edge edge = t.getEdge();
            t.removeEdge();
            fireEdgeRemoved( edge );
        }
    }


    ////////////////////////////////////////
    // Event firing methods
    ////////////////////////////////////////


    void fireNodeAdded( Object node )
    {
        observableDelegate.fireNodeAdded( node );
    }


    void fireNodeRemoved( Object node )
    {
        observableDelegate.fireNodeRemoved( node );
    }


    void fireEdgeAdded( Graph.Edge edge )
    {
        observableDelegate.fireEdgeAdded( edge );
    }


    void fireEdgeRemoved( Graph.Edge edge )
    {
        observableDelegate.fireEdgeRemoved( edge );
    }


    ////////////////////////////////////////
    // Graph methods
    ////////////////////////////////////////


    public boolean addNode( Object node )
    {
        checkInit();
        if( !delegate.addNode( node ) ) {
            return false;
        }
        fireNodeAdded( node );
        return true;
    }


    public boolean removeNode( Object node )
    {
        checkInit();
        if( !delegate.containsNode( node ) ) {
            return false;
        }
        removeIncidentEdges( node );
        // Even though we shouldn't have to, check the return value
        // from removeNode() anyway, since someone else may have
        // nuked it and we don't want to send out duplicate events.
        if( delegate.removeNode( node ) ) {
            fireNodeRemoved( node );
        }
        return true;
    }


    public boolean containsNode( Object node )
    {
        checkInit();
        return delegate.containsNode( node );
    }


    public Graph.Edge addEdge( Object object, Object tail, Object head, boolean isDirected )
    {
        checkInit();
        Graph.Edge edge = delegate.addEdge( object, tail, head, isDirected );
        if( edge != null ) {
            fireEdgeAdded( edge );
        }
        return edge;
    }


    public boolean removeEdge( Graph.Edge edge )
    {
        checkInit();
        if( !delegate.removeEdge( edge ) ) {
            return false;
        }
        fireEdgeRemoved( edge );
        return true;
    }


    public boolean containsEdge( Graph.Edge edge )
    {
        checkInit();
        return delegate.containsEdge( edge );
    }


    public int degree( Object node )
    {
        checkInit();
        return delegate.degree( node );
    }


    public int degree( Object node,
                       Predicate traverserPredicate )
    {
        checkInit();
        return delegate.degree( node, traverserPredicate );
    }


    public Collection nodes( Predicate nodePredicate )
    {
        checkInit();
        return new ObservableNodeCollection( delegate.nodes( nodePredicate ) );
    }


    public Collection edges( Predicate edgePredicate )
    {
        checkInit();
        return new ObservableEdgeCollection( delegate.edges( edgePredicate ) );
    }


    public Collection adjacentNodes( Object node,
                                     Predicate traverserPredicate )
    {
        checkInit();
        return new AdjacentNodeCollection( this, node, traverserPredicate );
    }


    public Collection incidentEdges( Object node,
                                     Predicate traverserPredicate )
    {
        checkInit();
        return new ObservableEdgeCollection( delegate.incidentEdges( node,
                                                                  traverserPredicate ) );
    }


    public Object getNode( Predicate nodePredicate )
    {
        checkInit();
        return delegate.getNode( nodePredicate );
    }


    public Graph.Edge getEdge( Predicate edgePredicate )
    {
        checkInit();
        return delegate.getEdge( edgePredicate );
    }


    public Object getAdjacentNode( Object node,
                                   Predicate traverserPredicate )
    {
        checkInit();
        return delegate.getAdjacentNode( node, traverserPredicate );
    }


    public Graph.Edge getIncidentEdge( Object node,
                                       Predicate traverserPredicate )
    {
        checkInit();
        return delegate.getIncidentEdge( node, traverserPredicate );
    }


    public Traverser traverser( Object node,
                                Predicate traverserPredicate )
    {
        checkInit();
        return new ObservableTraverserWrapper( delegate.traverser( node, traverserPredicate ) );
    }


    ////////////////////////////////////////
    // ObservableGraph
    ////////////////////////////////////////


    public void addGraphListener( GraphListener listener )
    {
        checkInit();
        observableDelegate.addGraphListener( listener );
    }


    public void removeGraphListener( GraphListener listener )
    {
        checkInit();
        observableDelegate.removeGraphListener( listener );
    }


    ////////////////////////////////////////
    // Object methods
    ////////////////////////////////////////


    public String toString()
    {
        checkInit();
        return delegate.toString();
    }


    ////////////////////////////////////////
    // Private iterator wrapper classes
    ////////////////////////////////////////


    /**
     *
     */
    private class ObservableNodeIteratorWrapper
        implements Iterator
    {
        final Iterator i;
        private Object currentNode;
        private boolean isCurrentValid = false;

        ObservableNodeIteratorWrapper( Iterator i )
        {
            super();
            this.i = i;
        }

        public boolean hasNext()
        {
            return i.hasNext();
        }

        public Object next()
        {
            currentNode = i.next();
            isCurrentValid = true;
            return currentNode;
        }

        public void remove()
        {
            if( !isCurrentValid ) {
                throw new IllegalStateException();
            }
            removeIncidentEdges( currentNode );
            i.remove();
            fireNodeRemoved( currentNode );
            isCurrentValid = false;
        }
    }


    /**
     *
     */
    private class ObservableEdgeIteratorWrapper
        implements Iterator
    {
        private final Iterator i;
        private Graph.Edge currentEdge;

        ObservableEdgeIteratorWrapper( Iterator i )
        {
            super();
            this.i = i;
        }

        public boolean hasNext()
        {
            return i.hasNext();
        }

        public Object next()
        {
            currentEdge = (Graph.Edge) i.next();
            return currentEdge;
        }

        public void remove()
        {
            i.remove();
            fireEdgeRemoved( currentEdge );
        }
    }


    /**
     *
     */
    private class ObservableTraverserWrapper extends ObservableNodeIteratorWrapper
        implements Traverser
    {
        ObservableTraverserWrapper( Traverser t )
        {
            super( t );
        }

        public Graph.Edge getEdge()
        {
            return ((Traverser) i).getEdge();
        }

        public void removeEdge()
        {
            Graph.Edge edge = ((Traverser) i).getEdge();
            ((Traverser) i).removeEdge();
            fireEdgeRemoved( edge );
        }
    }


    /**
     *
     */
    private class ObservableNodeCollection extends AbstractCollection
    {
        private final Collection nodeCollection;

        ObservableNodeCollection( Collection nodeCollection )
        {
            super();
            this.nodeCollection = nodeCollection;
        }

        public int size()
        {
            return nodeCollection.size();
        }

        public boolean isEmpty()
        {
            return nodeCollection.isEmpty();
        }

        public boolean add( Object object )
        {
            throw new UnsupportedOperationException();
        }

        public boolean remove( Object object )
        {
            if( !nodeCollection.contains( object ) ) {
                return false;
            }
            removeIncidentEdges( object );
            // Even though we shouldn't have to, check the return value
            // from removeNode() anyway, since someone else may have
            // nuked it and we don't want to send out duplicate events.
            if( nodeCollection.remove( object ) ) {
                fireNodeRemoved( object );
            }
            return true;
        }

        public boolean contains( Object object )
        {
            return nodeCollection.contains( object );
        }

        public Iterator iterator()
        {
            return new ObservableNodeIteratorWrapper( nodeCollection.iterator() );
        }

        public boolean containsAll( Collection collection )
        {
            return nodeCollection.containsAll( collection );
        }

        public boolean addAll( Collection collection )
        {
            throw new UnsupportedOperationException();
        }
    }


    /**
     *
     */
    private class ObservableEdgeCollection extends AbstractCollection
    {
        private final Collection edgeCollection;

        ObservableEdgeCollection( Collection edgeCollection )
        {
            super();
            this.edgeCollection = edgeCollection;
        }

        public int size()
        {
            return edgeCollection.size();
        }

        public boolean isEmpty()
        {
            return edgeCollection.isEmpty();
        }

        public boolean add( Object object )
        {
            throw new UnsupportedOperationException();
        }

        public boolean remove( Object object )
        {
            if( !edgeCollection.remove( object ) ) {
                return false;
            }
            fireEdgeRemoved( (Graph.Edge) object );
            return true;
        }

        public boolean contains( Object object )
        {
            return edgeCollection.contains( object );
        }

        public Iterator iterator()
        {
            return new ObservableEdgeIteratorWrapper( edgeCollection.iterator() );
        }

        public boolean containsAll( Collection collection )
        {
            return edgeCollection.containsAll( collection );
        }

        public boolean addAll( Collection collection )
        {
            throw new UnsupportedOperationException();
        }
    }

}
