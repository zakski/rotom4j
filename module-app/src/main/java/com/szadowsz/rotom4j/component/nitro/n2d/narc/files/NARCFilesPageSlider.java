package com.szadowsz.rotom4j.component.nitro.n2d.narc.files;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.input.slider.RSliderInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NARCFilesPageSlider extends RSliderInt {
    private static final Logger LOGGER = LoggerFactory.getLogger(NARCFilesPageSlider.class);

    private final NARCFilesPages filesGroup;

    public NARCFilesPageSlider(RotomGui gui, String path, NARCFilesPages parent) {
        super(gui, path, parent, 1, 1, parent.getTotalPages(), true);
        filesGroup = parent;
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
        valueMax = filesGroup.getTotalPages();
        buffer.invalidateBuffer();
    }

    @Override
    public void resetBuffer() {
        valueMax = filesGroup.getTotalPages();
        super.resetBuffer();
    }

    @Override
    protected void onValueChange() {
        super.onValueChange();
        filesGroup.turnPage();
    }
}
