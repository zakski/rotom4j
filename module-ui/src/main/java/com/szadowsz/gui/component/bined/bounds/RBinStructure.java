package com.szadowsz.gui.component.bined.bounds;

import com.szadowsz.gui.component.bined.settings.CodeType;

public class RBinStructure {

    private final RBinLayout layout = new RBinLayout();

    private CodeType codeType = CodeType.HEXADECIMAL;

    private int bytesPerRow;

    public int getBytesPerRow() {
        return bytesPerRow;
    }

    public CodeType getCodeType() {
        return codeType;
    }

    public int computeFirstCodeCharacterPos(int byteOffset) {
        return layout.computeFirstCodeCharacterPos(this, byteOffset);
    }
}
