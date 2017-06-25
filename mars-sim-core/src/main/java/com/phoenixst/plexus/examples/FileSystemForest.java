/*
 *  $Id: FileSystemForest.java,v 1.21 2006/06/21 20:15:26 rconner Exp $
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

import java.io.*;
import java.util.*;

import com.phoenixst.collections.AbstractUnmodifiableCollection;
import com.phoenixst.plexus.*;
import com.phoenixst.plexus.traversals.*;
import com.phoenixst.plexus.util.*;


/**
 *  A lazy forest graph of the local file system.  The single instance
 *  of this class should be retrieved through the {@link #getInstance}
 *  method.
 *
 *  @version    $Revision: 1.21 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class FileSystemForest extends AbstractGraph
    implements OrientedForest
{

    static final FileSystemForest FOREST_INSTANCE = new FileSystemForest();

    static final Collection NODES_INSTANCE = new NodeCollection();

    static final Collection EDGES_INSTANCE = new EdgeCollection();


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Private, this is a singleton.
     */
    private FileSystemForest()
    {
        super();
    }


    ////////////////////////////////////////
    // Instance method
    ////////////////////////////////////////


    public static final FileSystemForest getInstance()
    {
        return FOREST_INSTANCE;
    }


    ////////////////////////////////////////
    // AbstractGraph
    ////////////////////////////////////////


    protected Collection nodes()
    {
        return NODES_INSTANCE;
    }


    protected Collection edges()
    {
        return EDGES_INSTANCE;
    }


    protected Traverser traverser( Object node )
    {
        Graph.Edge parentEdge = FOREST_INSTANCE.getParentEdge( node );
        Traverser childTraverser = FOREST_INSTANCE.childTraverser( node );
        if( parentEdge == null ) {
            return childTraverser;
        }
        return new TraverserChain( GraphUtils.singletonTraverser( this,
                                                                  parentEdge.getOtherEndpoint( node ),
                                                                  parentEdge ),
                                   childTraverser );
    }


    public int degree( Object node )
    {
        File file = checkNode( node );
        File parent = file.getParentFile();
        return ((parent == null) ? 0 : 1)
            + (file.isDirectory() ? file.list().length : 0);
    }


    public boolean containsNode( Object node )
    {
        return testNode( node ) != null;
    }


    public boolean removeNode( Object node )
    {
        throw new UnsupportedOperationException();
    }


    public boolean containsEdge( Graph.Edge edge )
    {
        return FOREST_INSTANCE.isForestEdge( edge );
    }


    ////////////////////////////////////////
    // OrientedForest
    ////////////////////////////////////////


    public Object getParent( Object node )
    {
        return checkNode( node ).getParentFile();
    }


    public Traverser childTraverser( Object node )
    {
        File file = checkNode( node );
        return file.isDirectory()
            ? new ChildTraverser( file )
            : GraphUtils.EMPTY_TRAVERSER;
    }


    public Graph.Edge getParentEdge( Object node )
    {
        File file = checkNode( node );
        File parent = file.getParentFile();
        return parent == null
            ? null
            : new DefaultSimpleEdge( parent, file, true );
    }


    public boolean isForestEdge( Edge edge )
    {
        return edge.equals( getParentEdge( edge.getHead() ) );
    }


    public Object getParentEndpoint( Graph.Edge edge )
    {
        if( !isForestEdge( edge ) ) {
            throw new IllegalArgumentException( "Graph.Edge is not a forest edge: " + edge );
        }
        return edge.getTail();
    }


    public Collection rootNodes()
    {
        File[] roots = File.listRoots();
        return Collections.unmodifiableList( Arrays.asList( roots ) );
    }


    public Object getRoot( Object node )
    {
        File file = checkNode( node );
        File parent = file.getParentFile();
        while( parent != null ) {
            file = parent;
            parent = file.getParentFile();
        }
        return file;
    }


    public boolean isLeaf( Object node )
    {
        File file = checkNode( node );
        return !file.isDirectory()
            || file.list().length == 0;
    }


    public boolean isAncestor( Object ancestor, Object descendant )
    {
        return testAncestor( checkNode( ancestor ),
                             checkNode( descendant ) );
    }


    public Object getLeastCommonAncestor( Object aNode, Object bNode )
    {
        return GraphUtils.getLeastCommonAncestor( this, aNode, bNode );
    }


    public int getDepth( Object node )
    {
        int depth = 0;
        File file = checkNode( node );
        File parent = file.getParentFile();
        while( parent != null ) {
            file = parent;
            parent = file.getParentFile();
            depth++;
        }
        return depth;
    }


    public int getHeight( Object node )
    {
        int maxHeight = 0;
        int height = -1;
        for( DepthFirstTraverser t = new DepthFirstTraverser( node, this ); t.hasNext(); ) {
            t.next();
            if( t.isDescending() ) {
                height++;
                if( maxHeight < height ) {
                    maxHeight = height;
                }
            } else {
                height--;
            }
        }
        return maxHeight;
    }


    ////////////////////////////////////////
    // Helper methods
    ////////////////////////////////////////


    /**
     *  Tests whether the specified node is an existing File and
     *  returns the canonical form if it is.
     */
    static File testNode( Object node )
    {
        if( !(node instanceof File) ) {
            return null;
        }
        File file;
        try {
            file = ((File) node).getCanonicalFile();
        } catch( IOException e ) {
            return null;
        }
        if( !file.exists() ) {
            return null;
        }
        return file;
    }


    /**
     *  Returns the canonical file represented by the specified node,
     *  or throws an exception if the node is not in this graph.
     */
    static File checkNode( Object node )
    {
        File file = testNode( node );
        if( file == null ) {
            throw new NoSuchNodeException( "Node is not in this graph: " + node );
        }
        return file;
    }


    /**
     *  Tests whether descendant is beneath ancestor.  Both arguments
     *  should be canonical files.
     */
    private static boolean testAncestor( File ancestor, File descendant )
    {
        String ancestorPath = ancestor.getPath();
        if( !descendant.getPath().startsWith( ancestorPath ) ) {
            return false;
        }
        while( !descendant.equals( ancestor ) ) {
            descendant = descendant.getParentFile();
            if( descendant == null || !descendant.getPath().startsWith( ancestorPath ) ) {
                return false;
            }
        }
        return true;
    }


    ////////////////////////////////////////
    // Private classes
    ////////////////////////////////////////


    private static class ChildTraverser
        implements Traverser
    {
        private final File file;
        private final File[] fileArray;
        private int i = 0;

        ChildTraverser( File file )
        {
            super();
            this.file = file;
            fileArray = file.listFiles();
        }

        public boolean hasNext()
        {
            return i < fileArray.length;
        }

        public Object next()
        {
            if( !hasNext() ) {
                throw new NoSuchElementException();
            }
            return fileArray[i++];
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        public Graph.Edge getEdge()
        {
            if( i == 0 ) {
                throw new IllegalStateException();
            }
            return new DefaultSimpleEdge( file,
                                          fileArray[i - 1],
                                          true );
        }

        public void removeEdge()
        {
            throw new UnsupportedOperationException();
        }
    }


    private static class NodeCollection extends AbstractUnmodifiableCollection
    {
        NodeCollection()
        {
            super();
        }

        public boolean isEmpty()
        {
            return !iterator().hasNext();
        }

        public boolean contains( Object object )
        {
            return FOREST_INSTANCE.containsNode( object );
        }

        public Iterator iterator()
        {
            File[] roots = File.listRoots();
            Traverser[] traversers = new Traverser[ roots.length ];
            for( int i = 0; i < roots.length; i++ ) {
                traversers[i] = new PreOrderTraverser( roots[i], FOREST_INSTANCE );
            }
            return new TraverserNodeIteratorAdapter( new TraverserChain( traversers ) );
        }
    }


    private static class EdgeCollection extends AbstractUnmodifiableCollection
    {
        EdgeCollection()
        {
            super();
        }

        public boolean isEmpty()
        {
            return !iterator().hasNext();
        }

        public boolean contains( Object object )
        {
            return (object instanceof Graph.Edge)
                && FOREST_INSTANCE.containsEdge( (Graph.Edge) object );
        }

        public Iterator iterator()
        {
            File[] roots = File.listRoots();
            Traverser[] traversers = new Traverser[ roots.length ];
            for( int i = 0; i < roots.length; i++ ) {
                traversers[i] = new PreOrderTraverser( roots[i], FOREST_INSTANCE );
                // advance past the root node
                traversers[i].next();
            }
            return new TraverserEdgeIteratorAdapter( new TraverserChain( traversers ) );
        }
    }

}
