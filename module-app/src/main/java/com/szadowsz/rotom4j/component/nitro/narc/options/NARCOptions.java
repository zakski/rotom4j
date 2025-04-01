package com.szadowsz.rotom4j.component.nitro.narc.options;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.RGroupDrawable;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.rotom4j.file.nitro.narc.NARC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NARCOptions extends RGroupDrawable {
    private static final Logger LOGGER = LoggerFactory.getLogger(NARCOptions.class);

    protected static final String APPLY_FOLDER = "Apply";
    protected static final String REINDEX_FOLDER = "Reindex";
    protected static final String EXTRACT_FOLDER = "Extract";

    private final NARC data;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     */
    public NARCOptions(RotomGui gui, String path, RGroup parent, NARC data) {
        super(gui, path, parent);
        this.data = data;
        initComponents();
    }

    protected void initComponents() {
        if (!children.isEmpty()) {
            return;
        }
        children.add(new NARCApplyFolder(gui,path + "/" + APPLY_FOLDER, this));
        children.add(new NARCReindexFolder(gui,path + "/" + REINDEX_FOLDER, this));
        children.add(new NARCExtractFolder(gui,path + "/" + EXTRACT_FOLDER, this));
    }

    @Override
    public void setLayout(RLayoutBase layout) {
        // NOOP
    }

    public NARC getData() {
        return data;
    }
}
