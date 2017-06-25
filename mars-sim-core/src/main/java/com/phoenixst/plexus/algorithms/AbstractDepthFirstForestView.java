/*
 *  $Id: AbstractDepthFirstForestView.java,v 1.20 2005/10/03 17:02:41 rconner Exp $
 *
 *  Copyright (C) 1994-2005 by Phoenix Software Technologists,
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

package com.phoenixst.plexus.algorithms;

import java.util.*;

import org.apache.commons.collections.*;
import org.apache.log4j.Logger;

import com.phoenixst.collections.OrderedPair;
import com.phoenixst.plexus.*;
import com.phoenixst.plexus.traversals.DepthFirstTraverser;
import com.phoenixst.plexus.util.FilteredTraverser;


/**
 *  An abstract constructive (<strong>not</strong> lazy) depth-first
 *  tree or forest for a <code>Graph</code>.
 *
 *  <P>This implementation tracks discovery time and finishing time,
 *  and can possibly answer a few structural questions about the
 *  underlying <code>Graph</code>.  Whether or not these questions can
 *  be answered depends upon whether the supplied
 *  <code>Traverser</code> predicate or factory is <em>direction
 *  agnostic</em>.  If at least one encountered edge can be traversed
 *  in only one direction, then many structural queries cannot be
 *  answered by this class, and will throw exceptions.  The only
 *  exception is in the case of self-loops; these may only be
 *  traversed in one direction with no ill effect.  These cases are
 *  documented in the appropriate methods.
 *
 *  <P>If the underlying <code>Graph</code> changes, this view may
 *  become invalid, but perhaps not detectably so.
 *
 *  @version    $Revision: 1.20 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
abstract class AbstractDepthFirstForestView extends AbstractOrientedForest
    implements GraphView
{

    static final String NODE_NOT_PRESENT_MESSAGE = "Node is not in this view: ";

    /**
     *  The Graph of which this is a view.
     */
    private final Graph graph;

    /**
     *  The factory for producing new Traversers.
     */
    private final Transformer traverserFactory;

    /**
     *  The logger for this view.
     */
    private final Logger logger;

    /**
     *  Maps Graph nodes to NodeRecords.
     */
    private final Map nodeMap = new HashMap();

    /**
     *  Child traverser predicate.
     */
    private final Predicate childTraverserPredicate = new ChildTraverserPredicate( this );

    /**
     *  Whether or not the traversal was cyclic.
     */
    private boolean cyclic = false;

    /**
     *  Whether or not the traversal was direction agnostic.
     */
    private boolean directionAgnostic = true;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>AbstractDepthFirstForestView</code>.
     */
    AbstractDepthFirstForestView( Graph graph,
                                  Transformer traverserFactory,
                                  Logger logger )
    {
        super();
        this.graph = graph;
        this.traverserFactory = traverserFactory;
        this.logger = logger;
        if( graph == null ) {
            throw new IllegalArgumentException( "Graph is null." );
        }
        if( traverserFactory == null ) {
            throw new IllegalArgumentException( "Traverser Factory is null." );
        }
        if( logger == null ) {
            throw new IllegalArgumentException( "Logger is null." );
        }
    }


    ////////////////////////////////////////
    // Called by Subclass Constructors
    ////////////////////////////////////////


    /**
     *  This method is quite lengthy and does a lot, so I thought some
     *  documentation might be in order.  There are six different
     *  possible cases to handle for each edge traversed by the
     *  DepthFirstTraverser.  Since we make no assumptions about what
     *  kind of traversal the user has given us, we have to handle all
     *  possibilities.
     *
     *  Most importantly, we don't know ahead of time whether the
     *  traversal is direction agnostic, as defined in the class docs
     *  above.  So we have to do some bookkeeping during the traversal
     *  to figure this out.
     *
     *  The six possible cases are:
     *
     *    TREE - This is the first time the edge and its other
     *    endpoint have been encountered.
     *
     *    PARENT - The edge is the same as the TREE edge we just
     *    traversed to get here.
     *
     *    BACK - The edge goes back to a direct ancestor.
     *
     *    SELF - The edge goes from a node to itself.  This is
     *    technically considered to be a back edge, although the code
     *    for handling it is different.
     *
     *    FORWARD - The edge goes forward to a descendant that has
     *    previously been discovered.
     *
     *    CROSS - All other edges.  These are any edges whose
     *    endpoints do not have an ancestor/descendant relationship.
     *
     *  For the traversal to be direction agnostic, the following
     *  conditions must hold:
     *
     *  - Every TREE edge must later be traversed as a PARENT edge.
     *  - Every BACK edge must later be traversed as a FORWARD edge.
     *  - Every FORWARD edge must have been previously traversed as a
     *    BACK edge.
     *  - There cannot be any CROSS edges.
     *
     *  To determine this, we simply track every edge that is
     *  encountered to make sure that it is seen again.  One way to do
     *  this would be to just use a big Set, but that wouldn't be very
     *  efficient.  We can make significant gains by no longer
     *  tracking this information once we know the point is moot.  We
     *  really only need to know if an encountered edge will be/has
     *  been also encountered from the other endpoint.  Ironically, we
     *  can use a graph to track this information.  In this
     *  info-tracking graph, there is an edge from each NodeRecord to
     *  each incident edge which has yet to be traversed in the other
     *  direction.
     */
    int visitTree( Object root, int time )
    {
        if( logger.isDebugEnabled() ) {
            logger.debug( "Visiting new root node: " + root + ", time = " + time );
        }

        // edgeGraph will be reset to null to signify that the
        // traversal is not direction agnostic and we therefore don't
        // need to track this anymore.
        Graph edgeGraph = new DefaultGraph();

        for( DepthFirstTraverser t = new DepthFirstTraverser( root, traverserFactory );
             t.hasNext(); ) {
            Object node = t.next();
            Graph.Edge edge = t.getEdge();
            NodeRecord nodeRec = (NodeRecord) nodeMap.get( node );

            if( logger.isDebugEnabled() ) {
                logger.debug( "Traversing node, edge: " + node + ", " + edge );
            }

            if( t.isDescending() ) {

                if( nodeRec == null ) {
                    nodeRec = new NodeRecord( node, edge, ++time );
                    nodeMap.put( node, nodeRec );
                    if( logger.isDebugEnabled() ) {
                        logger.debug( "  Tree edge, discovery time = " + time );
                    }

                    if( edgeGraph != null ) {
                        edgeGraph.addNode( nodeRec );
                        if( edge != null ) {
                            edgeGraph.addNode( edge );
                            edgeGraph.addEdge( null, nodeRec, edge, false );
                        }
                    }

                } else {
                    // node has been seen before
                    t.prune();

                    NodeRecord fromRec = (NodeRecord) nodeMap.get( edge.getOtherEndpoint( node ) );

                    if( nodeRec == fromRec ) {
                        logger.debug( "  Self edge, graph is cyclic." );
                        cyclic = true;

                    } else if( nodeRec.finishingTime == 0 ) {
                        if( edge.equals( fromRec.parentEdge ) ) {
                            logger.debug( "  Parent edge." );
                            if( edgeGraph != null ) {
                                edgeGraph.removeNode( edge );
                            }
                        } else {
                            logger.debug( "  Back edge, graph is cyclic." );
                            cyclic = true;
                            if( edgeGraph != null ) {
                                edgeGraph.addNode( edge );
                                edgeGraph.addEdge( null, nodeRec, edge, false );
                                if( fromRec.reachableAncestor.discoveryTime > nodeRec.discoveryTime ) {
                                    logger.debug( "    Setting reachable ancestor of other end." );
                                    fromRec.reachableAncestor = nodeRec;
                                }
                            }
                        }

                    } else {
                        if( nodeRec.discoveryTime > fromRec.discoveryTime ) {
                            logger.debug( "  Forward edge." );
                            if( edgeGraph != null && !edgeGraph.removeNode( edge ) ) {
                                logger.debug( "    Forward edge not seen before, graph is not direction agnostic." );
                                edgeGraph = null;
                            }
                        } else {
                            logger.debug( "  Cross edge, graph is not direction agnostic." );
                            edgeGraph = null;
                        }
                    }
                }

            } else {
                // ascending, we're finishing this node
                nodeRec.finishingTime = ++time;
                if( logger.isDebugEnabled() ) {
                    logger.debug( "  Done with node, finishing time = " + time );
                }
                if( edgeGraph != null ) {
                    if( edgeGraph.getIncidentEdge( nodeRec, null ) == null ) {
                        edgeGraph.removeNode( nodeRec );
                        if( edge != null ) {
                            NodeRecord parentRec = (NodeRecord) nodeMap.get( edge.getOtherEndpoint( node ) );
                            // if this subtree cannot reach back
                            // further than the parent, then the
                            // parent is an articulation point
                            if( nodeRec.reachableAncestor.discoveryTime >= parentRec.discoveryTime ) {
                                logger.debug( "    Parent is an articulation point." );
                                parentRec.isArticulationPoint = true;
                            } else if( parentRec.reachableAncestor.discoveryTime > nodeRec.reachableAncestor.discoveryTime ) {
                                logger.debug( "    Setting parent's reachable ancestor." );
                                parentRec.reachableAncestor = nodeRec.reachableAncestor;
                            }
                        }
                    } else {
                        logger.debug( "  Node has unhandled edges, graph is not direction agnostic." );
                        edgeGraph = null;
                    }
                }
            }
        }

        if( edgeGraph == null ) {
            directionAgnostic = false;
        } else {
            // Root is an articulation point iff it has 2+ children
            // in the tree.
            NodeRecord startRec = (NodeRecord) nodeMap.get( root );
            startRec.isArticulationPoint = false;
            int count = 0;
            for( Traverser t = (Traverser) traverserFactory.transform( root );
                 t.hasNext(); ) {
                NodeRecord adjRec = (NodeRecord) nodeMap.get( t.next() );
                if( t.getEdge().equals( adjRec.parentEdge ) && ++count >= 2 ) {
                    logger.debug( "Root is an articulation point." );
                    startRec.isArticulationPoint = true;
                    break;
                }
            }
        }

        // Help the GC out a little
        edgeGraph = null;

        return time;
    }


    ////////////////////////////////////////
    // Methods for Subclasses
    ////////////////////////////////////////


    protected boolean hasProcessedNode( Object node )
    {
        return nodeMap.containsKey( node );
    }


    ////////////////////////////////////////
    // GraphView
    ////////////////////////////////////////


    public Graph getGraph()
    {
        return graph;
    }


    ////////////////////////////////////////
    // OrientedForest
    ////////////////////////////////////////


    /*
     *  Inherited from AbstractOrientedForest:
     *    getParent
     *    isForestEdge
     *    getParentEndpoint
     *    getRoot
     *    getDepth
     *    getHeight
     */


    public Graph.Edge getParentEdge( Object node )
    {
        NodeRecord nodeRec = (NodeRecord) nodeMap.get( node );
        if( nodeRec == null ) {
            throw new NoSuchNodeException( NODE_NOT_PRESENT_MESSAGE + node );
        }
        return nodeRec.parentEdge;
    }


    public Traverser childTraverser( Object node )
    {
        NodeRecord nodeRec = (NodeRecord) nodeMap.get( node );
        if( nodeRec == null ) {
            throw new NoSuchNodeException( NODE_NOT_PRESENT_MESSAGE + node );
        }
        if( nodeRec.finishingTime == nodeRec.discoveryTime + 1 ) {
            return GraphUtils.EMPTY_TRAVERSER;
        }
        return new FilteredTraverser( (Traverser) traverserFactory.transform( node ),
                                      childTraverserPredicate );
    }


    public boolean isLeaf( Object node )
    {
        NodeRecord nodeRec = (NodeRecord) nodeMap.get( node );
        if( nodeRec == null ) {
            throw new NoSuchNodeException( NODE_NOT_PRESENT_MESSAGE + node );
        }
        return nodeRec.finishingTime == nodeRec.discoveryTime + 1;
    }


    public boolean isAncestor( Object ancestor, Object descendant )
    {
        NodeRecord ancestorRec = (NodeRecord) nodeMap.get( ancestor );
        if( ancestorRec == null ) {
            throw new NoSuchNodeException( NODE_NOT_PRESENT_MESSAGE + ancestor );
        }
        NodeRecord descendantRec = (NodeRecord) nodeMap.get( descendant );
        if( descendantRec == null ) {
            throw new NoSuchNodeException( NODE_NOT_PRESENT_MESSAGE + descendant );
        }
        return ancestorRec.discoveryTime <= descendantRec.discoveryTime
            && ancestorRec.finishingTime >= descendantRec.finishingTime;
    }


    public Object getLeastCommonAncestor( Object aNode, Object bNode )
    {
        NodeRecord aRec = (NodeRecord) nodeMap.get( aNode );
        if( aRec == null ) {
            throw new NoSuchNodeException( NODE_NOT_PRESENT_MESSAGE + aNode );
        }
        NodeRecord bRec = (NodeRecord) nodeMap.get( bNode );
        if( bRec == null ) {
            throw new NoSuchNodeException( NODE_NOT_PRESENT_MESSAGE + bNode );
        }
        if( aRec == bRec ) {
            return aRec.node;
        }
        // reorder so that aRec has the least discovery time
        if( aRec.discoveryTime > bRec.discoveryTime ) {
            NodeRecord temp = aRec;
            aRec = bRec;
            bRec = temp;
        }
        while( aRec.finishingTime < bRec.finishingTime ) {
            aRec = (NodeRecord) nodeMap.get( aRec.parentEdge.getOtherEndpoint( aRec.node ) );
        }
        return aRec.node;
    }


    ////////////////////////////////////////
    // Other methods
    ////////////////////////////////////////


    /**
     *  Returns the &quot;time&quot; that the specified node was first
     *  discovered during the depth-first traversal.  The discovery
     *  time of the first node encountered starts at one and
     *  increments by one for each traversal step taken after that.
     */
    public int getDiscoveryTime( Object node )
    {
        NodeRecord nodeRec = (NodeRecord) nodeMap.get( node );
        if( nodeRec == null ) {
            throw new NoSuchNodeException( NODE_NOT_PRESENT_MESSAGE + node );
        }
        return nodeRec.discoveryTime;
    }


    /**
     *  Returns the &quot;time&quot; that the specified node was
     *  finished during the depth-first traversal.  The discovery time
     *  time of the first node encountered starts at one and
     *  increments by one for each traversal step taken after that.
     */
    public int getFinishingTime( Object node )
    {
        NodeRecord nodeRec = (NodeRecord) nodeMap.get( node );
        if( nodeRec == null ) {
            throw new NoSuchNodeException( NODE_NOT_PRESENT_MESSAGE + node );
        }
        return nodeRec.finishingTime;
    }


    /**
     *  Returns whether or not this traversal was cyclic.
     *  Specifically, returns <code>true</code> if and only if a
     *  self-loop or back edge was encountered during the traversal.
     */
    public boolean isCyclic()
    {
        return cyclic;
    }


    /**
     *  Returns whether or not this traversal was direction agnostic,
     *  as defined in the class docs.
     */
    public boolean isDirectionAgnostic()
    {
        return directionAgnostic;
    }


    /**
     *  Returns whether or not the specified node is an articulation
     *  point of this view.  An articulation point (also called a cut
     *  vertex) is a node whose removal would increase the number of
     *  components in the graph.  For this view, the question is
     *  restricted to whether removing the node would increase the
     *  number of components in the graph covered by the traversal.
     *
     *  <P>If this view is not direction agnostic, throws an
     *  <code>IllegalStateException</code>.  If the specified node was
     *  not reached during traversal, throws a
     *  <code>NoSuchNodeException</code>.
     *
     *  @param node the node to test for being an articulation point.
     *
     *  @return whether or not the specified node is an articulation
     *  point of this view.
     *
     *  @throws IllegalStateException if this view is not direction
     *  agnostic.
     *
     *  @throws NoSuchNodeException if the specified node was not
     *  reached during traversal.
     */
    public boolean isArticulationPoint( Object node )
    {
        if( !directionAgnostic ) {
            throw new IllegalStateException( "This traversal is not direction agnostic." );
        }
        NodeRecord nodeRec = (NodeRecord) nodeMap.get( node );
        if( nodeRec == null ) {
            throw new NoSuchNodeException( NODE_NOT_PRESENT_MESSAGE + node );
        }
        return nodeRec.isArticulationPoint;
    }


    /**
     *  Returns whether or not the specified <code>Edge</code> is a
     *  bridge of this view.  A bridge is an <code>Edge</code> whose
     *  removal would increase the number of components in the graph.
     *  For this view, the question is restricted to whether removing
     *  the <code>Edge</code> would increase the number of components
     *  in the graph covered by the traversal.
     *
     *  <P>If this view is not direction agnostic, throws an
     *  <code>IllegalStateException</code>.  If the specified
     *  <code>Edge</code> is not in the <code>Graph</code>, throws an
     *  <code>IllegalArgumentException</code>.
     *
     *  @param edge the <code>Edge</code> to test for being a bridge.
     *
     *  @return whether or not the specified <code>Edge</code> is a
     *  bridge of this view.
     *
     *  @throws IllegalStateException if this view is not direction
     *  agnostic.
     *
     *  @throws IllegalArgumentException if the specified
     *  <code>Edge</code> is not in the <code>Graph</code>.
     */
    public boolean isBridge( Graph.Edge edge )
    {
        if( !directionAgnostic ) {
            throw new IllegalStateException( "This traversal is not direction agnostic." );
        }
        if( !graph.containsEdge( edge ) ) {
            throw new IllegalArgumentException( "Edge is not in this graph: " + edge );
        }

        // An edge is a bridge if it is a tree edge and there is no
        // back edge from the descendant side to the ancestor side.
        // This is just another way of saying that the edge doesn't
        // lie on a cycle.

        NodeRecord tailRec = (NodeRecord) nodeMap.get( edge.getTail() );
        if( tailRec == null ) {
            return false;
        } else if( edge.equals( tailRec.parentEdge ) ) {
            return tailRec.reachableAncestor == tailRec;
        }

        NodeRecord headRec = (NodeRecord) nodeMap.get( edge.getHead() );
        return headRec != null
            && edge.equals( headRec.parentEdge )
            && headRec.reachableAncestor == headRec;
    }


    ////////////////////////////////////////
    // Private classes
    ////////////////////////////////////////


    private static class NodeRecord
    {
        final Object node;
        final Graph.Edge parentEdge;
        final int discoveryTime;
        int finishingTime = 0;
        boolean isArticulationPoint = false;

        // The highest ancestor in the DFS tree reachable from any
        // descendant (including this node) through a single back
        // edge.  In other words, the highest node reachable by going
        // down the tree and then making a final jump up through a
        // back edge.  Note that this is only tracked if the traversal
        // is direction agnostic.
        NodeRecord reachableAncestor = this;

        NodeRecord( Object node, Graph.Edge parentEdge, int discoveryTime )
        {
            super();
            this.node = node;
            this.parentEdge = parentEdge;
            this.discoveryTime = discoveryTime;
        }
    }


    private static class ChildTraverserPredicate
        implements Predicate
    {
        private final OrientedForest forest;

        ChildTraverserPredicate( OrientedForest forest )
        {
            super();
            this.forest = forest;
        }

        public boolean evaluate( Object object )
        {
            OrderedPair pair = (OrderedPair) object;
            Object baseNode = pair.getFirst();
            Graph.Edge edge = (Graph.Edge) pair.getSecond();
            return edge.equals( forest.getParentEdge( edge.getOtherEndpoint( baseNode ) ) );
        }
    }

}
