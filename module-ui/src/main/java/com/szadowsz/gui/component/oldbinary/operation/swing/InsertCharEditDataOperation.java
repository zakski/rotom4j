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

import java.nio.charset.Charset;
import com.szadowsz.gui.component.oldbinary.CodeAreaUtils;
import com.szadowsz.gui.component.oldbinary.RCodeAreaCore;
import com.szadowsz.binary.EditableBinaryData;
import com.szadowsz.gui.component.oldbinary.capability.CaretCapable;
import com.szadowsz.gui.component.oldbinary.capability.CharsetCapable;
import com.szadowsz.gui.component.oldbinary.capability.SelectionCapable;
import com.szadowsz.gui.component.oldbinary.operation.BinaryDataOperation;
import com.szadowsz.gui.component.oldbinary.operation.undo.BinaryDataAppendableOperation;
import com.szadowsz.gui.component.oldbinary.operation.undo.BinaryDataUndoableOperation;

/**
 * Operation for editing data using insert mode.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class InsertCharEditDataOperation extends CharEditDataOperation {

    protected final long startPosition;
    protected final char value;

    public InsertCharEditDataOperation(RCodeAreaCore coreArea, long startPosition, char value) {
        super(coreArea);
        this.value = value;
        this.startPosition = startPosition;
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
        EditableBinaryData data = (EditableBinaryData) codeArea.getContentData();
        Charset charset = ((CharsetCapable) codeArea).getCharset();
        byte[] bytes = CodeAreaUtils.characterToBytes(value, charset);
        data.insert(startPosition, bytes);
        long length = bytes.length;
        long dataPosition = startPosition + length;
        ((CaretCapable) codeArea).setActiveCaretPosition(dataPosition);
        ((SelectionCapable) codeArea).setSelection(dataPosition, dataPosition);

        if (withUndo) {
            undoOperation = new UndoOperation(codeArea, startPosition, length);
        }

        return undoOperation;
    }

    public long getStartPosition() {
        return startPosition;
    }

    /**
     * Appendable variant of RemoveDataOperation.
     */
    private static class UndoOperation extends CodeAreaOperation implements BinaryDataAppendableOperation {

        private final long position;
        private long length;

        public UndoOperation(RCodeAreaCore codeArea, long position, long length) {
            super(codeArea);
            this.position = position;
            this.length = length;
        }

        @Override
        public CodeAreaOperationType getType() {
            return CodeAreaOperationType.REMOVE_DATA;
        }

        @Override
        public boolean appendOperation(BinaryDataOperation operation) {
            if (operation instanceof UndoOperation) {
                length += ((UndoOperation) operation).length;
                return true;
            }

            return false;
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
            EditableBinaryData contentData = (EditableBinaryData) codeArea.getContentData();
            CodeAreaOperation undoOperation = null;
            if (withUndo) {
                EditableBinaryData undoData = (EditableBinaryData) contentData.copy(position, length);
                undoOperation = new InsertDataOperation(codeArea, position, 0, undoData);
            }
            contentData.remove(position, length);
            ((CaretCapable) codeArea).setActiveCaretPosition(position, 0);
            return undoOperation;
        }
    }
}
