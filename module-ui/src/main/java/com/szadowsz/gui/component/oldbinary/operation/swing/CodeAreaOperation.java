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


import com.szadowsz.gui.component.oldbinary.CodeAreaCaretPosition;
import com.szadowsz.gui.component.oldbinary.DefaultCodeAreaCaretPosition;
import com.szadowsz.gui.component.oldbinary.RCodeAreaCore;
import com.szadowsz.gui.component.oldbinary.operation.undo.BinaryDataUndoableOperation;

/**
 * Abstract class for operation on code area component.
 *
 * @author ExBin Project (https://exbin.org)
 */
public abstract class CodeAreaOperation implements BinaryDataUndoableOperation {

    protected final RCodeAreaCore codeArea;
    protected final DefaultCodeAreaCaretPosition backPosition = new DefaultCodeAreaCaretPosition();

    public CodeAreaOperation(RCodeAreaCore codeArea) {
        this(codeArea, null);
    }

    public CodeAreaOperation(RCodeAreaCore codeArea, CodeAreaCaretPosition backPosition) {
        this.codeArea = codeArea;
        if (backPosition != null) {
            this.backPosition.setPosition(backPosition);
        }
    }

    public RCodeAreaCore getCodeArea() {
        return codeArea;
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
