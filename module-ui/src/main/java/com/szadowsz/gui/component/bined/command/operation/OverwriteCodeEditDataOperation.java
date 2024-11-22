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
package com.szadowsz.gui.component.bined.command.operation;


import com.szadowsz.binary.BinaryData;
import com.szadowsz.binary.EditableBinaryData;

import com.szadowsz.gui.component.bined.CodeAreaUtils;
import com.szadowsz.gui.component.bined.settings.CodeType;
import com.szadowsz.gui.component.bined.RBinedAreaCore;
import com.szadowsz.gui.component.bined.command.BinaryDataOperation;
import com.szadowsz.gui.component.bined.command.operation.undo.BinaryDataAppendableOperation;
import com.szadowsz.gui.component.bined.command.operation.undo.BinaryDataUndoableOperation;

/**
 * Operation for editing data using overwrite mode.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class OverwriteCodeEditDataOperation extends CodeEditDataOperation {

    protected final long startPosition;
    protected final int codeOffset;
    protected final CodeType codeType;
    protected byte value;

    public OverwriteCodeEditDataOperation(RBinedAreaCore codeArea, long startPosition, int codeOffset, CodeType codeType, byte value) {
        super(codeArea);
        this.value = value;
        this.startPosition = startPosition;
        this.codeOffset = codeOffset;
        this.codeType = codeType;
    }

    @Override
    public CodeAreaOperationType getType() {
        return CodeAreaOperationType.EDIT_DATA;
    }

    @Override
    public CodeType getCodeType() {
        return codeType;
    }

    @Override
    public void execute() {
        execute(false);
    }

    @Override
    public BinaryDataUndoableOperation executeWithUndo() {
        return CodeAreaUtils.requireNonNull(execute(true));
    }

    private CodeAreaOperation execute(boolean withUndo) {
        EditableBinaryData data = (EditableBinaryData) codeArea.getContentData();
        if (startPosition > data.getDataSize() || (startPosition == data.getDataSize() && codeOffset > 0)) {
            throw new IllegalStateException("Cannot overwrite outside of the document");
        }

        CodeAreaOperation undoOperation = null;
        EditableBinaryData undoData = null;

        byte byteValue = 0;
        int removeLength = 0;
        if (codeOffset > 0) {
            undoData = (EditableBinaryData) codeArea.getContentData().copy(startPosition, 1);
            byteValue = undoData.getByte(0);
        } else {
            if (startPosition < data.getDataSize()) {
                undoData = (EditableBinaryData) data.copy(startPosition, 1);
                byteValue = undoData.getByte(0);
            } else {
                undoData = (EditableBinaryData) data.copy(startPosition, 0);
                data.insertUninitialized(startPosition, 1);
                removeLength = 1;
            }
        }

        byteValue = CodeAreaUtils.setCodeValue(byteValue, value, codeOffset, codeType);

        data.setByte(startPosition, byteValue);

        if (withUndo) {
            undoOperation = new UndoOperation(codeArea, startPosition, undoData, codeType, codeOffset, removeLength);
        }

        return undoOperation;
    }

    private static class UndoOperation extends CodeAreaOperation implements BinaryDataAppendableOperation {

        private final long position;
        private final BinaryData data;
        private final CodeType codeType;
        private int codeOffset;
        private long removeLength;

        public UndoOperation(RBinedAreaCore codeArea, long position, BinaryData data, CodeType codeType, int codeOffset, long removeLength) {
            super(codeArea);
            this.position = position;
            this.data = data;
            this.codeType = codeType;
            this.codeOffset = codeOffset;
            this.removeLength = removeLength;
        }

        @Override
        public CodeAreaOperationType getType() {
            return CodeAreaOperationType.MODIFY_DATA;
        }

        @Override
        public void execute() {
            execute(false);
        }

        @Override
        public BinaryDataUndoableOperation executeWithUndo() {
            return CodeAreaUtils.requireNonNull(execute(true));
        }

        @Override
        public boolean appendOperation(BinaryDataOperation operation) {
            if (operation instanceof UndoOperation && (((UndoOperation) operation).codeType == codeType)) {
                codeOffset++;
                if (codeOffset == codeType.getMaxDigitsForByte()) {
                    codeOffset = 0;
                    removeLength += ((UndoOperation) operation).removeLength;
                    ((EditableBinaryData) data).insert(data.getDataSize(), ((UndoOperation) operation).data);
                }
                return true;
            }

            return false;
        }

        private CodeAreaOperation execute(boolean withUndo) {
            CodeAreaOperation undoOperation = null;
            RemoveDataOperation removeOperation = null;
            EditableBinaryData contentData = (EditableBinaryData) codeArea.getContentData();
            if (removeLength > 0) {
                removeOperation = new RemoveDataOperation(codeArea, position + data.getDataSize(), 0, removeLength);
            }

            if (withUndo) {
                BinaryData undoData = contentData.copy(position, data.getDataSize());
                undoOperation = new ModifyDataOperation(codeArea, position, undoData);
            }
            contentData.replace(position, data);
            if (removeOperation != null) {
                if (withUndo) {
                    CodeAreaCompoundOperation compoundOperation = new CodeAreaCompoundOperation(codeArea);
                    compoundOperation.addOperation(removeOperation.executeWithUndo());
                    compoundOperation.addOperation(undoOperation);
                    undoOperation = compoundOperation;
                } else {
                    removeOperation.execute();
                }
            }
            return undoOperation;
        }
    }
}
