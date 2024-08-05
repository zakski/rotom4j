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

import com.szadowsz.nds4j.app.nodes.bin.core.CharsetStreamTranslator;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * Basic code area component dimensions.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class CodeAreaMetrics {

    protected int rowHeight;
    protected int characterWidth;
    protected int fontHeight;
    protected int maxBytesPerChar;
    protected int subFontSpace = 0;

    public boolean isInitialized() {
        return rowHeight != 0 && characterWidth != 0;
    }

    public int getRowHeight() {
        return rowHeight;
    }

    public int getCharacterWidth() {
        return characterWidth;
    }

    public int getFontHeight() {
        return fontHeight;
    }

    public int getSubFontSpace() {
        return subFontSpace;
    }

    public int getMaxBytesPerChar() {
        return maxBytesPerChar;
    }
}
