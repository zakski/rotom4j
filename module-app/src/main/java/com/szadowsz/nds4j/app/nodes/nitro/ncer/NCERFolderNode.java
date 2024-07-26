package com.szadowsz.nds4j.app.nodes.nitro.ncer;

import com.szadowsz.nds4j.app.nodes.nitro.NitroCmpFolderNode;
import com.szadowsz.nds4j.app.nodes.nitro.PreviewNode;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.nitro.ncer.NCER;
import com.szadowsz.ui.input.ActivateByType;
import com.szadowsz.ui.node.LayoutType;
import com.szadowsz.ui.node.impl.ButtonNode;
import com.szadowsz.ui.node.impl.FolderNode;
import com.szadowsz.ui.node.impl.SliderNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PImage;

import static com.szadowsz.ui.store.LayoutStore.cell;


public class NCERFolderNode extends NitroCmpFolderNode<NCER> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(NCERFolderNode.class);

     private final String CELL_NODE = "Cell";
    private final String SELECT_NCGR_FILE = "Select NCGR";

    public NCERFolderNode(String path, FolderNode parent, NCER ncer) throws NitroException {
        super(path, SELECT_NCER_FILE, parent, LayoutType.VERTICAL_1_COL, ncer);
        children.clear();
        children.add(new PreviewNode(path + "/" + ncer.getFileName(), this,ncer));
        SliderNode cell = new SliderNode(path + "/" + CELL_NODE, this, 0.0f, 0.0f, ncer.getCellsCount()-1, true){
            @Override
            protected void onValueFloatChanged() {
                super.onValueFloatChanged();
                try {
                    recolorImage();
                } catch (NitroException e){

                }
            }
        };
        cell.setPrecisionIndexAndValue(4);
        children.add(cell);

        children.add(createZoom());

        ButtonNode selectNcgr = new ButtonNode(path + "/" + SELECT_NCGR_FILE,this);
        selectNcgr.registerAction(ActivateByType.RELEASE, this::selectNcgr);
        children.add(selectNcgr);

        ButtonNode selectNcLr = new ButtonNode(path + "/" + SELECT_NCLR_FILE,this);
        selectNcLr.registerAction(ActivateByType.RELEASE, this::selectPalette);
        children.add(selectNcLr);
    }

    public void recolorImage() throws NitroException {
        imageable.getNCGR().recolorImage();

        SliderNode cellNode = (SliderNode) findChildByName(CELL_NODE);

        PImage pImage = resizeImage(imageable.getImage((int) cellNode.valueFloat));

        ((PreviewNode) findChildByName(imageable.getFileName())).loadImage(pImage);

        this.window.windowSizeX = autosuggestWindowWidthForContents();
        this.window.windowSizeXForContents = autosuggestWindowWidthForContents();
    }

    @Override
    public float autosuggestWindowWidthForContents() {
        float suggested = super.autosuggestWindowWidthForContents();
        if (imageable.getNCGR() != null) {
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
