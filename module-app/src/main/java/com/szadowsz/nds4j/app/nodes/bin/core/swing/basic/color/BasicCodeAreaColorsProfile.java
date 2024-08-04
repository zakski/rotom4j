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
package com.szadowsz.nds4j.app.nodes.bin.core.swing.basic.color;

import java.awt.Color;
import javax.swing.UIManager;

import com.szadowsz.nds4j.app.nodes.bin.core.CodeAreaUtils;
import com.szadowsz.nds4j.app.nodes.bin.core.color.BasicCodeAreaDecorationColorType;
import com.szadowsz.nds4j.app.nodes.bin.core.color.CodeAreaBasicColors;
import com.szadowsz.nds4j.app.nodes.bin.core.color.CodeAreaColorType;
import com.szadowsz.nds4j.app.nodes.bin.core.swing.CodeAreaSwingUtils;

/**
 * Basic code area set of colors.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class BasicCodeAreaColorsProfile implements CodeAreaColorsProfile {

    private Color textColor;
    private Color textBackground;
    private Color selectionColor;
    private Color selectionBackground;
    private Color selectionMirrorColor;
    private Color selectionMirrorBackground;
    private Color alternateColor;
    private Color alternateBackground;
    private Color cursorColor;
    private Color cursorNegativeColor;
    private Color decorationLine;

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
        return switch (colorType) {
            case CodeAreaBasicColors.TEXT_COLOR -> textColor;
            case CodeAreaBasicColors.TEXT_BACKGROUND -> textBackground;
            case CodeAreaBasicColors.SELECTION_COLOR -> selectionColor;
            case CodeAreaBasicColors.SELECTION_BACKGROUND -> selectionBackground;
            case CodeAreaBasicColors.SELECTION_MIRROR_COLOR -> selectionMirrorColor;
            case CodeAreaBasicColors.SELECTION_MIRROR_BACKGROUND -> selectionMirrorBackground;
            case CodeAreaBasicColors.ALTERNATE_COLOR -> alternateColor;
            case CodeAreaBasicColors.ALTERNATE_BACKGROUND -> alternateBackground;
            case CodeAreaBasicColors.CURSOR_COLOR -> cursorColor;
            case CodeAreaBasicColors.CURSOR_NEGATIVE_COLOR -> cursorNegativeColor;
            case BasicCodeAreaDecorationColorType.LINE -> decorationLine;
            default -> null;
        };
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
