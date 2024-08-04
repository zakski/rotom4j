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
package com.szadowsz.nds4j.app.nodes.bin.core.swing.operation;

import com.szadowsz.nds4j.app.nodes.bin.core.CodeAreaCaretPosition;
import com.szadowsz.nds4j.app.nodes.bin.core.operation.BinaryDataCompoundOperation;
import com.szadowsz.nds4j.app.nodes.bin.core.operation.BinaryDataOperation;
import com.szadowsz.nds4j.app.nodes.bin.core.operation.undo.BinaryDataUndoableOperation;
import com.szadowsz.nds4j.app.nodes.bin.core.swing.CodeAreaCore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Abstract class for compound operation on code area component.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class CodeAreaCompoundOperation extends CodeAreaOperation implements BinaryDataCompoundOperation {

    private final List<BinaryDataOperation> operations = new ArrayList<>();

    public CodeAreaCompoundOperation(CodeAreaCore codeArea) {
        super(codeArea);
    }

    public CodeAreaCompoundOperation(CodeAreaCore codeArea, CodeAreaCaretPosition backPosition) {
        super(codeArea, backPosition);
    }

    @Override
    public BinaryDataUndoableOperation executeWithUndo() {
        CodeAreaCompoundOperation undoOperations = new CodeAreaCompoundOperation(codeArea);
        for (BinaryDataOperation operation : operations) {
            BinaryDataUndoableOperation undoOperation = ((BinaryDataUndoableOperation) operation).executeWithUndo();
            undoOperations.insertOperation(0, undoOperation);
        }
        return undoOperations;
    }

    @Override
    public void execute() {
        for (BinaryDataOperation operation : operations) {
            operation.execute();
        }
    }

    @Override
    public CodeAreaOperationType getType() {
        return CodeAreaOperationType.COMPOUND;
    }

    @Override
    public void addOperation(BinaryDataOperation operation) {
        operations.add(operation);
    }

    @Override
    public void addOperations(Collection<BinaryDataOperation> operations) {
        operations.addAll(operations);
    }

    public void insertOperation(int index, BinaryDataOperation operation) {
        operations.add(index, operation);
    }

    @Override
    public List<BinaryDataOperation> getOperations() {
        return operations;
    }

    @Override
    public boolean isEmpty() {
        return operations.isEmpty();
    }

}
