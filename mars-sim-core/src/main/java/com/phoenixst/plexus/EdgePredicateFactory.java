/*
 *  $Id: EdgePredicateFactory.java,v 1.10 2005/10/03 15:24:00 rconner Exp $
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
 *  A factory for creating {@link EdgePredicate EdgePredicates}.
 *
 *  @version    $Revision: 1.10 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class EdgePredicateFactory implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

    ////////////////////////////////////////
    // Private constructor
    ////////////////////////////////////////


    private EdgePredicateFactory()
    {
        super();
    }


    ////////////////////////////////////////
    // Static factory methods
    ////////////////////////////////////////


    /**
     *  Creates a new <code>EdgePredicate</code> which tests for
     *  <code>Graph.Edges</code> which look just like the specified
     *  <code>edge</code>.  To pass the returned
     *  <code>Predicate</code>, an edge must have the same endpoints,
     *  contained user object, and directedness.  If the specified
     *  <code>edge</code> is undirected, then the endpoints may be
     *  swapped and still be valid.
     */
    public static final EdgePredicate create( Graph.Edge edge )
    {
        int directionFlags = edge.isDirected()
            ? GraphUtils.DIRECTED_OUT_MASK
            : GraphUtils.UNDIRECTED_MASK;
        return new EqualsUserNodesPredicate( edge.getUserObject(),
                                             edge.getTail(), edge.getHead(),
                                             directionFlags );
    }


    /**
     *  Creates a new <code>EdgePredicate</code> which tests for
     *  <code>Graph.Edges</code> that have the specified endpoints and
     *  direction relative to <code>firstNode</code>.
     */
    public static final EdgePredicate createEqualsNodes( Object firstNode,
                                                         Object secondNode,
                                                         int directionFlags )
    {
        return new EqualsNodesPredicate( firstNode,
                                         secondNode,
                                         directionFlags );
    }


    /**
     *  Creates a new <code>EdgePredicate</code> which tests for
     *  <code>Graph.Edges</code> that contain the specified user
     *  object and have the specified directedness.
     */
    public static final EdgePredicate createEqualsUser( Object userObject,
                                                        int directionFlags )
    {
        return new EqualsUserPredicate( userObject,
                                        directionFlags );
    }


    /**
     *  Creates a new <code>EdgePredicate</code> which tests for
     *  <code>Graph.Edges</code> that contain the specified user
     *  object, have the specified endpoints, and have the specified
     *  direction relative to <code>firstNode</code>.
     */
    public static final EdgePredicate createEquals( Object userObject,
                                                    Object firstNode,
                                                    Object secondNode,
                                                    int directionFlags )
    {
        return new EqualsUserNodesPredicate( userObject,
                                             firstNode,
                                             secondNode,
                                             directionFlags );
    }


    /**
     *  Creates a new <code>EdgePredicate</code> which tests for
     *  <code>Graph.Edges</code> whose contained user object and
     *  endpoints satisfy the specified <code>Predicates</code>, and
     *  which have the specified direction relative to the endpoint
     *  satisyfing the <code>firstNodePredicate</code>.
     */
    public static final EdgePredicate createPredicated( Predicate userObjectPredicate,
                                                        Predicate firstNodePredicate,
                                                        Predicate secondNodePredicate,
                                                        int directionFlags )
    {
        return new PredicatedUserNodesPredicate( userObjectPredicate,
                                                 firstNodePredicate,
                                                 secondNodePredicate,
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


    private static abstract class AbstractEdgePredicate
        implements EdgePredicate,
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
        AbstractEdgePredicate( int directionFlags )
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

        public Object getFirstNodeSpecification()
        {
            return TruePredicate.INSTANCE;
        }

        public Object getSecondNodeSpecification()
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

        abstract boolean evaluateUserObject( Object targetUserObject );

        abstract boolean evaluateNodes( Object targetFirstNode, Object targetSecondNode );

        public boolean evaluate( Object object )
        {
            Graph.Edge edge = (Graph.Edge) object;

            // Test user object

            if( !evaluateUserObject( edge.getUserObject() ) ) {
                return false;
            }

            // Test direction and endpoints

            Object tail = edge.getTail();
            Object head = edge.getHead();

            if( edge.isDirected() ) {
                return ( (directionFlags & GraphUtils.DIRECTED_OUT_MASK) != 0
                         && evaluateNodes( tail, head ) )
                    || ( (directionFlags & GraphUtils.DIRECTED_IN_MASK) != 0
                         && evaluateNodes( head, tail ) );
            }

            if( (directionFlags & GraphUtils.UNDIRECTED_MASK) == 0 ) {
                return false;
            }
            return evaluateNodes( tail, head )
                || evaluateNodes( head, tail );
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
            EdgePredicate pred = (EdgePredicate) object;
            return getDirectionFlags() == pred.getDirectionFlags()
                && getUserObjectSpecification().equals( pred.getUserObjectSpecification() )
                && getFirstNodeSpecification().equals( pred.getFirstNodeSpecification() )
                && getSecondNodeSpecification().equals( pred.getSecondNodeSpecification() );
        }

        public int hashCode()
        {
            // 17 & 37 are arbitrary, but non-zero and prime
            int result = 17;
            result = 37 * result + getUserObjectSpecification().hashCode();
            result = 37 * result + getFirstNodeSpecification().hashCode();
            result = 37 * result + getSecondNodeSpecification().hashCode();
            result = 37 * result + getDirectionFlags();
            return result;
        }

        public String toString()
        {
            StringBuilder s = new StringBuilder( "EdgePredicate( {" );
            s.append( GraphUtils.directionFlagsToString( getDirectionFlags() ) );
            s.append( "} [");
            s.append( getFirstNodeSpecification() );
            s.append( "] -- [");
            s.append( getUserObjectSpecification() );
            s.append( "] -- [");
            s.append( getSecondNodeSpecification() );
            s.append( "] )");
            return s.toString();
        }
    }


    private static final class EqualsNodesPredicate extends AbstractEdgePredicate implements Serializable {
    
        private static final long serialVersionUID = 1L;

        /**
         *  @serial
         */
        private final Object firstNode;

        /**
         *  @serial
         */
        private final Object secondNode;

        EqualsNodesPredicate( Object firstNode,
                              Object secondNode,
                              int directionFlags )
        {
            super( directionFlags );
            this.firstNode = firstNode;
            this.secondNode = secondNode;
        }

        public Object getFirstNodeSpecification()
        {
            return escapePredicate( firstNode );
        }

        public Object getSecondNodeSpecification()
        {
            return escapePredicate( secondNode );
        }

        boolean evaluateUserObject( Object targetUserObject )
        {
            return true;
        }

        boolean evaluateNodes( Object targetFirstNode, Object targetSecondNode )
        {
            return GraphUtils.equals( firstNode, targetFirstNode )
                && GraphUtils.equals( secondNode, targetSecondNode );
        }
    }


    private static final class EqualsUserPredicate extends AbstractEdgePredicate implements Serializable
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

        boolean evaluateUserObject( Object targetUserObject )
        {
            return GraphUtils.equals( userObject, targetUserObject );
        }

        boolean evaluateNodes( Object targetFirstNode, Object targetSecondNode )
        {
            return true;
        }
    }


    private static final class EqualsUserNodesPredicate extends AbstractEdgePredicate implements Serializable
    {
        private static final long serialVersionUID = 1L;

        /**
         *  @serial
         */
        private final Object userObject;

        /**
         *  @serial
         */
        private final Object firstNode;

        /**
         *  @serial
         */
        private final Object secondNode;

        EqualsUserNodesPredicate( Object userObject,
                                  Object firstNode,
                                  Object secondNode,
                                  int directionFlags )
        {
            super( directionFlags );
            this.userObject = userObject;
            this.firstNode = firstNode;
            this.secondNode = secondNode;
        }

        public Object getUserObjectSpecification()
        {
            return escapePredicate( userObject );
        }

        public Object getFirstNodeSpecification()
        {
            return escapePredicate( firstNode );
        }

        public Object getSecondNodeSpecification()
        {
            return escapePredicate( secondNode );
        }

        boolean evaluateUserObject( Object targetUserObject )
        {
            return GraphUtils.equals( userObject, targetUserObject );
        }

        boolean evaluateNodes( Object targetFirstNode, Object targetSecondNode )
        {
            return GraphUtils.equals( firstNode, targetFirstNode )
                && GraphUtils.equals( secondNode, targetSecondNode );
        }
    }


    private static final class PredicatedUserNodesPredicate extends AbstractEdgePredicate implements Serializable
    {
        private static final long serialVersionUID = 1L;

        /**
         *  @serial
         */
        private final Predicate userObjectPredicate;

        /**
         *  @serial
         */
        private final Predicate firstNodePredicate;

        /**
         *  @serial
         */
        private final Predicate secondNodePredicate;

        PredicatedUserNodesPredicate( Predicate userObjectPredicate,
                                      Predicate firstNodePredicate,
                                      Predicate secondNodePredicate,
                                      int directionFlags )
        {
            super( directionFlags );
            this.userObjectPredicate = userObjectPredicate;
            this.firstNodePredicate = firstNodePredicate;
            this.secondNodePredicate = secondNodePredicate;
            if( userObjectPredicate == null ) {
                throw new IllegalArgumentException( "User object Predicate is null." );
            }
            if( firstNodePredicate == null ) {
                throw new IllegalArgumentException( "First node Predicate is null." );
            }
            if( secondNodePredicate == null ) {
                throw new IllegalArgumentException( "Second node Predicate is null." );
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
            if( firstNodePredicate == null ) {
                throw new InvalidObjectException( "First node Predicate is null." );
            }
            if( secondNodePredicate == null ) {
                throw new InvalidObjectException( "Second node Predicate is null." );
            }
        }

        public Object getFirstNodeSpecification()
        {
            return firstNodePredicate;
        }

        public Object getSecondNodeSpecification()
        {
            return secondNodePredicate;
        }

        public Object getUserObjectSpecification()
        {
            return userObjectPredicate;
        }

        boolean evaluateUserObject( Object targetUserObject )
        {
            return userObjectPredicate.evaluate( targetUserObject );
        }

        boolean evaluateNodes( Object targetFirstNode, Object targetSecondNode )
        {
            return firstNodePredicate.evaluate( targetFirstNode )
                && secondNodePredicate.evaluate( targetSecondNode );
        }
    }

}
