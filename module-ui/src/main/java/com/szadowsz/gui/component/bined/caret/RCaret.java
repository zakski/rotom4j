package com.szadowsz.gui.component.bined.caret;

import com.szadowsz.gui.component.RComponent;
import com.szadowsz.gui.component.bined.settings.CodeAreaSection;
import com.szadowsz.gui.component.oldbinary.CodeAreaCaretPosition;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.Timer;
import java.util.TimerTask;


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
    protected CursorRenderingMode renderingMode = CursorRenderingMode.NEGATIVE;

    protected int blinkRate = 0;
    protected Timer blinkTimer = null;

    public RCaret(RComponent component) {
        this.component = component;
        privateSetBlinkRate(DEFAULT_BLINK_RATE);
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

    public PVector getMagicCaretPosition() {
        return magicCaretPosition;
    }

    /**
     * Returns currently active section.
     *
     * @return section
     */
    public CodeAreaSection getSection() {
        return caretPosition.getSection().orElse(CodeAreaSection.CODE_MATRIX);
    }

    public boolean isSelectionVisible() {
        return selectionVisible;
    }

    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets current caret position to provided value.
     *
     * @param caretPosition caret position
     */
    public void setCaretPosition(RCaretPos caretPosition) {
        if (caretPosition != null) {
            this.caretPosition.setPosition(caretPosition);
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

    /**
     * Sets current caret position to given position preserving section.
     *
     * @param dataPosition data position
     * @param codeOffset code offset
     */
    public void setCaretPosition(long dataPosition, int codeOffset) {
        caretPosition.setDataPosition(dataPosition);
        caretPosition.setCodeOffset(codeOffset);
        resetBlink();
    }

    public void setCaretPosition(long dataPosition, int codeOffset, CodeAreaSection section) {
        caretPosition.setDataPosition(dataPosition);
        caretPosition.setCodeOffset(codeOffset);
        caretPosition.setSection(section);
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

    public void setMagicCaretPosition(PVector magicCaretPosition) {
        this.magicCaretPosition = magicCaretPosition;
    }

    public void setSection(CodeAreaSection section) {
        caretPosition.setSection(section);
        resetBlink();
    }

    public void setSelectionVisible(boolean selectionVisible) {
        this.selectionVisible = selectionVisible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void draw(PGraphics g) {
    }


    public void setBlinkRate(int rate) {
        this.rate = rate;
    }

    public int getBlinkRate() {
        return rate;
    }

    public int getDot() {
        return dot;
    }

    public int getMark() {
        return 0;
    }

    public void setDot(int dot) {
        this.dot = dot;
    }

    public void moveDot(int dot) {
        this.dot = dot;
    }
}