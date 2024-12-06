package com.szadowsz.gui.component.bined;

import processing.core.PVector;

public class RBinRect {

    protected final PVector pos;
    protected float width;
    protected float height;

    public RBinRect(float x, float y, float width, float height) {
        pos = new PVector(x, y);
        this.width = width;
        this.height = height;
    }

    public RBinRect(){
        this(0,0,0,0);
    }

    public float getX(){
        return pos.x;
    }

    public float getY(){
        return pos.y;
    }

    public float getWidth(){
        return width;
    }

    public float getHeight(){
        return height;
    }

    public boolean isEmpty() {
        return (width <= 0) || (height <= 0);
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public void setBounds(float x, float y, float width, float height) {
        this.pos.x = x;
        this.pos.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Computes the intersection of this Rectangle with another
     *
     * @param     r   the other Rectangle
     * @return    the largest Rectangle contained in both specified Rectangles; or if the rectangles do not intersect,
     *            an empty rectangle.
     */
    public RBinRect intersection(RBinRect r) {
        float tx1 = this.pos.x;
        float ty1 = this.pos.y;
        float rx1 = r.pos.x;
        float ry1 = r.pos.y;
        float tx2 = tx1; tx2 += this.width;
        float ty2 = ty1; ty2 += this.height;
        float rx2 = rx1; rx2 += r.width;
        float ry2 = ry1; ry2 += r.height;
        if (tx1 < rx1) tx1 = rx1;
        if (ty1 < ry1) ty1 = ry1;
        if (tx2 > rx2) tx2 = rx2;
        if (ty2 > ry2) ty2 = ry2;
        tx2 -= tx1;
        ty2 -= ty1;
        // tx2,ty2 will never overflow (they will never be
        // larger than the smallest of the two source w,h)
        // they might underflow, though...
        if (tx2 < Float.MIN_VALUE) tx2 = Float.MIN_VALUE;
        if (ty2 < Float.MIN_VALUE) ty2 = Float.MIN_VALUE;
        return new RBinRect(tx1, ty1, tx2, ty2);
    }
}
