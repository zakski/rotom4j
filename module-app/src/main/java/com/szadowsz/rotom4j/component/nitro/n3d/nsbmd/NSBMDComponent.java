package com.szadowsz.rotom4j.component.nitro.n3d.nsbmd;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.rotom4j.component.R4JComponent;
import com.szadowsz.rotom4j.file.nitro.n3d.nsbca.NSBCA;
import com.szadowsz.rotom4j.file.nitro.n3d.nsbmd.NSBMD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NSBMDComponent extends R4JComponent<NSBMD> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NSBMDComponent.class);

    private NSBMDFolder parentFolder;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param parent the parent component reference
     */
    protected NSBMDComponent(RotomGui gui, String path, RGroup parent, NSBMD data) {
        super(gui, path, parent);
        parentFolder = (NSBMDFolder) getParentFolder();
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
