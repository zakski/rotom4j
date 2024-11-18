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

import com.szadowsz.gui.component.binary.RCodeAreaCore;
import com.szadowsz.gui.component.binary.auxiliary.binary_data.BinaryData;
import com.szadowsz.gui.component.binary.capability.CaretCapable;
import com.szadowsz.gui.component.binary.operation.swing.InsertDataOperation;
/**
 * Command for inserting data.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class InsertDataCommand extends OpCodeAreaCommand {

    protected final long position;
    protected final long dataLength;

    public InsertDataCommand(RCodeAreaCore codeArea, long position, int codeOffset, BinaryData data) {
        super(codeArea);
        this.position = position;
        dataLength = data.getDataSize();
        super.setOperation(new InsertDataOperation(codeArea, position, codeOffset, data));
    }

    @Override
    public CodeAreaCommandType getType() {
        return CodeAreaCommandType.DATA_INSERTED;
    }

    @Override
    public void redo() {
        super.redo();
        ((CaretCapable) codeArea).setActiveCaretPosition(position + dataLength);
    }

    @Override
    public void undo() {
        super.undo();
        ((CaretCapable) codeArea).setActiveCaretPosition(position);
    }
}
