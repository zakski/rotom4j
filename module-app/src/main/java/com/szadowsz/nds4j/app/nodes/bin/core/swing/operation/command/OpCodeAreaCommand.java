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
package com.szadowsz.nds4j.app.nodes.bin.core.swing.operation.command;

import com.szadowsz.nds4j.app.nodes.bin.core.CodeAreaUtils;
import com.szadowsz.nds4j.app.nodes.bin.core.operation.BinaryDataCommandPhase;
import com.szadowsz.nds4j.app.nodes.bin.core.operation.undo.BinaryDataUndoableOperation;
import com.szadowsz.nds4j.app.nodes.bin.core.swing.CodeAreaCore;
import com.szadowsz.nds4j.app.nodes.bin.core.swing.operation.CodeAreaOperation;

/**
 * Abstract class for operation on hexadecimal document.
 *
 * @author ExBin Project (https://exbin.org)
 */
public abstract class OpCodeAreaCommand extends CodeAreaCommand {

    protected BinaryDataUndoableOperation operation;
    protected BinaryDataCommandPhase phase = BinaryDataCommandPhase.CREATED;

    public OpCodeAreaCommand(CodeAreaCore codeArea) {
        super(codeArea);
    }

    public void setOperation(CodeAreaOperation operation) {
        if (this.operation != null) {
            this.operation.dispose();
        }
        this.operation = operation;
    }

    @Override
    public void execute() {
        if (phase != BinaryDataCommandPhase.CREATED) {
            throw new IllegalStateException();
        }

        executeInt();
    }

    @Override
    public void undo() {
        if (phase == BinaryDataCommandPhase.REVERTED) {
            throw new IllegalStateException();
        }

        BinaryDataUndoableOperation redoOperation = CodeAreaUtils.requireNonNull(operation).executeWithUndo();
        operation.dispose();
        operation = redoOperation;
        phase = BinaryDataCommandPhase.REVERTED;
    }

    @Override
    public void redo() {
        if (phase == BinaryDataCommandPhase.EXECUTED) {
            throw new IllegalStateException();
        }

        executeInt();
    }

    private void executeInt() {
        BinaryDataUndoableOperation undoOperation = CodeAreaUtils.requireNonNull(operation).executeWithUndo();
        operation.dispose();
        operation = undoOperation;
        phase = BinaryDataCommandPhase.EXECUTED;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (operation != null) {
            operation.dispose();
        }
    }
}
