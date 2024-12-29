package com.szadowsz.gui.component.bined.cursor;

import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.bined.settings.RCodeAreaSection;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Interface for code area caret.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class RCaret {
    protected static final int DOUBLE_CURSOR_WIDTH = 2;
    protected static final int DEFAULT_BLINK_RATE = 450;

    private RComponent component;

    private boolean visible = true;
    private boolean selectionVisible = false;

    private int rate;
    private int dot;

    private PVector magicCaretPosition = new PVector();
    private final RCaretPos caretPosition = new RCaretPos();
    protected RCursorRenderingMode renderingMode = RCursorRenderingMode.NEGATIVE;

    protected int blinkRate = 0;
    protected Timer blinkTimer = null;

    public RCaret(RComponent component) {
        this.component = component;
        privateSetBlinkRate(DEFAULT_BLINK_RATE);
    }

    public static int getCursorThickness(RCursorShape cursorShape, int characterWidth, int lineHeight) {
        switch (cursorShape) {
            case INSERT:
                return DOUBLE_CURSOR_WIDTH;
            case OVERWRITE:
            case MIRROR:
                return characterWidth;
        }

        return -1;
    }

    private void privateSetBlinkRate(int blinkRate) {
        if (blinkRate < 0) {
            throw new IllegalArgumentException("Blink rate cannot be negative");
        }

        this.blinkRate = blinkRate;
        if (blinkTimer != null) {
            blinkTimer.cancel();
            blinkTimer = null;
            visible = true;
        }
        if (blinkRate > 0) {
            blinkTimer = new Timer("blinkRate", true);
            blinkTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    visible = !visible;
                }
            }, blinkRate, blinkRate);
        }
    }
    public void resetBlink() {
        if (blinkTimer != null) {
            visible = true;
            privateSetBlinkRate(blinkRate);
        }
    }

    public int getBlinkRate() {
        return rate;
    }

    /**
     * Returns caret position.
     * <p>
     * Returned value should not be cast for editing.
     *
     * @return caret position
     */
    public RCaretPos getCaretPosition(){
        return caretPosition;
    }

    public int getCodeOffset() {
        return caretPosition.getCodeOffset();
    }

    public long getDataPosition() {
        return caretPosition.getDataPosition();
    }

    public int getDot() {
        return dot;
    }

    public PVector getMagicCaretPosition() {
        return magicCaretPosition;
    }

    public int getMark() {
        return 0;
    }

    public RCursorRenderingMode getRenderingMode() {
        return renderingMode;
    }

    /**
     * Returns currently active section.
     *
     * @return section
     */
    public RCodeAreaSection getSection() {
        return caretPosition.getSection().orElse(RCodeAreaSection.CODE_MATRIX);
    }

    public boolean isSelectionVisible() {
        return selectionVisible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setBlinkRate(int rate) {
        this.rate = rate;
    }

    /**
     * Sets current caret position to provided value.
     *
     * @param caretPosition caret position
     */
    public void setCaretPosition(RCaretPos caretPosition) {
        if (caretPosition != null) {
            this.caretPosition.setCaretPosition(caretPosition);
        } else {
            this.caretPosition.reset();
        }
        resetBlink();
    }

    /**
     * Sets current caret position to given position resetting offset and
     * preserving section.
     *
     * @param dataPosition data position
     */
    public void setCaretPosition(long dataPosition) {
        caretPosition.setDataPosition(dataPosition);
        caretPosition.setCodeOffset(0);
        resetBlink();
    }

    public void setCodeOffset(int codeOffset) {
        caretPosition.setCodeOffset(codeOffset);
        resetBlink();
    }

    public void setDataPosition(long dataPosition) {
        caretPosition.setDataPosition(dataPosition);
        resetBlink();
    }

    public void setSection(RCodeAreaSection section) {
        caretPosition.setSection(section);
        resetBlink();
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void draw(PGraphics g) {
    }
}