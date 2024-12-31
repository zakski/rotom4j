package com.szadowsz.gui.layout;

import processing.core.PVector;

/**
 * Convenience Rectangle Info Class
 */
public class RRect {

    protected final PVector pos; // Coordinates of upper-left corner
    protected float width;
    protected float height;

    /**
     * Initializes a new instance of RBinRect from the specified coordinates and dimensions
     *
     * @param x      the X-Coordinate of the upper-left corner
     * @param y      the Y-Coordinate of the upper-left corner
     * @param width  the width of the Rectangle
     * @param height the height of the Rectangle
     */
    public RRect(float x, float y, float width, float height) {
        pos = new PVector(x, y);
        this.width = width;
        this.height = height;
    }

    /**
     * Initializes a new empty instance of RBinRect with a upper-left corner at (0,0) and a width and height of 0.
     */
    public RRect() {
        this(0, 0, 0, 0);
    }

    /**
     * Get the X-Coordinate of the upper-left corner
     *
     * @return the value of x
     */
    public float getX() {
        return pos.x;
    }

    /**
     * Get the Y-Coordinate of the upper-left corner
     *
     * @return the value of y
     */
    public float getY() {
        return pos.y;
    }

    /**
     * Get the Width of this Rectangle
     *
     * @return the Width, left-to-right
     */
    public float getWidth() {
        return width;
    }

    /**
     * Get the Height of this Rectangle
     *
     * @return the Height, top-to-bottom
     */
    public float getHeight() {
        return height;
    }

    /**
     * Checks if this Rectangle is empty. An empty rectangle has a non-positive area
     *
     * @return true if this Rectangle is empty
     */
    public boolean isEmpty() {
        return (width <= 0) || (height <= 0);
    }

    /**
     * Sets the size of this Rectangle based on the specified dimensions.
     *
     * @param width  the new Width of this Rectangle
     * @param height the new Height of this Rectangle
     */
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Sets the location and size of this Rectangle based on the specified coordinates and dimensions.
     *
     * @param x      the new X-coordinate of this Rectangle
     * @param y      the new Y-coordinate of this Rectangle
     * @param width  the new Width of this Rectangle
     * @param height the new Height of this Rectangle
     */
    public void setSize(float x, float y, float width, float height) {
        this.pos.x = x;
        this.pos.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Computes the intersection of this Rectangle with another
     *
     * @param r the other Rectangle
     * @return the largest Rectangle contained in both specified Rectangles; or if the Rectangles do not intersect,
     * an empty Rectangle.
     */
    public RRect intersection(RRect r) {
        float tx1 = this.pos.x;
        float ty1 = this.pos.y;
        float rx1 = r.pos.x;
        float ry1 = r.pos.y;
        float tx2 = tx1;
        tx2 += this.width;
        float ty2 = ty1;
        ty2 += this.height;
        float rx2 = rx1;
        rx2 += r.width;
        float ry2 = ry1;
        ry2 += r.height;
        if (tx1 < rx1) {
            tx1 = rx1;
        }
        if (ty1 < ry1) {
            ty1 = ry1;
        }
        if (tx2 > rx2) {
            tx2 = rx2;
        }
        if (ty2 > ry2) {
            ty2 = ry2;
        }
        tx2 -= tx1;
        ty2 -= ty1;
        // tx2,ty2 will never overflow (they will never be larger than the smallest of the two source w,h), However they
        // might underflow, though...
        if (tx2 < Float.MIN_VALUE) {
            tx2 = Float.MIN_VALUE;
        }
        if (ty2 < Float.MIN_VALUE) {
            ty2 = Float.MIN_VALUE;
        }
        return new RRect(tx1, ty1, tx2, ty2);
    }
}
