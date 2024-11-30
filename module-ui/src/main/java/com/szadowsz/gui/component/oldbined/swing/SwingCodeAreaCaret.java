/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.szadowsz.gui.component.oldbined.swing;

import com.szadowsz.gui.component.oldbined.CodeAreaCaret;
import com.szadowsz.gui.component.oldbined.CodeAreaCaretPosition;
import com.szadowsz.gui.component.oldbined.CodeAreaSection;
import com.szadowsz.gui.component.oldbined.CodeAreaUtils;
import com.szadowsz.gui.component.oldbined.basic.BasicCodeAreaSection;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Default implementation of code area caret.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class SwingCodeAreaCaret implements CodeAreaCaret {

    protected static final int DOUBLE_CURSOR_WIDTH = 2;
    protected static final int DEFAULT_BLINK_RATE = 450;

    protected final CaretChangeListener changeListener;
    protected final SwingCodeAreaCaretPosition caretPosition = new SwingCodeAreaCaretPosition();

    protected int blinkRate = 0;
    protected Timer blinkTimer = null;
    protected boolean cursorVisible = true;

    protected CursorRenderingMode renderingMode = CursorRenderingMode.NEGATIVE;

    public SwingCodeAreaCaret(CaretChangeListener changeListener) {
        CodeAreaUtils.requireNonNull(changeListener, "Change listener cannot be null");

        this.changeListener = changeListener;
        privateSetBlinkRate(DEFAULT_BLINK_RATE);
    }

    public static int getCursorThickness(CursorShape cursorShape, int characterWidth, int lineHeight) {
        switch (cursorShape) {
            case INSERT:
                return DOUBLE_CURSOR_WIDTH;
            case OVERWRITE:
            case MIRROR:
                return characterWidth;
        }

        return -1;
    }

    @Override
    public CodeAreaCaretPosition getCaretPosition() {
        return caretPosition;
    }

    public void resetBlink() {
        if (blinkTimer != null) {
            cursorVisible = true;
            blinkTimer.restart();
        }
    }

    private void notifyCaredChanged() {
        changeListener.notifyCaretChanged();
    }

    @Override
    public void setCaretPosition(CodeAreaCaretPosition caretPosition) {
        if (caretPosition != null) {
            this.caretPosition.setPosition(caretPosition);
        } else {
            this.caretPosition.reset();
        }
        resetBlink();
    }

    @Override
    public void setCaretPosition(long dataPosition) {
        caretPosition.setDataPosition(dataPosition);
        caretPosition.setCodeOffset(0);
        resetBlink();
    }

    @Override
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

    public long getDataPosition() {
        return caretPosition.getDataPosition();
    }

    public void setDataPosition(long dataPosition) {
        caretPosition.setDataPosition(dataPosition);
        resetBlink();
    }

    public int getCodeOffset() {
        return caretPosition.getCodeOffset();
    }

    public void setCodeOffset(int codeOffset) {
        caretPosition.setCodeOffset(codeOffset);
        resetBlink();
    }

    @Override
    public CodeAreaSection getSection() {
        return caretPosition.getSection().orElse(BasicCodeAreaSection.CODE_MATRIX);
    }

    public void setSection(CodeAreaSection section) {
        caretPosition.setSection(section);
        resetBlink();
    }

    public int getBlinkRate() {
        return blinkRate;
    }

    public void setBlinkRate(int blinkRate) {
        privateSetBlinkRate(blinkRate);
    }

    public boolean isCursorVisible() {
        return cursorVisible;
    }

    public CursorRenderingMode getRenderingMode() {
        return renderingMode;
    }

    public void setRenderingMode(CursorRenderingMode renderingMode) {
        CodeAreaUtils.requireNonNull(renderingMode);

        this.renderingMode = renderingMode;
        notifyCaredChanged();
    }

    private void privateSetBlinkRate(int blinkRate) {
        if (blinkRate < 0) {
            throw new IllegalArgumentException("Blink rate cannot be negative");
        }

        this.blinkRate = blinkRate;
        if (blinkTimer != null) {
            if (blinkRate == 0) {
                blinkTimer.stop();
                blinkTimer = null;
                cursorVisible = true;
                notifyCaredChanged();
            } else {
                blinkTimer.setDelay(blinkRate);
                blinkTimer.setInitialDelay(blinkRate);
            }
        } else if (blinkRate > 0) {
            blinkTimer = new Timer(blinkRate, new Blink());
            blinkTimer.setRepeats(true);
            blinkTimer.start();
        }
    }

    protected class Blink implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            cursorVisible = !cursorVisible;
            notifyCaredChanged();
        }
    }

    /**
     * Enumeration of supported cursor shapes.
     */
    public enum CursorShape {
        INSERT, OVERWRITE, MIRROR
    }

    /**
     * Method for rendering cursor into CodeArea component.
     */
    public enum CursorRenderingMode {
        /**
         * Cursor is just painted.
         */
        PAINT,
        /**
         * Cursor is painted using pixels inversion.
         */
        XOR,
        /**
         * Underlying character is painted using negative color to cursor
         * cursor.
         */
        NEGATIVE
    }

    /**
     * Interface for changes listener.
     */
    public interface CaretChangeListener {

        /**
         * Caret changed.
         */
        void notifyCaretChanged();
    }
}
