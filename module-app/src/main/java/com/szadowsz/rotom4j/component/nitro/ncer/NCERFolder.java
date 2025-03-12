package com.szadowsz.rotom4j.component.nitro.ncer;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.drawable.tab.RTab;
import com.szadowsz.gui.component.group.drawable.tab.RTabFunction;
import com.szadowsz.rotom4j.component.R4JComponent;
import com.szadowsz.rotom4j.component.R4JResourceFolder;
import com.szadowsz.rotom4j.file.nitro.ncer.NCER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NCERFolder extends R4JResourceFolder<NCER> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(NCERFolder.class);

    protected static final String SELECT_NCER_FILE = "Select NCER";

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui        the gui for the window that the component is drawn under
     * @param path       the path in the component tree
     * @param parent     the parent component reference
     * @param data
     * @param selectName
     */
    public NCERFolder(RotomGui gui, String path, RGroup parent, NCER data) {
        super(gui, path, parent, data, SELECT_NCER_FILE);
    }

    @Override
    protected RTabFunction<R4JComponent<NCER>> createDisplay() {
        return (RTab tab) -> new NCERComponent(gui, tab.getPath() + "/Image" ,tab, data);
    }

    public void setDisplay(NCERComponent ncerComponent) {
        this.display = ncerComponent;
    }
}
