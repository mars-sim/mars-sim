/*
 * Copyright (c) 2009-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3x.jfx.window;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.logging.Logger;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;

/**
 * <code>Vector2f</code> defines a Vector for a two double value vector.
 * 
 * @author Mark Powell
 * @author Joshua Slack
 */
public final class Vector2d implements Savable, Cloneable, java.io.Serializable {

	static final long				serialVersionUID	= 1;
	private static final Logger		logger				= Logger.getLogger(Vector2d.class.getName());

	public static final Vector2d	ZERO				= new Vector2d(0f, 0f);
	public static final Vector2d	UNIT_XY				= new Vector2d(1f, 1f);

	/**
	 * the x value of the vector.
	 */
	public double					x;
	/**
	 * the y value of the vector.
	 */
	public double					y;

	/**
	 * Creates a Vector2f with the given initial x and y values.
	 * 
	 * @param x
	 *            The x value of this Vector2f.
	 * @param y
	 *            The y value of this Vector2f.
	 */
	public Vector2d(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Creates a Vector2f with x and y set to 0. Equivalent to Vector2f(0,0).
	 */
	public Vector2d() {
		this.x = this.y = 0;
	}

	/**
	 * Creates a new Vector2f that contains the passed vector's information
	 * 
	 * @param vector2f
	 *            The vector to copy
	 */
	public Vector2d(Vector2d vector2f) {
		this.x = vector2f.x;
		this.y = vector2f.y;
	}

	/**
	 * set the x and y values of the vector
	 * 
	 * @param x
	 *            the x value of the vector.
	 * @param y
	 *            the y value of the vector.
	 * @return this vector
	 */
	public Vector2d set(double x, double y) {
		this.x = x;
		this.y = y;
		return this;
	}

	/**
	 * set the x and y values of the vector from another vector
	 * 
	 * @param vec
	 *            the vector to copy from
	 * @return this vector
	 */
	public Vector2d set(Vector2d vec) {
		this.x = vec.x;
		this.y = vec.y;
		return this;
	}

	/**
	 * <code>add</code> adds a provided vector to this vector creating a resultant vector which is returned. If the provided vector is null, null is returned.
	 * 
	 * @param vec
	 *            the vector to add to this.
	 * @return the resultant vector.
	 */
	public Vector2d add(Vector2d vec) {
		if (null == vec) {
			Vector2d.logger.warning("Provided vector is null, null returned.");
			return null;
		}
		return new Vector2d(this.x + vec.x, this.y + vec.y);
	}

	/**
	 * <code>addLocal</code> adds a provided vector to this vector internally, and returns a handle to this vector for easy chaining of calls. If the provided vector is null, null is returned.
	 * 
	 * @param vec
	 *            the vector to add to this vector.
	 * @return this
	 */
	public Vector2d addLocal(Vector2d vec) {
		if (null == vec) {
			Vector2d.logger.warning("Provided vector is null, null returned.");
			return null;
		}
		this.x += vec.x;
		this.y += vec.y;
		return this;
	}

	/**
	 * <code>addLocal</code> adds the provided values to this vector internally, and returns a handle to this vector for easy chaining of calls.
	 * 
	 * @param addX
	 *            value to add to x
	 * @param addY
	 *            value to add to y
	 * @return this
	 */
	public Vector2d addLocal(double addX, double addY) {
		this.x += addX;
		this.y += addY;
		return this;
	}

	/**
	 * <code>add</code> adds this vector by <code>vec</code> and stores the result in <code>result</code>.
	 * 
	 * @param vec
	 *            The vector to add.
	 * @param result
	 *            The vector to store the result in.
	 * @return The result vector, after adding.
	 */
	public Vector2d add(Vector2d vec, Vector2d result) {
		if (null == vec) {
			Vector2d.logger.warning("Provided vector is null, null returned.");
			return null;
		}
		if (result == null)
			result = new Vector2d();
		result.x = this.x + vec.x;
		result.y = this.y + vec.y;
		return result;
	}

	/**
	 * <code>dot</code> calculates the dot product of this vector with a provided vector. If the provided vector is null, 0 is returned.
	 * 
	 * @param vec
	 *            the vector to dot with this vector.
	 * @return the resultant dot product of this vector and a given vector.
	 */
	public double dot(Vector2d vec) {
		if (null == vec) {
			Vector2d.logger.warning("Provided vector is null, 0 returned.");
			return 0;
		}
		return this.x * vec.x + this.y * vec.y;
	}

	public double determinant(Vector2d v) {
		return (this.x * v.y) - (this.y * v.x);
	}

	/**
	 * Sets this vector to the interpolation by changeAmnt from this to the finalVec this=(1-changeAmnt)*this + changeAmnt * finalVec
	 * 
	 * @param finalVec
	 *            The final vector to interpolate towards
	 * @param changeAmnt
	 *            An amount between 0.0 - 1.0 representing a percentage change from this towards finalVec
	 */
	public Vector2d interpolate(Vector2d finalVec, double changeAmnt) {
		this.x = (1 - changeAmnt) * this.x + changeAmnt * finalVec.x;
		this.y = (1 - changeAmnt) * this.y + changeAmnt * finalVec.y;
		return this;
	}

	/**
	 * Sets this vector to the interpolation by changeAmnt from beginVec to finalVec this=(1-changeAmnt)*beginVec + changeAmnt * finalVec
	 * 
	 * @param beginVec
	 *            The begining vector (delta=0)
	 * @param finalVec
	 *            The final vector to interpolate towards (delta=1)
	 * @param changeAmnt
	 *            An amount between 0.0 - 1.0 representing a precentage change from beginVec towards finalVec
	 */
	public Vector2d interpolate(Vector2d beginVec, Vector2d finalVec, double changeAmnt) {
		this.x = (1 - changeAmnt) * beginVec.x + changeAmnt * finalVec.x;
		this.y = (1 - changeAmnt) * beginVec.y + changeAmnt * finalVec.y;
		return this;
	}

	/**
	 * Check a vector... if it is null or its doubles are NaN or infinite, return false. Else return true.
	 * 
	 * @param vector
	 *            the vector to check
	 * @return true or false as stated above.
	 */
	public static boolean isValidVector(Vector2d vector) {
		if (vector == null)
			return false;
		if (Double.isNaN(vector.x) || Double.isNaN(vector.y))
			return false;
		if (Double.isInfinite(vector.x) || Double.isInfinite(vector.y))
			return false;
		return true;
	}

	/**
	 * <code>length</code> calculates the magnitude of this vector.
	 * 
	 * @return the length or magnitude of the vector.
	 */
	public double length() {
		return Math.sqrt(this.lengthSquared());
	}

	/**
	 * <code>lengthSquared</code> calculates the squared value of the magnitude of the vector.
	 * 
	 * @return the magnitude squared of the vector.
	 */
	public double lengthSquared() {
		return this.x * this.x + this.y * this.y;
	}

	/**
	 * <code>distanceSquared</code> calculates the distance squared between this vector and vector v.
	 *
	 * @param v
	 *            the second vector to determine the distance squared.
	 * @return the distance squared between the two vectors.
	 */
	public double distanceSquared(Vector2d v) {
		double dx = this.x - v.x;
		double dy = this.y - v.y;
		return dx * dx + dy * dy;
	}

	/**
	 * <code>distanceSquared</code> calculates the distance squared between this vector and vector v.
	 *
	 * @param otherX
	 *            The X coordinate of the v vector
	 * @param otherY
	 *            The Y coordinate of the v vector
	 * @return the distance squared between the two vectors.
	 */
	public double distanceSquared(double otherX, double otherY) {
		double dx = this.x - otherX;
		double dy = this.y - otherY;
		return dx * dx + dy * dy;
	}

	/**
	 * <code>distance</code> calculates the distance between this vector and vector v.
	 *
	 * @param v
	 *            the second vector to determine the distance.
	 * @return the distance between the two vectors.
	 */
	public double distance(Vector2d v) {
		return Math.sqrt(this.distanceSquared(v));
	}

	/**
	 * <code>mult</code> multiplies this vector by a scalar. The resultant vector is returned.
	 * 
	 * @param scalar
	 *            the value to multiply this vector by.
	 * @return the new vector.
	 */
	public Vector2d mult(double scalar) {
		return new Vector2d(this.x * scalar, this.y * scalar);
	}

	/**
	 * <code>multLocal</code> multiplies this vector by a scalar internally, and returns a handle to this vector for easy chaining of calls.
	 * 
	 * @param scalar
	 *            the value to multiply this vector by.
	 * @return this
	 */
	public Vector2d multLocal(double scalar) {
		this.x *= scalar;
		this.y *= scalar;
		return this;
	}

	/**
	 * <code>multLocal</code> multiplies a provided vector to this vector internally, and returns a handle to this vector for easy chaining of calls. If the provided vector is null, null is returned.
	 * 
	 * @param vec
	 *            the vector to mult to this vector.
	 * @return this
	 */
	public Vector2d multLocal(Vector2d vec) {
		if (null == vec) {
			Vector2d.logger.warning("Provided vector is null, null returned.");
			return null;
		}
		this.x *= vec.x;
		this.y *= vec.y;
		return this;
	}

	/**
	 * Multiplies this Vector2f's x and y by the scalar and stores the result in product. The result is returned for chaining. Similar to product=this*scalar;
	 * 
	 * @param scalar
	 *            The scalar to multiply by.
	 * @param product
	 *            The vector2f to store the result in.
	 * @return product, after multiplication.
	 */
	public Vector2d mult(double scalar, Vector2d product) {
		if (null == product) {
			product = new Vector2d();
		}

		product.x = this.x * scalar;
		product.y = this.y * scalar;
		return product;
	}

	/**
	 * <code>divide</code> divides the values of this vector by a scalar and returns the result. The values of this vector remain untouched.
	 * 
	 * @param scalar
	 *            the value to divide this vectors attributes by.
	 * @return the result <code>Vector</code>.
	 */
	public Vector2d divide(double scalar) {
		return new Vector2d(this.x / scalar, this.y / scalar);
	}

	/**
	 * <code>divideLocal</code> divides this vector by a scalar internally, and returns a handle to this vector for easy chaining of calls. Dividing by zero will result in an exception.
	 * 
	 * @param scalar
	 *            the value to divides this vector by.
	 * @return this
	 */
	public Vector2d divideLocal(double scalar) {
		this.x /= scalar;
		this.y /= scalar;
		return this;
	}

	/**
	 * <code>negate</code> returns the negative of this vector. All values are negated and set to a new vector.
	 * 
	 * @return the negated vector.
	 */
	public Vector2d negate() {
		return new Vector2d(-this.x, -this.y);
	}

	/**
	 * <code>negateLocal</code> negates the internal values of this vector.
	 * 
	 * @return this.
	 */
	public Vector2d negateLocal() {
		this.x = -this.x;
		this.y = -this.y;
		return this;
	}

	/**
	 * <code>subtract</code> subtracts the values of a given vector from those of this vector creating a new vector object. If the provided vector is null, an exception is thrown.
	 * 
	 * @param vec
	 *            the vector to subtract from this vector.
	 * @return the result vector.
	 */
	public Vector2d subtract(Vector2d vec) {
		return this.subtract(vec, null);
	}

	/**
	 * <code>subtract</code> subtracts the values of a given vector from those of this vector storing the result in the given vector object. If the provided vector is null, an exception is thrown.
	 * 
	 * @param vec
	 *            the vector to subtract from this vector.
	 * @param store
	 *            the vector to store the result in. It is safe for this to be the same as vec. If null, a new vector is created.
	 * @return the result vector.
	 */
	public Vector2d subtract(Vector2d vec, Vector2d store) {
		if (store == null)
			store = new Vector2d();
		store.x = this.x - vec.x;
		store.y = this.y - vec.y;
		return store;
	}

	/**
	 * <code>subtract</code> subtracts the given x,y values from those of this vector creating a new vector object.
	 * 
	 * @param valX
	 *            value to subtract from x
	 * @param valY
	 *            value to subtract from y
	 * @return this
	 */
	public Vector2d subtract(double valX, double valY) {
		return new Vector2d(this.x - valX, this.y - valY);
	}

	/**
	 * <code>subtractLocal</code> subtracts a provided vector to this vector internally, and returns a handle to this vector for easy chaining of calls. If the provided vector is null, null is returned.
	 * 
	 * @param vec
	 *            the vector to subtract
	 * @return this
	 */
	public Vector2d subtractLocal(Vector2d vec) {
		if (null == vec) {
			Vector2d.logger.warning("Provided vector is null, null returned.");
			return null;
		}
		this.x -= vec.x;
		this.y -= vec.y;
		return this;
	}

	/**
	 * <code>subtractLocal</code> subtracts the provided values from this vector internally, and returns a handle to this vector for easy chaining of calls.
	 * 
	 * @param valX
	 *            value to subtract from x
	 * @param valY
	 *            value to subtract from y
	 * @return this
	 */
	public Vector2d subtractLocal(double valX, double valY) {
		this.x -= valX;
		this.y -= valY;
		return this;
	}

	/**
	 * <code>normalize</code> returns the unit vector of this vector.
	 * 
	 * @return unit vector of this vector.
	 */
	public Vector2d normalize() {
		double length = this.length();
		if (length != 0) {
			return this.divide(length);
		}

		return this.divide(1);
	}

	/**
	 * <code>normalizeLocal</code> makes this vector into a unit vector of itself.
	 * 
	 * @return this.
	 */
	public Vector2d normalizeLocal() {
		double length = this.length();
		if (length != 0) {
			return this.divideLocal(length);
		}

		return this.divideLocal(1);
	}

	/**
	 * <code>smallestAngleBetween</code> returns (in radians) the minimum angle between two vectors. It is assumed that both this vector and the given vector are unit vectors (iow, normalized).
	 * 
	 * @param otherVector
	 *            a unit vector to find the angle against
	 * @return the angle in radians.
	 */
	public double smallestAngleBetween(Vector2d otherVector) {
		double dotProduct = this.dot(otherVector);
		double angle = Math.acos(dotProduct);
		return angle;
	}

	/**
	 * <code>angleBetween</code> returns (in radians) the angle required to rotate a ray represented by this vector to lie colinear to a ray described by the given vector. It is assumed that both this vector and the given vector are unit vectors
	 * (iow, normalized).
	 * 
	 * @param otherVector
	 *            the "destination" unit vector
	 * @return the angle in radians.
	 */
	public double angleBetween(Vector2d otherVector) {
		double angle = Math.atan2(otherVector.y, otherVector.x) - Math.atan2(this.y, this.x);
		return angle;
	}

	public double getX() {
		return this.x;
	}

	public Vector2d setX(double x) {
		this.x = x;
		return this;
	}

	public double getY() {
		return this.y;
	}

	public Vector2d setY(double y) {
		this.y = y;
		return this;
	}

	/**
	 * <code>getAngle</code> returns (in radians) the angle represented by this Vector2f as expressed by a conversion from rectangular coordinates (<code>x</code>,&nbsp;<code>y</code>) to polar coordinates (r,&nbsp;<i>theta</i>).
	 * 
	 * @return the angle in radians. [-pi, pi)
	 */
	public double getAngle() {
		return Math.atan2(this.y, this.x);
	}

	/**
	 * <code>zero</code> resets this vector's data to zero internally.
	 */
	public Vector2d zero() {
		this.x = this.y = 0;
		return this;
	}

	@Override
	public Vector2d clone() {
		try {
			return (Vector2d) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(); // can not happen
		}
	}

	/**
	 * Saves this Vector2f into the given double[] object.
	 * 
	 * @param doubles
	 *            The double[] to take this Vector2f. If null, a new double[2] is created.
	 * @return The array, with X, Y double values in that order
	 */
	public double[] toArray(double[] doubles) {
		if (doubles == null) {
			doubles = new double[2];
		}
		doubles[0] = this.x;
		doubles[1] = this.y;
		return doubles;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(this.x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		Vector2d other = (Vector2d) obj;
		if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}

	/**
	 * <code>toString</code> returns the string representation of this vector object. The format of the string is such: com.jme.math.Vector2f [X=XX.XXXX, Y=YY.YYYY]
	 * 
	 * @return the string representation of this vector.
	 */
	@Override
	public String toString() {
		return "(" + this.x + ", " + this.y + ")";
	}

	/**
	 * Used with serialization. Not to be called manually.
	 * 
	 * @param in
	 *            ObjectInput
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @see java.io.Externalizable
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.x = in.readDouble();
		this.y = in.readDouble();
	}

	/**
	 * Used with serialization. Not to be called manually.
	 * 
	 * @param out
	 *            ObjectOutput
	 * @throws IOException
	 * @see java.io.Externalizable
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeDouble(this.x);
		out.writeDouble(this.y);
	}

	@Override
	public void write(JmeExporter e) throws IOException {
		OutputCapsule capsule = e.getCapsule(this);
		capsule.write(this.x, "x", 0);
		capsule.write(this.y, "y", 0);
	}

	@Override
	public void read(JmeImporter e) throws IOException {
		InputCapsule capsule = e.getCapsule(this);
		this.x = capsule.readDouble("x", 0);
		this.y = capsule.readDouble("y", 0);
	}

	public void rotateAroundOrigin(double angle, boolean cw) {
		if (cw)
			angle = -angle;
		double newX = Math.cos(angle) * this.x - Math.sin(angle) * this.y;
		double newY = Math.sin(angle) * this.x + Math.cos(angle) * this.y;
		this.x = newX;
		this.y = newY;
	}
}
