/*
 *  $Id: TraverserPredicateFactory.java,v 1.12 2005/10/03 15:24:00 rconner Exp $
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

package com.phoenixst.plexus;

import java.io.*;

import org.apache.commons.collections.Predicate;

import com.phoenixst.collections.*;


/**
 *  A factory for creating {@link TraverserPredicate
 *  TraverserPredicates}.
 *
 *  @version    $Revision: 1.12 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class TraverserPredicateFactory
{

    ////////////////////////////////////////
    // Private constructor
    ////////////////////////////////////////


    private TraverserPredicateFactory()
    {
        super();
    }


    ////////////////////////////////////////
    // Static factory methods
    ////////////////////////////////////////


    /**
     *  Creates a new <code>TraverserPredicate</code> which tests for
     *  <code>Graph.Edges</code> which look just like the specified
     *  <code>edge</code>, ignoring one endpoint.
     *
     *  <P>If <code>fromTail</code> is <code>true</code>, then the
     *  returned <code>Predicate</code> tests for traversals over
     *  <code>Graph.Edges</code> that contain the same user object, to
     *  the head of the specified <code>edge</code>, with the same
     *  directedness.  In other words, test as if traversing from the
     *  tail looking for a similar <code>Graph.Edge</code>, although
     *  the tail itself is not checked.
     *
     *  <P>If <code>fromTail</code> is <code>false</code>, then the
     *  returned <code>Predicate</code> tests for traversals over
     *  <code>Graph.Edges</code> that contain the same user object, to
     *  the tail of the specified <code>edge</code>, with the same
     *  directedness.  In other words, test as if traversing from the
     *  head looking for a similar <code>Graph.Edge</code>, although
     *  the head itself is not checked.
     */
    public static final TraverserPredicate create( Graph.Edge edge,
                                                   boolean fromTail )
    {
        Object node;
        int directionFlags;

        if( fromTail ) {
            directionFlags = edge.isDirected()
                ? GraphUtils.DIRECTED_OUT_MASK
                : GraphUtils.UNDIRECTED_MASK;
            node = edge.getHead();
        } else {
            directionFlags = edge.isDirected()
                ? GraphUtils.DIRECTED_IN_MASK
                : GraphUtils.UNDIRECTED_MASK;
            node = edge.getTail();
        }
        return new EqualsUserNodePredicate( edge.getUserObject(),
                                            node,
                                            directionFlags );
    }


    /**
     *  Creates a new <code>TraverserPredicate</code> which tests for
     *  traversals to the specified <code>node</code>, with a
     *  direction specified relative to the endpoint
     *  <strong>from</strong> which the <code>Graph.Edge</code> is
     *  being traversed.
     */
    public static final TraverserPredicate createEqualsNode( Object node,
                                                             int directionFlags )
    {
        return new EqualsNodePredicate( node,
                                        directionFlags );
    }


    /**
     *  Creates a new <code>TraverserPredicate</code> which tests for
     *  traversals over <code>Graph.Edges</code> that contain the
     *  specified user object, with a direction specified relative to
     *  the endpoint <strong>from</strong> which the
     *  <code>Graph.Edge</code> is being traversed.
     */
    public static final TraverserPredicate createEqualsUser( Object userObject,
                                                             int directionFlags )
    {
        return new EqualsUserPredicate( userObject,
                                        directionFlags );
    }


    /**
     *  Creates a new <code>TraverserPredicate</code> which tests for
     *  traversals over <code>Graph.Edges</code> that contain the
     *  specified user object, to the specified <code>node</code>,
     *  with a direction specified relative to the endpoint
     *  <strong>from</strong> which the <code>Graph.Edge</code> is
     *  being traversed.
     */
    public static final TraverserPredicate createEquals( Object userObject,
                                                         Object node,
                                                         int directionFlags )
    {
        return new EqualsUserNodePredicate( userObject,
                                            node,
                                            directionFlags );
    }


    /**
     *  Creates a new <code>TraverserPredicate</code> which tests for
     *  traversals over <code>Graph.Edges</code> whose contained user
     *  object satisfies <code>userObjectPredicate</code>, to a node
     *  which satisfies <code>nodePredicate</code>, with a direction
     *  specified relative to the endpoint <strong>from</strong> which
     *  the <code>Graph.Edge</code> is being traversed.
     */
    public static final TraverserPredicate createPredicated( Predicate userObjectPredicate,
                                                             Predicate nodePredicate,
                                                             int directionFlags )
    {
        return new PredicatedUserNodePredicate( userObjectPredicate,
                                                nodePredicate,
                                                directionFlags );
    }


    ////////////////////////////////////////
    // Helper method
    ////////////////////////////////////////


    static Object escapePredicate( Object object )
    {
        return (object instanceof Predicate)
            ? new EqualPredicate( object )
            : object;
    }


    ////////////////////////////////////////
    // Private classes
    ////////////////////////////////////////


    private static abstract class AbstractTraverserPredicate
        implements TraverserPredicate,
                   Serializable
    {
        private static final long serialVersionUID = 1L;

        /**
         *  @serial
         */
        private final int directionFlags;

        /**
         *  Package-private constructor, to be invoked by subclasses.
         */
        AbstractTraverserPredicate( int directionFlags )
        {
            super();
            this.directionFlags = directionFlags & GraphUtils.ANY_DIRECTION_MASK;
            if( this.directionFlags == 0 ) {
                throw new IllegalArgumentException( "Direction flags are invalid: " + directionFlags );
            }
        }

        private void readObject( ObjectInputStream in )
            throws ClassNotFoundException,
                   IOException
        {
            in.defaultReadObject();
            if( (directionFlags & GraphUtils.ANY_DIRECTION_MASK) == 0 ) {
                throw new InvalidObjectException( "No direction flags set: " + directionFlags );
            }
            if( (directionFlags & GraphUtils.ANY_DIRECTION_MASK) != directionFlags ) {
                throw new InvalidObjectException( "Direction flags not valid: " + directionFlags );
            }
        }

        public Object getNodeSpecification()
        {
            return TruePredicate.INSTANCE;
        }

        public Object getUserObjectSpecification()
        {
            return TruePredicate.INSTANCE;
        }

        public int getDirectionFlags()
        {
            return directionFlags;
        }

        abstract boolean evaluate( Object targetUserObject, Object targetNode );

        public boolean evaluate( Object object )
        {
            OrderedPair pair = (OrderedPair) object;
            Object baseNode = pair.getFirst();
            Graph.Edge edge = (Graph.Edge) pair.getSecond();

            // Test direction

            if( edge.isDirected() ) {
                if( !( (directionFlags & GraphUtils.DIRECTED_OUT_MASK) != 0
                       && GraphUtils.equals( baseNode, edge.getTail() ) )
                    && !( (directionFlags & GraphUtils.DIRECTED_IN_MASK) != 0
                          && GraphUtils.equals( baseNode, edge.getHead() ) ) ) {
                    return false;
                }
            } else {
                if( (directionFlags & GraphUtils.UNDIRECTED_MASK) == 0 ) {
                    return false;
                }
            }

            // Test everything else

            return evaluate( edge.getUserObject(), edge.getOtherEndpoint( baseNode ) );
        }

        public boolean equals( Object object )
        {
            if( object == this ) {
                return true;
            }
            if( object == null ) {
                return false;
            }
            if( !getClass().equals( object.getClass() ) ) {
                return false;
            }
            TraverserPredicate pred = (TraverserPredicate) object;
            return getDirectionFlags() == pred.getDirectionFlags()
                && getUserObjectSpecification().equals( pred.getUserObjectSpecification() )
                && getNodeSpecification().equals( pred.getNodeSpecification() );
        }

        public int hashCode()
        {
            // 17 & 37 are arbitrary, but non-zero and prime
            int result = 17;
            result = 37 * result + getUserObjectSpecification().hashCode();
            result = 37 * result + getNodeSpecification().hashCode();
            result = 37 * result + getDirectionFlags();
            return result;
        }

        public String toString()
        {
            StringBuilder s = new StringBuilder( "TraverserPredicate( {" );
            s.append( GraphUtils.directionFlagsToString( getDirectionFlags() ) );
            s.append( "} [");
            s.append( getUserObjectSpecification() );
            s.append( "] -- [");
            s.append( getNodeSpecification() );
            s.append( "] )");
            return s.toString();
        }
    }


    private static final class EqualsNodePredicate extends AbstractTraverserPredicate
    {
        private static final long serialVersionUID = 1L;

        /**
         *  @serial
         */
        private final Object node;

        EqualsNodePredicate( Object node,
                             int directionFlags )
        {
            super( directionFlags );
            this.node = node;
        }

        public Object getNodeSpecification()
        {
            return escapePredicate( node );
        }

        boolean evaluate( Object targetUserObject, Object targetNode )
        {
            return GraphUtils.equals( node, targetNode );
        }
    }


    private static final class EqualsUserPredicate extends AbstractTraverserPredicate
    {
        private static final long serialVersionUID = 1L;

        /**
         *  @serial
         */
        private final Object userObject;

        EqualsUserPredicate( Object userObject,
                             int directionFlags )
        {
            super( directionFlags );
            this.userObject = userObject;
        }

        public Object getUserObjectSpecification()
        {
            return escapePredicate( userObject );
        }

        boolean evaluate( Object targetUserObject, Object targetNode )
        {
            return GraphUtils.equals( userObject, targetUserObject );
        }
    }


    private static final class EqualsUserNodePredicate extends AbstractTraverserPredicate
    {
        private static final long serialVersionUID = 1L;

        /**
         *  @serial
         */
        private final Object userObject;

        /**
         *  @serial
         */
        private final Object node;

        EqualsUserNodePredicate( Object userObject,
                                 Object node,
                                 int directionFlags )
        {
            super( directionFlags );
            this.userObject = userObject;
            this.node = node;
        }

        public Object getNodeSpecification()
        {
            return escapePredicate( node );
        }

        public Object getUserObjectSpecification()
        {
            return escapePredicate( userObject );
        }

        boolean evaluate( Object targetUserObject, Object targetNode )
        {
            return GraphUtils.equals( userObject, targetUserObject )
                && GraphUtils.equals( node, targetNode );
        }
    }


    private static final class PredicatedUserNodePredicate extends AbstractTraverserPredicate
    {
        private static final long serialVersionUID = 1L;

        /**
         *  @serial
         */
        private final Predicate userObjectPredicate;

        /**
         *  @serial
         */
        private final Predicate nodePredicate;

        PredicatedUserNodePredicate( Predicate userObjectPredicate,
                                     Predicate nodePredicate,
                                     int directionFlags )
        {
            super( directionFlags );
            this.userObjectPredicate = userObjectPredicate;
            this.nodePredicate = nodePredicate;
            if( userObjectPredicate == null ) {
                throw new IllegalArgumentException( "User object Predicate is null." );
            }
            if( nodePredicate == null ) {
                throw new IllegalArgumentException( "Node Predicate is null." );
            }
        }

        private void readObject( ObjectInputStream in )
            throws ClassNotFoundException,
                   IOException
        {
            in.defaultReadObject();
            if( userObjectPredicate == null ) {
                throw new InvalidObjectException( "User object Predicate is null." );
            }
            if( nodePredicate == null ) {
                throw new InvalidObjectException( "Node Predicate is null." );
            }
        }

        public Object getUserObjectSpecification()
        {
            return userObjectPredicate;
        }

        public Object getNodeSpecification()
        {
            return nodePredicate;
        }

        boolean evaluate( Object targetUserObject, Object targetNode )
        {
            return userObjectPredicate.evaluate( targetUserObject )
                && nodePredicate.evaluate( targetNode );
        }
    }

}
