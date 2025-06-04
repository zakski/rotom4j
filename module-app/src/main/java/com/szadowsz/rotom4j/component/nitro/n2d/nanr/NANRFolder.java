package com.szadowsz.rotom4j.component.nitro.n2d.nanr;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.drawable.tab.RTab;
import com.szadowsz.gui.component.group.drawable.tab.RTabFunction;
import com.szadowsz.rotom4j.component.R4JComponent;
import com.szadowsz.rotom4j.component.R4JResourceFolder;
import com.szadowsz.rotom4j.file.nitro.n2d.nanr.NANR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NANRFolder extends R4JResourceFolder<NANR> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(NANRFolder.class);

    protected static final String SELECT_NANR_FILE = "Select NANR";

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
    public NANRFolder(RotomGui gui, String path, RGroup parent, NANR data) {
        super(gui, path, parent, data, SELECT_NANR_FILE);
    }

    @Override
    protected RTabFunction<R4JComponent<NANR>> createDisplay() {
        return (RTab tab) -> new NANRComponent(gui, tab.getPath() + "/Image" ,tab, data);
    }

    public void setDisplay(NANRComponent nanrComponent) {
        this.display = nanrComponent;
    }
}
