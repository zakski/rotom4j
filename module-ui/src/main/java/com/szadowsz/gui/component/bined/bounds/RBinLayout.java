package com.szadowsz.gui.component.bined.bounds;

import com.szadowsz.gui.component.bined.settings.CodeType;

public class RBinLayout {

    public int computeFirstCodeCharacterPos(RBinStructure structure, int byteOffset) {
        CodeType codeType = structure.getCodeType();
        return byteOffset * (codeType.getMaxDigitsForByte() + 1);
    }
}
