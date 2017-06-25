/*
 *  $Id: ForestTreeAdapter.java,v 1.9 2005/10/03 15:24:00 rconner Exp $
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

package com.phoenixst.plexus;

import java.util.*;


/**
 *  An adapter which presents the subgraph of an {@link
 *  OrientedForest} rooted at a specified node as a {@link
 *  RootedTree}.
 *
 *  @version    $Revision: 1.9 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class ForestTreeAdapter
    implements RootedTree
{

    private static final String NODE_NOT_PRESENT_MESSAGE = "Node is not in this tree: ";

    private Object root;

    private final OrientedForest forest;

    private final boolean isStrict;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>ForestTreeAdapter</code>.
     *
     *  @param root create a tree of the subgraph rooted at this
     *  node.
     *
     *  @param forest the forest of which this tree is a subview.
     *
     *  @param isStrict if <code>true</code>, all appropriate methods
     *  accepting nodes will throw a <code>NoSuchNodeException</code>
     *  if the argument node is not a descendant of the root node of
     *  this tree.  If <code>false</code>, only {@link
     *  #getRoot(Object) getRoot( node )} and {@link #getDepth
     *  getDepth( node )} will throw an exception in this case.
     */
    public ForestTreeAdapter( Object root,
                              OrientedForest forest,
                              boolean isStrict )
    {
        super();
        this.root = root;
        this.forest = forest;
        this.isStrict = isStrict;

        // Make sure the forest contains the specified root node.
        forest.getParentEdge( root );
    }


    ////////////////////////////////////////
    // Rooted
    ////////////////////////////////////////


    public Object getRoot()
    {
        return root;
    }


    public void setRoot( Object root )
    {
        // Make sure the forest contains the specified root node.
        forest.getParentEdge( root );
        this.root = root;
    }


    ////////////////////////////////////////
    // OrientedForest
    ////////////////////////////////////////


    public Object getParent( Object node )
    {
        if( isStrict && !isTreeNode( node ) ) {
            throw new NoSuchNodeException( NODE_NOT_PRESENT_MESSAGE + node );
        }
        if( GraphUtils.equals( root, node ) ) {
            return null;
        }
        return forest.getParent( node );
    }


    public Traverser childTraverser( Object node )
    {
        if( isStrict && !isTreeNode( node ) ) {
            throw new NoSuchNodeException( NODE_NOT_PRESENT_MESSAGE + node );
        }
        return forest.childTraverser( node );
    }


    public Graph.Edge getParentEdge( Object node )
    {
        if( isStrict && !isTreeNode( node ) ) {
            throw new NoSuchNodeException( NODE_NOT_PRESENT_MESSAGE + node );
        }
        if( GraphUtils.equals( root, node ) ) {
            return null;
        }
        return forest.getParentEdge( node );
    }


    public boolean isForestEdge( Graph.Edge edge )
    {
        return forest.isForestEdge( edge )
            && isTreeNode( forest.getParentEndpoint( edge ) );
    }


    public Object getParentEndpoint( Graph.Edge edge )
    {
        Object parent = forest.getParentEndpoint( edge );
        if( !isTreeNode( parent ) ) {
            throw new IllegalArgumentException( "Edge is not a tree edge: " + edge );
        }
        return parent;
    }


    public Collection rootNodes()
    {
        return Collections.singleton( root );
    }


    public Object getRoot( Object node )
    {
        if( !isTreeNode( node ) ) {
            throw new NoSuchNodeException( NODE_NOT_PRESENT_MESSAGE + node );
        }
        return root;
    }


    public boolean isLeaf( Object node )
    {
        if( isStrict && !isTreeNode( node ) ) {
            throw new NoSuchNodeException( NODE_NOT_PRESENT_MESSAGE + node );
        }
        return forest.isLeaf( node );
    }


    public boolean isAncestor( Object ancestor, Object descendant )
    {
        boolean isAncestor = forest.isAncestor( ancestor, descendant );
        if( isStrict ) {
            if( !isTreeNode( ancestor ) ) {
                throw new NoSuchNodeException( NODE_NOT_PRESENT_MESSAGE + ancestor );
            }
            if( !isAncestor ) {
                // we need to check descendant, too
                if( !isTreeNode( descendant ) ) {
                    throw new NoSuchNodeException( NODE_NOT_PRESENT_MESSAGE + descendant );
                }
            }
        }
        return isAncestor;
    }


    public Object getLeastCommonAncestor( Object aNode, Object bNode )
    {
        Object ancestor = forest.getLeastCommonAncestor( aNode, bNode );
        if( isStrict ) {
            if( ancestor != null ) {
                if( !isTreeNode( ancestor ) ) {
                    throw new NoSuchNodeException( NODE_NOT_PRESENT_MESSAGE + ancestor );
                }
            } else {
                // It may be that they have no LCA, or it may be that
                // they have one and it is null.  In either case, we
                // must check both nodes for being tree nodes.
                if( !isTreeNode( aNode ) ) {
                    throw new NoSuchNodeException( NODE_NOT_PRESENT_MESSAGE + aNode );
                }
                if( !isTreeNode( bNode ) ) {
                    throw new NoSuchNodeException( NODE_NOT_PRESENT_MESSAGE + bNode );
                }
            }
        }
        return ancestor;
    }


    public int getDepth( Object node )
    {
        if( GraphUtils.equals( root, node ) ) {
            return 0;
        }
        Object testNode = node;
        Graph.Edge edge = forest.getParentEdge( testNode );
        for( int depth = 0; edge != null; depth++ ) {
            if( GraphUtils.equals( root, testNode ) ) {
                return depth;
            }
            testNode = edge.getOtherEndpoint( testNode );
            edge = forest.getParentEdge( testNode );
        }
        throw new NoSuchNodeException( NODE_NOT_PRESENT_MESSAGE + node );
    }


    public int getHeight( Object node )
    {
        if( isStrict && !isTreeNode( node ) ) {
            throw new NoSuchNodeException( NODE_NOT_PRESENT_MESSAGE + node );
        }
        return forest.getHeight( node );
    }


    ////////////////////////////////////////
    // RootedTree
    ////////////////////////////////////////


    public boolean isTreeNode( Object node )
    {
        return forest.isAncestor( root, node );
    }

}
