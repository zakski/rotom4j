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
package com.szadowsz.nds4j.app.nodes.bin.raw;

import com.szadowsz.nds4j.app.nodes.bin.raw.swing.CodeAreaSwingUtils;

import java.awt.*;

/**
 * Basic code area set of colors.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class CodeAreaColorsProfile {

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

    public CodeAreaColorsProfile() {
    }

    public Color getTextColor() {
        return textColor;
    }

    public Color getTextBackground() {
        return textBackground;
    }

    public Color getSelectionColor() {
        return selectionColor;
    }

    public Color getSelectionBackground() {
        return selectionBackground;
    }

    public Color getSelectionMirrorColor() {
        return selectionMirrorColor;
    }

    public Color getSelectionMirrorBackground() {
        return selectionMirrorBackground;
    }

    public Color getAlternateColor() {
        return alternateColor;
    }

    public Color getAlternateBackground() {
        return alternateBackground;
    }

    public Color getCursorColor() {
        return cursorColor;
    }

    public Color getCursorNegativeColor() {
        return cursorNegativeColor;
    }

    public Color getDecorationLine() {
        return decorationLine;
    }

    /**
     * Returns color of the specified type.
     *
     * @param colorType color type
     * @return color or null if not defined
     */
    public Color getColor(CodeAreaColorType colorType) {
        return switch (colorType) {
            case CodeAreaColorType.TEXT_COLOR -> textColor;
            case CodeAreaColorType.TEXT_BACKGROUND -> textBackground;
            case CodeAreaColorType.SELECTION_COLOR -> selectionColor;
            case CodeAreaColorType.SELECTION_BACKGROUND -> selectionBackground;
            case CodeAreaColorType.SELECTION_MIRROR_COLOR -> selectionMirrorColor;
            case CodeAreaColorType.SELECTION_MIRROR_BACKGROUND -> selectionMirrorBackground;
            case CodeAreaColorType.ALTERNATE_COLOR -> alternateColor;
            case CodeAreaColorType.ALTERNATE_BACKGROUND -> alternateBackground;
            case CodeAreaColorType.CURSOR_COLOR -> cursorColor;
            case CodeAreaColorType.CURSOR_NEGATIVE_COLOR -> cursorNegativeColor;
            case CodeAreaColorType.LINE -> decorationLine;
            default -> null;
        };
    }

    /**
     * Returns color of the specified type.
     *
     * @param colorType     color type
     * @param basicAltColor basic color type used as fallback
     * @return color or null if not defined
     */
    public Color getColor(CodeAreaColorType colorType, CodeAreaColorType basicAltColor) {
        Color color = getColor(colorType);
        return (color == null) ? (basicAltColor == null ? null : getColor(basicAltColor)) : color;
    }

    public void reinitialize() {

    }
}
