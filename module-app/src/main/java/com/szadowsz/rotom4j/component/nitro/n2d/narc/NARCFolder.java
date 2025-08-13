package com.szadowsz.rotom4j.component.nitro.n2d.narc;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.drawable.tab.RTab;
import com.szadowsz.gui.component.group.drawable.tab.RTabFunction;
import com.szadowsz.rotom4j.component.R4JComponent;
import com.szadowsz.rotom4j.component.R4JFolder;
import com.szadowsz.rotom4j.component.nitro.n2d.narc.files.NARCFilesPages;
import com.szadowsz.rotom4j.component.nitro.n2d.narc.options.NARCOptions;
import com.szadowsz.rotom4j.file.nitro.n2d.narc.NARC;

public class NARCFolder extends R4JFolder<NARC> {

    protected static final String SELECT_NARC_FILE = "Select NARC";



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
    public NARCFolder(RotomGui gui, String path, RGroup parent, NARC data) {
        super(gui, path, parent, data, SELECT_NARC_FILE);
    }

    protected RTabFunction<NARCOptions> createOptions() {
        return (RTab tab) -> new NARCOptions(gui, tab.getPath() + "/Options", tab, data);
    }

    @Override
    protected RTabFunction<R4JComponent<NARC>> createDisplay() {
        return (RTab tab) -> new NARCFilesPages(gui, tab.getPath() + "/Files" ,tab, data);
    }

    protected void createTabs() {
        if (data.isCompressed()) {
            tabs.addTab(createEditor("Compressed", true));
        }
        tabs.addTab(createEditor( "Binary", false));
        tabs.addTab(createOptions());
        tabs.addTab(createDisplay());
    }

    public void reindex() {
        ((NARCFilesPages)tabs.findTabByName("Files")).reindex();
        redrawBuffers();
    }
}
