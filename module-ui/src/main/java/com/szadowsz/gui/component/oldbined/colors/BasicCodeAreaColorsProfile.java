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
package com.szadowsz.gui.component.oldbined.colors;

import com.szadowsz.gui.component.oldbined.CodeAreaUtils;
import com.szadowsz.gui.component.oldbined.swing.CodeAreaSwingUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Basic code area set of colors.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class BasicCodeAreaColorsProfile implements CodeAreaColorsProfile {

    protected Color textColor;
    protected Color textBackground;
    protected Color selectionColor;
    protected Color selectionBackground;
    protected Color selectionMirrorColor;
    protected Color selectionMirrorBackground;
    protected Color alternateColor;
    protected Color alternateBackground;
    protected Color cursorColor;
    protected Color cursorNegativeColor;
    protected Color decorationLine;

    public BasicCodeAreaColorsProfile() {
    }

    public Color getTextColor() {
        return CodeAreaUtils.requireNonNull(textColor);
    }

    public Color getTextBackground() {
        return CodeAreaUtils.requireNonNull(textBackground);
    }

    public Color getSelectionColor() {
        return CodeAreaUtils.requireNonNull(selectionColor);
    }

    public Color getSelectionBackground() {
        return CodeAreaUtils.requireNonNull(selectionBackground);
    }

    public Color getSelectionMirrorColor() {
        return CodeAreaUtils.requireNonNull(selectionMirrorColor);
    }

    public Color getSelectionMirrorBackground() {
        return CodeAreaUtils.requireNonNull(selectionMirrorBackground);
    }

    public Color getAlternateColor() {
        return CodeAreaUtils.requireNonNull(alternateColor);
    }

    public Color getAlternateBackground() {
        return CodeAreaUtils.requireNonNull(alternateBackground);
    }

    public Color getCursorColor() {
        return CodeAreaUtils.requireNonNull(cursorColor);
    }

    public Color getCursorNegativeColor() {
        return CodeAreaUtils.requireNonNull(cursorNegativeColor);
    }

    public Color getDecorationLine() {
        return CodeAreaUtils.requireNonNull(decorationLine);
    }

    @Override
    public Color getColor(CodeAreaColorType colorType) {
        if (colorType == CodeAreaBasicColors.TEXT_COLOR) {
            return textColor;
        }
        if (colorType == CodeAreaBasicColors.TEXT_BACKGROUND) {
            return textBackground;
        }
        if (colorType == CodeAreaBasicColors.SELECTION_COLOR) {
            return selectionColor;
        }
        if (colorType == CodeAreaBasicColors.SELECTION_BACKGROUND) {
            return selectionBackground;
        }
        if (colorType == CodeAreaBasicColors.SELECTION_MIRROR_COLOR) {
            return selectionMirrorColor;
        }
        if (colorType == CodeAreaBasicColors.SELECTION_MIRROR_BACKGROUND) {
            return selectionMirrorBackground;
        }
        if (colorType == CodeAreaBasicColors.ALTERNATE_COLOR) {
            return alternateColor;
        }
        if (colorType == CodeAreaBasicColors.ALTERNATE_BACKGROUND) {
            return alternateBackground;
        }
        if (colorType == CodeAreaBasicColors.CURSOR_COLOR) {
            return cursorColor;
        }
        if (colorType == CodeAreaBasicColors.CURSOR_NEGATIVE_COLOR) {
            return cursorNegativeColor;
        }
        if (colorType == BasicCodeAreaDecorationColorType.LINE) {
            return decorationLine;
        }

        return null;
    }

    @Override
    public Color getColor(CodeAreaColorType colorType, CodeAreaBasicColors basicAltColor) {
        Color color = getColor(colorType);
        return (color == null) ? (basicAltColor == null ? null : getColor(basicAltColor)) : color;
    }

    @Override
    public void reinitialize() {
        textColor = UIManager.getColor("TextArea.foreground");
        if (textColor == null) {
            textColor = Color.BLACK;
        }

        textBackground = UIManager.getColor("TextArea.background");
        if (textBackground == null) {
            textBackground = Color.WHITE;
        }
        selectionColor = UIManager.getColor("TextArea.selectionForeground");
        if (selectionColor == null) {
            selectionColor = Color.WHITE;
        }
        selectionBackground = UIManager.getColor("TextArea.selectionBackground");
        if (selectionBackground == null) {
            selectionBackground = new Color(96, 96, 255);
        }
        selectionMirrorColor = selectionColor;
        selectionMirrorBackground = CodeAreaSwingUtils.computeGrayColor(selectionBackground);
        cursorColor = UIManager.getColor("TextArea.caretForeground");
        if (cursorColor == null) {
            cursorColor = Color.BLACK;
        }
        cursorNegativeColor = CodeAreaSwingUtils.createNegativeColor(cursorColor);
        decorationLine = Color.GRAY;

        alternateColor = textColor;
        alternateBackground = CodeAreaSwingUtils.createOddColor(textBackground);
    }
}
