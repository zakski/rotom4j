package com.szadowsz.gui.component.group;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.layout.RLayoutBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * Top Level Component
 */
public class RRoot extends RGroup {
    private static final Logger LOGGER = LoggerFactory.getLogger(RRoot.class);

    /**
     * Construct the Root Component
     * <p>
     =  * @param gui
     */
    public RRoot(RotomGui gui) {
        super(gui, "", null);
        isDraggable = false;
    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        // NOOP
    }

    @Override
    public void drawToBuffer() {

    }

    @Override
    public void setLayout(RLayoutBase layout) {
        this.layout = layout;
        resizeForContents();
    }

    @Override
    public float suggestWidth() {
        return getPreferredSize().x;
    }

    @Override
    public void insertChild(RComponent child) {
        if (child instanceof RFolder folder) {
            children.add(folder);
            gui.getWinManager().uncoverOrCreateWindow(folder);
            folder.getWindow().open(false);
        }
    }

    /**
     * Method to re-arrange windows according to the layout
     */
    public void resizeForContents() {
        if (!gui.isSetup() && !children.isEmpty()) {
            LOGGER.debug("Resizing Root for children of size {}", children.size());
            layout.setWinLayout(new PVector(gui.getSketch().width, gui.getSketch().height), children.stream().filter(c -> c instanceof RFolder).map(c -> ((RFolder) c).getWindow()).toList());
            for (RComponent child : children) {
                if (child instanceof RFolder folder) {
                    folder.getWindow().reinitialiseBuffer();
                }
            }
        }
    }
}