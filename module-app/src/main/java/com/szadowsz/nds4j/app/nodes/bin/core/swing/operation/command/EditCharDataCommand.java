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
import com.szadowsz.nds4j.app.nodes.bin.core.operation.BinaryDataCommand;
import com.szadowsz.nds4j.app.nodes.bin.core.operation.BinaryDataCommandPhase;
import com.szadowsz.nds4j.app.nodes.bin.core.operation.undo.BinaryDataAppendableCommand;
import com.szadowsz.nds4j.app.nodes.bin.core.operation.undo.BinaryDataAppendableOperation;
import com.szadowsz.nds4j.app.nodes.bin.core.operation.undo.BinaryDataUndoableOperation;
import com.szadowsz.nds4j.app.nodes.bin.core.swing.CodeAreaCore;
import com.szadowsz.nds4j.app.nodes.bin.core.swing.operation.DeleteCharEditDataOperation;
import com.szadowsz.nds4j.app.nodes.bin.core.swing.operation.InsertCharEditDataOperation;
import com.szadowsz.nds4j.app.nodes.bin.core.swing.operation.OverwriteCharEditDataOperation;

/**
 * Command for editing data in text mode.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class EditCharDataCommand extends EditDataCommand implements BinaryDataAppendableCommand {

   private final EditCommandType commandType;
    protected BinaryDataCommandPhase phase = BinaryDataCommandPhase.CREATED;
    private BinaryDataUndoableOperation activeOperation;

    public EditCharDataCommand(CodeAreaCore codeArea, EditCommandType commandType, long position, char charData) {
        super(codeArea);
        this.commandType = commandType;
        switch (commandType) {
            case INSERT: {
                activeOperation = new InsertCharEditDataOperation(codeArea, position, charData);
                break;
            }
            case OVERWRITE: {
                activeOperation = new OverwriteCharEditDataOperation(codeArea, position, charData);
                break;
            }
            case DELETE: {
                activeOperation = new DeleteCharEditDataOperation(codeArea, position, charData);
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(commandType);
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
        command.execute();
        if (command instanceof EditCharDataCommand && activeOperation instanceof BinaryDataAppendableOperation) {
            return ((BinaryDataAppendableOperation) activeOperation).appendOperation(((EditCharDataCommand) command).activeOperation);
        }

        return false;
    }

    @Override
    public EditCommandType getCommandType() {
        return commandType;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (activeOperation != null) {
            activeOperation.dispose();
        }
    }
}
