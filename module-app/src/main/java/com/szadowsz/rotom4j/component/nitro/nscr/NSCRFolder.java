package com.szadowsz.rotom4j.component.nitro.nscr;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.drawable.tab.RTab;
import com.szadowsz.gui.component.group.drawable.tab.RTabFunction;
import com.szadowsz.rotom4j.component.R4JComponent;
import com.szadowsz.rotom4j.component.R4JResourceFolder;
import com.szadowsz.rotom4j.file.nitro.nscr.NSCR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NSCRFolder extends R4JResourceFolder<NSCR> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(NSCRFolder.class);

    protected static final String SELECT_NSCR_FILE = "Select NSCR";

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui        the gui for the window that the component is drawn under
     * @param path       the path in the component tree
     * @param parent     the parent component reference
     * @param data
     */
    public NSCRFolder(RotomGui gui, String path, RGroup parent, NSCR data) {
        super(gui, path, parent, data, SELECT_NSCR_FILE);
    }

    @Override
    protected RTabFunction<R4JComponent<NSCR>> createDisplay() {
        return (RTab tab) -> new NSCRComponent(gui, tab.getPath() + "/Image" ,tab, data);
    }

    public void setDisplay(NSCRComponent nscrComponent) {
        this.display = nscrComponent;
    }
}
