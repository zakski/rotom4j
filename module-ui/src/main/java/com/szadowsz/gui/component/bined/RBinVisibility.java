package com.szadowsz.gui.component.bined;

import com.szadowsz.gui.component.bined.bounds.RBinDimensions;
import com.szadowsz.gui.component.bined.sizing.RBinMetrics;
import com.szadowsz.gui.component.bined.settings.CodeAreaViewMode;
import com.szadowsz.gui.component.bined.sizing.RBinStructure;

public class RBinVisibility {

    protected int previewCharPos;
    protected int previewRelativeX;

    protected int splitLinePos;

    protected int skipToCode;
    protected int skipToChar;
    protected int skipToPreview;
    protected int skipRestFromCode;
    protected int skipRestFromChar;
    protected int skipRestFromPreview;

    protected boolean codeSectionVisible;
    protected boolean previewSectionVisible;

    protected int charactersPerCodeSection;
    protected int codeLastCharPos;

    public int getCharactersPerCodeSection() {
        return charactersPerCodeSection;
    }

    public int getPreviewCharPos() {
        return previewCharPos;
    }

    public int getPreviewRelativeX() {
        return previewRelativeX;
    }


    public int getSkipRestFromChar() {
        return skipRestFromChar;
    }

    public int getSkipRestFromCode() {
        return skipRestFromCode;
    }

    public int getSkipToChar() {
        return skipToChar;
    }

    public int getSkipToCode() {
        return skipToCode;
    }

    public int getSkipToPreview() {
        return skipToPreview;
    }

    public int getSkipRestFromPreview() {
        return 0;
    }

    public void recomputeCharPositions(RBinMetrics metrics, RBinStructure structure, RBinDimensions dimensions){//, RBinScrolling scrolling) {
        int bytesPerRow = structure.getBytesPerRow();
        int characterWidth = metrics.getCharacterWidth();
        int charsPerByte = structure.getCodeType().getMaxDigitsForByte() + 1;

        CodeAreaViewMode viewMode = structure.getViewMode();

        int invisibleFromLeftX = 0;//scrolling.getHorizontalScrollX(characterWidth);
        int invisibleFromRightX = Math.round(invisibleFromLeftX + dimensions.getDataViewWidth());

        charactersPerCodeSection = structure.computeFirstCodeCharacterPos(bytesPerRow);

        // Compute first and last visible character of the code area
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            codeLastCharPos = bytesPerRow * charsPerByte - 1;
        } else {
            codeLastCharPos = 0;
        }

        if (viewMode == CodeAreaViewMode.DUAL) {
            previewCharPos = bytesPerRow * charsPerByte;
        } else {
            previewCharPos = 0;
        }
        previewRelativeX = previewCharPos * characterWidth;

        skipToCode = 0;
        skipToChar = 0;
        skipToPreview = 0;
        skipRestFromCode = -1;
        skipRestFromChar = -1;
        skipRestFromPreview = -1;
        codeSectionVisible = viewMode != CodeAreaViewMode.TEXT_PREVIEW;
        previewSectionVisible = viewMode != CodeAreaViewMode.CODE_MATRIX;

        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            skipToChar = invisibleFromLeftX / characterWidth;
            if (skipToChar < 0) {
                skipToChar = 0;
            }
            skipRestFromChar = (invisibleFromRightX + characterWidth - 1) / characterWidth;
            if (skipRestFromChar > structure.getCharactersPerRow()) {
                skipRestFromChar = structure.getCharactersPerRow();
            }
            skipToCode = structure.computePositionByte(skipToChar);
            skipRestFromCode = structure.computePositionByte(skipRestFromChar - 1) + 1;
            if (skipRestFromCode > bytesPerRow) {
                skipRestFromCode = bytesPerRow;
            }
        }

        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.TEXT_PREVIEW) {
            skipToPreview = invisibleFromLeftX / characterWidth - previewCharPos;
            if (skipToPreview < 0) {
                skipToPreview = 0;
            }
            if (skipToPreview > 0) {
                skipToChar = skipToPreview + previewCharPos;
            }
            skipRestFromPreview = (invisibleFromRightX + characterWidth - 1) / characterWidth - previewCharPos;
            if (skipRestFromPreview > bytesPerRow) {
                skipRestFromPreview = bytesPerRow;
            }
            if (skipRestFromPreview >= 0) {
                skipRestFromChar = skipRestFromPreview + previewCharPos;
            }
        }
    }

}
