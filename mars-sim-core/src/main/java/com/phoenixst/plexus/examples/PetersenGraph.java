/*
 *  $Id: PetersenGraph.java,v 1.19 2006/06/20 01:09:29 rconner Exp $
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

package com.phoenixst.plexus.examples;

import java.util.*;

import com.phoenixst.plexus.*;


/**
 *  A Petersen Graph.
 *
 *  @version    $Revision: 1.19 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class PetersenGraph extends AbstractIntegerNodeGraph
{

    /**
     *  Singleton <code>PetersenGraph</code> instance.
     */
    public static final PetersenGraph INSTANCE = new PetersenGraph();


    private static final long serialVersionUID = 2L;


    /**
     *  The nodes in this graph are broken up into two sets for ease
     *  of calculation, 0-4 and 5-9.
     */
    private static final int NODE_BREAK = 5;

    /**
     *  The number of nodes in this graph.
     */
    private static final int NODE_SIZE = 10;

    /**
     *  The number of edges in this graph.
     */
    private static final int EDGE_SIZE = 15;

    /**
     *  The degree of each node in this graph.
     */
    private static final int DEGREE = 3;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>PetersenGraph</code>.  Private to preserve
     *  singleton.
     */
    private PetersenGraph()
    {
        super( NODE_SIZE );
    }


    ////////////////////////////////////////
    // Serialization
    ////////////////////////////////////////


    /**
     *  Make sure that the singleton stays that way.
     */
    private Object readResolve()
    {
        return INSTANCE;
    }


    ////////////////////////////////////////
    // Graph methods
    ////////////////////////////////////////


    /**
     *  Returns the degree of <code>node</code>, defined as the number
     *  of edges incident on <code>node</code>.
     */
    public int degree( Object node )
    {
        checkNode( node );
        return DEGREE;
    }


    ////////////////////////////////////////
    // Other methods
    ////////////////////////////////////////


    protected Graph.Edge createEdge( int tailIndex, int headIndex )
    {
        int minIndex = Math.min( tailIndex, headIndex );
        int maxIndex = Math.max( tailIndex, headIndex );

        if( minIndex < NODE_BREAK ) {
            if( maxIndex < NODE_BREAK ) {
                if( minIndex + 1 == maxIndex ) {
                    return new EdgeImpl( minIndex, maxIndex, false );
                } else if( minIndex + 4 == maxIndex ) {
                    return new EdgeImpl( maxIndex, minIndex, false );
                }
            } else {
                if( minIndex + NODE_BREAK == maxIndex ) {
                    return new EdgeImpl( minIndex, maxIndex, false );
                }
            }
        } else {
            if( minIndex + 2 == maxIndex ) {
                return new EdgeImpl( minIndex, maxIndex, false );
            } else if( minIndex + 3 == maxIndex ) {
                return new EdgeImpl( maxIndex, minIndex, false );
            }
        }

        return null;
    }


    protected Collection createEdgeCollection()
    {
        return new EdgeCollection();
    }


    protected Traverser createTraverser( int nodeIndex )
    {
        return new TraverserImpl( nodeIndex );
    }


    public String toString()
    {
        return "Petersen Graph";
    }


    ////////////////////////////////////////
    // Private classes
    ////////////////////////////////////////


    private class EdgeCollection extends AbstractIntegerEdgeCollection
    {
        EdgeCollection()
        {
            super();
        }

        public int size()
        {
            return EDGE_SIZE;
        }

        public Iterator iterator()
        {
            return new EdgeIterator();
        }
    }


    private class EdgeIterator
        implements Iterator
    {
        private int i = 0;

        EdgeIterator()
        {
            super();
        }

        public boolean hasNext()
        {
            return i < EDGE_SIZE;
        }

        public Object next()
        {
            i++;
            if( i <= NODE_BREAK ) {
                return createEdge( i - 1, i % NODE_BREAK );
            } else if( i <= 2 * NODE_BREAK ) {
                return createEdge( i - 1, ((i + 1) % NODE_BREAK) + NODE_BREAK );
            } else if( i <= 3 * NODE_BREAK ) {
                return createEdge( (i - 1) - 2 * NODE_BREAK, (i - 1) - NODE_BREAK );
            } else {
                throw new NoSuchElementException();
            }
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }


    private class TraverserImpl
        implements Traverser
    {
        private final int nodeIndex;
        private final int node1;
        private final int node2;
        private final int node3;
        private int i = 0;

        TraverserImpl( int nodeIndex )
        {
            super();
            this.nodeIndex = nodeIndex;
            if( nodeIndex < NODE_BREAK ) {
                node1 = (nodeIndex + 1) % NODE_BREAK;
                node2 = (nodeIndex + NODE_BREAK - 1) % NODE_BREAK;
                node3 = nodeIndex + 5;
            } else {
                node1 = ((nodeIndex + 2) % NODE_BREAK) + NODE_BREAK;
                node2 = ((nodeIndex + NODE_BREAK - 2) % NODE_BREAK) + NODE_BREAK;
                node3 = nodeIndex - 5;
            }
        }

        public boolean hasNext()
        {
            return i < DEGREE;
        }

        public Object next()
        {
            i++;
            if( i == 1 ) {
                return new Integer( node1 );
            } else if( i == 2 ) {
                return new Integer( node2 );
            } else if( i == 3 ) {
                return new Integer( node3 );
            } else {
                throw new NoSuchElementException();
            }
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        public Graph.Edge getEdge()
        {
            if( i == 1 ) {
                return createEdge( nodeIndex, node1 );
            } else if( i == 2 ) {
                return createEdge( nodeIndex, node2 );
            } else if( i == 3 ) {
                return createEdge( nodeIndex, node3 );
            } else {
                throw new IllegalStateException();
            }
        }

        public void removeEdge()
        {
            throw new UnsupportedOperationException();
        }
    }

}
