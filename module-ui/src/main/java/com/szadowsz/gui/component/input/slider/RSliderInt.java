package com.szadowsz.gui.component.input.slider;


import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;

public class RSliderInt extends RSlider {

    public RSliderInt(RotomGui gui, String path, RGroup parent, int defaultValue, int min, int max, boolean constrained) {
        super(gui, path, parent, defaultValue, min, max, constrained);
    }

    public RSliderInt(RotomGui gui, String path, RGroup parent, int defaultValue, int min, int max, boolean constrained, boolean displaySquigglyEquals) {
        super(gui, path, parent, defaultValue, min, max, constrained, displaySquigglyEquals);
    }

    /**
     * Set to Integer Precision
     * @param value doesn't matter
     */
    @Override
    protected void setSensiblePrecision(String value) {
        precisionIndex = precisionRange.indexOf(1f);
        precisionValue = precisionRange.get(precisionIndex);
    }

    public int getValueAsInt() {
        return (int) value;
    }
}
