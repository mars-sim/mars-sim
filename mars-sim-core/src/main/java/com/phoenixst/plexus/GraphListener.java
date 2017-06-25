/*
 *  $Id: GraphListener.java,v 1.5 2004/05/06 15:20:24 rconner Exp $
 *
 *  Copyright (C) 1994-2004 by Phoenix Software Technologists,
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

import java.util.EventListener;


/**
 *  The listener interface for receiving {@link GraphEvent
 *  GraphEvents}.
 *
 *  @version    $Revision: 1.5 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public interface GraphListener extends EventListener
{

    /**
     *  Invoked when a node has been added to the <code>Graph</code>.
     */
    public void nodeAdded( GraphEvent event );


    /**
     *  Invoked when a node has been removed from the
     *  <code>Graph</code>.
     */
    public void nodeRemoved( GraphEvent event );


    /**
     *  Invoked when an edge has been added to the <code>Graph</code>.
     */
    public void edgeAdded( GraphEvent event );


    /**
     *  Invoked when an edge has been removed from the
     *  <code>Graph</code>.
     */
    public void edgeRemoved( GraphEvent event );

}
