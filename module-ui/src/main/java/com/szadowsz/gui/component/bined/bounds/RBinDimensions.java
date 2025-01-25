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

import com.szadowsz.gui.config.RLayoutStore;
import com.szadowsz.gui.layout.RRect;
import com.szadowsz.gui.component.bined.settings.RCodeType;
import com.szadowsz.gui.config.text.RFontMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Binary Editor Component dimensions.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class RBinDimensions {
    private static Logger LOGGER = LoggerFactory.getLogger(RBinDimensions.class);

    // Overall actual dimensions we would want
    protected final RRect componentDims = new RRect();

    // What we have space-wise to display in
    protected final RRect componentDisplayDims = new RRect();

    // Drawn By RBinHeader sub-component
    protected final RRect headerDims = new RRect(); // actual size of the header

    // Drawn By RBinMain sub-component
    protected final RRect rowPositionDims = new RRect(); // actual size of the row position info
    protected final RRect rowPositionDisplayDims = new RRect(); // display size of the row position info

    // Drawn By RBinMain sub-component
    protected final RRect contentDims = new RRect(); // actual size of the data area
    protected final RRect contentDisplayDims = new RRect(); // display size of the data area

    // Drawn By RBinEditor
    protected final RRect vScrollDims = new RRect(); // actual size of the vertical scrollbar


    protected long displayRows;
    protected long totalRows;

    protected int charactersPerRow;

    protected int computeCharactersPerRow(RFontMetrics metrics) {
        int characterWidth = metrics.getCharacterWidth();
        return characterWidth == 0 ? 0 : Math.round(contentDims.getWidth() / characterWidth);
    }

    public int getCharactersPerRow() {
        return charactersPerRow;
    }

    public RRect getComponentDims() {
        return componentDims;
    }

    public RRect getComponentDisplayDims() {
        return componentDisplayDims;
    }

    public float getContentWidth() {
        return contentDims.getWidth();
    }

    public float getContentHeight() {
        return contentDims.getHeight();
    }

    public float getContentDisplayHeight() {
        return contentDisplayDims.getHeight();
    }

    public RRect getContentDims() {
        return contentDims;
    }

    public float getHeaderHeight() {
        return headerDims.getHeight();
    }

    public RRect getHeaderDims() {
        return headerDims;
    }

    public float getRowPositionWidth() {
        return rowPositionDims.getWidth();
    }

    public RRect getRowPositionDims() {
        return rowPositionDims;
    }

    public long getTotalRows() {
        return totalRows;
    }

    public RRect getVerticalScrollbarDims() {
        return vScrollDims;
    }

    /**
     * Method to calculate the initial row position segment bounds
     *
     * @param metrics           Font Metrics
     * @param rowPositionChars  number of expected digits for the position info
     * @param rowsCount         number of rows in the data
     * @param maxRowsDisplayed  max number of rows to display at one time
     */
    public void computeRowDimensions(RFontMetrics metrics, int rowPositionChars, long rowsCount, long maxRowsDisplayed) {
        float rowPositionWidth = metrics.getCharacterWidth() * (rowPositionChars + 1); // get the width of the row position info

        displayRows = Math.min(rowsCount, maxRowsDisplayed); // get the displayable row count for the height
        totalRows = rowsCount;

        float rowPositionHeight = metrics.getRowHeight() * (rowsCount+1); // calculate the total height of the row position info
        float rowPositionDisplayHeight = metrics.getRowHeight() * (displayRows +1); // calculate the display height of the row position info

        float headerYOffset = metrics.getFontHeight() + (float) metrics.getFontHeight() / 4; // account for the header positioning // TODO Unify Offset calc

        LOGGER.info("Editor Row Position Dims: Chars: {}, Pos [{}, {}], Size [{}, {}]",
                rowPositionChars,
                0,
                headerYOffset,
                rowPositionWidth,
                rowPositionDisplayHeight
        );
        rowPositionDisplayDims.setSize(0, headerYOffset, rowPositionWidth, rowPositionDisplayHeight);
        rowPositionDims.setSize(0, headerYOffset, rowPositionWidth, rowPositionHeight);
    }

    /**
     * Method to calculate the initial row position segment bounds
     *
     * @param metrics Font Metrics
     * @param codeType the Base of the byte values displayed
     * @param bytesPerRow the max number of bytes per row.
    */
    public void computeHeaderAndDataDimensions(RFontMetrics metrics, RCodeType codeType, int bytesPerRow) {
        // we have the maximum of bytes per row, so at this stage we should work out the width we ideally should have to play with
        // contentData.getDataSize()
        // structure.getBytesPerRow() vs maxBytesPerRow
        // long numRows = contentData.getDataSize() / maxBytesPerRow + (contentData.getDataSize() % maxBytesPerRow>0?1:0);

        int characterWidth = metrics.getCharacterWidth(); // Get the width of a single character
        int digitsForByte = codeType.getMaxDigitsForByte() + 1; // Get the number of characters for a byte + spacing

        float contentWidth = digitsForByte * characterWidth * bytesPerRow; // Get the ideal width of a row based on the max byte width
        float headerYOffset = metrics.getFontHeight() + (float) metrics.getFontHeight() / 4; // account for the header positioning // TODO Unify OFfset calc

        // Set the Dimensions of the area we display the data.
        contentDisplayDims.setSize(rowPositionDisplayDims.getWidth(), headerYOffset, contentWidth, rowPositionDisplayDims.getHeight());
        contentDims.setSize(rowPositionDisplayDims.getWidth(), headerYOffset, contentWidth, rowPositionDims.getHeight());
        LOGGER.info("Editor Content Dims: BytesPerRow: {}, Pos [{}, {}], Size [{}, {}]",
                bytesPerRow,
                contentDisplayDims.getX(),
                contentDisplayDims.getY(),
                contentDisplayDims.getWidth(),
                contentDisplayDims.getHeight()
        );

        // Set the Dimensions of the area we display the header.
        headerDims.setSize(rowPositionDims.getWidth(), 0, contentWidth, headerYOffset);
        LOGGER.info("Editor Header Dims: BytesPerRow: {}, Pos [{}, {}], Size [{}, {}]",
                bytesPerRow,
                rowPositionDims.getWidth(),0,
                headerDims.getWidth(),
                headerDims.getHeight());

        // Set the Dimensions of the area we display the vertical scrollbar.
        if (displayRows < totalRows){
            vScrollDims.setSize(rowPositionDims.getWidth()+contentWidth,headerYOffset, RLayoutStore.getCell(), contentDisplayDims.getHeight());
            LOGGER.info("Editor VScroll Data: Pos [{}, {}], Size [{}, {}]",
                    rowPositionDims.getWidth(),0,
                    headerDims.getWidth(),
                    headerDims.getHeight());
        }

        componentDisplayDims.setSize(
                0,
                0,
                rowPositionDims.getWidth() + contentDisplayDims.getWidth() + vScrollDims.getWidth(),
                headerDims.getHeight() + contentDisplayDims.getHeight()
        );

        componentDims.setSize(
                0,
                0,
                rowPositionDims.getWidth() + contentDisplayDims.getWidth() + vScrollDims.getWidth(),
                headerDims.getHeight() + rowPositionDims.getHeight()
        );


        charactersPerRow = computeCharactersPerRow(metrics);
    }
}
