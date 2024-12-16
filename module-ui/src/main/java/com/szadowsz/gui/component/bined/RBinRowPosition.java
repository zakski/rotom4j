package com.szadowsz.gui.component.bined;

import com.szadowsz.gui.RotomGui;
import com.szadowsz.gui.component.bined.bounds.RBinDimensions;
import com.szadowsz.gui.component.bined.bounds.RBinRect;
import com.szadowsz.gui.component.bined.scroll.RBinScrollPos;
import com.szadowsz.gui.component.bined.settings.BackgroundPaintMode;
import com.szadowsz.gui.component.bined.settings.CodeType;
import com.szadowsz.gui.component.bined.sizing.RBinMetrics;
import com.szadowsz.gui.component.bined.sizing.RBinStructure;
import com.szadowsz.gui.component.bined.utils.RBinUtils;
import com.szadowsz.gui.config.text.RFontStore;
import com.szadowsz.gui.config.theme.RColorType;
import com.szadowsz.gui.config.theme.RThemeStore;
import processing.core.PFont;
import processing.core.PGraphics;

public class RBinRowPosition extends RBinComponent {

    protected RBinEditor editor;

    protected final RBinDimensions dimensions;
    protected final RBinMetrics metrics;
    protected final RBinStructure structure;
    protected final RBinVisibility visibility;
    protected final RBinScrollPos scrollPosition;

    protected PFont font;

    /**
     * Default Constructor
     * <p>
     * We generally assume that width and height are determined elsewhere: the length of text, the size of an image, etc.
     *
     * @param gui    the gui for the window that the component is drawn under
     * @param path   the path in the component tree
     * @param editor the parent component reference
     */
    public RBinRowPosition(RotomGui gui, String path, RBinEditor editor) {
        super(gui, path, editor);
        this.editor = editor;
        dimensions = editor.getDimensions();
        metrics = editor.getMetrics();
        structure = editor.getStructure();
        visibility = editor.getVisibility();
        scrollPosition = editor.getScrollPos();
        font = RFontStore.getMainFont();
    }

    @Override
    protected void drawBackground(PGraphics pg) {

    }

    @Override
    protected void drawForeground(PGraphics pg, String name) {
        int bytesPerRow = structure.getBytesPerRow();
        long dataSize = editor.getDataSize();
        int rowHeight = metrics.getRowHeight();
        int characterWidth = metrics.getCharacterWidth();
        int subFontSpace = metrics.getSubFontSpace();
        int rowsPerRect = dimensions.getRowsPerRect();
        RBinRect rowPosRectangle = dimensions.getRowPositionAreaRectangle();
        RBinRect dataViewRectangle = dimensions.getDataViewRectangle();
        RBinEditor.RowDataCache rowDataCache = editor.getRowDataCache();
        int rowPositionLength = editor.getRowPositionLength();
        BackgroundPaintMode backgroundPaintMode = editor.getBackgroundPaintMode();
        //RBinRect clipBounds = g.getClipBounds();
        //g.setClip(clipBounds != null ? clipBounds.intersection(rowPosRectangle) : rowPosRectangle);

        pg.textFont(font);
        pg.fill(RThemeStore.getRGBA(RColorType.NORMAL_BACKGROUND)); //  g.setColor(colorsProfile.getTextBackground());
        pg.rect(rowPosRectangle.getX(), rowPosRectangle.getY(), rowPosRectangle.getWidth(), rowPosRectangle.getHeight());

        if (backgroundPaintMode == BackgroundPaintMode.STRIPED) {
            long dataPosition = scrollPosition.getRowPosition() * bytesPerRow + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : bytesPerRow);
            float stripePositionY = rowPosRectangle.getY() - scrollPosition.getRowOffset() + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : rowHeight);
            pg.fill(RThemeStore.getRGBA(RColorType.FOCUS_BACKGROUND)); //  g.setColor(colorsProfile.getAlternateBackground());
            for (int row = 0; row <= rowsPerRect / 2; row++) {
                if (dataPosition > dataSize) {
                    break;
                }

                pg.rect(rowPosRectangle.getX(), stripePositionY, rowPosRectangle.getWidth(), rowHeight);
                stripePositionY += rowHeight * 2;
                dataPosition += bytesPerRow * 2;
            }
        }

        long dataPosition = bytesPerRow * scrollPosition.getRowPosition();
        float positionY = rowPosRectangle.getY() + rowHeight - subFontSpace - scrollPosition.getRowOffset();
        pg.stroke(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND)); //  g.setColor(colorsProfile.getTextColor());
        for (int row = 0; row <= rowsPerRect; row++) {
            if (dataPosition > dataSize) {
                break;
            }

            RBinUtils.longToBaseCode(rowDataCache.rowPositionCode, 0, dataPosition < 0 ? 0 : dataPosition, CodeType.HEXADECIMAL.getBase(), rowPositionLength, true, editor.getCodeCharactersCase());
            drawCenteredChars(pg, rowDataCache.rowPositionCode, 0, rowPositionLength, characterWidth, rowPosRectangle.getX(), positionY);

            positionY += rowHeight;
            dataPosition += bytesPerRow;
            if (dataPosition < 0) {
                break;
            }
        }

        // Decoration lines
        pg.stroke(RThemeStore.getRGBA(RColorType.NORMAL_FOREGROUND)); // g.setColor(colorsProfile.getDecorationLine());
        float lineX = rowPosRectangle.getX() + rowPosRectangle.getWidth() - (characterWidth / 2);
        if (lineX >= rowPosRectangle.getX()) {
            pg.line(lineX, dataViewRectangle.getY(), lineX, dataViewRectangle.getY() + dataViewRectangle.getHeight());
        }
        pg.line(dataViewRectangle.getX(), dataViewRectangle.getY() - 1, dataViewRectangle.getX() + dataViewRectangle.getWidth(), dataViewRectangle.getY() - 1);

//        pg.setClip(clipBounds);
    }

    @Override
    public float suggestWidth() {
        return editor.getRowPositionLength();
    }
}
