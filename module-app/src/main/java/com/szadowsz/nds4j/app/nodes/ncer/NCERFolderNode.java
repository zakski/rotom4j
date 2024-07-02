package com.szadowsz.nds4j.app.nodes.ncer;

import com.szadowsz.nds4j.app.Processing;
import com.szadowsz.nds4j.app.utils.FileChooser;
import com.szadowsz.nds4j.data.nfs.cells.CellInfo;
import com.szadowsz.nds4j.exception.NitroException;
import com.szadowsz.nds4j.file.nitro.NCER;
import com.szadowsz.nds4j.file.nitro.NCGR;
import com.szadowsz.nds4j.file.nitro.NCLR;
import com.szadowsz.nds4j.file.nitro.NSCR;
import com.szadowsz.ui.constants.GlobalReferences;
import com.szadowsz.ui.input.ActivateByType;
import com.szadowsz.ui.node.impl.ButtonNode;
import com.szadowsz.ui.node.impl.FolderNode;
import com.szadowsz.ui.node.impl.SliderNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;

import java.io.File;
import java.io.IOException;

import static com.szadowsz.ui.store.LayoutStore.cell;


public class NCERFolderNode extends FolderNode {
    protected static final Logger LOGGER = LoggerFactory.getLogger(NCERFolderNode.class);

    private final NCER ncer;

    private final String CELL_NODE = "Cell";
    private final String OAM_NODE = "OAM";
    private final String ZOOM_NODE = "Zoom";
    private final String SELECT_NCGR_FILE = "Select NCGR";
    private final String SELECT_NCLR_FILE = "Select NClR";

    public NCERFolderNode(String path, FolderNode parent, NCER ncer) throws NitroException {
        super(path, parent);
        this.ncer = ncer;
        children.clear();
        children.add(new NCERPreviewNode(path + "/" + ncer.getFileName(), this,ncer));
        children.add(new SliderNode(path + "/" + CELL_NODE, this, 0.0f, 0.0f, ncer.getCellsCount()-1, true){
            @Override
            protected void onValueFloatChanged() {
                super.onValueFloatChanged();
                SliderNode oam = ((SliderNode)findChildByName(OAM_NODE));
                CellInfo ncerCell = ncer.getCell((int) valueFloat);
                oam.setMaxValue(ncerCell.getOamCount()-1);
                ((NCERPreviewNode)findChildByName(ncer.getFileName())).loadImage(ncer.getImage((int) valueFloat, (int) oam.valueFloat));
            }
        });
        CellInfo ncerCell = ncer.getCell(0);
        children.add(new SliderNode(path + "/" + OAM_NODE, this, 0.0f, 0.0f, ncerCell.getOamCount()-1, true){
            @Override
            protected void onValueFloatChanged() {
                super.onValueFloatChanged();
                SliderNode cell = ((SliderNode)findChildByName(CELL_NODE));
                ((NCERPreviewNode)findChildByName(ncer.getFileName())).loadImage(ncer.getImage((int) cell.valueFloat, (int) valueFloat));
            }

        });
        children.add(new SliderNode(path + "/" + ZOOM_NODE, this, 1.0f, 1.0f, 4.0f, true){
            @Override
            protected void onValueFloatChanged() {
                super.onValueFloatChanged();
             //   ncer.setZoom(valueFloat);
                try {
                    recolorImage();
                } catch (NitroException e) {
                    throw new RuntimeException(e);
                }
            }

        });
        ButtonNode selectNcgr = new ButtonNode(path + "/" + SELECT_NCGR_FILE,this);
        selectNcgr.registerAction(ActivateByType.RELEASE, this::selectNcgr);
        children.add(selectNcgr);
        ButtonNode selectNcLr = new ButtonNode(path + "/" + SELECT_NCLR_FILE,this);
        selectNcLr.registerAction(ActivateByType.RELEASE, this::selectNclr);
        children.add(selectNcLr);
    }

    private void selectNcgr() {
        String lastPath = Processing.prefs.get("openNarcPath", System.getProperty("user.dir"));
        String ncgrPath = FileChooser.selectNcgrFile(GlobalReferences.gui.getGuiCanvas().parent, lastPath,SELECT_NCGR_FILE);
        if (ncgrPath != null) {
            Processing.prefs.put("openNarcPath", new File(ncgrPath).getParentFile().getAbsolutePath());
           try {
               LOGGER.info("Loading NCGR File: " + ncgrPath);
               ncer.setNCGR(NCGR.fromFile(ncgrPath));
               recolorImage();
               SliderNode cellNode = (SliderNode) findChildByName(CELL_NODE);
               SliderNode oamNode = (SliderNode) findChildByName(OAM_NODE);
               ((NCERPreviewNode) findChildByName(ncer.getFileName())).loadImage(ncer.getImage((int) cellNode.valueFloat,(int) oamNode.valueFloat));
               LOGGER.info("Loaded NCGR File: " + ncgrPath);
            } catch (IOException e) {
                LOGGER.error("NCGR Load Failed",e);
            }
        }
    }

    private void selectNclr() {
        String lastPath = Processing.prefs.get("openNarcPath", System.getProperty("user.dir"));
        String nclrPath = FileChooser.selectNclrFile(GlobalReferences.gui.getGuiCanvas().parent, lastPath,SELECT_NCLR_FILE);
        if (nclrPath != null) {
            Processing.prefs.put("openNarcPath", new File(nclrPath).getParentFile().getAbsolutePath());
            NCLR original = ncer.getNCLR();
            try {
                LOGGER.info("Loading NCLR File: " + nclrPath);
                ncer.setNCLR(NCLR.fromFile(nclrPath));
                recolorImage();
                SliderNode sliderNode = (SliderNode) findChildByName(CELL_NODE);
                SliderNode cellNode = (SliderNode) findChildByName(CELL_NODE);
                SliderNode oamNode = (SliderNode) findChildByName(OAM_NODE);
                ((NCERPreviewNode) findChildByName(ncer.getFileName())).loadImage(ncer.getImage((int) cellNode.valueFloat,(int) oamNode.valueFloat));
                LOGGER.info("Loaded NCLR File: " + nclrPath);
            } catch (IOException e) {
                LOGGER.error("NCLR Load Failed",e);
                ncer.setNCLR(original);
                try {
                    recolorImage();
                } catch (Throwable t){
                    throw new RuntimeException(t);
                }
            }
        }
    }

    public void recolorImage() throws NitroException {
        ncer.getNCGR().recolorImage();
        this.window.windowSizeX = autosuggestWindowWidthForContents();
    }

    @Override
    public float autosuggestWindowWidthForContents() {
        float suggested = super.autosuggestWindowWidthForContents();
        if (ncer.getNCGR() != null) {
            return Math.max(suggested,((NCERPreviewNode) children.get(0)).image.width);
        } else {
            return suggested;
        }
    }

    @Override
    protected void drawNodeForeground(PGraphics pg, String name) {
        drawLeftText(pg, name);
        drawRightBackdrop(pg, cell);
        //       drawRightTextToNotOverflowLeftText(pg, getValueAsString(), name, true); //we need to calculate how much space is left for value after the name is displayed
    }
}
