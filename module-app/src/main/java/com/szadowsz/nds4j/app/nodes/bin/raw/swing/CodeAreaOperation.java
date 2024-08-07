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

import com.szadowsz.nds4j.app.nodes.bin.raw.CodeAreaCaretPosition;
import com.szadowsz.nds4j.app.nodes.bin.raw.operation.undo.BinaryDataUndoableOperation;

/**
 * Abstract class for operation on code area component.
 *
 * @author ExBin Project (https://exbin.org)
 */
public abstract class CodeAreaOperation implements BinaryDataUndoableOperation {

    protected final CodeAreaSwing codeArea;
    protected final CodeAreaCaretPosition backPosition = new CodeAreaCaretPosition();

    public CodeAreaOperation(CodeAreaSwing codeArea) {
        this(codeArea, null);
    }

    public CodeAreaOperation(CodeAreaSwing codeArea, CodeAreaCaretPosition backPosition) {
        this.codeArea = codeArea;
        if (backPosition != null) {
            this.backPosition.setPosition(backPosition);
        }
    }

    /**
     * Returns type of the operation.
     *
     * @return operation type
     */
    public abstract CodeAreaOperationType getType();

    public CodeAreaSwing getCodeArea() {
        return codeArea;
    }

    /**
     * Returns caption as text.
     *
     * @return text caption
     */
    @Override
    public String getName() {
        return getType().getName();
    }

    public CodeAreaCaretPosition getBackPosition() {
        return backPosition;
    }

    public void setBackPosition(CodeAreaCaretPosition backPosition) {
        this.backPosition.setPosition(backPosition);
    }

    /**
     * Performs dispose of the operation's resources.
     * <p>
     * Default dispose is empty.
     */
    @Override
    public void dispose() {
    }
}
