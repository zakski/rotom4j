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

import com.szadowsz.gui.component.bined.CodeAreaCaretPosition;
import com.szadowsz.gui.component.bined.RBinedAreaCore;
import com.szadowsz.gui.component.bined.command.BinaryDataCompoundOperation;
import com.szadowsz.gui.component.bined.command.BinaryDataOperation;
import com.szadowsz.gui.component.bined.command.operation.undo.BinaryDataUndoableOperation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Abstract class for compound operation on code area component.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class CodeAreaCompoundOperation extends CodeAreaOperation implements BinaryDataCompoundOperation {

    protected final List<BinaryDataOperation> operations = new ArrayList<>();

    public CodeAreaCompoundOperation(RBinedAreaCore codeArea) {
        super(codeArea);
    }

    public CodeAreaCompoundOperation(RBinedAreaCore codeArea, CodeAreaCaretPosition backPosition) {
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
