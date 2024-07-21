package com.szadowsz.nds4j.app.nodes.nitro.nanr;

import com.szadowsz.nds4j.app.nodes.nitro.NitroCmpFolderNode;
import com.szadowsz.nds4j.app.nodes.nitro.PreviewNode;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.nitro.nanr.NANR;
import com.szadowsz.ui.input.ActivateByType;
import com.szadowsz.ui.node.LayoutType;
import com.szadowsz.ui.node.impl.ButtonNode;
import com.szadowsz.ui.node.impl.FolderNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PImage;

import static com.szadowsz.ui.store.LayoutStore.cell;


public class NANRFolderNode extends NitroCmpFolderNode {
    protected static final Logger LOGGER = LoggerFactory.getLogger(NANRFolderNode.class);

    private final NANR nanr;

    private final String CELL_NODE = "Cell";
    private final String SELECT_NCGR_FILE = "Select NCGR";

    public NANRFolderNode(String path, FolderNode parent, NANR nanr) throws NitroException {
        super(path, parent, LayoutType.VERTICAL_1_COL, nanr);
        this.nanr = nanr;
        children.clear();
        //children.add(new PreviewNode(path + "/" + nanr.getFileName(), this,nanr));

        children.add(createZoom());

        ButtonNode selectNcgr = new ButtonNode(path + "/" + SELECT_NCGR_FILE,this);
        selectNcgr.registerAction(ActivateByType.RELEASE, this::selectNcgr);
        children.add(selectNcgr);

        ButtonNode selectNcLr = new ButtonNode(path + "/" + SELECT_NCLR_FILE,this);
        selectNcLr.registerAction(ActivateByType.RELEASE, this::selectPalette);
        children.add(selectNcLr);
    }

    public void recolorImage() throws NitroException {
        nanr.getNCGR().recolorImage();

        PImage pImage = resizeImage(nanr.getImage());

        //((PreviewNode) findChildByName(nanr.getFileName())).loadImage(pImage);

        this.window.windowSizeX = autosuggestWindowWidthForContents();
        this.window.windowSizeXForContents = autosuggestWindowWidthForContents();
    }

    @Override
    public float autosuggestWindowWidthForContents() {
        float suggested = super.autosuggestWindowWidthForContents();
        if (nanr.getNCGR() != null) {
            return Math.max(suggested,((PreviewNode) children.get(0)).getImage().width);
        } else {
            return suggested;
        }
    }

    @Override
    protected void drawNodeForeground(PGraphics pg, String name) {
        drawLeftText(pg, name);
        drawRightBackdrop(pg, cell);
   }
}
