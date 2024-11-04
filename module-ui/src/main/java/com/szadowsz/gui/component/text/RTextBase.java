package com.szadowsz.gui.component.text;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.group.folder.RFolder;
import com.szadowsz.gui.config.theme.RColorConverter;
import com.szadowsz.gui.config.text.RFontStore;
import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.config.text.RTextConstants;
import com.szadowsz.gui.config.theme.RColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import processing.core.PGraphics;

import java.awt.font.TextAttribute;

import static com.old.ui.store.LayoutStore.cell;
import static processing.core.PConstants.*;

/**
 * Base class for any component that primarily uses text.
 */
public class RTextBase extends RComponent {
    // TODO Component Stub : WIP

    private final String regexLookBehindForNewLine = "(?<=\\n)";

    /** The styled text used by this component */
    protected StyledString stext = new StyledString("");

    // Alignment within zone
    protected RAlign textAlignH = RAlign.LEFT;
    protected RAlign textAlignV = RAlign.MIDDLE;

    protected boolean bufferInvalid = true;
    protected boolean shouldDisplayHeaderRow;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui          the gui for the window that the component is drawn under
     * @param path         the path in the component tree
     * @param parentFolder the parent component folder reference // TODO consider if needed
     */
    protected RTextBase(RotomGui gui, String path, RFolder parentFolder) {
        super(gui, path, parentFolder);
    }

    /**
     * Apply the style to the whole text
     *
     * @param style the style attribute
     * @param value 'amount' to apply
     */
    protected void addAttributeImpl(TextAttribute style, Object value) {
        stext.addAttribute(style, value);
        bufferInvalid = true;
    }

    /**
     * Apply the style to a portion of the string
     *
     * @param style the style attribute
     * @param value 'amount' to apply
     * @param s     first character to be included for styling
     * @param e     the first character not to be included for styling
     */
    protected void addAttributeImpl(TextAttribute style, Object value, int s, int e) {
        if (s >= e)
            return;
        if (s < 0)
            s = 0;
        if (e > stext.length())
            e = stext.length();
        stext.addAttribute(style, value, s, e);
        bufferInvalid = true;
    }

    /**
     * Draw Gradient Triangle
     *
     * @param pg processing graphics context
     * @param x starting x coordinate
     * @param y starting y coordinate
     * @param w width
     * @param h height
     * @param colorLeft left gradient color
     * @param colorRight right gradient color
     */
    protected void drawGradientRectangle(PGraphics pg, float x, float y, float w, float h, int colorLeft, int colorRight){
        pg.pushMatrix();
        pg.pushStyle();
        pg.translate(x, y);
        pg.noStroke();
        pg.beginShape();
        pg.fill(colorLeft);
        pg.vertex(0,0);
        pg.fill(colorRight);
        pg.vertex(w, 0);
        pg.vertex(w, h);
        pg.fill(colorLeft);
        pg.vertex(0, h);
        pg.endShape(CLOSE);
        pg.popStyle();
        pg.popMatrix();
    }

    /**
     * Draw Text Content
     *
     * @param pg processing graphics context
     * @param contentToDraw text to draw
     */
    protected void drawContent(PGraphics pg, String contentToDraw) {
        fillForeground(pg);
        pg.textAlign(LEFT, CENTER);
        String[] lines = contentToDraw.split(regexLookBehindForNewLine);
        pg.pushMatrix();
        float contentMarginLeft = 0.3f * RLayoutStore.getCell();
        if(shouldDisplayHeaderRow){
            pg.translate(0, RLayoutStore.getCell());
        }
        pg.translate(0, 0);
        pg.textFont(RFontStore.getSideFont());
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].replace("\n", "");
            boolean isLastLine = i == lines.length - 1;
            float textFieldWidth = size.x - contentMarginLeft - RFontStore.getMarginX() + (isLastLine ? -RLayoutStore.getCell() : 0);
            float fadeoutWidth = RLayoutStore.getCell() * 1.5f;
            String lineThatFitsWindow = RFontStore.substringToFit(pg, line, textFieldWidth);
            if (isLastLine) {
                // last line is displayed "fromEnd" because you want to see what you're typing,
                // and you never want to draw the right indicator there
                lineThatFitsWindow = RFontStore.substringToFit(pg, line, textFieldWidth);
            }
            pg.translate(0, RLayoutStore.getCell());
            pg.text(lineThatFitsWindow, contentMarginLeft + RFontStore.getMarginX(), -RFontStore.getMarginY());

