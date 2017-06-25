/*
 *  $Id: SingletonNodeCollection.java,v 1.4 2005/10/03 15:20:46 rconner Exp $
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

import com.phoenixst.collections.AbstractSingletonCollection;
import com.phoenixst.plexus.Graph;


/**
 *  A singleton node <code>Collection</code> view.  Since this
 *  collection is modifiable, it may be empty at any given point in
 *  time.
 *
 *  @version    $Revision: 1.4 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class SingletonNodeCollection extends AbstractSingletonCollection
{

    private final Graph graph;


    /**
     *  Creates a new <code>SingletonNodeCollection</code>.
     */
    public SingletonNodeCollection( Graph graph, Object node )
    {
        super( node );
        this.graph = graph;
    }


    public boolean isEmpty()
    {
        return !graph.containsNode( getElement() );
    }


    public boolean removeElement()
    {
        return graph.removeNode( getElement() );
    }

}
