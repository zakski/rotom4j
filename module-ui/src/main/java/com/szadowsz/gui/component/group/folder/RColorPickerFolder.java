package com.szadowsz.gui.component.group.folder;

import com.old.gui.RotomGui;
import com.old.gui.component.RComponent;
import com.old.gui.component.folder.RFolder;
import com.old.gui.component.group.RColorPicker;

import java.awt.*;

/**
 * For if you want to open an RColorPicker in a separate (internal) window
 *
 */
public class RColorPickerFolder extends com.old.gui.component.folder.RFolder {
    // TODO Component Stub : WIP

    /**
     * Construct a RColorPicker with The Default Layout
     *
     * @param gui
     * @param path          folder path
     * @param parent        parent folder
     * @param startingValue color to first show
     */
    public RColorPickerFolder(RotomGui gui, String path, com.old.gui.component.folder.RFolder parent, Color startingValue, boolean showAlpha) {
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