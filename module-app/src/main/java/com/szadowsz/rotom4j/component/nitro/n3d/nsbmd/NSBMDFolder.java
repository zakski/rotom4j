package com.szadowsz.rotom4j.component.nitro.n3d.nsbmd;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.drawable.tab.RTab;
import com.szadowsz.gui.component.group.drawable.tab.RTabFunction;
import com.szadowsz.rotom4j.component.R4JComponent;
import com.szadowsz.rotom4j.component.R4JFolder;
import com.szadowsz.rotom4j.file.nitro.n3d.nsbca.NSBCA;
import com.szadowsz.rotom4j.file.nitro.n3d.nsbmd.NSBMD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NSBMDFolder extends R4JFolder<NSBMD> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NSBMDFolder.class);

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui        the gui for the window that the component is drawn under
     * @param path       the path in the component tree
     * @param parent     the parent component reference
     * @param nsbmd
     */
    public NSBMDFolder(RotomGui gui, String path, RGroup parent, NSBMD nsbmd) {
        super(gui, path, parent,nsbmd,SELECT_NCLR_FILE);

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
    protected final RTabFunction<R4JComponent<NSBMD>> createDisplay() {
        return (RTab tab) -> new NSBMDComponent(gui, tab.getPath() + "/Structured" ,tab, data);
    }

    @Override
    protected void createTabs() {
        if (data.isCompressed()) {
            tabs.addTab(createEditor("Compressed", true));
        }
        tabs.addTab(createEditor( "Binary", false));
    }

}
