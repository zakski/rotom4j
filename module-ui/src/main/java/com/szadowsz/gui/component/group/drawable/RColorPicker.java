package com.szadowsz.gui.component.group.drawable;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.RGroup;
import com.szadowsz.gui.component.group.RGroupDrawable;
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
 * Overall grouping of components (ColorPreview, ColorSliders, etc.) that make up user color selection controls
 */
public class RColorPicker extends RGroupDrawable {
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
     * @param parent       the parent component reference // TODO consider if needed
     */
    public RColorPicker(RotomGui gui, String path, RGroup parent, Color c, boolean showAlpha) {
        super(gui, path, parent);
        this.color = c;
        this.showAlpha = showAlpha;
        initNodes();
    }

    /**
     * Utility Method to Create the Children
     */
    protected void initNodes() {
        if (!children.isEmpty()) {
            return;
        }

        children.add(new RColorPreview(gui, path + "/" + PREVIEW_NODE, this, color));

        // TODO try vertical sliders
        RColorSlider r = new RColorSlider(gui, path + "/" + R_NODE_NAME, this, color.getRed());
        r.initSliderBackgroundShader();
        children.add(r);
        RColorSlider g = new RColorSlider(gui, path + "/" + G_NODE_NAME, this, color.getGreen());
        g.initSliderBackgroundShader();
        children.add(g);
        RColorSlider b = new RColorSlider(gui, path + "/" + B_NODE_NAME, this, color.getBlue());
        b.initSliderBackgroundShader();
        children.add(b);

        if (showAlpha) {
            RColorSlider a = new RColorSlider(gui, path + "/" + A_NODE_NAME, this, color.getAlpha());
            a.initSliderBackgroundShader();
            children.add(a);
        }
        //children.add(new RColorHex(gui, path + "/" + HEX_NODE, this));
    }

    /**
     * Method to get the value of a color part
     *
     * @param nodeName Color component slider to find
     * @return the current value of the color component, i.e. R, G, B or A
     */
    protected int getSliderValue(String nodeName) {
        RColorSlider node = ((RColorSlider) findChildByName(nodeName));
        return node.getValueAsInt();
    }

    /**
     * Update the Preview Color
     */
    protected void setPreviewColor(){
        RColorPreview preview = (RColorPreview) findChildByName(PREVIEW_NODE);
        preview.setColor(color);
    }

    /**
     * Set Color Component Value
     *
     * @param partName name of the color part to change
     * @param value value of the part as an int
     */
    protected void setSliderValue(String partName, int value) {
        RColorSlider slider = ((RColorSlider) findChildByName(partName));
        if (slider != null) {
            slider.setValueFromParent(value);
        }
    }

    /**
     * Draw Child Component
     *
     * @param pg    Processing Graphics Context
     * @param child draw
     */
    protected void drawChildComponent(PGraphics pg, RComponent child) {
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
    protected void drawHorizontalSeparator(PGraphics pg, int width) {
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
    protected void drawVerticalSeparator(PGraphics pg) {
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
        LOGGER.debug("Drawing ColorPicker Group {} [{}, {}]", name, size.x, size.y);
        super.drawForeground(pg, name);
    }

    /**
     * Reload the individual RGB values into the display nodes from the current Color
     */
    public void loadValuesFromRGB() {
        if (showAlpha) {
            color = new Color(getSliderValue(R_NODE_NAME),
                    getSliderValue(G_NODE_NAME),
                    getSliderValue(B_NODE_NAME),
                    getSliderValue(A_NODE_NAME));
        } else {
            color = new Color(getSliderValue(R_NODE_NAME),
                    getSliderValue(G_NODE_NAME),
                    getSliderValue(B_NODE_NAME));
        }
        setPreviewColor();
        getParentWindow().redrawBuffer();
    }

    @Override
    public float suggestWidth() {
        return getPreferredSize().x;
    }

    /**
     * Calculate the Hex String to display
     *
     * @return hex id of the current color
     */
    public String getHexString() {
        return String.format("%06X", 0xFFFFFF & color.getRGB());
    }

    public float getRed() {
        return color.getRed();
    }

    public float getGreen() {
        return color.getGreen();
    }

    public float getBlue() {
        return color.getBlue();
    }

    @Override
    public boolean canChangeLayout() {
        return false;
    }

    @Override
    public void setLayout(RLayoutBase layout) {
        LOGGER.warn("Cannot change layout for RColorPicker at Path: {}", path);
    }

    public void setRed(int red) {
        color = new Color(red, color.getGreen(), color.getBlue(),color.getAlpha());
        setSliderValue(R_NODE_NAME, red);
        setPreviewColor();
    }

    public void setGreen(int green) {
        color = new Color(color.getRed(), green, color.getBlue(),color.getAlpha());
        setSliderValue(G_NODE_NAME, green);
        setPreviewColor();
    }

    public void setBlue(int blue) {
        color = new Color(color.getRed(), color.getGreen(), blue,color.getAlpha());
        setSliderValue(B_NODE_NAME, blue);
        setPreviewColor();
    }

}
