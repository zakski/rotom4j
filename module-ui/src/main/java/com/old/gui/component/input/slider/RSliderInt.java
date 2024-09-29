package com.old.gui.component.input.slider;

import com.old.gui.RotomGui;
import com.old.gui.component.group.RGroup;

public class RSliderInt extends RSlider {

    public RSliderInt(RotomGui gui, String path, RGroup parent, float defaultValue, float min, float max, boolean constrained) {
        super(gui, path, parent, defaultValue, min, max, constrained);
    }

    public RSliderInt(RotomGui gui, String path, RGroup parent, float defaultValue, float min, float max, float precision, boolean constrained) {
        super(gui, path, parent, defaultValue, min, max, precision, constrained);
    }

    public RSliderInt(RotomGui gui, String path, RGroup parent, float defaultValue, float min, float max, boolean constrained, boolean displaySquigglyEquals) {
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
}
