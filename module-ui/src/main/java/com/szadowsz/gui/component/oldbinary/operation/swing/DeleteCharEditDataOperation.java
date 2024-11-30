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
package com.szadowsz.gui.component.oldbinary.operation.swing;

import com.szadowsz.gui.component.oldbinary.RCodeAreaCore;
import com.szadowsz.binary.BinaryData;
import com.szadowsz.binary.EditableBinaryData;
import com.szadowsz.gui.component.oldbinary.capability.CaretCapable;
import com.szadowsz.gui.component.oldbinary.CodeAreaUtils;
import com.szadowsz.gui.component.oldbinary.capability.SelectionCapable;
import com.szadowsz.gui.component.oldbinary.operation.BinaryDataOperation;
import com.szadowsz.gui.component.oldbinary.operation.undo.BinaryDataAppendableOperation;
import com.szadowsz.gui.component.oldbinary.operation.undo.BinaryDataUndoableOperation;

/**
 * Operation for editing data in delete mode.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class DeleteCharEditDataOperation extends CharEditDataOperation {

    private static final char BACKSPACE_CHAR = '\b';
    private static final char DELETE_CHAR = (char) 0x7f;

    protected long position;
    protected char value;

    public DeleteCharEditDataOperation(RCodeAreaCore codeArea, long startPosition, char value) {
        super(codeArea);
        this.value = value;
        this.position = startPosition;
    }

     @Override
    public CodeAreaOperationType getType() {
        return CodeAreaOperationType.EDIT_DATA;
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
        CodeAreaOperation undoOperation = null;
        EditableBinaryData undoData = null;

        EditableBinaryData data = (EditableBinaryData) codeArea.getContentData();
        switch (value) {
            case BACKSPACE_CHAR: {
                if (position <= 0) {
                    throw new IllegalStateException("Cannot apply backspace on position " + position);
                }

                position--;
                undoData = (EditableBinaryData) data.copy(position, 1);
                data.remove(position, 1);
                break;
            }
            case DELETE_CHAR: {
                if (position >= data.getDataSize()) {
                    throw new IllegalStateException("Cannot apply delete on position " + position);
                }

                undoData = (EditableBinaryData) data.copy(position, 1);
                data.remove(position, 1);
                break;
            }
            default: {
                throw new IllegalStateException("Unexpected character " + value);
            }
        }
        ((CaretCapable) codeArea).setActiveCaretPosition(position);
        ((SelectionCapable) codeArea).setSelection(position, position);
        codeArea.repaint();

        if (withUndo) {
            undoOperation = new UndoOperation(codeArea, position, 0, undoData, value);
        }
        return undoOperation;
    }

      private static class UndoOperation extends InsertDataOperation implements BinaryDataAppendableOperation {

        private char value;

        public UndoOperation(RCodeAreaCore codeArea, long position, int codeOffset, BinaryData data, char value) {
            super(codeArea, position, codeOffset, data);
            this.value = value;
        }

        @Override
        public boolean appendOperation(BinaryDataOperation operation) {
            if (operation instanceof UndoOperation && ((UndoOperation) operation).value == value) {
                EditableBinaryData editableData = (EditableBinaryData) data;
                switch (value) {
                    case BACKSPACE_CHAR: {
                        editableData.insert(0, ((UndoOperation) operation).getData());
                        break;
                    }
                    case DELETE_CHAR: {
                        editableData.insert(editableData.getDataSize(), ((UndoOperation) operation).getData());
                        break;
                    }
                    default: {
                        throw new IllegalStateException("Unexpected character " + value);
                    }
                }
                return true;
            }

            return false;
        }
    }
}
