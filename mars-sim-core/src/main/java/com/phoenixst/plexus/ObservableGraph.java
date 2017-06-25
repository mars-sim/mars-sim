/*
 *  $Id: ObservableGraph.java,v 1.2 2004/01/09 20:38:53 rconner Exp $
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


/**
 *  A <code>Graph</code> which can be listened to for structural
 *  changes.
 *
 *  @version    $Revision: 1.2 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public interface ObservableGraph extends Graph
{

    /**
     *  Adds the specified <code>GraphListener</code> which will be
     *  notified whenever this <code>ObservableGraph's</code>
     *  structure changes.
     */
    public void addGraphListener( GraphListener listener );


    /**
     *  Removes a previously added <code>GraphListener</code>.
     */
    public void removeGraphListener( GraphListener listener );

}
