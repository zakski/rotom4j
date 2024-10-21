package com.szadowsz.gui.component.group;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.component.input.color.RColorHex;
import com.szadowsz.gui.component.input.color.RColorPreview;
import com.szadowsz.gui.component.input.color.RColorSlider;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.config.theme.RThemeStore;
import com.szadowsz.gui.layout.RDirection;
import com.szadowsz.gui.layout.RLayoutBase;
import com.szadowsz.gui.layout.RLinearLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;
import processing.core.PVector;

import java.awt.*;

import static com.szadowsz.gui.config.theme.RColorType.WINDOW_BORDER;
import static processing.core.PConstants.SQUARE;


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
    protected boolean showAlpha;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui          the gui for the window that the component is drawn under
     * @param path         the path in the component tree
     * @param parentFolder the parent component folder reference // TODO consider if needed
     */
    public RColorPicker(RotomGui gui, String path, RFolder parentFolder, Color c, boolean showAlpha) {
        super(gui, path, parentFolder);
        this.color = c;
        this.showAlpha = showAlpha;
        initNodes();
    }

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui          the gui for the window that the component is drawn under
     * @param path         the path in the component tree
     * @param parentFolder the parent component folder reference // TODO consider if needed
     */
    public RColorPicker(RotomGui gui, String path, RFolder parentFolder, Color c) {
        this(gui, path, parentFolder,c,false);
    }

    protected void initNodes() {
        if (!children.isEmpty()) {
            return;
        }

        children.add(new RColorPreview(gui,path + "/" + PREVIEW_NODE,this,color));

        // TODO try vertical sliders
        RColorSlider r = new RColorSlider(gui,path + "/" + R_NODE_NAME,this,color.getRed());
        r.initSliderBackgroundShader();
        children.add(r);
        RColorSlider g = new RColorSlider(gui,path + "/" + G_NODE_NAME,this,color.getGreen());
        g.initSliderBackgroundShader();
        children.add(g);
        RColorSlider b = new RColorSlider(gui,path + "/" + B_NODE_NAME,this,color.getBlue());
        b.initSliderBackgroundShader();
        children.add(b);

        if (showAlpha){
            RColorSlider a = new RColorSlider(gui,path + "/" + A_NODE_NAME,this,color.getAlpha());
            a.initSliderBackgroundShader();
            children.add(a);
        }
        children.add(new RColorHex(gui,path + "/" + HEX_NODE, this));
    }

    protected void setChildValue(String sliderName, float valueFloat) {
        RColorSlider slider = ((RColorSlider) findChildByName(sliderName));
        if (slider != null) {
            slider.setValueFromParent(valueFloat);
        }
    }
//    @Override
//    public RLayoutConfig getCompLayoutConfig() {
//        return layoutConfig;
//    }

    @Override
    public boolean canChangeLayout(){
        return false;
    }

    @Override
    public void setLayout(RLayoutBase layout) {
        LOGGER.warn("Cannot change layout for RColorPicker at Path: {}",path);
    }

    @Override
    protected void drawBackground(PGraphics pg) {

    }

    /**
     * Draw Child Component
     *
     * @param child
     */
    private void drawChildComponent(PGraphics pg, RComponent child) {
        pg.pushMatrix();
        pg.pushStyle();
        child.draw(pg);
        pg.popStyle();
        pg.popMatrix();
    }

    /**
     * Draw A Horizontal separator between two nodes
     *
     * @param pg    Processing Graphics Context
     * @param width separator width
     */
    private void drawHorizontalSeparator(PGraphics pg, int width) {
        boolean show = RLayoutStore.isShowHorizontalSeparators();
        float weight = RLayoutStore.getHorizontalSeparatorStrokeWeight();
        if (show) {
            pg.strokeCap(SQUARE);
            pg.strokeWeight(weight);
            pg.stroke(RThemeStore.getRGBA(WINDOW_BORDER));
            pg.line(0, 0, width, 0);
        }
    }

    /**
     * Draw A Horizontal separator between two nodes
     *
     * @param pg Processing Graphics Context
     */
    private void drawVerticalSeparator(PGraphics pg) {
        //boolean show = LayoutStore.isShowHorizontalSeparators();
        float weight = RLayoutStore.getHorizontalSeparatorStrokeWeight();
        // if (show) {
        pg.strokeCap(SQUARE);
        pg.strokeWeight(weight);
        pg.stroke(RThemeStore.getRGBA(WINDOW_BORDER));
        pg.line(0, 0, 0, pg.height);
        // }
    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        LOGGER.debug("Drawing ColorPicker Group {} [{}, {}]", name,size.x,size.y);
        int index = 0;
        for (RComponent component : children) {
            if (!component.isVisible()) {
                index++;
                continue;
            }
            pg.pushMatrix();
            pg.translate(component.getRelPosX(), component.getRelPosY());
            drawChildComponent(pg, component);
            if (index > 0) { // TODO if as to kind of separator to draw
                // separator
                if (layout instanceof RLinearLayout linear) {
                    pg.pushStyle();
                    if (linear.getDirection() == RDirection.VERTICAL) {
                        drawHorizontalSeparator(pg, (int) size.x);
                    } else {
                        drawVerticalSeparator(pg);
                    }
                    pg.popStyle();
                }
            }
            index++;
            pg.popMatrix();
        }
    }

    public void loadValuesFromRGB() {
        setChildValue(R_NODE_NAME, color.getRed());
        setChildValue(G_NODE_NAME, color.getGreen());
        setChildValue(B_NODE_NAME, color.getBlue());
        setChildValue(A_NODE_NAME, color.getAlpha());
    }

    @Override
    public float suggestWidth() {
        return getPreferredSize().x;
    }

    @Override
    public PVector getPreferredSize() {
        return layout.calcPreferredSize(getParentFolder().getName(),children);
    }

    public String getHexString() {
        return String.format("%06X", 0xFFFFFF & Color.BLUE.getRGB());
    }
}
