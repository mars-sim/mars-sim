/*
 *  $Id: Weight.java,v 1.5 2004/01/09 20:38:53 rconner Exp $
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
 *  A weight function, which may be applied to {@link Graph.Edge
 *  Edges}.
 *
 *  @version    $Revision: 1.5 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public interface Weight
{

    /**
     *  Gets the weight of the specified edge.
     *
     *  @param edge the edge whose weight is to be returned.
     */
    public double getWeight( Graph.Edge edge );


    /**
     *  Sets the weight of the specified edge (optional operation).
     *
     *  @param edge the edge whose weight is to be set.
     *
     *  @param weight the new weight of the specified edge.
     *
     *  @throws UnsupportedOperationException if this method is not
     *  supported.
     */
    public void setWeight( Graph.Edge edge, double weight );

}
