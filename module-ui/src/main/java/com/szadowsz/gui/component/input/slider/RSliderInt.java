package com.szadowsz.gui.component.input.slider;


import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;

/**
 * 1D Slider class that operates solely on integer precision
 */
public class RSliderInt extends RSlider {

    public RSliderInt(RotomGui gui, String path, RGroup parent, int defaultValue, int min, int max, boolean constrained) {
        super(gui, path, parent, defaultValue, min, max, constrained);
    }

    @Override
    protected boolean validatePrecision(int newPrecisionIndex) {
        return false; // TODO We current can't change precision, should allow for different whole number precision?
    }

    /**
     * Set to Integer Precision
     *
     * @param value doesn't matter
     */
    @Override
    protected void setSensiblePrecision(String value) {
        // TODO We current can't change precision, should allow for different whole number precision?
        precisionIndex = precisionRange.indexOf(1f);
    }

    /**
     * Get the value of the slider as an int
     *
     * @return current slider value as an integer
     */
    public int getValueAsInt() {
        return (int) value;
    }
}
