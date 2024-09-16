package com.szadowsz.gui.component.folder;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.RColorPicker;

import java.awt.*;

/**
 * For if you want to open an RColorPicker in a separate (internal) window
 *
 */
public class RColorPickerFolder extends RFolder {
    // TODO Component Stub : WIP

    /**
     * Construct a RColorPicker with The Default Layout
     *
     * @param gui
     * @param path          folder path
     * @param parent        parent folder
     * @param startingValue color to first show
     */
    public RColorPickerFolder(RotomGui gui, String path, RFolder parent, Color startingValue, boolean showAlpha) {
        super(gui, path, parent);
        children.add(new RColorPicker(gui, path + "/PICKER", this, startingValue,showAlpha));
    }


    /**
     * Construct a RColorPicker with The Default Layout
     *
     * @param gui
     * @param path          folder path
     * @param parent        parent folder
     * @param startingValue color to first show
     */
    public RColorPickerFolder(RotomGui gui, String path, RFolder parent, Color startingValue) {
        this(gui, path, parent, startingValue, false);
    }

    @Override
    public void insertChild(RComponent child) {
        super.insertChild(child);
    }
}