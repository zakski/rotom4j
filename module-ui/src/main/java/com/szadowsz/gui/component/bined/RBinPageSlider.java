package com.szadowsz.gui.component.bined;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.input.slider.RSliderInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RBinPageSlider extends RSliderInt {
    private static final Logger LOGGER = LoggerFactory.getLogger(RBinPageSlider.class);

    private final RBinEditor editor;

    public RBinPageSlider(RotomGui gui, String path, RBinEditor parent) {
        super(gui, path, parent, 1, 1, (int) parent.getStructure().getTotalPages(), true);
        editor = parent;
    }

    @Override
    public int getValueAsInt() {
        return super.getValueAsInt() - 1; // Map Page display value to 0-Index
    }

    public boolean isVisible(){
        return super.isVisible() && valueMin < valueMax;
    }

    @Override
    protected void redrawBuffers() {
        LOGGER.info("Redraw called for {} {}",getClassName(),getName());
        valueMax = editor.getStructure().getTotalPages();
        buffer.invalidateBuffer();
    }

    @Override
    public void resetBuffer() {
        valueMax = editor.getStructure().getTotalPages();
        super.resetBuffer();
    }

    @Override
    protected void onValueChange() {
        super.onValueChange();
        editor.turnPage();
    }
}
