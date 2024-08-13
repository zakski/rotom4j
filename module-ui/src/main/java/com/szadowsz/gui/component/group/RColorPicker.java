package com.szadowsz.gui.component.group;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.folder.RFolder;
import com.szadowsz.gui.component.input.color.RColorPreview;
import com.szadowsz.gui.component.input.color.RColorSlider;
import com.szadowsz.gui.component.layout.RLayoutConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PVector;

import java.awt.*;
import java.util.List;

/**
 * Overall grouping of components (ColorPreview, ColorSLiders, etc.) that make up user color selection controls
 */
public class RColorPicker extends RGroup {
    private static final Logger LOGGER = LoggerFactory.getLogger(RColorPicker.class);

    protected static final String PREVIEW_NODE = "Preview";
    protected static final String R_NODE_NAME = "R";
    protected static final String G_NODE_NAME = "G";
    protected static final String B_NODE_NAME = "B";
    protected static final String A_NODE_NAME = "A";
    protected static final String HEX_NODE = "Hex";

    protected Color color;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui          the gui for the window that the component is drawn under
     * @param path         the path in the component tree
     * @param parentFolder the parent component folder reference // TODO consider if needed
     */
    protected RColorPicker(RotomGui gui, String path, RFolder parentFolder, Color c) {
        super(gui, path, parentFolder);
        this.color = c;
        initNodes();
    }

    protected void initNodes() {
        if (!children.isEmpty()) {
            return;
        }

        children.add(new RColorPreview(gui,path + "/" + PREVIEW_NODE, parent,this,color));

        // TODO try vertical sliders
        children.add(new RColorSlider(gui,path + "/" + R_NODE_NAME, parent,this,color.getRed()));
        children.add(new RColorSlider(gui,path + "/" + G_NODE_NAME, parent,this,color.getGreen()));
        children.add(new RColorSlider(gui,path + "/" + B_NODE_NAME, parent,this,color.getBlue()));
        if (showAlpha){
            children.add(new RColorSlider(gui,path + "/" + A_NODE_NAME, parent,this,color.getAlpha()));
        }
        children.add(new RColorHex(gui,path + "/" + HEX_NODE, parent,this,color));
    }

    protected void setChildValue(String nodeName, float valueFloat) {
        RColorSlider node = ((RColorSlider) findChildByName(nodeName));
        node.setValueFromParent(valueFloat);
    }

    @Override
    protected PVector calcPreferredSize(List<RComponent> components) {
        return null;
    }

    @Override
    public RLayoutConfig getLayoutConfig() {
        return null;
    }

    @Override
    protected void drawBackground(PGraphics pg) {

    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {

    }

    @Override
    public float getRequiredWidthForHorizontalLayout() {
        return 0;
    }

    public void loadValuesFromRGB() {
        setChildValue(COLOR_NODE_NAME, index);
        setChildValue(R_NODE_NAME, color.getRed());
        setChildValue(G_NODE_NAME, color.getGreen());
        setChildValue(B_NODE_NAME, color.getBlue());
    }
}
