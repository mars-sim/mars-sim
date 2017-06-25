/*
 *  $Id: Reaper.java,v 1.12 2006/06/19 19:38:31 rconner Exp $
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

package com.phoenixst.collections;

import java.lang.ref.Reference;


/**
 *  The interface defining an object which reaps {@link Reapable
 *  Reapables}.  This interface, along with the {@link
 *  ReapableCollection} class, is intended to be used to solve the
 *  problem presented by the following requirements:
 *
 *  <UL>
 *    <LI>A resource produces objects (let's call them Foos) that are
 *        given to clients.
 *    <LI>The resource needs to track all Foos that it has produced
 *        that are still in use.
 *    <LI>Either it is not possible, or it is undesirable, for there
 *        to be an explicit way for a client to dispose of a Foo.
 *    <LI>However, when a client is no longer using a Foo (when its
 *        reference falls out of scope, for example), it should be
 *        reclaimed.
 *  </UL>
 *
 *  <P>A typical example would be a more robust <code>Iterator</code>
 *  which can actually deal with changes to the underlying data
 *  structure while iteration is in progress.
 *
 *  <P>However, this is not the only use-case.  A <code>Reaper</code>
 *  can also be used when there is no need to keep track of the
 *  currently reachable referents.  A <code>Reapable</code> instance
 *  could be created specific to each referent, to perform specific
 *  cleanup actions.  This would work as long as the referent is not
 *  reachable from the <code>Reapable</code>.
 *
 *  @version    $Revision: 1.12 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public interface Reaper
{

    /**
     *  Creates and returns a <code>Reference</code> for the given
     *  arguments.  Sometime after the <code>Reference</code> is
     *  reclaimed by the garbage collector, the argument's {@link
     *  Reapable#reap} method will be called.  Please note that there
     *  is typically no guarantee that this method will ever be
     *  called, so this mechanism should not be relied upon as a
     *  replacement for a try/finally block.
     *
     *  <P>Implementations should never return
     *  <code>PhantomReferences</code> if the referent needs to be
     *  retrievable while it is still referencable through other
     *  paths, or if the <code>Reference.clear()</code> is not
     *  guaranteed to be called.
     */
    public Reference createReference( Reapable reapable, Object referent );

}
