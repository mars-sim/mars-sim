/*
 *  $Id: GraphUtils.java,v 1.78 2006/06/19 20:30:12 rconner Exp $
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

import java.io.Serializable;
import java.util.*;

import org.apache.commons.collections.*;
import org.apache.log4j.Logger;

import com.phoenixst.collections.*;
import com.phoenixst.plexus.traversals.GraphStructureIterator;
import com.phoenixst.plexus.util.*;


/**
 *  This class contains static final members and static methods
 *  related to graphs and their iterators.
 *
 *  @version    $Revision: 1.78 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class GraphUtils implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

    /**
     *  The logger.
     */
    private static final Logger LOGGER = Logger.getLogger( GraphUtils.class );


    ////////////////////////////////////////
    // Constants
    ////////////////////////////////////////


    /**
     *
     */
    public static final int UNDIRECTED_MASK = 0x01;


    /**
     *
     */
    public static final int DIRECTED_OUT_MASK = 0x02;


    /**
     *
     */
    public static final int DIRECTED_IN_MASK = 0x04;


    /**
     *
     */
    public static final int DIRECTED_MASK = DIRECTED_OUT_MASK | DIRECTED_IN_MASK;


    /**
     *
     */
    public static final int ANY_DIRECTION_MASK = UNDIRECTED_MASK | DIRECTED_OUT_MASK | DIRECTED_IN_MASK;


    /**
     *  An empty <code>Traverser</code>.
     */
    public static final Traverser EMPTY_TRAVERSER = EmptyTraverser.INSTANCE;


    /**
     *  An immutable, <code>ObservableGraph</code> with no nodes or edges.
     */
    public static final ObservableGraph NULL_GRAPH = NullGraph.INSTANCE;


    /**
     *  An <code>Graph.Edge</code> predicate which is
     *  <code>true</code> when directed.
     */
    public static final Predicate DIRECTED_EDGE_PREDICATE = DirectedEdgePredicate.INSTANCE;


    /**
     *  An <code>Graph.Edge</code> predicate which is
     *  <code>true</code> when undirected.
     */
    public static final Predicate UNDIRECTED_EDGE_PREDICATE = UndirectedEdgePredicate.INSTANCE;


    /**
     *  An <code>Graph.Edge</code> predicate which is
     *  <code>true</code> when the edge is a self-loop.
     */
    public static final Predicate SELF_EDGE_PREDICATE = SelfEdgePredicate.INSTANCE;


    /**
     *  A <code>Traverser</code> predicate which is <code>true</code>
     *  when the edge is directed out.
     */
    public static final Predicate OUT_TRAVERSER_PREDICATE = OutTraverserPredicate.INSTANCE;


    /**
     *  A <code>Traverser</code> predicate which is <code>true</code>
     *  when the edge is directed in.
     */
    public static final Predicate IN_TRAVERSER_PREDICATE = InTraverserPredicate.INSTANCE;


    /**
     *  A <code>Traverser</code> predicate which is <code>true</code> when
     *  the edge is directed.
     */
    public static final Predicate DIRECTED_TRAVERSER_PREDICATE = DirectedTraverserPredicate.INSTANCE;


    /**
     *  A <code>Traverser</code> predicate which is <code>true</code>
     *  when the edge is undirected.
     */
    public static final Predicate UNDIRECTED_TRAVERSER_PREDICATE = UndirectedTraverserPredicate.INSTANCE;


    /**
     *  A <code>Traverser</code> predicate which is <code>true</code>
     *  when the edge is a self-loop.
     */
    public static final Predicate SELF_TRAVERSER_PREDICATE = SelfTraverserPredicate.INSTANCE;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Prevent instantiation.
     */
    private GraphUtils()
    {
        super();
    }


    ////////////////////////////////////////
    // Direction flag inversion method and flag toString() methods
    ////////////////////////////////////////


    private static final int[] inverseDirs = new int[]
        { 0, 1, 4, 5, 2, 3, 6, 7 };


    private static final String[] flagStrings = new String[]
        { "none",
          "-",
          ">",
          "- >",
          "<",
          "< -",
          "< >",
          "any" };


    /**
     *  Returns the inverse of the specified direction flags.
     */
    public static final int invertDirection( int directionFlags )
    {
        return inverseDirs[ directionFlags ];
    }


    /**
     *  Returns a String representation of the specified direction flags.
     *  <UL>
     *    <LI>no flags: <pre>&quot;none&quot;</pre>
     *    <LI>all flags: <pre>&quot;any&quot;</pre>
     *    <LI>undirected: <pre>&quot;-&quot;</pre>
     *    <LI>directed out: <pre>&quot;&gt;&quot;</pre>
     *    <LI>directed in: <pre>&quot;&lt;&quot;</pre>
     *    <LI>directed: <pre>&quot;&lt; &gt;&quot;</pre>
     *    <LI>undirected or directed out: <pre>&quot;- &gt;&quot;</pre>
     *    <LI>undirected or directed in: <pre>&quot;&lt; -&quot;</pre>
     *  </UL>
     */
    public static final String directionFlagsToString( int directionFlags )
    {
        return flagStrings[ directionFlags ];
    }


    ////////////////////////////////////////
    // Graph.Edge toString() methods
    ////////////////////////////////////////


    /**
     *  Returns a CharSequence representing the specified edge.
     */
    public static CharSequence getTextValue( Graph.Edge edge,
                                             boolean includeUserObject )
    {
        StringBuilder s = new StringBuilder();
        s.append( "(" );
        s.append( edge.getTail() );
        if( includeUserObject ) {
            s.append( ") -- (" );
            s.append( edge.getUserObject() );
        }
        s.append( edge.isDirected() ? ") -> (" : ") -- (" );
        s.append( edge.getHead() );
        s.append( ")" );
        return s;
    }


    ////////////////////////////////////////
    // Add Method
    ////////////////////////////////////////


    /**
     *  Adds all the nodes and edges from <code>source</code> to
     *  <code>destination</code>.  If the two <code>Graphs</code> are
     *  incompatible in some way (nodes and/or edges from from the
     *  source graph not allowed in the destination), then a best
     *  effort is still made.
     */
    public static void add( Graph destination, Graph source )
    {
        // Maps source edges to destination edges, if they are
        // also nodes.
        Map edgeMap = new HashMap();

        for( GraphStructureIterator i = new GraphStructureIterator( source ); i.hasNext(); ) {
            Object object = i.next();

            // Process Edge

            if( i.isEdge() ) {
                Graph.Edge edge = (Graph.Edge) object;
                if( LOGGER.isDebugEnabled() ) {
                    LOGGER.debug( "Processing source edge: " + edge );
                }

                Object tail = edge.getTail();
                if( edgeMap.containsKey( tail ) ) {
                    tail = edgeMap.get( tail );
                }
                if( !destination.containsNode( tail ) ) {
                    LOGGER.debug( "  Tail is not present in destination, skipping Edge" );
                    continue;
                }

                Object head = edge.getHead();
                if( edgeMap.containsKey( head ) ) {
                    head = edgeMap.get( head );
                }
                if( !destination.containsNode( head ) ) {
                    LOGGER.debug( "  Head is not present in destination, skipping Edge" );
                    continue;
                }

                Graph.Edge newEdge = null;
                try {
                    newEdge = destination.addEdge( edge.getUserObject(),
                                                   tail, head,
                                                   edge.isDirected() );
                } catch( Exception e ) {
                    LOGGER.debug( "  Could not add Edge", e );
                    continue;
                }
                if( newEdge == null ) {
                    LOGGER.debug( "  Duplicate Edge" );
                    continue;
                }
                if( i.isNode() ) {
                    LOGGER.debug( "  Adding Edge as a node" );
                    edgeMap.put( edge, newEdge );
                    try {
                        if( !destination.addNode( newEdge ) ) {
                            LOGGER.debug( "  Duplicate node" );
                        }
                    } catch( Exception e ) {
                        LOGGER.debug( "  Could not add node", e );
                    }
                }

            // Process Node

            } else if( i.isNode() ) {
                if( LOGGER.isDebugEnabled() ) {
                    LOGGER.debug( "Processing source node: " + object );
                }
                try {
                    if( !destination.addNode( object ) ) {
                        LOGGER.debug( "  Duplicate node" );
                    }
                } catch( Exception e ) {
                    LOGGER.debug( "  Could not add node", e );
                }

            } else {
                LOGGER.error( "GraphStructureIterator is broken, neither a node nor an edge: " + object );
            }
        }
    }


    ////////////////////////////////////////
    // Equals testing helper method
    ////////////////////////////////////////


    /**
     *  Tests two objects for being <code>.equals()</code>, handling
     *  <code>null</code> appropriately.
     */
    public static final boolean equals( Object a, Object b )
    {
        return (a == null) ? (b == null) : a.equals( b );
    }


    ////////////////////////////////////////
    // Least common ancestor helper methods
    ////////////////////////////////////////


    /**
     *  Helper method for <code>OrientedForest</code> implementations.
     */
    public static Object getLeastCommonAncestor( OrientedForest forest,
                                                 Object a, Object b )
    {
        return getFirstCommonNode( new ParentEdgeGetter( forest ), a, b );
    }


    /**
     *  Helper method for getFirstCommonNode().
     */
    private static List getWalk( Object node,
                                 Transformer incidentEdgeGetter )
    {
        List list = new ArrayList();
        list.add( node );
        Graph.Edge edge = (Graph.Edge) incidentEdgeGetter.transform( node );
        while( edge != null ) {
            node = edge.getOtherEndpoint( node );
            list.add( node );
            edge = (Graph.Edge) incidentEdgeGetter.transform( node );
        }
        list.add( null );
        return list;
    }


    /**
     *  Helper method primarily for <code>OrientedForest</code>
     *  implementations.  This method will repeatedly apply the
     *  <code>Transformer</code> to each node argument until
     *  it returns <code>null</code>, so the traversal should
     *  terminate.
     */
    public static Object getFirstCommonNode( Transformer incidentEdgeGetter,
                                             Object a, Object b )
    {
        List aList = getWalk( a, incidentEdgeGetter );
        List bList = getWalk( b, incidentEdgeGetter );

        // aList and bList contain the paths from a and b, with the
        // node at index 0, the last node second from the end, and
        // a null at the very end.  The additional null is to more
        // elegantly handle the case where the paths have no common
        // nodes (the case where this method should return null).

        int aIndex = aList.size() - 1;
        int bIndex = bList.size() - 1;
        while( --aIndex >= 0 && --bIndex >= 0 ) {
            if( !GraphUtils.equals( aList.get( aIndex ), bList.get( bIndex ) ) ) {
                // Entries at aIndex and bIndex don't match, least
                // common ancestor is the previously accessed element.
                return aList.get( aIndex + 1 );
            }
        }

        // Entries matched all the down to the beginning of one of the
        // lists.
        return aIndex < 0
            ? aList.get( 0 )
            : bList.get( 0 );
    }


    ////////////////////////////////////////
    // Singleton Methods
    ////////////////////////////////////////


    /**
     *  Returns an unmodifiable, serializable <code>Graph</code> with
     *  the single specified node and no edges.
     *
     *  @param node the node which the returned <code>Graph</code> is
     *  to contain.
     *
     *  @return an unmodifiable, serializable <code>Graph</code> with
     *  the single specified node and no edges.
     */
    public static Graph singletonGraph( Object node )
    {
        return new SingletonGraph( node );
    }


    /**
     *  Returns a modifiable <code>Iterator</code> over the specified
     *  edge.
     *
     *  @param graph the graph containing the edge over which the
     *  returned <code>Iterator</code> iterates.
     *
     *  @param edge the edge over which the returned
     *  <code>Iterator</code> iterates.
     *
     *  @return a modifiable <code>Iterator</code> over the specified
     *  edge.
     */
    public static Iterator singletonEdgeIterator( Graph graph, Graph.Edge edge )
    {
        return new SingletonEdgeIterator( graph, edge );
    }


    /**
     *  Returns a modifiable <code>Traverser</code> over the specified
     *  edge.
     *
     *  @param graph the graph containing the edge over which the
     *  returned <code>Traverser</code> iterates.
     *
     *  @param endpoint the endpoint of the edge over which the
     *  returned <code>Traverser</code> iterates.
     *
     *  @param edge the edge over which the returned
     *  <code>Traverser</code> iterates.
     *
     *  @return a modifiable <code>Traverser</code> over the specified
     *  edge.
     */
    public static Traverser singletonTraverser( Graph graph, Object endpoint, Graph.Edge edge )
    {
        return new SingletonTraverser( graph, endpoint, edge );
    }


    ////////////////////////////////////////
    // Wrapper Methods
    ////////////////////////////////////////


    /**
     *  Returns an unmodifiable view of the specified
     *  <code>Iterator</code>.
     *
     *  @param iterator the <code>Iterator</code> for which an
     *  unmodifiable view is to be returned.
     *
     *  @return an unmodifiable view of the specified
     *  <code>Iterator</code>.
     */
    public static Iterator unmodifiableIterator( Iterator iterator )
    {
        return new UnmodifiableIterator( iterator );
    }


    /**
     *  Returns an unmodifiable view of the specified
     *  <code>Traverser</code>.
     *
     *  @param traverser the <code>Traverser</code> for which an
     *  unmodifiable view is to be returned.
     *
     *  @return an unmodifiable view of the specified
     *  <code>Traverser</code>.
     */
    public static Traverser unmodifiableTraverser( Traverser traverser )
    {
        return new UnmodifiableTraverser( traverser );
    }


    /**
     *  Returns an unmodifiable view of the specified
     *  <code>Graph</code>.  If the specified <code>Graph</code> does
     *  not implement {@link ObservableGraph}, then {@link
     *  ObservableGraph#addGraphListener} and {@link
     *  ObservableGraph#removeGraphListener} with throw
     *  <code>UnsupportedOperationExceptions</code>.  The returned
     *  <code>Graph</code> will be serializable if the specified
     *  <code>Graph</code> is serializable.
     *
     *  @param graph the <code>Graph</code> for which an unmodifiable
     *  view is to be returned.
     *
     *  @return an unmodifiable view of the specified
     *  <code>Graph</code>.
     */
    public static ObservableGraph unmodifiableGraph( Graph graph )
    {
        return new UnmodifiableGraph( graph );
    }


    /**
     *  Returns a synchronized view of the specified
     *  <code>Graph</code>.  It is the user's responsibility to
     *  manually synchronize on the returned <code>Graph</code> when
     *  iterating over it.  If the specified <code>Graph</code> does
     *  not implement {@link ObservableGraph}, then {@link
     *  ObservableGraph#addGraphListener} and {@link
     *  ObservableGraph#removeGraphListener} with throw
     *  <code>UnsupportedOperationExceptions</code>.  The returned
     *  <code>Graph</code> will be serializable if the specified
     *  <code>Graph</code> is serializable.
     *
     *  @param graph the <code>Graph</code> for which a synchronized
     *  view is to be returned.
     *
     *  @return a synchronized view of the specified
     *  <code>Graph</code>.
     */
    public static ObservableGraph synchronizedGraph( Graph graph )
    {
        return new SynchronizedGraph( graph );
    }


    ////////////////////////////////////////
    // Private Empty Traverser Implementation
    ////////////////////////////////////////


    private static final class EmptyTraverser
        implements Traverser
    {
        static final Traverser INSTANCE = new EmptyTraverser();

        private EmptyTraverser()
        {
            super();
        }

        public boolean hasNext() { return false; }
        public Object next() { throw new NoSuchElementException(); }
        public void remove() { throw new IllegalStateException(); }
        public Graph.Edge getEdge() { throw new IllegalStateException(); }
        public void removeEdge() { throw new IllegalStateException(); }
    }


    ////////////////////////////////////////
    // Private Null Graph Implementation
    ////////////////////////////////////////


    private static final class NullGraph
        implements ObservableGraph,
                   java.io.Serializable
    {
        private static final long serialVersionUID = 1L;

        static final ObservableGraph INSTANCE = new NullGraph();

        private NullGraph()
        {
            super();
        }

        private Object readResolve()
        {
            return INSTANCE;
        }

        public boolean addNode( Object node )
        {
            throw new UnsupportedOperationException();
        }

        public boolean removeNode( Object node )
        {
            throw new UnsupportedOperationException();
        }

        public boolean containsNode( Object node )
        {
            return false;
        }

        public Edge addEdge( Object object, Object tail, Object head, boolean isDirected )
        {
            throw new UnsupportedOperationException();
        }

        public boolean removeEdge( Edge edge )
        {
            throw new UnsupportedOperationException();
        }

        public boolean containsEdge( Edge edge )
        {
            return false;
        }

        public int degree( Object node, Predicate traverserPredicate )
        {
            throw new NoSuchNodeException( "Node is not in this graph: " + node );
        }

        public int degree( Object node )
        {
            throw new NoSuchNodeException( "Node is not in this graph: " + node );
        }

        public Collection nodes( Predicate nodePredicate )
        {
            return Collections.EMPTY_SET;
        }

        public Collection edges( Predicate edgePredicate )
        {
            return Collections.EMPTY_SET;
        }

        public Collection adjacentNodes( Object node, Predicate traverserPredicate )
        {
            throw new NoSuchNodeException( "Node is not in this graph: " + node );
        }

        public Collection incidentEdges( Object node, Predicate traverserPredicate )
        {
            throw new NoSuchNodeException( "Node is not in this graph: " + node );
        }

        public Object getNode( Predicate nodePredicate )
        {
            return null;
        }

        public Edge getEdge( Predicate edgePredicate )
        {
            return null;
        }

        public Object getAdjacentNode( Object node, Predicate traverserPredicate )
        {
            throw new NoSuchNodeException( "Node is not in this graph: " + node );
        }

        public Edge getIncidentEdge( Object node, Predicate traverserPredicate )
        {
            throw new NoSuchNodeException( "Node is not in this graph: " + node );
        }

        public Traverser traverser( Object node, Predicate traverserPredicate )
        {
            throw new NoSuchNodeException( "Node is not in this graph: " + node );
        }

        public void addGraphListener( GraphListener listener )
        {
            // Do nothing
        }

        public void removeGraphListener( GraphListener listener )
        {
            // Do nothing
        }

        public String toString()
        {
            return "NULL_GRAPH";
        }
    }


    ////////////////////////////////////////
    // Private Predicate Implementations
    ////////////////////////////////////////


    private static final class DirectedEdgePredicate
        implements Predicate,
                   java.io.Serializable
    {
        private static final long serialVersionUID = 1L;

        static final DirectedEdgePredicate INSTANCE = new DirectedEdgePredicate();

        private DirectedEdgePredicate()
        {
            super();
        }

        private Object readResolve()
        {
            return INSTANCE;
        }

        public boolean evaluate( Object object )
        {
            return ((Graph.Edge) object).isDirected();
        }

        public String toString()
        {
            return "DIRECTED_EDGE_PREDICATE";
        }
    }


    private static final class UndirectedEdgePredicate
        implements Predicate,
                   java.io.Serializable
    {
        private static final long serialVersionUID = 1L;

        static final UndirectedEdgePredicate INSTANCE = new UndirectedEdgePredicate();

        private UndirectedEdgePredicate()
        {
            super();
        }

        private Object readResolve()
        {
            return INSTANCE;
        }

        public boolean evaluate( Object object )
        {
            return !((Graph.Edge) object).isDirected();
        }

        public String toString()
        {
            return "UNDIRECTED_EDGE_PREDICATE";
        }
    }


    private static final class SelfEdgePredicate
        implements Predicate,
                   java.io.Serializable
    {
        private static final long serialVersionUID = 1L;

        static final SelfEdgePredicate INSTANCE = new SelfEdgePredicate();

        private SelfEdgePredicate()
        {
            super();
        }

        private Object readResolve()
        {
            return INSTANCE;
        }

        public boolean evaluate( Object object )
        {
            Graph.Edge edge = (Graph.Edge) object;
            Object tail = edge.getTail();
            Object head = edge.getHead();
            return (tail == null) ? (head == null) : tail.equals( head );
        }

        public String toString()
        {
            return "SELF_EDGE_PREDICATE";
        }
    }


    private static final class OutTraverserPredicate
        implements Predicate,
                   java.io.Serializable
    {
        private static final long serialVersionUID = 1L;

        static final OutTraverserPredicate INSTANCE = new OutTraverserPredicate();

        private OutTraverserPredicate()
        {
            super();
        }

        private Object readResolve()
        {
            return INSTANCE;
        }

        public boolean evaluate( Object object )
        {
            OrderedPair pair = (OrderedPair) object;
            Object baseNode = pair.getFirst();
            Graph.Edge edge = (Graph.Edge) pair.getSecond();
            Object tail = edge.getTail();
            return edge.isDirected()
                && ((baseNode == null) ? (tail == null) : baseNode.equals( tail ));
        }

        public String toString()
        {
            return "OUT_TRAVERSER_PREDICATE";
        }
    }


    private static final class InTraverserPredicate
        implements Predicate,
                   java.io.Serializable
    {
        private static final long serialVersionUID = 1L;

        static final InTraverserPredicate INSTANCE = new InTraverserPredicate();

        private InTraverserPredicate()
        {
            super();
        }

        private Object readResolve()
        {
            return INSTANCE;
        }

        public boolean evaluate( Object object )
        {
            OrderedPair pair = (OrderedPair) object;
            Object baseNode = pair.getFirst();
            Graph.Edge edge = (Graph.Edge) pair.getSecond();
            Object head = edge.getHead();
            return edge.isDirected()
                && ((baseNode == null) ? (head == null) : baseNode.equals( head ));
        }

        public String toString()
        {
            return "IN_TRAVERSER_PREDICATE";
        }
    }


    private static final class DirectedTraverserPredicate
        implements Predicate,
                   java.io.Serializable
    {
        private static final long serialVersionUID = 1L;

        static final DirectedTraverserPredicate INSTANCE = new DirectedTraverserPredicate();

        private DirectedTraverserPredicate()
        {
            super();
        }

        private Object readResolve()
        {
            return INSTANCE;
        }

        public boolean evaluate( Object object )
        {
            OrderedPair pair = (OrderedPair) object;
            Graph.Edge edge = (Graph.Edge) pair.getSecond();
            return edge.isDirected();
        }

        public String toString()
        {
            return "DIRECTED_TRAVERSER_PREDICATE";
        }
    }


    private static final class UndirectedTraverserPredicate
        implements Predicate,
                   java.io.Serializable
    {
        private static final long serialVersionUID = 1L;

        static final UndirectedTraverserPredicate INSTANCE = new UndirectedTraverserPredicate();

        private UndirectedTraverserPredicate()
        {
            super();
        }

        private Object readResolve()
        {
            return INSTANCE;
        }

        public boolean evaluate( Object object )
        {
            OrderedPair pair = (OrderedPair) object;
            Graph.Edge edge = (Graph.Edge) pair.getSecond();
            return !edge.isDirected();
        }

        public String toString()
        {
            return "UNDIRECTED_TRAVERSER_PREDICATE";
        }
    }


    private static final class SelfTraverserPredicate
        implements Predicate,
                   java.io.Serializable
    {
        private static final long serialVersionUID = 1L;

        static final SelfTraverserPredicate INSTANCE = new SelfTraverserPredicate();

        private SelfTraverserPredicate()
        {
            super();
        }

        private Object readResolve()
        {
            return INSTANCE;
        }

        public boolean evaluate( Object object )
        {
            OrderedPair pair = (OrderedPair) object;
            Graph.Edge edge = (Graph.Edge) pair.getSecond();
            Object tail = edge.getTail();
            Object head = edge.getHead();
            return (tail == null) ? (head == null) : tail.equals( head );
        }

        public String toString()
        {
            return "SELF_TRAVERSER_PREDICATE";
        }
    }

}
