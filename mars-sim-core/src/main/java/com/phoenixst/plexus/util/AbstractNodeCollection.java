/*
 *  $Id: AbstractNodeCollection.java,v 1.6 2005/10/03 15:20:46 rconner Exp $
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

package com.phoenixst.plexus.util;

import java.util.*;

import com.phoenixst.plexus.Graph;


/**
 *  An abstract <code>Collection</code> for nodes to help implement
 *  the {@link Graph#nodes Graph.nodes( Predicate )} method.
 *
 *  @version    $Revision: 1.6 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public abstract class AbstractNodeCollection extends AbstractCollection
{

    private final Graph graph;


    /**
     *  Creates a new <code>AbstractNodeCollection</code>.
     */
    protected AbstractNodeCollection( Graph graph )
    {
        super();
        this.graph = graph;
    }


    /**
     *  This implementation counts the number of elements accessed by
     *  the <code>iterator()</code> method.
     */
    public int size()
    {
        int size = 0;
        for( Iterator i = iterator(); i.hasNext(); ) {
            i.next();
            size++;
        }
        return size;
    }


    /**
     *  This implementation returns <code>true</code> if the
     *  <code>iterator().hasNext()</code> returns <code>false</code>.
     */
    public boolean isEmpty()
    {
        return !iterator().hasNext();
    }


    /**
     *  This implementation delegates to {@link Graph#removeNode
     *  Graph.removeNode( Object )}.
     */
    public boolean remove( Object object )
    {
        return graph.removeNode( object );
    }


    /**
     *  This implementation delegates to {@link Graph#containsNode
     *  Graph.containsNode( Object )}.
     */
    public boolean contains( Object object )
    {
        return graph.containsNode( object );
    }


    /**
     *  Throws an <code>UnsupportedOperationException</code>.
     */
    public boolean addAll( Collection collection )
    {
        throw new UnsupportedOperationException();
    }

}
