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
package com.szadowsz.nds4j.app.nodes.bin.raw.swing;


import com.szadowsz.nds4j.app.nodes.bin.raw.operation.undo.BinaryDataUndoableOperation;
import com.szadowsz.nds4j.file.bin.core.EditableBinaryData;

/**
 * Operation for deleting child block.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class RemoveDataOperation extends CodeAreaOperation {

    private final long position;
    private final int codeOffset;
    private final long length;

    public RemoveDataOperation(CodeAreaSwing codeArea, long position, int codeOffset, long length) {
        super(codeArea);
        this.position = position;
        this.codeOffset = codeOffset;
        this.length = length;
    }

    @Override
    public CodeAreaOperationType getType() {
        return CodeAreaOperationType.REMOVE_DATA;
    }

    @Override
    public void execute() {
        execute(false);
    }

    @Override
    public BinaryDataUndoableOperation executeWithUndo() {
        return execute(true);
    }

    private CodeAreaOperation execute(boolean withUndo) {
        EditableBinaryData contentData = (EditableBinaryData) codeArea.getContentData();
        CodeAreaOperation undoOperation = null;
        if (withUndo) {
            EditableBinaryData undoData = (EditableBinaryData) contentData.copy(position, length);
            undoOperation = new InsertDataOperation(codeArea, position, codeOffset, undoData);
        }
        contentData.remove(position, length);
        codeArea.setActiveCaretPosition(position, codeOffset);
        return undoOperation;
    }
}
