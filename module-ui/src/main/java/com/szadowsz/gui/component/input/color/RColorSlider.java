package com.szadowsz.gui.component.input.color;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.folder.RFolder;
import com.szadowsz.gui.component.input.slider.RSliderInt;

/**
 * Slider Node to represent the R, G, B or A (if that way inclined) values of a Color
 */
public class RColorSlider extends RSliderInt { // TODO create other config options for the Color Values
    // TODO Component Stub : WIP

    public RColorSlider(RotomGui gui, String path, RFolder parentFolder, float currentValue) {
        super(gui, path, parentFolder, currentValue, 0, 255, true);
    }

}
