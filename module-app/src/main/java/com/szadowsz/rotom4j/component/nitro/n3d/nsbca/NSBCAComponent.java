package com.szadowsz.rotom4j.component.nitro.n3d.nsbca;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.rotom4j.component.R4JComponent;
import com.szadowsz.rotom4j.file.data.DataFile;
import com.szadowsz.rotom4j.file.nitro.n3d.nsbca.NSBCA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NSBCAComponent extends R4JComponent<NSBCA> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NSBCAComponent.class);

    private NSBCAFolder parentFolder;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     */
    protected NSBCAComponent(RotomGui gui, String path, RGroup parent, NSBCA data) {
        super(gui, path, parent);
        parentFolder = (NSBCAFolder) getParentFolder();
        this.data = data;
        initComponents();
    }

    protected void initComponents() {
    }

    @Override
    public void setLayout(RLayoutBase layout) {

    }

    @Override
    public void recolorImage() {
    }

    @Override
    public float suggestWidth() {
        return getPreferredSize().x;
    }
}
