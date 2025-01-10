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
package com.szadowsz.gui.component.bined.bounds;

import com.szadowsz.gui.layout.RRect;
import com.szadowsz.gui.component.bined.settings.RCodeType;
import com.szadowsz.gui.config.text.RFontMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PGraphics;

/**
 * Binary Editor Component dimensions.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class RBinDimensions {
    private static Logger LOGGER = LoggerFactory.getLogger(RBinDimensions.class);

    // Overall dimensions, to be fed back to the Editor component
    protected final RRect componentRectangle = new RRect();
    protected final RRect displayRectangle = new RRect();
    protected float dataDisplayHeight;

    // Drawn By RBinHeader sub-component
    protected final RRect headerAreaRectangle = new RRect();

    // Drawn By RBinMain sub-component
    protected final RRect rowPositionAreaRectangle = new RRect();
    protected final RRect mainAreaRectangle = new RRect();

    // TODO naming
    protected int charactersPerRectangle;
    protected int charactersPerPage;

    // TODO naming
    protected int rowsPerPage;
    protected int rowsPerRect;

    protected int computeCharactersPerRectangle(RFontMetrics metrics) {
        int characterWidth = metrics.getCharacterWidth();
        return characterWidth == 0 ? 0 : Math.round((mainAreaRectangle.getWidth() + characterWidth - 1) / characterWidth);
    }

    protected int computeCharactersPerPage(RFontMetrics metrics) {
        int characterWidth = metrics.getCharacterWidth();
        return characterWidth == 0 ? 0 : Math.round(mainAreaRectangle.getWidth() / characterWidth);
    }

    protected int computeRowsPerRectangle(RFontMetrics metrics) {
        int rowHeight = metrics.getRowHeight();
        return rowHeight == 0 ? 0 : Math.round((mainAreaRectangle.getHeight() + rowHeight - 1) / rowHeight);
    }

    protected int computeRowsPerPage(RFontMetrics metrics) {
        int rowHeight = metrics.getRowHeight();
        return rowHeight == 0 ? 0 : Math.round(mainAreaRectangle.getHeight() / rowHeight);
    }

    public int getCharactersPerPage() {
        return charactersPerPage;
    }

    public RRect getComponentRectangle() {
        return componentRectangle;
    }
    
    public float getDataViewWidth() {
        return mainAreaRectangle.getWidth();
    }

    public float getDataViewHeight() {
        return mainAreaRectangle.getHeight();
    }

    public RRect getDataViewRectangle() {
        return mainAreaRectangle;
    }

    public RRect getDisplayRectangle() {
        return displayRectangle;
    }

    public float getHeaderAreaHeight() {
        return headerAreaRectangle.getHeight();
    }

    public RRect getHeaderAreaRectangle() {
        return headerAreaRectangle;
    }

    public RRect getMainAreaRectangle() {
        return mainAreaRectangle;
    }

    public int getRowsPerPage() {
        return rowsPerPage;
    }

    public int getRowsPerRect() {
        return rowsPerRect;
    }

    public RRect getRowPositionAreaRectangle() {
        return rowPositionAreaRectangle;
    }

    public float getRowPositionAreaWidth() {
        return rowPositionAreaRectangle.getWidth();
    }

    /**
     * Method to calculate the initial row position segment bounds
     *
     * @param metrics           Font Metrics
     * @param rowPositionLength number of expected digits for the position info
     * @param maxRowsDisplayed
     * @param rowsCount         number of expected rows
     */
    public void computeRowDimensions(RFontMetrics metrics, int rowPositionLength, long maxRowsDisplayed, long rowsCount) {
        float rowPositionWidth = metrics.getCharacterWidth() * (rowPositionLength + 1);

        float rowsToDisplay = Math.min(rowsCount, maxRowsDisplayed);
        float rowPositionHeight = metrics.getRowHeight() * (rowsCount+1);

        float headerYOffset = metrics.getFontHeight() + (float) metrics.getFontHeight() / 4;
        LOGGER.info("Editor Row Position: Length: {}, Width {}, Height {}, YOffset {}", rowPositionLength, rowPositionWidth, rowPositionHeight, headerYOffset);
        rowPositionAreaRectangle.setSize(0, headerYOffset, rowPositionWidth, rowPositionHeight);
        dataDisplayHeight = metrics.getRowHeight()*(rowsToDisplay+1);
    }

    /**
     * Method to calculate the initial row position segment bounds
     *
     * @param metrics Font Metrics
     * @param codeType
    */
    public void computeHeaderAndDataDimensions(RFontMetrics metrics, RCodeType codeType, int bytesPerRow) {
        // we have the maximum of bytes per row, so at this stage we should work out the width we ideally should have to play with
        // contentData.getDataSize()
        // structure.getBytesPerRow() vs maxBytesPerRow
        // long numRows = contentData.getDataSize() / maxBytesPerRow + (contentData.getDataSize() % maxBytesPerRow>0?1:0);

        int characterWidth = metrics.getCharacterWidth(); // Get the width of a single character
        int digitsForByte = codeType.getMaxDigitsForByte() + 1; // Get the number of characters for a byte + spacing

        float contentWidth = digitsForByte * characterWidth * bytesPerRow; // Get the ideal width of a row based on the max byte width
        float headerYOffset = metrics.getFontHeight() + (float) metrics.getFontHeight() / 4;

        LOGGER.info("Editor Data Content: Length: {}, Width {}, Height {}, XOffset {}, YOffset {}",
                bytesPerRow,
                contentWidth,
                rowPositionAreaRectangle.getHeight(),
                rowPositionAreaRectangle.getWidth(),
                headerYOffset);

        mainAreaRectangle.setSize(rowPositionAreaRectangle.getWidth(), headerYOffset, contentWidth, rowPositionAreaRectangle.getHeight());

        LOGGER.info("Editor Header Data: Width {}, Height {}, XOffset {}", mainAreaRectangle.getWidth(), headerYOffset,rowPositionAreaRectangle.getWidth());
        headerAreaRectangle.setSize(rowPositionAreaRectangle.getWidth(), 0, mainAreaRectangle.getWidth(), headerYOffset);

        componentRectangle.setSize(0,0,rowPositionAreaRectangle.getWidth() +  mainAreaRectangle.getWidth(), headerYOffset + rowPositionAreaRectangle.getHeight());
        displayRectangle.setSize(0,0, componentRectangle.getWidth(),headerYOffset+dataDisplayHeight);
    }

    /**
     * Method to calculate the other initial bounds
     *
     * @param metrics Font Metrics
     */
    public void computeOtherMetrics(RFontMetrics metrics){
        charactersPerRectangle = computeCharactersPerRectangle(metrics);
        charactersPerPage = computeCharactersPerPage(metrics);
        rowsPerRect = computeRowsPerRectangle(metrics);
        rowsPerPage = computeRowsPerPage(metrics);
    }
}
