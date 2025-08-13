package com.szadowsz.rotom4j.component.nitro.n3d.nsbca;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.drawable.tab.RTab;
import com.szadowsz.gui.component.group.drawable.tab.RTabFunction;
import com.szadowsz.rotom4j.component.R4JComponent;
import com.szadowsz.rotom4j.component.R4JFolder;
import com.szadowsz.rotom4j.file.data.DataFile;
import com.szadowsz.rotom4j.file.nitro.n3d.nsbca.NSBCA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NSBCAFolder extends R4JFolder<NSBCA> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NSBCAFolder.class);

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui        the gui for the window that the component is drawn under
     * @param path       the path in the component tree
     * @param parent     the parent component reference
     * @param nsbca
     */
    public NSBCAFolder(RotomGui gui, String path, RGroup parent, NSBCA nsbca) {
        super(gui, path, parent,nsbca,SELECT_NCLR_FILE);

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
    protected final RTabFunction<R4JComponent<NSBCA>> createDisplay() {
        return (RTab tab) -> new NSBCAComponent(gui, tab.getPath() + "/Structured" ,tab, data);
    }

    @Override
    protected void createTabs() {
        if (data.isCompressed()) {
            tabs.addTab(createEditor("Compressed", true));
        }
        tabs.addTab(createEditor( "Binary", false));
    }

}
