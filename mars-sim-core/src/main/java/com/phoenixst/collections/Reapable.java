/*
 *  $Id: Reapable.java,v 1.1 2004/06/02 18:53:27 rconner Exp $
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

package com.phoenixst.collections;


/**
 *  Something which can be reaped; see {@link Reaper} for more
 *  information.
 *
 *  @version    $Revision: 1.1 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public interface Reapable
{

    /**
     *  Informs this <code>Reapable</code> that it has at least one
     *  reference which has been reclaimed.
     */
    public void reap();

}
