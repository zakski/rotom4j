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
import com.szadowsz.nds4j.file.bin.core.BinaryData;
import com.szadowsz.nds4j.file.bin.core.EditableBinaryData;

/**
 * Operation for inserting data.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class InsertDataOperation extends CodeAreaOperation {

    private final long position;
    private final int codeOffset;
    private final BinaryData data;

    public InsertDataOperation(CodeAreaSwing codeArea, long position, int codeOffset, BinaryData data) {
        super(codeArea);
        this.position = position;
        this.codeOffset = codeOffset;
        this.data = data;
    }

    @Override
    public CodeAreaOperationType getType() {
        return CodeAreaOperationType.INSERT_DATA;
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
        CodeAreaOperation undoOperation = null;
        EditableBinaryData contentData = (EditableBinaryData) codeArea.getContentData();
        contentData.insert(position, data);
        if (withUndo) {
            undoOperation = new RemoveDataOperation(codeArea, position, codeOffset, data.getDataSize());
        }
        codeArea.setActiveCaretPosition(position + data.getDataSize(), codeOffset);
        return undoOperation;
    }

    public void appendData(BinaryData appendData) {
        ((EditableBinaryData) data).insert(data.getDataSize(), appendData);
    }

      public BinaryData getData() {
        return data;
    }

    @Override
    public void dispose() {
        super.dispose();
        data.dispose();
    }
}
