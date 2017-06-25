/*
 *  $Id: RandomGraphFactory.java,v 1.9 2005/10/03 15:14:43 rconner Exp $
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

package com.phoenixst.plexus.examples;

import java.util.Random;

import org.apache.commons.collections.Predicate;

import com.phoenixst.plexus.*;


/**
 *  This class contains static factory methods for creating random
 *  graphs.
 *
 *  @version    $Revision: 1.9 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class RandomGraphFactory
{

    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Prevent instantiation.
     */
    private RandomGraphFactory()
    {
        super();
    }


    ////////////////////////////////////////
    // Factory Methods
    ////////////////////////////////////////


    /**
     *  Creates a random graph with <code>n</code> nodes where each
     *  pair of nodes has probability <code>prob</code> of having an
     *  edge between them.
     */
    public static Graph createStandardGraph( int n, double prob )
    {
        if( prob < 0.0 || prob > 1.0 ) {
            throw new IllegalArgumentException( "Probability must be between 0.0 and 1.0, inclusive." );
        }
        Graph graph = new DefaultGraph( new EmptyGraph( n ) );
        for( int head = 1; head < n; head++ ) {
            Object headNode = new Integer( head );
            for( int tail = 0; tail < head; tail++ ) {
                if( Math.random() < prob ) {
                    graph.addEdge( null, new Integer( tail ), headNode, true );
                }
            }
        }
        return graph;
    }


    /**
     *  Creates a random graph according to the Watts-Strogatz model.
     *  The number <code>d</code> here is half of <code>K</code> in
     *  the standard literature.
     *
     *  <P>Start with a circulant graph.  Arrange the nodes in a
     *  circle, starting at 0 and increasing, in order, clockwise.
     *  Begin with node 0 and the edge which connects it to its
     *  nearest clockwise neighbor, which is node 1.  With probability
     *  <code>prob</code>, reconnect this edge from node 0 to a
     *  uniformly randomly selected node, with duplicate and self edges
     *  forbidden.  Repeat this process for each node, moving
     *  clockwise around the circle.  Now, repeat the entire cycle,
     *  but instead choose edges which connect nodes to their
     *  second-nearest clockwise neighbor.  And so on, until every one
     *  of the original edges has been considered.
     */
    public static Graph createWattsStrogatz( int n, int d, double prob )
    {
        if( prob < 0.0 || prob > 1.0 ) {
            throw new IllegalArgumentException( "Probability must be between 0.0 and 1.0, inclusive." );
        }
        Graph graph = new DefaultGraph( new CirculantGraph( n, d ) );
        Random random = new Random();
        for( int dist = 1; dist <= d; dist++ ) {
            for( int nodeIndex = 0; nodeIndex < n; nodeIndex++ ) {
                if( random.nextDouble() < prob ) {
                    Object tail = new Integer( nodeIndex );
                    Object head = new Integer( (nodeIndex + dist) % n );
                    Predicate edgePred = EdgePredicateFactory.createEqualsNodes( tail, head,
                                                                                 GraphUtils.ANY_DIRECTION_MASK );
                    graph.removeEdge( graph.getEdge( edgePred ) );
                    while( true ) {
                        head = new Integer( random.nextInt( n ) );
                        Predicate traverserPred = TraverserPredicateFactory.createEqualsNode( head,
                                                                                              GraphUtils.ANY_DIRECTION_MASK );
                        if( !tail.equals( head )
                            && graph.getIncidentEdge( tail, traverserPred ) == null ) {
                            graph.addEdge( null, tail, head, true );
                            break;
                        }
                    }
                }
            }
        }
        return graph;
    }


    /**
     *  Creates a random graph according to the Barabasi-Albert model.
     *
     *  <P>Start with <code>numInitialNodes</code> nodes.  At each
     *  step, add a new node which is connected to
     *  <code>numEdges</code> existing nodes, with preference given to
     *  nodes that are more highly connected.
     */
    public static Graph createBarabasiAlbert( int numInitialNodes,
                                              int numFinalNodes,
                                              int numEdges )
    {
        if( numEdges > numInitialNodes ) {
            throw new IllegalArgumentException( "Number of edges cannot be more than the number of initial nodes." );
        }
        Graph graph = new DefaultGraph( new EmptyGraph( numInitialNodes + 1 ) );
        Random random = new Random();

        // Start out with numEdges randomly connected from the last
        // node to other nodes.
        Object tail = new Integer( numInitialNodes );
        for( int k = 0; k < numEdges; k++ ) {
            while( true ) {
                Object head = new Integer( random.nextInt( numInitialNodes ) );
                Predicate traverserPred = TraverserPredicateFactory.createEqualsNode( head,
                                                                                      GraphUtils.ANY_DIRECTION_MASK );
                if( graph.getIncidentEdge( tail, traverserPred ) == null ) {
                    graph.addEdge( null, tail, head, true );
                    break;
                }
            }
        }

        // Now, do the rest of the work
        int totalDegree = numEdges * 2;
        for( int i = numInitialNodes + 1; i < numFinalNodes; i++, totalDegree += numEdges * 2 ) {
            tail = new Integer( i );
            graph.addNode( tail );
            for( int k = 0; k < numEdges; k++ ) {
                while( true ) {
                    int randomDegreeSum = random.nextInt( totalDegree );
                    Object head = new Integer( 0 );
                    int degreeSum = 0;
                    for( int headIndex = 0; degreeSum < randomDegreeSum; headIndex++ ) {
                        head = new Integer( headIndex );
                        degreeSum += graph.degree( head );
                    }
                    Predicate traverserPred = TraverserPredicateFactory.createEqualsNode( head,
                                                                                          GraphUtils.ANY_DIRECTION_MASK );
                    if( graph.getIncidentEdge( tail, traverserPred ) == null ) {
                        graph.addEdge( null, tail, head, true );
                        break;
                    }
                }
            }
        }
        return graph;
    }

}
