/*
 *  $Id: Rooted.java,v 1.1 2004/10/01 21:06:07 rconner Exp $
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
 *  A graph structure which distinguishes a particular node as the
 *  "root".
 *
 *  @version    $Revision: 1.1 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public interface Rooted
{

    /**
     *  Gets the root node.
     */
    public Object getRoot();


    /**
     *  Sets the root node, which must already be present.
     */
    public void setRoot( Object root );

}
