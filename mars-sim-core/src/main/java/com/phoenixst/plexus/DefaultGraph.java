/*
 *  $Id: DefaultGraph.java,v 1.117 2007/04/01 20:16:27 rconner Exp $
 *
 *  Copyright (C) 1994-2007 by Phoenix Software Technologists,
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

import java.io.*;
import java.util.*;

import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;

import com.phoenixst.collections.*;
import com.phoenixst.plexus.util.*;


/**
 *  A default implementation of the {@link ObservableGraph} interface.
 *
 *  <h3>Design Criteria</h3>
 *
 *  <P>There are many ways of representing graphs in software, and
 *  this package uses just one of those for the general
 *  implementation.  Of the two most basic, adjacency list and
 *  adjacency matrix, adjacency list is the most efficient for even
 *  remotely sparse graphs.  Also, the design constraint that nodes
 *  and edges are user-provided objects would have made an adjacency
 *  matrix representation much more costly in terms of space (a
 *  <code>HashMap</code> would be required to map nodes to indices).
 *
 *  <P>In general, it seems preferable to implement a graph as a
 *  <code>HashMap</code> from nodes to their adjacency lists.  The
 *  alternative (using some non-O(1) access Collection) is just too
 *  prohibitive in time for the modest gains in space.  The design
 *  constraints (allowing both directed and undirected edges and edges
 *  having identity) really don't leave a lot of room for
 *  implementations largely different from the one found here.  An
 *  adjacency list is pretty much just a list of Edges, with some
 *  extra bookkeeping information.  Only if a single adjacency list
 *  could grow very large would it be worthwhile to implement it as
 *  something other than a simple list.
 *
 *  @version    $Revision: 1.117 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class DefaultGraph
    implements ObservableGraph,
               Serializable
{

    ////////////////////////////////////////
    // Constants and enums
    ////////////////////////////////////////

    private static final long serialVersionUID = 2L;

    /**
     *  The normal logger.
     */
    static final Logger LOGGER = Logger.getLogger( DefaultGraph.class );

    /**
     *  The event logger, just in case the user wants to just log those
     *  at a different level.
     */
    private static final Logger EVENT_LOGGER =
        Logger.getLogger( DefaultGraph.class.getName() + ".Event" );


    /**
     *  The possible types of the parts of an Edge/TraverserPredicate.
     */
    private enum PredicateSpec { ANY, EQUALS_PREDICATE, PREDICATE, OBJECT }


    static final Cursor EMPTY_CURSOR = new Cursor()
        {
            public boolean hasNext() { return false; }
            public Object next() { throw new NoSuchElementException(); }
            public void remove() { throw new IllegalStateException(); }
            public AdjacencyList getAdjacencyList() { throw new IllegalStateException(); }
            public Graph.Edge getCurrentEdge() { throw new IllegalStateException(); }
            public Object getOtherNode() { throw new IllegalStateException(); }
            public void removeOtherNode() { throw new IllegalStateException(); }
            public void edgeRemoved( int index ) {}
        };


    static final CursorFilter FALSE_CURSOR_FILTER = new CursorFilter()
        {
            public boolean evaluate( Object baseNode, Graph.Edge edge )
            { return false; }
        };


    private static final CursorFilter DIRECTED_CURSOR_FILTER = new CursorFilter()
        {
            public boolean evaluate( Object baseNode, Graph.Edge edge )
            { return edge.isDirected(); }
        };


    private static final CursorFilter UNDIRECTED_CURSOR_FILTER = new CursorFilter()
        {
            public boolean evaluate( Object baseNode, Graph.Edge edge )
            { return !edge.isDirected(); }
        };


    static final CursorFilter BASE_TAIL_CURSOR_FILTER = new CursorFilter()
        {
            public boolean evaluate( Object baseNode, Graph.Edge edge )
            { return GraphUtils.equals( baseNode, edge.getTail() ); }
        };


    private static final CursorFilter BASE_TAIL_DIRECTED_CURSOR_FILTER = new CursorFilter()
        {
            public boolean evaluate( Object baseNode, Graph.Edge edge )
            { return edge.isDirected() && GraphUtils.equals( baseNode, edge.getTail() ); }
        };


    private static final CursorFilter BASE_HEAD_DIRECTED_CURSOR_FILTER = new CursorFilter()
        {
            public boolean evaluate( Object baseNode, Graph.Edge edge )
            { return edge.isDirected() && GraphUtils.equals( baseNode, edge.getHead() ); }
        };


    private static final CursorFilter BASE_TAIL_UNDIRECTED_CURSOR_FILTER = new CursorFilter()
        {
            public boolean evaluate( Object baseNode, Graph.Edge edge )
            { return !edge.isDirected() && GraphUtils.equals( baseNode, edge.getTail() ); }
        };


    private static final CursorFilter SELF_CURSOR_FILTER = new CursorFilter()
        {
            public boolean evaluate( Object baseNode, Graph.Edge edge )
            { return GraphUtils.equals( edge.getTail(), edge.getHead() ); }
        };


    ////////////////////////////////////////
    // Instance fields
    ////////////////////////////////////////


    /**
     *  Map from nodes to adjacency lists.
     */
    transient Map nodeMap;

    /**
     *  The number of edges in this graph.
     */
    transient int edgeSize = 0;

    /**
     *  A lazy collection of all the nodes backed by the nodeMap.
     */
    private transient final Collection nodeCollection = new AllNodesCollection();

    /**
     *  A lazy collection of all the edges backed by the nodeMap.
     */
    private transient final Collection edgeCollection = new AllEdgesCollection();

    /**
     *  The delegate to handle observable functionality.
     */
    private transient ObservableGraphDelegate observableDelegate;

    /**
     *  A String representing this instance for logging purposes.
     */
    transient final String instanceString = "(" + System.identityHashCode( this ) + ")";

    /**
     *  The reapable collection of currently iterating cursors.
     */
    transient final Collection cursors = new ReapableCollection();


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>DefaultGraph</code>.
     */
    public DefaultGraph()
    {
        this( 16 );
    }


    /**
     *  Creates a new <code>DefaultGraph</code> which is a copy of the
     *  specified <code>Graph</code>.
     */
    public DefaultGraph( Graph graph )
    {
        this( graph.nodes( null ).size() );
        GraphUtils.add( this, graph );
    }


    /**
     *  Creates a new <code>DefaultGraph</code> with a capacity for
     *  the specified number of nodes (avoiding unnecessary rehashing).
     */
    protected DefaultGraph( int nodeSize )
    {
        super();
        observableDelegate = new ObservableGraphDelegate( this, EVENT_LOGGER );
        nodeMap = new HashMap( nodeSize );
    }


    ////////////////////////////////////////
    // Serialization methods
    ////////////////////////////////////////


    /**
     *  Serialize this <code>DefaultGraph</code>.
     *
     *  @serialData the number of nodes (int), all the nodes, the
     *  number of edges (int), all the edges.
     */
    private void writeObject( ObjectOutputStream out )
        throws IOException
    {
        if( LOGGER.isInfoEnabled() ) {
            LOGGER.info( "Serializing " + instanceString );
        }
        out.defaultWriteObject();
        out.writeInt( nodeMap.size() );
        for( Iterator i = nodeMap.keySet().iterator(); i.hasNext(); ) {
            out.writeObject( i.next() );
        }
        out.writeInt( edgeSize );
        for( Iterator i = edgeCollection.iterator(); i.hasNext(); ) {
            out.writeObject( i.next() );
        }
    }


    /**
     *  Deserialize this <code>DefaultGraph</code>.
     *
     *  @serialData the number of nodes (int), all the nodes, the
     *  number of edges (int), all the edges.
     */
    private void readObject( ObjectInputStream in )
        throws ClassNotFoundException,
               IOException
    {
        in.defaultReadObject();
        observableDelegate = new ObservableGraphDelegate( this, EVENT_LOGGER );

        int nodeSize = in.readInt();
        if( nodeSize < 0 ) {
            throw new InvalidObjectException( "Node size is less than 0: " + nodeSize );
        }
        nodeMap = new HashMap( nodeSize );
        if( LOGGER.isInfoEnabled() ) {
            LOGGER.info( "Deserializing " + instanceString );
        }

        for( int i = 0; i < nodeSize; i++ ) {
            Object node = in.readObject();
            if( nodeMap.containsKey( node ) ) {
                throw new InvalidObjectException( "Duplicate node: " + node );
            }
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + " deserialization: Adding node " + node );
            }
            nodeMap.put( node, new AdjacencyList( node ) );
        }

        edgeSize = in.readInt();
        if( edgeSize < 0 ) {
            throw new InvalidObjectException( "Edge size is less than 0: " + edgeSize );
        }
        for( int i = 0; i < edgeSize; i++ ) {
            Graph.Edge edge = (Graph.Edge) in.readObject();

            AdjacencyList tailAdj = (AdjacencyList) nodeMap.get( edge.getTail() );
            if( tailAdj == null ) {
                throw new InvalidObjectException( "Graph.Edge tail is not a node: " + edge.getTail() );
            }
            if( tailAdj.contains( edge ) ) {
                throw new InvalidObjectException( "Duplicate edge: " + edge );
            }
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + " deserialization: Adding edge " + edge );
            }
            tailAdj.edges.add( edge );

            if( !GraphUtils.equals( edge.getTail(), edge.getHead() ) ) {
                AdjacencyList headAdj = (AdjacencyList) nodeMap.get( edge.getHead() );
                if( headAdj == null ) {
                    throw new InvalidObjectException( "Graph.Edge head is not a node: " + edge.getHead() );
                }
                headAdj.edges.add( edge );
            }
        }
    }


    ////////////////////////////////////////
    // Protected Graph.Edge creation method - which can be overridden
    // by subclasses.
    ////////////////////////////////////////


    /**
     *  Creates a new <code>Graph.Edge</code>.  This method can be
     *  overridden by subclasses to provide a different implementation
     *  than this one, which produces a {@link DefaultObjectEdge}.
     *  This method should simply create the requested
     *  <code>Graph.Edge</code>, without checking to see whether it
     *  already exists.  <code>DefaultGraph</code> will not allow
     *  two edges which are <code>.equals()</code> in the same
     *  adjacency list.
     */
    protected Graph.Edge createEdge( Object object,
                                     Object tail,
                                     Object head,
                                     boolean isDirected,
                                     Object edgeState )
    {
        return new DefaultObjectEdge( object, tail, head, isDirected );
    }


    /**
     *  Adds a new <code>Graph.Edge</code> with additional information
     *  provided by the <code>edgeState</code> argument, which is
     *  given to the {@link #createEdge createEdge()} method.  This
     *  method is intended to be called by subclasses which require
     *  more information than just the object, tail, head, and
     *  direction to construct the edge.  This method cannot be
     *  overridden.
     *
     *  <P>Returns the newly created <code>Graph.Edge</code> if this
     *  <code>Graph</code> changed as a result of the call.  Returns
     *  <code>null</code> if this <code>Graph</code> does not allow
     *  duplicate edges and already contains the specified edge.
     */
    protected final Graph.Edge addEdge( Object object,
                                        Object tail,
                                        Object head,
                                        boolean isDirected,
                                        Object edgeState )
    {
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( instanceString + ".addEdge( "
                          + object + ", "
                          + tail + ", "
                          + head + ", "
                          + isDirected + " )" );
        }
        AdjacencyList tailAdj = checkNode( tail );
        AdjacencyList headAdj = checkNode( head );
        Graph.Edge edge = createEdge( object,
                                      tailAdj.node, headAdj.node,
                                      isDirected,
                                      edgeState );
        edge = tailAdj.addTo( edge, headAdj );
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "  " + instanceString + ".addEdge() returning " + edge );
        }
        return edge;
    }


    ////////////////////////////////////////
    // Protected notification methods - which can be overridden by
    // subclasses.
    ////////////////////////////////////////


    /**
     *  Invoked before a node has been added to this
     *  <code>Graph</code> and any {@link GraphListener
     *  GraphListeners} have been notified.
     */
    protected void nodeAdding( Object node )
    {
        // Do nothing
    }


    /**
     *  Invoked after a node has been added to this <code>Graph</code>
     *  and any {@link GraphListener GraphListeners} have been
     *  notified.
     */
    protected void nodeAdded( Object node )
    {
        // Do nothing
    }


    /**
     *  Invoked before a node has been removed from this
     *  <code>Graph</code> and any {@link GraphListener
     *  GraphListeners} have been notified.
     */
    protected void nodeRemoving( Object node )
    {
        // Do nothing
    }


    /**
     *  Invoked after a node has been removed from this
     *  <code>Graph</code> and any {@link GraphListener
     *  GraphListeners} have been notified.
     */
    protected void nodeRemoved( Object node )
    {
        // Do nothing
    }


    /**
     *  Invoked before an edge has been added to this
     *  <code>Graph</code> and any {@link GraphListener
     *  GraphListeners} have been notified.
     */
    protected void edgeAdding( Graph.Edge edge )
    {
        // Do nothing
    }


    /**
     *  Invoked after an edge has been added to this
     *  <code>Graph</code> and any {@link GraphListener
     *  GraphListeners} have been notified.
     */
    protected void edgeAdded( Graph.Edge edge )
    {
        // Do nothing
    }


    /**
     *  Invoked before an edge has been removed from this
     *  <code>Graph</code> and any {@link GraphListener
     *  GraphListeners} have been notified.
     */
    protected void edgeRemoving( Graph.Edge edge )
    {
        // Do nothing
    }


    /**
     *  Invoked after an edge has been removed from this
     *  <code>Graph</code> and any {@link GraphListener
     *  GraphListeners} have been notified.
     */
    protected void edgeRemoved( Graph.Edge edge )
    {
        // Do nothing
    }


    ////////////////////////////////////////
    // Package private notification methods
    ////////////////////////////////////////


    /**
     *  Invoked when an edge has been added to alert any listening
     *  Cursors.
     */
    void alertEdgeAdded( AdjacencyList adj )
    {
        // Do nothing - for now anyway
    }


    /**
     *  Invoked when an edge has been removed to alert any listening
     *  Cursors.
     */
    void alertEdgeRemoved( AdjacencyList adj, int index )
    {
        synchronized( cursors ) {
            for( Iterator i = cursors.iterator(); i.hasNext(); ) {
                Cursor cursor = (Cursor) i.next();
                if( adj == cursor.getAdjacencyList() ) {
                    cursor.edgeRemoved( index );
                }
            }
        }
    }


    /**
     *
     */
    void processNodeAdded( Object node )
    {
        observableDelegate.fireNodeAdded( node );
    }


    /**
     *
     */
    void processNodeRemoved( Object node )
    {
        observableDelegate.fireNodeRemoved( node );
    }


    /**
     *
     */
    void processEdgeAdded( Graph.Edge edge )
    {
        edgeSize++;
        observableDelegate.fireEdgeAdded( edge );
    }


    /**
     *
     */
    void processEdgeRemoved( Graph.Edge edge )
    {
        edgeSize--;
        observableDelegate.fireEdgeRemoved( edge );
    }


    ////////////////////////////////////////
    // Private debug toString method
    ////////////////////////////////////////


    /**
     *  A debugging toString method, showing the internal data
     *  structures.
     */
    private String debugToString()
    {
        StringBuilder s = new StringBuilder();
        String className = getClass().getName();
        s.append( className.substring( className.lastIndexOf( '.' ) + 1 ) );
        s.append( instanceString );
        s.append( " ( " );
        s.append( nodeCollection.size() );
        s.append( " nodes, " );
        s.append( edgeCollection.size() );
        s.append( " edges" );
        s.append( " ): " );

        s.append( "\n[" );
        for( Iterator i = nodeMap.entrySet().iterator(); i.hasNext(); ) {
            s.append( "\n  " );
            s.append( i.next() );
        }
        s.append( "\n]" );
        return s.toString();
    }


    ////////////////////////////////////////
    // Private checkNode method
    ////////////////////////////////////////


    /**
     *  Checks to see whether <code>node</code> is in this
     *  <code>Graph</code>.  If so, return the adjacency list for the
     *  node.  If not, throw an <code>NoSuchNodeException</code>.
     *
     *  @param node the node to be checked.
     *
     *  @throws NoSuchNodeException if the node is not in this
     *  <code>Graph</code>.
     */
    private AdjacencyList checkNode( Object node )
    {
        AdjacencyList adj = (AdjacencyList) nodeMap.get( node );
        if( adj == null ) {
            throw new NoSuchNodeException( "Node is not in this graph: " + node );
        }
        return adj;
    }


    ////////////////////////////////////////
    // Private Predicate/Cursor handling methods
    ////////////////////////////////////////


    /**
     *  Returns a constant representing the type of a part of an
     *  Edge/TraverserPredicate.
     */
    private static PredicateSpec getSpecType( Object object )
    {
        if( object == TruePredicate.INSTANCE ) {
            return PredicateSpec.ANY;
        } else if( object instanceof EqualPredicate ) {
            return PredicateSpec.EQUALS_PREDICATE;
        } else if( object instanceof Predicate ) {
            return PredicateSpec.PREDICATE;
        } else {
            return PredicateSpec.OBJECT;
        }
    }


    /**
     *  Converts the specified object to predicate, if necessary.
     *  This is typically used for the specifications of the
     *  user-defined object in a Graph.Edge that is in an
     *  Edge/TraverserPredicate.
     */
    private static Predicate toPredicate( Object object )
    {
        return (object instanceof Predicate)
            ? (Predicate) object
            : new EqualPredicate( object );
    }


    /**
     *  Creates a CursorFilter for the specified arguments, which are
     *  parts of an Edge/TraverserPredicate.
     */
    private CursorFilter createCursorFilter( Object node,
                                             Object userObject,
                                             int directionFlags )
    {
        Predicate userPredicate = toPredicate( userObject );

        PredicateSpec nodeType = getSpecType( node );
        if( nodeType == PredicateSpec.EQUALS_PREDICATE ) {
            node = ((EqualPredicate) node).getTestObject();
        }
        // node is now ANY, PRED, or other (OBJECT)

        if( nodeType == PredicateSpec.ANY ) {
            return new ToAnyCursorFilter( directionFlags,
                                          userPredicate );
        } else if( nodeType == PredicateSpec.PREDICATE ) {
            return new ToPredCursorFilter( directionFlags,
                                           (Predicate) node,
                                           userPredicate );
        } else {
            // Get the "real" node from the graph
            AdjacencyList adj = (AdjacencyList) nodeMap.get( node );
            if( adj == null ) {
                return FALSE_CURSOR_FILTER;
            }
            return new ToEqualsCursorFilter( directionFlags,
                                             adj.node,
                                             userPredicate );
        }
    }


    /**
     *  Creates a CursorFilter for the specified Traverser Predicate.
     *  A null return value signifies that the predicate was null or
     *  TruePredicate.INSTANCE.
     */
    private CursorFilter createCursorFilter( Predicate traverserPredicate )
    {
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "  " + instanceString + ".createCursorFilter( " + traverserPredicate + " )" );
        }
        if( traverserPredicate == null || traverserPredicate == TruePredicate.INSTANCE ) {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "    " + instanceString + ".createCursorFilter() returning null (true) filter" );
            }
            return null;

        } else if( traverserPredicate == FalsePredicate.INSTANCE ) {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "    " + instanceString + ".createCursorFilter() returning false filter" );
            }
            return FALSE_CURSOR_FILTER;

        } else if( traverserPredicate == GraphUtils.OUT_TRAVERSER_PREDICATE ) {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "    " + instanceString + ".createCursorFilter() returning directed out filter" );
            }
            return BASE_TAIL_DIRECTED_CURSOR_FILTER;

        } else if( traverserPredicate == GraphUtils.IN_TRAVERSER_PREDICATE ) {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "    " + instanceString + ".createCursorFilter() returning directed in filter" );
            }
            return BASE_HEAD_DIRECTED_CURSOR_FILTER;

        } else if( traverserPredicate == GraphUtils.DIRECTED_TRAVERSER_PREDICATE ) {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "    " + instanceString + ".createCursorFilter() returning directed filter" );
            }
            return DIRECTED_CURSOR_FILTER;

        } else if( traverserPredicate == GraphUtils.UNDIRECTED_TRAVERSER_PREDICATE ) {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "    " + instanceString + ".createCursorFilter() returning undirected filter" );
            }
            return UNDIRECTED_CURSOR_FILTER;

        } else if( traverserPredicate == GraphUtils.SELF_TRAVERSER_PREDICATE ) {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "    " + instanceString + ".createCursorFilter() returning self filter" );
            }
            return SELF_CURSOR_FILTER;

        } else if( traverserPredicate instanceof TraverserPredicate ) {
            TraverserPredicate predicate = (TraverserPredicate) traverserPredicate;
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "    " + instanceString + ".createCursorFilter() returning optimized filter" );
            }
            return createCursorFilter( predicate.getNodeSpecification(),
                                       predicate.getUserObjectSpecification(),
                                       predicate.getDirectionFlags() );

        } else if( traverserPredicate instanceof EqualsTraverserPredicate ) {
            Graph.Edge edge = ((EqualsTraverserPredicate) traverserPredicate).getTestEdge();
            AdjacencyList tailAdj = (AdjacencyList) nodeMap.get( edge.getTail() );
            AdjacencyList headAdj = (AdjacencyList) nodeMap.get( edge.getHead() );
            if( tailAdj == null || headAdj == null ) {
                if( LOGGER.isDebugEnabled() ) {
                    LOGGER.debug( "    " + instanceString + ".createCursorFilter() returning false filter" );
                }
                return FALSE_CURSOR_FILTER;
            }
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "    " + instanceString + ".createCursorFilter() returning optimized filter" );
            }
            return new EqualsCursorFilter( edge );

        } else {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "    " + instanceString + ".createCursorFilter() returning general filter" );
            }
            return new GeneralTraverserCursorFilter( traverserPredicate );
        }
    }


    ////////////////////////////////////////
    // Graph methods
    ////////////////////////////////////////


    public boolean addNode( Object node )
    {
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( instanceString + ".addNode( " + node + " )" );
        }
        if( nodeMap.containsKey( node ) ) {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".addNode() returning false" );
            }
            return false;
        }
        nodeAdding( node );
        nodeMap.put( node, new AdjacencyList( node ) );
        processNodeAdded( node );
        nodeAdded( node );
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "  " + instanceString + ".addNode() returning true" );
        }
        return true;
    }


    public boolean removeNode( Object node )
    {
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( instanceString + ".removeNode( " + node + " )" );
        }
        AdjacencyList adj = (AdjacencyList) nodeMap.get( node );
        if( adj == null ) {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".removeNode() returning false" );
            }
            return false;
        }
        nodeRemoving( node );
        adj.clear();
        nodeMap.remove( node );
        processNodeRemoved( node );
        nodeRemoved( adj.node );
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "  " + instanceString + ".removeNode() returning true" );
        }
        return true;
    }


    public boolean containsNode( Object node )
    {
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( instanceString + ".containsNode( " + node + " )" );
        }
        boolean contains = nodeMap.containsKey( node );
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "  " + instanceString + ".containsNode() returning " + contains );
        }
        return contains;
    }


    public Graph.Edge addEdge( Object object,
                               Object tail,
                               Object head,
                               boolean isDirected )
    {
        return addEdge( object, tail, head, isDirected, null );
    }


    public boolean removeEdge( Graph.Edge edge )
    {
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( instanceString + ".removeEdge( " + edge + " )" );
        }
        AdjacencyList tailAdj = (AdjacencyList) nodeMap.get( edge.getTail() );
        boolean modified = (tailAdj != null && tailAdj.remove( edge ));
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "  " + instanceString + ".removeEdge() returning " + modified );
        }
        return modified;
    }


    public boolean containsEdge( Graph.Edge edge )
    {
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( instanceString + ".containsEdge( " + edge + " )" );
        }
        AdjacencyList tailAdj = (AdjacencyList) nodeMap.get( edge.getTail() );
        boolean contains = (tailAdj != null && tailAdj.contains( edge ));
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "  " + instanceString + ".containsEdge() returning " + contains );
        }
        return contains;
    }


    public int degree( Object node )
    {
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( instanceString + ".degree( " + node + " )" );
        }
        int degree = checkNode( node ).degree();
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "  " + instanceString + ".degree() returning " + degree );
        }
        return degree;
    }


    public int degree( Object node,
                       Predicate traverserPredicate )
    {
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( instanceString + ".degree( "
                          + node + ", "
                          + traverserPredicate + " )" );
        }
        AdjacencyList adj = checkNode( node );
        int degree;

        if( traverserPredicate == null || traverserPredicate == TruePredicate.INSTANCE ) {
            degree = adj.size();

        } else if( traverserPredicate == FalsePredicate.INSTANCE ) {
            degree = 0;

        } else if( traverserPredicate == GraphUtils.OUT_TRAVERSER_PREDICATE ) {
            degree = adj.outDegree();

        } else if( traverserPredicate == GraphUtils.IN_TRAVERSER_PREDICATE ) {
            degree = adj.inDegree();

        } else {
            // The general case, also catches:
            //   DIRECTED_TRAVERSER_PREDICATE
            //   UNDIRECTED_TRAVERSER_PREDICATE
            //   SELF_TRAVERSER_PREDICATE
            //   EqualsTraverserPredicate
            //   TraverserPredicate (not worth the trouble to analyze)
            degree = adj.degree( traverserPredicate );
        }

        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "  " + instanceString + ".degree() returning " + degree );
        }
        return degree;
    }


    public Collection nodes( Predicate nodePredicate )
    {
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( instanceString + ".nodes( " + nodePredicate + " )" );
        }

        if( nodePredicate == null || nodePredicate == TruePredicate.INSTANCE ) {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".nodes() returning all nodes" );
            }
            return nodeCollection;

        } else if( nodePredicate == FalsePredicate.INSTANCE ) {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".nodes() returning empty set" );
            }
            return Collections.EMPTY_SET;

        } else if( nodePredicate instanceof EqualPredicate ) {
            Object testNode = ((EqualPredicate) nodePredicate).getTestObject();
            if( !containsNode( testNode ) ) {
                if( LOGGER.isDebugEnabled() ) {
                    LOGGER.debug( "  " + instanceString + ".nodes() returning empty set" );
                }
                return Collections.EMPTY_SET;
            }
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".nodes() returning singleton" );
            }
            return new SingletonNodeCollection( this, testNode );

        } else {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".nodes() returning general filtered collection" );
            }
            return new FilteredCollection( nodeCollection, nodePredicate );
        }
    }


    /**
     *  A special-case edge collection.
     */
    private Collection edgesHelper( EdgePredicate edgePredicate )
    {
        Object nodeA = edgePredicate.getFirstNodeSpecification();
        Object nodeB = edgePredicate.getSecondNodeSpecification();
        Object userObject = edgePredicate.getUserObjectSpecification();
        int directionFlags = edgePredicate.getDirectionFlags();

        // Check for either node being fixed

        PredicateSpec nodeAType = getSpecType( nodeA );
        if( nodeAType == PredicateSpec.EQUALS_PREDICATE || nodeAType == PredicateSpec.OBJECT ) {
            if( nodeAType == PredicateSpec.EQUALS_PREDICATE ) {
                nodeA = ((EqualPredicate) nodeA).getTestObject();
            }
            AdjacencyList adj = (AdjacencyList) nodeMap.get( nodeA );
            if( adj != null ) {
                CursorFilter filter = createCursorFilter( nodeB,
                                                          userObject,
                                                          directionFlags );
                if( filter != FALSE_CURSOR_FILTER ) {
                    if( LOGGER.isDebugEnabled() ) {
                        LOGGER.debug( "  " + instanceString + ".edges() returning some edges incident to " + adj.node );
                    }
                    return new IncEdgeCollection( adj, filter );
                }
            }
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges() returning empty set" );
            }
            return Collections.EMPTY_SET;
        }

        PredicateSpec nodeBType = getSpecType( nodeB );
        if( nodeBType == PredicateSpec.EQUALS_PREDICATE || nodeBType == PredicateSpec.OBJECT ) {
            if( nodeBType == PredicateSpec.EQUALS_PREDICATE ) {
                nodeB = ((EqualPredicate) nodeB).getTestObject();
            }
            AdjacencyList adj = (AdjacencyList) nodeMap.get( nodeB );
            if( adj != null ) {
                CursorFilter filter = createCursorFilter( nodeA,
                                                          userObject,
                                                          GraphUtils.invertDirection( directionFlags ) );
                if( filter != FALSE_CURSOR_FILTER ) {
                    if( LOGGER.isDebugEnabled() ) {
                        LOGGER.debug( "  " + instanceString + ".edges() returning some edges incident to " + adj.node );
                    }
                    return new IncEdgeCollection( adj, filter );
                }
            }
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges() returning empty set" );
            }
            return Collections.EMPTY_SET;
        }

        // Convert userObject to a Predicate
        Predicate userPredicate = toPredicate( userObject );

        // If we reach this point, both nodes are either of PredicateSpec.ANY
        // or PredicateSpec.PREDICATE

        if( nodeAType == PredicateSpec.ANY && nodeBType == PredicateSpec.ANY ) {
            if( userPredicate == TruePredicate.INSTANCE ) {
                if( (directionFlags & GraphUtils.UNDIRECTED_MASK) == 0 ) {
                    if( LOGGER.isDebugEnabled() ) {
                        LOGGER.debug( "  " + instanceString + ".edges() returning all directed edges" );
                    }
                    return new AnyToAnyEdgeCollection( GraphUtils.DIRECTED_EDGE_PREDICATE,
                                                       BASE_TAIL_DIRECTED_CURSOR_FILTER );
                } else if( (directionFlags & GraphUtils.DIRECTED_MASK) == 0 ) {
                    if( LOGGER.isDebugEnabled() ) {
                        LOGGER.debug( "  " + instanceString + ".edges() returning all undirected edges" );
                    }
                    return new AnyToAnyEdgeCollection( GraphUtils.UNDIRECTED_EDGE_PREDICATE,
                                                       BASE_TAIL_UNDIRECTED_CURSOR_FILTER );
                } else {
                    if( LOGGER.isDebugEnabled() ) {
                        LOGGER.debug( "  " + instanceString + ".edges() returning all edges" );
                    }
                    return edgeCollection;
                }
            }
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges() returning general filtered collection" );
            }
            return new AnyToAnyEdgeCollection( edgePredicate,
                                               new AnyToAnyCursorFilter( directionFlags,
                                                                         userPredicate ) );
        }

        // If we reach this point, at least one node is of
        // PredicateSpec.PREDICATE

        // Make sure A is a predicate
        if( nodeAType == PredicateSpec.ANY ) {
            nodeA = nodeB;
            nodeB = TruePredicate.INSTANCE;
            nodeBType = PredicateSpec.ANY;
            directionFlags = GraphUtils.invertDirection( directionFlags );
        }
        Predicate basePredicate = (Predicate) nodeA;

        if( nodeBType == PredicateSpec.ANY ) {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges() returning optimized filtered collection" );
            }
            return new PredToAnyEdgeCollection( edgePredicate,
                                                basePredicate,
                                                new PredToAnyCursorFilter( directionFlags,
                                                                           basePredicate,
                                                                           userPredicate ) );
        }

        // First is P~Q, Q(base) true.
        // Second is P~Q, Q(base) false.
        CursorFilter qFilter = new PQToQCursorFilter( directionFlags,
                                                      basePredicate,
                                                      (Predicate) nodeB,
                                                      userPredicate );
        CursorFilter notQFilter = new ToPredCursorFilter( directionFlags,
                                                          (Predicate) nodeB,
                                                          userPredicate );

        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "  " + instanceString + ".edges() returning optimized filtered collection" );
        }
        return new PToQEdgeCollection( edgePredicate,
                                       basePredicate,
                                       (Predicate) nodeB,
                                       qFilter,
                                       notQFilter );
    }


    public Collection edges( Predicate edgePredicate )
    {
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( instanceString + ".edges( " + edgePredicate + " )" );
        }

        if( edgePredicate == null || edgePredicate == TruePredicate.INSTANCE ) {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges() returning all edges" );
            }
            return edgeCollection;

        } else if( edgePredicate == FalsePredicate.INSTANCE ) {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges() returning empty set" );
            }
            return Collections.EMPTY_SET;

        } else if( edgePredicate == GraphUtils.DIRECTED_EDGE_PREDICATE ) {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges() returning all directed edges" );
            }
            return new AnyToAnyEdgeCollection( GraphUtils.DIRECTED_EDGE_PREDICATE,
                                               BASE_TAIL_DIRECTED_CURSOR_FILTER );

        } else if( edgePredicate == GraphUtils.UNDIRECTED_EDGE_PREDICATE ) {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges() returning all undirected edges" );
            }
            return new AnyToAnyEdgeCollection( GraphUtils.UNDIRECTED_EDGE_PREDICATE,
                                               BASE_TAIL_UNDIRECTED_CURSOR_FILTER );

        } else if( edgePredicate == GraphUtils.SELF_EDGE_PREDICATE ) {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges() returning all self edges" );
            }
            return new AnyToAnyEdgeCollection( GraphUtils.SELF_EDGE_PREDICATE,
                                               SELF_CURSOR_FILTER );

        } else if( edgePredicate instanceof EdgePredicate ) {
            return edgesHelper( (EdgePredicate) edgePredicate );

        } else if( edgePredicate instanceof EqualPredicate ) {
            Graph.Edge testEdge = (Graph.Edge) ((EqualPredicate) edgePredicate).getTestObject();
            if( !containsEdge( testEdge ) ) {
                if( LOGGER.isDebugEnabled() ) {
                    LOGGER.debug( "  " + instanceString + ".edges() returning empty set" );
                }
                return Collections.EMPTY_SET;
            }
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges() returning singleton" );
            }
            return new SingletonEdgeCollection( this, testEdge );

        } else {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges() returning general filtered collection" );
            }
            return new AnyToAnyEdgeCollection( edgePredicate,
                                               new BTailCursorFilter( edgePredicate ) );
        }
    }


    public Collection adjacentNodes( Object node,
                                     Predicate traverserPredicate )
    {
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( instanceString + ".adjacentNodes( "
                          + node + ", "
                          + traverserPredicate + " )" );
        }
        AdjacencyList adj = checkNode( node );
        CursorFilter filter = createCursorFilter( traverserPredicate );
        if( filter == FALSE_CURSOR_FILTER ) {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".adjacentNodes() returning empty set" );
            }
            return Collections.EMPTY_SET;
        }
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "  " + instanceString + ".adjacentNodes() returning filtered collection" );
        }
        return new AdjNodeCollection( adj, filter );
    }


    public Collection incidentEdges( Object node,
                                     Predicate traverserPredicate )
    {
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( instanceString + ".incidentEdges( "
                          + node + ", "
                          + traverserPredicate + " )" );
        }
        AdjacencyList adj = checkNode( node );
        CursorFilter filter = createCursorFilter( traverserPredicate );
        if( filter == FALSE_CURSOR_FILTER ) {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".incidentEdges() returning empty set" );
            }
            return Collections.EMPTY_SET;
        }
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "  " + instanceString + ".incidentEdges() returning filtered collection" );
        }
        return new IncEdgeCollection( adj, filter );
    }


    public Object getNode( Predicate nodePredicate )
    {
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( instanceString + ".getNode( " + nodePredicate + " )" );
        }
        Object node;

        if( nodePredicate == null || nodePredicate == TruePredicate.INSTANCE ) {
            Iterator i = nodeMap.keySet().iterator();
            node = i.hasNext() ? i.next() : null;

        } else if( nodePredicate == FalsePredicate.INSTANCE ) {
            node = null;

        } else if( nodePredicate instanceof EqualPredicate ) {
            Object testNode = ((EqualPredicate) nodePredicate).getTestObject();
            AdjacencyList adj = (AdjacencyList) nodeMap.get( testNode );
            node = (adj != null) ? adj.node : null;

        } else {
            node = null;
            for( Iterator i = nodeMap.keySet().iterator(); i.hasNext(); ) {
                Object testNode = i.next();
                if( nodePredicate.evaluate( testNode ) ) {
                    node = testNode;
                    break;
                }
            }
        }

        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "  " + instanceString + ".getNode() returning " + node );
        }
        return node;
    }


    public Graph.Edge getEdge( Predicate edgePredicate )
    {
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( instanceString + ".getEdge( " + edgePredicate + " )" );
        }
        Graph.Edge edge;

        if( edgePredicate == FalsePredicate.INSTANCE ) {
            edge = null;

        } else if( edgePredicate instanceof EqualPredicate ) {
            Graph.Edge testEdge = (Graph.Edge) ((EqualPredicate) edgePredicate).getTestObject();
            edge = containsEdge( testEdge ) ? testEdge : null;

        } else {
            Collection edges = edges( edgePredicate );
            if( edges instanceof EdgeCollection ) {
                edge = ((EdgeCollection) edges).get();
            } else {
                Iterator i = edges.iterator();
                edge = i.hasNext() ? (Graph.Edge) i.next() : null;
            }
        }

        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "  " + instanceString + ".getEdge() returning " + edge );
        }
        return edge;
    }


    public Object getAdjacentNode( Object node,
                                   Predicate traverserPredicate )
    {
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( instanceString + ".getAdjacentNode( "
                          + node + ", "
                          + traverserPredicate + " )" );
        }
        Graph.Edge edge = getIncidentEdge( node, traverserPredicate );
        Object returnNode = (edge != null)
            ? edge.getOtherEndpoint( node )
            : null;
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "  " + instanceString + ".getAdjacentNode() returning " + returnNode );
        }
        return returnNode;
    }


    public Graph.Edge getIncidentEdge( Object node,
                                       Predicate traverserPredicate )
    {
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( instanceString + ".getIncidentEdge( "
                          + node + ", "
                          + traverserPredicate + " )" );
        }
        AdjacencyList adj = checkNode( node );
        Graph.Edge edge = adj.get( createCursorFilter( traverserPredicate ) );
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "  " + instanceString + ".getIncidentEdge() returning " + edge );
        }
        return edge;
    }


    /**
     *  Returns a <code>Traverser</code> from <code>node</code> to all
     *  adjacent nodes for which the specified filter is satisfied.
     *
     *  <P>The returned <code>Traverser</code> is tolerant of changes
     *  to the underlying <code>Graph</code>.  Note that this does not
     *  mean the <code>Traverser</code> is thread-safe.  However, if a
     *  node or edge is added or removed while the iteration is is
     *  progress, the iteration will not throw a
     *  <code>ConcurrentModificationException</code>.  In fact, its
     *  state will reflect the changes.  This means that, among other
     *  things, you should always call {@link Traverser#hasNext()}
     *  before {@link Traverser#next()} if there is a chance the
     *  structure has changed since the last call to
     *  <code>hasNext()</code>.  The one exception is that if the node
     *  upon which the returned <code>Traverser</code> is based is
     *  removed, then all operations except <code>hasNext()</code>
     *  will throw a <code>ConcurrentModificationException</code>.
     *
     *  <P><b>Description copied from interface: {@link Graph}</b><br>
     *  <P>{@inheritDoc}
     */
    public Traverser traverser( Object node,
                                Predicate traverserPredicate )
    {
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( instanceString + ".traverser( "
                          + node + ", "
                          + traverserPredicate + " )" );
        }
        AdjacencyList adj = checkNode( node );
        Traverser t = new CursorTraverserAdapter( adj.cursor( createCursorFilter( traverserPredicate ) ) );
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "  " + instanceString + ".traverser() returning" );
        }
        return t;
    }


    ////////////////////////////////////////
    // ObservableGraph methods
    ////////////////////////////////////////


    public void addGraphListener( GraphListener listener )
    {
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( instanceString + ".addGraphListener( " + listener + " )" );
        }
        observableDelegate.addGraphListener( listener );
    }


    public void removeGraphListener( GraphListener listener )
    {
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( instanceString + ".removeGraphListener( " + listener + " )" );
        }
        observableDelegate.removeGraphListener( listener );
    }


    ////////////////////////////////////////
    // Object
    ////////////////////////////////////////


    public boolean equals( Object object )
    {
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( instanceString + ".equals( " + object + " )" );
        }
        boolean equals = super.equals( object );
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "  " + instanceString + ".equals() returning " + equals );
        }
        return equals;
    }


    public int hashCode()
    {
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( instanceString + ".hashCode()" );
        }
        int hashCode = super.hashCode();
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "  " + instanceString + ".hashCode() returning " + hashCode );
        }
        return hashCode;
    }


    public String toString()
    {
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( instanceString + ".toString()" );
        }
        // FIXME - create a better string representation
        String string = debugToString();
        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "  " + instanceString + ".toString() returning" );
        }
        return string;
    }


    ////////////////////////////////////////
    // Cursor interface definitions
    ////////////////////////////////////////


    private interface Cursor extends Iterator
    {
        /**
         *  Returns the <code>AdjacencyList</code> for which this
         *  is a cursor.
         */
        public AdjacencyList getAdjacencyList();

        /**
         *  Gets the current edge.
         */
        public Graph.Edge getCurrentEdge();

        /**
         *  Convenience method to get the other end of the current
         *  edge.
         */
        public Object getOtherNode();

        /**
         *  Convenience method to remove the other end of the current
         *  edge.
         */
        public void removeOtherNode();

        /**
         *  Notifies this cursor that something was removed from
         *  the underlying list.
         */
        public void edgeRemoved( int index );
    }


    private interface CursorFilter
    {
        public boolean evaluate( Object baseNode, Graph.Edge edge );
    }


    ////////////////////////////////////////
    // CursorFilters - basically Traverser filters.
    ////////////////////////////////////////


    /**
     *  Used by edge iterators or traversers to look for a specific
     *  edge.
     */
    private static class EqualsCursorFilter
        implements CursorFilter
    {
        private final Graph.Edge testEdge;

        EqualsCursorFilter( Graph.Edge edge )
        {
            super();
            testEdge = edge;
        }

        public boolean evaluate( Object baseNode, Graph.Edge edge )
        {
            return testEdge.equals( edge );
        }
    }


    /**
     *  Used by edge iterators where no more efficient/specific
     *  processing is possible.
     */
    private static class BTailCursorFilter
        implements CursorFilter
    {
        private final Predicate edgePredicate;

        BTailCursorFilter( Predicate edgePredicate )
        {
            super();
            this.edgePredicate = edgePredicate;
        }

        public boolean evaluate( Object baseNode, Graph.Edge edge )
        {
            return GraphUtils.equals( baseNode, edge.getTail() ) && edgePredicate.evaluate( edge );
        }
    }


    /**
     *  Used by traversers where no more efficient/specific processing
     *  is possible.
     */
    private static class GeneralTraverserCursorFilter
        implements CursorFilter
    {
        private final Predicate traverserPredicate;
        private final OrderedPair pair = new OrderedPair( null, null );

        GeneralTraverserCursorFilter( Predicate traverserPredicate )
        {
            super();
            this.traverserPredicate = traverserPredicate;
        }

        public boolean evaluate( Object baseNode, Graph.Edge edge )
        {
            pair.setFirst( baseNode );
            pair.setSecond( edge );
            return traverserPredicate.evaluate( pair );
        }
    }


    private static class ToAnyCursorFilter
        implements CursorFilter
    {
        private final int directionFlags;
        private final Predicate userPredicate;

        ToAnyCursorFilter( int directionFlags,
                           Predicate userPredicate )
        {
            super();
            this.directionFlags = directionFlags;
            this.userPredicate = userPredicate;
        }

        protected boolean subTest( Object baseNode, Graph.Edge edge )
        {
            return true;
        }

        public boolean evaluate( Object baseNode, Graph.Edge edge )
        {
            if( edge.isDirected() ) {
                if( (directionFlags & GraphUtils.DIRECTED_MASK) == GraphUtils.DIRECTED_MASK ) {
                    return subTest( baseNode, edge )
                        && userPredicate.evaluate( edge.getUserObject() );
                }
                return ( ( (directionFlags & GraphUtils.DIRECTED_OUT_MASK) != 0 && GraphUtils.equals( baseNode, edge.getTail() ) )
                         || ( (directionFlags & GraphUtils.DIRECTED_IN_MASK) != 0  && GraphUtils.equals( baseNode, edge.getHead() ) ) )
                    && userPredicate.evaluate( edge.getUserObject() );
            }
            return (directionFlags & GraphUtils.UNDIRECTED_MASK) != 0
                && subTest( baseNode, edge )
                && userPredicate.evaluate( edge.getUserObject() );
        }
    }


    // This is also PtoQCursorFilter, where Q(base) is false
    private static class ToPredCursorFilter extends ToAnyCursorFilter
    {
        private final Predicate nodePredicate;

        ToPredCursorFilter( int directionFlags,
                            Predicate nodePredicate,
                            Predicate userPredicate )
        {
            super( directionFlags, userPredicate );
            this.nodePredicate = nodePredicate;
        }

        public boolean evaluate( Object baseNode, Graph.Edge edge )
        {
            return super.evaluate( baseNode, edge )
                && nodePredicate.evaluate( edge.getOtherEndpoint( baseNode ) );
        }
    }


    private static class ToEqualsCursorFilter extends ToAnyCursorFilter
    {
        private final Object testNode;

        ToEqualsCursorFilter( int directionFlags,
                              Object testNode,
                              Predicate userPredicate )
        {
            super( directionFlags, userPredicate );
            this.testNode = testNode;
        }

        public boolean evaluate( Object baseNode, Graph.Edge edge )
        {
            return super.evaluate( baseNode, edge )
                && GraphUtils.equals( testNode, edge.getOtherEndpoint( baseNode ) );
        }
    }


    private static class AnyToAnyCursorFilter extends ToAnyCursorFilter
    {
        AnyToAnyCursorFilter( int directionFlags,
                              Predicate userPredicate )
        {
            super( directionFlags, userPredicate );
        }

        protected boolean subTest( Object baseNode, Graph.Edge edge )
        {
            return GraphUtils.equals( baseNode, edge.getTail() );
        }
    }


    private static class PredToAnyCursorFilter extends ToAnyCursorFilter
    {
        private final Predicate basePredicate;

        PredToAnyCursorFilter( int directionFlags,
                               Predicate basePredicate,
                               Predicate userPredicate )
        {
            super( directionFlags, userPredicate );
            this.basePredicate = basePredicate;
        }

        protected boolean subTest( Object baseNode, Graph.Edge edge )
        {
            Object tail = edge.getTail();
            return GraphUtils.equals( baseNode, tail )
                || !basePredicate.evaluate( tail );
        }
    }


    private static class PQToQCursorFilter extends ToAnyCursorFilter
    {
        private final Predicate basePredicate;
        private final Predicate nodePredicate;

        PQToQCursorFilter( int directionFlags,
                           Predicate basePredicate,
                           Predicate nodePredicate,
                           Predicate userPredicate )
        {
            super( directionFlags, userPredicate );
            this.basePredicate = basePredicate;
            this.nodePredicate = nodePredicate;
        }

        protected boolean subTest( Object baseNode, Graph.Edge edge )
        {
            Object tail = edge.getTail();
            return GraphUtils.equals( baseNode, tail )
                || !basePredicate.evaluate( tail );
        }

        public boolean evaluate( Object baseNode, Graph.Edge edge )
        {
            return super.evaluate( baseNode, edge )
                && nodePredicate.evaluate( edge.getOtherEndpoint( baseNode ) );
        }
    }


    ////////////////////////////////////////
    // Private Cursor-Iterator adapter classes
    ////////////////////////////////////////


    /**
     *  Iterates over the nodes in a Cursor.
     */
    private static class CursorNodeIteratorAdapter
        implements Iterator
    {
        private final Cursor cursor;

        CursorNodeIteratorAdapter( Cursor cursor )
        {
            super();
            this.cursor = cursor;
        }

        public boolean hasNext()
        {
            return cursor.hasNext();
        }

        public Object next()
        {
            cursor.next();
            return cursor.getOtherNode();
        }

        public void remove()
        {
            // Note that this just removes the Edge.
            cursor.remove();
        }
    }


    /**
     *  Iterates over the edges in a Cursor.
     */
    private static class CursorEdgeIteratorAdapter
        implements Iterator
    {
        private final Cursor cursor;

        CursorEdgeIteratorAdapter( Cursor cursor )
        {
            super();
            this.cursor = cursor;
        }

        public boolean hasNext()
        {
            return cursor.hasNext();
        }

        public Object next()
        {
            return cursor.next();
        }

        public void remove()
        {
            cursor.remove();
        }
    }


    /**
     *  Traverses over a Cursor.
     */
    private static class CursorTraverserAdapter
        implements Traverser
    {
        private final Cursor cursor;

        CursorTraverserAdapter( Cursor cursor )
        {
            super();
            this.cursor = cursor;
        }

        public boolean hasNext()
        {
            return cursor.hasNext();
        }

        public Object next()
        {
            cursor.next();
            return cursor.getOtherNode();
        }

        public void remove()
        {
            cursor.removeOtherNode();
        }

        public Graph.Edge getEdge()
        {
            return cursor.getCurrentEdge();
        }

        public void removeEdge()
        {
            cursor.remove();
        }
    }


    ////////////////////////////////////////
    // Private Collection view classes
    ////////////////////////////////////////


    private class AllNodesCollection
        implements Collection
    {
        AllNodesCollection()
        {
            super();
        }

        public int size()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".nodes().size()" );
            }
            int size = nodeMap.size();
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".nodes().size() returning " + size );
            }
            return size;
        }

        public boolean isEmpty()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".nodes().isEmpty()" );
            }
            boolean isEmpty = nodeMap.isEmpty();
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".nodes().isEmpty() returning " + isEmpty );
            }
            return isEmpty;
        }

        public boolean add( Object object )
        {
            throw new UnsupportedOperationException();
        }

        public boolean remove( Object object )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".nodes().remove( " + object + " )" );
            }
            boolean modified = removeNode( object );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".nodes().remove() returning " + modified );
            }
            return modified;
        }

        public boolean contains( Object object )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".nodes().contains( " + object + " )" );
            }
            boolean contains = containsNode( object );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".nodes().contains() returning " + contains );
            }
            return contains;
        }

        public Iterator iterator()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".nodes().iterator()" );
            }
            Iterator i = new NodeIterator();
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".nodes().iterator() returning" );
            }
            return i;
        }

        public boolean containsAll( Collection collection )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".nodes().containsAll( " + collection + " )" );
            }
            boolean containsAll = nodeMap.keySet().containsAll( collection );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".nodes().containsAll() returning " + containsAll );
            }
            return containsAll;
        }

        public boolean addAll( Collection collection )
        {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll( Collection collection )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".nodes().removeAll( " + collection + " )" );
            }
            // In this case, it's probably less overhead to iterate
            // over the provided collection.  Iterators over the
            // nodeset have a relatively high overhead.
            boolean modified = false;
            for( Iterator i = collection.iterator(); i.hasNext(); ) {
                modified |= removeNode( i.next() );
            }
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".nodes().removeAll() returning " + modified );
            }
            return modified;
        }

        public boolean retainAll( Collection collection )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".nodes().retainAll( " + collection + " )" );
            }
            // Here, we have no choice but to iterate over the
            // nodeset.
            boolean modified = false;
            for( Iterator i = iterator(); i.hasNext(); ) {
                if( !collection.contains( i.next() ) ) {
                    i.remove();
                    modified = true;
                }
            }
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".nodes().retainAll() returning " + modified );
            }
            return modified;
        }

        public void clear()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".nodes().clear()" );
            }
            // Remove all the nodes
            for( Iterator i = iterator(); i.hasNext(); ) {
                i.next();
                i.remove();
            }
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".nodes().clear() returning" );
            }
        }

        public Object[] toArray()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".nodes().toArray()" );
            }
            Object[] array = nodeMap.keySet().toArray();
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".nodes().toArray() returning " + array );
            }
            return array;
        }

        public Object[] toArray( Object[] array )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".nodes().toArray( " + array + " )" );
            }
            Object[] returnedArray = nodeMap.keySet().toArray( array );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".nodes().toArray() returning " + returnedArray );
            }
            return returnedArray;
        }

        public String toString()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".nodes().toString()" );
            }
            String string = nodeMap.keySet().toString();
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".nodes().toString() returning" );
            }
            return string;
        }

        private class NodeIterator
            implements Iterator
        {
            private final Iterator adjIter = nodeMap.values().iterator();
            private AdjacencyList adj = null;
            private boolean isCurrentValid = false;

            NodeIterator()
            {
                super();
            }

            public boolean hasNext()
            {
                return adjIter.hasNext();
            }

            public Object next()
            {
                adj = (AdjacencyList) adjIter.next();
                isCurrentValid = true;
                return adj.node;
            }

            public void remove()
            {
                if( !isCurrentValid ) {
                    throw new IllegalStateException();
                }
                if( LOGGER.isDebugEnabled() ) {
                    LOGGER.debug( instanceString + ".nodes().iterator().remove() upon node " + adj.node );
                }
                nodeRemoving( adj.node );
                adj.clear();
                adjIter.remove();
                processNodeRemoved( adj.node );
                nodeRemoved( adj.node );
                isCurrentValid = false;
                if( LOGGER.isDebugEnabled() ) {
                    LOGGER.debug( "  " + instanceString + ".nodes().iterator().remove() returning" );
                }
            }
        }
    }


    private class AdjNodeCollection extends AbstractCollection
    {
        private final AdjacencyList adj;
        private final CursorFilter filter;

        AdjNodeCollection( AdjacencyList adj, CursorFilter filter )
        {
            super();
            this.adj = adj;
            this.filter = filter;
        }

        public int size()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".adjacentNodes().size()" );
            }
            int size = adj.degree( filter );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".adjacentNodes().size() returning " + size );
            }
            return size;
        }

        public boolean isEmpty()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".adjacentNodes().isEmpty()" );
            }
            boolean isEmpty = super.isEmpty();
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".adjacentNodes().isEmpty() returning " + isEmpty );
            }
            return isEmpty;
        }

        public boolean remove( Object object )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".adjacentNodes().remove( " + object + " )" );
            }
            if( nodeMap.get( object ) == null ) {
                if( LOGGER.isDebugEnabled() ) {
                    LOGGER.debug( "  " + instanceString + ".adjacentNodes().remove() returning false" );
                }
                return false;
            }
            for( Cursor cursor = adj.cursor( filter ); cursor.hasNext(); ) {
                cursor.next();
                if( GraphUtils.equals( object, cursor.getOtherNode() ) ) {
                    // Note that this just removes the Edge.
                    cursor.remove();
                    if( LOGGER.isDebugEnabled() ) {
                        LOGGER.debug( "  " + instanceString + ".adjacentNodes().remove() returning true" );
                    }
                    return true;
                }
            }
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".adjacentNodes().remove() returning false" );
            }
            return false;
        }

        public boolean contains( Object object )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".adjacentNodes().contains( " + object + " )" );
            }
            if( nodeMap.get( object ) == null ) {
                if( LOGGER.isDebugEnabled() ) {
                    LOGGER.debug( "  " + instanceString + ".adjacentNodes().contains() returning false" );
                }
                return false;
            }
            for( Cursor cursor = adj.cursor( filter ); cursor.hasNext(); ) {
                cursor.next();
                if( GraphUtils.equals( object, cursor.getOtherNode() ) ) {
                    if( LOGGER.isDebugEnabled() ) {
                        LOGGER.debug( "  " + instanceString + ".adjacentNodes().contains() returning true" );
                    }
                    return true;
                }
            }
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".adjacentNodes().contains() returning false" );
            }
            return false;
        }

        public Iterator iterator()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".adjacentNodes().iterator()" );
            }
            Iterator i = new CursorNodeIteratorAdapter( adj.cursor( filter ) );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".adjacentNodes().iterator() returning" );
            }
            return i;
        }

        public boolean addAll( Collection collection )
        {
            throw new UnsupportedOperationException();
        }

        public boolean containsAll( Collection collection )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".adjacentNodes().containsAll( " + collection + " )" );
            }
            boolean containsAll = super.containsAll( collection );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".adjacentNodes().containsAll() returning " + containsAll );
            }
            return containsAll;
        }

        public boolean removeAll( Collection collection )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".adjacentNodes().removeAll( " + collection + " )" );
            }
            boolean modified = super.removeAll( collection );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".adjacentNodes().removeAll() returning " + modified );
            }
            return modified;
        }

        public boolean retainAll( Collection collection )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".adjacentNodes().retainAll( " + collection + " )" );
            }
            boolean modified = super.retainAll( collection );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".adjacentNodes().retainAll() returning " + modified );
            }
            return modified;
        }

        public void clear()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".adjacentNodes().clear()" );
            }
            for( Iterator i = adj.cursor( filter ); i.hasNext(); ) {
                i.next();
                i.remove();
            }
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".adjacentNodes().clear() returning" );
            }
        }

        public Object[] toArray()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".adjacentNodes().toArray()" );
            }
            Object[] array = super.toArray();
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".adjacentNodes().toArray() returning " + array );
            }
            return array;
        }

        public Object[] toArray( Object[] array )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".adjacentNodes().toArray( " + array + " )" );
            }
            Object[] returnedArray = super.toArray( array );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".adjacentNodes().toArray() returning " + returnedArray );
            }
            return returnedArray;
        }

        public String toString()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".adjacentNodes().toString()" );
            }
            String string = super.toString();
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".adjacentNodes().toString() returning " + string );
            }
            return string;
        }
    }


    /**
     *  Abstract base class for all Edge collections, defines a
     *  private get() method to just get a single Edge from the
     *  Collection.
     */
    private abstract class EdgeCollection extends AbstractCollection
    {
        EdgeCollection()
        {
            super();
        }

        abstract Graph.Edge get();

        public boolean addAll( Collection collection )
        {
            throw new UnsupportedOperationException();
        }
    }


    private class IncEdgeCollection extends EdgeCollection
    {
        private final AdjacencyList adj;
        private final CursorFilter filter;

        IncEdgeCollection( AdjacencyList adj, CursorFilter filter )
        {
            super();
            this.adj = adj;
            this.filter = filter;
        }

        Graph.Edge get()
        {
            return adj.get( filter );
        }

        public int size()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".incidentEdges().size()" );
            }
            int size = adj.degree( filter );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".incidentEdges().size() returning " + size );
            }
            return size;
        }

        public boolean isEmpty()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".incidentEdges().isEmpty()" );
            }
            boolean isEmpty = super.isEmpty();
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".incidentEdges().isEmpty() returning " + isEmpty );
            }
            return isEmpty;
        }

        public boolean remove( Object object )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".incidentEdges().remove( " + object + " )" );
            }
            if( !(object instanceof Graph.Edge) ) {
                if( LOGGER.isDebugEnabled() ) {
                    LOGGER.debug( "  " + instanceString + ".incidentEdges().remove() returning false" );
                }
                return false;
            }
            Graph.Edge edge = (Graph.Edge) object;
            boolean modified = (filter == null || filter.evaluate( adj.node, edge ))
                && adj.remove( edge );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".incidentEdges().remove() returning " + modified );
            }
            return modified;
        }

        public boolean contains( Object object )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".incidentEdges().contains( " + object + " )" );
            }
            if( !(object instanceof Graph.Edge) ) {
                if( LOGGER.isDebugEnabled() ) {
                    LOGGER.debug( "  " + instanceString + ".incidentEdges().contains() returning false" );
                }
                return false;
            }
            Graph.Edge edge = (Graph.Edge) object;
            boolean contains = (filter == null || filter.evaluate( adj.node, edge ))
                && adj.contains( edge );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".incidentEdges().contains() returning " + contains );
            }
            return contains;
        }

        public Iterator iterator()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".incidentEdges().iterator()" );
            }
            Iterator i = new CursorEdgeIteratorAdapter( adj.cursor( filter ) );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".incidentEdges().iterator() returning" );
            }
            return i;
        }

        public boolean containsAll( Collection collection )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".incidentEdges().containsAll( " + collection + " )" );
            }
            boolean containsAll = super.containsAll( collection );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".incidentEdges().containsAll() returning " + containsAll );
            }
            return containsAll;
        }

        public boolean removeAll( Collection collection )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".incidentEdges().removeAll( " + collection + " )" );
            }
            boolean modified = super.removeAll( collection );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".incidentEdges().removeAll() returning " + modified );
            }
            return modified;
        }

        public boolean retainAll( Collection collection )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".incidentEdges().retainAll( " + collection + " )" );
            }
            boolean modified = super.retainAll( collection );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".incidentEdges().retainAll() returning " + modified );
            }
            return modified;
        }

        public void clear()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".incidentEdges().clear()" );
            }
            for( Iterator i = adj.cursor( filter ); i.hasNext(); ) {
                i.next();
                i.remove();
            }
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".incidentEdges().clear() returning" );
            }
        }

        public Object[] toArray()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".incidentEdges().toArray()" );
            }
            Object[] array = super.toArray();
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".incidentEdges().toArray() returning " + array );
            }
            return array;
        }

        public Object[] toArray( Object[] array )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".incidentEdges().toArray( " + array + " )" );
            }
            Object[] returnedArray = super.toArray( array );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".incidentEdges().toArray() returning " + returnedArray );
            }
            return returnedArray;
        }

        public String toString()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".incidentEdges().toString()" );
            }
            String string = super.toString();
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".incidentEdges().toString() returning " + string );
            }
            return string;
        }
    }


    private abstract class FilteredEdgeCollection extends EdgeCollection
    {
        private final Predicate edgePredicate;

        FilteredEdgeCollection( Predicate edgePredicate )
        {
            super();
            this.edgePredicate = edgePredicate;
        }

        /**
         *  Returns the appropriate filter for the specified base
         *  node, or null if the specified base node is not allowed.
         */
        abstract CursorFilter getFilter( Object baseNode );

        Graph.Edge get()
        {
            for( Iterator i = nodeMap.values().iterator(); i.hasNext(); ) {
                AdjacencyList adj = (AdjacencyList) i.next();
                CursorFilter filter = getFilter( adj.node );
                if( filter != null ) {
                    Graph.Edge edge = adj.get( filter );
                    if( edge != null ) {
                        return edge;
                    }
                }
            }
            return null;
        }

        public int size()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".edges().size()" );
            }
            int size = 0;
            for( Iterator i = nodeMap.values().iterator(); i.hasNext(); ) {
                AdjacencyList adj = (AdjacencyList) i.next();
                CursorFilter filter = getFilter( adj.node );
                if( filter != null ) {
                    size += adj.degree( filter );
                }
            }
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges().size() returning " + size );
            }
            return size;
        }

        public boolean isEmpty()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".edges().isEmpty()" );
            }
            for( Iterator i = nodeMap.values().iterator(); i.hasNext(); ) {
                AdjacencyList adj = (AdjacencyList) i.next();
                CursorFilter filter = getFilter( adj.node );
                if( filter != null && adj.get( filter ) != null ) {
                    if( LOGGER.isDebugEnabled() ) {
                        LOGGER.debug( "  " + instanceString + ".edges().isEmpty() returning false" );
                    }
                    return false;
                }
            }
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges().isEmpty() returning true" );
            }
            return true;
        }

        public boolean remove( Object object )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".edges().remove( " + object + " )" );
            }
            boolean modified = (object instanceof Graph.Edge)
                && edgePredicate.evaluate( object )
                && removeEdge( (Graph.Edge) object );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges().remove() returning " + modified );
            }
            return modified;
        }

        public boolean contains( Object object )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".edges().contains( " + object + " )" );
            }
            boolean contains = (object instanceof Graph.Edge)
                && edgePredicate.evaluate( object )
                && containsEdge( (Graph.Edge) object );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges().contains() returning " + contains );
            }
            return contains;
        }

        public Iterator iterator()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".edges().iterator()" );
            }
            Iterator i = new EdgeIterator();
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges().iterator() returning" );
            }
            return i;
        }

        public boolean containsAll( Collection collection )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".edges().containsAll( " + collection + " )" );
            }
            boolean containsAll = super.containsAll( collection );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges().containsAll() returning " + containsAll );
            }
            return containsAll;
        }

        public boolean removeAll( Collection collection )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".edges().removeAll( " + collection + " )" );
            }
            boolean modified = super.removeAll( collection );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges().removeAll() returning " + modified );
            }
            return modified;
        }

        public boolean retainAll( Collection collection )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".edges().retainAll( " + collection + " )" );
            }
            boolean modified = super.retainAll( collection );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges().retainAll() returning " + modified );
            }
            return modified;
        }

        public void clear()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".edges().clear()" );
            }
            super.clear();
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges().clear() returning" );
            }
        }

        public Object[] toArray()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".edges().toArray()" );
            }
            Object[] array = super.toArray();
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges().toArray() returning " + array );
            }
            return array;
        }

        public Object[] toArray( Object[] array )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".edges().toArray( " + array + " )" );
            }
            Object[] returnedArray = super.toArray( array );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges().toArray() returning " + returnedArray );
            }
            return returnedArray;
        }

        public String toString()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".edges().toString()" );
            }
            String string = super.toString();
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges().toString() returning" );
            }
            return string;
        }

        private class EdgeIterator
            implements Iterator
        {
            private final Iterator adjIter = nodeMap.values().iterator();
            private Cursor cursor = EMPTY_CURSOR;
            private Object current = null;
            private Object next = null;

            EdgeIterator()
            {
                super();
            }

            protected void advance()
            {
                while( !cursor.hasNext() && adjIter.hasNext() ) {
                    AdjacencyList adj = (AdjacencyList) adjIter.next();
                    CursorFilter filter = getFilter( adj.node );
                    if( filter != null ) {
                        cursor = adj.cursor( filter );
                    }
                }
            }

            public boolean hasNext()
            {
                if( next != null ) {
                    return true;
                }
                advance();
                if( !cursor.hasNext() ) {
                    return false;
                }
                next = cursor.next();
                return true;
            }

            public Object next()
            {
                if( !hasNext() ) {
                    throw new NoSuchElementException();
                }
                current = next;
                next = null;
                return current;
            }

            public void remove()
            {
                if( current == null ) {
                    throw new IllegalStateException();
                }
                if( LOGGER.isDebugEnabled() ) {
                    LOGGER.debug( instanceString + ".edges().iterator().remove() upon edge " + current );
                }
                removeEdge( (Graph.Edge) current );
                current = null;
                if( LOGGER.isDebugEnabled() ) {
                    LOGGER.debug( instanceString + ".edges().iterator().remove() returning" );
                }
            }
        }
    }


    /**
     *  Edge collection for all the edges.
     */
    private class AllEdgesCollection extends FilteredEdgeCollection
    {
        AllEdgesCollection()
        {
            super( null );
        }

        CursorFilter getFilter( Object baseNode )
        {
            return BASE_TAIL_CURSOR_FILTER;
        }

        Graph.Edge get()
        {
            for( Iterator i = nodeMap.values().iterator(); i.hasNext(); ) {
                Graph.Edge edge = ((AdjacencyList) i.next()).get( null );
                if( edge != null ) {
                    return edge;
                }
            }
            return null;
        }

        public int size()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".edges().size()" );
                LOGGER.debug( "  " + instanceString + ".edges().size() returning " + edgeSize );
            }
            return edgeSize;
        }

        public boolean isEmpty()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".edges().isEmpty()" );
                LOGGER.debug( "  " + instanceString + ".edges().isEmpty() returning " + (edgeSize == 0) );
            }
            return edgeSize == 0;
        }

        public boolean remove( Object object )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".edges().remove( " + object + " )" );
            }
            boolean modified = (object instanceof Graph.Edge)
                && removeEdge( (Graph.Edge) object );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges().remove() returning " + modified );
            }
            return modified;
        }

        public boolean contains( Object object )
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( instanceString + ".edges().contains( " + object + " )" );
            }
            boolean contains = (object instanceof Graph.Edge)
                && containsEdge( (Graph.Edge) object );
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".edges().contains() returning " + contains );
            }
            return contains;
        }
    }


    /**
     *  An Edge collection where there is no way to filter either
     *  endpoint.
     */
    private class AnyToAnyEdgeCollection extends FilteredEdgeCollection
    {
        private final CursorFilter filter;

        AnyToAnyEdgeCollection( Predicate edgePredicate,
                                CursorFilter filter )
        {
            super( edgePredicate );
            this.filter = filter;
        }

        CursorFilter getFilter( Object baseNode )
        {
            return filter;
        }
    }


    /**
     *  An Edge collection where one endpoint is filtered.
     */
    private class PredToAnyEdgeCollection extends FilteredEdgeCollection
    {
        private final Predicate basePredicate;
        private final CursorFilter filter;

        PredToAnyEdgeCollection( Predicate edgePredicate,
                                 Predicate basePredicate,
                                 CursorFilter filter )
        {
            super( edgePredicate );
            this.basePredicate = basePredicate;
            this.filter = filter;
        }

        CursorFilter getFilter( Object baseNode )
        {
            return basePredicate.evaluate( baseNode )
                ? filter
                : null;
        }
    }


    /**
     *  An Edge collection where both endpoints are filtered.
     */
    private class PToQEdgeCollection extends FilteredEdgeCollection
    {
        private final Predicate basePredicate;
        private final Predicate nodePredicate;
        private final CursorFilter qBaseFilter;
        private final CursorFilter notQBaseFilter;

        PToQEdgeCollection( Predicate edgePredicate,
                            Predicate basePredicate,
                            Predicate nodePredicate,
                            CursorFilter qBaseFilter,
                            CursorFilter notQBaseFilter )
        {
            super( edgePredicate );
            this.basePredicate = basePredicate;
            this.nodePredicate = nodePredicate;
            this.qBaseFilter = qBaseFilter;
            this.notQBaseFilter = notQBaseFilter;
        }

        CursorFilter getFilter( Object baseNode )
        {
            return basePredicate.evaluate( baseNode )
                ? ( nodePredicate.evaluate( baseNode )
                    ? qBaseFilter
                    : notQBaseFilter )
                : null;
        }
    }


    ////////////////////////////////////////
    // Private AdjacencyList implementation
    ////////////////////////////////////////


    /**
     *  An adjacency list implementation.  When using this method of
     *  representation, each node in the graph has an associated
     *  adjacency list.  A node's adjacency list contains information
     *  about which other nodes are adjacent to it, and through which
     *  edges.  A self-loop should only be included once.
     */
    private class AdjacencyList
    {
        /**
         *  The node for which this is an adjacency list.
         */
        final Object node;

        /**
         *  The list of Graph.Edge objects in this adjacency list.
         */
        final ArrayList edges = new ArrayList( 5 );

        /**
         *  Whether or not this adjacency list is still valid (has not
         *  been cleared).  This is used by cursors.
         */
        boolean isValid = true;


        ////////////////////////////////////////
        // Constructor
        ////////////////////////////////////////


        /**
         *  Constructs a new <code>AdjacencyList</code> for the
         *  specified node.
         */
        AdjacencyList( Object node )
        {
            super();
            this.node = node;
        }


        ////////////////////////////////////////
        // Add method
        ////////////////////////////////////////


        /**
         *  Adds the specified edge to this
         *  <code>AdjacencyList</code>, if not already present.
         *  Returns the added <code>Graph.Edge</code> or
         *  <code>null</code>.
         *
         *  @param edge the new edge.
         *
         *  @param headAdj the AdjacencyList of the head of the new
         *  edge.
         *
         *  @return the new edge.
         */
        Graph.Edge addTo( Graph.Edge edge, AdjacencyList headAdj )
        {
            if( edges.contains( edge )
                || (this != headAdj && headAdj.edges.contains( edge )) ) {
                return null;
            }
            edgeAdding( edge );
            edges.add( edge );
            alertEdgeAdded( this );
            // Don't add self-loops twice
            if( this != headAdj ) {
                headAdj.edges.add( edge );
                alertEdgeAdded( headAdj );
            }
            processEdgeAdded( edge );
            edgeAdded( edge );
            return edge;
        }


        ////////////////////////////////////////
        // Remove methods
        ////////////////////////////////////////


        /**
         *  Removes the specified edge from this
         *  <code>AdjacencyList</code>.
         *
         *  @param edge the edge to be removed from this
         *  <code>AdjacencyList</code>.
         */
        boolean remove( Graph.Edge edge )
        {
            int index = edges.indexOf( edge );
            if( index == -1 ) {
                return false;
            }
            remove( index, (Graph.Edge) edges.get( index ) );
            return true;
        }


        /**
         *  Removes the specified edge at the specified index
         *  from this <code>AdjacencyList</code>.
         *
         *  @param index the index of the specified edge to be
         *  removed from this <code>AdjacencyList</code>.
         *
         *  @param edge the edge at the specified index to be
         *  removed from this <code>AdjacencyList</code>.
         */
        void remove( int index, Graph.Edge edge )
        {
            edgeRemoving( edge );
            edges.remove( index );
            alertEdgeRemoved( this, index );
            AdjacencyList otherAdj = (AdjacencyList) nodeMap.get( edge.getOtherEndpoint( node ) );
            if( this != otherAdj ) {
                int otherIndex = otherAdj.edges.indexOf( edge );
                otherAdj.edges.remove( otherIndex );
                alertEdgeRemoved( otherAdj, otherIndex );
            }
            processEdgeRemoved( edge );
            edgeRemoved( edge );
        }


        ////////////////////////////////////////
        // Contains method
        ////////////////////////////////////////


        /**
         *  Returns whether or not this <code>AdjacencyList</code>
         *  contains the specified edge.
         *
         *  @param edge the edge whose presence in this
         *  <code>AdjacencyList</code> is to be tested.
         */
        boolean contains( Graph.Edge edge )
        {
            return edges.contains( edge );
        }


        ////////////////////////////////////////
        // Clear method
        ////////////////////////////////////////


        /**
         *  Clears this <code>AdjacencyList</code> and renders it
         *  invalid for further use.
         */
        void clear()
        {
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".AdjacencyList.clear() for node " + node );
            }
            for( Cursor cursor = new CursorImpl( null ); cursor.hasNext(); ) {
                cursor.next();
                cursor.remove();
            }
            isValid = false;
            if( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "  " + instanceString + ".AdjacencyList.clear() returning" );
            }
        }


        ////////////////////////////////////////
        // Get method
        ////////////////////////////////////////


        /**
         *  Returns the specified edge if this
         *  <code>AdjacencyList</code> contains it, or
         *  <code>null</code> if it doesn't.
         *
         *  @return the specified edge if this
         *  <code>AdjacencyList</code> contains it, or
         *  <code>null</code> if it doesn't.
         */
        Graph.Edge get( CursorFilter filter )
        {
            int size = edges.size();

            if( filter == null ) {
                return (size > 0) ? (Graph.Edge) edges.get( 0 ) : null;

            } else if( filter == FALSE_CURSOR_FILTER ) {
                return null;

            } else {
                for( int i = 0; i < size; i++ ) {
                    Graph.Edge edge = (Graph.Edge) edges.get( i );
                    if( filter.evaluate( node, edge ) ) {
                        return edge;
                    }
                }
                return null;
            }
        }


        ////////////////////////////////////////
        // Counting methods
        ////////////////////////////////////////


        /**
         *  Returns the number of edges in this
         *  <code>AdjacencyList</code>.  If this
         *  <code>AdjacencyList</code> contains more than
         *  <code>Integer.MAX_VALUE</code> edges, returns
         *  <code>Integer.MAX_VALUE</code>.
         *
         *  @return the number of edges in this
         *  <code>AdjacencyList</code>.
         */
        int size()
        {
            return edges.size();
        }


        /**
         *  Returns the number of edges in this
         *  <code>AdjacencyList</code>, with self-loops counted twice.
         *  If this <code>AdjacencyList</code> contains more than
         *  <code>Integer.MAX_VALUE</code> edges, returns
         *  <code>Integer.MAX_VALUE</code>.
         *
         *  @return the number of edges in this
         *  <code>AdjacencyList</code>, with self-loops counted twice.
         */
        int degree()
        {
            int size = edges.size();
            int selfCount = 0;
            for( int i = 0; i < size; i++ ) {
                Graph.Edge edge = (Graph.Edge) edges.get( i );
                if( GraphUtils.equals( edge.getTail(), edge.getHead() ) ) {
                    selfCount++;
                }
            }
            return size + selfCount;
        }


        /**
         *  Returns the number of edges in this
         *  <code>AdjacencyList</code> satisfying the specified
         *  predicate.  If this <code>AdjacencyList</code> contains
         *  more than <code>Integer.MAX_VALUE</code> such edges,
         *  returns <code>Integer.MAX_VALUE</code>.
         *
         *  @return the number of edges in this
         *  <code>AdjacencyList</code> satisfying the specified
         *  predicate.
         */
        int degree( Predicate traverserPredicate )
        {
            int size = edges.size();
            int count = 0;
            OrderedPair pair = new OrderedPair( node, null );
            for( int i = 0; i < size; i++ ) {
                pair.setSecond( edges.get( i ) );
                if( traverserPredicate.evaluate( pair ) ) {
                    count++;
                }
            }
            return count;
        }


        /**
         *  Returns the number of edges in this
         *  <code>AdjacencyList</code> satisfying the specified cursor
         *  filter.  If this <code>AdjacencyList</code> contains more
         *  than <code>Integer.MAX_VALUE</code> such edges, returns
         *  <code>Integer.MAX_VALUE</code>.
         *
         *  @return the number of edges in this
         *  <code>AdjacencyList</code> satisfying the specified
         *  cursor filter.
         */
        int degree( CursorFilter filter )
        {
            int size = edges.size();

            if( filter == null ) {
                return size;

            } else if( filter == FALSE_CURSOR_FILTER ) {
                return 0;

            } else {
                int count = 0;
                for( int i = 0; i < size; i++ ) {
                    Graph.Edge edge = (Graph.Edge) edges.get( i );
                    if( filter.evaluate( node, edge ) ) {
                        count++;
                    }
                }
                return count;
            }
        }


        /**
         *  Returns the number of out edges in this
         *  <code>AdjacencyList</code>.  If this
         *  <code>AdjacencyList</code> contains more than
         *  <code>Integer.MAX_VALUE</code> out edges, returns
         *  <code>Integer.MAX_VALUE</code>.
         *
         *  @return the number of out edges in this
         *  <code>AdjacencyList</code>.
         */
        int outDegree()
        {
            int size = edges.size();
            int count = 0;
            for( int i = 0; i < size; i++ ) {
                Graph.Edge edge = (Graph.Edge) edges.get( i );
                if( edge.isDirected() && GraphUtils.equals( node, edge.getTail() ) ) {
                    count++;
                }
            }
            return count;
        }


        /**
         *  Returns the number of in edges in this
         *  <code>AdjacencyList</code>.  If this
         *  <code>AdjacencyList</code> contains more than
         *  <code>Integer.MAX_VALUE</code> in edges, returns
         *  <code>Integer.MAX_VALUE</code>.
         *
         *  @return the number of in edges in this
         *  <code>AdjacencyList</code>.
         */
        int inDegree()
        {
            int size = edges.size();
            int count = 0;
            for( int i = 0; i < size; i++ ) {
                Graph.Edge edge = (Graph.Edge) edges.get( i );
                if( edge.isDirected() && GraphUtils.equals( node, edge.getHead() ) ) {
                    count++;
                }
            }
            return count;
        }


        ////////////////////////////////////////
        // Cursor creation method
        ////////////////////////////////////////


        /**
         *  Returns a <code>Cursor</code> over all the edges in this
         *  <code>AdjacencyList</code> which satisfy the specified
         *  filter.
         *
         *  @return a <code>Cursor</code> over all the edges in this
         *  <code>AdjacencyList</code> which satisfy the specified
         *  filter.
         */
        Cursor cursor( CursorFilter filter )
        {
            return filter == FALSE_CURSOR_FILTER
                ? EMPTY_CURSOR
                : new CursorImpl( filter );
        }


        ////////////////////////////////////////
        // Other methods
        ////////////////////////////////////////


        public String toString()
        {
            int size = edges.size();
            StringBuilder s = new StringBuilder();
            s.append( "Adj( " );
            s.append( node );
            s.append( " ): [ " );
            for( int i = 0; i < size; i++ ) {
                Graph.Edge edge = (Graph.Edge) edges.get( i );
                s.append( "(" );
                s.append( edge.getUserObject() );
                s.append( ") " );
                if( GraphUtils.equals( node, edge.getTail() ) ) {
                    s.append( edge.isDirected() ? "-> (" : "-- (" );
                    s.append( edge.getHead() );
                    s.append( ")" );
                } else {
                    s.append( edge.isDirected() ? "<- (" : "-- (" );
                    s.append( edge.getTail() );
                    s.append( ")" );
                }
                if( i < size - 1 ) {
                    s.append( ", " );
                }
            }
            s.append( " ]" );
            return s.toString();
        }


        ////////////////////////////////////////
        // Private Cursor implementation
        ////////////////////////////////////////


        private class CursorImpl
            implements Cursor
        {
            /**
             *  The predicate being used to filter this cursor.
             */
            private final CursorFilter filter;

            /**
             *  The index into the underlying list of the last element
             *  returned by next, or -1 if the iteration hasn't
             *  started yet.
             */
            private int currentIndex = -1;

            /**
             *  The index into the underlying list of the next element
             *  to be returned by next (if known), otherwise the same
             *  as currentIndex.  An index of -1 indicates that we
             *  haven't started yet; one of -2 indicates that next()
             *  has been called when there was nothing left.
             */
            private int nextIndex = -1;

            /**
             *  The edge at currentIndex, or null if it isn't valid.
             */
            private Graph.Edge currentEdge = null;

            /**
             *  The edge at nextIndex, if nextIndex represents a valid edge.
             */
            private Graph.Edge nextEdge = null;

            /**
             *  Create a new cursor.
             */
            CursorImpl( CursorFilter filter )
            {
                super();
                this.filter = filter;
                // Add this cursor to those that receive notifications
                // from the graph.
                if (cursors != null)
                	cursors.add( this );
            }

            public final boolean hasNext()
            {
                if( !isValid ) {
                    return false;
                }
                if( nextIndex != currentIndex ) {
                    return nextIndex >= 0;
                }
                int size = edges.size();
                for( int i = nextIndex + 1; i < size; i++ ) {
                    Graph.Edge edge = (Graph.Edge) edges.get( i );
                    if( filter == null || filter.evaluate( node, edge ) ) {
                        nextIndex = i;
                        nextEdge = edge;
                        return true;
                    }
                }
                return false;
            }

            public final Object next()
            {
                if( !isValid ) {
                    throw new ConcurrentModificationException();
                }
                if( !hasNext() ) {
                    nextIndex = -2;
                    currentEdge = null;
                    throw new NoSuchElementException();
                }
                currentIndex = nextIndex;
                currentEdge = nextEdge;
                return currentEdge;
            }

            public final void remove()
            {
                if( !isValid ) {
                    throw new ConcurrentModificationException();
                }
                if( currentEdge == null ) {
                    throw new IllegalStateException();
                }
                AdjacencyList.this.remove( currentIndex, currentEdge );
            }

            public final AdjacencyList getAdjacencyList()
            {
                return AdjacencyList.this;
            }

            public final Graph.Edge getCurrentEdge()
            {
                if( !isValid ) {
                    throw new ConcurrentModificationException();
                }
                if( currentEdge == null ) {
                    throw new IllegalStateException();
                }
                return currentEdge;
            }

            public final Object getOtherNode()
            {
                if( !isValid ) {
                    throw new ConcurrentModificationException();
                }
                if( currentEdge == null ) {
                    throw new IllegalStateException();
                }
                return currentEdge.getOtherEndpoint( node );
            }

            public final void removeOtherNode()
            {
                removeNode( getOtherNode() );
            }

            public final void edgeRemoved( int index )
            {
                if( index < currentIndex ) {
                    currentIndex--;
                } else if( index == currentIndex ) {
                    currentIndex--;
                    currentEdge = null;
                }

                if( index < nextIndex ) {
                    nextIndex--;
                } else if( index == nextIndex ) {
                    int size = edges.size();
                    for( int i = nextIndex; i < size; i++ ) {
                        Graph.Edge edge = (Graph.Edge) edges.get( i );
                        if( filter == null || filter.evaluate( node, edge ) ) {
                            nextIndex = i;
                            nextEdge = edge;
                            return;
                        }
                    }
                    nextIndex = currentIndex;
                }
            }
        } // end CursorImpl definition

    } // end AdjacencyList definition

}
