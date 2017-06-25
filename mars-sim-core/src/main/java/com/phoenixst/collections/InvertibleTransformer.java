/*
 *  $Id: InvertibleTransformer.java,v 1.7 2005/10/03 15:11:54 rconner Exp $
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

package com.phoenixst.collections;

import org.apache.commons.collections.Transformer;


/**
 *  An invertible <code>Transformer</code>.
 *
 *  <P>No equivalent interface exists in Jakarta Commons-Collections
 *  3.0.
 *
 *  @version    $Revision: 1.7 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public interface InvertibleTransformer extends Transformer
{

    /**
     *  Performs the inverse operation of {@link
     *  org.apache.commons.collections.Transformer#transform
     *  transform( Object )} upon the specified object.
     */
    public Object untransform( Object object );

}
