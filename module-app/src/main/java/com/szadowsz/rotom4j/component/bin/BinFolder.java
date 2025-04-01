package com.szadowsz.rotom4j.component.bin;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.drawable.tab.RTab;
import com.szadowsz.gui.component.group.drawable.tab.RTabFunction;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.input.mouse.RMouseEvent;
import com.szadowsz.rotom4j.app.ProcessingRotom4J;
import com.szadowsz.rotom4j.app.utils.FileChooser;
import com.szadowsz.rotom4j.component.R4JComponent;
import com.szadowsz.rotom4j.component.R4JFolder;
import com.szadowsz.rotom4j.component.nitro.ncgr.NCGRFolder;
import com.szadowsz.rotom4j.file.data.DataFile;
import com.szadowsz.rotom4j.file.nitro.nclr.NCLR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static processing.core.PConstants.CENTER;

public class BinFolder extends R4JFolder<DataFile> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BinFolder.class);

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui        the gui for the window that the component is drawn under
     * @param path       the path in the component tree
     * @param parent     the parent component reference
     * @param nclr
     */
    public BinFolder(RotomGui gui, String path, RGroup parent, DataFile nclr) {
        super(gui, path, parent,nclr,SELECT_NCLR_FILE);

    }

    /**
     * Check to see if node should display regular name, or selection name
     *
     * @return true if regular, false otherwise
     */
    @Override
    protected boolean shouldDisplayName() {
        return true;
    }

    @Override
    protected final RTabFunction<R4JComponent<DataFile>> createDisplay() {
        return (RTab tab) -> new BinComponent(gui, tab.getPath() + "/Structured" ,tab, data);
    }

    @Override
    protected void createTabs() {
        if (data.isCompressed()) {
            tabs.addTab(createEditor("Compressed", true));
        }
        tabs.addTab(createEditor( "Binary", false));
    }

}
