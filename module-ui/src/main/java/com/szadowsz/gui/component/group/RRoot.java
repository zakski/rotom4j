package com.szadowsz.gui.component.group;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.folder.RFolder;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.layout.RLayoutConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.List;

/**
 * A node that opens a new window with child nodes when clicked.
 */
public final class RRoot extends RGroup {
    private static final Logger LOGGER = LoggerFactory.getLogger(RRoot.class);

    /**
     * Construct the Root Component
     * <p>
  =  * @param gui
     */
    public RRoot(RotomGui gui) {
        super(gui, "",null);
        isDraggable = false;
    }


    @Override
    public void setLayout(RLayoutBase layout) {
        this.layout = layout;
        for(RComponent child : children){
            if (child instanceof RFolder folder){
                folder.getWindow().reinitialiseBuffer();
            }
        }
    }

    @Override
    public void insertChild(RComponent child){
        if (!(child instanceof RFolder)) {
            super.insertChild(child);
        } else {
         LOGGER.warn("Unsuccessfully inserted child {}",child);
        }
    }

    @Override
    protected void drawBackground(PGraphics pg) {
        // NOOP
    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        // NOOP
    }

    @Override
    public float suggestWidth() {
        return calcPreferredSize().x;
    }

    public void resizeForContents() {
    }
}