            if(!isMouseOver){
                int bgColor = RThemeStore.getRGBA(RColorType.NORMAL_BACKGROUND);
                if(isLastLine){
                    boolean isTrimmedToFit = lineThatFitsWindow.length() < line.length();
                    if(isTrimmedToFit){
                        drawGradientRectangle(pg, 0, -RLayoutStore.getCell(), fadeoutWidth, RLayoutStore.getCell(),
                                bgColor, RColorConverter.toTransparent(bgColor));
                    }
                }else{
                    drawGradientRectangle(pg, size.x-fadeoutWidth, -RLayoutStore.getCell(), fadeoutWidth, RLayoutStore.getCell(),
                            RColorConverter.toTransparent(bgColor), bgColor);

                }
            }
        }
        pg.popMatrix();
    }
    
    @Override
    protected void drawBackground(PGraphics pg) {
        // NOOP
    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        fillBackground(pg);
        String toDisplay = stext.getPlainText();
        int lineCount = toDisplay.split(regexLookBehindForNewLine).length + (toDisplay.endsWith("\n") ? 1 : 0);
        if(shouldDisplayHeaderRow){
            drawTextLeft(pg, name);
            lineCount += 1;
        }
        heightInCells = lineCount;
        String contentToDraw = toDisplay.isEmpty() ? "..." : toDisplay;
        fillForeground(pg);
        drawContent(pg, contentToDraw);
    }

    /**
     * Get the text used for this control.
     *
     * @return the displayed text with styling
     */
    public StyledString getStyledText() {
        return stext;
    }

    /**
     * Get the text used for this control.
     *
     * @return the displayed text without styling
     */
    public String getText() {
        return stext.getPlainText();
    }

    @Override
    public String getValueAsString() {
        return getText();
    }

    /**
     * Set the horizontal and/or vertical text alignment. Use the constants in
     * GAlign e.g. <b>GAlign.LEFT</b> <br>
     *
     * If you want to set just one of these then pass null in the other
     *
     * @param horz GAlign.LEFT, CENTER, RIGHT or JUSTIFY
     * @param vert GAlign.TOP, MIDDLE, BOTTOM
     */
    public void setTextAlign(RAlign horz, RAlign vert) {
        if (horz != null && horz.isHorzAlign()) {
            textAlignH = horz;
            stext.setJustify(textAlignH == RAlign.JUSTIFY);
        }
        if (vert != null && vert.isVertAlign()) {
            textAlignV = vert;
        }
        bufferInvalid = true;
    }

    /**
     * Make the selected characters bold. <br>
     * Characters affected are &ge; start and &lt; end
     *
     * @param start the first character to style
     * @param end   the first character not to style
     */
    public void setTextBold(int start, int end) {
        addAttributeImpl(RTextConstants.WEIGHT, RTextConstants.WEIGHT_BOLD, start, end);
    }

    /**
     * Make all the characters bold.
     */
    public void setTextBold() {
        addAttributeImpl(RTextConstants.WEIGHT, RTextConstants.WEIGHT_BOLD);
    }

    /**
     * Make the selected characters italic. <br>
     * Characters affected are &ge; start and &lt; end
     *
     * @param start the first character to style
     * @param end   the first character not to style
     */
    public void setTextItalic(int start, int end) {
        addAttributeImpl(RTextConstants.POSTURE, RTextConstants.POSTURE_OBLIQUE, start, end);
    }

    /**
     * Make all the characters italic.
     */
    public void setTextItalic() {
        addAttributeImpl(RTextConstants.POSTURE, RTextConstants.POSTURE_OBLIQUE);
    }

    /**
     * Combines setting the text and text alignment in one method. <br>
     *
     * If you want to set just one of the alignments then pass null in the other.
     *
     * @param text the text to display
     * @param horz GAlign.LEFT, CENTER, RIGHT or JUSTIFY
     * @param vert GAlign.TOP, MIDDLE, BOTTOM
     */
    public void setText(String text, RAlign horz, RAlign vert) {
        text = text == null || text.length() == 0 ? " " : text;
        float contentMarginLeft = 0.3f * RLayoutStore.getCell();
        float textFieldWidth = size.x - contentMarginLeft - RFontStore.getMarginX();
        stext = new StyledString(text, (int) textFieldWidth);
        setTextAlign(horz, vert);
        bufferInvalid = true;
    }

    @Override
    public float suggestWidth() {
        return RFontStore.calcMainTextWidth(name,RLayoutStore.getCell()) +
                Math.max(cell * 2,RFontStore.calcMainTextWidth(getValueAsString(),RLayoutStore.getCell()));
    }

    @Override
    public void updateCoordinates(float bX, float bY, float rX, float rY, float w, float h) {
        super.updateCoordinates(bX, bY, rX, rY, w, h);
    }
}
