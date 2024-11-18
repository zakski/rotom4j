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
package com.szadowsz.gui.component.binary.operation.swing.command;

import com.szadowsz.gui.component.binary.CodeAreaUtils;
import com.szadowsz.gui.component.binary.RCodeAreaCore;
import com.szadowsz.gui.component.binary.capability.CodeTypeCapable;
import com.szadowsz.gui.component.binary.operation.BinaryDataCommand;
import com.szadowsz.gui.component.binary.operation.BinaryDataCommandPhase;
import com.szadowsz.gui.component.binary.operation.swing.DeleteCodeEditDataOperation;
import com.szadowsz.gui.component.binary.operation.swing.InsertCodeEditDataOperation;
import com.szadowsz.gui.component.binary.operation.swing.OverwriteCodeEditDataOperation;
import com.szadowsz.gui.component.binary.operation.undo.BinaryDataAppendableCommand;
import com.szadowsz.gui.component.binary.operation.undo.BinaryDataAppendableOperation;
import com.szadowsz.gui.component.binary.operation.undo.BinaryDataUndoableOperation;

/**
 * Command for editing data in code section.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class EditCodeDataCommand extends EditDataCommand implements BinaryDataAppendableCommand {

    protected final EditOperationType editOperationType;
    protected BinaryDataCommandPhase phase = BinaryDataCommandPhase.CREATED;
    protected BinaryDataUndoableOperation activeOperation;

    public EditCodeDataCommand(RCodeAreaCore codeArea, EditOperationType editOperationType, long position, int positionCodeOffset, byte value) {
        super(codeArea);
        this.editOperationType = editOperationType;
        switch (editOperationType) {
            case INSERT: {
                activeOperation = new InsertCodeEditDataOperation(codeArea, position, positionCodeOffset, value);
                break;
            }
            case OVERWRITE: {
                activeOperation = new OverwriteCodeEditDataOperation(codeArea, position, positionCodeOffset, ((CodeTypeCapable) codeArea).getCodeType(), value);
                break;
            }
            case DELETE: {
                activeOperation = new DeleteCodeEditDataOperation(codeArea, position, ((CodeTypeCapable) codeArea).getCodeType(), value);
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(editOperationType);
        }
    }

    @Override
    public void execute() {
        if (phase != BinaryDataCommandPhase.CREATED) {
            throw new IllegalStateException();
        }

        BinaryDataUndoableOperation undoOperation = activeOperation.executeWithUndo();
        activeOperation.dispose();
        activeOperation = undoOperation;
        phase = BinaryDataCommandPhase.EXECUTED;
    }

    @Override
    public void undo() {
        if (phase != BinaryDataCommandPhase.EXECUTED) {
            throw new IllegalStateException();
        }

        BinaryDataUndoableOperation undoOperation = activeOperation.executeWithUndo();
        activeOperation.dispose();
        activeOperation = undoOperation;
        phase = BinaryDataCommandPhase.REVERTED;
    }

    @Override
    public void redo() {
        if (phase != BinaryDataCommandPhase.REVERTED) {
            throw new IllegalStateException();
        }

        BinaryDataUndoableOperation undoOperation = activeOperation.executeWithUndo();
        activeOperation.dispose();
        activeOperation = undoOperation;
        phase = BinaryDataCommandPhase.EXECUTED;
    }

    @Override
    public CodeAreaCommandType getType() {
        return CodeAreaCommandType.DATA_EDITED;
    }

    @Override
    public boolean appendExecute(BinaryDataCommand command) {
        if (phase != BinaryDataCommandPhase.EXECUTED) {
            throw new IllegalStateException();
        }

        command.execute();

        if (command instanceof EditCodeDataCommand && activeOperation instanceof BinaryDataAppendableOperation) {
            return ((BinaryDataAppendableOperation) activeOperation).appendOperation(((EditCodeDataCommand) command).activeOperation);
        }

        return false;
    }

    @Override
    public EditOperationType getEditOperationType() {
        return editOperationType;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (activeOperation != null) {
            activeOperation.dispose();
        }
    }
}
